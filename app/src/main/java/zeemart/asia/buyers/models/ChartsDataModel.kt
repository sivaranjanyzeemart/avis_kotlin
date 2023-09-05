package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ChartsDataModel : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: List<ChartsDataByDateCreated>? = null

    class ChartsDataByDateCreated {
        @SerializedName("dateUpdated")
        @Expose
        var dateCreated: Long? = null

        @SerializedName("total")
        @Expose
        var total: PriceDetails? = null

        @SerializedName("orderCount")
        @Expose
        var orderCount: Int? = null
    }
}