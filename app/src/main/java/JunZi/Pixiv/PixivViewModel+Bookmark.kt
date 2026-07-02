package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.Illust
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.bookmarkIllust(
    illustId: Long,
    restrict: BookmarkRestrict = BookmarkRestrict.Public,
    tags: List<String> = emptyList(),
    isNovel: Boolean = false,
) {
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (illustId <= 0L) {
        showMessage("作品 ID 无效")
        return
    }

    viewModelScope.launch {
        runBusy {
            val cleanTags = normalizeBookmarkTags(tags)
            withAccessToken {
                if (isNovel) {
                    repository.addNovelBookmark(illustId, it, restrict, cleanTags)
                } else {
                    repository.addBookmark(illustId, it, restrict, cleanTags)
                }
            }
            markIllustBookmarked(illustId, isBookmarked = true)
            _previewState.update { state ->
                if (state.selectedIllust?.id == illustId) {
                    state.copy(
                        selectedBookmark = SelectedBookmarkState(
                            isLoaded = true,
                            restrict = restrict,
                            tags = cleanTags,
                        ),
                    )
                } else {
                    state
                }
            }
            if (!isNovel) {
                refreshMyBookmarkTagsAfterChange()
                refreshMyBookmarksAfterChange(restrict, cleanTags)
            }
            val typeLabel = if (restrict == BookmarkRestrict.Private) "私密收藏" else "公开收藏"
            val tagsText = cleanTags.takeIf { it.isNotEmpty() }
                ?.joinToString("、", prefix = " · 标签 ") { "#$it" }
                .orEmpty()
            showMessage("已添加$typeLabel$tagsText")
        }
    }
}
internal fun PixivViewModel.addTagsToSelectedBookmark(tags: List<String>) {
    val illust = _previewState.value.selectedIllust
    if (illust == null) {
        showMessage("没有选中的作品")
        return
    }
    if (!illust.isBookmarked) {
        showMessage("请先收藏作品")
        return
    }
    val current = _previewState.value.selectedBookmark
    if (!current.isLoaded) {
        showMessage("收藏标签同步中，请稍后再试")
        loadSelectedBookmarkDetail(illust.id)
        return
    }
    val mergedTags = (current.tags + tags).distinctBy { it.lowercase() }
    updateSelectedBookmarkTags(mergedTags)
}
internal fun PixivViewModel.toggleSelectedBookmarkTag(tag: String) {
    val illust = _previewState.value.selectedIllust
    if (illust == null) {
        showMessage("没有选中的作品")
        return
    }
    if (!illust.isBookmarked) {
        showMessage("请先收藏作品")
        return
    }
    val cleanTag = normalizeBookmarkTag(tag)
    if (cleanTag == null) {
        showMessage("请输入收藏标签")
        return
    }
    val bookmark = _previewState.value.selectedBookmark
    if (!bookmark.isLoaded) {
        showMessage("收藏标签同步中，请稍后再试")
        loadSelectedBookmarkDetail(illust.id)
        return
    }
    val currentTags = bookmark.tags
    val updatedTags = if (currentTags.any { it.equals(cleanTag, ignoreCase = true) }) {
        currentTags.filterNot { it.equals(cleanTag, ignoreCase = true) }
    } else {
        (currentTags + cleanTag).distinctBy { it.lowercase() }
    }
    updateSelectedBookmarkTags(updatedTags, toggledTag = cleanTag)
}
internal fun PixivViewModel.updateSelectedBookmarkTags(tags: List<String>, toggledTag: String? = null) {
    val illust = _previewState.value.selectedIllust
    if (illust == null) {
        showMessage("没有选中的作品")
        return
    }
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    val cleanTags = normalizeBookmarkTags(tags)
    val currentBookmark = _previewState.value.selectedBookmark
    if (!currentBookmark.isLoaded) {
        showMessage("收藏标签同步中，请稍后再试")
        loadSelectedBookmarkDetail(illust.id)
        return
    }
    val restrict = currentBookmark.restrict
    viewModelScope.launch {
        runBusy {
            withAccessToken { repository.addBookmark(illust.id, it, restrict, cleanTags) }
            _previewState.update { state ->
                if (state.selectedIllust?.id == illust.id) {
                    state.copy(
                        selectedBookmark = state.selectedBookmark.copy(
                            isLoaded = true,
                            restrict = restrict,
                            tags = cleanTags,
                            error = null,
                        ),
                    )
                } else {
                    state
                }
            }
            refreshMyBookmarkTagsAfterChange()
            refreshMyBookmarksAfterChange(restrict, cleanTags)
            val changedTag = toggledTag?.let { "#$it" }
            val message = when {
                toggledTag != null && cleanTags.any { it.equals(toggledTag, ignoreCase = true) } -> "已添加收藏标签 $changedTag"
                toggledTag != null -> "已移除收藏标签 $changedTag"
                cleanTags.isEmpty() -> "已清空收藏标签"
                else -> "已更新收藏标签"
            }
            showMessage(message)
        }
    }
}
internal fun PixivViewModel.deleteBookmark(illustId: Long, isNovel: Boolean = false) {
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (illustId <= 0L) {
        showMessage("作品 ID 无效")
        return
    }

    viewModelScope.launch {
        runBusy {
            withAccessToken {
                if (isNovel) {
                    repository.deleteNovelBookmark(illustId, it)
                } else {
                    repository.deleteBookmark(illustId, it)
                }
            }
            markIllustBookmarked(illustId, isBookmarked = false)
            _previewState.update { it.copy(selectedBookmark = SelectedBookmarkState()) }
            if (!isNovel) {
                refreshMyBookmarksAfterChange()
            }
            showMessage("已取消收藏")
        }
    }
}
internal fun PixivViewModel.bookmarkSelected(
    restrict: BookmarkRestrict = BookmarkRestrict.Public,
    tags: List<String> = emptyList(),
) {
    val illust = _previewState.value.selectedIllust
    if (illust == null) {
        showMessage("没有选中的作品")
        return
    }
    val isNovel = illust.type.equals("novel", ignoreCase = true)
    bookmarkIllust(illust.id, restrict, tags, isNovel = isNovel)
}
internal fun PixivViewModel.deleteSelectedBookmark() {
    val illust = _previewState.value.selectedIllust
    if (illust == null) {
        showMessage("没有选中的作品")
        return
    }
    val isNovel = illust.type.equals("novel", ignoreCase = true)
    deleteBookmark(illust.id, isNovel = isNovel)
}
internal fun PixivViewModel.toggleBookmark(illust: Illust) {
    val isNovel = illust.type.equals("novel", ignoreCase = true)
    if (illust.isBookmarked) {
        deleteBookmark(illust.id, isNovel = isNovel)
    } else {
        bookmarkIllust(illust.id, isNovel = isNovel)
    }
}
internal fun PixivViewModel.toggleSelectedBookmark() {
    val illust = _previewState.value.selectedIllust
    if (illust == null) {
        showMessage("没有选中的作品")
        return
    }
    toggleBookmark(illust)
}
internal fun PixivViewModel.refreshMyBookmarksAfterChange(
    restrict: BookmarkRestrict? = null,
    tags: List<String> = emptyList(),
) {
    val mine = _mineState.value.mine
    val session = _authState.value.session
    if (!mine.hasLoaded || session?.userId == null) return
    if (restrict == null || restrict == BookmarkRestrict.Public) {
        loadMyBookmarks(BookmarkFeed.Public, mine.bookmarks.queryTag, refresh = true)
    }
    if (restrict == null || restrict == BookmarkRestrict.Private) {
        loadMyBookmarks(BookmarkFeed.Private, mine.privateBookmarks.queryTag, refresh = true)
    }
    tags.forEach { tag ->
        if (restrict == BookmarkRestrict.Public && mine.bookmarks.queryTag.equals(tag, ignoreCase = true)) {
            loadMyBookmarks(BookmarkFeed.Public, tag, refresh = true)
        }
        if (restrict == BookmarkRestrict.Private && mine.privateBookmarks.queryTag.equals(tag, ignoreCase = true)) {
            loadMyBookmarks(BookmarkFeed.Private, tag, refresh = true)
        }
    }
}
internal fun PixivViewModel.refreshMyBookmarkTagsAfterChange() {
    val mine = _mineState.value.mine
    val session = _authState.value.session
    if (!mine.hasLoaded || session?.userId == null) return
    if (mine.hasBookmarkTagsLoaded) {
        loadMyBookmarkTags(refresh = true)
    }
}
internal fun PixivViewModel.loadSelectedBookmarkDetail(illustId: Long, force: Boolean = false) {
    val session = _authState.value.session
    if (session?.accessToken.isNullOrBlank()) return
    val preview = _previewState.value
    if (preview.selectedIllust?.id != illustId) return
    if (!force && (preview.selectedBookmark.isLoading || preview.selectedBookmark.isLoaded)) return

    viewModelScope.launch {
        _previewState.update {
            if (it.selectedIllust?.id == illustId) {
                it.copy(selectedBookmark = it.selectedBookmark.copy(isLoading = true, error = null))
            } else {
                it
            }
        }
        runCatching { withAccessToken { token -> repository.bookmarkDetail(illustId, token) } }
            .onSuccess { detail ->
                _previewState.update { current ->
                    if (current.selectedIllust?.id == illustId) {
                        current.copy(
                            selectedBookmark = SelectedBookmarkState(
                                isLoading = false,
                                isLoaded = true,
                                restrict = detail.restrict,
                                tags = detail.tags,
                                error = null,
                            ),
                        )
                    } else {
                        current
                    }
                }
            }
            .onFailure { error ->
                _previewState.update { current ->
                    if (current.selectedIllust?.id == illustId) {
                        current.copy(
                            selectedBookmark = current.selectedBookmark.copy(
                                isLoading = false,
                                isLoaded = true,
                                error = error.readableMessage(),
                            ),
                        )
                    } else {
                        current
                    }
                }
            }
    }
}
internal fun PixivViewModel.markIllustBookmarked(illustId: Long, isBookmarked: Boolean) {
    previewBackStack.replaceAll { it.withBookmarkState(illustId, isBookmarked) }
    _homeState.update { state ->
        state.copy(home = state.home.withBookmarkState(illustId, isBookmarked))
    }
    _searchState.update { state ->
        state.copy(
            discover = state.discover.withBookmarkState(illustId, isBookmarked),
            items = state.items.withBookmarkState(illustId, isBookmarked),
        )
    }
    _mineState.update { state ->
        state.copy(mine = state.mine.withBookmarkState(illustId, isBookmarked))
    }
    _authorState.update { state ->
        state.copy(author = state.author.withBookmarkState(illustId, isBookmarked))
    }
    _previewState.update { state ->
        state.copy(
            selectedIllust = state.selectedIllust?.let {
                if (it.id == illustId) it.withBookmarkState(isBookmarked) else it
            },
            related = state.related.withBookmarkState(illustId, isBookmarked),
        )
    }
}
