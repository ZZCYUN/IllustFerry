package JunZi.Pixiv.data.network

import JunZi.Pixiv.data.model.IllustDetailResponse
import JunZi.Pixiv.data.model.IllustBookmarkDetailResponse
import JunZi.Pixiv.data.model.IllustCommentsResponse
import JunZi.Pixiv.data.model.IllustsResponse
import JunZi.Pixiv.data.model.BookmarkTagsResponse
import JunZi.Pixiv.data.model.NovelDetailResponse
import JunZi.Pixiv.data.model.NovelTextPayload
import JunZi.Pixiv.data.model.NovelsResponse
import JunZi.Pixiv.data.model.OAuthTokenResponse
import JunZi.Pixiv.data.model.PixivErrorResponse
import JunZi.Pixiv.data.model.TrendingTagsResponse
import JunZi.Pixiv.data.model.UserDetailResponse
import JunZi.Pixiv.data.model.UserPreviewsResponse
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadIllustResponse
import JunZi.Pixiv.data.model.UploadNovelRequest
import JunZi.Pixiv.data.model.UploadNovelResponse
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
import java.util.Locale

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

    suspend fun recommendedManga(accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/manga/recommended".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun latestManga(accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/illust/new".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("content_type", "manga")
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

    suspend fun searchUser(
        keyword: String,
        accessToken: String,
    ): UserPreviewsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/search/user".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("word", keyword)
            .build()

        execute(authorizedGet(url.toString(), accessToken), UserPreviewsResponse::class.java)
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

    suspend fun recommendedNovel(accessToken: String): NovelsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/novel/recommended".toHttpUrl().newBuilder()
            .addQueryParameter("include_ranking_novels", "true")
            .build()
        execute(authorizedGet(url.toString(), accessToken), NovelsResponse::class.java)
    }

    suspend fun rankingNovel(
        accessToken: String,
        mode: String = "day",
        date: String? = null,
    ): NovelsResponse = withContext(Dispatchers.IO) {
        val normalizedMode = mode.removeSuffix("_novel").ifBlank { "day" }
        val builder = "$API_BASE/v1/novel/ranking".toHttpUrl().newBuilder()
            .addQueryParameter("mode", normalizedMode)
        date?.takeIf { it.isNotBlank() }?.let { builder.addQueryParameter("date", it) }
        execute(authorizedGet(builder.build().toString(), accessToken), NovelsResponse::class.java)
    }

    suspend fun illustSeries(seriesId: Long, accessToken: String): IllustsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/illust/series".toHttpUrl().newBuilder()
            .addQueryParameter("illust_series_id", seriesId.toString())
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .build()
        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun latestNovel(accessToken: String): NovelsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/novel/new".toHttpUrl()
        execute(authorizedGet(url.toString(), accessToken), NovelsResponse::class.java)
    }

    suspend fun followingNovel(
        accessToken: String,
        restrict: String = "all",
    ): NovelsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/novel/follow".toHttpUrl().newBuilder()
            .addQueryParameter("restrict", restrict)
            .build()
        execute(authorizedGet(url.toString(), accessToken), NovelsResponse::class.java)
    }

    suspend fun userNovels(userId: Long, accessToken: String): NovelsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/user/novels".toHttpUrl().newBuilder()
            .addQueryParameter("user_id", userId.toString())
            .build()
        execute(authorizedGet(url.toString(), accessToken), NovelsResponse::class.java)
    }

    suspend fun bookmarkedNovels(
        userId: Long,
        accessToken: String,
        restrict: String = "public",
        tag: String? = null,
    ): NovelsResponse = withContext(Dispatchers.IO) {
        val builder = "$API_BASE/v1/user/bookmarks/novel".toHttpUrl().newBuilder()
            .addQueryParameter("user_id", userId.toString())
            .addQueryParameter("restrict", restrict)
        tag?.trim()?.trimStart('#')?.takeIf { it.isNotBlank() }?.let {
            builder.addQueryParameter("tag", it)
        }
        execute(authorizedGet(builder.build().toString(), accessToken), NovelsResponse::class.java)
    }

    suspend fun searchNovel(
        keyword: String,
        accessToken: String,
        sort: String = "date_desc",
        searchTarget: String = "partial_match_for_tags",
    ): NovelsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/search/novel".toHttpUrl().newBuilder()
            .addQueryParameter("sort", sort)
            .addQueryParameter("include_translated_tag_results", "true")
            .addQueryParameter("merge_plain_keyword_results", "true")
            .addQueryParameter("search_target", searchTarget)
            .addQueryParameter("word", keyword)
            .build()
        execute(authorizedGet(url.toString(), accessToken), NovelsResponse::class.java)
    }

    suspend fun novelDetail(novelId: Long, accessToken: String): NovelDetailResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v2/novel/detail".toHttpUrl().newBuilder()
            .addQueryParameter("novel_id", novelId.toString())
            .build()
        execute(authorizedGet(url.toString(), accessToken), NovelDetailResponse::class.java)
    }

    suspend fun novelText(novelId: Long, accessToken: String): NovelTextPayload = withContext(Dispatchers.IO) {
        val url = "$API_BASE/webview/v2/novel".toHttpUrl().newBuilder()
            .addQueryParameter("id", novelId.toString())
            .addQueryParameter("viewer_version", "20221031_ai")
            .build()
        val request = authorizedGet(url.toString(), accessToken)
        val html = currentApiClient().newCall(request).execute().use { response ->
            val payload = response.body.string()
            if (!response.isSuccessful) {
                val error = runCatching { gson.fromJson(payload, PixivErrorResponse::class.java) }.getOrNull()
                val message = error?.error?.userMessage
                    ?: error?.error?.message
                    ?: error?.errors?.userMessage
                    ?: error?.errors?.message
                    ?: response.message
                throw PixivApiException(response.code, "HTTP ${response.code}: $message", payload.take(240))
            }
            payload
        }
        val match = Regex("novel:\\s*(\\{.+\\}),\\s*isOwnWork", RegexOption.DOT_MATCHES_ALL).find(html)
            ?: throw IOException("无法解析小说正文：未匹配到 novel JSON")
        val json = gson.fromJson(match.groupValues[1], WebviewNovel::class.java)
        val uploaded = json.images.orEmpty().mapNotNull { (key, value) ->
            val pick = value?.urls?.let { it.x1200 ?: it.mw480 ?: it.original ?: it.mw240 }
            if (pick.isNullOrBlank()) null else key to PixivImageProxy.convert(pick)!!
        }.toMap()
        val pixiv = json.illusts.orEmpty().mapNotNull { (key, value) ->
            val pick = value?.urls?.let { it.x1200 ?: it.mw480 ?: it.original ?: it.mw240 }
            if (pick.isNullOrBlank()) null else key to PixivImageProxy.convert(pick)!!
        }.toMap()
        NovelTextPayload(
            text = json.text.orEmpty(),
            uploadedImages = uploaded,
            pixivImages = pixiv,
        )
    }

    private data class WebviewNovel(
        @com.google.gson.annotations.SerializedName("text") val text: String? = null,
        @com.google.gson.annotations.SerializedName("images") val images: Map<String, WebviewImage?>? = null,
        @com.google.gson.annotations.SerializedName("illusts") val illusts: Map<String, WebviewImage?>? = null,
    )

    private data class WebviewImage(
        @com.google.gson.annotations.SerializedName("urls") val urls: WebviewImageUrls? = null,
    )

    private data class WebviewImageUrls(
        @com.google.gson.annotations.SerializedName("240mw") val mw240: String? = null,
        @com.google.gson.annotations.SerializedName("480mw") val mw480: String? = null,
        @com.google.gson.annotations.SerializedName("1200x1200") val x1200: String? = null,
        @com.google.gson.annotations.SerializedName("original") val original: String? = null,
    )

    suspend fun nextNovelPage(nextUrl: String, accessToken: String): NovelsResponse = withContext(Dispatchers.IO) {
        execute(authorizedGet(nextUrl, accessToken), NovelsResponse::class.java)
    }

    suspend fun addNovelBookmark(
        novelId: Long,
        accessToken: String,
        restrict: String = "public",
        tags: List<String> = emptyList(),
    ): Unit = withContext(Dispatchers.IO) {
        val bodyBuilder = FormBody.Builder()
            .add("novel_id", novelId.toString())
            .add("restrict", restrict)
        tags.asSequence()
            .map { it.trim().trimStart('#') }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .take(10)
            .joinToString(" ")
            .let { bodyBuilder.add("tags[]", it) }
        executeEmpty(authorizedPost("$API_BASE/v2/novel/bookmark/add", accessToken, bodyBuilder.build()))
    }

    suspend fun deleteNovelBookmark(novelId: Long, accessToken: String): Unit = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("novel_id", novelId.toString())
            .build()
        executeEmpty(authorizedPost("$API_BASE/v1/novel/bookmark/delete", accessToken, body))
    }

    suspend fun bookmarkedIllusts(
        userId: Long,
        accessToken: String,
        restrict: String = "public",
        tag: String? = null,
    ): IllustsResponse = withContext(Dispatchers.IO) {
        val builder = "$API_BASE/v1/user/bookmarks/illust".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("user_id", userId.toString())
            .addQueryParameter("restrict", restrict)
        tag?.trim()?.trimStart('#')?.takeIf { it.isNotBlank() }?.let {
            builder.addQueryParameter("tag", it)
        }
        val url = builder.build()

        execute(authorizedGet(url.toString(), accessToken), IllustsResponse::class.java)
    }

    suspend fun bookmarkTagsIllust(
        userId: Long,
        accessToken: String,
        restrict: String = "public",
        offset: Int? = null,
    ): BookmarkTagsResponse = withContext(Dispatchers.IO) {
        val builder = "$API_BASE/v1/user/bookmark-tags/illust".toHttpUrl().newBuilder()
            .addQueryParameter("user_id", userId.toString())
            .addQueryParameter("restrict", restrict)
        offset?.takeIf { it > 0 }?.let { builder.addQueryParameter("offset", it.toString()) }
        val url = builder.build()

        execute(authorizedGet(url.toString(), accessToken), BookmarkTagsResponse::class.java)
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

    suspend fun illustBookmarkDetail(
        illustId: Long,
        accessToken: String,
    ): IllustBookmarkDetailResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v2/illust/bookmark/detail".toHttpUrl().newBuilder()
            .addQueryParameter("illust_id", illustId.toString())
            .build()

        execute(authorizedGet(url.toString(), accessToken), IllustBookmarkDetailResponse::class.java)
    }

    suspend fun userDetail(userId: Long, accessToken: String): UserDetailResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/user/detail".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("user_id", userId.toString())
            .build()

        execute(authorizedGet(url.toString(), accessToken), UserDetailResponse::class.java)
    }

    suspend fun userFollowing(
        userId: Long,
        accessToken: String,
        restrict: String = "public",
    ): UserPreviewsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/user/following".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("user_id", userId.toString())
            .addQueryParameter("restrict", restrict)
            .build()

        execute(authorizedGet(url.toString(), accessToken), UserPreviewsResponse::class.java)
    }

    suspend fun userFollowers(userId: Long, accessToken: String): UserPreviewsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v1/user/follower".toHttpUrl().newBuilder()
            .addQueryParameter("filter", FILTER_FOR_IOS)
            .addQueryParameter("user_id", userId.toString())
            .build()

        execute(authorizedGet(url.toString(), accessToken), UserPreviewsResponse::class.java)
    }

    suspend fun nextUserPreviewsPage(nextUrl: String, accessToken: String): UserPreviewsResponse = withContext(Dispatchers.IO) {
        execute(authorizedGet(nextUrl, accessToken), UserPreviewsResponse::class.java)
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

    suspend fun novelComments(novelId: Long, accessToken: String): IllustCommentsResponse = withContext(Dispatchers.IO) {
        val url = "$API_BASE/v3/novel/comments".toHttpUrl().newBuilder()
            .addQueryParameter("novel_id", novelId.toString())
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

    suspend fun addNovelComment(
        novelId: Long,
        comment: String,
        accessToken: String,
    ): Unit = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("novel_id", novelId.toString())
            .add("comment", comment)
            .build()

        executeEmpty(authorizedPost("$API_BASE/v1/novel/comment/add", accessToken, body))
    }

    suspend fun addIllustBookmark(
        illustId: Long,
        accessToken: String,
        restrict: String = "public",
        tags: List<String> = emptyList(),
    ): Unit = withContext(Dispatchers.IO) {
        val bodyBuilder = FormBody.Builder()
            .add("illust_id", illustId.toString())
            .add("restrict", restrict)
        tags.asSequence()
            .map { it.trim().trimStart('#') }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .take(10)
            .joinToString(" ")
            .let { bodyBuilder.add("tags[]", it) }
        val body = bodyBuilder.build()

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

    suspend fun followUser(
        userId: Long,
        accessToken: String,
        restrict: String = "public",
    ): Unit = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("restrict", restrict)
            .build()

        executeEmpty(authorizedPost("$API_BASE/v1/user/follow/add", accessToken, body))
    }

    suspend fun unfollowUser(userId: Long, accessToken: String): Unit = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("user_id", userId.toString())
            .build()

        executeEmpty(authorizedPost("$API_BASE/v1/user/follow/delete", accessToken, body))
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

    suspend fun uploadNovel(
        accessToken: String,
        upload: UploadNovelRequest,
    ): UploadNovelResponse = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder("--boundary-${System.currentTimeMillis()}-pixiv")
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", upload.title)
            .addFormDataPart("caption", upload.caption)
            .addFormDataPart("text", upload.text)
            .addFormDataPart("visibility_scope", upload.visibilityScope.toString())
            .addFormDataPart("comment_access_control", upload.commentAccessControl.toString())
            .addFormDataPart("x_restrict", upload.xRestrict)
            .addFormDataPart("is_sexual", upload.isSexual.toString())
            .addFormDataPart("novel_ai_type", upload.novelAiType.toString())
            .addFormDataPart("is_original", upload.isOriginal.toString())
            .addFormDataPart("is_allow_citation_work", "true")
            .apply {
                upload.tags.forEach { tag ->
                    addFormDataPart("tags[]", tag)
                }
                upload.cover?.let { cover ->
                    addFormDataPart(
                        "cover",
                        cover.fileName,
                        cover.bytes.toRequestBody(cover.mimeType.toMediaTypeOrNull()),
                    )
                }
            }
            .build()

        val request = Request.Builder()
            .url("$API_BASE/v2/upload/novel")
            .header("Authorization", "Bearer ${accessToken.removePrefix("Bearer ").trim()}")
            .post(body)
            .build()
        execute(request, UploadNovelResponse::class.java)
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
