package JunZi.Pixiv.data.network

import java.util.concurrent.ConcurrentHashMap

object PixivNetworkConfig {
    /**
     * `host -> 实时 IP 列表` 缓存。由 [PixivDnsUpdater] 拉取，启动时可从本地持久化缓存恢复。
     * 兼容路由开启时，调用方拿到空列表必须等待刷新或直接失败，不能回退系统 DNS。
     */
    private val hostToIps = ConcurrentHashMap<String, List<String>>()

    @Volatile
    var useHostIpRouting: Boolean = true

    /**
     * cloudflare-ech.com 的 ECHConfigList（base64）。
     * 所有 Cloudflare 前置的 pixiv 域共用这一份 config；null 表示尚未获取，
     * CF 域上游会回退到无 SNI 直连（对 CF 无效，仅作为明显失败的信号暴露给上层）。
     * 由 [PixivDnsUpdater] 刷新，[JunZi.Pixiv.data.network.DohPreferences] 持久化。
     */
    @Volatile
    var echConfig: String? = null

    @Volatile
    var isVpnActive: Boolean = false

    fun shouldUseCompatibilityClient(): Boolean = true

    fun ipFor(host: String?): String? {
        if (!shouldUseCompatibilityClient()) return null
        return addressesFor(host).firstOrNull()
    }

    fun addressesFor(host: String?): List<String> {
        if (!shouldUseCompatibilityClient()) return emptyList()
        val normalized = host?.trim()?.lowercase()?.removeSuffix(".") ?: return emptyList()
        hostToIps[normalized]?.let { return it }
        // *.pximg.net / *.pixivision.net / *.pixiv.net 这三类子域和主域共用同一组 IP：
        // pximg 走 Tokyo 源站，pixivision 与 pixiv.net 子域（含 embed.pixiv.net 等
        // 未单独列出的子域）同在 Cloudflare 后面，复用 www 的 CF IP 走 ECH。
        // 其它通配 host 在缓存里没值时返回空，由调用方等待动态路由就绪或显式走普通网络路径。
        return when {
            normalized.endsWith(".pximg.net") -> hostToIps[PixivHost.Image.rawHost].orEmpty()
            normalized.endsWith(".pixivision.net") -> hostToIps[PixivHost.Pixivision.rawHost].orEmpty()
            normalized.endsWith(".pixiv.net") -> hostToIps[PixivHost.Web.rawHost].orEmpty()
            else -> emptyList()
        }
    }

    fun update(host: PixivHost, ip: String) {
        update(host, listOf(ip))
    }

    fun update(host: PixivHost, ips: List<String>) {
        val cleaned = ips.map { it.trim() }
            .filter { isValidPublicIpv4(it) }
            .distinct()
        if (cleaned.isNotEmpty()) {
            hostToIps[host.rawHost] = cleaned
        }
    }

    fun replaceAll(hostIps: Map<String, List<String>>) {
        hostToIps.clear()
        hostIps.forEach { (host, ips) ->
            val normalized = host.trim().lowercase().removeSuffix(".")
            val cleaned = ips.map { it.trim() }
                .filter { isValidPublicIpv4(it) }
                .distinct()
            if (normalized.isNotBlank() && cleaned.isNotEmpty()) {
                hostToIps[normalized] = cleaned
            }
        }
    }

    fun hasAnyAddress(): Boolean = hostToIps.isNotEmpty()

    fun hasAddressFor(host: String): Boolean = addressesFor(host).isNotEmpty()

    fun requiresDynamicRoute(host: String?): Boolean {
        val normalized = host?.trim()?.lowercase()?.removeSuffix(".") ?: return false
        return PixivHost.from(normalized) != null ||
            normalized.endsWith(".pximg.net") ||
            normalized.endsWith(".pixivision.net") ||
            normalized.endsWith(".pixiv.net")
    }

    fun snapshot(): Map<String, String> = hostToIps
        .toSortedMap()
        .mapValues { (_, ips) -> ips.joinToString(",") }

    fun snapshotIps(): Map<String, List<String>> = hostToIps
        .toSortedMap()
        .mapValues { (_, ips) -> ips.toList() }

    private val IPV4_REGEX = Regex("""\d{1,3}(\.\d{1,3}){3}""")

    /**
     * 校验 IPv4 字面量且拒绝 bogon 段。DoH 解析正常时不会返回内网地址，但内容
     * 过滤型 DoH（如 cleanbrowsing family）可能对被拦域名返回 NXDOMAIN 或 0.0.0.0
     * 这类占位应答；若原样进表，LocalPixivProxy 上游拨号就会拨到 127.0.0.1:443。
     * pixiv 的 Tokyo 源站都是公网 IP，过滤 bogon 不会误伤真实候选。
     */
    fun isValidPublicIpv4(ip: String): Boolean {
        if (!IPV4_REGEX.matches(ip)) return false
        val octets = ip.split('.')
        if (octets.size != 4 || octets.any { it.toIntOrNull() !in 0..255 }) return false
        val first = octets[0].toInt()
        val second = octets[1].toInt()
        return when {
            first == 0 -> false                       // 0.0.0.0/8
            first == 10 -> false                      // 10.0.0.0/8
            first == 127 -> false                     // 127.0.0.0/8 loopback
            first == 169 && second == 254 -> false    // 169.254.0.0/16 link-local
            first == 172 && second in 16..31 -> false // 172.16.0.0/12 private
            first == 192 && second == 168 -> false    // 192.168.0.0/16 private
            first in 224..239 -> false                // 224.0.0.0/4 multicast
            first in 240..255 -> false                // 240.0.0.0/4 reserved + 255.255.255.255
            else -> true
        }
    }
}
