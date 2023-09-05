package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.invoices.InvoiceDetailActivity
import zeemart.asia.buyers.models.invoice.Invoice
import java.util.*

/**
 * Created by RajPrudhviMarella on 7/2/2020.
 */
class ReviewPaymentInvoiceListAdapter(
    private val context: Context,
    private var lstInvoices: List<Invoice>,
    private val mListener: ReviewPaymentInvoiceSelectedListener
) : RecyclerView.Adapter<ReviewPaymentInvoiceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.layout_review_payment_invoice_row,
            viewGroup,
            false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.txtInvoiceNumber.text = "#" + lstInvoices[position].invoiceNum
        viewHolder.txtInvoiceDueAmount.text = lstInvoices[position].totalCharge!!.displayValue
        val paymentDue = lstInvoices[position].paymentDueDate!! * 1000
        val now = Calendar.getInstance(DateHelper.marketTimeZone)
        val currentDate = now.timeInMillis
        if (currentDate > paymentDue) {
            val intDays = DateHelper.getDaysBetweenDates(paymentDue, currentDate)
            viewHolder.txtOverDue.text =
                String.format(context.resources.getString(R.string.txt_due_days), intDays)
            viewHolder.txtOverDue.setTextColor(context.resources.getColor(R.color.pinky_red))
        } else {
            viewHolder.txtOverDue.text = String.format(
                context.resources.getString(R.string.txt_due_on),
                DateHelper.getDateInDateMonthFormat(
                    lstInvoices[position].paymentDueDate
                )
            )
            viewHolder.txtOverDue.setTextColor(context.resources.getColor(R.color.grey_medium))
        }
        viewHolder.checkBox.setOnClickListener {
            if (viewHolder.checkBox.isChecked) {
                viewHolder.checkBox.isChecked = true
                lstInvoices[position].isInvoiceSelected = true
                mListener.onInvoiceItemSelected(lstInvoices)
            } else {
                viewHolder.checkBox.isChecked = false
                lstInvoices[position].isInvoiceSelected = false
                mListener.onInvoiceItemSelected(lstInvoices)
            }
            notifyDataSetChanged()
        }
        if (lstInvoices[position].isInvoiceSelected) {
            viewHolder.checkBox.isChecked = true
            lstInvoices[position].isInvoiceSelected = true
        } else {
            viewHolder.checkBox.isChecked = false
            lstInvoices[position].isInvoiceSelected = false
        }
        viewHolder.txtInvoiceDueAmount.setOnClickListener {
            CommonMethods.avoidMultipleClicks(viewHolder.txtInvoiceDueAmount)
            val newIntent = Intent(context, InvoiceDetailActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_ID, lstInvoices[position].invoiceId)
            context.startActivity(newIntent)
        }
    }

    override fun getItemCount(): Int {
        return lstInvoices.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtInvoiceNumber: TextView
        val txtOverDue: TextView
        val txtInvoiceDueAmount: TextView
        val checkBox: CheckBox

        init {
            txtInvoiceNumber = itemView.findViewById(R.id.txt_invoice_number)
            txtOverDue = itemView.findViewById(R.id.txt_invoice_over_due)
            txtInvoiceDueAmount = itemView.findViewById(R.id.txt_invoice_due_amount)
            checkBox = itemView.findViewById(R.id.check_box_select_invoice_payment)
            setTypefaceView(
                txtInvoiceNumber,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtOverDue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtInvoiceDueAmount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    interface ReviewPaymentInvoiceSelectedListener {
        fun onInvoiceItemSelected(invoice: List<Invoice>?)
    }

    fun updateDataList(invoice: List<Invoice>) {
        lstInvoices = invoice
        notifyDataSetChanged()
    }
}