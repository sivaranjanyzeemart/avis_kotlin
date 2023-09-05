package zeemart.asia.buyers.models.orderimportimport


import com.google.gson.annotations.Expose
import zeemart.asia.buyers.models.UserDetails

class ReceiveOrderRequest {
    @Expose
    var orderId: String? = null

    @Expose
    var deliveryStatus: String? = null

    @Expose
    var receivedBy: UserDetails? = null
}