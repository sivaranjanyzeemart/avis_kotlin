package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 12/17/2017.
 */
class CreateNewOrderResponseModel {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("orderId")
    @Expose
    var orderId: String? = null
}