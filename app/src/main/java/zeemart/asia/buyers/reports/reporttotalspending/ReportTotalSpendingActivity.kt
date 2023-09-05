package zeemart.asia.buyers.reports.reporttotalspending

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.ReportTotalSpendingPagerAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DateHelper.StartDateEndDate
import zeemart.asia.buyers.helper.DialogHelper.GetDatePickerFromTo
import zeemart.asia.buyers.helper.ReportApi
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.DateRangeChangeObserver
import zeemart.asia.buyers.interfaces.DateRangeChangePublisher
import zeemart.asia.buyers.interfaces.ProgressIndicatorListener
import zeemart.asia.buyers.reports.ReportTimePeriod
import zeemart.asia.buyers.reports.reportsummary.ExportReportCsvFileActivity
import java.util.*

class ReportTotalSpendingActivity : AppCompatActivity(), View.OnClickListener,
    DateRangeChangePublisher, DatePickerDialog.OnDateSetListener, ProgressIndicatorListener {
    private lateinit var lytChangeCalendarDate: LinearLayout
    private lateinit var spendingTimePeriod: String
    private var txtTimePeriod: TextView? = null
    private var timePickerDialog: Dialog? = null
    private val listDateChangeObserver = ArrayList<DateRangeChangeObserver>()
    private lateinit var btnBack: ImageView
    private lateinit var btnDownload: ImageView
    private lateinit var startEndDateRange: StartDateEndDate
    private var txtTotalSpendingHeader: TextView? = null
    private lateinit var txtOutletName: TextView
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_total_spending)
        listDateChangeObserver.clear()
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ReportApi.SPENDING_DATA_PERIOD)) {
                spendingTimePeriod = bundle.getString(ReportApi.SPENDING_DATA_PERIOD).toString()
            }
        }
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_reports_total_pending_white)
        txtTimePeriod = findViewById(R.id.report_total_spending_time_range_val)
        setTimePeriod(spendingTimePeriod, false)
        viewPager = findViewById(R.id.report_total_spending_view_pager)
        viewPager.setAdapter(
            ReportTotalSpendingPagerAdapter(
                this,
                supportFragmentManager,
                spendingTimePeriod,
                this
            )
        )
        viewPager.setOffscreenPageLimit(2)
        tabLayout = findViewById(R.id.report_total_spending_tab_layout)
        tabLayout.setupWithViewPager(viewPager)
        //added to track firebase events on tab selected
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
                        this@ReportTotalSpendingActivity,
                        AnalyticsHelper.TAP_TOTAL_SPENDING_SUPPLIER_TAB
                    )
                } else if (position == 1) {
                    AnalyticsHelper.logAction(
                        this@ReportTotalSpendingActivity,
                        AnalyticsHelper.TAP_TOTAL_SPENDING_CATEGORY_TAB
                    )
                } else if (position == 2) {
                    AnalyticsHelper.logAction(
                        this@ReportTotalSpendingActivity,
                        AnalyticsHelper.TAP_TOTAL_SPENDING_SKU_TAB
                    )
                }
                CommonMethods.setFontForTabs(tabLayout)
            }

            override fun onPageScrollStateChanged(state: Int) {
                CommonMethods.setFontForTabs(tabLayout)
            }
        })
        txtTotalSpendingHeader = findViewById(R.id.report_total_spending_header_text)
        lytChangeCalendarDate = findViewById(R.id.report_total_spending_lyt_calendar)
        lytChangeCalendarDate.setOnClickListener(this)
        btnBack = findViewById(R.id.report_total_spending_bak_btn)
        btnBack.setOnClickListener(this)
        btnDownload = findViewById(R.id.report_total_spending_download_btn)
        btnDownload.setOnClickListener(this)
        txtOutletName = findViewById(R.id.txt_outlet_name)
        txtOutletName.setText(SharedPref.defaultOutlet?.currentOutletName)
        setFont()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.report_total_spending_lyt_calendar -> createTimePickerDialog()
            R.id.lyt_calendar_this_week -> {
                spendingTimePeriod = ReportTimePeriod.THISWEEK.timePeriodValue
                setTimePeriod(spendingTimePeriod, true)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_last_week -> {
                spendingTimePeriod = ReportTimePeriod.LASTWEEK.timePeriodValue
                setTimePeriod(spendingTimePeriod, true)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_this_month -> {
                spendingTimePeriod = ReportTimePeriod.THISMONTH.timePeriodValue
                setTimePeriod(spendingTimePeriod, true)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_last_month -> {
                spendingTimePeriod = ReportTimePeriod.LASTMONTH.timePeriodValue
                setTimePeriod(spendingTimePeriod, true)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_custom -> {
                val dpd = GetDatePickerFromTo(
                    this,
                    startEndDateRange!!.startDateMonthYearString,
                    startEndDateRange!!.endDateMonthYearString
                )
                dpd.show(fragmentManager, "Datepickerdialog")
            }
            R.id.report_total_spending_bak_btn -> finish()
            R.id.report_total_spending_download_btn -> {
                AnalyticsHelper.logAction(
                    this@ReportTotalSpendingActivity,
                    AnalyticsHelper.TAP_TOTAL_SPENDING_SHARE_REPORT
                )
                val b = Bundle()
                val startEndDateRangeString =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(startEndDateRange)
                b.putString(ReportApi.START_END_DATE_RANGE, startEndDateRangeString)
                val intent = Intent(
                    this@ReportTotalSpendingActivity,
                    ExportReportCsvFileActivity::class.java
                )
                intent.putExtras(b)
                startActivity(intent)
            }
            else -> {}
        }
    }

    private fun createTimePickerDialog() {
        timePickerDialog = Dialog(this, R.style.CustomDialogTheme)
        timePickerDialog!!.setContentView(R.layout.layout_report_date_selector)
        val lytThisWeek =
            timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_this_week)
        lytThisWeek.setOnClickListener(this)
        val lytLastWeek =
            timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_last_week)
        lytLastWeek.setOnClickListener(this)
        val lytThisMonth =
            timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_this_month)
        lytThisMonth.setOnClickListener(this)
        val lytLastMonth =
            timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_last_month)
        lytLastMonth.setOnClickListener(this)
        val lytCustom = timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_custom)
        lytCustom.setOnClickListener(this)
        val txtThisWeek = timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_this_week)
        val txtThisWeekRange =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_date_range_week)
        val txtLstWeek = timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_last_week)
        val txtLstWeekRange =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_date_range_last_week)
        val txtThisMonth = timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_this_month)
        val txtThisMonthRange =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_date_range_this_month)
        val txtLastMonth = timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_last_month)
        val txtLastMonthRange =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_date_range_last_month)
        val txtCustom = timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_custom)
        setTypefaceView(txtThisWeek, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtThisWeekRange, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtLstWeek, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtLstWeekRange, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtThisMonth, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtThisMonthRange, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtLastMonth, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtLastMonthRange, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCustom, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        val imgThisWeek =
            timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_this_week)
        imgThisWeek.visibility = View.GONE
        val imgLastWeek =
            timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_last_week)
        imgLastWeek.visibility = View.GONE
        val imgThisMonth =
            timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_this_month)
        imgThisMonth.visibility = View.GONE
        val imgLastMonth =
            timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_last_month)
        imgLastMonth.visibility = View.GONE
        val imgCustom = timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_custom)
        imgCustom.visibility = View.GONE
        if (spendingTimePeriod == ReportTimePeriod.THISWEEK.timePeriodValue) {
            imgThisWeek.visibility = View.VISIBLE
        }
        if (spendingTimePeriod == ReportTimePeriod.LASTWEEK.timePeriodValue) {
            imgLastWeek.visibility = View.VISIBLE
        }
        if (spendingTimePeriod == ReportTimePeriod.THISMONTH.timePeriodValue) {
            imgThisMonth.visibility = View.VISIBLE
        }
        if (spendingTimePeriod == ReportTimePeriod.LASTMONTH.timePeriodValue) {
            imgLastMonth.visibility = View.VISIBLE
        }
        if (spendingTimePeriod == ReportTimePeriod.CUSTOM.timePeriodValue) {
            imgCustom.visibility = View.VISIBLE
        }


        //Set Range for current week
        val dateRangeCurrentWeek = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
        val dateRange = dateRangeCurrentWeek.startEndRangeDateMonthFormat
        txtThisWeekRange.text = dateRange

        //Set Range for last week
        val dateRangeLastWeek = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
        val dateRangePreviousWeek = dateRangeLastWeek.startEndRangeDateMonthFormat
        txtLstWeekRange.text = dateRangePreviousWeek
        txtThisMonthRange.text = DateHelper.returnMonthYearString(0)
        txtLastMonthRange.text = DateHelper.returnMonthYearString(1)
        timePickerDialog!!.show()
    }

    private fun setTimePeriod(timePeriod: String?, isNotifyObserver: Boolean) {
        var timePeriodRange: String? = ""
        if (timePeriod == ReportTimePeriod.THISWEEK.timePeriodValue) {
            startEndDateRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
            timePeriodRange = startEndDateRange.startEndRangeDateMonthYearFormat
            if (isNotifyObserver) {
                notifyObserver(
                    timePeriod!!,
                    startEndDateRange.startDateMonthYearString,
                    startEndDateRange.endDateMonthYearString
                )
            }
        } else if (timePeriod == ReportTimePeriod.LASTWEEK.timePeriodValue) {
            startEndDateRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
            timePeriodRange = startEndDateRange.startEndRangeDateMonthYearFormat
            if (isNotifyObserver) {
                notifyObserver(
                    timePeriod!!,
                    startEndDateRange.startDateMonthYearString,
                    startEndDateRange.endDateMonthYearString
                )
            }
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTWEEK.timePeriodValue) {
            startEndDateRange =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_WEEK)
            timePeriodRange = startEndDateRange.startEndRangeDateMonthYearFormat
            if (isNotifyObserver) {
                notifyObserver(
                    timePeriod!!,
                    startEndDateRange.startDateMonthYearString,
                    startEndDateRange.endDateMonthYearString
                )
            }
        } else if (timePeriod == ReportTimePeriod.THISMONTH.timePeriodValue) {
            startEndDateRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.CURRENT_MONTH)
            timePeriodRange = startEndDateRange.startEndRangeDateMonthYearFormat
            if (isNotifyObserver) {
                notifyObserver(
                    timePeriod!!,
                    startEndDateRange.startDateMonthYearString,
                    startEndDateRange.endDateMonthYearString
                )
            }
        } else if (timePeriod == ReportTimePeriod.LASTMONTH.timePeriodValue) {
            startEndDateRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_MONTH)
            timePeriodRange = startEndDateRange.startEndRangeDateMonthYearFormat
            if (isNotifyObserver) {
                notifyObserver(
                    timePeriod!!,
                    startEndDateRange.startDateMonthYearString,
                    startEndDateRange.endDateMonthYearString
                )
            }
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTMONTH.timePeriodValue) {
            startEndDateRange =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_MONTH)
            timePeriodRange = startEndDateRange.startEndRangeDateMonthYearFormat
            if (isNotifyObserver) {
                notifyObserver(
                    timePeriod!!,
                    startEndDateRange.startDateMonthYearString,
                    startEndDateRange.endDateMonthYearString
                )
            }
        }
        //        if (isNotifyObserver) {
