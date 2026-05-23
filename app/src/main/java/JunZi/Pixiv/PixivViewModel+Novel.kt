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

internal fun PixivViewModel.openNovelReader(illust: Illust) {
    val state = _uiState.value
    val currentScreen = state.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.NovelReader && it != AppScreen.Preview }
        ?: state.previewReturnScreen
    if (currentScreen != AppScreen.NovelReader) {
        pushBackStack(returnScreen)
    }
    _uiState.update {
        it.copy(
            selectedIllust = illust,
            novelReader = NovelReaderState(
                isLoading = true,
                detail = null,
                text = "",
                error = null,
                returnScreen = returnScreen,
            ),
            comments = CommentState(),
            previewReturnScreen = returnScreen,
            screen = AppScreen.NovelReader,
            message = null,
        )
    }
    saveHistory(illust)
    loadNovelReader(illust.id)
    loadComments(illust.id)
}
internal fun PixivViewModel.loadNovelReader(novelId: Long) {
    if (novelId <= 0L) return
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        _uiState.update {
            it.copy(novelReader = it.novelReader.copy(isLoading = true, error = null))
        }
        runCatching {
            withAccessToken { token ->
                val detail = repository.novelDetail(novelId, token)
                val payload = repository.novelText(novelId, token)
                detail to payload
            }
        }.onSuccess { (detail, payload) ->
            if (_uiState.value.selectedIllust?.id != novelId) return@onSuccess
            _uiState.update {
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
            if (_uiState.value.selectedIllust?.id != novelId) return@onFailure
            _uiState.update {
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
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    val state = _uiState.value
    val currentScreen = state.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.Series }
        ?: state.previewReturnScreen
    if (currentScreen != AppScreen.Series) {
        pushBackStack(returnScreen)
    }
    _uiState.update {
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
            screen = AppScreen.Series,
            previewReturnScreen = returnScreen,
            message = null,
        )
    }
    loadSeries(refresh = true)
}
internal fun PixivViewModel.loadSeries(refresh: Boolean = false) {
    val state = _uiState.value
    val current = state.series
    if (current.seriesId <= 0L) return
    if (current.isLoading && !refresh) return
    if (!refresh && current.nextUrl.isNullOrBlank() && current.items.isNotEmpty()) return
    viewModelScope.launch {
        _uiState.update { it.copy(series = it.series.copy(isLoading = true, error = null)) }
        runCatching {
            withAccessToken { token ->
                if (refresh || current.items.isEmpty()) {
                    repository.illustSeries(current.seriesId, token)
                } else {
                    val next = _uiState.value.series.nextUrl ?: return@withAccessToken null
                    repository.nextPage(next, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _uiState.update { it.copy(series = it.series.copy(isLoading = false)) }
                return@onSuccess
            }
            if (_uiState.value.series.seriesId != current.seriesId) return@onSuccess
            _uiState.update {
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
            if (_uiState.value.series.seriesId != current.seriesId) return@onFailure
            _uiState.update {
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
    val illust = _uiState.value.selectedIllust ?: return
    if (!illust.type.equals("novel", ignoreCase = true)) return
    if (_uiState.value.novelReader.isBookmarkBusy) return
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        _uiState.update { it.copy(novelReader = it.novelReader.copy(isBookmarkBusy = true)) }
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
            _uiState.update {
                it.copy(
                    novelReader = it.novelReader.copy(isBookmarkBusy = false),
                    message = if (nowBookmarked) "已收藏小说" else "已取消收藏",
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    novelReader = it.novelReader.copy(isBookmarkBusy = false),
                    message = error.readableMessage(),
                )
            }
        }
    }
}
