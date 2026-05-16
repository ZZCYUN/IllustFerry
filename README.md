# IllustFerry

IllustFerry（画渡）是一个面向 pixiv 的非官方 Android 客户端实验项目，面向中文用户在复杂网络环境中的日常浏览体验，重点是本地网络兼容代理、可配置 Host IP 路由、图片加载 fallback，以及 Jetpack Compose 客户端界面。

本项目与 pixiv Inc. 无关，不包含 pixiv 作品内容、用户数据、调试日志或固定代理 CA / 代理私钥。仓库中的 Android 测试签名配置仅用于调试和打包示例，正式分发请替换为自己的签名密钥。

## 功能

- 登录：应用内 WebView OAuth 登录代理、手动 token 登录、运行时生成内存 CA，不分发固定代理证书。
- 网络兼容：pixiv 相关 Host 的内置 IP、备用 IP、DNS 刷新、API Host/IP 兼容路由，以及图片直连 / 备用 Host / 远端图片代理 fallback。
- 首页：推荐、排行榜、最新作品和搜索入口。
- 发现：公开关注作品、悄悄关注作品、趋势标签、标签/标题/作者搜索、热门预览、搜索结果分页自动加载，并可从搜索结果返回关注作品列表。
- 搜索筛选：搜索目标、排序方式、日期范围、收藏数阈值等参数会随搜索刷新。
- 作品预览：作品详情、横向翻页或纵向连续阅读、全屏半透明顶栏预览、相关作品、评论、收藏和下载。
- 作者页：查看作者资料、插画/漫画作品分页、关注、悄悄关注与取消关注。
- 我的：个人作品、收藏列表、浏览历史、公开/悄悄关注用户列表、下载记录和基础投稿面板。
- 下载：多图作品保留全部分页，已下载内容支持离线本地预览；GIF / 动 WebP 预览保持动画播放。
- 动图：支持 WebP / GIF 输出，并可按需额外保留 pixiv 原始 zip。
- 设置：Host/IP 路由、图片代理、过滤标签、预览方向、动图格式、保留 zip、网络诊断和开源许可查看。
- 实验功能：设置页可进入"Web Pixiv"，使用应用内置代理 WebView 直接访问 pixiv.net 官网。

## 网络兼容设计

网络层是本项目的核心：

- `LocalPixivProxy`：应用内 WebView 登录代理 / 浏览代理。
- `PixivHost` / `PixivNetworkConfig`：pixiv Host 与运行时 IP 配置。
- `PixivDnsUpdater`：通过外部 DoH 端点实时刷新可用 IP。
- `PixivDns` / `OkHttpProvider`：让 OkHttp 请求使用项目内 Host/IP 配置。
- `PixivImageProxy`：处理图片直连、备用 Host、默认远端代理与手动代理候选。
- `PixivUnsafeTls`：为兼容路径放宽证书与 Hostname 校验。

默认远端图片代理为 `https://i.yuki.sh`。感谢 [Yuki 妙妙屋](https://blog.yuki.sh/) 提供图片代理服务。

也可以在应用设置页手动输入图片代理地址。输入 `i.example.com` 这类域名时会自动按 `https://i.example.com` 保存；代理需要兼容 pixiv 图片原路径，例如 `/img-original/...`、`/img-master/...` 等。

### DNS 刷新接口

`PixivDnsUpdater` 默认请求 `https://api.sb6.me/getdnsipv4`，通过 `host` 查询参数获取目标域名的 IPv4 A 记录。我的 `getdnsipv4` 服务端源码如下：

```php
<?php
$host = isset($_GET['host']) ? trim($_GET['host']) : '';
$records = dns_get_record($host, DNS_A);
if ($records === false) {
    $ips = [];
} else {
    foreach ($records as $record) {
        if ($record['type'] === 'A' && isset($record['ip'])) {
            $ips[] = $record['ip'];
        }
    }
}
echo json_encode($ips);
?>
```

这段代码只返回 A 记录中的 IPv4 地址数组。若公开部署，建议根据自己的需求补充 `host` 格式校验、白名单或限流。

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
4. 在首页、发现页或作者页浏览作品；搜索结果可分页自动加载，也可以返回关注作品列表。
5. 在作品详情页下载图片或动图，已下载内容可在“我的 / 下载”中离线预览。

Token 保存在本应用的 Android `SharedPreferences` 中。请勿公开包含账号信息的日志、截图或备份。

## 说明

本项目仅供学习、研究与个人使用。请尊重创作者权益，遵守 pixiv 相关条款与所在地法律法规，不要用于批量抓取、未经授权转载、账号滥用或其他不当用途。

网络兼容功能会按设置使用内置 Host/IP 映射、远端图片代理和兼容 TLS 路径。启用这些功能前，请确认自己理解对应的网络与信任边界。

## 许可证

本项目使用 GNU General Public License v3.0 only（GPL-3.0-only）授权，授权标识见仓库根目录的 `LICENSE`。

任何人使用、复制、修改、分发或基于本项目发布衍生版本时，都必须遵守 GPL-3.0 的全部条款。二次分发修改版时，必须继续以 GPL-3.0 兼容方式开放对应源码，并保留原始版权与许可证声明。

第三方依赖的许可证请以各项目原始许可证为准；应用内“设置 / 开源许可”列出了主要运行时依赖及其许可证入口。
