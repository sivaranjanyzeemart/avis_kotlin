package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddCardSuccessResponse {
    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    @SerializedName("path")
    @Expose
    var path: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null

    inner class Data {
        @SerializedName("customerId")
        @Expose
        var customerId: String? = null
    }
}