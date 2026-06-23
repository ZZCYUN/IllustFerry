package JunZi.Pixiv

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.PixivRepository
import JunZi.Pixiv.data.auth.OAuthPkce
import JunZi.Pixiv.data.auth.TokenStore
import JunZi.Pixiv.data.local.HistoryStore
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.AuthorProfile
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.BookmarkTag
import JunZi.Pixiv.data.model.HomeCategory
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustComment
import JunZi.Pixiv.data.model.IllustPage
import JunZi.Pixiv.data.model.NovelDetail
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadImagePart
import JunZi.Pixiv.data.model.UploadNovelRequest
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.model.UserPreview
import JunZi.Pixiv.data.model.UserPreviewPage
import JunZi.Pixiv.data.network.OkHttpProvider
import JunZi.Pixiv.data.network.PixivApiException
import JunZi.Pixiv.data.network.PixivImageProxy
import JunZi.Pixiv.data.network.PixivNetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal const val APP_API_HOST = "app-api.pixiv.net"
internal const val DNS_RETRY_DELAY_MS = 2_000L

class PixivViewModel(application: Application) : AndroidViewModel(application) {
    internal val store = TokenStore(application)
    internal val repository = PixivRepository(application)
    internal val historyStore = HistoryStore(application)
    internal val _uiState = MutableStateFlow(PuxivUiState())
    internal val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    internal val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = updateVpnState()
        override fun onLost(network: Network) = updateVpnState()
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = updateVpnState()
    }
    internal val backStack = ArrayDeque<NavigationEntry>()
    internal val previewBackStack = ArrayDeque<PreviewSnapshot>()
    internal val dnsWarmupMutex = Mutex()
    internal var dnsWarmupAttempted = false
    val uiState: StateFlow<PuxivUiState> = _uiState

    init {
        val session = store.readSession()
        val useHostIpRouting = store.readUseHostIpRouting()
        val useRemoteImageProxy = store.readUseRemoteImageProxy()
        val storedImageProxyOrigin = store.readImageProxyOrigin()
        val previewSwipeMode = store.readPreviewSwipeMode()
        val saveUgoiraZip = store.readSaveUgoiraZip()
        val filteredTagsInput = store.readFilteredTagsInput()
        val storedUgoiraFormat = store.readUgoiraSaveFormat()
        val ugoiraSaveFormat = UgoiraSaveFormat.entries.find { it.name == storedUgoiraFormat } ?: UgoiraSaveFormat.WEBP
        val themeMode = store.readThemeMode()
        val useMaterialYou = store.readUseMaterialYou()
        val themePalette = store.readThemePalette()
        val customPalette = store.readCustomThemePalette()
        val imageProxyOrigin = storedImageProxyOrigin
            ?.takeIf { PixivImageProxy.setProxyOrigin(it) }
            ?.let { PixivImageProxy.proxyOrigin }
            ?: PixivImageProxy.DEFAULT_PROXY_ORIGIN
        PixivNetworkConfig.useHostIpRouting = useHostIpRouting
        PixivNetworkConfig.isVpnActive = isVpnActive()
        PixivNetworkConfig.replaceAll(store.readDynamicHostIps())
        PixivImageProxy.useRemoteProxy = useRemoteImageProxy
        OkHttpProvider.ensureApiProxyRunning()
        _uiState.update {
            it.copy(
                session = session,
                screen = AppScreen.Home,
                accessTokenInput = session?.accessToken.orEmpty(),
                refreshTokenInput = session?.refreshToken.orEmpty(),
                downloads = DownloadState(store.readDownloads()),
                useHostIpRouting = useHostIpRouting,
                useRemoteImageProxy = useRemoteImageProxy,
                imageProxyInput = imageProxyOrigin,
                previewSwipeMode = previewSwipeMode,
                saveUgoiraZip = saveUgoiraZip,
                filteredTagsInput = filteredTagsInput,
                ugoiraSaveFormat = ugoiraSaveFormat,
                themeMode = themeMode,
                useMaterialYou = useMaterialYou,
                themePalette = themePalette,
                customPalette = customPalette,
                home = it.home.copy(
                    diagnostics = it.home.diagnostics.copy(hostSnapshot = PixivNetworkConfig.snapshot()),
                ),
            )
        }
        viewModelScope.launch {
            warmupDnsIfNeeded(forceRefresh = true)
            loadHome(refresh = false)
        }
        registerNetworkCallback()
    }
















































































































































    internal fun List<Uri>.toUploadParts(prefix: String = "image"): List<UploadImagePart> {
        val resolver = getApplication<Application>().contentResolver
        return mapIndexed { index, uri ->
            val mimeType = resolver.getType(uri)?.takeIf { it.isNotBlank() } ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { input -> input.readBytes() }
                ?: throw IllegalStateException("无法读取第 ${index + 1} 张图片")
            val extension = when (mimeType.substringAfter('/', "jpeg").substringBefore(';').lowercase()) {
                "png" -> "png"
                "webp" -> "webp"
                else -> "jpg"
            }
            UploadImagePart(
                bytes = bytes,
                mimeType = mimeType,
                fileName = "${prefix}_${index + 1}.$extension",
            )
        }
    }

    internal fun AuthSession.withFallbackUser(previous: AuthSession): AuthSession {
        return copy(
            userId = userId ?: previous.userId,
            userName = userName ?: previous.userName,
            userAccount = userAccount ?: previous.userAccount,
            userAvatarUrl = userAvatarUrl ?: previous.userAvatarUrl,
        )
    }

    internal fun AuthSession.shouldRefresh(): Boolean {
        val expiresAt = expiresAtMillis ?: return false
        return expiresAt <= System.currentTimeMillis() + 60_000L
    }

    internal fun PuxivUiState.toPreviewSnapshot(): PreviewSnapshot {
        return PreviewSnapshot(
            illust = selectedIllust,
            selectedImageIndex = selectedImageIndex,
            selectedBookmark = selectedBookmark.copy(isLoading = false),
            related = related.copy(isLoading = false),
            comments = comments.copy(isLoading = false, isSending = false),
            ugoiraFrames = ugoiraFrames,
            ugoiraLoadedFrames = ugoiraLoadedFrames,
            ugoiraTotalFrames = ugoiraTotalFrames,
            returnScreen = previewReturnScreen,
        )
    }

    internal fun PuxivUiState.toNavigationEntry(screen: AppScreen): NavigationEntry? {
        return when (screen) {
            AppScreen.Preview -> selectedIllust?.let {
                NavigationEntry(screen = screen, preview = toPreviewSnapshot())
            }
            AppScreen.Author -> author.userId?.let {
                NavigationEntry(screen = screen, author = author.navigationSnapshot())
            }
            AppScreen.Series -> series.takeIf { it.seriesId > 0L }?.let {
                NavigationEntry(screen = screen, series = it)
            }
            else -> NavigationEntry(screen = screen)
        }
    }

    internal fun Throwable.readableMessage(): String {
        if (this is PixivApiException) {
            return buildString {
                append(message)
                bodyExcerpt?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
            }
        }
        return message?.takeIf { it.isNotBlank() } ?: this::class.java.simpleName
    }

    override fun onCleared() {
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        super.onCleared()
    }



}

