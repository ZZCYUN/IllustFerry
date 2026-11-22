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
import JunZi.Pixiv.data.model.PixivCollection
import JunZi.Pixiv.data.model.PixivCollectionDetail
import JunZi.Pixiv.data.model.PixivCollectionTop
import JunZi.Pixiv.data.model.PixivisionArticle
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.SpotlightArticle
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadImagePart
import JunZi.Pixiv.data.model.UploadNovelRequest
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.model.UserPreview
import JunZi.Pixiv.data.model.UserPreviewPage
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

internal fun nonBlankStringOrNull(value: String?): String? {
    return value?.takeIf { it.isNotBlank() }
}
enum class AppScreen {
    Login,
    WebLogin,
    WebPixiv,
    Home,
    Search,
    Me,
    Settings,
    Spotlight,
    SpotlightArticle,
    CollectionDetail,
    Preview,
    Author,
    NovelReader,
    Series,
}

internal fun AppScreen.defaultBackTarget(): AppScreen {
    return when (this) {
        AppScreen.Login -> AppScreen.Login
        AppScreen.WebLogin -> AppScreen.Login
        AppScreen.WebPixiv -> AppScreen.Settings
        AppScreen.Home -> AppScreen.Home
        AppScreen.Search -> AppScreen.Home
        AppScreen.Me -> AppScreen.Home
        AppScreen.Settings -> AppScreen.Me
        AppScreen.Spotlight -> AppScreen.Home
        AppScreen.SpotlightArticle -> AppScreen.Spotlight
        AppScreen.CollectionDetail -> AppScreen.Home
        AppScreen.Preview -> AppScreen.Home
        AppScreen.Author -> AppScreen.Home
        AppScreen.NovelReader -> AppScreen.Home
        AppScreen.Series -> AppScreen.Home
    }
}

enum class HomeFeed {
    Walkthrough,
    Recommended,
    Ranking,
    Latest,
}

enum class DiscoverFeed {
    Public,
    Private,
}

enum class PreviewSwipeMode {
    Vertical,
    Horizontal,
}

/**
 * 预览页「相关作品」的排布方式。
 * - Horizontal：当前样式，作品详情下方一行横向卡片
 * - Vertical：移到评论区下方，用首页同款瀑布流铺开，滚动到底自动加载更多
 */
enum class RelatedWorksLayout {
    Horizontal,
    Vertical,
}

enum class PuxivThemeMode {
    System,
    Light,
    Dark,
}

enum class PuxivThemePalette {
    Puxiv,
    Sakura,
    Mint,
    Violet,
    Amber,
    Slate,
    Custom,
}

@Immutable
data class PuxivCustomPalette(
    val primaryHex: String = "#1A9AFC",
    val secondaryHex: String = "#5F6872",
    val tertiaryHex: String = "#FF5A7A",
    val backgroundHex: String = "#F6F6F6",
    val surfaceHex: String = "#FFFFFF",
)

enum class UgoiraSaveFormat(val extension: String, val mimeType: String) {
    GIF("gif", "image/gif"),
    WEBP("webp", "image/webp"),
}

enum class DownloadStatus {
    Queued,
    Running,
    Finished,
    Failed,
}

enum class AuthorWorkTab(val apiValue: String) {
    Illust("illust"),
    Manga("manga"),
    Novel("novel"),
    Bookmarks(""),
}

enum class FollowUserFeed {
    Public,
    Private,
}

enum class BookmarkFeed {
    Public,
    Private,
}

enum class BookmarkKind {
    Illust,
    Novel,
}

/** 历史记录保留上限预设；0 = 不限。下载记录不设上限。 */
enum class HistoryLimitOption(val value: Int, val label: String) {
    Unlimited(0, "不限"),
    L500(500, "500 条"),
    L1000(1000, "1000 条"),
    L2000(2000, "2000 条"),
    L5000(5000, "5000 条"),
}

enum class SearchKind {
    Illust,
    Novel,
    User,
}

@Immutable
data class SelectedBookmarkState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val restrict: BookmarkRestrict = BookmarkRestrict.Public,
    val tags: List<String> = emptyList(),
    val error: String? = null,
)

@Immutable
data class FeedState(
    val items: List<Illust> = emptyList(),
    val nextUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val queryTag: String? = null,
)

