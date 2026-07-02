package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.SearchSort
import JunZi.Pixiv.data.model.SearchTarget
import JunZi.Pixiv.data.model.TrendingTag
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.updateKeyword(value: String) = _searchState.update { it.copy(keyword = value) }
internal fun PixivViewModel.updateSearchKind(kind: SearchKind) {
    if (_searchState.value.searchKind == kind) return
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _searchState.update {
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
    if (_searchState.value.searchSort == sort) return
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _searchState.update { it.copy(searchSort = sort) }
    if (shouldRefresh) search()
}
internal fun PixivViewModel.updateSearchTarget(target: SearchTarget) {
    if (_searchState.value.searchTarget == target) return
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _searchState.update { it.copy(searchTarget = target) }
    if (shouldRefresh) search()
}
internal fun PixivViewModel.updateSearchStartDate(value: String) = _searchState.update { it.copy(searchStartDate = value.trim()) }
internal fun PixivViewModel.updateSearchEndDate(value: String) = _searchState.update { it.copy(searchEndDate = value.trim()) }
internal fun PixivViewModel.updateSearchBookmarkNum(value: String) {
    _searchState.update { it.copy(searchBookmarkNum = value.filter(Char::isDigit)) }
}
internal fun PixivViewModel.applySearchFilters() {
    search()
}
internal fun PixivViewModel.clearSearchFilters() {
    val shouldRefresh = shouldRefreshSearchAfterParamChange()
    _searchState.update {
        it.copy(
            searchStartDate = "",
            searchEndDate = "",
            searchBookmarkNum = "",
        )
    }
    if (shouldRefresh) search()
}
internal fun PixivViewModel.loadTrendingTags(refresh: Boolean = false) {
    val state = _searchState.value
    if (_authState.value.session?.accessToken.isNullOrBlank()) return
    if (state.isTrendingLoading) return
    if (!refresh && state.trendingTags.isNotEmpty()) return
    _searchState.update { it.copy(isTrendingLoading = true) }

    viewModelScope.launch {
        runCatching { withAccessToken { repository.trendingTags(it) } }
            .onSuccess { tags ->
                _searchState.update { it.copy(trendingTags = tags, isTrendingLoading = false) }
            }
            .onFailure { error ->
                _searchState.update { it.copy(isTrendingLoading = false) }
                showMessage(error.readableMessage())
            }
    }
}
internal fun PixivViewModel.searchTrendingTag(tag: TrendingTag) {
    val keyword = tag.name.trim().trimStart('#')
    if (keyword.isBlank()) return
    navigateTo(AppScreen.Search)
    _searchState.update {
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
    _searchState.update {
        it.copy(
            keyword = keyword,
        )
    }
    _previewState.update { it.copy(isFullScreenPreview = false) }
    search()
}
internal fun PixivViewModel.submitHomeSearch() {
    if (_searchState.value.keyword.isBlank()) {
        showMessage("请输入搜索关键词")
        return
    }
    navigateTo(AppScreen.Search)
    search()
}
internal fun PixivViewModel.returnToDiscover() {
    _searchState.update {
        it.copy(
            keyword = "",
            items = emptyList(),
            searchUsers = emptyList(),
            nextUrl = null,
            isSearchActive = false,
            isLoadingMore = false,
        )
    }
    showMessage(null)
    loadDiscover(refresh = false)
}
internal fun PixivViewModel.loadDiscover(refresh: Boolean = false) {
    val state = _searchState.value
    if (_authState.value.session == null) {
        requireLogin()
        return
    }
    if (!refresh && state.discover.hasLoaded) return
    _searchState.update { it.copy(discover = it.discover.copy(hasLoaded = true)) }
    loadDiscoverFeed(DiscoverFeed.Public, refresh = true)
    loadDiscoverFeed(DiscoverFeed.Private, refresh = true)
}
internal fun PixivViewModel.loadDiscoverFeed(feed: DiscoverFeed, refresh: Boolean = false) {
    val state = _searchState.value
    if (_authState.value.session?.accessToken.isNullOrBlank()) return
    val current = state.discover.feed(feed)
    if (current.isLoading) return

    viewModelScope.launch {
        _searchState.update { currentState ->
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
                    val nextUrl = _searchState.value.discover.feed(feed).nextUrl ?: return@withAccessToken null
                    repository.nextPage(nextUrl, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _searchState.update { currentState ->
                    currentState.copy(discover = currentState.discover.withFeed(feed) { it.copy(isLoading = false) })
                }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _searchState.update { currentState ->
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
            _searchState.update { currentState ->
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
    val state = _searchState.value
    val keyword = state.keyword.trim()
    val kind = state.searchKind
    val sort = state.searchSort
    val target = state.searchTarget
    val startDate = state.searchStartDate.apiDateOrNull()
    val endDate = state.searchEndDate.apiDateOrNull()
    val bookmarkNum = state.searchBookmarkNum.apiPositiveIntOrNull()
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (keyword.isBlank()) {
        showMessage("请输入搜索关键词")
        return
    }

    _searchState.update {
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
            val paramsChanged = _searchState.value.let { cur ->
                cur.keyword.trim() != keyword ||
                    cur.searchKind != kind ||
                    cur.searchSort != sort ||
                    cur.searchTarget != target ||
                    cur.searchStartDate.apiDateOrNull() != startDate ||
                    cur.searchEndDate.apiDateOrNull() != endDate ||
                    cur.searchBookmarkNum.apiPositiveIntOrNull() != bookmarkNum
            }
            if (!paramsChanged) showMessage(null)
            _searchState.update { current ->
                if (paramsChanged) {
                    current
                } else {
                    when (result) {
                        is SearchResult.Works -> current.copy(
                            items = result.page.items,
                            searchUsers = emptyList(),
                            nextUrl = result.page.nextUrl,
                            isSearchActive = true,
                        )
                        is SearchResult.Users -> current.copy(
                            items = emptyList(),
                            searchUsers = result.page.items,
                            nextUrl = result.page.nextUrl,
                            isSearchActive = true,
                        )
                    }
                }
            }
        }
    }
}
internal fun PixivViewModel.loadMore() {
    val state = _searchState.value
    val nextUrl = state.nextUrl ?: return
    val token = _authState.value.session?.accessToken ?: return
    if (state.isLoadingMore || _shellState.value.isBusy) return

    viewModelScope.launch {
        _searchState.update { it.copy(isLoadingMore = true) }
        showMessage(null)
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
                _searchState.update {
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
                _searchState.update { it.copy(isLoadingMore = false) }
                showMessage(error.readableMessage())
            }
    }
}
internal fun PixivViewModel.shouldRefreshSearchAfterParamChange(): Boolean {
    val state = _searchState.value
    val shell = _shellState.value
    return shell.screen == AppScreen.Search &&
        state.keyword.isNotBlank() &&
        state.isSearchActive &&
        !shell.isBusy
}
