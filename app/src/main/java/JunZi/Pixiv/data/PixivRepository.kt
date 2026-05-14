package JunZi.Pixiv.data

import android.graphics.BitmapFactory
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustCommentPage
import JunZi.Pixiv.data.model.IllustPage
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadStatusResponse
import JunZi.Pixiv.data.model.avatarUrl
import JunZi.Pixiv.data.model.bestOriginal
import JunZi.Pixiv.data.model.toDomain
import JunZi.Pixiv.data.network.DnsRefreshResult
import JunZi.Pixiv.data.network.PixivApiClient
import JunZi.Pixiv.data.network.PixivDnsUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

class PixivRepository(
    private val api: PixivApiClient = PixivApiClient(),
    private val dnsUpdater: PixivDnsUpdater = PixivDnsUpdater(),
) {
    suspend fun exchangeCode(code: String, verifier: String, useNetworkProxy: Boolean = true): AuthSession {
        val response = api.exchangeCode(code.trim(), verifier, useNetworkProxy)
        val token = response.response ?: response
        val accessToken = requireNotNull(token.accessToken) { "Pixiv did not return an access token" }
        return token.toSession(fallbackRefreshToken = null)
            .copy(accessToken = accessToken)
    }

    suspend fun refresh(refreshToken: String): AuthSession {
        val response = api.refreshToken(refreshToken)
        val token = response.response ?: response
        return token.toSession(fallbackRefreshToken = refreshToken)
    }

    suspend fun refreshDns(): DnsRefreshResult {
        return dnsUpdater.refresh()
    }

    suspend fun recommended(token: String): IllustPage {
        val response = api.recommendedIllust(token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun walkthrough(token: String): IllustPage {
        val response = api.walkthroughIllust(token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun walkthrough(): IllustPage {
        val response = api.walkthroughIllust()
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun ranking(token: String, mode: RankingMode = RankingMode.Day, date: String? = null): IllustPage {
        return ranking(token, mode.apiValue, date)
    }

    suspend fun ranking(token: String, mode: String, date: String? = null): IllustPage {
        val normalizedMode = mode.trim().ifBlank { RankingMode.Day.apiValue }
        val response = when (normalizedMode) {
            RankingMode.WALKTHROUGH_API_VALUE -> api.walkthroughIllust(token)
            RankingMode.RECOMMENDED_API_VALUE -> api.recommendedIllust(token)
            else -> api.rankingIllust(token, normalizedMode, date.apiDateOrNull())
        }
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun latest(token: String): IllustPage {
        val response = api.latestIllust(token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun following(token: String): IllustPage {
        val response = api.followingIllust(token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun following(token: String, restrict: BookmarkRestrict): IllustPage {
        val response = api.followingIllust(token, restrict.apiValue)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun search(
        keyword: String,
        token: String,
        sort: SearchSort = SearchSort.DateDesc,
        searchTarget: SearchTarget = SearchTarget.Partial,
        startDate: String? = null,
        endDate: String? = null,
        bookmarkNum: Int? = null,
    ): IllustPage {
        val trimmedKeyword = keyword.trim()
        val cleanStartDate = startDate.apiDateOrNull()
        val cleanEndDate = endDate.apiDateOrNull()
        val cleanBookmarkNum = bookmarkNum?.takeIf { it > 0 }
        val hasAdvancedFilters = cleanStartDate != null || cleanEndDate != null || cleanBookmarkNum != null
        val response = if (sort == SearchSort.PopularDesc && !hasAdvancedFilters) {
            api.popularPreviewIllust(trimmedKeyword, token, searchTarget.apiValue)
        } else {
            api.searchIllust(
                keyword = trimmedKeyword,
                accessToken = token,
                sort = sort.apiValue,
                searchTarget = searchTarget.apiValue,
                startDate = cleanStartDate,
                endDate = cleanEndDate,
                bookmarkNum = cleanBookmarkNum,
            )
        }
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun popularPreview(keyword: String, token: String, searchTarget: SearchTarget = SearchTarget.Partial): IllustPage {
        val response = api.popularPreviewIllust(keyword.trim(), token, searchTarget.apiValue)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun trendingTags(token: String): List<TrendingTag> {
        val response = api.trendingTags(token)
        return response.trendTags.orEmpty().mapNotNull { it.toDomain() }
    }

    suspend fun userWorks(userId: Long, token: String, type: String = "illust"): IllustPage {
        val response = api.userIllusts(userId, token, type)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun bookmarkedIllusts(
        userId: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
    ): IllustPage {
        val response = api.bookmarkedIllusts(userId, token, restrict.apiValue)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun nextPage(nextUrl: String, token: String): IllustPage {
        val response = api.nextPage(nextUrl, token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun detail(id: Long, token: String): Illust {
        return requireNotNull(api.illustDetail(id, token).illust?.toDomain()) { "Illust not found" }
    }

    suspend fun related(id: Long, token: String): IllustPage {
        val response = api.relatedIllust(id, token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun comments(id: Long, token: String): IllustCommentPage {
        val response = api.illustComments(id, token)
        return IllustCommentPage(response.comments.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun addComment(id: Long, comment: String, token: String) {
        api.addIllustComment(id, comment.trim(), token)
    }

    suspend fun addBookmark(id: Long, token: String, restrict: BookmarkRestrict = BookmarkRestrict.Public) {
        api.addIllustBookmark(id, token, restrict.apiValue)
    }

    suspend fun deleteBookmark(id: Long, token: String) {
        api.deleteIllustBookmark(id, token)
    }

    suspend fun ugoiraFrames(
        id: Long,
        token: String,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
    ): List<UgoiraFrameImage> = withContext(Dispatchers.IO) {
        val metadata = api.ugoiraMetadata(id, token).metadata ?: return@withContext emptyList()
        val zipUrl = metadata.zipUrls.bestOriginal() ?: return@withContext emptyList()
        val expectedFrames = metadata.frames.orEmpty().size.coerceAtLeast(1)
        onProgress(0, expectedFrames)
        val zipBytes = api.downloadImageBytes(zipUrl)
        val delays = metadata.frames.orEmpty().associate { (it.file.orEmpty()) to (it.delay ?: 80) }
        val frames = mutableListOf<UgoiraFrameImage>()

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val bytes = zip.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        frames += UgoiraFrameImage(
                            bitmap = bitmap,
                            delayMs = delays[entry.name] ?: 80,
                        )
                        onProgress(frames.size, expectedFrames)
                    }
                }
                zip.closeEntry()
            }
        }

        frames
    }

    suspend fun ugoiraZipBytes(id: Long, token: String): ByteArray = withContext(Dispatchers.IO) {
        val metadata = api.ugoiraMetadata(id, token).metadata ?: throw IllegalStateException("未能取得动画元数据")
        val zipUrl = metadata.zipUrls.bestOriginal() ?: throw IllegalStateException("未能取得动画下载地址")
        api.downloadImageBytes(zipUrl)
    }

    suspend fun uploadIllust(token: String, request: UploadIllustRequest): UploadStatusResponse {
        val result = api.uploadIllust(token, request)
        val convertKey = requireNotNull(result.convertKey?.takeIf { it.isNotBlank() }) {
            "Pixiv did not return an upload convert key"
        }
        return api.uploadStatus(convertKey, token)
    }

    suspend fun probeImage(url: String) {
        api.probeImage(url)
    }

    suspend fun downloadImageBytes(url: String): ByteArray {
        return api.downloadImageBytes(url)
    }

    private fun JunZi.Pixiv.data.model.OAuthTokenResponse.toSession(fallbackRefreshToken: String?): AuthSession {
        val accessToken = requireNotNull(accessToken) { "Pixiv did not return an access token" }
        val refresh = refreshToken ?: fallbackRefreshToken
        val expiresAt = expiresIn
            ?.takeIf { it > 0 }
            ?.let { System.currentTimeMillis() + (it - 60).coerceAtLeast(60) * 1000L }
        return AuthSession(
            accessToken = accessToken,
            refreshToken = refresh,
            expiresAtMillis = expiresAt,
            userId = user?.id,
            userName = user?.name,
            userAccount = user?.account,
            userAvatarUrl = user?.avatarUrl(),
        )
    }

    private fun String?.apiDateOrNull(): String? {
        val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return value.takeIf { API_DATE_PATTERN.matches(it) }
    }

    private companion object {
        private val API_DATE_PATTERN = Regex("""\d{4}-\d{2}-\d{2}""")
    }
}
