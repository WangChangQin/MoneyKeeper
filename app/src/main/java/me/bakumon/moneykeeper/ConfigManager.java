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

package me.bakumon.moneykeeper;

import me.bakumon.moneykeeper.utill.SPUtils;

/**
 * 管理本地配置
 *
 * @author Bakumon https://bakumon.me
 */
public class ConfigManager {
    private static final String SP_NAME = "config";
    private static final String KEY_AUTO_BACKUP = "auto_backup";
    private static final String KEY_SUCCESSIVE = "successive";
    private static final String KEY_FAST = "fast";
    private static final String KEY_BUDGET = "budget";

    /**
     * 自动备份
     */
    public static boolean setIsAutoBackup(boolean isAutoBackup) {
        return SPUtils.getInstance(SP_NAME).put(KEY_AUTO_BACKUP, isAutoBackup);
    }

    public static boolean isAutoBackup() {
        return SPUtils.getInstance(SP_NAME).getBoolean(KEY_AUTO_BACKUP, true);
    }

    /**
     * 快速记账
     */
    public static boolean setIsFast(boolean isFast) {
        return SPUtils.getInstance(SP_NAME).put(KEY_FAST, isFast);
    }

    public static boolean isFast() {
        return SPUtils.getInstance(SP_NAME).getBoolean(KEY_FAST, false);
    }

    /**
     * 连续记账
     */
    public static boolean setIsSuccessive(boolean isAutoBackup) {
        return SPUtils.getInstance(SP_NAME).put(KEY_SUCCESSIVE, isAutoBackup);
    }

    public static boolean isSuccessive() {
        return SPUtils.getInstance(SP_NAME).getBoolean(KEY_SUCCESSIVE, true);
    }

    /**
     * 月预算
     */
    public static boolean setBudget(int budget) {
        return SPUtils.getInstance(SP_NAME).put(KEY_BUDGET, budget);
    }

    public static int getBudget() {
        return SPUtils.getInstance(SP_NAME).getInt(KEY_BUDGET, 0);
    }
}
