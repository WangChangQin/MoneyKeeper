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
import me.bakumon.moneykeeper.ui.setting.Category
import me.bakumon.moneykeeper.ui.setting.CategoryViewBinder
import me.bakumon.moneykeeper.ui.setting.NormalItem
import me.bakumon.moneykeeper.ui.setting.NormalItemViewBinder
import me.bakumon.moneykeeper.utill.AndroidUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register
import okhttp3.HttpUrl
import okhttp3.ResponseBody

/**
 * 云备份
 *
 * @author Bakumon https://bakumon.me
 */
class BackupActivity : BaseActivity() {
    private lateinit var mViewModel: BackupViewModel
    private lateinit var adapter: MultiTypeAdapter

    override val layoutId: Int
        get() = R.layout.activity_setting

    override fun onInitView(savedInstanceState: Bundle?) {
        toolbarLayout.tvTitle.text = getString(R.string.text_cloud_backup)
        setSupportActionBar(toolbarLayout as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onInit(savedInstanceState: Bundle?) {
        adapter = MultiTypeAdapter()
        adapter.register(Category::class, CategoryViewBinder())
        adapter.register(NormalItem::class, NormalItemViewBinder({ onNormalItemClick(it) }))
        rv_setting.adapter = adapter


        val items = Items()
        items.add(Category(getString(R.string.text_webdav)))
        items.add(NormalItem(getString(R.string.text_webdav_url), ConfigManager.webDavUrl))
        items.add(NormalItem(getString(R.string.text_webdav_account), ConfigManager.webDavAccount))
        items.add(NormalItem(getString(R.string.text_webdav_password), getItemDisplayPsw()))
        items.add(NormalItem(getString(R.string.text_go_backup), getString(R.string.text_backup_save, getString(R.string.text_webdav) + BackupViewModel.BACKUP_FILE)))
        items.add(NormalItem(getString(R.string.text_restore), getString(R.string.text_backup_save, getString(R.string.text_restore_content, getString(R.string.text_webdav) + BackupViewModel.BACKUP_FILE))))
        items.add(NormalItem(getString(R.string.text_auto_backup_mode_title), getBackupModeStr(), ConfigManager.cloudEnable))
        items.add(NormalItem(getString(R.string.text_webdav_help), Constant.NUTSTORE_HELP_URL))

        adapter.items = items
        adapter.notifyDataSetChanged()

        mViewModel = getViewModel()
        initDir()
    }

    private fun onNormalItemClick(item: NormalItem) {
        when (item.title) {
            getString(R.string.text_webdav_url) -> setUrl()
            getString(R.string.text_webdav_account) -> setAccount()
            getString(R.string.text_webdav_password) -> setPsw()
            getString(R.string.text_go_backup) -> showBackupDialog()
            getString(R.string.text_restore) -> showRestoreDialog()
            getString(R.string.text_auto_backup_mode_title) -> if (ConfigManager.cloudEnable) {
                chooseAutoBackupMode()
            }
            getString(R.string.text_webdav_help) -> AndroidUtil.openWeb(this, Constant.NUTSTORE_HELP_URL)
        }
    }

    private fun getItemDisplayPsw(): String {
        return if (ConfigManager.webDAVPsw.isEmpty()) "" else "******"
    }

    private fun setUrl() {
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
                                    updateUrlItem(url)
                                }
                                HttpUrl.parse(url) == null -> ToastUtils.show(R.string.text_url_illegal)
                                else -> {
                                    updateUrlItem(url)
                                    // 更新网络配置
                                    Network.updateDavServiceConfig()
                                    initDir()
                                }
                            }
                        }).show()
    }

    private fun updateUrlItem(url: String) {
        ConfigManager.setWevDavUrl(url)
        val position = 1
        (adapter.items[position] as NormalItem).content = url
        rv_setting.itemAnimator.changeDuration = 250
        adapter.notifyItemChanged(position)
    }

    private fun setAccount() {
        MaterialDialog.Builder(this)
                .title(R.string.text_webdav_account)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input("", ConfigManager.webDavAccount,
                        { _, input ->
                            updateAccountItem(input.toString().trim())
                            // 更新网络配置
                            Network.updateDavServiceConfig()
                            initDir()
                        }).show()
    }

    private fun updateAccountItem(account: String) {
        ConfigManager.setWevDavAccount(account)
        val position = 2
        (adapter.items[position] as NormalItem).content = account
        rv_setting.itemAnimator.changeDuration = 250
        adapter.notifyItemChanged(position)
    }

    private var isSaving = false

    private fun setPsw() {
        if (isSaving) {
            ToastUtils.show(R.string.text_saving_psw)
            return
        }
        MaterialDialog.Builder(this)
                .title(R.string.text_webdav_password)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input("", ConfigManager.webDAVPsw,
                        { _, input ->
                            savePsw(input.toString())
                        }).show()
    }

    private fun savePsw(input: String) {
        isSaving = true

        updatePswItem(input)

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

    private fun updatePswItem(input: String) {
        ConfigManager.webDAVPsw = input
        val position = 3
        (adapter.items[position] as NormalItem).content = getItemDisplayPsw()
        rv_setting.itemAnimator.changeDuration = 250
        adapter.notifyItemChanged(position)
    }

    private fun updateBackupEnable(enable: Boolean) {
        ConfigManager.setCloudEnable(enable)
        updateCloudBackupItem()
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

    private fun chooseAutoBackupMode() {
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
                    updateCloudBackupItem()
                    true
                })
                .positiveText(R.string.text_affirm)
                .show()
    }

    /**
     * 更新备份模式 item 内容
     */
    private fun updateCloudBackupItem() {
        val position = 6
        (adapter.items[position] as NormalItem).content = getBackupModeStr()
        (adapter.items[position] as NormalItem).clickEnable = ConfigManager.cloudEnable
        rv_setting.itemAnimator.changeDuration = 0
        adapter.notifyItemChanged(position)
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
