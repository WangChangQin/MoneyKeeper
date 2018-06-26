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

package me.bakumon.moneykeeper.ui.opensource

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivityOpenSourceBinding
import me.bakumon.moneykeeper.databinding.ActivitySettingBinding
import me.bakumon.moneykeeper.utill.AndroidUtil

/**
 * 开源许可证
 *
 * @author Bakumon https://bakumon.me
 */
class OpenSourceActivity : BaseActivity() {
    private lateinit var mBinding: ActivityOpenSourceBinding

    override val layoutId: Int
        get() = R.layout.activity_open_source

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()

        initView()
    }

    private fun initView() {
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }
        mBinding.titleBar?.title = getString(R.string.text_title_open_source)

        mBinding.rvOpenSource.layoutManager = LinearLayoutManager(this)
        val adapter = OpenSourceAdapter(OpenSourceListCreator.openSourceList)
        mBinding.rvOpenSource.adapter = adapter
        adapter.setOnItemClickListener { _, _, position -> AndroidUtil.openWeb(this, adapter.data[position].url) }
    }

}
