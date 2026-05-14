package JunZi.Pixiv.data.network

import JunZi.Pixiv.data.model.IllustDetailResponse
import JunZi.Pixiv.data.model.IllustCommentsResponse
import JunZi.Pixiv.data.model.IllustsResponse
import JunZi.Pixiv.data.model.OAuthTokenResponse
import JunZi.Pixiv.data.model.PixivErrorResponse
import JunZi.Pixiv.data.model.TrendingTagsResponse
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadIllustResponse
import JunZi.Pixiv.data.model.UploadStatusResponse
import JunZi.Pixiv.data.model.UgoiraMetadataResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class PixivApiClient(
    private val gson: Gson = Gson(),
) {
    private val apiClient = OkHttpProvider.apiClient()
    private val directClient = OkHttpProvider.directClient()
    private val imageClient = OkHttpProvider.imageClient()
    private val cleanClient = OkHttpProvider.cleanClient()

    suspend fun exchangeCode(
        code: String,
        codeVerifier: String,
        useNetworkProxy: Boolean = true,
    ): OAuthTokenResponse = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT")
            .add("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj")
            .add("include_policy", "true")
            .add("redirect_uri", "https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback")
            .add("code", code)
            .add("code_verifier", codeVerifier)
            .build()

        val requestBuilder = Request.Builder()
            .url("https://oauth.secure.pixiv.net/auth/token")
            .post(body)
        PixivHeaders.addAppHeaders(requestBuilder, PixivHost.OAuth.rawHost)
        val request = requestBuilder.build()

        execute(request, OAuthTokenResponse::class.java, if (useNetworkProxy) currentApiClient() else directClient)
    }

    suspend fun refreshToken(refreshToken: String): OAuthTokenResponse = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT")
            .add("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj")
            .add("refresh_token", refreshToken.trim())
            .add("get_secure_url", "true")
            .build()

        val requestBuilder = Request.Builder()
            .url("https://oauth.secure.pixiv.net/auth/token")
            .post(body)
        PixivHeaders.addAppHeaders(requestBuilder, PixivHost.OAuth.rawHost)
        val request = requestBuilder.build()

        execute(request, OAuthTokenResponse::class.java)
    }

    suspend fun recommendedIllust(accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/illust/recommended".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun walkthroughIllust(accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/walkthrough/illusts".toHttpUrl()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun walkthroughIllust(): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/walkthrough/illusts".toHttpUrl()

        execute(Request.Builder().url(url).get().build(), IllustsResponse::class.java)
    }

    suspend fun rankingIllust(
        accessToken: String,
        mode: String = "day",
        date: String? = null,
    ): IllustsResponse = withContext(Dispatchers.IO) {
        val builder = "$API_BASE/v1/illust/ranking".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("mode", mode)
        date?.takeIf { it.isNotBlank() }?.let { builder.addQueryParameter("date", it) }

        execute(authorizedGet(builder.build().toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun latestIllust(accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/illust/new".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("content_type", "illust")
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun followingIllust(
        accessToken: String,
        restrict: String = "all",
    ): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v2/illust/follow".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("restrict", restrict)
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun searchIllust(
        keyword: String,
        accessToken: String,
        sort: String = "date_desc",
        searchTarget: String = "partial_match_for_tags",
        startDate: String? = null,
        endDate: String? = null,
        bookmarkNum: Int? = null,
    ): IllustsResponse = withContext(Dispatchers.IO) {
        val builder = "$API_BASE/v1/search/illust".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("sort", sort)
            .addQueryParameter("include_translated_tag_results", "true")
            .addQueryParameter("search_target", searchTarget)
            .addQueryParameter("word", keyword)
        startDate?.takeIf { it.isNotBlank() }?.let { builder.addQueryParameter("start_date", it) }
        endDate?.takeIf { it.isNotBlank() }?.let { builder.addQueryParameter("end_date", it) }
        bookmarkNum?.takeIf { it > 0 }?.let { builder.addQueryParameter("bookmark_num", it.toString()) }

        execute(authorizedGet(builder.build().toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun popularPreviewIllust(
        keyword: String,
        accessToken: String,
        searchTarget: String = "partial_match_for_tags",
    ): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/search/popular-preview/illust".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("include_translated_tag_results", "true")
            .addQueryParameter("merge_plain_keyword_results", "true")
            .addQueryParameter("search_target", searchTarget)
            .addQueryParameter("word", keyword)
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun trendingTags(accessToken: String): TrendingTagsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/trending-tags/illust".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .build()

        execute(authorizedGet(url.toString(), accessToken), TrendingTagsResponse::class.java)
    }

    suspend fun userIllusts(
        userId: Long,
        accessToken: String,
        type: String = "illust",
    ): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/user/illusts".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("user_id", userId.toString())
            .addQueryParameter("type", type)
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun bookmarkedIllusts(
        userId: Long,
        accessToken: String,
        restrict: String = "public",
    ): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/user/bookmarks/illust".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("user_id", userId.toString())
            .addQueryParameter("restrict", restrict)
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun nextPage(nextUrl: String, accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        execute(authorizedGet(nextUrl, accessToken), IllustsResponse::class.java)
    }

    suspend fun illustDetail(illustId: Long, accessToken: String): IllustDetailResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/illust/detail".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("illust_id", illustId.toString())
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustDetailResponse::class.java)
    }

    suspend fun relatedIllust(illustId: Long, accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v2/illust/related".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("illust_id", illustId.toString())
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun ugoiraMetadata(illustId: Long, accessToken: String): UgoiraMetadataResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/ugoira/metadata".toHttpUrl().newBuilder()
            .addQueryParameter("illust_id", illustId.toString())
            .build()

        execute(authorizedGet(url.toString(), accessToken), UgoiraMetadataResponse::class.java)
    }

    suspend fun illustComments(illustId: Long, accessToken: String): IllustCommentsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v3/illust/comments".toHttpUrl().newBuilder()
            .addQueryParameter("illust_id", illustId.toString())
            .addQueryParameter("include_total_comments", "true")
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustCommentsResponse::class.java)
    }

    suspend fun addIllustComment(
        illustId: Long,
        comment: String,
        accessToken: String,
    ): Unit = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("illust_id", illustId.toString())
            .add("comment", comment)
            .build()

        executeEmpty(authorizedPost("$API_BASE/v1/illust/comment/add", accessToken, body))
    }

    suspend fun addIllustBookmark(
        illustId: Long,
        accessToken: String,
        restrict: String = "public",
    ): Unit = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("illust_id", illustId.toString())
            .add("restrict", restrict)
            .build()

        val request = authorizedPost("$API_BASE/v2/illust/bookmark/add", accessToken, body)
        executeEmpty(request)
    }

    suspend fun deleteIllustBookmark(illustId: Long, accessToken: String): Unit = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("illust_id", illustId.toString())
            .build()

        val request = authorizedPost("$API_BASE/v1/illust/bookmark/delete", accessToken, body)
        executeEmpty(request)
    }

    suspend fun uploadIllust(
        accessToken: String,
        upload: UploadIllustRequest,
    ): UploadIllustResponse = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder("--boundary-${System.currentTimeMillis()}-pixiv")
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", upload.title)
            .addFormDataPart("caption", upload.caption)
            .addFormDataPart("type", upload.type)
            .addFormDataPart("visibility_scope", upload.visibilityScope.toString())
            .addFormDataPart("comment_access_control", upload.commentAccessControl.toString())
            .addFormDataPart("x_restrict", upload.xRestrict)
            .addFormDataPart("is_sexual", upload.isSexual.toString())
            .addFormDataPart("illust_ai_type", upload.illustAiType.toString())
            .addFormDataPart("is_allow_citation_work", "true")
            .apply {
                upload.tags.forEach { tag ->
                    addFormDataPart("tags[]", tag)
                }
                upload.images.forEach { image ->
                    addFormDataPart(
                        "files[]",
                        image.fileName,
                        image.bytes.toRequestBody(image.mimeType.toMediaTypeOrNull()),
                    )
                }
            }
            .build()

        val request = Request.Builder()
            .url("$API_BASE/v2/upload/illust")
            .header("Authorization", "Bearer ${accessToken.removePrefix("Bearer ").trim()}")
            .post(body)
            .build()
        execute(request, UploadIllustResponse::class.java)
    }

    suspend fun uploadStatus(
        convertKey: String,
        accessToken: String,
    ): UploadStatusResponse = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("convert_key", convertKey)
            .build()
        val request = authorizedPost("$API_BASE/v1/upload/status", accessToken, body)
        execute(request, UploadStatusResponse::class.java)
    }

    suspend fun downloadImageBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(PixivImageProxy.requireProxied(url)).build()
        currentImageClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: ${response.message}")
            response.body.bytes()
        }
    }

    suspend fun probeImage(url: String): Unit = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(PixivImageProxy.requireProxied(url))
            .header("Range", "bytes=0-0")
            .get()
            .build()
        currentImageClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw PixivApiException(response.code, "HTTP ${response.code}: ${response.message}")
            }
        }
    }

    private fun authorizedGet(url: String, accessToken: String): Request {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${accessToken.removePrefix("Bearer ").trim()}")
            .get()
            .build()
    }

    private fun authorizedPost(url: String, accessToken: String, body: FormBody): Request {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${accessToken.removePrefix("Bearer ").trim()}")
            .post(body)
            .build()
    }

    private fun <T> execute(
        request: Request,
        type: Class<T>,
        client: okhttp3.OkHttpClient = currentApiClient(),
    ): T {
        return client.newCall(request).execute().use { response ->
            response.parse(type)
        }
    }

    private fun executeEmpty(request: Request) {
        currentApiClient().newCall(request).execute().use { response ->
            response.parseEmpty()
        }
    }

    private fun currentApiClient(): okhttp3.OkHttpClient = OkHttpProvider.currentApiClient()

    private fun currentImageClient(): okhttp3.OkHttpClient {
        return if (PixivNetworkConfig.shouldUseCompatibilityClient()) imageClient else cleanClient
    }

    private fun <T> Response.parse(type: Class<T>): T {
        val payload = body.string()
        if (!isSuccessful) {
            val error = runCatching { gson.fromJson(payload, PixivErrorResponse::class.java) }.getOrNull()
            val message = error?.error?.userMessage
                ?: error?.error?.message
                ?: error?.errors?.userMessage
                ?: error?.errors?.message
                ?: message
            throw PixivApiException(code, "HTTP $code: $message", payload.take(240))
        }
        return gson.fromJson(payload, type)
    }

    private fun Response.parseEmpty() {
        val payload = body.string()
        if (!isSuccessful) {
            val error = runCatching { gson.fromJson(payload, PixivErrorResponse::class.java) }.getOrNull()
            val message = error?.error?.userMessage
                ?: error?.error?.message
                ?: error?.errors?.userMessage
                ?: error?.errors?.message
                ?: message
            throw PixivApiException(code, "HTTP $code: $message", payload.take(240))
        }
    }

    private companion object {
        private const val API_BASE = "https://app-api.pixiv.net"
        private const val FILTER_FOR_IOS = "for_ios"
    }
}
