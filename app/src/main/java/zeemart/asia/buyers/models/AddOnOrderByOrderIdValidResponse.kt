package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class AddOnOrderByOrderIdValidResponse {
    @SerializedName("data")
    @Expose
    var data: List<AddOnOrderByOrderIdValidResponse.Datum>? = null

    inner class Datum {
        @SerializedName("orderId")
        @Expose
        var orderId: String? = null

        @SerializedName("isAddOn")
        @Expose
        private var isAddOn = false
        fun IsAddOn(): Boolean {
            return isAddOn
        }

        fun setIsAddOn(isAddOn: Boolean) {
            this.isAddOn = isAddOn
        }
    }
}