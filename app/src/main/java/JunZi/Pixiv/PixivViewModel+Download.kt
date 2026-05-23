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

internal fun PixivViewModel.downloadSelectedIllust() {
    val illust = _uiState.value.selectedIllust
    if (illust == null) {
        _uiState.update { it.copy(message = "没有选中的作品") }
        return
    }
    enqueueIllustDownload(illust)
}
internal fun PixivViewModel.downloadSelectedNovel() {
    val illust = _uiState.value.selectedIllust
    if (illust == null || !illust.type.equals("novel", ignoreCase = true)) {
        _uiState.update { it.copy(message = "没有选中的小说") }
        return
    }
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    enqueueNovelDownload(illust)
}
internal fun PixivViewModel.enqueueNovelDownload(illust: Illust) {
    val job = DownloadItem(
        key = "novel-${illust.id}-${System.nanoTime()}",
        illustId = illust.id,
        title = illust.title,
        illust = illust.copy(type = "novel"),
        fileName = "${illust.safeFolderName()}.txt",
        status = DownloadStatus.Queued,
        isUgoira = false,
        pageCount = 1,
        relativePath = buildDownloadRelativePath(
            rootDirectory = Environment.DIRECTORY_DOWNLOADS,
            authorName = illust.authorName,
            title = illust.title,
        ),
        detail = "等待下载小说正文",
    )

    _uiState.update { state ->
        val downloads = state.downloads.copy(
            items = listOf(job) + state.downloads.items.filterNot {
                it.illustId == illust.id && it.illust?.type.equals("novel", ignoreCase = true)
            },
        )
        store.saveDownloads(downloads.items)
        state.copy(
            downloads = downloads,
            message = "已加入小说下载队列",
        )
    }
    startNovelDownload(job, illust)
}
internal fun PixivViewModel.enqueueIllustDownload(illust: Illust) {
    val requiresAuth = illust.isUgoira
    if (requiresAuth && _uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    val pages = illust.imageUrls.ifEmpty { listOfNotNull(illust.previewUrl) }
    if (!illust.isUgoira && pages.isEmpty()) {
        _uiState.update { it.copy(message = "没有可下载的图片地址") }
        return
    }

    val ugoiraFormat = _uiState.value.ugoiraSaveFormat
    val job = DownloadItem(
        key = "${illust.id}-${System.nanoTime()}",
        illustId = illust.id,
        title = illust.title,
        illust = illust,
        fileName = if (illust.isUgoira) {
            "${illust.safeFolderName()}_动图.${ugoiraFormat.extension}"
        } else {
            illust.safeFolderName()
        },
        status = DownloadStatus.Queued,
        isUgoira = illust.isUgoira,
        pageCount = if (illust.isUgoira) 1 else pages.size,
        relativePath = if (illust.isUgoira) {
            buildDownloadRelativePath(
                rootDirectory = Environment.DIRECTORY_PICTURES,
                authorName = illust.authorName,
            )
        } else {
            buildDownloadRelativePath(
                rootDirectory = Environment.DIRECTORY_PICTURES,
                authorName = illust.authorName,
                title = illust.title,
            )
        },
        detail = if (illust.isUgoira) "等待合成 ${ugoiraFormat.name}" else "等待下载 ${pages.size} 张",
    )

    _uiState.update { state ->
        val downloads = state.downloads.copy(items = listOf(job) + state.downloads.items.filterNot { it.illustId == illust.id })
        store.saveDownloads(downloads.items)
        state.copy(
            downloads = downloads,
            message = "已加入下载队列",
        )
    }
    startDownload(job, illust)
}
internal fun PixivViewModel.deleteDownloadItem(key: String) {
    _uiState.update { state ->
        val downloads = state.downloads.copy(
            items = state.downloads.items.filterNot { it.key == key },
        )
        store.saveDownloads(downloads.items)
        state.copy(
            downloads = downloads,
            message = "已删除下载记录",
        )
    }
}
internal fun PixivViewModel.uploadIllust(
    title: String,
    caption: String,
    tags: List<String>,
    type: String,
    visibilityScope: Int,
    xRestrict: String,
    isSexual: Boolean,
    illustAiType: Int,
    imageUris: List<Uri>,
) {
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (title.isBlank()) {
        _uiState.update { it.copy(message = "请输入作品标题") }
        return
    }
    if (imageUris.isEmpty()) {
        _uiState.update { it.copy(message = "请至少选择一张图片") }
        return
    }

    viewModelScope.launch {
        _uiState.update { state ->
            state.copy(
                mine = state.mine.copy(isUploading = true, uploadStatus = "正在上传"),
                message = null,
            )
        }
        runCatching {
            val request = UploadIllustRequest(
                title = title.trim(),
                caption = caption.trim(),
                tags = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(10),
                type = type.takeIf { it == "manga" } ?: "illust",
                visibilityScope = visibilityScope.coerceIn(1, 3),
                commentAccessControl = 0,
                xRestrict = xRestrict.takeIf { it == "r18" || it == "r18g" } ?: "none",
                isSexual = isSexual,
                illustAiType = illustAiType.coerceIn(1, 2),
                images = imageUris.take(20).toUploadParts(),
            )
            withAccessToken { token -> repository.uploadIllust(token, request) }
        }.onSuccess { status ->
            val summary = status.illustId?.takeIf { it > 0L }?.let { "投稿成功：#$it" }
                ?: status.status?.takeIf { it.isNotBlank() }?.let { "投稿处理中：$it" }
                ?: "投稿已提交"
            _uiState.update { state ->
                state.copy(
                    mine = state.mine.copy(isUploading = false, uploadStatus = summary),
                    message = summary,
                )
            }
            loadMyWorks(refresh = true)
        }.onFailure { error ->
            _uiState.update { state ->
                state.copy(
                    mine = state.mine.copy(isUploading = false, uploadStatus = error.readableMessage()),
                    message = error.readableMessage(),
                )
            }
        }
    }
}
internal fun PixivViewModel.uploadNovel(
    title: String,
    caption: String,
    text: String,
    tags: List<String>,
    visibilityScope: Int,
    xRestrict: String,
    isSexual: Boolean,
    novelAiType: Int,
    isOriginal: Boolean,
    coverUri: Uri?,
) {
    if (_uiState.value.session?.accessToken.isNullOrBlank()) {
        requireLogin()
        return
    }
    if (title.isBlank()) {
        _uiState.update { it.copy(message = "请输入小说标题") }
        return
    }
    if (text.isBlank()) {
        _uiState.update { it.copy(message = "请输入小说正文") }
        return
    }

    viewModelScope.launch {
        _uiState.update { state ->
            state.copy(
                mine = state.mine.copy(isUploading = true, uploadStatus = "正在上传"),
                message = null,
            )
        }
        runCatching {
            val request = UploadNovelRequest(
                title = title.trim(),
                caption = caption.trim(),
                text = text,
                tags = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(10),
                visibilityScope = visibilityScope.coerceIn(1, 3),
                commentAccessControl = 0,
                xRestrict = xRestrict.takeIf { it == "r18" || it == "r18g" } ?: "none",
                isSexual = isSexual,
                novelAiType = novelAiType.coerceIn(1, 2),
                isOriginal = isOriginal,
                cover = coverUri?.let { listOf(it).toUploadParts(prefix = "cover").firstOrNull() },
            )
            withAccessToken { token -> repository.uploadNovel(token, request) }
        }.onSuccess { response ->
            val summary = response.novelId?.takeIf { it > 0L }?.let { "小说投稿成功：#$it" }
                ?: response.convertKey?.takeIf { it.isNotBlank() }?.let { "小说投稿处理中" }
                ?: "小说投稿已提交"
            _uiState.update { state ->
                state.copy(
                    mine = state.mine.copy(isUploading = false, uploadStatus = summary),
                    message = summary,
                )
            }
            loadMyWorks(refresh = true)
        }.onFailure { error ->
            _uiState.update { state ->
                state.copy(
                    mine = state.mine.copy(isUploading = false, uploadStatus = error.readableMessage()),
                    message = error.readableMessage(),
                )
            }
        }
    }
}
internal fun PixivViewModel.startDownload(item: DownloadItem, illust: Illust) {
    viewModelScope.launch(Dispatchers.IO) {
        updateDownload(item.key, persist = false) {
            it.copy(status = DownloadStatus.Running, detail = "下载中")
        }
        runCatching {
            if (illust.isUgoira) {
                val result = withAccessToken { token ->
                    repository.downloadUgoira(
                        id = illust.id,
                        token = token,
                        includeZip = _uiState.value.saveUgoiraZip,
                        workingDirectory = getApplication<Application>().cacheDir,
                        saveFormat = _uiState.value.ugoiraSaveFormat,
                    ) { stage, current, total ->
                        val detail = when {
                            total > 0 -> "$stage $current/$total"
                            else -> stage
                        }
                        updateDownload(item.key, persist = false) {
                            it.copy(
                                status = DownloadStatus.Running,
                                detail = detail,
                            )
                        }
                    }
                }
                updateDownload(item.key, persist = false) {
                    it.copy(
                        status = DownloadStatus.Running,
                        detail = "保存动图",
                    )
                }
                val extension = result.format.extension
                val animatedFileName = item.fileName.substringBeforeLast('.') + ".$extension"
                val animatedUri = saveDownloadBytes(item.relativePath, animatedFileName, result.animatedBytes)
                val zipUri = result.zipBytes?.let { zipBytes ->
                    updateDownload(item.key, persist = false) {
                        it.copy(
                            status = DownloadStatus.Running,
                            detail = "保存 ZIP",
                        )
                    }
                    saveDownloadBytes(
                        buildDownloadRelativePath(
                            rootDirectory = Environment.DIRECTORY_DOWNLOADS,
                            authorName = illust.authorName,
                            title = illust.title,
                        ),
                        "${illust.safeFolderName()}_原图.zip",
                        zipBytes,
                    )
                }
                DownloadWriteResult(
                    mainUri = animatedUri,
                    zipUri = zipUri,
                    format = result.format,
                    savedUris = listOf(animatedUri.toString()),
                )
            } else {
                val pages = illust.imageUrls.ifEmpty { listOfNotNull(illust.previewUrl) }
                val savedUris = mutableListOf<Uri>()
                pages.forEachIndexed { index, imageUrl ->
                    updateDownload(item.key, persist = false) {
                        it.copy(
                            status = DownloadStatus.Running,
                            detail = "下载第 ${index + 1}/${pages.size} 张",
                        )
                    }
                    val bytes = repository.downloadImageBytes(imageUrl)
                    val savedUri = saveDownloadBytes(
                        item.relativePath,
                        "${(index + 1).toString().padStart(2, '0')}${imageUrl.fileExtension()}",
                        bytes,
                    )
                    savedUris += savedUri
                }
                DownloadWriteResult(
                    mainUri = requireNotNull(savedUris.firstOrNull()),
                    savedUris = savedUris.map { it.toString() },
                )
            }
        }.onSuccess { result ->
            updateDownload(item.key) {
                it.copy(
                    status = DownloadStatus.Finished,
                    fileName = if (illust.isUgoira && result.format != null) {
                        item.fileName.substringBeforeLast('.') + ".${result.format.extension}"
                    } else {
                        item.fileName
                    },
                    detail = when {
                        illust.isUgoira && result.zipUri != null -> "${result.format?.name ?: "动图"} 已保存，ZIP 已另存"
                        illust.isUgoira -> "${result.format?.name ?: "动图"} 已保存"
                        item.pageCount > 1 -> "已保存 ${item.pageCount} 张"
                        else -> "已保存"
                    },
                    savedUri = result.mainUri.toString(),
                    savedUris = result.savedUris.ifEmpty { listOf(result.mainUri.toString()) },
                    zipSavedUri = result.zipUri?.toString(),
                )
            }
        }.onFailure { error ->
            updateDownload(item.key) {
                it.copy(status = DownloadStatus.Failed, detail = error.readableMessage())
            }
        }
    }
}
internal fun PixivViewModel.startNovelDownload(item: DownloadItem, illust: Illust) {
    viewModelScope.launch(Dispatchers.IO) {
        updateDownload(item.key, persist = false) {
            it.copy(status = DownloadStatus.Running, detail = "下载小说正文")
        }
        runCatching {
            val currentReader = _uiState.value.novelReader
            val text = currentReader.text.takeIf {
                _uiState.value.selectedIllust?.id == illust.id && it.isNotBlank()
            } ?: withAccessToken { token ->
                repository.novelText(illust.id, token).text
            }
            val detail = currentReader.detail.takeIf { _uiState.value.selectedIllust?.id == illust.id }
            val title = detail?.title?.takeIf { it.isNotBlank() } ?: illust.title
            val author = detail?.authorName?.takeIf { it.isNotBlank() } ?: illust.authorName
            val header = buildString {
                appendLine(title)
                author.takeIf { it.isNotBlank() }?.let { appendLine("作者：$it") }
                illust.createDate?.takeIf { it.isNotBlank() }?.take(10)?.let { appendLine("日期：$it") }
                appendLine()
            }
            val savedUri = saveDownloadBytes(
                item.relativePath,
                "${illust.safeFolderName()}.txt",
                (header + text).toByteArray(Charsets.UTF_8),
            )
            DownloadWriteResult(
                mainUri = savedUri,
                savedUris = listOf(savedUri.toString()),
            )
        }.onSuccess { result ->
            updateDownload(item.key) {
                it.copy(
                    status = DownloadStatus.Finished,
                    detail = "小说正文已保存",
                    savedUri = result.mainUri.toString(),
                    savedUris = result.savedUris.ifEmpty { listOf(result.mainUri.toString()) },
                )
            }
        }.onFailure { error ->
            updateDownload(item.key) {
                it.copy(status = DownloadStatus.Failed, detail = error.readableMessage())
            }
        }
    }
}
internal fun PixivViewModel.updateDownload(
    key: String,
    persist: Boolean = true,
    transform: (DownloadItem) -> DownloadItem,
) {
    _uiState.update { state ->
        val downloads = state.downloads.copy(
            items = state.downloads.items.map { item ->
                if (item.key == key) transform(item) else item
            },
        )
        if (persist) {
            store.saveDownloads(downloads.items)
        }
        state.copy(
            downloads = downloads,
        )
    }
}
