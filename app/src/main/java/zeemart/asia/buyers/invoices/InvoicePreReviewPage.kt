package zeemart.asia.buyers.invoices

import android.Manifest
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.yongchun.library.view.ImageSelectorActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper.displayErrorMessageDialog
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.network.InvoiceHelper
import java.io.*
import java.util.*

class InvoicePreReviewPage : AppCompatActivity(), View.OnClickListener {
    private lateinit var pager: ViewPager
    private var imageUriRotationList: ArrayList<InQueueForUploadDataModel>? = null
    private var allInvoices: ArrayList<InQueueForUploadDataModel>? = null
    private lateinit var btnCloseReviewScreen: ImageButton
    private lateinit var btnDelete: ImageButton
    private var txtTotaluploads: TextView? = null
    private lateinit var txtInvoiceImageNoIndicator: TextView
    private var outlet: Outlet? = null
    private var calledFrom: String? = null
    private lateinit var lytUnMerge: LinearLayout
    private var txtUnMergeInvoices: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_pre_review_page)
        if (intent.extras != null) {
            calledFrom = intent.extras!!.getString(ZeemartAppConstants.CALLED_FROM)
        }
        pager = findViewById(R.id.view_pager_invoice_image_review)
        btnCloseReviewScreen = findViewById(R.id.img_btn_cancel_review)
        btnCloseReviewScreen.setOnClickListener(this)
        btnDelete = findViewById(R.id.img_btn_delete_invoice)
        btnDelete.setOnClickListener(this)
        txtTotaluploads = findViewById(R.id.txt_no_of_invoices_upload)
        setTypefaceView(txtTotaluploads, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        txtInvoiceImageNoIndicator = findViewById(R.id.txt_invoice_image_no_indicator)
        setTypefaceView(
            txtInvoiceImageNoIndicator,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        lytUnMerge = findViewById(R.id.lyt_un_merge)
        lytUnMerge.setOnClickListener(this)
        txtUnMergeInvoices = findViewById(R.id.txt_merge_invoices)
        setTypefaceView(txtUnMergeInvoices, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        outlet = SharedPref.defaultOutlet
        if (intent.extras != null) {
            if (intent.extras!!.containsKey(ZeemartAppConstants.INVOICE_IMAGE_URI)) {
                val uriListString = intent.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                imageUriRotationList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    uriListString,
                    object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                )
            }
            if (intent.extras!!.containsKey(ZeemartAppConstants.ALL_SELECTED_INVOICES)) {
                val uriListString =
                    intent.extras!!.getString(ZeemartAppConstants.ALL_SELECTED_INVOICES)
                allInvoices = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    uriListString,
                    object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                )
            }
        }
        if (isStoragePermissionGranted) {
            pager.setAdapter(CreateInvoiceImagesSwipeAdapter(this, imageUriRotationList!!))
        }
        setTitleAndTotalUploads()
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (imageUriRotationList!!.size > 1) {
                    lytUnMerge.setVisibility(View.VISIBLE)
                } else {
                    lytUnMerge.setVisibility(View.GONE)
                }
            }

            override fun onPageSelected(position: Int) {
                txtInvoiceImageNoIndicator.setText((pager.getCurrentItem() + 1).toString() + " of " + imageUriRotationList!!.size)
                //                if (imageUriRotationList.get(position).isInvoiceSelectedIsImage()) {
//                    btnCrop.setVisibility(View.VISIBLE);
//                } else {
//                    btnCrop.setVisibility(View.GONE);
//                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setTitleAndTotalUploads() {
        if (imageUriRotationList != null && imageUriRotationList!!.size > 0) {
            txtInvoiceImageNoIndicator!!.text =
                (pager!!.currentItem + 1).toString() + " of " + imageUriRotationList!!.size
        }
        if (imageUriRotationList != null && imageUriRotationList!!.size > 1) {
            txtTotaluploads!!.visibility = View.VISIBLE
            txtTotaluploads!!.text = imageUriRotationList!!.size.toString() + ""
        } else {
            txtTotaluploads!!.visibility = View.INVISIBLE
        }
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
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("", "Permission: " + permissions[0] + "was " + grantResults[0])
            //resume tasks needing this permission
            pager!!.adapter = CreateInvoiceImagesSwipeAdapter(this, imageUriRotationList!!)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_btn_cancel_review -> {
                val intent = Intent()
                val invoiceImageList = ZeemartBuyerApp.gsonExposeExclusive.toJson(allInvoices)
                intent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageList)
                setResult(ZeemartAppConstants.ResultCode.RESULT_DELETED, intent)
                finish()
                overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
            }
            R.id.lyt_un_merge -> dialogConfirmUnMerge()
            R.id.img_btn_delete_invoice -> {
                val file = File(imageUriRotationList!![pager!!.currentItem].imageDirectoryPath)
                val isDeleted = InvoiceDataManager(this@InvoicePreReviewPage).deleteDirectory(file)
                Log.d("FILE DELETED", "$isDeleted***")
                var i = 0
                while (i < allInvoices!!.size) {
                    if (allInvoices!![i].imageFilePath == imageUriRotationList!![pager!!.currentItem].imageFilePath && allInvoices!![i].imageDirectoryPath == imageUriRotationList!![pager!!.currentItem].imageDirectoryPath) {
                        allInvoices!!.remove(allInvoices!![i])
                    }
                    i++
                }
                imageUriRotationList!!.removeAt(pager!!.currentItem)
                pager!!.adapter!!.notifyDataSetChanged()
                setTitleAndTotalUploads()
                if (imageUriRotationList!!.size == 0) {
                    val newIntent = Intent()
                    val invoiceImageListJson =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(allInvoices)
                    newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
                    newIntent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        ZeemartAppConstants.CALLED_FROM_REVIEW_INVOICE_IMAGE_ACTIVITY
                    )
                    setResult(ZeemartAppConstants.ResultCode.RESULT_DELETED, newIntent)
                    finish()
                } else {
                    val newIntent = Intent()
                    val invoiceImageListJson =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(allInvoices)
                    newIntent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        ZeemartAppConstants.CALLED_FROM_REVIEW_INVOICE_IMAGE_ACTIVITY
                    )
                    newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
                    setResult(ZeemartAppConstants.ResultCode.RESULT_DELETED, newIntent)
                    // finish();
                }
            }
            else -> {}
        }
    }

    private fun dialogConfirmUnMerge() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(R.string.txt_unmerge_ok) { dialog, which ->
            for (i in allInvoices!!.indices) {
                allInvoices!![i].isMerged = false
                allInvoices!![i].isInvoiceSelectedForDeleteOrMerge = false
            }
            dialog.dismiss()
            val newIntents = Intent()
            val invoicesList = ZeemartBuyerApp.gsonExposeExclusive.toJson(allInvoices)
            newIntents.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoicesList)
            setResult(ZeemartAppConstants.ResultCode.RESULT_MERGED, newIntents)
            finish()
            overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
        }
        builder.setNegativeButton(R.string.dialog_cancel_button_text) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setTitle(getString(R.string.txt_un_merge))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(getString(R.string.txt_unmerge_warning))
        dialog.show()
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
                    pager!!.adapter!!.notifyDataSetChanged()
                    pager!!.currentItem = 0
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
                    pager!!.adapter!!.notifyDataSetChanged()
                    pager!!.currentItem = 0
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
                val fileDest = File(imageUriRotationList!![pager!!.currentItem].imageFilePath)
                try {
                    copyFileData(fileSrc, fileDest)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
            val imageViewProduct = pager!!.findViewWithTag<View>(
                imageUriRotationList!![pager!!.currentItem]
            ).findViewById<ImageView>(R.id.image_product_product_details)
            val builder = Picasso.Builder(this)
            builder.listener { picasso, uri, exception ->
                Log.d("EXCEPTION", "****************************************")
                exception.printStackTrace()
            }
            builder.build().load(File(imageUriRotationList!![pager!!.currentItem].imageFilePath))
                .into(imageViewProduct)
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
        pager!!.adapter!!.notifyDataSetChanged()
        pager!!.currentItem = 0
    }

    private fun createInvoiceDataGallery(uri: Uri?, displayName: String?, filename: String) {
        val deviceDimensions = CommonMethods.getDeviceDimensions(this@InvoicePreReviewPage)
        val manager = InvoiceDataManager(this@InvoicePreReviewPage)
        val bitmap = manager.decodeSampledBitmapFromImageUri(
            uri!!,
            deviceDimensions.deviceWidth,
            deviceDimensions.deviceHeight
        )
        val imageInternalPaths = manager.saveInInternalStorage(
            bitmap!!,
            SharedPref.defaultOutlet?.outletId,
            filename
        )
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

    override fun onDestroy() {
        super.onDestroy()
        pager!!.adapter = null
    }

    private val noOfFilesThatCanBeUploaded: Int
        private get() = if (imageUriRotationList != null && imageUriRotationList!!.size > 0) {
            InvoiceDataManager.TOTAL_NO_OF_FILES_TO_BE_UPLOADED_AT_ONCE - imageUriRotationList!!.size
        } else {
            InvoiceDataManager.TOTAL_NO_OF_FILES_TO_BE_UPLOADED_AT_ONCE
        }

    override fun onBackPressed() {
        val newIntent = Intent()
        val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(allInvoices)
        newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
        setResult(ZeemartAppConstants.ResultCode.RESULT_DELETED, newIntent)
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