package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Incoming {
    @SerializedName("incomingQty")
    @Expose
    var incomingQty: Double? = null

    @SerializedName("timeDelivery")
    @Expose
    var timeDelivery: Long? = null
}