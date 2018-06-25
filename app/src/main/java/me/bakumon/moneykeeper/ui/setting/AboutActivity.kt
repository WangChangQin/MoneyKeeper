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

package me.bakumon.moneykeeper.ui.setting

import android.os.Bundle
import android.view.View

import me.bakumon.moneykeeper.BuildConfig
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivityAboutBinding
import me.bakumon.moneykeeper.utill.AndroidUtil

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

        mBinding.tvVersion.text = BuildConfig.VERSION_NAME
    }

    fun goPrivacy(view: View) {
        AndroidUtil.openWeb(this, "https://github.com/Bakumon/MoneyKeeper/blob/master/PrivacyPolicy.md")
    }

    fun share(view: View) {
        AndroidUtil.share(this, getString(R.string.text_share_content))
    }

    fun goHelp(view: View) {
        AndroidUtil.openWeb(this, "https://github.com/Bakumon/MoneyKeeper/blob/master/Help.md")
    }
}
