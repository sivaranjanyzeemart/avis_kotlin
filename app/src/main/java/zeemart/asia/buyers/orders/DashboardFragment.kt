package zeemart.asia.buyers.orders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.shape.ShapeType
import co.mobiwise.materialintro.view.MaterialIntroView
import com.android.volley.VolleyError
import me.leolin.shortcutbadger.ShortcutBadger
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.DashboardOrdersAdapter
import zeemart.asia.buyers.adapter.DashboardOrdersAdapter.ViewHolderOrder
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.ApiParamsHelper.IncludeFields
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.NotifyDataChangedListener
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.order.Orders.Companion.sortForNearestTimeDelivery
import zeemart.asia.buyers.models.orderimport.OrdersWithPagination
import zeemart.asia.buyers.models.orderimportimport.DashboardOrdersMgr
import zeemart.asia.buyers.modelsimport.OutletSuppliersResponseForOrderSettings
import zeemart.asia.buyers.modelsimport.RetrieveAllAnnouncementsRes
import zeemart.asia.buyers.network.NotificationsApi.RetrieveAllAnnouncementsResponseListener
import zeemart.asia.buyers.network.NotificationsApi.retrieveAllAnnouncements
import zeemart.asia.buyers.network.OrderSettingsApi.OrderSettingOutletsListener
import zeemart.asia.buyers.network.OrderSettingsApi.getAllOutletsSuppliers
import zeemart.asia.buyers.network.RetrieveOrders
import zeemart.asia.buyers.network.RetrieveOrders.GetOrderDataListener
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.notifications.NotificationsActivity
import zeemart.asia.buyers.orders.createorders.BrowseCreateNewOrder
import zeemart.asia.buyers.orders.createorders.RepeatOrderActivity
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity
import zeemart.asia.buyers.orders.deliveries.DeliveryListingActivity
import zeemart.asia.buyers.orders.vieworders.ViewOrdersActivity
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment(), View.OnClickListener {
    private val btnOrderNow: Button? = null
    private lateinit var btnRepeatOrder: Button
    private lateinit var btnNewOrder: Button
    private lateinit var btnViewOrders: Button
    private lateinit var btnDeliveries: Button
    private lateinit var swipeRefreshOrdersHome: SwipeRefreshLayout
    private lateinit var lstDashboardList: RecyclerView
    private var allOrdersDashboard: MutableList<Orders>? = null
    private val listActionRequired = ArrayList<Orders>()
    private val listContinueOrdering = ArrayList<Orders>()
    private val listPendingOrders = ArrayList<Orders>()
    private val listDeliveriesToday = ArrayList<Orders>()
    private val lstDashboardData = ArrayList<DashboardOrdersMgr>()
    private var isFragmentAttached = false
    private var homeNavigationHeader: FrameLayout? = null
    private var lytAppHeader: RelativeLayout? = null
    private var mainLayout: ConstraintLayout? = null
    private var txtNavigationToBgGrey: TextView? = null
    private val isVisibleToUser = false
    private var threeDotLoader: CustomLoadingViewBlue? = null
    private var dashboardListLayoutManager: LinearLayoutManager? = null
    private lateinit var imgNotification: ImageView
    private lateinit var txtUnReadNotifications: TextView
    private var numberOfRecords = 0
    private var mShowCaseVisible = false
    private val UNIQUE_ID_FOR_SHOW_CASE = "UNIQUE_ID_FOR_SHOW_CASE"
    private lateinit var lytOrderSettings: RelativeLayout
    private var txtOrderSettingsHeader: TextView? = null
    private var txtOrderSettingsSubHeader: TextView? = null
    private var isOrderSettingsVisible = false
    override fun onResume() {
        super.onResume()
        Log.e(
            "boolean",
            "onResume: " + SharedPref.readBool(SharedPref.DISPLAY_SHOW_CASE_FOR_NEW_ORDER, false)
        )
        Handler().postDelayed({
            if (!mShowCaseVisible && SharedPref.readBool(
                    SharedPref.DISPLAY_SHOW_CASE_FOR_NEW_ORDER,
                    false
                )!!
            ) {
                MaterialIntroView.Builder(activity)
                    .enableDotAnimation(true)
                    .enableIcon(false)
                    .setFocusGravity(FocusGravity.CENTER)
                    .setFocusType(Focus.ALL)
                    .setDelayMillis(300)
                    .enableFadeAnimation(true)
                    .performClick(true)
                    .setInfoText("Ready to order? Tap here to begin")
                    .setShape(ShapeType.CIRCLE)
                    .setTarget(btnNewOrder)
                    .setUsageId(UNIQUE_ID_FOR_SHOW_CASE) //THIS SHOULD BE UNIQUE ID
                    .enableDotAnimation(false)
                    .show()
                mShowCaseVisible = true
            }
        }, 300)
        //register receiver
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            mNotifMessageReceiver,
            IntentFilter(NotificationConstants.NOTIFICATION_BROADCAST_INTENT_ANNOUNCEMENT)
        )
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            mNotifMessageReceiver,
            IntentFilter(NotificationConstants.NOTIFICATION_BROADCAST_INTENT_INVOICE)
        )
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            mNotifMessageReceiver,
            IntentFilter(NotificationConstants.BROADCAST_GRN_RECEIVE)
        )
        Log.d("ON RESUME", "dashboard fragment")

