package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*
import java.util.*

/**
 * Created by RajPrudhviMarella on 09/Feb/2021.
 */
class ProductDetailsBySearch {
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
    private var productCustomName: String? = null

    @SerializedName("supplierProductCode")
    @Expose
    var supplierProductCode: String? = null

    @SerializedName("priceList")
    @Expose
    var priceList: ProductPriceList? = null

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
    var certifications: List<ProductDetailBySupplier.Certification>? = null

    @SerializedName("isFavourite")
    @Expose
    var isFavourite = false

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
    var isSelected = false
    var isHasMultipleUom = false

    fun getProductCustomName(): String? {
        return if (productCustomName != null && productCustomName!!.length == 0) null else productCustomName
    }

    fun setProductCustomName(productCustomName: String?) {
        this.productCustomName = productCustomName
    }

    class Response : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: ProductPagination? = null
    }

    class ProductPagination : Pagination() {
        @SerializedName("data")
        @Expose
        var data: List<ProductDetailsBySearch>? = null
    }

    companion object {
        fun sortByProductName(productList: List<ProductDetailsBySearch>?) {
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