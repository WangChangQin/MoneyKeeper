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

package me.bakumon.moneykeeper.database.entity

import android.arch.persistence.room.*
import java.io.Serializable

/**
 * 记账类型
 *
 * @author bakumon https://bakumon.me
 */
@Entity(indices = [Index("type", "ranking", "state")])
class RecordType : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var name: String?

    /**
     * 图片 name（本地mipmap）
     */
    @ColumnInfo(name = "img_name")
    var imgName: String?
    /**
     * 类型
     * 0：支出
     * 1：收入
     *
     * @see RecordType.TYPE_OUTLAY
     *
     * @see RecordType.TYPE_INCOME
     */
    var type: Int = 0
    /**
     * 排序
     */
    var ranking: Long = 0
    /**
     * 状态
     * 0：正常
     * 1：已删除
     *
     * @see RecordType.STATE_NORMAL
     *
     * @see RecordType.STATE_DELETED
     */
    var state: Int = 0
    /**
     * 是否选中，用于 UI
     */
    @Ignore
    var isChecked: Boolean = false

    @Ignore
    constructor(name: String, imgName: String, type: Int) {
        this.name = name
        this.imgName = imgName
        this.type = type
    }

    @Ignore
    constructor(name: String, imgName: String, type: Int, ranking: Long) {
        this.name = name
        this.imgName = imgName
        this.type = type
        this.ranking = ranking
    }

    constructor(id: Int, name: String, imgName: String, type: Int, ranking: Long) {
        this.id = id
        this.name = name
        this.imgName = imgName
        this.type = type
        this.ranking = ranking
    }

    companion object {
        @Ignore
        var TYPE_OUTLAY = 0
        @Ignore
        var TYPE_INCOME = 1
        @Ignore
        var STATE_NORMAL = 0
        @Ignore
        var STATE_DELETED = 1
    }
}
