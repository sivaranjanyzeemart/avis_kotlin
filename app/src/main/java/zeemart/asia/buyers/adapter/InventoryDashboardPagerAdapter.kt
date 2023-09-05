package zeemart.asia.buyers.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import zeemart.asia.buyers.R
import zeemart.asia.buyers.inventory.InventoryDataManager

/**
 * Created by ParulBhandari on 2/7/2018.
 */
class InventoryDashboardPagerAdapter(private val context: Context, fm: FragmentManager?) :
    SmartFragmentPagerAdapter(fm) {
    private val fragments: MutableList<Fragment> = ArrayList()

    // Our custom method that populates this Adapter with Fragments
    fun addFragments(fragment: Fragment) {
        fragments.add(fragment)
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == InventoryDataManager.LIST_TAB_ID) {
            context.getString(R.string.txt_inventory_lists)
        } else if (position == InventoryDataManager.ACTIVITY_TAB_ID) {
            context.getString(R.string.txt_activity)
        } else {
            ""
        }
    }
}