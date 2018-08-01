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

package me.bakumon.moneykeeper.ui.setting

import android.text.TextUtils
import android.widget.LinearLayout

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import me.bakumon.moneykeeper.R

/**
 * @author Bakumon https://bakumon.me
 */
class SettingAdapter(data: List<SettingSectionEntity>?) : BaseSectionQuickAdapter<SettingSectionEntity, BaseViewHolder>(R.layout.item_setting, R.layout.item_setting_head, data) {

    override fun convertHead(helper: BaseViewHolder, item: SettingSectionEntity) {
        val llHead = helper.getView<LinearLayout>(R.id.ll_head)
        if (helper.adapterPosition == 0) {
            llHead.setPadding(llHead.paddingLeft, 0, llHead.paddingRight, llHead.paddingBottom)
        } else {
            llHead.setPadding(llHead.paddingLeft, llHead.paddingTop, llHead.paddingRight, llHead.paddingBottom)
        }
        helper.setText(R.id.tv_head, item.header)
    }

    override fun convert(helper: BaseViewHolder, item: SettingSectionEntity) {
        helper.setText(R.id.tv_title, item.t.title)
                .setGone(R.id.tv_title, !TextUtils.isEmpty(item.t.title))
                .setTextColor(R.id.tv_title, mContext.resources.getColor(if (item.t.isEnable) R.color.colorText else R.color.colorText3))
                .setText(R.id.tv_content, item.t.content)
                .setGone(R.id.tv_content, !TextUtils.isEmpty(item.t.content))
                .setGone(R.id.iv_icon, item.t.isShowIcon)
                .setVisible(R.id.switch_item, item.t.isShowSwitch)
                .setChecked(R.id.switch_item, item.t.isConfigOpen)
                .addOnClickListener(R.id.switch_item)
    }
}
