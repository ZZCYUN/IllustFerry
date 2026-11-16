package JunZi.Pixiv.data.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** DoH 查询类型。 */
object DnsRecordType {
    const val A = 1
    const val HTTPS = 65
}

/**
 * RFC 8484 DNS-over-HTTPS 客户端（wire format，POST application/dns-message，
 * 失败回退 GET ?dns=base64url）。返回的是原始 RR rdata，由调用方按类型解析。
 */
class DohClient(private val endpoint: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    private val dnsMessage = "application/dns-message".toMediaType()

    /** 返回指定类型的全部 answer rdata；rcode != 0 或无 answer 返回空列表。 */
    fun query(name: String, type: Int): List<ByteArray> {
        val wire = DnsWire.buildQuery(name, type)
        runCatching { return postQuery(wire) }
        return runCatching { getQuery(wire) }.getOrElse { throw IOException("DoH $endpoint query $name type=$type failed", it) }
    }

    private fun postQuery(wire: ByteArray): List<ByteArray> {
        val request = Request.Builder()
            .url(endpoint)
            .post(wire.toRequestBody(dnsMessage))
            .header("accept", "application/dns-message")
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body.bytes()
            if (!response.isSuccessful) throw IOException("DoH HTTP ${response.code}")
            return DnsWire.parseAnswers(body, wantType = null)
        }
    }

    private fun getQuery(wire: ByteArray): List<ByteArray> {
        val encoded = android.util.Base64.encodeToString(wire, android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        val request = Request.Builder()
            .url("$endpoint?dns=$encoded")
            .get()
            .header("accept", "application/dns-message")
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body.bytes()
            if (!response.isSuccessful) throw IOException("DoH HTTP ${response.code}")
            return DnsWire.parseAnswers(body, wantType = null)
        }
    }

    /** 解析 A 记录为 IPv4 字符串列表。 */
    fun resolveA(name: String): List<String> =
        query(name, DnsRecordType.A).mapNotNull { DnsWire.parseARecord(it) }.distinct()

    /**
     * 解析 HTTPS RR，返回 SVCB key=5（ech）参数的 base64。
     * pixiv 域名自己的 HTTPS RR 不带 ech；ECH config 统一从公共配置域
     * cloudflare-ech.com 的 HTTPS RR 获取（同 Cloudflare 全 zone 通用）。
     */
    fun resolveEchConfig(name: String): String? =
        query(name, DnsRecordType.HTTPS).firstNotNullOfOrNull { DnsWire.parseHttpsEch(it) }
}

/** DNS wire format 的构造与解析（仅覆盖本项目需要的 A / HTTPS RR 子集）。 */
object DnsWire {
    private const val FLAG_RD = 0x0100
    private const val SVCB_KEY_ECH = 5

    fun buildQuery(name: String, type: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val header = java.io.DataOutputStream(out)
        header.writeShort(0x4321) // id
        header.writeShort(FLAG_RD)
        header.writeShort(1) // qdcount
        header.writeShort(0)
        header.writeShort(0)
        header.writeShort(0)
        name.trim().removeSuffix(".").split('.').forEach { label ->
            val bytes = label.toByteArray(Charsets.US_ASCII)
            out.write(bytes.size)
            out.write(bytes)
        }
        out.write(0)
        header.writeShort(type)
        header.writeShort(1) // IN
        return out.toByteArray()
    }

    /** 解析响应中的 answer rdata；wantType 为 null 时返回全部。 */
    fun parseAnswers(data: ByteArray, wantType: Int?): List<ByteArray> {
        if (data.size < 12) return emptyList()
        val buf = java.nio.ByteBuffer.wrap(data)
        val rcode = buf.getShort(2).toInt() and 0x000F
        if (rcode != 0) return emptyList()
        val qd = buf.getShort(4).toInt() and 0xFFFF
        val an = buf.getShort(6).toInt() and 0xFFFF
        var pos = 12
        repeat(qd) {
            pos = skipName(data, pos)
            pos += 4
        }
        val records = mutableListOf<ByteArray>()
        repeat(an) {
            if (pos >= data.size) return@repeat
            pos = skipName(data, pos)
            if (pos + 10 > data.size) return@repeat
            val rtype = ((data[pos].toInt() and 0xFF) shl 8) or (data[pos + 1].toInt() and 0xFF)
            val rdlen = ((data[pos + 8].toInt() and 0xFF) shl 8) or (data[pos + 9].toInt() and 0xFF)
            pos += 10
            if (pos + rdlen > data.size) return@repeat
            if (wantType == null || rtype == wantType) {
                records += data.copyOfRange(pos, pos + rdlen)
            }
            pos += rdlen
        }
        return records
    }