internal fun HomeState.categoryState(category: HomeCategory): HomeCategoryState {
    return when (category) {
        HomeCategory.Illust -> illust
        HomeCategory.Manga -> manga
        HomeCategory.Novel -> novel
    }
}

internal fun HomeState.withCategoryState(
    category: HomeCategory,
    transform: (HomeCategoryState) -> HomeCategoryState,
): HomeState {
    return when (category) {
        HomeCategory.Illust -> copy(illust = transform(illust))
        HomeCategory.Manga -> copy(manga = transform(manga))
        HomeCategory.Novel -> copy(novel = transform(novel))
    }
}

internal fun HomeState.feed(feed: HomeFeed): FeedState {
    return feed(category, feed)
}

internal fun HomeState.feed(category: HomeCategory, feed: HomeFeed): FeedState {
    if (feed == HomeFeed.Walkthrough) return walkthrough
    val cat = categoryState(category)
    return when (feed) {
        HomeFeed.Walkthrough -> walkthrough
        HomeFeed.Recommended -> cat.recommended
        HomeFeed.Ranking -> cat.ranking
        HomeFeed.Latest -> cat.latest
    }
}

internal fun HomeState.withFeed(feed: HomeFeed, transform: (FeedState) -> FeedState): HomeState {
    return withFeed(category, feed, transform)
}

