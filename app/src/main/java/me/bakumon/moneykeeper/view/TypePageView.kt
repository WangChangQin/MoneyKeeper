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

package me.bakumon.moneykeeper.view

import android.content.Context
import android.databinding.DataBindingUtil
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.databinding.LayoutTypePageBinding
import me.bakumon.moneykeeper.ui.add.TypeAdapter
import me.bakumon.moneykeeper.view.pagerlayoutmanager.PagerGridLayoutManager
import me.bakumon.moneykeeper.view.pagerlayoutmanager.PagerGridSnapHelper

/**
 * 翻页的 recyclerView + 指示器
 *
 * @author Bakumon https://bakumon.me
 */
class TypePageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var mBinding: LayoutTypePageBinding
    private lateinit var mAdapter: TypeAdapter
    private lateinit var mLayoutManager: PagerGridLayoutManager
    private var mCurrentTypeIndex = -1

    val currentItem: RecordType?
        get() = mAdapter.currentItem

    init {
        init(context)
    }

    private fun init(context: Context) {
        orientation = LinearLayout.VERTICAL
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_type_page, this, true)

        // 1.水平分页布局管理器
        mLayoutManager = PagerGridLayoutManager(
                ROW, COLUMN, PagerGridLayoutManager.HORIZONTAL)
        mBinding.recyclerView.layoutManager = mLayoutManager

        // 2.设置滚动辅助工具
        val pageSnapHelper = PagerGridSnapHelper()
        pageSnapHelper.attachToRecyclerView(mBinding.recyclerView)


        mAdapter = TypeAdapter(null)
        mAdapter.setOnItemClickListener { _, _, position ->
            mAdapter.clickItem(position)
            mCurrentTypeIndex = position
        }
        mBinding.recyclerView.adapter = mAdapter

        mLayoutManager.setPageListener(object : PagerGridLayoutManager.PageListener {
            internal var currentPageIndex: Int = 0
            internal var pageSize: Int = 0

            override fun onPageSizeChanged(pageSize: Int) {
                this.pageSize = pageSize
                setIndicator()
            }

            override fun onPageSelect(pageIndex: Int) {
                currentPageIndex = pageIndex
                setIndicator()
            }

            private fun setIndicator() {
                if (pageSize > 1) {
                    mBinding.indicator.visibility = View.VISIBLE
                    mBinding.indicator.setTotal(pageSize, currentPageIndex)
                } else {
                    mBinding.indicator.visibility = View.INVISIBLE
                }
            }
        })
    }

    fun setNewData(data: List<RecordType>?, type: Int) {
        mAdapter.setNewData(data, type)
    }

    /**
     * 该方法只改变一次
     */
    fun initCheckItem(record: RecordWithType?) {
        if (mCurrentTypeIndex == -1) {
            mCurrentTypeIndex = 0
            var isTypeExist = 0
            val size = mAdapter.data.size
            if (record != null && size > 0) {
                for (i in 0 until size) {
                    if (record.mRecordTypes!![0].id == mAdapter.data[i].id) {
                        mCurrentTypeIndex = i
                        isTypeExist++
                        break
                    }
                }
                if (isTypeExist != 0) {
                    // 选中对应的页
                    val pageIndex = mCurrentTypeIndex / (ROW * COLUMN)
                    post { mLayoutManager.smoothScrollToPage(pageIndex) }
                } else {
                    showTypeNotExistTip()
                }
            }
            mAdapter.clickItem(mCurrentTypeIndex)
        }
    }

    /**
     * 提示用户该记录的类型已经被删除
     */
    private fun showTypeNotExistTip() {
        MaterialDialog.Builder(context)
                .content(R.string.text_tip_type_delete)
                .positiveText(R.string.text_button_know)
                .show()
    }

    companion object {

        private const val ROW = 2
        private const val COLUMN = 4
    }

}
