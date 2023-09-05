package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.marketlist.Product

/**
 * Created by ParulBhandari on 12/17/2017.
 */
class CreateOrder {
    @SerializedName("outletId")
    @Expose
    var outletId: String? = null

    @SerializedName("supplierId")
    @Expose
    var supplierId: String? = null

    @SerializedName("timeDelivered")
    @Expose
    var timeDelivered: Long? = null

    @SerializedName("products")
    @Expose
    var products: List<Product>? = null

    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("deliveryInstruction")
    @Expose
    var deliveryInstruction: String? = null

    @SerializedName("promoCode")
    @Expose
    var promoCode: String? = null
}