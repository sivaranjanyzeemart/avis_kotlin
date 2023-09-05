package zeemart.asia.buyers.orders.createorders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.volley.VolleyError
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SpclDealsImageSliderAdapter
import zeemart.asia.buyers.adapter.SupplierListEssentialsAdapter
import zeemart.asia.buyers.adapter.SupplierListEssentialsOrderAgainAdapter
import zeemart.asia.buyers.adapter.SupplierListNewOrderAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.deals.AboutDealsActivity
import zeemart.asia.buyers.deals.DealProductListActivity
import zeemart.asia.buyers.essentials.EssentialsProductListActivity
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.AnalyticsHelper.logAction
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.ProductDataHelper.createSelectedProductForDraftObject
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.LoaderInteface
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.SharedPref.defaultOutlet
import zeemart.asia.buyers.helper.SharedPref.read
import zeemart.asia.buyers.helper.SharedPref.readBool
import zeemart.asia.buyers.helper.SharedPref.write
import zeemart.asia.buyers.helper.SharedPref.writeBool
import zeemart.asia.buyers.interfaces.ProductListBySupplierListner
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.ProductListBySupplier
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.CreateDraftOrder
import zeemart.asia.buyers.models.orderimportimport.DraftOrdersBySKUPaginated
import zeemart.asia.buyers.models.orderimportimportimport.SpclDealsBaseResponse
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseListener
import zeemart.asia.buyers.network.EssentialsApi.getOrderAgainPaginatedEssentials
import zeemart.asia.buyers.network.EssentialsApi.getPaginatedEssentials
import zeemart.asia.buyers.network.MarketListApi.retrieveMarketListOutlet
import zeemart.asia.buyers.network.MarketListApi.retrieveSupplierListNew
import zeemart.asia.buyers.network.OrderHelper.OutletDraftOrdersListener
import zeemart.asia.buyers.network.OrderHelper.SpclDealsResponseListener
import zeemart.asia.buyers.network.OrderHelper.ValidAddOnOrderByOrderIdListener
import zeemart.asia.buyers.network.OrderHelper.getActiveSpclDeals
import zeemart.asia.buyers.network.OrderHelper.getCartItemsCount
import zeemart.asia.buyers.network.OrderHelper.returnDraftOrdersForOutlet
import zeemart.asia.buyers.network.OrderHelper.validateAddOnOrderByOrderId
import zeemart.asia.buyers.network.OrderManagement.CreateDraftOrderBasedOnSKUsListener
import zeemart.asia.buyers.network.OrderManagement.createDraftOrdersBasedOnSKUs
import zeemart.asia.buyers.network.OutletsApi.GetSpecificOutletResponseListener
import zeemart.asia.buyers.network.OutletsApi.getSpecificOutlet
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.createorders.AddToOrderActivity
import zeemart.asia.buyers.orders.createorders.SearchForNewOrder
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity
import java.util.*

