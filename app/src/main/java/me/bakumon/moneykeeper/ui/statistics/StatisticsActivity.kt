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
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.activity_two_tab.*
import kotlinx.android.synthetic.main.layout_tool_bar.view.*
import kotlinx.android.synthetic.main.layout_type_choose.view.*
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.ui.common.BaseActivity
import me.bakumon.moneykeeper.ui.common.ViewPagerAdapter
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
    private lateinit var mBillFragment: BillFragment
    private lateinit var mReportsFragment: ReportsFragment
    private var mCurrentYear = DateUtils.getCurrentYear()
    private var mCurrentMonth = DateUtils.getCurrentMonth()

    override val layoutId: Int
        get() = R.layout.activity_two_tab

    override fun onInitView(savedInstanceState: Bundle?) {
        toolbarLayout.tvTitle.text = DateUtils.getCurrentYearMonth()
        toolbarLayout.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        toolbarLayout.tvTitle.compoundDrawablePadding = 10
        toolbarLayout.tvTitle.setOnClickListener { chooseMonth() }
        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        typeChoose.rbLeft.setText(R.string.text_order)
        typeChoose.rbRight.setText(R.string.text_reports)
    }

    override fun onInit(savedInstanceState: Bundle?) {
        val infoPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        mBillFragment = BillFragment()
        mReportsFragment = ReportsFragment()
        infoPagerAdapter.addFragment(mBillFragment)
        infoPagerAdapter.addFragment(mReportsFragment)
        viewPager.adapter = infoPagerAdapter
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

    private fun chooseMonth() {
        toolbarLayout.tvTitle.isEnabled = false
        val chooseMonthDialog = ChooseMonthDialog(this, mCurrentYear, mCurrentMonth)
        chooseMonthDialog.mOnDismissListener = {
            toolbarLayout.tvTitle.isEnabled = true
        }
        chooseMonthDialog.mOnChooseListener = { year, month ->
            mCurrentYear = year
            mCurrentMonth = month
            toolbarLayout.tvTitle.text = DateUtils.getYearMonthFormatString(year, month)
            mBillFragment.setYearMonth(year, month)
            mReportsFragment.setYearMonth(year, month)
        }
        chooseMonthDialog.show()
    }
}
