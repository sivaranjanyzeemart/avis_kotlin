package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.NotificationDetail
import zeemart.asia.buyers.models.NotificationRead
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.reportsimportimport.MarkAnnouncementReadReq
import zeemart.asia.buyers.modelsimport.RetrieveAllAnnouncementsRes
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes

/**
 * Created by ParulBhandari on 11/23/2017.
 */
object NotificationsApi {
    //   api id - 9.1 Retrieve Notifications
    @JvmStatic
    fun retrieveNotifications(context: Context?, listener: RetrieveNotificationResponseListener) {
        val getNotificationRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            ServiceConstant.ENDPOINT_NOTIFICATIONS,
            null,
            CommonMethods.headerAllOutlets,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
//                List<NotificationDetail> notificationDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(response.toString(), new TypeToken<List<NotificationDetail>>() {
//                }.getType());
                    val notificationDetails: List<NotificationDetail?>? =
                        ZeemartBuyerApp.fromJsonList<NotificationDetail>(
                            response,
                            NotificationDetail::class.java,
                            object : TypeToken<List<NotificationDetail?>?>() {}.getType()
                        )
                    listener.onSuccessResponse(notificationDetails)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    //   api id - 9.2 Mark Notification(s) Read/Unread
    fun markNotification(
        context: Context?,
        notificationIds: ArrayList<NotificationRead?>?,
        listener: GetRequestStatusResponseListener
    ) {
        val json: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(notificationIds)
        val getNotificationRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            ServiceConstant.ENDPOINT_NOTIFICATIONS,
            json,
            CommonMethods.headerAllOutlets,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    listener.onSuccessResponse(response)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    //   api id - 9.4 Retrieve specific announcement
    @JvmStatic
    fun retrieveSpecificAnnouncement(
        context: Context?,
        outletId: String,
        announcementId: String?,
        listner: RetrieveSpecificAnnouncementResponseListener
    ) {
        var endpointUrl: String? = ServiceConstant.ENDPOINT_NOTIFICATIONS_ANNOUNCEMENTS_DETAILS
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setAnnouncementsId(announcementId!!)
        endpointUrl = apiParamsHelper.getUrl(endpointUrl)
        val outlet: Outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val getNotificationAnnouncementData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            endpointUrl,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val notificationAnnouncements: RetrieveSpecificAnnouncementRes =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response.toString(),
                            object : TypeToken<RetrieveSpecificAnnouncementRes?>() {}.getType()
                        )
                    listner.onSuccessResponse(notificationAnnouncements)
                }

            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listner.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationAnnouncementData)
    }

    //   api id - 9.5 Retrieve all announcements
    @JvmStatic
    fun retrieveAllAnnouncements(
        context: Context?,
        listener: RetrieveAllAnnouncementsResponseListener,
        apiParamsHelper: ApiParamsHelper
    ) {
        var endpointUrl: String? = ServiceConstant.ENDPOINT_NOTIFICATIONS_ANNOUNCEMENTS
        endpointUrl = apiParamsHelper.getUrl(endpointUrl)
        val getNotificationAnnouncementData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            endpointUrl,
            null,
            CommonMethods.headerAllOutlets,
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val notificationAnnouncements: RetrieveAllAnnouncementsRes.Response =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response.toString(),
                            object : TypeToken<RetrieveAllAnnouncementsRes.Response?>() {}.getType()
                        )
                    listener.onSuccessResponse(notificationAnnouncements)
                }

            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationAnnouncementData)
    }

    //   api id - 9.7 Mark read/unread announcement
    fun markNotificationAnnouncement(
        context: Context?,
        announcementIds: MarkAnnouncementReadReq?,
        listener: GetRequestStatusResponseListener
    ) {
        val json: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(announcementIds)
        val getNotificationRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            ServiceConstant.ENDPOINT_NOTIFICATIONS_ANNOUNCEMENTS_READ,
            json,
            CommonMethods.headerAllOutlets,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    listener.onSuccessResponse(response)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    open interface RetrieveNotificationResponseListener {
        fun onSuccessResponse(notificationDetails: List<NotificationDetail?>?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    open interface RetrieveAllAnnouncementsResponseListener {
        fun onSuccessResponse(retrieveAllAnnouncementsRes: RetrieveAllAnnouncementsRes.Response?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    open interface RetrieveSpecificAnnouncementResponseListener {
        fun onSuccessResponse(notificationAnnouncementsDetails: RetrieveSpecificAnnouncementRes?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }
}