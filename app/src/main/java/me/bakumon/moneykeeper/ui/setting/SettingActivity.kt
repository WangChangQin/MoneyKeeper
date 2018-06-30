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
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.BuildConfig
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivitySettingBinding
import me.bakumon.moneykeeper.utill.AlipayZeroSdk
import me.bakumon.moneykeeper.utill.AndroidUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
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
        mViewModel = ViewModelProviders.of(this).get(SettingViewModel::class.java)

        initView()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun initView() {
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }
        mBinding.titleBar?.title = getString(R.string.text_title_setting)

        mBinding.rvSetting.layoutManager = LinearLayoutManager(this)
        mAdapter = SettingAdapter(null)

        val list = ArrayList<SettingSectionEntity>()

        list.add(SettingSectionEntity(getString(R.string.text_setting_money)))
        val budget = if (ConfigManager.budget == 0) getString(R.string.text_no_budget) else getString(R.string.text_money_symbol) + ConfigManager.budget
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_monty_budget), budget)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_type_manage), null)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_fast_accounting), getString(R.string.text_fast_tip), ConfigManager.isFast)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_successive_record), getString(R.string.text_successive_record_tip), ConfigManager.isSuccessive)))


        list.add(SettingSectionEntity(getString(R.string.text_setting_backup)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_go_backup), getString(R.string.text_setting_go_backup_content, backupDir))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_restore), getString(R.string.text_setting_restore_content, backupDir))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_auto_backup), getString(R.string.text_setting_auto_backup_content), ConfigManager.isAutoBackup)))

        list.add(SettingSectionEntity(getString(R.string.text_setting_about_and_help)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_about), getString(R.string.text_about_content))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_score), getString(R.string.text_setting_good_score) + "\uD83D\uDE18")))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_donate), "")))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_lisence))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_setting_help))))

        mAdapter.setNewData(list)
        addListener()
        mBinding.rvSetting.adapter = mAdapter
    }

    private fun addListener() {
        mAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                1 -> setBudget(position)
                2 -> goTypeManage()
                6 -> showBackupDialog()
                7 -> showRestoreDialog()
                10 -> goAbout()
                11 -> market()
                12 -> alipay()
                13 -> goOpenSource()
                14 -> AndroidUtil.openWeb(this, "https://github.com/Bakumon/MoneyKeeper/blob/master/Help.md")
                else -> {
                }
            }
        }
        // Switch
        mAdapter.setOnItemChildClickListener { _, _, position ->
            when (position) {
                3 -> switchFast()
                4 -> switchSuccessive()
                8 -> switchAutoBackup(position)
                else -> {
                }
            }
        }
    }

    private fun setBudget(position: Int) {
        val oldBudget = if (ConfigManager.budget == 0) null else ConfigManager.budget.toString()
        MaterialDialog.Builder(this)
                .title(R.string.text_set_budget)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRange(0, 8)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_button_cancel)
                .input(getString(R.string.hint_enter_budget), oldBudget,
                        { _, input ->
                            val text = input.toString()
                            if (!TextUtils.isEmpty(text)) {
                                ConfigManager.setBudget(Integer.parseInt(text))
                            } else {
                                ConfigManager.setBudget(0)
                            }
                            mAdapter.data[position].t.content = if (ConfigManager.budget == 0) getString(R.string.text_no_budget) else getString(R.string.text_money_symbol) + ConfigManager.budget
                            mBinding.rvSetting.itemAnimator.changeDuration = 250
                            mAdapter.notifyItemChanged(position)
                        }).show()
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
                    .negativeText(R.string.text_button_cancel)
                    .onNegative({ _, _ -> mAdapter.notifyDataSetChanged() })
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
                            .setNegativeButtonText(R.string.text_button_cancel)
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
                    .setNegativeButton(R.string.text_button_cancel)
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
                        .setNegativeButtonText(R.string.text_button_cancel)
                        .build())
    }

    private fun backupDB() {
        MaterialDialog.Builder(this)
                .title(R.string.text_backup)
                .content(R.string.text_backup_save, backupFilepath)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_button_cancel)
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
                        .setNegativeButtonText(R.string.text_button_cancel)
                        .build())
    }

    private fun restore() {
        mDisposable.add(mViewModel.backupFiles
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ backupBeans ->
                    val dialog = BackupFliesDialog(this, backupBeans)
//                    dialog.setListener { file -> restoreDB(file.path) }
                    dialog.setOnItemClickListener(object : BackupFliesDialog.OnItemClickListener {
                        override fun onClick(file: File) {
                            restoreDB(file.path)
                        }
                    })
                    dialog.show()
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_backup_list_fail)
                    Log.e(TAG, "备份文件列表获取失败", throwable)
                })
    }

    private fun restoreDB(restoreFile: String) {
        mDisposable.add(mViewModel.restoreDB(restoreFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    ToastUtils.show(R.string.toast_restore_success)
                    Floo.stack(this)
                            .target(Router.IndexKey.INDEX_KEY_HOME)
                            .result("refresh")
                            .start()
                }
                ) { throwable ->
                    ToastUtils.show(R.string.toast_restore_fail)
                    Log.e(TAG, "恢复备份失败", throwable)
                })
    }

    private fun goTypeManage() {
        Floo.navigation(this, Router.Url.URL_TYPE_MANAGE)
                .start()
    }

    private fun goAbout() {
        Floo.navigation(this, Router.Url.URL_ABOUT)
                .start()
    }

    private fun goOpenSource() {
        Floo.navigation(this, Router.Url.URL_OPEN_SOURCE)
                .start()
    }

    private fun market() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.show(R.string.toast_not_install_market)
            e.printStackTrace()
        }

    }

    private fun alipay() {
        // https://fama.alipay.com/qrcode/qrcodelist.htm?qrCodeType=P  二维码地址
        // http://cli.im/deqr/ 解析二维码
        // aex01251c8foqaprudcp503
        if (AlipayZeroSdk.hasInstalledAlipayClient(this)) {
            AlipayZeroSdk.startAlipayClient(this, "aex01251c8foqaprudcp503")
        } else {
            ToastUtils.show(R.string.toast_not_install_alipay)
        }
    }

    companion object {
        private val TAG = SettingActivity::class.java.simpleName
        @SuppressLint("SdCardPath")
        val backupDir = "/sdcard" + if (BuildConfig.DEBUG) "/backup_moneykeeper_debug/" else "/backup_moneykeeper/"
        @SuppressLint("SdCardPath")
        val backupFilepath = backupDir + if (BuildConfig.DEBUG) "MoneyKeeperBackupUserDebug.db" else "MoneyKeeperBackupUser.db"

    }
}
