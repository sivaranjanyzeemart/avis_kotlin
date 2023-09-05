package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getDoubleToString

class InventorySaveStockRequest {
    @SerializedName("shelve")
    @Expose
    var shelve: Shelve? = null

    @SerializedName("products")
    @Expose
    var products: List<Product>? = null

    @SerializedName("countDayIncomingIncluded")
    @Expose
    var countDayIncomingIncluded: Boolean? = null

    @SerializedName("countDate")
    @Expose
    var countDate: Long? = null

    @SerializedName("stockageId")
    @Expose
    var stockageId: String? = null

    @SerializedName("stockageStatus")
    @Expose
    var stockageStatus: String? = null

    class Product {
        @SerializedName("productId")
        @Expose
        var productId: String? = null

        @SerializedName("stockQuantity")
        @Expose
        var stockQuantity: Double? = null
        fun displayStockQuantityValue(): String {
            return getDoubleToString(stockQuantity)
        }
    }
}