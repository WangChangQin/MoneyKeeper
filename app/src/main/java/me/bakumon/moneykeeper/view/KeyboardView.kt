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

package me.bakumon.moneykeeper.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_keyboard.view.*
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.utill.SoftInputUtils

/**
 * 自定义键盘
 *
 * @author Bakumon https://bakumon.me
 */
class KeyboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var mOnAffirmClickListener: ((String) -> Unit)? = null

    init {
        init(context)
    }

    fun setAffirmEnable(enable: Boolean) {
        tvAffirm.isEnabled = enable
    }

    fun setText(text: String) {
        editInput.setText(text)
        editInput.setSelection(editInput.text.length)
        SoftInputUtils.hideSoftInput(editInput)
        if (!editInput.isFocused) {
            editInput.requestFocus()
        }
    }

    fun setEditTextFocus() {
        editInput.requestFocus()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context) {
        // 当前 activity 打开时不弹出软键盘
        val activity = context as Activity
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_keyboard, this, true)

        editInput.requestFocus()
        editInput.setOnTouchListener { _, _ ->
            SoftInputUtils.hideSoftInput(editInput)
            editInput.requestFocus()
            // 返回 true，拦截了默认的点击和长按操作，这是一个妥协的做法
            // 不再考虑多选粘贴的情况
            true
        }

        setInputTextViews(tvNum0, tvNum1,
                tvNum2, tvNum3,
                tvNum4, tvNum5,
                tvNum6, tvNum7,
                tvNum8, tvNum9,
                tvPoint)
        setDeleteView(llDelete)

        tvAffirm.setOnClickListener {
            val text = editInput.text.toString()
            val isDigital = (!TextUtils.isEmpty(text)
                    && !TextUtils.equals("0", text)
                    && !TextUtils.equals("0.", text)
                    && !TextUtils.equals("0.0", text)
                    && !TextUtils.equals("0.00", text))
            if (!isDigital) {
                val animation = AnimationUtils.loadAnimation(App.instance, R.anim.shake)
                editInput.startAnimation(animation)
            } else {
                mOnAffirmClickListener?.invoke(text)
            }
        }
    }

    /**
     * 设置键盘输入字符的textView，注意，textView点击后text将会输入到editText上
     */
    private fun setInputTextViews(vararg textViews: TextView) {
        val target = editInput
        if (textViews.isEmpty()) {
            return
        }
        for (i in textViews.indices) {
            textViews[i].setOnClickListener {
                val sb = StringBuilder(target.text.toString().trim { it <= ' ' })
                val result = inputFilter(sb, textViews[i].text.toString())
                setText(result)
            }
        }
    }

    /**
     * 整数9位，小数2位
     */
    private fun inputFilter(sb: StringBuilder, text: String): String {
        if (sb.isEmpty()) {
            // 输入第一个字符
            if (TextUtils.equals(text, ".")) {
                sb.insert(0, "0.")
            } else {
                sb.insert(0, text)
            }
        } else if (sb.length == 1) {
            // 输入第二个字符
            if (sb.toString().startsWith("0")) {
                if (TextUtils.equals(".", text)) {
                    sb.insert(sb.length, ".")
                } else {
                    sb.replace(0, 1, text)
                }
            } else {
                sb.insert(sb.length, text)
            }
        } else if (sb.toString().contains(".")) {
            // 已经输入了小数点
            val length = sb.length
            val index = sb.indexOf(".")
            if (!TextUtils.equals(".", text)) {
                if (length - index < 3) {
                    sb.insert(sb.length, text)
                }
            }
        } else {
            // 整数
            if (TextUtils.equals(".", text)) {
                sb.insert(sb.length, text)
            } else {
                if (sb.length < MAX_INTEGER_NUMBER) {
                    sb.insert(sb.length, text)
                }
            }
        }
        return sb.toString()
    }

    /**
     * 设置删除键
     */
    private fun setDeleteView(deleteView: View) {
        val target = editInput
        deleteView.setOnClickListener {
            val sb = StringBuilder(target.text.toString().trim { it <= ' ' })
            if (sb.isNotEmpty()) {
                sb.deleteCharAt(sb.length - 1)
                setText(sb.toString())
            }
        }
        deleteView.setOnLongClickListener {
            val sb = StringBuilder(target.text.toString().trim { it <= ' ' })
            if (sb.isNotEmpty()) {
                setText("")
            }
            true
        }
    }

    companion object {
        private const val MAX_INTEGER_NUMBER = 6
    }
}
