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

package me.bakumon.moneykeeper.ui.statistics.bill

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.android.databinding.library.baseAdapters.BR
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseFragment
import me.bakumon.moneykeeper.database.entity.DaySumMoneyBean
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.databinding.FragmentBillBinding
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.home.HomeAdapter
import me.bakumon.moneykeeper.utill.DateUtils
import me.bakumon.moneykeeper.utill.ToastUtils
import me.bakumon.moneykeeper.view.BarChartMarkerView
import me.drakeet.floo.Floo
import java.util.*

/**
 * 统计-账单
 *
 * @author Bakumon https://bakumon.me
 */
class BillFragment : BaseFragment() {
    private lateinit var mBinding: FragmentBillBinding
    private lateinit var mViewModel: BillViewModel
    private lateinit var mAdapter: HomeAdapter

    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mType: Int = 0

    override val layoutId: Int
        get() = R.layout.fragment_bill

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(BillViewModel::class.java)

        mYear = DateUtils.getCurrentYear()
        mMonth = DateUtils.getCurrentMonth()
        mType = RecordType.TYPE_OUTLAY

        initView()
    }

    private fun initView() {
        mBinding.rvRecordBill.layoutManager = LinearLayoutManager(context)
        mAdapter = HomeAdapter(null)
        mBinding.rvRecordBill.adapter = mAdapter
        mAdapter.setOnItemChildLongClickListener { _, _, position ->
            showOperateDialog(mAdapter.data[position])
            false
        }

        initBarChart()

        mBinding.layoutSumMoney?.rgType?.setOnCheckedChangeListener { _, checkedId ->
            mType = if (checkedId == R.id.rb_outlay) {
                RecordType.TYPE_OUTLAY
            } else {
                RecordType.TYPE_INCOME
            }
            getOrderData()
            getDaySumData()
            getMonthSumMoney()
        }
    }

    private fun initBarChart() {
        mBinding.barChart.setNoDataText("")
        mBinding.barChart.setScaleEnabled(false)
        mBinding.barChart.description.isEnabled = false
        mBinding.barChart.legend.isEnabled = false

        mBinding.barChart.axisLeft.axisMinimum = 0f
        mBinding.barChart.axisLeft.isEnabled = false
        mBinding.barChart.axisRight.isEnabled = false
        val xAxis = mBinding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = resources.getColor(R.color.colorTextGray)
        xAxis.labelCount = 5

        val mv = BarChartMarkerView(context!!)
        mv.chartView = mBinding.barChart
        mBinding.barChart.marker = mv
    }

    private fun showOperateDialog(record: RecordWithType) {
        if (context == null) {
            return
        }
        AlertDialog.Builder(context!!)
                .setItems(arrayOf(getString(R.string.text_modify), getString(R.string.text_delete))) { _, which ->
                    if (which == 0) {
                        modifyRecord(record)
                    } else {
                        deleteRecord(record)
                    }
                }
                .create()
                .show()
    }

    private fun modifyRecord(record: RecordWithType) {
        if (context == null) {
            return
        }
        Floo.navigation(context!!, Router.Url.URL_ADD_RECORD)
                .putExtra(Router.ExtraKey.KEY_RECORD_BEAN, record)
                .start()
    }

    private fun deleteRecord(record: RecordWithType) {
        mDisposable.add(mViewModel.deleteRecord(record)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }
                ) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（删除记账记录失败的时候）", throwable)
                    } else {
                        ToastUtils.show(R.string.toast_record_delete_fail)
                        Log.e(TAG, "删除记账记录失败", throwable)
                    }
                })
    }

    private fun setChartData(daySumMoneyBeans: List<DaySumMoneyBean>?) {
        if (daySumMoneyBeans == null || daySumMoneyBeans.isEmpty()) {
            mBinding.barChart.visibility = View.INVISIBLE
            return
        } else {
            mBinding.barChart.visibility = View.VISIBLE
        }

        val count = DateUtils.getDayCount(mYear, mMonth)
        val barEntries = BarEntryConverter.getBarEntryList(count, daySumMoneyBeans)

        val set1: BarDataSet
        if (mBinding.barChart.data != null && mBinding.barChart.data.dataSetCount > 0) {
            set1 = mBinding.barChart.data.getDataSetByIndex(0) as BarDataSet
            set1.values = barEntries
            mBinding.barChart.data.notifyDataChanged()
            mBinding.barChart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(barEntries, "")
            set1.setDrawIcons(false)
            set1.setDrawValues(false)
            set1.color = resources.getColor(R.color.colorAccent)
            set1.valueTextColor = resources.getColor(R.color.colorTextWhite)

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.barWidth = 0.5f
            data.isHighlightEnabled = true
            mBinding.barChart.data = data
        }
        mBinding.barChart.invalidate()
        mBinding.barChart.animateY(1000)
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
        getOrderData()
        getDaySumData()
        getMonthSumMoney()
    }

    override fun lazyInitData() {
        mBinding.layoutSumMoney?.rgType?.check(R.id.rb_outlay)
    }

    private fun getOrderData() {
        mDisposable.add(mViewModel.getRecordWithTypes(mYear, mMonth, mType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recordWithTypes ->
                    mAdapter.setNewData(recordWithTypes)
                    if (recordWithTypes == null || recordWithTypes.isEmpty()) {
                        mAdapter.emptyView = inflate(R.layout.layout_statistics_empty)
                    }
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_records_fail)
                    Log.e(TAG, "获取记录列表失败", throwable)
                })
    }

    private fun getDaySumData() {
        mDisposable.add(mViewModel.getDaySumMoney(mYear, mMonth, mType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.setChartData(it) }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_get_statistics_fail)
                    Log.e(TAG, "获取统计数据失败", throwable)
                })
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

    companion object {
        private val TAG = BillFragment::class.java.simpleName
    }
}
