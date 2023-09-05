package zeemart.asia.buyers.models.orderimportimport


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.PriceDetails

class OrderAmount {
    @SerializedName("subTotal")
    @Expose
    var subTotal: PriceDetails? = null

    @SerializedName("gst")
    @Expose
    var gst: PriceDetails? = null

    @SerializedName("deliveryFee")
    @Expose
    var deliveryFee: PriceDetails? = null

    @SerializedName("total")
    @Expose
    var total: PriceDetails? = null

    @SerializedName("discount")
    @Expose
    var discount: PriceDetails? = null
}