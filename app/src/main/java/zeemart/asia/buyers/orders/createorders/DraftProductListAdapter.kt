package zeemart.asia.buyers.orders.createordersimport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.marketlist.Product

internal class DraftProductListAdapter(
    private val context: Context,
    private val products: List<Product>
) : RecyclerView.Adapter<DraftProductListAdapter.ViewHolderItem>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        i: Int
    ): DraftProductListAdapter.ViewHolderItem {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_draft_product_row, parent, false)
        return DraftProductListAdapter.ViewHolderItem(v)
    }

    override fun onBindViewHolder(
        viewHolderItem: DraftProductListAdapter.ViewHolderItem,
        position: Int
    ) {
        if (!StringHelper.isStringNullOrEmpty(products[position].customName)) {
            viewHolderItem.txtProductName.text = products[position].customName
        } else {
            viewHolderItem.txtProductName.text = products[position].productName
        }
        viewHolderItem.txtProductQuantity.text =
            products[position].quantityDisplayValue + " " + returnShortNameValueUnitSize(
                products[position].unitSize
            )
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class ViewHolderItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtProductName: TextView
        var txtProductQuantity: TextView

        init {
            txtProductName = itemView.findViewById(R.id.txt_draft_product_name)
            txtProductQuantity = itemView.findViewById(R.id.txt_draft_product_quantity)
            setTypefaceView(txtProductName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }
}