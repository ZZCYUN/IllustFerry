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

internal fun PixivViewModel.openFullScreenPreview(startIndex: Int = _uiState.value.selectedImageIndex) {
    _uiState.update { state ->
        val size = state.selectedIllust?.previewImageCount() ?: 0
        state.copy(
            selectedImageIndex = startIndex.coerceIn(0, (size - 1).coerceAtLeast(0)),
            isFullScreenPreview = true,
        )
    }
}
internal fun PixivViewModel.closeFullScreenPreview() {
    _uiState.update { it.copy(isFullScreenPreview = false) }
}
internal fun PixivViewModel.loadRelated(refresh: Boolean = false) {
    val illustId = _uiState.value.selectedIllust?.id ?: return
    loadRelatedFeed(illustId, refresh)
}
internal fun PixivViewModel.openPreview(illust: Illust) {
    if (illust.type.equals("novel", ignoreCase = true)) {
        openNovelReader(illust)
        return
    }
    val state = _uiState.value
    val currentScreen = state.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.Preview } ?: state.previewReturnScreen
    if (currentScreen == AppScreen.Preview) {
        previewBackStack.addLast(state.toPreviewSnapshot())
    } else {
        previewBackStack.clear()
        pushBackStack(returnScreen)
    }
    _uiState.update {
        it.copy(
            selectedIllust = illust,
            selectedBookmark = SelectedBookmarkState(),
            selectedImageIndex = 0,
            related = FeedState(),
            comments = CommentState(),
            ugoiraFrames = emptyList(),
            ugoiraLoadedFrames = 0,
            ugoiraTotalFrames = 0,
            previewReturnScreen = returnScreen,
            screen = AppScreen.Preview,
            message = null,
        )
    }
    loadSelectedBookmarkDetail(illust.id, force = true)
    refreshPreviewDetail(illust)
    saveHistory(illust)
}
internal fun PixivViewModel.openFullScreenPreviewForIllust(illust: Illust) {
    openPreview(illust)
    _uiState.update { it.copy(isFullScreenPreview = true) }
}
internal fun PixivViewModel.openDownloadedPreview(item: DownloadItem) {
    val savedUris = item.savedUris.orEmpty()
        .mapNotNull(::nonBlankStringOrNull)
        .ifEmpty { listOfNotNull(item.savedUri?.takeIf { it.isNotBlank() }) }
    val firstSavedUri = savedUris.firstOrNull()
    if (firstSavedUri == null) {
        _uiState.update { it.copy(message = "没有可预览的本地文件") }
        return
    }
    val localPages = savedUris.map {
        JunZi.Pixiv.data.model.IllustImagePage(
            url = it,
            width = item.illust?.width ?: 1,
            height = item.illust?.height ?: 1,
        )
    }
    val sourceIllust = item.illust ?: Illust(
        id = item.illustId,
        title = item.title,
        authorId = 0L,
        authorName = "",
        authorAccount = "",
        authorAvatarUrl = null,
        type = "illust",
        caption = "",
        previewUrl = firstSavedUri,
        imageUrls = savedUris,
        imagePages = localPages,
        tags = emptyList(),
        pageCount = savedUris.size,
        width = item.illust?.width ?: 1,
        height = item.illust?.height ?: 1,
        totalBookmarks = 0,
        totalView = 0,
        isBookmarked = false,
        aiType = null,
        createDate = null,
    )
    val currentScreen = _uiState.value.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.Preview } ?: _uiState.value.previewReturnScreen
    val localIllust = sourceIllust.copy(
        type = if (item.isUgoira) "illust" else sourceIllust.type,
        previewUrl = firstSavedUri,
        imageUrls = savedUris,
        imagePages = savedUris.map {
            JunZi.Pixiv.data.model.IllustImagePage(
                url = it,
                width = sourceIllust.width,
                height = sourceIllust.height,
            )
        },
        pageCount = savedUris.size,
    )
    if (currentScreen == AppScreen.Preview) {
        previewBackStack.addLast(_uiState.value.toPreviewSnapshot())
    } else {
        previewBackStack.clear()
        pushBackStack(returnScreen)
    }
    _uiState.update {
        it.copy(
            selectedIllust = localIllust,
            selectedBookmark = SelectedBookmarkState(),
            selectedImageIndex = 0,
            related = FeedState(),
            comments = CommentState(),
            ugoiraFrames = emptyList(),
            ugoiraLoadedFrames = 0,
            ugoiraTotalFrames = 0,
            isPreviewLoading = false,
            isFullScreenPreview = false,
            previewReturnScreen = returnScreen,
            screen = AppScreen.Preview,
            message = null,
        )
    }
    if (item.illustId > 0L) {
        loadSelectedBookmarkDetail(item.illustId, force = true)
        loadRelatedFeed(item.illustId, refresh = true)
        loadComments(item.illustId)
        saveHistory(localIllust)
    }
}
internal fun PixivViewModel.openPreviewById(illustId: Long, fullScreen: Boolean = false) {
    val session = _uiState.value.session
    if (session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        runBusy {
            val illust = withAccessToken { token -> repository.detail(illustId, token) }
            openPreview(illust)
            if (fullScreen) {
                _uiState.update { it.copy(isFullScreenPreview = true) }
            }
        }
    }
}
internal fun PixivViewModel.selectImage(index: Int) {
    _uiState.update { state ->
        val size = state.selectedIllust?.previewImageCount() ?: 0
        val selectedIndex = index.coerceIn(0, (size - 1).coerceAtLeast(0))
        if (state.selectedImageIndex == selectedIndex) {
            state
        } else {
            state.copy(selectedImageIndex = selectedIndex)
        }
    }
}
internal fun PixivViewModel.updateCommentInput(value: String) {
    _uiState.update { it.copy(comments = it.comments.copy(input = value.take(500))) }
}
internal fun PixivViewModel.sendComment() {
    val state = _uiState.value
    val illustId = state.selectedIllust?.id
    val comment = state.comments.input.trim()
    if (illustId == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
        return
    }
    if (comment.isBlank()) {
        _uiState.update { it.copy(message = "请输入评论内容") }
        return
    }
    if (state.comments.isSending) return
    val isNovel = state.selectedIllust.type.equals("novel", ignoreCase = true)

    viewModelScope.launch {
        _uiState.update { it.copy(comments = it.comments.copy(isSending = true, error = null)) }
        runCatching {
            withAccessToken { token ->
                if (isNovel) repository.addNovelComment(illustId, comment, token)
                else repository.addComment(illustId, comment, token)
            }
        }.onSuccess {
            _uiState.update {
                it.copy(
                    comments = it.comments.copy(isSending = false, input = ""),
                    message = "评论已发送",
                )
            }
            loadComments(illustId)
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    comments = it.comments.copy(isSending = false, error = error.readableMessage()),
                    message = error.readableMessage(),
                )
            }
        }
    }
}
internal fun PixivViewModel.closePreview() = goBack()
internal fun PixivViewModel.refreshPreviewDetail(illust: Illust) {
    val token = _uiState.value.session?.accessToken ?: return
    viewModelScope.launch {
        _uiState.update { it.copy(isPreviewLoading = true) }
        runCatching { withAccessToken { repository.detail(illust.id, it) } }
            .onSuccess { detail ->
                if (_uiState.value.selectedIllust?.id != illust.id) return@onSuccess
                _uiState.update { it.copy(selectedIllust = detail, isPreviewLoading = detail.isUgoira) }
                saveHistory(detail)
                loadSelectedBookmarkDetail(detail.id, force = true)
                loadRelatedFeed(detail.id, refresh = true)
                loadComments(detail.id)
                if (detail.isUgoira) {
                    loadUgoiraFrames(detail.id)
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isPreviewLoading = false, message = error.readableMessage()) }
            }
    }
}
internal fun PixivViewModel.loadUgoiraFrames(illustId: Long) {
    viewModelScope.launch {
        runCatching {
            withAccessToken { token ->
                repository.ugoiraFrames(illustId, token) { loaded, total ->
                    _uiState.update {
                        if (it.selectedIllust?.id == illustId) {
                            it.copy(ugoiraLoadedFrames = loaded, ugoiraTotalFrames = total)
                        } else {
                            it
                        }
                    }
                }
            }
        }
            .onSuccess { frames ->
                _uiState.update {
                    it.copy(
                        ugoiraFrames = frames,
                        ugoiraLoadedFrames = frames.size,
                        ugoiraTotalFrames = frames.size.coerceAtLeast(it.ugoiraTotalFrames),
                        isPreviewLoading = false,
                        message = if (frames.isEmpty()) "未能取得动画帧数据" else null,
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isPreviewLoading = false, message = error.readableMessage()) }
            }
    }
}
internal fun PixivViewModel.loadRelatedFeed(illustId: Long, refresh: Boolean) {
    val state = _uiState.value
    if (state.session?.accessToken.isNullOrBlank()) return
    if (state.related.isLoading) return

    viewModelScope.launch {
        _uiState.update { it.copy(related = it.related.copy(isLoading = true, error = null)) }
        runCatching {
            withAccessToken { accessToken ->
                if (refresh) {
                    repository.related(illustId, accessToken)
                } else {
                    val nextUrl = _uiState.value.related.nextUrl ?: return@withAccessToken null
                    repository.nextPage(nextUrl, accessToken)
                }
            }
        }.onSuccess { page ->
            if (_uiState.value.selectedIllust?.id != illustId) return@onSuccess
            if (page == null) {
                _uiState.update { it.copy(related = it.related.copy(isLoading = false)) }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { current ->
                current.copy(
                    related = current.related.copy(
                        items = if (refresh) filteredPage.items else current.related.items + filteredPage.items,
                        nextUrl = filteredPage.nextUrl,
                        isLoading = false,
                        error = null,
                    ),
                )
            }
        }.onFailure { error ->
            if (_uiState.value.selectedIllust?.id != illustId) return@onFailure
            _uiState.update {
                it.copy(
                    related = it.related.copy(
                        isLoading = false,
                        error = error.readableMessage(),
                    ),
                )
            }
        }
    }
}
internal fun PixivViewModel.loadComments(illustId: Long) {
    val state = _uiState.value
    if (state.session?.accessToken.isNullOrBlank()) return
    if (state.comments.isLoading) return
    val isNovel = state.selectedIllust?.type?.equals("novel", ignoreCase = true) == true

    viewModelScope.launch {
        _uiState.update { it.copy(comments = it.comments.copy(isLoading = true, error = null)) }
        runCatching {
            withAccessToken { token ->
                if (isNovel) repository.novelComments(illustId, token)
                else repository.comments(illustId, token)
            }
        }
            .onSuccess { page ->
                if (_uiState.value.selectedIllust?.id != illustId) return@onSuccess
                _uiState.update {
                    it.copy(
                        comments = it.comments.copy(
                            items = page.items,
                            nextUrl = page.nextUrl,
                            isLoading = false,
                            error = null,
                        ),
                    )
                }
            }
            .onFailure { error ->
                if (_uiState.value.selectedIllust?.id != illustId) return@onFailure
                _uiState.update {
                    it.copy(comments = it.comments.copy(isLoading = false, error = error.readableMessage()))
                }
            }
    }
}
