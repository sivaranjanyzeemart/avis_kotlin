package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*
import java.util.*

/**
 * Created by ParulBhandari on 12/12/2017.
 */
class ProductDetailBySupplier {
    @SerializedName("sku")
    @Expose
    var sku: String? = null

    @SerializedName("productName")
    @Expose
    var productName: String? = null

    @SerializedName("categoryPath")
    @Expose
    var categoryPath: String? = null

    @SerializedName("customName")
    @Expose
    var productCustomName: String? = null

    @SerializedName("supplierProductCode")
    @Expose
    var supplierProductCode: String? = null

    @SerializedName("customProductCode")
    @Expose
    var customProductCode: String? = null

    @SerializedName("priceList")
    @Expose
    var priceList: MutableList<ProductPriceList>? = null

    @SerializedName("categoryTags")
    @Expose
    var categoryTags: List<String>? = null

    @SerializedName("stocks")
    @Expose
    var stocks: List<StocksList>? = null

    @SerializedName("outletTags")
    @Expose
    var tags: List<String>? = null

    @SerializedName("timePriceUpdated")
    @Expose
    var timePriceUpdated: Long = 0

    @SerializedName("images")
    @Expose
    var images: List<ImagesModel>? = null

    @SerializedName("supplierId")
    @Expose
    var supplierId: String? = null

    @SerializedName("favourites")
    @Expose
    var favourites: List<String>? = null

    @SerializedName("certifications")
    @Expose
    var certifications: List<Certification>? = null

    @SerializedName("isFavourite")
    @Expose
    var isFavourite = false

    @SerializedName("belowParLevel")
    @Expose
    var isBelowParLevel = false

    @Expose
    var supplierName: String? = null

    @Expose
    var supplier: Supplier? = null

    @Expose
    var dealNumber: String? = null

    @Expose
    var dealProductId: String? = null

    @Expose
    var essentialsId: String? = null

    @Expose
    var essentialsProductId: String? = null

    //field added to check if the product selected for new order
    @Expose
    var isSelected = false

    @Expose
    var isHasMultipleUom = false


    class Response : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: ProductPagination? = null
    }

    class ProductPagination : Pagination() {
        @SerializedName("data")
        @Expose
        var data: List<ProductDetailBySupplier>? = null
    }

    inner class Certification {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("certificationURL")
        @Expose
        var certificationURL: ImagesModel? = null
        override fun toString(): String {
            return "Certification{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", certificationURL=" + certificationURL +
                    '}'
        }
    }

    companion object {
        @JvmStatic
        fun sortByProductName(productList: List<ProductDetailBySupplier>?) {
            if (productList != null) {
                Collections.sort(productList) { o1, o2 ->
                    o1.productName!!.compareTo(
                        o2.productName!!
                    )
                }
            }
        }
    }
}