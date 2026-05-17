package JunZi.Pixiv.data.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import JunZi.Pixiv.data.network.PixivImageProxy
import com.google.gson.annotations.SerializedName

@Immutable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtMillis: Long? = null,
    val userId: Long? = null,
    val userName: String? = null,
    val userAccount: String? = null,
    val userAvatarUrl: String? = null,
)

data class OAuthTokenResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("response") val response: OAuthTokenResponse? = null,
)

data class PixivErrorResponse(
    @SerializedName("error") val error: PixivError? = null,
    @SerializedName("errors") val errors: PixivError? = null,
)

data class PixivError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("user_message") val userMessage: String? = null,
    @SerializedName("system_message") val systemMessage: String? = null,
)

data class IllustsResponse(
    @SerializedName("illusts") val illusts: List<IllustDto>? = null,
    @SerializedName("next_url") val nextUrl: String? = null,
)

data class NovelsResponse(
    @SerializedName("novels") val novels: List<NovelDto>? = null,
    @SerializedName("next_url") val nextUrl: String? = null,
)

data class NovelDetailResponse(
    @SerializedName("novel") val novel: NovelDto? = null,
)

data class IllustDetailResponse(
    @SerializedName("illust") val illust: IllustDto? = null,
)

data class BookmarkTagsResponse(
    @SerializedName("bookmark_tags") val bookmarkTags: List<BookmarkTagDto>? = null,
    @SerializedName("next_url") val nextUrl: String? = null,
)

data class IllustBookmarkDetailResponse(
    @SerializedName("bookmark_detail") val bookmarkDetail: IllustBookmarkDetailDto? = null,
)

data class IllustBookmarkDetailDto(
    @SerializedName("is_bookmarked") val isBookmarked: Boolean? = null,
    @SerializedName("is_registered") val isRegistered: Boolean? = null,
    @SerializedName("restrict") val restrict: String? = null,
    @SerializedName("tags") val tags: List<BookmarkTagDto>? = null,
)

data class BookmarkTagDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("is_registered") val isRegistered: Boolean? = null,
)

data class UserDetailResponse(
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("profile") val profile: UserProfileDto? = null,
)

data class UserPreviewsResponse(
    @SerializedName("user_previews") val userPreviews: List<UserPreviewDto>? = null,
    @SerializedName("next_url") val nextUrl: String? = null,
)

data class UserPreviewDto(
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("illusts") val illusts: List<IllustDto>? = null,
)

data class IllustCommentsResponse(
    @SerializedName("comments") val comments: List<IllustCommentDto>? = null,
    @SerializedName("next_url") val nextUrl: String? = null,
)

enum class RankingMode(val apiValue: String) {
    Day("day"),
    DayAi("day_ai"),
    Week("week"),
    Month("month"),
    Male("day_male"),
    Female("day_female"),
    Rookie("week_rookie"),
    Original("week_original"),
    DayManga("day_manga"),
    WeekManga("week_manga"),
    MonthManga("month_manga"),
    DayNovel("day_novel"),
    DayAiNovel("day_ai_novel"),
    WeekNovel("week_novel"),
    MonthNovel("month_novel"),
    WeekRookieNovel("week_rookie_novel"),
    MaleNovel("day_male_novel"),
    FemaleNovel("day_female_novel");

    val category: HomeCategory = when (apiValue) {
        "day_manga", "week_manga", "month_manga" -> HomeCategory.Manga
        "day_novel", "day_ai_novel", "week_novel", "month_novel",
        "week_rookie_novel", "day_male_novel", "day_female_novel" -> HomeCategory.Novel
        else -> HomeCategory.Illust
    }

    companion object {
        const val WALKTHROUGH_API_VALUE = "walkthrough"
        const val RECOMMENDED_API_VALUE = "recommended"

        val liteApiValues: List<String> = listOf(
            WALKTHROUGH_API_VALUE,
            RECOMMENDED_API_VALUE,
        ) + entries.map { it.apiValue }

        fun fromApiValue(value: String): RankingMode? {
            return entries.firstOrNull { it.apiValue == value }
        }

        fun defaultFor(category: HomeCategory): RankingMode = when (category) {
            HomeCategory.Illust -> Day
            HomeCategory.Manga -> DayManga
            HomeCategory.Novel -> DayNovel
        }
    }
}

enum class HomeCategory {
    Illust,
    Manga,
    Novel,
}

enum class SearchSort(val apiValue: String) {
    DateDesc("date_desc"),
    DateAsc("date_asc"),
    PopularDesc("popular_desc"),
    PopularMale("popular_male_desc"),
    PopularFemale("popular_female_desc"),
}

