package zeemart.asia.buyers.models.paymentimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.PriceDetails

class PayOrderRequest {
    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("cardReferenceNumber")
    @Expose
    var cardReferenceNumber: String? = null

    @SerializedName("usedCredit")
    @Expose
    var usedCredit: PriceDetails? = null
}