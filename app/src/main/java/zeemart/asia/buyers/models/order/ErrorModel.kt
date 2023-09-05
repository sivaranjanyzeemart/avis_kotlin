package zeemart.asia.buyers.models.orderimportimport


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.PriceDetails

class ErrorModel {
    enum class ErrorCode(val value: String) {
        REQUEST_VALIDATION_ERROR("REQUEST_VALIDATION_ERROR");

        override fun toString(): String {
            return value
        }
    }

    @SerializedName("errorCode")
    @Expose
    var errorCode: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ProductData? = null
    fun isErrorCode(errorCode: ErrorModel.ErrorCode): Boolean {
        return if (this.errorCode == errorCode.toString()) {
            true
        } else false
    }

    inner class ProductData {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("supplierProductCode")
        @Expose
        var supplierProductCode: String? = null

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("unitPrice")
        @Expose
        var unitPrice: PriceDetails? = null

        @SerializedName("discount")
        @Expose
        var discount: PriceDetails? = null

        @SerializedName("totalPrice")
        @Expose
        var totalPrice: PriceDetails? = null
    }
}