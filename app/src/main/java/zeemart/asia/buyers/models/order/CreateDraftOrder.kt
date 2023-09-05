package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CreateDraftOrder {
    @SerializedName("outletId")
    @Expose
    var outletId: String? = null

    @SerializedName("products")
    @Expose
    var products: List<CreateDraftOrder.Product>? = null
    override fun toString(): String {
        return "CreateDraftOrder{" +
                "outletId='" + outletId + '\'' +
                ", products=" + products +
                '}'
    }

    class Product {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity = 0.0

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null
        override fun toString(): String {
            return "Product{" +
                    "sku='" + sku + '\'' +
                    ", quantity=" + quantity +
                    ", unitSize='" + unitSize + '\'' +
                    '}'
        }
    }
}