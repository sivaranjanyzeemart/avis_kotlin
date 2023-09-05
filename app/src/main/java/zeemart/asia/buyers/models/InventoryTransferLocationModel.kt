package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InventoryTransferLocationModel {
    @SerializedName("path")
    @Expose
    var path: String? = null

    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: List<TransferOutlets>? = null

    inner class TransferOutlets {
        @SerializedName("outletId")
        @Expose
        var outletId: String? = null

        @SerializedName("outletName")
        @Expose
        var outletName: String? = null

        @SerializedName("logoURL")
        @Expose
        var logoURL: String? = null

        @Expose
        var isSelected = false
    }
}