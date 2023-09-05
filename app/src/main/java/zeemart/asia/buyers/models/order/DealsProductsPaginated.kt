package zeemart.asia.buyers.models.orderimportimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Pagination
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier.Certification

class DealsProductsPaginated {
    @SerializedName("data")
    @Expose
    var data: DealsProductsPaginated.WithPaginationData? = null

    inner class WithPaginationData : Pagination() {
        @SerializedName("data")
        @Expose
        var dealProducts: List<DealProducts>? = null
    }

    inner class DealProducts {
        @SerializedName("dateUpdated")
        @Expose
        var dateUpdated: String? = null

        @SerializedName("timeUpdated")
        @Expose
        var timeUpdated: Int? = null

        @SerializedName("updatedBy")
        @Expose
        var updatedBy: UserDetails? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("dealProductId")
        @Expose
        var dealProductId: String? = null

        @SerializedName("dealNumber")
        @Expose
        var dealNumber: String? = null

        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null

        @SerializedName("supplierProductCode")
        @Expose
        var supplierProductCode: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("images")
        @Expose
        var images: List<String>? = null

        @SerializedName("unitPrices")
        @Expose
        var unitPrices: List<ProductPriceList>? = null

        @SerializedName("categoryTags")
        @Expose
        var categoryTags: List<String>? = null

        @SerializedName("certifications")
        @Expose
        var certifications: List<Certification>? = null
    }
}