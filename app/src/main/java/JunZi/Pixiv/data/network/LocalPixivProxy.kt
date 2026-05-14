package JunZi.Pixiv.data.network

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.KeyStore
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Provider
import java.security.SecureRandom
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Date
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

class LocalPixivProxy(
    preferredPort: Int = 7891,
) {
    private val running = AtomicBoolean(false)
    private val activeSockets = ConcurrentHashMap.newKeySet<Socket>()
    private val certificateAuthority by lazy { inMemoryCertificateAuthority }
    private val serverSslContexts = ConcurrentHashMap<String, SSLContext>()
    @Volatile
    private var executor: ExecutorService? = null
    private var serverSocket: ServerSocket? = null

    var port: Int = preferredPort
        private set

    @Synchronized
    fun start() {
        if (running.get()) return
        lastProxyError = null
        lastProxyEvent = null
        reportState("start requested")
        val newExecutor = Executors.newCachedThreadPool()
        val newServerSocket = bindServerSocket(port)
        executor = newExecutor
        serverSocket = newServerSocket.also { port = it.localPort }
        running.set(true)
        reportState("listening 127.0.0.1:${newServerSocket.localPort}")
        newExecutor.execute { acceptLoop(newServerSocket, newExecutor) }
    }

    @Synchronized
    fun stop() {
        running.set(false)
        reportState("stop requested")
        runCatching { serverSocket?.close() }
        serverSocket = null
        activeSockets.forEach { socket ->
            runCatching { socket.close() }
        }
        activeSockets.clear()
        executor?.shutdownNow()
        executor = null
    }

    fun isProxyCertificate(host: String?, certificate: X509Certificate?): Boolean {
        val normalized = normalizeHost(host) ?: return false
        if (!isMitmHost(normalized) || certificate == null) return false
        return runCatching {
            val ca = certificateAuthority
            certificate.verify(ca.certificate.publicKey)
            certificate.issuerX500Principal == ca.certificate.subjectX500Principal &&
                certificate.matchesHost(normalized)
        }.getOrDefault(false)
    }

    private fun bindServerSocket(preferredPort: Int): ServerSocket {
        return runCatching {
            ServerSocket(preferredPort, 64, InetAddress.getByName("127.0.0.1"))
        }.getOrElse {
            ServerSocket(0, 64, InetAddress.getByName("127.0.0.1"))
        }
    }

    private fun acceptLoop(server: ServerSocket, executor: ExecutorService) {
        while (running.get()) {
            val client = runCatching { server.accept() }.getOrNull() ?: break
            reportState("accepted ${client.inetAddress.hostAddress}:${client.port}")
            trackSocket(client)
            runCatching {
                executor.execute { handleClient(client, executor) }
            }.onFailure {
                reportError("acceptLoop dispatch", it)
                closeAndForget(client)
            }
        }
        if (running.get()) {
            reportState("accept loop ended unexpectedly")
        }
    }

    private fun handleClient(client: Socket, executor: ExecutorService) {
        try {
            client.use { source ->
                source.soTimeout = 20_000
                val input = source.getInputStream()
                val firstLine = input.readAsciiLine() ?: return
                reportState("request $firstLine")
                val requestLine = parseRequestLine(firstLine)
                if (requestLine == null) {
                    sendSimpleResponse(source, 400, "Bad Request")
                    return
                }
                if (requestLine.method.equals("CONNECT", ignoreCase = true)) {
                    handleConnect(source, input, requestLine, executor)
                } else {
                    handleHttp(source, input, requestLine)
                }
            }
        } catch (throwable: Throwable) {
            reportError("handleClient", throwable)
        } finally {
            activeSockets.remove(client)
        }
    }

    private fun handleConnect(
        client: Socket,
        clientInput: InputStream,
        requestLine: RequestLine,
        executor: ExecutorService,
    ) {
        val target = parseAuthority(requestLine.target, defaultPort = 443)
        if (target == null) {
            drainHeaders(clientInput)
            sendSimpleResponse(client, 400, "Bad Request")
            return
        }
        drainHeaders(clientInput)

        var tunnelEstablished = false
        try {
            val clientOutput = client.getOutputStream()
            clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(Charsets.US_ASCII))
            clientOutput.flush()
            tunnelEstablished = true
            reportState("connect established ${target.host}:${target.port}")

            if (shouldMitm(target.host, target.port)) {
                reportState("mitm selected ${target.host}:${target.port}")
                handleMitmTunnel(client, target, executor)
            } else {
                reportState("transparent selected ${target.host}:${target.port}")
                handleTransparentTunnel(client, target, executor)
            }
        } catch (throwable: Throwable) {
            reportError("handleConnect ${target.host}:${target.port}", throwable)
            if (!tunnelEstablished) {
                sendSimpleResponse(client, 502, "Bad Gateway")
            }
        }
    }

    private fun handleTransparentTunnel(
        client: Socket,
        target: Authority,
        executor: ExecutorService,
    ) {
        val upstream = connectUpstream(target.host, target.port, 20_000)
        upstream.soTimeout = 0
        client.soTimeout = 0
        try {
            proxySockets(
                clientInput = client.getInputStream(),
                clientOutput = client.getOutputStream(),
                clientSocket = client,
                upstreamInput = upstream.getInputStream(),
                upstreamOutput = upstream.getOutputStream(),
                upstreamSocket = upstream,
                executor = executor,
            )
        } finally {
            closeAndForget(upstream)
        }
    }

    private fun handleMitmTunnel(
        client: Socket,
        target: Authority,
        executor: ExecutorService,
    ) {
        val upstream = connectUpstream(target.host, target.port, 20_000)
        upstream.soTimeout = 0
        client.soTimeout = 0
        reportState("client tls handshake ${target.host}:${target.port}")
        val clientTls = createClientFacingTlsSocket(client, target.host)
        reportState(
            "upstream tls handshake ${target.host}:${target.port} via " +
                "${upstream.inetAddress.hostAddress}:${upstream.port}",
        )
        val upstreamTls = createUpstreamTlsSocket(upstream, target.host, target.port)
        try {
            proxySockets(
                clientInput = clientTls.getInputStream(),
                clientOutput = clientTls.getOutputStream(),
                clientSocket = clientTls,
                upstreamInput = upstreamTls.getInputStream(),
                upstreamOutput = upstreamTls.getOutputStream(),
                upstreamSocket = upstreamTls,
                executor = executor,
            )
        } finally {
            runCatching { clientTls.close() }
            runCatching { upstreamTls.close() }
            closeAndForget(upstream)
        }
    }

    private fun handleHttp(client: Socket, clientInput: InputStream, requestLine: RequestLine) {
        val headers = readHeaders(clientInput) ?: run {
            sendSimpleResponse(client, 400, "Bad Request")
            return
        }
        val target = parseHttpTarget(requestLine.target, headers) ?: run {
            sendSimpleResponse(client, 400, "Bad Request")
            return
        }
        val contentLength = headers.contentLength()
        val chunked = headers.isChunked()
        val expectsContinue = headers.expectsContinue()
        if (expectsContinue) {
            sendContinue(client)
        }

        var upstream: Socket? = null
        var responseStarted = false
        try {
            val server = connectUpstream(target.host, target.port, 20_000)
            upstream = server
            server.soTimeout = 20_000

            val upstreamOutput = server.getOutputStream()
            upstreamOutput.write(
                buildHttpRequestHead(
                    requestLine = requestLine,
                    target = target,
                    headers = headers,
                    skipExpect = expectsContinue,
                ),
            )
            if (chunked) {
                copyChunkedBody(clientInput, upstreamOutput)
            } else if (contentLength != null && contentLength > 0) {
                copyFixedLength(clientInput, upstreamOutput, contentLength)
            }
            upstreamOutput.flush()

            server.getInputStream().copyResponseTo(client.getOutputStream()) {
                responseStarted = true
            }
        } catch (throwable: Throwable) {
            reportError("handleHttp ${target.host}:${target.port}", throwable)
            if (!responseStarted) {
                sendSimpleResponse(client, 502, "Bad Gateway")
            }
        } finally {
            upstream?.let { closeAndForget(it) }
        }
    }

    private fun connectUpstream(host: String, port: Int, timeoutMillis: Int): Socket {
        val candidates = (PixivNetworkConfig.addressesFor(host) + host).distinct()
        var lastFailure: IOException? = null
        candidates.forEach { candidate ->
            val socket = trackSocket(Socket())
            try {
                reportState("dial $host:$port via $candidate")
                socket.connect(InetSocketAddress(candidate, port), timeoutMillis)
                reportState("dial success $host:$port via $candidate")
                return socket
            } catch (error: IOException) {
                lastFailure = error
                reportError("dial $host:$port via $candidate", error)
                closeAndForget(socket)
            }
        }
        throw lastFailure ?: IOException("No upstream address for $host")
    }

    private fun proxySockets(
        clientInput: InputStream,
        clientOutput: OutputStream,
        clientSocket: Socket,
        upstreamInput: InputStream,
        upstreamOutput: OutputStream,
        upstreamSocket: Socket,
        executor: ExecutorService,
    ) {
        val clientToServer = executor.submit {
            clientInput.copyToAndClose(upstreamOutput, upstreamSocket, clientSocket)
        }
        val serverToClient = executor.submit {
            upstreamInput.copyToAndClose(clientOutput, upstreamSocket, clientSocket)
        }
        runCatching { clientToServer.get() }
        runCatching { serverToClient.get() }
    }

    private fun shouldMitm(host: String, port: Int): Boolean {
        if (port != 443) return false
        return isMitmHost(host)
    }

    private fun createClientFacingTlsSocket(client: Socket, host: String): SSLSocket {
        val socket = (serverSslContextFor(host).socketFactory.createSocket(client, host, client.port, false) as SSLSocket)
        socket.useClientMode = false
        socket.needClientAuth = false
        socket.startHandshake()
        return socket
    }

    private fun createUpstreamTlsSocket(upstream: Socket, host: String, port: Int): SSLSocket {
        val dialHost = upstream.inetAddress.hostAddress ?: host
        reportState("upstream tls strategy direct-ip $host via $dialHost:$port")
        val socket = (PixivUnsafeTls.socketFactory().createSocket(upstream, dialHost, port, true) as SSLSocket)
        socket.useClientMode = true
        socket.enabledProtocols = socket.supportedProtocols
        socket.startHandshake()
        return socket
    }

    private fun serverSslContextFor(host: String): SSLContext {
        val normalized = host.trim().lowercase(Locale.US).removeSuffix(".")
        return serverSslContexts.getOrPut(normalized) {
            lastProxyEvent = "build server ssl context $normalized"
            stage("build server ssl context $normalized") {
                val leafKeyPair = stage("build leaf keypair $normalized") { generateKeyPair() }
                val ca = stage("load ca $normalized") { certificateAuthority }
                val issuer = X500Name(ca.certificate.subjectX500Principal.name)
                val subject = X500Name("CN=$normalized")
                val notBefore = Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L)
                val notAfter = Date(System.currentTimeMillis() + 365L * 24L * 60L * 60L * 1000L)
                val builder = JcaX509v3CertificateBuilder(
                    issuer,
                    randomSerialNumber(),
                    notBefore,
                    notAfter,
                    subject,
                    leafKeyPair.public,
                )
                builder.addExtension(Extension.basicConstraints, true, BasicConstraints(false))
                builder.addExtension(
                    Extension.keyUsage,
                    true,
                    KeyUsage(KeyUsage.digitalSignature or KeyUsage.keyEncipherment),
                )
                builder.addExtension(
                    Extension.extendedKeyUsage,
                    false,
                    ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth),
                )
                builder.addExtension(
                    Extension.subjectAlternativeName,
                    false,
                    GeneralNames(GeneralName(GeneralName.dNSName, normalized)),
                )
                builder.addExtension(
                    Extension.subjectKeyIdentifier,
                    false,
                    extensionUtils.createSubjectKeyIdentifier(leafKeyPair.public),
                )
                builder.addExtension(
                    Extension.authorityKeyIdentifier,
                    false,
                    extensionUtils.createAuthorityKeyIdentifier(ca.certificate),
                )
                val signer = stage("build leaf signer $normalized") { buildContentSigner(ca.privateKey) }
                val holder = stage("build leaf certificate holder $normalized") { builder.build(signer) }
                val leafCertificate = stage("convert leaf certificate $normalized") { convertCertificate(holder) }
                stage("verify leaf certificate $normalized") { leafCertificate.verify(ca.certificate.publicKey) }

                val keyStore = stage("create keystore $normalized") {
                    KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                        load(null, null)
                        setKeyEntry(
                            "puxiv-local-proxy-$normalized",
                            leafKeyPair.private,
                            CharArray(0),
                            arrayOf<X509Certificate>(leafCertificate, ca.certificate),
                        )
                    }
                }
                val keyManagers = stage("create key managers $normalized") {
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                        init(keyStore, CharArray(0))
                    }.keyManagers
                }
                stage("create ssl context $normalized") {
                    SSLContext.getInstance("TLS").apply {
                        init(keyManagers, null, secureRandom)
                    }
                }
            }
        }
    }

    private fun drainHeaders(input: InputStream) {
        while (true) {
            val line = input.readAsciiLine() ?: return
            if (line.isEmpty()) return
        }
    }

    private fun readHeaders(input: InputStream): List<HttpHeader>? {
        val headers = mutableListOf<HttpHeader>()
        while (true) {
            val line = input.readAsciiLine() ?: return null
            if (line.isEmpty()) return headers
            val separator = line.indexOf(':')
            if (separator <= 0) return null
            headers += HttpHeader(
                name = line.substring(0, separator),
                value = line.substring(separator + 1).trimStart(),
            )
        }
    }

    private fun parseRequestLine(line: String): RequestLine? {
        val parts = line.trim().split(Regex("\\s+"), limit = 3)
        if (parts.size != 3) return null
        val method = parts[0]
        val target = parts[1]
        val version = parts[2]
        if (method.isBlank() || target.isBlank() || !version.startsWith("HTTP/", ignoreCase = true)) return null
        return RequestLine(method = method, target = target, version = version)
    }

    private fun parseHttpTarget(requestTarget: String, headers: List<HttpHeader>): HttpTarget? {
        val uri = runCatching { URI(requestTarget) }.getOrNull()
        if (uri?.isAbsolute == true) {
            if (!uri.scheme.equals("http", ignoreCase = true)) return null
            val host = uri.host ?: parseAuthority(uri.rawAuthority.orEmpty(), defaultPort = 80)?.host ?: return null
            val explicitPort = uri.port != -1
            val port = if (explicitPort) uri.port else 80
            return HttpTarget(
                host = host,
                port = port,
                hostHeader = buildHostHeader(host, port, explicitPort),
                originTarget = buildOriginTarget(uri),
            )
        }

        val hostHeader = headers.lastOrNull { it.name.equals("Host", ignoreCase = true) }?.value ?: return null
        val authority = parseAuthority(hostHeader.trim(), defaultPort = 80) ?: return null
        return HttpTarget(
            host = authority.host,
            port = authority.port,
            hostHeader = hostHeader.trim(),
            originTarget = requestTarget.ifEmpty { "/" },
        )
    }

    private fun parseAuthority(authority: String, defaultPort: Int): Authority? {
        if (authority.isBlank()) return null
        val host: String
        val port: Int
        if (authority.startsWith("[")) {
            val endBracket = authority.indexOf(']')
            if (endBracket <= 1) return null
            host = authority.substring(1, endBracket)
            port = if (authority.length > endBracket + 1 && authority[endBracket + 1] == ':') {
                authority.substring(endBracket + 2).toIntOrNull() ?: return null
            } else {
                defaultPort
            }
        } else {
            val separator = authority.lastIndexOf(':').takeIf { it == authority.indexOf(':') }
            host = if (separator == null || separator < 0) authority else authority.substring(0, separator)
            port = if (separator == null || separator < 0) {
                defaultPort
            } else {
                authority.substring(separator + 1).toIntOrNull() ?: return null
            }
        }
        if (host.isBlank() || port !in 1..65535) return null
        return Authority(host = host, port = port)
    }

    private fun buildOriginTarget(uri: URI): String {
        val path = uri.rawPath?.takeIf { it.isNotEmpty() } ?: "/"
        val query = uri.rawQuery
        return if (query == null) path else "$path?$query"
    }

    private fun buildHostHeader(host: String, port: Int, explicitPort: Boolean): String {
        val formattedHost = if (host.contains(':') && !host.startsWith("[")) "[$host]" else host
        return if (explicitPort) "$formattedHost:$port" else formattedHost
    }

    private fun buildHttpRequestHead(
        requestLine: RequestLine,
        target: HttpTarget,
        headers: List<HttpHeader>,
        skipExpect: Boolean,
    ): ByteArray {
        val builder = StringBuilder()
        builder.append(requestLine.method)
            .append(' ')
            .append(target.originTarget)
            .append(' ')
            .append(requestLine.version)
            .append("\r\n")
        builder.append("Host: ").append(target.hostHeader).append("\r\n")
        headers.forEach { header ->
            val name = header.name.lowercase(Locale.US)
            if (name == "host" ||
                name == "proxy-connection" ||
                name == "connection" ||
                name == "keep-alive" ||
                name == "proxy-authorization" ||
                (skipExpect && name == "expect")
            ) {
                return@forEach
            }
            builder.append(header.name).append(": ").append(header.value).append("\r\n")
        }
        builder.append("Connection: close\r\n")
        builder.append("\r\n")
        return builder.toString().toByteArray(Charsets.ISO_8859_1)
    }

    private fun List<HttpHeader>.contentLength(): Long? {
        return lastOrNull { it.name.equals("Content-Length", ignoreCase = true) }
            ?.value
            ?.trim()
            ?.toLongOrNull()
            ?.takeIf { it >= 0 }
    }

    private fun List<HttpHeader>.isChunked(): Boolean {
        return any { header ->
            header.name.equals("Transfer-Encoding", ignoreCase = true) &&
                header.value.split(',')
                    .any { it.trim().equals("chunked", ignoreCase = true) }
        }
    }

    private fun List<HttpHeader>.expectsContinue(): Boolean {
        return any { header ->
            header.name.equals("Expect", ignoreCase = true) &&
                header.value.equals("100-continue", ignoreCase = true)
        }
    }

    private fun copyFixedLength(input: InputStream, output: OutputStream, byteCount: Long) {
        var remaining = byteCount
        val buffer = ByteArray(16 * 1024)
        while (remaining > 0) {
            val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (read == -1) throw IOException("Unexpected end of request body")
            output.write(buffer, 0, read)
            remaining -= read
        }
    }

    private fun copyChunkedBody(input: InputStream, output: OutputStream) {
        while (true) {
            val sizeLine = input.readAsciiLine() ?: throw IOException("Unexpected end of chunked request body")
            writeAsciiLine(output, sizeLine)
            val size = sizeLine.substringBefore(';').trim().toLongOrNull(radix = 16)
                ?: throw IOException("Invalid chunk size")
            if (size == 0L) {
                copyTrailers(input, output)
                return
            }
            copyFixedLength(input, output, size)
            copyChunkEnding(input, output)
        }
    }

    private fun copyTrailers(input: InputStream, output: OutputStream) {
        while (true) {
            val line = input.readAsciiLine() ?: throw IOException("Unexpected end of chunk trailers")
            writeAsciiLine(output, line)
            if (line.isEmpty()) return
        }
    }

    private fun copyChunkEnding(input: InputStream, output: OutputStream) {
        val first = input.read()
        val second = input.read()
        if (first != '\r'.code || second != '\n'.code) throw IOException("Invalid chunk ending")
        output.write(first)
        output.write(second)
    }

    private fun writeAsciiLine(output: OutputStream, line: String) {
        output.write(line.toByteArray(Charsets.ISO_8859_1))
        output.write("\r\n".toByteArray(Charsets.ISO_8859_1))
    }

    private fun InputStream.copyResponseTo(output: OutputStream, onFirstByte: () -> Unit) {
        val buffer = ByteArray(16 * 1024)
        var started = false
        while (true) {
            val read = read(buffer)
            if (read == -1) break
            if (!started) {
                started = true
                onFirstByte()
            }
            output.write(buffer, 0, read)
        }
        output.flush()
    }

    private fun InputStream.readAsciiLine(): String? {
        val bytes = ArrayList<Byte>(128)
        while (true) {
            val next = read()
            if (next == -1) return if (bytes.isEmpty()) null else bytes.toByteArray().toString(Charsets.US_ASCII)
            if (next == '\n'.code) {
                if (bytes.lastOrNull() == '\r'.code.toByte()) {
                    bytes.removeAt(bytes.lastIndex)
                }
                return bytes.toByteArray().toString(Charsets.US_ASCII)
            }
            bytes += next.toByte()
        }
    }

    private fun sendContinue(socket: Socket) {
        runCatching {
            socket.getOutputStream().write("HTTP/1.1 100 Continue\r\n\r\n".toByteArray(Charsets.US_ASCII))
            socket.getOutputStream().flush()
        }
    }

    private fun sendSimpleResponse(socket: Socket, statusCode: Int, reason: String) {
        runCatching {
            socket.getOutputStream().write(
                "HTTP/1.1 $statusCode $reason\r\nConnection: close\r\nContent-Length: 0\r\n\r\n"
                    .toByteArray(Charsets.US_ASCII),
            )
            socket.getOutputStream().flush()
        }
    }

    private fun trackSocket(socket: Socket): Socket {
        activeSockets += socket
        if (!running.get()) {
            runCatching { socket.close() }
        }
        return socket
    }

    private fun closeAndForget(socket: Socket) {
        runCatching { socket.close() }
        activeSockets.remove(socket)
    }

    private fun InputStream.copyToAndClose(
        output: java.io.OutputStream,
        upstream: Socket,
        client: Socket,
    ) {
        runCatching {
            copyTo(output, bufferSize = 16 * 1024)
            output.flush()
        }
        runCatching { upstream.close() }
        runCatching { client.close() }
    }

    private fun reportState(message: String) {
        lastProxyEvent = message
        Log.d(TAG, message)
    }

    private fun reportError(stage: String, throwable: Throwable) {
        val chain = buildString {
            var current: Throwable? = throwable
            var depth = 0
            while (current != null && depth < 4) {
                if (isNotEmpty()) append(" <= ")
                append(current.javaClass.simpleName)
                current.message?.takeIf { it.isNotBlank() }?.let {
                    append(": ").append(it)
                }
                current = current.cause
                depth += 1
            }
        }
        val summary = "$stage: $chain"
        lastProxyError = summary
        Log.e(TAG, summary, throwable)
    }

    private data class RequestLine(
        val method: String,
        val target: String,
        val version: String,
    )

    private data class HttpHeader(
        val name: String,
        val value: String,
    )

    private data class Authority(
        val host: String,
        val port: Int,
    )

    private data class HttpTarget(
        val host: String,
        val port: Int,
        val hostHeader: String,
        val originTarget: String,
    )

    companion object {
        val MITM_EXACT_HOSTS = setOf(
            "pixiv.net",
            "pximg.net",
            "d.pixiv.org",
            "i.pximg.net",
            "s.pximg.net",
            "i1.pixiv.net",
            "i2.pixiv.net",
            "i3.pixiv.net",
            "i4.pixiv.net",
            "www.pixiv.net",
            "dic.pixiv.net",
            "touch.pixiv.net",
            "imgaz.pixiv.net",
            "comic.pixiv.net",
            "novel.pixiv.net",
            "pixivsketch.net",
            "pixiv.pximg.net",
            "source.pixiv.net",
            "sketch.pixiv.net",
            "sensei.pixiv.net",
            "en-dic.pixiv.net",
            "fanbox.pixiv.net",
            "app-api.pixiv.net",
            "factory.pixiv.net",
            "payment.pixiv.net",
            "accounts.pixiv.net",
            "oauth.secure.pixiv.net",
            "g-client-proxy.pixiv.net",
        )

        val secureRandom = SecureRandom()

        private val inMemoryCertificateAuthority: CertificateAuthority by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            stage("generate in-memory ca") {
                val keyPair = stage("build ca keypair") { generateKeyPair() }
                val subject = X500Name("CN=Puxiv Local Proxy CA")
                val notBefore = Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L)
                val notAfter = Date(System.currentTimeMillis() + 3650L * 24L * 60L * 60L * 1000L)
                val builder = JcaX509v3CertificateBuilder(
                    subject,
                    randomSerialNumber(),
                    notBefore,
                    notAfter,
                    subject,
                    keyPair.public,
                )
                builder.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
                builder.addExtension(
                    Extension.keyUsage,
                    true,
                    KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign),
                )
                builder.addExtension(
                    Extension.subjectKeyIdentifier,
                    false,
                    extensionUtils.createSubjectKeyIdentifier(keyPair.public),
                )
                val signer = stage("build ca signer") { buildContentSigner(keyPair.private) }
                val holder = stage("build ca certificate holder") { builder.build(signer) }
                val certificate = stage("convert ca certificate") { convertCertificate(holder) }
                stage("verify in-memory ca certificate") { certificate.verify(certificate.publicKey) }
                CertificateAuthority(privateKey = keyPair.private, certificate = certificate)
            }
        }

        val permissiveTrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }

        val extensionUtils by lazy {
            JcaX509ExtensionUtils()
        }

        val clientSslContext: SSLContext by lazy {
            SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(permissiveTrustManager), SecureRandom())
            }
        }

        fun generateKeyPair(): KeyPair {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048, secureRandom)
            return generator.generateKeyPair()
        }

        fun randomSerialNumber(): java.math.BigInteger {
            return java.math.BigInteger(128, secureRandom).abs()
        }

        fun isMitmHost(host: String?): Boolean {
            return normalizeHost(host) in MITM_EXACT_HOSTS
        }

        fun normalizeHost(host: String?): String? {
            return host?.trim()?.lowercase(Locale.US)?.removeSuffix(".")?.takeIf { it.isNotEmpty() }
        }

        fun X509Certificate.matchesHost(host: String): Boolean {
            val dnsNames = subjectAlternativeNames
                ?.mapNotNull { name ->
                    val type = name.getOrNull(0) as? Int
                    val value = name.getOrNull(1) as? String
                    value?.takeIf { type == GeneralName.dNSName }
                }
                .orEmpty()
            if (dnsNames.isNotEmpty()) return host in dnsNames.map { it.lowercase(Locale.US).removeSuffix(".") }
            return subjectX500Principal.name
                .split(',')
                .map { it.trim() }
                .any { it.equals("CN=$host", ignoreCase = true) }
        }

        val bundledBouncyCastleProvider: Provider by lazy {
            BouncyCastleProvider()
        }

        fun ensureBouncyCastle() {
            val existing = Security.getProvider(BOUNCY_CASTLE_PROVIDER)
            if (existing == null || existing.javaClass.name != bundledBouncyCastleProvider.javaClass.name) {
                existing?.let { Security.removeProvider(BOUNCY_CASTLE_PROVIDER) }
                Security.insertProviderAt(bundledBouncyCastleProvider, 1)
            }
        }

        fun convertCertificate(holder: X509CertificateHolder): X509Certificate {
            ensureBouncyCastle()
            val attempts = listOf(
                { JcaX509CertificateConverter().setProvider(bundledBouncyCastleProvider).getCertificate(holder) },
                { JcaX509CertificateConverter().getCertificate(holder) },
            )
            var lastError: Throwable? = null
            attempts.forEach { attempt ->
                try {
                    return attempt()
                } catch (throwable: Throwable) {
                    lastError = throwable
                }
            }
            throw lastError ?: IllegalStateException("Unable to create certificate converter")
        }

        fun buildContentSigner(privateKey: java.security.PrivateKey): ContentSigner {
            ensureBouncyCastle()
            val attempts = listOf(
                { JcaContentSignerBuilder("SHA256withRSA").setProvider(bundledBouncyCastleProvider).build(privateKey) },
                { JcaContentSignerBuilder("SHA256withRSA").build(privateKey) },
                { JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(bundledBouncyCastleProvider).build(privateKey) },
                { JcaContentSignerBuilder("SHA256WithRSAEncryption").build(privateKey) },
            )
            var lastError: Throwable? = null
            attempts.forEach { attempt ->
                try {
                    return attempt()
                } catch (throwable: Throwable) {
                    lastError = throwable
                }
            }
            throw lastError ?: IllegalStateException("Unable to create content signer")
        }

        inline fun <T> stage(label: String, block: () -> T): T {
            lastProxyEvent = label
            return try {
                block()
            } catch (throwable: Throwable) {
                throw IllegalStateException(label, throwable)
            }
        }

        const val BOUNCY_CASTLE_PROVIDER = "BC"
        const val TAG = "LocalPixivProxy"

        @Volatile
        var lastProxyError: String? = null

        @Volatile
        var lastProxyEvent: String? = null

        data class CertificateAuthority(
            val privateKey: PrivateKey,
            val certificate: X509Certificate,
        )
    }
}
