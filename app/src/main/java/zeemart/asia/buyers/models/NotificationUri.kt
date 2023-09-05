package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 1/12/2018.
 */
class NotificationUri {
    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("parameters")
    @Expose
    var parameters: NotificationUriParameters? = null

    inner class NotificationUriParameters {
        @SerializedName("orderId")
        @Expose
        var orderId: String? = null

        @SerializedName("orderStatus")
        @Expose
        var orderStatus: String? = null
    }
}