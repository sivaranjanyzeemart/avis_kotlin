package zeemart.asia.buyers.goodsReceivedNote

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.GRNskusAdapter
import zeemart.asia.buyers.adapter.GRNskusAdapter.SelectedSKUSListener
import zeemart.asia.buyers.adapter.GrnImageListAdapter
import zeemart.asia.buyers.adapter.ViewOrderListAdapter
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DateHelper.getDateInFullDayDateMonthYearFormat
import zeemart.asia.buyers.helper.DialogHelper.alertDialogPaymentFailure
import zeemart.asia.buyers.helper.DialogHelper.alertDialogPaymentSuccess
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.invoices.InvoiceCameraPreview
import zeemart.asia.buyers.invoices.InvoiceDataManager
import zeemart.asia.buyers.models.GrnRequest
import zeemart.asia.buyers.models.GrnRequest.CustomLineItem
import zeemart.asia.buyers.models.MultipleFileUploadRequest
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.modelsimport.MultiPartImageUploadResponse
import zeemart.asia.buyers.network.GRNApi
import zeemart.asia.buyers.network.GRNApi.saveGRNResponseListener
import zeemart.asia.buyers.network.ImageUploadHelper.MultiPartImageUploaded
import zeemart.asia.buyers.network.ImageUploadHelper.UploadFileMultipart
import zeemart.asia.buyers.network.ImageUploadHelper.uploadMultipleFileMultipart
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.io.File
import java.util.*

/**
 * Created by RajPrudhviMarella on 14/June/2021.
 */
