package JunZi.Pixiv.data.network

/**
 * 已知的 Pixiv 域名清单。
 *
 * 这里**不再保留任何兜底 IP**——所有可路由 IP 由 [PixivDnsUpdater] 在运行时
 * 通过外部 DoH 端点拉回来，写入 [PixivNetworkConfig.hostToIps]。运行时表为空
 * 时（首次启动且 DoH 还没回来、或 DoH 整个挂掉），调用方应回退到系统 DNS，
 * 而不是回到 APK 里固化的"过期 IP"——这正是把 210.140.131.199 长期排在
 * 候选首位、把 MITM 拖慢 N×20 秒的根因。
 */
enum class PixivHost(
    val rawHost: String,
    val isApiHost: Boolean,
) {
    AppApi("app-api.pixiv.net", true),
    OAuth("oauth.secure.pixiv.net", true),
    Accounts("accounts.pixiv.net", true),
    Source("source.pixiv.net", true),
    PublicApi("public-api.secure.pixiv.net", true),
    Image("i.pximg.net", false),
    StaticImage("s.pximg.net", false),
    Pixivision("www.pixivision.net", false),
    Web("www.pixiv.net", false);

    companion object {
        private val byHost = entries.associateBy { it.rawHost }

        fun from(host: String?): PixivHost? = byHost[host]
    }
}
