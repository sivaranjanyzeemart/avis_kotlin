package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.constants.ZeemartAppConstants
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Created by ParulBhandari on 3/29/2018.
 */
class Settings {
    @SerializedName("GST")
    @Expose
    var gst: Gst? = null

    @SerializedName("inventory")
    @Expose
    var inventory: Inventory? = null

    class Gst {
        @SerializedName("percent")
        @Expose
        var percent: Double? = null
        val percentDisplayValue: String
            get() {
                val symbols = DecimalFormatSymbols(ZeemartAppConstants.Market.`this`.locale)
                return DecimalFormat("###,###,###.###", symbols).format(percent)
            }
    }
}