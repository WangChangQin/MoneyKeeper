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

package me.bakumon.moneykeeper.ui.review

import io.reactivex.Flowable
import me.bakumon.moneykeeper.base.BaseViewModel
import me.bakumon.moneykeeper.database.entity.MonthSumMoneyBean
import me.bakumon.moneykeeper.database.entity.SumMoneyBean
import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.utill.DateUtils

/**
 * 回顾
 *
 * @author Bakumon https://bakumon.me
 */
class ReviewModel(dataSource: AppDataSource) : BaseViewModel(dataSource) {

    fun getYearSumMoney(year: Int): Flowable<List<SumMoneyBean>> {
        val dateFrom = DateUtils.getYearStart(year)
        val dateTo = DateUtils.getYearEnd(year)
        return mDataSource.getMonthSumMoney(dateFrom, dateTo)
    }

    fun getMonthOfYearSumMoney(year: Int): Flowable<List<MonthSumMoneyBean>> {
        val dateFrom = DateUtils.getYearStart(year)
        val dateTo = DateUtils.getYearEnd(year)
        return mDataSource.getMonthOfYearSumMoney(dateFrom, dateTo)
    }

}
