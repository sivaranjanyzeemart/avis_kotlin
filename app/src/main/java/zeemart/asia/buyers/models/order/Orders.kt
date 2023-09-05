package zeemart.asia.buyers.models.order

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.R
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.orderimportimport.OrderAmount
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes
import java.util.*

/**
 * Created by ParulBhandari on 12/8/2017.
 */
class Orders {
    enum class Type(val displayName: String, val resId: Int) {
        STANDING("Standing", R.string.order_type_standing),
        DEAL("Deal", R.string.txt_deal),
        ESSENTIALS("Essentials", R.string.txt_my_essentials);
    }

    @SerializedName("transactionImage")
    @Expose
    var transactionImage: TransactionImage? = null

    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null
        get() = if (field == null) {
            Outlet("", "")
        } else field

    @SerializedName("supplier")
    @Expose
    var supplier: Supplier? = null

    @SerializedName("timeCreated")
    @Expose
    private var timeCreated: Long? = null

    @SerializedName("timeUpdated")
    @Expose
    var timeUpdated: Long? = null

    @SerializedName("timePlaced")
    @Expose
    var timePlaced: Long? = null
    // temporary

    // new standard
//        if (timeDelivered==0)
//            return null;
    @SerializedName("timeDelivered")
    @Expose
    var timeDelivered: Long? = null
        get() =// temporary
            if (field == null) 0L else field

    // new standard
//        if (timeDelivered==0)
//            return null;

    @SerializedName("timeRejected")
    @Expose
    var timeRejected: Long? = null

    @SerializedName("timeCancelled")
    @Expose
    var timeCancelled: Long? = null

    @SerializedName("timeReceived")
    @Expose
    var timeReceived: Long? = null

    @SerializedName("timeCutOff")
    @Expose
    var timeCutOff: Long? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: UserDetails? = null

    @SerializedName("approvedBy")
    @Expose
    var approvedBy: UserDetails? = null

    @SerializedName("cancelledBy")
    @Expose
    var cancelledBy: UserDetails? = null

    @SerializedName("rejectedBy")
    @Expose
    var rejectedBy: UserDetails? = null

    @SerializedName("addOns")
    @Expose
    lateinit var addOns: List<String>

    @SerializedName("rejectedRemark")
    @Expose
    var rejectedRemark: String? = null

    @SerializedName("cancelledRemark")
    @Expose
    var cancelledRemark: String? = null

    @SerializedName("approvers")
    @Expose
    var approvers: List<UserDetails>? = null

    @SerializedName("products")
    @Expose
    var products: MutableList<Product>? = null
        get() = if (field == null) {
            ArrayList()
        } else field

    @SerializedName("amount")
    @Expose
    var amount: OrderAmount? = null

    @SerializedName("orderStatus")
    @Expose
    var orderStatus: String? = null

    @SerializedName("orderType")
    @Expose
    var orderType: String? = null
        get() = if (field == null) "" else field

    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("pdfURL")
    @Expose
    var pdfURL: String? = null

    @SerializedName("emailStatusDetails")
    @Expose
    var emailStatusDetails: List<StatusDetail.Email>? = null

    @SerializedName("mobileStatusDetails")
    @Expose
    var mobileStatusDetails: List<StatusDetail.Mobile>? = null

    @SerializedName("whatsappStatusDetails")
    @Expose
    var whatsAppStatusDetails: List<StatusDetail.WhatsApp>? = null

    @SerializedName("deliveryStatus")
    @Expose
    var deliveryStatus: String? = null

    @SerializedName("deliveryInstruction")
    @Expose
    var deliveryInstruction: String? = null

    @SerializedName("promoCode")
    @Expose
    var promoCode: String? = null

    @SerializedName("isInvoiced")
    @Expose
    var isInvoiced = false

    @SerializedName("isLinkedToInvoice")
    @Expose
    var isLinkedToInvoice = false

    @SerializedName("isReceived")
    @Expose
    var isReceived = false

    @SerializedName("isAcknowledged")
    @Expose
    var isAcknowledged = false

    @SerializedName("isAllow")
    @Expose
    var isAllow = false

