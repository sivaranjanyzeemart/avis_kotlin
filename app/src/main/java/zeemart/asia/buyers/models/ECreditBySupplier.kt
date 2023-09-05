package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.invoice.Invoice

/**
 * Created by RajPrudhviMarella on 7/2/2020.
 */
class ECreditBySupplier : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: ECreditBySupplierWithPagination? = null

    inner class ECreditBySupplierWithPagination : Pagination() {
        @SerializedName("invoices")
        @Expose
        var invoices: List<Invoice>? = null

        @SerializedName("totalCredits")
        @Expose
        var totalCredits: PriceDetails? = null
    }
}