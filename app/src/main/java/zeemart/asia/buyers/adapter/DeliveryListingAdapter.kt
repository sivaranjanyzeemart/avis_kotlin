package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
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
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.models.orderimportimport.ListWithStickyHeaderUI
import zeemart.asia.buyers.orders.OrderDetailsActivity

/**
 * Created by ParulBhandari on 12/29/2017.
 */
class DeliveryListingAdapter(
    private val context: Context,
    viewOrderSectionOrRowData: ArrayList<ListWithStickyHeaderUI>?,
    isUpcoming: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HeaderItemDecoration.StickyHeaderInterface {
    var viewOrderSectionOrRowData = ArrayList<ListWithStickyHeaderUI>()
    private val isUpcoming: Boolean

    init {
        this.viewOrderSectionOrRowData = ArrayList(viewOrderSectionOrRowData)
        this.isUpcoming = isUpcoming
    }

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
                .inflate(R.layout.layout_dashboard_row, parent, false)
            return ViewHolderItem(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = viewOrderSectionOrRowData[position]
        if (item.isHeader) {
            val header = holder as ViewHolderHeader?
            val headerDayData = "(" + item.headerData?.day?.trim { it <= ' ' } + ")"
            header!!.txtDay.text = headerDayData.trim { it <= ' ' }
            header.txtDate.text = item.headerData?.date?.trim { it <= ' ' }
        } else if (item.isLoader) {
            /*set the Animation in onBindViewHolder , because if we use notifyDataSetChanged()/add/removeIt any items , it will cancel all animation at any time*/
            val viewHolderLoader = holder as ViewHolderLoader?
            CustomLoadingViewBlue.setAnimationForThreeDots(
                context,
                viewHolderLoader!!.imgDot1,
                viewHolderLoader.imgDot2,
                viewHolderLoader.imgDot3
            )
        } else {
            val viewHolderItem = holder as ViewHolderItem?
            viewHolderItem!!.txtSupplierName?.text = item.order?.supplier?.supplierName
            viewHolderItem.txtOutletName?.text = item.order?.outlet?.outletName
            viewHolderItem.txtOrderStatus?.visibility = View.GONE
            //            Double dollarTotalPrice = ZeemartBuyerApp.centsToDollar(item.getOrder().getAmount().getTotal().getAmount());
//            String orderTotal = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(dollarTotalPrice,2));
            if (item.order?.isReceived == true) {
                viewHolderItem.imgSwipeRightIndicator?.visibility = View.GONE
            } else {
                viewHolderItem.imgSwipeRightIndicator?.visibility = View.VISIBLE
            }
            val orderTotal = item.order?.amount?.total?.displayValue
            viewHolderItem.txtCreatedDate?.text = orderTotal
            viewHolderItem.txtCreatedDate?.setTextColor(context.resources.getColor(R.color.black))
            viewHolderItem.txtCreatedDate?.textSize = 16f
            viewHolderItem.txtDeliveryDate?.visibility = View.GONE
            val scale = context.resources.displayMetrics.density
            val dpAsPixels = (20 * scale + 0.5f).toInt()
            viewHolderItem.txtOutletName?.setPadding(0, 0, 0, dpAsPixels)
            val orderId = "#" + item.order?.orderId
            viewHolderItem.txtOrderId?.text = orderId
            viewHolderItem.txtOrderTotal?.visibility = View.GONE
            viewHolderItem.txtDraftIndicator?.visibility = View.GONE
            viewHolderItem.lytListRow?.setOnClickListener(View.OnClickListener {
                if (isUpcoming) AnalyticsHelper.logAction(
                    context,
                    AnalyticsHelper.TAP_ITEM_DELIVERIES_UPCOMING_ORDER
                ) else AnalyticsHelper.logAction(
                    context, AnalyticsHelper.TAP_ITEM_DELIVERIES_PAST_ORDER
                )
                val newIntent = Intent(context, OrderDetailsActivity::class.java)
                newIntent.putExtra(ZeemartAppConstants.ORDER_ID, item.order?.orderId)
                newIntent.putExtra(
                    ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                    item.order?.outlet?.outletId
                )
                context.startActivity(newIntent)
            })
            if (StringHelper.isStringNullOrEmpty(item.order?.supplier?.logoURL)) {
                viewHolderItem.imgSupplier?.visibility = View.INVISIBLE
                viewHolderItem.lytSupplierThumbNail?.visibility = View.VISIBLE
                viewHolderItem.lytSupplierThumbNail?.setBackgroundColor(
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        item.order?.supplier?.supplierName!!,
                        context
                    )
                )
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
            if (item.order?.isReceived!! || ((item.order!!.grn != null) && (item.order!!.grn?.grnList != null)
                        && (item.order!!.grn?.grnList?.size!! > 0) && !StringHelper.isStringNullOrEmpty(
                    item.order!!.grn?.grnList!![0].grnId
                ))
            ) {
                viewHolderItem.imgTruck?.setImageResource(R.drawable.green_receive_tick)
            } else {
                viewHolderItem.imgTruck?.setImageResource(R.drawable.truck)
            }
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
        if (viewOrderSectionOrRowData[headerPosition].headerData != null &&
            viewOrderSectionOrRowData[headerPosition].headerData?.date != null
        ) {
            txtdate?.text =
                viewOrderSectionOrRowData.get(headerPosition).headerData?.date?.trim { it <= ' ' }
        }
        ZeemartBuyerApp.setTypefaceView(
            txtdate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtDayString =
            "(" + viewOrderSectionOrRowData[headerPosition].headerData?.day?.trim { it <= ' ' } + ")"
        val txtDay = header?.findViewById<TextView>(R.id.txt_list_header_day)
        ZeemartBuyerApp.setTypefaceView(
            txtDay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtDay?.text = txtDayString
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return if (itemPosition < viewOrderSectionOrRowData.size) {
            viewOrderSectionOrRowData.get(itemPosition).isHeader
        } else {
            false
        }
    }

    override fun onHeaderClicked(header: View?, position: Int) {
        //do nothing
    }

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        var lytSupplierThumbNail: RelativeLayout?
        var txtSupplierThumbNail: TextView?

        init {
            imgSupplier = itemView.findViewById(R.id.dashboard_img_supplier)
            imgTruck = itemView.findViewById(R.id.dashboard_img_truck)
            txtSupplierName = itemView.findViewById(R.id.dashboard_txt_supplier_name)
            txtDeliveryDate = itemView.findViewById(R.id.dashboard_txt_delivery_date)
            txtCreatedDate = itemView.findViewById(R.id.dashboard_txt_order_created_date)
            txtOrderStatus = itemView.findViewById(R.id.dashboard_txt_order_status)
            txtOrderTotal = itemView.findViewById(R.id.dashboard_txt_order_total)
            txtOrderId = itemView.findViewById(R.id.dashboard_txt_order_id)
            txtOutletName = itemView.findViewById(R.id.dashboard_txt_outlet_name)
            txtDraftIndicator = itemView.findViewById(R.id.dashboard_text_draft_indicator)
            lytListRow = itemView.findViewById(R.id.lyt_dashboard_row)
            btnDeleteDraft = itemView.findViewById(R.id.dashboard_btn_delete_draft)
            btnRepeatOrder = itemView.findViewById(R.id.dashboard_btn_repeat_order)
            imgSwipeLeftIndicator = itemView.findViewById(R.id.img_swipe_left_indicator)
            imgSwipeRightIndicator = itemView.findViewById(R.id.img_swipe_right_indicator)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
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
                txtCreatedDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOutletName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtDeliveryDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderTotal,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            @JvmField
            val SWIPE_BUTTON_WIDTH = CommonMethods.dpToPx(70)
            @JvmField
            val SWIPE_BUTTON_TEXT_SIZE = CommonMethods.dpToPx(14)
            @JvmField
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
            viewOrderSectionOrRowData.clear()
            for (i in list.indices) {
                viewOrderSectionOrRowData.add(list[i])
            }
            notifyDataSetChanged()
        }
    }

    companion object {
        private val ITEM_TYPE_HEADER = 0
        private val ITEM_TYPE_ROW = 1
        private val ITEM_TYPE_LOADER = 2
    }
}