class GoodsReceivedNoteDashBoardActivity : AppCompatActivity() {
    private var txtHeader: TextView? = null
    private lateinit var btnCancel: ImageButton
    private var txtAddNotes: TextView? = null
    private var lytAddNotes: LinearLayout? = null
    private lateinit var btnSave: TextView
    private lateinit var lytReceivedDate: RelativeLayout
    private var txtReceivedDateHeader: TextView? = null
    private lateinit var txtReceivedDateValue: TextView
    private var txtNumberOfItemsSelected: TextView? = null
    private lateinit var txtSelectAll: TextView
    private var txtAddNew: TextView? = null
    private lateinit var lytAddNew: RelativeLayout
    private lateinit var lstSkus: RecyclerView
    private var mOrder: Orders? = null
    private lateinit var myCalendar: Calendar
    private var receivedDate = 0L
    private var grNskusAdapter: GRNskusAdapter? = null
    private var isSelectAllClicked = false
    var dateListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        myCalendar!![Calendar.YEAR] = year
        myCalendar!![Calendar.MONTH] = monthOfYear
        myCalendar!![Calendar.DAY_OF_MONTH] = dayOfMonth
        updateLabel()
    }
    private var selectedItemsCount = 0
    private var txtInvoiceNameHeader: TextView? = null
    private lateinit var lytSelectPhotos: RelativeLayout
    private lateinit var lytListPhotos: RelativeLayout
    private lateinit var lstPhotos: RecyclerView
    private lateinit var txtSelectPhoto: TextView
    private var txtIncoicesSub: TextView? = null
    private var invoiceDataList: ArrayList<InQueueForUploadDataModel>? = ArrayList()
    private var customLoadingViewWhite: CustomLoadingViewWhite? = null
    private lateinit var imgCamera: ImageView
    private lateinit var lytEmailToSupplier: LinearLayout
    private lateinit var lytEmailMessage: LinearLayout
    private var txtDidntReceivedHeader: TextView? = null
    private var txtEmailThisOrderSwitch: TextView? = null
    private var txtAddMessage: TextView? = null
    private lateinit var btnEnabledEmail: Switch
    private var isEmailSelected = false
    private lateinit var txtRemainingCharacters: TextView
    private lateinit var edtEmailSettings: EditText
    private lateinit var txtEmailList: TextView
    private var SupplierEmails = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_received_note_dash_board)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }
        lytEmailToSupplier = findViewById(R.id.lyt_email_to_supplier)
        lytEmailToSupplier.setVisibility(View.GONE)
        lytEmailMessage = findViewById(R.id.lyt_message)
        lytEmailMessage.setVisibility(View.GONE)
        txtDidntReceivedHeader = findViewById(R.id.txt_email_to_supplier_header)
        txtEmailThisOrderSwitch = findViewById(R.id.txt_email_this_supplier_switch_remains)
        txtAddMessage = findViewById(R.id.txt_add_message)
        txtEmailList = findViewById(R.id.txt_emails_list)
        btnEnabledEmail = findViewById(R.id.btn_notification_on_off)
        btnEnabledEmail.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            isEmailSelected = if (isChecked) {
                lytEmailMessage.setVisibility(View.VISIBLE)
                true
            } else {
                lytEmailMessage.setVisibility(View.GONE)
                false
            }
        })
        edtEmailSettings = findViewById(R.id.edt_sku_name)
        txtRemainingCharacters = findViewById(R.id.txt_characters_remaining)
        edtEmailSettings.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (!StringHelper.isStringNullOrEmpty(
                        edtEmailSettings.getText().toString()
                    ) && edtEmailSettings.getText().length > 0
                ) {
                    txtRemainingCharacters.setText(
                        String.format(
                            getString(R.string.txt_characters_remaining),
                            300 - edtEmailSettings.getText().length
                        )
                    )
                } else {
                    txtRemainingCharacters.setText(
                        String.format(
                            getString(R.string.txt_characters_remaining),
                            300
                        )
                    )
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        customLoadingViewWhite = findViewById(R.id.spin_kit_loader_product_list)
        txtHeader = findViewById(R.id.txt_header)
        btnCancel = findViewById(R.id.btn_cancel)
        btnCancel.setOnClickListener(View.OnClickListener { dialogConfirmLogout() })
        txtAddNotes = findViewById(R.id.txt_add_notes)
        btnSave = findViewById(R.id.txt_add_order)
        lytAddNotes = findViewById(R.id.lyt_notes)
        txtReceivedDateHeader = findViewById(R.id.txt_received_on)
        txtReceivedDateValue = findViewById(R.id.txt_received_date)
        lytReceivedDate = findViewById(R.id.lyt_received_Date)
        lytReceivedDate.setOnClickListener(View.OnClickListener {
            val mDate = Date(receivedDate * 1000)
            myCalendar = Calendar.getInstance(DateHelper.marketTimeZone)
            myCalendar.setTime(mDate)
            val datePickerDialog = DatePickerDialog(
                this@GoodsReceivedNoteDashBoardActivity, dateListener, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            //following line to restrict future date selection
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        })
        if (mOrder != null) {
            if (mOrder!!.emailStatusDetails != null && mOrder!!.emailStatusDetails?.size!! > 0) {
                lytEmailToSupplier.setVisibility(View.VISIBLE)
                val emails = StringBuilder()
                for (i in mOrder!!.emailStatusDetails?.indices!!) {
                    if (i != mOrder!!.emailStatusDetails?.size?.minus(1)) {
                        emails.append(mOrder!!.emailStatusDetails!![i].emailId).append(", ")
                    } else {
                        emails.append(mOrder!!.emailStatusDetails!![i].emailId)
                    }
                }
                SupplierEmails = emails.toString() + ""
                txtEmailList.setText(
                    String.format(
                        resources.getString(R.string.txt_will_be_sent_to),
                        emails
                    )
                )
            } else {
                lytEmailToSupplier.setVisibility(View.GONE)
            }
            val timeDelivered = (mOrder!!.timeDelivered?.minus(3600)?.times(1000))
            val currentTime = System.currentTimeMillis()
            if (currentTime < timeDelivered!!) {
                val dateTime = getDateInFullDayDateMonthYearFormat(
                    currentTime / 1000,
                    mOrder!!.outlet?.timeZoneFromOutlet
                )
                txtReceivedDateValue.text = dateTime
                receivedDate = currentTime / 1000
            } else {
                val dateTime = getDateInFullDayDateMonthYearFormat(
                    mOrder?.timeDelivered!!,
                    mOrder?.outlet?.timeZoneFromOutlet
                )
                txtReceivedDateValue.text = dateTime
                receivedDate = mOrder?.timeDelivered!!
            }

        }
        lstSkus = findViewById(R.id.lst_skus)
        lstSkus.setLayoutManager(LinearLayoutManager(this))
        grNskusAdapter = GRNskusAdapter(
            this@GoodsReceivedNoteDashBoardActivity,
            mOrder!!.products as MutableList<Product>,
            object : SelectedSKUSListener {
                override fun onSKUSelected() {
                    selectedItemsCount = selectedItemsCount + 1
                    updateCountState()
                }

                override fun onSKUdeSelected() {
                    selectedItemsCount = selectedItemsCount - 1
                    updateCountState()
                }
            })
        lstSkus.setAdapter(grNskusAdapter)
        initOrderListingSwipe()
        txtNumberOfItemsSelected = findViewById(R.id.txt_items_selected)
        txtSelectAll = findViewById(R.id.txt_select_all)
        txtSelectAll.setOnClickListener(View.OnClickListener {
            if (txtSelectAll.getText().toString()
                    .equals(resources.getString(R.string.txt_select_all), ignoreCase = true)
            ) {
                isSelectAllClicked = true
                txtSelectAll.setText(resources.getString(R.string.txt_deselect_all))
            } else {
                isSelectAllClicked = false
                txtSelectAll.setText(resources.getString(R.string.txt_select_all))
            }
            if (grNskusAdapter != null) {
                grNskusAdapter!!.selectAllProducts(isSelectAllClicked)
            }
        })
        txtAddNew = findViewById(R.id.txt_add_new)
        lytAddNew = findViewById(R.id.lyt_add_new)
        lytAddNew.setOnClickListener(View.OnClickListener {
            val newIntent =
                Intent(this@GoodsReceivedNoteDashBoardActivity, GRNProductListActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, mOrder!!.outlet?.outletId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, mOrder!!.supplier?.supplierId)
            val supplierDetailsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                mOrder!!.supplier
            )
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailsJson)
            newIntent.putExtra(
                ZeemartAppConstants.ORDER_DETAILS_JSON,
                ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
            )
            startActivityForResult(newIntent, REQUEST_CODE_SELECTED_FILTERS)
        })
        txtInvoiceNameHeader = findViewById(R.id.txt_upload_invoice_header)
        lytSelectPhotos = findViewById(R.id.lyt_add_images)
        txtSelectPhoto = findViewById(R.id.txt_select)
        txtIncoicesSub = findViewById(R.id.txt_invoices_sub)
        lytSelectPhotos.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logGuestAction(
                this@GoodsReceivedNoteDashBoardActivity,
                AnalyticsHelper.TAP_GRN_PAGE_SELECT_PHOTO,
                mOrder!!
            )
            val newIntent =
                Intent(this@GoodsReceivedNoteDashBoardActivity, InvoiceCameraPreview::class.java)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, ZeemartAppConstants.CALLED_FROM_GRN)
            startActivityForResult(
                newIntent,
                ZeemartAppConstants.RequestCode.GoodsReceivedNoteDashBoardActivity
            )
        })
        lstPhotos = findViewById(R.id.lst_images)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        lstPhotos.setLayoutManager(layoutManager)
        lytListPhotos = findViewById(R.id.lyt_image_list)
        lytListPhotos.setVisibility(View.GONE)
        setFont()
        updateCountState()
        imgCamera = findViewById(R.id.img_camera)
        imgCamera.setOnClickListener(View.OnClickListener {
            val newIntent =
                Intent(this@GoodsReceivedNoteDashBoardActivity, InvoiceCameraPreview::class.java)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, ZeemartAppConstants.CALLED_FROM_GRN)
            startActivityForResult(
                newIntent,
                ZeemartAppConstants.RequestCode.GoodsReceivedNoteDashBoardActivity
            )
        })
        txtSelectPhoto.setOnClickListener(View.OnClickListener {
            val newIntent =
                Intent(this@GoodsReceivedNoteDashBoardActivity, InvoiceCameraPreview::class.java)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, ZeemartAppConstants.CALLED_FROM_GRN)
            startActivityForResult(
                newIntent,
                ZeemartAppConstants.RequestCode.GoodsReceivedNoteDashBoardActivity
            )
        })
        btnSave.setOnClickListener(View.OnClickListener {
            if (isEmailSelected) {
                if (StringHelper.isStringNullOrEmpty(edtEmailSettings.getText().toString())) {
                    edtEmailSettings.setError("Please add message")
                } else {
                    dialogConfirmSave()
                }
            } else {
                dialogConfirmSave()
            }
        })
    }

    private fun upLoadAllFiles() {
        customLoadingViewWhite!!.visibility = View.VISIBLE
        if (invoiceDataList != null && invoiceDataList!!.size > 0) {
            val images: MutableList<GrnRequest.Image> = ArrayList()
            val invoiceDataManager = InvoiceDataManager(this)
            val multipleFileUploadRequests: MutableList<MultipleFileUploadRequest> = ArrayList()
            for (i in 0 until invoiceDataList!!.size - 1) {
                val inQueueForUploadDataModel = invoiceDataList!![i]
                val deviceDimensions = CommonMethods.getDeviceDimensions(this)
                val file = File(inQueueForUploadDataModel.imageFilePath)
                var bitmap: Bitmap?
                bitmap = if (inQueueForUploadDataModel.isInvoiceSelectedIsImage) {
                    invoiceDataManager.decodeSampledBitmapFromImageUri(
                        Uri.fromFile(file),
                        deviceDimensions.deviceWidth,
                        deviceDimensions.deviceHeight
                    )
                } else {
                    null
                }
                inQueueForUploadDataModel.status =
                    InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING
                val filePathSplitArray =
                    inQueueForUploadDataModel.imageFilePath?.split("/".toRegex())
                        ?.dropLastWhile { it.isEmpty() }
                        ?.toTypedArray()
                val filename = filePathSplitArray?.get(filePathSplitArray?.size?.minus(1)!!)
                val multipleFileUploadRequest = MultipleFileUploadRequest()
                multipleFileUploadRequest.bitmap = bitmap
                multipleFileUploadRequest.uri = Uri.fromFile(file)
                multipleFileUploadRequest.fileName = filename
                multipleFileUploadRequests.add(multipleFileUploadRequest)
                if (multipleFileUploadRequests.size == invoiceDataList!!.size - 1) {
                    uploadMultipleFileMultipart(
                        this,
                        multipleFileUploadRequests,
                        object : MultiPartImageUploaded {
                            override fun result(imageUploads: MultiPartImageUploadResponse?) {
                                customLoadingViewWhite!!.visibility = View.GONE
                                if (imageUploads != null && imageUploads.data != null && imageUploads.data!!
                                        .files != null
                                ) {
                                    for (j in 0 until imageUploads.data!!.files.size) {
                                        val strings: MutableList<String> = ArrayList()
                                        strings.add(
                                            imageUploads.data!!.files.get(j).fileName!!
                                        )
                                        val image = GrnRequest.Image()
                                        image.imageFileNames = strings
                                        image.imageURL =
                                            imageUploads.data!!.files.get(j).fileUrl
                                                ?.substring(
                                                    0,
                                                    imageUploads.data!!.files.get(j)
                                                        .fileUrl?.lastIndexOf(
                                                            File.separator
                                                        )!!
                                                ) + "/"
                                        images.add(image)
                                        if (images.size == invoiceDataList!!.size - 1) {
                                            Log.e(
                                                "images",
                                                "result: " + ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    images
                                                )
                                            )
                                            callSaveOrdersApi(images)
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        } else {
            callSaveOrdersApi(null)
        }
    }

    private fun uploadFiles() {
        customLoadingViewWhite!!.visibility = View.VISIBLE
        if (invoiceDataList != null && invoiceDataList!!.size > 0) {
            val images: MutableList<GrnRequest.Image> = ArrayList()
            val invoiceDataManager = InvoiceDataManager(this)
            for (i in 0 until invoiceDataList!!.size - 1) {
                val inQueueForUploadDataModel = invoiceDataList!![i]
                val deviceDimensions = CommonMethods.getDeviceDimensions(this)
                val file = File(inQueueForUploadDataModel.imageFilePath)
                var bitmap: Bitmap?
                bitmap = if (inQueueForUploadDataModel.isInvoiceSelectedIsImage) {
                    invoiceDataManager.decodeSampledBitmapFromImageUri(
                        Uri.fromFile(file),
                        deviceDimensions.deviceWidth,
                        deviceDimensions.deviceHeight
                    )
                } else {
                    null
                }
                inQueueForUploadDataModel.status =
                    InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING
                val filePathSplitArray =
                    inQueueForUploadDataModel.imageFilePath?.split("/".toRegex())
                        ?.dropLastWhile { it.isEmpty() }
                        ?.toTypedArray()
                val filename = filePathSplitArray?.get(filePathSplitArray.size?.minus(1)!!)
                UploadFileMultipart(
                    this,
                    bitmap,
                    Uri.fromFile(file),
                    "INVOICE",
                    filename!!,
                    object : MultiPartImageUploaded {
                        override fun result(imageUploads: MultiPartImageUploadResponse?) {
                            if (imageUploads != null && imageUploads.data != null && imageUploads.data!!
                                    .files != null
                            ) {
                                val strings: MutableList<String> = ArrayList()
                                strings.add(imageUploads.data!!.files.get(0).fileName!!)
                                val image = GrnRequest.Image()
                                image.imageFileNames = strings
                                image.imageURL =
                                    imageUploads.data!!.files.get(0).fileUrl?.substring(
                                        0,
                                        imageUploads.data!!.files.get(0).fileUrl
                                            ?.lastIndexOf(
                                                File.separator
                                            )!!
                                    ) + "/"
                                images.add(image)
                                if (images.size == invoiceDataList!!.size - 1) {
                                    Log.e(
                                        "images",
                                        "result: " + ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                            images
                                        )
                                    )
                                    callSaveOrdersApi(images)
                                }
                            }
                        }
                    })
            }
        } else {
            callSaveOrdersApi(null)
        }
    }

    private fun callSaveOrdersApi(images: List<GrnRequest.Image>?) {
        val products: MutableList<GrnRequest.Product> = ArrayList()
        val customLineItems: MutableList<CustomLineItem> = ArrayList()
        val grnRequest = GrnRequest()
        grnRequest.timeReceived = receivedDate
        grnRequest.orderId = mOrder!!.orderId
        grnRequest.noteToSupplier = edtEmailSettings!!.text.toString()
        grnRequest.isSendEmailToSupplier = isEmailSelected
        grnRequest.supplierNotificationEmail = SupplierEmails
        for (i in mOrder!!.products?.indices!!) {
            if (mOrder!!.products!![i].isSelected) {
                if (mOrder!!.products!![i].isCustom) {
                    val customLineItem = CustomLineItem()
                    customLineItem.productName = mOrder!!.products!![i].productName
                    customLineItem.sku = i.toString() + ""
                    customLineItem.quantity = mOrder!!.products!![i].quantity
                    customLineItem.unitSize = mOrder!!.products!![i].unitSize
                    customLineItem.categoryPath = ""
                    customLineItems.add(customLineItem)
                } else {
                    val product = GrnRequest.Product()
                    product.productName = mOrder!!.products!![i].productName
                    product.sku = mOrder!!.products!![i].sku
                    product.quantity = mOrder!!.products!![i].quantity
                    product.unitSize = mOrder!!.products!![i].unitSize
                    if (!StringHelper.isStringNullOrEmpty(mOrder!!.products!![i].customName)) {
                        product.customName = mOrder!!.products!![i].customName
                    }
                    product.categoryPath = ""
                    products.add(product)
                }
            }
        }
        grnRequest.customLineItems = customLineItems
        grnRequest.products = products
        grnRequest.notes = ""
        if (images != null && images.size > 0) {
            grnRequest.images = images
        }
        AnalyticsHelper.logGuestAction(
            this@GoodsReceivedNoteDashBoardActivity,
            AnalyticsHelper.TAP_GRN_PAGE_SAVE_GRN,
            images,
            products,
            customLineItems,
            receivedDate,
            mOrder!!
        )
        Log.e(
            "Request body",
            "callSaveOrdersApi: " + ZeemartBuyerApp.gsonExposeExclusive.toJson(grnRequest)
        )
        GRNApi.saveGRN(
            this,
            ZeemartBuyerApp.gsonExposeExclusive.toJson(grnRequest),
            mOrder!!.orderId,
            mOrder!!.outlet!!,
            object : saveGRNResponseListener {
                override fun onSuccessResponse(response: String?) {
                    Log.e("Response", "onSuccessResponse: $response")
                    customLoadingViewWhite!!.visibility = View.GONE
                    if (response != null) {
                        val dialogSuccess = alertDialogPaymentSuccess(
                            this@GoodsReceivedNoteDashBoardActivity, getString(
                                R.string.txt_saved
                            )
                        )
                        Handler().postDelayed({
                            dialogSuccess.dismiss()
                            finish()
                        }, 3000)
                        val intentNotification = Intent(NotificationConstants.BROADCAST_GRN_RECEIVE)
                        LocalBroadcastManager.getInstance(this@GoodsReceivedNoteDashBoardActivity)
                            .sendBroadcast(intentNotification)
                    } else {
                        customLoadingViewWhite!!.visibility = View.GONE
                        val dialogFailure = alertDialogPaymentFailure(
                            this@GoodsReceivedNoteDashBoardActivity, getString(
                                R.string.txt_failed
                            ), ""
                        )
                        dialogFailure.show()
                        Handler().postDelayed({ dialogFailure.dismiss() }, 3000)
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    Log.e("Response", "onSuccessResponse: " + error?.message)
                    customLoadingViewWhite!!.visibility = View.GONE
                    val dialogFailure = alertDialogPaymentFailure(
                        this@GoodsReceivedNoteDashBoardActivity, getString(
                            R.string.txt_failed
                        ), ""
                    )
                    dialogFailure.show()
                    Handler().postDelayed({ dialogFailure.dismiss() }, 3000)
                }
            })
    }

    private fun updateLabel() {
        val dateTime = DateHelper.getDateInFullDayDateMonthYearFormat(
            myCalendar!!.timeInMillis / 1000,
            mOrder!!.outlet?.timeZoneFromOutlet
        )
        txtReceivedDateValue!!.text = dateTime
        receivedDate = myCalendar!!.timeInMillis / 1000
    }

    private fun updateCountState() {
        if (selectedItemsCount != 0 && selectedItemsCount == mOrder!!.products?.size) {
            txtSelectAll!!.text = resources.getString(R.string.txt_deselect_all)
        } else {
            txtSelectAll!!.text = resources.getString(R.string.txt_select_all)
        }
        if (selectedItemsCount == 1) {
            txtNumberOfItemsSelected!!.text = String.format(
                resources.getString(R.string.txt_item_selected),
                selectedItemsCount
            )
        } else {
            txtNumberOfItemsSelected!!.text = String.format(
                resources.getString(R.string.txt_items_selected),
                selectedItemsCount
            )
        }
        if (selectedItemsCount > 0) {
            btnSave!!.isClickable = true
            btnSave!!.background = resources.getDrawable(R.drawable.btn_rounded_green)
            btnSave!!.text = String.format(
                resources.getString(R.string.txt_save_items),
                selectedItemsCount
            )
        } else {
            btnSave!!.isClickable = false
            btnSave!!.text = resources.getString(R.string.txt_save)
            btnSave!!.background = resources.getDrawable(R.drawable.btn_rounded_dark_grey)
        }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtInvoiceNameHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNumberOfItemsSelected,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddNotes,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSave,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceivedDateValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddNew,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceivedDateHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectAll,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectPhoto,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtIncoicesSub,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDidntReceivedHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEmailThisOrderSwitch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRemainingCharacters,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            edtEmailSettings,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEmailList,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == OrderDetailsActivity.IS_STATUS_CHANGED) {
            var product: Product? = null
            val bundle = data!!.extras
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.SELECTED_PRODUCT_MAP)) {
                    product = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        data.extras!!.getString(ZeemartAppConstants.SELECTED_PRODUCT_MAP),
                        Product::class.java
                    )
                }
            }
            if (product != null) {
                for (i in mOrder!!.products?.indices!!) {
                    if (mOrder!!.products!![i].productName == product.productName && !StringHelper.isStringNullOrEmpty(
                            product.sku
                        ) && !StringHelper.isStringNullOrEmpty(
                            mOrder!!.products!![i].sku
                        ) && mOrder!!.products!![i].sku == product.sku
                    ) {
                        if (mOrder!!.products!![i].isSelected) {
                            selectedItemsCount = selectedItemsCount - 1
                        }
                        mOrder!!.products?.remove(mOrder!!.products!![i])
                        break
                    }
                }
                mOrder!!.products?.add(product)
                selectedItemsCount = selectedItemsCount + 1
                updateCountState()
                lstSkus!!.adapter!!.notifyDataSetChanged()
            }
        } else if (resultCode == ZeemartAppConstants.ResultCode.RESULT_IMAGES) {
            val bundle = data!!.extras
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.INVOICE_IMAGE_URI)) {
                    val uriListString = bundle.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                    if (invoiceDataList != null && invoiceDataList!!.size > 0) {
                        invoiceDataList!!.removeAt(invoiceDataList!!.size - 1)
                        val invoiceData =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<InQueueForUploadDataModel>>(
                                uriListString,
                                object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                            )
                        invoiceDataList!!.addAll(invoiceData)
                    } else {
                        invoiceDataList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            uriListString,
                            object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                        )
                    }
                    if (invoiceDataList != null && invoiceDataList!!.size > 0) {
                        val inQueueForUploadDataModel = InQueueForUploadDataModel()
                        inQueueForUploadDataModel.isNewAdded = true
                        invoiceDataList!!.add(inQueueForUploadDataModel)
                        lytListPhotos!!.visibility = View.VISIBLE
                        lytSelectPhotos!!.visibility = View.GONE
                        lstPhotos.adapter =
                            GrnImageListAdapter(this@GoodsReceivedNoteDashBoardActivity,
                                invoiceDataList!!, object : GrnImageListAdapter.onNewCameraClicked_ {
                                   override fun onNewCameraClicked() {
                                        val newIntent = Intent(
                                            this@GoodsReceivedNoteDashBoardActivity,
                                            InvoiceCameraPreview::class.java
                                        )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.CALLED_FROM,
                                            ZeemartAppConstants.CALLED_FROM_GRN
                                        )
                                        startActivityForResult(
                                            newIntent,
                                            ZeemartAppConstants.RequestCode.GoodsReceivedNoteDashBoardActivity
                                        )
                                    }

                                    override fun onViewImageClicked(inQueueForUploadDataModel: InQueueForUploadDataModel?) {
                                        val newIntent = Intent(
                                            this@GoodsReceivedNoteDashBoardActivity,
                                            GrnImageViewActivity::class.java
                                        )
                                        val invoiceImageListJson =
                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                inQueueForUploadDataModel
                                            )
                                        newIntent.putExtra(
                                            ZeemartAppConstants.INVOICE_IMAGE_URI,
                                            invoiceImageListJson
                                        )
                                        startActivityForResult(
                                            newIntent,
                                            ZeemartAppConstants.RequestCode.GoodsReceivedNoteDashBoardActivity
                                        )
                                    }
                                })
                    } else {
                        lytListPhotos!!.visibility = View.GONE
                        lytSelectPhotos!!.visibility = View.VISIBLE
                    }
                }
            }
        } else if (resultCode == ZeemartAppConstants.ResultCode.RESULT_DELETED) {
            val bundle = data!!.extras
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.INVOICE_IMAGE_URI)) {
                    val uriListString = bundle.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                    val inQueueForUploadDataModel = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        uriListString,
                        InQueueForUploadDataModel::class.java
                    )
                    if (inQueueForUploadDataModel != null) {
                        for (i in invoiceDataList!!.indices) {
                            if (invoiceDataList!![i].imageFilePath == inQueueForUploadDataModel.imageFilePath) {
                                invoiceDataList!!.remove(invoiceDataList!![i])
                                break
                            }
                        }
                    }
                    lstPhotos!!.adapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onBackPressed() {
        dialogConfirmLogout()
    }

    private fun dialogConfirmLogout() {
        val builder = AlertDialog.Builder(this@GoodsReceivedNoteDashBoardActivity)
        builder.setPositiveButton(R.string.txt_leave) { dialog, which ->
            dialog.dismiss()
            finish()
        }
        builder.setNegativeButton(R.string.txt_stay_here) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setTitle(getString(R.string.txt_leave_this_page))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(getString(R.string.txt_changes_discard))
        dialog.show()
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.setTextColor(resources.getColor(R.color.text_blue))
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(resources.getColor(R.color.pinky_red))
    }

    private fun dialogConfirmSave() {
        val builder = AlertDialog.Builder(this@GoodsReceivedNoteDashBoardActivity)
        builder.setPositiveButton(R.string.txt_save) { dialog, which -> uploadFiles() }
        builder.setNegativeButton(R.string.txt_cancel) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setTitle(getString(R.string.txt_save_this_goods))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun initOrderListingSwipe() {
        val orderListingSwipehelper: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val background = ColorDrawable()
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val mAdapter = lstSkus!!.adapter as GRNskusAdapter?
                if (mAdapter != null) {
                    if (direction == ItemTouchHelper.LEFT) {
                        mAdapter.removeAt(viewHolder.adapterPosition)
                    }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                return ItemTouchHelper.LEFT
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
                val mViewHolder = viewHolder as GRNskusAdapter.ViewHolder
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX <= 0) // Swiping Left
                {
                    val bmp = BitmapFactory.decodeResource(
                        resources,
                        R.drawable.whitedelete
                    )
                    drawLeftSwipeButton(
                        background,
                        c,
                        dX,
                        mViewHolder,
                        getString(R.string.txt_delete),
                        bmp
                    )
                }
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
        val helperActionRequired = ItemTouchHelper(orderListingSwipehelper)
        helperActionRequired.attachToRecyclerView(lstSkus)
    }

    /**
     * @param background
     * @param c
     * @param dX
     * @param mViewHolder
     * @param text
     * @param bmp
     */
    private fun drawLeftSwipeButton(
        background: ColorDrawable, c: Canvas,
        dX: Float, mViewHolder: GRNskusAdapter.ViewHolder, text: String, bmp: Bitmap,
    ) {
        //Show reject button
        val color = resources.getColor(R.color.pinky_red)
        background.color = color
        background.setBounds(
            mViewHolder.itemView.right + dX.toInt(),
            mViewHolder.itemView.top,
            mViewHolder.itemView.right,
            mViewHolder.itemView.bottom
        )
        background.draw(c)
        val p = Paint()
        val buttonWidth =
            mViewHolder.itemView.right - ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_WIDTH
        val rightButton = RectF(
            buttonWidth.toFloat(),
            mViewHolder.itemView.top.toFloat(),
            mViewHolder.itemView.right.toFloat(),
            mViewHolder.itemView.bottom.toFloat()
        )
        p.color = color
        c.drawRect(rightButton, p)
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_TEXT_SIZE.toFloat()
        val textWidth = p.measureText(text)
        val bounds = Rect()
        p.getTextBounds(text, 0, text.length, bounds)
        val combinedHeight =
            (bmp.height + ViewOrderListAdapter.ViewHolderItem.SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
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
    }

    companion object {
        private const val REQUEST_CODE_SELECTED_FILTERS = 101
    }
}