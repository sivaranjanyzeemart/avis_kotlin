package zeemart.asia.buyers.models.orderimportimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Pagination
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier.Certification

/**
 * Created by RajPrudhvi on 4/20/2018.
 */
class EssentialProductsPaginated {
    @SerializedName("data")
    @Expose
    var data: EssentialProductsPaginated.WithPaginationData? = null

    inner class WithPaginationData : Pagination() {
        @SerializedName("data")
        @Expose
        var essentialProducts: List<EssentialsProducts>? = null
    }

    inner class EssentialsProducts {
        @SerializedName("essentialsProdId")
        @Expose
        var essentialsProdId: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("essentialsId")
        @Expose
        var essentialsId: String? = null

        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("supplierProductCode")
        @Expose
        var supplierProductCode: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("isFavourite")
        @Expose
        var isFavourite = false

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null

        @SerializedName("images")
        @Expose
        var images: List<String>? = null

        @SerializedName("unitPrices")
        @Expose
        var unitPrices: List<ProductPriceList>? = null

        @SerializedName("dateUpdated")
        @Expose
        var dateUpdated: String? = null

        @SerializedName("timeUpdated")
        @Expose
        var timeUpdated: Int? = null

        @SerializedName("updatedBy")
        @Expose
        var updatedBy: UserDetails? = null

        @SerializedName("categoryTags")
        @Expose
        var categoryTags: List<String>? = null

        @SerializedName("certifications")
        @Expose
        var certifications: List<Certification>? = null
    }
}