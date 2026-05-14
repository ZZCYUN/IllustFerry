# IllustFerry

IllustFerry（画渡）是一个面向 pixiv 的非官方 Android 客户端实验项目，面向中文用户在复杂网络环境中的日常浏览体验，重点是本地网络兼容代理、可配置 Host IP 路由、图片加载 fallback，以及 Jetpack Compose 客户端界面。

本项目与 pixiv Inc. 无关，不包含 pixiv 作品内容、用户数据、调试日志、静态证书或私钥。

## 功能

- 应用内 WebView 登录代理，用于改善登录链路稳定性。
- 运行时生成内存 CA，不随仓库或安装包分发固定代理证书。
- pixiv 相关 Host 的内置 IP、备用 IP 与 DNS 刷新。
- API 请求 Host/IP 兼容路由。
- 图片直连、备用 Host、默认远端图片代理与可手动填写的图片代理 fallback。
- 首页推荐、排行榜、最新作品与搜索。
- 作品详情、相关作品、评论、收藏与下载队列。
- 个人作品、收藏列表与基础投稿面板。
- 网络诊断面板，用于检查 DNS、API 与图片访问状态。

## 网络兼容设计

网络层是本项目的核心：

- `LocalPixivProxy`：应用内 WebView 登录代理。
- `PixivHost` / `PixivNetworkConfig`：pixiv Host 与 IP fallback 配置。
- `PixivDnsUpdater`：刷新部分 Host 的可用 IP。
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

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Activity Compose
- AndroidX Core KTX
- AndroidX Lifecycle Runtime Compose
- AndroidX Lifecycle ViewModel Compose
- AndroidX WebKit
- Kotlin Coroutines Android
- OkHttp
- Gson
- Glide
- Glide OkHttp integration
- Bouncy Castle `bcprov-jdk18on`
- Bouncy Castle `bcpkix-jdk18on`

构建相关：

- Android Gradle Plugin 9.0.1
- Kotlin Compose plugin 2.2.10
- Gradle 9.1.0
- compileSdk 36
- minSdk 26
- targetSdk 36

## 构建

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

## 使用

1. 安装 Debug APK。
2. 使用 OAuth code 登录，或手动填入 access token。
3. 在设置页刷新 DNS、运行网络诊断，并按需切换或手动填写图片代理。

Token 保存在本应用的 Android `SharedPreferences` 中。请勿公开包含账号信息的日志、截图或备份。

## 说明

本项目仅供学习、研究与个人使用。请尊重创作者权益，遵守 pixiv 相关条款与所在地法律法规，不要用于批量抓取、未经授权转载、账号滥用或其他不当用途。

## 许可证

本项目使用 GNU General Public License v3.0（GPL-3.0）授权。

任何人使用、复制、修改、分发或基于本项目发布衍生版本时，都必须遵守 GPL-3.0 的全部条款。二次分发修改版时，必须继续以 GPL-3.0 兼容方式开放对应源码，并保留原始版权与许可证声明。
