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

package me.bakumon.moneykeeper.ui.statistics.bill

import com.github.mikephil.charting.data.BarEntry
import me.bakumon.moneykeeper.database.entity.DaySumMoneyBean
import java.util.*

/**
 * 柱状图数据转换器
 *
 * @author Bakumon https://bakumon.me
 */
object BarEntryConverter {
    /**
     * 获取柱状图所需数据格式 BarEntry
     *
     * @param count            生成的数据 list 大小
     * @param daySumMoneyBeans 包含日期和该日期汇总数据
     * @return List<BarEntry>
    </BarEntry> */
    fun getBarEntryList(count: Int, daySumMoneyBeans: List<DaySumMoneyBean>?): List<BarEntry> {
        val entryList = ArrayList<BarEntry>()
        if (daySumMoneyBeans != null && daySumMoneyBeans.isNotEmpty()) {
            var barEntry: BarEntry
            for (i in 0 until count) {
                for (j in daySumMoneyBeans.indices) {
                    if (i + 1 == daySumMoneyBeans[j].time.date) {
                        barEntry = BarEntry((i + 1).toFloat(), daySumMoneyBeans[j].daySumMoney.toFloat())
                        // 这里的 y 由于是 float，所以数值很大的话，还是会出现科学计数法
                        // 为了避免科学计数法显示,marker从data中取值
                        barEntry.data = daySumMoneyBeans[j].daySumMoney
                        entryList.add(barEntry)
                    }
                }
                barEntry = BarEntry((i + 1).toFloat(), 0f)
                entryList.add(barEntry)
            }
        }
        return entryList
    }
}
