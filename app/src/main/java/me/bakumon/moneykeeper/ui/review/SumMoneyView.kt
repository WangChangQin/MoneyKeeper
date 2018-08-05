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

package me.bakumon.moneykeeper.ui.review

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.layout_summoney.view.*
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.SumMoneyBean
import me.bakumon.moneykeeper.utill.BigDecimalUtil
import java.math.BigDecimal

/**
 * 汇总数据
 * 支出 收入 结余
 *
 * @author Bakumon https://bakumon.me
 */
class SumMoneyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val inflater = LayoutInflater.from(context)

    init {
        orientation = LinearLayout.HORIZONTAL
        inflater.inflate(R.layout.layout_summoney, this, true)
    }

    @SuppressLint("SetTextI18n")
    fun setSumMoneyBean(sumMoneyBean: List<SumMoneyBean>) {
        var outlay = BigDecimal(0)
        var income = BigDecimal(0)
        if (sumMoneyBean.isNotEmpty()) {
            for ((type, sumMoney) in sumMoneyBean) {
                if (type == RecordType.TYPE_OUTLAY) {
                    outlay = sumMoney
                } else {
                    income = sumMoney
                }
            }
        }
        tv_outlay.text = context.getString(R.string.text_outlay) + " " + ConfigManager.symbol + BigDecimalUtil.fen2Yuan(outlay)
        tv_income.text = context.getString(R.string.text_income) + " " + ConfigManager.symbol + BigDecimalUtil.fen2Yuan(income)

        if (income > BigDecimal(0)) {
            tv_overage.visibility = View.VISIBLE
            val prefix2 = tv_overage.context.getString(R.string.text_overage) + " " + ConfigManager.symbol
            val overage = prefix2 + BigDecimalUtil.fen2Yuan(income.subtract(outlay))
            tv_overage.text = overage
        } else {
            tv_overage.visibility = View.GONE
        }
    }
}
