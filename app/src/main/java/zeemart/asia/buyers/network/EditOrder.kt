package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.orderimportimport.DraftErrorModel

/**
 * Created by ParulBhandari on 1/8/2018.
 */
class EditOrder(
    private val context: Context,
    private val getRequestStatusResponseListener: GetResponseStatusListener
) {
    //   api id - 20.2 Edit and place Draft Order
    fun editOrderData(jsonRequestBody: String?, outletId: String) {
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outlet.outletId!!)
        var url: String = ServiceConstant.ENDPOINT_ORDERS_DRAFT_EDIT_PLACE
        url = apiParamsHelper.getUrl(url)
        val editOrderResponse = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            url,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            Response.Listener<String> { response ->
                getRequestStatusResponseListener.onSuccessResponse(response)
            },
            Response.ErrorListener { },
            object : ZeemartAPIRequest.DraftErrorListener {
                 override fun errorListener(draftErrorModel: DraftErrorModel?, error: VolleyError?) {
                    getRequestStatusResponseListener.onErrorDraftResponse(draftErrorModel, error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(editOrderResponse)
    }

    //   api id - 20.2 Edit  and Approve Order
    fun editApproveOrderData(jsonRequestBody: String?, outletId: String, supplierId: String?) {
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId)
        apiParamsHelper.setSupplierId(supplierId!!)
        var url: String = ServiceConstant.ENDPOINT_EDIT_APPROVE_ORDER
        url = apiParamsHelper.getUrl(url)
        val editApproveOrderResponse = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            url,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            Response.Listener<String> { response ->
                getRequestStatusResponseListener.onSuccessResponse(response)
            },
            Response.ErrorListener { },
            object : ZeemartAPIRequest.DraftErrorListener {
                override fun errorListener(draftErrorModel: DraftErrorModel?, error: VolleyError?) {
                    getRequestStatusResponseListener.onErrorDraftResponse(draftErrorModel, error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(editApproveOrderResponse)
    }

    interface GetResponseStatusListener {
        fun onSuccessResponse(status: String?)
        fun onErrorDraftResponse(draftError: DraftErrorModel?, error: VolleyError?)
    }
}