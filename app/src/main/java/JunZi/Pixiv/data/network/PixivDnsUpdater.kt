package JunZi.Pixiv.data.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

data class DnsRefreshResult(
    val updated: Map<String, String>,
    val errors: Map<String, String>,
) {
    val summary: String
        get() = buildString {
            append("更新 ${updated.size} 个 host")
            if (errors.isNotEmpty()) append("，失败 ${errors.size} 个")
        }

    fun requireAnyUpdated(): DnsRefreshResult {
        if (updated.isNotEmpty()) return this
        val detail = errors.entries.joinToString("; ") { (host, error) -> "$host: $error" }
        throw IOException("动态 Host IP 获取失败${detail.takeIf { it.isNotBlank() }?.let { "：$it" }.orEmpty()}")
    }
}

/**
 * DoH 驱动的动态 Host IP 获取器。
 *
 * 每个域名独立向 DoH 查询 A 记录（不再把 public-api 的 Tokyo 段复用到 CF 域），
 * 并顺带向 cloudflare-ech.com 查 HTTPS RR 取 ECHConfigList —— pixiv 的 API 域
 * 全在 Cloudflare 后面但从不发布自己的 ech 参数，ECH config 是 Cloudflare 的
 * 公共配置域，对所有 CF zone 通用。
 *
 * 端点选择策略（设置页可改）：
 *   - auto：先用上次成功的端点，失效就从头按 [DohEndpoints.ALL] 顺序重试，
 *     成功的写进持久化；下次启动直接用。
 *   - 固定端点：只查这一个。
 */
class PixivDnsUpdater(
    private val preferences: DohPreferences? = null,
) {
    private constructor(context: Context) : this(DohPreferences(context.applicationContext))

    companion object {
        private const val TAG = "PixivDns"
        private const val ECH_PUBLIC_NAME = "cloudflare-ech.com"

        /** 从 Context 构造（接入持久化）。 */
        fun withPreferences(context: Context): PixivDnsUpdater = PixivDnsUpdater(context)

        /** 全部待解析目标：CF 域与 Tokyo 域分开，ECH 只给 CF 域用。 */
        val QUERY_TARGETS: List<PixivHost> = PixivHost.entries
    }

    private val prefs: DohPreferences? = preferences

    suspend fun refresh(): DnsRefreshResult = withContext(Dispatchers.IO) {
        val probe = selectEndpointWithProbe()
            ?: throw IOException("没有可用的 DoH 端点（请检查网络或更换 DoH 设置）")
        val client = DohClient(probe.endpoint)
        val updated = linkedMapOf<String, String>()
        val errors = linkedMapOf<String, String>()

        // 探测时顺带拿到的 app-api 解析结果直接入表，避免重复查询。
        if (probe.appApiIps.isNotEmpty()) {
            val valid = probe.appApiIps.filter { PixivNetworkConfig.isValidPublicIpv4(it) }
            if (valid.isNotEmpty()) {
                PixivNetworkConfig.update(PixivHost.AppApi, valid)
                updated[PixivHost.AppApi.rawHost] = valid.joinToString(",")
            }
        }

        // 1. ECH config：一次获取，所有 CF 域共用。失败不阻断 IP 解析。
        val echB64 = runCatching { client.resolveEchConfig(ECH_PUBLIC_NAME) }
            .getOrNull()
            ?.takeIf { DnsWire.isValidEchConfigList(it) }
        if (echB64 != null) {
            PixivNetworkConfig.echConfig = echB64
            prefs?.echConfig = echB64
            Log.i(TAG, "ECH config refreshed (${echB64.length} chars)")
        } else if (PixivNetworkConfig.echConfig == null) {
            Log.w(TAG, "ECH config unavailable from ${probe.endpoint}; CF hosts fall back to no-ECH")
        }

        // 2. 其余域名逐个解析 A 记录（app-api 已由探测阶段完成）。
        QUERY_TARGETS.filter { it != PixivHost.AppApi }.forEach { host ->
            runCatching { client.resolveA(host.rawHost) }
                .onSuccess { ips ->
                    val valid = ips.filter { PixivNetworkConfig.isValidPublicIpv4(it) }
                    if (valid.isNotEmpty()) {
                        PixivNetworkConfig.update(host, valid)
                        updated[host.rawHost] = valid.joinToString(",")
                    } else {
                        errors[host.rawHost] = "DoH 返回无有效 A 记录"
                    }
                }
                .onFailure { error ->
                    errors[host.rawHost] = error.message ?: error.javaClass.simpleName
                }
        }

        if (updated.isNotEmpty() && preferences == null) {
            selectedEndpointOnce = probe.endpoint
        }
        DnsRefreshResult(updated = updated, errors = errors)
    }

    private data class EndpointProbe(
        val endpoint: String,
        val appApiIps: List<String>,
    )

    /**
     * 端点选择 + 探测。有持久化偏好时按用户设置；无偏好时按顺序探测。
     * 探测即解析 app-api.pixiv.net，返回的解析结果由 refresh() 复用。
     */
    private suspend fun selectEndpointWithProbe(): EndpointProbe? = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val pref = prefs
        if (pref == null) {
            // 未接持久化：沿用进程内记录的可用端点，否则顺序探测第一个。
            val cached = selectedEndpointOnce
            if (cached != null) {
                val ips = runCatching { DohClient(cached).resolveA(PixivHost.AppApi.rawHost) }.getOrNull().orEmpty()
                if (ips.isNotEmpty()) return@withContext EndpointProbe(cached, ips)
                selectedEndpointOnce = null
            }
            return@withContext probeFirstAvailable(DohEndpoints.ALL_URLS)
        }
        val selected = pref.selectedEndpoint
        if (selected != DohEndpoints.AUTO) {
            val ips = runCatching { DohClient(selected).resolveA(PixivHost.AppApi.rawHost) }.getOrNull().orEmpty()
            return@withContext EndpointProbe(selected, ips).takeIf { ips.isNotEmpty() }
        }

        val working = pref.workingEndpoint
        val order = buildList {
            if (working != null) add(working)
            DohEndpoints.ALL_URLS.filter { it != working }.forEach { add(it) }
        }
        val probe = probeFirstAvailable(order) ?: return@withContext null
        if (probe.endpoint != working) {
            Log.i(TAG, "DoH endpoint switched: ${working ?: "none"} -> ${probe.endpoint}")
        }
        pref.workingEndpoint = probe.endpoint
        probe
    }

    private suspend fun probeFirstAvailable(order: List<String>): EndpointProbe? {
        // 直接解析目标域本身就是可用性探测，避免重复查询。
        for (endpoint in order) {
            runCatching {
                val ips = DohClient(endpoint).resolveA(PixivHost.AppApi.rawHost)
                if (ips.isNotEmpty()) return EndpointProbe(endpoint, ips)
            }
        }
        return null
    }

    /** 无持久化时的进程内回退记录。 */
    private var selectedEndpointOnce: String? = null
}
