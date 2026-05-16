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
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustComment
import JunZi.Pixiv.data.model.IllustPage
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadImagePart
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private fun nonBlankStringOrNull(value: String?): String? {
    return value?.takeIf { it.isNotBlank() }
}

enum class AppScreen {
    Login,
    WebLogin,
    Home,
    Search,
    Me,
    Settings,
    Preview,
    Author,
}

private fun AppScreen.defaultBackTarget(): AppScreen {
    return when (this) {
        AppScreen.Login -> AppScreen.Login
        AppScreen.WebLogin -> AppScreen.Login
        AppScreen.Home -> AppScreen.Home
        AppScreen.Search -> AppScreen.Home
        AppScreen.Me -> AppScreen.Home
        AppScreen.Settings -> AppScreen.Me
        AppScreen.Preview -> AppScreen.Home
        AppScreen.Author -> AppScreen.Home
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
    Bookmarks(""),
}

enum class FollowUserFeed {
    Public,
    Private,
}

@Immutable
data class FeedState(
    val items: List<Illust> = emptyList(),
    val nextUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
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
data class HomeState(
    val walkthrough: FeedState = FeedState(),
    val recommended: FeedState = FeedState(),
    val ranking: FeedState = FeedState(),
    val latest: FeedState = FeedState(),
    val diagnostics: DiagnosticsState = DiagnosticsState(),
    val hasLoaded: Boolean = false,
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
    val savedUri: String? = null,
    val savedUris: List<String> = emptyList(),
    val zipSavedUri: String? = null,
)

private const val HISTORY_PAGE_SIZE = 20

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
    val totalBookmarks: Int = 0,
    val selectedTab: AuthorWorkTab = AuthorWorkTab.Illust,
    val illusts: FeedState = FeedState(),
    val manga: FeedState = FeedState(),
    val bookmarks: FeedState = FeedState(),
    val isLoadingProfile: Boolean = false,
    val isLoadingWorks: Boolean = false,
    val isFollowBusy: Boolean = false,
)

@Immutable
private data class PreviewSnapshot(
    val illust: Illust?,
    val selectedImageIndex: Int,
    val related: FeedState,
    val comments: CommentState,
    val ugoiraFrames: List<UgoiraFrameImage>,
    val ugoiraLoadedFrames: Int,
    val ugoiraTotalFrames: Int,
    val returnScreen: AppScreen,
)

private data class NavigationEntry(
    val screen: AppScreen,
    val preview: PreviewSnapshot? = null,
    val author: AuthorState? = null,
)

private data class DownloadWriteResult(
    val mainUri: Uri,
    val zipUri: Uri? = null,
    val format: UgoiraSaveFormat? = null,
    val savedUris: List<String> = emptyList(),
)

@Immutable
data class PuxivUiState(
    val screen: AppScreen = AppScreen.Login,
    val previewReturnScreen: AppScreen = AppScreen.Home,
    val session: AuthSession? = null,
    val home: HomeState = HomeState(),
    val discover: DiscoverState = DiscoverState(),
    val mine: MyState = MyState(),
    val history: HistoryState = HistoryState(),
    val author: AuthorState = AuthorState(),
    val downloads: DownloadState = DownloadState(),
    val rankingMode: RankingMode = RankingMode.Day,
    val rankingDate: String = "",
    val accessTokenInput: String = "",
    val refreshTokenInput: String = "",
    val authCodeInput: String = "",
    val loginUrl: String = "",
    val keyword: String = "",
    val searchSort: SearchSort = SearchSort.DateDesc,
    val searchTarget: SearchTarget = SearchTarget.Partial,
    val searchStartDate: String = "",
    val searchEndDate: String = "",
    val searchBookmarkNum: String = "",
    val trendingTags: List<TrendingTag> = emptyList(),
    val items: List<Illust> = emptyList(),
    val nextUrl: String? = null,
    val isSearchActive: Boolean = false,
    val selectedIllust: Illust? = null,
    val selectedImageIndex: Int = 0,
    val related: FeedState = FeedState(),
    val comments: CommentState = CommentState(),
    val ugoiraFrames: List<UgoiraFrameImage> = emptyList(),
    val ugoiraLoadedFrames: Int = 0,
    val ugoiraTotalFrames: Int = 0,
    val isFullScreenPreview: Boolean = false,
    val previewSwipeMode: PreviewSwipeMode = PreviewSwipeMode.Horizontal,
    val themeMode: PuxivThemeMode = PuxivThemeMode.System,
    val useMaterialYou: Boolean = false,
    val themePalette: PuxivThemePalette = PuxivThemePalette.Puxiv,
    val customPalette: PuxivCustomPalette = PuxivCustomPalette(),
    val useHostIpRouting: Boolean = true,
    val useRemoteImageProxy: Boolean = false,
    val imageProxyInput: String = PixivImageProxy.DEFAULT_PROXY_ORIGIN,
    val saveUgoiraZip: Boolean = false,
    val filteredTagsInput: String = "",
    val ugoiraSaveFormat: UgoiraSaveFormat = UgoiraSaveFormat.WEBP,
    val isBusy: Boolean = false,
    val isTrendingLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isPreviewLoading: Boolean = false,
    val message: String? = null,
)

class PixivViewModel(application: Application) : AndroidViewModel(application) {
    private val store = TokenStore(application)
    private val repository = PixivRepository(application)
    private val historyStore = HistoryStore(application)
    private val _uiState = MutableStateFlow(PuxivUiState())
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = updateVpnState()
        override fun onLost(network: Network) = updateVpnState()
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = updateVpnState()
    }
    private val backStack = ArrayDeque<NavigationEntry>()
    private val previewBackStack = ArrayDeque<PreviewSnapshot>()
    private var dnsWarmupAttempted = false
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
        PixivImageProxy.useRemoteProxy = useRemoteImageProxy
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
            )
        }
        viewModelScope.launch {
            if (PixivNetworkConfig.shouldUseCompatibilityClient()) refreshDns(showMessage = false)
            loadHome(refresh = false)
        }
        registerNetworkCallback()
    }

    fun updateAccessToken(value: String) = _uiState.update { it.copy(accessTokenInput = value) }

    fun updateRefreshToken(value: String) = _uiState.update { it.copy(refreshTokenInput = value) }

    fun updateAuthCode(value: String) = _uiState.update { it.copy(authCodeInput = value) }

    fun updateKeyword(value: String) = _uiState.update { it.copy(keyword = value) }

    fun updateRankingMode(mode: RankingMode) {
        if (_uiState.value.rankingMode == mode) return
        _uiState.update { state ->
            state.copy(
                rankingMode = mode,
                home = state.home.copy(ranking = FeedState()),
            )
        }
        loadHomeFeed(HomeFeed.Ranking, refresh = true)
    }

    fun updateRankingDate(value: String) = _uiState.update { it.copy(rankingDate = value.trim()) }

    fun applyRankingFilters() {
        _uiState.update { state ->
            state.copy(home = state.home.copy(ranking = FeedState()))
        }
        loadHomeFeed(HomeFeed.Ranking, refresh = true)
    }

    fun clearRankingDate() {
        if (_uiState.value.rankingDate.isBlank()) return
        _uiState.update { it.copy(rankingDate = "") }
        applyRankingFilters()
    }

    fun updateSearchSort(sort: SearchSort) {
        if (_uiState.value.searchSort == sort) return
        val shouldRefresh = shouldRefreshSearchAfterParamChange()
        _uiState.update { it.copy(searchSort = sort) }
        if (shouldRefresh) search()
    }

    fun updateSearchTarget(target: SearchTarget) {
        if (_uiState.value.searchTarget == target) return
        val shouldRefresh = shouldRefreshSearchAfterParamChange()
        _uiState.update { it.copy(searchTarget = target) }
        if (shouldRefresh) search()
    }

    fun updateSearchStartDate(value: String) = _uiState.update { it.copy(searchStartDate = value.trim()) }

    fun updateSearchEndDate(value: String) = _uiState.update { it.copy(searchEndDate = value.trim()) }

    fun updateSearchBookmarkNum(value: String) {
        _uiState.update { it.copy(searchBookmarkNum = value.filter(Char::isDigit)) }
    }

    fun applySearchFilters() {
        search()
    }

    fun clearSearchFilters() {
        val shouldRefresh = shouldRefreshSearchAfterParamChange()
        _uiState.update {
            it.copy(
                searchStartDate = "",
                searchEndDate = "",
                searchBookmarkNum = "",
            )
        }
        if (shouldRefresh) search()
    }

    fun openHome() {
        navigateTo(AppScreen.Home, addToBackStack = false)
        loadHome(refresh = false)
    }

    fun openSearch() {
        navigateTo(AppScreen.Search, addToBackStack = false)
        loadTrendingTags(refresh = false)
        loadDiscover(refresh = false)
    }

    fun openMe() {
        navigateTo(AppScreen.Me, addToBackStack = false)
        loadMine(refresh = false)
    }

    fun openSettings() {
        navigateTo(AppScreen.Settings)
    }

    fun openLoginScreen() {
        navigateTo(AppScreen.Login)
    }

    fun showMainPage(screen: AppScreen) {
        if (screen !in setOf(AppScreen.Home, AppScreen.Search, AppScreen.Me)) return
        navigateTo(screen, addToBackStack = false)
    }

    fun ensureMainPageLoaded(screen: AppScreen) {
        if (_uiState.value.session == null && screen !in setOf(AppScreen.Home, AppScreen.Me)) return
        when (screen) {
            AppScreen.Home -> loadHome(refresh = false)
            AppScreen.Search -> {
                loadTrendingTags(refresh = false)
                loadDiscover(refresh = false)
            }
            AppScreen.Me -> loadMine(refresh = false)
            else -> Unit
        }
    }

    fun updatePreviewSwipeMode(mode: PreviewSwipeMode) {
        store.savePreviewSwipeMode(mode)
        _uiState.update { it.copy(previewSwipeMode = mode) }
    }

    fun updateThemeMode(mode: PuxivThemeMode) {
        store.saveThemeMode(mode)
        _uiState.update {
            it.copy(
                themeMode = mode,
                message = when (mode) {
                    PuxivThemeMode.System -> "主题已跟随系统"
                    PuxivThemeMode.Light -> "已切换到亮色主题"
                    PuxivThemeMode.Dark -> "已切换到暗色主题"
                },
            )
        }
    }

    fun updateMaterialYouEnabled(enabled: Boolean) {
        store.saveUseMaterialYou(enabled)
        _uiState.update {
            it.copy(
                useMaterialYou = enabled,
                message = if (enabled) {
                    "已启用 Material You 动态取色"
                } else {
                    "已关闭 Material You，使用自定义调色板"
                },
            )
        }
    }

    fun updateThemePalette(palette: PuxivThemePalette) {
        store.saveThemePalette(palette)
        _uiState.update {
            it.copy(
                themePalette = palette,
                message = "调色板已切换为 ${palette.messageLabel()}",
            )
        }
    }

    fun updateCustomThemePalette(palette: PuxivCustomPalette) {
        store.saveCustomThemePalette(palette)
        store.saveThemePalette(PuxivThemePalette.Custom)
        _uiState.update {
            it.copy(
                customPalette = palette,
                themePalette = PuxivThemePalette.Custom,
                message = "自定义调色板已保存",
            )
        }
    }

    fun updateSaveUgoiraZip(enabled: Boolean) {
        store.saveSaveUgoiraZip(enabled)
        _uiState.update {
            it.copy(
                saveUgoiraZip = enabled,
                message = if (enabled) "下载动图时会额外保留 zip" else "下载动图时仅保留动图文件",
            )
        }
    }

    fun updateUgoiraSaveFormat(format: UgoiraSaveFormat) {
        store.saveUgoiraSaveFormat(format.name)
        _uiState.update {
            it.copy(
                ugoiraSaveFormat = format,
                message = "动图保存格式已设置为 ${format.name}",
            )
        }
    }

    fun updateImageProxyEnabled(enabled: Boolean) {
        PixivImageProxy.useRemoteProxy = enabled
        store.saveUseRemoteImageProxy(enabled)
        _uiState.update {
            it.copy(
                useRemoteImageProxy = enabled,
                message = if (enabled) {
                    "使用代理获取图片，可能更快"
                } else {
                    "使用官方链接进行获取图片"
                },
            )
        }
    }

    fun updateHostIpRoutingEnabled(enabled: Boolean) {
        PixivNetworkConfig.useHostIpRouting = enabled
        dnsWarmupAttempted = !enabled
        store.saveUseHostIpRouting(enabled)
        _uiState.update {
            it.copy(
                useHostIpRouting = enabled,
                message = if (enabled) {
                    "已启用 Host/IP 兼容路由"
                } else {
                    "已关闭 Host/IP 兼容路由，HTTP 请求将绕过系统代理"
                },
            )
        }
    }

    fun updateImageProxyInput(value: String) = _uiState.update { it.copy(imageProxyInput = value.trim()) }

    fun updateFilteredTagsInput(value: String) {
        _uiState.update { it.copy(filteredTagsInput = value) }
    }

    fun saveFilteredTags() {
        val normalized = normalizeFilteredTagsInput(_uiState.value.filteredTagsInput)
        store.saveFilteredTagsInput(normalized)
        _uiState.update {
            it.copy(
                filteredTagsInput = normalized,
                message = if (normalized.isBlank()) "已清空过滤标签" else "已保存过滤标签",
            )
        }
        reloadFeedsAfterFilterChange()
    }

    private fun excludedTags(): Set<String> = _uiState.value.excludedTags()

    private fun reloadFeedsAfterFilterChange() {
        val state = _uiState.value
        if (state.keyword.isNotBlank() && state.session != null) {
            search()
        }
        if (state.home.hasLoaded) {
            loadHome(refresh = true)
        }
        if (state.discover.hasLoaded && state.session != null) {
            loadDiscover(refresh = true)
        }
        if (state.mine.hasLoaded) {
            loadMine(refresh = true)
        }
        if (state.author.userId != null && state.session != null) {
            loadAuthorWorks(refresh = true, tab = state.author.selectedTab)
        }
        if (state.selectedIllust != null && state.session != null) {
            loadRelated(refresh = true)
        }
    }

    fun saveImageProxyOrigin() {
        val normalized = PixivImageProxy.normalizeProxyOrigin(_uiState.value.imageProxyInput)
        if (normalized == null) {
            _uiState.update { it.copy(message = "请输入有效的图片代理地址") }
            return
        }
        PixivImageProxy.setProxyOrigin(normalized)
        store.saveImageProxyOrigin(normalized)
        _uiState.update {
            it.copy(
                imageProxyInput = normalized,
                message = "图片代理已设置为 $normalized",
            )
        }
    }

    fun resetImageProxyOrigin() {
        PixivImageProxy.resetProxyOrigin()
        store.clearImageProxyOrigin()
        _uiState.update {
            it.copy(
                imageProxyInput = PixivImageProxy.DEFAULT_PROXY_ORIGIN,
                message = "图片代理已恢复默认",
            )
        }
    }

    fun loadTrendingTags(refresh: Boolean = false) {
        val state = _uiState.value
        if (state.session?.accessToken.isNullOrBlank()) return
        if (state.isTrendingLoading) return
        if (!refresh && state.trendingTags.isNotEmpty()) return
        _uiState.update { it.copy(isTrendingLoading = true) }

        viewModelScope.launch {
            runCatching { withAccessToken { repository.trendingTags(it) } }
                .onSuccess { tags ->
                    _uiState.update { it.copy(trendingTags = tags, isTrendingLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isTrendingLoading = false,
                            message = error.readableMessage(),
                        )
                    }
                }
        }
    }

    fun searchTrendingTag(tag: TrendingTag) {
        val keyword = tag.name.trim().trimStart('#')
        if (keyword.isBlank()) return
        navigateTo(AppScreen.Search)
        _uiState.update {
            it.copy(
                keyword = keyword,
            )
        }
        search()
    }

    fun searchTag(tag: String) {
        val keyword = tag.trim().trimStart('#')
        if (keyword.isBlank()) return
        navigateTo(AppScreen.Search)
        _uiState.update {
            it.copy(
                keyword = keyword,
                isFullScreenPreview = false,
            )
        }
        search()
    }

    fun saveManualToken() {
        val state = _uiState.value
        val token = state.accessTokenInput.trim().removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            _uiState.update { it.copy(message = "请输入 access token") }
            return
        }
        val session = AuthSession(
            accessToken = token,
            refreshToken = state.refreshTokenInput.trim().ifBlank { null },
        )
        store.save(session)
        resetNavigation(AppScreen.Home)
        _uiState.update {
            it.copy(
                session = session,
                message = "已保存登录信息",
            )
        }
        loadHome(refresh = true)
    }

    fun startWebLogin() {
        val verifier = OAuthPkce.generateVerifier()
        store.saveCodeVerifier(verifier)
        navigateTo(AppScreen.WebLogin)
        _uiState.update {
            it.copy(
                loginUrl = "",
                message = if (_uiState.value.useHostIpRouting) {
                    "正在准备免代理登录"
                } else {
                    "正在准备系统网络登录"
                },
            )
        }
        viewModelScope.launch {
            val dnsResult = if (PixivNetworkConfig.shouldUseCompatibilityClient()) {
                runCatching { repository.refreshDns() }
            } else {
                Result.success(null)
            }
            dnsWarmupAttempted = true
            dnsResult
                .onSuccess { result ->
                    if (result == null) return@onSuccess
                    _uiState.update { state ->
                        state.copy(
                            home = state.home.copy(
                                diagnostics = state.home.diagnostics.copy(
                                    lastDnsResult = result.summary,
                                    hostSnapshot = PixivNetworkConfig.snapshot(),
                                    lastError = result.errors.values.firstOrNull(),
                                ),
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            home = state.home.copy(
                                diagnostics = state.home.diagnostics.copy(
                                    lastDnsResult = "DNS 更新失败",
                                    hostSnapshot = PixivNetworkConfig.snapshot(),
                                    lastError = error.readableMessage(),
                                ),
                            ),
                        )
                    }
                }
            _uiState.update {
                it.copy(
                    loginUrl = OAuthPkce.loginUrl(verifier),
                    message = dnsResult.exceptionOrNull()?.let { error ->
                        "DNS 更新失败，使用内置备用地址继续：${error.readableMessage()}"
                    },
                )
            }
        }
    }

    fun exchangeAuthCode(codeOverride: String? = null, useNetworkProxy: Boolean = true) {
        val state = _uiState.value
        val code = (codeOverride ?: state.authCodeInput).trim()
        val verifier = store.readCodeVerifier()
        if (code.isBlank() || verifier.isNullOrBlank()) {
            _uiState.update { it.copy(message = "缺少授权 code 或 code verifier，请先生成网页登录链接") }
            return
        }

        viewModelScope.launch {
            runBusy {
                warmupDnsIfNeeded()
                val session = repository.exchangeCode(code, verifier, useNetworkProxy)
                store.save(session)
                resetNavigation(AppScreen.Home)
                _uiState.update {
                    it.copy(
                        session = session,
                        accessTokenInput = session.accessToken,
                        refreshTokenInput = session.refreshToken.orEmpty(),
                        authCodeInput = "",
                        message = "登录成功",
                    )
                }
                loadHome(refresh = true)
            }
        }
    }

    fun submitHomeSearch() {
        if (_uiState.value.keyword.isBlank()) {
            _uiState.update { it.copy(message = "请输入搜索关键词") }
            return
        }
        navigateTo(AppScreen.Search)
        search()
    }

    fun returnToDiscover() {
        _uiState.update {
            it.copy(
                keyword = "",
                items = emptyList(),
                nextUrl = null,
                isSearchActive = false,
                isLoadingMore = false,
                message = null,
            )
        }
        loadDiscover(refresh = false)
    }

    fun loadHome(refresh: Boolean = true) {
        val state = _uiState.value
        if (!refresh && state.home.hasLoaded) return
        _uiState.update { it.copy(home = it.home.copy(hasLoaded = true)) }
        if (state.session == null) {
            loadHomeFeed(HomeFeed.Walkthrough, refresh = true)
            return
        }
        listOf(HomeFeed.Recommended, HomeFeed.Ranking, HomeFeed.Latest).forEach { loadHomeFeed(it, refresh = true) }
    }

    fun loadHomeFeed(feed: HomeFeed, refresh: Boolean = false) {
        val state = _uiState.value
        val token = state.session?.accessToken
        val anonymousWalkthrough = token.isNullOrBlank() && feed == HomeFeed.Walkthrough
        if (!anonymousWalkthrough && token.isNullOrBlank()) return
        val current = state.home.feed(feed)
        val requestRankingMode = state.rankingMode
        val requestRankingDate = state.rankingDate.apiDateOrNull()
        if (current.isLoading) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(home = state.home.withFeed(feed) { it.copy(isLoading = true, error = null) })
            }
            runCatching {
                if (anonymousWalkthrough) {
                    repository.walkthrough()
                } else {
                    withAccessToken { accessToken ->
                        if (refresh) firstHomePage(feed, accessToken, requestRankingMode, requestRankingDate) else {
                            val nextUrl = _uiState.value.home.feed(feed).nextUrl ?: return@withAccessToken null
                            repository.nextPage(nextUrl, accessToken)
                        }
                    }
                }
            }.onSuccess { page ->
                if (feed == HomeFeed.Ranking && _uiState.value.rankingRequestChanged(requestRankingMode, requestRankingDate)) {
                    return@onSuccess
                }
                if (page == null) {
                    _uiState.update { state ->
                        state.copy(home = state.home.withFeed(feed) { it.copy(isLoading = false) })
                    }
                    return@onSuccess
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { state ->
                    state.copy(
                        home = state.home.withFeed(feed) { old ->
                            old.copy(
                                items = if (refresh) filteredPage.items else old.items + filteredPage.items,
                                nextUrl = filteredPage.nextUrl,
                                isLoading = false,
                                error = null,
                            )
                        },
                    )
                }
            }.onFailure { error ->
                if (feed == HomeFeed.Ranking && _uiState.value.rankingRequestChanged(requestRankingMode, requestRankingDate)) {
                    return@onFailure
                }
                _uiState.update { state ->
                    state.copy(
                        home = state.home.withFeed(feed) {
                            it.copy(isLoading = false, error = error.readableMessage())
                        },
                    )
                }
            }
        }
    }

    fun loadDiscover(refresh: Boolean = false) {
        val state = _uiState.value
        if (state.session == null) {
            requireLogin()
            return
        }
        if (!refresh && state.discover.hasLoaded) return
        _uiState.update { it.copy(discover = it.discover.copy(hasLoaded = true)) }
        loadDiscoverFeed(DiscoverFeed.Public, refresh = true)
        loadDiscoverFeed(DiscoverFeed.Private, refresh = true)
    }

    fun loadDiscoverFeed(feed: DiscoverFeed, refresh: Boolean = false) {
        val state = _uiState.value
        if (state.session?.accessToken.isNullOrBlank()) return
        val current = state.discover.feed(feed)
        if (current.isLoading) return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(discover = currentState.discover.withFeed(feed) { it.copy(isLoading = true, error = null) })
            }
            runCatching {
                withAccessToken { token ->
                    if (refresh) {
                        val restrict = when (feed) {
                            DiscoverFeed.Public -> BookmarkRestrict.Public
                            DiscoverFeed.Private -> BookmarkRestrict.Private
                        }
                        repository.following(token, restrict)
                    } else {
                        val nextUrl = _uiState.value.discover.feed(feed).nextUrl ?: return@withAccessToken null
                        repository.nextPage(nextUrl, token)
                    }
                }
            }.onSuccess { page ->
                if (page == null) {
                    _uiState.update { currentState ->
                        currentState.copy(discover = currentState.discover.withFeed(feed) { it.copy(isLoading = false) })
                    }
                    return@onSuccess
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { currentState ->
                    currentState.copy(
                        discover = currentState.discover.withFeed(feed) { old ->
                            old.copy(
                                items = if (refresh) filteredPage.items else old.items + filteredPage.items,
                                nextUrl = filteredPage.nextUrl,
                                isLoading = false,
                                error = null,
                            )
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        discover = currentState.discover.withFeed(feed) {
                            it.copy(isLoading = false, error = error.readableMessage())
                        },
                    )
                }
            }
        }
    }

    fun loadMine(refresh: Boolean = false) {
        val state = _uiState.value
        if (state.session == null) {
            loadHistory(refresh = refresh)
            return
        }
        if (state.session.userId == null) {
            loadHistory(refresh = refresh)
            _uiState.update { it.copy(message = "当前会话缺少用户 ID，请用网页登录或授权 code 登录") }
            return
        }
        if (!refresh && state.mine.hasLoaded) return
        _uiState.update { it.copy(mine = it.mine.copy(hasLoaded = true)) }
        loadMyProfile()
        loadMyWorks(refresh = true)
        loadMyBookmarks(refresh = true)
        loadMyFollowing(FollowUserFeed.Public, refresh = true)
        loadMyFollowing(FollowUserFeed.Private, refresh = true)
        loadHistory(refresh = refresh)
    }

    fun loadMyProfile() {
        val state = _uiState.value
        val userId = state.session?.userId ?: return
        viewModelScope.launch {
            runCatching { withAccessToken { token -> repository.userDetail(userId, token) } }
                .onSuccess { profile ->
                    val followerPage = runCatching {
                        withAccessToken { token -> repository.userFollowers(userId, token) }
                    }.getOrNull()
                    _uiState.update { current ->
                        current.copy(
                            mine = current.mine.copy(
                                followCount = profile.followingCount,
                                followerCount = profile.followerCount.takeIf { it > 0 }
                                    ?: followerPage?.items?.size
                                    ?: current.mine.followerCount,
                                hasMoreFollowers = if (profile.followerCount > 0) {
                                    false
                                } else {
                                    followerPage?.nextUrl != null
                                },
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(message = error.readableMessage()) }
                }
        }
    }

    fun loadHistory(refresh: Boolean = false) {
        val state = _uiState.value
        if (state.history.isLoading) return
        if (!refresh && state.history.items.isNotEmpty()) return
        val accessToken = state.session?.accessToken
        if (accessToken.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    history = it.history.copy(
                        items = emptyList(),
                        isLoading = false,
                        error = "登录后可查看历史作品",
                    ),
                )
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            loadHistoryPage(accessToken = accessToken, offset = 0, append = false)
        }
    }

    fun loadMoreHistory() {
        val state = _uiState.value
        if (state.history.isLoading || !state.history.hasMore) return
        val accessToken = state.session?.accessToken ?: return
        viewModelScope.launch(Dispatchers.IO) {
            loadHistoryPage(
                accessToken = accessToken,
                offset = state.history.nextOffset,
                append = true,
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyStore.clear()
            _uiState.update { it.copy(history = HistoryState()) }
        }
    }

    fun deleteHistoryItem(illustId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            historyStore.delete(illustId)
            _uiState.update { state ->
                state.copy(
                    history = state.history.copy(
                        items = state.history.items.filterNot { it.illust.id == illustId },
                        nextOffset = (state.history.nextOffset - 1).coerceAtLeast(0),
                    ),
                )
            }
        }
    }

    fun loadMyWorks(refresh: Boolean = false) {
        val state = _uiState.value
        val userId = state.session?.userId ?: return
        if (state.mine.works.isLoading) return

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(mine = current.mine.copy(works = current.mine.works.copy(isLoading = true, error = null)))
            }
            runCatching {
                withAccessToken { token ->
                    if (refresh) {
                        repository.userWorks(userId, token)
                    } else {
                        val nextUrl = _uiState.value.mine.works.nextUrl ?: return@withAccessToken null
                        repository.nextPage(nextUrl, token)
                    }
                }
            }.onSuccess { page ->
                if (page == null) {
                    _uiState.update { it.copy(mine = it.mine.copy(works = it.mine.works.copy(isLoading = false))) }
                    return@onSuccess
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.copy(
                            works = current.mine.works.copy(
                                items = if (refresh) filteredPage.items else current.mine.works.items + filteredPage.items,
                                nextUrl = filteredPage.nextUrl,
                                isLoading = false,
                                error = null,
                            ),
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.copy(
                            works = current.mine.works.copy(isLoading = false, error = error.readableMessage()),
                        ),
                    )
                }
            }
        }
    }

    fun loadMyBookmarks(refresh: Boolean = false) {
        val state = _uiState.value
        val userId = state.session?.userId ?: return
        if (state.mine.bookmarks.isLoading) return

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(mine = current.mine.copy(bookmarks = current.mine.bookmarks.copy(isLoading = true, error = null)))
            }
            runCatching {
                withAccessToken { token ->
                    if (refresh) {
                        repository.bookmarkedIllusts(userId, token)
                    } else {
                        val nextUrl = _uiState.value.mine.bookmarks.nextUrl ?: return@withAccessToken null
                        repository.nextPage(nextUrl, token)
                    }
                }
            }.onSuccess { page ->
                if (page == null) {
                    _uiState.update { it.copy(mine = it.mine.copy(bookmarks = it.mine.bookmarks.copy(isLoading = false))) }
                    return@onSuccess
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.copy(
                            bookmarks = current.mine.bookmarks.copy(
                                items = if (refresh) filteredPage.items else current.mine.bookmarks.items + filteredPage.items,
                                nextUrl = filteredPage.nextUrl,
                                isLoading = false,
                                error = null,
                            ),
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.copy(
                            bookmarks = current.mine.bookmarks.copy(isLoading = false, error = error.readableMessage()),
                        ),
                    )
                }
            }
        }
    }

    fun loadMyFollowing(feed: FollowUserFeed, refresh: Boolean = false) {
        val state = _uiState.value
        val userId = state.session?.userId ?: return
        val currentFeed = state.mine.following(feed)
        if (currentFeed.isLoading) return
        if (!refresh && currentFeed.items.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.withFollowing(feed) {
                        it.copy(isLoading = true, error = null)
                    },
                )
            }
            runCatching {
                withAccessToken { token ->
                    if (refresh) {
                        val restrict = when (feed) {
                            FollowUserFeed.Public -> BookmarkRestrict.Public
                            FollowUserFeed.Private -> BookmarkRestrict.Private
                        }
                        repository.userFollowing(userId, token, restrict)
                    } else {
                        val nextUrl = _uiState.value.mine.following(feed).nextUrl ?: return@withAccessToken null
                        repository.nextUserPreviewsPage(nextUrl, token)
                    }
                }
            }.onSuccess { page ->
                if (page == null) {
                    _uiState.update { current ->
                        current.copy(
                            mine = current.mine.withFollowing(feed) { it.copy(isLoading = false) },
                        )
                    }
                    return@onSuccess
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.withFollowing(feed) { old ->
                            old.copy(
                                items = if (refresh) filteredPage.items else old.items + filteredPage.items,
                                nextUrl = filteredPage.nextUrl,
                                isLoading = false,
                                error = null,
                            )
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.withFollowing(feed) {
                            it.copy(isLoading = false, error = error.readableMessage())
                        },
                    )
                }
            }
        }
    }

    fun refreshDns(showMessage: Boolean = true) {
        if (!PixivNetworkConfig.shouldUseCompatibilityClient()) {
            _uiState.update {
                it.copy(message = if (showMessage) "Host/IP 兼容路由当前未启用" else it.message)
            }
            return
        }
        viewModelScope.launch {
            runCatching { repository.refreshDns() }
                .onSuccess { result ->
                    _uiState.update { state ->
                        state.copy(
                            home = state.home.copy(
                                diagnostics = state.home.diagnostics.copy(
                                    lastDnsResult = result.summary,
                                    hostSnapshot = PixivNetworkConfig.snapshot(),
                                    lastError = result.errors.values.firstOrNull(),
                                ),
                            ),
                            message = if (showMessage) result.summary else state.message,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            home = state.home.copy(
                                diagnostics = state.home.diagnostics.copy(
                                    lastDnsResult = "DNS 更新失败",
                                    hostSnapshot = PixivNetworkConfig.snapshot(),
                                    lastError = error.readableMessage(),
                                ),
                            ),
                            message = if (showMessage) error.readableMessage() else state.message,
                        )
                    }
                }
        }
    }

    fun runDiagnostics() {
        val session = _uiState.value.session
        if (session == null) {
            requireLogin()
            return
        }
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    home = state.home.copy(
                        diagnostics = state.home.diagnostics.copy(
                            isRunning = true,
                            apiStatus = "检测中",
                            imageStatus = "检测中",
                            lastError = null,
                        ),
                    ),
                )
            }
            val dnsResult = runCatching { repository.refreshDns() }
            val apiResult = runCatching { withAccessToken { repository.recommended(it) } }
            val probeUrl = apiResult.getOrNull()?.items?.firstOrNull()?.previewUrl
                ?: _uiState.value.home.recommended.items.firstOrNull()?.previewUrl
            val imageResult = if (probeUrl != null) runCatching { repository.probeImage(probeUrl) } else null

            _uiState.update { state ->
                val firstError = dnsResult.exceptionOrNull()
                    ?: apiResult.exceptionOrNull()
                    ?: imageResult?.exceptionOrNull()
                state.copy(
                    home = state.home.copy(
                        diagnostics = state.home.diagnostics.copy(
                            isRunning = false,
                            lastDnsResult = dnsResult.getOrNull()?.summary ?: "DNS 更新失败",
                            apiStatus = if (apiResult.isSuccess) "可用" else "失败",
                            imageStatus = when {
                                imageResult == null -> "无图片样本"
                                imageResult.isSuccess -> "可用"
                                else -> "失败"
                            },
                            lastError = firstError?.readableMessage(),
                            hostSnapshot = PixivNetworkConfig.snapshot(),
                        ),
                    ),
                    message = firstError?.readableMessage(),
                )
            }
        }
    }

    fun search() {
        val state = _uiState.value
        val keyword = state.keyword.trim()
        val sort = state.searchSort
        val target = state.searchTarget
        val startDate = state.searchStartDate.apiDateOrNull()
        val endDate = state.searchEndDate.apiDateOrNull()
        val bookmarkNum = state.searchBookmarkNum.apiPositiveIntOrNull()
        if (state.session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        if (keyword.isBlank()) {
            _uiState.update { it.copy(message = "请输入搜索关键词") }
            return
        }

        _uiState.update {
            it.copy(
                items = emptyList(),
                nextUrl = null,
                isSearchActive = true,
                isLoadingMore = false,
            )
        }
        viewModelScope.launch {
            runBusy {
                val page = withAccessToken {
                    repository.search(
                        keyword = keyword,
                        token = it,
                        sort = sort,
                        searchTarget = target,
                        startDate = startDate,
                        endDate = endDate,
                        bookmarkNum = bookmarkNum,
                    )
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { current ->
                    if (
                        current.keyword.trim() != keyword ||
                        current.searchSort != sort ||
                        current.searchTarget != target ||
                        current.searchStartDate.apiDateOrNull() != startDate ||
                        current.searchEndDate.apiDateOrNull() != endDate ||
                        current.searchBookmarkNum.apiPositiveIntOrNull() != bookmarkNum
                    ) {
                        current
                    } else {
                        current.copy(
                            items = filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isSearchActive = true,
                            message = null,
                        )
                    }
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        val nextUrl = state.nextUrl ?: return
        val token = state.session?.accessToken ?: return
        if (state.isLoadingMore || state.isBusy) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, message = null) }
            runCatching { withAccessToken { repository.nextPage(nextUrl, it) } }
                .onSuccess { page ->
                    val filteredPage = page.filteredBy(excludedTags())
                    _uiState.update {
                        it.copy(
                            items = it.items + filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isLoadingMore = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingMore = false, message = error.readableMessage()) }
                }
        }
    }

    fun openFullScreenPreview(startIndex: Int = _uiState.value.selectedImageIndex) {
        _uiState.update { state ->
            val size = state.selectedIllust?.previewImageCount() ?: 0
            state.copy(
                selectedImageIndex = startIndex.coerceIn(0, (size - 1).coerceAtLeast(0)),
                isFullScreenPreview = true,
            )
        }
    }

    fun closeFullScreenPreview() {
        _uiState.update { it.copy(isFullScreenPreview = false) }
    }

    fun loadRelated(refresh: Boolean = false) {
        val illustId = _uiState.value.selectedIllust?.id ?: return
        loadRelatedFeed(illustId, refresh)
    }

    fun bookmarkIllust(illustId: Long, restrict: BookmarkRestrict = BookmarkRestrict.Public) {
        if (_uiState.value.session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        if (illustId <= 0L) {
            _uiState.update { it.copy(message = "作品 ID 无效") }
            return
        }

        viewModelScope.launch {
            runBusy {
                withAccessToken { repository.addBookmark(illustId, it, restrict) }
                markIllustBookmarked(illustId, isBookmarked = true)
                _uiState.update { it.copy(message = "已收藏作品") }
            }
        }
    }

    fun deleteBookmark(illustId: Long) {
        if (_uiState.value.session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        if (illustId <= 0L) {
            _uiState.update { it.copy(message = "作品 ID 无效") }
            return
        }

        viewModelScope.launch {
            runBusy {
                withAccessToken { repository.deleteBookmark(illustId, it) }
                markIllustBookmarked(illustId, isBookmarked = false)
                _uiState.update { it.copy(message = "已取消收藏") }
            }
        }
    }

    fun bookmarkSelected(restrict: BookmarkRestrict = BookmarkRestrict.Public) {
        val illustId = _uiState.value.selectedIllust?.id
        if (illustId == null) {
            _uiState.update { it.copy(message = "没有选中的作品") }
            return
        }
        bookmarkIllust(illustId, restrict)
    }

    fun deleteSelectedBookmark() {
        val illustId = _uiState.value.selectedIllust?.id
        if (illustId == null) {
            _uiState.update { it.copy(message = "没有选中的作品") }
            return
        }
        deleteBookmark(illustId)
    }

    fun toggleBookmark(illust: Illust) {
        if (illust.isBookmarked) {
            deleteBookmark(illust.id)
        } else {
            bookmarkIllust(illust.id)
        }
    }

    fun toggleSelectedBookmark() {
        val illust = _uiState.value.selectedIllust
        if (illust == null) {
            _uiState.update { it.copy(message = "没有选中的作品") }
            return
        }
        toggleBookmark(illust)
    }

    fun downloadSelectedIllust() {
        val illust = _uiState.value.selectedIllust
        if (illust == null) {
            _uiState.update { it.copy(message = "没有选中的作品") }
            return
        }
        enqueueIllustDownload(illust)
    }

    fun enqueueIllustDownload(illust: Illust) {
        val requiresAuth = illust.isUgoira
        if (requiresAuth && _uiState.value.session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        val pages = illust.imageUrls.ifEmpty { listOfNotNull(illust.previewUrl) }
        if (!illust.isUgoira && pages.isEmpty()) {
            _uiState.update { it.copy(message = "没有可下载的图片地址") }
            return
        }

        val ugoiraFormat = _uiState.value.ugoiraSaveFormat
        val job = DownloadItem(
            key = "${illust.id}-${System.nanoTime()}",
            illustId = illust.id,
            title = illust.title,
            illust = illust,
            fileName = if (illust.isUgoira) {
                "${illust.safeFolderName()}_动图.${ugoiraFormat.extension}"
            } else {
                illust.safeFolderName()
            },
            status = DownloadStatus.Queued,
            isUgoira = illust.isUgoira,
            pageCount = if (illust.isUgoira) 1 else pages.size,
            relativePath = if (illust.isUgoira) {
                buildDownloadRelativePath(
                    rootDirectory = Environment.DIRECTORY_PICTURES,
                    authorName = illust.authorName,
                )
            } else {
                buildDownloadRelativePath(
                    rootDirectory = Environment.DIRECTORY_PICTURES,
                    authorName = illust.authorName,
                    title = illust.title,
                )
            },
            detail = if (illust.isUgoira) "等待合成 ${ugoiraFormat.name}" else "等待下载 ${pages.size} 张",
        )

        _uiState.update { state ->
            val downloads = state.downloads.copy(items = listOf(job) + state.downloads.items.filterNot { it.illustId == illust.id })
            store.saveDownloads(downloads.items)
            state.copy(
                downloads = downloads,
                message = "已加入下载队列",
            )
        }
        startDownload(job, illust)
    }

    fun deleteDownloadItem(key: String) {
        _uiState.update { state ->
            val downloads = state.downloads.copy(
                items = state.downloads.items.filterNot { it.key == key },
            )
            store.saveDownloads(downloads.items)
            state.copy(
                downloads = downloads,
                message = "已删除下载记录",
            )
        }
    }

    fun uploadIllust(
        title: String,
        caption: String,
        tags: List<String>,
        type: String,
        visibilityScope: Int,
        xRestrict: String,
        isSexual: Boolean,
        illustAiType: Int,
        imageUris: List<Uri>,
    ) {
        if (_uiState.value.session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        if (title.isBlank()) {
            _uiState.update { it.copy(message = "请输入作品标题") }
            return
        }
        if (imageUris.isEmpty()) {
            _uiState.update { it.copy(message = "请至少选择一张图片") }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    mine = state.mine.copy(isUploading = true, uploadStatus = "正在上传"),
                    message = null,
                )
            }
            runCatching {
                val request = UploadIllustRequest(
                    title = title.trim(),
                    caption = caption.trim(),
                    tags = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(10),
                    type = type.takeIf { it == "manga" } ?: "illust",
                    visibilityScope = visibilityScope.coerceIn(1, 3),
                    commentAccessControl = 0,
                    xRestrict = xRestrict.takeIf { it == "r18" || it == "r18g" } ?: "none",
                    isSexual = isSexual,
                    illustAiType = illustAiType.coerceIn(1, 2),
                    images = imageUris.take(20).toUploadParts(),
                )
                withAccessToken { token -> repository.uploadIllust(token, request) }
            }.onSuccess { status ->
                val summary = status.illustId?.takeIf { it > 0L }?.let { "投稿成功：#$it" }
                    ?: status.status?.takeIf { it.isNotBlank() }?.let { "投稿处理中：$it" }
                    ?: "投稿已提交"
                _uiState.update { state ->
                    state.copy(
                        mine = state.mine.copy(isUploading = false, uploadStatus = summary),
                        message = summary,
                    )
                }
                loadMyWorks(refresh = true)
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        mine = state.mine.copy(isUploading = false, uploadStatus = error.readableMessage()),
                        message = error.readableMessage(),
                    )
                }
            }
        }
    }

    fun openPreview(illust: Illust) {
        val state = _uiState.value
        val currentScreen = state.screen
        val returnScreen = currentScreen.takeIf { it != AppScreen.Preview } ?: state.previewReturnScreen
        if (currentScreen == AppScreen.Preview) {
            previewBackStack.addLast(state.toPreviewSnapshot())
        } else {
            previewBackStack.clear()
            pushBackStack(returnScreen)
        }
        _uiState.update {
            it.copy(
                selectedIllust = illust,
                selectedImageIndex = 0,
                related = FeedState(),
                comments = CommentState(),
                ugoiraFrames = emptyList(),
                ugoiraLoadedFrames = 0,
                ugoiraTotalFrames = 0,
                previewReturnScreen = returnScreen,
                screen = AppScreen.Preview,
                message = null,
            )
        }
        refreshPreviewDetail(illust)
        saveHistory(illust)
    }

    fun openFullScreenPreviewForIllust(illust: Illust) {
        openPreview(illust)
        _uiState.update { it.copy(isFullScreenPreview = true) }
    }

    fun openDownloadedPreview(item: DownloadItem) {
        val savedUris = item.savedUris.orEmpty()
            .mapNotNull(::nonBlankStringOrNull)
            .ifEmpty { listOfNotNull(item.savedUri?.takeIf { it.isNotBlank() }) }
        val firstSavedUri = savedUris.firstOrNull()
        if (firstSavedUri == null) {
            _uiState.update { it.copy(message = "没有可预览的本地文件") }
            return
        }
        val localPages = savedUris.map {
            JunZi.Pixiv.data.model.IllustImagePage(
                url = it,
                width = item.illust?.width ?: 1,
                height = item.illust?.height ?: 1,
            )
        }
        val sourceIllust = item.illust ?: Illust(
            id = item.illustId,
            title = item.title,
            authorId = 0L,
            authorName = "",
            authorAccount = "",
            authorAvatarUrl = null,
            type = "illust",
            caption = "",
            previewUrl = firstSavedUri,
            imageUrls = savedUris,
            imagePages = localPages,
            tags = emptyList(),
            pageCount = savedUris.size,
            width = item.illust?.width ?: 1,
            height = item.illust?.height ?: 1,
            totalBookmarks = 0,
            totalView = 0,
            isBookmarked = false,
            aiType = null,
            createDate = null,
        )
        val currentScreen = _uiState.value.screen
        val returnScreen = currentScreen.takeIf { it != AppScreen.Preview } ?: _uiState.value.previewReturnScreen
        val localIllust = sourceIllust.copy(
            type = if (item.isUgoira) "illust" else sourceIllust.type,
            previewUrl = firstSavedUri,
            imageUrls = savedUris,
            imagePages = savedUris.map {
                JunZi.Pixiv.data.model.IllustImagePage(
                    url = it,
                    width = sourceIllust.width,
                    height = sourceIllust.height,
                )
            },
            pageCount = savedUris.size,
        )
        if (currentScreen == AppScreen.Preview) {
            previewBackStack.addLast(_uiState.value.toPreviewSnapshot())
        } else {
            previewBackStack.clear()
            pushBackStack(returnScreen)
        }
        _uiState.update {
            it.copy(
                selectedIllust = localIllust,
                selectedImageIndex = 0,
                related = FeedState(),
                comments = CommentState(),
                ugoiraFrames = emptyList(),
                ugoiraLoadedFrames = 0,
                ugoiraTotalFrames = 0,
                isPreviewLoading = false,
                isFullScreenPreview = false,
                previewReturnScreen = returnScreen,
                screen = AppScreen.Preview,
                message = null,
            )
        }
        if (item.illustId > 0L) {
            loadRelatedFeed(item.illustId, refresh = true)
            loadComments(item.illustId)
            saveHistory(localIllust)
        }
    }

    fun openPreviewById(illustId: Long, fullScreen: Boolean = false) {
        val session = _uiState.value.session
        if (session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        viewModelScope.launch {
            runBusy {
                val illust = withAccessToken { token -> repository.detail(illustId, token) }
                openPreview(illust)
                if (fullScreen) {
                    _uiState.update { it.copy(isFullScreenPreview = true) }
                }
            }
        }
    }

    fun openAuthor(illust: Illust) {
        val authorId = illust.authorId.takeIf { it > 0L } ?: run {
            _uiState.update { it.copy(message = "未找到作者信息") }
            return
        }
        openAuthor(
            userId = authorId,
            userName = illust.authorName,
            userAccount = illust.authorAccount,
            userAvatarUrl = illust.authorAvatarUrl,
        )
    }

    fun openAuthor(user: UserPreview) {
        openAuthor(
            userId = user.userId,
            userName = user.userName,
            userAccount = user.userAccount,
            userAvatarUrl = user.avatarUrl,
        )
    }

    private fun openAuthor(
        userId: Long,
        userName: String,
        userAccount: String,
        userAvatarUrl: String?,
    ) {
        navigateTo(AppScreen.Author)
        _uiState.update {
            it.copy(
                author = AuthorState(
                    userId = userId,
                    userName = userName,
                    userAccount = userAccount,
                    userAvatarUrl = userAvatarUrl,
                    selectedTab = AuthorWorkTab.Illust,
                ),
                message = null,
            )
        }
        loadAuthorProfile()
        loadAuthorWorks(refresh = true, tab = AuthorWorkTab.Illust)
    }

    fun selectAuthorTab(tab: AuthorWorkTab) {
        val current = _uiState.value.author
        if (current.selectedTab == tab) return
        _uiState.update { it.copy(author = it.author.copy(selectedTab = tab)) }
        val feed = _uiState.value.author.feed(tab)
        if (feed.items.isEmpty()) {
            loadAuthorWorks(refresh = true, tab = tab)
        }
    }

    fun loadAuthorProfile() {
        val authorId = _uiState.value.author.userId ?: return
        if (_uiState.value.session?.accessToken.isNullOrBlank()) return
        if (_uiState.value.author.isLoadingProfile) return
        viewModelScope.launch {
            _uiState.update { it.copy(author = it.author.copy(isLoadingProfile = true)) }
            runCatching { withAccessToken { token -> repository.userDetail(authorId, token) } }
                .onSuccess { profile ->
                    if (_uiState.value.author.userId != authorId) return@onSuccess
                    _uiState.update {
                        it.copy(
                            author = it.author.mergeProfile(profile).copy(isLoadingProfile = false),
                        )
                    }
                }
                .onFailure { error ->
                    if (_uiState.value.author.userId != authorId) return@onFailure
                    _uiState.update {
                        it.copy(
                            author = it.author.copy(isLoadingProfile = false),
                            message = error.readableMessage(),
                        )
                    }
                }
        }
    }

    fun loadAuthorWorks(refresh: Boolean = false, tab: AuthorWorkTab = _uiState.value.author.selectedTab) {
        val state = _uiState.value
        val authorId = state.author.userId ?: return
        if (state.session?.accessToken.isNullOrBlank()) return
        if (state.author.isLoadingWorks) return
        if (!refresh && state.author.feed(tab).nextUrl == null) return

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    author = current.author.copy(
                        isLoadingWorks = true,
                    ).withFeed(tab) { it.copy(error = null) },
                )
            }
            runCatching {
                withAccessToken { token ->
                    if (refresh) {
                        when (tab) {
                            AuthorWorkTab.Bookmarks -> repository.bookmarkedIllusts(authorId, token)
                            else -> repository.userWorks(authorId, token, tab.apiValue)
                        }
                    } else {
                        val nextUrl = _uiState.value.author.feed(tab).nextUrl ?: return@withAccessToken null
                        repository.nextPage(nextUrl, token)
                    }
                }
            }.onSuccess { page ->
                val currentAuthorId = _uiState.value.author.userId
                if (currentAuthorId != authorId) return@onSuccess
                if (page == null) {
                    _uiState.update { it.copy(author = it.author.copy(isLoadingWorks = false)) }
                    return@onSuccess
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { current ->
                    current.copy(
                        author = current.author.copy(
                            isLoadingWorks = false,
                        ).withFeed(tab) { feed ->
                            feed.copy(
                                items = if (refresh) filteredPage.items else feed.items + filteredPage.items,
                                nextUrl = filteredPage.nextUrl,
                                isLoading = false,
                                error = null,
                            )
                        },
                    )
                }
            }.onFailure { error ->
                if (_uiState.value.author.userId != authorId) return@onFailure
                _uiState.update {
                    it.copy(
                        author = it.author.copy(isLoadingWorks = false).withFeed(tab) { feed ->
                            feed.copy(error = error.readableMessage(), isLoading = false)
                        },
                        message = error.readableMessage(),
                    )
                }
            }
        }
    }

    fun followAuthor(restrict: BookmarkRestrict = BookmarkRestrict.Public) {
        val authorId = _uiState.value.author.userId ?: return
        if (_uiState.value.session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        if (_uiState.value.author.isFollowBusy) return
        viewModelScope.launch {
            _uiState.update { it.copy(author = it.author.copy(isFollowBusy = true)) }
            runCatching { withAccessToken { token -> repository.followUser(authorId, token, restrict) } }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            author = it.author.copy(
                                isFollowBusy = false,
                                isFollowed = true,
                            ),
                            message = if (restrict == BookmarkRestrict.Private) "已悄悄关注作者" else "已关注作者",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            author = it.author.copy(isFollowBusy = false),
                            message = error.readableMessage(),
                        )
                    }
                }
        }
    }

    fun unfollowAuthor() {
        val authorId = _uiState.value.author.userId ?: return
        if (_uiState.value.session?.accessToken.isNullOrBlank()) {
            requireLogin()
            return
        }
        if (_uiState.value.author.isFollowBusy) return
        viewModelScope.launch {
            _uiState.update { it.copy(author = it.author.copy(isFollowBusy = true)) }
            runCatching { withAccessToken { token -> repository.unfollowUser(authorId, token) } }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            author = it.author.copy(
                                isFollowBusy = false,
                                isFollowed = false,
                            ),
                            message = "已取消关注作者",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            author = it.author.copy(isFollowBusy = false),
                            message = error.readableMessage(),
                        )
                    }
                }
        }
    }

    fun selectImage(index: Int) {
        _uiState.update { state ->
            val size = state.selectedIllust?.previewImageCount() ?: 0
            val selectedIndex = index.coerceIn(0, (size - 1).coerceAtLeast(0))
            if (state.selectedImageIndex == selectedIndex) {
                state
            } else {
                state.copy(selectedImageIndex = selectedIndex)
            }
        }
    }

    fun updateCommentInput(value: String) {
        _uiState.update { it.copy(comments = it.comments.copy(input = value.take(500))) }
    }

    fun sendComment() {
        val state = _uiState.value
        val illustId = state.selectedIllust?.id
        val comment = state.comments.input.trim()
        if (illustId == null) {
            _uiState.update { it.copy(message = "没有选中的作品") }
            return
        }
        if (comment.isBlank()) {
            _uiState.update { it.copy(message = "请输入评论内容") }
            return
        }
        if (state.comments.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(comments = it.comments.copy(isSending = true, error = null)) }
            runCatching {
                withAccessToken { token -> repository.addComment(illustId, comment, token) }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        comments = it.comments.copy(isSending = false, input = ""),
                        message = "评论已发送",
                    )
                }
                loadComments(illustId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        comments = it.comments.copy(isSending = false, error = error.readableMessage()),
                        message = error.readableMessage(),
                    )
                }
            }
        }
    }

    fun closePreview() = goBack()

    fun backToLogin() = goBack()

    fun logout() {
        store.clear()
        previewBackStack.clear()
        resetNavigation(AppScreen.Me)
        _uiState.update {
            it.copy(
                session = null,
                accessTokenInput = "",
                refreshTokenInput = "",
                home = HomeState(),
                mine = MyState(),
                author = AuthorState(),
                trendingTags = emptyList(),
                discover = DiscoverState(),
                items = emptyList(),
                nextUrl = null,
                isSearchActive = false,
                selectedIllust = null,
                related = FeedState(),
                comments = CommentState(),
                message = "已退出登录",
            )
        }
    }

    fun goBack() {
        val currentState = _uiState.value
        val current = currentState.screen
        if (current == AppScreen.Preview && previewBackStack.isNotEmpty()) {
            val previous = previewBackStack.removeLast()
            _uiState.update {
                it.copy(
                    selectedIllust = previous.illust,
                    selectedImageIndex = previous.selectedImageIndex,
                    related = previous.related,
                    comments = previous.comments,
                    ugoiraFrames = previous.ugoiraFrames,
                    ugoiraLoadedFrames = previous.ugoiraLoadedFrames,
                    ugoiraTotalFrames = previous.ugoiraTotalFrames,
                    previewReturnScreen = previous.returnScreen,
                    isFullScreenPreview = false,
                    isPreviewLoading = false,
                    message = null,
                )
            }
            return
        }
        val targetEntry = backStack.removeLastOrNull()
        val target = targetEntry?.screen ?: current.defaultBackTarget()
        if (current == AppScreen.Preview) {
            previewBackStack.clear()
        }
        _uiState.update { state ->
            val previewSnapshot = targetEntry?.preview?.takeIf { target == AppScreen.Preview }
            val authorSnapshot = targetEntry?.author?.takeIf {
                target == AppScreen.Author && state.author.userId != it.userId
            }
            state.copy(
                screen = target,
                selectedIllust = when {
                    previewSnapshot != null -> previewSnapshot.illust
                    current == AppScreen.Preview -> null
                    else -> state.selectedIllust
                },
                selectedImageIndex = previewSnapshot?.selectedImageIndex
                    ?: if (current == AppScreen.Preview) 0 else state.selectedImageIndex,
                author = when {
                    authorSnapshot != null -> authorSnapshot
                    current == AppScreen.Author && target != AppScreen.Preview -> AuthorState()
                    else -> state.author
                },
                related = previewSnapshot?.related ?: if (current == AppScreen.Preview) FeedState() else state.related,
                comments = previewSnapshot?.comments ?: if (current == AppScreen.Preview) CommentState() else state.comments,
                ugoiraFrames = previewSnapshot?.ugoiraFrames ?: if (current == AppScreen.Preview) emptyList() else state.ugoiraFrames,
                ugoiraLoadedFrames = previewSnapshot?.ugoiraLoadedFrames ?: if (current == AppScreen.Preview) 0 else state.ugoiraLoadedFrames,
                ugoiraTotalFrames = previewSnapshot?.ugoiraTotalFrames ?: if (current == AppScreen.Preview) 0 else state.ugoiraTotalFrames,
                previewReturnScreen = previewSnapshot?.returnScreen ?: state.previewReturnScreen,
                isFullScreenPreview = if (previewSnapshot != null || current == AppScreen.Preview) false else state.isFullScreenPreview,
                isPreviewLoading = if (previewSnapshot != null || current == AppScreen.Preview) false else state.isPreviewLoading,
                message = null,
            )
        }
        when (target) {
            AppScreen.Home -> loadHome(refresh = false)
            AppScreen.Search -> {
                loadTrendingTags(refresh = false)
                loadDiscover(refresh = false)
            }
            AppScreen.Me -> loadMine(refresh = false)
            AppScreen.Author -> {
                if (_uiState.value.author.userId != null) {
                    loadAuthorProfile()
                    loadAuthorWorks(
                        refresh = _uiState.value.author.feed(_uiState.value.author.selectedTab).items.isEmpty(),
                        tab = _uiState.value.author.selectedTab,
                    )
                }
            }
            else -> Unit
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
        val current = _uiState.value.screen
        if (current == screen) return
        if (addToBackStack) pushBackStack(current)
        _uiState.update { it.copy(screen = screen) }
    }

    private fun pushBackStack(screen: AppScreen) {
        val entry = _uiState.value.toNavigationEntry(screen) ?: return
        if (backStack.lastOrNull()?.screen == screen) {
            backStack.removeLast()
        }
        backStack.addLast(entry)
    }

    private fun resetNavigation(screen: AppScreen) {
        backStack.clear()
        previewBackStack.clear()
        _uiState.update { it.copy(screen = screen) }
    }

    private fun requireLogin() {
        _uiState.update { it.copy(message = "请先在我的页面登录") }
    }

    private fun refreshPreviewDetail(illust: Illust) {
        val token = _uiState.value.session?.accessToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isPreviewLoading = true) }
            runCatching { withAccessToken { repository.detail(illust.id, it) } }
                .onSuccess { detail ->
                    if (_uiState.value.selectedIllust?.id != illust.id) return@onSuccess
                    _uiState.update { it.copy(selectedIllust = detail, isPreviewLoading = detail.isUgoira) }
                    saveHistory(detail)
                    loadRelatedFeed(detail.id, refresh = true)
                    loadComments(detail.id)
                    if (detail.isUgoira) {
                        loadUgoiraFrames(detail.id)
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isPreviewLoading = false, message = error.readableMessage()) }
                }
        }
    }

    private fun loadUgoiraFrames(illustId: Long) {
        viewModelScope.launch {
            runCatching {
                withAccessToken { token ->
                    repository.ugoiraFrames(illustId, token) { loaded, total ->
                        _uiState.update {
                            if (it.selectedIllust?.id == illustId) {
                                it.copy(ugoiraLoadedFrames = loaded, ugoiraTotalFrames = total)
                            } else {
                                it
                            }
                        }
                    }
                }
            }
                .onSuccess { frames ->
                    _uiState.update {
                        it.copy(
                            ugoiraFrames = frames,
                            ugoiraLoadedFrames = frames.size,
                            ugoiraTotalFrames = frames.size.coerceAtLeast(it.ugoiraTotalFrames),
                            isPreviewLoading = false,
                            message = if (frames.isEmpty()) "未能取得动画帧数据" else null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isPreviewLoading = false, message = error.readableMessage()) }
                }
        }
    }

    private fun loadRelatedFeed(illustId: Long, refresh: Boolean) {
        val state = _uiState.value
        if (state.session?.accessToken.isNullOrBlank()) return
        if (state.related.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(related = it.related.copy(isLoading = true, error = null)) }
            runCatching {
                withAccessToken { accessToken ->
                    if (refresh) {
                        repository.related(illustId, accessToken)
                    } else {
                        val nextUrl = _uiState.value.related.nextUrl ?: return@withAccessToken null
                        repository.nextPage(nextUrl, accessToken)
                    }
                }
            }.onSuccess { page ->
                if (_uiState.value.selectedIllust?.id != illustId) return@onSuccess
                if (page == null) {
                    _uiState.update { it.copy(related = it.related.copy(isLoading = false)) }
                    return@onSuccess
                }
                val filteredPage = page.filteredBy(excludedTags())
                _uiState.update { current ->
                    current.copy(
                        related = current.related.copy(
                            items = if (refresh) filteredPage.items else current.related.items + filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isLoading = false,
                            error = null,
                        ),
                    )
                }
            }.onFailure { error ->
                if (_uiState.value.selectedIllust?.id != illustId) return@onFailure
                _uiState.update {
                    it.copy(
                        related = it.related.copy(
                            isLoading = false,
                            error = error.readableMessage(),
                        ),
                    )
                }
            }
        }
    }

    private fun loadComments(illustId: Long) {
        val state = _uiState.value
        if (state.session?.accessToken.isNullOrBlank()) return
        if (state.comments.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(comments = it.comments.copy(isLoading = true, error = null)) }
            runCatching { withAccessToken { token -> repository.comments(illustId, token) } }
                .onSuccess { page ->
                    if (_uiState.value.selectedIllust?.id != illustId) return@onSuccess
                    _uiState.update {
                        it.copy(
                            comments = it.comments.copy(
                                items = page.items,
                                nextUrl = page.nextUrl,
                                isLoading = false,
                                error = null,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    if (_uiState.value.selectedIllust?.id != illustId) return@onFailure
                    _uiState.update {
                        it.copy(comments = it.comments.copy(isLoading = false, error = error.readableMessage()))
                    }
                }
        }
    }

    private fun startDownload(item: DownloadItem, illust: Illust) {
        viewModelScope.launch(Dispatchers.IO) {
            updateDownload(item.key, persist = false) {
                it.copy(status = DownloadStatus.Running, detail = "下载中")
            }
            runCatching {
                if (illust.isUgoira) {
                    val result = withAccessToken { token ->
                        repository.downloadUgoira(
                            id = illust.id,
                            token = token,
                            includeZip = _uiState.value.saveUgoiraZip,
                            workingDirectory = getApplication<Application>().cacheDir,
                            saveFormat = _uiState.value.ugoiraSaveFormat,
                        ) { stage, current, total ->
                            val detail = when {
                                total > 0 -> "$stage $current/$total"
                                else -> stage
                            }
                            updateDownload(item.key, persist = false) {
                                it.copy(
                                    status = DownloadStatus.Running,
                                    detail = detail,
                                )
                            }
                        }
                    }
                    updateDownload(item.key, persist = false) {
                        it.copy(
                            status = DownloadStatus.Running,
                            detail = "保存动图",
                        )
                    }
                    val extension = result.format.extension
                    val animatedFileName = item.fileName.substringBeforeLast('.') + ".$extension"
                    val animatedUri = saveDownloadBytes(item.relativePath, animatedFileName, result.animatedBytes)
                    val zipUri = result.zipBytes?.let { zipBytes ->
                        updateDownload(item.key, persist = false) {
                            it.copy(
                                status = DownloadStatus.Running,
                                detail = "保存 ZIP",
                            )
                        }
                        saveDownloadBytes(
                            buildDownloadRelativePath(
                                rootDirectory = Environment.DIRECTORY_DOWNLOADS,
                                authorName = illust.authorName,
                                title = illust.title,
                            ),
                            "${illust.safeFolderName()}_原图.zip",
                            zipBytes,
                        )
                    }
                    DownloadWriteResult(
                        mainUri = animatedUri,
                        zipUri = zipUri,
                        format = result.format,
                        savedUris = listOf(animatedUri.toString()),
                    )
                } else {
                    val pages = illust.imageUrls.ifEmpty { listOfNotNull(illust.previewUrl) }
                    val savedUris = mutableListOf<Uri>()
                    pages.forEachIndexed { index, imageUrl ->
                        updateDownload(item.key, persist = false) {
                            it.copy(
                                status = DownloadStatus.Running,
                                detail = "下载第 ${index + 1}/${pages.size} 张",
                            )
                        }
                        val bytes = repository.downloadImageBytes(imageUrl)
                        val savedUri = saveDownloadBytes(
                            item.relativePath,
                            "${(index + 1).toString().padStart(2, '0')}${imageUrl.fileExtension()}",
                            bytes,
                        )
                        savedUris += savedUri
                    }
                    DownloadWriteResult(
                        mainUri = requireNotNull(savedUris.firstOrNull()),
                        savedUris = savedUris.map { it.toString() },
                    )
                }
            }.onSuccess { result ->
                updateDownload(item.key) {
                    it.copy(
                        status = DownloadStatus.Finished,
                        fileName = if (illust.isUgoira && result.format != null) {
                            item.fileName.substringBeforeLast('.') + ".${result.format.extension}"
                        } else {
                            item.fileName
                        },
                        detail = when {
                            illust.isUgoira && result.zipUri != null -> "${result.format?.name ?: "动图"} 已保存，ZIP 已另存"
                            illust.isUgoira -> "${result.format?.name ?: "动图"} 已保存"
                            item.pageCount > 1 -> "已保存 ${item.pageCount} 张"
                            else -> "已保存"
                        },
                        savedUri = result.mainUri.toString(),
                        savedUris = result.savedUris.ifEmpty { listOf(result.mainUri.toString()) },
                        zipSavedUri = result.zipUri?.toString(),
                    )
                }
            }.onFailure { error ->
                updateDownload(item.key) {
                    it.copy(status = DownloadStatus.Failed, detail = error.readableMessage())
                }
            }
        }
    }

    private fun updateDownload(
        key: String,
        persist: Boolean = true,
        transform: (DownloadItem) -> DownloadItem,
    ) {
        _uiState.update { state ->
            val downloads = state.downloads.copy(
                items = state.downloads.items.map { item ->
                    if (item.key == key) transform(item) else item
                },
            )
            if (persist) {
                store.saveDownloads(downloads.items)
            }
            state.copy(
                downloads = downloads,
            )
        }
    }

    private suspend fun saveDownloadBytes(relativePath: String, fileName: String, bytes: ByteArray): Uri = withContext(Dispatchers.IO) {
        val app = getApplication<Application>()
        val cleanFileName = fileName.ifBlank { "simple_pixiv_${System.currentTimeMillis()}.jpg" }
        val cleanRelativePath = relativePath.ifBlank { Environment.DIRECTORY_PICTURES + "/IllustFerry" }
        val mimeType = cleanFileName.mimeType()
        val isImageTarget = mimeType.startsWith("image/")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, cleanFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, cleanRelativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = app.contentResolver
            val collection = if (isImageTarget) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
            val uri = requireNotNull(resolver.insert(collection, values)) {
                "无法创建下载文件"
            }
            runCatching {
                resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("无法写入下载文件")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }.onFailure {
                resolver.delete(uri, null, null)
                throw it
            }
            uri
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(cleanRelativePath.substringBefore('/')),
                cleanRelativePath.substringAfter('/', ""),
            )
            if (!dir.exists() && !dir.mkdirs()) error("无法创建下载目录")
            val file = File(dir, cleanFileName)
            file.writeBytes(bytes)
            Uri.fromFile(file)
        }
    }

    private suspend fun runBusy(block: suspend () -> Unit) {
        _uiState.update { it.copy(isBusy = true, message = null) }
        runCatching { block() }
            .onFailure { error -> _uiState.update { it.copy(message = error.readableMessage()) } }
        _uiState.update { it.copy(isBusy = false) }
    }

    private fun saveHistory(illust: Illust) {
        viewModelScope.launch(Dispatchers.IO) {
            historyStore.save(illust.id)
            val accessToken = _uiState.value.session?.accessToken
            if (!accessToken.isNullOrBlank()) {
                val entries = runCatching { historyStore.recentPage(limit = HISTORY_PAGE_SIZE, offset = 0) }.getOrDefault(emptyList())
                val items = runCatching { loadHistoryItems(entries, accessToken) }.getOrNull()
                if (items != null) {
                    _uiState.update {
                        it.copy(
                            history = it.history.copy(
                                items = items,
                                isLoading = false,
                                error = null,
                                nextOffset = entries.size,
                                hasMore = entries.size >= HISTORY_PAGE_SIZE,
                            ),
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadHistoryItems(entries: List<JunZi.Pixiv.data.local.HistoryEntry>, accessToken: String): List<HistoryItem> = coroutineScope {
        entries.map { entry ->
            async {
                runCatching {
                    HistoryItem(
                        illust = repository.detail(entry.illustId, accessToken),
                        viewedAtMillis = entry.viewedAtMillis,
                    )
                }.getOrNull()
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun loadHistoryPage(
        accessToken: String,
        offset: Int,
        append: Boolean,
    ) {
        _uiState.update { it.copy(history = it.history.copy(isLoading = true, error = null)) }
        val entriesResult = runCatching { historyStore.recentPage(limit = HISTORY_PAGE_SIZE, offset = offset) }
        val entries = entriesResult.getOrDefault(emptyList())
        val itemsResult = runCatching { loadHistoryItems(entries, accessToken) }
        _uiState.update { state ->
            val previousItems = if (append) state.history.items else emptyList()
            state.copy(
                history = state.history.copy(
                    items = previousItems + itemsResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error = entriesResult.exceptionOrNull()?.readableMessage() ?: itemsResult.exceptionOrNull()?.readableMessage(),
                    nextOffset = offset + entries.size,
                    hasMore = entries.size >= HISTORY_PAGE_SIZE,
                ),
            )
        }
    }

    private suspend fun firstHomePage(
        feed: HomeFeed,
        token: String,
        rankingMode: RankingMode,
        rankingDate: String?,
    ): IllustPage {
        return when (feed) {
            HomeFeed.Walkthrough -> repository.walkthrough(token)
            HomeFeed.Recommended -> repository.recommended(token)
            HomeFeed.Ranking -> repository.ranking(token, rankingMode, rankingDate)
            HomeFeed.Latest -> repository.latest(token)
        }
    }

    private suspend fun <T> withAccessToken(block: suspend (String) -> T): T {
        warmupDnsIfNeeded()
        var session = requireNotNull(_uiState.value.session) { "请先登录" }
        if (session.shouldRefresh()) {
            session = refreshSession(session)
        }
        return try {
            block(session.accessToken)
        } catch (error: PixivApiException) {
            if (error.code == 400 || error.code == 401) {
                session = refreshSession(session)
                block(session.accessToken)
            } else {
                throw error
            }
        }
    }

    private suspend fun refreshSession(session: AuthSession): AuthSession {
        val refreshToken = session.refreshToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("登录已过期，且没有 refresh token")
        val refreshed = repository.refresh(refreshToken).withFallbackUser(session)
        store.save(refreshed)
        _uiState.update {
            it.copy(
                session = refreshed,
                accessTokenInput = refreshed.accessToken,
                refreshTokenInput = refreshed.refreshToken.orEmpty(),
            )
        }
        return refreshed
    }

    private suspend fun warmupDnsIfNeeded() {
        if (!_uiState.value.useHostIpRouting) return
        if (PixivNetworkConfig.isVpnActive) return
        if (dnsWarmupAttempted) return
        dnsWarmupAttempted = true
        runCatching { repository.refreshDns() }
            .onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = result.summary,
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = result.errors.values.firstOrNull(),
                            ),
                        ),
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = "DNS 更新失败",
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = error.readableMessage(),
                            ),
                        ),
                    )
                }
            }
    }

    private fun shouldRefreshSearchAfterParamChange(): Boolean {
        val state = _uiState.value
        return state.screen == AppScreen.Search &&
            state.keyword.isNotBlank() &&
            state.isSearchActive &&
            !state.isBusy
    }

    private fun markIllustBookmarked(illustId: Long, isBookmarked: Boolean) {
        previewBackStack.replaceAll { it.withBookmarkState(illustId, isBookmarked) }
        _uiState.update { state ->
            state.copy(
                home = state.home.withBookmarkState(illustId, isBookmarked),
                discover = state.discover.withBookmarkState(illustId, isBookmarked),
                mine = state.mine.withBookmarkState(illustId, isBookmarked),
                author = state.author.withBookmarkState(illustId, isBookmarked),
                items = state.items.withBookmarkState(illustId, isBookmarked),
                selectedIllust = state.selectedIllust?.let {
                    if (it.id == illustId) it.withBookmarkState(isBookmarked) else it
                },
                related = state.related.withBookmarkState(illustId, isBookmarked),
            )
        }
    }

    private fun List<Uri>.toUploadParts(): List<UploadImagePart> {
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
                fileName = "image_${index + 1}.$extension",
            )
        }
    }

    private fun AuthSession.withFallbackUser(previous: AuthSession): AuthSession {
        return copy(
            userId = userId ?: previous.userId,
            userName = userName ?: previous.userName,
            userAccount = userAccount ?: previous.userAccount,
            userAvatarUrl = userAvatarUrl ?: previous.userAvatarUrl,
        )
    }

    private fun AuthSession.shouldRefresh(): Boolean {
        val expiresAt = expiresAtMillis ?: return false
        return expiresAt <= System.currentTimeMillis() + 60_000L
    }

    private fun PuxivUiState.toPreviewSnapshot(): PreviewSnapshot {
        return PreviewSnapshot(
            illust = selectedIllust,
            selectedImageIndex = selectedImageIndex,
            related = related.copy(isLoading = false),
            comments = comments.copy(isLoading = false, isSending = false),
            ugoiraFrames = ugoiraFrames,
            ugoiraLoadedFrames = ugoiraLoadedFrames,
            ugoiraTotalFrames = ugoiraTotalFrames,
            returnScreen = previewReturnScreen,
        )
    }

    private fun PuxivUiState.toNavigationEntry(screen: AppScreen): NavigationEntry? {
        return when (screen) {
            AppScreen.Preview -> selectedIllust?.let {
                NavigationEntry(screen = screen, preview = toPreviewSnapshot())
            }
            AppScreen.Author -> author.userId?.let {
                NavigationEntry(screen = screen, author = author.navigationSnapshot())
            }
            else -> NavigationEntry(screen = screen)
        }
    }

    private fun Throwable.readableMessage(): String {
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

    private fun registerNetworkCallback() {
        runCatching {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                networkCallback,
            )
        }
    }

    private fun updateVpnState() {
        PixivNetworkConfig.isVpnActive = isVpnActive()
    }

    private fun isVpnActive(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        return connectivityManager.getNetworkCapabilities(activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    }
}

