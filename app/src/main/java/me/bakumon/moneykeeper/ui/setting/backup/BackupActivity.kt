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

package me.bakumon.moneykeeper.ui.setting.backup

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.layout_tool_bar.view.*
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.Constant
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.api.ApiErrorResponse
import me.bakumon.moneykeeper.api.ApiSuccessResponse
import me.bakumon.moneykeeper.api.Network
import me.bakumon.moneykeeper.base.EmptyResource
import me.bakumon.moneykeeper.base.ErrorResource
import me.bakumon.moneykeeper.base.SuccessResource
import me.bakumon.moneykeeper.ui.common.BaseActivity
import me.bakumon.moneykeeper.ui.home.HomeActivity
import me.bakumon.moneykeeper.ui.setting.SettingAdapter
import me.bakumon.moneykeeper.ui.setting.SettingSectionEntity
import me.bakumon.moneykeeper.utill.AndroidUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import java.util.*

/**
 * 云备份
 *
 * @author Bakumon https://bakumon.me
 */
class BackupActivity : BaseActivity() {
    private lateinit var mViewModel: BackupViewModel
    private lateinit var mAdapter: SettingAdapter

    override val layoutId: Int
        get() = R.layout.activity_setting

    override fun onInitView(savedInstanceState: Bundle?) {
//        mBinding = getDataBinding()
        mViewModel = getViewModel()

        initView()
        initDir()
    }

