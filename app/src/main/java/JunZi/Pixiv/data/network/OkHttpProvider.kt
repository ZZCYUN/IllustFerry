package JunZi.Pixiv.data.network

import okhttp3.Interceptor
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object OkHttpProvider {
    private val baseBuilder: OkHttpClient.Builder
        get() = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .dns(PixivDns())
            .sslSocketFactory(PixivUnsafeTls.socketFactory(), PixivUnsafeTls.trustManager)
            .hostnameVerifier(PixivUnsafeTls.hostnameVerifier)

    private val apiClient: OkHttpClient by lazy {
        baseBuilder
            .addInterceptor(ApiDirectInterceptor)
            .build()
    }

    private val imageClient: OkHttpClient by lazy {
        baseBuilder
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
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

    private val cleanClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @JvmStatic
    fun apiClient(): OkHttpClient = apiClient

    @JvmStatic
    fun imageClient(): OkHttpClient = imageClient

    fun directClient(): OkHttpClient = directClient

    fun cleanClient(): OkHttpClient = cleanClient

    fun currentApiClient(): OkHttpClient = if (PixivNetworkConfig.shouldUseCompatibilityClient()) apiClient else cleanClient

    fun currentImageClient(): OkHttpClient = if (PixivNetworkConfig.shouldUseCompatibilityClient()) imageClient else cleanClient

    @JvmStatic
    fun imageCallFactory(): Call.Factory = CurrentImageCallFactory

    private object CurrentImageCallFactory : Call.Factory {
        override fun newCall(request: Request): Call {
            return currentImageClient().newCall(request)
        }
    }

    private object ApiDirectInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            val originalHost = request.url.host
            val pixivHost = PixivHost.from(originalHost)

            val builder = request.newBuilder()
            if (pixivHost?.isApiHost == true) {
                PixivNetworkConfig.ipFor(originalHost)
                    ?.takeIf { it != originalHost }
                    ?.let { ip -> builder.url(request.url.newBuilder().host(ip).build()) }
            }
            PixivHeaders.addAppHeaders(builder, pixivHost?.takeIf { it.isApiHost }?.rawHost)
            return chain.proceed(builder.build())
        }
    }

    private object ImageProxyInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            val candidates = PixivImageProxy.candidateUrls(request.url)
            var lastFailure: IOException? = null

            candidates.forEachIndexed { index, url ->
                val candidateRequest = request.newBuilder()
                    .url(url)
                    .removeHeader("Host")
                    .header("User-Agent", PixivHeaders.IMAGE_USER_AGENT)
                    .header("Referer", PixivHeaders.IMAGE_PROXY_REFERER)
                    .build()

                val response = try {
                    chain.proceed(candidateRequest)
                } catch (error: IOException) {
                    lastFailure = error
                    return@forEachIndexed
                }

                if (response.isSuccessful || index == candidates.lastIndex || !response.shouldRetryImage()) {
                    return response
                }

                response.close()
            }

            throw lastFailure ?: IOException("Pixiv image request failed without a response")
        }

        private fun okhttp3.Response.shouldRetryImage(): Boolean {
            return code == 403 || code == 404 || code == 429 || code >= 500
        }
    }
}
