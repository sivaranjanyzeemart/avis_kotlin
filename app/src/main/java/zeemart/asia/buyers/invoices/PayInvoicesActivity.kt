package zeemart.asia.buyers.invoices

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.PayInvoicesAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoiceimport.EInvoicesGroupBySupplier
import zeemart.asia.buyers.network.InvoiceHelper
import java.util.*

/**
 * Created by RajPrudhviMarella on 6/2/2020.
 */
class PayInvoicesActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private var txtPayInvoicesHeader: TextView? = null
    private var txtOutletName: TextView? = null
    private var txtDateRage: TextView? = null
    private var imgBack: ImageView? = null
    private var txtNoInvoices: TextView? = null
    private var lytChangeCalenderDate: LinearLayout? = null
    private var lstPayInvoices: RecyclerView? = null
    private var timePickerDialog: Dialog? = null
    private var spendingTimePeriod: String? = null
    private var startDate: String? = null
    private var endDate: String? = null
    private var eInvoicesList: List<EInvoicesGroupBySupplier.EInvoiceWithPagination.InvoicesGroupBySupplier>? = ArrayList<EInvoicesGroupBySupplier.EInvoiceWithPagination.InvoicesGroupBySupplier>()
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private var lytNoInvoiceItems: LinearLayout? = null
    private var txtOverDueHeader: TextView? = null
    private var txtOverDueAmount: TextView? = null
    private var txtOverDueDate: TextView? = null
    private var txtUpcomingHeader: TextView? = null
    private var txtUpcomingAmount: TextView? = null
    private var txtUpcomingDate: TextView? = null
    private var lytCardView: ConstraintLayout? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_invoices)
        txtDateRage = findViewById<TextView>(R.id.pay_invoices_time_range_val)
        threeDotLoaderWhite =
            findViewById<CustomLoadingViewWhite>(R.id.spin_kit_loader_pay_invoices)
        txtPayInvoicesHeader = findViewById<TextView>(R.id.pay_invoices_header_text)
        txtOutletName = findViewById<TextView>(R.id.txt_outlet_name)
        lytNoInvoiceItems = findViewById<LinearLayout>(R.id.lyt_no_invoices)
        lytNoInvoiceItems!!.setVisibility(View.GONE)
        txtNoInvoices = findViewById<TextView>(R.id.invoices_no_invoices_text)
        imgBack = findViewById<ImageView>(R.id.pay_invoices_back_btn)
        imgBack!!.setOnClickListener { finish() }
        lytChangeCalenderDate = findViewById<LinearLayout>(R.id.pay_invoices_lyt_calendar)
        lytChangeCalenderDate!!.setOnClickListener(View.OnClickListener { createTimePickerDialog() })
        lstPayInvoices = findViewById<RecyclerView>(R.id.lst_pay_invoices)
        lstPayInvoices!!.setLayoutManager(LinearLayoutManager(this))
        lytCardView = findViewById<ConstraintLayout>(R.id.lyt_over_due_payment)
        lytCardView!!.setVisibility(View.GONE)
        txtOverDueHeader = findViewById<TextView>(R.id.txt_over_due_heading)
        txtOverDueAmount = findViewById<TextView>(R.id.txt_over_due_amount)
        txtOverDueDate = findViewById<TextView>(R.id.txt_over_due_date)
        txtUpcomingHeader = findViewById<TextView>(R.id.txt_upcoming_heading)
        txtUpcomingAmount = findViewById<TextView>(R.id.txt_upcoming_amount)
        txtUpcomingDate = findViewById<TextView>(R.id.txt_upcoming_date)
        setFont()
        txtOutletName!!.setText(SharedPref.defaultOutlet?.outletName)
        spendingTimePeriod = InvoicePaymentTimePeriod.DEFAULT.timePeriodValue
        setTimePeriod(spendingTimePeriod!!)
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtPayInvoicesHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOutletName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDateRage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoInvoices,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOverDueHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOverDueAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOverDueDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpcomingHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpcomingAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpcomingDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun createTimePickerDialog() {
        timePickerDialog = Dialog(this@PayInvoicesActivity, R.style.CustomDialogTheme)
        timePickerDialog!!.setContentView(R.layout.layout_pay_invoices_date_selector)
        val lytDefault: RelativeLayout =
            timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_default)
        lytDefault.setOnClickListener(View.OnClickListener {
            spendingTimePeriod = InvoicePaymentTimePeriod.DEFAULT.timePeriodValue
            setTimePeriod(spendingTimePeriod!!)
            timePickerDialog!!.dismiss()
        })
        val lytOverDue: RelativeLayout =
            timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_over_due)
        lytOverDue.setOnClickListener(View.OnClickListener {
            spendingTimePeriod = InvoicePaymentTimePeriod.OVERDUE.timePeriodValue
            setTimePeriod(spendingTimePeriod!!)
            timePickerDialog!!.dismiss()
        })
        val lytOverDueWithIn7days: RelativeLayout = timePickerDialog!!.findViewById<RelativeLayout>(
            R.id.lyt_calendar_due_with_in_7days
        )
        lytOverDueWithIn7days.setOnClickListener(View.OnClickListener {
            spendingTimePeriod = InvoicePaymentTimePeriod.DUEIN7DAYS.timePeriodValue
            setTimePeriod(spendingTimePeriod!!)
            timePickerDialog!!.dismiss()
        })
        val lytCustom: RelativeLayout =
            timePickerDialog!!.findViewById<RelativeLayout>(R.id.lyt_calendar_custom)
        lytCustom.setOnClickListener(View.OnClickListener { //populate the date picker
            val dpd: DatePickerDialog =
                DialogHelper.GetDatePickerFromTo(this@PayInvoicesActivity, startDate, endDate)
            dpd.show(getFragmentManager(), "Datepickerdialog")
        })
        val txtDefault: TextView =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_default)
        val txtDefaultRange: TextView =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_date_range_default)
        val txtOverDue: TextView =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_over_due)
        val txtOverDueRange: TextView =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_date_range_over_due)
        val txtOverDueWithIn7Days: TextView =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_due_with_in_7days)
        val txtOverDueWithIn7DaysRange: TextView =
            timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_date_range_due_with_in_7days)
        val txtCustom: TextView = timePickerDialog!!.findViewById<TextView>(R.id.txt_calendar_custom)
        ZeemartBuyerApp.setTypefaceView(
            txtDefault,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDefaultRange,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOverDue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOverDueRange,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOverDueWithIn7Days,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOverDueWithIn7DaysRange,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCustom,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val imgDefault = timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_default)
        imgDefault.visibility = View.GONE
        val imgOverDue = timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_over_due)
        imgOverDue.visibility = View.GONE
        val imgDueWithIn7Days =
            timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_due_with_in_7days)
        imgDueWithIn7Days.visibility = View.GONE
        val imgCustom = timePickerDialog!!.findViewById<ImageView>(R.id.img_date_selected_custom)
        imgCustom.visibility = View.GONE
        if (spendingTimePeriod == InvoicePaymentTimePeriod.DEFAULT.timePeriodValue) {
            imgDefault.visibility = View.VISIBLE
        }
        if (spendingTimePeriod == InvoicePaymentTimePeriod.OVERDUE.timePeriodValue) {
            imgOverDue.visibility = View.VISIBLE
        }
        if (spendingTimePeriod == InvoicePaymentTimePeriod.DUEIN7DAYS.timePeriodValue) {
            imgDueWithIn7Days.visibility = View.VISIBLE
        }
        if (spendingTimePeriod == InvoicePaymentTimePeriod.CUSTOM.timePeriodValue) {
            imgCustom.visibility = View.VISIBLE
        }
        //set Range for Default
        val lastToLastMonthRange: DateHelper.StartDateEndDate =
            DateHelper.getPaymentStartEndDate(DateHelper.PaymentDue.DEFAULT)
        val dateRangeDefault: String = lastToLastMonthRange.startEndRangeDateMonthFormat
        txtDefaultRange.setText(dateRangeDefault)
        //Set Range for over due
        val dateRangeLastWeek: DateHelper.StartDateEndDate =
            DateHelper.getPaymentStartEndDate(DateHelper.PaymentDue.OVER_DUE)
        val dateRangePreviousWeek: String = dateRangeLastWeek.startEndRangeDateMonthFormat
        txtOverDueRange.setText(dateRangePreviousWeek)
        //Set Range for Due within 7days
        val dateRangeCurrentWeek: DateHelper.StartDateEndDate =
            DateHelper.getPaymentStartEndDate(DateHelper.PaymentDue.DUE_IN_7DAYS)
        val dateRange: String = dateRangeCurrentWeek.startEndRangeDateMonthFormat
        txtOverDueWithIn7DaysRange.setText(dateRange)
        timePickerDialog!!.show()
    }

    private fun setTimePeriod(timePeriod: String) {
        var timePeriodRange = ""
        if (timePeriod == InvoicePaymentTimePeriod.DUEIN7DAYS.timePeriodValue) {
            val thisWeekRange: DateHelper.StartDateEndDate =
                DateHelper.getPaymentStartEndDate(DateHelper.PaymentDue.DUE_IN_7DAYS)
            timePeriodRange = thisWeekRange.startEndRangeDateMonthYearFormat
            startDate = thisWeekRange.startDateMonthYearString
            endDate = thisWeekRange.endDateMonthYearString
        } else if (timePeriod == InvoicePaymentTimePeriod.OVERDUE.timePeriodValue) {
            val lastMonthRange: DateHelper.StartDateEndDate =
                DateHelper.getPaymentStartEndDate(DateHelper.PaymentDue.OVER_DUE)
            timePeriodRange = lastMonthRange.startEndRangeDateMonthYearFormat
            startDate = lastMonthRange.startDateMonthYearString
            endDate = lastMonthRange.endDateMonthYearString
        } else if (timePeriod == InvoicePaymentTimePeriod.DEFAULT.timePeriodValue) {
            val lastToLastMonthRange: DateHelper.StartDateEndDate =
                DateHelper.getPaymentStartEndDate(DateHelper.PaymentDue.DEFAULT)
            timePeriodRange = lastToLastMonthRange.startEndRangeDateMonthYearFormat
            startDate = lastToLastMonthRange.startDateMonthYearString
            endDate = lastToLastMonthRange.endDateMonthYearString
        } else if (timePeriod == InvoicePaymentTimePeriod.CUSTOM.timePeriodValue) {
            timePeriodRange = "$startDate - $endDate"
        }
        txtDateRage!!.setText(timePeriodRange)
        callPendingPaymentInvoices()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZeemartAppConstants.RequestCode.FilterInvoiceActivity && resultCode == ZeemartAppConstants.ResultCode.RESULT_OK) {
            val bundle: Bundle? = getIntent().getExtras()
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.DUE_START_DATE)) {
                    startDate = bundle.getString(ZeemartAppConstants.DUE_START_DATE)
                }
                if (bundle.containsKey(ZeemartAppConstants.DUE_END_DATE)) {
                    endDate = bundle.getString(ZeemartAppConstants.DUE_END_DATE)
                }
            }
        }
        callPendingPaymentInvoices()
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
        val startDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
        startDate[year, monthOfYear] = dayOfMonth
        val endDate: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
        endDate[yearEnd, monthOfYearEnd] = dayOfMonthEnd
        if (endDate.before(startDate)) {
            ZeemartBuyerApp.getToastRed("Please enter valid date range")
        } else {
            timePickerDialog!!.dismiss()
            spendingTimePeriod = InvoicePaymentTimePeriod.CUSTOM.timePeriodValue
            val startEndDateRange: DateHelper.StartDateEndDate =
                DateHelper.returnStartDateEndDateCustomRange(startDate, endDate)
            val startEndDate: String = startEndDateRange.startEndRangeDateMonthYearFormat
            txtDateRage?.setText(startEndDate)
            //load data for new date range
            this.startDate = startEndDateRange.startDateMonthYearString
            this.endDate = startEndDateRange.endDateMonthYearString
            //callSpendingDetailAPI(true);
            callPendingPaymentInvoices()
        }
    }

    private fun callPendingPaymentInvoices() {
        lytCardView?.setVisibility(View.GONE)
        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
        val apiParamsHelper = ApiParamsHelper()
        val startDateLong: Long = DateHelper.returnEpochTimeSOD(startDate)
        val endDateLong: Long = DateHelper.returnEpochTimeEOD(endDate)
        apiParamsHelper.setDueStartDate(startDateLong)
        apiParamsHelper.setDueEndDate(endDateLong)
        apiParamsHelper.setManualPayment(true)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        InvoiceHelper.retrieveEInvoicesGroupBySupplier(
            this,
            apiParamsHelper,
            outlets,
            object : InvoiceHelper.GetEInvoicesWithPagination {
                override fun result(invoices: EInvoicesGroupBySupplier?) {
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                    if (invoices != null && invoices.data
                            ?.invoicesGroupBySupplier != null && invoices.data!!
                            .invoicesGroupBySupplier?.size!! > 0
                    ) {
                        if (invoices.data!!.overdue != null && invoices.data!!
                                .upcoming != null
                        ) {
                            lytCardView?.setVisibility(View.VISIBLE)
                            txtUpcomingAmount?.setText(
                                invoices.data!!.upcoming?.paymentAmount
                                    ?.displayValue
                            )
                            txtOverDueAmount?.setText(
                                invoices.data!!.overdue?.paymentAmount?.displayValue
                            )
                            val startEndOverDue = DateHelper.StartDateEndDate(
                                invoices.data!!.overdue?.startDate!!,
                                invoices.data!!.overdue?.endDate!!
                            )
                            val startEndUpcoming = DateHelper.StartDateEndDate(
                                invoices.data!!.upcoming?.startDate!!,
                                invoices.data!!.upcoming?.endDate!!
                            )
                            txtUpcomingDate?.setText(
                                startEndUpcoming.startEndRangeDateMonthFormat.uppercase(
                                    Locale.getDefault()
                                )
                            )
                            txtOverDueDate?.setText(
                                startEndOverDue.startEndRangeDateMonthFormat.uppercase(
                                    Locale.getDefault()
                                )
                            )
                        }
                        lstPayInvoices?.setVisibility(View.VISIBLE)
                        lytNoInvoiceItems?.setVisibility(View.GONE)
                        eInvoicesList = invoices.data!!.invoicesGroupBySupplier
                        if (eInvoicesList != null && eInvoicesList!!.size > 0) {
                            lstPayInvoices?.setAdapter(
                                PayInvoicesAdapter(
                                    this@PayInvoicesActivity,
                                    eInvoicesList!!,
                                    object : PayInvoicesAdapter.PayInvoiceSelectedListener {
                                        override fun onPayInvoicesItemSelected(invoicesGroupBySupplier: EInvoicesGroupBySupplier.EInvoiceWithPagination.InvoicesGroupBySupplier?) {
                                            val intent = Intent(
                                                this@PayInvoicesActivity,
                                                ReviewMultipleInvoicePaymentActivity::class.java
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.SUPPLIER_NAME,
                                                invoicesGroupBySupplier?.supplier
                                                    ?.supplierName
                                            )
                                            val supplierJson: String =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    invoicesGroupBySupplier?.supplier
                                                )
                                            intent.putExtra(
                                                ZeemartAppConstants.SUPPLIER_LIST,
                                                supplierJson
                                            )
                                            val invoicesJson: String =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    invoicesGroupBySupplier?.invoices
                                                )
                                            intent.putExtra(
                                                ZeemartAppConstants.INVOICES_LIST,
                                                invoicesJson
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.DUE_START_DATE,
                                                startDate
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.DUE_END_DATE,
                                                endDate
                                            )
                                            startActivityForResult(
                                                intent,
                                                ZeemartAppConstants.RequestCode.PayInvoicesActivity
                                            )
                                        }
                                    })
                            )
                        }
                    } else {
                        lstPayInvoices?.setVisibility(View.GONE)
                        lytNoInvoiceItems?.setVisibility(View.VISIBLE)
                    }
                }
            })
    }
}