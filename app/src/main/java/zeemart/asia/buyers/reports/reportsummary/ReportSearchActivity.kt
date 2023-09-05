package zeemart.asia.buyers.reports.reportsummary

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.ReportsSearchSupplierListAdapter
import zeemart.asia.buyers.adapter.ReportsSearchSupplierListAdapter.ItemClickListener
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.OutletSpendingHistoryDataListener
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.reports.SpendingModel
import zeemart.asia.buyers.models.reportsimport.Products
import zeemart.asia.buyers.models.reportsimport.ReportsSearchUi
import zeemart.asia.buyers.models.reportsimportimport.ReportSpendingDataModel
import zeemart.asia.buyers.network.GetOutletSpendingHistoryData
import zeemart.asia.buyers.network.MarketListApi
import zeemart.asia.buyers.network.MarketListApi.ProductListListener
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.reports.ReportTimePeriod
import zeemart.asia.buyers.reports.ReportsSpendingDetailsActivity
import zeemart.asia.buyers.reports.reportpendingbysku.ReportsSpendingBySKUActivity
import java.util.*

/**
 * Created by Muthumari on 25/02/2020.
 */
class ReportSearchActivity : AppCompatActivity(), OutletSpendingHistoryDataListener {
    private val txtSupplierHeader: TextView? = null
    private val txtSkuHeader: TextView? = null
    private var txtRecentSearchHeader: TextView? = null
    private lateinit var txtSearchClear: TextView
    private var txtNoResult: TextView? = null
    private lateinit var txtCancel: TextView
    private var textSearch: TextView? = null
    private var layoutSearch: RelativeLayout? = null
    private lateinit var lstSupplier: RecyclerView
    private lateinit var lstRecentSearch: ListView
    private lateinit var edtSearch: EditText
    private lateinit var edtSearchClear: ImageView
    private var selectedStartDate = ""
    private var selectedEndDate = ""
    private var spendingTimePeriod: String? = null
    private lateinit var lytNoResults: RelativeLayout
    private var recentSearchList: MutableList<String>? = ArrayList()
    private val lastToLastMonthSpending: MutableList<SpendingModel> = ArrayList()
    private var outletId: String? = null
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    private var skuList: List<Products>? = null
    private var supplierList: List<DetailSupplierDataModel>? = null
    private var lstSearchData: ArrayList<ReportsSearchUi>? = null
    private var searchedString = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_search)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        outletId = if (SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")!!.isEmpty()) {
            SharedPref.defaultOutlet?.outletId
        } else {
            SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
        }
        lastTheeeMonthsDates
        txtRecentSearchHeader = findViewById(R.id.txt_recent_search)
        txtNoResult = findViewById(R.id.txt_no_search_found)
        txtCancel = findViewById(R.id.search_btn_cancel)
        txtCancel.setOnClickListener(View.OnClickListener { finish() })
        txtSearchClear = findViewById(R.id.txt_recent_search_clear)
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_report_search_white)
        txtSearchClear.setOnClickListener(View.OnClickListener {
            recentSearchList!!.clear()
            setRecentSearchListAdapter(recentSearchList)
            setRecentSearchListVisibility()
            SharedPref.removeString(SharedPref.REPORT_RECENT_SEARCH)
        })
        lytNoResults = findViewById(R.id.lyt_no_search_results)
        lytNoResults.setVisibility(View.GONE)
        lstSupplier = findViewById(R.id.lst_search_supplier_sku)
        lstSupplier.setLayoutManager(LinearLayoutManager(this))
        lstRecentSearch = findViewById(R.id.lst_recent_search)
        lstRecentSearch.setOnItemClickListener(AdapterView.OnItemClickListener { arg0, arg1, position, id ->
            val name = recentSearchList!![position]
            if (name != null) {
                edtSearch!!.setText(name)
                edtSearch!!.setSelection(edtSearch!!.text.length)
            }
        })
        val jsonText = SharedPref.read(SharedPref.REPORT_RECENT_SEARCH, "")
        val lstRecentSearch =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson(jsonText, Array<String>::class.java)
        if (lstRecentSearch != null) recentSearchList = ArrayList(Arrays.asList(*lstRecentSearch))
        setRecentSearchListAdapter(recentSearchList)
        edtSearch = findViewById(R.id.edit_search)
        edtSearchClear = findViewById(R.id.edit_search_clear)
        layoutSearch = findViewById(R.id.lyt_search)
        textSearch = findViewById(R.id.txt_search)
        edtSearch.setHint(resources.getString(R.string.txt_report_search_sku_supplier))
        edtSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event -> actionId == EditorInfo.IME_ACTION_SEARCH })
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val searchText = edtSearch.getText().toString().trim { it <= ' ' }
                if (charSequence.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear.setVisibility(View.VISIBLE)
                } else {
                    edtSearchClear.setVisibility(View.GONE)
                }
                FilterSuppliers().filter(edtSearch.getText().toString())
                FilterProducts().filter(edtSearch.getText().toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        edtSearchClear.setOnClickListener(View.OnClickListener { edtSearch.setText("") })
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        callSupplierList()
        outletSpendingHistoryData
        setRecentSearchListVisibility()
        setSupplierListVisibility()
        setFont()
    }

    private fun setSupplierListVisibility() {
        if (lstSearchData != null && lstSearchData!!.size > 0 && !StringHelper.isStringNullOrEmpty(
                edtSearch!!.text.toString()
            )
        ) {
            lstSupplier!!.visibility = View.VISIBLE
            layoutSearch!!.visibility = View.GONE
        } else {
            lstSupplier!!.visibility = View.GONE
        }
    }

    private fun setRecentSearchListVisibility() {
        if (recentSearchList != null && recentSearchList!!.size > 0 && StringHelper.isStringNullOrEmpty(
                edtSearch!!.text.toString()
            )
        ) {
            lstRecentSearch!!.visibility = View.VISIBLE
            txtRecentSearchHeader!!.visibility = View.VISIBLE
            txtSearchClear!!.visibility = View.VISIBLE
            layoutSearch!!.visibility = View.GONE
            lytNoResults!!.visibility = View.GONE
            lstSupplier!!.visibility = View.GONE
        } else {
            lstRecentSearch!!.visibility = View.GONE
            txtRecentSearchHeader!!.visibility = View.GONE
            txtSearchClear!!.visibility = View.GONE
            layoutSearch!!.visibility = View.VISIBLE
        }
    }

    private fun updateNoResultLayout() {
        if ((lstSearchData == null || lstSearchData!!.size == 0) && !StringHelper.isStringNullOrEmpty(
                edtSearch!!.text.toString()
            )
        ) {
            lytNoResults!!.visibility = View.VISIBLE
            layoutSearch!!.visibility = View.GONE
        } else {
            lytNoResults!!.visibility = View.GONE
        }
    }

    private fun setReportSearchLayout() {
        setRecentSearchListVisibility()
        setSupplierListVisibility()
        updateNoResultLayout()
    }

    private fun setFont() {
        setTypefaceView(txtSupplierHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSkuHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtRecentSearchHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtSearchClear, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtNoResult, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(textSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCancel, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    private val outletSpendingHistoryData: Unit
        private get() {
            lastToLastMonthSpending.clear()
            val currentMonthStartEndDate =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.TWO_MONTHS_AGO)
            GetOutletSpendingHistoryData(
                this,
                this
            ).getOutletSpendingData(
                currentMonthStartEndDate.startDateMillis,
                currentMonthStartEndDate.endDateMillis,
                true,
                SharedPref.defaultOutlet!!
            )
        }

    private fun callSupplierList() {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId!!)
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        MarketListApi.retrieveSupplierListNew(
            this,
            apiParamsHelper,
            outlets,
            object : SupplierListResponseListener {
                override fun onSuccessResponse(supplierList1: List<DetailSupplierDataModel?>?) {
                    supplierList = supplierList1 as List<DetailSupplierDataModel>?
                    callProductList()
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    val errorMessage = error?.errorMessage
                    val errorType = error?.errorType
                    if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        getToastRed(errorMessage)
                    }
                }
            })
    }

    private fun SetRecentSearchList(name: String) {
        if (!recentSearchList!!.contains(name)) {
            recentSearchList!!.add(0, name)
        }
        saveRecentSearchInSharedPref(recentSearchList)
        setRecentSearchListAdapter(recentSearchList)
    }

    private fun callProductList() {
        MarketListApi.retrieveProductListOutlet(this, outletId!!, object : ProductListListener {
            override fun onSuccessResponse(products: Array<Products?>?) {
                threeDotLoaderWhite!!.visibility = View.GONE
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                if (products != null && products.size > 0) {
                    skuList = ArrayList(Arrays.asList(*products))
                    setSupplierName()
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {
                threeDotLoaderWhite!!.visibility = View.GONE
                val errorMessage = error?.errorMessage
                val errorType = error?.errorType
                if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                    getToastRed(errorMessage)
                }
            }
        })
    }

    private fun setSupplierName() {
        if (supplierList != null && supplierList!!.size > 0 && skuList != null && skuList!!.size > 0) {
            for (i in supplierList!!.indices) {
                for (j in skuList!!.indices) {
                    if (supplierList!![i].supplier.supplierId == skuList!![j].supplierId) {
                        skuList!![j].supplierName = supplierList!![i].supplier.supplierName
                    }
                }
            }
        }
    }

    private fun setRecentSearchListAdapter(recentSearchLst: MutableList<String>?) {
        if (recentSearchLst != null && recentSearchLst.size > 0) for (i in recentSearchLst.indices) {
            if (recentSearchLst.size > 10) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val adapter: ArrayAdapter<String?> = object : ArrayAdapter<String?>(
            applicationContext,
            android.R.layout.simple_list_item_1, recentSearchLst!! as List<String?>
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val typeface = Typeface.createFromAsset(
                    context.assets,
                    "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
                )
                val text = view.findViewById<View>(android.R.id.text1) as TextView
                text.setTypeface(typeface)
                return view
            }
        }
        lstRecentSearch!!.adapter = adapter
    }

    ////Getting Last three month dates details from now
    private val lastTheeeMonthsDates: Unit
        private get() {
            ////Getting Last three month dates details from now
            val startDate = Calendar.getInstance(DateHelper.marketTimeZone)
            startDate.add(Calendar.MONTH, -3)
            startDate[Calendar.DAY_OF_MONTH] = 1
            val endDate = Calendar.getInstance(DateHelper.marketTimeZone)
            spendingTimePeriod = ReportTimePeriod.CUSTOM.timePeriodValue
            val startEndDateRange = DateHelper.returnStartDateEndDateCustomRange(startDate, endDate)
            selectedStartDate = startEndDateRange.startDateMonthYearString
            selectedEndDate = startEndDateRange.endDateMonthYearString
        }

    override fun onSuccessResponse(spendingDataModel: ReportSpendingDataModel?) {
//        call the API for last to last month spending data
        val lastToLastMonthStartEndDate =
            DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.TWO_MONTHS_AGO)
        GetOutletSpendingHistoryData(
            this@ReportSearchActivity,
            object : OutletSpendingHistoryDataListener {
                override fun onSuccessResponse(spendingDataModel: ReportSpendingDataModel?) {
                    if (spendingDataModel != null && !StringHelper.isStringNullOrEmpty(
                            spendingDataModel.status
                        )
                    ) {
                        if (spendingDataModel.status == "success") {
                            lastToLastMonthSpending.addAll(spendingDataModel.spendings!!)
                            val dataJson =
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(lastToLastMonthSpending)
                            SharedPref.write(ReportApi.TOTAL_SPENDING_DATA, dataJson)
                        }
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {}
            }).getOutletSpendingData(
            lastToLastMonthStartEndDate.startDateMillis,
            lastToLastMonthStartEndDate.endDateMillis,
            true,
            SharedPref.defaultOutlet!!
        )
    }

    override fun onErrorResponse(error: VolleyErrorHelper?) {
        Log.d("VolleyError", error?.message + "***")
        //        String errorMessage = error.getErrorMessage();
//        String errorType = error.getErrorType();
//        if (errorType.equals(ServiceConstant.STATUS_CODE_403_404_405_MESSAGE)) {
//            ZeemartBuyerApp.getToastRed(errorMessage);
//        }
    }

    private fun saveRecentSearchInSharedPref(recentSearchLst: MutableList<String>?) {
        for (i in recentSearchLst!!.indices) {
            if (recentSearchLst.size > 10) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val recentSearchJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(recentSearchList)
        SharedPref.write(SharedPref.REPORT_RECENT_SEARCH, recentSearchJson)
    }

    private inner class FilterSuppliers : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (charSequence != null && charSequence.length > 0) {
                searchedString = charSequence as String
                val tempList: MutableList<DetailSupplierDataModel> = ArrayList()
                // search content in friend list
                for (i in supplierList!!.indices) {
                    if (supplierList!![i].supplier.supplierName?.lowercase(Locale.getDefault())
                            !!.contains(
                                charSequence.toString().lowercase(
                                    Locale.getDefault()
                                )
                            )
                    ) {
                        tempList.add(supplierList!![i])
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
            } else {
                searchedString = ""
                filterResults.count = supplierList!!.size
                filterResults.values = supplierList
            }
            return filterResults
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            val filterSupplierList: List<DetailSupplierDataModel>
            if (filterResults.values != null && filterResults.count > 0) {
                filterSupplierList = filterResults.values as List<DetailSupplierDataModel>
                val reportsSearchUi = ReportsSearchUi()
                reportsSearchUi.isHeader = true
                reportsSearchUi.header =
                    resources.getString(R.string.txt_report_search_supplier)
                lstSearchData = ArrayList()
                lstSearchData!!.add(reportsSearchUi)
                for (i in filterSupplierList.indices) {
                    val reportsSearchUi1 = ReportsSearchUi()
                    reportsSearchUi1.detailSupplierDataModel = filterSupplierList[i]
                    lstSearchData!!.add(reportsSearchUi1)
                }
            } else {
                lstSearchData = ArrayList()
            }
        }
    }

    private inner class FilterProducts : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null && constraint.length > 0) {
                searchedString = constraint as String
                val tempList = ArrayList<Products>()
                // search content in friend list
                for (i in skuList!!.indices) {
                    if (skuList!![i].productName?.lowercase(Locale.getDefault())!!.contains(
                            constraint.toString().lowercase(
                                Locale.getDefault()
                            )
                        )
                    ) {
                        tempList.add(skuList!![i])
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
            } else {
                searchedString = ""
                filterResults.count = skuList!!.size
                filterResults.values = skuList
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, filterResults: FilterResults) {
            val filterSkuList: List<Products>
            if (filterResults.values != null && filterResults.count > 0) {
                filterSkuList = filterResults.values as List<Products>
                val reportsSearchUi = ReportsSearchUi()
                reportsSearchUi.isHeader = true
                reportsSearchUi.header = resources.getString(R.string.txt_report_search_sku)
                lstSearchData!!.add(reportsSearchUi)
                for (i in filterSkuList.indices) {
                    val reportsSearchUi1 = ReportsSearchUi()
                    reportsSearchUi1.products = filterSkuList[i]
                    lstSearchData!!.add(reportsSearchUi1)
                }
            }
            if (lstSearchData != null && lstSearchData!!.size > 0) {
                lstSupplier!!.adapter = ReportsSearchSupplierListAdapter(
                    this@ReportSearchActivity,
                    lstSearchData!!,
                    object : ItemClickListener {
                        override fun onItemClick(item: String?, supplierName: String?) {
                            if (supplierName != null) {
                                SetRecentSearchList(supplierName)
                            }
                            val newIntent = Intent(
                                this@ReportSearchActivity,
                                ReportsSpendingDetailsActivity::class.java
                            )
                            newIntent.putExtra(ReportApi.START_DATE, selectedStartDate)
                            newIntent.putExtra(ReportApi.END_DATE, selectedEndDate)
                            newIntent.putExtra(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR, item)
                            newIntent.putExtra(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
                            newIntent.putExtra(ReportApi.SKU_SUPPLIER_NAME, supplierName)
                            newIntent.putExtra(
                                ReportApi.CALLED_FROM_FRAGMENT,
                                ReportApi.REPORT_SEARCH_ACTIVITY
                            )
                            startActivity(newIntent)
                        }

                        override fun onSKUItemClick(item: String?, SKUName: String?) {
                            if (SKUName != null) {
                                SetRecentSearchList(SKUName)
                            }
                            val newIntent = Intent(
                                this@ReportSearchActivity,
                                ReportsSpendingBySKUActivity::class.java
                            )
                            newIntent.putExtra(ReportApi.START_DATE, selectedStartDate)
                            newIntent.putExtra(ReportApi.END_DATE, selectedEndDate)
                            newIntent.putExtra(ReportApi.TOTAL_SPENDING_DETAILS_CALLED_FOR, item)
                            newIntent.putExtra(ReportApi.SPENDING_DATA_PERIOD, spendingTimePeriod)
                            newIntent.putExtra(ReportApi.SKU_NAME, SKUName)
                            newIntent.putExtra(
                                ReportApi.CALLED_FROM_FRAGMENT,
                                ReportApi.REPORT_SEARCH_ACTIVITY
                            )
                            startActivity(newIntent)
                        }
                    })
            }
            setReportSearchLayout()
        }
    }
}