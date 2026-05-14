package JunZi.Pixiv.data.auth

import android.content.Context
import JunZi.Pixiv.DownloadItem
import JunZi.Pixiv.PreviewSwipeMode
import JunZi.Pixiv.data.model.AuthSession
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
            gson.fromJson<List<DownloadItem>>(json, DOWNLOAD_LIST_TYPE)
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

    private fun String.trimBearer(): String {
        return trim().removePrefix("Bearer ").trim()
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
        const val KEY_IMAGE_PROXY_ORIGIN = "image_proxy_origin"
        const val KEY_PREVIEW_SWIPE_MODE = "preview_swipe_mode"
        const val MAX_STORED_DOWNLOADS = 200
        val DOWNLOAD_LIST_TYPE = object : TypeToken<List<DownloadItem>>() {}.type
    }
}
