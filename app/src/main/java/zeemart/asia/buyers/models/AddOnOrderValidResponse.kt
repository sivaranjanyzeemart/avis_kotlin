package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddOnOrderValidResponse {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data {
        @SerializedName("isAddOn")
        @Expose
        var isAddOn = false
    }
}