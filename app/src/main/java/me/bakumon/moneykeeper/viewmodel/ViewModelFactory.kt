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

package me.bakumon.moneykeeper.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.ui.add.AddRecordViewModel
import me.bakumon.moneykeeper.ui.addtype.AddTypeViewModel
import me.bakumon.moneykeeper.ui.home.HomeViewModel
import me.bakumon.moneykeeper.ui.review.ReviewModel
import me.bakumon.moneykeeper.ui.setting.backup.BackupViewModel
import me.bakumon.moneykeeper.ui.statistics.bill.BillViewModel
import me.bakumon.moneykeeper.ui.statistics.reports.ReportsViewModel
import me.bakumon.moneykeeper.ui.typemanage.TypeManageViewModel
import me.bakumon.moneykeeper.ui.typerecords.TypeRecordsViewModel
import me.bakumon.moneykeeper.ui.typesort.TypeSortViewModel


/**
 * ViewModel 工厂
 *
 * @author Bakumon https://bakumon.me
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val mDataSource: AppDataSource) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddRecordViewModel::class.java) -> AddRecordViewModel(mDataSource) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(mDataSource) as T
            modelClass.isAssignableFrom(TypeManageViewModel::class.java) -> TypeManageViewModel(mDataSource) as T
            modelClass.isAssignableFrom(TypeSortViewModel::class.java) -> TypeSortViewModel(mDataSource) as T
            modelClass.isAssignableFrom(AddTypeViewModel::class.java) -> AddTypeViewModel(mDataSource) as T
            modelClass.isAssignableFrom(BillViewModel::class.java) -> BillViewModel(mDataSource) as T
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> ReportsViewModel(mDataSource) as T
            modelClass.isAssignableFrom(TypeRecordsViewModel::class.java) -> TypeRecordsViewModel(mDataSource) as T
            modelClass.isAssignableFrom(ReviewModel::class.java) -> ReviewModel(mDataSource) as T
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> BackupViewModel(mDataSource) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
