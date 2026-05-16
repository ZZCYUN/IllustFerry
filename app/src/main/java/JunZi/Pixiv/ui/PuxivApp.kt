package JunZi.Pixiv.ui

import JunZi.Pixiv.AppScreen
import JunZi.Pixiv.AuthorWorkTab
import JunZi.Pixiv.AuthorState
import JunZi.Pixiv.BookmarkFeed
import JunZi.Pixiv.CommentState
import JunZi.Pixiv.DiscoverFeed
import JunZi.Pixiv.DiscoverState
import JunZi.Pixiv.DiagnosticsState
import JunZi.Pixiv.DownloadItem
import JunZi.Pixiv.DownloadStatus
import JunZi.Pixiv.FeedState
import JunZi.Pixiv.FollowUserFeed
import JunZi.Pixiv.HomeFeed
import JunZi.Pixiv.HistoryItem
import JunZi.Pixiv.HistoryState
import JunZi.Pixiv.MyState
import JunZi.Pixiv.PixivViewModel
import JunZi.Pixiv.PreviewSwipeMode
import JunZi.Pixiv.PuxivCustomPalette
import JunZi.Pixiv.PuxivThemeMode
import JunZi.Pixiv.PuxivThemePalette
import JunZi.Pixiv.UgoiraSaveFormat
import JunZi.Pixiv.PuxivUiState
import JunZi.Pixiv.SelectedBookmarkState
import JunZi.Pixiv.UserPreviewFeedState
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.BookmarkTag
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustImagePage
import JunZi.Pixiv.data.model.UserPreview
import JunZi.Pixiv.ui.theme.PuxivTheme
import JunZi.Pixiv.ui.theme.puxivColorScheme
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.network.LocalPixivProxy
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.http.SslCertificate
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executor

private val PuxivSurfaceShape = RoundedCornerShape(8.dp)
private val PuxivControlShape = RoundedCornerShape(6.dp)
private val PuxivImageShape = RoundedCornerShape(6.dp)

private const val PuxivPreviewImageSize = 1600
private const val PuxivCardImageSize = 520
private const val PuxivAvatarImageSize = 96
private const val PuxivTrendImageSize = 360
private const val PuxivFullScreenMaxScale = 5f
private const val PuxivZoomReleaseEpsilon = 0.01f
private const val PuxivZoomedPageTurnThresholdDp = 72f
private val PuxivFullScreenTopBarHeight = 64.dp

private enum class MyTab(val label: String) {
    Works("作品"),
    Bookmarks("收藏"),
    History("历史"),
    Following("关注"),
    Downloads("下载"),
    Upload("投稿"),
}

