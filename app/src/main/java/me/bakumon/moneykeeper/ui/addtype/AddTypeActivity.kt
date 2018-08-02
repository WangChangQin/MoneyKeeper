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

package me.bakumon.moneykeeper.ui.addtype

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.databinding.ActivityAddTypeBinding
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.utill.ToastUtils

/**
 * 添加或修改记账类型
 *
 * @author Bakumon https://bakumon.me
 */
class AddTypeActivity : BaseActivity() {

    private lateinit var mBinding: ActivityAddTypeBinding
    private lateinit var mViewModel: AddTypeViewModel
    private lateinit var mAdapter: TypeImgAdapter

    private var mType: Int = 0
    private var mRecordType: RecordType? = null

    override val layoutId: Int
        get() = R.layout.activity_add_type

    override fun onInitView(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddTypeViewModel::class.java)

        initView()
        initData()
    }

    private fun initView() {
        mType = intent.getIntExtra(Router.ExtraKey.KEY_TYPE, RecordType.TYPE_OUTLAY)
        mRecordType = intent.getSerializableExtra(Router.ExtraKey.KEY_TYPE_BEAN) as RecordType?

        val prefix = if (mRecordType == null) getString(R.string.text_add) else getString(R.string.text_modify)
        val type = if (mType == RecordType.TYPE_OUTLAY) getString(R.string.text_outlay_type) else getString(R.string.text_income_type)

        mBinding.edtTypeName.setText(mRecordType?.name)
        mBinding.edtTypeName.setSelection(mBinding.edtTypeName.text.length)

        mBinding.toolbarLayout?.title = prefix + type
        setSupportActionBar(mBinding.toolbarLayout?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mBinding.rvType.layoutManager = GridLayoutManager(this, COLUMN)
        mAdapter = TypeImgAdapter(null)
        mBinding.rvType.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position -> checkItem(position) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_type, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.action_save -> saveType()
            android.R.id.home -> finish()
        }
        return true
    }

    private fun checkItem(position: Int) {
        mAdapter.checkItem(position)
        val resId = resources.getIdentifier(mAdapter.currentItem!!.imgName, "mipmap", packageName)
        mBinding.ivType.setImageResource(resId)
    }

    private fun initData() {
        getAllTypeImg()
    }

    private fun getAllTypeImg() {
        mDisposable.add(mViewModel.getAllTypeImgBeans(mType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ typeImgBeans ->
                    mAdapter.setNewData(typeImgBeans)
                    if (mRecordType == null) {
                        checkItem(0)
                    } else {
                        for (i in typeImgBeans.indices) {
                            if (TextUtils.equals(mRecordType!!.imgName, typeImgBeans[i].imgName)) {
                                checkItem(i)
                                return@subscribe
                            }
                        }
                    }
                }) { throwable ->
                    ToastUtils.show(R.string.toast_type_img_fail)
                    Log.e(TAG, "类型图片获取失败", throwable)
                })
    }

    /**
     * 防止重复点击
     */
    private var isSaveEnable = true

    private fun saveType() {
        if (!isSaveEnable) {
            return
        }
        isSaveEnable = false
        val text = mBinding.edtTypeName.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(text)) {
            val animation = AnimationUtils.loadAnimation(App.instance, R.anim.shake)
            mBinding.edtTypeName.startAnimation(animation)
            isSaveEnable = true
            return
        }
        val bean = mAdapter.currentItem
        mDisposable.add(mViewModel.saveRecordType(mRecordType, mType, bean!!.imgName, text)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.finish() }) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（类型保存失败的时候）", throwable)
                        finish()
                    } else {
                        isSaveEnable = true
                        val failTip = if (TextUtils.isEmpty(throwable.message)) getString(R.string.toast_type_save_fail) else throwable.message
                        ToastUtils.show(failTip)
                        Log.e(TAG, "类型保存失败", throwable)
                    }
                })
    }

    companion object {

        private val TAG = AddTypeActivity::class.java.simpleName

        private const val COLUMN = 4
    }
}
