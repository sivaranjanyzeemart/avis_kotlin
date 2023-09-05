package zeemart.asia.buyers.orders.createorders

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.LoaderInteface
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.CartListDraftOrderUI
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.OrderBaseResponse
import zeemart.asia.buyers.models.orderimport.PlaceDraftOrdersResponse
import zeemart.asia.buyers.models.orderimportimport.CartSelectedOrdersBreakdownListUI
import zeemart.asia.buyers.models.orderimportimport.DraftOrdersBySKUPaginated
import zeemart.asia.buyers.models.orderimportimport.ErrorModel
import zeemart.asia.buyers.network.OrderManagement
import java.util.*

class ActivityOrderSummaryPreview : AppCompatActivity(), LoaderInteface {
    private var txtDeliverTo: TextView? = null
    private var txtOutletName: TextView? = null
    private var imgCloseCart: ImageView? = null
    private var txtTotalAmountPayableValue: TextView? = null
    private var lytTotalAmountOpenBreakdown: RelativeLayout? = null
    private var btnPlaceOrder: Button? = null
    private var supplierList: List<DetailSupplierDataModel>? = null
    var supplierIdDetailMap: MutableMap<String, DetailSupplierDataModel>? = null
    private var lstDrafts: RecyclerView? = null
    private var lytCartTotalBreakDown: RelativeLayout? = null
    private var lytPlaceOrderCartTotal: RelativeLayout? = null
    private var lytAllOrdersPlaced: RelativeLayout? = null
    private var txtNoDrafts: TextView? = null
    private var txtGoToViewOrders: TextView? = null
    private var btnBackToHome: Button? = null
    private var cartLoadingView: CustomLoadingViewWhite? = null
    private var lytCreatingOrdersShadow: RelativeLayout? = null
    private var lytDialogPlaceOrderResponse: RelativeLayout? = null
    private var imgPlaceOrderSuccessError: ImageView? = null
    private var txtMsgErrorSuccess: TextView? = null
    private var lytLoaderCreatingOrder: LinearLayout? = null
    private var txtCreatingOrders: TextView? = null
    private var imgViewCartBreakdown: ImageView? = null
    private var txtOrderSummaryTag: TextView? = null
    private var imgErrorOrWarningInOrderSummary: ImageView? = null
    private var lstBreakdownSupplierAndTotal: RecyclerView? = null
    private var txtBreakdownTotalAmountTag: TextView? = null
    private var txtBreakdownTotalAmountValue: TextView? = null
    private var btnBreakdownPlaceOrders: Button? = null

