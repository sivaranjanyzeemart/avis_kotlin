package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SetPaymentCardDefault {
    @SerializedName("companyId")
    @Expose
    var companyId: String? = null

    @SerializedName("referenceNo")
    @Expose
    var referenceNo: String? = null

    @SerializedName("setAsDefaultBy")
    @Expose
    var setAsDefaultBy: UserDetails? = null
}