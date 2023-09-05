package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getDoubleToString
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getPriceDouble
import zeemart.asia.buyers.models.Pagination
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.UserDetails

open class StockCountEntries {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("dateCreated")
    @Expose
    var dateCreated: String? = null

    @SerializedName("dateUpdated")
    @Expose
    var dateUpdated: String? = null

    @SerializedName("dateDeleted")
    @Expose
    var dateDeleted: String? = null

    @SerializedName("timeCreated")
    @Expose
    var timeCreated: Long? = null

    @SerializedName("timeUpdated")
    @Expose
    var timeUpdated: Long? = null
        get() = if (field == null) {
            timeCreated
        } else {
            field
        }

    @SerializedName("timeDeleted")
    @Expose
    var timeDeleted: Long? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: UserDetails? = null

    @SerializedName("updatedBy")
    @Expose
    var updatedBy: UserDetails? = null

    @SerializedName("deletedBy")
    @Expose
    var deletedBy: UserDetails? = null

    @SerializedName("stockageId")
    @Expose
    var stockageId: String? = null

    @SerializedName("outletId")
    @Expose
    var outletId: String? = null

    @SerializedName("shelve")
    @Expose
    var shelve: Shelve? = null

    @SerializedName("countDayIncomingIncluded")
    @Expose
    var countDayIncomingIncluded: Boolean? = null

    @SerializedName("stockageType")
    @Expose
    var stockageType: String? = null

    @SerializedName("products")
    @Expose
    var products: List<StockInventoryProduct>? = null

    @SerializedName("estimatedTotalValue")
    @Expose
    var estimatedTotalValue: PriceDetails? = null

    @SerializedName("stockageReason")
    @Expose
    var stockageReason: String? = null

    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("deleteRemark")
    @Expose
    var deleteRemark: String? = null

    @SerializedName("countDate")
    @Expose
    var countDate: Long? = null

    @SerializedName("stockUnitConversion")
    @Expose
    var orderByUnitConversions: StockUnitConversionsInventory? = null

    @SerializedName("prevStockUnitConversion")
    @Expose
    var prevOrderByUnitConversions: StockUnitConversionsInventory? = null

    @SerializedName("selectedOutlet")
    @Expose
    var selectedOutlet: InventoryCreateAdjustmentRequest.SelectedOutlet? = null

    class StockCountEntriesWithPagination : Pagination() {
        @SerializedName("data")
        @Expose
        var data: List<StockCountEntriesMgr>? = null
    }

    class StockInventoryProduct {
        @SerializedName("productId")
        @Expose
        var productId: String? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("customName")
        @Expose
        var customName: String? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("unitPrice")
        @Expose
        var unitPrice: PriceDetails? = null

        @SerializedName("parLevel")
        @Expose
        var parLevel = 0.0

        @SerializedName("stockQuantity")
        @Expose
        var stockQuantity: Double? = null

        @SerializedName("onHandQuantity")
        @Expose
        var onHandQuantity: Double? = null

        @SerializedName("prevQuantity")
        @Expose
        var prevQuantity: Double? = null

        @SerializedName("belowParLevel")
        @Expose
        var isBelowParLevel = false
        fun getBelowParLevel(): Boolean {
            return isBelowParLevel
        }

        val stockQuantityDisplayValue: String
            get() = getDoubleToString(stockQuantity)
        val onHandQuantityDisplayValue: String
            get() = getDoubleToString(onHandQuantity)
        val previousQuantityDisplayValue: String
            get() = if (prevQuantity != null) {
                getDoubleToString(prevQuantity)
            } else {
                "0"
            }
        val parLevelDisplayValue: String
            get() = getDoubleToString(parLevel)
    }

    enum class InventoryActivityStatus(val value: String) {
        ACTIVE("ACTIVE"), DELETED("DELETED");

    }

    inner class StockUnitConversionsInventory {
        @SerializedName("stockUnit")
        @Expose
        var stockUnit: String? = null

        @SerializedName("stockUnitQty")
        @Expose
        var stockUnitQty: Double? = null

        @SerializedName("productUnit")
        @Expose
        var orderByUnit: String? = null

        @SerializedName("productUnitQty")
        @Expose
        var orderByUnitQty: Double? = null
        val stockUnitQtyDisplayValue: String
            get() = if (stockUnitQty != null) {
                getDoubleToString(getPriceDouble(stockUnitQty!!, 1))
            } else {
                "0"
            }
        val orderByUnitQtyDisplayValue: String
            get() = if (orderByUnitQty != null) {
                getDoubleToString(
                    getPriceDouble(
                        orderByUnitQty!!, 1
                    )
                )
            } else {
                "0"
            }
    }
}