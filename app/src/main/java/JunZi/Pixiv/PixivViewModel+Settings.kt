package JunZi.Pixiv

import JunZi.Pixiv.data.network.PixivImageProxy
import JunZi.Pixiv.data.network.PixivNetworkConfig
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.updatePreviewSwipeMode(mode: PreviewSwipeMode) {
    store.savePreviewSwipeMode(mode)
    _settingsState.update { it.copy(previewSwipeMode = mode) }
}
internal fun PixivViewModel.updateThemeMode(mode: PuxivThemeMode) {
    store.saveThemeMode(mode)
    _settingsState.update { it.copy(themeMode = mode) }
    showMessage(
        when (mode) {
            PuxivThemeMode.System -> "主题已跟随系统"
            PuxivThemeMode.Light -> "已切换到亮色主题"
            PuxivThemeMode.Dark -> "已切换到暗色主题"
        }
    )
}
internal fun PixivViewModel.updateMaterialYouEnabled(enabled: Boolean) {
    store.saveUseMaterialYou(enabled)
    _settingsState.update { it.copy(useMaterialYou = enabled) }
    showMessage(if (enabled) "已启用 Material You 动态取色" else "已关闭 Material You，使用自定义调色板")
}
internal fun PixivViewModel.updateThemePalette(palette: PuxivThemePalette) {
    store.saveThemePalette(palette)
    _settingsState.update {
        it.copy(themePalette = palette)
    }
    showMessage("调色板已切换为 ${palette.messageLabel()}")
}
internal fun PixivViewModel.updateCustomThemePalette(palette: PuxivCustomPalette) {
    store.saveCustomThemePalette(palette)
    store.saveThemePalette(PuxivThemePalette.Custom)
    _settingsState.update {
        it.copy(
            customPalette = palette,
            themePalette = PuxivThemePalette.Custom,
        )
    }
    showMessage("自定义调色板已保存")
}
internal fun PixivViewModel.updateSaveUgoiraZip(enabled: Boolean) {
    store.saveSaveUgoiraZip(enabled)
    _settingsState.update { it.copy(saveUgoiraZip = enabled) }
    showMessage(if (enabled) "下载动图时会额外保留 zip" else "下载动图时仅保留动图文件")
}
internal fun PixivViewModel.updateUseThumbnailPreview(enabled: Boolean) {
    store.saveUseThumbnailPreview(enabled)
    _settingsState.update { it.copy(useThumbnailPreview = enabled) }
    showMessage(if (enabled) "预览页将加载缩略图以加快速度" else "预览页将加载原图")
}
internal fun PixivViewModel.updateUgoiraSaveFormat(format: UgoiraSaveFormat) {
    store.saveUgoiraSaveFormat(format.name)
    _settingsState.update { it.copy(ugoiraSaveFormat = format) }
    showMessage("动图保存格式已设置为 ${format.name}")
}
internal fun PixivViewModel.updateImageProxyEnabled(enabled: Boolean) {
    PixivImageProxy.useRemoteProxy = enabled
    store.saveUseRemoteImageProxy(enabled)
    _settingsState.update { it.copy(useRemoteImageProxy = enabled) }
    showMessage(if (enabled) "使用代理获取图片，可能更快" else "使用官方链接进行获取图片")
}
internal fun PixivViewModel.updateHostIpRoutingEnabled(enabled: Boolean) {
    PixivNetworkConfig.useHostIpRouting = enabled
    dnsWarmupAttempted = !enabled || PixivNetworkConfig.hasAddressFor(APP_API_HOST)
    store.saveUseHostIpRouting(enabled)
    _settingsState.update { it.copy(useHostIpRouting = enabled) }
    showMessage(
        if (enabled) "已启用 Host/IP 兼容路由"
        else "已关闭 Host/IP 兼容路由，HTTP 请求将绕过系统代理"
    )
    if (enabled && !PixivNetworkConfig.hasAddressFor(APP_API_HOST)) {
        viewModelScope.launch { warmupDnsIfNeeded(forceRefresh = true) }
    }
}
internal fun PixivViewModel.updateImageProxyInput(value: String) = _settingsState.update { it.copy(imageProxyInput = value.trim()) }
internal fun PixivViewModel.updateFilteredTagsInput(value: String) {
    _settingsState.update { it.copy(filteredTagsInput = value) }
}
internal fun PixivViewModel.saveFilteredTags() {
    val normalized = normalizeFilteredTagsInput(_settingsState.value.filteredTagsInput)
    store.saveFilteredTagsInput(normalized)
    _settingsState.update { it.copy(filteredTagsInput = normalized) }
    showMessage(if (normalized.isBlank()) "已清空过滤标签" else "已保存过滤标签")
    reloadFeedsAfterFilterChange()
}
internal fun PixivViewModel.reloadFeedsAfterFilterChange() {
    val search = _searchState.value
    val session = _authState.value.session
    if (search.keyword.isNotBlank() && session != null) {
        search()
    }
    if (_homeState.value.home.hasLoaded) {
        loadHome(refresh = true)
    }
    if (search.discover.hasLoaded && session != null) {
        loadDiscover(refresh = true)
    }
    if (_mineState.value.mine.hasLoaded) {
        loadMine(refresh = true)
    }
    val authorState = _authorState.value
    if (authorState.author.userId != null && session != null) {
        loadAuthorWorks(refresh = true, tab = authorState.author.selectedTab)
    }
    if (_previewState.value.selectedIllust != null && session != null) {
        loadRelated(refresh = true)
    }
}
internal fun PixivViewModel.saveImageProxyOrigin() {
    val normalized = PixivImageProxy.normalizeProxyOrigin(_settingsState.value.imageProxyInput)
    if (normalized == null) {
        showMessage("请输入有效的图片代理地址")
        return
    }
    PixivImageProxy.setProxyOrigin(normalized)
    store.saveImageProxyOrigin(normalized)
    _settingsState.update { it.copy(imageProxyInput = normalized) }
    showMessage("图片代理已设置为 $normalized")
}
internal fun PixivViewModel.resetImageProxyOrigin() {
    PixivImageProxy.resetProxyOrigin()
    store.clearImageProxyOrigin()
    _settingsState.update { it.copy(imageProxyInput = PixivImageProxy.DEFAULT_PROXY_ORIGIN) }
    showMessage("图片代理已恢复默认")
}
internal fun PixivViewModel.refreshDns(showMessage: Boolean = true) {
    if (!PixivNetworkConfig.shouldUseCompatibilityClient()) {
        if (showMessage) {
            showMessage("Host/IP 兼容路由当前未启用")
        }
        return
    }
    viewModelScope.launch {
        runCatching { refreshAndPersistDns().requireAnyUpdated() }
            .onSuccess { result ->
                dnsWarmupAttempted = true
                _homeState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = result.summary,
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = result.errors.values.firstOrNull(),
                            ),
                        ),
                    )
                }
                if (showMessage) {
                    showMessage(result.summary)
                }
            }
            .onFailure { error ->
                _homeState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = "DNS 更新失败",
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = error.readableMessage(),
                            ),
                        ),
                    )
                }
                if (showMessage) {
                    showMessage(error.readableMessage())
                }
            }
    }
}
