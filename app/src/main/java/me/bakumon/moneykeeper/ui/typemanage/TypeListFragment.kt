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

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.ErrorResource
import me.bakumon.moneykeeper.base.SuccessResource
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.common.AbsListFragment
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register

/**
 * 类型列表-支出或收入
 *
 * @author Bakumon https://bakumon.me
 */
class TypeListFragment : AbsListFragment() {

    private var mType: Int? = RecordType.TYPE_OUTLAY
    private lateinit var mViewModel: TypeManageViewModel

    override fun onAdapterCreated(adapter: MultiTypeAdapter) {
        val viewBinder = TypeListViewBinder({ onClickItem(it) }, { onLongClickItem(it) })
        adapter.register(RecordType::class, viewBinder)
    }

    override fun onItemsCreated(items: Items) {

    }

    override fun onParentInitDone(recyclerView: RecyclerView, savedInstanceState: Bundle?) {
        recyclerView.setPadding(0, 0, 0, 200)
        mType = arguments?.getInt(Router.ExtraKey.KEY_TYPE, RecordType.TYPE_OUTLAY)
        mViewModel = getViewModel()
        initData()
    }

    override fun lazyInitData() {

    }

    private fun initData() {
        mViewModel.getRecordTypes(mType!!).observe(this, Observer {
            if (it != null){
                setItems(it)
            }
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
                .onPositive { _, _ -> deleteType(recordType) }
                .show()
    }

    private fun deleteType(recordType: RecordType) {
        mViewModel.deleteRecordType(recordType).observe(this, Observer {
            when (it) {
                is SuccessResource<Boolean> -> {
                }
                is ErrorResource<Boolean> -> {
                    ToastUtils.show(R.string.toast_delete_fail)
                }
            }
        })
    }

    companion object {
        fun newInstance(type: Int): TypeListFragment {
            val fragment = TypeListFragment()
            val bundle = Bundle()
            bundle.putInt(Router.ExtraKey.KEY_TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }
}
