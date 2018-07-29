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

package me.bakumon.moneykeeper.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.snatik.storage.Storage
import io.reactivex.disposables.CompositeDisposable
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.api.ApiResponse
import me.bakumon.moneykeeper.api.Network
import me.bakumon.moneykeeper.database.AppDatabase

import me.bakumon.moneykeeper.datasource.AppDataSource
import me.bakumon.moneykeeper.ui.setting.backup.BackupViewModel
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody

/**
 * ViewModel基类
 * 包含 AppDataSource 数据源
 *
 * @author Bakumon https://bakumon.me
 */
open class BaseViewModel(protected var mDataSource: AppDataSource) : ViewModel() {
    protected val mDisposable = CompositeDisposable()
    override fun onCleared() {
        super.onCleared()
        mDisposable.clear()
    }

    fun createDir(): LiveData<ApiResponse<ResponseBody>> {
        return Network.davService().createDir(BackupViewModel.BACKUP_DIR)
    }

    fun getList(): LiveData<ApiResponse<ResponseBody>> {
        return Network.davService().list(BackupViewModel.BACKUP_DIR)
    }

    fun backup(): LiveData<ApiResponse<ResponseBody>> {
        val storage = Storage(App.instance)
        val path = App.instance.getDatabasePath(AppDatabase.DB_NAME)?.path
        val file = storage.getFile(path)

        val body = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        return Network.davService().upload(BackupViewModel.BACKUP_FILE, body)
    }

}
