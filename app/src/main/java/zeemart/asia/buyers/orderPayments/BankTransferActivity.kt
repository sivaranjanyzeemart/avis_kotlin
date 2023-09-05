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

class BankTransferActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var txtHeader: TextView
    private lateinit var txtBankTransfer: TextView
    private lateinit var txtImportant: TextView
    private lateinit var txtBankNameTag: TextView
    private lateinit var txtBankNameValue: TextView
    private lateinit var txtAccountNoTag: TextView
    private lateinit var txtAccountNoValue: TextView
    private lateinit var txtRecipientNameTag: TextView
    private lateinit var txtRecipientNameValue: TextView
    private lateinit var txtReferenceTag: TextView
    private lateinit var txtReferenceValue: TextView
    private lateinit var txtAmountTag: TextView
    private lateinit var txtAmountValue: TextView
    private lateinit var btnTransfer: Button
    private lateinit var btnCopyAccountNo: Button
    private lateinit var btnCopyRecipientName: Button
    private lateinit var btnCopyReferenceNo: Button
    private lateinit var btnCopyAmount: Button
    private lateinit var imgBack: ImageView
    private lateinit var mOrder: Orders
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_transfer)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }
        txtHeader = findViewById(R.id.txt_product_list_heading)
        txtBankTransfer = findViewById(R.id.desc_bt)
        txtImportant = findViewById(R.id.desc_imp)
        txtBankNameTag = findViewById(R.id.header_bank_name)
        txtBankNameValue = findViewById(R.id.txt_bank_name)
        txtAccountNoTag = findViewById(R.id.header_acc_no)
        txtAccountNoValue = findViewById(R.id.txt_acc_no)
        txtRecipientNameTag = findViewById(R.id.header_recipient_name)
        txtRecipientNameValue = findViewById(R.id.txt_recipient_name)
        txtReferenceTag = findViewById(R.id.header_reference)
        txtReferenceValue = findViewById(R.id.txt_reference)
        txtAmountTag = findViewById(R.id.header_amount)
        txtAmountValue = findViewById(R.id.txt_amount)
        btnTransfer = findViewById(R.id.btn_bank_transfer)
        btnTransfer.setOnClickListener(this)
        btnCopyAccountNo = findViewById(R.id.btn_acc_no_copy)
        btnCopyAccountNo.setOnClickListener(this)
        btnCopyRecipientName = findViewById(R.id.btn_recipient_copy)
        btnCopyRecipientName.setOnClickListener(this)
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
                override fun onSuccessResponse(paymentBankAccountDetails: PaymentBankAccountDetails?) {
                    if (paymentBankAccountDetails != null && paymentBankAccountDetails.data != null && paymentBankAccountDetails.data!![0].bank != null && mOrder != null) {
                        txtBankNameValue!!.text =
                            paymentBankAccountDetails.data!![0].bank!!.bankName
                        txtAccountNoValue!!.text =
                            paymentBankAccountDetails.data!![0].bank!!.accountNumber
                        txtRecipientNameValue!!.text =
                            paymentBankAccountDetails.data!![0].bank!!.accountName
                        txtReferenceValue!!.text = mOrder!!.orderId
                        txtAmountValue!!.text = mOrder!!.amount!!.total!!.displayValue
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
        setTypefaceView(txtBankTransfer, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtImportant, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtBankNameTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtBankNameValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtAccountNoTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtAccountNoValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtRecipientNameTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtRecipientNameValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtReferenceTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtReferenceValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtAmountTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtAmountValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnTransfer, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_bank_transfer -> {
                if (!StringHelper.isStringNullOrEmpty(mOrder!!.dealNumber)) {
                    AnalyticsHelper.logAction(
                        this@BankTransferActivity,
                        AnalyticsHelper.TAP_ORDER_PAY_BY_BANKTRANSFER,
                        mOrder!!.orderId,
                        mOrder!!.outlet!!,
                        true,
                        false,
                        mOrder!!.amount!!.total!!
                    )
                }
                if (!StringHelper.isStringNullOrEmpty(mOrder!!.essentialsId)) {
                    AnalyticsHelper.logAction(
                        this@BankTransferActivity,
                        AnalyticsHelper.TAP_ORDER_PAY_BY_BANKTRANSFER,
                        mOrder!!.orderId,
                        mOrder!!.outlet!!,
                        false,
                        true,
                        mOrder!!.amount!!.total!!
                    )
                }
                val newIntent = Intent(this@BankTransferActivity, PaymentConfirmation::class.java)
                val json = ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
                newIntent.putExtra(ZeemartAppConstants.ORDER_DETAILS_JSON, json)
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_ORDER_BANK_TRANSFER
                )
                startActivity(newIntent)
            }
            R.id.btn_acc_no_copy -> {
                val acc_no_clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val acc_clip =
                    ClipData.newPlainText(txtAccountNoValue!!.text, txtAccountNoValue!!.text)
                acc_no_clipboard.setPrimaryClip(acc_clip)
                displayCopiedDialog()
            }
            R.id.btn_recipient_copy -> {
                val rec_clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val rec_clip = ClipData.newPlainText(
                    txtRecipientNameValue!!.text,
                    txtRecipientNameValue!!.text
                )
                rec_clipboard.setPrimaryClip(rec_clip)
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
        val dialogSuccess = alertDialogSmallSuccess(this@BankTransferActivity, "Copied")
        Handler().postDelayed({ dialogSuccess.dismiss() }, 500)
    }
}