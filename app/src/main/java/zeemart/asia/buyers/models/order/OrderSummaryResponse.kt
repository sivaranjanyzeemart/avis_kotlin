package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OrderSummaryResponse {
    @SerializedName("data")
    @Expose
    var data: OrderSummary? = null
}