package zeemart.asia.buyers.invoices

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter.InvoiceImageListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.AnalyticsHelper.logAction
import zeemart.asia.buyers.helper.CommonMethods.dpToPx
import zeemart.asia.buyers.helper.DateHelper.getDateInDateMonthHourMinuteFormat
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.EditInvoiceListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import zeemart.asia.buyers.network.InvoiceHelper.createDeleteJsonForInvoice
import zeemart.asia.buyers.network.InvoiceHelper.deleteUnprocessedInvoice
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by RajPrudhviMarella on 20/Dec/2021.
 */
class InvoicesRejectedListAdapter(
    private var lstInQueueAndUploadedList: MutableList<InvoiceUploadsListDataMgr>,
    private val isDisplayOnly4: Boolean,
    private val context: Context,
    private val listener: EditInvoiceListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val isDeleteOrMerge = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADING) {
            val v = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_invoice_upload_tab_list_header,
                parent,
                false
            )
            ViewHolderHeading(v)
        } else if (viewType == VIEW_TYPE_UPLOADED) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_uploads, parent, false)
            ViewHolderUploaded(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_invoice_upload_tab_list_header,
                parent,
                false
            )
            ViewHolderHeading(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (lstInQueueAndUploadedList[position].header != null) {
            val headerHolder = holder as ViewHolderHeading
            if (isDisplayOnly4) {
                if (lstInQueueAndUploadedList.size > 4) {
                    headerHolder.btnMergeOrDelete.visibility = View.VISIBLE
                    headerHolder.btnMergeOrDelete.text = String.format(
                        context.resources.getString(R.string.txt_count_view_all),
                        lstInQueueAndUploadedList.size - 1
                    )
                } else {
                    headerHolder.btnMergeOrDelete.visibility = View.GONE
                }
                headerHolder.txtHeading.text =
                    context.getString(lstInQueueAndUploadedList[position].header!!.headerResId)
            } else {
                headerHolder.txtHeading.text = String.format(
                    context.getString(R.string.txt_no_uploads),
                    lstInQueueAndUploadedList.size - 1
                )
                headerHolder.btnMergeOrDelete.visibility = View.GONE
            }
            headerHolder.btnMergeOrDelete.setOnClickListener {
                logAction(context, AnalyticsHelper.TAP_VIEW_ALL_REJECTED_INVOICES)
                val intent = Intent(context, RejectedInvoicesListActivity::class.java)
                context.startActivity(intent)
            }
        } else if (lstInQueueAndUploadedList[position].uploadedInvoice != null) {
            val item = lstInQueueAndUploadedList[position].uploadedInvoice
            val viewHolderItem = holder as ViewHolderUploaded
            //            if (item.getOrderData() != null && item.getOrderData().get(0).getTimeDelivered() != null && item.getOrderData().get(0).getTimeDelivered() != 0L) {
//                viewHolderItem.txtOrderDeliveryDate.setVisibility(View.VISIBLE);
//                viewHolderItem.imgDeliveryDate.setVisibility(View.VISIBLE);
//                viewHolderItem.txtOrderDeliveryDate.setText(DateHelper.getDateInDateMonthFormat(item.getOrderData().get(0).getTimeDelivered()));
//            } else {
            viewHolderItem.txtOrderDeliveryDate.visibility = View.GONE
            viewHolderItem.imgDeliveryDate.visibility = View.GONE
            //            }
//            if (item.getDaysElapsed() != null) {
//                if (item.getDaysElapsed() == 0) {
//                    viewHolderItem.txtTimeUploaded.setText(DateHelper.getDateInDateMonthFormat(item.getTimeCreated()) + " (" + context.getString(R.string.txt_today) + ") ");
//                } else if (item.getDaysElapsed() == 1) {
//                    viewHolderItem.txtTimeUploaded.setText(DateHelper.getDateInDateMonthFormat(item.getTimeCreated()) + " (" + context.getString(R.string.txt_yesterday) + ") ");
//                } else {
//                    viewHolderItem.txtTimeUploaded.setText(DateHelper.getDateInDateMonthFormat(item.getTimeCreated()) + " (" + String.valueOf(item.getDaysElapsed()) + " " + context.getString(R.string.txt_working_days_ago) + ") ");
//                }
//            } else {
            viewHolderItem.txtTimeUploaded.text = item!!.rejectReason!!.reasonText
            //            }
            if (isDeleteOrMerge) {
                ////PNF-340 created by user check
                if (item.isStatus(Invoice.Status.PENDING) || item.isStatus(Invoice.Status.REJECTED)) {
                    viewHolderItem.chkSelectInvoice.visibility = View.VISIBLE
                } else {
                    viewHolderItem.chkSelectInvoice.visibility = View.GONE
                }
            } else {
                viewHolderItem.chkSelectInvoice.visibility = View.GONE
            }
            viewHolderItem.txtInvoiceStatus.text = context.getString(R.string.txt_invoice_rejected)
            viewHolderItem.txtInvoiceStatus.visibility = View.GONE
            viewHolderItem.txtUploadFailedIndicator.visibility = View.VISIBLE
            viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.pinky_red))
            var count = 0
            for (imagesModel in item.images!!) {
                if (imagesModel.imageFileNames != null && imagesModel.imageFileNames.size > 0) count += imagesModel.imageFileNames.size
            }
            if (count > 1) {
                viewHolderItem.txtInvoiceMergedAtTime.visibility = View.GONE
                //calculate the merged time
                val mergedTime = getDateInDateMonthHourMinuteFormat(item.timeCreated!!)
                viewHolderItem.txtInvoiceMergedAtTime.text =
                    "(" + context.getString(R.string.txt_merged_on) + " " + mergedTime + ")"
            } else {
                viewHolderItem.txtInvoiceMergedAtTime.visibility = View.GONE
            }
            viewHolderItem.imgInvoice.setImageResource(R.drawable.placeholder_all)
            if (item.supplier != null && !StringHelper.isStringNullOrEmpty(item.supplier!!.supplierName)) {
                viewHolderItem.txtInvoiceName.text = item.supplier!!.supplierName
            } else {
                viewHolderItem.txtInvoiceName.text =
                    context.resources.getString(R.string.txt_unknown_supplier)
            }
            if (count > 0) {
                for (i in item.images!!.indices) {
                    try {
                        if (!item.images!![i].imageURL!!.isEmpty() && item.images!![i].imageFileNames != null && item.images!![i].imageFileNames.size > 0) {
                            if (count == 1) {
                                viewHolderItem.txtInvoiceMergedAtTime.visibility = View.GONE
                            }
                            val url = item.images!![i].imageURL + item.images!![i].imageFileNames[0]
                            if (!StringHelper.isStringNullOrEmpty(url)) Picasso.get().load(url)
                                .placeholder(
                                    R.drawable.placeholder_all
                                ).resize(
                                ViewHolderUploaded.PLACE_HOLDER_INVOICE_IMAGE_WIDTH,
                                ViewHolderUploaded.PLACE_HOLDER_INVOICE_IMAGE_HEIGHT
                            ).into(viewHolderItem.imgInvoice)
                            break
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            if (item.isInvoiceSelected && isDeleteOrMerge) {
                viewHolderItem.chkSelectInvoice.isChecked = true
            } else {
                viewHolderItem.chkSelectInvoice.isChecked = false
            }
            viewHolderItem.itemView.setOnClickListener {
                logAction(context, AnalyticsHelper.TAP_ITEM_UPLOADS_INVOICES_INVOICE)
                createImageFullscreenDialog(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isDisplayOnly4 && lstInQueueAndUploadedList.size > 4) {
            4
        } else {
            lstInQueueAndUploadedList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (lstInQueueAndUploadedList[position].header != null) {
            VIEW_TYPE_HEADING
        } else if (lstInQueueAndUploadedList[position].uploadedInvoice != null) {
            VIEW_TYPE_UPLOADED
        } else {
            0
        }
    }

    class ViewHolderUploaded(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgInvoice: ImageView
        var txtInvoiceName: TextView
        var txtTimeUploaded: TextView
        var imgSwipeIndicator: ImageView
        var txtInvoiceStatus: TextView
        var chkSelectInvoice: CheckBox
        var txtInvoiceMergedAtTime: TextView
        var txtUploadFailedIndicator: TextView
        var imgDeliveryDate: ImageView
        var txtOrderDeliveryDate: TextView

        init {
            txtOrderDeliveryDate = itemView.findViewById(R.id.txt_delivery_date)
            imgDeliveryDate = itemView.findViewById(R.id.img_delivery_date)
            imgInvoice = itemView.findViewById(R.id.upload_tab_invoice_image)
            txtInvoiceName = itemView.findViewById(R.id.txt_image_name)
            txtTimeUploaded = itemView.findViewById(R.id.txt_time_uploaded)
            imgSwipeIndicator = itemView.findViewById(R.id.img_swipe_indicator)
            txtInvoiceStatus = itemView.findViewById(R.id.txt_invoice_status)
            txtUploadFailedIndicator = itemView.findViewById(R.id.upload_failed_indicator)
            chkSelectInvoice = itemView.findViewById(R.id.checkBox_select_uploaded_invoice)
            txtInvoiceMergedAtTime = itemView.findViewById(R.id.txt_invoice_merged_at)
            setTypefaceView(
                txtOrderDeliveryDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            setTypefaceView(txtInvoiceName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtTimeUploaded, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtInvoiceStatus, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtInvoiceMergedAtTime,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val PLACE_HOLDER_INVOICE_IMAGE_WIDTH = dpToPx(36)
            val PLACE_HOLDER_INVOICE_IMAGE_HEIGHT = dpToPx(36)
        }
    }

    inner class ViewHolderHeading(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeading: TextView
        var btnMergeOrDelete: TextView
        val SWIPE_BUTTON_WIDTH = dpToPx(70)
        val SWIPE_BUTTON_TEXT_SIZE = dpToPx(14)
        val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE = dpToPx(11)

        init {
            txtHeading = itemView.findViewById(R.id.txt_heading_data)
            btnMergeOrDelete = itemView.findViewById(R.id.txt_merge_delete)
            setTypefaceView(txtHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(btnMergeOrDelete, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    private fun createImageFullscreenDialog(invoiceMgr: InvoiceMgr?) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_fullscreen)
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val viewPager = dialog.findViewById<ViewPager>(R.id.imageView)
        viewPager.offscreenPageLimit = 0
        viewPager.adapter =
            InvoiceImagesSliderAdapter(context, invoiceMgr!!, object : InvoiceImageListener {
                override fun invoiceUpdated(inv: Invoice?) {
                    for (i in lstInQueueAndUploadedList.indices) {
                        if (lstInQueueAndUploadedList[i].uploadedInvoice != null) {
                            if (lstInQueueAndUploadedList[i].uploadedInvoice!!.invoiceId == inv!!.invoiceId) {
                                val updatedInvoice = InvoiceUploadsListDataMgr()
                                val invoiceMgr = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(inv),
                                    InvoiceMgr::class.java
                                )
                                updatedInvoice.uploadedInvoice = invoiceMgr
                                lstInQueueAndUploadedList[i] = updatedInvoice
                                notifyDataChanged(
                                    lstInQueueAndUploadedList, false, false, true, i
                                )
                                return
                            }
                        }
                    }
                }

                override fun invoiceDeleted(inv: Invoice?, isDeleted: Boolean) {
                    for (i in lstInQueueAndUploadedList.indices) {
                        if (lstInQueueAndUploadedList[i].uploadedInvoice != null) {
                            if (lstInQueueAndUploadedList[i].uploadedInvoice!!.invoiceId == inv!!.invoiceId) {
                                dialog.dismiss()
                                removeAt(i, isDeleted)
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
        invoiceUploadTabList: MutableList<InvoiceUploadsListDataMgr>,
        isRemoved: Boolean,
        isNewUpload: Boolean,
        isItemStatuschanged: Boolean,
        position: Int
    ) {
        lstInQueueAndUploadedList = invoiceUploadTabList
        if (isRemoved) {
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, lstInQueueAndUploadedList.size)
            notifyDataSetChanged()
        } else if (isNewUpload) {
            notifyItemInserted(position)
            notifyItemRangeChanged(position, lstInQueueAndUploadedList.size)
        } else if (isItemStatuschanged) {
            notifyItemChanged(position)
        } else {
            notifyDataSetChanged()
        }
    }

    fun removeAt(position: Int, isDeleted: Boolean) {
        if (lstInQueueAndUploadedList[position].uploadedInvoice != null) {
            SharedPref.write(
                SharedPref.INVOICE_ID_DELETE,
                lstInQueueAndUploadedList[position].uploadedInvoice!!.invoiceId
            )
            if (isDeleted) {
                val json = createDeleteJsonForInvoice(
                    lstInQueueAndUploadedList[position].uploadedInvoice!!.invoiceId
                )
                val outlet: MutableList<Outlet?> = ArrayList()
                outlet.add(SharedPref.defaultOutlet)
                deleteUnprocessedInvoice(
                    context,
                    outlet as List<Outlet>?,
                    object : GetRequestStatusResponseListener {
                        override fun onSuccessResponse(status: String?) {
                            lstInQueueAndUploadedList.removeAt(position)
                            if (lstInQueueAndUploadedList.size == 1) {
                                lstInQueueAndUploadedList.removeAt(0)
                            }
                            notifyDataChanged(
                                lstInQueueAndUploadedList, true, false, false, position
                            )
                            listener.invoicesCountByStatus()
                        }

                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                            getToastRed(context.getString(R.string.txt_delete_draft_fail))
                        }
                    },
                    json
                )
            } else {
                lstInQueueAndUploadedList.removeAt(position)
                if (lstInQueueAndUploadedList.size == 1) {
                    lstInQueueAndUploadedList.removeAt(0)
                }
                notifyDataChanged(lstInQueueAndUploadedList, true, false, false, position)
                listener.invoicesCountByStatus()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADING = 1
        private const val VIEW_TYPE_UPLOADED = 3
    }
}