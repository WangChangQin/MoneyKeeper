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

package me.bakumon.moneykeeper.ui.home

import me.bakumon.moneykeeper.BR
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseDataBindingAdapter
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.utill.DateUtils

/**
 * HomeAdapter
 *
 * @author bakumon https://bakumon.me
 * @date 2018/4/9
 */

class HomeAdapter(data: List<RecordWithType>?) : BaseDataBindingAdapter<RecordWithType>(R.layout.item_home, data) {

    override fun convert(helper: BaseDataBindingAdapter.DataBindingViewHolder, item: RecordWithType) {
        val binding = helper.binding
        helper.addOnLongClickListener(R.id.ll_item_click)
        binding.setVariable(BR.recordWithType, item)
        val isDataShow = helper.adapterPosition == 0 || !DateUtils.isSameDay(item.time!!, data[helper.adapterPosition - 1].time!!)
        binding.setVariable(BR.isDataShow, isDataShow)
        binding.executePendingBindings()
    }
}
