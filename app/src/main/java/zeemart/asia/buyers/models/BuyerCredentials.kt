package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper


/**
 * Created by ParulBhandari on 11/9/2017.
 * Bean class to hold user credentials
 */
class BuyerCredentials     //        this.market = ZeemartAppConstants.Market.getThis().getValue();
    (
    @field:Expose @field:SerializedName("userName") val zeemartId: String,
    @field:Expose @field:SerializedName(
        "password"
    ) var password: String
) {

    @SerializedName("market")
    @Expose
    val market: String? = null

    fun checkNullEmpty(): Boolean {
        return StringHelper.isStringNullOrEmpty(zeemartId) || StringHelper.isStringNullOrEmpty(
                password
            )
    }

    class ForgotPassword(@field:Expose @field:SerializedName("userName") var zeemartId: String) {

        @SerializedName("market")
        @Expose
        private val market: String? = null

        @SerializedName("userType")
        @Expose
        private val userType: String = USER_TYPE

        companion object {
            private const val USER_TYPE = "BUYER"
        }
    }

    class ValidateCode(
        @field:Expose @field:SerializedName("userName") var zeemartId: String,
        @field:Expose @field:SerializedName(
            "verificationCode"
        ) var verificationCode: String,
        @field:Expose @field:SerializedName("requestedBy") var requestedBy: String
    )

    class UpdatePassword(password: String, newPassword: String) {
        @SerializedName("userName")
        @Expose
        var zeemartId: String = SharedPref.read(SharedPref.USERNAME, "")!!

        @SerializedName("password")
        @Expose
        var password: String

        @SerializedName("newPassword")
        @Expose
        var newPassword: String

        @SerializedName("clientType")
        @Expose
        private val clienType: String

        init {
            this.password = password
            this.newPassword = newPassword
            clienType = "BUYER"
        }
    }

    class ResetPassword     //            this.market = ZeemartAppConstants.Market.getThis().getValue();
        (
        @field:Expose @field:SerializedName("verificationCode") var verificationCode: String,
        @field:Expose @field:SerializedName(
            "newPassword"
        ) var newPassword: String,
        @field:Expose @field:SerializedName(
            "userName"
        ) var zeemartId: String
    ) {

        @SerializedName("market")
        @Expose
        private val market: String? = null
    }
}