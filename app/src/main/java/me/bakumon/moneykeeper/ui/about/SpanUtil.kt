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

package me.bakumon.moneykeeper.ui.about

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.utill.AndroidUtil

/**
 * SpanUtil
 *
 * @author Bakumon https://bakumon.me
 */
object SpanUtil {

    /**
     * 捐赠作者 Span
     */
    fun getDonateSpannable(activity: Activity): SpannableStringBuilder {

        val line = System.getProperty("line.separator")
        val builder = SpannableStringBuilder(line)
        builder.append(line)

        val text = App.instance.resources.getString(R.string.text_donate)
        val start = 0
        val end = text.length
        val span = SpannableString(text)
        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(App.instance, R.color.colorText)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(RadiusBackgroundSpan(ContextCompat.getColor(App.instance, R.color.colorDonate), 10, 20), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                AndroidUtil.alipay(activity)
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.append(span)

        return builder
    }
}
