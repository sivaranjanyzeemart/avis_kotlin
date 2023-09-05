package zeemart.asia.buyers.orders

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.adapter.LinkedAddOnOrdersAdapter
import zeemart.asia.buyers.adapter.LinkedGrnOrdersAdapter
import zeemart.asia.buyers.adapter.LinkedInvoiceListAdapter
import zeemart.asia.buyers.adapter.OrderDetailProductListAdapter
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.deals.ReviewDealsActivity
import zeemart.asia.buyers.essentials.ReviewEssentialsActivity
import zeemart.asia.buyers.goodsReceivedNote.GoodsReceivedNoteDashBoardActivity
import zeemart.asia.buyers.goodsReceivedNote.GrnDetailsActivity
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.NotificationBroadCastReceiverHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.invoices.InvoiceDetailActivity
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.order.Orders.Grn
import zeemart.asia.buyers.models.order.StatusDetail
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.models.orderimport.RetrieveOrderDetails
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseListener
import zeemart.asia.buyers.network.GetOrderDetails.GetOrderDetailedDataListener
import zeemart.asia.buyers.network.MarketListApi.retrieveSupplierListNew
import zeemart.asia.buyers.network.OrderHelper.DealsResponseListener
import zeemart.asia.buyers.network.OutletsApi.GetSpecificOutletResponseListener
import zeemart.asia.buyers.orderPayments.OrderPaymentListActivity
import zeemart.asia.buyers.orders.createorders.ReviewOrderActivity
import zeemart.asia.buyers.orders.vieworders.ViewOrdersActivity

class OrderDetailsActivity() : AppCompatActivity(), View.OnClickListener {
    private lateinit var txtStatus: TextView
    private lateinit var txtOrderType: TextView
    private lateinit var txtRecurringOrderIcon: ImageView
    private lateinit var imgReceiptStatus: ImageView
    private lateinit var txtReceiptStatus: TextView
    private lateinit var txtSupplierName: TextView
    private lateinit var imgSupplierImage: ImageView
    private lateinit var lytSupplierThumbNail: RelativeLayout
    private lateinit var txtSupplierThumbNail: TextView
    private lateinit var txtOrderId: TextView
    private lateinit var txtDeliverTo: TextView
    private lateinit var txtDeliverOn: TextView
    private lateinit var txtCreatedBy: TextView
    private lateinit var txtNotes: TextView
    private lateinit var lstOrderItems: RecyclerView
    private lateinit var txtEstimatedSubtotal: TextView
    private lateinit var txtEstimatedGST: TextView
    private lateinit var txtDeliveryFee: TextView
    private lateinit var txtEstimatedTotal: TextView
    private lateinit var orderId: String
    private lateinit var outletId: String
    private lateinit var horizontalScrollViewSpReqDelInst: HorizontalScrollView
    private lateinit var lytNotes: RelativeLayout
    private lateinit var lytDeliveryInstructions: RelativeLayout
    private lateinit var txtDeliveryInstructions: TextView
    private lateinit var txtNoOfProducts: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnOptions: ImageView
    private lateinit var mOrder: Orders
    private lateinit var lytApproveRejectorders: LinearLayout