internal fun HomeState.withFeed(
    category: HomeCategory,
    feed: HomeFeed,
    transform: (FeedState) -> FeedState,
): HomeState {
    if (feed == HomeFeed.Walkthrough) return copy(walkthrough = transform(walkthrough))
    return withCategoryState(category) { cat ->
        when (feed) {
            HomeFeed.Walkthrough -> cat
            HomeFeed.Recommended -> cat.copy(recommended = transform(cat.recommended))
            HomeFeed.Ranking -> cat.copy(ranking = transform(cat.ranking))
            HomeFeed.Latest -> cat.copy(latest = transform(cat.latest))
        }
    }
}

internal fun HomeCategoryState.withBookmarkState(illustId: Long, isBookmarked: Boolean): HomeCategoryState {
    val updatedRecommended = recommended.withBookmarkState(illustId, isBookmarked)
    val updatedRanking = ranking.withBookmarkState(illustId, isBookmarked)
    val updatedLatest = latest.withBookmarkState(illustId, isBookmarked)
    if (
        updatedRecommended === recommended &&
        updatedRanking === ranking &&
        updatedLatest === latest
    ) return this
    return copy(
        recommended = updatedRecommended,
        ranking = updatedRanking,
        latest = updatedLatest,
    )
}

internal fun HomeState.withBookmarkState(illustId: Long, isBookmarked: Boolean): HomeState {
    val updatedWalkthrough = walkthrough.withBookmarkState(illustId, isBookmarked)
    val updatedIllust = illust.withBookmarkState(illustId, isBookmarked)
    val updatedManga = manga.withBookmarkState(illustId, isBookmarked)
    val updatedNovel = novel.withBookmarkState(illustId, isBookmarked)
    if (
        updatedWalkthrough === walkthrough &&
        updatedIllust === illust &&
        updatedManga === manga &&
        updatedNovel === novel
    ) {
        return this
    }
    return copy(
        walkthrough = updatedWalkthrough,
        illust = updatedIllust,
        manga = updatedManga,
        novel = updatedNovel,
    )
}

internal fun DiscoverState.feed(feed: DiscoverFeed): FeedState {
    return when (feed) {
        DiscoverFeed.Public -> publicWorks
        DiscoverFeed.Private -> privateWorks
    }
}

internal fun DiscoverState.withFeed(feed: DiscoverFeed, transform: (FeedState) -> FeedState): DiscoverState {
    return when (feed) {
        DiscoverFeed.Public -> copy(publicWorks = transform(publicWorks))
        DiscoverFeed.Private -> copy(privateWorks = transform(privateWorks))
    }
}

internal fun DiscoverState.withBookmarkState(illustId: Long, isBookmarked: Boolean): DiscoverState {
    val updatedPublicWorks = publicWorks.withBookmarkState(illustId, isBookmarked)
    val updatedPrivateWorks = privateWorks.withBookmarkState(illustId, isBookmarked)
    if (updatedPublicWorks === publicWorks && updatedPrivateWorks === privateWorks) return this
    return copy(
        publicWorks = updatedPublicWorks,
        privateWorks = updatedPrivateWorks,
    )
}

internal fun MyState.bookmarks(feed: BookmarkFeed): FeedState {
    return when (feed) {
        BookmarkFeed.Public -> bookmarks
        BookmarkFeed.Private -> privateBookmarks
    }
}

