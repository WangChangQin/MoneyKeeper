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

package me.bakumon.moneykeeper.ui.add

import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.BR
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseDataBindingAdapter
import me.bakumon.moneykeeper.database.entity.RecordType
import me.drakeet.floo.Floo
import java.util.*

/**
 * TypeAdapter
 *
 * @author bakumon https://bakumon.me
 * @date 2018/4/9
 */

class TypeAdapter(data: List<RecordType>?) : BaseDataBindingAdapter<RecordType>(R.layout.item_type, data) {

    private var mCurrentCheckPosition: Int = 0
    private var mCurrentCheckId = -1
    private var mType: Int = 0

    /**
     * 获取当前选中的 item
     */
    val currentItem: RecordType?
        get() = getItem(mCurrentCheckPosition)

    override fun convert(helper: BaseDataBindingAdapter.DataBindingViewHolder, item: RecordType) {
        val binding = helper.binding
        binding.setVariable(BR.recordType, item)
        binding.executePendingBindings()
    }

    /**
     * 筛选出支出和收入
     *
     * @param data 支出和收入总数据
     * @param type 类型 0：支出 1：收入
     * @see RecordType.TYPE_OUTLAY 支出
     *
     * @see RecordType.TYPE_INCOME 收入
     */
    fun setNewData(data: List<RecordType>?, type: Int) {
        mType = type
        if (data != null && data.isNotEmpty()) {
            val result = ArrayList<RecordType>()
            for (i in data.indices) {
                if (data[i].type == type) {
                    result.add(data[i])
                }
            }
            // 增加设置 item， type == -1 表示是设置 item
            val settingItem = RecordType(App.instance.getString(R.string.text_setting), "type_item_setting", -1)
            result.add(settingItem)
            // 找出上次选中的 item
            var checkPosition = 0
            if (result[0].type != -1) {
                for (i in result.indices) {
                    if (result[i].id == mCurrentCheckId) {
                        checkPosition = i
                        break
                    }
                }
                super.setNewData(result)
                clickItem(checkPosition)
            } else {
                super.setNewData(result)
            }
        } else {
            super.setNewData(null)
        }
    }

    /**
     * 选中某一个 item，或点击设置 item
     *
     * @param position 选中 item 的索引
     */
    fun clickItem(position: Int) {
        // 点击设置 item
        val item = getItem(position)
        if (item != null && item.type == -1) {
            Floo.navigation(mContext, Router.Url.URL_TYPE_MANAGE)
                    .putExtra(Router.ExtraKey.KEY_TYPE, mType)
                    .start()
            return
        }
        // 选中某一个 item
        var temp: RecordType?
        for (i in 0 until data.size) {
            temp = data[i]
            if (temp != null && temp.type != -1) {
                temp.isChecked = i == position
            }
        }
        mCurrentCheckPosition = position
        mCurrentCheckId = currentItem!!.id
        notifyDataSetChanged()
    }
}
