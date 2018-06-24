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

package me.bakumon.moneykeeper.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.Update

import java.util.Date

import io.reactivex.Flowable
import me.bakumon.moneykeeper.database.entity.DaySumMoneyBean
import me.bakumon.moneykeeper.database.entity.Record
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.database.entity.SumMoneyBean
import me.bakumon.moneykeeper.database.entity.TypeSumMoneyBean

/**
 * 记账记录表操作类
 *
 * @author Bakumon https://bakumon.me
 */
@Dao
interface RecordDao {

    @Transaction
    @Query("SELECT * from Record WHERE time BETWEEN :from AND :to ORDER BY time DESC, create_time DESC")
    fun getRangeRecordWithTypes(from: Date, to: Date): Flowable<List<RecordWithType>>

    @Transaction
    @Query("SELECT Record.* from Record LEFT JOIN RecordType ON Record.record_type_id=RecordType.id WHERE (RecordType.type=:type AND time BETWEEN :from AND :to) ORDER BY time DESC, create_time DESC")
    fun getRangeRecordWithTypes(from: Date, to: Date, type: Int): Flowable<List<RecordWithType>>

    @Transaction
    @Query("SELECT Record.* from Record LEFT JOIN RecordType ON Record.record_type_id=RecordType.id WHERE (RecordType.type=:type AND Record.record_type_id=:typeId AND time BETWEEN :from AND :to) ORDER BY time DESC, create_time DESC")
    fun getRangeRecordWithTypes(from: Date, to: Date, type: Int, typeId: Int): Flowable<List<RecordWithType>>

    @Transaction
    @Query("SELECT Record.* from Record LEFT JOIN RecordType ON Record.record_type_id=RecordType.id WHERE (RecordType.type=:type AND Record.record_type_id=:typeId AND time BETWEEN :from AND :to) ORDER BY money DESC, create_time DESC")
    fun getRecordWithTypesSortMoney(from: Date, to: Date, type: Int, typeId: Int): Flowable<List<RecordWithType>>

    @Insert
    fun insertRecord(record: Record)

    @Update
    fun updateRecords(vararg records: Record)

    @Delete
    fun deleteRecord(record: Record)

    @Query("SELECT RecordType.type AS type, sum(Record.money) AS sumMoney FROM Record LEFT JOIN RecordType ON Record.record_type_id=RecordType.id WHERE time BETWEEN :from AND :to GROUP BY RecordType.type")
    fun getSumMoney(from: Date, to: Date): Flowable<List<SumMoneyBean>>

    @Query("SELECT count(id) FROM Record WHERE record_type_id = :typeId")
    fun getRecordCountWithTypeId(typeId: Int): Long

    @Query("SELECT * FROM Record WHERE record_type_id = :typeId")
    fun getRecordsWithTypeId(typeId: Int): List<Record>

    /**
     * 尽量使用 Flowable 返回，因为当数据库数据改变时，会自动回调
     * 而直接用 List ，在调用的地方自己写 Flowable 不会自动回调
     */
    @Query("SELECT RecordType.type AS type, Record.time AS time, sum(Record.money) AS daySumMoney FROM Record LEFT JOIN RecordType ON Record.record_type_id=RecordType.id where (RecordType.type=:type and Record.time BETWEEN :from AND :to) GROUP BY Record.time")
    fun getDaySumMoney(from: Date, to: Date, type: Int): Flowable<List<DaySumMoneyBean>>

    @Query("SELECT t_type.img_name AS imgName,t_type.name AS typeName, Record.record_type_id AS typeId,sum(Record.money) AS typeSumMoney, count(Record.record_type_id) AS count FROM Record LEFT JOIN RecordType AS t_type ON Record.record_type_id=t_type.id where (t_type.type=:type and Record.time BETWEEN :from AND :to) GROUP by Record.record_type_id Order by sum(Record.money) DESC")
    fun getTypeSumMoney(from: Date, to: Date, type: Int): Flowable<List<TypeSumMoneyBean>>
}
