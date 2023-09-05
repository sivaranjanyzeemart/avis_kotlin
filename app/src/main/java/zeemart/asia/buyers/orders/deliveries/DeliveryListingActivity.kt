package zeemart.asia.buyers.orders.deliveries

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.DeliveryListingAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.goodsReceivedNote.GoodsReceivedNoteDashBoardActivity
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.pagination.DeliveryScrollHelper
import zeemart.asia.buyers.helper.pagination.PaginationListScrollHelper
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI
import zeemart.asia.buyers.models.orderimport.OrdersWithPagination
import zeemart.asia.buyers.models.orderimportimport.ListWithStickyHeaderUI
import zeemart.asia.buyers.network.MarketListApi
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.RetrieveOrders
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.filter.FilterOrdersActivity
import java.util.*

class DeliveryListingActivity() : AppCompatActivity(), View.OnClickListener {
    private var btnUpcomingDeliveries: Button? = null
    private var btnPastDeliveries: Button? = null
    private var txtUpcomingHighlighter: TextView? = null
    private var txtPastHighlighter: TextView? = null
    private var txtHeadingDeliveries: TextView? = null
    private lateinit var lstPastDeliveries: RecyclerView
    private lateinit var lstUpcomingDeliveries: RecyclerView
    private val upcomingOrderList: ArrayList<ListWithStickyHeaderUI?>? =
        ArrayList<ListWithStickyHeaderUI?>()
    private val pastOrderList: ArrayList<ListWithStickyHeaderUI?>? =
        ArrayList<ListWithStickyHeaderUI?>()
    private var mOrders: MutableList<Orders>? = null
    private lateinit var lytNoDeliveries: RelativeLayout
    private var txtNoDeliveriesText: TextView? = null
    private lateinit var btnFilter: ImageButton
    private var isPastOrderListSelected = false
    private lateinit var txtShowingFilteredResultText: TextView
    private var filterUntilDate: Long = 0
    private var filterFromDate: Long = 0
    private var threeDotLoaderBlue: CustomLoadingViewBlue? = null
    private var totalPageCountPast = 1
    private var totalPageCountUpcoming = 1
    private var isFilterApplied = false
    private var searchString: String? = null
    private lateinit var edtSearchOrders: EditText
    private var deliveryScrollHelperPast: DeliveryScrollHelper? = null
    private var deliveryScrollHelperUpcoming: DeliveryScrollHelper? = null
    private var lytPastDeliveriesLayoutManager: LinearLayoutManager? = null
    private var lytUpcomingDeliveriesLayoutManager: LinearLayoutManager? = null
    private var btnBack: ImageView? = null
    private var edtSearchClear: ImageView? = null
    private lateinit var retrieveOrderRequestParameters: ApiParamsHelper
    private lateinit var txtSelectedFilterCount: TextView
    private var isPastOrderlistLoaded = false
    private var isUpcomingOrderListLoaded = false
    private lateinit var swipeRefreshLayoutDeliveries: SwipeRefreshLayout
    private lateinit var lytNoFilterResults: RelativeLayout
    private var txtNoFilterResults: TextView? = null
    private var txtDeselectFilters: TextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_listing)
        pastPageCount = 0
        upcomingPageCount = 0
        txtSelectedFilterCount =
            findViewById<TextView>(R.id.txt_number_of_selected_deliveries_filters)
        txtSelectedFilterCount.setVisibility(View.GONE)
        threeDotLoaderBlue = findViewById<CustomLoadingViewBlue>(R.id.spin_kit_loader_delivery_blue)
        btnUpcomingDeliveries = findViewById<Button>(R.id.deliveries_btn_upcoming)
        btnUpcomingDeliveries!!.setOnClickListener(this)
        btnPastDeliveries = findViewById<Button>(R.id.deliveries_btn_past)
        btnPastDeliveries!!.setOnClickListener(this)
        txtUpcomingHighlighter =
            findViewById<TextView>(R.id.deliveries_txt_btn_upcoming_highlighter)
        txtPastHighlighter = findViewById<TextView>(R.id.deliveries_txt_btn_past_highlighter)
        txtHeadingDeliveries = findViewById<TextView>(R.id.deliveries_txt_heading)
        lytNoDeliveries = findViewById<RelativeLayout>(R.id.deliveries_lyt_no_deliveries)
        lytNoFilterResults = findViewById<RelativeLayout>(R.id.lyt_no_filter_results)
        txtDeselectFilters = findViewById<TextView>(R.id.txt_deselect_filters)
        txtNoFilterResults = findViewById<TextView>(R.id.txt_no_result)
        lytNoDeliveries.setVisibility(View.GONE)
        lytNoFilterResults.setVisibility(View.GONE)
        txtNoDeliveriesText = findViewById<TextView>(R.id.deliveries_no_deliveries_text)
        swipeRefreshLayoutDeliveries =
            findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_deliveries)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayoutDeliveries)
        swipeRefreshLayoutDeliveries.setOnRefreshListener(object :
            SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                setInitialParamsForGetDeliveries()
                swipeRefreshLayoutDeliveries.setRefreshing(false)
            }
        })
        btnBack = findViewById<ImageView>(R.id.deliveries_back_btn)
        btnBack!!.setOnClickListener(this)
        btnFilter = findViewById<ImageButton>(R.id.deliveries_btn_filter)
        btnFilter.setOnClickListener(this)
        btnFilter.setAlpha(.5f)
        btnFilter.setEnabled(false)
        edtSearchOrders = findViewById<EditText>(R.id.edit_search)
        edtSearchClear = findViewById<ImageView>(R.id.edit_search_clear)
        edtSearchOrders.setHint(getResources().getString(R.string.content_orders_edit_search_orders_hint))
        edtSearchOrders.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText: String = edtSearchOrders.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear!!.visibility = View.VISIBLE
                } else {
                    edtSearchClear!!.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        edtSearchOrders.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                AnalyticsHelper.logAction(
                    this@DeliveryListingActivity,
                    AnalyticsHelper.TAP_DELIVERIES_SEARCH_DELIVERY
                )
            }
        })
        edtSearchOrders.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setInitialParamsForGetDeliveries()
                    CommonMethods.hideKeyboard(this@DeliveryListingActivity)
                }
                return false
            }
        })
        edtSearchClear!!.setOnClickListener(View.OnClickListener {
            edtSearchOrders.setText("")
            setInitialParamsForGetDeliveries()
        })
        lstPastDeliveries = findViewById<RecyclerView>(R.id.lst_past_deliveries)
        lstUpcomingDeliveries = findViewById<RecyclerView>(R.id.lst_upcoming_deliveries)
        lytPastDeliveriesLayoutManager = LinearLayoutManager(this)
        lytUpcomingDeliveriesLayoutManager = LinearLayoutManager(this)
        lstUpcomingDeliveries.setLayoutManager(lytUpcomingDeliveriesLayoutManager)
        lstPastDeliveries.setLayoutManager(lytPastDeliveriesLayoutManager)
        setDeliveryScrollHelperPast()
        setDeliveryScrollHelperUpcoming()
        deliveryScrollHelperUpcoming?.setOnScrollListener()
        deliveryScrollHelperPast?.setOnScrollListener()
        txtShowingFilteredResultText = findViewById<TextView>(R.id.deliveries_txt_filtered_result)
        txtShowingFilteredResultText.setVisibility(View.GONE)
        //clear all the supplier outlet and order status filters from shared prefs
        DetailSupplierMgr.clearAllSupplierFilterData()
        OrderStatus.clearAllOrderStatusFilterData()
        OutletMgr.clearAllOutletFilterData()
        DeliveryStatus.clearAllDeliveryStatusFilterData()
        InvoiceStatusWithFilter.clearAllInvoiceStatusFilterData()
        DeliveryStatus.initializeDeliveryStatusFilters()
        callGetSuppliers()
        setInitialParamsForGetDeliveries()
        SharedPref.removeVal(SharedPref.FILTER_ORDER_UNTIL_DATE)
        SharedPref.removeVal(SharedPref.FILTER_ORDERS_FROM_DATE)
        setFont()
    }

    fun refreshData() {
        setInitialParamsForGetDeliveries()
        swipeRefreshLayoutDeliveries.setRefreshing(false)
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            btnPastDeliveries,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnUpcomingDeliveries,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtHeadingDeliveries,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoDeliveriesText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            edtSearchOrders,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectedFilterCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoFilterResults,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeselectFilters,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.deliveries_back_btn -> finish()
            R.id.deliveries_btn_upcoming -> {
                AnalyticsHelper.logAction(
                    this@DeliveryListingActivity,
                    AnalyticsHelper.TAP_DELIVERIES_UPCOMING_TAB
                )
                deliveryScrollHelperUpcoming?.setOnScrollListener()
                setUpcomingTabActive()
            }
            R.id.deliveries_btn_past -> {
                AnalyticsHelper.logAction(
                    this@DeliveryListingActivity,
                    AnalyticsHelper.TAP_DELIVERIES_PAST_TAB
                )
                deliveryScrollHelperPast?.setOnScrollListener()
                setPastTabActive()
            }
            R.id.deliveries_btn_filter -> {
                AnalyticsHelper.logAction(
                    this@DeliveryListingActivity,
                    AnalyticsHelper.TAP_DELIVERIES_FILTER_DELIVERY
                )
                val newIntent =
                    Intent(this@DeliveryListingActivity, FilterOrdersActivity::class.java)
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.FILTER_CALLED_FROM_DELIVERIES
                )
                startActivityForResult(
                    newIntent,
                    FilterOrdersActivity.REQUEST_CODE_SELECTED_FILTERS
                )
            }
            else -> {}
        }
    }

    private fun setInitialParamsForGetDeliveries() {
        lytNoDeliveries.setVisibility(View.GONE)
        lytNoFilterResults.setVisibility(View.GONE)
        threeDotLoaderBlue?.setVisibility(View.VISIBLE)
        upcomingPageCount = 0
        pastPageCount = 0
        clearData()
        retrieveOrderRequestParameters = ApiParamsHelper()
        //        retrieveOrderRequestParameters.setOrderStartDate(0);
        searchString = edtSearchOrders.getText().toString()
        if (!StringHelper.isStringNullOrEmpty(searchString) && searchString!!.trim { it <= ' ' }.length > 0) {
            retrieveOrderRequestParameters.setOrderIdText(searchString!!)
        }
        if (isFilterApplied) {
            setFiltersToAPI(retrieveOrderRequestParameters)
        }
        val calendar: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
        DateHelper.setCalendarStartOfDayTime(calendar)
        val todaysDate = calendar.timeInMillis / 1000
        calendar.add(Calendar.DATE, -1)
        DateHelper.setCalendarEndOfDayTime(calendar)
        val previousDate = calendar.timeInMillis / 1000
        if (filterUntilDate != 0L && filterFromDate != 0L) {
            if ((filterFromDate > previousDate) && (filterUntilDate > previousDate)) {
                callUpcomingDeliveries()
                return
            } else if ((filterFromDate < todaysDate) && (filterUntilDate < todaysDate)) {
                callPastDeliveries()
                return
            }
        }
        callGeneralDeliveries()
    }

    private fun callGeneralDeliveries() {
        isPastOrderlistLoaded = false
        isUpcomingOrderListLoaded = false
        callUpcomingDeliveries()
        callPastDeliveries()
    }

    private fun callPastDeliveries() {
        //call the retrieve order API
        if (pastPageCount < totalPageCountPast) {
            pastPageCount = pastPageCount + 1
            retrieveOrderRequestParameters.setPageSize(pageSize)
            retrieveOrderRequestParameters.setPageNumber(pastPageCount)
            retrieveOrderRequestParameters.setSortOrder(ApiParamsHelper.SortOrder.DESC_CAP)
            retrieveOrderRequestParameters.setSortBy(ApiParamsHelper.SortField.TIME_DELIVERED)
            val calendar: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            calendar.add(Calendar.DATE, -1)
            DateHelper.setCalendarEndOfDayTime(calendar)
            val previousDate = calendar.timeInMillis / 1000
            searchString = edtSearchOrders.getText().toString()
            if (!StringHelper.isStringNullOrEmpty(searchString) && searchString!!.trim { it <= ' ' }.length > 0) {
                retrieveOrderRequestParameters.setOrderIdText(searchString!!)
            }
            val outlets: List<Outlet>
            if (isFilterApplied) {
                setFiltersToAPI(retrieveOrderRequestParameters)
                val outletMgrList: List<OutletMgr>? = OutletMgr.selectedOutlet
                if (outletMgrList != null && outletMgrList.size > 0) {
                    outlets = ZeemartBuyerApp.fromJsonList<Outlet>(
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(outletMgrList),
                        Outlet::class.java,
                        object : TypeToken<List<Outlet?>?>() {}.type
                    )!!
                } else {
                    outlets = SharedPref.allOutlets!!
                }
                if (filterFromDate != 0L && filterUntilDate != 0L) {
                    if (filterUntilDate > previousDate) {
                        retrieveOrderRequestParameters.setDeliveryEndDate(previousDate)
                    }
                    if (filterFromDate > previousDate) {
                        retrieveOrderRequestParameters.setDeliveryStartDate(0)
                    }
                } else {
                    retrieveOrderRequestParameters.setDeliveryEndDate(previousDate)
                    retrieveOrderRequestParameters.setDeliveryStartDate(0)
                }
            } else {
                outlets = SharedPref.allOutlets!!
                retrieveOrderRequestParameters.setDeliveryEndDate(previousDate)
                retrieveOrderRequestParameters.setDeliveryStartDate(0)
            }
            RetrieveOrders(this, object : RetrieveOrders.GetOrderDataListener {
                override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                    threeDotLoaderBlue?.setVisibility(View.GONE)
                    isPastOrderlistLoaded = true
                    if ((ordersData != null) && (ordersData.data
                            ?.orders != null) && (ordersData.data!!.orders?.size!! > 0)
                    ) {
                        totalPageCountPast = ordersData.data!!.numberOfPages!!
                        val orders: List<Orders> = ordersData.data!!.orders!!
                        val fetchedOrders: List<Orders> = ArrayList<Orders>(orders)
                        if (mOrders != null && mOrders!!.size > 0) {
                            mOrders!!.addAll(fetchedOrders)
                        } else {
                            mOrders = ArrayList<Orders>(orders)
                            pastOrderList!!.clear()
                        }
                        if (pastOrderList != null && pastOrderList.size > 0) {
                            for (i in pastOrderList.indices) {
                                if (pastOrderList[i]?.isLoader == true) {
                                    pastOrderList.remove(pastOrderList[i])
                                }
                            }
                        }
                        setPastDeliveriesAdapter(fetchedOrders)
                    } else {
                        setUIForDeliveries()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderBlue?.setVisibility(View.GONE)
                    isPastOrderlistLoaded = true
                    if (mOrders != null && mOrders!!.size > 0) {
                        btnFilter.setEnabled(true)
                    }
                    ZeemartBuyerApp.getToastRed(error?.message)
                }
            }, outlets, retrieveOrderRequestParameters).orders
        }
    }

    private fun callUpcomingDeliveries() {
        if (upcomingPageCount < totalPageCountUpcoming) {
            upcomingPageCount = upcomingPageCount + 1
            retrieveOrderRequestParameters.setPageSize(pageSize)
            retrieveOrderRequestParameters.setPageNumber(upcomingPageCount)
            retrieveOrderRequestParameters.setSortOrder(ApiParamsHelper.SortOrder.ASC_CAP)
            retrieveOrderRequestParameters.setSortBy(ApiParamsHelper.SortField.TIME_DELIVERED)
            val calendar: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            DateHelper.setCalendarStartOfDayTime(calendar)
            val todayDate = calendar.timeInMillis / 1000
            searchString = edtSearchOrders.getText().toString()
            if (!StringHelper.isStringNullOrEmpty(searchString) && searchString!!.trim { it <= ' ' }.length > 0) {
                retrieveOrderRequestParameters.setOrderIdText(searchString!!)
            }
            val outlets: List<Outlet>
            if (isFilterApplied) {
                setFiltersToAPI(retrieveOrderRequestParameters)
                val outletMgrList: List<OutletMgr>? = OutletMgr.selectedOutlet
                if (outletMgrList != null && outletMgrList.size > 0) {
                    outlets = ZeemartBuyerApp.fromJsonList<Outlet>(
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(outletMgrList),
                        Outlet::class.java,
                        object : TypeToken<List<Outlet?>?>() {}.type
                    )!!
                } else {
                    outlets = SharedPref.allOutlets!!
                }
                if (filterFromDate != 0L && filterUntilDate != 0L) {
                    if (filterFromDate < todayDate) {
                        retrieveOrderRequestParameters.setDeliveryStartDate(todayDate)
                    }
                    if (filterUntilDate < todayDate) {
                        retrieveOrderRequestParameters.setDeliveryEndDate(0)
                    }
                } else {
                    retrieveOrderRequestParameters.setDeliveryStartDate(todayDate)
                }
            } else {
                outlets = SharedPref.allOutlets!!
                retrieveOrderRequestParameters.setDeliveryStartDate(todayDate)
            }
            RetrieveOrders(this, object : RetrieveOrders.GetOrderDataListener {
                override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                    threeDotLoaderBlue?.setVisibility(View.GONE)
                    isUpcomingOrderListLoaded = true
                    if ((ordersData != null) && (ordersData.data
                            ?.orders != null) && (ordersData.data!!.orders?.size!! > 0)
                    ) {
                        totalPageCountUpcoming = ordersData.data!!.numberOfPages!!
                        val orders: List<Orders> = ordersData.data!!.orders!!
                        val fetchedOrders: List<Orders> = ArrayList<Orders>(orders)
                        if (mOrders != null && mOrders!!.size > 0) {
                            mOrders!!.addAll(fetchedOrders)
                        } else {
                            mOrders = ArrayList<Orders>(orders)
                            upcomingOrderList!!.clear()
                        }
                        if (upcomingOrderList != null && upcomingOrderList.size > 0) {
                            for (i in upcomingOrderList.indices) {
                                if (upcomingOrderList[i]?.isLoader == true) {
                                    upcomingOrderList.remove(upcomingOrderList[i])
                                }
                            }
                        }
                        setUpcomingDeliveriesAdapters(fetchedOrders)
                    } else {
                        setUIForDeliveries()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderBlue?.setVisibility(View.GONE)
                    isUpcomingOrderListLoaded = true
                    ZeemartBuyerApp.getToastRed(error?.message)
                }
            }, outlets, retrieveOrderRequestParameters).orders
        }
    }

    private fun setUIForDeliveries() {
        if (isPastOrderListSelected) {
            if (isPastOrderlistLoaded) setPastTabActive()
        } else {
            if (isUpcomingOrderListLoaded) setUpcomingTabActive()
        }
    }

    private fun clearData() {
        lstPastDeliveries.setAdapter(null)
        lstUpcomingDeliveries.setAdapter(null)
        mOrders = null
        pastOrderList!!.clear()
        upcomingOrderList!!.clear()
        deliveryScrollHelperUpcoming?.updateScrollListener(
            lstUpcomingDeliveries,
            lytUpcomingDeliveriesLayoutManager
        )
        deliveryScrollHelperPast?.updateScrollListener(
            lstPastDeliveries,
            lytPastDeliveriesLayoutManager
        )
    }

    /**
     * suppliers are fetched for filters only on first load
     */
    private fun callGetSuppliers() {
        //initialize all the filters
        //add the order status filter list to shared prefs.
        OrderStatus.initializeOrderStatusFilters()
        DeliveryStatus.initializeDeliveryStatusFilters()
        InvoiceStatusWithFilter.initializeInvoiceStatusFilters()
        val buyerDetails: BuyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson<BuyerDetails>(
            SharedPref.read(
                SharedPref.BUYER_DETAIL,
                ""
            ), BuyerDetails::class.java
        )
        val outletMgrList: MutableList<OutletMgr> = ArrayList<OutletMgr>()
        for (j in buyerDetails.outlet?.indices!!) {
            val outletMgr = OutletMgr()
            outletMgr.outletId = buyerDetails.outlet?.get(j)?.outletId
            outletMgr.outletName = buyerDetails.outlet?.get(j)?.outletName
            outletMgrList.add(outletMgr)
        }
        OutletMgr.setOutletFilters(outletMgrList)
        val apiParamsHelper = ApiParamsHelper()
        MarketListApi.retrieveSupplierList(
            this,
            apiParamsHelper,
            SharedPref.allOutlets,
            object : SupplierListResponseListener {
                override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                    if (supplierList != null && supplierList.size > 0) {
                        val detailSupplierList: MutableList<DetailSupplierMgr>? =
                            ArrayList<DetailSupplierDataModel>(supplierList) as MutableList<DetailSupplierMgr>
                        DetailSupplierMgr.supplierFilters = detailSupplierList
                    }
                    btnFilter.setAlpha(1f)
                    btnFilter.setEnabled(true)
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    threeDotLoaderBlue?.setVisibility(View.GONE)
                }
            })
    }

    private fun setDeliveryScrollHelperPast() {
        deliveryScrollHelperPast = DeliveryScrollHelper(
            this@DeliveryListingActivity,
            lstPastDeliveries,
            lytPastDeliveriesLayoutManager!!,
            object : PaginationListScrollHelper.ScrollCallback {
                override fun loadMore() {
                    if (pastPageCount < totalPageCountPast) {
                        callPastDeliveries()
                    }
                }
            })
    }

    private fun setDeliveryScrollHelperUpcoming() {
        deliveryScrollHelperUpcoming = DeliveryScrollHelper(
            this@DeliveryListingActivity,
            lstUpcomingDeliveries,
            lytUpcomingDeliveriesLayoutManager!!,
            object : PaginationListScrollHelper.ScrollCallback {
                override fun loadMore() {
                    if (upcomingPageCount < totalPageCountUpcoming) {
                        callUpcomingDeliveries()
                    }
                }
            })
    }

    private fun setPastTabActive() {
        lstUpcomingDeliveries.setVisibility(View.GONE)
        isPastOrderListSelected = true
        btnUpcomingDeliveries?.setTextColor(getResources().getColor(R.color.grey_medium))
        txtUpcomingHighlighter?.setVisibility(View.GONE)
        btnPastDeliveries?.setTextColor(getResources().getColor(R.color.black))
        txtPastHighlighter?.setVisibility(View.VISIBLE)
        lstPastDeliveries.setVisibility(View.VISIBLE)
        lytNoDeliveries.setVisibility(View.GONE)
        lytNoFilterResults.setVisibility(View.GONE)
        if (isPastOrderlistLoaded) checkForNoDeliveries(pastOrderList, lstPastDeliveries, true)
    }

    private fun setUpcomingTabActive() {
        lstPastDeliveries.setVisibility(View.GONE)
        isPastOrderListSelected = false
        btnUpcomingDeliveries?.setTextColor(getResources().getColor(R.color.black))
        txtUpcomingHighlighter?.setVisibility(View.VISIBLE)
        btnPastDeliveries?.setTextColor(getResources().getColor(R.color.grey_medium))
        txtPastHighlighter?.setVisibility(View.GONE)
        lstUpcomingDeliveries.setVisibility(View.VISIBLE)
        lytNoDeliveries.setVisibility(View.GONE)
        lytNoFilterResults.setVisibility(View.GONE)
        if (isUpcomingOrderListLoaded) checkForNoDeliveries(
            upcomingOrderList,
            lstUpcomingDeliveries,
            false
        )
    }

    private fun checkForNoDeliveries(
        deliveriesList: ArrayList<ListWithStickyHeaderUI?>?,
        lstDeliveries: RecyclerView?,
        isForPastOrders: Boolean,
    ) {
        if (deliveriesList!!.size == 0) {
            lstDeliveries?.setVisibility(View.GONE)
            if (isFilterApplied || !StringHelper.isStringNullOrEmpty(
                    edtSearchOrders.getText().toString()
                )
            ) {
                lytNoFilterResults.setVisibility(View.VISIBLE)
                lytNoDeliveries.setVisibility(View.GONE)
            } else {
                lytNoFilterResults.setVisibility(View.GONE)
                lytNoDeliveries.setVisibility(View.VISIBLE)
                if (isForPastOrders) {
                    txtNoDeliveriesText?.setText(getString(R.string.txt_no_past_deliveries))
                } else {
                    txtNoDeliveriesText?.setText(getString(R.string.txt_no_upcoming_deliveries))
                }
            }
        }
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FilterOrdersActivity.REQUEST_CODE_SELECTED_GRN) {
            refreshData()
        } else {
            if (resultCode == Activity.RESULT_OK) {
                filterFromDate = SharedPref.readLong(SharedPref.FILTER_ORDERS_FROM_DATE, 0)!!
                filterUntilDate = SharedPref.readLong(SharedPref.FILTER_ORDER_UNTIL_DATE, 0)!!
                val selectedSupplierFilter: List<DetailSupplierMgr>? =
                    DetailSupplierMgr.selectedSupplierFilter
                val selectedOutletsFilter: List<OutletMgr>? = OutletMgr.selectedOutlet
                val deliveryStatusFilterList: Set<String> = DeliveryStatus.statusSelectedForFilters
                if (((selectedOutletsFilter != null && selectedOutletsFilter.size > 0) ||
                            (selectedSupplierFilter != null && selectedSupplierFilter.size > 0) ||
                            (filterUntilDate != 0L && filterFromDate != 0L) ||
                            (deliveryStatusFilterList != null && deliveryStatusFilterList.size > 0))
                ) {
                    txtShowingFilteredResultText.setVisibility(View.VISIBLE)
                    btnFilter.setImageResource(R.drawable.filteryellow)
                    isFilterApplied = true
                    setFilterSelectedCount()
                } else {
                    isFilterApplied = false
                    setFilterSelectedCount()
                    txtShowingFilteredResultText.setVisibility(View.GONE)
                    btnFilter.setImageResource(R.drawable.filtericon)
                    txtSelectedFilterCount.setVisibility(View.GONE)
                }
                if (isPastOrderListSelected) {
                    deliveryScrollHelperPast?.setOnScrollListener()
                } else {
                    deliveryScrollHelperUpcoming?.setOnScrollListener()
                }
                setInitialParamsForGetDeliveries()
            }
        }
    }

    fun setUpcomingDeliveriesAdapters(receivedOrders: List<Orders>?) {
        if (receivedOrders != null && receivedOrders.size > 0) {
            for (i in receivedOrders.indices) {
                if (receivedOrders[i].isPlacedInvoiced) {
                    val newListItem = ListWithStickyHeaderUI()
                    val headerViewOrders = HeaderViewOrderUI()
                    val date: String = DateHelper.getDateInDateMonthYearFormat(
                        receivedOrders[i].timeDelivered, receivedOrders[i].outlet?.timeZoneFromOutlet
                    )
                    val day: String = DateHelper.getDateIn3LetterDayFormat(
                        receivedOrders[i].timeDelivered,
                        receivedOrders[i].outlet?.timeZoneFromOutlet
                    )
                    headerViewOrders.date = date
                    headerViewOrders.day = day
                    createListWithHeadersData(
                        receivedOrders,
                        i,
                        newListItem,
                        headerViewOrders,
                        upcomingOrderList
                    )
                }
            }
        }
        if (upcomingOrderList != null) {
            val newListItemLoader = ListWithStickyHeaderUI()
            newListItemLoader.isLoader
            if (upcomingPageCount != totalPageCountUpcoming) upcomingOrderList.add(newListItemLoader)
        }
        if (lstUpcomingDeliveries.getAdapter() == null) {
            val mAdapter = DeliveryListingAdapter(this, upcomingOrderList as kotlin.collections.ArrayList<ListWithStickyHeaderUI>?, true)
            lstUpcomingDeliveries.setAdapter(mAdapter)
            lstUpcomingDeliveries.addItemDecoration(
                HeaderItemDecoration(
                    lstUpcomingDeliveries,
                    mAdapter
                )
            )
            initDeliveriesSwipe(lstUpcomingDeliveries)
        } else {
            (lstUpcomingDeliveries.getAdapter() as DeliveryListingAdapter).updateDataList(
                upcomingOrderList as kotlin.collections.ArrayList<ListWithStickyHeaderUI>?
            )
        }
        setUIForDeliveries()
    }

    fun setPastDeliveriesAdapter(receivedOrders: List<Orders>?) {
        if (receivedOrders != null && receivedOrders.size > 0) {
            for (i in receivedOrders.indices) {
                if (receivedOrders[i].isPlacedInvoiced) {
                    val newListItem = ListWithStickyHeaderUI()
                    val headerViewOrders = HeaderViewOrderUI()
                    val date: String = DateHelper.getDateInDateMonthYearFormat(
                        receivedOrders[i].timeDelivered, receivedOrders[i].outlet?.timeZoneFromOutlet
                    )
                    val day: String = DateHelper.getDateIn3LetterDayFormat(
                        receivedOrders[i].timeDelivered,
                        receivedOrders[i].outlet?.timeZoneFromOutlet
                    )
                    headerViewOrders.date = date
                    headerViewOrders.day = day
                    createListWithHeadersData(
                        receivedOrders,
                        i,
                        newListItem,
                        headerViewOrders,
                        pastOrderList
                    )
                }
            }
        }
        if (pastOrderList != null) {
            val newListItemLoader = ListWithStickyHeaderUI()
            newListItemLoader.isLoader
            if (pastPageCount != totalPageCountPast) pastOrderList.add(newListItemLoader)
        }
        if (lstPastDeliveries.getAdapter() == null) {
            val mAdapterPast = DeliveryListingAdapter(this, pastOrderList as kotlin.collections.ArrayList<ListWithStickyHeaderUI>?, false)
            lstPastDeliveries.setAdapter(mAdapterPast)
            lstPastDeliveries.addItemDecoration(
                HeaderItemDecoration(
                    lstPastDeliveries,
                    mAdapterPast
                )
            )
            initDeliveriesSwipe(lstPastDeliveries)
        } else {
            (lstPastDeliveries.getAdapter() as DeliveryListingAdapter).updateDataList(pastOrderList as kotlin.collections.ArrayList<ListWithStickyHeaderUI>? )
        }
        setUIForDeliveries()
    }

    private fun setFiltersToAPI(retrieveOrderRequestParameters: ApiParamsHelper?) {
        if (filterUntilDate != 0L) {
            retrieveOrderRequestParameters?.setDeliveryEndDate(filterUntilDate)
        }
        if (filterFromDate != 0L) {
            retrieveOrderRequestParameters?.setDeliveryStartDate(filterFromDate)
        }
        val supplierSelectedList: List<DetailSupplierMgr>? = DetailSupplierMgr.selectedSupplierFilter
        val outletSelectedList: List<OutletMgr>? = OutletMgr.selectedOutlet
        val selectedOrderStatus: Set<String> = OrderStatus.statusSelectedForFilters
        val selectedDeliveryStatusList: Set<String> = DeliveryStatus.statusSelectedForFilters
        if (supplierSelectedList != null && supplierSelectedList.size > 0) {
            val supplierIdArray = arrayOfNulls<String>(supplierSelectedList.size)
            for (i in supplierSelectedList.indices) {
                supplierIdArray[i] = supplierSelectedList[i].supplier.supplierId
            }
            retrieveOrderRequestParameters?.setSupplierId(
                ApiParamsHelper.Companion.arraysToString(
                    supplierIdArray as List<String>
                )
            )
        }
        if (outletSelectedList != null && outletSelectedList.size > 0) {
            val outletIdArray = arrayOfNulls<String>(outletSelectedList.size)
            for (i in outletSelectedList.indices) {
                outletIdArray[i] = outletSelectedList[i].outletId
            }
            retrieveOrderRequestParameters?.setOutletIds(outletIdArray as Array<String>)
        }
        if (selectedOrderStatus != null && selectedOrderStatus.size > 0) {
            val selectedOrderStatusArray = selectedOrderStatus.toTypedArray()
            retrieveOrderRequestParameters?.setOrderStatus(selectedOrderStatusArray)
        }
        if (selectedDeliveryStatusList != null && selectedDeliveryStatusList.size > 0) {
            /**
             * condition to check
             * if all the statuses are selected then we need not pass this filter as API by default
             * returns all the orders irrespective of delivery status.
             */
            if (selectedDeliveryStatusList.size != DeliveryStatus.filterListFromSharedPrefs.size) {
                if (selectedDeliveryStatusList.contains(DeliveryStatus.RECEIVED.statusName)) {
                    retrieveOrderRequestParameters?.setIsReceived(true)
                } else if (selectedDeliveryStatusList.contains(DeliveryStatus.NOT_RECEIVED.statusName)) {
                    retrieveOrderRequestParameters?.setIsReceived(false)
                }
            }
        }
    }

    private fun createListWithHeadersData(
        orderList: List<Orders>,
        i: Int,
        newListItem: ListWithStickyHeaderUI,
        headerViewOrders: HeaderViewOrderUI,
        referencedList: ArrayList<ListWithStickyHeaderUI?>?,
    ) {
        if (referencedList!!.size == 0) {
            saveHeaderAndOrder(orderList[i], newListItem, headerViewOrders, referencedList)
        } else {
            //if the last entry of the data list has same order date as that of the current order
            // then do not add a header element otherwise add a header element
            if (referencedList[referencedList.size - 1] != null) {
                if (referencedList[referencedList.size - 1]?.headerData?.date
                        .equals(headerViewOrders.date)
                ) {
                    newListItem.order = (orderList[i])
                    newListItem.headerData = (headerViewOrders)
                    newListItem.isHeader
                    referencedList.add(newListItem)
                } else {
                    saveHeaderAndOrder(orderList[i], newListItem, headerViewOrders, referencedList)
                }
            } else {
                if (referencedList[referencedList.size - 2]?.headerData?.date
                        .equals(headerViewOrders.date)
                ) {
                    newListItem.order = (orderList[i])
                    newListItem.headerData = (headerViewOrders)
                    newListItem.isHeader
                    referencedList.add(newListItem)
                } else {
                    saveHeaderAndOrder(orderList[i], newListItem, headerViewOrders, referencedList)
                }
            }
        }
    }

    private fun saveHeaderAndOrder(
        order: Orders,
        newListItem: ListWithStickyHeaderUI,
        headerViewOrders: HeaderViewOrderUI,
        referencedList: ArrayList<ListWithStickyHeaderUI?>?,
    ) {
        newListItem.headerData = (headerViewOrders)
        newListItem.isHeader
        referencedList!!.add(newListItem)
        val orderDataEntry = ListWithStickyHeaderUI()
        orderDataEntry.isHeader
        orderDataEntry.headerData = (headerViewOrders)
        orderDataEntry.order = (order)
        referencedList.add(orderDataEntry)
    }

    private fun initDeliveriesSwipe(lstDeliveries: RecyclerView?) {
        val deliveriesListingSwipeHelper: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                private val background: ColorDrawable = ColorDrawable()
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val mAdapter: DeliveryListingAdapter? =
                        lstDeliveries?.getAdapter() as DeliveryListingAdapter
                    if (mAdapter != null && mAdapter.viewOrderSectionOrRowData != null) {
                        //call the receive API and reset the adapter
                        val position: Int = viewHolder.getAdapterPosition()
                        val order: Orders =
                            mAdapter.viewOrderSectionOrRowData.get(position).order!!
                        order.outlet?.let {
                            OutletsApi.getSpecificOutlet(
                                this@DeliveryListingActivity,
                                it,
                                object : OutletsApi.GetSpecificOutletResponseListener {
                                    override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                                        if ((specificOutlet != null) && (specificOutlet.data != null)
                                            && (specificOutlet.data!!.settings != null)
                                            && specificOutlet.data!!.settings?.isEnableGRN == true
                                        ) {
                                            val intent = Intent(
                                                this@DeliveryListingActivity,
                                                GoodsReceivedNoteDashBoardActivity::class.java
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.ORDER_DETAILS_JSON,
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(order)
                                            )
                                            startActivityForResult(
                                                intent,
                                                FilterOrdersActivity.REQUEST_CODE_SELECTED_GRN
                                            )
                                            mAdapter.notifyItemChanged(position)
                                        } else {
                                            DialogHelper.receiveConfirmationDialog(
                                                this@DeliveryListingActivity,
                                                order,
                                                AnalyticsHelper.SLIDE_ITEM_DELIVERIES_RECEIVE,
                                                object : DialogHelper.ReceiveOrderListener {
                                                    override fun onReceiveSuccessful() {
                                                        order.isReceived = true
                                                        lstDeliveries.getAdapter()!!.notifyDataSetChanged()

                                                    }

                                                    override fun onReceiveError() {
                                                        mAdapter.notifyItemChanged(position)
                                                    }

                                                    override fun onDialogDismiss() {
                                                        mAdapter.notifyItemChanged(position)
                                                    }
                                                })
                                        }
                                    }

                                    override fun onError(error: VolleyErrorHelper?) {
                                        DialogHelper.receiveConfirmationDialog(
                                            this@DeliveryListingActivity,
                                            order,
                                            AnalyticsHelper.SLIDE_ITEM_DELIVERIES_RECEIVE,
                                            object : DialogHelper.ReceiveOrderListener {
                                                override fun onReceiveSuccessful() {
                                                    order.isReceived = true
                                                  lstDeliveries.getAdapter()!!.notifyDataSetChanged()
                                                }

                                                override fun onReceiveError() {
                                                    mAdapter.notifyItemChanged(position)
                                                }

                                                override fun onDialogDismiss() {
                                                    mAdapter.notifyItemChanged(position)
                                                }
                                            })
                                    }
                                })
                        }
                    }
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ): Int {
                    if (viewHolder is DeliveryListingAdapter.ViewHolderHeader) return 0 else if (viewHolder is DeliveryListingAdapter.ViewHolderItem) {
                        var order: Orders? = null
                        if (isPastOrderListSelected) {
                            order = pastOrderList!![viewHolder.getAdapterPosition()]?.order
                        } else {
                            order = upcomingOrderList!![viewHolder.getAdapterPosition()]?.order
                        }
                        return if (order?.isReceived == true) {
                            0
                        } else {
                            ItemTouchHelper.RIGHT
                        }
                    } else {
                        return super.getSwipeDirs(recyclerView, viewHolder)
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
                    if (viewHolder is DeliveryListingAdapter.ViewHolderItem) {
                        val mViewHolder = viewHolder as DeliveryListingAdapter.ViewHolderItem
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) //swiping Right
                        {
                            //show approve button
                            val bmp: Bitmap = BitmapFactory.decodeResource(
                                getResources(),
                                R.drawable.receive_icon
                            )
                            drawRightSwipeButton(
                                background,
                                c,
                                dX,
                                mViewHolder,
                                getString(R.string.txt_receive),
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
        val helperActionRequired = ItemTouchHelper(deliveriesListingSwipeHelper)
        helperActionRequired.attachToRecyclerView(lstDeliveries)
    }

    /**
     * draw a background when user swipes right on the action required row
     *
     * @param background
     * @param c
     * @param dX
     * @param mViewHolder
     * @param text
     * @param bmp
     */
    private fun drawRightSwipeButton(
        background: ColorDrawable,
        c: Canvas,
        dX: Float,
        mViewHolder: DeliveryListingAdapter.ViewHolderItem,
        text: String,
        bmp: Bitmap,
    ) {
        val color: Int = getResources().getColor(R.color.text_blue)
        background.setColor(color)
        background.setBounds(
            mViewHolder.itemView.left,
            mViewHolder.itemView.top,
            dX.toInt(),
            mViewHolder.itemView.bottom
        )
        background.draw(c)
        val p = Paint()
        val rightButton = RectF(
            mViewHolder.itemView.left.toFloat(),
            mViewHolder.itemView.top.toFloat(),
            DeliveryListingAdapter.ViewHolderItem.SWIPE_BUTTON_WIDTH.toFloat(),
            mViewHolder.itemView.bottom.toFloat()
        )
        p.color = color
        c.drawRect(rightButton, p)
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = DeliveryListingAdapter.ViewHolderItem.SWIPE_BUTTON_TEXT_SIZE.toFloat()
        val textWidth = p.measureText(text)
        val bounds = Rect()
        p.getTextBounds(text, 0, text.length, bounds)
        val combinedHeight: Float =
            (bmp.getHeight() + DeliveryListingAdapter.ViewHolderItem.SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
        c.drawBitmap(
            bmp,
            rightButton.centerX() - (bmp.getWidth() / 2),
            rightButton.centerY() - (combinedHeight / 2),
            null
        )
        c.drawText(
            text,
            rightButton.centerX() - (textWidth / 2),
            rightButton.centerY() + (combinedHeight / 2),
            p
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }

    private fun setFilterSelectedCount() {
        val count: Int = FilterOrdersActivity.selectedFiltersCount
        if (count != 0) {
            txtSelectedFilterCount.setVisibility(View.VISIBLE)
            txtSelectedFilterCount.setText(count.toString() + "")
        } else {
            txtSelectedFilterCount.setVisibility(View.GONE)
        }
    }

    companion object {
        private var pastPageCount = 0
        private var upcomingPageCount = 0
        private val pageSize = 50
    }
}