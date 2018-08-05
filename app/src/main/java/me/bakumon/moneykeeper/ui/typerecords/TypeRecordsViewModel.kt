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

package me.bakumon.moneykeeper.ui.typerecords

import io.reactivex.Completable
import io.reactivex.Flowable
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.ui.common.BaseViewModel
import me.bakumon.moneykeeper.utill.DateUtils

/**
 * 某一类型的记账记录
 *
 * @author Bakumon https://bakumon.me
 */
class TypeRecordsViewModel(dataSource: AppDataSource) : BaseViewModel(dataSource) {

    fun getRecordWithTypes(sortType: Int, type: Int, typeId: Int, year: Int, month: Int): Flowable<List<RecordWithType>> {
        val dateFrom = DateUtils.getMonthStart(year, month)
        val dateTo = DateUtils.getMonthEnd(year, month)
        return if (sortType == 0) {
            mDataSource.getRecordWithTypes(dateFrom, dateTo, type, typeId)
        } else {
            mDataSource.getRecordWithTypesSortMoney(dateFrom, dateTo, type, typeId)
        }
    }

    fun deleteRecord(record: RecordWithType): Completable {
        val oldType = record.mRecordTypes!![0].type
        if (oldType == RecordType.TYPE_OUTLAY) {
            ConfigManager.addAssets(record.money!!)
        } else {
            ConfigManager.reduceAssets(record.money!!)
        }
        return mDataSource.deleteRecord(record)
    }

}
