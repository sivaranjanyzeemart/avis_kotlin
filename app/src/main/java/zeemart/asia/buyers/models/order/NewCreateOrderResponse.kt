package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.order.Orders

/**
 * response structure for deal and essentials
 */
class NewCreateOrderResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Orders? = null
}