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

package me.bakumon.moneykeeper.ui.statistics.reports

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.android.databinding.library.baseAdapters.BR

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseFragment
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.TypeSumMoneyBean
import me.bakumon.moneykeeper.databinding.FragmentReportsBinding
import me.bakumon.moneykeeper.utill.DateUtils
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo

/**
 * 统计-报表
 *
 * @author Bakumon https://bakumon.me
 */
class ReportsFragment : BaseFragment() {
    private lateinit var mBinding: FragmentReportsBinding
    private lateinit var mViewModel: ReportsViewModel
    private lateinit var mAdapter: ReportAdapter

    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mType: Int = 0

    override val layoutId: Int
        get() = R.layout.fragment_reports

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(ReportsViewModel::class.java)

        mYear = DateUtils.getCurrentYear()
        mMonth = DateUtils.getCurrentMonth()
        mType = RecordType.TYPE_OUTLAY

        initView()
    }

    private fun initView() {
        mBinding.rvRecordReports.layoutManager = LinearLayoutManager(context)
        mAdapter = ReportAdapter(null)
        mBinding.rvRecordReports.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position ->
            val (_, typeName, _, typeId) = mAdapter.data[position]
            navTypeRecords(typeName, typeId)
        }

        initPieChart()

        mBinding.layoutSumMoney?.rgType?.setOnCheckedChangeListener { _, checkedId ->
            mType = if (checkedId == R.id.rb_outlay) {
                RecordType.TYPE_OUTLAY
            } else {
                RecordType.TYPE_INCOME
            }
            getTypeSumMoney()
            getMonthSumMoney()
        }
    }

    private fun navTypeRecords(typeName: String, typeId: Int) {
        if (context != null) {
            Floo.navigation(context!!, Router.Url.URL_TYPE_RECORDS)
                    .putExtra(Router.ExtraKey.KEY_TYPE_NAME, typeName)
                    .putExtra(Router.ExtraKey.KEY_RECORD_TYPE, mType)
                    .putExtra(Router.ExtraKey.KEY_RECORD_TYPE_ID, typeId)
                    .putExtra(Router.ExtraKey.KEY_YEAR, mYear)
                    .putExtra(Router.ExtraKey.KEY_MONTH, mMonth)
                    .start()
        }
    }

    private fun initPieChart() {
        mBinding.pieChart.description.isEnabled = false
        mBinding.pieChart.setNoDataText("")
        mBinding.pieChart.setUsePercentValues(true)
        mBinding.pieChart.isDrawHoleEnabled = false
        mBinding.pieChart.isRotationEnabled = false

        mBinding.pieChart.legend.isEnabled = false
        mBinding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val typeName = (e.data as TypeSumMoneyBean).typeName
                val typeId = (e.data as TypeSumMoneyBean).typeId
                navTypeRecords(typeName, typeId)
            }

            override fun onNothingSelected() {

            }
        })
    }

    private fun setChartData(typeSumMoneyBeans: List<TypeSumMoneyBean>?) {
        if (typeSumMoneyBeans == null || typeSumMoneyBeans.isEmpty()) {
            mBinding.pieChart.visibility = View.INVISIBLE
            return
        } else {
            mBinding.pieChart.visibility = View.VISIBLE
        }

        val entries = PieEntryConverter.getBarEntryList(typeSumMoneyBeans)
        val dataSet: PieDataSet

        if (mBinding.pieChart.data != null && mBinding.pieChart.data.dataSetCount > 0) {
            dataSet = mBinding.pieChart.data.getDataSetByIndex(0) as PieDataSet
            dataSet.values = entries
            mBinding.pieChart.data.notifyDataChanged()
            mBinding.pieChart.notifyDataSetChanged()
        } else {
            dataSet = PieDataSet(entries, "")
            dataSet.sliceSpace = 0f
            dataSet.selectionShift = 1.2f
            dataSet.valueLinePart1Length = 0.3f
            dataSet.valueLinePart2Length = 1f
            dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            dataSet.valueTextSize = 10f
            dataSet.isValueLineVariableLength = true
            dataSet.valueLineColor = resources.getColor(R.color.colorTextWhite)

            val color: List<Int>
            if (entries.size % 7 == 0) {
                color = ColorTemplate.createColors(resources,
                        intArrayOf(R.color.colorPieChart1, R.color.colorPieChart2, R.color.colorPieChart3, R.color.colorPieChart4, R.color.colorPieChart5, R.color.colorPieChart6, R.color.colorPieChart7))
            } else {
                color = ColorTemplate.createColors(resources,
                        intArrayOf(R.color.colorPieChart1, R.color.colorPieChart2, R.color.colorPieChart3, R.color.colorPieChart4, R.color.colorPieChart5, R.color.colorPieChart6))
            }
            dataSet.colors = color

            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter())
            data.setValueTextSize(10f)
            data.setValueTextColor(Color.WHITE)

            mBinding.pieChart.data = data
        }
        // undo all highlights
        mBinding.pieChart.highlightValues(null)
        mBinding.pieChart.invalidate()
        mBinding.pieChart.animateY(1000)
    }

    /**
     * 设置月份
     */
    fun setYearMonth(year: Int, month: Int) {
        if (year == mYear && month == mMonth) {
            return
        }
        mYear = year
        mMonth = month
        // 更新数据
        getTypeSumMoney()
        getMonthSumMoney()
    }

    override fun lazyInitData() {
        mBinding.layoutSumMoney?.rgType?.check(R.id.rb_outlay)
    }

    private fun getMonthSumMoney() {
        mDisposable.add(mViewModel.getMonthSumMoney(mYear, mMonth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    // 这种方式被误报红
                    // mBinding.layoutSumMoney?.sumMoneyBeanList = it
                    mBinding.layoutSumMoney?.setVariable(BR.sumMoneyBeanList, it)
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_get_month_summary_fail)
                    Log.e(TAG, "获取该月汇总数据失败", throwable)
                })
    }

    private fun getTypeSumMoney() {
        mDisposable.add(mViewModel.getTypeSumMoney(mYear, mMonth, mType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ typeSumMoneyBeans ->
                    setChartData(typeSumMoneyBeans)
                    mAdapter.setNewData(typeSumMoneyBeans)
                    if (typeSumMoneyBeans.isEmpty()) {
                        mAdapter.emptyView = inflate(R.layout.layout_statistics_empty)
                    }
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_get_type_summary_fail)
                    Log.e(TAG, "获取类型汇总数据失败", throwable)
                })
    }

    companion object {

        private val TAG = ReportsFragment::class.java.simpleName
    }
}
