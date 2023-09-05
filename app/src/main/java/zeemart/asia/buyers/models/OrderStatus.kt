package zeemart.asia.buyers.models

import android.content.Context
import android.widget.TextView
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.order.Orders
import java.util.*

/**
 * Created by ParulBhandari on 12/27/2017.
 */
enum class OrderStatus(val statusName: String, val statusResId: Int) {
    APPROVING("Approving", R.string.order_status_approving), CANCELLING(
        "Cancelling",
        R.string.order_status_cancelling
    ),
    CREATING("Creating", R.string.order_status_processing), REJECTING(
        "Rejecting",
        R.string.order_status_rejecting
    ),
    PENDING_PAYMENT("PendingPayment", R.string.order_status_pending_payment), DRAFT(
        "Draft",
        R.string.order_status_draft
    ),
    CREATED(
        "Created",
        R.string.order_status_pending_approval
    ),
    NEEDS_YOUR_APPROVAL("NeedsYourApproval", R.string.txt_needs_your_approval), INVOICED(
        "Invoiced",
        R.string.order_status_invoiced
    ),
    PLACED("Placed", R.string.order_status_placed), CANCELLED(
        "Cancelled",
        R.string.order_status_cancelled
    ),
    DELETED("Deleted", R.string.order_status_deleted), REJECTED(
        "Rejected",
        R.string.order_status_rejected
    ),
    UNAVAILABLE("Unavailable", R.string.order_status_unavailable), VOIDED(
        "Void",
        R.string.order_status_voided
    );

    object TextFormat {
        @JvmField
        var SentenceCap = 0
        var AllCap = 1
    }

    override fun toString(): String {
        return statusName
    }

    class OrderStatusWithFilter {
        @Expose
        var orderStatus: OrderStatus? = null

        @Expose
        var isFilterSelected = false
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun SetStatusBackground(
            context: Context,
            orders: Orders,
            txtOrderStatus: TextView,
            format: Int = TextFormat.AllCap
        ) {
            val status = orders.orderStatus
            txtOrderStatus.setTextColor(context.resources.getColor(R.color.white))
            if (status == APPROVING.statusName) {
                txtOrderStatus.setText(APPROVING.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_grey)
                txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
            } else if (status == CANCELLING.statusName) {
                txtOrderStatus.setText(CANCELLING.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_grey)
                txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
            } else if (status == CREATING.statusName) {
                txtOrderStatus.setText(CREATING.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_grey)
                txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
            } else if (status == REJECTING.statusName) {
                txtOrderStatus.setText(REJECTING.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_grey)
                txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
            } else if (status == DRAFT.statusName) {
                txtOrderStatus.setText(DRAFT.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_blue)
            } else if (status == CREATED.statusName) {
                if (orders.isUserApprover) {
                    txtOrderStatus.setText(NEEDS_YOUR_APPROVAL.statusResId)
                    txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
                } else {
                    txtOrderStatus.setText(CREATED.statusResId)
                    txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_yellow)
                    txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
                }
            } else if (status == PLACED.statusName || orders.isInvoiced) {
                txtOrderStatus.setText(PLACED.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_green)
            } else if (status == CANCELLED.statusName) {
                txtOrderStatus.setText(CANCELLED.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
            } else if (status == DELETED.statusName) {
                txtOrderStatus.setText(DELETED.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
            } else if (status == REJECTED.statusName) {
                txtOrderStatus.setText(REJECTED.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
            } else if (status == VOIDED.statusName) {
                txtOrderStatus.setText(VOIDED.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
            } else if (status == PENDING_PAYMENT.statusName) {
                txtOrderStatus.setText(PENDING_PAYMENT.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_yellow)
                txtOrderStatus.setTextColor(context.resources.getColor(R.color.black))
            } else {
                txtOrderStatus.setText(UNAVAILABLE.statusResId)
                txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
            }
            txtOrderStatus.text = txtOrderStatus.text.toString().uppercase(Locale.getDefault())
            if (format == TextFormat.SentenceCap) txtOrderStatus.text =
                CommonMethods.SentenceCap(txtOrderStatus.text.toString())
        }

        @JvmStatic
        fun initializeOrderStatusFilters() {
            SharedPref.removeVal(SharedPref.ORDER_FILTER_STATUS_LIST)
            val filterOrderStatusList: MutableList<OrderStatusWithFilter> = ArrayList()
            val orderStatusPlaced = OrderStatusWithFilter()
            val placed = PLACED
            orderStatusPlaced.orderStatus = placed
            orderStatusPlaced.isFilterSelected = false
            filterOrderStatusList.add(orderStatusPlaced)
            val orderStatusCancelled = OrderStatusWithFilter()
            val cancelled = CANCELLED
            orderStatusCancelled.orderStatus = cancelled
            orderStatusCancelled.isFilterSelected = false
            filterOrderStatusList.add(orderStatusCancelled)
            val orderStatusRejected = OrderStatusWithFilter()
            val rejected = REJECTED
            orderStatusRejected.orderStatus = rejected
            orderStatusRejected.isFilterSelected = false
            filterOrderStatusList.add(orderStatusRejected)
            val orderStatusVoided = OrderStatusWithFilter()
            val voided = VOIDED
            orderStatusVoided.orderStatus = voided
            orderStatusVoided.isFilterSelected = false
            filterOrderStatusList.add(orderStatusVoided)
            val filterOrderList = ZeemartBuyerApp.gsonExposeExclusive.toJson(filterOrderStatusList)
            SharedPref.write(SharedPref.ORDER_FILTER_STATUS_LIST, filterOrderList)
        }

        @JvmStatic
        fun setOrderStatusFilterListUpdated(list: List<OrderStatusWithFilter?>?) {
            val orderStatusListString = ZeemartBuyerApp.gsonExposeExclusive.toJson(list)
            SharedPref.write(SharedPref.ORDER_FILTER_STATUS_LIST, orderStatusListString)
        }

        @JvmStatic
        val filterListFromSharedPrefs: List<OrderStatusWithFilter>
            get() {
                val filterStatusList =
                    SharedPref.read(SharedPref.ORDER_FILTER_STATUS_LIST, "")
                return ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    filterStatusList,
                    object : TypeToken<List<OrderStatusWithFilter?>?>() {}.type
                )
            }

        /**
         * return all the selected status name in a Set of Strings for filtering the orders
         *
         * @return
         */
        @JvmStatic
        val statusSelectedForFilters: Set<String>
            get() {
                val selectedOrderStatusForFiltering: MutableSet<String> = HashSet()
                val allOrderStatusFilters = filterListFromSharedPrefs
                if (allOrderStatusFilters != null) {
                    for (i in allOrderStatusFilters.indices) {
                        if (allOrderStatusFilters[i].isFilterSelected) {
                            selectedOrderStatusForFiltering.add(allOrderStatusFilters[i].orderStatus!!.statusName)
                        }
                    }
                }
                return selectedOrderStatusForFiltering
            }

        @JvmStatic
        fun clearAllOrderStatusFilterData() {
            SharedPref.removeVal(SharedPref.ORDER_FILTER_STATUS_LIST)
        }
    }
}