package me.bakumon.moneykeeper.api

import android.util.Log
import me.bakumon.moneykeeper.BuildConfig
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.utill.EncryptUtil
import okhttp3.Challenge
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

object Network {

    private var davService: DavService? = null
    private var retrofitBuilder: Retrofit.Builder
    private val key = EncryptUtil.key
    private val salt = EncryptUtil.salt

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
                .authenticator({ _, response ->

                    val challenges = response.challenges()

                    Log.e("mafei", challenges.toString())

                    val credential = if (challenges.size < 1) {
                        ""
                    } else {
                        if (challenges[0].scheme() == "Basic") {
                            // Basic 基本认证
                            basicAuth()
                        } else if (challenges[0].scheme() == "Digest") {
                            // Digest 摘要认证
                            digestAuth(challenges[0])
                        } else {
                            ""
                        }
                    }

                    if (credential.isEmpty() || credential == response.request().header("Authorization")) {
                        null
                    } else {
                        response.request().newBuilder()
                                .header("Authorization", credential)
                                .build()
                    }
                })
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

    private fun getPws(): String {
        return if (ConfigManager.webDAVPsw.isEmpty()) {
            EncryptUtil.decrypt(ConfigManager.webDavEncryptPsw, key, salt)
        } else {
            ConfigManager.webDAVPsw
        }
    }

    private fun basicAuth(): String {
        val account = ConfigManager.webDavAccount
        val psw = getPws()
        return Credentials.basic(account, psw)
    }

    private fun digestAuth(challenge: Challenge): String {
        // TODO Digest 摘要认证
        return ""
    }
}