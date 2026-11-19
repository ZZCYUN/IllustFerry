package JunZi.Pixiv.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object PixivImageProxy {
    /** 不内置任何默认图片代理；代理完全由用户在设置页手动输入。 */
    const val DEFAULT_PROXY_HOST = ""
    const val DEFAULT_PROXY_ORIGIN = ""
    private const val PIXIV_IMAGE_HOST = "i.pximg.net"
    private const val PIXIV_SECURE_HOST = "s.pximg.net"

    val hasActiveProxy: Boolean
        get() = activeProxy.host.isNotBlank()

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
        // 保留路径前缀（如 https://pixiv.example.com/image），仅去掉 query / fragment。
        return parsed.newBuilder()
            .query(null)
            .fragment(null)
            .build()
            .toString()
            .trimEnd('/')
    }

    fun convert(url: String?): String? {
        val raw = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val currentProxy = activeProxy
        if (!currentProxy.host.isNotBlank() || !useRemoteProxy) return raw
        if (raw.startsWith("/")) return "${currentProxy.origin}$raw"

        val normalized = if (raw.startsWith("//")) "https:$raw" else raw
        val parsed = normalized.toHttpUrlOrNull() ?: return raw
        val host = parsed.host.lowercase()
        if (host == currentProxy.host) return parsed.toString()
        if (!host.isPixivImageHost()) return raw
        if (!parsed.shouldUseProxy()) return parsed.toString()

        return parsed.withProxyOrigin(currentProxy).toString()
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
        val originalHost = when (host) {
            PIXIV_SECURE_HOST -> PIXIV_SECURE_HOST
            else -> PIXIV_IMAGE_HOST
        }
        if (!useRemoteProxy || !currentProxy.host.isNotBlank()) {
            return listOf(url.withHost(originalHost)).distinctBy { it.toString() }
        }
        if (host == currentProxy.host) return listOf(url)
        if (!host.isPixivImageHost()) return listOf(url)
        if (!url.shouldUseProxy()) return listOf(url)

        val primary = url.withProxyOrigin(currentProxy)
        val direct = url.withHost(originalHost)
        val candidates = buildList {
            if (host != PIXIV_IMAGE_HOST && host != PIXIV_SECURE_HOST) add(url)
            add(primary)
            add(direct)
        }

        return candidates.distinctBy { it.toString() }
    }

    private fun String.isPixivImageHost(): Boolean {
        return this == "pximg.net" || endsWith(".pximg.net")
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

    /** 换到代理源：host 取 origin 的 host，路径前缀拼 origin 的 basePath。 */
    private fun HttpUrl.withProxyOrigin(proxy: ProxyConfig): HttpUrl {
        return newBuilder()
            .scheme("https")
            .host(proxy.host)
            .encodedPath((proxy.basePath + encodedPath).ifEmpty { "/" })
            .build()
    }

    private data class ProxyConfig(
        val origin: String,
        val host: String,
    ) {
        /** origin 里的路径前缀（如 "https://pixiv.example.com/image" 的 "/image"），整域反代时为空串。 */
        val basePath: String
            get() = origin.toHttpUrlOrNull()?.encodedPath?.trimEnd('/') ?: ""
    }

    private val PIXIV_DATE_PATH_REGEX = Regex(""".*/(\d{4}/\d{2}/\d{2}/).*""")
}
