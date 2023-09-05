package zeemart.asia.buyers.models.inventoryimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.inventory.OnHandHistoryActivity

class OnHandHistory {
    @SerializedName("productId")
    @Expose
    var productId: String? = null

    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null

    @SerializedName("stockCountQty")
    @Expose
    var stockCountQty: Double? = null

    @SerializedName("posSalesQty")
    @Expose
    var posSalesQty: Double? = null

    @SerializedName("onHandStockActivity")
    @Expose
    var onHandStockActivity: List<OnHandHistoryActivity>? = null
}