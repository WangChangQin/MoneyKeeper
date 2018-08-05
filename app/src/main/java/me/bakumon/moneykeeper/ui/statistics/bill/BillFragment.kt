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

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_bill.*
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.common.BaseFragment
import me.bakumon.moneykeeper.ui.common.Empty
import me.bakumon.moneykeeper.ui.common.EmptyViewBinder
import me.bakumon.moneykeeper.ui.home.RecordViewBinder
import me.bakumon.moneykeeper.utill.DateUtils
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register

/**
 * 统计-账单
 *
 * @author Bakumon https://bakumon.me
 */
class BillFragment : BaseFragment() {
    private lateinit var mViewModel: BillViewModel
    private lateinit var adapter: MultiTypeAdapter
    private var mYear: Int = DateUtils.getCurrentYear()
    private var mMonth: Int = DateUtils.getCurrentMonth()
    private var mType: Int = RecordType.TYPE_OUTLAY

    override val layoutId: Int
        get() = R.layout.fragment_bill

    override fun onInit(savedInstanceState: Bundle?) {
        mViewModel = getViewModel()
        // 设置 MultiTypeAdapter
        adapter = MultiTypeAdapter()
        adapter.register(RecordWithType::class, RecordViewBinder({ deleteRecord(it) }))
        adapter.register(Empty::class, EmptyViewBinder())
        rvRecordBill.adapter = adapter

        sumMoneyChooseView.setOnCheckedChangeListener({
            mType = it
            updateData()
        })
    }

    override fun lazyInitData() {
        sumMoneyChooseView.checkItem(mType)
    }

    private fun updateData() {
        getOrderData()
        getDaySumData()
        getMonthSumMoney()
    }

    /**
     * 设置月份
     * 父 activity 调用
     */
    fun setYearMonth(year: Int, month: Int) {
        if (year == mYear && month == mMonth) {
            return
        }
        mYear = year
        mMonth = month
        // 更新数据
        updateData()
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


    private fun getOrderData() {
        mDisposable.add(mViewModel.getRecordWithTypes(mYear, mMonth, mType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setItems(it)
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_records_fail)
                    Log.e(TAG, "获取记录列表失败", throwable)
                })
    }

    private fun setItems(recordWithTypes: List<RecordWithType>) {
        val items = Items()
        if (recordWithTypes.isEmpty()) {
            items.add(Empty(getString(R.string.text_empty_tip), Gravity.CENTER_HORIZONTAL))
        } else {
            items.addAll(recordWithTypes)
        }
        adapter.items = items
        adapter.notifyDataSetChanged()
    }

    private fun getDaySumData() {
        mDisposable.add(mViewModel.getDaySumMoney(mYear, mMonth, mType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    barChart.setChartData(it, mYear, mMonth)
                }
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
                    sumMoneyChooseView.setSumMoneyBean(it)
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
