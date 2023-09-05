package zeemart.asia.buyers.models.orderimportimport

import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI

/**
 * Created by ParulBhandari on 12/28/2017.
 */
class ListWithStickyHeaderUI {
    var isHeader = false
    var isLoader = false
    var headerData: HeaderViewOrderUI? = null
    var order: Orders? = null
}