package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.model.HomeCategory
import JunZi.Pixiv.data.model.RankingMode
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.updateRankingMode(mode: RankingMode) {
    val state = _homeState.value
    val category = mode.category
    val homeCategoryState = state.home.categoryState(category)
    if (state.home.category == category && homeCategoryState.rankingMode == mode) return
    _homeState.update {
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
internal fun PixivViewModel.updateRankingDate(value: String) = _homeState.update { it.copy(rankingDate = value.trim()) }
internal fun PixivViewModel.selectHomeCategory(category: HomeCategory) {
    val state = _homeState.value
    if (state.home.category == category) return
    val target = state.home.categoryState(category)
    _homeState.update {
        it.copy(
            home = it.home.copy(category = category),
            rankingMode = target.rankingMode,
        )
    }
    if (!target.hasLoaded && _authState.value.session != null) {
        loadCategoryFeeds(category, refresh = true)
    }
}
internal fun PixivViewModel.applyRankingFilters() {
    val category = _homeState.value.home.category
    _homeState.update { state ->
        state.copy(
            home = state.home.withCategoryState(category) { it.copy(ranking = FeedState()) },
        )
    }
    loadHomeFeed(category, HomeFeed.Ranking, refresh = true)
}
internal fun PixivViewModel.clearRankingDate() {
    if (_homeState.value.rankingDate.isBlank()) return
    _homeState.update { it.copy(rankingDate = "") }
    applyRankingFilters()
}
internal fun PixivViewModel.loadHome(refresh: Boolean = true) {
    val state = _homeState.value
    if (!refresh && state.home.hasLoaded) return
    _homeState.update { it.copy(home = it.home.copy(hasLoaded = true)) }
    if (_authState.value.session == null) {
        loadHomeFeed(state.home.category, HomeFeed.Walkthrough, refresh = true)
        return
    }
    loadCategoryFeeds(state.home.category, refresh = true)
}
internal fun PixivViewModel.loadCategoryFeeds(category: HomeCategory, refresh: Boolean) {
    _homeState.update {
        it.copy(home = it.home.withCategoryState(category) { state -> state.copy(hasLoaded = true) })
    }
    listOf(HomeFeed.Recommended, HomeFeed.Ranking, HomeFeed.Latest).forEach {
        loadHomeFeed(category, it, refresh = refresh)
    }
}
internal fun PixivViewModel.loadHomeFeed(feed: HomeFeed, refresh: Boolean = false) {
    loadHomeFeed(_homeState.value.home.category, feed, refresh)
}
internal fun PixivViewModel.loadHomeFeed(category: HomeCategory, feed: HomeFeed, refresh: Boolean = false) {
    val homeState = _homeState.value
    val token = _authState.value.session?.accessToken
    val anonymousWalkthrough = token.isNullOrBlank() && feed == HomeFeed.Walkthrough
    if (!anonymousWalkthrough && token.isNullOrBlank()) return
    val current = homeState.home.feed(category, feed)
    val requestRankingMode = homeState.home.categoryState(category).rankingMode
    val requestRankingDate = homeState.rankingDate.apiDateOrNull()
    if (current.isLoading) return

    viewModelScope.launch {
        _homeState.update { current ->
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
                        val nextUrl = _homeState.value.home.feed(category, feed).nextUrl ?: return@withAccessToken null
                        if (category == HomeCategory.Novel) {
                            repository.nextNovelPage(nextUrl, accessToken)
                        } else {
                            repository.nextPage(nextUrl, accessToken)
                        }
                    }
                }
            }
        }.onSuccess { page ->
            if (feed == HomeFeed.Ranking && rankingRequestChanged(category, requestRankingMode, requestRankingDate)) {
                return@onSuccess
            }
            if (page == null) {
                _homeState.update { current ->
                    current.copy(
                        home = current.home.withFeed(category, feed) { it.copy(isLoading = false) },
                    )
                }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _homeState.update { current ->
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
            if (feed == HomeFeed.Ranking && rankingRequestChanged(category, requestRankingMode, requestRankingDate)) {
                return@onFailure
            }
            _homeState.update { current ->
                current.copy(
                    home = current.home.withFeed(category, feed) {
                        it.copy(isLoading = false, error = error.readableMessage())
                    },
                )
            }
        }
    }
}
