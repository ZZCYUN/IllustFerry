package JunZi.Pixiv.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import JunZi.Pixiv.UgoiraSaveFormat
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.AuthorProfile
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.BookmarkTag
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustBookmarkDetail
import JunZi.Pixiv.data.model.IllustCommentPage
import JunZi.Pixiv.data.model.IllustPage
import JunZi.Pixiv.data.model.NovelDetail
import JunZi.Pixiv.data.model.NovelTextPayload
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadNovelRequest
import JunZi.Pixiv.data.model.UploadNovelResponse
import JunZi.Pixiv.data.model.UploadStatusResponse
import JunZi.Pixiv.data.model.UserPreviewPage
import JunZi.Pixiv.data.model.avatarUrl
import JunZi.Pixiv.data.model.bestOriginal
import JunZi.Pixiv.data.model.toDetailDomain
import JunZi.Pixiv.data.model.toDomain
import JunZi.Pixiv.data.network.DnsRefreshResult
import JunZi.Pixiv.data.network.PixivApiClient
import JunZi.Pixiv.data.network.PixivDnsUpdater
import JunZi.Pixiv.data.network.PixivNetworkConfig
import com.aureusapps.android.webpandroid.encoder.WebPAnimEncoder
import com.aureusapps.android.webpandroid.encoder.WebPAnimEncoderOptions
import com.aureusapps.android.webpandroid.encoder.WebPConfig
import com.aureusapps.android.webpandroid.encoder.WebPMuxAnimParams
import com.aureusapps.android.webpandroid.encoder.WebPPreset
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