@Immutable
data class DiagnosticsState(
    val isRunning: Boolean = false,
    val lastDnsResult: String? = null,
    val apiStatus: String = "未检测",
    val imageStatus: String = "未检测",
    val lastError: String? = null,
    val hostSnapshot: Map<String, String> = PixivNetworkConfig.snapshot(),
)

@Immutable
data class HomeCategoryState(
    val recommended: FeedState = FeedState(),
    val ranking: FeedState = FeedState(),
    val latest: FeedState = FeedState(),
    val rankingMode: RankingMode = RankingMode.Day,
    val hasLoaded: Boolean = false,
)

/** 首页顶部 Tab：插画 / 漫画 / 小说 / 珍藏册。 */
enum class HomeTopTab(val category: HomeCategory?, val label: String) {
    Illust(HomeCategory.Illust, "插画"),
    Manga(HomeCategory.Manga, "漫画"),
    Novel(HomeCategory.Novel, "小说"),
    CollectionBooks(null, "珍藏册"),
}

/** 首页入口行的三个子页面标签。 */
internal val HomeFeed.pageLabel: String
    get() = when (this) {
        HomeFeed.Walkthrough -> "浏览"
        HomeFeed.Recommended -> "推荐"
        HomeFeed.Ranking -> "排行"
        HomeFeed.Latest -> "最新"
    }

@Immutable
data class HomeState(
    val category: HomeCategory = HomeCategory.Illust,
    val topTab: HomeTopTab = HomeTopTab.Illust,
    /** 首页入口行选中的子页面（推荐/排行/最新），原地切换不跳转。 */
    val feedPage: HomeFeed = HomeFeed.Recommended,
    val walkthrough: FeedState = FeedState(),
    val illust: HomeCategoryState = HomeCategoryState(rankingMode = RankingMode.Day),
    val manga: HomeCategoryState = HomeCategoryState(rankingMode = RankingMode.DayManga),
    val novel: HomeCategoryState = HomeCategoryState(rankingMode = RankingMode.DayNovel),
    val diagnostics: DiagnosticsState = DiagnosticsState(),
    val hasLoaded: Boolean = false,
)

/**
 * 特辑分类：app-api /v1/spotlight/articles 只提供 all/manga 且与官网分类对不上，
 * 三个分类统一解析 pixivision 官网分类页（c/illustration、c/manga、c/novel，各为最新 10 篇）。
 */
enum class SpotlightCategory(val path: String, val label: String) {
    Illustration("illustration", "插画"),
    Manga("manga", "漫画"),
    // 注意：pixivision 小说分类的 slug 是复数 novels（/zh/c/novel 是 404）。
    Novel("novels", "小说"),
}

