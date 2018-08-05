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

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_type_sort.*
import kotlinx.android.synthetic.main.layout_tool_bar.view.*
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.common.BaseActivity
import me.bakumon.moneykeeper.utill.ToastUtils

/**
 * 类型排序
 *
 * @author bakumon https://bakumon.me
 */
class TypeSortActivity : BaseActivity() {

    private lateinit var mViewModel: TypeSortViewModel
    private lateinit var mAdapter: TypeSortAdapter
    private var mType: Int = 0

    override val layoutId: Int
        get() = R.layout.activity_type_sort

    override fun onInitView(savedInstanceState: Bundle?) {
        toolbarLayout.tvTitle.text = getString(R.string.text_drag_sort)
        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onInit(savedInstanceState: Bundle?) {
        mType = intent.getIntExtra(Router.ExtraKey.KEY_TYPE, RecordType.TYPE_OUTLAY)

        mAdapter = TypeSortAdapter(null)
        rvType.adapter = mAdapter

        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(mAdapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(rvType)

        // open drag
        mAdapter.enableDragItem(itemTouchHelper)

        mViewModel = getViewModel()
        initData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sort, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.action_done -> sortRecordTypes()
            android.R.id.home -> finish()
        }
        return true
    }

    private fun sortRecordTypes() {
        mDisposable.add(mViewModel.sortRecordTypes(mAdapter.data).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.finish() }) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（类型排序失败的时候）", throwable)
                        finish()
                    } else {
                        ToastUtils.show(R.string.toast_sort_fail)
                        Log.e(TAG, "类型排序失败", throwable)
                    }
                })
    }

    private fun initData() {
        mDisposable.add(mViewModel.getRecordTypes(mType).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recordTypes -> mAdapter.setNewData(recordTypes) }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_get_types_fail)
                    Log.e(TAG, "获取类型数据失败", throwable)
                })
    }

    companion object {
        private val TAG = TypeSortActivity::class.java.simpleName
    }
}
