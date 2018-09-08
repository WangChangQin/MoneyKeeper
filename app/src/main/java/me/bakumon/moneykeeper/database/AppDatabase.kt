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

package me.bakumon.moneykeeper.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.database.converters.Converters
import me.bakumon.moneykeeper.database.dao.AssetsDao
import me.bakumon.moneykeeper.database.dao.AssetsModifyRecordDao
import me.bakumon.moneykeeper.database.dao.RecordDao
import me.bakumon.moneykeeper.database.dao.RecordTypeDao
import me.bakumon.moneykeeper.database.entity.Assets
import me.bakumon.moneykeeper.database.entity.AssetsModifyRecord
import me.bakumon.moneykeeper.database.entity.Record
import me.bakumon.moneykeeper.database.entity.RecordType


/**
 * 数据库
 *
 * @author Bakumon https:bakumon.me
 */
@Database(entities = [Record::class, RecordType::class, Assets::class, AssetsModifyRecord::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取记账类型操作类
     *
     * @return RecordTypeDao 记账类型操作类
     */
    abstract fun recordTypeDao(): RecordTypeDao

    /**
     * 获取记账操作类
     *
     * @return RecordDao 记账操作类
     */
    abstract fun recordDao(): RecordDao

    abstract fun assetsDao(): AssetsDao

    abstract fun assetsModifyRecordDao(): AssetsModifyRecordDao

    companion object {
        const val DB_NAME = "MoneyKeeper.db"
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `Assets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL, `img_name` TEXT NOT NULL, `type` INTEGER NOT NULL, `state` INTEGER NOT NULL, `remark` TEXT NOT NULL, `create_time` INTEGER NOT NULL, `money` INTEGER NOT NULL)")
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val instance: AppDatabase?
            get() {
                if (INSTANCE == null) {
                    synchronized(AppDatabase::class) {
                        if (INSTANCE == null) {
                            INSTANCE = Room.databaseBuilder(App.instance, AppDatabase::class.java, DB_NAME)
                                    .addMigrations(MIGRATION_1_2)
                                    .build()
                        }
                    }
                }
                return INSTANCE
            }
    }
}
