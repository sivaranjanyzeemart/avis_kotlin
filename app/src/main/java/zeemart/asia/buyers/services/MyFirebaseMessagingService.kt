package zeemart.asia.buyers.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.intercom.android.sdk.push.IntercomPushClient
import org.json.JSONException
import org.json.JSONObject
import zeemart.asia.buyers.R
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.MixPanelHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.AdjustmentRecordActivity
import zeemart.asia.buyers.inventory.InventoryDataManager
import zeemart.asia.buyers.invoices.RejectedInvoiceActivity
import zeemart.asia.buyers.login.BuyerLoginActivity
import zeemart.asia.buyers.models.NotificationBundle
import zeemart.asia.buyers.notifications.NotificationAnnouncementDetails
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.OrderSettingsActivity

/**
 * Created by saiful on 22/1/18.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
     val intercomPushClient = IntercomPushClient()
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d("NEW_TOKEN", s)
        //Get hold of the registration token
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.d(TAG, "Refreshed token: $s")
        //save the device token in SharedPreference
        SharedPref.write(SharedPref.NOTIFICATION_DEVICE_TOKEN, s)
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(NotificationConstants.REFRESH_TOKEN_RECEIVED))
        intercomPushClient.sendTokenToIntercom(
            application,
            SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")!!
        )
        //send token to clevertap
        MixPanelHelper.registerFCM(applicationContext, s)
    }

    /**
     * this method will be called only when app is in the foreground
     * for handling both the notification and the data payload in the Message
     * otherwise when the app is in the background the launcher activity
     * getIntent().getExtras will contain the data payload in extras
     * and notification payload will be handles by FCM SDK and Android system
     *
     * @param remoteMessage
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            //Log data to Log Cat
            Log.d(TAG, "From: " + remoteMessage.from)
            Log.d(TAG, "Data: " + remoteMessage.data)
            //notification from intercom
            //notification from intercom
            val message: Map<String, String> = remoteMessage.data
            if (intercomPushClient.isIntercomPush(message)) {
                intercomPushClient.handlePush(application, message)
                return
            }
            //notification is from clevertap
            val extras = Bundle()
            for ((key, value) in remoteMessage.data) {
                extras.putString(key, value)
            }
            //            NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);
//            if (((NotificationInfo) info).fromCleverTap) {
//                CleverTapAPI.createNotificationChannel(getApplicationContext(), DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, DEFAULT_CHANNEL_DESC, NotificationManager.IMPORTANCE_MAX, true);
//                CleverTapAPI.createNotification(getApplicationContext(), extras, DEFAULT_NOTIFICATION_ID);
//                return;
//            }
            //notification from Zeemart
            val notificationBundle = NotificationBundle()
            if (remoteMessage.notification != null && remoteMessage.notification!!.body != null) {
                notificationBundle.messageBody = remoteMessage.notification!!.body
            }
            if (remoteMessage.notification != null && remoteMessage.notification!!.title != null) {
                notificationBundle.messageTitle = remoteMessage.notification!!.title
            }
            //create notification
            createNotification(notificationBundle, remoteMessage.data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotification(
        notificationBundle: NotificationBundle,
        notificationData: Map<String, String>,
    ) {
        try {
            notificationBundle.messageBody = notificationData[NotificationConstants.BODY_KEY]
            notificationBundle.messageTitle = notificationData[NotificationConstants.TITLE_KEY]
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!StringHelper.isStringNullOrEmpty(SharedPref.read(SharedPref.PASSWORD_ENCRYPTED, ""))) {
            /**
             * Handles the notifications associated with the user since the user is logged in
             */
            if (notificationData.containsKey(NotificationConstants.URI_KEY)) {
                var type: String? = null
                try {
                    val jsonObjURI = JSONObject(notificationData[NotificationConstants.URI_KEY])
                    type = jsonObjURI.getString("type")
                    val jsonObjectParameters = jsonObjURI.getJSONObject("parameters")
                    if (type != null && type == NotificationConstants.ORDER_DETAILS) {
                        notificationBundle.orderId = jsonObjectParameters.getString("orderId")
                        notificationBundle.orderStatus =
                            jsonObjectParameters.getString("orderStatus")
                    }
                    if (type != null && type == NotificationConstants.ADJUSTMENT) {
                        notificationBundle.stockageId = jsonObjectParameters.getString("stockageId")
                    }
                    if (type != null && type == NotificationConstants.INVOICE_DETAILS) {
                        notificationBundle.invoiceId = jsonObjectParameters.getString("invoiceId")
                        notificationBundle.status = jsonObjectParameters.getString("status")
                    }
                    notificationBundle.announcementId =
                        jsonObjectParameters.getString("announcementId")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if (type != null && type == NotificationConstants.ORDER_DETAILS && notificationBundle.orderId != null && notificationData.containsKey(
                        NotificationConstants.OUTLETID_KEY
                    )
                ) {
                    val intent = Intent(this, OrderDetailsActivity::class.java)
                    intent.putExtra(ZeemartAppConstants.ORDER_ID, notificationBundle.orderId)
                    intent.putExtra(
                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                        notificationData[NotificationConstants.OUTLETID_KEY]
                    )
                    intent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        NotificationConstants.CALLED_FROM_ORDER_DETAILS
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    //add this otherwise extras are not passed to the activity
                    intent.action = java.lang.Long.toString(System.currentTimeMillis())
                    val resultIntent = PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
                    )
                    val id =
                        (NotificationConstants.ZEEMART_NOTIF_ID + System.currentTimeMillis()).hashCode()
                    notificationBundle.pendingIntentNotification = resultIntent
                    if (notificationData.containsKey(NotificationConstants.CATEGORY) && notificationData[NotificationConstants.CATEGORY] == NotificationConstants.CATEGORY_APPROVAL) {
                        val intentApprove = Intent(this, NotificationActionReceiver::class.java)
                        intentApprove.action = NotificationConstants.NOTIFICATION_ACTION_APPROVE
                        intentApprove.putExtra(
                            ZeemartAppConstants.ORDER_ID,
                            notificationBundle.orderId
                        )
                        intentApprove.putExtra(NotificationConstants.ZEEMART_NOTIF_ID, id)
                        intentApprove.putExtra(
                            ZeemartAppConstants.CALLED_FROM,
                            NotificationConstants.CALLED_FROM_ORDER_DETAILS
                        )
                        intentApprove.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            notificationData[NotificationConstants.OUTLETID_KEY]
                        )
                        intentApprove.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        val pendingIntentApprove: PendingIntent
                        pendingIntentApprove = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PendingIntent.getBroadcast(
                                this,
                                0,
                                intentApprove,
                                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        } else {
                            PendingIntent.getBroadcast(
                                this,
                                0,
                                intentApprove,
                                PendingIntent.FLAG_CANCEL_CURRENT
                            )
                        }
                        val intentView = Intent(this, NotificationActionReceiver::class.java)
                        intentView.action = NotificationConstants.NOTIFICATION_ACTION_VIEW
                        intentView.putExtra(
                            ZeemartAppConstants.ORDER_ID,
                            notificationBundle.orderId
                        )
                        intentView.putExtra(NotificationConstants.ZEEMART_NOTIF_ID, id)
                        intentView.putExtra(
                            ZeemartAppConstants.CALLED_FROM,
                            NotificationConstants.CALLED_FROM_ORDER_DETAILS
                        )
                        intentView.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            notificationData[NotificationConstants.OUTLETID_KEY]
                        )
                        intentView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        var pendingIntentView: PendingIntent? = null
                        pendingIntentView = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PendingIntent.getBroadcast(
                                this,
                                1,
                                intentView,
                                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        } else {
                            PendingIntent.getBroadcast(
                                this,
                                1,
                                intentView,
                                PendingIntent.FLAG_CANCEL_CURRENT
                            )
                        }
                        notificationBundle.pendingIntentView = pendingIntentView
                        notificationBundle.pendingIntentApprove = pendingIntentApprove
                        notificationBundle.category = NotificationConstants.CATEGORY_APPROVAL
                    }
                    notificationBundle.id = id
                    notificationBundle.calledFrom = NotificationConstants.CALLED_FROM_ORDER_DETAILS
                    sendNotification(notificationBundle)
                    val intentNotification =
                        Intent(NotificationConstants.NOTIFICATION_BROADCAST_INTENT)
                    intentNotification.putExtra(
                        ZeemartAppConstants.ORDER_ID,
                        notificationBundle.orderId
                    )
                    intentNotification.putExtra(
                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                        notificationData[NotificationConstants.OUTLETID_KEY]
                    )
                    intentNotification.putExtra(
                        ZeemartAppConstants.ORDER_STATUS,
                        notificationBundle.orderStatus
                    )
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentNotification)
                } else if (type != null && type == NotificationConstants.ANNOUNCEMENTS && notificationData.containsKey(
                        NotificationConstants.OUTLETID_KEY
                    )
                ) {
                    val id =
                        (NotificationConstants.ZEEMART_NOTIF_ID + System.currentTimeMillis()).hashCode()
                    val intentAnnouncement =
                        Intent(this, NotificationAnnouncementDetails::class.java)
                    intentAnnouncement.putExtra(
                        ZeemartAppConstants.ANNOUNCEMENT_ID,
                        notificationBundle.announcementId
                    )
                    intentAnnouncement.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        NotificationConstants.CALLED_FROM_ANNOUNCEMENT_DETAILS
                    )
                    intentAnnouncement.action = java.lang.Long.toString(System.currentTimeMillis())
                    val resultIntent: PendingIntent
                    resultIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.getActivity(
                            this,
                            0,
                            intentAnnouncement,
                            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        PendingIntent.getActivity(
                            this,
                            0,
                            intentAnnouncement,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    }
                    notificationBundle.id = id
                    notificationBundle.calledFrom =
                        NotificationConstants.CALLED_FROM_ANNOUNCEMENT_DETAILS
                    notificationBundle.pendingIntentAnnouncement = resultIntent
                    sendNotification(notificationBundle)
                    val intentNotification =
                        Intent(NotificationConstants.NOTIFICATION_BROADCAST_INTENT_ANNOUNCEMENT)
                    intentNotification.putExtra(
                        ZeemartAppConstants.ANNOUNCEMENT_ID,
                        notificationBundle.announcementId
                    )
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentNotification)
                } else if (type != null && type == NotificationConstants.INVOICE_DETAILS && notificationBundle.invoiceId != null && notificationData.containsKey(
                        NotificationConstants.OUTLETID_KEY
                    )
                ) {
                    val id =
                        (NotificationConstants.ZEEMART_NOTIF_ID + System.currentTimeMillis()).hashCode()
                    val intentRejectedInvoice =
                        Intent(applicationContext, RejectedInvoiceActivity::class.java)
                    intentRejectedInvoice.putExtra(
                        ZeemartAppConstants.INVOICE_ID,
                        notificationBundle.invoiceId
                    )
                    intentRejectedInvoice.action =
                        java.lang.Long.toString(System.currentTimeMillis())
                    val resultIntent: PendingIntent
                    resultIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.getActivity(
                            this,
                            0,
                            intentRejectedInvoice,
                            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        PendingIntent.getActivity(
                            this,
                            0,
                            intentRejectedInvoice,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    }
                    notificationBundle.id = id
                    notificationBundle.calledFrom =
                        NotificationConstants.CALLED_FROM_INVOICE_DETAILS
                    notificationBundle.pendingIntentInvoice = resultIntent
                    sendNotification(notificationBundle)
                    val intentNotification =
                        Intent(NotificationConstants.NOTIFICATION_BROADCAST_INTENT_INVOICE)
                    intentNotification.putExtra(
                        ZeemartAppConstants.INVOICE_ID,
                        notificationBundle.invoiceId
                    )
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentNotification)
                } else if (type != null && type == NotificationConstants.ADJUSTMENT && notificationBundle.stockageId != null) {
                    val id =
                        (NotificationConstants.ZEEMART_NOTIF_ID + System.currentTimeMillis()).hashCode()
                    val intentRejectedInvoice =
                        Intent(applicationContext, AdjustmentRecordActivity::class.java)
                    intentRejectedInvoice.putExtra(
                        InventoryDataManager.INTENT_STOCKAGE_ID,
                        notificationBundle.stockageId
                    )
                    intentRejectedInvoice.action =
                        java.lang.Long.toString(System.currentTimeMillis())
                    val resultIntent: PendingIntent
                    resultIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.getActivity(
                            this,
                            0,
                            intentRejectedInvoice,
                            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        PendingIntent.getActivity(
                            this,
                            0,
                            intentRejectedInvoice,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    }
                    notificationBundle.id = id
                    notificationBundle.calledFrom = NotificationConstants.CALLED_FROM_ORDER_DETAILS
                    notificationBundle.pendingIntentNotification = resultIntent
                    sendNotification(notificationBundle)
                    val intentNotification =
                        Intent(NotificationConstants.NOTIFICATION_BROADCAST_INTENT)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentNotification)
                } else if (type != null && type == NotificationConstants.PENDING_SUPPLIER_SETTINGS_REVIEW) {
                    val id =
                        (NotificationConstants.ZEEMART_NOTIF_ID + System.currentTimeMillis()).hashCode()
                    val intentRejectedInvoice =
                        Intent(applicationContext, OrderSettingsActivity::class.java)
                    intentRejectedInvoice.action =
                        java.lang.Long.toString(System.currentTimeMillis())
                    val resultIntent: PendingIntent
                    resultIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.getActivity(
                            this,
                            0,
                            intentRejectedInvoice,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        PendingIntent.getActivity(
                            this, 0, intentRejectedInvoice,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
                        )
                    }
                    notificationBundle.id = id
                    notificationBundle.calledFrom = NotificationConstants.CALLED_FROM_ORDER_DETAILS
                    notificationBundle.pendingIntentNotification = resultIntent
                    sendNotification(notificationBundle)
                    val intentNotification =
                        Intent(NotificationConstants.NOTIFICATION_BROADCAST_INTENT)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentNotification)
                }
            }
        } else {
            notificationBundle.id = DEFAULT_NOTIFICATION_ID
            sendNotification(notificationBundle)
        }
    }

    private fun sendNotification(notificationBundle: NotificationBundle) {
        val mBuilder = NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
        if (StringHelper.isStringNullOrEmpty(notificationBundle.calledFrom)) {
            val intent = Intent(applicationContext, BuyerLoginActivity::class.java)
            if (notificationBundle.calledFrom.equals(
                    NotificationConstants.CALLED_FROM_ANNOUNCEMENT_DETAILS,
                    ignoreCase = true
                )
            ) {
                val pendingIntentAnnouncement = PendingIntent.getActivity(this, 0, intent, 0)
                notificationBundle.pendingIntentAnnouncement = pendingIntentAnnouncement
            } else if (notificationBundle.calledFrom.equals(
                    NotificationConstants.CALLED_FROM_INVOICE_DETAILS,
                    ignoreCase = true
                )
            ) {
                val pendingIntentInvoice = PendingIntent.getActivity(this, 0, intent, 0)
                notificationBundle.pendingIntentInvoice = pendingIntentInvoice
            } else {
                val pendingIntentNotification = PendingIntent.getActivity(this, 0, intent, 0)
                notificationBundle.pendingIntentNotification = pendingIntentNotification
            }
        }
        if (!StringHelper.isStringNullOrEmpty(notificationBundle.category) && notificationBundle.category == NotificationConstants.CATEGORY_APPROVAL) {
            mBuilder.addAction(
                0,
                getString(R.string.txt_approve),
                notificationBundle.pendingIntentApprove
            )
            mBuilder.addAction(
                0,
                getString(R.string.txt_view),
                notificationBundle.pendingIntentView
            )
        } else {
            mBuilder.setContentTitle(notificationBundle.messageTitle)
            mBuilder.setContentText(notificationBundle.messageBody)
        }
        if (notificationBundle.calledFrom.equals(
                NotificationConstants.CALLED_FROM_ANNOUNCEMENT_DETAILS,
                ignoreCase = true
            )
        ) {
            mBuilder.setContentIntent(notificationBundle.pendingIntentAnnouncement)
        } else if (notificationBundle.calledFrom.equals(
                NotificationConstants.CALLED_FROM_INVOICE_DETAILS,
                ignoreCase = true
            )
        ) {
            mBuilder.setContentIntent(notificationBundle.pendingIntentInvoice)
        } else {
            mBuilder.setContentIntent(notificationBundle.pendingIntentNotification)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.notification_icon)
            mBuilder.color = resources.getColor(R.color.colorPrimaryDark)
        } else {
            mBuilder.setSmallIcon(R.drawable.notification_icon)
        }
        mBuilder.setContentTitle(notificationBundle.messageTitle)
        mBuilder.setContentText(notificationBundle.messageBody)
        mBuilder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(notificationBundle.messageBody)
        )
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        mBuilder.priority = Notification.PRIORITY_MAX
        mBuilder.setAutoCancel(true)
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                notificationBundle.messageTitle, NotificationManager.IMPORTANCE_DEFAULT
            )
            mNotificationManager.createNotificationChannel(channel)
        }
        mNotificationManager.notify(notificationBundle.id, mBuilder.build())
    }

    companion object {
        private const val TAG = "MyAndroidFCMService"
        private const val DEFAULT_CHANNEL_ID = "CHANNEL1"
        private const val DEFAULT_CHANNEL_NAME = "default"
        private const val DEFAULT_CHANNEL_DESC = "default"
        private const val DEFAULT_NOTIFICATION_ID = 0
    }
}