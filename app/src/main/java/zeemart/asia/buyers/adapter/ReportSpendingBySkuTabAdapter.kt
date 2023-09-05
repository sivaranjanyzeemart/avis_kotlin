package zeemart.asia.buyers.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import zeemart.asia.buyers.R
import zeemart.asia.buyers.reports.reportpendingbysku.SpendingBySkuSpendingDetailsFragment.Companion.newInstance
import zeemart.asia.buyers.reports.reportpendingbysku.SpendingbySKUPriceHistoryFragment.Companion.newInstance

/**
 * Created by ParulBhandari on 2/7/2018.
 */
class ReportSpendingBySkuTabAdapter(
    private val context: Context,
    fm: FragmentManager?,
    private val startDate: String,
    private val endDate: String,
    private val spendingTimePeriod: String,
    private val skuValue: String
) : FragmentPagerAdapter(
    fm!!
) {
    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return newInstance(
                startDate, endDate, spendingTimePeriod, skuValue
            )
        }
        return if (position == 1) {
            newInstance(
                spendingTimePeriod, skuValue
            )
        } else newInstance(
            startDate, endDate, spendingTimePeriod, skuValue
        )
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) {
            context.getString(R.string.txt_report_spending_details)
        } else if (position == 1) {
            context.getString(R.string.txt_report_price_history)
        } else {
            ""
        }
    }
}