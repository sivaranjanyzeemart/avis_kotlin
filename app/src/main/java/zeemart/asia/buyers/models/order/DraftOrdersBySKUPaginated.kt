package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.order.Orders

class DraftOrdersBySKUPaginated {
    @SerializedName("data")
    @Expose
    var data: List<DraftOrdersBySKU>? = null

    class DraftOrdersBySKU {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("data")
        @Expose
        lateinit var orders: Orders

        @SerializedName("errors")
        @Expose
        var errors: List<ErrorModel>? = null
    }
}