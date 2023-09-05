package zeemart.asia.buyers.models.order

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.R

abstract class StatusDetail {
    interface StatusDetailUI {
        val displayName: String?
        val displayStatus: String?
        val displayTimestamp: String?
        val displayStatusIconResId: Int
        val statusResourceString: Int
        val statusTextColorResId: Int
    }

    class Email(
        override val displayName: String?,
        override val displayStatus: String?,
        override val displayTimestamp: String?,
        override val displayStatusIconResId: Int,
        override val statusResourceString: Int,
        override val statusTextColorResId: Int
    ) : StatusDetailUI {

        enum class Status(var name_: String, var resId: Int) {
            PENDING("Pending", R.string.receipt_status_email_pending), FAILED(
                "Failed",
                R.string.txt_failed
            ),
            DELIVERED("Delivered", R.string.receipt_status_email_delivered), OPENED(
                "Opened",
                R.string.receipt_status_email_opened
            ),
            NOT_AVAILABLE("Not Available", R.string.receipt_status_email_not_available);

            open fun Status(name: String?, resId: Int) {
                this.name_ = name!!
                this.resId = resId
            }

            open fun getName(): String? {
                return name
            }

//            open fun getResId(): Int {
//                return resId
//            }
        }

        @SerializedName("emailId")
        @Expose
        var emailId: String? = null

        @SerializedName("status")
        @Expose
        private var status: String? = null

        @SerializedName("message_id")
        @Expose
        private var message_id: String? = null

        @SerializedName("timestamp")
        @Expose
        private var timestamp: Long? = null

        @SerializedName("emailType")
        @Expose
        private var emailType: String? = null


        fun isSent(): Boolean {
            if (status.equals(Status.DELIVERED.getName(), ignoreCase = true)) return true
            return if (status.equals(Status.OPENED.getName(), ignoreCase = true)) true else false
        }
    }

    class Mobile(
        override val displayName: String?,
        override val displayStatus: String?,
        override val displayTimestamp: String?,
        override val displayStatusIconResId: Int,
        override val statusResourceString: Int,
        override val statusTextColorResId: Int
    ) : StatusDetailUI {
        enum class Status(var name_: String, var resId: Int) {
            DELIVERED("Delivered", R.string.receipt_status_mobile_delivered), PENDING(
                "Pending",
                R.string.receipt_status_email_pending
            ),
            OPENED("Opened", R.string.receipt_status_mobile_opened), FAILED(
                "Failed",
                R.string.receipt_status_mobile_failed
            ),
            NOTAVAILABLE("Not Available", R.string.receipt_status_mobile_not_available);

            open fun Status(name: String?, resId: Int) {
                this.name_ = name!!
                this.resId = resId
            }

            open fun getName(): String? {
                return name
            }

//            open fun getResId(): Int {
//                return resId
//            }

        }

        @SerializedName("phoneNumber")
        @Expose
        private var phoneNumber: String? = null

        @SerializedName("status")
        @Expose
        private var status: String? = null

        @SerializedName("messageSid")
        @Expose
        private var messageSid: String? = null

        @SerializedName("smsSid")
        @Expose
        private var smsSid: String? = null

        @SerializedName("timestamp")
        @Expose
        private var timestamp: Long? = null

        fun getPhoneNumber(): String? {
            return phoneNumber
        }

        fun isSent(): Boolean {
            return if (status.equals(
                    Status.DELIVERED.getName(),
                    ignoreCase = true
                ) || status.equals(
                    Status.OPENED.getName(), ignoreCase = true
                )
            ) true else false
        }

    }

    class WhatsApp(
        override val displayName: String?,
        override val displayStatus: String?,
        override val displayTimestamp: String?,
        override val displayStatusIconResId: Int,
        override val statusResourceString: Int,
        override val statusTextColorResId: Int
    ) : StatusDetailUI {
        enum class Status(var name_: String, var resId: Int) {
            DELIVERED("Delivered", R.string.receipt_status_mobile_delivered), PENDING(
                "Pending",
                R.string.receipt_status_email_pending
            ),
            OPENED("Opened", R.string.receipt_status_mobile_opened), FAILED(
                "Failed",
                R.string.receipt_status_mobile_failed
            ),
            NOTAVAILABLE("Not Available", R.string.receipt_status_mobile_not_available);

            open fun Status(name: String?, resId: Int) {
                this.name_ = name!!
                this.resId = resId
            }

            open fun getName(): String? {
                return name
            }

//            open fun getResId(): Int {
//                return resId
//            }

        }

        @SerializedName("phoneNumber")
        @Expose
        private var phoneNumber: String? = null

        @SerializedName("status")
        @Expose
        private var status: String? = null

        @SerializedName("messageSid")
        @Expose
        private var messageSid: String? = null

        @SerializedName("smsSid")
        @Expose
        private var smsSid: String? = null

        @SerializedName("timestamp")
        @Expose
        private var timestamp: Long? = null

        fun getPhoneNumber(): String? {
            return phoneNumber
        }

        fun isSent(): Boolean {
            return if (status.equals(
                    Status.DELIVERED.getName(),
                    ignoreCase = true
                ) || status.equals(
                    Status.OPENED.getName(), ignoreCase = true
                )
            ) true else false
        }
    }
}