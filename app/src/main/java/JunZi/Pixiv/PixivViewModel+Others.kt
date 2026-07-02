package JunZi.Pixiv

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.local.HistoryEntry
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.model.HomeCategory
import JunZi.Pixiv.data.model.IllustPage
import JunZi.Pixiv.data.model.RankingMode
import JunZi.Pixiv.data.network.PixivApiException
import JunZi.Pixiv.data.network.PixivNetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

internal suspend fun PixivViewModel.saveDownloadBytes(relativePath: String, fileName: String, bytes: ByteArray): Uri = withContext(Dispatchers.IO) {
    val app = getApplication<Application>()
    val cleanFileName = fileName.ifBlank { "simple_pixiv_${System.currentTimeMillis()}.jpg" }
    val cleanRelativePath = relativePath.ifBlank { Environment.DIRECTORY_PICTURES + "/IllustFerry" }
    val mimeType = cleanFileName.mimeType()
    val isImageTarget = mimeType.startsWith("image/")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, cleanFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, cleanRelativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val resolver = app.contentResolver
        val collection = if (isImageTarget) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }
        val uri = requireNotNull(resolver.insert(collection, values)) {
            "无法创建下载文件"
        }
        runCatching {
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("无法写入下载文件")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }.onFailure {
            resolver.delete(uri, null, null)
            throw it
        }
        uri
    } else {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(cleanRelativePath.substringBefore('/')),
            cleanRelativePath.substringAfter('/', ""),
        )
        if (!dir.exists() && !dir.mkdirs()) error("无法创建下载目录")
        val file = File(dir, cleanFileName)
        file.writeBytes(bytes)
        Uri.fromFile(file)
    }
}
internal suspend fun PixivViewModel.runBusy(block: suspend () -> Unit) {
    _shellState.update { it.copy(isBusy = true, message = null) }
    runCatching { block() }
        .onFailure { error -> showMessage(error.readableMessage()) }
    _shellState.update { it.copy(isBusy = false) }
}
internal suspend fun PixivViewModel.loadHistoryItems(entries: List<HistoryEntry>, accessToken: String): List<HistoryItem> = coroutineScope {
    entries.map { entry ->
        async {
            runCatching {
                val stored = entry.illustId
                val illust = if (stored < 0L) {
                    repository.novelAsIllust(-stored, accessToken)
                } else {
                    repository.detail(stored, accessToken)
                }
                HistoryItem(
                    illust = illust,
                    viewedAtMillis = entry.viewedAtMillis,
                )
            }.getOrNull()
        }
    }.awaitAll().filterNotNull()
}
internal suspend fun PixivViewModel.loadHistoryPage(
    accessToken: String,
    offset: Int,
    append: Boolean,
) {
    _mineState.update { it.copy(history = it.history.copy(isLoading = true, error = null)) }
    val entriesResult = runCatching { historyStore.recentPage(limit = HISTORY_PAGE_SIZE, offset = offset) }
    val entries = entriesResult.getOrDefault(emptyList())
    val itemsResult = runCatching { loadHistoryItems(entries, accessToken) }
    _mineState.update { state ->
        val previousItems = if (append) state.history.items else emptyList()
        state.copy(
            history = state.history.copy(
                items = previousItems + itemsResult.getOrDefault(emptyList()),
                isLoading = false,
                error = entriesResult.exceptionOrNull()?.readableMessage() ?: itemsResult.exceptionOrNull()?.readableMessage(),
                nextOffset = offset + entries.size,
                hasMore = entries.size >= HISTORY_PAGE_SIZE,
            ),
        )
    }
}
internal suspend fun PixivViewModel.firstHomePage(
    category: HomeCategory,
    feed: HomeFeed,
    token: String,
    rankingMode: RankingMode,
    rankingDate: String?,
): IllustPage {
    return when (category) {
        HomeCategory.Illust -> when (feed) {
            HomeFeed.Walkthrough -> repository.walkthrough(token)
            HomeFeed.Recommended -> repository.recommended(token)
            HomeFeed.Ranking -> repository.ranking(token, rankingMode, rankingDate)
            HomeFeed.Latest -> repository.latest(token)
        }
        HomeCategory.Manga -> when (feed) {
            HomeFeed.Walkthrough -> repository.walkthrough(token)
            HomeFeed.Recommended -> repository.recommendedManga(token)
            HomeFeed.Ranking -> repository.ranking(token, rankingMode, rankingDate)
            HomeFeed.Latest -> repository.latestManga(token)
        }
        HomeCategory.Novel -> when (feed) {
            HomeFeed.Walkthrough -> repository.recommendedNovels(token)
            HomeFeed.Recommended -> repository.recommendedNovels(token)
            HomeFeed.Ranking -> repository.rankingNovels(token, rankingMode, rankingDate)
            HomeFeed.Latest -> repository.latestNovels(token)
        }
    }
}
internal suspend fun <T> PixivViewModel.withAccessToken(block: suspend (String) -> T): T {
    warmupDnsIfNeeded()
    var session = requireNotNull(_authState.value.session) { "请先登录" }
    if (session.shouldRefresh()) {
        session = refreshSession(session)
    }
    return try {
        block(session.accessToken)
    } catch (error: PixivApiException) {
        if (error.code == 400 || error.code == 401) {
            session = refreshSession(session)
            block(session.accessToken)
        } else {
            throw error
        }
    }
}
internal suspend fun PixivViewModel.refreshSession(session: AuthSession): AuthSession {
    val refreshToken = session.refreshToken?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("登录已过期，且没有 refresh token")
    val refreshed = repository.refresh(refreshToken).withFallbackUser(session)
    store.save(refreshed)
    _authState.update {
        it.copy(
            session = refreshed,
            accessTokenInput = refreshed.accessToken,
            refreshTokenInput = refreshed.refreshToken.orEmpty(),
        )
    }
    return refreshed
}
internal suspend fun PixivViewModel.warmupDnsIfNeeded(forceRefresh: Boolean = false) {
    if (!forceRefresh && dnsWarmupAttempted && PixivNetworkConfig.hasAddressFor(APP_API_HOST)) return
    dnsWarmupMutex.withLock {
        if (!forceRefresh && dnsWarmupAttempted && PixivNetworkConfig.hasAddressFor(APP_API_HOST)) return
        if (!forceRefresh && PixivNetworkConfig.hasAddressFor(APP_API_HOST)) {
            dnsWarmupAttempted = true
            return
        }
        val result = runCatching { refreshAndPersistDns().requireAnyUpdated() }
        dnsWarmupAttempted = result.isSuccess
        result
            .onSuccess { dnsResult ->
                _homeState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = dnsResult.summary,
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = dnsResult.errors.values.firstOrNull(),
                            ),
                        ),
                    )
                }
            }
            .onFailure { error ->
                val hasPersistedOrRuntimeIp = PixivNetworkConfig.hasAddressFor(APP_API_HOST)
                _homeState.update { state ->
                    state.copy(
                        home = state.home.copy(
                            diagnostics = state.home.diagnostics.copy(
                                lastDnsResult = if (hasPersistedOrRuntimeIp) {
                                    "动态 Host IP 获取失败，使用持久化缓存"
                                } else {
                                    "动态 Host IP 获取失败"
                                },
                                hostSnapshot = PixivNetworkConfig.snapshot(),
                                lastError = error.readableMessage(),
                            ),
                        ),
                    )
                }
            }
        while (!PixivNetworkConfig.hasAddressFor(APP_API_HOST)) {
            delay(DNS_RETRY_DELAY_MS)
            val retry = runCatching { refreshAndPersistDns().requireAnyUpdated() }
            dnsWarmupAttempted = retry.isSuccess
            retry
                .onSuccess { dnsResult ->
                    _homeState.update { state ->
                        state.copy(
                            home = state.home.copy(
                                diagnostics = state.home.diagnostics.copy(
                                    lastDnsResult = dnsResult.summary,
                                    hostSnapshot = PixivNetworkConfig.snapshot(),
                                    lastError = dnsResult.errors.values.firstOrNull(),
                                ),
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _homeState.update { state ->
                        state.copy(
                            home = state.home.copy(
                                diagnostics = state.home.diagnostics.copy(
                                    lastDnsResult = "动态 Host IP 获取失败，等待重试",
                                    hostSnapshot = PixivNetworkConfig.snapshot(),
                                    lastError = error.readableMessage(),
                                ),
                            ),
                        )
                    }
                }
        }
    }
}
internal suspend fun PixivViewModel.refreshAndPersistDns(): JunZi.Pixiv.data.network.DnsRefreshResult {
    val result = repository.refreshDns()
    if (result.updated.isNotEmpty()) {
        store.saveDynamicHostIps(PixivNetworkConfig.snapshotIps())
    }
    return result
}
internal fun PixivViewModel.registerNetworkCallback() {
    runCatching {
        connectivityManager.registerNetworkCallback(
            android.net.NetworkRequest.Builder().build(),
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
        ?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN) == true
}
internal fun PixivViewModel.runDiagnostics() {
    val session = _authState.value.session
    if (session == null) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        _homeState.update { state ->
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
            ?: _homeState.value.home.illust.recommended.items.firstOrNull()?.previewUrl
        val imageResult = if (probeUrl != null) runCatching { repository.probeImage(probeUrl) } else null

        val firstError = dnsResult.exceptionOrNull()
            ?: apiResult.exceptionOrNull()
            ?: imageResult?.exceptionOrNull()
        _homeState.update { state ->
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
            )
        }
        firstError?.let { _shellState.update { s -> s.copy(message = it.readableMessage()) } }
            ?: showMessage(null)
    }
}
