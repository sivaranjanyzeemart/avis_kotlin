package zeemart.asia.buyers.reports.reportsummary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.ReportDashboardPagerAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity

/**
 * Created by ParulBhandari on 9/4/2018.
 */
class ReportsDashboardActivity : AppCompatActivity(),
    ReportDashboardMonthFragment.OnFragmentInteractionListener,
    ReportDashboardWeekFragment.OnFragmentInteractionListener, View.OnClickListener {
    private var oldOutletName = SharedPref.defaultOutlet?.outletName
    private var tabLayout: TabLayout? = null
    private lateinit var viewPager: ViewPager
    private lateinit var btnReportSearch: ImageView
    private lateinit var btnDashboardBack: ImageView
    private var txtOutletName: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports_dashboard)
        viewPager = findViewById(R.id.report_dashboard_view_pager)
        btnReportSearch = findViewById(R.id.btn_report_search)
        btnDashboardBack = findViewById(R.id.btn_report_dashboard_back)
        tabLayout = findViewById(R.id.lyt_tab_reports_home)
        txtOutletName = findViewById(R.id.txt_report_outlet_name)
        viewPager.setAdapter(ReportDashboardPagerAdapter(this, supportFragmentManager))
        btnReportSearch.setOnClickListener(this)
        btnDashboardBack.setOnClickListener(this)
        CommonMethods.setFontForTabs(tabLayout!!)
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_report_search -> {
                val intent = Intent(this, ReportSearchActivity::class.java)
                startActivity(intent)
            }
            R.id.txt_report_outlet_name -> {
                val intentChangeOutlet = Intent(this, SelectOutletActivity::class.java)
                startActivity(intentChangeOutlet)
            }
            R.id.btn_report_dashboard_back -> finish()
            else -> {}
        }
    }

    override fun onFragmentInteraction(uri: Uri?) {}
    override fun onResume() {
        super.onResume()
        if (SharedPref.defaultOutlet != null) txtOutletName!!.text =
            SharedPref.defaultOutlet!!.outletName else txtOutletName!!.text = ""
        txtOutletName!!.setOnClickListener(this)
        //refresh the dashboard everytime user switches the outlet.
        if (oldOutletName != SharedPref.defaultOutlet?.outletName) {
            viewPager!!.adapter = ReportDashboardPagerAdapter(this, supportFragmentManager)
            oldOutletName = SharedPref.defaultOutlet?.outletName
        }
    }
}