private fun HomeState.feed(feed: HomeFeed): FeedState {
    return when (feed) {
        HomeFeed.Walkthrough -> walkthrough
        HomeFeed.Recommended -> recommended
        HomeFeed.Ranking -> ranking
        HomeFeed.Latest -> latest
    }
}

private fun HomeState.withFeed(feed: HomeFeed, transform: (FeedState) -> FeedState): HomeState {
    return when (feed) {
        HomeFeed.Walkthrough -> copy(walkthrough = transform(walkthrough))
        HomeFeed.Recommended -> copy(recommended = transform(recommended))
        HomeFeed.Ranking -> copy(ranking = transform(ranking))
        HomeFeed.Latest -> copy(latest = transform(latest))
    }
}

private fun HomeState.withBookmarkState(illustId: Long, isBookmarked: Boolean): HomeState {
    val updatedWalkthrough = walkthrough.withBookmarkState(illustId, isBookmarked)
    val updatedRecommended = recommended.withBookmarkState(illustId, isBookmarked)
    val updatedRanking = ranking.withBookmarkState(illustId, isBookmarked)
    val updatedLatest = latest.withBookmarkState(illustId, isBookmarked)
    if (
        updatedWalkthrough === walkthrough &&
        updatedRecommended === recommended &&
        updatedRanking === ranking &&
        updatedLatest === latest
    ) {
        return this
    }
    return copy(
        walkthrough = updatedWalkthrough,
        recommended = updatedRecommended,
        ranking = updatedRanking,
        latest = updatedLatest,
    )
}

