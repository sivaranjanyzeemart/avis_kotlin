package zeemart.asia.buyers.models.invoice

import android.util.Log
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.marketlist.Product
import java.util.*

/**
 * Created by saiful on 3/1/18.
 */
open class Invoice {
    enum class Status(var invoiceStatusName: String) {
        PENDING("Pending"), PROCESSING("Processing"), PROCESSED("Processed"), DELETED("Deleted"), EXPORTED(
            "Exported"
        ),
        VOIDED("Voided"), DRAFT("Draft"), ISSUED("Issued"), PAID("Paid"), PENDINGREVIEW("PendingReview"), REJECTED(
            "Rejected"
        );

        override fun toString(): String {
            return invoiceStatusName
        }
    }

    enum class InvoiceStatus(var statusName: String) {
        INVOICED("Invoiced"), NO_LINKED_INVOICE("No linked invoice");

    }

    enum class InvoiceType(var value: String) {
        EINVOICE("eInvoice"), ECREDITNOTE("eCreditNote");

        override fun toString(): String {
            return value
        }
    }

    enum class PaymentStatus(var value: String) {
        USED("Used"), UNUSED("Unused"), PAID("Paid"), UNPAID("Unpaid");

        override fun toString(): String {
            return value
        }
    }

    class InvoiceDateCompare : Comparator<Invoice> {
        override fun compare(i1: Invoice, i2: Invoice): Int {
            if (i1.invoiceDate == null || i2.invoiceDate == null) return 0
            val r = i2.invoiceDate!! - i1.invoiceDate!!
            return r.toInt()
        }
    }

    class TimeCreatedCompare : Comparator<Invoice?> {
        override fun compare(p0: Invoice?, p1: Invoice?): Int {
            if (p0?.timeCreated == null || p1?.timeCreated == null) return 0
            val r = p1.timeCreated!! - p0.timeCreated!!
            return r.toInt()
        }
    }

    @SerializedName("invoiceId")
    @Expose
    var invoiceId: String? = null

    @SerializedName("invoiceDate")
    @Expose
    var invoiceDate: Long? = null
        get() = if (field == null) {
            0L
        } else {
            field
        }

    @SerializedName("paymentDueDate")
    @Expose
    var paymentDueDate: Long? = null
        get() = if (field == null) {
            0L
        } else {
            field
        }

    @SerializedName("invoiceNum")
    @Expose
    var invoiceNum: String? = null

    @SerializedName("timeCreated")
    @Expose
    var timeCreated: Long? = null

    @SerializedName("daysElapsed")
    @Expose
    var daysElapsed: Int? = null

    @SerializedName("paymentTerms")
    @Expose
    var paymentTerms: String? = null

    @SerializedName("totalCharge")
    @Expose
    var totalCharge: PriceDetails? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("images")
    @Expose
    var images: List<ImagesModel>? = null

    @SerializedName("products")
    @Expose
    var products: List<InvoiceProduct>? = null

    @SerializedName("orderData")
    @Expose
    var orderData: List<OrderData>? = null

    @SerializedName("discount")
    @Expose
    var discount: PriceDetails? = null

    @SerializedName("subTotal")
    @Expose
    var subtotal: PriceDetails? = null

    @SerializedName("gst")
    @Expose
    var gst: PriceDetails? = null

    @SerializedName("deliveryFee")
    @Expose
    var deliveryFee: PriceDetails? = null

    @SerializedName("others")
    @Expose
    var others: Others? = null

    @SerializedName("orderIds")
    @Expose
    var orderIds: List<String>? = null

    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null

    @SerializedName("supplier")
    @Expose
    var supplier: Supplier? = null

    @SerializedName("lastUpdatedBy")
    @Expose
    var lastUpdatedBy: UserDetails? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: UserDetails? = null

    @SerializedName("invoiceType")
    @Expose
    var invoiceType: String? = null

    @SerializedName("pdfURL")
    @Expose
    var pdfURL: String? = null

    @SerializedName("count")
    @Expose
    var count: Int? = null

    @SerializedName("rejectReason")
    @Expose
    var rejectReason: UserDetails? = null

    @SerializedName("linkedInvoice")
    @Expose
    var linkedInvoice: LinkedInvoice? = null

    @SerializedName("paymentStatus")
    @Expose
    var paymentStatus: String? = null

    @SerializedName("linkedCreditNotes")
    @Expose
    var linkedCreditNotes: List<LinkedCreditNotes>? = null

    @SerializedName("customLineItems")
    @Expose
    var customLineItems: List<InvoiceProduct>? = null

    @SerializedName("isGuest")
    @Expose
    var guestInvoice: Boolean? = null
    var companyId: String? = null

