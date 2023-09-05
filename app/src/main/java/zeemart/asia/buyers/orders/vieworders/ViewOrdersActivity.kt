package zeemart.asia.buyers.orders.vieworders

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.android.volley.VolleyError
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJsonList
import zeemart.asia.buyers.adapter.OrderListingSpendingPagerAdapter
import zeemart.asia.buyers.adapter.ViewOrderListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DateHelper.getDateIn3LetterDayFormat
import zeemart.asia.buyers.helper.DateHelper.getDateInDateMonthYearFormat
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.pagination.ViewOrderScrollHelper
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.NotificationBroadCastReceiverHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.NotifyDataChangedListener
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.OutletMgr.Companion.selectedOutlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI
import zeemart.asia.buyers.models.orderimport.OrdersWithPagination
import zeemart.asia.buyers.models.orderimportimport.ListWithStickyHeaderUI
import zeemart.asia.buyers.models.orderimportimport.OrderSummary
import zeemart.asia.buyers.network.MarketListApi
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.RetrieveOrders
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.filter.FilterOrdersActivity
import java.util.*

class ViewOrdersActivity : AppCompatActivity() {
    private val lstViewOrderWithHeaders: ArrayList<ListWithStickyHeaderUI?> =
        ArrayList<ListWithStickyHeaderUI?>()
    private lateinit var lstOrders: RecyclerView
    private lateinit var btnFilter: ImageButton
    private var mOrders: ArrayList<Orders>? = null
    private var imgFilter: ImageButton? = null
    private lateinit var lytNoOrders: LinearLayout
    private val txtNoOrdersBefore60days: TextView? = null
    private lateinit var edtSearchOrders: EditText
    private lateinit var pager: ViewPager
    private val thisWeekSpending: PriceDetails = PriceDetails()
    private val lastWeekSpending: PriceDetails = PriceDetails()
    private val thisMonthSpending: PriceDetails = PriceDetails()
    private val lastMonthSpending: PriceDetails = PriceDetails()
    private var thisMonthOrder = 0
    private var lastMonthOrder = 0
    private var txtThisMonthTotal: TextView? = null
    private var txtLastMonthTotal: TextView? = null
    private var txtOrderThisMonth: TextView? = null
    private var txtOrderLastMonth: TextView? = null
    private var txtThisMonth: TextView? = null
    private var txtLastMonth: TextView? = null
    private var txtThisWeekTotal: TextView? = null
    private var lastWeekTotal: TextView? = null
    private var txtDateThisWeek: TextView? = null
    private var txtDateLastWeek: TextView? = null
    private lateinit var barChart: BarChart
    private var imgSwipeIndicator1: ImageView? = null
    private var imgSwipeIndicator2: ImageView? = null
    private var imgSwipeIndicator3: ImageView? = null
    private var txtThisWeek: TextView? = null
    private var txtLastWeek: TextView? = null
    private var txtPast14Days: TextView? = null
    private lateinit var layoutSwipeIndicators: LinearLayout
    private var txtOrdersHeading: TextView? = null
    private lateinit var swipeRefreshViewOrders: SwipeRefreshLayout
    private var filterFromDate: Long = 0
    private var filterUntilDate: Long = 0
    private var lstOrdersLayoutManager: LinearLayoutManager? = null
    private lateinit var lytWeeklySpending: LinearLayout
    private var constraintViewOrders: ConstraintLayout? = null
    private var isFilterApplied = false
    private var lytTotalOrdersFilterApplied: LinearLayout? = null
    private var lytConstraintSwipeRefresh: ConstraintLayout? = null
    private var txtTotalAmountFilterApplied: TextView? = null
    private var txtTotalNoOfOrdersFilterApplied: TextView? = null
    private lateinit var lytLoadMore: RelativeLayout
    private var searchString: String? = null
    private var totalPageCount = 1
    private lateinit var viewOrderScrollHelper: ViewOrderScrollHelper
    private var selectedOrderId: String? = null
    private var selectedOrderStatus: String? = null
    private var notificationBroadCastReceiverHelper: NotificationBroadCastReceiverHelper? = null
    private lateinit var txtSelectedFilterCount: TextView
    private lateinit var threeDotLoaderBlue: CustomLoadingViewBlue
    private var edtSearchClear: ImageView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageCount = 0
        setContentView(R.layout.activity_orders)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        lytWeeklySpending = findViewById<LinearLayout>(R.id.lyt_weekly_spend_data)
        txtSelectedFilterCount = findViewById<TextView>(R.id.txt_number_of_selected_order_filters)
        txtSelectedFilterCount.setVisibility(View.GONE)
        if (!UserPermission.HasViewPrice()) {
            lytWeeklySpending.setVisibility(View.GONE)
        } else {
            lytWeeklySpending.setVisibility(View.VISIBLE)
        }
        lytConstraintSwipeRefresh =
            findViewById<ConstraintLayout>(R.id.lyt_constraint_swipe_refresh)
        txtTotalAmountFilterApplied = findViewById<TextView>(R.id.txt_total_amount_filter_applied)
        txtTotalNoOfOrdersFilterApplied =
            findViewById<TextView>(R.id.txt_no_of_orders_filter_applied)
        threeDotLoaderBlue =
            findViewById<CustomLoadingViewBlue>(R.id.spin_kit_loader_view_orders_blue)
        constraintViewOrders = findViewById<ConstraintLayout>(R.id.constraint_view_orders)
        lytTotalOrdersFilterApplied =
            findViewById<LinearLayout>(R.id.lyt_filter_applied_order_total)
        lstOrders = findViewById<RecyclerView>(R.id.order_lst_orders)
        lstOrdersLayoutManager = object : LinearLayoutManager(this) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
                super.onLayoutChildren(recycler, state)
                //condition to check if the list is not scrollable
                if (totalPageCount == 1 && UserPermission.HasViewPrice() && lstOrdersLayoutManager?.findLastCompletelyVisibleItemPosition() == lstOrdersLayoutManager?.getItemCount()?.minus(1)) {
                    lytWeeklySpending.setVisibility(View.VISIBLE)
                }
            }
        }
        lstOrders.setLayoutManager(lstOrdersLayoutManager)
        txtOrdersHeading = findViewById<TextView>(R.id.orders_txt_heading)
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
        edtSearchOrders.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    AnalyticsHelper.logAction(
                        this@ViewOrdersActivity,
                        AnalyticsHelper.TAP_ORDERS_LIST_SEARCH_ORDER
                    )
                    graphAndOrdersData
                    CommonMethods.hideKeyboard(this@ViewOrdersActivity)
                    edtSearchOrders.clearFocus()
                }
                return false
            }
        })
        edtSearchClear!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                edtSearchOrders.setText("")
                graphAndOrdersData
            }
        })
        imgSwipeIndicator1 = findViewById<ImageView>(R.id.swipe_indicator_1)
        imgSwipeIndicator2 = findViewById<ImageView>(R.id.swipe_indicator_2)
        imgSwipeIndicator3 = findViewById<ImageView>(R.id.swipe_indicator_3)
        lytLoadMore = findViewById<RelativeLayout>(R.id.lyt_bottom_view_orders)
        lytLoadMore.setVisibility(View.GONE)
        barChart = findViewById<BarChart>(R.id.barChartOrderListing)
        layoutSwipeIndicators = findViewById<LinearLayout>(R.id.lyt_swipe_indicators)
        pager = findViewById<ViewPager>(R.id.pager_week_spending)
        imgFilter = findViewById<ImageButton>(R.id.orders_btn_filter)
        //remove all supplier outlet and order status filters from the Shared Prefs
        DetailSupplierMgr.clearAllSupplierFilterData()
        OutletMgr.clearAllOutletFilterData()
        OrderStatus.clearAllOrderStatusFilterData()
        DeliveryStatus.clearAllDeliveryStatusFilterData()
        InvoiceStatusWithFilter.clearAllInvoiceStatusFilterData()
        OrderTypeWithFilter.clearAllOrderTypeFilterData()
        SharedPref.removeVal(SharedPref.FILTER_ORDER_UNTIL_DATE)
        SharedPref.removeVal(SharedPref.FILTER_ORDERS_FROM_DATE)
        swipeRefreshViewOrders =
            findViewById<SwipeRefreshLayout>(R.id.lyt_swipe_refresh_view_orders)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshViewOrders)
        swipeRefreshViewOrders.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (threeDotLoaderBlue.getVisibility() == View.GONE) {
                    threeDotLoaderBlue.setVisibility(View.VISIBLE)
                    graphAndOrdersData
                }
                //to cancel visual indication of refresh
                swipeRefreshViewOrders.setRefreshing(false)
            }
        })
        lytNoOrders = findViewById<LinearLayout>(R.id.order_lyt_no_orders)

        //initially all the layout are invisible as the orders screen loads
        lytNoOrders.setVisibility(View.GONE)
        lstOrders.setVisibility(View.GONE)
        pager.setVisibility(View.GONE)
        layoutSwipeIndicators.setVisibility(View.GONE)
        threeDotLoaderBlue.setVisibility(View.VISIBLE)
        //call get All supliers for outlet
        callGetSuppliers()
        viewOrderScrollHelper = ViewOrderScrollHelper(
            this@ViewOrdersActivity,
            lstOrders,
            lstOrdersLayoutManager!!,
            object : ViewOrderScrollHelper.ViewOrderScrollCallback {
                override fun isHideSummaryGraph(isHide: Boolean) {
                    val animate =
                        TranslateAnimation(0f, 0f, -lytWeeklySpending.getHeight().toFloat(), 0f)
                    animate.setDuration(500)
                    animate.setFillAfter(true)
                    animate.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {
                            enableDisableSwipeRefresh(false)
                        }

                        override fun onAnimationEnd(animation: Animation) {
                            enableDisableSwipeRefresh(true)
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                    if (isHide) {
                        enableDisableSwipeRefresh(false)
                        lytWeeklySpending.setAnimation(null)
                        lytWeeklySpending.setVisibility(View.GONE)
                    } else {
                        if (!isFilterApplied) {
                            if (lytWeeklySpending.getVisibility() == View.GONE) {
                                lytWeeklySpending.startAnimation(animate)
                                if (UserPermission.HasViewPrice()) lytWeeklySpending.setVisibility(
                                    View.VISIBLE
                                )
                            }
                        } else {
                            enableDisableSwipeRefresh(true)
                            lytWeeklySpending.setAnimation(null)
                            lytWeeklySpending.setVisibility(View.GONE)
                        }
                    }
                }

                override fun loadMore() {
                    if (pageCount < totalPageCount) {
                        callRetrieveAllOrder(true)
                    }
                }
            })
        viewOrderScrollHelper.setOnScrollListener()
        btnFilter = findViewById<ImageButton>(R.id.orders_btn_filter)
        btnFilter.setAlpha(.5f)
        btnFilter.setEnabled(false)
        btnFilter.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(
                this@ViewOrdersActivity,
                AnalyticsHelper.TAP_ORDERS_LIST_FILTER_ORDER
            )
            val newIntent = Intent(this@ViewOrdersActivity, FilterOrdersActivity::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.FILTER_CALLED_FROM_ORDERS
            )
            startActivityForResult(newIntent, REQUEST_CODE_SELECTED_FILTERS)
        })
        pager.setAdapter(OrderListingSpendingPagerAdapter(this))
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                //removes the marker if the page is swiped and marker is displayed
                barChart.highlightValue(null)
                if (position == 0) {
                    imgSwipeIndicator1!!.setImageResource(R.drawable.swipe_indicator_blue)
                    imgSwipeIndicator2!!.setImageResource(R.drawable.swipe_indicator_grey)
                    imgSwipeIndicator3!!.setImageResource(R.drawable.swipe_indicator_grey)
                } else if (position == 1) {
                    imgSwipeIndicator1!!.setImageResource(R.drawable.swipe_indicator_grey)
                    imgSwipeIndicator2!!.setImageResource(R.drawable.swipe_indicator_blue)
                    imgSwipeIndicator3!!.setImageResource(R.drawable.swipe_indicator_grey)
                } else {
                    imgSwipeIndicator1!!.setImageResource(R.drawable.swipe_indicator_grey)
                    imgSwipeIndicator2!!.setImageResource(R.drawable.swipe_indicator_grey)
                    imgSwipeIndicator3!!.setImageResource(R.drawable.swipe_indicator_blue)
                }
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {
                enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE)
            }
        })
        txtThisWeekTotal = findViewById<TextView>(R.id.order_listing_this_week_spending)
        lastWeekTotal = findViewById<TextView>(R.id.order_listing_last_week_spending)
        txtDateThisWeek = findViewById<TextView>(R.id.txt_order_listing_date_this_week)
        txtDateLastWeek = findViewById<TextView>(R.id.txt_order_listing_date_last_week)
        txtThisWeek = findViewById<TextView>(R.id.txt_order_listing_heading_this_week)
        txtLastWeek = findViewById<TextView>(R.id.txt_order_listing_heading_last_week)
        txtThisMonthTotal = findViewById<TextView>(R.id.order_listing_this_month_spending)
        txtLastMonthTotal = findViewById<TextView>(R.id.order_listing_last_month_spending)
        txtOrderThisMonth = findViewById<TextView>(R.id.txt_order_listing_date_this_month)
        txtOrderLastMonth = findViewById<TextView>(R.id.txt_order_listing_date_last_month)
        txtThisMonth = findViewById<TextView>(R.id.txt_order_listing_heading_this_month)
        txtLastMonth = findViewById<TextView>(R.id.txt_order_listing_heading_last_month)
        txtPast14Days = findViewById<TextView>(R.id.txt_heading_past_14_days)
        setUIFont()
        graphAndOrdersData
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
        OrderTypeWithFilter.initializeOrderTypeFilters()
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
                    if (SharedPref.readBool(
                            SharedPref.DISPLAY_USE_FILTER_FOR_INVOICED_POPUP,
                            true
                        )!!
                    ) createUseFilterForInvoicedOrdersDialog()
                    btnFilter.setEnabled(true)
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    threeDotLoaderBlue.setVisibility(View.GONE)
                }
            })
    }//set the summary data//to get the outletHeaderMap//call the graph data API

    /**
     * get the graph and order data to refresh the entire view order activity
     */
    val graphAndOrdersData: Unit
        get() {

            //call the graph data API
            if (isFilterApplied) {
                lytWeeklySpending.setVisibility(View.GONE)
            } else {
                lytWeeklySpending.setVisibility(View.GONE)
                val apiParamsHelper = ApiParamsHelper()
                apiParamsHelper.setDefaultSixtyDaysPeriod(null)
                searchString = edtSearchOrders.getText().toString().trim { it <= ' ' }
                if (searchString != null && searchString!!.trim { it <= ' ' }.length > 0) {
                    apiParamsHelper.setOrderIdText(searchString!!)
                }
                if (isFilterApplied) {
                    setFiltersToAPI(apiParamsHelper)
                }
                //to get the outletHeaderMap
                val outlets: Any? = if (StringHelper.isStringNullOrEmpty(apiParamsHelper.outletIdList)) {
                    SharedPref.allOutlets
                } else ({
                    val outletMgrSelected: List<OutletMgr>? = OutletMgr.selectedOutlet
                    ZeemartBuyerApp.fromJsonList<Outlet>(
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            outletMgrSelected
                        ), Outlet::class.java, object : TypeToken<List<Outlet?>?>() {}.type
                    )
                })!!
                OrderHelper.getChartsDataViewOrder(
                    this@ViewOrdersActivity,
                    outlets as List<Outlet>?,
                    apiParamsHelper,
                    object : OrderHelper.ChartsDataListener {
                        override fun onSuccessResponse(chartsDataModel: ChartsDataModel?) {
                            if (chartsDataModel != null && chartsDataModel.data != null && chartsDataModel.data!!.size > 0) {
                                //set the summary data
                                if (UserPermission.HasViewPrice()) lytWeeklySpending.setVisibility(
                                    View.VISIBLE
                                )
                                setSummaryData(chartsDataModel)
                            } else {
                                lytWeeklySpending.setVisibility(View.GONE)
                            }
                        }

                        override fun onErrorResponse(error: VolleyError?) {}
                    })
                OrderHelper.getChartsDataViewOrderNew(this, object :
                    OrderHelper.OrderSummaryListener {
                    override fun onSuccessResponse(orderSummary: OrderSummary?) {
                        if (orderSummary != null) {
                            thisMonthOrder = orderSummary.currMonthOrderCount!!
                            lastMonthOrder = orderSummary.lastMonthOrderCount!!
                            thisMonthSpending.amount = orderSummary.totalSpendingCurrMonth!!
                            lastMonthSpending.amount = orderSummary.totalSpendingLastMonth!!
                            updateOrderSummary()
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {}
                })
            }
            setInitialParamsForGetOrders()
        }

    private fun setSummaryData(chartsDataModel: ChartsDataModel?) {
        //get the this week last week dates
        //set the date range for this week and last week
        val thisWeekStartEnd: DateHelper.StartDateEndDate =
            DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
        txtDateThisWeek?.setText(
            thisWeekStartEnd.startEndRangeDateMonthFormat.uppercase(Locale.getDefault())
        )

        //set the date range for this week and last week
        val lastWeekStartEnd: DateHelper.StartDateEndDate =
            DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
        txtDateLastWeek?.setText(
            lastWeekStartEnd.startEndRangeDateMonthFormat.uppercase(Locale.getDefault())
        )
        if (chartsDataModel != null && chartsDataModel.data != null && chartsDataModel.data!!.size > 0) {
            val chartsDataByDateCreated: List<ChartsDataModel.ChartsDataByDateCreated>? = chartsDataModel.data!! as List<ChartsDataModel.ChartsDataByDateCreated>
            val thisWeekStartDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            thisWeekStartDate.timeInMillis = thisWeekStartEnd.startDateMillis * 1000
            val thisWeekEndDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            thisWeekEndDate.timeInMillis = thisWeekStartEnd.endDateMillis * 1000
            val lastWeekStartDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            lastWeekStartDate.timeInMillis = lastWeekStartEnd.startDateMillis * 1000
            val lastWeekEndDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            lastWeekEndDate.timeInMillis = lastWeekStartEnd.endDateMillis * 1000
            for (i in chartsDataByDateCreated?.indices!!) {
                val orderTimeCreated: Calendar =
                    Calendar.getInstance(DateHelper.marketTimeZone)
                orderTimeCreated.timeInMillis = chartsDataByDateCreated!![i].dateCreated?.times(1000)!!
                DateHelper.setCalendarStartOfDayTime(orderTimeCreated)
                if (orderTimeCreated.after(thisWeekStartDate) && orderTimeCreated.before(
                        thisWeekEndDate
                    ) || orderTimeCreated == thisWeekStartDate || orderTimeCreated == thisWeekEndDate
                ) {
                    thisWeekSpending.addAmount(chartsDataByDateCreated[i].total?.amount!!)
                } else if (orderTimeCreated.after(lastWeekStartDate) && orderTimeCreated.before(
                        lastWeekEndDate
                    ) || orderTimeCreated == lastWeekStartDate || orderTimeCreated == lastWeekEndDate
                ) {
                    lastWeekSpending.addAmount(chartsDataByDateCreated[i].total?.amount!!)
                }
            }
        }
        txtThisWeekTotal?.setText(thisWeekSpending.displayValue)
        lastWeekTotal?.setText(lastWeekSpending.displayValue)
        setChart(chartsDataModel)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            filterFromDate = SharedPref.readLong(SharedPref.FILTER_ORDERS_FROM_DATE, 0)!!
            filterUntilDate = SharedPref.readLong(SharedPref.FILTER_ORDER_UNTIL_DATE, 0)!!
            val supplierSelectedList: List<DetailSupplierMgr>? =
                DetailSupplierMgr.selectedSupplierFilter
            val outletSelectedList: List<OutletMgr>? = OutletMgr.selectedOutlet
            val selectedOrderStatus: Set<String> = OrderStatus.statusSelectedForFilters
            val selectedDeliveryStatus: Set<String> = DeliveryStatus.statusSelectedForFilters
            val selectedInvoiceStatus: Set<String> =
                InvoiceStatusWithFilter.statusSelectedForFilters as Set<String>
            val selectedOrderType: Set<String> = OrderTypeWithFilter.statusSelectedForFilters as Set<String>
            if (outletSelectedList != null && outletSelectedList.size > 0 || supplierSelectedList != null
                && supplierSelectedList.size > 0 || filterUntilDate != 0L || filterFromDate != 0L || selectedOrderStatus != null
                && selectedOrderStatus.size > 0 || selectedDeliveryStatus != null
                && selectedDeliveryStatus.size > 0 || selectedInvoiceStatus != null
                && selectedInvoiceStatus.size > 0 || selectedOrderType != null && selectedOrderType.size > 0) {
                //txtOrderFilterSelected.setVisibility(View.VISIBLE);
                btnFilter.setImageResource(R.drawable.filteryellow)
                setFilterSelectedCount()
                isFilterApplied = true
                lytWeeklySpending.setVisibility(View.GONE)
                setInitialParamsForGetOrders()
            } else {
                //txtOrderFilterSelected.setVisibility(View.GONE);
                btnFilter.setImageResource(R.drawable.filtericon)
                txtSelectedFilterCount.setVisibility(View.GONE)
                if (UserPermission.HasViewPrice()) lytWeeklySpending.setVisibility(View.VISIBLE)
                isFilterApplied = false
                graphAndOrdersData
            }
        } else if (resultCode == OrderDetailsActivity.IS_STATUS_CHANGED) {
            val bundle: Bundle? = data?.getExtras()
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.ORDER_STATUS)) {
                    selectedOrderStatus =
                        data.getExtras()!!.getString(ZeemartAppConstants.ORDER_STATUS)
                }
                if (bundle.containsKey(ZeemartAppConstants.ORDER_ID)) {
                    selectedOrderId = data.getExtras()!!.getString(ZeemartAppConstants.ORDER_ID)
                }
            }
            if (selectedOrderStatus != null && selectedOrderId != null) refreshParticularOrder(
                selectedOrderId!!, selectedOrderStatus!!
            )
        }
    }

    private fun updateOrderSummary() {
        txtThisMonthTotal?.setText(thisMonthSpending.displayValue)
        txtLastMonthTotal?.setText(lastMonthSpending.displayValue)
        if (thisMonthOrder == 1) {
            txtOrderThisMonth?.setText(thisMonthOrder.toString() + " " + getResources().getString(R.string.txt_no_of_order))
        } else {
            txtOrderThisMonth?.setText(thisMonthOrder.toString() + " " + getResources().getString(R.string.txt_no_of_orders))
        }
        if (lastMonthOrder == 1) {
            txtOrderLastMonth?.setText(lastMonthOrder.toString() + " " + getResources().getString(R.string.txt_no_of_order))
        } else {
            txtOrderLastMonth?.setText(lastMonthOrder.toString() + " " + getResources().getString(R.string.txt_no_of_orders))
        }
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

    private fun setInitialParamsForGetOrders() {
        threeDotLoaderBlue.setVisibility(View.VISIBLE)
        pageCount = 0
        mOrders = null
        lstOrders.setAdapter(null)
        viewOrderScrollHelper.updateScrollListener(lstOrders, lstOrdersLayoutManager)
        //viewOrderScrollHelper.setOnScrollListener();
        searchString = edtSearchOrders.getText().toString().trim { it <= ' ' }
        callRetrieveAllOrder(true)
    }

    /**
     * sets the adapter for the view order screen
     *
     * @param receivedOrders
     */
    open fun setOrdersAdapter(receivedOrders: ArrayList<Orders>?) {
        if (receivedOrders != null && receivedOrders.size > 0) {
            thisWeekSpending.amount = 0.0
            lastWeekSpending.amount = 0.0
            noOrderLayoutVisibility(false, false)
            val orders = ArrayList(receivedOrders)
            if (orders != null && orders.size > 0) {
                Collections.sort(orders, object : Comparator<Orders?> {
                    override fun compare(o1: Orders?, o2: Orders?): Int {
                        return o2!!.timeCompare!!.compareTo(o1!!.timeCompare!!)
                    }
                })
                for (i in orders.indices) {
                    if (orders[i].isPlacedInvoiced || orders[i].isRejectedCancelled) {
                        val newListItem = ListWithStickyHeaderUI()
                        val headerViewOrders = HeaderViewOrderUI()
                        val date = getDateInDateMonthYearFormat(
                            orders[i].timeCompare,
                            orders[i].outlet!!.timeZoneFromOutlet
                        )
                        val day = getDateIn3LetterDayFormat(
                            orders[i].timeCompare,
                            orders[i].outlet!!.timeZoneFromOutlet
                        )
                        headerViewOrders.date = (date)
                        headerViewOrders.day = (day)
                        if (lstViewOrderWithHeaders!!.size == 0) {
                            saveHeaderAndOrder(orders[i], newListItem, headerViewOrders)
                        } else {
                            //if the last entry of the data list has same order date as that of the current order
                            // then do not add a header element otherwise add a header element
                            if (lstViewOrderWithHeaders!![lstViewOrderWithHeaders!!.size - 1] != null) {
                                if (lstViewOrderWithHeaders!![lstViewOrderWithHeaders!!.size - 1]?.headerData
                                        ?.date.equals(headerViewOrders.date)
                                ) {
                                    newListItem.order = (orders[i])
                                    newListItem.headerData = (headerViewOrders)
                                    newListItem.isHeader = (false)
                                    lstViewOrderWithHeaders!!.add(newListItem)
                                } else {
                                    saveHeaderAndOrder(orders[i], newListItem, headerViewOrders)
                                }
                            } else {
                                if (lstViewOrderWithHeaders!![lstViewOrderWithHeaders!!.size - 2]?.headerData
                                        ?.date.equals(headerViewOrders.date)
                                ) {
                                    newListItem.order = (orders[i])
                                    newListItem.headerData = (headerViewOrders)
                                    newListItem.isHeader = (false)
                                    lstViewOrderWithHeaders!!.add(newListItem)
                                } else {
                                    saveHeaderAndOrder(orders[i], newListItem, headerViewOrders)
                                }
                            }
                        }
                    }
                }
                if (lstViewOrderWithHeaders!!.size == 0) {
                    noOrderLayoutVisibility(true, false)
                } else {
                    noOrderLayoutVisibility(false, false)
                    if (lstOrders.adapter != null) {
                        lstOrders.adapter!!.notifyDataSetChanged()
                        (lstOrders.adapter as ViewOrderListAdapter?)!!.updateOrderListWithTotal()
                    } else {
                        val mAdapter = ViewOrderListAdapter(
                            this,
                            lstViewOrderWithHeaders as ArrayList<ListWithStickyHeaderUI> ,
                            object : NotifyDataChangedListener {
                                override fun notifyResetAdapter() {
                                    //callRetrieveAllOrder(false);
                                }
                            })
                        lstOrders.adapter = mAdapter
                        lstOrders.addItemDecoration(HeaderItemDecoration(lstOrders, mAdapter))
                        //new ItemTouchHelper(new ListItemSwipeHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, mAdapter)).attachToRecyclerView(lstOrders);
                        initOrderListingSwipe()
                    }
                    if (lstViewOrderWithHeaders != null) {
                        val newListItemLoader = ListWithStickyHeaderUI()
                        newListItemLoader.isLoader = (true)
                        if (pageCount != totalPageCount) lstViewOrderWithHeaders.add(
                            newListItemLoader
                        )
                        lstOrders.adapter!!.notifyItemInserted(lstViewOrderWithHeaders.size)
                    }
                }
            }
        } else {
            noOrderLayoutVisibility(true, false)
        }
    }

    private fun saveHeaderAndOrder(
        order: Orders,
        newListItem: ListWithStickyHeaderUI,
        headerViewOrders: HeaderViewOrderUI,
    ) {
        newListItem.headerData = (headerViewOrders)
        newListItem.isHeader
        lstViewOrderWithHeaders!!.add(newListItem)
        val orderDataEntry = ListWithStickyHeaderUI()
        orderDataEntry.isHeader
        orderDataEntry.headerData = (headerViewOrders)
        orderDataEntry.order = (order)
        lstViewOrderWithHeaders.add(orderDataEntry)
    }

    private fun initOrderListingSwipe() {
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
                val mAdapter: ViewOrderListAdapter = lstOrders.getAdapter() as ViewOrderListAdapter
                val orders: Orders? =
                    lstViewOrderWithHeaders!![viewHolder.getAdapterPosition()]?.order
                if (mAdapter != null) {
                    if (direction == ItemTouchHelper.LEFT) {
                        AnalyticsHelper.logAction(
                            this@ViewOrdersActivity,
                            AnalyticsHelper.SLIDE_ITEM_ORDERS_LIST_CANCEL,
                            orders!!
                        )
                        mAdapter.removeAt(viewHolder.getAdapterPosition(), false)
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        AnalyticsHelper.logAction(
                            this@ViewOrdersActivity,
                            AnalyticsHelper.SLIDE_ITEM_ORDERS_LIST_REPEAT,
                            orders!!
                        )
                        mAdapter.removeAt(viewHolder.getAdapterPosition(), true)
                    }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                return if (viewHolder is ViewOrderListAdapter.ViewHolderHeader) 0 else if (viewHolder is ViewOrderListAdapter.ViewHolderItem) {
                    try {
                        val order: Orders? =
                            lstViewOrderWithHeaders!![viewHolder.getAdapterPosition()]?.order
                        //                        if(order.getOrderType().equals(Orders.Type.DEAL.getName()) ||order.getOrderType().equals(Orders.Type.ESSENTIALS.getName())){
//                            return 0;
//                        }
//                        else {
                        return if (order?.isRejectedCancelled == true) {
                            ItemTouchHelper.RIGHT
                        } else {
                            if (order?.orderStatus == OrderStatus.PLACED.statusName && (order.isApprovedByUser || order.isOrderByUser)) {
                                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                            } else {
                                ItemTouchHelper.RIGHT
                            }
                            //                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    0
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
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
                if (viewHolder is ViewOrderListAdapter.ViewHolderItem) {
                    val mViewHolder = viewHolder as ViewOrderListAdapter.ViewHolderItem
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX <= 0) // Swiping Left
                    {
                        if (viewHolder is ViewOrderListAdapter.ViewHolderItem) {
                            if (lstViewOrderWithHeaders!![viewHolder.getAdapterPosition()]?.order != null) {
                                val order: Orders? =
                                    lstViewOrderWithHeaders[viewHolder.getAdapterPosition()]?.order
                                if (order?.orderStatus == OrderStatus.PLACED.statusName && (order.isApprovedByUser || order.isOrderByUser)) {
                                    val bmp: Bitmap = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.white_cross
                                    )
                                    drawLeftSwipeButton(
                                        background,
                                        c,
                                        dX,
                                        mViewHolder,
                                        getString(R.string.txt_cancel),
                                        bmp
                                    )
                                }
                            }
                        }
                    } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) //swiping Right
                    {
                        //show approve button
                        val bmp: Bitmap = BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.repeatordericon
                        )
                        drawRightSwipeButton(
                            background,
                            c,
                            dX,
                            mViewHolder,
                            getString(R.string.txt_repeat),
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
        helperActionRequired.attachToRecyclerView(lstOrders)
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
        mViewHolder: ViewOrderListAdapter.ViewHolderItem,
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
            ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_WIDTH.toFloat(),
            mViewHolder.itemView.bottom.toFloat()
        )
        p.color = color
        c.drawRect(rightButton, p)
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_TEXT_SIZE.toFloat()
        val textWidth = p.measureText(text)
        val bounds = Rect()
        p.getTextBounds(text, 0, text.length, bounds)
        val combinedHeight: Float =
            (bmp.getHeight() + ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
        c.drawBitmap(
            bmp,
            rightButton.centerX() - bmp.getWidth() / 2,
            rightButton.centerY() - combinedHeight / 2,
            null
        )
        c.drawText(
            text,
            rightButton.centerX() - textWidth / 2,
            rightButton.centerY() + combinedHeight / 2,
            p
        )
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
        mViewHolder: ViewOrderListAdapter.ViewHolderItem,
        text: String,
        bmp: Bitmap,
    ) {
        //Show reject button
        val color: Int = getResources().getColor(R.color.pinky_red)
        background.setColor(color)
        background.setBounds(
            mViewHolder.itemView.right + dX.toInt(),
            mViewHolder.itemView.top,
            mViewHolder.itemView.right,
            mViewHolder.itemView.bottom
        )
        background.draw(c)
        val p = Paint()
        val buttonWidth: Int =
            mViewHolder.itemView.right - ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_WIDTH
        val rightButton = RectF(
            buttonWidth.toFloat(),
            mViewHolder.itemView.top.toFloat(),
            mViewHolder.itemView.right.toFloat(),
            mViewHolder.itemView.bottom.toFloat()
        )
        p.color = color
        c.drawRect(rightButton, p)
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_TEXT_SIZE.toFloat()
        val textWidth = p.measureText(text)
        val bounds = Rect()
        p.getTextBounds(text, 0, text.length, bounds)
        val combinedHeight: Float =
            (bmp.getHeight() + ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
        c.drawBitmap(
            bmp,
            rightButton.centerX() - bmp.getWidth() / 2,
            rightButton.centerY() - combinedHeight / 2,
            null
        )
        c.drawText(
            text,
            rightButton.centerX() - textWidth / 2,
            rightButton.centerY() + combinedHeight / 2,
            p
        )
    }

    private fun callRetrieveAllOrder(isLoadMore: Boolean) {
        //call the retrieve order API
        if (isLoadMore && pageCount < totalPageCount) {
            pageCount = pageCount + 1
        } else {
            threeDotLoaderBlue.setVisibility(View.VISIBLE)
            pageCount = 1
            pageSize = 0
        }
        val retrieveOrderRequestParameters = ApiParamsHelper()
        retrieveOrderRequestParameters.setPageSize(pageSize)
        retrieveOrderRequestParameters.setPageNumber(pageCount)
        retrieveOrderRequestParameters.setSortOrder(ApiParamsHelper.SortOrder.DESC_CAP)
        retrieveOrderRequestParameters.setSortBy(ApiParamsHelper.SortField.TIME_UPDATED)
        if (searchString != null && searchString!!.trim { it <= ' ' }.length > 0) {
            retrieveOrderRequestParameters.setOrderIdText(searchString!!)
        }
        if (isFilterApplied) {
            setFiltersToAPI(retrieveOrderRequestParameters)
        } else {
            val date = Date()
            val currentDate = date.time
            retrieveOrderRequestParameters.setOrderStartDate(0)
            retrieveOrderRequestParameters.setOrderEndDate(currentDate)
        }
        val outlets: List<Outlet>?
        if (!StringHelper.isStringNullOrEmpty(retrieveOrderRequestParameters.outletIdList)) {
            val outleMgrList = selectedOutlet
            outlets = fromJsonList(
                ZeemartBuyerApp.gsonExposeExclusive.toJson(outleMgrList),
                Outlet::class.java, object : TypeToken<List<Outlet?>?>() {}.type
            )
        }else {
                outlets = SharedPref.allOutlets
            }
        RetrieveOrders(this, object : RetrieveOrders.GetOrderDataListener {
            override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                threeDotLoaderBlue.setVisibility(View.GONE)
                lstOrders.setVisibility(View.VISIBLE)
                if (ordersData != null && ordersData.data
                        ?.orders != null && ordersData.data!!.orders?.size!! > 0
                ) {
                    totalPageCount = ordersData.data!!.numberOfPages!!
                    viewOrderScrollHelper.updateTotalItemCount(pageCount, totalPageCount)
                    noOrderLayoutVisibility(false, false)
                    val orders: List<Orders>? = ordersData.data!!.orders
                    val fetchedOrders: ArrayList<Orders> = ArrayList<Orders>(orders)
                    if (isLoadMore && mOrders != null && mOrders!!.size > 0) {
                        mOrders!!.addAll(fetchedOrders)
                    } else {
                        mOrders = ArrayList<Orders>(orders)
                        lstViewOrderWithHeaders!!.clear()
                    }
                    if (lstViewOrderWithHeaders != null && lstViewOrderWithHeaders.size > 0) {
                        for (i in lstViewOrderWithHeaders.indices) {
                            if (lstViewOrderWithHeaders[i]?.isLoader == true) {
                                lstViewOrderWithHeaders.remove(lstViewOrderWithHeaders[i])
                            }
                        }
                    }
                    setOrdersAdapter(fetchedOrders)
                    val lastDate: Long? = fetchedOrders[fetchedOrders.size - 1].timeUpdated
                    var apiParamsHelper = ApiParamsHelper()
                    val dateStr: String = DateHelper.getDateInDateMonthYearFormat(lastDate)
                    apiParamsHelper =
                        apiParamsHelper.setOrderStartDate(DateHelper.returnEpochTimeSOD(dateStr))
                    apiParamsHelper =
                        apiParamsHelper.setOrderEndDate(DateHelper.returnEpochTimeEOD(dateStr))
                    OrderHelper.getChartsDataViewOrder(
                        this@ViewOrdersActivity,
                        SharedPref.allOutlets,
                        apiParamsHelper,
                        object : OrderHelper.ChartsDataListener {
                            override fun onSuccessResponse(chartsDataModel: ChartsDataModel?) {
                                if (lstOrders.getAdapter() != null && chartsDataModel != null && chartsDataModel.data != null && chartsDataModel.data!!.size > 0) {
                                    (lstOrders.getAdapter() as ViewOrderListAdapter).setLastDateTotal(
                                        chartsDataModel.data!!.get(0)
                                    )
                                    (lstOrders.getAdapter() as ViewOrderListAdapter).notifyDataSetChanged()
                                }
                            }

                            override fun onErrorResponse(error: VolleyError?) {}
                        })
                } else {
                    noOrderLayoutVisibility(true, false)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                lytLoadMore.setVisibility(View.GONE)
                threeDotLoaderBlue.setVisibility(View.GONE)
            }
        }, outlets!!, retrieveOrderRequestParameters).orders
    }

    private fun setFiltersToAPI(retrieveOrderRequestParameters: ApiParamsHelper) {
        if (filterUntilDate != 0L) {
            retrieveOrderRequestParameters.setOrderEndDate(filterUntilDate)
        }
        if (filterFromDate != 0L) {
            retrieveOrderRequestParameters.setOrderStartDate(filterFromDate)
        }
        val supplierSelectedList: List<DetailSupplierMgr>? = DetailSupplierMgr.selectedSupplierFilter
        val outletSelectedList: List<OutletMgr>? = OutletMgr.selectedOutlet
        val selectedOrderStatus: Set<String> = OrderStatus.statusSelectedForFilters
        val selectedDeliveryStatusList: Set<String> = DeliveryStatus.statusSelectedForFilters
        val selectedInvoiceStatusList: Set<String> =
            InvoiceStatusWithFilter.statusSelectedForFilters as Set<String>
        val selectedOrderTypeList: Set<String> = OrderTypeWithFilter.statusSelectedForFilters as Set<String>
        if (supplierSelectedList != null && supplierSelectedList.size > 0) {
            val supplierIdArray = arrayOfNulls<String>(supplierSelectedList.size)
            for (i in supplierSelectedList.indices) {
                supplierIdArray[i] = supplierSelectedList[i].supplier.supplierId
            }
            retrieveOrderRequestParameters.setSupplierId(ApiParamsHelper.arraystoString(supplierIdArray as Array<String>))

        }
        if (outletSelectedList != null && outletSelectedList.size > 0) {
            val outletIdArray = arrayOfNulls<String>(outletSelectedList.size)
            for (i in outletSelectedList.indices) {
                outletIdArray[i] = outletSelectedList[i].outletId
            }
            retrieveOrderRequestParameters.setOutletIds(outletIdArray as Array<String>)
        }
        if (selectedOrderStatus != null && selectedOrderStatus.size > 0) {
            val selectedOrderStatusArray = selectedOrderStatus.toTypedArray()
            retrieveOrderRequestParameters.setOrderStatus(selectedOrderStatusArray)
        }
        if (selectedOrderTypeList != null && selectedOrderTypeList.size > 0) {
            val selectedOrderTypeArray = selectedOrderTypeList.toTypedArray()
            retrieveOrderRequestParameters.setOrdertype(selectedOrderTypeArray)
        }
        if (selectedDeliveryStatusList != null && selectedDeliveryStatusList.size > 0) {
            /**
             * condition to check
             * if all the statuses are selected then we need not pass this filter as API by default
             * returns all the orders irrespective of delivery status.
             */
            if (selectedDeliveryStatusList.size != DeliveryStatus.filterListFromSharedPrefs.size) {
                if (selectedDeliveryStatusList.contains(DeliveryStatus.RECEIVED.statusName)) {
                    retrieveOrderRequestParameters.setIsReceived(true)
                } else if (selectedDeliveryStatusList.contains(DeliveryStatus.NOT_RECEIVED.statusName)) {
                    retrieveOrderRequestParameters.setIsReceived(false)
                }
            }
        }
        if (selectedInvoiceStatusList != null && selectedInvoiceStatusList.size > 0) {
            if (selectedInvoiceStatusList.size != InvoiceStatusWithFilter.filterListFromSharedPrefs.size) {
                if (selectedInvoiceStatusList.contains(Invoice.InvoiceStatus.INVOICED.statusName)) {
                    retrieveOrderRequestParameters.setIsInvoiced(true)
                } else if (selectedInvoiceStatusList.contains(Invoice.InvoiceStatus.NO_LINKED_INVOICE.statusName)) {
                    retrieveOrderRequestParameters.setIsInvoiced(false)
                }
            }
        }
    }

    private fun setChart(chartsDataModel: ChartsDataModel?) {
        val entries: MutableList<BarEntry> = ArrayList<BarEntry>()
        val past14Days = datesForLast14days
        val xAxisDays = arrayOfNulls<String>(14)
        val itr: Iterator<*> = past14Days.iterator()
        var i = 0
        while (itr.hasNext() && i < 14) {
            val dateTimeData = itr.next() as String
            xAxisDays[i] = dateTimeData.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
            i = i + 1
        }
        val totalAmountMap = LinkedHashMap<String, Double>()
        val chartsDataByDateCreateds: List<ChartsDataModel.ChartsDataByDateCreated>? = chartsDataModel?.data
        for (j in chartsDataByDateCreateds?.indices!!) {
            val dateCreated: String = DateHelper.getDateInDateMonthYearFormat(
                chartsDataByDateCreateds[j].dateCreated, null
            )
            if (past14Days.contains(dateCreated)) {
                totalAmountMap[dateCreated] = chartsDataByDateCreateds[j].total?.amount!!
            }
        }
        //Add the 0 amount value for dates within past 14 days range which do no t have entires in the AMp
        val iterator: Iterator<String> = past14Days.iterator()
        while (iterator.hasNext()) {
            val date = iterator.next()
            if (!totalAmountMap.containsKey(date)) {
                totalAmountMap[date] = 0.0
            }
        }
        val totalAmountTempList: List<Map.Entry<String, Double>> =
            ArrayList<Map.Entry<String, Double>>(totalAmountMap.entries)
        Collections.sort<Map.Entry<String, Double>>(totalAmountTempList) { (key), (key1) ->
            val date1: Date? = DateHelper.toDate(key)
            val date2: Date? = DateHelper.toDate(key1)
            date1!!.compareTo(date2)
        }
        for (j in totalAmountTempList.indices) {
            entries.add(BarEntry(j.toFloat(), totalAmountTempList[j].value.toFloat()))
        }
        val dataSet = BarDataSet(entries, "") // add entries to dataset
        dataSet.setDrawValues(false)
        dataSet.setColor(getResources().getColor(R.color.chart_light_yellow))
        dataSet.setHighLightColor(getResources().getColor(R.color.chart_orange))
        val data = BarData(dataSet)
        barChart.setData(data)
        val d = Description()
        d.text = ""
        barChart.setDescription(d)
        barChart.setScaleEnabled(false)
        barChart.getLegend().setEnabled(false)
        val mv = CustomMarkerViewCharts(this, R.layout.chart_marker_layout, true)
        barChart.setMarker(mv)
        val xAxis: XAxis = barChart.getXAxis()
        xAxis.setValueFormatter(ChartsHelper.ChartXAxisValueFormatter(xAxisDays as Array<String>))
        barChart.setDrawGridBackground(false)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setDrawGridLines(false)
        xAxis.setLabelCount(14)
        val yAxisRight: YAxis = barChart.getAxisRight()
        yAxisRight.setDrawAxisLine(false)
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawGridLines(false)
        val yAxisLeft: YAxis = barChart.getAxisLeft()
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setLabelCount(3, true)
        yAxisLeft.setAxisMinimum(0f)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.setDrawLabels(false)
        barChart.invalidate() // refresh
        barChart.animateXY(1000, 1000, Easing.EasingOption.Linear, Easing.EasingOption.Linear)

        //fonts for the chart text
        val xLabels: XAxis = barChart.getXAxis()
        xLabels.setTextColor(getResources().getColor(R.color.grey_medium))
    }

    private val datesForLast14days: LinkedHashSet<String>
        private get() {
            val past14DateArray = LinkedHashSet<String>()
            val startDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            startDate.add(Calendar.DAY_OF_MONTH, -13)
            DateHelper.setCalendarStartOfDayTime(startDate)
            val currentDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            DateHelper.setCalendarStartOfDayTime(currentDate)
            while (!startDate.after(currentDate)) {
                val formattedDateString: String =
                    DateHelper.getDateInDateMonthYearFormat(startDate.timeInMillis / 1000)
                past14DateArray.add(formattedDateString)
                startDate.add(Calendar.DAY_OF_MONTH, 1)
            }
            return past14DateArray
        }

    private fun setUIFont() {
        //set the UI font for OrderListing
        ZeemartBuyerApp.setTypefaceView(
            txtThisWeekTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDateThisWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDateLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            lastWeekTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtThisWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtThisMonthTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderThisMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLastMonthTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtThisMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPast14Days,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            edtSearchOrders,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrdersHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalAmountFilterApplied,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalNoOfOrdersFilterApplied,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoOrdersBefore60days,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectedFilterCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    fun noOrderLayoutVisibility(isVisible: Boolean, isCalledFromSearchFilter: Boolean) {
        if (isVisible) {
            val txtNoSearchResult1: TextView = findViewById<TextView>(R.id.txt_no_search_1)
            val txtNoSearchResult2: TextView = findViewById<TextView>(R.id.txt_no_search_2)
            val imgNoSearchResult: ImageView = findViewById<ImageView>(R.id.img_no_search_result)
            val btnNoOrders: Button = findViewById<Button>(R.id.btn_no_orders_view_orders)
            lytNoOrders.setVisibility(View.VISIBLE)
            lstOrders.setVisibility(View.GONE)
            pager.setVisibility(View.GONE)
            layoutSwipeIndicators.setVisibility(View.GONE)
            //txtNoOrdersBefore60days.setVisibility(View.GONE);
            lytLoadMore.setVisibility(View.GONE)
            lytTotalOrdersFilterApplied?.setVisibility(View.INVISIBLE)

            //set the font style for the text view
            ZeemartBuyerApp.setTypefaceView(
                btnNoOrders,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtNoSearchResult1,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtNoSearchResult2,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            if (isFilterApplied || !StringHelper.isStringNullOrEmpty(
                    edtSearchOrders.getText().toString()
                )
            ) {
                txtNoSearchResult1.setVisibility(View.VISIBLE)
                txtNoSearchResult2.setVisibility(View.VISIBLE)
                imgNoSearchResult.visibility = View.VISIBLE
                btnNoOrders.visibility = View.GONE
            } else {
                txtNoSearchResult1.setVisibility(View.GONE)
                txtNoSearchResult2.setVisibility(View.GONE)
                imgNoSearchResult.visibility = View.GONE
                btnNoOrders.visibility = View.VISIBLE
            }
        } else {
            lytNoOrders.setVisibility(View.INVISIBLE)
            lstOrders.setVisibility(View.VISIBLE)
            pager.setVisibility(View.VISIBLE)
            layoutSwipeIndicators.setVisibility(View.VISIBLE)
        }
    }

    //enable the refresh action only when the view pager is idle
    private fun enableDisableSwipeRefresh(enable: Boolean) {
        if (swipeRefreshViewOrders != null) {
            swipeRefreshViewOrders.setEnabled(enable)
        }
    }

    /**
     * set the top layout for total orders, total order placed/invoiced and Total
     * amount when any filter is applied or search is done
     */
    fun setTotalLayout() {
        if (isFilterApplied || !StringHelper.isStringNullOrEmpty(
                edtSearchOrders.getText().toString()
            )
        ) {
            //set the visibility of order total layout and align the list below it
            lytWeeklySpending.setVisibility(View.INVISIBLE)
            lytTotalOrdersFilterApplied?.setVisibility(View.VISIBLE)
            val constraintSet = ConstraintSet()
            constraintSet.clone(lytConstraintSwipeRefresh)
            lytTotalOrdersFilterApplied?.getId()?.let {
                constraintSet.connect(
                    lstOrders.getId(),
                    ConstraintSet.TOP,
                    it,
                    ConstraintSet.BOTTOM
                )
            }
            constraintSet.applyTo(lytConstraintSwipeRefresh)

            //logic getting the data
            val adapterList: ArrayList<ListWithStickyHeaderUI> =
                (lstOrders.getAdapter() as ViewOrderListAdapter).adapterDataList
            var totalOrders = 0
            var totalPlacedOrInvoicedOrders = 0
            var totalAmount = 0.0
            if (lstViewOrderWithHeaders!!.size != 0) {
                for (i in adapterList.indices) {
                    if (!adapterList[i].isHeader) {
                        totalOrders = totalOrders + 1
                        val currentOrder: Orders? = adapterList[i].order
                        if (currentOrder?.isPlacedInvoiced == true) {
                            totalPlacedOrInvoicedOrders = totalPlacedOrInvoicedOrders + 1
                            totalAmount = totalAmount + currentOrder.amount?.total?.amount!!
                        }
                    }
                }
            }
            txtTotalAmountFilterApplied?.setText(PriceDetails(totalAmount).displayValue)
            txtTotalNoOfOrdersFilterApplied?.setText(
                String.format(
                    getString(R.string.format_showing_orders_placed),
                    totalOrders,
                    totalPlacedOrInvoicedOrders
                )
            )
        } else {
            //set the visibility of order total layout and alin the list below it
            if (UserPermission.HasViewPrice()) lytWeeklySpending.setVisibility(View.VISIBLE)
            lytTotalOrdersFilterApplied?.setVisibility(View.INVISIBLE)
            val constraintSet = ConstraintSet()
            constraintSet.clone(lytConstraintSwipeRefresh)
            constraintSet.connect(
                lstOrders.getId(),
                ConstraintSet.TOP,
                lytWeeklySpending.getId(),
                ConstraintSet.BOTTOM
            )
            constraintSet.applyTo(lytConstraintSwipeRefresh)
        }
    }

    protected override fun onResume() {
        notificationBroadCastReceiverHelper =
            NotificationBroadCastReceiverHelper(this, object :
                NotificationBroadCastReceiverHelper.OnNotificationReceivedListner {
                override fun onNotificationReceived(orderId: String?, orderStatus: String?) {
                    refreshParticularOrder(orderId!!, orderStatus!!)
                }
            })
        notificationBroadCastReceiverHelper!!.registerReceiver()
        super.onResume()
    }

    protected override fun onPause() {
        notificationBroadCastReceiverHelper?.unRegisterReceiver()
        super.onPause()
    }

    private fun refreshParticularOrder(orderid: String, orderStatus: String) {
        if (mOrders != null) for (i in mOrders!!.indices) {
            if (orderid.equals(mOrders!![i].orderId, ignoreCase = true)) {
                if (!mOrders!![i].orderStatus.equals(
                        orderStatus,
                        ignoreCase = true
                    )
                ) mOrders!![i].orderStatus = orderStatus
            }
        }
        if (lstOrders.getAdapter() != null) lstOrders.getAdapter()!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }

    private fun createUseFilterForInvoicedOrdersDialog() {
        val d = Dialog(this@ViewOrdersActivity, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_use_filter_for_invoiced_orders_hint)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        d.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                SharedPref.writeBool(SharedPref.DISPLAY_USE_FILTER_FOR_INVOICED_POPUP, false)
            }
        })
        val txtUseFilterHint: TextView = d.findViewById<TextView>(R.id.txt_use_filer)
        val txtOk: TextView = d.findViewById<TextView>(R.id.txt_ok)
        txtOk.setOnClickListener(View.OnClickListener { d.dismiss() })
        ZeemartBuyerApp.setTypefaceView(
            txtUseFilterHint,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOk,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (!this@ViewOrdersActivity.isFinishing()) {
            d.show()
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECTED_FILTERS = 101
        private var pageCount = 0
        private var pageSize = 50
    }
}