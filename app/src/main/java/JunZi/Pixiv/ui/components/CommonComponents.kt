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


@Composable
internal fun HistoryIllustCard(
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
internal fun EmptyStateCard(
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

internal fun formatViewedAt(viewedAtMillis: Long): String {
    return runCatching {
        DateTimeFormatter.ofPattern("MM-dd HH:mm")
            .format(Instant.ofEpochMilli(viewedAtMillis).atZone(ZoneId.systemDefault()))
    }.getOrElse { viewedAtMillis.toString() }
}

@Composable
internal fun IllustCard(
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
    val seriesOpener = LocalSeriesOpener.current
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
            if (illust.type.equals("novel", ignoreCase = true) && illust.tags.isNotEmpty()) {
                NovelTagOverlay(
                    tags = illust.tags,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Row(
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
        }
    }
}

@Composable
internal fun PagingFooter(
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

internal fun Illust.previewUrls(): List<String> = imageUrls.ifEmpty { listOfNotNull(previewUrl) }

internal fun MyState.bookmarkFeed(feed: BookmarkFeed): FeedState {
    return when (feed) {
        BookmarkFeed.Public -> bookmarks
        BookmarkFeed.Private -> privateBookmarks
    }
}

internal fun MyState.bookmarkNovelsFeed(feed: BookmarkFeed): FeedState {
    return when (feed) {
        BookmarkFeed.Public -> bookmarkNovels
        BookmarkFeed.Private -> privateBookmarkNovels
    }
}

internal fun MyState.bookmarkTags(feed: BookmarkFeed): List<BookmarkTag> {
    return when (feed) {
        BookmarkFeed.Public -> publicBookmarkTags
        BookmarkFeed.Private -> privateBookmarkTags
    }
}

internal fun MyState.followingFeed(feed: FollowUserFeed): UserPreviewFeedState {
    return when (feed) {
        FollowUserFeed.Public -> publicFollowing
        FollowUserFeed.Private -> privateFollowing
    }
}

internal fun DiscoverState.feed(feed: DiscoverFeed): FeedState {
    return when (feed) {
        DiscoverFeed.Public -> publicWorks
        DiscoverFeed.Private -> privateWorks
    }
}

internal fun MyState.bookmarkTags(): List<String> {
    return (publicBookmarkTags.map { it.name } + privateBookmarkTags.map { it.name })
        .distinctBy { it.lowercase() }
        .take(10)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookmarkActionButton(
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
internal fun IllustMeta(
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
    val inputTag = bookmarkTagInput.normalizedBookmarkTagOrNull()
    val activeBookmarkTags = selectedBookmark.tags
    val selectedTags = listOfNotNull(inputTag)
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
            if (!illust.type.equals("novel", ignoreCase = true)) {
                MetadataPill("${illust.pageCount}P")
            }
            MetadataPill("${illust.totalBookmarks} 收藏")
            if (illust.totalView > 0) MetadataPill("${illust.totalView} 浏览")
            illust.createDate?.takeIf { it.isNotBlank() }?.take(10)?.let { MetadataPill(it) }
            if (illust.aiType != null && illust.aiType > 1) MetadataPill("AI")
        }
        val seriesId = illust.seriesId
        val seriesOpener = LocalSeriesOpener.current
        if (seriesId != null && seriesOpener != null && !illust.type.equals("novel", ignoreCase = true)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { seriesOpener(illust) },
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
                            text = illust.seriesTitle?.takeIf { it.isNotBlank() } ?: "#$seriesId",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "查看系列",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
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
        if (illust.isBookmarked && (actionsEnabled || bookmarkTags.isNotEmpty() || bookmarkTagInput.isNotBlank())) {
            BookmarkTagSelector(
                tags = bookmarkTags,
                selectedTags = activeBookmarkTags.toSet(),
                input = bookmarkTagInput,
                onInputChange = { bookmarkTagInput = it },
                isBusy = selectedBookmark.isLoading,
                onAddInputTag = {
                    val tag = bookmarkTagInput.normalizedBookmarkTagOrNull() ?: return@BookmarkTagSelector
                    onAddBookmarkTags(listOf(tag))
                    bookmarkTagInput = ""
                },
                onToggle = { tag ->
                    onToggleBookmarkTag(tag)
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
internal fun BookmarkTagSelector(
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
internal fun BookmarkSelectionChip(
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

internal fun String.normalizedBookmarkTagOrNull(): String? {
    return trim()
        .trimStart('#')
        .replace(Regex("""\s+"""), " ")
        .take(40)
        .takeIf { it.isNotBlank() }
}

@Composable
internal fun GlideImage(
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
internal fun AnimatedGlideImage(
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

internal fun String.glideModel(): Any {
    return when {
        startsWith("content://", ignoreCase = true) ||
            startsWith("file://", ignoreCase = true) ||
            startsWith("android.resource://", ignoreCase = true) -> toUri()
        else -> GlideUrl(this)
    }
}

internal fun String.shouldUseDrawableGlide(): Boolean {
    val lower = substringBefore('?').substringBefore('#').lowercase()
    return startsWith("content://", ignoreCase = true) ||
        startsWith("file://", ignoreCase = true) ||
        lower.endsWith(".gif") ||
            lower.endsWith(".webp")
}

internal fun String.isGifLike(): Boolean {
    return substringBefore('?')
        .substringBefore('#')
        .lowercase()
        .endsWith(".gif")
}

internal fun String.isGifLike(context: Context): Boolean {
    if (isGifLike()) return true
    if (!startsWith("content://", ignoreCase = true)) return false
    return runCatching {
        context.contentResolver.getType(toUri()).equals("image/gif", ignoreCase = true)
    }.getOrDefault(false)
}

internal fun String.isLocalUriGif(context: Context): Boolean {
    return (
        startsWith("content://", ignoreCase = true) ||
            startsWith("file://", ignoreCase = true)
        ) && isGifLike(context)
}

internal fun DownloadItem.localSavedUris(): List<String> {
    return savedUris.orEmpty()
        .mapNotNull(::nonBlankStringOrNull)
        .ifEmpty { listOfNotNull(savedUri?.takeIf { it.isNotBlank() }) }
}

internal fun nonBlankStringOrNull(value: String?): String? {
    return value?.takeIf { it.isNotBlank() }
}

internal fun String.downloadMimeType(): String {
    return when (substringAfterLast('.', "").lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "zip" -> "application/zip"
        else -> "image/jpeg"
    }
}

internal val Illust.typeLabel: String
    get() = when (type.lowercase()) {
        "ugoira" -> "动画"
        "manga" -> "漫画"
        "novel" -> "小说"
        else -> "插画"
    }

@Composable
internal fun UgoiraLoadingProgress(
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
internal fun CommentsPanel(
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

internal fun String.plainCaption(): String {
    return replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .trim()
}

internal fun Uri.extractPixivCode(): String? {
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
