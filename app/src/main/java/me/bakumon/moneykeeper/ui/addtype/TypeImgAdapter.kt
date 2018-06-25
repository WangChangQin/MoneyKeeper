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

package me.bakumon.moneykeeper.ui.addtype

import android.databinding.ViewDataBinding

import me.bakumon.moneykeeper.BR
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseDataBindingAdapter

/**
 * 类型图片适配器
 *
 * @author bakumon https://bakumon.me
 * @date 2018/5/16
 */

class TypeImgAdapter(data: List<TypeImgBean>?) : BaseDataBindingAdapter<TypeImgBean>(R.layout.item_type_img, data) {

    private var mCurrentCheckPosition: Int = 0

    /**
     * 获取当前选中的 item
     */
    val currentItem: TypeImgBean?
        get() = getItem(mCurrentCheckPosition)

    override fun convert(helper: BaseDataBindingAdapter.DataBindingViewHolder, item: TypeImgBean) {
        val binding = helper.binding
        binding.setVariable(BR.typeImg, item)
        binding.executePendingBindings()
    }

    fun checkItem(position: Int) {
        // 选中某一个 item
        var temp: TypeImgBean?
        for (i in 0 until data.size) {
            temp = data[i]
            if (temp != null) {
                temp.isChecked = i == position
            }
        }
        mCurrentCheckPosition = position
        notifyDataSetChanged()
    }
}
