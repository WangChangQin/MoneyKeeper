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

package me.bakumon.moneykeeper.ui.add

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.base.Resource
import me.bakumon.moneykeeper.database.entity.Assets
import me.bakumon.moneykeeper.database.entity.Record
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.ui.common.BaseViewModel

/**
 * 记一笔界面 ViewModel
 *
 * @author Bakumon https://bakumon.me
 */
class AddRecordViewModel(dataSource: AppDataSource) : BaseViewModel(dataSource) {

    val allRecordTypes: LiveData<List<RecordType>>
        get() = mDataSource.getAllRecordType()

    fun insertRecord(type: Int, assets: Assets?, record: Record): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()

        record.assetsId = if (assets == null) -1 else assets.id!!
        mDisposable.add(mDataSource.insertRecord(record)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (assets == null) {
                        liveData.value = Resource.create(true)
                    } else {
                        updateAssetsForInsert(liveData, type, assets, record)
                    }
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
        return liveData
    }

    private fun updateAssetsForInsert(liveData: MutableLiveData<Resource<Boolean>>, type: Int, assets: Assets, record: Record) {
        if (type == RecordType.TYPE_OUTLAY) {
            assets.money = assets.money.subtract(record.money)
        } else {
            assets.money = assets.money.add(record.money)
        }
        mDisposable.add(mDataSource.updateAssets(assets)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveData.value = Resource.create(true)
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
    }

    fun updateRecord(oldType: Int, type: Int, oldAssets: Assets?, assets: Assets?, record: Record): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()

        record.assetsId = if (assets == null) -1 else assets.id!!
        mDisposable.add(mDataSource.updateRecord(record)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateAssetsForModify(liveData, oldType, type, oldAssets, assets, record)
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
        return liveData
    }

    private fun updateAssetsForModify(liveData: MutableLiveData<Resource<Boolean>>, oldType: Int, type: Int, oldAssets: Assets?, assets: Assets?, record: Record) {
        // 太灾难了
        if (oldType == type) {
            if (oldAssets == null) {
                if (assets == null) {
                    // 不用更新资产
                    liveData.value = Resource.create(true)
                } else {
                    if (type == RecordType.TYPE_OUTLAY) {
                        // 更新 assets，减
                        assets.money = assets.money.subtract(record.money)
                        updateAssets(assets = assets, liveData = liveData)
                    } else {
                        assets.money = assets.money.add(record.money)
                        updateAssets(assets = assets, liveData = liveData)
                    }
                }
            } else {
                if (assets == null) {
                    if (type == RecordType.TYPE_OUTLAY) {
                        // 更新 oldAssets，加
                        oldAssets.money = oldAssets.money.add(record.money)
                        updateAssets(assets = oldAssets, liveData = liveData)
                    } else {
                        oldAssets.money = oldAssets.money.subtract(record.money)
                        updateAssets(assets = oldAssets, liveData = liveData)
                    }
                } else {
                    if (oldAssets.id == assets.id) {
                        // 不用更新资产
                        liveData.value = Resource.create(true)
                    } else {
                        if (type == RecordType.TYPE_OUTLAY) {
                            oldAssets.money = oldAssets.money.add(record.money)
                            assets.money = assets.money.subtract(record.money)
                            updateAssets(assets = oldAssets, otherAssets = assets, liveData = liveData)
                        } else {
                            oldAssets.money = oldAssets.money.subtract(record.money)
                            assets.money = assets.money.add(record.money)
                            updateAssets(assets = oldAssets, otherAssets = assets, liveData = liveData)
                        }
                    }
                }
            }
        } else {
            if (oldAssets == null) {
                if (assets == null) {
                    // 不用更新资产
                    liveData.value = Resource.create(true)
                } else {
                    if (type == RecordType.TYPE_OUTLAY) {
                        assets.money = assets.money.subtract(record.money)
                        updateAssets(assets = assets, liveData = liveData)
                    } else {
                        assets.money = assets.money.add(record.money)
                        updateAssets(assets = assets, liveData = liveData)
                    }
                }
            } else {
                if (assets == null) {
                    if (oldType == RecordType.TYPE_OUTLAY) {
                        oldAssets.money = oldAssets.money.add(record.money)
                        updateAssets(assets = oldAssets, liveData = liveData)
                    } else {
                        oldAssets.money = oldAssets.money.subtract(record.money)
                        updateAssets(assets = oldAssets, liveData = liveData)
                    }
                } else {
                    if (type == RecordType.TYPE_OUTLAY) {
                        // oldType==RecordType.TYPE_INCOME
                        oldAssets.money = oldAssets.money.subtract(record.money)
                        assets.money = assets.money.subtract(record.money)
                        updateAssets(assets = oldAssets, otherAssets = assets, liveData = liveData)
                    } else {
                        oldAssets.money = oldAssets.money.add(record.money)
                        assets.money = assets.money.add(record.money)
                        updateAssets(assets = oldAssets, otherAssets = assets, liveData = liveData)
                    }
                }
            }
        }
    }

    private fun updateAssets(assets: Assets, otherAssets: Assets? = null, liveData: MutableLiveData<Resource<Boolean>>) {
        mDisposable.add(mDataSource.updateAssets(assets)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (otherAssets == null) {
                        liveData.value = Resource.create(true)
                    } else {
                        updateAssets(assets = otherAssets, liveData = liveData)
                    }
                }
                ) { throwable ->
                    liveData.value = Resource.create(throwable)
                })
    }

    fun getAssets(): LiveData<List<Assets>> {
        return mDataSource.getAssets()
    }

    fun getAssetsById(id: Int): LiveData<Assets> {
        return mDataSource.getAssetsById(id)
    }
}
