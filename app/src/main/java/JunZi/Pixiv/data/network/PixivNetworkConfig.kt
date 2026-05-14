package JunZi.Pixiv.data.network

import java.util.concurrent.ConcurrentHashMap

object PixivNetworkConfig {
    private val hostToIps = ConcurrentHashMap<String, List<String>>().apply {
        PixivHost.entries.forEach { put(it.rawHost, it.defaultIps) }
    }

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
        PixivHost.from(normalized)?.defaultIps?.let { return it }
        return when {
            normalized.endsWith(".pximg.net") -> hostToIps[PixivHost.Image.rawHost].orEmpty()
            normalized.endsWith(".pixivision.net") -> hostToIps[PixivHost.Pixivision.rawHost].orEmpty()
            normalized.endsWith(".pixivsketch.net") -> emptyList()
            normalized.endsWith(".pixiv.org") -> emptyList()
            normalized.endsWith(".pixiv.net") -> emptyList()
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
