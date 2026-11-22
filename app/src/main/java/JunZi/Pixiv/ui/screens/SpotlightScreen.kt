package JunZi.Pixiv.ui

import JunZi.Pixiv.AppScreen
import JunZi.Pixiv.CollectionBooksState
import JunZi.Pixiv.CollectionDetailState
import JunZi.Pixiv.PixivViewModel
import JunZi.Pixiv.SpotlightArticleState
import JunZi.Pixiv.SpotlightCategory
import JunZi.Pixiv.SpotlightState
import JunZi.Pixiv.data.model.PixivCollection
import JunZi.Pixiv.data.model.PixivisionArticle
import JunZi.Pixiv.data.model.PixivisionBlock
import JunZi.Pixiv.data.model.SpotlightArticle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 底部「特辑」（pixiv Spotlight / pixivision）原生页。
 * 数据来自 app-api /v1/spotlight/articles（OAuth），结构与首页一致：
 * 分类 Tab + 分区头 + 瀑布流卡片 + 加载更多。文章正文在应用内代理 WebView 打开。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SpotlightScreen(
    spotlight: SpotlightState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onSelectCategory: (SpotlightCategory) -> Unit,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenArticle: (SpotlightArticle) -> Unit,
) {
    val category = spotlight.selectedCategory
    val feed = spotlight.feeds[category] ?: JunZi.Pixiv.SpotlightFeedState()
    val selectedTabIndex = SpotlightCategory.entries.indexOf(category)

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("特辑", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "pixivision · pixiv 官方特辑",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                SpotlightCategory.entries.forEach { option ->
                    Tab(
                        selected = category == option,
                        onClick = { onSelectCategory(option) },
                        text = { Text(option.label) },
                    )
                }
            }
            // 滑到底部且还有下一页时自动加载更多。
            val feedGridState = rememberLazyStaggeredGridState()
            val shouldLoadMoreSpotlight by derivedStateOf {
                val info = feedGridState.layoutInfo
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
                feed.nextUrl != null && !feed.isLoading && info.totalItemsCount > 0 &&
                    last >= info.totalItemsCount - 5
            }
            LaunchedEffect(shouldLoadMoreSpotlight) {
                if (shouldLoadMoreSpotlight) onLoadMore()
            }
            LazyVerticalStaggeredGrid(
                state = feedGridState,
                columns = StaggeredGridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
            ) {
                spotlightFeedSection(
                    label = "特辑 · ${category.label}",
                    feed = feed,
                    onLoad = onLoad,
                    onLoadMore = onLoadMore,
                    onOpenArticle = onOpenArticle,
                )
            }
        }
    }
}

internal fun LazyStaggeredGridScope.spotlightFeedSection(
    label: String,
    feed: JunZi.Pixiv.SpotlightFeedState,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenArticle: (SpotlightArticle) -> Unit,
) {
    item(
        key = "spotlight-header",
        contentType = "section-header",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        SectionHeader(
            title = label,
            count = feed.items.size,
            isLoading = feed.isLoading,
            onRefresh = onLoad,
            modifier = Modifier,
        )
    }
    if (feed.error != null) {
        item(
            key = "spotlight-error",
            contentType = "feed-error",
            span = StaggeredGridItemSpan.FullLine,
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Text(
                        text = feed.error,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = onLoad) { Text("重试") }
            }
        }
    }
    if (feed.items.isEmpty() && feed.isLoading) {
        item(
            key = "spotlight-loading",
            contentType = "feed-loading",
            span = StaggeredGridItemSpan.FullLine,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
    }
    items(
        feed.items,
        key = { "spotlight-${it.id}" },
        contentType = { "spotlight-article" },
    ) { article ->
        SpotlightArticleCard(
            article = article,
            onClick = { onOpenArticle(article) },
        )
    }
    item(
        key = "spotlight-footer",
        contentType = "feed-footer",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                feed.isLoading && feed.items.isNotEmpty() ->
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)

                feed.nextUrl != null ->
                    TextButton(onClick = onLoadMore) { Text("加载更多特辑") }
            }
        }
    }
}