private fun DiscoverState.feed(feed: DiscoverFeed): FeedState {
    return when (feed) {
        DiscoverFeed.Public -> publicWorks
        DiscoverFeed.Private -> privateWorks
    }
}

private fun DiscoverState.withFeed(feed: DiscoverFeed, transform: (FeedState) -> FeedState): DiscoverState {
    return when (feed) {
        DiscoverFeed.Public -> copy(publicWorks = transform(publicWorks))
        DiscoverFeed.Private -> copy(privateWorks = transform(privateWorks))
    }
}

private fun DiscoverState.withBookmarkState(illustId: Long, isBookmarked: Boolean): DiscoverState {
    val updatedPublicWorks = publicWorks.withBookmarkState(illustId, isBookmarked)
    val updatedPrivateWorks = privateWorks.withBookmarkState(illustId, isBookmarked)
    if (updatedPublicWorks === publicWorks && updatedPrivateWorks === privateWorks) return this
    return copy(
        publicWorks = updatedPublicWorks,
        privateWorks = updatedPrivateWorks,
    )
}

private fun MyState.following(feed: FollowUserFeed): UserPreviewFeedState {
    return when (feed) {
        FollowUserFeed.Public -> publicFollowing
        FollowUserFeed.Private -> privateFollowing
    }
}

