package zeemart.asia.buyers.companies

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.essentials.EssentialsProductListActivity
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DateHelper.getDateInYearMonthDayHourMinSec
import zeemart.asia.buyers.helper.DialogHelper.alertPaymentDialogSmallFailure
import zeemart.asia.buyers.helper.DialogHelper.alertPaymentDialogSmallSuccess
import zeemart.asia.buyers.helper.ImageRotationHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.interfaces.PermissionCallback
import zeemart.asia.buyers.invoices.InvoiceDataManager
import zeemart.asia.buyers.models.Companies
import zeemart.asia.buyers.models.CompanyUpdate
import zeemart.asia.buyers.modelsimport.MultiPartImageUploadResponse
import zeemart.asia.buyers.network.CompaniesApi.uploadCompanyVerifyImage
import zeemart.asia.buyers.network.ImageUploadHelper.MultiPartImageUploaded
import zeemart.asia.buyers.network.ImageUploadHelper.UploadFileMultipart
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CompanyVerificationRequestActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var company: Companies
    private lateinit var uenET: EditText
    private lateinit var companyNameTxt: TextView
    private lateinit var companyNameHeader: TextView
    private lateinit var viewHeader: TextView
    private lateinit var uenHeader: TextView
    private lateinit var uploadProofHeader: TextView
    private lateinit var proofTxt: TextView
    private lateinit var txtHeader: TextView
    private lateinit var txtPayDesc: TextView
    private lateinit var lytImgUpload: LinearLayout
    private lateinit var lytPdfUploaded: LinearLayout
    private lateinit var txtPdfname: TextView
    private lateinit var txtSelectFile: TextView
    private lateinit var txtImgType: TextView
    private lateinit var btnSubmit: Button
    private lateinit var imgBack: ImageView
    private lateinit var imgCancel: ImageView
    private lateinit var imgSelect: ImageView
    private lateinit var outPutfileUri: Uri
    private lateinit var bitmap: Bitmap
    private var uri: Uri? = null
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    private lateinit var lytImageDisplay: RelativeLayout
    private lateinit var imgDisplay: ImageView
    private lateinit var txtPDFNoPreviewAvailable: TextView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_verification_request)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.COMPANY)) {
                company = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.COMPANY), Companies::class.java
                )
            }
        }
        uenET = findViewById(R.id.company_verity_uen_et)
        companyNameTxt = findViewById(R.id.txt_company_name)
        companyNameHeader = findViewById(R.id.header_bank_name)
        viewHeader = findViewById(R.id.desc_bt)
        uenHeader = findViewById(R.id.txt_company_verify_uen_header)
        uploadProofHeader = findViewById(R.id.txt_company_verify_upload_proof)
        proofTxt = findViewById(R.id.desc_proof)
        companyNameTxt.setText(company.name)
        txtHeader = findViewById(R.id.txt_product_list_heading)
        txtPayDesc = findViewById(R.id.desc_py)
        lytImgUpload = findViewById(R.id.lyt_img_upload)
        lytImgUpload.setOnClickListener(this)
        lytPdfUploaded = findViewById(R.id.lyt_pdf_uploaded)
        lytPdfUploaded.setVisibility(View.GONE)
        txtPdfname = findViewById(R.id.txt_pdf_name)
        txtPDFNoPreviewAvailable = findViewById(R.id.txt_pdf_no_preview)
        imgSelect = findViewById(R.id.img_select)
        txtSelectFile = findViewById(R.id.txt_select_file)
        txtImgType = findViewById(R.id.txt_img_type)
        btnSubmit = findViewById(R.id.btn_bank_transfer)
        btnSubmit.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (isValidate) uploadImage(bitmap)
            }
        })
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

    private val isValidate: Boolean
        get() {
            var isValid = true
            if (StringHelper.isStringNullOrEmpty(uenET.text.toString())) {
                uenET.error = "please fill details"
                isValid = false
            }
            if (false) {
                ZeemartBuyerApp.getToastRed("Please upload valid proof")
                isValid = false
            }
            return isValid
        }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPayDesc,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectFile,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtImgType,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSubmit,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            viewHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(uenET, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        ZeemartBuyerApp.setTypefaceView(
            uenHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            companyNameTxt,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            uploadProofHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            proofTxt,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            companyNameHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPdfname,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPDFNoPreviewAvailable,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.lyt_img_upload -> pickImageIntent
            R.id.bt_back_btn -> finish()
            R.id.img_cancel -> {
                lytImgUpload.visibility = View.VISIBLE
                lytImgUpload.isClickable = true
                lytImageDisplay.visibility = View.GONE
                lytImgUpload.setBackgroundColor(resources.getColor(R.color.faint_grey))
                imgSelect.visibility = View.VISIBLE
                txtSelectFile.visibility = View.VISIBLE
                imgCancel.visibility = View.INVISIBLE
                lytImgUpload.isClickable = true
                threeDotLoaderWhite.visibility = View.GONE
                lytPdfUploaded.visibility = View.GONE
                bitmap != null
                uri = null
            }
            else -> {}
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        threeDotLoaderWhite.visibility = View.VISIBLE
        val currentDate =
            getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        val filename = currentDate + "_" + SharedPref.read(SharedPref.USER_ID, "")
        UploadFileMultipart(
            this@CompanyVerificationRequestActivity,
            bitmap,
            uri,
            "COMMON",
            filename,
            object : MultiPartImageUploaded {
                override fun result(imageUpload: MultiPartImageUploadResponse?) {
                    if (imageUpload != null && imageUpload.data != null && imageUpload.data!!
                            .files.size > 0
                    ) {
                        val url: String = imageUpload.data!!.files.get(0).fileUrl!!
                        val uploadTransactionImage = CompanyUpdate()
                        uploadTransactionImage.imageURL = url
                        uploadTransactionImage.uenNo = uenET.text.toString()
                        val jsonRequest =
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(uploadTransactionImage)
                        uploadCompanyVerifyImage(
                            this@CompanyVerificationRequestActivity,
                            company.companyId,
                            jsonRequest,
                            object : GetRequestStatusResponseListener {
                                override fun onSuccessResponse(status: String?) {
                                    threeDotLoaderWhite.visibility = View.INVISIBLE
                                    if (!StringHelper.isStringNullOrEmpty(status)) {
                                        val dialogSuccess = alertPaymentDialogSmallSuccess(
                                            this@CompanyVerificationRequestActivity,
                                            getString(R.string.txt_thanks),
                                            "We’ll check your request and get back to you soon"
                                        )
                                        dialogSuccess.show()
                                        Handler().postDelayed({
                                            dialogSuccess.dismiss()
                                            val intent = Intent(
                                                this@CompanyVerificationRequestActivity,
                                                EssentialsProductListActivity::class.java
                                            )
                                            setResult(IS_COMPANY_STATUS_CHANGED, intent)
                                            finish()
                                        }, 2000)
                                    } else {
                                        val dialogFailure = alertPaymentDialogSmallFailure(
                                            this@CompanyVerificationRequestActivity,
                                            getString(R.string.txt_upload_failed),
                                            getString(R.string.txt_please_try_again)
                                        )
                                        dialogFailure.show()
                                        Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                                    }
                                }

                                override fun onErrorResponse(error: VolleyErrorHelper?) {
                                    threeDotLoaderWhite.visibility = View.INVISIBLE
                                    getToastRed(error?.errorMessage)
                                }
                            })
                    } else {
                        threeDotLoaderWhite.visibility = View.INVISIBLE
                        val dialogFailure = alertPaymentDialogSmallFailure(
                            this@CompanyVerificationRequestActivity,
                            getString(R.string.txt_upload_failed),
                            getString(R.string.txt_please_try_again)
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
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select file")
            val options = arrayOf(
                this.getString(R.string.txt_camera),
                this.getString(R.string.txt_gallery),
                this.getString(
                    R.string.txt_documents
                )
            )
            builder.setItems(
                options
            ) { dialog, which ->
                if (which == 0) {
                    dialog.dismiss()
                    requestPermission(PermissionCallback.TAKE_IMAGE, object : PermissionCallback {
                        override fun denied(requestCode: Int) {}
                        override fun allowed(requestCode: Int) {
                            openCamera()
                        }
                    })
                } else if (which == 1) {
                    dialog.dismiss()
                    requestPermission(PermissionCallback.WRITE_IMAGE, object : PermissionCallback {
                        override fun denied(requestCode: Int) {}
                        override fun allowed(requestCode: Int) {
                            openGallery()
                        }
                    })
                } else if (which == 2) {
                    dialog.dismiss()
                    requestPermission(PermissionCallback.WRITE_IMAGE, object : PermissionCallback {
                        override fun denied(requestCode: Int) {}
                        override fun allowed(requestCode: Int) {
                            openPdf()
                        }
                    })
                }
            }
            // Create the AlertDialog
            val dialog = builder.create()
            dialog.show()
        }

    private fun openCamera() {
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
    }

    private fun openGallery() {
        val captureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(captureIntent, PICK_FROM_GALLERY)
    }

    private fun openPdf() {
        val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = ServiceConstant.FILE_TYPE_PDF
        chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(chooseFile, PICK_FROM_FILES)
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

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            if (outPutfileUri != null) {
                try {
                    bitmap =
                        ImageRotationHelper.handleSamplingAndRotationBitmap(this, outPutfileUri)!!
                    val background = BitmapDrawable(resources, bitmap)
                    lytImgUpload!!.visibility = View.GONE
                    lytImgUpload!!.background = background
                    lytImgUpload!!.isClickable = false
                    lytImageDisplay!!.visibility = View.VISIBLE
                    imgDisplay!!.setImageBitmap(bitmap)
                    imgCancel!!.visibility = View.VISIBLE
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
                    )!!
                    val background = BitmapDrawable(resources, bitmap)
                    lytImgUpload!!.visibility = View.GONE
                    lytImgUpload!!.background = background
                    lytImgUpload!!.isClickable = false
                    lytImageDisplay!!.visibility = View.VISIBLE
                    imgDisplay!!.setImageBitmap(bitmap)
                    imgCancel!!.visibility = View.VISIBLE
                    lytImgUpload!!.isClickable = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == PICK_FROM_FILES && resultCode == RESULT_OK) {
            uri = data!!.data!!
            val uriString = uri.toString()
            val myFile = File(uriString)
            var displayName: String? = null
            if (uriString.startsWith("content://")) {
                var cursor: Cursor? = null
                try {
                    cursor = this.contentResolver.query(uri!!, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor!!.close()
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.name
            }
            txtPdfname!!.text = displayName
            lytImgUpload!!.visibility = View.GONE
            imgCancel!!.visibility = View.VISIBLE
            lytPdfUploaded!!.visibility = View.VISIBLE
            bitmap = InvoiceDataManager(this).decodeSampledBitmapFromImageUri(
                Uri.fromFile(myFile),
                35,
                35
            )!!
            lytImgUpload!!.isClickable = false
        }
    }

    var permissionCallback: PermissionCallback? = null
    fun requestPermission(requestCode: Int, permissionCallback: PermissionCallback?) {
        this.permissionCallback = permissionCallback
        when (requestCode) {
            PermissionCallback.WRITE_IMAGE -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is not granted => Request for permission
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
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
        grantResults: IntArray,
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
        private const val PICK_FROM_CAMERA = 0
        private const val PICK_FROM_GALLERY = 1
        private const val PICK_FROM_FILES = 2
        const val IS_COMPANY_STATUS_CHANGED = 2
    }
}