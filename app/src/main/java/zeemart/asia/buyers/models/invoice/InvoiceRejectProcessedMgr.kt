package zeemart.asia.buyers.models.invoiceimportimport

import com.google.gson.annotations.Expose
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr

/**
 * Created by RajPrudhviMarella on 23/Dec/2021.
 */
class InvoiceRejectProcessedMgr {
    @Expose
    var invoiceUploadsListDataMgr: InvoiceUploadsListDataMgr? = null

    @Expose
    var invoiceMgr: InvoiceMgr? = null
}