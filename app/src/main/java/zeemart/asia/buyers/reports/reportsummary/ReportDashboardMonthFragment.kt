package zeemart.asia.buyers.reports.reportsummary

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.android.volley.VolleyError
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getTwoDecimalQuantity
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.PriceUpdateReportSummaryListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.ChartsHelper.ChartModel
import zeemart.asia.buyers.helper.ChartsHelper.ChartXAxisValueFormatter
import zeemart.asia.buyers.helper.DateHelper.getDateInDateMonthYearFormat
import zeemart.asia.buyers.helper.DateHelper.pastDaysStartEndDate
import zeemart.asia.buyers.helper.ReportApi.TotalSpendingDetailByRangeResponseListener
import zeemart.asia.buyers.helper.ReportApi.TotalSpendingSupplierResponseListener
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.interfaces.PriceUpdateDataListener
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse
import zeemart.asia.buyers.models.reportsimport.ReportResponse
import zeemart.asia.buyers.network.GetPriceUpdateData
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.reports.ReportDataController
import zeemart.asia.buyers.reports.ReportPriceUpdateActivity
import zeemart.asia.buyers.reports.ReportTimePeriod
import zeemart.asia.buyers.reports.reportpendingbysku.ReportsSpendingBySKUActivity
import zeemart.asia.buyers.reports.reporttotalspending.ReportTotalSpendingActivity
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ReportDashboardMonthFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ReportDashboardMonthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportDashboardMonthFragment : Fragment(), View.OnClickListener {
    private var mListener: ReportDashboardWeekFragment.OnFragmentInteractionListener? = null
    private var crdThisMonthSpending: CardView? = null
    private lateinit var crdLastMonthSpending: CardView
    private lateinit var crd2MonthsAgoSpending: CardView
    private var scrollSummaryMonth: NestedScrollView? = null
    private lateinit var swipeRefreshReportMonth: SwipeRefreshLayout
    private var threeDotLoaderBlue: CustomLoadingViewBlue? = null
    private val priceHistoryDayAgo = 90
    private val lytExpectedSpending: LinearLayout? = null
    private lateinit var lstPriceUpdates: RecyclerView
    private var txtNoPriceUpdates: TextView? = null
    private var imgNoPriceUpdates: ImageView? = null
    private var txtThisMonthSpendingAmount: TextView? = null
    private var txtLastMonthSpendingAmount: TextView? = null
    private var txtLastToLastMonthSpendinAmount: TextView? = null
    private var imgHighestTransactionSupplierLastMonth: ImageView? = null
    var lytSupplierThumbNailTransactionSupplierLastMonth: RelativeLayout? = null
    var txtSupplierThumbNailTransactionSupplierLastMonth: TextView? = null
    private var imgHighestTransactionSupplierLastToLastMonth: ImageView? = null
    var lytSupplierThumbNailTransactionSupplierLastToLastMonth: RelativeLayout? = null
    var txtSupplierThumbNailTransactionSupplierLastToLastMonth: TextView? = null
    private var txtSupplierNameHighestTransactionLastMonth: TextView? = null
    private var txtSupplierNameHighestTransactionLastToLastMonth: TextView? = null
    private var imgArrowPriceVariationLastMonth: ImageView? = null
    private var txtPriceVariationPercentageLastMonth: TextView? = null
    private var imgArrowPriceVariationLastToLastMonth: ImageView? = null
    private var txtPriceVariationPercentageLastToLastMonth: TextView? = null
    private var txtStartEndDateCurrentMonth: TextView? = null
    private var txtLastMonth: TextView? = null
    private var txtLastToLastMonth: TextView? = null
    private var dashboardMonthView: View? = null
    private var isFragmentAttached = false
    private lateinit var txtViewSpendingDetails: Button
    private var chart: LineChart? = null
    private var thisMonthSpendingButton: TextView? = null
    private var txtLastMonthHeading: TextView? = null
    private var txtHighestTransactionHeading: TextView? = null
    private lateinit var txtViewAllPriceChanges: TextView
    private var txt2MonthAgoHeading: TextView? = null
    private var txtHighestTransactionHeading2MonthAgo: TextView? = null
    private var txtDatOlderThan90DaysNotShown: TextView? = null
    private lateinit var week_txt: TextView
    private var txtRecentPriceChangesHeader: TextView? = null
    private lateinit var month_txt: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_report_dashboard_month, container, false)
        dashboardMonthView = v
        val txtCurrentMonthHeader =
            v.findViewById<TextView>(R.id.report_dashboard_week_txt_this_week_spending)
        // txtCurrentMonthHeader.setText(R.string.txt_this_month_spending);
        val lytPriceUpdateList =
            v.findViewById<RelativeLayout>(R.id.report_dashboard_week_lyt_price_changes)
        // lytPriceUpdateList.setVisibility(View.GONE);
        threeDotLoaderBlue = v.findViewById(R.id.spin_kit_loader_reports_blue)
        crdThisMonthSpending = v.findViewById(R.id.report_dashboard_week_card_total_spending_week)
        crdLastMonthSpending = v.findViewById(R.id.dashboard_report_week_card_last_week)
        crdLastMonthSpending.setOnClickListener(this)
        crd2MonthsAgoSpending =
            v.findViewById(R.id.report_dashboard_week_card_total_spending_2weeks_ago)
        crd2MonthsAgoSpending.setOnClickListener(this)
        scrollSummaryMonth = v.findViewById(R.id.nested_scroll_view_month_fragment)
        // scrollSummaryMonth.setNestedScrollingEnabled(false);
        swipeRefreshReportMonth = v.findViewById(R.id.swipe_refresh_report_month_fragment)
        txtThisMonthSpendingAmount = v.findViewById(R.id.report_dashboard_week_txt_total_spending)
        txtLastMonthSpendingAmount = v.findViewById(R.id.dashboard_report_week_last_week_value)
        txtLastToLastMonthSpendinAmount =
            v.findViewById(R.id.dashboard_report_week_last_2week_ago_value)
        imgHighestTransactionSupplierLastMonth =
            v.findViewById(R.id.dashboard_report_week_last_week_supplier_image)
        lytSupplierThumbNailTransactionSupplierLastMonth =
            v.findViewById(R.id.lyt_supplier_thumbnail)
        txtSupplierThumbNailTransactionSupplierLastMonth =
            v.findViewById(R.id.txt_supplier_thumbnail)
        setTypefaceView(
            txtSupplierThumbNailTransactionSupplierLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        lytSupplierThumbNailTransactionSupplierLastToLastMonth =
            v.findViewById(R.id.lyt_supplier_thumbnail2)
        txtSupplierThumbNailTransactionSupplierLastToLastMonth =
            v.findViewById(R.id.txt_supplier_thumbnail2)
        setTypefaceView(
            txtSupplierThumbNailTransactionSupplierLastToLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        imgHighestTransactionSupplierLastToLastMonth =
            v.findViewById(R.id.dashboard_report_week_2week_ago_supplier_image)
        txtSupplierNameHighestTransactionLastMonth =
            v.findViewById(R.id.dashboard_report_week_last_week_highest_transactions_supplier)
        txtSupplierNameHighestTransactionLastToLastMonth =
            v.findViewById(R.id.dashboard_report_week_2week_ago_highest_transactions_supplier)
        val lytExpectedSpending =
            v.findViewById<LinearLayout>(R.id.report_dashboard_week_lyt_expected_spend)
        txtLastMonth = v.findViewById(R.id.dashboard_report_week_last_week_text_heading)
        txtLastToLastMonth = v.findViewById(R.id.dashboard_report_week_2week_ago_text_heading)
        txtRecentPriceChangesHeader =
            v.findViewById(R.id.dashboard_report_week_recent_price_changes_heading)
        // txtRecentPriceChangesHeader.setVisibility(View.GONE);
        txtViewAllPriceChanges =
            v.findViewById(R.id.report_dashboard_week_txt_view_all_price_changes)
        txtViewAllPriceChanges.setOnClickListener(this)
        // txtViewAllPriceChanges.setVisibility(View.GONE);
        val txtDividerHide = v.findViewById<TextView>(R.id.dashboard_report_week_divider4)
        week_txt = v.findViewById(R.id.week_txt)
        month_txt = v.findViewById(R.id.month_txt)
        txtDividerHide.visibility = View.GONE
        lytExpectedSpending.visibility = View.GONE
        thisMonthSpendingButton = v.findViewById(R.id.report_dashboard_week_txt_this_week_spending)
        txtLastMonthHeading = v.findViewById(R.id.dashboard_report_week_last_week_text_heading)
        txtHighestTransactionHeading =
            v.findViewById(R.id.dashboard_report_week_last_week_highest_transactions)
        txt2MonthAgoHeading = v.findViewById(R.id.dashboard_report_week_2week_ago_text_heading)
        txtHighestTransactionHeading2MonthAgo =
            v.findViewById(R.id.dashboard_report_week_2week_ago_highest_transactions)
        txtDatOlderThan90DaysNotShown = v.findViewById(R.id.dashboard_report_month_90_days_message)
        lstPriceUpdates = v.findViewById(R.id.dashboard_report_week_lst_price_changes)
        lstPriceUpdates.setNestedScrollingEnabled(false)
        txtNoPriceUpdates = v.findViewById(R.id.txt_no_price_details)
        imgNoPriceUpdates = v.findViewById(R.id.img_no_price_details)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshReportMonth)
        swipeRefreshReportMonth.setOnRefreshListener(OnRefreshListener { //getDataMonth();
            swipeRefreshReportMonth.setRefreshing(false)
        })
        imgArrowPriceVariationLastMonth =
            v.findViewById(R.id.dashboard_report_week_last_week_value_updown_icon)
        txtPriceVariationPercentageLastMonth =
            v.findViewById(R.id.dashboard_report_week_last_week_value_updown_perc_value)
        imgArrowPriceVariationLastToLastMonth =
            v.findViewById(R.id.dashboard_report_week_2week_value_updown_icon)
        txtPriceVariationPercentageLastToLastMonth =
            v.findViewById(R.id.dashboard_report_week_2week_value_updown_perc_value)
        txtStartEndDateCurrentMonth = v.findViewById(R.id.report_dashboard_week_txt_date_range)
        txtViewSpendingDetails = v.findViewById(R.id.btn_view_spending_details)
        txtViewSpendingDetails.setOnClickListener(this)
        chart = v.findViewById(R.id.chart)
        month_txt.setVisibility(View.VISIBLE)
        week_txt.setVisibility(View.GONE)
        setFonts()
        dataMonth
        month_txt.setOnClickListener(View.OnClickListener { ShowDialog(context) })
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        threeDotLoaderBlue!!.visibility = View.VISIBLE
        dataMonth
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
        mListener = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_view_spending_details -> {
                AnalyticsHelper.logAction(
                    activity,
                    AnalyticsHelper.TAP_REPORTS_MONTH_VIEW_SPENDING_DETAILS
                )
                val newIntentThisMonth = Intent(activity, ReportTotalSpendingActivity::class.java)
                newIntentThisMonth.putExtra(
                    ReportApi.SPENDING_DATA_PERIOD,
                    ReportTimePeriod.THISMONTH.timePeriodValue
                )
                startActivity(newIntentThisMonth)
            }
            R.id.dashboard_report_week_card_last_week -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_REPORTS_PREVIOUS_MONTH)
                val newIntentLastMonth = Intent(activity, ReportTotalSpendingActivity::class.java)
                newIntentLastMonth.putExtra(
                    ReportApi.SPENDING_DATA_PERIOD,
                    ReportTimePeriod.LASTMONTH.timePeriodValue
                )
                startActivity(newIntentLastMonth)
            }
            R.id.report_dashboard_week_card_total_spending_2weeks_ago -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_REPORTS_PREVIOUS_TWO_MONTH)
                val newIntent2MonthsAgo = Intent(activity, ReportTotalSpendingActivity::class.java)
                newIntent2MonthsAgo.putExtra(
                    ReportApi.SPENDING_DATA_PERIOD,
                    ReportTimePeriod.LASTTOLASTMONTH.timePeriodValue
                )
                startActivity(newIntent2MonthsAgo)
            }
            R.id.report_dashboard_week_txt_view_all_price_changes -> {
                AnalyticsHelper.logAction(
                    activity,
                    AnalyticsHelper.TAP_REPORTS_VIEW_ALL_PRICE_CHANGES
                )
                val newIntentViewAllPriceUpdate =
                    Intent(activity, ReportPriceUpdateActivity::class.java)
                startActivity(newIntentViewAllPriceUpdate)
            }
            else -> {}
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }//calculate supplier with highest transaction//calculate supplier with highest transaction

    //get last to last week details
    //call method to get the data for the past 2 weeks
    // to display week graph and this week total spend
    val dataMonth:
    //get last week details
            Unit
        get() {
            threeDotLoaderBlue!!.visibility = View.VISIBLE
            val monthYearLastMonth = DateHelper.returnMonthYearString(1)
            txtLastMonth!!.text = monthYearLastMonth
            val monthYearLastToLastMonth = DateHelper.returnMonthYearString(2)
            txtLastToLastMonth!!.text = monthYearLastToLastMonth
            val currentMonthStartEndDate =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.CURRENT_MONTH)
            val lastMonthStartEndDate =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_MONTH)
            val lastToLastMonthStartEndDate =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_MONTH)
            val twoMonthsAgoStartEndDate =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.TWO_MONTHS_AGO)
            //call method to get the data for the past 2 weeks
            // to display week graph and this week total spend
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setStartDate(lastMonthStartEndDate.startDateMillis)
            apiParamsHelper.setEndDate(currentMonthStartEndDate.endDateMillis)
            apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.DAILY)
            ReportApi.retrieveTotalSpendings(
                activity,
                apiParamsHelper,
                object : TotalSpendingDetailByRangeResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingDetailsByRange?) {
                        if (isFragmentAttached) {
                            threeDotLoaderBlue!!.visibility = View.GONE
                            if (reportResponseData != null) {
                                val thisMonthTotal =
                                    ReportDataController.getTotalSpendingByWeekOrMonth(
                                        reportResponseData,
                                        currentMonthStartEndDate
                                    )
                                val thisWeekPriceDetails = PriceDetails(thisMonthTotal)
                                txtThisMonthSpendingAmount!!.text =
                                    thisWeekPriceDetails.displayValue
                                setChart(reportResponseData.data!!)
                            } else {
                                val thisWeekPriceDetails = PriceDetails()
                                txtThisMonthSpendingAmount!!.text =
                                    thisWeekPriceDetails.displayValue
                            }
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        threeDotLoaderBlue!!.visibility = View.GONE
                    }
                })
            //get last week details
            val apiParamsHelperLastWeek = ApiParamsHelper()
            apiParamsHelperLastWeek.setGroupBy(ApiParamsHelper.GroupBy.SUPPLIER)
            apiParamsHelperLastWeek.setStartDate(lastMonthStartEndDate.startDateMillis)
            apiParamsHelperLastWeek.setEndDate(lastMonthStartEndDate.endDateMillis)
            apiParamsHelperLastWeek.setRefStartDate(lastToLastMonthStartEndDate.startDateMillis)
            apiParamsHelperLastWeek.setRefEndDate(lastToLastMonthStartEndDate.endDateMillis)
            ReportApi.retrieveSpendings(
                activity,
                apiParamsHelperLastWeek,
                object : TotalSpendingSupplierResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingBySupplier?) {
                        if (isFragmentAttached) {
                            if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                                //calculate supplier with highest transaction
                                val supplier = ReportDataController.getHighestTransactionSupplier(
                                    reportResponseData.data!!
                                )
                                if (supplier != null) {
                                    setHighestTransactionLastMonthVisibility(View.VISIBLE)
                                    if (!StringHelper.isStringNullOrEmpty(supplier.supplierName)) {
                                        txtSupplierNameHighestTransactionLastMonth!!.text =
                                            supplier.supplierName
                                    }
                                    setSupplierLogo(
                                        imgHighestTransactionSupplierLastMonth,
                                        supplier,
                                        lytSupplierThumbNailTransactionSupplierLastMonth,
                                        txtSupplierThumbNailTransactionSupplierLastMonth
                                    )
                                } else {
                                    setHighestTransactionLastMonthVisibility(View.GONE)
                                }
                                val totalSpendingCurrentAndReferenceData =
                                    ReportDataController.getTotalSpendingAndReferenceDateSpending(
                                        reportResponseData.data!!
                                    )
                                txtLastMonthSpendingAmount!!.text =
                                    totalSpendingCurrentAndReferenceData.totalSpendingCurrent?.displayValue
                                val percentage = totalSpendingCurrentAndReferenceData.totalSpendingReference?.let {
                                    totalSpendingCurrentAndReferenceData.totalSpendingCurrent?.let { it1 ->
                                        ReportDataController.getTotalSpendingVariations(
                                            it,
                                            it1
                                        )
                                    }
                                }
                                if (percentage != null) {
                                    setPriceVariationPercentage(
                                        percentage,
                                        txtPriceVariationPercentageLastMonth,
                                        imgArrowPriceVariationLastMonth
                                    )
                                }
                            } else {
                                setHighestTransactionLastMonthVisibility(View.GONE)
                                val priceDetailsZeroPrice = PriceDetails()
                                txtLastMonthSpendingAmount!!.text =
                                    priceDetailsZeroPrice.displayValue
                                setPriceVariationPercentage(
                                    0.0,
                                    txtPriceVariationPercentageLastMonth,
                                    imgArrowPriceVariationLastMonth
                                )
                            }
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        setHighestTransactionLastMonthVisibility(View.GONE)
                        val priceDetailsZeroPrice = PriceDetails()
                        txtLastMonthSpendingAmount!!.text = priceDetailsZeroPrice.displayValue
                        setPriceVariationPercentage(
                            0.0,
                            txtPriceVariationPercentageLastMonth,
                            imgArrowPriceVariationLastMonth
                        )
                    }
                })

            //get last to last week details
            val apiParamsHelperLastToLastWeek = ApiParamsHelper()
            apiParamsHelperLastToLastWeek.setGroupBy(ApiParamsHelper.GroupBy.SUPPLIER)
            apiParamsHelperLastToLastWeek.setStartDate(lastToLastMonthStartEndDate.startDateMillis)
            apiParamsHelperLastToLastWeek.setEndDate(lastToLastMonthStartEndDate.endDateMillis)
            apiParamsHelperLastToLastWeek.setRefStartDate(twoMonthsAgoStartEndDate.startDateMillis)
            apiParamsHelperLastToLastWeek.setRefEndDate(twoMonthsAgoStartEndDate.endDateMillis)
            ReportApi.retrieveSpendings(
                activity,
                apiParamsHelperLastToLastWeek,
                object : TotalSpendingSupplierResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingBySupplier?) {
                        if (isFragmentAttached) {
                            if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                                //calculate supplier with highest transaction
                                val supplier = ReportDataController.getHighestTransactionSupplier(
                                    reportResponseData.data!!
                                )
                                if (supplier != null) {
                                    setHighestTransactionLastToLastMonthVisibility(View.VISIBLE)
                                    if (!StringHelper.isStringNullOrEmpty(supplier.supplierName)) {
                                        txtSupplierNameHighestTransactionLastToLastMonth!!.text =
                                            supplier.supplierName
                                    }
                                    setSupplierLogo(
                                        imgHighestTransactionSupplierLastToLastMonth,
                                        supplier,
                                        lytSupplierThumbNailTransactionSupplierLastToLastMonth,
                                        txtSupplierThumbNailTransactionSupplierLastToLastMonth
                                    )
                                } else {
                                    setHighestTransactionLastToLastMonthVisibility(View.GONE)
                                }
                                val totalSpendingCurrentAndReferenceData =
                                    ReportDataController.getTotalSpendingAndReferenceDateSpending(
                                        reportResponseData.data!!
                                    )
                                txtLastToLastMonthSpendinAmount!!.text =
                                    totalSpendingCurrentAndReferenceData.totalSpendingCurrent?.displayValue
                                val percentage = totalSpendingCurrentAndReferenceData.totalSpendingReference?.let {
                                    totalSpendingCurrentAndReferenceData.totalSpendingCurrent?.let { it1 ->
                                        ReportDataController.getTotalSpendingVariations(
                                            it,
                                            it1
                                        )
                                    }
                                }
                                if (percentage != null) {
                                    setPriceVariationPercentage(
                                        percentage,
                                        txtPriceVariationPercentageLastToLastMonth,
                                        imgArrowPriceVariationLastToLastMonth
                                    )
                                }
                            } else {
                                setHighestTransactionLastToLastMonthVisibility(View.GONE)
                                val priceDetailsZeroPrice = PriceDetails()
                                txtLastToLastMonthSpendinAmount!!.text =
                                    priceDetailsZeroPrice.displayValue
                                setPriceVariationPercentage(
                                    0.0,
                                    txtPriceVariationPercentageLastToLastMonth,
                                    imgArrowPriceVariationLastToLastMonth
                                )
                            }
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        setHighestTransactionLastToLastMonthVisibility(View.GONE)
                        val priceDetailsZeroPrice = PriceDetails()
                        txtLastToLastMonthSpendinAmount!!.text = priceDetailsZeroPrice.displayValue
                        setPriceVariationPercentage(
                            0.0,
                            txtPriceVariationPercentageLastToLastMonth,
                            imgArrowPriceVariationLastToLastMonth
                        )
                    }
                })
            val priceUpdateHistoryDates = DateHelper.pastDaysStartEndDate(priceHistoryDayAgo, true)
            val apiParamsHelperPrice = ApiParamsHelper()
            apiParamsHelperPrice.setStartDate(priceUpdateHistoryDates.startDateMillis)
            apiParamsHelperPrice.setEndDate(priceUpdateHistoryDates.endDateMillis)
            GetPriceUpdateData(requireContext(), object : PriceUpdateDataListener {
                override fun onSuccessResponse(priceUpdateData: List<PriceUpdateModelBaseResponse.PriceDetailModel?>?) {
                    if (isFragmentAttached && priceUpdateData!! != null && priceUpdateData.size > 0) {
                        lstPriceUpdates.visibility = View.VISIBLE
                        txtNoPriceUpdates!!.visibility = View.GONE
                        imgNoPriceUpdates!!.visibility = View.GONE
                        lstPriceUpdates.layoutManager = LinearLayoutManager(activity)
                        lstPriceUpdates.adapter =
                            PriceUpdateReportSummaryListAdapter(
                                activity!!,
                                priceUpdateData as List<PriceUpdateModelBaseResponse.PriceDetailModel>,
                                object : OnTotalSpendingItemClick {
                                    override fun onItemClick(item: String?, SKUName: String?) {
                                        val newIntent = Intent(
                                            activity,
                                            ReportsSpendingBySKUActivity::class.java
                                        )
                                        val priceUpdateHistoryDates =
                                            pastDaysStartEndDate(priceHistoryDayAgo, true)
                                        val dateStringsStartDate =
                                            getDateInDateMonthYearFormat(priceUpdateHistoryDates.startDateMillis)
                                        val dateStringsEndDate =
                                            getDateInDateMonthYearFormat(priceUpdateHistoryDates.endDateMillis)
                                        newIntent.putExtra(
                                            ReportApi.START_DATE,
                                            dateStringsStartDate
                                        )
                                        newIntent.putExtra(ReportApi.END_DATE, dateStringsEndDate)
                                        newIntent.putExtra(
                                            ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR,
                                            item
                                        )
                                        newIntent.putExtra(
                                            ReportApi.SPENDING_DATA_PERIOD,
                                            ReportTimePeriod.CUSTOM.timePeriodValue
                                        )
                                        newIntent.putExtra(ReportApi.SKU_NAME, SKUName)
                                        newIntent.putExtra(
                                            ReportApi.CALLED_FROM_FRAGMENT,
                                            ReportApi.DASHBOARD_WEEK_FRAGMENT
                                        )
                                        startActivity(newIntent)
                                    }
                                })
                    } else {
                        lstPriceUpdates.visibility = View.GONE
                        txtNoPriceUpdates!!.visibility = View.VISIBLE
                        imgNoPriceUpdates!!.visibility = View.VISIBLE
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {}
            }).getOutletPriceUpdateData(
                apiParamsHelperPrice,
                SharedPref.defaultOutlet?.outletId!!
            )
        }

    private fun setSupplierLogo(
        imgSupplier: ImageView?,
        supplier: Supplier,
        lytSupplierThumbNail: RelativeLayout?,
        txtSupplierThumbNail: TextView?,
    ) {
        if (StringHelper.isStringNullOrEmpty(supplier.logoURL)) {
            imgSupplier!!.visibility = View.INVISIBLE
            lytSupplierThumbNail!!.visibility = View.VISIBLE
            lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    supplier.supplierName!!,
                    requireContext()
                )
            )
            txtSupplierThumbNail!!.text =
                CommonMethods.SupplierThumbNailShortCutText(supplier.supplierName!!)
            txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    supplier.supplierName!!,
                    requireContext()
                )
            )
        } else {
            imgSupplier!!.visibility = View.VISIBLE
            lytSupplierThumbNail!!.visibility = View.GONE
            Picasso.get().load(supplier.logoURL).placeholder(R.drawable.placeholder_all).resize(
                PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH, PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
            ).into(imgSupplier)
        }
    }

    fun setChart(totalSpendingData: List<ReportResponse.TotalSpendingDetailsByRangeData>) {
        val entries: MutableList<Entry> = ArrayList()
        val currentMonthStartEndDate =
            DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.CURRENT_MONTH)
        val chartData = ChartsHelper.getAllDatesForChartInPeriod(
            currentMonthStartEndDate.startDateMillis,
            currentMonthStartEndDate.endDateMillis
        )
        val mapChart: MutableMap<String, ChartModel> = HashMap()
        for (item in chartData) {
            mapChart[DateHelper.getDateInDateMonthYearFormat(item.invoiceDate)] = item
        }
        for (data in totalSpendingData) {
            val dateStr = DateHelper.getDateInDateMonthYearFormat(data.dateTime)
            if (mapChart.containsKey(dateStr)) {
                mapChart[dateStr]!!.totalPrice?.addAmount(data.totalSpendingAmount!!)
            }
        }
        val chartDataList: MutableList<ChartModel> = ArrayList()
        for ((_, value) in mapChart) {
            chartDataList.add(value)
        }
        Collections.sort(chartDataList) { rhs, lhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            if (lhs.invoiceDate!! > rhs.invoiceDate!!) -1 else if (lhs.invoiceDate!! < rhs.invoiceDate!!) 1 else 0
        }
        val entriesLastmonth: MutableList<Entry> = ArrayList()
        val lastWeekStartEndDate = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_MONTH)
        val chartDataLastweek = ChartsHelper.getAllDatesForChartInPeriod(
            lastWeekStartEndDate.startDateMillis,
            lastWeekStartEndDate.endDateMillis
        )
        val mapChartLastMonth: MutableMap<String, ChartModel> = HashMap()
        for (item in chartDataLastweek) {
            mapChartLastMonth[DateHelper.getDateInDateMonthYearFormat(item.invoiceDate)] = item
        }
        for (data in totalSpendingData) {
            val dateStr = DateHelper.getDateInDateMonthYearFormat(data.dateTime)
            if (mapChartLastMonth.containsKey(dateStr)) {
                mapChartLastMonth[dateStr]!!.totalPrice?.addAmount(data.totalSpendingAmount!!)
            }
        }
        val chartDataListLastweek: MutableList<ChartModel> = ArrayList()
        for ((_, value) in mapChartLastMonth) {
            chartDataListLastweek.add(value)
        }
        Collections.sort(chartDataListLastweek) { rhs, lhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            if (lhs.invoiceDate!! > rhs.invoiceDate!!) -1 else if (lhs.invoiceDate!! < rhs.invoiceDate!!) 1 else 0
        }
        val maxDays =
            if (chartDataList.size > chartDataListLastweek.size) chartDataList.size else chartDataListLastweek.size
        val dayValues: MutableList<String> = ArrayList()
        for (i in 0 until maxDays) {
            if (chartDataList.size > i) entries.add(
                Entry(
                    i.toFloat(),
                    chartDataList[i].totalPrice?.amount?.toFloat()!!
                )
            )
            if (chartDataListLastweek.size > i) entriesLastmonth.add(
                Entry(
                    i.toFloat(),
                    chartDataListLastweek[i].totalPrice?.amount?.toFloat()!!
                )
            )
            if (chartDataList.size > i) dayValues.add(DateHelper.getDayOfMonthFromDate(chartDataList[i].invoiceDate as Long)) else dayValues.add(
                DateHelper.getDayOfMonthFromDate(
                    chartDataListLastweek[i].invoiceDate as Long
                )
            )
        }
        val dataSet =
            LineDataSet(entries, getString(R.string.txt_this_month)) // add entries to dataset
        dataSet.setDrawValues(false)
        dataSet.color = requireActivity().resources.getColor(R.color.orange)
        dataSet.setDrawCircles(false)
        dataSet.setDrawCircleHole(false)
        val dataSetLastmonth = LineDataSet(
            entriesLastmonth,
            getString(R.string.txt_last_month)
        ) // add entries to dataset
        dataSetLastmonth.setDrawValues(false)
        dataSetLastmonth.color = requireActivity().resources.getColor(R.color.key_line_grey)
        dataSetLastmonth.setDrawCircles(false)
        dataSetLastmonth.setDrawCircleHole(false)
        val lineData = LineData(dataSetLastmonth, dataSet)
        val xAxis = chart!!.xAxis
        xAxis.valueFormatter = ChartXAxisValueFormatter(dayValues.toTypedArray())
        val mv = CustomMarkerViewMonthCharts(
            activity,
            R.layout.chart_marker_layout,
            dayValues,
            entriesLastmonth,
            entries
        )
        mv.chartView = chart
        chart!!.marker = mv
        chart!!.data = lineData
        ChartsHelper.lineChartUiSetup(chart!!)
        chart!!.invalidate() // refresh
        chart!!.animateXY(1000, 1000, Easing.EasingOption.Linear, Easing.EasingOption.Linear)
    }

    private fun setPriceVariationPercentage(
        percVariationLastWeek: Double,
        txtPriceVariationText: TextView?,
        imgPriceVariationIcon: ImageView?,
    ) {
        if (percVariationLastWeek < 0 && percVariationLastWeek != -100.0) {
            imgPriceVariationIcon!!.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.icon_angle_arrow_down))
            txtPriceVariationText!!.setTextColor(requireActivity().resources.getColor(R.color.text_blue))
        } else if (percVariationLastWeek > 0) {
            imgPriceVariationIcon!!.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.icon_angle_arrow_up))
            txtPriceVariationText!!.setTextColor(requireActivity().resources.getColor(R.color.pinky_red))
        } else if (percVariationLastWeek == -100.0) {
            imgPriceVariationIcon!!.visibility = View.GONE
            txtPriceVariationText!!.visibility = View.GONE
        } else {
            imgPriceVariationIcon!!.visibility = View.GONE
            txtPriceVariationText!!.setTextColor(requireActivity().resources.getColor(R.color.dark_grey))
        }
        if (percVariationLastWeek == 0.0) {
            txtPriceVariationText.text = "-"
        } else {
            txtPriceVariationText.text =
                getTwoDecimalQuantity(Math.abs(percVariationLastWeek)) + "%"
        }
    }

    private fun setHighestTransactionLastMonthVisibility(visibility: Int) {
        val txtHighestTransactionHeading =
            dashboardMonthView!!.findViewById<TextView>(R.id.dashboard_report_week_last_week_highest_transactions)
        txtHighestTransactionHeading.visibility = visibility
        val txtHighestTransactionSupplierName =
            dashboardMonthView!!.findViewById<TextView>(R.id.dashboard_report_week_last_week_highest_transactions)
        txtHighestTransactionSupplierName.visibility = visibility
        val imgHighestTransactionSupplierImage =
            dashboardMonthView!!.findViewById<ImageView>(R.id.dashboard_report_week_last_week_supplier_image)
        imgHighestTransactionSupplierImage.visibility = visibility
        val txtDivider =
            dashboardMonthView!!.findViewById<TextView>(R.id.dashboard_report_week_last_week_divider2)
        txtDivider.visibility = visibility
    }

    private fun setHighestTransactionLastToLastMonthVisibility(visibility: Int) {
        val txtHighestTransactionHeading =
            dashboardMonthView!!.findViewById<TextView>(R.id.dashboard_report_week_2week_ago_highest_transactions)
        txtHighestTransactionHeading.visibility = visibility
        val txtHighestTransactionSupplierName =
            dashboardMonthView!!.findViewById<TextView>(R.id.dashboard_report_week_2week_ago_highest_transactions_supplier)
        txtHighestTransactionSupplierName.visibility = visibility
        val imgHighestTransactionSupplierImage =
            dashboardMonthView!!.findViewById<ImageView>(R.id.dashboard_report_week_2week_ago_supplier_image)
        imgHighestTransactionSupplierImage.visibility = visibility
        val txtDivider =
            dashboardMonthView!!.findViewById<TextView>(R.id.dashboard_report_week_2week_ago_divider3)
        txtDivider.visibility = visibility
    }

    private fun setFonts() {
        setTypefaceView(week_txt, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(month_txt, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            thisMonthSpendingButton,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtThisMonthSpendingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtStartEndDateCurrentMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtViewSpendingDetails,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtLastMonthHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtLastMonthSpendingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtPriceVariationPercentageLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtHighestTransactionHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtSupplierNameHighestTransactionLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txt2MonthAgoHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtLastToLastMonthSpendinAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtPriceVariationPercentageLastToLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtRecentPriceChangesHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtHighestTransactionHeading2MonthAgo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtSupplierNameHighestTransactionLastToLastMonth,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtDatOlderThan90DaysNotShown,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtViewAllPriceChanges,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    fun ShowDialog(context: Context?) {
        val dl = Dialog(requireContext())
        dl.setContentView(R.layout.custom_dialog_invoiced_week_month)
        dl.window!!.setGravity(Gravity.CENTER)
        dl.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val txt_view_report_by = dl.findViewById<TextView>(R.id.txt_view_report_by)
        val txt_week_dialog = dl.findViewById<TextView>(R.id.txt_week_dialog)
        val txt_month_dialog = dl.findViewById<TextView>(R.id.txt_month_dialog)
        val btn_delete_invoice = dl.findViewById<Button>(R.id.btn_delete_invoice)
        val img_select_week = dl.findViewById<ImageView>(R.id.img_select_week)
        val img_select_month = dl.findViewById<ImageView>(R.id.img_select_month)
        setTypefaceView(txt_view_report_by, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btn_delete_invoice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txt_week_dialog, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txt_month_dialog, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        txt_week_dialog.setOnClickListener {
            val fm = parentFragmentManager
            val fragment: Fragment = ReportDashboardWeekFragment()
            val contentView = requireActivity().findViewById<View>(R.id.frame_container) as FrameLayout
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            dl.dismiss()
        }
        txt_month_dialog.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                dataMonth
                dl.dismiss()
            }
        })
        /* if (txt_month_dialog.isClickable()){
             img_select_month.setVisibility(View.VISIBLE);
             img_select_week.setVisibility(View.GONE);
         }else if (txt_week_dialog.isClickable()){
             img_select_month.setVisibility(View.GONE);
             img_select_week.setVisibility(View.VISIBLE);

         }
*/dl.show()
    }

    companion object {
        private val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH = CommonMethods.dpToPx(30)
        private val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT = CommonMethods.dpToPx(30)

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ReportDashboardWeekFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(): ReportDashboardMonthFragment {
            return ReportDashboardMonthFragment()
        }
    }
}