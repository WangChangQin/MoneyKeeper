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

import android.text.TextUtils

import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * BigDecimal 工具类
 *
 * @author Bakumon https://bakumon.me
 */
object BigDecimalUtil {
    /**
     * 分转换为元
     */
    fun fen2Yuan(fenBD: BigDecimal?): String {
        return if (fenBD != null) {
            val yuanBD = fenBD.divide(BigDecimal(100))
            val df = format(yuanBD.toPlainString())
            df.format(yuanBD)
        } else {
            "0"
        }
    }

    /**
     * 分转换为元，去掉分隔符
     */
    fun fen2YuanNoSeparator(fenBD: BigDecimal?): String {
        return fen2Yuan(fenBD).replace(",", "")
    }

    private fun format(yuanStr: String): DecimalFormat {
        val strList = yuanStr.split(".")
        return if (strList.size == 2) {
            if (strList[1].length == 1) {
                DecimalFormat("#,##0.0")
            } else {
                DecimalFormat("#,##0.00")
            }
        } else {
            DecimalFormat("#,###")
        }
    }

    fun formatNum(numStr: String): String {
        val numDB = BigDecimal(numStr)
        val df = format(numStr)
        return df.format(numDB)
    }

    /**
     * 元转换为分
     */
    fun yuan2FenBD(strYuan: String): BigDecimal {
        return if (!TextUtils.isEmpty(strYuan)) {
            // 元最多两位小数，可直接去掉小数位
            BigDecimal(strYuan).multiply(BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN)
        } else {
            BigDecimal(0)
        }
    }
}
