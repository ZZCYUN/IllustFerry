package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.auth.OAuthPkce
import JunZi.Pixiv.data.model.AuthSession
import JunZi.Pixiv.data.network.PixivNetworkConfig
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.updateAccessToken(value: String) = _authState.update { it.copy(accessTokenInput = value) }
internal fun PixivViewModel.updateRefreshToken(value: String) = _authState.update { it.copy(refreshTokenInput = value) }
internal fun PixivViewModel.updateAuthCode(value: String) = _authState.update { it.copy(authCodeInput = value) }
internal fun PixivViewModel.saveManualToken() {
    val state = _authState.value
    val token = state.accessTokenInput.trim().removePrefix("Bearer ").trim()
    if (token.isBlank()) {
        showMessage("请输入 access token")
        return
    }
    val session = AuthSession(
        accessToken = token,
        refreshToken = state.refreshTokenInput.trim().ifBlank { null },
    )
    store.save(session)
    resetNavigation(AppScreen.Home)
    _authState.update { it.copy(session = session) }
    showMessage("已保存登录信息")
    loadHome(refresh = true)
}
internal fun PixivViewModel.startWebLogin() {
    val verifier = OAuthPkce.generateVerifier()
    store.saveCodeVerifier(verifier)
    navigateTo(AppScreen.WebLogin)
    _authState.update { it.copy(loginUrl = "") }
    showMessage(if (_settingsState.value.useHostIpRouting) "正在准备免代理登录" else "正在准备系统网络登录")
    viewModelScope.launch {
        val routeResult = if (PixivNetworkConfig.shouldUseCompatibilityClient()) {
            runCatching { warmupDnsIfNeeded(forceRefresh = true) }
        } else {
            Result.success(Unit)
        }
        _authState.update {
            it.copy(
                loginUrl = if (routeResult.isSuccess) OAuthPkce.loginUrl(verifier) else "",
            )
        }
        showMessage(
            routeResult.exceptionOrNull()?.let { error ->
                "动态 Host IP 获取失败，等待获取成功后再继续：${error.readableMessage()}"
            }
        )
    }
}
internal fun PixivViewModel.exchangeAuthCode(codeOverride: String? = null, useNetworkProxy: Boolean = true) {
    val state = _authState.value
    val code = (codeOverride ?: state.authCodeInput).trim()
    val verifier = store.readCodeVerifier()
    if (code.isBlank() || verifier.isNullOrBlank()) {
        showMessage("缺少授权 code 或 code verifier，请先生成网页登录链接")
        return
    }

    viewModelScope.launch {
        runBusy {
            warmupDnsIfNeeded()
            val session = repository.exchangeCode(code, verifier, useNetworkProxy)
            store.save(session)
            resetNavigation(AppScreen.Home)
            _authState.update {
                it.copy(
                    session = session,
                    accessTokenInput = session.accessToken,
                    refreshTokenInput = session.refreshToken.orEmpty(),
                    authCodeInput = "",
                )
            }
            showMessage("登录成功")
            loadHome(refresh = true)
        }
    }
}
internal fun PixivViewModel.logout() {
    store.clear()
    previewBackStack.clear()
    resetNavigation(AppScreen.Me)
    _authState.update {
        it.copy(
            session = null,
            accessTokenInput = "",
            refreshTokenInput = "",
        )
    }
    _homeState.update { it.copy(home = HomeState()) }
    _mineState.update { it.copy(mine = MyState()) }
    _authorState.update { it.copy(author = AuthorState()) }
    _searchState.update {
        it.copy(
            trendingTags = emptyList(),
            discover = DiscoverState(),
            items = emptyList(),
            nextUrl = null,
            isSearchActive = false,
        )
    }
    _previewState.update {
        it.copy(
            selectedIllust = null,
            selectedBookmark = SelectedBookmarkState(),
            related = FeedState(),
            comments = CommentState(),
        )
    }
    showMessage("已退出登录")
}
internal fun PixivViewModel.requireLogin() {
    showMessage("请先在我的页面登录")
}
