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

internal fun PixivViewModel.updateAccessToken(value: String) = _uiState.update { it.copy(accessTokenInput = value) }
internal fun PixivViewModel.updateRefreshToken(value: String) = _uiState.update { it.copy(refreshTokenInput = value) }
internal fun PixivViewModel.updateAuthCode(value: String) = _uiState.update { it.copy(authCodeInput = value) }
internal fun PixivViewModel.saveManualToken() {
    val state = _uiState.value
    val token = state.accessTokenInput.trim().removePrefix("Bearer ").trim()
    if (token.isBlank()) {
        _uiState.update { it.copy(message = "请输入 access token") }
        return
    }
    val session = AuthSession(
        accessToken = token,
        refreshToken = state.refreshTokenInput.trim().ifBlank { null },
    )
    store.save(session)
    resetNavigation(AppScreen.Home)
    _uiState.update {
        it.copy(
            session = session,
            message = "已保存登录信息",
        )
    }
    loadHome(refresh = true)
}
internal fun PixivViewModel.startWebLogin() {
    val verifier = OAuthPkce.generateVerifier()
    store.saveCodeVerifier(verifier)
    navigateTo(AppScreen.WebLogin)
    _uiState.update {
        it.copy(
            loginUrl = "",
            message = if (_uiState.value.useHostIpRouting) {
                "正在准备免代理登录"
            } else {
                "正在准备系统网络登录"
            },
        )
    }
    viewModelScope.launch {
        val routeResult = if (PixivNetworkConfig.shouldUseCompatibilityClient()) {
            runCatching { warmupDnsIfNeeded(forceRefresh = true) }
        } else {
            Result.success(Unit)
        }
        _uiState.update {
            it.copy(
                loginUrl = if (routeResult.isSuccess) OAuthPkce.loginUrl(verifier) else "",
                message = routeResult.exceptionOrNull()?.let { error ->
                    "动态 Host IP 获取失败，等待获取成功后再继续：${error.readableMessage()}"
                },
            )
        }
    }
}
internal fun PixivViewModel.exchangeAuthCode(codeOverride: String? = null, useNetworkProxy: Boolean = true) {
    val state = _uiState.value
    val code = (codeOverride ?: state.authCodeInput).trim()
    val verifier = store.readCodeVerifier()
    if (code.isBlank() || verifier.isNullOrBlank()) {
        _uiState.update { it.copy(message = "缺少授权 code 或 code verifier，请先生成网页登录链接") }
        return
    }

    viewModelScope.launch {
        runBusy {
            warmupDnsIfNeeded()
            val session = repository.exchangeCode(code, verifier, useNetworkProxy)
            store.save(session)
            resetNavigation(AppScreen.Home)
            _uiState.update {
                it.copy(
                    session = session,
                    accessTokenInput = session.accessToken,
                    refreshTokenInput = session.refreshToken.orEmpty(),
                    authCodeInput = "",
                    message = "登录成功",
                )
            }
            loadHome(refresh = true)
        }
    }
}
internal fun PixivViewModel.logout() {
    store.clear()
    previewBackStack.clear()
    resetNavigation(AppScreen.Me)
    _uiState.update {
        it.copy(
            session = null,
            accessTokenInput = "",
            refreshTokenInput = "",
            home = HomeState(),
            mine = MyState(),
            author = AuthorState(),
            trendingTags = emptyList(),
            discover = DiscoverState(),
            items = emptyList(),
            nextUrl = null,
            isSearchActive = false,
            selectedIllust = null,
            selectedBookmark = SelectedBookmarkState(),
            related = FeedState(),
            comments = CommentState(),
            message = "已退出登录",
        )
    }
}
internal fun PixivViewModel.requireLogin() {
    _uiState.update { it.copy(message = "请先在我的页面登录") }
}
