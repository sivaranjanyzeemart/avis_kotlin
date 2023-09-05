package zeemart.asia.buyers.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.StockageType
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.inventory.*
import zeemart.asia.buyers.models.inventoryimportimport.InventoryShelves
import zeemart.asia.buyers.modelsimport.InventoryTransferLocationModel

object InventoryDataManager {
    const val LIST_TAB_ID = 0
    const val ACTIVITY_TAB_ID = 1
    const val INTENT_SHELVE_ID = "INTENT_SHELVE_ID"
    const val INTENT_IS_LAST_STOCK_COUNT_DATA_AVAILABLE = "IS_LAST_STOCK_COUNT_DATA_AVAILABLE"
    const val INTENT_STOCKAGE_ID = "STOCKAGE_ID"
    const val INTENT_PRODUCT_ID = "INTENT_PRODUCT_ID"
    const val INTENT_PRODUCT_NAME = "INTENT_PRODUCT_NAME"
    const val INTENT_PRODUCT_DETAIL = "INTENT_PRODUCT_DETAIL"
    const val INTENT_SHELVE_NO_ITEMS = "INTENT_SHELVE_NO_ITEMS"
    const val INTENT_SHELVE_VALUE = "INTENT_SHELVE_VALUE"
    @JvmStatic
    val selectedShelveIds: List<String>
        get() {
            val shelveFilterList: List<Shelve.ShelveFilter> = Shelve.shelveListFromSharedPrefs
            val shelveFilterIds: MutableList<String> = ArrayList()
            for (i in shelveFilterList.indices) {
                if (shelveFilterList[i].isShelveSelected) {
                    shelveFilterIds.add(shelveFilterList[i].shelveId!!)
                }
            }
            return shelveFilterIds
        }
    @JvmStatic
    val selectedStockageTypes: List<Any>
        get() {
            val stockFilterList: List<StockageType.StockageTypeFilter> =
                StockageType.stockTypeListFromSharedPrefs
            val dataList: MutableList<StockageType> = ArrayList<StockageType>()
            for (i in stockFilterList.indices) {
                if (stockFilterList[i].isStockageTypeSelected) {
                    dataList.add(stockFilterList[i].stockageType!!)
                }
            }
            return dataList
        }
    @JvmStatic
    val selectedFiltersForActivityCount: Int
        get() {
            val shelveFilterList: List<Shelve.ShelveFilter> = Shelve.shelveListFromSharedPrefs
            val stockTypeFilterList: List<StockageType.StockageTypeFilter> =
                StockageType.stockTypeListFromSharedPrefs
            var count = 0
            if (shelveFilterList != null) {
                for (i in shelveFilterList.indices) {
                    if (shelveFilterList[i].isShelveSelected) {
                        count = count + 1
                        break
                    }
                }
            }
            if (stockTypeFilterList != null) {
                for (i in stockTypeFilterList.indices) {
                    if (stockTypeFilterList[i].isStockageTypeSelected) {
                        count = count + 1
                        break
                    }
                }
            }
            return count
        }

