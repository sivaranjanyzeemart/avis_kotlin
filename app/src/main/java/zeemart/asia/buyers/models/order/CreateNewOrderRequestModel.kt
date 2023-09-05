package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * request model for creating deal and essentials order
 */
class CreateNewOrderRequestModel {
    @SerializedName("timeDelivered")
    @Expose
    var timeDelivered: Long? = null

    @SerializedName("products")
    @Expose
    var products: List<CreateNewOrderRequestModel.Product>? = null

    @SerializedName("deliveryInstruction")
    @Expose
    var deliveryInstruction: String? = null

    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("promoCode")
    @Expose
    var promoCode: String? = null

    inner class Product {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("notes")
        @Expose
        var notes: String? = null
    }
}