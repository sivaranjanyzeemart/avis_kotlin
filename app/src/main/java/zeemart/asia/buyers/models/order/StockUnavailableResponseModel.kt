package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 4/27/2018.
 */
class StockUnavailableResponseModel {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("insufficientProducts")
    @Expose
    var insufficientProducts: List<InsufficientProduct>? = null

    inner class InsufficientProduct {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("stocks")
        @Expose
        var stocks: List<Stock>? = null

        @SerializedName("availableStockQty")
        @Expose
        var availableStockQty: Double? = null

        @SerializedName("orderedQuantity")
        @Expose
        var orderedQuantity: Double? = null
    }

    inner class Stock {
        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("threshold")
        @Expose
        var threshold: Double? = null

        @SerializedName("stockQuantity")
        @Expose
        var stockQuantity: Double? = null

        @SerializedName("orderedQuantity")
        @Expose
        var orderedQuantity: List<Double>? = null
    }
}