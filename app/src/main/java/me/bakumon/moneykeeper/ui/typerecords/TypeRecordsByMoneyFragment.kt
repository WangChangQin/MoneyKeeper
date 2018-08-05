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

package me.bakumon.moneykeeper.ui.typerecords

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_list.*
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.common.BaseFragment
import me.bakumon.moneykeeper.ui.common.Empty
import me.bakumon.moneykeeper.ui.common.EmptyViewBinder
import me.bakumon.moneykeeper.ui.home.RecordViewBinder
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register

/**
 * 某一类型记账记录
 * 按时间排序
 *
 * @author Bakumon https://bakumon.me
 */
class TypeRecordsByMoneyFragment : BaseFragment() {

    private lateinit var mViewModel: TypeRecordsViewModel
    private lateinit var mAdapter: MultiTypeAdapter

    private var mRecordType: Int = 0
    private var mRecordTypeId: Int = 0
    private var mYear: Int = 0
    private var mMonth: Int = 0

    override val layoutId: Int
        get() = R.layout.layout_list

    override fun onInit(savedInstanceState: Bundle?) {
        val bundle = arguments
        if (bundle != null) {
            mRecordType = bundle.getInt(Router.ExtraKey.KEY_RECORD_TYPE)
            mRecordTypeId = bundle.getInt(Router.ExtraKey.KEY_RECORD_TYPE_ID)
            mYear = bundle.getInt(Router.ExtraKey.KEY_YEAR)
            mMonth = bundle.getInt(Router.ExtraKey.KEY_MONTH)
        }

        mAdapter = MultiTypeAdapter()
        mAdapter.register(RecordWithType::class, RecordByMoneyViewBinder({ deleteRecord(it) }))
        mAdapter.register(Empty::class, EmptyViewBinder())
        recyclerView.adapter = mAdapter

        mViewModel = getViewModel()
        getData()
    }

    private fun deleteRecord(record: RecordWithType) {
        mDisposable.add(mViewModel.deleteRecord(record)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }
                ) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（删除记账记录失败的时候）", throwable)
                    } else {
                        ToastUtils.show(R.string.toast_record_delete_fail)
                        Log.e(TAG, "删除记账记录失败", throwable)
                    }
                })
    }

    override fun lazyInitData() {

    }

    private fun getData() {
        mDisposable.add(mViewModel.getRecordWithTypes(1, mRecordType, mRecordTypeId, mYear, mMonth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setItems(it)
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_records_fail)
                    Log.e(TAG, "获取记录列表失败", throwable)
                })
    }

    private fun setItems(recordWithTypes: List<RecordWithType>) {
        val items = Items()
        if (recordWithTypes.isEmpty()) {
            items.add(Empty(getString(R.string.text_empty_tip), Gravity.CENTER))
        } else {
            items.addAll(recordWithTypes)
        }
        mAdapter.items = items
        mAdapter.notifyDataSetChanged()
    }

    companion object {
        private val TAG = TypeRecordsByMoneyFragment::class.java.simpleName

        fun newInstance(recordType: Int, recordTypeId: Int, year: Int, month: Int): TypeRecordsByMoneyFragment {
            val fragment = TypeRecordsByMoneyFragment()
            val bundle = Bundle()
            bundle.putInt(Router.ExtraKey.KEY_RECORD_TYPE, recordType)
            bundle.putInt(Router.ExtraKey.KEY_RECORD_TYPE_ID, recordTypeId)
            bundle.putInt(Router.ExtraKey.KEY_YEAR, year)
            bundle.putInt(Router.ExtraKey.KEY_MONTH, month)
            fragment.arguments = bundle
            return fragment
        }
    }
}
