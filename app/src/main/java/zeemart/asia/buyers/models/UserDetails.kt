package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 12/20/2017.
 */
open class UserDetails {
    @SerializedName("id")
    @Expose
    var id: String? = null
        get() {
            if (field == null) field = ""
            return field
        }

    @SerializedName("firstName")
    @Expose
    var firstName: String? = null

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null

    @SerializedName("imageURL")
    @Expose
    var imageURL: String? = null

    @SerializedName("phone")
    @Expose
    var phone: String? = null

    @SerializedName("trackNotificationsStatus")
    @Expose
    var trackNotificationsStatus: Boolean? = null

    @SerializedName("customNameAndCode")
    @Expose
    var customNameAndCode: Boolean? = null

    @SerializedName("doNotSendReports")
    @Expose
    var doNotSendReports: Boolean? = null

    @SerializedName("language")
    @Expose
    var language: String? = null

    @SerializedName("reasonText")
    @Expose
    var reasonText: String? = null

    @SerializedName("market")
    @Expose
    var market: String? = null

    @SerializedName("roleGroup")
    @Expose
    var roleGroup: String? = null

    @SerializedName("settings")
    @Expose
    var settings: UserSettings? = null

    class ViewUser {
        @SerializedName("user")
        @Expose
        var user: UserProfileDetails? = null

        @SerializedName("status")
        @Expose
        var status: String? = null
    }

    class UserProfileDetails : UserDetails() {
        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("email")
        @Expose
        var email: String? = null

        @SerializedName("outlet")
        @Expose
        var outlet: List<Outlet>? = null

        @SerializedName("company")
        @Expose
        var company: List<Companies>? = null

        @SerializedName("status")
        @Expose
        var status: String? = null
    }
}