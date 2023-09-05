package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*

/**
 * Created by RajPrudhviMarella on 26/Apr/2021.
 */
class SelfOnBoardingSearchEssentialAndDealsModel {
    @SerializedName("path")
    @Expose
    var path: String? = null

    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: SelfOnBoardingSearchEssentialAndDealsModel.Data? = null

    inner class Data {
        @SerializedName("suppliers")
        @Expose
        var suppliers: List<SearchedEssentialAndDealsSuppliers>? = null

        @SerializedName("products")
        @Expose
        var products: SelfOnBoardingSearchEssentialAndDealsModel.Products? = null

        @SerializedName("categories")
        @Expose
        var categories: List<SearchEssentialAndDealsModel.Category>? = null
    }

    inner class Products {
        @SerializedName("numberOfRecords")
        @Expose
        var numberOfRecords: Int? = null

        @SerializedName("currentPageNumber")
        @Expose
        var currentPageNumber: Int? = null

        @SerializedName("numberOfPages")
        @Expose
        var numberOfPages: Int? = null

        @SerializedName("data")
        @Expose
        var data: List<SelfOnBoardingSearchedEssentialAndDealsPruducts>? = null
    }

    inner class SelfOnBoardingSearchedEssentialAndDealsPruducts {
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

        @SerializedName("unitPrices")
        @Expose
        var unitPrices: List<ProductPriceList>? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

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

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null

        @SerializedName("essentialsProdId")
        @Expose
        var essentialsProdId: String? = null

        @SerializedName("essentialsId")
        @Expose
        var essentialsId: String? = null

        @SerializedName("isFavourite")
        @Expose
        var isFavourite = false
    }
}