    //for basic users handling
    var lytBindTotalAndWarning: LinearLayout? = null
    var imgWarningIconBasicUser: ImageView? = null
    var lytTotalAmountBreakdownView: RelativeLayout? = null
    private var draftOrdersBySKUSList: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary_preview)
        if (!StringHelper.isStringNullOrEmpty(
                SharedPref.read(
                    ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                    ""
                )
            )
        ) {
            val json: String = SharedPref.read(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, "")!!
            supplierList =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<DetailSupplierDataModel>>(
                    json,
                    object : TypeToken<List<DetailSupplierDataModel?>?>() {}.type
                )
            supplierIdDetailMap = HashMap<String, DetailSupplierDataModel>()
            for (i in supplierList!!.indices) {
                supplierIdDetailMap!![supplierList!![i].supplier.supplierId] = supplierList!![i]
            }
        }
        if (getIntent().getExtras() != null) {
            val cardDraftListJson: String =
                getIntent().getExtras()?.getString(ZeemartAppConstants.CART_DRAFT_LIST).toString()
            if (!StringHelper.isStringNullOrEmpty(cardDraftListJson)) {
                val draftOrdersBySKUPaginated: DraftOrdersBySKUPaginated? = ZeemartBuyerApp.fromJson(
                    cardDraftListJson,
                    DraftOrdersBySKUPaginated::class.java
                )
                draftOrdersBySKUSList = draftOrdersBySKUPaginated?.data as MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?
                for (i in draftOrdersBySKUSList!!.indices) {
                    if (draftOrdersBySKUSList!![i].errors != null && draftOrdersBySKUSList!![i].errors
                            ?.size!! > 0
                    ) {
                        draftOrdersBySKUSList!![i].orders.isAllow
                        for (j in 0 until draftOrdersBySKUSList!![i].errors?.size!!) {
                            if (draftOrdersBySKUSList!![i].errors?.get(j)
                                    ?.isErrorCode(ErrorModel.ErrorCode.REQUEST_VALIDATION_ERROR)!!
                            ) {
                                draftOrdersBySKUSList!![i].orders.isErrorBelowMoq
                                break
                            }
                        }
                    } else {
                        draftOrdersBySKUSList!![i].orders.isAllow
                        draftOrdersBySKUSList!![i].orders.isErrorBelowMoq
                    }
                }
            }
        }
        inflateViewsSetFont()
        lytPlaceOrderCartTotal?.setVisibility(View.GONE)
        /*lytPlaceOrderButton.setVisibility(View.GONE);
        lytReviewCartTotal.setVisibility(View.GONE);*/txtOutletName?.setText(SharedPref.defaultOutlet?.outletName)
        lytTotalAmountOpenBreakdown?.setOnClickListener(View.OnClickListener { expandOrderAmountBreakDown() })
        lytTotalAmountOpenBreakdown?.setClickable(false)
        //hide the detail total break down layout when visible and user clicks outside the layout to hide it
        lytCartTotalBreakDown?.setOnClickListener(View.OnClickListener {
            if (lytCartTotalBreakDown?.getVisibility() == View.VISIBLE) {
                lytCartTotalBreakDown!!.setVisibility(View.GONE)
                lytPlaceOrderCartTotal?.setVisibility(View.VISIBLE)
            }
        })
        btnPlaceOrder!!.setOnClickListener { callPlaceOrders() }
        btnBreakdownPlaceOrders!!.setOnClickListener { callPlaceOrders() }
        btnBackToHome!!.setOnClickListener {
            val newIntent =
                Intent(this@ActivityOrderSummaryPreview, BaseNavigationActivity::class.java)
            newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(newIntent)
        }
        imgCloseCart!!.setOnClickListener { finish() }
        lytCreatingOrdersShadow?.setOnClickListener(View.OnClickListener {
            //catches the click event
        })
        if (draftOrdersBySKUSList != null && draftOrdersBySKUSList!!.size > 0) {
            lytPlaceOrderCartTotal?.setVisibility(View.VISIBLE)
            lytAllOrdersPlaced?.setVisibility(View.GONE)
            //create the display data list for cart
            setAdapterForDraftList(draftOrdersBySKUSList)
        } else {
            lytAllOrdersPlaced?.setVisibility(View.VISIBLE)
            if (SharedPref.readInt(
                    SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                    0
                )!! > 20
            ) CommonMethods.askAppReview(this@ActivityOrderSummaryPreview)
        }
    }

    /**
     * on click of total cart amount
     */
    fun expandOrderAmountBreakDown() {
        if (lytCartTotalBreakDown?.getVisibility() == View.VISIBLE) {
            lytCartTotalBreakDown?.setVisibility(View.GONE)
            lytPlaceOrderCartTotal?.setVisibility(View.VISIBLE)
        } else {
            lytCartTotalBreakDown?.setVisibility(View.VISIBLE)
            lytPlaceOrderCartTotal?.setVisibility(View.GONE)
            setPlaceOrderUI(draftOrdersBySKUSList)
        }
    }

    /**
     * call the place orders API
     */
    private fun callPlaceOrders() {
        val ordersIds: MutableList<String> = ArrayList()
        for (i in draftOrdersBySKUSList!!.indices) {
            if (draftOrdersBySKUSList!![i].orders.isOrderSelected) {
                ordersIds.add(draftOrdersBySKUSList!![i].orders.orderId!!)
            }
        }
        if (ordersIds.size > 0) {
            val builder = AlertDialog.Builder(this@ActivityOrderSummaryPreview)
            val dialog: AlertDialog
            builder.setPositiveButton(
                this@ActivityOrderSummaryPreview.getResources().getString(R.string.txt_yes),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.dismiss()
                        for (i in draftOrdersBySKUSList!!.indices) {
                            if (draftOrdersBySKUSList!![i].orders.isOrderSelected) {
                                AnalyticsHelper.logAction(
                                    this@ActivityOrderSummaryPreview,
                                    AnalyticsHelper.TAP_ORDER_REVIEW_PLACE_ORDER,
                                    draftOrdersBySKUSList!![i].orders
                                )
                            }
                        }
                        lytCreatingOrdersShadow?.setVisibility(View.VISIBLE)
                        lytLoaderCreatingOrder?.setVisibility(View.VISIBLE)
                        OrderManagement.placeDraftedOrdersAfterReview(
                            this@ActivityOrderSummaryPreview,
                            ordersIds,
                            object : OrderManagement.PlaceDraftedOrdersAfterReviewListener {
                                override fun onSuccessResponse(placeDraftOrdersResponse: List<PlaceDraftOrdersResponse.OrdersData?>?) {
                                    if (placeDraftOrdersResponse != null && placeDraftOrdersResponse.size > 0) {
                                        lytLoaderCreatingOrder?.setVisibility(View.GONE)
                                        val orderWithFailureStatus: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU> =
                                            ArrayList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>()
                                        var totalSuccessfulOrders = 0

                                        /**
                                         * check for the current draft list and
                                         * update the order status for orders which were sent to be placed
                                         */
                                        val iterator: MutableIterator<DraftOrdersBySKUPaginated.DraftOrdersBySKU> =
                                            draftOrdersBySKUSList!!.iterator()
                                        while (iterator.hasNext()) {
                                            val draftOrder: DraftOrdersBySKUPaginated.DraftOrdersBySKU = iterator.next()
                                            for (j in placeDraftOrdersResponse.indices) {
                                                if (draftOrder.orders.orderId.equals(
                                                        placeDraftOrdersResponse[j]?.data!!
                                                            .orderId!!
                                                    )
                                                ) {
                                                    if (placeDraftOrdersResponse[j]?.isStatus(
                                                            PlaceDraftOrdersResponse.OrdersData.Status.SUCCESS
                                                        )!!
                                                    ) {
                                                        //remove the order from the list which has been successfully placed
                                                        totalSuccessfulOrders =
                                                            totalSuccessfulOrders + 1
                                                        iterator.remove()
                                                    } else if (placeDraftOrdersResponse[j]?.isStatus(
                                                            PlaceDraftOrdersResponse.OrdersData.Status.FAILURE
                                                        )!!
                                                    ) {
                                                        if (placeDraftOrdersResponse[j]?.errors != null && placeDraftOrdersResponse[j]?.errors
                                                                ?.size!! > 0
                                                        ) {
                                                            for (i in 0 until placeDraftOrdersResponse[j]?.errors!!
                                                                .size) {
                                                                //if order has been placed with another device or deleted
                                                                if (placeDraftOrdersResponse[j]?.errors!!
                                                                        .get(i).isErrorCode(
                                                                        PlaceDraftOrdersResponse.Error.ErrorCode.ENTRY_NOT_FOUND_ERROR
                                                                    )
                                                                ) {
                                                                    draftOrder.orders
                                                                        .errorMessageFromPlaceDraftsResponse = (
                                                                            placeDraftOrdersResponse[j]?.errors
                                                                                !!.get(i).message
                                                                        )
                                                                    draftOrder.orders
                                                                        .isAllow
                                                                    draftOrder.orders
                                                                        .isErrorBelowMoq
                                                                    //flag to display delete option
                                                                    draftOrder.orders
                                                                        .isDeleteOrder
                                                                    orderWithFailureStatus.add(
                                                                        draftOrder
                                                                    )
                                                                } else if (placeDraftOrdersResponse[j]?.errors
                                                                        !!.get(i).isErrorCode(
                                                                        PlaceDraftOrdersResponse.Error.ErrorCode.REQUEST_VALIDATION_ERROR
                                                                    )
                                                                ) {
                                                                    //if order failed due to some other problem like promo expired
                                                                    draftOrder.orders
                                                                        .errorMessageFromPlaceDraftsResponse = (
                                                                            placeDraftOrdersResponse[j]?.errors
                                                                                !!.get(i).message
                                                                        )
                                                                    draftOrder.orders
                                                                        .isAllow
                                                                    draftOrder.orders
                                                                        .isErrorBelowMoq
                                                                    orderWithFailureStatus.add(
                                                                        draftOrder
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        // update the orders with Failure status in the draft order list
                                        if (draftOrdersBySKUSList != null && draftOrdersBySKUSList!!.size > 0) {
                                            if (orderWithFailureStatus.size > 0) {
                                                for (i in draftOrdersBySKUSList!!.indices) {
                                                    draftOrdersBySKUSList!![i].orders
                                                        .isOrderSelected
                                                    for (j in orderWithFailureStatus.indices) {
                                                        if (draftOrdersBySKUSList!![i].orders
                                                                .orderId.equals(
                                                                orderWithFailureStatus[j].orders
                                                                    .orderId
                                                            )
                                                        ) {
                                                            draftOrdersBySKUSList!![i] =
                                                                orderWithFailureStatus[j]
                                                            break
                                                        }
                                                    }
                                                }
                                            }
                                            if (lstDrafts?.getAdapter() != null) {
                                                setAdapterForDraftList(draftOrdersBySKUSList)
                                            }
                                            if (totalSuccessfulOrders == 0 && orderWithFailureStatus.size > 0) {
                                                //none of the order could be successfully placed
                                                displayOrderPlaceMessageView(
                                                    R.drawable.warning_order,
                                                    this@ActivityOrderSummaryPreview.getString(
                                                        R.string.txt_your_order_cannot_be_created
                                                    ),
                                                    false
                                                )
                                            } else if (totalSuccessfulOrders > 0 && orderWithFailureStatus.size > 0) {
                                                //some successfully placed
                                                val totalOrderSentForPlacing =
                                                    totalSuccessfulOrders + orderWithFailureStatus.size
                                                if (totalOrderSentForPlacing == 1) {
                                                    displayOrderPlaceMessageView(
                                                        R.drawable.tick_receive_big,
                                                        this@ActivityOrderSummaryPreview.getString(
                                                            R.string.txt_no_of_order_created_successfully,
                                                            totalSuccessfulOrders,
                                                            totalOrderSentForPlacing
                                                        ),
                                                        false
                                                    )
                                                } else {
                                                    displayOrderPlaceMessageView(
                                                        R.drawable.tick_receive_big,
                                                        this@ActivityOrderSummaryPreview.getString(
                                                            R.string.txt_no_of_orders_created_successfully,
                                                            totalSuccessfulOrders,
                                                            totalOrderSentForPlacing
                                                        ),
                                                        false
                                                    )
                                                }
                                            } else {
                                                //all successfully placed
                                                if (totalSuccessfulOrders == 1) {
                                                    displayOrderPlaceMessageView(
                                                        R.drawable.tick_receive_big,
                                                        this@ActivityOrderSummaryPreview.getString(
                                                            R.string.txt_number_of_order_created,
                                                            totalSuccessfulOrders
                                                        ),
                                                        false
                                                    )
                                                } else {
                                                    displayOrderPlaceMessageView(
                                                        R.drawable.tick_receive_big,
                                                        this@ActivityOrderSummaryPreview.getString(
                                                            R.string.txt_number_of_orders_created,
                                                            totalSuccessfulOrders
                                                        ),
                                                        false
                                                    )
                                                }
                                            }
                                            lytPlaceOrderCartTotal?.setVisibility(View.VISIBLE)
                                            setPlaceOrderUI(draftOrdersBySKUSList)
                                        } else {
                                            //all orders have been placed
                                            if (totalSuccessfulOrders == 1) {
                                                displayOrderPlaceMessageView(
                                                    R.drawable.tick_receive_big,
                                                    this@ActivityOrderSummaryPreview.getString(
                                                        R.string.txt_number_of_order_created,
                                                        totalSuccessfulOrders
                                                    ),
                                                    true
                                                )
                                            } else {
                                                displayOrderPlaceMessageView(
                                                    R.drawable.tick_receive_big,
                                                    this@ActivityOrderSummaryPreview.getString(
                                                        R.string.txt_number_of_orders_created,
                                                        totalSuccessfulOrders
                                                    ),
                                                    true
                                                )
                                            }
                                        }
                                        val totalSuccessfulOrdersPlaced: Int = SharedPref.readInt(
                                            SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                            0
                                        )!!
                                        SharedPref.writeInt(
                                            SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                            totalSuccessfulOrdersPlaced + totalSuccessfulOrders
                                        )
                                    }
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    if (error != null && error.message != null) {
                                        displayOrderPlaceMessageView(
                                            R.drawable.warning_order,
                                            error.message!!,
                                            false
                                        )
                                    }
                                    for (i in draftOrdersBySKUSList!!.indices) {
                                        draftOrdersBySKUSList!![i].orders
                                            .isOrderSelected
                                    }
                                    //lytCreatingOrder.setVisibility(View.GONE);
                                    //lytCreatingOrdersShadow.setVisibility(View.GONE);
                                    if (lstDrafts?.getAdapter() != null) {
                                        setAdapterForDraftList(draftOrdersBySKUSList)
                                    }
                                    /*lytPlaceOrderButton.setVisibility(View.VISIBLE);*/lytPlaceOrderCartTotal?.setVisibility(
                                        View.VISIBLE
                                    )
                                    setPlaceOrderUI(draftOrdersBySKUSList)
                                }
                            })
                    }
                })
            builder.setNegativeButton(
                this@ActivityOrderSummaryPreview.getResources().getString(R.string.txt_cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.dismiss()
                    }
                })
            dialog = builder.create()
            if (ordersIds.size > 1) {
                dialog.setTitle(
                    String.format(
                        getResources().getString(R.string.txt_place_order_to_suppliers),
                        ordersIds.size
                    )
                )
            } else {
                dialog.setTitle(
                    String.format(
                        getResources().getString(R.string.txt_place_order_to_supplier),
                        ordersIds.size
                    )
                )
            }
            dialog.show()
        }
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val cardDraftListJson: String =
                data?.getExtras()?.getString(ZeemartAppConstants.CART_DRAFT_LIST)!!
            val draftOrdersBySKUPaginated: OrderBaseResponse.Data =
                ZeemartBuyerApp.fromJson(cardDraftListJson, OrderBaseResponse.Data::class.java)!!
            val updatedOrder: Orders = draftOrdersBySKUPaginated.data!!
            for (i in draftOrdersBySKUSList!!.indices) {
                if (draftOrdersBySKUSList!![i].orders.orderId
                        .equals(updatedOrder.orderId)
                ) {
                    if (draftOrdersBySKUSList!![i].orders.isOrderSelected) {
                        updatedOrder.isOrderSelected = true
                    }
                    draftOrdersBySKUSList!![i].errors = (draftOrdersBySKUPaginated.errors)
                    //                    updatedOrder.setAddOn(draftOrdersBySKUSList.get(i).getOrders().isAddOn());
                    draftOrdersBySKUSList!![i].orders = (updatedOrder)
                    if (draftOrdersBySKUPaginated.errors!= null && draftOrdersBySKUPaginated.errors!!
                            .size > 0
                    ) {
                        draftOrdersBySKUSList!![i].orders.isAllow
                        for (j in 0 until draftOrdersBySKUSList!![i].errors?.size!!) {
                            if (draftOrdersBySKUSList!![i].errors?.get(j)
                                    ?.isErrorCode(ErrorModel.ErrorCode.REQUEST_VALIDATION_ERROR)!!
                            ) {
                                draftOrdersBySKUSList!![i].orders.isErrorBelowMoq
                                break
                            }
                        }
                    } else {
                        draftOrdersBySKUSList!![i].orders.isAllow
                        draftOrdersBySKUSList!![i].orders.isErrorBelowMoq
                    }
                }
            }
            setAdapterForDraftList(draftOrdersBySKUSList)
        }
    }

    private fun inflateViewsSetFont() {
        txtDeliverTo = findViewById<TextView>(R.id.txt_deliverTo_review_cart)
        txtOutletName = findViewById<TextView>(R.id.txt_deliver_to_outlet_name_review_cart)
        imgCloseCart = findViewById<ImageView>(R.id.btn_close_review_cart)
        txtTotalAmountPayableValue = findViewById<TextView>(R.id.txt_cart_total)
        lytTotalAmountOpenBreakdown = findViewById<RelativeLayout>(R.id.lyt_view_breakdown_summary)
        btnPlaceOrder = findViewById<Button>(R.id.btn_review_cart_place_order)
        lstDrafts = findViewById<RecyclerView>(R.id.lst_draft)
        lytCartTotalBreakDown = findViewById<RelativeLayout>(R.id.lyt_cart_total_breakdown)
        lytPlaceOrderCartTotal =
            findViewById<RelativeLayout>(R.id.lyt_review_cart_total_place_order)
        txtNoDrafts = findViewById<TextView>(R.id.txt_no_drafts)
        txtGoToViewOrders = findViewById<TextView>(R.id.txt_goto_view_orders)
        btnBackToHome = findViewById<Button>(R.id.btn_back_to_home)
        lytAllOrdersPlaced = findViewById<RelativeLayout>(R.id.lyt_all_orders_placed)
        cartLoadingView = findViewById<CustomLoadingViewWhite>(R.id.lyt_loading_overlay)
        lytCreatingOrdersShadow = findViewById<RelativeLayout>(R.id.lyt_creating_orders_shadow)
        txtOrderSummaryTag = findViewById<TextView>(R.id.txt_order_summary_tag)
        imgViewCartBreakdown = findViewById<ImageView>(R.id.img_arrow_view_breakdown)
        imgErrorOrWarningInOrderSummary = findViewById<ImageView>(R.id.img_draft_error)
        txtBreakdownTotalAmountTag =
            findViewById<TextView>(R.id.txt_review_cart_estimated_total_tag)
        txtBreakdownTotalAmountValue =
            findViewById<TextView>(R.id.txt_review_cart_estimated_total_val)
        btnBreakdownPlaceOrders = findViewById<Button>(R.id.btn_review_cart_place_order_breakdown)
        lstBreakdownSupplierAndTotal = findViewById<RecyclerView>(R.id.lst_supplier_and_total)
        lytDialogPlaceOrderResponse =
            findViewById<RelativeLayout>(R.id.lyt_dialog_place_order_response)
        imgPlaceOrderSuccessError =
            findViewById<ImageView>(R.id.img_icon_order_place_success_failure)
        txtMsgErrorSuccess = findViewById<TextView>(R.id.txt_order_place_success_failure_msg)
        lytLoaderCreatingOrder = findViewById<LinearLayout>(R.id.lyt_loader_creating_orders)
        txtCreatingOrders = findViewById<TextView>(R.id.txt_creating_orders)
        lytBindTotalAndWarning = findViewById<LinearLayout>(R.id.lyt_bind_total_and_warning)
        imgWarningIconBasicUser = findViewById<ImageView>(R.id.img_warning_basic_user)
        lytTotalAmountBreakdownView =
            findViewById<RelativeLayout>(R.id.lyt_total_amount_breakdown_view)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOutletName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalAmountPayableValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnPlaceOrder,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoDrafts,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtGoToViewOrders,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnBackToHome,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderSummaryTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtBreakdownTotalAmountTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtBreakdownTotalAmountValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnBreakdownPlaceOrders,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtMsgErrorSuccess,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCreatingOrders,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    /**
     * set or update the adapter for draft order list and update the Place order UI
     *
     * @param cartOrderList
     */
    private fun setAdapterForDraftList(cartOrderList: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?) {
        if (lstDrafts?.getAdapter() == null) {
            lstDrafts?.setLayoutManager(LinearLayoutManager(this@ActivityOrderSummaryPreview))
            val draftListAdapter = SummaryPreviewDraftOrdersAdapter(
                this@ActivityOrderSummaryPreview,
                cartOrderList,
                supplierIdDetailMap,
                object : SummaryPreviewDraftOrdersAdapter.UpdateUiListener {
                    override fun updatePlaceOrderButton(orders: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?) {
                        draftOrdersBySKUSList = orders
                        if (draftOrdersBySKUSList != null && draftOrdersBySKUSList!!.size == 0) {
                            lytAllOrdersPlaced?.setVisibility(View.VISIBLE)
                            if (SharedPref.readInt(
                                    SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                    0
                                )!! > 20
                            ) CommonMethods.askAppReview(this@ActivityOrderSummaryPreview)
                        }
                        setPlaceOrderUI(orders)
                    }

                    override fun showLoadingOverlay(isShowing: Boolean) {
                        if (isShowing) {
                            displayLoader(cartLoadingView)
                        } else {
                            hideLoader(cartLoadingView)
                        }
                    }
                })
            lstDrafts?.setAdapter(draftListAdapter)
            lstDrafts!!.addItemDecoration(HeaderItemDecoration(lstDrafts!!, draftListAdapter as HeaderItemDecoration.StickyHeaderInterface))
            initDraftOrderProductListSwipe()
        } else {
            (lstDrafts!!.getAdapter() as SummaryPreviewDraftOrdersAdapter).updateDataList(
                draftOrdersBySKUSList
            )
        }
        setPlaceOrderUI(draftOrdersBySKUSList)
    }

    private fun setPlaceOrderUI(cartOrderList: List<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?) {
        var isWarning = false
        var isError = false
        var isBlockMinimumOrder = false
        var orderSelectedCount = 0
        var cartTotal = 0.0
        if (!UserPermission.HasViewPrice()) {
            lytTotalAmountBreakdownView?.setVisibility(View.GONE)
        } else {
            lytTotalAmountBreakdownView?.setVisibility(View.VISIBLE)
        }
        if (cartOrderList != null && cartOrderList.size == 0) {
            lytAllOrdersPlaced?.setVisibility(View.VISIBLE)
            if (SharedPref.readInt(
                    SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                    0
                )!! > 20
            ) CommonMethods.askAppReview(this@ActivityOrderSummaryPreview)
        } else {
            lytAllOrdersPlaced?.setVisibility(View.GONE)
            for (i in cartOrderList!!.indices) {
                if (cartOrderList[i].orders.isOrderSelected) {
                    orderSelectedCount = orderSelectedCount + 1
                    val supplierDataModel: DetailSupplierDataModel? =
                        supplierIdDetailMap!![cartOrderList[i].orders.supplier
                            ?.supplierId]
                    val totalAmount = PriceDetails()
                    if (cartOrderList[i].orders.isAddOn) {
                        isError = false
                        isBlockMinimumOrder = false
                        isWarning = false
                        if (cartOrderList[i].orders.amount?.discount
                                ?.amount !== 0.0
                        ) {
                            val orderSubTotalAfterDiscount: Double =
                                cartOrderList[i].orders.amount?.subTotal
                                    ?.amount?.minus(cartOrderList[i].orders.amount
                                    ?.discount?.amount!!)!!
                            val subTotalAfterDiscount = PriceDetails(orderSubTotalAfterDiscount)
                            totalAmount.amount =
                                orderSubTotalAfterDiscount + subTotalAfterDiscount.getAmount(
                                    supplierDataModel?.supplier?.settings?.gst!!
                                )
                        } else {
                            totalAmount.amount =
                                cartOrderList[i].orders.amount?.subTotal
                                    ?.amount?.plus(cartOrderList[i].orders.amount
                                    ?.subTotal
                                    ?.getAmount(supplierDataModel?.supplier?.settings?.gst!!)!!)
                        }
                        if (cartOrderList[i].errors != null && cartOrderList[i].errors
                                !!.size > 0
                        ) {
                            isError = true
                        }
                    } else {
                        if (supplierDataModel != null && supplierDataModel.deliveryFeePolicy != null && supplierDataModel.deliveryFeePolicy!!.minOrder != null && cartOrderList[i].orders
                                .amount?.subTotal
                                ?.amount!! < supplierDataModel.deliveryFeePolicy!!.minOrder!!.amount!!
                        ) {
                            //display warning icon and text in yellow
                            isWarning = true
                        }
                        if (supplierDataModel != null && supplierDataModel.deliveryFeePolicy != null &&
                            supplierDataModel.deliveryFeePolicy!!.isBlockBelowMinOrder && cartOrderList[i].orders
                                .amount?.subTotal
                                ?.amount!! < supplierDataModel.deliveryFeePolicy!!.minOrder?.amount!!
                        ) {
                            isBlockMinimumOrder = true
                        }
                        if (cartOrderList[i].errors != null && cartOrderList[i].errors
                                !!.size > 0
                        ) {
                            isError = true
                        }
                        totalAmount.amount =
                            cartOrderList[i].orders.amount?.total?.amount
                    }
                    cartTotal = cartTotal + totalAmount.amount!!
                }
            }
            if (!UserPermission.HasViewPrice()) {
                lytBindTotalAndWarning?.setVisibility(View.GONE)
            } else {
                lytBindTotalAndWarning?.setVisibility(View.VISIBLE)
            }
            if (orderSelectedCount == 0) {
                imgWarningIconBasicUser!!.visibility = View.GONE
                imgErrorOrWarningInOrderSummary!!.visibility = View.GONE
                btnPlaceOrder!!.background =
                    getResources().getDrawable(R.drawable.dark_grey_rounded_corner)
                btnPlaceOrder!!.setText(getString(R.string.txt_yes_place_order))
                btnPlaceOrder!!.isClickable = false
                txtOrderSummaryTag?.setText(getString(R.string.txt_no_orders_selected))
                imgViewCartBreakdown!!.visibility = View.GONE
                txtTotalAmountPayableValue?.setText(PriceDetails().displayValue)
                lytTotalAmountOpenBreakdown?.setClickable(false)
            } else {
                lytTotalAmountOpenBreakdown?.setClickable(true)
                txtOrderSummaryTag?.setText(getString(R.string.txt_order_summary))
                imgViewCartBreakdown!!.visibility = View.VISIBLE
                if (orderSelectedCount == 1) {
                    btnPlaceOrder!!.text = String.format(
                        getString(R.string.txt_place_order_supplier_selected),
                        orderSelectedCount
                    )
                    btnBreakdownPlaceOrders!!.text = String.format(
                        getString(R.string.txt_place_order_supplier_selected),
                        orderSelectedCount
                    )
                    txtBreakdownTotalAmountTag?.setText(
                        String.format(
                            getString(R.string.txt_total_order),
                            orderSelectedCount
                        )
                    )
                } else {
                    btnPlaceOrder!!.text = String.format(
                        getString(R.string.txt_place_order_suppliers_selected),
                        orderSelectedCount
                    )
                    btnBreakdownPlaceOrders!!.text = String.format(
                        getString(R.string.txt_place_order_suppliers_selected),
                        orderSelectedCount
                    )
                    txtBreakdownTotalAmountTag?.setText(
                        String.format(
                            getString(R.string.txt_total_orders),
                            orderSelectedCount
                        )
                    )
                }
                if (isError || isWarning) {
                    imgErrorOrWarningInOrderSummary!!.visibility = View.VISIBLE
                    if (isError) {
                        if (!UserPermission.HasViewPrice()) {
                            imgWarningIconBasicUser!!.visibility = View.VISIBLE
                            imgWarningIconBasicUser!!.setImageResource(R.drawable.warning_red)
                        } else {
                            imgWarningIconBasicUser!!.visibility = View.GONE
                        }
                        imgErrorOrWarningInOrderSummary!!.setImageResource(R.drawable.warning_red)
                        btnPlaceOrder!!.background =
                            getResources().getDrawable(R.drawable.dark_grey_rounded_corner)
                        btnPlaceOrder!!.isClickable = false
                        btnBreakdownPlaceOrders!!.background =
                            getResources().getDrawable(R.drawable.dark_grey_rounded_corner)
                        btnBreakdownPlaceOrders!!.isClickable = false
                    } else {
                        imgWarningIconBasicUser!!.visibility = View.GONE
                        imgErrorOrWarningInOrderSummary!!.setImageResource(R.drawable.warning_yellow_black)
                        btnPlaceOrder!!.background =
                            getResources().getDrawable(R.drawable.green_round_corner)
                        btnPlaceOrder!!.isClickable = true
                        btnBreakdownPlaceOrders!!.background =
                            getResources().getDrawable(R.drawable.green_round_corner)
                        btnBreakdownPlaceOrders!!.isClickable = true
                    }
                } else {
                    imgWarningIconBasicUser!!.visibility = View.GONE
                    imgErrorOrWarningInOrderSummary!!.visibility = View.GONE
                    btnPlaceOrder!!.background =
                        getResources().getDrawable(R.drawable.green_round_corner)
                    btnPlaceOrder!!.isClickable = true
                    btnBreakdownPlaceOrders!!.background =
                        getResources().getDrawable(R.drawable.green_round_corner)
                    btnBreakdownPlaceOrders!!.isClickable = true
                }
                if (isBlockMinimumOrder) {
                    if (!UserPermission.HasViewPrice()) {
                        imgWarningIconBasicUser!!.visibility = View.VISIBLE
                        imgWarningIconBasicUser!!.setImageResource(R.drawable.warning_red)
                    } else {
                        imgWarningIconBasicUser!!.visibility = View.GONE
                    }
                    imgErrorOrWarningInOrderSummary!!.setImageResource(R.drawable.warning_red)
                    btnPlaceOrder!!.background =
                        getResources().getDrawable(R.drawable.dark_grey_rounded_corner)
                    btnPlaceOrder!!.isClickable = false
                    btnBreakdownPlaceOrders!!.background =
                        getResources().getDrawable(R.drawable.dark_grey_rounded_corner)
                    btnBreakdownPlaceOrders!!.isClickable = false
                }
                val totalAmount = PriceDetails()
                totalAmount.amount = cartTotal
                txtTotalAmountPayableValue?.setText(totalAmount.displayValue)
                txtBreakdownTotalAmountValue?.setText(totalAmount.displayValue)
                //create list with delivery date header for the supplier
                val lstSupplierWithHeaders: List<CartSelectedOrdersBreakdownListUI> =
                    getListWithDeliveryDateHeaders(cartOrderList)
                lstBreakdownSupplierAndTotal?.setLayoutManager(LinearLayoutManager(this@ActivityOrderSummaryPreview))
                lstBreakdownSupplierAndTotal?.setAdapter(
                    SupplierAndDeliveryDateListAdapter(
                        this@ActivityOrderSummaryPreview,
                        lstSupplierWithHeaders,
                        supplierIdDetailMap
                    )
                )
            }
        }
    }

    private fun getListWithDeliveryDateHeaders(cartOrderList: List<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?): List<CartSelectedOrdersBreakdownListUI> {
        val selectedOrders: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU> = ArrayList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>()
        val lstSupplierData: MutableList<CartSelectedOrdersBreakdownListUI> =
            ArrayList<CartSelectedOrdersBreakdownListUI>()
        for (i in cartOrderList!!.indices) {
            if (cartOrderList[i].orders.isOrderSelected) {
                selectedOrders.add(cartOrderList[i])
            }
        }
        if (selectedOrders.size > 0) {
            Collections.sort(selectedOrders, CartListDraftOrderUI.OrderDeliveryDateCompare())
            for (i in selectedOrders.indices) {
                if (lstSupplierData.size == 0) {
                    val deliveryDateHeader = CartSelectedOrdersBreakdownListUI()
                    deliveryDateHeader.isDeliveryDateHeader
                    val deliveryDate: String = DateHelper.getDateInDayDateMonthYearFormat(
                        selectedOrders[i].orders.timeDelivered!!
                    )
                    deliveryDateHeader.deliveryDate = (deliveryDate)
                    lstSupplierData.add(deliveryDateHeader)
                    val supplierData = CartSelectedOrdersBreakdownListUI()
                    supplierData.isSupplierRow
                    supplierData.order = (selectedOrders[i].orders)
                    supplierData.deliveryDate = (deliveryDate)
                    lstSupplierData.add(supplierData)
                } else {
                    val currentOrderDeliveryDate: String =
                        DateHelper.getDateInDayDateMonthYearFormat(
                            selectedOrders[i].orders.timeDelivered!!
                        )
                    val previousDeliveryDate: String =
                        lstSupplierData[lstSupplierData.size - 1].deliveryDate!!
                    if (currentOrderDeliveryDate == previousDeliveryDate) {
                        val supplierData = CartSelectedOrdersBreakdownListUI()
                        supplierData.isSupplierRow
                        supplierData.order = (selectedOrders[i].orders)
                        supplierData.deliveryDate = (currentOrderDeliveryDate)
                        lstSupplierData.add(supplierData)
                    } else {
                        val deliveryDateHeader = CartSelectedOrdersBreakdownListUI()
                        deliveryDateHeader.isDeliveryDateHeader
                        val deliveryDate: String = DateHelper.getDateInDayDateMonthYearFormat(
                            selectedOrders[i].orders.timeDelivered!!
                        )
                        deliveryDateHeader.deliveryDate = (deliveryDate)
                        lstSupplierData.add(deliveryDateHeader)
                        val supplierData = CartSelectedOrdersBreakdownListUI()
                        supplierData.isSupplierRow
                        supplierData.order = (selectedOrders[i].orders)
                        supplierData.deliveryDate = (deliveryDate)
                        lstSupplierData.add(supplierData)
                    }
                }
            }
        }
        return lstSupplierData
    }

    private fun displayOrderPlaceMessageView(
        backgroundResource: Int, message: String,
        isAllOrdersPlaced: Boolean,
    ) {
        lytCartTotalBreakDown?.setVisibility(View.GONE)
        lytDialogPlaceOrderResponse?.setVisibility(View.VISIBLE)
        imgPlaceOrderSuccessError!!.setImageResource(backgroundResource)
        txtMsgErrorSuccess?.setText(message)
        Handler().postDelayed({
            if (lytCreatingOrdersShadow != null && lytDialogPlaceOrderResponse != null) {
                lytDialogPlaceOrderResponse!!.setVisibility(View.GONE)
                lytCreatingOrdersShadow!!.setVisibility(View.GONE)
                if (isAllOrdersPlaced) {
                    if (lstDrafts?.getAdapter() != null) {
                        setAdapterForDraftList(draftOrdersBySKUSList)
                    }
                    lytAllOrdersPlaced?.setVisibility(View.VISIBLE)
                    if (SharedPref.readInt(
                            SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                            0
                        )!! > 20
                    ) CommonMethods.askAppReview(this@ActivityOrderSummaryPreview)
                }
            }
        }, 1000)
    }

    override fun displayLoader(view: View?) {
        if (view != null) {
            view.visibility = View.VISIBLE
        }
    }

    override fun hideLoader(view: View?) {
        if (view != null) {
            view.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initDraftOrderProductListSwipe() {
        val orderListingSwipehelper: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val background: ColorDrawable = ColorDrawable()
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val mAdapter = lstDrafts?.getAdapter() as SummaryPreviewDraftOrdersAdapter
                if (mAdapter != null) {
                    val swipeDeleteRowData: CartListDraftOrderUI? =
                        mAdapter.getSwipeDeleteRowData(viewHolder.getAdapterPosition())
                    if (swipeDeleteRowData?.isDraftProduct!! && direction == ItemTouchHelper.LEFT) {
                        mAdapter.removeAt(viewHolder.getAdapterPosition())
                    }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                return if (viewHolder is SummaryPreviewDraftOrdersAdapter.ViewHolderProduct) {
                    ItemTouchHelper.LEFT
                } else {
                    0
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {
                if (viewHolder is SummaryPreviewDraftOrdersAdapter.ViewHolderProduct) {
                    val mViewHolder: SummaryPreviewDraftOrdersAdapter.ViewHolderProduct? = viewHolder as SummaryPreviewDraftOrdersAdapter.ViewHolderProduct?
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX <= 0) // Swiping Left
                    {
                        val bmp: Bitmap = BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.trash_white_small
                        )
                        drawLeftSwipeButton(
                            background,
                            c,
                            dX,
                            mViewHolder!!,
                            getString(R.string.txt_delete),
                            bmp
                        )
                    }
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        val helperActionRequired = ItemTouchHelper(orderListingSwipehelper)
        helperActionRequired.attachToRecyclerView(lstDrafts)
    }

    /**
     * @param background
     * @param c
     * @param dX
     * @param mViewHolder
     * @param text
     * @param bmp
     */
    private fun drawLeftSwipeButton(
        background: ColorDrawable,
        c: Canvas,
        dX: Float,
        mViewHolder: SummaryPreviewDraftOrdersAdapter.ViewHolderProduct,
        text: String,
        bmp: Bitmap,
    ) {
        //Show reject button
        val color: Int = getResources().getColor(R.color.pinky_red)
        background.setColor(color)
        background.setBounds(
            mViewHolder.itemView.getRight() + dX.toInt(),
            mViewHolder.itemView.getTop(),
            mViewHolder.itemView.getRight(),
            mViewHolder.itemView.getBottom()
        )
        background.draw(c)
        val buttonWidth: Int =
            mViewHolder.itemView.getRight() - SummaryPreviewDraftOrdersAdapter.ViewHolderProduct.Companion.SWIPE_BUTTON_WIDTH
        val rightButton = RectF(
            buttonWidth.toFloat(),
            mViewHolder.itemView.getTop().toFloat(),
            mViewHolder.itemView.getRight().toFloat(),
            mViewHolder.itemView.getBottom().toFloat()
        )
        val bounds = Rect()
        val combinedHeight: Float = (bmp.getHeight() + bounds.height()).toFloat()
        c.drawBitmap(
            bmp,
            rightButton.centerX() - bmp.getWidth() / 2,
            rightButton.centerY() - combinedHeight / 2,
            null
        )
    }
}

operator fun <K, V> MutableMap<K, V>.set(supplierId: K?, value: V) {

}
