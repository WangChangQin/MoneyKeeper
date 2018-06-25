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

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import me.bakumon.moneykeeper.BR
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.BaseDataBindingAdapter
import java.io.File

/**
 * 恢复备份对话框
 *
 * @author Bakumon https://bakumon.me
 */
class BackupFliesDialog(private val mContext: Context, private val mBackupBeans: List<BackupBean>) {
    private lateinit var mDialog: BottomSheetDialog
    private var mListen: ((File) -> Unit)? = null

    init {
        setupDialog()
    }

    private fun setupDialog() {
        val layoutInflater = LayoutInflater.from(mContext)
        val contentView = layoutInflater.inflate(R.layout.dialog_backup_files, null, false)
        val rvFiles = contentView.findViewById<RecyclerView>(R.id.rv_files)
        rvFiles.layoutManager = LinearLayoutManager(mContext)
        val adapter = FilesAdapter(null)
        rvFiles.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            dismiss()
            mListen?.invoke(adapter.data[position].file)
        }
        adapter.setNewData(mBackupBeans)

        mDialog = BottomSheetDialog(mContext)
        mDialog.setContentView(contentView)
    }

    fun show() {
        mDialog.show()
    }

    fun dismiss() {
        mDialog.dismiss()
    }

    fun setListener(listener: (File) -> Unit) {
        this.mListen = listener
    }

    internal inner class FilesAdapter(data: List<BackupBean>?) : BaseDataBindingAdapter<BackupBean>(R.layout.item_backup_files, data) {

        override fun convert(helper: BaseDataBindingAdapter.DataBindingViewHolder, item: BackupBean) {
            val binding = helper.binding

            binding.setVariable(BR.backupBean, item)

            binding.executePendingBindings()
        }
    }

}
