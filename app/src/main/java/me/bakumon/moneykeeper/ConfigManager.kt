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

package me.bakumon.moneykeeper

import android.support.annotation.IntDef
import android.text.TextUtils
import me.bakumon.moneykeeper.utill.SPUtils
import java.math.BigDecimal

/**
 * 管理本地配置
 *
 * @author Bakumon https://bakumon.me
 */
object ConfigManager {

    @IntDef(MODE_NO, MODE_LAUNCHER_APP, MODE_EXIT_APP)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CloudBackupMode

    const val MODE_NO = 0L
    const val MODE_LAUNCHER_APP = 1L
    const val MODE_EXIT_APP = 2L

    private const val SP_NAME = "config"
    private const val KEY_AUTO_BACKUP = "auto_backup"
    private const val KEY_SUCCESSIVE = "successive"
    private const val KEY_FAST = "fast"
    private const val KEY_BUDGET = "budget"
    private const val KEY_ASSETS = "assets"
    private const val KEY_SYMBOL = "symbol"
    private const val KEY_WEBDAV_URL = "webDav_url"
    private const val KEY_WEBDAV_ACCOUNT = "webDav_account"
    private const val KEY_WEBDAV_ENCRYPT_PSW = "webDav_encrypt_psw"
    private const val KEY_CLOUD_BACKUP_MODE = "cloud_backup_mode"
    private const val KEY_CLOUD_ENABLE = "cloud_enable"
    private const val KEY_IS_THEME_DARK = "is_theme_dark"
    private const val KEY_WIDGET_ENABLE = "is_widget_enable"
    private const val KEY_BACKUP_FOLDER = "backup_folder"

    val isAutoBackup: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_AUTO_BACKUP, true)

    val isFast: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_FAST)

    val isSuccessive: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_SUCCESSIVE, true)

    val budget: Int
        get() = SPUtils.getInstance(SP_NAME)!!.getInt(KEY_BUDGET, 0)

    val assets: String
        get() = SPUtils.getInstance(SP_NAME)!!.getString(KEY_ASSETS, "NaN")

    val symbol: String
        get() = SPUtils.getInstance(SP_NAME)!!.getString(KEY_SYMBOL, App.instance.resources.getStringArray(R.array.simple_symbol)[0])

    val webDavUrl: String
        get() = SPUtils.getInstance(SP_NAME)!!.getString(KEY_WEBDAV_URL, "")

    val webDavAccount: String
        get() = SPUtils.getInstance(SP_NAME)!!.getString(KEY_WEBDAV_ACCOUNT, "")

    val webDavEncryptPsw: String
        get() = SPUtils.getInstance(SP_NAME)!!.getString(KEY_WEBDAV_ENCRYPT_PSW, "")

    var webDAVPsw = ""

    @CloudBackupMode
    val cloudBackupMode: Long
        get() = SPUtils.getInstance(SP_NAME)!!.getLong(KEY_CLOUD_BACKUP_MODE, MODE_NO)

    val cloudEnable: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_CLOUD_ENABLE, false)

    val isThemeDark: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_IS_THEME_DARK, true)

    val isWidgetEnable: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_WIDGET_ENABLE, false)

    val backupFolder: String
        get() = SPUtils.getInstance(SP_NAME)!!.getString(KEY_BACKUP_FOLDER)

    /**
     * 自动备份
     */
    fun setIsAutoBackup(isAutoBackup: Boolean): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_AUTO_BACKUP, isAutoBackup)
    }

    /**
     * 快速记账
     */
    fun setIsFast(isFast: Boolean): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_FAST, isFast)
    }

    /**
     * 连续记账
     */
    fun setIsSuccessive(isAutoBackup: Boolean): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_SUCCESSIVE, isAutoBackup)
    }

    /**
     * 月预算
     */
    fun setBudget(budget: Int): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_BUDGET, budget)
    }

    /**
     * 资产余额
     */
    fun setAssets(assets: String): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_ASSETS, assets)
    }

    /**
     * 增加资产
     */
    fun addAssets(num: BigDecimal): Boolean {
        if (TextUtils.equals(assets, "NaN")) {
            return true
        }
        return setAssets(BigDecimal(assets).add(num).toPlainString())
    }

    /**
     * 减少资产
     */
    fun reduceAssets(num: BigDecimal): Boolean {
        if (TextUtils.equals(assets, "NaN")) {
            return true
        }
        return setAssets(BigDecimal(assets).subtract(num).toPlainString())
    }

    /**
     * 货币符号
     */
    fun setSymbol(symbol: String): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_SYMBOL, symbol)
    }

    /**
     * WebDAV地址
     */
    fun setWevDavUrl(url: String): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_WEBDAV_URL, url)
    }

    /**
     * WebDAV账号
     */
    fun setWevDavAccount(account: String): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_WEBDAV_ACCOUNT, account)
    }

    /**
     * WebDAV加密后的密码
     */
    fun setWebDavEncryptPsw(encryptPsw: String): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_WEBDAV_ENCRYPT_PSW, encryptPsw)
    }

    /**
     * 云备份模式
     */
    fun setCloudBackupMode(@CloudBackupMode cloudBackupMode: Long): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_CLOUD_BACKUP_MODE, cloudBackupMode)
    }

    /**
     * 网盘是否可用
     */
    fun setCloudEnable(cloudEnable: Boolean): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_CLOUD_ENABLE, cloudEnable)
    }

    /**
     * 保存主题
     */
    fun setIsThemeDark(isDark: Boolean): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_IS_THEME_DARK, isDark)
    }

    /**
     * 保存主题
     */
    fun setIsWidgetEnable(enable: Boolean): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_WIDGET_ENABLE, enable)
    }

    /**
     * 本地备份文件夹
     */
    fun setBackupFolder(folder: String): Boolean {
        return SPUtils.getInstance(SP_NAME)!!.put(KEY_BACKUP_FOLDER, folder)
    }

}
