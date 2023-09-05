package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 11/23/2017.
 */
open class NotificationDetail {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("outletId")
    @Expose
    var outletId: String? = null

    @SerializedName("dateTime")
    @Expose
    var dateTime: Long? = null

    @SerializedName("isRead")
    @Expose
    var read: Boolean? = null

    @SerializedName("text")
    @Expose
    var text: String? = null

    @SerializedName("uri")
    @Expose
    var uri: NotificationUri? = null

    @SerializedName("imageURL")
    @Expose
    var imageURL: String? = null

    @SerializedName("supplierName")
    @Expose
    var supplierName: String? = null
}