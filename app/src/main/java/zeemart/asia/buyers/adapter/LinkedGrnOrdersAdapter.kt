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
import zeemart.asia.buyers.models.order.Orders.Grn

/**
 * Created by RajPrudhviMarella on 17/June/2021.
 */
class LinkedGrnOrdersAdapter(
    private val lstLinkedGrns: List<Grn>?,
    private val context: Context,
    private val tapOnOrder: TapOnOrder
) : RecyclerView.Adapter<LinkedGrnOrdersAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_linked_invoice_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (lstLinkedGrns != null) {
            viewHolder.txtLinkedInvoice.text =
                context.resources.getString(R.string.txt_grn_with_hashtag) + lstLinkedGrns[position].grnId
            viewHolder.txtLinkedInvoice.setTextColor(context.resources.getColor(R.color.color_azul_two))
            viewHolder.lytLinkedInvoice.setOnClickListener {
                CommonMethods.avoidMultipleClicks(viewHolder.lytLinkedInvoice)
                tapOnOrder.onOrderTapped(lstLinkedGrns[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return lstLinkedGrns!!.size
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
        fun onOrderTapped(linkedInvoice: Grn?)
    }
}