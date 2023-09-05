package zeemart.asia.buyers.orders.createorders

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.SearchEssentialsAndDealsAdapter
import zeemart.asia.buyers.adapter.SearchForNewOrderProductAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.deals.DealProductListActivity
import zeemart.asia.buyers.essentials.EssentialsProductListActivity
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.pagination.DeliveryScrollHelper
import zeemart.asia.buyers.helper.pagination.PaginationListScrollHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.EssentialsBaseResponse
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.ProductDetailsBySearch
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.order.SearchForNewOrderMgr
import zeemart.asia.buyers.models.orderimport.CreateDraftOrder
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.models.orderimportimport.DraftOrdersBySKUPaginated
import zeemart.asia.buyers.modelsimport.SearchEssentialAndDealsModel
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsPruducts
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsSuppliers
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.util.*

/**
 * Created by Muthumari on 19/10/2019.
 * Updated by RajprudhviMarella on 10/Feb/2021.
 */
class SearchForNewOrder : AppCompatActivity() {
    private var txtRecentSearchHeader: TextView? = null
    private lateinit var txtSearchClear: TextView
    private var txtNoResult: TextView? = null
    private var txtRetry: TextView? = null
    private var textSearch: TextView? = null
    private lateinit var txtCancel: TextView
    private lateinit var lstSupplier: RecyclerView
    private lateinit var lstEssentials: RecyclerView
    private var lstRecentSearch: ListView? = null
    private lateinit var edtSearch: EditText
    private var edtSearchClear: ImageView? = null
    private lateinit var lytNoResults: RelativeLayout
    private var filterSupplierList: List<DetailSupplierDataModel>? = null
    private var ordersList: List<Orders>? = null
    private var recentSearchList: MutableList<String>? = ArrayList()
    private lateinit var outletId: String
    private lateinit var threeDotLoaderWhite: CustomLoadingViewBlue
    private var productList: MutableList<ProductDetailBySupplier>? =
        ArrayList<ProductDetailBySupplier>()
    private var essentialProductList: MutableList<ProductDetailBySupplier>? = null
    private var supplierList: MutableList<DetailSupplierDataModel>? = null
    private var cartItemCount = 0
    private lateinit var lytOrderSummary: RelativeLayout
    private var selectedProductMap: Map<String, Product> = HashMap()
    private var layoutSearch: RelativeLayout? = null
    private var txtTotalItems: TextView? = null
    private var searchText: String? = null
    private var supplierDetails: DetailSupplierDataModel? = null
    private val selectedProductList: MutableList<Product>? = ArrayList()
    private var showProductListCalledFrom: String? = null
    private lateinit var lstSearchData: ArrayList<SearchForNewOrderMgr>
    private var lstSearchEssentialsAndDealsData: ArrayList<SearchForNewOrderMgr> =
        ArrayList<SearchForNewOrderMgr>()
    private var btnMySuppliers: Button? = null
    private var btnDealsAndEssentials: Button? = null
    private var txtMySuppliersHighLight: TextView? = null
    private var txtDealsAndEssentialsHighLight: TextView? = null
    private lateinit var lytTabs: ConstraintLayout
    private var isEssentialTabSelected = false
    private var txtEssentialsCount: TextView? = null
    private var txtMySupplierCount: TextView? = null
    private var essentialsCount = 0
    private var suppliersCount = 0
    val handler = Handler()
    var runnable: Runnable? = null
    private var isSupplierViewAllSelected = false
    private var isCategoriesViewAllSelected = false
    private var isMySuppliersViewAllSelected = false
    private var totalMyProductsPageCount = 1
    private lateinit var mySuppliersScrollHelper: DeliveryScrollHelper
    private lateinit var essentialSuppliersScrollHelper: DeliveryScrollHelper
    private var totalEssentialProductsPageCount = 1
    private var lstMyProductsLayoutManager: LinearLayoutManager? = null
    private var lstEssentialProductsLayoutManager: LinearLayoutManager? = null
    private var myProducts: ArrayList<ProductDetailsBySearch>? = null
    private var essentialsProducts: ArrayList<SearchedEssentialAndDealsPruducts>? = null
    private var isAutomaticSearchApplied = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_for_new_order)
        pageCountEssentialProducts = 0
        pageCountMyProducts = 0
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        outletId = if (SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")?.isEmpty()!!) ({
            SharedPref.defaultOutlet?.outletId
        }).toString() else {
            SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")!!
        }
        txtRecentSearchHeader = findViewById<TextView>(R.id.txt_recent_search)
        txtNoResult = findViewById<TextView>(R.id.txt_no_search_found)
        txtRetry = findViewById<TextView>(R.id.txt_try_with_other)
        if (!StringHelper.isStringNullOrEmpty(
                SharedPref.read(
                    ZeemartAppConstants.SUPPLIER_LIST,
                    ""
                )
            )
        ) {
            val json: String = SharedPref.read(ZeemartAppConstants.SUPPLIER_LIST, "")!!
            supplierList =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<MutableList<DetailSupplierDataModel>>(
                    json,
                    object : TypeToken<List<DetailSupplierDataModel?>?>() {}.type
                )
        }
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle?.containsKey(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED) == true) {
            isEssentialTabSelected =
                bundle?.getBoolean(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED) == true
        }
        txtCancel = findViewById<TextView>(R.id.search_btn_cancel)
        txtCancel.setOnClickListener(View.OnClickListener {
            if (threeDotLoaderWhite?.getVisibility() == View.GONE) {
                if (supplierList != null && supplierList!!.size > 0) {
                    createOrEditDraftOrder(object : CreateDraftOrderResponseListener {
                        override fun doNavigation(response: String?) {
                            finish()
                        }
                    })
                } else {
                    finish()
                }
            }
        })
        txtSearchClear = findViewById<TextView>(R.id.txt_recent_search_clear)
        lytOrderSummary = findViewById<RelativeLayout>(R.id.lyt_review_order_search)
        lytOrderSummary.setVisibility(View.GONE)
        layoutSearch = findViewById<RelativeLayout>(R.id.lyt_search)
        textSearch = findViewById<TextView>(R.id.txt_search)
        threeDotLoaderWhite =
            findViewById<CustomLoadingViewBlue>(R.id.spin_kit_loader_report_search_white)
        txtTotalItems = lytOrderSummary.findViewById<TextView>(R.id.txt_review_total_items_in_cart)
        lytOrderSummary.setOnClickListener(View.OnClickListener {
            if (threeDotLoaderWhite.getVisibility() == View.GONE) {
                createOrEditDraftOrder(object : CreateDraftOrderResponseListener {
                    override fun doNavigation(response: String?) {
                        val draftOrdersBySKUPaginated: DraftOrdersBySKUPaginated? =
                            ZeemartBuyerApp.fromJson(
                                response,
                                DraftOrdersBySKUPaginated::class.java
                            )
                        OrderHelper.validateAddOnOrderByOrderId(
                            this@SearchForNewOrder,
                            draftOrdersBySKUPaginated,
                            object : OrderHelper.ValidAddOnOrderByOrderIdListener {
                                override fun onSuccessResponse(addOnOrderValidResponse: DraftOrdersBySKUPaginated?) {
                                    val response: String =
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                            addOnOrderValidResponse
                                        )
                                    val orderSummaryPreviewIntent = Intent(
                                        this@SearchForNewOrder,
                                        ActivityOrderSummaryPreview::class.java
                                    )
                                    if (supplierList != null && supplierList!!.size > 0) {
                                        val detailSupplierJson: String =
                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(supplierList)
                                        SharedPref.write(
                                            ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                                            detailSupplierJson
                                        )
                                    }
                                    orderSummaryPreviewIntent.putExtra(
                                        ZeemartAppConstants.CART_DRAFT_LIST,
                                        response
                                    )
                                    startActivity(orderSummaryPreviewIntent)
                                    finish()
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    val orderSummaryPreviewIntent = Intent(
                                        this@SearchForNewOrder,
                                        ActivityOrderSummaryPreview::class.java
                                    )
                                    if (supplierList != null && supplierList!!.size > 0) {
                                        val detailSupplierJson: String =
                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(supplierList)
                                        SharedPref.write(
                                            ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                                            detailSupplierJson
                                        )
                                    }
                                    orderSummaryPreviewIntent.putExtra(
                                        ZeemartAppConstants.CART_DRAFT_LIST,
                                        response
                                    )
                                    startActivity(orderSummaryPreviewIntent)
                                    finish()
                                }
                            })
                    }
                })
            }
        })
        txtSearchClear.setOnClickListener(View.OnClickListener {
            recentSearchList!!.clear()
            setRecentSearchListAdapter(recentSearchList as MutableList<String?>)
            setRecentSearchListVisibility()
            SharedPref.removeString(SharedPref.ORDER_BY_PRODUCT_RECENT_SEARCH)
        })
        lytNoResults = findViewById<RelativeLayout>(R.id.lyt_no_search_results)
        lytNoResults.setVisibility(View.GONE)
        lstSupplier = findViewById<RecyclerView>(R.id.lst_search_supplier)
        lstSupplier.setVisibility(View.GONE)
        lstMyProductsLayoutManager = LinearLayoutManager(this)
        lstSupplier.setLayoutManager(lstMyProductsLayoutManager)
        lstRecentSearch = findViewById<ListView>(R.id.lst_recent_search)
        lstRecentSearch!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                arg0: AdapterView<*>?, arg1: View,
                position: Int, id: Long
            ) {
                val name = recentSearchList!![position]
                if (name != null) {
                    edtSearch?.setText(name)
                    edtSearch?.getText()?.length?.let { edtSearch?.setSelection(it) }
                    suppliersCount = 0
                    essentialsCount = 0
                }
            }
        })
        val jsonText: String = SharedPref.read(SharedPref.ORDER_BY_PRODUCT_RECENT_SEARCH, "")!!
        val lstRecentSearch: Array<String> =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson<Array<String>>(
                jsonText,
                Array<String>::class.java
            )
        if (lstRecentSearch != null) {
            recentSearchList = ArrayList(Arrays.asList(*lstRecentSearch))
        }
        setRecentSearchListAdapter(recentSearchList as MutableList<String?>)
        edtSearch = findViewById<EditText>(R.id.edit_search)
        edtSearch.requestFocus()
        edtSearchClear = findViewById<ImageView>(R.id.edit_search_clear)
        edtSearch.setHint(getResources().getString(R.string.txt_search_products_or_suppliers))
        edtSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!StringHelper.isStringNullOrEmpty(
                            edtSearch.getText().toString()
                        ) && edtSearch.getText().length > 0
                    ) {
                        callSupplierList(false)
                    }
                }
                return false
            }
        })
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                handler.removeCallbacks(runnable!!)
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                searchText = edtSearch.getText().toString().trim { it <= ' ' }
                if (charSequence.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear!!.visibility = View.VISIBLE
                } else {
                    edtSearchClear!!.visibility = View.GONE
                    setRecentSearchListVisibility()
                }
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.length > 1) {
                    // Show some progress, because you can access UI here
                    runnable = Runnable {
                        callSupplierList(true)
                        // Do some work with s.toString()
                    }
                    handler.postDelayed(runnable!!, 1500)
                }
            }
        })
        edtSearchClear!!.setOnClickListener {
            if (threeDotLoaderWhite.getVisibility() == View.GONE) {
                createOrEditDraftOrder(object : CreateDraftOrderResponseListener {
                    override fun doNavigation(response: String?) {}
                })
            }
            edtSearch.setText("")
            setRecentSearchListVisibility()
        }
        lytTabs = findViewById<ConstraintLayout>(R.id.suppliers_lyt_tab)
        lytTabs.setVisibility(View.GONE)
        btnDealsAndEssentials = findViewById<Button>(R.id.browse_btn_essentials)
        btnDealsAndEssentials!!.setOnClickListener {
            isEssentialTabSelected = true
            setEssentialsTabActive()
            SharedPref.writeBool(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED, true)
        }
        btnMySuppliers = findViewById<Button>(R.id.browse_btn_my_supplier)
        btnMySuppliers!!.setOnClickListener {
            isEssentialTabSelected = false
            setMySupplierTabActive()
            SharedPref.writeBool(ZeemartAppConstants.IS_ESSENTIALS_TAB_SELECTED, false)
        }
        txtMySuppliersHighLight =
            findViewById<TextView>(R.id.browse_txt_btn_my_supplier_highlighter)
        txtDealsAndEssentialsHighLight =
            findViewById<TextView>(R.id.browse_txt_btn_essentials_highlighter)
        lstEssentials = findViewById<RecyclerView>(R.id.lst_search_essentials)
        lstEssentialProductsLayoutManager = LinearLayoutManager(this)
        lstEssentials.setLayoutManager(lstEssentialProductsLayoutManager)
        lstEssentials.setVisibility(View.GONE)
        txtEssentialsCount = findViewById<TextView>(R.id.txt_number_of_essentials)
        txtMySupplierCount = findViewById<TextView>(R.id.txt_number_of_mysuppliers)
        essentialSuppliersScrollHelper = DeliveryScrollHelper(
            this,
            lstEssentials,
            lstEssentialProductsLayoutManager!!,
            object : PaginationListScrollHelper.ScrollCallback {
                override fun loadMore() {
                    if (pageCountEssentialProducts < totalEssentialProductsPageCount) {
                        callDealsAndEssentialsList(true)
                    }
                }
            }
        )
        mySuppliersScrollHelper = DeliveryScrollHelper(
            this,
            lstSupplier,
            lstMyProductsLayoutManager!!,
            object : PaginationListScrollHelper.ScrollCallback {
                override fun loadMore() {
                    if (pageCountMyProducts < totalMyProductsPageCount) {
                        callProductList(true)
                    }
                }
            }
        )
        if (isEssentialTabSelected) {
            essentialSuppliersScrollHelper.setOnScrollListener()
        } else {
            mySuppliersScrollHelper.setOnScrollListener()
        }
        setRecentSearchListVisibility()
        setFont()
    }

    private fun setSupplierListVisibility() {
        if (lstSearchData != null && lstSearchData!!.size > 0 && !StringHelper.isStringNullOrEmpty(
                edtSearch.getText().toString()
            )
        ) {
            lytTabs.setVisibility(View.VISIBLE)
            if (!isEssentialTabSelected) lstSupplier.setVisibility(View.VISIBLE)
            layoutSearch?.setVisibility(View.GONE)
            lytNoResults.setVisibility(View.GONE)
        } else {
            lytNoResults.setVisibility(View.VISIBLE)
            lytOrderSummary.setVisibility(View.GONE)
            layoutSearch?.setVisibility(View.GONE)
            lytTabs.setVisibility(View.VISIBLE)
            lstSupplier.setVisibility(View.GONE)
        }
    }

    private fun setEssentialListVisibility() {
        if (lstSearchEssentialsAndDealsData != null && lstSearchEssentialsAndDealsData!!.size > 0 && !StringHelper.isStringNullOrEmpty(
                edtSearch.getText().toString()
            )
        ) {
            lytTabs.setVisibility(View.VISIBLE)
            if (isEssentialTabSelected) lstEssentials.setVisibility(View.VISIBLE)
            layoutSearch?.setVisibility(View.GONE)
            lytNoResults.setVisibility(View.GONE)
        } else {
            lytNoResults.setVisibility(View.VISIBLE)
            lstEssentials.setVisibility(View.GONE)
            layoutSearch?.setVisibility(View.GONE)
            lytTabs.setVisibility(View.VISIBLE)
        }
        if (!isAutomaticSearchApplied) CommonMethods.hideKeyboard(this@SearchForNewOrder)
    }

    private fun setRecentSearchListVisibility() {
        if (StringHelper.isStringNullOrEmpty(edtSearch.getText().toString())) {
            threeDotLoaderWhite.setVisibility(View.GONE)
            lytOrderSummary.setVisibility(View.GONE)
            lytNoResults.setVisibility(View.GONE)
            lstSupplier.setVisibility(View.GONE)
            lytTabs.setVisibility(View.GONE)
            lstEssentials.setVisibility(View.GONE)
            if (recentSearchList != null && recentSearchList!!.size > 0) {
                lstRecentSearch!!.visibility = View.VISIBLE
                txtRecentSearchHeader?.setVisibility(View.VISIBLE)
                txtSearchClear.setVisibility(View.VISIBLE)
                layoutSearch?.setVisibility(View.GONE)
            } else {
                lstRecentSearch!!.visibility = View.GONE
                txtRecentSearchHeader?.setVisibility(View.GONE)
                txtSearchClear.setVisibility(View.GONE)
                layoutSearch?.setVisibility(View.VISIBLE)
            }
        }
    }

    fun noSearchSupplierResultLayoutVisibility(filteredSuppliers: List<DetailSupplierDataModel>?) {
        filterSupplierList = filteredSuppliers
        setReportSearchLayout()
    }

    private fun setReportSearchLayout() {
        setRecentSearchListVisibility()
        setSupplierListVisibility()
        if (!isAutomaticSearchApplied) CommonMethods.hideKeyboard(this@SearchForNewOrder)
    }

    private fun createOrEditDraftOrder(mListener: CreateDraftOrderResponseListener) {
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        val selectedProducts: MutableList<Product?> = ArrayList(selectedProductList)
        if (productList != null && productList!!.size > 0) for (i in productList!!.indices) {
            if (productList!![i].isSelected) {
                var item: Product?
                val key: String? = productList!![i].priceList?.get(0)?.unitSize?.let {
                    ProductDataHelper.getKeyProductMap(
                        productList!![i].sku,
                        it
                    )
                }
                item = if (selectedProductMap.containsKey(key)) {
                    selectedProductMap[productList!![i].sku + productList!![i].priceList?.get(0)?.unitSize]
                } else {
                    ProductDataHelper.createSelectedProductObject(productList!![i])
                }
                if (!selectedProducts.contains(item)) {
                    selectedProducts.add(item)
                }
            }
        }
        val selectedProductsJson: String =
            ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProducts)
        val draftProducts: List<CreateDraftOrder.Product> =
            ProductDataHelper.createSelectedProductForDraftObject(selectedProductsJson)
        val createDraftOrder = CreateDraftOrder()
        createDraftOrder.outletId = (SharedPref.read(SharedPref.SELECTED_OUTLET_ID, ""))
        createDraftOrder.products = (draftProducts)
        OrderManagement.createDraftOrdersBasedOnSKUs(
            this@SearchForNewOrder,
            requestBody(createDraftOrder),
            object : OrderManagement.CreateDraftOrderBasedOnSKUsListener {
                override fun onSuccessResponse(status: String?) {
                    threeDotLoaderWhite.setVisibility(View.GONE)
                    mListener.doNavigation(status)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite.setVisibility(View.GONE)
                }
            },
            createDraftOrder
        )
    }

    private fun requestBody(createDraftOrder: CreateDraftOrder): String {
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(createDraftOrder)
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtRecentSearchHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSearchClear,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoResult,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalItems,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            textSearch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            edtSearch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCancel,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRetry,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEssentialsCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtMySupplierCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnMySuppliers,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnDealsAndEssentials,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun callSupplierList(isAutoSearch: Boolean) {
        isAutomaticSearchApplied = isAutoSearch
        suppliersCount = 0
        essentialsCount = 0
        pageCountEssentialProducts = 0
        pageCountMyProducts = 0
        totalEssentialProductsPageCount = 1
        totalMyProductsPageCount = 1
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        isCategoriesViewAllSelected = false
        isSupplierViewAllSelected = false
        isMySuppliersViewAllSelected = false
        myProducts = null
        essentialsProducts = null
        lstSearchData = ArrayList<SearchForNewOrderMgr>()
        lstSearchEssentialsAndDealsData = ArrayList<SearchForNewOrderMgr>()
        lstSupplier.setAdapter(null)
        lstEssentials.setAdapter(null)
        lytNoResults.setVisibility(View.GONE)
        lytTabs.setVisibility(View.GONE)
        mySuppliersScrollHelper.updateScrollListener(lstSupplier, lstMyProductsLayoutManager)
        essentialSuppliersScrollHelper.updateScrollListener(
            lstEssentials,
            lstEssentialProductsLayoutManager
        )
        FilterSuppliers().filter(edtSearch.getText().toString())
        if (supplierList != null && supplierList!!.size > 0) {
            for (i in supplierList!!.indices) {
                if (supplierList!![i].isFavoriteProducts) supplierList!!.removeAt(i)
            }
            getDrafts(supplierList!!)
        } else {
            callDealsAndEssentialsList(false)
        }
    }

    fun moveToProductList(supplier: DetailSupplierDataModel) {
        //call the retrieve order API to get the list of draft orders
        val newIntent = Intent(this@SearchForNewOrder, AddToOrderActivity::class.java)
        newIntent.putExtra(
            ZeemartAppConstants.OUTLET_ID,
            SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
        )
        val supplierListJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplierList)
        SharedPref.write(ZeemartAppConstants.SUPPLIER_LIST, supplierListJson)
        newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier.supplier.supplierId)
        newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier.supplier.supplierName)
        val supplierDetailsJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplier)
        newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailsJson)
        newIntent.putExtra(
            ZeemartAppConstants.CALLED_FROM,
            ZeemartAppConstants.CALLED_FROM_SUPPLIER_SEARCH
        )
        startActivityForResult(newIntent, REQUEST_CODE_UPDATE_CART_COUNT)
    }

    /**
     * get the draft orders for all the supplier from this outlet
     *
     * @param supplierList
     */
    private fun getDrafts(supplierList: List<DetailSupplierDataModel>) {
        val supplierIdArray = arrayOfNulls<String>(supplierList.size)
        for (i in supplierList.indices) {
            supplierIdArray[i] = supplierList[i].supplier.supplierId
        }
        if (supplierIdArray.size > 0) OrderHelper.returnDraftOrdersForOutlet(
            this,
            supplierIdArray,
            object : OrderHelper.OutletDraftOrdersListener {
                override fun onSuccessResponse(orders: List<Orders>?) {
                    if (orders != null && orders.size > 0) {
                        for (i in orders.indices) {
                            orders[i].products?.let { selectedProductList?.addAll(it) }
                        }
                        selectedProductMap =
                            ProductDataHelper.createSelectedProductMap(selectedProductList)!!
                    }
                    if (!isEssentialTabSelected) lytOrderSummary.setVisibility(View.VISIBLE)
                    cartItemCount = OrderHelper.getCartItemsCount(orders)
                    setLayoutReviewButton(cartItemCount)
                    ordersList = orders
                    callProductList(false)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    lstSupplier.setVisibility(View.VISIBLE)
                    callProductList(false)
                }
            })
    }

    private fun setLayoutReviewButton(items: Int) {
        if (items > 0) {
            if (!isEssentialTabSelected) {
                lytOrderSummary.setVisibility(View.VISIBLE)
            } else {
                lytOrderSummary.setVisibility(View.GONE)
            }
            lytOrderSummary.setClickable(true)
            lytOrderSummary.setBackground(getResources().getDrawable(R.drawable.blue_rounded_corner))
            if (items == 1) {
                txtTotalItems?.setText(
                    String.format(
                        getString(R.string.txt_review_order_cart_item),
                        items
                    )
                )
            } else {
                txtTotalItems?.setText(
                    String.format(
                        getString(R.string.txt_review_order_cart_items),
                        items
                    )
                )
            }
        } else {
            lytOrderSummary.setVisibility(View.GONE)
        }
    }

    private fun callProductList(isLoadMore: Boolean) {
        if (pageCountMyProducts < totalMyProductsPageCount) {
            pageCountMyProducts = pageCountMyProducts + 1
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setSearchText(searchText!!)
            apiParamsHelper.setPageSize(20)
            apiParamsHelper.setPageNumber(pageCountMyProducts)
            MarketListApi.retrieveSearchProductListOutlet(
                this,
                apiParamsHelper,
                outletId,
                object : MarketListApi.SearchProductListListener {
                    override fun onSuccessResponse(productsList: ProductDetailsBySearch.Response?) {
                        if (productsList != null && productsList.data != null && productsList.data!!.data != null && productsList.data!!.data!!.size > 0) {
                            totalMyProductsPageCount = productsList.data!!.numberOfPages!!
                            totalProductsMySupplier = productsList.data!!.numberOfRecords!!
                            val orders: List<ProductDetailsBySearch>? = productsList.data!!.data
                            val fetchedOrders: ArrayList<ProductDetailsBySearch> =
                                ArrayList<ProductDetailsBySearch>(orders)
                            if (isLoadMore && myProducts != null && myProducts!!.size > 0) {
                                myProducts!!.addAll(fetchedOrders)
                            } else {
                                myProducts = ArrayList<ProductDetailsBySearch>(orders)
                            }
                            setProductsData(fetchedOrders, isLoadMore)
                        } else {
                            setMySuppliersAdapter()
                        }
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        setMySuppliersAdapter()
                        threeDotLoaderWhite.setVisibility(View.GONE)
                        val errorMessage: String? = error?.errorMessage
                        val errorType: String? = error?.errorType
                        if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                            ZeemartBuyerApp.getToastRed(errorMessage)
                        }
                    }
                })
        }
    }

    private fun setMySuppliersAdapter() {
        if (lstSupplier.getAdapter() != null) {
            lstSupplier.getAdapter()!!.notifyDataSetChanged()
        } else {
            lstSupplier.setAdapter(
                SearchForNewOrderProductAdapter(
                    this@SearchForNewOrder,
                    lstSearchData,
                    outletId,
                    supplierDetails,
                    selectedProductMap as MutableMap<String, Product>?,
                    ordersList,
                    object : SearchForNewOrderProductAdapter.SupplierClickListener {
                        override fun onSupplierClicked(supplier: DetailSupplierDataModel?) {
                            if (!StringHelper.isStringNullOrEmpty(supplier?.supplier?.supplierName)) {
                                supplier?.supplier?.supplierName?.let { setRecentSearchList(it) }
                            }
                            moveToProductList(supplier!!)
                        }

                        override fun noSearchSupplierResult(filterSuppliers: List<DetailSupplierDataModel?>?) {
                            noSearchSupplierResultLayoutVisibility(filterSuppliers as List<DetailSupplierDataModel>?)
                        }
                    },
                    object : SearchForNewOrderProductAdapter.SelectedProductsListener {
                        override fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?) {
                            if (!StringHelper.isStringNullOrEmpty(productDetailBySupplier?.productName)) {
                                productDetailBySupplier?.productName?.let { setRecentSearchList(it) }
                            }
                            cartItemCount = cartItemCount + 1
                            setLayoutReviewButton(cartItemCount)
                            if (lytOrderSummary.getVisibility() == View.GONE) {
                                if (!isEssentialTabSelected) lytOrderSummary.setVisibility(View.VISIBLE)
                            }
                        }

                        override fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?) {
                            cartItemCount = cartItemCount - 1
                            setLayoutReviewButton(cartItemCount)
                            removeSelectedProductToDraftList(productDetailBySupplier!!)
                        }

                        override fun onProductSelectedForRecentSearch(productName: String?) {
                            if (!StringHelper.isStringNullOrEmpty(productName)) {
                                setRecentSearchList(productName!!)
                            }
                        }

                        override fun onItemClick(item: String?, productName: String?) {
                            if (!StringHelper.isStringNullOrEmpty(productName)) {
                                setRecentSearchList(productName!!)
                            }
                        }
                    },
                    object : SearchForNewOrderProductAdapter.onViewAllClickListener {
                        override fun onSupplierViewAllClicked() {
                            isMySuppliersViewAllSelected = true
                            val searchForNewOrderMgr = SearchForNewOrderMgr()
                            searchForNewOrderMgr.isHeader
                            if (filterSupplierList!!.size == 1) {
                                searchForNewOrderMgr.header = (getResources().getString(R.string.txt_supplier_small))
                            } else {
                                searchForNewOrderMgr.header = (getResources().getString(R.string.txt_suppliers))
                            }
                            searchForNewOrderMgr.headerCount = (filterSupplierList!!.size)
                            lstSearchData!!.clear()
                            lstSearchData!!.add(searchForNewOrderMgr)
                            for (i in filterSupplierList!!.indices) {
                                val searchOrdersManagerOrder = SearchForNewOrderMgr()
                                searchOrdersManagerOrder.detailSupplierDataModel = (
                                    filterSupplierList!![i]
                                )
                                lstSearchData!!.add(searchOrdersManagerOrder)
                            }
                            setProductsData(myProducts, false)
                        }
                    })
            )
            callDealsAndEssentialsList(false)
        }
        (lstSupplier.getAdapter() as SearchForNewOrderProductAdapter).getFilter()
            .filter(edtSearch.getText().toString())
    }

    private fun setProductsData(
        productsList: ArrayList<ProductDetailsBySearch>?,
        isFirstTime: Boolean
    ) {
        productList = ArrayList<ProductDetailBySupplier>()
        if (!isAutomaticSearchApplied) CommonMethods.hideKeyboard(this@SearchForNewOrder)
        if (productsList != null && productsList.size > 0) {
            if (!isFirstTime) {
                val searchForNewOrderManager = SearchForNewOrderMgr()
                searchForNewOrderManager.isHeader
                if (productsList.size == 1) {
                    searchForNewOrderManager.header = (getResources().getString(R.string.txt_product))
                } else {
                    searchForNewOrderManager.header = (getResources().getString(R.string.txt_products))
                }
                searchForNewOrderManager.headerCount = (totalProductsMySupplier)
                suppliersCount = suppliersCount + totalProductsMySupplier
                lstSearchData!!.add(searchForNewOrderManager)
            }
            for (i in productsList.indices) {
                val productDetailBySupplier: ProductDetailBySupplier =
                    ProductDataHelper.getSearchProduct(
                        productsList[i]
                    )
                if (!productList!!.contains(productDetailBySupplier)) productList!!.add(
                    productDetailBySupplier
                )
            }
            setSupplierName()
            for (i in productList!!.indices) {
                val searchNewOrdersManagerOrder = SearchForNewOrderMgr()
                searchNewOrdersManagerOrder.productDetailBySupplier = (productList!![i])
                lstSearchData!!.add(searchNewOrdersManagerOrder)
            }
        } else {
            threeDotLoaderWhite.setVisibility(View.GONE)
        }
        setMySuppliersAdapter()
    }

    private fun callDealsAndEssentialsList(isLoadMore: Boolean) {
        if (pageCountEssentialProducts < totalEssentialProductsPageCount) {
            pageCountEssentialProducts = pageCountEssentialProducts + 1
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setSearchText(searchText!!)
            apiParamsHelper.setPageSize(20)
            apiParamsHelper.setPageNumber(pageCountEssentialProducts)
            EssentialsApi.getSearchedEssentialAndDeals(
                this,
                apiParamsHelper,
                object : EssentialsApi.EssentialsProductsSearchResponseListener {
                    override fun onSuccessResponse(essentialsList: SearchEssentialAndDealsModel?) {
                        if (essentialsList != null && essentialsList.data != null) {
                            if (essentialsList.data!!.products != null) {
                                totalEssentialProductsPageCount =
                                    essentialsList.data!!.products?.numberOfPages!!
                                val orders: List<SearchedEssentialAndDealsPruducts> =
                                    essentialsList.data!!.products?.data!!
                                val fetchedOrders: ArrayList<SearchedEssentialAndDealsPruducts> =
                                    ArrayList<SearchedEssentialAndDealsPruducts>(orders)
                                if (isLoadMore && essentialsProducts != null && essentialsProducts!!.size > 0) {
                                    essentialsProducts!!.addAll(fetchedOrders)
                                } else {
                                    essentialsProducts =
                                        ArrayList<SearchedEssentialAndDealsPruducts>(orders)
                                    lstSearchEssentialsAndDealsData =
                                        ArrayList<SearchForNewOrderMgr>()
                                }
                                if (isLoadMore) {
                                    setData(fetchedOrders)
                                } else {
                                    if (essentialsList.data!!
                                            .products != null
                                    ) totalProductsEssential =
                                        essentialsList.data!!.products?.numberOfRecords!!
                                    setData(essentialsList, fetchedOrders, isLoadMore)
                                }
                            } else {
                                setData(essentialsList, null, isLoadMore)
                            }
                        } else {
                            lytTabs.setVisibility(View.VISIBLE)
                            if (isEssentialTabSelected) {
                                setEssentialsTabActive()
                            } else {
                                setMySupplierTabActive()
                            }
                            if (suppliersCount > 0) {
                                txtMySupplierCount?.setVisibility(View.VISIBLE)
                                val count = suppliersCount.toString() + ""
                                txtMySupplierCount?.setText(count)
                            } else {
                                txtMySupplierCount?.setVisibility(View.GONE)
                            }
                            if (essentialsCount > 0) {
                                txtEssentialsCount?.setVisibility(View.VISIBLE)
                                val count = essentialsCount.toString() + ""
                                txtEssentialsCount?.setText(count)
                            } else {
                                txtEssentialsCount?.setVisibility(View.GONE)
                            }
                            threeDotLoaderWhite.setVisibility(View.GONE)
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        lytTabs.setVisibility(View.VISIBLE)
                        if (isEssentialTabSelected) {
                            setEssentialsTabActive()
                        } else {
                            setMySupplierTabActive()
                        }
                        if (suppliersCount > 0) {
                            txtMySupplierCount?.setVisibility(View.VISIBLE)
                            val count = suppliersCount.toString() + ""
                            txtMySupplierCount?.setText(count)
                        } else {
                            txtMySupplierCount?.setVisibility(View.GONE)
                        }
                        if (essentialsCount > 0) {
                            txtEssentialsCount?.setVisibility(View.VISIBLE)
                            val count = essentialsCount.toString() + ""
                            txtEssentialsCount?.setText(count)
                        } else {
                            txtEssentialsCount?.setVisibility(View.GONE)
                        }
                        threeDotLoaderWhite.setVisibility(View.GONE)
                    }
                })
        }
    }

    private fun setData(fetchedOrders: ArrayList<SearchedEssentialAndDealsPruducts>) {
        if (lstSearchEssentialsAndDealsData != null && lstSearchEssentialsAndDealsData!!.size > 0) {
            val products: List<SearchedEssentialAndDealsPruducts> = ArrayList<SearchedEssentialAndDealsPruducts>(fetchedOrders)
            essentialProductList = ArrayList<ProductDetailBySupplier>()
            if (products.size > 0) {
                for (i in products.indices) {
                    val productUom: ProductDetailBySupplier =
                        ProductDataHelper.getEssentialProducts(
                            products[i]
                        )
                    val productPriceLists: MutableList<ProductPriceList> =
                        ArrayList<ProductPriceList>()
                    products[i].unitPrices?.let { productPriceLists.add(it) }
                    productUom.priceList = productPriceLists
                    productUom.isHasMultipleUom = true
                    essentialProductList!!.add(productUom)
                }
                for (i in essentialProductList!!.indices) {
                    val searchNewOrdersManagerOrder = SearchForNewOrderMgr()
                    searchNewOrdersManagerOrder.productDetailBySupplier = (essentialProductList!![i])
                    lstSearchEssentialsAndDealsData!!.add(searchNewOrdersManagerOrder)
                }
            }
            if (lstEssentials.getAdapter() != null) {
                lstEssentials.getAdapter()!!.notifyDataSetChanged()
            }
        }
        threeDotLoaderWhite.setVisibility(View.GONE)
    }

    private fun setData(
        searchEssentialAndDealsModel: SearchEssentialAndDealsModel,
        fetchedOrders: ArrayList<SearchedEssentialAndDealsPruducts>?,
        isLoadMore: Boolean
    ) {
        if (searchEssentialAndDealsModel.data != null && searchEssentialAndDealsModel.data!!
                .categories != null && searchEssentialAndDealsModel.data!!.categories
                ?.size!! > 0
        ) {
            val searchForNewOrder = SearchForNewOrderMgr()
            searchForNewOrder.isHeader
            if (searchEssentialAndDealsModel.data!!.categories?.size!! === 1) {
                searchForNewOrder.header = (getResources().getString(R.string.txt_small_category))
            } else {
                searchForNewOrder.header = (getResources().getString(R.string.txt_categories))
            }
            searchForNewOrder.headerCount = (
                searchEssentialAndDealsModel.data!!.categories?.size!!
            )
            essentialsCount =
                essentialsCount + searchEssentialAndDealsModel.data!!.categories?.size!!
            lstSearchEssentialsAndDealsData!!.add(searchForNewOrder)
            if (!isCategoriesViewAllSelected && searchEssentialAndDealsModel.data!!
                    .categories?.size!! > LIMIT_NUMBER_OF_RECORDS
            ) {
                for (i in 0 until LIMIT_NUMBER_OF_RECORDS) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.categoriesList = (
                        searchEssentialAndDealsModel.data!!.categories?.get(i)
                    )
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
                val searchForNewOrderViewAll = SearchForNewOrderMgr()
                searchForNewOrderViewAll.isViewAll
                searchForNewOrderViewAll.viewAllFor = (ZeemartAppConstants.VIEW_ALL_FOR_CATEGORIES)
                lstSearchEssentialsAndDealsData!!.add(searchForNewOrderViewAll)
            } else {
                for (i in 0 until searchEssentialAndDealsModel.data!!.categories?.size!!) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.categoriesList = (
                        searchEssentialAndDealsModel.data!!.categories?.get(i)
                    )
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
            }
        }
        if (searchEssentialAndDealsModel.data != null && searchEssentialAndDealsModel.data!!
                .suppliers != null && searchEssentialAndDealsModel.data!!.suppliers
                ?.size!! > 0
        ) {
            val searchForNewOrder = SearchForNewOrderMgr()
            searchForNewOrder.isHeader
            if (searchEssentialAndDealsModel.data!!.suppliers?.size!! === 1) {
                searchForNewOrder.header = (getResources().getString(R.string.txt_supplier_small))
            } else {
                searchForNewOrder.header = (getResources().getString(R.string.txt_suppliers))
            }
            searchForNewOrder.headerCount = (
                searchEssentialAndDealsModel.data!!.suppliers?.size!!
            )
            essentialsCount =
                essentialsCount + searchEssentialAndDealsModel.data!!.suppliers?.size!!
            lstSearchEssentialsAndDealsData!!.add(searchForNewOrder)
            if (!isSupplierViewAllSelected && searchEssentialAndDealsModel.data!!.suppliers
                    ?.size!! > LIMIT_NUMBER_OF_RECORDS
            ) {
                for (i in 0 until LIMIT_NUMBER_OF_RECORDS) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.suppliersList = (
                        searchEssentialAndDealsModel.data!!.suppliers?.get(i)
                    )
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
                val searchForNewOrderViewAll = SearchForNewOrderMgr()
                searchForNewOrderViewAll.isViewAll
                searchForNewOrderViewAll.viewAllFor = (ZeemartAppConstants.VIEW_ALL_FOR_SUPPLIERS)
                lstSearchEssentialsAndDealsData!!.add(searchForNewOrderViewAll)
            } else {
                for (i in 0 until searchEssentialAndDealsModel.data!!.suppliers?.size!!) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.suppliersList = (
                        searchEssentialAndDealsModel.data!!.suppliers?.get(i)
                    )
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
            }
        }
        if (fetchedOrders != null && fetchedOrders.size > 0) {
            val products: List<SearchedEssentialAndDealsPruducts> = ArrayList<SearchedEssentialAndDealsPruducts>(fetchedOrders)
            essentialProductList = ArrayList<ProductDetailBySupplier>()
            if (products != null && products.size > 0) {
                if (!isLoadMore) {
                    val searchForNewOrderManager = SearchForNewOrderMgr()
                    searchForNewOrderManager.isHeader
                    if (products.size == 1) {
                        searchForNewOrderManager.header = (getResources().getString(R.string.txt_product))
                    } else {
                        searchForNewOrderManager.header = (getResources().getString(R.string.txt_products))
                    }
                    searchForNewOrderManager.headerCount = (totalProductsEssential)
                    essentialsCount = essentialsCount + totalProductsEssential
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderManager)
                }
                for (i in products.indices) {
                    val productUom: ProductDetailBySupplier =
                        ProductDataHelper.getEssentialProducts(
                            products[i]
                        )
                    val productPriceLists: MutableList<ProductPriceList> =
                        ArrayList<ProductPriceList>()
                    products[i].unitPrices?.let { productPriceLists.add(it) }
                    productUom.priceList = productPriceLists
                    productUom.isHasMultipleUom = true
                    essentialProductList!!.add(productUom)
                }
                for (i in essentialProductList!!.indices) {
                    val searchNewOrdersManagerOrder = SearchForNewOrderMgr()
                    searchNewOrdersManagerOrder.productDetailBySupplier = (essentialProductList!![i])
                    lstSearchEssentialsAndDealsData!!.add(searchNewOrdersManagerOrder)
                }
            }
        }
        if (lstEssentials.getAdapter() != null) {
            (lstEssentials.getAdapter() as SearchEssentialsAndDealsAdapter).notifyDataSetChanged()
        } else {
            lstEssentials.setAdapter(
                SearchEssentialsAndDealsAdapter(
                    this,
                    lstSearchEssentialsAndDealsData,
                    supplierDetails,
                    object : SearchEssentialsAndDealsAdapter.OnItemClicked {
                        override fun onEssentialOrDealSupplierClicked(supplier: SearchedEssentialAndDealsSuppliers?) {
                            if (!StringHelper.isStringNullOrEmpty(
                                    supplier?.supplier?.supplierName
                                )
                            ) {
                                setRecentSearchList(supplier?.supplier?.supplierName!!)
                            }
                            threeDotLoaderWhite.setVisibility(View.VISIBLE)
                            if (!StringHelper.isStringNullOrEmpty(supplier?.essentialsId)) {
                                moveToEssentialProductScreen(supplier?.essentialsId!!, null)
                            } else if (!StringHelper.isStringNullOrEmpty(supplier?.dealNumber)) {
                                moveToDealsProductScreen(supplier?.dealNumber!!, null)
                            }
                        }

                        override fun onEssentialOrDealProductClicked(products: ProductDetailBySupplier?) {
                            if (!StringHelper.isStringNullOrEmpty(products?.productName)) {
                                setRecentSearchList(products?.productName!!)
                            }
                            threeDotLoaderWhite.setVisibility(View.VISIBLE)
                            if (!StringHelper.isStringNullOrEmpty(products?.essentialsId)) {
                                moveToEssentialProductScreen(products?.essentialsId!!, products)
                            } else if (!StringHelper.isStringNullOrEmpty(products?.dealNumber)) {
                                moveToDealsProductScreen(products?.dealNumber!!, products)
                            }
                        }

                        override fun onCategoryClicked(category: String?) {
                            if (!StringHelper.isStringNullOrEmpty(category)) {
                                setRecentSearchList(category!!)
                            }
                        }
                    },
                    object : SearchEssentialsAndDealsAdapter.onViewAllClickListener {
                        override fun onViewAllSupplierClicked() {
                            isSupplierViewAllSelected = true
                            lstSearchEssentialsAndDealsData!!.clear()
                            essentialsCount = 0
                            setData(searchEssentialAndDealsModel, essentialsProducts, false)
                        }

                        override fun onViewAllCategoryClicked() {
                            isCategoriesViewAllSelected = true
                            lstSearchEssentialsAndDealsData!!.clear()
                            essentialsCount = 0
                            setData(searchEssentialAndDealsModel, essentialsProducts, false)
                        }
                    })
            )
            (lstEssentials.getAdapter() as SearchEssentialsAndDealsAdapter).getFilter()
                .filter(edtSearch.getText().toString())
        }
        threeDotLoaderWhite.setVisibility(View.GONE)
        lytTabs.setVisibility(View.VISIBLE)
        if (isEssentialTabSelected) {
            setEssentialsTabActive()
        } else {
            setMySupplierTabActive()
        }
        if (suppliersCount > 0) {
            txtMySupplierCount?.setVisibility(View.VISIBLE)
            val count = suppliersCount.toString() + ""
            txtMySupplierCount?.setText(count)
        } else {
            txtMySupplierCount?.setVisibility(View.GONE)
        }
        if (essentialsCount > 0) {
            txtEssentialsCount?.setVisibility(View.VISIBLE)
            val count = essentialsCount.toString() + ""
            txtEssentialsCount?.setText(count)
        } else {
            txtEssentialsCount?.setVisibility(View.GONE)
        }
    }

    private fun moveToDealsProductScreen(
        DealNumber: String,
        productDetailBySupplier: ProductDetailBySupplier?
    ) {
        OrderHelper.getActiveDeals(this, DealNumber, object : OrderHelper.DealsResponseListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsBaseResponse?) {
                if (dealsBaseResponse != null && dealsBaseResponse.deals != null && dealsBaseResponse.deals!!
                        .size!! > 0
                ) {
                    val intent = Intent(this@SearchForNewOrder, DealProductListActivity::class.java)
                    intent.putExtra(
                        ZeemartAppConstants.DEALS_LIST,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            dealsBaseResponse.deals!!.get(0)
                        )
                    )
                    intent.putExtra(
                        ZeemartAppConstants.DEALS_NUMBER,
                        dealsBaseResponse.deals!!.get(0).dealNumber
                    )
                    if (productDetailBySupplier != null) intent.putExtra(
                        ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetailBySupplier)
                    )
                    startActivity(intent)
                }
                threeDotLoaderWhite.setVisibility(View.GONE)
            }

            override fun onErrorResponse(error: VolleyError?) {
                threeDotLoaderWhite.setVisibility(View.GONE)
            }
        })
    }

    private fun moveToEssentialProductScreen(
        EssentialId: String,
        productDetailBySupplier: ProductDetailBySupplier?
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setEssentialsId(EssentialId)
        EssentialsApi.getPaginatedEssentials(
            this,
            apiParamsHelper,
            object : EssentialsApi.EssentialsResponseListener {
                override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                    if (essentialsList != null && essentialsList.essentials != null && essentialsList.essentials!!.size > 0) {
                        val intent = Intent(
                            this@SearchForNewOrder,
                            EssentialsProductListActivity::class.java
                        )
                        intent.putExtra(
                            ZeemartAppConstants.ESSENTIALS_LIST,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                essentialsList.essentials!!.get(0)
                            )
                        )
                        intent.putExtra(
                            ZeemartAppConstants.ESSENTIALS_ID,
                            essentialsList.essentials!!.get(0).essentialsId
                        )
                        if (productDetailBySupplier != null) intent.putExtra(
                            ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetailBySupplier)
                        )
                        intent.putExtra(
                            ZeemartAppConstants.CALLED_FROM,
                            ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST
                        )
                        startActivityForResult(intent, REQUEST_CODE_UPDATE_CART_COUNT)
                    }
                    threeDotLoaderWhite.setVisibility(View.GONE)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite.setVisibility(View.GONE)
                }
            })
    }

    private fun setRecentSearchList(name: String) {
        if (!recentSearchList!!.contains(name)) {
            recentSearchList!!.add(TOP, name)
        }
        saveRecentSearchInSharedPref(recentSearchList as MutableList<String>?)
        setRecentSearchListAdapter(recentSearchList as MutableList<String?>)
    }

    private fun setSupplierName() {
        if (supplierList != null && supplierList!!.size > 0 && productList != null && productList!!.size > 0) {
            for (i in supplierList!!.indices) {
                for (j in productList!!.indices) {
                    if (supplierList!![i].supplier.supplierId != null && productList!![j].supplierId != null && supplierList!![i].supplier.supplierId == productList!![j].supplierId) {
                        productList!![j].supplier = supplierList!![i].supplier
                        val supplierDetailsJson: String =
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                productList!![j]
                            )
                        supplierDetails =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson<DetailSupplierDataModel>(
                                supplierDetailsJson,
                                DetailSupplierDataModel::class.java
                            )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE_CART_COUNT && resultCode == Activity.RESULT_OK) {
            callSupplierList(false)
        }
        if (resultCode == OrderDetailsActivity.IS_STATUS_CHANGED) {
            callSupplierList(false)
        }
    }

    private fun setRecentSearchListAdapter(recentSearchLst: MutableList<String?>) {
        if (recentSearchLst.size > 0) for (i in recentSearchLst.indices) {
            if (recentSearchLst.size > 10) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val adapter: ArrayAdapter<String?> = object : ArrayAdapter<String?>(
            getApplicationContext(),
            android.R.layout.simple_list_item_1, recentSearchLst
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View = super.getView(position, convertView, parent)
                val typeface: Typeface = Typeface.createFromAsset(
                    getContext().getAssets(),
                    "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
                )
                val text: TextView = view.findViewById<View>(android.R.id.text1) as TextView
                text.setTypeface(typeface)
                return view
            }
        }
        lstRecentSearch!!.adapter = adapter
    }

    private fun saveRecentSearchInSharedPref(recentSearchLst: MutableList<String>?) {
        for (i in recentSearchLst!!.indices) {
            if (recentSearchLst.size > 10) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val recentSearchJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(recentSearchList)
        SharedPref.write(SharedPref.ORDER_BY_PRODUCT_RECENT_SEARCH, recentSearchJson)
    }

    protected override fun onResume() {
        super.onResume()
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                showProductListCalledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        if (showProductListCalledFrom != null && showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_SUPPLIER_ADD_TO_ORDER) {
            callSupplierList(false)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
        if (threeDotLoaderWhite.getVisibility() == View.GONE) {
            createOrEditDraftOrder(object : CreateDraftOrderResponseListener {
                override fun doNavigation(response: String?) {
                    finish()
                }
            })
        }
    }

    private interface CreateDraftOrderResponseListener {
        fun doNavigation(response: String?)
    }

    private fun removeSelectedProductToDraftList(productDetailBySupplier: ProductDetailBySupplier) {
        for (i in selectedProductList?.indices!!) {
            if (selectedProductList[i]!!.sku == productDetailBySupplier.sku) selectedProductList.removeAt(
                i
            )
        }
    }

    private inner class FilterSuppliers : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterResults = FilterResults()
            var searchedString = ""
            if (charSequence != null && charSequence.length > 0) {
                searchedString = charSequence as String
                val tempList: MutableList<DetailSupplierDataModel> =
                    ArrayList<DetailSupplierDataModel>()
                // search content in friend list
                if (supplierList != null) for (i in supplierList!!.indices) {
                    if (supplierList!![i].supplier.supplierName?.lowercase(Locale.getDefault())
                            ?.contains(
                                charSequence.toString().lowercase(
                                    Locale.getDefault()
                                )
                            ) == true
                    ) {
                        tempList.add(supplierList!![i])
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
            } else {
                searchedString = ""
                filterResults.count = supplierList!!.size
                filterResults.values = supplierList
            }
            return filterResults
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            if (filterResults.values != null && filterResults.count > 0) {
                filterSupplierList = filterResults.values as List<DetailSupplierDataModel>
                if (filterSupplierList != null) suppliersCount =
                    suppliersCount + filterSupplierList!!.size
                val searchForNewOrderMgr = SearchForNewOrderMgr()
                searchForNewOrderMgr.isHeader
                if (filterSupplierList!!.size == 1) {
                    searchForNewOrderMgr.header = (getResources().getString(R.string.txt_supplier_small))
                } else {
                    searchForNewOrderMgr.header = (getResources().getString(R.string.txt_suppliers))
                }
                searchForNewOrderMgr.headerCount = (filterSupplierList!!.size)
                lstSearchData = ArrayList<SearchForNewOrderMgr>()
                lstSearchData!!.add(searchForNewOrderMgr)
                if (!isMySuppliersViewAllSelected && filterSupplierList!!.size > LIMIT_NUMBER_OF_RECORDS) {
                    for (i in 0 until LIMIT_NUMBER_OF_RECORDS) {
                        val searchOrdersManagerOrder = SearchForNewOrderMgr()
                        searchOrdersManagerOrder.detailSupplierDataModel = (filterSupplierList!![i])
                        lstSearchData!!.add(searchOrdersManagerOrder)
                    }
                    val searchForNewOrderMgrs = SearchForNewOrderMgr()
                    searchForNewOrderMgrs.isViewAll
                    lstSearchData!!.add(searchForNewOrderMgrs)
                } else {
                    for (i in filterSupplierList!!.indices) {
                        val searchOrdersManagerOrder = SearchForNewOrderMgr()
                        searchOrdersManagerOrder.detailSupplierDataModel = (filterSupplierList!![i])
                        lstSearchData!!.add(searchOrdersManagerOrder)
                    }
                }
            } else {
                lstSearchData = ArrayList<SearchForNewOrderMgr>()
                filterSupplierList = ArrayList<DetailSupplierDataModel>()
            }
        }
    }

    private fun setMySupplierTabActive() {
        lstEssentials.setVisibility(View.GONE)
        mySuppliersScrollHelper.setOnScrollListener()
        btnMySuppliers?.setTextColor(getResources().getColor(R.color.black))
        txtMySuppliersHighLight?.setVisibility(View.VISIBLE)
        btnDealsAndEssentials?.setTextColor(getResources().getColor(R.color.grey_medium))
        txtDealsAndEssentialsHighLight?.setVisibility(View.GONE)
        setReportSearchLayout()
        setLayoutReviewButton(cartItemCount)
        if (suppliersCount > 0) {
            txtMySupplierCount?.setVisibility(View.VISIBLE)
            val count = suppliersCount.toString() + ""
            txtMySupplierCount?.setText(count)
        } else {
            txtMySupplierCount?.setVisibility(View.GONE)
        }
    }

    private fun setEssentialsTabActive() {
        essentialSuppliersScrollHelper.setOnScrollListener()
        lstSupplier.setVisibility(View.GONE)
        btnDealsAndEssentials?.setTextColor(getResources().getColor(R.color.black))
        txtDealsAndEssentialsHighLight?.setVisibility(View.VISIBLE)
        btnMySuppliers?.setTextColor(getResources().getColor(R.color.grey_medium))
        txtMySuppliersHighLight?.setVisibility(View.GONE)
        setRecentSearchListVisibility()
        setLayoutReviewButton(cartItemCount)
        setEssentialListVisibility()
        if (essentialsCount > 0) {
            txtEssentialsCount?.setVisibility(View.VISIBLE)
            val count = essentialsCount.toString() + ""
            txtEssentialsCount?.setText(count)
        } else {
            txtEssentialsCount?.setVisibility(View.GONE)
        }
    }

    companion object {
        private const val TOP = 0
        private const val REQUEST_CODE_UPDATE_CART_COUNT = 1
        const val LIMIT_NUMBER_OF_RECORDS = 10
        private var pageCountMyProducts = 0
        private var totalProductsMySupplier = 0
        private var totalProductsEssential = 0
        private var pageCountEssentialProducts = 0
    }
}