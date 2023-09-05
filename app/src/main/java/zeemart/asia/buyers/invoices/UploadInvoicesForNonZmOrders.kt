package zeemart.asia.buyers.invoices

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.EditInvoiceListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.MultipleFileUploadRequest
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.Invoice.Create
import zeemart.asia.buyers.models.invoice.Invoice.TimeCreatedCompare
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr.Companion.deserializeInQueueToInvoiceUploads
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr.Companion.deserializeInvoiceMgrToInvoiceUploads
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr.Companion.getInstanceWithHeaderData
import zeemart.asia.buyers.modelsimport.MultiPartImageUploadResponse
import zeemart.asia.buyers.network.ImageUploadHelper.MultiPartImageUploaded
import zeemart.asia.buyers.network.ImageUploadHelper.UploadFileMultipart
import zeemart.asia.buyers.network.ImageUploadHelper.uploadMultipleFileMultipart
import zeemart.asia.buyers.network.InvoiceHelper.CreateInvoice
import zeemart.asia.buyers.network.InvoiceHelper.GetAllPendingInvoicesV1
import zeemart.asia.buyers.network.InvoiceHelper.GetInvoices
import zeemart.asia.buyers.network.InvoiceHelper.createDeleteJson
import zeemart.asia.buyers.network.InvoiceHelper.createDeleteJsonForInvoice
import zeemart.asia.buyers.network.InvoiceHelper.deleteUnprocessedInvoice
import zeemart.asia.buyers.network.InvoiceHelper.mergeInvoiceJson
import zeemart.asia.buyers.network.InvoiceHelper.mergeInvoiceUnprocessed
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.io.File
import java.util.*

/**
 * Created by RajPrudhviMarella on 22/Dec/2021.
 */
