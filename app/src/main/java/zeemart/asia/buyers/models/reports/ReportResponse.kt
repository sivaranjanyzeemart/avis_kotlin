package zeemart.asia.buyers.models.reportsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier

class ReportResponse {
    /****************total spending by supplier */
    class TotalSpendingBySupplier : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<TotalSpendingBySupplierData>? = null
    }

    class TotalSpendingBySupplierData {
        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("subTotalSpending")
        @Expose
        var subTotalSpending: Double? = null

        @SerializedName("discountSpending")
        @Expose
        var discountSpending: Double? = null

        @SerializedName("otherSpending")
        @Expose
        var otherSpending: Double? = null

        @SerializedName("taxSpending")
        @Expose
        var taxSpending: Double? = null

        @SerializedName("deliveryFeeSpending")
        @Expose
        var deliveryFeeSpending: Double? = null

        @SerializedName("totalSpending")
        @Expose
        var totalSpending: Double? = null

        @SerializedName("pastTotalSpending")
        @Expose
        var pastTotalSpending: Double? = null

        @SerializedName("spendingPercentage")
        @Expose
        var spendingPercentage: Double? = null

        @SerializedName("noOfInvoices")
        @Expose
        var noOfInvoices: Int? = null
    }

    /**********************total category spending  */
    class TotalSpendingByCategory : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<TotalSpendingCategoryData>? = null
    }

    class TotalSpendingCategoryData {
        @SerializedName("category")
        @Expose
        var category: String? = null

        @SerializedName("totalamountCategory")
        @Expose
        var totalamountCategory: Double? = null
    }

    /**********************total Tags spending  */
    class TotalSpendingByTags : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<TotalSpendingTagsData>? = null
    }

    class TotalSpendingTagsData {
        @SerializedName("outletTag")
        @Expose
        var outletTag: String? = null

        @SerializedName("total")
        @Expose
        var total: Double? = null
    }

    /**********************total spending by SKU */
    class TotalSpendingBySku : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<TotalSpendingSkuData>? = null
    }

    class TotalSpendingSkuData {
        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("totalSpendingOnSkuAmount")
        @Expose
        var totalSpendingOnSkuAmount: Double? = null

        @SerializedName("pastTotalSpendingOnSkuAmount")
        @Expose
        var pastTotalSpendingOnSkuAmount: Long? = null

        @SerializedName("spendingPercentage")
        @Expose
        var spendingPercentage: Double? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("isCredits")
        @Expose
        var isCredits = false
    }

    /**********************total spending by Tag Detail */
    class TotalSpendingByTagDetail : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<TotalSpendingByTagDetailData>? = null
    }

    class TotalSpendingByTagDetailData {
        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("total")
        @Expose
        var total: Double? = null
    }

    /**********************total spending by Tag Detail for chart */
    class TotalSpendingByTagDetailChart : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<TotalSpendingDetailsByRangeData>? = null
    }

    /*************************************Total Spendind Range */
    class TotalSpendingDetailsByRange : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: List<TotalSpendingDetailsByRangeData>? = null
    }

    class TotalSpendingDetailsByRangeData {
        @SerializedName("dateTime")
        @Expose
        var dateTime: Long? = null

        @SerializedName("subTotalSpendingAmount")
        @Expose
        var subTotalSpendingAmount: Double? = null

        @SerializedName("totalSpendingAmount")
        @Expose
        var totalSpendingAmount: Double? = null

        @SerializedName("discountSpending")
        @Expose
        var discountSpending: Double? = null

        @SerializedName("otherSpending")
        @Expose
        var otherSpending: Double? = null

        @SerializedName("taxSpending")
        @Expose
        var taxSpending: Double? = null

        @SerializedName("deliveryFeeSpending")
        @Expose
        var deliveryFeeSpending: Double? = null

        /****** Fields for  range data by category */
        @SerializedName("noOfInvoices")
        @Expose
        var noOfInvoices: Int? = null

        @SerializedName("isCredits")
        @Expose
        var isCredits = false

        @SerializedName("amount")
        @Expose
        var amount: Double? = null

        @SerializedName("invoiceDate")
        @Expose
        var invoiceDate: Long = 0

        /*********** Field for range data by SKU */
        @SerializedName("uom")
        @Expose
        var uom: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null
    }
}