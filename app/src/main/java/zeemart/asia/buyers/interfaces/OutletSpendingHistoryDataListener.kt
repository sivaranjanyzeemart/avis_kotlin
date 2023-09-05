package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.reportsimportimport.ReportSpendingDataModel
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 2/9/2018.
 */
interface OutletSpendingHistoryDataListener {
    fun onSuccessResponse(spendingDataModel: ReportSpendingDataModel?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}