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
            val cached = entry.illust
            if (cached != null) {
                return@async HistoryItem(
                    illust = cached,
                    viewedAtMillis = entry.viewedAtMillis,
                )
            }
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
        withNetworkRetry { block(session.accessToken) }
    } catch (error: PixivApiException) {
        if (error.code == 400 || error.code == 401) {
            session = refreshSession(session)
            withNetworkRetry { block(session.accessToken) }
        } else {
            throw error
        }
    }
}

/**
 * 网络层失败重连：请求开始时记下 DNS 代际，失败时——
 *  - 请求在途期间后台静默刷新已完成（代际变了）→ 直接用新 IP 重试一次；
 *  - 否则立即强制刷新一次（带 5s 冷却），确实拿到新 IP 才重试。
 */
internal suspend fun <T> PixivViewModel.withNetworkRetry(block: suspend () -> T): T {
    val requestGeneration = dnsRefreshGeneration.get()
    return try {
        block()
    } catch (error: java.io.IOException) {
        if (PixivNetworkConfig.useHostIpRouting && shouldRetryWithRefreshedDns(requestGeneration)) {
            block()
        } else {
            throw error
        }
    }
}

/**
 * 判断失败请求是否值得用刷新后的 IP 重连。5s 冷却防止持续失败时反复打 DoH。
 */
internal suspend fun PixivViewModel.shouldRetryWithRefreshedDns(requestGeneration: Long): Boolean {
    if (dnsRefreshGeneration.get() != requestGeneration) return true
    val now = System.currentTimeMillis()
    if (now - lastRetryDnsRefreshAtMillis < DNS_RETRY_REFRESH_MIN_INTERVAL_MS) return false
    lastRetryDnsRefreshAtMillis = now
    val result = runCatching { refreshAndPersistDns() }.getOrNull() ?: return false
    return result.updated.isNotEmpty() && dnsRefreshGeneration.get() != requestGeneration
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

internal fun PixivViewModel.applyDnsResult(dnsResult: JunZi.Pixiv.data.network.DnsRefreshResult) {
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

internal fun PixivViewModel.applyDnsFailure(error: Throwable, waiting: Boolean = false) {
    val hasFallbackIp = PixivNetworkConfig.hasAddressFor(APP_API_HOST)
    _homeState.update { state ->
        state.copy(
            home = state.home.copy(
                diagnostics = state.home.diagnostics.copy(
                    lastDnsResult = when {
                        hasFallbackIp -> "动态 Host IP 获取失败，使用持久化缓存"
                        waiting -> "动态 Host IP 获取失败，等待重试"
                        else -> "动态 Host IP 获取失败"
                    },
                    hostSnapshot = PixivNetworkConfig.snapshot(),
                    lastError = error.readableMessage(),
                ),
            ),
        )
    }
}

/**
 * 后台静默刷新动态 Host IP。启动时用持久化 IP 直接进首页，本函数在后台把全量刷新跑完，
 * 结果落表 + 持久化 + 代际 +1；期间失败的请求按代际变化用新 IP 重连。
 */
internal fun PixivViewModel.refreshDnsSilently() {
    viewModelScope.launch {
        runCatching { refreshAndPersistDns() }
            .onSuccess { dnsResult -> if (dnsResult.updated.isNotEmpty()) applyDnsResult(dnsResult) }
            .onFailure { error -> applyDnsFailure(error) }
    }
}

/**
 * 确保 app-api 至少有一个可路由 IP。
 *  - 已有 IP（含启动时从持久化恢复的）：立即返回，绝不阻塞请求；
 *    forceRefresh 只是把全量刷新转成后台静默任务。
 *  - 一个 IP 都没有（冷启动且无持久化缓存）：沿用阻塞重试，拿到首个 IP 才放行。
 */
internal suspend fun PixivViewModel.warmupDnsIfNeeded(forceRefresh: Boolean = false) {
    if (!forceRefresh && PixivNetworkConfig.hasAddressFor(APP_API_HOST)) {
        dnsWarmupAttempted = true
        return
    }
    dnsWarmupMutex.withLock {
        if (PixivNetworkConfig.hasAddressFor(APP_API_HOST)) {
            dnsWarmupAttempted = true
            if (forceRefresh) refreshDnsSilently()
            return
        }
        runCatching { refreshAndPersistDns().requireAnyUpdated() }
            .onSuccess { dnsResult ->
                dnsWarmupAttempted = true
                applyDnsResult(dnsResult)
            }
            .onFailure { error -> applyDnsFailure(error) }
        while (!PixivNetworkConfig.hasAddressFor(APP_API_HOST)) {
            delay(DNS_RETRY_DELAY_MS)
            runCatching { refreshAndPersistDns().requireAnyUpdated() }
                .onSuccess { dnsResult ->
                    dnsWarmupAttempted = true
                    applyDnsResult(dnsResult)
                }
                .onFailure { error -> applyDnsFailure(error, waiting = true) }
        }
    }
}
internal suspend fun PixivViewModel.refreshAndPersistDns(): JunZi.Pixiv.data.network.DnsRefreshResult {
    val result = repository.refreshDns()
    if (result.updated.isNotEmpty()) {
        store.saveDynamicHostIps(PixivNetworkConfig.snapshotIps())
        // 代际 +1：让在途/刚失败的请求感知到「有更新过的 IP 可用」。
        dnsRefreshGeneration.incrementAndGet()
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
