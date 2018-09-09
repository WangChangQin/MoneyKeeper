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

package me.bakumon.moneykeeper.ui.assets.transfer

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.customListAdapter
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.activity_assets_transfer.*
import kotlinx.android.synthetic.main.layout_tool_bar.view.*
import kotlinx.android.synthetic.main.layout_transfer_account.view.*
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.ErrorResource
import me.bakumon.moneykeeper.base.SuccessResource
import me.bakumon.moneykeeper.database.entity.Assets
import me.bakumon.moneykeeper.database.entity.AssetsTransferRecord
import me.bakumon.moneykeeper.ui.common.BaseActivity
import me.bakumon.moneykeeper.utill.*
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register
import java.util.*

/**
 * AddAssetsActivity
 *
 * @author Bakumon https://bakumon.me
 */
class TransferActivity : BaseActivity() {

    private lateinit var mViewModel: TransferViewModel
    private var mDialog: MaterialDialog? = null

    private var mOutAssets: Assets? = null
    private var mInAssets: Assets? = null
    private lateinit var mCurrentType: String

    private var mCurrentChooseDate: Date? = DateUtils.getTodayDate()
    private val mCurrentChooseCalendar = Calendar.getInstance()

    override val layoutId: Int
        get() = R.layout.activity_assets_transfer

    override fun onInitView(savedInstanceState: Bundle?) {
        toolbarLayout.tvTitle.text = getString(R.string.text_transfer)
        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        outAccount.setOnClickListener { chooseAccount(TYPE_OUT_ACCOUNT) }
        inAccount.setOnClickListener { chooseAccount(TYPE_IN_ACCOUNT) }
        keyboard.mOnAffirmClickListener = {
            submit(it)
        }
        tvDate.setOnClickListener { showDatePickerDialog() }
    }

    override fun onInit(savedInstanceState: Bundle?) {
        outAccount.ivAccount.visibility = View.GONE
        outAccount.tvAccountName.text = getString(R.string.text_choose_out_account)
        outAccount.tvAccountName.setTextColor(resources.getColor(R.color.colorTextHint))
        outAccount.tvAccountRemark.visibility = View.GONE

        inAccount.ivAccount.visibility = View.GONE
        inAccount.tvAccountName.text = getString(R.string.text_choose_in_account)
        inAccount.tvAccountName.setTextColor(resources.getColor(R.color.colorTextHint))
        inAccount.tvAccountRemark.visibility = View.GONE

        mViewModel = getViewModel()
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

    private fun chooseAccount(type: String) {
        mCurrentType = type
        mViewModel.getAssets().observe(this, Observer {
            if (it != null) {
                showListDialog(it)
            }
        })
    }

    private fun showListDialog(list: List<Assets>) {
        val adapter = MultiTypeAdapter()
        adapter.register(Assets::class, AssetsChooseViewBinder { assetsItemClick(it) })
        val items = Items()
        items.addAll(list)
        adapter.items = items

        if (isDialogShow) {
            return
        }
        isDialogShow = true
        mDialog = MaterialDialog(this)
                .title(R.string.text_choose_bank)
                .customListAdapter(adapter)
                .positiveButton(res = R.string.text_cancel)
                .onDismiss { isDialogShow = false }
        mDialog?.show()
    }

    private fun assetsItemClick(assets: Assets) {
        if (mCurrentType == TYPE_OUT_ACCOUNT) {
            mOutAssets = assets
            outAccount.ivAccount.visibility = View.VISIBLE
            outAccount.ivAccount.setImageResource(ResourcesUtil.getTypeImgId(this, assets.imgName))
            outAccount.tvAccountName.text = assets.name
            outAccount.tvAccountName.setTextColor(resources.getColor(R.color.colorText))
            if (assets.remark.isBlank()) {
                outAccount.tvAccountRemark.visibility = View.GONE
            } else {
                outAccount.tvAccountRemark.visibility = View.VISIBLE
                outAccount.tvAccountRemark.text = assets.remark
            }
        } else {
            mInAssets = assets
            inAccount.ivAccount.visibility = View.VISIBLE
            inAccount.ivAccount.setImageResource(ResourcesUtil.getTypeImgId(this, assets.imgName))
            inAccount.tvAccountName.text = assets.name
            inAccount.tvAccountName.setTextColor(resources.getColor(R.color.colorText))
            if (assets.remark.isBlank()) {
                inAccount.tvAccountRemark.visibility = View.GONE
            } else {
                inAccount.tvAccountRemark.visibility = View.VISIBLE
                inAccount.tvAccountRemark.text = assets.remark
            }
        }
        mDialog?.dismiss()
    }

    /**
     * 正在提交
     */
    private var isSubmitting = false

    private fun submit(input: String) {
        if (mOutAssets == null) {
            ViewUtil.startShake(outAccount)
            return
        }
        if (mInAssets == null) {
            ViewUtil.startShake(inAccount)
            return
        }
        if (mOutAssets?.id == mInAssets?.id) {
            ToastUtils.show(R.string.toast_choose_account)
            return
        }
        if (mCurrentChooseDate == null) {
            ToastUtils.show(R.string.toast_date_null)
            return
        }
        // 防止修改过程中 dialog 自动弹起
        isDialogShow = true
        isSubmitting = true
        // 防止重复提交
        keyboard.setAffirmEnable(false)
        val transferRecord = AssetsTransferRecord(mOutAssets!!.id!!, mInAssets!!.id!!,
                BigDecimalUtil.yuan2FenBD(input), mCurrentChooseDate!!, edtRemark.text.toString())
        mViewModel.addTransferRecord(mOutAssets!!, mInAssets!!, transferRecord).observe(this, Observer {
            when (it) {
                is SuccessResource<Boolean> -> {
                    isSubmitting = false
                    finish()
                }
                is ErrorResource<Boolean> -> {
                    isDialogShow = false
                    isSubmitting = false
                    keyboard.setAffirmEnable(true)
                    ToastUtils.show(R.string.toast_modify_record_fail)
                }
            }
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
            ToastUtils.show(R.string.toast_transfer_now)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG_PICKER_DIALOG = "Datepickerdialog"
        private const val TYPE_OUT_ACCOUNT = "type_out_account"
        private const val TYPE_IN_ACCOUNT = "type_in_account"
    }
}
