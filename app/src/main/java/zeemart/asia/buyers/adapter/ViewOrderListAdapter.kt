package zeemart.asia.buyers.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.deals.ReviewDealsActivity
import zeemart.asia.buyers.essentials.ReviewEssentialsActivity
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DialogHelper.CancelOrderCallback
import zeemart.asia.buyers.helper.DialogHelper.ShowCancelOrder
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.NotifyDataChangedListener
import zeemart.asia.buyers.models.ChartsDataModel.ChartsDataByDateCreated
import zeemart.asia.buyers.models.EssentialsBaseResponse
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.models.orderimportimport.ListWithStickyHeaderUI
import zeemart.asia.buyers.network.CancelOrder
import zeemart.asia.buyers.network.EssentialsApi
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseListener
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderHelper.DealsResponseListener
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.createorders.ReviewOrderActivity

/**
 * Created by ParulBhandari on 12/28/2017.
 */
class ViewOrderListAdapter(
    private val context: Context,
    val adapterDataList: ArrayList<ListWithStickyHeaderUI>,
    listener: NotifyDataChangedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HeaderItemDecoration.StickyHeaderInterface {
    private val listener: NotifyDataChangedListener
    private val searchedString = ""
    private var lstDeals: DealsBaseResponse.Deals? = null
    private var lstEssentials: Essential? = null

    init {
        updateOrderListWithTotal()
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_order_list_header, parent, false)
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
        val item = adapterDataList[position]
        if (item.isHeader) {
            val header = holder as ViewHolderHeader?
            val headerDayData = "(" + item.headerData?.day?.trim { it <= ' ' } + ")"
            header!!.txtDay.text = headerDayData.trim { it <= ' ' }
            header.txtDate.text = item.headerData?.date?.trim { it <= ' ' }
            //            Double dollarTotalPrice = ZeemartBuyerApp.centsToDollar(item.getHeaderData().getOrderTotal());
//            String orderTotal = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(dollarTotalPrice, 2));
            header.txtOrderTotal.text = PriceDetails(item.headerData?.orderTotal).displayValue
        } else if (item.isLoader) {
            val viewHolderLoader = holder as ViewHolderLoader?
            /*set the Animation in onBindViewHolder , because if we use notifyDataSetChanged()/add/removeIt any items , it will cancel all animation at any time*/CustomLoadingViewBlue.setAnimationForThreeDots(
                context,
                viewHolderLoader!!.imgDot1,
                viewHolderLoader.imgDot2,
                viewHolderLoader.imgDot3
            )
        } else {
            val itemHolder = holder as ViewHolderItem?
            itemHolder!!.txtSupplierName.text = item.order?.supplier?.supplierName
            itemHolder.txtOutletName.text = item.order?.outlet?.outletName
            val dateDelivered = DateHelper.getDateInDateMonthFormat(
                item.order?.timeDelivered,
                item.order?.outlet?.timeZoneFromOutlet
            )
            itemHolder.txtCreatedDate.visibility = View.INVISIBLE
            itemHolder.txtDeliveryDate.text = dateDelivered
            itemHolder.txtOrderTotal.text = item.order?.amount?.total?.displayValue
            itemHolder.txtDraftIndicator.visibility = View.GONE
            itemHolder.txtOrderNumber.visibility = View.VISIBLE
            itemHolder.txtOrderNumber.text = "#" + item.order?.orderId
            //set swipe indicator for repeat order swipe availability
//            if (item.getOrder().getOrderType().equals(Orders.Type.DEAL.getName()) || item.getOrder().getOrderType().equals(Orders.Type.ESSENTIALS.getName())) {
//                itemHolder.imgSwipeRightIndicator.setVisibility(View.GONE);
//            } else {
//                itemHolder.imgSwipeRightIndicator.setVisibility(View.VISIBLE);
//            }
            itemHolder.imgSwipeRightIndicator.visibility = View.VISIBLE
            //set visibility for recurring order image
            if ((item.order?.orderType == Orders.Type.STANDING.name)) {
                itemHolder.imgRecurringOrder.visibility = View.VISIBLE
            } else {
                itemHolder.imgRecurringOrder.visibility = View.GONE
            }
            if ((item.order?.orderType == Orders.Type.DEAL.name) || (item.order?.orderType == Orders.Type.ESSENTIALS.name)) {
                itemHolder.imgSwipeLeftIndicator.visibility = View.GONE
            } else {
                //set swipe indicator if order is placed and created by the logged in user
                if ((item.order?.orderStatus == OrderStatus.PLACED.statusName) && (item.order!!.isApprovedByUser || item.order!!.isOrderByUser)) {
                    itemHolder.imgSwipeLeftIndicator.visibility = View.VISIBLE
                } else {
                    itemHolder.imgSwipeLeftIndicator.visibility = View.GONE
                }
            }
            if ((item.order?.orderStatus == OrderStatus.PLACED.statusName)) {
                itemHolder.itemView.alpha = 1.0f
                itemHolder.txtDraftIndicator.visibility = View.INVISIBLE
            } else if ((item.order?.orderStatus == OrderStatus.INVOICED.statusName)) {
                itemHolder.itemView.alpha = 1.0f
                itemHolder.txtDraftIndicator.visibility = View.INVISIBLE
            } else if (((item.order?.orderStatus == OrderStatus.CANCELLED.statusName) || (item.order?.orderStatus == OrderStatus.REJECTED.statusName) || (item.order?.orderStatus == OrderStatus.VOIDED.statusName))) {
                itemHolder.itemView.alpha = 0.3f
                itemHolder.txtDraftIndicator.visibility = View.INVISIBLE
            } else {
                itemHolder.itemView.alpha = 1.0f
            }
            item.order?.let { order ->
                OrderStatus.SetStatusBackground(context, order, itemHolder.txtOrderStatus)
            }
            // OrderStatus.SetStatusBackground(context, item.order!!, itemHolder.txtOrderStatus)
            holder!!.lytListRow.setOnClickListener(
                View.OnClickListener {
                    AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_ORDERS_LIST_ORDER)
                    val newIntent = Intent(context, OrderDetailsActivity::class.java)
                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, item.order!!.orderId)
                    newIntent.putExtra(
                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                        item.order!!.outlet?.outletId
                    )
                    (context as Activity).startActivityForResult(
                        newIntent,
                        REQUEST_CODE_ORDER_STATUS_CHANGED
                    )
                })
            item.order?.supplier?.let { supplier ->
                if (StringHelper.isStringNullOrEmpty(supplier.logoURL)) {
                    itemHolder.imgSupplier.visibility = View.INVISIBLE
                    itemHolder.lytSupplierThumbNail.visibility = View.VISIBLE
                    itemHolder.lytSupplierThumbNail.setBackgroundColor(
                        CommonMethods.SupplierThumbNailBackgroundColor(
                            supplier.supplierName!!,
                            context
                        )
                    )
                    itemHolder.txtSupplierThumbNail.text =
                        CommonMethods.SupplierThumbNailShortCutText(supplier.supplierName!!)
                    itemHolder.txtSupplierThumbNail.setTextColor(
                        CommonMethods.SupplierThumbNailTextColor(
                            supplier.supplierName!!,
                            context
                        )
                    )
                } else {
                    itemHolder.imgSupplier.visibility = View.VISIBLE
                    itemHolder.lytSupplierThumbNail.visibility = View.GONE
                    Picasso.get().load(supplier.logoURL)
                        .placeholder(R.drawable.placeholder_all).into(
                            itemHolder.imgSupplier
                        )
                }
            }
            /*   if (StringHelper.isStringNullOrEmpty(item.order!!.supplier?.logoURL)) {
                   itemHolder.imgSupplier.visibility = View.INVISIBLE
                   itemHolder.lytSupplierThumbNail.visibility = View.VISIBLE
                   itemHolder.lytSupplierThumbNail.setBackgroundColor(
                       CommonMethods.SupplierThumbNailBackgroundColor(
                           item.order!!.supplier?.supplierName!!,
                           context
                       )
                   )
                   itemHolder.txtSupplierThumbNail.text =
                       CommonMethods.SupplierThumbNailShortCutText(item.order!!.supplier?.supplierName!!)
                   itemHolder.txtSupplierThumbNail.setTextColor(
                       CommonMethods.SupplierThumbNailTextColor(
                           item.order!!.supplier?.supplierName!!,
                           context
                       )
                   )
               } else {
                   itemHolder.imgSupplier.visibility = View.VISIBLE
                   itemHolder.lytSupplierThumbNail.visibility = View.GONE
                   Picasso.get().load(item.order!!.supplier?.logoURL)
                       .placeholder(R.drawable.placeholder_all).into(
                       itemHolder.imgSupplier
                   )
               }*/
            item.order?.let { order ->
                if (order.isReceived ||
                    (order.grn?.grnList?.isNotEmpty() == true && !StringHelper.isStringNullOrEmpty(
                        order.grn!!.grnList?.get(0)?.grnId
                    ))
                ) {
                    itemHolder.imgTruck.setImageResource(R.drawable.green_receive_tick)
                } else {
                    itemHolder.imgTruck.setImageResource(R.drawable.truck)
                }
            }


            /*  if (item.order!!.isReceived || ((item.order!!.grn != null) && (item.order!!.grn?.grnList != null) && (item.order!!.grn?.grnList?.size!! > 0) && !StringHelper.isStringNullOrEmpty(
                      item.order!!.grn?.grnList!![0].grnId
                  ))
              ) {
                  itemHolder.imgTruck.setImageResource(R.drawable.green_receive_tick)
              } else {
                  itemHolder.imgTruck.setImageResource(R.drawable.truck)
              }*/
        }
    }

    override fun getItemCount(): Int {
        return adapterDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item = adapterDataList[position]
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
        return R.layout.lst_order_list_header
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        val txtHeaderDate = header?.findViewById<View>(R.id.txt_list_header_date) as TextView
        ZeemartBuyerApp.setTypefaceView(
            txtHeaderDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtHeaderDay = header.findViewById<View>(R.id.txt_list_header_day) as TextView
        ZeemartBuyerApp.setTypefaceView(
            txtHeaderDay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtHeaderDate.text = adapterDataList.get(headerPosition).headerData?.date
        val txtDay = "(" + adapterDataList[headerPosition].headerData?.day?.trim { it <= ' ' } + ")"
        txtHeaderDay.text = txtDay.trim { it <= ' ' }

//        Double dollarTotalPrice = ZeemartBuyerApp.centsToDollar(viewOrderSectionOrRowData.get(headerPosition).getHeaderData().getOrderTotal());
//        String orderTotal = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(dollarTotalPrice, 2));
        val txtOrdersTotal = header.findViewById<View>(R.id.txt_list_orders_total) as TextView
        txtOrdersTotal.text =
            PriceDetails(adapterDataList.get(headerPosition).headerData?.orderTotal).displayValue
        ZeemartBuyerApp.setTypefaceView(
            txtOrdersTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return if (itemPosition < adapterDataList.size) {
            adapterDataList.get(itemPosition).isHeader
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
        var txtOrderTotal: TextView

        init {
            txtDate = itemView.findViewById(R.id.txt_list_header_date)
            txtDay = itemView.findViewById(R.id.txt_list_header_day)
            txtOrderTotal = itemView.findViewById(R.id.txt_list_orders_total)
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtDay,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderTotal,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    class ViewHolderItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var txtDeliveryDate: TextView
        var txtCreatedDate: TextView
        var txtOrderStatus: TextView
        var txtOrderTotal: TextView
        var txtOrderId: TextView
        var txtOutletName: TextView
        var imgTruck: ImageView
        var imgRecurringOrder: ImageView
        var txtDraftIndicator: TextView
        var lytListRow: ConstraintLayout
        var txtOrderNumber: TextView
        var imgSwipeRightIndicator: ImageView
        var imgSwipeLeftIndicator: ImageView
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.dashboard_img_supplier)
            imgTruck = itemView.findViewById(R.id.dashboard_img_truck)
            imgRecurringOrder = itemView.findViewById(R.id.img_recurring_order)
            txtSupplierName = itemView.findViewById(R.id.dashboard_txt_supplier_name)
            txtDeliveryDate = itemView.findViewById(R.id.dashboard_txt_delivery_date)
            txtCreatedDate = itemView.findViewById(R.id.dashboard_txt_order_created_date)
            txtOrderStatus = itemView.findViewById(R.id.dashboard_txt_order_status)
            txtOrderTotal = itemView.findViewById(R.id.dashboard_txt_order_total)
            txtOrderId = itemView.findViewById(R.id.dashboard_txt_order_id)
            txtOutletName = itemView.findViewById(R.id.dashboard_txt_outlet_name)
            txtDraftIndicator = itemView.findViewById(R.id.dashboard_text_draft_indicator)
            lytListRow = itemView.findViewById(R.id.lyt_dashboard_row)
            txtOrderNumber = itemView.findViewById(R.id.txt_order_list_order_id)
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
                txtOrderTotal,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtDeliveryDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOutletName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderId,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderNumber,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderStatus,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
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
        @JvmField
        var imgDot1: ImageView
        @JvmField
        var imgDot2: ImageView
        @JvmField
        var imgDot3: ImageView

        init {
            imgDot1 = v.findViewById(R.id.img_dot_1)
            imgDot2 = v.findViewById(R.id.img_dot_2)
            imgDot3 = v.findViewById(R.id.img_dot_3)
        }
    }

    fun removeAt(position: Int, isRepeat: Boolean) {
        if (isRepeat) {
            notifyDataSetChanged()
            val order = adapterDataList[position].order
            if ((order?.orderType == Orders.Type.DEAL.name)) {
                if (!StringHelper.isStringNullOrEmpty(order.dealNumber)) {
                    OrderHelper.getActiveDeals(
                        context,
                        order.dealNumber,
                        object : DealsResponseListener {
                            override fun onSuccessResponse(dealsBaseResponse: DealsBaseResponse?) {
                                if ((dealsBaseResponse != null) && (dealsBaseResponse.deals != null) && (dealsBaseResponse.deals!!.size > 0)) {
                                    lstDeals = dealsBaseResponse.deals!![0]
                                    openDeals(order)
                                } else {
                                    showAlert()
                                }
                            }

                            override fun onErrorResponse(error: VolleyError?) {
                                showAlert()
                            }
                        })
                }
            } else if ((order?.orderType == Orders.Type.ESSENTIALS.name)) {
                if (!StringHelper.isStringNullOrEmpty(order?.essentialsId)) {
                    val apiParamsHelper = ApiParamsHelper()
                    apiParamsHelper.setEssentialsId(order.essentialsId!!)
                    EssentialsApi.getPaginatedEssentials(
                        context,
                        apiParamsHelper,
                        object : EssentialsResponseListener {
                            override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                                if ((essentialsList != null) && (essentialsList.essentials != null) && (essentialsList.essentials!!.size > 0)) {
                                    lstEssentials = essentialsList.essentials!![0]
                                    openEssentials(order)
                                } else {
                                    showAlert()
                                }
                            }

                            override fun onErrorResponse(error: VolleyError?) {
                                showAlert()
                            }
                        })
                }
            } else {
                OrderHelper.checkExistingDraftThenDiscard(
                    context,
                    adapterDataList[position].order?.supplier?.supplierId!!,
                    null
                )
                val newIntent = Intent(context, ReviewOrderActivity::class.java)
                val selectedOrders: MutableList<Orders> = ArrayList()
                selectedOrders.add(adapterDataList[position].order!!)
                val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrders)
                newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
                val supplierJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrders[0].supplier)
                val outletJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrders[0].outlet)
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
                newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
                context.startActivity(newIntent)
            }
        } else {
            ShowCancelOrder(context, object : CancelOrderCallback {
                override fun dismiss() {
                    notifyItemChanged(position)
                }

                override fun cancelOrder(remarks: String?) {
                    val orderStatusBeforeCancel = adapterDataList[position].order?.orderStatus
                    val updateOrderStatusRequestData = OrderHelper.CancelOrderJson(
                        adapterDataList[position].order, remarks
                    )
                    CancelOrder(context, object : CancelOrder.GetResponseStatusListener {
                        override fun onSuccessResponse(status: String?) {
                            //refresh the entire list with new data
                            Log.d("status", status!!)
                            listener.notifyResetAdapter()
                        }

                        override fun onErrorResponse(error: VolleyError?) {
                            adapterDataList.get(position).order?.orderStatus =
                                orderStatusBeforeCancel
                            notifyItemChanged(position)
                            if (error?.networkResponse != null && error?.message != null) {
                                if ((error.networkResponse.statusCode.toString() == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE)) {
                                    ZeemartBuyerApp.getToastRed(error.message)
                                }
                            }
                        }
                    }).cancelOrderData(
                        updateOrderStatusRequestData,
                        adapterDataList[position].order?.outlet?.outletId
                    )
                }
            })
        }
    }

    private fun openDeals(order: Orders) {
        val newIntent = Intent(context, ReviewDealsActivity::class.java)
        val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order)
        newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
        val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.supplier)
        val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.outlet)
        newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
        newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
        val deliveriesJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstDeals!!.deliveryDates)
        newIntent.putExtra(ZeemartAppConstants.DELIVERY_DATES, deliveriesJson)
        newIntent.putExtra(ZeemartAppConstants.DEAL_NUMBER, lstDeals!!.dealNumber)
        newIntent.putExtra(ZeemartAppConstants.DEAL_NAME, lstDeals!!.title)
        if (lstDeals!!.deliveryFeePolicy != null) {
            val minimumOrderValueJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                lstDeals!!.deliveryFeePolicy?.minOrder
            )
            newIntent.putExtra(
                ZeemartAppConstants.DEAL_MINIMUM_ORDER_VALUE_DETAILS,
                minimumOrderValueJson
            )
        }
        val dealsList = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstDeals)
        newIntent.putExtra(ZeemartAppConstants.DEALS_LIST, dealsList)
        newIntent.putExtra(
            ZeemartAppConstants.CALLED_FROM,
            ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
        )
        context.startActivity(newIntent)
    }

    private fun openEssentials(order: Orders) {
        val newIntent = Intent(context, ReviewEssentialsActivity::class.java)
        val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order)
        newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
        val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.supplier)
        val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.outlet)
        newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
        newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
        newIntent.putExtra(ZeemartAppConstants.ESSENTIAL_ID, order.essentialsId)
        val deliveriesJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
            lstEssentials!!.deliveryDates
        )
        newIntent.putExtra(ZeemartAppConstants.DELIVERY_DATES, deliveriesJson)
        if (lstEssentials!!.deliveryFeePolicy != null) {
            val minimumOrderValueJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                lstEssentials!!.deliveryFeePolicy?.minOrder
            )
            newIntent.putExtra(
                ZeemartAppConstants.ESSENTIAL_MINIMUM_ORDER_VALUE_DETAILS,
                minimumOrderValueJson
            )
        }
        val essentialList = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstEssentials)
        newIntent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, essentialList)
        newIntent.putExtra(
            ZeemartAppConstants.CALLED_FROM,
            ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
        )
        context.startActivity(newIntent)
    }

    private fun showAlert() {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(250)
        val builder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.supplier_unavailable_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        val txtHeading = view.findViewById<TextView>(R.id.txt_small_dialog_failure)
        txtHeading.setText(R.string.txt_cant_repeat)
        val txtMessage = view.findViewById<TextView>(R.id.txt_small_dialog_failure_message)
        txtMessage.setText(R.string.txt_item_no_available)
        val btnOK = view.findViewById<Button>(R.id.btn_ok)
        btnOK.setText(R.string.dialog_ok_button_text)
        ZeemartBuyerApp.setTypefaceView(
            txtHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnOK,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setLayout(DIALOG_LAYOUT_WIDTH, WindowManager.LayoutParams.WRAP_CONTENT)
        btnOK.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                dialog.dismiss()
            }
        })
    }

    fun setLastDateTotal(chartsDataByDateCreated: ChartsDataByDateCreated) {
        for (i in adapterDataList.indices) {
            if (adapterDataList[i].isHeader) {
                val date =
                    DateHelper.getDateInDateMonthYearFormat(chartsDataByDateCreated.dateCreated)
                if ((adapterDataList[i].headerData?.date == date)) {
                    adapterDataList.get(i).headerData?.orderTotal =
                        chartsDataByDateCreated.total?.amount
                }
            }
        }
    }

    fun updateOrderListWithTotal() {
        var amount = 0.0
        var headerPosition = 0
        for (i in adapterDataList.indices) {
            if (adapterDataList[i].isHeader) {
                if (i != 0) {
                    adapterDataList.get(headerPosition).headerData?.orderTotal = amount
                    amount = 0.0
                }
                headerPosition = i
            } else {
                if (adapterDataList[i].order?.orderStatus !in listOf(OrderStatus.CANCELLED.statusName, OrderStatus.REJECTED.statusName, OrderStatus.VOIDED.statusName)) {
                    val order = adapterDataList[i].order
                    if (order != null && order.amount?.total?.amount != null) {
                        amount = amount + order.amount!!.total?.amount!!
                        // ...
                    }
                }
                /* if (!((adapterDataList[i].order?.orderStatus == OrderStatus.CANCELLED.statusName) || (adapterDataList[i].order?.orderStatus == OrderStatus.REJECTED.statusName) || (adapterDataList[i].order?.orderStatus == OrderStatus.VOIDED.statusName))) {
                     amount = amount + adapterDataList[i].order?.amount?.total?.amount!!
                     if (i == adapterDataList.size - 1) {
                         adapterDataList.get(headerPosition).headerData?.orderTotal = amount
                     }
                 }*/
            }
        }
    }

    companion object {
        private val ITEM_TYPE_HEADER = 0
        private val ITEM_TYPE_ROW = 1
        private val ITEM_TYPE_LOADER = 2
        private val REQUEST_CODE_ORDER_STATUS_CHANGED = 102
    }
}