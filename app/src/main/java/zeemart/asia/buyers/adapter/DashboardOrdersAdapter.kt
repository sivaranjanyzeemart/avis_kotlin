package zeemart.asia.buyers.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.goodsReceivedNote.GoodsReceivedNoteDashBoardActivity
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.helper.DialogHelper.ReceiveOrderListener
import zeemart.asia.buyers.helper.DialogHelper.RejectOrderCallback
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.NotifyDataChangedListener
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimport.DashboardOrdersMgr
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.network.OutletsApi.GetSpecificOutletResponseListener
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.createorders.ReviewOrderActivity
import zeemart.asia.buyers.orders.deliveries.DeliveryListingActivity
import zeemart.asia.buyers.orders.vieworders.ViewOrdersActivity
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by ParulBhandari on 7/27/2018.
 */
class DashboardOrdersAdapter(
    private val context: Context,
    var dashboardOrdersManagerArraylist: ArrayList<DashboardOrdersMgr>,
    private val dashboardDataChangedListener: NotifyDataChangedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_header_dashboard, parent, false)
            ViewHolderHeader(v)
        } else if (viewType == VIEW_TYPE_NO_DATA_VIEW) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_dashboard_no_data, parent, false)
            ViewHolderNoData(v)
        } else if (viewType == VIEW_TYPE_VIEW_FULL_DATA) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_header_dashboard, parent, false)
            ViewHolderFullData(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_dashboard_row, parent, false)
            ViewHolderOrder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (dashboardOrdersManagerArraylist[position].isHeader) {
            val viewHolderHeader = holder as ViewHolderHeader
            viewHolderHeader.txtHeader.text = dashboardOrdersManagerArraylist[position].getDashboardOrdersMgrHeader()
        } else if (dashboardOrdersManagerArraylist[position].isNoDeliveries || dashboardOrdersManagerArraylist[position].isNoPendingOrder) {
            val viewHolderNoData = holder as ViewHolderNoData
            if (dashboardOrdersManagerArraylist[position].isNoDeliveries) {
                viewHolderNoData.noData.text =
                    context.resources.getString(R.string.txt_no_deliveries_are_scheduled_to_arrive_today)
                viewHolderNoData.noData.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.icontrucknodeliveriessmall,
                    0,
                    0
                )
            } else if (dashboardOrdersManagerArraylist[position].isNoPendingOrder) {
                viewHolderNoData.noData.text =
                    context.resources.getString(R.string.txt_all_orders_have_been_sent)
                viewHolderNoData.noData.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.noorderssmall,
                    0,
                    0
                )
            }
        } else if (dashboardOrdersManagerArraylist[position].isViewAllDeliveriesToday || dashboardOrdersManagerArraylist[position].isViewAllPlacedData) {
            val viewHolderFullData = holder as ViewHolderFullData
            viewHolderFullData.txtViewAllData.text =
                dashboardOrdersManagerArraylist[position].viewAllData
            viewHolderFullData.itemView.setOnClickListener {
                if (dashboardOrdersManagerArraylist[position].isViewAllDeliveriesToday) {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_DASHBOARD_VIEW_ALL_DELIVERIES
                    )
                    val newIntentScheduledDeliveries =
                        Intent(context, DeliveryListingActivity::class.java)
                    context.startActivity(newIntentScheduledDeliveries)
                } else {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_DASHBOARD_VIEW_PLACED_ORDERS
                    )
                    val newIntentPlaceOrders = Intent(context, ViewOrdersActivity::class.java)
                    context.startActivity(newIntentPlaceOrders)
                }
            }
        } else {
            val viewHolderOrder = holder as ViewHolderOrder
            val order = dashboardOrdersManagerArraylist[position].order
            //if none of the above its an order
            if (dashboardOrdersManagerArraylist[position].isActionRequiredOrder) {
                setActionRequiredRowData(viewHolderOrder, order!!)
            } else if (dashboardOrdersManagerArraylist[position].isContinueOrderingOrder) {
                setContinueOrderingRowData(viewHolderOrder, order!!)
            } else if (dashboardOrdersManagerArraylist[position].isPendingOrder) {
                setPendingOrdersRowData(viewHolderOrder, order!!)
            } else {
                setDeliveriesTodayRowData(viewHolderOrder, order!!)
            }
        }
    }

    fun setDeliveriesTodayRowData(viewHolderOrder: ViewHolderOrder, order: Orders) {
        viewHolderOrder.txtSupplierName.text = order.supplier?.supplierName
        viewHolderOrder.txtOutletName.text = order.outlet?.outletName
        viewHolderOrder.txtOrderStatus.visibility = View.GONE
        viewHolderOrder.txtCreatedDate.text = order.amount?.total?.displayValue
        viewHolderOrder.txtDeliveryDate.visibility = View.GONE
        viewHolderOrder.txtOutletName.setPadding(
            0,
            0,
            0,
            ViewHolderOrder.PADDING_FOR_TEXT_OUTLET_NAME
        )
        val orderId = "#" + order.orderId
        viewHolderOrder.txtOrderId.text = orderId
        viewHolderOrder.txtOrderTotal.visibility = View.GONE
        viewHolderOrder.txtDraftIndicator.visibility = View.GONE
        if (order.orderType == Orders.Type.DEAL.name || order.orderType == Orders.Type.ESSENTIALS.name) {
            viewHolderOrder.imgSwipeRightIndicator.visibility = View.GONE
        } else {
            viewHolderOrder.imgSwipeRightIndicator.visibility = View.VISIBLE
        }
        viewHolderOrder.lytListRow.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_DASHBOARD_DELIVERIES_TODAY)
            val newIntent = Intent(context, OrderDetailsActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, order.orderId)
            newIntent.putExtra(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID, order.outlet?.outletId)
            context.startActivity(newIntent)
        }
        if (StringHelper.isStringNullOrEmpty(order.supplier?.logoURL)) {
            viewHolderOrder.imgSupplier.visibility = View.INVISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
            viewHolderOrder.txtSupplierThumbNail.text =
                CommonMethods.SupplierThumbNailShortCutText(order.supplier?.supplierName!!)
            viewHolderOrder.txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
        } else {
            viewHolderOrder.imgSupplier.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.GONE
            Picasso.get().load(order.supplier?.logoURL).placeholder(R.drawable.placeholder_all)
                .into(viewHolderOrder.imgSupplier)
        }
        if (order.isReceived || order.grn != null && order.grn!!.grnList != null && order.grn!!.grnList?.size!! > 0
            && !StringHelper.isStringNullOrEmpty(
                order.grn!!.grnList!![0].grnId
            )
        ) {
            viewHolderOrder.imgTruck.setImageResource(R.drawable.green_receive_tick)
        } else {
            viewHolderOrder.imgTruck.setImageResource(R.drawable.truck)
        }
    }

    fun setPendingOrdersRowData(viewHolderOrder: ViewHolderOrder, order: Orders) {
        viewHolderOrder.txtCreatedDate.text =
            DateHelper.getDateInDateMonthFormat(order.timeUpdated, order.outlet?.timeZoneFromOutlet)
        viewHolderOrder.txtDeliveryDate.text = DateHelper.getDateInDateMonthFormat(
            order.timeDelivered,
            order.outlet?.timeZoneFromOutlet
        )
        viewHolderOrder.txtSupplierName.text = order.supplier?.supplierName
        ZeemartBuyerApp.setTypefaceView(
            viewHolderOrder.txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        viewHolderOrder.txtOutletName.text = order.outlet?.outletName
        if (order.amount != null && order.amount!!.total != null && order.amount!!.total?.displayValue != null)
            viewHolderOrder.txtOrderTotal.text =
            order.amount!!.total?.displayValue
        if (order.orderStatus == OrderStatus.PENDING_PAYMENT.statusName) {
            viewHolderOrder.imgSwipeRightIndicator.visibility = View.GONE
            val orderStatus =
                context.resources.getString(OrderStatus.PENDING_PAYMENT.statusResId) + " - " + context.resources.getString(
                    R.string.txt_verifying
                )
            viewHolderOrder.txtOrderStatus.text = orderStatus
            viewHolderOrder.txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
            viewHolderOrder.txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_yellow)
        } else {
            if (order.orderType == Orders.Type.DEAL.name || order.orderType == Orders.Type.ESSENTIALS.name) {
                viewHolderOrder.imgSwipeRightIndicator.visibility = View.GONE
            } else {
                viewHolderOrder.imgSwipeRightIndicator.visibility = View.VISIBLE
            }
            viewHolderOrder.txtOrderStatus.text = order.orderStatus
            OrderStatus.SetStatusBackground(context, order, viewHolderOrder.txtOrderStatus)
        }
        viewHolderOrder.txtDraftIndicator.visibility = View.GONE
        viewHolderOrder.lytListRow.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_DASHBOARD_ORDERS_PENDING)
            val newIntent = Intent(context, OrderDetailsActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, order.orderId)
            newIntent.putExtra(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID, order.outlet?.outletId)
            context.startActivity(newIntent)
        }
        if (StringHelper.isStringNullOrEmpty(order.supplier?.logoURL)) {
            viewHolderOrder.imgSupplier.visibility = View.INVISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
            viewHolderOrder.txtSupplierThumbNail.text =
                CommonMethods.SupplierThumbNailShortCutText(order.supplier?.supplierName!!)
            viewHolderOrder.txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
        } else {
            viewHolderOrder.imgSupplier.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.GONE
            Picasso.get().load(order.supplier?.logoURL).placeholder(R.drawable.placeholder_all)
                .into(viewHolderOrder.imgSupplier)
        }
    }

    fun setContinueOrderingRowData(viewHolderOrder: ViewHolderOrder, order: Orders) {
        viewHolderOrder.txtSupplierName.text = order.supplier?.supplierName
        ZeemartBuyerApp.setTypefaceView(
            viewHolderOrder.txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        viewHolderOrder.txtOutletName.text = order.outlet?.outletName
        OrderStatus.SetStatusBackground(
            context,
            Orders(OrderStatus.DRAFT.statusName),
            viewHolderOrder.txtOrderStatus
        )
        viewHolderOrder.imgTruck.visibility = View.GONE
        viewHolderOrder.txtCreatedDate.visibility = View.GONE
        val totalItems = order.products?.size
        var totalNoItems = " "
        totalNoItems = if (totalItems == 1) {
            totalItems.toString() + " " + context.getString(R.string.txt_item)
        } else {
            totalItems.toString() + " " + context.getString(R.string.txt_items)
        }
        viewHolderOrder.txtDeliveryDate.text = totalNoItems
        val constraintSet = ConstraintSet()
        constraintSet.clone(viewHolderOrder.lytListRow)
        constraintSet.connect(
            viewHolderOrder.txtDeliveryDate.id,
            ConstraintSet.LEFT,
            viewHolderOrder.txtSupplierName.id,
            ConstraintSet.LEFT
        )
        constraintSet.connect(
            viewHolderOrder.txtDeliveryDate.id,
            ConstraintSet.TOP,
            viewHolderOrder.txtSupplierName.id,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(viewHolderOrder.lytListRow)
        viewHolderOrder.txtCreatedDate.visibility = View.GONE
        val dateCreated = order.timeUpdated?.times(1000)
        val dateCreatedDate = dateCreated?.let { Date(it) }
        val todaysDate = Date()
        val differenceMap = DateHelper.computeDateDiff(dateCreatedDate!!, todaysDate)
        Log.d("DIFFERENCE MAP", Arrays.asList(differenceMap).toString() + "****")
        viewHolderOrder.txtOrderTotal.setTextColor(context.resources.getColor(R.color.text_blue))
        if (differenceMap[TimeUnit.DAYS] == 0L) {
            if (differenceMap[TimeUnit.HOURS] == 0L) {
                val updatedTimeText =
                    context.getString(R.string.txt_updated) + " " + differenceMap[TimeUnit.MINUTES] + " " + context.getString(
                        R.string.txt_minutes_ago
                    )
                viewHolderOrder.txtOrderTotal.text = updatedTimeText
            } else {
                val updatedTimeText =
                    context.getString(R.string.txt_updated) + " " + differenceMap[TimeUnit.HOURS] + " " + context.getString(
                        R.string.txt_hours_ago
                    )
                viewHolderOrder.txtOrderTotal.text = updatedTimeText
            }
        } else {
            val updatedTimeText =
                context.getString(R.string.txt_updated) + " " + differenceMap[TimeUnit.DAYS] + " " + context.getString(
                    R.string.txt_days_ago
                )
            viewHolderOrder.txtOrderTotal.text = updatedTimeText
        }
        if (StringHelper.isStringNullOrEmpty(order.supplier?.logoURL)) {
            viewHolderOrder.imgSupplier.visibility = View.INVISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
            viewHolderOrder.txtSupplierThumbNail.text =
                CommonMethods.SupplierThumbNailShortCutText(order.supplier?.supplierName!!)
            viewHolderOrder.txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
        } else {
            viewHolderOrder.imgSupplier.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.GONE
            Picasso.get().load(order.supplier?.logoURL).placeholder(R.drawable.placeholder_all)
                .into(viewHolderOrder.imgSupplier)
        }
        if (order.orderType == Orders.Type.DEAL.name || order.orderType == Orders.Type.ESSENTIALS.name) {
            viewHolderOrder.imgSwipeLeftIndicator.visibility = View.GONE
        } else {
            viewHolderOrder.imgSwipeLeftIndicator.visibility = View.VISIBLE
        }
        viewHolderOrder.lytListRow.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_DASHBOARD_CONTINUE_DRAFT)
            val newIntent = Intent(context, ReviewOrderActivity::class.java)
            val selectedOrder: MutableList<Orders> = ArrayList()
            selectedOrder.add(order)
            val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrder)
            newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
            val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.supplier)
            val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.outlet)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
            context.startActivity(newIntent)
        }
    }

    fun setActionRequiredRowData(viewHolderOrder: ViewHolderOrder, order: Orders) {
        viewHolderOrder.dashboardRowProgressBar.visibility = View.GONE
        if (StringHelper.isStringNullOrEmpty(order.supplier?.logoURL)) {
            viewHolderOrder.imgSupplier.visibility = View.INVISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
            viewHolderOrder.txtSupplierThumbNail.text =
                CommonMethods.SupplierThumbNailShortCutText(order.supplier?.supplierName!!)
            viewHolderOrder.txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    order.supplier?.supplierName!!,
                    context
                )
            )
        } else {
            viewHolderOrder.imgSupplier.visibility = View.VISIBLE
            viewHolderOrder.lytSupplierThumbNail.visibility = View.GONE
            Picasso.get().load(order.supplier?.logoURL).placeholder(R.drawable.placeholder_all)
                .into(viewHolderOrder.imgSupplier)
        }
        //orderDataList.get(position).getSupplier().get
        viewHolderOrder.txtCreatedDate.text =
            DateHelper.getDateInDateMonthFormat(order.timeUpdated, order.outlet?.timeZoneFromOutlet)
        viewHolderOrder.txtDeliveryDate.text = DateHelper.getDateInDateMonthFormat(
            order.timeDelivered,
            order.outlet?.timeZoneFromOutlet
        )
        viewHolderOrder.txtSupplierName.text = order.supplier?.supplierName
        ZeemartBuyerApp.setTypefaceView(
            viewHolderOrder.txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        viewHolderOrder.txtOutletName.text = order.outlet?.outletName
        if (order.orderStatus == OrderStatus.PENDING_PAYMENT.statusName) {
            viewHolderOrder.txtOrderStatus.text = order.orderStatus
            viewHolderOrder.txtOrderTotal.text = order.amount?.total?.displayValue
            val paymentDue = order.timeCutOff?.times(1000)
            val currentDate = System.currentTimeMillis()
            val intDays = paymentDue?.let { DateHelper.getDaysBetweenDates(it, currentDate) }
            var orderStatus = context.resources.getString(OrderStatus.PENDING_PAYMENT.statusResId)
            orderStatus = if (intDays == 0) {
                orderStatus + " - " + context.resources.getString(R.string.txt_due_today)
            } else {
                val dueDays =
                    String.format(context.resources.getString(R.string.txt_due_in_days), intDays)
                "$orderStatus - $dueDays"
            }
            viewHolderOrder.txtOrderStatus.text = orderStatus
            viewHolderOrder.txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_yellow)
            viewHolderOrder.txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
            viewHolderOrder.txtDraftIndicator.visibility = View.GONE
            viewHolderOrder.imgSwipeRightIndicator.visibility = View.GONE
            viewHolderOrder.imgSwipeLeftIndicator.visibility = View.GONE
        } else {
            viewHolderOrder.txtOrderStatus.text = order.orderStatus
            viewHolderOrder.txtOrderTotal.text = order.amount?.total?.displayValue
            viewHolderOrder.txtOrderStatus.setText(R.string.txt_needs_your_approval)
            viewHolderOrder.txtOrderStatus.setTextColor(context.resources.getColor(R.color.white))
            viewHolderOrder.txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
            viewHolderOrder.txtDraftIndicator.visibility = View.GONE
            if (order.orderType == Orders.Type.DEAL.name || order.orderType == Orders.Type.ESSENTIALS.name) {
                viewHolderOrder.imgSwipeRightIndicator.visibility = View.GONE
                viewHolderOrder.imgSwipeLeftIndicator.visibility = View.GONE
            } else {
                viewHolderOrder.imgSwipeRightIndicator.visibility = View.VISIBLE
                viewHolderOrder.imgSwipeLeftIndicator.visibility = View.VISIBLE
            }
        }
        viewHolderOrder.lytListRow.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_DASHBOARD_ACTION_REQUIRED)
            val newIntent = Intent(context, OrderDetailsActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, order.orderId)
            newIntent.putExtra(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID, order.outlet?.outletId)
            context.startActivity(newIntent)
        }
    }

    override fun getItemCount(): Int {
        return dashboardOrdersManagerArraylist.size
    }

    override fun getItemViewType(position: Int): Int {
        val data = dashboardOrdersManagerArraylist[position]
        return if (data.isHeader) {
            VIEW_TYPE_HEADER
        } else if (data.isNoDeliveries || data.isNoPendingOrder) {
            VIEW_TYPE_NO_DATA_VIEW
        } else if (data.isViewAllPlacedData || data.isViewAllDeliveriesToday) {
            VIEW_TYPE_VIEW_FULL_DATA
        } else if (data.isActionRequiredOrder) {
            VIEW_TYPE_ACTION_REQUIRED_ORDER
        } else if (data.isContinueOrderingOrder) {
            VIEW_TYPE_CONTINUE_ORDERING_ORDER
        } else if (data.isPendingOrder) {
            VIEW_TYPE_PENDING_ORDER
        } else if (data.isDeliveriesOrder) {
            VIEW_TYPE_TODAY_DELIVERY_ORDER
        } else {
            0
        }
    }

    class ViewHolderOrder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var lytListRow: ConstraintLayout
        var dashboardRowProgressBar: ProgressBar
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var txtDeliveryDate: TextView
        var txtCreatedDate: TextView
        var txtOrderStatus: TextView
        var txtOrderTotal: TextView
        var txtOrderId: TextView
        var txtOutletName: TextView
        var imgTruck: ImageView
        var txtDraftIndicator: TextView
        var imgSwipeLeftIndicator: ImageView
        var imgSwipeRightIndicator: ImageView
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            dashboardRowProgressBar = itemView.findViewById(R.id.dashboardRow_progressBar)
            lytListRow = itemView.findViewById(R.id.lyt_dashboard_row)
            imgSupplier = itemView.findViewById(R.id.dashboard_img_supplier)
            imgTruck = itemView.findViewById(R.id.dashboard_img_truck)
            txtSupplierName = itemView.findViewById(R.id.dashboard_txt_supplier_name)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtDeliveryDate = itemView.findViewById(R.id.dashboard_txt_delivery_date)
            ZeemartBuyerApp.setTypefaceView(
                txtDeliveryDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtCreatedDate = itemView.findViewById(R.id.dashboard_txt_order_created_date)
            ZeemartBuyerApp.setTypefaceView(
                txtCreatedDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            txtOrderStatus = itemView.findViewById(R.id.dashboard_txt_order_status)
            ZeemartBuyerApp.setTypefaceView(
                txtOrderStatus,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtOrderTotal = itemView.findViewById(R.id.dashboard_txt_order_total)
            ZeemartBuyerApp.setTypefaceView(
                txtOrderTotal,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            txtOrderId = itemView.findViewById(R.id.dashboard_txt_order_id)
            txtOutletName = itemView.findViewById(R.id.dashboard_txt_outlet_name)
            ZeemartBuyerApp.setTypefaceView(
                txtOutletName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            txtDraftIndicator = itemView.findViewById(R.id.dashboard_text_draft_indicator)
            imgSwipeLeftIndicator = itemView.findViewById(R.id.img_swipe_left_indicator)
            imgSwipeRightIndicator = itemView.findViewById(R.id.img_swipe_right_indicator)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }

        companion object {
            val PADDING_FOR_TEXT_OUTLET_NAME = CommonMethods.dpToPx(15)
        }
    }

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeader: TextView

        init {
            txtHeader = itemView.findViewById(R.id.txt_dashboard_sublist_header)
            txtHeader.setTextColor(context.resources.getColor(R.color.black))
            ZeemartBuyerApp.setTypefaceView(
                txtHeader,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtHeader.textSize = 18f
        }
    }

    inner class ViewHolderFullData(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtViewAllData: TextView

        init {
            txtViewAllData = itemView.findViewById(R.id.txt_dashboard_sublist_header)
            txtViewAllData.setTextColor(context.resources.getColor(R.color.text_blue))
            ZeemartBuyerApp.setTypefaceView(
                txtViewAllData,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            itemView.setBackgroundColor(context.resources.getColor(R.color.white))
        }
    }

    inner class ViewHolderNoData(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noData: Button

        init {
            noData = itemView.findViewById(R.id.home_btn_no_data)
            ZeemartBuyerApp.setTypefaceView(
                noData,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    fun removeAt(
        position: Int,
        isDeleteDraft: Boolean,
        isApprove: Boolean,
        isReceive: Boolean,
        isRepeatOrder: Boolean,
        mOrder: Orders
    ) {
        if (isReceive) {
            //call the receive API
            if (dashboardOrdersManagerArraylist[position].order != null) {
                val order = dashboardOrdersManagerArraylist[position].order
                order?.outlet?.let {
                    OutletsApi.getSpecificOutlet(
                        context,
                        it,
                        object : GetSpecificOutletResponseListener {
                            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                                if (specificOutlet != null && specificOutlet.data != null && specificOutlet.data!!.settings != null && specificOutlet.data!!.settings?.isEnableGRN == true) {
                                    val intent =
                                        Intent(context, GoodsReceivedNoteDashBoardActivity::class.java)
                                    intent.putExtra(
                                        ZeemartAppConstants.ORDER_DETAILS_JSON,
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(order)
                                    )
                                    context.startActivity(intent)
                                    notifyItemChanged(position)
                                } else {
                                    DialogHelper.receiveConfirmationDialog(
                                        context,
                                        order,
                                        AnalyticsHelper.SLIDE_ITEM_DASHBOARD_RECEIVE_DELIVERIES,
                                        object : ReceiveOrderListener {
                                            override fun onReceiveSuccessful() {
                                                dashboardDataChangedListener.notifyResetAdapter()
                                            }

                                            override fun onReceiveError() {
                                                ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_receive_error_message))
                                            }

                                            override fun onDialogDismiss() {
                                                notifyItemChanged(position)
                                            }
                                        })
                                }
                            }

                            override fun onError(error: VolleyErrorHelper?) {
                                DialogHelper.receiveConfirmationDialog(
                                    context,
                                    order,
                                    AnalyticsHelper.SLIDE_ITEM_DASHBOARD_RECEIVE_DELIVERIES,
                                    object : ReceiveOrderListener {
                                        override fun onReceiveSuccessful() {
                                            dashboardDataChangedListener.notifyResetAdapter()
                                        }

                                        override fun onReceiveError() {
                                            ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_receive_error_message))
                                        }

                                        override fun onDialogDismiss() {
                                            notifyItemChanged(position)
                                        }
                                    })
                            }
                        })
                }
            }
        } else if (isRepeatOrder) {
            OrderHelper.checkExistingDraftThenDiscard(context, mOrder.supplier?.supplierId!!, null)
            val newIntent = Intent(context, ReviewOrderActivity::class.java)
            val selectedOrder: MutableList<Orders> = ArrayList()
            selectedOrder.add(mOrder)
            val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrder)
            newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
            val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder.supplier)
            val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder.outlet)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
            context.startActivity(newIntent)
        } else if (isDeleteDraft) {
            //remove from the backend server
            DeleteOrder(context, object : DeleteOrder.GetResponseStatusListener {
                override fun onSuccessResponse(status: String?) {}
                override fun onErrorResponse(error: VolleyError?) {
                    ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_delete_draft_fail))
                }
            }).deleteOrderData(
                dashboardOrdersManagerArraylist[position].order?.orderId,
                dashboardOrdersManagerArraylist[position].order?.outlet?.outletId
            )
            //remove continue ordering header
            dashboardOrdersManagerArraylist.removeAt(position)
            notifyItemRemoved(position)
            removeContinueOrderingHeader()
            Log.d("", dashboardOrdersManagerArraylist.size.toString() + "**")
        } else {
            if (isApprove) {
                val builder = AlertDialog.Builder(
                    context
                )
                builder.setTitle(context.getString(R.string.txt_approve_order))
                builder.setMessage(context.getString(R.string.txt_want_to_approve_the_order))
                builder.setPositiveButton(context.getString(R.string.txt_approve)) { dialog, which ->
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_ORDER_REVIEW_APPROVE_ORDER,
                        dashboardOrdersManagerArraylist[position].order!!
                    )
                    dashboardOrdersManagerArraylist[position].order?.let {
                        ApproveRejectOrders.ApproveOrder(
                            context,
                            it,
                            object : ApproveRejectOrders.GetResponseStatusListener {
                                override fun onSuccessResponse(status: String?) {}
                                override fun onErrorResponse(error: VolleyError?) {
                                    ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_error_approving_order))
                                    dashboardDataChangedListener.notifyResetAdapter()
                                }
                            })
                    }
                    dashboardOrdersManagerArraylist.removeAt(position)
                    notifyItemRemoved(position)
                    removeActionRequiredHeaderRow()
                    dialog.dismiss()
                }
                builder.setNegativeButton(context.getString(R.string.txt_no)) { dialog, which -> dialog.dismiss() }
                val d = builder.create()
                d.setOnDismissListener { notifyItemChanged(position) }
                d.show()
            } else {
                DialogHelper.ShowRejectOrder(context, object : RejectOrderCallback {
                    override fun dismiss() {
                        notifyItemChanged(position)
                    }

                    override fun rejectOrder(remarks: String?) {
                        dashboardOrdersManagerArraylist[position].order?.let {
                            ApproveRejectOrders.RejectOrder(
                                context,
                                it,
                                remarks,
                                object : ApproveRejectOrders.GetResponseStatusListener {
                                    override fun onSuccessResponse(status: String?) {}
                                    override fun onErrorResponse(error: VolleyError?) {
                                        ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_error_rejecting_order))
                                        dashboardDataChangedListener.notifyResetAdapter()
                                    }
                                })
                        }
                        dashboardOrdersManagerArraylist.removeAt(position)
                        notifyItemRemoved(position)
                        removeActionRequiredHeaderRow()
                    }
                })
            }
        }
    }

    fun removeContinueOrderingHeader() {
        var continueOrderingCount = 0
        var continueOrderingOrderHeadingPosition = -1
        for (i in dashboardOrdersManagerArraylist.indices) {
            if (continueOrderingOrderHeadingPosition == -1) {
                if (dashboardOrdersManagerArraylist[i].isHeader && dashboardOrdersManagerArraylist[i].getDashboardOrdersMgrHeader() == context.resources.getString(
                        R.string.fragment_home_txt_home_continue_ordering_text
                    )
                ) {
                    continueOrderingOrderHeadingPosition = i
                }
            }
            if (dashboardOrdersManagerArraylist[i].isContinueOrderingOrder) {
                continueOrderingCount = continueOrderingCount + 1
            }
        }
        if (continueOrderingCount == 0) {
            dashboardOrdersManagerArraylist.removeAt(continueOrderingOrderHeadingPosition)
            notifyItemRemoved(continueOrderingOrderHeadingPosition)
        }
    }

    fun removeActionRequiredHeaderRow() {
        //check if it was the last order to be rejected remove the action required heading
        var actionRequiredCount = 0
        for (i in dashboardOrdersManagerArraylist.indices) {
            if (dashboardOrdersManagerArraylist[i].isActionRequiredOrder) {
                actionRequiredCount = actionRequiredCount + 1
            }
        }
        if (actionRequiredCount == 0) {
            dashboardOrdersManagerArraylist.removeAt(0)
            notifyItemRemoved(0)
        }
    }

    companion object {
        const val VIEW_TYPE_HEADER = 1
        const val VIEW_TYPE_VIEW_FULL_DATA = 2
        const val VIEW_TYPE_NO_DATA_VIEW = 3
        const val VIEW_TYPE_ACTION_REQUIRED_ORDER = 4
        const val VIEW_TYPE_CONTINUE_ORDERING_ORDER = 5
        const val VIEW_TYPE_PENDING_ORDER = 6
        const val VIEW_TYPE_TODAY_DELIVERY_ORDER = 7
    }
}