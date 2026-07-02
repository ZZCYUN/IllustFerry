package JunZi.Pixiv.ui

import JunZi.Pixiv.*

import JunZi.Pixiv.AppScreen
import JunZi.Pixiv.SeriesState
import JunZi.Pixiv.NovelReaderState
import JunZi.Pixiv.AuthorWorkTab
import JunZi.Pixiv.AuthorState
import JunZi.Pixiv.BookmarkFeed
import JunZi.Pixiv.BookmarkKind
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
import JunZi.Pixiv.SearchKind
import JunZi.Pixiv.UgoiraSaveFormat
import JunZi.Pixiv.UserPreviewFeedState
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.BookmarkTag
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustImagePage
import JunZi.Pixiv.data.model.UserPreview
import JunZi.Pixiv.ui.theme.PuxivTheme
import JunZi.Pixiv.ui.theme.puxivColorScheme
import JunZi.Pixiv.data.model.HomeCategory
import JunZi.Pixiv.data.model.NovelDetail
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.network.LocalPixivProxy
import JunZi.Pixiv.data.network.PixivNetworkConfig
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

internal val PuxivSurfaceShape = RoundedCornerShape(8.dp)
internal val PuxivControlShape = RoundedCornerShape(6.dp)
internal val PuxivImageShape = RoundedCornerShape(6.dp)

internal const val PuxivPreviewImageSize = 1600
internal const val PuxivCardImageSize = 520
internal const val PuxivAvatarImageSize = 96
internal const val PuxivTrendImageSize = 360
internal const val PuxivFullScreenMaxScale = 5f
internal const val PuxivZoomReleaseEpsilon = 0.01f
internal const val PuxivZoomedPageTurnThresholdDp = 72f
internal val PuxivFullScreenTopBarHeight = 64.dp
internal const val PixivOfficialWebUrl = "https://www.pixiv.net/"

internal enum class MyTab(val label: String) {
    Works("作品"),
    Bookmarks("收藏"),
    History("历史"),
    Following("关注"),
    Downloads("下载"),
    Upload("投稿"),
}

internal enum class DownloadKind {
    Illust,
    Novel,
}

internal val LocalSeriesOpener = staticCompositionLocalOf<((Illust) -> Unit)?> { null }

