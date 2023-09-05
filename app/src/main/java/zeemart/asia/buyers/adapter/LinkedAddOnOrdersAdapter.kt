package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods

/**
 * Created by Rajprudhvi on 26/Nov/2020.
 */
class LinkedAddOnOrdersAdapter(
    private val lstLinkedOrders: List<String>?,
    private val context: Context,
    private val tapOnOrder: TapOnOrder
) : RecyclerView.Adapter<LinkedAddOnOrdersAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_linked_invoice_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (lstLinkedOrders != null) {
            viewHolder.txtLinkedInvoice.text =
                context.resources.getString(R.string.txt_order_with_hash_tag) + " " + lstLinkedOrders[position]
            viewHolder.txtLinkedInvoice.setTextColor(context.resources.getColor(R.color.color_azul_two))
            viewHolder.lytLinkedInvoice.setOnClickListener {
                CommonMethods.avoidMultipleClicks(viewHolder.lytLinkedInvoice)
                tapOnOrder.onOrderTapped(lstLinkedOrders[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return lstLinkedOrders!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLinkedInvoice: TextView
        var lytLinkedInvoice: ConstraintLayout

        init {
            txtLinkedInvoice = itemView.findViewById(R.id.txt_linked_invoice_value)
            lytLinkedInvoice = itemView.findViewById(R.id.lyt_linked_invoice)
            setTypefaceView(txtLinkedInvoice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    interface TapOnOrder {
        fun onOrderTapped(linkedInvoice: String?)
    }
}