//            ReportApi.callTotalSpendingDeprecatedSaveDataInSharedPrefs(this, startEndDateRange.getStartDateMillis(), startEndDateRange.getEndDateMillis());
//        }
        txtTimePeriod!!.text = timePeriodRange
    }

    override fun addDateRangeChangeObserver(o: DateRangeChangeObserver?) {
        if (o != null) {
            listDateChangeObserver.add(o)
        }
    }

    override fun removeDateRangeChangeObserver(o: DateRangeChangeObserver?) {
        listDateChangeObserver.remove(o)
    }

    override fun notifyObserver(timePeriodValue: String?, startDate: String?, endDate: String?) {
        for (i in listDateChangeObserver.indices) {
            listDateChangeObserver[i].onDateRangeChanged(timePeriodValue, startDate, endDate)
        }
    }

    override fun onDateSet(
        view: DatePickerDialog,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        yearEnd: Int,
        monthOfYearEnd: Int,
        dayOfMonthEnd: Int
    ) {
        Log.d(
            "START END DATE",
            "$year-$monthOfYear-$dayOfMonth****$yearEnd--$monthOfYearEnd-$dayOfMonthEnd"
        )
        val startDate = Calendar.getInstance(DateHelper.marketTimeZone)
        startDate[year, monthOfYear] = dayOfMonth
        val endDate = Calendar.getInstance(DateHelper.marketTimeZone)
        endDate[yearEnd, monthOfYearEnd] = dayOfMonthEnd
        if (endDate.before(startDate)) {
            getToastRed(getString(R.string.txt_enter_valid_date_range))
        } else {
            timePickerDialog!!.dismiss()
            spendingTimePeriod = ReportTimePeriod.CUSTOM.timePeriodValue
            startEndDateRange = DateHelper.returnStartDateEndDateCustomRange(startDate, endDate)
            val startEndDate = startEndDateRange.startEndRangeDateMonthYearFormat
            txtTimePeriod!!.text = startEndDate
            notifyObserver(
                spendingTimePeriod,
                startEndDateRange.startDateMonthYearString,
                startEndDateRange.endDateMonthYearString
            )
            //            ReportApi.callTotalSpendingDeprecatedSaveDataInSharedPrefs(this, startEndDateRange.getStartDateMillis(), startEndDateRange.getEndDateMillis());
        }
    }

    private fun exportCompletionMsg(msg: String) {
        val builder = AlertDialog.Builder(this@ReportTotalSpendingActivity)
        builder.setNeutralButton(getString(R.string.txt_dismiss), null)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(getString(R.string.txt_reports))
        dialog.setMessage(msg)
        dialog.show()
    }

    private fun setFont() {
        setTypefaceView(
            txtTotalSpendingHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtTimePeriod, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        CommonMethods.setFontForTabs(tabLayout)
    }

    override fun visibilityVisible() {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
    }

    override fun visibilityGone() {
        threeDotLoaderWhite!!.visibility = View.GONE
    }
}