enum class SearchTarget(val apiValue: String) {
    Partial("partial_match_for_tags"),
    Exact("exact_match_for_tags"),
    TitleCaption("title_and_caption"),
}

enum class SearchBookmarkThreshold(val bookmarkNum: Int?) {
    Any(null),
    Over100(100),
    Over500(500),
    Over1000(1000),
    Over5000(5000),
}

enum class BookmarkRestrict(val apiValue: String) {
    Public("public"),
    Private("private"),
}

data class TrendingTagsResponse(
    @SerializedName("trend_tags") val trendTags: List<TrendingTagDto>? = null,
)

data class TrendingTagDto(
    @SerializedName("tag") val tag: String? = null,
    @SerializedName("translated_name") val translatedName: String? = null,
    @SerializedName("illust") val illust: IllustDto? = null,
)

data class IllustDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("caption") val caption: String? = null,
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("tags") val tags: List<TagDto>? = null,
    @SerializedName("image_urls") val imageUrls: ImageUrlsDto? = null,
    @SerializedName("meta_single_page") val metaSinglePage: MetaSinglePageDto? = null,
    @SerializedName("meta_pages") val metaPages: List<MetaPageDto>? = null,
    @SerializedName("page_count") val pageCount: Int? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("sanity_level") val sanityLevel: Int? = null,
    @SerializedName("x_restrict") val xRestrict: Int? = null,
    @SerializedName("restrict") val restrict: Int? = null,
    @SerializedName("visible") val visible: Boolean? = null,
    @SerializedName("total_view") val totalView: Int? = null,
    @SerializedName("total_bookmarks") val totalBookmarks: Int? = null,
    @SerializedName("illust_ai_type") val illustAiType: Int? = null,
    @SerializedName("is_bookmarked") val isBookmarked: Boolean? = null,
    @SerializedName("tools") val tools: List<String>? = null,
    @SerializedName("series") val series: IllustSeriesDto? = null,
    @SerializedName("create_date") val createDate: String? = null,
)

data class IllustSeriesDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
)

data class NovelDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("caption") val caption: String? = null,
    @SerializedName("restrict") val restrict: Int? = null,
    @SerializedName("x_restrict") val xRestrict: Int? = null,
    @SerializedName("is_original") val isOriginal: Boolean? = null,
    @SerializedName("image_urls") val imageUrls: ImageUrlsDto? = null,
    @SerializedName("create_date") val createDate: String? = null,
    @SerializedName("tags") val tags: List<TagDto>? = null,
    @SerializedName("page_count") val pageCount: Int? = null,
    @SerializedName("text_length") val textLength: Int? = null,
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("series") val series: NovelSeriesDto? = null,
    @SerializedName("is_bookmarked") val isBookmarked: Boolean? = null,
    @SerializedName("visible") val visible: Boolean? = null,
    @SerializedName("total_view") val totalView: Int? = null,
    @SerializedName("total_bookmarks") val totalBookmarks: Int? = null,
    @SerializedName("total_comments") val totalComments: Int? = null,
    @SerializedName("novel_ai_type") val novelAiType: Int? = null,
)

data class NovelSeriesDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
)

data class UserDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("account") val account: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("profile_image_urls") val profileImageUrls: ProfileImageUrlsDto? = null,
    @SerializedName("is_followed") val isFollowed: Boolean? = null,
)

data class UserProfileDto(
    @SerializedName("webpage") val webpage: String? = null,
    @SerializedName("twitter_url") val twitterUrl: String? = null,
    @SerializedName("pawoo_url") val pawooUrl: String? = null,
    @SerializedName("background_image_url") val backgroundImageUrl: String? = null,
    @SerializedName("total_follow_users") val totalFollowUsers: Int? = null,
    @SerializedName("total_follower") val totalFollower: Int? = null,
    @SerializedName("total_mypixiv_users") val totalMyPixivUsers: Int? = null,
    @SerializedName("total_illusts") val totalIllusts: Int? = null,
    @SerializedName("total_manga") val totalManga: Int? = null,
    @SerializedName("total_novels") val totalNovels: Int? = null,
    @SerializedName("total_illust_bookmarks_public") val totalIllustBookmarksPublic: Int? = null,
)

data class IllustCommentDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("user") val user: UserDto? = null,
)

data class ProfileImageUrlsDto(
    @SerializedName("px_16x16") val px16x16: String? = null,
    @SerializedName("px_50x50") val px50x50: String? = null,
    @SerializedName("px_170x170") val px170x170: String? = null,
    @SerializedName("medium") val medium: String? = null,
)

data class TagDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("translated_name") val translatedName: String? = null,
)

