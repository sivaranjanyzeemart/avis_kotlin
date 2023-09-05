package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.order.Orders

class RepeatOrderWithPagination {
    @SerializedName("data")
    @Expose
    var data: List<RepeatOrders>? = null

    inner class RepeatOrders {
        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("orders")
        @Expose
        var orders: List<Orders>? = null
    }
}