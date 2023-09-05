package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.interfaces.OutletSpendingHistoryDataListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.reportsimportimport.ReportSpendingDataModel

/**
 * Created by ParulBhandari on 2/8/2018.
 */
class GetOutletSpendingHistoryData constructor(
    private val context: Context,
    listener: OutletSpendingHistoryDataListener
) {
    private val outletSpendingHistoryDataListener: OutletSpendingHistoryDataListener

    init {
        outletSpendingHistoryDataListener = listener
    }

    // api id - 33.1 Outlet Spending History data
    fun getOutletSpendingData(
        startDate: Long,
        endDate: Long,
        isDetailData: Boolean,
        outlet: Outlet
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val endpointUrl: String =
            ServiceConstant.ENDPOINT_REPORT_SPENDING_DEPRECATED + "?" + DETAILED_DATA + "=" + isDetailData + "&" + START_DATE + "=" + startDate + "&" + END_DATE + "=" + endDate
        val getOrderDetailsData: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            endpointUrl,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val spendingHistoryData: ReportSpendingDataModel =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            response.toString(),
                            ReportSpendingDataModel::class.java
                        )
                    outletSpendingHistoryDataListener.onSuccessResponse(spendingHistoryData)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    outletSpendingHistoryDataListener.onErrorResponse(error as VolleyErrorHelper?)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getOrderDetailsData)
    }

    companion object {
        private val DETAILED_DATA: String = "detailedData"
        private val START_DATE: String = "startDate"
        private val END_DATE: String = "endDate"
    }
}