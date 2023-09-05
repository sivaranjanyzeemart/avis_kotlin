package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import zeemart.asia.buyers.R
import zeemart.asia.buyers.orders.vieworders.ViewOrdersActivity

/**
 * Created by ParulBhandari on 4/16/2018.
 */
class OrderListingSpendingPagerAdapter(var context: Context) : PagerAdapter() {
    override fun getCount(): Int {
        return 3
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var resId = 0
        when (position) {
            0 -> resId = R.id.lyt_spending_month_order_listing
            1 -> resId = R.id.lyt_spending_week_order_listing
            2 -> resId = R.id.lyt_spending_charts_order_listing
        }
        return (context as ViewOrdersActivity).findViewById(resId)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}