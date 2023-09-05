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
 * [ReportDashboardWeekFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ReportDashboardWeekFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportDashboardWeekFragment : Fragment(), View.OnClickListener {
    private var mListener: OnFragmentInteractionListener? = null
    private var crdThisWeekSpending: CardView? = null
    private lateinit var crdLastWeekSpending: CardView
    private lateinit var crd2WeeksAgoSpending: CardView
    private var scrollSummaryWeek: NestedScrollView? = null
    private lateinit var swipeRefreshReportWeek: SwipeRefreshLayout
    private var threeDotLoaderBlue: CustomLoadingViewBlue? = null
    private var txtThisWeekSpendingAmount: TextView? = null
    private var txtLastWeekSpendingAmount: TextView? = null
    private var txtLastToLastWeekSpendinAmount: TextView? = null
    private lateinit var week_txt: TextView
    private lateinit var month_txt: TextView
    private var imgHighestTransactionSupplierLastWeek: ImageView? = null
    var lytSupplierThumbNailTransactionSupplierLastWeek: RelativeLayout? = null
    var txtSupplierThumbNailTransactionSupplierLastWeek: TextView? = null
    private var imgHighestTransactionSupplierLastToLastWeek: ImageView? = null
    var lytSupplierThumbNailTransactionSupplierLastToLastWeek: RelativeLayout? = null
    var txtSupplierThumbNailTransactionSupplierLastToLastWeek: TextView? = null
    private var txtSupplierNameHighestTransactionLastWeek: TextView? = null
    private var txtSupplierNameHighestTransactionLastToLastWeek: TextView? = null
    private lateinit var lytExpectedSpending: LinearLayout
    private lateinit var lstPriceUpdates: RecyclerView
    private var txtNoPriceUpdates: TextView? = null
    private var imgNoPriceUpdates: ImageView? = null
    private var imgArrowPriceVariationLastWeek: ImageView? = null
    private var txtPriceVariationPercentageLastWeek: TextView? = null
    private var imgArrowPriceVariationLastToLastWeek: ImageView? = null
    private var txtPriceVariationPercentageLastToLastWeek: TextView? = null
    private var txtStartEndDateCurrentWeek: TextView? = null
    private var dashboardWeekView: View? = null
    private val priceHistoryDayAgo = 90
    private lateinit var txtViewAllPriceUpdates: TextView
    private var isFragmentAttached = false
    private lateinit var txtViewSpendingDetails: Button
    private var chart: LineChart? = null
    private var thisWeekSpendingButton: Button? = null
    private var txtLastWeekHeading: TextView? = null
    private var txt2WeekAgoHeading: TextView? = null
    private var txtHighestTransactionHeading: TextView? = null
    private var txtHighestTransactionHeading2WeeksAgo: TextView? = null
    private var txtRecentPriceChangesHeading: TextView? = null
    private var txtViewAllPriceChangeHistory: TextView? = null
    private val context: Context? = null
    private lateinit var dayValuesArray: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_report_dashboard_week, container, false)
        dashboardWeekView = v
        crdThisWeekSpending = v.findViewById(R.id.report_dashboard_week_card_total_spending_week)
        crdLastWeekSpending = v.findViewById(R.id.dashboard_report_week_card_last_week)
        crdLastWeekSpending.setOnClickListener(this)
        crd2WeeksAgoSpending =
            v.findViewById(R.id.report_dashboard_week_card_total_spending_2weeks_ago)
        crd2WeeksAgoSpending.setOnClickListener(this)
        scrollSummaryWeek = v.findViewById(R.id.nested_scroll_view_week_fragment)
        threeDotLoaderBlue = v.findViewById(R.id.spin_kit_loader_reports_blue)
        swipeRefreshReportWeek = v.findViewById(R.id.swipe_refresh_report_week_fragment)
        txtThisWeekSpendingAmount = v.findViewById(R.id.report_dashboard_week_txt_total_spending)
        txtLastWeekSpendingAmount = v.findViewById(R.id.dashboard_report_week_last_week_value)
        txtLastToLastWeekSpendinAmount =
            v.findViewById(R.id.dashboard_report_week_last_2week_ago_value)
        imgHighestTransactionSupplierLastWeek =
            v.findViewById(R.id.dashboard_report_week_last_week_supplier_image)
        imgHighestTransactionSupplierLastToLastWeek =
            v.findViewById(R.id.dashboard_report_week_2week_ago_supplier_image)
        lytSupplierThumbNailTransactionSupplierLastToLastWeek =
            v.findViewById(R.id.lyt_supplier_thumbnail2)
        txtSupplierThumbNailTransactionSupplierLastToLastWeek =
            v.findViewById(R.id.txt_supplier_thumbnail2)
        setTypefaceView(
            txtSupplierThumbNailTransactionSupplierLastToLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        lytSupplierThumbNailTransactionSupplierLastWeek =
            v.findViewById(R.id.lyt_supplier_thumbnail)
        txtSupplierThumbNailTransactionSupplierLastWeek =
            v.findViewById(R.id.txt_supplier_thumbnail)
        setTypefaceView(
            txtSupplierThumbNailTransactionSupplierLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtSupplierNameHighestTransactionLastWeek =
            v.findViewById(R.id.dashboard_report_week_last_week_highest_transactions_supplier)
        txtSupplierNameHighestTransactionLastToLastWeek =
            v.findViewById(R.id.dashboard_report_week_2week_ago_highest_transactions_supplier)
        lytExpectedSpending = v.findViewById(R.id.report_dashboard_week_lyt_expected_spend)
        week_txt = v.findViewById(R.id.week_txt)
        month_txt = v.findViewById(R.id.month_txt)
        lytExpectedSpending.setVisibility(View.GONE)
        lstPriceUpdates = v.findViewById(R.id.dashboard_report_week_lst_price_changes)
        lstPriceUpdates.setNestedScrollingEnabled(false)
        txtNoPriceUpdates = v.findViewById(R.id.txt_no_price_details)
        imgNoPriceUpdates = v.findViewById(R.id.img_no_price_details)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshReportWeek)
        week_txt.setOnClickListener(View.OnClickListener { ShowDialog(getContext()) })
        swipeRefreshReportWeek.setOnRefreshListener(OnRefreshListener { // getDataWeek();
            swipeRefreshReportWeek.setRefreshing(false)
        })
        imgArrowPriceVariationLastWeek =
            v.findViewById(R.id.dashboard_report_week_last_week_value_updown_icon)
        txtPriceVariationPercentageLastWeek =
            v.findViewById(R.id.dashboard_report_week_last_week_value_updown_perc_value)
        imgArrowPriceVariationLastToLastWeek =
            v.findViewById(R.id.dashboard_report_week_2week_value_updown_icon)
        txtPriceVariationPercentageLastToLastWeek =
            v.findViewById(R.id.dashboard_report_week_2week_value_updown_perc_value)
        txtStartEndDateCurrentWeek = v.findViewById(R.id.report_dashboard_week_txt_date_range)
        txtViewAllPriceUpdates =
            v.findViewById(R.id.report_dashboard_week_txt_view_all_price_changes)
        txtViewAllPriceUpdates.setOnClickListener(this)
        txtViewSpendingDetails = v.findViewById(R.id.btn_view_spending_details)
        thisWeekSpendingButton = v.findViewById(R.id.report_dashboard_week_txt_this_week_spending)
        txtLastWeekHeading = v.findViewById(R.id.dashboard_report_week_last_week_text_heading)
        txt2WeekAgoHeading = v.findViewById(R.id.dashboard_report_week_2week_ago_text_heading)
        txtHighestTransactionHeading =
            v.findViewById(R.id.dashboard_report_week_last_week_highest_transactions)
        txtHighestTransactionHeading2WeeksAgo =
            v.findViewById(R.id.dashboard_report_week_2week_ago_highest_transactions)
        txtRecentPriceChangesHeading =
            v.findViewById(R.id.dashboard_report_week_recent_price_changes_heading)
        txtViewAllPriceChangeHistory =
            v.findViewById(R.id.report_dashboard_week_txt_view_all_price_changes)
        txtViewSpendingDetails.setOnClickListener(this)
        chart = v.findViewById(R.id.chart)
        week_txt.setVisibility(View.VISIBLE)
        month_txt.setVisibility(View.GONE)
        setFonts()
        dataWeek
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //call the API to get week data
        threeDotLoaderBlue!!.visibility = View.VISIBLE
        dataWeek
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
                    AnalyticsHelper.TAP_REPORTS_WEEK_VIEW_SPENDING_DETAILS
                )
                val newIntentThisWeek = Intent(activity, ReportTotalSpendingActivity::class.java)
                newIntentThisWeek.putExtra(
                    ReportApi.SPENDING_DATA_PERIOD,
                    ReportTimePeriod.THISWEEK.timePeriodValue
                )
                startActivity(newIntentThisWeek)
            }
            R.id.dashboard_report_week_card_last_week -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_REPORTS_PREVIOUS_WEEK)
                val newIntentLastWeek = Intent(activity, ReportTotalSpendingActivity::class.java)
                newIntentLastWeek.putExtra(
                    ReportApi.SPENDING_DATA_PERIOD,
                    ReportTimePeriod.LASTWEEK.timePeriodValue
                )
                startActivity(newIntentLastWeek)
            }
            R.id.report_dashboard_week_card_total_spending_2weeks_ago -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_REPORTS_PREVIOUS_TWO_WEEK)
                val newIntent2WeekAgo = Intent(activity, ReportTotalSpendingActivity::class.java)
                newIntent2WeekAgo.putExtra(
                    ReportApi.SPENDING_DATA_PERIOD,
                    ReportTimePeriod.LASTTOLASTWEEK.timePeriodValue
                )
                startActivity(newIntent2WeekAgo)
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
    }//calculate supplier with highest transaction

    //DateHelper.StartDateEndDate currentWeekStartEndDate =  DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK);
    //new GetOutletSpendingHistoryData(getActivity(),this).getOutletSpendingData(currentWeekStartEndDate.getStartDateMillis(),currentWeekStartEndDate.getEndDateMillis(),true, SharedPref.getDefaultOutlet().getOutletId());