private fun MyState.withFollowing(
    feed: FollowUserFeed,
    transform: (UserPreviewFeedState) -> UserPreviewFeedState,
): MyState {
    return when (feed) {
        FollowUserFeed.Public -> copy(publicFollowing = transform(publicFollowing))
        FollowUserFeed.Private -> copy(privateFollowing = transform(privateFollowing))
    }
}

private fun MyState.withBookmarkState(illustId: Long, isBookmarked: Boolean): MyState {
    val updatedBookmarks = if (isBookmarked) {
        bookmarks.withBookmarkState(illustId, isBookmarked)
    } else {
        val filtered = bookmarks.items.filterNot { it.id == illustId }
        if (filtered.size == bookmarks.items.size) bookmarks else bookmarks.copy(items = filtered)
    }
    val updatedWorks = works.withBookmarkState(illustId, isBookmarked)
    if (updatedWorks === works && updatedBookmarks === bookmarks) return this
    return copy(
        works = updatedWorks,
        bookmarks = updatedBookmarks,
    )
}

private fun AuthorState.withBookmarkState(illustId: Long, isBookmarked: Boolean): AuthorState {
    val updatedIllusts = illusts.withBookmarkState(illustId, isBookmarked)
    val updatedManga = manga.withBookmarkState(illustId, isBookmarked)
    val updatedBookmarks = bookmarks.withBookmarkState(illustId, isBookmarked)
    if (
        updatedIllusts === illusts &&
        updatedManga === manga &&
        updatedBookmarks === bookmarks
    ) {
        return this
    }
    return copy(
        illusts = updatedIllusts,
        manga = updatedManga,
        bookmarks = updatedBookmarks,
    )
}

