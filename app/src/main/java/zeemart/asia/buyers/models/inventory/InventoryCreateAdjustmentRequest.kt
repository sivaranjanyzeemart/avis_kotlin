package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getDoubleToString


class InventoryCreateAdjustmentRequest {
    @SerializedName("shelve")
    @Expose
    var shelve: Shelve? = null

    @SerializedName("products")
    @Expose
    var products: List<Product>? = null

    @SerializedName("countDayIncomingIncluded")
    @Expose
    var countDayIncomingIncluded: Boolean? = null

    @SerializedName("stockageReason")
    @Expose
    var stockageReason: String? = null

    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("selectedOutlet")
    @Expose
    var selectedOutlet: SelectedOutlet? = null

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

    class SelectedOutlet {
        @SerializedName("outletId")
        @Expose
        var outletId: String? = null

        @SerializedName("outletName")
        @Expose
        var outletName: String? = null
    }
}