package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.NewOrderResponseListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.orderimportimport.CreateNewOrderResponseModel

/**
 * Created by ParulBhandari on 12/17/2017.
 */
class CreateNewOrder(private val context: Context, listener: NewOrderResponseListener) {
    private val newOrderResponseListener: NewOrderResponseListener

    init {
        newOrderResponseListener = listener
    }

    //   api id - 20.1 Create Order
    fun pushNewOrderData(
        jsonRequestBody: String,
        outlet: Outlet,
        supplier: Supplier,
        outlets: List<Outlet>?
    ) {
        Log.d(
            "Create New Order BODY",
            jsonRequestBody + "***" + SharedPref.read(SharedPref.ACCESS_TOKEN, "")
        )
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outlet.outletId!!)
        apiParamsHelper.setSupplierId(supplier.supplierId!!)
        var url: String = ServiceConstant.ENDPOINT_ORDER_SINGLE_ORDER
        url = apiParamsHelper.getUrl(url)
        val getCreateNewOrderResponse = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            url,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val responseData: CreateNewOrderResponseModel =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response, CreateNewOrderResponseModel::class.java
                    )
                newOrderResponseListener.onSuccessResponse(responseData)
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    newOrderResponseListener.onErrorResponse(error as VolleyErrorHelper)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getCreateNewOrderResponse)
    }

    //   api id - 20.7 Create Draft Order
    fun pushNewDraftOrderData(
        jsonRequestBody: String,
        outlet: Outlet,
        supplier: Supplier,
        outlets: List<Outlet>?
    ) {
        Log.d(
            "Create Draft Order BODY",
            jsonRequestBody + "***" + SharedPref.read(SharedPref.ACCESS_TOKEN, "")
        )
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outlet.outletId!!)
        apiParamsHelper.setSupplierId(supplier.supplierId!!)
        var url: String = ServiceConstant.ENDPOINT_ORDERS_DRAFT
        url = apiParamsHelper.getUrl(url)
        val getCreateNewDraftOrderResponse = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            url,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val responseData: CreateNewOrderResponseModel =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response, CreateNewOrderResponseModel::class.java
                    )
                newOrderResponseListener.onSuccessResponse(responseData)
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    newOrderResponseListener.onErrorResponse(error as VolleyErrorHelper)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getCreateNewDraftOrderResponse)
    }
}