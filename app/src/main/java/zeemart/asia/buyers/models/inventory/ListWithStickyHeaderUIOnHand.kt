package zeemart.asia.buyers.models.inventory

import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI


class ListWithStickyHeaderUIOnHand {
    var isHeader = false
    var isLoader = false
    var headerData: HeaderViewOrderUI? = null
    var onHandHistoryActivity: OnHandHistoryActivity? = null
}