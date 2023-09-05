package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import zeemart.asia.buyers.models.ValidatePromoCode.PromoCode

open class PromoListUI : PromoCode() {
    @Expose
    var isPromoSelected = false

    @Expose
    var orderSubTotalPromoApplied: PriceDetails? = null

    class PromoDataPassed : PromoListUI() {
        @Expose
        override var promoCode: String? = null

        @Expose
        var outletId: String? = null

        @Expose
        var supplierId: String? = null

        @Expose
        var calledFrom: String? = null

        @Expose
        var promoCodeId: String? = null
    }

    companion object {
        const val SUBTOTAL_AMOUNT = "SUBTOTAL_AMOUNT"
        const val PROMO_CODE_DETAILS = "PROMO_CODE_DETAILS"
    }
}