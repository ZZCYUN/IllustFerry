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

internal fun PixivViewModel.updateKeyword(value: String) = _uiState.update { it.copy(keyword = value) }
internal fun PixivViewModel.updateSearchKind(kind: SearchKind) {
    if (_uiState.value.searchKind == kind) return
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _uiState.update {
        it.copy(
            searchKind = kind,
            items = emptyList(),
            searchUsers = emptyList(),
            nextUrl = null,
            isSearchActive = if (shouldRefresh) it.isSearchActive else false,
        )
    }
    if (shouldRefresh) search()
}
internal fun PixivViewModel.updateSearchSort(sort: SearchSort) {
    if (_uiState.value.searchSort == sort) return
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _uiState.update { it.copy(searchSort = sort) }
    if (shouldRefresh) search()
}
internal fun PixivViewModel.updateSearchTarget(target: SearchTarget) {
    if (_uiState.value.searchTarget == target) return
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _uiState.update { it.copy(searchTarget = target) }
    if (shouldRefresh) search()
}
internal fun PixivViewModel.updateSearchStartDate(value: String) = _uiState.update { it.copy(searchStartDate = value.trim()) }
internal fun PixivViewModel.updateSearchEndDate(value: String) = _uiState.update { it.copy(searchEndDate = value.trim()) }
internal fun PixivViewModel.updateSearchBookmarkNum(value: String) {
    _uiState.update { it.copy(searchBookmarkNum = value.filter(Char::isDigit)) }
}
internal fun PixivViewModel.applySearchFilters() {
    search()
}
internal fun PixivViewModel.clearSearchFilters() {
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _uiState.update {
        it.copy(
            searchStartDate = "",
            searchEndDate = "",
            searchBookmarkNum = "",
        )
    }
    if (shouldRefresh) search()
}
internal fun PixivViewModel.loadTrendingTags(refresh: Boolean = false) {
    val state = _uiState.value
    if (state.session?.accessToken.isNullOrBlank()) return
    if (state.isTrendingLoading) return
    if (!refresh && state.trendingTags.isNotEmpty()) return
    _uiState.update { it.copy(isTrendingLoading = true) }

    viewModelScope.launch {
        runCatching { withAccessToken { repository.trendingTags(it) } }
            .onSuccess { tags ->
                _uiState.update { it.copy(trendingTags = tags, isTrendingLoading = false) }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isTrendingLoading = false,
                        message = error.readableMessage(),
                    )
                }
            }
    }
}
internal fun PixivViewModel.searchTrendingTag(tag: TrendingTag) {
    val keyword = tag.name.trim().trimStart('#')
    if (keyword.isBlank()) return
    navigateTo(AppScreen.Search)
    _uiState.update {
        it.copy(
            keyword = keyword,
        )
    }
    search()
}
internal fun PixivViewModel.searchTag(tag: String) {
    val keyword = tag.trim().trimStart('#')
    if (keyword.isBlank()) return
    navigateTo(AppScreen.Search)
    _uiState.update {
        it.copy(
            keyword = keyword,
            isFullScreenPreview = false,
        )
    }
    search()
}
internal fun PixivViewModel.submitHomeSearch() {
    if (_uiState.value.keyword.isBlank()) {
        _uiState.update { it.copy(message = "请输入搜索关键词") }
        return
    }
    navigateTo(AppScreen.Search)
    search()
}
internal fun PixivViewModel.returnToDiscover() {
    _uiState.update {
        it.copy(
            keyword = "",
            items = emptyList(),
            searchUsers = emptyList(),
            nextUrl = null,
            isSearchActive = false,
            isLoadingMore = false,
            message = null,
        )
    }
    loadDiscover(refresh = false)
}
internal fun PixivViewModel.loadDiscover(refresh: Boolean = false) {
    val state = _uiState.value
    if (state.session == null) {
        requireLogin()
        return
    }
    if (!refresh && state.discover.hasLoaded) return
    _uiState.update { it.copy(discover = it.discover.copy(hasLoaded = true)) }
    loadDiscoverFeed(DiscoverFeed.Public, refresh = true)
    loadDiscoverFeed(DiscoverFeed.Private, refresh = true)
}
internal fun PixivViewModel.loadDiscoverFeed(feed: DiscoverFeed, refresh: Boolean = false) {
    val state = _uiState.value
    if (state.session?.accessToken.isNullOrBlank()) return
    val current = state.discover.feed(feed)
    if (current.isLoading) return

    viewModelScope.launch {
        _uiState.update { currentState ->
            currentState.copy(discover = currentState.discover.withFeed(feed) { it.copy(isLoading = true, error = null) })
        }
        runCatching {
            withAccessToken { token ->
                if (refresh) {
                    val restrict = when (feed) {
                        DiscoverFeed.Public -> BookmarkRestrict.Public
                        DiscoverFeed.Private -> BookmarkRestrict.Private
                    }
                    repository.following(token, restrict)
                } else {
                    val nextUrl = _uiState.value.discover.feed(feed).nextUrl ?: return@withAccessToken null
                    repository.nextPage(nextUrl, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _uiState.update { currentState ->
                    currentState.copy(discover = currentState.discover.withFeed(feed) { it.copy(isLoading = false) })
                }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { currentState ->
                currentState.copy(
                    discover = currentState.discover.withFeed(feed) { old ->
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
            _uiState.update { currentState ->
                currentState.copy(
                    discover = currentState.discover.withFeed(feed) {
                        it.copy(isLoading = false, error = error.readableMessage())
                    },
                )
            }
        }
    }
}
internal fun PixivViewModel.search() {
    val state = _uiState.value
    val keyword = state.keyword.trim()
    val kind = state.searchKind
    val sort = state.searchSort
    val target = state.searchTarget
    val startDate = state.searchStartDate.apiDateOrNull()
    val endDate = state.searchEndDate.apiDateOrNull()
    val bookmarkNum = state.searchBookmarkNum.apiPositiveIntOrNull()
    if (state.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (keyword.isBlank()) {
        _uiState.update { it.copy(message = "请输入搜索关键词") }
        return
    }

    _uiState.update {
        it.copy(
            items = emptyList(),
            searchUsers = emptyList(),
            nextUrl = null,
            isSearchActive = true,
            isLoadingMore = false,
        )
    }
    viewModelScope.launch {
        runBusy {
            val result = withAccessToken { token ->
                when (kind) {
                    SearchKind.Illust -> SearchResult.Works(
                        repository.search(
                            keyword = keyword,
                            token = token,
                            sort = sort,
                            searchTarget = target,
                            startDate = startDate,
                            endDate = endDate,
                            bookmarkNum = bookmarkNum,
                        ).filteredBy(excludedTags()),
                    )
                    SearchKind.Novel -> SearchResult.Works(
                        repository.searchNovels(
                            keyword = keyword,
                            token = token,
                            sort = sort,
                            searchTarget = target,
                        ).filteredBy(excludedTags()),
                    )
                    SearchKind.User -> SearchResult.Users(repository.searchUsers(keyword, token))
                }
            }
            _uiState.update { current ->
                if (
                    current.keyword.trim() != keyword ||
                    current.searchKind != kind ||
                    current.searchSort != sort ||
                    current.searchTarget != target ||
                    current.searchStartDate.apiDateOrNull() != startDate ||
                    current.searchEndDate.apiDateOrNull() != endDate ||
                    current.searchBookmarkNum.apiPositiveIntOrNull() != bookmarkNum
                ) {
                    current
                } else {
                    when (result) {
                        is SearchResult.Works -> current.copy(
                            items = result.page.items,
                            searchUsers = emptyList(),
                            nextUrl = result.page.nextUrl,
                            isSearchActive = true,
                            message = null,
                        )
                        is SearchResult.Users -> current.copy(
                            items = emptyList(),
                            searchUsers = result.page.items,
                            nextUrl = result.page.nextUrl,
                            isSearchActive = true,
                            message = null,
                        )
                    }
                }
            }
        }
    }
}
internal fun PixivViewModel.loadMore() {
    val state = _uiState.value
    val nextUrl = state.nextUrl ?: return
    val token = state.session?.accessToken ?: return
    if (state.isLoadingMore || state.isBusy) return

    viewModelScope.launch {
        _uiState.update { it.copy(isLoadingMore = true, message = null) }
        runCatching {
            withAccessToken {
                when (state.searchKind) {
                    SearchKind.Illust -> SearchResult.Works(repository.nextPage(nextUrl, it).filteredBy(excludedTags()))
                    SearchKind.Novel -> SearchResult.Works(repository.nextNovelPage(nextUrl, it).filteredBy(excludedTags()))
                    SearchKind.User -> SearchResult.Users(repository.nextUserPage(nextUrl, it))
                }
            }
        }
            .onSuccess { result ->
                _uiState.update {
                    when (result) {
                        is SearchResult.Works -> it.copy(
                            items = it.items + result.page.items,
                            nextUrl = result.page.nextUrl,
                            isLoadingMore = false,
                        )
                        is SearchResult.Users -> it.copy(
                            searchUsers = it.searchUsers + result.page.items,
                            nextUrl = result.page.nextUrl,
                            isLoadingMore = false,
                        )
                    }
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoadingMore = false, message = error.readableMessage()) }
            }
    }
}
internal fun PixivViewModel.shouldRefreshSearchAfterParamChange(): Boolean {
    val state = _uiState.value
    return state.screen == AppScreen.Search &&
        state.keyword.isNotBlank() &&
        state.isSearchActive &&
        !state.isBusy
}
