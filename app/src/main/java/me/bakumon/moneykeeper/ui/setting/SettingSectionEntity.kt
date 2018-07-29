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

import com.chad.library.adapter.base.entity.SectionEntity

/**
 * 分组布局实体
 *
 * @author Bakumon https://bakumon.me
 */
class SettingSectionEntity : SectionEntity<SettingSectionEntity.Item> {
    constructor(header: String) : super(true, header)

    constructor(item: Item) : super(item)

    class Item {

        var title: String? = null
        var content: String?
        var isShowIcon: Boolean = false
        var isShowSwitch: Boolean = false
        var isConfigOpen: Boolean = false
        var isEnable: Boolean = true

        constructor(content: String) {
            this.title = null
            this.content = content
            this.isShowIcon = false
            this.isShowSwitch = false
            this.isConfigOpen = false
        }

        constructor(title: String, content: String?) {
            this.title = title
            this.content = content
            this.isShowIcon = false
            this.isShowSwitch = false
            this.isConfigOpen = false
        }

        constructor(title: String, isEnable: Boolean, content: String?) {
            this.title = title
            this.content = content
            this.isEnable = isEnable
            this.isShowIcon = false
            this.isShowSwitch = false
            this.isConfigOpen = false
        }

        constructor(title: String, content: String, isConfigOpen: Boolean) {
            this.title = title
            this.content = content
            this.isShowIcon = false
            this.isShowSwitch = true
            this.isConfigOpen = isConfigOpen
        }

        constructor(isShowIcon: Boolean, title: String, content: String?) {
            this.title = title
            this.content = content
            this.isShowIcon = isShowIcon
            this.isShowSwitch = false
            this.isConfigOpen = false
        }
    }
}