internal fun MyState.withBookmarks(
    feed: BookmarkFeed,
    transform: (FeedState) -> FeedState,
): MyState {
    return when (feed) {
        BookmarkFeed.Public -> copy(bookmarks = transform(bookmarks))
        BookmarkFeed.Private -> copy(privateBookmarks = transform(privateBookmarks))
    }
}

internal fun MyState.bookmarkNovels(feed: BookmarkFeed): FeedState {
    return when (feed) {
        BookmarkFeed.Public -> bookmarkNovels
        BookmarkFeed.Private -> privateBookmarkNovels
    }
}

internal fun MyState.withBookmarkNovels(
    feed: BookmarkFeed,
    transform: (FeedState) -> FeedState,
): MyState {
    return when (feed) {
        BookmarkFeed.Public -> copy(bookmarkNovels = transform(bookmarkNovels))
        BookmarkFeed.Private -> copy(privateBookmarkNovels = transform(privateBookmarkNovels))
    }
}

internal fun MyState.following(feed: FollowUserFeed): UserPreviewFeedState {
    return when (feed) {
        FollowUserFeed.Public -> publicFollowing
        FollowUserFeed.Private -> privateFollowing
    }
}

internal fun MyState.withFollowing(
    feed: FollowUserFeed,
    transform: (UserPreviewFeedState) -> UserPreviewFeedState,
): MyState {
    return when (feed) {
        FollowUserFeed.Public -> copy(publicFollowing = transform(publicFollowing))
        FollowUserFeed.Private -> copy(privateFollowing = transform(privateFollowing))
    }
}

internal fun MyState.withBookmarkState(illustId: Long, isBookmarked: Boolean): MyState {
    val updatedBookmarks = if (isBookmarked) {
        bookmarks.withBookmarkState(illustId, isBookmarked)
    } else {
        val filtered = bookmarks.items.filterNot { it.id == illustId }
        if (filtered.size == bookmarks.items.size) bookmarks else bookmarks.copy(items = filtered)
    }
    val updatedPrivateBookmarks = if (isBookmarked) {
        privateBookmarks.withBookmarkState(illustId, isBookmarked)
    } else {
        val filtered = privateBookmarks.items.filterNot { it.id == illustId }
        if (filtered.size == privateBookmarks.items.size) privateBookmarks else privateBookmarks.copy(items = filtered)
    }
        val updatedWorks = works.withBookmarkState(illustId, isBookmarked)
        if (
            updatedWorks === works &&
            updatedBookmarks === bookmarks &&
            updatedPrivateBookmarks === privateBookmarks
        ) {
            return this
        }
    return copy(
        works = updatedWorks,
        bookmarks = updatedBookmarks,
        privateBookmarks = updatedPrivateBookmarks,
    )
}

internal fun AuthorState.withBookmarkState(illustId: Long, isBookmarked: Boolean): AuthorState {
    val updatedIllusts = illusts.withBookmarkState(illustId, isBookmarked)
    val updatedManga = manga.withBookmarkState(illustId, isBookmarked)
    val updatedNovels = novels.withBookmarkState(illustId, isBookmarked)
    val updatedBookmarks = bookmarks.withBookmarkState(illustId, isBookmarked)
    if (
        updatedIllusts === illusts &&
        updatedManga === manga &&
        updatedNovels === novels &&
        updatedBookmarks === bookmarks
    ) {
        return this
    }
    return copy(
        illusts = updatedIllusts,
        manga = updatedManga,
        novels = updatedNovels,
        bookmarks = updatedBookmarks,
    )
}

internal fun AuthorState.feed(tab: AuthorWorkTab): FeedState {
    return when (tab) {
        AuthorWorkTab.Illust -> illusts
        AuthorWorkTab.Manga -> manga
        AuthorWorkTab.Novel -> novels
        AuthorWorkTab.Bookmarks -> bookmarks
    }
}

