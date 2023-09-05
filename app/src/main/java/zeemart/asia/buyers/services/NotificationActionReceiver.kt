package zeemart.asia.buyers.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.android.volley.VolleyError
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.orderimport.RetrieveOrderDetails
import zeemart.asia.buyers.network.ApproveRejectOrders
import zeemart.asia.buyers.network.GetOrderDetails
import zeemart.asia.buyers.network.GetOrderDetails.GetOrderDetailedDataListener
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.OrderProcessingPlacedActivity

/**
 * Created by RajPrudhviMarella on 6/09/2019.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var orderId: String? = null
        var outletId: String? = null
        var notificationId = 0
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_ID)) {
                orderId = bundle.getString(ZeemartAppConstants.ORDER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID)) {
                outletId = bundle.getString(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID)
            }
            if (bundle.containsKey(NotificationConstants.ZEEMART_NOTIF_ID)) {
                notificationId = bundle.getInt(NotificationConstants.ZEEMART_NOTIF_ID)
            }
        }
        if (notificationId > 0) {
            val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.cancel(notificationId)
        }
        val action = intent.action
        if (NotificationConstants.NOTIFICATION_ACTION_APPROVE == action) {
            if (!StringHelper.isStringNullOrEmpty(orderId) && !StringHelper.isStringNullOrEmpty(
                    outletId
                )
            ) callGetOrderDetails(orderId, outletId, context)
        } else if (NotificationConstants.NOTIFICATION_ACTION_VIEW == action) {
            val newIntent = Intent(context, OrderDetailsActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            newIntent.putExtra(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID, outletId)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(newIntent)
        }
    }

    private fun callGetOrderDetails(orderId: String?, outletId: String?, context: Context) {
        //call the order detail API
        GetOrderDetails(context, object : GetOrderDetailedDataListener {
            override fun onErrorResponse(error: VolleyError?) {}
            override fun onSuccessResponse(orders: RetrieveOrderDetails?) {
                if (orders != null && orders.data != null) {
                    val mOrder = orders.data
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_ORDER_REVIEW_APPROVE_ORDER,
                        mOrder!!
                    )
                    ApproveRejectOrders.ApproveOrder(
                        context,
                        mOrder,
                        object : ApproveRejectOrders.GetResponseStatusListener {
                            override fun onSuccessResponse(status: String?) {
                                val newIntent =
                                    Intent(context, OrderProcessingPlacedActivity::class.java)
                                newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder.orderId)
                                newIntent.putExtra(
                                    ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                    mOrder.outlet?.outletId
                                )
                                newIntent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM,
                                    ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
                                )
                                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(newIntent)
                            }

                            override fun onErrorResponse(error: VolleyError?) {}
                        })
                }
            }
        }).getOrderDetailsData(orderId, outletId!!)
    }
}