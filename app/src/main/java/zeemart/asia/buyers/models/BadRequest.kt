package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by saiful on 3/1/18.
 */
class BadRequest {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("errors")
    @Expose
    var errors: List<RequestError>? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    class RequestError {
        @SerializedName("message")
        @Expose
        var message: String? = null

        @SerializedName("parentNode")
        @Expose
        var parentNode: String? = null

        @SerializedName("errortype")
        @Expose
        var errortype: String? = null
    }
}