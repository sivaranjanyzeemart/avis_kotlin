package zeemart.asia.buyers.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import zeemart.asia.buyers.R
import zeemart.asia.buyers.reports.reportsummary.ReportDashboardMonthFragment
import zeemart.asia.buyers.reports.reportsummary.ReportDashboardWeekFragment

/**
 * Created by ParulBhandari on 2/7/2018.
 */
class ReportDashboardPagerAdapter(var context: Context, fm: FragmentManager?) :
    FragmentPagerAdapter(
        fm!!
    ) {
    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return ReportDashboardWeekFragment.newInstance()
        }
        return if (position == 1) {
            ReportDashboardMonthFragment.newInstance()
        } else ReportDashboardWeekFragment.newInstance()
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) {
            context.getString(R.string.txt_week)
        } else if (position == 1) {
            context.getString(R.string.txt_month)
        } else {
            ""
        }
    }
}