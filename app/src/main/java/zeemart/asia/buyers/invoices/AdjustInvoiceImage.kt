package zeemart.asia.buyers.invoices

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.reflect.TypeToken
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.camera.CameraEdgeDetectionHelper
import zeemart.asia.buyers.helper.camera.CameraEdgeDetectionHelper.Corners
import zeemart.asia.buyers.helper.customviews.RectangularBoundView
import zeemart.asia.buyers.helper.customviews.RectangularBoundView.AdjustImageListener
import zeemart.asia.buyers.helper.imgpostprocessing.ImagePostProcessing
import zeemart.asia.buyers.invoices.ReviewInvoiceImageActivity
import java.io.File
import java.util.*

class AdjustInvoiceImage : AppCompatActivity() {
    private lateinit var rectangularBoundView: RectangularBoundView
    private var holderImageCrop: RelativeLayout? = null
    private lateinit var imageView: ImageView
    private var txtAdjust: TextView? = null
    private var invoiceDataManager: InvoiceDataManager? = null
    private var imagePostProcessing: ImagePostProcessing? = null
    private var imgBitmap: Bitmap? = null
    private lateinit var btnDone: Button
    private lateinit var txtCancel: TextView
    private lateinit var btnRotate: ImageButton
    private var orignalMatImage: Mat? = null
    private lateinit var btnNoCrop: ImageButton
    var imageFilePath: String? = null
    var directoryFilePath: String? = null
    var objInfoFilePath: String? = null
    var calledFrom: String? = null
    private var invoiceImageList: MutableList<InQueueForUploadDataModel> = ArrayList()
    private var inQueueForUploadDataModelCamera: InQueueForUploadDataModel? = null
    private var status = 0
    private var clickCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_invoice_image)
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
        rectangularBoundView = findViewById(R.id.bounded_rect)
        holderImageCrop = findViewById(R.id.holderImageCrop)
        imageView = findViewById(R.id.imageView)
        txtAdjust = findViewById(R.id.txt_adjust)
        setTypefaceView(txtAdjust, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        btnDone = findViewById(R.id.btn_done_adjust)
        btnDone.setOnClickListener(View.OnClickListener { onButtonDonePressed() })
        btnNoCrop = findViewById(R.id.imgbtn_no_crop)
        btnNoCrop.setOnClickListener(View.OnClickListener {
            status = if (status == 0) {
                btnNoCrop.setImageResource(R.drawable.auto_detect)
                setEdgePointNoCrop()
                1
            } else {
                btnNoCrop.setImageResource(R.drawable.no_crop)
                initializeElement()
                0
            }
        })
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
            val rotationAngle = 90
            clickCount = clickCount + 1
            val point2f: MatOfPoint2f
            if (status == 0) {
                rectangularBoundView.setVisibility(View.INVISIBLE)
                initializeElement()
                imageView.setRotation(imageView.getRotation() + rotationAngle)
                rectangularBoundView.setRotation(rectangularBoundView.getRotation() + rotationAngle)
                //Check degree is 90 or 270
                if (clickCount % 2 == 1) {
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                    lp.setMargins(0, 180, 0, 180)
                    imageView.setLayoutParams(lp)
                    rectangularBoundView.setLayoutParams(lp)
                } else {
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                    lp.setMargins(0, 0, 0, 0)
                    imageView.setLayoutParams(lp)
                    rectangularBoundView.setLayoutParams(lp)
                }
            } else {
                imageView.setRotation(imageView.getRotation() + rotationAngle)
                rectangularBoundView.setRotation(rectangularBoundView.getRotation() + rotationAngle)
                val tempBitmap = (imageView.getDrawable() as BitmapDrawable).bitmap
                if (clickCount % 2 == 0) {
                    point2f = imagePostProcessing!!.getPointsFullScreen(
                        tempBitmap.width,
                        tempBitmap.height
                    )
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                    lp.setMargins(0, 0, 0, 0)
                    imageView.setLayoutParams(lp)
                    rectangularBoundView.setLayoutParams(lp)
                } else {
                    point2f = imagePostProcessing!!.getRotateImagePointsFullScreen(
                        tempBitmap.width,
                        tempBitmap.height
                    )
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                    lp.setMargins(0, 180, 0, 180)
                    imageView.setLayoutParams(lp)
                }
                setCropPoints(tempBitmap, point2f)
            }
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
        rectangularBoundView.setOnAdjustImageListener(object : AdjustImageListener {
            override fun adjustImage() {
                btnNoCrop.setImageResource(
                    R.drawable.no_crop
                )
            }
        })
        initializeElement()
    }

    private fun onButtonDonePressed() {
        if (orignalMatImage != null) {
            val cornerCropPoints = rectangularBoundView!!.corners2Crop
            val croppedImage =
                CameraEdgeDetectionHelper.cropPicture(orignalMatImage, cornerCropPoints)
            val croppedBitmap = Bitmap.createBitmap(
                croppedImage.width(),
                croppedImage.height(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(croppedImage, croppedBitmap)
            imageView!!.setImageBitmap(croppedBitmap)
            if (rectangularBoundView!!.visibility == View.VISIBLE) rectangularBoundView!!.visibility =
                View.GONE
            InvoiceDataManager.saveCroppedBitmapToFilePath(croppedBitmap, imageFilePath)
            if (calledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_IMAGE_SCREEN) {
                val newIntent = Intent(this, ReviewInvoiceImageActivity::class.java)
                inQueueForUploadDataModelCamera = InQueueForUploadDataModel()
                inQueueForUploadDataModelCamera!!.imageFilePath = imageFilePath
                inQueueForUploadDataModelCamera!!.imageDirectoryPath = directoryFilePath
                inQueueForUploadDataModelCamera!!.objectInfoFilePath = objInfoFilePath
                inQueueForUploadDataModelCamera!!.isInvoiceSelectedIsImage = true
                invoiceImageList.add(inQueueForUploadDataModelCamera!!)
                val invoiceImageListJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(invoiceImageList)
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
    }

    private fun initializeElement() {
        imagePostProcessing = ImagePostProcessing()
        holderImageCrop!!.post {
            val tempBitmap = (imageView!!.drawable as BitmapDrawable).bitmap
            val scaleX = imageView!!.scaleX
            val scaleY = imageView!!.scaleY
            val finalWidth = imageView!!.width * scaleX
            val finalHeight = imageView!!.height * scaleY
            val scaledBitmap = Bitmap.createScaledBitmap(
                tempBitmap,
                finalWidth.toInt(),
                finalHeight.toInt(),
                false
            )
            /*Log.d("DIMEN_DEVICE",dui.getDeviceHeight()+"*****"+dui.getDeviceWidth());
                    Log.d("DIMEN_BITMAP", "finalWidth->"+finalWidth+" finalHeight->"+finalHeight+" BitmapHeight->"+scaledBitmap.getHeight()+" BitmapWidth->"+scaledBitmap.getWidth());
                    */
            val param = RelativeLayout.LayoutParams(finalWidth.toInt(), finalHeight.toInt())
            param.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            rectangularBoundView!!.layoutParams = param
            imageView!!.setImageBitmap(scaledBitmap)
            val point2f = imagePostProcessing!!.getPoint(
                scaledBitmap,
                scaledBitmap.width,
                scaledBitmap.height
            )
            imageView!!.setImageBitmap(scaledBitmap)
            //Log.d("DIMEN_IMG_PNT",Arrays.toString(point2f.toArray()));
            setCropPoints(scaledBitmap, point2f!!)
        }
    }

    /**
     * set the crop points on the image for detected document
     * @param tempBitmap
     * @param point2f
     */
    private fun setCropPoints(tempBitmap: Bitmap, point2f: MatOfPoint2f) {
        val pointFs = getEdgePointsFloat(point2f)
        val cornerPoints: MutableList<Point> = ArrayList()
        for (i in 0..3) {
            val po = Point()
            if (i < pointFs!!.size) {
                po.x = pointFs[i].x.toDouble()
                po.y = pointFs[i].y.toDouble()
            } else {
                po.x = 0.0
                po.y = 0.0
            }
            cornerPoints.add(po)
        }
        orignalMatImage = Mat()
        Utils.bitmapToMat(tempBitmap, orignalMatImage)
        rectangularBoundView!!.visibility = View.VISIBLE
        val corner = Corners(cornerPoints, orignalMatImage!!.size())
        rectangularBoundView!!.onCornersDetected(corner)
    }

    private fun setEdgePointNoCrop() {
        val point2f: MatOfPoint2f
        val tempBitmap = (imageView!!.drawable as BitmapDrawable).bitmap
        point2f = if (clickCount % 2 == 0) {
            imagePostProcessing!!.getPointsFullScreen(tempBitmap.width, tempBitmap.height)
        } else {
            imagePostProcessing!!.getRotateImagePointsFullScreen(
                tempBitmap.width,
                tempBitmap.height
            ) // for 90 or 270 rotation
        }
        setCropPoints(tempBitmap, point2f)
    }

    //get points on the corners of the edge of document, if none are detected, points of the corners of the whole image
    private fun getEdgePointsFloat(point2f: MatOfPoint2f?): List<PointF>? {
        return if (point2f != null) //will be null if it isnt rectangular enough (75deg check)
        {
            val points = Arrays.asList(*point2f.toArray())
            val result: MutableList<PointF> = ArrayList()
            for (i in points.indices) {
                result.add(PointF(points[i].x.toFloat(), points[i].y.toFloat()))
            }
            result
        } else {
            null
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