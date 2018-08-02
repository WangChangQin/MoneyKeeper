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

package me.bakumon.moneykeeper.ui.review

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.database.entity.MonthSumMoneyBean
import me.bakumon.moneykeeper.databinding.ActivityReviewBinding
import me.bakumon.moneykeeper.utill.DateUtils
import me.bakumon.moneykeeper.utill.ToastUtils

/**
 * 回顾
 * 查看年账单或某几个月的数据
 *
 * @author Bakumon https://bakumon
 */
class ReviewActivity : BaseActivity() {
    private lateinit var mBinding: ActivityReviewBinding
    private lateinit var mViewModel: ReviewModel
    private lateinit var mAdapter: ReviewAdapter

    private var mCurrentYear = DateUtils.getCurrentYear()

    override val layoutId: Int
        get() = R.layout.activity_review

    override fun onInitView(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(ReviewModel::class.java)

        initView()
    }

    private fun initView() {
        mBinding.toolbarLayout?.title = mCurrentYear.toString()
        mBinding.toolbarLayout?.tvTitle?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        mBinding.toolbarLayout?.tvTitle?.compoundDrawablePadding = 10
        mBinding.toolbarLayout?.tvTitle?.setOnClickListener { chooseYear() }
        setSupportActionBar(mBinding.toolbarLayout?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mBinding.rvReview.layoutManager = LinearLayoutManager(this)
        mAdapter = ReviewAdapter(null)
        mBinding.rvReview.adapter = mAdapter

        initLineChart()

        updateData(mCurrentYear)
    }

    private fun chooseYear() {
        mBinding.toolbarLayout?.tvTitle?.isEnabled = false
        val chooseYearDialog = ChooseYearDialog(this, mCurrentYear)
        chooseYearDialog.mOnDismissListener = {
            mBinding.toolbarLayout?.tvTitle?.isEnabled = true
        }
        chooseYearDialog.mOnChooseListener = { year ->
            mCurrentYear = year
            mBinding.toolbarLayout?.title = mCurrentYear.toString()
            updateData(mCurrentYear)
        }
        chooseYearDialog.show()
    }

    private fun initLineChart() {
        mBinding.lineChart.setNoDataText("")
        mBinding.lineChart.setScaleEnabled(false)
        mBinding.lineChart.description.isEnabled = false
        mBinding.lineChart.legend.isEnabled = true
        mBinding.lineChart.legend.textColor = resources.getColor(R.color.colorText1)

        val marker = LineChartMarkerView(this)
        marker.chartView = mBinding.lineChart
        mBinding.lineChart.marker = marker

        val xAxis = mBinding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = resources.getColor(R.color.colorTextHint)
        xAxis.setLabelCount(12, true)
        xAxis.setValueFormatter { value, _ ->
            val intValue = value.toInt()
            if (intValue >= 0) {
                (intValue + 1).toString() + getString(R.string.text_month)
            } else {
                ""
            }
        }

        val leftAxis = mBinding.lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawAxisLine(true)
        leftAxis.setDrawGridLines(false)
        leftAxis.textSize = 0f
        leftAxis.textColor = Color.TRANSPARENT
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        val rightAxis = mBinding.lineChart.axisRight
        rightAxis.axisMinimum = 0f
        rightAxis.setDrawAxisLine(true)
        rightAxis.setDrawGridLines(false)
        rightAxis.textSize = 0f
        rightAxis.textColor = Color.TRANSPARENT
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)

    }

    private fun updateData(year: Int) {
        getYearSumMoney(year)
        getMonthOfYearSumMoney(year)
    }

    private fun getYearSumMoney(year: Int) {
        mDisposable.add(mViewModel.getYearSumMoney(year)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mBinding.sumMoneyBeanList = it
                }
                ) {
                    ToastUtils.show(R.string.toast_get_review_sum_money_fail)
                    Log.e(TAG, "获取回顾数据失败", it)
                })
    }

    private fun getMonthOfYearSumMoney(year: Int) {
        mDisposable.add(mViewModel.getMonthOfYearSumMoney(year)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mAdapter.setNewData(ReviewItemDataConverter.getBarEntryList(it))
                    if (it.isEmpty()) {
                        mBinding.lineChart.clear()
                        mAdapter.emptyView = inflate(R.layout.layout_statistics_empty)
                    } else {
                        setLineChartData(it)
                    }
                }
                ) {
                    ToastUtils.show(R.string.toast_get_review_fail)
                    Log.e(TAG, "获取回顾数据失败", it)
                })
    }

    private fun setLineChartData(beans: List<MonthSumMoneyBean>) {
        mBinding.lineChart.clear()
        val lineData = LineEntryConverter.getBarEntryList(beans)
        mBinding.lineChart.data = lineData
        mBinding.lineChart.animateY(1000)
    }

    companion object {
        private val TAG = ReviewActivity::class.java.simpleName
    }
}