@Composable
fun PuxivApp(viewModel: PixivViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val transientStateHolder = rememberSaveableStateHolder()
    val mainShellScreen = if (state.screen == AppScreen.Preview) state.previewReturnScreen else state.screen
    val showMainShell = mainShellScreen in setOf(AppScreen.Home, AppScreen.Search, AppScreen.Me, AppScreen.Settings)
    val showTransientScreen = state.screen in setOf(AppScreen.Login, AppScreen.WebLogin, AppScreen.Preview, AppScreen.Author)

    PuxivTheme(
        themeMode = state.themeMode,
        useMaterialYou = state.useMaterialYou,
        palette = state.themePalette,
        customPalette = state.customPalette,
    ) {
        LaunchedEffect(state.message) {
            val message = state.message
            if (message != null) {
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessage()
            }
        }

        BackHandler(enabled = state.screen != AppScreen.Home) {
            if (state.isFullScreenPreview) {
                viewModel.closeFullScreenPreview()
            } else {
                viewModel.goBack()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(Modifier.fillMaxSize()) {
                if (showMainShell) {
                    MainShell(
                        screen = mainShellScreen,
                        home = state.home,
                        keyword = state.keyword,
                        isBusy = state.isBusy,
                        rankingMode = state.rankingMode,
                        searchTarget = state.searchTarget,
                        searchSort = state.searchSort,
                        isTrendingLoading = state.isTrendingLoading,
                        trendingTags = state.trendingTags,
                        discover = state.discover,
                        items = state.items,
                        isSearchActive = state.isSearchActive,
                        isLoadingMore = state.isLoadingMore,
                        nextUrl = state.nextUrl,
                        session = state.session,
                        mine = state.mine,
                        history = state.history,
                        downloads = state.downloads.items,
                        diagnostics = state.home.diagnostics,
                        useHostIpRouting = state.useHostIpRouting,
                        useRemoteImageProxy = state.useRemoteImageProxy,
                        imageProxyInput = state.imageProxyInput,
                        saveUgoiraZip = state.saveUgoiraZip,
                        filteredTagsInput = state.filteredTagsInput,
                        previewSwipeMode = state.previewSwipeMode,
                        ugoiraSaveFormat = state.ugoiraSaveFormat,
                        themeMode = state.themeMode,
                        useMaterialYou = state.useMaterialYou,
                        themePalette = state.themePalette,
                        customPalette = state.customPalette,
                        viewModel = viewModel,
                    )
                }
                if (showTransientScreen) {
                    AnimatedContent(
                        targetState = state.screen,
                        label = "transient-screen",
                    ) { screen ->
                        when (screen) {
                            AppScreen.Login -> LoginScreen(
                                state = state,
                                onAccessTokenChange = viewModel::updateAccessToken,
                                onRefreshTokenChange = viewModel::updateRefreshToken,
                                onAuthCodeChange = viewModel::updateAuthCode,
                                onSaveToken = viewModel::saveManualToken,
                                onStartWebLogin = viewModel::startWebLogin,
                                onExchangeCode = { viewModel.exchangeAuthCode() },
                            )

                            AppScreen.WebLogin -> WebLoginScreen(
                                loginUrl = state.loginUrl,
                                onBack = viewModel::goBack,
                                onCode = { code, useNetworkProxy -> viewModel.exchangeAuthCode(code, useNetworkProxy) },
                            )

                            AppScreen.Preview -> PreviewScreen(
                                state = state,
                                onBack = viewModel::goBack,
                                onSelectImage = viewModel::selectImage,
                                bookmarkTags = state.mine.bookmarkTags(),
                                areBookmarkTagsLoaded = state.mine.hasBookmarkTagsLoaded,
                                selectedBookmark = state.selectedBookmark,
                                onBookmarkPublic = { tags ->
                                    viewModel.bookmarkSelected(
                                        restrict = BookmarkRestrict.Public,
                                        tags = tags,
                                    )
                                },
                                onBookmarkPrivate = { tags ->
                                    viewModel.bookmarkSelected(
                                        restrict = BookmarkRestrict.Private,
                                        tags = tags,
                                    )
                                },
                                onAddBookmarkTags = viewModel::addTagsToSelectedBookmark,
                                onToggleBookmarkTag = viewModel::toggleSelectedBookmarkTag,
                                onDeleteBookmark = viewModel::deleteSelectedBookmark,
                                onLoadBookmarkDetail = { id, force -> viewModel.loadSelectedBookmarkDetail(id, force) },
                                onLoadBookmarkTags = { viewModel.loadMyBookmarkTags(refresh = false) },
                                onLoadRelated = { viewModel.loadRelated(refresh = false) },
                                onOpenPreview = viewModel::openPreview,
                                onOpenFullScreen = viewModel::openFullScreenPreview,
                                onCloseFullScreen = viewModel::closeFullScreenPreview,
                                onTagClick = viewModel::searchTag,
                                onDownload = viewModel::downloadSelectedIllust,
                                onCommentInputChange = viewModel::updateCommentInput,
                                onSendComment = viewModel::sendComment,
                                onOpenAuthor = viewModel::openAuthor,
                            )

                            AppScreen.Author -> transientStateHolder.SaveableStateProvider(
                                key = "author-${state.author.userId ?: 0L}",
                            ) {
                                AuthorScreen(
                                    author = state.author,
                                    onBack = viewModel::goBack,
                                    onRefreshProfile = viewModel::loadAuthorProfile,
                                    onRefreshWorks = { viewModel.loadAuthorWorks(refresh = true) },
                                    onLoadMore = { viewModel.loadAuthorWorks(refresh = false) },
                                    onSelectTab = viewModel::selectAuthorTab,
                                    onFollowPublic = { viewModel.followAuthor(BookmarkRestrict.Public) },
                                    onFollowPrivate = { viewModel.followAuthor(BookmarkRestrict.Private) },
                                    onUnfollow = viewModel::unfollowAuthor,
                                    onOpenPreview = viewModel::openPreview,
                                )
                            }

                            else -> Unit
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun MainShell(
    screen: AppScreen,
    home: JunZi.Pixiv.HomeState,
    keyword: String,
    isBusy: Boolean,
    rankingMode: RankingMode,
    searchTarget: SearchTarget,
    searchSort: SearchSort,
    isTrendingLoading: Boolean,
    trendingTags: List<TrendingTag>,
    discover: DiscoverState,
    items: List<Illust>,
    isSearchActive: Boolean,
    isLoadingMore: Boolean,
    nextUrl: String?,
    session: AuthSession?,
    mine: MyState,
    history: HistoryState,
    downloads: List<DownloadItem>,
    diagnostics: DiagnosticsState,
    useHostIpRouting: Boolean,
    useRemoteImageProxy: Boolean,
    imageProxyInput: String,
    saveUgoiraZip: Boolean,
    filteredTagsInput: String,
    previewSwipeMode: PreviewSwipeMode,
    ugoiraSaveFormat: UgoiraSaveFormat,
    themeMode: PuxivThemeMode,
    useMaterialYou: Boolean,
    themePalette: PuxivThemePalette,
    customPalette: PuxivCustomPalette,
    viewModel: PixivViewModel,
) {
    val mainScreens = remember { listOf(AppScreen.Home, AppScreen.Search, AppScreen.Me) }
    val pagerState = rememberPagerState(
        initialPage = mainScreens.indexOf(screen).takeIf { it >= 0 } ?: 0,
    ) { mainScreens.size }
    val coroutineScope = rememberCoroutineScope()
    val currentScreen by rememberUpdatedState(screen)
    var requestedPage by remember {
        mutableIntStateOf(mainScreens.indexOf(screen).takeIf { it >= 0 } ?: 0)
    }
    val selectedMainScreen = if (screen == AppScreen.Settings) {
        AppScreen.Me
    } else {
        mainScreens.getOrNull(requestedPage) ?: AppScreen.Home
    }

    LaunchedEffect(screen) {
        val target = mainScreens.indexOf(
            if (screen == AppScreen.Settings) AppScreen.Me else screen,
        )
        if (target >= 0) {
            val wasRequested = requestedPage
            requestedPage = target
            if (wasRequested != target && target != pagerState.currentPage) {
                pagerState.animateScrollToPage(target)
            }
        }
    }
    LaunchedEffect(pagerState) {
        var hasInitialPage = false
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                if (!hasInitialPage) {
                    hasInitialPage = true
                    return@collect
                }
                if (mainScreens.getOrNull(page) == currentScreen) {
                    mainScreens.getOrNull(page)?.let(viewModel::ensureMainPageLoaded)
                    return@collect
                }
                val screen = mainScreens.getOrNull(page) ?: return@collect
                viewModel.showMainPage(screen)
                viewModel.ensureMainPageLoaded(screen)
        }
    }
    fun animateToMainPage(index: Int) {
        requestedPage = index
        val screen = mainScreens[index]
        if (pagerState.currentPage == index) {
            if (currentScreen != screen) {
                viewModel.showMainPage(screen)
            }
            viewModel.ensureMainPageLoaded(screen)
            return
        }
        coroutineScope.launch {
            pagerState.animateScrollToPage(index)
        }
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                current = selectedMainScreen,
                onHome = { animateToMainPage(0) },
                onSearch = { animateToMainPage(1) },
                onMe = { animateToMainPage(2) },
            )
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 2,
                userScrollEnabled = false,
                key = { index -> mainScreens[index].name },
            ) { page ->
                when (mainScreens[page]) {
                    AppScreen.Home -> HomeScreen(
                        session = session,
                        home = home,
                        keyword = keyword,
                        isBusy = isBusy,
                        rankingMode = rankingMode,
                        contentPadding = padding,
                        onKeywordChange = viewModel::updateKeyword,
                        onSubmitSearch = viewModel::submitHomeSearch,
                        onLoadHome = { viewModel.loadHome(refresh = true) },
                        onLoadFeed = viewModel::loadHomeFeed,
                        onRankingModeChange = viewModel::updateRankingMode,
                        onOpenPreview = viewModel::openPreview,
                    )

                    AppScreen.Search -> SearchScreen(
                        keyword = keyword,
                        isBusy = isBusy,
                        searchTarget = searchTarget,
                        searchSort = searchSort,
                        isTrendingLoading = isTrendingLoading,
                        trendingTags = trendingTags,
                        discover = discover,
                        items = items,
                        isSearchActive = isSearchActive,
                        isLoadingMore = isLoadingMore,
                        nextUrl = nextUrl,
                        contentPadding = padding,
                        onKeywordChange = viewModel::updateKeyword,
                        onSearch = viewModel::search,
                        onLoadMore = viewModel::loadMore,
                        onSearchSortChange = viewModel::updateSearchSort,
                        onSearchTargetChange = viewModel::updateSearchTarget,
                        onTrendingTagClick = viewModel::searchTrendingTag,
                        onReturnToDiscover = viewModel::returnToDiscover,
                        onOpenPreview = viewModel::openPreview,
                        onRefreshDiscover = { viewModel.loadDiscover(refresh = true) },
                        onLoadMoreDiscover = viewModel::loadDiscoverFeed,
                    )

                    AppScreen.Me -> MeScreen(
                        isActive = selectedMainScreen == AppScreen.Me,
                        session = session,
                        mine = mine,
                        history = history,
                        downloads = downloads,
                        contentPadding = padding,
                        onLoadMine = { viewModel.loadMine(refresh = true) },
                        onLoadWorks = { viewModel.loadMyWorks(refresh = true) },
                        onLoadBookmarks = { feed, tag -> viewModel.loadMyBookmarks(feed, tag, refresh = true) },
                        onLoadBookmarkTags = { viewModel.loadMyBookmarkTags(refresh = true) },
                        onLoadHistory = { viewModel.loadHistory(refresh = true) },
                        onLoadMoreWorks = { viewModel.loadMyWorks(refresh = false) },
                        onLoadMoreBookmarks = { feed, tag -> viewModel.loadMyBookmarks(feed, tag, refresh = false) },
                        onLoadMoreHistory = viewModel::loadMoreHistory,
                        onLoadFollowing = { viewModel.loadMyFollowing(it, refresh = true) },
                        onLoadMoreFollowing = { viewModel.loadMyFollowing(it, refresh = false) },
                        onClearHistory = viewModel::clearHistory,
                        onDeleteHistory = viewModel::deleteHistoryItem,
                        onDeleteDownload = viewModel::deleteDownloadItem,
                        onOpenPreview = viewModel::openPreview,
                        onOpenAuthor = viewModel::openAuthor,
                        onUploadIllust = viewModel::uploadIllust,
                        onOpenDownloadPreview = viewModel::openDownloadedPreview,
                        onOpenSettings = viewModel::openSettings,
                        onLogout = viewModel::logout,
                        onOpenLogin = viewModel::openLoginScreen,
                        onStartWebLogin = viewModel::startWebLogin,
                    )

                    else -> Unit
                }
            }

            if (screen == AppScreen.Settings) {
                SettingsScreen(
                    diagnostics = diagnostics,
                    useHostIpRouting = useHostIpRouting,
                    useRemoteImageProxy = useRemoteImageProxy,
                    imageProxyInput = imageProxyInput,
                    saveUgoiraZip = saveUgoiraZip,
                    filteredTagsInput = filteredTagsInput,
                    previewSwipeMode = previewSwipeMode,
                    ugoiraSaveFormat = ugoiraSaveFormat,
                    themeMode = themeMode,
                    useMaterialYou = useMaterialYou,
                    themePalette = themePalette,
                    customPalette = customPalette,
                    contentPadding = padding,
                    onRefreshDns = { viewModel.refreshDns(showMessage = true) },
                    onDiagnostics = viewModel::runDiagnostics,
                    onHostIpRoutingEnabledChange = viewModel::updateHostIpRoutingEnabled,
                    onPreviewSwipeModeChange = viewModel::updatePreviewSwipeMode,
                    onThemeModeChange = viewModel::updateThemeMode,
                    onMaterialYouEnabledChange = viewModel::updateMaterialYouEnabled,
                    onThemePaletteChange = viewModel::updateThemePalette,
                    onCustomThemePaletteChange = viewModel::updateCustomThemePalette,
                    onImageProxyEnabledChange = viewModel::updateImageProxyEnabled,
                    onImageProxyInputChange = viewModel::updateImageProxyInput,
                    onSaveImageProxy = viewModel::saveImageProxyOrigin,
                    onResetImageProxy = viewModel::resetImageProxyOrigin,
                    onSaveUgoiraZipChange = viewModel::updateSaveUgoiraZip,
                    onFilteredTagsInputChange = viewModel::updateFilteredTagsInput,
                    onSaveFilteredTags = viewModel::saveFilteredTags,
                    onUgoiraSaveFormatChange = viewModel::updateUgoiraSaveFormat,
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    current: AppScreen,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onMe: () -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = current == AppScreen.Home,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("首页") },
        )
        NavigationBarItem(
            selected = current == AppScreen.Search,
            onClick = onSearch,
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("发现") },
        )
        NavigationBarItem(
            selected = current == AppScreen.Me || current == AppScreen.Settings,
            onClick = onMe,
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("我的") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    session: AuthSession?,
    home: JunZi.Pixiv.HomeState,
    keyword: String,
    isBusy: Boolean,
    rankingMode: RankingMode,
    contentPadding: PaddingValues,
    onKeywordChange: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onLoadHome: () -> Unit,
    onLoadFeed: (HomeFeed, Boolean) -> Unit,
    onRankingModeChange: (RankingMode) -> Unit,
    onOpenPreview: (Illust) -> Unit,
) {
    val recommendedItems = home.recommended.items
    val rankingItems = home.ranking.items
    val latestItems = home.latest.items
    val isAnonymous = session == null
    val primaryFeed = if (isAnonymous) home.walkthrough else home.recommended
    val visibleItems = if (isAnonymous) {
        home.walkthrough.items
    } else {
        recommendedItems + rankingItems + latestItems
    }
    val gridState = rememberLazyStaggeredGridState()
    val coroutineScope = rememberCoroutineScope()
    val decorativeFeedSemantics = rememberDecorativeFeedSemantics()
    val totalWorks by remember(visibleItems) {
        derivedStateOf {
            visibleItems.size
        }
    }
    val homeTrendTags by remember(visibleItems) {
        derivedStateOf {
            visibleItems
                .asSequence()
                .flatMap { it.tags.asSequence() }
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(12)
                .toList()
        }
    }
    val recommendedHeaderIndex = 2 + if (homeTrendTags.isNotEmpty()) 1 else 0
    val rankingModesIndex = recommendedHeaderIndex + homeFeedSectionItemCount(home.recommended)
    val rankingHeaderIndex = rankingModesIndex + 1

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("IllustFerry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$totalWorks 件作品",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .feedSemanticsBoundary(decorativeFeedSemantics),
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(150.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
            ) {
                if (!isAnonymous) {
                    item(
                        key = "home-search",
                        contentType = "home-search",
                        span = StaggeredGridItemSpan.FullLine,
                    ) {
                        HomeSearchBar(
                            keyword = keyword,
                            isBusy = isBusy,
                            onKeywordChange = onKeywordChange,
                            onSubmitSearch = onSubmitSearch,
                        )
                    }
                    item(
                        key = "home-entry-row",
                        contentType = "home-entry-row",
                        span = StaggeredGridItemSpan.FullLine,
                    ) {
                        HomeEntryRow(
                            rankingLabel = rankingMode.label,
                            onRecommended = {
                                coroutineScope.launch {
                                    gridState.animateScrollToItem(recommendedHeaderIndex)
                                }
                                onLoadFeed(HomeFeed.Recommended, true)
                            },
                            onRanking = {
                                coroutineScope.launch {
                                    gridState.animateScrollToItem(rankingHeaderIndex)
                                }
                                onLoadFeed(HomeFeed.Ranking, true)
                            },
                        )
                    }
                }
                if (!isAnonymous && homeTrendTags.isNotEmpty()) {
                    item(
                        key = "home-trend-tags",
                        contentType = "home-trend-tags",
                        span = StaggeredGridItemSpan.FullLine,
                    ) {
                        HomeTrendChips(
                            tags = homeTrendTags,
                            onTagClick = { tag ->
                                onKeywordChange(tag)
                                onSubmitSearch()
                            },
                        )
                    }
                }
                homeFeedSection(
                    title = if (isAnonymous) "浏览" else "推荐",
                    feed = if (isAnonymous) HomeFeed.Walkthrough else HomeFeed.Recommended,
                    feedState = primaryFeed,
                    clearItemSemantics = decorativeFeedSemantics,
                    onLoadFeed = onLoadFeed,
                    onOpenPreview = onOpenPreview,
                )
                if (!isAnonymous) {
                    item(
                        key = "home-ranking-modes",
                        contentType = "home-ranking-modes",
                        span = StaggeredGridItemSpan.FullLine,
                    ) {
                        RankingModeChips(
                            selected = rankingMode,
                            onSelect = onRankingModeChange,
                        )
                    }
                    homeFeedSection(
                        title = "排行 · ${rankingMode.label}",
                        feed = HomeFeed.Ranking,
                        feedState = home.ranking,
                        clearItemSemantics = decorativeFeedSemantics,
                        onLoadFeed = onLoadFeed,
                        onOpenPreview = onOpenPreview,
                    )
                    homeFeedSection(
                        title = "最新",
                        feed = HomeFeed.Latest,
                        feedState = home.latest,
                        clearItemSemantics = decorativeFeedSemantics,
                        onLoadFeed = onLoadFeed,
                        onOpenPreview = onOpenPreview,
                    )
                }
                item(
                    key = "home-refresh",
                    contentType = "home-refresh",
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FilledTonalButton(
                            onClick = {
                                coroutineScope.launch {
                                    gridState.animateScrollToItem(0)
                                }
                                if (isAnonymous) {
                                    onLoadFeed(HomeFeed.Walkthrough, true)
                                } else {
                                    onLoadHome()
                                }
                            },
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("刷新首页")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSearchBar(
    keyword: String,
    isBusy: Boolean,
    onKeywordChange: (String) -> Unit,
    onSubmitSearch: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixivSearchField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                placeholder = "搜索作品、标签、画师",
                onSearch = onSubmitSearch,
            )
            IconButton(
                onClick = onSubmitSearch,
                enabled = !isBusy,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PixivSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeEntryRow(
    rankingLabel: String,
    onRecommended: () -> Unit,
    onRanking: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HomeEntryButton(
            title = "推荐",
            subtitle = "查看推荐",
            icon = Icons.Default.Image,
            onClick = onRecommended,
            modifier = Modifier.weight(1f),
        )
        HomeEntryButton(
            title = "排行",
            subtitle = rankingLabel,
            icon = Icons.Default.GridView,
            onClick = onRanking,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeEntryButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeTrendChips(
    tags: List<String>,
    onTagClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = "趋势标签",
            count = tags.size,
            isLoading = false,
            onRefresh = {},
            showRefresh = false,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(end = 2.dp),
        ) {
            lazyRowItems(tags, key = { it }) { tag ->
                AssistChip(
                    onClick = { onTagClick(tag) },
                    label = {
                        Text(
                            text = "#$tag",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun FeaturedIllustCard(
    illust: Illust,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(132.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
    ) {
        Box {
            GlideImage(
                url = illust.previewUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp),
                crop = true,
                requestSize = PuxivCardImageSize,
            )
            ImageScrim()
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OverlayChip(illust.typeLabel)
                if (illust.pageCount > 1) {
                    OverlayChip("${illust.pageCount}P")
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = illust.title.ifBlank { "#${illust.id}" },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = illust.authorName.ifBlank { "Unknown artist" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RankingModeChips(
    selected: RankingMode,
    onSelect: (RankingMode) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "排行榜",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RankingMode.values().forEach { mode ->
                    FilterChip(
                        selected = selected == mode,
                        onClick = { onSelect(mode) },
                        label = { Text(mode.label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayChip(text: String) {
    Surface(
        modifier = Modifier.clearAndSetSemantics {},
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.58f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BoxScope.ImageScrim() {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clearAndSetSemantics {}
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.58f to Color.Black.copy(alpha = 0.18f),
                    1f to Color.Black.copy(alpha = 0.68f),
                ),
            ),
    )
}

@Composable
private fun MetadataPill(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusDot(
    active: Boolean,
    running: Boolean,
) {
    val color = when {
        running -> MaterialTheme.colorScheme.tertiary
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    Surface(
        modifier = Modifier.size(10.dp),
        shape = RoundedCornerShape(50),
        color = color,
        content = {},
    )
}

@Composable
private fun DiagnosticsPanel(
    diagnostics: DiagnosticsState,
    onRefreshDns: () -> Unit,
    onDiagnostics: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusDot(
                    active = diagnostics.apiStatus.contains("可用") || diagnostics.imageStatus.contains("可用"),
                    running = diagnostics.isRunning,
                )
                Column(Modifier.weight(1f)) {
                    Text("连接状态", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = diagnostics.lastDnsResult ?: "DNS 未更新",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起" else "详情")
                }
                IconButton(onClick = onDiagnostics, enabled = !diagnostics.isRunning) {
                    if (diagnostics.isRunning) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "检测网络")
                    }
                }
            }
            if (diagnostics.isRunning) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MetadataPill("API ${diagnostics.apiStatus}")
                MetadataPill("图片 ${diagnostics.imageStatus}")
                TextButton(
                    onClick = onRefreshDns,
                    enabled = !diagnostics.isRunning,
                    modifier = Modifier.height(32.dp),
                ) {
                    Text("刷新 DNS")
                }
            }
            diagnostics.lastError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                )
            }
            if (expanded) {
                SelectionContainer {
                    Text(
                        text = diagnostics.hostSnapshot.entries.joinToString("\n") { "${it.key} = ${it.value}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun LazyStaggeredGridScope.homeFeedSection(
    title: String,
    feed: HomeFeed,
    feedState: FeedState,
    clearItemSemantics: Boolean,
    onLoadFeed: (HomeFeed, Boolean) -> Unit,
    onOpenPreview: (Illust) -> Unit,
) {
    item(
        key = "${feed.name}-header",
        contentType = "section-header",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        SectionHeader(
            title = title,
            count = feedState.items.size,
            isLoading = feedState.isLoading,
            onRefresh = { onLoadFeed(feed, true) },
        )
    }
    if (feedState.error != null) {
        item(
            key = "${feed.name}-error",
            contentType = "feed-error",
            span = StaggeredGridItemSpan.FullLine,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Text(
                    text = feedState.error,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (feedState.items.isEmpty() && feedState.isLoading) {
        item(
            key = "${feed.name}-loading",
            contentType = "feed-loading",
            span = StaggeredGridItemSpan.FullLine,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
    }
    items(
        feedState.items,
        key = { "${feed.name}-${it.id}" },
        contentType = { "illust-card" },
    ) { illust ->
        IllustCard(
            illust = illust,
            clearSemantics = clearItemSemantics,
            onClick = onOpenPreview,
        )
    }
    item(
        key = "${feed.name}-footer",
        contentType = "feed-footer",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                feedState.isLoading && feedState.items.isNotEmpty() ->
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)

                feedState.nextUrl != null ->
                    TextButton(onClick = { onLoadFeed(feed, false) }) { Text("加载更多$title") }
            }
        }
    }
}

private fun homeFeedSectionItemCount(feedState: FeedState): Int {
    var count = 2
    if (feedState.error != null) count += 1
    if (feedState.items.isEmpty() && feedState.isLoading) count += 1
    count += feedState.items.size
    return count
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    countLabel: String? = "$count works",
    isLoading: Boolean,
    onRefresh: () -> Unit,
    actionLabel: String? = null,
    showRefresh: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            countLabel?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (actionLabel != null) {
            Surface(
                onClick = onRefresh,
                enabled = !isLoading,
                shape = PuxivControlShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        if (showRefresh) {
            Surface(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.size(36.dp),
                shape = PuxivControlShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新$title", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    state: PuxivUiState,
    onAccessTokenChange: (String) -> Unit,
    onRefreshTokenChange: (String) -> Unit,
    onAuthCodeChange: (String) -> Unit,
    onSaveToken: () -> Unit,
    onStartWebLogin: () -> Unit,
    onExchangeCode: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("P", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    }
                }
                Column {
                    Text(
                        text = "IllustFerry",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "面向 pixiv 的非官方插画客户端",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("令牌登录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedTextField(
                        value = state.accessTokenInput,
                        onValueChange = onAccessTokenChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Access token") },
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2,
                        maxLines = 4,
                    )
                    OutlinedTextField(
                        value = state.refreshTokenInput,
                        onValueChange = onRefreshTokenChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Refresh token") },
                        shape = RoundedCornerShape(8.dp),
                        minLines = 1,
                        maxLines = 3,
                    )
                    Button(
                        onClick = onSaveToken,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isBusy,
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("保存令牌登录")
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "OAuth code 登录",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    FilledTonalButton(
                        onClick = onStartWebLogin,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isBusy,
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("打开网页登录")
                    }
                    OutlinedTextField(
                        value = state.authCodeInput,
                        onValueChange = onAuthCodeChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("回调 code") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                    )
                    Button(
                        onClick = onExchangeCode,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isBusy,
                    ) {
                        if (state.isBusy) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("兑换并登录")
                    }
                }
            }

            if (state.loginUrl.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    SelectionContainer {
                        Text(
                            text = state.loginUrl,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeScreen(
    isActive: Boolean,
    session: AuthSession?,
    mine: MyState,
    history: HistoryState,
    downloads: List<DownloadItem>,
    contentPadding: PaddingValues,
    onLoadMine: () -> Unit,
    onLoadWorks: () -> Unit,
    onLoadBookmarks: (BookmarkFeed, String?) -> Unit,
    onLoadBookmarkTags: () -> Unit,
    onLoadHistory: () -> Unit,
    onLoadMoreWorks: () -> Unit,
    onLoadMoreBookmarks: (BookmarkFeed, String?) -> Unit,
    onLoadMoreHistory: () -> Unit,
    onLoadFollowing: (FollowUserFeed) -> Unit,
    onLoadMoreFollowing: (FollowUserFeed) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteHistory: (Long) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onOpenAuthor: (UserPreview) -> Unit,
    onUploadIllust: (String, String, List<String>, String, Int, String, Boolean, Int, List<Uri>) -> Unit,
    onOpenDownloadPreview: (DownloadItem) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onOpenLogin: () -> Unit,
    onStartWebLogin: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(MyTab.Works) }
    var selectedBookmarkFeed by remember { mutableStateOf(BookmarkFeed.Public) }
    var selectedBookmarkTag by remember { mutableStateOf<String?>(null) }
    var selectedFollowingFeed by remember { mutableStateOf(FollowUserFeed.Public) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var pendingDeleteHistory by remember { mutableStateOf<HistoryItem?>(null) }
    var pendingDeleteDownload by remember { mutableStateOf<DownloadItem?>(null) }
    val gridState = rememberLazyStaggeredGridState()
    val decorativeFeedSemantics = rememberDecorativeFeedSemantics()

    LaunchedEffect(isActive, session?.userId, mine.hasLoaded) {
        if (isActive && session?.userId != null && !mine.hasLoaded) {
            onLoadMine()
        }
    }

    val visibleBookmarkTags = mine.bookmarkTags(selectedBookmarkFeed).map { it.name }
    LaunchedEffect(visibleBookmarkTags, selectedBookmarkFeed) {
        val selectedTag = selectedBookmarkTag
        if (selectedTag != null && visibleBookmarkTags.none { it.equals(selectedTag, ignoreCase = true) }) {
            selectedBookmarkTag = null
            if (selectedTab == MyTab.Bookmarks && session != null) {
                onLoadBookmarks(selectedBookmarkFeed, null)
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("我的")
                        Text(
                            text = session?.userName?.takeIf { it.isNotBlank() } ?: "未登录",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                actions = {},
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .feedSemanticsBoundary(decorativeFeedSemantics),
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(154.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
            ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                MyHeader(
                    session = session,
                    followerCount = mine.followerCount,
                    hasMoreFollowers = mine.hasMoreFollowers,
                    downloadsCount = downloads.size,
                    isUploading = mine.isUploading,
                    onRefresh = onLoadMine,
                    onSettings = onOpenSettings,
                    onLogout = onLogout,
                    onLoginClick = { showLoginDialog = true },
                )
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                MyTabRow(
                    selected = selectedTab,
                    onSelect = { tab ->
                        val requiresLogin = tab == MyTab.Works ||
                            tab == MyTab.Bookmarks ||
                            tab == MyTab.Following ||
                            tab == MyTab.Upload
                        if (requiresLogin && session == null) {
                            showLoginDialog = true
                            return@MyTabRow
                        }
                        selectedTab = tab
                        when (tab) {
                            MyTab.Works -> if (mine.works.items.isEmpty()) onLoadWorks()
                            MyTab.Bookmarks -> {
                                if (
                                    !mine.hasBookmarkTagsLoaded &&
                                    !mine.isBookmarkTagsLoading
                                ) {
                                    onLoadBookmarkTags()
                                }
                                val feed = mine.bookmarkFeed(selectedBookmarkFeed)
                                if (feed.items.isEmpty() || feed.queryTag != selectedBookmarkTag) {
                                    onLoadBookmarks(selectedBookmarkFeed, selectedBookmarkTag)
                                }
                            }
                            MyTab.History -> if (history.items.isEmpty()) onLoadHistory()
                            MyTab.Following -> {
                                if (mine.followingFeed(selectedFollowingFeed).items.isEmpty()) {
                                    onLoadFollowing(selectedFollowingFeed)
                                }
                            }
                            MyTab.Downloads -> Unit
                            MyTab.Upload -> Unit
                        }
                    },
                )
            }

            when (selectedTab) {
                MyTab.Works -> {
                    val feed = mine.works
                    mineFeedMessages(feed, "还没有加载到发布作品")
                    items(
                        feed.items,
                        key = { "mine-work-${it.id}" },
                        contentType = { "illust-card" },
                    ) { illust ->
                        IllustCard(
                            illust = illust,
                            clearSemantics = decorativeFeedSemantics,
                            onClick = onOpenPreview,
                        )
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        MinePagingFooter(feed = feed, onLoadMore = onLoadMoreWorks)
                    }
                }

                MyTab.Bookmarks -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        BookmarkCollectionPanel(
                            mine = mine,
                            selectedFeed = selectedBookmarkFeed,
                            selectedTag = selectedBookmarkTag,
                            onSelectFeed = { feed ->
                                selectedBookmarkFeed = feed
                                if (
                                    !mine.hasBookmarkTagsLoaded &&
                                    !mine.isBookmarkTagsLoading
                                ) {
                                    onLoadBookmarkTags()
                                }
                                onLoadBookmarks(feed, selectedBookmarkTag)
                            },
                            onSelectTag = { tag ->
                                selectedBookmarkTag = tag
                                onLoadBookmarks(selectedBookmarkFeed, tag)
                            },
                            onRefreshTags = onLoadBookmarkTags,
                        )
                    }
                    val feed = mine.bookmarkFeed(selectedBookmarkFeed)
                    mineFeedMessages(
                        feed = feed,
                        emptyText = if (selectedBookmarkFeed == BookmarkFeed.Private) {
                            "还没有私密收藏"
                        } else {
                            "还没有收藏作品"
                        },
                    )
                    items(
                        feed.items,
                        key = { "mine-bookmark-${selectedBookmarkFeed.name}-${selectedBookmarkTag.orEmpty()}-${it.id}" },
                        contentType = { "illust-card" },
                    ) { illust ->
                        IllustCard(
                            illust = illust,
                            clearSemantics = false,
                            onClick = onOpenPreview,
                        )
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        MinePagingFooter(
                            feed = feed,
                            onLoadMore = { onLoadMoreBookmarks(selectedBookmarkFeed, selectedBookmarkTag) },
                        )
                    }
                }

                MyTab.Upload -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        UploadIllustPanel(
                            isUploading = mine.isUploading,
                            uploadStatus = mine.uploadStatus,
                            onUploadIllust = onUploadIllust,
                        )
                    }
                }

                MyTab.History -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SectionHeader(
                            title = "最近浏览",
                            count = history.items.size,
                            isLoading = history.isLoading,
                            onRefresh = onLoadHistory,
                            actionLabel = null,
                            showRefresh = true,
                        )
                    }
                    if (history.items.isEmpty() && history.isLoading) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (!history.error.isNullOrBlank()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ) {
                                Text(
                                    text = history.error,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    } else if (history.items.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            EmptyStateCard(
                                text = "还没有浏览历史",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(onClick = onClearHistory) {
                                    Text("清空历史")
                                }
                            }
                        }
                        items(
                            history.items,
                            key = { "history-${it.illust.id}-${it.viewedAtMillis}" },
                            contentType = { "history-card" },
                        ) { entry ->
                            HistoryIllustCard(
                                entry = entry,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onOpenPreview(entry.illust) },
                                onDelete = { pendingDeleteHistory = entry },
                            )
                        }
                        if (history.hasMore || history.isLoading) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                PagingFooter(
                                    isLoadingMore = history.isLoading,
                                    nextUrl = if (history.hasMore) "history" else null,
                                    hasItems = history.items.isNotEmpty(),
                                    onLoadMore = onLoadMoreHistory,
                                )
                            }
                        }
                    }
                }

                MyTab.Following -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        FollowingUsersPanel(
                            publicFollowing = mine.publicFollowing,
                            privateFollowing = mine.privateFollowing,
                            selectedFeed = selectedFollowingFeed,
                            onSelectFeed = { feed ->
                                selectedFollowingFeed = feed
                                if (mine.followingFeed(feed).items.isEmpty()) onLoadFollowing(feed)
                            },
                            onRefresh = onLoadFollowing,
                            onLoadMore = onLoadMoreFollowing,
                            onOpenAuthor = onOpenAuthor,
                        )
                    }
                }

                MyTab.Downloads -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        DownloadsPanel(
                            downloads = downloads,
                            onOpenPreview = onOpenDownloadPreview,
                            onDelete = { pendingDeleteDownload = it },
                        )
                    }
                }
            }
            }

            if (showLoginDialog) {
                AlertDialog(
                    onDismissRequest = { showLoginDialog = false },
                    title = { Text("登录") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLoginDialog = false
                            onOpenLogin()
                        }) {
                            Text("使用Token登录")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showLoginDialog = false
                            onStartWebLogin()
                        }) {
                            Text("使用网页登录")
                        }
                    },
                )
            }

            pendingDeleteHistory?.let { entry ->
                AlertDialog(
                    onDismissRequest = { pendingDeleteHistory = null },
                    title = { Text("删除历史记录") },
                    text = {
                        Text(
                            "确认删除《${entry.illust.title.ifBlank { "#${entry.illust.id}" }}》这条浏览历史吗？",
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDeleteHistory(entry.illust.id)
                                pendingDeleteHistory = null
                            },
                        ) {
                            Text("删除")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDeleteHistory = null }) {
                            Text("取消")
                        }
                    },
                )
            }

            pendingDeleteDownload?.let { item ->
                AlertDialog(
                    onDismissRequest = { pendingDeleteDownload = null },
                    title = { Text("删除下载记录") },
                    text = {
                        Text(
                            "确认删除《${item.title.ifBlank { item.fileName }}》这条下载记录吗？",
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDeleteDownload(item.key)
                                pendingDeleteDownload = null
                            },
                        ) {
                            Text("删除")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDeleteDownload = null }) {
                            Text("取消")
                        }
                    },
                )
            }
        }
    }
}

private fun LazyStaggeredGridScope.mineFeedMessages(
    feed: FeedState,
    emptyText: String,
) {
    if (feed.error != null) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Text(
                    text = feed.error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (feed.items.isEmpty()) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (feed.isLoading) {
                        CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyHeader(
    session: AuthSession?,
    followerCount: Int,
    hasMoreFollowers: Boolean,
    downloadsCount: Int,
    isUploading: Boolean,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (session == null) Modifier.clickable(onClick = onLoginClick) else Modifier),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlideImage(
                    url = session?.userAvatarUrl,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    crop = true,
                    requestSize = PuxivAvatarImageSize,
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = session?.userName?.takeIf { it.isNotBlank() } ?: "未登录",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = session?.let { account ->
                            buildString {
                                account.userAccount?.takeIf { it.isNotBlank() }?.let { append("@").append(it) }
                                account.userId?.let {
                                    if (isNotEmpty()) append(" · ")
                                    append("ID ").append(it)
                                }
                                if (isEmpty()) append("当前会话未保存用户 ID")
                            }
                        } ?: "点击登录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetadataPill("作品")
                MetadataPill("收藏")
                MetadataPill("关注")
                MetadataPill("${followerCount}${if (hasMoreFollowers) "+" else ""} 粉丝")
                MetadataPill("$downloadsCount 下载")
                if (isUploading) MetadataPill("投稿中")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = if (session == null) onLoginClick else onRefresh,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Icon(
                        if (session == null) Icons.AutoMirrored.Filled.Login else Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (session == null) "登录" else "刷新")
                }
                FilledTonalButton(
                    onClick = onSettings,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("设置")
                }
                if (session != null) {
                    FilledTonalButton(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("退出")
                    }
                }
            }
        }
    }
}

@Composable
private fun MyTabRow(
    selected: MyTab,
    onSelect: (MyTab) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MyTab.entries.forEach { tab ->
                FilterChip(
                    selected = selected == tab,
                    onClick = { onSelect(tab) },
                    label = { Text(tab.label) },
                    leadingIcon = {
                        val icon = when (tab) {
                            MyTab.Works -> Icons.Default.Image
                            MyTab.Bookmarks -> Icons.Default.CollectionsBookmark
                            MyTab.History -> Icons.Default.History
                            MyTab.Following -> Icons.Default.AccountCircle
                            MyTab.Downloads -> Icons.Default.Download
                            MyTab.Upload -> Icons.Default.CloudUpload
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )
            }
        }
    }
}

@Composable
private fun BookmarkCollectionPanel(
    mine: MyState,
    selectedFeed: BookmarkFeed,
    selectedTag: String?,
    onSelectFeed: (BookmarkFeed) -> Unit,
    onSelectTag: (String?) -> Unit,
    onRefreshTags: () -> Unit,
) {
    val currentFeed = mine.bookmarkFeed(selectedFeed)
    val accountTags = mine.bookmarkTags(selectedFeed)
    val filterTags = accountTags.map { it.name }
        .distinctBy { it.lowercase() }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CollectionsBookmark,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "收藏",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = selectedTag?.let { "#$it" } ?: "全部标签",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (currentFeed.isLoading || mine.isBookmarkTagsLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                }
                IconButton(
                    onClick = onRefreshTags,
                    enabled = !mine.isBookmarkTagsLoading,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新收藏标签", modifier = Modifier.size(18.dp))
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BookmarkFeed.entries.forEach { feed ->
                    FilterChip(
                        selected = selectedFeed == feed,
                        onClick = { onSelectFeed(feed) },
                        label = { Text(feed.label) },
                        leadingIcon = {
                            Icon(feed.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                }
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item(key = "all-bookmark-tags") {
                    BookmarkTagChip(
                        tag = "全部标签",
                        isAll = true,
                        isAccountTag = true,
                        selected = selectedTag == null,
                        onClick = { onSelectTag(null) },
                        onRemove = null,
                    )
                }
                lazyRowItems(filterTags, key = { it }) { tag ->
                    BookmarkTagChip(
                        tag = tag,
                        isAll = false,
                        isAccountTag = true,
                        selected = selectedTag?.equals(tag, ignoreCase = true) == true,
                        onClick = { onSelectTag(tag) },
                        onRemove = null,
                    )
                }
            }
            mine.bookmarkTagsError?.takeIf { it.isNotBlank() }?.let { error ->
                Text(
                    text = "账号标签加载失败：$error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun BookmarkTagChip(
    tag: String,
    isAll: Boolean,
    isAccountTag: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    Surface(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 9.dp, end = if (!isAccountTag && onRemove != null) 3.dp else 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (!isAll) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(
                text = if (isAll) tag else "#$tag",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!isAccountTag && onRemove != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "移除收藏标签",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onRemove)
                        .padding(3.dp),
                )
            }
        }
    }
}

@Composable
private fun MinePagingFooter(
    feed: FeedState,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            feed.isLoading -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            feed.nextUrl != null -> FilledTonalButton(onClick = onLoadMore) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("加载更多")
            }
            feed.items.isNotEmpty() -> Text(
                text = "已经到底了",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun FollowingUsersPanel(
    publicFollowing: UserPreviewFeedState,
    privateFollowing: UserPreviewFeedState,
    selectedFeed: FollowUserFeed,
    onSelectFeed: (FollowUserFeed) -> Unit,
    onRefresh: (FollowUserFeed) -> Unit,
    onLoadMore: (FollowUserFeed) -> Unit,
    onOpenAuthor: (UserPreview) -> Unit,
) {
    val feed = when (selectedFeed) {
        FollowUserFeed.Public -> publicFollowing
        FollowUserFeed.Private -> privateFollowing
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FollowUserFeed.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedFeed == tab,
                        onClick = { onSelectFeed(tab) },
                        label = { Text(tab.label) },
                        leadingIcon = {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                }
            }
            FollowingUserSection(
                title = selectedFeed.label,
                feed = feed,
                emptyText = if (selectedFeed == FollowUserFeed.Private) "还没有悄悄关注" else "还没有公开关注",
                onRefresh = { onRefresh(selectedFeed) },
                onLoadMore = { onLoadMore(selectedFeed) },
                onOpenAuthor = onOpenAuthor,
            )
        }
    }
}

@Composable
private fun FollowingUserSection(
    title: String,
    feed: UserPreviewFeedState,
    emptyText: String,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenAuthor: (UserPreview) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = title,
            count = feed.items.size,
            countLabel = null,
            isLoading = feed.isLoading,
            onRefresh = onRefresh,
            showRefresh = true,
        )
        if (!feed.error.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Text(
                    text = feed.error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (feed.items.isEmpty()) {
            EmptyStateCard(
                text = if (feed.isLoading) "正在加载..." else emptyText,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            feed.items.forEach { user ->
                FollowingUserCard(user = user, onClick = { onOpenAuthor(user) })
            }
            PagingFooter(
                isLoadingMore = feed.isLoading,
                nextUrl = feed.nextUrl,
                hasItems = feed.items.isNotEmpty(),
                onLoadMore = onLoadMore,
            )
        }
    }
}

@Composable
private fun FollowingUserCard(
    user: UserPreview,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GlideImage(
                    url = user.avatarUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    crop = true,
                    requestSize = PuxivAvatarImageSize,
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = user.userName.ifBlank { "#${user.userId}" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = user.userAccount.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "ID ${user.userId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    user.comment.takeIf { it.isNotBlank() }?.let { comment ->
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            if (user.illusts.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    user.illusts.take(3).forEach { illust ->
                        GlideImage(
                            url = illust.previewUrl,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp)),
                            aspectRatio = 1f,
                            crop = true,
                            requestSize = PuxivTrendImageSize,
                        )
                    }
                    repeat((3 - user.illusts.take(3).size).coerceAtLeast(0)) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsPanel(
    downloads: List<DownloadItem>,
    onOpenPreview: (DownloadItem) -> Unit,
    onDelete: (DownloadItem) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("下载内容", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "点击条目可用本地预览查看作品",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val running = downloads.count { it.status == DownloadStatus.Running || it.status == DownloadStatus.Queued }
                if (running > 0) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }
            if (downloads.isEmpty()) {
                EmptyStateCard("还没有下载内容", Modifier.fillMaxWidth())
            } else {
                downloads.forEach { item ->
                    val savedUris = item.localSavedUris()
                    val canOpen = item.status == DownloadStatus.Finished && savedUris.any { it.isNotBlank() }
                    val previewUrl = savedUris.firstOrNull { it.isNotBlank() } ?: item.illust?.previewUrl
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (canOpen) {
                                        onOpenPreview(item)
                                    }
                                },
                                onLongClick = { onDelete(item) },
                            ),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (!previewUrl.isNullOrBlank()) {
                                    GlideImage(
                                        url = previewUrl,
                                        modifier = Modifier.matchParentSize(),
                                        aspectRatio = 1f,
                                        crop = true,
                                        requestSize = PuxivCardImageSize,
                                    )
                                } else {
                                    Icon(
                                        imageVector = when (item.status) {
                                            DownloadStatus.Finished -> Icons.Default.Save
                                            DownloadStatus.Failed -> Icons.Default.Refresh
                                            else -> Icons.Default.Download
                                        },
                                        contentDescription = null,
                                        tint = when (item.status) {
                                            DownloadStatus.Failed -> MaterialTheme.colorScheme.error
                                            DownloadStatus.Finished -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                ) {
                                    Icon(
                                        imageVector = when (item.status) {
                                            DownloadStatus.Finished -> Icons.Default.Save
                                            DownloadStatus.Failed -> Icons.Default.Refresh
                                            else -> Icons.Default.Download
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.padding(4.dp).size(14.dp),
                                        tint = when (item.status) {
                                            DownloadStatus.Failed -> MaterialTheme.colorScheme.error
                                            DownloadStatus.Finished -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.title.ifBlank { item.fileName },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = when {
                                        item.isUgoira -> "动图 ${item.fileName.substringAfterLast('.', "WEBP").uppercase()}"
                                        item.pageCount > 1 -> "${item.pageCount} 张图片"
                                        else -> "单张图片"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = item.detail.ifBlank { item.status.label },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (canOpen) {
                                Icon(
                                    Icons.AutoMirrored.Filled.NavigateNext,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            } else {
                                Text(
                                    text = item.status.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (item.status == DownloadStatus.Failed) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadIllustPanel(
    isUploading: Boolean,
    uploadStatus: String?,
    onUploadIllust: (String, String, List<String>, String, Int, String, Boolean, Int, List<Uri>) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("illust") }
    var visibilityScope by remember { mutableIntStateOf(1) }
    var xRestrict by remember { mutableStateOf("none") }
    var isSexual by remember { mutableStateOf(false) }
    var illustAiType by remember { mutableIntStateOf(1) }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = uris.take(20)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("发布作品", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Pixiv 投稿队列",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标题") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
            )
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("说明") },
                minLines = 2,
                shape = RoundedCornerShape(8.dp),
            )
            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标签，空格、半角逗号、全角逗号均可分隔") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = type == "illust", onClick = { type = "illust" }, label = { Text("插画") })
                FilterChip(selected = type == "manga", onClick = { type = "manga" }, label = { Text("漫画") })
                FilterChip(selected = visibilityScope == 1, onClick = { visibilityScope = 1 }, label = { Text("公开") })
                FilterChip(selected = visibilityScope == 2, onClick = { visibilityScope = 2 }, label = { Text("好友") })
                FilterChip(selected = visibilityScope == 3, onClick = { visibilityScope = 3 }, label = { Text("非公开") })
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = xRestrict == "none", onClick = { xRestrict = "none" }, label = { Text("全年龄") })
                FilterChip(selected = xRestrict == "r18", onClick = { xRestrict = "r18" }, label = { Text("R18") })
                FilterChip(selected = xRestrict == "r18g", onClick = { xRestrict = "r18g" }, label = { Text("R18G") })
                FilterChip(selected = isSexual, onClick = { isSexual = !isSexual }, label = { Text("性描写") })
                FilterChip(selected = illustAiType == 2, onClick = { illustAiType = if (illustAiType == 2) 1 else 2 }, label = { Text("AI 辅助") })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = { picker.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    enabled = !isUploading,
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedUris.isEmpty()) "选择图片" else "${selectedUris.size} 张图片")
                }
                Button(
                    onClick = {
                        val tags = tagsText.split(Regex("""[\s,，]+"""))
                        onUploadIllust(title, caption, tags, type, visibilityScope, xRestrict, isSexual, illustAiType, selectedUris)
                    },
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f),
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("提交")
                }
            }
            uploadStatus?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    diagnostics: DiagnosticsState,
    useHostIpRouting: Boolean,
    useRemoteImageProxy: Boolean,
    imageProxyInput: String,
    saveUgoiraZip: Boolean,
    filteredTagsInput: String,
    previewSwipeMode: PreviewSwipeMode,
    ugoiraSaveFormat: UgoiraSaveFormat,
    themeMode: PuxivThemeMode,
    useMaterialYou: Boolean,
    themePalette: PuxivThemePalette,
    customPalette: PuxivCustomPalette,
    contentPadding: PaddingValues,
    onRefreshDns: () -> Unit,
    onDiagnostics: () -> Unit,
    onHostIpRoutingEnabledChange: (Boolean) -> Unit,
    onPreviewSwipeModeChange: (PreviewSwipeMode) -> Unit,
    onThemeModeChange: (PuxivThemeMode) -> Unit,
    onMaterialYouEnabledChange: (Boolean) -> Unit,
    onThemePaletteChange: (PuxivThemePalette) -> Unit,
    onCustomThemePaletteChange: (PuxivCustomPalette) -> Unit,
    onImageProxyEnabledChange: (Boolean) -> Unit,
    onImageProxyInputChange: (String) -> Unit,
    onSaveImageProxy: () -> Unit,
    onResetImageProxy: () -> Unit,
    onSaveUgoiraZipChange: (Boolean) -> Unit,
    onFilteredTagsInputChange: (String) -> Unit,
    onSaveFilteredTags: () -> Unit,
    onUgoiraSaveFormatChange: (UgoiraSaveFormat) -> Unit,
) {
    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("设置")
                        Text(
                            text = "外观、网络路由、图片代理与预览方式",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemeSettingsSection(
                themeMode = themeMode,
                useMaterialYou = useMaterialYou,
                themePalette = themePalette,
                customPalette = customPalette,
                onThemeModeChange = onThemeModeChange,
                onMaterialYouEnabledChange = onMaterialYouEnabledChange,
                onThemePaletteChange = onThemePaletteChange,
                onCustomThemePaletteChange = onCustomThemePaletteChange,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "启用 Host/IP 兼容路由",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (useHostIpRouting) {
                                "使用内置 Host IP 与 DNS 映射改善 pixiv 直连"
                            } else {
                                "跟随系统网络，适合 VPN 或全局代理环境"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = useHostIpRouting,
                        onCheckedChange = onHostIpRoutingEnabledChange,
                    )
                }
            }
            if (useHostIpRouting) {
                Text(
                    text = "网络诊断",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                DiagnosticsPanel(
                    diagnostics = diagnostics,
                    onRefreshDns = onRefreshDns,
                    onDiagnostics = onDiagnostics,
                )
            }
            Text(
                text = "图片加载",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "远端图片代理",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (useRemoteImageProxy) {
                            "使用代理获取图片，可能更快"
                        } else {
                            "使用官方链接进行获取图片"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = !useRemoteImageProxy,
                            onClick = { onImageProxyEnabledChange(false) },
                            label = { Text("关闭") },
                        )
                        FilterChip(
                            selected = useRemoteImageProxy,
                            onClick = { onImageProxyEnabledChange(true) },
                            label = { Text("开启") },
                        )
                    }
                    if (useRemoteImageProxy) {
                        OutlinedTextField(
                            value = imageProxyInput,
                            onValueChange = onImageProxyInputChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("图片代理地址") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done,
                            ),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilledTonalButton(
                                onClick = onSaveImageProxy,
                                contentPadding = PaddingValues(horizontal = 12.dp),
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("保存")
                            }
                            OutlinedButton(
                                onClick = onResetImageProxy,
                                contentPadding = PaddingValues(horizontal = 12.dp),
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("默认")
                            }
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "动图保存格式",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "WebP 体积更小质量更好，GIF 兼容性更广。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = ugoiraSaveFormat == UgoiraSaveFormat.WEBP,
                            onClick = { onUgoiraSaveFormatChange(UgoiraSaveFormat.WEBP) },
                            label = { Text("WebP") },
                        )
                        FilterChip(
                            selected = ugoiraSaveFormat == UgoiraSaveFormat.GIF,
                            onClick = { onUgoiraSaveFormatChange(UgoiraSaveFormat.GIF) },
                            label = { Text("GIF") },
                        )
                    }
                }
            }
            Text(
                text = "内容过滤",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "下载动图时保留 zip",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "默认只保留动图文件。开启后会额外把原始 zip 存到 Downloads/IllustFerry。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = saveUgoiraZip,
                        onCheckedChange = onSaveUgoiraZipChange,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "过滤标签",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "用半角逗号分隔，命中标签的作品会从列表里隐藏。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = filteredTagsInput,
                        onValueChange = onFilteredTagsInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("例：AI生成,R-18,苦手标签") },
                    )
                    FilledTonalButton(
                        onClick = onSaveFilteredTags,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("保存过滤标签")
                    }
                }
            }
            Text(
                text = "作品预览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "多图作品浏览方向",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "影响作品详情页和全屏预览。漫画建议上下连续滚动，插画组图建议左右滑动。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = previewSwipeMode == PreviewSwipeMode.Vertical,
                            onClick = { onPreviewSwipeModeChange(PreviewSwipeMode.Vertical) },
                            label = { Text("上下连续滚动") },
                        )
                        FilterChip(
                            selected = previewSwipeMode == PreviewSwipeMode.Horizontal,
                            onClick = { onPreviewSwipeModeChange(PreviewSwipeMode.Horizontal) },
                            label = { Text("左右滑动") },
                        )
                    }
                }
            }
            OpenSourceLicenseSection()
        }
    }
}

@Composable
private fun ThemeSettingsSection(
    themeMode: PuxivThemeMode,
    useMaterialYou: Boolean,
    themePalette: PuxivThemePalette,
    customPalette: PuxivCustomPalette,
    onThemeModeChange: (PuxivThemeMode) -> Unit,
    onMaterialYouEnabledChange: (Boolean) -> Unit,
    onThemePaletteChange: (PuxivThemePalette) -> Unit,
    onCustomThemePaletteChange: (PuxivCustomPalette) -> Unit,
) {
    val materialYouAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val materialYouActive = useMaterialYou && materialYouAvailable
    var showCustomPaletteDialog by remember { mutableStateOf(false) }
    val previewDark = when (themeMode) {
        PuxivThemeMode.System -> isSystemInDarkTheme()
        PuxivThemeMode.Light -> false
        PuxivThemeMode.Dark -> true
    }
    val paletteScheme = themePalette.puxivColorScheme(previewDark, customPalette)

    if (showCustomPaletteDialog) {
        CustomPaletteDialog(
            customPalette = customPalette,
            previewDark = previewDark,
            onDismiss = { showCustomPaletteDialog = false },
            onSave = { palette ->
                onCustomThemePaletteChange(palette)
                showCustomPaletteDialog = false
            },
        )
    }

    Text(
        text = "外观",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Material You 动态取色",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (materialYouAvailable) "跟随系统壁纸生成应用色彩。" else "Android 12 及以上可用。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = materialYouActive,
                    onCheckedChange = { if (materialYouAvailable) onMaterialYouEnabledChange(it) },
                    enabled = materialYouAvailable,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "亮色 / 暗色",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PuxivThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.label) },
                        )
                    }
                }
            }

            if (!materialYouActive) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "调色板",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    PuxivThemePalette.entries.chunked(2).forEach { rowPalettes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowPalettes.forEach { palette ->
                                ThemePaletteChip(
                                    palette = palette,
                                    selected = themePalette == palette,
                                    previewDark = previewDark,
                                    customPalette = customPalette,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp),
                                    onClick = {
                                        if (palette == PuxivThemePalette.Custom) {
                                            showCustomPaletteDialog = true
                                        } else {
                                            onThemePaletteChange(palette)
                                        }
                                    },
                                )
                            }
                            if (rowPalettes.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    PaletteDetailPreview(colorScheme = paletteScheme)
                }
            }
        }
    }
}

@Composable
private fun ThemePaletteChip(
    palette: PuxivThemePalette,
    selected: Boolean,
    previewDark: Boolean,
    customPalette: PuxivCustomPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colorScheme = palette.puxivColorScheme(previewDark, customPalette)
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PaletteMiniSwatches(colorScheme = colorScheme)
            Text(
                text = palette.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PaletteMiniSwatches(colorScheme: ColorScheme) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        listOf(
            colorScheme.primary,
            colorScheme.secondary,
            colorScheme.tertiary,
        ).forEach { color ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun CustomPaletteDialog(
    customPalette: PuxivCustomPalette,
    previewDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (PuxivCustomPalette) -> Unit,
) {
    var primaryHex by remember(customPalette) { mutableStateOf(customPalette.primaryHex) }
    var secondaryHex by remember(customPalette) { mutableStateOf(customPalette.secondaryHex) }
    var tertiaryHex by remember(customPalette) { mutableStateOf(customPalette.tertiaryHex) }
    var backgroundHex by remember(customPalette) { mutableStateOf(customPalette.backgroundHex) }
    var surfaceHex by remember(customPalette) { mutableStateOf(customPalette.surfaceHex) }
    val candidate = PuxivCustomPalette(
        primaryHex = primaryHex.normalizedHexOrEmpty(),
        secondaryHex = secondaryHex.normalizedHexOrEmpty(),
        tertiaryHex = tertiaryHex.normalizedHexOrEmpty(),
        backgroundHex = backgroundHex.normalizedHexOrEmpty(),
        surfaceHex = surfaceHex.normalizedHexOrEmpty(),
    )
    val isValid = listOf(
        primaryHex,
        secondaryHex,
        tertiaryHex,
        backgroundHex,
        surfaceHex,
    ).all { it.normalizedHexOrNull() != null }
    val previewScheme = PuxivThemePalette.Custom.puxivColorScheme(previewDark, candidate)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自定义调色板") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CustomPaletteField("主色", primaryHex, { primaryHex = it })
                CustomPaletteField("辅色", secondaryHex, { secondaryHex = it })
                CustomPaletteField("强调色", tertiaryHex, { tertiaryHex = it })
                CustomPaletteField("背景", backgroundHex, { backgroundHex = it })
                CustomPaletteField("表面", surfaceHex, { surfaceHex = it })
                if (!isValid) {
                    Text(
                        text = "请输入 #RRGGBB 格式的颜色。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                PaletteDetailPreview(colorScheme = previewScheme)
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = { onSave(candidate) },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun CustomPaletteField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val color = value.toColorOrNull()
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color ?: MaterialTheme.colorScheme.errorContainer),
        )
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.take(7)) },
            modifier = Modifier.weight(1f),
            label = { Text(label) },
            singleLine = true,
            isError = value.normalizedHexOrNull() == null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next,
            ),
        )
    }
}

@Composable
private fun PaletteDetailPreview(
    colorScheme: ColorScheme,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PaletteDetailToken("主色", colorScheme.primary)
            PaletteDetailToken("辅色", colorScheme.secondary)
            PaletteDetailToken("强调", colorScheme.tertiary)
            PaletteDetailToken("容器", colorScheme.primaryContainer)
            PaletteDetailToken("背景", colorScheme.background)
            PaletteDetailToken("表面", colorScheme.surfaceVariant)
        }
    }
}

@Composable
private fun PaletteDetailToken(
    label: String,
    color: Color,
) {
    Row(
        modifier = Modifier.widthIn(min = 116.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = color.toHexString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val PuxivThemeMode.label: String
    get() = when (this) {
        PuxivThemeMode.System -> "跟随系统"
        PuxivThemeMode.Light -> "亮色"
        PuxivThemeMode.Dark -> "暗色"
    }

private val PuxivThemePalette.label: String
    get() = when (this) {
        PuxivThemePalette.Puxiv -> "Puxiv 蓝"
        PuxivThemePalette.Sakura -> "樱花"
        PuxivThemePalette.Mint -> "薄荷"
        PuxivThemePalette.Violet -> "紫罗兰"
        PuxivThemePalette.Amber -> "琥珀"
        PuxivThemePalette.Slate -> "青石"
        PuxivThemePalette.Custom -> "自定义"
    }

private fun Color.toHexString(): String {
    val rgb = toArgb() and 0x00FFFFFF
    return "#${rgb.toString(16).padStart(6, '0').uppercase()}"
}

private fun String.normalizedHexOrEmpty(): String {
    return normalizedHexOrNull().orEmpty()
}

private fun String.normalizedHexOrNull(): String? {
    val raw = trim()
    val withHash = if (raw.startsWith("#")) raw else "#$raw"
    return if (UI_HEX_COLOR_PATTERN.matches(withHash)) withHash.uppercase() else null
}

private fun String.toColorOrNull(): Color? {
    val hex = normalizedHexOrNull()?.removePrefix("#") ?: return null
    return Color(0xFF000000 or hex.toLong(16))
}

private val UI_HEX_COLOR_PATTERN = Regex("""#[0-9A-Fa-f]{6}""")

@Composable
private fun OpenSourceLicenseSection() {
    val context = LocalContext.current
    Text(
        text = "开源许可",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "IllustFerry",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "GPL-3.0-only 开源协议",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OpenSourceLibraries.forEach { library ->
                OpenSourceLicenseRow(
                    library = library,
                    onOpen = { openExternalUrl(context, it) },
                )
            }
        }
    }
}

@Composable
private fun OpenSourceLicenseRow(
    library: OpenSourceLibrary,
    onOpen: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onOpen(library.licenseUrl) }
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = library.license,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class OpenSourceLibrary(
    val name: String,
    val license: String,
    val licenseUrl: String,
)

private val OpenSourceLibraries = listOf(
    OpenSourceLibrary(
        name = "Kotlin",
        license = "Apache License 2.0",
        licenseUrl = "https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt",
    ),
    OpenSourceLibrary(
        name = "AndroidX Activity / Core / Lifecycle / WebKit",
        license = "Apache License 2.0",
        licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
    ),
    OpenSourceLibrary(
        name = "Jetpack Compose / Material 3 / Material Icons",
        license = "Apache License 2.0",
        licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
    ),
    OpenSourceLibrary(
        name = "Kotlinx Coroutines",
        license = "Apache License 2.0",
        licenseUrl = "https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt",
    ),
    OpenSourceLibrary(
        name = "OkHttp",
        license = "Apache License 2.0",
        licenseUrl = "https://github.com/square/okhttp/blob/master/LICENSE.txt",
    ),
    OpenSourceLibrary(
        name = "Gson",
        license = "Apache License 2.0",
        licenseUrl = "https://github.com/google/gson/blob/main/LICENSE",
    ),
    OpenSourceLibrary(
        name = "Glide / Glide OkHttp / Glide GIF Encoder",
        license = "Simplified BSD License / Apache License 2.0",
        licenseUrl = "https://github.com/bumptech/glide/blob/master/LICENSE",
    ),
    OpenSourceLibrary(
        name = "webp-android",
        license = "MIT License",
        licenseUrl = "https://github.com/UdaraWanasinghe/webp-android/blob/main/LICENSE",
    ),
    OpenSourceLibrary(
        name = "Bouncy Castle bcprov / bcpkix",
        license = "Bouncy Castle Licence",
        licenseUrl = "https://www.bouncycastle.org/licence.html",
    ),
)

private fun openExternalUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SearchScreen(
    keyword: String,
    isBusy: Boolean,
    searchTarget: SearchTarget,
    searchSort: SearchSort,
    isTrendingLoading: Boolean,
    trendingTags: List<TrendingTag>,
    discover: DiscoverState,
    items: List<Illust>,
    isSearchActive: Boolean,
    isLoadingMore: Boolean,
    nextUrl: String?,
    contentPadding: PaddingValues,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onSearchSortChange: (SearchSort) -> Unit,
    onSearchTargetChange: (SearchTarget) -> Unit,
    onTrendingTagClick: (TrendingTag) -> Unit,
    onReturnToDiscover: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onRefreshDiscover: () -> Unit,
    onLoadMoreDiscover: (DiscoverFeed, Boolean) -> Unit,
) {
    val searchGridState = rememberLazyStaggeredGridState()
    val decorativeFeedSemantics = rememberDecorativeFeedSemantics()
    val resultGridState = rememberLazyStaggeredGridState()
    var selectedDiscoverFeed by remember { mutableStateOf(DiscoverFeed.Public) }

    LaunchedEffect(isSearchActive, nextUrl, isLoadingMore, isBusy, items.size) {
        if (!isSearchActive) return@LaunchedEffect
        snapshotFlow {
            val layoutInfo = resultGridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: -1
            layoutInfo.totalItemsCount > 0 && lastVisibleIndex >= layoutInfo.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && nextUrl != null && !isLoadingMore && !isBusy) {
                    onLoadMore()
                }
            }
    }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("发现")
                        Text(
                            text = if (isSearchActive) {
                                "${items.size} 搜索结果"
                            } else {
                                "关注作品"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        if (!isSearchActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .feedSemanticsBoundary(decorativeFeedSemantics),
            ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(154.dp),
                    state = searchGridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SearchControlPanel(
                            keyword = keyword,
                            isBusy = isBusy,
                            searchTarget = searchTarget,
                            searchSort = searchSort,
                            isTrendingLoading = isTrendingLoading,
                            trendingTags = trendingTags,
                            onKeywordChange = onKeywordChange,
                            onSearch = onSearch,
                            onSearchSortChange = onSearchSortChange,
                            onSearchTargetChange = onSearchTargetChange,
                            onTrendingTagClick = onTrendingTagClick,
                        )
                    }
                    discoverItems(
                        discover = discover,
                        selectedFeed = selectedDiscoverFeed,
                        onSelectFeed = { feed ->
                            selectedDiscoverFeed = feed
                            if (discover.feed(feed).items.isEmpty()) {
                                onLoadMoreDiscover(feed, true)
                            }
                        },
                        onRefresh = onRefreshDiscover,
                        onLoadMore = onLoadMoreDiscover,
                        onOpenPreview = onOpenPreview,
                        clearSemantics = decorativeFeedSemantics,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .feedSemanticsBoundary(decorativeFeedSemantics),
            ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(154.dp),
                    state = resultGridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SearchControlPanel(
                            keyword = keyword,
                            isBusy = isBusy,
                            searchTarget = searchTarget,
                            searchSort = searchSort,
                            isTrendingLoading = isTrendingLoading,
                            trendingTags = trendingTags,
                            onKeywordChange = onKeywordChange,
                            onSearch = onSearch,
                            onSearchSortChange = onSearchSortChange,
                            onSearchTargetChange = onSearchTargetChange,
                            onTrendingTagClick = onTrendingTagClick,
                        )
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SearchResultHeader(
                            resultCount = items.size,
                            isBusy = isBusy,
                            onBack = onReturnToDiscover,
                        )
                    }
                    if (items.isEmpty() && !isBusy) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            EmptySearch(Modifier.fillMaxWidth())
                        }
                    } else {
                        items(
                            items,
                            key = { it.id },
                            contentType = { "illust-card" },
                        ) { illust ->
                            IllustCard(
                                illust = illust,
                                clearSemantics = decorativeFeedSemantics,
                                onClick = onOpenPreview,
                            )
                        }
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PagingFooter(
                                isLoadingMore = isLoadingMore,
                                nextUrl = nextUrl,
                                hasItems = items.isNotEmpty(),
                                onLoadMore = onLoadMore,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchControlPanel(
    keyword: String,
    isBusy: Boolean,
    searchTarget: SearchTarget,
    searchSort: SearchSort,
    isTrendingLoading: Boolean,
    trendingTags: List<TrendingTag>,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchSortChange: (SearchSort) -> Unit,
    onSearchTargetChange: (SearchTarget) -> Unit,
    onTrendingTagClick: (TrendingTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PixivSearchField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    placeholder = "标签、标题、作者",
                    onSearch = onSearch,
                )
                IconButton(
                    onClick = onSearch,
                    enabled = !isBusy,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SearchTarget.values().forEach { target ->
                    FilterChip(
                        selected = searchTarget == target,
                        onClick = { onSearchTargetChange(target) },
                        label = { Text(target.label) },
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SearchSort.entries.forEach { sort ->
                    FilterChip(
                        selected = searchSort == sort,
                        onClick = { onSearchSortChange(sort) },
                        label = { Text(sort.label) },
                    )
                }
            }

            if (isTrendingLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (trendingTags.isNotEmpty()) {
                TrendingTagRail(
                    tags = trendingTags,
                    onTrendingTagClick = onTrendingTagClick,
                )
            }
        }
    }
}

@Composable
private fun TrendingTagRail(
    tags: List<TrendingTag>,
    onTrendingTagClick: (TrendingTag) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 2.dp),
    ) {
        lazyRowItems(tags, key = { it.name }) { tag ->
            ElevatedCard(
                onClick = { onTrendingTagClick(tag) },
                modifier = Modifier.width(142.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
            ) {
                Box {
                    GlideImage(
                        url = tag.previewUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        crop = true,
                        requestSize = PuxivTrendImageSize,
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            tag.translatedName?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultHeader(
    resultCount: Int,
    isBusy: Boolean,
    onBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("返回关注")
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = if (isBusy) "搜索中" else "$resultCount 个结果",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DiscoverFollowPanel(
    discover: DiscoverState,
    selectedFeed: DiscoverFeed,
    onSelectFeed: (DiscoverFeed) -> Unit,
    onRefresh: () -> Unit,
) {
    val feed = discover.feed(selectedFeed)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionHeader(
                title = "关注作品",
                count = feed.items.size,
                countLabel = null,
                isLoading = feed.isLoading,
                onRefresh = onRefresh,
                showRefresh = true,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiscoverFeed.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedFeed == tab,
                        onClick = { onSelectFeed(tab) },
                        label = { Text(tab.label) },
                        leadingIcon = {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                }
            }
        }
    }
}

private fun LazyStaggeredGridScope.discoverItems(
    discover: DiscoverState,
    selectedFeed: DiscoverFeed,
    onSelectFeed: (DiscoverFeed) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: (DiscoverFeed, Boolean) -> Unit,
    onOpenPreview: (Illust) -> Unit,
    clearSemantics: Boolean,
) {
    val feed = discover.feed(selectedFeed)
    item(span = StaggeredGridItemSpan.FullLine) {
        DiscoverFollowPanel(
            discover = discover,
            selectedFeed = selectedFeed,
            onSelectFeed = onSelectFeed,
            onRefresh = onRefresh,
        )
    }
    discoverFeedMessages(
        feed = feed,
        emptyText = if (selectedFeed == DiscoverFeed.Private) "还没有加载到悄悄关注作品" else "还没有加载到关注用户作品",
    )
    items(
        feed.items,
        key = { "discover-${selectedFeed.name}-${it.id}" },
        contentType = { "illust-card" },
    ) { illust ->
        IllustCard(
            illust = illust,
            clearSemantics = clearSemantics,
            onClick = onOpenPreview,
        )
    }
    item(span = StaggeredGridItemSpan.FullLine) {
        DiscoverPagingFooter(
            feed = feed,
            onLoadMore = { onLoadMore(selectedFeed, false) },
        )
    }
}

private fun LazyStaggeredGridScope.discoverFeedMessages(
    feed: FeedState,
    emptyText: String,
) {
    if (feed.error != null) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Text(
                    text = feed.error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (feed.items.isEmpty() && !feed.isLoading) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Text(
                text = emptyText,
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiscoverPagingFooter(
    feed: FeedState,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            feed.isLoading -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            feed.nextUrl != null -> FilledTonalButton(onClick = onLoadMore) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("加载更多")
            }
            feed.items.isNotEmpty() -> Text(
                text = "已经到底了",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewScreen(
    state: PuxivUiState,
    onBack: () -> Unit,
    onSelectImage: (Int) -> Unit,
    bookmarkTags: List<String>,
    areBookmarkTagsLoaded: Boolean,
    selectedBookmark: SelectedBookmarkState,
    onBookmarkPublic: (List<String>) -> Unit,
    onBookmarkPrivate: (List<String>) -> Unit,
    onAddBookmarkTags: (List<String>) -> Unit,
    onToggleBookmarkTag: (String) -> Unit,
    onDeleteBookmark: () -> Unit,
    onLoadBookmarkDetail: (Long, Boolean) -> Unit,
    onLoadBookmarkTags: () -> Unit,
    onLoadRelated: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onOpenFullScreen: (Int) -> Unit,
    onCloseFullScreen: () -> Unit,
    onTagClick: (String) -> Unit,
    onDownload: () -> Unit,
    onCommentInputChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onOpenAuthor: (Illust) -> Unit,
) {
    val illust = state.selectedIllust
    LaunchedEffect(illust?.id, illust?.isBookmarked, selectedBookmark.isLoaded) {
        val id = illust?.id ?: return@LaunchedEffect
        if (illust.isBookmarked && !selectedBookmark.isLoaded) {
            onLoadBookmarkDetail(id, false)
        }
    }
    LaunchedEffect(areBookmarkTagsLoaded) {
        if (!areBookmarkTagsLoaded) {
            onLoadBookmarkTags()
        }
    }
    val previewStateHolder = rememberSaveableStateHolder()
    var measuredRatios by remember(illust?.id) { mutableStateOf<Map<Int, Float>>(emptyMap()) }
    fun updateMeasuredRatio(index: Int, width: Int, height: Int) {
        val ratio = (width.toFloat() / height.coerceAtLeast(1)).coerceIn(0.18f, 4.5f)
        if (measuredRatios[index] != ratio) {
            measuredRatios = measuredRatios + (index to ratio)
        }
    }
    val fullScreenIndex = illust?.let {
        val size = it.imageUrls.ifEmpty { listOfNotNull(it.previewUrl) }.size
        state.selectedImageIndex.coerceIn(0, (size - 1).coerceAtLeast(0))
    } ?: 0
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = illust?.title.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        if (illust == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("没有可预览的作品")
            }
            return@Scaffold
        }

        previewStateHolder.SaveableStateProvider(illust.id) {
            val pages = illust.imagePages.ifEmpty {
                illust.imageUrls.ifEmpty { listOfNotNull(illust.previewUrl) }.map {
                    IllustImagePage(it, illust.width, illust.height)
                }
            }
            val safeIndex = state.selectedImageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (!illust.isUgoira && state.previewSwipeMode == PreviewSwipeMode.Vertical) {
                    VerticalComicPreview(
                        illust = illust,
                        pages = pages,
                        selectedIndex = safeIndex,
                        measuredRatios = measuredRatios,
                        animateImages = !state.isFullScreenPreview,
                        onSelectImage = onSelectImage,
                        onOpenFullScreen = onOpenFullScreen,
                        onImageMeasured = { index, width, height ->
                            updateMeasuredRatio(index, width, height)
                        },
                        footer = {
                            IllustMeta(
                                illust = illust,
                                related = state.related,
                                bookmarkTags = bookmarkTags,
                                selectedBookmark = selectedBookmark,
                                onBookmarkPublic = onBookmarkPublic,
                                onBookmarkPrivate = onBookmarkPrivate,
                                onAddBookmarkTags = onAddBookmarkTags,
                                onToggleBookmarkTag = onToggleBookmarkTag,
                                onDeleteBookmark = onDeleteBookmark,
                                onDownload = onDownload,
                                actionsEnabled = !state.isBusy,
                                onLoadRelated = onLoadRelated,
                                onOpenPreview = onOpenPreview,
                                onTagClick = onTagClick,
                                onOpenAuthor = onOpenAuthor,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            CommentsPanel(
                                comments = state.comments,
                                onInputChange = onCommentInputChange,
                                onSend = onSendComment,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (state.isPreviewLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                } else {
                    val previewRatio = measuredRatios[safeIndex] ?: pages.getOrNull(safeIndex)?.aspectRatio ?: illust.aspectRatio
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val dynamicHeight = (maxWidth / previewRatio.coerceIn(0.35f, 2.4f))
                            .coerceIn(maxHeight * 0.34f, maxHeight * 0.76f)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dynamicHeight)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                if (illust.isUgoira && state.ugoiraFrames.isNotEmpty()) {
                                    UgoiraPlayer(
                                        frames = state.ugoiraFrames,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    HorizontalImagePreview(
                                        illust = illust,
                                        imageIndex = state.selectedImageIndex,
                                        animateImages = !state.isFullScreenPreview,
                                        onSelectImage = onSelectImage,
                                        onOpenFullScreen = onOpenFullScreen,
                                        onImageMeasured = { index, width, height ->
                                            updateMeasuredRatio(index, width, height)
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }

                                if (state.isPreviewLoading) {
                                    if (illust.isUgoira) {
                                        UgoiraLoadingProgress(
                                            loaded = state.ugoiraLoadedFrames,
                                            total = state.ugoiraTotalFrames,
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    }
                                }
                            }

                            IllustMeta(
                                illust = illust,
                                related = state.related,
                                bookmarkTags = bookmarkTags,
                                selectedBookmark = selectedBookmark,
                                onBookmarkPublic = onBookmarkPublic,
                                onBookmarkPrivate = onBookmarkPrivate,
                                onAddBookmarkTags = onAddBookmarkTags,
                                onToggleBookmarkTag = onToggleBookmarkTag,
                                onDeleteBookmark = onDeleteBookmark,
                                onDownload = onDownload,
                                actionsEnabled = !state.isBusy,
                                onLoadRelated = onLoadRelated,
                                onOpenPreview = onOpenPreview,
                                onTagClick = onTagClick,
                                onOpenAuthor = onOpenAuthor,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            CommentsPanel(
                                comments = state.comments,
                                onInputChange = onCommentInputChange,
                                onSend = onSendComment,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.isFullScreenPreview && illust != null && !illust.isUgoira) {
        FullScreenPreview(
            illust = illust,
            imageIndex = fullScreenIndex,
            swipeMode = state.previewSwipeMode,
            frames = state.ugoiraFrames,
            measuredRatios = measuredRatios,
            onSelectImage = onSelectImage,
            onClose = onCloseFullScreen,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthorScreen(
    author: AuthorState,
    onBack: () -> Unit,
    onRefreshProfile: () -> Unit,
    onRefreshWorks: () -> Unit,
    onLoadMore: () -> Unit,
    onSelectTab: (AuthorWorkTab) -> Unit,
    onFollowPublic: () -> Unit,
    onFollowPrivate: () -> Unit,
    onUnfollow: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
) {
    val gridState = rememberLazyStaggeredGridState()
    val feed = when (author.selectedTab) {
        AuthorWorkTab.Illust -> author.illusts
        AuthorWorkTab.Manga -> author.manga
        AuthorWorkTab.Bookmarks -> author.bookmarks
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = author.userName.ifBlank { "作者" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onRefreshProfile()
                        onRefreshWorks()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(154.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                AuthorHeader(
                    author = author,
                    onRefresh = onRefreshProfile,
                    onFollowPublic = onFollowPublic,
                    onFollowPrivate = onFollowPrivate,
                    onUnfollow = onUnfollow,
                )
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                AuthorTabRow(
                    selected = author.selectedTab,
                    illustCount = author.totalIllusts,
                    mangaCount = author.totalManga,
                    bookmarkCount = author.totalBookmarks,
                    onSelect = onSelectTab,
                )
            }
            if (feed.error != null) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        Text(
                            text = feed.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            if (feed.items.isEmpty() && (author.isLoadingProfile || author.isLoadingWorks)) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (feed.items.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptySearch(Modifier.fillMaxWidth())
                }
            } else {
                items(
                    feed.items,
                    key = { "author-${author.userId}-${it.id}" },
                    contentType = { "illust-card" },
                ) { illust ->
                    IllustCard(
                        illust = illust,
                        onClick = onOpenPreview,
                    )
                }
                item(span = StaggeredGridItemSpan.FullLine) {
                    PagingFooter(
                        isLoadingMore = author.isLoadingWorks && feed.items.isNotEmpty(),
                        nextUrl = feed.nextUrl,
                        hasItems = feed.items.isNotEmpty(),
                        onLoadMore = onLoadMore,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthorHeader(
    author: AuthorState,
    onRefresh: () -> Unit,
    onFollowPublic: () -> Unit,
    onFollowPrivate: () -> Unit,
    onUnfollow: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlideImage(
                    url = author.userAvatarUrl,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    crop = true,
                    requestSize = PuxivAvatarImageSize,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = author.userName.ifBlank { "Unknown artist" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    author.userAccount.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "@$it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "关注 ${author.followingCount} · 好友 ${author.myPixivCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetadataPill("${author.totalIllusts} 插画")
                MetadataPill("${author.totalManga} 漫画")
                MetadataPill("${author.totalBookmarks} 收藏")
            }
            author.userComment.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (author.isFollowed) {
                    FilledTonalButton(onClick = onUnfollow, enabled = !author.isFollowBusy) {
                        Text("取消关注")
                    }
                } else {
                    FilledTonalButton(onClick = onFollowPublic, enabled = !author.isFollowBusy) {
                        Text("关注")
                    }
                    OutlinedButton(onClick = onFollowPrivate, enabled = !author.isFollowBusy) {
                        Text("悄悄关注")
                    }
                }
                OutlinedButton(onClick = onRefresh, enabled = !author.isLoadingProfile) {
                    Text("刷新")
                }
            }
        }
    }
}

@Composable
private fun AuthorTabRow(
    selected: AuthorWorkTab,
    illustCount: Int,
    mangaCount: Int,
    bookmarkCount: Int,
    onSelect: (AuthorWorkTab) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selected == AuthorWorkTab.Illust,
                onClick = { onSelect(AuthorWorkTab.Illust) },
                label = { Text("插画 $illustCount") },
            )
            FilterChip(
                selected = selected == AuthorWorkTab.Manga,
                onClick = { onSelect(AuthorWorkTab.Manga) },
                label = { Text("漫画 $mangaCount") },
            )
            FilterChip(
                selected = selected == AuthorWorkTab.Bookmarks,
                onClick = { onSelect(AuthorWorkTab.Bookmarks) },
                label = { Text("收藏 $bookmarkCount") },
            )
        }
    }
}

@Composable
private fun HistoryIllustCard(
    entry: HistoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IllustCard(
            illust = entry.illust,
            onLongPress = onDelete,
            onClick = { onClick() },
        )
        Text(
            text = formatViewedAt(entry.viewedAtMillis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun EmptyStateCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptySearch(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text("输入关键词开始搜索", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun rememberDecorativeFeedSemantics(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        manager?.isEnabled == true && manager.isTouchExplorationEnabled == false
    }
}

private fun Modifier.feedSemanticsBoundary(enabled: Boolean): Modifier {
    return if (enabled) clearAndSetSemantics {} else this
}

private fun formatViewedAt(viewedAtMillis: Long): String {
    return runCatching {
        DateTimeFormatter.ofPattern("MM-dd HH:mm")
            .format(Instant.ofEpochMilli(viewedAtMillis).atZone(ZoneId.systemDefault()))
    }.getOrElse { viewedAtMillis.toString() }
}

@Composable
private fun IllustCard(
    illust: Illust,
    forceSquare: Boolean = false,
    clearSemantics: Boolean = false,
    onLongPress: (() -> Unit)? = null,
    onClick: (Illust) -> Unit,
) {
    val cardModifier = if (clearSemantics) {
        Modifier.clearAndSetSemantics {}
    } else {
        Modifier
    }
    val interactiveModifier = cardModifier.combinedClickable(
        onClick = { onClick(illust) },
        onLongClick = onLongPress,
    )
    ElevatedCard(
        modifier = interactiveModifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box {
            GlideImage(
                url = illust.previewUrl,
                modifier = Modifier.fillMaxWidth(),
                aspectRatio = if (forceSquare) 1f else illust.aspectRatio,
                crop = true,
                requestSize = PuxivCardImageSize,
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OverlayChip(illust.typeLabel)
                if (illust.pageCount > 1) {
                    OverlayChip("${illust.pageCount}P")
                }
            }
        }
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = illust.title.ifBlank { "#${illust.id}" },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = illust.authorName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetadataPill(illust.typeLabel)
                if (illust.pageCount > 1) MetadataPill("${illust.pageCount}P")
            }
        }
    }
}

@Composable
private fun PagingFooter(
    isLoadingMore: Boolean,
    nextUrl: String?,
    hasItems: Boolean,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoadingMore -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            nextUrl != null -> Button(onClick = onLoadMore) { Text("加载更多") }
            hasItems -> Text(
                text = "已经到底了",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

private fun Illust.previewUrls(): List<String> = imageUrls.ifEmpty { listOfNotNull(previewUrl) }

private fun MyState.bookmarkFeed(feed: BookmarkFeed): FeedState {
    return when (feed) {
        BookmarkFeed.Public -> bookmarks
        BookmarkFeed.Private -> privateBookmarks
    }
}

private fun MyState.bookmarkTags(feed: BookmarkFeed): List<BookmarkTag> {
    return when (feed) {
        BookmarkFeed.Public -> publicBookmarkTags
        BookmarkFeed.Private -> privateBookmarkTags
    }
}

private fun MyState.followingFeed(feed: FollowUserFeed): UserPreviewFeedState {
    return when (feed) {
        FollowUserFeed.Public -> publicFollowing
        FollowUserFeed.Private -> privateFollowing
    }
}

private fun DiscoverState.feed(feed: DiscoverFeed): FeedState {
    return when (feed) {
        DiscoverFeed.Public -> publicWorks
        DiscoverFeed.Private -> privateWorks
    }
}

private val BookmarkFeed.label: String
    get() = when (this) {
        BookmarkFeed.Public -> "收藏作品"
        BookmarkFeed.Private -> "私密收藏"
    }

private val BookmarkFeed.icon: ImageVector
    get() = when (this) {
        BookmarkFeed.Public -> Icons.Default.Favorite
        BookmarkFeed.Private -> Icons.Default.Lock
    }

private val FollowUserFeed.label: String
    get() = when (this) {
        FollowUserFeed.Public -> "公开关注"
        FollowUserFeed.Private -> "悄悄关注"
    }

private val FollowUserFeed.icon: ImageVector
    get() = when (this) {
        FollowUserFeed.Public -> Icons.Default.AccountCircle
        FollowUserFeed.Private -> Icons.Default.Lock
    }

private val DiscoverFeed.label: String
    get() = when (this) {
        DiscoverFeed.Public -> "公开关注"
        DiscoverFeed.Private -> "悄悄关注"
    }

private val DiscoverFeed.icon: ImageVector
    get() = when (this) {
        DiscoverFeed.Public -> Icons.Default.AccountCircle
        DiscoverFeed.Private -> Icons.Default.Lock
    }

private fun MyState.bookmarkTags(): List<String> {
    return (publicBookmarkTags.map { it.name } + privateBookmarkTags.map { it.name })
        .distinctBy { it.lowercase() }
        .take(10)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalImagePreview(
    illust: Illust,
    imageIndex: Int,
    animateImages: Boolean,
    onSelectImage: (Int) -> Unit,
    onOpenFullScreen: (Int) -> Unit,
    onImageMeasured: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val urls = illust.previewUrls()
    val safeIndex = imageIndex.coerceIn(0, (urls.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(initialPage = safeIndex) { urls.size.coerceAtLeast(1) }

    LaunchedEffect(safeIndex, urls.size) {
        if (urls.isNotEmpty() && pagerState.currentPage != safeIndex) {
            pagerState.animateScrollToPage(safeIndex)
        }
    }
    LaunchedEffect(pagerState, urls.size) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (urls.isNotEmpty()) onSelectImage(page.coerceIn(urls.indices))
            }
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> "${illust.id}-preview-$index" },
        ) { index ->
            GlideImage(
                url = urls.getOrNull(index),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onOpenFullScreen(index) })
                    },
                crop = false,
                showLoadingBar = true,
                requestSize = PuxivPreviewImageSize,
                onDrawableSize = { width, height -> onImageMeasured(index, width, height) },
                animate = animateImages,
            )
        }
        if (urls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = { onSelectImage(safeIndex - 1) }, enabled = safeIndex > 0) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "上一张")
                }
                Text("${safeIndex + 1} / ${urls.size}")
                IconButton(onClick = { onSelectImage(safeIndex + 1) }, enabled = safeIndex < urls.lastIndex) {
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "下一张")
                }
            }
        }
    }
}

@Composable
private fun VerticalComicPreview(
    illust: Illust,
    pages: List<IllustImagePage>,
    selectedIndex: Int,
    measuredRatios: Map<Int, Float>,
    animateImages: Boolean,
    onSelectImage: (Int) -> Unit,
    onOpenFullScreen: (Int) -> Unit,
    onImageMeasured: (Int, Int, Int) -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val urls = illust.previewUrls()
    val scrollState = rememberScrollState()
    var containerWidthPx by remember(illust.id) { mutableIntStateOf(1) }
    var lastSelectedFromScroll by remember(illust.id) {
        mutableIntStateOf(selectedIndex.coerceIn(0, (urls.size - 1).coerceAtLeast(0)))
    }

    fun pageHeightPx(index: Int): Int {
        val ratio = measuredRatios[index] ?: pages.getOrNull(index)?.aspectRatio ?: illust.aspectRatio
        return (containerWidthPx / ratio.coerceIn(0.18f, 4.5f)).roundToInt().coerceAtLeast(1)
    }

    LaunchedEffect(selectedIndex, urls.size) {
        if (urls.isNotEmpty() && selectedIndex in urls.indices && selectedIndex != lastSelectedFromScroll) {
            val targetOffset = urls.indices
                .take(selectedIndex)
                .sumOf(::pageHeightPx)
            scrollState.animateScrollTo(targetOffset)
        }
    }
    LaunchedEffect(scrollState, urls.size, measuredRatios) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .collect { offset ->
                if (urls.isNotEmpty()) {
                    var accumulated = 0
                    val index = urls.indices.firstOrNull { page ->
                        val next = accumulated + pageHeightPx(page)
                        val isCurrent = offset < next
                        accumulated = next
                        isCurrent
                    } ?: urls.lastIndex
                    val safeIndex = index.coerceIn(urls.indices)
                    lastSelectedFromScroll = safeIndex
                    onSelectImage(safeIndex)
                }
            }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { containerWidthPx = it.width.coerceAtLeast(1) }
            .verticalScroll(scrollState),
    ) {
        urls.forEachIndexed { index, url ->
            val ratio = measuredRatios[index] ?: pages.getOrNull(index)?.aspectRatio ?: illust.aspectRatio
            GlideImage(
                url = url,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio.coerceIn(0.18f, 4.5f))
                    .pointerInput(index) {
                        detectTapGestures(onTap = { onOpenFullScreen(index) })
                    },
                crop = false,
                showLoadingBar = true,
                requestSize = PuxivPreviewImageSize,
                onDrawableSize = { width, height -> onImageMeasured(index, width, height) },
                animate = animateImages,
            )
        }
        footer()
    }
}

@Composable
private fun UgoiraPlayer(
    frames: List<UgoiraFrameImage>,
    modifier: Modifier = Modifier,
) {
    var index by remember(frames) { mutableIntStateOf(0) }
    val currentFrames by rememberUpdatedState(frames)

    LaunchedEffect(frames) {
        index = 0
        while (isActive && currentFrames.isNotEmpty()) {
            val frame = currentFrames[index.coerceIn(currentFrames.indices)]
            delay(frame.delayMs.coerceAtLeast(16).toLong())
            index = (index + 1) % currentFrames.size
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            bitmap = frames[index.coerceIn(frames.indices)].bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullScreenPreview(
    illust: Illust,
    imageIndex: Int,
    swipeMode: PreviewSwipeMode,
    frames: List<UgoiraFrameImage>,
    measuredRatios: Map<Int, Float>,
    onSelectImage: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val urls = illust.imageUrls.ifEmpty { listOfNotNull(illust.previewUrl) }
    val safeIndex = imageIndex.coerceIn(0, (urls.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(initialPage = safeIndex) { urls.size.coerceAtLeast(1) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(safeIndex, swipeMode, urls.size) {
        if (urls.isNotEmpty() && pagerState.currentPage != safeIndex) {
            pagerState.scrollToPage(safeIndex)
        }
    }
    LaunchedEffect(pagerState, urls.size) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (urls.isNotEmpty()) onSelectImage(page.coerceIn(urls.indices))
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
        contentColor = Color.White,
    ) {
        Box(Modifier.fillMaxSize()) {
            if (illust.isUgoira && frames.isNotEmpty()) {
                UgoiraPlayer(
                    frames = frames,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onClose() })
                        },
                )
            } else {
                if (swipeMode == PreviewSwipeMode.Vertical) {
                    FullScreenVerticalPreview(
                        illust = illust,
                        selectedIndex = safeIndex,
                        measuredRatios = measuredRatios,
                        onSelectImage = onSelectImage,
                        onClose = onClose,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        key = { index -> "${illust.id}-full-h-$index" },
                    ) { index ->
                        FullScreenImagePage(
                            url = urls.getOrNull(index),
                            onClose = onClose,
                            enabled = index == pagerState.currentPage,
                            onPrevious = {
                                if (index > 0) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index - 1) }
                                }
                            },
                            onNext = {
                                if (index < urls.lastIndex) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index + 1) }
                                }
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "关闭", tint = Color.White)
                }
                Text(
                    text = illust.title.ifBlank { "#${illust.id}" },
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                )
                Text(
                    text = "${safeIndex + 1} / ${urls.size.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullScreenVerticalPreview(
    illust: Illust,
    selectedIndex: Int,
    measuredRatios: Map<Int, Float>,
    onSelectImage: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val urls = illust.previewUrls()
    val scrollState = rememberScrollState()
    val firstPageTopPaddingPx = with(LocalDensity.current) { PuxivFullScreenTopBarHeight.toPx().roundToInt() }
    var scale by remember(illust.id) { mutableStateOf(1f) }
    var offsetX by remember(illust.id) { mutableStateOf(0f) }
    var offsetY by remember(illust.id) { mutableStateOf(0f) }
    var containerWidthPx by remember(illust.id) { mutableIntStateOf(1) }
    var containerHeightPx by remember(illust.id) { mutableIntStateOf(1) }
    var didPlaceInitialPage by remember(illust.id) { mutableStateOf(false) }
    var isTransforming by remember(illust.id) { mutableStateOf(false) }
    var lastSelectedFromScroll by remember(illust.id) {
        mutableIntStateOf(selectedIndex.coerceIn(0, (urls.size - 1).coerceAtLeast(0)))
    }

    fun pageHeightPx(index: Int): Int {
        val ratio = measuredRatios[index] ?: illust.imagePages.getOrNull(index)?.aspectRatio ?: illust.aspectRatio
        return (containerWidthPx / ratio.coerceIn(0.18f, 4.5f)).roundToInt().coerceAtLeast(1)
    }

    fun pageTopPx(index: Int): Int {
        return firstPageTopPaddingPx + urls.indices.take(index).sumOf(::pageHeightPx)
    }

    fun maxOffsetX(forScale: Float = scale): Float = ((forScale - 1f) * containerWidthPx / 2f).coerceAtLeast(0f)

    fun maxOffsetY(forScale: Float = scale): Float = ((forScale - 1f) * containerHeightPx / 2f).coerceAtLeast(0f)

    fun clampOffsets(forScale: Float = scale) {
        val maxX = maxOffsetX(forScale)
        val maxY = maxOffsetY(forScale)
        offsetX = offsetX.coerceIn(-maxX, maxX)
        offsetY = offsetY.coerceIn(-maxY, maxY)
    }

    LaunchedEffect(urls.size, containerWidthPx) {
        if (
            containerWidthPx > 1 &&
            urls.isNotEmpty() &&
            selectedIndex in urls.indices &&
            !didPlaceInitialPage
        ) {
            val targetOffset = pageTopPx(selectedIndex).let { pageTop ->
                if (selectedIndex == 0) 0 else pageTop
            }
            scrollState.scrollTo(targetOffset)
            didPlaceInitialPage = true
        }
    }
    LaunchedEffect(urls.size, containerWidthPx, containerHeightPx, firstPageTopPaddingPx, measuredRatios) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .collect { offset ->
                if (urls.isNotEmpty()) {
                    val viewportTop = offset
                    val viewportBottom = offset + containerHeightPx
                    val viewportCenter = viewportTop + containerHeightPx / 2
                    val index = urls.indices.maxByOrNull { page ->
                        val pageTop = pageTopPx(page)
                        val pageBottom = pageTop + pageHeightPx(page)
                        val visible = minOf(pageBottom, viewportBottom) - maxOf(pageTop, viewportTop)
                        if (visible > 0) {
                            visible
                        } else {
                            -abs(((pageTop + pageBottom) / 2) - viewportCenter)
                        }
                    } ?: urls.lastIndex
                    lastSelectedFromScroll = index
                    onSelectImage(index)
                }
            }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged {
                containerWidthPx = it.width.coerceAtLeast(1)
                containerHeightPx = it.height.coerceAtLeast(1)
                clampOffsets()
            }
            .pointerInput(illust.id) {
                detectTapGestures(onTap = { onClose() })
            }
            .pointerInput(illust.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        isTransforming = pressed >= 2
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        if (pressed >= 2) {
                            val oldScale = scale
                            val nextScale = (scale * zoomChange).coerceIn(1f, PuxivFullScreenMaxScale)
                            if (zoomChange != 1f) {
                                val centroid = event.calculateCentroid(useCurrent = true)
                                val originX = containerWidthPx / 2f
                                val originY = containerHeightPx / 2f
                                val scaleRatio = nextScale / oldScale
                                offsetX = (offsetX - (centroid.x - originX)) * scaleRatio + (centroid.x - originX)
                                offsetY = (offsetY - (centroid.y - originY)) * scaleRatio + (centroid.y - originY)
                            }
                            scale = nextScale
                            offsetX += panChange.x
                            offsetY += panChange.y
                            if (nextScale <= 1f + PuxivZoomReleaseEpsilon) {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                            clampOffsets(scale)
                            event.changes.forEach { it.consume() }
                        } else if (scale > 1f + PuxivZoomReleaseEpsilon && abs(panChange.x) > abs(panChange.y)) {
                            offsetX += panChange.x
                            if (abs(panChange.y) > 0f) {
                                offsetY += panChange.y
                            }
                            clampOffsets(scale)
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                    isTransforming = false
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                },
        ) {
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState, enabled = !isTransforming),
                ) {
                    urls.forEachIndexed { index, url ->
                        val ratio = measuredRatios[index] ?: illust.imagePages.getOrNull(index)?.aspectRatio ?: illust.aspectRatio
                        GlideImage(
                            url = url,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (index == 0) Modifier.padding(top = PuxivFullScreenTopBarHeight) else Modifier)
                                .aspectRatio(ratio.coerceIn(0.18f, 4.5f)),
                            crop = false,
                            showLoadingBar = true,
                            requestSize = PuxivPreviewImageSize,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenImagePage(
    url: String?,
    onClose: () -> Unit,
    enabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    FullScreenZoomableImage(
        url = url,
        modifier = Modifier.fillMaxSize(),
        onClose = onClose,
        enabled = enabled,
        onPrevious = onPrevious,
        onNext = onNext,
    )
}

@Composable
private fun FullScreenZoomableImage(
    url: String?,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    enabled: Boolean = true,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    var scale by remember(url) { mutableStateOf(1f) }
    var offsetX by remember(url) { mutableStateOf(0f) }
    var offsetY by remember(url) { mutableStateOf(0f) }
    var boundaryPanX by remember(url) { mutableStateOf(0f) }
    var widthPx by remember(url) { mutableIntStateOf(1) }
    var heightPx by remember(url) { mutableIntStateOf(1) }
    val pageTurnThresholdPx = with(LocalDensity.current) { PuxivZoomedPageTurnThresholdDp.dp.toPx() }

    fun maxOffsetX(forScale: Float = scale): Float = ((forScale - 1f) * widthPx / 2f).coerceAtLeast(0f)

    fun maxOffsetY(forScale: Float = scale): Float = ((forScale - 1f) * heightPx / 2f).coerceAtLeast(0f)

    fun clampOffsets(forScale: Float = scale) {
        val maxX = maxOffsetX(forScale)
        val maxY = maxOffsetY(forScale)
        offsetX = offsetX.coerceIn(-maxX, maxX)
        offsetY = offsetY.coerceIn(-maxY, maxY)
    }

    fun handleZoomedBoundaryPageTurn(unusedPanX: Float, panY: Float) {
        if (scale <= 1f + PuxivZoomReleaseEpsilon || unusedPanX == 0f || abs(unusedPanX) <= abs(panY)) {
            boundaryPanX = 0f
            return
        }
        boundaryPanX += unusedPanX
        when {
            boundaryPanX >= pageTurnThresholdPx -> {
                boundaryPanX = 0f
                onPrevious?.invoke()
            }
            boundaryPanX <= -pageTurnThresholdPx -> {
                boundaryPanX = 0f
                onNext?.invoke()
            }
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged {
                widthPx = it.width.coerceAtLeast(1)
                heightPx = it.height.coerceAtLeast(1)
                clampOffsets()
            }
            .pointerInput(url) {
                detectTapGestures(onTap = { onClose() })
            }
            .pointerInput(url, enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        if (pressed >= 2 || scale > 1f) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val previousScale = scale
                            val nextScale = (scale * zoomChange).coerceIn(1f, PuxivFullScreenMaxScale)
                            if (pressed >= 2 && zoomChange != 1f) {
                                val centroid = event.calculateCentroid(useCurrent = true)
                                val originX = widthPx / 2f
                                val originY = heightPx / 2f
                                val scaleRatio = nextScale / previousScale
                                offsetX = (offsetX - (centroid.x - originX)) * scaleRatio + (centroid.x - originX)
                                offsetY = (offsetY - (centroid.y - originY)) * scaleRatio + (centroid.y - originY)
                            }
                            scale = nextScale
                            if (zoomChange != 1f || abs(panChange.x) <= abs(panChange.y)) {
                                boundaryPanX = 0f
                            }
                            if (nextScale <= 1f + PuxivZoomReleaseEpsilon) {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                                boundaryPanX = 0f
                            } else {
                                val previousOffsetX = offsetX
                                offsetY += panChange.y
                                val maxX = maxOffsetX(nextScale)
                                offsetX = (offsetX + panChange.x).coerceIn(-maxX, maxX)
                                val unusedPanX = panChange.x - (offsetX - previousOffsetX)
                                val maxY = maxOffsetY(nextScale)
                                offsetY = offsetY.coerceIn(-maxY, maxY)
                                handleZoomedBoundaryPageTurn(unusedPanX, panChange.y)
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        GlideImage(
            url = url,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
            crop = false,
            showLoadingBar = true,
            requestSize = PuxivPreviewImageSize,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkActionButton(
    isBookmarked: Boolean,
    enabled: Boolean,
    onPublicBookmark: () -> Unit,
    onPrivateBookmark: () -> Unit,
    onDeleteBookmark: () -> Unit,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(48.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (isBookmarked) {
                        onDeleteBookmark()
                    } else {
                        onPublicBookmark()
                    }
                },
                onLongClick = if (isBookmarked) null else onPrivateBookmark,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isBookmarked) "取消收藏" else "收藏",
            tint = if (enabled) contentColor else contentColor.copy(alpha = 0.45f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IllustMeta(
    illust: Illust,
    related: FeedState,
    bookmarkTags: List<String>,
    selectedBookmark: SelectedBookmarkState,
    onBookmarkPublic: (List<String>) -> Unit,
    onBookmarkPrivate: (List<String>) -> Unit,
    onAddBookmarkTags: (List<String>) -> Unit,
    onToggleBookmarkTag: (String) -> Unit,
    onDeleteBookmark: () -> Unit,
    onDownload: () -> Unit,
    actionsEnabled: Boolean,
    onLoadRelated: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onTagClick: (String) -> Unit,
    onOpenAuthor: (Illust) -> Unit,
    modifier: Modifier = Modifier,
) {
    var bookmarkTagInput by remember(illust.id) { mutableStateOf("") }
    var selectedBookmarkTags by remember(illust.id) {
        mutableStateOf<Set<String>>(emptySet())
    }
    val inputTag = bookmarkTagInput.normalizedBookmarkTagOrNull()
    val activeBookmarkTags = selectedBookmark.tags
    val selectedTags = (selectedBookmarkTags + listOfNotNull(inputTag))
        .mapNotNull { it.normalizedBookmarkTagOrNull() }
        .distinctBy { it.lowercase() }
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlideImage(
                    url = illust.authorAvatarUrl,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenAuthor(illust) },
                    crop = true,
                    requestSize = PuxivAvatarImageSize,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenAuthor(illust) },
                ) {
                Text(
                    text = illust.title.ifBlank { "#${illust.id}" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildString {
                        append(illust.authorName.ifBlank { "Unknown artist" })
                        illust.authorAccount.takeIf { it.isNotBlank() }?.let { append(" @").append(it) }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                }
            }
            IconButton(
                onClick = onDownload,
                enabled = actionsEnabled,
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "下载",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BookmarkActionButton(
                isBookmarked = illust.isBookmarked,
                enabled = actionsEnabled,
                onPublicBookmark = { onBookmarkPublic(selectedTags) },
                onPrivateBookmark = { onBookmarkPrivate(selectedTags) },
                onDeleteBookmark = onDeleteBookmark,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MetadataPill(illust.typeLabel)
            MetadataPill("${illust.pageCount}P")
            MetadataPill("${illust.totalBookmarks} 收藏")
            if (illust.totalView > 0) MetadataPill("${illust.totalView} 浏览")
            illust.createDate?.takeIf { it.isNotBlank() }?.take(10)?.let { MetadataPill(it) }
            if (illust.aiType != null && illust.aiType > 1) MetadataPill("AI")
        }
        if (illust.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                illust.tags.forEach { tag ->
                    AssistChip(onClick = { onTagClick(tag) }, label = { Text(tag) })
                }
            }
        }
        if (actionsEnabled || bookmarkTags.isNotEmpty() || bookmarkTagInput.isNotBlank()) {
            BookmarkTagSelector(
                tags = bookmarkTags,
                selectedTags = if (illust.isBookmarked) activeBookmarkTags.toSet() else selectedBookmarkTags,
                input = bookmarkTagInput,
                onInputChange = { bookmarkTagInput = it },
                isBusy = selectedBookmark.isLoading,
                onAddInputTag = {
                    val tag = bookmarkTagInput.normalizedBookmarkTagOrNull() ?: return@BookmarkTagSelector
                    if (illust.isBookmarked) {
                        onAddBookmarkTags(listOf(tag))
                        bookmarkTagInput = ""
                    } else {
                        selectedBookmarkTags = (selectedBookmarkTags + tag).toSet()
                        bookmarkTagInput = ""
                    }
                },
                onToggle = { tag ->
                    if (illust.isBookmarked) {
                        onToggleBookmarkTag(tag)
                        return@BookmarkTagSelector
                    }
                    val selected = selectedBookmarkTags.any { it.equals(tag, ignoreCase = true) }
                    selectedBookmarkTags = if (selected) {
                        selectedBookmarkTags.filterNot { it.equals(tag, ignoreCase = true) }.toSet()
                    } else {
                        selectedBookmarkTags + tag
                    }
                },
            )
        }
        illust.caption
            .plainCaption()
            .takeIf { it.isNotBlank() }
            ?.let { caption ->
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        if (related.items.isNotEmpty() || related.isLoading || related.nextUrl != null) {
            SectionHeader(
                title = "相关作品",
                count = related.items.size,
                isLoading = related.isLoading,
                onRefresh = onLoadRelated,
                actionLabel = if (related.nextUrl != null) "更多" else null,
                showRefresh = related.nextUrl != null || related.isLoading,
            )
            if (related.items.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 2.dp),
                ) {
                    lazyRowItems(related.items.take(12), key = { "related-${it.id}" }) { item ->
                        FeaturedIllustCard(
                            illust = item,
                            onClick = { onOpenPreview(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkTagSelector(
    tags: List<String>,
    selectedTags: Set<String>,
    input: String,
    onInputChange: (String) -> Unit,
    isBusy: Boolean,
    onAddInputTag: () -> Unit,
    onToggle: (String) -> Unit,
) {
    val visibleTags = remember(tags, selectedTags) {
        (tags + selectedTags.filter { selected ->
            tags.none { it.equals(selected, ignoreCase = true) }
        }).distinctBy { it.lowercase() }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Default.LocalOffer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "收藏标签",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isBusy) {
                    Spacer(Modifier.width(2.dp))
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    label = { Text("新标签") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddInputTag() }),
                )
                IconButton(
                    onClick = onAddInputTag,
                    enabled = input.normalizedBookmarkTagOrNull() != null && !isBusy,
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加收藏标签",
                        tint = if (input.normalizedBookmarkTagOrNull() != null && !isBusy) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        },
                    )
                }
            }
            if (visibleTags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(end = 2.dp),
                    modifier = Modifier.height(34.dp),
                ) {
                    lazyRowItems(visibleTags, key = { it.lowercase() }) { tag ->
                        val selected = selectedTags.any { it.equals(tag, ignoreCase = true) }
                        BookmarkSelectionChip(
                            tag = tag,
                            selected = selected,
                            enabled = !isBusy,
                            onClick = { onToggle(tag) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkSelectionChip(
    tag: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.height(32.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun String.normalizedBookmarkTagOrNull(): String? {
    return trim()
        .trimStart('#')
        .replace(Regex("""\s+"""), " ")
        .take(40)
        .takeIf { it.isNotBlank() }
}

@Composable
private fun GlideImage(
    url: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float? = null,
    crop: Boolean = true,
    showLoadingBar: Boolean = false,
    requestSize: Int? = null,
    onDrawableSize: ((Int, Int) -> Unit)? = null,
    animate: Boolean = true,
) {
    val background = MaterialTheme.colorScheme.surfaceVariant
    val imageModifier = if (aspectRatio != null) modifier.aspectRatio(aspectRatio) else modifier
    val imageUrl = url?.takeIf { it.isNotBlank() }
    if (imageUrl != null && imageUrl.shouldUseDrawableGlide()) {
        AnimatedGlideImage(
            url = imageUrl,
            modifier = imageModifier,
            crop = crop,
            showLoadingBar = showLoadingBar,
            requestSize = requestSize,
            onDrawableSize = onDrawableSize,
            animate = animate,
        )
        return
    }
    val cacheKey = remember(imageUrl, requestSize) { PuxivImageCache.key(imageUrl, requestSize) }
    var bitmap by remember(cacheKey) { mutableStateOf(cacheKey?.let(PuxivImageCache::get)) }
    var isLoading by remember(cacheKey, bitmap) { mutableStateOf(cacheKey != null && bitmap == null) }
    val context = LocalContext.current
    val currentOnDrawableSize by rememberUpdatedState(onDrawableSize)

    DisposableEffect(context, cacheKey) {
        val key = cacheKey
        if (key == null || imageUrl == null) {
            bitmap = null
            isLoading = false
            onDispose {}
        } else {
            PuxivImageCache.get(key)?.let { cached ->
                bitmap = cached
                isLoading = false
                currentOnDrawableSize?.invoke(cached.width.coerceAtLeast(1), cached.height.coerceAtLeast(1))
                onDispose {}
            } ?: run {
                isLoading = true
                val listener: (PuxivImageResult) -> Unit = { result ->
                    when (result) {
                        is PuxivImageResult.Success -> {
                            bitmap = result.bitmap
                            isLoading = false
                            currentOnDrawableSize?.invoke(
                                result.bitmap.width.coerceAtLeast(1),
                                result.bitmap.height.coerceAtLeast(1),
                            )
                        }
                        PuxivImageResult.Failed -> {
                            isLoading = false
                        }
                    }
                }
                PuxivImageCache.load(
                    key = key,
                    url = imageUrl,
                    requestSize = requestSize,
                    requestManager = Glide.with(context.applicationContext),
                    listener = listener,
                )
                onDispose {
                    PuxivImageCache.removeListener(key, listener)
                }
            }
        }
    }

    Box(
        modifier = imageModifier
            .background(background)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let { loaded ->
            val imageBitmap = remember(loaded) { loaded.asImageBitmap() }
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
            )
        }
        if (showLoadingBar && isLoading) {
            PreviewLoadingStrip(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AnimatedGlideImage(
    url: String?,
    modifier: Modifier = Modifier,
    crop: Boolean = true,
    showLoadingBar: Boolean = false,
    requestSize: Int? = null,
    onDrawableSize: ((Int, Int) -> Unit)? = null,
    animate: Boolean = true,
) {
    val background = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = background.toArgb()
    val imageUrl = url?.takeIf { it.isNotBlank() }
    var isLoading by remember(imageUrl, requestSize) { mutableStateOf(imageUrl != null) }
    val currentOnDrawableSize by rememberUpdatedState(onDrawableSize)

    Box(
        modifier = modifier
            .background(background)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { context ->
                ImageView(context).apply {
                    setBackgroundColor(backgroundColor)
                    scaleType = if (crop) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = false
                }
            },
            update = { view ->
                view.setBackgroundColor(backgroundColor)
                view.scaleType = if (crop) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
                val loadKey = "${imageUrl.orEmpty()}|${requestSize ?: 0}|$crop|$animate"
                if (view.tag == loadKey) return@AndroidView
                view.tag = loadKey
                if (imageUrl == null) {
                    Glide.with(view).clear(view)
                    view.setImageDrawable(null)
                    isLoading = false
                    return@AndroidView
                }
                isLoading = true
                val isGif = imageUrl.isGifLike(view.context)
                val baseOptions = RequestOptions().let { options ->
                    if (requestSize != null && !imageUrl.isLocalUriGif(view.context)) {
                        options.override(requestSize)
                    } else {
                        options
                    }
                }
                if (isGif && !animate) {
                    Glide.with(view)
                        .asBitmap()
                        .load(imageUrl.glideModel())
                        .apply(baseOptions.disallowHardwareConfig())
                        .listener(object : RequestListener<Bitmap> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                isLoading = false
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap,
                                model: Any?,
                                target: Target<Bitmap>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean,
                            ): Boolean {
                                isLoading = false
                                currentOnDrawableSize?.invoke(
                                    resource.width.coerceAtLeast(1),
                                    resource.height.coerceAtLeast(1),
                                )
                                return false
                            }
                        })
                        .into(view)
                } else if (isGif) {
                    Glide.with(view)
                        .asGif()
                        .load(imageUrl.glideModel())
                        .apply(baseOptions)
                        .listener(object : RequestListener<GifDrawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<GifDrawable>?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                isLoading = false
                                return false
                            }

                            override fun onResourceReady(
                                resource: GifDrawable,
                                model: Any?,
                                target: Target<GifDrawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean,
                            ): Boolean {
                                isLoading = false
                                currentOnDrawableSize?.invoke(
                                    resource.intrinsicWidth.coerceAtLeast(1),
                                    resource.intrinsicHeight.coerceAtLeast(1),
                                )
                                return false
                            }
                        })
                        .into(view)
                } else {
                    Glide.with(view)
                        .load(imageUrl.glideModel())
                        .apply(baseOptions)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                isLoading = false
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean,
                            ): Boolean {
                                isLoading = false
                                currentOnDrawableSize?.invoke(
                                    resource.intrinsicWidth.coerceAtLeast(1),
                                    resource.intrinsicHeight.coerceAtLeast(1),
                                )
                                return false
                            }
                        })
                        .into(view)
                }
            },
        )
        if (showLoadingBar && isLoading) {
            PreviewLoadingStrip(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

private sealed interface PuxivImageResult {
    data class Success(val bitmap: Bitmap) : PuxivImageResult
    data object Failed : PuxivImageResult
}

private object PuxivImageCache {
    private val lock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val memoryCache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }
    private val listeners = mutableMapOf<String, MutableSet<(PuxivImageResult) -> Unit>>()
    private val inFlightTargets = mutableMapOf<String, CustomTarget<Bitmap>>()

    fun key(url: String?, requestSize: Int?): String? {
        val imageUrl = url?.takeIf { it.isNotBlank() } ?: return null
        return "${requestSize ?: 0}|$imageUrl"
    }

    fun get(key: String): Bitmap? = synchronized(lock) { memoryCache.get(key) }

    fun load(
        key: String,
        url: String,
        requestSize: Int?,
        requestManager: RequestManager,
        listener: (PuxivImageResult) -> Unit,
    ) {
        synchronized(lock) {
            memoryCache.get(key)?.let {
                listener(PuxivImageResult.Success(it))
                return
            }
            listeners.getOrPut(key) { linkedSetOf() }.add(listener)
            if (inFlightTargets.containsKey(key)) return
        }

        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?,
            ) {
                val appOwnedBitmap = resource.copy(Bitmap.Config.ARGB_8888, false)
                synchronized(lock) {
                    memoryCache.put(key, appOwnedBitmap)
                }
                finish(key, PuxivImageResult.Success(appOwnedBitmap), requestManager)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                finish(key, PuxivImageResult.Failed, requestManager)
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit
        }
        val shouldStart = synchronized(lock) {
            if (inFlightTargets.containsKey(key)) {
                false
            } else {
                inFlightTargets[key] = target
                true
            }
        }
        if (!shouldStart) return
        val options = if (requestSize != null) {
            RequestOptions().override(requestSize)
        } else {
            RequestOptions()
        }.disallowHardwareConfig()
        requestManager
            .asBitmap()
            .load(url.glideModel())
            .apply(options)
            .into(target)
    }

    fun removeListener(key: String, listener: (PuxivImageResult) -> Unit) {
        synchronized(lock) {
            listeners[key]?.let { activeListeners ->
                activeListeners.remove(listener)
                if (activeListeners.isEmpty()) listeners.remove(key)
            }
        }
    }

    private fun finish(
        key: String,
        result: PuxivImageResult,
        requestManager: RequestManager,
    ) {
        val target: CustomTarget<Bitmap>?
        val activeListeners: List<(PuxivImageResult) -> Unit>
        synchronized(lock) {
            target = inFlightTargets.remove(key)
            activeListeners = listeners.remove(key).orEmpty().toList()
        }
        activeListeners.forEach { it(result) }
        if (target != null) {
            mainHandler.post {
                requestManager.clear(target)
            }
        }
    }
}

private fun String.glideModel(): Any {
    return when {
        startsWith("content://", ignoreCase = true) ||
            startsWith("file://", ignoreCase = true) ||
            startsWith("android.resource://", ignoreCase = true) -> toUri()
        else -> GlideUrl(this)
    }
}

private fun String.shouldUseDrawableGlide(): Boolean {
    val lower = substringBefore('?').substringBefore('#').lowercase()
    return startsWith("content://", ignoreCase = true) ||
        startsWith("file://", ignoreCase = true) ||
        lower.endsWith(".gif") ||
            lower.endsWith(".webp")
}

private fun String.isGifLike(): Boolean {
    return substringBefore('?')
        .substringBefore('#')
        .lowercase()
        .endsWith(".gif")
}

private fun String.isGifLike(context: Context): Boolean {
    if (isGifLike()) return true
    if (!startsWith("content://", ignoreCase = true)) return false
    return runCatching {
        context.contentResolver.getType(toUri()).equals("image/gif", ignoreCase = true)
    }.getOrDefault(false)
}

private fun String.isLocalUriGif(context: Context): Boolean {
    return (
        startsWith("content://", ignoreCase = true) ||
            startsWith("file://", ignoreCase = true)
        ) && isGifLike(context)
}

@Composable
private fun PreviewLoadingStrip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled", "WebViewClientOnReceivedSslError")
@Composable
private fun WebLoginScreen(
    loginUrl: String,
    onBack: () -> Unit,
    onCode: (String, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val fallbackProxy = remember { LocalPixivProxy() }
    val mainExecutor = remember { Executor { command -> Handler(Looper.getMainLooper()).post(command) } }
    var useAppProxy by remember { mutableStateOf(true) }
    var proxyReady by remember { mutableStateOf(false) }
    var proxyStatus by remember { mutableStateOf("内置代理准备中") }
    var proxyDiagnostics by remember { mutableStateOf("等待代理事件") }
    var activeProxyPort by remember { mutableIntStateOf(0) }

    LaunchedEffect(useAppProxy, activeProxyPort, proxyReady) {
        while (isActive) {
            proxyDiagnostics = when {
                !useAppProxy -> "当前为直连模式"
                !proxyReady -> "代理端口准备中"
                else -> buildString {
                    append("127.0.0.1:").append(activeProxyPort)
                    LocalPixivProxy.lastProxyEvent?.takeIf { it.isNotBlank() }?.let {
                        append(" | ").append(it)
                    }
                    LocalPixivProxy.lastProxyError?.takeIf { it.isNotBlank() }?.let {
                        append(" | ERROR ").append(it)
                    }
                }
            }
            delay(300)
        }
    }

    DisposableEffect(loginUrl, useAppProxy) {
        proxyReady = false
        activeProxyPort = 0
        proxyStatus = if (useAppProxy) "内置代理准备中" else "直连准备中"
        if (useAppProxy) {
            fallbackProxy.stop()
            fallbackProxy.start()
            val proxyPort = fallbackProxy.port
            activeProxyPort = proxyPort
            if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                val proxyConfig = ProxyConfig.Builder()
                    .addProxyRule("127.0.0.1:$proxyPort")
                    .addDirect()
                    .build()
                ProxyController.getInstance().setProxyOverride(proxyConfig, mainExecutor) {
                    proxyReady = true
                    proxyStatus = "内置代理已启用"
                }
            } else {
                proxyReady = true
                proxyStatus = "当前 WebView 不支持应用代理，尝试直连"
            }
        } else {
            fallbackProxy.stop()
            if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                ProxyController.getInstance().clearProxyOverride(mainExecutor) {
                    proxyReady = true
                    proxyStatus = "直连模式"
                }
            } else {
                proxyReady = true
                proxyStatus = "直连模式"
            }
        }

        onDispose {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                ProxyController.getInstance().clearProxyOverride(mainExecutor) {}
            }
            fallbackProxy.stop()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("网页登录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: SslError?,
                            ) {
                                if (
                                    useAppProxy &&
                                    fallbackProxy.isProxyCertificate(
                                        error?.url?.toUri()?.host,
                                        error?.certificate?.toX509Certificate(),
                                    )
                                ) {
                                    handler?.proceed()
                                } else {
                                    handler?.cancel()
                                }
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                url?.toUri()?.extractPixivCode()?.let {
                                    onCode(it, useAppProxy)
                                    return
                                }
                                proxyStatus = "加载中"
                                super.onPageStarted(view, url, favicon)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                proxyStatus = "页面已加载"
                                super.onPageFinished(view, url)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                proxyStatus = "加载失败：${error?.description ?: "未知错误"}"
                                super.onReceivedError(view, request, error)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                val code = request.url.extractPixivCode()
                                if (code != null) {
                                    onCode(code, useAppProxy)
                                    return true
                                }
                                return false
                            }
                        }
                    }
                },
                update = { webView ->
                    val loadKey = "${loginUrl.hashCode()}:$useAppProxy"
                    if (proxyReady && loginUrl.isNotBlank() && webView.tag != loadKey) {
                        webView.tag = loadKey
                        webView.loadUrl(loginUrl)
                    }
                },
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = proxyStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (proxyReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = proxyDiagnostics,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    AssistChip(
                        onClick = { useAppProxy = !useAppProxy },
                        label = { Text(if (useAppProxy) "内置代理" else "直连") },
                    )
                    AssistChip(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, loginUrl.toUri()))
                            }.onFailure {
                                proxyStatus = "无法打开外部浏览器"
                            }
                        },
                        enabled = loginUrl.isNotBlank(),
                        label = { Text("浏览器") },
                    )
                }
            }
        }
    }
}

private val RankingMode.label: String
    get() = when (this) {
        RankingMode.Day -> "日榜"
        RankingMode.DayAi -> "AI 日榜"
        RankingMode.Week -> "周榜"
        RankingMode.Month -> "月榜"
        RankingMode.Male -> "男性向"
        RankingMode.Female -> "女性向"
        RankingMode.Rookie -> "新人"
        RankingMode.Original -> "原创"
        RankingMode.DayManga -> "漫画日榜"
        RankingMode.WeekManga -> "漫画周榜"
        RankingMode.MonthManga -> "漫画月榜"
    }

private val SearchSort.label: String
    get() = when (this) {
        SearchSort.DateDesc -> "最新"
        SearchSort.DateAsc -> "最早"
        SearchSort.PopularDesc -> "热度"
        SearchSort.PopularMale -> "男性热门"
        SearchSort.PopularFemale -> "女性热门"
    }

private val SearchTarget.label: String
    get() = when (this) {
        SearchTarget.Partial -> "标签部分一致"
        SearchTarget.Exact -> "标签完全一致"
        SearchTarget.TitleCaption -> "标题/说明"
    }

private val DownloadStatus.label: String
    get() = when (this) {
        DownloadStatus.Queued -> "排队"
        DownloadStatus.Running -> "下载中"
        DownloadStatus.Finished -> "完成"
        DownloadStatus.Failed -> "失败"
    }

private fun DownloadItem.localSavedUris(): List<String> {
    return savedUris.orEmpty()
        .mapNotNull(::nonBlankStringOrNull)
        .ifEmpty { listOfNotNull(savedUri?.takeIf { it.isNotBlank() }) }
}

private fun nonBlankStringOrNull(value: String?): String? {
    return value?.takeIf { it.isNotBlank() }
}

private fun String.downloadMimeType(): String {
    return when (substringAfterLast('.', "").lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "zip" -> "application/zip"
        else -> "image/jpeg"
    }
}

private val Illust.typeLabel: String
    get() = when (type.lowercase()) {
        "ugoira" -> "动画"
        "manga" -> "漫画"
        else -> "插画"
    }

@Composable
private fun UgoiraLoadingProgress(
    loaded: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(min = 160.dp, max = 240.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val progress = if (total > 0) loaded.toFloat() / total.toFloat() else 0f
            CircularProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, strokeWidth = 3.dp)
            Text(
                text = if (total > 0) "加载动画 $loaded / $total" else "加载动画中",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CommentsPanel(
    comments: CommentState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader(
            title = "评论",
            count = comments.items.size,
            isLoading = comments.isLoading,
            onRefresh = {},
            showRefresh = false,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = comments.input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("写一条评论") },
                enabled = !comments.isSending,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            FilledTonalButton(
                onClick = onSend,
                enabled = comments.input.isNotBlank() && !comments.isSending,
            ) {
                Text(if (comments.isSending) "发送中" else "发送")
            }
        }
        comments.error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (comments.isLoading && comments.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else if (comments.items.isEmpty()) {
            Text(
                text = "还没有评论",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            comments.items.take(20).forEach { comment ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            GlideImage(
                                url = comment.userAvatarUrl,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                crop = true,
                                requestSize = PuxivAvatarImageSize,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.userName.ifBlank { "Unknown" },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    comment.date?.takeIf { it.isNotBlank() }?.take(10)?.let {
                                        Text(
                                            text = " · $it",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Text(
                                    text = comment.text.plainCaption(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.plainCaption(): String {
    return replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .trim()
}

private fun Uri.extractPixivCode(): String? {
    val isPixivCallback = scheme == "pixiv" ||
        scheme == "pixiv-inner" ||
        (host == "app-api.pixiv.net" && path == "/web/v1/users/auth/pixiv/callback")
    if (!isPixivCallback) return null
    getQueryParameter("code")?.let { return it }
    val fragmentValue = fragment ?: return null
    return runCatching {
        "https://puxiv.local?$fragmentValue".toUri().getQueryParameter("code")
    }.getOrNull()
}

private fun SslCertificate.toX509Certificate(): X509Certificate? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        x509Certificate?.let { return it }
    }
    return runCatching {
        val bundle = SslCertificate.saveState(this) ?: return null
        val bytes = bundle.getByteArray(SSL_CERTIFICATE_KEY) ?: return null
        CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(bytes)) as? X509Certificate
    }.getOrNull()
}

private const val SSL_CERTIFICATE_KEY = "x509-certificate"