    @SerializedName("isErrorBelowMoq")
    @Expose
    var isErrorBelowMoq = false

    @SerializedName("linkedInvoices")
    @Expose
    var linkedInvoices: List<LinkedInvoice>? = null

    @SerializedName("outletId")
    @Expose
    var outletId: String? = null

    @SerializedName("supplierId")
    @Expose
    var supplierId: String? = null

    @SerializedName("essentialsId")
    @Expose
    var essentialsId: String? = null

    @SerializedName("dealNumber")
    @Expose
    var dealNumber: String? = null

    @SerializedName("isAddOn")
    @Expose
    var isAddOn = false

    @SerializedName("uploadRequired")
    @Expose
    var isUploadRequired = false

    @SerializedName("linkedOrder")
    @Expose
    var linkedOrder: String? = null

    //Added for the Order History
    var invoiceId: String? = null
    var invoiceNum: String? = null

    @SerializedName("paymentType")
    @Expose
    var paymentType: String? = null

    @SerializedName("grndata")
    @Expose
    var grn: GrnList? = null

    //added for Review draft orders
    var isOrderSelected = false
    var errorMessageFromPlaceDraftsResponse = ""
    var isDeleteOrder = false

    constructor() {}
    constructor(orderId: String?, orderStatus: OrderStatus) {
        this.orderId = orderId
        this.orderStatus = orderStatus.statusName
    }

    constructor(orderStatus: String?) {
        this.orderStatus = orderStatus
    }

    val allStatusDetail: MutableList<StatusDetail.StatusDetailUI>
        get() {
            val newList: MutableList<StatusDetail.StatusDetailUI> = ArrayList()
            if (mobileStatusDetails != null) newList.addAll(mobileStatusDetails!!)
            if (emailStatusDetails != null) newList.addAll(emailStatusDetails!!)
            if (whatsAppStatusDetails != null) newList.addAll(whatsAppStatusDetails!!)
            return newList
        }

    val isBeforeCutOffTime: Boolean
        get() {
            var isBeforeCutOffTime = false
            val calender = Calendar.getInstance(DateHelper.marketTimeZone)
            val currentDate = Date(calender.timeInMillis)
            if (timeCutOff != null) {
                val cutOffDate = Date(timeCutOff!!.toLong() * 1000)
                if (currentDate.before(cutOffDate)) isBeforeCutOffTime = true
            }
            return isBeforeCutOffTime
        }
    val isPlacedCancelledInvoiced: Boolean
        get() {
            if (OrderStatus.PLACED.statusName == orderStatus) return true
            if (OrderStatus.INVOICED.statusName == orderStatus) return true
            return if (OrderStatus.CANCELLED.statusName == orderStatus) true else false
        }
    val isPlacedInvoiced: Boolean
        get() {
            if (OrderStatus.PLACED.statusName == orderStatus) return true
            return if (OrderStatus.INVOICED.statusName == orderStatus) true else false
        }
    val isRejectedCancelled: Boolean
        get() {
            if (OrderStatus.REJECTED.statusName == orderStatus) return true
            if (OrderStatus.CANCELLED.statusName == orderStatus) return true
            return if (OrderStatus.VOIDED.statusName == orderStatus) true else false
        }
    val isUserApprover: Boolean
        get() {
            var isUserApprover = false
            if (approvers != null && approvers!!.size != 0) {
                for (j in approvers!!.indices) {
                    if (SharedPref.read(SharedPref.USER_ID, "") == approvers!![j].id) {
                        isUserApprover = true
                        break
                    }
                }
            }
            return isUserApprover
        }

    class WithPagination : Pagination() {
        @SerializedName("orders")
        @Expose
        var orders: List<Orders>? = null
    }

