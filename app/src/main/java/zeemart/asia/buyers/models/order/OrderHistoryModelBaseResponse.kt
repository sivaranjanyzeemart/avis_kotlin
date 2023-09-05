package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.order.Orders

/**
 * Created by ParulBhandari on 4/6/2018.
 */
class OrderHistoryModelBaseResponse {
    @SerializedName("data")
    @Expose
    var data: List<OrderHistoryModel>? = null

    class OrderHistoryModel {
        @SerializedName("orderId")
        @Expose
        var orderId: String? = null

        @SerializedName("activity")
        @Expose
        var orderHistoryActivityName: String? = null

        @SerializedName("previousValue")
        @Expose
        var previousValue: Orders? = null

        @SerializedName("updatedValue")
        @Expose
        var updatedValue: Orders? = null

        @SerializedName("timeActivity")
        @Expose
        var timeActivity: Long? = null

        @SerializedName("activityUser")
        @Expose
        var activityUser: UserDetails? = null

        @SerializedName("activityMessage")
        @Expose
        var activityMessage: String? = null

        @SerializedName("activityRemark")
        @Expose
        var activityRemark: String? = null

        @SerializedName("activityAction")
        @Expose
        var activityAction: ActivityAction? = null

        inner class ActivityAction {
            @SerializedName("invoiceId")
            @Expose
            var invoiceId: String? = null

            @SerializedName("invoiceNum")
            @Expose
            var invoiceNum: String? = null

            @SerializedName("pdfURL")
            @Expose
            var pdfURL: String? = null
        }

        enum class OrderActivityName(var orderActivityType: String) {
            ORDER_DRAFTED("ORDER_DRAFTED"), ORDER_CREATING("ORDER_CREATING"), ORDER_EDITED("ORDER_EDITED"), ORDER_DIRECTLY_PLACED(
                "ORDER_DIRECTLY_PLACED"
            ),
            ORDER_SENT_TO_APPROVAL("ORDER_SENT_TO_APPROVAL"), ORDER_SENT_TO_NEXT_LEVEL_APPROVAL("ORDER_SENT_TO_NEXT_LEVEL_APPROVAL"), ORDER_EDIT_AND_APPROVING(
                "ORDER_EDIT_AND_APPROVING"
            ),
            ORDER_APPROVING("ORDER_APPROVING"), ORDER_APPROVED_PLACED("ORDER_APPROVED_PLACED"), ORDER_REJECTING(
                "ORDER_REJECTING"
            ),
            ORDER_REJECTED("ORDER_REJECTED"), ORDER_REJECTED_BY_SYSTEM("ORDER_REJECTED_BY_SYSTEM"), ORDER_CANCELLING(
                "ORDER_CANCELLING"
            ),
            ORDER_CANCELLED("ORDER_CANCELLED"), ORDER_LINKED_TO_INVOICE("ORDER_LINKED_TO_INVOICE"), LINKED_INVOICE_VOIDED(
                "LINKED_INVOICE_VOIDED"
            ),
            ORDER_MARKED_AS_RECEIVED("ORDER_MARKED_AS_RECEIVED"), ORDER_DELETING("ORDER_DELETING"), ORDER_DELETED(
                "ORDER_DELETED"
            ),
            ORDER_CANCEL_NOTIFIED_TO_SUPPLIER("ORDER_CANCEL_NOTIFIED_TO_SUPPLIER"), ORDER_SENT_TO_SUPPLIER_WITH_PDF(
                "ORDER_SENT_TO_SUPPLIER_WITH_PDF"
            ),
            ORDER_ACTION_UNAUTHORIZED("ORDER_ACTION_UNAUTHORIZED");

            override fun toString(): String {
                return orderActivityType
            }
        }

        fun isOrderHistoryActivityName(orderActivityName: OrderActivityName): Boolean {
            return if (!StringHelper.isStringNullOrEmpty(orderHistoryActivityName)) {
                orderHistoryActivityName == orderActivityName.toString()
            } else false
        }
    }
}