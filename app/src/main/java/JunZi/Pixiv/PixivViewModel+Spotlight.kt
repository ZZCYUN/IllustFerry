package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.model.PixivCollection
import JunZi.Pixiv.data.model.SpotlightArticle
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val COLLECTION_BOOKS_PAGE_SIZE = 12

// ==================== 特辑（Spotlight / pixivision） ====================

/**
 * 打开底部「特辑」。再点一次底栏「特辑」= 关闭并回到底层主页面。
 */
internal fun PixivViewModel.openSpotlight() {
    if (_shellState.value.screen == AppScreen.Spotlight) {
        goBack()
        return
    }
    navigateTo(AppScreen.Spotlight)
    loadSpotlight(refresh = false)
}

internal fun PixivViewModel.selectSpotlightCategory(category: SpotlightCategory) {
    if (_spotlightState.value.selectedCategory == category) return
    _spotlightState.update { it.copy(selectedCategory = category) }
    loadSpotlight(refresh = false)
}

internal fun PixivViewModel.loadSpotlight(refresh: Boolean = false) {
    val category = _spotlightState.value.selectedCategory
    val current = _spotlightState.value.feeds[category]
    if (current?.isLoading == true) return
    if (!refresh && (current?.items?.isNotEmpty() == true || current?.error != null)) return

    // 分类页数据来自 pixivision 官网（匿名可读），无需登录。
    viewModelScope.launch {
        _spotlightState.update { state ->
            state.copy(
                feeds = state.feeds + (category to (state.feeds[category] ?: SpotlightFeedState())
                    .copy(isLoading = true, error = null)),
            )
        }
        runCatching {
            withNetworkRetry { repository.spotlightCategoryPage(category) }
        }.onSuccess { page ->
            _spotlightState.update { state ->
                state.copy(
                    feeds = state.feeds + (category to SpotlightFeedState(
                        items = page.items,
                        nextUrl = page.nextUrl,
                        isLoading = false,
                        error = null,
                    )),
                )
            }
        }.onFailure { error ->
            _spotlightState.update { state ->
                state.copy(
                    feeds = state.feeds + (category to (state.feeds[category] ?: SpotlightFeedState())
                        .copy(isLoading = false, error = error.readableMessage())),
                )
            }
        }
    }
}

/** 特辑翻页：抓取 feed.nextUrl（?p=N），追加条目。 */
internal fun PixivViewModel.loadMoreSpotlight() {
    val state = _spotlightState.value
    val category = state.selectedCategory
    val feed = state.feeds[category] ?: return
    if (feed.isLoading || feed.nextUrl.isNullOrBlank()) return

    viewModelScope.launch {
        _spotlightState.update { current ->
            current.copy(feeds = current.feeds + (category to feed.copy(isLoading = true)))
        }
        runCatching {
            withNetworkRetry { repository.spotlightCategoryPage(feed.nextUrl!!) }
        }.onSuccess { page ->
            _spotlightState.update { current ->
                val old = current.feeds[category] ?: SpotlightFeedState()
                val knownIds = old.items.map { it.id }.toSet()
                val fresh = page.items.filterNot { it.id in knownIds }
                current.copy(
                    feeds = current.feeds + (category to old.copy(
                        items = old.items + fresh,
                        nextUrl = page.nextUrl?.takeIf { fresh.isNotEmpty() || page.nextUrl != feed.nextUrl },
                        isLoading = false,
                        error = null,
                    )),
                )
            }
        }.onFailure { error ->
            _spotlightState.update { current ->
                val old = current.feeds[category] ?: SpotlightFeedState()
                current.copy(
                    feeds = current.feeds + (category to old.copy(
                        isLoading = false,
                        error = error.readableMessage(),
                    )),
                )
            }
        }
    }
}

/** 打开特辑文章：导航到原生文章页并加载正文（抓 pixivision 页面解析）。 */
internal fun PixivViewModel.openSpotlightArticle(article: SpotlightArticle) {
    navigateTo(AppScreen.SpotlightArticle)
    _spotlightState.update { it.copy(article = article, articleState = SpotlightArticleState(isLoading = true)) }
    loadSpotlightArticle(article.id)
}

