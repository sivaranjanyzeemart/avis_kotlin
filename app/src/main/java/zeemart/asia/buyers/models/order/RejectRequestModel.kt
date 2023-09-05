package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.UserDetails

/**
 * Created by ParulBhandari on 1/8/2018.
 */
class RejectRequestModel {
    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("rejectedBy")
    @Expose
    var rejectedBy: UserDetails? = null

    @SerializedName("rejectedRemark")
    @Expose
    var rejectedRemark: String? = null
}