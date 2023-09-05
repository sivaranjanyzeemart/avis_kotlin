package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.PriceDetails

/**
 * Created by ParulBhandari on 12/19/2017.
 */
class DeliveryFeePolicy {
    @SerializedName("minOrder")
    @Expose
    var minOrder: PriceDetails? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("fee")
    @Expose
    var fee: PriceDetails? = null

    @SerializedName("condition")
    @Expose
    var condition: String? = null

    @SerializedName("blockBelowMinOrder")
    @Expose
    var isBlockBelowMinOrder = false
}