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
import zeemart.asia.buyers.models.orderimport.OrdersWithPagination

/**
 * Created by ParulBhandari on 12/8/2017.
 */
class RetrieveOrders constructor(
    private val context: Context?,
    private val getOrderDataListener: GetOrderDataListener,
    outlets: List<Outlet>,
    apiParamsHelper: ApiParamsHelper
) {
    var outlets: List<Outlet>
    var apiParamsHelper: ApiParamsHelper

    init {
        this.outlets = outlets
        this.apiParamsHelper = apiParamsHelper
    }

    // api id - 20.7 Retrieve Orders V1
    val orders: Unit
        get() {
            val outletIds: Array<String?> = arrayOfNulls(outlets.size)
            for (i in outlets.indices) {
                outletIds[i] = outlets.get(i).outletId
            }
            apiParamsHelper.setOutletIds(outletIds as Array<String>)
            apiParamsHelper.setOnlyInvoices(false)
            var Uri: String? = ServiceConstant.ENDPOINT_ORDERS
            Uri = apiParamsHelper.getUrl(Uri)
            val getOrdersRequest: ZeemartAPIRequest = ZeemartAPIRequest(
                context,
                Request.Method.GET,
                Uri,
                null,
                CommonMethods.getHeaderFromOutlets(outlets),
                object : Response.Listener<String> {
                    public override fun onResponse(response: String?) {
                        val allOrders: OrdersWithPagination? =
                            ZeemartBuyerApp.fromJson(response, OrdersWithPagination::class.java)
                        getOrderDataListener.onSuccessResponse(allOrders)
                    }
                },
                object : Response.ErrorListener {
                    public override fun onErrorResponse(error: VolleyError) {
                        getOrderDataListener.onErrorResponse(error)
                    }
                })
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(getOrdersRequest)
        }

    open interface GetOrderDataListener {
        fun onErrorResponse(error: VolleyError?)
        fun onSuccessResponse(ordersWithPagination: OrdersWithPagination?)
    }
}