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

package me.bakumon.moneykeeper.binding

import android.databinding.BindingAdapter
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.SumMoneyBean
import me.bakumon.moneykeeper.utill.BigDecimalUtil
import me.bakumon.moneykeeper.utill.SizeUtils
import java.math.BigDecimal

/**
 * binding 属性适配器（自动被 DataBinding 引用）
 *
 * @author Bakumon https://bakumon.me
 */
object BindAdapter {

    @JvmStatic
    @BindingAdapter("android:visibility")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("custom_margin_bottom")
    fun setMarginBottom(view: View, bottomMargin: Int) {
        val layoutParams = view.layoutParams
        val marginParams: ViewGroup.MarginLayoutParams
        marginParams = layoutParams as? ViewGroup.MarginLayoutParams ?: ViewGroup.MarginLayoutParams(layoutParams)
        marginParams.bottomMargin = SizeUtils.dp2px(bottomMargin.toFloat())
    }

    @JvmStatic
    @BindingAdapter("text_check_null")
    fun setText(textView: TextView, text: String) {
        textView.text = text
        textView.visibility = if (TextUtils.isEmpty(text)) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("src_img_name")
    fun setImg(imageView: ImageView, imgName: String) {
        val resId = imageView.context.resources.getIdentifier(
                if (TextUtils.isEmpty(imgName)) "type_item_default" else imgName,
                "mipmap",
                imageView.context.packageName)
        imageView.setImageResource(resId)
    }

    @JvmStatic
    @BindingAdapter("text_money")
    fun setMoneyText(textView: TextView, bigDecimal: BigDecimal) {
        textView.text = BigDecimalUtil.fen2Yuan(bigDecimal)
    }

    @JvmStatic
    @BindingAdapter("text_money_with_prefix")
    fun setMoneyTextWithPrefix(textView: TextView, bigDecimal: BigDecimal) {
        val text = textView.resources.getString(R.string.text_money_symbol) + BigDecimalUtil.fen2Yuan(bigDecimal)
        textView.text = text
    }

    @JvmStatic
    @BindingAdapter("text_income_or_budget")
    fun setTitleIncomeOrBudget(textView: TextView, list: List<SumMoneyBean>?) {
        // 显示剩余预算或本月收入
        if (ConfigManager.budget > 0) {
            textView.setText(R.string.text_month_remaining_budget)
        } else {
            textView.setText(R.string.text_month_income)
        }
    }

    @JvmStatic
    @BindingAdapter("text_month_outlay")
    fun setMonthOutlay(textView: TextView, sumMoneyBean: List<SumMoneyBean>?) {
        var outlay = "0"
        if (sumMoneyBean != null && sumMoneyBean.isNotEmpty()) {
            for ((type, sumMoney) in sumMoneyBean) {
                if (type == RecordType.TYPE_OUTLAY) {
                    outlay = BigDecimalUtil.fen2Yuan(sumMoney)
                }
            }
        }
        textView.text = outlay
    }

    @JvmStatic
    @BindingAdapter("text_month_income_or_budget")
    fun setMonthIncomeOrBudget(textView: TextView, sumMoneyBean: List<SumMoneyBean>?) {
        var outlay = BigDecimal(0)
        var inComeStr = "0"
        if (sumMoneyBean != null && sumMoneyBean.isNotEmpty()) {
            for ((type, sumMoney) in sumMoneyBean) {
                if (type == RecordType.TYPE_OUTLAY) {
                    outlay = sumMoney
                } else if (type == RecordType.TYPE_INCOME) {
                    inComeStr = BigDecimalUtil.fen2Yuan(sumMoney)
                }
            }
        }
        // 显示剩余预算或本月收入
        val budget = ConfigManager.budget
        if (budget > 0) {
            val budgetStr = BigDecimalUtil.fen2Yuan(BigDecimal(ConfigManager.budget).multiply(BigDecimal(100)).subtract(outlay))
            textView.text = budgetStr
        } else {
            textView.text = inComeStr
        }
    }

    @JvmStatic
    @BindingAdapter("text_statistics_outlay")
    fun setMonthStatisticsOutlay(textView: TextView, sumMoneyBean: List<SumMoneyBean>?) {
        val prefix = textView.context.getString(R.string.text_month_outlay_symbol)
        var outlay = prefix + "0"
        if (sumMoneyBean != null && sumMoneyBean.isNotEmpty()) {
            for ((type, sumMoney) in sumMoneyBean) {
                if (type == RecordType.TYPE_OUTLAY) {
                    outlay = prefix + BigDecimalUtil.fen2Yuan(sumMoney)
                }
            }
        }
        textView.text = outlay
    }

    @JvmStatic
    @BindingAdapter("text_statistics_income")
    fun setMonthStatisticsIncome(textView: TextView, sumMoneyBean: List<SumMoneyBean>?) {
        val prefix = textView.context.getString(R.string.text_month_income_symbol)
        var income = prefix + "0"
        if (sumMoneyBean != null && sumMoneyBean.isNotEmpty()) {
            for ((type, sumMoney) in sumMoneyBean) {
                if (type == RecordType.TYPE_INCOME) {
                    income = prefix + BigDecimalUtil.fen2Yuan(sumMoney)
                }
            }
        }
        textView.text = income
    }

    @JvmStatic
    @BindingAdapter("text_statistics_overage")
    fun setMonthStatisticsOverage(textView: TextView, sumMoneyBean: List<SumMoneyBean>?) {
        var outlayBd = BigDecimal(0)
        var incomeBd = BigDecimal(0)
        // 是否显示结余
        var isShowOverage = false
        if (sumMoneyBean != null && sumMoneyBean.isNotEmpty()) {
            for ((type, sumMoney) in sumMoneyBean) {
                if (type == RecordType.TYPE_OUTLAY) {
                    outlayBd = sumMoney
                } else if (type == RecordType.TYPE_INCOME) {
                    isShowOverage = sumMoney > BigDecimal(0)
                    incomeBd = sumMoney
                }
            }
        }
        if (isShowOverage) {
            textView.visibility = View.VISIBLE
            val prefix = textView.context.getString(R.string.text_month_overage)
            val overage = prefix + BigDecimalUtil.fen2Yuan(incomeBd.subtract(outlayBd))
            textView.text = overage
        } else {
            textView.visibility = View.GONE
        }
    }
}
