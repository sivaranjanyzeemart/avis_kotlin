package zeemart.asia.buyers.orders.filter

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.FilterOrdersAdapter
import zeemart.asia.buyers.adapter.FilterOrdersAdapter.Companion.resetFilterOrderList
import zeemart.asia.buyers.adapter.FilterOrdersAdapter.SelectedFiltersListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.DeliveryStatus.Companion.clearAllDeliveryStatusFilterData
import zeemart.asia.buyers.models.DeliveryStatus.Companion.setAllDeliveryStatusUpdatedList
import zeemart.asia.buyers.models.InvoiceStatusWithFilter.Companion.clearAllInvoiceStatusFilterData
import zeemart.asia.buyers.models.InvoiceStatusWithFilter.Companion.setAllInvoiceStatusUpdatedList
import zeemart.asia.buyers.models.OrderStatus.Companion.clearAllOrderStatusFilterData
import zeemart.asia.buyers.models.OrderStatus.Companion.setOrderStatusFilterListUpdated
import zeemart.asia.buyers.models.OrderTypeWithFilter.Companion.clearAllOrderTypeFilterData
import zeemart.asia.buyers.models.OrderTypeWithFilter.Companion.setAllOrderTypeUpdatedList
import zeemart.asia.buyers.models.OutletMgr.Companion.clearAllOutletFilterData
import zeemart.asia.buyers.models.OutletMgr.Companion.outletsFilters
import zeemart.asia.buyers.models.OutletMgr.Companion.selectedOutlet
import zeemart.asia.buyers.models.OutletMgr.Companion.setOutletFilters
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr.clearAllSupplierFilterData
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr.selectedSupplierFilter
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr.supplierFilters
import zeemart.asia.buyers.orders.deliveries.DeliveryListingActivity
import zeemart.asia.buyers.orders.vieworders.ViewOrdersActivity
import java.util.*

