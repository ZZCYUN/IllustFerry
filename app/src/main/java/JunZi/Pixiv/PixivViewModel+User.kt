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

internal fun PixivViewModel.loadMine(refresh: Boolean = false) {
    val state = _uiState.value
    if (state.session == null) {
        loadHistory(refresh = refresh)
        return
    }
    if (state.session.userId == null) {
        loadHistory(refresh = refresh)
        _uiState.update { it.copy(message = "当前会话缺少用户 ID，请用网页登录或授权 code 登录") }
        return
    }
    if (!refresh && state.mine.hasLoaded) return
    _uiState.update { it.copy(mine = it.mine.copy(hasLoaded = true)) }
    loadMyProfile()
    loadHistory(refresh = refresh)
}
internal fun PixivViewModel.loadMyProfile() {
    val state = _uiState.value
    val userId = state.session?.userId ?: return
    viewModelScope.launch {
        runCatching { withAccessToken { token -> repository.userDetail(userId, token) } }
            .onSuccess { profile ->
                val followerPage = runCatching {
                    withAccessToken { token -> repository.userFollowers(userId, token) }
                }.getOrNull()
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.copy(
                            followCount = profile.followingCount,
                            followerCount = profile.followerCount.takeIf { it > 0 }
                                ?: followerPage?.items?.size
                                ?: current.mine.followerCount,
                            hasMoreFollowers = if (profile.followerCount > 0) {
                                false
                            } else {
                                followerPage?.nextUrl != null
                            },
                        ),
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(message = error.readableMessage()) }
            }
    }
}
internal fun PixivViewModel.loadHistory(refresh: Boolean = false) {
    val state = _uiState.value
    if (state.history.isLoading) return
    if (!refresh && state.history.items.isNotEmpty()) return
    val accessToken = state.session?.accessToken
    if (accessToken.isNullOrBlank()) {
        _uiState.update {
            it.copy(
                history = it.history.copy(
                    items = emptyList(),
                    isLoading = false,
                    error = "登录后可查看历史作品",
                ),
            )
        }
        return
    }
    viewModelScope.launch(Dispatchers.IO) {
        loadHistoryPage(accessToken = accessToken, offset = 0, append = false)
    }
}
internal fun PixivViewModel.loadMoreHistory() {
    val state = _uiState.value
    if (state.history.isLoading || !state.history.hasMore) return
    val accessToken = state.session?.accessToken ?: return
    viewModelScope.launch(Dispatchers.IO) {
        loadHistoryPage(
            accessToken = accessToken,
            offset = state.history.nextOffset,
            append = true,
        )
    }
}
internal fun PixivViewModel.clearHistory() {
    viewModelScope.launch(Dispatchers.IO) {
        historyStore.clear()
        _uiState.update { it.copy(history = HistoryState()) }
    }
}
internal fun PixivViewModel.deleteHistoryItem(illust: Illust) {
    val storageKey = historyStorageKey(illust)
    val displayId = illust.id
    viewModelScope.launch(Dispatchers.IO) {
        historyStore.delete(storageKey)
        _uiState.update { state ->
            state.copy(
                history = state.history.copy(
                    items = state.history.items.filterNot {
                        it.illust.id == displayId && it.illust.type.equals(illust.type, ignoreCase = true)
                    },
                    nextOffset = (state.history.nextOffset - 1).coerceAtLeast(0),
                ),
            )
        }
    }
}
internal fun PixivViewModel.loadMyWorks(refresh: Boolean = false) {
    val state = _uiState.value
    val userId = state.session?.userId ?: return
    if (state.mine.works.isLoading) return

    viewModelScope.launch {
        _uiState.update { current ->
            current.copy(mine = current.mine.copy(works = current.mine.works.copy(isLoading = true, error = null)))
        }
        runCatching {
            withAccessToken { token ->
                if (refresh) {
                    repository.userWorks(userId, token)
                } else {
                    val nextUrl = _uiState.value.mine.works.nextUrl ?: return@withAccessToken null
                    repository.nextPage(nextUrl, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _uiState.update { it.copy(mine = it.mine.copy(works = it.mine.works.copy(isLoading = false))) }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.copy(
                        works = current.mine.works.copy(
                            items = if (refresh) filteredPage.items else current.mine.works.items + filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isLoading = false,
                            error = null,
                        ),
                    ),
                )
            }
        }.onFailure { error ->
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.copy(
                        works = current.mine.works.copy(isLoading = false, error = error.readableMessage()),
                    ),
                )
            }
        }
    }
}
internal fun PixivViewModel.loadMyBookmarks(
    feed: BookmarkFeed = BookmarkFeed.Public,
    tag: String? = null,
    refresh: Boolean = false,
) {
    val state = _uiState.value
    val userId = state.session?.userId ?: return
    val cleanTag = normalizeBookmarkTag(tag.orEmpty())
    val currentFeed = state.mine.bookmarks(feed)
    val shouldRefresh = refresh || currentFeed.queryTag != cleanTag
    if (currentFeed.isLoading) return

    viewModelScope.launch {
        _uiState.update { current ->
            current.copy(
                mine = current.mine.withBookmarks(feed) {
                    it.copy(isLoading = true, error = null, queryTag = cleanTag)
                },
            )
        }
        runCatching {
            withAccessToken { token ->
                if (shouldRefresh) {
                    val restrict = when (feed) {
                        BookmarkFeed.Public -> BookmarkRestrict.Public
                        BookmarkFeed.Private -> BookmarkRestrict.Private
                    }
                    repository.bookmarkedIllusts(userId, token, restrict, cleanTag)
                } else {
                    val nextUrl = _uiState.value.mine.bookmarks(feed).nextUrl ?: return@withAccessToken null
                    repository.nextPage(nextUrl, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.withBookmarks(feed) {
                            it.copy(isLoading = false)
                        },
                    )
                }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.withBookmarks(feed) { old ->
                        old.copy(
                            items = if (shouldRefresh) filteredPage.items else old.items + filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isLoading = false,
                            error = null,
                            queryTag = cleanTag,
                        )
                    },
                )
            }
        }.onFailure { error ->
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.withBookmarks(feed) {
                        it.copy(isLoading = false, error = error.readableMessage())
                    },
                )
            }
        }
    }
}
internal fun PixivViewModel.loadMyBookmarkNovels(
    feed: BookmarkFeed = BookmarkFeed.Public,
    tag: String? = null,
    refresh: Boolean = false,
) {
    val state = _uiState.value
    val userId = state.session?.userId ?: return
    val cleanTag = normalizeBookmarkTag(tag.orEmpty())
    val currentFeed = state.mine.bookmarkNovels(feed)
    val shouldRefresh = refresh || currentFeed.queryTag != cleanTag
    if (currentFeed.isLoading) return

    viewModelScope.launch {
        _uiState.update { current ->
            current.copy(
                mine = current.mine.withBookmarkNovels(feed) {
                    it.copy(isLoading = true, error = null, queryTag = cleanTag)
                },
            )
        }
        runCatching {
            withAccessToken { token ->
                if (shouldRefresh) {
                    val restrict = when (feed) {
                        BookmarkFeed.Public -> BookmarkRestrict.Public
                        BookmarkFeed.Private -> BookmarkRestrict.Private
                    }
                    repository.bookmarkedNovels(userId, token, restrict, cleanTag)
                } else {
                    val nextUrl = _uiState.value.mine.bookmarkNovels(feed).nextUrl ?: return@withAccessToken null
                    repository.nextNovelPage(nextUrl, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.withBookmarkNovels(feed) {
                            it.copy(isLoading = false)
                        },
                    )
                }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.withBookmarkNovels(feed) { old ->
                        old.copy(
                            items = if (shouldRefresh) filteredPage.items else old.items + filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isLoading = false,
                            error = null,
                            queryTag = cleanTag,
                        )
                    },
                )
            }
        }.onFailure { error ->
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.withBookmarkNovels(feed) {
                        it.copy(isLoading = false, error = error.readableMessage())
                    },
                )
            }
        }
    }
}
internal fun PixivViewModel.loadMyBookmarkTags(refresh: Boolean = false) {
    val state = _uiState.value
    val userId = state.session?.userId ?: return
    if (state.mine.isBookmarkTagsLoading) return
    if (!refresh && state.mine.hasBookmarkTagsLoaded) {
        return
    }

    viewModelScope.launch {
        _uiState.update { current ->
            current.copy(mine = current.mine.copy(isBookmarkTagsLoading = true, bookmarkTagsError = null))
        }
        runCatching {
            withAccessToken { token ->
                val publicTags = repository.bookmarkTags(userId, token, BookmarkRestrict.Public)
                val privateTags = repository.bookmarkTags(userId, token, BookmarkRestrict.Private)
                publicTags to privateTags
            }
        }.onSuccess { (publicTags, privateTags) ->
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.copy(
                        publicBookmarkTags = publicTags,
                        privateBookmarkTags = privateTags,
                        hasBookmarkTagsLoaded = true,
                        isBookmarkTagsLoading = false,
                        bookmarkTagsError = null,
                    ),
                )
            }
        }.onFailure { error ->
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.copy(
                        isBookmarkTagsLoading = false,
                        bookmarkTagsError = error.readableMessage(),
                    ),
                )
            }
        }
    }
}
internal fun PixivViewModel.loadMyFollowing(feed: FollowUserFeed, refresh: Boolean = false) {
    val state = _uiState.value
    val userId = state.session?.userId ?: return
    val currentFeed = state.mine.following(feed)
    if (currentFeed.isLoading) return
    if (!refresh && currentFeed.items.isNotEmpty()) return

    viewModelScope.launch {
        _uiState.update { current ->
            current.copy(
                mine = current.mine.withFollowing(feed) {
                    it.copy(isLoading = true, error = null)
                },
            )
        }
        runCatching {
            withAccessToken { token ->
                if (refresh) {
                    val restrict = when (feed) {
                        FollowUserFeed.Public -> BookmarkRestrict.Public
                        FollowUserFeed.Private -> BookmarkRestrict.Private
                    }
                    repository.userFollowing(userId, token, restrict)
                } else {
                    val nextUrl = _uiState.value.mine.following(feed).nextUrl ?: return@withAccessToken null
                    repository.nextUserPreviewsPage(nextUrl, token)
                }
            }
        }.onSuccess { page ->
            if (page == null) {
                _uiState.update { current ->
                    current.copy(
                        mine = current.mine.withFollowing(feed) { it.copy(isLoading = false) },
                    )
                }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.withFollowing(feed) { old ->
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
            _uiState.update { current ->
                current.copy(
                    mine = current.mine.withFollowing(feed) {
                        it.copy(isLoading = false, error = error.readableMessage())
                    },
                )
            }
        }
    }
}
internal fun PixivViewModel.saveHistory(illust: Illust) {
    viewModelScope.launch(Dispatchers.IO) {
        historyStore.save(historyStorageKey(illust))
        val accessToken = _uiState.value.session?.accessToken
        if (!accessToken.isNullOrBlank()) {
            val entries = runCatching { historyStore.recentPage(limit = HISTORY_PAGE_SIZE, offset = 0) }.getOrDefault(emptyList())
            val items = runCatching { loadHistoryItems(entries, accessToken) }.getOrNull()
            if (items != null) {
                _uiState.update {
                    it.copy(
                        history = it.history.copy(
                            items = items,
                            isLoading = false,
                            error = null,
                            nextOffset = entries.size,
                            hasMore = entries.size >= HISTORY_PAGE_SIZE,
                        ),
                    )
                }
            }
        }
    }
}
internal fun PixivViewModel.historyStorageKey(illust: Illust): Long =
    if (illust.type.equals("novel", ignoreCase = true)) -illust.id else illust.id
