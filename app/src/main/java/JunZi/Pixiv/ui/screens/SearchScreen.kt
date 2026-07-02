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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun SearchScreen(
    keyword: String,
    isBusy: Boolean,
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
    contentPadding: PaddingValues,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onSearchKindChange: (SearchKind) -> Unit,
    onSearchSortChange: (SearchSort) -> Unit,
    onSearchTargetChange: (SearchTarget) -> Unit,
    onTrendingTagClick: (TrendingTag) -> Unit,
    onReturnToDiscover: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onOpenAuthor: (UserPreview) -> Unit,
    onRefreshDiscover: () -> Unit,
    onLoadMoreDiscover: (DiscoverFeed, Boolean) -> Unit,
) {
    val searchGridState = rememberLazyStaggeredGridState()
    val decorativeFeedSemantics = rememberDecorativeFeedSemantics()
    val resultGridState = rememberLazyStaggeredGridState()
    var selectedDiscoverFeed by remember { mutableStateOf(DiscoverFeed.Public) }

    val activeResultCount = if (searchKind == SearchKind.User) searchUsers.size else items.size
    LaunchedEffect(isSearchActive, nextUrl, isLoadingMore, isBusy, activeResultCount, searchKind) {
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
                                "$activeResultCount 搜索结果"
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
                            searchKind = searchKind,
                            searchTarget = searchTarget,
                            searchSort = searchSort,
                            isTrendingLoading = isTrendingLoading,
                            trendingTags = trendingTags,
                            onKeywordChange = onKeywordChange,
                            onSearch = onSearch,
                            onSearchKindChange = onSearchKindChange,
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
                            searchKind = searchKind,
                            searchTarget = searchTarget,
                            searchSort = searchSort,
                            isTrendingLoading = isTrendingLoading,
                            trendingTags = trendingTags,
                            onKeywordChange = onKeywordChange,
                            onSearch = onSearch,
                            onSearchKindChange = onSearchKindChange,
                            onSearchSortChange = onSearchSortChange,
                            onSearchTargetChange = onSearchTargetChange,
                            onTrendingTagClick = onTrendingTagClick,
                        )
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SearchResultHeader(
                            resultCount = activeResultCount,
                            isBusy = isBusy,
                            onBack = onReturnToDiscover,
                        )
                    }
                    if (activeResultCount == 0 && !isBusy) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            EmptySearch(Modifier.fillMaxWidth())
                        }
                    } else if (searchKind == SearchKind.User) {
                        items(
                            searchUsers,
                            key = { "user-${it.userId}" },
                            contentType = { "user-card" },
                        ) { user ->
                            FollowingUserCard(
                                user = user,
                                onClick = { onOpenAuthor(user) },
                            )
                        }
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PagingFooter(
                                isLoadingMore = isLoadingMore,
                                nextUrl = nextUrl,
                                hasItems = searchUsers.isNotEmpty(),
                                onLoadMore = onLoadMore,
                            )
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
internal fun SearchControlPanel(
    keyword: String,
    isBusy: Boolean,
    searchKind: SearchKind,
    searchTarget: SearchTarget,
    searchSort: SearchSort,
    isTrendingLoading: Boolean,
    trendingTags: List<TrendingTag>,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchKindChange: (SearchKind) -> Unit,
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
                    placeholder = when (searchKind) {
                        SearchKind.Illust -> "搜索作品、标签、标题"
                        SearchKind.Novel -> "搜索小说、标签、标题"
                        SearchKind.User -> "搜索作者名或账号"
                    },
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
                SearchKind.entries.forEach { kind ->
                    FilterChip(
                        selected = searchKind == kind,
                        onClick = { onSearchKindChange(kind) },
                        label = { Text(kind.label) },
                    )
                }
            }

            if (searchKind != SearchKind.User) {
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
internal fun TrendingTagRail(
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
internal fun SearchResultHeader(
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
internal fun DiscoverFollowPanel(
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

internal fun LazyStaggeredGridScope.discoverItems(
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

internal fun LazyStaggeredGridScope.discoverFeedMessages(
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
internal fun DiscoverPagingFooter(
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

@Composable
internal fun EmptySearch(modifier: Modifier = Modifier) {
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
internal fun rememberDecorativeFeedSemantics(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        manager?.isEnabled == true && manager.isTouchExplorationEnabled == false
    }
}

internal fun Modifier.feedSemanticsBoundary(enabled: Boolean): Modifier {
    return if (enabled) clearAndSetSemantics {} else this
}
