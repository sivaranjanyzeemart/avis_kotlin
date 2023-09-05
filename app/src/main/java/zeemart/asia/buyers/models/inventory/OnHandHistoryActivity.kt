package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.inventoryimportimport.OnHandAdjustment
import zeemart.asia.buyers.models.inventoryimportimport.OnHandReceived
import zeemart.asia.buyers.models.inventoryimportimport.OnHandSales


class OnHandHistoryActivity {
    @SerializedName("timeCreated")
    @Expose
    var timeCreated: Long? = null

    @SerializedName("activityType")
    @Expose
    var activityType: String? = null

    @SerializedName("received")
    @Expose
    var received: OnHandReceived? = null

    @SerializedName("adjustment")
    @Expose
    var adjustment: OnHandAdjustment? = null

    @SerializedName("posSales")
    @Expose
    var posSales: OnHandSales? = null
    override fun toString(): String {
        return "OnHandHistoryActivity{" +
                "timeCreated=" + timeCreated +
                ", activityType='" + activityType + '\'' +
                ", received=" + received +
                ", adjustment=" + adjustment +
                ", posSales=" + posSales +
                '}'
    }
}