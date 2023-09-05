package zeemart.asia.buyers.models.reports

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier

/**
 * Created by saiful on 18/4/18.
 */
class ExportModel {
    class SummarySkuModel {
        @SerializedName("invoiceDate")
        @Expose
        var invoiceDate: Long? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null

        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity = 0.0

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("totalPrice")
        @Expose
        var totalPrice: PriceDetails? = null
        val categoryName: String
            get() {
                if (categoryPath == null) return ""
                val arr = categoryPath!!.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                return if (arr.size > 1) {
                    arr[1]
                } else categoryPath!!
            }
    }

    class SummaryModel {
        @SerializedName("invoiceDate")
        @Expose
        var invoiceDate: Long? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("totalCharge")
        @Expose
        var totalCharge: PriceDetails? = null

        @SerializedName("totalChargeWithoutGst")
        @Expose
        var totalChargeWithoutGst: PriceDetails? = null
    }

    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null

    @SerializedName("fromDate")
    @Expose
    var fromDate: Long? = null

    @SerializedName("untilDate")
    @Expose
    var untilDate: Long? = null

    @SerializedName("exportDate")
    @Expose
    var exportDate: Long? = null

    @SerializedName("spendingModelList")
    @Expose
    var spendingModelList: List<SpendingModel>? = null
}