package zeemart.asia.buyers.orders.createorders

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.DeliveryDatesHorizontalListAdapter
import zeemart.asia.buyers.adapter.DeliveryInstructionOptionsAdapter
import zeemart.asia.buyers.adapter.ReviewOrderProductsListAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.*
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.marketlist.*
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.CreateOrder
import zeemart.asia.buyers.models.orderimport.OrderBaseResponse
import zeemart.asia.buyers.models.orderimport.RetrieveOrderDetails
import zeemart.asia.buyers.models.orderimportimport.*
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.orders.OrderProcessingPlacedActivity
import java.util.*

class ReviewOrderActivity : AppCompatActivity(), ReviewOrderItemChangeListener {
    private lateinit var txtSupplierName: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtDeliverTo: TextView
    private var txtDeliveredOn: TextView? = null
    private lateinit var lstProducts: RecyclerView
    private var txtEstimatedSubtotal: TextView? = null
    private var txtCalculatedGST: TextView? = null
    private var txtDeliveryFee: TextView? = null
    private var txtEstimatedTotalValue: TextView? = null
    private lateinit var lytAddMoreSkus: LinearLayout

    //private TextView txtTotalPriceSendToApprover;
    private lateinit var edtNotes: EditText
    private var productsNewOrder = ArrayList<Product>()
    private var txtTotalNumberOfProducts: TextView? = null
    private lateinit var txtFillToParAction: TextView
    private var lytItems: RelativeLayout? = null
    private var supplier: Supplier? = null
    private var outlet: Outlet? = null
    private lateinit var lytPlaceOrder: RelativeLayout
    private val orderSubTotal: PriceDetails = PriceDetails()
    private val orderTotal: PriceDetails = PriceDetails()
    private val orderGST: PriceDetails = PriceDetails()
    private val deliveryFee: PriceDetails = PriceDetails()
    var lstProductListSupplier: MutableMap<String?, ProductDetailBySupplier> =
        HashMap<String?, ProductDetailBySupplier>()
    private var deliveryDates: List<DeliveryDate>? = null
    private lateinit var nestedScrollView: NestedScrollView
    private var imgSupplier: ImageView? = null
    private val newOrderStatus: OrderStatus? = null
    private var mIsStarting = false
    private var existingDraftOrderId: String? = null
    private var txtEstimatedTotal: TextView? = null
    private lateinit var txtReviewOrderHeading: TextView
    private var txtNotesTag: TextView? = null
    private lateinit var btnDeleteOrder: ImageButton
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    var supplierDetails: DetailSupplierDataModel? = null
    var detailSupplierDataModels: List<DetailSupplierDataModel>? = null
    private var orders: MutableList<Orders>? = null
    private lateinit var txtPlaceOrderText: TextView
    private lateinit var txtGstTag: TextView
    private val moveBack = false
    private var txtStatusTag: TextView? = null
    private var txtDeliverToTag: TextView? = null
    private var txtDeliverOnTag: TextView? = null
    private var txtEstimatedSubtotalTag: TextView? = null
    private var txtDeliveryFeeTag: TextView? = null
    private var imgDeliveryDateAlert: ImageView? = null
    private var txtAddItemsHeading: TextView? = null
    private var txtSelectFromCatalogue: TextView? = null
    private lateinit var txtInputManually: TextView
    var isEditOrder = false
    private var lytWarningSupplierBelowMov: RelativeLayout? = null
    private var txtSupplierRequiresMov: TextView? = null
    private var txtWarningEstimateAmount: TextView? = null
    private var txtWarningMsgBelowMov: TextView? = null
    private var imgWarningEstimateAmount: ImageButton? = null
    private var imgWarningMov: ImageButton? = null
    private var txtDeliveryInstructionsHeading: TextView? = null
    private var lytAddDeliveryInstruction: RelativeLayout? = null
    private lateinit var txtDeliveryInstruction: TextView
    private var imgCancelDeliveryInstructions: ImageView? = null
    private lateinit var lytDeliveryInstructionOptionList: RelativeLayout
    private lateinit var listDeliveryInstructionOptionList: RecyclerView
    private var txtDeliveryBillingInstruction: TextView? = null
    private var txtPromoCodeText: TextView? = null
    private lateinit var btnRemovePromoCode: ImageButton
    private lateinit var lytPromoCode: RelativeLayout
    private lateinit var lytOrderTotal: RelativeLayout
    private var txtPromoCodeValue: TextView? = null
    private var deliveryInstructionList: List<OutletSettingModel.DeliveryInstructionIsSelected>? = null
    private var promoCodeDetails: PromoListUI?= null
    private lateinit var lytPromoOrderChanged: LinearLayout
    private var txtPromoOrderChanged: TextView? = null
    private var txtPromoReapply: TextView? = null
    private lateinit var lstErrors: MutableList<ErrorModel>
    private lateinit var lytAddOnOrder: RelativeLayout
    private var txtAddOnOrderHeading: TextView? = null
    private var txtAddOnOrderContent: TextView? = null
    private var txtWhatsAddOnOrder: TextView? = null
    private var lytSupplierThumbNail: RelativeLayout? = null
    private var txtSupplierThumbNail: TextView? = null
    private var isAddOnOrder = false
    private var posIntegration = false
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_order)
        imgSupplier = findViewById<ImageView>(R.id.img_supplierImage)
        lytSupplierThumbNail = findViewById<RelativeLayout>(R.id.lyt_supplier_thumbnail)
        txtSupplierThumbNail = findViewById<TextView>(R.id.txt_supplier_thumbnail)
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierThumbNail,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtStatus = findViewById<TextView>(R.id.txt_invoice_date_value)
        txtDeliverTo = findViewById<TextView>(R.id.txt_location_value)
        txtReviewOrderHeading = findViewById<TextView>(R.id.txt_review_order_heading)
        txtDeliveredOn = findViewById<TextView>(R.id.txt_linked_to_order_value)
        lstProducts = findViewById<RecyclerView>(R.id.lst_order_products)
        lstProducts.setNestedScrollingEnabled(false)
        txtEstimatedSubtotal = findViewById<TextView>(R.id.txt_estimated_subtotal_value)
        txtCalculatedGST = findViewById<TextView>(R.id.txt_gst_value)
        txtGstTag = findViewById<TextView>(R.id.txt_gst_tag)
        txtGstTag.setText(ZeemartAppConstants.Market.`this`.taxCode)
        txtDeliveryFee = findViewById<TextView>(R.id.txt_delivery_fee_value)
        txtDeliveryFeeTag = findViewById<TextView>(R.id.txt_txt_delivery_fee_tag)
        txtEstimatedTotalValue = findViewById<TextView>(R.id.txt_estimated_total_value)
        txtEstimatedTotal = findViewById<TextView>(R.id.txt_estimated_total)
        lytAddMoreSkus = findViewById<LinearLayout>(R.id.lyt_add_more_skus)
        lytPlaceOrder = findViewById<RelativeLayout>(R.id.lyt_send_to_approver)
        nestedScrollView = findViewById<NestedScrollView>(R.id.review_orders_nestedScrollView)
        lytItems = findViewById<RelativeLayout>(R.id.lyt_items)
        txtTotalNumberOfProducts = findViewById<TextView>(R.id.txt_itemCount)
        txtFillToParAction = findViewById<TextView>(R.id.txt_fill_to_par_action)
        txtFillToParAction.setOnClickListener(View.OnClickListener { clickFillAllToPar() })
        edtNotes = findViewById<EditText>(R.id.edt_notes_value)
        edtNotes.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                AnalyticsHelper.logAction(
                    this@ReviewOrderActivity,
                    AnalyticsHelper.TAP_ORDER_REVIEW_SPECIAL_REQUEST
                )
            }
        })
        imgSupplier = findViewById<ImageView>(R.id.img_supplierImage)
        btnDeleteOrder = findViewById<ImageButton>(R.id.reviewOrder_delete_order)
        threeDotLoaderWhite =
            findViewById<CustomLoadingViewWhite>(R.id.spin_kit_loader_review_order_white)
        txtSupplierName = findViewById<TextView>(R.id.txt_supplier_name_review_order)
        txtPlaceOrderText = findViewById<TextView>(R.id.txt_place_order_text)
        txtNotesTag = findViewById<TextView>(R.id.txt_notes_tag)
        txtStatusTag = findViewById<TextView>(R.id.txt_invoice_date_tag)
        txtDeliverToTag = findViewById<TextView>(R.id.txt_location_tag)
        txtDeliverOnTag = findViewById<TextView>(R.id.txt_linked_to_tag)
        txtEstimatedSubtotalTag = findViewById<TextView>(R.id.txt_estimated_total_tag)
        imgDeliveryDateAlert = findViewById<ImageView>(R.id.img_delivery_date_alert)
        txtAddItemsHeading = findViewById<TextView>(R.id.txt_add_items_heading)
        txtSelectFromCatalogue = findViewById<TextView>(R.id.txt_select_from_catalogue)
        txtInputManually = findViewById<TextView>(R.id.txt_input_manually)
        txtWarningEstimateAmount = findViewById<TextView>(R.id.txt_warning_estimate_amount)
        imgWarningEstimateAmount = findViewById<ImageButton>(R.id.img_warning)
        imgWarningMov = findViewById<ImageButton>(R.id.img_warning_mov)
        lytWarningSupplierBelowMov = findViewById<RelativeLayout>(R.id.lyt_below_mov_warning)
        txtSupplierRequiresMov = findViewById<TextView>(R.id.txt_supplier_requires_mov)
        txtWarningMsgBelowMov = findViewById<TextView>(R.id.txt_warning_msg_below_mov)
        txtDeliveryInstructionsHeading =
            findViewById<TextView>(R.id.txt_delivery_instructions_heading)
        txtDeliveryBillingInstruction =
            findViewById<TextView>(R.id.txt_delivery_billing_instruction_list_title)
        txtDeliveryInstruction = findViewById<TextView>(R.id.txt_delivery_instruction)
        lytPromoOrderChanged = findViewById<LinearLayout>(R.id.lyt_promo_order_changed)
        txtPromoOrderChanged = findViewById<TextView>(R.id.txt_promo_order_changed)
        txtPromoReapply = findViewById<TextView>(R.id.txt_please_reapply_promo)
        lytPromoOrderChanged.setVisibility(View.GONE)
        txtPromoCodeText = findViewById<TextView>(R.id.txt_promo_code)
        txtPromoCodeValue = findViewById<TextView>(R.id.txt_promo_code_value)
        btnRemovePromoCode = findViewById<ImageButton>(R.id.btn_remove_promo_code)
        btnRemovePromoCode.setVisibility(View.INVISIBLE)
        btnRemovePromoCode.setOnClickListener(View.OnClickListener { removePromoCodeClicked() })
        lytOrderTotal = findViewById<RelativeLayout>(R.id.lyt_order_total)
        if (UserPermission.HasViewPrice()) {
            lytOrderTotal.setVisibility(View.VISIBLE)
        } else {
            lytOrderTotal.setVisibility(View.GONE)
        }
        lytPromoCode = findViewById<RelativeLayout>(R.id.lyt_promo_code)
        lytPromoCode.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(
                    this@ReviewOrderActivity,
                    AnalyticsHelper.TAP_ORDER_REVIEW_USE_PROMO_CODE
                )
                mIsStarting = true
                val newIntent = Intent(this@ReviewOrderActivity, AddPromoCodeActivity::class.java)
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier!!.supplierId)
                newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outlet?.outletId)
                val subTotalDetails: String =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(orderSubTotal)
                newIntent.putExtra(PromoListUI.SUBTOTAL_AMOUNT, subTotalDetails)
                if (promoCodeDetails != null) {
                    if (isOrderQuantityChangedAfterPromoApplied) {
                        promoCodeDetails!!.isPromoSelected
                    } else {
                        promoCodeDetails!!.isPromoSelected
                    }
                    val promoCodeDetailsString: String =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(promoCodeDetails)
                    newIntent.putExtra(PromoListUI.PROMO_CODE_DETAILS, promoCodeDetailsString)
                }
                startActivityForResult(
                    newIntent,
                    ZeemartAppConstants.RequestCode.ReviewOrderActivity
                )
            }
        })
        imgCancelDeliveryInstructions =
            findViewById<ImageView>(R.id.btn_delivery_instruction_remove)
        lytAddDeliveryInstruction = findViewById<RelativeLayout>(R.id.lyt_add_delivery_instructions)
        lytDeliveryInstructionOptionList =
            findViewById<RelativeLayout>(R.id.lyt_delivery_instruction_options)
        lytDeliveryInstructionOptionList.setVisibility(View.GONE)
        lytDeliveryInstructionOptionList.setOnClickListener(View.OnClickListener {
            lytDeliveryInstructionOptionList.setVisibility(
                View.GONE
            )
        })
        listDeliveryInstructionOptionList =
            findViewById<RecyclerView>(R.id.lst_delivery_instructions)
        listDeliveryInstructionOptionList.setLayoutManager(LinearLayoutManager(this))
        txtDeliveryInstruction.setOnClickListener(View.OnClickListener { onAddDeliveryInstructionClicked() })
        imgCancelDeliveryInstructions!!.setOnClickListener { onDeleteDeliveryInstructionClicked() }
        lytAddOnOrder = findViewById<RelativeLayout>(R.id.lyt_add_on_order)
        lytAddOnOrder.setVisibility(View.GONE)
        lytAddOnOrder.setOnClickListener(View.OnClickListener {
            DialogHelper.ShowAddOnOrderDescription(
                this@ReviewOrderActivity
            )
        })
        txtAddOnOrderHeading = findViewById<TextView>(R.id.txt_add_on_order_heading)
        txtAddOnOrderContent = findViewById<TextView>(R.id.txt_add_on_order_content)
        txtWhatsAddOnOrder = findViewById<TextView>(R.id.txt_whats_add_on_order)
        setFont()
        if (getIntent().getExtras()?.containsKey(ZeemartAppConstants.REVIEW_ORDER_LIST) == true) {
            val json: String? =
                getIntent().getExtras()?.getString(ZeemartAppConstants.REVIEW_ORDER_LIST)
            orders = ZeemartBuyerApp.gsonExposeExclusive.fromJson<MutableList<Orders>?>(
                json, object : TypeToken<MutableList<Orders>?>() {}.type
            )
        }
        if (getIntent().getExtras()?.containsKey(ZeemartAppConstants.ERROR_DETAILS) == true) {
            val json: String? = getIntent().getExtras()?.getString(ZeemartAppConstants.ERROR_DETAILS)
            lstErrors = ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<ErrorModel>>(
                json,
                object : TypeToken<List<ErrorModel?>?>() {}.type
            ) as MutableList<ErrorModel>
        }
        if (getIntent().getExtras()?.containsKey(ZeemartAppConstants.SUPPLIER_DETAILS) == true) {
            supplier = ZeemartBuyerApp.gsonExposeExclusive.fromJson<Supplier>(
                getIntent().getExtras()?.getString(ZeemartAppConstants.SUPPLIER_DETAILS, ""),
                Supplier::class.java
            )
            if (getIntent().getExtras()
                    ?.containsKey(ZeemartAppConstants.CALLED_FROM) == true && getIntent().getExtras()
                    ?.getString(ZeemartAppConstants.CALLED_FROM) == ZeemartAppConstants.APPROVER_EDIT_ORDER
            ) {
                isEditOrder = true
                txtPlaceOrderText.setText(R.string.txt_save_approve)
            } else if (orders!![0].orderStatus != null && orders!![0].orderStatus == OrderStatus.DRAFT.statusName) {
                existingDraftOrderId = orders!![0].orderId
            }
            txtSupplierName.setText(supplier!!.supplierName)
        }
        if (getIntent().getExtras()?.containsKey(ZeemartAppConstants.OUTLET_DETAILS) == true) {
            outlet = ZeemartBuyerApp.gsonExposeExclusive.fromJson<Outlet>(
                getIntent().getExtras()?.getString(ZeemartAppConstants.OUTLET_DETAILS, ""),
                Outlet::class.java
            )
        }
        if (getIntent().getExtras()?.containsKey(ZeemartAppConstants.EXISTING_DRAFT_ID) == true) {
            existingDraftOrderId =
                getIntent().getExtras()?.getString(ZeemartAppConstants.EXISTING_DRAFT_ID)
        }
        if (isEditOrder) {
            txtReviewOrderHeading.setText(R.string.txt_edit_order)
            btnDeleteOrder.setVisibility(View.GONE)
        } else {
            txtReviewOrderHeading.setText(R.string.txt_new_order)
            btnDeleteOrder.setVisibility(View.VISIBLE)
        }
        txtDeliverTo.setText(outlet?.outletName)
        lstProducts.setLayoutManager(LinearLayoutManager(this))
        lstProducts.setNestedScrollingEnabled(false)
        nestedScrollView.setNestedScrollingEnabled(false)
        nestedScrollView.setVisibility(View.GONE)
        lytPlaceOrder.setVisibility(View.GONE)
        /**
         * get the outlet setting on response will call the subsequent get supplier detail and get order detail
         */
        getOutletSettings(outlet)
        lytAddMoreSkus.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(
                this@ReviewOrderActivity,
                AnalyticsHelper.TAP_ORDER_REVIEW_SELECT_FROM_CATALOG,
                outlet!!,
                supplier!!
            )
            val newIntent = Intent(this@ReviewOrderActivity, AddToOrderActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier!!.supplierId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier!!.supplierName)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outlet?.outletId)
            newIntent.putExtra(ZeemartAppConstants.POS_INTEGRATION, posIntegration)
            if (supplierDetails != null) {
                val supplierDetailInfo: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                    supplierDetails,
                    DetailSupplierDataModel::class.java
                )
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailInfo)
            }
            val supplierListJson: String =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(detailSupplierDataModels)
            SharedPref.write(ZeemartAppConstants.SUPPLIER_LIST, supplierListJson)
            val json: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(productsNewOrder)
            newIntent.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, json)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER
            )
            mIsStarting = true
            startActivityForResult(newIntent, REQUEST_CODE_SELECT_PRODUCTS)
        })
        txtInputManually.setOnClickListener(View.OnClickListener {
            // currently not in use
//                addProductManuallyDialog();
        })
        lytPlaceOrder.setOnClickListener(View.OnClickListener {
            lytPlaceOrder.setClickable(false)
            if (deliveryDates != null && deliveryDates!!.size > 0) {
                for (i in deliveryDates!!.indices) {
                    if (deliveryDates!![i].dateSelected) {
                        val apiParamsHelper = ApiParamsHelper()
                        apiParamsHelper.setSupplierId(supplier!!.supplierId!!)
                        apiParamsHelper.setOutletId(outlet?.outletId!!)
                        deliveryDates!![i].deliveryDate?.let { it1 ->
                            apiParamsHelper.setTimeDelivered(
                                it1
                            )
                        }
                        apiParamsHelper.setOrdertype(ApiParamsHelper.OrderType.SINGLE.value)
                        threeDotLoaderWhite.setVisibility(View.VISIBLE)
                        OrderHelper.validateAddOnOrderBySupplierId(
                            this@ReviewOrderActivity,
                            apiParamsHelper,
                            object : OrderHelper.ValidAddOnOrderListener {
                                override fun onSuccessResponse(addOnOrderValidResponse: AddOnOrderValidResponse?) {
                                    if (addOnOrderValidResponse != null && addOnOrderValidResponse.data != null) {
                                        if (isAddOnOrder != addOnOrderValidResponse.data!!.isAddOn) {
                                            isAddOnOrder = addOnOrderValidResponse.data!!.isAddOn
                                            lytPlaceOrder.setClickable(true)
                                            createCantCreateOrderAlert()
                                        } else {
                                            createOrderConfirmationAlert()
                                        }
                                    } else {
                                        createOrderConfirmationAlert()
                                    }
                                    threeDotLoaderWhite.setVisibility(View.GONE)
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    createOrderConfirmationAlert()
                                    threeDotLoaderWhite.setVisibility(View.GONE)
                                }
                            })
                    }
                }
            } else {
                createOrderConfirmationAlert()
            }
        })
        OrderStatus.SetStatusBackground(
            this@ReviewOrderActivity,
            Orders(OrderStatus.DRAFT.statusName),
            txtStatus
        )
        btnDeleteOrder.setOnClickListener(View.OnClickListener { createDeleteConformationDialog() })
    }

    private fun createCantCreateOrderAlert() {
        val builder = AlertDialog.Builder(this@ReviewOrderActivity)
        builder.setMessage(getString(R.string.txt_check_again_order))
            .setCancelable(false)
            .setTitle(getString(R.string.txt_cant_create_order))
            .setPositiveButton(
                getString(R.string.dialog_ok_button_text),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        dialog.dismiss()
                        setAddOnOrderVisibility()
                    }
                })
        val alert = builder.create()
        alert.show()
    }

    /**
     * Promo code clicked, opens add promo code screen.
     */
    private fun removePromoCodeClicked() {
        txtPromoCodeValue?.setText("")
        txtPromoCodeText?.setText(R.string.txt_use_promo_code)
        txtPromoCodeText?.setTextColor(getResources().getColor(R.color.text_blue))
        promoCodeDetails = null
        btnRemovePromoCode.setVisibility(View.INVISIBLE)
        calculateTotalPrice()
    }

    private fun onDeleteDeliveryInstructionClicked() {
        if (deliveryInstructionList != null && deliveryInstructionList!!.size > 0) {
            for (i in deliveryInstructionList!!.indices) {
                deliveryInstructionList!![i].isSelected = false
            }
            if (listDeliveryInstructionOptionList.getAdapter() != null) listDeliveryInstructionOptionList.getAdapter()!!
                .notifyDataSetChanged()
            setDeleteDeliveryInstructionButton()
        }
    }

    private fun onAddDeliveryInstructionClicked() {
        lytDeliveryInstructionOptionList.setVisibility(View.VISIBLE)
        setDeleteDeliveryInstructionButton()
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtPlaceOrderText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReviewOrderHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedTotalValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPlaceOrderText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNotesTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalNumberOfProducts,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtFillToParAction,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtStatus,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveredOn,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierName,
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
            edtNotes,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedSubtotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtGstTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryFeeTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectFromCatalogue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtInputManually,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddItemsHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtWarningEstimateAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtWarningMsgBelowMov,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryFee,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCalculatedGST,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedSubtotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierRequiresMov,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryInstructionsHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryInstruction,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryBillingInstruction,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCodeText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCodeValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoOrderChanged,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoReapply,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtWhatsAddOnOrder,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddOnOrderContent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddOnOrderHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    /**
     * call back select Product list screen
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mIsStarting = false
        if (requestCode == REQUEST_CODE_SELECT_PRODUCTS) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.hasExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
                        val selectedProductList: String? =
                            data.getExtras()?.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                        val mProductList: Array<Product> =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson<Array<Product>>(
                                selectedProductList,
                                Array<Product>::class.java
                            )
                        val mSelectedProductList = Arrays.asList(*mProductList)
                        //logic to add newly added products to the list and check if to remove promo code or not
                        for (i in mSelectedProductList.indices) {
                            var productFound = false
                            for (j in productsNewOrder.indices) {
                                if (mSelectedProductList[i].sku == productsNewOrder[j].sku && mSelectedProductList[i].unitSize == productsNewOrder[j].unitSize) {
                                    productFound = true
                                    //replace the product with the new product info from product list activity
                                    productsNewOrder[j] = mSelectedProductList[i]
                                    break
                                }
                            }
                            if (!productFound) {
                                productsNewOrder.add(mSelectedProductList[i])
                            }
                        }
                        //logic to remove the  product that was removed from the add new product screen
                        for (i in productsNewOrder.indices) {
                            var productFound = false
                            for (j in mSelectedProductList.indices) {
                                if (mSelectedProductList[j].sku == productsNewOrder[i].sku && mSelectedProductList[j].unitSize == productsNewOrder[i].unitSize) {
                                    productFound = true
                                    break
                                }
                            }
                            if (!productFound) {
                                productsNewOrder.removeAt(i)
                            }
                        }
                        //if the supplier is still listed for that outlet
                        if (lstProductListSupplier.size > 0) {
                            lstProducts.setAdapter(
                                ReviewOrderProductsListAdapter(
                                    this,
                                    this,
                                    productsNewOrder,
                                    lstProductListSupplier,
                                    supplierDetails!!,
                                    lstErrors
                                )
                            )
                            updateTotalNumberOfProductsText()
                            calculateTotalPrice()
                        }
                    }
                }
            }
        } else if (requestCode == ZeemartAppConstants.RequestCode.ReviewOrderActivity && resultCode == ZeemartAppConstants.ResultCode.RESULT_OK) {
            val bundle: Bundle? = data?.getExtras()
            val promoCodeDetailsString: String? = bundle?.getString(PromoListUI.PROMO_CODE_DETAILS)
            promoCodeDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson<PromoListUI>(
                promoCodeDetailsString,
                PromoListUI::class.java
            )
            calculateTotalPrice()
        }
    }

    private fun updateTotalNumberOfProductsText() {
        if (productsNewOrder != null && productsNewOrder.size == 1) {
            txtTotalNumberOfProducts?.setText(productsNewOrder.size.toString() + " " + getString(R.string.txt_item))
        } else {
            txtTotalNumberOfProducts?.setText(productsNewOrder.size.toString() + " " + getString(R.string.txt_items))
        }
        txtFillToParAction.setText(getString(R.string.txt_fill_all_to_par))
    }

    /**
     * call the all product list API for an supplier to pass to the products list adapter for quantity and unit size selection
     * and set the product list adapter.
     */
    private fun callProductListSetProductAdapter() {
        //call the get All Products API to get the MOQ for the products
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSupplierId(supplier!!.supplierId!!)
        outlet?.outletId?.let {
            MarketListApi.retrieveMarketListOutlet(
                this,
                apiParamsHelper,
                it,
                object : ProductListBySupplierListner {
                    override fun onSuccessResponse(productList: ProductListBySupplier?) {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                        nestedScrollView.setVisibility(View.VISIBLE)
                        lytPlaceOrder.setClickable(false)
                        lytPlaceOrder.setBackgroundColor(getResources().getColor(R.color.grey_medium))
                        lytPlaceOrder.setVisibility(View.VISIBLE)
                        if (productList != null && productList.data?.products?.size!! > 0) {
                            val supplierProducts: List<ProductDetailBySupplier>? =
                                productList.data?.products
                            for (i in supplierProducts?.indices!!) {
                                lstProductListSupplier[supplierProducts[i].sku] = supplierProducts[i]
                            }
                            /**
                             * Remove the products from the list which are no longer sold by the supplier
                             */
                            val iterator = productsNewOrder.iterator()
                            while (iterator.hasNext()) {
                                val dummyProduct = iterator.next()
                                if (!lstProductListSupplier.containsKey(dummyProduct.sku)) {
                                    iterator.remove()
                                }
                            }
                            //set the total number of products in the UI
                            if (lstProductListSupplier.size > 0) {
                                //set the adapter for the Product list New Order
                                updateTotalNumberOfProductsText()
                                lstProducts.setAdapter(
                                    ReviewOrderProductsListAdapter(
                                        this@ReviewOrderActivity,
                                        this@ReviewOrderActivity,
                                        productsNewOrder,
                                        lstProductListSupplier,
                                        supplierDetails!!,
                                        lstErrors
                                    )
                                )
                                calculateTotalPrice()
                            }
                        } else {
                            if (lstProductListSupplier.size > 0) {
                                updateTotalNumberOfProductsText()
                                lstProducts.setAdapter(
                                    ReviewOrderProductsListAdapter(
                                        this@ReviewOrderActivity,
                                        this@ReviewOrderActivity,
                                        productsNewOrder,
                                        lstProductListSupplier,
                                        supplierDetails!!,
                                        lstErrors
                                    )
                                )
                                calculateTotalPrice()
                            }
                        }
                        threeDotLoaderWhite.setVisibility(View.GONE)
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                    }
                })
        }
    }

    private fun stockAvailableChangePlaceOrder(error: VolleyError) {
        val jsonMessage: String = String(error.networkResponse.data)
        val stockUnavailableData: StockUnavailableResponseModel =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                jsonMessage,
                StockUnavailableResponseModel::class.java
            )
        if (stockUnavailableData != null && stockUnavailableData.insufficientProducts != null && stockUnavailableData.insufficientProducts!!
                .size!! > 0
        ) {
            for (i in 0 until stockUnavailableData!!.insufficientProducts!!.size) {
                val insufficientProduct: StockUnavailableResponseModel.InsufficientProduct =
                    stockUnavailableData!!.insufficientProducts!!.get(i)
                val productDetailBySupplier: ProductDetailBySupplier? =
                    lstProductListSupplier[insufficientProduct.sku]
                for (j in 0 until insufficientProduct.stocks?.size!!) {
                    for (k in productDetailBySupplier?.stocks?.indices!!) {
                        if (insufficientProduct.stocks?.get(j)?.unitSize
                                .equals(productDetailBySupplier?.stocks?.get(k)?.unitSize)
                        ) {
                            productDetailBySupplier!!.stocks!!.get(k).stockQuantity =
                                insufficientProduct.stocks!!.get(j).stockQuantity
                            productDetailBySupplier!!.stocks!!.get(k).orderedQuantity =
                                insufficientProduct.stocks!!.get(j).orderedQuantity
                            productDetailBySupplier!!.stocks!!.get(k).threshold =
                                insufficientProduct.stocks!!.get(j).threshold
                            break
                        }
                    }
                }
                lstProductListSupplier[insufficientProduct.sku]?.stocks =
                    productDetailBySupplier?.stocks
            }
            lstProducts.setAdapter(
                ReviewOrderProductsListAdapter(
                    this@ReviewOrderActivity,
                    this@ReviewOrderActivity,
                    productsNewOrder,
                    lstProductListSupplier,
                    supplierDetails!!,
                    lstErrors
                )
            )
            //display the dialog that with message some product in order changed
            createItemNowUnAvailableDialog()
        }
    }

    private fun createItemNowUnAvailableDialog() {
        val builder = AlertDialog.Builder(this@ReviewOrderActivity)
        builder.setPositiveButton(
            getString(R.string.dialog_ok_button_text),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(getString(R.string.txt_ops))
        dialog.setMessage(getString(R.string.txt_items_unavailable_please_edit))
        dialog.show()
    }

    override fun onItemQuantityChanged(selectedProducts: List<Product?>?) {
        productsNewOrder = selectedProducts as ArrayList<Product>
        calculateTotalPrice()
        updateTotalNumberOfProductsText()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view: View? = getCurrentFocus()
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * calculate the subtotal, gst and the total price for the order.
     * and also check for enabling and disabling Place Order button
     */
    fun calculateTotalPrice() {
        /**
         * check for the stock available messages, moq and quant 0
         */
        var isPlaceOrder = true
        if (productsNewOrder.size > 0) {
            /**
             * check for the stock available messages, moq and quant 0
             */
            for (i in productsNewOrder.indices) {
                if (!productsNewOrder[i].isItemAvailable) {
                    isPlaceOrder = false
                }
            }
            var isChangePlaceOrderTextBelowMinOrder = false
            lytPlaceOrder.setBackgroundColor(getResources().getColor(R.color.green))
            lytPlaceOrder.setClickable(true)
            var tempOrderSubTotal = 0.0
            var totalPriceCentAllItems = 0.0
            for (i in productsNewOrder.indices) {
                val doubleTotalPriceCent =
                    productsNewOrder[i].unitPrice?.amount!! * productsNewOrder[i].quantity
                totalPriceCentAllItems = totalPriceCentAllItems + doubleTotalPriceCent
                tempOrderSubTotal = tempOrderSubTotal + productsNewOrder[i].totalPrice!!.amount!!
            }
            orderSubTotal.amount = tempOrderSubTotal
            txtEstimatedSubtotal?.setText(PriceDetails(tempOrderSubTotal).displayValue)
            setUIForDiscountApplied()

            // defaults gst value to $0
            txtCalculatedGST?.setText(PriceDetails().getDisplayValue(Settings.Gst()))
            //calculate Delivery Fee
            if (supplierDetails != null) {
                if (supplierDetails!!.deliveryFeePolicy != null && !StringHelper.isStringNullOrEmpty(
                        supplierDetails!!.deliveryFeePolicy!!.type
                    ) && supplierDetails!!.deliveryFeePolicy!!.type == ServiceConstant.APPLY_FEE && !isAddOnOrder
                ) {
                    if (supplierDetails!!.deliveryFeePolicy?.condition == ServiceConstant.BELOW_MINIMUM_ORDER) {
                        if (orderSubTotal.amount!! < supplierDetails!!.deliveryFeePolicy?.minOrder?.amount!!) {
                            txtDeliveryFeeTag?.setVisibility(View.VISIBLE)
                            txtDeliveryFee?.setVisibility(View.VISIBLE)
                            txtDeliveryFee?.setText(supplierDetails!!.deliveryFeePolicy?.fee?.displayValue)
                            txtWarningMsgBelowMov?.setText(getResources().getString(R.string.txt_msg_below_mov))
                            val deliveryAmount: String = kotlin.String.format(
                                getResources().getString(
                                    R.string.txt_delivery_fee_order_below
                                ), supplierDetails!!.deliveryFeePolicy?.minOrder?.displayValue
                            )
                            txtDeliveryFeeTag?.setText(deliveryAmount)
                            deliveryFee.amount = supplierDetails!!.deliveryFeePolicy?.fee?.amount
                        } else {
                            txtDeliveryFeeTag?.setVisibility(View.GONE)
                            txtDeliveryFee?.setVisibility(View.GONE)
                            deliveryFee.amount = 0.0
                        }
                    } else if (supplierDetails!!.deliveryFeePolicy?.condition == ServiceConstant.ALL_ORDER) {
                        txtDeliveryFeeTag?.setVisibility(View.VISIBLE)
                        txtDeliveryFee?.setVisibility(View.VISIBLE)
                        txtDeliveryFee?.setText(supplierDetails!!.deliveryFeePolicy?.fee?.displayValue)
                        txtWarningMsgBelowMov?.setText(getResources().getString(R.string.txt_msg_below_mov))
                        txtDeliveryFeeTag?.setTextColor(getResources().getColor(R.color.black))
                        txtDeliveryFeeTag?.setText(getString(R.string.txt_delivery_fee))
                        deliveryFee.amount = supplierDetails!!.deliveryFeePolicy?.fee?.amount
                    } else if (supplierDetails!!.deliveryFeePolicy?.condition == ServiceConstant.REJECT_BELOW_MIN_ORDER) {
                        //no delivery fee
                        txtDeliveryFeeTag?.setVisibility(View.GONE)
                        txtDeliveryFee?.setVisibility(View.GONE)
                        txtWarningMsgBelowMov?.setText(getResources().getString(R.string.txt_msg_below_mov))
                        deliveryFee.amount = 0.0
                    }
                } else {
                    //no delivery fee
                    txtWarningMsgBelowMov?.setText(getResources().getString(R.string.txt_msg_below_mov))
                    txtDeliveryFeeTag?.setVisibility(View.GONE)
                    txtDeliveryFee?.setVisibility(View.GONE)
                    deliveryFee.amount = 0.0
                }
            }
            if (supplierDetails != null) {
                val supplierSettings: Settings? = supplierDetails!!.supplier.settings
                if (supplierSettings != null && supplierSettings.gst != null) {
                    if (supplierSettings.gst!!.percent != null && supplierSettings.gst!!.percent!! > 0) {
                        //  txtGstTag.setText(ZeemartAppConstants.Market.getThis().getTaxCode() + " " + supplierSettings.getGst().getPercentDisplayValue() + "%");
                        if (deliveryFee.amount != 0.0) {
                            //if subtotal remains same and promo code is applied
                            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
                                val orderSubTotalAfterDiscount: Double? =
                                    tempOrderSubTotal - promoCodeDetails!!.discount?.amount!!
                                val temSubTotalWithDeliveryFee: Double? =
                                    deliveryFee.amount?.let { orderSubTotalAfterDiscount?.plus(it) }
                                val subTotalAfterDiscount = PriceDetails(temSubTotalWithDeliveryFee)
                                txtCalculatedGST?.setText(
                                    subTotalAfterDiscount.getDisplayValue(
                                        supplierSettings.gst!!
                                    )
                                )
                                orderGST.amount =
                                    subTotalAfterDiscount.getAmount(supplierSettings.gst!!)
                            } else {
                                val temSubTotalWithDeliveryFee: Double =
                                    tempOrderSubTotal + deliveryFee!!.amount!!
                                val subTotalAfterDiscount = PriceDetails(temSubTotalWithDeliveryFee)
                                orderGST.amount =
                                    subTotalAfterDiscount.getAmount(supplierSettings.gst!!)
                                txtCalculatedGST?.setText(
                                    subTotalAfterDiscount.getDisplayValue(
                                        supplierSettings.gst!!
                                    )
                                )
                            }
                        } else {
                            //if subtotal remains same and promo code is applied
                            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
                                val orderSubTotalAfterDiscount: Double =
                                    tempOrderSubTotal - promoCodeDetails!!.discount?.amount!!
                                val subTotalAfterDiscount = PriceDetails(orderSubTotalAfterDiscount)
                                txtCalculatedGST?.setText(
                                    subTotalAfterDiscount.getDisplayValue(
                                        supplierSettings.gst!!
                                    )
                                )
                                orderGST.amount =
                                    subTotalAfterDiscount.getAmount(supplierSettings.gst!!)
                            } else {
                                orderGST.amount = orderSubTotal.getAmount(supplierSettings.gst!!)
                                txtCalculatedGST?.setText(
                                    PriceDetails(tempOrderSubTotal).getDisplayValue(
                                        supplierSettings.gst!!
                                    )
                                )
                            }
                        }
                    } else {
                        txtGstTag.setText(ZeemartAppConstants.Market.`this`.taxCode)
                    }
                }
            }
            if (supplierDetails != null) {
                isChangePlaceOrderTextBelowMinOrder = false
                try {
                    if (orderSubTotal.amount!! < supplierDetails!!.deliveryFeePolicy?.minOrder?.amount!!) {
                        //not let the user to place the order
                        isChangePlaceOrderTextBelowMinOrder = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (!isPlaceOrder) {
                lytPlaceOrder.setClickable(false)
                lytPlaceOrder.setBackgroundColor(getResources().getColor(R.color.grey_medium))
                lytWarningSupplierBelowMov?.setVisibility(View.GONE)
                imgWarningEstimateAmount?.setVisibility(View.GONE)
            } else if (isChangePlaceOrderTextBelowMinOrder) {
                val minOrderPrice: String? = supplierDetails?.deliveryFeePolicy?.minOrder?.displayValue
                val displayMsg: String =
                    getString(R.string.txt_warning_below_mov) + " " + minOrderPrice
                txtSupplierRequiresMov?.setText(displayMsg)
                if (UserPermission.HasViewPrice() && !isAddOnOrder) {
                    lytWarningSupplierBelowMov?.setVisibility(View.VISIBLE)
                    imgWarningEstimateAmount?.setVisibility(View.VISIBLE)
                }
                if (supplierDetails != null && supplierDetails!!.deliveryFeePolicy?.isBlockBelowMinOrder == true && !isAddOnOrder) {
                    lytPlaceOrder.setClickable(false)
                    lytPlaceOrder.setBackgroundColor(getResources().getColor(R.color.grey_medium))
                    txtPlaceOrderText.setText(getResources().getString(R.string.send_to_approver))
                    txtSupplierRequiresMov?.setTextColor(getResources().getColor(R.color.pinky_red))
                    imgWarningMov?.setImageResource(R.drawable.img_red_warning)
                    imgWarningEstimateAmount?.setImageResource(R.drawable.img_red_warning)
                    txtWarningMsgBelowMov?.setText(getResources().getString(R.string.deals_below_mov_message))
                } else {
                    lytPlaceOrder.setClickable(true)
                    lytPlaceOrder.setBackgroundColor(getResources().getColor(R.color.green))
                    txtPlaceOrderText.setText(getResources().getString(R.string.send_to_approver))
                    txtSupplierRequiresMov?.setTextColor(getResources().getColor(R.color.black))
                    imgWarningMov?.setImageResource(R.drawable.warning_yellow_black)
                    imgWarningEstimateAmount?.setImageResource(R.drawable.warning_yellow_black)
                    txtWarningMsgBelowMov?.setText(getResources().getString(R.string.txt_msg_below_mov))
                }
            } else {
                lytWarningSupplierBelowMov?.setVisibility(View.GONE)
                imgWarningEstimateAmount?.setVisibility(View.GONE)
                lytPlaceOrder.setClickable(true)
                lytPlaceOrder.setBackgroundColor(getResources().getColor(R.color.green))
                if (isEditOrder) {
                    txtPlaceOrderText.setText(getResources().getString(R.string.txt_save_approve))
                } else {
                    txtPlaceOrderText.setText(getResources().getString(R.string.send_to_approver))
                }
            }
            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) orderTotal.amount =
                orderSubTotal.amount!! - promoCodeDetails!!.discount?.amount!! + orderGST.amount!! + deliveryFee.amount!! else orderTotal.amount =
                orderSubTotal.amount!! + orderGST.amount!! + deliveryFee.amount!!
            txtEstimatedTotalValue?.setText(orderTotal.displayValue)
        } else {
            //PNF-130(draft contains no item)
            txtPlaceOrderText.setText(R.string.txt_your_draft_is_empty)
            lytPlaceOrder.setClickable(false)
            lytPlaceOrder.setBackgroundColor(getResources().getColor(R.color.grey_medium))
            lytWarningSupplierBelowMov?.setVisibility(View.GONE)
            imgWarningEstimateAmount?.setVisibility(View.GONE)
            txtCalculatedGST?.setText(PriceDetails().displayValue)
            txtEstimatedSubtotal?.setText(PriceDetails().displayValue)
            txtEstimatedTotalValue?.setText(PriceDetails().displayValue)
        }
    }

    private fun setUIForDiscountApplied() {
        if (promoCodeDetails == null) {
            //set use promo view
            txtPromoCodeText?.setTextColor(getResources().getColor(R.color.text_blue))
            txtPromoCodeText?.setText(getString(R.string.txt_use_promo_code))
            btnRemovePromoCode.setVisibility(View.INVISIBLE)
            txtPromoCodeValue?.setVisibility(View.INVISIBLE)
            lytPromoOrderChanged.setVisibility(View.GONE)
        } else {
            if (isOrderQuantityChangedAfterPromoApplied) {
                txtPromoCodeText?.setTextColor(getResources().getColor(R.color.text_blue))
                txtPromoCodeText?.setText(getString(R.string.txt_use_promo_code))
                btnRemovePromoCode.setVisibility(View.INVISIBLE)
                txtPromoCodeValue?.setVisibility(View.INVISIBLE)
                lytPromoOrderChanged.setVisibility(View.VISIBLE)
            } else {
                txtPromoCodeText?.setText(promoCodeDetails!!.promoCode)
                txtPromoCodeText?.setTextColor(getResources().getColor(R.color.green))
                btnRemovePromoCode.setVisibility(View.VISIBLE)
                txtPromoCodeValue?.setVisibility(View.VISIBLE)
                txtPromoCodeValue?.setText(promoCodeDetails!!.discountDisplayValue)
                lytPromoOrderChanged.setVisibility(View.GONE)
            }
        }
    }

    /**
     * create te json request for the new order
     *
     * @param products
     * @return
     */
    private fun createNewOrderRequestJson(products: ArrayList<Product>): String? {
        var requestJson: String? = null
        val newOrderData = CreateOrder()
        var timeDelivered = 0L
        if (deliveryDates != null) {
            for (i in deliveryDates!!.indices) {
                if (deliveryDates!![i].dateSelected) {
                    timeDelivered = deliveryDates!![i].deliveryDate!!
                }
            }
        }
        newOrderData.timeDelivered = (timeDelivered)
        newOrderData.products = (products)
        if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
            newOrderData.promoCode = (promoCodeDetails!!.promoCode)
        } else {
            newOrderData.promoCode = ("")
        }
        newOrderData.notes = (edtNotes.getText().toString())
        val deliveryInstruction = returnSelectedDeliveryInstruction()
        if (!StringHelper.isStringNullOrEmpty(deliveryInstruction)) {
            newOrderData.deliveryInstruction = (deliveryInstruction)
        }
        requestJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(newOrderData)
        return requestJson
    }

    private fun createPlaceOrderDialogBtn(builder: AlertDialog.Builder) {
        val createOrderYesButton: String
        if (isEditOrder) {
            createOrderYesButton = getString(R.string.txt_yes_save_approve_order)
        } else {
            createOrderYesButton = getString(R.string.txt_yes_place_order)
        }
        builder.setPositiveButton(createOrderYesButton, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (isEditOrder) {
                    AnalyticsHelper.logAction(
                        this@ReviewOrderActivity,
                        AnalyticsHelper.TAP_ORDER_REVIEW_APPROVE_ORDER,
                        updatedOrderValues(productsNewOrder)
                    )
                } else {
                    AnalyticsHelper.logAction(
                        this@ReviewOrderActivity,
                        AnalyticsHelper.TAP_ORDER_REVIEW_PLACE_ORDER,
                        updatedOrderValues(productsNewOrder)
                    )
                }
                //only create request if order has some products
                if (!StringHelper.isStringNullOrEmpty(existingDraftOrderId) || isEditOrder) {
                    val jsonrequestEditOrder: String?
                    val jsonrequestEditApproveOrder: String?
                    if (isEditOrder) {
                        jsonrequestEditApproveOrder = createEditOrderRequestJson(productsNewOrder)
                        threeDotLoaderWhite.setVisibility(View.VISIBLE)
                        outlet?.outletId?.let {
                            EditOrder(
                                this@ReviewOrderActivity,
                                object : EditOrder.GetResponseStatusListener {
                                     override fun onSuccessResponse(status: String?) {
                                        val totalSuccessfulOrdersPlaced: Int = SharedPref.readInt(
                                            SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                            0
                                        )!!
                                        SharedPref.writeInt(
                                            SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                            totalSuccessfulOrdersPlaced + 1
                                        )
                                        threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                                        //Log.d("EDIT APPROVE ORDER RESPONSE", newOrderResponse.getOrderId()+"******");
                                        val newIntent = Intent(
                                            this@ReviewOrderActivity,
                                            OrderProcessingPlacedActivity::class.java
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.ORDER_ID,
                                            orders!![0].orderId
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                            outlet?.outletId
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.CALLED_FROM,
                                            ZeemartAppConstants.APPROVER_EDIT_ORDER
                                        )
                                        mIsStarting = true
                                        startActivity(newIntent)
                                        finish()
                                    }

                                    override fun onErrorDraftResponse(
                                        draftError: DraftErrorModel?,
                                        error: VolleyError?,
                                    ) {
                                        threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                                        //check for item unavailable when order placed
                                        if (error?.networkResponse != null && error?.networkResponse!!.statusCode == ServiceConstant.STATUS_CODE_412_INSUFFICIENT_STOCK) {
                                            stockAvailableChangePlaceOrder(error)
                                        } else if (draftError != null) {
                                            if (!StringHelper.isStringNullOrEmpty(draftError.message)) {
                                                val builder =
                                                    AlertDialog.Builder(this@ReviewOrderActivity)
                                                builder.setMessage(draftError.message)
                                                    .setCancelable(false)
                                                    .setPositiveButton(
                                                        getString(R.string.dialog_ok_button_text),
                                                        object : DialogInterface.OnClickListener {
                                                            override fun onClick(
                                                                dialog: DialogInterface,
                                                                id: Int
                                                            ) {
                                                                //if the supplier is still listed for that outlet
                                                                if (lstProductListSupplier.size > 0) {
                                                                    lstProducts.setAdapter(
                                                                        ReviewOrderProductsListAdapter(
                                                                            this@ReviewOrderActivity,
                                                                            this@ReviewOrderActivity,
                                                                            productsNewOrder,
                                                                            lstProductListSupplier,
                                                                            supplierDetails!!,
                                                                            draftError.errors
                                                                        )
                                                                    )
                                                                    updateTotalNumberOfProductsText()
                                                                    calculateTotalPrice()
                                                                }
                                                                dialog.dismiss()
                                                            }
                                                        })
                                                val alert = builder.create()
                                                alert.show()
                                            }
                                        } else {
                                            if (!StringHelper.isStringNullOrEmpty(error?.message)) {
                                                val builder =
                                                    AlertDialog.Builder(this@ReviewOrderActivity)
                                                builder.setMessage(error?.message)
                                                    .setCancelable(false)
                                                    .setPositiveButton(
                                                        getString(R.string.dialog_ok_button_text),
                                                        object : DialogInterface.OnClickListener {
                                                            override fun onClick(
                                                                dialog: DialogInterface,
                                                                id: Int
                                                            ) {
                                                                dialog.dismiss()
                                                            }
                                                        })
                                                val alert = builder.create()
                                                alert.show()
                                            }
                                        }
                                    }
                                }).editApproveOrderData(
                                jsonrequestEditApproveOrder,
                                it,
                                supplier!!.supplierId
                            )
                        }
                    } else {
                        //Edit and Place draft order
                        jsonrequestEditOrder = createEditOrderRequestJson(productsNewOrder)
                        threeDotLoaderWhite.setVisibility(View.VISIBLE)
                        outlet?.outletId?.let {
                            EditOrder(
                                this@ReviewOrderActivity,
                                object : EditOrder.GetResponseStatusListener {
                                    override fun onSuccessResponse(status: String?) {
                                        val totalSuccessfulOrdersPlaced: Int = SharedPref.readInt(
                                            SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                            0
                                        )!!
                                        SharedPref.writeInt(
                                            SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                            totalSuccessfulOrdersPlaced + 1
                                        )
                                        //remove the order from the list which has been successfully placed
                                        threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                                        //Log.d("CREATE ORDER RESPONSE", newOrderResponse.getOrderId()+"******");
                                        val newIntent = Intent(
                                            this@ReviewOrderActivity,
                                            OrderProcessingPlacedActivity::class.java
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.ORDER_ID,
                                            existingDraftOrderId
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                            outlet?.outletId
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.CALLED_FROM,
                                            ZeemartAppConstants.CALLED_FROM_CREATE_NEW_ORDER
                                        )
                                        mIsStarting = true
                                        startActivity(newIntent)
                                        finish()
                                    }

                                    override fun onErrorDraftResponse(
                                        draftErrorModel: DraftErrorModel?,
                                        error: VolleyError?
                                    ) {
                                        threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                                        //check for item unavailable when order placed
                                        if (error?.networkResponse != null && error?.networkResponse!!.statusCode == ServiceConstant.STATUS_CODE_412_INSUFFICIENT_STOCK) {
                                            stockAvailableChangePlaceOrder(error)
                                        } else if (draftErrorModel != null) {
                                            if (!StringHelper.isStringNullOrEmpty(draftErrorModel.message)) {
                                                val builder =
                                                    AlertDialog.Builder(this@ReviewOrderActivity)
                                                builder.setTitle(getString(R.string.txt_cant_create_order_title))
                                                builder.setMessage(draftErrorModel.message)
                                                    .setCancelable(false)
                                                    .setPositiveButton(
                                                        getString(R.string.dialog_ok_button_text),
                                                        object : DialogInterface.OnClickListener {
                                                            override fun onClick(
                                                                dialog: DialogInterface,
                                                                id: Int
                                                            ) {
                                                                //if the supplier is still listed for that outlet
                                                                if (lstProductListSupplier.size > 0) {
                                                                    lstProducts.setAdapter(
                                                                        ReviewOrderProductsListAdapter(
                                                                            this@ReviewOrderActivity,
                                                                            this@ReviewOrderActivity,
                                                                            productsNewOrder,
                                                                            lstProductListSupplier,
                                                                            supplierDetails!!,
                                                                            draftErrorModel.errors
                                                                        )
                                                                    )
                                                                    updateTotalNumberOfProductsText()
                                                                    calculateTotalPrice()
                                                                }
                                                                dialog.dismiss()
                                                            }
                                                        })
                                                val alert = builder.create()
                                                alert.show()
                                            }
                                        } else {
                                            if (!StringHelper.isStringNullOrEmpty(error?.message)) {
                                                val builder =
                                                    AlertDialog.Builder(this@ReviewOrderActivity)
                                                builder.setTitle(getString(R.string.txt_cant_create_order_title))
                                                builder.setMessage(error?.message)
                                                    .setCancelable(false)
                                                    .setPositiveButton(
                                                        getString(R.string.dialog_ok_button_text),
                                                        object : DialogInterface.OnClickListener {
                                                            override fun onClick(
                                                                dialog: DialogInterface,
                                                                id: Int
                                                            ) {
                                                                dialog.dismiss()
                                                            }
                                                        })
                                                val alert = builder.create()
                                                alert.show()
                                            }
                                        }
                                    }
                                }).editOrderData(jsonrequestEditOrder, it)
                        }
                    }
                } else {
                    if (productsNewOrder.size > 0) {
                        //create request body json.
                        val jsonRequestBody = createNewOrderRequestJson(productsNewOrder)
                        createNewOrderApi(jsonRequestBody)
                    } else {
                        ZeemartBuyerApp.getToastRed(getString(R.string.txt_order_has_no_products))
                    }
                }
            }
        })
    }

    private fun createSelectDateDialogBtn(builder: AlertDialog.Builder) {
        builder.setNegativeButton(
            getString(R.string.dialog_select_diff_date),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    txtDeliveredOn?.callOnClick()
                    dialog.cancel()
                }
            })
    }

    private fun createCancelDialogBtn(builder: AlertDialog.Builder) {
        builder.setNegativeButton(
            getString(R.string.dialog_cancel_button_text),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.cancel()
                }
            })
    }

    private fun createOrderConfirmationAlert() {
        val builder = AlertDialog.Builder(this@ReviewOrderActivity)
        val dialog: AlertDialog
        val selected: DeliveryDate? = deliveryDates?.let { DeliveryDate.GetSelectedDate(it) }
        if (selected != null && selected.isPH) {
            createPlaceOrderDialogBtn(builder)
            createSelectDateDialogBtn(builder)
            dialog = builder.create()
            dialog.setTitle(getString(R.string.order_place_order_holiday))
            dialog.setMessage(getString(R.string.order_place_order_holiday_msg))
        } else if (selected != null && selected.isEvePH) {
            createPlaceOrderDialogBtn(builder)
            createSelectDateDialogBtn(builder)
            dialog = builder.create()
            dialog.setTitle(getString(R.string.order_place_order_holiday_eve))
            dialog.setMessage(getString(R.string.order_place_order_holiday_eve_msg))
        } else {
            createPlaceOrderDialogBtn(builder)
            createCancelDialogBtn(builder)
            dialog = builder.create()
            if (isEditOrder) {
                dialog.setTitle(getString(R.string.txt_save_approve_this_order))
            } else {
                dialog.setTitle(getString(R.string.order_place_this_order))
            }
        }
        dialog.setOnCancelListener(object : DialogInterface.OnCancelListener {
            override fun onCancel(dialog: DialogInterface?) {
                lytPlaceOrder.setClickable(true)
            }
        })
        dialog.show()
    }

    fun addProductManuallyDialog() {
        AddProductManuallyDialogHelper.createManuallyAddItemDialog(
            this,
            object : AddProductManuallyDialogHelper.ManuallyAddedProductDataListener {
                override fun onProductDataReceived(productName: String?, quantity: String?) {
                    if (productsNewOrder != null) {
                        val manuallyAddedProduct = Product()
                        manuallyAddedProduct.productName = productName
                        if (!StringHelper.isStringNullOrEmpty(quantity)) {
                            val quant = quantity?.toDouble()
                            manuallyAddedProduct.quantity = (quant!!)
                        }
                        manuallyAddedProduct.isProductManuallyAdded = true
                        productsNewOrder.add(manuallyAddedProduct)
                        //if the supplier is still listed for that outlet
                        if (lstProductListSupplier.size > 0) {
                            lstProducts.setAdapter(
                                ReviewOrderProductsListAdapter(
                                    this@ReviewOrderActivity,
                                    this@ReviewOrderActivity,
                                    productsNewOrder,
                                    lstProductListSupplier,
                                    supplierDetails!!,
                                    lstErrors
                                )
                            )
                            updateTotalNumberOfProductsText()
                            calculateTotalPrice()
                        }
                    }
                }
            })
    }

    fun createDeliveryDatesSelectionDialog(timeZone: TimeZone?, dates: List<DeliveryDate>?) {
        var isDatePHorEPH = false
        val d = Dialog(this, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_delivery_dates)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val lstDeliveryDateList: RecyclerView =
            d.findViewById<RecyclerView>(R.id.lst_deliveryDatesSelection)
        val txtDeliveryDateHeader1: TextView =
            d.findViewById<TextView>(R.id.deliveryDates_header_text_1)
        val txtDeliveryDateHeader2: TextView =
            d.findViewById<TextView>(R.id.deliveryDates_header_text_2)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDateHeader1,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDateHeader2,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        for (i in dates!!.indices) {
            if (dates[i].isPH || dates[i].isEvePH) {
                isDatePHorEPH = true
                break
            }
        }
        if (isDatePHorEPH) {
            txtDeliveryDateHeader2.setVisibility(View.VISIBLE)
        } else {
            txtDeliveryDateHeader2.setVisibility(View.GONE)
        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        lstDeliveryDateList.setLayoutManager(layoutManager)
        val adapter = timeZone?.let {
            DeliveryDatesHorizontalListAdapter(
                this,
                it,
                dates,
                object : DeliveryDateSelectedListener {
                    override fun deliveryDateSelected(date: String?, deliveryDate: Long?) {
                        d.dismiss()
                        txtDeliveredOn?.setText(date?.trim { it <= ' ' })
                        setDeliveryAlertIcon()
                        validateAddOnOrder(deliveryDate!!)
                    }
                })
        }
        lstDeliveryDateList.setAdapter(adapter)
        for (i in dates.indices) {
            if (dates[i].dateSelected) {
                lstDeliveryDateList.scrollToPosition(i)
            }
        }
        d.show()
    }

    override fun onBackPressed() {
        if (productsNewOrder != null && productsNewOrder.size > 0) {
            AnalyticsHelper.logAction(
                this@ReviewOrderActivity,
                AnalyticsHelper.TAP_ORDER_REVIEW_BACK,
                updatedOrderValues(productsNewOrder)
            )
        }
        if (lytDeliveryInstructionOptionList.getVisibility() == View.VISIBLE) {
            lytDeliveryInstructionOptionList.setVisibility(View.GONE)
        } else {
            if (isEditOrder) {
//                when approver edits and press back , there no need to create  a draft
                super.onBackPressed()
            } else {
                if (threeDotLoaderWhite.getVisibility() == View.GONE) createOrEditOrDeletingDraft()
            }
        }
    }

    protected override fun onUserLeaveHint() {
        if (!isEditOrder) {
            if (mIsStarting) {
                Log.d("onUserLeaveHint", " moved to another activity")
            } else {
                createOrEditOrDeletingDraft()
                Log.d("onUserLeaveHint", "Home button pressed")
            }
        }
        super.onUserLeaveHint()
    }

    /**
     * Supplier not linked to this outlet dialog created.
     */
    fun createSupplierNotLinkedDialog() {
        val builder = AlertDialog.Builder(this@ReviewOrderActivity)
        builder.setNeutralButton(
            getResources().getString(R.string.dialog_ok_button_text),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                    finish()
                }
            })
        builder.setMessage(R.string.supplier_delink_message)
        val dialog: Dialog = builder.create()
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
    }

    private fun createOrEditOrDeletingDraft() {
        //create or edit a draft
        if (!StringHelper.isStringNullOrEmpty(existingDraftOrderId)) {
            // editing Draft
            if (productsNewOrder.size == 0) {
                threeDotLoaderWhite.setVisibility(View.VISIBLE)
                //remove from the backend server
                DeleteOrder(this, object : DeleteOrder.GetResponseStatusListener {
                    override fun onSuccessResponse(status: String?) {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                        finish()
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                        finish()
                    }
                }).deleteOrderData(existingDraftOrderId, outlet?.outletId)
            } else {
                threeDotLoaderWhite.setVisibility(View.VISIBLE)
                //edit the draft if there are products
                val jsonRequestEditOrder = createEditOrderRequestJson(productsNewOrder)
                val outlets: MutableList<Outlet?> = ArrayList<Outlet?>()
                outlets.add(outlet)
                OrderManagement.singleDraftOrderUpdate(
                    this@ReviewOrderActivity,
                    jsonRequestEditOrder,
                    outlet?.outletId,
                    outlets as List<Outlet>?,
                    object : OrderManagement.SingleDraftOrderUpdateListener {
                        override fun onSuccessResponse(orderData: OrderBaseResponse.Data?) {
                            OrderHelper.validateAddOnOrderByOrderId(
                                this@ReviewOrderActivity,
                                orderData,
                                object : OrderHelper.ValidAddOnOrderByOrderIdsListener {
                                    override fun onSuccessResponse(draftOrdersBySKUPaginated: OrderBaseResponse.Data?) {
                                        Log.d("", "Edit API called order status changed")
                                        threeDotLoaderWhite.setVisibility(View.GONE)
                                        val intent = Intent(
                                            this@ReviewOrderActivity,
                                            ActivityOrderSummaryPreview::class.java
                                        )
                                        intent.putExtra(
                                            ZeemartAppConstants.CART_DRAFT_LIST,
                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                draftOrdersBySKUPaginated
                                            )
                                        )
                                        setResult(Activity.RESULT_OK, intent)
                                        finish()
                                    }

                                    override fun onErrorResponse(error: VolleyError?) {
                                        Log.d("", "Edit API called order status changed")
                                        threeDotLoaderWhite.setVisibility(View.GONE)
                                        val intent = Intent(
                                            this@ReviewOrderActivity,
                                            ActivityOrderSummaryPreview::class.java
                                        )
                                        intent.putExtra(
                                            ZeemartAppConstants.CART_DRAFT_LIST,
                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(orderData)
                                        )
                                        setResult(Activity.RESULT_OK, intent)
                                        finish()
                                    }
                                })
                        }

                        override fun onErrorResponse(error: VolleyError?) {
                            threeDotLoaderWhite.setVisibility(View.GONE)
                            if (error?.networkResponse != null && error.networkResponse.statusCode == ServiceConstant.STATUS_CODE_412_INSUFFICIENT_STOCK) {
                                stockAvailableChangePlaceOrder(error)
                            } else {
                                finish()
                            }
                        }
                    })
            }
        } else {
            //Create New Draft
            if (productsNewOrder.size > 0) {
                val jsonRequestBody = createNewOrderRequestJson(productsNewOrder)
                createNewDraftOrderApi(jsonRequestBody)
            } else {
                //No Action Required
            }
        }
    }

    private fun createNewOrderApi(jsonRequestBody: String?) {
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        val outletList: MutableList<Outlet?> = ArrayList<Outlet?>()
        outletList.add(outlet)
        CreateNewOrder(this@ReviewOrderActivity, object : NewOrderResponseListener {
            override fun onSuccessResponse(newOrderResponse: CreateNewOrderResponseModel?) {
                threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                if (!StringHelper.isStringNullOrEmpty(newOrderResponse?.orderId) && newOrderResponse?.status
                        .equals(OrderStatus.CREATING.statusName)
                ) {
                    val totalSuccessfulOrdersPlaced: Int =
                        SharedPref.readInt(SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS, 0)!!
                    SharedPref.writeInt(
                        SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                        totalSuccessfulOrdersPlaced + 1
                    )
                    val newIntent =
                        Intent(this@ReviewOrderActivity, OrderProcessingPlacedActivity::class.java)
                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, newOrderResponse?.orderId)
                    newIntent.putExtra(
                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                        outlet?.outletId
                    )
                    newIntent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        ZeemartAppConstants.CALLED_FROM_CREATE_NEW_ORDER
                    )
                    mIsStarting = true
                    startActivity(newIntent)
                }
                finish()
            }


            override fun onErrorResponse(error: VolleyErrorHelper?) {
                threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                if (error?.errorType != null && error.errorMessage != null) {
                    if (error?.errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        ZeemartBuyerApp.getToastRed(error.errorMessage)
                    } else if (error?.networkResponse != null && error.networkResponse.statusCode == ServiceConstant.STATUS_CODE_412_INSUFFICIENT_STOCK) {
                        stockAvailableChangePlaceOrder(error)
                    }
                }
            }
        }).pushNewOrderData(jsonRequestBody!!, outlet!!, supplier!!, outletList as List<Outlet>?)
    }

    // Create new draft order
    private fun createNewDraftOrderApi(jsonRequestBody: String?) {
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        val outletList: MutableList<Outlet?> = ArrayList<Outlet?>()
        outletList.add(outlet)
        CreateNewOrder(this@ReviewOrderActivity, object : NewOrderResponseListener {
            override fun onSuccessResponse(newOrderResponse: CreateNewOrderResponseModel?) {
                threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                if (!StringHelper.isStringNullOrEmpty(newOrderResponse?.orderId) && newOrderStatus?.statusName == OrderStatus.CREATING.statusName) {
                    val totalSuccessfulOrdersPlaced: Int =
                        SharedPref.readInt(SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS, 0)!!
                    SharedPref.writeInt(
                        SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                        totalSuccessfulOrdersPlaced + 1
                    )
                    val newIntent =
                        Intent(this@ReviewOrderActivity, OrderProcessingPlacedActivity::class.java)
                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, newOrderResponse?.orderId)
                    newIntent.putExtra(
                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                        outlet?.outletId
                    )
                    newIntent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        ZeemartAppConstants.CALLED_FROM_CREATE_NEW_ORDER
                    )
                    mIsStarting = true
                    startActivity(newIntent)
                }
                finish()
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {
                threeDotLoaderWhite.setVisibility(View.INVISIBLE)
                if (error?.errorType != null && error.errorMessage != null) {
                    if (error.errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        ZeemartBuyerApp.getToastRed(error.errorMessage)
                    } else if (error.networkResponse != null && error.networkResponse.statusCode == ServiceConstant.STATUS_CODE_412_INSUFFICIENT_STOCK) {
                        stockAvailableChangePlaceOrder(error)
                    }
                }
            }
        }).pushNewDraftOrderData(jsonRequestBody!!, outlet!!, supplier!!, outletList as List<Outlet>?)
    }

    /**
     * create te json request for the new order
     *
     * @param products
     * @return
     */
    private fun createEditOrderRequestJson(products: ArrayList<Product>): String? {
        var requestJson: String? = null
        val newOrderData = Orders()
        if (products.size > 0) {
            var timeDelivered = 0L
            if (deliveryDates != null && deliveryDates!!.size > 0) {
                for (i in deliveryDates!!.indices) {
                    if (deliveryDates!![i].dateSelected) {
                        timeDelivered = deliveryDates!![i].deliveryDate!!
                    }
                }
            }
            newOrderData.timeDelivered = timeDelivered
            newOrderData.products = products
            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
                newOrderData.promoCode = promoCodeDetails!!.promoCode
            } else {
                newOrderData.promoCode = ""
            }
            newOrderData.notes = edtNotes.getText().toString()
        }
        if (isEditOrder) {
            if (orders != null && orders!!.size > 0 && orders!![0].orderId != null) {
                newOrderData.orderId = orders!![0].orderId
            }
        } else {
            newOrderData.orderId = existingDraftOrderId
        }
        val deliveryInstruction = returnSelectedDeliveryInstruction()
        if (!StringHelper.isStringNullOrEmpty(deliveryInstruction)) {
            newOrderData.deliveryInstruction = deliveryInstruction
        }
        requestJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(newOrderData)
        return requestJson
    }

    private fun returnSelectedDeliveryInstruction(): String? {
        var selectedDeliveryInstruction: String? = null
        if (deliveryInstructionList != null && deliveryInstructionList!!.size > 0) {
            for (i in deliveryInstructionList!!.indices) {
                if (deliveryInstructionList!![i].isSelected) {
                    selectedDeliveryInstruction = deliveryInstructionList!![i].deliveryInstruction
                    break
                }
            }
        }
        return selectedDeliveryInstruction
    }

    private fun createDeleteConformationDialog() {
        val builder = AlertDialog.Builder(this@ReviewOrderActivity)
        builder.setPositiveButton(
            getString(R.string.txt_yes),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    var orderId: String? = null
                    AnalyticsHelper.logAction(
                        this@ReviewOrderActivity,
                        AnalyticsHelper.TAP_ORDER_REVIEW_PAGE_DELETE_DRAFT
                    )
                    dialog.dismiss()
                    if (existingDraftOrderId != null) {
                        if (isEditOrder) {
                            if (orders != null && orders!!.size > 0 && orders!![0].orderId != null) {
                                orderId = orders!![0].orderId
                            }
                        } else {
                            orderId = existingDraftOrderId
                        }
                        DeleteOrder(
                            this@ReviewOrderActivity,
                            object : DeleteOrder.GetResponseStatusListener {
                                override fun onSuccessResponse(status: String?) {
                                    dialog.dismiss()
                                    finish()
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    dialog.dismiss()
                                    val errorResponse: BaseResponse? =
                                        ZeemartBuyerApp.fromJson<BaseResponse>(
                                            error.toString(),
                                            BaseResponse::class.java
                                        )
                                    if (error?.networkResponse != null && error.message != null) {
                                        if (error.networkResponse.statusCode.toString() == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                            ZeemartBuyerApp.getToastRed(error.message)
                                        }
                                    }
                                }
                            }).deleteOrderData(orderId, outlet?.outletId)
                    } else {
                        dialog.dismiss()
                        finish()
                    }
                }
            })
        builder.setNeutralButton(
            getString(R.string.txt_no),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(getString(R.string.txt_delete_order))
        dialog.setMessage(getString(R.string.txt_delete_order_sure))
        dialog.show()
    }

    fun setDeliveryAlertIcon() {
        if (deliveryDates == null) return
        for (item in deliveryDates!!) {
            imgDeliveryDateAlert!!.visibility = View.GONE
            if (item.dateSelected) {
                if (item.isPH) {
                    imgDeliveryDateAlert!!.setImageResource(R.drawable.warning_red)
                    imgDeliveryDateAlert!!.visibility = View.VISIBLE
                    return
                } else if (item.isEvePH) {
                    imgDeliveryDateAlert!!.setImageResource(R.drawable.warning_yellow)
                    imgDeliveryDateAlert!!.visibility = View.VISIBLE
                    return
                }
            }
        }
    }

    private fun getOutletSettings(outlet: Outlet?) {
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        val outlets: MutableList<Outlet?> = ArrayList<Outlet?>()
        outlets.add(outlet)
        OutletsApi.getSpecificOutlet(this, object : OutletsApi.GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                posIntegration =
                    if (specificOutlet?.data!!.posIntegration != null && specificOutlet?.data!!.posIntegration?.posIntegration != null) {
                        specificOutlet.data!!.posIntegration?.posIntegration!!
                    }else {
                        false
                    }
                if (specificOutlet != null && specificOutlet.data != null && specificOutlet.data!!.settings != null) specificOutlet.data!!.settings?.let {
                    defaultSettingDeliveryInstructionSpecialNotes(
                        it
                    )
                }
                getSupplierDetails(outlet)
            }

            override fun onError(error: VolleyErrorHelper?) {
                getSupplierDetails(outlet)
            }
        })
    }

    /**
     * default setting after receiving the outlet setting to set the viibility of the views
     *
     * @param settings
     */
    private fun defaultSettingDeliveryInstructionSpecialNotes(settings: OutletSettingModel) {
        if (settings.enableSpecialRequest != null && settings.enableSpecialRequest!!) {
            txtNotesTag?.setVisibility(View.VISIBLE)
            edtNotes.setVisibility(View.VISIBLE)
        } else {
            txtNotesTag?.setVisibility(View.GONE)
            edtNotes.setVisibility(View.GONE)
        }
        if (settings.enableDeliveryInstruction != null && settings.enableDeliveryInstruction!!) {
            txtDeliveryInstructionsHeading?.setVisibility(View.VISIBLE)
            lytAddDeliveryInstruction?.setVisibility(View.VISIBLE)
            deliveryInstructionList = settings.deliveryInstructionSelectionList
            setDeleteDeliveryInstructionButton()
            listDeliveryInstructionOptionList.setAdapter(
                deliveryInstructionList?.let {
                    DeliveryInstructionOptionsAdapter(
                        this,
                        it,
                        object : DeliveryInstructionOptionsAdapter.DeliveryOptionTappedListener {
                            override fun onDeliveryOptionTapped() {
                                Handler().postDelayed(
                                    { lytDeliveryInstructionOptionList.setVisibility(View.GONE) },
                                    500
                                )
                                setDeleteDeliveryInstructionButton()
                            }
                        })
                }
            )
        } else {
            txtDeliveryInstructionsHeading?.setVisibility(View.GONE)
            lytAddDeliveryInstruction?.setVisibility(View.GONE)
        }
    }

    private fun setDeleteDeliveryInstructionButton() {
        if (deliveryInstructionList != null && deliveryInstructionList!!.size > 0) {
            var isRemoveDeliveryInstructionVisible = false
            var selectedDeliveryInstruction: String? = null
            for (i in deliveryInstructionList!!.indices) {
                if (deliveryInstructionList!![i].isSelected) {
                    isRemoveDeliveryInstructionVisible = true
                    selectedDeliveryInstruction = deliveryInstructionList!![i].deliveryInstruction
                    break
                }
            }
            if (isRemoveDeliveryInstructionVisible) {
                imgCancelDeliveryInstructions!!.visibility = View.VISIBLE
                txtDeliveryInstruction.setText(selectedDeliveryInstruction)
            } else {
                imgCancelDeliveryInstructions!!.visibility = View.INVISIBLE
                txtDeliveryInstruction.setText(getString(R.string.txt_add_instructions))
            }
        }
    }

    private fun getSupplierDetails(outlet: Outlet?) {
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outlet?.outletId!!)
        val outlets: MutableList<Outlet?> = ArrayList<Outlet?>()
        outlets.add(outlet)
        MarketListApi.retrieveSupplierListNew(
            this,
            apiParamsHelper,
            outlets as List<Outlet>?,
            object : SupplierListResponseListener {
                override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                    if (supplierList != null && supplierList.size > 0) {
                        for (i in supplierList.indices) {
                            if (supplierList[i]?.supplier?.supplierId == supplier!!.supplierId) {
                                supplierDetails = supplierList[i]
                            }
                        }
                        detailSupplierDataModels = supplierList as List<DetailSupplierDataModel>?
                        if (supplierDetails != null && supplierDetails!!.supplier != null) {
                            val supplierImageURL: String? = supplierDetails!!.supplier.logoURL
                            if (StringHelper.isStringNullOrEmpty(supplierImageURL)) {
                                imgSupplier!!.visibility = View.INVISIBLE
                                lytSupplierThumbNail?.setVisibility(View.VISIBLE)
                                lytSupplierThumbNail?.setBackgroundColor(
                                    CommonMethods.SupplierThumbNailBackgroundColor(
                                        supplierDetails!!.supplier.supplierName!!,
                                        this@ReviewOrderActivity
                                    )
                                )
                                txtSupplierThumbNail?.setText(
                                    CommonMethods.SupplierThumbNailShortCutText(
                                        supplierDetails!!.supplier.supplierName!!
                                    )
                                )
                                txtSupplierThumbNail?.setTextColor(
                                    CommonMethods.SupplierThumbNailTextColor(
                                        supplierDetails!!.supplier.supplierName!!,
                                        this@ReviewOrderActivity
                                    )
                                )
                            } else {
                                imgSupplier!!.visibility = View.VISIBLE
                                lytSupplierThumbNail?.setVisibility(View.GONE)
                                Picasso.get().load(supplierImageURL)
                                    .placeholder(R.drawable.placeholder_all).resize(
                                    PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                                    PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                                ).into(imgSupplier)
                            }
                        }
                        setOrderDeliveryDate()
                        if (orders != null && !StringHelper.isStringNullOrEmpty(orders!![0].orderId)) {
                            threeDotLoaderWhite.setVisibility(View.VISIBLE)
                            getOrderDetails(orders!![0].orderId)
                        } else {
                            for (i in orders!![0].products?.indices!!) {
                                orders!![0].products?.get(i)?.let { productsNewOrder.add(it) }
                            }
                            setNotesAndDeliveryInstructions()
                            callProductListSetProductAdapter()
                        }
                    } else {
                        imgSupplier!!.setImageResource(R.drawable.placeholder_all)
                        //display the dialog that supplier is no longer listed for this outlet
                        createSupplierNotLinkedDialog()
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    threeDotLoaderWhite.setVisibility(View.GONE)
                }
            })
    }

    private fun validateAddOnOrder(timeDelivered: Long) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSupplierId(supplier!!.supplierId!!)
        apiParamsHelper.setOutletId(outlet?.outletId!!)
        apiParamsHelper.setTimeDelivered(timeDelivered)
        apiParamsHelper.setOrdertype(ApiParamsHelper.OrderType.SINGLE.value)
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        OrderHelper.validateAddOnOrderBySupplierId(
            this,
            apiParamsHelper,
            object : OrderHelper.ValidAddOnOrderListener {
                override fun onSuccessResponse(addOnOrderValidResponse: AddOnOrderValidResponse?) {
                    threeDotLoaderWhite.setVisibility(View.GONE)
                    if (addOnOrderValidResponse != null && addOnOrderValidResponse.data != null) {
                        isAddOnOrder = addOnOrderValidResponse.data!!.isAddOn
                        setAddOnOrderVisibility()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite.setVisibility(View.GONE)
                }
            })
    }

    private fun setAddOnOrderVisibility() {
        if (isAddOnOrder) {
            lytAddOnOrder.setVisibility(View.VISIBLE)
            lytWarningSupplierBelowMov?.setVisibility(View.GONE)
            imgWarningEstimateAmount?.setVisibility(View.GONE)
            calculateTotalPrice()
        } else {
            lytAddOnOrder.setVisibility(View.GONE)
            calculateTotalPrice()
        }
    }

    /**
     * sets the notes and delivery instruction in UI
     */
    private fun setNotesAndDeliveryInstructions() {
        if (orders != null && orders!!.size > 0) {
            if (!StringHelper.isStringNullOrEmpty(orders!![0].notes)) {
                edtNotes.setText(orders!![0].notes)
            }
            if (!StringHelper.isStringNullOrEmpty(orders!![0].deliveryInstruction)) {
                txtDeliveryInstruction.setText(orders!![0].deliveryInstruction)
                setDeliveryInstructionAsSelectedInList()
            }
        }
    }

    /**
     * set the value as selected for the order delivery instruction received,
     * in the list of delivery options
     */
    private fun setDeliveryInstructionAsSelectedInList() {
        if (deliveryInstructionList != null && deliveryInstructionList!!.size > 0) {
            for (i in deliveryInstructionList!!.indices) {
                if (deliveryInstructionList!![i].deliveryInstruction == orders!![0].deliveryInstruction) {
                    deliveryInstructionList!![i].isSelected = true
                    setDeleteDeliveryInstructionButton()
                    break
                }
            }
        }
    }

    fun setOrderDeliveryDate() {
        if (supplierDetails != null && supplierDetails!!.deliveryDates != null && supplierDetails!!.deliveryDates?.size!! > 0) {
            deliveryDates = supplierDetails!!.deliveryDates
            var timeDelivered: Long = 0
            if (orders != null && orders!![0].timeDelivered != null) {
                val latestDeliveryDate: Calendar =
                    Calendar.getInstance(DateHelper.marketTimeZone)
                val dLatestDeliveryDate: Date = Date(deliveryDates!![0].deliveryDate?.times(1000)!!)
                latestDeliveryDate.time = dLatestDeliveryDate
                val orderDeliveryDate: Calendar =
                    Calendar.getInstance(DateHelper.marketTimeZone)
                val dOrderDeliveryDate: Date = Date(orders!![0].timeDelivered?.times(1000)!!)
                orderDeliveryDate.time = dOrderDeliveryDate
                if (orderDeliveryDate.before(latestDeliveryDate)) {
                    timeDelivered = deliveryDates!![0].deliveryDate!!
                    deliveryDates!![0].dateSelected = true
                } else {
                    timeDelivered = orders!![0].timeDelivered!!
                    val dateDayOrderDelivery: String = DateHelper.getDateInDateMonthYearFormat(
                        timeDelivered,
                        orders!![0].outlet?.timeZoneFromOutlet
                    )
                    for (i in deliveryDates!!.indices) {
                        val dateDaySupplierDeliveryDate: String =
                            DateHelper.getDateInDateMonthYearFormat(
                                deliveryDates!![i].deliveryDate
                            )
                        if (dateDayOrderDelivery == dateDaySupplierDeliveryDate) {
                            timeDelivered = deliveryDates!![i].deliveryDate!!
                            deliveryDates!![i].dateSelected = true
                            break
                        }
                    }
                }
            } else {
                timeDelivered = deliveryDates!![0].deliveryDate!!
                deliveryDates!![0].dateSelected = true
            }
            val dateDay: String = DateHelper.getDateInFullDayDateMonthYearFormat(
                timeDelivered,
                outlet?.timeZoneFromOutlet
            )
            txtDeliveredOn?.setText(dateDay)
            setDeliveryAlertIcon()
            txtDeliveredOn?.setOnClickListener(View.OnClickListener {
                AnalyticsHelper.logAction(
                    this@ReviewOrderActivity,
                    AnalyticsHelper.TAP_ORDER_REVIEW_DELIVER_ON
                )
                if (deliveryDates != null) createDeliveryDatesSelectionDialog(
                    outlet?.timeZoneFromOutlet,
                    deliveryDates
                )
            })
            validateAddOnOrder(timeDelivered)
        }
    }

    fun getOrderDetails(selectedOrdersIdArray: String?) {
        outlet?.outletId?.let {
            GetOrderDetails(this@ReviewOrderActivity, object :
                GetOrderDetails.GetOrderDetailedDataListener {

                override fun onSuccessResponse(ordersReceived: RetrieveOrderDetails?) {
                    nestedScrollView.setVisibility(View.VISIBLE)
                    //lytPlaceOrder.setVisibility(View.VISIBLE);
                    if (ordersReceived?.data != null) {
                        orders!!.add(ordersReceived.data!!)
                        //set the special request note if available in the response
                        setNotesAndDeliveryInstructions()
                        for (i in orders!!.indices) {
                            val productsInOrderList: List<Product> = orders!![i].products!!
                            for (j in productsInOrderList.indices) {
                                productsInOrderList[j].orderDate = orders!![0].timeUpdated
                                productsNewOrder.add(productsInOrderList[j])
                            }
                        }
                        //PBF-249
                        Collections.sort(productsNewOrder, object : Comparator<Product?> {
                            override fun compare(o1: Product?, o2: Product?): Int {
                                return o2?.orderDate!!.compareTo(o1?.orderDate!!)
                            }
                        })
                        val mergedProductList: HashMap<String, Product> = LinkedHashMap()
                        for (i in productsNewOrder.indices) {
                            val key: String = ProductDataHelper.getKeyProductMap(
                                productsNewOrder[i].sku, productsNewOrder[i].unitSize
                            )
                            if (i == 0) {
                                mergedProductList[key] = productsNewOrder[i]
                            } else {
                                if (!mergedProductList.containsKey(key)) {
                                    mergedProductList[key] = productsNewOrder[i]
                                }
                            }
                        }
                        val valuesList = ArrayList(mergedProductList.values)
                        productsNewOrder.clear()
                        productsNewOrder.addAll(valuesList)
                        //lytPlaceOrder.setVisibility(View.VISIBLE);
                        if (existingDraftOrderId != null || isEditOrder) {
                            if (!StringHelper.isStringNullOrEmpty(orders!![0].promoCode) && orders!![0].amount?.discount != null) {
                                val subTotalAmount: PriceDetails = orders!![0].amount?.subTotal!!
                                val request = ValidatePromoCode(
                                    supplier!!.supplierId,
                                    subTotalAmount,
                                    orders!![0].promoCode
                                )
                                PromoCodeApi.validatePromotionCode(
                                    this@ReviewOrderActivity,
                                    request,
                                    outlet?.outletId,
                                    object : PromoCodeApi.ValidatePromoCodeListener {
                                        override fun onSuccessResponse(response: ValidatePromoCode.Response?) {
                                            //create promo details object
                                            promoCodeDetails =
                                                ZeemartBuyerApp.gsonExposeExclusive.fromJson<PromoListUI>(
                                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(response?.data),
                                                    PromoListUI::class.java
                                                )
                                            promoCodeDetails!!.isPromoSelected
                                            promoCodeDetails!!.orderSubTotalPromoApplied = subTotalAmount
                                            callProductListSetProductAdapter()
                                        }

                                        override fun onErrorResponse(error: VolleyError?) {
                                            callProductListSetProductAdapter()
                                        }
                                    })
                            } else {
                                callProductListSetProductAdapter()
                            }
                        } else {
                            callProductListSetProductAdapter()
                        }
                    } else {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite.setVisibility(View.GONE)
                    if (error?.networkResponse != null && error.message != null) {
                        if (error.networkResponse.statusCode.toString() == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                            ZeemartBuyerApp.getToastRed(error.message)
                        } else if (error.networkResponse != null && error.networkResponse.statusCode == ServiceConstant.STATUS_CODE_412_INSUFFICIENT_STOCK) {
                            stockAvailableChangePlaceOrder(error)
                        }
                    }
                }
            }).getOrderDetailsData(selectedOrdersIdArray, it)
        }
    }

    fun generateJsonRequestBodyWithUpdatedStatus(
        status: String,
        orderId: String
    ): String {
        val order = Orders()
        order.orderStatus = status
        order.orderId = orderId
        order.orderStatus = status
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(order)
    }

    private val isOrderQuantityChangedAfterPromoApplied: Boolean
        private get() {
            if (promoCodeDetails != null) {
                if (promoCodeDetails!!.orderSubTotalPromoApplied?.amount?.toLong() != orderSubTotal.amount?.toLong()) {
                    return true
                }
            }
            return false
        }

    private fun updatedOrderValues(products: ArrayList<Product>): Orders {
        val newOrderData: Orders = orders!![0]
        var timeDelivered = 0L
        var timeCutOff = 0L
        if (deliveryDates != null) {
            for (i in deliveryDates!!.indices) {
                if (deliveryDates!![i].dateSelected) {
                    timeDelivered = deliveryDates!![i].deliveryDate!!
                    timeCutOff = deliveryDates!![i].cutOffDate!!
                }
            }
        }
        newOrderData.timeDelivered = timeDelivered
        newOrderData.timeCutOff = timeCutOff
        newOrderData.products = products
        val amount = OrderAmount()
        val subTotal: PriceDetails = orderSubTotal
        subTotal.currencyCode = (products[0].unitPrice?.currencyCode)
        amount.subTotal = (subTotal)
        val gst: PriceDetails = orderGST
        gst.currencyCode = (products[0].unitPrice?.currencyCode)
        amount.gst = (gst)
        if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
            amount.discount = (promoCodeDetails!!.discount)
            newOrderData.promoCode = promoCodeDetails!!.promoCode
        } else {
            newOrderData.promoCode = ""
        }
        val deliveryFeeObj: PriceDetails = deliveryFee
        deliveryFeeObj.currencyCode = (products[0].unitPrice?.currencyCode)
        amount.deliveryFee = (deliveryFeeObj)
        val orderTotalPriceDetail: PriceDetails = orderTotal
        orderTotalPriceDetail.currencyCode = (products[0].unitPrice?.currencyCode)
        amount.total = (orderTotalPriceDetail)
        newOrderData.amount = amount
        newOrderData.notes = edtNotes.getText().toString()
        val deliveryInstruction = returnSelectedDeliveryInstruction()
        if (!StringHelper.isStringNullOrEmpty(deliveryInstruction)) {
            newOrderData.deliveryInstruction = deliveryInstruction
        }
        return newOrderData
    }

    private fun clickFillAllToPar() {
        var count = 0
        for (i in productsNewOrder.indices) {
            if (lstProductListSupplier.containsKey(productsNewOrder[i].sku)) {
                //Set the MOQ for each Product
                val productPriceList: List<ProductPriceList>? =
                    lstProductListSupplier[productsNewOrder[i].sku]?.priceList
                if (productPriceList != null && lstProductListSupplier[productsNewOrder[i].sku]?.isBelowParLevel == true) {
                    var onHand = 0.0
                    if (productPriceList[0].onHandQty != null) {
                        onHand = productPriceList[0].onHandQty!!
                    }
                    if (productPriceList[0].parLevel != null && productPriceList[0].parLevel != 0.0) {
                        var value: Double? = productPriceList[0].parLevel?.minus(onHand)
                        value = value?.let { Math.ceil(it) }
                        if (value != null) {
                            if (value > productsNewOrder[i].quantity) {
                                count = count + 1
                            }
                        }
                    }
                }
            }
        }
        if (count > 0) {
            createFillAllToParDialog()
        } else {
            createNoParLevelDialog()
        }
    }

    fun createFillAllToParDialog() {
        val builder = AlertDialog.Builder(this@ReviewOrderActivity)
        builder.setMessage(getString(R.string.txt_fill_to_par_overwrite))
            .setCancelable(true)
            .setTitle(getString(R.string.txt_fill_to_par_alert_title))
            .setPositiveButton(
                getString(R.string.txt_fill_alert_yes),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        dialog.dismiss()
                        selectAllToParLevel()
                    }
                })
        createCancelDialogBtn(builder)
        val alert = builder.create()
        alert.show()
    }

    fun selectAllToParLevel() {
        for (i in productsNewOrder.indices) {
            if (lstProductListSupplier.containsKey(productsNewOrder[i].sku)) {
                //Set the MOQ for each Product
                val productPriceList: List<ProductPriceList>? =
                    lstProductListSupplier[productsNewOrder[i].sku]?.priceList
                if (productPriceList != null && lstProductListSupplier[productsNewOrder[i].sku]?.isBelowParLevel == true) {
                    if (productPriceList[0].parLevel != null && productPriceList[0].parLevel != 0.0) {
                        var onHand = 0.0
                        if (productPriceList[0].onHandQty != null) {
                            onHand = productPriceList[0].onHandQty!!
                        }
                        var value: Double? = productPriceList[0].parLevel?.minus(onHand)
                        value = value?.let { Math.ceil(it) }
                        if (value != null) {
                            if (value > productPriceList[0].moq!!) {
                                //  item.quantity = (value);
                                productsNewOrder[i].quantity = (value)
                            } else {
                                productsNewOrder[i].quantity = (productPriceList[0].moq!!)
                            }
                        }
                    }
                }
            }
        }
        lstProducts.setAdapter(
            ReviewOrderProductsListAdapter(
                this@ReviewOrderActivity,
                this@ReviewOrderActivity,
                productsNewOrder,
                lstProductListSupplier,
                supplierDetails!!,
                lstErrors
            )
        )
        calculateTotalPrice()
    }

    fun createNoParLevelDialog() {
        val builder = AlertDialog.Builder(this@ReviewOrderActivity)
        builder.setNeutralButton(
            getResources().getString(R.string.dialog_ok_button_text),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        builder.setTitle(getString(R.string.txt_no_items_to_fill_to_par))
        builder.setMessage(getString(R.string.txt_no_fill_to_par_alert_message))
        val dialog: Dialog = builder.create()
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
    }

    companion object {
        const val REQUEST_CODE_SELECT_PRODUCTS = 1
        private val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH: Int = CommonMethods.dpToPx(50)
        private val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT: Int = CommonMethods.dpToPx(50)
    }
}