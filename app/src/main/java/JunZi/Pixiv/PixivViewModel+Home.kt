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
    selectHomeTab(
        when (category) {
            HomeCategory.Illust -> HomeTopTab.Illust
            HomeCategory.Manga -> HomeTopTab.Manga
            HomeCategory.Novel -> HomeTopTab.Novel
        },
    )
}
/**
 * 首页顶部 Tab 切换：插画 / 漫画 / 小说 / 珍藏册。
 * 珍藏册 tab 走 web ajax（top 聚合 + 全部分页），其余 tab 维持原有 feed 逻辑。
 */
internal fun PixivViewModel.selectHomeTab(tab: HomeTopTab) {
    val state = _homeState.value
    if (state.home.topTab == tab) return
    _homeState.update { it.copy(home = it.home.copy(topTab = tab)) }
    val category = tab.category
    if (category == null) {
        loadCollectionTop(refresh = false)
        loadMoreCollectionBooks()
        return
    }
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
    // 子页面模式：只加载当前选中的推荐/排行/最新，其余在入口切换时按需加载。
    loadHomeFeed(category, _homeState.value.home.feedPage, refresh = refresh)
}

/** 首页入口行切换子页面（推荐/排行/最新）：原地替换下方 feed 区域。 */
internal fun PixivViewModel.selectHomeFeedPage(feed: HomeFeed) {
    val state = _homeState.value
    if (state.home.feedPage == feed) return
    _homeState.update { it.copy(home = it.home.copy(feedPage = feed)) }
    if (_authState.value.session == null) return
    val category = state.home.category
    val target = state.home.categoryState(category)
    val feedState = when (feed) {
        HomeFeed.Recommended -> target.recommended
        HomeFeed.Ranking -> target.ranking
        else -> target.latest
    }
    if (feedState.items.isEmpty()) {
        loadHomeFeed(category, feed, refresh = true)
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
                withNetworkRetry { repository.walkthrough() }
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
