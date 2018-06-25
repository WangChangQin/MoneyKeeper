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

package me.bakumon.moneykeeper.ui.statistics

import android.os.Bundle
import android.view.View

import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivityStatisticsBinding
import me.bakumon.moneykeeper.ui.statistics.bill.BillFragment
import me.bakumon.moneykeeper.ui.statistics.reports.ChooseMonthDialog
import me.bakumon.moneykeeper.ui.statistics.reports.ReportsFragment
import me.bakumon.moneykeeper.utill.DateUtils

/**
 * 统计
 *
 * @author Bakumon https://bakumon
 */
class StatisticsActivity : BaseActivity() {
    private lateinit var mBinding: ActivityStatisticsBinding
    private lateinit var mBillFragment: BillFragment
    private lateinit var mReportsFragment: ReportsFragment
    private var mCurrentYear = DateUtils.getCurrentYear()
    private var mCurrentMonth = DateUtils.getCurrentMonth()

    override val layoutId: Int
        get() = R.layout.activity_statistics

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()

        initView()
    }

    private fun initView() {
        val title = DateUtils.getCurrentYearMonth()
        mBinding.titleBar?.title = title
        mBinding.titleBar?.ivTitle?.visibility = View.VISIBLE
        mBinding.titleBar?.llTitle?.setOnClickListener { chooseMonth() }
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }
        mBinding.typeChoice?.rbOutlay?.setText(R.string.text_order)
        mBinding.typeChoice?.rbIncome?.setText(R.string.text_reports)

        setUpFragment()
    }

    private fun setUpFragment() {
        val infoPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        mBillFragment = BillFragment()
        mReportsFragment = ReportsFragment()
        infoPagerAdapter.addFragment(mBillFragment)
        infoPagerAdapter.addFragment(mReportsFragment)
        mBinding.viewPager.adapter = infoPagerAdapter
        mBinding.viewPager.offscreenPageLimit = 2

        mBinding.typeChoice?.rgType?.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_outlay) {
                mBinding.viewPager.setCurrentItem(0, false)
            } else {
                mBinding.viewPager.setCurrentItem(1, false)
            }
        }
        mBinding.typeChoice?.rgType?.check(R.id.rb_outlay)
    }

    private fun chooseMonth() {
        mBinding.titleBar?.llTitle?.isEnabled = false
        val chooseMonthDialog = ChooseMonthDialog(this, mCurrentYear, mCurrentMonth)
        chooseMonthDialog.setOnDismissListener(object : ChooseMonthDialog.OnDismissListener {
            override fun onDismiss() {
                mBinding.titleBar?.llTitle?.isEnabled = true
            }
        })
        chooseMonthDialog.setOnChooseAffirmListener(object : ChooseMonthDialog.OnChooseAffirmListener {
            override fun onClick(year: Int, month: Int) {
                mCurrentYear = year
                mCurrentMonth = month
                val title = DateUtils.getYearMonthFormatString(year, month)
                mBinding.titleBar?.title = title
                mBillFragment.setYearMonth(year, month)
                mReportsFragment.setYearMonth(year, month)
            }
        })
        chooseMonthDialog.show()
    }
}
