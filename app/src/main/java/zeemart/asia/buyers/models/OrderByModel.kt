package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 1/23/2018.
 */
class OrderByModel {
    @SerializedName("unitSize")
    @Expose
    var unitSize: String? = null

    @SerializedName("unitQuantity")
    @Expose
    var unitQuantity: Double? = null
}