data class ImageUrlsDto(
    @SerializedName("square_medium") val squareMedium: String? = null,
    @SerializedName("medium") val medium: String? = null,
    @SerializedName("large") val large: String? = null,
    @SerializedName("original") val original: String? = null,
)

data class MetaSinglePageDto(
    @SerializedName("original_image_url") val originalImageUrl: String? = null,
)

data class MetaPageDto(
    @SerializedName("image_urls") val imageUrls: ImageUrlsDto? = null,
)

data class UgoiraMetadataResponse(
    @SerializedName("ugoira_metadata") val metadata: UgoiraMetadataDto? = null,
)

data class UploadIllustResponse(
    @SerializedName("convert_key") val convertKey: String? = null,
)

data class UploadNovelResponse(
    @SerializedName("convert_key") val convertKey: String? = null,
    @SerializedName("novel_id") val novelId: Long? = null,
)

data class UploadStatusResponse(
    @SerializedName("illust_id") val illustId: Long? = null,
    @SerializedName("status") val status: String? = null,
)

data class UgoiraMetadataDto(
    @SerializedName("zip_urls") val zipUrls: ImageUrlsDto? = null,
    @SerializedName("frames") val frames: List<UgoiraFrameDto>? = null,
)

data class UgoiraFrameDto(
    @SerializedName("file") val file: String? = null,
    @SerializedName("delay") val delay: Int? = null,
)

@Immutable
data class Illust(
    val id: Long,
    val title: String,
    val authorId: Long,
    val authorName: String,
    val authorAccount: String,
    val authorAvatarUrl: String?,
    val type: String,
    val caption: String,
    val previewUrl: String?,
    val imageUrls: List<String>,
    val imagePages: List<IllustImagePage> = emptyList(),
    val tags: List<String>,
    val pageCount: Int,
    val width: Int,
    val height: Int,
    val totalBookmarks: Int,
    val totalView: Int,
    val isBookmarked: Boolean,
    val aiType: Int?,
    val createDate: String?,
    val seriesId: Long? = null,
    val seriesTitle: String? = null,
) {
    val isUgoira: Boolean = type == "ugoira"
    val aspectRatio: Float = (imagePages.firstOrNull()?.aspectRatio ?: (width.toFloat() / height.coerceAtLeast(1)))
        .coerceIn(0.45f, 2.25f)
}

@Immutable
data class IllustImagePage(
    val url: String,
    val width: Int,
    val height: Int,
) {
    val aspectRatio: Float = (width.toFloat() / height.coerceAtLeast(1)).coerceIn(0.18f, 4.5f)
}

@Immutable
data class IllustPage(
    val items: List<Illust>,
    val nextUrl: String?,
)

@Immutable
data class IllustComment(
    val id: Long,
    val text: String,
    val date: String?,
    val userId: Long,
    val userName: String,
    val userAccount: String,
    val userAvatarUrl: String?,
)

@Immutable
data class IllustCommentPage(
    val items: List<IllustComment>,
    val nextUrl: String?,
)

@Immutable
data class UgoiraFrameImage(
    val bitmap: Bitmap,
    val delayMs: Int,
)

@Immutable
data class TrendingTag(
    val name: String,
    val translatedName: String?,
    val previewUrl: String?,
)

@Immutable
data class AuthorProfile(
    val userId: Long,
    val userName: String,
    val userAccount: String,
    val avatarUrl: String?,
    val comment: String,
    val isFollowed: Boolean,
    val followingCount: Int,
    val followerCount: Int,
    val myPixivCount: Int,
    val totalIllusts: Int,
    val totalManga: Int,
    val totalNovels: Int,
    val totalBookmarks: Int,
)

@Immutable
data class UserPreview(
    val userId: Long,
    val userName: String,
    val userAccount: String,
    val avatarUrl: String?,
    val comment: String,
    val isFollowed: Boolean,
    val illusts: List<Illust>,
)

@Immutable
data class UserPreviewPage(
    val items: List<UserPreview>,
    val nextUrl: String?,
)

@Immutable
data class BookmarkTag(
    val name: String,
    val count: Int,
)

@Immutable
data class IllustBookmarkDetail(
    val isBookmarked: Boolean,
    val restrict: BookmarkRestrict,
    val tags: List<String>,
)

data class UploadImagePart(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
)

data class UploadIllustRequest(
    val title: String,
    val caption: String,
    val tags: List<String>,
    val type: String,
    val visibilityScope: Int,
    val commentAccessControl: Int,
    val xRestrict: String,
    val isSexual: Boolean,
    val illustAiType: Int,
    val images: List<UploadImagePart>,
)

