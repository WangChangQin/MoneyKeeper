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

/**
 * Shortcut 工具类
 *
 * @author Bakumon https://bakumon.me
 */
public class ShortcutUtil {
    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void addRecordShortcut(Context context) {
        try {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            Intent intent = new Intent(context, AddRecordActivity.class);
            intent.setAction("LOCATION_SHORTCUT");
            ShortcutInfo shortcut = new ShortcutInfo.Builder(context, "add")
                    .setShortLabel(context.getString(R.string.shortcuts_add_short))
                    .setLongLabel(context.getString(R.string.shortcuts_add_long))
                    .setIcon(Icon.createWithResource(context, R.drawable.shortcutse_add))
                    .setIntent(intent)
                    .build();
            if (shortcutManager != null) {
                shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