private fun AuthorState.feed(tab: AuthorWorkTab): FeedState {
    return when (tab) {
        AuthorWorkTab.Illust -> illusts
        AuthorWorkTab.Manga -> manga
        AuthorWorkTab.Bookmarks -> bookmarks
    }
}

private fun AuthorState.withFeed(tab: AuthorWorkTab, transform: (FeedState) -> FeedState): AuthorState {
    return when (tab) {
        AuthorWorkTab.Illust -> copy(illusts = transform(illusts))
        AuthorWorkTab.Manga -> copy(manga = transform(manga))
        AuthorWorkTab.Bookmarks -> copy(bookmarks = transform(bookmarks))
    }
}

private fun AuthorState.mergeProfile(profile: AuthorProfile): AuthorState {
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
        totalBookmarks = profile.totalBookmarks,
    )
}

private fun AuthorState.navigationSnapshot(): AuthorState {
    return copy(
        isLoadingProfile = false,
        isLoadingWorks = false,
        isFollowBusy = false,
        illusts = illusts.copy(isLoading = false),
        manga = manga.copy(isLoading = false),
        bookmarks = bookmarks.copy(isLoading = false),
    )
}

private fun PuxivUiState.rankingRequestChanged(mode: RankingMode, date: String?): Boolean {
    return rankingMode != mode || rankingDate.apiDateOrNull() != date
}

