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
internal fun HomeScreen(
    session: AuthSession?,
    home: JunZi.Pixiv.HomeState,
    keyword: String,
    isBusy: Boolean,
    contentPadding: PaddingValues,
    onKeywordChange: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onLoadHome: () -> Unit,
    onLoadFeed: (HomeFeed, Boolean) -> Unit,
    onRankingModeChange: (RankingMode) -> Unit,
    onSelectCategory: (HomeCategory) -> Unit,
    onOpenPreview: (Illust) -> Unit,
) {
    val isAnonymous = session == null
    val category = home.category
    val categoryState = when (category) {
        HomeCategory.Illust -> home.illust
        HomeCategory.Manga -> home.manga
        HomeCategory.Novel -> home.novel
    }
    val rankingMode = categoryState.rankingMode
    val primaryFeed = if (isAnonymous) home.walkthrough else categoryState.recommended
    val visibleItems = if (isAnonymous) {
        home.walkthrough.items
    } else {
        categoryState.recommended.items + categoryState.ranking.items + categoryState.latest.items
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
    val tabOptions = listOf(
        HomeCategory.Illust to "插画",
        HomeCategory.Manga to "漫画",
        HomeCategory.Novel to "小说",
    )
    val selectedTabIndex = tabOptions.indexOfFirst { it.first == category }.takeIf { it >= 0 } ?: 0
    val baseHeaderCount = (if (!isAnonymous) 2 else 0) +
        (if (!isAnonymous) 1 else 0) +
        (if (!isAnonymous && homeTrendTags.isNotEmpty()) 1 else 0)
    val recommendedHeaderIndex = baseHeaderCount
    val rankingModesIndex = recommendedHeaderIndex + homeFeedSectionItemCount(primaryFeed)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .feedSemanticsBoundary(decorativeFeedSemantics),
        ) {
            if (!isAnonymous) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    tabOptions.forEachIndexed { index, (cat, label) ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onSelectCategory(cat) },
                            text = { Text(label) },
                        )
                    }
                }
            }
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
                val recommendedTitle = when {
                    isAnonymous -> "浏览"
                    category == HomeCategory.Novel -> "小说推荐"
                    category == HomeCategory.Manga -> "漫画推荐"
                    else -> "推荐"
                }
                homeFeedSection(
                    title = recommendedTitle,
                    feed = if (isAnonymous) HomeFeed.Walkthrough else HomeFeed.Recommended,
                    feedState = primaryFeed,
                    keyPrefix = category.name,
                    clearItemSemantics = decorativeFeedSemantics,
                    onLoadFeed = onLoadFeed,
                    onOpenPreview = onOpenPreview,
                )
                if (!isAnonymous) {
                    item(
                        key = "home-ranking-modes-${category.name}",
                        contentType = "home-ranking-modes",
                        span = StaggeredGridItemSpan.FullLine,
                    ) {
                        RankingModeChips(
                            selected = rankingMode,
                            category = category,
                            onSelect = onRankingModeChange,
                        )
                    }
                    homeFeedSection(
                        title = "排行 · ${rankingMode.label}",
                        feed = HomeFeed.Ranking,
                        feedState = categoryState.ranking,
                        keyPrefix = category.name,
                        clearItemSemantics = decorativeFeedSemantics,
                        onLoadFeed = onLoadFeed,
                        onOpenPreview = onOpenPreview,
                    )
                    homeFeedSection(
                        title = "最新",
                        feed = HomeFeed.Latest,
                        feedState = categoryState.latest,
                        keyPrefix = category.name,
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
internal fun HomeSearchBar(
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
internal fun PixivSearchField(
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
internal fun HomeEntryRow(
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
internal fun HomeEntryButton(
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
internal fun HomeTrendChips(
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
internal fun FeaturedIllustCard(
    illust: Illust,
    onClick: () -> Unit,
) {
    val seriesOpener = LocalSeriesOpener.current
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
                if (illust.pageCount > 1 && !illust.type.equals("novel", ignoreCase = true)) {
                    OverlayChip("${illust.pageCount}P")
                }
                if (illust.seriesId != null && seriesOpener != null && !illust.type.equals("novel", ignoreCase = true)) {
                    SeriesBadge(onClick = { seriesOpener(illust) })
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
internal fun RankingModeChips(
    selected: RankingMode,
    category: HomeCategory,
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
                RankingMode.values().filter { it.category == category }.forEach { mode ->
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
internal fun OverlayChip(text: String) {
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
internal fun SeriesBadge(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.86f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = "查看系列",
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = "系列",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun NovelTagOverlay(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    val display = tags.asSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(3)
        .toList()
    if (display.isEmpty()) return
    FlowRow(
        modifier = modifier.widthIn(max = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        display.forEach { tag ->
            Surface(
                modifier = Modifier.clearAndSetSemantics {},
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text(
                    text = "#$tag",
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun BoxScope.ImageScrim() {
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
internal fun MetadataPill(text: String) {
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
internal fun StatusDot(
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
internal fun DiagnosticsPanel(
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

internal fun LazyStaggeredGridScope.homeFeedSection(
    title: String,
    feed: HomeFeed,
    feedState: FeedState,
    clearItemSemantics: Boolean,
    onLoadFeed: (HomeFeed, Boolean) -> Unit,
    onOpenPreview: (Illust) -> Unit,
    keyPrefix: String = "",
) {
    val sectionKey = if (keyPrefix.isEmpty()) feed.name else "$keyPrefix-${feed.name}"
    item(
        key = "$sectionKey-header",
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
            key = "$sectionKey-error",
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
            key = "$sectionKey-loading",
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
        key = { "$sectionKey-${it.id}" },
        contentType = { "illust-card" },
    ) { illust ->
        IllustCard(
            illust = illust,
            clearSemantics = clearItemSemantics,
            onClick = onOpenPreview,
        )
    }
    item(
        key = "$sectionKey-footer",
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

internal fun homeFeedSectionItemCount(feedState: FeedState): Int {
    var count = 2
    if (feedState.error != null) count += 1
    if (feedState.items.isEmpty() && feedState.isLoading) count += 1
    count += feedState.items.size
    return count
}

@Composable
internal fun SectionHeader(
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