    val isOrderByUser: Boolean
        get() = if (createdBy?.id == SharedPref.read(SharedPref.USER_ID, "")) {
            true
        } else {
            false
        }
    val isPendingOrderStatusCreatorCheck: Boolean
        get() = if (orderStatus == OrderStatus.CREATED.statusName || orderStatus == OrderStatus.CREATING.statusName || orderStatus == OrderStatus.APPROVING.statusName || orderStatus == OrderStatus.REJECTING.statusName || orderStatus == OrderStatus.CANCELLING.statusName) {
            true
        } else {
            false
        }
    val isPendingOrderStatusApproverCheck: Boolean
        get() = if (orderStatus == OrderStatus.APPROVING.statusName || orderStatus == OrderStatus.REJECTING.statusName) {
            true
        } else {
            false
        }
    val isApprovedByUser: Boolean
        get() = if (approvedBy != null && approvedBy?.id == SharedPref.read(
                SharedPref.USER_ID,
                ""
            )
        ) {
            true
        } else false
    val timeCompare: Long?
        get() = if (timePlaced != null && ((orderStatus == OrderStatus.PLACED.statusName) || (orderStatus) == OrderStatus.INVOICED.statusName)) {
            timePlaced
        } else if (timeRejected != null && orderStatus == OrderStatus.REJECTED.statusName) {
            timeRejected
        } else if (timeCancelled != null && orderStatus == OrderStatus.CANCELLED.statusName) {
            timeCancelled
        } else if (timeUpdated != null) {
            timeUpdated
        } else {
            timeCreated
        }

    class LinkedInvoice {
        @SerializedName("invoiceId")
        @Expose
        var invoiceId: String? = null

        @SerializedName("invoiceNum")
        @Expose
        var invoiceNum: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null
        fun isStatus(invoiceStatus: Invoice.Status): Boolean {
            return if (!StringHelper.isStringNullOrEmpty(status)) {
                status == invoiceStatus.toString()
            } else false
        }

        override fun toString(): String {
            return "LinkedInvoice{" +
                    "invoiceId='" + invoiceId + '\'' +
                    ", invoiceNum='" + invoiceNum + '\'' +
                    ", status='" + status + '\'' +
                    '}'
        }
    }

    val isPromoCodeApplied: Boolean
        get() = if (!StringHelper.isStringNullOrEmpty(promoCode)) {
            true
        } else {
            false
        }
    val isSpecialNotesAvailable: Boolean
        get() = if (!StringHelper.isStringNullOrEmpty(notes)) {
            true
        } else {
            false
        }

    inner class TransactionImage {
        @SerializedName("imageURL")
        @Expose
        var imageURL: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null
    }

    enum class PaymentStatus(val statusName: String) {
        UPLOADED("UPLOADED"), REJECTED("REJECTED"), UNAVAILABLE("APPROVED");

        override fun toString(): String {
            return statusName
        }
    }

    inner class GrnList {
        @SerializedName("grns")
        @Expose
        var grnList: List<Grn>? = null
    }

    inner class Grn {
        @SerializedName("grnId")
        @Expose
        var grnId: String? = null

        @SerializedName("orderId")
        @Expose
        var orderId: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("products")
        @Expose
        var products: List<Product>? = null

        @SerializedName("customLineItems")
        @Expose
        var customLineItems: List<GrnRequest.CustomLineItem>? = null

        @SerializedName("timeCreated")
        @Expose
        var timeCreated: Long? = null

        @SerializedName("timeReceived")
        @Expose
        var timeReceived: Long? = null

        @SerializedName("timeEmailSent")
        @Expose
        var emailDate: Long? = null

        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("pdfURL")
        @Expose
        var pdfURL: String? = null

        @SerializedName("sendEmailToSupplier")
        @Expose
        var isSendEmailToSupplier = false

        @SerializedName("supplierNotificationEmail")
        @Expose
        var supplierNotificationEmail: String? = null

        @SerializedName("noteToSupplier")
        @Expose
        var noteToSupplier: String? = null

        @SerializedName("createdBy")
        @Expose
        var createdBy: RetrieveSpecificAnnouncementRes.Announcements.CreatedBy? = null
    }

    companion object {
        @JvmStatic
        fun sortForNearestTimeDelivery(orders: List<Orders>?) {
            Collections.sort(orders) { o1, o2 -> o1.timeDelivered!!.compareTo(o2.timeDelivered!!) }
        }
    }
}