package JunZi.Pixiv.data.network

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Go 编写的 ECH/无 SNI 隧道胶水层（illusttunnel.aar，由 gomobile bind 生成）。
 *
 * Go crypto/tls 原生支持 Encrypted Client Hello（draft-13, ECHConfigList）：
 *   - echConfigB64 非空 → TCP + TLS 1.3 + ECH（外层 SNI 用 ECHConfig 的 public_name，
 *     内层 SNI 用 [serverName]，完整证书链与主机名校验）
 *   - echConfigB64 为空 → TCP + TLS 1.3 无 SNI（适用于按 IP 直连且无法预知证书域名的
 *     场景，握手后按证书 SAN 补主机名校验）
 *
 * JNI 细节全部由 gomobile 绑定层处理；Go 侧完成 TLS 后隧道里就是明文字节流。
 */
object EchTunnel {
    /** gomobile aar 是编译期依赖，库必然可用；保留标志位以兼容旧的回退逻辑。 */
    val available: Boolean = true

    /** 打开隧道，返回句柄（>0）；失败抛 [IOException]，错误码语义保留给日志。 */
    fun open(ip: String, serverName: String, echConfigB64: String, port: Int): Long {
        return try {
            illusttunnel.Illusttunnel.open(ip, serverName, echConfigB64, port.toLong())
        } catch (e: Exception) {
            throw IOException("tunnel open $serverName via $ip:$port failed: ${e.message}", e)
        }
    }

    fun write(handle: Long, data: ByteArray, off: Int, len: Int): Int {
        val payload = if (off == 0 && len == data.size) data else data.copyOfRange(off, off + len)
        return try {
            illusttunnel.Illusttunnel.write(handle, payload).toInt()
        } catch (e: Exception) {
            throw IOException("tunnel write failed: ${e.message}", e)
        }
    }

    /** 阻塞读。返回 0 表示对端关闭，异常抛 [IOException]。 */
    fun read(handle: Long, buf: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        val chunk = if (off == 0 && len == buf.size) buf else ByteArray(len)
        val n = try {
            illusttunnel.Illusttunnel.read(handle, chunk).toInt()
        } catch (e: Exception) {
            throw IOException("tunnel read failed: ${e.message}", e)
        }
        if (n > 0 && chunk !== buf) {
            System.arraycopy(chunk, 0, buf, off, n)
        }
        return n
    }

    fun close(handle: Long) {
        runCatching { illusttunnel.Illusttunnel.close(handle) }
    }

    fun lastError(handle: Long): String? = runCatching {
        illusttunnel.Illusttunnel.lastError(handle)
    }.getOrNull()?.takeIf { it.isNotBlank() }
}

/**
 * 把 Go 隧道句柄包装成 [Socket]，供 LocalPixivProxy 的 MITM 上游直接使用。
 * TLS 已在 Go 侧完成，这里的流是明文 HTTP。
 */
class TunnelSocket(
    private val handle: Long,
    private val upstreamIp: String,
) : Socket() {
    private val closed = AtomicBoolean(false)

    override fun getInputStream(): InputStream = TunnelInputStream(handle)

    override fun getOutputStream(): OutputStream = TunnelOutputStream(handle)

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            runCatching { EchTunnel.close(handle) }
        }
    }

    override fun isClosed(): Boolean = closed.get()

    override fun isConnected(): Boolean = !closed.get()

    override fun isInputShutdown(): Boolean = closed.get()

    override fun isOutputShutdown(): Boolean = closed.get()

    override fun setSoTimeout(timeout: Int) = Unit

    override fun getSoTimeout(): Int = 0

    override fun shutdownInput() = Unit

    override fun shutdownOutput() = Unit

    override fun toString(): String = "TunnelSocket(handle=$handle via $upstreamIp)"

    private class TunnelInputStream(private val handle: Long) : InputStream() {
        private val buffer = ByteArray(BUFFER_SIZE)

        private val single = ByteArray(1)

        override fun read(): Int {
            val n = EchTunnel.read(handle, single, 0, 1)
            return if (n > 0) single[0].toInt() and 0xFF else -1
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            val chunk = minOf(len, BUFFER_SIZE)
            val n = EchTunnel.read(handle, buffer, 0, chunk)
            if (n > 0) {
                System.arraycopy(buffer, 0, b, off, n)
            }
            return n
        }

        override fun available(): Int = 0

        override fun close() {
            runCatching { EchTunnel.close(handle) }
        }

        companion object {
            private const val BUFFER_SIZE = 64 * 1024
        }
    }

    private class TunnelOutputStream(private val handle: Long) : OutputStream() {
        override fun write(b: Int) {
            writeOne(byteArrayOf(b.toByte()), 0, 1)
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            if (len == 0) return
            writeOne(b, off, len)
        }

        private fun writeOne(b: ByteArray, off: Int, len: Int) {
            EchTunnel.write(handle, b, off, len)
        }

        override fun flush() = Unit

        override fun close() {
            runCatching { EchTunnel.close(handle) }
        }
    }

    private companion object {
        fun tunnelError(handle: Long, fallback: String): String {
            val detail = EchTunnel.lastError(handle)
            return if (detail.isNullOrBlank()) fallback else "$fallback: $detail"
        }
    }
}
