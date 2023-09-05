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
class CancelOrder(
    private val context: Context,
    private val getRequestStatusResponseListener: GetResponseStatusListener
) {
    //   api id - 23.17 Cancel Order
    fun cancelOrderData(jsonRequestBody: String?, outletId: String?) {
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId!!)
        var url = ServiceConstant.ENDPOINT_CANCEL_ORDER
        url = apiParamsHelper.getUrl(url)
        val deleteOrderResponse = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            url,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
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