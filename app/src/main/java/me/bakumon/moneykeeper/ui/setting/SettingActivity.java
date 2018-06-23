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

package me.bakumon.moneykeeper.ui.setting;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.bakumon.moneykeeper.ConfigManager;
import me.bakumon.moneykeeper.R;
import me.bakumon.moneykeeper.Router;
import me.bakumon.moneykeeper.base.BaseActivity;
import me.bakumon.moneykeeper.databinding.ActivitySettingBinding;
import me.bakumon.moneykeeper.utill.AlipayZeroSdk;
import me.bakumon.moneykeeper.utill.AndroidUtil;
import me.bakumon.moneykeeper.utill.SoftInputUtils;
import me.bakumon.moneykeeper.utill.ToastUtils;
import me.drakeet.floo.Floo;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

/**
 * 设置
 *
 * @author Bakumon https://bakumon.me
 */
public class SettingActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = SettingActivity.class.getSimpleName();
    private ActivitySettingBinding mBinding;
    private SettingViewModel mViewModel;
    private SettingAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void onInit(@Nullable Bundle savedInstanceState) {
        mBinding = getDataBinding();
        mViewModel = ViewModelProviders.of(this).get(SettingViewModel.class);

        initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void initView() {
        mBinding.titleBar.ibtClose.setOnClickListener(v -> finish());
        mBinding.titleBar.setTitle(getString(R.string.text_title_setting));

        mBinding.rvSetting.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SettingAdapter(null);

        List<SettingSectionEntity> list = new ArrayList<>();

        list.add(new SettingSectionEntity(getString(R.string.text_setting_money)));
        String budget = ConfigManager.INSTANCE.getBudget() == 0 ? getString(R.string.text_no_budget) : getString(R.string.text_money_symbol) + ConfigManager.INSTANCE.getBudget();
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_monty_budget), budget)));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_setting_type_manage), null)));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_fast_accounting), getString(R.string.text_fast_tip), ConfigManager.INSTANCE.isFast())));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_successive_record), getString(R.string.text_successive_record_tip), ConfigManager.INSTANCE.isSuccessive())));


        list.add(new SettingSectionEntity(getString(R.string.text_setting_backup)));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_go_backup), getString(R.string.text_setting_go_backup_content))));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_setting_restore), getString(R.string.text_setting_restore_content))));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_setting_auto_backup), getString(R.string.text_setting_auto_backup_content), ConfigManager.INSTANCE.isAutoBackup())));

        list.add(new SettingSectionEntity(getString(R.string.text_setting_about_and_help)));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_about), getString(R.string.text_about_content))));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_setting_score), getString(R.string.text_setting_good_score) + "\uD83D\uDE18")));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_setting_donate), "")));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_setting_lisence))));
        list.add(new SettingSectionEntity(new SettingSectionEntity.Item(getString(R.string.text_setting_help))));

        mAdapter.setNewData(list);
        addListener();
        mBinding.rvSetting.setAdapter(mAdapter);
    }

    private void addListener() {
        mAdapter.setOnItemClickListener((adapter1, view, position) -> {
            switch (position) {
                case 1:
                    setBudget(position);
                    break;
                case 2:
                    goTypeManage();
                    break;
                case 6:
                    showBackupDialog();
                    break;
                case 7:
                    showRestoreDialog();
                    break;
                case 10:
                    goAbout();
                    break;
                case 11:
                    market();
                    break;
                case 12:
                    alipay();
                    break;
                case 13:
                    goOpenSource();
                    break;
                case 14:
                    AndroidUtil.INSTANCE.openWeb(this, "https://github.com/Bakumon/MoneyKeeper/blob/master/Help.md");
                    break;
                default:
                    break;
            }
        });
        // Switch
        mAdapter.setOnItemChildClickListener((adapter12, view, position) -> {
            switch (position) {
                case 3:
                    switchFast();
                    break;
                case 4:
                    switchSuccessive();
                    break;
                case 8:
                    switchAutoBackup(position);
                    break;
                default:
                    break;
            }
        });
    }

    private void setBudget(int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View contentView = layoutInflater.inflate(R.layout.dialog_input_budget, null, false);
        EditText editText = contentView.findViewById(R.id.edt_budget);
        editText.setText(ConfigManager.INSTANCE.getBudget() == 0 ? null : String.valueOf(ConfigManager.INSTANCE.getBudget()));
        editText.setSelection(editText.getText().length());
        new AlertDialog.Builder(this)
                .setView(contentView)
                .setTitle(R.string.text_set_budget)
                .setPositiveButton(R.string.text_affirm, (dialogInterface, i) -> {
                    String text = editText.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        ConfigManager.INSTANCE.setBudget(Integer.parseInt(text));
                    } else {
                        ConfigManager.INSTANCE.setBudget(0);
                    }
                    mAdapter.getData().get(position).t.content = ConfigManager.INSTANCE.getBudget() == 0 ? getString(R.string.text_no_budget) : getString(R.string.text_money_symbol) + ConfigManager.INSTANCE.getBudget();
                    mAdapter.notifyItemChanged(position);
                    SoftInputUtils.INSTANCE.hideSoftInput(editText);
                })
                .setNegativeButton(R.string.text_button_cancel, (dialogInterface, i) -> SoftInputUtils.INSTANCE.hideSoftInput(editText))
                .create()
                .show();
    }

    private void switchFast() {
        boolean oldIsConfigOpen = ConfigManager.INSTANCE.isFast();
        ConfigManager.INSTANCE.setIsFast(!oldIsConfigOpen);
    }

    private void switchSuccessive() {
        boolean oldIsConfigOpen = ConfigManager.INSTANCE.isSuccessive();
        ConfigManager.INSTANCE.setIsSuccessive(!oldIsConfigOpen);
    }

    private void switchAutoBackup(int position) {
        boolean oldIsConfigOpen = mAdapter.getData().get(position).t.isConfigOpen;
        if (oldIsConfigOpen) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.text_close_auto_backup)
                    .setMessage(R.string.text_close_auto_backup_tip)
                    .setNegativeButton(R.string.text_button_cancel, (dialog, which) -> mAdapter.notifyDataSetChanged())
                    .setPositiveButton(R.string.text_affirm, (dialog, which) -> setAutoBackup(position, false))
                    .create()
                    .show();
        } else {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ConfigManager.INSTANCE.setIsAutoBackup(true);
                initView();
                return;
            }
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, 11, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            .setRationale(R.string.text_storage_content)
                            .setPositiveButtonText(R.string.text_affirm)
                            .setNegativeButtonText(R.string.text_button_cancel)
                            .build());
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case 11:
                ConfigManager.INSTANCE.setIsAutoBackup(true);
                initView();
                break;
            case 12:
                backupDB();
                break;
            case 13:
                restore();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setRationale(R.string.text_storage_permission_tip)
                    .setTitle(R.string.text_storage)
                    .setPositiveButton(R.string.text_affirm)
                    .setNegativeButton(R.string.text_button_cancel)
                    .build()
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            initView();
        }
    }

    private void setAutoBackup(int position, boolean isBackup) {
        ConfigManager.INSTANCE.setIsAutoBackup(isBackup);
        mAdapter.getData().get(position).t.isConfigOpen = isBackup;
        mAdapter.notifyDataSetChanged();
    }

    private void showBackupDialog() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            backupDB();
            return;
        }
        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, 12, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setRationale(R.string.text_storage_content)
                        .setPositiveButtonText(R.string.text_affirm)
                        .setNegativeButtonText(R.string.text_button_cancel)
                        .build());
    }

    private void backupDB() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.text_backup)
                .setMessage(R.string.text_backup_save)
                .setNegativeButton(R.string.text_button_cancel, null)
                .setPositiveButton(R.string.text_affirm, (dialog, which) ->
                        mDisposable.add(mViewModel.backupDB()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> ToastUtils.INSTANCE.show(R.string.toast_backup_success),
                                        throwable -> {
                                            ToastUtils.INSTANCE.show(R.string.toast_backup_fail);
                                            Log.e(TAG, "备份失败", throwable);
                                        })))
                .create()
                .show();
    }

    private void showRestoreDialog() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            restore();
            return;
        }
        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, 13, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setRationale(R.string.text_storage_content)
                        .setPositiveButtonText(R.string.text_affirm)
                        .setNegativeButtonText(R.string.text_button_cancel)
                        .build());
    }

    private void restore() {
        mDisposable.add(mViewModel.getBackupFiles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(backupBeans -> {
                            BackupFliesDialog dialog = new BackupFliesDialog(this, backupBeans);
                            dialog.setOnItemClickListener(file -> restoreDB(file.getPath()));
                            dialog.show();
                        },
                        throwable -> {
                            ToastUtils.INSTANCE.show(R.string.toast_backup_list_fail);
                            Log.e(TAG, "备份文件列表获取失败", throwable);
                        }));
    }

    private void restoreDB(String restoreFile) {
        mDisposable.add(mViewModel.restoreDB(restoreFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Floo.stack(this)
                                .target(Router.IndexKey.INDEX_KEY_HOME)
                                .result("refresh")
                                .start(),
                        throwable -> {
                            ToastUtils.INSTANCE.show(R.string.toast_restore_fail);
                            Log.e(TAG, "恢复备份失败", throwable);
                        }));
    }

    private void goTypeManage() {
        Floo.navigation(this, Router.Url.URL_TYPE_MANAGE)
                .start();
    }

    private void goAbout() {
        Floo.navigation(this, Router.Url.URL_ABOUT)
                .start();
    }

    private void goOpenSource() {
        Floo.navigation(this, Router.Url.URL_OPEN_SOURCE)
                .start();
    }

    private void market() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtils.INSTANCE.show(R.string.toast_not_install_market);
            e.printStackTrace();
        }
    }

    private void alipay() {
        // https://fama.alipay.com/qrcode/qrcodelist.htm?qrCodeType=P  二维码地址
        // http://cli.im/deqr/ 解析二维码
        // aex01251c8foqaprudcp503
        if (AlipayZeroSdk.INSTANCE.hasInstalledAlipayClient(this)) {
            AlipayZeroSdk.INSTANCE.startAlipayClient(this, "aex01251c8foqaprudcp503");
        } else {
            ToastUtils.INSTANCE.show(R.string.toast_not_install_alipay);
        }
    }
}
