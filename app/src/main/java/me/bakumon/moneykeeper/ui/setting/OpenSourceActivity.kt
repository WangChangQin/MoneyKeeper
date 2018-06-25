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
import android.support.v7.widget.LinearLayoutManager

import java.util.ArrayList

import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivitySettingBinding
import me.bakumon.moneykeeper.utill.AndroidUtil

/**
 * 开源许可证
 *
 * @author Bakumon https://bakumon.me
 */
class OpenSourceActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySettingBinding

    override val layoutId: Int
        get() = R.layout.activity_setting

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()

        initView()
    }

    private fun initView() {
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }
        mBinding.titleBar?.title = getString(R.string.text_title_open_source)

        mBinding.rvSetting.layoutManager = LinearLayoutManager(this)
        val adapter = OpenSourceAdapter(null)
        mBinding.rvSetting.adapter = adapter
        adapter.setOnItemClickListener { _, _, position -> AndroidUtil.openWeb(this, adapter.data[position].url) }

        val list = ArrayList<OpenSourceBean>()
        val support = OpenSourceBean("android support libraries - Google",
                "https://source.android.com",
                "Apache Software License 2.0")
        val lifecycle = OpenSourceBean("android arch lifecycle - Google",
                "https://source.android.com",
                "Apache Software License 2.0")
        val room = OpenSourceBean("android arch room - Google",
                "https://source.android.com",
                "Apache Software License 2.0")
        val rxJava = OpenSourceBean("RxJava - ReactiveX",
                "https://github.com/ReactiveX/RxJava",
                "Apache Software License 2.0")
        val rxAndroid = OpenSourceBean("RxAndroid - ReactiveX",
                "https://github.com/ReactiveX/rxAndroid",
                "Apache Software License 2.0")
        val leakcanary = OpenSourceBean("leakcanary - square",
                "https://github.com/square/leakcanary",
                "Apache Software License 2.0")
        val BRVAH = OpenSourceBean("BRVAH - CymChad",
                "https://github.com/CymChad/BaseRecyclerViewAdapterHelper",
                "Apache Software License 2.0")
        val chart = OpenSourceBean("MPAndroidChart - PhilJay",
                "https://github.com/PhilJay/MPAndroidChart",
                "Apache Software License 2.0")
        val floo = OpenSourceBean("floo - drakeet",
                "https://github.com/drakeet/Floo",
                "Apache Software License 2.0")
        val layoutManage = OpenSourceBean("StatusLayoutManager - Bakumon",
                "https://github.com/Bakumon/StatusLayoutManager",
                "MIT License")
        val permission = OpenSourceBean("easypermissions - googlesamples",
                "https://github.com/googlesamples/easypermissions",
                "Apache Software License 2.0")
        val storage = OpenSourceBean("android-storage - sromku",
                "https://github.com/sromku/android-storage",
                "Apache Software License 2.0")
        val datePicker = OpenSourceBean("MaterialDateTimePicker - wdullaer",
                "https://github.com/wdullaer/MaterialDateTimePicker",
                "Apache Software License 2.0")
        val pagerLayout = OpenSourceBean("pager-layoutmanager - GcsSloop",
                "https://github.com/GcsSloop/pager-layoutmanager",
                "Apache Software License 2.0")
        val layoutManagerGroup = OpenSourceBean("LayoutManagerGroup - DingMouRen",
                "https://github.com/DingMouRen/LayoutManagerGroup",
                "Apache Software License 2.0")
        val alipayZeroSDK = OpenSourceBean("AlipayZeroSdk - fython",
                "https://github.com/fython/AlipayZeroSdk",
                "Apache Software License 2.0")
        val prettytime = OpenSourceBean("prettytime - ocpsoft",
                "https://github.com/ocpsoft/prettytime",
                "Apache Software License 2.0")
        val circleImageView = OpenSourceBean("CircleImageView - hdodenhof",
                "https://github.com/hdodenhof/CircleImageView",
                "Apache Software License 2.0")

        list.add(support)
        list.add(lifecycle)
        list.add(room)
        list.add(rxJava)
        list.add(rxAndroid)
        list.add(leakcanary)
        list.add(BRVAH)
        list.add(chart)
        list.add(floo)
        list.add(layoutManage)
        list.add(permission)
        list.add(storage)
        list.add(datePicker)
        list.add(pagerLayout)
        list.add(layoutManagerGroup)
        list.add(alipayZeroSDK)
        list.add(prettytime)
        list.add(circleImageView)

        adapter.setNewData(list)
    }

}
