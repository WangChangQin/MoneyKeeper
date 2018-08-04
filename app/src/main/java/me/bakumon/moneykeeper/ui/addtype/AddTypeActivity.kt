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

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_type.*
import kotlinx.android.synthetic.main.layout_tool_bar.view.*
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.ui.common.BaseActivity
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register

/**
 * 添加或修改记账类型
 *
 * @author Bakumon https://bakumon.me
 */
class AddTypeActivity : BaseActivity() {

    private lateinit var mViewModel: AddTypeViewModel
    private lateinit var adapter: MultiTypeAdapter

    private var mType: Int = 0
    private var mRecordType: RecordType? = null
    private var mCurrentItem: TypeImgBean? = null

    override val layoutId: Int
        get() = R.layout.activity_add_type

    override fun onInitView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    @SuppressLint("SetTextI18n")
    override fun onInit(savedInstanceState: Bundle?) {
        mType = intent.getIntExtra(Router.ExtraKey.KEY_TYPE, RecordType.TYPE_OUTLAY)
        mRecordType = intent.getSerializableExtra(Router.ExtraKey.KEY_TYPE_BEAN) as RecordType?

        val prefix = if (mRecordType == null) getString(R.string.text_add) else getString(R.string.text_modify)
        val type = if (mType == RecordType.TYPE_OUTLAY) getString(R.string.text_outlay_type) else getString(R.string.text_income_type)

        toolbarLayout.tvTitle.text = prefix + type

        edt_type_name.setText(mRecordType?.name)
        edt_type_name.setSelection(edt_type_name.text.length)

        adapter = MultiTypeAdapter()
        adapter.register(TypeImgBean::class, TypeImgViewBinder({ checkedItem(it) }))
        rv_type.adapter = adapter

        mViewModel = getViewModel()
        getAllTypeImg()
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

    private fun checkedItem(position: Int) {
        for (i in 0 until adapter.items.size) {
            (adapter.items[i] as TypeImgBean).isChecked = i == position
        }
        mCurrentItem = adapter.items[position] as TypeImgBean
        val resId = resources.getIdentifier(mCurrentItem?.imgName, "mipmap", packageName)
        iv_type.setImageResource(resId)
        adapter.notifyDataSetChanged()
    }

    private fun getAllTypeImg() {
        mDisposable.add(mViewModel.getAllTypeImgBeans(mType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ typeImgBeans ->
                    setItems(typeImgBeans)
                }) { throwable ->
                    ToastUtils.show(R.string.toast_type_img_fail)
                    Log.e(TAG, "类型图片获取失败", throwable)
                })
    }

    private fun setItems(typeImgBeans: List<TypeImgBean>) {
        val items = Items()

        var checkedPosition = 0
        for (i in typeImgBeans.indices) {
            if (TextUtils.equals(mRecordType?.imgName, typeImgBeans[i].imgName)) {
                checkedPosition = i
                break
            }
        }
        items.addAll(typeImgBeans)
        adapter.items = items
        checkedItem(checkedPosition)
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
        val text = edt_type_name.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(text)) {
            val animation = AnimationUtils.loadAnimation(App.instance, R.anim.shake)
            edt_type_name.startAnimation(animation)
            isSaveEnable = true
            return
        }
        if (mCurrentItem == null) {
            return
        }
        mDisposable.add(mViewModel.saveRecordType(mRecordType, mType, mCurrentItem!!.imgName, text)
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
    }
}
