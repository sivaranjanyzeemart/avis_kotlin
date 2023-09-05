package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.modelsimport.OrderSettingDeliveryPreferences

class OrderSettingsRequest {
    @SerializedName("userNotifications")
    @Expose
    var userNotifications: OrderSettingDeliveryPreferences.UserNotifications? = null

    @SerializedName("cutOffTimes")
    @Expose
    var cutOffTimes: List<OrderSettingDeliveryPreferences.CutOffTimes>? = null

    @SerializedName("useDefault")
    @Expose
    var isUseDefault = false

    @SerializedName("orderDisabled")
    @Expose
    var isOrderDisabled = false
}