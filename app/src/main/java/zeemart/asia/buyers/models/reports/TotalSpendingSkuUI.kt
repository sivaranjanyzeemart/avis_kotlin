package zeemart.asia.buyers.models.reportsimport

/**
 * Created by ParulBhandari on 2/20/2018.
 */
class TotalSpendingSkuUI {
    var sku: String? = null
    var productName: String? = null
    var totalSpendingOnSkuAmount: Long = 0
    var supplierName: String? = null

    class SupplierSKUReportDataWithSubtotal {
        var skuListBySupplier: ArrayList<TotalSpendingSkuUI>? = null
        var totalWithoutGSTForSupplier: Long = 0
    }
}