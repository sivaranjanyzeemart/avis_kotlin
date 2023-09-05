package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr

/**
 * Created by ParulBhandari on 3/20/2018.
 */
interface EditInvoiceListener {
    fun retryUploadOfFailedInvoices(
        failedInvoice: List<InQueueForUploadDataModel?>?,
        retryInvoicePosition: Int
    )

    fun getSelectedInvoicesDeleteOrMerge(selectedInvoices: List<InvoiceUploadsListDataMgr?>?)
    fun invoicesCountByStatus()
    fun onEditButtonClicked()
    fun onListItemLongPressed()
}