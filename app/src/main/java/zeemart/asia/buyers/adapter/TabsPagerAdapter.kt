package zeemart.asia.buyers.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Created by ParulBhandari on 11/21/2017.
 */
class TabsPagerAdapter(private val fragmentManager: FragmentManager) : SmartFragmentPagerAdapter(
    fragmentManager
) {
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
}