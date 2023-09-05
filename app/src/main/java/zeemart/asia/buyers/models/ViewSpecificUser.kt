package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by RajPrudhviMarella on 27/07/2020.
 */
class ViewSpecificUser {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    inner class Data {
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

        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("phone")
        @Expose
        var phone: String? = null

        @SerializedName("email")
        @Expose
        var email: String? = null

        @SerializedName("imageURL")
        @Expose
        var imageURL: String? = null

        @SerializedName("sendToOutletEmail")
        @Expose
        var sendToOutletEmail: Boolean? = null

        @SerializedName("trackNotificationsStatus")
        @Expose
        var trackNotificationsStatus: Boolean? = null

        @SerializedName("customNameAndCode")
        @Expose
        var customNameAndCode: Boolean? = null

        @SerializedName("doNotSendReports")
        @Expose
        var doNotSendReports: Boolean? = null

        @SerializedName("outlet")
        @Expose
        var outlet: List<Outlet>? = null

        @SerializedName("supplier")
        @Expose
        var supplier: List<Supplier>? = null

        @SerializedName("roleGroup")
        @Expose
        var roleGroup: String? = null

        @SerializedName("roles")
        @Expose
        var roles: List<String>? = null

        @SerializedName("authType")
        @Expose
        var authType: String? = null

        @SerializedName("market")
        @Expose
        var market: String? = null

        @SerializedName("language")
        @Expose
        var language: String? = null

        @SerializedName("lastLoggedIn")
        @Expose
        var lastLoggedIn: Int? = null

        @SerializedName("outletFeatures")
        @Expose
        var outletFeatures: OutletFeatures? = null

        @SerializedName("settings")
        @Expose
        var settings: UserSettings? = null
    }

    inner class CuisineFeature {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null
    }

    inner class CuisineType {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null
    }

    inner class OutletFeatures {
        @SerializedName("businessType")
        @Expose
        var businessType: List<BusinessType>? = null

        @SerializedName("cuisineType")
        @Expose
        var cuisineType: List<CuisineType>? = null

        @SerializedName("cuisineFeatures")
        @Expose
        var cuisineFeatures: List<CuisineFeature>? = null

        @SerializedName("tags")
        @Expose
        var tags: List<Any>? = null
    }

    inner class BusinessType {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null
    }
}