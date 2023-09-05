package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.marketlist.DeliveryDate
import zeemart.asia.buyers.models.marketlist.DeliveryFeePolicy

/**
 * Created by RajPrudhvi on 4/17/2018.
 */
class EssentialsBaseResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var essentials: List<Essential>? = null

    inner class Essential {
        @SerializedName("essentialsId")
        @Expose
        var essentialsId: String? = null

        @SerializedName("shortDesc")
        @Expose
        var shortDesc: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("landingBanners")
        @Expose
        var landingBanners: List<String>? = null

        @SerializedName("deliveryFeePolicy")
        @Expose
        var deliveryFeePolicy: DeliveryFeePolicy? = null

        @SerializedName("deliveryDates")
        @Expose
        var deliveryDates: List<DeliveryDate>? = null

        @SerializedName("paymentTermDescription")
        @Expose
        var paymentTermDescription: String? = null

        @SerializedName("paymentTermsDescription")
        @Expose
        var paymentTermsDescription: String? = null

        @SerializedName("gstPercent")
        @Expose
        var gstPercent: Double? = null

        @SerializedName("rebatePercentage")
        @Expose
        var rebatePercentage: Double? = null

        @SerializedName("orderSettings")
        @Expose
        var orderSettings: OrderSettings? = null
    }

    class OrderSettings {
        @SerializedName("deliveryFeePolicy")
        @Expose
        var deliveryFeePolicy: DeliveryFeePolicy? = null
    }
}