package zeemart.asia.buyers.orderPayments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DialogHelper.alertPaymentDialogSmallFailure
import zeemart.asia.buyers.helper.DialogHelper.alertPaymentDialogSmallSuccess
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.ImageRotationHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.PermissionCallback
import zeemart.asia.buyers.models.OrderPayments.UploadTransactionImage
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.modelsimport.MultiPartImageUploadResponse
import zeemart.asia.buyers.network.ImageUploadHelper.MultiPartImageUploaded
import zeemart.asia.buyers.network.ImageUploadHelper.UploadFileMultipart
import zeemart.asia.buyers.network.OrderManagement
import zeemart.asia.buyers.network.OrderManagement.uploadTransactionrResponseListener
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PaymentConfirmation : AppCompatActivity(), View.OnClickListener {
    private var txtHeader: TextView? = null
    private var txtPayDesc: TextView? = null
    private lateinit var lytImgUpload: LinearLayout
    private var txtSelectFile: TextView? = null
    private var txtImgType: TextView? = null
    private lateinit var btnSubmit: Button
    private lateinit var imgBack: ImageView
    private lateinit var imgCancel: ImageView
    private var imgSelect: ImageView? = null
    private var outPutfileUri: Uri? = null
    private var bitmap: Bitmap? = null
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private var mOrder: Orders? = null
    private var calledFrom: String? = null
    private lateinit var lytImageDisplay: RelativeLayout
    private var imgDisplay: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        txtHeader = findViewById(R.id.txt_product_list_heading)
        txtPayDesc = findViewById(R.id.desc_py)
        lytImgUpload = findViewById(R.id.lyt_img_upload)
        lytImgUpload.setOnClickListener(this)
        imgSelect = findViewById(R.id.img_select)
        txtSelectFile = findViewById(R.id.txt_select_file)
        txtImgType = findViewById(R.id.txt_img_type)
        btnSubmit = findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener(View.OnClickListener { uploadImage(bitmap) })
        btnSubmit.setClickable(false)
        imgBack = findViewById(R.id.bt_back_btn)
        imgBack.setOnClickListener(this)
        imgCancel = findViewById(R.id.img_cancel)
        imgCancel.setOnClickListener(this)
        threeDotLoaderWhite = findViewById(R.id.three_dot_loader)
        imgDisplay = findViewById(R.id.img_uploaded)
        lytImageDisplay = findViewById(R.id.img_display)
        lytImageDisplay.setVisibility(View.GONE)
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtPayDesc, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtSelectFile, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtImgType, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnSubmit, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.lyt_img_upload -> pickImageIntent
            R.id.bt_back_btn -> finish()
            R.id.img_cancel -> {
                lytImgUpload!!.visibility = View.VISIBLE
                lytImgUpload!!.isClickable = true
                lytImageDisplay!!.visibility = View.GONE
                lytImgUpload!!.setBackgroundColor(resources.getColor(R.color.faint_grey))
                imgSelect!!.visibility = View.VISIBLE
                txtSelectFile!!.visibility = View.VISIBLE
                btnSubmit!!.background = resources.getDrawable(R.drawable.dark_grey_rounded_corner)
                btnSubmit!!.isClickable = false
                imgCancel!!.visibility = View.INVISIBLE
                lytImgUpload!!.isClickable = true
                threeDotLoaderWhite!!.visibility = View.GONE
            }
            else -> {}
        }
    }

    private fun uploadImage(bitmap: Bitmap?) {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        val currentDate =
            DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        val filename = currentDate + "_" + SharedPref.read(SharedPref.USER_ID, "")
        UploadFileMultipart(
            this@PaymentConfirmation,
            bitmap,
            outPutfileUri,
            "PAYMENT_RECEIPTS",
            filename,
            object : MultiPartImageUploaded {
                override fun result(imageUpload: MultiPartImageUploadResponse?) {
                    if (imageUpload != null && imageUpload.data != null && imageUpload.data!!.files.size > 0) {
                        if (!StringHelper.isStringNullOrEmpty(mOrder!!.dealNumber)) {
                            AnalyticsHelper.logAction(
                                this@PaymentConfirmation,
                                AnalyticsHelper.TAP_ORDER_UPLOAD_PROOF,
                                mOrder!!.orderId,
                                mOrder!!.outlet!!,
                                true,
                                false
                            )
                        }
                        if (!StringHelper.isStringNullOrEmpty(mOrder!!.essentialsId)) {
                            AnalyticsHelper.logAction(
                                this@PaymentConfirmation,
                                AnalyticsHelper.TAP_ORDER_UPLOAD_PROOF,
                                mOrder!!.orderId,
                                mOrder!!.outlet!!,
                                false,
                                true
                            )
                        }
                        val url = imageUpload.data!!.files[0].fileUrl
                        val uploadTransactionImage = UploadTransactionImage()
                        uploadTransactionImage.orderId = mOrder!!.orderId
                        uploadTransactionImage.imageURL = url
                        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_PAY_NOW) {
                            uploadTransactionImage.paymentType =
                                UploadTransactionImage.PaymentType.PAY_NOW.name
                        } else if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_BANK_TRANSFER) {
                            uploadTransactionImage.paymentType =
                                UploadTransactionImage.PaymentType.BANK_TRANSFER.name
                        } else {
                            uploadTransactionImage.paymentType =
                                UploadTransactionImage.PaymentType.CARD.name
                        }
                        val jsonRequest =
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(uploadTransactionImage)
                        OrderManagement.uploadTransactionImageToOrder(
                            this@PaymentConfirmation,
                            jsonRequest,
                            object : uploadTransactionrResponseListener {
                                 override fun onSuccessResponse(status: String?) {
                                    threeDotLoaderWhite!!.visibility = View.INVISIBLE
                                    if (!StringHelper.isStringNullOrEmpty(status)) {
                                        val dialogSuccess = alertPaymentDialogSmallSuccess(
                                            this@PaymentConfirmation, getString(
                                                R.string.txt_thanks
                                            ), getString(R.string.txt_get_back)
                                        )
                                        dialogSuccess.show()
                                        Handler().postDelayed({
                                            dialogSuccess.dismiss()
                                            val intent = Intent(
                                                this@PaymentConfirmation,
                                                OrderDetailsActivity::class.java
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.ORDER_ID,
                                                mOrder!!.orderId
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                                mOrder!!.outlet!!.outletId
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.CALLED_FROM,
                                                ZeemartAppConstants.CALLED_FROM_ORDER_PAYMENT_CONFIRMATION
                                            )
                                            startActivity(intent)
                                        }, 2000)
                                    } else {
                                        val dialogFailure = alertPaymentDialogSmallFailure(
                                            this@PaymentConfirmation, getString(
                                                R.string.txt_upload_failed
                                            ), getString(R.string.txt_please_try_again)
                                        )
                                        dialogFailure.show()
                                        Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                                    }
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    threeDotLoaderWhite!!.visibility = View.GONE
                                    val dialogFailure = alertPaymentDialogSmallFailure(
                                        this@PaymentConfirmation, getString(
                                            R.string.txt_upload_failed
                                        ), getString(R.string.txt_please_try_again)
                                    )
                                    dialogFailure.show()
                                    Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                                }
                            })
                    } else {
                        threeDotLoaderWhite!!.visibility = View.INVISIBLE
                        val dialogFailure = alertPaymentDialogSmallFailure(
                            this@PaymentConfirmation, getString(
                                R.string.txt_upload_failed
                            ), getString(R.string.txt_please_try_again)
                        )
                        dialogFailure.show()
                        Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                    }
                }
            })
    }

    // Create the AlertDialog
    private val pickImageIntent: Unit
        private get() {
            val builder = AlertDialog.Builder(this@PaymentConfirmation)
            builder.setMessage(R.string.txt_select_photo_from)
            builder.setPositiveButton(R.string.txt_camera) { dialog, id ->
                dialog.dismiss()
                requestPermission(PermissionCallback.TAKE_IMAGE, object : PermissionCallback {
                    override fun denied(requestCode: Int) {}
                    override fun allowed(requestCode: Int) {
                        imagePicker(PICK_FROM_CAMERA)
                    }
                })
            }
            builder.setNegativeButton(R.string.txt_gallery) { dialog, id ->
                dialog.dismiss()
                requestPermission(PermissionCallback.WRITE_IMAGE, object : PermissionCallback {
                    override fun denied(requestCode: Int) {}
                    override fun allowed(requestCode: Int) {
                        imagePicker(PICK_FROM_GALLERY)
                    }
                })
            }
            // Create the AlertDialog
            val dialog = builder.create()
            dialog.show()
        }

    private fun imagePicker(source: Int) {
        if (source == PICK_FROM_CAMERA) {
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val auth = this.applicationContext.packageName + ".zeemart.asia.buyers.fileprovider"
            try {
                outPutfileUri = FileProvider.getUriForFile(this, auth, createImageFile())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri)
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(captureIntent, PICK_FROM_CAMERA)
        } else if (source == PICK_FROM_GALLERY) {
            val captureIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(captureIntent, PICK_FROM_GALLERY)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ServiceConstant.FILE_EXT_JPEG,  /* suffix */
            storageDir /* directory */
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        btnSubmit!!.isClickable = false
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            if (outPutfileUri != null) {
                try {
                    bitmap =
                        ImageRotationHelper.handleSamplingAndRotationBitmap(this, outPutfileUri)
                    val background = BitmapDrawable(resources, bitmap)
                    lytImgUpload!!.visibility = View.GONE
                    lytImgUpload!!.background = background
                    lytImgUpload!!.isClickable = false
                    lytImageDisplay!!.visibility = View.VISIBLE
                    imgDisplay!!.setImageBitmap(bitmap)
                    btnSubmit!!.background = resources.getDrawable(R.drawable.btn_azul_blue)
                    imgCancel!!.visibility = View.VISIBLE
                    btnSubmit!!.isClickable = true
                    lytImgUpload!!.isClickable = false
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                    // Get the cursor
                    val cursor =
                        contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                    // Move to first row
                    cursor!!.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val imgDecodableString = cursor.getString(columnIndex)
                    cursor.close()
                    //Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
                    bitmap = ImageRotationHelper.handleSamplingAndRotationBitmap(
                        this, Uri.fromFile(
                            File(imgDecodableString)
                        )
                    )
                    val background = BitmapDrawable(resources, bitmap)
                    lytImgUpload!!.visibility = View.GONE
                    lytImgUpload!!.background = background
                    lytImgUpload!!.isClickable = false
                    lytImageDisplay!!.visibility = View.VISIBLE
                    imgDisplay!!.setImageBitmap(bitmap)
                    btnSubmit!!.background = resources.getDrawable(R.drawable.btn_azul_blue)
                    imgCancel!!.visibility = View.VISIBLE
                    btnSubmit!!.isClickable = true
                    lytImgUpload!!.isClickable = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    var permissionCallback: PermissionCallback? = null
    fun requestPermission(requestCode: Int, permissionCallback: PermissionCallback?) {
        this.permissionCallback = permissionCallback
        when (requestCode) {
            PermissionCallback.WRITE_IMAGE -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is not granted => Request for permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        requestCode
                    )
                } else {
                    permissionCallback?.allowed(requestCode)
                }
            }
            PermissionCallback.TAKE_IMAGE -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is not granted => Request for permission
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.CAMERA),
                        requestCode
                    )
                } else {
                    permissionCallback?.allowed(requestCode)
                }
            }
            else -> permissionCallback?.denied(requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionCallback != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted
            permissionCallback!!.allowed(requestCode)
        } else {
            // permission denied, boo! Disable the
            permissionCallback!!.denied(requestCode)
        }
    }

    companion object {
        private const val PICK_FROM_CAMERA = 1
        private const val PICK_FROM_GALLERY = 2
    }
}