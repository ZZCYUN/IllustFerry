package JunZi.Pixiv.data.network

import java.util.concurrent.ConcurrentHashMap

object PixivNetworkConfig {
    /**
     * `host -> 实时 IP 列表` 缓存。**初始为空**——只由 [PixivDnsUpdater] 写入。
     * 调用方拿到空列表时应回退到系统 DNS（OkHttp 这条路径见 [PixivDns]），
     * 而不是回到任何硬编码 IP。
     */
    private val hostToIps = ConcurrentHashMap<String, List<String>>()

    @Volatile
    var useHostIpRouting: Boolean = true

    @Volatile
    var isVpnActive: Boolean = false

    fun shouldUseCompatibilityClient(): Boolean = useHostIpRouting && !isVpnActive

    fun ipFor(host: String?): String? {
        if (!shouldUseCompatibilityClient()) return null
        return addressesFor(host).firstOrNull()
    }

    fun addressesFor(host: String?): List<String> {
        if (!shouldUseCompatibilityClient()) return emptyList()
        val normalized = host?.trim()?.lowercase()?.removeSuffix(".") ?: return emptyList()
        hostToIps[normalized]?.let { return it }
        // *.pximg.net / *.pixivision.net 这两类子域和主域共用同一组 IP，
        // 命中主域的运行时记录就直接复用；其它通配 host 在缓存里没值时
        // 留给上层走系统 DNS，不再回退到硬编码。
        return when {
            normalized.endsWith(".pximg.net") -> hostToIps[PixivHost.Image.rawHost].orEmpty()
            normalized.endsWith(".pixivision.net") -> hostToIps[PixivHost.Pixivision.rawHost].orEmpty()
            else -> emptyList()
        }
    }

    fun update(host: PixivHost, ip: String) {
        update(host, listOf(ip))
    }

    fun update(host: PixivHost, ips: List<String>) {
        val cleaned = ips.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (cleaned.isNotEmpty()) {
            hostToIps[host.rawHost] = cleaned
        }
    }

    fun snapshot(): Map<String, String> = hostToIps
        .toSortedMap()
        .mapValues { (_, ips) -> ips.joinToString(",") }
}
