package zeemart.asia.buyers.models.orderimport


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.marketlist.DeliveryDate
import zeemart.asia.buyers.models.marketlist.DeliveryFeePolicy

class DealsBaseResponse {
    @SerializedName("data")
    @Expose
    var deals: List<DealsBaseResponse.Deals>? = null

    inner class Deals {
        @SerializedName("dealNumber")
        @Expose
        var dealNumber: String? = null

        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("carouselBanners")
        @Expose
        var carouselBanners: List<String>? = null

        @SerializedName("landingBanners")
        @Expose
        var landingBanners: List<String>? = null

        @SerializedName("activePeriod")
        @Expose
        var activePeriod: DealsBaseResponse.ActivePeriod? = null

        @SerializedName("deliveryFeePolicy")
        @Expose
        var deliveryFeePolicy: DeliveryFeePolicy? = null

        @SerializedName("deliveryDates")
        @Expose
        var deliveryDates: List<DeliveryDate>? = null

        @SerializedName("displaySequence")
        @Expose
        var displaySequence: Int? = null

        @SerializedName("paymentDescription")
        @Expose
        var paymentDescription: String? = null

        @SerializedName("paymentTermsDescription")
        @Expose
        var paymentTermsDescription: String? = null

        @SerializedName("gstPercent")
        @Expose
        var gstPercent: Double? = null

        @SerializedName("orderSettings")
        @Expose
        var orderSettings: DealsBaseResponse.OrderSettings? = null
    }

    inner class ActivePeriod {
        @SerializedName("startTime")
        @Expose
        var startTime: Long = 0

        @SerializedName("endTime")
        @Expose
        var endTime: Long = 0
    }

    class OrderSettings {
        @SerializedName("deliveryFeePolicy")
        @Expose
        var deliveryFeePolicy: DeliveryFeePolicy? = null
    }
}