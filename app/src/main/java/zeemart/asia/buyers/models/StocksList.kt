package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 4/20/2018.
 */
class StocksList {
    @SerializedName("unitSize")
    @Expose
    var unitSize: String? = null

    @SerializedName("threshold")
    @Expose
    var threshold: Double? = null

    @SerializedName("stockQuantity")
    @Expose
    var stockQuantity: Double? = null

    @SerializedName("orderedQuantity")
    @Expose
    var orderedQuantity: List<Double>? = null
}