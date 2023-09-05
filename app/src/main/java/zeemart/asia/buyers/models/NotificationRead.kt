package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 11/27/2017.
 */
class NotificationRead {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("isRead")
    @Expose
    var isRead = false
}