private fun String.apiDateOrNull(): String? {
    val value = trim().takeIf { it.isNotBlank() } ?: return null
    return value.takeIf { API_DATE_PATTERN.matches(it) }
}

private fun String.apiPositiveIntOrNull(): Int? {
    return trim().toIntOrNull()?.takeIf { it > 0 }
}

private val API_DATE_PATTERN = Regex("""\d{4}-\d{2}-\d{2}""")

private fun FeedState.withBookmarkState(illustId: Long, isBookmarked: Boolean): FeedState {
    val updatedItems = items.withBookmarkState(illustId, isBookmarked)
    if (updatedItems === items) return this
    return copy(items = updatedItems)
}

private fun PreviewSnapshot.withBookmarkState(illustId: Long, isBookmarked: Boolean): PreviewSnapshot {
    val updatedIllust = illust?.let {
        if (it.id == illustId) it.withBookmarkState(isBookmarked) else it
    }
    val updatedRelated = related.withBookmarkState(illustId, isBookmarked)
    if (updatedIllust === illust && updatedRelated === related) return this
    return copy(illust = updatedIllust, related = updatedRelated)
}

private fun List<Illust>.withBookmarkState(illustId: Long, isBookmarked: Boolean): List<Illust> {
    val index = indexOfFirst { it.id == illustId }
    if (index == -1) return this
    val updated = this[index].withBookmarkState(isBookmarked)
    if (updated === this[index]) return this
    return toMutableList().also { it[index] = updated }
}

