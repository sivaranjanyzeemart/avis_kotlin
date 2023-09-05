package zeemart.asia.buyers.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.NotificationAdapter
import zeemart.asia.buyers.adapter.NotificationAnnouncementsAdapter
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.NotificationDataHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.pagination.NotificationScrollHelper
import zeemart.asia.buyers.helper.pagination.PaginationListScrollHelper
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.NotificationBroadCastReceiverHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.NotificationDetail
import zeemart.asia.buyers.models.NotificationDetailMgr
import zeemart.asia.buyers.modelsimport.RetrieveAllAnnouncementsRes
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes
import zeemart.asia.buyers.network.NotificationsApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.util.*

/**
 * Created by Rajprudhvi on 11/06/2019.
 */
class NotificationsActivity : AppCompatActivity() {
    private var btnBack: ImageView? = null
    private lateinit var btnNotificationSettings: ImageButton
    private var txtNotificationHeader: TextView? = null
    private var btnNewsTab: Button? = null
    private var btnActivitiesTab: Button? = null
    private var txtNewsTabHighlighter: TextView? = null
    private var txtActivitiesTabHighlighter: TextView? = null
    private lateinit var threeDotLoaderBlue: CustomLoadingViewBlue
    private lateinit var swipeRefreshLayoutNotifications: SwipeRefreshLayout
    private lateinit var swipeRefreshLayoutNotificationAnnouncements: SwipeRefreshLayout
    private lateinit var lstAnnouncementNotifications: RecyclerView
    private lateinit var lstActivitiesNotifications: RecyclerView
    private lateinit var lytNoNotifications: RelativeLayout
    private var txtNoNotifications: TextView? = null
    private var isActivitiesNotificationsSelected = false
    private var notificationBroadCastReceiverHelper: NotificationBroadCastReceiverHelper? = null
    private lateinit var lstNotificationDetails: List<NotificationDetail>
    private var lstNotificationAnnouncements: MutableList<RetrieveSpecificAnnouncementRes.Announcements>? =
        ArrayList<RetrieveSpecificAnnouncementRes.Announcements>()
    private var pageCount = 0
    private var pageSize = 10
    private var totalPageCount = 1
    private lateinit var notificationScrollHelper: NotificationScrollHelper
    private var lstNotificationLayoutManager: LinearLayoutManager? = null
    private var notificationAnnouncementsAdapter: NotificationAnnouncementsAdapter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_notifications)
        btnBack = findViewById<ImageView>(R.id.img_back_button)
        btnBack!!.setOnClickListener { finish() }
        txtNotificationHeader = findViewById<TextView>(R.id.txt_notification_heading)
        btnNotificationSettings = findViewById<ImageButton>(R.id.notification_settings_image_button)
        btnNotificationSettings.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(
                this@NotificationsActivity,
                AnalyticsHelper.TAP_NOTIFICATIONS_NOTIFICATION_SETTINGS
            )
            val newIntent = Intent(this@NotificationsActivity, NotificationSettings::class.java)
            startActivity(newIntent)
        })
        btnNewsTab = findViewById<Button>(R.id.notification_btn_news)
        btnNewsTab!!.setOnClickListener {
            AnalyticsHelper.logAction(
                this@NotificationsActivity,
                AnalyticsHelper.TAP_NOTIFICATIONS_NEWS_VIEW_PAGE
            )
            notificationScrollHelper?.setOnScrollListener()
            setAnnouncementsTabActive()
        }
        btnActivitiesTab = findViewById<Button>(R.id.notification_btn_activity)
        btnActivitiesTab!!.setOnClickListener {
            AnalyticsHelper.logAction(
                this@NotificationsActivity,
                AnalyticsHelper.TAP_NOTIFICATIONS_ACTIVITY_VIEW_PAGE
            )
            setNotificationsTabActive()
        }
        txtNewsTabHighlighter = findViewById<TextView>(R.id.notification_txt_btn_news_highlighter)
        txtActivitiesTabHighlighter =
            findViewById<TextView>(R.id.notification_txt_btn_activity_highlighter)
        threeDotLoaderBlue =
            findViewById<CustomLoadingViewBlue>(R.id.spin_kit_loader_notifications_blue)
        swipeRefreshLayoutNotificationAnnouncements =
            findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_notifications_announcements)
        swipeRefreshLayoutNotificationAnnouncements.setEnabled(true)
        swipeRefreshLayoutNotifications =
            findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_notifications)
        swipeRefreshLayoutNotifications.setEnabled(false)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayoutNotifications)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayoutNotificationAnnouncements)
        swipeRefreshLayoutNotifications.setOnRefreshListener(object :
            SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                threeDotLoaderBlue.setVisibility(View.VISIBLE)
                callNotificationActivitiesDataAPI()
                swipeRefreshLayoutNotifications.setRefreshing(false)
            }
        })
        swipeRefreshLayoutNotificationAnnouncements.setOnRefreshListener(object :
            SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                setInitialParamsForNotificationAnnouncements()
                swipeRefreshLayoutNotificationAnnouncements.setRefreshing(false)
            }
        })
        lstActivitiesNotifications = findViewById<RecyclerView>(R.id.lst_activity_notifications)
        lstActivitiesNotifications.setLayoutManager(LinearLayoutManager(this))
        lstAnnouncementNotifications = findViewById<RecyclerView>(R.id.lst_news_notifications)
        lstNotificationLayoutManager = LinearLayoutManager(this)
        lstAnnouncementNotifications.setLayoutManager(lstNotificationLayoutManager)
        notificationScrollHelper = NotificationScrollHelper(
            this@NotificationsActivity,
            lstAnnouncementNotifications,
            lstNotificationLayoutManager!!,
            object : PaginationListScrollHelper.ScrollCallback {
                override fun loadMore() {
                    if (pageCount < totalPageCount) {
                        callNotificationAnnouncementsDataAPI()
                    }
                }
            })
        notificationScrollHelper.setOnScrollListener()
        txtNoNotifications = findViewById<TextView>(R.id.no_notification_text)
        lytNoNotifications = findViewById<RelativeLayout>(R.id.lyt_no_deliveries_activities)
        lytNoNotifications.setVisibility(View.GONE)
        setInitialParamsForNotificationAnnouncements()
        callNotificationActivitiesDataAPI()
        setFont()
    }

    private fun callNotificationActivitiesDataAPI() {
        NotificationsApi.retrieveNotifications(
            this@NotificationsActivity,
            object : NotificationsApi.RetrieveNotificationResponseListener {
                override fun onSuccessResponse(notificationDetails: List<NotificationDetail?>?) {
                    threeDotLoaderBlue.setVisibility(View.GONE)
                    if (notificationDetails != null && notificationDetails.size > 0) {
                        lstNotificationDetails = notificationDetails as List<NotificationDetail>
                        Collections.sort<NotificationDetail>(
                            notificationDetails,
                            { o1, o2 -> o1.dateTime?.let { o2.dateTime?.compareTo(it) }!! })
                        var notificationDataList: List<NotificationDetailMgr> =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<NotificationDetailMgr>>(
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(notificationDetails)
                                    .toString(),
                                object : TypeToken<List<NotificationDetailMgr?>?>() {}.type
                            )
                        val notificationHelper = NotificationDataHelper(notificationDataList as MutableList<NotificationDetailMgr>)
                        notificationDataList = notificationHelper.notificationDataInAppFormat as List<NotificationDetailMgr>
                        val adapter =
                            NotificationAdapter(this@NotificationsActivity, notificationDataList)
                        lstActivitiesNotifications.setAdapter(adapter)
                        val headerItemDecoration =
                            HeaderItemDecoration(lstActivitiesNotifications, adapter)
                        lstActivitiesNotifications.addItemDecoration(headerItemDecoration)
                        setUIForNotifications()
                    } else {
                        setUIForNotifications()
                    }
                }


                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    threeDotLoaderBlue.setVisibility(View.GONE)
                }
            })
    }

    private fun setInitialParamsForNotificationAnnouncements() {
        threeDotLoaderBlue.setVisibility(View.VISIBLE)
        lytNoNotifications.setVisibility(View.GONE)
        lstAnnouncementNotifications.setAdapter(null)
        lstNotificationAnnouncements!!.clear()
        pageCount = 0
        totalPageCount = 1
        notificationScrollHelper.updateScrollListener(
            lstAnnouncementNotifications,
            lstNotificationLayoutManager
        )
        callNotificationAnnouncementsDataAPI()
    }

    private fun callNotificationAnnouncementsDataAPI() {
        if (pageCount < totalPageCount) {
            pageCount = pageCount + 1
        } else {
            threeDotLoaderBlue.setVisibility(View.VISIBLE)
            pageCount = 1
            pageSize = 0
        }
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setPageSize(pageSize)
        apiParamsHelper.setPageNumber(pageCount)
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.DESC)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.TIME_PUBLISHED)
        apiParamsHelper.setAnnouncementsStatus(ApiParamsHelper.Status.ACTIVE.value)
        NotificationsApi.retrieveAllAnnouncements(
            this@NotificationsActivity,
            object : NotificationsApi.RetrieveAllAnnouncementsResponseListener {
                override fun onSuccessResponse(notificationAnnouncementsList: RetrieveAllAnnouncementsRes.Response?) {
                    threeDotLoaderBlue.setVisibility(View.GONE)
                    if (notificationAnnouncementsList != null) {
                        totalPageCount = notificationAnnouncementsList.data?.numberOfPages!!
                        val announcements: List<RetrieveSpecificAnnouncementRes.Announcements>? =
                            notificationAnnouncementsList.data!!.announcementsList
                        val fetchedAnnouncements: List<RetrieveSpecificAnnouncementRes.Announcements> =
                            ArrayList<RetrieveSpecificAnnouncementRes.Announcements>(announcements)
                        if (lstNotificationAnnouncements != null && lstNotificationAnnouncements!!.size > 0) {
                            lstNotificationAnnouncements!!.addAll(fetchedAnnouncements)
                        } else {
                            lstNotificationAnnouncements = ArrayList<RetrieveSpecificAnnouncementRes.Announcements>(announcements)
                        }
                        if (lstNotificationAnnouncements != null && lstNotificationAnnouncements!!.size > 0) {
                            setAnnouncementsAdapter()
                            setUIForNotifications()
                        }
                    } else {
                        setUIForNotifications()
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    threeDotLoaderBlue.setVisibility(View.GONE)
                }
            },
            apiParamsHelper
        )
    }

    private fun setAnnouncementsAdapter() {
        if (lstAnnouncementNotifications.adapter == null) {
            notificationAnnouncementsAdapter = NotificationAnnouncementsAdapter(
                lstNotificationAnnouncements,
                this@NotificationsActivity,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        setInitialParamsForNotificationAnnouncements()
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        threeDotLoaderBlue.visibility = View.GONE
                    }
                })
            lstAnnouncementNotifications.adapter = notificationAnnouncementsAdapter
        } else {
            notificationAnnouncementsAdapter =
                lstAnnouncementNotifications.adapter as NotificationAnnouncementsAdapter?
            notificationAnnouncementsAdapter!!.updateDataList(lstNotificationAnnouncements)
            lstAnnouncementNotifications.adapter!!.notifyDataSetChanged()
        }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtNotificationHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnActivitiesTab,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnNewsTab,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoNotifications,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun setUIForNotifications() {
        if (isActivitiesNotificationsSelected) {
            setNotificationsTabActive()
        } else {
            setAnnouncementsTabActive()
        }
    }

    private fun setAnnouncementsTabActive() {
        isActivitiesNotificationsSelected = false
        swipeRefreshLayoutNotificationAnnouncements.setEnabled(true)
        swipeRefreshLayoutNotifications.setEnabled(false)
        lstActivitiesNotifications.setVisibility(View.GONE)
        btnNewsTab?.setTextColor(getResources().getColor(R.color.black))
        txtNewsTabHighlighter?.setVisibility(View.VISIBLE)
        btnActivitiesTab?.setTextColor(getResources().getColor(R.color.grey_medium))
        txtActivitiesTabHighlighter?.setVisibility(View.GONE)
        lytNoNotifications.setVisibility(View.GONE)
        if (lstNotificationAnnouncements != null && lstNotificationAnnouncements!!.size > 0) {
            lstAnnouncementNotifications.setVisibility(View.VISIBLE)
            displayNoNotificationMessage(false)
        } else {
            lstAnnouncementNotifications.setVisibility(View.GONE)
            displayNoNotificationMessage(true)
        }
    }

    private fun setNotificationsTabActive() {
        isActivitiesNotificationsSelected = true
        swipeRefreshLayoutNotifications.setEnabled(true)
        swipeRefreshLayoutNotificationAnnouncements.setEnabled(false)
        btnNewsTab?.setTextColor(getResources().getColor(R.color.grey_medium))
        txtNewsTabHighlighter?.setVisibility(View.GONE)
        btnActivitiesTab?.setTextColor(getResources().getColor(R.color.black))
        txtActivitiesTabHighlighter?.setVisibility(View.VISIBLE)
        lstAnnouncementNotifications.setVisibility(View.GONE)
        lytNoNotifications.setVisibility(View.GONE)
        if (lstNotificationDetails != null && lstNotificationDetails!!.size > 0) {
            lstActivitiesNotifications.setVisibility(View.VISIBLE)
            displayNoNotificationMessage(false)
        } else {
            lstActivitiesNotifications.setVisibility(View.GONE)
            displayNoNotificationMessage(true)
        }
    }

    private fun displayNoNotificationMessage(showNoMessage: Boolean) {
        if (showNoMessage) {
            lytNoNotifications.setVisibility(View.VISIBLE)
            if (isActivitiesNotificationsSelected) {
                txtNoNotifications?.setText(getResources().getString(R.string.txt_no_recent_activities))
            } else {
                txtNoNotifications?.setText(getResources().getString(R.string.txt_no_announcements))
            }
        } else {
            lytNoNotifications.setVisibility(View.GONE)
        }
    }

    override fun onResume() {
        super.onResume()
        notificationBroadCastReceiverHelper = NotificationBroadCastReceiverHelper(
            this@NotificationsActivity,
            object : NotificationBroadCastReceiverHelper.OnNotificationReceivedListner {
                override fun onNotificationReceived(orderId: String?, orderStatus: String?) {
                    callNotificationActivitiesDataAPI()
                }
            })
        //        Register receiver
        LocalBroadcastManager.getInstance(this@NotificationsActivity).registerReceiver(
            mNotifMessageReceiver,
            IntentFilter(NotificationConstants.NOTIFICATION_BROADCAST_INTENT_ANNOUNCEMENT)
        )
    }

    protected override fun onPause() {
        super.onPause()
        //        unregister receiver
        LocalBroadcastManager.getInstance(this@NotificationsActivity)
            .unregisterReceiver(mNotifMessageReceiver)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_right)
    }

    /**
     * register this receiver to know everytime
     */
    private val mNotifMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setInitialParamsForNotificationAnnouncements()
        }
    }
}