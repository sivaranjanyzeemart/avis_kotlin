package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.inventory.InventoryProduct.InventoryProductWithPagination
import zeemart.asia.buyers.models.inventory.StockCountEntries.StockCountEntriesWithPagination
import zeemart.asia.buyers.models.inventoryimportimport.InventoryShelves
import zeemart.asia.buyers.models.inventoryimportimport.OnHandHistory

class InventoryResponse {
    class AllStockShelvesByOutlet : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<InventoryShelves>? = null
    }

    class StockCountEntriesByOutlet : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: StockCountEntriesWithPagination? = null
    }

    class ProductsByShelveId : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: InventoryProductWithPagination? = null
    }

    class StockCountEntryByOutlet : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: StockCountEntries? = null
    }

    class OnHandHistoryData : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: OnHandHistory? = null
    }
}