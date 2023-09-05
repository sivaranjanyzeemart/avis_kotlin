package zeemart.asia.buyers.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter.InvoiceImageListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.EditInvoiceListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.network.InvoiceHelper.createDeleteJsonForInvoice
import zeemart.asia.buyers.network.InvoiceHelper.deleteUnprocessedInvoice
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by RajPrudhviMarella on 11/Jan/2021.
 */
class InvoicesLinkedToOrderListingAdapter(
    private val context: Context,
    private var invoiceList: MutableList<Invoice?>,
    private val listener: EditInvoiceListener,
) : RecyclerView.Adapter<InvoicesLinkedToOrderListingAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_invoice_uploads, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = invoiceList[position]
        if (invoiceList[position]!!.daysElapsed != null) {
            if (item!!.daysElapsed == 0) {
                holder.txtTimeUploaded.text = context.getString(R.string.txt_today)
            } else if (item.daysElapsed == 1) {
                holder.txtTimeUploaded.text = context.getString(R.string.txt_yesterday)
            } else {
                holder.txtTimeUploaded.text = item.daysElapsed.toString() + " " + context.getString(
                    R.string.txt_working_days_ago
                )
            }
        }
        holder.chkSelectInvoice.visibility = View.GONE
        if (item!!.isStatus(Invoice.Status.PROCESSING)) {
            holder.txtInvoiceStatus.text = context.getString(R.string.txt_processing_lower_case)
            holder.txtUploadFailedIndicator.visibility = View.GONE
            holder.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.green))
        } else if (item.isStatus(Invoice.Status.REJECTED)) {
            holder.txtInvoiceStatus.text = context.getString(R.string.txt_invoice_rejected)
            holder.txtUploadFailedIndicator.visibility = View.VISIBLE
            holder.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.pinky_red))
        } else {
            holder.txtInvoiceStatus.text = ""
            holder.txtUploadFailedIndicator.visibility = View.GONE
        }
        if (item.isStatus(Invoice.Status.PENDING)) {
            holder.imgSwipeIndicator.visibility = View.VISIBLE
        } else {
            holder.imgSwipeIndicator.visibility = View.INVISIBLE
        }
        holder.imgInvoice.setImageResource(R.drawable.placeholder_all)
        if (item.images != null && item.images!!.size > 0) {
            var count = 0
            for (imagesModel: ImagesModel in item.images!!) {
                if (imagesModel.imageFileNames != null
                    && imagesModel.imageFileNames.size > 0) count += imagesModel.imageFileNames.size
            }
            if (count > 1) {
                holder.txtInvoiceMergedAtTime.visibility = View.GONE
                //calculate the merged time
                val mergedTime = DateHelper.getDateInDateMonthHourMinuteFormat(
                    item.timeCreated!!
                )
                holder.txtInvoiceMergedAtTime.text =
                    "(" + context.getString(R.string.txt_merged_on) + " " + mergedTime + ")"
            } else {
                holder.txtInvoiceMergedAtTime.visibility = View.GONE
            }
            if (count > 0) {
                for (i in item.images!!.indices) {
                    try {
                        if (!item.images!![i].imageURL?.isEmpty()!! && (item.images!![i].imageFileNames != null)
                            && (item.images!![i].imageFileNames.size > 0)) {
                            if (count == 1) {
                                holder.txtInvoiceMergedAtTime.visibility = View.GONE
                            }
                            val url = item.images!![i].imageURL + item.images!![i].imageFileNames[0]
                            if (!StringHelper.isStringNullOrEmpty(url)) Picasso.get().load(url)
                                .placeholder(
                                    R.drawable.placeholder_all
                                ).resize(
                                ViewHolder.PLACE_HOLDER_INVOICE_IMAGE_WIDTH,
                                ViewHolder.PLACE_HOLDER_INVOICE_IMAGE_HEIGHT
                            ).into(holder.imgInvoice)
                            break
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        holder.txtInvoiceName.text =
            context.resources.getString(R.string.txt_uploaded) + " " + DateHelper.getDateInDateMonthFormat(
                item.timeCreated
            )
        holder.itemView.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_UPLOADS_INVOICES_INVOICE)
            createImageFullscreenDialog(item)
        })
    }

    override fun getItemCount(): Int {
        return invoiceList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgInvoice: ImageView
        var txtInvoiceName: TextView
        var txtTimeUploaded: TextView
        var imgSwipeIndicator: ImageView
        var txtInvoiceStatus: TextView
        var chkSelectInvoice: CheckBox
        var txtInvoiceMergedAtTime: TextView
        var txtUploadFailedIndicator: TextView

        init {
            imgInvoice = itemView.findViewById(R.id.upload_tab_invoice_image)
            txtInvoiceName = itemView.findViewById(R.id.txt_image_name)
            txtTimeUploaded = itemView.findViewById(R.id.txt_time_uploaded)
            imgSwipeIndicator = itemView.findViewById(R.id.img_swipe_indicator)
            txtInvoiceStatus = itemView.findViewById(R.id.txt_invoice_status)
            txtUploadFailedIndicator = itemView.findViewById(R.id.upload_failed_indicator)
            chkSelectInvoice = itemView.findViewById(R.id.checkBox_select_uploaded_invoice)
            txtInvoiceMergedAtTime = itemView.findViewById(R.id.txt_invoice_merged_at)
            ZeemartBuyerApp.setTypefaceView(
                txtInvoiceName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtTimeUploaded,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
            ZeemartBuyerApp.setTypefaceView(
                txtInvoiceStatus,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtInvoiceMergedAtTime,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val PLACE_HOLDER_INVOICE_IMAGE_WIDTH = CommonMethods.dpToPx(36)
            val PLACE_HOLDER_INVOICE_IMAGE_HEIGHT = CommonMethods.dpToPx(36)
        }
    }

    private fun createImageFullscreenDialog(invoiceMgr: Invoice?) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_fullscreen)
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val viewPager = dialog.findViewById<ViewPager>(R.id.imageView)
        viewPager.offscreenPageLimit = 0
        viewPager.adapter =
            InvoiceImagesSliderAdapter(context, (invoiceMgr)!!, object : InvoiceImageListener {
                override fun invoiceUpdated(inv: Invoice?) {}
                override fun invoiceDeleted(inv: Invoice?, isDeleted: Boolean) {
                    for (i in invoiceList.indices) {
                        if (invoiceList.get(i) != null) {
                            if ((invoiceList.get(i)!!.invoiceId == inv!!.invoiceId)) {
                                dialog.dismiss()
                                removeAt(i)
                                return
                            }
                        }
                    }
                }

                override fun onRejectedInvoiceBackPressed() {
                    //no action required from list of uploads screen
                }
            })
        dialog.show()
    }

    fun notifyDataChanged(
        invoiceUploadTabList: MutableList<Invoice?>,
        isRemoved: Boolean,
        isNewUpload: Boolean,
        isItemStatuschanged: Boolean,
        position: Int,
    ) {
        invoiceList = invoiceUploadTabList
        if (isRemoved) {
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, invoiceList.size)
            notifyDataSetChanged()
        } else if (isNewUpload) {
            notifyItemInserted(position)
            notifyItemRangeChanged(position, invoiceList.size)
        } else if (isItemStatuschanged) {
            notifyItemChanged(position)
        } else {
            notifyDataSetChanged()
        }
    }

    fun removeAt(position: Int) {
        if (invoiceList[position] != null) {
            val json = createDeleteJsonForInvoice(invoiceList[position]!!.invoiceId)
            val outlet: MutableList<Outlet?> = ArrayList()
            outlet.add(SharedPref.defaultOutlet)
            deleteUnprocessedInvoice(context, outlet as List<Outlet>?, object : GetRequestStatusResponseListener {
                override fun onSuccessResponse(status: String?) {
                    listener.invoicesCountByStatus()
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    getToastRed(context.getString(R.string.txt_delete_draft_fail))
                }
            }, json)
            invoiceList.removeAt(position)
            notifyDataChanged(invoiceList, true, false, false, position)
        }
    }
}