package zeemart.asia.buyers.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.marketlist.Product

/**
 * Created by RajPrudhviMarella on 17/June/2021.
 */
class GrnOrderDetailsProductListAdapter(
    private val context: Context,
    private val products: List<Product>
) : RecyclerView.Adapter<GrnOrderDetailsProductListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_grn_order_details_product_list,
            parent,
            false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("===>", products[position].toString())
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && products[position].customName != null
        ) {
            holder.txtItemName.text = products[position].customName + ""
            holder.txtCustName.text = products[position].productName + ""
            holder.txtCustName.visibility = View.VISIBLE
        } else {
            holder.txtItemName.text = products[position].productName + ""
            holder.txtCustName.visibility = View.GONE
        }
        holder.edtProductQuantity.text = products[position].quantity.toString() + ""
        holder.txtUnitSizeValue.text = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
            products[position].unitSize
        )
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtCustName: TextView
        var edtProductQuantity: TextView
        var txtUnitSizeValue: TextView

        init {
            txtItemName = itemView.findViewById(R.id.inventory_product_txt_product_name)
            edtProductQuantity = itemView.findViewById(R.id.inventory_product_txt_quantity)
            txtUnitSizeValue = itemView.findViewById(R.id.inventory_product_txt_uom)
            txtCustName = itemView.findViewById(R.id.inventory_product_txt_product_cust_name)
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(
                edtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtUnitSizeValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtCustName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS)
        }
    }
}