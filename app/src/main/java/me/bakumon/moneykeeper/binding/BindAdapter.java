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

package me.bakumon.moneykeeper.binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import me.bakumon.moneykeeper.ConfigManager;
import me.bakumon.moneykeeper.R;
import me.bakumon.moneykeeper.database.entity.RecordType;
import me.bakumon.moneykeeper.database.entity.SumMoneyBean;
import me.bakumon.moneykeeper.utill.BigDecimalUtil;
import me.bakumon.moneykeeper.utill.SizeUtils;

/**
 * binding 属性适配器（自动被 DataBinding 引用）
 *
 * @author Bakumon https://bakumon.me
 */
public class BindAdapter {

    @BindingAdapter("android:visibility")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("custom_margin_bottom")
    public static void setMarginBottom(View view, int bottomMargin) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams;
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
        } else {
            marginParams = new ViewGroup.MarginLayoutParams(layoutParams);
        }
        marginParams.bottomMargin = SizeUtils.dp2px(bottomMargin);
    }

    @BindingAdapter("text_check_null")
    public static void setText(TextView textView, String text) {
        textView.setText(text);
        textView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    @BindingAdapter("src_img_name")
    public static void setImg(ImageView imageView, String imgName) {
        Context context = imageView.getContext();
        if (TextUtils.isEmpty(imgName)) {
            imgName = "type_item_default";
        }
        int resId = context.getResources().getIdentifier(imgName, "mipmap", context.getPackageName());
        imageView.setImageResource(resId);
    }

    @BindingAdapter("text_money")
    public static void setMoneyText(TextView textView, BigDecimal bigDecimal) {
        textView.setText(BigDecimalUtil.fen2Yuan(bigDecimal));
    }

    @BindingAdapter("text_money_with_prefix")
    public static void setMoneyTextWithPrefix(TextView textView, BigDecimal bigDecimal) {
        String symbol = textView.getResources().getString(R.string.text_money_symbol);
        textView.setText(symbol + BigDecimalUtil.fen2Yuan(bigDecimal));
    }

    @BindingAdapter("text_income_or_budget")
    public static void setTitleIncomeOrBudget(TextView textView, List<SumMoneyBean> list) {
        // 显示剩余预算或本月收入
        if (ConfigManager.getBudget() > 0) {
            textView.setText(R.string.text_month_remaining_budget);
        } else {
            textView.setText(R.string.text_month_income);
        }
    }

    @BindingAdapter("text_month_outlay")
    public static void setMonthOutlay(TextView textView, List<SumMoneyBean> sumMoneyBean) {
        String outlay = "0";
        if (sumMoneyBean != null && sumMoneyBean.size() > 0) {
            for (SumMoneyBean bean : sumMoneyBean) {
                if (bean.type == RecordType.TYPE_OUTLAY) {
                    outlay = BigDecimalUtil.fen2Yuan(bean.sumMoney);
                }
            }
        }
        textView.setText(outlay);
    }

    @BindingAdapter("text_month_income_or_budget")
    public static void setMonthIncomeOrBudget(TextView textView, List<SumMoneyBean> sumMoneyBean) {
        BigDecimal outlay = new BigDecimal(0);
        String inComeStr = "0";
        if (sumMoneyBean != null && sumMoneyBean.size() > 0) {
            for (SumMoneyBean bean : sumMoneyBean) {
                if (bean.type == RecordType.TYPE_OUTLAY) {
                    outlay = bean.sumMoney;
                } else if (bean.type == RecordType.TYPE_INCOME) {
                    inComeStr = BigDecimalUtil.fen2Yuan(bean.sumMoney);
                }
            }
        }
        // 显示剩余预算或本月收入
        int budget = ConfigManager.getBudget();
        if (budget > 0) {
            String budgetStr = BigDecimalUtil.fen2Yuan(new BigDecimal(ConfigManager.getBudget()).multiply(new BigDecimal(100)).subtract(outlay));
            textView.setText(budgetStr);
        } else {
            textView.setText(inComeStr);
        }
    }

    @BindingAdapter("text_statistics_outlay")
    public static void setMonthStatisticsOutlay(TextView textView, List<SumMoneyBean> sumMoneyBean) {
        String prefix = textView.getContext().getString(R.string.text_month_outlay_symbol);
        String outlay = prefix + "0";
        if (sumMoneyBean != null && sumMoneyBean.size() > 0) {
            for (SumMoneyBean bean : sumMoneyBean) {
                if (bean.type == RecordType.TYPE_OUTLAY) {
                    outlay = prefix + BigDecimalUtil.fen2Yuan(bean.sumMoney);
                }
            }
        }
        textView.setText(outlay);
    }

    @BindingAdapter("text_statistics_income")
    public static void setMonthStatisticsIncome(TextView textView, List<SumMoneyBean> sumMoneyBean) {
        String prefix = textView.getContext().getString(R.string.text_month_income_symbol);
        String income = prefix + "0";
        if (sumMoneyBean != null && sumMoneyBean.size() > 0) {
            for (SumMoneyBean bean : sumMoneyBean) {
                if (bean.type == RecordType.TYPE_INCOME) {
                    income = prefix + BigDecimalUtil.fen2Yuan(bean.sumMoney);
                }
            }
        }
        textView.setText(income);
    }

    @BindingAdapter("text_statistics_overage")
    public static void setMonthStatisticsOverage(TextView textView, List<SumMoneyBean> sumMoneyBean) {
        BigDecimal outlayBd = new BigDecimal(0);
        BigDecimal incomeBd = new BigDecimal(0);
        // 是否显示结余
        boolean isShowOverage = false;
        if (sumMoneyBean != null && sumMoneyBean.size() > 0) {
            for (SumMoneyBean bean : sumMoneyBean) {
                if (bean.type == RecordType.TYPE_OUTLAY) {
                    outlayBd = bean.sumMoney;
                } else if (bean.type == RecordType.TYPE_INCOME) {
                    isShowOverage = bean.sumMoney.compareTo(new BigDecimal(0)) > 0;
                    incomeBd = bean.sumMoney;
                }
            }
        }
        if (isShowOverage) {
            textView.setVisibility(View.VISIBLE);
            String prefix = textView.getContext().getString(R.string.text_month_overage);
            String overage = prefix + BigDecimalUtil.fen2Yuan(incomeBd.subtract(outlayBd));
            textView.setText(overage);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}
