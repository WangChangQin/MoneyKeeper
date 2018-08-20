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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.base.Resource
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.database.entity.SumMoneyBean
import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.ui.common.BaseViewModel
import me.bakumon.moneykeeper.utill.EncryptUtil

/**
 * 主页 ViewModel
 *
 * @author Bakumon https://bakumon.me
 */
class HomeViewModel(dataSource: AppDataSource) : BaseViewModel(dataSource) {

    private lateinit var pswLiveData: MutableLiveData<Resource<String>>

    fun getPsw(): LiveData<Resource<String>> {
        pswLiveData = MutableLiveData()
        getClearPsw()
        return pswLiveData
    }

    private fun getClearPsw() {
        val key = EncryptUtil.key
        val salt = EncryptUtil.salt

        mDisposable.add(Flowable.create(FlowableOnSubscribe<String> {
            val displayPsw = ConfigManager.webDavEncryptPsw
            val psw = if (displayPsw.isEmpty()) "" else EncryptUtil.decrypt(displayPsw, key, salt)
            it.onNext(psw)
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    pswLiveData.value = Resource.create(it)
                })
                { throwable ->
                    pswLiveData.value = Resource.create(throwable)
                }
        )
    }

    val currentMonthRecordWithTypes: LiveData<List<RecordWithType>>
        get() = mDataSource.getCurrentMonthRecordWithTypes()

    val currentMonthSumMoney: LiveData<List<SumMoneyBean>>
        get() = mDataSource.getCurrentMonthSumMoneyLiveData()

    fun initRecordTypes(): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        mDisposable.add(mDataSource.initRecordTypes()
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

    fun deleteRecord(record: RecordWithType): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        val oldType = record.mRecordTypes!![0].type
        if (oldType == RecordType.TYPE_OUTLAY) {
            ConfigManager.addAssets(record.money!!)
        } else {
            ConfigManager.reduceAssets(record.money!!)
        }
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
