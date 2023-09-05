package zeemart.asia.buyers.orderPayments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DialogHelper.alertDialogSmallSuccess
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.OrderPayments.PaymentBankAccountDetails
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.network.PaymentApi.Companion.retrieveBankAccountDetails
import zeemart.asia.buyers.network.PaymentApi.RetrieveAccountDetailsListener
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orderPayments.PaymentConfirmation

class PayNowActivity : AppCompatActivity(), View.OnClickListener {
    private var txtHeader: TextView? = null
    private var txtPayNow: TextView? = null
    private var txtImportant: TextView? = null
    private var txtTransfer: TextView? = null
    private var txtUENTag: TextView? = null
    private var txtUENValue: TextView? = null
    private var txtEntityNameTag: TextView? = null
    private var txtEntityNameValue: TextView? = null
    private var txtReferenceTag: TextView? = null
    private var txtReferenceValue: TextView? = null
    private var txtAmountTag: TextView? = null
    private var txtAmountValue: TextView? = null
    private var txtUpload: TextView? = null
    private var txtUploadDesc: TextView? = null
    private var imgQR: ImageView? = null
    private var txtDownloadQR: TextView? = null
    private lateinit var btnTransfer: Button
    private lateinit var btnCopyUEN: Button
    private lateinit var btnCopyReferenceNo: Button
    private lateinit var btnCopyAmount: Button
    private lateinit var imgBack: ImageView
    private var mOrder: Orders? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_now)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }
        txtHeader = findViewById(R.id.txt_product_list_heading)
        txtPayNow = findViewById(R.id.desc_py)
        txtTransfer = findViewById(R.id.txt_transfer)
        txtImportant = findViewById(R.id.desc_imp)
        txtUENTag = findViewById(R.id.header_uen)
        txtUENValue = findViewById(R.id.txt_uen)
        txtEntityNameTag = findViewById(R.id.header_entity_name)
        txtEntityNameValue = findViewById(R.id.txt_entity_name)
        txtReferenceTag = findViewById(R.id.header_reference)
        txtReferenceValue = findViewById(R.id.txt_reference)
        txtAmountTag = findViewById(R.id.header_amount)
        txtAmountValue = findViewById(R.id.txt_amount)
        txtUpload = findViewById(R.id.txt_upload)
        txtUploadDesc = findViewById(R.id.header_upload)
        imgQR = findViewById(R.id.img_qr)
        txtDownloadQR = findViewById(R.id.txt_download)
        btnTransfer = findViewById(R.id.btn_pay_now)
        btnTransfer.setOnClickListener(this)
        btnCopyUEN = findViewById(R.id.btn_uen_copy)
        btnCopyUEN.setOnClickListener(this)
        btnCopyReferenceNo = findViewById(R.id.btn_reference_copy)
        btnCopyReferenceNo.setOnClickListener(this)
        btnCopyAmount = findViewById(R.id.btn_amount_copy)
        btnCopyAmount.setOnClickListener(this)
        imgBack = findViewById(R.id.bt_back_btn)
        imgBack.setOnClickListener(this)
        loadBankDetails()
        setFont()
    }

    private fun loadBankDetails() {
        retrieveBankAccountDetails(
            this,
            SharedPref.defaultOutlet!!,
            object : RetrieveAccountDetailsListener {
                override fun onSuccessResponse(bankAccountDetails: PaymentBankAccountDetails?) {
                    if (bankAccountDetails != null && bankAccountDetails.data != null && bankAccountDetails.data!![0].payNow != null && mOrder != null) {
                        txtUENValue!!.text = bankAccountDetails.data!![0].payNow!!.uenId
                        txtEntityNameValue!!.text =
                            bankAccountDetails.data!![0].payNow!!.accountName
                        txtReferenceValue!!.text = mOrder!!.orderId
                        txtAmountValue!!.text = mOrder!!.amount!!.total!!.displayValue
                        Picasso.get().load(bankAccountDetails.data!![0].payNow!!.qrCode)
                            .placeholder(R.drawable.announcement_place_holder).centerCrop().fit()
                            .into(imgQR)
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    val errorMessage = error!!.errorMessage
                    val errorType = error.errorType
                    if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        getToastRed(errorMessage)
                    }
                }
            })
    }

    private fun setFont() {
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtPayNow, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtImportant, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtTransfer, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtUENTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtUENValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtEntityNameTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtEntityNameValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtReferenceTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtReferenceValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtAmountTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtAmountValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtUpload, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtUploadDesc, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDownloadQR, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnTransfer, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_pay_now -> {
                if (!StringHelper.isStringNullOrEmpty(mOrder!!.dealNumber)) {
                    AnalyticsHelper.logAction(
                        this@PayNowActivity,
                        AnalyticsHelper.TAP_ORDER_PAY_BY_PAYNOW,
                        mOrder!!.orderId,
                        mOrder!!.outlet!!,
                        true,
                        false,
                        mOrder!!.amount!!.total!!
                    )
                }
                if (!StringHelper.isStringNullOrEmpty(mOrder!!.essentialsId)) {
                    AnalyticsHelper.logAction(
                        this@PayNowActivity,
                        AnalyticsHelper.TAP_ORDER_PAY_BY_PAYNOW,
                        mOrder!!.orderId,
                        mOrder!!.outlet!!,
                        false,
                        true,
                        mOrder!!.amount!!.total!!
                    )
                }
                val newIntent = Intent(this@PayNowActivity, PaymentConfirmation::class.java)
                val json = ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
                newIntent.putExtra(ZeemartAppConstants.ORDER_DETAILS_JSON, json)
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_ORDER_PAY_NOW
                )
                startActivity(newIntent)
            }
            R.id.btn_uen_copy -> {
                val acc_no_clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val acc_clip = ClipData.newPlainText(txtUENValue!!.text, txtUENValue!!.text)
                acc_no_clipboard.setPrimaryClip(acc_clip)
                displayCopiedDialog()
            }
            R.id.btn_reference_copy -> {
                val ref_clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val ref_clip =
                    ClipData.newPlainText(txtReferenceValue!!.text, txtReferenceValue!!.text)
                ref_clipboard.setPrimaryClip(ref_clip)
                displayCopiedDialog()
            }
            R.id.btn_amount_copy -> {
                val amt_clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val amt_clip = ClipData.newPlainText(
                    mOrder!!.amount!!.total!!.amount.toString(),
                    mOrder!!.amount!!.total!!.amount.toString()
                )
                amt_clipboard.setPrimaryClip(amt_clip)
                displayCopiedDialog()
            }
            R.id.bt_back_btn -> finish()
            else -> {}
        }
    }

    private fun displayCopiedDialog() {
        val dialogSuccess = alertDialogSmallSuccess(this@PayNowActivity, "Copied")
        Handler().postDelayed({ dialogSuccess.dismiss() }, 500)
    }
}