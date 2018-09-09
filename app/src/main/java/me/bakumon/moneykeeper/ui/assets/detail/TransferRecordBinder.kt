package me.bakumon.moneykeeper.ui.assets.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.database.entity.AssetsTransferRecord
import me.bakumon.moneykeeper.utill.BigDecimalUtil
import me.bakumon.moneykeeper.utill.DateUtils
import me.bakumon.moneykeeper.utill.ResourcesUtil
import me.drakeet.multitype.ItemViewBinder

/**
 * @author Bakumon https://bakumon.me
 */
class TransferRecordBinder : ItemViewBinder<AssetsTransferRecord, TransferRecordBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val root = inflater.inflate(R.layout.item_assets_record, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: AssetsTransferRecord) {
        val content: String = ("id" + item.assetsIdFrom) + " ➡ " + ("id" + item.assetsIdTo)
        holder.ivTypeImg.setImageResource(ResourcesUtil.getTypeImgId(holder.ivTypeImg.context, "ic_transform"))
        holder.tvTypeName.text = holder.tvTypeName.context.resources.getString(R.string.text_assets_transfer)
        holder.tvRemark.text = content
        holder.tvSubtitle.visibility = View.VISIBLE
        holder.tvSubtitle.text = item.remark
        holder.tvMoney.visibility = View.VISIBLE
        holder.tvMoney.text = BigDecimalUtil.fen2Yuan(item.money)
        holder.tvTime.text = DateUtils.date2MonthDay(item.time)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivTypeImg: ImageView = itemView.findViewById(R.id.ivTypeImg)
        val tvTypeName: TextView = itemView.findViewById(R.id.tvTypeName)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        val tvRemark: TextView = itemView.findViewById(R.id.tvRemark)
        val tvMoney: TextView = itemView.findViewById(R.id.tvMoney)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }
}
