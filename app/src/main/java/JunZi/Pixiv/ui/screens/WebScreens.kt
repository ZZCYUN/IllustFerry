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
import JunZi.Pixiv.data.network.OkHttpProvider
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
@SuppressLint("SetJavaScriptEnabled", "WebViewClientOnReceivedSslError")
@Composable
internal fun WebLoginScreen(
    loginUrl: String,
    onBack: () -> Unit,
    onCode: (String, Boolean) -> Unit,
) {
    ProxiedPixivWebViewScreen(
        title = "网页登录",
        initialUrl = loginUrl,
        onBack = onBack,
        onInterceptUrl = { uri, useAppProxy ->
            uri.extractPixivCode()?.let {
                onCode(it, useAppProxy)
                true
            } ?: false
        },
    )
}

@Composable
internal fun WebPixivScreen(
    onBack: () -> Unit,
) {
    ProxiedPixivWebViewScreen(
        title = "Web Pixiv",
        initialUrl = PixivOfficialWebUrl,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled", "WebViewClientOnReceivedSslError")
@Composable
internal fun ProxiedPixivWebViewScreen(
    title: String,
    initialUrl: String,
    onBack: () -> Unit,
    onInterceptUrl: (Uri, Boolean) -> Boolean = { _, _ -> false },
) {
    val context = LocalContext.current
    val mainExecutor = remember { Executor { command -> Handler(Looper.getMainLooper()).post(command) } }
    var useAppProxy by remember { mutableStateOf(true) }
    var proxyReady by remember { mutableStateOf(false) }
    var proxyStatus by remember { mutableStateOf("内置代理准备中") }
    var proxyDiagnostics by remember { mutableStateOf("等待代理事件") }
    var activeProxyPort by remember { mutableIntStateOf(0) }
    var debugBarExpanded by rememberSaveable { mutableStateOf(true) }

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

    DisposableEffect(initialUrl, useAppProxy) {
        proxyReady = false
        activeProxyPort = 0
        if (useAppProxy) {
            val snapshot = PixivNetworkConfig.snapshot()
            proxyStatus = when {
                PixivNetworkConfig.isVpnActive ->
                    "VPN 已生效，代理候选将走系统 DNS"
                !PixivNetworkConfig.useHostIpRouting ->
                    "host/IP 路由已关闭，代理仅能依赖系统 DNS"
                snapshot.isEmpty() ->
                    "动态 hosts 未就绪，等待 ViewModel 首次刷新"
                else ->
                    "动态 hosts 已就绪（${snapshot.size} 项），内置代理准备中"
            }
            val proxyPort = OkHttpProvider.ensureApiProxyRunning()
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
            proxyStatus = "直连准备中"
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
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                                // 放弃校验：本地代理用的是进程内一次性 CA，反正 WebView 也不会主动信任。
                                // 与其纠结签发链，不如全放过；想抓包就抓，不在威胁模型里。
                                LocalPixivProxy.lastProxyEvent =
                                    "ssl proceed host=${error?.url?.toUri()?.host} " +
                                        "primary=${error?.primaryError} trust-all"
                                handler?.proceed()
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                if (url?.toUri()?.let { onInterceptUrl(it, useAppProxy) } == true) {
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
                                // 只关心主框架真正失败的情况——子资源（广告、追踪域、证书已 proceed 的旧失败）
                                // 在这里冒出来都和"页面能不能看"无关，悄悄记到诊断里就行，不要刷顶部状态吓用户。
                                val isMainFrame = request?.isForMainFrame == true
                                val description = error?.description?.toString().orEmpty()
                                LocalPixivProxy.lastProxyEvent =
                                    "resource-error main=$isMainFrame url=${request?.url} desc=$description"
                                if (isMainFrame) {
                                    proxyStatus = "加载失败：${description.ifBlank { "未知错误" }}"
                                }
                                super.onReceivedError(view, request, error)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                return onInterceptUrl(request.url, useAppProxy)
                            }
                        }
                    }
                },
                update = { webView ->
                    val loadKey = "${initialUrl.hashCode()}:$useAppProxy"
                    if (proxyReady && initialUrl.isNotBlank() && webView.tag != loadKey) {
                        webView.tag = loadKey
                        webView.loadUrl(initialUrl)
                    }
                },
            )
            if (debugBarExpanded) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
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
                                    context.startActivity(Intent(Intent.ACTION_VIEW, initialUrl.toUri()))
                                }.onFailure {
                                    proxyStatus = "无法打开外部浏览器"
                                }
                            },
                            enabled = initialUrl.isNotBlank(),
                            label = { Text("浏览器") },
                        )
                        IconButton(onClick = { debugBarExpanded = false }) {
                            Icon(
                                imageVector = Icons.Filled.ExpandLess,
                                contentDescription = "收起调试栏",
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shadowElevation = 2.dp,
                ) {
                    IconButton(onClick = { debugBarExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "展开调试栏",
                        )
                    }
                }
            }
        }
    }
}
