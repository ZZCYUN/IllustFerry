package JunZi.Pixiv.data.network

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson

/**
 * DoH 链路的持久化状态：
 *   - 用户在设置页选择的端点（auto = 按顺序自动）
 *   - 自动模式下上次成功的端点（下次直接先用它，失效再按序重试）
 *   - ECH config 与各域名解析结果（带 TTL，失效后由 [PixivDnsResolver] 重查）
 */
class DohPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /** 设置页选择：[DohEndpoints.AUTO] 或具体 URL。 */
    var selectedEndpoint: String
        get() = preferences.getString(KEY_SELECTED, DohEndpoints.AUTO) ?: DohEndpoints.AUTO
        set(value) = preferences.edit { putString(KEY_SELECTED, value) }

    /** 用户手动输入的自定义 DoH 端点（用于在列表里回显）。 */
    var customEndpoint: String?
        get() = preferences.getString(KEY_CUSTOM, null)?.takeIf { it.isNotBlank() }
        set(value) = preferences.edit {
            if (value.isNullOrBlank()) remove(KEY_CUSTOM) else putString(KEY_CUSTOM, value)
        }

    /** 自动模式下上次成功的端点；为 null 表示还没有成功记录。 */
    var workingEndpoint: String?
        get() = preferences.getString(KEY_WORKING, null)?.takeIf { it.isNotBlank() }
        set(value) = preferences.edit {
            if (value == null) remove(KEY_WORKING) else putString(KEY_WORKING, value)
        }

    /** 上次成功解析的域名 -> IP 列表。 */
    var resolvedHosts: Map<String, List<String>>
        get() {
            val raw = preferences.getString(KEY_RESOLVED, null) ?: return emptyMap()
            return runCatching {
                gson.fromJson(raw, ResolvedPayload::class.java).hosts
            }.getOrDefault(emptyMap())
        }
        set(value) {
            val payload = ResolvedPayload(hosts = value, savedAt = System.currentTimeMillis())
            preferences.edit { putString(KEY_RESOLVED, gson.toJson(payload)) }
        }

    /** 上次解析结果的保存时间。 */
    val resolvedSavedAt: Long
        get() {
            val raw = preferences.getString(KEY_RESOLVED, null) ?: return 0L
            return runCatching { gson.fromJson(raw, ResolvedPayload::class.java).savedAt }.getOrDefault(0L)
        }

    /** cloudflare-ech.com 的 ECHConfigList（base64）；null = 尚未获取或已失效清除。 */
    var echConfig: String?
        get() {
            val raw = preferences.getString(KEY_ECH, null)?.takeIf { it.isNotBlank() } ?: return null
            val savedAt = preferences.getLong(KEY_ECH_SAVED_AT, 0L)
            if (savedAt <= 0 || System.currentTimeMillis() - savedAt > ECH_TTL_MILLIS) return null
            return raw.takeIf { DnsWire.isValidEchConfigList(it) }
        }
        set(value) = preferences.edit {
            if (value == null) {
                remove(KEY_ECH)
                remove(KEY_ECH_SAVED_AT)
            } else {
                putString(KEY_ECH, value)
                putLong(KEY_ECH_SAVED_AT, System.currentTimeMillis())
            }
        }

    private data class ResolvedPayload(
        val hosts: Map<String, List<String>> = emptyMap(),
        val savedAt: Long = 0L,
    )

    private companion object {
        const val PREF_NAME = "puxiv_doh"
        const val KEY_SELECTED = "selected_endpoint"
        const val KEY_WORKING = "working_endpoint"
        const val KEY_CUSTOM = "custom_endpoint"
        const val KEY_RESOLVED = "resolved_hosts"
        const val KEY_ECH = "ech_config_b64"
        const val KEY_ECH_SAVED_AT = "ech_saved_at"
        const val ECH_TTL_MILLIS = 24L * 60 * 60 * 1000
    }
}
