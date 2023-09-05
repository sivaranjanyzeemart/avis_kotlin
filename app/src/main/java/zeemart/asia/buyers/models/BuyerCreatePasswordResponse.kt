package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by RajPrudhviMarella on 24/sep/2020.
 */
class BuyerCreatePasswordResponse {
    @SerializedName("password")
    @Expose
    var password: String? = null

    @SerializedName("confirmPassword")
    @Expose
    var confirmPassword: String? = null
}