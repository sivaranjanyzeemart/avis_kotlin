package zeemart.asia.buyers.models.orderimportimportimport


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.marketlist.DeliveryDate
import zeemart.asia.buyers.models.marketlist.DeliveryFeePolicy
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse

class SpclDealsBaseResponse {
    @SerializedName("data")
    @Expose
    var data: SpclDealsBaseResponse.Suppliers? = null

    inner class Suppliers {
        @SerializedName("suppliers")
        @Expose
        var suppliers: List<SpclDealsBaseResponse.Deals>? = null
    }

    inner class Deals {
        @SerializedName("dealNumber")
        @Expose
        var dealNumber: String? = null

        @SerializedName("essentialsId")
        @Expose
        var essentialsId: String? = null

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

        @SerializedName("dealSettings")
        @Expose
        var dealSettings: DealSettings? = null
        override fun toString(): String {
            return "Deals{" +
                    "dealNumber='" + dealNumber + '\'' +
                    ", essentialsId='" + essentialsId + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", supplier=" + supplier +
                    ", carouselBanners=" + carouselBanners +
                    ", landingBanners=" + landingBanners +
                    ", activePeriod=" + activePeriod +
                    ", deliveryFeePolicy=" + deliveryFeePolicy +
                    ", deliveryDates=" + deliveryDates +
                    ", displaySequence=" + displaySequence +
                    ", paymentDescription='" + paymentDescription + '\'' +
                    ", paymentTermsDescription='" + paymentTermsDescription + '\'' +
                    ", gstPercent=" + gstPercent +
                    ", orderSettings=" + orderSettings +
                    ", dealSettings=" + dealSettings +
                    '}'
        }
    }

    inner class ActivePeriod {
        @SerializedName("startTime")
        @Expose
        var startTime: Long = 0

        @SerializedName("endTime")
        @Expose
        var endTime: Long = 0
    }

    inner class DealSettings {
        @SerializedName("carouselBanners")
        @Expose
        var carouselBanners: List<String>? = null
    }
}