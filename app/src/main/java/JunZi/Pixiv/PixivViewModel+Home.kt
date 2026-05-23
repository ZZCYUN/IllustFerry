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

internal fun PixivViewModel.updateRankingMode(mode: RankingMode) {
    val state = _uiState.value
    val category = mode.category
    val homeCategoryState = state.home.categoryState(category)
    if (state.home.category == category && homeCategoryState.rankingMode == mode) return
    _uiState.update {
        it.copy(
            home = it.home
                .copy(category = category)
                .withCategoryState(category) { cat ->
                    cat.copy(rankingMode = mode, ranking = FeedState())
                },
            rankingMode = mode,
        )
    }
    loadHomeFeed(category, HomeFeed.Ranking, refresh = true)
}
internal fun PixivViewModel.updateRankingDate(value: String) = _uiState.update { it.copy(rankingDate = value.trim()) }
internal fun PixivViewModel.selectHomeCategory(category: HomeCategory) {
    val state = _uiState.value
    if (state.home.category == category) return
    val target = state.home.categoryState(category)
    _uiState.update {
        it.copy(
            home = it.home.copy(category = category),
            rankingMode = target.rankingMode,
        )
    }
    if (!target.hasLoaded && state.session != null) {
        loadCategoryFeeds(category, refresh = true)
    }
}
internal fun PixivViewModel.applyRankingFilters() {
    val category = _uiState.value.home.category
    _uiState.update { state ->
        state.copy(
            home = state.home.withCategoryState(category) { it.copy(ranking = FeedState()) },
        )
    }
    loadHomeFeed(category, HomeFeed.Ranking, refresh = true)
}
internal fun PixivViewModel.clearRankingDate() {
    if (_uiState.value.rankingDate.isBlank()) return
    _uiState.update { it.copy(rankingDate = "") }
    applyRankingFilters()
}
internal fun PixivViewModel.loadHome(refresh: Boolean = true) {
    val state = _uiState.value
    if (!refresh && state.home.hasLoaded) return
    _uiState.update { it.copy(home = it.home.copy(hasLoaded = true)) }
    if (state.session == null) {
        loadHomeFeed(state.home.category, HomeFeed.Walkthrough, refresh = true)
        return
    }
    loadCategoryFeeds(state.home.category, refresh = true)
}
internal fun PixivViewModel.loadCategoryFeeds(category: HomeCategory, refresh: Boolean) {
    _uiState.update {
        it.copy(home = it.home.withCategoryState(category) { state -> state.copy(hasLoaded = true) })
    }
    listOf(HomeFeed.Recommended, HomeFeed.Ranking, HomeFeed.Latest).forEach {
        loadHomeFeed(category, it, refresh = refresh)
    }
}
internal fun PixivViewModel.loadHomeFeed(feed: HomeFeed, refresh: Boolean = false) {
    loadHomeFeed(_uiState.value.home.category, feed, refresh)
}
internal fun PixivViewModel.loadHomeFeed(category: HomeCategory, feed: HomeFeed, refresh: Boolean = false) {
    val state = _uiState.value
    val token = state.session?.accessToken
    val anonymousWalkthrough = token.isNullOrBlank() && feed == HomeFeed.Walkthrough
    if (!anonymousWalkthrough && token.isNullOrBlank()) return
    val current = state.home.feed(category, feed)
    val requestRankingMode = state.home.categoryState(category).rankingMode
    val requestRankingDate = state.rankingDate.apiDateOrNull()
    if (current.isLoading) return

    viewModelScope.launch {
        _uiState.update { current ->
            current.copy(
                home = current.home.withFeed(category, feed) { it.copy(isLoading = true, error = null) },
            )
        }
        runCatching {
            if (anonymousWalkthrough) {
                warmupDnsIfNeeded()
                repository.walkthrough()
            } else {
                withAccessToken { accessToken ->
                    if (refresh) firstHomePage(category, feed, accessToken, requestRankingMode, requestRankingDate) else {
                        val nextUrl = _uiState.value.home.feed(category, feed).nextUrl ?: return@withAccessToken null
                        if (category == HomeCategory.Novel) {
                            repository.nextNovelPage(nextUrl, accessToken)
                        } else {
                            repository.nextPage(nextUrl, accessToken)
                        }
                    }
                }
            }
        }.onSuccess { page ->
            if (feed == HomeFeed.Ranking && _uiState.value.rankingRequestChanged(category, requestRankingMode, requestRankingDate)) {
                return@onSuccess
            }
            if (page == null) {
                _uiState.update { current ->
                    current.copy(
                        home = current.home.withFeed(category, feed) { it.copy(isLoading = false) },
                    )
                }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { current ->
                current.copy(
                    home = current.home.withFeed(category, feed) { old ->
                        old.copy(
                            items = if (refresh) filteredPage.items else old.items + filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isLoading = false,
                            error = null,
                        )
                    },
                )
            }
        }.onFailure { error ->
            if (feed == HomeFeed.Ranking && _uiState.value.rankingRequestChanged(category, requestRankingMode, requestRankingDate)) {
                return@onFailure
            }
            _uiState.update { current ->
                current.copy(
                    home = current.home.withFeed(category, feed) {
                        it.copy(isLoading = false, error = error.readableMessage())
                    },
                )
            }
        }
    }
}
