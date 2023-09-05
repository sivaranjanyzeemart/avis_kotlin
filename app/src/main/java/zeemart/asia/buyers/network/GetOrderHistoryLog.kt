package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.orderimportimport.OrderHistoryModelBaseResponse

/**
 * Created by ParulBhandari on 12/22/2017.
 */
class GetOrderHistoryLog constructor(
    private val context: Context,
    private val getOrderHistoryDataListener: GetOrderHistoryDataListener
) {
    //     api id - 25.1 Get Order Activities
    fun getOrderHistoryLogData(orderId: Array<String?>, outletId: String) {
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOrderId(orderId.get(0)!!)
        apiParamsHelper.setIncludeOrderChanges(false)
        val endpointUrl: String = ServiceConstant.ENDPOINT_ACTIVITY_LOG
        val outlet: Outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val getOrderDetailsData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(endpointUrl),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val baseResponse: OrderHistoryModelBaseResponse =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response,
                            OrderHistoryModelBaseResponse::class.java
                        )
                    getOrderHistoryDataListener.onSuccessResponse(baseResponse.data)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    getOrderHistoryDataListener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getOrderDetailsData)
    }

    /**
     * Interface for returning the network response to the activity
     */
    open interface GetOrderHistoryDataListener {
        fun onSuccessResponse(orderHistoryData: List<OrderHistoryModelBaseResponse.OrderHistoryModel?>?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }
}