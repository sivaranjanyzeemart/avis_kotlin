package zeemart.asia.buyers.models.orderimportimport

import zeemart.asia.buyers.models.order.Orders

/**
 * class for holding data of the cart order breakdown list of suppliers sorted by delivery date
 */
class CartSelectedOrdersBreakdownListUI {
    var isDeliveryDateHeader = false
    var isSupplierRow = false
    var order: Orders? = null
    var deliveryDate: String? = null
}