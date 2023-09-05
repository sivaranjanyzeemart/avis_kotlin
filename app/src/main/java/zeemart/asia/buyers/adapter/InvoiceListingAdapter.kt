package zeemart.asia.buyers.adapter

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
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
import zeemart.asia.buyers.invoices.InvoiceDetailActivity
import zeemart.asia.buyers.invoices.RejectedInvoicesListActivity
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import zeemart.asia.buyers.models.invoiceimportimport.InvoiceRejectProcessedMgr
import zeemart.asia.buyers.network.InvoiceHelper.createDeleteJsonForInvoice
import zeemart.asia.buyers.network.InvoiceHelper.deleteUnprocessedInvoice
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 12/29/2017.
 */
class InvoiceListingAdapter(
    private val context: Context,
    invoiceList: MutableList<InvoiceRejectProcessedMgr>?,
    lstInQueueAndUploadedList: MutableList<InvoiceUploadsListDataMgr>,
    listener: EditInvoiceListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var invoiceList: MutableList<InvoiceRejectProcessedMgr>
    private val listener: EditInvoiceListener
    private val lstInQueueAndUploadedList: MutableList<InvoiceUploadsListDataMgr>

    init {
        this.invoiceList = ArrayList(invoiceList)
        this.listener = listener
        this.lstInQueueAndUploadedList = lstInQueueAndUploadedList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_order_list_header, parent, false)
            return ViewHolderHeader(v)
        } else if (viewType == VIEW_TYPE_HEADING) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_upload_tab_list_header, parent, false)
            return ViewHolderHeading(v)
        } else if (viewType == VIEW_TYPE_UPLOADED) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_uploads, parent, false)
            return ViewHolderUploaded(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_processed, parent, false)
            return ViewHolderItem(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)  {
        if (invoiceList[position].invoiceUploadsListDataMgr != null) {
            if (invoiceList[position].invoiceUploadsListDataMgr?.header != null) {
                val headerHolder = holder as ViewHolderHeading?
                if (lstInQueueAndUploadedList.size > 4) {
                    headerHolder!!.btnMergeOrDelete.visibility = View.VISIBLE
                    headerHolder.btnMergeOrDelete.text = String.format(
                        context.resources.getString(R.string.txt_count_view_all),
                        lstInQueueAndUploadedList.size - 1
                    )
                } else {
                    headerHolder!!.btnMergeOrDelete.visibility = View.GONE
                }
                headerHolder.txtHeading.text =
                    context.getString(invoiceList.get(position).invoiceUploadsListDataMgr?.header?.headerResId!!)
                headerHolder.btnMergeOrDelete.setOnClickListener(View.OnClickListener {
                    val intent = Intent(context, RejectedInvoicesListActivity::class.java)
                    (context as Activity).startActivityForResult(
                        intent,
                        ZeemartAppConstants.RequestCode.GoodsReceivedNoteDashBoardActivity
                    )
                })
            } else if (invoiceList[position].invoiceUploadsListDataMgr?.uploadedInvoice != null) {
                val item = invoiceList[position].invoiceUploadsListDataMgr?.uploadedInvoice
                val viewHolderItem = holder as ViewHolderUploaded?
                //                if (item.getDaysElapsed() != null) {
//                    if (item.getDaysElapsed() == 0) {
//                        viewHolderItem.txtTimeUploaded.setText(DateHelper.getDateInDateMonthFormat(item.getTimeCreated()) + " (" + context.getString(R.string.txt_today) + ") ");
//                    } else if (item.getDaysElapsed() == 1) {
//                        viewHolderItem.txtTimeUploaded.setText(DateHelper.getDateInDateMonthFormat(item.getTimeCreated()) + " (" + context.getString(R.string.txt_yesterday) + ") ");
//                    } else {
//                        viewHolderItem.txtTimeUploaded.setText(DateHelper.getDateInDateMonthFormat(item.getTimeCreated()) + " (" + String.valueOf(item.getDaysElapsed()) + " " + context.getString(R.string.txt_working_days_ago) + ") ");
//                    }
//                } else {
                viewHolderItem!!.txtTimeUploaded.text =
                    invoiceList.get(position).invoiceUploadsListDataMgr?.uploadedInvoice?.rejectReason?.reasonText
                // }
//                if (item.getOrderData() != null && item.getOrderData().get(0).getTimeDelivered() != null && item.getOrderData().get(0).getTimeDelivered() != 0L) {
//                    viewHolderItem.txtOrderDeliveryDate.setVisibility(View.VISIBLE);
//                    viewHolderItem.imgDeliveryDate.setVisibility(View.VISIBLE);
//                    viewHolderItem.txtOrderDeliveryDate.setText(DateHelper.getDateInDateMonthFormat(item.getOrderData().get(0).getTimeDelivered()));
//                } else {
                viewHolderItem.txtOrderDeliveryDate.visibility = View.GONE
                viewHolderItem.imgDeliveryDate.visibility = View.GONE
                //                }
                viewHolderItem.chkSelectInvoice.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.text =
                    context.getString(R.string.txt_invoice_rejected)
                viewHolderItem.txtInvoiceStatus.visibility = View.GONE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.VISIBLE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.pinky_red))
                var count = 0
                for (imagesModel: ImagesModel in item?.images!!) {
                    if (imagesModel.imageFileNames != null && imagesModel.imageFileNames.size > 0) count += imagesModel.imageFileNames.size
                }
                if (count > 1) {
                    viewHolderItem.txtInvoiceMergedAtTime.visibility = View.GONE
                    //calculate the merged time
                    val mergedTime = DateHelper.getDateInDateMonthHourMinuteFormat(item.timeCreated!!)
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
                            if (!item.images!![i].imageURL?.isEmpty()!! && (item.images!![i].imageFileNames != null)
                                && (item.images!![i].imageFileNames.size > 0)) {
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
                                ).into(
                                    viewHolderItem.imgInvoice
                                )
                                break
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                viewHolderItem.itemView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        AnalyticsHelper.logAction(
                            context,
                            AnalyticsHelper.TAP_ITEM_UPLOADS_INVOICES_INVOICE
                        )
                        createImageFullscreenDialog(item)
                    }
                })
            }
        } else if (invoiceList[position].invoiceMgr != null) {
            val item = invoiceList[position].invoiceMgr
            if (item?.isHeader == true) {
                val header = holder as ViewHolderHeader?
                val headerDayData = "(" + item?.headerDay?.trim { it <= ' ' } + ")"
                header!!.txtDay.text = headerDayData.trim { it <= ' ' }
                header.txtDate.text = item?.headerDate
            } else {
                val viewHolderItem = holder as ViewHolderItem?
                viewHolderItem!!.itemView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        AnalyticsHelper.logAction(
                            context,
                            AnalyticsHelper.TAP_ITEM_INVOICES_INVOICE
                        )
                        val newIntent = Intent(context, InvoiceDetailActivity::class.java)
                        newIntent.putExtra(ZeemartAppConstants.INVOICE_ID, item?.invoiceId)
                        context.startActivity(newIntent)
                    }
                })
                viewHolderItem.txtSupplierName?.text = item?.supplier?.supplierName
                viewHolderItem.txtOrderId?.text = "#" + item?.invoiceNum
                // total cost
                if (item?.isInvoiceType(Invoice.InvoiceType.ECREDITNOTE) == true) {
                    viewHolderItem.imgECreditNote?.visibility = View.VISIBLE
                    viewHolderItem.txtPaymentTerms?.visibility = View.GONE
                    viewHolderItem.txtOrderTotal?.text = item.totalCharge?.negativeDisplayValue
                } else {
                    viewHolderItem.imgECreditNote?.visibility = View.GONE
                    viewHolderItem.txtPaymentTerms?.visibility = View.VISIBLE
                    if (item?.totalCharge != null) {
                        viewHolderItem.txtOrderTotal?.text = item.totalCharge!!.displayValue
                    } else {
                        viewHolderItem.txtOrderTotal?.text = PriceDetails().displayValue
                    }
                }
                if (item?.isPaymentStatus(Invoice.PaymentStatus.PAID) == true || item?.isPaymentStatus(Invoice.PaymentStatus.USED) == true) {
                    viewHolderItem.imgPaymentStatus?.visibility = View.VISIBLE
                } else {
                    viewHolderItem.imgPaymentStatus?.visibility = View.GONE
                }
                if (StringHelper.isStringNullOrEmpty(item?.paymentTerms)) {
                    viewHolderItem.txtPaymentTerms?.visibility = View.GONE
                } else {
                    viewHolderItem.txtPaymentTerms?.visibility = View.VISIBLE
                    viewHolderItem.txtPaymentTerms?.text = item?.paymentTerms // payment mode
                }
                if (StringHelper.isStringNullOrEmpty(item?.supplier?.logoURL)) {
                    viewHolderItem.imgSupplier?.visibility = View.INVISIBLE
                    viewHolderItem.lytSupplierThumbNail?.visibility = View.VISIBLE
                    viewHolderItem.lytSupplierThumbNail?.setBackgroundColor(
                        CommonMethods.SupplierThumbNailBackgroundColor(
                            item?.supplier?.supplierName!!,
                            context
                        )
                    )
                    viewHolderItem.txtSupplierThumbNail?.text =
                        CommonMethods.SupplierThumbNailShortCutText(item?.supplier?.supplierName!!)
                    viewHolderItem.txtSupplierThumbNail?.setTextColor(
                        CommonMethods.SupplierThumbNailTextColor(
                            item?.supplier?.supplierName!!,
                            context
                        )
                    )
                } else {
                    viewHolderItem.imgSupplier?.visibility = View.VISIBLE
                    viewHolderItem.lytSupplierThumbNail?.visibility = View.GONE
                    Picasso.get().load(item?.supplier?.logoURL)
                        .placeholder(R.drawable.placeholder_all).fit().centerInside().into(
                        viewHolderItem.imgSupplier
                    )
                }
                val scale = context.resources.displayMetrics.density
                val dpAsPixels = (20 * scale + 0.5f).toInt()
                viewHolderItem.txtOrderId?.setPadding(0, 0, 0, dpAsPixels)
                viewHolderItem.txtDraftIndicator?.visibility = View.GONE
                if (item?.isStatus(Invoice.Status.VOIDED) == true) {
                    viewHolderItem.itemView.alpha = 0.5f
                } else {
                    viewHolderItem.itemView.alpha = 1f
                }
                if (invoiceList.size - 1 == position) {
                    viewHolderItem.lytExtraSpace?.visibility = View.VISIBLE
                } else {
                    viewHolderItem.lytExtraSpace?.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return invoiceList.size
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        if (invoiceList[position].invoiceUploadsListDataMgr != null) {
            val items = invoiceList[position].invoiceUploadsListDataMgr
            return if (items?.header != null) {
                VIEW_TYPE_HEADING
            } else {
                VIEW_TYPE_UPLOADED
            }
        } else {
            val item = invoiceList[position].invoiceMgr
            return if (item?.isHeader == true) {
                ITEM_TYPE_HEADER
            } else {
                ITEM_TYPE_ROW
            }
        }
    }

    class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView
        var txtDay: TextView

        init {
            txtDate = itemView.findViewById(R.id.txt_list_header_date)
            txtDay = itemView.findViewById(R.id.txt_list_header_day)
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtDay,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    class ViewHolderItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView?
        var txtSupplierName: TextView?
        var txtDeliveryDate: TextView?
        var txtCreatedDate: TextView?
        var txtOrderTotal: TextView?
        var txtPaymentTerms: TextView?
        var txtOrderId: TextView?
        //var imgTruck: ImageView?
        var txtDraftIndicator: TextView?
        var lytListRow: ConstraintLayout?
        var btnDeleteDraft: Button?
        var btnRepeatOrder: Button?
        var imgECreditNote: ImageView?
        var imgPaymentStatus: ImageView?
        var lytSupplierThumbNail: RelativeLayout?
        var txtSupplierThumbNail: TextView?
        var lytExtraSpace: LinearLayout?

        init {
            imgSupplier = itemView.findViewById(R.id.dashboard_img_supplier)
            //imgTruck = itemView.findViewById(R.id.dashboard_img_truck)
            txtSupplierName = itemView.findViewById(R.id.dashboard_txt_supplier_name)!!
            txtDeliveryDate = itemView.findViewById(R.id.dashboard_txt_delivery_date)
            txtCreatedDate = itemView.findViewById(R.id.dashboard_txt_order_created_date)
            txtOrderTotal = itemView.findViewById(R.id.dashboard_txt_order_total)
            txtPaymentTerms = itemView.findViewById(R.id.processed_payment_terms)
            txtOrderId = itemView.findViewById(R.id.processed_invoice_order_id)
            txtDraftIndicator = itemView.findViewById(R.id.dashboard_text_draft_indicator)
            lytListRow = itemView.findViewById(R.id.lyt_dashboard_row)
            btnDeleteDraft = itemView.findViewById(R.id.dashboard_btn_delete_draft)
            btnRepeatOrder = itemView.findViewById(R.id.dashboard_btn_repeat_order)
            imgECreditNote = itemView.findViewById(R.id.img_e_credit_note)
            imgPaymentStatus = itemView.findViewById(R.id.img_payment_status)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            lytExtraSpace = itemView.findViewById(R.id.extra_space)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderId,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderTotal,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtPaymentTerms,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtCreatedDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    fun updateDataList(dataList: List<InvoiceRejectProcessedMgr>?) {
        invoiceList = ArrayList(dataList)
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
            ZeemartBuyerApp.setTypefaceView(
                txtInvoiceName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtTimeUploaded,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtInvoiceStatus,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtInvoiceMergedAtTime,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderDeliveryDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val PLACE_HOLDER_INVOICE_IMAGE_WIDTH = CommonMethods.dpToPx(36)
            val PLACE_HOLDER_INVOICE_IMAGE_HEIGHT = CommonMethods.dpToPx(36)
        }
    }

    inner class ViewHolderHeading(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeading: TextView
        var btnMergeOrDelete: TextView
        val SWIPE_BUTTON_WIDTH = CommonMethods.dpToPx(70)
        val SWIPE_BUTTON_TEXT_SIZE = CommonMethods.dpToPx(14)
        val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE = CommonMethods.dpToPx(11)

        init {
            txtHeading = itemView.findViewById(R.id.txt_heading_data)
            btnMergeOrDelete = itemView.findViewById(R.id.txt_merge_delete)
            ZeemartBuyerApp.setTypefaceView(
                txtHeading,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                btnMergeOrDelete,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    private fun createImageFullscreenDialog(invoiceMgr: InvoiceMgr) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_fullscreen)
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val viewPager = dialog.findViewById<ViewPager>(R.id.imageView)
        viewPager.offscreenPageLimit = 0
        viewPager.adapter =
            InvoiceImagesSliderAdapter(context, invoiceMgr, object : InvoiceImageListener {
                override fun invoiceUpdated(inv: Invoice?) {
                    for (i in invoiceList.indices) {
                        if (invoiceList.get(i).invoiceUploadsListDataMgr != null && invoiceList.get(
                                i
                            ).invoiceUploadsListDataMgr!!.uploadedInvoice != null
                        ) {
                            if ((invoiceList.get(i).invoiceUploadsListDataMgr!!.uploadedInvoice?.invoiceId == inv!!.invoiceId)) {
                                val updatedInvoice: InvoiceUploadsListDataMgr =
                                    InvoiceUploadsListDataMgr()
                                val invoiceMgr: InvoiceMgr =
                                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(inv),
                                        InvoiceMgr::class.java
                                    )
                                updatedInvoice.uploadedInvoice = invoiceMgr
                                invoiceList.get(i).invoiceUploadsListDataMgr = updatedInvoice
                                notifyDataChanged(invoiceList, false, false, true, i)
                                return
                            }
                        }
                    }
                }

                override fun invoiceDeleted(inv: Invoice?, isDeleted: Boolean) {
                    for (i in invoiceList.indices) {
                        if (invoiceList.get(i).invoiceUploadsListDataMgr != null && invoiceList.get(
                                i
                            ).invoiceUploadsListDataMgr!!.uploadedInvoice != null
                        ) {
                            if ((invoiceList.get(i).invoiceUploadsListDataMgr!!.uploadedInvoice?.invoiceId == inv!!.invoiceId)) {
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
        invoiceUploadTabList: MutableList<InvoiceRejectProcessedMgr>,
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

    fun removeAt(position: Int, isDeleted: Boolean) {
        if (invoiceList[position].invoiceUploadsListDataMgr?.uploadedInvoice != null) {
            SharedPref.write(
                SharedPref.INVOICE_ID_DELETE,
                lstInQueueAndUploadedList[position].uploadedInvoice!!.invoiceId
            )
            if (isDeleted) {
                val json = createDeleteJsonForInvoice(
                    invoiceList[position].invoiceUploadsListDataMgr?.uploadedInvoice
                        ?.invoiceId
                )
                val outlet: MutableList<Outlet?> = java.util.ArrayList()
                outlet.add(SharedPref.defaultOutlet)
                deleteUnprocessedInvoice(
                    context,
                    outlet as List<Outlet>?,
                    object : GetRequestStatusResponseListener {
                        override fun onSuccessResponse(status: String?) {
                            invoiceList.removeAt(position)
                            notifyDataChanged(invoiceList, true, false, false, position)
                            listener.onEditButtonClicked()
                        }

                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                            getToastRed(context.getString(R.string.txt_delete_draft_fail))
                        }
                    },
                    json
                )
            } else {
                invoiceList.removeAt(position)
                notifyDataChanged(invoiceList, true, false, false, position)
                listener.onEditButtonClicked()
            }
        }
    }

    companion object {
        private val ITEM_TYPE_HEADER = 0
        private val ITEM_TYPE_ROW = 1
        private val VIEW_TYPE_HEADING = 2
        private val VIEW_TYPE_UPLOADED = 3
    }
}