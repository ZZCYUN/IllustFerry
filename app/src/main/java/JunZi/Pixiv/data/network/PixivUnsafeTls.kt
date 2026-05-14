package JunZi.Pixiv.data.network

import android.annotation.SuppressLint
import java.net.InetAddress
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
object PixivUnsafeTls {
    val trustManager: X509TrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    val hostnameVerifier = HostnameVerifier { _, _ -> true }

    fun socketFactory(): SSLSocketFactory {
        val context = SSLContext.getInstance("TLS")
        context.init(null, arrayOf(trustManager), SecureRandom())
        return RubyStyleSocketFactory(context.socketFactory)
    }

    private class RubyStyleSocketFactory(
        private val delegate: SSLSocketFactory,
    ) : SSLSocketFactory() {
        override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

        override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

        override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket {
            if (!host.needsRubyStyleSocket()) {
                return delegate.createSocket(socket, host, port, autoClose).withAllProtocols()
            }
            val address = socket.inetAddress
            if (autoClose) {
                runCatching { socket.close() }
            }
            return delegate.createSocket(address, port).withAllProtocols()
        }

        override fun createSocket(host: String, port: Int): Socket {
            return delegate.createSocket(host, port).withAllProtocols()
        }

        override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
            return delegate.createSocket(host, port, localHost, localPort).withAllProtocols()
        }

        override fun createSocket(host: InetAddress, port: Int): Socket {
            return delegate.createSocket(host, port).withAllProtocols()
        }

        override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
            return delegate.createSocket(address, port, localAddress, localPort).withAllProtocols()
        }

        private fun Socket.withAllProtocols(): Socket {
            if (this is SSLSocket) {
                enabledProtocols = supportedProtocols
            }
            return this
        }

        private fun String.needsRubyStyleSocket(): Boolean {
            val normalized = lowercase()
            return normalized.isIpAddress() ||
                PixivHost.from(normalized) != null ||
                normalized == "i.pximg.net" ||
                normalized == "s.pximg.net"
        }

        private fun String.isIpAddress(): Boolean {
            return matches(Regex("""\d{1,3}(\.\d{1,3}){3}"""))
        }
    }
}