//calculate supplier with highest transaction

    //get last to last week details
    //call method to get the data for the past 2 weeks
    // to display week graph and this week total spend
    val dataWeek:
    //get last week details
            Unit
        get() {
            threeDotLoaderBlue!!.visibility = View.VISIBLE
            val currentWeekStartEndDate =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
            val lastWeekStartEndDate =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
            val lastToLastWeekStartEndDate =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_WEEK)
            val twoWeeksAgoStartEndDate =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.TWO_WEEKS_AGO)
            //call method to get the data for the past 2 weeks
            // to display week graph and this week total spend
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setStartDate(lastWeekStartEndDate.startDateMillis)
            apiParamsHelper.setEndDate(currentWeekStartEndDate.endDateMillis)
            apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.DAILY)
            ReportApi.retrieveTotalSpendings(
                activity,
                apiParamsHelper,
                object : TotalSpendingDetailByRangeResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingDetailsByRange?) {
                        if (isFragmentAttached) {
                            threeDotLoaderBlue!!.visibility = View.GONE
                            if (reportResponseData != null) {
                                val thisWeekTotal =
                                    ReportDataController.getTotalSpendingByWeekOrMonth(
                                        reportResponseData,
                                        currentWeekStartEndDate
                                    )
                                val thisWeekPriceDetails = PriceDetails(thisWeekTotal)
                                txtThisWeekSpendingAmount!!.text = thisWeekPriceDetails.displayValue
                                setChart(reportResponseData.data!!)
                            } else {
                                val thisWeekPriceDetails = PriceDetails()
                                txtThisWeekSpendingAmount!!.text = thisWeekPriceDetails.displayValue
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
            apiParamsHelperLastWeek.setStartDate(lastWeekStartEndDate.startDateMillis)
            apiParamsHelperLastWeek.setEndDate(lastWeekStartEndDate.endDateMillis)
            apiParamsHelperLastWeek.setRefStartDate(lastToLastWeekStartEndDate.startDateMillis)
            apiParamsHelperLastWeek.setRefEndDate(lastToLastWeekStartEndDate.endDateMillis)
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
                                    setHighestTransactionLastWeekVisibility(View.VISIBLE)
                                    if (!StringHelper.isStringNullOrEmpty(supplier.supplierName)) {
                                        txtSupplierNameHighestTransactionLastWeek!!.text =
                                            supplier.supplierName
                                    }
                                    setSupplierLogo(
                                        imgHighestTransactionSupplierLastWeek,
                                        supplier,
                                        lytSupplierThumbNailTransactionSupplierLastWeek,
                                        txtSupplierThumbNailTransactionSupplierLastWeek
                                    )
                                } else {
                                    setHighestTransactionLastWeekVisibility(View.GONE)
                                }
                                val totalSpendingCurrentAndReferenceData =
                                    ReportDataController.getTotalSpendingAndReferenceDateSpending(
                                        reportResponseData.data!!
                                    )
                                txtLastWeekSpendingAmount!!.text =
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
                                        txtPriceVariationPercentageLastWeek,
                                        imgArrowPriceVariationLastWeek
                                    )
                                }
                            } else {
                                setHighestTransactionLastWeekVisibility(View.GONE)
                                val totalSpendingLastWeekPrice = PriceDetails()
                                txtLastWeekSpendingAmount!!.text =
                                    totalSpendingLastWeekPrice.displayValue
                                setPriceVariationPercentage(
                                    0.0,
                                    txtPriceVariationPercentageLastWeek,
                                    imgArrowPriceVariationLastWeek
                                )
                            }
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        setHighestTransactionLastWeekVisibility(View.GONE)
                        val totalSpendingLastWeekPrice = PriceDetails()
                        txtLastWeekSpendingAmount!!.text = totalSpendingLastWeekPrice.displayValue
                        setPriceVariationPercentage(
                            0.0,
                            txtPriceVariationPercentageLastWeek,
                            imgArrowPriceVariationLastWeek
                        )
                    }
                })

            //get last to last week details
            val apiParamsHelperLastToLastWeek = ApiParamsHelper()
            apiParamsHelperLastToLastWeek.setGroupBy(ApiParamsHelper.GroupBy.SUPPLIER)
            apiParamsHelperLastToLastWeek.setStartDate(lastToLastWeekStartEndDate.startDateMillis)
            apiParamsHelperLastToLastWeek.setEndDate(lastToLastWeekStartEndDate.endDateMillis)
            apiParamsHelperLastToLastWeek.setRefStartDate(twoWeeksAgoStartEndDate.startDateMillis)
            apiParamsHelperLastToLastWeek.setRefEndDate(twoWeeksAgoStartEndDate.endDateMillis)
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
                                    setHighestTransactionLastToLastWeekVisibility(View.VISIBLE)
                                    if (!StringHelper.isStringNullOrEmpty(supplier.supplierName)) {
                                        txtSupplierNameHighestTransactionLastToLastWeek!!.text =
                                            supplier.supplierName
                                    }
                                    setSupplierLogo(
                                        imgHighestTransactionSupplierLastToLastWeek,
                                        supplier,
                                        lytSupplierThumbNailTransactionSupplierLastToLastWeek,
                                        txtSupplierThumbNailTransactionSupplierLastToLastWeek
                                    )
                                } else {
                                    setHighestTransactionLastToLastWeekVisibility(View.INVISIBLE)
                                }
                                val totalSpendingCurrentAndReferenceData =
                                    ReportDataController.getTotalSpendingAndReferenceDateSpending(
                                        reportResponseData.data!!
                                    )
                                txtLastToLastWeekSpendinAmount!!.text =
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
                                        txtPriceVariationPercentageLastToLastWeek,
                                        imgArrowPriceVariationLastToLastWeek
                                    )
                                }
                            } else {
                                setHighestTransactionLastToLastWeekVisibility(View.GONE)
                                val totalSpendingLastToLastWeekPrice = PriceDetails()
                                txtLastToLastWeekSpendinAmount!!.text =
                                    totalSpendingLastToLastWeekPrice.displayValue
                                setPriceVariationPercentage(
                                    0.0,
                                    txtPriceVariationPercentageLastToLastWeek,
                                    imgArrowPriceVariationLastToLastWeek
                                )
                            }
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        setHighestTransactionLastToLastWeekVisibility(View.GONE)
                        val totalSpendingLastToLastWeekPrice = PriceDetails()
                        txtLastToLastWeekSpendinAmount!!.text =
                            totalSpendingLastToLastWeekPrice.displayValue
                        setPriceVariationPercentage(
                            0.0,
                            txtPriceVariationPercentageLastToLastWeek,
                            imgArrowPriceVariationLastToLastWeek
                        )
                    }
                })

            //DateHelper.StartDateEndDate currentWeekStartEndDate =  DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK);
            //new GetOutletSpendingHistoryData(getActivity(),this).getOutletSpendingData(currentWeekStartEndDate.getStartDateMillis(),currentWeekStartEndDate.getEndDateMillis(),true, SharedPref.getDefaultOutlet().getOutletId());

            //DateHelper.StartDateEndDate currentWeekStartEndDate =  DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK);
            //new GetOutletSpendingHistoryData(getActivity(),this).getOutletSpendingData(currentWeekStartEndDate.getStartDateMillis(),currentWeekStartEndDate.getEndDateMillis(),true, SharedPref.getDefaultOutlet().getOutletId());
            val priceUpdateHistoryDates = pastDaysStartEndDate(priceHistoryDayAgo, true)
            val apiParamsHelperPrice = ApiParamsHelper()
            apiParamsHelperPrice.setStartDate(priceUpdateHistoryDates.startDateMillis)
            apiParamsHelperPrice.setEndDate(priceUpdateHistoryDates.endDateMillis)
            GetPriceUpdateData(requireContext(), object : PriceUpdateDataListener {
                override fun onSuccessResponse(priceUpdateData: List<PriceUpdateModelBaseResponse.PriceDetailModel?>?) {
                    if (isFragmentAttached && priceUpdateData != null && priceUpdateData.size!! > 0) {
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
        val currentWeekStartEndDate =
            DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
        val chartData = ChartsHelper.getAllDatesForChartInPeriod(
            currentWeekStartEndDate.startDateMillis,
            currentWeekStartEndDate.endDateMillis
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
        val entriesLastWeek: MutableList<Entry> = ArrayList()
        val lastWeekStartEndDate = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
        val chartDataLastWeek = ChartsHelper.getAllDatesForChartInPeriod(
            lastWeekStartEndDate.startDateMillis,
            lastWeekStartEndDate.endDateMillis
        )
        val mapChartLastWeek: MutableMap<String, ChartModel> = HashMap()
        for (item in chartDataLastWeek) {
            mapChartLastWeek[DateHelper.getDateInDateMonthYearFormat(item.invoiceDate)] = item
        }
        for (data in totalSpendingData) {
            val dateStr = DateHelper.getDateInDateMonthYearFormat(data.dateTime)
            if (mapChartLastWeek.containsKey(dateStr)) {
                mapChartLastWeek[dateStr]!!.totalPrice?.addAmount(data.totalSpendingAmount!!)
            }
        }
        val chartDataListLastWeek: MutableList<ChartModel> = ArrayList()
        for ((_, value) in mapChartLastWeek) {
            chartDataListLastWeek.add(value)
        }
        Collections.sort(chartDataListLastWeek) { rhs, lhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            if (lhs.invoiceDate!! > rhs.invoiceDate!!) -1 else if (lhs.invoiceDate!! < rhs.invoiceDate!!) 1 else 0
        }
        val dayValues: MutableList<String> = ArrayList()
        val dateMonthValues: MutableList<String> = ArrayList()
        val dayValuesLastWeek: MutableList<String> = ArrayList()
        var i = 0
        var j = 0
        for (item in chartDataListLastWeek) {
            entriesLastWeek.add(Entry(i.toFloat(), item.totalPrice?.amount?.toFloat()!!))
            dayValuesLastWeek.add(DateHelper.getDateInDateMonthFormat(item.invoiceDate as Long))
            i++
        }
        for (item in chartDataList) {
            entries.add(Entry(j.toFloat(), item.totalPrice?.amount?.toFloat()!!))
            dayValues.add(DateHelper.getDayOfMonthFromDate(item.invoiceDate!!))
            dateMonthValues.add(DateHelper.getDateInDateMonthFormat(item.invoiceDate))
            j++
        }
        val dataSet =
            LineDataSet(entries, getString(R.string.txt_this_week)) // add entries to dataset
        dataSet.setDrawValues(false)
        dataSet.color = requireActivity().resources.getColor(R.color.orange)
        dataSet.setDrawCircles(false)
        dataSet.setDrawCircleHole(false)
        val dataSetLastweek = LineDataSet(
            entriesLastWeek,
            getString(R.string.txt_last_week)
        ) // add entries to dataset
        dataSetLastweek.setDrawValues(false)
        dataSetLastweek.color = requireActivity().resources.getColor(R.color.key_line_grey)
        dataSetLastweek.setDrawCircles(false)
        dataSetLastweek.setDrawCircleHole(false)
        val lineData = LineData(dataSetLastweek, dataSet)
        val xAxis = chart!!.xAxis
        xAxis.valueFormatter = ChartXAxisValueFormatter(dayValues.toTypedArray())
        val mv = CustomMarkerViewWeekCharts(
            activity,
            R.layout.chart_marker_layout,
            dayValuesLastWeek,
            dateMonthValues,
            entriesLastWeek,
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
        imgPriceVariationIcon!!.visibility = View.VISIBLE
        txtPriceVariationText!!.visibility = View.VISIBLE
        if (percVariationLastWeek < 0 && percVariationLastWeek != -100.0) {
            imgPriceVariationIcon.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.icon_angle_arrow_down))
            txtPriceVariationText.setTextColor(requireActivity().resources.getColor(R.color.text_blue))
        } else if (percVariationLastWeek > 0) {
            imgPriceVariationIcon.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.icon_angle_arrow_up))
            txtPriceVariationText.setTextColor(requireActivity().resources.getColor(R.color.pinky_red))
        } else if (percVariationLastWeek == -100.0) {
            imgPriceVariationIcon.visibility = View.GONE
            txtPriceVariationText.visibility = View.GONE
        } else {
            imgPriceVariationIcon.visibility = View.GONE
            txtPriceVariationText.setTextColor(requireActivity().resources.getColor(R.color.dark_grey))
        }
        if (percVariationLastWeek == 0.0) {
            txtPriceVariationText.text = "-"
        } else {
            txtPriceVariationText.text =
                getTwoDecimalQuantity(Math.abs(percVariationLastWeek)) + "%"
        }
    }

    private fun setHighestTransactionLastWeekVisibility(visibility: Int) {
        txtHighestTransactionHeading!!.visibility = visibility
        val txtHighestTransactionSupplierName =
            dashboardWeekView!!.findViewById<TextView>(R.id.dashboard_report_week_last_week_highest_transactions)
        txtHighestTransactionSupplierName.visibility = visibility
        val imgHighestTransactionSupplierImage =
            dashboardWeekView!!.findViewById<ImageView>(R.id.dashboard_report_week_last_week_supplier_image)
        imgHighestTransactionSupplierImage.visibility = visibility
        val txtDivider =
            dashboardWeekView!!.findViewById<TextView>(R.id.dashboard_report_week_last_week_divider2)
        txtDivider.visibility = visibility
    }

    private fun setHighestTransactionLastToLastWeekVisibility(visibility: Int) {
        val txtHighestTransactionHeading2WeeksAgo =
            dashboardWeekView!!.findViewById<TextView>(R.id.dashboard_report_week_2week_ago_highest_transactions)
        txtHighestTransactionHeading2WeeksAgo.visibility = visibility
        val txtHighestTransactionSupplierName =
            dashboardWeekView!!.findViewById<TextView>(R.id.dashboard_report_week_2week_ago_highest_transactions_supplier)
        txtHighestTransactionSupplierName.visibility = visibility
        val imgHighestTransactionSupplierImage =
            dashboardWeekView!!.findViewById<ImageView>(R.id.dashboard_report_week_2week_ago_supplier_image)
        imgHighestTransactionSupplierImage.visibility = visibility
        val txtDivider =
            dashboardWeekView!!.findViewById<TextView>(R.id.dashboard_report_week_2week_ago_divider3)
        txtDivider.visibility = visibility
    }

    /**
     * set the fonts for various text views
     */
    private fun setFonts() {
        setTypefaceView(
            thisWeekSpendingButton,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(week_txt, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(month_txt, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtThisWeekSpendingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtStartEndDateCurrentWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtViewSpendingDetails,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtLastWeekHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtLastWeekSpendingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtPriceVariationPercentageLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtHighestTransactionHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtSupplierNameHighestTransactionLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txt2WeekAgoHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtLastToLastWeekSpendinAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtPriceVariationPercentageLastToLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtHighestTransactionHeading2WeeksAgo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtSupplierNameHighestTransactionLastToLastWeek,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtRecentPriceChangesHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtNoPriceUpdates, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtViewAllPriceChangeHistory,
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
        txt_week_dialog.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                dataWeek
                dl.dismiss()
            }
        })
        txt_month_dialog.setOnClickListener {
            val fm = fragmentManager
            val fragment: Fragment = ReportDashboardMonthFragment()
            val contentView = requireActivity().findViewById<View>(R.id.frame_container) as FrameLayout
            assert(fm != null)
            fm!!.beginTransaction()
                .replace(contentView.id, fragment)
                .addToBackStack(null)
                .commit()
            dl.dismiss()
        }

        /* if (txt_week_dialog.isClickable()){
               img_select_week.setVisibility(View.VISIBLE);
               img_select_month.setVisibility(View.GONE);

           }else if (txt_month_dialog.isClickable()){
               img_select_week.setVisibility(View.GONE);
               img_select_month.setVisibility(View.VISIBLE);
           }*/dl.show()
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
        fun newInstance(): ReportDashboardWeekFragment {
            return ReportDashboardWeekFragment()
        }
    }
}