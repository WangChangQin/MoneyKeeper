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

package me.bakumon.moneykeeper.view.qmui.utill

import android.view.View

import java.lang.ref.WeakReference

class QMUIAlphaViewHelper {

    private var mTarget: WeakReference<View>? = null

    /**
     * 设置是否要在 press 时改变透明度
     */
    private var mChangeAlphaWhenPress = true

    /**
     * 设置是否要在 disabled 时改变透明度
     */
    private var mChangeAlphaWhenDisable = true

    private val mNormalAlpha = 1f
    private var mPressedAlpha = .5f
    private var mDisabledAlpha = .5f

    constructor(target: View) {
        mTarget = WeakReference(target)
    }

    constructor(target: View, pressedAlpha: Float, disabledAlpha: Float) {
        mTarget = WeakReference(target)
        mPressedAlpha = pressedAlpha
        mDisabledAlpha = disabledAlpha
    }

    /**
     * @param current the view to be handled, maybe not equal to target view
     * @param pressed
     */
    fun onPressedChanged(current: View, pressed: Boolean) {
        val target = mTarget!!.get() ?: return
        if (current.isEnabled) {
            target.alpha = if (mChangeAlphaWhenPress && pressed && current.isClickable) mPressedAlpha else mNormalAlpha
        } else {
            if (mChangeAlphaWhenDisable) {
                target.alpha = mDisabledAlpha
            }
        }
    }

    /**
     * @param current the view to be handled, maybe not  equal to target view
     * @param enabled
     */
    fun onEnabledChanged(current: View, enabled: Boolean) {
        val target = mTarget!!.get() ?: return
        val alphaForIsEnable: Float
        if (mChangeAlphaWhenDisable) {
            alphaForIsEnable = if (enabled) mNormalAlpha else mDisabledAlpha
        } else {
            alphaForIsEnable = mNormalAlpha
        }
        if (current !== target && target.isEnabled != enabled) {
            target.isEnabled = enabled
        }
        target.alpha = alphaForIsEnable
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    fun setChangeAlphaWhenPress(changeAlphaWhenPress: Boolean) {
        mChangeAlphaWhenPress = changeAlphaWhenPress
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    fun setChangeAlphaWhenDisable(changeAlphaWhenDisable: Boolean) {
        mChangeAlphaWhenDisable = changeAlphaWhenDisable
        val target = mTarget!!.get()
        if (target != null) {
            onEnabledChanged(target, target.isEnabled)
        }

    }

}