internal fun AuthorState.withFeed(tab: AuthorWorkTab, transform: (FeedState) -> FeedState): AuthorState {
    return when (tab) {
        AuthorWorkTab.Illust -> copy(illusts = transform(illusts))
        AuthorWorkTab.Manga -> copy(manga = transform(manga))
        AuthorWorkTab.Novel -> copy(novels = transform(novels))
        AuthorWorkTab.Bookmarks -> copy(bookmarks = transform(bookmarks))
    }
}

internal fun AuthorState.mergeProfile(profile: AuthorProfile): AuthorState {
    return copy(
        userId = profile.userId,
        userName = profile.userName,
        userAccount = profile.userAccount,
        userAvatarUrl = profile.avatarUrl,
        userComment = profile.comment,
        isFollowed = profile.isFollowed,
        followingCount = profile.followingCount,
        followerCount = profile.followerCount,
        myPixivCount = profile.myPixivCount,
        totalIllusts = profile.totalIllusts,
        totalManga = profile.totalManga,
        totalNovels = profile.totalNovels,
        totalBookmarks = profile.totalBookmarks,
    )
}

internal fun AuthorState.navigationSnapshot(): AuthorState {
    return copy(
        isLoadingProfile = false,
        isLoadingWorks = false,
        isFollowBusy = false,
        illusts = illusts.copy(isLoading = false),
        manga = manga.copy(isLoading = false),
        novels = novels.copy(isLoading = false),
        bookmarks = bookmarks.copy(isLoading = false),
    )
}

internal fun PuxivUiState.rankingRequestChanged(
    category: HomeCategory,
    mode: RankingMode,
    date: String?,
): Boolean {
    val current = home.categoryState(category)
    return home.category != category || current.rankingMode != mode || rankingDate.apiDateOrNull() != date
}

internal fun String.apiDateOrNull(): String? {
    val value = trim().takeIf { it.isNotBlank() } ?: return null
    return value.takeIf { API_DATE_PATTERN.matches(it) }
}

internal fun String.apiPositiveIntOrNull(): Int? {
    return trim().toIntOrNull()?.takeIf { it > 0 }
}

internal val API_DATE_PATTERN = Regex("""\d{4}-\d{2}-\d{2}""")

internal fun FeedState.withBookmarkState(illustId: Long, isBookmarked: Boolean): FeedState {
    val updatedItems = items.withBookmarkState(illustId, isBookmarked)
    if (updatedItems === items) return this
    return copy(items = updatedItems)
}

internal fun PreviewSnapshot.withBookmarkState(illustId: Long, isBookmarked: Boolean): PreviewSnapshot {
    val updatedIllust = illust?.let {
        if (it.id == illustId) it.withBookmarkState(isBookmarked) else it
    }
    val updatedRelated = related.withBookmarkState(illustId, isBookmarked)
    if (updatedIllust === illust && updatedRelated === related) return this
    return copy(illust = updatedIllust, related = updatedRelated)
}

internal fun List<Illust>.withBookmarkState(illustId: Long, isBookmarked: Boolean): List<Illust> {
    val index = indexOfFirst { it.id == illustId }
    if (index == -1) return this
    val updated = this[index].withBookmarkState(isBookmarked)
    if (updated === this[index]) return this
    return toMutableList().also { it[index] = updated }
}

internal fun Illust.withBookmarkState(isBookmarked: Boolean): Illust {
    if (this.isBookmarked == isBookmarked) return this
    val adjustedBookmarks = when {
        isBookmarked && !this.isBookmarked -> totalBookmarks + 1
        !isBookmarked && this.isBookmarked -> (totalBookmarks - 1).coerceAtLeast(0)
        else -> totalBookmarks
    }
    return copy(isBookmarked = isBookmarked, totalBookmarks = adjustedBookmarks)
}

internal fun Illust.safeDownloadBaseName(suffix: String): String {
    val cleanTitle = title.ifBlank { "illust_$id" }
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .take(80)
        .trim('_', ' ', '.')
        .ifBlank { "illust_$id" }
    return "${id}_${cleanTitle}_$suffix"
}

