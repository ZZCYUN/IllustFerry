package JunZi.Pixiv

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.PixivRepository
import JunZi.Pixiv.data.auth.OAuthPkce
import JunZi.Pixiv.data.auth.TokenStore
import JunZi.Pixiv.data.local.HistoryStore
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.AuthorProfile
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.BookmarkTag
import JunZi.Pixiv.data.model.HomeCategory
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.IllustComment
import JunZi.Pixiv.data.model.IllustPage
import JunZi.Pixiv.data.model.NovelDetail
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import JunZi.Pixiv.data.model.UploadIllustRequest
import JunZi.Pixiv.data.model.UploadImagePart
import JunZi.Pixiv.data.model.UploadNovelRequest
import JunZi.Pixiv.data.model.UgoiraFrameImage
import JunZi.Pixiv.data.model.UserPreview
import JunZi.Pixiv.data.model.UserPreviewPage
import JunZi.Pixiv.data.network.PixivApiException
import JunZi.Pixiv.data.network.PixivImageProxy
import JunZi.Pixiv.data.network.PixivNetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal fun PixivViewModel.updatePreviewSwipeMode(mode: PreviewSwipeMode) {
    store.savePreviewSwipeMode(mode)
    _uiState.update { it.copy(previewSwipeMode = mode) }
}
internal fun PixivViewModel.updateThemeMode(mode: PuxivThemeMode) {
    store.saveThemeMode(mode)
    _uiState.update {
        it.copy(
            themeMode = mode,
            message = when (mode) {
                PuxivThemeMode.System -> "主题已跟随系统"
                PuxivThemeMode.Light -> "已切换到亮色主题"
                PuxivThemeMode.Dark -> "已切换到暗色主题"
            },
        )
    }
}
internal fun PixivViewModel.updateMaterialYouEnabled(enabled: Boolean) {
    store.saveUseMaterialYou(enabled)
    _uiState.update {
        it.copy(
            useMaterialYou = enabled,
            message = if (enabled) {
                "已启用 Material You 动态取色"
            } else {
                "已关闭 Material You，使用自定义调色板"
            },
        )
    }
}
internal fun PixivViewModel.updateThemePalette(palette: PuxivThemePalette) {
    store.saveThemePalette(palette)
    _uiState.update {
        it.copy(
            themePalette = palette,
            message = "调色板已切换为 ${palette.messageLabel()}",
        )
    }
}
internal fun PixivViewModel.updateCustomThemePalette(palette: PuxivCustomPalette) {
    store.saveCustomThemePalette(palette)
    store.saveThemePalette(PuxivThemePalette.Custom)
    _uiState.update {
        it.copy(
            customPalette = palette,
            themePalette = PuxivThemePalette.Custom,
            message = "自定义调色板已保存",
        )
    }
}
internal fun PixivViewModel.updateSaveUgoiraZip(enabled: Boolean) {
    store.saveSaveUgoiraZip(enabled)
    _uiState.update {
        it.copy(
            saveUgoiraZip = enabled,
            message = if (enabled) "下载动图时会额外保留 zip" else "下载动图时仅保留动图文件",
        )
    }
}
internal fun PixivViewModel.updateUgoiraSaveFormat(format: UgoiraSaveFormat) {
    store.saveUgoiraSaveFormat(format.name)
    _uiState.update {
        it.copy(
            ugoiraSaveFormat = format,
            message = "动图保存格式已设置为 ${format.name}",
        )
    }
}
internal fun PixivViewModel.updateImageProxyEnabled(enabled: Boolean) {
    PixivImageProxy.useRemoteProxy = enabled
    store.saveUseRemoteImageProxy(enabled)
    _uiState.update {
        it.copy(
            useRemoteImageProxy = enabled,
            message = if (enabled) {
                "使用代理获取图片，可能更快"
            } else {
                "使用官方链接进行获取图片"
            },
        )
    }
}
internal fun PixivViewModel.updateHostIpRoutingEnabled(enabled: Boolean) {
    PixivNetworkConfig.useHostIpRouting = enabled
    dnsWarmupAttempted = !enabled || PixivNetworkConfig.hasAddressFor(APP_API_HOST)
    store.saveUseHostIpRouting(enabled)
    _uiState.update {
        it.copy(
            useHostIpRouting = enabled,
            message = if (enabled) {
                "已启用 Host/IP 兼容路由"
            } else {
                "已关闭 Host/IP 兼容路由，HTTP 请求将绕过系统代理"
            },
        )
    }
    if (enabled && !PixivNetworkConfig.hasAddressFor(APP_API_HOST)) {
        viewModelScope.launch { warmupDnsIfNeeded(forceRefresh = true) }
    }
}
internal fun PixivViewModel.updateImageProxyInput(value: String) = _uiState.update { it.copy(imageProxyInput = value.trim()) }
internal fun PixivViewModel.updateFilteredTagsInput(value: String) {
    _uiState.update { it.copy(filteredTagsInput = value) }
}
internal fun PixivViewModel.saveFilteredTags() {
    val normalized = normalizeFilteredTagsInput(_uiState.value.filteredTagsInput)
    store.saveFilteredTagsInput(normalized)
    _uiState.update {
        it.copy(
            filteredTagsInput = normalized,
            message = if (normalized.isBlank()) "已清空过滤标签" else "已保存过滤标签",
        )
    }
    reloadFeedsAfterFilterChange()
}
internal fun PixivViewModel.excludedTags(): Set<String> = _uiState.value.excludedTags()
internal fun PixivViewModel.reloadFeedsAfterFilterChange() {
    val state = _uiState.value
    if (state.keyword.isNotBlank() && state.session != null) {
        search()
    }
    if (state.home.hasLoaded) {
        loadHome(refresh = true)
    }
    if (state.discover.hasLoaded && state.session != null) {
        loadDiscover(refresh = true)
    }
    if (state.mine.hasLoaded) {
        loadMine(refresh = true)
    }
    if (state.author.userId != null && state.session != null) {
        loadAuthorWorks(refresh = true, tab = state.author.selectedTab)
    }
    if (state.selectedIllust != null && state.session != null) {
        loadRelated(refresh = true)
    }
}
internal fun PixivViewModel.saveImageProxyOrigin() {
    val normalized = PixivImageProxy.normalizeProxyOrigin(_uiState.value.imageProxyInput)
    if (normalized == null) {
        _uiState.update { it.copy(message = "请输入有效的图片代理地址") }
        return
    }
    PixivImageProxy.setProxyOrigin(normalized)
    store.saveImageProxyOrigin(normalized)
    _uiState.update {
        it.copy(
            imageProxyInput = normalized,
            message = "图片代理已设置为 $normalized",
        )
    }
}
internal fun PixivViewModel.resetImageProxyOrigin() {
    PixivImageProxy.resetProxyOrigin()
    store.clearImageProxyOrigin()
    _uiState.update {
        it.copy(
            imageProxyInput = PixivImageProxy.DEFAULT_PROXY_ORIGIN,
            message = "图片代理已恢复默认",
        )
    }
}
internal fun PixivViewModel.refreshDns(showMessage: Boolean = true) {
    if (!PixivNetworkConfig.shouldUseCompatibilityClient()) {
        _uiState.update {
            it.copy(message = if (showMessage) "Host/IP 兼容路由当前未启用" else it.message)
        }
        return
    }
    viewModelScope.launch {
        runCatching { refreshAndPersistDns().requireAnyUpdated() }
            .onSuccess { result ->
                dnsWarmupAttempted = true
                _uiState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = result.summary,
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = result.errors.values.firstOrNull(),
                            ),
                        ),
                        message = if (showMessage) result.summary else state.message,
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = "DNS 更新失败",
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = error.readableMessage(),
                            ),
                        ),
                        message = if (showMessage) error.readableMessage() else state.message,
                    )
                }
            }
    }
}
internal fun PixivViewModel.runDiagnostics() {
    val session = _uiState.value.session
    if (session == null) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        _uiState.update { state ->
            state.copy(
                home = state.home.copy(
                    diagnostics = state.home.diagnostics.copy(
                        isRunning = true,
                        apiStatus = "检测中",
                        imageStatus = "检测中",
                        lastError = null,
                    ),
                ),
            )
        }
        val dnsResult = runCatching { refreshAndPersistDns().requireAnyUpdated() }
        val apiResult = runCatching { withAccessToken { repository.recommended(it) } }
        val probeUrl = apiResult.getOrNull()?.items?.firstOrNull()?.previewUrl
            ?: _uiState.value.home.illust.recommended.items.firstOrNull()?.previewUrl
        val imageResult = if (probeUrl != null) runCatching { repository.probeImage(probeUrl) } else null

        _uiState.update { state ->
            val firstError = dnsResult.exceptionOrNull()
                ?: apiResult.exceptionOrNull()
                ?: imageResult?.exceptionOrNull()
            state.copy(
                home = state.home.copy(
                    diagnostics = state.home.diagnostics.copy(
                        isRunning = false,
                        lastDnsResult = dnsResult.getOrNull()?.summary ?: "DNS 更新失败",
                        apiStatus = if (apiResult.isSuccess) "可用" else "失败",
                        imageStatus = when {
                            imageResult == null -> "无图片样本"
                            imageResult.isSuccess -> "可用"
                            else -> "失败"
                        },
                        lastError = firstError?.readableMessage(),
                        hostSnapshot = PixivNetworkConfig.snapshot(),
                    ),
                ),
                message = firstError?.readableMessage(),
            )
        }
    }
}
internal fun PixivViewModel.registerNetworkCallback() {
    runCatching {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback,
        )
    }
}
internal fun PixivViewModel.updateVpnState() {
    PixivNetworkConfig.isVpnActive = isVpnActive()
}
internal fun PixivViewModel.isVpnActive(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    return connectivityManager.getNetworkCapabilities(activeNetwork)
        ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
}