class UploadInvoicesForNonZmOrders : AppCompatActivity(), View.OnClickListener,
    EditInvoiceListener {
    private lateinit var txtHeader: TextView
    private lateinit var backButton: ImageView
    private lateinit var threeDotLoaderBlue: CustomLoadingViewBlue
    private lateinit var deliveries_txt_heading: TextView
    private lateinit var btnNewInvoice: RelativeLayout
    private lateinit var lyt_header_adjustment_record: RelativeLayout
    private lateinit var txtUpload: TextView
    private val inQueueAndUploadedList =
        Collections.synchronizedList(ArrayList<InvoiceUploadsListDataMgr>())
    private lateinit var listInvoicesNoItem: RelativeLayout
    private lateinit var imgNoInvoices: ImageView
    private lateinit var invoicesNoInvoicesText: TextView
    private lateinit var invoicesNoInvoicesSubText: TextView
    private lateinit var txtEditInvoice: TextView
    private lateinit var lytUploadsList: LinearLayout
    private lateinit var lstUploadTabList: RecyclerView
    private lateinit var invoiceUploadsAdapter: InvoiceInQueueUploadListAdapter
    private var isUploadInProgress = false
    private lateinit var lytMergeDelete: LinearLayout
    private lateinit var btnDelete: Button
    private lateinit var btnMerge: Button
    private lateinit var invoicesTobeEditedList: MutableList<InvoiceUploadsListDataMgr>
    private lateinit var invoiceDataManager: InvoiceDataManager
    private var stopUploading = false
    private lateinit var swipeRefreshLayoutInvoicesUploads: SwipeRefreshLayout
    private var isEditButtonClicked = false
    public override fun onDestroy() {
        super.onDestroy()
        stopUploading = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_invoices_for_non_zm_orders)
        threeDotLoaderBlue = findViewById(R.id.spin_kit_loader_invoice_blue)
        lyt_header_adjustment_record = findViewById(R.id.lyt_header_adjustment_record)
        txtHeader = findViewById(R.id.txt_adjustment_record)
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        backButton = findViewById(R.id.adjustment_record_back_btn)
        backButton.setOnClickListener(View.OnClickListener {
            if (isEditButtonClicked) {
                isEditButtonClicked = false
                btnNewInvoice!!.visibility = View.VISIBLE
                txtEditInvoice!!.visibility = View.GONE
                lytMergeDelete!!.visibility = View.GONE
                //notify the list to remove all check boxes
                if (lstUploadTabList!!.adapter != null) {
                    for (i in inQueueAndUploadedList.indices) {
                        if (inQueueAndUploadedList[i].uploadedInvoice != null) { // instanceof InvoiceMgr) {
                            inQueueAndUploadedList[i].uploadedInvoice!!.isInvoiceSelected = false
                        } else if (inQueueAndUploadedList[i].inQueueForUploadInvoice != null) { // instanceof InQueueForUploadDataModel) {
                            inQueueAndUploadedList[i].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                                false
                        }
                    }
                    if (invoicesTobeEditedList != null) invoicesTobeEditedList!!.clear()
                    (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.setDeleteMergeActiveInactive(
                        false
                    )
                }
            } else {
                finish()
            }
        })
        threeDotLoaderBlue = findViewById(R.id.spin_kit_loader_invoice_blue)
        invoiceDataManager = InvoiceDataManager(this)
        lytUploadsList = findViewById(R.id.scroll_uploads)
        lytUploadsList.setVisibility(View.GONE)
        lstUploadTabList = findViewById(R.id.inqueue_invoice_list)
        lstUploadTabList.setLayoutManager(LinearLayoutManager(this))
        lstUploadTabList.setNestedScrollingEnabled(false)
        lytMergeDelete = findViewById(R.id.lyt_del_merge_buttons)
        btnDelete = findViewById(R.id.btn_delete_invoice)
        btnDelete.setOnClickListener(this)
        btnMerge = findViewById(R.id.btn_merge_invoice)
        btnMerge.setOnClickListener(this)
        lytMergeDelete.setVisibility(View.GONE)
        btnDelete.setAlpha(0.5f)
        btnMerge.setAlpha(0.5f)
        btnDelete.setClickable(false)
        btnMerge.setClickable(false)
        deliveries_txt_heading = findViewById(R.id.deliveries_txt_heading)
        btnNewInvoice = findViewById(R.id.btn_new_invoice)
        txtUpload = findViewById(R.id.txt_upload)
        btnNewInvoice.setOnClickListener(this)
        listInvoicesNoItem = findViewById(R.id.invoices_no_item)
        imgNoInvoices = findViewById(R.id.img_no_invoices)
        txtEditInvoice = findViewById(R.id.txt_edit_invoice)
        txtEditInvoice.setVisibility(View.GONE)
        txtEditInvoice.setOnClickListener(this)
        swipeRefreshLayoutInvoicesUploads = findViewById(R.id.swipe_refresh_invoices_uploads)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayoutInvoicesUploads)
        swipeRefreshLayoutInvoicesUploads.setOnRefreshListener(OnRefreshListener {
            txtEditInvoice.setText(resources.getString(R.string.txt_edit))
            reloadUploads()
            swipeRefreshLayoutInvoicesUploads.setRefreshing(false)
        })
        invoicesNoInvoicesText = findViewById(R.id.invoices_no_invoices_text)
        invoicesNoInvoicesSubText = findViewById(R.id.no_invoices_sub)
        displayNoInvoiceMessage(false)
        setUploadsTabActive()
        setFont()
        reloadUploads()
    }

    private fun setFont() {
        setTypefaceView(
            deliveries_txt_heading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtUpload, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            invoicesNoInvoicesText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(btnMerge, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnDelete, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            invoicesNoInvoicesSubText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtEditInvoice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    override fun onBackPressed() {
        if (isEditButtonClicked) {
            isEditButtonClicked = false
            btnNewInvoice!!.visibility = View.VISIBLE
            txtEditInvoice!!.visibility = View.GONE
            lytMergeDelete!!.visibility = View.GONE
            //notify the list to remove all check boxes
            if (lstUploadTabList!!.adapter != null) {
                for (i in inQueueAndUploadedList.indices) {
                    if (inQueueAndUploadedList[i].uploadedInvoice != null) { // instanceof InvoiceMgr) {
                        inQueueAndUploadedList[i].uploadedInvoice!!.isInvoiceSelected = false
                    } else if (inQueueAndUploadedList[i].inQueueForUploadInvoice != null) { // instanceof InQueueForUploadDataModel) {
                        inQueueAndUploadedList[i].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                            false
                    }
                }
                if (invoicesTobeEditedList != null) invoicesTobeEditedList!!.clear()
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.setDeleteMergeActiveInactive(
                    false
                )
            }
        } else {
            finish()
        }
    }

    override fun retryUploadOfFailedInvoices(
        data: List<InQueueForUploadDataModel?>?,
        retryInvoicePosition: Int
    ) {
        if (!isUploadInProgress) {
            //add the invoice to the inQueue list and refresh the adapter
            //create a new InvoiceImageRotationModel to add to the upload list
            val inQueueForUploadDataModels: MutableList<InQueueForUploadDataModel> = ArrayList()
            for (i in data?.indices!!) {
                val retryInvoiceData = InQueueForUploadDataModel()
                retryInvoiceData.status = InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE
                retryInvoiceData.isInvoiceSelectedForDeleteOrMerge =
                    data[i]?.isInvoiceSelectedForDeleteOrMerge!!
                retryInvoiceData.objectInfoFilePath = data[i]?.objectInfoFilePath
                retryInvoiceData.isInvoiceSelectedIsImage = data[i]?.isInvoiceSelectedIsImage!!
                retryInvoiceData.imageFilePath = data[i]?.imageFilePath
                retryInvoiceData.imageDirectoryPath = data[i]?.imageDirectoryPath
                retryInvoiceData.rotation = data[i]?.rotation!!
                retryInvoiceData.serverImagePath = data[i]?.serverImagePath
                UpdateImageInvoiceStatusTask().execute(retryInvoiceData)
                inQueueForUploadDataModels.add(retryInvoiceData)
            }
            //now remove the data from the list and notify adapter
            inQueueAndUploadedList.removeAt(retryInvoicePosition)
            if (lstUploadTabList!!.adapter != null) {
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                    inQueueAndUploadedList,
                    true,
                    false,
                    false,
                    retryInvoicePosition
                )
            }
            if (inQueueAndUploadedList.size > 0 && inQueueAndUploadedList[0].isHeader(
                    InvoiceUploadsListDataMgr.UploadListHeader.InQueue
                )
            ) {
                //find the position from where we need to add new inqueue
                var posToAdd = 0
                for (i in inQueueAndUploadedList.indices) {
                    if (inQueueAndUploadedList[i].uploadedInvoice != null) {
                        posToAdd = i - 1
                        break
                    } else posToAdd = posToAdd + 1
                }
                val retryUploadInvoiceData = InvoiceUploadsListDataMgr()
                if (inQueueForUploadDataModels != null && inQueueForUploadDataModels.size > 0) {
                    if (inQueueForUploadDataModels.size == 1) {
                        retryUploadInvoiceData.inQueueForUploadInvoice =
                            inQueueForUploadDataModels[0]
                    } else {
                        retryUploadInvoiceData.inQueueForUploadMergedInvoice =
                            inQueueForUploadDataModels
                    }
                }
                inQueueAndUploadedList.add(posToAdd, retryUploadInvoiceData)
            }
            Companion.currentUploadPosition = this.currentUploadPosition
            createInvoiceCall(inQueueAndUploadedList)
        }
    }

    override fun getSelectedInvoicesDeleteOrMerge(totalUploadedInvoices: List<InvoiceUploadsListDataMgr?>?) {
        invoicesTobeEditedList = totalUploadedInvoices as MutableList<InvoiceUploadsListDataMgr>
        if (totalUploadedInvoices.size == 1) {
            btnDelete!!.alpha = 1f
            btnMerge!!.alpha = 0.5f
            lytMergeDelete!!.alpha = 1f
            btnDelete!!.isClickable = true
            btnMerge!!.isClickable = false
        } else if (totalUploadedInvoices.size > 1) {
            btnDelete!!.alpha = 1f
            btnMerge!!.alpha = 1f
            btnDelete!!.isClickable = true
            btnMerge!!.isClickable = true
            for (i in totalUploadedInvoices.indices) {
                if (totalUploadedInvoices[i].uploadedInvoice != null && totalUploadedInvoices[i].uploadedInvoice!!.isStatus(
                        Invoice.Status.REJECTED
                    )
                ) {
                    btnDelete!!.alpha = 1f
                    btnMerge!!.alpha = 0.5f
                    lytMergeDelete!!.alpha = 1f
                    btnDelete!!.isClickable = true
                    btnMerge!!.isClickable = false
                    break
                }
            }
        } else if (totalUploadedInvoices.size == 0) {
            btnDelete!!.alpha = 0.5f
            btnMerge!!.alpha = 0.5f
            btnDelete!!.isClickable = false
            btnMerge!!.isClickable = false
        } else {
            btnDelete!!.alpha = 0.5f
            btnMerge!!.alpha = 0.5f
            btnDelete!!.isClickable = false
            btnMerge!!.isClickable = false
        }
    }

    override fun invoicesCountByStatus() {}
    override fun onEditButtonClicked() {
        onEditInvoicesClicked()
    }

    override fun onListItemLongPressed() {
        onEditInvoicesClicked()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_new_invoice -> {
                AnalyticsHelper.logAction(
                    this@UploadInvoicesForNonZmOrders,
                    AnalyticsHelper.TAP_UPLOAD_NONZM_UPLOAD
                )
                //set the flash boolean true for edge detection
                SharedPref.writeBool(SharedPref.FLASH_INVOICE_UPLOAD, true)
                SharedPref.writeBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true)
                val newIntent =
                    Intent(this@UploadInvoicesForNonZmOrders, InvoiceCameraPreview::class.java)
                startActivity(newIntent)
            }
            R.id.txt_edit_invoice -> {
                isEditButtonClicked = false
                btnNewInvoice!!.visibility = View.VISIBLE
                txtEditInvoice!!.visibility = View.GONE
                lytMergeDelete!!.visibility = View.GONE
                //notify the list to remove all check boxes
                if (lstUploadTabList!!.adapter != null) {
                    var i = 0
                    while (i < inQueueAndUploadedList.size) {
                        if (inQueueAndUploadedList[i].uploadedInvoice != null) { // instanceof InvoiceMgr) {
                            inQueueAndUploadedList[i].uploadedInvoice!!.isInvoiceSelected = false
                        } else if (inQueueAndUploadedList[i].inQueueForUploadInvoice != null) { // instanceof InQueueForUploadDataModel) {
                            inQueueAndUploadedList[i].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                                false
                        }
                        i++
                    }
                    if (invoicesTobeEditedList != null) invoicesTobeEditedList!!.clear()
                    (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.setDeleteMergeActiveInactive(
                        false
                    )
                }
            }
            R.id.btn_merge_invoice -> {
                AnalyticsHelper.logAction(
                    this@UploadInvoicesForNonZmOrders,
                    AnalyticsHelper.TAP_UPLOADS_INVOICES_EDIT_MERGE
                )
                createConfirmationDialog(false)
            }
            R.id.btn_delete_invoice -> {
                AnalyticsHelper.logAction(
                    this@UploadInvoicesForNonZmOrders,
                    AnalyticsHelper.TAP_UPLOADS_INVOICES_EDIT_DELETE
                )
                createConfirmationDialog(true)
            }
            else -> {}
        }
    }

    fun onEditInvoicesClicked() {
        isEditButtonClicked = true
        AnalyticsHelper.logAction(
            this@UploadInvoicesForNonZmOrders,
            AnalyticsHelper.TAP_UPLOADS_INVOICES_EDIT
        )
        txtEditInvoice!!.visibility = View.VISIBLE
        txtEditInvoice!!.text = resources.getString(R.string.txt_cancel)
        lytMergeDelete!!.visibility = View.VISIBLE
        btnNewInvoice!!.visibility = View.GONE
        btnDelete!!.alpha = 1f
        btnMerge!!.alpha = 0.5f
        btnDelete!!.isClickable = false
        btnMerge!!.isClickable = false
        for (i in inQueueAndUploadedList.indices) {
            if (inQueueAndUploadedList[i].uploadedInvoice != null) { // instanceof InvoiceMgr) {
                inQueueAndUploadedList[i].uploadedInvoice!!.isInvoiceSelected = false
            } else if (inQueueAndUploadedList[i].inQueueForUploadInvoice != null) { // instanceof InQueueForUploadDataModel) {
                inQueueAndUploadedList[i].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                    false
            }
        }

        //update the adapter to show the check boxes
        if (lstUploadTabList!!.adapter != null) {
            (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.setDeleteMergeActiveInactive(
                true
            )
        }
    }

    private fun reloadUploads() {
        displayNoInvoiceMessage(false)
        UploadInvoicesForNonZmOrders.currentUploadPosition = 1
        invoicesCountByStatus()
        //load all the invoices which could not be loaded
        val invoicesNotProcessedList =
            invoiceDataManager.getAlltheInQueueInvoiceList(SharedPref.defaultOutlet?.outletId!!)
        if (invoicesNotProcessedList != null && invoicesNotProcessedList.size > 0) {
            invoicesNotProcessedList.sortedWith(Comparator { p0, p1 -> p0?.status!!.compareTo(p1?.status!!) })
        }
        threeDotLoaderBlue.visibility = View.VISIBLE
        inQueueAndUploadedList.clear()
        val outlet: MutableList<Outlet?> = ArrayList()
        outlet.add(SharedPref.defaultOutlet)
        GetAllPendingInvoicesV1(
            this@UploadInvoicesForNonZmOrders,
            outlet as List<Outlet>?,
            false,
            object : GetInvoices {

                override fun result(invoices: List<Invoice>?) {
                    threeDotLoaderBlue.visibility = View.GONE
                    inQueueAndUploadedList.clear()
                    if (invoices != null) {
                        Log.e("GetAllPendingInvoices", "invoices.size()" + invoices.size)
                        val invoicePendingList =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<InvoiceMgr?>>(
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(invoices).toString(),
                                object : TypeToken<ArrayList<InvoiceMgr?>?>() {}.type
                            )
                        Collections.sort(invoicePendingList, TimeCreatedCompare())
                        inQueueAndUploadedList.addAll(deserializeInvoiceMgrToInvoiceUploads(invoicePendingList)
                        )
                        if (inQueueAndUploadedList.size > 0) {
                            inQueueAndUploadedList.add(
                                0,
                                getInstanceWithHeaderData(InvoiceUploadsListDataMgr.UploadListHeader.Uploaded)
                            )
                        }
                        stopUploading = false
                        uploadPendingInvoices(invoicesNotProcessedList)
                    } else {
                        setUploadsTabActive()
                    }
                }
            })
    }

    private fun setUploadsTabActive() {
        txtEditInvoice!!.visibility = View.GONE
        lytMergeDelete!!.visibility = View.GONE
        swipeRefreshLayoutInvoicesUploads!!.isEnabled = true
        if (invoicesTobeEditedList != null) invoicesTobeEditedList!!.clear()
        btnNewInvoice!!.visibility = View.VISIBLE
        lytUploadsList!!.visibility = View.VISIBLE
        if (inQueueAndUploadedList.size > 0) {
            txtEditInvoice!!.visibility = View.GONE
        }
        updateUiEmptyList()
    }

    private fun updateUiEmptyList() {
        if (threeDotLoaderBlue!!.visibility == View.GONE) {
            if (inQueueAndUploadedList.size > 0) {
                lstUploadTabList!!.visibility = View.VISIBLE
                displayNoInvoiceMessage(false)
            } else {
                lstUploadTabList!!.visibility = View.GONE
                displayNoInvoiceMessage(true)
            }
        }
    }

    private fun initUploadInvoiceSwipe() {
        val uploadedInvoiceSwipeHelper: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                private val background = ColorDrawable()
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (lstUploadTabList!!.adapter != null && lstUploadTabList!!.adapter is InvoiceInQueueUploadListAdapter) {
                        val mAdapter =
                            lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?
                        if (mAdapter != null) {
                            AnalyticsHelper.logAction(
                                this@UploadInvoicesForNonZmOrders,
                                AnalyticsHelper.SLIDE_ITEM_UPLOADS_INVOICES_DELETE
                            )
                            val deleteItemPosition = viewHolder.adapterPosition
                            if (inQueueAndUploadedList[deleteItemPosition].inQueueForUploadInvoice != null) {
                                //call the task to remove the invoice details from the internal memory
                                invoiceDataManager!!.deleteDirectory(File(inQueueAndUploadedList[deleteItemPosition].inQueueForUploadInvoice!!.imageDirectoryPath))
                                inQueueAndUploadedList.removeAt(deleteItemPosition)
                                //notify the adapter of deletion
                                mAdapter.notifyDataChanged(
                                    inQueueAndUploadedList,
                                    true,
                                    false,
                                    false,
                                    deleteItemPosition
                                )
                                checkToRemoveInQueueHeading()
                            } else if (inQueueAndUploadedList[deleteItemPosition].uploadedInvoice != null) {
                                createDeleteUploadedInvoiceDialog(mAdapter, deleteItemPosition)
                            }
                        }
                    }
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return if (lstUploadTabList!!.adapter != null && lstUploadTabList!!.adapter is InvoiceInQueueUploadListAdapter) {
                        val mAdapter =
                            lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?
                        if (mAdapter != null) {
                            val currentItemPosition = viewHolder.adapterPosition
                            if (inQueueAndUploadedList[currentItemPosition].inQueueForUploadInvoice != null) {
                                val invoiceStatus =
                                    inQueueAndUploadedList[currentItemPosition].inQueueForUploadInvoice!!.status
                                if (invoiceStatus == InQueueForUploadDataModel.InvoiceStatus.FAILED || invoiceStatus == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE) {
                                    ItemTouchHelper.LEFT
                                } else {
                                    0
                                }
                            } else if (inQueueAndUploadedList[currentItemPosition].uploadedInvoice != null) {
                                val uploadedInvoice =
                                    inQueueAndUploadedList[currentItemPosition].uploadedInvoice
                                //PNF-340 created by user check, removed as permissed to view invoice added.
                                //UserDetails createdBy = ((InvoiceMgr) inQueueAndUploadedList.get(currentItemPosition)).getCreatedBy();
                                if (uploadedInvoice!!.isStatus(Invoice.Status.PENDING) || uploadedInvoice.isStatus(
                                        Invoice.Status.REJECTED
                                    )
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
                    isCurrentlyActive: Boolean
                ) {
                    if (lstUploadTabList!!.adapter != null && lstUploadTabList!!.adapter is InvoiceInQueueUploadListAdapter) {
                        val text = getString(R.string.txt_delete)
                        //InvoiceUploadsAdapter.ViewHolderItem mViewHolder = (InvoiceUploadsAdapter.ViewHolderItem) viewHolder;
                        val color = resources.getColor(R.color.pinky_red)
                        background.color = color
                        background.setBounds(
                            viewHolder.itemView.right + dX.toInt(),
                            viewHolder.itemView.top,
                            viewHolder.itemView.right,
                            viewHolder.itemView.bottom
                        )
                        background.draw(c)
                        val p = Paint()
                        val buttonWidth = viewHolder.itemView.right - SWIPE_BUTTON_WIDTH
                        val rightButton = RectF(
                            buttonWidth.toFloat(),
                            viewHolder.itemView.top.toFloat(),
                            viewHolder.itemView.right.toFloat(),
                            viewHolder.itemView.bottom.toFloat()
                        )
                        p.color = color
                        c.drawRect(rightButton, p)
                        p.color = Color.WHITE
                        p.isAntiAlias = true
                        p.textSize = SWIPE_BUTTON_TEXT_SIZE.toFloat()
                        val textWidth = p.measureText(text)
                        val bmp = BitmapFactory.decodeResource(
                            this@UploadInvoicesForNonZmOrders.resources,
                            R.drawable.deletedraft
                        )
                        val bounds = Rect()
                        p.getTextBounds(text, 0, text.length, bounds)
                        val combinedHeight =
                            (bmp.height + SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
                        c.drawBitmap(
                            bmp,
                            rightButton.centerX() - bmp.width / 2,
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
        helperUploadInvoice.attachToRecyclerView(lstUploadTabList)
    }

    fun checkToRemoveInQueueHeading() {
        //check if it was the last inQueue item to be deleted
        var hasInQueueInvoice = false
        for (i in inQueueAndUploadedList.indices) {
            if (inQueueAndUploadedList[i].inQueueForUploadInvoice != null) {
                hasInQueueInvoice = true
                break
            } else if (inQueueAndUploadedList[i].inQueueForUploadMergedInvoice != null) {
                hasInQueueInvoice = true
                break
            }
        }
        if (!hasInQueueInvoice) {
            //remove the inqueue heading
            inQueueAndUploadedList.removeAt(0)
            if (lstUploadTabList!!.adapter != null && lstUploadTabList!!.adapter is InvoiceInQueueUploadListAdapter) {
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                    inQueueAndUploadedList,
                    true,
                    false,
                    false,
                    0
                )
            }
            Companion.currentUploadPosition = 1
        } else {
            //set the current upload position
            val pos = this.currentUploadPosition
            Companion.currentUploadPosition = pos
        }
        if (inQueueAndUploadedList != null && inQueueAndUploadedList.size == 0) {
            displayNoInvoiceMessage(true)
            btnNewInvoice!!.visibility = View.VISIBLE
        }
        invoicesCountByStatus()
    }

    fun checkToRemoveUploadedHeading() {
        //check if it was the last inQueue item to be deleted
        var hasUploadedInvoice = false
        var posUploadedHeading = 0
        for (i in inQueueAndUploadedList.indices) {
            if (inQueueAndUploadedList[i].isHeader(InvoiceUploadsListDataMgr.UploadListHeader.Uploaded)) {
                posUploadedHeading = i
            }
            if (inQueueAndUploadedList[i].uploadedInvoice != null) {
                hasUploadedInvoice = true
                break
            }
        }
        if (!hasUploadedInvoice) {
            //remove the inqueue heading
            inQueueAndUploadedList.removeAt(posUploadedHeading)
            if (lstUploadTabList!!.adapter != null && lstUploadTabList!!.adapter is InvoiceInQueueUploadListAdapter) {
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                    inQueueAndUploadedList,
                    true,
                    false,
                    false,
                    posUploadedHeading
                )
            }
        }
        if (inQueueAndUploadedList != null && inQueueAndUploadedList.size == 0) {
            displayNoInvoiceMessage(true)
            btnNewInvoice!!.visibility = View.VISIBLE
        }
    }

    fun createDeleteUploadedInvoiceDialog(
        mAdapter: InvoiceInQueueUploadListAdapter, deleteItemPosition: Int
    ) {
        val builder = AlertDialog.Builder(this@UploadInvoicesForNonZmOrders)
        builder.setPositiveButton(getString(R.string.txt_yes_delete)) { dialog, which -> //remove the row from the adapter and notify the adapter call the API also to remove from the backend
            val json = createDeleteJsonForInvoice(
                inQueueAndUploadedList[deleteItemPosition].uploadedInvoice!!.invoiceId
            )
            val outlet: MutableList<Outlet?> = ArrayList()
            outlet.add(SharedPref.defaultOutlet)
            deleteUnprocessedInvoice(
                this@UploadInvoicesForNonZmOrders,
                outlet as List<Outlet>?,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        Log.d("", getString(R.string.txt_invoice_successfully_deleted))
                        invoicesCountByStatus()
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        getToastRed(this@UploadInvoicesForNonZmOrders.getString(R.string.txt_delete_draft_fail))
                    }
                },
                json
            )

            //Remove from the list and update the adapter
            inQueueAndUploadedList.removeAt(deleteItemPosition)
            mAdapter.notifyDataChanged(
                inQueueAndUploadedList,
                true,
                false,
                false,
                deleteItemPosition
            )
            checkToRemoveUploadedHeading()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.txt_cancel)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnDismissListener {
            mAdapter.notifyDataChanged(
                inQueueAndUploadedList,
                false,
                false,
                true,
                deleteItemPosition
            )
        }
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

    private fun dataReceivedFromReviewInvoices(invoiceDataList: List<InQueueForUploadDataModel>?) {
        if (invoiceDataList != null) {
            if (inQueueAndUploadedList.size == 0) {
                inQueueAndUploadedList.addAll(
                    0,
                    deserializeInQueueToInvoiceUploads(invoiceDataList)
                )
                inQueueAndUploadedList.add(
                    0,
                    getInstanceWithHeaderData(InvoiceUploadsListDataMgr.UploadListHeader.InQueue)
                )
                if (lstUploadTabList!!.adapter == null) {
                    invoiceUploadsAdapter = InvoiceInQueueUploadListAdapter(
                        this@UploadInvoicesForNonZmOrders,
                        inQueueAndUploadedList,
                        this
                    )
                    lstUploadTabList!!.adapter = invoiceUploadsAdapter
                } else {
                    lstUploadTabList!!.adapter!!.notifyDataSetChanged()
                }
                initUploadInvoiceSwipe()
                setUploadsTabActive()
                Companion.currentUploadPosition = 1
                createInvoiceCall(inQueueAndUploadedList)
            } else if (inQueueAndUploadedList.size > 0 && inQueueAndUploadedList[0].isHeader(
                    InvoiceUploadsListDataMgr.UploadListHeader.Uploaded
                )
            ) {
                inQueueAndUploadedList.addAll(
                    0,
                    deserializeInQueueToInvoiceUploads(invoiceDataList)
                )
                inQueueAndUploadedList.add(
                    0,
                    getInstanceWithHeaderData(InvoiceUploadsListDataMgr.UploadListHeader.InQueue)
                )
                if (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter? != null) {
                    (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                        inQueueAndUploadedList,
                        false,
                        true,
                        false,
                        0
                    )
                }
                lstUploadTabList!!.scrollToPosition(0)
                setUploadsTabActive()
                createInvoiceCall(inQueueAndUploadedList)
            } else if (inQueueAndUploadedList.size > 0 && inQueueAndUploadedList[0].isHeader(
                    InvoiceUploadsListDataMgr.UploadListHeader.InQueue
                )
            ) {
                //find the position from where we need to add new inqueue
                var posToAdd = 0
                //this throws index out of bound if there are no uploaded invoices as the loop never finds the instance of InvoiceMgr and pos becomes -ve
                for (i in inQueueAndUploadedList.indices) {
                    if (inQueueAndUploadedList[i].uploadedInvoice != null) {
                        posToAdd = i - 1
                        break
                    } else {
                        posToAdd = posToAdd + 1
                    }
                }
                inQueueAndUploadedList.addAll(
                    posToAdd,
                    deserializeInQueueToInvoiceUploads(invoiceDataList)
                )
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                    inQueueAndUploadedList,
                    false,
                    false,
                    false,
                    0
                )
                setUploadsTabActive()
                createInvoiceCall(inQueueAndUploadedList)
            }
        }
    }

    private fun uploadPendingInvoices(invoiceDataList: List<InQueueForUploadDataModel>?) {
        if (invoiceDataList != null && invoiceDataList.size > 0) {
            //nothing in the inqueue list its the first upload, or if the invoice list is empty altogether(no in queue and no uploaded)
            if (inQueueAndUploadedList.size > 0 && inQueueAndUploadedList[0].isHeader(
                    InvoiceUploadsListDataMgr.UploadListHeader.Uploaded
                ) || inQueueAndUploadedList.size == 0
            ) {
                inQueueAndUploadedList.addAll(
                    0,
                    deserializeInQueueToInvoiceUploads(invoiceDataList)
                )
                inQueueAndUploadedList.add(
                    0,
                    getInstanceWithHeaderData(InvoiceUploadsListDataMgr.UploadListHeader.InQueue)
                )
            }
            val pos = this.currentUploadPosition
            Companion.currentUploadPosition = pos
        } else {
            Companion.currentUploadPosition = 1
        }
        if (inQueueAndUploadedList.size > 0) {
            txtEditInvoice!!.visibility = View.GONE
            lstUploadTabList!!.visibility = View.VISIBLE
            displayNoInvoiceMessage(false)
            //listInvoicesNoItem.setVisibility(View.GONE);
            if (lstUploadTabList!!.adapter == null) {
                invoiceUploadsAdapter = InvoiceInQueueUploadListAdapter(
                    this@UploadInvoicesForNonZmOrders,
                    inQueueAndUploadedList,
                    this
                )
                lstUploadTabList!!.adapter = invoiceUploadsAdapter
            } else {
                lstUploadTabList!!.adapter!!.notifyDataSetChanged()
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.setDeleteMergeActiveInactive(
                    false
                )
            }
            initUploadInvoiceSwipe()
        } else {
            displayNoInvoiceMessage(true)
        }
        setUploadsTabActive()
        if (invoiceDataList != null && invoiceDataList.size > 0) {
            createInvoiceCall(inQueueAndUploadedList)
        }
    }

    //logic to get the position from where to start uploading
    private val currentUploadPosition: Int
        private get() {
            //logic to get the position from where to start uploading
            var startUploadPosition = 1
            for (i in inQueueAndUploadedList.indices) {
                if (inQueueAndUploadedList[i].inQueueForUploadInvoice != null) {
                    val status = inQueueAndUploadedList[i].inQueueForUploadInvoice!!.status
                    if (status == InQueueForUploadDataModel.InvoiceStatus.FAILED) {
                        startUploadPosition = startUploadPosition + 1
                    }
                }
            }
            return startUploadPosition
        }

    @Synchronized
    fun createInvoiceCall(
        invoiceData: MutableList<InvoiceUploadsListDataMgr>?
    ) {
        Log.d("", "$inQueueAndUploadedList*******")
        //also check if the current outlet selected and invoice being uploaded is for same outlet or not
        if (invoiceData!!.size > 0 && Companion.currentUploadPosition < invoiceData.size && !stopUploading) {
            val isInvoiceForSelectedOutlet = invoiceDataManager!!.checkIfInvoiceIsForSelectedOutlet(
                invoiceData[Companion.currentUploadPosition]
            )
            Log.d(
                "INVOICE FRAGMENT ",
                "CurrentUploadPosition " + Companion.currentUploadPosition + "*****"
            )
            //since the first value is header;
            //InvoiceImageRotationDataModel.InvoiceStatus invoiceStatus = ((InvoiceImageRotationDataModel) invoiceData.get(currentUploadPosition)).getStatus();
            if (invoiceData[Companion.currentUploadPosition].inQueueForUploadInvoice != null && isInvoiceForSelectedOutlet && ((invoiceData[Companion.currentUploadPosition].inQueueForUploadInvoice!!.status == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE) || (invoiceData[Companion.currentUploadPosition].inQueueForUploadInvoice!!.status) == InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING)) {
                val inQueueForUploadDataModel =
                    invoiceData[Companion.currentUploadPosition].inQueueForUploadInvoice
                isUploadInProgress = true
                val deviceDimensions =
                    CommonMethods.getDeviceDimensions(this@UploadInvoicesForNonZmOrders)
                val file = File(inQueueForUploadDataModel!!.imageFilePath)
                val bitmap: Bitmap?
                bitmap = if (inQueueForUploadDataModel.isInvoiceSelectedIsImage) {
                    invoiceDataManager!!.decodeSampledBitmapFromImageUri(
                        Uri.fromFile(file),
                        deviceDimensions.deviceWidth,
                        deviceDimensions.deviceHeight
                    )
                } else {
                    null
                }
                inQueueForUploadDataModel.status =
                    InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING
                UpdateImageInvoiceStatusTask().execute(inQueueForUploadDataModel)
                if (lstUploadTabList!!.adapter != null) {
                    (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                        invoiceData,
                        false,
                        false,
                        true,
                        Companion.currentUploadPosition
                    )
                }
                val filePathSplitArray =
                    inQueueForUploadDataModel.imageFilePath?.split("/".toRegex())
                        ?.dropLastWhile { it.isEmpty() }
                        ?.toTypedArray()
                val filename = filePathSplitArray?.get(filePathSplitArray.size?.minus(1)!!)
                //                CommonMethods.parsedText(getActivity(), inQueueForUploadDataModel, lstSuppliers);
                UploadFileMultipart(
                    this@UploadInvoicesForNonZmOrders,
                    bitmap,
                    Uri.fromFile(file),
                    "INVOICE",
                    filename!!,
                    object : MultiPartImageUploaded {
                        override fun result(imageUpload: MultiPartImageUploadResponse?) {
                            if (imageUpload != null && imageUpload.data != null && imageUpload.data!!.files != null
                                && imageUpload.data!!.files.size > 0 && !StringHelper.isStringNullOrEmpty(
                                    imageUpload.data!!.files[0].fileUrl
                                )
                            ) {
                                val name = imageUpload.data!!.files[0].fileName
                                val url = imageUpload.data!!.files[0].fileUrl
                                val ic = Create()
                                val imageData: MutableList<ImagesModel> = ArrayList()
                                val imageModel = ImagesModel()
                                if (url != null && url.length > 0) {
                                    imageModel.imageURL =
                                        url.substring(0, url.lastIndexOf(File.separator)) + "/"
                                }
                                val imageNames: MutableList<String?> = ArrayList()
                                imageNames.add(name)
                                imageModel.imageFileNames = imageNames as MutableList<String>
                                imageData.add(imageModel)
                                ic.images = imageData

                                //update the Web URL of image
                                val defaultOutlet = SharedPref.defaultOutlet
                                if (defaultOutlet != null) {
                                    ic.outlet = defaultOutlet
                                } else {
                                    val outlet = Outlet()
                                    outlet.outletName =
                                        invoiceData[Companion.currentUploadPosition].inQueueForUploadInvoice!!.outletName
                                    outlet.outletId =
                                        invoiceData[Companion.currentUploadPosition].inQueueForUploadInvoice!!.outletId
                                    ic.outlet = outlet
                                }
                                val createdByUserDetails = UserDetails()
                                createdByUserDetails.firstName =
                                    SharedPref.read(SharedPref.USER_FIRST_NAME, "")
                                createdByUserDetails.id = SharedPref.read(SharedPref.USER_ID, "")
                                createdByUserDetails.imageURL =
                                    SharedPref.read(SharedPref.USER_IMAGE_URL, "")
                                createdByUserDetails.lastName =
                                    SharedPref.read(SharedPref.USER_LAST_NAME, "")
                                createdByUserDetails.phone = ""
                                ic.createdBy = createdByUserDetails
                                CreateInvoice(
                                    this@UploadInvoicesForNonZmOrders,
                                    ic,
                                    object : CreateInvoice {
                                        override fun result(response: Create.Response?) {
                                            if (response != null && invoiceData.size > 0) {
                                                //showMsg(getString(R.string.txt_invoice_created));
                                                //invoice created, delete from directory and call update uploads invoice list
                                                invoiceDataManager!!.deleteDirectory(
                                                    File(
                                                        inQueueForUploadDataModel.imageDirectoryPath
                                                    )
                                                )
                                                //remove from the inQueue local list
                                                invoiceData.removeAt(Companion.currentUploadPosition)
                                                if (lstUploadTabList!!.adapter != null) {
                                                    (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                        invoiceData,
                                                        true,
                                                        false,
                                                        false,
                                                        Companion.currentUploadPosition
                                                    )
                                                }
                                                //call the retrieve all invoice API to refresh the data
                                                //update the uploaded list
                                                val outlet: MutableList<Outlet?> = ArrayList()
                                                outlet.add(SharedPref.defaultOutlet)
                                                GetAllPendingInvoicesV1(
                                                    this@UploadInvoicesForNonZmOrders,
                                                    outlet as List<Outlet>?,
                                                    false,
                                                    object : GetInvoices {

                                                        override fun result(invoices: List<Invoice>?) {
                                                            Log.d(
                                                                "INVOICE UPLOAD",
                                                                "get all pending invoice"
                                                            )
                                                            isUploadInProgress = false
                                                            threeDotLoaderBlue!!.visibility =
                                                                View.GONE
                                                            //invoicePendingList.clear();
                                                            if (invoices != null) {
                                                                val newInvoiceAddedUpdatedList: ArrayList<InvoiceMgr>
                                                                Log.e(
                                                                    "GetAllPendingInvoices",
                                                                    "invoices.size()" + invoices.size
                                                                )
                                                                newInvoiceAddedUpdatedList =
                                                                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                                            invoices
                                                                        ).toString(),
                                                                        object :
                                                                            TypeToken<ArrayList<InvoiceMgr?>?>() {}.type
                                                                    )
                                                                Collections.sort(
                                                                    newInvoiceAddedUpdatedList,
                                                                    TimeCreatedCompare()
                                                                )
                                                                if (newInvoiceAddedUpdatedList.size == 1) {
                                                                    invoiceData.add(
                                                                        getInstanceWithHeaderData(
                                                                            InvoiceUploadsListDataMgr.UploadListHeader.Uploaded
                                                                        )
                                                                    )
                                                                    val uploadedInvoice =
                                                                        InvoiceUploadsListDataMgr()
                                                                    uploadedInvoice.uploadedInvoice =
                                                                        newInvoiceAddedUpdatedList[0]
                                                                    invoiceData.add(uploadedInvoice)
                                                                    (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                                        invoiceData,
                                                                        false,
                                                                        false,
                                                                        false,
                                                                        0
                                                                    )
                                                                } else {
                                                                    var positionToBeUpdated = 0
                                                                    for (i in invoiceData.indices) {
                                                                        if (invoiceData[i].uploadedInvoice != null) {
                                                                            positionToBeUpdated = i
                                                                            break
                                                                        }
                                                                    }
                                                                    val uploadedInvoice =
                                                                        InvoiceUploadsListDataMgr()
                                                                    uploadedInvoice.uploadedInvoice =
                                                                        newInvoiceAddedUpdatedList[0]
                                                                    invoiceData.add(
                                                                        positionToBeUpdated,
                                                                        uploadedInvoice
                                                                    )
                                                                    if (lstUploadTabList!!.adapter != null) {
                                                                        (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                                            invoiceData,
                                                                            false,
                                                                            true,
                                                                            false,
                                                                            positionToBeUpdated
                                                                        )
                                                                    }
                                                                }
                                                                createInvoiceCall(invoiceData)
                                                                //checks that the inQueue header is removed if there is nothing pending
                                                                if (invoiceData.size > 1 && invoiceData[1].header != null && invoiceData[1].isHeader(
                                                                        InvoiceUploadsListDataMgr.UploadListHeader.Uploaded
                                                                    )
                                                                ) {
                                                                    invoiceData.removeAt(0)
                                                                    if (lstUploadTabList!!.adapter != null) {
                                                                        (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                                            invoiceData,
                                                                            true,
                                                                            false,
                                                                            false,
                                                                            0
                                                                        )
                                                                    }
                                                                    Companion.currentUploadPosition =
                                                                        1
                                                                }
                                                            } else {
                                                                isUploadInProgress = false
                                                                //error fetching all the pending invoices data
                                                            }
                                                        }
                                                    })
                                            } else {
                                                /* invoiceUploadFail(invoiceData);*/
                                                isUploadInProgress = false
                                                Log.d("Invoice", "create invoice creation error")
                                                invoiceUploadFail(
                                                    invoiceData,
                                                    Companion.currentUploadPosition
                                                )
                                            }
                                        }
                                    })
                            } else {
                                isUploadInProgress = false
                                Log.d("Invoice", "invoice image upload error error")
                                val response =
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUpload)
                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception(ServiceConstant.CrashlyticsCustomErrorCode.IMAGE_UPLOAD_FAIL.toString() + " " + response))
                                invoiceUploadFail(invoiceData, Companion.currentUploadPosition)
                            }
                        }
                    })
            }
            //            Log.e("size", "createInvoiceCall: " + invoiceData.get(currentUploadPosition).getInQueueForUploadMergedInvoice().size());
//            Log.e("size", "createInvoiceCall: " + invoiceData.get(currentUploadPosition).getInQueueForUploadMergedInvoice().get(0).getStatus());
            if (invoiceData[Companion.currentUploadPosition].inQueueForUploadMergedInvoice != null && isInvoiceForSelectedOutlet && ((invoiceData[Companion.currentUploadPosition].inQueueForUploadMergedInvoice!![0].status == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE) || (invoiceData[Companion.currentUploadPosition].inQueueForUploadMergedInvoice!![0].status) == InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING)) {
                Log.e("Came here", "createInvoiceCall: ")
                val inQueueForUploadDataModel =
                    invoiceData[Companion.currentUploadPosition].inQueueForUploadMergedInvoice
                isUploadInProgress = true
                val deviceDimensions =
                    CommonMethods.getDeviceDimensions(this@UploadInvoicesForNonZmOrders)
                val multipleFileUploadRequests: MutableList<MultipleFileUploadRequest> = ArrayList()
                for (i in inQueueForUploadDataModel!!.indices) {
                    val file = File(inQueueForUploadDataModel[i].imageFilePath)
                    var bitmap: Bitmap?
                    bitmap = if (inQueueForUploadDataModel[i].isInvoiceSelectedIsImage) {
                        invoiceDataManager!!.decodeSampledBitmapFromImageUri(
                            Uri.fromFile(file),
                            deviceDimensions.deviceWidth,
                            deviceDimensions.deviceHeight
                        )
                    } else {
                        null
                    }
                    inQueueForUploadDataModel[i].status =
                        InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING
                    UpdateImageInvoiceStatusTask().execute(inQueueForUploadDataModel[i])
                    if (lstUploadTabList!!.adapter != null) {
                        (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                            invoiceData,
                            false,
                            false,
                            true,
                            Companion.currentUploadPosition
                        )
                    }
                    val filePathSplitArray =
                        inQueueForUploadDataModel[i].imageFilePath?.split("/".toRegex())
                            ?.dropLastWhile { it.isEmpty() }
                            ?.toTypedArray()
                    val filename = filePathSplitArray?.get(filePathSplitArray.size?.minus(1)!!)
                    val multipleFileUploadRequest = MultipleFileUploadRequest()
                    multipleFileUploadRequest.fileName = filename
                    multipleFileUploadRequest.uri = Uri.fromFile(file)
                    multipleFileUploadRequest.bitmap = bitmap
                    multipleFileUploadRequests.add(multipleFileUploadRequest)
                }
                if (multipleFileUploadRequests != null && multipleFileUploadRequests.size == inQueueForUploadDataModel.size) {
                    uploadMultipleFileMultipart(
                        this@UploadInvoicesForNonZmOrders,
                        multipleFileUploadRequests,
                        object : MultiPartImageUploaded {
                            override fun result(imageUpload: MultiPartImageUploadResponse?) {
                                if (imageUpload != null && imageUpload.data != null && imageUpload.data!!.files != null && imageUpload.data!!.files.size > 0) {
                                    val ic = Create()
                                    val imageData: MutableList<ImagesModel> = ArrayList()
                                    for (i in imageUpload.data!!.files.indices) {
                                        val name = imageUpload.data!!.files[i].fileName
                                        val url = imageUpload.data!!.files[i].fileUrl
                                        val imageModel = ImagesModel()
                                        if (url != null && url.length > 0) {
                                            imageModel.imageURL = url.substring(
                                                0,
                                                url.lastIndexOf(File.separator)
                                            ) + "/"
                                        }
                                        val imageNames: MutableList<String?> = ArrayList()
                                        imageNames.add(name)
                                        imageModel.imageFileNames = imageNames as MutableList<String>
                                        imageData.add(imageModel)
                                    }
                                    if (imageData != null && imageData.size > 0 && imageData.size == imageUpload.data!!.files.size) ic.images =
                                        imageData

                                    //update the Web URL of image
                                    val defaultOutlet = SharedPref.defaultOutlet
                                    if (defaultOutlet != null) {
                                        ic.outlet = defaultOutlet
                                    } else {
                                        val outlet = Outlet()
                                        outlet.outletName =
                                            invoiceData[Companion.currentUploadPosition].inQueueForUploadMergedInvoice!![0].outletName
                                        outlet.outletId =
                                            invoiceData[Companion.currentUploadPosition].inQueueForUploadMergedInvoice!![0].outletId
                                        ic.outlet = outlet
                                    }
                                    val createdByUserDetails = UserDetails()
                                    createdByUserDetails.firstName =
                                        SharedPref.read(SharedPref.USER_FIRST_NAME, "")
                                    createdByUserDetails.id =
                                        SharedPref.read(SharedPref.USER_ID, "")
                                    createdByUserDetails.imageURL =
                                        SharedPref.read(SharedPref.USER_IMAGE_URL, "")
                                    createdByUserDetails.lastName =
                                        SharedPref.read(SharedPref.USER_LAST_NAME, "")
                                    createdByUserDetails.phone = ""
                                    ic.createdBy = createdByUserDetails
                                    CreateInvoice(
                                        this@UploadInvoicesForNonZmOrders,
                                        ic,
                                        object : CreateInvoice {
                                            override fun result(response: Create.Response?) {
                                                if (response != null && invoiceData != null && invoiceData.size > 0) {
                                                    //showMsg(getString(R.string.txt_invoice_created));
                                                    //invoice created, delete from directory and call update uploads invoice list
                                                    for (i in inQueueForUploadDataModel.indices) invoiceDataManager!!.deleteDirectory(
                                                        File(
                                                            inQueueForUploadDataModel[i].imageDirectoryPath
                                                        )
                                                    )
                                                    //remove from the inQueue local list
                                                    invoiceData.removeAt(Companion.currentUploadPosition)
                                                    if (lstUploadTabList!!.adapter != null) {
                                                        (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                            invoiceData,
                                                            true,
                                                            false,
                                                            false,
                                                            Companion.currentUploadPosition
                                                        )
                                                    }
                                                    //call the retrieve all invoice API to refresh the data
                                                    //update the uploaded list
                                                    val outlet: MutableList<Outlet?> = ArrayList()
                                                    outlet.add(SharedPref.defaultOutlet)
                                                    GetAllPendingInvoicesV1(
                                                        this@UploadInvoicesForNonZmOrders,
                                                        outlet as List<Outlet>?,
                                                        false,
                                                        object : GetInvoices {

                                                            override fun result(invoices: List<Invoice>?) {
                                                                Log.d(
                                                                    "INVOICE UPLOAD",
                                                                    "get all pending invoice"
                                                                )
                                                                isUploadInProgress = false
                                                                threeDotLoaderBlue!!.visibility =
                                                                    View.GONE
                                                                //invoicePendingList.clear();
                                                                if (invoices != null) {
                                                                    val newInvoiceAddedUpdatedList: ArrayList<InvoiceMgr>
                                                                    Log.e(
                                                                        "GetAllPendingInvoices",
                                                                        "invoices.size()" + invoices.size
                                                                    )
                                                                    newInvoiceAddedUpdatedList =
                                                                        ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                                                invoices
                                                                            ).toString(),
                                                                            object :
                                                                                TypeToken<ArrayList<InvoiceMgr?>?>() {}.type
                                                                        )
                                                                    Collections.sort(
                                                                        newInvoiceAddedUpdatedList,
                                                                        TimeCreatedCompare()
                                                                    )
                                                                    if (newInvoiceAddedUpdatedList.size == 1) {
                                                                        invoiceData.add(
                                                                            getInstanceWithHeaderData(
                                                                                InvoiceUploadsListDataMgr.UploadListHeader.Uploaded
                                                                            )
                                                                        )
                                                                        val uploadedInvoice =
                                                                            InvoiceUploadsListDataMgr()
                                                                        uploadedInvoice.uploadedInvoice =
                                                                            newInvoiceAddedUpdatedList[0]
                                                                        invoiceData.add(
                                                                            uploadedInvoice
                                                                        )
                                                                        (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                                            invoiceData,
                                                                            false,
                                                                            false,
                                                                            false,
                                                                            0
                                                                        )
                                                                    } else {
                                                                        var positionToBeUpdated = 0
                                                                        for (i in invoiceData.indices) {
                                                                            if (invoiceData[i].uploadedInvoice != null) {
                                                                                positionToBeUpdated =
                                                                                    i
                                                                                break
                                                                            }
                                                                        }
                                                                        val uploadedInvoice =
                                                                            InvoiceUploadsListDataMgr()
                                                                        uploadedInvoice.uploadedInvoice =
                                                                            newInvoiceAddedUpdatedList[0]
                                                                        invoiceData.add(
                                                                            positionToBeUpdated,
                                                                            uploadedInvoice
                                                                        )
                                                                        if (lstUploadTabList!!.adapter != null) {
                                                                            (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                                                invoiceData,
                                                                                false,
                                                                                true,
                                                                                false,
                                                                                positionToBeUpdated
                                                                            )
                                                                        }
                                                                    }
                                                                    createInvoiceCall(invoiceData)
                                                                    //checks that the inQueue header is removed if there is nothing pending
                                                                    if (invoiceData.size > 1 && invoiceData[1].header != null && invoiceData[1].isHeader(
                                                                            InvoiceUploadsListDataMgr.UploadListHeader.Uploaded
                                                                        )
                                                                    ) {
                                                                        invoiceData.removeAt(0)
                                                                        if (lstUploadTabList!!.adapter != null) {
                                                                            (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                                                                                invoiceData,
                                                                                true,
                                                                                false,
                                                                                false,
                                                                                0
                                                                            )
                                                                        }
                                                                        Companion.currentUploadPosition =
                                                                            1
                                                                    }
                                                                } else {
                                                                    isUploadInProgress = false
                                                                    //error fetching all the pending invoices data
                                                                }
                                                            }
                                                        })
                                                } else {
                                                    /* invoiceUploadFail(invoiceData);*/
                                                    isUploadInProgress = false
                                                    Log.d(
                                                        "Invoice",
                                                        "create invoice creation error"
                                                    )
                                                    invoiceUploadFail(
                                                        invoiceData,
                                                        Companion.currentUploadPosition
                                                    )
                                                }
                                            }
                                        })
                                } else {
                                    isUploadInProgress = false
                                    Log.d("Invoice", "invoice image upload error error")
                                    val response =
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUpload)
                                    FirebaseCrashlytics.getInstance()
                                        .recordException(Exception(ServiceConstant.CrashlyticsCustomErrorCode.IMAGE_UPLOAD_FAIL.toString() + " " + response))
                                    invoiceUploadFail(invoiceData, Companion.currentUploadPosition)
                                }
                            }
                        })
                }
            } else if (invoiceData != null && invoiceData.size > 2 && invoiceData[2].uploadedInvoice != null) {
                invoiceData.removeAt(0)
                if (lstUploadTabList!!.adapter != null) {
                    (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                        invoiceData,
                        true,
                        false,
                        false,
                        0
                    )
                }
                Companion.currentUploadPosition = 1
            }
        }
        invoicesCountByStatus()
    }

    /**
     * if any one of the invoice upload fails continue uploading others
     *
     * @param invoiceData
     */
    private fun invoiceUploadFail(
        invoiceData: MutableList<InvoiceUploadsListDataMgr>?,
        failedInvoicePosition: Int
    ) {
        Log.d("FAILED UPLOAD INVOICE", "****" + Companion.currentUploadPosition)
        //added check for class cast exception
        if (invoiceData!!.size > failedInvoicePosition && invoiceData[failedInvoicePosition].inQueueForUploadInvoice != null) {
            //Invoice creation failed
            invoiceData[failedInvoicePosition].inQueueForUploadInvoice!!.status =
                InQueueForUploadDataModel.InvoiceStatus.FAILED
            UpdateImageInvoiceStatusTask().execute(invoiceData[failedInvoicePosition].inQueueForUploadInvoice)
            //notify data set changed
            if (lstUploadTabList!!.adapter != null) {
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                    invoiceData,
                    false,
                    false,
                    true,
                    failedInvoicePosition
                )
            }
            Log.e("createInvoice", "failed")
            if (invoiceData.size > 0) {
                Companion.currentUploadPosition = this.currentUploadPosition
                createInvoiceCall(invoiceData)
            }
        } else if (invoiceData.size > failedInvoicePosition && invoiceData[failedInvoicePosition].inQueueForUploadMergedInvoice != null) {
            for (i in invoiceData[failedInvoicePosition].inQueueForUploadMergedInvoice!!.indices) {
                invoiceData[failedInvoicePosition].inQueueForUploadMergedInvoice!![i].status =
                    InQueueForUploadDataModel.InvoiceStatus.FAILED
                UpdateImageInvoiceStatusTask().execute(invoiceData[failedInvoicePosition].inQueueForUploadMergedInvoice!![i])
            }
            //Invoice creation failed
            //notify data set changed
            if (lstUploadTabList!!.adapter != null) {
                (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.notifyDataChanged(
                    invoiceData,
                    false,
                    false,
                    true,
                    failedInvoicePosition
                )
            }
            Log.e("createInvoice", "failed")
            if (invoiceData.size > 0) {
                Companion.currentUploadPosition = this.currentUploadPosition
                createInvoiceCall(invoiceData)
            }
        }
    }

    /**
     * Delete and merge confirmation dialog
     */
    private inner class UpdateImageInvoiceStatusTask :
        AsyncTask<InQueueForUploadDataModel?, Void?, Void?>() {

        override fun doInBackground(vararg invoiceImageRotationDataModels: InQueueForUploadDataModel?): Void? {
            invoiceDataManager.saveInfoFileToInternalStorage(invoiceImageRotationDataModels[0]!!)
            return null
        }
    }

    private fun createConfirmationDialog(isDelete: Boolean) {
        Log.d("INVOICE FRAGMENT", "Create confirmation dialog displayed")
        val toBeEditedListUploaded = ArrayList<InvoiceMgr?>()
        val toBeEditedListInQueue = ArrayList<InQueueForUploadDataModel?>()
        for (i in invoicesTobeEditedList.indices) {
            if (invoicesTobeEditedList[i].uploadedInvoice != null) {
                toBeEditedListUploaded.add(invoicesTobeEditedList[i].uploadedInvoice)
            } else if (invoicesTobeEditedList[i].inQueueForUploadInvoice != null) {
                toBeEditedListInQueue.add(invoicesTobeEditedList[i].inQueueForUploadInvoice)
            } else if (invoicesTobeEditedList[i].inQueueForUploadMergedInvoice != null) {
                toBeEditedListInQueue.addAll(invoicesTobeEditedList[i].inQueueForUploadMergedInvoice!!)
            }
        }
        val message: String
        val positiveButtonText: String
        if (isDelete) {
            message = getString(R.string.txt_delete_invoice_qtn)
            positiveButtonText = getString(R.string.txt_delete)
        } else {
            message = getString(R.string.txt_merge_invoices_qtn)
            positiveButtonText = getString(R.string.txt_merge)
        }
        val builder = androidx.appcompat.app.AlertDialog.Builder(this@UploadInvoicesForNonZmOrders)
        builder.setPositiveButton(positiveButtonText) { dialog, which ->
            dialog.dismiss()
            if (isDelete) {
                if (toBeEditedListUploaded.size > 0) {
                    threeDotLoaderBlue!!.visibility = View.VISIBLE
                    val json = createDeleteJson(toBeEditedListUploaded)
                    val outlet: MutableList<Outlet?> = ArrayList()
                    outlet.add(SharedPref.defaultOutlet)
                    deleteUnprocessedInvoice(
                        this@UploadInvoicesForNonZmOrders,
                        outlet as List<Outlet>?,
                        object : GetRequestStatusResponseListener {
                            override fun onSuccessResponse(status: String?) {
                                Log.d("", "Deleted Successfully")
                                threeDotLoaderBlue!!.visibility = View.GONE
                                txtEditInvoice!!.visibility = View.GONE
                                lytMergeDelete!!.visibility = View.GONE
                                stopUploading = true
                                lstUploadTabList!!.adapter = null
                                reloadUploads()
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                /*String networkResponse = new String(error.getNetworkResponse().data);
                                    Log.d("invoice", "cannot delete " + networkResponse);*/
                                getToastRed(getString(R.string.txt_could_not_delete))
                                threeDotLoaderBlue!!.visibility = View.GONE
                            }
                        },
                        json
                    )
                }
                if (toBeEditedListInQueue.size > 0) {
                    Log.e("invoices", "onClick: " + toBeEditedListInQueue.size)
                    for (i in toBeEditedListInQueue.indices) {
                        for (j in inQueueAndUploadedList.indices) {
                            if (inQueueAndUploadedList[j].inQueueForUploadInvoice != null) {
                                if (inQueueAndUploadedList[j].inQueueForUploadInvoice!!.imageFilePath == toBeEditedListInQueue[i]!!
                                        .imageFilePath
                                ) {
                                    //delete from internal storage
                                    invoiceDataManager!!.deleteDirectory(
                                        File(
                                            inQueueAndUploadedList[j].inQueueForUploadInvoice!!.imageDirectoryPath
                                        )
                                    )
                                    inQueueAndUploadedList.removeAt(j)
                                    checkToRemoveInQueueHeading()
                                    break
                                }
                            } else if (inQueueAndUploadedList[j].inQueueForUploadMergedInvoice != null) {
                                Log.e("invoices merge", "onClick: " + toBeEditedListInQueue.size)
                                for (k in inQueueAndUploadedList[j].inQueueForUploadMergedInvoice!!.indices) {
                                    if (inQueueAndUploadedList[j].inQueueForUploadMergedInvoice!![k].imageFilePath == toBeEditedListInQueue[i]!!
                                            .imageFilePath
                                    ) {
                                        //delete from internal storage
                                        invoiceDataManager!!.deleteDirectory(
                                            File(
                                                inQueueAndUploadedList[j].inQueueForUploadMergedInvoice!![k].imageDirectoryPath
                                            )
                                        )
                                        inQueueAndUploadedList.removeAt(j)
                                        checkToRemoveInQueueHeading()
                                        break
                                    }
                                }
                            }
                        }
                    }
                    if (lstUploadTabList!!.adapter != null) {
                        txtEditInvoice!!.visibility = View.GONE
                        btnNewInvoice!!.visibility = View.VISIBLE
                        lytMergeDelete!!.visibility = View.GONE
                        (lstUploadTabList!!.adapter as InvoiceInQueueUploadListAdapter?)!!.setDeleteMergeActiveInactive(
                            false
                        )
                    }
                }
            } else {
                threeDotLoaderBlue!!.visibility = View.VISIBLE
                if (toBeEditedListUploaded.size > 1) {
                    val jsonMerge = mergeInvoiceJson(toBeEditedListUploaded)
                    val outlet: MutableList<Outlet?> = ArrayList()
                    outlet.add(SharedPref.defaultOutlet)
                    mergeInvoiceUnprocessed(
                        this@UploadInvoicesForNonZmOrders,
                        outlet as List<Outlet>?,
                        object : GetRequestStatusResponseListener {
                            override fun onSuccessResponse(status: String?) {
                                stopUploading = true
                                lstUploadTabList!!.adapter = null
                                reloadUploads()
                                threeDotLoaderBlue!!.visibility = View.GONE
                                lytMergeDelete!!.visibility = View.GONE
                                txtEditInvoice!!.visibility = View.GONE
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                threeDotLoaderBlue!!.visibility = View.GONE
                                getToastRed(getString(R.string.txt_could_not_merge))
                            }
                        },
                        jsonMerge
                    )
                } else {
                    if (toBeEditedListInQueue.size > 0) {
                        getToastRed(getString(R.string.txt_cannot_merge_in_queue_invoices))
                    }
                }
            }
        }
        builder.setNegativeButton(getString(R.string.dialog_cancel_button_text)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(message)
        dialog.show()
    }

    private fun displayNoInvoiceMessage(showNoMessage: Boolean) {
        if (showNoMessage) {
            txtEditInvoice!!.visibility = View.GONE
            invoicesNoInvoicesSubText!!.visibility = View.VISIBLE
            invoicesNoInvoicesText!!.text =
                resources.getString(R.string.txt_no_invoice_uploads)
            listInvoicesNoItem!!.visibility = View.VISIBLE
            imgNoInvoices!!.setImageResource(R.drawable.no_upload_invoices)
        } else {
            txtEditInvoice!!.visibility = View.GONE
            listInvoicesNoItem!!.visibility = View.GONE
        }
    }

    companion object {
        private var currentUploadPosition = 1
        private val SWIPE_BUTTON_WIDTH = CommonMethods.dpToPx(70)
        private val SWIPE_BUTTON_TEXT_SIZE = CommonMethods.dpToPx(14)
        private val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE = CommonMethods.dpToPx(11)
    }
}