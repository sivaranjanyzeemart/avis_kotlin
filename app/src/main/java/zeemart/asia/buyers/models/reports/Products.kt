package zeemart.asia.buyers.models.reportsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.Pagination
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.StocksList

/**
 * Created by Rajprudhvi Marella on 2/8/2018.
 */
class Products {
    @SerializedName("sku")
    @Expose
    var sku: String? = null

    @SerializedName("supplierId")
    @Expose
    var supplierId: String? = null

    @SerializedName("productName")
    @Expose
    var productName: String? = null

    @SerializedName("supplierProductCode")
    @Expose
    var supplierProductCode: String? = null

    @SerializedName("tags")
    @Expose
    var tags: List<String>? = null

    @Expose
    var supplierName: String? = null

    @SerializedName("priceList")
    @Expose
    var priceList: List<ProductPriceList>? = null

    @SerializedName("images")
    @Expose
    var images: List<ImagesModel>? = null

    @SerializedName("customName")
    @Expose
    var productCustomName: String? = null

    @SerializedName("stocks")
    @Expose
    var stocks: List<StocksList>? = null

    @SerializedName("timePriceUpdated")
    @Expose
    var timePriceUpdated: Long = 0
    var isSelected = false

    @Expose
    var isFavourite = false

    class WithPagination : Pagination() {
        @SerializedName("products")
        @Expose
        var products: List<Products>? = null
    }
}