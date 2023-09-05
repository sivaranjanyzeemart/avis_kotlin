package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.marketlist.DeliveryDate

/**
 * Created by RajprudhviMarella on 07/08/2020.
 */
class SearchedEssentialAndDealsSuppliers {
    @SerializedName("essentialsId")
    @Expose
    var essentialsId: String? = null

    @SerializedName("shortDesc")
    @Expose
    var shortDesc: String? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("dealNumber")
    @Expose
    var dealNumber: String? = null

    @SerializedName("supplier")
    @Expose
    var supplier: Supplier? = null

    @SerializedName("landingBanners")
    @Expose
    var landingBanners: List<String>? = null

    @SerializedName("minOrderValue")
    @Expose
    var minOrderValue: PriceDetails? = null

    @SerializedName("deliveryDates")
    @Expose
    var deliveryDates: List<DeliveryDate>? = null

    @SerializedName("paymentTermDescription")
    @Expose
    var paymentTermDescription: String? = null

    @SerializedName("gstPercent")
    @Expose
    var gstPercent: Double? = null

    @SerializedName("rebatePercentage")
    @Expose
    var rebatePercentage: Double? = null
}