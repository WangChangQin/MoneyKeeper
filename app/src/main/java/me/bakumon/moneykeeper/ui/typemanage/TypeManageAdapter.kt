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

package me.bakumon.moneykeeper.ui.typemanage

import android.databinding.ViewDataBinding

import java.util.ArrayList

import me.bakumon.moneykeeper.BR
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseDataBindingAdapter
import me.bakumon.moneykeeper.database.entity.RecordType

/**
 * 类型管理适配器
 *
 * @author bakumon https://bakumon.me
 * @date 2018/5/4
 */

class TypeManageAdapter(data: List<RecordType>?) : BaseDataBindingAdapter<RecordType>(R.layout.item_type_manage, data) {

    override fun convert(helper: BaseDataBindingAdapter.DataBindingViewHolder, item: RecordType) {
        val binding = helper.binding

        binding.setVariable(BR.recordType, item)
        val isLastItem = helper.adapterPosition == mData.size - 1
        // 单位是 dp
        binding.setVariable(BR.bottomMargin, if (isLastItem) 100 else 0)

        binding.executePendingBindings()
    }

    fun setNewData(data: List<RecordType>?, type: Int) {
        if (data != null && data.isNotEmpty()) {
            val result = ArrayList<RecordType>()
            for (i in data.indices) {
                if (data[i].type == type) {
                    result.add(data[i])
                }
            }
            super.setNewData(result)
        } else {
            super.setNewData(null)
        }
    }
}
