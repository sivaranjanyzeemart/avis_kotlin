package zeemart.asia.buyers.essentials

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.DeliveryDatesHorizontalListAdapter
import zeemart.asia.buyers.adapter.DeliveryInstructionOptionsAdapter
import zeemart.asia.buyers.adapter.DeliveryInstructionOptionsAdapter.DeliveryOptionTappedListener
import zeemart.asia.buyers.adapter.ReviewEssentialsOrderProductListAdapter
import zeemart.asia.buyers.adapter.ReviewEssentialsOrderProductListAdapter.ReviewEssentialsOrderItemChangeListener
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DialogHelper.ShowAddOnOrderDescription
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.interfaces.DeliveryDateSelectedListener
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.OutletSettingModel.DeliveryInstructionIsSelected
import zeemart.asia.buyers.models.marketlist.DeliveryDate
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.Settings.Gst
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.CreateNewOrderRequestModel
import zeemart.asia.buyers.models.orderimport.NewCreateOrderResponse
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderHelper.ValidAddOnOrderListener
import zeemart.asia.buyers.network.OrderManagement
import zeemart.asia.buyers.network.OrderManagement.NewEssentialOrderResponseListener
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.OutletsApi.GetSpecificOutletResponseListener
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.createorders.AddPromoCodeActivity
import java.util.*

