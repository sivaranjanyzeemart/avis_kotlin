package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OutletFuturesModel {
    @SerializedName("cuisineType")
    @Expose
    var cuisineType: List<CuisineType>? = null

    inner class CuisineType {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @Expose
        var isSelected = false
    }
}