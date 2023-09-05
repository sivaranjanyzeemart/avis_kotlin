package zeemart.asia.buyers.models.inventoryimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes

class OnHandReceived {
    @SerializedName("timeReceived")
    @Expose
    var timeReceived: Long? = null

    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("unitSize")
    @Expose
    var unitSize: String? = null

    @SerializedName("grnId")
    @Expose
    var grnId: String? = null

    @SerializedName("quantity")
    @Expose
    var quantity: Double? = null

    @SerializedName("updatedBy")
    @Expose
    var updatedBy: RetrieveSpecificAnnouncementRes.Announcements.CreatedBy? = null
}