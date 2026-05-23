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

internal fun PixivViewModel.bookmarkIllust(
    illustId: Long,
    restrict: BookmarkRestrict = BookmarkRestrict.Public,
    tags: List<String> = emptyList(),
    isNovel: Boolean = false,
) {
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (illustId <= 0L) {
        _uiState.update { it.copy(message = "作品 ID 无效") }
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
            _uiState.update { state ->
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
            _uiState.update { it.copy(message = "已添加$typeLabel$tagsText") }
        }
    }
}
internal fun PixivViewModel.addTagsToSelectedBookmark(tags: List<String>) {
    val illust = _uiState.value.selectedIllust
    if (illust == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
        return
    }
    if (!illust.isBookmarked) {
        _uiState.update { it.copy(message = "请先收藏作品") }
        return
    }
    val current = _uiState.value.selectedBookmark
    if (!current.isLoaded) {
        _uiState.update { it.copy(message = "收藏标签同步中，请稍后再试") }
        loadSelectedBookmarkDetail(illust.id)
        return
    }
    val mergedTags = (current.tags + tags).distinctBy { it.lowercase() }
    updateSelectedBookmarkTags(mergedTags)
}
internal fun PixivViewModel.toggleSelectedBookmarkTag(tag: String) {
    val illust = _uiState.value.selectedIllust
    if (illust == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
        return
    }
    if (!illust.isBookmarked) {
        _uiState.update { it.copy(message = "请先收藏作品") }
        return
    }
    val cleanTag = normalizeBookmarkTag(tag)
    if (cleanTag == null) {
        _uiState.update { it.copy(message = "请输入收藏标签") }
        return
    }
    val bookmark = _uiState.value.selectedBookmark
    if (!bookmark.isLoaded) {
        _uiState.update { it.copy(message = "收藏标签同步中，请稍后再试") }
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
    val illust = _uiState.value.selectedIllust
    if (illust == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
        return
    }
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    val cleanTags = normalizeBookmarkTags(tags)
    val currentBookmark = _uiState.value.selectedBookmark
    if (!currentBookmark.isLoaded) {
        _uiState.update { it.copy(message = "收藏标签同步中，请稍后再试") }
        loadSelectedBookmarkDetail(illust.id)
        return
    }
    val restrict = currentBookmark.restrict
    viewModelScope.launch {
        runBusy {
            withAccessToken { repository.addBookmark(illust.id, it, restrict, cleanTags) }
            _uiState.update { state ->
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
            _uiState.update { it.copy(message = message) }
        }
    }
}
internal fun PixivViewModel.deleteBookmark(illustId: Long, isNovel: Boolean = false) {
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (illustId <= 0L) {
        _uiState.update { it.copy(message = "作品 ID 无效") }
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
            _uiState.update { it.copy(selectedBookmark = SelectedBookmarkState()) }
            if (!isNovel) {
                refreshMyBookmarksAfterChange()
            }
            _uiState.update { it.copy(message = "已取消收藏") }
        }
    }
}
internal fun PixivViewModel.bookmarkSelected(
    restrict: BookmarkRestrict = BookmarkRestrict.Public,
    tags: List<String> = emptyList(),
) {
    val illust = _uiState.value.selectedIllust
    if (illust == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
        return
    }
    val isNovel = illust.type.equals("novel", ignoreCase = true)
    bookmarkIllust(illust.id, restrict, tags, isNovel = isNovel)
}
internal fun PixivViewModel.deleteSelectedBookmark() {
    val illust = _uiState.value.selectedIllust
    if (illust == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
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
    val illust = _uiState.value.selectedIllust
    if (illust == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
        return
    }
    toggleBookmark(illust)
}
internal fun PixivViewModel.refreshMyBookmarksAfterChange(
    restrict: BookmarkRestrict? = null,
    tags: List<String> = emptyList(),
) {
    val state = _uiState.value
    if (!state.mine.hasLoaded || state.session?.userId == null) return
    if (restrict == null || restrict == BookmarkRestrict.Public) {
        loadMyBookmarks(BookmarkFeed.Public, state.mine.bookmarks.queryTag, refresh = true)
    }
    if (restrict == null || restrict == BookmarkRestrict.Private) {
        loadMyBookmarks(BookmarkFeed.Private, state.mine.privateBookmarks.queryTag, refresh = true)
    }
    tags.forEach { tag ->
        if (restrict == BookmarkRestrict.Public && state.mine.bookmarks.queryTag.equals(tag, ignoreCase = true)) {
            loadMyBookmarks(BookmarkFeed.Public, tag, refresh = true)
        }
        if (restrict == BookmarkRestrict.Private && state.mine.privateBookmarks.queryTag.equals(tag, ignoreCase = true)) {
            loadMyBookmarks(BookmarkFeed.Private, tag, refresh = true)
        }
    }
}
internal fun PixivViewModel.refreshMyBookmarkTagsAfterChange() {
    val state = _uiState.value
    if (!state.mine.hasLoaded || state.session?.userId == null) return
    if (state.mine.hasBookmarkTagsLoaded) {
        loadMyBookmarkTags(refresh = true)
    }
}
internal fun PixivViewModel.loadSelectedBookmarkDetail(illustId: Long, force: Boolean = false) {
    val state = _uiState.value
    if (state.session?.accessToken.isNullOrBlank()) return
    if (state.selectedIllust?.id != illustId) return
    if (!force && (state.selectedBookmark.isLoading || state.selectedBookmark.isLoaded)) return

    viewModelScope.launch {
        _uiState.update {
            if (it.selectedIllust?.id == illustId) {
                it.copy(selectedBookmark = it.selectedBookmark.copy(isLoading = true, error = null))
            } else {
                it
            }
        }
        runCatching { withAccessToken { token -> repository.bookmarkDetail(illustId, token) } }
            .onSuccess { detail ->
                _uiState.update { current ->
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
                _uiState.update { current ->
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
    _uiState.update { state ->
        state.copy(
            home = state.home.withBookmarkState(illustId, isBookmarked),
            discover = state.discover.withBookmarkState(illustId, isBookmarked),
            mine = state.mine.withBookmarkState(illustId, isBookmarked),
            author = state.author.withBookmarkState(illustId, isBookmarked),
            items = state.items.withBookmarkState(illustId, isBookmarked),
            selectedIllust = state.selectedIllust?.let {
                if (it.id == illustId) it.withBookmarkState(isBookmarked) else it
            },
            related = state.related.withBookmarkState(illustId, isBookmarked),
        )
    }
}
