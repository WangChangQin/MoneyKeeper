package me.bakumon.moneykeeper.view.mpchartpatch.piechart

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.charts.PieChart

/**
 * @author mafei
 */
class MyPieChart : PieChart {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()
        // 此处把mRenderer替换成我们自己的PieChartRenderer
        mRenderer = MyPieChartRenderer(this, mAnimator, mViewPortHandler)
    }

    override fun setHoleColor(color: Int) {
        (mRenderer as MyPieChartRenderer).paintHole.color = color
    }
}