class PixivRepository(
    context: Context? = null,
    private val api: PixivApiClient = PixivApiClient(),
    private val dnsUpdater: PixivDnsUpdater = PixivDnsUpdater.withPreferences(
        requireNotNull(context?.applicationContext) { "PixivRepository needs a Context for DoH persistence" }
    ),
) {
    private val appContext = context?.applicationContext

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

    suspend fun ensureDynamicHostIps(): DnsRefreshResult {
        if (!PixivNetworkConfig.shouldUseCompatibilityClient() || PixivNetworkConfig.hasAnyAddress()) {
            return DnsRefreshResult(updated = emptyMap(), errors = emptyMap())
        }
        return dnsUpdater.refresh().requireAnyUpdated()
    }

    suspend fun recommended(token: String): IllustPage {
        val response = api.recommendedIllust(token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun recommendedManga(token: String): IllustPage {
        val response = api.recommendedManga(token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun latestManga(token: String): IllustPage {
        val response = api.latestManga(token)
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

    suspend fun searchNovels(
        keyword: String,
        token: String,
        sort: SearchSort = SearchSort.DateDesc,
        searchTarget: SearchTarget = SearchTarget.Partial,
    ): IllustPage {
        val response = api.searchNovel(
            keyword = keyword.trim(),
            accessToken = token,
            sort = sort.apiValue,
            searchTarget = searchTarget.apiValue,
        )
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun searchUsers(keyword: String, token: String): UserPreviewPage {
        val response = api.searchUser(keyword.trim(), token)
        return UserPreviewPage(response.userPreviews.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun nextUserPage(nextUrl: String, token: String): UserPreviewPage {
        val response = api.nextUserPreviewsPage(nextUrl, token)
        return UserPreviewPage(response.userPreviews.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun trendingTags(token: String): List<TrendingTag> {
        val response = api.trendingTags(token)
        return response.trendTags.orEmpty().mapNotNull { it.toDomain() }
    }

    suspend fun userWorks(userId: Long, token: String, type: String = "illust"): IllustPage {
        val response = api.userIllusts(userId, token, type)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun recommendedNovels(token: String): IllustPage {
        val response = api.recommendedNovel(token)
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun rankingNovels(token: String, mode: RankingMode = RankingMode.DayNovel, date: String? = null): IllustPage {
        return rankingNovels(token, mode.apiValue, date)
    }

    suspend fun rankingNovels(token: String, mode: String, date: String? = null): IllustPage {
        val normalizedMode = mode.trim().ifBlank { RankingMode.DayNovel.apiValue }
        val response = api.rankingNovel(token, normalizedMode, date.apiDateOrNull())
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun latestNovels(token: String): IllustPage {
        val response = api.latestNovel(token)
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun followingNovels(token: String, restrict: BookmarkRestrict = BookmarkRestrict.Public): IllustPage {
        val response = api.followingNovel(token, restrict.apiValue)
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun userNovels(userId: Long, token: String): IllustPage {
        val response = api.userNovels(userId, token)
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun bookmarkedNovels(
        userId: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
        tag: String? = null,
    ): IllustPage {
        val response = api.bookmarkedNovels(userId, token, restrict.apiValue, tag)
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun nextNovelPage(nextUrl: String, token: String): IllustPage {
        val response = api.nextNovelPage(nextUrl, token)
        return IllustPage(response.novels.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun novelDetail(novelId: Long, token: String): NovelDetail {
        return requireNotNull(api.novelDetail(novelId, token).novel?.toDetailDomain()) { "Novel not found" }
    }

    suspend fun novelAsIllust(novelId: Long, token: String): Illust {
        return requireNotNull(api.novelDetail(novelId, token).novel?.toDomain()) { "Novel not found" }
    }

    suspend fun illustSeries(seriesId: Long, token: String): IllustPage {
        val response = api.illustSeries(seriesId, token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun novelText(novelId: Long, token: String): NovelTextPayload {
        return api.novelText(novelId, token)
    }

    suspend fun addNovelBookmark(
        novelId: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
        tags: List<String> = emptyList(),
    ) {
        api.addNovelBookmark(novelId, token, restrict.apiValue, tags)
    }

    suspend fun deleteNovelBookmark(novelId: Long, token: String) {
        api.deleteNovelBookmark(novelId, token)
    }

    suspend fun bookmarkedIllusts(
        userId: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
        tag: String? = null,
    ): IllustPage {
        val response = api.bookmarkedIllusts(userId, token, restrict.apiValue, tag)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun bookmarkTags(
        userId: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
    ): List<BookmarkTag> {
        val response = api.bookmarkTagsIllust(userId, token, restrict.apiValue)
        return response.bookmarkTags.orEmpty().mapNotNull { it.toDomain() }
    }

    suspend fun nextPage(nextUrl: String, token: String): IllustPage {
        val response = api.nextPage(nextUrl, token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun detail(id: Long, token: String): Illust {
        return requireNotNull(api.illustDetail(id, token).illust?.toDomain()) { "Illust not found" }
    }

    suspend fun bookmarkDetail(id: Long, token: String): IllustBookmarkDetail {
        return api.illustBookmarkDetail(id, token).bookmarkDetail?.toDomain()
            ?: IllustBookmarkDetail(isBookmarked = false, restrict = BookmarkRestrict.Public, tags = emptyList())
    }

    suspend fun userDetail(userId: Long, token: String): AuthorProfile {
        return requireNotNull(api.userDetail(userId, token).toDomain()) { "User not found" }
    }

    suspend fun userFollowing(
        userId: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
    ): UserPreviewPage {
        val response = api.userFollowing(userId, token, restrict.apiValue)
        return UserPreviewPage(response.userPreviews.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun userFollowers(userId: Long, token: String): UserPreviewPage {
        val response = api.userFollowers(userId, token)
        return UserPreviewPage(response.userPreviews.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun nextUserPreviewsPage(nextUrl: String, token: String): UserPreviewPage {
        val response = api.nextUserPreviewsPage(nextUrl, token)
        return UserPreviewPage(response.userPreviews.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun related(id: Long, token: String): IllustPage {
        val response = api.relatedIllust(id, token)
        return IllustPage(response.illusts.orEmpty().map { it.toDomain() }, response.nextUrl)
    }

    suspend fun comments(id: Long, token: String): IllustCommentPage {
        val response = api.illustComments(id, token)
        return IllustCommentPage(response.comments.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun novelComments(id: Long, token: String): IllustCommentPage {
        val response = api.novelComments(id, token)
        return IllustCommentPage(response.comments.orEmpty().mapNotNull { it.toDomain() }, response.nextUrl)
    }

    suspend fun addComment(id: Long, comment: String, token: String) {
        api.addIllustComment(id, comment.trim(), token)
    }

    suspend fun addNovelComment(id: Long, comment: String, token: String) {
        api.addNovelComment(id, comment.trim(), token)
    }

    suspend fun addBookmark(
        id: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
        tags: List<String> = emptyList(),
    ) {
        api.addIllustBookmark(id, token, restrict.apiValue, tags)
    }

    suspend fun deleteBookmark(id: Long, token: String) {
        api.deleteIllustBookmark(id, token)
    }

    suspend fun followUser(
        userId: Long,
        token: String,
        restrict: BookmarkRestrict = BookmarkRestrict.Public,
    ) {
        api.followUser(userId, token, restrict.apiValue)
    }

    suspend fun unfollowUser(userId: Long, token: String) {
        api.unfollowUser(userId, token)
    }

    /**
     * 拉取并解码动图帧。整个过程分两个阶段，各自从 0 走到 1：
     * - [UgoiraStage.Download]：下载 pixiv 原始 zip，按字节数上报
     * - [UgoiraStage.Encode]：解压并逐帧解码，按帧数上报
     */
    suspend fun ugoiraFrames(
        id: Long,
        token: String,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onStage: (UgoiraStage, Float) -> Unit = { _, _ -> },
    ): List<UgoiraFrameImage> = withContext(Dispatchers.IO) {
        val metadata = api.ugoiraMetadata(id, token).metadata ?: return@withContext emptyList()
        val zipUrl = metadata.zipUrls.bestOriginal() ?: return@withContext emptyList()
        val expectedFrames = metadata.frames.orEmpty().size.coerceAtLeast(1)

        // 阶段一：下载整包 zip，按字节进度上报（此前这里没有进度回调，进度条会一直卡 0）。
        onStage(UgoiraStage.Download, 0f)
        val zipBytes = api.downloadImageBytes(zipUrl) { downloaded, total ->
            if (total > 0) {
                onStage(
                    UgoiraStage.Download,
                    (downloaded.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f),
                )
            }
        }
        onStage(UgoiraStage.Download, 1f)

        // 阶段二：解压 + 逐帧解码，按帧进度上报。
        onStage(UgoiraStage.Encode, 0f)
        onProgress(0, expectedFrames)
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
                        onStage(
                            UgoiraStage.Encode,
                            (frames.size.toDouble() / expectedFrames.toDouble())
                                .toFloat().coerceIn(0f, 1f),
                        )
                    }
                }
                zip.closeEntry()
            }
        }
        onStage(UgoiraStage.Encode, 1f)

        frames
    }

    suspend fun ugoiraZipBytes(id: Long, token: String): ByteArray = withContext(Dispatchers.IO) {
        val metadata = api.ugoiraMetadata(id, token).metadata ?: throw IllegalStateException("未能取得动画元数据")
        val zipUrl = metadata.zipUrls.bestOriginal() ?: throw IllegalStateException("未能取得动画下载地址")
        api.downloadImageBytes(zipUrl)
    }

    /**
     * 把本地已保存的动图文件（动 WebP）解码成帧序列，供预览页直接播放。
     * GIF 由 Glide 的 GifDrawable 负责，这里只处理本项目合成的动 WebP。
     */
    suspend fun decodeLocalAnimatedWebp(uri: Uri): List<UgoiraFrameImage> = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext emptyList()
        val frames = mutableListOf<UgoiraFrameImage>()
        val decoder = com.aureusapps.android.webpandroid.decoder.WebPDecoder(ctx)
        try {
            decoder.setDataSource(uri)
            val info = runCatching { decoder.decodeInfo() }.getOrNull()
            if (info?.hasAnimation != true) return@withContext emptyList()
            val count = info.frameCount.coerceAtLeast(1)
            var lastTimestamp = 0
            while (decoder.hasNextFrame()) {
                val result = runCatching { decoder.decodeNextFrame() }.getOrNull() ?: break
                val bitmap = result.frame ?: break
                // WebP 帧时间戳是累计毫秒；换算成本帧停留时长。
                val timestamp = result.timestamp
                val delayMs = (timestamp - lastTimestamp).coerceIn(16, 10_000)
                lastTimestamp = timestamp
                frames += UgoiraFrameImage(bitmap = bitmap, delayMs = delayMs)
                if (frames.size >= count) break
            }
        } catch (_: Throwable) {
            // 解码失败时返回已拿到的帧；全失败则空列表，预览页回退为静态图。
        } finally {
            runCatching { decoder.release() }
        }
        frames
    }

    /**
     * 下载并合成动图。整个流程分两个阶段，各自从 0 走到 1：
     * - [UgoiraStage.Download]：拉取 pixiv 原始 zip 包，按字节数上报进度
     * - [UgoiraStage.Encode]：逐帧解码 + 合成 WebP/GIF，按帧数上报进度
     */
    suspend fun downloadUgoira(
        id: Long,
        token: String,
        includeZip: Boolean = false,
        workingDirectory: File,
        saveFormat: UgoiraSaveFormat = UgoiraSaveFormat.WEBP,
        onProgress: (String, Int, Int) -> Unit = { _, _, _ -> },
        onStage: (UgoiraStage, Float) -> Unit = { _, _ -> },
    ): UgoiraDownloadResult = withContext(Dispatchers.IO) {
        val metadata = api.ugoiraMetadata(id, token).metadata ?: throw IllegalStateException("未能取得动画元数据")
        val zipUrl = metadata.zipUrls.bestOriginal() ?: throw IllegalStateException("未能取得动画下载地址")
        val framesMeta = metadata.frames.orEmpty()
        val expectedFrames = framesMeta.size.coerceAtLeast(1)
        val delays = framesMeta.associate { (it.file.orEmpty()) to (it.delay ?: 80) }

        // 阶段一：整包下载，按字节进度上报。
        onStage(UgoiraStage.Download, 0f)
        onProgress("下载动图源文件", 0, expectedFrames)
        val zipBytes = api.downloadImageBytes(zipUrl) { downloaded, total ->
            if (total > 0) {
                val fraction = (downloaded.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f)
                onStage(UgoiraStage.Download, fraction)
                onProgress("下载动图源文件", (downloaded / 1024).toInt(), (total / 1024).toInt())
            } else {
                onProgress("下载动图源文件", (downloaded / 1024).toInt(), 0)
            }
        }
        onStage(UgoiraStage.Download, 1f)

        // 阶段二：逐帧合成，按帧进度上报。
        val workDir = File(workingDirectory, "ugoira_${id}_${System.nanoTime()}").apply { mkdirs() }
        val encodedBytes = try {
            onStage(UgoiraStage.Encode, 0f)
            val encodeProgress: (String, Int, Int) -> Unit = { stage, current, total ->
                onProgress(stage, current, total)
                if (total > 0) {
                    onStage(UgoiraStage.Encode, (current.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f))
                }
            }
            when (saveFormat) {
                UgoiraSaveFormat.WEBP -> encodeWebPFromZip(
                    requireNotNull(appContext) { "WebP 编码需要 Android Context" },
                    zipBytes,
                    delays,
                    expectedFrames,
                    encodeProgress,
                )
                UgoiraSaveFormat.GIF -> encodeGifFromZip(zipBytes, delays, expectedFrames, encodeProgress)
            }
        } finally {
            workDir.deleteRecursively()
        }
        onStage(UgoiraStage.Encode, 1f)
        UgoiraDownloadResult(
            animatedBytes = encodedBytes,
            format = saveFormat,
            zipBytes = if (includeZip) zipBytes else null,
        )
    }

    suspend fun uploadIllust(token: String, request: UploadIllustRequest): UploadStatusResponse {
        val result = api.uploadIllust(token, request)
        val convertKey = requireNotNull(result.convertKey?.takeIf { it.isNotBlank() }) {
            "Pixiv did not return an upload convert key"
        }
        return api.uploadStatus(convertKey, token)
    }

    suspend fun uploadNovel(token: String, request: UploadNovelRequest): UploadNovelResponse {
        return api.uploadNovel(token, request)
    }

    suspend fun probeImage(url: String) {
        api.probeImage(url)
    }

    suspend fun downloadImageBytes(url: String): ByteArray {
        return api.downloadImageBytes(url)
    }

    /** 带字节进度的图片下载，用于下载列表的逐张进度展示。 */
    suspend fun downloadImageBytes(
        url: String,
        onBytes: (downloaded: Long, total: Long) -> Unit,
    ): ByteArray {
        return api.downloadImageBytes(url, onBytes)
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

data class UgoiraDownloadResult(
    val animatedBytes: ByteArray,
    val format: UgoiraSaveFormat,
    val zipBytes: ByteArray?,
)

/** 动图处理的两个阶段：先整包下载，再逐帧合成。 */
enum class UgoiraStage {
    Download,
    Encode,
}

private data class UgoiraDiskFrame(
    val file: File,
    val delayMs: Int,
)

private fun encodeWebPFromZip(
    context: Context,
    zipBytes: ByteArray,
    delays: Map<String, Int>,
    expectedFrames: Int,
    onProgress: (String, Int, Int) -> Unit,
): ByteArray {
    val outputFile = File.createTempFile("ugoira_", ".webp", context.cacheDir)
    var encoder: WebPAnimEncoder? = null
    var timestamp = 0L
    var encoded = 0

    try {
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val bytes = zip.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        val webpEncoder = encoder ?: WebPAnimEncoder(
                            context = context,
                            width = bitmap.width,
                            height = bitmap.height,
                            options = WebPAnimEncoderOptions(
                                minimizeSize = true,
                                animParams = WebPMuxAnimParams(loopCount = 0),
                            ),
                        ).configure(
                            config = WebPConfig(
                                lossless = WebPConfig.COMPRESSION_LOSSY,
                                quality = 90f,
                                method = 4,
                                threadLevel = 1,
                            ),
                            preset = WebPPreset.WEBP_PRESET_PICTURE,
                        ).also { encoder = it }

                        try {
                            webpEncoder.addFrame(timestamp, bitmap)
                            timestamp += (delays[entry.name] ?: 80).coerceAtLeast(20).toLong()
                            encoded += 1
                            onProgress("编码 WebP", encoded, expectedFrames)
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
                zip.closeEntry()
            }
        }

        val webpEncoder = encoder ?: throw IllegalStateException("未能从动画压缩包解码出有效帧")
        webpEncoder.assemble(timestamp.coerceAtLeast(1L), Uri.fromFile(outputFile))
        return outputFile.readBytes()
    } finally {
        encoder?.release()
        outputFile.delete()
    }
}

private fun encodeGifFromZip(
    zipBytes: ByteArray,
    delays: Map<String, Int>,
    expectedFrames: Int,
    onProgress: (String, Int, Int) -> Unit,
): ByteArray {
    ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
        val output = ByteArrayOutputStream()
        val encoder = AnimatedGifEncoder().apply {
            setRepeat(0)
            setQuality(5)
        }
        var started = false
        var encoded = 0
        while (true) {
            val entry = zip.nextEntry ?: break
            if (!entry.isDirectory) {
                val bytes = zip.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    try {
                        if (!started) {
                            encoder.setSize(bitmap.width, bitmap.height)
                            check(encoder.start(output)) { "GIF 编码器启动失败" }
                            started = true
                        }
                        encoder.setDelay((delays[entry.name] ?: 80).coerceAtLeast(20))
                        encoded += 1
                        onProgress("编码 GIF", encoded, expectedFrames)
                        check(encoder.addFrame(bitmap)) { "GIF 第 $encoded 帧编码失败" }
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
            zip.closeEntry()
        }
        check(started) { "未能取得动画帧数据" }
        check(encoder.finish()) { "GIF 编码收尾失败" }
        return output.toByteArray()
    }
}
