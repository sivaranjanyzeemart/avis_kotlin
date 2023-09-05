package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.interfaces.PriceUpdateDataListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse

/**
 * Created by ParulBhandari on 2/8/2018.
 */
class GetPriceUpdateData constructor(
    private val context: Context,
    listener: PriceUpdateDataListener
) {
    private val priceUpdateDataListener: PriceUpdateDataListener

    init {
        priceUpdateDataListener = listener
    }

    // api id - 34.1 Retrieve all price updates (Buyer Admin)
    fun getOutletPriceUpdateData(apiParamsHelper: ApiParamsHelper, outletId: String) {
        val outlet: Outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val outletIds: Array<String?> = arrayOfNulls(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId!!
        }
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        val getOrderDetailsData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_REPORT_REPORT_PRICE_CHANGES),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                 override fun onResponse(response: String?) {
                    val priceUpdateModelBaseResponse: PriceUpdateModelBaseResponse? =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response,
                            PriceUpdateModelBaseResponse::class.java
                        )
                    if (priceUpdateModelBaseResponse != null && priceUpdateModelBaseResponse.data != null) {
                        val priceUpdateDataList: MutableList<PriceUpdateModelBaseResponse.PriceDetailModel> =
                            priceUpdateModelBaseResponse.data!!
                        priceUpdateDataListener.onSuccessResponse(priceUpdateDataList)
                    }
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    priceUpdateDataListener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getOrderDetailsData)
    }
}