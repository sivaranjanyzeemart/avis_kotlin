package zeemart.asia.buyers.models.OrderPayments

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UploadTransactionImage {
    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("imageURL")
    @Expose
    var imageURL: String? = null

    @SerializedName("paymentType")
    @Expose
    var paymentType: String? = null

    enum class PaymentType(val displayName: String) {
        BANK_TRANSFER("bankTransfer"), PAY_NOW("payNow"), CARD("card"), FINAXAR("finaxar"), FUNDING_SOCIETIES(
            "FundingSocieties"
        ),
        GRAB_FINANCE("GrabFinance");

    }
}