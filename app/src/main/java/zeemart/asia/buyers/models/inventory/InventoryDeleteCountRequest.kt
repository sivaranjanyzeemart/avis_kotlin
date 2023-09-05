package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class InventoryDeleteCountRequest {
    @SerializedName("stockageId")
    @Expose
    var stockageId: String? = null

    @SerializedName("deleteRemark")
    @Expose
    var deleteRemark: String? = null
}