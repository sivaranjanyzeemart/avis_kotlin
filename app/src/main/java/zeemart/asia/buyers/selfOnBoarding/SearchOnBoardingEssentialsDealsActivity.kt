package zeemart.asia.buyers.selfOnBoarding

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SelfOnBoardingSearchEssentialsAndDealsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.pagination.DeliveryScrollHelper
import zeemart.asia.buyers.helper.pagination.PaginationListScrollHelper.ScrollCallback
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.order.SearchForNewOrderMgr
import zeemart.asia.buyers.modelsimport.DealsResponseForOnBoarding
import zeemart.asia.buyers.modelsimport.EssentialsResponseForOnBoarding
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsSuppliers
import zeemart.asia.buyers.modelsimport.SelfOnBoardingSearchEssentialAndDealsModel
import zeemart.asia.buyers.network.EssentialsApi
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseOnBoardingListener
import zeemart.asia.buyers.network.EssentialsApi.SelfOnBoardingEssentialsProductsSearchResponseListener
import zeemart.asia.buyers.network.EssentialsApi.getSearchedEssentialAndDealsForOnBoarding
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderHelper.DealsOnBoardingResponseListener
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.util.*

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class SearchOnBoardingEssentialsDealsActivity : AppCompatActivity() {
    private var txtRecentSearchHeader: TextView? = null
    private lateinit var txtSearchClear: TextView
    private lateinit var txtNoResult: TextView
    private var txtRetry: TextView? = null
    private var textSearch: TextView? = null
    private lateinit var txtCancel: TextView
    private lateinit var lstRecentSearch: ListView
    private lateinit var edtSearch: EditText
    private lateinit var edtSearchClear: ImageView
    private lateinit var lytNoResults: RelativeLayout
    private var recentSearchList: MutableList<String?>? = ArrayList()
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    private var essentialProductList: MutableList<ProductDetailBySupplier>? = null
    private var layoutSearch: RelativeLayout? = null
    private var searchText: String? = null
    private var lstSearchEssentialsAndDealsData: ArrayList<SearchForNewOrderMgr>? = ArrayList()
    private lateinit var lstEssentials: RecyclerView
    private var showProductListCalledFrom: String? = null
    val handler = Handler()
    var runnable: Runnable? = null
    private var isSupplierViewAllSelected = false
    private var isCategoriesViewAllSelected = false
    private var essentialSuppliersScrollHelper: DeliveryScrollHelper? = null
    private var totalEssentialProductsPageCount = 1
    private var lstEssentialProductsLayoutManager: LinearLayoutManager? = null
    private var essentialsProducts: ArrayList<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts>? =
        null
    private var isAutomaticSearchApplied = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_on_boarding_essentials_deals)
        pageCountEssentialProducts = 0
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        txtRecentSearchHeader = findViewById(R.id.txt_recent_search)
        txtNoResult = findViewById(R.id.txt_no_search_found)
        txtRetry = findViewById(R.id.txt_try_with_other)
        txtCancel = findViewById(R.id.search_btn_cancel)
        txtCancel.setOnClickListener(View.OnClickListener {
            if (threeDotLoaderWhite!!.visibility == View.GONE) {
                finish()
            }
        })
        txtSearchClear = findViewById(R.id.txt_recent_search_clear)
        layoutSearch = findViewById(R.id.lyt_search)
        textSearch = findViewById(R.id.txt_search)
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_report_search_white)
        txtSearchClear.setOnClickListener(View.OnClickListener {
            recentSearchList!!.clear()
            setRecentSearchListAdapter(recentSearchList)
            setRecentSearchListVisibility()
            SharedPref.removeString(SharedPref.ORDER_BY_PRODUCT_RECENT_SEARCH)
        })
        lytNoResults = findViewById(R.id.lyt_no_search_results)
        lytNoResults.setVisibility(View.GONE)
        lstRecentSearch = findViewById(R.id.lst_recent_search)
        lstRecentSearch.setOnItemClickListener(AdapterView.OnItemClickListener { arg0, arg1, position, id ->
            val name = recentSearchList!![position]
            if (name != null) {
                edtSearch!!.setText(name)
                edtSearch!!.setSelection(edtSearch!!.text.length)
            }
        })
        val jsonText = SharedPref.read(SharedPref.ORDER_BY_PRODUCT_RECENT_SEARCH, "")
        val lstRecentSearch =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson(jsonText, Array<String>::class.java)
        if (lstRecentSearch != null) {
            recentSearchList = ArrayList(Arrays.asList(*lstRecentSearch))
        }
        setRecentSearchListAdapter(recentSearchList)
        edtSearch = findViewById(R.id.edit_search)
        edtSearch.requestFocus()
        edtSearchClear = findViewById(R.id.edit_search_clear)
        edtSearch.setHint(resources.getString(R.string.txt_search_products_or_suppliers))
        edtSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!StringHelper.isStringNullOrEmpty(searchText)) {
                    setRecentSearchList(searchText)
                }
                threeDotLoaderWhite.setVisibility(View.VISIBLE)
                callSupplierList(false)
            }
            false
        })
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                handler.removeCallbacks(runnable!!)
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                searchText = edtSearch.getText().toString().trim { it <= ' ' }
                if (charSequence.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear.setVisibility(View.VISIBLE)
                } else {
                    edtSearchClear.setVisibility(View.GONE)
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
        edtSearchClear.setOnClickListener(View.OnClickListener {
            edtSearch.setText("")
            setRecentSearchListVisibility()
        })
        lstEssentials = findViewById(R.id.lst_search_essentials)
        lstEssentialProductsLayoutManager = LinearLayoutManager(this)
        lstEssentials.setLayoutManager(lstEssentialProductsLayoutManager)
        lstEssentials.setVisibility(View.GONE)
        essentialSuppliersScrollHelper = DeliveryScrollHelper(this, lstEssentials,
            lstEssentialProductsLayoutManager!!, object : ScrollCallback {
                override fun loadMore() {
                    if (pageCountEssentialProducts < totalEssentialProductsPageCount) {
                        callDealsAndEssentialsList(true)
                    }
                }
            }
        )
        essentialSuppliersScrollHelper!!.setOnScrollListener()
        setRecentSearchListVisibility()
        setFont()
    }

    private fun setEssentialListVisibility() {
        if (lstSearchEssentialsAndDealsData != null && lstSearchEssentialsAndDealsData!!.size > 0 && !StringHelper.isStringNullOrEmpty(
                edtSearch!!.text.toString()
            )
        ) {
            lstEssentials!!.visibility = View.VISIBLE
            layoutSearch!!.visibility = View.GONE
            lytNoResults!!.visibility = View.GONE
        } else {
            lytNoResults!!.visibility = View.VISIBLE
            lstEssentials!!.visibility = View.GONE
            layoutSearch!!.visibility = View.GONE
        }
        if (!isAutomaticSearchApplied) CommonMethods.hideKeyboard(this@SearchOnBoardingEssentialsDealsActivity)
    }

    private fun setRecentSearchListVisibility() {
        if (recentSearchList != null && recentSearchList!!.size > 0 && StringHelper.isStringNullOrEmpty(
                edtSearch!!.text.toString()
            )
        ) {
            lstRecentSearch!!.visibility = View.VISIBLE
            txtRecentSearchHeader!!.visibility = View.VISIBLE
            txtSearchClear!!.visibility = View.VISIBLE
            layoutSearch!!.visibility = View.GONE
            threeDotLoaderWhite!!.visibility = View.GONE
            lytNoResults!!.visibility = View.GONE
            lstEssentials!!.visibility = View.GONE
        } else {
            lstRecentSearch!!.visibility = View.GONE
            txtRecentSearchHeader!!.visibility = View.GONE
            txtSearchClear!!.visibility = View.GONE
            layoutSearch!!.visibility = View.VISIBLE
        }
    }

    private fun setFont() {
        setTypefaceView(
            txtRecentSearchHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtSearchClear, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtNoResult, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(textSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCancel, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtRetry, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    private fun callSupplierList(isAutoSearch: Boolean) {
        isAutomaticSearchApplied = isAutoSearch
        pageCountEssentialProducts = 0
        totalEssentialProductsPageCount = 1
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        isCategoriesViewAllSelected = false
        isSupplierViewAllSelected = false
        essentialsProducts = null
        lstSearchEssentialsAndDealsData = null
        lstEssentials!!.adapter = null
        lytNoResults!!.visibility = View.GONE
        essentialSuppliersScrollHelper!!.updateScrollListener(
            lstEssentials,
            lstEssentialProductsLayoutManager
        )
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        callDealsAndEssentialsList(false)
    }

    private fun callDealsAndEssentialsList(isLoadMore: Boolean) {
        if (pageCountEssentialProducts < totalEssentialProductsPageCount) {
            pageCountEssentialProducts = pageCountEssentialProducts + 1
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setSearchText(searchText!!)
            apiParamsHelper.setPageSize(20)
            apiParamsHelper.setPageNumber(pageCountEssentialProducts)
            getSearchedEssentialAndDealsForOnBoarding(
                this,
                apiParamsHelper,
                object : SelfOnBoardingEssentialsProductsSearchResponseListener {
                    override fun onSuccessResponse(essentialsList: SelfOnBoardingSearchEssentialAndDealsModel?) {
                        if (essentialsList != null && essentialsList.data != null) {
                            if (essentialsList.data!!.products != null) {
                                totalEssentialProductsPageCount =
                                    essentialsList.data!!.products?.numberOfPages!!
                                val orders: List<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts> =
                                    essentialsList.data!!.products?.data!!
                                val fetchedOrders: ArrayList<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts> =
                                    ArrayList<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts>(orders)
                                if (isLoadMore && essentialsProducts != null && essentialsProducts!!.size > 0) {
                                    essentialsProducts!!.addAll(fetchedOrders)
                                } else {
                                    essentialsProducts =
                                        ArrayList<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts>(
                                            orders
                                        )
                                    lstSearchEssentialsAndDealsData = ArrayList()
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
                        }
                        threeDotLoaderWhite.visibility = View.GONE
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        setEssentialsTabActive()
                        threeDotLoaderWhite.visibility = View.GONE
                    }
                })
        }
    }

    private fun setData(fetchedOrders: ArrayList<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts>) {
        if (lstSearchEssentialsAndDealsData != null && lstSearchEssentialsAndDealsData!!.size > 0) {
            val products: List<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts> =
                ArrayList(fetchedOrders)
            essentialProductList = ArrayList()
            if (products.size > 0) {
                for (i in products.indices) {
                    val productUom = ProductDataHelper.getEssentialProducts(products[i])
                    val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                    productPriceLists.add(products[i].unitPrices!![0])
                    productUom.priceList = productPriceLists
                    productUom.isHasMultipleUom = true
                    (essentialProductList as ArrayList<ProductDetailBySupplier>).add(productUom)
                }
                for (i in (essentialProductList as ArrayList<ProductDetailBySupplier>).indices) {
                    val searchNewOrdersManagerOrder = SearchForNewOrderMgr()
                    searchNewOrdersManagerOrder.productDetailBySupplier =
                        (essentialProductList as ArrayList<ProductDetailBySupplier>).get(i)
                    lstSearchEssentialsAndDealsData!!.add(searchNewOrdersManagerOrder)
                }
            }
            if (lstEssentials!!.adapter != null) {
                lstEssentials!!.adapter!!.notifyDataSetChanged()
            }
        }
        threeDotLoaderWhite!!.visibility = View.GONE
    }

    private fun setData(
        searchEssentialAndDealsModel: SelfOnBoardingSearchEssentialAndDealsModel,
        fetchedOrders: ArrayList<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts>?,
        isLoadMore: Boolean,
    ) {
        if (searchEssentialAndDealsModel.data != null && searchEssentialAndDealsModel.data!!.categories != null && searchEssentialAndDealsModel.data!!.categories!!.size > 0) {
            val searchForNewOrder = SearchForNewOrderMgr()
            searchForNewOrder.isHeader = true
            if (searchEssentialAndDealsModel.data!!.categories?.size == 1) {
                searchForNewOrder.header = (resources.getString(R.string.txt_small_category))
            } else {
                searchForNewOrder.header = (resources.getString(R.string.txt_categories))
            }
            searchForNewOrder.headerCount = searchEssentialAndDealsModel.data!!.categories?.size!!
            lstSearchEssentialsAndDealsData!!.add(searchForNewOrder)
            if (!isCategoriesViewAllSelected && searchEssentialAndDealsModel.data!!.categories!!.size > LIMIT_NUMBER_OF_RECORDS) {
                for (i in 0 until LIMIT_NUMBER_OF_RECORDS) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.categoriesList =
                        searchEssentialAndDealsModel.data!!.categories!![i]
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
                val searchForNewOrderViewAll = SearchForNewOrderMgr()
                searchForNewOrderViewAll.isViewAll = true
                searchForNewOrderViewAll.viewAllFor = ZeemartAppConstants.VIEW_ALL_FOR_CATEGORIES
                lstSearchEssentialsAndDealsData!!.add(searchForNewOrderViewAll)
            } else {
                for (i in searchEssentialAndDealsModel.data!!.categories?.indices!!) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.categoriesList =
                        searchEssentialAndDealsModel.data!!.categories!![i]
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
            }
        }
        if (searchEssentialAndDealsModel.data != null && searchEssentialAndDealsModel.data!!.suppliers != null && searchEssentialAndDealsModel.data!!.suppliers!!.size > 0) {
            val searchForNewOrder = SearchForNewOrderMgr()
            searchForNewOrder.isHeader = true
            if (searchEssentialAndDealsModel.data!!.suppliers!!.size == 1) {
                searchForNewOrder.header = (resources.getString(R.string.txt_supplier_small))
            } else {
                searchForNewOrder.header = (resources.getString(R.string.txt_suppliers))
            }
            searchForNewOrder.headerCount = searchEssentialAndDealsModel.data!!.suppliers!!.size
            lstSearchEssentialsAndDealsData!!.add(searchForNewOrder)
            if (!isSupplierViewAllSelected && searchEssentialAndDealsModel.data!!.suppliers!!.size > LIMIT_NUMBER_OF_RECORDS) {
                for (i in 0 until LIMIT_NUMBER_OF_RECORDS) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.suppliersList =
                        searchEssentialAndDealsModel.data!!.suppliers!![i]
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
                val searchForNewOrderViewAll = SearchForNewOrderMgr()
                searchForNewOrderViewAll.isViewAll = true
                searchForNewOrderViewAll.viewAllFor = ZeemartAppConstants.VIEW_ALL_FOR_SUPPLIERS
                lstSearchEssentialsAndDealsData!!.add(searchForNewOrderViewAll)
            } else {
                for (i in searchEssentialAndDealsModel.data!!.suppliers!!.indices) {
                    val searchForNewOrderSupplier = SearchForNewOrderMgr()
                    searchForNewOrderSupplier.suppliersList =
                        searchEssentialAndDealsModel.data!!.suppliers!![i]
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderSupplier)
                }
            }
        }
        if (fetchedOrders != null && fetchedOrders.size > 0) {
            val products: List<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts> =
                ArrayList(fetchedOrders)
            essentialProductList = ArrayList()
            if (products != null && products.size > 0) {
                if (!isLoadMore) {
                    val searchForNewOrderManager = SearchForNewOrderMgr()
                    searchForNewOrderManager.isHeader = true
                    if (products.size == 1) {
                        searchForNewOrderManager.header = (resources.getString(R.string.txt_product))
                    } else {
                        searchForNewOrderManager.header = (resources.getString(R.string.txt_products))
                    }
                    searchForNewOrderManager.headerCount = totalProductsEssential
                    lstSearchEssentialsAndDealsData!!.add(searchForNewOrderManager)
                }
                for (i in products.indices) {
                    val productUom = ProductDataHelper.getEssentialProducts(products[i])
                    val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                    productPriceLists.add(products[i].unitPrices!![0])
                    productUom.priceList = productPriceLists
                    productUom.isHasMultipleUom = true
                    (essentialProductList as ArrayList<ProductDetailBySupplier>).add(productUom)
                }
                for (i in (essentialProductList as ArrayList<ProductDetailBySupplier>).indices) {
                    val searchNewOrdersManagerOrder = SearchForNewOrderMgr()
                    searchNewOrdersManagerOrder.productDetailBySupplier =
                        (essentialProductList as ArrayList<ProductDetailBySupplier>).get(i)
                    lstSearchEssentialsAndDealsData!!.add(searchNewOrdersManagerOrder)
                }
            }
        }
        if (lstEssentials!!.adapter != null) {
            (lstEssentials!!.adapter as SelfOnBoardingSearchEssentialsAndDealsAdapter?)!!.notifyDataSetChanged()
        } else {
            lstEssentials!!.adapter = SelfOnBoardingSearchEssentialsAndDealsAdapter(
                this,
                lstSearchEssentialsAndDealsData!!,
                null,
                object : SelfOnBoardingSearchEssentialsAndDealsAdapter.OnItemClicked {
                    override fun onEssentialOrDealSupplierClicked(supplier: SearchedEssentialAndDealsSuppliers?) {
                        if (!StringHelper.isStringNullOrEmpty(supplier?.supplier?.supplierName)) {
                            setRecentSearchList(supplier?.supplier?.supplierName)
                        }
                        threeDotLoaderWhite!!.visibility = View.VISIBLE
                        if (!StringHelper.isStringNullOrEmpty(supplier?.essentialsId)) {
                            moveToEssentialProductScreen(supplier?.essentialsId!!, null)
                        } else if (!StringHelper.isStringNullOrEmpty(supplier?.dealNumber)) {
                            moveToDealsProductScreen(supplier?.dealNumber!!, null)
                        }
                    }

                    override fun onEssentialOrDealProductClicked(products: ProductDetailBySupplier?) {
                        if (!StringHelper.isStringNullOrEmpty(products?.productName)) {
                            setRecentSearchList(products?.productName)
                        }
                        threeDotLoaderWhite!!.visibility = View.VISIBLE
                        if (!StringHelper.isStringNullOrEmpty(products?.essentialsId)) {
                            moveToEssentialProductScreen(products?.essentialsId!!, products)
                        } else if (!StringHelper.isStringNullOrEmpty(products?.dealNumber)) {
                            moveToDealsProductScreen(products?.dealNumber!!, products)
                        }
                    }

                    override fun onCategoryClicked(category: String?) {
                        if (!StringHelper.isStringNullOrEmpty(category)) {
                            setRecentSearchList(category)
                        }
                    }
                },
                object : SelfOnBoardingSearchEssentialsAndDealsAdapter.onViewAllClickListener {
                    override fun onViewAllSupplierClicked() {
                        isSupplierViewAllSelected = true
                        lstSearchEssentialsAndDealsData!!.clear()
                        setData(searchEssentialAndDealsModel, essentialsProducts, false)
                    }

                    override fun onViewAllCategoryClicked() {
                        isCategoriesViewAllSelected = true
                        lstSearchEssentialsAndDealsData!!.clear()
                        setData(searchEssentialAndDealsModel, essentialsProducts, false)
                    }
                })
            (lstEssentials!!.adapter as SelfOnBoardingSearchEssentialsAndDealsAdapter?)!!.filter.filter(
                edtSearch!!.text.toString()
            )
        }
        setEssentialsTabActive()
        threeDotLoaderWhite!!.visibility = View.GONE
    }

    private fun moveToDealsProductScreen(
        DealNumber: String,
        productDetailBySupplier: ProductDetailBySupplier?,
    ) {
        OrderHelper.getActiveDealsForOnBoarding(this, object : DealsOnBoardingResponseListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsResponseForOnBoarding?) {
                if (dealsBaseResponse != null && dealsBaseResponse.data != null && dealsBaseResponse.data!!.deals != null && dealsBaseResponse.data!!.deals!!.size > 0) {
                    for (i in dealsBaseResponse.data!!.deals!!.indices) {
                        if (dealsBaseResponse.data!!.deals!![i].dealNumber == DealNumber) {
                            val intent = Intent(
                                this@SearchOnBoardingEssentialsDealsActivity,
                                DealsOnBoardingProductListActivity::class.java
                            )
                            intent.putExtra(
                                ZeemartAppConstants.DEALS_LIST,
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                    dealsBaseResponse.data!!.deals!![0]
                                )
                            )
                            if (productDetailBySupplier != null) intent.putExtra(
                                ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetailBySupplier)
                            )
                            startActivity(intent)
                        }
                    }
                }
                threeDotLoaderWhite!!.visibility = View.GONE
            }

            override fun onErrorResponse(error: VolleyError?) {
                threeDotLoaderWhite!!.visibility = View.GONE
            }
        })
    }

    private fun moveToEssentialProductScreen(
        EssentialId: String,
        productDetailBySupplier: ProductDetailBySupplier?,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setEssentialsId(EssentialId)
        EssentialsApi.getPaginatedEssentialsForOnBoarding(
            this,
            object : EssentialsResponseOnBoardingListener {
                override fun onSuccessResponse(essentialsList: EssentialsResponseForOnBoarding?) {
                    if (essentialsList != null && essentialsList.data?.essentials != null && essentialsList.data!!.essentials!!.size > 0) {
                        for (i in essentialsList.data!!.essentials!!.indices) {
                            if (essentialsList.data!!.essentials!![i].essentialsId == EssentialId) {
                                val intent = Intent(
                                    this@SearchOnBoardingEssentialsDealsActivity,
                                    EssentialsOnBoardingProductListActivity::class.java
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.ESSENTIALS_LIST,
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                        essentialsList.data!!.essentials!![i]
                                    )
                                )
                                if (productDetailBySupplier != null) intent.putExtra(
                                    ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                        productDetailBySupplier
                                    )
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM,
                                    ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST
                                )
                                startActivityForResult(intent, REQUEST_CODE_UPDATE_CART_COUNT)
                            }
                        }
                    }
                    threeDotLoaderWhite!!.visibility = View.GONE
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    private fun setRecentSearchList(name: String?) {
        if (!recentSearchList!!.contains(name)) {
            recentSearchList!!.add(TOP, name)
        }
        saveRecentSearchInSharedPref(recentSearchList)
        setRecentSearchListAdapter(recentSearchList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE_CART_COUNT && resultCode == RESULT_OK) {
            callSupplierList(false)
        }
        if (resultCode == OrderDetailsActivity.IS_STATUS_CHANGED) {
            callSupplierList(false)
        }
    }

    private fun setRecentSearchListAdapter(recentSearchLst: MutableList<String?>?) {
        if (recentSearchLst != null && recentSearchLst.size > 0) for (i in recentSearchLst.indices) {
            if (recentSearchLst.size > 10) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val adapter: ArrayAdapter<String?> = object : ArrayAdapter<String?>(
            applicationContext,
            android.R.layout.simple_list_item_1, recentSearchLst!!
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val typeface = Typeface.createFromAsset(
                    context.assets,
                    "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
                )
                val text = view.findViewById<View>(android.R.id.text1) as TextView
                text.setTypeface(typeface)
                return view
            }
        }
        lstRecentSearch!!.adapter = adapter
    }

    private fun saveRecentSearchInSharedPref(recentSearchLst: MutableList<String?>?) {
        for (i in recentSearchLst!!.indices) {
            if (recentSearchLst.size > 10) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val recentSearchJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(recentSearchList)
        SharedPref.write(SharedPref.ORDER_BY_PRODUCT_RECENT_SEARCH, recentSearchJson)
    }

    override fun onResume() {
        super.onResume()
        val bundle = intent.extras
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
        if (threeDotLoaderWhite!!.visibility == View.GONE) {
            finish()
        }
    }

    private fun setEssentialsTabActive() {
        setRecentSearchListVisibility()
        setEssentialListVisibility()
    }

    companion object {
        private const val TOP = 0
        private const val REQUEST_CODE_UPDATE_CART_COUNT = 1
        const val LIMIT_NUMBER_OF_RECORDS = 10
        private var totalProductsEssential = 0
        private var pageCountEssentialProducts = 0
    }
}