package me.bakumon.moneykeeper.ui.statistics.reports.piechart

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.entity.TypeSumMoneyBean
import me.bakumon.moneykeeper.view.mpchartpatch.piechart.NoSmallPieChart

class MinePieChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : NoSmallPieChart(context, attrs, defStyle) {

    private var onValueClickListener: ((String, Int) -> Unit)? = null

    init {
        this.description.isEnabled = false
        this.setNoDataText("")
        this.setUsePercentValues(true)
        this.isDrawHoleEnabled = true
        this.setHoleColor(Color.TRANSPARENT)
        this.isRotationEnabled = false
        this.rotationAngle = 20f

        this.legend.isEnabled = false
        this.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val typeName = (e.data as TypeSumMoneyBean).typeName
                val typeId = (e.data as TypeSumMoneyBean).typeId
                onValueClickListener?.invoke(typeName, typeId)
            }

            override fun onNothingSelected() {

            }
        })
    }

    fun setOnValueClickListener(listener: ((String, Int) -> Unit)) {
        onValueClickListener = listener
    }

    fun setChartData(typeSumMoneyBeans: List<TypeSumMoneyBean>) {
        if (typeSumMoneyBeans.isEmpty()) {
            this.visibility = View.INVISIBLE
            return
        } else {
            this.visibility = View.VISIBLE
        }

        val entries = PieEntryConverter.getBarEntryList(typeSumMoneyBeans)
        val dataSet: PieDataSet

        if (this.data != null && this.data.dataSetCount > 0) {
            dataSet = this.data.getDataSetByIndex(0) as PieDataSet
            dataSet.values = entries
            dataSet.colors = PieColorsCreator.colors(this.context, entries.size)
            this.data.notifyDataChanged()
            this.notifyDataSetChanged()
        } else {
            dataSet = PieDataSet(entries, "")
            dataSet.sliceSpace = 0f
            dataSet.selectionShift = 1.2f
            dataSet.valueLinePart1Length = 0.2f
            dataSet.valueLinePart2Length = 0.4f
            dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            dataSet.isValueLineVariableLength = false
            dataSet.valueLineColor = resources.getColor(R.color.colorText)

            dataSet.colors = PieColorsCreator.colors(this.context, entries.size)

            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter())
            data.setValueTextSize(9f)
            data.setValueTextColor(Color.WHITE)

            this.data = data
        }
        // undo all highlights
        this.highlightValues(null)
        this.invalidate()
        this.animateXY(1000, 1000)
    }

}
