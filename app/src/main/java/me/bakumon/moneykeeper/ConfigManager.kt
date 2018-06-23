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

import me.bakumon.moneykeeper.utill.SPUtils

/**
 * 管理本地配置
 *
 * @author Bakumon https://bakumon.me
 */
object ConfigManager {
    private const val SP_NAME = "config"
    private const val KEY_AUTO_BACKUP = "auto_backup"
    private const val KEY_SUCCESSIVE = "successive"
    private const val KEY_FAST = "fast"
    private const val KEY_BUDGET = "budget"

    val isAutoBackup: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_AUTO_BACKUP, true)

    val isFast: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_FAST)

    val isSuccessive: Boolean
        get() = SPUtils.getInstance(SP_NAME)!!.getBoolean(KEY_SUCCESSIVE, true)

    val budget: Int
        get() = SPUtils.getInstance(SP_NAME)!!.getInt(KEY_BUDGET, 0)

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
}
