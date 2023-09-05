package zeemart.asia.buyers.reports

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.TotalSpendingSkuListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.ChartsHelper.ChartXAxisValueFormatter
import zeemart.asia.buyers.helper.ChartsHelper.ChartYAxisValueFormatter
import zeemart.asia.buyers.helper.CustomMarkerViewCharts
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper.GetDatePickerFromTo
import zeemart.asia.buyers.helper.ReportApi
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.reportsimport.ReportResponse
import zeemart.asia.buyers.reports.reportpendingbysku.ReportsSpendingBySKUActivity
import java.util.*

class ReportsSpendingDetailsActivity : AppCompatActivity(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener {
    private lateinit var lytChangeCalendarDate: LinearLayout
    private var lytReportNoData: LinearLayout? = null
    private var txtTimePeriod: TextView? = null
    private var timePickerDialog: Dialog? = null
    private var spendingTimePeriod: String? = null
    private var startDate: String? = ""
    private var endDate: String? = ""
    private var spendingDetailsByValue: String? = null
    private lateinit var btnBack: ImageView
    private var calledFrom: String? = null
    private var SKUName: String? = null
    private var txtTotalSpendingAmount: TextView? = null
    private var txtSpendingDataHeading: TextView? = null
    private var TxtNoDataSupplier: TextView? = null
    private lateinit var lstTotalSpendingSupplier: RecyclerView
    private var txtAvergaeDailySpend: TextView? = null
    private lateinit var txtHeading: TextView
    private lateinit var chart: PieChart
    private lateinit var barChart: BarChart
    private var ReportTotalSpendingSummary: CardView? = null
    private var txtTotalSpendingHeading: TextView? = null
    private var txtTotalWithoutGST: TextView? = null
    private lateinit var txtOutletName: TextView
    private var progressBar: ProgressBar? = null
    private var supplierName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports_spending_details)
        txtTotalSpendingAmount = findViewById(R.id.report_total_spending_amount)
        lstTotalSpendingSupplier = findViewById(R.id.lst_total_spending_by_supplier)
        lstTotalSpendingSupplier.setLayoutManager(LinearLayoutManager(this))
        lstTotalSpendingSupplier.setNestedScrollingEnabled(false)
        txtAvergaeDailySpend = findViewById(R.id.report_total_spending_average_daily_value)
        txtHeading = findViewById(R.id.report_total_spending_header_text)
        txtTimePeriod = findViewById(R.id.report_total_spending_time_range_val)
        lytChangeCalendarDate = findViewById(R.id.report_total_spending_lyt_calendar)
        txtSpendingDataHeading =
            findViewById(R.id.report_total_spending_spending_detail_header_text)
        TxtNoDataSupplier = findViewById(R.id.txt_no_report_data)
        lytChangeCalendarDate.setOnClickListener(this)
        txtOutletName = findViewById(R.id.txt_outlet_name)
        txtOutletName.setText(SharedPref.defaultOutlet?.currentOutletName)
        lytReportNoData = findViewById(R.id.report_total_spending_no_data_layout)
        ReportTotalSpendingSummary = findViewById(R.id.report_total_spending_summary)
        txtTotalSpendingHeading = findViewById(R.id.txt_total_spending_detail_heading)
        txtTotalWithoutGST = findViewById(R.id.report_dashboard_week_txt_date_range)
        chart = findViewById(R.id.chart)
        barChart = findViewById(R.id.barChart)
        chart.setVisibility(View.GONE)
        barChart.setVisibility(View.VISIBLE)
        progressBar = findViewById(R.id.progress_spending_detail)
        val bundle = intent.extras
        if (bundle!!.containsKey(ReportApi.SPENDING_DATA_PERIOD)) {
            spendingTimePeriod = bundle.getString(ReportApi.SPENDING_DATA_PERIOD)
        }
        if (bundle.containsKey(ReportApi.START_DATE)) {
            startDate = bundle.getString(ReportApi.START_DATE)
        }
        if (bundle.containsKey(ReportApi.END_DATE)) {
            endDate = bundle.getString(ReportApi.END_DATE)
        }
        if (bundle.containsKey(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)) {
            spendingDetailsByValue = bundle.getString(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)
        }
        if (bundle.containsKey(ReportApi.SKU_SUPPLIER_NAME)) {
            SKUName = bundle.getString(ReportApi.SKU_SUPPLIER_NAME)
        }
        if (bundle.containsKey(ReportApi.CALLED_FROM_FRAGMENT)) {
            calledFrom = bundle.getString(ReportApi.CALLED_FROM_FRAGMENT)
        }
        if (bundle.containsKey(ReportApi.SUPPLIER_NAME)) {
            supplierName = bundle.getString(ReportApi.SUPPLIER_NAME)
        }
        setTimePeriod(spendingTimePeriod, false)
        callTotalSpendingSKUBreakup(calledFrom)
        btnBack = findViewById(R.id.report_total_spending_bak_btn)
        btnBack.setOnClickListener(this)
        if (calledFrom == ReportApi.TOTAL_SPENDING_CATEGORY_FRAGMENT || calledFrom == ReportApi.TOTAL_SPENDING_TAG_FRAGMENT) txtHeading.setText(
            spendingDetailsByValue
        ) else if (calledFrom == ReportApi.REPORT_SEARCH_ACTIVITY) txtHeading.setText(SKUName) else if (calledFrom == ReportApi.TOTAL_SPENDING_SUPPLIER_FRAGMENT) txtHeading.setText(
            supplierName
        )
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtTimePeriod, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtTotalSpendingHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtTotalSpendingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtTotalWithoutGST, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtAvergaeDailySpend,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtSpendingDataHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(TxtNoDataSupplier, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
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

    private fun setTimePeriod(timePeriod: String?, isUpdateSpendingData: Boolean) {
        var timePeriodRange = ""
        if (timePeriod == ReportTimePeriod.THISWEEK.timePeriodValue) {
            val thisWeekRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
            timePeriodRange = thisWeekRange.startEndRangeDateMonthYearFormat
            startDate = thisWeekRange.startDateMonthYearString
            endDate = thisWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTWEEK.timePeriodValue) {
            val lastWeekRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
            timePeriodRange = lastWeekRange.startEndRangeDateMonthYearFormat
            startDate = lastWeekRange.startDateMonthYearString
            endDate = lastWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTWEEK.timePeriodValue) {
            val lastToLastWeekRange =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_WEEK)
            timePeriodRange = lastToLastWeekRange.startEndRangeDateMonthYearFormat
            startDate = lastToLastWeekRange.startDateMonthYearString
            endDate = lastToLastWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.THISMONTH.timePeriodValue) {
            val thisMonthRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.CURRENT_MONTH)
            timePeriodRange = thisMonthRange.startEndRangeDateMonthYearFormat
            startDate = thisMonthRange.startDateMonthYearString
            endDate = thisMonthRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTMONTH.timePeriodValue) {
            val lastMonthRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_MONTH)
            timePeriodRange = lastMonthRange.startEndRangeDateMonthYearFormat
            startDate = lastMonthRange.startDateMonthYearString
            endDate = lastMonthRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTMONTH.timePeriodValue) {
            val lastToLastMonthRange =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_MONTH)
            timePeriodRange = lastToLastMonthRange.startEndRangeDateMonthYearFormat
            startDate = lastToLastMonthRange.startDateMonthYearString
            endDate = lastToLastMonthRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.CUSTOM.timePeriodValue) {
            timePeriodRange = "$startDate - $endDate"
        }
        txtTimePeriod!!.text = timePeriodRange
        if (isUpdateSpendingData) callTotalSpendingSKUBreakup(calledFrom)
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
                //poulate the date picker
                val dpd = GetDatePickerFromTo(this, startDate, endDate)
                dpd.show(fragmentManager, "Datepickerdialog")
            }
            R.id.report_total_spending_bak_btn -> finish()
            else -> {}
        }
    }

    override fun onDateSet(
        view: DatePickerDialog,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        yearEnd: Int,
        monthOfYearEnd: Int,
        dayOfMonthEnd: Int,
    ) {
        val startDate = Calendar.getInstance(DateHelper.marketTimeZone)
        startDate[year, monthOfYear] = dayOfMonth
        val endDate = Calendar.getInstance(DateHelper.marketTimeZone)
        endDate[yearEnd, monthOfYearEnd] = dayOfMonthEnd
        if (endDate.before(startDate)) {
            getToastRed("Please enter valid date range")
        } else {
            timePickerDialog!!.dismiss()
            spendingTimePeriod = ReportTimePeriod.CUSTOM.timePeriodValue
            val startEndDateRange = DateHelper.returnStartDateEndDateCustomRange(startDate, endDate)
            val startEndDate = startEndDateRange.startEndRangeDateMonthYearFormat
            txtTimePeriod!!.text = startEndDate
            //load data for new date range
            this.startDate = startEndDateRange.startDateMonthYearString
            this.endDate = startEndDateRange.endDateMonthYearString
            //callSpendingDetailAPI(true);
            callTotalSpendingSKUBreakup(calledFrom)
        }
    }

    fun setChart(calledFrom: String?, spendingData: List<ReportResponse.TotalSpendingDetailsByRangeData>) {
        val entries: MutableList<BarEntry> = ArrayList()
        val dayValues: MutableList<String> = ArrayList()
        var i = 0
        var totalAmount = 0.0
        var totalAmountWithoutCredits = 0.0
        var totalTaxSpending = 0.0
        for (item in spendingData) {
            if (calledFrom == ReportApi.TOTAL_SPENDING_CATEGORY_FRAGMENT) {
                val priceDetails = PriceDetails(item.amount)
                entries.add(BarEntry(i.toFloat(), priceDetails.amount?.toFloat()!!))
                dayValues.add(DateHelper.getDayOfMonthFromDate(item.invoiceDate))
                totalAmount = totalAmount + item.amount!!
            } else if (calledFrom == ReportApi.TOTAL_SPENDING_TAG_FRAGMENT) {
                val priceDetails = PriceDetails(item.totalSpendingAmount)
                entries.add(BarEntry(i.toFloat(), priceDetails.amount?.toFloat()!!))
                dayValues.add(DateHelper.getDayOfMonthFromDate(item.dateTime as Long))
                totalAmount = totalAmount + item.totalSpendingAmount!!
                totalAmountWithoutCredits = totalAmount
                totalTaxSpending = totalTaxSpending + item.taxSpending!!
            } else if (calledFrom == ReportApi.TOTAL_SPENDING_SUPPLIER_FRAGMENT || calledFrom == ReportApi.REPORT_SEARCH_ACTIVITY) {
                val priceDetails = PriceDetails(item.totalSpendingAmount)
                entries.add(BarEntry(i.toFloat(), priceDetails.amount?.toFloat()!!))
                dayValues.add(DateHelper.getDayOfMonthFromDate(item.dateTime as Long))
                txtTotalWithoutGST!!.visibility = View.VISIBLE
                if (item.isCredits && item.totalSpendingAmount != 0.0) {
                    totalAmountWithoutCredits = totalAmount
                    totalAmount = totalAmount - item.totalSpendingAmount!!
                } else {
                    totalAmount = totalAmount + item.totalSpendingAmount!!
                    totalAmountWithoutCredits = totalAmount
                }
                totalTaxSpending = totalTaxSpending + item.taxSpending!!
            }
            i++
        }
        val priceDetails = PriceDetails(totalAmount)
        txtTotalSpendingAmount!!.text = priceDetails.displayValue
        if (totalTaxSpending != 0.0) {
            txtTotalWithoutGST!!.visibility = View.VISIBLE
            val totWithoutGST = totalAmountWithoutCredits - totalTaxSpending
            val priceDetailsTotNoGst = PriceDetails(totWithoutGST)
            txtTotalWithoutGST!!.text = getString(
                R.string.report_total_without_gst,
                ZeemartAppConstants.Market.`this`.taxCode
            )
            txtAvergaeDailySpend!!.text = priceDetailsTotNoGst.displayValue
        } else {
            txtTotalWithoutGST!!.visibility = View.GONE
            txtAvergaeDailySpend!!.visibility = View.GONE
        }
        val dataSet = BarDataSet(entries, "") // add entries to dataset
        dataSet.setDrawValues(false)
        dataSet.color = resources.getColor(R.color.chart_yellow)
        val data = BarData(dataSet)
        barChart!!.data = data
        val d = Description()
        d.text = ""
        barChart!!.description = d
        barChart!!.setScaleEnabled(false)
        barChart!!.legend.isEnabled = false
        val mv = CustomMarkerViewCharts(
            this@ReportsSpendingDetailsActivity,
            R.layout.chart_marker_layout,
            false
        )
        barChart!!.marker = mv
        val xAxis = barChart!!.xAxis
        xAxis.valueFormatter = ChartXAxisValueFormatter(dayValues.toTypedArray())
        barChart!!.setDrawGridBackground(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        val yAxisRight = barChart!!.axisRight
        yAxisRight.setDrawAxisLine(false)
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawGridLines(false)
        val yAxisLeft = barChart!!.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.valueFormatter = ChartYAxisValueFormatter()
        barChart!!.invalidate() // refresh
        barChart!!.animateXY(1000, 1000, Easing.EasingOption.Linear, Easing.EasingOption.Linear)
    }

    private fun callTotalSpendingSKUBreakup(calledFrom: String?) {
        val startDateLong = DateHelper.returnEpochTimeSOD(startDate)
        val endDateLong = DateHelper.returnEpochTimeEOD(endDate)
        progressBar!!.visibility = View.VISIBLE
        if (calledFrom == ReportApi.TOTAL_SPENDING_TAG_FRAGMENT) {
            //call total spending by range range API
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setStartDate(startDateLong)
            apiParamsHelper.setEndDate(endDateLong)
            apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.DAILY)
            apiParamsHelper.setTag(spendingDetailsByValue!!)
            ReportApi.retrieveTagsDetailsChatSpending(
                this,
                apiParamsHelper,
                object : ReportApi.TotalSpendingByTagDetailChartResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingByTagDetailChart?) {
                        if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                            setChart(calledFrom, reportResponseData.data!!)
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {}
                })
            ReportApi.retrieveTagsDetailsSpending(
                this,
                startDateLong,
                endDateLong,
                spendingDetailsByValue,
                object : ReportApi.TotalSpendingByTagDetailResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingByTagDetail?) {
                        progressBar!!.visibility = View.GONE
                        if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                            ReportTotalSpendingSummary!!.visibility = View.VISIBLE
                            lstTotalSpendingSupplier!!.visibility = View.VISIBLE
                            txtSpendingDataHeading!!.visibility = View.VISIBLE
                            lytReportNoData!!.visibility = View.GONE
                            val reportResponseList: MutableList<ReportResponse.TotalSpendingSkuData> = ArrayList()
                            for (i in reportResponseData.data!!.indices) {
                                val obj = ReportResponse.TotalSpendingSkuData()
                                obj.productName = reportResponseData.data!![i].productName
                                obj.sku = reportResponseData.data!![i].sku
                                obj.supplier = reportResponseData.data!![i].supplier
                                obj.totalSpendingOnSkuAmount = reportResponseData.data!![i].total
                                obj.isCredits = false
                                reportResponseList.add(obj)
                            }
                            getUpdatedData(calledFrom, reportResponseList)
                        } else {
                            ReportTotalSpendingSummary!!.visibility = View.GONE
                            lstTotalSpendingSupplier!!.visibility = View.GONE
                            txtSpendingDataHeading!!.visibility = View.GONE
                            lytReportNoData!!.visibility = View.VISIBLE
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        progressBar!!.visibility = View.GONE
                    }
                })
        } else {
            var supplierId: String? = null
            var category: String? = null
            if (calledFrom == ReportApi.TOTAL_SPENDING_CATEGORY_FRAGMENT) category =
                spendingDetailsByValue else if (calledFrom == ReportApi.TOTAL_SPENDING_SUPPLIER_FRAGMENT || calledFrom == ReportApi.REPORT_SEARCH_ACTIVITY) supplierId =
                spendingDetailsByValue
            //call total spending by range range API
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setStartDate(startDateLong)
            apiParamsHelper.setEndDate(endDateLong)
            apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.DAILY)
            if (!StringHelper.isStringNullOrEmpty(supplierId)) apiParamsHelper.setSupplierId(
                supplierId!!
            )
            if (!StringHelper.isStringNullOrEmpty(category)) apiParamsHelper.setCategory(category!!)
            if (!StringHelper.isStringNullOrEmpty(category)) apiParamsHelper.setCategory(category!!)
            ReportApi.retrieveTotalSpendings(
                this,
                apiParamsHelper,
                object : ReportApi.TotalSpendingDetailByRangeResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingDetailsByRange?) {
                        if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                            setChart(calledFrom, reportResponseData.data!!)
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {}
                })
            //call total spending by sku api
            ReportApi.retrieveSkuSpendings(
                this,
                startDateLong,
                endDateLong,
                supplierId,
                category,
                object : ReportApi.TotalSpendingBySkuResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingBySku?) {
                        progressBar!!.visibility = View.GONE
                        if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                            ReportTotalSpendingSummary!!.visibility = View.VISIBLE
                            lstTotalSpendingSupplier!!.visibility = View.VISIBLE
                            txtSpendingDataHeading!!.visibility = View.VISIBLE
                            lytReportNoData!!.visibility = View.GONE
                            getUpdatedData(calledFrom, reportResponseData.data as MutableList<ReportResponse.TotalSpendingSkuData>)
                        } else {
                            ReportTotalSpendingSummary!!.visibility = View.GONE
                            lstTotalSpendingSupplier!!.visibility = View.GONE
                            txtSpendingDataHeading!!.visibility = View.GONE
                            lytReportNoData!!.visibility = View.VISIBLE
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        progressBar!!.visibility = View.GONE
                    }
                })
        }
    }

    private fun getUpdatedData(
        calledFrom: String?,
        reportResponseList: MutableList<ReportResponse.TotalSpendingSkuData>,
    ) {
        val isShowSupplierName: Boolean
        isShowSupplierName =
            if (calledFrom == ReportApi.TOTAL_SPENDING_CATEGORY_FRAGMENT || calledFrom == ReportApi.TOTAL_SPENDING_TAG_FRAGMENT) {
                true
            } else {
                false
            }
        for (i in reportResponseList.indices) {
            if (reportResponseList[i].isCredits && reportResponseList[i].totalSpendingOnSkuAmount == 0.0 && reportResponseList[i].productName == null) {
                reportResponseList.removeAt(i)
            }
        }

        //set the list for the total spending by supplier

        //set the list for the total spending by supplier
        lstTotalSpendingSupplier.adapter =
            TotalSpendingSkuListAdapter(
                this,
                reportResponseList,
                object : OnTotalSpendingItemClick {
                    override fun onItemClick(item: String?, SKUName: String?) {
                        //call the Total spending detail Activity.
                        val newIntent = Intent(
                            this@ReportsSpendingDetailsActivity,
                            ReportsSpendingBySKUActivity::class.java
                        )
                        newIntent.putExtra(ReportApi.START_DATE, startDate)
                        newIntent.putExtra(ReportApi.END_DATE, endDate)
                        newIntent.putExtra(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR, item)
                        newIntent.putExtra(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
                        newIntent.putExtra(ReportApi.SKU_NAME, SKUName)
                        if (calledFrom == ReportApi.TOTAL_SPENDING_CATEGORY_FRAGMENT) newIntent.putExtra(
                            ReportApi.CATEGORY_NAME,
                            spendingDetailsByValue
                        )
                        if (calledFrom == ReportApi.TOTAL_SPENDING_SUPPLIER_FRAGMENT) newIntent.putExtra(
                            ReportApi.SUPPLIER_ID,
                            spendingDetailsByValue
                        )
                        if (calledFrom == ReportApi.TOTAL_SPENDING_TAG_FRAGMENT) newIntent.putExtra(
                            ReportApi.TAG,
                            spendingDetailsByValue
                        )
                        newIntent.putExtra(
                            ReportApi.CALLED_FROM_FRAGMENT,
                            ReportApi.REPORT_SPENDING_DETAIL_ACTIVITY
                        )
                        startActivity(newIntent)
                    }
                },
                isShowSupplierName
            )
    }
}