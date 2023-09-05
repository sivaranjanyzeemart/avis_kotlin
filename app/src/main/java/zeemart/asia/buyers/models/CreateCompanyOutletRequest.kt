package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*

/**
 * Created by RajPrudhviMarella on 22/sep/2020.
 */
class CreateCompanyOutletRequest {
    @SerializedName("companyName")
    @Expose
    var companyName: String? = null

    @SerializedName("companyEmail")
    @Expose
    var companyEmail: String? = null

    @SerializedName("outletName")
    @Expose
    var outletName: String? = null

    @SerializedName("phone")
    @Expose
    var mobileNumber: String? = null

    @SerializedName("address")
    @Expose
    var address: CreateCompanyOutletRequest.Address? = null

    @SerializedName("otherCuisineFeatures")
    @Expose
    var otherCuisineFeatures: List<String>? = null

    @SerializedName("cuisineType")
    @Expose
    var cuisineType: List<OutletFuturesModel.CuisineType>? = null

    class Address {
        @SerializedName("line1")
        @Expose
        var line1: String? = null

        @SerializedName("postal")
        @Expose
        var postal: String? = null

        @SerializedName("line2")
        @Expose
        var line2: String? = null
    }
}