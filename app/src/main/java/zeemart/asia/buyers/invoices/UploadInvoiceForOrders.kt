package zeemart.asia.buyers.invoices

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.InvoiceOrderListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.pagination.InvoiceOrdersScrollHelper
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI
import zeemart.asia.buyers.models.orderimport.OrdersWithPagination
import zeemart.asia.buyers.models.orderimportimport.ListWithStickyHeaderUI
import zeemart.asia.buyers.network.*
import java.util.*

/**
 * Created by RajPrudhviMarella on 22/Dec/2021.
 */
class UploadInvoiceForOrders() : AppCompatActivity() {
    private var txtHeader: TextView? = null
    private var backButton: ImageView? = null
    private var threeDotLoaderBlue: CustomLoadingViewBlue? = null
    private var btnOptions: ImageView? = null
    private var lstOrders: RecyclerView? = null
    private var dl: Dialog? = null
    private var myClip: ClipData? = null
    private var myClipboard: ClipboardManager? = null
    private var lytOrdersLayoutManager: LinearLayoutManager? = null
    private var ordersScrollHelper: InvoiceOrdersScrollHelper? = null
    private var totalPageCount = 1
    private var lytNoOrders: RelativeLayout? = null
    private var txtNoOrdersText: TextView? = null
    private val invoiceOrderList: ArrayList<ListWithStickyHeaderUI?>? =
        ArrayList<ListWithStickyHeaderUI?>()
    private var mOrders: MutableList<Orders>? = null
    private var retrieveOrderRequestParameters: ApiParamsHelper? = null
    private var isFilterApplied = false
    private var isSortAppliedDeliveryDate = false
    private var isRejectOrdUploadApplied = false
    private var lytNoFilterResults: RelativeLayout? = null
    private var txtNoFilterResults: TextView? = null
    private var txtDeselectFilters: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var scrollView: HorizontalScrollView? = null
    private val dialogShownOnce = false
    private var lytSupplier: CardView? = null
    private var lytSortDate: CardView? = null
    private var lytRejectOrUpload: CardView? = null
    private var txtSupplierFilter: TextView? = null
    private var txtSortDateFilter: TextView? = null
    private var txtRejectOrUploadFilter: TextView? = null
    private var txtSupplierSelectedFilterCount: TextView? = null
    private lateinit var invoiceEmailId: String
    private val specificOutlet: SpecificOutlet? = null
    var lstSuppliers: List<DetailSupplierDataModel>? = ArrayList<DetailSupplierDataModel>()
    var lstSelectedSuppliers: List<DetailSupplierDataModel>? = null
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private var searchString: String? = ""
    private var edtSearchOrders: EditText? = null
    private var edtSearchClear: ImageView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_invoice_for_orders)
        pageCount = 0
        threeDotLoaderBlue = findViewById<CustomLoadingViewBlue>(R.id.spin_kit_loader_invoice_blue)
        txtHeader = findViewById<TextView>(R.id.txt_adjustment_record)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        backButton = findViewById<ImageView>(R.id.adjustment_record_back_btn)
        scrollView = findViewById<HorizontalScrollView>(R.id.scroll_view)
        backButton!!.setOnClickListener(View.OnClickListener { finish() })
        btnOptions = findViewById<ImageView>(R.id.order_detail_imageButtonOptions)
        dl = Dialog(this@UploadInvoiceForOrders, R.style.CustomDialogForTagsTheme)
        btnOptions!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                ShowDialog(dl!!, applicationContext, invoiceEmailId)
            }
        })
        lstOrders = findViewById<RecyclerView>(R.id.lst_past_deliveries)
        lytOrdersLayoutManager = LinearLayoutManager(this)
        lstOrders!!.layoutManager = lytOrdersLayoutManager
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_invoices_processed)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayout!!)
        swipeRefreshLayout!!.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                setInitialParamsForGetOrders()
                swipeRefreshLayout!!.isRefreshing = false
            }
        })
        ordersScrollHelper = InvoiceOrdersScrollHelper(
            this,
            lstOrders!!,
            lytOrdersLayoutManager!!,
            object : InvoiceOrdersScrollHelper.DeliveryScrollCallback {
                override fun isHideFilters(isHide: Boolean) {}
                override fun loadMore() {
                    if (pageCount < totalPageCount) {
                        callOrdersApi()
                    }
                }
            })
        threeDotLoaderWhite = findViewById<CustomLoadingViewWhite>(R.id.spin_kit_loader_white)
        threeDotLoaderWhite!!.setVisibility(View.GONE)
        ordersScrollHelper!!.setOnScrollListener()
        lytNoOrders = findViewById<RelativeLayout>(R.id.deliveries_lyt_no_deliveries)
        txtNoOrdersText = findViewById<TextView>(R.id.deliveries_no_deliveries_text)
        lytNoFilterResults = findViewById<RelativeLayout>(R.id.lyt_no_filter_results)
        txtDeselectFilters = findViewById<TextView>(R.id.txt_deselect_filters)
        txtNoFilterResults = findViewById<TextView>(R.id.txt_no_result)
        lytNoOrders!!.setVisibility(View.GONE)
        lytNoFilterResults!!.setVisibility(View.GONE)
        txtSupplierFilter = findViewById<TextView>(R.id.txt_supplier_filter)
        txtSortDateFilter = findViewById<TextView>(R.id.txt_sort_date)
        txtRejectOrUploadFilter = findViewById<TextView>(R.id.txt_reject_or_upload)
        txtSupplierSelectedFilterCount = findViewById<TextView>(R.id.txt_number_of_selected_filters)
        txtSupplierSelectedFilterCount!!.setVisibility(View.GONE)
        lytSupplier = findViewById<CardView>(R.id.lyt_supplier_filter)
        lytSupplier!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(
                    this@UploadInvoiceForOrders,
                    AnalyticsHelper.TAP_INVOICES_SUPPLIER_FILTER
                )
                DialogHelper.ShowFilterSupplierDialog(
                    this@UploadInvoiceForOrders,
                    lstSuppliers!!,
                    object : DialogHelper.InvoicesSupplierFilterItemClickListener {
                        override fun onFilterSelected(selectedSuppliers: List<DetailSupplierDataModel>?) {
                            lstSelectedSuppliers =
                                ArrayList<DetailSupplierDataModel>(selectedSuppliers)
                            if (lstSelectedSuppliers!!.size > 0) {
                                isFilterApplied = true
                                txtSupplierSelectedFilterCount!!.setVisibility(View.VISIBLE)
                                txtSupplierSelectedFilterCount!!.setText(lstSelectedSuppliers!!.size.toString() + "")
                                txtSupplierFilter!!.setTextColor(getResources().getColor(R.color.color_azul_two))
                            } else {
                                txtSupplierSelectedFilterCount!!.setVisibility(View.GONE)
                                isFilterApplied = false
                                txtSupplierFilter!!.setTextColor(getResources().getColor(R.color.black))
                            }
                            setInitialParamsForGetOrders()
                        }
                    })
            }
        })
        lytSortDate = findViewById<CardView>(R.id.lyt_card_sort_date)
        lytSortDate!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(
                    this@UploadInvoiceForOrders,
                    AnalyticsHelper.TAP_INVOICES_SORT_BY_FILTER
                )
                DialogHelper.ShowFilterSortByDialog(
                    this@UploadInvoiceForOrders,
                    isSortAppliedDeliveryDate,
                    object : DialogHelper.ProductListSelectedFilterItemClickListener {
                        override fun onFilterSelected(isDefaultSelected: Boolean) {
                            isSortAppliedDeliveryDate = isDefaultSelected
                            setFiltersUI()
                            setInitialParamsForGetOrders()
                        }
                    })
            }
        })
        lytRejectOrUpload = findViewById<CardView>(R.id.lyt_card_reject_or_upload)
        lytRejectOrUpload!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(
                    this@UploadInvoiceForOrders,
                    AnalyticsHelper.TAP_INVOICES_ONLY_REJECTED_UPLOADS_FILTER
                )
                DialogHelper.ShowFilterRejectedOrUploadedDialog(
                    this@UploadInvoiceForOrders,
                    isRejectOrdUploadApplied,
                    object : DialogHelper.ProductListSelectedFilterItemClickListener {
                        override fun onFilterSelected(isDefaultSelected: Boolean) {
                            isRejectOrdUploadApplied = isDefaultSelected
                            setFiltersUI()
                            setInitialParamsForGetOrders()
                        }
                    })
            }
        })
        edtSearchOrders = findViewById<EditText>(R.id.edit_search)
        edtSearchClear = findViewById<ImageView>(R.id.edit_search_clear)
        edtSearchClear!!.visibility = View.GONE
        edtSearchOrders!!.setHint(getResources().getString(R.string.content_orders_edit_search_orders_hint))
        edtSearchOrders!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText: String = edtSearchOrders!!.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear!!.visibility = View.VISIBLE
                } else {
                    edtSearchClear!!.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        edtSearchOrders!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setInitialParamsForGetOrders()
                    CommonMethods.hideKeyboard(this@UploadInvoiceForOrders)
                }
                return false
            }
        })
        edtSearchClear!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                edtSearchOrders!!.setText("")
                setInitialParamsForGetOrders()
            }
        })
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierFilter,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSortDateFilter,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRejectOrUploadFilter,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoFilterResults,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeselectFilters,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoOrdersText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierSelectedFilterCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            edtSearchOrders,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        isRejectOrdUploadApplied = true
        isSortAppliedDeliveryDate = true
        setFiltersUI()
        callapi()
    }

    protected override fun onResume() {
        super.onResume()
        if (lstSuppliers != null && lstSuppliers!!.size > 0) {
            setFiltersUI()
            setInitialParamsForGetOrders()
        } else {
            callGetSuppliers()
        }
    }

    private fun callGetSuppliers() {
        threeDotLoaderBlue!!.setVisibility(View.VISIBLE)
        val apiParamsHelper = ApiParamsHelper()
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet!!.outletId!!)
        MarketListApi.retrieveSupplierListNew(
            this@UploadInvoiceForOrders,
            apiParamsHelper,
            outlets,
            object : SupplierListResponseListener {
                override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                    if (supplierList != null && supplierList.size > 0) {
                        lstSuppliers = ArrayList<DetailSupplierDataModel>(supplierList)
                    }
                    if (lstSuppliers != null && lstSuppliers!!.size > 0) {
                        lytSupplier?.setClickable(true)
                    } else {
                        lytSupplier?.setClickable(false)
                    }
                    setInitialParamsForGetOrders()
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    setInitialParamsForGetOrders()
                }
            })
    }

    private fun setFiltersUI() {
        if (isSortAppliedDeliveryDate) {
            txtSortDateFilter?.setText(getResources().getString(R.string.txt_delivery_date))
        } else {
            txtSortDateFilter?.setText(getResources().getString(R.string.txt_order_date))
        }
        if (isRejectOrdUploadApplied) {
            txtRejectOrUploadFilter?.setText(getResources().getString(R.string.txt_rejected_or_uploaded))
        } else {
            txtRejectOrUploadFilter?.setText(getResources().getString(R.string.txt_all_orders))
        }
    }

    private fun enableDisableSwipeRefresh(enable: Boolean) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout!!.setEnabled(enable)
        }
    }

    private fun clearData() {
        mOrders = null
        invoiceOrderList!!.clear()
        ordersScrollHelper!!.updateScrollListener(lstOrders, lytOrdersLayoutManager)
    }

    private fun setInitialParamsForGetOrders() {
        lytNoOrders?.setVisibility(View.GONE)
        lytNoFilterResults?.setVisibility(View.GONE)
        threeDotLoaderBlue?.setVisibility(View.VISIBLE)
        pageCount = 0
        clearData()
        searchString = edtSearchOrders?.getText().toString()
        if ((searchString != null) && !StringHelper.isStringNullOrEmpty(searchString)
            && (searchString!!.trim { it <= ' ' }.length > 0)) {
            retrieveOrderRequestParameters?.setOrderIdText(searchString!!)
        }
        retrieveOrderRequestParameters = ApiParamsHelper()
        if (isFilterApplied) {
            setFiltersToAPI(retrieveOrderRequestParameters)
        }
        callOrdersApi()
    }

    private fun callOrdersApi() {
        //call the retrieve order API
        if (pageCount < totalPageCount) {
            pageCount = pageCount + 1
            retrieveOrderRequestParameters?.setPageSize(pageSize)
            retrieveOrderRequestParameters?.setPageNumber(pageCount)
            searchString = edtSearchOrders?.getText().toString()
            if (!StringHelper.isStringNullOrEmpty(searchString) && searchString!!.trim { it <= ' ' }.length > 0) {
                retrieveOrderRequestParameters?.setOrderIdText(searchString!!)
            }
            retrieveOrderRequestParameters?.setSortOrder(ApiParamsHelper.SortOrder.DESC_CAP)
            val calendar: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            DateHelper.setCalendarEndOfDayTime(calendar)
            val previousDate = calendar.timeInMillis / 1000
            if (isSortAppliedDeliveryDate) {
                retrieveOrderRequestParameters?.setSortBy(ApiParamsHelper.SortField.TIME_DELIVERED)
                retrieveOrderRequestParameters?.setDeliveryEndDate(previousDate)
                retrieveOrderRequestParameters?.setDeliveryStartDate(0)
            } else {
                retrieveOrderRequestParameters?.setSortBy(ApiParamsHelper.SortField.TIME_CREATED)
                retrieveOrderRequestParameters?.setOrderStartDate(0)
                retrieveOrderRequestParameters?.setOrderEndDate(previousDate)
            }
            if (isRejectOrdUploadApplied) {
                retrieveOrderRequestParameters?.setIsRejectedOrNonUploaded(true)
            }
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            if (isFilterApplied) {
                setFiltersToAPI(retrieveOrderRequestParameters)
            }
            outlets.add(SharedPref.defaultOutlet!!)
            retrieveOrderRequestParameters?.setOrderStatus(
                arrayOf<String>(
                    OrderStatus.PLACED.statusName,
                    OrderStatus.INVOICED.statusName
                )
            )
            RetrieveOrders(this, object : RetrieveOrders.GetOrderDataListener {
                override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                    threeDotLoaderBlue?.setVisibility(View.GONE)
                    if ((ordersData != null) && (ordersData.data
                            !!.orders != null) && (ordersData.data!!.orders?.size!! > 0)
                    ) {
                        totalPageCount = ordersData.data!!.numberOfPages!!
                        val orders: List<Orders> = ordersData.data!!.orders!!
                        val fetchedOrders: List<Orders> = ArrayList<Orders>(orders)
                        if (mOrders != null && mOrders!!.size > 0) {
                            mOrders!!.addAll(fetchedOrders)
                        } else {
                            mOrders = ArrayList<Orders>(orders)
                            invoiceOrderList!!.clear()
                        }
                        if (invoiceOrderList != null && invoiceOrderList.size > 0) {
                            for (i in invoiceOrderList.indices) {
                                if (invoiceOrderList[i]!!.isLoader) {
                                    invoiceOrderList.remove(invoiceOrderList[i])
                                }
                            }
                        }
                        setInvoiceOrdersAdapter(fetchedOrders)
                    } else {
                        setUIForInvoiceOrders()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderBlue?.setVisibility(View.GONE)
                    ZeemartBuyerApp.getToastRed(error?.message)
                }
            }, outlets, retrieveOrderRequestParameters!!).orders
        }
    }

    fun setInvoiceOrdersAdapter(receivedOrders: List<Orders>?) {
        if (receivedOrders != null && receivedOrders.size > 0) {
            for (i in receivedOrders.indices) {
                if ((receivedOrders[i].orderStatus == OrderStatus.PLACED.statusName)) {
                    val newListItem = ListWithStickyHeaderUI()
                    val headerViewOrders = HeaderViewOrderUI()
                    var date: String = ""
                    var day: String = ""
                    if (isSortAppliedDeliveryDate) {
                        date = DateHelper.getDateInDateMonthYearFormat(
                            receivedOrders[i].timeDelivered,
                            receivedOrders[i].outlet?.timeZoneFromOutlet
                        )
                        day = DateHelper.getDateIn3LetterDayFormat(
                            receivedOrders[i].timeDelivered,
                            receivedOrders[i].outlet?.timeZoneFromOutlet
                        )
                    } else {
                        date = DateHelper.getDateInDateMonthYearFormat(
                            receivedOrders[i].timeCompare,
                            receivedOrders[i].outlet?.timeZoneFromOutlet
                        )
                        day = DateHelper.getDateIn3LetterDayFormat(
                            receivedOrders[i].timeCompare,
                            receivedOrders[i].outlet?.timeZoneFromOutlet
                        )
                    }
                    headerViewOrders.date = (date)
                    headerViewOrders.day = (day)
                    createListWithHeadersData(
                        receivedOrders,
                        i,
                        newListItem,
                        headerViewOrders,
                        invoiceOrderList
                    )
                }
            }
        }
        if (invoiceOrderList != null) {
            val newListItemLoader = ListWithStickyHeaderUI()
            newListItemLoader.isLoader
            if (pageCount != totalPageCount) invoiceOrderList.add(newListItemLoader)
        }
        if (lstOrders?.getAdapter() == null) {
            val mAdapterPast =
                InvoiceOrderListAdapter(this, invoiceOrderList as ArrayList<ListWithStickyHeaderUI>, object :
                    InvoiceOrderListAdapter.InvoiceOrderClicked {
                    override fun onInvoiceOrderClicked(mOrder: Orders?) {
                        AnalyticsHelper.logAction(
                            this@UploadInvoiceForOrders,
                            AnalyticsHelper.TAP_ORDER_INVOICE_ITEM
                        )
                        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
                        val orderIds = arrayOf<String>(mOrder?.orderId!!)
                        InvoiceHelper.GetAllInvoicesForParticularOrder(
                            this@UploadInvoiceForOrders,
                            orderIds as Array<String>,
                            false,
                            object : InvoiceHelper.GetInvoices {
                               override fun result(invoices: List<Invoice>?) {
                                    threeDotLoaderWhite?.setVisibility(View.GONE)
                                    DialogHelper.ShowInvoiceLinkedToOrderDetailsDialog(
                                        this@UploadInvoiceForOrders,
                                        mOrder,
                                        invoices as MutableList<Invoice?>,
                                        object : DialogHelper.InvoiceOrderDeleted {
                                            override fun onDeleted() {
                                                setFiltersUI()
                                                setInitialParamsForGetOrders()
                                            }

                                            override fun OnSuccess() {
                                                setFiltersUI()
                                                setInitialParamsForGetOrders()
                                            }
                                        })
                                }
                            })
                    }
                })
            lstOrders?.setAdapter(mAdapterPast)
            lstOrders?.addItemDecoration(HeaderItemDecoration(lstOrders!!, mAdapterPast as HeaderItemDecoration.StickyHeaderInterface))
        } else {
            (lstOrders!!.getAdapter() as InvoiceOrderListAdapter).updateDataList(invoiceOrderList as ArrayList<ListWithStickyHeaderUI>)
        }
        setUIForInvoiceOrders()
    }

    private fun setUIForInvoiceOrders() {
        lstOrders?.setVisibility(View.VISIBLE)
        lytNoOrders?.setVisibility(View.GONE)
        lytNoFilterResults?.setVisibility(View.GONE)
        if (invoiceOrderList!!.size == 0) {
            lstOrders?.setVisibility(View.GONE)
            if (isFilterApplied || !StringHelper.isStringNullOrEmpty(
                    edtSearchOrders?.getText().toString()
                )
            ) {
                lytNoFilterResults?.setVisibility(View.VISIBLE)
                lytNoOrders?.setVisibility(View.GONE)
            } else {
                lytNoFilterResults?.setVisibility(View.GONE)
                lytNoOrders?.setVisibility(View.VISIBLE)
            }
        }
    }

    private fun createListWithHeadersData(
        orderList: List<Orders>,
        i: Int,
        newListItem: ListWithStickyHeaderUI,
        headerViewOrders: HeaderViewOrderUI,
        referencedList: ArrayList<ListWithStickyHeaderUI?>?
    ) {
        if (referencedList!!.size == 0) {
            saveHeaderAndOrder(orderList[i], newListItem, headerViewOrders, referencedList)
        } else {
            //if the last entry of the data list has same order date as that of the current order
            // then do not add a header element otherwise add a header element
            if (referencedList[referencedList.size - 1] != null) {
                if (referencedList[referencedList.size - 1]?.headerData?.date
                        .equals(headerViewOrders.date)
                ) {
                    newListItem.order = (orderList[i])
                    newListItem.headerData = (headerViewOrders)
                    newListItem.isHeader
                    referencedList.add(newListItem)
                } else {
                    saveHeaderAndOrder(orderList[i], newListItem, headerViewOrders, referencedList)
                }
            } else {
                if (referencedList[referencedList.size - 2]?.headerData?.date
                        .equals(headerViewOrders.date)
                ) {
                    newListItem.order = (orderList[i])
                    newListItem.headerData = (headerViewOrders)
                    newListItem.isHeader
                    referencedList.add(newListItem)
                } else {
                    saveHeaderAndOrder(orderList[i], newListItem, headerViewOrders, referencedList)
                }
            }
        }
    }

    private fun saveHeaderAndOrder(
        order: Orders,
        newListItem: ListWithStickyHeaderUI,
        headerViewOrders: HeaderViewOrderUI,
        referencedList: ArrayList<ListWithStickyHeaderUI?>?
    ) {
        newListItem.headerData = (headerViewOrders)
        newListItem.isHeader
        referencedList!!.add(newListItem)
        val orderDataEntry = ListWithStickyHeaderUI()
        orderDataEntry.isHeader
        orderDataEntry.headerData = (headerViewOrders)
        orderDataEntry.order = (order)
        referencedList.add(orderDataEntry)
    }

    private fun setFiltersToAPI(retrieveOrderRequestParameters: ApiParamsHelper?) {
        var supplierIdArray: Array<String?>? = null
        if (isFilterApplied) {
            if (lstSelectedSuppliers != null && lstSelectedSuppliers!!.size > 0) {
                supplierIdArray = arrayOfNulls(lstSelectedSuppliers!!.size)
                for (i in lstSelectedSuppliers!!.indices) {
                    supplierIdArray[i] = lstSelectedSuppliers!![i].supplier.supplierId
                }
            }
            retrieveOrderRequestParameters?.setSupplierId(supplierIdArray)
            retrieveOrderRequestParameters?.setRemoveSquareBrackets(true)
        }
    }

    fun showPopup() {
        val popup = PopupMenu(this, btnOptions)
        popup.menuInflater.inflate(R.menu.invoice_upload_menu, popup.menu)
        val popupMenu = popup.menu
        popupMenu.findItem(R.id.txt_upload_for_non_zm_orders).isVisible = true
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.txt_upload_for_non_zm_orders) {
                    AnalyticsHelper.logAction(
                        this@UploadInvoiceForOrders,
                        AnalyticsHelper.TAP_NON_ZM_ORDERS_INVOICES_MENU_OPTION
                    )
                    popup.dismiss()
                    val intent = Intent(
                        this@UploadInvoiceForOrders,
                        UploadInvoicesForNonZmOrders::class.java
                    )
                    startActivity(intent)
                    return true
                }
                return false
            }
        })
        popup.show()
    }

    fun ShowDialog(dialog: Dialog, context: Context?, emailId: String?) {
        if (dialog.isShowing) {
            return
        }
        dialog.setContentView(R.layout.custom_dialog_upload_invoice_email)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val txt_other_upload_options: TextView =
            dialog.findViewById<TextView>(R.id.txt_other_upload_options)
        val txt_upload_via_email: TextView =
            dialog.findViewById<TextView>(R.id.txt_upload_via_email)
        val txt_send_invoices_email: TextView =
            dialog.findViewById<TextView>(R.id.txt_send_invoices_email)
        val txt_email_show: TextView = dialog.findViewById<TextView>(R.id.txt_email_show)
        val txt_copy_email_address: TextView =
            dialog.findViewById<TextView>(R.id.txt_copy_email_address)
        val txt_other_uploads: TextView = dialog.findViewById<TextView>(R.id.txt_other_uploads)
        val txt_for_non_zm_order_uploads: TextView =
            dialog.findViewById<TextView>(R.id.txt_for_non_zm_order_uploads)
        val txt_copied_email: TextView = dialog.findViewById<TextView>(R.id.txt_copied_email)
        val image_tick = dialog.findViewById<ImageView>(R.id.image_tick)
        val linear_email_copied: LinearLayout =
            dialog.findViewById<LinearLayout>(R.id.linear_email_copied)
        val linear_other_uploads: LinearLayout =
            dialog.findViewById<LinearLayout>(R.id.linear_other_uploads)
        val linear_upload_via_email: LinearLayout =
            dialog.findViewById<LinearLayout>(R.id.linear_upload_via_email)
        val linear_email: LinearLayout = dialog.findViewById<LinearLayout>(R.id.linear_email)
        val linear_click_email: LinearLayout =
            dialog.findViewById<LinearLayout>(R.id.linear_click_email)
        val view = dialog.findViewById<View>(R.id.view_line)
        ZeemartBuyerApp.setTypefaceView(
            txt_other_upload_options,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_upload_via_email,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_send_invoices_email,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_for_non_zm_order_uploads,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_copied_email,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_other_uploads,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_email_show,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_copy_email_address,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        myClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        linear_email_copied.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                invoiceEmailId = txt_email_show.getText().toString()
                myClip = ClipData.newPlainText("", invoiceEmailId)
                myClipboard!!.setPrimaryClip(myClip!!)
                txt_copied_email.setVisibility(View.VISIBLE)
                image_tick.visibility = View.VISIBLE
                val thread: Thread = object : Thread() {
                    override fun run() {
                        try {
                            sleep(1500)
                        } catch (e: InterruptedException) {
                        }
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                txt_copied_email.setVisibility(View.GONE)
                                image_tick.visibility = View.GONE
                            }
                        })
                    }
                }
                thread.start()
            }
        })
        linear_email.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                dialog.dismiss()
                sendEmail()
            }
        })
        txt_email_show.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                dialog.dismiss()
                sendEmail()
            }
        })
        linear_click_email.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                sendEmail()
                dialog.dismiss()
            }
        })
        linear_other_uploads.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent =
                    Intent(this@UploadInvoiceForOrders, UploadInvoicesForNonZmOrders::class.java)
                startActivity(intent)
                dialog.dismiss()
            }
        })
        if (TextUtils.isEmpty(invoiceEmailId)) {
            linear_email.setVisibility(View.GONE)
        } else {
            txt_email_show.setText(invoiceEmailId)
            linear_email.setVisibility(View.VISIBLE)
        }
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
    }

    private fun callapi() {
        OutletsApi.getSpecificOutlet(
            getApplicationContext(),
            object : OutletsApi.GetSpecificOutletResponseListener {
                override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                    if ((specificOutlet != null) && (specificOutlet.data != null) && (specificOutlet.data!!.status != null)) {
                        invoiceEmailId = specificOutlet.data!!.invoiceEmailId!!
                    }
                }

                override fun onError(error: VolleyErrorHelper?) {}
            })
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$invoiceEmailId"))
        emailIntent.putExtra(
            Intent.EXTRA_TEXT,
            getApplicationContext().getString(R.string.email_title_subject)
        )
        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var pageCount = 0
        private val pageSize = 50
    }
}