class ReviewEssentialsActivity : AppCompatActivity(), ReviewEssentialsOrderItemChangeListener {
    private var txtReviewEssentialOrderHeading: TextView? = null
    private var btnBack: ImageButton? = null
    private var imgSupplierImage: ImageView? = null
    private var lytSupplierThumbNail: RelativeLayout? = null
    private var txtSupplierThumbNail: TextView? = null
    private var txtSupplierName: TextView? = null
    private var txtStatusTag: TextView? = null
    private var txtStatusValue: TextView? = null
    private var txtDeliverToTag: TextView? = null
    private var txtDeliverToValue: TextView? = null
    private var txtDeliveryDateTag: TextView? = null
    private var txtDeliverDateValue: TextView? = null
    private var imgDeliveryDateWarning: ImageView? = null
    private var txtItemCount: TextView? = null
    private var lstProductEssentials: RecyclerView? = null
    private var txtAddItemHeading: TextView? = null
    private var lytAddMoreSkus: LinearLayout? = null
    private var txtSelectFromCatalog: TextView? = null
    private var txtNotesTag: TextView? = null
    private var edtNotesValue: EditText? = null
    private var txtEstimatedSubtotalTag: TextView? = null
    private var getTxtEstimatedSubtotalValue: TextView? = null
    private var imgWarning: ImageButton? = null
    private var txtGstTag: TextView? = null
    private var txtGstValue: TextView? = null
    private var txtTotalTag: TextView? = null
    private var txtTotalValue: TextView? = null
    private var lytBelowMovError: RelativeLayout? = null
    private var txtMovValue: TextView? = null
    private var txtMovErrorMessage: TextView? = null
    private var lytPlaceOrder: RelativeLayout? = null
    private var txtPlaceOrderText: TextView? = null
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    private var orders: Orders? = null
    private var supplier: Supplier? = null
    private var outlet: Outlet? = null
    private var movPriceDetails: PriceDetails? = null
    private var deliveryDates: List<DeliveryDate>? = null
    private var products: MutableList<Product>? = null
    private val orderSubTotal = PriceDetails()
    private val orderTotal = PriceDetails()
    private val orderGST = PriceDetails()
    private val deliveryFee = PriceDetails()
    private var essentialId: String? = null
    private var lstEssentials: Essential? = null
    private var txtDeliveryInstructionsHeading: TextView? = null
    private var lytAddDeliveryInstruction: RelativeLayout? = null
    private var txtDeliveryInstruction: TextView? = null
    private var imgCancelDeliveryInstructions: ImageView? = null
    private var lytDeliveryInstructionOptionList: RelativeLayout? = null
    private var listDeliveryInstructionOptionList: RecyclerView? = null
    private var txtDeliveryBillingInstruction: TextView? = null
    private var deliveryInstructionList: List<DeliveryInstructionIsSelected>? = null
    private var calledFrom: String? = null
    private lateinit var btnRemovePromoCode: ImageButton
    private var lytPromoCode: RelativeLayout? = null
    private lateinit var lytOrderTotal: RelativeLayout
    private var txtPromoCodeValue: TextView? = null
    private var promoCodeDetails: PromoListUI? = null
    private lateinit var lytPromoOrderChanged: LinearLayout
    private var txtPromoOrderChanged: TextView? = null
    private var txtPromoReapply: TextView? = null
    private var txtPromoCodeText: TextView? = null
    private var txtDeliveryFeeTag: TextView? = null
    private var txtDeliveryFee: TextView? = null
    private var imgWarningMov: ImageButton? = null
    private lateinit var lytAddOnOrder: RelativeLayout
    private var txtAddOnOrderHeading: TextView? = null
    private var txtAddOnOrderContent: TextView? = null
    private var txtWhatsAddOnOrder: TextView? = null
    private var isAddOnOrder = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_essentials)
        setUIAndFont()
        txtStatusValue!!.text = OrderStatus.DRAFT.statusName
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ESSENTIALS_LIST)) {
                lstEssentials = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ESSENTIALS_LIST), Essential::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.REVIEW_ORDER_LIST)) {
                val json = bundle.getString(ZeemartAppConstants.REVIEW_ORDER_LIST)
                orders = ZeemartBuyerApp.gsonExposeExclusive.fromJson(json, Orders::class.java)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_DETAILS)) {
                supplier = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    intent.extras!!.getString(ZeemartAppConstants.SUPPLIER_DETAILS, ""),
                    Supplier::class.java
                )
            }
            if (intent.extras!!.containsKey(ZeemartAppConstants.OUTLET_DETAILS)) {
                outlet = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(
                        ZeemartAppConstants.OUTLET_DETAILS,
                        ""
                    ), Outlet::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.ESSENTIAL_MINIMUM_ORDER_VALUE_DETAILS)) {
                movPriceDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(
                        ZeemartAppConstants.ESSENTIAL_MINIMUM_ORDER_VALUE_DETAILS,
                        ""
                    ), PriceDetails::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.DELIVERY_DATES)) {
                deliveryDates = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(
                        ZeemartAppConstants.DELIVERY_DATES,
                        ""
                    ), object : TypeToken<List<DeliveryDate?>?>() {}.type
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.ESSENTIAL_ID)) {
                essentialId = bundle.getString(ZeemartAppConstants.ESSENTIAL_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        setData()
        /**
         * get the outlet setting to set the delivery instructions
         */
        getOutletSettings(outlet)
        btnBack!!.setOnClickListener { moveToEssentialsProductList() }
        lytAddMoreSkus!!.setOnClickListener {
            val newIntent =
                Intent(this@ReviewEssentialsActivity, EssentialsProductListActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ESSENTIALS_ID, lstEssentials!!.essentialsId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier!!.supplierId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier!!.supplierName)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outlet!!.outletId)
            val essentialList = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstEssentials)
            newIntent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, essentialList)
            val json = ZeemartBuyerApp.gsonExposeExclusive.toJson(products)
            newIntent.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, json)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER
            )
            startActivityForResult(
                newIntent,
                ZeemartAppConstants.RequestCode.ReviewDealOrderActivity
            )
        }
        lytPlaceOrder!!.setOnClickListener {
            lytPlaceOrder!!.isClickable = false
            if (deliveryDates != null && deliveryDates!!.size > 0) {
                for (i in deliveryDates!!.indices) {
                    if (deliveryDates!![i].dateSelected) {
                        val apiParamsHelper = ApiParamsHelper()
                        apiParamsHelper.setSupplierId(supplier!!.supplierId!!)
                        apiParamsHelper.setOutletId(outlet!!.outletId!!)
                        apiParamsHelper.setTimeDelivered(deliveryDates!![i].deliveryDate!!)
                        apiParamsHelper.setOrdertype(ApiParamsHelper.OrderType.DEAL.value)
                        threeDotLoaderWhite!!.visibility = View.VISIBLE
                        OrderHelper.validateAddOnOrderBySupplierId(
                            this@ReviewEssentialsActivity,
                            apiParamsHelper,
                            object : ValidAddOnOrderListener {
                                override fun onSuccessResponse(addOnOrderValidResponse: AddOnOrderValidResponse?) {
                                    if (addOnOrderValidResponse != null && addOnOrderValidResponse.data != null) {
                                        if (isAddOnOrder != addOnOrderValidResponse.data!!.isAddOn) {
                                            isAddOnOrder = addOnOrderValidResponse.data!!.isAddOn
                                            lytPlaceOrder!!.isClickable = true
                                            createCantCreateOrderAlert()
                                        } else {
                                            createOrderConfirmationAlert()
                                        }
                                    } else {
                                        createOrderConfirmationAlert()
                                    }
                                    threeDotLoaderWhite!!.visibility = View.GONE
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    createOrderConfirmationAlert()
                                    threeDotLoaderWhite!!.visibility = View.GONE
                                }
                            })
                    }
                }
            } else {
                createOrderConfirmationAlert()
            }
        }
        listDeliveryInstructionOptionList!!.layoutManager = LinearLayoutManager(this)
        txtDeliveryInstruction!!.setOnClickListener { onAddDeliveryInstructionClicked() }
        imgCancelDeliveryInstructions!!.setOnClickListener { onDeleteDeliveryInstructionClicked() }
        btnRemovePromoCode!!.setOnClickListener { removePromoCodeClicked() }
        lytPromoCode!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(
                    this@ReviewEssentialsActivity,
                    AnalyticsHelper.TAP_ORDER_REVIEW_USE_PROMO_CODE
                )
                val newIntent =
                    Intent(this@ReviewEssentialsActivity, AddPromoCodeActivity::class.java)
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier!!.supplierId)
                newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outlet!!.outletId)
                val subTotalDetails = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderSubTotal)
                newIntent.putExtra(PromoListUI.SUBTOTAL_AMOUNT, subTotalDetails)
                if (promoCodeDetails != null) {
                    if (isOrderQuantityChangedAfterPromoApplied) {
                        promoCodeDetails!!.isPromoSelected = false
                    } else {
                        promoCodeDetails!!.isPromoSelected = true
                    }
                    val promoCodeDetailsString =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(promoCodeDetails)
                    newIntent.putExtra(PromoListUI.PROMO_CODE_DETAILS, promoCodeDetailsString)
                }
                startActivityForResult(
                    newIntent,
                    ZeemartAppConstants.RequestCode.ReviewOrderActivity
                )
            }
        })
    }

    private fun createCantCreateOrderAlert() {
        val builder = AlertDialog.Builder(this@ReviewEssentialsActivity)
        builder.setMessage(getString(R.string.txt_check_again_order))
            .setCancelable(false)
            .setTitle(getString(R.string.txt_cant_create_order))
            .setPositiveButton(getString(R.string.dialog_ok_button_text)) { dialog, id ->
                dialog.dismiss()
                setAddOnOrderVisibility()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun setData() {
        lytDeliveryInstructionOptionList!!.visibility = View.GONE
        if (supplier != null) {
            txtSupplierName!!.text = supplier!!.supplierName
            if (StringHelper.isStringNullOrEmpty(supplier!!.logoURL)) {
                imgSupplierImage!!.visibility = View.INVISIBLE
                lytSupplierThumbNail!!.visibility = View.VISIBLE
                lytSupplierThumbNail!!.setBackgroundColor(
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        supplier!!.supplierName!!, this@ReviewEssentialsActivity
                    )
                )
                txtSupplierThumbNail!!.text =
                    CommonMethods.SupplierThumbNailShortCutText(supplier!!.supplierName!!)
                txtSupplierThumbNail!!.setTextColor(
                    CommonMethods.SupplierThumbNailTextColor(
                        supplier!!.supplierName!!, this@ReviewEssentialsActivity
                    )
                )
            } else {
                imgSupplierImage!!.visibility = View.VISIBLE
                lytSupplierThumbNail!!.visibility = View.GONE
                Picasso.get().load(supplier!!.logoURL).placeholder(R.drawable.placeholder_all)
                    .resize(
                        PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH, PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                    ).into(imgSupplierImage)
            }
        }
        if (orders != null) {
            lstProductEssentials!!.layoutManager = LinearLayoutManager(this)
            lstProductEssentials!!.isNestedScrollingEnabled = false
            products = orders!!.products as MutableList<Product>
            if (products != null && products!!.size > 0) {
                updateTotalNumberOfProductsText(products)
                lstProductEssentials!!.adapter = ReviewEssentialsOrderProductListAdapter(
                    this@ReviewEssentialsActivity,
                    this@ReviewEssentialsActivity,
                    products!!,
                    supplier!!
                )
                calculateTotalPrice()
            }
        }
        if (deliveryDates != null && deliveryDates!!.size > 0) {
            setOrderDeliveryDate()
        }
        if (outlet != null) {
            txtDeliverToValue!!.text = outlet!!.outletName
        }
    }

    private fun updateTotalNumberOfProductsText(product: List<Product>?) {
        if (product!!.size == 1) {
            txtItemCount!!.text = product.size.toString() + " " + getString(R.string.txt_item)
        } else {
            txtItemCount!!.text =
                product.size.toString() + " " + getString(R.string.txt_items)
        }
    }

    private fun setUIAndFont() {
        txtReviewEssentialOrderHeading = findViewById(R.id.txt_review_order_essentials_heading)
        ZeemartBuyerApp.setTypefaceView(
            txtReviewEssentialOrderHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtSupplierName = findViewById(R.id.txt_supplier_name_review_order_essentials)
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtStatusTag = findViewById(R.id.txt_status_essentials_order_tag)
        ZeemartBuyerApp.setTypefaceView(
            txtStatusTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtStatusValue = findViewById(R.id.txt_status_essentials_order_value)
        ZeemartBuyerApp.setTypefaceView(
            txtStatusValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtDeliverToTag = findViewById(R.id.txt_location_tag)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverToTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtDeliverToValue = findViewById(R.id.txt_location_value)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverToValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtDeliveryDateTag = findViewById(R.id.txt_delivery_date_tag)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDateTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtDeliverDateValue = findViewById(R.id.txt_delivery_date_value)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverDateValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtItemCount = findViewById(R.id.txt_item_count)
        ZeemartBuyerApp.setTypefaceView(
            txtItemCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtAddItemHeading = findViewById(R.id.txt_add_items_heading)
        ZeemartBuyerApp.setTypefaceView(
            txtAddItemHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtSelectFromCatalog = findViewById(R.id.txt_select_from_catalogue)
        ZeemartBuyerApp.setTypefaceView(
            txtSelectFromCatalog,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtNotesTag = findViewById(R.id.txt_notes_tag)
        ZeemartBuyerApp.setTypefaceView(
            txtNotesTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        edtNotesValue = findViewById(R.id.edt_notes_value)
        ZeemartBuyerApp.setTypefaceView(
            edtNotesValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtEstimatedSubtotalTag = findViewById(R.id.txt_estimated_total_tag)
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedSubtotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        getTxtEstimatedSubtotalValue = findViewById(R.id.txt_estimated_subtotal_value)
        ZeemartBuyerApp.setTypefaceView(
            getTxtEstimatedSubtotalValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtGstTag = findViewById(R.id.txt_gst_tag)
        ZeemartBuyerApp.setTypefaceView(
            txtGstTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtGstValue = findViewById(R.id.txt_gst_value)
        ZeemartBuyerApp.setTypefaceView(
            txtGstValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtTotalTag = findViewById(R.id.txt_estimated_total)
        ZeemartBuyerApp.setTypefaceView(
            txtTotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtTotalValue = findViewById(R.id.txt_estimated_total_value)
        ZeemartBuyerApp.setTypefaceView(
            txtTotalValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtMovValue = findViewById(R.id.txt_supplier_requires_mov)
        ZeemartBuyerApp.setTypefaceView(
            txtMovValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtMovErrorMessage = findViewById(R.id.txt_error_msg_below_mov)
        ZeemartBuyerApp.setTypefaceView(
            txtMovErrorMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtPlaceOrderText = findViewById(R.id.txt_place_order_text)
        ZeemartBuyerApp.setTypefaceView(
            txtPlaceOrderText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtDeliveryInstructionsHeading = findViewById(R.id.txt_delivery_instructions_heading)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryInstructionsHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        lytAddDeliveryInstruction = findViewById(R.id.lyt_add_delivery_instructions)
        txtDeliveryInstruction = findViewById(R.id.txt_delivery_instruction)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryInstruction,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        imgCancelDeliveryInstructions = findViewById(R.id.btn_delivery_instruction_remove)
        lytDeliveryInstructionOptionList = findViewById(R.id.lyt_delivery_instruction_options)
        listDeliveryInstructionOptionList = findViewById(R.id.lst_delivery_instructions)
        txtDeliveryBillingInstruction =
            findViewById(R.id.txt_delivery_billing_instruction_list_title)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryBillingInstruction,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnBack = findViewById(R.id.btn_essentials_order_back)
        imgSupplierImage = findViewById(R.id.img_supplierImage)
        lytSupplierThumbNail = findViewById(R.id.lyt_supplier_thumbnail)
        txtSupplierThumbNail = findViewById(R.id.txt_supplier_thumbnail)
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierThumbNail,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        lstProductEssentials = findViewById(R.id.lst_essentials_order_products)
        lytAddMoreSkus = findViewById(R.id.lyt_add_more_skus)
        imgWarning = findViewById(R.id.img_warning)
        imgDeliveryDateWarning = findViewById(R.id.img_delivery_date_alert)
        lytBelowMovError = findViewById(R.id.lyt_below_mov_error)
        lytPlaceOrder = findViewById(R.id.lyt_place_order_essentials)
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_review_order_white)
        threeDotLoaderWhite.setVisibility(View.GONE)
        btnRemovePromoCode = findViewById(R.id.btn_remove_promo_code)
        btnRemovePromoCode.setVisibility(View.INVISIBLE)
        txtPromoCodeText = findViewById(R.id.txt_promo_code)
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCodeText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        lytPromoCode = findViewById(R.id.lyt_promo_code)
        lytOrderTotal = findViewById(R.id.lyt_order_total)
        if (UserPermission.HasViewPrice()) {
            lytOrderTotal.setVisibility(View.VISIBLE)
        } else {
            lytOrderTotal.setVisibility(View.GONE)
        }
        txtPromoCodeValue = findViewById(R.id.txt_promo_code_value)
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCodeValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        lytPromoOrderChanged = findViewById(R.id.lyt_promo_order_changed)
        lytPromoOrderChanged.setVisibility(View.GONE)
        txtPromoOrderChanged = findViewById(R.id.txt_promo_order_changed)
        ZeemartBuyerApp.setTypefaceView(
            txtPromoOrderChanged,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtPromoReapply = findViewById(R.id.txt_please_reapply_promo)
        ZeemartBuyerApp.setTypefaceView(
            txtPromoReapply,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtDeliveryFeeTag = findViewById(R.id.txt_txt_delivery_fee_tag)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryFeeTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtDeliveryFee = findViewById(R.id.txt_delivery_fee_value)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryFee,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        imgWarningMov = findViewById(R.id.img_error_mov_essentials)
        lytAddOnOrder = findViewById(R.id.lyt_add_on_order)
        lytAddOnOrder.setVisibility(View.GONE)
        lytAddOnOrder.setOnClickListener(View.OnClickListener { ShowAddOnOrderDescription(this@ReviewEssentialsActivity) })
        txtAddOnOrderHeading = findViewById(R.id.txt_add_on_order_heading)
        txtAddOnOrderContent = findViewById(R.id.txt_add_on_order_content)
        txtWhatsAddOnOrder = findViewById(R.id.txt_whats_add_on_order)
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

    override fun onItemQuantityChanged(selectedProducts: List<Product>?) {
        products = selectedProducts as MutableList<Product>
        calculateTotalPrice()
        updateTotalNumberOfProductsText(products)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun setOrderDeliveryDate() {
        if (deliveryDates != null && deliveryDates!!.size > 0) {
            var timeDelivered: Long = 0
            if (orders != null && orders!!.timeDelivered != null) {
                val latestDeliveryDate = Calendar.getInstance(DateHelper.marketTimeZone)
                val dLatestDeliveryDate = Date(deliveryDates!![0].deliveryDate?.times(1000)!!)
                latestDeliveryDate.time = dLatestDeliveryDate
                val orderDeliveryDate = Calendar.getInstance(DateHelper.marketTimeZone)
                val dOrderDeliveryDate = Date(orders!!.timeDelivered?.times(1000)!!)
                orderDeliveryDate.time = dOrderDeliveryDate
                if (orderDeliveryDate.before(latestDeliveryDate)) {
                    timeDelivered = deliveryDates!![0].deliveryDate!!
                    deliveryDates!![0].dateSelected = true
                } else {
                    timeDelivered = orders!!.timeDelivered!!
                    val dateDayOrderDelivery = DateHelper.getDateInDateMonthYearFormat(
                        timeDelivered,
                        orders!!.outlet?.timeZoneFromOutlet
                    )
                    for (i in deliveryDates!!.indices) {
                        val dateDaySupplierDeliveryDate = DateHelper.getDateInDateMonthYearFormat(
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
            val dateDay = DateHelper.getDateInFullDayDateMonthYearFormat(
                timeDelivered,
                outlet!!.timeZoneFromOutlet
            )
            txtDeliverDateValue!!.text = dateDay
            setDeliveryAlertIcon()
            txtDeliverDateValue!!.setOnClickListener {
                if (deliveryDates != null) createDeliveryDatesSelectionDialog(
                    outlet!!.timeZoneFromOutlet,
                    deliveryDates!!
                )
            }
            validateAddOnOrder(timeDelivered)
        }
    }

    fun setDeliveryAlertIcon() {
        if (deliveryDates == null) return
        for (item in deliveryDates!!) {
            imgDeliveryDateWarning!!.visibility = View.GONE
            if (item.dateSelected) {
                if (item.isPH) {
                    imgDeliveryDateWarning!!.setImageResource(R.drawable.warning_red)
                    imgDeliveryDateWarning!!.visibility = View.VISIBLE
                    return
                } else if (item.isEvePH) {
                    imgDeliveryDateWarning!!.setImageResource(R.drawable.warning_yellow)
                    imgDeliveryDateWarning!!.visibility = View.VISIBLE
                    return
                }
            }
        }
    }

    fun createDeliveryDatesSelectionDialog(timeZone: TimeZone?, dates: List<DeliveryDate>) {
        var isDatePHorEPH = false
        val d = Dialog(this, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_delivery_dates)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val lstDeliveryDateList = d.findViewById<RecyclerView>(R.id.lst_deliveryDatesSelection)
        val txtDeliveryDateHeader1 = d.findViewById<TextView>(R.id.deliveryDates_header_text_1)
        val txtDeliveryDateHeader2 = d.findViewById<TextView>(R.id.deliveryDates_header_text_2)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDateHeader1,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDateHeader2,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        for (i in dates.indices) {
            if (dates[i].isPH || dates[i].isEvePH) {
                isDatePHorEPH = true
                break
            }
        }
        if (isDatePHorEPH) {
            txtDeliveryDateHeader2.visibility = View.VISIBLE
        } else {
            txtDeliveryDateHeader2.visibility = View.GONE
        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        lstDeliveryDateList.layoutManager = layoutManager
        val adapter = DeliveryDatesHorizontalListAdapter(this,
            timeZone!!, dates, object : DeliveryDateSelectedListener {
                override fun deliveryDateSelected(date: String?, deliveryDate: Long?) {
                    d.dismiss()
                    txtDeliverDateValue!!.text = date!!.trim { it <= ' ' }
                    setDeliveryAlertIcon()
                    validateAddOnOrder(deliveryDate!!)
                }
            })
        lstDeliveryDateList.adapter = adapter
        for (i in dates.indices) {
            if (dates[i].dateSelected) {
                lstDeliveryDateList.scrollToPosition(i)
            }
        }
        d.show()
    }

    private fun validateAddOnOrder(timeDelivered: Long) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSupplierId(supplier!!.supplierId!!)
        apiParamsHelper.setOutletId(outlet!!.outletId!!)
        apiParamsHelper.setTimeDelivered(timeDelivered)
        apiParamsHelper.setOrdertype(ApiParamsHelper.OrderType.ESSENTIAL.value)
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        OrderHelper.validateAddOnOrderBySupplierId(
            this,
            apiParamsHelper,
            object : ValidAddOnOrderListener {
                override fun onSuccessResponse(addOnOrderValidResponse: AddOnOrderValidResponse?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    if (addOnOrderValidResponse != null && addOnOrderValidResponse.data != null) {
                        isAddOnOrder = addOnOrderValidResponse.data!!.isAddOn
                        setAddOnOrderVisibility()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    private fun setAddOnOrderVisibility() {
        if (isAddOnOrder) {
            lytAddOnOrder!!.visibility = View.VISIBLE
            lytBelowMovError!!.visibility = View.GONE
            imgWarning!!.visibility = View.GONE
            calculateTotalPrice()
        } else {
            lytAddOnOrder!!.visibility = View.GONE
            calculateTotalPrice()
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
        if (products != null && products!!.size > 0) {
            /**
             * check for the stock available messages, moq and quant 0
             */
            for (i in products!!.indices) {
                if (!products!![i].isItemAvailable) {
                    isPlaceOrder = false
                }
            }
            var isChangePlaceOrderTextBelowMinOrder = false
            lytPlaceOrder!!.setBackgroundColor(resources.getColor(R.color.chart_blue))
            lytPlaceOrder!!.isClickable = true
            var tempOrderSubTotal = 0.0
            var totalPriceCentAllItems = 0.0
            for (i in products!!.indices) {
                val doubleTotalPriceCent = products!![i].unitPrice?.amount?.times(products!![i].quantity)
                totalPriceCentAllItems = totalPriceCentAllItems + doubleTotalPriceCent!!
                tempOrderSubTotal = tempOrderSubTotal + products!![i].totalPrice?.amount!!
            }
            orderSubTotal.amount = tempOrderSubTotal
            getTxtEstimatedSubtotalValue!!.text = PriceDetails(tempOrderSubTotal).displayValue
            setUIForDiscountApplied()
            // defaults gst value to $0
            txtGstValue!!.text = PriceDetails().getDisplayValue(Gst())
            //calculate Delivery Fee
            if (lstEssentials != null) {
                if (lstEssentials!!.deliveryFeePolicy != null && !StringHelper.isStringNullOrEmpty(
                        lstEssentials!!.deliveryFeePolicy?.type
                    ) && lstEssentials!!.deliveryFeePolicy?.type == ServiceConstant.APPLY_FEE && !isAddOnOrder
                ) {
                    if (!StringHelper.isStringNullOrEmpty(lstEssentials!!.deliveryFeePolicy?.condition))
                        if (lstEssentials!!.deliveryFeePolicy?.condition == ServiceConstant.BELOW_MINIMUM_ORDER) {
                        if (orderSubTotal.amount!! < lstEssentials!!.deliveryFeePolicy?.minOrder?.amount!!) {
                            txtDeliveryFeeTag!!.visibility = View.VISIBLE
                            txtDeliveryFee!!.visibility = View.VISIBLE
                            if (lstEssentials!!.deliveryFeePolicy?.fee != null) txtDeliveryFee!!.text =
                                lstEssentials!!.deliveryFeePolicy?.fee?.displayValue
                            txtMovErrorMessage!!.text =
                                resources.getString(R.string.txt_msg_below_mov)
                            val deliveryAmount = String.format(
                                resources.getString(R.string.txt_delivery_fee_order_below),
                                lstEssentials!!.deliveryFeePolicy?.minOrder?.displayValue
                            )
                            txtDeliveryFeeTag!!.text = deliveryAmount
                            if (lstEssentials!!.deliveryFeePolicy?.fee != null) {
                                deliveryFee.amount = lstEssentials!!.deliveryFeePolicy?.fee?.amount
                            } else {
                                deliveryFee.amount = 0.0
                            }
                        } else {
                            txtDeliveryFeeTag!!.visibility = View.GONE
                            txtDeliveryFee!!.visibility = View.GONE
                            deliveryFee.amount = 0.0
                        }
                    } else if (lstEssentials!!.deliveryFeePolicy?.condition == ServiceConstant.ALL_ORDER) {
                        txtDeliveryFeeTag!!.visibility = View.VISIBLE
                        txtDeliveryFee!!.visibility = View.VISIBLE
                        if (lstEssentials!!.deliveryFeePolicy?.fee != null) txtDeliveryFee!!.text =
                            lstEssentials!!.deliveryFeePolicy?.fee?.displayValue
                        txtMovErrorMessage!!.text = resources.getString(R.string.txt_msg_below_mov)
                        txtDeliveryFeeTag!!.setTextColor(resources.getColor(R.color.black))
                        txtDeliveryFeeTag!!.text = getString(R.string.txt_delivery_fee)
                        if (lstEssentials!!.deliveryFeePolicy?.fee != null) {
                            deliveryFee.amount = lstEssentials!!.deliveryFeePolicy?.fee?.amount
                        } else {
                            deliveryFee.amount = 0.0
                        }
                    } else if (lstEssentials!!.deliveryFeePolicy?.condition == ServiceConstant.REJECT_BELOW_MIN_ORDER) {
                        //no delivery fee
                        txtDeliveryFeeTag!!.visibility = View.GONE
                        txtDeliveryFee!!.visibility = View.GONE
                        txtMovErrorMessage!!.text = resources.getString(R.string.txt_msg_below_mov)
                        deliveryFee.amount = 0.0
                    }
                } else {
                    //no delivery fee
                    txtMovErrorMessage!!.text = resources.getString(R.string.txt_msg_below_mov)
                    txtDeliveryFeeTag!!.visibility = View.GONE
                    txtDeliveryFee!!.visibility = View.GONE
                    deliveryFee.amount = 0.0
                }
            }
            if (supplier != null) {
                val supplierSettings = supplier!!.settings
                if (supplierSettings != null && supplierSettings.gst != null) {
                    if (supplierSettings.gst!!.percent != null && supplierSettings.gst!!.percent!! > 0) {
                        txtGstTag!!.text = ZeemartAppConstants.Market.`this`.taxCode
                        if (deliveryFee.amount != 0.0) {
                            //if subtotal remains same and promo code is applied
                            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
                                val orderSubTotalAfterDiscount =
                                    tempOrderSubTotal - promoCodeDetails!!.discount?.amount!!
                                val temSubTotalWithDeliveryFee =
                                    orderSubTotalAfterDiscount + deliveryFee.amount!!
                                val subTotalAfterDiscount = PriceDetails(temSubTotalWithDeliveryFee)
                                txtGstValue!!.text =
                                    subTotalAfterDiscount.getDisplayValue(supplierSettings.gst!!)
                                orderGST.amount =
                                    subTotalAfterDiscount.getAmount(supplierSettings.gst!!)
                            } else {
                                val temSubTotalWithDeliveryFee =
                                    tempOrderSubTotal + deliveryFee.amount!!
                                val subTotalAfterDiscount = PriceDetails(temSubTotalWithDeliveryFee)
                                orderGST.amount =
                                    subTotalAfterDiscount.getAmount(supplierSettings.gst!!)
                                txtGstValue!!.text =
                                    subTotalAfterDiscount.getDisplayValue(supplierSettings.gst!!)
                            }
                        } else {
                            //if subtotal remains same and promo code is applied
                            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
                                val orderSubTotalAfterDiscount =
                                    tempOrderSubTotal - promoCodeDetails!!.discount?.amount!!
                                val subTotalAfterDiscount = PriceDetails(orderSubTotalAfterDiscount)
                                txtGstValue!!.text =
                                    subTotalAfterDiscount.getDisplayValue(supplierSettings.gst!!)
                                orderGST.amount =
                                    subTotalAfterDiscount.getAmount(supplierSettings.gst!!)
                            } else {
                                orderGST.amount = orderSubTotal.getAmount(supplierSettings.gst!!)
                                txtGstValue!!.text =
                                    PriceDetails(tempOrderSubTotal).getDisplayValue(supplierSettings.gst!!)
                            }
                        }
                    } else {
                        txtGstTag!!.text = ZeemartAppConstants.Market.`this`.taxCode
                    }
                } else {
                    txtGstTag!!.text = ZeemartAppConstants.Market.`this`.taxCode
                }
            }
            if (movPriceDetails != null) {
                try {
                    if (orderSubTotal.amount!! < movPriceDetails!!.amount!!) {
                        //not let the user to place the order
                        isChangePlaceOrderTextBelowMinOrder = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (isChangePlaceOrderTextBelowMinOrder) {
                txtMovValue!!.text =
                    getString(R.string.txt_minimum_order_deals, movPriceDetails!!.displayValue)
                if (UserPermission.HasViewPrice() && !isAddOnOrder) {
                    lytBelowMovError!!.visibility = View.VISIBLE
                    imgWarning!!.visibility = View.VISIBLE
                }
                if (lstEssentials != null && lstEssentials!!.deliveryFeePolicy?.isBlockBelowMinOrder!! && !isAddOnOrder) {
                    isPlaceOrder = false
                    lytPlaceOrder!!.isClickable = false
                    lytPlaceOrder!!.setBackgroundColor(resources.getColor(R.color.grey_medium))
                    txtPlaceOrderText!!.text = resources.getString(R.string.send_to_approver)
                    txtMovValue!!.setTextColor(resources.getColor(R.color.pinky_red))
                    imgWarningMov!!.setImageResource(R.drawable.img_red_warning)
                    imgWarning!!.setImageResource(R.drawable.img_red_warning)
                    txtMovErrorMessage!!.text =
                        resources.getString(R.string.deals_below_mov_message)
                } else {
                    isPlaceOrder = true
                    lytPlaceOrder!!.isClickable = true
                    lytPlaceOrder!!.setBackgroundColor(resources.getColor(R.color.green))
                    txtPlaceOrderText!!.text = resources.getString(R.string.send_to_approver)
                    txtMovValue!!.setTextColor(resources.getColor(R.color.black))
                    imgWarningMov!!.setImageResource(R.drawable.warning_yellow_black)
                    imgWarning!!.setImageResource(R.drawable.warning_yellow_black)
                    txtMovErrorMessage!!.text = resources.getString(R.string.txt_msg_below_mov)
                }
            } else {
                lytBelowMovError!!.visibility = View.GONE
                imgWarning!!.visibility = View.GONE
            }
            if (!isPlaceOrder) {
                lytPlaceOrder!!.isClickable = false
                lytPlaceOrder!!.setBackgroundColor(resources.getColor(R.color.grey_medium))
            } else {
                lytPlaceOrder!!.isClickable = true
                lytPlaceOrder!!.setBackgroundColor(resources.getColor(R.color.chart_blue))
            }
            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
                orderTotal.amount =
                    orderSubTotal.amount!! - promoCodeDetails!!.discount?.amount!! + orderGST.amount!! + deliveryFee.amount!!
            } else {
                orderTotal.amount = orderSubTotal.amount!! + orderGST.amount!! + deliveryFee.amount!!
            }
            txtTotalValue!!.text = orderTotal.displayValue
        } else {
            //PNF-130(draft contains no item)
            txtPlaceOrderText!!.setText(R.string.txt_your_draft_is_empty)
            lytPlaceOrder!!.isClickable = false
            lytPlaceOrder!!.setBackgroundColor(resources.getColor(R.color.grey_medium))
            lytBelowMovError!!.visibility = View.GONE
            imgWarning!!.visibility = View.GONE
            txtGstValue!!.text = PriceDetails().displayValue
            getTxtEstimatedSubtotalValue!!.text = PriceDetails().displayValue
            txtTotalValue!!.text = PriceDetails().displayValue
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZeemartAppConstants.RequestCode.ReviewDealOrderActivity) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.hasExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
                        val selectedProductList =
                            data.extras!!.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                        val mProductList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            selectedProductList,
                            Array<Product>::class.java
                        )
                        val mSelectedProductList = Arrays.asList(*mProductList)
                        //logic to add newly added products to the list and check if to remove promo code or not
                        for (i in mSelectedProductList.indices) {
                            var productFound = false
                            for (j in products!!.indices) {
                                if (mSelectedProductList[i].sku == products!![j].sku && mSelectedProductList[i].unitSize == products!![j].unitSize) {
                                    productFound = true
                                    //replace the product with the new product info from product list activity
                                    products!![j] = mSelectedProductList[i]
                                    break
                                }
                            }
                            if (!productFound) {
                                products!!.add(mSelectedProductList[i])
                            }
                        }
                        //logic to remove the  product that was removed from the add new product screen
                        for (i in products!!.indices) {
                            var productFound = false
                            for (j in mSelectedProductList.indices) {
                                if (mSelectedProductList[j].sku == products!![i].sku && mSelectedProductList[j].unitSize == products!![i].unitSize) {
                                    productFound = true
                                    break
                                }
                            }
                            if (!productFound) {
                                products!!.removeAt(i)
                            }
                        }
                        lstProductEssentials!!.adapter =
                            ReviewEssentialsOrderProductListAdapter(this, this, products!!, supplier!!)
                        updateTotalNumberOfProductsText(products)
                        calculateTotalPrice()
                    }
                }
            }
        } else if (requestCode == ZeemartAppConstants.RequestCode.ReviewOrderActivity && resultCode == ZeemartAppConstants.ResultCode.RESULT_OK) {
            val bundle = data!!.extras
            val promoCodeDetailsString = bundle!!.getString(PromoListUI.PROMO_CODE_DETAILS)
            promoCodeDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                promoCodeDetailsString,
                PromoListUI::class.java
            )
            calculateTotalPrice()
        }
    }

    private fun createOrderConfirmationAlert() {
        val builder = AlertDialog.Builder(this@ReviewEssentialsActivity)
        val dialog: AlertDialog
        val selected = DeliveryDate.GetSelectedDate(deliveryDates!!)
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
            dialog.setTitle(getString(R.string.txt_continue_with_this_order))
        }
        dialog.setOnCancelListener { lytPlaceOrder!!.isClickable = true }
        dialog.show()
    }

    private fun createPlaceOrderDialogBtn(builder: AlertDialog.Builder) {
        val createOrderYesButton: String
        createOrderYesButton = getString(R.string.txt_yes_continue)
        builder.setPositiveButton(createOrderYesButton) { dialog, which ->
            //only create request if order has some products
            if (products!!.size > 0) {
                //create request body json.
                val jsonRequestBody = createNewEssentialsOrderRequestJson(products)
                createNewEssentialsOrderApi(jsonRequestBody, essentialId)
            } else {
                ZeemartBuyerApp.getToastRed(getString(R.string.txt_order_has_no_products))
            }
        }
    }

    private fun createSelectDateDialogBtn(builder: AlertDialog.Builder) {
        builder.setNegativeButton(getString(R.string.dialog_select_diff_date)) { dialog, which ->
            if (deliveryDates != null) createDeliveryDatesSelectionDialog(
                outlet!!.timeZoneFromOutlet,
                deliveryDates!!
            )
            dialog.cancel()
        }
    }

    private fun createCancelDialogBtn(builder: AlertDialog.Builder) {
        builder.setNegativeButton(getString(R.string.dialog_cancel_button_text)) { dialog, which -> dialog.cancel() }
    }

    /**
     * create te json request for the new order
     *
     * @param products
     * @return
     */
    private fun createNewEssentialsOrderRequestJson(products: List<Product>?): String? {
        var requestJson: String? = null
        val createNewOrderRequestModel = CreateNewOrderRequestModel()
        if (products!!.size > 0) {
            var timeDelivered: Long? = 0L
            if (deliveryDates != null && deliveryDates!!.size > 0) {
                for (i in deliveryDates!!.indices) {
                    if (deliveryDates!![i].dateSelected) {
                        timeDelivered = deliveryDates!![i].deliveryDate
                    }
                }
            }
            val deliveryInstruction = returnSelectedDeliveryInstruction()
            if (!StringHelper.isStringNullOrEmpty(deliveryInstruction)) {
                createNewOrderRequestModel.deliveryInstruction = deliveryInstruction
            }
            createNewOrderRequestModel.timeDelivered = timeDelivered
            var essentialProducts: List<CreateNewOrderRequestModel.Product?>? = ArrayList()
            essentialProducts = ZeemartBuyerApp.fromJsonList(
                ZeemartBuyerApp.gsonExposeExclusive.toJson(products),
                CreateNewOrderRequestModel.Product::class.java,
                object : TypeToken<List<CreateNewOrderRequestModel.Product?>?>() {}.type
            )
            if (promoCodeDetails != null && !isOrderQuantityChangedAfterPromoApplied) {
                createNewOrderRequestModel.promoCode = promoCodeDetails!!.promoCode
            } else {
                createNewOrderRequestModel.promoCode = ""
            }
            createNewOrderRequestModel.products = essentialProducts
            createNewOrderRequestModel.notes = edtNotesValue!!.text.toString()
        }
        requestJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(createNewOrderRequestModel)
        return requestJson
    }

    private fun createNewEssentialsOrderApi(jsonRequestBody: String?, essentialId: String?) {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        OrderManagement.createOrderFromEssentialsForBuyers(
            this,
            outlet!!.outletId,
            jsonRequestBody,
            essentialId,
            object : NewEssentialOrderResponseListener {
                override fun onSuccessResponse(newOrderResponse: NewCreateOrderResponse?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    if (newOrderResponse != null && newOrderResponse.data != null) {
                        AnalyticsHelper.logAction(
                            this@ReviewEssentialsActivity,
                            AnalyticsHelper.TAP_ORDER_REVIEW_PLACE_ORDER_ESSENTIALS,
                            newOrderResponse.data!!,
                            essentialId
                        )
                        val orderResponse = newOrderResponse.data
                        if (!StringHelper.isStringNullOrEmpty(orderResponse?.orderId)) {
                            val totalSuccessfulOrdersPlaced =
                                SharedPref.readInt(SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS, 0)
                            SharedPref.writeInt(
                                SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                                totalSuccessfulOrdersPlaced?.plus(1)!!
                            )
                            //go to order detail screen
                            val newIntent = Intent(
                                this@ReviewEssentialsActivity,
                                OrderDetailsActivity::class.java
                            )
                            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, orderResponse?.orderId)
                            newIntent.putExtra(
                                ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                orderResponse?.outlet?.outletId
                            )
                            newIntent.putExtra(
                                ZeemartAppConstants.CALLED_FROM,
                                ZeemartAppConstants.CALLED_FROM_REVIEW_ESSENTIAL_ORDER
                            )
                            startActivity(newIntent)
                        }
                        finish()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    ZeemartBuyerApp.getToastRed(error?.message)
                }
            })
    }

    private fun getOutletSettings(outlet: Outlet?) {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        OutletsApi.getSpecificOutlet(this, object : GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                threeDotLoaderWhite!!.visibility = View.GONE
                if (specificOutlet != null && specificOutlet.data != null && specificOutlet.data!!.settings != null) defaultSettingDeliveryInstructionSpecialNotes(
                    specificOutlet.data!!.settings!!
                )
            }

            override fun onError(error: VolleyErrorHelper?) {
                threeDotLoaderWhite!!.visibility = View.GONE
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
            txtNotesTag!!.visibility = View.VISIBLE
            edtNotesValue!!.visibility = View.VISIBLE
        } else {
            txtNotesTag!!.visibility = View.GONE
            edtNotesValue!!.visibility = View.GONE
        }
        if (settings.enableDeliveryInstruction != null && settings.enableDeliveryInstruction!!) {
            txtDeliveryInstructionsHeading!!.visibility = View.VISIBLE
            lytAddDeliveryInstruction!!.visibility = View.VISIBLE
            deliveryInstructionList = settings.deliveryInstructionSelectionList
            setDeleteDeliveryInstructionButton()
            listDeliveryInstructionOptionList!!.adapter = DeliveryInstructionOptionsAdapter(this,
                deliveryInstructionList!!, object : DeliveryOptionTappedListener {
                    override fun onDeliveryOptionTapped() {
                        Handler().postDelayed({
                            lytDeliveryInstructionOptionList!!.visibility = View.GONE
                        }, 500)
                        setDeleteDeliveryInstructionButton()
                    }
                })
        } else {
            txtDeliveryInstructionsHeading!!.visibility = View.GONE
            lytAddDeliveryInstruction!!.visibility = View.GONE
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
                txtDeliveryInstruction!!.text = selectedDeliveryInstruction
            } else {
                imgCancelDeliveryInstructions!!.visibility = View.INVISIBLE
                txtDeliveryInstruction!!.text = getString(R.string.txt_add_instructions)
            }
        }
    }

    private fun onAddDeliveryInstructionClicked() {
        lytDeliveryInstructionOptionList!!.visibility = View.VISIBLE
        setDeleteDeliveryInstructionButton()
    }

    private fun onDeleteDeliveryInstructionClicked() {
        if (deliveryInstructionList != null && deliveryInstructionList!!.size > 0) {
            for (i in deliveryInstructionList!!.indices) {
                deliveryInstructionList!![i].isSelected = false
            }
            if (listDeliveryInstructionOptionList!!.adapter != null) listDeliveryInstructionOptionList!!.adapter!!
                .notifyDataSetChanged()
            setDeleteDeliveryInstructionButton()
        }
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

    private fun moveToEssentialsProductList() {
        if (lytDeliveryInstructionOptionList!!.visibility == View.VISIBLE) {
            lytDeliveryInstructionOptionList!!.visibility = View.GONE
        } else {
            if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.txt_leave_this_page)
                builder.setMessage(R.string.txt_essential_items_not_saved)
                builder.setPositiveButton(resources.getString(R.string.txt_stay_here)) { dialog, which -> dialog.dismiss() }
                builder.setNegativeButton(resources.getString(R.string.txt_leave)) { dialog, which -> finish() }
                val d = builder.create()
                d.show()
                val negativeButton = d.getButton(DialogInterface.BUTTON_NEGATIVE)
                negativeButton.setTextColor(resources.getColor(R.color.pinky_red))
                val positiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE)
                positiveButton.setTextColor(resources.getColor(R.color.text_blue))
            } else {
                val newIntent =
                    Intent(this@ReviewEssentialsActivity, EssentialsProductListActivity::class.java)
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier!!.supplierId)
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier!!.supplierName)
                newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outlet!!.outletId)
                val essentialList = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstEssentials)
                newIntent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, essentialList)
                val json = ZeemartBuyerApp.gsonExposeExclusive.toJson(products)
                newIntent.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, json)
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER
                )
                setResult(RESULT_OK, newIntent)
                finish()
            }
        }
    }

    private fun setUIForDiscountApplied() {
        if (promoCodeDetails == null) {
            //set use promo view
            txtPromoCodeText!!.setTextColor(resources.getColor(R.color.text_blue))
            txtPromoCodeText!!.text = getString(R.string.txt_use_promo_code)
            btnRemovePromoCode!!.visibility = View.INVISIBLE
            txtPromoCodeValue!!.visibility = View.INVISIBLE
            lytPromoOrderChanged!!.visibility = View.GONE
        } else {
            if (isOrderQuantityChangedAfterPromoApplied) {
                txtPromoCodeText!!.setTextColor(resources.getColor(R.color.text_blue))
                txtPromoCodeText!!.text = getString(R.string.txt_use_promo_code)
                btnRemovePromoCode!!.visibility = View.INVISIBLE
                txtPromoCodeValue!!.visibility = View.INVISIBLE
                lytPromoOrderChanged!!.visibility = View.VISIBLE
            } else {
                txtPromoCodeText!!.text = promoCodeDetails!!.promoCode
                txtPromoCodeText!!.setTextColor(resources.getColor(R.color.green))
                btnRemovePromoCode!!.visibility = View.VISIBLE
                txtPromoCodeValue!!.visibility = View.VISIBLE
                txtPromoCodeValue!!.text = promoCodeDetails!!.discountDisplayValue
                lytPromoOrderChanged!!.visibility = View.GONE
            }
        }
    }

    /**
     * Promo code clicked, opens add promo code screen.
     */
    private fun removePromoCodeClicked() {
        txtPromoCodeValue!!.text = ""
        txtPromoCodeText!!.setText(R.string.txt_use_promo_code)
        txtPromoCodeText!!.setTextColor(resources.getColor(R.color.text_blue))
        promoCodeDetails = null
        btnRemovePromoCode!!.visibility = View.INVISIBLE
        calculateTotalPrice()
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

    override fun onBackPressed() {
        moveToEssentialsProductList()
    }

    companion object {
        private val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH = CommonMethods.dpToPx(50)
        private val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT = CommonMethods.dpToPx(50)
    }
}