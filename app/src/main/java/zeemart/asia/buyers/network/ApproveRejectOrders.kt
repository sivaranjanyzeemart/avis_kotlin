package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimport.RejectRequestModel

/**
 * Created by ParulBhandari on 1/8/2018.
 */
object ApproveRejectOrders {
    //   api id - 20.4 Reject Order
    fun RejectOrder(
        context: Context?,
        order: Orders,
        remarks: String?,
        listener: GetResponseStatusListener
    ) {
        val userDetail: UserDetails = SharedPref.currentUserDetail
        val rejectRequestModel: RejectRequestModel = RejectRequestModel()
        rejectRequestModel.orderId
        if (remarks != null) rejectRequestModel.rejectedRemark
        val jsonRequestBody: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(rejectRequestModel)
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(order.outlet?.outletId!!)
        var url: String? = ServiceConstant.ENDPOINT_REJECT_ORDER
        url = apiParamsHelper.getUrl(url)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        order.outlet?.let { outlets.add(it) }
        val editOrderResponse: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            url,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    listener.onSuccessResponse(response)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(editOrderResponse)
    }

    //   api id - 20.3 Approve Order
    fun ApproveOrder(context: Context?, order: Orders, listener: GetResponseStatusListener) {
        val userDetail: UserDetails = SharedPref.currentUserDetail
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(order.outlet?.outletId!!)
        apiParamsHelper.setOrderId(order.orderId!!)
        var url: String? = ServiceConstant.ENDPOINT_APPROVE_ORDER
        url = apiParamsHelper.getUrl(url)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        order.outlet?.let { outlets.add(it) }
        val editOrderResponse: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    listener.onSuccessResponse(response)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(editOrderResponse)
    }

    open interface GetResponseStatusListener {
        fun onSuccessResponse(status: String?)
        fun onErrorResponse(error: VolleyError?)
    }
}