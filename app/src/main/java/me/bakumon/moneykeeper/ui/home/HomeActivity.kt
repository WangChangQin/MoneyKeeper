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

package me.bakumon.moneykeeper.ui.home

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.database.entity.RecordWithType
import me.bakumon.moneykeeper.databinding.ActivityHomeBinding
import me.bakumon.moneykeeper.datasource.BackupFailException
import me.bakumon.moneykeeper.utill.ShortcutUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import me.drakeet.floo.StackCallback
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

/**
 * HomeActivity
 *
 * @author bakumon https://bakumon.me
 * @date 2018/4/9
 */
class HomeActivity : BaseActivity(), StackCallback, EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
    private lateinit var mBinding: ActivityHomeBinding
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mAdapter: HomeAdapter
    private var isUserFirst: Boolean = false

    override fun getLayoutId(): Int {
        return R.layout.activity_home
    }

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel::class.java)

        initView()
        initData()
        checkPermissionForBackup()

        // 快速记账
        if (ConfigManager.isFast) {
            Floo.navigation(this, Router.Url.URL_ADD_RECORD).start()
        }
    }

    private fun initView() {
        mBinding.rvHome.layoutManager = LinearLayoutManager(this)
        mAdapter = HomeAdapter(null)
        mBinding.rvHome.adapter = mAdapter

        mAdapter.setOnItemChildLongClickListener { _, _, position ->
            showOperateDialog(mAdapter.data[position])
            false
        }
    }

    fun settingClick(view: View) {
        Floo.navigation(this, Router.Url.URL_SETTING)
                .start()
    }

    fun statisticsClick(view: View) {
        Floo.navigation(this, Router.Url.URL_STATISTICS)
                .start()
    }

    fun addRecordClick(view: View) {
        Floo.navigation(this, Router.Url.URL_ADD_RECORD).start()
    }

    override fun onResume() {
        super.onResume()
        getCurrentMontySumMonty()
        if (ConfigManager.isSuccessive) {
            mBinding.btnAddRecord.setOnLongClickListener {
                Floo.navigation(this, Router.Url.URL_ADD_RECORD)
                        .putExtra(Router.ExtraKey.KEY_IS_SUCCESSIVE, true)
                        .start()
                false
            }
        } else {
            mBinding.btnAddRecord.setOnLongClickListener(null)
        }
    }

    private fun showOperateDialog(record: RecordWithType) {
        AlertDialog.Builder(this)
                .setItems(arrayOf(getString(R.string.text_modify), getString(R.string.text_delete))) { _, which ->
                    if (which == 0) {
                        modifyRecord(record)
                    } else {
                        deleteRecord(record)
                    }
                }
                .create()
                .show()
    }

    private fun modifyRecord(record: RecordWithType) {
        Floo.navigation(this, Router.Url.URL_ADD_RECORD)
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

    private fun initData() {
        initRecordTypes()
        getCurrentMonthRecords()
    }

    private fun initRecordTypes() {
        mDisposable.add(mViewModel.initRecordTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ ShortcutUtil.addRecordShortcut(this) }
                ) { throwable ->
                    if (throwable is BackupFailException) {
                        ToastUtils.show(throwable.message)
                        Log.e(TAG, "备份失败（初始化类型数据失败的时候）", throwable)
                    } else {
                        ToastUtils.show(R.string.toast_init_types_fail)
                        Log.e(TAG, "初始化类型数据失败", throwable)
                    }
                })
    }

    private fun getCurrentMontySumMonty() {
        mDisposable.add(mViewModel.currentMonthSumMoney
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ sumMoneyBeans -> mBinding.sumMoneyBeanList = sumMoneyBeans }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_current_sum_money_fail)
                    Log.e(TAG, "本月支出收入总数获取失败", throwable)
                })
    }

    private fun getCurrentMonthRecords() {
        mDisposable.add(mViewModel.currentMonthRecordWithTypes
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recordWithTypes ->
                    setListData(recordWithTypes)
                    if (recordWithTypes == null || recordWithTypes.isEmpty()) {
                        setEmptyView()
                    }
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_records_fail)
                    Log.e(TAG, "获取记录列表失败", throwable)
                })
    }

    private fun setListData(recordWithTypes: List<RecordWithType>?) {
        mAdapter.setNewData(recordWithTypes)
        val isShowFooter = recordWithTypes != null && recordWithTypes.size > MAX_ITEM_TIP
        if (isShowFooter) {
            mAdapter.setFooterView(inflate(R.layout.layout_footer_tip))
        } else {
            mAdapter.removeAllFooterView()
        }
    }

    private fun setEmptyView() {
        mAdapter.emptyView = inflate(R.layout.layout_home_empty)
    }

    override fun indexKeyForStackTarget(): String? {
        return Router.IndexKey.INDEX_KEY_HOME
    }

    override fun onReceivedResult(result: Any?) {
        initData()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun checkPermissionForBackup() {
        if (!ConfigManager.isAutoBackup) {
            return
        }
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return
        }
        // 当自动备份打开，并且没有存储权限，提示用户需要申请权限
        EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, REQUEST_CODE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setRationale(R.string.text_storage_content)
                        .setPositiveButtonText(R.string.text_affirm)
                        .setNegativeButtonText(R.string.text_button_cancel)
                        .build())
    }

    private fun updateConfig(isAutoBackup: Boolean) {
        if (isAutoBackup) {
            ConfigManager.setIsAutoBackup(true)
        } else {
            if (ConfigManager.setIsAutoBackup(false)) {
                ToastUtils.show(R.string.toast_open_auto_backup)
            }
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        isUserFirst = true
    }

    override fun onRationaleDenied(requestCode: Int) {
        isUserFirst = true
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        updateConfig(true)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (!isUserFirst) {
                AppSettingsDialog.Builder(this)
                        .setRationale(R.string.text_storage_permission_tip)
                        .setTitle(R.string.text_storage)
                        .setPositiveButton(R.string.text_affirm)
                        .setNegativeButton(R.string.text_button_cancel)
                        .build()
                        .show()
            }
        } else {
            updateConfig(false)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                updateConfig(true)
            } else {
                updateConfig(false)
            }
        }
    }

    companion object {

        private val TAG = HomeActivity::class.java.simpleName
        private const val MAX_ITEM_TIP = 5

        ///////////////////////////////
        //// 自动备份打开时，检查是否有权限
        ///////////////////////////////

        private const val REQUEST_CODE_STORAGE = 11
    }

}