@Composable
fun PuxivApp(viewModel: PixivViewModel) {
    val shell by viewModel.shellState.collectAsStateWithLifecycle()
    val auth by viewModel.authState.collectAsStateWithLifecycle()
    val home by viewModel.homeState.collectAsStateWithLifecycle()
    val search by viewModel.searchState.collectAsStateWithLifecycle()
    val preview by viewModel.previewState.collectAsStateWithLifecycle()
    val author by viewModel.authorState.collectAsStateWithLifecycle()
    val novel by viewModel.novelState.collectAsStateWithLifecycle()
    val mine by viewModel.mineState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val transientStateHolder = rememberSaveableStateHolder()
    val mainShellScreen = remember(shell.screen) {
        if (shell.screen in setOf(AppScreen.Home, AppScreen.Search, AppScreen.Me, AppScreen.Settings)) {
            shell.screen
        } else if (shell.screen == AppScreen.Preview) {
            preview.previewReturnScreen
        } else {
            viewModel.backStack.lastOrNull {
                it.screen in setOf(AppScreen.Home, AppScreen.Search, AppScreen.Me, AppScreen.Settings)
            }?.screen ?: AppScreen.Home
        }
    }
    val showMainShell = mainShellScreen in setOf(AppScreen.Home, AppScreen.Search, AppScreen.Me, AppScreen.Settings)
    val showTransientScreen = shell.screen in setOf(
        AppScreen.Login,
        AppScreen.WebLogin,
        AppScreen.WebPixiv,
        AppScreen.Preview,
        AppScreen.Author,
        AppScreen.NovelReader,
        AppScreen.Series,
    )

    PuxivTheme(
        themeMode = settings.themeMode,
        useMaterialYou = settings.useMaterialYou,
        palette = settings.themePalette,
        customPalette = settings.customPalette,
    ) {
        LaunchedEffect(shell.message) {
            val message = shell.message
            if (message != null) {
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessage()
            }
        }

        BackHandler(enabled = shell.screen != AppScreen.Home) {
            if (preview.isFullScreenPreview) {
                viewModel.closeFullScreenPreview()
            } else {
                viewModel.goBack()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val seriesOpener: (Illust) -> Unit = { illust ->
                val sid = illust.seriesId ?: 0L
                if (sid > 0L && !illust.type.equals("novel", ignoreCase = true)) {
                    viewModel.openSeries(
                        seriesId = sid,
                        initialTitle = illust.seriesTitle.orEmpty(),
                    )
                }
            }
            CompositionLocalProvider(LocalSeriesOpener provides seriesOpener) {
            Box(Modifier.fillMaxSize()) {
                if (showMainShell) {
                    MainShell(
                        screen = mainShellScreen,
                        home = home.home,
                        keyword = search.keyword,
                        isBusy = shell.isBusy,
                        rankingMode = home.rankingMode,
                        searchKind = search.searchKind,
                        searchTarget = search.searchTarget,
                        searchSort = search.searchSort,
                        isTrendingLoading = search.isTrendingLoading,
                        trendingTags = search.trendingTags,
                        discover = search.discover,
                        items = search.items,
                        searchUsers = search.searchUsers,
                        isSearchActive = search.isSearchActive,
                        isLoadingMore = search.isLoadingMore,
                        nextUrl = search.nextUrl,
                        session = auth.session,
                        mine = mine.mine,
                        history = mine.history,
                        downloads = mine.downloads.items,
                        diagnostics = home.home.diagnostics,
                        useHostIpRouting = settings.useHostIpRouting,
                        useRemoteImageProxy = settings.useRemoteImageProxy,
                        imageProxyInput = settings.imageProxyInput,
                        saveUgoiraZip = settings.saveUgoiraZip,
                        useThumbnailPreview = settings.useThumbnailPreview,
                        filteredTagsInput = settings.filteredTagsInput,
                        previewSwipeMode = settings.previewSwipeMode,
                        ugoiraSaveFormat = settings.ugoiraSaveFormat,
                        themeMode = settings.themeMode,
                        useMaterialYou = settings.useMaterialYou,
                        themePalette = settings.themePalette,
                        customPalette = settings.customPalette,
                        viewModel = viewModel,
                    )
                }
                if (showTransientScreen) {
                    AnimatedContent(
                        targetState = shell.screen,
                        label = "transient-screen",
                    ) { screen ->
                        when (screen) {
                            AppScreen.Login -> LoginScreen(
                                accessTokenInput = auth.accessTokenInput,
                                refreshTokenInput = auth.refreshTokenInput,
                                authCodeInput = auth.authCodeInput,
                                isBusy = shell.isBusy,
                                loginUrl = auth.loginUrl,
                                onAccessTokenChange = viewModel::updateAccessToken,
                                onRefreshTokenChange = viewModel::updateRefreshToken,
                                onAuthCodeChange = viewModel::updateAuthCode,
                                onSaveToken = viewModel::saveManualToken,
                                onStartWebLogin = viewModel::startWebLogin,
                                onExchangeCode = { viewModel.exchangeAuthCode() },
                            )

                            AppScreen.WebLogin -> WebLoginScreen(
                                loginUrl = auth.loginUrl,
                                onBack = viewModel::goBack,
                                onCode = { code, useNetworkProxy -> viewModel.exchangeAuthCode(code, useNetworkProxy) },
                            )

                            AppScreen.WebPixiv -> WebPixivScreen(
                                onBack = viewModel::goBack,
                            )

                            AppScreen.Preview -> PreviewScreen(
                                preview = preview,
                                shell = shell,
                                settings = settings,
                                onBack = viewModel::goBack,
                                onSelectImage = viewModel::selectImage,
                                bookmarkTags = mine.mine.bookmarkTags(),
                                areBookmarkTagsLoaded = mine.mine.hasBookmarkTagsLoaded,
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
                                key = "author-${author.author.userId ?: 0L}",
                            ) {
                                AuthorScreen(
                                    author = author.author,
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

                            AppScreen.NovelReader -> NovelReaderScreen(
                                illust = preview.selectedIllust,
                                readerState = novel.novelReader,
                                comments = preview.comments,
                                onBack = viewModel::closeNovelReader,
                                onReload = {
                                    preview.selectedIllust?.id?.let { viewModel.loadNovelReader(it) }
                                },
                                onDownload = viewModel::downloadSelectedNovel,
                                onToggleBookmark = viewModel::toggleNovelBookmark,
                                onOpenAuthor = viewModel::openAuthor,
                                onTagClick = viewModel::searchTag,
                                onCommentInputChange = viewModel::updateCommentInput,
                                onSendComment = viewModel::sendComment,
                            )

                            AppScreen.Series -> SeriesScreen(
                                state = novel.series,
                                onBack = viewModel::goBack,
                                onOpenPreview = viewModel::openPreview,
                                onLoadMore = viewModel::loadMoreSeries,
                                onRetry = { viewModel.loadSeries(refresh = true) },
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
    }
}

@Composable
internal fun MainShell(
    screen: AppScreen,
    home: JunZi.Pixiv.HomeState,
    keyword: String,
    isBusy: Boolean,
    rankingMode: RankingMode,
    searchKind: SearchKind,
    searchTarget: SearchTarget,
    searchSort: SearchSort,
    isTrendingLoading: Boolean,
    trendingTags: List<TrendingTag>,
    discover: DiscoverState,
    items: List<Illust>,
    searchUsers: List<UserPreview>,
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
    useThumbnailPreview: Boolean,
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
    val initialMainScreen = if (screen == AppScreen.Settings) AppScreen.Me else screen
    val pagerState = rememberPagerState(
        initialPage = mainScreens.indexOf(initialMainScreen).takeIf { it >= 0 } ?: 0,
    ) { mainScreens.size }
    val coroutineScope = rememberCoroutineScope()
    val currentScreen by rememberUpdatedState(screen)
    var requestedPage by remember {
        mutableIntStateOf(mainScreens.indexOf(initialMainScreen).takeIf { it >= 0 } ?: 0)
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
                        contentPadding = padding,
                        onKeywordChange = viewModel::updateKeyword,
                        onSubmitSearch = viewModel::submitHomeSearch,
                        onLoadHome = { viewModel.loadHome(refresh = true) },
                        onLoadFeed = { feed, refresh -> viewModel.loadHomeFeed(feed, refresh) },
                        onRankingModeChange = viewModel::updateRankingMode,
                        onSelectCategory = viewModel::selectHomeCategory,
                        onOpenPreview = viewModel::openPreview,
                    )

                    AppScreen.Search -> SearchScreen(
                        keyword = keyword,
                        isBusy = isBusy,
                        searchKind = searchKind,
                        searchTarget = searchTarget,
                        searchSort = searchSort,
                        isTrendingLoading = isTrendingLoading,
                        trendingTags = trendingTags,
                        discover = discover,
                        items = items,
                        searchUsers = searchUsers,
                        isSearchActive = isSearchActive,
                        isLoadingMore = isLoadingMore,
                        nextUrl = nextUrl,
                        contentPadding = padding,
                        onKeywordChange = viewModel::updateKeyword,
                        onSearch = viewModel::search,
                        onLoadMore = viewModel::loadMore,
                        onSearchKindChange = viewModel::updateSearchKind,
                        onSearchSortChange = viewModel::updateSearchSort,
                        onSearchTargetChange = viewModel::updateSearchTarget,
                        onTrendingTagClick = viewModel::searchTrendingTag,
                        onReturnToDiscover = viewModel::returnToDiscover,
                        onOpenPreview = viewModel::openPreview,
                        onOpenAuthor = viewModel::openAuthor,
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
                        onLoadBookmarkNovels = { feed, tag -> viewModel.loadMyBookmarkNovels(feed, tag, refresh = true) },
                        onLoadBookmarkTags = { viewModel.loadMyBookmarkTags(refresh = true) },
                        onLoadHistory = { viewModel.loadHistory(refresh = true) },
                        onLoadMoreWorks = { viewModel.loadMyWorks(refresh = false) },
                        onLoadMoreBookmarks = { feed, tag -> viewModel.loadMyBookmarks(feed, tag, refresh = false) },
                        onLoadMoreBookmarkNovels = { feed, tag -> viewModel.loadMyBookmarkNovels(feed, tag, refresh = false) },
                        onLoadMoreHistory = viewModel::loadMoreHistory,
                        onLoadFollowing = { viewModel.loadMyFollowing(it, refresh = true) },
                        onLoadMoreFollowing = { viewModel.loadMyFollowing(it, refresh = false) },
                        onClearHistory = viewModel::clearHistory,
                        onDeleteHistory = viewModel::deleteHistoryItem,
                        onDeleteDownload = viewModel::deleteDownloadItem,
                        onOpenPreview = viewModel::openPreview,
                        onOpenAuthor = viewModel::openAuthor,
                        onUploadIllust = viewModel::uploadIllust,
                        onUploadNovel = viewModel::uploadNovel,
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
                    useThumbnailPreview = useThumbnailPreview,
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
                    onUseThumbnailPreviewChange = viewModel::updateUseThumbnailPreview,
                    onFilteredTagsInputChange = viewModel::updateFilteredTagsInput,
                    onSaveFilteredTags = viewModel::saveFilteredTags,
                    onUgoiraSaveFormatChange = viewModel::updateUgoiraSaveFormat,
                    onOpenWebPixiv = viewModel::openWebPixiv,
                )
            }
        }
    }
}

@Composable
internal fun MainBottomBar(
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

@Composable
internal fun LoginScreen(
    accessTokenInput: String,
    refreshTokenInput: String,
    authCodeInput: String,
    isBusy: Boolean,
    loginUrl: String,
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
                        value = accessTokenInput,
                        onValueChange = onAccessTokenChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Access token") },
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2,
                        maxLines = 4,
                    )
                    OutlinedTextField(
                        value = refreshTokenInput,
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
                        enabled = !isBusy,
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
                        enabled = !isBusy,
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("打开网页登录")
                    }
                    OutlinedTextField(
                        value = authCodeInput,
                        onValueChange = onAuthCodeChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("回调 code") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                    )
                    Button(
                        onClick = onExchangeCode,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy,
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("兑换并登录")
                    }
                }
            }

            if (loginUrl.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    SelectionContainer {
                        Text(
                            text = loginUrl,
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

@Composable
internal fun DownloadsPanel(
    downloads: List<DownloadItem>,
    onOpenPreview: (DownloadItem) -> Unit,
    onDelete: (DownloadItem) -> Unit,
) {
    var selectedKind by remember { mutableStateOf(DownloadKind.Illust) }
    val visibleDownloads = remember(downloads, selectedKind) {
        downloads.filter { item ->
            val isNovel = item.illust?.type.equals("novel", ignoreCase = true)
            when (selectedKind) {
                DownloadKind.Illust -> !isNovel
                DownloadKind.Novel -> isNovel
            }
        }
    }
    val novelCount = downloads.count { it.illust?.type.equals("novel", ignoreCase = true) }
    val illustCount = downloads.size - novelCount
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
                        text = "作品可本地预览，小说保存为文本文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val running = downloads.count { it.status == DownloadStatus.Running || it.status == DownloadStatus.Queued }
                if (running > 0) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = selectedKind == DownloadKind.Illust,
                    onClick = { selectedKind = DownloadKind.Illust },
                    label = { Text("作品 $illustCount") },
                )
                FilterChip(
                    selected = selectedKind == DownloadKind.Novel,
                    onClick = { selectedKind = DownloadKind.Novel },
                    label = { Text("小说 $novelCount") },
                )
            }
            if (visibleDownloads.isEmpty()) {
                EmptyStateCard(
                    if (selectedKind == DownloadKind.Novel) "还没有下载小说" else "还没有下载作品",
                    Modifier.fillMaxWidth(),
                )
            } else {
                visibleDownloads.forEach { item ->
                    val isNovel = item.illust?.type.equals("novel", ignoreCase = true)
                    val savedUris = item.localSavedUris()
                    val canOpen = !isNovel && item.status == DownloadStatus.Finished && savedUris.any { it.isNotBlank() }
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
                                if (!isNovel && !previewUrl.isNullOrBlank()) {
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
                                            DownloadStatus.Finished -> if (isNovel) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Save
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
                                            DownloadStatus.Finished -> if (isNovel) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Save
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
                                        isNovel -> "小说文本"
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
internal fun UploadIllustPanel(
    isUploading: Boolean,
    uploadStatus: String?,
    onUploadIllust: (String, String, List<String>, String, Int, String, Boolean, Int, List<Uri>) -> Unit,
    onUploadNovel: (String, String, String, List<String>, Int, String, Boolean, Int, Boolean, Uri?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("illust") }
    var novelText by remember { mutableStateOf("") }
    var visibilityScope by remember { mutableIntStateOf(1) }
    var xRestrict by remember { mutableStateOf("none") }
    var isSexual by remember { mutableStateOf(false) }
    var illustAiType by remember { mutableIntStateOf(1) }
    var isOriginal by remember { mutableStateOf(true) }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var novelCover by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = uris.take(20)
    }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        novelCover = uri
    }

    val typeOptions = listOf(
        "illust" to "插画",
        "manga" to "漫画",
        "novel" to "小说",
    )
    val selectedTypeIndex = typeOptions.indexOfFirst { it.first == type }.takeIf { it >= 0 } ?: 0
    val isNovel = type == "novel"

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
            PrimaryTabRow(
                selectedTabIndex = selectedTypeIndex,
                containerColor = Color.Transparent,
            ) {
                typeOptions.forEachIndexed { index, (value, label) ->
                    Tab(
                        selected = selectedTypeIndex == index,
                        onClick = { type = value },
                        text = { Text(label) },
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
                label = { Text(if (isNovel) "简介" else "说明") },
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
            if (isNovel) {
                OutlinedTextField(
                    value = novelText,
                    onValueChange = { novelText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("正文（支持 [newpage] 分章节）") },
                    minLines = 8,
                    shape = RoundedCornerShape(8.dp),
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                if (isNovel) {
                    FilterChip(selected = isOriginal, onClick = { isOriginal = !isOriginal }, label = { Text("原创") })
                }
            }
            if (isNovel) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalButton(
                        onClick = { coverPicker.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading,
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (novelCover == null) "选择封面（可选）" else "已选择封面")
                    }
                    Button(
                        onClick = {
                            val tags = tagsText.split(Regex("""[\s,，]+"""))
                            onUploadNovel(
                                title,
                                caption,
                                novelText,
                                tags,
                                visibilityScope,
                                xRestrict,
                                isSexual,
                                illustAiType,
                                isOriginal,
                                novelCover,
                            )
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
                        Text("提交小说")
                    }
                }
            } else {
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

internal val PuxivThemeMode.label: String
    get() = when (this) {
        PuxivThemeMode.System -> "跟随系统"
        PuxivThemeMode.Light -> "亮色"
        PuxivThemeMode.Dark -> "暗色"
    }

internal val PuxivThemePalette.label: String
    get() = when (this) {
        PuxivThemePalette.Puxiv -> "Puxiv 蓝"
        PuxivThemePalette.Sakura -> "樱花"
        PuxivThemePalette.Mint -> "薄荷"
        PuxivThemePalette.Violet -> "紫罗兰"
        PuxivThemePalette.Amber -> "琥珀"
        PuxivThemePalette.Slate -> "青石"
        PuxivThemePalette.Custom -> "自定义"
    }

internal val UI_HEX_COLOR_PATTERN = Regex("""#[0-9A-Fa-f]{6}""")

internal data class OpenSourceLibrary(
    val name: String,
    val license: String,
    val licenseUrl: String,
)

internal val OpenSourceLibraries = listOf(
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

internal sealed interface NovelBlock {
    data class Paragraph(val text: AnnotatedString) : NovelBlock
    data class Image(val url: String?, val rawTag: String) : NovelBlock
    data class Chapter(val title: String) : NovelBlock
    data object Blank : NovelBlock
}

internal val BookmarkFeed.label: String
    get() = when (this) {
        BookmarkFeed.Public -> "收藏作品"
        BookmarkFeed.Private -> "私密收藏"
    }

internal val BookmarkFeed.icon: ImageVector
    get() = when (this) {
        BookmarkFeed.Public -> Icons.Default.Favorite
        BookmarkFeed.Private -> Icons.Default.Lock
    }

internal val FollowUserFeed.label: String
    get() = when (this) {
        FollowUserFeed.Public -> "公开关注"
        FollowUserFeed.Private -> "悄悄关注"
    }

internal val FollowUserFeed.icon: ImageVector
    get() = when (this) {
        FollowUserFeed.Public -> Icons.Default.AccountCircle
        FollowUserFeed.Private -> Icons.Default.Lock
    }

internal val DiscoverFeed.label: String
    get() = when (this) {
        DiscoverFeed.Public -> "公开关注"
        DiscoverFeed.Private -> "悄悄关注"
    }

internal val DiscoverFeed.icon: ImageVector
    get() = when (this) {
        DiscoverFeed.Public -> Icons.Default.AccountCircle
        DiscoverFeed.Private -> Icons.Default.Lock
    }

internal sealed interface PuxivImageResult {
    data class Success(val bitmap: Bitmap) : PuxivImageResult
    data object Failed : PuxivImageResult
}

internal object PuxivImageCache {
    internal val lock = Any()
    internal val mainHandler = Handler(Looper.getMainLooper())

    internal val memoryCache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }
    internal val listeners = mutableMapOf<String, MutableSet<(PuxivImageResult) -> Unit>>()
    internal val inFlightTargets = mutableMapOf<String, CustomTarget<Bitmap>>()

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

    internal fun finish(
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

internal val RankingMode.label: String
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
        RankingMode.DayNovel -> "小说日榜"
        RankingMode.DayAiNovel -> "AI 小说日榜"
        RankingMode.WeekNovel -> "小说周榜"
        RankingMode.MonthNovel -> "小说月榜"
        RankingMode.WeekRookieNovel -> "小说新人"
        RankingMode.MaleNovel -> "小说男性向"
        RankingMode.FemaleNovel -> "小说女性向"
    }

internal val SearchSort.label: String
    get() = when (this) {
        SearchSort.DateDesc -> "最新"
        SearchSort.DateAsc -> "最早"
        SearchSort.PopularDesc -> "热度"
        SearchSort.PopularMale -> "男性热门"
        SearchSort.PopularFemale -> "女性热门"
    }

internal val SearchTarget.label: String
    get() = when (this) {
        SearchTarget.Partial -> "标签部分一致"
        SearchTarget.Exact -> "标签完全一致"
        SearchTarget.TitleCaption -> "标题/说明"
    }

internal val SearchKind.label: String
    get() = when (this) {
        SearchKind.Illust -> "作品"
        SearchKind.Novel -> "小说"
        SearchKind.User -> "作者"
    }

internal val DownloadStatus.label: String
    get() = when (this) {
        DownloadStatus.Queued -> "排队"
        DownloadStatus.Running -> "下载中"
        DownloadStatus.Finished -> "完成"
        DownloadStatus.Failed -> "失败"
    }
