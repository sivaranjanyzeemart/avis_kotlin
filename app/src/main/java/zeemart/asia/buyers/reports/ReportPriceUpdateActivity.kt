package zeemart.asia.buyers.reports

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.AllPriceUpdateListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper.GetDatePickerFromTo
import zeemart.asia.buyers.helper.ReportApi
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.interfaces.PriceUpdateDataListener
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse
import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI
import zeemart.asia.buyers.models.reportsimportimport.PriceUpdateListWithStickyHeaderUI
import zeemart.asia.buyers.network.GetPriceUpdateData
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.reports.reportpendingbysku.ReportsSpendingBySKUActivity
import java.text.SimpleDateFormat
import java.util.*

class ReportPriceUpdateActivity : AppCompatActivity(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener {
    private lateinit var lytChangeCalendarDate: LinearLayout
    private lateinit var txtTimePeriod: TextView
    private var timePickerDialog: Dialog? = null
    private lateinit var btnBack: ImageView
    private var spendingTimePeriod = ReportTimePeriod.CUSTOM.timePeriodValue
    private lateinit var lstPriceUpdate: RecyclerView
    private var txtNoPriceUpdates: TextView? = null
    private var imgNoPriceUpdates: ImageView? = null
    private val selectedStartDate: String? = null
    private val selectedEndDate: String? = null
    private var currentDate: String? = null
    private var dueDate: String? = null
    private var txtPriceUpdateHeading: TextView? = null
    private lateinit var txtOutletName: TextView
    private lateinit var txt_pastmonth_price_updates: TextView
    private var txt_no_price_details_no_data: TextView? = null
    private var txt_no_price_details_no_data_buyer_hub: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_price_update)
        txtTimePeriod = findViewById(R.id.report_total_spending_time_range_val)
        lytChangeCalendarDate = findViewById(R.id.report_total_spending_lyt_calendar)
        lytChangeCalendarDate.setOnClickListener(this)
        btnBack = findViewById(R.id.report_total_spending_bak_btn)
        btnBack.setOnClickListener(this)
        lstPriceUpdate = findViewById(R.id.price_updates_lst_price_changes)
        lstPriceUpdate.setLayoutManager(LinearLayoutManager(this))
        txtNoPriceUpdates = findViewById(R.id.txt_no_price_details)
        txt_no_price_details_no_data = findViewById(R.id.txt_no_price_details_no_data)
        imgNoPriceUpdates = findViewById(R.id.img_no_price_details)
        txt_no_price_details_no_data_buyer_hub =
            findViewById(R.id.txt_no_price_details_no_data_buyer_hub)
        //call the price update API
        val startEndDate = DateHelper.pastDaysStartEndDate(90, false)
        dueDate = DateHelper.getDateInDateMonthYearFormat(startEndDate.startDateMillis)
        currentDate = DateHelper.getDateInDateMonthYearFormat(startEndDate.endDateMillis)
        txtTimePeriod.setText("$selectedStartDate - $selectedEndDate")
        txtPriceUpdateHeading = findViewById(R.id.report_total_spending_header_text)
        txt_pastmonth_price_updates = findViewById(R.id.txt_pastmonth_price_updates)
        txtOutletName = findViewById(R.id.txt_outlet_name)
        txtOutletName.setText(SharedPref.defaultOutlet?.currentOutletName)
        val date = Date()
        var df = SimpleDateFormat("dd MMM yyyy")
        val c1 = Calendar.getInstance()
        currentDate = df.format(date)
        c1.add(Calendar.DAY_OF_YEAR, -90)
        df = SimpleDateFormat("dd MMM yyyy")
        val resultDate = c1.time
        txt_pastmonth_price_updates.setText(resources.getString(R.string.format_current_date_to_past_90_days) + " (" + dueDate + " - " + currentDate + ")")
        callPriceUpdateAPI()
        setFonts()
    }

    private fun setFonts() {
        setTypefaceView(
            txtPriceUpdateHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtTimePeriod, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txt_pastmonth_price_updates,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.report_total_spending_lyt_calendar -> createTimePickerDialog()
            R.id.lyt_calendar_this_week -> {
                spendingTimePeriod = ReportTimePeriod.THISWEEK.timePeriodValue
                setTimePeriod(spendingTimePeriod)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_last_week -> {
                spendingTimePeriod = ReportTimePeriod.LASTWEEK.timePeriodValue
                setTimePeriod(spendingTimePeriod)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_this_month -> {
                spendingTimePeriod = ReportTimePeriod.THISMONTH.timePeriodValue
                setTimePeriod(spendingTimePeriod)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_last_month -> {
                spendingTimePeriod = ReportTimePeriod.LASTMONTH.timePeriodValue
                setTimePeriod(spendingTimePeriod)
                timePickerDialog!!.dismiss()
            }
            R.id.lyt_calendar_custom -> {
                //poulate the date picker
//                Calendar now = Calendar.getInstance(DateHelper.getMarketTimeZone());
//                DatePickerDialog dpd = DatePickerDialog.newInstance(
//                        this,
//                        now.get(Calendar.YEAR),
//                        now.get(Calendar.MONTH),
//                        now.get(Calendar.DAY_OF_MONTH)
//                );
                val dpd = GetDatePickerFromTo(this, dueDate, currentDate)
                dpd.show(fragmentManager, "Datepickerdialog")
            }
            R.id.report_total_spending_bak_btn -> finish()
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
        setTypefaceView(txtNoPriceUpdates, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txt_no_price_details_no_data,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txt_no_price_details_no_data_buyer_hub,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
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

    private fun setTimePeriod(timePeriod: String?) {
        var timePeriodRange = ""
        if (timePeriod == ReportTimePeriod.THISWEEK.timePeriodValue) {
            val thisWeekRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
            timePeriodRange = thisWeekRange.startEndRangeDateMonthYearFormat
            dueDate = thisWeekRange.startDateMonthYearString
            currentDate = thisWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTWEEK.timePeriodValue) {
            val lastWeekRange = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
            timePeriodRange = lastWeekRange.startEndRangeDateMonthYearFormat
            dueDate = lastWeekRange.startDateMonthYearString
            currentDate = lastWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTWEEK.timePeriodValue) {
            val lastToLastWeekRange =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_WEEK)
            timePeriodRange = lastToLastWeekRange.startEndRangeDateMonthYearFormat
            dueDate = lastToLastWeekRange.startDateMonthYearString
            currentDate = lastToLastWeekRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.THISMONTH.timePeriodValue) {
            val thisMonthRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.CURRENT_MONTH)
            timePeriodRange = thisMonthRange.startEndRangeDateMonthYearFormat
            dueDate = thisMonthRange.startDateMonthYearString
            currentDate = thisMonthRange.endDateMonthYearString
        } else if (timePeriod == ReportTimePeriod.LASTMONTH.timePeriodValue) {
            val lastMonthRange = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_MONTH)
            timePeriodRange = lastMonthRange.startEndRangeDateMonthYearFormat
        } else if (timePeriod == ReportTimePeriod.LASTTOLASTMONTH.timePeriodValue) {
            val lastToLastMonthRange =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_MONTH)
            timePeriodRange = lastToLastMonthRange.startEndRangeDateMonthYearFormat
            dueDate = lastToLastMonthRange.startDateMonthYearString
            currentDate = lastToLastMonthRange.endDateMonthYearString
        }
        //call the price update API
        callPriceUpdateAPI()
        txtTimePeriod!!.text = timePeriodRange
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
        Log.d(
            "START END DATE",
            "$year-$monthOfYear-$dayOfMonth****$yearEnd--$monthOfYearEnd-$dayOfMonthEnd"
        )
        val startDate = Calendar.getInstance(DateHelper.marketTimeZone)
        startDate[year, monthOfYear] = dayOfMonth
        DateHelper.setCalendarStartOfDayTime(startDate)
        val endDate = Calendar.getInstance(DateHelper.marketTimeZone)
        endDate[yearEnd, monthOfYearEnd] = dayOfMonthEnd
        DateHelper.setCalendarEndOfDayTime(endDate)
        /*LocalDate startDate = new LocalDate(year,monthOfYear+1,dayOfMonth);
        LocalDate endDate = new LocalDate(yearEnd,monthOfYearEnd+1,dayOfMonthEnd);*/if (endDate.before(
                startDate
            )
        ) {
            getToastRed(getString(R.string.txt_enter_valid_date_range))
        } else {
            timePickerDialog!!.dismiss()
            spendingTimePeriod = ReportTimePeriod.CUSTOM.timePeriodValue
            val startEndDateRange = DateHelper.returnStartDateEndDateCustomRange(startDate, endDate)
            dueDate = startEndDateRange.startDateMonthYearString
            currentDate = startEndDateRange.endDateMonthYearString
            val startEndDate = startEndDateRange.startEndRangeDateMonthYearFormat
            txtTimePeriod!!.text = startEndDate
            //call the price update API
            callPriceUpdateAPI()
        }
    }

    private fun callPriceUpdateAPI() {
        val startDateLong = DateHelper.returnEpochTimeSOD(dueDate)
        val endDateLong = DateHelper.returnEpochTimeEOD(currentDate)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        GetPriceUpdateData(this, object : PriceUpdateDataListener {
            override fun onSuccessResponse(priceUpdateData: List<PriceUpdateModelBaseResponse.PriceDetailModel?>?) {
                if (priceUpdateData != null && priceUpdateData.size > 0) {
                    lstPriceUpdate!!.visibility = View.VISIBLE
                    txtNoPriceUpdates!!.visibility = View.GONE
                    txt_no_price_details_no_data!!.visibility = View.GONE
                    txt_no_price_details_no_data_buyer_hub!!.visibility = View.GONE
                    imgNoPriceUpdates!!.visibility = View.GONE
                    getListWithDateHeaders(priceUpdateData as List<PriceUpdateModelBaseResponse.PriceDetailModel>)
                    lstPriceUpdate.adapter = AllPriceUpdateListAdapter(
                        this@ReportPriceUpdateActivity,
                        getListWithDateHeaders(priceUpdateData),
                        object : OnTotalSpendingItemClick {
                            override fun onItemClick(item: String?, SKUName: String?) {
                                val newIntent = Intent(
                                    this@ReportPriceUpdateActivity,
                                    ReportsSpendingBySKUActivity::class.java
                                )
                                newIntent.putExtra(ReportApi.START_DATE, dueDate)
                                newIntent.putExtra(ReportApi.END_DATE, currentDate)
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
                                    ReportApi.ALL_PRICE_UPDATE_ACTIVITY
                                )
                                startActivity(newIntent)
                            }
                        })
                } else {
                    lstPriceUpdate!!.visibility = View.GONE
                    txtNoPriceUpdates!!.visibility = View.VISIBLE
                    txt_no_price_details_no_data_buyer_hub!!.visibility = View.VISIBLE
                    txt_no_price_details_no_data!!.visibility = View.VISIBLE
                    imgNoPriceUpdates!!.visibility = View.VISIBLE
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {}
        }).getOutletPriceUpdateData(apiParamsHelper, SharedPref.defaultOutlet?.outletId!!)
    }

    private fun getListWithDateHeaders(productList: List<PriceUpdateModelBaseResponse.PriceDetailModel>): ArrayList<PriceUpdateListWithStickyHeaderUI> {
        val listWithHeaders = ArrayList<PriceUpdateListWithStickyHeaderUI>()
        //sort the list by Date
        Collections.sort(productList, Comparator { o1, o2 ->
            if (o2.invoiceDate!! < o1.invoiceDate!!) {
                return@Comparator -1
            } else if (o1.invoiceDate!! < o2.invoiceDate!!) {
                return@Comparator 1
            }
            0
        })
        var currentHeaderDate: String? = null
        for (i in productList.indices) {
            val invoiceDateArray =
                DateHelper.getDateInDateMonthYearFormat(productList[i].invoiceDate)
            val invoiceDayArray = DateHelper.getDateIn3LetterDayFormat(productList[i].invoiceDate)
            if (listWithHeaders.size == 0) {
                //set the first header list entry 0
                val headerFirst = HeaderViewOrderUI()
                headerFirst.date = invoiceDateArray
                headerFirst.day = invoiceDayArray
                val priceUpdateHeader = PriceUpdateListWithStickyHeaderUI()
                priceUpdateHeader.isHeader = true
                priceUpdateHeader.headerData = headerFirst
                priceUpdateHeader.skuPriceData != null
                listWithHeaders.add(priceUpdateHeader)
                currentHeaderDate = invoiceDateArray

                //set the price update data list entry 1
                val priceUpdateValue = PriceUpdateListWithStickyHeaderUI()
                priceUpdateValue.isHeader = false
                priceUpdateValue.headerData = null
                priceUpdateValue.skuPriceData = productList[i]
                listWithHeaders.add(priceUpdateValue)
            } else {
                //same header just add the price update value
                if (currentHeaderDate == invoiceDateArray) {
                    //set the price update data list entry
                    val priceUpdateValue = PriceUpdateListWithStickyHeaderUI()
                    priceUpdateValue.isHeader = false
                    priceUpdateValue.headerData = null
                    priceUpdateValue.skuPriceData = productList[i]
                    listWithHeaders.add(priceUpdateValue)
                } else {
                    //set the header entry
                    val headerFirst = HeaderViewOrderUI()
                    headerFirst.date = invoiceDateArray
                    headerFirst.day = invoiceDayArray
                    val priceUpdateHeader = PriceUpdateListWithStickyHeaderUI()
                    priceUpdateHeader.isHeader = true
                    priceUpdateHeader.headerData = headerFirst
                    priceUpdateHeader.skuPriceData != null
                    listWithHeaders.add(priceUpdateHeader)
                    currentHeaderDate = invoiceDateArray

                    //set the price update data in list
                    val priceUpdateValue = PriceUpdateListWithStickyHeaderUI()
                    priceUpdateValue.isHeader = false
                    priceUpdateValue.headerData = null
                    priceUpdateValue.skuPriceData = productList[i]
                    listWithHeaders.add(priceUpdateValue)
                }
            }
        }
        return listWithHeaders
    }
}