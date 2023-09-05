package zeemart.asia.buyers.invoices

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.adapter.InvoiceListingAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.pagination.InvoiceScrollHelper
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.EditInvoiceListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.invoices.filter.FilterInvoiceActivity
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import zeemart.asia.buyers.models.invoiceimport.EInvoicesGroupBySupplier
import zeemart.asia.buyers.models.invoiceimportimport.InvoiceRejectProcessedMgr
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr
import zeemart.asia.buyers.models.orderimport.OrdersWithPagination
import zeemart.asia.buyers.models.paymentimport.PaymentCardDetails
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.network.InvoiceHelper.GetInvoices
import zeemart.asia.buyers.network.InvoiceHelper.invoiceCountbyStatus
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity
import java.util.*

/**
 * Created by ParulBhandari on 7/2/2018.
 */
class InvoicesFragment : Fragment(), View.OnClickListener, EditInvoiceListener {
    private lateinit var deliveries_txt_heading: TextView
    private var deliveries_txt_heading_arrow: ImageView? = null
    private lateinit var btnNewInvoice: RelativeLayout
    private var txtUpload: TextView? = null
    private lateinit var listInvoices: RecyclerView
    private val headerItemDecoration: HeaderItemDecoration? = null
    private val invoiceRejectedList: MutableList<InvoiceUploadsListDataMgr> =
        Collections.synchronizedList<InvoiceUploadsListDataMgr>(
            ArrayList<InvoiceUploadsListDataMgr>()
        )
    private var listInvoicesNoItem: RelativeLayout? = null
    private var imgNoInvoices: ImageView? = null
    private var invoicesNoInvoicesText: TextView? = null
    private var invoicesNoInvoicesSubText: TextView? = null
    private lateinit var imgCloseSearch: ImageView
    private var isFragmentAttached = false
    private var threeDotLoaderBlue: CustomLoadingViewBlue? = null
    private lateinit var btnFilter: ImageView
    private lateinit var lytNoSearchResults: RelativeLayout
    private var pageNumber = 0
    private var filterCounter = 0
    private var invoiceDataManager: InvoiceDataManager? = null
    private var totalNumberOfPages = 1
    private var searchString: String? = null
    private var isFilterApplied = false
    private var isCardLinked = false
    private var txtSearchNoResult: TextView? = null
    private lateinit var txtSelectedFilterCount: TextView
    private lateinit var swipeRefreshLayoutInvoicesProcessed: SwipeRefreshLayout
    private var txtNoFilterResult: TextView? = null
    private var txtNoFilterResultDesc: TextView? = null
    private lateinit var txtPaymentDueHeader: TextView
    private var txtPaymentDueAmount: TextView? = null
    private lateinit var lytPaymentDue: CardView
    private var rejectedFailedInvoiceCount = 0
    var lstSuppliers: List<DetailSupplierDataModel>? = null
    private var lstRejectedProcessedData: MutableList<InvoiceRejectProcessedMgr>? =
        ArrayList<InvoiceRejectProcessedMgr>()
    private lateinit var lytUploadNew: RelativeLayout
    private var txtUploadNew: TextView? = null
    private var txtNoInvoicesCount: TextView? = null
    private lateinit var btnUploadNew: LinearLayout
    private var isPast7daysDisplayed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (isFragmentAttached) {
            val defaultOutlet: Outlet = SharedPref.defaultOutlet!!
            if (defaultOutlet != null) {
                deliveries_txt_heading?.setText(defaultOutlet.outletName)
            } else {
                deliveries_txt_heading?.setText("")
            }
            reloadProcessed()
        }
    }

    fun loadInvoiceFragmentData() {
        if (isFragmentAttached) {
            if (UserPermission.HasViewProcessedInvoice()) {
                if (isFilterApplied) {
                    ApplyFilter()
                } else {
                    filterCounter = 0
                    searchString = null
                    DetailSupplierMgr.clearAllSupplierFilterData()
                    callGetSuppliers()
                    listInvoices?.setAdapter(null)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout_more_root for this fragment
        val v: View = inflater.inflate(R.layout.fragment_invoices, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        //uploads list views initialization
        txtNoFilterResult = v.findViewById<TextView>(R.id.invoice_no_search_result_text)
        txtNoFilterResultDesc = v.findViewById<TextView>(R.id.invoice_change_search_term_text)
        threeDotLoaderBlue =
            v.findViewById<CustomLoadingViewBlue>(R.id.spin_kit_loader_invoice_blue)
        invoiceDataManager = InvoiceDataManager(requireActivity())
        txtSelectedFilterCount =
            v.findViewById<TextView>(R.id.txt_number_of_selected_invoices_filters)
        txtSelectedFilterCount.setVisibility(View.GONE)
        deliveries_txt_heading = v.findViewById<TextView>(R.id.deliveries_txt_heading)
        deliveries_txt_heading_arrow = v.findViewById(R.id.deliveries_txt_heading_arrow)
        btnNewInvoice = v.findViewById<RelativeLayout>(R.id.btn_new_invoice)
        btnNewInvoice.setVisibility(View.GONE)
        txtUpload = v.findViewById<TextView>(R.id.txt_upload)
        btnNewInvoice.setOnClickListener(this)
        listInvoicesNoItem = v.findViewById<RelativeLayout>(R.id.invoices_no_item)
        imgNoInvoices = v.findViewById(R.id.img_no_invoices)
        txtSearchNoResult = v.findViewById<TextView>(R.id.txt_no_result)
        lytNoSearchResults = v.findViewById<RelativeLayout>(R.id.invoices_search_no_result)
        lytNoSearchResults.setVisibility(View.GONE)
        txtPaymentDueHeader = v.findViewById<TextView>(R.id.txt_payment_due_header)
        txtPaymentDueHeader.setText(resources.getString(R.string.txt_payable_online))
        txtPaymentDueAmount = v.findViewById<TextView>(R.id.txt_payment_due_amount)
        lytPaymentDue = v.findViewById<CardView>(R.id.lyt_payment_due)
        lytPaymentDue.setOnClickListener(this)
        lytPaymentDue.setVisibility(View.GONE)
        btnFilter = v.findViewById(R.id.img_filter_icon)
        btnFilter.setEnabled(false)
        btnFilter.setAlpha(0.5f)
        btnFilter.setOnClickListener(this)
        imgCloseSearch = v.findViewById(R.id.img_end_search)
        imgCloseSearch.setOnClickListener(this)
        imgCloseSearch.setVisibility(View.GONE)
        swipeRefreshLayoutInvoicesProcessed =
            v.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_invoices_processed)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayoutInvoicesProcessed)
        swipeRefreshLayoutInvoicesProcessed.setOnRefreshListener(object :
            SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                reloadProcessed()
                if (isFragmentAttached) invoicesCountByStatus()
                swipeRefreshLayoutInvoicesProcessed.setRefreshing(false)
            }
        })
        invoicesNoInvoicesText = v.findViewById<TextView>(R.id.invoices_no_invoices_text)
        invoicesNoInvoicesSubText = v.findViewById<TextView>(R.id.no_invoices_sub)
        displayNoInvoiceMessage(false)
        listInvoices = v.findViewById<RecyclerView>(R.id.invoices_list)
        val linearLayoutManager = LinearLayoutManager(activity)
        listInvoices.setLayoutManager(linearLayoutManager)
        InvoiceScrollHelper(
            activity,
            listInvoices,
            linearLayoutManager,
            object : InvoiceScrollHelper.InvoiceScrollCallback {
                override fun isHidePaymentDue(isHide: Boolean) {
                    val animate =
                        TranslateAnimation(0f, 0f, -lytPaymentDue.getHeight().toFloat(), 0f)
                    animate.setDuration(500)
                    animate.setFillAfter(true)
                    animate.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {
                            enableDisableSwipeRefresh(false)
                        }

                        override fun onAnimationEnd(animation: Animation) {
                            enableDisableSwipeRefresh(true)
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                    if (isHide) {
                        enableDisableSwipeRefresh(false)
                        lytPaymentDue.setAnimation(null)
                        lytPaymentDue.setVisibility(View.GONE)
                    } else {
                        if (!isFilterApplied) {
                            if (lytPaymentDue.getVisibility() == View.GONE) {
                                lytPaymentDue.startAnimation(animate)
                                if (isCardLinked) lytPaymentDue.setVisibility(View.VISIBLE)
                            }
                        } else {
                            enableDisableSwipeRefresh(true)
                            lytPaymentDue.setAnimation(null)
                            lytPaymentDue.setVisibility(View.GONE)
                        }
                    }
                }

                override fun loadMore() {
                    processedInvoiceData
                }
            }).setOnScrollListener()
        lytUploadNew = v.findViewById<RelativeLayout>(R.id.btn_invoice_not_count)
        lytUploadNew.setVisibility(View.GONE)
        btnUploadNew = v.findViewById<LinearLayout>(R.id.lyt_upload_new_count)
        txtNoInvoicesCount = v.findViewById<TextView>(R.id.txt_invoice_not_uploaded_count)
        txtUploadNew = v.findViewById<TextView>(R.id.txt_upload1)
        btnUploadNew.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_INVOICES_ADD_INVOICE)
            val newIntent = Intent(activity, UploadInvoiceForOrders::class.java)
            startActivity(newIntent)
        })
        setFont()
        deliveries_txt_heading.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(activity, SelectOutletActivity::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_INVOICE
            )
            startActivityForResult(newIntent, ZeemartAppConstants.RequestCode.SelectOutletActivity)
        })
        setProcessedTabActive()
        initUploadInvoiceSwipe()
        return v
    }

    private fun enableDisableSwipeRefresh(enable: Boolean) {
        if (swipeRefreshLayoutInvoicesProcessed != null) {
            swipeRefreshLayoutInvoicesProcessed.setEnabled(enable)
        }
    }

    override fun retryUploadOfFailedInvoices(
        failedInvoice: List<InQueueForUploadDataModel?>?,
        retryInvoicePosition: Int,
    ) {
        TODO("Not yet implemented")
    }

