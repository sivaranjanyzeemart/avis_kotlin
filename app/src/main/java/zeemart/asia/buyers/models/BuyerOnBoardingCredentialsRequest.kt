package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by RajPrudhviMarella on 22/sep/2020.
 */
class BuyerOnBoardingCredentialsRequest {
    @SerializedName("firstName")
    @Expose
    var firstName: String? = null

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null

    @SerializedName("country")
    @Expose
    var country: String? = null

    @SerializedName("phone")
    @Expose
    var phone: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null
}