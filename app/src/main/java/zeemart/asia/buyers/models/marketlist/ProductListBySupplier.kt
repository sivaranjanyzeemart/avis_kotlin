package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Pagination

/**
 * Created by ParulBhandari on 12/12/2017.
 */
class ProductListBySupplier : Pagination() {
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
    var data: Data? = null

    inner class Data {
        @SerializedName("numberOfPages")
        @Expose
        var numberOfPages: Int? = null

        @SerializedName("numberOfRecords")
        @Expose
        var numberOfRecords: Int? = null

        @SerializedName("currentPageNumber")
        @Expose
        var currentPageNumber: Int? = null

        @SerializedName("data")
        @Expose
        var products: List<ProductDetailBySupplier>? = null
    }
}