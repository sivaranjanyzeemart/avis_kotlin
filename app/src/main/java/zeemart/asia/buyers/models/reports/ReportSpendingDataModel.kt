package zeemart.asia.buyers.models.reportsimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.reports.SpendingModel

/**
 * Created by ParulBhandari on 2/9/2018.
 */
class ReportSpendingDataModel {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("spendings")
    @Expose
    var spendings: List<SpendingModel>? = null
}