class FilterOrdersActivity : AppCompatActivity() {
    private lateinit var lstDeliverLocation: RecyclerView
    private lateinit var lstSuppliers: RecyclerView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var btnApplyFilter: Button
    private var calledFrom: String? = null
    private var btnReset: Button? = null
    private lateinit var btnCancel: ImageButton
    private var fromDate: Long = 0
    private var untilDate: Long = 0
    private lateinit var txtSelectEndFilterDate: TextView
    private lateinit var txtSelectStartFilterDate: TextView
    private var txtFilterHeader: TextView? = null
    private var txtReset: Button? = null
    private var txtDateHeader: TextView? = null
    private var txtFromDate: TextView? = null
    private var txtUntilDate: TextView? = null
    private var txtFromDateVal: TextView? = null
    private var getTxtUntilDateValue: TextView? = null
    private var txtDeliveryLocationHeader: TextView? = null
    private var txtOrderStatusHeading: TextView? = null
    private var txtSupplierHeader: TextView? = null
    private var txtDeliveryStatusHeader: TextView? = null
    private lateinit var lytOrderStatusHeader: RelativeLayout
    private lateinit var lstOrderStatusList: RecyclerView
    private lateinit var lstDeliveryStatus: RecyclerView
    private lateinit var lytDateHeader: RelativeLayout
    private lateinit var lytDate: LinearLayout
    private var selectedFiltersListeners: SelectedFiltersListener? = null
    private var txtInvoiceStatusHeader: TextView? = null
    private lateinit var lstInvoiceStatus: RecyclerView
    private lateinit var lytInvoiceStatusHeader: RelativeLayout
    private lateinit var lytOrderTypeHeader: RelativeLayout
    private lateinit var lstOrderTypeList: RecyclerView
    private var txtOrderTypeHeader: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_orders)
        txtSelectStartFilterDate = findViewById(R.id.txt_select_from_filter_date)
        txtSelectEndFilterDate = findViewById(R.id.txt_select_until_filter_date)
        txtFilterHeader = findViewById(R.id.txt_filter)
        txtReset = findViewById(R.id.filter_btn_reset)
        txtDateHeader = findViewById(R.id.txt_date_header)
        txtFromDate = findViewById(R.id.txt_filter_from_date)
        txtFromDateVal = findViewById(R.id.txt_select_from_filter_date)
        txtUntilDate = findViewById(R.id.txt_filter_until_date)
        getTxtUntilDateValue = findViewById(R.id.txt_select_until_filter_date)
        txtDeliveryLocationHeader = findViewById(R.id.txt_delivery_location_heading)
        txtInvoiceStatusHeader = findViewById(R.id.filter_txt_invoice_status)
        txtOrderTypeHeader = findViewById(R.id.filter_txt_order_type)
        txtSupplierHeader = findViewById(R.id.filter_txt_supplier)
        lytOrderStatusHeader = findViewById(R.id.filter_lyt_order_status_header)
        lstOrderStatusList = findViewById(R.id.lst_filter_order_status_list)
        lstOrderStatusList.setNestedScrollingEnabled(false)
        lstOrderStatusList.setLayoutManager(LinearLayoutManager(this))
        txtOrderStatusHeading = findViewById(R.id.txt_view_status_heading)
        txtDeliveryStatusHeader = findViewById(R.id.filter_txt_delivery_status)
        lstDeliveryStatus = findViewById(R.id.filter_lst_delivery_status)
        lytDateHeader = findViewById(R.id.filter_lyt_date_header)
        lytDate = findViewById(R.id.filter_lyt_date)
        fromDate = SharedPref.readLong(SharedPref.FILTER_ORDERS_FROM_DATE, 0)!!
        untilDate = SharedPref.readLong(SharedPref.FILTER_ORDER_UNTIL_DATE, 0)!!
        val bundle = intent.extras

        //save the called from and selected Filters information from bundle to the corresponding lists
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        if (SharedPref.readLong(SharedPref.FILTER_ORDERS_FROM_DATE, 0) != 0L) {
            fromDate = SharedPref.readLong(SharedPref.FILTER_ORDERS_FROM_DATE, 0)!!
            txtSelectStartFilterDate.setText(DateHelper.getDateInDateMonthYearFormat(fromDate))
        }
        if (SharedPref.readLong(SharedPref.FILTER_ORDER_UNTIL_DATE, 0) != 0L) {
            untilDate = SharedPref.readLong(SharedPref.FILTER_ORDER_UNTIL_DATE, 0)!!
            txtSelectEndFilterDate.setText(DateHelper.getDateInDateMonthYearFormat(untilDate))
        }
        //initialize the listener for filter selected
        initializeFilterSelectedListener()
        nestedScrollView = findViewById(R.id.filter_nested_scroll_view)
        nestedScrollView.setNestedScrollingEnabled(false)
        lstDeliverLocation = findViewById(R.id.filter_lst_delivery_location)
        lstDeliverLocation.setNestedScrollingEnabled(false)
        lstDeliverLocation.setFocusable(false)
        lstDeliverLocation.setLayoutManager(LinearLayoutManager(this))
        lstSuppliers = findViewById(R.id.filter_lst_supplier)
        lstSuppliers.setNestedScrollingEnabled(false)
        lstSuppliers.setLayoutManager(LinearLayoutManager(this))
        lstDeliveryStatus.setNestedScrollingEnabled(false)
        lstDeliveryStatus.setLayoutManager(LinearLayoutManager(this))
        lstInvoiceStatus = findViewById(R.id.filter_lst_invoice_status)
        lstInvoiceStatus.setNestedScrollingEnabled(false)
        lstInvoiceStatus.setLayoutManager(LinearLayoutManager(this))
        lytInvoiceStatusHeader = findViewById(R.id.filter_lyt_invoice_status_header)
        lytOrderTypeHeader = findViewById(R.id.filter_lyt_order_type_header)
        lstOrderTypeList = findViewById(R.id.filter_lst_order_type)
        lstOrderTypeList.setNestedScrollingEnabled(false)
        lstOrderTypeList.setLayoutManager(LinearLayoutManager(this))
        setDateFilterClickListeners()
        if (calledFrom == ZeemartAppConstants.FILTER_CALLED_FROM_ORDERS) {
            lytOrderStatusHeader.setVisibility(View.VISIBLE)
            lstOrderStatusList.setVisibility(View.VISIBLE)
            lstOrderTypeList.setVisibility(View.VISIBLE)
            lytOrderTypeHeader.setVisibility(View.VISIBLE)
            lytInvoiceStatusHeader.setVisibility(View.VISIBLE)
            lstInvoiceStatus.setVisibility(View.VISIBLE)
            lytDateHeader.setVisibility(View.VISIBLE)
            lytDate.setVisibility(View.VISIBLE)
        } else {
            lytInvoiceStatusHeader.setVisibility(View.GONE)
            lstInvoiceStatus.setVisibility(View.GONE)
            lytOrderStatusHeader.setVisibility(View.GONE)
            lstOrderStatusList.setVisibility(View.GONE)
            lytDateHeader.setVisibility(View.GONE)
            lytDate.setVisibility(View.GONE)
            lstOrderTypeList.setVisibility(View.GONE)
            lytOrderTypeHeader.setVisibility(View.GONE)
        }
        setAdapterFilters()
        btnApplyFilter = findViewById(R.id.filter_btn_apply_filter)
        btnApplyFilter.setOnClickListener(View.OnClickListener {
            if (FilterOrdersAdapter.outletList != null && FilterOrdersAdapter.outletList!!.size > 0) {
                clearAllOutletFilterData()
                setOutletFilters(FilterOrdersAdapter.outletList)
            }
            if (FilterOrdersAdapter.suppliersList != null && FilterOrdersAdapter.suppliersList!!.size > 0) {
                clearAllSupplierFilterData()
                supplierFilters = FilterOrdersAdapter.suppliersList
            }
            if (FilterOrdersAdapter.deliveryStatusList != null && FilterOrdersAdapter.deliveryStatusList!!.size > 0) {
                clearAllDeliveryStatusFilterData()
                setAllDeliveryStatusUpdatedList(FilterOrdersAdapter.deliveryStatusList)
            }
            if (FilterOrdersAdapter.orderStatusList != null && FilterOrdersAdapter.orderStatusList!!.size > 0) {
                clearAllOrderStatusFilterData()
                setOrderStatusFilterListUpdated(FilterOrdersAdapter.orderStatusList)
            }
            if (FilterOrdersAdapter.invoiceStatusList != null && FilterOrdersAdapter.invoiceStatusList!!.size > 0) {
                clearAllInvoiceStatusFilterData()
                setAllInvoiceStatusUpdatedList(FilterOrdersAdapter.invoiceStatusList)
            }
            if (FilterOrdersAdapter.orderTypeList != null && FilterOrdersAdapter.orderTypeList!!.size > 0) {
                clearAllOrderTypeFilterData()
                setAllOrderTypeUpdatedList(FilterOrdersAdapter.orderTypeList)
            }
            val selectedOutlets = ArrayList<Outlet>()
            val selectedSuppliers = ArrayList<DetailSupplierDataModel>()
            val supplierFiltersList: List<DetailSupplierMgr>? = supplierFilters
            val outletFiltersList: List<OutletMgr>? = outletsFilters
            for (i in outletFiltersList!!.indices) {
                if (outletFiltersList[i].isOutletSelected) {
                    selectedOutlets.add(outletFiltersList[i])
                }
            }
            for (i in supplierFiltersList!!.indices) {
                if (supplierFiltersList[i].isSupplierSelected) {
                    selectedSuppliers.add(supplierFiltersList[i])
                }
            }
            var newIntent: Intent? = null
            newIntent = if (calledFrom == ZeemartAppConstants.FILTER_CALLED_FROM_ORDERS) {
                Intent(this@FilterOrdersActivity, ViewOrdersActivity::class.java)
            } else {
                Intent(this@FilterOrdersActivity, DeliveryListingActivity::class.java)
            }
            if (selectedSuppliers.size > 0) {
                val selectedSuppliertJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedSuppliers)
                newIntent.putExtra(
                    ZeemartAppConstants.SELECTED_FILTER_SUPPLIERS,
                    selectedSuppliertJson
                )
            }
            if (selectedOutlets.size > 0) {
                val selectedOutletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOutlets)
                newIntent.putExtra(
                    ZeemartAppConstants.SELECTED_FILTER_LOCATIONS,
                    selectedOutletJson
                )
            }
            SharedPref.writeLong(SharedPref.FILTER_ORDERS_FROM_DATE, fromDate)
            SharedPref.writeLong(SharedPref.FILTER_ORDER_UNTIL_DATE, untilDate)
            setResult(RESULT_OK, newIntent)
            finish()
        })
        btnReset = findViewById(R.id.filter_btn_reset)
        setResetButtonClick()
        btnCancel = findViewById(R.id.filter_btn_cancel)
        btnCancel.setOnClickListener(View.OnClickListener {
            resetFilterOrderList()
            finish()
            overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
        })
        setFont()
    }

    /**
     * set the reset button visibility on start and also set click listener on reset button
     */
    private fun setResetButtonClick() {
        if (selectedFiltersCount != 0) {
            btnReset!!.visibility = View.VISIBLE
        } else {
            btnReset!!.visibility = View.GONE
        }
        btnReset!!.setOnClickListener {
            resetFilterOrderList()
            fromDate = 0
            untilDate = 0
            txtSelectEndFilterDate!!.setText(R.string.txt_select_end_date)
            txtSelectStartFilterDate!!.setText(R.string.txt_select_start_date)
            lstOrderStatusList!!.adapter = FilterOrdersAdapter(
                this@FilterOrdersActivity,
                ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_STATUS,
                FilterOrdersAdapter.orderStatusList,
                selectedFiltersListeners!!
            )
            lstDeliverLocation!!.adapter = FilterOrdersAdapter(
                this@FilterOrdersActivity,
                ZeemartAppConstants.CALLED_FOR_OUTLET,
                FilterOrdersAdapter.outletList,
                selectedFiltersListeners!!
            )
            lstSuppliers!!.adapter = FilterOrdersAdapter(
                this@FilterOrdersActivity,
                ZeemartAppConstants.CALLED_FOR_SUPPLIER,
                FilterOrdersAdapter.suppliersList,
                selectedFiltersListeners!!
            )
            lstDeliveryStatus!!.adapter = FilterOrdersAdapter(
                this@FilterOrdersActivity,
                ZeemartAppConstants.CALLED_FROM_FILTER_DELIVERY_STATUS,
                FilterOrdersAdapter.deliveryStatusList,
                selectedFiltersListeners!!
            )
            lstInvoiceStatus!!.adapter = FilterOrdersAdapter(
                this@FilterOrdersActivity,
                ZeemartAppConstants.CALLED_FROM_FILTER_INVOICE_STATUS,
                FilterOrdersAdapter.invoiceStatusList,
                selectedFiltersListeners!!
            )
            lstOrderTypeList!!.adapter = FilterOrdersAdapter(
                this@FilterOrdersActivity,
                ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_TYPE,
                FilterOrdersAdapter.orderTypeList,
                selectedFiltersListeners!!
            )
            btnReset!!.visibility = View.GONE
        }
    }

    //initialize selected filterListner
    private fun initializeFilterSelectedListener() {
        selectedFiltersListeners = object : SelectedFiltersListener {
            override fun onFilterSelected() {
                if (!FilterOrdersAdapter.isAnyFilterSelected && fromDate == 0L && untilDate == 0L) {
                    btnReset!!.visibility = View.GONE
                } else {
                    btnReset!!.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setFont() {
        setTypefaceView(txtFilterHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnReset, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDateHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtFromDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtFromDateVal, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtUntilDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(getTxtUntilDateValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtDeliveryLocationHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtSupplierHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtInvoiceStatusHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(btnApplyFilter, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtOrderStatusHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtDeliveryStatusHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtOrderTypeHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    /**
     * sets the outlet and supplier list to be displayed in the filters
     */
    private fun setAdapterFilters() {
        lstOrderStatusList!!.adapter = FilterOrdersAdapter(
            this,
            ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_STATUS,
            OrderStatus.filterListFromSharedPrefs,
            selectedFiltersListeners!!
        )
        lstDeliverLocation!!.adapter = FilterOrdersAdapter(
            this,
            ZeemartAppConstants.CALLED_FOR_OUTLET,
            outletsFilters,
            selectedFiltersListeners!!
        )
        lstSuppliers!!.adapter = FilterOrdersAdapter(
            this,
            ZeemartAppConstants.CALLED_FOR_SUPPLIER,
            supplierFilters,
            selectedFiltersListeners!!
        )
        lstDeliveryStatus!!.adapter = FilterOrdersAdapter(
            this,
            ZeemartAppConstants.CALLED_FROM_FILTER_DELIVERY_STATUS,
            DeliveryStatus.filterListFromSharedPrefs,
            selectedFiltersListeners!!
        )
        lstInvoiceStatus!!.adapter = FilterOrdersAdapter(
            this,
            ZeemartAppConstants.CALLED_FROM_FILTER_INVOICE_STATUS,
            InvoiceStatusWithFilter.filterListFromSharedPrefs,
            selectedFiltersListeners!!
        )
        lstOrderTypeList!!.adapter = FilterOrdersAdapter(
            this,
            ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_TYPE,
            OrderTypeWithFilter.filterListFromSharedPrefs,
            selectedFiltersListeners!!
        )
    }

    private fun setDateFilterClickListeners() {
        val startDate = Calendar.getInstance(DateHelper.marketTimeZone)
        txtSelectStartFilterDate!!.setOnClickListener {
            val calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            val yy = calendar[Calendar.YEAR]
            val mm = calendar[Calendar.MONTH]
            val dd = calendar[Calendar.DAY_OF_MONTH]
            val datePicker =
                DatePickerDialog(this@FilterOrdersActivity, { view, year, monthOfYear, dayOfMonth ->
                    startDate[Calendar.YEAR] = year
                    startDate[Calendar.MONTH] = monthOfYear
                    startDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                    startDate[Calendar.HOUR_OF_DAY] = 0
                    startDate[Calendar.MINUTE] = 0
                    startDate[Calendar.SECOND] = 0
                    fromDate = startDate.timeInMillis / 1000
                    val formatedDate = DateHelper.getDateInDateMonthYearFormat(fromDate)
                    txtSelectStartFilterDate!!.text = formatedDate
                    btnReset!!.visibility = View.VISIBLE
                    if (untilDate < fromDate && untilDate != 0L) {
                        txtSelectEndFilterDate!!.setText(R.string.error_before_date)
                        txtSelectEndFilterDate!!.setTextColor(resources.getColor(R.color.pinky_red))
                    }
                }, yy, mm, dd)
            datePicker.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.txt_clear_start_date)
            ) { dialog, which ->
                txtSelectStartFilterDate!!.text =
                    resources.getString(R.string.txt_select_start_date)
                fromDate = 0
                if (!FilterOrdersAdapter.isAnyFilterSelected && untilDate == 0L) {
                    btnReset!!.visibility = View.INVISIBLE
                }
            }
            datePicker.show()
        }
        txtSelectEndFilterDate!!.setOnClickListener {
            val mCalendar = Calendar.getInstance(DateHelper.marketTimeZone)
            val yy = mCalendar[Calendar.YEAR]
            val mm = mCalendar[Calendar.MONTH]
            val dd = mCalendar[Calendar.DAY_OF_MONTH]
            val datePicker =
                DatePickerDialog(this@FilterOrdersActivity, { view, year, monthOfYear, dayOfMonth ->
                    val endDate = Calendar.getInstance(DateHelper.marketTimeZone)
                    endDate[Calendar.YEAR] = year
                    endDate[Calendar.MONTH] = monthOfYear
                    endDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                    endDate[Calendar.HOUR_OF_DAY] = 23
                    endDate[Calendar.MINUTE] = 59
                    endDate[Calendar.SECOND] = 59
                    untilDate = endDate.timeInMillis / 1000
                    val formatedDate = DateHelper.getDateInDateMonthYearFormat(untilDate)
                    if (untilDate < fromDate) {
                        txtSelectEndFilterDate!!.setText(R.string.error_before_date)
                        txtSelectEndFilterDate!!.setTextColor(resources.getColor(R.color.pinky_red))
                    } else {
                        txtSelectEndFilterDate!!.text = formatedDate
                        txtSelectEndFilterDate!!.setTextColor(resources.getColor(R.color.text_blue))
                        btnReset!!.visibility = View.VISIBLE
                    }
                }, yy, mm, dd)
            datePicker.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.txt_clear_end_date)
            ) { dialog, which ->
                txtSelectEndFilterDate!!.text = resources.getString(R.string.txt_select_end_date)
                untilDate = 0
                if (!FilterOrdersAdapter.isAnyFilterSelected && fromDate == 0L) {
                    btnReset!!.visibility = View.INVISIBLE
                }
            }
            datePicker.show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }

    companion object {
        const val REQUEST_CODE_SELECTED_FILTERS = 201
        const val REQUEST_CODE_SELECTED_GRN = 217
        @JvmStatic
        val selectedFiltersCount: Int
            get() {
                var count = 0
                if (selectedOutlet != null && selectedOutlet!!.size > 0) count = count + 1
                if (selectedSupplierFilter != null && selectedSupplierFilter!!.size > 0) count =
                    count + 1
                if (OrderStatus.statusSelectedForFilters != null && OrderStatus.statusSelectedForFilters.size > 0) count =
                    count + 1
                if (DeliveryStatus.statusSelectedForFilters != null && DeliveryStatus.statusSelectedForFilters.size > 0) count =
                    count + 1
                if (InvoiceStatusWithFilter.statusSelectedForFilters != null && InvoiceStatusWithFilter.statusSelectedForFilters.size > 0) count =
                    count + 1
                if (OrderTypeWithFilter.statusSelectedForFilters != null && OrderTypeWithFilter.statusSelectedForFilters.size > 0) count =
                    count + 1
                if (SharedPref.readLong(SharedPref.FILTER_ORDERS_FROM_DATE, 0) != 0L ||
                    SharedPref.readLong(SharedPref.FILTER_ORDER_UNTIL_DATE, 0) != 0L
                ) count = count + 1
                return count
            }
    }
}