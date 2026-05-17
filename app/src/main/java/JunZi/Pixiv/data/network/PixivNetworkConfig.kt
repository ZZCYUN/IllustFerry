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
        // 命中主域的运行时记录就直接复用；其它通配 host 在缓存里没值时返回空，
        // 由调用方等待动态路由就绪或对非 Pixiv 域名显式走普通网络路径。
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
            .filter { IPV4_REGEX.matches(it) }
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
                .filter { IPV4_REGEX.matches(it) }
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
            normalized.endsWith(".pixivision.net")
    }

    fun snapshot(): Map<String, String> = hostToIps
        .toSortedMap()
        .mapValues { (_, ips) -> ips.joinToString(",") }

    fun snapshotIps(): Map<String, List<String>> = hostToIps
        .toSortedMap()
        .mapValues { (_, ips) -> ips.toList() }

    private val IPV4_REGEX = Regex("""\d{1,3}(\.\d{1,3}){3}""")
}