private fun Illust.withBookmarkState(isBookmarked: Boolean): Illust {
    if (this.isBookmarked == isBookmarked) return this
    val adjustedBookmarks = when {
        isBookmarked && !this.isBookmarked -> totalBookmarks + 1
        !isBookmarked && this.isBookmarked -> (totalBookmarks - 1).coerceAtLeast(0)
        else -> totalBookmarks
    }
    return copy(isBookmarked = isBookmarked, totalBookmarks = adjustedBookmarks)
}

private fun Illust.safeDownloadBaseName(suffix: String): String {
    val cleanTitle = title.ifBlank { "illust_$id" }
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .take(80)
        .trim('_', ' ', '.')
        .ifBlank { "illust_$id" }
    return "${id}_${cleanTitle}_$suffix"
}

private fun Illust.safeFolderName(): String {
    return title.sanitizeFileSegment(maxLength = 80).ifBlank { "illust_$id" }
}

private fun String.sanitizeFileSegment(maxLength: Int = 64): String {
    return trim()
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .replace(Regex("""\s+"""), " ")
        .trim('_', ' ', '.')
        .take(maxLength)
        .ifBlank { "unknown" }
}

private fun buildDownloadRelativePath(
    rootDirectory: String,
    authorName: String,
    title: String? = null,
): String {
    val authorSegment = authorName.sanitizeFileSegment()
    val base = "$rootDirectory/IllustFerry/$authorSegment"
    val titleSegment = title?.sanitizeFileSegment()
    return if (titleSegment.isNullOrBlank()) base else "$base/$titleSegment"
}

