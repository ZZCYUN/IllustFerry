package JunZi.Pixiv.data.network

import java.io.IOException

class PixivApiException(
    val code: Int,
    override val message: String,
    val bodyExcerpt: String? = null,
) : IOException(message)
