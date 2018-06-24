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

package me.bakumon.moneykeeper.utill

import com.snatik.storage.Storage
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.BuildConfig
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.AppDatabase
import me.bakumon.moneykeeper.ui.setting.BackupBean
import org.ocpsoft.prettytime.PrettyTime
import java.io.File
import java.util.*

/**
 * 备份相关工具类
 *
 * @author Bakumon https:/bakumon.me
 */
object BackupUtil {
    private val BACKUP_DIR = if (BuildConfig.DEBUG) "backup_moneykeeper_debug" else "backup_moneykeeper"
    private val AUTO_BACKUP_PREFIX = if (BuildConfig.DEBUG) "MoneyKeeperBackupAutoDebug" else "MoneyKeeperBackupAuto"
    private val USER_BACKUP_PREFIX = if (BuildConfig.DEBUG) "MoneyKeeperBackupUserDebug" else "MoneyKeeperBackupUser"
    private val BACKUP_SUFFIX = App.instance.getString(R.string.text_before_reverting)
    private const val SUFFIX = ".db"

    fun getBackupFiles(): List<BackupBean> {
        val storage = Storage(App.instance)
        val dir = storage.externalStorageDirectory + File.separator + BACKUP_DIR
        val backupBeans = ArrayList<BackupBean>()
        var bean: BackupBean
        val files = storage.getFiles(dir, "[\\S]*\\.db") ?: return backupBeans
        var fileTemp: File
        val prettyTime = PrettyTime()
        for (i in files.indices) {
            fileTemp = files[i]
            bean = BackupBean()
            bean.file = fileTemp
            bean.name = fileTemp.name
            bean.size = storage.getReadableSize(fileTemp)
            bean.time = prettyTime.format(Date(fileTemp.lastModified()))
            backupBeans.add(bean)
        }
        return backupBeans
    }

    private fun backupDB(fileName: String): Boolean {
        val storage = Storage(App.instance)
        val isWritable = Storage.isExternalWritable()
        if (!isWritable) {
            return false
        }
        val path = storage.externalStorageDirectory + File.separator + BACKUP_DIR
        if (!storage.isDirectoryExists(path)) {
            storage.createDirectory(path)
        }
        val filePath = path + File.separator + fileName
        if (!storage.isFileExist(filePath)) {
            // 创建空文件，在模拟器上测试，如果没有这个文件，复制的时候会报 FileNotFound
            storage.createFile(filePath, "")
        }
        return storage.copy(App.instance.getDatabasePath(AppDatabase.DB_NAME)?.path, path + File.separator + fileName)
    }

    fun autoBackup(): Boolean {
        val fileName = AUTO_BACKUP_PREFIX + SUFFIX
        return backupDB(fileName)
    }

    fun autoBackupForNecessary(): Boolean {
        val fileName = AUTO_BACKUP_PREFIX + SUFFIX
        val storage = Storage(App.instance)
        val isWritable = Storage.isExternalWritable()
        if (!isWritable) {
            return false
        }
        val path = storage.externalStorageDirectory + File.separator + BACKUP_DIR
        if (!storage.isDirectoryExists(path)) {
            storage.createDirectory(path)
        }
        val filePath = path + File.separator + fileName
        if (!storage.isFileExist(filePath)) {
            // 创建空文件，在模拟器上测试，如果没有这个文件，复制的时候会报 FileNotFound
            storage.createFile(filePath, "")
            return storage.copy(App.instance.getDatabasePath(AppDatabase.DB_NAME)?.path, path + File.separator + fileName)
        }
        return true
    }

    fun userBackup(): Boolean {
        val fileName = USER_BACKUP_PREFIX + SUFFIX
        return backupDB(fileName)
    }

    fun restoreDB(restoreFile: String): Boolean {
        val storage = Storage(App.instance)
        if (storage.isFileExist(restoreFile)) {
            // 恢复之前，备份一下最新数据
            val fileName = "MoneyKeeperBackup" + DateUtils.getCurrentDateString() + BACKUP_SUFFIX + SUFFIX
            val isBackupSuccess = backupDB(fileName)
            val isRestoreSuccess = storage.copy(restoreFile, App.instance.getDatabasePath(AppDatabase.DB_NAME)?.path)
            return isBackupSuccess && isRestoreSuccess
        }
        return false
    }
}
