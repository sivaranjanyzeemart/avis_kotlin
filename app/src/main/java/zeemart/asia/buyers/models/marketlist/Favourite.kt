package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Favourite {
    @SerializedName("sku")
    @Expose
    var sku: String? = null

    @SerializedName("isFavourite")
    @Expose
    var isFavourite: Boolean? = null
}