package zeemart.asia.buyers.reports.reporttotalspending

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.github.mikephil.charting.charts.PieChart
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.TotalSpendingTagListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.AnalyticsHelper.logAction
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.ReportApi
import zeemart.asia.buyers.helper.ReportApi.TotalSpendingByTagsResponseListener
import zeemart.asia.buyers.interfaces.DateRangeChangeObserver
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.interfaces.ProgressIndicatorListener
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.reportsimport.ReportResponse
import zeemart.asia.buyers.reports.ReportsSpendingDetailsActivity

/**
 * Created by RajPrudhviMarella on 18/May/2021.
 */
/**
 * A simple [Fragment] subclass.
 * Use the [TotalSpendingTagsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TotalSpendingTagsFragment : Fragment(), DateRangeChangeObserver {
    //private ArrayList<SpendingModel> spendingData = new ArrayList<>();
    private var spendingTimePeriod: String? = null
    private var txtTotalSpendingAmount: TextView? = null
    private lateinit var lstTotalSpendingSupplier: RecyclerView
    private lateinit var txtAverageDailySpend: TextView
    private var selectedStartDate = ""
    private var selectedEndDate = ""
    private lateinit var txtTotalSpendingHeading: TextView
    private lateinit var lytNoSpendingData: LinearLayout
    private lateinit var txtSpendingDetailHeading: TextView
    private var txtNoReportData: TextView? = null
    private lateinit var txtChangeDateRange: TextView
    private var isFragmentAttached = false
    private lateinit var chart: PieChart
    private var mListener: ProgressIndicatorListener? = null
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
        lstTotalSpendingSupplier = v.findViewById(R.id.lst_total_spending_by_supplier)
        lstTotalSpendingSupplier.setLayoutManager(LinearLayoutManager(activity))
        txtAverageDailySpend = v.findViewById(R.id.report_total_spending_average_daily_value)
        txtTotalSpendingHeading = v.findViewById(R.id.txt_total_spending_detail_heading)
        txtChangeDateRange = v.findViewById(R.id.txt_change_date_range)
        txtChangeDateRange.setText(resources.getString(R.string.txt_no_data_for_tags_spending))
        txtNoReportData = v.findViewById(R.id.txt_no_report_data)
        txtTotalSpendingHeading.setVisibility(View.GONE)
        txtSpendingDetailHeading =
            v.findViewById(R.id.report_total_spending_spending_detail_header_text)
        lytNoSpendingData = v.findViewById(R.id.report_total_spending_no_data_layout)
        chart = v.findViewById(R.id.chart)
        chart.setVisibility(View.GONE)
        v.findViewById<View>(R.id.report_total_spending_summary).visibility = View.GONE
        val startDateEndDate = ReportApi.getDateRange(spendingTimePeriod!!)
        mListener!!.visibilityVisible()
        ReportApi.retrieveTagsSpending(
            activity,
            startDateEndDate?.startDateMillis!!,
            startDateEndDate.endDateMillis,
            object : TotalSpendingByTagsResponseListener {
                override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingByTags?) {
                    if (isFragmentAttached) {
                        mListener!!.visibilityGone()
                        if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                            val totalSpendingSkuDataList = reportResponseData.data
                            updateSkuTotalSpending(totalSpendingSkuDataList!!)
                        } else {
                            txtSpendingDetailHeading.setVisibility(View.GONE)
                            lytNoSpendingData.setVisibility(View.VISIBLE)
                            lstTotalSpendingSupplier.setVisibility(View.GONE)
                            txtAverageDailySpend.setText(PriceDetails().displayValue)
                        }
                    }
                }

                override fun onTotalSpendingResponseError(error: VolleyError?) {
                    if (isFragmentAttached) mListener!!.visibilityGone()
                }
            })
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

    override fun onDateRangeChanged(TimePeriodValue: String?, startDate: String?, endDate: String?) {
        spendingTimePeriod = TimePeriodValue
        selectedStartDate = startDate!!
        selectedEndDate = endDate!!
        val startDateLong = DateHelper.returnEpochTimeSOD(startDate)
        val endDateLong = DateHelper.returnEpochTimeEOD(endDate)
        //call the API to get the spending details for time range startDate end Date
        mListener!!.visibilityVisible()
        ReportApi.retrieveTagsSpending(
            activity,
            startDateLong,
            endDateLong,
            object : TotalSpendingByTagsResponseListener {
                override fun onTotalSpendingResponseSuccess(reportResponseData: ReportResponse.TotalSpendingByTags?) {
                    if (isFragmentAttached) {
                        mListener!!.visibilityGone()
                        if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                            val totalSpendingSkuDataList = reportResponseData.data
                            updateSkuTotalSpending(totalSpendingSkuDataList!!)
                        } else {
                            txtSpendingDetailHeading!!.visibility = View.GONE
                            lytNoSpendingData!!.visibility = View.VISIBLE
                            lstTotalSpendingSupplier!!.visibility = View.GONE
                            txtAverageDailySpend!!.text = PriceDetails().displayValue
                        }
                    }
                }

                override fun onTotalSpendingResponseError(error: VolleyError?) {
                    if (isFragmentAttached) mListener!!.visibilityGone()
                }
            })
    }

    private fun updateSkuTotalSpending(skuList: List<ReportResponse.TotalSpendingTagsData>) {
        if (isFragmentAttached) {
            var amount = 0.0
            for (i in skuList.indices) {
                amount = amount + skuList[i].total!!
            }
            val amountPriceDetail = PriceDetails()
            amountPriceDetail.amount = amount
            txtTotalSpendingAmount!!.text = amountPriceDetail.displayValue
            if (amountPriceDetail.amount != 0.0) {
                txtSpendingDetailHeading!!.visibility = View.VISIBLE
                lytNoSpendingData!!.visibility = View.GONE
                lstTotalSpendingSupplier!!.visibility = View.VISIBLE
                lstTotalSpendingSupplier.adapter =
                    TotalSpendingTagListAdapter(
                        requireContext(),
                        skuList,
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
                                newIntent.putExtra(
                                    ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR,
                                    item
                                )
                                newIntent.putExtra(
                                    ReportApi.SPENDING_DATA_PERIOD,
                                    spendingTimePeriod
                                )
                                newIntent.putExtra(
                                    ReportApi.CALLED_FROM_FRAGMENT,
                                    ReportApi.TOTAL_SPENDING_TAG_FRAGMENT
                                )
                                startActivity(newIntent)
                            }
                        })
                val days = ReportApi.daysBetweenDates(
                    spendingTimePeriod!!,
                    selectedStartDate,
                    selectedEndDate
                )
                val priceDetails = PriceDetails()
                priceDetails.amount = amountPriceDetail.amount?.div(days)
                txtAverageDailySpend!!.text = priceDetails.displayValue
            } else {
                txtSpendingDetailHeading!!.visibility = View.GONE
                lytNoSpendingData!!.visibility = View.VISIBLE
                lstTotalSpendingSupplier!!.visibility = View.GONE
                txtAverageDailySpend!!.text = PriceDetails().displayValue
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param spendingTimePeriod Parameter 1.
         * @return A new instance of fragment TotalSpendingTagsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(spendingTimePeriod: String?): TotalSpendingTagsFragment {
            val fragment = TotalSpendingTagsFragment()
            val args = Bundle()
            args.putString(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
            fragment.arguments = args
            return fragment
        }
    }
}