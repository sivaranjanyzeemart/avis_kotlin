package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.PushNotification

object PushNotificationApi {
    //   api id - 10.2 Register user (Buyer)
    fun registerDeviceUserLoggedIn(context: Context?, deviceToken: String) {
        val reqBody: String = generateJSONBodyRegisterUser(deviceToken)
        val requestHeader: MutableMap<String, String> = CommonMethods.headerAllOutlets as MutableMap<String, String>
        requestHeader.put(ServiceConstant.USER_ID, SharedPref.read(SharedPref.USER_ID, "")!!)
        val getOrderDetailsData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_NOTIFICATION_REGISTER_DEVICE,
            reqBody,
            requestHeader,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {}
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {}
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getOrderDetailsData)
    }

    private fun generateJSONBodyRegisterUser(deviceToken: String): String {
        val pushNotification: PushNotification =
            PushNotification(deviceToken, SharedPref.read(SharedPref.USER_ID, ""))
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(pushNotification)
    }

    //   api id - 10.1 Register Device (Buyer)
    fun registerDeviceUserNotLoggedIn(context: Context?, deviceToken: String) {
        val reqBody: String = generateJSONBodyRegisterDevice(deviceToken)
        val getOrderDetailsData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_NOTIFICATION_APP_LAUNCH_NO_LOGIN,
            reqBody,
            null,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {}
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {}
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getOrderDetailsData)
    }

    private fun generateJSONBodyRegisterDevice(deviceToken: String): String {
        val pushNotification: PushNotification = PushNotification(deviceToken)
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(pushNotification)
    }

    //   api id - 10.3 Un-Register user (Buyer)
    @JvmStatic
    fun registerDeviceUserLogsOut(
        context: Context?,
        deviceToken: String,
        userId: String,
        listener: GetRequestStatusResponseListener
    ) {
        val reqBody: String = generateJSONBodyUnregisterUser(deviceToken)
        val requestHeader: MutableMap<String, String> = CommonMethods.headerAllOutlets as MutableMap<String, String>
        requestHeader.put(ServiceConstant.USER_ID, userId)
        val getOrderDetailsData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            ServiceConstant.ENDPOINT_NOTIFICATION_REGISTER_DEVICE,
            reqBody,
            requestHeader,
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
            .addToRequestQueue<String>(getOrderDetailsData)
    }

    private fun generateJSONBodyUnregisterUser(deviceToken: String): String {
        val pushNotification: PushNotification = PushNotification(deviceToken)
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(pushNotification)
    }
}