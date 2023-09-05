package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.OutletFuturesModel
import zeemart.asia.buyers.models.SpecificOutlet

object OutletsApi {
    //   api id - 6.3 View Specific Outlet
    fun getSpecificOutlet(context: Context?, listener: GetSpecificOutletResponseListener) {
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet!!.outletId!!)
        val uri = ServiceConstant.ENDPOINT_SPECIFIC_OUTLET
        val zeemartAPIRequest =
            ZeemartAPIRequest(context, Request.Method.GET, apiParamsHelper.getUrl(uri), null,
                CommonMethods.getHeaderFromOutlets(outlets), { response ->
                    if (response != null) {
                        val specificOutlet = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response,
                            SpecificOutlet::class.java
                        )
                        listener.onSuccessResponse(specificOutlet!!)
                    }
                }) { error -> listener.onError(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 6.3 View Specific Outlet
    @JvmStatic
    fun getSpecificOutlet(
        context: Context?,
        outlet: Outlet,
        listener: GetSpecificOutletResponseListener
    ) {
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outlet.outletId!!)
        val uri = ServiceConstant.ENDPOINT_SPECIFIC_OUTLET
        val zeemartAPIRequest =
            ZeemartAPIRequest(context, Request.Method.GET, apiParamsHelper.getUrl(uri), null,
                CommonMethods.getHeaderFromOutlets(outlets), { response ->
                    if (response != null) {
                        val specificOutlet = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response,
                            SpecificOutlet::class.java
                        )
                        listener.onSuccessResponse(specificOutlet)
                    }
                }) { error -> listener.onError(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun getOutletFuturesMap(context: Context?, listener: OutletFuturesResponseListener) {
        val stringRequest =
            StringRequest(Request.Method.GET, ServiceConstant.ENDPOINT_OUTLET_FUTURES, { response ->
                Log.e("response", "onResponse: $response")
                if (response != null) {
                    val outletFuturesModel = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        OutletFuturesModel::class.java
                    )
                    listener.onSuccessResponse(outletFuturesModel)
                }
            }) { error -> listener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(stringRequest)
    }

    //   api id - 6.3 View Specific Outlet
    @JvmStatic
    fun getSpecificOutletDetail(
        context: Context?,
        outletId: String?,
        listener: GetSpecificOutletResponseListener
    ) {
        val outlets = SharedPref.allOutlets
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId!!)
        val uri = ServiceConstant.ENDPOINT_SPECIFIC_OUTLET
        val zeemartAPIRequest =
            ZeemartAPIRequest(context, Request.Method.GET, apiParamsHelper.getUrl(uri), null,
                CommonMethods.getHeaderFromOutlets(outlets), { response ->
                    if (response != null) {
                        val specificOutlet = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response,
                            SpecificOutlet::class.java
                        )
                        listener.onSuccessResponse(specificOutlet)
                    }
                }) { error -> listener.onError(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    interface GetSpecificOutletResponseListener {
        fun onSuccessResponse(specificOutlet: SpecificOutlet?)
        fun onError(error: VolleyErrorHelper?)
    }

    interface OutletFuturesResponseListener {
        fun onSuccessResponse(outletFuturesModel: OutletFuturesModel?)
        fun onErrorResponse(error: VolleyError?)
    }
}