    @JvmStatic
    fun createJsonRequestForSaveAdjustment(
        shelve: Shelve,
        product: StockCountEntries.StockInventoryProduct,
        adjustmentReasonList: List<AdjustmentReasons.AdjustmentReasonSelected>,
        notes: String,
        quantity: String,
        selectedOutlets: List<InventoryTransferLocationModel.TransferOutlets>?
    ): String {
        val adjustmentRequest =
            InventoryCreateAdjustmentRequest()
        adjustmentRequest.shelve = shelve
        val selectedOutlet = InventoryCreateAdjustmentRequest.SelectedOutlet()
        if (selectedOutlets != null && selectedOutlets.size > 0) {
            selectedOutlet.outletName = selectedOutlets[0].outletName
            selectedOutlet.outletId = selectedOutlets[0].outletId
        }
        adjustmentRequest.selectedOutlet = selectedOutlet
        val productList: MutableList<InventoryCreateAdjustmentRequest.Product> =
            ArrayList()
        val productAdjustment =
            InventoryCreateAdjustmentRequest.Product()
        productAdjustment.productId = product.productId
        if (!StringHelper.isStringNullOrEmpty(quantity)) {
            try {
                val quantityDouble = quantity.toDouble()
                productAdjustment.stockQuantity = quantityDouble
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        productList.add(productAdjustment)
        adjustmentRequest.products = productList
        for (i in adjustmentReasonList.indices) {
            if (adjustmentReasonList[i].isSelected) {
                adjustmentRequest.stockageReason =
                    adjustmentReasonList[i].adjustmentReasons?.apiValue
                break
            }
        }
        if (!StringHelper.isStringNullOrEmpty(notes)) {
            adjustmentRequest.notes = notes
        }
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(adjustmentRequest)
    }

    @JvmStatic
    fun createJsonRequestForSaveAmendment(
        shelve: Shelve,
        product: StockCountEntries.StockInventoryProduct,
        quantity: String
    ): String {
        val adjustmentRequest =
            InventoryCreateAdjustmentRequest()
        adjustmentRequest.shelve = shelve
        val productList: MutableList<InventoryCreateAdjustmentRequest.Product> =
            ArrayList()
        val productAdjustment =
            InventoryCreateAdjustmentRequest.Product()
        productAdjustment.productId = product.productId
        if (!StringHelper.isStringNullOrEmpty(quantity)) {
            try {
                val quantityDouble = quantity.toDouble()
                productAdjustment.stockQuantity = quantityDouble
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        productList.add(productAdjustment)
        adjustmentRequest.products = productList
        adjustmentRequest.stockageReason = ZeemartAppConstants.STOCK_AMENDMENT
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(adjustmentRequest)
    }

    fun createJsonRequestForSaveStockCount(
        productQuantityList: List<InventorySaveStockRequest.Product>?,
        stockageId: String,
        stockageStatus: String,
        isItemReceivedTodayIncluded: Boolean,
        shelve: Shelve,
        receivedDate: Long
    ): String? {
        var jsonRequest: String? = null
        val request = InventorySaveStockRequest()
        request.shelve = shelve
        request.countDayIncomingIncluded = isItemReceivedTodayIncluded
        request.products = productQuantityList
        request.countDate = receivedDate
        if (!StringHelper.isStringNullOrEmpty(stockageId)) {
            request.stockageId = stockageId
        }
        if (!StringHelper.isStringNullOrEmpty(stockageStatus)) {
            request.stockageStatus = stockageStatus
        }
        jsonRequest = ZeemartBuyerApp.gsonExposeExclusive.toJson(request)
        return jsonRequest
    }

    @JvmStatic
    fun generateDeleteStockRequestJson(
        stockageId: String,
        reason: String
    ): String {
        val inventoryDeleteCountRequest =
            InventoryDeleteCountRequest()
        inventoryDeleteCountRequest.stockageId = stockageId
        inventoryDeleteCountRequest.deleteRemark = reason
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(
            inventoryDeleteCountRequest,
            InventoryDeleteCountRequest::class.java
        )
    }

    class ShelveProductListUIModel {
        var isBuyerAdminMessage = false
        var isDeleteThisStockCountMessage = false
        var inventoryProduct: StockCountEntries.StockInventoryProduct? = null
    }

    class CountStockProductListUIModel {
        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("customName")
        @Expose
        var customName: String? = null

        @SerializedName("productId")
        @Expose
        var productId: String? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("isIncludeLatestCountRow")
        @Expose
        var isIncLudelatestCountRow = false
            private set

        @SerializedName("isHeader")
        @Expose
        var isHeader = false

        @SerializedName("orderByUnitConversions")
        @Expose
        var orderByUnitConversions: List<InventoryProduct.OrderByUnitConversion>? = null

        fun setIncludeLatestCountRow(includeLatestCountRow: Boolean) {
            isIncLudelatestCountRow = includeLatestCountRow
        }
//
//        fun getOrderByUnitConversions(): List<InventoryProduct.OrderByUnitConversion>? {
//            return orderByUnitConversions
//        }

//        fun setOrderByUnitConversions(orderByUnitConversions: List<InventoryProduct.OrderByUnitConversion>?) {
//            this.orderByUnitConversions = orderByUnitConversions
//        }

        fun displayStockQuantityValue(): String {
            return ZeemartBuyerApp.getDoubleToString(quantity)
        }

        override fun toString(): String {
            return "CountStockProductListUIModel{" +
                    "productName='" + productName + '\'' +
                    ", customName='" + customName + '\'' +
                    ", productId='" + productId + '\'' +
                    ", supplier=" + supplier +
                    ", unitSize='" + unitSize + '\'' +
                    ", quantity=" + quantity +
                    ", isIncludeLatestCountRow=" + isIncLudelatestCountRow +
                    ", isHeader=" + isHeader +
                    ", orderByUnitConversions=" + orderByUnitConversions +
                    '}'
        }
    }

    class InventoryShelvesUIModel {
        var inventoryShelves: InventoryShelves? = null
        var isBuyerAdminMessage = false
    }
}