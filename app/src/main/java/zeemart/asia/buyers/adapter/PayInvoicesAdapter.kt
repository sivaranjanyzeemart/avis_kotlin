package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.invoiceimport.EInvoicesGroupBySupplier
import java.util.*

/**
 * Created by RajPrudhviMarella on 6/2/2020.
 */
class PayInvoicesAdapter(
    private val context: Context,
    lstInvoices: List<EInvoicesGroupBySupplier.EInvoiceWithPagination.InvoicesGroupBySupplier>,
    payInvoiceSelectedListener: PayInvoiceSelectedListener
) : RecyclerView.Adapter<PayInvoicesAdapter.ViewHolder?>() {
    private val lstInvoices: List<EInvoicesGroupBySupplier.EInvoiceWithPagination.InvoicesGroupBySupplier>
    private val mListener: PayInvoiceSelectedListener

    init {
        this.lstInvoices = lstInvoices
        mListener = payInvoiceSelectedListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.layout_pay_invoices, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.txtSupplierName.setText(lstInvoices[position].supplier?.supplierName)
        val priceDetails = PriceDetails()
        var overDueCount = 0
        for (i in 0 until lstInvoices[position].invoices?.size!!) {
            priceDetails.addAmount(
                lstInvoices[position].invoices?.get(i)?.totalCharge?.amount!!
            )
            val paymentDue: Long =
                lstInvoices[position].invoices?.get(i)?.paymentDueDate?.times(1000)!!
            val now: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            val currentDate = now.timeInMillis
            if (currentDate > paymentDue) {
                overDueCount = overDueCount + 1
            }
        }
        if (overDueCount > 0) {
            viewHolder.txtOverDue.setVisibility(View.VISIBLE)
            viewHolder.txtOverDue.setText(
                String.format(
                    context.resources.getString(R.string.txt_total_over_dues),
                    overDueCount
                )
            )
            if (lstInvoices[position].invoices?.size === 1) {
                val totalOverDues = java.lang.String.format(
                    context.resources.getString(R.string.txt_pending_invoice),
                    lstInvoices[position].invoices?.size
                ) + " • "
                viewHolder.txtPendingInvoices.setText(totalOverDues)
            } else {
                val totalOverDues = java.lang.String.format(
                    context.resources.getString(R.string.txt_pending_invoices),
                    lstInvoices[position].invoices?.size
                ) + " • "
                viewHolder.txtPendingInvoices.setText(totalOverDues)
            }
        } else {
            viewHolder.txtOverDue.setVisibility(View.GONE)
            if (lstInvoices[position].invoices?.size === 1) {
                viewHolder.txtPendingInvoices.setText(
                    java.lang.String.format(
                        context.resources.getString(
                            R.string.txt_pending_invoice
                        ), lstInvoices[position].invoices?.size
                    )
                )
            } else {
                viewHolder.txtPendingInvoices.setText(
                    java.lang.String.format(
                        context.resources.getString(
                            R.string.txt_pending_invoices
                        ), lstInvoices[position].invoices?.size
                    )
                )
            }
        }
        viewHolder.txtAmount.setText(priceDetails.displayValue)
        if (StringHelper.isStringNullOrEmpty(lstInvoices[position].supplier?.logoURL)) {
            viewHolder.imgSupplier.visibility = View.INVISIBLE
            viewHolder.lytSupplierThumbNail.setVisibility(View.VISIBLE)
            viewHolder.lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    lstInvoices[position].supplier?.supplierName!!, context
                )
            )
            viewHolder.txtSupplierThumbNail.setText(
                CommonMethods.SupplierThumbNailShortCutText(
                    lstInvoices[position].supplier?.supplierName!!
                )
            )
            viewHolder.txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    lstInvoices[position].supplier?.supplierName!!, context
                )
            )
        } else {
            viewHolder.imgSupplier.visibility = View.VISIBLE
            viewHolder.lytSupplierThumbNail.setVisibility(View.GONE)
            Picasso.get().load(lstInvoices[position].supplier?.logoURL)
                .placeholder(R.drawable.placeholder_all).resize(
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
            ).into(viewHolder.imgSupplier)
        }
        if (StringHelper.isStringNullOrEmpty(lstInvoices[position].supplier?.logoURL)) {
            viewHolder.imgSupplier.setImageResource(R.drawable.placeholder_all)
        } else {
            Picasso.get().load(lstInvoices[position].supplier?.logoURL)
                .placeholder(R.drawable.placeholder_all).resize(
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
            ).into(viewHolder.imgSupplier)
        }
        viewHolder.lytRow.setOnClickListener(View.OnClickListener {
            CommonMethods.avoidMultipleClicks(viewHolder.lytRow)
            mListener.onPayInvoicesItemSelected(lstInvoices[position])
        })
    }

    override fun getItemCount(): Int {
        return lstInvoices.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSupplierName: TextView
        val txtPendingInvoices: TextView
        val txtOverDue: TextView
        val imgSupplier: ImageView
        val txtAmount: TextView
        val lytRow: ConstraintLayout
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            txtSupplierName = itemView.findViewById<TextView>(R.id.txt_payment_supplier_name)
            txtPendingInvoices = itemView.findViewById<TextView>(R.id.txt_number_of_invoices)
            txtOverDue = itemView.findViewById<TextView>(R.id.txt_over_due)
            imgSupplier = itemView.findViewById(R.id.img_payment_supplier)
            txtAmount = itemView.findViewById<TextView>(R.id.txt_payment_total)
            lytRow = itemView.findViewById<ConstraintLayout>(R.id.lyt_payment_row)
            lytSupplierThumbNail =
                itemView.findViewById<RelativeLayout>(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById<TextView>(R.id.txt_supplier_thumbnail)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtPendingInvoices,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOverDue,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtAmount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH: Int = CommonMethods.dpToPx(30)
            val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT: Int = CommonMethods.dpToPx(30)
        }
    }

    interface PayInvoiceSelectedListener {
        fun onPayInvoicesItemSelected(invoicesGroupBySupplier: EInvoicesGroupBySupplier.EInvoiceWithPagination.InvoicesGroupBySupplier?)
    }
}