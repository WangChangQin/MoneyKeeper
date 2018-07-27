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

package me.bakumon.moneykeeper.ui.statistics.reports

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import me.bakumon.moneykeeper.BR
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseDataBindingAdapter
import me.bakumon.moneykeeper.database.entity.TypeSumMoneyBean
import java.math.BigDecimal

/**
 * ReportAdapter
 *
 * @author bakumon https://bakumon.me
 * @date 2018/5/25
 */

class ReportAdapter(data: List<TypeSumMoneyBean>?) : BaseDataBindingAdapter<TypeSumMoneyBean>(R.layout.item_report, data) {

    var colors: List<Int> = arrayListOf()
    private var maxvalueFix: BigDecimal = BigDecimal(0)
    var maxValue: BigDecimal = BigDecimal(0)
        set(value) {
            field = value
            // 和 BarEntryConverter 补偿高度相同逻辑
            maxvalueFix = value.divide(BigDecimal(25), 0, BigDecimal.ROUND_HALF_DOWN)
        }

    override fun convert(helper: BaseDataBindingAdapter.DataBindingViewHolder, item: TypeSumMoneyBean) {
        val lengthView: View = helper.getView(R.id.view_length)
        val lengthEndView: View = helper.getView(R.id.view_length_end)

        lengthView.layoutParams = LinearLayout.LayoutParams(0, 10,
                item.typeSumMoney.add(maxvalueFix).toFloat())
        lengthEndView.layoutParams = LinearLayout.LayoutParams(0, 10,
                maxValue.subtract(item.typeSumMoney.add(maxvalueFix)).toFloat())

        val gradient = lengthView.background as GradientDrawable
        if (colors.size > helper.adapterPosition) {
            gradient.setColor(colors[helper.adapterPosition])
        } else {
            gradient.setColor(mContext.resources.getColor(R.color.colorPieChart1))
        }

        val binding = helper.binding
        binding.setVariable(BR.typeSumMoney, item)
        binding.executePendingBindings()
    }
}
