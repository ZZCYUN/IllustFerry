package JunZi.Pixiv.data.network

enum class PixivHost(
    val rawHost: String,
    val defaultIp: String,
    val isApiHost: Boolean,
    private val fallbackIps: List<String> = emptyList(),
) {
    AppApi("app-api.pixiv.net", "210.140.139.158", true, listOf("210.140.139.161", "210.140.139.155", "210.140.139.152")),
    OAuth("oauth.secure.pixiv.net", "210.140.139.158", true, listOf("210.140.139.161", "210.140.139.155", "210.140.139.152")),
    Accounts("accounts.pixiv.net", "210.140.139.158", true, listOf("210.140.139.161", "210.140.139.155", "210.140.139.152")),
    Source("source.pixiv.net", "210.140.139.158", true, listOf("210.140.139.161", "210.140.139.155", "210.140.139.152")),
    PublicApi("public-api.secure.pixiv.net", "210.140.139.158", true, listOf("210.140.139.161", "210.140.139.152", "210.140.139.155")),
    Image("i.pximg.net", "210.140.139.133", false),
    StaticImage("s.pximg.net", "210.140.139.133", false),
    Pixivision("www.pixivision.net", "210.140.131.224", false),
    Web("www.pixiv.net", "210.140.131.199", false, listOf("210.140.139.158", "104.18.12.135", "104.18.13.135"));

    val defaultIps: List<String> = (listOf(defaultIp) + fallbackIps).distinct()

    companion object {
        private val byHost = entries.associateBy { it.rawHost }

        fun from(host: String?): PixivHost? = byHost[host]
    }
}
