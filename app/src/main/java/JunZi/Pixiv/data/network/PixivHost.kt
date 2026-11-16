package JunZi.Pixiv.data.network

/**
 * 已知的 Pixiv 域名清单。
 *
 * 这里不保留任何兜底 IP。所有可路由 IP 由 [PixivDnsUpdater] 在运行时拉取，并由
 * [JunZi.Pixiv.data.auth.TokenStore] 持久化。兼容路由开启时，运行时表为空就等待拉取
 * 或使用上次持久化结果，绝不回退系统 DNS。
 */
enum class PixivHost(
    val rawHost: String,
    val isApiHost: Boolean,
    /**
     * 该域名的服务位置：
     *  - true  → Cloudflare（app-api / oauth / accounts / source / www / pixivision），
     *    需要走 ECH 隧道（外层 SNI=cloudflare-ech.com，内层 SNI=本域名）
     *  - false → pixiv Tokyo 自有源站（public-api / i.pximg / s.pximg），
     *    走无 SNI 直连，靠 Host 头路由
     */
    val isCloudflareFronted: Boolean,
) {
    AppApi("app-api.pixiv.net", true, true),
    OAuth("oauth.secure.pixiv.net", true, true),
    Accounts("accounts.pixiv.net", true, true),
    Source("source.pixiv.net", true, true),
    PublicApi("public-api.secure.pixiv.net", true, false),
    Image("i.pximg.net", false, false),
    StaticImage("s.pximg.net", false, false),
    Pixivision("www.pixivision.net", false, true),
    Web("www.pixiv.net", false, true);

    companion object {
        private val byHost = entries.associateBy { it.rawHost }

        fun from(host: String?): PixivHost? = byHost[host]
    }
}
