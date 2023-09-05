package zeemart.asia.buyers.network

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.yongchun.library.view.ImageSelectorActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.activities.BaseNavigationActivity.Companion.updateInvoiceCount
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper.IncludeFields
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.ECreditBySupplier
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.Invoice.*
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoiceimport.EInvoicesGroupBySupplier
import zeemart.asia.buyers.network.VolleyRequest.Companion.getInstance

/**
 * Created by saiful on 3/1/18.
 */
object InvoiceHelper {
    @JvmField
    val PICK_FROM_FILES = 1
    @JvmField
    val PICK_FROM_GALLERY = 2
    val PROCESSED_INVOICE_PAGE_SIZE = 50

    // api id - 27.11 Retrieve Processed Invoices
    @JvmStatic
    fun GetAllProcessedInvoices(
        context: Context?,
        outlets: List<Outlet>?,
        callback: GetInvoicesWithPagination,
        pageNumber: Int,
        searchText: String?,
        supplierIdArray: Array<String?>?,
    ) {
        var Uri = ServiceConstant.ENDPOINT_INVOICE_PROCESSED
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setPageSize(PROCESSED_INVOICE_PAGE_SIZE)
        apiParamsHelper.setPageNumber(pageNumber)
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.DESC)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.INVOICE_DATE)
        if (supplierIdArray != null && supplierIdArray.size > 0) {
            apiParamsHelper.setSupplierId(supplierIdArray)
        }
        val processedInvoiceIncludeFields = arrayOf(
            IncludeFields.PAYMENT_TERMS,
            IncludeFields.ISGUEST_INVOICE,
            IncludeFields.TOTAL_CHARGE,
            IncludeFields.INVOICE_TYPE,
            IncludeFields.PAYMENT_STATUS,
            IncludeFields.ORDER_DATA
        )
        apiParamsHelper.setIncludeFields(processedInvoiceIncludeFields as Array<ApiParamsHelper.IncludeFields?>?)
        apiParamsHelper.setRemoveSquareBrackets(true)
        if (!StringHelper.isStringNullOrEmpty(searchText)) {
            apiParamsHelper.setInvoiceNumSearchText(searchText!!)
        }
        Uri = apiParamsHelper.getUrl(Uri)
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            Response.Listener { response ->
                val invoicesWithPaginationData = ZeemartBuyerApp.fromJson(
                    response, InvoicesWithPaginationData::class.java
                )
                callback.result(invoicesWithPaginationData)
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    // api id - 27.3 Retrieve Pending Invoices
    fun GetAllPendingInvoices(context: Context?, outlets: List<Outlet>?, callback: GetInvoices) {
        var Uri = ServiceConstant.ENDPOINT_INVOICES_PENDING
        val apiParamsHelper = ApiParamsHelper()
        val pendingInvoiceIncludeFields =
            arrayOf(IncludeFields.CREATED_BY, IncludeFields.IMAGES, IncludeFields.REJECTED_REASON)
        apiParamsHelper.setIncludeFields(pendingInvoiceIncludeFields as Array<ApiParamsHelper.IncludeFields?>?)
        apiParamsHelper.setRemoveSquareBrackets(true)
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        apiParamsHelper.setSortField(ApiParamsHelper.SortField.INVOICE_DATE)
        Uri = apiParamsHelper.getUrl(Uri)
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {

                override fun onResponse(response: String?) {
                    val invoicesWithPaginationData = ZeemartBuyerApp.fromJson(
                        response, InvoicesWithPaginationData::class.java
                    )
                    if ((invoicesWithPaginationData != null) && (invoicesWithPaginationData.data != null) && (invoicesWithPaginationData.data!!.invoices != null)) {
                        callback.result(invoicesWithPaginationData.data!!.invoices)
                    } else {
                        callback.result(null)
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    @JvmStatic
    fun GetAllInvoicesForParticularOrder(
        context: Context?,
        orderIds: Array<String>?,
        isFoRejected: Boolean,
        callback: GetInvoices,
    ) {
        var Uri = ServiceConstant.ENDPOINT_INVOICES_LINKED_TO_ORDER
        val apiParamsHelper = ApiParamsHelper()
        val pendingInvoiceIncludeFields = arrayOf(
            IncludeFields.CREATED_BY,
            IncludeFields.IMAGES,
            IncludeFields.REJECTED_REASON,
            IncludeFields.SUPPLIER,
            IncludeFields.ORDER_IDS,
            IncludeFields.ORDER_DATA,
            IncludeFields.SUPPLIER
        )
        apiParamsHelper.setIncludeFields(pendingInvoiceIncludeFields as Array<ApiParamsHelper.IncludeFields?>?)
        apiParamsHelper.setRemoveSquareBrackets(true)
        if (isFoRejected) {
            apiParamsHelper.setInvoiceStatuses(arrayOf(Invoice.Status.REJECTED.toString()))
        } else {
            apiParamsHelper.setInvoiceStatuses(
                arrayOf(
                    Invoice.Status.REJECTED.toString(),
                    Invoice.Status.PENDING.toString(),
                    Invoice.Status.PROCESSING.toString()
                )
            )
            apiParamsHelper.setMultipleOrderIds(orderIds!!)
        }
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        apiParamsHelper.setSortField(ApiParamsHelper.SortField.INVOICE_DATE)
        Uri = apiParamsHelper.getUrl(Uri)
        val outlet: MutableList<Outlet> = ArrayList()
        outlet.add(SharedPref.defaultOutlet!!)
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {

                override fun onResponse(response: String?) {
                    Log.e("response", "onResponse: $response")
                    val invoicesWithPaginationData = ZeemartBuyerApp.fromJson(
                        response, InvoicesWithPaginationDataV1::class.java
                    )
                    if ((invoicesWithPaginationData != null) && (invoicesWithPaginationData.data != null) && (invoicesWithPaginationData.data!!.data != null)) {
                        callback.result(invoicesWithPaginationData.data!!.data)
                    } else {
                        val data: List<Invoice> = ArrayList()
                        callback.result(data)
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    val data: List<Invoice> = ArrayList()
                    callback.result(data)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    @JvmStatic
    fun GetAllPendingInvoicesV1(
        context: Context?,
        outlets: List<Outlet>?,
        isFoRejected: Boolean,
        callback: GetInvoices,
    ) {
        var Uri = ServiceConstant.ENDPOINT_INVOICES_PENDING_V1
        val apiParamsHelper = ApiParamsHelper()
        val pendingInvoiceIncludeFields = arrayOf(
            IncludeFields.CREATED_BY,
            IncludeFields.IMAGES,
            IncludeFields.REJECTED_REASON,
            IncludeFields.SUPPLIER,
            IncludeFields.ORDER_IDS
        )
        apiParamsHelper.setIncludeFields(pendingInvoiceIncludeFields as Array<ApiParamsHelper.IncludeFields?>?)
        apiParamsHelper.setRemoveSquareBrackets(true)
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        apiParamsHelper.setSortField(ApiParamsHelper.SortField.INVOICE_DATE)
        if (isFoRejected) {
            apiParamsHelper.setInvoiceStatuses(arrayOf(Invoice.Status.REJECTED.toString()))
        } else {
            apiParamsHelper.setLinkedToOrder(false)
            apiParamsHelper.setInvoiceStatuses(
                arrayOf(
                    Invoice.Status.REJECTED.toString(),
                    Invoice.Status.PENDING.toString(),
                    Invoice.Status.PROCESSING.toString()
                )
            )
        }
        Uri = apiParamsHelper.getUrl(Uri)
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {

                override fun onResponse(response: String?) {
                    val invoicesWithPaginationData = ZeemartBuyerApp.fromJson(
                        response, InvoicesWithPaginationDataV1::class.java
                    )
                    if ((invoicesWithPaginationData != null) && (invoicesWithPaginationData.data != null) && (invoicesWithPaginationData.data!!.data != null)) {
                        callback.result(invoicesWithPaginationData.data!!.data)
                    } else {
                        callback.result(null)
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    // api id - 27.4 Retrieve Specific Invoice
    @JvmStatic
    fun GetInvoice(
        context: Context?,
        outlets: List<Outlet>?,
        invoiceId: String?,
        callback: GetInvoice,
    ) {
        var Uri = ServiceConstant.ENDPOINT_INVOICE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setId(invoiceId!!)
        Uri = apiParamsHelper.getUrl(Uri)
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val invoiceDetailResponse = ZeemartBuyerApp.fromJson(
                        response, InvoiceDetailResponse::class.java
                    )
                    if (invoiceDetailResponse!!.status == ServiceConstant.STATUS_CODE_200_OK) {
                        callback.result(invoiceDetailResponse.data)
                    }
                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    fun GetRejectedInvoice(
        context: Context?,
        outlets: List<Outlet>?,
        invoiceId: String?,
        callback: GetRejectedInvoice,
    ) {
        var Uri = ServiceConstant.ENDPOINT_INVOICE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setId(invoiceId!!)
        Uri = apiParamsHelper.getUrl(Uri)
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val invoiceDetailResponse = ZeemartBuyerApp.fromJson(
                        response, InvoiceDetailResponse::class.java
                    )
                    if (invoiceDetailResponse!!.status == ServiceConstant.STATUS_CODE_200_OK) {
                        callback.result(invoiceDetailResponse.data)
                    }
                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    /**
     * Request to invoice count
     *
     * @param context
     * @param invoiceStatuses
     * @param callback
     */
    // api id - 27.19 Invoice Count by status
    @JvmStatic
    fun invoiceCountbyStatus(context: Context?, invoiceStatuses: String?, callback: GetInvoices) {
        var Uri = ServiceConstant.ENDPOINT_INVOICE_COUNT_BY_STATUS
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.invoiceStatuses = invoiceStatuses!!
        Uri = apiParamsHelper.getUrl(Uri)
        val outlet: MutableList<Outlet> = ArrayList()
        outlet.add(SharedPref.defaultOutlet!!)
        val invoiceCountByStatus = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val invoiceCountbyStatus =
                        ZeemartBuyerApp.fromJson(response, BaseResponse::class.java)
                    if (invoiceCountbyStatus != null && invoiceCountbyStatus.status == ServiceConstant.STATUS_CODE_200_OK) {
                        val invoiceCountbyStatusResponse = ZeemartBuyerApp.fromJson(
                            response, InvoiceCountbyStatusResponse::class.java
                        )
                        callback.result(invoiceCountbyStatusResponse!!.data!!)
                    }
                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(invoiceCountByStatus)
    }

    /**
     * Request to delete processed invoices
     *
     * @param context
     * @param invoiceId
     * @param callback
     */
    // api id - 27.14 Delete Processed Invoice
    @JvmStatic
    fun deleteProcessedInvoice(
        context: Context?,
        invoiceId: String?,
        callback: GetRequestStatusResponseListener,
    ) {
        val Uri = ServiceConstant.ENDPOINT_INVOICE_PROCESSED
        val deleteProcessedInvoiceRequest = createDeleteJsonForInvoice(invoiceId)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val deleteInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.DELETE,
            Uri,
            deleteProcessedInvoiceRequest,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val deleteMergeInvoiceStatus =
                        ZeemartBuyerApp.fromJson(response, BaseResponse::class.java)
                    if (deleteMergeInvoiceStatus!!.status == ServiceConstant.STATUS_CODE_200_OK) {
                        callback.onSuccessResponse("success")
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.onErrorResponse(error as VolleyErrorHelper)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(deleteInvoiceRequest)
    }

    // api id - 27.1 Create Invoice
    @JvmStatic
    @Synchronized
    fun CreateInvoice(context: Context?, invoiceCreate: Create?, callback: CreateInvoice) {
        val jsonRequestBody = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceCreate).toString()
        Log.e("CreateInvoice", "jsonRequestBody$jsonRequestBody")
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val request = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_INVOICE,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val responseData =
                        ZeemartBuyerApp.fromJson(response, Create.Response::class.java)
                    callback.result(responseData)
                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    callback.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(request)
    }

    @JvmStatic
    fun imagePickerDialog(activity: Activity, noOfImagesThatCanBeSelected: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.txt_select_photo_from)
        val options = arrayOf(
            activity.getString(R.string.txt_gallery),
            activity.getString(R.string.txt_documents)
        )
        builder.setItems(options, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                when (which) {
                    0 -> {
                        dialog.dismiss()
                        if (verifyStoragePermissions(activity, PICK_FROM_GALLERY)) {
                            imagePicker(PICK_FROM_GALLERY, activity, noOfImagesThatCanBeSelected)
                        }
                    }
                    1 -> {
                        dialog.dismiss()
                        if (verifyStoragePermissions(activity, PICK_FROM_FILES)) {
                            imagePicker(PICK_FROM_FILES, activity, noOfImagesThatCanBeSelected)
                        }
                    }
                    else -> {}
                }
            }
        })
        // Create the AlertDialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun imagePicker(source: Int, activity: Activity, noOfFilesThatCanBeSelected: Int) {
        if (source == PICK_FROM_FILES) {
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = ServiceConstant.FILE_TYPE_PDF
            chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activity.startActivityForResult(chooseFile, PICK_FROM_FILES)
        } else if (source == PICK_FROM_GALLERY) {
            ImageSelectorActivity.start(
                activity,
                noOfFilesThatCanBeSelected,
                1,
                false,
                false,
                false
            )
        }
    }

    private fun verifyStoragePermissions(activity: Activity, source: Int): Boolean {
        // Check if we have write permission
        val permission =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    source
                )
            }
            return false
        }
        return true
    }

    // api id - 27.7 Edit Pending Invoices
    open fun EditInvoice(context: Context, invoice: Invoice?, callback: EditInvoice) {
        val jsonRequestBody = ZeemartBuyerApp.gsonExposeExclusive.toJson(
            invoice,
            Invoice::class.java
        )
        Log.e("EditInvoice", "jsonRequestBody:$jsonRequestBody")
        val outlet: MutableList<Outlet> = java.util.ArrayList()
        outlet.add(SharedPref.defaultOutlet!!)
        val request = ZeemartAPIRequest(context,
            Request.Method.PUT,
            ServiceConstant.ENDPOINT_INVOICE_PENDING,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlet),
            { response ->
                Log.e("EditInvoice", "onResponse:$response")
                callback.result(null)
            }
        ) { error ->
            if (error.message != null) callback.result(error.message) else callback.result(
                context.getString(R.string.txt_server_error)
            )
        }
        getInstance(context)!!.addToRequestQueue(request)
    }

    // api id - 27.13 Delete UnProcessed Invoice
    @JvmStatic
    fun deleteUnprocessedInvoice(
        context: Context?,
        outlets: List<Outlet>?,
        listener: GetRequestStatusResponseListener,
        requestBody: String?,
    ) {
        val endpointUrl = ServiceConstant.ENDPOINT_INVOICES
        val deleteInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.DELETE,
            endpointUrl,
            requestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val deleteMergeInvoiceStatus =
                        ZeemartBuyerApp.fromJson(response, BaseResponse::class.java)
                    if (deleteMergeInvoiceStatus!!.status == ServiceConstant.STATUS_CODE_200_OK) {
                        listener.onSuccessResponse("success")
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error as VolleyErrorHelper)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(deleteInvoiceRequest)
    }

    // api id - 27.9 Merge Invoices
    @JvmStatic
    fun mergeInvoiceUnprocessed(
        context: Context?,
        outlets: List<Outlet>?,
        listener: GetRequestStatusResponseListener,
        requestBody: String?,
    ) {
        val endpointUrl = ServiceConstant.ENDPOINT_INVOICES + "/merge"
        val mergeInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            endpointUrl,
            requestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val deleteMergeInvoiceStatus =
                        ZeemartBuyerApp.fromJson(response, BaseResponse::class.java)
                    if (deleteMergeInvoiceStatus!!.status == ServiceConstant.STATUS_CODE_200_OK) {
                        listener.onSuccessResponse("success")
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error as VolleyErrorHelper)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(mergeInvoiceRequest)
    }

    @JvmStatic
    fun createDeleteJson(list: java.util.ArrayList<InvoiceMgr?>): String {
//      DeleteInvoiceRequestBody deleteInvoiceRequestBody = new DeleteInvoiceRequestBody();
        val invoiceIds = ArrayList<String>()
        for (i in list.indices) {
            if (list[i]?.isInvoiceSelected == true) {
                list[i]?.invoiceId?.let { invoiceIds.add(it) }
            }
        }
        val request = Invoice.Delete()
        request.deletedBy = SharedPref.currentUserDetail
        request.invoiceIds = invoiceIds
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(request)
    }

    @JvmStatic
    fun mergeInvoiceJson(list: java.util.ArrayList<InvoiceMgr?>): String {
        if (list.size > 1) {
            val mergeInvoiceObj = MergeInvoice()
            mergeInvoiceObj.mergedBy = SharedPref.currentUserDetail
            mergeInvoiceObj.mergeIntoInvoice = list.get(0)?.invoiceId
            val sublistOfInvoices =
                ArrayList<String>()
            for (i in 1 until list.size) {
                list[i]?.invoiceId?.let { sublistOfInvoices.add(it) }
            }
            mergeInvoiceObj.toBeMergeInvoices = sublistOfInvoices
            return ZeemartBuyerApp.gsonExposeExclusive.toJson(mergeInvoiceObj)
        }
        return ""
    }

    @JvmStatic
    fun createDeleteJsonForInvoice(invoiceId: String?): String {
        val invoiceIds = ArrayList<String?>()
        invoiceIds.add(invoiceId)
        val request = Invoice.Delete()
        request.deletedBy = SharedPref.currentUserDetail
        request.invoiceIds = invoiceIds as List<String>
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(request)
    }

    @JvmStatic
    fun setBottomNavigationRejectedInvoiceCount(context: Context?) {
        val invoiceStatuses = Invoice.Status.REJECTED.toString()
        invoiceCountbyStatus(context, invoiceStatuses, object : GetInvoices {
            override fun result(invoices: List<Invoice>?) {
                if (invoices != null && invoices.size > 0) {
                    val invoiceCountbyStatusResponse = InvoiceCountbyStatusResponse()
                    invoiceCountbyStatusResponse.data
                    for (i in invoices.indices) {
                        val rejectedInvoiceCount = invoices[i].count?.let { Integer.toString(it) }
                        if (invoices[i].isStatus(Invoice.Status.REJECTED) && !StringHelper.isStringNullOrEmpty(
                                rejectedInvoiceCount
                            )
                        ) {
                            invoices[i].count?.let { updateInvoiceCount(context, it) }
                        } else {
                            updateInvoiceCount(context, 0)
                        }
                    }
                } else {
                    updateInvoiceCount(context, 0)
                }
            }
        })
    }

    @JvmStatic
    fun retrieveEInvoicesGroupBySupplier(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outlets: List<Outlet>?,
        mListener: GetEInvoicesWithPagination,
    ) {
        val endpointUrl = ServiceConstant.ENDPOINT_E_INVOICE_GROUP_SUPPLIER
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(endpointUrl),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val eInvoicesWithPaginationData = ZeemartBuyerApp.fromJson(
                        response, EInvoicesGroupBySupplier::class.java
                    )
                    mListener.result(eInvoicesWithPaginationData)
                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mListener.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    @JvmStatic
    fun retrieveECreditBySupplier(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outlets: List<Outlet>?,
        mListener: GetECreditBySupplierListener,
    ) {
        val endpointUrl = ServiceConstant.ENDPOINT_E_CREDIT_SUPPLIER
        val getInvoiceRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(endpointUrl),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    val eCreditBySupplierWithPaginationData = ZeemartBuyerApp.fromJson(
                        response, ECreditBySupplier::class.java
                    )
                    mListener.result(eCreditBySupplierWithPaginationData)
                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mListener.result(null)
                }
            })
        VolleyRequest.getInstance(context)?.addToRequestQueue(getInvoiceRequest)
    }

    enum class FileType(val value: String) {
        PDF("pdf"), JPG("jpg"), JPEG("jpeg"), PNG("png");

    }

    interface GetInvoicesWithPagination {
        fun result(invoices: InvoicesWithPaginationData?)
    }

    interface GetInvoices {
        fun result(invoices: List<Invoice>?)
    }

    interface GetInvoice {
        fun result(invoice: Invoice?)
    }

    interface GetRejectedInvoice {
        fun result(invoicemgr: Invoice?)
    }

    interface CreateInvoice {
        fun result(response: Create.Response?)
    }

    interface EditInvoice {
        fun result(errorMsg: String?)
    }

    interface GetEInvoicesWithPagination {
        fun result(invoices: EInvoicesGroupBySupplier?)
    }

    interface GetECreditBySupplierListener {
        fun result(eCredit: ECreditBySupplier?)
    }
}