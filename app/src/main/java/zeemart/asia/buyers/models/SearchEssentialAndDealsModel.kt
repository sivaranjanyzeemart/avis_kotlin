package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by RajprudhviMarella on 07/08/2020.
 */
class SearchEssentialAndDealsModel {
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
    var data: SearchEssentialAndDealsModel.Data? = null

    inner class Data {
        @SerializedName("suppliers")
        @Expose
        var suppliers: List<SearchedEssentialAndDealsSuppliers>? = null

        @SerializedName("products")
        @Expose
        var products: SearchEssentialAndDealsModel.Products? = null

        @SerializedName("categories")
        @Expose
        var categories: List<SearchEssentialAndDealsModel.Category>? = null
    }

    inner class Category {
        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("path")
        @Expose
        var path: String? = null
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
        var data: List<SearchedEssentialAndDealsPruducts>? = null
    }
}