    //Added for merging and deleting invoice
    @Expose
    var isInvoiceSelected = false
    val isProcessingProcessedDeleted: Boolean
        get() {
            if (status == Status.PROCESSED.toString()) return true
            if (status == Status.PROCESSING.toString()) return true
            return if (status == Status.DELETED.toString()) true else false
        }

    class Others {
        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("charge")
        @Expose
        var charge: PriceDetails? = null
    }

    class Create {
        class Response {
            @SerializedName("code")
            @Expose
            var code: String? = null

            @SerializedName("message")
            @Expose
            var message: String? = null
        }

        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @SerializedName("images")
        @Expose
        var images: List<ImagesModel>? = null

        @SerializedName("orderIds")
        @Expose
        lateinit var orderIds: Array<String>

        @SerializedName("createdBy")
        @Expose
        var createdBy: UserDetails? = null
    }

    class Delete {
        @SerializedName("invoiceIds")
        @Expose
        var invoiceIds: List<String>? = null

        @SerializedName("deletedBy")
        @Expose
        var deletedBy: UserDetails? = null
    }

    class MergeInvoice {
        @SerializedName("toBeMergeInvoices")
        @Expose
        var toBeMergeInvoices: List<String>? = null

        @SerializedName("mergeIntoInvoice")
        @Expose
        var mergeIntoInvoice: String? = null

        @SerializedName("mergedBy")
        @Expose
        var mergedBy: UserDetails? = null
    }

    class InvoiceProduct : Product() {
        @SerializedName("discount")
        @Expose
        var discount: PriceDetails? = null
    }

    val isInvoiceDeletable: Boolean
        get() = status == Status.PROCESSED.toString() || status == Status.EXPORTED.toString()

    class InvoicesWithPaginationData : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: InvoiceWithPagination? = null
    }

    class InvoicesWithPaginationDataV1 : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: InvoiceWithPaginationV1? = null
    }

    class InvoiceWithPagination : Pagination() {
        @SerializedName("invoices")
        @Expose
        var invoices: List<Invoice>? = null
    }

    class InvoiceWithPaginationV1 : Pagination() {
        @SerializedName("data")
        @Expose
        var data: List<Invoice>? = null
    }

    class InvoiceDetailResponse : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: Invoice? = null
    }

    class CsvFileData {
        @SerializedName("fileName")
        @Expose
        var fileName: String? = null
            private set

        @SerializedName("fileUrl")
        @Expose
        var fileUrl: String? = null
        fun setFileNameAndUrl(fileName: String?) {
            this.fileName = fileName
        }
    }

    class ExportCsvUrlData : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: CsvFileData? = null
    }

    class InvoiceCountbyStatusResponse : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<Invoice>? = null
    }

    open val invoiceImagesList: List<String>
        get() {
            val imageUrls: MutableList<String> = ArrayList()
            for (imgs in images!!) {
                for (name in imgs.imageFileNames!!) {
                    imageUrls.add(imgs.imageURL + name)
                    Log.e("imageUrls=", "" + imgs.imageURL + name)
                }
            }
            return imageUrls
        }

    fun isStatus(invoiceStatus: Status): Boolean {
        return if (!StringHelper.isStringNullOrEmpty(status)) {
            status == invoiceStatus.toString()
        } else false
    }

    fun isInvoiceType(invoiceType: InvoiceType): Boolean {
        return if (!StringHelper.isStringNullOrEmpty(this.invoiceType)) {
            this.invoiceType == invoiceType.toString()
        } else false
    }

    fun isPaymentStatus(paymentStatus: PaymentStatus): Boolean {
        return if (!StringHelper.isStringNullOrEmpty(this.paymentStatus)) {
            this.paymentStatus == paymentStatus.toString()
        } else false
    }

    fun isUnPaid(invoice: Invoice): Boolean {
        return (invoice.isInvoiceType(InvoiceType.EINVOICE) || invoice.isInvoiceType(InvoiceType.ECREDITNOTE)) && !invoice.isStatus(
            Status.VOIDED
        ) && invoice.isPaymentStatus(PaymentStatus.UNPAID)
    }

    open class LinkedInvoice {
        @SerializedName("invoiceId")
        @Expose
        var invoiceId: String? = null

        @SerializedName("invoiceNum")
        @Expose
        var invoiceNum: String? = null

        @SerializedName("invoiceType")
        @Expose
        var invoiceType: String? = null
    }

    class LinkedCreditNotes : LinkedInvoice()
    inner class OrderData {
        @SerializedName("orderId")
        @Expose
        var orderId: String? = null

        @SerializedName("timeDelivered")
        @Expose
        var timeDelivered: Long? = null
    }

    companion object {
        @JvmStatic
        fun sortByDueDate(invoices: List<Invoice>?) {
            Collections.sort(invoices) { o1, o2 ->
                o1.paymentDueDate!!.compareTo(
                    o2.paymentDueDate!!
                )
            }
        }
    }
}