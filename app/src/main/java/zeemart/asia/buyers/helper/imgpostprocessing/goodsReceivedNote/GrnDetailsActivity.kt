package zeemart.asia.buyers.goodsReceivedNote

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.GrnOrderDetailsProductListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper.ShowEditGrnEmailSupplierDialog
import zeemart.asia.buyers.helper.DialogHelper.ShowGrnEmailSupplierDialog
import zeemart.asia.buyers.helper.DialogHelper.alertDialogPaymentFailure
import zeemart.asia.buyers.helper.DialogHelper.alertDialogPaymentSuccess
import zeemart.asia.buyers.helper.DialogHelper.onGrnEmailEditedListener
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.GrnRequest
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.order.Orders.Grn
import zeemart.asia.buyers.network.GRNApi
import zeemart.asia.buyers.network.GRNApi.saveGRNResponseListener
import zeemart.asia.buyers.orders.OrderDetailsActivity

/**
 * Created by RajPrudhviMarella on 17/June/2021.
 */
class GrnDetailsActivity : AppCompatActivity() {
    private var txtInvoiceDetailHeading: TextView? = null
    private lateinit var recyclerView: RecyclerView
    private var txtSupplierName: TextView? = null
    private var imgSupplierImage // image
            : ImageView? = null
    private var txtLocationValue // deliver to
            : TextView? = null
    private var txtLocationTag: TextView? = null
    private lateinit var txtLinkedToOrderValue // link to setonclick
            : TextView
    private var txtLinkedToTag: TextView? = null
    private var txtItemCount: TextView? = null
    private var txtReceiverValue: TextView? = null
    private var txtReceiverTag: TextView? = null
    private lateinit var btnDelete: ImageButton
    private lateinit var btnBack: ImageButton
    private var txtInvoiceNumber: TextView? = null
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private var mGrn: Grn? = null
    private var count = 0
    private lateinit var viewOriginalInvoice: TextView
    private lateinit var lytEmailToSupplier: LinearLayout
    private lateinit var lytEmailSettings: RelativeLayout
    private var txtDidntReceivedHeader: TextView? = null
    private lateinit var txtEmailThisOrderSwitch: TextView
    private lateinit var imgGreenTick: ImageView
    private var mOrder: Orders? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grn_details)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.GRN_DETAILS_JSON)) {
                mGrn = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.GRN_DETAILS_JSON), Grn::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }
        lytEmailToSupplier = findViewById(R.id.lyt_email_to_supplier)
        lytEmailSettings = findViewById(R.id.lyt_notification_on_off)
        lytEmailToSupplier.setVisibility(View.GONE)
        if (mOrder != null && mOrder!!.emailStatusDetails != null && mOrder!!.emailStatusDetails?.size!! > 0) {
            lytEmailToSupplier.setVisibility(View.VISIBLE)
        } else {
            lytEmailToSupplier.setVisibility(View.GONE)
        }
        txtDidntReceivedHeader = findViewById(R.id.txt_email_to_supplier_header)
        txtEmailThisOrderSwitch = findViewById(R.id.txt_email_this_supplier_switch_remains)
        imgGreenTick = findViewById(R.id.img_tick)
        if (mGrn!!.isSendEmailToSupplier) {
            txtEmailThisOrderSwitch.setText(
                String.format(
                    resources.getString(R.string.txt_email_sent_on),
                    DateHelper.getDateInDateMonthYearFormat(
                        mGrn!!.emailDate
                    )
                )
            )
            imgGreenTick.setVisibility(View.VISIBLE)
        } else {
            txtEmailThisOrderSwitch.setText(resources.getString(R.string.txt_email_supplier))
            imgGreenTick.setVisibility(View.GONE)
        }
        lytEmailSettings.setOnClickListener(View.OnClickListener {
            if (mGrn!!.isSendEmailToSupplier) {
                ShowGrnEmailSupplierDialog(this@GrnDetailsActivity, mGrn)
            } else {
                ShowEditGrnEmailSupplierDialog(
                    this@GrnDetailsActivity,
                    mOrder,
                    mGrn!!,
                    object : onGrnEmailEditedListener {
                        override fun onSendSelected(edtNotes: String?, supplierEmails: String?) {
                            callSaveOrdersApi(edtNotes, supplierEmails)
                        }
                    })
            }
        })
        txtInvoiceDetailHeading = findViewById(R.id.txt_invoice_details_heading)
        btnDelete = findViewById(R.id.more_options)
        btnDelete.setOnClickListener(View.OnClickListener { deleteGRN() })
        btnBack = findViewById(R.id.img_back_button)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        imgSupplierImage = findViewById(R.id.img_supplier_image)
        txtSupplierName = findViewById(R.id.txt_supplier_name)
        txtInvoiceNumber = findViewById(R.id.txt_invoice_number_invoice_details)
        txtLocationValue = findViewById(R.id.txt_location_value) // deliver to
        txtLocationTag = findViewById(R.id.txt_location_tag)
        txtLinkedToTag = findViewById(R.id.txt_linked_to_tag)
        txtLinkedToOrderValue = findViewById(R.id.txt_linked_to_order_value)
        txtItemCount = findViewById(R.id.txt_itemCount)
        recyclerView = findViewById(R.id.recyclerView)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.setLayoutManager(linearLayoutManager)
        recyclerView.setNestedScrollingEnabled(false)
        txtReceiverValue = findViewById(R.id.txt_payment_due_date_value) // due date
        txtReceiverTag = findViewById(R.id.txt_payment_due_date_tag)
        threeDotLoaderWhite = findViewById(R.id.lyt_loading_overlay)
        viewOriginalInvoice = findViewById(R.id.view_original_invoice) // link to image
        txtLinkedToOrderValue.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(this@GrnDetailsActivity, OrderDetailsActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mGrn!!.orderId)
            newIntent.putExtra(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID, mGrn!!.outlet?.outletId)
            startActivity(newIntent)
        })
        viewOriginalInvoice.setOnClickListener(View.OnClickListener {
            val invoicePdfFileName = "grn_" + mGrn!!.grnId + "_zeemart.pdf"
            if (!StringHelper.isStringNullOrEmpty(mGrn!!.pdfURL)) {
                downloadPDF(mGrn!!.pdfURL!!, invoicePdfFileName)
            } else ZeemartBuyerApp.getToastRed("PDF not available")
        }) // link to i
        setFont()
        setValues()
    }

    private fun setValues() {
        if (mGrn != null) {
            txtInvoiceNumber!!.text = "#" + mGrn!!.grnId
            txtSupplierName!!.text = "#" + mGrn!!.supplier?.supplierName
            if (!StringHelper.isStringNullOrEmpty(mGrn!!.supplier?.logoURL)) Picasso.get().load(
                mGrn!!.supplier?.logoURL
            ).placeholder(R.drawable.placeholder_all)
                .resize(PLACE_HOLDER_SUPPLIER_IMAGE_SIZE, PLACE_HOLDER_SUPPLIER_IMAGE_SIZE)
                .into(imgSupplierImage)
            txtLocationValue!!.text = mGrn!!.outlet?.outletName // deliver to
            txtLinkedToOrderValue!!.text =
                resources.getString(R.string.txt_order_with_hash_tag) + mGrn!!.orderId // link to order
            if (mGrn!!.products != null && mGrn!!.products?.size!! > 0) {
                count = count + mGrn!!.products?.size!!
            }
            if (mGrn!!.customLineItems != null && mGrn!!.customLineItems?.size!! > 0) {
                count = count + mGrn!!.customLineItems?.size!!
            }
            if (count == 0) {
                txtItemCount!!.text = String.format(getString(R.string.format_items), 0)
            } else if (count == 1) {
                txtItemCount!!.text =
                    count.toString() + " " + resources.getString(R.string.txt_item)
            } else {
                txtItemCount!!.text = String.format(getString(R.string.format_items), count)
            }
            val lstProduct: MutableList<Product> = ArrayList()
            if (mGrn!!.products != null) {
                lstProduct.addAll(mGrn!!.products!!)
            }
            if (mGrn!!.customLineItems != null) {
                for (i in mGrn!!.customLineItems?.indices!!) {
                    val product = Product()
                    product.productName = mGrn!!.customLineItems!![i].productName
                    product.sku = mGrn!!.customLineItems!![i].sku
                    product.unitSize = mGrn!!.customLineItems!![i].unitSize!!
                    product.quantity = (mGrn!!.customLineItems!![i].quantity!!)
                    lstProduct.add(product)
                }
            }
            if (mGrn!!.timeReceived != null && mGrn!!.timeReceived != 0L) {
                txtInvoiceDetailHeading!!.text =
                    resources.getString(R.string.txt_received) + " - " + DateHelper.getDateInDateMonthYearFormat(
                        mGrn!!.timeReceived
                    )
            } else {
                txtInvoiceDetailHeading!!.text = resources.getString(R.string.txt_received)
            }
            if (mGrn!!.createdBy != null && !StringHelper.isStringNullOrEmpty(mGrn!!.createdBy?.firstName) && !StringHelper.isStringNullOrEmpty(
                    mGrn!!.createdBy?.lastName
                )
            ) {
                txtReceiverValue!!.text =
                    mGrn!!.createdBy?.firstName + " " + mGrn!!.createdBy?.lastName
            } else {
                txtReceiverValue!!.text =
                    SharedPref.read(SharedPref.USER_FIRST_NAME, "") + " " + SharedPref.read(
                        SharedPref.USER_LAST_NAME,
                        ""
                    )
            }
            recyclerView!!.adapter = GrnOrderDetailsProductListAdapter(this, lstProduct)
        }
    }

    private fun deleteGRN() {
        val builder = AlertDialog.Builder(this@GrnDetailsActivity)
        builder.setPositiveButton(R.string.txt_delete) { dialog, which ->
            dialog.dismiss()
            threeDotLoaderWhite!!.visibility = View.VISIBLE
            GRNApi.deleteGrn(
                this@GrnDetailsActivity,
                mGrn!!.grnId,
                mGrn!!.outlet!!,
                object : saveGRNResponseListener {
                    override fun onSuccessResponse(response: String?) {
                        threeDotLoaderWhite!!.visibility = View.GONE
                        finish()
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        threeDotLoaderWhite!!.visibility = View.GONE
                        finish()
                    }
                })
        }
        builder.setNegativeButton(R.string.txt_cancel) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setTitle(getString(R.string.txt_delete_grn))
        dialog.setMessage(getString(R.string.txt_delete_grn_sure))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtInvoiceDetailHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtInvoiceNumber,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLocationValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtItemCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceiverValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceiverTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLinkedToTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLocationTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            viewOriginalInvoice,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLinkedToOrderValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDidntReceivedHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEmailThisOrderSwitch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) { // If request is cancelled, the result arrays are empty.
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                if (mGrn != null) {
                    val invoicePdfFileName = "grn_" + mGrn!!.grnId + "_zeemart.pdf"
                    if (!StringHelper.isStringNullOrEmpty(mGrn!!.pdfURL)) {
                        downloadPDF(mGrn!!.pdfURL!!, invoicePdfFileName)
                    } else ZeemartBuyerApp.getToastRed("PDF not available")
                }
            }
        }
    }

    /**
     * downloads the Order PDF file to the Downlpoads folder
     */
    private fun downloadPDF(pdfUrl: String, fileName: String) {
        if (ContextCompat.checkSelfPermission(
                this@GrnDetailsActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request for permission
            ActivityCompat.requestPermissions(
                this@GrnDetailsActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            val uri = Uri.parse(pdfUrl)
            val request = DownloadManager.Request(uri)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            ) //mOrder.getOrderId().concat("_zeemart.pdf"));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // to notify when download is complete
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }
    }

    private fun callSaveOrdersApi(edtNotes: String?, supplierEmails: String?) {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        val grnRequest = GrnRequest()
        grnRequest.noteToSupplier = edtNotes
        grnRequest.isSendEmailToSupplier = true
        grnRequest.supplierNotificationEmail = supplierEmails
        GRNApi.edtGrn(
            this,
            ZeemartBuyerApp.gsonExposeExclusive.toJson(grnRequest),
            mGrn!!.grnId,
            mOrder!!.outlet!!,
            object : saveGRNResponseListener {
                override fun onSuccessResponse(response: String?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    if (response != null) {
                        val currentEpochTime = System.currentTimeMillis() / 1000
                        mGrn!!.emailDate = currentEpochTime
                        mGrn!!.isSendEmailToSupplier = true
                        mGrn!!.supplierNotificationEmail = supplierEmails
                        mGrn!!.noteToSupplier = edtNotes
                        if (mGrn!!.isSendEmailToSupplier) {
                            txtEmailThisOrderSwitch!!.text = String.format(
                                resources.getString(R.string.txt_email_sent_on),
                                DateHelper.getDateInDateMonthYearFormat(
                                    mGrn!!.emailDate
                                )
                            )
                            imgGreenTick!!.visibility = View.VISIBLE
                        }
                        val dialogSuccess = alertDialogPaymentSuccess(
                            this@GrnDetailsActivity, getString(
                                R.string.txt_saved
                            )
                        )
                        Handler().postDelayed({ dialogSuccess.dismiss() }, 3000)
                    } else {
                        threeDotLoaderWhite!!.visibility = View.GONE
                        val dialogFailure = alertDialogPaymentFailure(
                            this@GrnDetailsActivity, getString(
                                R.string.txt_sent
                            ), ""
                        )
                        dialogFailure.show()
                        Handler().postDelayed({ dialogFailure.dismiss() }, 3000)
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    val dialogFailure = alertDialogPaymentFailure(
                        this@GrnDetailsActivity, getString(
                            R.string.txt_failed
                        ), ""
                    )
                    dialogFailure.show()
                    Handler().postDelayed({ dialogFailure.dismiss() }, 3000)
                }
            })
    }

    companion object {
        private val PLACE_HOLDER_SUPPLIER_IMAGE_SIZE = CommonMethods.dpToPx(50)
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }
}