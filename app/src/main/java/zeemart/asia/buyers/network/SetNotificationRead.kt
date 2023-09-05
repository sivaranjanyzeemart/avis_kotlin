package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.*
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.NotificationRead

/**
 * Created by ParulBhandari on 11/27/2017.
 */
class SetNotificationRead constructor(
    private val context: Context,
    private val setNotificationReadResponseListener: GetRequestStatusResponseListener,
    private val notificationIds: ArrayList<NotificationRead>
) {
    //  9.2 Mark Notification(s) Read/Unread(refactor into single function)
    fun setNotificationRead() {
        val getNotificationRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            ServiceConstant.ENDPOINT_NOTIFICATIONS,
            requestBody(),
            CommonMethods.headerAllOutlets,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    setNotificationReadResponseListener.onSuccessResponse(response)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    setNotificationReadResponseListener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    private fun requestBody(): String {
        val json: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(notificationIds)
        return json
    }
}