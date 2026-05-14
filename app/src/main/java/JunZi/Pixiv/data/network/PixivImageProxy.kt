package JunZi.Pixiv.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object PixivImageProxy {
    const val DEFAULT_PROXY_HOST = "i.yuki.sh"
    const val DEFAULT_PROXY_ORIGIN = "https://i.yuki.sh"
    private const val PIXIV_IMAGE_HOST = "i.pximg.net"
    private const val PIXIV_SECURE_HOST = "s.pximg.net"
    private val FALLBACK_PROXY_HOSTS = listOf(
        DEFAULT_PROXY_HOST,
        "i.pixiv.re",
        "i.pixiv.nl",
        "i.pixiv.cat",
    )
    @Volatile
    var useRemoteProxy: Boolean = false
    @Volatile
    private var activeProxy = ProxyConfig(DEFAULT_PROXY_ORIGIN, DEFAULT_PROXY_HOST)

    val proxyOrigin: String
        get() = activeProxy.origin

    val proxyHost: String
        get() = activeProxy.host

    fun setProxyOrigin(value: String): Boolean {
        val normalized = normalizeProxyOrigin(value) ?: return false
        activeProxy = ProxyConfig(normalized, normalized.toHttpUrlOrNull()?.host ?: DEFAULT_PROXY_HOST)
        return true
    }

    fun resetProxyOrigin() {
        activeProxy = ProxyConfig(DEFAULT_PROXY_ORIGIN, DEFAULT_PROXY_HOST)
    }

    fun normalizeProxyOrigin(value: String): String? {
        val raw = value.trim().trimEnd('/').takeIf { it.isNotBlank() } ?: return null
        val withScheme = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
        val parsed = withScheme.toHttpUrlOrNull() ?: return null
        if (parsed.host.isBlank()) return null
        return parsed.newBuilder()
            .encodedPath("/")
            .query(null)
            .fragment(null)
            .build()
            .toString()
            .trimEnd('/')
    }

    fun convert(url: String?): String? {
        val raw = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val currentProxy = activeProxy
        if (raw.startsWith("/")) return "${currentProxy.origin}$raw"

        val normalized = if (raw.startsWith("//")) "https:$raw" else raw
        val parsed = normalized.toHttpUrlOrNull() ?: return raw
        val host = parsed.host.lowercase()
        if (host == currentProxy.host) return parsed.toString()
        if (!host.isPixivImageHost()) return raw
        if (!parsed.shouldUseProxy()) return parsed.toString()
        if (!useRemoteProxy) return parsed.toString()

        return parsed.withHost(currentProxy.host).toString()
    }

    fun requireProxied(url: String): String {
        return convert(url) ?: url
    }

    fun proxiedUrl(url: HttpUrl): HttpUrl {
        return candidateUrls(url).first()
    }

    fun candidateUrls(url: HttpUrl): List<HttpUrl> {
        val host = url.host.lowercase()
        val currentProxy = activeProxy
        if (host == currentProxy.host) return listOf(url)
        if (!host.isPixivImageHost()) return listOf(url)
        if (!url.shouldUseProxy()) return listOf(url)

        val originalHost = when (host) {
            PIXIV_SECURE_HOST -> PIXIV_SECURE_HOST
            else -> PIXIV_IMAGE_HOST
        }

        if (!useRemoteProxy) {
            return listOf(url.withHost(originalHost)).distinctBy { it.toString() }
        }

        val primary = url.withHost(currentProxy.host)
        val direct = url.withHost(originalHost)
        val candidates = buildList {
            if (host != PIXIV_IMAGE_HOST && host != PIXIV_SECURE_HOST) add(url)
            add(primary)
            FALLBACK_PROXY_HOSTS.forEach { add(url.withHost(it)) }
            add(direct)
        }

        return candidates.distinctBy { it.toString() }
    }

    private fun String.isPixivImageHost(): Boolean {
        return this in FALLBACK_PROXY_HOSTS ||
            this == "pximg.net" ||
            endsWith(".pximg.net")
    }

    private fun okhttp3.HttpUrl.shouldUseProxy(): Boolean {
        val fullPath = encodedPath
        if (fullPath.contains("/common/")) return false
        return fullPath.contains("/img-original/") ||
            fullPath.contains("/img-master/") ||
            fullPath.contains("/c/") ||
            fullPath.contains("/user-profile/") ||
            PIXIV_DATE_PATH_REGEX.containsMatchIn(fullPath)
    }

    private fun HttpUrl.withHost(host: String): HttpUrl {
        return newBuilder()
            .scheme("https")
            .host(host)
            .build()
    }

    private data class ProxyConfig(
        val origin: String,
        val host: String,
    )

    private val PIXIV_DATE_PATH_REGEX = Regex(""".*/(\d{4}/\d{2}/\d{2}/).*""")
}
