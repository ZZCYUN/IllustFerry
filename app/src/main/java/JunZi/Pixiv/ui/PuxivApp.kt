package JunZi.Pixiv.ui

import JunZi.Pixiv.AppScreen
import JunZi.Pixiv.CommentState
import JunZi.Pixiv.DiscoverFeed
import JunZi.Pixiv.DiscoverState
import JunZi.Pixiv.DiagnosticsState
import JunZi.Pixiv.DownloadItem
import JunZi.Pixiv.DownloadStatus
import JunZi.Pixiv.FeedState
import JunZi.Pixiv.HomeFeed
import JunZi.Pixiv.MyState
import JunZi.Pixiv.PixivViewModel
import JunZi.Pixiv.PreviewSwipeMode
import JunZi.Pixiv.PuxivUiState
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustImagePage
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.network.LocalPixivProxy
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
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
import android.widget.Toast
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
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

private enum class MyTab(val label: String) {
    Works("作品"),
    Bookmarks("收藏"),
    Downloads("下载"),
    Upload("投稿"),
}

@Composable
fun PuxivApp(viewModel: PixivViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val mainShellScreen = if (state.screen == AppScreen.Preview) state.previewReturnScreen else state.screen
    val showMainShell = mainShellScreen in setOf(AppScreen.Home, AppScreen.Search, AppScreen.Me, AppScreen.Settings)
    val showTransientScreen = state.screen in setOf(AppScreen.Login, AppScreen.WebLogin, AppScreen.Preview)

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
                    useWaterfall = state.useWaterfall,
                    isLoadingMore = state.isLoadingMore,
                    nextUrl = state.nextUrl,
                    session = state.session,
                    mine = state.mine,
                    downloads = state.downloads.items,
                    diagnostics = state.home.diagnostics,
                    useHostIpRouting = state.useHostIpRouting,
                    useRemoteImageProxy = state.useRemoteImageProxy,
                    imageProxyInput = state.imageProxyInput,
                    previewSwipeMode = state.previewSwipeMode,
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
                            onToggleBookmark = viewModel::toggleSelectedBookmark,
                            onLoadRelated = { viewModel.loadRelated(refresh = false) },
                            onOpenPreview = viewModel::openPreview,
                            onOpenFullScreen = viewModel::openFullScreenPreview,
                            onCloseFullScreen = viewModel::closeFullScreenPreview,
                            onTagClick = viewModel::searchTag,
                            onDownload = viewModel::downloadSelectedIllust,
                            onCommentInputChange = viewModel::updateCommentInput,
                            onSendComment = viewModel::sendComment,
                        )

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
    useWaterfall: Boolean,
    isLoadingMore: Boolean,
    nextUrl: String?,
    session: AuthSession?,
    mine: MyState,
    downloads: List<DownloadItem>,
    diagnostics: DiagnosticsState,
    useHostIpRouting: Boolean,
    useRemoteImageProxy: Boolean,
    imageProxyInput: String,
    previewSwipeMode: PreviewSwipeMode,
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
                        useWaterfall = useWaterfall,
                        isLoadingMore = isLoadingMore,
                        nextUrl = nextUrl,
                        contentPadding = padding,
                        onKeywordChange = viewModel::updateKeyword,
                        onSearch = viewModel::search,
                        onLoadMore = viewModel::loadMore,
                        onSearchSortChange = viewModel::updateSearchSort,
                        onSearchTargetChange = viewModel::updateSearchTarget,
                        onPopularPreview = viewModel::loadPopularPreview,
                        onTrendingTagClick = viewModel::searchTrendingTag,
                        onToggleLayout = viewModel::toggleLayout,
                        onOpenPreview = viewModel::openPreview,
                        onRefreshDiscover = { viewModel.loadDiscover(refresh = true) },
                        onLoadMoreDiscover = viewModel::loadDiscoverFeed,
                    )

                    AppScreen.Me -> MeScreen(
                        isActive = selectedMainScreen == AppScreen.Me,
                        session = session,
                        mine = mine,
                        downloads = downloads,
                        contentPadding = padding,
                        onLoadMine = { viewModel.loadMine(refresh = true) },
                        onLoadWorks = { viewModel.loadMyWorks(refresh = true) },
                        onLoadBookmarks = { viewModel.loadMyBookmarks(refresh = true) },
                        onLoadMoreWorks = { viewModel.loadMyWorks(refresh = false) },
                        onLoadMoreBookmarks = { viewModel.loadMyBookmarks(refresh = false) },
                        onOpenPreview = viewModel::openPreview,
                        onUploadIllust = viewModel::uploadIllust,
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
                    previewSwipeMode = previewSwipeMode,
                    contentPadding = padding,
                    onRefreshDns = { viewModel.refreshDns(showMessage = true) },
                    onDiagnostics = viewModel::runDiagnostics,
                    onHostIpRoutingEnabledChange = viewModel::updateHostIpRoutingEnabled,
                    onPreviewSwipeModeChange = viewModel::updatePreviewSwipeMode,
                    onImageProxyEnabledChange = viewModel::updateImageProxyEnabled,
                    onImageProxyInputChange = viewModel::updateImageProxyInput,
                    onSaveImageProxy = viewModel::saveImageProxyOrigin,
                    onResetImageProxy = viewModel::resetImageProxyOrigin,
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
            Text(
                text = "$count works",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    downloads: List<DownloadItem>,
    contentPadding: PaddingValues,
    onLoadMine: () -> Unit,
    onLoadWorks: () -> Unit,
    onLoadBookmarks: () -> Unit,
    onLoadMoreWorks: () -> Unit,
    onLoadMoreBookmarks: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onUploadIllust: (String, String, List<String>, String, Int, String, Boolean, Int, List<Uri>) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onOpenLogin: () -> Unit,
    onStartWebLogin: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(MyTab.Works) }
    var showLoginDialog by remember { mutableStateOf(false) }
    val gridState = rememberLazyStaggeredGridState()
    val decorativeFeedSemantics = rememberDecorativeFeedSemantics()

    LaunchedEffect(isActive, session?.userId, mine.hasLoaded) {
        if (isActive && session?.userId != null && !mine.hasLoaded) {
            onLoadMine()
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
                actions = {
                    if (session == null) {
                        TextButton(onClick = { showLoginDialog = true }) {
                            Text("登录")
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
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
                    worksCount = mine.works.items.size,
                    bookmarksCount = mine.bookmarks.items.size,
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
                        if (session == null) {
                            showLoginDialog = true
                            return@MyTabRow
                        }
                        selectedTab = tab
                        when (tab) {
                            MyTab.Works -> if (mine.works.items.isEmpty()) onLoadWorks()
                            MyTab.Bookmarks -> if (mine.bookmarks.items.isEmpty()) onLoadBookmarks()
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
                    val feed = mine.bookmarks
                    mineFeedMessages(feed, "还没有加载到收藏作品")
                    items(
                        feed.items,
                        key = { "mine-bookmark-${it.id}" },
                        contentType = { "illust-card" },
                    ) { illust ->
                        IllustCard(
                            illust = illust,
                            clearSemantics = false,
                            onClick = onOpenPreview,
                        )
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        MinePagingFooter(feed = feed, onLoadMore = onLoadMoreBookmarks)
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

                MyTab.Downloads -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        DownloadsPanel(downloads = downloads)
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
    worksCount: Int,
    bookmarksCount: Int,
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
                MetadataPill("$worksCount 作品")
                MetadataPill("$bookmarksCount 收藏")
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
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("刷新")
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
                FilledTonalButton(
                    onClick = if (session == null) onLoginClick else onLogout,
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
            modifier = Modifier.padding(8.dp),
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
private fun DownloadsPanel(
    downloads: List<DownloadItem>,
) {
    val context = LocalContext.current
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
                        text = "保存到系统 Downloads/IllustFerry",
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
                Text(
                    text = "还没有下载内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                downloads.forEach { item ->
                    val canOpen = item.status == DownloadStatus.Finished && !item.savedUri.isNullOrBlank()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canOpen) {
                                val uri = item.savedUri?.toUri() ?: return@clickable
                                val intent = Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(uri, item.fileName.downloadMimeType())
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                runCatching {
                                    context.startActivity(intent)
                                }.recoverCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                                }.onFailure {
                                    val message = if (it is ActivityNotFoundException) "没有可预览此文件的应用" else "无法打开下载内容"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = item.detail.ifBlank { item.status.label },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
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
                label = { Text("标签，用空格或逗号分隔") },
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
    previewSwipeMode: PreviewSwipeMode,
    contentPadding: PaddingValues,
    onRefreshDns: () -> Unit,
    onDiagnostics: () -> Unit,
    onHostIpRoutingEnabledChange: (Boolean) -> Unit,
    onPreviewSwipeModeChange: (PreviewSwipeMode) -> Unit,
    onImageProxyEnabledChange: (Boolean) -> Unit,
    onImageProxyInputChange: (String) -> Unit,
    onSaveImageProxy: () -> Unit,
    onResetImageProxy: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("设置")
                        Text(
                            text = "网络路由、图片代理与预览方式",
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
        }
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
    useWaterfall: Boolean,
    isLoadingMore: Boolean,
    nextUrl: String?,
    contentPadding: PaddingValues,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onSearchSortChange: (SearchSort) -> Unit,
    onSearchTargetChange: (SearchTarget) -> Unit,
    onPopularPreview: () -> Unit,
    onTrendingTagClick: (TrendingTag) -> Unit,
    onToggleLayout: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onRefreshDiscover: () -> Unit,
    onLoadMoreDiscover: (DiscoverFeed, Boolean) -> Unit,
) {
    val searchGridState = rememberLazyStaggeredGridState()
    val decorativeFeedSemantics = rememberDecorativeFeedSemantics()
    val resultGridState = rememberLazyStaggeredGridState()
    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("发现")
                        Text(
                            text = "${discover.publicWorks.items.size + discover.privateWorks.items.size} 关注作品 · ${items.size} 搜索结果",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleLayout) {
                        Icon(
                            imageVector = if (useWaterfall) Icons.Default.GridView else Icons.Default.ViewAgenda,
                            contentDescription = "切换布局",
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        if (items.isEmpty() && !isBusy) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
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
                    onPopularPreview = onPopularPreview,
                    onTrendingTagClick = onTrendingTagClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
                DiscoverContent(
                    discover = discover,
                    trendingTags = trendingTags,
                    onRefresh = onRefreshDiscover,
                    onLoadMore = onLoadMoreDiscover,
                    onOpenPreview = onOpenPreview,
                    modifier = Modifier.weight(1f),
                )
            }
        } else if (useWaterfall) {
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
                            onPopularPreview = onPopularPreview,
                            onTrendingTagClick = onTrendingTagClick,
                        )
                    }
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
                            onPopularPreview = onPopularPreview,
                            onTrendingTagClick = onTrendingTagClick,
                        )
                    }
                    items(
                        items,
                        key = { it.id },
                        contentType = { "illust-card" },
                    ) { illust ->
                        IllustCard(
                            illust = illust,
                            forceSquare = true,
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
    onPopularPreview: () -> Unit,
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
                OutlinedTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("标签、标题、作者") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                )
                IconButton(
                    onClick = onSearch,
                    enabled = !isBusy,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
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
                SearchSort.values().forEach { sort ->
                    FilterChip(
                        selected = searchSort == sort,
                        onClick = { onSearchSortChange(sort) },
                        label = { Text(sort.label) },
                    )
                }
                FilledTonalButton(
                    onClick = onPopularPreview,
                    enabled = keyword.isNotBlank() && !isBusy,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Text("热门预览")
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
private fun DiscoverContent(
    discover: DiscoverState,
    trendingTags: List<TrendingTag>,
    onRefresh: () -> Unit,
    onLoadMore: (DiscoverFeed, Boolean) -> Unit,
    onOpenPreview: (Illust) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyStaggeredGridState()
    val decorativeFeedSemantics = rememberDecorativeFeedSemantics()
    Box(
        modifier = modifier.feedSemanticsBoundary(decorativeFeedSemantics),
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
                SectionHeader(
                    title = "关注作品",
                    count = discover.publicWorks.items.size,
                    isLoading = discover.publicWorks.isLoading,
                    onRefresh = onRefresh,
                    actionLabel = "刷新",
                    showRefresh = true,
                )
            }
            discoverFeedMessages(discover.publicWorks, "还没有加载到关注用户作品")
            items(
                discover.publicWorks.items,
                key = { "discover-public-${it.id}" },
                contentType = { "illust-card" },
            ) { illust ->
                IllustCard(
                    illust = illust,
                    clearSemantics = decorativeFeedSemantics,
                    onClick = onOpenPreview,
                )
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                DiscoverPagingFooter(
                    feed = discover.publicWorks,
                    onLoadMore = { onLoadMore(DiscoverFeed.Public, false) },
                )
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                SectionHeader(
                    title = "悄悄关注",
                    count = discover.privateWorks.items.size,
                    isLoading = discover.privateWorks.isLoading,
                    onRefresh = onRefresh,
                    actionLabel = "刷新",
                    showRefresh = true,
                )
            }
            discoverFeedMessages(discover.privateWorks, "还没有加载到悄悄关注作品")
            items(
                discover.privateWorks.items,
                key = { "discover-private-${it.id}" },
                contentType = { "illust-card" },
            ) { illust ->
                IllustCard(
                    illust = illust,
                    clearSemantics = decorativeFeedSemantics,
                    onClick = onOpenPreview,
                )
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                DiscoverPagingFooter(
                    feed = discover.privateWorks,
                    onLoadMore = { onLoadMore(DiscoverFeed.Private, false) },
                )
            }
            if (trendingTags.isEmpty() && discover.publicWorks.items.isEmpty() && discover.privateWorks.items.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptySearch(Modifier.fillMaxWidth())
                }
            }
        }
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
    onToggleBookmark: () -> Unit,
    onLoadRelated: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onOpenFullScreen: (Int) -> Unit,
    onCloseFullScreen: () -> Unit,
    onTagClick: (String) -> Unit,
    onDownload: () -> Unit,
    onCommentInputChange: (String) -> Unit,
    onSendComment: () -> Unit,
) {
    val illust = state.selectedIllust
    val previewStateHolder = rememberSaveableStateHolder()
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
            var measuredRatios by remember(illust.id) { mutableStateOf<Map<Int, Float>>(emptyMap()) }
            fun updateMeasuredRatio(index: Int, width: Int, height: Int) {
                val ratio = (width.toFloat() / height.coerceAtLeast(1)).coerceIn(0.18f, 4.5f)
                if (measuredRatios[index] != ratio) {
                    measuredRatios = measuredRatios + (index to ratio)
                }
            }

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
                        onSelectImage = onSelectImage,
                        onOpenFullScreen = onOpenFullScreen,
                        onImageMeasured = { index, width, height ->
                            updateMeasuredRatio(index, width, height)
                        },
                        footer = {
                            IllustMeta(
                                illust = illust,
                                related = state.related,
                                onToggleBookmark = onToggleBookmark,
                                onDownload = onDownload,
                                actionsEnabled = !state.isBusy,
                                onLoadRelated = onLoadRelated,
                                onOpenPreview = onOpenPreview,
                                onTagClick = onTagClick,
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
                                onToggleBookmark = onToggleBookmark,
                                onDownload = onDownload,
                                actionsEnabled = !state.isBusy,
                                onLoadRelated = onLoadRelated,
                                onOpenPreview = onOpenPreview,
                                onTagClick = onTagClick,
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
            onSelectImage = onSelectImage,
            onClose = onCloseFullScreen,
        )
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

@Composable
private fun IllustCard(
    illust: Illust,
    forceSquare: Boolean = false,
    clearSemantics: Boolean = false,
    onClick: (Illust) -> Unit,
) {
    val cardModifier = if (clearSemantics) {
        Modifier.clearAndSetSemantics {}
    } else {
        Modifier
    }
    ElevatedCard(
        onClick = { onClick(illust) },
        modifier = cardModifier,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalImagePreview(
    illust: Illust,
    imageIndex: Int,
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
                    .clickable { onOpenFullScreen(index) },
                crop = false,
                showLoadingBar = true,
                requestSize = PuxivPreviewImageSize,
                onDrawableSize = { width, height -> onImageMeasured(index, width, height) },
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
                    .clickable { onOpenFullScreen(index) },
                crop = false,
                showLoadingBar = true,
                requestSize = PuxivPreviewImageSize,
                onDrawableSize = { width, height -> onImageMeasured(index, width, height) },
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
    onSelectImage: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val urls = illust.previewUrls()
    val scrollState = rememberScrollState()
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
        val ratio = illust.imagePages.getOrNull(index)?.aspectRatio ?: illust.aspectRatio
        return (containerWidthPx / ratio.coerceIn(0.18f, 4.5f)).roundToInt().coerceAtLeast(1)
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
            val targetOffset = urls.indices.take(selectedIndex).sumOf(::pageHeightPx)
            scrollState.scrollTo(targetOffset)
            didPlaceInitialPage = true
        }
    }
    LaunchedEffect(urls.size, containerWidthPx) {
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
                        val ratio = illust.imagePages.getOrNull(index)?.aspectRatio ?: illust.aspectRatio
                        GlideImage(
                            url = url,
                            modifier = Modifier
                                .fillMaxWidth()
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

@Composable
private fun IllustMeta(
    illust: Illust,
    related: FeedState,
    onToggleBookmark: () -> Unit,
    onDownload: () -> Unit,
    actionsEnabled: Boolean,
    onLoadRelated: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
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
            IconButton(
                onClick = onToggleBookmark,
                enabled = actionsEnabled,
            ) {
                Icon(
                    imageVector = if (illust.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (illust.isBookmarked) "取消收藏" else "收藏",
                    tint = if (illust.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
private fun GlideImage(
    url: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float? = null,
    crop: Boolean = true,
    showLoadingBar: Boolean = false,
    requestSize: Int? = null,
    onDrawableSize: ((Int, Int) -> Unit)? = null,
) {
    val background = MaterialTheme.colorScheme.surfaceVariant
    val imageModifier = if (aspectRatio != null) modifier.aspectRatio(aspectRatio) else modifier
    val imageUrl = url?.takeIf { it.isNotBlank() }
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
            .load(GlideUrl(url))
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
