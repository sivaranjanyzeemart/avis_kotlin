package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.models.Outlet

/**
 * Created by Muthumari on 19/03/2020.
 */
class DeleteOrder(
    private val context: Context,
    private val getRequestStatusResponseListener: GetResponseStatusListener
) {
    //   api id - 23.19 Delete Order
    fun deleteOrderData(orderId: String?, outletId: String?) {
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId!!)
        apiParamsHelper.setOrderId(orderId!!)
        var url = ServiceConstant.ENDPOINT_ORDER_SINGLE_ORDER
        url = apiParamsHelper.getUrl(url)
        val deleteOrderResponse = ZeemartAPIRequest(
            context,
            Request.Method.DELETE,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response -> getRequestStatusResponseListener.onSuccessResponse(response) }) { error ->
            getRequestStatusResponseListener.onErrorResponse(
                error
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(deleteOrderResponse)
    }

    interface GetResponseStatusListener {
        fun onSuccessResponse(status: String?)
        fun onErrorResponse(error: VolleyError?)
    }
}