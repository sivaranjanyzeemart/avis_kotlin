package zeemart.asia.buyers.reports.reportpendingbysku

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.ReportSpendingBySkuTabAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.ReportApi
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.reports.SpendingModel

class ReportsSpendingBySKUActivity : AppCompatActivity(), View.OnClickListener {
    private val spendingData = ArrayList<SpendingModel>()
    private var spendingDetailsByValue: String? = null
    private var totalSpendingData: String? = null
    private var spendingTimePeriod: String? = null
    private var startDate: String? = null
    private var endDate: String? = null
    private lateinit var btnBack: ImageView
    private var calledFrom: String? = null
    private var skuName: String? = ""
    private lateinit var txtSKUHeading: TextView
    private lateinit var txtOutletName: TextView
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_spending_by_sku)
        val bundle = intent.extras
        if (bundle != null) {
            /* if(bundle.containsKey(ReportApi.TOTAL_SPENDING_DATA))
            {
                totalSpendingData = bundle.getString(ReportApi.TOTAL_SPENDING_DATA);
                spendingData.clear();
                SpendingModel[] data = ZeemartBuyerApp.gsonExposeExclusive.fromJson(totalSpendingData, SpendingModel[].class);
                spendingData.addAll(Arrays.asList(data));
            }*/
            spendingData.clear()
            totalSpendingData = SharedPref.read(ReportApi.TOTAL_SPENDING_DATA, null)
            if (totalSpendingData != null) {
                val spendingDataSharedPrefs =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<SpendingModel>>(
                        totalSpendingData,
                        object : TypeToken<ArrayList<SpendingModel?>?>() {}.type
                    )
                if (spendingDataSharedPrefs != null) {
                    spendingData.addAll(spendingDataSharedPrefs)
                }
            }
            if (bundle.containsKey(ReportApi.SPENDING_DATA_PERIOD)) {
                spendingTimePeriod = bundle.getString(ReportApi.SPENDING_DATA_PERIOD)
            }
            if (bundle.containsKey(ReportApi.START_DATE)) {
                startDate = bundle.getString(ReportApi.START_DATE)
            }
            if (bundle.containsKey(ReportApi.END_DATE)) {
                endDate = bundle.getString(ReportApi.END_DATE)
            }
            if (bundle.containsKey(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)) {
                spendingDetailsByValue =
                    bundle.getString(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)
            }
            if (bundle.containsKey(ReportApi.CALLED_FROM_FRAGMENT)) {
                calledFrom = bundle.getString(ReportApi.CALLED_FROM_FRAGMENT)
            }
            if (bundle.containsKey(ReportApi.SKU_NAME)) {
                skuName = bundle.getString(ReportApi.SKU_NAME)
            }
        }
        viewPager = findViewById(R.id.report_total_spending_view_pager)
        viewPager.setAdapter(
            ReportSpendingBySkuTabAdapter(
                this@ReportsSpendingBySKUActivity,
                supportFragmentManager,
                startDate!!,
                endDate!!,
                spendingTimePeriod!!,
                spendingDetailsByValue!!
            )
        )
        if (calledFrom == ReportApi.DASHBOARD_WEEK_FRAGMENT || calledFrom == ReportApi.ALL_PRICE_UPDATE_ACTIVITY) {
            viewPager.setCurrentItem(1)
        }
        tabLayout = findViewById(R.id.report_total_spending_sku_tab_layout)
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                CommonMethods.setFontForTabs(tabLayout)
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    AnalyticsHelper.logAction(
                        this@ReportsSpendingBySKUActivity,
                        AnalyticsHelper.TAP_REPORTS_SKU_SPENDING_DETAILS
                    )
                } else if (position == 1) {
                    AnalyticsHelper.logAction(
                        this@ReportsSpendingBySKUActivity,
                        AnalyticsHelper.TAP_REPORTS_SKU_PRICE_HISTORY
                    )
                }
                CommonMethods.setFontForTabs(tabLayout)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        btnBack = findViewById(R.id.report_total_spending_bak_btn)
        btnBack.setOnClickListener(this)
        txtSKUHeading = findViewById(R.id.report_total_spending_header_text)
        txtOutletName = findViewById(R.id.txt_outlet_name)
        txtOutletName.setText(SharedPref.defaultOutlet?.currentOutletName)
        txtSKUHeading.setText(skuName)
        setTypefaceView(txtSKUHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        CommonMethods.setFontForTabs(tabLayout)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.report_total_spending_bak_btn -> finish()
            else -> {}
        }
    }
}