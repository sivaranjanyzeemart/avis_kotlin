package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.order.Orders

/**
 * Created by ParulBhandari on 12/5/2017.
 */
interface UpdateRepeatOrderActivityListener {
    fun noOfSelectedOrders(selectedOrder: Int, selectedOrders: Map<String?, Orders?>?)
    fun callOrderDetailDialogFragment(
        pastOrders: ArrayList<Orders?>?,
        position: Int,
        horizontalRowId: Int
    )
}