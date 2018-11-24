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
import java.math.BigDecimal

/**
 * RecordTypeViewModel
 *
 * @author Bakumon https://bakumon.me
 */
class TransferAssetsViewModel(dataSource: AppDataSource) : BaseViewModel(dataSource) {

    fun getAssets(): LiveData<List<Assets>> {
        return mDataSource.getAssets()
    }

    fun getAssetsById(id: Int): LiveData<Assets> {
        return mDataSource.getAssetsById(id)
    }

}