internal fun Illust.safeFolderName(): String {
    return title.sanitizeFileSegment(maxLength = 80).ifBlank { "illust_$id" }
}

internal fun String.sanitizeFileSegment(maxLength: Int = 64): String {
    return trim()
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .replace(Regex("""\s+"""), " ")
        .trim('_', ' ', '.')
        .take(maxLength)
        .ifBlank { "unknown" }
}

internal fun buildDownloadRelativePath(
    rootDirectory: String,
    authorName: String,
    title: String? = null,
): String {
    val authorSegment = authorName.sanitizeFileSegment()
    val base = "$rootDirectory/IllustFerry/$authorSegment"
    val titleSegment = title?.sanitizeFileSegment()
    return if (titleSegment.isNullOrBlank()) base else "$base/$titleSegment"
}

internal fun List<Illust>.filteredBy(excludedTags: Set<String>): List<Illust> {
    if (excludedTags.isEmpty()) return this
    return filterNot { illust ->
        illust.tags.any { tag ->
            excludedTags.contains(tag.trim().trimStart('#').lowercase(Locale.ROOT))
        }
    }
}

internal fun IllustPage.filteredBy(excludedTags: Set<String>): IllustPage {
    if (excludedTags.isEmpty()) return this
    return copy(items = items.filteredBy(excludedTags))
}

internal fun UserPreviewPage.filteredBy(excludedTags: Set<String>): UserPreviewPage {
    if (excludedTags.isEmpty()) return this
    return copy(
        items = items.mapNotNull { user ->
            val filteredIllusts = user.illusts.filteredBy(excludedTags)
            if (filteredIllusts.isEmpty() && user.illusts.isNotEmpty()) null else user.copy(illusts = filteredIllusts)
        },
    )
}

internal fun normalizeFilteredTagsInput(value: String): String {
    return value.split(',')
        .map { it.trim().trimStart('#') }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }
        .joinToString(",")
}

internal fun normalizeBookmarkTag(value: String): String? {
    return value.trim()
        .trimStart('#')
        .replace(Regex("""\s+"""), " ")
        .take(40)
        .takeIf { it.isNotBlank() }
}

internal fun normalizeBookmarkTags(values: List<String>): List<String> {
    return values.mapNotNull(::normalizeBookmarkTag)
        .distinctBy { it.lowercase(Locale.ROOT) }
        .take(MAX_BOOKMARK_TAGS)
}

internal fun PuxivThemePalette.messageLabel(): String {
    return when (this) {
        PuxivThemePalette.Puxiv -> "Puxiv 蓝"
        PuxivThemePalette.Sakura -> "樱花"
        PuxivThemePalette.Mint -> "薄荷"
        PuxivThemePalette.Violet -> "紫罗兰"
        PuxivThemePalette.Amber -> "琥珀"
        PuxivThemePalette.Slate -> "青石"
        PuxivThemePalette.Custom -> "自定义"
    }
}

internal fun PuxivUiState.excludedTags(): Set<String> {
    return filteredTagsInput.split(',')
        .map { it.trim().trimStart('#').lowercase(Locale.ROOT) }
        .filter { it.isNotBlank() }
        .toSet()
}

internal fun Illust.previewImageCount(): Int = imageUrls.ifEmpty { listOfNotNull(previewUrl) }.size

internal fun String.fileExtension(): String {
    val cleanPath = substringBefore('?').substringBefore('#')
    val extension = cleanPath.substringAfterLast('.', "").lowercase(Locale.US)
    return when (extension) {
        "jpg", "jpeg" -> ".jpg"
        "png" -> ".png"
        "webp" -> ".webp"
        "gif" -> ".gif"
        else -> ".jpg"
    }
}

internal fun String.mimeType(): String {
    return when (substringAfterLast('.', "").lowercase(Locale.US)) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "zip" -> "application/zip"
        "txt" -> "text/plain"
        else -> "image/jpeg"
    }
}
