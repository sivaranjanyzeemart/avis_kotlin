package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CompanyUpdate {
    @SerializedName("uenNo")
    @Expose
    var uenNo: String? = null

    @SerializedName("imageURL")
    @Expose
    var imageURL: String? = null
}