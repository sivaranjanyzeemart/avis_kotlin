package zeemart.asia.buyers.models.invoiceimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.Pagination
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.invoice.Invoice

/**
 * Created by RajPrudhviMarella on 7/2/2020.
 */
class EInvoicesGroupBySupplier : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: EInvoiceWithPagination? = null

    class EInvoiceWithPagination : Pagination() {
        @SerializedName("invoicesGroupBySupplier")
        @Expose
        var invoicesGroupBySupplier: List<InvoicesGroupBySupplier>? = null

        @SerializedName("overdue")
        @Expose
        var overdue: Overdue? = null

        @SerializedName("upcoming")
        @Expose
        var upcoming: Upcoming? = null

        inner class Overdue {
            @SerializedName("paymentAmount")
            @Expose
            var paymentAmount: PriceDetails? = null

            @SerializedName("startDate")
            @Expose
            var startDate: Long? = null

            @SerializedName("endDate")
            @Expose
            var endDate: Long? = null
        }

        inner class Upcoming {
            @SerializedName("paymentAmount")
            @Expose
            var paymentAmount: PriceDetails? = null

            @SerializedName("startDate")
            @Expose
            var startDate: Long? = null

            @SerializedName("endDate")
            @Expose
            var endDate: Long? = null
        }

        inner class InvoicesGroupBySupplier {
            @SerializedName("supplier")
            @Expose
            var supplier: Supplier? = null

            @SerializedName("invoices")
            @Expose
            var invoices: List<Invoice>? = null
        }
    }
}