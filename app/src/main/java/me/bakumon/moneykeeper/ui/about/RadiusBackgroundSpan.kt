package me.bakumon.moneykeeper.ui.about

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

/**
 * 圆角背景的 Span
 * @author Bakumon https://bakumon.me
 */
class RadiusBackgroundSpan(private val mColor: Int, private val mRadius: Int, private val mPadding: Int) : ReplacementSpan() {
    private var mSize: Int = 0

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        mSize = (paint.measureText(text, start, end) + 2 * (mRadius + mPadding)).toInt()
        return mSize
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val color = paint.color
        paint.color = mColor
        paint.isAntiAlias = true
        val oval = RectF(x, y + paint.ascent() - mPadding, x + mSize, y.toFloat() + paint.descent() + mPadding.toFloat())
        canvas.drawRoundRect(oval, mRadius.toFloat(), mRadius.toFloat(), paint)
        paint.color = color
        canvas.drawText(text, start, end, x + mRadius.toFloat() + mPadding.toFloat(), y.toFloat(), paint)
    }
}
