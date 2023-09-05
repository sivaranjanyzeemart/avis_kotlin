package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getPriceDouble
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.marketlist.Settings.Gst

/**
 * Created by ParulBhandari on 12/21/2017.
 */
class PriceDetails {
    @SerializedName("currencyCode")
    @Expose
    var currencyCode: String? = null

    @SerializedName("amountV1")
    @Expose
    private var amountV1: Double? = 0.0

    constructor() {
        currencyCode = (ZeemartAppConstants.Market.`this`.countryCode)
    }

    constructor(amount: Double?) {
        amountV1 = amount
    }

//    fun getCurrencyCode(): String {
//        return currencyCode ?: ""
//    }
//
//    fun setCurrencyCode(currencyCode: String?) {
//        this.currencyCode = currencyCode
//    }

    // convert the third digit after the decimal
    var amount: Double?
        get() = if (amountV1 == null) {
            0.0
        } else {
            amountV1 = Math.round(amountV1!! * 100)
                .toDouble() / 100 // convert the third digit after the decimal
            amountV1
        }
        set(amount) {
            amountV1 = amount
        }

    fun addAmount(amount: Double) {
        amountV1 = this.amount!! + amount
    }

    val displayValueEscapeComma: String
        get() = '"'.toString() + displayValue + '"'
    private val marketAmountWithSymbol: String
        private get() {
            var amt = amount!!
            if (amt < 0) {
                amt = Math.abs(amt)
            }
            return if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.INDONESIA)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s %,d",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    amt.toLong()
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.VIETNAM)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%,d %s ",
                    amt.toLong(),
                    ZeemartAppConstants.Market.`this`.currencySymbol
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.MALAYSIA)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s %,.2f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.UAE)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s %,.2f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.AUSTRALIA)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s%,.2f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            } else {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s%,.2f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            }
        }
    private val marketAmountWithSymbolForChart: String
        private get() {
            val amt = amount!!
            return if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.INDONESIA)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s %,d",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    amt.toLong()
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.VIETNAM)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%,d %s ",
                    amt.toLong(),
                    ZeemartAppConstants.Market.`this`.currencySymbol
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.MALAYSIA)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s %,.0f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.UAE)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s %,.0f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            } else if (ZeemartAppConstants.Market.`this`
                    .isMarket(ZeemartAppConstants.Market.AUSTRALIA)
            ) {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s%,.0f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            } else {
                String.format(
                    ZeemartAppConstants.Market.`this`.locale,
                    "%s%,.0f",
                    ZeemartAppConstants.Market.`this`.currencySymbol,
                    getPriceDouble(amt, 2)
                )
            }
        }

    /**
     * used for getting the set value to pass to the API,
     * Please use getResId when displaying on the UI.
     *
     * @param gst
     * @return
     */
    fun getAmount(gst: Gst): Double {
        return amount!! * gst.percent!! / 100
    }

    fun getDisplayValue(gst: Gst): String {
        if (!UserPermission.HasViewPrice()) {
            return ""
        }
        var amt = amount!!
        try {
            amt = amt * gst.percent!! / 100
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return PriceDetails(amt).displayValue
    }

    val displayValue: String
        get() {
            if (!UserPermission.HasViewPrice()) {
                return ""
            }
            val amt = amount!!
            return if (amt < 0) {
                "-$marketAmountWithSymbol"
            } else {
                marketAmountWithSymbol
            }
        }
    val negativeDisplayValue: String
        get() = if (!UserPermission.HasViewPrice()) {
            ""
        } else "-$marketAmountWithSymbol"
    val displayChartValue: String
        get() = if (!UserPermission.HasViewPrice()) {
            ""
        } else marketAmountWithSymbolForChart

    fun getDisplayValueWithUom(uom: String?): String {
        return if (!UserPermission.HasViewPrice()) {
            ""
        } else String.format("%s / %s", marketAmountWithSymbol, uom)
    }

    fun getDisplayValueWithUomNoSpacing(uom: String?): String {
        return if (!UserPermission.HasViewPrice()) {
            ""
        } else String.format("%s/%s", marketAmountWithSymbol, uom)
    }

    fun getDisplayValueWithUomAndBullet(uom: String?): String {
        return if (!UserPermission.HasViewPrice()) {
            ""
        } else String.format("%s / %s", marketAmountWithSymbol, uom)
    }

    val displayValueWithBullet: String
        get() = if (!UserPermission.HasViewPrice()) {
            ""
        } else String.format(" • %s", marketAmountWithSymbol)
}