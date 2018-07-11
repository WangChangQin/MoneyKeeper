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

package me.bakumon.moneykeeper.ui.home

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.entity.SumMoneyBean
import me.bakumon.moneykeeper.databinding.LayoutHeadPageBinding
import me.bakumon.moneykeeper.databinding.LayoutHeadPageContent1Binding
import me.bakumon.moneykeeper.databinding.LayoutHeadPageContentBinding

/**
 * 翻页的 recyclerView + 指示器
 *
 * @author Bakumon https://bakumon.me
 */
class HeadPageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var mBinding: LayoutHeadPageBinding
    private var mContentBinding: LayoutHeadPageContentBinding
    private var mContentBinding1: LayoutHeadPageContent1Binding
    private val inflater = LayoutInflater.from(context)

    init {
        orientation = LinearLayout.VERTICAL
        mBinding = DataBindingUtil.inflate(inflater, R.layout.layout_head_page, this, true)
        mContentBinding = DataBindingUtil.inflate(inflater, R.layout.layout_head_page_content, null, true)
        mContentBinding1 = DataBindingUtil.inflate(inflater, R.layout.layout_head_page_content1, null, true)

        val viewList = listOf(mContentBinding.root, mContentBinding1.root)
        val adapter = HeadPagerAdapter(viewList)
        mBinding.viewPager.adapter = adapter

        mBinding.indicator.setTotal(adapter.count, mBinding.viewPager.currentItem)

        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                mBinding.indicator.setTotal(mBinding.viewPager.childCount, position)
            }
        })
    }

    fun setSumMoneyBeanList(beanList: List<SumMoneyBean>?) {
        mContentBinding.sumMoneyBeanList = beanList
        mContentBinding1.sumMoneyBeanList = beanList
    }

    internal inner class HeadPagerAdapter(private val mViews: List<View>) : PagerAdapter() {

        override fun getCount(): Int {
            return mViews.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(mViews[position])
            return mViews[position]
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(mViews[position])
        }
    }
}