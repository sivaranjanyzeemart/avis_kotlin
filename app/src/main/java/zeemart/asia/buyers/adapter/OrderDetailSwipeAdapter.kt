package zeemart.asia.buyers.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.orders.createorders.OrderDetailFragment.Companion.newInstance

/**
 * Created by ParulBhandari on 3/27/2018.
 */
class OrderDetailSwipeAdapter(
    fm: FragmentManager?,
    private val pastOrderList: ArrayList<Orders>,
    private val position: Int,
    private val horizontalRowId: Int
) : FragmentPagerAdapter(
    fm!!
) {
    override fun getItem(position: Int): Fragment {
        val order = pastOrderList[position]
        val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order, Orders::class.java)
        return newInstance(orderJson, position, horizontalRowId)
    }

    override fun getCount(): Int {
        return pastOrderList.size
    }
}