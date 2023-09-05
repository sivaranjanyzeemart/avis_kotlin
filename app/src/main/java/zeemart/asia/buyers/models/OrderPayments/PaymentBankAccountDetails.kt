package zeemart.asia.buyers.models.OrderPayments

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse

class PaymentBankAccountDetails : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: List<AccountDetailsResponse>? = null

    class AccountDetailsResponse {
        @SerializedName("bank")
        @Expose
        var bank: Bank? = null

        @SerializedName("payNow")
        @Expose
        var payNow: PayNow? = null

        @SerializedName("id")
        @Expose
        var id: String? = null
    }

    class PayNow {
        @SerializedName("uenId")
        @Expose
        var uenId: String? = null

        @SerializedName("accountName")
        @Expose
        var accountName: String? = null

        @SerializedName("qrCode")
        @Expose
        var qrCode: String? = null
    }

    class Bank {
        @SerializedName("bankName")
        @Expose
        var bankName: String? = null

        @SerializedName("accountName")
        @Expose
        var accountName: String? = null

        @SerializedName("accountNumber")
        @Expose
        var accountNumber: String? = null
    }
}