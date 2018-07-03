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

package me.bakumon.moneykeeper.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View

import me.bakumon.moneykeeper.BuildConfig
import me.bakumon.moneykeeper.Constant
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivityAboutBinding
import me.bakumon.moneykeeper.utill.AlipayZeroSdk
import me.bakumon.moneykeeper.utill.AndroidUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo

/**
 * 关于
 *
 * @author Bakumon https://bakumon.me
 */
class AboutActivity : BaseActivity() {
    private lateinit var mBinding: ActivityAboutBinding

    override val layoutId: Int
        get() = R.layout.activity_about

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()

        initView()
    }

    private fun initView() {
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }
        mBinding.titleBar?.title = getString(R.string.text_title_about)
        mBinding.titleBar?.ibtRight?.visibility = View.VISIBLE
        mBinding.titleBar?.ibtRight?.setOnClickListener { share() }

        mBinding.tvVersion.text = BuildConfig.VERSION_NAME
    }

    private fun share() {
        AndroidUtil.share(this, getString(R.string.text_share_content, Constant.URL_APP_DOWNLOAD))
    }

    fun market(view: View) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.show(R.string.toast_not_install_market)
            e.printStackTrace()
        }
    }

    fun alipay(view: View) {
        if (AlipayZeroSdk.hasInstalledAlipayClient(this)) {
            AlipayZeroSdk.startAlipayClient(this, Constant.ALIPAY_CODE)
        } else {
            ToastUtils.show(R.string.toast_not_install_alipay)
        }
    }

    fun goOpenSource(view: View) {
        Floo.navigation(this, Router.Url.URL_OPEN_SOURCE)
                .start()
    }

    fun contactAuthor(view: View) {
        try {
            val data = Intent(Intent.ACTION_SENDTO)
            data.data = Uri.parse("mailto:" + Constant.AUTHOR_EMAIL)
            data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.text_feedback) + getString(R.string.app_name))
            val content = "\n\n\n\n\n\n______________________________" +
                    "\n" + getString(R.string.text_phone_brand) + android.os.Build.BRAND +
                    "\n" + getString(R.string.text_phone_model) + android.os.Build.MODEL +
                    "\n" + getString(R.string.text_system_version) + android.os.Build.VERSION.RELEASE +
                    "\n" + getString(R.string.text_app_version) + BuildConfig.VERSION_NAME
            data.putExtra(Intent.EXTRA_TEXT, content)
            startActivity(data)
        } catch (e: Exception) {
            ToastUtils.show(R.string.toast_not_install_email)
            e.printStackTrace()
        }
    }

    fun goPrivacy(view: View) {
        AndroidUtil.openWeb(this, Constant.URL_PRIVACY)
    }

    fun goHelp(view: View) {
        AndroidUtil.openWeb(this, Constant.URL_HELP)
    }
}
