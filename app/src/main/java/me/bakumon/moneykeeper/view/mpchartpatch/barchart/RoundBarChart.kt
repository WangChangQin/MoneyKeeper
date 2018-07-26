package me.bakumon.moneykeeper.view.mpchartpatch.barchart

import android.content.Context
import android.util.AttributeSet

import com.github.mikephil.charting.charts.BarChart

/**
 * 上方圆角柱状图
 *
 * @author Bakumon https://bakumon.me
 */
class RoundBarChart : BarChart {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()
        mRenderer = RoundBarChartRenderer(this, mAnimator, mViewPortHandler)
    }
}
