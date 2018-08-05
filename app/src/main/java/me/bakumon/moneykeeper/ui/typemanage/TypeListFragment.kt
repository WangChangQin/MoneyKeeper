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

package me.bakumon.moneykeeper.ui.typemanage

import android.os.Bundle
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_list.*
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.common.BaseFragment
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register

/**
 * 类型列表
 *
 * @author Bakumon https://bakumon.me
 */
class TypeListFragment : BaseFragment() {

    private var mType: Int? = RecordType.TYPE_OUTLAY
    private lateinit var mViewModel: TypeManageViewModel
    private lateinit var mAdapter: MultiTypeAdapter

    override val layoutId: Int
        get() = R.layout.layout_list

    override fun onInit(savedInstanceState: Bundle?) {
        mType = arguments?.getInt(Router.ExtraKey.KEY_TYPE, RecordType.TYPE_OUTLAY)

        mAdapter = MultiTypeAdapter()
        val viewModel = TypeListViewBinder({ onClickItem(it) }, { onLongClickItem(it) })
        mAdapter.register(RecordType::class, viewModel)
        recyclerView.adapter = mAdapter
        recyclerView.setPadding(0, 0, 0, 180)

        mViewModel = getViewModel()
        initData()
    }

    override fun lazyInitData() {

    }

    private fun initData() {
        mDisposable.add(mViewModel.getRecordTypes(mType!!).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ setItems(it) }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_get_types_fail)
                    Log.e(TAG, "获取类型数据失败", throwable)
                })
    }

    private fun setItems(recordTypes: List<RecordType>) {
        val items = Items()
        items.addAll(recordTypes)
        mAdapter.items = items
        mAdapter.notifyDataSetChanged()
    }

    private fun onClickItem(item: RecordType) {
        if (context == null) {
            return
        }
        Floo.navigation(context!!, Router.Url.URL_ADD_TYPE)
                .putExtra(Router.ExtraKey.KEY_TYPE_BEAN, item)
                .putExtra(Router.ExtraKey.KEY_TYPE, item.type)
                .start()
    }

    private fun onLongClickItem(item: RecordType) {
        if (mAdapter.items.size > 1) {
            showDeleteDialog(item)
        } else {
            ToastUtils.show(R.string.toast_least_one_type)
        }
    }

    private fun showDeleteDialog(recordType: RecordType) {
        if (context == null) {
            return
        }
        MaterialDialog.Builder(context!!)
                .title(getString(R.string.text_delete) + recordType.name!!)
                .content(R.string.text_delete_type_note)
                .positiveText(R.string.text_affirm_delete)
                .negativeText(R.string.text_cancel)
                .onPositive({ _, _ -> deleteType(recordType) })
                .show()
    }

    private fun deleteType(recordType: RecordType) {
        mDisposable.add(mViewModel.deleteRecordType(recordType).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }
                ) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（类型删除失败的时候）", throwable)
                    } else {
                        ToastUtils.show(R.string.toast_delete_fail)
                        Log.e(TAG, "类型删除失败", throwable)
                    }
                })
    }

    companion object {
        private val TAG = TypeListFragment::class.java.simpleName

        fun newInstance(type: Int): TypeListFragment {
            val fragment = TypeListFragment()
            val bundle = Bundle()
            bundle.putInt(Router.ExtraKey.KEY_TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }
}
