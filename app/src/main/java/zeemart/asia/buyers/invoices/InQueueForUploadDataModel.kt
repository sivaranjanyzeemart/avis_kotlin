package zeemart.asia.buyers.invoices

import com.google.gson.annotations.Expose

/**
 * Created by ParulBhandari on 6/19/2018.
 */
class InQueueForUploadDataModel {
    enum class InvoiceStatus {
        FAILED, INVOICE_UPLOADING, IN_QUEUE, NOT_PROCESSED, INVOICE_UPLOADED, IMAGE_UPLOADED
    }

    @Expose
    var imageFilePath: String? = null

    @Expose
    var imageDirectoryPath: String? = null

    @Expose
    var objectInfoFilePath: String? = null

    @Expose
    var rotation = 0

    @Expose
    var status = InvoiceStatus.NOT_PROCESSED

    @Expose
    var serverImagePath: String? = null

    @Expose
    var outletId: String? = null

    @Expose
    var outletName: String? = null

    @Expose
    var selectedPdfOriginalName: String? = null

    @Expose
    var isInvoiceSelectedForDeleteOrMerge = false

    @Expose
    var isInvoiceSelectedIsImage = false

    @Expose
    var isNewAdded = false

    @Expose
    var isMerged = false

    @Expose
    var countOfMerged = 0

    @Expose
    var imagesCount = 0

    @Expose
    var isDeleted = false
    override fun equals(obj: Any?): Boolean {
        return if (obj == null) {
            false
        } else {
            if (obj is InQueueForUploadDataModel) {
                val inQueueForUploadDataModel = obj
                return if (rotation == inQueueForUploadDataModel.rotation && imageDirectoryPath == inQueueForUploadDataModel.imageDirectoryPath && imageFilePath == inQueueForUploadDataModel.imageFilePath) {
                    true
                } else {
                    false
                }
            }
            false
        }
    }
}