package JunZi.Pixiv.data.network

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object OkHttpProvider {
    private val apiProxy = LocalPixivProxy()

    fun ensureApiProxyRunning(): Int {
        apiProxy.start()
        return apiProxy.port
    }

    fun stopApiProxy() = apiProxy.stop()

    private val proxyPort: Int
        get() {
            ensureApiProxyRunning()
            return apiProxy.port
        }

    private fun apiProxy(): Proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", proxyPort))

    private val proxySslFactory: javax.net.ssl.SSLSocketFactory by lazy {
        val ctx = javax.net.ssl.SSLContext.getInstance("TLS")
        ctx.init(null, arrayOf(PixivUnsafeTls.trustManager), java.security.SecureRandom())
        ctx.socketFactory
    }

    private val apiClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .proxy(apiProxy())
            .sslSocketFactory(proxySslFactory, PixivUnsafeTls.trustManager)
            .hostnameVerifier(PixivUnsafeTls.hostnameVerifier)
            .addInterceptor(ApiHeadersInterceptor)
            .build()
    }

    private val imageClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .proxy(apiProxy())
            .sslSocketFactory(proxySslFactory, PixivUnsafeTls.trustManager)
            .hostnameVerifier(PixivUnsafeTls.hostnameVerifier)
            .addInterceptor(ImageProxyInterceptor)
            .build()
    }

    private val directClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .build()
    }

    @JvmStatic
    fun apiClient(): OkHttpClient = apiClient

    @JvmStatic
    fun imageClient(): OkHttpClient = imageClient

    fun directClient(): OkHttpClient = directClient

    fun currentApiClient(): OkHttpClient = apiClient

    fun currentImageClient(): OkHttpClient = imageClient

    @JvmStatic
    fun imageCallFactory(): Call.Factory = CurrentImageCallFactory

    private object CurrentImageCallFactory : Call.Factory {
        override fun newCall(request: Request): Call {
            return currentImageClient().newCall(request)
        }
    }

    private object ApiHeadersInterceptor : okhttp3.Interceptor {
        override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            val pixivHost = PixivHost.from(request.url.host)
            val builder = request.newBuilder()
            PixivHeaders.addAppHeaders(builder, pixivHost?.takeIf { it.isApiHost }?.rawHost)
            return chain.proceed(builder.build())
        }
    }

    private object ImageProxyInterceptor : okhttp3.Interceptor {
        override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            val candidates = PixivImageProxy.candidateUrls(request.url)
            var lastFailure: java.io.IOException? = null

            candidates.forEachIndexed { index, url ->
                val candidateRequest = request.newBuilder()
                    .url(url)
                    .removeHeader("Host")
                    .header("User-Agent", PixivHeaders.IMAGE_USER_AGENT)
                    .header("Referer", PixivHeaders.IMAGE_PROXY_REFERER)
                    .build()

                val response = try {
                    chain.proceed(candidateRequest)
                } catch (error: java.io.IOException) {
                    lastFailure = error
                    return@forEachIndexed
                }

                if (response.isSuccessful || index == candidates.lastIndex || !response.shouldRetryImage()) {
                    return response
                }

                response.close()
            }

            throw lastFailure ?: java.io.IOException("Pixiv image request failed without a response")
        }

        private fun okhttp3.Response.shouldRetryImage(): Boolean {
            return code == 403 || code == 404 || code == 429 || code >= 500
        }
    }
}
