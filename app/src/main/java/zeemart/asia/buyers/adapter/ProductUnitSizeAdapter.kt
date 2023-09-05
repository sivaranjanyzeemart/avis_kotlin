package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize

/**
 * Created by ParulBhandari on 12/19/2017.
 */
class ProductUnitSizeAdapter(
    var context: Context,
    var priceList: List<ProductPriceList>,
    var changeQuantitydialog: CustomChangeQuantityDialog
) : RecyclerView.Adapter<ProductUnitSizeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_change_quantity_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uom = returnShortNameValueUnitSize(priceList[position].unitSize)
        val mUnitPrice = priceList[position].price!!.getDisplayValueWithUom(uom)
        holder.txtUnitPrice.text = mUnitPrice
        holder.txtUnitSize.text =
            returnShortNameValueUnitSize(priceList[position].unitSize) //unitSizeMapping.get(priceList.get(position).getUnitSize()).getShortName());
        holder.itemCell.setOnClickListener {
            if (priceList[position].selected) {
                holder.imgTick.visibility = View.INVISIBLE
                priceList[position].selected = false
                holder.itemCell.setBackgroundColor(context.resources.getColor(R.color.faint_grey))
                holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.black))
                holder.txtUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
            } else {
                holder.imgTick.visibility = View.VISIBLE
                priceList[position].selected = true
                for (i in priceList.indices) {
                    if (i != position) {
                        priceList[i].selected = false
                    }
                }
                notifyDataSetChanged()
                changeQuantitydialog.onUnitSizeSelectedCalled(changeQuantitydialog)
                holder.itemCell.setBackgroundColor(context.resources.getColor(R.color.text_blue))
                holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.white))
                holder.txtUnitPrice.setTextColor(context.resources.getColor(R.color.white))
            }
        }
        if (priceList[position].selected) {
            holder.imgTick.visibility = View.VISIBLE
            priceList[position].selected = true
            holder.itemCell.setBackgroundColor(context.resources.getColor(R.color.text_blue))
            holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.white))
            holder.txtUnitPrice.setTextColor(context.resources.getColor(R.color.white))
        } else {
            holder.imgTick.visibility = View.INVISIBLE
            priceList[position].selected = false
            holder.itemCell.setBackgroundColor(context.resources.getColor(R.color.faint_grey))
            holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.black))
            holder.txtUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
        }
    }

    override fun getItemCount(): Int {
        return priceList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtUnitSize: TextView
        var txtUnitPrice: TextView
        var imgTick: ImageView
        var itemCell: RelativeLayout

        init {
            txtUnitSize = itemView.findViewById(R.id.txt_unit_size)
            setTypefaceView(txtUnitSize, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtUnitPrice = itemView.findViewById(R.id.txt_unitPrice)
            setTypefaceView(txtUnitPrice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            imgTick = itemView.findViewById(R.id.img_unit_selected)
            itemCell = itemView.findViewById(R.id.lyt_unit_size_cell)
        }
    }
}