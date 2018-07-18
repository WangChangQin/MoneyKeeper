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
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
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
            val retrofit = retrofitBuilder.baseUrl("http://dav.jianguoyun.com/dav/")
                    .build()
            davService = retrofit.create(DavService::class.java)
        }
        return davService!!
    }

    internal class AuthInterceptor : Interceptor {
        private val key = EncryptUtil.key
        private val salt = EncryptUtil.salt
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            val builder = request.newBuilder()

            if (ConfigManager.auth.isEmpty()) {
                val account = ConfigManager.jianguoyunAccount
                val psw = EncryptUtil.decrypt(ConfigManager.jianguoyunEncryptPsw, key, salt)
                ConfigManager.auth = "Basic " + Base64Util.encode("$account:$psw")
            }

            request = builder
                    .addHeader("Authorization", ConfigManager.auth)
                    .build()

            return chain.proceed(request)
        }
    }
}