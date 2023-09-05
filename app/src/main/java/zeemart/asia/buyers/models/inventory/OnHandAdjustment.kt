package zeemart.asia.buyers.models.inventoryimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes


class OnHandAdjustment {
    @SerializedName("timeCreated")
    @Expose
    var timeCreated: Long? = null

    @SerializedName("stockageId")
    @Expose
    var stockageId: String? = null

    @SerializedName("shelveId")
    @Expose
    var shelveId: String? = null

    @SerializedName("stockageReason")
    @Expose
    var stockageReason: String? = null

    @SerializedName("quantity")
    @Expose
    var quantity: Double? = null

    @SerializedName("estimatedValue")
    @Expose
    var estimatedValue: PriceDetails? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: RetrieveSpecificAnnouncementRes.Announcements.CreatedBy? = null

    @SerializedName("selectedOutlet")
    @Expose
    var selectedOutlet: Outlet? = null
}