    //private Button btnRejectOrder;
    private lateinit var btnApproveOrder: RelativeLayout
    private lateinit var btnEdit: RelativeLayout
    private lateinit var crdDocumentsLayout: CardView
    private lateinit var txtDeliveryFeeTag: TextView
    private lateinit var imgOrderCreatedBy: ImageView
    private lateinit var lytOrderStatus: LinearLayout
    private lateinit var txtEstimatedSubtotalTag: TextView
    private lateinit var txtEstimatedTotalTag: TextView
    private lateinit var txtEstimatedGSTTag: TextView
    private lateinit var txtStatusTag: TextView
    private lateinit var txtDeliverToTag: TextView
    private lateinit var txtDeliverOnTag: TextView
    private lateinit var txtOrderDetailHeaderText: TextView
    private lateinit var txtSpecialRequestHeadingText: TextView
    private lateinit var txtDeliveryInstructionTag: TextView
    private lateinit var txtPricesCalNotFinalText: TextView
    private lateinit var btnContactSupplier: TextView
    private lateinit var txtOrderCreatedByTag: TextView
    private lateinit var txtApprove: TextView
    private lateinit var txtEdit: TextView
    private lateinit var txtRepeatOrder: TextView
    private lateinit var txtRecieveOrder: TextView
    private lateinit var lytRepeatReceive: LinearLayout
    private lateinit var lytRepeatOrder: RelativeLayout
    private lateinit var lytReceive: RelativeLayout
    private lateinit var imgReceiveTick: ImageView
    private var isStatusChanged = false
    private lateinit var notificationBroadCastReceiverHelper: NotificationBroadCastReceiverHelper
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    private lateinit var txtPromoCodeTag: TextView
    private lateinit var txtPromoCodeValue: TextView
    private lateinit var lytTotalAmount: RelativeLayout
    private lateinit var txtPlacedOnTag: TextView
    private lateinit var txtPlacedOnValue: TextView
    private lateinit var lytPlacedOn: LinearLayout
    private lateinit var txtDividerPlacedOnOrders: TextView
    private var calledFrom: String? = null
    private lateinit var txtLinkedToTag: TextView
    private lateinit var lstLinkedInvoices: RecyclerView
    private lateinit var lstLinkedGRN: RecyclerView
    private lateinit var lytLinkedTo: LinearLayout
    private lateinit var txtDividerLinkedOrders: TextView
    private lateinit var lytPay: LinearLayout
    private lateinit var txtPayHeader: TextView
    private lateinit var txtPayHeaderNotAuthorized: TextView
    private lateinit var txtCutOffTime: TextView
    private lateinit var txtCutOffTimeNotAuthorized: TextView
    private lateinit var btnPay: Button
    private lateinit var lytNotAuthorizedForPayment: RelativeLayout
    private lateinit var txtNoAuthorisedHeader: TextView
    private var txtNoAuthorisedContent: TextView? = null
    private var lytPaymentHeader: RelativeLayout? = null
    private var txtLine: TextView? = null
    private var lstEssentials: Essential? = null
    private var lytContactSupplier: RelativeLayout? = null
    private var lstDeals: DealsBaseResponse.Deals? = null
    private var detailSuppliersList: List<DetailSupplierDataModel>? = null
    private var txtLinkedToOrderValue: TextView? = null
    private var lstLinkedOrders: RecyclerView? = null
    private var txtReceived: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        if (StringHelper.isStringNullOrEmpty(SharedPref.read(SharedPref.PASSWORD_ENCRYPTED, ""))) {
            finish()
        }
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_ID)) {
                orderId = bundle.getString(ZeemartAppConstants.ORDER_ID).toString()
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID)) {
                outletId = bundle.getString(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID).toString()
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM).toString()
            }
        }
        lytPay = findViewById(R.id.lyt_pay_order)
        lytPay.setVisibility(View.GONE)
        lytNotAuthorizedForPayment = findViewById(R.id.lyt_not_authorized_for_payment)
        txtNoAuthorisedHeader = findViewById(R.id.txt_order_payable1)
        lytContactSupplier = findViewById(R.id.lyt_contact_supplier)
        txtNoAuthorisedContent = findViewById(R.id.txt_order_payable2)
        txtPayHeader = findViewById(R.id.txt_pay_header)
        txtPayHeaderNotAuthorized = findViewById(R.id.txt_pay_header_not_authorized)
        txtCutOffTime = findViewById(R.id.txt_pay_cut_of_time)
        txtCutOffTimeNotAuthorized = findViewById(R.id.txt_pay_cut_of_time_not_authorized)
        txtLine = findViewById(R.id.lyt_guide_line)
        lytPaymentHeader = findViewById(R.id.lyt_pay_header)
        btnPay = findViewById(R.id.btn_pay_button)
        txtLinkedToOrderValue = findViewById(R.id.txt_linked_to_order_value)
        txtLinkedToOrderValue!!.setVisibility(View.GONE)
        btnPay.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@OrderDetailsActivity, OrderPaymentListActivity::class.java)
            val json = ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
            intent.putExtra(ZeemartAppConstants.ORDER_DETAILS_JSON, json)
            startActivityForResult(intent, ZeemartAppConstants.RequestCode.OrderDetailsActivity)
        })
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_order_details_white)
        txtStatus = findViewById(R.id.order_detail_txt_status_value)
        txtOrderType = findViewById(R.id.order_detail_txt_order_type)
        txtRecurringOrderIcon = findViewById(R.id.recurring_order_detail)
        txtApprove = findViewById(R.id.txt_approve)
        txtEdit = findViewById(R.id.txt_edit)
        txtDividerLinkedOrders = findViewById(R.id.order_detail_divider_five)
        txtDividerPlacedOnOrders = findViewById(R.id.order_detail_divider_four)
        txtRepeatOrder = findViewById(R.id.txt_repeat_order)
        txtRecieveOrder = findViewById(R.id.txt_receive_order)
        imgReceiptStatus = findViewById(R.id.imgReceiptStatus)
        txtReceiptStatus = findViewById(R.id.txtReceiptStatus)
        txtSupplierName = findViewById(R.id.order_detail_txt_supplier_name)
        imgSupplierImage = findViewById(R.id.order_detail_img_supplierImage)
        lytSupplierThumbNail = findViewById(R.id.lyt_supplier_thumbnail)
        txtSupplierThumbNail = findViewById(R.id.txt_supplier_thumbnail)
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierThumbNail,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtOrderId = findViewById(R.id.order_detail_txt_orderno_date)
        txtDeliverTo = findViewById(R.id.order_detail_txt_location_value)
        txtDeliverOn = findViewById(R.id.order_detail_txt_calendar_value)
        txtCreatedBy = findViewById(R.id.order_detail_txt_created_by_value)
        imgOrderCreatedBy = findViewById(R.id.imageView_created_by_user)
        txtNotes = findViewById(R.id.order_detail_txt_notes_value)
        txtDeliveryInstructions = findViewById(R.id.order_detail_txt_delivery_instruction_value)
        lstOrderItems = findViewById(R.id.order_detail_lst_order_products)
        lstOrderItems.setLayoutManager(LinearLayoutManager(this))
        lstOrderItems.setNestedScrollingEnabled(false)
        txtEstimatedSubtotal = findViewById(R.id.order_detail_txt_estimated_subtotal_value)
        txtEstimatedGST = findViewById(R.id.order_detail_txt_gst_value)
        txtDeliveryFee = findViewById(R.id.order_detail_txt_delivery_fee_value)
        txtEstimatedTotal = findViewById(R.id.order_detail_txt_estimated_total_value)
        lytNotes = findViewById(R.id.order_detail_lyt_notes)
        lytDeliveryInstructions = findViewById(R.id.order_detail_lyt_delivery_instruction)
        horizontalScrollViewSpReqDelInst = findViewById(R.id.scrollView_special_req_special_inst)
        txtNoOfProducts = findViewById(R.id.order_detail_txt_item_count)
        txtEstimatedSubtotalTag = findViewById(R.id.order_detail_txt_estimated_total_tag)
        txtEstimatedTotalTag = findViewById(R.id.order_detail_txt_estimated_total)
        txtEstimatedGSTTag = findViewById(R.id.order_detail_txt_gst_tag)
        txtEstimatedGSTTag.setText(ZeemartAppConstants.Market.`this`.taxCode)
        txtOrderDetailHeaderText = findViewById(R.id.order_detailHeader)
        txtDeliverToTag = findViewById(R.id.order_detail_txt_location_tag)
        txtStatusTag = findViewById(R.id.order_detail_txt_status_tag)
        txtDeliverOnTag = findViewById(R.id.order_detail_txt_calendar_tag)
        txtSpecialRequestHeadingText = findViewById(R.id.order_detail_txt_notes_header)
        txtDeliveryInstructionTag = findViewById(R.id.order_detail_txt_delivery_instruction_header)
        txtPricesCalNotFinalText = findViewById(R.id.txt_warning_estimate_amount)
        txtOrderCreatedByTag = findViewById(R.id.order_detail_txt_created_by_tag)
        txtPromoCodeTag = findViewById(R.id.order_detail_txt_promo_code_tag)
        txtPromoCodeValue = findViewById(R.id.order_detail_txt_promo_code_value)
        imgReceiveTick = findViewById(R.id.img_marked_received)
        imgReceiveTick.setVisibility(View.GONE)
        txtPlacedOnTag = findViewById(R.id.order_detail_txt_placed_on_tag)
        txtPlacedOnValue = findViewById(R.id.order_detail_txt_placed_on_value)
        lytTotalAmount = findViewById(R.id.order_detail_lyt_order_total)
        lytPlacedOn = findViewById(R.id.lyt_order_detail_placed_on)
        lytLinkedTo = findViewById(R.id.lyt_order_invoice_linked_to)
        txtLinkedToTag = findViewById(R.id.order_detail_txt_linked_to_tag)
        lstLinkedInvoices = findViewById(R.id.lst_linked_invoices)
        lstLinkedInvoices.setLayoutManager(LinearLayoutManager(this))
        lstLinkedGRN = findViewById(R.id.lst_linked_grns)
        lstLinkedGRN.setLayoutManager(LinearLayoutManager(this))
        lstLinkedOrders = findViewById(R.id.lst_linked_orders)
        lstLinkedOrders!!.setLayoutManager(LinearLayoutManager(this))
        if (UserPermission.HasViewPrice()) {
            lytTotalAmount.setVisibility(View.VISIBLE)
        } else {
            lytTotalAmount.setVisibility(View.GONE)
        }
        btnContactSupplier = findViewById(R.id.txt_contact_supplier)
        btnContactSupplier.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (mOrder != null) {
                    AnalyticsHelper.logAction(
                        this@OrderDetailsActivity,
                        AnalyticsHelper.TAP_ORDER_DETAILS_CONTACT_SUPPLIER
                    )
                    val intent = Intent(
                        this@OrderDetailsActivity,
                        ContactSupplierDetailsActivity::class.java
                    )
                    intent.putExtra(ZeemartAppConstants.SUPPLIER_ID, mOrder!!.supplier?.supplierId)
                    intent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder!!.orderId)
                    intent.putExtra(ZeemartAppConstants.OUTLET_ID, mOrder!!.outlet?.outletId)
                    intent.putExtra(
                        ZeemartAppConstants.ORDER_DETAILS_JSON,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder).toString()
                    )
                    startActivity(intent)
                }
            }
        })
        btnBack = findViewById(R.id.order_detail_imageButtonMoveBack)
        btnBack.setOnClickListener(this)
        btnOptions = findViewById(R.id.order_detail_imageButtonOptions)
        btnOptions.setOnClickListener(this)
        btnOptions.setVisibility(View.VISIBLE)
        lytApproveRejectorders = findViewById(R.id.lyt_approve_reject_order)
        lytApproveRejectorders.setVisibility(View.GONE)
        lytRepeatReceive = findViewById(R.id.lyt_repeat_receive_order)
        lytRepeatReceive.setVisibility(View.GONE)
        lytRepeatOrder = findViewById(R.id.order_detail_repeat_order)
        lytRepeatOrder.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(
                    this@OrderDetailsActivity,
                    AnalyticsHelper.TAP_ORDER_DETAILS_REPEAT_ORDER,
                    mOrder
                )
                if ((mOrder!!.orderType == Orders.Type.ESSENTIALS.name)) {
                    threeDotLoaderWhite.setVisibility(View.VISIBLE)
                    val apiParamsHelper = ApiParamsHelper()
                    if (!StringHelper.isStringNullOrEmpty(mOrder!!.essentialsId)) {
                        apiParamsHelper.setEssentialsId(mOrder!!.essentialsId!!)
                        EssentialsApi.getPaginatedEssentials(
                            this@OrderDetailsActivity,
                            apiParamsHelper,
                            object : EssentialsResponseListener {
                                override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                                    threeDotLoaderWhite.setVisibility(View.GONE)
                                    if ((essentialsList != null) && (essentialsList.essentials != null) && (essentialsList.essentials!!.size > 0)) {
                                        lstEssentials = essentialsList.essentials!![0]
                                        if (lstEssentials != null) {
                                            val newIntent = Intent(
                                                this@OrderDetailsActivity,
                                                ReviewEssentialsActivity::class.java
                                            )
                                            val orderJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
                                            newIntent.putExtra(
                                                ZeemartAppConstants.REVIEW_ORDER_LIST,
                                                orderJson
                                            )
                                            val supplierJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    mOrder!!.supplier
                                                )
                                            val outletJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    mOrder!!.outlet
                                                )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.SUPPLIER_DETAILS,
                                                supplierJson
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.OUTLET_DETAILS,
                                                outletJson
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.ESSENTIAL_ID,
                                                mOrder!!.essentialsId
                                            )
                                            val deliveriesJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    lstEssentials!!.deliveryDates
                                                )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.DELIVERY_DATES,
                                                deliveriesJson
                                            )
                                            if (lstEssentials!!.deliveryFeePolicy != null) {
                                                val minimumOrderValueJson =
                                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                        lstEssentials!!.deliveryFeePolicy?.minOrder
                                                    )
                                                newIntent.putExtra(
                                                    ZeemartAppConstants.ESSENTIAL_MINIMUM_ORDER_VALUE_DETAILS,
                                                    minimumOrderValueJson
                                                )
                                            }
                                            val essentialList =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    lstEssentials
                                                )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.ESSENTIALS_LIST,
                                                essentialList
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.CALLED_FROM,
                                                ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
                                            )
                                            startActivity(newIntent)
                                        }
                                    } else {
                                        threeDotLoaderWhite.setVisibility(View.GONE)
                                        showAlert()
                                    }
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    threeDotLoaderWhite.setVisibility(View.GONE)
                                    showAlert()
                                }
                            })
                    } else {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                        showAlert()
                    }
                } else if ((mOrder!!.orderType == Orders.Type.DEAL.name)) {
                    if (!StringHelper.isStringNullOrEmpty(mOrder!!.dealNumber)) {
                        threeDotLoaderWhite.setVisibility(View.VISIBLE)
                        OrderHelper.getActiveDeals(
                            this@OrderDetailsActivity,
                            mOrder!!.dealNumber,
                            object : DealsResponseListener {
                                override fun onSuccessResponse(dealsBaseResponse: DealsBaseResponse?) {
                                    if ((dealsBaseResponse != null) && (dealsBaseResponse.deals != null)
                                        && (dealsBaseResponse.deals!!.size > 0)) {
                                        lstDeals = dealsBaseResponse.deals!![0]
                                        threeDotLoaderWhite.setVisibility(View.GONE)
                                        if (lstDeals != null) {
                                            val newIntent = Intent(
                                                this@OrderDetailsActivity,
                                                ReviewDealsActivity::class.java
                                            )
                                            val orderJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
                                            newIntent.putExtra(
                                                ZeemartAppConstants.REVIEW_ORDER_LIST,
                                                orderJson
                                            )
                                            val supplierJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    mOrder!!.supplier
                                                )
                                            val outletJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    mOrder!!.outlet
                                                )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.SUPPLIER_DETAILS,
                                                supplierJson
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.OUTLET_DETAILS,
                                                outletJson
                                            )
                                            val deliveriesJson =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    lstDeals!!.deliveryDates
                                                )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.DELIVERY_DATES,
                                                deliveriesJson
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.DEAL_NUMBER,
                                                lstDeals!!.dealNumber
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.DEAL_NAME,
                                                lstDeals!!.title
                                            )
                                            if (lstDeals!!.deliveryFeePolicy != null) {
                                                val minimumOrderValueJson =
                                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                        lstDeals!!.deliveryFeePolicy?.minOrder
                                                    )
                                                newIntent.putExtra(
                                                    ZeemartAppConstants.DEAL_MINIMUM_ORDER_VALUE_DETAILS,
                                                    minimumOrderValueJson
                                                )
                                            }
                                            val dealsList =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(lstDeals)
                                            newIntent.putExtra(
                                                ZeemartAppConstants.DEALS_LIST,
                                                dealsList
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.CALLED_FROM,
                                                ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
                                            )
                                            startActivity(newIntent)
                                        }
                                    } else {
                                        threeDotLoaderWhite.setVisibility(View.GONE)
                                        showAlert()
                                    }
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    threeDotLoaderWhite.setVisibility(View.GONE)
                                    showAlert()
                                }
                            })
                    }
                } else {
                    mOrder!!.supplier?.supplierId?.let {
                        OrderHelper.checkExistingDraftThenDiscard(
                            this@OrderDetailsActivity,
                            it,
                            lytRepeatOrder
                        )
                    }
                    if ((detailSuppliersList != null) && (detailSuppliersList!!.size > 0)
                        && detailSuppliersList!![0].isOrderDisabled()
                    ) {
                        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(250)
                        val builder = AlertDialog.Builder(this@OrderDetailsActivity)
                        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val view = inflater.inflate(R.layout.supplier_unavailable_dialog, null)
                        builder.setView(view)
                        builder.setCancelable(false)
                        val txtHeading = view.findViewById<TextView>(R.id.txt_small_dialog_failure)
                        txtHeading.setText(R.string.txt_cant_repeat)
                        val txtMessage =
                            view.findViewById<TextView>(R.id.txt_small_dialog_failure_message)
                        txtMessage.setText(R.string.txt_unavailable_supplier_msg)
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
                        dialog.window!!.setLayout(
                            DIALOG_LAYOUT_WIDTH,
                            WindowManager.LayoutParams.WRAP_CONTENT
                        )
                        btnOK.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(view: View) {
                                dialog.dismiss()
                            }
                        })
                    } else {
                        val newIntent =
                            Intent(this@OrderDetailsActivity, ReviewOrderActivity::class.java)
                        val selectedOrder: MutableList<Orders?> = ArrayList()
                        selectedOrder.add(mOrder)
                        val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrder)
                        newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
                        val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            mOrder!!.supplier
                        )
                        val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            mOrder!!.outlet
                        )
                        newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
                        newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
                        startActivity(newIntent)
                    }
                }
            }
        })
        lytReceive = findViewById(R.id.order_detail_receive_order)
        lytReceive.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                threeDotLoaderWhite.setVisibility(View.VISIBLE)
                mOrder!!.outlet?.let {
                    OutletsApi.getSpecificOutlet(
                        this@OrderDetailsActivity,
                        it,
                        object : GetSpecificOutletResponseListener {
                            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                                if ((specificOutlet != null) && (specificOutlet.data != null)
                                    && (specificOutlet.data!!.settings != null) && specificOutlet.data!!.settings?.isEnableGRN == true
                                ) {
                                    val intent = Intent(
                                        this@OrderDetailsActivity,
                                        GoodsReceivedNoteDashBoardActivity::class.java
                                    )
                                    intent.putExtra(
                                        ZeemartAppConstants.ORDER_DETAILS_JSON,
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
                                    )
                                    startActivity(intent)
                                } else {
                                    DialogHelper.receiveConfirmationDialog(
                                        this@OrderDetailsActivity,
                                        mOrder,
                                        AnalyticsHelper.TAP_ORDER_DETAILS_RECEIVE_ORDER,
                                        object : DialogHelper.ReceiveOrderListener {
                                            override fun onReceiveSuccessful() {
                                                val dialog: AlertDialog =
                                                    DialogHelper.alertDialogSmallSuccess(
                                                        this@OrderDetailsActivity as Activity,
                                                        getString(
                                                            R.string.txt_receive
                                                        )
                                                    )
                                                Handler().postDelayed(object : Runnable {
                                                    override fun run() {
                                                        if (dialog != null && dialog.isShowing) {
                                                            dialog.dismiss()
                                                            callGetOrderDetails()
                                                        }
                                                    }
                                                }, 2000)
                                            }

                                            override fun onReceiveError() {}
                                            override fun onDialogDismiss() {}
                                        })
                                }
                                threeDotLoaderWhite.setVisibility(View.GONE)
                            }

                            override fun onError(error: VolleyErrorHelper?) {
                                threeDotLoaderWhite.setVisibility(View.GONE)
                                DialogHelper.receiveConfirmationDialog(
                                    this@OrderDetailsActivity,
                                    mOrder,
                                    AnalyticsHelper.TAP_ORDER_DETAILS_RECEIVE_ORDER,
                                    object : DialogHelper.ReceiveOrderListener {
                                        override fun onReceiveSuccessful() {
                                            val dialog: AlertDialog =
                                                DialogHelper.alertDialogSmallSuccess(
                                                    this@OrderDetailsActivity as Activity, getString(
                                                        R.string.txt_receive
                                                    )
                                                )
                                            Handler().postDelayed(object : Runnable {
                                                override fun run() {
                                                    if (dialog != null && dialog.isShowing) {
                                                        dialog.dismiss()
                                                        callGetOrderDetails()
                                                    }
                                                }
                                            }, 2000)
                                        }

                                        override fun onReceiveError() {}
                                        override fun onDialogDismiss() {}
                                    })
                            }
                        })
                }
            }
        })
        btnApproveOrder = findViewById(R.id.order_detail_approve_order)
        btnApproveOrder.setOnClickListener(this)
        btnEdit = findViewById(R.id.order_detail_edit_order)
        btnEdit.setOnClickListener(this)
        crdDocumentsLayout = findViewById(R.id.order_detail_card_lyt_documents)
        crdDocumentsLayout.setVisibility(View.GONE)
        txtDeliveryFeeTag = findViewById(R.id.order_detail_txt_delivery_fee_tag)
        lytOrderStatus = findViewById(R.id.orderdetail_status_row)
        lytOrderStatus.setOnClickListener(this)
        txtReceived = findViewById(R.id.btn_received)
        txtReceived!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (mOrder != null) {
                    AnalyticsHelper.logAction(
                        this@OrderDetailsActivity,
                        AnalyticsHelper.TAP_ORDER_DETAILS_ORDER_STATUS
                    )
                    val newIntent =
                        Intent(this@OrderDetailsActivity, OrderHistoryLogActivity::class.java)
                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder!!.orderId)
                    newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, mOrder!!.outlet?.outletId)
                    newIntent.putExtra(
                        ZeemartAppConstants.ORDER_DETAILS_JSON,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder).toString()
                    )
                    startActivity(newIntent)
                }
            }
        })
        //        callGetOrderDetails();
        setFont()
    }

    private fun refreshSupplierList(mOrder: Orders?) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        apiParamsHelper.setSupplierId(mOrder!!.supplier?.supplierId!!)
        //apiParamsHelper.setOrderEnabled(true);
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        retrieveSupplierListNew(
            this,
            apiParamsHelper,
            outlets,
            object : SupplierListResponseListener {
                override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                    if (supplierList != null && supplierList.size > 0) {
                        detailSuppliersList = java.util.ArrayList(supplierList) as List<DetailSupplierDataModel>
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    val errorMessage = error!!.errorMessage
                    val errorType = error!!.errorType
                    if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        getToastRed(errorMessage)
                    }
                }
            })
    }

    private fun showAlert() {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(250)
        val builder = AlertDialog.Builder(this@OrderDetailsActivity)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        btnOK.setOnClickListener { dialog.dismiss() }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderId,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtStatus,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderType,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceiptStatus,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverOn,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCreatedBy,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNotes,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoOfProducts,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedSubtotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedGST,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryFee,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryFeeTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedGSTTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedSubtotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedTotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderDetailHeaderText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtStatusTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverOnTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverToTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSpecialRequestHeadingText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPricesCalNotFinalText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnContactSupplier,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderCreatedByTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtApprove,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEdit,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRecieveOrder,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRepeatOrder,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryInstructions,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryInstructionTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCodeTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCodeValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCodeValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPlacedOnTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPlacedOnValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLinkedToTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPayHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPayHeaderNotAuthorized,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCutOffTime,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCutOffTimeNotAuthorized,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnPay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoAuthorisedHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceived,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoAuthorisedContent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLinkedToOrderValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZeemartAppConstants.RequestCode.OrderDetailsActivity) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    callGetOrderDetails()
                    lytPay!!.visibility = View.GONE
                }
            }
        }
    }

    private fun callGetOrderDetails() {
        //call the order detail API
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        GetOrderDetails(this@OrderDetailsActivity, object : GetOrderDetailedDataListener {
            override fun onErrorResponse(error: VolleyError?) {
                threeDotLoaderWhite!!.visibility = View.GONE
            }
            override fun onSuccessResponse(orders: RetrieveOrderDetails?) {
                threeDotLoaderWhite!!.visibility = View.GONE
                if (orders != null && orders.data != null) {
                    mOrder = orders.data!!
                    refreshSupplierList(mOrder)
                    if ((mOrder.orderType == Orders.Type.DEAL.name)) {
                        txtEstimatedSubtotalTag!!.text = resources.getString(R.string.txt_subtotal)
                        txtEstimatedTotalTag!!.text = resources.getString(R.string.txt_total)
                        txtPricesCalNotFinalText!!.visibility = View.GONE
                        btnEdit!!.visibility = View.GONE
                        lytContactSupplier!!.visibility = View.GONE
                        lytRepeatOrder!!.visibility = View.VISIBLE
                        /* if (!StringHelper.isStringNullOrEmpty(mOrder.getSupplier().getSettings().getGst().getPercentDisplayValue()))
                            txtEstimatedGSTTag.setText(ZeemartAppConstants.Market.getThis().getTaxCode() + " " + mOrder.getSupplier().getSettings().getGst().getPercentDisplayValue() + "%");*/
                    } else if ((mOrder.orderType == Orders.Type.ESSENTIALS.name)) {
                        txtEstimatedSubtotalTag!!.text = resources.getString(R.string.txt_subtotal)
                        txtEstimatedTotalTag!!.text = resources.getString(R.string.txt_total)
                        lytContactSupplier!!.visibility = View.GONE
                        txtPricesCalNotFinalText!!.visibility = View.GONE
                        btnEdit!!.visibility = View.GONE
                        lytRepeatOrder!!.visibility = View.VISIBLE
                        /* if (!StringHelper.isStringNullOrEmpty(mOrder.getSupplier().getSettings().getGst().getPercentDisplayValue()))
                            txtEstimatedGSTTag.setText(ZeemartAppConstants.Market.getThis().getTaxCode() + " " + mOrder.getSupplier().getSettings().getGst().getPercentDisplayValue() + "%");*/
                    } else {
                        lytContactSupplier!!.visibility = View.VISIBLE
                        threeDotLoaderWhite!!.visibility = View.GONE
                        txtEstimatedSubtotalTag!!.text =
                            resources.getString(R.string.txt_estimated_subtotal)
                        txtEstimatedTotalTag!!.text =
                            resources.getString(R.string.txt_estimated_total)
                        txtPricesCalNotFinalText!!.visibility = View.VISIBLE
                        txtEstimatedGSTTag!!.text = ZeemartAppConstants.Market.`this`.taxCode
                    }
                    //Log.e("StatusDetail","toJson:"+ZeemartBuyerApp.gson.toJson(mOrder).toString());
                    setUIReceiptStatus(mOrder)
                    lytLinkedTo!!.visibility = View.GONE
                    txtDividerLinkedOrders!!.visibility = View.GONE
                    if (mOrder.isAddOn && !StringHelper.isStringNullOrEmpty(mOrder.linkedOrder)) {
                        txtLinkedToOrderValue!!.visibility = View.VISIBLE
                        lstLinkedOrders!!.visibility = View.GONE
                        txtLinkedToOrderValue!!.text =
                            resources.getString(R.string.txt_order_with_hash_tag) + mOrder.linkedOrder // link to order
                        txtLinkedToOrderValue!!.setOnClickListener {
                            val newIntent =
                                Intent(this@OrderDetailsActivity, OrderDetailsActivity::class.java)
                            newIntent.putExtra(
                                ZeemartAppConstants.ORDER_ID,
                                mOrder.linkedOrder
                            )
                            newIntent.putExtra(
                                ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                mOrder.outlet?.outletId
                            )
                            startActivity(newIntent)
                        }
                        lytLinkedTo!!.visibility = View.VISIBLE
                        txtDividerLinkedOrders!!.visibility = View.VISIBLE
                    } else if (!mOrder.isAddOn && (mOrder.addOns != null) && (mOrder.addOns.size > 0)) {
                        txtLinkedToOrderValue!!.visibility = View.GONE
                        lstLinkedOrders!!.visibility = View.VISIBLE
                        lytLinkedTo!!.visibility = View.VISIBLE
                        txtDividerLinkedOrders!!.visibility = View.VISIBLE
                        val linkedInvoiceListAdapter = LinkedAddOnOrdersAdapter(
                            mOrder.addOns,
                            this@OrderDetailsActivity,
                            object : LinkedAddOnOrdersAdapter.TapOnOrder {
                                override fun onOrderTapped(linkedInvoice: String?) {
                                    val newIntent = Intent(
                                        this@OrderDetailsActivity,
                                        OrderDetailsActivity::class.java
                                    )
                                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, linkedInvoice)
                                    newIntent.putExtra(
                                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                        mOrder.outlet!!.outletId
                                    )
                                    startActivity(newIntent)
                                }
                            })
                        lstLinkedOrders!!.adapter = linkedInvoiceListAdapter
                    } else {
                        txtLinkedToOrderValue?.visibility = View.GONE
                        lstLinkedOrders?.visibility = View.GONE
                    }
                    if (mOrder.isLinkedToInvoice) {
                        if (mOrder.linkedInvoices != null) {
                            txtDividerLinkedOrders.visibility = View.VISIBLE
                            val LinkedInvoicesLst: MutableList<Orders.LinkedInvoice?> =
                                ArrayList<Orders.LinkedInvoice?>()
                            for (i in mOrder.linkedInvoices!!.indices) {
                                if (!StringHelper.isStringNullOrEmpty(mOrder.linkedInvoices!![i].invoiceNum)) {
                                    LinkedInvoicesLst.add(mOrder.linkedInvoices!![i])
                                    lytLinkedTo.visibility = View.VISIBLE
                                }
                            }
                            val linkedInvoiceListAdapter = LinkedInvoiceListAdapter(
                                this@OrderDetailsActivity,
                                LinkedInvoicesLst as List<Orders.LinkedInvoice>?,
                                object : LinkedInvoiceListAdapter.TapOnInvoiceListener {
                                    override fun onInvoiceTapped(linkedInvoice: Orders.LinkedInvoice?) {
                                        val newIntent = Intent(
                                            this@OrderDetailsActivity,
                                            InvoiceDetailActivity::class.java
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.INVOICE_ID,
                                            linkedInvoice?.invoiceId
                                        )
                                        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(newIntent)
                                    }
                                })
                            lstLinkedInvoices!!.visibility = View.VISIBLE
                            lstLinkedInvoices!!.adapter = linkedInvoiceListAdapter
                        } else {
                            lstLinkedInvoices!!.visibility = View.GONE
                        }
                    } else {
                    }
                    if ((mOrder.orderStatus == OrderStatus.CREATED.statusName)) {
                        btnOptions!!.visibility = View.VISIBLE
                        if (mOrder.createdBy?.id != SharedPref.read(SharedPref.USER_ID, "")) {
                            setUIForPendingYourApproval(mOrder)
                        } else {
                            setUIForPendingApprovalOrders(mOrder)
                        }
                    } else if ((mOrder.orderStatus == OrderStatus.PLACED.statusName)) {
                        btnOptions!!.visibility = View.VISIBLE
                        lytPlacedOn!!.visibility = View.VISIBLE
                        txtDividerPlacedOnOrders!!.visibility = View.VISIBLE
                        setUIForPlacedOrders(mOrder)
                    } else if ((mOrder.orderStatus == OrderStatus.INVOICED.statusName)) {
                        btnOptions!!.visibility = View.VISIBLE
                        lytPlacedOn!!.visibility = View.GONE
                        txtDividerPlacedOnOrders!!.visibility = View.GONE
                        setUIForInvoicedOrders(mOrder)
                    } else if ((mOrder.orderStatus == OrderStatus.CREATING.statusName)) {
                        btnOptions!!.visibility = View.VISIBLE
                        lytPlacedOn!!.visibility = View.GONE
                        txtDividerPlacedOnOrders!!.visibility = View.GONE
                        setUIForProcessingOrder(mOrder)
                    } else if ((mOrder.orderStatus == OrderStatus.CANCELLED.statusName)) {
                        btnOptions!!.visibility = View.GONE
                        lytPlacedOn!!.visibility = View.VISIBLE
                        txtDividerPlacedOnOrders!!.visibility = View.VISIBLE
                        setUIForCancelledOrder(mOrder)
                    } else if ((mOrder.orderStatus == OrderStatus.REJECTED.statusName)) {
                        btnOptions!!.visibility = View.GONE
                        lytPlacedOn!!.visibility = View.GONE
                        txtDividerPlacedOnOrders!!.visibility = View.GONE
                        setUIForRejectedOrder(mOrder)
                    } else if ((mOrder.orderStatus == OrderStatus.VOIDED.statusName)) {
                        btnOptions!!.visibility = View.GONE
                        lytPlacedOn!!.visibility = View.GONE
                        txtDividerPlacedOnOrders!!.visibility = View.GONE
                        setUIForRejectedOrder(mOrder)
                    } else if (((mOrder.orderStatus == OrderStatus.REJECTING.statusName) || (mOrder.orderStatus == OrderStatus.CANCELLING.statusName))) {
                        btnOptions!!.visibility = View.GONE
                        lytPlacedOn!!.visibility = View.GONE
                        txtDividerPlacedOnOrders!!.visibility = View.GONE
                        setUIForIntermediateStates(mOrder)
                    } else if ((mOrder.orderStatus == OrderStatus.APPROVING.statusName)) {
                        btnOptions!!.visibility = View.VISIBLE
                        lytPlacedOn!!.visibility = View.GONE
                        txtDividerPlacedOnOrders!!.visibility = View.GONE
                        setUIForIntermediateStates(mOrder)
                    } else if ((mOrder.orderStatus == OrderStatus.PENDING_PAYMENT.statusName)) {
                        btnOptions!!.visibility = View.GONE
                        setUIForPendingPaymentOrders(mOrder)
                    }
                }
            }
        }).getOrderDetailsData(orderId, outletId)
    }

    private fun createCancelOrderDialog() {
        DialogHelper.ShowCancelOrder(this@OrderDetailsActivity, object :
            DialogHelper.CancelOrderCallback {
            override fun dismiss() {}
            override fun cancelOrder(remarks: String?) {
                isStatusChanged = true
                txtStatus!!.setText(OrderStatus.CANCELLING.statusResId)
                txtStatus!!.setBackgroundResource(R.drawable.btn_rounded_grey)
                txtStatus!!.setTextColor(resources.getColor(R.color.black))

                //Call the cancel order API
                val orderStatusBeforeCancel = mOrder!!.orderStatus
                val updateOrderStatusRequestData = OrderHelper.CancelOrderJson(mOrder, remarks)
                CancelOrder(
                    this@OrderDetailsActivity,
                    object : CancelOrder.GetResponseStatusListener {
                        override fun onSuccessResponse(status: String?) {}
                        override fun onErrorResponse(error: VolleyError?) {
                            Log.d("", "$error*****")
                            mOrder!!.orderStatus = orderStatusBeforeCancel
                            if (error?.networkResponse != null && error.message != null) {
                                if ((error.networkResponse.statusCode.toString() == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE)) {
                                    ZeemartBuyerApp.getToastRed(error.message)
                                } else {
                                    ZeemartBuyerApp.getToastRed(error.message)
                                }
                            } else {
                                if (error?.message != null) {
                                    ZeemartBuyerApp.getToastRed(error.message)
                                }
                            }
                        }
                    }).cancelOrderData(updateOrderStatusRequestData, outletId)
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.order_detail_imageButtonMoveBack -> onStatusChanged()
            R.id.order_detail_imageButtonOptions ->                 //show the Optionsmenu
                if (mOrder != null) {
                    AnalyticsHelper.logAction(
                        this@OrderDetailsActivity,
                        AnalyticsHelper.TAP_ORDER_DETAILS_MORE
                    )
                    showPopup()
                }
            R.id.order_detail_approve_order -> {
                //call the approve API
                val dialogMessage = "Approve this order?"
                createConfirmationDialog(dialogMessage, true)
            }
            R.id.order_detail_edit_order -> {
                //call the REview order apI with the edit functionality
                val newIntentEditOrder = Intent(this, ReviewOrderActivity::class.java)
                val selectedOrders: MutableList<Orders?> = ArrayList()
                selectedOrders.add(mOrder)
                val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrders)
                newIntentEditOrder.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
                val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                    selectedOrders[0]!!.supplier
                )
                val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                    selectedOrders[0]!!.outlet
                )
                newIntentEditOrder.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
                newIntentEditOrder.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
                newIntentEditOrder.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.APPROVER_EDIT_ORDER
                )
                startActivity(newIntentEditOrder)
            }
            R.id.orderdetail_status_row ->                 //Call the Activity History if order detail has loaded
                if (mOrder != null) {
                    AnalyticsHelper.logAction(
                        this@OrderDetailsActivity,
                        AnalyticsHelper.TAP_ORDER_DETAILS_ORDER_STATUS
                    )
                    val newIntent = Intent(this, OrderHistoryLogActivity::class.java)
                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder!!.orderId)
                    newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, mOrder!!.outlet?.outletId)
                    newIntent.putExtra(
                        ZeemartAppConstants.ORDER_DETAILS_JSON,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder).toString()
                    )
                    startActivity(newIntent)
                }
            else -> {}
        }
    }

    private fun setUIReceiptStatus(order: Orders?) {
        //imgReceiptStatus
        var fail = 0
        if (order!!.emailStatusDetails != null) for (email: StatusDetail.Email in order.emailStatusDetails!!) {
            if (!email.isSent()) fail++
        }
        if (order.mobileStatusDetails != null) for (mobile: StatusDetail.Mobile in order.mobileStatusDetails!!) {
            if (!mobile.isSent()) fail++
        }
        if (order.whatsAppStatusDetails != null) for (whatsApp: StatusDetail.WhatsApp in order.whatsAppStatusDetails!!) {
            if (!whatsApp.isSent()) fail++
        }
        if (order.isPlacedCancelledInvoiced) {
            imgReceiptStatus!!.visibility = View.VISIBLE
            txtReceiptStatus!!.visibility = View.VISIBLE
            if (fail == 0) {
                imgReceiptStatus!!.setImageResource(R.drawable.tick_black)
                ImageViewCompat.setImageTintList(
                    (imgReceiptStatus)!!, ColorStateList.valueOf(
                        ContextCompat.getColor(this@OrderDetailsActivity, R.color.dark_grey)
                    )
                )
                txtReceiptStatus!!.setText(R.string.receipt_all_notifications_sent)
            } else {
                imgReceiptStatus!!.setImageResource(R.drawable.icon_error)
                txtReceiptStatus!!.text =
                    String.format(getString(R.string.receipt_undelivered_notification), fail)
            }
        } else {
            imgReceiptStatus!!.visibility = View.GONE
            txtReceiptStatus!!.visibility = View.GONE
        }
        if ((OrderStatus.PLACED.statusName == order.orderStatus)) {
            if (order.isAcknowledged) {
                imgReceiptStatus!!.visibility = View.VISIBLE
                txtReceiptStatus!!.visibility = View.VISIBLE
                imgReceiptStatus!!.setImageResource(R.drawable.tick_black)
                ImageViewCompat.setImageTintList(
                    (imgReceiptStatus)!!, ColorStateList.valueOf(
                        ContextCompat.getColor(this@OrderDetailsActivity, R.color.green)
                    )
                )
                txtReceiptStatus!!.setText(R.string.txt_acknowledged)
                txtReceiptStatus!!.setTextColor(resources.getColor(R.color.green))
            }
        }
    }

    /**
     * set UI for the pending approval order details
     *
     * @param order
     */
    private fun setUIForPendingApprovalOrders(order: Orders?) {
        lytRepeatReceive!!.visibility = View.VISIBLE
        if (mOrder!!.orderType != Orders.Type.DEAL.name && (mOrder!!.orderType != Orders.Type.ESSENTIALS.name)) lytRepeatOrder!!.visibility =
            View.VISIBLE
        lytApproveRejectorders!!.visibility = View.GONE
        setOrderDetailCommonUI(order)
    }

    /**
     * set UI for pending your approval order details
     *
     * @param order
     */
    fun setUIForPendingYourApproval(order: Orders?) {
        lytApproveRejectorders!!.visibility = View.VISIBLE
        lytRepeatReceive!!.visibility = View.GONE
        setOrderDetailCommonUI(order)
    }

    private fun setUIForPlacedOrders(order: Orders?) {
        if (order!!.pdfURL != null) {
        }
        lytApproveRejectorders!!.visibility = View.GONE
        lytRepeatReceive!!.visibility = View.VISIBLE
        if (mOrder!!.orderType != Orders.Type.DEAL.name && mOrder!!.orderType != Orders.Type.ESSENTIALS.name) lytRepeatOrder!!.visibility =
            View.VISIBLE
        setOrderDetailCommonUI(order)
    }

    private fun setUIForInvoicedOrders(order: Orders?) {
        lytApproveRejectorders!!.visibility = View.GONE
        lytRepeatReceive!!.visibility = View.VISIBLE
        if (mOrder!!.orderType != Orders.Type.DEAL.name && mOrder!!.orderType != Orders.Type.ESSENTIALS.name) lytRepeatOrder!!.visibility =
            View.VISIBLE
        setOrderDetailCommonUI(order)
    }

    private fun setUIForRejectedOrder(order: Orders?) {
        lytApproveRejectorders!!.visibility = View.GONE
        lytRepeatReceive!!.visibility = View.VISIBLE
        if (mOrder!!.orderType != Orders.Type.DEAL.name && mOrder!!.orderType != Orders.Type.ESSENTIALS.name) lytRepeatOrder!!.visibility =
            View.VISIBLE
        setOrderDetailCommonUI(order)
    }

    private fun setUIForCancelledOrder(order: Orders?) {
        lytApproveRejectorders!!.visibility = View.GONE
        lytRepeatReceive!!.visibility = View.VISIBLE
        if (StringHelper.isStringNullOrEmpty(mOrder!!.pdfURL)) {
            btnOptions!!.visibility = View.GONE
        } else {
            btnOptions!!.visibility = View.VISIBLE
        }
        if (mOrder!!.orderType != Orders.Type.DEAL.name && mOrder!!.orderType != Orders.Type.ESSENTIALS.name) lytRepeatOrder!!.visibility =
            View.VISIBLE
        setOrderDetailCommonUI(order)
    }

    private fun setUIForProcessingOrder(order: Orders?) {
        lytApproveRejectorders!!.visibility = View.GONE
        lytRepeatReceive!!.visibility = View.VISIBLE
        if (mOrder!!.orderType != Orders.Type.DEAL.name && mOrder!!.orderType != Orders.Type.ESSENTIALS.name) lytRepeatOrder!!.visibility =
            View.VISIBLE
        setOrderDetailCommonUI(order)
    }

    private fun setUIForPendingPaymentOrders(orders: Orders?) {
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && (calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_PAYMENT_CONFIRMATION)) {
            lytPay!!.visibility = View.VISIBLE
            lytRepeatReceive!!.visibility = View.GONE
            btnPay!!.visibility = View.GONE
            btnOptions!!.visibility = View.GONE
            lytNotAuthorizedForPayment!!.visibility = View.VISIBLE
            txtNoAuthorisedHeader!!.text =
                resources.getString(R.string.txt_payment_is_verifying)
            txtNoAuthorisedContent!!.text =
                resources.getString(R.string.txt_update_order_shortly)
            txtPayHeaderNotAuthorized!!.visibility = View.GONE
            txtCutOffTimeNotAuthorized!!.visibility = View.GONE
            txtLine!!.visibility = View.GONE
            lytPaymentHeader!!.visibility = View.GONE
        } else {
            if ((orders!!.transactionImage != null && (orders.transactionImage?.status == Orders.PaymentStatus.UPLOADED.statusName)) || (orders.paymentType != null && (orders.paymentType == ZeemartAppConstants.FINAXAR))) {
                lytPay!!.visibility = View.VISIBLE
                lytRepeatReceive!!.visibility = View.GONE
                btnPay!!.visibility = View.GONE
                btnOptions!!.visibility = View.GONE
                lytNotAuthorizedForPayment!!.visibility = View.VISIBLE
                txtNoAuthorisedHeader!!.text =
                    resources.getString(R.string.txt_payment_is_verifying)
                txtNoAuthorisedContent!!.text =
                    resources.getString(R.string.txt_update_order_shortly)
                txtPayHeaderNotAuthorized!!.visibility = View.GONE
                txtCutOffTimeNotAuthorized!!.visibility = View.GONE
                txtLine!!.visibility = View.GONE
                lytPaymentHeader!!.visibility = View.GONE
            } else {
                btnOptions!!.visibility = View.VISIBLE
                lytPay!!.visibility = View.VISIBLE
                lytRepeatReceive!!.visibility = View.GONE
                lytPaymentHeader!!.visibility = View.VISIBLE
                txtCutOffTime!!.text = DateHelper.getDateInDateMonthHourMinute(orders.timeCutOff)
                txtCutOffTimeNotAuthorized!!.text =
                    DateHelper.getDateInDateMonthHourMinute(orders.timeCutOff)
                if (UserPermission.HasManagePayments()) {
                    btnPay!!.visibility = View.VISIBLE
                    lytNotAuthorizedForPayment!!.visibility = View.GONE
                } else {
                    btnPay!!.visibility = View.GONE
                    lytNotAuthorizedForPayment!!.visibility = View.VISIBLE
                    txtPayHeaderNotAuthorized!!.visibility = View.VISIBLE
                    txtCutOffTimeNotAuthorized!!.visibility = View.VISIBLE
                    lytPaymentHeader!!.visibility = View.GONE
                }
            }
        }
        setOrderDetailCommonUI(orders)
    }

    private fun setUIForIntermediateStates(order: Orders?) {
        lytApproveRejectorders!!.visibility = View.GONE
        setOrderDetailCommonUI(order)
    }

    private fun setOrderDetailCommonUI(order: Orders?) {
        if (order != null) {
            OrderStatus.SetStatusBackground(this@OrderDetailsActivity, order, txtStatus)
        }
        if ((order!!.orderType == Orders.Type.STANDING.name)) {
            txtOrderType!!.visibility = View.VISIBLE
            txtRecurringOrderIcon!!.visibility = View.VISIBLE
            txtOrderType!!.setText(Orders.Type.STANDING.resId)
            txtOrderId!!.text = " • "
        } else {
            txtRecurringOrderIcon!!.visibility = View.GONE
            txtOrderType!!.visibility = View.GONE
            txtOrderId!!.text = ""
        }
        txtSupplierName!!.text = order.supplier?.supplierName
        val orderIdDateCreated = "#" + order.orderId
        txtOrderId!!.append(orderIdDateCreated)
        txtDeliverTo!!.text = order.outlet?.outletName
        val dateTime = order.timeDelivered?.let {
            DateHelper.getDateInFullDayDateMonthYearFormat(
                it,
                order.outlet?.timeZoneFromOutlet
            )
        }
        txtDeliverOn!!.text = dateTime
        txtCreatedBy!!.text = order.createdBy?.firstName + " " + order.createdBy?.lastName
        if (order.timePlaced != null) {
            val datePlacedOn = DateHelper.getDateInFullDayDateMonthYearFormat(
                order.timePlaced!!
            )
            txtPlacedOnValue!!.text = datePlacedOn
        } else {
            lytPlacedOn!!.visibility = View.GONE
            txtDividerPlacedOnOrders!!.visibility = View.GONE
        }
        if (StringHelper.isStringNullOrEmpty(order.createdBy?.imageURL)) {
            Picasso.get().load(R.drawable.placeholder_user)
                .centerCrop()
                .resize(PLACE_HOLDER_USER_IMAGE_WIDTH, PLACE_HOLDER_USER_IMAGE_HEIGHT)
                .transform(CropCircleTransformation())
                .into(imgOrderCreatedBy)
        } else {
            Picasso.get().load(order.createdBy?.imageURL)
                .placeholder(R.drawable.placeholder_user)
                .centerCrop()
                .resize(PLACE_HOLDER_USER_IMAGE_WIDTH, PLACE_HOLDER_USER_IMAGE_HEIGHT)
                .transform(CropCircleTransformation())
                .into(imgOrderCreatedBy)
        }
        if (StringHelper.isStringNullOrEmpty(order.supplier?.logoURL)) {
            imgSupplierImage!!.visibility = View.INVISIBLE
            lytSupplierThumbNail!!.visibility = View.VISIBLE
            lytSupplierThumbNail!!.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    order.supplier?.supplierName!!, this@OrderDetailsActivity
                )
            )
            txtSupplierThumbNail!!.text =
                CommonMethods.SupplierThumbNailShortCutText(order.supplier?.supplierName!!)
            txtSupplierThumbNail!!.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    order.supplier?.supplierName!!,
                    this@OrderDetailsActivity
                )
            )
        } else {
            imgSupplierImage!!.visibility = View.VISIBLE
            lytSupplierThumbNail!!.visibility = View.GONE
            Picasso.get().load(order.supplier?.logoURL).placeholder(R.drawable.placeholder_all)
                .into(imgSupplierImage)
        }
        setNotesAndDeliveryInstructions(order)
        val orderProducts = order.products
        lstOrderItems!!.adapter = OrderDetailProductListAdapter(this, orderProducts!!)
        if (orderProducts?.size == 1) {
            txtNoOfProducts!!.text =
                orderProducts.size.toString() + " " + getString(R.string.txt_item)
        } else {
            txtNoOfProducts!!.text =
                orderProducts?.size.toString() + " " + getString(R.string.txt_items)
        }
        if (order.amount?.subTotal?.amount != null) {
            txtEstimatedSubtotal!!.text = order.amount!!.subTotal?.displayValue
        }
        if (order.amount?.gst?.amount != null) {
            txtEstimatedGST!!.text = order.amount!!.gst?.displayValue
        }
        if (order.amount?.deliveryFee?.amount != null && order.amount!!.deliveryFee?.amount != 0.0) {
            txtDeliveryFee!!.text = order.amount!!.deliveryFee?.displayValue
            txtDeliveryFeeTag!!.setTextColor(resources.getColor(R.color.black))
            txtDeliveryFeeTag!!.visibility = View.VISIBLE
            txtDeliveryFee!!.visibility = View.VISIBLE
        } else {
            txtDeliveryFeeTag!!.visibility = View.GONE
            txtDeliveryFee!!.visibility = View.GONE
            txtDeliveryFeeTag!!.setTextColor(resources.getColor(R.color.black))
            txtDeliveryFeeTag!!.text = getString(R.string.txt_delivery_fee)
            txtDeliveryFee!!.text = PriceDetails().displayValue
        }
        if (order.amount?.total?.amount != null) {
            txtEstimatedTotal!!.text = order.amount!!.total?.displayValue
        }
        if (order.amount?.discount != null && !StringHelper.isStringNullOrEmpty(order.promoCode)) {
            txtPromoCodeValue!!.visibility = View.VISIBLE
            txtPromoCodeTag!!.visibility = View.VISIBLE
            txtPromoCodeTag!!.text = getString(R.string.promo_code_colon) + order.promoCode
            txtPromoCodeValue!!.text = "-" + order.amount!!.discount?.displayValue
        } else {
            txtPromoCodeValue!!.visibility = View.GONE
            txtPromoCodeTag!!.visibility = View.GONE
        }
        if (order.isReceived) {
            txtReceived!!.visibility = View.VISIBLE
            txtReceived!!.text = resources.getString(R.string.txt_received)
        } else {
            txtReceived!!.visibility = View.GONE
        }
        if ((order.grn != null) && (order.grn!!.grnList != null) && (order.grn!!.grnList?.size!! > 0)) {
            val linkedGrnOrdersAdapter = LinkedGrnOrdersAdapter(
                order.grn!!.grnList,
                this@OrderDetailsActivity,
                object : LinkedGrnOrdersAdapter.TapOnOrder {
                    override fun onOrderTapped(linkedInvoice: Grn?) {
                        val newIntent =
                            Intent(this@OrderDetailsActivity, GrnDetailsActivity::class.java)
                        newIntent.putExtra(
                            ZeemartAppConstants.GRN_DETAILS_JSON,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(linkedInvoice)
                        )
                        newIntent.putExtra(
                            ZeemartAppConstants.ORDER_DETAILS_JSON,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
                        )
                        startActivity(newIntent)
                    }
                })
            lstLinkedGRN!!.visibility = View.VISIBLE
            lstLinkedGRN!!.adapter = linkedGrnOrdersAdapter
            if (order.grn!!.grnList?.get(0)?.timeReceived != null && order.grn!!.grnList?.get(0)?.timeReceived != 0L) {
                txtReceived!!.visibility = View.VISIBLE
                txtReceived!!.text =
                    resources.getString(R.string.txt_received) + " - " + DateHelper.getDateInDateMonthYearFormat(
                        order.grn!!.grnList?.get(0)?.timeReceived
                    )
            } else {
                if (order.isReceived) {
                    txtReceived!!.visibility = View.VISIBLE
                    txtReceived!!.text = resources.getString(R.string.txt_received)
                } else {
                    txtReceived!!.visibility = View.GONE
                }
            }
            lytLinkedTo!!.visibility = View.VISIBLE
            txtDividerLinkedOrders!!.visibility = View.VISIBLE
        }
        lstOrderItems!!.visibility = View.VISIBLE
        setReceiveLayoutVisibility()
    }

    /**
     * set special request and delivery instructions if available for the order
     *
     * @param order
     */
    private fun setNotesAndDeliveryInstructions(order: Orders?) {
        if (StringHelper.isStringNullOrEmpty(order!!.notes) && StringHelper.isStringNullOrEmpty(
                order.deliveryInstruction
            )
        ) {
            horizontalScrollViewSpReqDelInst!!.visibility = View.GONE
        }
        if (StringHelper.isStringNullOrEmpty(order.notes)) {
            lytNotes!!.visibility = View.GONE
        } else {
            lytNotes!!.visibility = View.VISIBLE
            txtNotes!!.text = order.notes
        }
        if (StringHelper.isStringNullOrEmpty(order.deliveryInstruction)) {
            lytDeliveryInstructions!!.visibility = View.GONE
        } else {
            lytDeliveryInstructions!!.visibility = View.VISIBLE
            txtDeliveryInstructions!!.text = order.deliveryInstruction
        }
    }

    /**
     * create confirmation dialog for approve or reject request
     *
     * @param message
     * @param isApprove
     */
    fun createConfirmationDialog(message: String?, isApprove: Boolean) {
        if (isApprove) {
            val builder = AlertDialog.Builder(this)
            builder.setNegativeButton(getString(R.string.txt_no)) { dialog, which -> dialog.dismiss() }
            builder.setPositiveButton(getString(R.string.txt_approve)) { dialog, which ->
                AnalyticsHelper.logAction(
                    this@OrderDetailsActivity,
                    AnalyticsHelper.TAP_ORDER_REVIEW_APPROVE_ORDER,
                    mOrder
                )
                ApproveRejectOrders.ApproveOrder(
                    this@OrderDetailsActivity,
                    mOrder,
                    object : ApproveRejectOrders.GetResponseStatusListener {
                        override fun onSuccessResponse(status: String?) {
                            val newIntent = Intent(
                                this@OrderDetailsActivity,
                                OrderProcessingPlacedActivity::class.java
                            )
                            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder!!.orderId)
                            newIntent.putExtra(
                                ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                mOrder!!.outlet?.outletId
                            )
                            newIntent.putExtra(
                                ZeemartAppConstants.CALLED_FROM,
                                ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
                            )
                            startActivity(newIntent)
                            finish()
                            dialog.dismiss()
                        }

                        override fun onErrorResponse(error: VolleyError?) {
                            dialog.dismiss()
                        }
                    })
            }
            builder.setMessage(message)
            builder.setTitle(null)
            builder.create().show()
        } else if (!isApprove) {
            DialogHelper.ShowRejectOrder(this@OrderDetailsActivity, object :
                DialogHelper.RejectOrderCallback {
                override fun dismiss() {}
                override fun rejectOrder(remarks: String?) {
                    ApproveRejectOrders.RejectOrder(
                        this@OrderDetailsActivity,
                        mOrder,
                        remarks,
                        object : ApproveRejectOrders.GetResponseStatusListener {
                            override fun onSuccessResponse(status: String?) {
                                isStatusChanged = true
                                val newIntent = Intent(
                                    this@OrderDetailsActivity,
                                    OrderProcessingPlacedActivity::class.java
                                )
                                newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder!!.orderId)
                                newIntent.putExtra(
                                    ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                    mOrder!!.outlet?.outletId
                                )
                                newIntent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM,
                                    ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
                                )
                                startActivity(newIntent)
                                finish()
                            }

                            override fun onErrorResponse(error: VolleyError?) {}
                        })
                }
            })
        }
    }

    fun showPopup() {
        val orderStatus = mOrder!!.orderStatus
        val popup = PopupMenu(this, btnOptions)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.order_details_options, popup.menu)
        val popupMenu = popup.menu
        popupMenu.findItem(R.id.txt_cancel_orderoption).isVisible = true
        //popupMenu.findItem(R.id.txt_repeat_orderview).setVisible(true);
        popupMenu.findItem(R.id.txt_reject_order_option).isVisible = false
        popupMenu.findItem(R.id.txt_report_issue_mistakeview).isVisible = false
        popupMenu.findItem(R.id.txt_receive_order_option).isVisible = false
        if (StringHelper.isStringNullOrEmpty(mOrder!!.pdfURL)) {
            popupMenu.findItem(R.id.txt_view_as_pdfview).isVisible = false
            popupMenu.findItem(R.id.txt_download_pdfview).isVisible = false
        } else {
            popupMenu.findItem(R.id.txt_view_as_pdfview).isVisible = true
            popupMenu.findItem(R.id.txt_download_pdfview).isVisible = true
        }
        if ((orderStatus == OrderStatus.PLACED.statusName) && (!mOrder!!.isInvoiced)) {
            if ((mOrder!!.isOrderByUser || mOrder!!.isApprovedByUser)) {
                popupMenu.findItem(R.id.txt_cancel_orderoption).isVisible = true
            } else {
                popupMenu.findItem(R.id.txt_cancel_orderoption).isVisible = false
            }
            //            popupMenu.findItem(R.id.txt_report_issue_mistakeview).setVisible(true);
        }
        if ((orderStatus == OrderStatus.CANCELLED.statusName)) {
            popupMenu.findItem(R.id.txt_cancel_orderoption).isVisible = false
        }
        if ((mOrder != null) && (mOrder!!.grn != null) && (mOrder!!.grn?.grnList != null) && (mOrder!!.grn?.grnList?.size!! > 0)) {
            popupMenu.findItem(R.id.txt_receive_order_option).isVisible = true
        } else {
            popupMenu.findItem(R.id.txt_receive_order_option).isVisible = false
        }
        if ((mOrder!!.isInvoiced) && (orderStatus == OrderStatus.PLACED.statusName)) {
            popupMenu.findItem(R.id.txt_cancel_orderoption).isVisible = false
        }
        if (lytApproveRejectorders!!.visibility == View.VISIBLE) {
            popupMenu.findItem(R.id.txt_cancel_orderoption).isVisible = false
            popupMenu.findItem(R.id.txt_view_as_pdfview).isVisible = false
            popupMenu.findItem(R.id.txt_download_pdfview).isVisible = false
            popupMenu.findItem(R.id.txt_reject_order_option).isVisible = true
        }
        if ((orderStatus == OrderStatus.PENDING_PAYMENT.statusName)) {
            popupMenu.findItem(R.id.txt_cancel_orderoption).isVisible = true
            popupMenu.findItem(R.id.txt_view_as_pdfview).isVisible = false
            popupMenu.findItem(R.id.txt_download_pdfview).isVisible = false
            popupMenu.findItem(R.id.txt_reject_order_option).isVisible = false
        }
        if (!mOrder!!.isAddOn && (mOrder!!.addOns != null) && (mOrder!!.addOns.size > 0)) {
            popupMenu.findItem(R.id.txt_cancel_orderoption).title =
                getString(R.string.txt_cancel_linked_orders)
        }
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.txt_view_as_pdfview -> {
                    AnalyticsHelper.logAction(
                        this@OrderDetailsActivity,
                        AnalyticsHelper.TAP_ORDER_DETAILS_VIEW_AS_PDF
                    )
                    popup.dismiss()
                    mOrder!!.pdfURL?.let { GetPdfFile(it, this@OrderDetailsActivity).invoice }
                    true
                }
                R.id.txt_receive_order_option -> {
                    popup.dismiss()
                    val intent = Intent(
                        this@OrderDetailsActivity,
                        GoodsReceivedNoteDashBoardActivity::class.java
                    )
                    intent.putExtra(
                        ZeemartAppConstants.ORDER_DETAILS_JSON,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
                    )
                    startActivity(intent)
                    true
                }
                R.id.txt_download_pdfview -> {
                    downloadPDF()
                    true
                }
                R.id.txt_report_issue_mistakeview -> {
                    AnalyticsHelper.logAction(
                        this@OrderDetailsActivity,
                        AnalyticsHelper.TAP_ORDER_DETAILS_REPORT_ISSUE
                    )
                    popup.dismiss()
                    EmailHelper.SendFeedback(this@OrderDetailsActivity, mOrder)
                    //                        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//                        emailIntent.setType("plain/text");
//                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, getResources().getString(R.string.support_mailid_zeemart));
//                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.txt_issue_mistake) + mOrder.getOrderId());
//                        emailIntent.setType("plain/text");
//                        String body = getString(R.string.txt_email_fields_order);
//                        body += String.format(getString(R.string.format_email_fields_order_id), mOrder.getOrderId());
//                        body += String.format(getString(R.string.format_email_fields_supplier), mOrder.getSupplier().getSupplierName());
//                        body += getString(R.string.txt_desc_mistake);
//                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
//                        startActivity(emailIntent);
                    true
                }
                R.id.txt_cancel_orderoption -> {
                    AnalyticsHelper.logAction(
                        this@OrderDetailsActivity,
                        AnalyticsHelper.TAP_ORDER_DETAILS_CANCEL_ORDER
                    )
                    popup.dismiss()
                    createCancelOrderDialog()
                    true
                }
                R.id.txt_reject_order_option -> {
                    popup.dismiss()
                    val dialogMessageReject = getString(R.string.txt_reject_this_order)
                    createConfirmationDialog(dialogMessageReject, false)
                    true
                }
                else -> false
            }
        })
        if (!UserPermission.HasViewPrice()) {
            popupMenu.findItem(R.id.txt_view_as_pdfview).isVisible = false
            popupMenu.findItem(R.id.txt_download_pdfview).isVisible = false
        }
        popup.show()
    }

    /**
     * downloads the Order PDF file to the Downlpoads folder
     */
    private fun downloadPDF() {
        if ((ContextCompat.checkSelfPermission(
                this@OrderDetailsActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            // Permission is not granted
            // Request for permission
            ActivityCompat.requestPermissions(
                this@OrderDetailsActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            val uri = Uri.parse(mOrder!!.pdfURL)
            val request = DownloadManager.Request(uri)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                mOrder!!.orderId + "_zeemart.pdf"
            )
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // to notify when download is complete
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }
    }

    override fun onResume() {
        super.onResume()
        callGetOrderDetails()
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        notificationBroadCastReceiverHelper = NotificationBroadCastReceiverHelper(
            this@OrderDetailsActivity,
            object : NotificationBroadCastReceiverHelper.OnNotificationReceivedListner {
                override fun onNotificationReceived(
                    notificationOrderId: String?,
                    orderStatus: String?,
                ) {
                    if (notificationOrderId.equals(orderId, ignoreCase = true)) {
                        isStatusChanged = true
                        callGetOrderDetails()
                    }
                }
            })
        notificationBroadCastReceiverHelper!!.registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(onComplete)
        notificationBroadCastReceiverHelper!!.unRegisterReceiver()
    }

    //broadcast receiver to listen to download complete action
    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            ZeemartBuyerApp.getToastRed(getString(R.string.txt_pdf_save_to_downloads))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {

                // If request is cancelled, the result arrays are empty.
                if ((grantResults.size > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {

                    // permission was granted
                    downloadPDF()
                } else {

                    // permission denied, boo! Disable the
                }
                return
            }
            else -> {}
        }
    }

    private fun setReceiveLayoutVisibility() {
        if (mOrder!!.isReceived) {
            lytReceive!!.visibility = View.GONE
        } else {
            if (mOrder!!.isPlacedInvoiced) {
                if ((mOrder!!.grn != null) && (mOrder!!.grn?.grnList != null) && (mOrder!!.grn?.grnList?.size!! > 0) && !StringHelper.isStringNullOrEmpty(
                        mOrder!!.grn?.grnList!![0].grnId
                    )
                ) {
                    lytReceive!!.visibility = View.GONE
                } else {
                    lytReceive!!.visibility = View.VISIBLE
                }
            } else {
                lytReceive!!.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed();
        onStatusChanged()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_right)
    }

    private fun onStatusChanged() {
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && (calledFrom == NotificationConstants.CALLED_FROM_ORDER_DETAILS)) {
            startActivity(
                Intent(
                    this@OrderDetailsActivity,
                    BaseNavigationActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && (calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_PAYMENT_CONFIRMATION)) {
            startActivity(
                Intent(
                    this@OrderDetailsActivity,
                    BaseNavigationActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        } else if (!StringHelper.isStringNullOrEmpty(calledFrom) && ((calledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_DEAL_ORDER) || ((calledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ESSENTIAL_ORDER)))) {
            startActivity(
                Intent(
                    this@OrderDetailsActivity,
                    BaseNavigationActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        } else {
            if (isStatusChanged) {
                val orderStatusBeforeCancel = mOrder!!.orderStatus
                val intent = Intent(this@OrderDetailsActivity, ViewOrdersActivity::class.java)
                intent.putExtra(ZeemartAppConstants.ORDER_STATUS, orderStatusBeforeCancel)
                intent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
                setResult(IS_STATUS_CHANGED, intent)
            }
        }
        finish()
    }

    companion object {
        private val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
        @JvmField
        val IS_STATUS_CHANGED = 2
        private val PLACE_HOLDER_USER_IMAGE_WIDTH = CommonMethods.dpToPx(30)
        private val PLACE_HOLDER_USER_IMAGE_HEIGHT = CommonMethods.dpToPx(30)
    }
}