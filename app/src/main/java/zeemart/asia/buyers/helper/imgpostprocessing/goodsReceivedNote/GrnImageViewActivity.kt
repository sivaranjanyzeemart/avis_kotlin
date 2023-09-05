package zeemart.asia.buyers.goodsReceivedNote

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.invoices.InvoiceDataManager
import java.io.File

/**
 * Created by RajPrudhviMarella on 15/June/2021.
 */
class GrnImageViewActivity : AppCompatActivity() {
    private lateinit var txtHeader: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: ImageButton
    lateinit var imageViewProduct: ImageView
    lateinit var imageViewNoImage: ImageView
    lateinit var txtNoImage: TextView
    lateinit var txtInvoicePdfDisplayName: TextView
    lateinit var txtNoPreviewForPdf: TextView
    lateinit var lytInvoicePdfView: RelativeLayout
    lateinit var imageUris: InQueueForUploadDataModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grn_image_view)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.INVOICE_IMAGE_URI)) {
                imageUris = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.INVOICE_IMAGE_URI),
                    InQueueForUploadDataModel::class.java
                )
            }
        }
        btnBack = findViewById(R.id.img_btn_cancel_review)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        btnDelete = findViewById(R.id.img_btn_delete_invoice)
        btnDelete.setOnClickListener(View.OnClickListener {
            val file = File(imageUris!!.imageDirectoryPath)
            val isDeleted = InvoiceDataManager(this@GrnImageViewActivity).deleteDirectory(file)
            Log.d("FILE DELETED", "$isDeleted***")
            val newIntent = Intent()
            val invoiceImageListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUris)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            setResult(ZeemartAppConstants.ResultCode.RESULT_DELETED, newIntent)
            finish()
        })
        txtHeader = findViewById(R.id.txt_invoice_image_name)
        imageViewProduct = findViewById(R.id.image_product_product_details)
        imageViewNoImage = findViewById(R.id.image_no_image)
        txtNoImage = findViewById(R.id.txt_no_image)
        txtInvoicePdfDisplayName = findViewById(R.id.txt_invoice_pdf_name)
        txtNoPreviewForPdf = findViewById(R.id.txt_no_preview_for_pdf)
        lytInvoicePdfView = findViewById(R.id.lyt_invoice_pdf_view)
        setTypefaceView(
            txtInvoicePdfDisplayName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtNoPreviewForPdf, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        lytInvoicePdfView.setVisibility(View.GONE)
        imageViewProduct.setVisibility(View.GONE)
        if (!StringHelper.isStringNullOrEmpty(imageUris!!.selectedPdfOriginalName)) {
            txtHeader.setText(imageUris!!.selectedPdfOriginalName)
        }
        if (StringHelper.isStringNullOrEmpty(imageUris!!.imageFilePath)) {
            lytInvoicePdfView.setVisibility(View.GONE)
            imageViewNoImage.setVisibility(View.VISIBLE)
            imageViewProduct.setVisibility(View.GONE)
            txtNoImage.setVisibility(View.VISIBLE)
        } else {
            imageViewProduct.setVisibility(View.VISIBLE)
            lytInvoicePdfView.setVisibility(View.GONE)
            if (imageUris!!.isInvoiceSelectedIsImage) {
                val uri = imageUris!!.imageFilePath
                val builder = Picasso.Builder(this)
                builder.listener { picasso, uri, exception ->
                    Log.d("EXCEPTION", "****************************************")
                    exception.printStackTrace()
                }
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                val width = displayMetrics.widthPixels
                Log.d("ROTATION", imageUris!!.rotation.toString() + "****")
                builder.build().load(File(uri)).into(imageViewProduct)
            } else {
                imageViewProduct.setVisibility(View.GONE)
                lytInvoicePdfView.setVisibility(View.VISIBLE)
                txtInvoicePdfDisplayName.setText(imageUris!!.selectedPdfOriginalName)
                txtNoPreviewForPdf.setAlpha(0.3f)
            }
        }
    }
}