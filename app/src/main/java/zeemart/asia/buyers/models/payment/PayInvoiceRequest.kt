package zeemart.asia.buyers.models.paymentimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.invoice.Invoice

class PayInvoiceRequest {
    @SerializedName("supplierId")
    @Expose
    var supplierId: String? = null

    @SerializedName("invoiceIds")
    @Expose
    var invoiceIds: List<String>? = null

    @SerializedName("usedCredit")
    @Expose
    var usedCredit: UsedCredit? = null

    @SerializedName("cardReferenceNo")
    @Expose
    var cardReferenceNo: String? = null

    @SerializedName("paidBy")
    @Expose
    var paidBy: UserDetails? = null

    @SerializedName("status")
    @Expose
    var status: Invoice.PaymentStatus? = null

    class UsedCredit {
        @SerializedName("amountV1")
        @Expose
        var amount: Double? = null

        @SerializedName("currencyCode")
        @Expose
        var currencyCode: String? = null
    }
}