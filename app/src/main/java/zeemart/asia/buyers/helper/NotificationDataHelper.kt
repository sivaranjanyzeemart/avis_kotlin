package zeemart.asia.buyers.helper

import android.content.Context
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.NotificationDetail
import zeemart.asia.buyers.models.NotificationDetailMgr
import zeemart.asia.buyers.models.NotificationRead
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes
import zeemart.asia.buyers.network.UsersApi.editSingleUser
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 11/24/2017.
 */
class NotificationDataHelper constructor(notificationDetailsList: MutableList<NotificationDetailMgr>) {
    private val notificationDetailsList: MutableList<NotificationDetailMgr>

    init {
        this.notificationDetailsList = notificationDetailsList
    }

    open interface GetNotificationStatus {
        fun onSuccessResponse(isChecked: Boolean)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }
    /**
     * return the RequestBody For Notification Status String
     *
     * @param isChecked
     * @return
     */
    //    public static String createRequestBodyForNotificationSetting(boolean isChecked) {
    //        UserDetails userDetails = SharedPref.getCurrentUserDetail();
    //        userDetails.setTrackNotificationsStatus(isChecked);
    //        List<UserDetails> userList = new ArrayList();
    //        userList.add(userDetails);
    //        return ZeemartBuyerApp.gsonExposeExclusive.toJson(userList);
    //    }
    /**
     * return the arraylist of notification data segregated by date weekDay and time in the
     * NotificationDetailMgr model class
     *
     * @return
     */
    val notificationDataInAppFormat: List<Any>
        get() {
            if (notificationDetailsList.size > 0) {
                var item: NotificationDetailMgr = NotificationDetailMgr()
                item.isHeader = true
                val date: String? =
                    DateHelper.getDateInDateMonthYearFormat(notificationDetailsList.get(0).dateTime)
                val day: String? =
                    DateHelper.getDateIn3LetterDayFormat(notificationDetailsList.get(0).dateTime)
                var headerDate: String = date.toString()
                item.headerDate = date
                item.headerDay = day
                item.dateTime = notificationDetailsList.get(0).dateTime
                notificationDetailsList.add(0, item)
                for (i in notificationDetailsList.indices) {
                    item = NotificationDetailMgr()
                    item.isHeader = true
                    val dateHeader: String? =
                        DateHelper.getDateInDateMonthYearFormat(notificationDetailsList.get(i).dateTime)
                    val dayHeader: String? =
                        DateHelper.getDateIn3LetterDayFormat(notificationDetailsList.get(i).dateTime)
                    if (!headerDate.equals(dateHeader, ignoreCase = true)) {
                        headerDate = dateHeader.toString()
                        item.headerDate = dateHeader
                        item.headerDay = dayHeader
                        item.dateTime = notificationDetailsList.get(i).dateTime
                        notificationDetailsList.add(i, item)
                    }
                }
            }
            return notificationDetailsList
        }

    /**
     * return the unread notification arraylist
     *
     * @param notifications
     * @return
     */
    fun returnUnreadNotifications(notifications: List<NotificationDetail>?): List<NotificationRead> {
        val listUnreadNotifications: MutableList<NotificationRead> = ArrayList<NotificationRead>()
        if (notifications != null && notifications.size > 0) {
            for (i in notifications.indices) {
                if (!notifications.get(i).read!!) {
                    val unreadNotif: NotificationRead = NotificationRead()
                    unreadNotif.id = notifications.get(i).id
                    unreadNotif.isRead
                    listUnreadNotifications.add(unreadNotif)
                }
            }
        }
        return listUnreadNotifications
    }

    companion object {
        fun callNotificationApi(
            isChecked: Boolean,
            getNotificationStatus: GetNotificationStatus,
            context: Context?,
        ) {
            val userDetails: UserDetails = UserDetails()
            userDetails.id = SharedPref.currentUserDetail.id
            userDetails.trackNotificationsStatus = isChecked
            editSingleUser(context, userDetails, object : GetRequestStatusResponseListener {
                override fun onSuccessResponse(status: String?) {
                    getNotificationStatus.onSuccessResponse(isChecked)
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    getNotificationStatus.onErrorResponse(error)
                }
            })
        }

        @JvmStatic
        fun callInventorySettingsApi(
            isChecked: Boolean,
            getNotificationStatus: GetNotificationStatus,
            context: Context?,
        ) {
            val userDetails: UserDetails = UserDetails()
            userDetails.id = SharedPref.read(SharedPref.USER_ID, "")
            userDetails.customNameAndCode = isChecked
            editSingleUser(context, userDetails, object : GetRequestStatusResponseListener {
                override fun onSuccessResponse(status: String?) {
                    getNotificationStatus.onSuccessResponse(isChecked)
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    getNotificationStatus.onErrorResponse(error)
                }
            })
        }

        @JvmStatic
        fun callREportsSettingsApi(
            isChecked: Boolean,
            getNotificationStatus: GetNotificationStatus,
            context: Context?,
        ) {
            val userDetails: UserDetails = UserDetails()
            userDetails.id = SharedPref.read(SharedPref.USER_ID, "")
            userDetails.doNotSendReports = isChecked
            //        UserInventorySettings inventorySettings = new UserInventorySettings();
//        inventorySettings.setDisplayCustomName(isChecked);
//        UserSettings userSettings = new UserSettings();
//        userSettings.setInventorySettings(inventorySettings);
//        userDetails.setSettings(userSettings);
            //        UserInventorySettings inventorySettings = new UserInventorySettings();
//        inventorySettings.setDisplayCustomName(isChecked);
//        UserSettings userSettings = new UserSettings();
//        userSettings.setInventorySettings(inventorySettings);
//        userDetails.setSettings(userSettings);
            //        UserInventorySettings inventorySettings = new UserInventorySettings();
//        inventorySettings.setDisplayCustomName(isChecked);
//        UserSettings userSettings = new UserSettings();
//        userSettings.setInventorySettings(inventorySettings);
//        userDetails.setSettings(userSettings);
            editSingleUser(context, userDetails, object : GetRequestStatusResponseListener {
                override fun onSuccessResponse(status: String?) {
                    getNotificationStatus.onSuccessResponse(isChecked)
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    getNotificationStatus.onErrorResponse(error)
                }
            })
        }

        /**
         * return the unread notification arraylist
         *
         * @param notifications
         * @return
         */
        fun returnUnreadNotificationAnnouncements(notifications: List<RetrieveSpecificAnnouncementRes.Announcements>?): List<String> {
            val listAnnouncementId: MutableList<String> = ArrayList()
            if (notifications != null && notifications.size > 0) {
                for (i in notifications.indices) {
                    if ((notifications.get(i).isRead != null) && (!notifications.get(i)
                            .isRead!!)
                    ) {
                        listAnnouncementId.add(notifications.get(i).announcementId!!)
                    }
                }
            }
            return listAnnouncementId
        }
    }
}