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

internal fun PixivViewModel.openAuthor(illust: Illust) {
    val authorId = illust.authorId.takeIf { it > 0L } ?: run {
        _uiState.update { it.copy(message = "未找到作者信息") }
        return
    }
    openAuthor(
        userId = authorId,
        userName = illust.authorName,
        userAccount = illust.authorAccount,
        userAvatarUrl = illust.authorAvatarUrl,
    )
}
internal fun PixivViewModel.openAuthor(user: UserPreview) {
    openAuthor(
        userId = user.userId,
        userName = user.userName,
        userAccount = user.userAccount,
        userAvatarUrl = user.avatarUrl,
    )
}
internal fun PixivViewModel.openAuthor(
    userId: Long,
    userName: String,
    userAccount: String,
    userAvatarUrl: String?,
) {
    navigateTo(AppScreen.Author)
    _uiState.update {
        it.copy(
            author = AuthorState(
                userId = userId,
                userName = userName,
                userAccount = userAccount,
                userAvatarUrl = userAvatarUrl,
                selectedTab = AuthorWorkTab.Illust,
            ),
            message = null,
        )
    }
    loadAuthorProfile()
    loadAuthorWorks(refresh = true, tab = AuthorWorkTab.Illust)
}
internal fun PixivViewModel.selectAuthorTab(tab: AuthorWorkTab) {
    val current = _uiState.value.author
    if (current.selectedTab == tab) return
    _uiState.update { it.copy(author = it.author.copy(selectedTab = tab)) }
    val feed = _uiState.value.author.feed(tab)
    if (feed.items.isEmpty()) {
        loadAuthorWorks(refresh = true, tab = tab)
    }
}
internal fun PixivViewModel.loadAuthorProfile() {
    val authorId = _uiState.value.author.userId ?: return
    if (_uiState.value.session?.accessToken.isNullOrBlank()) return
    if (_uiState.value.author.isLoadingProfile) return
    viewModelScope.launch {
        _uiState.update { it.copy(author = it.author.copy(isLoadingProfile = true)) }
        runCatching { withAccessToken { token -> repository.userDetail(authorId, token) } }
            .onSuccess { profile ->
                if (_uiState.value.author.userId != authorId) return@onSuccess
                _uiState.update {
                    it.copy(
                        author = it.author.mergeProfile(profile).copy(isLoadingProfile = false),
                    )
                }
            }
            .onFailure { error ->
                if (_uiState.value.author.userId != authorId) return@onFailure
                _uiState.update {
                    it.copy(
                        author = it.author.copy(isLoadingProfile = false),
                        message = error.readableMessage(),
                    )
                }
            }
    }
}
internal fun PixivViewModel.loadAuthorWorks(refresh: Boolean = false, tab: AuthorWorkTab = _uiState.value.author.selectedTab) {
    val state = _uiState.value
    val authorId = state.author.userId ?: return
    if (state.session?.accessToken.isNullOrBlank()) return
    if (state.author.isLoadingWorks) return
    if (!refresh && state.author.feed(tab).nextUrl == null) return

    viewModelScope.launch {
        _uiState.update { current ->
            current.copy(
                author = current.author.copy(
                    isLoadingWorks = true,
                ).withFeed(tab) { it.copy(error = null) },
            )
        }
        runCatching {
            withAccessToken { token ->
                if (refresh) {
                    when (tab) {
                        AuthorWorkTab.Bookmarks -> repository.bookmarkedIllusts(authorId, token)
                        AuthorWorkTab.Novel -> repository.userNovels(authorId, token)
                        else -> repository.userWorks(authorId, token, tab.apiValue)
                    }
                } else {
                    val nextUrl = _uiState.value.author.feed(tab).nextUrl ?: return@withAccessToken null
                    if (tab == AuthorWorkTab.Novel) {
                        repository.nextNovelPage(nextUrl, token)
                    } else {
                        repository.nextPage(nextUrl, token)
                    }
                }
            }
        }.onSuccess { page ->
            val currentAuthorId = _uiState.value.author.userId
            if (currentAuthorId != authorId) return@onSuccess
            if (page == null) {
                _uiState.update { it.copy(author = it.author.copy(isLoadingWorks = false)) }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _uiState.update { current ->
                current.copy(
                    author = current.author.copy(
                        isLoadingWorks = false,
                    ).withFeed(tab) { feed ->
                        feed.copy(
                            items = if (refresh) filteredPage.items else feed.items + filteredPage.items,
                            nextUrl = filteredPage.nextUrl,
                            isLoading = false,
                            error = null,
                        )
                    },
                )
            }
        }.onFailure { error ->
            if (_uiState.value.author.userId != authorId) return@onFailure
            _uiState.update {
                it.copy(
                    author = it.author.copy(isLoadingWorks = false).withFeed(tab) { feed ->
                        feed.copy(error = error.readableMessage(), isLoading = false)
                    },
                    message = error.readableMessage(),
                )
            }
        }
    }
}
internal fun PixivViewModel.followAuthor(restrict: BookmarkRestrict = BookmarkRestrict.Public) {
    val authorId = _uiState.value.author.userId ?: return
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (_uiState.value.author.isFollowBusy) return
    viewModelScope.launch {
        _uiState.update { it.copy(author = it.author.copy(isFollowBusy = true)) }
        runCatching { withAccessToken { token -> repository.followUser(authorId, token, restrict) } }
            .onSuccess {
                _uiState.update {
                    it.copy(
                        author = it.author.copy(
                            isFollowBusy = false,
                            isFollowed = true,
                        ),
                        message = if (restrict == BookmarkRestrict.Private) "已悄悄关注作者" else "已关注作者",
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        author = it.author.copy(isFollowBusy = false),
                        message = error.readableMessage(),
                    )
                }
            }
    }
}
internal fun PixivViewModel.unfollowAuthor() {
    val authorId = _uiState.value.author.userId ?: return
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (_uiState.value.author.isFollowBusy) return
    viewModelScope.launch {
        _uiState.update { it.copy(author = it.author.copy(isFollowBusy = true)) }
        runCatching { withAccessToken { token -> repository.unfollowUser(authorId, token) } }
            .onSuccess {
                _uiState.update {
                    it.copy(
                        author = it.author.copy(
                            isFollowBusy = false,
                            isFollowed = false,
                        ),
                        message = "已取消关注作者",
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        author = it.author.copy(isFollowBusy = false),
                        message = error.readableMessage(),
                    )
                }
            }
    }
}
