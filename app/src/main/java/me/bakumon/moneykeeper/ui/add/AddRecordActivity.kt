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

package me.bakumon.moneykeeper.ui.add

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.customListAdapter
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.activity_add_record.*
import kotlinx.android.synthetic.main.layout_tool_bar.view.*
import kotlinx.android.synthetic.main.layout_type_choose.view.*
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.ErrorResource
import me.bakumon.moneykeeper.base.SuccessResource
import me.bakumon.moneykeeper.database.entity.Assets
import me.bakumon.moneykeeper.database.entity.Record
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.ui.assets.transfer.AssetsChooseViewBinder
import me.bakumon.moneykeeper.ui.common.BaseActivity
import me.bakumon.moneykeeper.utill.*
import me.bakumon.moneykeeper.widget.WidgetProvider
import me.drakeet.floo.Floo
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register
import java.util.*

/**
 * 记一笔
 *
 * @author bakumon https://bakumon.me
 * @date 2018/4/9
 */
class AddRecordActivity : BaseActivity() {
    private lateinit var mViewModel: AddRecordViewModel

    private var mCurrentChooseDate: Date? = DateUtils.getTodayDate()
    private val mCurrentChooseCalendar = Calendar.getInstance()
    private var mCurrentType: Int = 0

    private var mRecord: RecordWithType? = null

    private var mAssets: Assets? = null
    private var mOldAssets: Assets? = null

    /**
     * 连续记账
     */
    private var mIsSuccessive: Boolean = false

    override val layoutId: Int
        get() = R.layout.activity_add_record