//     fun retryUploadOfFailedInvoices(
//         failedInvoice: List<InQueueForUploadDataModel>,
//         retryInvoicePosition: Int,
//     ) {
//    }

//     fun getSelectedInvoicesDeleteOrMerge(selectedInvoices: List<InvoiceUploadsListDataMgr>) {}
//    override fun retryUploadOfFailedInvoices(
//        failedInvoice: List<InQueueForUploadDataModel?>?,
//        retryInvoicePosition: Int,
//    ) {
//        TODO("Not yet implemented")
//    }

    override fun getSelectedInvoicesDeleteOrMerge(selectedInvoices: List<InvoiceUploadsListDataMgr?>?) {
        TODO("Not yet implemented")
    }

    override fun invoicesCountByStatus() {
        rejectedFailedInvoiceCount = 0
        invoiceDataManager = InvoiceDataManager(requireActivity())
        val pendingForUploadInvoices =
            SharedPref.defaultOutlet?.outletId?.let {
                invoiceDataManager!!.getAlltheInQueueInvoiceList(
                    it
                )
            }
        rejectedFailedInvoiceCount =
            if (pendingForUploadInvoices != null && pendingForUploadInvoices.size > 0) {
                pendingForUploadInvoices.size
            } else {
                0
            }
        val invoiceStatuses: String = Invoice.Status.REJECTED.toString()
        invoiceCountbyStatus(activity, invoiceStatuses, object : GetInvoices {
            override fun result(invoices: List<Invoice>?) {
                if (invoices != null && invoices.size > 0) {
                    for (i in invoices.indices) {
                        val rejectedInvoiceCount = Integer.toString(invoices[i].count!!)
                        if (invoices[i].isStatus(Invoice.Status.REJECTED) && !StringHelper.isStringNullOrEmpty(
                                rejectedInvoiceCount
                            )
                        ) {
                            rejectedFailedInvoiceCount =
                                rejectedFailedInvoiceCount + invoices[i].count!!
                        }
                    }
                } else {
                    BaseNavigationActivity.updateInvoiceCount(
                        activity, rejectedFailedInvoiceCount
                    )


                }
                if (activity != null) {
                    BaseNavigationActivity.updateInvoiceCount(
                        activity,
                        rejectedFailedInvoiceCount
                    )
                }
            }
        })
    }

    override fun onEditButtonClicked() {}
    override fun onListItemLongPressed() {}
    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            deliveries_txt_heading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSearchNoResult,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSearchNoResult,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSearchNoResult,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoFilterResult,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoFilterResultDesc,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPaymentDueHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPaymentDueAmount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpload,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUploadNew,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoInvoicesCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            invoicesNoInvoicesText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            invoicesNoInvoicesSubText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    /**
     * suppliers are fetched for filters only on first load
     */
    private fun callGetSuppliers() {
        val apiParamsHelper = ApiParamsHelper()
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        MarketListApi.retrieveSupplierListNew(
            activity,
            apiParamsHelper,
            outlets,
            object : SupplierListResponseListener {
                override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                    if (supplierList != null && supplierList.size > 0) {
                        val detailSupplierList: List<*> =
                            ArrayList<DetailSupplierDataModel>(supplierList)
                        lstSuppliers = ArrayList<DetailSupplierDataModel>(supplierList)
                        DetailSupplierMgr.supplierFilters = detailSupplierList as MutableList<DetailSupplierMgr>
                    }
                    btnFilter!!.alpha = 1f
                    btnFilter!!.isEnabled = true
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {}
            })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_new_invoice -> {
                AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_UPLOAD_BYORDER_UPLOAD)
                //set the flash boolean true for edge detection
                SharedPref.writeBool(SharedPref.FLASH_INVOICE_UPLOAD, true)
                SharedPref.writeBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true)
                val newIntent = Intent(activity, UploadInvoiceForOrders::class.java)
                startActivity(newIntent)
            }
            R.id.img_filter_icon -> {
                val intent = Intent(activity, FilterInvoiceActivity::class.java)
                selectedCounter = DetailSupplierMgr.selectedSupplierFilter?.size!!
                if (searchString != null) {
                    intent.putExtra(ZeemartAppConstants.INVOICE_ID, searchString)
                }
                startActivityForResult(
                    intent,
                    ZeemartAppConstants.RequestCode.FilterInvoiceActivity
                )
            }
            R.id.img_end_search -> {
                if (isPast7daysDisplayed) {
                    btnNewInvoice.setVisibility(View.GONE)
                    lytUploadNew.setVisibility(View.VISIBLE)
                } else {
                    btnNewInvoice.setVisibility(View.VISIBLE)
                    lytUploadNew.setVisibility(View.GONE)
                }
                imgCloseSearch!!.visibility = View.GONE
                btnFilter!!.visibility = View.VISIBLE
                if (filterCounter > 0) {
                    txtSelectedFilterCount.setVisibility(View.VISIBLE)
                } else {
                    txtSelectedFilterCount.setVisibility(View.GONE)
                }
                CommonMethods.hideKeyboard(requireActivity())
            }
            R.id.lyt_payment_due -> {
                CommonMethods.avoidMultipleClicks(lytPaymentDue)
                val paymentIntent = Intent(activity, PayInvoicesActivity::class.java)
                startActivity(paymentIntent)
            }
            else -> {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ZeemartAppConstants.RequestCode.SelectOutletActivity && resultCode == Activity.RESULT_OK) {
            isFilterApplied = false
            DetailSupplierMgr.clearAllSupplierFilterData()
            btnFilter!!.isEnabled = false
            btnFilter!!.alpha = 0.5f
            callGetSuppliers()
            //refreshes the tabs when outlet is changed
            filterCounter = 0
            btnFilter!!.setImageResource(R.drawable.filtericon)
            btnFilter!!.visibility = View.VISIBLE
            txtSelectedFilterCount.setVisibility(View.GONE)
            imgCloseSearch!!.visibility = View.GONE
            listInvoices.setAdapter(null)
            setProcessedTabActive()
            listInvoices.setAdapter(null)
        }
        if (requestCode == ZeemartAppConstants.RequestCode.FilterInvoiceActivity && resultCode == ZeemartAppConstants.ResultCode.RESULT_OK) {
            ApplyFilter()
        }
    }

    private fun ApplyFilter() {
        searchString = SharedPref.read(ZeemartAppConstants.FILTER_INVOICE_ID, "")
        btnFilter!!.alpha = 1f
        btnFilter!!.isEnabled = true
        val supplierSelectedList: List<DetailSupplierMgr>? = DetailSupplierMgr.selectedSupplierFilter
        if (supplierSelectedList != null && supplierSelectedList.size > 0 || !StringHelper.isStringNullOrEmpty(
                searchString
            )
        ) {
            btnFilter!!.setImageResource(R.drawable.filteryellow)
            isFilterApplied = true
            setFilterSelectedCount()
        } else {
            btnFilter!!.setImageResource(R.drawable.filtericon)
            isFilterApplied = false
            setFilterSelectedCount()
        }
        listInvoices.setAdapter(null)
    }

    private fun callRejectedInvoicesApi() {
        InvoiceHelper.GetAllInvoicesForParticularOrder(activity, null, true,
            object :
            InvoiceHelper.GetInvoices {

                override fun result(invoices: List<Invoice>?) {
                    if (isFragmentAttached) {
                        threeDotLoaderBlue?.setVisibility(View.GONE)
                        invoiceRejectedList.clear()
                        if (invoices != null) {
                            val invoicePendingList: ArrayList<InvoiceMgr> =
                                ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<InvoiceMgr>>(
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(invoices).toString(),
                                    object : TypeToken<ArrayList<InvoiceMgr?>?>() {}.type
                                )
                            Collections.sort<InvoiceMgr>(invoicePendingList,
                                Invoice.TimeCreatedCompare()
                            )
                            invoiceRejectedList.addAll(
                                InvoiceUploadsListDataMgr.deserializeInvoiceMgrToInvoiceUploads(
                                    invoicePendingList
                                )
                            )
                            if (invoiceRejectedList.size > 0 && activity != null) {
                                invoiceRejectedList.add(
                                    0,
                                    InvoiceUploadsListDataMgr.getInstanceWithHeaderData(
                                        InvoiceUploadsListDataMgr.UploadListHeader.Rejected
                                    )
                                )
                            }
                            var count = 4
                            if (invoiceRejectedList.size < 4) {
                                count = invoiceRejectedList.size
                            }
                            lstRejectedProcessedData = ArrayList<InvoiceRejectProcessedMgr>()
                            for (i in 0 until count) {
                                val invoiceRejectProcessedMgr = InvoiceRejectProcessedMgr()
                                invoiceRejectProcessedMgr.invoiceUploadsListDataMgr = (
                                        invoiceRejectedList[i]
                                        )
                                lstRejectedProcessedData!!.add(invoiceRejectProcessedMgr)
                            }
                        }
                    }
                }
            })
    }

    private fun reloadProcessed() {
        threeDotLoaderBlue?.setVisibility(View.VISIBLE)
        lstRejectedProcessedData!!.clear()
        displayNoInvoiceMessage(false)
        pageNumber = 0
        totalNumberOfPages = 1
        callRejectedInvoicesApi()
        retrieveCompanyCardPaymentData()
    }

    private fun callInvoicesPast7daysApi() {
        val retrieveOrderRequestParameters = ApiParamsHelper()
        val calendar: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
        val todayDate = calendar.timeInMillis / 1000
        calendar.add(Calendar.DATE, -7)
        DateHelper.setCalendarEndOfDayTime(calendar)
        val previousDate = calendar.timeInMillis / 1000
        val outlet: Outlet = SharedPref.defaultOutlet!!
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        retrieveOrderRequestParameters.setDeliveryStartDate(previousDate)
        retrieveOrderRequestParameters.setDeliveryEndDate(todayDate)
        retrieveOrderRequestParameters.setIsInvoiced(false)
        retrieveOrderRequestParameters.setIsRejectedOrNonUploaded(true)
        RetrieveOrders(activity, object : RetrieveOrders.GetOrderDataListener {
            override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                if (isFragmentAttached) {
                    if (ordersData != null && ordersData.data
                            ?.orders != null && ordersData.data!!.orders?.size!! > 0
                    ) {
                        lytUploadNew.setVisibility(View.VISIBLE)
                        isPast7daysDisplayed = true
                        btnNewInvoice.setVisibility(View.GONE)
                        if (ordersData.data!!.orders?.size === 1) {
                            txtNoInvoicesCount?.setText(
                                java.lang.String.format(
                                    activity!!.getString(
                                        R.string.txt_invoice_not_uploaded
                                    ), ordersData.data!!.orders?.size!!
                                )
                            )
                        } else {
                            txtNoInvoicesCount?.setText(
                                java.lang.String.format(
                                    activity!!.getString(
                                        R.string.txt_invoices_not_uploaded
                                    ), ordersData.data!!.orders?.size!!
                                )
                            )
                        }
                    } else {
                        lytUploadNew.setVisibility(View.GONE)
                        btnNewInvoice.setVisibility(View.VISIBLE)
                        isPast7daysDisplayed = false
                    }
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                lytUploadNew.setVisibility(View.GONE)
                btnNewInvoice.setVisibility(View.VISIBLE)
                isPast7daysDisplayed = false
            }
        }, outlets, retrieveOrderRequestParameters).orders
    }

    private val processedInvoiceData: Unit
        private get() {
            if (pageNumber < totalNumberOfPages) {
                pageNumber = pageNumber + 1
                var supplierIdArray: Array<String?>? = null
                if (isFilterApplied) {
                    val supplierSelectedList: List<DetailSupplierMgr>? =
                        DetailSupplierMgr.selectedSupplierFilter
                    if (supplierSelectedList != null && supplierSelectedList.size > 0) {
                        supplierIdArray = arrayOfNulls(supplierSelectedList.size)
                        for (i in supplierSelectedList.indices) {
                            supplierIdArray[i] = supplierSelectedList[i].supplier.supplierId
                        }
                    }
                } else {
                    supplierIdArray = null
                    searchString = null
                }
                val outlets: MutableList<Outlet> = ArrayList<Outlet>()
                outlets.add(SharedPref.defaultOutlet!!)
                InvoiceHelper.GetAllProcessedInvoices(
                    activity,
                    outlets,
                    object : InvoiceHelper.GetInvoicesWithPagination {
                        override fun result(invoicesWithPaginationData: Invoice.InvoicesWithPaginationData?) {
                            if (isFragmentAttached && invoicesWithPaginationData != null && invoicesWithPaginationData.data != null) {
                                totalNumberOfPages = invoicesWithPaginationData.data!!.numberOfPages!!
                                if (totalNumberOfPages == 0 && lstRejectedProcessedData == null && lstRejectedProcessedData!!.size == 0) {
                                    listInvoices.setVisibility(View.GONE)
                                    lytPaymentDue.setVisibility(View.GONE)
                                    displayNoInvoiceMessage(true)
                                } else {
                                    displayNoInvoiceMessage(false)
                                }
                                if (isFilterApplied) {
                                    lytPaymentDue.setVisibility(View.GONE)
                                } else {
                                    if (isCardLinked) lytPaymentDue.setVisibility(View.VISIBLE)
                                }
                                if (invoicesWithPaginationData.data!!.invoices != null && invoicesWithPaginationData.data!!.invoices?.size!! > 0) {
                                    val invoiceMgrList: MutableList<InvoiceMgr> =
                                        ZeemartBuyerApp.gsonExposeExclusive.fromJson<MutableList<InvoiceMgr>>(
                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                invoicesWithPaginationData.data!!.invoices
                                            ).toString(),
                                            object : TypeToken<ArrayList<InvoiceMgr?>?>() {}.type
                                        )
                                    if (invoiceMgrList.size > 0) {
                                        var invoiceProcessedList: MutableList<InvoiceMgr> =
                                            ArrayList<InvoiceMgr>()
                                        invoiceProcessedList =
                                            invoiceDataManager!!.createListWithHeadersData(
                                                invoiceProcessedList,
                                                invoiceMgrList
                                            ) as MutableList<InvoiceMgr>
                                        listInvoices.setVisibility(View.VISIBLE)
                                        for (i in invoiceProcessedList.indices) {
                                            val invoiceRejectProcessedMgr =
                                                InvoiceRejectProcessedMgr()
                                            invoiceRejectProcessedMgr.invoiceMgr = (
                                                invoiceProcessedList[i]
                                            )
                                            lstRejectedProcessedData!!.add(invoiceRejectProcessedMgr)
                                        }
                                    }
                                }
                                val mAdapterProcessed: InvoiceListingAdapter
                                if (listInvoices.getAdapter() == null) {
                                    mAdapterProcessed = InvoiceListingAdapter(
                                        requireContext(),
                                        lstRejectedProcessedData,
                                        invoiceRejectedList,
                                        object : EditInvoiceListener {

                                            override fun retryUploadOfFailedInvoices(
                                                failedInvoice: List<InQueueForUploadDataModel?>?,
                                                retryInvoicePosition: Int,
                                            ) {
                                                TODO("Not yet implemented")
                                            }

                                            override fun getSelectedInvoicesDeleteOrMerge(
                                                selectedInvoices: List<InvoiceUploadsListDataMgr?>?,
                                            ) {
                                                if (isFragmentAttached) invoicesCountByStatus()
                                            }

                                            override fun invoicesCountByStatus() {}
                                            override fun onEditButtonClicked() {
                                                reloadProcessed()
                                                if (isFragmentAttached) this@InvoicesFragment.invoicesCountByStatus()
                                            }

                                            override fun onListItemLongPressed() {}
                                        })
                                    listInvoices.setAdapter(mAdapterProcessed)
                                } else {
                                    mAdapterProcessed =
                                        listInvoices.getAdapter() as InvoiceListingAdapter
                                    mAdapterProcessed.updateDataList(lstRejectedProcessedData)
                                    mAdapterProcessed.notifyDataSetChanged()
                                }
                                setProcessedTabActive()
                            }
                            callInvoicesPast7daysApi()
                        }
                    },
                    pageNumber,
                    searchString,
                    supplierIdArray
                )
            }
        }

    private fun retrieveCompanyCardPaymentData() {
        isCardLinked = false
        OutletsApi.getSpecificOutlet(activity, object :
            OutletsApi.GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                val apiParamsHelper = ApiParamsHelper()
                if (specificOutlet != null && specificOutlet.data != null && specificOutlet.data!!.company != null
                    && specificOutlet.data!!.company?.companyId != null) apiParamsHelper.setCompanyId(
                    specificOutlet.data!!.company?.companyId!!
                )
                PaymentApi.retrieveCompanyCardPaymentData(
                    context,
                    apiParamsHelper,
                    SharedPref.defaultOutlet!!,
                    object : PaymentApi.CompanyCardDetailsListener {
                        override fun onSuccessResponse(paymentCardDetails: PaymentCardDetails?) {
                            if (paymentCardDetails != null && paymentCardDetails.data != null) {
                                for (i in 0 until paymentCardDetails.data!!.size) {
                                    if (paymentCardDetails.data!!.get(i).isDefaultCard) {
                                        callPendingPaymentInvoicesForTotalAMount()
                                        isCardLinked = true
                                        break
                                    }
                                }
                            }
                            processedInvoiceData
                        }

                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                            val errorMessage: String? = error?.errorMessage
                            val errorType: String? = error?.errorType
                            if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                ZeemartBuyerApp.getToastRed(errorMessage)
                            }
                            processedInvoiceData
                        }
                    })
            }

            override fun onError(error: VolleyErrorHelper?) {
                val errorMessage: String? = error?.errorMessage
                val errorType: String? = error?.errorType
                if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                    ZeemartBuyerApp.getToastRed(errorMessage)
                }
                processedInvoiceData
            }
        })
    }

    private fun setProcessedTabActive() {
        swipeRefreshLayoutInvoicesProcessed.setEnabled(true)
        if (isFragmentAttached) {
            if (imgCloseSearch!!.visibility == View.VISIBLE) {
                btnFilter!!.visibility = View.GONE
                txtSelectedFilterCount.setVisibility(View.GONE)
            } else {
                btnFilter!!.visibility = View.VISIBLE
                if (filterCounter > 0) {
                    txtSelectedFilterCount.setVisibility(View.VISIBLE)
                } else {
                    txtSelectedFilterCount.setVisibility(View.GONE)
                }
            }
        }
        updateUiEmptyList()
    }

    private fun updateUiEmptyList() {
        if (isFragmentAttached && threeDotLoaderBlue?.getVisibility() == View.GONE) {
            if (lstRejectedProcessedData!!.size > 0) {
                listInvoices.setVisibility(View.VISIBLE)
                if (isFilterApplied) {
                    lytPaymentDue.setVisibility(View.GONE)
                } else {
                    if (isCardLinked) lytPaymentDue.setVisibility(View.VISIBLE)
                }
                displayNoInvoiceMessage(false)
            } else {
                listInvoices.setVisibility(View.GONE)
                lytPaymentDue.setVisibility(View.GONE)
                displayNoInvoiceMessage(true)
            }
        }
    }

    private fun setFilterSelectedCount() {
        filterCounter = selectedFiltersCount
        if (filterCounter != 0) {
            txtSelectedFilterCount.setVisibility(View.VISIBLE)
            txtSelectedFilterCount.setText(filterCounter.toString() + "")
        } else {
            txtSelectedFilterCount.setVisibility(View.GONE)
        }
    }

    private val selectedFiltersCount: Int
        private get() {
            var count = 0
            if (DetailSupplierMgr.selectedSupplierFilter != null && DetailSupplierMgr.selectedSupplierFilter!!.size > 0) count =
                count + 1
            if (!StringHelper.isStringNullOrEmpty(searchString)) count = count + 1
            return count
        }

    private fun initUploadInvoiceSwipe() {
        val uploadedInvoiceSwipeHelper: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                private val background: ColorDrawable = ColorDrawable()
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (listInvoices.getAdapter() != null && listInvoices.getAdapter() is InvoiceListingAdapter) {
                        val mAdapter: InvoiceListingAdapter =
                            listInvoices.getAdapter() as InvoiceListingAdapter
                        if (mAdapter != null) {
                            AnalyticsHelper.logAction(
                                activity,
                                AnalyticsHelper.SLIDE_ITEM_UPLOADS_INVOICES_DELETE
                            )
                            val deleteItemPosition: Int = viewHolder.getAdapterPosition()
                            if (lstRejectedProcessedData!![deleteItemPosition].invoiceUploadsListDataMgr != null
                                && lstRejectedProcessedData!![deleteItemPosition].invoiceUploadsListDataMgr
                                    ?.uploadedInvoice != null
                            ) {
                                createDeleteUploadedInvoiceDialog(mAdapter, deleteItemPosition)
                            }
                        }
                    }
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ): Int {
                    return if (listInvoices.getAdapter() != null && listInvoices.getAdapter() is InvoiceListingAdapter) {
                        val mAdapter: InvoiceListingAdapter =
                            listInvoices.getAdapter() as InvoiceListingAdapter
                        if (mAdapter != null) {
                            val currentItemPosition: Int = viewHolder.getAdapterPosition()
                            if (lstRejectedProcessedData!![currentItemPosition].invoiceUploadsListDataMgr != null
                                && lstRejectedProcessedData!![currentItemPosition].invoiceUploadsListDataMgr
                                    ?.uploadedInvoice != null
                            ) {
                                val uploadedInvoice: InvoiceMgr? =
                                    lstRejectedProcessedData!![currentItemPosition].invoiceUploadsListDataMgr
                                        ?.uploadedInvoice
                                //PNF-340 created by user check, removed as permissed to view invoice added.
                                //UserDetails createdBy = ((InvoiceMgr) inQueueAndUploadedList.get(currentItemPosition)).getCreatedBy();
                                if (uploadedInvoice?.isStatus(Invoice.Status.PENDING) == true || uploadedInvoice?.isStatus(
                                        Invoice.Status.REJECTED
                                    ) == true
                                ) {
                                    ItemTouchHelper.LEFT
                                } else {
                                    0
                                }
                            } else {
                                0
                            }
                        } else {
                            0
                        }
                    } else {
                        0
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean,
                ) {
                    if (listInvoices.getAdapter() != null && listInvoices.getAdapter() is InvoiceListingAdapter) {
                        val text = getString(R.string.txt_delete)
                        //InvoiceUploadsAdapter.ViewHolderItem mViewHolder = (InvoiceUploadsAdapter.ViewHolderItem) viewHolder;
                        val color = resources.getColor(R.color.pinky_red)
                        background.setColor(color)
                        background.setBounds(
                            viewHolder.itemView.getRight() + dX.toInt(),
                            viewHolder.itemView.getTop(),
                            viewHolder.itemView.getRight(),
                            viewHolder.itemView.getBottom()
                        )
                        background.draw(c)
                        val p = Paint()
                        val buttonWidth: Int = viewHolder.itemView.getRight() - SWIPE_BUTTON_WIDTH
                        val rightButton = RectF(
                            buttonWidth.toFloat(),
                            viewHolder.itemView.getTop().toFloat(),
                            viewHolder.itemView.getRight().toFloat(),
                            viewHolder.itemView.getBottom().toFloat()
                        )
                        p.color = color
                        c.drawRect(rightButton, p)
                        p.color = Color.WHITE
                        p.isAntiAlias = true
                        p.textSize = SWIPE_BUTTON_TEXT_SIZE.toFloat()
                        val textWidth = p.measureText(text)
                        val bmp: Bitmap = BitmapFactory.decodeResource(
                            activity!!.resources,
                            R.drawable.deletedraft
                        )
                        val bounds = Rect()
                        p.getTextBounds(text, 0, text.length, bounds)
                        val combinedHeight: Float =
                            (bmp.getHeight() + SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
                        c.drawBitmap(
                            bmp,
                            rightButton.centerX() - bmp.getWidth() / 2,
                            rightButton.centerY() - combinedHeight / 2,
                            null
                        )
                        c.drawText(
                            text,
                            rightButton.centerX() - textWidth / 2,
                            rightButton.centerY() + combinedHeight / 2,
                            p
                        )
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                }
            }
        val helperUploadInvoice = ItemTouchHelper(uploadedInvoiceSwipeHelper)
        helperUploadInvoice.attachToRecyclerView(listInvoices)
    }

    fun createDeleteUploadedInvoiceDialog(
        mAdapter: InvoiceListingAdapter, deleteItemPosition: Int,
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setPositiveButton(
            getString(R.string.txt_yes_delete),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    //remove the row from the adapter and notify the adapter call the API also to remove from the backend
                    val json: String = InvoiceHelper.createDeleteJsonForInvoice(
                        invoiceRejectedList[deleteItemPosition].uploadedInvoice?.invoiceId
                    )
                    val outlet: MutableList<Outlet> = ArrayList<Outlet>()
                    outlet.add(SharedPref.defaultOutlet!!)
                    InvoiceHelper.deleteUnprocessedInvoice(
                        activity,
                        outlet,
                        object : GetRequestStatusResponseListener {
                            override fun onSuccessResponse(status: String?) {
                                Log.d("", getString(R.string.txt_invoice_successfully_deleted))
                                reloadProcessed()
                                if (isFragmentAttached) invoicesCountByStatus()
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                ZeemartBuyerApp.getToastRed(activity!!.getString(R.string.txt_delete_draft_fail))
                            }
                        },
                        json
                    )

                    //Remove from the list and update the adapter
                    lstRejectedProcessedData!!.removeAt(deleteItemPosition)
                    invoiceRejectedList.removeAt(deleteItemPosition)
                    mAdapter.notifyDataChanged(
                        lstRejectedProcessedData!!,
                        true,
                        false,
                        false,
                        deleteItemPosition
                    )
                    dialog.dismiss()
                }
            })
        builder.setNegativeButton(
            getString(R.string.txt_cancel),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        val dialog = builder.create()
        dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                mAdapter.notifyDataChanged(
                    lstRejectedProcessedData!!,
                    false,
                    false,
                    true,
                    deleteItemPosition
                )
            }
        })
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle(getString(R.string.txt_delete_invoice))
        dialog.setMessage(getString(R.string.txt_want_to_delete_the_invoice))
        dialog.show()
        val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        deleteBtn.setTextColor(resources.getColor(R.color.chart_red))
        deleteBtn.isAllCaps = false
        val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.isAllCaps = false
    }

    private fun displayNoInvoiceMessage(showNoMessage: Boolean) {
        if (showNoMessage) {
            if (isFilterApplied) {
                lytPaymentDue.setVisibility(View.GONE)
                listInvoicesNoItem?.setVisibility(View.GONE)
                lytNoSearchResults.setVisibility(View.VISIBLE)
            } else {
                lytPaymentDue.setVisibility(View.GONE)
                lytNoSearchResults.setVisibility(View.GONE)
                listInvoicesNoItem?.setVisibility(View.VISIBLE)
                invoicesNoInvoicesSubText?.setVisibility(View.GONE)
                invoicesNoInvoicesText?.setText(resources.getString(R.string.txt_no_invoice_processed))
                imgNoInvoices!!.setImageResource(R.drawable.no_processed_invoices)
            }
        } else {
            lytNoSearchResults.setVisibility(View.GONE)
            listInvoicesNoItem?.setVisibility(View.GONE)
        }
    }

    private fun callPendingPaymentInvoicesForTotalAMount() {
        val lastToLastMonthRange: DateHelper.StartDateEndDate =
            DateHelper.getPaymentStartEndDate(DateHelper.PaymentDue.DEFAULT)
        val startDate: String = lastToLastMonthRange.startDateMonthYearString
        val endDate: String = lastToLastMonthRange.endDateMonthYearString
        val apiParamsHelper = ApiParamsHelper()
        val startDateLong: Long = DateHelper.returnEpochTimeSOD(startDate)
        val endDateLong: Long = DateHelper.returnEpochTimeEOD(endDate)
        apiParamsHelper.setDueStartDate(startDateLong)
        apiParamsHelper.setDueEndDate(endDateLong)
        apiParamsHelper.setManualPayment(true)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        InvoiceHelper.retrieveEInvoicesGroupBySupplier(
            context,
            apiParamsHelper,
            outlets,
            object : InvoiceHelper.GetEInvoicesWithPagination {
                override fun result(invoices: EInvoicesGroupBySupplier?) {
                    if (invoices != null && invoices.data
                            ?.invoicesGroupBySupplier != null && invoices.data!!
                            .invoicesGroupBySupplier?.size!! > 0
                    ) {

                        val overduePaymentAmount = invoices.data!!.overdue?.paymentAmount?.amount ?: 0.0
                        val upcomingPaymentAmount = invoices.data!!.upcoming?.paymentAmount?.amount ?: 0.0
                        val totalAmount: Double = overduePaymentAmount + upcomingPaymentAmount

                        val priceDetails = PriceDetails()
                        priceDetails.amount = totalAmount
                        txtPaymentDueAmount?.setText(priceDetails.displayValue)
                    } else {
                        txtPaymentDueAmount?.setText(PriceDetails().displayValue)
                    }
                    processedInvoiceData
                }
            })
    }

    companion object {
        var selectedCounter = 0
        private val SWIPE_BUTTON_WIDTH: Int = CommonMethods.dpToPx(70)
        private val SWIPE_BUTTON_TEXT_SIZE: Int = CommonMethods.dpToPx(14)
        private val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE: Int = CommonMethods.dpToPx(11)
        fun newInstance(): InvoicesFragment {
            return InvoicesFragment()
        }
    }
}