package zeemart.asia.buyers.models

import android.app.PendingIntent

class NotificationBundle {
    var orderId: String? = null
    var stockageId: String? = null
    var announcementId: String? = null
    var orderStatus: String? = null
    var messageTitle: String? = null
    var messageBody: String? = null
    var category: String? = null
    var id = 0
    var pendingIntentNotification: PendingIntent? = null
    var pendingIntentApprove: PendingIntent? = null
    var pendingIntentView: PendingIntent? = null
    var pendingIntentAnnouncement: PendingIntent? = null
    var pendingIntentInvoice: PendingIntent? = null
    var calledFrom: String? = null
    var invoiceId: String? = null
    var status: String? = null
}