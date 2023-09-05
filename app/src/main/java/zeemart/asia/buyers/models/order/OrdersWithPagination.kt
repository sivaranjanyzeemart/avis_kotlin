package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.Pagination
import zeemart.asia.buyers.models.order.Orders

class OrdersWithPagination : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: withPaginationData? = null

    inner class withPaginationData : Pagination() {
        @SerializedName("data")
        @Expose
        var orders: List<Orders>? = null
    }
}