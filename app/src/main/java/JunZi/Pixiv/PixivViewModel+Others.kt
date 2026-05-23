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
    _uiState.update { it.copy(isBusy = true, message = null) }
    runCatching { block() }
        .onFailure { error -> _uiState.update { it.copy(message = error.readableMessage()) } }
    _uiState.update { it.copy(isBusy = false) }
}
internal suspend fun PixivViewModel.loadHistoryItems(entries: List<JunZi.Pixiv.data.local.HistoryEntry>, accessToken: String): List<HistoryItem> = coroutineScope {
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
    _uiState.update { it.copy(history = it.history.copy(isLoading = true, error = null)) }
    val entriesResult = runCatching { historyStore.recentPage(limit = HISTORY_PAGE_SIZE, offset = offset) }
    val entries = entriesResult.getOrDefault(emptyList())
    val itemsResult = runCatching { loadHistoryItems(entries, accessToken) }
    _uiState.update { state ->
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
    var session = requireNotNull(_uiState.value.session) { "请先登录" }
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
    _uiState.update {
        it.copy(
            session = refreshed,
            accessTokenInput = refreshed.accessToken,
            refreshTokenInput = refreshed.refreshToken.orEmpty(),
        )
    }
    return refreshed
}
internal suspend fun PixivViewModel.warmupDnsIfNeeded(forceRefresh: Boolean = false) {
    if (!_uiState.value.useHostIpRouting) return
    if (PixivNetworkConfig.isVpnActive) return
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
                _uiState.update { state ->
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
                _uiState.update { state ->
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
                    _uiState.update { state ->
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
                    _uiState.update { state ->
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
