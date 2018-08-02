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

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.databinding.ActivityTypeManageBinding
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo

/**
 * 类型管理
 *
 * @author bakumon https://bakumon.me
 * @date 2018/5/3
 */
class TypeManageActivity : BaseActivity() {

    private lateinit var mBinding: ActivityTypeManageBinding
    private lateinit var mViewModel: TypeManageViewModel
    private lateinit var mAdapter: TypeManageAdapter

    private var mRecordTypes: List<RecordType>? = null

    private var mCurrentType: Int = 0

    override val layoutId: Int
        get() = R.layout.activity_type_manage

    override fun onInitView(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(TypeManageViewModel::class.java)

        initView()
        initData()
    }

    private fun initView() {
        mCurrentType = intent.getIntExtra(Router.ExtraKey.KEY_TYPE, RecordType.TYPE_OUTLAY)

        mBinding.toolbarLayout?.title = getString(R.string.text_type_manage)
        setSupportActionBar(mBinding.toolbarLayout?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mBinding.rvType.layoutManager = LinearLayoutManager(this)
        mAdapter = TypeManageAdapter(null)
        mBinding.rvType.adapter = mAdapter

        mAdapter.setOnItemLongClickListener { adapter, view, position ->
            if (adapter.data.size > 1) {
                showDeleteDialog(mAdapter.data[position])
            } else {
                ToastUtils.show(R.string.toast_least_one_type)
            }
            true
        }

        mAdapter.setOnItemClickListener { _, _, position ->
            Floo.navigation(this, Router.Url.URL_ADD_TYPE)
                    .putExtra(Router.ExtraKey.KEY_TYPE_BEAN, mAdapter.getItem(position))
                    .putExtra(Router.ExtraKey.KEY_TYPE, mCurrentType)
                    .start()
        }

        mBinding.typeChoice?.rgType?.setOnCheckedChangeListener { _, checkedId ->
            mCurrentType = if (checkedId == R.id.rb_outlay) RecordType.TYPE_OUTLAY else RecordType.TYPE_INCOME
            mAdapter.setNewData(mRecordTypes, mCurrentType)
        }

    }

    // TODO 少于一个时，隐藏排序 action
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_type_manage, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.action_sort -> Floo.navigation(this, Router.Url.URL_TYPE_SORT)
                    .putExtra(Router.ExtraKey.KEY_TYPE, mCurrentType)
                    .start()
            android.R.id.home -> finish()
        }
        return true
    }

    private fun showDeleteDialog(recordType: RecordType) {
        MaterialDialog.Builder(this)
                .title(getString(R.string.text_delete) + recordType.name!!)
                .content(R.string.text_delete_type_note)
                .positiveText(R.string.text_affirm_delete)
                .negativeText(R.string.text_cancel)
                .onPositive({ _, _ -> deleteType(recordType) })
                .show()
    }

    fun addType(view: View) {
        Floo.navigation(this, Router.Url.URL_ADD_TYPE)
                .putExtra(Router.ExtraKey.KEY_TYPE, mCurrentType)
                .start()
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

    private fun initData() {
        mDisposable.add(mViewModel.allRecordTypes.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recordTypes ->
                    mRecordTypes = recordTypes
                    val id = if (mCurrentType == RecordType.TYPE_OUTLAY) R.id.rb_outlay else R.id.rb_income
                    mBinding.typeChoice?.rgType?.clearCheck()
                    mBinding.typeChoice?.rgType?.check(id)
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_get_types_fail)
                    Log.e(TAG, "获取类型数据失败", throwable)
                })
    }

    companion object {

        private val TAG = TypeManageActivity::class.java.simpleName
    }
}
