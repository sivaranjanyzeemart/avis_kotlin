package zeemart.asia.buyers.invoices

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.yongchun.library.view.ImageSelectorActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.AnalyticsHelper.logAction
import zeemart.asia.buyers.helper.DialogHelper.alertDialogSmallFailure
import zeemart.asia.buyers.helper.DialogHelper.alertDialogSmallSuccess
import zeemart.asia.buyers.helper.DialogHelper.displayErrorMessageDialog
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.ReviewInvoiceAdapter.OnInvoiceImageClick
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.invoice.Invoice.Create
import zeemart.asia.buyers.modelsimport.MultiPartImageUploadResponse
import zeemart.asia.buyers.network.ImageUploadHelper.MultiPartImageUploaded
import zeemart.asia.buyers.network.ImageUploadHelper.UploadFileMultipart
import zeemart.asia.buyers.network.InvoiceHelper
import zeemart.asia.buyers.network.InvoiceHelper.CreateInvoice
import java.io.*
import java.util.*

class ReviewInvoiceImageActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var lstPhotos: RecyclerView
    private lateinit var imageUriRotationList: ArrayList<InQueueForUploadDataModel>
    private lateinit var lstImagesForUpload: ArrayList<ImagesModel>
    private lateinit var btnCloseReviewScreen: ImageButton

    //    private ImageButton btnDelete;
    //private ImageButton btnAddMoreFromCamera;
    //private ImageButton btnAddMoreFromGallery;
    private lateinit var txtInvoiceImageNoIndicator: TextView
    private lateinit var outlet: Outlet
    private lateinit var calledFrom: String
    private lateinit var lytUpload: RelativeLayout
    private lateinit var lytMerge: LinearLayout
    private lateinit var txtMergeInvoice: TextView
    private lateinit var txtUploadInvoice: TextView
    private lateinit var lytToolTip: RelativeLayout
    private lateinit var txtToolTipHeader: TextView
    private lateinit var txtToolTipSubHeader: TextView
    private lateinit var orderId: String
    private lateinit var threeDotLoader: CustomLoadingViewWhite
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_invoice_image)
        if (intent.extras != null) {
            calledFrom = intent.extras!!.getString(ZeemartAppConstants.CALLED_FROM).toString()
        }
        if (!StringHelper.isStringNullOrEmpty(calledFrom)) {
            if (calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS) {
                orderId = intent.extras!!.getString(ZeemartAppConstants.ORDER_ID).toString()
            }
        }
        lstImagesForUpload = ArrayList()
        lstPhotos = findViewById(R.id.lst_photos)
        lstPhotos.setNestedScrollingEnabled(false)
        lstPhotos.setLayoutManager(GridLayoutManager(this, 2))
        btnCloseReviewScreen = findViewById(R.id.img_btn_cancel_review)
        btnCloseReviewScreen.setOnClickListener(this)
        lytUpload = findViewById(R.id.lyt_upload)
        lytUpload.setOnClickListener(this)
        lytToolTip = findViewById(R.id.lyt_tool_tip)
        txtToolTipHeader = findViewById(R.id.txt_use_filer)
        txtToolTipSubHeader = findViewById(R.id.txt_ok)
        setTypefaceView(txtToolTipHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtToolTipSubHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        txtToolTipSubHeader.setOnClickListener(View.OnClickListener {
            SharedPref.writeBool(SharedPref.DISPLAY_BUBBLE_INVOICE, false)
            lytToolTip.setVisibility(View.GONE)
        })
        //        btnDelete = findViewById(R.id.img_btn_delete_invoice);
//        btnDelete.setOnClickListener(this);
        txtMergeInvoice = findViewById(R.id.txt_merge_invoices)
        txtUploadInvoice = findViewById(R.id.txt_upload)
        setTypefaceView(txtMergeInvoice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtUploadInvoice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)


        /*btnAddMoreFromCamera = findViewById(R.id.img_btn_add_more_invoice);
        btnAddMoreFromCamera.setOnClickListener(this);
        btnAddMoreFromGallery = findViewById(R.id.img_btn_select_from_gallery);
        btnAddMoreFromGallery.setOnClickListener(this);*/lytMerge = findViewById(R.id.lyt_merge)
        lytMerge.setOnClickListener(this)
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_GRN) {
            lytMerge.setVisibility(View.GONE)
            lytToolTip.setVisibility(View.GONE)
        } else if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS) {
            lytMerge.setVisibility(View.GONE)
            lytToolTip.setVisibility(View.GONE)
        } else {
            lytMerge.setVisibility(View.VISIBLE)
            if (!SharedPref.readBool(SharedPref.DISPLAY_BUBBLE_INVOICE, true)!!) {
                lytToolTip.setVisibility(View.GONE)
            } else {
                lytToolTip.setVisibility(View.VISIBLE)
            }
        }
        txtInvoiceImageNoIndicator = findViewById(R.id.txt_invoice_image_no_indicator)
        setTypefaceView(
            txtInvoiceImageNoIndicator,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        outlet = SharedPref.defaultOutlet!!
        if (intent.extras != null) {
            val uriListString = intent.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
            imageUriRotationList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                uriListString,
                object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
            )
        }
        if (isStoragePermissionGranted) {
            setAdapter()
        }
        threeDotLoader = findViewById(R.id.spin_kit_loader_white)
        threeDotLoader.setVisibility(View.GONE)
        setTitleAndTotalUploads()
    }

    private fun setTitleAndTotalUploads() {
        if (imageUriRotationList != null && imageUriRotationList!!.size > 0) {
            var MergedCounter = 0
            var NonMergedCount = 0
            for (i in imageUriRotationList!!.indices) {
                if (imageUriRotationList!![i].isMerged) {
                    MergedCounter++
                } else {
                    NonMergedCount = NonMergedCount + 1
                }
            }
            if (MergedCounter != 0) {
                NonMergedCount = NonMergedCount + 1
            }
            if (NonMergedCount == 1) {
                txtInvoiceImageNoIndicator!!.text = String.format(
                    resources.getString(R.string.txt_number_of_invoice_to_upload),
                    NonMergedCount
                )
            } else {
                txtInvoiceImageNoIndicator!!.text = String.format(
                    resources.getString(R.string.txt_number_of_invoices_to_upload),
                    NonMergedCount
                )
            }
        }
    }

    private fun setAdapter() {
        val UpdateInvoice = ArrayList<InQueueForUploadDataModel>()
        val mergedInvoices: MutableList<InQueueForUploadDataModel> = ArrayList()
        for (i in imageUriRotationList!!.indices) {
            if (imageUriRotationList!![i].isMerged) {
                mergedInvoices.add(imageUriRotationList!![i])
            } else {
                UpdateInvoice.add(imageUriRotationList!![i])
            }
        }
        if (mergedInvoices.size > 0) {
            UpdateInvoice.add(0, mergedInvoices[0])
            UpdateInvoice[0].imagesCount = mergedInvoices.size
        } else {
            UpdateInvoice[0].imagesCount = 0
        }
        lstPhotos!!.adapter = ReviewInvoiceAdapter(
            this@ReviewInvoiceImageActivity,
            false,
            UpdateInvoice,
            object : OnInvoiceImageClick {
                override fun onImageClicked(inQueueForUploadDataModel: InQueueForUploadDataModel?) {
                    val newIntent =
                        Intent(this@ReviewInvoiceImageActivity, InvoicePreReviewPage::class.java)
                    val imageUriRotationLists = ArrayList<InQueueForUploadDataModel>()
                    if (inQueueForUploadDataModel?.isMerged!!) {
                        for (i in imageUriRotationList!!.indices) {
                            if (imageUriRotationList!![i].isMerged) {
                                imageUriRotationLists.add(imageUriRotationList!![i])
                            }
                        }
                    } else {
                        imageUriRotationLists.add(inQueueForUploadDataModel!!)
                    }
                    val invoiceImageListJson =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationLists)
                    newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
                    newIntent.putExtra(
                        ZeemartAppConstants.ALL_SELECTED_INVOICES,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                    )
                    startActivityForResult(
                        newIntent,
                        ZeemartAppConstants.RequestCode.CameraPreviewActivityAdjustImage
                    )
                }

                override fun onFilterSelected(inQueueForUploadDataModel: InQueueForUploadDataModel?) {}
                override fun onFilterDeselected(inQueueForUploadDataModel: InQueueForUploadDataModel?) {}
            })
    }

    //permission is automatically granted on sdk<23 upon installation
    val isStoragePermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("", "Permission is granted")
                true
            } else {
                Log.v("", "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("", "Permission is granted")
            true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("", "Permission: " + permissions[0] + "was " + grantResults[0])
            setAdapter()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_btn_cancel_review -> {
                /*AlertDialog.Builder builder = new AlertDialog.Builder(ReviewInvoiceImageActivity.this);
                builder.setPositiveButton(R.string.txt_yes_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.txt_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage(getString(R.string.txt_cancel_upload, imageUriRotationList.size()));
                dialog.show();
                Button deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                deleteBtn.setTextColor(getResources().getColor(R.color.chart_red));
                deleteBtn.setAllCaps(false);
                Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                cancelBtn.setAllCaps(false);*/
                val intent = Intent()
                val invoiceImageList =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                intent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageList)
                setResult(ZeemartAppConstants.ResultCode.RESULT_CANCELED, intent)
                finish()
                overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
            }
            R.id.lyt_merge -> {
                logAction(
                    this@ReviewInvoiceImageActivity,
                    AnalyticsHelper.TAP_MERGE_INVOICES_REVIEW_INVOICES_SCREEN
                )
                val newIntent =
                    Intent(this@ReviewInvoiceImageActivity, MergeInvoicesActivity::class.java)
                val invoiceImageListJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
                startActivityForResult(
                    newIntent,
                    ZeemartAppConstants.RequestCode.CameraPreviewActivityAdjustImage
                )
            }
            R.id.lyt_upload -> {
                AnalyticsHelper.logActionL(
                    applicationContext,
                    AnalyticsHelper.TAP_UPLOAD_INVOICE,
                    imageUriRotationList as ArrayList<InQueueForUploadDataModel?>
                )
                //display a progress bar
                /*create serialized data file in internal storage for each of the image in background thread
                once complete call the InvoiceFragment class for update*/
                if (!StringHelper.isStringNullOrEmpty(
                        calledFrom
                    ) && calledFrom.equals(ZeemartAppConstants.CALLED_FROM_GRN, ignoreCase = true)
                ) {
                    val grnIntent = Intent()
                    val grnInvoiceImageList =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                    grnIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, grnInvoiceImageList)
                    setResult(ZeemartAppConstants.ResultCode.RESULT_IMAGES, grnIntent)
                    finish()
                } else if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom.equals(
                        ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS,
                        ignoreCase = true
                    )
                ) {
                    UploadInvoices()
                } else {
                    SerializeInvoiceStatusDataOnInternalStorage(imageUriRotationList).execute()
                }
            }
            else -> {}
        }
    }

    private fun UploadInvoices() {
        threeDotLoader!!.visibility = View.VISIBLE
        if (imageUriRotationList != null && imageUriRotationList!!.size > 0) {
            val invoiceDataManager = InvoiceDataManager(this)
            for (i in imageUriRotationList!!.indices) {
                val inQueueForUploadDataModel = imageUriRotationList!![i]
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
                        override fun result(imageUpload: MultiPartImageUploadResponse?) {
                            if (imageUpload != null && imageUpload.data != null && imageUpload.data!!.files != null && imageUpload.data!!.files.size > 0 && !StringHelper.isStringNullOrEmpty(
                                    imageUpload.data!!.files[0].fileUrl
                                )
                            ) {
                                val strings: MutableList<String?> = ArrayList()
                                strings.add(imageUpload.data!!.files[0].fileName)
                                val image = ImagesModel()
                                image.imageFileNames = strings as MutableList<String>
                                image.imageURL = imageUpload.data!!.files[0].fileUrl!!.substring(
                                    0, imageUpload.data!!.files[0].fileUrl!!.lastIndexOf(
                                        File.separator
                                    )
                                ) + "/"
                                lstImagesForUpload!!.add(image)
                                createInvoiceCall(image)
                            }
                        }
                    })
            }
        }
    }

    private fun createInvoiceCall(imageData: ImagesModel) {
        val ic = Create()
        val lstImages: MutableList<ImagesModel> = ArrayList()
        lstImages.add(imageData)
        ic.images = lstImages
        val defaultOutlet = SharedPref.defaultOutlet
        if (defaultOutlet != null) {
            ic.outlet = defaultOutlet
        }
        val createdByUserDetails = UserDetails()
        createdByUserDetails.firstName = SharedPref.read(SharedPref.USER_FIRST_NAME, "")
        createdByUserDetails.id = SharedPref.read(SharedPref.USER_ID, "")
        createdByUserDetails.imageURL = SharedPref.read(SharedPref.USER_IMAGE_URL, "")
        createdByUserDetails.lastName = SharedPref.read(SharedPref.USER_LAST_NAME, "")
        createdByUserDetails.phone = ""
        ic.createdBy = createdByUserDetails
        val orderIds = arrayOf(orderId)
        ic.orderIds = orderIds
        CreateInvoice(this@ReviewInvoiceImageActivity, ic, object : CreateInvoice {
            override fun result(response: Create.Response?) {
                if (imageUriRotationList!!.size == lstImagesForUpload!!.size) {
                    if (response != null) {
                        val dialogSuccess = alertDialogSmallSuccess(
                            this@ReviewInvoiceImageActivity, resources.getString(
                                R.string.txt_uploaded
                            )
                        )
                        Handler().postDelayed({
                            val grnIntent = Intent()
                            val invoiceImageList =
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                            grnIntent.putExtra(
                                ZeemartAppConstants.INVOICE_IMAGE_URI,
                                invoiceImageList
                            )
                            setResult(
                                ZeemartAppConstants.ResultCode.RESULT_ORDER_INVOICE,
                                grnIntent
                            )
                            finish()
                            dialogSuccess.dismiss()
                        }, 500)
                    } else {
                        val dialogFailure = alertDialogSmallFailure(
                            this@ReviewInvoiceImageActivity, getString(
                                R.string.txt_failed_to_upload
                            ), getString(R.string.txt_please_try_again)
                        )
                        dialogFailure.show()
                        Handler().postDelayed({ dialogFailure.dismiss() }, 500)
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_IMAGE_FROM_CAMERA_PREVIEW_ACTIVITY && resultCode == RESULT_OK) {
            if (data!!.extras != null) {
                val totalNoOfFilesRemaining = noOfFilesThatCanBeUploaded
                val uriListString = data.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                var invoiceImageDataList = ArrayList<InQueueForUploadDataModel>()
                if (invoiceImageDataList.size > 0 && invoiceImageDataList.size > totalNoOfFilesRemaining) {
                    displayErrorMessageDialog(
                        this,
                        null,
                        getString(
                            R.string.txt_can_add_only_these_more_files,
                            totalNoOfFilesRemaining
                        )
                    )
                } else {
                    if (!StringHelper.isStringNullOrEmpty(uriListString)) {
                        invoiceImageDataList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            uriListString,
                            object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                        )
                    }
                    if (imageUriRotationList!!.size > 0) {
                        imageUriRotationList!!.addAll(0, invoiceImageDataList)
                    } else {
                        imageUriRotationList!!.addAll(invoiceImageDataList)
                    }
                    setAdapter()
                    setTitleAndTotalUploads()
                }
            }
        }
        if (resultCode == ZeemartAppConstants.ResultCode.RESULT_MERGED) {
            if (data!!.extras != null) {
                val uriListString = data.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                var invoiceImageDataList: ArrayList<InQueueForUploadDataModel>? = ArrayList()
                if (!StringHelper.isStringNullOrEmpty(uriListString)) {
                    invoiceImageDataList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        uriListString,
                        object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                    )
                }
                imageUriRotationList = ArrayList()
                imageUriRotationList!!.addAll(invoiceImageDataList!!)
                for (i in imageUriRotationList!!.indices) {
                    imageUriRotationList!![i].isInvoiceSelectedForDeleteOrMerge = false
                }
                if (imageUriRotationList!!.size == 0) {
                    val newIntent = Intent()
                    setResult(ZeemartAppConstants.ResultCode.RESULT_CANCELED, newIntent)
                    finish()
                } else {
                    setAdapter()
                    setTitleAndTotalUploads()
                }
            }
        }
        if (resultCode == ZeemartAppConstants.ResultCode.RESULT_DELETED) {
            if (data!!.extras != null) {
                val uriListString = data.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                var invoiceImageDataList: ArrayList<InQueueForUploadDataModel>? = ArrayList()
                if (!StringHelper.isStringNullOrEmpty(uriListString)) {
                    invoiceImageDataList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        uriListString,
                        object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                    )
                }
                imageUriRotationList = ArrayList()
                imageUriRotationList!!.addAll(invoiceImageDataList!!)
                if (imageUriRotationList!!.size == 0) {
                    val newIntent = Intent()
                    setResult(ZeemartAppConstants.ResultCode.RESULT_CANCELED, newIntent)
                    finish()
                } else {
                    setAdapter()
                    setTitleAndTotalUploads()
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE) {
            val images =
                data!!.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT) as ArrayList<String>?
            Log.d("SELECTED IMAGE LIST", images!!.size.toString() + "*****" + images[0] + "****")
            if (images != null) {
                for (i in images.indices) {
                    val uri = Uri.fromFile(File(images[i]))
                    val displayName = getRealPathFromURI(uri)
                    val currentDate =
                        DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
                    val filename = currentDate + "_" + i.toString()
                    createInvoiceDataGallery(uri, displayName, filename)
                    setAdapter()
                }
                setTitleAndTotalUploads()
            }
        }
        if (requestCode == InvoiceHelper.PICK_FROM_FILES && resultCode == RESULT_OK) {
            var clipData: ClipData? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                clipData = data!!.clipData
            }
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val item = clipData.getItemAt(i)
                    val uri = item.uri
                    val currentDate =
                        DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
                    val filename = currentDate + "_" + i.toString()
                    val displayName = getRealPathFromURI(uri)
                    checkFileTypeSaveInvoiceToList(uri, i)
                }
            } else {
                val uri = data!!.data!!
                val index = 0
                checkFileTypeSaveInvoiceToList(uri, index)
            }
            setTitleAndTotalUploads()
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                val fileSrc = File(resultUri.path.toString())
                //                File fileDest = new File(imageUriRotationList.get(pager.getCurrentItem()).getImageFilePath());
//                try {
//                    copyFileData(fileSrc, fileDest);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
            //            ImageView imageViewProduct = pager.findViewWithTag(imageUriRotationList.get(pager.getCurrentItem())).findViewById(R.id.image_product_product_details);
            val builder = Picasso.Builder(this)
            builder.listener { picasso, uri, exception ->
                Log.d("EXCEPTION", "****************************************")
                exception.printStackTrace()
            }
            //            builder.build().load(new File(imageUriRotationList.get(pager.getCurrentItem()).getImageFilePath())).into(imageViewProduct);
        }
    }

    /**
     * checks the file type of the image uploaded,
     * if the file type is image(png,jpeg,jpg) upload it as image
     * if the type is file(pdf) upload it as file
     * anything other than pdf, png, jpeg,jpg is ignored.
     * and saves the invoice data to the list of invoices.
     *
     * @param uri
     * @param index
     * @return
     */
    fun checkFileTypeSaveInvoiceToList(uri: Uri?, index: Int) {
        val displayName = getRealPathFromURI(uri)
        val currentDate =
            DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        val filename = currentDate + "_" + index.toString()
        val type = CommonMethods.getFileExtension(uri, this)
        if (type.equals(InvoiceHelper.FileType.PDF.value, ignoreCase = true)) {
            createInvoiceDataPDF(uri, displayName, filename)
        } else if (type.equals(InvoiceHelper.FileType.PNG.value, ignoreCase = true) ||
            type.equals(InvoiceHelper.FileType.JPG.value, ignoreCase = true) ||
            type.equals(InvoiceHelper.FileType.JPEG.value, ignoreCase = true)
        ) {
            createInvoiceDataGallery(uri, displayName, filename)
        } else {
            getToastRed(getString(R.string.txt_only_pdf_image))
            return
        }
        setAdapter()
    }

    private fun createInvoiceDataGallery(uri: Uri?, displayName: String?, filename: String) {
        val deviceDimensions = CommonMethods.getDeviceDimensions(this@ReviewInvoiceImageActivity)
        val manager = InvoiceDataManager(this@ReviewInvoiceImageActivity)
        val bitmap = manager.decodeSampledBitmapFromImageUri(
            uri!!,
            deviceDimensions.deviceWidth,
            deviceDimensions.deviceHeight
        )
        val imageInternalPaths =
            manager.saveInInternalStorage(bitmap!!, SharedPref.defaultOutlet?.outletId, filename)
        val invoiceImageDataModel = InQueueForUploadDataModel()
        invoiceImageDataModel.imageDirectoryPath = imageInternalPaths.directoryPath
        invoiceImageDataModel.imageFilePath = imageInternalPaths.filePath
        invoiceImageDataModel.rotation = 0
        invoiceImageDataModel.selectedPdfOriginalName = displayName
        invoiceImageDataModel.isInvoiceSelectedIsImage = true
        invoiceImageDataModel.outletId = SharedPref.defaultOutlet?.outletId
        if (imageUriRotationList!!.size > 0) {
            imageUriRotationList!!.add(0, invoiceImageDataModel)
        } else {
            imageUriRotationList!!.add(invoiceImageDataModel)
        }
    }

    private fun createInvoiceDataPDF(uri: Uri?, displayName: String?, filename: String) {
        val manager = InvoiceDataManager(this)
        val imageInternalPaths =
            manager.savePdfInInternalStorage(SharedPref.defaultOutlet?.outletId, filename, uri)
        val invoiceImageDataModel = InQueueForUploadDataModel()
        invoiceImageDataModel.imageDirectoryPath = imageInternalPaths.directoryPath
        invoiceImageDataModel.imageFilePath = imageInternalPaths.filePath
        invoiceImageDataModel.rotation = 0
        invoiceImageDataModel.selectedPdfOriginalName = displayName
        invoiceImageDataModel.isInvoiceSelectedIsImage = false
        invoiceImageDataModel.outletId = SharedPref.defaultOutlet?.outletId
        val bytes = getBytesData(uri)
        if (bytes.size() < 2000000) {
            if (imageUriRotationList!!.size > 0) {
                imageUriRotationList!!.add(0, invoiceImageDataModel)
            } else {
                imageUriRotationList!!.add(invoiceImageDataModel)
            }
        } else {
            displayErrorMsg()
        }
    }

    @SuppressLint("Range")
    private fun getRealPathFromURI(uri: Uri?): String? {
        var displayName: String? = null
        val uriString = uri.toString()
        val myFile = File(uriString)
        if (uriString.startsWith("content://")) {
            var cursor: Cursor? = null
            try {
                cursor = this.contentResolver.query(uri!!, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    displayName =
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.name
        }
        return displayName
    }

    private fun displayErrorMsg() {
        getToastRed(getString(R.string.txt_file_upload_fail))
    }

    private fun getBytesData(uri: Uri?): ByteArrayOutputStream {
        val baos = ByteArrayOutputStream()
        val fis: InputStream?
        try {
            fis = contentResolver.openInputStream(uri!!)
            val buf = ByteArray(1024)
            var n: Int
            while (-1 != fis!!.read(buf).also { n = it }) baos.write(buf, 0, n)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return baos
    }

    internal inner class SerializeInvoiceStatusDataOnInternalStorage(private val invoiceDataList: ArrayList<InQueueForUploadDataModel>?) :
        AsyncTask<Void?, Void?, Void?>() {

        override fun doInBackground(vararg p0: Void?): Void? {
            for (i in invoiceDataList!!.indices) {
                val inQueueForUploadDataModel = invoiceDataList[i]
                inQueueForUploadDataModel.status = InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE

                /*write the serailaized InvoiceImageRotationModel object to a new file
                 in same directory where the image is saved*/
                val invoiceDataManager = InvoiceDataManager(this@ReviewInvoiceImageActivity)
                val infoFilePath = invoiceDataManager.saveInfoFileToInternalStorage(
                    invoiceDataList[i]
                )
                invoiceDataList[i].objectInfoFilePath = infoFilePath
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            val newIntent =
                Intent(this@ReviewInvoiceImageActivity, UploadInvoicesForNonZmOrders::class.java)
            val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                invoiceDataList
            )
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(newIntent)
            finish()
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val noOfFilesThatCanBeUploaded: Int
        private get() = if (imageUriRotationList != null && imageUriRotationList!!.size > 0) {
            InvoiceDataManager.TOTAL_NO_OF_FILES_TO_BE_UPLOADED_AT_ONCE - imageUriRotationList!!.size
        } else {
            InvoiceDataManager.TOTAL_NO_OF_FILES_TO_BE_UPLOADED_AT_ONCE
        }

    override fun onBackPressed() {
        val newIntent = Intent()
        val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
        newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
        setResult(ZeemartAppConstants.ResultCode.RESULT_CANCELED, newIntent)
        finish()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }

    companion object {
        private const val GET_IMAGE_FROM_CAMERA_PREVIEW_ACTIVITY = 101
        @Throws(IOException::class)
        fun copyFileData(src: File?, dst: File?) {
            val `in`: InputStream = FileInputStream(src)
            try {
                val out: OutputStream = FileOutputStream(dst)
                try {
                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                } finally {
                    out.close()
                }
            } finally {
                `in`.close()
            }
        }
    }
}