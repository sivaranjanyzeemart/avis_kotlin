package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class BaseResponse {
    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    @SerializedName("path")
    @Expose
    var path: String? = null

    @SerializedName("status")
    @Expose
    var status = 0

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("details")
    @Expose
    var details: String? = null
}