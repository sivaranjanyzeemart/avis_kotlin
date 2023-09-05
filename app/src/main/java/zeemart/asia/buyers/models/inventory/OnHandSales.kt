package zeemart.asia.buyers.models.inventoryimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OnHandSales {
    @Expose
    @SerializedName("invoiceNumber")
    var invoiceNumber: String? = null

    @Expose
    @SerializedName("timeCreated")
    var timeCreated = 0

    @Expose
    @SerializedName("posSalesQty")
    var posSalesQty = 0

    @Expose
    @SerializedName("activityId")
    var activityId: String? = null
}