package JunZi.Pixiv.data.local

import JunZi.Pixiv.data.model.PixivisionArticle
import JunZi.Pixiv.data.model.PixivisionBlock
import JunZi.Pixiv.data.network.PixivImageProxy
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * pixivision 特辑文章解析器（Jsoup DOM 精确抓取）。
 *
 * 页面结构（pixivision WordPress 主题）：
 *  - 标题 `h1.am__title`，日期 `time._date[datetime]`，头部标签 `ul.am__header-tags a.tls__list-item`
 *  - 正文 `div._feature-article-body` 的直接子节点按文档顺序即内容块：
 *    `div.article-item._feature-article-body__paragraph`   → 段落（内部多个 <p>）
 *    `div.article-item._feature-article-body__article_thumbnail` → 头图
 *    `div.article-item._feature-article-body__pixiv_illust`      → 内嵌作品卡（点击进应用内预览）
 *    其余 h2/h3 → 小标题；ul → 列表
 */
object PixivisionArticleParser {

    fun parse(articleId: Long, html: String): PixivisionArticle {
        val document = Jsoup.parse(html, "https://www.pixivision.net/")

        val title = document.selectFirst("h1.am__title")?.text()?.trim()
            ?: document.selectFirst("meta[property=og:title]")?.attr("content").orEmpty()

        val publishDate = document.selectFirst("time._date")?.attr("datetime")
            ?.takeIf { it.isNotBlank() }

        val tags = document.select("ul.am__header-tags a.tls__list-item")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }

        val cover = document.selectFirst("meta[property=og:image]")?.attr("content")
            ?.takeIf { it.isNotBlank() }
            ?.let(PixivImageProxy::convert)

        val blocks = mutableListOf<PixivisionBlock>()
        val bodyRoot = document.selectFirst("div._feature-article-body")
        if (bodyRoot != null) {
            for (child in bodyRoot.children()) {
                val block = parseBlock(child) ?: continue
                // 正文头图（article_thumbnail）与 og:image 封面常为同一张，去重避免画两遍。
                if (block is PixivisionBlock.Image && block.url == cover) continue
                blocks += block
            }
        }

        return PixivisionArticle(
            articleId = articleId,
            title = title,
            publishDate = publishDate,
            coverUrl = cover,
            tags = tags,
            blocks = blocks,
        )
    }

    private fun parseBlock(element: Element): PixivisionBlock? {
        val classes = element.classNames()
        return when {
            element.hasClass("article-item") -> when {
                classes.any { it.contains("_feature-article-body__paragraph") } ->
                    element.select("p")
                        .map { it.text().trim() }
                        .filter { it.isNotBlank() }
                        .takeIf { it.isNotEmpty() }
                        ?.let { PixivisionBlock.Paragraph(it.joinToString("\n")) }

                classes.any { it.contains("_feature-article-body__article_thumbnail") } ->
                    element.selectFirst("img.aie__image")?.absUrl("src")
                        ?.takeIf { it.isNotBlank() }
                        ?.let { PixivisionBlock.Image(PixivImageProxy.convert(it) ?: it) }

                classes.any { it.contains("_feature-article-body__pixiv_illust") } ->
                    parseArtworkCard(element)

                else -> null
            }

            element.tagName() in setOf("h2", "h3", "h4") ->
                element.text().trim().takeIf { it.isNotBlank() }?.let { PixivisionBlock.Heading(it) }

            element.tagName() == "ul" ->
                element.select("li")
                    .map { it.text().trim() }
                    .filter { it.isNotBlank() }
                    .takeIf { it.isNotEmpty() }
                    ?.let { PixivisionBlock.BulletList(it) }

            element.tagName() == "p" ->
                element.text().trim().takeIf { it.isNotBlank() }?.let { PixivisionBlock.Paragraph(it) }

            element.tagName() == "img" ->
                element.absUrl("src").takeIf { it.isNotBlank() }?.let { PixivisionBlock.Image(PixivImageProxy.convert(it) ?: it) }

            else -> null
        }
    }

    /**
     * 作品卡：标题链接带 /artworks/{id}；缩略图取块内第一张非用户头像的图。
     * 结构：<div class="am__work">
     *   <div class="am__work__info"> …用户头像/标题/作者…
     *   <div class="am__work__main"> …作品缩略图（i.pximg.net /img-master/ 或 /c/…）
     */
    private fun parseArtworkCard(element: Element): PixivisionBlock.ArtworkCard? {
        val titleLink = element.selectFirst("h3.am__work__title a[href]")
        val illustId = titleLink?.absUrl("href")
            ?.let { ARTWORK_ID_REGEX.find(it)?.groupValues?.get(1)?.toLongOrNull() }
            ?: return null

        val title = titleLink.text().trim()
        val authorName = element.selectFirst("p.am__work__user-name a")?.text()
            ?.trim()?.removePrefix("by ")?.trim().orEmpty()

        val thumb = element.select("img")
            .firstOrNull { !it.absUrl("src").contains("/user-profile/") }
            ?.absUrl("src")
            ?.takeIf { it.isNotBlank() }
            ?.let(PixivImageProxy::convert)

        return PixivisionBlock.ArtworkCard(
            illustId = illustId,
            title = title,
            authorName = authorName,
            thumbUrl = thumb,
        )
    }

    private val ARTWORK_ID_REGEX = Regex("""/artworks/(\d+)""")

    private val ARTICLE_ID_REGEX = Regex("""/a/(\d+)""")
    private val BACKGROUND_URL_REGEX = Regex("""url\((['\"]?)(.*?)\1\)""")

    /**
     * pixivision 分类页（/zh/c/{category}）的文章列表 + 下一页地址。
     * 主列表是 li.article-card-container > article._article-card（每页 20 篇）；
     * 页尾 a.next 指向下一页（/zh/c/{slug}/?p=N）。注意不要抓侧栏
     * （aside.sidebar-container 里 _article-summary-card 的排行榜/推荐跨分类重复）。
     */
    fun parseCategoryArticles(html: String): Pair<List<JunZi.Pixiv.data.model.SpotlightArticle>, String?> {
        val document = Jsoup.parse(html, "https://www.pixivision.net/")
        val items = document.select("li.article-card-container > article._article-card").mapNotNull { card ->
            val link = card.selectFirst("a[href*=/a/]") ?: return@mapNotNull null
            val articleId = ARTICLE_ID_REGEX.find(link.absUrl("href"))?.groupValues?.get(1)?.toLongOrNull()
                ?: return@mapNotNull null
            val title = card.selectFirst("h2.arc__title a")?.text().orEmpty()
            val background = card.selectFirst("div._thumbnail")?.attr("style").orEmpty()
            val thumbnail = BACKGROUND_URL_REGEX.find(background)
                ?.groupValues?.get(2)
                ?.takeIf { it.isNotBlank() }
                ?.let(PixivImageProxy::convert)
            val date = card.selectFirst("time._date")?.attr("datetime")?.takeIf { it.isNotBlank() }
            JunZi.Pixiv.data.model.SpotlightArticle(
                id = articleId,
                title = title,
                publishDate = date,
                thumbnailUrl = thumbnail,
                articleUrl = link.absUrl("href"),
            )
        }
        val nextUrl = document.selectFirst("a.next")?.absUrl("href")?.takeIf { it.isNotBlank() }
        return items to nextUrl
    }
}
