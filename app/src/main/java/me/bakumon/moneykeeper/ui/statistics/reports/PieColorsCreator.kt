package me.bakumon.moneykeeper.ui.statistics.reports

import android.content.Context
import com.github.mikephil.charting.utils.ColorTemplate
import me.bakumon.moneykeeper.R

/**
 * 生成饼图的颜色数组
 *
 * @author Bakumon https://bakumon.me
 */
object PieColorsCreator {
    fun colors(context: Context?, count: Int): List<Int> {

        val colorId = if (count % 2 != 0) {
            intArrayOf(R.color.colorPieChart1, R.color.colorPieChart2, R.color.colorPieChart3, R.color.colorPieChart4, R.color.colorPieChart5, R.color.colorPieChart6, R.color.colorPieChart7)
        } else {
            intArrayOf(R.color.colorPieChart1, R.color.colorPieChart2, R.color.colorPieChart3, R.color.colorPieChart4, R.color.colorPieChart5, R.color.colorPieChart6)
        }

        val allColors = ColorTemplate.createColors(context?.resources, colorId)

        val result: ArrayList<Int> = ArrayList()

        for (i in 0..count) {
            if (i < allColors.size) {
                result.add(allColors[i])
            } else {
                result.add(allColors[i - allColors.size])
            }
        }
        return result
    }
}
