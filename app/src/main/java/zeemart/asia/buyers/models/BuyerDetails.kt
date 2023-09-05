package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.UserDetails.UserProfileDetails

/**
 * Created by ParulBhandari on 11/9/2017.
 * Bean class for holding buyer successful login details
 */
class BuyerDetails {
    @SerializedName("mudra")
    @Expose
    var mudra: String? = null

    @SerializedName("outlet")
    @Expose
    var outlet: List<Outlet>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("market")
    @Expose
    var market: String? = null

    @SerializedName("restrictedAccess")
    @Expose
    var restrictedAccess: List<String>? = null

    @SerializedName("language")
    @Expose
    var language: String? = null

    @SerializedName("user")
    @Expose
    var userDetails: UserProfileDetails? = null
}