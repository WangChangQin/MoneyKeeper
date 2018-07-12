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

import io.reactivex.Completable
import io.reactivex.Flowable
import me.bakumon.moneykeeper.database.entity.*
import me.bakumon.moneykeeper.ui.addtype.TypeImgBean
import java.util.*

/**
 * 数据源
 *
 * @author Bakumon https://bakumon.me
 */
interface AppDataSource {

    /**
     * 初始化默认的记账类型
     */
    fun initRecordTypes(): Completable

    /**
     * 添加记账类型
     *
     * @param type    类型
     * @param imgName 图片
     * @param name    类型名称
     * @see RecordType.TYPE_OUTLAY
     *
     * @see RecordType.TYPE_INCOME
     */
    fun addRecordType(type: Int, imgName: String, name: String): Completable

    /**
     * 修改记账类型
     *
     * @param oldRecordType 修改之前的 RecordType
     * @param recordType    修改的 RecordType
     */
    fun updateRecordType(oldRecordType: RecordType, recordType: RecordType): Completable

    /**
     * 删除记账类型
     *
     * @param recordType 要删除的记账类型对象
     */
    fun deleteRecordType(recordType: RecordType): Completable

    /**
     * 获取所有记账类型数据
     *
     * @return 所有记账类型数据
     */
    fun getAllRecordType(): Flowable<List<RecordType>>

    /**
     * 获取指出或收入记账类型数据
     *
     * @param type 类型
     * @return 记账类型数据
     * @see RecordType.TYPE_OUTLAY
     *
     * @see RecordType.TYPE_INCOME
     */
    fun getRecordTypes(type: Int): Flowable<List<RecordType>>

    /**
     * 记账类型排序
     *
     * @param recordTypes 记账类型对象
     */
    fun sortRecordTypes(recordTypes: List<RecordType>): Completable

    /**
     * 获取类型图片数据
     *
     * @param type 收入或支出类型
     * @return 所有获取类型图片数据
     * @see RecordType.TYPE_OUTLAY
     *
     * @see RecordType.TYPE_INCOME
     */
    fun getAllTypeImgBeans(type: Int): Flowable<List<TypeImgBean>>

    /**
     * 新增一条记账记录
     *
     * @param record 记账记录实体
     */
    fun insertRecord(record: Record): Completable

    /**
     * 更新一条记账记录
     *
     * @param record 记录对象
     */
    fun updateRecord(record: Record): Completable

    /**
     * 删除一天记账记录
     *
     * @param record 要删除的记账记录
     */
    fun deleteRecord(record: Record): Completable

    /**
     * 获取当前月份的记账记录数据
     *
     * @return 当前月份的记录数据的 Flowable 对象
     */
    fun getCurrentMonthRecordWithTypes(): Flowable<List<RecordWithType>>

    /**
     * 根据类型获取某段时间的记账记录数据
     *
     * @return 包含记录数据的 Flowable 对象
     */
    fun getRecordWithTypes(dateFrom: Date, dateTo: Date, type: Int): Flowable<List<RecordWithType>>

    /**
     * 获取某一类型某段时间的记账记录数据
     *
     * @return 包含记录数据的 Flowable 对象
     */
    fun getRecordWithTypes(dateFrom: Date, dateTo: Date, type: Int, typeId: Int): Flowable<List<RecordWithType>>

    /**
     * 获取某一类型某段时间的记账记录数据，money 排序
     *
     * @return 包含记录数据的 Flowable 对象
     */
    fun getRecordWithTypesSortMoney(dateFrom: Date, dateTo: Date, type: Int, typeId: Int): Flowable<List<RecordWithType>>

    /**
     * 获取本月支出和收入总数
     */
    fun getCurrentMonthSumMoney(): Flowable<List<SumMoneyBean>>

    /**
     * 获取某月支出和收入总数
     */
    fun getMonthSumMoney(dateFrom: Date, dateTo: Date): Flowable<List<SumMoneyBean>>

    /**
     * 获取某天的合计
     *
     * @param year  年
     * @param month 月
     * @param type  类型
     */
    fun getDaySumMoney(year: Int, month: Int, type: Int): Flowable<List<DaySumMoneyBean>>

    /**
     * 获取按类型汇总数据
     */
    fun getTypeSumMoney(from: Date, to: Date, type: Int): Flowable<List<TypeSumMoneyBean>>

    /**
     * 获取某年（或某段时间）内所有月份的收支总数
     */
    fun getMonthOfYearSumMoney(from: Date, to: Date): Flowable<List<MonthSumMoneyBean>>
}
