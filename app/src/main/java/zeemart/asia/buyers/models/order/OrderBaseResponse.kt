package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimport.ErrorModel


class OrderBaseResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: OrderBaseResponse.Data? = null

    class Data {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("data")
        @Expose
        var data: Orders? = null

        @SerializedName("errors")
        @Expose
        var errors: List<ErrorModel>? = null
    }
}