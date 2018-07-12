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

package me.bakumon.moneykeeper.ui.setting

import android.arch.lifecycle.ViewModel

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import me.bakumon.moneykeeper.utill.BackupUtil

/**
 * 设置 ViewModel
 *
 * @author Bakumon https://bakumon.me
 */
class SettingViewModel : ViewModel() {

    val backupFiles: Flowable<List<BackupBean>>
        get() = Flowable.create({ e ->
            e.onNext(BackupUtil.getBackupFiles())
            e.onComplete()
        }, BackpressureStrategy.BUFFER)

    fun backupDB(): Completable {
        return Completable.create { e ->
            val result = BackupUtil.userBackup()
            if (result) {
                e.onComplete()
            } else {
                e.onError(Exception())
            }
        }
    }

    fun restoreDB(restoreFile: String): Completable {
        return Completable.create { e ->
            val result = BackupUtil.restoreDB(restoreFile)
            if (result) {
                e.onComplete()
            } else {
                e.onError(Exception())
            }
        }
    }
}
