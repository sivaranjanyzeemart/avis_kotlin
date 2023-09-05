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
import zeemart.asia.buyers.models.order.Orders

class LinkedInvoiceListAdapter(
    private val context: Context,
    private val lstLinkedInvoices: List<Orders.LinkedInvoice>?,
    private val tapOnInvoice: TapOnInvoiceListener
) : RecyclerView.Adapter<LinkedInvoiceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.layout_linked_invoice_list, viewGroup, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (lstLinkedInvoices != null) {
            viewHolder.txtLinkedInvoice.text =
                context.resources.getString(R.string.txt_invoice_with_hash_tag) + " " + lstLinkedInvoices[position].invoiceNum
            viewHolder.txtLinkedInvoice.setTextColor(context.resources.getColor(R.color.color_azul_two))
            viewHolder.lytLinkedInvoice.setOnClickListener {
                CommonMethods.avoidMultipleClicks(viewHolder.lytLinkedInvoice)
                tapOnInvoice.onInvoiceTapped(lstLinkedInvoices[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return lstLinkedInvoices!!.size
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

    interface TapOnInvoiceListener {
        fun onInvoiceTapped(linkedInvoice: Orders.LinkedInvoice?)
    }
}