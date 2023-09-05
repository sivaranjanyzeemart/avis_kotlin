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
import zeemart.asia.buyers.models.orderimport.RetrieveOrderDetails

/**
 * Created by ParulBhandari on 12/22/2017.
 */
class GetOrderDetails constructor(
    private val context: Context,
    private val getOrderDataListener: GetOrderDetailedDataListener
) {
    //   api id - 20.12 Retrieve order by Id
    fun getOrderDetailsData(orderId: String?, outletId: String) {
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId)
        apiParamsHelper.setOrderId(orderId!!)
        apiParamsHelper.setOrderIncludeFields(arrayOf<ApiParamsHelper.IncludeFields>(ApiParamsHelper.IncludeFields.DELIVERY_INSTRUCTION))
        var endpointUrl: String? = ServiceConstant.ENDPOINT_RETRIEVE_ORDER_DETAILS
        endpointUrl = apiParamsHelper.getUrl(endpointUrl)
        val outlet: Outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val getOrderDetailsData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            endpointUrl,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val retrieveOrderDetails: RetrieveOrderDetails? =
                        ZeemartBuyerApp.fromJson(response, RetrieveOrderDetails::class.java)
                    getOrderDataListener.onSuccessResponse(retrieveOrderDetails)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    getOrderDataListener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getOrderDetailsData)
    }

    open interface GetOrderDetailedDataListener {
        fun onErrorResponse(error: VolleyError?)
        fun onSuccessResponse(ordersList: RetrieveOrderDetails?)
    }
}