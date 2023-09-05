package zeemart.asia.buyers.models.paymentimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse

class LinkedSupplierPreference : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: LinkedSupplierPreferenceResponse? = null

    class LinkedSupplierPreferenceResponse {
        @SerializedName("payments")
        @Expose
        var payments: ManualPaymentData? = null
    }

    class ManualPaymentData {
        @SerializedName("manualPayment")
        @Expose
        var manualPayment: ManualPaymentStatus? = null
    }

    class ManualPaymentStatus {
        @SerializedName("active")
        @Expose
        var isActive = false
    }
}