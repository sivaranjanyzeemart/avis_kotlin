package zeemart.asia.buyers.models.reportsimport

/**
 * Created by ParulBhandari on 2/22/2018.
 */
class QuantityBoughtUI {
    var quantity: Double? = null
        get() = if (field == null) 0.0 else field
    var uom: String? = null
    var amount: Double? = null
        get() = if (field == null) 0.0 else field
}