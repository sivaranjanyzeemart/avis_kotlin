package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class SplashViewPagerAdapter(private val context: Context) : PagerAdapter() {
    override fun getCount(): Int {
        return 3
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return if (position == 0) {
            val imageLayout = LayoutInflater.from(context)
                .inflate(R.layout.lyt_splash_screen1, container, false)!!
            val txtContent = imageLayout.findViewById<TextView>(R.id.txt_content)
            setTypefaceView(
                txtContent,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            val txtHeader = imageLayout.findViewById<TextView>(R.id.txt_header)
            setTypefaceView(
                txtHeader,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
            )
            container.addView(imageLayout, position)
            imageLayout
        } else if (position == 1) {
            val imageLayout = LayoutInflater.from(context)
                .inflate(R.layout.lyt_splash_screen2, container, false)!!
            val txtContent = imageLayout.findViewById<TextView>(R.id.txt_content)
            setTypefaceView(
                txtContent,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            val txtHeader = imageLayout.findViewById<TextView>(R.id.txt_header)
            setTypefaceView(
                txtHeader,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
            )
            container.addView(imageLayout, position)
            imageLayout
        } else {
            val imageLayout = LayoutInflater.from(context)
                .inflate(R.layout.lyt_splash_screen3, container, false)!!
            val txtContent = imageLayout.findViewById<TextView>(R.id.txt_content)
            setTypefaceView(
                txtContent,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            val txtHeader = imageLayout.findViewById<TextView>(R.id.txt_header)
            setTypefaceView(
                txtHeader,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
            )
            container.addView(imageLayout, position)
            imageLayout
        }
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }
}