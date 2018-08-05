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
import android.widget.ImageView
import android.widget.TextView
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.utill.BigDecimalUtil
import java.math.BigDecimal

/**
 * binding 属性适配器（自动被 DataBinding 引用）
 *
 * @author Bakumon https://bakumon.me
 */
object BindAdapter {

    @JvmStatic
    @BindingAdapter("src_img_name")
    fun setImg(imageView: ImageView, imgName: String) {
        val resId = imageView.context.resources.getIdentifier(
                if (TextUtils.isEmpty(imgName)) "type_item_default" else imgName,
                "mipmap",
                imageView.context.packageName)
        imageView.setImageResource(resId)
    }

}
