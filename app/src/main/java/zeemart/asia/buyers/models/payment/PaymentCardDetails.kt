package zeemart.asia.buyers.models.paymentimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.BaseResponse

class PaymentCardDetails : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: List<PaymentResponse>? = null

    enum class CardType(var value: String) {
        VISA("Visa"), MASTERCARD("Mastercard"), AMEX("Amex");

        override fun toString(): String {
            return value
        }
    }

    class PaymentResponse {
        @SerializedName("referenceNo")
        @Expose
        var referenceNo: String? = null

        @SerializedName("isDefaultCard")
        @Expose
        var isDefaultCard = false

        @SerializedName("customerCardData")
        @Expose
        var customerCardData: CustomerCardDetails? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        class CustomerCardDetails {
            @SerializedName("cardType")
            @Expose
            var cardType: String? = null

            @SerializedName("cardLast4Digits")
            @Expose
            var cardLast4Digits: String? = null

            @SerializedName("customerId")
            @Expose
            var customerId: String? = null
            fun isCardType(cardType: CardType): Boolean {
                return if (!StringHelper.isStringNullOrEmpty(this.cardType)) {
                    this.cardType == cardType.toString()
                } else false
            }
        }
    }
}