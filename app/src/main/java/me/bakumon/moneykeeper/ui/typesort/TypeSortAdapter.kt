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

package me.bakumon.moneykeeper.ui.typesort

import android.support.v7.widget.RecyclerView
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.listener.OnItemDragListener
import me.bakumon.moneykeeper.BR
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseDraggableAdapter
import me.bakumon.moneykeeper.database.entity.RecordType

/**
 * 类型排序列表适配器
 *
 * @author bakumon https://bakumon.me
 * @date 2018/5/10
 */

class TypeSortAdapter(data: List<RecordType>?) : BaseDraggableAdapter<RecordType>(R.layout.item_type_sort, data), OnItemDragListener {

    init {
        setOnItemDragListener(this)
    }

    override fun convert(helper: BaseDraggableAdapter.DataBindingViewHolder, item: RecordType) {
        val binding = helper.binding
        binding.setVariable(BR.recordType, item)
        binding.executePendingBindings()
    }

    override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
        (viewHolder as BaseViewHolder)
                .itemView.alpha = 0.8f
    }

    override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int, target: RecyclerView.ViewHolder, to: Int) {

    }

    override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
        (viewHolder as BaseViewHolder)
                .itemView.alpha = 1f
    }
}
