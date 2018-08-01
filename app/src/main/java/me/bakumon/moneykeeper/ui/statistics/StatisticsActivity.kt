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
import android.view.Menu
import android.view.MenuItem
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivityStatisticsBinding
import me.bakumon.moneykeeper.ui.statistics.bill.BillFragment
import me.bakumon.moneykeeper.ui.statistics.reports.ReportsFragment
import me.bakumon.moneykeeper.utill.DateUtils
import me.drakeet.floo.Floo

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
        mBinding.toolbarLayout?.title = DateUtils.getCurrentYearMonth()
        mBinding.toolbarLayout?.tvTitle?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        mBinding.toolbarLayout?.tvTitle?.compoundDrawablePadding = 10
        mBinding.toolbarLayout?.tvTitle?.setOnClickListener { chooseMonth() }
        setSupportActionBar(mBinding.toolbarLayout?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mBinding.typeChoice?.rbOutlay?.setText(R.string.text_order)
        mBinding.typeChoice?.rbIncome?.setText(R.string.text_reports)

        setUpFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_statistics, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.action_review -> Floo.navigation(this, Router.Url.URL_REVIEW).start()
            android.R.id.home -> finish()
        }
        return true
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
        mBinding.toolbarLayout?.tvTitle?.isEnabled = false
        val chooseMonthDialog = ChooseMonthDialog(this, mCurrentYear, mCurrentMonth)
        chooseMonthDialog.mOnDismissListener = {
            mBinding.toolbarLayout?.tvTitle?.isEnabled = true
        }
        chooseMonthDialog.mOnChooseListener = { year, month ->
            mCurrentYear = year
            mCurrentMonth = month
            mBinding.toolbarLayout?.title = DateUtils.getYearMonthFormatString(year, month)
            mBillFragment.setYearMonth(year, month)
            mReportsFragment.setYearMonth(year, month)
        }
        chooseMonthDialog.show()
    }
}