    fun parseARecord(rdata: ByteArray): String? {
        if (rdata.size != 4) return null
        return rdata.joinToString(".") { (it.toInt() and 0xFF).toString() }
    }

    /**
     * 解析 SVCB/HTTPS rdata：priority(2) + target(name) + params{key(2),len(2),value}。
     * 返回 key=5（ech）参数的原始字节 base64。
     */
    fun parseHttpsEch(rdata: ByteArray): String? {
        if (rdata.size < 4) return null
        var pos = 2 // priority
        // target name（SVCB 的 target 不做压缩，按标签逐个跳）
        while (pos < rdata.size && rdata[pos].toInt() != 0) {
            pos += 1 + (rdata[pos].toInt() and 0xFF)
        }
        pos += 1
        while (pos + 4 <= rdata.size) {
            val key = ((rdata[pos].toInt() and 0xFF) shl 8) or (rdata[pos + 1].toInt() and 0xFF)
            val vlen = ((rdata[pos + 2].toInt() and 0xFF) shl 8) or (rdata[pos + 3].toInt() and 0xFF)
            pos += 4
            if (pos + vlen > rdata.size) return null
            if (key == SVCB_KEY_ECH) {
                val ech = rdata.copyOfRange(pos, pos + vlen)
                return android.util.Base64.encodeToString(ech, android.util.Base64.NO_WRAP)
            }
            pos += vlen
        }
        return null
    }

    /** ECHConfigList 的合法性粗检：外层 2 字节长度头 + 内部 ECHConfig(version 0xFE0D)。 */
    fun isValidEchConfigList(base64: String?): Boolean {
        if (base64.isNullOrBlank()) return false
        val raw = runCatching {
            android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
        }.getOrNull() ?: return false
        if (raw.size < 6) return false
        val listLen = ((raw[0].toInt() and 0xFF) shl 8) or (raw[1].toInt() and 0xFF)
        if (listLen != raw.size - 2 || listLen <= 0) return false
        val version = ((raw[2].toInt() and 0xFF) shl 8) or (raw[3].toInt() and 0xFF)
        return version == 0xFE0D || version == 0xFE0C || version == 0xFE0B
    }

    private fun skipName(data: ByteArray, from: Int): Int {
        var pos = from
        while (pos < data.size) {
            val b = data[pos].toInt() and 0xFF
            if (b == 0) return pos + 1
            if (b and 0xC0 == 0xC0) return pos + 2 // 压缩指针
            pos += 1 + b
        }
        return pos
    }
}

/** 用户提供的可直连 DoH 端点（自动模式按此顺序尝试）。 */
object DohEndpoints {
    const val AUTO = "auto"

    data class Endpoint(val url: String, val source: String)

    val ALL: List<Endpoint> = listOf(
        Endpoint("https://ns0.fdn.fr/dns-query", "法国 FDN（French Data Network，非营利 ISP）"),
        Endpoint("https://doh.applied-privacy.net/query", "奥地利 Applied Privacy 基金会"),
        Endpoint("https://odvr.nic.cz/dns-query", "捷克 CZ.NIC（国家域名注册局）ODVR"),
        Endpoint("https://dns1.digitale-gesellschaft.ch/dns-query", "瑞士 Digitale Gesellschaft 协会"),
        Endpoint("https://dns.digitale-gesellschaft.ch/dns-query", "瑞士 Digitale Gesellschaft 协会"),
        Endpoint("https://dns.aa.net.uk/dns-query", "英国 Andrews & Arnold ISP"),
        Endpoint("https://anycast.uncensoreddns.org/dns-query", "丹麦 uncensoreddns.org（Anycast）"),
        Endpoint("https://doh.tiarap.org/dns-query", "印尼社区维护（tiarap.org）"),
        Endpoint("https://doh.cleanbrowsing.org/doh/security-filter/", "美国 CleanBrowsing（安全过滤）"),
        Endpoint("https://fdns1.dismail.de/dns-query", "德国 dismail.de 隐私服务"),
        Endpoint("https://doh.cleanbrowsing.org/doh/adult-filter/", "美国 CleanBrowsing（成人过滤）"),
        // family-filter 对 pixiv 域返回 NXDOMAIN（内容过滤），放最后兜底。
        Endpoint("https://doh.cleanbrowsing.org/doh/family-filter/", "美国 CleanBrowsing（家庭过滤，会拦截 pixiv）"),
    )

    val ALL_URLS: List<String> = ALL.map { it.url }

    fun sourceOf(url: String): String? = ALL.firstOrNull { it.url == url }?.source

    fun isValidCustom(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("https://") && trimmed.length > "https://".length
    }
}
