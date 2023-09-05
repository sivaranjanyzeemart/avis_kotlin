package zeemart.asia.buyers.reports.reporttotalspending

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.TotalSpendingCategoryListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.AnalyticsHelper.logAction
import zeemart.asia.buyers.helper.ChartsHelper.ChartModel
import zeemart.asia.buyers.helper.CustomMarkerViewCharts
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.ReportApi
import zeemart.asia.buyers.helper.ReportApi.TotalSpendingByCategoryResponseListener
import zeemart.asia.buyers.helper.ReportApi.daysBetweenDates
import zeemart.asia.buyers.interfaces.DateRangeChangeObserver
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.interfaces.ProgressIndicatorListener
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.reportsimport.ReportResponse
import zeemart.asia.buyers.reports.ReportsSpendingDetailsActivity

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [TotalSpendingCategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TotalSpendingCategoryFragment : Fragment(), DateRangeChangeObserver {
    private var spendingTimePeriod: String? = null
    private lateinit var txtTotalSpendingAmount: TextView
    private lateinit var lstTotalSpendingSupplier: RecyclerView
    private lateinit var txtAverageDailySpend: TextView
    private var selectedStartDate = ""
    private var selectedEndDate = ""
    private lateinit var txtTotalSpendingHeading: TextView
    private lateinit var lytNoSpendingData: LinearLayout
    private lateinit var txtSpendingDetailHeading: TextView
    private var txtNoReportData: TextView? = null
    private var txtChangeDateRange: TextView? = null
    private var isFragmentAttached = false
    private var chart: PieChart? = null
    private var mListener: ProgressIndicatorListener? = null
    private lateinit var lytChartReports: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val bundle = arguments
        if (bundle!!.containsKey(ReportApi.SPENDING_DATA_PERIOD)) {
            spendingTimePeriod = bundle.getString(ReportApi.SPENDING_DATA_PERIOD)
        }

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_total_spending, container, false)
        txtTotalSpendingAmount = v.findViewById(R.id.report_total_spending_amount)
        txtTotalSpendingAmount.setVisibility(View.GONE)
        lstTotalSpendingSupplier = v.findViewById(R.id.lst_total_spending_by_supplier)
        lytChartReports = v.findViewById(R.id.report_total_spending_summary)
        txtChangeDateRange = v.findViewById(R.id.txt_change_date_range)
        txtNoReportData = v.findViewById(R.id.txt_no_report_data)
        lstTotalSpendingSupplier.setLayoutManager(LinearLayoutManager(activity))
        //lstTotalSpendingSupplier.setNestedScrollingEnabled(false);
        txtAverageDailySpend = v.findViewById(R.id.report_total_spending_average_daily_value)
        txtTotalSpendingHeading = v.findViewById(R.id.txt_total_spending_detail_heading)
        txtTotalSpendingHeading.setVisibility(View.GONE)
        v.findViewById<View>(R.id.avg_daily_spend).visibility = View.GONE
        txtSpendingDetailHeading =
            v.findViewById(R.id.report_total_spending_spending_detail_header_text)
        lytNoSpendingData = v.findViewById(R.id.report_total_spending_no_data_layout)
        setFont()
        chart = v.findViewById(R.id.chart)
        val startDateEndDate = ReportApi.getDateRange(spendingTimePeriod!!)
        ReportApi.retrieveCategorySpendings(
            activity,
            startDateEndDate?.startDateMillis!!,
            startDateEndDate?.endDateMillis,
            object : TotalSpendingByCategoryResponseListener {
                override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingByCategory?) {
                    mListener!!.visibilityGone()
                    if (isFragmentAttached) {
                        if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                            var totalSpendingCategoryDataList: List<ReportResponse.TotalSpendingCategoryData> =
                                ArrayList()
                            totalSpendingCategoryDataList = reportResponseData.data!!
                            updateCategoryTotalSpending(totalSpendingCategoryDataList)
                        } else {
                            txtSpendingDetailHeading.setVisibility(View.GONE)
                            lytNoSpendingData.setVisibility(View.VISIBLE)
                            lytChartReports.setVisibility(View.GONE)
                            lstTotalSpendingSupplier.setVisibility(View.GONE)
                            txtAverageDailySpend.setText(PriceDetails().displayValue)
                        }
                    }
                }

                override fun onTotalSpendingResponseError(error: VolleyError?) {
                    if (isFragmentAttached) mListener!!.visibilityGone()
                }
            })
        return v
    }

    private fun setFont() {
        setTypefaceView(
            txtTotalSpendingHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtTotalSpendingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtAverageDailySpend,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtSpendingDetailHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtChangeDateRange, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtNoReportData, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
        mListener = context as ProgressIndicatorListener
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onDateRangeChanged(timePeriodValue: String?, startDate: String?, endDate: String?) {
        spendingTimePeriod = timePeriodValue
        selectedStartDate = startDate!!
        selectedEndDate = endDate!!
        val startDateLong = DateHelper.returnEpochTimeSOD(startDate)
        val endDateLong = DateHelper.returnEpochTimeEOD(endDate)
        //call the API to get the spending details for time range startDate end Date
        mListener!!.visibilityVisible()
        ReportApi.retrieveCategorySpendings(
            activity,
            startDateLong,
            endDateLong,
            object : TotalSpendingByCategoryResponseListener {
                override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingByCategory?) {
                    if (isFragmentAttached) {
                        mListener!!.visibilityGone()
                        var totalSpendingCategoryDataList: List<ReportResponse.TotalSpendingCategoryData> =
                            ArrayList()
                        if (reportResponseData != null && reportResponseData.data != null) {
                            totalSpendingCategoryDataList = reportResponseData.data!!
                        }
                        updateCategoryTotalSpending(totalSpendingCategoryDataList)
                    }
                }

                override fun onTotalSpendingResponseError(error: VolleyError?) {
                    if (isFragmentAttached) mListener!!.visibilityGone()
                }
            })
    }

    fun setChart(supplierModels: List<ReportResponse.TotalSpendingCategoryData>) {
        val mapChart: Map<String, ChartModel> = HashMap()
        val entries = ArrayList<PieEntry>()
        var i = 0
        var totalOthers = 0.0
        for (data in supplierModels) {
            if (i < 6) entries.add(
                PieEntry(
                    data.totalamountCategory?.toFloat()!!,
                    data.category
                )
            ) else totalOthers += data.totalamountCategory!!
            i++
        }
        if (totalOthers > 0) entries.add(PieEntry(totalOthers.toFloat(), "Others"))
        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 0f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // add a lot of colors
        val colors = ArrayList<Int>()
        colors.add(requireActivity().resources.getColor(R.color.chart_blue))
        colors.add(requireActivity().resources.getColor(R.color.chart_light_blue))
        colors.add(requireActivity().resources.getColor(R.color.chart_green))
        colors.add(requireActivity().resources.getColor(R.color.chart_yellow))
        colors.add(requireActivity().resources.getColor(R.color.chart_orange))
        colors.add(requireActivity().resources.getColor(R.color.chart_red))
        colors.add(requireActivity().resources.getColor(R.color.dark_grey))
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(0f)
        // undo all highlights
        chart!!.setDrawEntryLabels(false)
        chart!!.setDrawCenterText(false)
        val d = Description()
        d.text = ""
        chart!!.description = d
        chart!!.highlightValues(null)
        chart!!.legend.isWordWrapEnabled = true
        chart!!.setTouchEnabled(true)
        val mv = CustomMarkerViewCharts(activity, R.layout.chart_marker_layout, false)
        chart!!.marker = mv
        chart!!.legend.position = Legend.LegendPosition.BELOW_CHART_CENTER
        chart!!.data = data
        chart!!.invalidate()
        chart!!.animateXY(1000, 1000, Easing.EasingOption.Linear, Easing.EasingOption.Linear)
    }

    private fun updateCategoryTotalSpending(reportSpendingByCategoryDataList: List<ReportResponse.TotalSpendingCategoryData>) {
        if (isFragmentAttached) {
            var amount = 0.0
            for (i in reportSpendingByCategoryDataList.indices) {
                amount = amount + reportSpendingByCategoryDataList[i].totalamountCategory!!
            }
            val amountPriceDetail = PriceDetails()
            amountPriceDetail.amount = amount
            txtTotalSpendingAmount.text = amountPriceDetail.displayValue
            if (amountPriceDetail.amount != 0.0) {
                //set the list for the total spending by supplier
                txtSpendingDetailHeading.visibility = View.VISIBLE
                lytNoSpendingData.visibility = View.GONE
                lytChartReports.visibility = View.VISIBLE
                lstTotalSpendingSupplier.visibility = View.VISIBLE
                lstTotalSpendingSupplier.adapter = TotalSpendingCategoryListAdapter(
                    requireContext(),
                    reportSpendingByCategoryDataList,
                    object : OnTotalSpendingItemClick {
                        override fun onItemClick(item: String?, SKUName: String?) {
                            //call the Total spending detail Activity.
                            logAction(
                                activity,
                                AnalyticsHelper.TAP_ITEM_TOTAL_SPENDING_CATEGORY_LIST
                            )
                            val newIntent =
                                Intent(activity, ReportsSpendingDetailsActivity::class.java)
                            newIntent.putExtra(ReportApi.START_DATE, selectedStartDate)
                            newIntent.putExtra(ReportApi.END_DATE, selectedEndDate)
                            newIntent.putExtra(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR, item)
                            newIntent.putExtra(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
                            newIntent.putExtra(
                                ReportApi.CALLED_FROM_FRAGMENT,
                                ReportApi.TOTAL_SPENDING_CATEGORY_FRAGMENT
                            )
                            startActivity(newIntent)
                        }
                    })
                val days =
                    daysBetweenDates(spendingTimePeriod!!, selectedStartDate, selectedEndDate)
                val priceDetails = PriceDetails()
                priceDetails.amount = amountPriceDetail.amount!! / days
                txtAverageDailySpend.text = priceDetails.displayValue
            } else {
                txtSpendingDetailHeading.visibility = View.GONE
                lytNoSpendingData.visibility = View.VISIBLE
                lstTotalSpendingSupplier.visibility = View.GONE
                lytChartReports.visibility = View.GONE
                txtAverageDailySpend.text = PriceDetails().displayValue
            }
            setChart(reportSpendingByCategoryDataList)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of fragment
         *
         * @return A new instance of fragment TotalSpendingCategoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(spendingTimePeriod: String?): TotalSpendingCategoryFragment {
            val fragment = TotalSpendingCategoryFragment()
            val args = Bundle()
            args.putString(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
            fragment.arguments = args
            return fragment
        }
    }
}