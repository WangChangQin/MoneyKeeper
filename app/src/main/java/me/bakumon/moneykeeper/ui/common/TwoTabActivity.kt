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

package me.bakumon.moneykeeper.ui.common

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.widget.RadioGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_two_tab.*
import kotlinx.android.synthetic.main.layout_tool_bar.view.*
import kotlinx.android.synthetic.main.layout_type_choose.view.*
import me.bakumon.moneykeeper.R
import java.util.*

/**
 * 两个tab fragment
 *
 * @author Bakumon https://bakumon
 */
abstract class TwoTabActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_two_tab

    protected abstract fun onSetupTitle(tvTitle: TextView)

    protected abstract fun getTwoTabText(): ArrayList<String>

    override fun onInitView(savedInstanceState: Bundle?) {

        onSetupTitle(toolbarLayout.tvTitle)

        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        typeChoose.rbLeft.text = getTwoTabText()[0]
        typeChoose.rbRight.text = getTwoTabText()[1]
    }

    protected abstract fun getTwoFragments(): ArrayList<Fragment>

    override fun onInit(savedInstanceState: Bundle?) {
        val adapter = FragmentViewPagerAdapter(supportFragmentManager, getTwoFragments())
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2

        (typeChoose as RadioGroup).setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbLeft) {
                viewPager.setCurrentItem(0, false)
            } else {
                viewPager.setCurrentItem(1, false)
            }
        }
        (typeChoose as RadioGroup).check(R.id.rbLeft)
    }

    inner class FragmentViewPagerAdapter(fm: FragmentManager, private val fragments: ArrayList<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

    }
}
