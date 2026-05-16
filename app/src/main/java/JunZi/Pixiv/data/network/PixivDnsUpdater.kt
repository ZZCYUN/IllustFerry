package JunZi.Pixiv.data.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

data class DnsRefreshResult(
    val updated: Map<String, String>,
    val errors: Map<String, String>,
) {
    val summary: String
        get() = buildString {
            append("更新 ${updated.size} 个 host")
            if (errors.isNotEmpty()) append("，失败 ${errors.size} 个")
        }
}

class PixivDnsUpdater(
    private val gson: Gson = Gson(),
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun refresh(): DnsRefreshResult = withContext(Dispatchers.IO) {
        val updated = linkedMapOf<String, String>()
        val errors = linkedMapOf<String, String>()

        QUERY_TARGETS.forEach { target ->
            val result = runCatching { resolve(target.queryHost) }
            val ips = result.getOrNull()
            if (!ips.isNullOrEmpty()) {
                target.apply(ips)
                target.affectedHosts.forEach { updated[it.rawHost] = ips.joinToString(",") }
            } else {
                errors[target.queryHost] = result.exceptionOrNull()?.message.orEmpty().ifBlank { "解析失败" }
            }
        }

        DnsRefreshResult(updated = updated, errors = errors)
    }

    private fun resolve(host: String): List<String> {
        val answers = request(host)
            .filter { it.isIpAddress() }
            .distinct()
        if (answers.isNotEmpty()) return answers
        throw IOException("No DNS answer")
    }

    private fun request(host: String): List<String> {
        val url = DNS_ENDPOINT.toHttpUrl().newBuilder()
            .addQueryParameter("host", host)
            .addQueryParameter("isCN", "0")
            .build()

        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            val payload = response.body.string()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${payload.take(120)}")
            }
            return gson.fromJson(payload, Array<String>::class.java)?.toList().orEmpty()
        }
    }

    private fun String.isIpAddress(): Boolean {
        return matches(IPV4_REGEX)
    }

    private data class DnsTarget(
        val queryHost: String,
        val affectedHosts: List<PixivHost>,
    ) {
        fun apply(ips: List<String>) {
            affectedHosts.forEach { PixivNetworkConfig.update(it, ips) }
        }
    }

    private companion object {
        const val DNS_ENDPOINT = "https://api.sb6.me/getdnsipv4"
        val IPV4_REGEX = Regex("""\d{1,3}(\.\d{1,3}){3}""")

        val QUERY_TARGETS = listOf(
            DnsTarget(
                PixivHost.PublicApi.rawHost,
                listOf(
                    PixivHost.PublicApi,
                    PixivHost.AppApi,
                    PixivHost.OAuth,
                    PixivHost.Accounts,
                    PixivHost.Source,
                    // www.pixiv.net 复用 public-api 的 Pixiv Tokyo 段 IP——单独查
                    // www.pixiv.net 会拿到 CloudFlare anycast，而 CF 节点必须靠 SNI
                    // 路由；LocalPixivProxy 的上游握手是故意不带 SNI 的（避开 GFW DPI），
                    // 所以这条链路只能走能在 default vhost 服 pixiv 的 Tokyo 源站。
                    PixivHost.Web,
                ),
            ),
            DnsTarget("i.pximg.net", listOf(PixivHost.Image)),
            DnsTarget("s.pximg.net", listOf(PixivHost.StaticImage)),
            DnsTarget("www.pixivision.net", listOf(PixivHost.Pixivision)),
        )
    }
}
