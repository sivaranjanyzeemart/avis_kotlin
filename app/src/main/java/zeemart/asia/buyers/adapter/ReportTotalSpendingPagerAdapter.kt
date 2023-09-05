package zeemart.asia.buyers.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import zeemart.asia.buyers.R
import zeemart.asia.buyers.interfaces.DateRangeChangePublisher
import zeemart.asia.buyers.reports.reporttotalspending.TotalSpendingCategoryFragment
import zeemart.asia.buyers.reports.reporttotalspending.TotalSpendingSKUFragment
import zeemart.asia.buyers.reports.reporttotalspending.TotalSpendingSupplierFragment
import zeemart.asia.buyers.reports.reporttotalspending.TotalSpendingTagsFragment

/**
 * Created by ParulBhandari on 2/7/2018.
 */
class ReportTotalSpendingPagerAdapter(
    var context: Context,
    fm: FragmentManager?,
    private val spendingTimePeriod: String,
    private val dateRangeChangePublisher: DateRangeChangePublisher
) : FragmentStatePagerAdapter(
    fm!!
) {
    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            val frag = TotalSpendingSupplierFragment.newInstance(
                spendingTimePeriod
            )
            dateRangeChangePublisher.addDateRangeChangeObserver(frag)
            return frag
        }
        if (position == 1) {
            val frag = TotalSpendingCategoryFragment.newInstance(
                spendingTimePeriod
            )
            dateRangeChangePublisher.addDateRangeChangeObserver(frag)
            return frag
        }
        if (position == 2) {
            val frag = TotalSpendingSKUFragment.newInstance(
                spendingTimePeriod
            )
            dateRangeChangePublisher.addDateRangeChangeObserver(frag)
            return frag
        }
        if (position == 3) {
            val frag = TotalSpendingTagsFragment.newInstance(
                spendingTimePeriod
            )
            dateRangeChangePublisher.addDateRangeChangeObserver(frag)
            return frag
        }
        val frag = TotalSpendingSupplierFragment.newInstance(
            spendingTimePeriod
        )
        dateRangeChangePublisher.addDateRangeChangeObserver(frag)
        return frag
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) {
            context.getString(R.string.title_supplier)
        } else if (position == 1) {
            context.getString(R.string.title_category)
        } else if (position == 2) {
            context.getString(R.string.title_sku)
        } else if (position == 3) {
            context.getString(R.string.txt_tag)
        } else {
            ""
        }
    }
}