@Composable
internal fun SpotlightArticleCard(
    article: SpotlightArticle,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
    ) {
        Box {
            GlideImage(
                url = article.thumbnailUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                crop = true,
                requestSize = PuxivCardImageSize,
            )
            ImageScrim()
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = article.title.ifBlank { "#${article.id}" },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                article.publishDate?.take(10)?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.86f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * 珍藏册详情：pixiv 官方 Collection（web ajax）。
 * tiles 按官方「区域」网格顺序铺开，点击作品进应用内预览 / 小说阅读。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionDetailScreen(
    state: CollectionDetailState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenWork: (JunZi.Pixiv.data.model.Illust) -> Unit,
    onOpenWorkById: (Long) -> Unit,
) {
    val collection = state.collection
    val detail = state.detail
    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            collection?.title?.ifBlank { null } ?: "珍藏册",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = listOfNotNull(
                                collection?.userName?.takeIf { it.isNotBlank() }?.let { "作者：$it" },
                                collection?.viewCount?.takeIf { it > 0 }?.let { "${it} 次浏览" },
                                collection?.bookmarkCount?.takeIf { it > 0 }?.let { "${it} 次收藏" },
                            ).joinToString(" · ").ifBlank { "pixiv 珍藏册" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
        ) {
            item(
                key = "collection-meta",
                contentType = "collection-meta",
                span = StaggeredGridItemSpan.FullLine,
            ) {
                CollectionMetaPanel(state = state)
            }
            if (state.error != null) {
                item(
                    key = "collection-error",
                    contentType = "feed-error",
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ) {
                            Text(
                                text = state.error,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        TextButton(onClick = onRetry) { Text("重试") }
                    }
                }
            }
            if (detail == null && state.isLoading) {
                item(
                    key = "collection-loading",
                    contentType = "feed-loading",
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
                    }
                }
            }
            val works = detail?.works.orEmpty()
            items(
                detail?.tiles.orEmpty(),
                key = { "collection-tile-${it.tileId}" },
                contentType = { "collection-tile" },
            ) { tile ->
                val work = works[tile.workId]
                if (work != null) {
                    IllustCard(
                        illust = work,
                        onClick = onOpenWork,
                    )
                } else {
                    CollectionTilePlaceholder(
                        workId = tile.workId,
                        isNovel = tile.workType.equals("novel", ignoreCase = true),
                        onClick = { onOpenWorkById(tile.workId) },
                    )
                }
            }
            if (detail != null && detail.tiles.isEmpty() && !state.isLoading) {
                item(
                    key = "collection-empty",
                    contentType = "feed-empty",
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    EmptyStateCard("这本珍藏册还没有收录作品", Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
internal fun CollectionMetaPanel(state: CollectionDetailState) {
    val collection = state.collection
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (collection == null) return@Column
            Text(
                text = collection.title.ifBlank { "珍藏册" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (collection.caption.isNotBlank()) {
                Text(
                    text = collection.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (collection.tags.isNotEmpty()) {
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    collection.tags.take(8).forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "#$tag",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionTilePlaceholder(
    workId: Long,
    isNovel: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    if (isNovel) Icons.Default.MenuBook else Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (isNovel) "小说 #$workId" else "作品 #$workId",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 珍藏册横向卡片（珍藏册浏览页/首页共用样式）。 */
@Composable
internal fun CollectionBookCard(
    book: PixivCollection,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(132.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
    ) {
        Box {
            GlideImage(
                url = book.thumbnailUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                crop = true,
                requestSize = PuxivCardImageSize,
            )
            ImageScrim()
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OverlayChip("${book.viewCount} 浏览")
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = book.title.ifBlank { "#${book.id}" },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = book.userName.ifBlank { "pixiv 用户" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ==================== 特辑文章（原生渲染 pixivision 正文块） ====================

/**
 * 特辑文章页：正文抓 pixivision 页面并用 Jsoup 解析成内容块后原生渲染。
 * 内嵌作品卡点击直接进应用内预览，不再套 WebView。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SpotlightArticleScreen(
    headline: SpotlightArticle?,
    state: SpotlightArticleState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenIllust: (JunZi.Pixiv.data.model.Illust) -> Unit,
    onTagClick: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text("特辑文章", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = headline?.title?.takeIf { it.isNotBlank() } ?: "pixivision",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        val article = state.article
        when {
            state.error != null && article == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        Text(
                            text = state.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = onRetry) { Text("重试") }
                }
            }

            article == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 2.dp)
                }
            }

            else -> PixivisionArticleBody(
                article = article,
                contentPadding = padding,
                onOpenIllust = onOpenIllust,
                onTagClick = onTagClick,
            )
        }
    }
}

@Composable
internal fun PixivisionArticleBody(
    article: PixivisionArticle,
    contentPadding: PaddingValues,
    onOpenIllust: (JunZi.Pixiv.data.model.Illust) -> Unit,
    onTagClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        article.coverUrl?.let { cover ->
            item(key = "cover") {
                GlideImage(
                    url = cover,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    crop = true,
                    requestSize = PuxivCardImageSize,
                )
            }
        }
        item(key = "headline") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = article.title.ifBlank { "#${article.articleId}" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                article.publishDate?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (article.tags.isNotEmpty()) {
            item(key = "tags") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    article.tags.forEach { tag ->
                        AssistChip(
                            onClick = { onTagClick(tag) },
                            label = { Text("#$tag", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            }
        }
        items(article.blocks.size, key = { "block-$it" }) { index ->
            when (val block = article.blocks[index]) {
                is PixivisionBlock.Paragraph -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                is PixivisionBlock.Heading -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                is PixivisionBlock.Image -> GlideImage(
                    url = block.url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    crop = true,
                    requestSize = PuxivCardImageSize,
                )

                is PixivisionBlock.BulletList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.items.forEach { line ->
                        Text(
                            text = "· $line",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                is PixivisionBlock.ArtworkCard -> PixivisionArtworkCard(
                    block = block,
                    onClick = { onOpenIllust(block.toIllust()) },
                )
            }
        }
    }
}

@Composable
internal fun PixivisionArtworkCard(
    block: PixivisionBlock.ArtworkCard,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                GlideImage(
                    url = block.thumbUrl,
                    modifier = Modifier.matchParentSize(),
                    crop = true,
                    requestSize = 200,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = block.title.ifBlank { "#${block.illustId}" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = block.authorName.ifBlank { "pixiv 用户" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "查看作品 →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** 用文章里已知的标题/作者/缩略图构造最小 Illust：先秒进预览，详情由预览页异步补全。 */
internal fun PixivisionBlock.ArtworkCard.toIllust(): JunZi.Pixiv.data.model.Illust {
    val pageUrlList = listOfNotNull(thumbUrl)
    return JunZi.Pixiv.data.model.Illust(
        id = illustId,
        title = title,
        authorId = 0L,
        authorName = authorName,
        authorAccount = "",
        authorAvatarUrl = null,
        type = "illust",
        caption = "",
        previewUrl = thumbUrl,
        imageUrls = pageUrlList,
        imagePages = pageUrlList.map { JunZi.Pixiv.data.model.IllustImagePage(url = it, width = 1, height = 1) },
        tags = emptyList(),
        pageCount = 1,
        width = 1,
        height = 1,
        totalBookmarks = 0,
        totalView = 0,
        isBookmarked = false,
        aiType = null,
        createDate = null,
    )
}

// ==================== 珍藏册内容（首页顶部 tab 内嵌 + 结构对齐首页） ====================

/**
 * 珍藏册 tab 内容：top 聚合（推荐 / 大家都在看 / 标签组）横向卡片流 + 全部瀑布流分页。
 */
internal fun LazyStaggeredGridScope.collectionBooksSection(
    state: CollectionBooksState,
    onOpenBook: (PixivCollection) -> Unit,
    onLoadMore: () -> Unit,
) {
    if (state.error != null) {
        item(
            key = "books-error",
            contentType = "feed-error",
            span = StaggeredGridItemSpan.FullLine,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Text(
                        text = state.error,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onLoadMore) { Text("重试") }
            }
        }
    }
    if (state.top == null && state.isLoadingTop) {
        item(
            key = "books-loading",
            contentType = "feed-loading",
            span = StaggeredGridItemSpan.FullLine,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
    }
    val top = state.top
    top?.recommend?.takeIf { it.isNotEmpty() }?.let { list ->
        collectionStripSection(
            key = "books-recommend",
            title = "推荐",
            items = list,
            onOpenBook = onOpenBook,
        )
    }
    top?.everyone?.takeIf { it.isNotEmpty() }?.let { list ->
        collectionStripSection(
            key = "books-everyone",
            title = "大家都在看",
            items = list,
            onOpenBook = onOpenBook,
        )
    }
    top?.tagGroups.orEmpty().take(3).forEachIndexed { index, group ->
        collectionStripSection(
            key = "books-tag-$index",
            title = "标签 · ${group.tag}",
            items = group.items,
            onOpenBook = onOpenBook,
        )
    }
    item(
        key = "books-all-header",
        contentType = "section-header",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        SectionHeader(
            title = "全部",
            count = state.all.size,
            isLoading = state.isLoadingMore,
            onRefresh = onLoadMore,
        )
    }
    items(
        state.all,
        key = { "books-all-${it.id}" },
        contentType = { "collection-book" },
    ) { book ->
        CollectionBookGridCard(
            book = book,
            onClick = { onOpenBook(book) },
        )
    }
    item(
        key = "books-footer",
        contentType = "feed-footer",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoadingMore && state.all.isNotEmpty() ->
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)

                state.hasMore || state.all.isEmpty() ->
                    TextButton(onClick = onLoadMore) { Text(if (state.all.isEmpty()) "加载珍藏册" else "加载更多") }
            }
        }
    }
}

internal fun LazyStaggeredGridScope.collectionStripSection(
    key: String,
    title: String,
    items: List<PixivCollection>,
    onOpenBook: (PixivCollection) -> Unit,
) {
    item(
        key = "$key-header",
        contentType = "section-header",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        SectionHeader(
            title = title,
            count = items.size,
            isLoading = false,
            onRefresh = {},
            showRefresh = false,
        )
    }
    item(
        key = "$key-strip",
        contentType = "collection-strip",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 2.dp),
        ) {
            lazyRowItems(items, key = { it.id }) { book ->
                CollectionBookCard(
                    book = book,
                    onClick = { onOpenBook(book) },
                )
            }
        }
    }
}

@Composable
internal fun CollectionBookGridCard(
    book: PixivCollection,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
    ) {
        Column {
            Box {
                // 珍藏册封面源图是 1:1（288x288），按 1:1 展示避免裁边。
                GlideImage(
                    url = book.thumbnailUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    crop = true,
                    requestSize = PuxivCardImageSize,
                )
                ImageScrim()
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OverlayChip("${book.viewCount} 浏览")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = book.title.ifBlank { "#${book.id}" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = book.userName.ifBlank { "pixiv 用户" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
