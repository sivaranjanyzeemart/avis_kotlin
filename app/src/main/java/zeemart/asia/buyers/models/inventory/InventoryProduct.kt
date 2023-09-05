package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getDoubleToString
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getPriceDouble
import zeemart.asia.buyers.models.*
import java.math.BigDecimal
import java.math.RoundingMode

class InventoryProduct {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("dateCreated")
    @Expose
    var dateCreated: String? = null

    @SerializedName("dateUpdated")
    @Expose
    var dateUpdated: String? = null

    @SerializedName("timeCreated")
    @Expose
    var timeCreated: Long? = null

    @SerializedName("timeUpdated")
    @Expose
    var timeUpdated: Long? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: UserDetails? = null

    @SerializedName("updatedBy")
    @Expose
    var updatedBy: UserDetails? = null

    @SerializedName("productId")
    @Expose
    var productId: String? = null

    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null

    @SerializedName("supplier")
    @Expose
    var supplier: Supplier? = null

    @SerializedName("shelves")
    @Expose
    var shelves: List<Shelve>? = null

    @SerializedName("sku")
    @Expose
    var sku: String? = null

    @SerializedName("productName")
    @Expose
    var productName: String? = null

    @SerializedName("customName")
    @Expose
    var customName: String? = null

    @SerializedName("categoryTags")
    @Expose
    var categoryTags: List<String>? = null

    @SerializedName("unitSize")
    @Expose
    var unitSize: String? = null

    @SerializedName("parLevel")
    @Expose
    var parLevel: Double? = null

    @SerializedName("unitPrice")
    @Expose
    var unitPrice: PriceDetails? = null

    @SerializedName("belowParLevel")
    @Expose
    var belowParLevel = false

    @SerializedName("dateLastCounted")
    @Expose
    var dateLastCounted: String? = null

    @SerializedName("timeLastCounted")
    @Expose
    var timeLastCounted: Long? = null

    @SerializedName("totalEstimatedValue")
    @Expose
    var totalEstimatedValue: PriceDetails? = null

    @SerializedName("latestStockCounts")
    @Expose
    var latestStockCounts: List<LatestStockCount>? = null

    @SerializedName("incomings")
    @Expose
    var incomings: List<Incoming>? = null

    @SerializedName("favourite")
    @Expose
    var favourite: Boolean? = null

    @SerializedName("isConversionUpdated")
    @Expose
    var isConversionUpdated = false

    @SerializedName("orderByUnitConversions")
    @Expose
    var orderByUnitConversions: List<OrderByUnitConversion>? = null

    @SerializedName("prevOrderByUnitConversions")
    @Expose
    var prevOrderByUnitConversions: List<OrderByUnitConversion>? = null

    @SerializedName("lastupdatedData")
    @Expose
    var lastupdatedData: OrderByUnitConversion? = null

    @SerializedName("conversionReview")
    @Expose
    var conversionReview: String? = null

    class LatestStockCount {
        @SerializedName("stockageId")
        @Expose
        var stockageId: String? = null

        @SerializedName("shelveId")
        @Expose
        var shelveId: String? = null

        @SerializedName("timeCreated")
        @Expose
        var timeCreated: Long? = null

        @SerializedName("dateCreated")
        @Expose
        var dateCreated: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("onHandQuantity")
        @Expose
        var onHandQuantity: Double? = null

        @SerializedName("estimatedValue")
        @Expose
        var estimatedValue: PriceDetails? = null

        @SerializedName("createdBy")
        @Expose
        var createdBy: UserDetails? = null
    }

    class Incoming {
        @SerializedName("timeDelivery")
        @Expose
        var timeDelivery: Long? = null

        @SerializedName("dateDelivery")
        @Expose
        var dateDelivery: String? = null

        @SerializedName("timeCreated")
        @Expose
        var timeCreated: Long? = null

        @SerializedName("dateCreated")
        @Expose
        var dateCreated: String? = null

        @SerializedName("timeReceived")
        @Expose
        var timeReceived: Long? = null

        @SerializedName("dateReceived")
        @Expose
        var dateReceived: String? = null

        @SerializedName("createdBy")
        @Expose
        var createdBy: UserDetails? = null

        @SerializedName("orderId")
        @Expose
        var orderId: String? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("estimatedValue")
        @Expose
        var estimatedValue: PriceDetails? = null

        @SerializedName("orderStatus")
        @Expose
        var orderStatus: String? = null

        @SerializedName("isReceived")
        @Expose
        var received: Boolean? = null
    }

    class InventoryProductWithPagination : Pagination() {
        @SerializedName("data")
        @Expose
        var data: List<InventoryProduct>? = null
    }

    inner class OrderByUnitConversion {
        @SerializedName("stockUnit")
        @Expose
        var stockUnit: String? = null

        @SerializedName("stockUnitQty")
        @Expose
        var stockUnitQty: Double? = null

        @SerializedName("orderByUnit")
        @Expose
        var orderByUnit: String? = null

        @SerializedName("orderByUnitQty")
        @Expose
        var orderByUnitQty: Double? = null

        @SerializedName("selectedQuantity")
        @Expose
        var selectedQuantity: Double? = null
        val stockUnitQtyDisplayValue: String
            get() = if (stockUnitQty != null) {
                getDoubleToString(getPriceDouble(stockUnitQty!!, 1))
            } else {
                "0"
            }
        val orderByUnitQtyDisplayValue: String
            get() = if (orderByUnitQty != null) {
                getDoubleToString(orderByUnitQty)
            } else {
                "0"
            }
        val orderByUnitQtyConversionValue: Double
            get() = if (orderByUnitQty != null) {
                val bd = BigDecimal(orderByUnitQty!!).setScale(2, RoundingMode.HALF_UP)
                100 / bd.toDouble() / 100
            } else {
                1.0
            }

        fun displaySelectedQuantityValue(): String {
            return getDoubleToString(selectedQuantity)
        }

        override fun toString(): String {
            return "OrderByUnitConversion{" +
                    "stockUnit='" + stockUnit + '\'' +
                    ", stockUnitQty=" + stockUnitQty +
                    ", orderByUnit='" + orderByUnit + '\'' +
                    ", orderByUnitQty=" + orderByUnitQty +
                    ", selectedQuantity=" + selectedQuantity +
                    '}'
        }
    }
}