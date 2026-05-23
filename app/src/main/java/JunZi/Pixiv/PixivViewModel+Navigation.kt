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
    if (_uiState.value.session == null && screen !in setOf(AppScreen.Home, AppScreen.Me)) return
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
    val currentState = _uiState.value
    val current = currentState.screen
    if (current == AppScreen.Preview && previewBackStack.isNotEmpty()) {
        val previous = previewBackStack.removeLast()
        _uiState.update {
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
                message = null,
            )
        }
        return
    }
    val targetEntry = backStack.removeLastOrNull()
    val target = targetEntry?.screen ?: current.defaultBackTarget()
    if (current == AppScreen.Preview) {
        previewBackStack.clear()
    }
    _uiState.update { state ->
        val previewSnapshot = targetEntry?.preview?.takeIf { target == AppScreen.Preview }
        val authorSnapshot = targetEntry?.author?.takeIf {
            target == AppScreen.Author && state.author.userId != it.userId
        }
        val seriesSnapshot = targetEntry?.series?.takeIf { target == AppScreen.Series }
        state.copy(
            screen = target,
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
            author = when {
                authorSnapshot != null -> authorSnapshot
                current == AppScreen.Author && target != AppScreen.Preview -> AuthorState()
                else -> state.author
            },
            related = previewSnapshot?.related ?: if (current == AppScreen.Preview) FeedState() else state.related,
            comments = previewSnapshot?.comments ?: if (current == AppScreen.Preview) CommentState() else state.comments,
            ugoiraFrames = previewSnapshot?.ugoiraFrames ?: if (current == AppScreen.Preview) emptyList() else state.ugoiraFrames,
            ugoiraLoadedFrames = previewSnapshot?.ugoiraLoadedFrames ?: if (current == AppScreen.Preview) 0 else state.ugoiraLoadedFrames,
            ugoiraTotalFrames = previewSnapshot?.ugoiraTotalFrames ?: if (current == AppScreen.Preview) 0 else state.ugoiraTotalFrames,
            previewReturnScreen = previewSnapshot?.returnScreen ?: state.previewReturnScreen,
            isFullScreenPreview = if (previewSnapshot != null || current == AppScreen.Preview) false else state.isFullScreenPreview,
            isPreviewLoading = if (previewSnapshot != null || current == AppScreen.Preview) false else state.isPreviewLoading,
            novelReader = if (current == AppScreen.NovelReader && target != AppScreen.NovelReader) NovelReaderState() else state.novelReader,
            series = when {
                seriesSnapshot != null -> seriesSnapshot
                current == AppScreen.Series && target != AppScreen.Series -> SeriesState()
                else -> state.series
            },
            message = null,
        )
    }
    when (target) {
        AppScreen.Home -> loadHome(refresh = false)
        AppScreen.Search -> {
            loadTrendingTags(refresh = false)
            loadDiscover(refresh = false)
        }
        AppScreen.Me -> loadMine(refresh = false)
        AppScreen.Author -> {
            if (_uiState.value.author.userId != null) {
                loadAuthorProfile()
                loadAuthorWorks(
                    refresh = _uiState.value.author.feed(_uiState.value.author.selectedTab).items.isEmpty(),
                    tab = _uiState.value.author.selectedTab,
                )
            }
        }
        else -> Unit
    }
}
internal fun PixivViewModel.clearMessage() = _uiState.update { it.copy(message = null) }
internal fun PixivViewModel.navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
    val current = _uiState.value.screen
    if (current == screen) return
    if (addToBackStack) pushBackStack(current)
    _uiState.update { it.copy(screen = screen) }
}
internal fun PixivViewModel.pushBackStack(screen: AppScreen) {
    val entry = _uiState.value.toNavigationEntry(screen) ?: return
    if (backStack.lastOrNull()?.screen == screen) {
        backStack.removeLast()
    }
    backStack.addLast(entry)
}
internal fun PixivViewModel.resetNavigation(screen: AppScreen) {
    backStack.clear()
    previewBackStack.clear()
    _uiState.update { it.copy(screen = screen) }
}
