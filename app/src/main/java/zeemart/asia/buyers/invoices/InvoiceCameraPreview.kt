package zeemart.asia.buyers.invoices

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.gson.reflect.TypeToken
import com.yongchun.library.view.ImageSelectorActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.goodsReceivedNote.GoodsReceivedNoteDashBoardActivity
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper.displayErrorMessageDialog
import zeemart.asia.buyers.helper.camera.CameraPreviewActivity
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.network.InvoiceHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * Created by ParulBhandari on 7/10/2018.
 */
class InvoiceCameraPreview : CameraPreviewActivity() {
    private var calledFrom: String? = null
    private var orderId: String? = null
    private var invoiceImageList: MutableList<InQueueForUploadDataModel>? = ArrayList()
    private var noOfFilesThatCanBeAdded = 0
    private var imageDisplayRotation = 0
    private var inQueueForUploadDataModelCamera: InQueueForUploadDataModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.extras != null) {
            calledFrom = intent.extras!!.getString(ZeemartAppConstants.CALLED_FROM)
            if (intent.extras!!.containsKey(ZeemartAppConstants.ORDER_ID)) orderId = intent.extras!!
                .getString(ZeemartAppConstants.ORDER_ID)
            noOfFilesThatCanBeAdded =
                intent.extras!!.getInt(ZeemartAppConstants.NO_OF_FILES_THAT_CAN_BE_ADDED)
            if (noOfFilesThatCanBeAdded != 0) setNoOfMoreFilesToBeAdded(noOfFilesThatCanBeAdded)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == InvoiceHelper.PICK_FROM_GALLERY || requestCode == InvoiceHelper.PICK_FROM_FILES) && resultCode == RESULT_OK) {
            val bitmap = InvoiceDataManager(this).decodeSampledBitmapFromImageUri(
                Uri.fromFile(
                    File(
                        invoiceImageList!![0].imageFilePath
                    )
                ), 35, 35
            )
            var MergedCounter = 0
            var NonMergedCount = 0
            for (i in invoiceImageList!!.indices) {
                if (invoiceImageList!![i].isMerged) {
                    MergedCounter++
                } else {
                    NonMergedCount = NonMergedCount + 1
                }
            }
            if (MergedCounter != 0) {
                NonMergedCount = NonMergedCount + 1
            }
            setReviewImageIcon(bitmap, NonMergedCount)
        } else if (resultCode == RESULT_OK) {
            if (inQueueForUploadDataModelCamera != null && !StringHelper.isStringNullOrEmpty(
                    inQueueForUploadDataModelCamera!!.imageFilePath
                )
            ) {
                invoiceImageList!!.add(inQueueForUploadDataModelCamera!!)
                val bitmap = InvoiceDataManager(this).decodeSampledBitmapFromImageUri(
                    Uri.fromFile(
                        File(
                            inQueueForUploadDataModelCamera!!.imageFilePath
                        )
                    ), 35, 35
                )
                var MergedCounter = 0
                var NonMergedCount = 0
                for (i in invoiceImageList!!.indices) {
                    if (invoiceImageList!![i].isMerged) {
                        MergedCounter++
                    } else {
                        NonMergedCount = NonMergedCount + 1
                    }
                }
                if (MergedCounter != 0) {
                    NonMergedCount = NonMergedCount + 1
                }
                setReviewImageIcon(bitmap, NonMergedCount)
            } else {
                val bitmap = InvoiceDataManager(this).decodeSampledBitmapFromImageUri(
                    Uri.fromFile(
                        File(
                            invoiceImageList!![0].imageFilePath
                        )
                    ), 35, 35
                )
                var MergedCounter = 0
                var NonMergedCount = 0
                for (i in invoiceImageList!!.indices) {
                    if (invoiceImageList!![i].isMerged) {
                        MergedCounter++
                    } else {
                        NonMergedCount = NonMergedCount + 1
                    }
                }
                if (MergedCounter != 0) {
                    NonMergedCount = NonMergedCount + 1
                }
                setReviewImageIcon(bitmap, NonMergedCount)
            }
        } else if (resultCode == ZeemartAppConstants.ResultCode.RESULT_CANCELED) {
            if (data != null && data.extras != null && !StringHelper.isStringNullOrEmpty(
                    data.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                )
            ) {
                val uriListString = data.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                invoiceImageList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    uriListString,
                    object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                )
                if (invoiceImageList != null && invoiceImageList!!.size > 0) {
                    val bitmap = InvoiceDataManager(this).decodeSampledBitmapFromImageUri(
                        Uri.fromFile(
                            File(
                                invoiceImageList!![0].imageFilePath
                            )
                        ), 35, 35
                    )
                    var MergedCounter = 0
                    var NonMergedCount = 0
                    for (i in invoiceImageList!!.indices) {
                        if (invoiceImageList!![i].isMerged) {
                            MergedCounter++
                        } else {
                            NonMergedCount = NonMergedCount + 1
                        }
                    }
                    if (MergedCounter != 0) {
                        NonMergedCount = NonMergedCount + 1
                    }
                    setReviewImageIcon(bitmap, NonMergedCount)
                }
            } else if ((requestCode == InvoiceHelper.PICK_FROM_GALLERY || requestCode == InvoiceHelper.PICK_FROM_FILES) && invoiceImageList!!.size > 0) {
                val bitmap = InvoiceDataManager(this).decodeSampledBitmapFromImageUri(
                    Uri.fromFile(
                        File(
                            invoiceImageList!![0].imageFilePath
                        )
                    ), 35, 35
                )
                var MergedCounter = 0
                var NonMergedCount = 0
                for (i in invoiceImageList!!.indices) {
                    if (invoiceImageList!![i].isMerged) {
                        MergedCounter++
                    } else {
                        NonMergedCount = NonMergedCount + 1
                    }
                }
                if (MergedCounter != 0) {
                    NonMergedCount = NonMergedCount + 1
                }
                setReviewImageIcon(bitmap, NonMergedCount)
            } else {
                hideReviewImageIcon()
                invoiceImageList!!.clear()
            }
        } else if (resultCode == ZeemartAppConstants.ResultCode.RESULT_IMAGES) {
            val grnIntent =
                Intent(this@InvoiceCameraPreview, GoodsReceivedNoteDashBoardActivity::class.java)
            grnIntent.putExtra(
                ZeemartAppConstants.INVOICE_IMAGE_URI,
                data!!.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
            )
            grnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            setResult(ZeemartAppConstants.ResultCode.RESULT_IMAGES, grnIntent)
            finish()
        } else if (resultCode == ZeemartAppConstants.ResultCode.RESULT_ORDER_INVOICE) {
            val grnIntent = Intent()
            grnIntent.putExtra(
                ZeemartAppConstants.INVOICE_IMAGE_URI,
                data!!.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
            )
            grnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            setResult(ZeemartAppConstants.ResultCode.RESULT_IMAGES, grnIntent)
            finish()
        }
    }

    override fun onPictureTakenCallback(data: ByteArray?) {
        //start the adjust image activity and call the preview activity in the on ActivityResult
        val deviceDimensions = CommonMethods.getDeviceDimensions(this)
        val manager = InvoiceDataManager(this)
        imageDisplayRotation = getImageDisplayRotation()
        val imageSampledBitmap = data?.let {
            manager.decodeSampledBitmapFromByteArray(
                it,
                deviceDimensions.deviceWidth,
                deviceDimensions.deviceHeight,
                imageDisplayRotation
            )
        }
        //Bitmap imageSampledBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        val currentDate =
            DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        val filename = currentDate + "_" + 0.toString()
        val imageFilePath = imageSampledBitmap?.let {
            manager.saveInInternalStorage(
                it,
                SharedPref.defaultOutlet?.outletId,
                filename
            )
        }

        //invoiceImageList.clear();
        inQueueForUploadDataModelCamera = InQueueForUploadDataModel()
        //inQueueForUploadDataModel.setRotation(imageDisplayRotation);
        inQueueForUploadDataModelCamera!!.imageDirectoryPath = imageFilePath?.directoryPath
        inQueueForUploadDataModelCamera!!.imageFilePath = imageFilePath?.filePath
        inQueueForUploadDataModelCamera!!.isInvoiceSelectedIsImage = true
        val adjustImageIntent =
            Intent(this@InvoiceCameraPreview, AdjustInvoiceImageWithOutCropping::class.java)
        val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
        adjustImageIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
        adjustImageIntent.putExtra(
            ZeemartAppConstants.CALLED_FROM,
            ZeemartAppConstants.CALLED_FROM_CAMERA_PREVIEW_SCREEN
        )
        adjustImageIntent.putExtra(ZeemartAppConstants.IMAGE_FILE_PATH, imageFilePath?.filePath)
        adjustImageIntent.putExtra(
            ZeemartAppConstants.IMAGE_DIRECTORY_PATH,
            imageFilePath?.directoryPath
        )
        startActivityForResult(
            adjustImageIntent,
            ZeemartAppConstants.RequestCode.CameraPreviewActivityAdjustImage
        )

        /*if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom.equals(ZeemartAppConstants.CALLED_FROM_REVIEW_INVOICE_IMAGE_ACTIVITY)) {
            Intent newIntent = new Intent(this, ReviewInvoiceImageActivity.class);
            String invoiceListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList);
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceListJson);
            setResult(Activity.RESULT_OK, newIntent);
            finish();
        } else {
            Intent newIntent = new Intent(this, ReviewInvoiceImageActivity.class);
            String invoiceListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList);
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceListJson);
            newIntent.putExtra(ZeemartAppConstants.IMAGE_ROTATION_ANGLE, imageDisplayRotation);
            startActivity(newIntent);
        }*/
    }

    override fun imagePickedFromGallery(data: Intent?, pickForFiles: String?) {
        if (pickForFiles == ZeemartAppConstants.PICK_FOR_FILES) {
            Log.d("CAMERA", "inside pick from docs")
            var clipData: ClipData? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                clipData = data!!.clipData
            }
            if (clipData != null) {
                if (clipData.itemCount > noOfFilesThatCanBeAdded) {
                    displayErrorMessageDialog(
                        this,
                        null,
                        getString(
                            R.string.txt_can_add_only_these_more_files,
                            noOfFilesThatCanBeAdded
                        )
                    )
                } else {
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        val uri = item.uri
                        checkFileTypeSaveInvoiceToList(uri, i)
                    }
                }
            } else {
                val uri = data!!.data!!
                val index = 0
                checkFileTypeSaveInvoiceToList(uri, index)
            }
        }
        if (pickForFiles == ZeemartAppConstants.PICK_FOR_GALLERY) {
            val images =
                data!!.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT) as ArrayList<String>?
            if (images != null) {
                for (i in images.indices) {
                    val uri = Uri.fromFile(File(images[i]))
                    val displayName = getRealPathFromURI(uri)
                    val currentDate =
                        DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
                    val filename = currentDate + "_" + i.toString()
                    createInvoiceDataGallery(uri, displayName, filename)
                }
            }
        }
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_INVOICE_IMAGE_ACTIVITY) {
            val newIntent = Intent(this, ReviewInvoiceImageActivity::class.java)
            val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, calledFrom)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
            setResult(ZeemartAppConstants.ResultCode.RESULT_FROM_GALLERY, newIntent)
            finish()
        } else {
            val newIntent = Intent(this, ReviewInvoiceImageActivity::class.java)
            val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            newIntent.putExtra(ZeemartAppConstants.IMAGE_ROTATION_ANGLE, imageDisplayRotation)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, calledFrom)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
            startActivityForResult(newIntent, ZeemartAppConstants.RequestCode.ReviewInvoiceImage)
        }
    }

    override fun reviewImagesCallback() {
        if (invoiceImageList!!.size > 0) {
            val newIntent = Intent(this, ReviewInvoiceImageActivity::class.java)
            val invoiceListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceListJson)
            imageDisplayRotation = getImageDisplayRotation()
            newIntent.putExtra(ZeemartAppConstants.IMAGE_ROTATION_ANGLE, imageDisplayRotation)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, calledFrom)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
            startActivityForResult(newIntent, ZeemartAppConstants.RequestCode.ReviewInvoiceImage)
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
            createInvoiceDataPdf(uri, displayName, filename)
        } else if (type.equals(InvoiceHelper.FileType.PNG.value, ignoreCase = true) ||
            type.equals(InvoiceHelper.FileType.JPG.value, ignoreCase = true) ||
            type.equals(InvoiceHelper.FileType.JPEG.value, ignoreCase = true)
        ) {
            createInvoiceDataGallery(uri, displayName, filename)
        } else {
            getToastRed(getString(R.string.txt_only_pdf_image))
            return
        }
    }

    /**
     * create the invoice data object for images selected from gallery
     * sets the isImage flag true
     *
     * @param uri
     * @param displayName
     * @param fileName
     */
    fun createInvoiceDataGallery(uri: Uri?, displayName: String?, fileName: String?) {
        val deviceDimensions = CommonMethods.getDeviceDimensions(this)
        val manager = InvoiceDataManager(this)
        val bitmap = uri?.let {
            manager.decodeSampledBitmapFromImageUri(
                it,
                deviceDimensions.deviceWidth,
                deviceDimensions.deviceHeight
            )
        }
        val imageInternalPaths =
            bitmap?.let { manager.saveInInternalStorage(it, SharedPref.defaultOutlet?.outletId, fileName) }
        val invoiceImageDataModel = InQueueForUploadDataModel()
        invoiceImageDataModel.imageDirectoryPath = imageInternalPaths?.directoryPath
        invoiceImageDataModel.imageFilePath = imageInternalPaths?.filePath
        invoiceImageDataModel.selectedPdfOriginalName = displayName
        invoiceImageDataModel.rotation = 0
        invoiceImageDataModel.isInvoiceSelectedIsImage = true
        invoiceImageList!!.add(invoiceImageDataModel)
    }

    /**
     * create the invoice data object for pdf
     * sets the isImage flag false
     *
     * @param uri
     * @param displayName
     * @param filename
     */
    fun createInvoiceDataPdf(uri: Uri?, displayName: String?, filename: String?) {
        val manager = InvoiceDataManager(this)
        val imageInternalPaths =
            manager.savePdfInInternalStorage(SharedPref.defaultOutlet?.outletId, filename, uri)
        val invoiceImageDataModel = InQueueForUploadDataModel()
        invoiceImageDataModel.imageDirectoryPath = imageInternalPaths.directoryPath
        invoiceImageDataModel.imageFilePath = imageInternalPaths.filePath
        invoiceImageDataModel.rotation = 0
        invoiceImageDataModel.selectedPdfOriginalName = displayName
        invoiceImageDataModel.isInvoiceSelectedIsImage = false
        val bytes = getBytesData(uri)
        if (bytes.size() < 2000000) {
            invoiceImageList!!.add(invoiceImageDataModel)
        } else {
            displayErrorMsg()
        }
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

    override fun onBackPressed() {
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_INVOICE_IMAGE_ACTIVITY && noOfFilesThatCanBeAdded != 0) {
            val newIntent =
                Intent(this@InvoiceCameraPreview, ReviewInvoiceImageActivity::class.java)
            val invoiceListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceListJson)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, calledFrom)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
            setResult(RESULT_OK, newIntent)
            finish()
        } else if (invoiceImageList!!.size > 0) {
            val builder = AlertDialog.Builder(this@InvoiceCameraPreview)
            builder.setPositiveButton(R.string.txt_yes_cancel) { dialog, which ->
                dialog.dismiss()
                if (!StringHelper.isStringNullOrEmpty(calledFrom) && (calledFrom.equals(
                        ZeemartAppConstants.CALLED_FROM_GRN,
                        ignoreCase = true
                    ) || calledFrom.equals(
                        ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS,
                        ignoreCase = true
                    ))
                ) {
                    finish()
                } else {
                    val newIntent =
                        Intent(this@InvoiceCameraPreview, BaseNavigationActivity::class.java)
                    startActivity(newIntent)
                }
            }
            builder.setNegativeButton(R.string.txt_no) { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            var MergedCounter = 0
            var NonMergedCount = 0
            for (i in invoiceImageList!!.indices) {
                if (invoiceImageList!![i].isMerged) {
                    MergedCounter++
                } else {
                    NonMergedCount = NonMergedCount + 1
                }
            }
            if (MergedCounter != 0) {
                NonMergedCount = NonMergedCount + 1
            }
            dialog.setMessage(getString(R.string.txt_cancel_upload, NonMergedCount))
            dialog.show()
            val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            deleteBtn.setTextColor(resources.getColor(R.color.chart_red))
            deleteBtn.isAllCaps = false
            val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            cancelBtn.isAllCaps = false
        } else {
            if (!StringHelper.isStringNullOrEmpty(calledFrom) && (calledFrom.equals(
                    ZeemartAppConstants.CALLED_FROM_GRN,
                    ignoreCase = true
                ) || calledFrom.equals(
                    ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS,
                    ignoreCase = true
                ))
            ) {
                finish()
            } else {
                val newIntent =
                    Intent(this@InvoiceCameraPreview, BaseNavigationActivity::class.java)
                startActivity(newIntent)
            }
        }
    }

    interface AdjustImageInterface {
        fun saveImageInInvoiceList()
    }

    companion object {
        private const val GET_IMAGE_FROM_CAMERA_PREVIEW_ACTIVITY = 101
    }
}