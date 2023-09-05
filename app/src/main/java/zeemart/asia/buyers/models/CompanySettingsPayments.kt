package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CompanySettingsPayments {
    @SerializedName("finaxarEnabled")
    @Expose
    var finaxarEnabled = false

    @SerializedName("paymentSources")
    @Expose
    var paymentSources: List<PaymentSource>? = null

    inner class PaymentSource {
        @SerializedName("paymentType")
        @Expose
        var paymentType: String? = null

        @SerializedName("approvedLimit")
        @Expose
        var approvedLimit: PriceDetails? = null

        @SerializedName("availableLimit")
        @Expose
        var availableLimit: PriceDetails? = null

        @SerializedName("status")
        @Expose
        var status: String? = null
    }
}