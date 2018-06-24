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

package me.bakumon.moneykeeper.datasource

import android.text.TextUtils
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.AppDatabase
import me.bakumon.moneykeeper.database.entity.*
import me.bakumon.moneykeeper.ui.addtype.TypeImgBean
import me.bakumon.moneykeeper.utill.BackupUtil
import me.bakumon.moneykeeper.utill.DateUtils
import java.util.*

/**
 * 数据源本地实现类
 *
 * @author Bakumon https://bakumon.me
 */
class LocalAppDataSource(private val mAppDatabase: AppDatabase) : AppDataSource {

    /**
     * 自动备份
     */
    @Throws(Exception::class)
    private fun autoBackup() {
        if (ConfigManager.isAutoBackup) {
            val isSuccess = BackupUtil.autoBackup()
            if (!isSuccess) {
                throw BackupFailException()
            }
        }
    }

    /**
     * 自动备份 for first
     */
    @Throws(Exception::class)
    private fun autoBackupForNecessary() {
        if (ConfigManager.isAutoBackup) {
            val isSuccess = BackupUtil.autoBackupForNecessary()
            if (!isSuccess) {
                throw BackupFailException()
            }
        }
    }

    override fun initRecordTypes(): Completable {
        return Completable.fromAction {
            if (mAppDatabase.recordTypeDao().getRecordTypeCount() < 1) {
                // 没有记账类型数据记录，插入默认的数据类型
                mAppDatabase.recordTypeDao().insertRecordTypes(*RecordTypeInitCreator.createRecordTypeData())
                autoBackupForNecessary()
            }
        }
    }

    override fun addRecordType(type: Int, imgName: String, name: String): Completable {
        return Completable.fromAction {
            val recordType = mAppDatabase.recordTypeDao().getTypeByName(type, name)
            if (recordType != null) {
                // name 类型存在
                if (recordType.state == RecordType.STATE_DELETED) {
                    // 已删除状态
                    recordType.state = RecordType.STATE_NORMAL
                    recordType.ranking = System.currentTimeMillis()
                    recordType.imgName = imgName
                    mAppDatabase.recordTypeDao().updateRecordTypes(recordType)
                } else {
                    // 提示用户该类型已经存在
                    throw IllegalStateException(name + App.instance.getString(R.string.toast_type_is_exist))
                }
            } else {
                // 不存在，直接新增
                val insertType = RecordType(name, imgName, type, System.currentTimeMillis())
                mAppDatabase.recordTypeDao().insertRecordTypes(insertType)
            }
            autoBackup()
        }
    }

    override fun updateRecordType(oldRecordType: RecordType, recordType: RecordType): Completable {
        return Completable.fromAction {
            val oldName = oldRecordType.name
            val oldImgName = oldRecordType.imgName
            if (!TextUtils.equals(oldName, recordType.name)) {
                val recordTypeFromDb = mAppDatabase.recordTypeDao().getTypeByName(recordType.type, recordType.name!!)
                if (recordTypeFromDb != null) {
                    if (recordTypeFromDb.state == RecordType.STATE_DELETED) {

                        // 1。recordTypeFromDb 改成正常状态，name改成recordType#name，imageName同理
                        // 2。更新 recordTypeFromDb
                        // 3。判断是否有 oldRecordType 类型的 record 记录
                        // 4。如果有记录，把这些记录的 type_id 改成 recordTypeFromDb.id
                        // 5。删除 oldRecordType 记录

                        recordTypeFromDb.state = RecordType.STATE_NORMAL
                        recordTypeFromDb.name = recordType.name
                        recordTypeFromDb.imgName = recordType.imgName
                        recordTypeFromDb.ranking = System.currentTimeMillis()

                        mAppDatabase.recordTypeDao().updateRecordTypes(recordTypeFromDb)

                        val recordsWithOldType = mAppDatabase.recordDao().getRecordsWithTypeId(oldRecordType.id)
                        if (recordsWithOldType != null && recordsWithOldType.isNotEmpty()) {
                            for (record in recordsWithOldType) {
                                record.recordTypeId = recordTypeFromDb.id
                            }
                            mAppDatabase.recordDao().updateRecords(*recordsWithOldType.toTypedArray())
                        }

                        mAppDatabase.recordTypeDao().deleteRecordType(oldRecordType)
                    } else {
                        // 提示用户该类型已经存在
                        throw IllegalStateException(recordType.name + App.instance.getString(R.string.toast_type_is_exist))
                    }
                } else {
                    mAppDatabase.recordTypeDao().updateRecordTypes(recordType)
                }
            } else if (!TextUtils.equals(oldImgName, recordType.imgName)) {
                mAppDatabase.recordTypeDao().updateRecordTypes(recordType)
            }
            autoBackup()
        }
    }

