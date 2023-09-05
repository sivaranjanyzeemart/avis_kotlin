package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.order.Orders

/**
 * Created by ParulBhandari on 12/5/2017.
 */
interface OrderHorizontalListOrderChangeListener {
    fun onOrderSelected(rowId: Int)
    fun onOrderDeselected(rowId: Int)
    fun showOrderDetailFragment(
        pastOrderList: ArrayList<Orders?>?,
        position: Int,
        horizontalRowId: Int
    )
}