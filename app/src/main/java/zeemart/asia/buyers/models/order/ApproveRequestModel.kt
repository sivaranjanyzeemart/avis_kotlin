package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.UserDetails

/**
 * Created by ParulBhandari on 1/8/2018.
 */
class ApproveRequestModel {
    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("approvedBy")
    @Expose
    var approvedBy: UserDetails? = null
}