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

package me.bakumon.moneykeeper.ui.review

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.bakumon.moneykeeper.Injection
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseActivity
import me.bakumon.moneykeeper.databinding.ActivityReviewBinding
import me.bakumon.moneykeeper.utill.ToastUtils

/**
 * 回顾
 * 查看年账单或某几个月的数据
 *
 * @author Bakumon https://bakumon
 */
class ReviewActivity : BaseActivity() {
    private lateinit var mBinding: ActivityReviewBinding
    private lateinit var mViewModel: ReviewModel
    private lateinit var mAdapter: ReviewAdapter

    override val layoutId: Int
        get() = R.layout.activity_review

    override fun onInit(savedInstanceState: Bundle?) {
        mBinding = getDataBinding()
        val viewModelFactory = Injection.provideViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(ReviewModel::class.java)

        initView()
    }

    private fun initView() {
        mBinding.titleBar?.title = "2018"
        mBinding.titleBar?.ivTitle?.visibility = View.VISIBLE
        mBinding.titleBar?.llTitle?.setOnClickListener { ToastUtils.show("title") }
        mBinding.titleBar?.ibtClose?.setOnClickListener { finish() }

        mBinding.rvReview.layoutManager = LinearLayoutManager(this)
        mAdapter = ReviewAdapter(null)
        mBinding.rvReview.adapter = mAdapter

        updateData()
    }

    private fun updateData() {
        mDisposable.add(mViewModel.getYearSumMoney(2018)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mBinding.sumMoneyBeanList = it
                }
                ) {
                    ToastUtils.show(R.string.toast_get_review_sum_money_fail)
                    Log.e(TAG, "获取回顾数据失败", it)
                })


        mDisposable.add(mViewModel.getMonthOfYearSumMoney(2018)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mAdapter.setNewData(ReviewItemDataConverter.getBarEntryList(it))
                    if (it.isEmpty()) {
                        mAdapter.emptyView = inflate(R.layout.layout_statistics_empty)
                    }
                }
                ) {
                    ToastUtils.show(R.string.toast_get_review_fail)
                    Log.e(TAG, "获取回顾数据失败", it)
                })
    }

    companion object {
        private val TAG = ReviewActivity::class.java.simpleName
    }
}
