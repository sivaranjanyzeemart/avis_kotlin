package zeemart.asia.buyers.invoices.filter

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.FilterInvoiceAdapter
import zeemart.asia.buyers.adapter.FilterInvoiceAdapter.Companion.resetFilterInvoiceList
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.InvoicesFragment
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr.clearAllSupplierFilterData
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr.selectedSupplierFilter
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr.supplierFilters

class FilterInvoiceActivity : AppCompatActivity() {
    private lateinit var lstSuppliers: RecyclerView
    private lateinit var btnApplyFilter: Button
    private var btnReset: Button? = null
    private lateinit var btnCancel: ImageButton
    private lateinit var edtSearchProcessedInvoices: EditText
    private var txtFilterHeader: TextView? = null
    private var txtInvoiceHeader: TextView? = null
    private var txtSupplierHeader: TextView? = null

    //    public static final int RESULT_FOR_FILTER_SELECTED = 1;
    private var searchString: String? = null
    private var selectedFiltersListener: FilterInvoiceAdapter.SelectedInvoiceFiltersListener? = null
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var edtSearchClear: ImageView
    private lateinit var lytSearchBar: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_invoice)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        txtInvoiceHeader = findViewById(R.id.txt_invoice_heading)
        txtSupplierHeader = findViewById(R.id.filter_txt_supplier)
        txtFilterHeader = findViewById(R.id.txt_filter)
        nestedScrollView = findViewById(R.id.filter_nested_scroll_view)
        nestedScrollView.setNestedScrollingEnabled(false)
        lstSuppliers = findViewById(R.id.filter_lst_supplier)
        lstSuppliers.setLayoutManager(LinearLayoutManager(this))
        lstSuppliers.setNestedScrollingEnabled(false)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.INVOICE_ID)) {
                searchString = bundle.getString(ZeemartAppConstants.INVOICE_ID)
            }
        }
        counter = InvoicesFragment.selectedCounter
        btnApplyFilter = findViewById(R.id.filter_btn_apply_filter)
        btnApplyFilter.setOnClickListener(View.OnClickListener {
            applyFilter()
            CommonMethods.hideKeyboard(this@FilterInvoiceActivity)
        })
        lytSearchBar = findViewById(R.id.lyt_search_bar)
        edtSearchProcessedInvoices = findViewById(R.id.edit_search)
        edtSearchClear = findViewById(R.id.edit_search_clear)
        lytSearchBar.setBackgroundColor(resources.getColor(R.color.white))
        edtSearchProcessedInvoices.setHint(resources.getString(R.string.search_supplier_or_invoice_number))
        edtSearchProcessedInvoices.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilter()
                CommonMethods.hideKeyboard(this@FilterInvoiceActivity)
            }
            false
        })
        edtSearchClear.setOnClickListener(View.OnClickListener {
            edtSearchProcessedInvoices.setText(
                ""
            )
        })
        if (searchString != null) edtSearchProcessedInvoices.setText(searchString)
        edtSearchProcessedInvoices.setSelection(edtSearchProcessedInvoices.getText().length)
        edtSearchProcessedInvoices.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                searchString = edtSearchProcessedInvoices.getText().toString()
                if (counter == 0 && StringHelper.isStringNullOrEmpty(searchString) && searchString!!.length == 0) {
                    btnReset!!.visibility = View.GONE
                    edtSearchClear.setVisibility(View.GONE)
                } else {
                    btnReset!!.visibility = View.VISIBLE
                    edtSearchClear.setVisibility(View.VISIBLE)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        btnCancel = findViewById(R.id.filter_btn_cancel)
        btnCancel.setOnClickListener(View.OnClickListener {
            resetFilterInvoiceList()
            finish()
            overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
        })
        btnReset = findViewById(R.id.filter_btn_reset)
        initializeFilterSelectedListener()
        setResetButtonClick()
        setAdapterFilters()
        setFont()
    }

    private fun setAdapterFilters() {
        lstSuppliers!!.adapter = FilterInvoiceAdapter(
            selectedFiltersListener!!,
            this,
            supplierFilters!!
        )
    }

    private fun setResetButtonClick() {
        val selectedSuppliers = selectedSupplierFilter
        searchString = edtSearchProcessedInvoices!!.text.toString()
        if (selectedSuppliers != null && selectedSuppliers.size > 0 || !StringHelper.isStringNullOrEmpty(
                searchString
            )
        ) {
            btnReset!!.visibility = View.VISIBLE
        } else {
            btnReset!!.visibility = View.INVISIBLE
        }
        btnReset!!.setOnClickListener {
            resetFilterInvoiceList()
            searchString = null
            lstSuppliers!!.adapter = FilterInvoiceAdapter(
                selectedFiltersListener!!,
                this@FilterInvoiceActivity,
                FilterInvoiceAdapter.suppliersList!!
            )
            edtSearchProcessedInvoices.setText(null)
            btnReset!!.visibility = View.GONE
            counter = 0
        }
    }

    private fun applyFilter() {
        searchString = edtSearchProcessedInvoices!!.text.toString()
        SharedPref.write(ZeemartAppConstants.FILTER_INVOICE_ID, searchString)
        if (FilterInvoiceAdapter.suppliersList != null && FilterInvoiceAdapter.suppliersList!!.size > 0) {
            clearAllSupplierFilterData()
            supplierFilters = FilterInvoiceAdapter.suppliersList as MutableList<DetailSupplierMgr>?
        }
        val selectedSuppliers = ArrayList<DetailSupplierDataModel>()
        val supplierFiltersList: List<DetailSupplierMgr>? = supplierFilters
        for (i in supplierFiltersList!!.indices) {
            if (supplierFiltersList[i].isSupplierSelected) {
                selectedSuppliers.add(supplierFiltersList[i])
            }
        }
        val newIntent = Intent()
        if (selectedSuppliers.size > 0) {
            val selectedSupplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedSuppliers)
            newIntent.putExtra(ZeemartAppConstants.SELECTED_FILTER_SUPPLIERS, selectedSupplierJson)
        }
        setResult(ZeemartAppConstants.ResultCode.RESULT_OK, newIntent)
        finish()
    }

    private fun initializeFilterSelectedListener() {
        selectedFiltersListener = object : FilterInvoiceAdapter.SelectedInvoiceFiltersListener {
            override fun onFilterSelected() {
                counter = counter + 1
                updateFilter(counter)
            }

            override fun onFilterDeselected() {
                counter = counter - 1
                updateFilter(counter)
            }
        }
    }

    private fun updateFilter(counter: Int) {
        searchString = edtSearchProcessedInvoices!!.text.toString()
        if (counter == 0 && StringHelper.isStringNullOrEmpty(searchString) && searchString!!.length == 0) {
            btnReset!!.visibility = View.GONE
        } else {
            btnReset!!.visibility = View.VISIBLE
        }
    }

    private fun setFont() {
        setTypefaceView(txtFilterHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtInvoiceHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSupplierHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnApplyFilter, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnReset, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            edtSearchProcessedInvoices,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    companion object {
        var counter = 0
    }
}