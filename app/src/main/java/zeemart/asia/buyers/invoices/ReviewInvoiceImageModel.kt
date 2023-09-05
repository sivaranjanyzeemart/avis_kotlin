package zeemart.asia.buyers.invoices

import com.google.gson.annotations.Expose

class ReviewInvoiceImageModel {
    @Expose
    var lstInvoices: List<InQueueForUploadDataModel>? = null
}