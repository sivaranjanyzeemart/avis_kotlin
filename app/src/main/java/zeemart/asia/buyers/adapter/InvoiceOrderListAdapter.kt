package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.invoices.InvoiceCameraPreview
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimport.ListWithStickyHeaderUI

/**
 * Created by RajPrudhviMarella on 22/Dec/2021.
 */
 class InvoiceOrderListAdapter(
    private val context: Context,
    private var viewOrderSectionOrRowData: ArrayList<ListWithStickyHeaderUI>,
    var mListener: InvoiceOrderClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HeaderItemDecoration.StickyHeaderInterface {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_notification_header, parent, false)
            return ViewHolderHeader(v)
        } else if (viewType == ITEM_TYPE_LOADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lyt_loader_for_more_items, parent, false)
            return ViewHolderLoader(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_orders, parent, false)
            return ViewHolderItem(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val item = viewOrderSectionOrRowData[position]
        if (item.isHeader) {
            val header = holder as ViewHolderHeader
            val headerDayData = "(" + item.headerData?.day?.trim { it <= ' ' } + ")"
            header.txtDay.text = headerDayData.trim { it <= ' ' }
            header.txtDate.text = item.headerData?.date?.trim { it <= ' ' }
        } else if (item.isLoader) {
            val viewHolderLoader = holder as ViewHolderLoader
            CustomLoadingViewBlue.setAnimationForThreeDots(
                context,
                viewHolderLoader.imgDot1,
                viewHolderLoader.imgDot2,
                viewHolderLoader.imgDot3
            )
        } else {
            val viewHolderItem = holder as ViewHolderItem
            viewHolderItem.txtSupplierName?.text = item.order?.supplier?.supplierName
            viewHolderItem.lytListRow?.setOnClickListener(View.OnClickListener {
                mListener.onInvoiceOrderClicked(
                    item.order
                )
            })
            if (StringHelper.isStringNullOrEmpty(item.order?.supplier?.logoURL)) {
                viewHolderItem.imgSupplier?.visibility = View.INVISIBLE
                viewHolderItem.lytSupplierThumbNail?.visibility = View.VISIBLE
                item.order?.supplier?.supplierName?.let {
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        it,
                        context
                    )
                }?.let {
                    viewHolderItem.lytSupplierThumbNail?.setBackgroundColor(
                        it
                    )
                }
                viewHolderItem.txtSupplierThumbNail?.text =
                    CommonMethods.SupplierThumbNailShortCutText(item.order?.supplier?.supplierName!!)
                viewHolderItem.txtSupplierThumbNail?.setTextColor(
                    CommonMethods.SupplierThumbNailTextColor(
                        item.order?.supplier?.supplierName!!,
                        context
                    )
                )
            } else {
                viewHolderItem.imgSupplier?.visibility = View.VISIBLE
                viewHolderItem.lytSupplierThumbNail?.visibility = View.GONE
                Picasso.get().load(item.order?.supplier?.logoURL)
                    .placeholder(R.drawable.placeholder_all).into(
                    holder.imgSupplier
                )
            }
            var count = 0
            if (item.order?.linkedInvoices != null) //                viewHolderItem.txt_skipped_upload_not_required.setVisibility(View.GONE);
                for (i in item.order!!.linkedInvoices?.indices!!) {
                    if (item.order!!.linkedInvoices!![i].isStatus(Invoice.Status.REJECTED)) {
                        count = count + 1
                    }
                }
            if (item.order?.isLinkedToInvoice == true) {
                viewHolderItem.imgTruck?.visibility = View.GONE
                viewHolderItem.txtRejectedUploads?.text =
                    context.resources.getString(R.string.txt_linked_to_invoice)
                viewHolderItem.txtDraftIndicator?.visibility = View.GONE
                viewHolderItem.lytUpload?.visibility = View.GONE
            } else {
                if (count != 0) {
                    viewHolderItem.imgTruck?.visibility = View.VISIBLE
                    if (count == 1) {
                        viewHolderItem.txtRejectedUploads?.text =
                            String.format(context.getString(R.string.txt_no_upload_rejected), count)
                    } else {
                        viewHolderItem.txtRejectedUploads?.text = String.format(
                            context.getString(R.string.txt_no_uploads_rejected),
                            count
                        )
                    }
                    viewHolderItem.txtDraftIndicator?.visibility = View.VISIBLE
                    viewHolderItem.lytUpload?.visibility = View.GONE
                    viewHolderItem.txtRejectedUploads?.setTextColor(context.resources.getColor(R.color.pinky_red))
                } else if (item.order?.linkedInvoices != null && item.order?.linkedInvoices!!.size > 0) {
                    val linkedInvoices: MutableList<Orders.LinkedInvoice> = ArrayList()
                    for (i in item.order?.linkedInvoices!!.indices) {
                        Log.d(
                            "FATAL",
                            "onBindViewHolder: " + item.order!!.linkedInvoices!![i].toString()
                        )
                        if (item.order!!.linkedInvoices!![i].isStatus(Invoice.Status.PENDING) || item.order!!.linkedInvoices!![i].isStatus(
                                Invoice.Status.PROCESSING
                            )
                        ) {
                            linkedInvoices.add(item.order!!.linkedInvoices!![i])
                        }
                    }
                    viewHolderItem.imgTruck?.visibility = View.GONE
                    if (linkedInvoices.size == 1) {
                        viewHolderItem.txtRejectedUploads?.text = String.format(
                            context.getString(R.string.txt_no_upload),
                            linkedInvoices.size
                        )
                    } else {
                        viewHolderItem.txtRejectedUploads?.text = String.format(
                            context.getString(R.string.txt_no_uploads),
                            linkedInvoices.size
                        )
                    }
                    viewHolderItem.txtDraftIndicator?.visibility = View.GONE
                    viewHolderItem.txtRejectedUploads?.setTextColor(context.resources.getColor(R.color.color_azul_two))
                    viewHolderItem.lytUpload?.visibility = View.GONE
                } else {
                    viewHolderItem.lytUpload?.visibility = View.VISIBLE
                    viewHolderItem.imgTruck?.visibility = View.GONE
                    viewHolderItem.txtDraftIndicator?.visibility = View.GONE
                    viewHolderItem.txtRejectedUploads?.visibility = View.GONE
                }
            }
            val orderTotal = item.order?.amount?.total?.displayValue
            viewHolderItem.txtDeliveryDate?.text = orderTotal
            val orderId = "#" + item.order?.orderId
            viewHolderItem.txtOutletName?.text = orderId
            viewHolderItem.txtOutletName?.setTextColor(context.resources.getColor(R.color.grey_medium))
            if (item.order?.isAddOn == true) {
                viewHolderItem.add_icon?.visibility = View.VISIBLE
            } else {
                viewHolderItem.add_icon?.visibility = View.GONE
            }
            if (item.order?.isUploadRequired == true) {
                /*viewHolderItem.imgTruck.setVisibility(View.GONE);
                    viewHolderItem.txtRejectedUploads.setVisibility(View.GONE);*/
                // viewHolderItem.lytUpload.setVisibility(View.VISIBLE);
                /*   viewHolderItem.tick_green.setVisibility(View.GONE);
                viewHolderItem.txtOutletName.setVisibility(View.VISIBLE);
              //  viewHolderItem.add_icon.setVisibility(View.VISIBLE);
                viewHolderItem.txt_skipped_upload_not_required.setVisibility(View.GONE);*/
            } else {
                if (item.order?.linkedInvoices != null && item.order?.linkedInvoices!!.size > 0) {
                    viewHolderItem.tick_green?.visibility = View.GONE
                    viewHolderItem.txtOutletName?.visibility = View.VISIBLE
                    //                      viewHolderItem.add_icon.setVisibility(View.VISIBLE);
                    viewHolderItem.txt_skipped_upload_not_required?.visibility = View.GONE
                    //                    Log.d("Hi","hello-if");
                } else {
                    // if (item.getOrder().getLinkedInvoices() !=null) {
                    /*  viewHolderItem.imgTruck.setVisibility(View.GONE);
                    viewHolderItem.txtRejectedUploads.setVisibility(View.GONE);*/
                    viewHolderItem.txtDeliveryDate?.visibility = View.GONE
                    viewHolderItem.txtOutletName?.visibility = View.GONE
                    viewHolderItem.tick_green?.visibility = View.VISIBLE
                    viewHolderItem.lytUpload?.visibility = View.GONE
                    viewHolderItem.add_icon?.visibility = View.GONE
                    viewHolderItem.txt_skipped_upload_not_required?.visibility = View.VISIBLE
                    //                    Log.d("Hi","hello-else");
                }
            }
            viewHolderItem.lytUpload?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    //set the flash boolean true for edge detection
                    SharedPref.writeBool(SharedPref.FLASH_INVOICE_UPLOAD, true)
                    SharedPref.writeBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true)
                    AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_UPLOAD_BYORDER_UPLOAD)
                    val newIntent = Intent(context, InvoiceCameraPreview::class.java)
                    newIntent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
                    )
                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, item.order?.orderId)
                    context.startActivity(newIntent)
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return viewOrderSectionOrRowData.size
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item = viewOrderSectionOrRowData[position]
        if (item.isHeader) {
            return ITEM_TYPE_HEADER
        } else return if (item.isLoader) {
            ITEM_TYPE_LOADER
        } else {
            ITEM_TYPE_ROW
        }
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var itemPosition = itemPosition
        var headerPosition = 0
        do {
            if (isHeader(itemPosition)) {
                headerPosition = itemPosition
                break
            }
            itemPosition -= 1
        } while (itemPosition >= 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return R.layout.lst_notification_header
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        val txtdate = header?.findViewById<TextView>(R.id.txt_list_header_date)
        txtdate?.text =
            viewOrderSectionOrRowData.get(headerPosition).headerData?.date?.trim { it <= ' ' }
        setTypefaceView(txtdate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        val txtDayString =
            "(" + viewOrderSectionOrRowData[headerPosition].headerData?.day?.trim { it <= ' ' } + ")"
        val txtDay = header?.findViewById<TextView>(R.id.txt_list_header_day)
        setTypefaceView(txtDay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        txtDay?.text = txtDayString
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return if (itemPosition < viewOrderSectionOrRowData.size) {
            viewOrderSectionOrRowData.get(itemPosition).isHeader
        } else {
            false
        }
    }

    override fun onHeaderClicked(headerView: View?, position: Int) {}
    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView
        var txtDay: TextView

        init {
            txtDate = itemView.findViewById(R.id.txt_list_header_date)
            txtDay = itemView.findViewById(R.id.txt_list_header_day)
            setTypefaceView(txtDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtDay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    class ViewHolderItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView?
        var txtSupplierName: TextView?
        var txtDeliveryDate: TextView?
        var txtCreatedDate: TextView?
        var txtOrderStatus: TextView?
        var txtOrderTotal: TextView?
        var txtOrderId: TextView?
        var txtOutletName: TextView?
        var imgTruck: ImageView?
        var txtDraftIndicator: TextView?
        var lytListRow: ConstraintLayout?
        var btnDeleteDraft: Button?
        var btnRepeatOrder: Button?
        var imgSwipeRightIndicator: ImageView?
        var imgSwipeLeftIndicator: ImageView?
        var add_icon: ImageView?
        var tick_green: ImageView?
        var txt_skipped_upload_not_required: TextView?
        var lytSupplierThumbNail: RelativeLayout?
        var txtSupplierThumbNail: TextView?
        var lytUpload: RelativeLayout?
        var txtUpload: TextView?
        var txtRejectedUploads: TextView?

        init {
            imgSupplier = itemView.findViewById(R.id.dashboard_img_supplier)
            add_icon = itemView.findViewById(R.id.add_plus)
            imgTruck = itemView.findViewById(R.id.dashboard_img_truck)
            txtSupplierName = itemView.findViewById(R.id.dashboard_txt_supplier_name)
            txtDeliveryDate = itemView.findViewById(R.id.dashboard_txt_delivery_date)
            txtCreatedDate = itemView.findViewById(R.id.dashboard_txt_order_created_date)
            txtOrderStatus = itemView.findViewById(R.id.dashboard_txt_order_status)
            txtOrderTotal = itemView.findViewById(R.id.dashboard_txt_order_total)
            txtOrderId = itemView.findViewById(R.id.dashboard_txt_order_id)
            txtOutletName = itemView.findViewById(R.id.dashboard_txt_outlet_name)
            txtDraftIndicator = itemView.findViewById(R.id.dashboard_text_draft_indicator)
            lytListRow = itemView.findViewById(R.id.lyt_order_list_row)
            btnDeleteDraft = itemView.findViewById(R.id.dashboard_btn_delete_draft)
            btnRepeatOrder = itemView.findViewById(R.id.dashboard_btn_repeat_order)
            imgSwipeLeftIndicator = itemView.findViewById(R.id.img_swipe_left_indicator)
            imgSwipeRightIndicator = itemView.findViewById(R.id.img_swipe_right_indicator)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            txtUpload = itemView.findViewById(R.id.txt_upload)
            txtRejectedUploads = itemView.findViewById(R.id.dashboard_txt_uploads)
            lytUpload = itemView.findViewById(R.id.btn_new_invoice)
            tick_green = itemView.findViewById(R.id.tick_green)
            txt_skipped_upload_not_required =
                itemView.findViewById(R.id.txt_skipped_upload_not_required)
            setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtOrderId, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtCreatedDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtDeliveryDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
            setTypefaceView(txtOrderTotal, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtRejectedUploads,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            setTypefaceView(txtUpload, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(
                txt_skipped_upload_not_required,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val SWIPE_BUTTON_WIDTH = CommonMethods.dpToPx(70)
            val SWIPE_BUTTON_TEXT_SIZE = CommonMethods.dpToPx(14)
            val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE = CommonMethods.dpToPx(11)
        }
    }

    inner class ViewHolderLoader(v: View) : RecyclerView.ViewHolder(v) {
        var imgDot1: ImageView
        var imgDot2: ImageView
        var imgDot3: ImageView

        init {
            imgDot1 = v.findViewById(R.id.img_dot_1)
            imgDot2 = v.findViewById(R.id.img_dot_2)
            imgDot3 = v.findViewById(R.id.img_dot_3)
        }
    }

    fun updateDataList(list: ArrayList<ListWithStickyHeaderUI>?) {
        if (list != null && list.size > 0) {
            viewOrderSectionOrRowData = ArrayList(list)
            notifyDataSetChanged()
        }
    }

    interface InvoiceOrderClicked {
        fun onInvoiceOrderClicked(mOrder: Orders?)
    }

    companion object {
        private val ITEM_TYPE_HEADER = 0
        private val ITEM_TYPE_ROW = 1
        private val ITEM_TYPE_LOADER = 2
    }
}