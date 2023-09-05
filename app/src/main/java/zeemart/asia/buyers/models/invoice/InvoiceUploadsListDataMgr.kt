package zeemart.asia.buyers.models.invoice

import zeemart.asia.buyers.R
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel

class InvoiceUploadsListDataMgr {
    var header: UploadListHeader? = null
    var uploadedInvoice: InvoiceMgr? = null
    var inQueueForUploadInvoice: InQueueForUploadDataModel? = null
    var inQueueForUploadMergedInvoice: List<InQueueForUploadDataModel>? = null

    enum class UploadListHeader(val headerType: Int, val headerResId: Int) {
        Uploaded(1, R.string.txt_uploaded), InQueue(2, R.string.txt_in_queue), Rejected(
            3,
            R.string.txt_rejected_uploads
        );

    }

    fun isHeader(uploadListHeader: UploadListHeader): Boolean {
        return header != null && header!!.headerType == uploadListHeader.headerType
    }

    companion object {
        @JvmStatic
        fun deserializeInvoiceMgrToInvoiceUploads(invoiceUploadedList: List<InvoiceMgr?>): List<InvoiceUploadsListDataMgr> {
            val uploadsListInvoiceMgr: MutableList<InvoiceUploadsListDataMgr> = ArrayList()
            for (i in invoiceUploadedList.indices) {
                val invoiceUploadsListDataMgr = InvoiceUploadsListDataMgr()
                invoiceUploadsListDataMgr.uploadedInvoice = invoiceUploadedList[i]
                uploadsListInvoiceMgr.add(invoiceUploadsListDataMgr)
            }
            return uploadsListInvoiceMgr
        }

        @JvmStatic
        fun deserializeInQueueToInvoiceUploads(invoiceInQueuedList: List<InQueueForUploadDataModel>): List<InvoiceUploadsListDataMgr> {
            val inQueueList: MutableList<InvoiceUploadsListDataMgr> = ArrayList()
            val mergedInvoice: MutableList<InQueueForUploadDataModel> = ArrayList()
            for (i in invoiceInQueuedList.indices) {
                if (invoiceInQueuedList[i].isMerged) {
                    mergedInvoice.add(invoiceInQueuedList[i])
                }
            }
            if (mergedInvoice != null && mergedInvoice.size > 0) {
                val invoiceUploadsListDataMgr = InvoiceUploadsListDataMgr()
                invoiceUploadsListDataMgr.inQueueForUploadMergedInvoice = mergedInvoice
                inQueueList.add(0, invoiceUploadsListDataMgr)
            }
            for (i in invoiceInQueuedList.indices) {
                val invoiceUploadsListDataMgrs = InvoiceUploadsListDataMgr()
                if (!invoiceInQueuedList[i].isMerged) {
                    invoiceUploadsListDataMgrs.inQueueForUploadInvoice = invoiceInQueuedList[i]
                    inQueueList.add(invoiceUploadsListDataMgrs)
                }
            }
            return inQueueList
        }

        @JvmStatic
        fun getInstanceWithHeaderData(uploadListHeader: UploadListHeader?): InvoiceUploadsListDataMgr {
            val headerData = InvoiceUploadsListDataMgr()
            headerData.header = uploadListHeader
            return headerData
        }
    }
}