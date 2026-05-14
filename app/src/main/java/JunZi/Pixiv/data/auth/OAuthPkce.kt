package JunZi.Pixiv.data.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object OAuthPkce {
    private val secureRandom = SecureRandom()

    fun generateVerifier(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun challengeFor(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    fun loginUrl(verifier: String): String {
        val challenge = challengeFor(verifier)
        return "https://app-api.pixiv.net/web/v1/login" +
            "?code_challenge=$challenge&code_challenge_method=S256&client=pixiv-android"
    }
}