    override fun onInitView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        typeChoose.rbLeft.setText(R.string.text_outlay)
        typeChoose.rbRight.setText(R.string.text_income)
    }

    override fun onInit(savedInstanceState: Bundle?) {
        mViewModel = getViewModel()
        mRecord = intent.getSerializableExtra(Router.ExtraKey.KEY_RECORD_BEAN) as RecordWithType?
        mIsSuccessive = intent.getBooleanExtra(Router.ExtraKey.KEY_IS_SUCCESSIVE, false)

        edtRemark.setOnEditorActionListener { _, _, _ ->
            SoftInputUtils.hideSoftInput(typePageOutlay)
            keyboard.setEditTextFocus()
            false
        }

        if (mRecord == null) {
            // 新建
            mCurrentType = RecordType.TYPE_OUTLAY
            toolbarLayout.tvTitle.text = getString(if (mIsSuccessive) R.string.text_add_record_successive else R.string.text_add_record)
            val assetsId = ConfigManager.assetId
            if (assetsId == -1) {
                updateAccountView(name = getString(R.string.text_no_choose_account))
            } else {
                getAssetsAccount(assetsId)
            }
        } else {
            // 修改
            mCurrentType = mRecord!!.mRecordTypes!![0].type
            toolbarLayout.tvTitle.text = getString(R.string.text_modify_record)
            edtRemark.setText(mRecord!!.remark)
            keyboard.setText(BigDecimalUtil.fen2YuanNoSeparator(mRecord!!.money))
            mCurrentChooseDate = mRecord!!.time
            mCurrentChooseCalendar.time = mCurrentChooseDate
            tvDate.text = DateUtils.getWordTime(mCurrentChooseDate!!)
            if (mRecord!!.assetsId == -1 || mRecord!!.assetsId == null) {
                updateAccountView(name = getString(R.string.text_no_choose_account))
            } else {
                getAssetsAccount(mRecord!!.assetsId!!)
            }
        }

        keyboard.mOnAffirmClickListener = {
            if (mRecord == null) {
                insertRecord(it)
            } else {
                modifyRecord(it)
            }
        }

        tvDate.setOnClickListener { showDatePickerDialog() }

        (typeChoose as RadioGroup).setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbLeft) {
                mCurrentType = RecordType.TYPE_OUTLAY
                typePageOutlay.visibility = View.VISIBLE
                typePageIncome.visibility = View.GONE
            } else {
                mCurrentType = RecordType.TYPE_INCOME
                typePageOutlay.visibility = View.GONE
                typePageIncome.visibility = View.VISIBLE
            }
        }

        llAccount.setOnClickListener { chooseAccount() }

        getAllRecordTypes()
    }

    private fun getAssetsAccount(id: Int) {
        mViewModel.getAssetsById(id).observe(this, Observer {
            if (it == null) {
                updateAccountView(name = getString(R.string.text_no_choose_account))
            } else {
                mAssets = it
                if (mRecord != null) {
                    // 修改记录
                    mOldAssets = it
                }
                updateAccountView(it.imgName, it.name)
            }
        })
    }

    private fun chooseAccount() {
        mViewModel.getAssets().observe(this, Observer {
            if (it != null) {
                showListDialog(it)
            }
        })
    }

    private var mDialog: MaterialDialog? = null
    private fun showListDialog(list: List<Assets>) {
        val adapter = MultiTypeAdapter()
        adapter.register(Assets::class, AssetsChooseViewBinder { assetsItemClick(it) })
        adapter.register(NoAccount::class, AssetsNoChooseViewBinder { noAssetsItemClick(it) })
        val items = Items()
        items.addAll(list)
        items.add(NoAccount(0, getString(R.string.text_no_choose_account), getString(R.string.text_no_choose_account_tip)))
        items.add(NoAccount(1, getString(R.string.text_new_choose_account)))
        adapter.items = items

        if (isDialogShow) {
            return
        }
        isDialogShow = true
        mDialog = MaterialDialog(this)
                .title(R.string.text_choose_account)
                .customListAdapter(adapter)
                .positiveButton(res = R.string.text_cancel)
                .onDismiss { isDialogShow = false }
        mDialog?.show()
    }

    private fun assetsItemClick(assets: Assets) {
        mAssets = assets
        updateAccountView(assets.imgName, assets.name)
        mDialog?.dismiss()
    }

    private fun noAssetsItemClick(noAccount: NoAccount) {
        if (noAccount.type == 0) {
            // 不选择账户
            mAssets = null
            updateAccountView(name = getString(R.string.text_no_choose_account))
        } else {
            // 添加账户
            Floo.navigation(this, Router.Url.URL_CHOOSE_ASSETS).start()
        }
        mDialog?.dismiss()
    }

    private fun updateAccountView(imgName: String = "", name: String) {
        if (isSubmitting) {
            return
        }
        if (imgName.isBlank()) {
            ivAccount.visibility = View.GONE
        } else {
            ivAccount.visibility = View.VISIBLE
            ivAccount.setImageResource(ResourcesUtil.getTypeImgId(this, imgName))
        }
        tvAccount.text = name
    }

    private var isDialogShow = false

    private fun showDatePickerDialog() {
        if (isDialogShow) {
            return
        }
        isDialogShow = true
        val dpd = DatePickerDialog.newInstance(
                { _, year, monthOfYear, dayOfMonth ->
                    mCurrentChooseDate = DateUtils.getDate(year, monthOfYear + 1, dayOfMonth)
                    mCurrentChooseCalendar.time = mCurrentChooseDate
                    tvDate.text = DateUtils.getWordTime(mCurrentChooseDate!!)
                }, mCurrentChooseCalendar)
        dpd.maxDate = Calendar.getInstance()
        dpd.isThemeDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        @Suppress("DEPRECATION")
        dpd.show(fragmentManager, TAG_PICKER_DIALOG)
        dpd.setOnDismissListener { isDialogShow = false }
    }

    /**
     * 正在提交
     */
    private var isSubmitting = false

    private fun insertRecord(text: String) {
        // 防止重复提交
        keyboard.setAffirmEnable(false)
        // 防止修改过程中 dialog 自动弹起
        isDialogShow = true
        isSubmitting = true
        val record = Record()
        record.money = BigDecimalUtil.yuan2FenBD(text)
        record.remark = edtRemark.text.toString().trim { it <= ' ' }
        record.time = mCurrentChooseDate
        record.createTime = Date()
        record.recordTypeId = if (mCurrentType == RecordType.TYPE_OUTLAY)
            typePageOutlay.currentItem!!.id
        else
            typePageIncome.currentItem!!.id

        mViewModel.insertRecord(mCurrentType, mAssets, record).observe(this, android.arch.lifecycle.Observer {
            when (it) {
                is SuccessResource<Boolean> -> insertRecordDone()
                is ErrorResource<Boolean> -> {
                    keyboard.setAffirmEnable(true)
                    isDialogShow = false
                    isSubmitting = false
                    ToastUtils.show(R.string.toast_add_record_fail)
                }
            }
        })
    }

    /**
     * 新增记账记录完成
     */
    private fun insertRecordDone() {
        isSubmitting = false
        // 更新上次使用的 assets id
        val assetsId = if (mAssets == null) -1 else mAssets!!.id!!
        ConfigManager.setAssetsId(assetsId)
        // 更新 widget
        WidgetProvider.updateWidget(this)
        if (mIsSuccessive) {
            // 继续记账，清空输入
            keyboard.setText("")
            edtRemark.setText("")
            keyboard.setAffirmEnable(true)
            isDialogShow = false
            ToastUtils.show(R.string.toast_success_record)
        } else {
            finish()
        }
    }

    private fun modifyRecord(text: String) {
        // 防止重复提交
        keyboard.setAffirmEnable(false)
        // 防止修改过程中 dialog 自动弹起
        isDialogShow = true
        isSubmitting = true
        val oldType = mRecord!!.mRecordTypes!![0].type
        mRecord!!.money = BigDecimalUtil.yuan2FenBD(text)
        mRecord!!.remark = edtRemark.text.toString().trim { it <= ' ' }
        mRecord!!.time = mCurrentChooseDate
        mRecord!!.recordTypeId = if (mCurrentType == RecordType.TYPE_OUTLAY)
            typePageOutlay.currentItem!!.id
        else
            typePageIncome.currentItem!!.id

        mViewModel.updateRecord(oldType, mCurrentType, mOldAssets, mAssets, mRecord!!).observe(this, Observer {
            when (it) {
                is SuccessResource<Boolean> -> {
                    // 更新 widget
                    WidgetProvider.updateWidget(this)
                    isSubmitting = false
                    finish()
                }
                is ErrorResource<Boolean> -> {
                    keyboard.setAffirmEnable(true)
                    isDialogShow = false
                    isSubmitting = false
                    ToastUtils.show(R.string.toast_modify_record_fail)
                }
            }
        })
    }

    private fun getAllRecordTypes() {
        mViewModel.allRecordTypes.observe(this, Observer {
            if (mCurrentType == RecordType.TYPE_OUTLAY) {
                (typeChoose as RadioGroup).check(R.id.rbLeft)
            } else {
                (typeChoose as RadioGroup).check(R.id.rbRight)
            }
            typePageOutlay.setItems(it, RecordType.TYPE_OUTLAY, mRecord)
            typePageIncome.setItems(it, RecordType.TYPE_INCOME, mRecord)
        })
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onBackPressed() {
        if (isSubmitting) {
            ToastUtils.show(R.string.toast_submit_now)
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        private const val TAG_PICKER_DIALOG = "Datepickerdialog"
    }
}
