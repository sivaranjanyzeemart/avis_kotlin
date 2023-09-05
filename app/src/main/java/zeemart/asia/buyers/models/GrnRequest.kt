package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by RajPrudhviMarella on 16/June/2021.
 */
class GrnRequest {
    @SerializedName("orderId")
    @Expose
    var orderId: String? = null

    @SerializedName("timeReceived")
    @Expose
    var timeReceived: Long? = null

    @SerializedName("products")
    @Expose
    var products: List<Product>? = null

    @SerializedName("customLineItems")
    @Expose
    var customLineItems: List<CustomLineItem>? = null

    @SerializedName("images")
    @Expose
    var images: List<Image>? = null

    @SerializedName("notes")
    @Expose
    var notes: String? = null

    @SerializedName("sendEmailToSupplier")
    @Expose
    var isSendEmailToSupplier = false

    @SerializedName("supplierNotificationEmail")
    @Expose
    var supplierNotificationEmail: String? = null

    @SerializedName("noteToSupplier")
    @Expose
    var noteToSupplier: String? = null

    class Image {
        @SerializedName("imageFileNames")
        @Expose
        var imageFileNames: List<String>? = null

        @SerializedName("imageURL")
        @Expose
        var imageURL: String? = null
    }

    open class Product {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("customName")
        @Expose
        var customName: String? = null

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null
    }

    class CustomLineItem {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null
    }
}