//      if (isAdded() && isVisible() && getUserVisibleHint()) {
//            threeDotLoader.setVisibility(View.GONE);
//            refreshDashboard();
//        }
        txtUnReadNotifications!!.visibility = View.GONE
        callNotificationAnnouncementsDataAPI()
    }

    override fun onPause() {
        //unregister receiver
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mNotifMessageReceiver)
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mNotifMessageReceiver)
        super.onPause()
    }

    fun refreshDashboard() {
        if (isFragmentAttached && threeDotLoader != null) threeDotLoader!!.visibility = View.VISIBLE
        //nestedScrollView.setVisibility(View.GONE);
        val apiParamsHelperRetrieveOthers = ApiParamsHelper()
        apiParamsHelperRetrieveOthers.setOrderIncludeFields(
            arrayOf(
                IncludeFields.SUPPLIER,
                IncludeFields.OUTLET,
                IncludeFields.AMOUNT,
                IncludeFields.ORDER_ID,
                IncludeFields.ORDER_STATUS,
                IncludeFields.PRODUCTS,
                IncludeFields.TIME_CREATED,
                IncludeFields.TIME_DELIVERED,
                IncludeFields.IS_RECEIVED,
                IncludeFields.ORDER_TYPE,
                IncludeFields.TIME_UPDATED,
                IncludeFields.APPROVERS,
                IncludeFields.TIME_PLACED,
                IncludeFields.IS_INVOICED,
                IncludeFields.CREATED_BY,
                IncludeFields.TIME_CUT_OFF,
                IncludeFields.TRANSACTION_IMAGE,
                IncludeFields.PAYMENT_TYPE
            )
        )
        apiParamsHelperRetrieveOthers.setDefaultSixtyDaysPeriod(null)
        apiParamsHelperRetrieveOthers.setSortField(ApiParamsHelper.SortField.TIME_UPDATED)
        apiParamsHelperRetrieveOthers.setSortOrder(ApiParamsHelper.SortOrder.DESC_CAP)
        apiParamsHelperRetrieveOthers.setOrderStatus(
            arrayOf(
                OrderStatus.DRAFT.statusName,
                OrderStatus.CREATED.statusName,
                OrderStatus.CREATING.statusName,
                OrderStatus.APPROVING.statusName,
                OrderStatus.REJECTING.statusName,
                OrderStatus.CANCELLING.statusName,
                OrderStatus.PENDING_PAYMENT.statusName
            )
        )
        val apiParamsHelperDeliveries = ApiParamsHelper()
        apiParamsHelperDeliveries.setSortBy(ApiParamsHelper.SortField.TIME_UPDATED)
        apiParamsHelperDeliveries.setSortOrder(ApiParamsHelper.SortOrder.DESC_CAP)
        apiParamsHelperDeliveries.setOrderStatus(arrayOf(OrderStatus.PLACED.statusName))
        val todayDate = DateHelper.getGivenDateStartEndTime(Calendar.getInstance())
        apiParamsHelperDeliveries.setDeliveryStartDate(todayDate.startDateMillis)
        apiParamsHelperDeliveries.setDeliveryEndDate(todayDate.endDateMillis)
        apiParamsHelperDeliveries.setPageSize(10)
        apiParamsHelperDeliveries.setPageNumber(1)
        apiParamsHelperDeliveries.setIsReceived(false)
        RetrieveOrders(activity, object : GetOrderDataListener {
            override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                RetrieveOrders(activity, object : GetOrderDataListener {
                    override fun onErrorResponse(error: VolleyError?) {
                        if (isFragmentAttached) {
                            threeDotLoader!!.visibility = View.GONE
                            getToastRed(error!!.message)
                        }
                    }

                    override fun onSuccessResponse(ordersWithPagination: OrdersWithPagination?) {
                        if (allOrdersDashboard == null) allOrdersDashboard = ArrayList()
                        allOrdersDashboard!!.clear()
                        if (ordersWithPagination != null && ordersWithPagination.data != null) {
                            numberOfRecords = ordersWithPagination.data!!.numberOfRecords!!
                            if (ordersData != null && ordersData.data!!.orders != null && ordersData.data!!.orders!!.size > 0) {
                                allOrdersDashboard!!.addAll(ordersData.data!!.orders!!)
                            }
                            if (ordersWithPagination != null && ordersWithPagination.data!!.orders != null && ordersWithPagination.data!!.orders!!.size > 0) {
                                allOrdersDashboard!!.addAll(ordersWithPagination.data!!.orders!!)
                            }
                            setDashboardData(allOrdersDashboard)
                        }
                    }
                }, SharedPref.allOutlets!!, apiParamsHelperDeliveries).orders
            }

            override fun onErrorResponse(error: VolleyError?) {
                if (isFragmentAttached) {
                    threeDotLoader!!.visibility = View.GONE
                    getToastRed(error!!.message)
                }
            }
        }, SharedPref.allOutlets!!, apiParamsHelperRetrieveOthers).orders
        callNotificationAnnouncementsDataAPI()
        callOrderSettingsApi()
    }

    private fun callOrderSettingsApi() {
        getAllOutletsSuppliers(activity, object : OrderSettingOutletsListener {
            override fun onOutletSuccess(allStockShelveData: OutletSuppliersResponseForOrderSettings?) {
                if (isFragmentAttached) {
                    if (allStockShelveData != null && allStockShelveData.data != null && allStockShelveData.data!!.size > 0) {
                        isOrderSettingsVisible = true
                        lytOrderSettings!!.visibility = View.VISIBLE
                    } else {
                        isOrderSettingsVisible = false
                        lytOrderSettings!!.visibility = View.GONE
                    }
                    threeDotLoader!!.visibility = View.GONE
                }
            }

            override fun errorResponse(error: VolleyError?) {
                if (isFragmentAttached) {
                    isOrderSettingsVisible = false
                    lytOrderSettings!!.visibility = View.GONE
                    threeDotLoader!!.visibility = View.GONE
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val v = inflater.inflate(R.layout.layout_home_root, container, false)
        btnRepeatOrder = v.findViewById(R.id.btn_home_repeat_order)
        btnRepeatOrder.setOnClickListener(this)
        lytOrderSettings = v.findViewById(R.id.lyt_order_settings)
        txtOrderSettingsHeader = v.findViewById(R.id.txt_add_on_order_content)
        txtOrderSettingsSubHeader = v.findViewById(R.id.txt_add_on_order_heading)
        lytOrderSettings.setVisibility(View.GONE)
        lytOrderSettings.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, OrderSettingsActivity::class.java)
            startActivity(intent)
        })
        txtUnReadNotifications = v.findViewById(R.id.txt_number_of_unread_notifications)
        txtUnReadNotifications.setVisibility(View.GONE)
        btnNewOrder = v.findViewById(R.id.btn_home_new_order)
        btnNewOrder.setOnClickListener(this)
        btnViewOrders = v.findViewById(R.id.btn_home_view_order)
        btnViewOrders.setOnClickListener(this)
        btnDeliveries = v.findViewById(R.id.btn_home_deliveries)
        btnDeliveries.setOnClickListener(this)
        homeNavigationHeader = v.findViewById(R.id.lyt_home_top_navigation)
        lytAppHeader = v.findViewById(R.id.relative_layout_home_header)
        mainLayout = v.findViewById(R.id.lyt_home_fragment)
        txtNavigationToBgGrey = v.findViewById(R.id.txt_lyt_navigation_bg_grey)
        swipeRefreshOrdersHome = v.findViewById(R.id.lyt_swipe_refresh_orders_home)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshOrdersHome)
        swipeRefreshOrdersHome.setOnRefreshListener(OnRefreshListener {
            refreshDashboard()
            swipeRefreshOrdersHome.setRefreshing(false)
        })
        lstDashboardList = v.findViewById(R.id.lst_dashboard_list)
        dashboardListLayoutManager = LinearLayoutManager(activity)
        lstDashboardList.setLayoutManager(dashboardListLayoutManager)
        threeDotLoader = v.findViewById(R.id.spin_kit_loader)
        imgNotification = v.findViewById(R.id.img_notification_icon)
        imgNotification.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_NOTIFICATIONS)
            val intent = Intent(activity, NotificationsActivity::class.java)
            requireActivity().startActivity(intent)
        })
        setFonts()
        return v
    }

    private fun setFonts() {
        setTypefaceView(btnRepeatOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnViewOrders, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnDeliveries, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnNewOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtUnReadNotifications,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtOrderSettingsHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(
            txtOrderSettingsSubHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun callNotificationAnnouncementsDataAPI() {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setAnnouncementsStatus(ApiParamsHelper.Status.ACTIVE.value)
        retrieveAllAnnouncements(activity, object : RetrieveAllAnnouncementsResponseListener {
            override fun onSuccessResponse(notificationAnnouncementsList: RetrieveAllAnnouncementsRes.Response?) {
                if (txtUnReadNotifications != null) txtUnReadNotifications!!.post {
                    val notificationAnnouncements =
                        notificationAnnouncementsList!!.data!!.announcementsList
                    val unReadNotificationAnnouncements =
                        NotificationDataHelper.returnUnreadNotificationAnnouncements(
                            notificationAnnouncements
                        )
                    if (unReadNotificationAnnouncements != null && unReadNotificationAnnouncements.size > 0) {
                        txtUnReadNotifications!!.visibility = View.VISIBLE
                        txtUnReadNotifications!!.text =
                            Integer.toString(unReadNotificationAnnouncements.size)
                    } else {
                        txtUnReadNotifications!!.visibility = View.GONE
                    }
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {}
        }, apiParamsHelper)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_home_new_order -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_DASHBOARD_NEW_ORDER)
                if (StringHelper.isStringNullOrEmpty(
                        SharedPref.read(
                            SharedPref.SELECTED_OUTLET_ID,
                            ""
                        )
                    )
                ) {
                    val buyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        SharedPref.read(
                            SharedPref.BUYER_DETAIL,
                            ""
                        ), BuyerDetails::class.java
                    )
                    if (buyerDetails.outlet!!.size == 1) {
                        SharedPref.write(
                            SharedPref.SELECTED_OUTLET_NAME,
                            buyerDetails.outlet!![0].outletName
                        )
                        SharedPref.write(
                            SharedPref.SELECTED_OUTLET_ID,
                            buyerDetails.outlet!![0].outletId
                        )
                        val newIntent = Intent(activity, BrowseCreateNewOrder::class.java)
                        startActivity(newIntent)
                    }
                    val newIntent = Intent(activity, SelectOutletActivity::class.java)
                    newIntent.putExtra(
                        ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                        ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_DASH_BOARD_FOR_BROWSE_SCREEN
                    )
                    startActivity(newIntent)
                } else {
                    /* Intent newIntent = new Intent(getActivity(), CreateOrderSupplierListActivity.class);
                    startActivity(newIntent);*/
                    val browseForNewOrderIntent = Intent(activity, BrowseCreateNewOrder::class.java)
                    startActivity(browseForNewOrderIntent)
                }
            }
            R.id.btn_home_repeat_order -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_DASHBOARD_REPEAT_ORDER)
                val selectedOutletId = SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
                if (StringHelper.isStringNullOrEmpty(selectedOutletId)) {
                    val buyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        SharedPref.read(
                            SharedPref.BUYER_DETAIL,
                            ""
                        ), BuyerDetails::class.java
                    )
                    if (buyerDetails.outlet!!.size == 1) {
                        SharedPref.write(
                            SharedPref.SELECTED_OUTLET_NAME,
                            buyerDetails.outlet!![0].outletName
                        )
                        SharedPref.write(
                            SharedPref.SELECTED_OUTLET_ID,
                            buyerDetails.outlet!![0].outletId
                        )
                        val newIntent = Intent(activity, RepeatOrderActivity::class.java)
                        startActivity(newIntent)
                    }
                    val newIntent = Intent(activity, SelectOutletActivity::class.java)
                    newIntent.putExtra(
                        ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                        ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_DASH_BOARD_FOR_REPEAT_ORDER
                    )
                    startActivity(newIntent)
                } else {
                    val newIntent = Intent(activity, RepeatOrderActivity::class.java)
                    startActivity(newIntent)
                }
            }
            R.id.btn_home_view_order -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_DASHBOARD_VIEW_ORDERS)
                val newIntentOrders = Intent(activity, ViewOrdersActivity::class.java)
                startActivity(newIntentOrders)
            }
            R.id.btn_home_deliveries -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_DASHBOARD_DELIVERIES)
                val newIntentDeliveries = Intent(activity, DeliveryListingActivity::class.java)
                startActivity(newIntentDeliveries)
            }
            else -> {}
        }
    }

    fun setDashboardData(orders: List<Orders>?) {
        if (isFragmentAttached) {
            threeDotLoader!!.visibility = View.GONE
            lstDashboardList!!.visibility = View.VISIBLE
            listContinueOrdering.clear()
            listDeliveriesToday.clear()
            listPendingOrders.clear()
            listActionRequired.clear()
            if (orders != null && orders.size > 0) {
                for (i in orders.indices) {
                    var deliveryDate = ""
                    if (orders[i].timeDelivered != null) {
                        deliveryDate =
                            DateHelper.getDateInDateMonthYearFormat(orders[i].timeDelivered)
                    }
                    val currentEpochTime = System.currentTimeMillis() / 1000
                    val todayDate = DateHelper.getDateInDateMonthYearFormat(currentEpochTime)
                    if (orders[i].orderStatus == OrderStatus.PENDING_PAYMENT.statusName) {
                        if ((orders[i].transactionImage != null && orders[i].transactionImage!!.status == Orders.PaymentStatus.UPLOADED.statusName) || (orders[i].paymentType != null && orders[i].paymentType == ZeemartAppConstants.FINAXAR)) {
                            listPendingOrders.add(orders[i])
                        } else {
                            listActionRequired.add(orders[i])
                        }
                    }
                    if (orders[i].orderStatus == OrderStatus.CREATED.statusName && orders[i].isUserApprover) {
                        listActionRequired.add(orders[i])
                    }
                    if (orders[i].orderStatus == OrderStatus.DRAFT.statusName && orders[i].isOrderByUser) {
                        listContinueOrdering.add(orders[i])
                    }
                    if (orders[i].isPendingOrderStatusCreatorCheck && orders[i].isOrderByUser) {
                        listPendingOrders.add(orders[i])
                    }
                    if (orders[i].isPendingOrderStatusApproverCheck && orders[i].isUserApprover) {
                        listPendingOrders.add(orders[i])
                    }
                    if (orders[i].orderStatus == OrderStatus.PLACED.statusName && deliveryDate == todayDate) {
                        listDeliveriesToday.add(orders[i])
                    }
                }
            } else {
            }
            createDashboardDataList()
            lstDashboardList.adapter = DashboardOrdersAdapter(
                requireContext(),
                lstDashboardData,
                object : NotifyDataChangedListener {
                    override fun notifyResetAdapter() {
                        refreshDashboard()
                    }
                })
            initDashboardSwipe()
            setDashboardListScrollBehaviour()
        }
    }

    /**
     * creates a single list of data to be displayed on dashboard
     * for all the categories section on dashboard
     */
    fun createDashboardDataList() {
        lstDashboardData.clear()
        if (listActionRequired.size > 0) {
            sortForNearestTimeDelivery(listActionRequired)
            val dashboardOrdersManager = DashboardOrdersMgr()
            dashboardOrdersManager.isHeader = true
            dashboardOrdersManager.setDashboardOrdersMgrHeader(resources.getString(R.string.fragment_home_txt_home_action_required_text))
            lstDashboardData.add(dashboardOrdersManager)
            for (i in listActionRequired.indices) {
                val dashboardOrdersManagerOrder = DashboardOrdersMgr()
                dashboardOrdersManagerOrder.isActionRequiredOrder = true
                dashboardOrdersManagerOrder.order = listActionRequired[i]
                lstDashboardData.add(dashboardOrdersManagerOrder)
            }
            ShortcutBadger.applyCount(context, listActionRequired.size)
        } else {
            ShortcutBadger.applyCount(context, 0)
        }
        if (listContinueOrdering.size > 0) {
            val dashboardOrdersManager = DashboardOrdersMgr()
            dashboardOrdersManager.isHeader = true
            dashboardOrdersManager.setDashboardOrdersMgrHeader(resources.getString(R.string.fragment_home_txt_home_continue_ordering_text))
            lstDashboardData.add(dashboardOrdersManager)
            for (i in listContinueOrdering.indices) {
                val dashboardOrdersManagerOrder = DashboardOrdersMgr()
                dashboardOrdersManagerOrder.isContinueOrderingOrder = true
                dashboardOrdersManagerOrder.order = listContinueOrdering[i]
                lstDashboardData.add(dashboardOrdersManagerOrder)
            }
        }
        if (listPendingOrders.size > 0) {
            sortForNearestTimeDelivery(listPendingOrders)
            val dashboardOrdersManager = DashboardOrdersMgr()
            dashboardOrdersManager.isHeader = true
            dashboardOrdersManager.setDashboardOrdersMgrHeader(
                resources.getString(
                    R.string.txt_orders_pending,
                    listPendingOrders.size
                )
            )
            lstDashboardData.add(dashboardOrdersManager)
            for (i in listPendingOrders.indices) {
                val dashboardOrdersManagerOrder = DashboardOrdersMgr()
                dashboardOrdersManagerOrder.isPendingOrder = true
                dashboardOrdersManagerOrder.order = listPendingOrders[i]
                lstDashboardData.add(dashboardOrdersManagerOrder)
            }
        } else {
            val dashboardOrdersManager = DashboardOrdersMgr()
            dashboardOrdersManager.isHeader = true
            dashboardOrdersManager.setDashboardOrdersMgrHeader(resources.getString(R.string.txt_orders_pending, 0))
            lstDashboardData.add(dashboardOrdersManager)
            val dashboardOrdersManagerOrder = DashboardOrdersMgr()
            dashboardOrdersManagerOrder.isNoPendingOrder = true
            lstDashboardData.add(dashboardOrdersManagerOrder)
        }
        val dashboardOrdersManagerAllPlaced = DashboardOrdersMgr()
        dashboardOrdersManagerAllPlaced.isViewAllPlacedData = true
        dashboardOrdersManagerAllPlaced.viewAllData =
            resources.getString(R.string.fragment_home_txt_home_view_place_orders_text)
        lstDashboardData.add(dashboardOrdersManagerAllPlaced)
        if (listDeliveriesToday.size > 0) {
            val dashboardOrdersManager = DashboardOrdersMgr()
            dashboardOrdersManager.isHeader = true
            val dashboardOrdersManagerDeliverySchedule = DashboardOrdersMgr()
            dashboardOrdersManagerDeliverySchedule.viewAllData =
                resources.getString(R.string.fragment_home_txt_home_view_all_orders_text)
            dashboardOrdersManagerDeliverySchedule.isViewAllDeliveriesToday = true
            if (numberOfRecords == 1) {
                dashboardOrdersManager.setDashboardOrdersMgrHeader(
                    resources.getString(
                        R.string.txt_delivery_today,
                        numberOfRecords
                    )
                )
            } else {
                dashboardOrdersManager.setDashboardOrdersMgrHeader(
                    resources.getString(
                        R.string.txt_deliveries_today,
                        numberOfRecords
                    )
                )
            }
            lstDashboardData.add(dashboardOrdersManager)
            for (i in listDeliveriesToday.indices) {
                val dashboardOrdersManagerOrder = DashboardOrdersMgr()
                dashboardOrdersManagerOrder.isDeliveriesOrder = true
                dashboardOrdersManagerOrder.order = listDeliveriesToday[i]
                lstDashboardData.add(dashboardOrdersManagerOrder)
            }
            lstDashboardData.add(dashboardOrdersManagerDeliverySchedule)
        } else {
            val dashboardOrdersManager = DashboardOrdersMgr()
            dashboardOrdersManager.isHeader = true
            dashboardOrdersManager.setDashboardOrdersMgrHeader(resources.getString(R.string.txt_deliveries_today, 0))
            lstDashboardData.add(dashboardOrdersManager)
            val dashboardOrdersManagerOrder = DashboardOrdersMgr()
            dashboardOrdersManagerOrder.isNoDeliveries = true
            lstDashboardData.add(dashboardOrdersManagerOrder)
        }
    }

    private fun setDashboardListScrollBehaviour() {
        val totalItems = dashboardListLayoutManager!!.itemCount
        val lastVisibleItemPosition = dashboardListLayoutManager!!.findLastVisibleItemPosition()
        val dashboardListScrollListener: RecyclerView.OnScrollListener? = null

        if (totalItems > lastVisibleItemPosition) {
            //add on scroll listener
            lstDashboardList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val currentFirstVisibleItemPosition =
                            dashboardListLayoutManager!!.findFirstVisibleItemPosition()
                        if (currentFirstVisibleItemPosition == 0 && lytAppHeader!!.visibility != View.GONE) {
                            lytAppHeader!!.visibility = View.VISIBLE
                            homeNavigationHeader!!.setBackgroundDrawable(null)
                            txtNavigationToBgGrey!!.setBackgroundResource(R.color.faint_grey)
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val currentFirstVisibleItemPosition =
                        dashboardListLayoutManager!!.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition =
                        dashboardListLayoutManager!!.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = dashboardListLayoutManager!!.itemCount
                    if (currentFirstVisibleItemPosition > 0) {
                        lytAppHeader!!.visibility = View.GONE
                        lytOrderSettings!!.visibility = View.GONE
                        homeNavigationHeader!!.setBackgroundResource(R.color.black)
                        txtNavigationToBgGrey!!.setBackgroundResource(R.color.black)
                    } else if (dy < 0 && lytAppHeader!!.visibility == View.GONE && currentFirstVisibleItemPosition == 0) {
                        lytAppHeader!!.visibility = View.VISIBLE
                        if (isOrderSettingsVisible) lytOrderSettings!!.visibility = View.VISIBLE
                        homeNavigationHeader!!.setBackgroundDrawable(null)
                        txtNavigationToBgGrey!!.setBackgroundResource(R.color.faint_grey)
                    } else if (currentFirstVisibleItemPosition == 0 && lastVisibleItemPosition == totalItemCount - 1 && dy > 0) {
                        lytAppHeader!!.visibility = View.VISIBLE
                        if (isOrderSettingsVisible) lytOrderSettings!!.visibility = View.VISIBLE
                        homeNavigationHeader!!.setBackgroundDrawable(null)
                        txtNavigationToBgGrey!!.setBackgroundResource(R.color.faint_grey)
                    }
                }
            })
        } else {
            lstDashboardList.addOnScrollListener(dashboardListScrollListener!!)
        }
    }

    /**
     * register this receiver to know everytime
     * a notification with an orderId comes so that we can refresh the dashboard
     */
    private val mNotifMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (userVisibleHint) {
                refreshDashboard()
            }
        }
    }

    private fun initDashboardSwipe() {
        val actionRequiredSwipehelper: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val background = ColorDrawable()
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                val order = lstDashboardData[viewHolder.adapterPosition].order
                return if (order != null && !StringHelper.isStringNullOrEmpty(order.orderType)
                    && (order.orderType == Orders.Type.DEAL.name) || (order?.orderType) == Orders.Type.ESSENTIALS.name) {
                    0
                } else {
                    if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_CONTINUE_ORDERING_ORDER) {
                        ItemTouchHelper.LEFT
                    } else if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_ACTION_REQUIRED_ORDER) {
                        if (order!!.orderStatus != OrderStatus.PENDING_PAYMENT.statusName) {
                            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                        } else {
                            0
                        }
                    } else if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_PENDING_ORDER) {
                        if (order!!.orderStatus != OrderStatus.PENDING_PAYMENT.statusName) {
                            ItemTouchHelper.RIGHT
                        } else {
                            0
                        }
                    } else if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_TODAY_DELIVERY_ORDER) {
                        ItemTouchHelper.RIGHT
                    } else {
                        0
                    }
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val mAdapter = lstDashboardList!!.adapter as DashboardOrdersAdapter?
                val order = lstDashboardData[viewHolder.adapterPosition].order
                if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_ACTION_REQUIRED_ORDER) {
                    if (mAdapter != null) {
                        if (direction == ItemTouchHelper.LEFT) {
                            mAdapter.removeAt(
                                viewHolder.adapterPosition,
                                false,
                                false,
                                false,
                                false,
                                order!!
                            )
                        } else if (direction == ItemTouchHelper.RIGHT) {
                            mAdapter.removeAt(
                                viewHolder.adapterPosition,
                                false,
                                true,
                                false,
                                false,
                                order!!
                            )
                        }
                    }
                } else if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_PENDING_ORDER) {
                    mAdapter!!.removeAt(
                        viewHolder.adapterPosition,
                        false,
                        false,
                        false,
                        true,
                        order!!
                    )
                } else if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_CONTINUE_ORDERING_ORDER) {
                    mAdapter!!.removeAt(
                        viewHolder.adapterPosition,
                        true,
                        false,
                        false,
                        false,
                        order!!
                    )
                } else if (viewHolder.itemViewType == DashboardOrdersAdapter.VIEW_TYPE_TODAY_DELIVERY_ORDER) {
                    if (order != null && order.isReceived) {
                        mAdapter!!.removeAt(
                            viewHolder.adapterPosition,
                            false,
                            false,
                            false,
                            true,
                            order
                        )
                    } else {
                        mAdapter!!.removeAt(
                            viewHolder.adapterPosition,
                            false,
                            false,
                            true,
                            false,
                            order!!
                        )
                    }
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
                val mViewHolder = viewHolder as ViewHolderOrder
                if (viewHolder.getItemViewType() == DashboardOrdersAdapter.VIEW_TYPE_ACTION_REQUIRED_ORDER) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX <= 0) // Swiping Left
                    {
                        //show approve button
                        val bmp = BitmapFactory.decodeResource(
                            activity!!.resources,
                            R.drawable.white_cross
                        )
                        drawLeftSwipeButton(
                            background,
                            c,
                            dX,
                            mViewHolder,
                            resources.getString(R.string.txt_reject),
                            bmp,
                            resources.getColor(
                                R.color.pinky_red
                            )
                        )
                    } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) //swiping Right
                    {
                        //show approve button
                        val bmp = BitmapFactory.decodeResource(
                            activity!!.resources,
                            R.drawable.whitetick
                        )
                        drawRightSwipeButton(
                            background,
                            c,
                            dX,
                            mViewHolder,
                            resources.getString(R.string.txt_approve),
                            bmp,
                            resources.getColor(
                                R.color.green
                            )
                        )
                    }
                } else if (viewHolder.getItemViewType() == DashboardOrdersAdapter.VIEW_TYPE_CONTINUE_ORDERING_ORDER) {
                    AnalyticsHelper.logAction(
                        activity,
                        AnalyticsHelper.SLIDE_ITEM_DASHBOARD_DELETE_DRAFT
                    )
                    val bmp = BitmapFactory.decodeResource(
                        activity!!.resources,
                        R.drawable.deletedraft
                    )
                    drawLeftSwipeButton(
                        background,
                        c,
                        dX,
                        mViewHolder,
                        resources.getString(R.string.txt_delete),
                        bmp,
                        resources.getColor(
                            R.color.pinky_red
                        )
                    )
                } else if (viewHolder.getItemViewType() == DashboardOrdersAdapter.VIEW_TYPE_PENDING_ORDER) {
                    val bmp = BitmapFactory.decodeResource(
                        activity!!.resources,
                        R.drawable.repeatordericon
                    )
                    drawRightSwipeButton(
                        background,
                        c,
                        dX,
                        mViewHolder,
                        getString(R.string.txt_repeat),
                        bmp,
                        resources.getColor(
                            R.color.text_blue
                        )
                    )
                } else if (viewHolder.getItemViewType() == DashboardOrdersAdapter.VIEW_TYPE_TODAY_DELIVERY_ORDER) {
                    val order = lstDashboardData[viewHolder.getAdapterPosition()].order
                    if (order != null && order.isReceived) {
                        val bmp = BitmapFactory.decodeResource(
                            activity!!.resources,
                            R.drawable.repeatordericon
                        )
                        drawRightSwipeButton(
                            background,
                            c,
                            dX,
                            mViewHolder,
                            getString(R.string.txt_repeat),
                            bmp,
                            resources.getColor(
                                R.color.text_blue
                            )
                        )
                    } else {
                        val bmp = BitmapFactory.decodeResource(
                            activity!!.resources,
                            R.drawable.receive_icon
                        )
                        drawRightSwipeButton(
                            background,
                            c,
                            dX,
                            mViewHolder,
                            getString(R.string.txt_receive),
                            bmp,
                            resources.getColor(
                                R.color.text_blue
                            )
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
        val helperActionRequired = ItemTouchHelper(actionRequiredSwipehelper)
        helperActionRequired.attachToRecyclerView(lstDashboardList)
    }

    private fun drawLeftSwipeButton(
        background: ColorDrawable,
        c: Canvas,
        dX: Float,
        mViewHolder: ViewHolderOrder,
        text: String,
        bmp: Bitmap,
        color: Int,
    ) {
        //Show reject button
        background.color = color
        background.setBounds(
            mViewHolder.itemView.right + dX.toInt(),
            mViewHolder.itemView.top,
            mViewHolder.itemView.right,
            mViewHolder.itemView.bottom
        )
        background.draw(c)
        val p = Paint()
        val buttonWidth = mViewHolder.itemView.right - SWIPE_BUTTON_WIDTH
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
        p.textSize = SWIPE_BUTTON_TEXT_SIZE.toFloat()
        val textWidth = p.measureText(text)
        val bounds = Rect()
        p.getTextBounds(text, 0, text.length, bounds)
        val combinedHeight =
            (bmp.height + SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
        c.drawBitmap(
            bmp,
            rightButton.centerX() - bmp.width / 2,
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

    private fun drawRightSwipeButton(
        background: ColorDrawable,
        c: Canvas,
        dX: Float,
        mViewHolder: ViewHolderOrder,
        text: String,
        bmp: Bitmap,
        color: Int,
    ) {
        background.color = color
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
            SWIPE_BUTTON_WIDTH.toFloat(),
            mViewHolder.itemView.bottom.toFloat()
        )
        p.color = color
        c.drawRect(rightButton, p)
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = SWIPE_BUTTON_TEXT_SIZE.toFloat()
        val textWidth = p.measureText(text)
        val bounds = Rect()
        p.getTextBounds(text, 0, text.length, bounds)
        val combinedHeight =
            (bmp.height + SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
        c.drawBitmap(
            bmp,
            rightButton.centerX() - bmp.width / 2,
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

    companion object {
        private val SWIPE_BUTTON_WIDTH = CommonMethods.dpToPx(70)
        private val SWIPE_BUTTON_TEXT_SIZE = CommonMethods.dpToPx(14)
        private val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE = CommonMethods.dpToPx(11)

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }
}