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
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.text.TextUtils
import com.afollestad.materialdialogs.MaterialDialog
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.api.ApiEmptyResponse
import me.bakumon.moneykeeper.api.ApiErrorResponse
import me.bakumon.moneykeeper.api.ApiSuccessResponse
import me.bakumon.moneykeeper.api.DavFileList
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.base.ErrorResource
import me.bakumon.moneykeeper.base.SuccessResource
import me.bakumon.moneykeeper.databinding.ActivitySettingBinding
import me.bakumon.moneykeeper.ui.setting.SettingAdapter
import me.bakumon.moneykeeper.ui.setting.SettingSectionEntity
import me.bakumon.moneykeeper.utill.AndroidUtil
import me.bakumon.moneykeeper.utill.Base64Util
import me.bakumon.moneykeeper.utill.ToastUtils
import okhttp3.ResponseBody
import java.util.*

/**
 * 云备份
 *
 * @author Bakumon https://bakumon.me
 */
class BackupActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySettingBinding
    private lateinit var mViewModel: BackupViewModel
    private lateinit var mAdapter: SettingAdapter

    private var psw = ""

    override val layoutId: Int
        get() = R.layout.activity_setting

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(BackupViewModel::class.java)

        initView()
    }

    private fun initView() {
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }
        mBinding.titleBar?.title = getString(R.string.text_cloud_backup)

        mBinding.rvSetting.layoutManager = LinearLayoutManager(this)
        mAdapter = SettingAdapter(null)

        val list = ArrayList<SettingSectionEntity>()

        list.add(SettingSectionEntity(getString(R.string.text_nutstore)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_nutstore_account), ConfigManager.jianguoyunAccount)))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_nutstore_password), getItemDisplayPsw())))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_go_backup), getString(R.string.text_nutstore_backup_content))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_restore), getString(R.string.text_nutstore_restore_content))))
        list.add(SettingSectionEntity(SettingSectionEntity.Item(getString(R.string.text_nutstore_help), NUTSTORE_HELP_URL)))

        mAdapter.setNewData(list)
        addListener()
        mBinding.rvSetting.adapter = mAdapter

        getOldPsw()
    }

    private fun addListener() {
        mAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                1 -> setAccount()
                2 -> setPsw()
                3 -> backup()
                4 -> restore()
                5 -> AndroidUtil.openWeb(this, NUTSTORE_HELP_URL)
                else -> {
                }
            }
        }
    }

    private fun setAccount() {
        MaterialDialog.Builder(this)
                .title(R.string.text_nutstore_account)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input("", ConfigManager.jianguoyunAccount,
                        { _, input ->
                            ConfigManager.setJianguoyunAccount(input.toString().trim())
                            mAdapter.data[1].t.content = ConfigManager.jianguoyunAccount
                            mBinding.rvSetting.itemAnimator.changeDuration = 250
                            mAdapter.notifyItemChanged(1)
                            initDir()
                        }).show()
    }

    private fun getItemDisplayPsw(): String {
        return if (ConfigManager.jianguoyunEncryptPsw.isEmpty()) "" else "******"
    }

    private var isSaving = false

    private fun setPsw() {
        if (isSaving) {
            return
        }
        MaterialDialog.Builder(this)
                .title(R.string.text_nutstore_password)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.text_affirm)
                .negativeText(R.string.text_cancel)
                .input("", psw,
                        { _, input ->
                            savePsw(input.toString())
                        }).show()
    }

    private var inputPsw = ""

    private fun savePsw(input: String) {
        isSaving = true
        inputPsw = input
        mViewModel.savePsw(input).observe(this, Observer {
            isSaving = false
            when (it) {
                is SuccessResource<Boolean> -> {
                    if (it.body) {
                        mAdapter.data[2].t.content = getItemDisplayPsw()
                        mBinding.rvSetting.itemAnimator.changeDuration = 0
                        mAdapter.notifyItemChanged(2)
                        initDir()
                    }
                }
                is ErrorResource<Boolean> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    private fun getOldPsw() {
        mViewModel.getPsw().observe(this, Observer {
            when (it) {
                is SuccessResource<String> -> psw = it.body
                is ErrorResource<String> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    private fun updateAuth() {
        val account = ConfigManager.jianguoyunAccount
        ConfigManager.auth = "Basic " + Base64Util.encode("$account:$inputPsw")
    }

    private fun initDir() {
        updateAuth()
        if (ConfigManager.jianguoyunAccount.isEmpty() || ConfigManager.jianguoyunEncryptPsw.isEmpty()) {
            return
        }
        mViewModel.createDir().observe(this, Observer {
            when (it) {
                is ApiErrorResponse<ResponseBody> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    private fun backup() {
        // 创建目录
        mViewModel.createDir().observe(this, Observer {
            when (it) {
                is ApiSuccessResponse<ResponseBody> -> backupUpload()
                is ApiEmptyResponse<ResponseBody> -> backupUpload()
                is ApiErrorResponse<ResponseBody> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    private fun backupUpload() {
        // 上传文件
        mViewModel.backup().observe(this, Observer {
            when (it) {
                is ApiSuccessResponse<ResponseBody> -> checkUpload()
                is ApiEmptyResponse<ResponseBody> -> checkUpload()
                is ApiErrorResponse<ResponseBody> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    private fun checkUpload() {
        // 检测上传是否真的成功
        mViewModel.getList().observe(this, Observer {
            when (it) {
                is ApiSuccessResponse<DavFileList> -> {
                    for (bean in it.body.list) {
                        if (TextUtils.equals(bean.displayName, BackupViewModel.BACKUP_FILE_NAME)) {
                            ToastUtils.show(R.string.toast_backup_success)
                            break
                        }
                    }
                }
                is ApiErrorResponse<DavFileList> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    private fun restore() {
        mViewModel.restore().observe(this, Observer {
            when (it) {
                is ApiSuccessResponse<ResponseBody> -> {
                    // TODO 恢复网备份
                    ToastUtils.show("Response:" + it.body)
                }
                is ApiErrorResponse<ResponseBody> -> ToastUtils.show(it.errorMessage)
            }
        })
    }

    companion object {
        const val NUTSTORE_HELP_URL = "http://help.jianguoyun.com/?p=2064"
    }

}