    private fun initView() {
        toolbarLayout.tvTitle.text = getString(R.string.text_cloud_backup)
        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        rv_setting.layoutManager = LinearLayoutManager(this)
        mAdapter = SettingAdapter(null)

        val list = ArrayList<SettingSectionEntity>()

        list.add(SettingSectionEntity(getString(R.string.text_webdav)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_webdav_url), ConfigManager.webDavUrl)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_webdav_account), ConfigManager.webDavAccount)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_webdav_password), getItemDisplayPsw())))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_go_backup), getString(R.string.text_backup_save, getString(R.string.text_webdav) + BackupViewModel.BACKUP_FILE))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_restore), getString(R.string.text_restore_content, getString(R.string.text_webdav) + BackupViewModel.BACKUP_FILE))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_auto_backup_mode_title), ConfigManager.cloudEnable, getBackupModeStr())))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_webdav_help), Constant.NUTSTORE_HELP_URL)))

        mAdapter.setNewData(list)
        addListener()
        rv_setting.adapter = mAdapter
    }

    override fun onInit(savedInstanceState: Bundle?) {

    }

    private fun getItemDisplayPsw(): String {
        return if (ConfigManager.webDAVPsw.isEmpty()) "" else "******"
    }

    private fun addListener() {
        mAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                1 -> setUrl(position)
                2 -> setAccount(position)
                3 -> setPsw(position)
                4 -> showBackupDialog()
                5 -> showRestoreDialog()
                6 -> if (ConfigManager.cloudEnable) {
                    chooseAutoBackupMode(position)
                }
                7 -> AndroidUtil.openWeb(this, Constant.NUTSTORE_HELP_URL)
                else -> {
                }
            }
        }
    }

    private fun setUrl(position: Int) {
        MaterialDialog.Builder(this)
                .title(R.string.text_webdav_url)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input("", ConfigManager.webDavUrl,
                        { _, input ->
                            val url = input.toString().trim()
                            when {
                                url.isEmpty() -> {
                                    updateUrlItem(url, position)
                                }
                                HttpUrl.parse(url) == null -> ToastUtils.show(R.string.text_url_illegal)
                                else -> {
                                    updateUrlItem(url, position)
                                    // 更新网络配置
                                    Network.updateDavServiceConfig()
                                    initDir()
                                }
                            }
                        }).show()
    }

    private fun updateUrlItem(url: String, position: Int) {
        ConfigManager.setWevDavUrl(url)
        mAdapter.data[position].t.content = ConfigManager.webDavUrl
        rv_setting.itemAnimator.changeDuration = 250
        mAdapter.notifyItemChanged(position)
    }

    private fun setAccount(position: Int) {
        MaterialDialog.Builder(this)
                .title(R.string.text_webdav_account)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input("", ConfigManager.webDavAccount,
                        { _, input ->
                            ConfigManager.setWevDavAccount(input.toString().trim())
                            mAdapter.data[position].t.content = ConfigManager.webDavAccount
                            rv_setting.itemAnimator.changeDuration = 250
                            mAdapter.notifyItemChanged(position)
                            // 更新网络配置
                            Network.updateDavServiceConfig()
                            initDir()
                        }).show()
    }

    private var isSaving = false

    private fun setPsw(position: Int) {
        if (isSaving) {
            return
        }
        MaterialDialog.Builder(this)
                .title(R.string.text_webdav_password)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input("", ConfigManager.webDAVPsw,
                        { _, input ->
                            savePsw(position, input.toString())
                        }).show()
    }

    private fun savePsw(position: Int, input: String) {
        isSaving = true
        ConfigManager.webDAVPsw = input
        mAdapter.data[position].t.content = getItemDisplayPsw()
        rv_setting.itemAnimator.changeDuration = 0
        mAdapter.notifyItemChanged(position)
        // 更新网络配置
        Network.updateDavServiceConfig()
        initDir()
        mViewModel.savePsw(input).observe(this, Observer {
            isSaving = false
            when (it) {
                is ErrorResource<Boolean> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    private fun updateBackupEnable(enable: Boolean) {
        ConfigManager.setCloudEnable(enable)
        updateCloudBackupItem(6)
    }

    private fun initDir() {
        if (ConfigManager.webDavUrl.isEmpty() || ConfigManager.webDavAccount.isEmpty() || ConfigManager.webDAVPsw.isEmpty()) {
            updateBackupEnable(false)
            return
        }
        mViewModel.getList().observe(this, Observer {
            when (it) {
                is ApiErrorResponse<ResponseBody> -> {
                    if (it.code == 404) {
                        createDir()
                    } else {
                        updateBackupEnable(false)
                        ToastUtils.show(it.errorMessage)
                    }
                }
                else -> {
                    updateBackupEnable(true)
                }
            }
        })
    }

    private fun createDir() {
        mViewModel.createDir().observe(this, Observer {
            when (it) {
                is ApiErrorResponse<ResponseBody> -> {
                    updateBackupEnable(false)
                    ToastUtils.show(it.errorMessage)
                }
                else -> {
                    updateBackupEnable(true)
                }
            }
        })
    }

    private fun showBackupDialog() {
        if (ConfigManager.webDavUrl.isEmpty() || ConfigManager.webDavAccount.isEmpty() || ConfigManager.webDAVPsw.isEmpty()) {
            ToastUtils.show(R.string.text_config_webdav)
            return
        }
        MaterialDialog.Builder(this)
                .title(R.string.text_go_backup)
                .content(R.string.text_backup_save, getString(R.string.text_webdav) + BackupViewModel.BACKUP_FILE)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .onPositive({ _, _ -> cloudBackup(mViewModel) })
                .show()
    }

    override fun onCloudBackupSuccess() {
        ToastUtils.show(R.string.toast_backup_success)
    }

    private fun showRestoreDialog() {
        if (ConfigManager.webDavUrl.isEmpty() || ConfigManager.webDavAccount.isEmpty() || ConfigManager.webDAVPsw.isEmpty()) {
            ToastUtils.show(R.string.text_config_webdav)
            return
        }
        MaterialDialog.Builder(this)
                .title(R.string.text_restore)
                .content(R.string.text_restore_content, getString(R.string.text_webdav) + BackupViewModel.BACKUP_FILE)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .onPositive({ _, _ -> restore() })
                .show()
    }

    private fun restore() {
        mViewModel.restore().observe(this, Observer {
            when (it) {
                is ApiSuccessResponse<ResponseBody> -> {
                    restoreToDB(it.body)
                }
                is ApiErrorResponse<ResponseBody> -> {
                    if (it.code == 404) {
                        ToastUtils.show(R.string.text_backup_file_not_exist)
                    } else {
                        ToastUtils.show(it.errorMessage)
                    }
                }
            }
        })
    }

    private fun restoreToDB(body: ResponseBody) {
        mViewModel.restoreToDB(body).observe(this, Observer {
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
                .popCount(2)
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

    private fun chooseAutoBackupMode(position: Int) {
        val index = when (ConfigManager.cloudBackupMode) {
            ConfigManager.MODE_NO -> 0
            ConfigManager.MODE_LAUNCHER_APP -> 1
            else -> 0
        }

        MaterialDialog.Builder(this)
                .title(R.string.text_auto_backup_mode)
                .items(R.array.text_cloud_auto_backup_mode)
                .itemsCallbackSingleChoice(index, { _, _, which, _ ->
                    when (which) {
                        0 -> ConfigManager.setCloudBackupMode(ConfigManager.MODE_NO)
                        1 -> ConfigManager.setCloudBackupMode(ConfigManager.MODE_LAUNCHER_APP)
                    }
                    updateCloudBackupItem(position)
                    true
                })
                .positiveText(R.string.text_affirm)
                .show()
    }

    /**
     * 更新备份模式 item 内容
     */
    private fun updateCloudBackupItem(position: Int) {
        mAdapter.data[position].t.content = getBackupModeStr()
        mAdapter.data[position].t.isEnable = ConfigManager.cloudEnable
        rv_setting.itemAnimator.changeDuration = 0
        mAdapter.notifyItemChanged(position)
    }

    private fun getBackupModeStr(): String {
        return when (ConfigManager.cloudBackupMode) {
            ConfigManager.MODE_NO -> resources.getStringArray(R.array.text_cloud_auto_backup_mode)[0]
            ConfigManager.MODE_LAUNCHER_APP -> resources.getStringArray(R.array.text_cloud_auto_backup_mode)[1]
            else -> resources.getStringArray(R.array.text_cloud_auto_backup_mode)[3]
        }
    }

    override fun onDestroy() {
        if (isSaving) {
            ToastUtils.show(R.string.text_saving_psw)
        } else {
            super.onDestroy()
        }
    }

}
