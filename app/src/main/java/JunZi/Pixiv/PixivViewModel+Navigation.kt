package JunZi.Pixiv

import kotlinx.coroutines.flow.update

internal fun PixivViewModel.openHome() {
    navigateTo(AppScreen.Home, addToBackStack = false)
    loadHome(refresh = false)
}
internal fun PixivViewModel.openSearch() {
    navigateTo(AppScreen.Search, addToBackStack = false)
    loadTrendingTags(refresh = false)
    loadDiscover(refresh = false)
}
internal fun PixivViewModel.openMe() {
    navigateTo(AppScreen.Me, addToBackStack = false)
    loadMine(refresh = false)
}
internal fun PixivViewModel.openSettings() {
    navigateTo(AppScreen.Settings)
}
internal fun PixivViewModel.openWebPixiv() {
    navigateTo(AppScreen.WebPixiv)
}
internal fun PixivViewModel.openLoginScreen() {
    navigateTo(AppScreen.Login)
}
internal fun PixivViewModel.showMainPage(screen: AppScreen) {
    if (screen !in setOf(AppScreen.Home, AppScreen.Search, AppScreen.Me)) return
    navigateTo(screen, addToBackStack = false)
}
internal fun PixivViewModel.ensureMainPageLoaded(screen: AppScreen) {
    if (_authState.value.session == null && screen !in setOf(AppScreen.Home, AppScreen.Me)) return
    when (screen) {
        AppScreen.Home -> loadHome(refresh = false)
        AppScreen.Search -> {
            loadTrendingTags(refresh = false)
            loadDiscover(refresh = false)
        }
        AppScreen.Me -> loadMine(refresh = false)
        else -> Unit
    }
}
internal fun PixivViewModel.backToLogin() = goBack()
internal fun PixivViewModel.goBack() {
    val currentState = _shellState.value
    val current = currentState.screen
    if (current == AppScreen.Preview && previewBackStack.isNotEmpty()) {
        val previous = previewBackStack.removeLast()
        _previewState.update {
            it.copy(
                selectedIllust = previous.illust,
                selectedBookmark = previous.selectedBookmark,
                selectedImageIndex = previous.selectedImageIndex,
                related = previous.related,
                comments = previous.comments,
                ugoiraFrames = previous.ugoiraFrames,
                ugoiraLoadedFrames = previous.ugoiraLoadedFrames,
                ugoiraTotalFrames = previous.ugoiraTotalFrames,
                previewReturnScreen = previous.returnScreen,
                isFullScreenPreview = false,
                isPreviewLoading = false,
            )
        }
        showMessage(null)
        return
    }
    val targetEntry = backStack.removeLastOrNull()
    val target = targetEntry?.screen ?: current.defaultBackTarget()
    if (current == AppScreen.Preview) {
        previewBackStack.clear()
    }
    val previewSnapshot = targetEntry?.preview?.takeIf { target == AppScreen.Preview }
    val authorSnapshot = targetEntry?.author?.takeIf {
        target == AppScreen.Author && _authorState.value.author.userId != it.userId
    }
    val seriesSnapshot = targetEntry?.series?.takeIf { target == AppScreen.Series }
    _previewState.update { state ->
        state.copy(
            selectedIllust = when {
                previewSnapshot != null -> previewSnapshot.illust
                current == AppScreen.Preview -> null
                else -> state.selectedIllust
            },
            selectedBookmark = when {
                previewSnapshot != null -> previewSnapshot.selectedBookmark
                current == AppScreen.Preview -> SelectedBookmarkState()
                else -> state.selectedBookmark
            },
            selectedImageIndex = previewSnapshot?.selectedImageIndex
                ?: if (current == AppScreen.Preview) 0 else state.selectedImageIndex,
            related = previewSnapshot?.related ?: if (current == AppScreen.Preview) FeedState() else state.related,
            comments = previewSnapshot?.comments ?: if (current == AppScreen.Preview) CommentState() else state.comments,
            ugoiraFrames = previewSnapshot?.ugoiraFrames ?: if (current == AppScreen.Preview) emptyList() else state.ugoiraFrames,
            ugoiraLoadedFrames = previewSnapshot?.ugoiraLoadedFrames ?: if (current == AppScreen.Preview) 0 else state.ugoiraLoadedFrames,
            ugoiraTotalFrames = previewSnapshot?.ugoiraTotalFrames ?: if (current == AppScreen.Preview) 0 else state.ugoiraTotalFrames,
            previewReturnScreen = previewSnapshot?.returnScreen ?: state.previewReturnScreen,
            isFullScreenPreview = if (previewSnapshot != null || current == AppScreen.Preview) false else state.isFullScreenPreview,
            isPreviewLoading = if (previewSnapshot != null || current == AppScreen.Preview) false else state.isPreviewLoading,
        )
    }
    if (current == AppScreen.NovelReader && target != AppScreen.NovelReader) {
        _novelState.update { it.copy(novelReader = NovelReaderState()) }
    }
    if (authorSnapshot != null) {
        _authorState.update { it.copy(author = authorSnapshot) }
    } else if (current == AppScreen.Author && target != AppScreen.Preview) {
        _authorState.update { it.copy(author = AuthorState()) }
    }
    if (seriesSnapshot != null) {
        _novelState.update { it.copy(series = seriesSnapshot) }
    } else if (current == AppScreen.Series && target != AppScreen.Series) {
        _novelState.update { it.copy(series = SeriesState()) }
    }
    _shellState.update { it.copy(screen = target) }
    showMessage(null)
    when (target) {
        AppScreen.Home -> loadHome(refresh = false)
        AppScreen.Search -> {
            loadTrendingTags(refresh = false)
            loadDiscover(refresh = false)
        }
        AppScreen.Me -> loadMine(refresh = false)
        AppScreen.Author -> {
            if (_authorState.value.author.userId != null) {
                loadAuthorProfile()
                loadAuthorWorks(
                    refresh = _authorState.value.author.feed(_authorState.value.author.selectedTab).items.isEmpty(),
                    tab = _authorState.value.author.selectedTab,
                )
            }
        }
        else -> Unit
    }
}
internal fun PixivViewModel.navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
    val current = _shellState.value.screen
    if (current == screen) return
    if (addToBackStack) pushBackStack(current)
    _shellState.update { it.copy(screen = screen) }
}
internal fun PixivViewModel.pushBackStack(screen: AppScreen) {
    val entry = toNavigationEntry(screen) ?: return
    if (backStack.lastOrNull()?.screen == screen) {
        backStack.removeLast()
    }
    backStack.addLast(entry)
}
internal fun PixivViewModel.resetNavigation(screen: AppScreen) {
    backStack.clear()
    previewBackStack.clear()
    _shellState.update { it.copy(screen = screen) }
}
