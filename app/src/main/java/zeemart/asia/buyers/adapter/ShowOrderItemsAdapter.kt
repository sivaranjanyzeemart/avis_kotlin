package zeemart.asia.buyers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.marketlist.Product

/**
 * Created by ParulBhandari on 12/7/2017.
 */
class ShowOrderItemsAdapter(var products: List<Product>) :
    RecyclerView.Adapter<ShowOrderItemsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (products[position].customName != null) {
            holder.txtProductCustomName.visibility = View.VISIBLE
            holder.txtProductCustomName.text = products[position].productName
            holder.txtItemName.text = products[position].customName
        } else {
            holder.txtItemName.text = products[position].productName
            holder.txtProductCustomName.visibility = View.GONE
        }
        holder.txtItemQuantity.text =
            products[position].quantityDisplayValue + " " + returnShortNameValueUnitSizeForQuantity(
                products[position].unitSize
            )
        setUIFont(holder)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtItemQuantity: TextView
        var txtProductCustomName: TextView

        init {
            txtItemName = itemView.findViewById(R.id.txt_per_price)
            txtItemQuantity = itemView.findViewById(R.id.txt_item_quantity)
            txtProductCustomName = itemView.findViewById(R.id.txt_item_custom_name)
        }
    }

    private fun setUIFont(holder: ViewHolder) {
        setTypefaceView(
            holder.txtItemQuantity,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(holder.txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            holder.txtProductCustomName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
        )
    }
}