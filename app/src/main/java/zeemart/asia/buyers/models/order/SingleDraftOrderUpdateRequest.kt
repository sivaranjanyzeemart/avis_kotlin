package zeemart.asia.buyers.models.order

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.models.Outlet

class SingleDraftOrderUpdateRequest {
    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null

    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("timeDelivered")
    @Expose
    var timeDelivered: Int? = null

    @SerializedName("promoCode")
    @Expose
    var promoCode: String? = null

    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("products")
    @Expose
    var products: List<Product>? = null

    class Product {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity = 0.0
    }

    companion object {
        @JvmStatic
        fun getJsonRequestFromOrder(order: Orders?): String {
            if (order != null) {
                val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order)
                val singleDraftOrderUpdateRequest =
                    fromJson(
                        orderJson,
                        SingleDraftOrderUpdateRequest::class.java
                    )
                return ZeemartBuyerApp.gsonExposeExclusive.toJson(singleDraftOrderUpdateRequest)
            }
            return ""
        }
    }
}