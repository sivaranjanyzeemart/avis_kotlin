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
import zeemart.asia.buyers.models.invoice.Invoice.LinkedCreditNotes

class LinkedCreditNotesListAdapter(
    private val context: Context,
    private val lstLinkedCreditNotes: List<LinkedCreditNotes>?,
    private val tapOnInvoice: TapOnInvoiceListener
) : RecyclerView.Adapter<LinkedCreditNotesListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.layout_linked_credit_note_list,
            viewGroup,
            false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (lstLinkedCreditNotes != null) {
            viewHolder.txtLinkedCreditNote.text =
                context.resources.getString(R.string.txt_credit_note_with_hash_tag) + " " + lstLinkedCreditNotes[position].invoiceNum
            viewHolder.txtLinkedCreditNote.setTextColor(context.resources.getColor(R.color.text_blue))
            viewHolder.lytLinkedCreditNote.setOnClickListener {
                CommonMethods.avoidMultipleClicks(viewHolder.lytLinkedCreditNote)
                tapOnInvoice.onInvoiceTapped(lstLinkedCreditNotes[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return lstLinkedCreditNotes!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLinkedCreditNote: TextView
        var lytLinkedCreditNote: ConstraintLayout

        init {
            txtLinkedCreditNote = itemView.findViewById(R.id.txt_linked_credit_note_value)
            lytLinkedCreditNote = itemView.findViewById(R.id.lyt_linked_credit_note)
            setTypefaceView(
                txtLinkedCreditNote,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    interface TapOnInvoiceListener {
        fun onInvoiceTapped(linkedCreditNotes: LinkedCreditNotes?)
    }
}