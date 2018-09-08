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

package me.bakumon.moneykeeper.ui.assets

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_assets.*
import me.bakumon.moneykeeper.ConfigManager
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.Router
import me.bakumon.moneykeeper.database.entity.Assets
import me.bakumon.moneykeeper.database.entity.AssetsMoneyBean
import me.bakumon.moneykeeper.ui.common.BaseActivity
import me.bakumon.moneykeeper.ui.common.Empty
import me.bakumon.moneykeeper.ui.common.EmptyViewBinder
import me.bakumon.moneykeeper.ui.setting.Category
import me.bakumon.moneykeeper.ui.setting.CategoryViewBinder
import me.bakumon.moneykeeper.utill.BigDecimalUtil
import me.bakumon.moneykeeper.utill.ToastUtils
import me.drakeet.floo.Floo
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register

/**
 * AssetsActivity
 *
 * @author bakumon https://bakumon.me
 * @date 2018/8/28
 */
class AssetsActivity : BaseActivity() {

    private lateinit var mViewModel: AssetsViewModel
    private lateinit var mAdapter: MultiTypeAdapter

    override val layoutId: Int
        get() = R.layout.activity_assets

    override fun isChangeStatusColor(): Boolean {
        return false
    }

    @SuppressLint("SetTextI18n")
    override fun onInitView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btnAdd.setOnClickListener { ToastUtils.show("转账") }

        val text = if (ConfigManager.symbol.isEmpty()) "" else "(" + ConfigManager.symbol + ")"
        tvNetAssetsTitle.text = getString(R.string.text_assets) + text
    }

    override fun onInit(savedInstanceState: Bundle?) {
        mAdapter = MultiTypeAdapter()
        mAdapter.register(Category::class, CategoryViewBinder())
        mAdapter.register(Assets::class, AssetsViewBinder())
        mAdapter.register(Empty::class, EmptyViewBinder())
        rvAssets.adapter = mAdapter

        mViewModel = getViewModel()
        getAssetsMoney()
        getAssetsList()
    }

    private fun getAssetsMoney() {
        mViewModel.getAssetsMoney().observe(this, Observer {
            setAssetsMoney(it)
        })
    }

    private fun setAssetsMoney(it: AssetsMoneyBean?) {
        tvNetAssetsMoney.text = BigDecimalUtil.fen2Yuan(it?.netAssets)
        tvAllAssetsMoney.text = BigDecimalUtil.fen2Yuan(it?.allAssets)
        tvLiabilitiesMoney.text = BigDecimalUtil.fen2Yuan(it?.liabilityAssets)
    }

    private fun getAssetsList() {
        mViewModel.getAssets().observe(this, Observer {
            if (it != null) {
                setItems(it)
            }
        })
    }

    private fun setItems(list: List<Assets>) {
        if (list.size < 2) {
            btnAdd.visibility = View.GONE
        } else {
            btnAdd.visibility = View.VISIBLE
        }
        val items = Items()
        if (list.isEmpty()) {
            items.add(Empty(getString(R.string.text_assets_no_account), Gravity.CENTER))
        } else {
            items.add(Category(getString(R.string.text_assets_account)))
            items.addAll(list)
        }
        mAdapter.items = items
        mAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_assets, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.action_add -> Floo.navigation(this, Router.Url.URL_CHOOSE_ASSETS).start()
            android.R.id.home -> finish()
        }
        return true
    }

}
