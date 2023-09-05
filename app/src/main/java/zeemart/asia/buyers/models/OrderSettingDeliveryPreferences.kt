package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.models.*

/**
 * Created by RajPrudhviMarella on 23/Aug/2021.
 */
class OrderSettingDeliveryPreferences {
    @SerializedName("path")
    @Expose
    var path: String? = null

    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: OrderSettingDeliveryPreferences.Data? = null

    class Data {
        @SerializedName("cutOffTimes")
        @Expose
        lateinit var cutOffTimes: List<CutOffTimes>

        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("userNotifications")
        @Expose
        var userNotifications: UserNotifications? = null
    }

    class UserNotifications {
        @SerializedName("email")
        @Expose
        var email: String? = null

        @SerializedName("phone")
        @Expose
        var phone: String? = null

        @SerializedName("whatsapp")
        @Expose
        var whatsapp: String? = null
    }

    class CutOffTimes {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("deliverOn")
        @Expose
        var deliverOn: String? = null

        @SerializedName("cutoffTime")
        @Expose
        var cutoffTime: CutoffTime? = null
    }

    class CutoffTime {
        @SerializedName("days")
        @Expose
        var days: Int? = null

        @SerializedName("time")
        @Expose
        var time: String? = null
            get() = DateHelper.convert24hrTo12hrsFormat(field)
    }
}