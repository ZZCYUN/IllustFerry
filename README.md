# IllustFerry

IllustFerry（画渡）是一个面向 pixiv 的非官方 Android 客户端实验项目，面向中文用户在复杂网络环境中的日常浏览体验，重点是网络兼容直连、本地代理与图片加载 fallback，以及 Jetpack Compose 客户端界面。

本项目与 pixiv Inc. 无关，不包含 pixiv 作品内容、用户数据、调试日志或固定代理 CA / 代理私钥。仓库中的 Android 测试签名配置仅用于调试和打包示例，正式分发请替换为自己的签名密钥。

## 功能

- 登录：应用内 WebView OAuth 登录代理、手动 token 登录，运行时生成内存 CA，不分发固定代理证书。
- 网络兼容：所有 pixiv 域名请求统一走内置 7891 中间人代理（MITM），各域名按其服务位置自动选择兼容直连策略并按可用性回退；地址解析与握手配置均通过加密 DNS 实时获取并持久化，图片支持手动配置远端代理 fallback。
- 首页：插画、漫画、小说、珍藏册四个顶层分栏，支持推荐、排行榜、最新作品子页面切换，AI 生成作品在卡片左上角显示 AI 标识。
- 特辑（pixivision）：底栏特辑入口，插画 / 漫画 / 小说三个分类，文章列表与正文均为原生渲染，文章内嵌作品可一键跳转应用内预览。
- 珍藏册：浏览 pixiv 官方珍藏册，支持推荐、大家都在看、标签分组与全部列表，详情页展示收录作品并可打开应用内预览。
- 发现：公开关注作品、悄悄关注作品、趋势标签、标签/标题/作者/小说搜索、热门预览、搜索结果分页自动加载，并可从搜索结果返回关注作品列表。
- 搜索筛选：搜索目标、排序方式、日期范围、收藏数阈值等参数会随搜索刷新。
- 作品预览：作品详情、横向翻页或纵向连续阅读、全屏半透明顶栏预览、相关作品、系列入口、评论、收藏和下载。
- 小说：小说详情、正文阅读、评论、收藏、正文图片展示和 TXT 下载。
- 作者页：查看作者资料、插画/漫画/小说作品分页、关注、悄悄关注与取消关注。
- 我的：个人作品、插画/小说收藏列表、浏览历史、公开/悄悄关注用户列表、下载记录和基础投稿面板。
- 下载：多图作品保留全部分页，小说保存为 TXT，已下载内容支持离线本地预览；GIF / 动 WebP 预览保持动画播放；下载元数据本地 SQLite 持久化，无数量上限。
- 动图：支持 WebP / GIF 输出，并可按需额外保留 pixiv 原始 zip。
- 设置：Host/IP 路由、DoH 服务器选择（内置多个公共 DoH，支持自定义输入）、图片代理（手动配置）、历史记录保留上限、过滤标签、预览方向、动图格式、保留 zip、网络诊断和开源许可查看。
- 实验功能：设置页可进入"Web Pixiv"，使用应用内置代理 WebView 直接访问 pixiv.net 官网。

## 网络兼容设计

网络层是本项目的核心。不同 pixiv 域名的服务位置不同，网络层会按域名自动选择对应的兼容直连策略，并在多种策略间按可用性回退：

- `PixivDnsUpdater`：通过加密 DNS（RFC 8484）实时解析各域名的地址记录并持久化；握手所需的 TLS 扩展配置也从加密 DNS 的记录中自动获取。端点支持自动探测与手动指定，失效后自动重新探测。
- `echtunnel`（Go + gomobile）：基于 Go 标准加密库编译的传输层，对上游连接按域名启用相应的 TLS 握手策略（部分域名启用加密握手扩展，部分域名跳过握手可见字段），证书均做完整校验。
- `LocalPixivProxy`：进程内 7891 中间人代理（MITM），统一承接 OkHttp 与 WebView 的 pixiv 域名流量，上游连接全部由上述传输层完成。
- `PixivHost` / `PixivNetworkConfig`：pixiv Host 清单与运行时地址配置，写入时过滤环回 / 私有 / 保留等无效地址。
- `PixivImageProxy`：图片默认直连；应用不内置任何远端图片代理，可在设置页手动输入代理地址，开启后按可用性回退。

