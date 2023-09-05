package zeemart.asia.buyers.models.reports

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier

/**
 * Created by ParulBhandari on 2/9/2018.
 */
class SpendingModel {
    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null

    @SerializedName("supplier")
    @Expose
    var supplier: Supplier? = null

    @SerializedName("totalCharge")
    @Expose
    var totalCharge: PriceDetails? = null

    @SerializedName("invoiceDate")
    @Expose
    var invoiceDate: Long? = null

    @SerializedName("invoiceNum")
    @Expose
    var invoiceNum: String? = null

    @SerializedName("discount")
    @Expose
    var discount: PriceDetails? = null

    @SerializedName("others")
    @Expose
    var others: PriceDetails? = null

    @SerializedName("deliveryFee")
    @Expose
    var deliveryFee: PriceDetails? = null

    @SerializedName("gst")
    @Expose
    var gst: PriceDetails? = null

    @SerializedName("subTotal")
    @Expose
    var subTotal: PriceDetails? = null

    @SerializedName("products")
    @Expose
    var products: List<ProductSpending>? = null

    inner class ProductSpending {
        @SerializedName("discount")
        @Expose
        var discount: PriceDetails? = null

        @SerializedName("categoryPath")
        @Expose
        var categoryPath: String? = null
        var spendingSupplierName: String? = null
        private var invoiceDate: Long = 0
        val categoryName: String
            get() {
                if (categoryPath == null) return ""
                val arr = categoryPath!!.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                return if (arr.size > 1) {
                    arr[1]
                } else categoryPath!!
            }

//        fun getInvoiceDate(): Long? {
//            return if (invoiceDate == 0L) null else invoiceDate
//        }
//
//        fun setInvoiceDate(invoiceDate: Long) {
//            this.invoiceDate = invoiceDate
//        }
    }
}