private fun List<Illust>.filteredBy(excludedTags: Set<String>): List<Illust> {
    if (excludedTags.isEmpty()) return this
    return filterNot { illust ->
        illust.tags.any { tag ->
            excludedTags.contains(tag.trim().trimStart('#').lowercase(Locale.ROOT))
        }
    }
}

private fun IllustPage.filteredBy(excludedTags: Set<String>): IllustPage {
    if (excludedTags.isEmpty()) return this
    return copy(items = items.filteredBy(excludedTags))
}

private fun UserPreviewPage.filteredBy(excludedTags: Set<String>): UserPreviewPage {
    if (excludedTags.isEmpty()) return this
    return copy(
        items = items.mapNotNull { user ->
            val filteredIllusts = user.illusts.filteredBy(excludedTags)
            if (filteredIllusts.isEmpty() && user.illusts.isNotEmpty()) null else user.copy(illusts = filteredIllusts)
        },
    )
}

private fun normalizeFilteredTagsInput(value: String): String {
    return value.split(',')
        .map { it.trim().trimStart('#') }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }
        .joinToString(",")
}

private fun PuxivThemePalette.messageLabel(): String {
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

private fun PuxivUiState.excludedTags(): Set<String> {
    return filteredTagsInput.split(',')
        .map { it.trim().trimStart('#').lowercase(Locale.ROOT) }
        .filter { it.isNotBlank() }
        .toSet()
}

private fun Illust.previewImageCount(): Int = imageUrls.ifEmpty { listOfNotNull(previewUrl) }.size

private fun String.fileExtension(): String {
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

private fun String.mimeType(): String {
    return when (substringAfterLast('.', "").lowercase(Locale.US)) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "zip" -> "application/zip"
        else -> "image/jpeg"
    }
}
