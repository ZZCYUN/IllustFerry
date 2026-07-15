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
internal fun PreviewScreen(
    preview: PreviewUiState,
    shell: ShellUiState,
    settings: SettingsUiState,
    onBack: () -> Unit,
    onSelectImage: (Int) -> Unit,
    bookmarkTags: List<String>,
    areBookmarkTagsLoaded: Boolean,
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
    val illust = preview.selectedIllust
    LaunchedEffect(illust?.id, preview.selectedBookmark.isLoaded) {
        val id = illust?.id ?: return@LaunchedEffect
        if (illust.isBookmarked && !preview.selectedBookmark.isLoaded) {
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
        preview.selectedImageIndex.coerceIn(0, (size - 1).coerceAtLeast(0))
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
            val safeIndex = preview.selectedImageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (!illust.isUgoira && settings.previewSwipeMode == PreviewSwipeMode.Vertical) {
                    VerticalComicPreview(
                        illust = illust,
                        pages = pages,
                        selectedIndex = safeIndex,
                        measuredRatios = measuredRatios,
                        animateImages = !preview.isFullScreenPreview,
                        useThumbnail = settings.useThumbnailPreview,
                        onSelectImage = onSelectImage,
                        onOpenFullScreen = onOpenFullScreen,
                        onImageMeasured = { index, width, height ->
                            updateMeasuredRatio(index, width, height)
                        },
                        footer = {
                            IllustMeta(
                                illust = illust,
                                related = preview.related,
                                bookmarkTags = bookmarkTags,
                                selectedBookmark = preview.selectedBookmark,
                                onBookmarkPublic = onBookmarkPublic,
                                onBookmarkPrivate = onBookmarkPrivate,
                                onAddBookmarkTags = onAddBookmarkTags,
                                onToggleBookmarkTag = onToggleBookmarkTag,
                                onDeleteBookmark = onDeleteBookmark,
                                onDownload = onDownload,
                                actionsEnabled = !shell.isBusy,
                                onLoadRelated = onLoadRelated,
                                onOpenPreview = onOpenPreview,
                                onTagClick = onTagClick,
                                onOpenAuthor = onOpenAuthor,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            CommentsPanel(
                                comments = preview.comments,
                                onInputChange = onCommentInputChange,
                                onSend = onSendComment,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (preview.isPreviewLoading) {
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
                                if (illust.isUgoira && preview.ugoiraFrames.isNotEmpty()) {
                                    UgoiraPlayer(
                                        frames = preview.ugoiraFrames,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    HorizontalImagePreview(
                                        illust = illust,
                                        imageIndex = preview.selectedImageIndex,
                                        animateImages = !preview.isFullScreenPreview,
                                        useThumbnail = settings.useThumbnailPreview,
                                        onSelectImage = onSelectImage,
                                        onOpenFullScreen = onOpenFullScreen,
                                        onImageMeasured = { index, width, height ->
                                            updateMeasuredRatio(index, width, height)
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }

                                if (preview.isPreviewLoading) {
                                    if (illust.isUgoira) {
                                        UgoiraLoadingProgress(
                                            loaded = preview.ugoiraLoadedFrames,
                                            total = preview.ugoiraTotalFrames,
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
                                related = preview.related,
                                bookmarkTags = bookmarkTags,
                                selectedBookmark = preview.selectedBookmark,
                                onBookmarkPublic = onBookmarkPublic,
                                onBookmarkPrivate = onBookmarkPrivate,
                                onAddBookmarkTags = onAddBookmarkTags,
                                onToggleBookmarkTag = onToggleBookmarkTag,
                                onDeleteBookmark = onDeleteBookmark,
                                onDownload = onDownload,
                                actionsEnabled = !shell.isBusy,
                                onLoadRelated = onLoadRelated,
                                onOpenPreview = onOpenPreview,
                                onTagClick = onTagClick,
                                onOpenAuthor = onOpenAuthor,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            CommentsPanel(
                                comments = preview.comments,
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

    if (preview.isFullScreenPreview && illust != null && !illust.isUgoira) {
        FullScreenPreview(
            illust = illust,
            imageIndex = fullScreenIndex,
            swipeMode = settings.previewSwipeMode,
            frames = preview.ugoiraFrames,
            measuredRatios = measuredRatios,
            onSelectImage = onSelectImage,
            onClose = onCloseFullScreen,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HorizontalImagePreview(
    illust: Illust,
    imageIndex: Int,
    animateImages: Boolean,
    useThumbnail: Boolean = false,
    onSelectImage: (Int) -> Unit,
    onOpenFullScreen: (Int) -> Unit,
    onImageMeasured: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val urls = illust.previewUrls(useThumbnail)
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
internal fun VerticalComicPreview(
    illust: Illust,
    pages: List<IllustImagePage>,
    selectedIndex: Int,
    measuredRatios: Map<Int, Float>,
    animateImages: Boolean,
    useThumbnail: Boolean = false,
    onSelectImage: (Int) -> Unit,
    onOpenFullScreen: (Int) -> Unit,
    onImageMeasured: (Int, Int, Int) -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val urls = illust.previewUrls(useThumbnail)
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
internal fun UgoiraPlayer(
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
internal fun FullScreenPreview(
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
internal fun FullScreenVerticalPreview(
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
internal fun FullScreenImagePage(
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
internal fun FullScreenZoomableImage(
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
internal fun PreviewLoadingStrip(modifier: Modifier = Modifier) {
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
