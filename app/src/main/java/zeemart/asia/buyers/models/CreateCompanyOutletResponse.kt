package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Outlet

class CreateCompanyOutletResponse {
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
    var data: CreateCompanyOutletResponse.Data? = null

    inner class Data {
        @SerializedName("dateCreated")
        @Expose
        var dateCreated: String? = null

        @SerializedName("dateUpdated")
        @Expose
        var dateUpdated: String? = null

        @SerializedName("timeCreated")
        @Expose
        var timeCreated: Int? = null

        @SerializedName("timeUpdated")
        @Expose
        var timeUpdated: Int? = null

        @SerializedName("userId")
        @Expose
        var userId: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("firstName")
        @Expose
        var firstName: String? = null

        @SerializedName("lastName")
        @Expose
        var lastName: String? = null

        @SerializedName("phone")
        @Expose
        var phone: String? = null

        @SerializedName("email")
        @Expose
        var email: String? = null

        @SerializedName("sendToOutletEmail")
        @Expose
        var sendToOutletEmail: Boolean? = null

        @SerializedName("trackNotificationsStatus")
        @Expose
        var trackNotificationsStatus: Boolean? = null

        @SerializedName("outlet")
        @Expose
        var outlet: List<Outlet>? = null

        @SerializedName("roleGroup")
        @Expose
        var roleGroup: String? = null

        @SerializedName("roles")
        @Expose
        var roles: List<Any>? = null

        @SerializedName("authType")
        @Expose
        var authType: String? = null

        @SerializedName("market")
        @Expose
        var market: String? = null

        @SerializedName("language")
        @Expose
        var language: String? = null

        @SerializedName("whatsapp")
        @Expose
        var whatsapp: String? = null

        @SerializedName("lastLoggedIn")
        @Expose
        var lastLoggedIn: Int? = null
    }
}