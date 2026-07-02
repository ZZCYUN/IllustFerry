package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.model.Illust
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.openNovelReader(illust: Illust) {
    val currentScreen = _shellState.value.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.NovelReader && it != AppScreen.Preview }
        ?: _previewState.value.previewReturnScreen
    if (currentScreen != AppScreen.NovelReader) {
        pushBackStack(returnScreen)
    }
    _previewState.update {
        it.copy(
            selectedIllust = illust,
            comments = CommentState(),
            previewReturnScreen = returnScreen,
        )
    }
    _novelState.update {
        it.copy(
            novelReader = NovelReaderState(
                isLoading = true,
                detail = null,
                text = "",
                error = null,
                returnScreen = returnScreen,
            ),
        )
    }
    _shellState.update { it.copy(screen = AppScreen.NovelReader) }
    showMessage(null)
    saveHistory(illust)
    loadNovelReader(illust.id)
    loadComments(illust.id)
}
internal fun PixivViewModel.loadNovelReader(novelId: Long) {
    if (novelId <= 0L) return
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        _novelState.update {
            it.copy(novelReader = it.novelReader.copy(isLoading = true, error = null))
        }
        runCatching {
            withAccessToken { token ->
                val detail = repository.novelDetail(novelId, token)
                val payload = repository.novelText(novelId, token)
                detail to payload
            }
        }.onSuccess { (detail, payload) ->
            if (_previewState.value.selectedIllust?.id != novelId) return@onSuccess
            _novelState.update {
                it.copy(
                    novelReader = it.novelReader.copy(
                        isLoading = false,
                        detail = detail,
                        text = payload.text,
                        uploadedImages = payload.uploadedImages,
                        pixivImages = payload.pixivImages,
                        error = null,
                    ),
                )
            }
            markIllustBookmarked(novelId, isBookmarked = detail.isBookmarked)
        }.onFailure { error ->
            if (_previewState.value.selectedIllust?.id != novelId) return@onFailure
            _novelState.update {
                it.copy(
                    novelReader = it.novelReader.copy(
                        isLoading = false,
                        error = error.readableMessage(),
                    ),
                )
            }
        }
    }
}
internal fun PixivViewModel.closeNovelReader() {
    goBack()
}
internal fun PixivViewModel.openSeries(seriesId: Long, isNovel: Boolean = false, initialTitle: String = "") {
    if (seriesId <= 0L) return
    if (isNovel) return
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    val currentScreen = _shellState.value.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.Series }
        ?: _previewState.value.previewReturnScreen
    if (currentScreen != AppScreen.Series) {
        pushBackStack(returnScreen)
    }
    _novelState.update {
        it.copy(
            series = SeriesState(
                seriesId = seriesId,
                title = initialTitle,
                items = emptyList(),
                nextUrl = null,
                isLoading = true,
                error = null,
                returnScreen = returnScreen,
            ),
        )
    }
    _previewState.update { it.copy(previewReturnScreen = returnScreen) }
    _shellState.update { it.copy(screen = AppScreen.Series) }
    showMessage(null)
    loadSeries(refresh = true)
}
internal fun PixivViewModel.loadSeries(refresh: Boolean = false) {
    val current = _novelState.value.series
    if (current.seriesId <= 0L) return
    if (current.isLoading && !refresh) return
    if (!refresh && current.nextUrl.isNullOrBlank() && current.items.isNotEmpty()) return
    viewModelScope.launch {
        _novelState.update { it.copy(series = it.series.copy(isLoading = true, error = null)) }
        runCatching {
            withAccessToken { token ->
                if (refresh || current.items.isEmpty()) {
                    repository.illustSeries(current.seriesId, token)
                } else {
                    val next = _novelState.value.series.nextUrl ?: return@withAccessToken null
                    repository.nextPage(next, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _novelState.update { it.copy(series = it.series.copy(isLoading = false)) }
                return@onSuccess
            }
            if (_novelState.value.series.seriesId != current.seriesId) return@onSuccess
            _novelState.update {
                val merged = if (refresh) page.items else it.series.items + page.items
                val firstSeriesTitle = merged.firstNotNullOfOrNull { item -> item.seriesTitle?.takeIf { t -> t.isNotBlank() } }
                it.copy(
                    series = it.series.copy(
                        items = merged,
                        nextUrl = page.nextUrl,
                        title = it.series.title.ifBlank { firstSeriesTitle.orEmpty() },
                        isLoading = false,
                        error = null,
                    ),
                )
            }
        }.onFailure { error ->
            if (_novelState.value.series.seriesId != current.seriesId) return@onFailure
            _novelState.update {
                it.copy(
                    series = it.series.copy(
                        isLoading = false,
                        error = error.readableMessage(),
                    ),
                )
            }
        }
    }
}
internal fun PixivViewModel.loadMoreSeries() {
    loadSeries(refresh = false)
}
internal fun PixivViewModel.toggleNovelBookmark() {
    val illust = _previewState.value.selectedIllust ?: return
    if (!illust.type.equals("novel", ignoreCase = true)) return
    if (_novelState.value.novelReader.isBookmarkBusy) return
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        _novelState.update { it.copy(novelReader = it.novelReader.copy(isBookmarkBusy = true)) }
        runCatching {
            withAccessToken { token ->
                if (illust.isBookmarked) {
                    repository.deleteNovelBookmark(illust.id, token)
                } else {
                    repository.addNovelBookmark(illust.id, token)
                }
            }
        }.onSuccess {
            val nowBookmarked = !illust.isBookmarked
            markIllustBookmarked(illust.id, isBookmarked = nowBookmarked)
            _novelState.update {
                it.copy(
                    novelReader = it.novelReader.copy(isBookmarkBusy = false),
                )
            }
            showMessage(if (nowBookmarked) "已收藏小说" else "已取消收藏")
        }.onFailure { error ->
            _novelState.update {
                it.copy(
                    novelReader = it.novelReader.copy(isBookmarkBusy = false),
                )
            }
            showMessage(error.readableMessage())
        }
    }
}
