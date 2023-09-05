package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*

/**
 * Created by RajprudhviMarella on 07/08/2020.
 */
class SearchedEssentialAndDealsPruducts {
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

    @SerializedName("unitPrice")
    @Expose
    var unitPrices: ProductPriceList? = null

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