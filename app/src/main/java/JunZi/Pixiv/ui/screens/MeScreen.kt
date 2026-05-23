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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeScreen(
    isActive: Boolean,
    session: AuthSession?,
    mine: MyState,
    history: HistoryState,
    downloads: List<DownloadItem>,
    contentPadding: PaddingValues,
    onLoadMine: () -> Unit,
    onLoadWorks: () -> Unit,
    onLoadBookmarks: (BookmarkFeed, String?) -> Unit,
    onLoadBookmarkNovels: (BookmarkFeed, String?) -> Unit,
    onLoadBookmarkTags: () -> Unit,
    onLoadHistory: () -> Unit,
    onLoadMoreWorks: () -> Unit,
    onLoadMoreBookmarks: (BookmarkFeed, String?) -> Unit,
    onLoadMoreBookmarkNovels: (BookmarkFeed, String?) -> Unit,
    onLoadMoreHistory: () -> Unit,
    onLoadFollowing: (FollowUserFeed) -> Unit,
    onLoadMoreFollowing: (FollowUserFeed) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteHistory: (Illust) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onOpenAuthor: (UserPreview) -> Unit,
    onUploadIllust: (String, String, List<String>, String, Int, String, Boolean, Int, List<Uri>) -> Unit,
    onUploadNovel: (String, String, String, List<String>, Int, String, Boolean, Int, Boolean, Uri?) -> Unit,
    onOpenDownloadPreview: (DownloadItem) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onOpenLogin: () -> Unit,
    onStartWebLogin: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(MyTab.Works) }
    var selectedBookmarkFeed by remember { mutableStateOf(BookmarkFeed.Public) }
    var selectedBookmarkKind by remember { mutableStateOf(BookmarkKind.Illust) }
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
            if (selectedTab == MyTab.Bookmarks && session != null && selectedBookmarkKind == BookmarkKind.Illust) {
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
                                when (selectedBookmarkKind) {
                                    BookmarkKind.Illust -> {
                                        val feed = mine.bookmarkFeed(selectedBookmarkFeed)
                                        if (feed.items.isEmpty() || feed.queryTag != selectedBookmarkTag) {
                                            onLoadBookmarks(selectedBookmarkFeed, selectedBookmarkTag)
                                        }
                                    }
                                    BookmarkKind.Novel -> {
                                        val feed = mine.bookmarkNovelsFeed(selectedBookmarkFeed)
                                        if (feed.items.isEmpty()) {
                                            onLoadBookmarkNovels(selectedBookmarkFeed, null)
                                        }
                                    }
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
                            selectedKind = selectedBookmarkKind,
                            selectedTag = selectedBookmarkTag,
                            onSelectFeed = { feed ->
                                selectedBookmarkFeed = feed
                                if (
                                    !mine.hasBookmarkTagsLoaded &&
                                    !mine.isBookmarkTagsLoading
                                ) {
                                    onLoadBookmarkTags()
                                }
                                when (selectedBookmarkKind) {
                                    BookmarkKind.Illust -> onLoadBookmarks(feed, selectedBookmarkTag)
                                    BookmarkKind.Novel -> onLoadBookmarkNovels(feed, null)
                                }
                            },
                            onSelectKind = { kind ->
                                selectedBookmarkKind = kind
                                when (kind) {
                                    BookmarkKind.Illust -> {
                                        val feed = mine.bookmarkFeed(selectedBookmarkFeed)
                                        if (feed.items.isEmpty() || feed.queryTag != selectedBookmarkTag) {
                                            onLoadBookmarks(selectedBookmarkFeed, selectedBookmarkTag)
                                        }
                                    }
                                    BookmarkKind.Novel -> {
                                        val feed = mine.bookmarkNovelsFeed(selectedBookmarkFeed)
                                        if (feed.items.isEmpty()) {
                                            onLoadBookmarkNovels(selectedBookmarkFeed, null)
                                        }
                                    }
                                }
                            },
                            onSelectTag = { tag ->
                                selectedBookmarkTag = tag
                                onLoadBookmarks(selectedBookmarkFeed, tag)
                            },
                            onRefreshTags = onLoadBookmarkTags,
                        )
                    }
                    when (selectedBookmarkKind) {
                        BookmarkKind.Illust -> {
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
                        BookmarkKind.Novel -> {
                            val feed = mine.bookmarkNovelsFeed(selectedBookmarkFeed)
                            mineFeedMessages(
                                feed = feed,
                                emptyText = if (selectedBookmarkFeed == BookmarkFeed.Private) {
                                    "还没有私密收藏的小说"
                                } else {
                                    "还没有收藏的小说"
                                },
                            )
                            items(
                                feed.items,
                                key = { "mine-bookmark-novel-${selectedBookmarkFeed.name}-${it.id}" },
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
                                    onLoadMore = { onLoadMoreBookmarkNovels(selectedBookmarkFeed, null) },
                                )
                            }
                        }
                    }
                }

                MyTab.Upload -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        UploadIllustPanel(
                            isUploading = mine.isUploading,
                            uploadStatus = mine.uploadStatus,
                            onUploadIllust = onUploadIllust,
                            onUploadNovel = onUploadNovel,
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
                                onDeleteHistory(entry.illust)
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

internal fun LazyStaggeredGridScope.mineFeedMessages(
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
internal fun MyHeader(
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
internal fun MyTabRow(
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
internal fun BookmarkCollectionPanel(
    mine: MyState,
    selectedFeed: BookmarkFeed,
    selectedKind: BookmarkKind,
    selectedTag: String?,
    onSelectFeed: (BookmarkFeed) -> Unit,
    onSelectKind: (BookmarkKind) -> Unit,
    onSelectTag: (String?) -> Unit,
    onRefreshTags: () -> Unit,
) {
    val currentFeed = when (selectedKind) {
        BookmarkKind.Illust -> mine.bookmarkFeed(selectedFeed)
        BookmarkKind.Novel -> mine.bookmarkNovelsFeed(selectedFeed)
    }
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
                        text = when (selectedKind) {
                            BookmarkKind.Illust -> selectedTag?.let { "#$it" } ?: "全部标签"
                            BookmarkKind.Novel -> "小说收藏"
                        },
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
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = selectedKind == BookmarkKind.Illust,
                    onClick = { onSelectKind(BookmarkKind.Illust) },
                    label = { Text("作品") },
                )
                FilterChip(
                    selected = selectedKind == BookmarkKind.Novel,
                    onClick = { onSelectKind(BookmarkKind.Novel) },
                    label = { Text("小说") },
                )
            }
            if (selectedKind == BookmarkKind.Illust) {
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
}

@Composable
internal fun BookmarkTagChip(
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
internal fun MinePagingFooter(
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
internal fun FollowingUsersPanel(
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
internal fun FollowingUserSection(
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
internal fun FollowingUserCard(
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
