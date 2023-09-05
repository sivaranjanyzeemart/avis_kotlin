package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.StringHelper

/**
 * Created by ParulBhandari on 12/12/2017.
 */
open class ProductPriceList {
    @SerializedName("price")
    @Expose
    var price: PriceDetails? = null
        get() = if (field == null) {
            if (dealPrice != null && dealPrice!!.amount != 0.0) {
                dealPrice
            } else if (essentialPrice != null && essentialPrice!!.amount != 0.0) {
                essentialPrice
            } else {
                originalPrice
            }
        } else {
            field
        }

    @SerializedName("unitSize")
    @Expose
    var unitSize: String? = null

    @SerializedName("moq")
    @Expose
    var moq: Double? = null
        get() = if (field == null) {
            0.0
        } else {
            field
        }

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("dealPrice")
    @Expose
    var dealPrice: PriceDetails? = null

    @SerializedName("essentialPrice")
    @Expose
    var essentialPrice: PriceDetails? = null

    @SerializedName("originalPrice")
    @Expose
    var originalPrice: PriceDetails? = null

    @SerializedName("shelfLife")
    @Expose
    var shelfLife: String? = null

    @SerializedName("onHandQty")
    @Expose
    var onHandQty: Double? = null

    @SerializedName("parLevel")
    @Expose
    var parLevel: Double? = null

    @SerializedName("incomings")
    @Expose
    var incomings: List<Incoming>? = null

    //used to set the value if the particular unit size is selected in Review Order Screen
    var selected = false

    enum class UnitSizeStatus(var unitSizeStatusName: String) {
        VISIBLE("visible"), INVISIBLE("invisible"), ACTIVE("ACTIVE"), INACTIVE("INACTIVE");

        override fun toString(): String {
            return unitSizeStatusName
        }
    }

    fun isStatus(unitSizeStatus: UnitSizeStatus): Boolean {
        return if (!StringHelper.isStringNullOrEmpty(status)) {
            status == unitSizeStatus.toString()
        } else false
    }
}