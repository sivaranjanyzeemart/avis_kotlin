package zeemart.asia.buyers.invoices

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.invoices.ReviewInvoiceImageActivity
import java.io.File

class AdjustInvoiceImageWithOutCropping : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var txtAdjust: TextView? = null
    private var invoiceDataManager: InvoiceDataManager? = null
    private var imgBitmap: Bitmap? = null
    private lateinit var btnDone: Button
    private lateinit var txtCancel: TextView
    private lateinit var btnRotate: ImageButton
    var imageFilePath: String? = null
    var directoryFilePath: String? = null
    var objInfoFilePath: String? = null
    var calledFrom: String? = null
    private var invoiceImageList: MutableList<InQueueForUploadDataModel> = ArrayList()
    private var inQueueForUploadDataModelCamera: InQueueForUploadDataModel? = null
    private val clickCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_invoice_image_with_out_cropping)
        val bundle = intent.extras
        calledFrom = bundle!!.getString(ZeemartAppConstants.CALLED_FROM)
        imageFilePath = bundle.getString(ZeemartAppConstants.IMAGE_FILE_PATH)
        if (bundle.getString(ZeemartAppConstants.IMAGE_DIRECTORY_PATH) != null) {
            directoryFilePath = bundle.getString(ZeemartAppConstants.IMAGE_DIRECTORY_PATH)
        }
        if (bundle.getString(ZeemartAppConstants.OBJECT_FILE_INFO_PATH) != null) {
            objInfoFilePath = bundle.getString(ZeemartAppConstants.OBJECT_FILE_INFO_PATH)
        }
        if (bundle.getString(ZeemartAppConstants.INVOICE_IMAGE_URI) != null) {
            val uriListString = bundle.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
            invoiceImageList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                uriListString,
                object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
            )
        }
        imageFilePath = bundle.getString(ZeemartAppConstants.IMAGE_FILE_PATH)
        imageView = findViewById(R.id.imageView)
        txtAdjust = findViewById(R.id.txt_adjust)
        setTypefaceView(txtAdjust, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        btnDone = findViewById(R.id.btn_done_adjust)
        btnDone.setOnClickListener(View.OnClickListener { onButtonDonePressed() })
        txtCancel = findViewById(R.id.txt_cancel_review)
        setTypefaceView(txtCancel, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        txtCancel.setOnClickListener(View.OnClickListener {
            val newIntent = Intent()
            val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            setResult(ZeemartAppConstants.ResultCode.RESULT_CANCELED, newIntent)
            finish()
        })
        btnRotate = findViewById(R.id.imgbtn_rotate)
        btnRotate.setOnClickListener(View.OnClickListener {
            val matrix = Matrix()
            matrix.postRotate(90f)
            val tempBitmap = (imageView.getDrawable() as BitmapDrawable).bitmap
            val rotatedBitmap = Bitmap.createBitmap(
                tempBitmap,
                0,
                0,
                tempBitmap.width,
                tempBitmap.height,
                matrix,
                true
            )
            imageView.setImageBitmap(rotatedBitmap)
        })
        invoiceDataManager = InvoiceDataManager(this)
        val file = File(imageFilePath)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        imgBitmap =
            invoiceDataManager!!.decodeSampledBitmapFromImageUri(Uri.fromFile(file), width, height)
        imageView.setImageBitmap(imgBitmap)
    }

    private fun onButtonDonePressed() {
        val bm = (imageView!!.drawable as BitmapDrawable).bitmap
        imageView!!.setImageBitmap(bm)
        InvoiceDataManager.saveCroppedBitmapToFilePath(bm, imageFilePath)
        if (calledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_IMAGE_SCREEN) {
            val newIntent = Intent(this, ReviewInvoiceImageActivity::class.java)
            inQueueForUploadDataModelCamera = InQueueForUploadDataModel()
            inQueueForUploadDataModelCamera!!.imageFilePath = imageFilePath
            inQueueForUploadDataModelCamera!!.imageDirectoryPath = directoryFilePath
            inQueueForUploadDataModelCamera!!.objectInfoFilePath = objInfoFilePath
            inQueueForUploadDataModelCamera!!.isInvoiceSelectedIsImage = true
            invoiceImageList.add(inQueueForUploadDataModelCamera!!)
            val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            startActivityForResult(
                newIntent,
                ZeemartAppConstants.RequestCode.CameraPreviewActivityAdjustImage
            )
        } else {
            val newIntent = Intent()
            setResult(ZeemartAppConstants.ResultCode.RESULT_OK, newIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        val newIntent = Intent()
        val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
        newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
        setResult(ZeemartAppConstants.ResultCode.RESULT_CANCELED, newIntent)
        finish()
    }
}