internal fun PixivViewModel.loadSpotlightArticle(articleId: Long) {
    val existing = _spotlightState.value.articleState
    // 同一篇文章已成功解析过就直接复用，返回/重进不重复抓取。
    if (existing.error == null && existing.article?.articleId == articleId) return
    viewModelScope.launch {
        _spotlightState.update { it.copy(articleState = SpotlightArticleState(isLoading = true)) }
        runCatching {
            withNetworkRetry { repository.spotlightArticleDetail(articleId) }
        }.onSuccess { parsed ->
            if (_shellState.value.screen != AppScreen.SpotlightArticle) return@onSuccess
            _spotlightState.update {
                it.copy(articleState = SpotlightArticleState(article = parsed))
            }
        }.onFailure { error ->
            if (_shellState.value.screen != AppScreen.SpotlightArticle) return@onFailure
            _spotlightState.update {
                it.copy(articleState = SpotlightArticleState(error = error.readableMessage()))
            }
        }
    }
}

// ==================== 珍藏册（Collection，web ajax） ====================

internal fun PixivViewModel.loadCollectionTop(refresh: Boolean) {
    val state = _collectionBooksState.value
    if (state.isLoadingTop) return
    if (!refresh && state.top != null) return

    viewModelScope.launch {
        _collectionBooksState.update { it.copy(isLoadingTop = true, error = null) }
        runCatching {
            withNetworkRetry { repository.collectionTop() }
        }.onSuccess { top ->
            _collectionBooksState.update { it.copy(top = top, isLoadingTop = false, error = null) }
        }.onFailure { error ->
            _collectionBooksState.update {
                it.copy(isLoadingTop = false, error = error.readableMessage())
            }
        }
    }
}

/** 「全部」分页：search?mode=all，offset 递增追加。 */
internal fun PixivViewModel.loadMoreCollectionBooks() {
    val state = _collectionBooksState.value
    if (state.isLoadingMore || !state.hasMore && state.all.isNotEmpty()) return

    viewModelScope.launch {
        _collectionBooksState.update { it.copy(isLoadingMore = true, error = null) }
        runCatching {
            withNetworkRetry { repository.collectionBooks(limit = COLLECTION_BOOKS_PAGE_SIZE, offset = state.nextOffset) }
        }.onSuccess { items ->
            _collectionBooksState.update { current ->
                val knownIds = current.all.map { it.id }.toSet()
                val fresh = items.filterNot { it.id in knownIds }
                current.copy(
                    all = current.all + fresh,
                    nextOffset = current.nextOffset + items.size,
                    // 只有真的新增了条目才继续翻页，防止空转导致无限刷新。
                    hasMore = items.isNotEmpty() && fresh.isNotEmpty(),
                    isLoadingMore = false,
                    error = null,
                )
            }
        }.onFailure { error ->
            _collectionBooksState.update {
                it.copy(isLoadingMore = false, error = error.readableMessage())
            }
        }
    }
}

/** 打开珍藏册详情（带列表已知的摘要，正文随后加载）。 */
internal fun PixivViewModel.openCollectionBook(book: PixivCollection) {
    navigateTo(AppScreen.CollectionDetail)
    _collectionDetailState.update {
        it.copy(collection = book, detail = null, isLoading = true, error = null)
    }
    loadCollectionDetail(book.id)
}

internal fun PixivViewModel.loadCollectionDetail(collectionId: String) {
    viewModelScope.launch {
        _collectionDetailState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            withNetworkRetry { repository.collectionDetail(collectionId) }
        }.onSuccess { detail ->
            if (_shellState.value.screen != AppScreen.CollectionDetail) return@onSuccess
            _collectionDetailState.update {
                it.copy(
                    collection = it.collection ?: detail.collection,
                    detail = detail,
                    isLoading = false,
                    error = null,
                )
            }
        }.onFailure { error ->
            if (_shellState.value.screen != AppScreen.CollectionDetail) return@onFailure
            _collectionDetailState.update { it.copy(isLoading = false, error = error.readableMessage()) }
        }
    }
}
