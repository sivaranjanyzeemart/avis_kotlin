package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DraftErrorModel {
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

    @SerializedName("details")
    @Expose
    var details: String? = null

    @SerializedName("errors")
    @Expose
    var errors: List<ErrorModel>? = null

}