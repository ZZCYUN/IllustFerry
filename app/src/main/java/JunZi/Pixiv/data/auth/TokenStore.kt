package JunZi.Pixiv.data.auth

import android.content.Context
import JunZi.Pixiv.DownloadItem
import JunZi.Pixiv.DownloadStatus
import JunZi.Pixiv.PreviewSwipeMode
import JunZi.Pixiv.PuxivCustomPalette
import JunZi.Pixiv.PuxivThemeMode
import JunZi.Pixiv.PuxivThemePalette
import JunZi.Pixiv.data.model.AuthSession
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private fun nonBlankStringOrNull(value: String?): String? {
    return value?.takeIf { it.isNotBlank() }
}

private fun DownloadStatus?.orFailed(): DownloadStatus {
    return this ?: DownloadStatus.Failed
}

class TokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("puxiv_auth", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun readSession(): AuthSession? {
        val token = preferences.getString(KEY_ACCESS_TOKEN, null)?.takeIf { it.isNotBlank() }
        return token?.let {
            AuthSession(
                accessToken = it,
                refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)?.takeIf { value -> value.isNotBlank() },
                expiresAtMillis = preferences.getLong(KEY_EXPIRES_AT, 0L).takeIf { value -> value > 0L },
                userId = preferences.getLong(KEY_USER_ID, 0L).takeIf { value -> value > 0L },
                userName = preferences.getString(KEY_USER_NAME, null)?.takeIf { value -> value.isNotBlank() },
                userAccount = preferences.getString(KEY_USER_ACCOUNT, null)?.takeIf { value -> value.isNotBlank() },
                userAvatarUrl = preferences.getString(KEY_USER_AVATAR, null)?.takeIf { value -> value.isNotBlank() },
            )
        }
    }

    fun save(session: AuthSession) {
        preferences.edit {
            putString(KEY_ACCESS_TOKEN, session.accessToken.trimBearer())
            putString(KEY_REFRESH_TOKEN, session.refreshToken?.trimBearer().orEmpty())
            putLong(KEY_EXPIRES_AT, session.expiresAtMillis ?: 0L)
            putLong(KEY_USER_ID, session.userId ?: 0L)
            putString(KEY_USER_NAME, session.userName.orEmpty())
            putString(KEY_USER_ACCOUNT, session.userAccount.orEmpty())
            putString(KEY_USER_AVATAR, session.userAvatarUrl.orEmpty())
        }
    }

    fun clear() {
        preferences.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_CODE_VERIFIER)
            remove(KEY_EXPIRES_AT)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_ACCOUNT)
            remove(KEY_USER_AVATAR)
        }
    }

    fun saveCodeVerifier(verifier: String) {
        preferences.edit { putString(KEY_CODE_VERIFIER, verifier) }
    }

    fun readCodeVerifier(): String? {
        return preferences.getString(KEY_CODE_VERIFIER, null)?.takeIf { it.isNotBlank() }
    }

    fun readDownloads(): List<DownloadItem> {
        val json = preferences.getString(KEY_DOWNLOADS, null)?.takeIf { it.isNotBlank() } ?: return emptyList()
        return runCatching {
            val parsed: List<DownloadItem?>? = gson.fromJson(json, DOWNLOAD_LIST_TYPE)
            parsed.orEmpty().mapNotNull { it?.normalized() }
        }.getOrDefault(emptyList())
    }

    fun saveDownloads(items: List<DownloadItem>) {
        preferences.edit {
            putString(KEY_DOWNLOADS, gson.toJson(items.take(MAX_STORED_DOWNLOADS)))
        }
    }

    fun readUseRemoteImageProxy(): Boolean {
        return preferences.getBoolean(KEY_USE_REMOTE_IMAGE_PROXY, false)
    }

    fun saveUseRemoteImageProxy(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_USE_REMOTE_IMAGE_PROXY, enabled) }
    }

    fun readUseHostIpRouting(): Boolean {
        return preferences.getBoolean(KEY_USE_HOST_IP_ROUTING, true)
    }

    fun saveUseHostIpRouting(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_USE_HOST_IP_ROUTING, enabled) }
    }

    fun readDynamicHostIps(): Map<String, List<String>> {
        val json = preferences.getString(KEY_DYNAMIC_HOST_IPS, null)?.takeIf { it.isNotBlank() } ?: return emptyMap()
        return runCatching {
            val parsed: Map<String, List<String>?>? = gson.fromJson(json, HOST_IPS_TYPE)
            parsed.orEmpty()
                .mapKeys { (host, _) -> host.trim().lowercase().removeSuffix(".") }
                .mapValues { (_, ips) ->
                    ips.orEmpty()
                        .map { it.trim() }
                        .filter { IPV4_PATTERN.matches(it) }
                        .distinct()
                }
                .filter { (host, ips) -> host.isNotBlank() && ips.isNotEmpty() }
        }.getOrDefault(emptyMap())
    }

    fun saveDynamicHostIps(hostIps: Map<String, List<String>>) {
        val normalized = hostIps
            .mapKeys { (host, _) -> host.trim().lowercase().removeSuffix(".") }
            .mapValues { (_, ips) ->
                ips.map { it.trim() }
                    .filter { IPV4_PATTERN.matches(it) }
                    .distinct()
            }
            .filter { (host, ips) -> host.isNotBlank() && ips.isNotEmpty() }
        preferences.edit { putString(KEY_DYNAMIC_HOST_IPS, gson.toJson(normalized)) }
    }

    fun readImageProxyOrigin(): String? {
        return preferences.getString(KEY_IMAGE_PROXY_ORIGIN, null)?.takeIf { it.isNotBlank() }
    }

    fun saveImageProxyOrigin(origin: String) {
        preferences.edit { putString(KEY_IMAGE_PROXY_ORIGIN, origin) }
    }

    fun clearImageProxyOrigin() {
        preferences.edit { remove(KEY_IMAGE_PROXY_ORIGIN) }
    }

    fun readPreviewSwipeMode(): PreviewSwipeMode {
        val raw = preferences.getString(KEY_PREVIEW_SWIPE_MODE, null)
        return PreviewSwipeMode.entries.firstOrNull { it.name == raw } ?: PreviewSwipeMode.Horizontal
    }

    fun savePreviewSwipeMode(mode: PreviewSwipeMode) {
        preferences.edit { putString(KEY_PREVIEW_SWIPE_MODE, mode.name) }
    }

    fun readSaveUgoiraZip(): Boolean {
        return preferences.getBoolean(KEY_SAVE_UGOIRA_ZIP, false)
    }

    fun saveSaveUgoiraZip(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_SAVE_UGOIRA_ZIP, enabled) }
    }

    fun readFilteredTagsInput(): String {
        return preferences.getString(KEY_FILTERED_TAGS_INPUT, null).orEmpty()
    }

    fun saveFilteredTagsInput(value: String) {
        preferences.edit { putString(KEY_FILTERED_TAGS_INPUT, value) }
    }

    fun readUgoiraSaveFormat(): String {
        return preferences.getString(KEY_UGOIRA_SAVE_FORMAT, "WEBP") ?: "WEBP"
    }

    fun saveUgoiraSaveFormat(format: String) {
        preferences.edit { putString(KEY_UGOIRA_SAVE_FORMAT, format) }
    }

    fun readThemeMode(): PuxivThemeMode {
        val raw = preferences.getString(KEY_THEME_MODE, null)
        return PuxivThemeMode.entries.firstOrNull { it.name == raw } ?: PuxivThemeMode.System
    }

    fun saveThemeMode(mode: PuxivThemeMode) {
        preferences.edit { putString(KEY_THEME_MODE, mode.name) }
    }

    fun readUseMaterialYou(): Boolean {
        return preferences.getBoolean(KEY_USE_MATERIAL_YOU, false)
    }

    fun saveUseMaterialYou(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_USE_MATERIAL_YOU, enabled) }
    }

    fun readThemePalette(): PuxivThemePalette {
        val raw = preferences.getString(KEY_THEME_PALETTE, null)
        return PuxivThemePalette.entries.firstOrNull { it.name == raw } ?: PuxivThemePalette.Puxiv
    }

    fun saveThemePalette(palette: PuxivThemePalette) {
        preferences.edit { putString(KEY_THEME_PALETTE, palette.name) }
    }

    fun readCustomThemePalette(): PuxivCustomPalette {
        val json = preferences.getString(KEY_CUSTOM_THEME_PALETTE, null)?.takeIf { it.isNotBlank() }
            ?: return PuxivCustomPalette()
        val parsed = runCatching {
            gson.fromJson(json, PuxivCustomPalette::class.java)
        }.getOrNull()
        return parsed.normalizedCustomPalette()
    }

    fun saveCustomThemePalette(palette: PuxivCustomPalette) {
        preferences.edit { putString(KEY_CUSTOM_THEME_PALETTE, gson.toJson(palette.normalizedCustomPalette())) }
    }

    private fun String.trimBearer(): String {
        return trim().removePrefix("Bearer ").trim()
    }

    private fun DownloadItem.normalized(): DownloadItem {
        val cleanSavedUris = savedUris.orEmpty()
            .mapNotNull(::nonBlankStringOrNull)
            .ifEmpty { listOfNotNull(savedUri?.takeIf { it.isNotBlank() }) }
        return copy(
            title = title.orEmpty(),
            fileName = fileName.orEmpty(),
            status = status.orFailed(),
            pageCount = pageCount.coerceAtLeast(cleanSavedUris.size.coerceAtLeast(1)),
            relativePath = relativePath.orEmpty(),
            detail = detail.orEmpty(),
            savedUri = savedUri?.takeIf { it.isNotBlank() } ?: cleanSavedUris.firstOrNull(),
            savedUris = cleanSavedUris,
            zipSavedUri = zipSavedUri?.takeIf { it.isNotBlank() },
        )
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_CODE_VERIFIER = "code_verifier"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_ACCOUNT = "user_account"
        const val KEY_USER_AVATAR = "user_avatar"
        const val KEY_DOWNLOADS = "downloads"
        const val KEY_USE_REMOTE_IMAGE_PROXY = "use_remote_image_proxy"
        const val KEY_USE_HOST_IP_ROUTING = "use_host_ip_routing"
        const val KEY_DYNAMIC_HOST_IPS = "dynamic_host_ips"
        const val KEY_IMAGE_PROXY_ORIGIN = "image_proxy_origin"
        const val KEY_PREVIEW_SWIPE_MODE = "preview_swipe_mode"
        const val KEY_SAVE_UGOIRA_ZIP = "save_ugoira_zip"
        const val KEY_FILTERED_TAGS_INPUT = "filtered_tags_input"
        const val KEY_UGOIRA_SAVE_FORMAT = "ugoira_save_format"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_USE_MATERIAL_YOU = "use_material_you"
        const val KEY_THEME_PALETTE = "theme_palette"
        const val KEY_CUSTOM_THEME_PALETTE = "custom_theme_palette"
        const val MAX_STORED_DOWNLOADS = 200
        val DOWNLOAD_LIST_TYPE = object : TypeToken<List<DownloadItem?>>() {}.type
        val HOST_IPS_TYPE = object : TypeToken<Map<String, List<String>?>>() {}.type
        val IPV4_PATTERN = Regex("""\d{1,3}(\.\d{1,3}){3}""")
    }
}

private fun PuxivCustomPalette?.normalizedCustomPalette(): PuxivCustomPalette {
    val fallback = PuxivCustomPalette()
    return PuxivCustomPalette(
        primaryHex = this?.primaryHex.normalizedHexOr(fallback.primaryHex),
        secondaryHex = this?.secondaryHex.normalizedHexOr(fallback.secondaryHex),
        tertiaryHex = this?.tertiaryHex.normalizedHexOr(fallback.tertiaryHex),
        backgroundHex = this?.backgroundHex.normalizedHexOr(fallback.backgroundHex),
        surfaceHex = this?.surfaceHex.normalizedHexOr(fallback.surfaceHex),
    )
}

private fun String?.normalizedHexOr(fallback: String): String {
    val raw = this?.trim().orEmpty()
    val withHash = if (raw.startsWith("#")) raw else "#$raw"
    return if (HEX_COLOR_PATTERN.matches(withHash)) withHash.uppercase() else fallback
}

private val HEX_COLOR_PATTERN = Regex("""#[0-9A-Fa-f]{6}""")