@Immutable
data class SpotlightFeedState(
    val items: List<SpotlightArticle> = emptyList(),
    val nextUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@Immutable
data class SpotlightArticleState(
    val isLoading: Boolean = false,
    val article: PixivisionArticle? = null,
    val error: String? = null,
)

@Immutable
data class SpotlightState(
    val selectedCategory: SpotlightCategory = SpotlightCategory.Illustration,
    val feeds: Map<SpotlightCategory, SpotlightFeedState> = emptyMap(),
    /** 当前打开的特辑文章（原生渲染 PixivisionArticle 内容块）。 */
    val article: SpotlightArticle? = null,
    val articleState: SpotlightArticleState = SpotlightArticleState(),
)

@Immutable
data class CollectionDetailState(
    val collection: PixivCollection? = null,
    val detail: PixivCollectionDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

/** 珍藏册浏览页：top 聚合（推荐/大家都在看/标签组）+ search?mode=all 分页追加。 */
@Immutable
data class CollectionBooksState(
    val top: PixivCollectionTop? = null,
    val all: List<PixivCollection> = emptyList(),
    val nextOffset: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingTop: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
)

@Immutable
data class DiscoverState(
    val publicWorks: FeedState = FeedState(),
    val privateWorks: FeedState = FeedState(),
    val hasLoaded: Boolean = false,
)

@Immutable
data class UserPreviewFeedState(
    val items: List<UserPreview> = emptyList(),
    val nextUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@Immutable
data class MyState(
    val works: FeedState = FeedState(),
    val bookmarks: FeedState = FeedState(),
    val privateBookmarks: FeedState = FeedState(),
    val bookmarkNovels: FeedState = FeedState(),
    val privateBookmarkNovels: FeedState = FeedState(),
    val publicBookmarkTags: List<BookmarkTag> = emptyList(),
    val privateBookmarkTags: List<BookmarkTag> = emptyList(),
    val hasBookmarkTagsLoaded: Boolean = false,
    val isBookmarkTagsLoading: Boolean = false,
    val bookmarkTagsError: String? = null,
    val publicFollowing: UserPreviewFeedState = UserPreviewFeedState(),
    val privateFollowing: UserPreviewFeedState = UserPreviewFeedState(),
    val followCount: Int = 0,
    val followerCount: Int = 0,
    val hasMoreFollowers: Boolean = false,
    val hasLoaded: Boolean = false,
    val isUploading: Boolean = false,
    val uploadStatus: String? = null,
)

@Immutable
data class DownloadItem(
    val key: String,
    val illustId: Long,
    val title: String,
    val illust: Illust? = null,
    val fileName: String,
    val status: DownloadStatus,
    val isUgoira: Boolean = false,
    val pageCount: Int = 1,
    val relativePath: String = "",
    val detail: String = "",
    /** 当前阶段进度 0f..1f；null 表示无确定进度（显示不确定进度条）。 */
    val progress: Float? = null,
    val savedUri: String? = null,
    val savedUris: List<String> = emptyList(),
    val zipSavedUri: String? = null,
)

internal const val HISTORY_PAGE_SIZE = 20
internal const val MAX_BOOKMARK_TAGS = 10

@Immutable
data class DownloadState(
    val items: List<DownloadItem> = emptyList(),
)

@Immutable
data class CommentState(
    val items: List<IllustComment> = emptyList(),
    val nextUrl: String? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val input: String = "",
    val error: String? = null,
)

@Immutable
data class HistoryItem(
    val illust: Illust,
    val viewedAtMillis: Long,
)

@Immutable
data class HistoryState(
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val nextOffset: Int = 0,
    val hasMore: Boolean = false,
)

@Immutable
data class AuthorState(
    val userId: Long? = null,
    val userName: String = "",
    val userAccount: String = "",
    val userAvatarUrl: String? = null,
    val userComment: String = "",
    val isFollowed: Boolean = false,
    val followingCount: Int = 0,
    val followerCount: Int = 0,
    val myPixivCount: Int = 0,
    val totalIllusts: Int = 0,
    val totalManga: Int = 0,
    val totalNovels: Int = 0,
    val totalBookmarks: Int = 0,
    val selectedTab: AuthorWorkTab = AuthorWorkTab.Illust,
    val illusts: FeedState = FeedState(),
    val manga: FeedState = FeedState(),
    val novels: FeedState = FeedState(),
    val bookmarks: FeedState = FeedState(),
    val isLoadingProfile: Boolean = false,
    val isLoadingWorks: Boolean = false,
    val isFollowBusy: Boolean = false,
)

@Immutable
data class NovelReaderState(
    val isLoading: Boolean = false,
    val detail: NovelDetail? = null,
    val text: String = "",
    val uploadedImages: Map<String, String> = emptyMap(),
    val pixivImages: Map<String, String> = emptyMap(),
    val error: String? = null,
    val isBookmarkBusy: Boolean = false,
    val returnScreen: AppScreen = AppScreen.Home,
)

@Immutable
data class SeriesState(
    val seriesId: Long = 0L,
    val title: String = "",
    val items: List<Illust> = emptyList(),
    val nextUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val returnScreen: AppScreen = AppScreen.Home,
)

@Immutable
internal data class PreviewSnapshot(
    val illust: Illust?,
    val selectedImageIndex: Int,
    val selectedBookmark: SelectedBookmarkState,
    val related: FeedState,
    val comments: CommentState,
    val ugoiraFrames: List<UgoiraFrameImage>,
    val ugoiraLoadedFrames: Int,
    val ugoiraTotalFrames: Int,
    val returnScreen: AppScreen,
)

internal data class NavigationEntry(
    val screen: AppScreen,
    val preview: PreviewSnapshot? = null,
    val author: AuthorState? = null,
    val novelReader: NovelReaderState? = null,
    val series: SeriesState? = null,
)

internal data class DownloadWriteResult(
    val mainUri: Uri,
    val zipUri: Uri? = null,
    val format: UgoiraSaveFormat? = null,
    val savedUris: List<String> = emptyList(),
)

internal sealed interface SearchResult {
    data class Works(val page: IllustPage) : SearchResult
    data class Users(val page: UserPreviewPage) : SearchResult
}

@Immutable
data class ShellUiState(
    val screen: AppScreen = AppScreen.Login,
    val isBusy: Boolean = false,
    val message: String? = null,
)

@Immutable
data class AuthUiState(
    val session: AuthSession? = null,
    val accessTokenInput: String = "",
    val refreshTokenInput: String = "",
    val authCodeInput: String = "",
    val loginUrl: String = "",
)

@Immutable
data class HomeUiState(
    val home: HomeState = HomeState(),
    val rankingMode: RankingMode = RankingMode.Day,
    val rankingDate: String = "",
)

@Immutable
data class SearchUiState(
    val keyword: String = "",
    val searchKind: SearchKind = SearchKind.Illust,
    val searchSort: SearchSort = SearchSort.DateDesc,
    val searchTarget: SearchTarget = SearchTarget.Partial,
    val searchStartDate: String = "",
    val searchEndDate: String = "",
    val searchBookmarkNum: String = "",
    val trendingTags: List<TrendingTag> = emptyList(),
    val discover: DiscoverState = DiscoverState(),
    val items: List<Illust> = emptyList(),
    val searchUsers: List<UserPreview> = emptyList(),
    val nextUrl: String? = null,
    val isSearchActive: Boolean = false,
    val isTrendingLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
)

@Immutable
data class PreviewUiState(
    val previewReturnScreen: AppScreen = AppScreen.Home,
    val selectedIllust: Illust? = null,
    val selectedBookmark: SelectedBookmarkState = SelectedBookmarkState(),
    val selectedImageIndex: Int = 0,
    val related: FeedState = FeedState(),
    val comments: CommentState = CommentState(),
    val ugoiraFrames: List<UgoiraFrameImage> = emptyList(),
    val ugoiraLoadedFrames: Int = 0,
    val ugoiraTotalFrames: Int = 0,
    /** 动图加载阶段：null = 尚未开始，Download = 拉取源文件，Encode = 解帧。 */
    val ugoiraStage: JunZi.Pixiv.data.UgoiraStage? = null,
    /** 当前阶段内进度 0f..1f。 */
    val ugoiraStageProgress: Float = 0f,
    val isFullScreenPreview: Boolean = false,
    val isPreviewLoading: Boolean = false,
)

@Immutable
data class AuthorUiState(
    val author: AuthorState = AuthorState(),
)

@Immutable
data class NovelUiState(
    val novelReader: NovelReaderState = NovelReaderState(),
    val series: SeriesState = SeriesState(),
)

@Immutable
data class MineUiState(
    val mine: MyState = MyState(),
    val history: HistoryState = HistoryState(),
    val downloads: DownloadState = DownloadState(),
)

/** 预览页加载档位：从 original URL 派生不同分辨率的 pixiv 缩略图。 */
enum class PreviewQuality(val label: String) {
    Original("原图"),
    Large("大图"),
    Medium("中图"),
    Small("缩略图"),
}

@Immutable
data class SettingsUiState(
    val themeMode: PuxivThemeMode = PuxivThemeMode.System,
    val useMaterialYou: Boolean = false,
    val themePalette: PuxivThemePalette = PuxivThemePalette.Puxiv,
    val customPalette: PuxivCustomPalette = PuxivCustomPalette(),
    val useHostIpRouting: Boolean = true,
    val useRemoteImageProxy: Boolean = false,
    val imageProxyInput: String = PixivImageProxy.DEFAULT_PROXY_ORIGIN,
    val saveUgoiraZip: Boolean = false,
    val filteredTagsInput: String = "",
    /** 历史记录保留上限（条）；0 = 不限，默认 1000。仅作用于历史，下载始终无上限。 */
    val historyLimit: Int = JunZi.Pixiv.data.local.DEFAULT_HISTORY_RETENTION_LIMIT,
    val ugoiraSaveFormat: UgoiraSaveFormat = UgoiraSaveFormat.WEBP,
    val previewSwipeMode: PreviewSwipeMode = PreviewSwipeMode.Horizontal,
    val relatedWorksLayout: RelatedWorksLayout = RelatedWorksLayout.Horizontal,
    val previewQuality: PreviewQuality = PreviewQuality.Medium,
    /** DoH 端点选择：JunZi.Pixiv.data.network.DohEndpoints.AUTO 或具体 URL。 */
    val dohEndpoint: String = JunZi.Pixiv.data.network.DohEndpoints.AUTO,
)

