package zeemart.asia.buyers.models.paymentimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.invoice.Invoice

class PayInvoiceResponse {
    @SerializedName("status")
    @Expose
    var status = 0

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: PayInvoiceResponse.Data? = null

    enum class TransactionStatus(var value: String) {
        PAID("Paid"), FAILED("Failed");

        override fun toString(): String {
            return value
        }
    }

    inner class Data {
        @SerializedName("invoiceCreditData")
        @Expose
        var invoiceCreditData: InvoiceCreditData? = null

        @SerializedName("transactionId")
        @Expose
        var transactionId: String? = null

        @SerializedName("transactionStatus")
        @Expose
        var transactionStatus: String? = null

        @SerializedName("failureReason")
        @Expose
        var failureReason: String? = null
        fun isTransactionStatus(transactionStatus: TransactionStatus): Boolean {
            return if (!StringHelper.isStringNullOrEmpty(this.transactionStatus)) {
                this.transactionStatus == transactionStatus.toString()
            } else false
        }

        inner class InvoiceCreditData {
            @SerializedName("invoices")
            @Expose
            var invoices: List<Invoice>? = null
        }
    }
}