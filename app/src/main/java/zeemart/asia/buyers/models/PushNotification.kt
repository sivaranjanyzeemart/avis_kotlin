package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants

/**
 * Created by ParulBhandari on 12/8/2017.
 */
class PushNotification(@field:Expose @field:SerializedName("deviceToken") var deviceToken: String) {

    @SerializedName("platform")
    @Expose
    var platform: String = ServiceConstant.GCM

    @SerializedName("userId")
    @Expose
    var userId: String? = null

    @SerializedName("market")
    @Expose
    var market: String = ZeemartAppConstants.Market.`this`.countryCode

    constructor(deviceToken: String, userId: String?) : this(deviceToken) {
        this.userId = userId
    }
}