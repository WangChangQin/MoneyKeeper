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

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseFragment
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.databinding.FragmentTypeRecordsBinding
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.home.HomeAdapter
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo

/**
 * 某一类型记账记录
 * 按金额或时间排序
 *
 * @author Bakumon https://bakumon.me
 */
class TypeRecordsFragment : BaseFragment() {

    private lateinit var mBinding: FragmentTypeRecordsBinding
    private lateinit var mViewModel: TypeRecordsViewModel
    private lateinit var mSortTimeAdapter: HomeAdapter
    private lateinit var mSortMoneyAdapter: RecordAdapter

    private var mSortType: Int = 0
    private var mRecordType: Int = 0
    private var mRecordTypeId: Int = 0
    private var mYear: Int = 0
    private var mMonth: Int = 0

    override val layoutId: Int
        get() = R.layout.fragment_type_records

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(TypeRecordsViewModel::class.java)

        val bundle = arguments
        if (bundle != null) {
            mSortType = bundle.getInt(Router.ExtraKey.KEY_SORT_TYPE)
            mRecordType = bundle.getInt(Router.ExtraKey.KEY_RECORD_TYPE)
            mRecordTypeId = bundle.getInt(Router.ExtraKey.KEY_RECORD_TYPE_ID)
            mYear = bundle.getInt(Router.ExtraKey.KEY_YEAR)
            mMonth = bundle.getInt(Router.ExtraKey.KEY_MONTH)
        }

        initView()

        getData()
    }

    private fun initView() {
        mBinding.rvRecords.layoutManager = LinearLayoutManager(context)
        if (mSortType == SORT_TIME) {
            mSortTimeAdapter = HomeAdapter(null)
            mBinding.rvRecords.adapter = mSortTimeAdapter
            mSortTimeAdapter.setOnItemChildLongClickListener { _, _, position ->
                showOperateDialog(mSortTimeAdapter.data[position])
                false
            }
        } else {
            mSortMoneyAdapter = RecordAdapter(null)
            mBinding.rvRecords.adapter = mSortMoneyAdapter
            mSortMoneyAdapter.setOnItemChildLongClickListener { _, _, position ->
                showOperateDialog(mSortMoneyAdapter.data[position])
                false
            }
        }
    }

    private fun showOperateDialog(record: RecordWithType) {
        if (context == null) {
            return
        }
        MaterialDialog.Builder(context!!)
                .items(getString(R.string.text_modify), getString(R.string.text_delete))
                .itemsCallback({ _, _, which, _ ->
                    if (which == 0) {
                        modifyRecord(record)
                    } else {
                        deleteRecord(record)
                    }
                })
                .show()
    }

    private fun modifyRecord(record: RecordWithType) {
        if (context == null) {
            return
        }
        Floo.navigation(context!!, Router.Url.URL_ADD_RECORD)
                .putExtra(Router.ExtraKey.KEY_RECORD_BEAN, record)
                .start()
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
        mDisposable.add(mViewModel.getRecordWithTypes(mSortType, mRecordType, mRecordTypeId, mYear, mMonth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recordWithTypes ->
                    if (mSortType == 0) {
                        mSortTimeAdapter.setNewData(recordWithTypes)
                        if (recordWithTypes.isEmpty()) {
                            mSortTimeAdapter.emptyView = inflate(R.layout.layout_record_empty)
                        }
                    } else {
                        mSortMoneyAdapter.setNewData(recordWithTypes)
                        if (recordWithTypes.isEmpty()) {
                            mSortMoneyAdapter.emptyView = inflate(R.layout.layout_record_empty)
                        }
                    }
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_records_fail)
                    Log.e(TAG, "获取记录列表失败", throwable)
                })
    }

    companion object {
        private val TAG = TypeRecordsFragment::class.java.simpleName
        const val SORT_TIME = 0
        const val SORT_MONEY = 1

        fun newInstance(sortType: Int, recordType: Int, recordTypeId: Int, year: Int, month: Int): TypeRecordsFragment {
            val fragment = TypeRecordsFragment()
            val bundle = Bundle()
            bundle.putInt(Router.ExtraKey.KEY_SORT_TYPE, sortType)
            bundle.putInt(Router.ExtraKey.KEY_RECORD_TYPE, recordType)
            bundle.putInt(Router.ExtraKey.KEY_RECORD_TYPE_ID, recordTypeId)
            bundle.putInt(Router.ExtraKey.KEY_YEAR, year)
            bundle.putInt(Router.ExtraKey.KEY_MONTH, month)
            fragment.arguments = bundle
            return fragment
        }
    }
}
