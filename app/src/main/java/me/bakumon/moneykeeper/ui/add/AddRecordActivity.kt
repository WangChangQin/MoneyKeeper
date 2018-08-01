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

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.View
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.database.entity.Record
import me.bakumon.moneykeeper.database.entity.RecordType
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.databinding.ActivityAddRecordBinding
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.utill.BigDecimalUtil
import me.bakumon.moneykeeper.utill.DateUtils
import me.bakumon.moneykeeper.utill.SoftInputUtils
import me.bakumon.moneykeeper.utill.ToastUtils
import java.util.*

/**
 * HomeActivity
 *
 * @author bakumon https://bakumon.me
 * @date 2018/4/9
 */
class AddRecordActivity : BaseActivity() {

    private lateinit var mBinding: ActivityAddRecordBinding

    private lateinit var mViewModel: AddRecordViewModel

    private var mCurrentChooseDate: Date? = DateUtils.getTodayDate()
    private val mCurrentChooseCalendar = Calendar.getInstance()
    private var mCurrentType: Int = 0

    private var mRecord: RecordWithType? = null
    /**
     * 连续记账
     */
    private var mIsSuccessive: Boolean = false

    override val layoutId: Int
        get() = R.layout.activity_add_record

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddRecordViewModel::class.java)

        initView()
        initData()
    }

    private fun initData() {
        getAllRecordTypes()
    }

    private fun initView() {
        setSupportActionBar(mBinding.toolbarLayout?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mRecord = intent.getSerializableExtra(Router.ExtraKey.KEY_RECORD_BEAN) as RecordWithType?
        mIsSuccessive = intent.getBooleanExtra(Router.ExtraKey.KEY_IS_SUCCESSIVE, false)

        mBinding.edtRemark.setOnEditorActionListener { _, _, _ ->
            SoftInputUtils.hideSoftInput(mBinding.typePageOutlay)
            mBinding.keyboard.setEditTextFocus()
            false
        }

        if (mRecord == null) {
            mCurrentType = RecordType.TYPE_OUTLAY
            mBinding.toolbarLayout?.title = getString(if (mIsSuccessive) R.string.text_add_record_successive else R.string.text_add_record)
        } else {
            mCurrentType = mRecord!!.mRecordTypes!![0].type
            mBinding.toolbarLayout?.title = getString(R.string.text_modify_record)
            mBinding.edtRemark.setText(mRecord!!.remark)
            mBinding.keyboard.setText(BigDecimalUtil.fen2YuanNoSeparator(mRecord!!.money))
            mCurrentChooseDate = mRecord!!.time
            mCurrentChooseCalendar.time = mCurrentChooseDate
            mBinding.qmTvDate.text = DateUtils.getWordTime(mCurrentChooseDate!!)
        }

        mBinding.keyboard.mOnAffirmClickListener = {
            if (mRecord == null) {
                insertRecord(it)
            } else {
                modifyRecord(it)
            }
        }

        mBinding.qmTvDate.setOnClickListener {
            val dpd = DatePickerDialog.newInstance(
                    { _, year, monthOfYear, dayOfMonth ->
                        mCurrentChooseDate = DateUtils.getDate(year, monthOfYear + 1, dayOfMonth)
                        mCurrentChooseCalendar.time = mCurrentChooseDate
                        mBinding.qmTvDate.text = DateUtils.getWordTime(mCurrentChooseDate!!)
                    }, mCurrentChooseCalendar)
            dpd.maxDate = Calendar.getInstance()
            dpd.show(fragmentManager, TAG_PICKER_DIALOG)
        }
        mBinding.typeChoice?.rgType?.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.rb_outlay) {
                mCurrentType = RecordType.TYPE_OUTLAY
                mBinding.typePageOutlay.visibility = View.VISIBLE
                mBinding.typePageIncome.visibility = View.GONE
            } else {
                mCurrentType = RecordType.TYPE_INCOME
                mBinding.typePageOutlay.visibility = View.GONE
                mBinding.typePageIncome.visibility = View.VISIBLE
            }

        }
    }

    private fun insertRecord(text: String) {
        // 防止重复提交
        mBinding.keyboard.setAffirmEnable(false)
        val record = Record()
        record.money = BigDecimalUtil.yuan2FenBD(text)
        record.remark = mBinding.edtRemark.text.toString().trim { it <= ' ' }
        record.time = mCurrentChooseDate
        record.createTime = Date()
        record.recordTypeId = if (mCurrentType == RecordType.TYPE_OUTLAY)
            mBinding.typePageOutlay.currentItem!!.id
        else
            mBinding.typePageIncome.currentItem!!.id

        mDisposable.add(mViewModel.insertRecord(record, mCurrentType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.insertRecordDone() }
                ) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（新增记录失败的时候）", throwable)
                        insertRecordDone()
                    } else {
                        Log.e(TAG, "新增记录失败", throwable)
                        mBinding.keyboard.setAffirmEnable(true)
                        ToastUtils.show(R.string.toast_add_record_fail)
                    }
                })
    }

    /**
     * 新增记账记录完成
     */
    private fun insertRecordDone() {
        if (mIsSuccessive) {
            // 继续记账，清空输入
            mBinding.keyboard.setText("")
            mBinding.edtRemark.setText("")
            mBinding.keyboard.setAffirmEnable(true)
            ToastUtils.show(R.string.toast_success_record)
        } else {
            finish()
        }
    }

    private fun modifyRecord(text: String) {
        // 防止重复提交
        mBinding.keyboard.setAffirmEnable(false)

        val oldType = mRecord!!.mRecordTypes!![0].type
        val oldMoney = mRecord!!.money!!
        val newType = mCurrentType

        mRecord!!.money = BigDecimalUtil.yuan2FenBD(text)
        mRecord!!.remark = mBinding.edtRemark.text.toString().trim { it <= ' ' }
        mRecord!!.time = mCurrentChooseDate
        mRecord!!.recordTypeId = if (mCurrentType == RecordType.TYPE_OUTLAY)
            mBinding.typePageOutlay.currentItem!!.id
        else
            mBinding.typePageIncome.currentItem!!.id

        mDisposable.add(mViewModel.updateRecord(mRecord!!, newType, oldMoney, oldType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.finish() }
                ) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（记录修改失败的时候）", throwable)
                        finish()
                    } else {
                        Log.e(TAG, "记录修改失败", throwable)
                        mBinding.keyboard.setAffirmEnable(true)
                        ToastUtils.show(R.string.toast_modify_record_fail)
                    }
                })
    }

    private fun getAllRecordTypes() {
        mDisposable.add(mViewModel.allRecordTypes
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recordTypes ->
                    mBinding.typePageOutlay.setNewData(recordTypes, RecordType.TYPE_OUTLAY)
                    mBinding.typePageIncome.setNewData(recordTypes, RecordType.TYPE_INCOME)

                    if (mCurrentType == RecordType.TYPE_OUTLAY) {
                        mBinding.typeChoice?.rgType?.check(R.id.rb_outlay)
                        mBinding.typePageOutlay.initCheckItem(mRecord)
                    } else {
                        mBinding.typeChoice?.rgType?.check(R.id.rb_income)
                        mBinding.typePageIncome.initCheckItem(mRecord)
                    }

                }) { throwable ->
                    ToastUtils.show(R.string.toast_get_types_fail)
                    Log.e(TAG, "获取类型数据失败", throwable)
                })
    }

    companion object {

        private val TAG = AddRecordActivity::class.java.simpleName
        private const val TAG_PICKER_DIALOG = "Datepickerdialog"
    }
}
