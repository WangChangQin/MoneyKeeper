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

package me.bakumon.moneykeeper.utill;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import java.util.Arrays;

import me.bakumon.moneykeeper.R;
import me.bakumon.moneykeeper.ui.add.AddRecordActivity;
import me.bakumon.moneykeeper.ui.statistics.StatisticsActivity;

/**
 * Shortcut 工具类
 *
 * @author Bakumon https://bakumon.me
 */
public class ShortcutUtil {
    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void addRecordShortcut(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return;
        }
        try {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            Intent intentAdd = new Intent(context, AddRecordActivity.class);
            intentAdd.setAction("LOCATION_SHORTCUT");
            ShortcutInfo shortcutAdd = new ShortcutInfo.Builder(context, "add")
                    .setShortLabel(context.getString(R.string.shortcuts_add_short))
                    .setLongLabel(context.getString(R.string.shortcuts_add_long))
                    .setIcon(Icon.createWithResource(context, R.drawable.shortcuts_add))
                    .setIntent(intentAdd)
                    .build();

            Intent intentStatistics = new Intent(context, StatisticsActivity.class);
            intentStatistics.setAction("LOCATION_SHORTCUT");
            ShortcutInfo shortcutStatistics = new ShortcutInfo.Builder(context, "statistics")
                    .setShortLabel(context.getString(R.string.shortcuts_statistics_short))
                    .setLongLabel(context.getString(R.string.shortcuts_statistics_long))
                    .setIcon(Icon.createWithResource(context, R.drawable.shortcuts_statistics))
                    .setIntent(intentStatistics)
                    .build();
            if (shortcutManager != null) {
                shortcutManager.setDynamicShortcuts(Arrays.asList(shortcutAdd, shortcutStatistics));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
