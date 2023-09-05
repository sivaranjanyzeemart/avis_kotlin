package zeemart.asia.buyers.models.reportsimport

/**
 * Created by ParulBhandari on 2/27/2018.
 */
class SkuDateBoughtUI {
    var invoiceDate: Long? = null
        get() = if (field == 0L) null else field
    var uom: String? = null
    var quantity: Double? = null
        get() = if (field == null) 0.0 else field
    var amount: Double? = null
        get() = if (field == null) 0.0 else field
}