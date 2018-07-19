package me.bakumon.moneykeeper.api

import me.bakumon.moneykeeper.BuildConfig
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.utill.Base64Util
import me.bakumon.moneykeeper.utill.EncryptUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object Network {

    private var davService: DavService? = null
    private var retrofitBuilder: Retrofit.Builder

    init {
        // 日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .addInterceptor(loggingInterceptor)
                .addNetworkInterceptor(AuthInterceptor())
                .build()

        retrofitBuilder = Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
    }

    fun davService(): DavService {
        if (davService == null) {
            updateDavServiceBaseUrl()
        }
        return davService!!
    }

    fun updateDavServiceBaseUrl() {
        val url = ConfigManager.webDavUrl
        val baseUrl = if (url.endsWith("/")) url else "$url/"
        val retrofit = retrofitBuilder.baseUrl(baseUrl)
                .build()
        davService = retrofit.create(DavService::class.java)
    }

    internal class AuthInterceptor : Interceptor {
        private val key = EncryptUtil.key
        private val salt = EncryptUtil.salt
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            val builder = request.newBuilder()

            val account = ConfigManager.webDavAccount

            // TODO 动态处理 Authorization
            // https://blog.csdn.net/qq_30806949/article/details/52447771
            val auth = if (ConfigManager.webDAVPsw.isEmpty()) {
                val psw = EncryptUtil.decrypt(ConfigManager.webDavEncryptPsw, key, salt)
                "Basic " + Base64Util.encode("$account:$psw")
            } else {
                val psw = ConfigManager.webDAVPsw
                "Basic " + Base64Util.encode("$account:$psw")
            }

            request = builder
                    .addHeader("Authorization", auth)
                    .build()

            return chain.proceed(request)
        }
    }
}