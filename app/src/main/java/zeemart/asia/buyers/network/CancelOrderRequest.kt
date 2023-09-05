package zeemart.asia.buyers.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

internal class CancelOrderRequest constructor() {
    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("cancelledRemark")
    @Expose
    var cancelledRemark: String? = null
//    fun getOrderId(): String? {
//        return orderId
//    }
//
//    fun setOrderId(orderId: String?) {
//        this.orderId = orderId
//    }
//
//    fun getCancelledRemark(): String? {
//        return cancelledRemark
//    }
//
//    fun setCancelledRemark(cancelledRemark: String?) {
//        this.cancelledRemark = cancelledRemark
//    }
}