data class UploadNovelRequest(
    val title: String,
    val caption: String,
    val text: String,
    val tags: List<String>,
    val visibilityScope: Int,
    val commentAccessControl: Int,
    val xRestrict: String,
    val isSexual: Boolean,
    val novelAiType: Int,
    val isOriginal: Boolean,
    val cover: UploadImagePart?,
)

fun IllustDto.toDomain(): Illust {
    val originalPages = metaPages.orEmpty().mapNotNull { it.imageUrls.bestOriginal() }
    val fallbackOriginal = firstNonBlank(metaSinglePage?.originalImageUrl) ?: imageUrls.bestOriginal()
    val images = if (originalPages.isNotEmpty()) {
        originalPages
    } else {
        listOfNotNull(PixivImageProxy.convert(fallbackOriginal))
    }
    val preview = PixivImageProxy.convert(
        firstNonBlank(imageUrls?.medium, imageUrls?.squareMedium, imageUrls?.large, images.firstOrNull()),
    )
    val normalizedWidth = width ?: 1
    val normalizedHeight = height ?: 1

    return Illust(
        id = id ?: 0L,
        title = title.orEmpty(),
        authorId = user?.id ?: 0L,
        authorName = user?.name.orEmpty(),
        authorAccount = user?.account.orEmpty(),
        authorAvatarUrl = user?.avatarUrl(),
        type = type.orEmpty(),
        caption = caption.orEmpty(),
        previewUrl = preview,
        imageUrls = images,
        imagePages = images.map { url ->
            IllustImagePage(
                url = url,
                width = normalizedWidth,
                height = normalizedHeight,
            )
        },
        tags = tags.orEmpty().mapNotNull { it.translatedName ?: it.name }.take(8),
        pageCount = pageCount ?: images.size.coerceAtLeast(1),
        width = normalizedWidth,
        height = normalizedHeight,
        totalBookmarks = totalBookmarks ?: 0,
        totalView = totalView ?: 0,
        isBookmarked = isBookmarked ?: false,
        aiType = illustAiType,
        createDate = createDate,
        seriesId = series?.id?.takeIf { it > 0L },
        seriesTitle = series?.title?.takeIf { it.isNotBlank() },
    )
}

fun ImageUrlsDto?.bestOriginal(): String? {
    if (this == null) return null
    return PixivImageProxy.convert(firstNonBlank(original, large, medium, squareMedium))
}

fun NovelDto.toDomain(): Illust {
    val cover = PixivImageProxy.convert(
        firstNonBlank(imageUrls?.large, imageUrls?.medium, imageUrls?.squareMedium, imageUrls?.original),
    )
    val coverList = listOfNotNull(cover)
    val width = NOVEL_COVER_WIDTH
    val height = NOVEL_COVER_HEIGHT
    return Illust(
        id = id ?: 0L,
        title = title.orEmpty(),
        authorId = user?.id ?: 0L,
        authorName = user?.name.orEmpty(),
        authorAccount = user?.account.orEmpty(),
        authorAvatarUrl = user?.avatarUrl(),
        type = "novel",
        caption = caption.orEmpty(),
        previewUrl = cover,
        imageUrls = coverList,
        imagePages = coverList.map { url ->
            IllustImagePage(url = url, width = width, height = height)
        },
        tags = tags.orEmpty().mapNotNull { it.translatedName ?: it.name }.take(8),
        pageCount = pageCount ?: 1,
        width = width,
        height = height,
        totalBookmarks = totalBookmarks ?: 0,
        totalView = totalView ?: 0,
        isBookmarked = isBookmarked ?: false,
        aiType = novelAiType,
        createDate = createDate,
        seriesId = series?.id?.takeIf { it > 0L },
        seriesTitle = series?.title?.takeIf { it.isNotBlank() },
    )
}

@Immutable
data class NovelTextPayload(
    val text: String,
    val uploadedImages: Map<String, String> = emptyMap(),
    val pixivImages: Map<String, String> = emptyMap(),
)

@Immutable
data class NovelDetail(
    val id: Long,
    val title: String,
    val caption: String,
    val authorId: Long,
    val authorName: String,
    val authorAccount: String,
    val authorAvatarUrl: String?,
    val coverUrl: String?,
    val tags: List<String>,
    val textLength: Int,
    val totalView: Int,
    val totalBookmarks: Int,
    val totalComments: Int,
    val isBookmarked: Boolean,
    val isOriginal: Boolean,
    val xRestrict: Int,
    val seriesId: Long?,
    val seriesTitle: String?,
    val createDate: String?,
)

