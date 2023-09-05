package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ValidatePromoCode(supplierId: String?, amount: PriceDetails?, promoCode: String?) {
    @SerializedName("promoCode")
    @Expose
    var promoCode: String? = null

    @SerializedName("subTotal")
    @Expose
    var subTotal: PriceDetails? = null

    @SerializedName("supplierId")
    @Expose
    var supplierId: String? = null

    init {
        this.supplierId = supplierId
        subTotal = amount
        this.promoCode = promoCode
    }

    class Response : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: PromoCode? = null
    }

    open class PromoCode {
        @SerializedName("discount")
        @Expose
        var discount: PriceDetails? = null

        @SerializedName("promoCode")
        @Expose
        open var promoCode: String? = null

        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("imageURL")
        @Expose
        var imageURL: String? = null

        @Expose
        var id: String? = null
        val discountDisplayValue: String
            get() {
                return if (!UserPermission.HasViewPrice()) {
                    ""
                } else {
                    if (discount != null) {
                        return "-" + discount!!.displayValue
                    }
                    ""
                }
            }
    }
}