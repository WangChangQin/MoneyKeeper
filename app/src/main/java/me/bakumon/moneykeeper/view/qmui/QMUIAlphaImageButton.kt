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

package me.bakumon.moneykeeper.view.qmui

import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet

import me.bakumon.moneykeeper.view.qmui.utill.QMUIAlphaViewHelper

class QMUIAlphaImageButton : AppCompatImageButton {

    private var mAlphaViewHelper: QMUIAlphaViewHelper? = null

    private val alphaViewHelper: QMUIAlphaViewHelper?
        get() {
            if (mAlphaViewHelper == null) {
                mAlphaViewHelper = QMUIAlphaViewHelper(this)
            }
            return mAlphaViewHelper
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        alphaViewHelper?.onPressedChanged(this, pressed)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alphaViewHelper?.onEnabledChanged(this, enabled)
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    fun setChangeAlphaWhenPress(changeAlphaWhenPress: Boolean) {
        alphaViewHelper?.setChangeAlphaWhenPress(changeAlphaWhenPress)
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    fun setChangeAlphaWhenDisable(changeAlphaWhenDisable: Boolean) {
        alphaViewHelper?.setChangeAlphaWhenDisable(changeAlphaWhenDisable)
    }

}
