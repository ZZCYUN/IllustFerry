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
internal fun SettingsScreen(
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
    onUseThumbnailPreviewChange: (Boolean) -> Unit,
    onFilteredTagsInputChange: (String) -> Unit,
    onSaveFilteredTags: () -> Unit,
    onUgoiraSaveFormatChange: (UgoiraSaveFormat) -> Unit,
    onOpenWebPixiv: () -> Unit,
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
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "预览页使用缩略图",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "非全屏模式加载缩略图以加快速度，点击全屏后加载原图。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = useThumbnailPreview,
                        onCheckedChange = onUseThumbnailPreviewChange,
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
            ExperimentalFeaturesSection(onOpenWebPixiv = onOpenWebPixiv)
            OpenSourceLicenseSection()
        }
    }
}

@Composable
internal fun ThemeSettingsSection(
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
internal fun ThemePaletteChip(
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
internal fun PaletteMiniSwatches(colorScheme: ColorScheme) {
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
internal fun CustomPaletteDialog(
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
internal fun CustomPaletteField(
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
internal fun PaletteDetailPreview(
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
internal fun PaletteDetailToken(
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

internal fun Color.toHexString(): String {
    val rgb = toArgb() and 0x00FFFFFF
    return "#${rgb.toString(16).padStart(6, '0').uppercase()}"
}

internal fun String.normalizedHexOrEmpty(): String {
    return normalizedHexOrNull().orEmpty()
}

internal fun String.normalizedHexOrNull(): String? {
    val raw = trim()
    val withHash = if (raw.startsWith("#")) raw else "#$raw"
    return if (UI_HEX_COLOR_PATTERN.matches(withHash)) withHash.uppercase() else null
}

internal fun String.toColorOrNull(): Color? {
    val hex = normalizedHexOrNull()?.removePrefix("#") ?: return null
    return Color(0xFF000000 or hex.toLong(16))
}

@Composable
internal fun ExperimentalFeaturesSection(
    onOpenWebPixiv: () -> Unit,
) {
    Text(
        text = "实验功能",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onOpenWebPixiv),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Web Pixiv",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "使用登录页的内置代理 WebView 模式访问 Pixiv 官网",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun OpenSourceLicenseSection() {
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
internal fun OpenSourceLicenseRow(
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

internal fun openExternalUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
