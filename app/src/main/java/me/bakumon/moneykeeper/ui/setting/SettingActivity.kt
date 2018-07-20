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

package me.bakumon.moneykeeper.ui.setting

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.jakewharton.processphoenix.ProcessPhoenix
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.*
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.base.EmptyResource
import me.bakumon.moneykeeper.base.ErrorResource
import me.bakumon.moneykeeper.base.SuccessResource
import me.bakumon.moneykeeper.databinding.ActivitySettingBinding
import me.bakumon.moneykeeper.ui.home.HomeActivity
import me.bakumon.moneykeeper.utill.AndroidUtil
import me.bakumon.moneykeeper.utill.BigDecimalUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.math.BigDecimal
import java.util.*

/**
 * 设置
 *
 * @author Bakumon https://bakumon.me
 */
class SettingActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var mBinding: ActivitySettingBinding
    private lateinit var mViewModel: SettingViewModel
    private lateinit var mAdapter: SettingAdapter

    override val layoutId: Int
        get() = R.layout.activity_setting

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingViewModel::class.java)

        initView()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun initView() {
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }
        mBinding.titleBar?.title = getString(R.string.text_setting)

        mBinding.rvSetting.layoutManager = LinearLayoutManager(this)
        mAdapter = SettingAdapter(null)

        val list = ArrayList<SettingSectionEntity>()

        list.add(SettingSectionEntity(getString(R.string.text_money)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_monty_budget), getBudgetStr())))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_assets), getAssetsStr())))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_title_symbol), getString(R.string.text_content_symbol))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_type_manage), null)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_fast_accounting), getString(R.string.text_fast_tip), ConfigManager.isFast)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_successive_record), getString(R.string.text_successive_record_tip), ConfigManager.isSuccessive)))


        list.add(SettingSectionEntity(getString(R.string.text_backup)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_go_backup), getString(R.string.text_backup_save, backupDir))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_restore), getString(R.string.text_restore_content, backupDir))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_auto_backup), getString(R.string.text_auto_backup_content), ConfigManager.isAutoBackup)))

        list.add(SettingSectionEntity(getString(R.string.text_cloud_backup)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(true, getString(R.string.text_cloud_backup_title), getString(R.string.text_cloud_backup_content))))

        list.add(SettingSectionEntity(getString(R.string.text_about_and_help)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_about), getString(R.string.text_about_content))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_help))))

        mAdapter.setNewData(list)
        addListener()
        mBinding.rvSetting.adapter = mAdapter
    }

    private fun addListener() {
        mAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                1 -> setBudget(position)
                2 -> setAssets(position)
                3 -> setSymbol()
                4 -> Floo.navigation(this, Router.Url.URL_TYPE_MANAGE).start()
                8 -> showBackupDialog()
                9 -> showRestoreDialog()
                12 -> Floo.navigation(this, Router.Url.URL_BACKUP).start()
                14 -> Floo.navigation(this, Router.Url.URL_ABOUT).start()
                15 -> AndroidUtil.openWeb(this, Constant.URL_HELP)
                else -> {
                }
            }
        }
        // Switch
        mAdapter.setOnItemChildClickListener { _, _, position ->
            when (position) {
                5 -> switchFast()
                6 -> switchSuccessive()
                10 -> switchAutoBackup(position)
                else -> {
                }
            }
        }
    }

    private fun getBudgetStr(): String {
        return if (ConfigManager.budget == 0)
            getString(R.string.text_no_setting)
        else
            ConfigManager.symbol + BigDecimalUtil.formatNum(ConfigManager.budget.toString())
    }

    private fun setBudget(position: Int) {
        val oldBudget = if (ConfigManager.budget == 0)
            null
        else
            ConfigManager.budget.toString().replace(",", "")
        MaterialDialog.Builder(this)
                .title(R.string.text_set_budget)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRange(0, 8)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input(getString(R.string.hint_enter_budget), oldBudget,
                        { _, input ->
                            val text = input.toString()
                            if (!TextUtils.isEmpty(text)) {
                                ConfigManager.setBudget(Integer.parseInt(text))
                            } else {
                                ConfigManager.setBudget(0)
                            }
                            resetBudgetItem(position)
                        }).show()
    }

    private fun resetBudgetItem(position: Int) {
        mAdapter.data[position].t.content = getBudgetStr()
        mBinding.rvSetting.itemAnimator.changeDuration = 250
        mAdapter.notifyItemChanged(position)
    }

    private fun getAssetsStr(): String {
        return if (TextUtils.equals(ConfigManager.assets, "NaN"))
            getString(R.string.text_no_setting)
        else
            ConfigManager.symbol + BigDecimalUtil.fen2Yuan(BigDecimal(ConfigManager.assets))
    }

    private fun setAssets(position: Int) {
        val oldAssets = if (TextUtils.equals(ConfigManager.assets, "NaN"))
            null
        else
            BigDecimalUtil.fen2YuanNoSeparator(BigDecimal(ConfigManager.assets))
        MaterialDialog.Builder(this)
                .title(R.string.text_setting_assets)
                .inputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .inputRange(0, 10)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input(getString(R.string.hint_enter_assets), oldAssets,
                        { _, input ->
                            val text = input.toString()
                            if (TextUtils.isEmpty(text) || TextUtils.equals(text, ".")) {
                                ConfigManager.setAssets("NaN")
                            } else {
                                val saveStr = BigDecimalUtil.yuan2FenBD(inputFilter(text)).toPlainString()
                                ConfigManager.setAssets(saveStr)
                            }
                            resetAssetsItem(position)
                        }).show()
    }

    private fun inputFilter(text: String): String {
        return if (text.contains(".")) {
            val splitList = text.split(".")
            if (splitList[1].length > 2) {
                splitList[0] + "." + splitList[1].substring(0, 2)
            } else {
                text
            }
        } else {
            text
        }
    }

    private fun resetAssetsItem(position: Int) {
        mAdapter.data[position].t.content = getAssetsStr()
        mBinding.rvSetting.itemAnimator.changeDuration = 250
        mAdapter.notifyItemChanged(position)
    }

    private fun setSymbol() {
        var index = 0
        val symbolList = resources.getStringArray(R.array.simple_symbol)
        val savedSymbol = ConfigManager.symbol

        symbolList.forEachIndexed { i, symbol ->
            if (TextUtils.equals(symbol, savedSymbol)) {
                index = i
            }
        }

        MaterialDialog.Builder(this)
                .title(R.string.text_set_symbol)
                .items(R.array.symbol)
                .itemsCallbackSingleChoice(index, { _, _, which, _ ->
                    val simpleSymbol = resources.getStringArray(R.array.simple_symbol)[which]
                    ConfigManager.setSymbol(simpleSymbol)
                    // 更新预算和资产符号
                    resetBudgetItem(1)
                    resetAssetsItem(2)
                    true
                })
                .positiveText(R.string.text_affirm)
                .show()
    }

    private fun switchFast() {
        val oldIsConfigOpen = ConfigManager.isFast
        ConfigManager.setIsFast(!oldIsConfigOpen)
    }

    private fun switchSuccessive() {
        val oldIsConfigOpen = ConfigManager.isSuccessive
        ConfigManager.setIsSuccessive(!oldIsConfigOpen)
    }

    private fun switchAutoBackup(position: Int) {
        val oldIsConfigOpen = mAdapter.data[position].t.isConfigOpen
        if (oldIsConfigOpen) {
            MaterialDialog.Builder(this)
                    .cancelable(false)
                    .title(R.string.text_close_auto_backup)
                    .content(R.string.text_close_auto_backup_tip)
                    .positiveText(R.string.text_affirm)
                    .negativeText(R.string.text_cancel)
                    .onNegative({ _, _ -> mAdapter.notifyItemChanged(position) })
                    .onPositive({ _, _ -> setAutoBackup(position, false) })
                    .show()

        } else {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                setAutoBackup(position, true)
                return
            }
            EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(this, 11, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            .setRationale(R.string.text_storage_content)
                            .setPositiveButtonText(R.string.text_affirm)
                            .setNegativeButtonText(R.string.text_cancel)
                            .build())
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        when (requestCode) {
            11 -> {
                ConfigManager.setIsAutoBackup(true)
                initView()
            }
            12 -> backupDB()
            13 -> restore()
            else -> {
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                    .setRationale(R.string.text_storage_permission_tip)
                    .setTitle(R.string.text_storage)
                    .setPositiveButton(R.string.text_affirm)
                    .setNegativeButton(R.string.text_cancel)
                    .build()
                    .show()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            initView()
        }
    }

    private fun setAutoBackup(position: Int, isBackup: Boolean) {
        ConfigManager.setIsAutoBackup(isBackup)
        mAdapter.data[position].t.isConfigOpen = isBackup
        // 取消 item 动画
        mBinding.rvSetting.itemAnimator.changeDuration = 0
        mAdapter.notifyItemChanged(position)
    }

    private fun showBackupDialog() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            backupDB()
            return
        }
        EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, 12, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setRationale(R.string.text_storage_content)
                        .setPositiveButtonText(R.string.text_affirm)
                        .setNegativeButtonText(R.string.text_cancel)
                        .build())
    }

    private fun backupDB() {
        MaterialDialog.Builder(this)
                .title(R.string.text_go_backup)
                .content(R.string.text_backup_save, backupFilepath)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .onPositive({ _, _ ->
                    mDisposable.add(mViewModel.backupDB()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ ToastUtils.show(R.string.toast_backup_success) }
                            ) { throwable ->
                                ToastUtils.show(R.string.toast_backup_fail)
                                Log.e(TAG, "备份失败", throwable)
                            })
                })
                .show()
    }

    private fun showRestoreDialog() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            restore()
            return
        }
        EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, 13, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setRationale(R.string.text_storage_content)
                        .setPositiveButtonText(R.string.text_affirm)
                        .setNegativeButtonText(R.string.text_cancel)
                        .build())
    }

    private fun restore() {
        mDisposable.add(mViewModel.backupFiles
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ backupBeans ->
                    val dialog = BackupFliesDialog(this, backupBeans)
                    dialog.mOnItemClickListener = {
                        restoreDB(it.path)
                    }
                    dialog.show()
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_backup_list_fail)
                    Log.e(TAG, "备份文件列表获取失败", throwable)
                })
    }

    private fun restoreDB(restoreFile: String) {
        mViewModel.restoreToDB(restoreFile).observe(this, android.arch.lifecycle.Observer {
            when (it) {
                is SuccessResource<Boolean> -> {
                    if (it.body) {
                        ToastUtils.show(R.string.toast_restore_success)
                        backHome()
                    } else {
                        ToastUtils.show(R.string.toast_restore_fail)
                    }
                }
                is EmptyResource -> {
                    restartApp()
                }
                is ErrorResource<Boolean> -> ToastUtils.show(getString(R.string.toast_restore_fail) + "\n" + it.errorMessage)
            }
        })
    }

    private fun backHome() {
        Floo.stack(this)
                .target(Router.IndexKey.INDEX_KEY_HOME)
                .result("refresh")
                .start()
    }

    private fun restartApp() {
        MaterialDialog.Builder(this)
                .cancelable(false)
                .title("\uD83D\uDC7A" + getString(R.string.text_error))
                .content(R.string.text_restore_fail_rollback)
                .positiveText(R.string.text_affirm)
                .onPositive({ _, _ ->
                    ProcessPhoenix.triggerRebirth(this, Intent(this, HomeActivity::class.java))
                })
                .show()
    }

    companion object {
        private val TAG = SettingActivity::class.java.simpleName
        @SuppressLint("SdCardPath")
        val backupDir = "/sdcard" + if (BuildConfig.DEBUG) "/backup_moneykeeper_debug/" else "/backup_moneykeeper/"
        @SuppressLint("SdCardPath")
        val backupFilepath = backupDir + if (BuildConfig.DEBUG) "MoneyKeeperBackupUserDebug.db" else "MoneyKeeperBackupUser.db"
    }
}