class BrowseCreateNewOrder : AppCompatActivity(), LoaderInteface {
    private var edtSearch: EditText? = null
    private var btnSelectedOutlet: TextView? = null
    private var lytOrderSummary: RelativeLayout? = null
    private var lstSuppliers: RecyclerView? = null
    private var txtTotalItems: TextView? = null
    private var customLoadingViewWhite: CustomLoadingViewWhite? = null
    private var ordersList: List<Orders>? = null
    private var detailSuppliersList: MutableList<DetailSupplierDataModel>? = null
    private var outlet: Outlet? = null
    private var dealsViewPager: ViewPager? = null
    private var timer: Timer? = null
    private var lytDeals: RelativeLayout? = null
    private var txtDealsForYou: TextView? = null
    private var txtDealsDetails: TextView? = null
    private var tabLayout: TabLayout? = null
    private var lytTabBanner: RelativeLayout? = null
    private var lytSupplierHeaders: ConstraintLayout? = null
    private var btnMySuppliers: Button? = null
    private var btnEssentials: Button? = null
    private var txtMySuppliersHighLight: TextView? = null
    private var txtEssentialsHighLight: TextView? = null
    private var lstEssentials: RecyclerView? = null
    private var lstEssentialsSuppliers: MutableList<Essential>? = null
    private var isEssentialTabSelected = false
    private var lytEssentialBanner: RelativeLayout? = null
    private var txtItemsForSupplier: TextView? = null
    private var txtRebateWithEssentialOrder: TextView? = null
    private var txtNew: TextView? = null
    private var isFirstTimeLoading = true
    private var lytNoSuppliers: RelativeLayout? = null
    private var txtNoSuppliers: TextView? = null
    private var txtFindOutMore: TextView? = null
    private var cartItemsCount = 0
    private var isEssentialsLoaded = false
    private var isSuppliersLoaded = false
    private var isDealsLoaded = false
    private var lytEssentialBannerThousandSuppliers: RelativeLayout? = null
    private var lstOrderAgainEssentialSuppliers: RecyclerView? = null
    private var isOrderAgainEssentialSuppliersAvailable = false
    private var txtOrderAgain: TextView? = null
    private var lytOrderAgain: RelativeLayout? = null
    private var txtEssentialsSupplierCount: TextView? = null
    private var posIntegration = false
    private var products: MutableList<ProductDetailBySupplier?>? = null
    override fun onResume() {
        super.onResume()
        lstEssentials!!.adapter = null
        lstSuppliers!!.adapter = null
        lytEssentialBanner!!.visibility = View.GONE
        outletSettings
        refreshSupplierList()
        retrieveActiveDeals()
        retrieveActiveEssentials()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_create_new_order)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        outlet = defaultOutlet
        customLoadingViewWhite = findViewById(R.id.spin_kit_loader_browse_suppliers_white)
        btnSelectedOutlet = findViewById(R.id.txt_outlet_name_browse_new_order)
        btnSelectedOutlet!!.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(this@BrowseCreateNewOrder, SelectOutletActivity::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_NEW_ORDER
            )
            startActivity(newIntent)
        })
        lytOrderSummary = findViewById(R.id.lyt_review_order_browse)
        lytOrderSummary!!.setVisibility(View.GONE)
        txtNew = findViewById(R.id.txt_new)
        lytEssentialBanner = findViewById(R.id.lyt_essential_banner)
        lytEssentialBanner!!.setVisibility(View.GONE)
        txtItemsForSupplier = findViewById(R.id.txt_items_from_trusted_supplier)
        txtRebateWithEssentialOrder = findViewById(R.id.txt_get_up_to_5_percent)
        lytOrderSummary!!.setOnClickListener(View.OnClickListener {
            createOrEditDraftOrder(object : BrowseCreateNewOrder.CreateDraftOrderResponseListener {
                override fun doNavigation(response: String?) {
                    val draftOrdersBySKUPaginated =
                        fromJson(response, DraftOrdersBySKUPaginated::class.java)
                    validateAddOnOrderByOrderId(
                        this@BrowseCreateNewOrder,
                        draftOrdersBySKUPaginated,
                        object : ValidAddOnOrderByOrderIdListener {
                            override fun onSuccessResponse(addOnOrderValidResponse: DraftOrdersBySKUPaginated?) {
                                val response = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                    addOnOrderValidResponse
                                )
                                val orderSummaryPreviewIntent = Intent(
                                    this@BrowseCreateNewOrder,
                                    ActivityOrderSummaryPreview::class.java
                                )
                                if (detailSuppliersList != null && detailSuppliersList!!.size > 0) {
                                    val detailSupplierJson =
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                            detailSuppliersList
                                        )
                                    write(
                                        ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                                        detailSupplierJson
                                    )
                                }
                                orderSummaryPreviewIntent.putExtra(
                                    ZeemartAppConstants.CART_DRAFT_LIST,
                                    response
                                )
                                startActivity(orderSummaryPreviewIntent)
                            }

                            override fun onErrorResponse(error: VolleyError?) {
                                val orderSummaryPreviewIntent = Intent(
                                    this@BrowseCreateNewOrder,
                                    ActivityOrderSummaryPreview::class.java
                                )
                                if (detailSuppliersList != null && detailSuppliersList!!.size > 0) {
                                    val detailSupplierJson =
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                            detailSuppliersList
                                        )
                                    write(
                                        ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                                        detailSupplierJson
                                    )
                                }
                                orderSummaryPreviewIntent.putExtra(
                                    ZeemartAppConstants.CART_DRAFT_LIST,
                                    response
                                )
                                startActivity(orderSummaryPreviewIntent)
                            }
                        })
                }
            })
        })
        txtOrderAgain = findViewById(R.id.txt_order_again)
        lytOrderAgain = findViewById(R.id.lyt_order_again)
        txtEssentialsSupplierCount = findViewById(R.id.txt_suppliers_count)
        lytEssentialBannerThousandSuppliers = findViewById(R.id.lyt_banner)
        lstOrderAgainEssentialSuppliers = findViewById(R.id.lst_order_again)
        lstOrderAgainEssentialSuppliers!!.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        lstOrderAgainEssentialSuppliers!!.setNestedScrollingEnabled(false)
        txtTotalItems = lytOrderSummary!!.findViewById(R.id.txt_review_total_items_in_cart)
        edtSearch = findViewById(R.id.edit_search)
        edtSearch!!.setHint(getString(R.string.txt_search_products_or_suppliers))
        edtSearch!!.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                logAction(this@BrowseCreateNewOrder, AnalyticsHelper.TAP_CREATE_ORDER_SEARCH)
                val searchIntent = Intent(this@BrowseCreateNewOrder, SearchForNewOrder::class.java)
                startActivity(searchIntent)
                return@OnEditorActionListener true
            }
            false
        })
        edtSearch!!.setTextIsSelectable(true)
        edtSearch!!.setFocusable(false)
        edtSearch!!.setClickable(false)
        edtSearch!!.setOnClickListener(View.OnClickListener {
            val newSearchIntent = Intent(this@BrowseCreateNewOrder, SearchForNewOrder::class.java)
            val supplierListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(detailSuppliersList)
            write(ZeemartAppConstants.SUPPLIER_LIST, supplierListJson)
            val orderList = ZeemartBuyerApp.gsonExposeExclusive.toJson(ordersList)
            newSearchIntent.putExtra(ZeemartAppConstants.ORDER_DETAILS_JSON, orderList)
            newSearchIntent.putExtra(
                ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED,
                isEssentialTabSelected
            )
            startActivity(newSearchIntent)
        })
        lstSuppliers = findViewById(R.id.lst_supplier_list_browse_new_order)
        lstSuppliers!!.setLayoutManager(LinearLayoutManager(this))
        lstEssentials = findViewById(R.id.lst_supplier_list_browse_essential_order)
        lstEssentials!!.setLayoutManager(LinearLayoutManager(this))
        dealsViewPager = findViewById(R.id.deals_view_pager)
        lytDeals = findViewById(R.id.lyt_deals)
        lytDeals!!.setVisibility(View.GONE)
        txtDealsForYou = findViewById(R.id.txt_deals_for_you)
        txtDealsDetails = findViewById(R.id.txt_about_this_deals)
        txtDealsDetails!!.setOnClickListener(View.OnClickListener {
            val testAboutDeals = Intent(this@BrowseCreateNewOrder, AboutDealsActivity::class.java)
            startActivity(testAboutDeals)
        })
        lytNoSuppliers = findViewById(R.id.lyt_no_suppliers)
        lytNoSuppliers!!.setVisibility(View.GONE)
        txtNoSuppliers = findViewById(R.id.txt_no_favourite_yet)
        txtFindOutMore = findViewById(R.id.txt_tap_fav_button)
        tabLayout = findViewById(R.id.tab_layout_carousal)
        lytTabBanner = findViewById(R.id.lyt_tab_banner)
        lytSupplierHeaders = findViewById(R.id.suppliers_lyt_tab)
        lytSupplierHeaders!!.setVisibility(View.GONE)
        btnMySuppliers = findViewById(R.id.browse_btn_my_supplier)
        txtMySuppliersHighLight = findViewById(R.id.browse_txt_btn_my_supplier_highlighter)
        txtMySuppliersHighLight!!.setVisibility(View.GONE)
        txtEssentialsHighLight = findViewById(R.id.browse_txt_btn_essentials_highlighter)
        txtEssentialsHighLight!!.setVisibility(View.GONE)
        btnMySuppliers!!.setOnClickListener(View.OnClickListener {
            isEssentialTabSelected = false
            setMySupplierTabActive()
            writeBool(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED, false)
        })
        btnEssentials = findViewById(R.id.browse_btn_essentials)
        btnEssentials!!.setOnClickListener(View.OnClickListener {
            isEssentialTabSelected = true
            setEssentialsTabActive()
            writeBool(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED, true)
        })
        setFont()
    }

    private fun retrieveActiveEssentials() {
        val apiParamsHelper = ApiParamsHelper()
        getPaginatedEssentials(this, apiParamsHelper, object : EssentialsResponseListener {
            override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                if (essentialsList != null && essentialsList.essentials != null && essentialsList.essentials!!.size > 0) {
                    lytSupplierHeaders!!.visibility = View.VISIBLE
                    lstEssentialsSuppliers = ArrayList()
                    lstEssentialsSuppliers!!.addAll(essentialsList.essentials!!)
                    if (essentialsList.essentials!!.size == 1) {
                        txtEssentialsSupplierCount!!.text =
                            essentialsList.essentials!!.size.toString() + " " + resources.getString(
                                R.string.txt_supplier_small
                            )
                    } else {
                        txtEssentialsSupplierCount!!.text =
                            essentialsList.essentials!!.size.toString() + " " + resources.getString(
                                R.string.txt_suppliers
                            )
                    }
                    lstEssentials!!.adapter = SupplierListEssentialsAdapter(
                        lstEssentialsSuppliers!!,
                        this@BrowseCreateNewOrder,
                        object : SupplierListEssentialsAdapter.SupplierClickListener {
                            override fun onItemSelected(essential: Essential?) {
                                logAction(
                                    this@BrowseCreateNewOrder,
                                    AnalyticsHelper.TAP_CREATE_ORDER_ESSENTIALS,
                                    defaultOutlet!!,
                                    essential!!.essentialsId,
                                    essential.supplier!!
                                )
                                val intent = Intent(
                                    this@BrowseCreateNewOrder,
                                    EssentialsProductListActivity::class.java
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.ESSENTIALS_ID,
                                    essential.essentialsId
                                )
                                //                            intent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, ZeemartBuyerApp.gsonExposeExclusive.toJson(essential));
                                startActivity(intent)
                            }
                        })
                    if (isFirstTimeLoading) {
                        writeBool(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED, true)
                        isFirstTimeLoading = false
                    }
                    isEssentialTabSelected =
                        readBool(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED, false)!!
                    callOrderAgainSuppliersAPI()
                } else {
                    isEssentialsLoaded = true
                    hideLoader(customLoadingViewWhite)
                    lytSupplierHeaders!!.visibility = View.GONE
                    lytEssentialBanner!!.visibility = View.GONE
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                isEssentialsLoaded = true
                hideLoader(customLoadingViewWhite)
                lytSupplierHeaders!!.visibility = View.GONE
                lytEssentialBanner!!.visibility = View.GONE
            }
        })
    }

    private fun callOrderAgainSuppliersAPI() {
        getOrderAgainPaginatedEssentials(this, object : EssentialsResponseListener {
            override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                if (essentialsList != null && essentialsList.essentials != null && essentialsList.essentials!!.size > 0) {
                    isOrderAgainEssentialSuppliersAvailable = true
                    lstOrderAgainEssentialSuppliers!!.adapter =
                        SupplierListEssentialsOrderAgainAdapter(
                            essentialsList.essentials!!,
                            this@BrowseCreateNewOrder,
                            object : SupplierListEssentialsOrderAgainAdapter.SupplierClickListener {
                                override fun onItemSelected(essential: Essential?) {
                                    logAction(
                                        this@BrowseCreateNewOrder,
                                        AnalyticsHelper.TAP_CREATE_ORDER_ESSENTIALS,
                                        defaultOutlet!!,
                                        essential!!.essentialsId,
                                        essential.supplier!!
                                    )
                                    val intent = Intent(
                                        this@BrowseCreateNewOrder,
                                        EssentialsProductListActivity::class.java
                                    )
                                    intent.putExtra(
                                        ZeemartAppConstants.ESSENTIALS_ID,
                                        essential.essentialsId
                                    )
                                    //                            intent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, ZeemartBuyerApp.gsonExposeExclusive.toJson(essential));
                                    startActivity(intent)
                                }
                            })
                    isEssentialsLoaded = true
                    hideLoader(customLoadingViewWhite)
                    if (isEssentialTabSelected) {
                        setEssentialsTabActive()
                    } else {
                        setMySupplierTabActive()
                    }
                } else {
                    isOrderAgainEssentialSuppliersAvailable = false
                    isEssentialsLoaded = true
                    hideLoader(customLoadingViewWhite)
                    if (isEssentialTabSelected) {
                        setEssentialsTabActive()
                    } else {
                        setMySupplierTabActive()
                    }
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                isEssentialsLoaded = true
                isOrderAgainEssentialSuppliersAvailable = false
                hideLoader(customLoadingViewWhite)
                if (isEssentialTabSelected) {
                    setEssentialsTabActive()
                } else {
                    setMySupplierTabActive()
                }
            }
        })
    }

    private fun retrieveActiveDeals() {
        getActiveSpclDeals(this, null, object : SpclDealsResponseListener {
            override fun onSuccessResponse(dealsBaseResponse: SpclDealsBaseResponse?) {
                if (dealsBaseResponse != null && dealsBaseResponse.data != null && dealsBaseResponse.data!!.suppliers != null && dealsBaseResponse.data!!.suppliers!!.size > 0) {
                    lytDeals!!.visibility = View.VISIBLE
                    val dealsImageSliderAdapter = SpclDealsImageSliderAdapter(
                        dealsBaseResponse.data!!.suppliers,
                        this@BrowseCreateNewOrder,
                        object : SpclDealsImageSliderAdapter.DealsImageListener {
                            override fun onDealSelected(deal: SpclDealsBaseResponse.Deals?) {
                                //call clevertap to register on deal click
//                            AnalyticsHelper.logAction(BrowseCreateNewOrder.this, AnalyticsHelper.TAP_CREATE_ORDER_DEALS, SharedPref.getDefaultOutlet(), deal.getDealNumber(), deal.getTitle());
                                if (deal!!.dealNumber != null) {
                                    val intent = Intent(
                                        this@BrowseCreateNewOrder,
                                        DealProductListActivity::class.java
                                    )
                                    intent.putExtra(
                                        ZeemartAppConstants.DEALS_NUMBER,
                                        deal.dealNumber
                                    )
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(
                                        this@BrowseCreateNewOrder,
                                        EssentialsProductListActivity::class.java
                                    )
                                    intent.putExtra(
                                        ZeemartAppConstants.ESSENTIALS_ID,
                                        deal.essentialsId
                                    )
                                    startActivity(intent)
                                }
                            }
                        })
                    dealsViewPager!!.offscreenPageLimit = dealsBaseResponse.data!!.suppliers!!.size
                    dealsViewPager!!.adapter = dealsImageSliderAdapter
                    if (dealsBaseResponse.data!!.suppliers!!.size == 1) {
                        tabLayout!!.visibility = View.GONE
                        lytTabBanner!!.visibility = View.GONE
                    } else {
                        lytTabBanner!!.visibility = View.VISIBLE
                        tabLayout!!.visibility = View.VISIBLE
                        tabLayout!!.setupWithViewPager(dealsViewPager, true)
                    }
                    val timerTask: TimerTask = object : TimerTask() {
                        override fun run() {
                            dealsViewPager!!.post {
                                dealsViewPager!!.currentItem =
                                    (dealsViewPager!!.currentItem + 1) % dealsBaseResponse.data!!.suppliers!!.size
                            }
                        }
                    }
                    isDealsLoaded = true
                    timer = Timer()
                    timer!!.schedule(timerTask, 2500, 2500)
                } else {
                    isDealsLoaded = true
                    lytDeals!!.visibility = View.GONE
                    hideLoader(customLoadingViewWhite)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                isDealsLoaded = true
                hideLoader(customLoadingViewWhite)
                lytDeals!!.visibility = View.GONE
            }
        })
    }

    private interface CreateDraftOrderResponseListener {
        fun doNavigation(response: String?)
    }

    private fun createOrEditDraftOrder(mListener: BrowseCreateNewOrder.CreateDraftOrderResponseListener) {
        displayLoader(customLoadingViewWhite)
        val selectedProducts: MutableList<Product> = ArrayList()
        if (ordersList != null && ordersList!!.size > 0) {
            for (i in ordersList!!.indices) {
                selectedProducts.addAll(ordersList!![i].products!!)
            }
        }
        val selectedProductsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProducts)
        val draftProducts = createSelectedProductForDraftObject(selectedProductsJson)
        val createDraftOrder = CreateDraftOrder()
        createDraftOrder.outletId = read(SharedPref.SELECTED_OUTLET_ID, "")
        createDraftOrder.products = draftProducts
        createDraftOrdersBasedOnSKUs(
            this@BrowseCreateNewOrder,
            requestBody(createDraftOrder),
            object : CreateDraftOrderBasedOnSKUsListener {
                override fun onSuccessResponse(status: String?) {
                    hideLoader(customLoadingViewWhite)
                    mListener.doNavigation(status)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    hideLoader(customLoadingViewWhite)
                }
            },
            createDraftOrder
        )
    }

    private fun requestBody(createDraftOrder: CreateDraftOrder): String {
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(createDraftOrder)
    }

    private fun setFont() {
        setTypefaceView(txtTotalItems, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnSelectedOutlet, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDealsForYou, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtDealsDetails, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnMySuppliers, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnEssentials, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtItemsForSupplier, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(
            txtRebateWithEssentialOrder,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtNew, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtFindOutMore, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtNoSuppliers, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOrderAgain, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtEssentialsSupplierCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    /**
     * get the list of suppliers for the selected outlet
     */
    private fun refreshSupplierList() {
        btnSelectedOutlet!!.text = read(SharedPref.SELECTED_OUTLET_NAME, "")
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(defaultOutlet!!.outletId!!)
        apiParamsHelper.setOrderEnabled(true)
        displayLoader(customLoadingViewWhite)
        val outlets: MutableList<Outlet?> = ArrayList()
        outlets.add(defaultOutlet)
        retrieveSupplierListNew(
            this,
            apiParamsHelper,
            outlets as List<Outlet>?,
            object : SupplierListResponseListener {
               override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                    if (supplierList != null && supplierList.size > 0) {
                        detailSuppliersList = ArrayList()
                        detailSuppliersList!!.addAll(supplierList as Collection<DetailSupplierDataModel>)
                        getBelowParMarketList(supplierList as MutableList<DetailSupplierDataModel>)
                    } else {
                        isSuppliersLoaded = true
                        hideLoader(customLoadingViewWhite)
                        lytOrderSummary!!.visibility = View.GONE
                        lstSuppliers!!.visibility = View.GONE
                        if (!isEssentialTabSelected) {
                            lytNoSuppliers!!.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    isSuppliersLoaded = true
                    hideLoader(customLoadingViewWhite)
                }
            })
    }

    private val outletSettings: Unit
        private get() {
            getSpecificOutlet(this, object : GetSpecificOutletResponseListener {
                override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                    posIntegration =
                        if (specificOutlet!!.data!!.posIntegration != null && specificOutlet.data!!.posIntegration!!.posIntegration != null) {
                            specificOutlet.data!!.posIntegration!!.posIntegration!!
                        } else {
                            false
                        }
                }

                override fun onError(error: VolleyErrorHelper?) {}
            })
        }

    private fun getBelowParMarketList(supplierList: MutableList<DetailSupplierDataModel>) {
        if (posIntegration) {
            val supplierOrderEnabledArray = ArrayList<String?>()
            for (i in supplierList.indices) {
                if (supplierList[i].orderDisabled != null && !supplierList[i].orderDisabled!!) {
                    supplierOrderEnabledArray.add(supplierList[i].supplier.supplierId)
                }
            }
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setIsBelowPar(true)
            apiParamsHelper.setSupplierId(java.lang.String.join(",", supplierOrderEnabledArray))
            retrieveMarketListOutlet(
                this,
                apiParamsHelper,
                defaultOutlet!!.outletId!!,
                object : ProductListBySupplierListner {
                    override fun onSuccessResponse(productList: ProductListBySupplier?) {
                        addProductsList(
                            productList,
                            java.lang.String.join(",", supplierOrderEnabledArray),
                            defaultOutlet!!.outletId
                        )
                        getDrafts(supplierList)
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        getDrafts(supplierList)
                    }
                })
        } else {
            getDrafts(supplierList)
        }
    }

    private fun addProductsList(
        productList: ProductListBySupplier?,
        supplierId: String,
        outletID: String?
    ) {
        if (productList != null && productList.data != null && productList.data!!.products != null && productList.data!!.products!!.size > 0) {
            products = ArrayList()
            for (i in productList.data!!.products!!.indices) {
                if (productList.data!!.products!![i].priceList!!.size > 1) {
                    for (j in productList.data!!.products!![i].priceList!!.indices) {
                        if (productList.data!!.products!![i].priceList!![j].status == "visible") {
                            val productUom = fromJson(
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                    productList.data!!.products!![i]
                                ), ProductDetailBySupplier::class.java
                            )
                            val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                            productPriceLists.add(productList.data!!.products!![i].priceList!![j])
                            productUom!!.priceList = productPriceLists
                            productUom.isHasMultipleUom = true
                            products!!.add(productUom)
                        }
                    }
                } else {
                    if (productList.data!!.products!![i].priceList!![0].status == "visible") {
                        products!!.add(productList.data!!.products!![i])
                    }
                }
            }
            //get the fav on top + sorted product list
//            products = ProductDataHelper.getDisplayProductList(BrowseCreateNewOrder.this, products, outletID, supplierId);
            Log.d("****", "addProductsList: " + products!!.size)
        } else {
            products = ArrayList()
        }
    }

    /**
     * get the draft orders for all the supplier from this outlet
     *
     * @param supplierList
     */
    private fun getDrafts(supplierList: MutableList<DetailSupplierDataModel>) {
        val supplierIdArray = arrayOfNulls<String>(supplierList.size)
        val supplierOrderEnabledArray = ArrayList<String?>()
        for (i in supplierList.indices) {
            supplierIdArray[i] = supplierList[i].supplier.supplierId
            if (supplierList[i].orderDisabled != null && !supplierList[i].orderDisabled!!) {
                supplierOrderEnabledArray.add(supplierList[i].supplier.supplierId)
            }
        }
        returnDraftOrdersForOutlet(this, supplierIdArray, object : OutletDraftOrdersListener {
            override fun onSuccessResponse(orders: List<Orders>?) {
                isSuppliersLoaded = true
                hideLoader(customLoadingViewWhite)
                val favoriteItem = DetailSupplierDataModel()
                favoriteItem.isFavoriteProducts = true
                val supplier1 = Supplier()
                supplier1.supplierId = java.lang.String.join(",", supplierOrderEnabledArray)
                favoriteItem.supplier = supplier1
                supplierList.add(0, favoriteItem)
                if (posIntegration) {
                    val isFilltoPar = DetailSupplierDataModel()
                    isFilltoPar.isFillToPar = true
                    val supplier = Supplier()
                    supplier.supplierId = java.lang.String.join(",", supplierOrderEnabledArray)
                    var size: String? = String.format(getString(R.string.txt_items_count), 0)
                    if (products != null) {
                        size = Integer.toString(products!!.size)
                        size = if (products!!.size == 1) {
                            getString(R.string.txt_item_count)
                        } else {
                            String.format(getString(R.string.txt_items_count), products!!.size)
                        }
                    }
                    supplier.shortDesc = size
                    isFilltoPar.supplier = supplier
                    supplierList.add(1, isFilltoPar)
                }
                lstSuppliers!!.adapter = SupplierListNewOrderAdapter(
                    this@BrowseCreateNewOrder,
                    supplierList,
                    object : SupplierListNewOrderAdapter.SupplierClickListener {
                        override fun onItemSelected(supplier: DetailSupplierDataModel?) {
                            moveToProductList(supplier)
                        }
                    })
                cartItemsCount = getCartItemsCount(orders)
                setLayoutReviewButton(cartItemsCount)
                ordersList = orders
                if (isEssentialTabSelected) {
                    setEssentialsTabActive()
                } else {
                    setMySupplierTabActive()
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                isSuppliersLoaded = true
                hideLoader(customLoadingViewWhite)
                if (!isEssentialTabSelected) lstSuppliers!!.visibility = View.VISIBLE
                val favoriteItem = DetailSupplierDataModel()
                favoriteItem.isFavoriteProducts = true
                supplierList.add(0, favoriteItem)
                lstSuppliers!!.adapter = SupplierListNewOrderAdapter(
                    this@BrowseCreateNewOrder,
                    supplierList,
                    object : SupplierListNewOrderAdapter.SupplierClickListener {
                        override fun onItemSelected(supplier: DetailSupplierDataModel?) {
                            moveToProductList(supplier)
                        }
                    })
                if (isEssentialTabSelected) {
                    setEssentialsTabActive()
                } else {
                    setMySupplierTabActive()
                }
            }
        })
    }

    private fun setLayoutReviewButton(items: Int) {
        if (items > 0) {
            if (!isEssentialTabSelected) {
                lytOrderSummary!!.visibility = View.VISIBLE
            } else {
                lytOrderSummary!!.visibility = View.GONE
            }
            if (items == 1) {
                txtTotalItems!!.text = String.format(
                    getString(R.string.txt_review_order_cart_item),
                    items
                )
            } else {
                txtTotalItems!!.text = String.format(
                    getString(R.string.txt_review_order_cart_items),
                    items
                )
            }
        } else {
            lytOrderSummary!!.visibility = View.GONE
        }
    }

    private fun moveToProductList(supplier: DetailSupplierDataModel?) {
        val newIntent = Intent(this@BrowseCreateNewOrder, AddToOrderActivity::class.java)
        newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, read(SharedPref.SELECTED_OUTLET_ID, ""))
        newIntent.putExtra(ZeemartAppConstants.POS_INTEGRATION, posIntegration)
        if (detailSuppliersList != null && detailSuppliersList!!.size > 0) {
            val detailSupplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(detailSuppliersList)
            write(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, detailSupplierJson)
        }
        val supplierListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(detailSuppliersList)
        write(ZeemartAppConstants.SUPPLIER_LIST, supplierListJson)
        if (supplier!!.isFavoriteProducts) {
            logAction(this, AnalyticsHelper.TAP_CREATE_ORDER_FAVOURITE_LIST, outlet!!)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_FAVORITE_BROWSE
            )
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier.supplier.supplierId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier.supplier.supplierName)
            val supplierDetailsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplier)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailsJson)
        } else if (supplier.isFillToPar) {
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_BELOW_PAR_ITEMS
            )
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier.supplier.supplierId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier.supplier.supplierName)
            val supplierDetailsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplier)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailsJson)
        } else {
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier.supplier.supplierId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier.supplier.supplierName)
            val supplierDetailsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplier)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailsJson)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_SUPPLIER_BROWSE
            )
        }
        startActivity(newIntent)
    }

    override fun displayLoader(view: View?) {
        if (view != null) {
            view.visibility = View.VISIBLE
        }
    }

    override fun hideLoader(view: View?) {
        if (view != null) {
            if (isDealsLoaded && isEssentialsLoaded && isSuppliersLoaded) view.visibility =
                View.GONE
        }
    }

    private fun setMySupplierTabActive() {
        lytEssentialBanner!!.visibility = View.GONE
        btnMySuppliers!!.setTextColor(resources.getColor(R.color.black))
        txtMySuppliersHighLight!!.visibility = View.VISIBLE
        btnEssentials!!.setTextColor(resources.getColor(R.color.grey_medium))
        txtEssentialsHighLight!!.visibility = View.GONE
        setLayoutReviewButton(cartItemsCount)
        if (!isEssentialTabSelected) {
            if (detailSuppliersList != null && detailSuppliersList!!.size > 0) {
                lstSuppliers!!.visibility = View.VISIBLE
                lytNoSuppliers!!.visibility = View.GONE
            } else {
                lytNoSuppliers!!.visibility = View.VISIBLE
                lstSuppliers!!.visibility = View.GONE
            }
        }
    }

    private fun setEssentialsTabActive() {
        lstSuppliers!!.visibility = View.GONE
        btnEssentials!!.setTextColor(resources.getColor(R.color.black))
        txtEssentialsHighLight!!.visibility = View.VISIBLE
        btnMySuppliers!!.setTextColor(resources.getColor(R.color.grey_medium))
        txtMySuppliersHighLight!!.visibility = View.GONE
        lytNoSuppliers!!.visibility = View.GONE
        setLayoutReviewButton(cartItemsCount)
        if (isEssentialTabSelected) {
            lytEssentialBanner!!.visibility = View.VISIBLE
            if (isOrderAgainEssentialSuppliersAvailable) {
                lytEssentialBannerThousandSuppliers!!.visibility = View.GONE
                lytOrderAgain!!.visibility = View.VISIBLE
            } else {
                lytEssentialBannerThousandSuppliers!!.visibility = View.VISIBLE
                lytOrderAgain!!.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        if (timer != null) {
            timer!!.cancel()
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (timer != null) {
            timer!!.cancel()
        }
    }
}