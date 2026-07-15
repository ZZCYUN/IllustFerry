package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.model.Illust
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.openFullScreenPreview(startIndex: Int = _previewState.value.selectedImageIndex) {
    _previewState.update { state ->
        val size = state.selectedIllust?.previewImageCount() ?: 0
        state.copy(
            selectedImageIndex = startIndex.coerceIn(0, (size - 1).coerceAtLeast(0)),
            isFullScreenPreview = true,
        )
    }
}
internal fun PixivViewModel.closeFullScreenPreview() {
    _previewState.update { it.copy(isFullScreenPreview = false) }
}
internal fun PixivViewModel.loadRelated(refresh: Boolean = false) {
    val illustId = _previewState.value.selectedIllust?.id ?: return
    loadRelatedFeed(illustId, refresh)
}
internal fun PixivViewModel.openPreview(illust: Illust, alreadyFetchedDetail: Boolean = false) {
    if (illust.type.equals("novel", ignoreCase = true)) {
        openNovelReader(illust)
        return
    }
    val currentScreen = _shellState.value.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.Preview } ?: _previewState.value.previewReturnScreen
    if (currentScreen == AppScreen.Preview) {
        previewBackStack.addLast(toPreviewSnapshot())
    } else {
        previewBackStack.clear()
        pushBackStack(returnScreen)
    }
    _previewState.update {
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
        )
    }
    _shellState.update { it.copy(screen = AppScreen.Preview) }
    showMessage(null)
    loadSelectedBookmarkDetail(illust.id, force = true)
    refreshPreviewDetail(illust, skipDetailFetch = alreadyFetchedDetail)
    saveHistory(illust)
}
internal fun PixivViewModel.openFullScreenPreviewForIllust(illust: Illust) {
    openPreview(illust)
    _previewState.update { it.copy(isFullScreenPreview = true) }
}
internal fun PixivViewModel.openDownloadedPreview(item: DownloadItem) {
    val savedUris = item.savedUris.orEmpty()
        .mapNotNull(::nonBlankStringOrNull)
        .ifEmpty { listOfNotNull(item.savedUri?.takeIf { it.isNotBlank() }) }
    val firstSavedUri = savedUris.firstOrNull()
    if (firstSavedUri == null) {
        showMessage("没有可预览的本地文件")
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
    val currentScreen = _shellState.value.screen
    val returnScreen = currentScreen.takeIf { it != AppScreen.Preview } ?: _previewState.value.previewReturnScreen
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
        previewBackStack.addLast(toPreviewSnapshot())
    } else {
        previewBackStack.clear()
        pushBackStack(returnScreen)
    }
    _previewState.update {
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
        )
    }
    _shellState.update { it.copy(screen = AppScreen.Preview) }
    showMessage(null)
    if (item.illustId > 0L) {
        loadSelectedBookmarkDetail(item.illustId, force = true)
        loadRelatedFeed(item.illustId, refresh = true)
        loadComments(item.illustId)
        saveHistory(localIllust)
    }
}
internal fun PixivViewModel.openPreviewById(illustId: Long, fullScreen: Boolean = false) {
    val session = _authState.value.session
    if (session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    viewModelScope.launch {
        runBusy {
            val illust = withAccessToken { token -> repository.detail(illustId, token) }
            openPreview(illust, alreadyFetchedDetail = true)
            if (fullScreen) {
                _previewState.update { it.copy(isFullScreenPreview = true) }
            }
        }
    }
}
internal fun PixivViewModel.selectImage(index: Int) {
    _previewState.update { state ->
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
    _previewState.update { it.copy(comments = it.comments.copy(input = value.take(500))) }
}
internal fun PixivViewModel.sendComment() {
    val state = _previewState.value
    val illustId = state.selectedIllust?.id
    val comment = state.comments.input.trim()
    if (illustId == null) {
        showMessage("没有选中的作品")
        return
    }
    if (comment.isBlank()) {
        showMessage("请输入评论内容")
        return
    }
    if (state.comments.isSending) return
    val isNovel = state.selectedIllust.type.equals("novel", ignoreCase = true)

    viewModelScope.launch {
        _previewState.update { it.copy(comments = it.comments.copy(isSending = true, error = null)) }
        runCatching {
            withAccessToken { token ->
                if (isNovel) repository.addNovelComment(illustId, comment, token)
                else repository.addComment(illustId, comment, token)
            }
        }.onSuccess {
            _previewState.update {
                it.copy(
                    comments = it.comments.copy(isSending = false, input = ""),
                )
            }
            showMessage("评论已发送")
            loadComments(illustId)
        }.onFailure { error ->
            _previewState.update {
                it.copy(
                    comments = it.comments.copy(isSending = false, error = error.readableMessage()),
                )
            }
            showMessage(error.readableMessage())
        }
    }
}
internal fun PixivViewModel.closePreview() = goBack()
internal fun PixivViewModel.refreshPreviewDetail(illust: Illust, skipDetailFetch: Boolean = false) {
    val token = _authState.value.session?.accessToken ?: return
    viewModelScope.launch {
        if (skipDetailFetch) {
            _previewState.update { it.copy(selectedIllust = illust, isPreviewLoading = illust.isUgoira) }
            saveHistory(illust)
            loadRelatedFeed(illust.id, refresh = true)
            loadComments(illust.id)
            if (illust.isUgoira) {
                loadUgoiraFrames(illust.id)
            }
            return@launch
        }
        _previewState.update { it.copy(isPreviewLoading = true) }
        runCatching { withAccessToken { repository.detail(illust.id, it) } }
            .onSuccess { detail ->
                if (_previewState.value.selectedIllust?.id != illust.id) return@onSuccess
                _previewState.update { it.copy(selectedIllust = detail, isPreviewLoading = detail.isUgoira) }
                saveHistory(detail)
                loadRelatedFeed(detail.id, refresh = true)
                loadComments(detail.id)
                if (detail.isUgoira) {
                    loadUgoiraFrames(detail.id)
                }
            }
            .onFailure { error ->
                _previewState.update { it.copy(isPreviewLoading = false) }
                showMessage(error.readableMessage())
            }
    }
}
internal fun PixivViewModel.loadUgoiraFrames(illustId: Long) {
    viewModelScope.launch {
        runCatching {
            withAccessToken { token ->
                repository.ugoiraFrames(illustId, token) { loaded, total ->
                    _previewState.update {
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
                _previewState.update {
                    it.copy(
                        ugoiraFrames = frames,
                        ugoiraLoadedFrames = frames.size,
                        ugoiraTotalFrames = frames.size.coerceAtLeast(it.ugoiraTotalFrames),
                        isPreviewLoading = false,
                    )
                }
                showMessage(if (frames.isEmpty()) "未能取得动画帧数据" else null)
            }
            .onFailure { error ->
                _previewState.update { it.copy(isPreviewLoading = false) }
                showMessage(error.readableMessage())
            }
    }
}
internal fun PixivViewModel.loadRelatedFeed(illustId: Long, refresh: Boolean) {
    val state = _previewState.value
    if (_authState.value.session?.accessToken.isNullOrBlank()) return
    if (state.related.isLoading) return

    viewModelScope.launch {
        _previewState.update { it.copy(related = it.related.copy(isLoading = true, error = null)) }
        runCatching {
            withAccessToken { accessToken ->
                if (refresh) {
                    repository.related(illustId, accessToken)
                } else {
                    val nextUrl = _previewState.value.related.nextUrl ?: return@withAccessToken null
                    repository.nextPage(nextUrl, accessToken)
                }
            }
        }.onSuccess { page ->
            if (_previewState.value.selectedIllust?.id != illustId) return@onSuccess
            if (page == null) {
                _previewState.update { it.copy(related = it.related.copy(isLoading = false)) }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _previewState.update { current ->
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
            if (_previewState.value.selectedIllust?.id != illustId) return@onFailure
            _previewState.update {
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
    val state = _previewState.value
    if (_authState.value.session?.accessToken.isNullOrBlank()) return
    if (state.comments.isLoading) return
    val isNovel = state.selectedIllust?.type?.equals("novel", ignoreCase = true) == true

    viewModelScope.launch {
        _previewState.update { it.copy(comments = it.comments.copy(isLoading = true, error = null)) }
        runCatching {
            withAccessToken { token ->
                if (isNovel) repository.novelComments(illustId, token)
                else repository.comments(illustId, token)
            }
        }
            .onSuccess { page ->
                if (_previewState.value.selectedIllust?.id != illustId) return@onSuccess
                _previewState.update {
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
                if (_previewState.value.selectedIllust?.id != illustId) return@onFailure
                _previewState.update {
                    it.copy(comments = it.comments.copy(isLoading = false, error = error.readableMessage()))
                }
            }
    }
}
