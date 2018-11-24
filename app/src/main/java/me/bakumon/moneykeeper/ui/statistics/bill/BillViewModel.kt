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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.base.Resource
import me.bakumon.moneykeeper.database.entity.DaySumMoneyBean
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.database.entity.SumMoneyBean
import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.ui.common.BaseViewModel
import me.bakumon.moneykeeper.utill.DateUtils

/**
 * 统计-账单
 *
 * @author Bakumon https://bakumon.me
 */
class BillViewModel(dataSource: AppDataSource) : BaseViewModel(dataSource) {

    fun getRecordWithTypes(year: Int, month: Int, type: Int): LiveData<List<RecordWithType>> {
        val dateFrom = DateUtils.getMonthStart(year, month)
        val dateTo = DateUtils.getMonthEnd(year, month)
        return mDataSource.getRecordWithTypes(dateFrom, dateTo, type)
    }

    fun getDaySumMoney(year: Int, month: Int, type: Int): LiveData<List<DaySumMoneyBean>> {
        return mDataSource.getDaySumMoney(year, month, type)
    }

    fun getMonthSumMoney(year: Int, month: Int): LiveData<List<SumMoneyBean>> {
        val dateFrom = DateUtils.getMonthStart(year, month)
        val dateTo = DateUtils.getMonthEnd(year, month)
        return mDataSource.getMonthSumMoneyLiveData(dateFrom, dateTo)
    }

    fun deleteRecord(record: RecordWithType): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        mDisposable.add(mDataSource.deleteRecord(record)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveData.value = Resource.create(true)
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
        return liveData
    }
}
