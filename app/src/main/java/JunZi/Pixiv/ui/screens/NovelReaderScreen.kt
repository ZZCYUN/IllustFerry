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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NovelReaderScreen(
    illust: Illust?,
    readerState: NovelReaderState,
    comments: CommentState,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onDownload: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenAuthor: (Illust) -> Unit,
    onTagClick: (String) -> Unit,
    onCommentInputChange: (String) -> Unit,
    onSendComment: () -> Unit,
) {
    val detail = readerState.detail
    val title = detail?.title ?: illust?.title ?: "小说"
    val linkColor = MaterialTheme.colorScheme.primary
    val blocks = remember(readerState.text, readerState.uploadedImages, readerState.pixivImages, linkColor) {
        parseNovelBlocks(
            text = readerState.text,
            uploadedImages = readerState.uploadedImages,
            pixivImages = readerState.pixivImages,
            linkStyle = SpanStyle(color = linkColor),
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
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
                    IconButton(onClick = onDownload, enabled = !readerState.isLoading) {
                        Icon(Icons.Default.Download, contentDescription = "下载小说")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                readerState.isLoading && readerState.text.isBlank() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                readerState.error != null && readerState.text.isBlank() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = readerState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = onReload) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("重试")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        item(key = "novel-cover") {
                            NovelCover(illust = illust, detail = detail)
                        }
                        item(key = "novel-meta") {
                            NovelMetaPanel(
                                illust = illust,
                                detail = detail,
                                readerState = readerState,
                                onOpenAuthor = onOpenAuthor,
                                onTagClick = onTagClick,
                                onToggleBookmark = onToggleBookmark,
                            )
                        }
                        item(key = "novel-text-header") {
                            Spacer(Modifier.height(4.dp))
                        }
                        itemsIndexed(
                            items = blocks,
                            key = { index, _ -> "b-$index" },
                        ) { _, block ->
                            NovelBlockRow(block)
                        }
                        item(key = "novel-comments") {
                            CommentsPanel(
                                comments = comments,
                                onInputChange = onCommentInputChange,
                                onSend = onSendComment,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item(key = "novel-footer") { Spacer(Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SeriesScreen(
    state: SeriesState,
    onBack: () -> Unit,
    onOpenPreview: (Illust) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    val gridState = rememberLazyStaggeredGridState()
    LaunchedEffect(state.seriesId, state.items.size, state.nextUrl, state.isLoading) {
        if (state.items.isEmpty() || state.isLoading || state.nextUrl.isNullOrBlank()) return@LaunchedEffect
        snapshotFlow {
            val layout = gridState.layoutInfo
            val last = layout.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow false
            last >= layout.totalItemsCount - 4
        }.distinctUntilChanged().collect { needsMore ->
            if (needsMore) onLoadMore()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.title.ifBlank { "系列" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val subtitle = buildString {
                            append("作品系列")
                            if (state.items.isNotEmpty()) {
                                append(" · ${state.items.size} 件")
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onRetry, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.items.isEmpty() && state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.items.isEmpty() && state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = onRetry) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("重试")
                        }
                    }
                }
                state.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "系列暂无作品",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                    ) {
                        items(
                            state.items,
                            key = { "series-${state.seriesId}-${it.id}" },
                            contentType = { "illust-card" },
                        ) { illust ->
                            IllustCard(
                                illust = illust,
                                onClick = onOpenPreview,
                            )
                        }
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PagingFooter(
                                isLoadingMore = state.isLoading && state.items.isNotEmpty(),
                                nextUrl = state.nextUrl,
                                hasItems = state.items.isNotEmpty(),
                                onLoadMore = onLoadMore,
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun parseNovelBlocks(
    text: String,
    uploadedImages: Map<String, String>,
    pixivImages: Map<String, String>,
    linkStyle: SpanStyle,
): List<NovelBlock> {
    if (text.isBlank()) return emptyList()
    val lines = text.replace("\r\n", "\n").split("\n")
    val chapterRegex = Regex("^\\[chapter:(.+)\\]$")
    val uploadedRegex = Regex("^\\[uploadedimage:(\\d+)\\]$")
    val pixivImageRegex = Regex("^\\[pixivimage:([0-9-]+)\\]$")
    val result = mutableListOf<NovelBlock>()
    for (raw in lines) {
        val line = raw.trim()
        if (line.isEmpty() || line.equals("[newpage]", ignoreCase = true)) {
            result += NovelBlock.Blank
            continue
        }
        val chapter = chapterRegex.matchEntire(line)
        if (chapter != null) {
            result += NovelBlock.Chapter(chapter.groupValues[1])
            continue
        }
        val uploaded = uploadedRegex.matchEntire(line)
        if (uploaded != null) {
            val id = uploaded.groupValues[1]
            result += NovelBlock.Image(uploadedImages[id], "uploadedimage:$id")
            continue
        }
        val pixiv = pixivImageRegex.matchEntire(line)
        if (pixiv != null) {
            val key = pixiv.groupValues[1]
            val url = pixivImages[key]
                ?: pixivImages["$key-1"]
                ?: pixivImages[key.substringBefore('-')]
            result += NovelBlock.Image(url, "pixivimage:$key")
            continue
        }
        result += NovelBlock.Paragraph(parseInlineNovel(line, linkStyle))
    }
    return result
}

internal fun parseInlineNovel(text: String, linkStyle: SpanStyle): AnnotatedString = buildAnnotatedString {
    val jumpuriRegex = Regex("^jumpuri:(.*?)>(.+)$")
    val rbRegex = Regex("^rb:(.*?)>(.+)$")
    val jumpRegex = Regex("^jump:(\\d+)$")
    var i = 0
    while (i < text.length) {
        val open = text.indexOf("[[", i)
        if (open < 0) {
            append(text.substring(i))
            break
        }
        if (open > i) append(text.substring(i, open))
        val close = text.indexOf("]]", open + 2)
        if (close < 0) {
            append(text.substring(open))
            break
        }
        val inner = text.substring(open + 2, close)
        val jumpuriMatch = jumpuriRegex.matchEntire(inner)
        val rbMatch = rbRegex.matchEntire(inner)
        val jumpMatch = jumpRegex.matchEntire(inner)
        when {
            jumpuriMatch != null -> {
                val display = jumpuriMatch.groupValues[1].ifBlank { jumpuriMatch.groupValues[2] }
                val url = jumpuriMatch.groupValues[2]
                withLink(LinkAnnotation.Url(url, TextLinkStyles(style = linkStyle))) {
                    append(display)
                }
            }
            rbMatch != null -> {
                append(rbMatch.groupValues[1])
                append("(")
                append(rbMatch.groupValues[2])
                append(")")
            }
            jumpMatch != null -> append("→ 第${jumpMatch.groupValues[1]}页")
            else -> append("[[$inner]]")
        }
        i = close + 2
    }
}

@Composable
internal fun NovelBlockRow(block: NovelBlock) {
    when (block) {
        is NovelBlock.Blank -> Spacer(Modifier.height(8.dp))
        is NovelBlock.Chapter -> Text(
            text = block.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
        )
        is NovelBlock.Paragraph -> Text(
            text = block.text,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
        )
        is NovelBlock.Image -> NovelInlineImage(url = block.url, rawTag = block.rawTag)
    }
}

@Composable
internal fun NovelInlineImage(url: String?, rawTag: String) {
    var aspect by remember(url) { mutableStateOf<Float?>(null) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            GlideImage(
                url = url,
                modifier = Modifier.fillMaxWidth(),
                aspectRatio = aspect,
                crop = false,
                onDrawableSize = { width, height ->
                    if (width > 0 && height > 0) {
                        aspect = width.toFloat() / height.toFloat()
                    }
                },
            )
        } else {
            Text(
                text = "[$rawTag]",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(16.dp),
            )
        }
    }
}

@Composable
internal fun NovelCover(illust: Illust?, detail: NovelDetail?) {
    val coverUrl = illust?.previewUrl ?: detail?.coverUrl
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (!coverUrl.isNullOrBlank()) {
            GlideImage(
                url = coverUrl,
                modifier = Modifier.fillMaxSize(),
                crop = true,
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun NovelMetaPanel(
    illust: Illust?,
    detail: NovelDetail?,
    readerState: NovelReaderState,
    onOpenAuthor: (Illust) -> Unit,
    onTagClick: (String) -> Unit,
    onToggleBookmark: () -> Unit,
) {
    val tags = detail?.tags ?: illust?.tags ?: emptyList()
    val caption = (detail?.caption?.takeIf { it.isNotBlank() }
        ?: illust?.caption?.takeIf { it.isNotBlank() })
        ?.plainCaption()
    val totalView = detail?.totalView ?: illust?.totalView ?: 0
    val totalBookmarks = detail?.totalBookmarks ?: illust?.totalBookmarks ?: 0
    val textLength = detail?.textLength ?: 0
    val seriesTitle = detail?.seriesTitle?.takeIf { it.isNotBlank() }
        ?: illust?.seriesTitle?.takeIf { it.isNotBlank() }
    val isBookmarked = illust?.isBookmarked == true
    val canBookmark = illust != null && !readerState.isBookmarkBusy
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
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
                    url = illust?.authorAvatarUrl ?: detail?.authorAvatarUrl,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = illust != null) { illust?.let(onOpenAuthor) },
                    crop = true,
                    requestSize = PuxivAvatarImageSize,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = illust != null) { illust?.let(onOpenAuthor) },
                ) {
                    Text(
                        text = (detail?.title ?: illust?.title.orEmpty()).ifBlank { "#${illust?.id ?: 0}" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val authorName = detail?.authorName ?: illust?.authorName.orEmpty()
                    val authorAccount = detail?.authorAccount ?: illust?.authorAccount.orEmpty()
                    Text(
                        text = buildString {
                            append(authorName.ifBlank { "Unknown author" })
                            authorAccount.takeIf { it.isNotBlank() }?.let { append(" @").append(it) }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            BookmarkActionButton(
                isBookmarked = isBookmarked,
                enabled = canBookmark,
                onPublicBookmark = onToggleBookmark,
                onPrivateBookmark = onToggleBookmark,
                onDeleteBookmark = onToggleBookmark,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MetadataPill("小说")
            if (textLength > 0) MetadataPill("${textLength} 字")
            if (totalBookmarks > 0) MetadataPill("${totalBookmarks} 收藏")
            if (totalView > 0) MetadataPill("${totalView} 浏览")
            detail?.createDate?.takeIf { it.isNotBlank() }?.take(10)?.let { MetadataPill(it) }
                ?: illust?.createDate?.takeIf { it.isNotBlank() }?.take(10)?.let { MetadataPill(it) }
            if (detail?.isOriginal == true) MetadataPill("原创")
            if (detail?.xRestrict != null && detail.xRestrict > 0) MetadataPill("R-18")
        }
        if (!seriesTitle.isNullOrBlank() && illust?.type?.equals("novel", ignoreCase = true) != true) {
            val seriesId = detail?.seriesId ?: illust?.seriesId
            val seriesOpener = LocalSeriesOpener.current
            val openSeries = if (seriesId != null && seriesId > 0L && seriesOpener != null && illust != null) {
                {
                    seriesOpener(
                        illust.copy(
                            seriesId = seriesId,
                            seriesTitle = seriesTitle,
                            type = "novel",
                        ),
                    )
                }
            } else {
                null
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        if (openSeries != null) it.clickable(onClick = openSeries) else it
                    },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "系列",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = seriesTitle,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (openSeries != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "查看系列",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                tags.forEach { tag ->
                    AssistChip(onClick = { onTagClick(tag) }, label = { Text(tag) })
                }
            }
        }
        caption?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
