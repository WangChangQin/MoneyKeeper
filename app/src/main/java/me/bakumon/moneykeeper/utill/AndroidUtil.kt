/*
 * Copyright 2018 Bakumon. https://github.com/Bakumon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.bakumon.moneykeeper.utill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import me.bakumon.moneykeeper.Constant
import me.bakumon.moneykeeper.R

/**
 * AndroidUtil
 *
 * @author Bakumon https://bakumon.me
 */
object AndroidUtil {
    /**
     * 去应用市场 app 查看（评分）应用
     */
    fun goMarket(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=" + context.packageName)
            context.startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.show(R.string.toast_not_install_market)
            e.printStackTrace()
        }
    }

    /**
     * 去支付宝捐赠
     */
    fun alipay(activity: Activity) {
        if (AlipayZeroSdk.hasInstalledAlipayClient(activity)) {
            AlipayZeroSdk.startAlipayClient(activity, Constant.ALIPAY_CODE)
        } else {
            ToastUtils.show(R.string.toast_not_install_alipay)
        }
    }

    /**
     * 通过浏览器打开
     * @param url 要打开的 Url
     */
    fun openWeb(context: Context, url: String) {
        val builder = CustomTabsIntent.Builder()
        // github 黑色把
        builder.setToolbarColor(Color.parseColor("#ff24292d"))
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}
