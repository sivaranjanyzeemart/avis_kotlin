package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RetrieveSpecificPromoCode : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: PromoCodes? = null

    class PromoCodes {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("promoCode")
        @Expose
        var promoCode: String? = null

        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("startDate")
        @Expose
        var startDate: Long? = null

        @SerializedName("endDate")
        @Expose
        var endDate: Long? = null

        @SerializedName("discountType")
        @Expose
        var discountType: String? = null

        @SerializedName("discountDetails")
        @Expose
        var discountDetails: List<DiscountDetail>? = null

        @SerializedName("noOfUsages")
        @Expose
        var noOfUsages: Int? = null

        @SerializedName("outletId")
        @Expose
        var outletId: List<String>? = null

        @SerializedName("supplier")
        @Expose
        var supplier: List<Supplier>? = null

        @SerializedName("imageURL")
        @Expose
        var imageURL: String? = null

        @SerializedName("orderNoRange")
        @Expose
        var orderNoRange: OrderNoRange? = null

        @SerializedName("terms")
        @Expose
        var terms: List<String>? = null

        @SerializedName("supplierNames")
        @Expose
        var supplierNames: String? = null

        class DiscountDetail {
            @SerializedName("discountPercentage")
            @Expose
            var discountPercentage: Double? = null

            @SerializedName("minOrder")
            @Expose
            var minOrder: PriceDetails? = null

            @SerializedName("cappedAmount")
            @Expose
            var cappedAmount: PriceDetails? = null
        }

        class Supplier {
            @SerializedName("supplierId")
            @Expose
            var supplierId: String? = null

            @SerializedName("supplierName")
            @Expose
            var supplierName: String? = null

            @SerializedName("showPrice")
            @Expose
            var showPrice: String? = null

            @SerializedName("supplierIntegrationEnabled")
            @Expose
            var supplierIntegrationEnabled: Boolean? = null
        }

        inner class OrderNoRange {
            @SerializedName("startOrderNo")
            @Expose
            var startOrderNo: Int? = null

            @SerializedName("endOrderNo")
            @Expose
            var endOrderNo: Int? = null
        }
    }
}