fun NovelDto.toDetailDomain(): NovelDetail? {
    val novelId = id ?: return null
    return NovelDetail(
        id = novelId,
        title = title.orEmpty(),
        caption = caption.orEmpty(),
        authorId = user?.id ?: 0L,
        authorName = user?.name.orEmpty(),
        authorAccount = user?.account.orEmpty(),
        authorAvatarUrl = user?.avatarUrl(),
        coverUrl = PixivImageProxy.convert(
            firstNonBlank(imageUrls?.large, imageUrls?.medium, imageUrls?.squareMedium),
        ),
        tags = tags.orEmpty().mapNotNull { it.translatedName ?: it.name },
        textLength = textLength ?: 0,
        totalView = totalView ?: 0,
        totalBookmarks = totalBookmarks ?: 0,
        totalComments = totalComments ?: 0,
        isBookmarked = isBookmarked ?: false,
        isOriginal = isOriginal ?: false,
        xRestrict = xRestrict ?: 0,
        seriesId = series?.id,
        seriesTitle = series?.title,
        createDate = createDate,
    )
}

private const val NOVEL_COVER_WIDTH = 600
private const val NOVEL_COVER_HEIGHT = 900

fun TrendingTagDto.toDomain(): TrendingTag? {
    val name = tag?.takeIf { it.isNotBlank() } ?: return null
    return TrendingTag(
        name = name,
        translatedName = translatedName,
        previewUrl = illust?.toDomain()?.previewUrl,
    )
}

fun BookmarkTagDto.toDomain(): BookmarkTag? {
    val normalizedName = name?.trim()?.trimStart('#')?.takeIf { it.isNotBlank() } ?: return null
    return BookmarkTag(name = normalizedName, count = count ?: 0)
}

fun IllustBookmarkDetailDto.toDomain(): IllustBookmarkDetail {
    val restrictValue = restrict?.lowercase()
    return IllustBookmarkDetail(
        isBookmarked = isRegistered ?: isBookmarked ?: false,
        restrict = BookmarkRestrict.entries.firstOrNull { it.apiValue == restrictValue } ?: BookmarkRestrict.Public,
        tags = tags.orEmpty()
            .filter { it.isRegistered == true }
            .mapNotNull { it.name?.trim()?.trimStart('#')?.takeIf(String::isNotBlank) }
            .distinctBy { it.lowercase() }
            .take(10),
    )
}

fun IllustCommentDto.toDomain(): IllustComment? {
    val text = comment?.takeIf { it.isNotBlank() } ?: return null
    return IllustComment(
        id = id ?: 0L,
        text = text,
        date = date,
        userId = user?.id ?: 0L,
        userName = user?.name.orEmpty(),
        userAccount = user?.account.orEmpty(),
        userAvatarUrl = user?.avatarUrl(),
    )
}

fun UserDto.avatarUrl(): String? {
    return PixivImageProxy.convert(
        firstNonBlank(
            profileImageUrls?.px170x170,
            profileImageUrls?.medium,
            profileImageUrls?.px50x50,
            profileImageUrls?.px16x16,
        ),
    )
}

fun UserDetailResponse.toDomain(): AuthorProfile? {
    val detailUser = user ?: return null
    val userId = detailUser.id ?: return null
    return AuthorProfile(
        userId = userId,
        userName = detailUser.name.orEmpty(),
        userAccount = detailUser.account.orEmpty(),
        avatarUrl = detailUser.avatarUrl(),
        comment = detailUser.comment.orEmpty(),
        isFollowed = detailUser.isFollowed ?: false,
        followingCount = profile?.totalFollowUsers ?: 0,
        followerCount = profile?.totalFollower ?: 0,
        myPixivCount = profile?.totalMyPixivUsers ?: 0,
        totalIllusts = profile?.totalIllusts ?: 0,
        totalManga = profile?.totalManga ?: 0,
        totalNovels = profile?.totalNovels ?: 0,
        totalBookmarks = profile?.totalIllustBookmarksPublic ?: 0,
    )
}

fun UserPreviewDto.toDomain(): UserPreview? {
    val previewUser = user ?: return null
    val userId = previewUser.id ?: return null
    return UserPreview(
        userId = userId,
        userName = previewUser.name.orEmpty(),
        userAccount = previewUser.account.orEmpty(),
        avatarUrl = previewUser.avatarUrl(),
        comment = previewUser.comment.orEmpty(),
        isFollowed = previewUser.isFollowed ?: false,
        illusts = illusts.orEmpty().map { it.toDomain() },
    )
}

private fun firstNonBlank(vararg values: String?): String? {
    return values.firstOrNull { !it.isNullOrBlank() }?.trim()
}
