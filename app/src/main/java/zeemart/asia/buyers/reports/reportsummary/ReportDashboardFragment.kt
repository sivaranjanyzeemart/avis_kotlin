package zeemart.asia.buyers.reports.reportsummary

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.ReportDashboardPagerAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity

/**
 * A simple [Fragment] subclass.
 */
class ReportDashboardFragment : Fragment(),
    ReportDashboardMonthFragment.OnFragmentInteractionListener,
    ReportDashboardWeekFragment.OnFragmentInteractionListener, View.OnClickListener {
    private var oldOutletName = SharedPref.defaultOutlet?.outletName
    private var tabLayout: TabLayout? = null
    private lateinit var viewPager: ViewPager
    private lateinit var btnReportSearch: ImageView
    private lateinit var btnDashboardBack: ImageView
    private var txtOutletName: TextView? = null
    private var context: Context? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.activity_reports_dashboard, container, false)
        viewPager = v.findViewById(R.id.report_dashboard_view_pager)
        tabLayout = v.findViewById(R.id.lyt_tab_reports_home)
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                CommonMethods.setFontForTabs(tabLayout!!)
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_REPORTS_WEEK_TAB)
                } else if (position == 1) {
                    AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_REPORTS_MONTH_TAB)
                }
                CommonMethods.setFontForTabs(tabLayout!!)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        btnReportSearch = v.findViewById(R.id.btn_report_search)
        btnDashboardBack = v.findViewById(R.id.btn_report_dashboard_back)
        txtOutletName = v.findViewById(R.id.txt_report_outlet_name)
        btnReportSearch.setOnClickListener(this)
        btnDashboardBack.setOnClickListener(this)
        setFont()
        return v
    }

    private fun setFont() {
        setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_report_search -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_REPORTS_SEARCH)
                val intent = Intent(context, ReportSearchActivity::class.java)
                startActivity(intent)
            }
            R.id.txt_report_outlet_name -> {
                val intentChangeOutlet = Intent(context, SelectOutletActivity::class.java)
                startActivity(intentChangeOutlet)
            }
            R.id.btn_report_dashboard_back -> {}
            else -> {}
        }
    }

    override fun onFragmentInteraction(uri: Uri?) {}
    fun loadReport() {
        if (context != null) {
            viewPager!!.adapter = ReportDashboardPagerAdapter(requireContext(), childFragmentManager)
            CommonMethods.setFontForTabs(tabLayout!!)
        }
    }

    override fun onResume() {
        super.onResume()
        if (SharedPref.defaultOutlet != null) txtOutletName!!.text =
            SharedPref.defaultOutlet!!.outletName else txtOutletName!!.text = ""
        txtOutletName!!.setOnClickListener(this)
        //refresh the dashboard everytime user switches the outlet.
        if (context != null && oldOutletName != SharedPref.defaultOutlet?.outletName) {
            loadReport()
            oldOutletName = SharedPref.defaultOutlet?.outletName
        }
    }

    companion object {
        fun newInstance(): ReportDashboardFragment {
            return ReportDashboardFragment()
        }
    }
}