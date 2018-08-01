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

package me.bakumon.moneykeeper.base

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.api.ApiEmptyResponse
import me.bakumon.moneykeeper.api.ApiErrorResponse
import me.bakumon.moneykeeper.api.ApiSuccessResponse
import me.bakumon.moneykeeper.utill.StatusBarUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import okhttp3.ResponseBody

/**
 * 1.沉浸式状态栏
 * 2.ViewDataBinding 封装
 *
 * @author Bakumon
 * @date 18-1-17
 */
abstract class BaseActivity : AppCompatActivity() {

    private lateinit var dataBinding: ViewDataBinding
    protected val mDisposable = CompositeDisposable()

    /**
     * 子类必须实现，用于创建 view
     *
     * @return 布局文件 Id
     */
    @get:LayoutRes
    protected abstract val layoutId: Int

    /**
     * 是否已经设置了沉浸式状态栏
     */
    private var isSetupImmersive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, layoutId)
        onInit(savedInstanceState)
    }

    /**
     * 获取 ViewDataBinding
     *
     * @param <T> BaseActivity#getLayoutId() 布局创建的 ViewDataBinding
     * 如 R.layout.activity_demo 会创建出 ActivityDemoBinding.java
     * @return T
    </T> */
    @Suppress("UNCHECKED_CAST")
    protected fun <T : ViewDataBinding> getDataBinding(): T {
        return dataBinding as T
    }

    /**
     * 实例化 BaseViewModel 子类
     */
    inline fun <reified T : BaseViewModel> getViewModel(): T {
        val viewModelFactory = Injection.provideViewModelFactory()
        return ViewModelProviders.of(this, viewModelFactory).get(T::class.java)
    }

    /**
     * WebDAV 云备份
     */
    fun cloudBackup(mViewModel: BaseViewModel) {
        mViewModel.getList().observe(this, Observer {
            when (it) {
                is ApiEmptyResponse<ResponseBody> -> backupUpload(mViewModel)
                is ApiSuccessResponse<ResponseBody> -> backupUpload(mViewModel)
                is ApiErrorResponse<ResponseBody> -> {
                    if (it.code == 404) {
                        mViewModel.createDir().observe(this, Observer {
                            when (it) {
                                is ApiSuccessResponse<ResponseBody> -> backupUpload(mViewModel)
                                is ApiEmptyResponse<ResponseBody> -> backupUpload(mViewModel)
                                is ApiErrorResponse<ResponseBody> -> ToastUtils.show(it.errorMessage)
                            }
                        })
                    } else {
                        onCloudBackupFail(it)
                    }
                }
            }
        })
    }

    private fun backupUpload(mViewModel: BaseViewModel) {
        // 上传文件
        mViewModel.backup().observe(this, Observer {
            when (it) {
                is ApiSuccessResponse<ResponseBody> -> onCloudBackupSuccess()
                is ApiEmptyResponse<ResponseBody> -> onCloudBackupSuccess()
                is ApiErrorResponse<ResponseBody> -> onCloudBackupFail(it)
            }
        })
    }

    /**
     * WebDAV 云备份成功
     * 子类可以重写此方法
     */
    open fun onCloudBackupSuccess() {

    }

    /**
     * WebDAV 云备份失败
     * 子类可以重写此方法
     */
    open fun onCloudBackupFail(errorResponse: ApiErrorResponse<ResponseBody>) {
        ToastUtils.show(errorResponse.errorMessage)
    }

    /**
     * 设置沉浸式状态栏
     */
    private fun setImmersiveStatus() {
        val views = setImmersiveView()
        if (views.isEmpty()) {
            return
        }
        StatusBarUtil.immersive(this)
        for (view in views) {
            StatusBarUtil.setPaddingSmart(this, view)
        }
    }

    /**
     * 子类可以重写该方法设置沉浸式状态栏
     *
     * @return view[]大小为0,则不启用沉浸式
     */
    protected fun setImmersiveView(): Array<View> {
        // 默认使用第一个子 View
        val rootView = dataBinding.root as ViewGroup
        return arrayOf(rootView.getChildAt(0))
    }

    override fun onResume() {
        super.onResume()
        if (!isSetupImmersive) {
            setImmersiveStatus()
            isSetupImmersive = true
        }
    }

    /**
     * 开始的方法
     *
     * @param savedInstanceState 保存的 Bundle
     */
    protected abstract fun onInit(savedInstanceState: Bundle?)

    /**
     * inflate view root：null，attachToRoot：false
     *
     * @param resource 布局 id
     * @return view
     */
    protected fun inflate(@LayoutRes resource: Int): View {
        return layoutInflater.inflate(resource, null, false)
    }

    @Suppress("DEPRECATION")
    override fun getResources(): Resources {
        // 固定字体大小，不随系统字体大小改变
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
    }

}