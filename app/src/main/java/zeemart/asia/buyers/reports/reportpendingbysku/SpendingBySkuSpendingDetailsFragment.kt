package zeemart.asia.buyers.reports.reportpendingbysku

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SpendingBySkuDateBoughtAdapter
import zeemart.asia.buyers.adapter.SpendingBySkuQuantityBoughtAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DialogHelper.GetDatePickerFromTo
import zeemart.asia.buyers.helper.ReportApi.TotalSpendingDetailByRangeResponseListener
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.reportsimport.ReportResponse
import zeemart.asia.buyers.models.reportsimport.SkuDateBoughtUI
import zeemart.asia.buyers.reports.ReportDataController
import zeemart.asia.buyers.reports.ReportTimePeriod
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SpendingBySkuSpendingDetailsFragment : Fragment(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener {
    private var spendingTimePeriod: String? = null
    private var txtTotalSpendingAmount: TextView? = null
    private var txtNoData: TextView? = null
    private var skuValue: String? = null
    private lateinit var lstQuantityBought: RecyclerView
    private lateinit var lstByDate: RecyclerView
    private lateinit var txtAvergaeDailySpend: TextView
    private var selectedStartDate: String? = ""
    private var selectedEndDate: String? = ""
    private var txtTotalSpendingHeading: TextView? = null
    private lateinit var lytChangeCalendarDate: LinearLayout
    private var txtTimePeriod: TextView? = null
    private var timePickerDialog: Dialog? = null
    private var isFragmentAttached = false
    private var barChart: BarChart? = null
    private var lytNoDataReport: LinearLayout? = null
    private var lytSpendingDataResults: NestedScrollView? = null
    private lateinit var txtAverageDailySpendHeading: TextView
    private var txtQuantityBought: TextView? = null
    private var txtByDate: TextView? = null
    private var supplierId: String? = null
    private var categoryName: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val bundle = arguments
        if (bundle!!.containsKey(ReportApi.SPENDING_DATA_PERIOD)) {
            spendingTimePeriod = bundle.getString(ReportApi.SPENDING_DATA_PERIOD)
        }
        if (bundle.containsKey(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)) {
            skuValue = bundle.getString(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)
        }
        if (bundle.containsKey(ReportApi.START_DATE)) {
            selectedStartDate = bundle.getString(ReportApi.START_DATE)
        }
        if (bundle.containsKey(ReportApi.END_DATE)) {
            selectedEndDate = bundle.getString(ReportApi.END_DATE)
        }
        if (bundle.containsKey(ReportApi.SUPPLIER_ID)) supplierId =
            bundle.getString(ReportApi.SUPPLIER_ID)
        if (bundle.containsKey(ReportApi.CATEGORY_NAME)) categoryName =
            bundle.getString(ReportApi.CATEGORY_NAME)
        val v =
            inflater.inflate(R.layout.fragment_spending_by_sku_spending_details, container, false)
        barChart = v.findViewById(R.id.barChart)
        txtTotalSpendingAmount = v.findViewById(R.id.report_total_spending_amount)
        lstQuantityBought = v.findViewById(R.id.lst_spending_by_sku_quantity_brought_list)
        lstQuantityBought.setLayoutManager(LinearLayoutManager(activity))
        lstQuantityBought.setNestedScrollingEnabled(false)
        lstByDate = v.findViewById(R.id.lst_spending_by_sku_by_date)
        lstByDate.setLayoutManager(LinearLayoutManager(activity))
        lstByDate.setNestedScrollingEnabled(false)
        txtAvergaeDailySpend = v.findViewById(R.id.report_total_spending_average_daily_value)
        txtAvergaeDailySpend.setVisibility(View.GONE)
        txtTotalSpendingHeading = v.findViewById(R.id.txt_total_spending_detail_heading)
        lytChangeCalendarDate = v.findViewById(R.id.report_total_spending_lyt_calendar)
        lytChangeCalendarDate.setOnClickListener(this)
        txtTimePeriod = v.findViewById(R.id.report_total_spending_time_range_val)
        txtAverageDailySpendHeading = v.findViewById(R.id.report_dashboard_week_txt_date_range)
        txtAverageDailySpendHeading.setVisibility(View.GONE)
        txtNoData = v.findViewById(R.id.txt_no_data)
        setTypefaceView(txtNoData, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        lytNoDataReport = v.findViewById(R.id.report_total_spending_no_layout)
        lytSpendingDataResults = v.findViewById(R.id.lyt_data_spending_details)
        txtQuantityBought = v.findViewById(R.id.txt_quantity_bought_heading)
        txtByDate = v.findViewById(R.id.txt_by_date)
        setFont()
        return v
    }

    private fun setFont() {
        setTypefaceView(txtTimePeriod, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtTotalSpendingHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtTotalSpendingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtAverageDailySpendHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtAvergaeDailySpend,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtQuantityBought, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtByDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTimePeriod(spendingTimePeriod, false)
        callSpendingDetailAPI()
    }

    fun setChart(spendingData: List<ReportResponse.TotalSpendingDetailsByRangeData>) {
        val entries: MutableList<BarEntry> = ArrayList()
        val startDateLong = DateHelper.returnEpochTimeSOD(selectedStartDate)
        val endDateLong = DateHelper.returnEpochTimeEOD(selectedEndDate)
        val chartData = ChartsHelper.getAllDatesForChartInPeriod(startDateLong, endDateLong)
        val mapChart: MutableMap<String, ChartsHelper.ChartModel> = HashMap()
        for (item in chartData) {
            mapChart[DateHelper.getDateInDateMonthYearFormat(item.invoiceDate)] = item
        }
        for (data in spendingData) {
            val dateStr = DateHelper.getDateInDateMonthYearFormat(data.invoiceDate)
            if (mapChart.containsKey(dateStr)) mapChart[dateStr]!!.totalPrice?.addAmount(data.amount!!)
        }
        val chartDataList: MutableList<ChartsHelper.ChartModel> = ArrayList()
        for ((_, value) in mapChart) {
            chartDataList.add(value)
        }
        Collections.sort(chartDataList) { rhs, lhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            if (lhs.invoiceDate!! > rhs.invoiceDate!!) -1 else if (lhs.invoiceDate!! < rhs.invoiceDate!!) 1 else 0
        }
        val dayValues: MutableList<String> = ArrayList()
        var i = 0
        for (item in chartDataList) {
            entries.add(BarEntry(i.toFloat(), item.totalPrice?.amount?.toFloat()!!))
            dayValues.add(DateHelper.getDayOfMonthFromDate(item.invoiceDate as Long))
            i++
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
        val mv = CustomMarkerViewCharts(activity, R.layout.chart_marker_layout, false)
        barChart!!.marker = mv
        val xAxis = barChart!!.xAxis
        xAxis.valueFormatter = ChartsHelper.ChartXAxisValueFormatter(dayValues.toTypedArray())
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
        yAxisLeft.valueFormatter = ChartsHelper.ChartYAxisValueFormatter()
        barChart!!.invalidate() // refresh
        barChart!!.animateXY(1000, 1000, Easing.EasingOption.Linear, Easing.EasingOption.Linear)
    }

    private fun updateSKUSpendingDetails(spendingData: List<ReportResponse.TotalSpendingDetailsByRangeData>) {
        val controller = ReportDataController()
        val quantityBoughtlist = controller.getSKUByQuantityBoughtData(ArrayList(spendingData))
        val dateBoughtList = controller.getSKUByDateData(ArrayList(spendingData))
        var amount = 0.0
        for (i in quantityBoughtlist.indices) {
            amount = amount + quantityBoughtlist[i].amount!!
        }
        val amountPriceDetail = PriceDetails(amount)
        txtTotalSpendingAmount!!.text = amountPriceDetail.displayValue
        if (amountPriceDetail.amount != 0.0) {
            lytNoDataReport!!.visibility = View.GONE
            lytSpendingDataResults!!.visibility = View.VISIBLE
        } else {
            lytSpendingDataResults!!.visibility = View.GONE
            lytNoDataReport!!.visibility = View.VISIBLE
        }
        if (amountPriceDetail.amount != 0.0) {
            //set the list for the total spending by supplier
            lstQuantityBought!!.adapter =
                SpendingBySkuQuantityBoughtAdapter(requireContext(), quantityBoughtlist)
            lstByDate!!.adapter = SpendingBySkuDateBoughtAdapter(requireContext(), dateBoughtList as kotlin.collections.ArrayList<SkuDateBoughtUI>)
            val days =
                ReportApi.daysBetweenDates(spendingTimePeriod!!, selectedStartDate, selectedEndDate)
            val priceDetails = PriceDetails(amountPriceDetail.amount?.div(days))
            txtAvergaeDailySpend!!.text = priceDetails.displayValue
        } else {
            txtAvergaeDailySpend!!.text = PriceDetails().displayValue
        }
        setChart(spendingData)
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
                val dpd = GetDatePickerFromTo(this, selectedStartDate, selectedEndDate)
                dpd.show(requireActivity().fragmentManager, "Datepickerdialog")
            }
            else -> {}
        }
    }

    private fun setTimePeriod(timePeriod: String?, isCallSpendingDetailAPI: Boolean) {
        var timePeriodRange = ""
        if (timePeriod == ReportTimePeriod.THISWEEK.timePeriodValue) {
            val thisWeekRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
            timePeriodRange = thisWeekRange.startEndRangeDateMonthYearFormat
            selectedStartDate = thisWeekRange.startDateMonthYearString
            selectedEndDate = thisWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTWEEK.timePeriodValue) {
            val lastWeekRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
            timePeriodRange = lastWeekRange.startEndRangeDateMonthYearFormat
            selectedStartDate = lastWeekRange.startDateMonthYearString
            selectedEndDate = lastWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTWEEK.timePeriodValue) {
            val lastToLastWeekRange =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_WEEK)
            timePeriodRange = lastToLastWeekRange.startEndRangeDateMonthYearFormat
            selectedStartDate = lastToLastWeekRange.startDateMonthYearString
            selectedEndDate = lastToLastWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.THISMONTH.timePeriodValue) {
            val thisMonthRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.CURRENT_MONTH)
            timePeriodRange = thisMonthRange.startEndRangeDateMonthYearFormat
            selectedStartDate = thisMonthRange.startDateMonthYearString
            selectedEndDate = thisMonthRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTMONTH.timePeriodValue) {
            val lastMonthRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_MONTH)
            timePeriodRange = lastMonthRange.startEndRangeDateMonthYearFormat
            selectedStartDate = lastMonthRange.startDateMonthYearString
            selectedEndDate = lastMonthRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTMONTH.timePeriodValue) {
            val lastToLastMonthRange =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_MONTH)
            timePeriodRange = lastToLastMonthRange.startEndRangeDateMonthYearFormat
            selectedStartDate = lastToLastMonthRange.startDateMonthYearString
            selectedEndDate = lastToLastMonthRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.CUSTOM.timePeriodValue) {
            timePeriodRange = "$selectedStartDate - $selectedEndDate"
        }
        if (isCallSpendingDetailAPI) {
            callSpendingDetailAPI()
        }
        txtTimePeriod!!.text = timePeriodRange
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
            val startEndDateRange = DateHelper.returnStartDateEndDateCustomRange(startDate, endDate)
            val startEndDate = startEndDateRange.startEndRangeDateMonthYearFormat
            selectedStartDate = startEndDateRange.startDateMonthYearString
            selectedEndDate = startEndDateRange.endDateMonthYearString
            txtTimePeriod!!.text = startEndDate
            callSpendingDetailAPI()
        }
    }

    private fun createTimePickerDialog() {
        timePickerDialog = Dialog(requireActivity(), R.style.CustomDialogTheme)
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

    private fun callSpendingDetailAPI() {
        //call spending detail API
        //call the API to get the spending details for time range startDate end Date
        val startDateLong = DateHelper.returnEpochTimeSOD(selectedStartDate)
        val endDateLong = DateHelper.returnEpochTimeEOD(selectedEndDate)
        //new GetOutletSpendingHistoryData(getActivity(), this).getOutletSpendingData(startDateLong, endDateLong, true, SharedPref.getDefaultOutlet().getOutletId());
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        if (!StringHelper.isStringNullOrEmpty(supplierId)) apiParamsHelper.setSupplierId(supplierId!!)
        if (!StringHelper.isStringNullOrEmpty(categoryName)) apiParamsHelper.setCategory(
            categoryName!!
        )
        apiParamsHelper.setSku(skuValue!!)
        apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.DAILY)
        ReportApi.retrieveTotalSpendings(
            activity,
            apiParamsHelper,
            object : TotalSpendingDetailByRangeResponseListener {
                override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingDetailsByRange?) {
                    if (isFragmentAttached && reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) updateSKUSpendingDetails(
                        reportResponseData.data!!
                    ) else {
                        lytSpendingDataResults!!.visibility = View.GONE
                        lytNoDataReport!!.visibility = View.VISIBLE
                    }
                }

                override fun onTotalSpendingResponseError(error: VolleyError?) {}
            })
    }

    companion object {
        @JvmStatic
        fun newInstance(
            startDate: String?,
            endDate: String?,
            spendingTimePeriod: String?,
            skuValue: String?
        ): SpendingBySkuSpendingDetailsFragment {
            val fragment = SpendingBySkuSpendingDetailsFragment()
            val args = Bundle()
            args.putString(ReportApi.START_DATE, startDate)
            args.putString(ReportApi.END_DATE, endDate)
            args.putString(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
            args.putString(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR, skuValue)
            fragment.arguments = args
            return fragment
        }
    }
}