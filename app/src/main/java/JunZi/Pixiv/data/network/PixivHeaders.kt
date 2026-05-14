package JunZi.Pixiv.data.network

import android.os.Build
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object PixivHeaders {
    private const val APP_VERSION = "5.0.118"
    private const val CLIENT_HASH_SALT = "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c"
    const val IMAGE_REFERER = "https://app-api.pixiv.net/"
    const val IMAGE_PROXY_REFERER = "https://www.pixiv.net/"
    const val IMAGE_USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.4 (KHTML, like Gecko) Ubuntu/12.10 Chromium/22.0.1229.94 Chrome/22.0.1229.94 Safari/537.4"

    fun appUserAgent(): String {
        return "PixivAndroidApp/$APP_VERSION (Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"
    }

    fun addAppHeaders(builder: okhttp3.Request.Builder, host: String? = null): okhttp3.Request.Builder {
        val clientTime = clientTime()
        builder
            .header("User-Agent", appUserAgent())
            .header("Accept-Language", Locale.getDefault().language.ifBlank { "en" })
            .header("App-OS-Version", Build.VERSION.RELEASE)
            .header("App-OS", "android")
            .header("App-Version", APP_VERSION)
            .header("X-Client-Time", clientTime)
            .header("X-Client-Hash", md5(clientTime + CLIENT_HASH_SALT))
        if (host != null) {
            builder.header("Host", host)
        }
        return builder
    }

    private fun clientTime(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        format.timeZone = TimeZone.getDefault()
        return format.format(Date())
    }

    private fun md5(value: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
