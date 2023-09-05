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
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.TotalSpendingListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.AnalyticsHelper.logAction
import zeemart.asia.buyers.helper.ReportApi.TotalSpendingSupplierResponseListener
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.DateRangeChangeObserver
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.interfaces.OutletSpendingHistoryDataListener
import zeemart.asia.buyers.interfaces.ProgressIndicatorListener
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.reports.SpendingModel
import zeemart.asia.buyers.models.reportsimport.ReportResponse
import zeemart.asia.buyers.models.reportsimportimport.ReportSpendingDataModel
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.reports.ReportsSpendingDetailsActivity

/**
 * A simple [Fragment] subclass.
 * Use the [TotalSpendingSupplierFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TotalSpendingSupplierFragment : Fragment(), DateRangeChangeObserver,
    OutletSpendingHistoryDataListener {
    private val spendingData = ArrayList<SpendingModel>()
    private var spendingTimePeriod: String? = null
    private var txtTotalSpendingAmount: TextView? = null
    private lateinit var lstTotalSpendingSupplier: RecyclerView
    private var txtAverageDailySpend: TextView? = null
    private var selectedStartDate = ""
    private var selectedEndDate = ""
    private lateinit var txtTotalSpendingHeading: TextView
    private var lytNoSpendingData: LinearLayout? = null
    private lateinit var txtSpendingDetailHeading: TextView
    private var isFragmentAttached = false
    private var chart: PieChart? = null
    private var txtTotalWithoutGst: TextView? = null
    private var txtNoReportData: TextView? = null
    private var txtChangeDateRange: TextView? = null
    private var mListener: ProgressIndicatorListener? = null
    private lateinit var cardView: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val bundle = arguments
        spendingData.clear()
        val json = SharedPref.read(ReportApi.TOTAL_SPENDING_DATA, null)
        if (json != null) {
            val spendingDataSharedPrefs =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<SpendingModel>>(
                    json,
                    object : TypeToken<ArrayList<SpendingModel?>?>() {}.type
                )
            if (spendingDataSharedPrefs != null) {
                spendingData.addAll(spendingDataSharedPrefs)
            }
        }
        if (bundle!!.containsKey(ReportApi.SPENDING_DATA_PERIOD)) {
            spendingTimePeriod = bundle.getString(ReportApi.SPENDING_DATA_PERIOD)
        }
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_total_spending, container, false)
        cardView = v.findViewById(R.id.report_total_spending_summary)
        cardView.setVisibility(View.GONE)
        txtTotalSpendingAmount = v.findViewById(R.id.report_total_spending_amount)
        lstTotalSpendingSupplier = v.findViewById(R.id.lst_total_spending_by_supplier)
        txtChangeDateRange = v.findViewById(R.id.txt_change_date_range)
        txtNoReportData = v.findViewById(R.id.txt_no_report_data)
        lstTotalSpendingSupplier.setLayoutManager(LinearLayoutManager(activity))
        //lstTotalSpendingSupplier.setNestedScrollingEnabled(false);
        txtAverageDailySpend = v.findViewById(R.id.report_total_spending_average_daily_value)
        txtTotalSpendingHeading = v.findViewById(R.id.txt_total_spending_detail_heading)
        txtSpendingDetailHeading =
            v.findViewById(R.id.report_total_spending_spending_detail_header_text)
        txtSpendingDetailHeading.setVisibility(View.GONE)
        txtTotalWithoutGst = v.findViewById(R.id.report_dashboard_week_txt_date_range)
        txtTotalSpendingHeading.setVisibility(View.GONE)
        lytNoSpendingData = v.findViewById(R.id.report_total_spending_no_data_layout)
        //        threeDotLoaderWhite.setVisibility(View.GONE);
        chart = v.findViewById(R.id.chart)
        setFont()
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
        setTypefaceView(txtTotalWithoutGst, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mListener!!.visibilityVisible()
        cardView!!.visibility = View.GONE
        txtSpendingDetailHeading!!.visibility = View.GONE
        val startDateEndDate = ReportApi.getDateRange(spendingTimePeriod!!)
        if (startDateEndDate != null) {
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setStartDate(startDateEndDate.startDateMillis)
            apiParamsHelper.setEndDate(startDateEndDate.endDateMillis)
            apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.SUPPLIER)
            ReportApi.retrieveSpendings(
                activity,
                apiParamsHelper,
                object : TotalSpendingSupplierResponseListener {
                    override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingBySupplier?) {
                        if (isFragmentAttached) {
                            mListener!!.visibilityGone()
                            cardView!!.visibility = View.VISIBLE
                            txtSpendingDetailHeading!!.visibility = View.VISIBLE
                            updateSupplierTotalSpending(reportResponseData)
                        }
                    }

                    override fun onTotalSpendingResponseError(error: VolleyError?) {
                        if (isFragmentAttached) mListener!!.visibilityGone()
                        cardView!!.visibility = View.VISIBLE
                        txtSpendingDetailHeading!!.visibility = View.VISIBLE
                    }
                })
        }
    }

    fun setChart(supplierModels: List<ReportResponse.TotalSpendingBySupplierData>) {
        val entries = ArrayList<PieEntry>()
        var i = 0
        var totalOthers = 0.0
        for (data in supplierModels) {
            if (i < 6) entries.add(
                PieEntry(
                    data.totalSpending?.toFloat()!!,
                    data.supplier?.supplierName
                )
            ) else totalOthers += data.totalSpending!!
            i++
        }
        if (totalOthers > 0) entries.add(
            PieEntry(
                totalOthers.toFloat(),
                getString(R.string.txt_others)
            )
        )
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

    private fun updateSupplierTotalSpending(totalSpendingBySupplier: ReportResponse.TotalSpendingBySupplier?) {
        if (isFragmentAttached) {
            var totalSpendingBySupplierList: List<ReportResponse.TotalSpendingBySupplierData> = ArrayList()
            if (totalSpendingBySupplier != null && totalSpendingBySupplier.data != null && totalSpendingBySupplier.data!!.size > 0) {
                var amount = 0.0
                var taxSpendingAmount = 0.0
                totalSpendingBySupplierList = totalSpendingBySupplier.data!!
                for (i in totalSpendingBySupplierList.indices) {
                    amount = amount + totalSpendingBySupplierList[i].totalSpending!!
                    taxSpendingAmount =
                        taxSpendingAmount + totalSpendingBySupplierList[i].taxSpending!!
                }
                val amountPriceDetail = PriceDetails()
                amountPriceDetail.amount = amount
                txtTotalSpendingAmount!!.text = amountPriceDetail.displayValue
                if (amountPriceDetail.amount != 0.0) {
                    txtSpendingDetailHeading!!.visibility = View.VISIBLE
                    lytNoSpendingData!!.visibility = View.GONE
                    if (amountPriceDetail.amount!! < 0) {
                        chart!!.visibility = View.GONE
                    } else {
                        chart!!.visibility = View.VISIBLE
                    }
                    lstTotalSpendingSupplier!!.visibility = View.VISIBLE
                    //set the list for the total spending by supplier
                    //set the list for the total spending by supplier
                    lstTotalSpendingSupplier.adapter =
                        TotalSpendingListAdapter(
                            requireContext(),
                            totalSpendingBySupplierList,
                            object : OnTotalSpendingItemClick {
                                override fun onItemClick(item: String?, supplierName: String?) {
                                    //call the spending detail Activity;
                                    logAction(
                                        activity,
                                        AnalyticsHelper.TAP_ITEM_TOTAL_SPENDING_SUPPLIER_LIST
                                    )
                                    val newIntent =
                                        Intent(activity, ReportsSpendingDetailsActivity::class.java)
                                    newIntent.putExtra(ReportApi.START_DATE, selectedStartDate)
                                    newIntent.putExtra(ReportApi.END_DATE, selectedEndDate)
                                    newIntent.putExtra(
                                        ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR,
                                        item
                                    )
                                    newIntent.putExtra(
                                        ReportApi.SPENDING_DATA_PERIOD,
                                        spendingTimePeriod
                                    )
                                    newIntent.putExtra(ReportApi.SUPPLIER_NAME, supplierName)
                                    newIntent.putExtra(
                                        ReportApi.CALLED_FROM_FRAGMENT,
                                        ReportApi.TOTAL_SPENDING_SUPPLIER_FRAGMENT
                                    )
                                    startActivity(newIntent)
                                }
                            })
                    txtTotalWithoutGst!!.visibility = View.VISIBLE
                    txtTotalWithoutGst!!.text = getString(
                        R.string.report_total_without_gst,
                        ZeemartAppConstants.Market.`this`.taxCode
                    )
                    val totWithoutGst = amount - taxSpendingAmount
                    val priceDetails = PriceDetails()
                    priceDetails.amount = totWithoutGst
                    txtAverageDailySpend!!.text = priceDetails.displayValue
                } else {
                    txtTotalSpendingAmount!!.text = amountPriceDetail.displayValue
                    txtSpendingDetailHeading!!.visibility = View.GONE
                    lytNoSpendingData!!.visibility = View.VISIBLE
                    chart!!.visibility = View.GONE
                    lstTotalSpendingSupplier!!.visibility = View.GONE
                    txtTotalWithoutGst!!.visibility = View.VISIBLE
                    txtTotalWithoutGst!!.text = getString(
                        R.string.report_total_without_gst,
                        ZeemartAppConstants.Market.`this`.taxCode
                    )
                    txtAverageDailySpend!!.text = PriceDetails().displayValue
                }
            } else {
                //hide the spending details
                txtSpendingDetailHeading!!.visibility = View.GONE
                lytNoSpendingData!!.visibility = View.VISIBLE
                chart!!.visibility = View.GONE
                lstTotalSpendingSupplier!!.visibility = View.GONE
                txtTotalWithoutGst!!.visibility = View.VISIBLE
                //hide the spending details
                txtTotalSpendingAmount!!.text = PriceDetails().displayValue
                txtTotalWithoutGst!!.text = getString(
                    R.string.report_total_without_gst,
                    ZeemartAppConstants.Market.`this`.taxCode
                )
                txtAverageDailySpend!!.text = PriceDetails().displayValue
            }
            setChart(totalSpendingBySupplierList)
        }
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
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.SUPPLIER)
        ReportApi.retrieveSpendings(
            activity,
            apiParamsHelper,
            object : TotalSpendingSupplierResponseListener {
                override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingBySupplier?) {
                    if (isFragmentAttached) {
                        mListener!!.visibilityGone()
                        updateSupplierTotalSpending(reportResponseData)
                    }
                }

                override fun onTotalSpendingResponseError(error: VolleyError?) {
                    if (isFragmentAttached) mListener!!.visibilityGone()
                }
            })
    }

    override fun onSuccessResponse(spendingDataModel: ReportSpendingDataModel?) {
        if (isFragmentAttached && spendingDataModel != null && !StringHelper.isStringNullOrEmpty(
                spendingDataModel.status
            )
        ) {
            mListener!!.visibilityGone()
            cardView!!.visibility = View.VISIBLE
            txtSpendingDetailHeading!!.visibility = View.VISIBLE
            if (spendingDataModel.status == "success") {
                spendingData.clear()
                spendingData.addAll(spendingDataModel.spendings!!)
                //updateSupplierTotalSpending();
                SharedPref.write(
                    ReportApi.TOTAL_SPENDING_DATA,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(spendingData).toString()
                )
            }
        }
    }

    override fun onErrorResponse(error: VolleyErrorHelper?) {
        if (isFragmentAttached) {
            mListener!!.visibilityGone()
            cardView!!.visibility = View.VISIBLE
            txtSpendingDetailHeading!!.visibility = View.VISIBLE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TotalSpendingSupplierFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(spendingTimePeriod: String?): TotalSpendingSupplierFragment {
            val fragment = TotalSpendingSupplierFragment()
            val args = Bundle()
            args.putString(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
            fragment.arguments = args
            return fragment
        }
    }
}