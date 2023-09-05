package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier.Certification

/**
 * Created by ParulBhandari on 1/23/2018.
 */
class ProductDetailModel {
    @SerializedName("sku")
    @Expose
    var sku: String? = null

    @SerializedName("productName")
    @Expose
    var productName: String? = null

    @SerializedName("brand")
    @Expose
    var brand: String? = null

    @SerializedName("supplierProductCode")
    @Expose
    var supplierProductCode: String? = null

    @SerializedName("imageFileNames")
    @Expose
    var imageFileNames: List<String>? = null

    @SerializedName("imageURL")
    @Expose
    var imageURL: String? = null

    @SerializedName("chargeBy")
    @Expose
    var chargeBy: UnitSizeModel? = null

    @SerializedName("orderBy")
    @Expose
    var orderBy: List<OrderByModel>? = null

    @SerializedName("mainCategoryId")
    @Expose
    var mainCategoryId: String? = null

    @SerializedName("categoryId")
    @Expose
    var categoryId: String? = null

    @SerializedName("categoryPath")
    @Expose
    var categoryPath: String? = null

    @SerializedName("attributes")
    @Expose
    var attributes: List<ProductAttributeModel>? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("upcEanNumber")
    @Expose
    var upcEanNumber: String? = null

    @SerializedName("tags")
    @Expose
    var tags: List<String>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("stocks")
    @Expose
    var stocks: List<StocksList>? = null

    @SerializedName("images")
    @Expose
    var images: List<ImagesModel>? = null

    @SerializedName("certifications")
    @Expose
    var certifications: List<Certification>? = null
}