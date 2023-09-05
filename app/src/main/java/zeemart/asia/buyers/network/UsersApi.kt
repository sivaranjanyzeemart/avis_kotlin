package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.*
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.ViewSpecificUser

object UsersApi {
    //   api id - 8.2 View Specific User
    @JvmStatic
    fun viewSpecificUser(context: Context?, userId: String?, listener: ViewSpecificUserListener) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setUserId(userId!!)
        val uri = apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_USER_PROFILE)
        val request = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.headerAllOutlets,
            { response ->
                val userProfileData = fromJson(response, ViewSpecificUser::class.java)
                listener.onSuccessResponse(userProfileData)
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(request)
    }

    //   api id - 8.5 Edit User Details
    @JvmStatic
    fun editSingleUser(
        context: Context?,
        userDetails: UserDetails,
        listener: GetRequestStatusResponseListener
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setUserId(userDetails.id!!)
        val uri = ServiceConstant.ENDPOINT_USER_PROFILE
        val requestBody = ZeemartBuyerApp.gsonExposeExclusive.toJson(userDetails)
        val request = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            apiParamsHelper.getUrl(uri),
            requestBody,
            CommonMethods.headerAllOutlets,
            { response -> listener.onSuccessResponse(response) }) { error ->
            listener.onErrorResponse(
                error as VolleyErrorHelper
            )
        }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(request)
    }

    interface ViewSpecificUserListener {
        fun onSuccessResponse(user: ViewSpecificUser?)
        fun onErrorResponse(error: VolleyErrorHelper)
    }
}