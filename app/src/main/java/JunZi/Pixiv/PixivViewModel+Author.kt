package JunZi.Pixiv

import androidx.lifecycle.viewModelScope
import JunZi.Pixiv.data.model.BookmarkRestrict
import JunZi.Pixiv.data.model.Illust
import JunZi.Pixiv.data.model.UserPreview
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PixivViewModel.openAuthor(illust: Illust) {
    val authorId = illust.authorId.takeIf { it > 0L } ?: run {
        showMessage("未找到作者信息")
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
    _authorState.update {
        it.copy(
            author = AuthorState(
                userId = userId,
                userName = userName,
                userAccount = userAccount,
                userAvatarUrl = userAvatarUrl,
                selectedTab = AuthorWorkTab.Illust,
            ),
        )
    }
    showMessage(null)
    loadAuthorProfile()
    loadAuthorWorks(refresh = true, tab = AuthorWorkTab.Illust)
}
internal fun PixivViewModel.selectAuthorTab(tab: AuthorWorkTab) {
    val current = _authorState.value.author
    if (current.selectedTab == tab) return
    _authorState.update { it.copy(author = it.author.copy(selectedTab = tab)) }
    val feed = _authorState.value.author.feed(tab)
    if (feed.items.isEmpty()) {
        loadAuthorWorks(refresh = true, tab = tab)
    }
}
internal fun PixivViewModel.loadAuthorProfile() {
    val authorId = _authorState.value.author.userId ?: return
    if (_authState.value.session?.accessToken.isNullOrBlank()) return
    if (_authorState.value.author.isLoadingProfile) return
    viewModelScope.launch {
        _authorState.update { it.copy(author = it.author.copy(isLoadingProfile = true)) }
        runCatching { withAccessToken { token -> repository.userDetail(authorId, token) } }
            .onSuccess { profile ->
                if (_authorState.value.author.userId != authorId) return@onSuccess
                _authorState.update {
                    it.copy(
                        author = it.author.mergeProfile(profile).copy(isLoadingProfile = false),
                    )
                }
            }
            .onFailure { error ->
                if (_authorState.value.author.userId != authorId) return@onFailure
                _authorState.update {
                    it.copy(
                        author = it.author.copy(isLoadingProfile = false),
                    )
                }
                showMessage(error.readableMessage())
            }
    }
}
internal fun PixivViewModel.loadAuthorWorks(refresh: Boolean = false, tab: AuthorWorkTab = _authorState.value.author.selectedTab) {
    val authorState = _authorState.value.author
    val authorId = authorState.userId ?: return
    if (_authState.value.session?.accessToken.isNullOrBlank()) return
    if (authorState.isLoadingWorks) return
    if (!refresh && authorState.feed(tab).nextUrl == null) return

    viewModelScope.launch {
        _authorState.update { current ->
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
                    val nextUrl = _authorState.value.author.feed(tab).nextUrl ?: return@withAccessToken null
                    if (tab == AuthorWorkTab.Novel) {
                        repository.nextNovelPage(nextUrl, token)
                    } else {
                        repository.nextPage(nextUrl, token)
                    }
                }
            }
        }.onSuccess { page ->
            val currentAuthorId = _authorState.value.author.userId
            if (currentAuthorId != authorId) return@onSuccess
            if (page == null) {
                _authorState.update { it.copy(author = it.author.copy(isLoadingWorks = false)) }
                return@onSuccess
            }
            val filteredPage = page.filteredBy(excludedTags())
            _authorState.update { current ->
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
            if (_authorState.value.author.userId != authorId) return@onFailure
            _authorState.update {
                it.copy(
                    author = it.author.copy(isLoadingWorks = false).withFeed(tab) { feed ->
                        feed.copy(error = error.readableMessage(), isLoading = false)
                    },
                )
            }
            showMessage(error.readableMessage())
        }
    }
}
internal fun PixivViewModel.followAuthor(restrict: BookmarkRestrict = BookmarkRestrict.Public) {
    val authorId = _authorState.value.author.userId ?: return
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (_authorState.value.author.isFollowBusy) return
    viewModelScope.launch {
        _authorState.update { it.copy(author = it.author.copy(isFollowBusy = true)) }
        runCatching { withAccessToken { token -> repository.followUser(authorId, token, restrict) } }
            .onSuccess {
                _authorState.update {
                    it.copy(
                        author = it.author.copy(
                            isFollowBusy = false,
                            isFollowed = true,
                        ),
                    )
                }
                showMessage(if (restrict == BookmarkRestrict.Private) "已悄悄关注作者" else "已关注作者")
            }
            .onFailure { error ->
                _authorState.update {
                    it.copy(
                        author = it.author.copy(isFollowBusy = false),
                    )
                }
                showMessage(error.readableMessage())
            }
    }
}
internal fun PixivViewModel.unfollowAuthor() {
    val authorId = _authorState.value.author.userId ?: return
    if (_authState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (_authorState.value.author.isFollowBusy) return
    viewModelScope.launch {
        _authorState.update { it.copy(author = it.author.copy(isFollowBusy = true)) }
        runCatching { withAccessToken { token -> repository.unfollowUser(authorId, token) } }
            .onSuccess {
                _authorState.update {
                    it.copy(
                        author = it.author.copy(
                            isFollowBusy = false,
                            isFollowed = false,
                        ),
                    )
                }
                showMessage("已取消关注作者")
            }
            .onFailure { error ->
                _authorState.update {
                    it.copy(
                        author = it.author.copy(isFollowBusy = false),
                    )
                }
                showMessage(error.readableMessage())
            }
    }
}
