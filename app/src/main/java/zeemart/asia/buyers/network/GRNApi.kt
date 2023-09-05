package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.models.Outlet

/**
 * Created by RajPrudhviMarella on 14/June/2021.
 */
object GRNApi {
    // api id - 21.1 Create draft orders based on the SKUs - (for Outlet)
    fun saveGRN(
        context: Context?,
        requestBody: String?,
        orderId: String?,
        outlet: Outlet,
        saveGRNResponseListener: saveGRNResponseListener
    ) {
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOrderId(orderId!!)
        var url = ServiceConstant.ENDPOINT_SAVE_GRN
        url = apiParamsHelper.getUrl(url)
        val getNotificationRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            url,
            requestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response -> saveGRNResponseListener.onSuccessResponse(response) }) { error ->
            saveGRNResponseListener.onErrorResponse(
                error
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    fun edtGrn(
        context: Context?,
        requestBody: String?,
        grnId: String?,
        outlet: Outlet,
        saveGRNResponseListener: saveGRNResponseListener
    ) {
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setGrnId(grnId!!)
        var url = ServiceConstant.ENDPOINT_SAVE_GRN
        url = apiParamsHelper.getUrl(url)
        val getNotificationRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            url,
            requestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response -> saveGRNResponseListener.onSuccessResponse(response) }) { error ->
            saveGRNResponseListener.onErrorResponse(
                error
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    // api id - 21.1 Create draft orders based on the SKUs - (for Outlet)
    fun deleteGrn(
        context: Context?,
        grnId: String?,
        outlet: Outlet,
        saveGRNResponseListener: saveGRNResponseListener
    ) {
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setGrnId(grnId!!)
        var url = ServiceConstant.ENDPOINT_DELETE_GRN
        url = apiParamsHelper.getUrl(url)
        val getNotificationRequest = ZeemartAPIRequest(
            context,
            Request.Method.DELETE,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response -> saveGRNResponseListener.onSuccessResponse(response) }) { error ->
            saveGRNResponseListener.onErrorResponse(
                error
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    interface saveGRNResponseListener {
        fun onSuccessResponse(response: String?)
        fun onErrorResponse(error: VolleyError?)
    }
}