具体实现细节请直接阅读源码。内置的 DNS 端点均来自公开的公共服务，可在设置页更换或填入自定义端点。

## 技术栈

主要运行时与应用依赖：

- Kotlin 与 Kotlin Coroutines Android 1.11.0
- Jetpack Compose BOM 2026.05.00、Compose UI / Foundation、Material 3、Material Icons Extended
- AndroidX Activity Compose 1.13.0
- AndroidX Core KTX 1.18.0
- AndroidX Lifecycle Runtime Compose / ViewModel Compose 2.10.0
- AndroidX WebKit 1.16.0
- OkHttp 5.3.2
- Gson 2.14.0
- Jsoup 1.18.3
- Glide 5.0.7、Glide OkHttp integration、Glide GIF Encoder integration、Glide compiler
- webp-android 1.1.2
- Bouncy Castle `bcprov-jdk18on` / `bcpkix-jdk18on` 1.84

构建相关：

- Java 17
- Android Gradle Plugin 9.2.1
- Kotlin Compose plugin 2.3.21
- Gradle 9.5.1
- compileSdk 36
- minSdk 26
- targetSdk 36

## 构建

普通用户无需自行构建，可以前往 [Releases](https://github.com/ZZCYUN/IllustFerry/releases) 下载已经打包好的 APK。

Windows：

```powershell
.\gradlew.bat assembleDebug
```

macOS / Linux：

```bash
./gradlew assembleDebug
```

Debug APK 输出路径：

```text
app/build/outputs/apk/debug/
```

项目已启用 ABI 拆包，Debug / Release 构建会生成 `arm64-v8a`、`armeabi-v7a`、`x86`、`x86_64` 与 universal APK。Release APK 输出路径：

```text
app/build/outputs/apk/release/
```

## 使用

1. 安装 APK。
2. 使用应用内 WebView OAuth 登录，或手动填入 access token。
3. 在设置页按需开启 Host/IP 兼容路由、刷新 DNS、运行网络诊断，并配置图片代理。
4. 首页在插画 / 漫画 / 小说分栏下通过推荐 / 排行 / 最新入口切换子页面；底栏「特辑」浏览 pixivision 官方特辑，「珍藏册」浏览 pixiv 官方珍藏册。
5. 在首页、发现页或作者页浏览作品；搜索结果可分页自动加载，也可以返回关注作品列表。
6. 在作品详情页下载图片或动图，已下载内容可在“我的 / 下载”中离线预览。

Token 保存在本应用的 Android `SharedPreferences` 中；下载元数据与浏览历史保存在本地 SQLite 数据库（`puxiv_downloads.db` / `puxiv_history.db`）。请勿公开包含账号信息的日志、截图或备份。

## 说明

本项目仅供学习、研究与个人使用。请尊重创作者权益，遵守 pixiv 相关条款与所在地法律法规，不要用于批量抓取、未经授权转载、账号滥用或其他不当用途。

网络兼容功能会按设置使用内置 Host/IP 映射、远端图片代理和兼容 TLS 路径。启用这些功能前，请确认自己理解对应的网络与信任边界。

## 许可证

本项目使用 GNU General Public License v3.0 only（GPL-3.0-only）授权，授权标识见仓库根目录的 `LICENSE`。

任何人使用、复制、修改、分发或基于本项目发布衍生版本时，都必须遵守 GPL-3.0 的全部条款。二次分发修改版时，必须继续以 GPL-3.0 兼容方式开放对应源码，并保留原始版权与许可证声明。

第三方依赖的许可证请以各项目原始许可证为准；应用内“设置 / 开源许可”列出了主要运行时依赖及其许可证入口。
