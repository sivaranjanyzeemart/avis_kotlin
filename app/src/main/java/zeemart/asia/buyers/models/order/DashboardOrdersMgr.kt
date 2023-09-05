package zeemart.asia.buyers.models.orderimportimport

import zeemart.asia.buyers.models.order.Orders

/**
 * Created by ParulBhandari on 7/27/2018.
 */
class DashboardOrdersMgr {
    var isHeader = false
    var isViewAllPlacedData = false
    var isViewAllDeliveriesToday = false
    var isNoDeliveries = false
    var isActionRequiredOrder = false
    var isContinueOrderingOrder = false
    var isPendingOrder = false
    var isDeliveriesOrder = false
    var isNoPendingOrder = false
    var mDashboardOrdersMgrHeader: String? = null
    var viewAllData: String? = null
    var order: Orders? = null

    fun getDashboardOrdersMgrHeader(): String? {
        return mDashboardOrdersMgrHeader
    }

    fun setDashboardOrdersMgrHeader(header: String?) {
        this.mDashboardOrdersMgrHeader = header
    }
}