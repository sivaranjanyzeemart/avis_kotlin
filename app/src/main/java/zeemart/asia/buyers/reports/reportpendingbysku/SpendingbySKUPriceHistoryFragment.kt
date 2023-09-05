package zeemart.asia.buyers.reports.reportpendingbysku

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SpendingBySkuPriceHistoryAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.ChartsHelper.ChartXAxisValueFormatter
import zeemart.asia.buyers.helper.ChartsHelper.ChartYAxisValueFormatter
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.PriceUpdateDataListener
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse
import zeemart.asia.buyers.models.reports.SpendingModel
import zeemart.asia.buyers.network.GetPriceUpdateData
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.reports.ReportDataController
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SpendingbySKUPriceHistoryFragment : Fragment() {
    private val spendingData = ArrayList<SpendingModel>()
    var priceUpdateList: List<PriceUpdateModelBaseResponse.PriceDetailModel>? = null
    private var spendingTimePeriod: String? = null
    private var skuValue: String? = null
    private lateinit var spinnerUOM: Spinner
    private lateinit var lstPriceUpdateList: RecyclerView
    private var txtMostRecentPriceUpdate: TextView? = null
    private var txtNoPriceDataHeading: TextView? = null
    private var txtRecentPriceUpdateDate: TextView? = null
    private var txt_spinner_value: TextView? = null
    private var isFragmentAttached = false
    private var chart: LineChart? = null
    private var lytNoPriceData: LinearLayout? = null
    private var lytNestedScrollView: NestedScrollView? = null
    private var getTxtMostRecentPriceHeading: TextView? = null
    private val txt_case: TextView? = null
    private var updateHistoryHeading: TextView? = null
    private lateinit var txtspinnerUOM: TextView
    private var uom_txt: TextView? = null
    private val spinner_value: TextView? = null
    private val txt_case_unit: TextView? = null
    private val unit_size_image: ImageView? = null
    private var image_spinner: ImageView? = null
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
        callSKUPriceUpdateAPI()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_spendingby_skuprice_history, container, false)
        val bundle = arguments
        val json = SharedPref.read(ReportApi.TOTAL_SPENDING_DATA, null)
        spendingData.clear()
        val data =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson(json, Array<SpendingModel>::class.java)
        if (data != null) {
            spendingData.addAll(Arrays.asList(*data))
        }
        if (bundle!!.containsKey(ReportApi.SPENDING_DATA_PERIOD)) {
            spendingTimePeriod = bundle.getString(ReportApi.SPENDING_DATA_PERIOD)
        }
        if (bundle.containsKey(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)) {
            skuValue = bundle.getString(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR)
        }
        spinnerUOM = v.findViewById(R.id.spinner_uom)
        //  spinnerUOM.getBackground().setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
        // spinnerUOM.setPrompt(getString(R.string.txt_select_unit_size));
        spinnerUOM.setTextDirection(View.TEXT_DIRECTION_LOCALE)
        setTypefaceView(spinnerUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        lstPriceUpdateList = v.findViewById(R.id.lst_price_update_list)
        lstPriceUpdateList.setLayoutManager(LinearLayoutManager(activity))
        lstPriceUpdateList.setNestedScrollingEnabled(false)
        txtMostRecentPriceUpdate = v.findViewById(R.id.report_total_spending_amount)
        txtRecentPriceUpdateDate = v.findViewById(R.id.report_dashboard_week_txt_date_range)
        txtNoPriceDataHeading = v.findViewById(R.id.txt_no_price_data)
        txt_spinner_value = v.findViewById(R.id.txt_spinner_value)
        uom_txt = v.findViewById(R.id.uom_txt)
        lytNestedScrollView = v.findViewById(R.id.lyt_nested_scroll_view)
        lytNoPriceData = v.findViewById(R.id.report_total_spending_no_price_layout)
        getTxtMostRecentPriceHeading = v.findViewById(R.id.txt_total_spending_detail_heading)
        updateHistoryHeading = v.findViewById(R.id.txt_update_history_heading)
        image_spinner = v.findViewById(R.id.image_spinner)
        // spinner_value = v.findViewById(R.id.spinner_value);
        chart = v.findViewById(R.id.chart)
        setFont()
        return v
    }

    private fun setFont() {
        setTypefaceView(
            txtNoPriceDataHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            getTxtMostRecentPriceHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txt_case, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtMostRecentPriceUpdate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtRecentPriceUpdateDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(spinner_value, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(spinnerUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            updateHistoryHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    fun callSKUPriceUpdateAPI() {
        val startDateLong: Long = 0
        val endDateLong = Calendar.getInstance(DateHelper.marketTimeZone).timeInMillis / 1000
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSku(skuValue!!)
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        GetPriceUpdateData(requireActivity(), object : PriceUpdateDataListener {
            override fun onSuccessResponse(priceUpdateData: List<PriceUpdateModelBaseResponse.PriceDetailModel?>?) {
                if (isFragmentAttached) {
                    val nonNullPriceUpdateData = priceUpdateData?.filterNotNull() ?: emptyList()

                    if (nonNullPriceUpdateData.isNotEmpty()) {
                        for (i in nonNullPriceUpdateData.size - 1 downTo 1) {
                            Log.e(
                                "priceUpdateData",
                                "getInvoiceDate:" + nonNullPriceUpdateData[i]?.invoiceDate
                            )
                            if (nonNullPriceUpdateData[i]?.invoiceDate == null) {
                                nonNullPriceUpdateData.toMutableList().removeAt(i)
                            }
                        }
                        val reportDataController = ReportDataController()
                        val skuPriceUpdateUOMMap =
                            reportDataController.getSKUPriceUpdateUOMMap(nonNullPriceUpdateData)
                        if (skuPriceUpdateUOMMap != null && skuPriceUpdateUOMMap.size > 0) {
                            setSpinnerData(skuPriceUpdateUOMMap)
                        }
                    } else {
                        lytNestedScrollView!!.visibility = View.GONE
                        lytNoPriceData!!.visibility = View.VISIBLE
                    }
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {}
        }).getOutletPriceUpdateData(apiParamsHelper, SharedPref.defaultOutlet?.outletId!!)
    }

    fun setChart() {
//        long startDateLong = 0;
//        long endDateLong = Calendar.getInstance(DateHelper.getMarketTimeZone()).getTimeInMillis()/1000;
        val entries: MutableList<Entry> = ArrayList()
        //        List<ChartsHelper.ChartModel> chartData = ReportApi.getAllDatesForChartInPeriod(startDateLong,endDateLong);

//        Map<String, ChartsHelper.ChartModel> mapChart = new HashMap<>();
        val dayValues: MutableList<String> = ArrayList()
        //        entries.add(new Entry(0, 0));
//        entries.add(new Entry(1, 0));
//        entries.add(new Entry(2, 0));
//        entries.add(new Entry(3, 0));
//        dayValues.add("");
//        dayValues.add("");
//        dayValues.add("");
//        dayValues.add("");
        val skuList: MutableList<PriceUpdateModelBaseResponse.PriceDetailModel> = ArrayList()
        var cnt = 0
        for (item in priceUpdateList!!) {
            if (cnt < 5) skuList.add(
                ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                        item
                    ), PriceUpdateModelBaseResponse.PriceDetailModel::class.java
                )
            )
            cnt++
        }
        Collections.sort(skuList, Comparator { o1, o2 ->
            if (o1.invoiceDate!! < o2.invoiceDate!!) {
                return@Comparator -1
            } else if (o2.invoiceDate!! < o1.invoiceDate!!) {
                return@Comparator 1
            }
            0
        })
        var i = 0
        for (item in skuList) {
            entries.add(Entry(i.toFloat(), item.unitPrice?.amount?.toFloat()!!))
            dayValues.add("" + DateHelper.getDateInDateMonthFormat(item.invoiceDate))
            i++
        }
        val dataSet = LineDataSet(entries, "") // add entries to dataset
        dataSet.setDrawValues(false)
        dataSet.color = requireActivity().resources.getColor(R.color.orange)
        dataSet.setDrawCircles(false)
        dataSet.setDrawCircleHole(false)
        val lineData = LineData(dataSet)
        val xAxis = chart!!.xAxis
        xAxis.valueFormatter = ChartXAxisValueFormatter(dayValues.toTypedArray())
        xAxis.setLabelCount(dayValues.size, true)
        xAxis.setAvoidFirstLastClipping(true)
        val d = Description()
        d.text = ""
        chart!!.description = d
        chart!!.setScaleEnabled(false)
        chart!!.legend.isEnabled = false
        chart!!.setDrawGridBackground(false)
        val mv = CustomMarkerViewCharts(activity, R.layout.chart_marker_layout, false)
        mv.chartView = chart
        chart!!.marker = mv
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        val yAxisRight = chart!!.axisRight
        yAxisRight.setDrawAxisLine(false)
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawGridLines(false)
        val yAxisLeft = chart!!.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.valueFormatter = ChartYAxisValueFormatter()
        /**
         * PBF-318, added constant line when there is only one entry for the chart
         */
        yAxisLeft.removeAllLimitLines() // reset all limit lines to avoid overlapping lines
        if (dayValues.size == 1) {
            val ll1 = LimitLine(entries[0].y, "")
            ll1.lineWidth = 1f
            yAxisLeft.addLimitLine(ll1)
        }
        chart!!.data = lineData
        chart!!.invalidate() // refresh
        chart!!.animateXY(1000, 1000, Easing.EasingOption.Linear, Easing.EasingOption.Linear)
    }

    private fun setSpinnerData(skuPriceUpdateUOMMap: Map<String, List<PriceUpdateModelBaseResponse.PriceDetailModel>>?) {
        val uomList: MutableList<String> = ArrayList()
        for (key in skuPriceUpdateUOMMap!!.keys) {
            uomList.add(key)
        }
        val adapter: ArrayAdapter<String?> = object : ArrayAdapter<String?>(
            requireActivity(),
            android.R.layout.simple_spinner_dropdown_item,
            uomList as List<String?>
        ) {
            @SuppressLint("ResourceAsColor")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                txtspinnerUOM = view.findViewById(android.R.id.text1)
                txtspinnerUOM.setText(
                    txtspinnerUOM.getText().toString().lowercase(Locale.getDefault())
                )
                txtspinnerUOM.setPadding(0, 34, 2, 0)
                txtspinnerUOM.setTextColor(Color.parseColor("#2078dc"))
                setTypefaceView(
                    txtspinnerUOM,
                    ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
                )
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                txtspinnerUOM = view.findViewById(android.R.id.text1)
                txtspinnerUOM.setPadding(70, 5, 0, 5)
                txtspinnerUOM.setText(
                    txtspinnerUOM.getText().toString().lowercase(Locale.getDefault())
                )
                setTypefaceView(txtspinnerUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
                return view
            }
        }
        spinnerUOM!!.adapter = adapter
        spinnerUOM!!.setSelection(0)
        /*View v = spinnerUOM.getSelectedView();
        ((TextView)v).setTextColor(Color.parseColor("#2078dc"));*/spinnerUOM!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    Log.d("SPINNER UOM", "A value selected from the spinner")


                    //set adapter for selected item
                    priceUpdateList = skuPriceUpdateUOMMap[uomList[position]]
                    val item = parent.getItemAtPosition(position).toString()

                    /*   View v = spinnerUOM.getSelectedView();
                 ((TextView)v).setTextColor(Color.parseColor("#2078dc"));*/if (skuPriceUpdateUOMMap != null && skuPriceUpdateUOMMap.size == 1) {
                        val v1 = spinnerUOM!!.selectedView
                        v1.visibility = View.GONE
                        (v1 as TextView).setTextColor(Color.parseColor("#9b9b9b"))
                        /* ZeemartBuyerApp.setTypefaceView(v1, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
                    image_spinner.setVisibility(View.GONE);
                    spinnerUOM.setClickable(false);*/spinnerUOM!!.visibility = View.GONE
                        txtspinnerUOM!!.visibility = View.GONE
                        txt_spinner_value!!.visibility = View.VISIBLE
                        txt_spinner_value!!.text = item
                        txt_spinner_value!!.text =
                            txt_spinner_value!!.text.toString().lowercase(Locale.getDefault())
                        txt_spinner_value!!.setTextColor(Color.parseColor("#9b9b9b"))
                        setTypefaceView(
                            txt_spinner_value,
                            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
                        )
                    } else if (skuPriceUpdateUOMMap != null && skuPriceUpdateUOMMap.size > 0) {
                        val v3 = spinnerUOM!!.selectedView
                        (v3 as TextView).setTextColor(Color.parseColor("#2078dc"))
                        setTypefaceView(v3, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
                    }
                    Collections.sort(priceUpdateList, Comparator { o1, o2 ->
                        if (o2.invoiceDate!! < o1.invoiceDate!!) {
                            return@Comparator -1
                        } else if (o1.invoiceDate!! < o2.invoiceDate!!) {
                            return@Comparator 1
                        }
                        0
                    })
                    //
                    val priceWithUom = priceUpdateList!![0].unitPrice?.displayValue
                    txtMostRecentPriceUpdate!!.text = priceWithUom
                    val invoiceDate = DateHelper.getDateInDateMonthYearFormat(
                        priceUpdateList!![0].invoiceDate
                    )
                    txtRecentPriceUpdateDate!!.text =
                        String.format(getString(R.string.format_updated_on), invoiceDate)
                    lstPriceUpdateList!!.adapter =
                        SpendingBySkuPriceHistoryAdapter(requireContext(), priceUpdateList!!)
                    setChart()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Log.d("SPINNER UOM", "nothing selected")
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            spendingTimePeriod: String?,
            skuValue: String?
        ): SpendingbySKUPriceHistoryFragment {
            val fragment = SpendingbySKUPriceHistoryFragment()
            val args = Bundle()
            args.putString(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
            args.putString(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR, skuValue)
            fragment.arguments = args
            return fragment
        }
    }
}