    override fun deleteRecordType(recordType: RecordType): Completable {
        return Completable.fromAction {
            if (mAppDatabase.recordDao().getRecordCountWithTypeId(recordType.id) > 0) {
                recordType.state = RecordType.STATE_DELETED
                mAppDatabase.recordTypeDao().updateRecordTypes(recordType)
            } else {
                mAppDatabase.recordTypeDao().deleteRecordType(recordType)
            }
            autoBackup()
        }
    }

    override fun getAllRecordType(): Flowable<List<RecordType>> {
        return mAppDatabase.recordTypeDao().getAllRecordTypes()
    }

    override fun getRecordTypes(type: Int): Flowable<List<RecordType>> {
        return mAppDatabase.recordTypeDao().getRecordTypes(type)
    }

    override fun sortRecordTypes(recordTypes: List<RecordType>): Completable {
        return Completable.fromAction {
            if (recordTypes.size > 1) {
                val sortTypes = ArrayList<RecordType>()
                for (i in recordTypes.indices) {
                    val type = recordTypes[i]
                    if (type.ranking != i.toLong()) {
                        type.ranking = i.toLong()
                        sortTypes.add(type)
                    }
                }
                mAppDatabase.recordTypeDao().updateRecordTypes(*sortTypes.toTypedArray())
                autoBackup()
            }
        }
    }

    override fun getAllTypeImgBeans(type: Int): Flowable<List<TypeImgBean>> {
        return Flowable.create({ e ->
            val beans = TypeImgListCreator.createTypeImgBeanData(type)
            e.onNext(beans)
            e.onComplete()
        }, BackpressureStrategy.BUFFER)
    }

    override fun insertRecord(record: Record): Completable {
        return Completable.fromAction {
            mAppDatabase.recordDao().insertRecord(record)
            autoBackup()
        }
    }

    override fun updateRecord(record: Record): Completable {
        return Completable.fromAction {
            mAppDatabase.recordDao().updateRecords(record)
            autoBackup()
        }
    }

    override fun deleteRecord(record: Record): Completable {
        return Completable.fromAction {
            mAppDatabase.recordDao().deleteRecord(record)
            autoBackup()
        }
    }

    override fun getCurrentMonthRecordWithTypes(): Flowable<List<RecordWithType>> {
        val dateFrom = DateUtils.getCurrentMonthStart()
        val dateTo = DateUtils.getCurrentMonthEnd()
        return mAppDatabase.recordDao().getRangeRecordWithTypes(dateFrom, dateTo)
    }

    override fun getRecordWithTypes(dateFrom: Date, dateTo: Date, type: Int): Flowable<List<RecordWithType>> {
        return mAppDatabase.recordDao().getRangeRecordWithTypes(dateFrom, dateTo, type)
    }

    override fun getRecordWithTypes(dateFrom: Date, dateTo: Date, type: Int, typeId: Int): Flowable<List<RecordWithType>> {
        return mAppDatabase.recordDao().getRangeRecordWithTypes(dateFrom, dateTo, type, typeId)
    }

    override fun getRecordWithTypesSortMoney(dateFrom: Date, dateTo: Date, type: Int, typeId: Int): Flowable<List<RecordWithType>> {
        return mAppDatabase.recordDao().getRecordWithTypesSortMoney(dateFrom, dateTo, type, typeId)
    }

    override fun getCurrentMonthSumMoney(): Flowable<List<SumMoneyBean>> {
        val dateFrom = DateUtils.getCurrentMonthStart()
        val dateTo = DateUtils.getCurrentMonthEnd()
        return mAppDatabase.recordDao().getSumMoney(dateFrom, dateTo)
    }

    override fun getMonthSumMoney(dateFrom: Date, dateTo: Date): Flowable<List<SumMoneyBean>> {
        return mAppDatabase.recordDao().getSumMoney(dateFrom, dateTo)
    }

    override fun getDaySumMoney(year: Int, month: Int, type: Int): Flowable<List<DaySumMoneyBean>> {
        val dateFrom = DateUtils.getMonthStart(year, month)
        val dateTo = DateUtils.getMonthEnd(year, month)
        return mAppDatabase.recordDao().getDaySumMoney(dateFrom, dateTo, type)
    }

    override fun getTypeSumMoney(from: Date, to: Date, type: Int): Flowable<List<TypeSumMoneyBean>> {
        return mAppDatabase.recordDao().getTypeSumMoney(from, to, type)
    }
}
