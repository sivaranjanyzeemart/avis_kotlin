package zeemart.asia.buyers.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants

class NotificationBroadCastReceiverHelper constructor(
    private val context: Context?,
    private val onNotificationReceivedListner: OnNotificationReceivedListner
) {
    private val broadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        public override fun onReceive(context: Context, intent: Intent) {
            var orderStatus: String? = null
            var orderId: String? = null
            val bundle: Bundle? = intent.getExtras()
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.ORDER_ID)) {
                    orderId = bundle.getString(ZeemartAppConstants.ORDER_ID)
                }
                if (bundle.containsKey(ZeemartAppConstants.ORDER_STATUS)) {
                    orderStatus = bundle.getString(ZeemartAppConstants.ORDER_STATUS)
                }
            }
            if (orderStatus != null && orderId != null) onNotificationReceivedListner.onNotificationReceived(
                orderId,
                orderStatus
            )
        }
    }

    fun registerReceiver() {
        if (context != null && broadcastReceiver != null) LocalBroadcastManager.getInstance(context)
            .registerReceiver(
                broadcastReceiver,
                IntentFilter(NotificationConstants.NOTIFICATION_BROADCAST_INTENT)
            )
    }

    fun unRegisterReceiver() {
        if (context != null && broadcastReceiver != null) LocalBroadcastManager.getInstance(context)
            .unregisterReceiver(broadcastReceiver)
    }

    open interface OnNotificationReceivedListner {
        fun onNotificationReceived(orderId: String?, orderStatus: String?)
    }
}