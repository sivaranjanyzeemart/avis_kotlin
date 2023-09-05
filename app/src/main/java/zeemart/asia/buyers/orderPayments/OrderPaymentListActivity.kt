package zeemart.asia.buyers.orderPayments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.OrderPayments.UploadTransactionImage
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.paymentimport.PayInvoiceResponse
import zeemart.asia.buyers.models.paymentimport.PaymentCardDetails
import zeemart.asia.buyers.models.paymentimport.PaymentStatus
import zeemart.asia.buyers.models.paymentimportimport.PayOrderRequest
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.orders.OrderDetailsActivity

/**
 * Created by RajPrudhviMarella on 5/4/2020.
 */
class OrderPaymentListActivity : AppCompatActivity() {
    private lateinit var txtHeader: TextView
    private lateinit var imgClose: ImageView
    private lateinit var lytPayButton: RelativeLayout
    private lateinit var txtPaymentButton: TextView
    private lateinit var txtPayBeforeDate: TextView
    private lateinit var txtOrderUpfront: TextView
    private lateinit var txtOrderCutoffTime: TextView
    private lateinit var txtAmountValue: TextView
    private lateinit var txtAmountTag: TextView
    private lateinit var txtSelectPaymentMethod: TextView
    private lateinit var lytCreditDebitCard: RelativeLayout
    private lateinit var lytFinaxarPay: RelativeLayout
    private lateinit var txtFinaxarpay: TextView
    private lateinit var txtFinaxarpayTag: TextView
    private lateinit var txtFinaxarTagValue: TextView
    private lateinit var lytGrabFinance: RelativeLayout
    private lateinit var txtGrabFinance: TextView
    private lateinit var txtGrabFinanceTag: TextView
    private lateinit var txtGrabFinanceTagValue: TextView
    private lateinit var lytFundingSocieties: RelativeLayout
    private lateinit var txtFundingSocieties: TextView
    private lateinit var txtFundingSocietiesTag: TextView
    private lateinit var txtFundingSocietiesTagValue: TextView
    private lateinit var txtCreditDebitCard: TextView
    private lateinit var lytBankTransfer: RelativeLayout
    private lateinit var txtBankTransfer: TextView
    private lateinit var txtBankTransferTag: TextView
    private lateinit var lytPayNow: RelativeLayout
    private lateinit var txtPayNow: TextView
    private lateinit var txtPayNowTag: TextView
    private lateinit var txtCardPaymentAvailabilityTag: TextView
    private lateinit var mOrder: Orders
    private lateinit var cardReferenceNo: String
    private var isPaidSuccessfully = false
    private lateinit var threeDotLoader: CustomLoadingViewWhite
    private lateinit var txtCutOffTime: TextView
    private lateinit var lstCompany: Companies
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_payment_list)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson<Orders>(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }
        threeDotLoader = findViewById<CustomLoadingViewWhite>(R.id.three_dot_loader)
        threeDotLoader.setVisibility(View.GONE)
        txtHeader = findViewById<TextView>(R.id.txt_supplier_name_review_payment)
        imgClose = findViewById<ImageView>(R.id.btn_close_review_payment)
        lytPayButton = findViewById<RelativeLayout>(R.id.lyt_pay_button)
        txtPaymentButton = findViewById<TextView>(R.id.txt_payment_button)
        txtPayBeforeDate = findViewById<TextView>(R.id.txt_pay_before_date)
        txtCutOffTime = findViewById<TextView>(R.id.txt_pay_cut_off_date)
        txtOrderUpfront = findViewById<TextView>(R.id.txt_order_requires_upfront)
        txtOrderCutoffTime =
            findViewById<TextView>(R.id.txt_order_will_process_before_cutt_off_time)
        txtAmountValue = findViewById<TextView>(R.id.txt_amount_value)
        txtAmountTag = findViewById<TextView>(R.id.txt_amount_header)
        txtSelectPaymentMethod = findViewById<TextView>(R.id.txt_select_payment_method)
        lytCreditDebitCard = findViewById<RelativeLayout>(R.id.lyt_credit_debit_card)
        lytFinaxarPay = findViewById<RelativeLayout>(R.id.layout_finaxarpay)
        txtFinaxarpay = findViewById<TextView>(R.id.txt_finaxarpay)
        txtFinaxarpayTag = findViewById<TextView>(R.id.txt_finaxarpay_tag)
        txtFinaxarTagValue = findViewById<TextView>(R.id.txt_finaxarpay_tag_value)
        lytGrabFinance = findViewById<RelativeLayout>(R.id.layout_grab_finance)
        txtGrabFinance = findViewById<TextView>(R.id.txt_grab_finance)
        txtGrabFinanceTag = findViewById<TextView>(R.id.txt_grab_finance_tag)
        txtGrabFinanceTagValue = findViewById<TextView>(R.id.txt_grab_finance_tag_value)
        lytFundingSocieties = findViewById<RelativeLayout>(R.id.layout_funding_societies)
        txtFundingSocieties = findViewById<TextView>(R.id.txt_funding_societies)
        txtFundingSocietiesTag = findViewById<TextView>(R.id.txt_funding_societies_tag)
        txtFundingSocietiesTagValue = findViewById<TextView>(R.id.txt_funding_societies_tag_Value)
        txtCreditDebitCard = findViewById<TextView>(R.id.txt_credit_card)
        lytBankTransfer = findViewById<RelativeLayout>(R.id.layout_bank_transfer)
        txtBankTransfer = findViewById<TextView>(R.id.txt_bank_transfer)
        txtBankTransferTag = findViewById<TextView>(R.id.txt_bank_transfer_tag)
        lytPayNow = findViewById<RelativeLayout>(R.id.lyt_pay_now)
        txtPayNow = findViewById<TextView>(R.id.txt_pay_now)
        txtPayNowTag = findViewById<TextView>(R.id.txt_pay_now_tag)
        txtCardPaymentAvailabilityTag =
            findViewById<TextView>(R.id.txt_card_payment_available_some_users)
        imgClose!!.setOnClickListener { finish() }
        lytPayButton.setOnClickListener(View.OnClickListener { finish() })
        lytCreditDebitCard.setOnClickListener(View.OnClickListener {
            mOrder?.amount?.total?.amount?.let { it1 ->
                createPayConfirmationDialog(
                    it1
                )
            }
        })
        lytBankTransfer.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@OrderPaymentListActivity, BankTransferActivity::class.java)
            val json: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
            intent.putExtra(ZeemartAppConstants.ORDER_DETAILS_JSON, json)
            startActivity(intent)
        })
        lytPayNow.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@OrderPaymentListActivity, PayNowActivity::class.java)
            val json: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(mOrder)
            intent.putExtra(ZeemartAppConstants.ORDER_DETAILS_JSON, json)
            startActivity(intent)
        })
        lytFinaxarPay.setOnClickListener(View.OnClickListener {
            createFinaxarAlert(
                UploadTransactionImage.PaymentType.FINAXAR.name
            )
        })
        lytFundingSocieties.setOnClickListener(View.OnClickListener {
            createFinaxarAlert(
                UploadTransactionImage.PaymentType.FUNDING_SOCIETIES.name
            )
        })
        lytGrabFinance.setOnClickListener(View.OnClickListener {
            createFinaxarAlert(
                UploadTransactionImage.PaymentType.GRAB_FINANCE.name
            )
        })
        lytFinaxarPay.setVisibility(View.GONE)
        lytGrabFinance.setVisibility(View.GONE)
        lytFundingSocieties.setVisibility(View.GONE)
        setFont()
        callApi()
        if (mOrder != null) {
            if (mOrder.timeCutOff != null) {
                val cutOffTime: Long = (mOrder.timeCutOff!! - 3600) * 1000
                val currentTime = System.currentTimeMillis()
                if (currentTime < cutOffTime) {
                    lytPayNow.setAlpha(1f)
                    lytPayNow.setClickable(true)
                    lytBankTransfer.setAlpha(1f)
                    lytBankTransfer.setClickable(true)
                    lytFinaxarPay.setAlpha(1f)
                    lytFinaxarPay.setClickable(true)
                    lytFundingSocieties.setAlpha(1f)
                    lytFundingSocieties.setClickable(true)
                } else {
                    lytPayNow.setAlpha(0.5f)
                    lytPayNow.setClickable(false)
                    lytBankTransfer.setAlpha(0.5f)
                    lytBankTransfer.setClickable(false)
                    lytFinaxarPay.setAlpha(0.5f)
                    lytFinaxarPay.setClickable(false)
                    lytFundingSocieties.setAlpha(0.5f)
                    lytFundingSocieties.setClickable(false)
                }
                val date: String = kotlin.String.format(
                    getResources().getString(R.string.txt_pay_before) + " ",
                    DateHelper.getDateInDateMonthHourMinute(mOrder.timeCutOff)
                )
                txtPayBeforeDate.setText(date)
            }
            if (mOrder.amount?.total != null) txtAmountValue.setText(mOrder.amount?.total!!.displayValue)
        }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPaymentButton,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPayBeforeDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderCutoffTime,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderUpfront,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAmountValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAmountTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCreditDebitCard,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtBankTransfer,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtBankTransferTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtFinaxarpay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtFinaxarpayTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtGrabFinance,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtGrabFinanceTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtGrabFinanceTagValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtFundingSocietiesTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtFundingSocietiesTagValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtFinaxarTagValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtFundingSocieties,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPayNow,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPayNowTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectPaymentMethod,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCutOffTime,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCardPaymentAvailabilityTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun createPayConfirmationDialog(totalAmount: Double) {
        val d = Dialog(this@OrderPaymentListActivity, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_payment_confirmation)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtInvoiceTotalTag: TextView = d.findViewById<TextView>(R.id.txt_invoice_total_tag)
        txtInvoiceTotalTag.setText(getResources().getString(R.string.txt_order_total))
        val txtInvoiceTotal: TextView = d.findViewById<TextView>(R.id.txt_invoice_total_value)
        val txtProcessFeeTag: TextView = d.findViewById<TextView>(R.id.txt_process_fee_tag)
        val txtProcessFee: TextView = d.findViewById<TextView>(R.id.txt_process_fee_value)
        val imgCard = d.findViewById<ImageView>(R.id.img_card)
        val txtCreditAvailable: TextView = d.findViewById<TextView>(R.id.txt_available_credit)
        txtCreditAvailable.setText(
            kotlin.String.format(
                getResources().getString(R.string.txt_available_credit),
                PriceDetails().displayValue
            )
        )
        val btnUseCredits: TextView = d.findViewById<TextView>(R.id.txt_use_credit)
        val lytUseCredits: RelativeLayout = d.findViewById<RelativeLayout>(R.id.lyt_use_credit)
        lytUseCredits.setVisibility(View.GONE)
        val txtCredits: TextView = d.findViewById<TextView>(R.id.txt_credits)
        val btnRemoveCredits: TextView = d.findViewById<TextView>(R.id.txt_remove_credit)
        val txtCreditFeeValue: TextView = d.findViewById<TextView>(R.id.txt_credit_fee_value)
        val lytCardDetails: LinearLayout = d.findViewById<LinearLayout>(R.id.lyt_card_detail)
        val txtTotalTag: TextView = d.findViewById<TextView>(R.id.txt_total)
        txtTotalTag.setVisibility(View.GONE)
        val lytRemoveCredits: RelativeLayout =
            d.findViewById<RelativeLayout>(R.id.lyt_remove_credit)
        lytRemoveCredits.setVisibility(View.GONE)
        val txtCardNumber: TextView = d.findViewById<TextView>(R.id.txt_card_number)
        val txtTotal: TextView = d.findViewById<TextView>(R.id.txt_total_value)
        val btnPay = d.findViewById<Button>(R.id.btn_pay)
        val txtPaymentTerms: TextView = d.findViewById<TextView>(R.id.txt_payment_terms)
        val lytAddCard: LinearLayout = d.findViewById<LinearLayout>(R.id.lyt_add_new_card)
        val txtAddCard: TextView = d.findViewById<TextView>(R.id.txt_add_new_Card)
        lytAddCard.setVisibility(View.GONE)
        txtPaymentTerms.setVisibility(View.GONE)
        ZeemartBuyerApp.setTypefaceView(
            txtInvoiceTotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtInvoiceTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtProcessFeeTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtProcessFee,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCardNumber,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnPay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPaymentTerms,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCreditAvailable,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnUseCredits,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCredits,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnRemoveCredits,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCreditFeeValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddCard,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val builder = SpannableStringBuilder()
        val str1 = SpannableString(getResources().getText(R.string.txt_payment_terms))
        str1.setSpan(
            ForegroundColorSpan(getResources().getColor(R.color.dark_grey)),
            0,
            str1.length,
            0
        )
        builder.append(str1)
        builder.append(" ")
        val str2 = SpannableString(getResources().getText(R.string.txt_payment_conditions))
        str2.setSpan(
            ForegroundColorSpan(getResources().getColor(R.color.color_azul_two)),
            0,
            str2.length,
            0
        )
        builder.append(str2)
        txtPaymentTerms.setText(builder, TextView.BufferType.SPANNABLE)

        /*Call eCredit API to get Credit Amount*/
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSupplierId(mOrder.supplier?.supplierId!!)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        mOrder.outlet?.let { outlets.add(it) }
        mOrder.outlet?.let {
            OutletsApi.getSpecificOutlet(
                this,
                it,
                object : OutletsApi.GetSpecificOutletResponseListener {
                    override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                        val apiParamsHelper = ApiParamsHelper()
                        if (specificOutlet != null && specificOutlet.data != null && specificOutlet.data!!.company != null && specificOutlet.data!!.company?.companyId != null) apiParamsHelper.setCompanyId(
                            specificOutlet.data!!.company?.companyId!!
                        )
                        PaymentApi.retrieveCompanyCardPaymentData(
                            this@OrderPaymentListActivity,
                            apiParamsHelper,
                            mOrder.outlet!!,
                            object : PaymentApi.CompanyCardDetailsListener {
                                override fun onSuccessResponse(paymentCardDetails: PaymentCardDetails?) {
                                    val cardList: MutableList<PaymentCardDetails.PaymentResponse> =
                                        ArrayList<PaymentCardDetails.PaymentResponse>()
                                    if (paymentCardDetails != null && paymentCardDetails.data != null && paymentCardDetails.data!!
                                            .size > 0
                                    ) {
                                        lytAddCard.setVisibility(View.GONE)
                                        for (i in 0 until paymentCardDetails.data!!.size) {
                                            if (paymentCardDetails.data!![i]
                                                    .status != null && paymentCardDetails.data!![i].status
                                                    .equals(PaymentStatus.ACTIVE.getmStatusName())
                                            ) {
                                                cardList.add(paymentCardDetails.data!![i])
                                            }
                                        }
                                        for (i in 0 until paymentCardDetails.data!!.size) {
                                            if (paymentCardDetails.data!!.get(i).isDefaultCard) {
                                                txtCardNumber.setText(
                                                    getResources().getString(R.string.txt_card_bullet) + " " + paymentCardDetails.data!!
                                                        .get(i).customerCardData
                                                        ?.cardLast4Digits
                                                )
                                                if (paymentCardDetails.data!![i]
                                                        .customerCardData
                                                        ?.isCardType(PaymentCardDetails.CardType.VISA) == true
                                                ) {
                                                    imgCard.setImageResource(R.drawable.visa)
                                                } else if (paymentCardDetails.data!![i]
                                                        .customerCardData
                                                        ?.isCardType(PaymentCardDetails.CardType.MASTERCARD) == true
                                                ) {
                                                    imgCard.setImageResource(R.drawable.master_card)
                                                } else {
                                                    imgCard.setImageResource(R.drawable.amex)
                                                }
                                                imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                                cardReferenceNo =
                                                    paymentCardDetails.data!![i].referenceNo.toString()
                                            }
                                        }
                                    } else {
                                        lytAddCard.setVisibility(View.VISIBLE)
                                        lytCardDetails.setVisibility(View.GONE)
                                    }
                                    lytAddCard.setOnClickListener(View.OnClickListener {
                                        DialogHelper.ShowAddNewCardDialog(
                                            this@OrderPaymentListActivity,
                                            specificOutlet?.data?.company?.companyId,
                                            object : DialogHelper.onCardAddedListener {
                                                override fun onCardSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse) {
                                                    selectedCardDetails.isDefaultCard
                                                    cardList.add(selectedCardDetails)
                                                    lytAddCard.setVisibility(View.GONE)
                                                    lytCardDetails.setVisibility(View.VISIBLE)
                                                    txtCardNumber.setText(
                                                        getResources().getString(R.string.txt_card_bullet) + " " + selectedCardDetails.customerCardData
                                                            ?.cardLast4Digits
                                                    )
                                                    if (selectedCardDetails.customerCardData
                                                            ?.isCardType(PaymentCardDetails.CardType.VISA) == true
                                                    ) {
                                                        imgCard.setImageResource(R.drawable.visa)
                                                    } else if (selectedCardDetails.customerCardData
                                                            ?.isCardType(PaymentCardDetails.CardType.MASTERCARD) == true
                                                    ) {
                                                        imgCard.setImageResource(R.drawable.master_card)
                                                    } else {
                                                        imgCard.setImageResource(R.drawable.amex)
                                                    }
                                                    imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                                    cardReferenceNo =
                                                        selectedCardDetails.referenceNo.toString()
                                                }
                                            })
                                    })
                                    lytCardDetails.setOnClickListener(View.OnClickListener {
                                        CommonMethods.avoidMultipleClicks(lytCardDetails)
                                        if (cardList != null && cardList.size > 0) {
                                            val cardListJson: String =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(cardList)
                                            DialogHelper.ShowCardListDialog(
                                                this@OrderPaymentListActivity,
                                                cardListJson,
                                                specificOutlet?.data?.company?.companyId,
                                                object : DialogHelper.onCardSelectedListener {
                                                    override fun onCardSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse?) {
                                                        for (i in cardList.indices) {
                                                            if (cardList[i].referenceNo
                                                                    .equals(selectedCardDetails?.referenceNo)
                                                            ) {
                                                                cardList[i].isDefaultCard
                                                            } else {
                                                                cardList[i].isDefaultCard
                                                            }
                                                        }
                                                        val paymentCardDefault = SetPaymentCardDefault()
                                                        if (specificOutlet != null && specificOutlet.data != null && specificOutlet.data!!.company != null && specificOutlet.data!!.company?.companyId != null) paymentCardDefault.companyId =
                                                            specificOutlet.data!!.company?.companyId
                                                        paymentCardDefault.referenceNo =
                                                            selectedCardDetails?.referenceNo
                                                        paymentCardDefault.setAsDefaultBy =
                                                            SharedPref.currentUserDetail
                                                        PaymentApi.setPaymentCardDefault(
                                                            this@OrderPaymentListActivity,
                                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                                paymentCardDefault
                                                            ),
                                                            object :
                                                                GetRequestStatusResponseListener {
                                                                override fun onSuccessResponse(status: String?) {
    //                                                   No Implementation required
                                                                }

                                                                override fun onErrorResponse(error: VolleyErrorHelper?) {
    //                                                   No Implementation required
                                                                }
                                                            })
                                                        txtCardNumber.setText(
                                                            getResources().getString(R.string.txt_card_bullet) + " " + selectedCardDetails?.customerCardData
                                                                ?.cardLast4Digits
                                                        )
                                                        if (selectedCardDetails?.customerCardData
                                                                ?.isCardType(PaymentCardDetails.CardType.VISA) == true
                                                        ) {
                                                            imgCard.setImageResource(R.drawable.visa)
                                                        } else if (selectedCardDetails?.customerCardData
                                                                ?.isCardType(PaymentCardDetails.CardType.MASTERCARD) == true
                                                        ) {
                                                            imgCard.setImageResource(R.drawable.master_card)
                                                        } else {
                                                            imgCard.setImageResource(R.drawable.amex)
                                                        }
                                                        imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                                        cardReferenceNo =
                                                            selectedCardDetails?.referenceNo.toString()
                                                    }
                                                })
                                        }
                                    })
                                }

                                override fun onErrorResponse(error: VolleyErrorHelper?) {
                                    val errorMessage: String? = error?.errorMessage
                                    val errorType: String? = error?.errorType
                                    if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                        ZeemartBuyerApp.getToastRed(errorMessage)
                                    }
                                }
                            })
                    }

                    override fun onError(error: VolleyErrorHelper?) {
                        val errorMessage: String? = error?.errorMessage
                        val errorType: String? = error?.errorType
                        if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                            ZeemartBuyerApp.getToastRed(errorMessage)
                        }
                    }
                })
        }
        val priceDetails = PriceDetails()
        priceDetails.amount = totalAmount
        txtInvoiceTotal.setText(priceDetails.displayValue)
        txtTotal.setText(priceDetails.displayValue)
        btnPay.setText(getString(R.string.txt_pay, priceDetails.displayValue))
        btnPay.setOnClickListener {
            CommonMethods.avoidMultipleClicks(btnPay)
            threeDotLoader.setVisibility(View.VISIBLE)
            val payOrderRequest = PayOrderRequest()
            payOrderRequest.orderId
            payOrderRequest.cardReferenceNumber
            val apiParamsHelpers = ApiParamsHelper()
            apiParamsHelpers.setSupplierId(mOrder.supplier?.supplierId!!)
            apiParamsHelpers.setOutletId(mOrder.outlet?.outletId!!)
            mOrder.outlet?.let { it1 ->
                PaymentApi.payOrder(
                    this@OrderPaymentListActivity,
                    payOrderRequest,
                    apiParamsHelpers,
                    it1,
                    object : PaymentApi.PayInvoiceListener {
                        override fun onSuccessResponse(payInvoice: PayInvoiceResponse?) {
                            if (payInvoice != null && payInvoice.data != null) {
                                if (payInvoice.data!!
                                        .isTransactionStatus(PayInvoiceResponse.TransactionStatus.PAID)
                                ) {
                                    threeDotLoader.setVisibility(View.GONE)
                                    isPaidSuccessfully = true
                                    if (!StringHelper.isStringNullOrEmpty(mOrder.dealNumber)) {
                                        AnalyticsHelper.logAction(
                                            this@OrderPaymentListActivity,
                                            AnalyticsHelper.TAP_ORDER_PAY_BY_CARD,
                                            mOrder.orderId,
                                            mOrder.outlet!!,
                                            true,
                                            false,
                                            mOrder.amount?.total!!
                                        )
                                    }
                                    if (!StringHelper.isStringNullOrEmpty(mOrder.essentialsId)) {
                                        AnalyticsHelper.logAction(
                                            this@OrderPaymentListActivity,
                                            AnalyticsHelper.TAP_ORDER_PAY_BY_CARD,
                                            mOrder.orderId,
                                            mOrder.outlet!!,
                                            false,
                                            true,
                                            mOrder.amount?.total!!
                                        )
                                    }
                                    val dialogSuccess: AlertDialog =
                                        DialogHelper.alertDialogPaymentSuccess(
                                            this@OrderPaymentListActivity, getString(
                                                R.string.txt_payment_success
                                            )
                                        )
                                    Handler().postDelayed({
                                        dialogSuccess.dismiss()
                                        lytPayButton.setVisibility(View.GONE)
                                        val intent = Intent(
                                            this@OrderPaymentListActivity,
                                            OrderDetailsActivity::class.java
                                        )
                                        intent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder.orderId)
                                        setResult(Activity.RESULT_OK, intent)
                                        finish()
                                    }, 2000)
                                } else {
                                    threeDotLoader.setVisibility(View.GONE)
                                    val dialogFailure: AlertDialog =
                                        DialogHelper.alertDialogPaymentFailure(
                                            this@OrderPaymentListActivity, getString(
                                                R.string.txt_payment_failed
                                            ), payInvoice.data!!.failureReason
                                        )
                                    dialogFailure.show()
                                    Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                                }
                            } else {
                                threeDotLoader.setVisibility(View.GONE)
                                isPaidSuccessfully = false
                                val dialogFailure: AlertDialog = DialogHelper.alertDialogPaymentFailure(
                                    this@OrderPaymentListActivity, getString(
                                        R.string.txt_payment_failed
                                    ), getString(R.string.txt_please_try_again)
                                )
                                dialogFailure.show()
                                Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                            }
                        }

                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                            val errorMessage: String? = error?.errorMessage
                            val errorType: String? = error?.errorType
                            if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                ZeemartBuyerApp.getToastRed(errorMessage)
                            } else {
                                ZeemartBuyerApp.getToastRed(errorMessage)
                            }
                        }
                    })
            }
            d.dismiss()
        }
        d.show()
    }

    override fun onBackPressed() {
        if (isPaidSuccessfully) {
            val intent = Intent(this@OrderPaymentListActivity, OrderDetailsActivity::class.java)
            intent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder.orderId)
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
    }

    private fun callApi() {
        OutletsApi.getSpecificOutletDetail(
            this,
            mOrder.outlet?.outletId,
            object : OutletsApi.GetSpecificOutletResponseListener {
                override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                    if (specificOutlet != null) {
                        val apiParamsHelper = ApiParamsHelper()
                        apiParamsHelper.setCompanyId(specificOutlet.data?.company?.companyId!!)
                        CompaniesApi.retrieveSpecificCompany(
                            this@OrderPaymentListActivity,
                            apiParamsHelper,
                            SharedPref.allOutlets,
                            object : CompaniesApi.SpecificCompanyResponseListener {
                                override fun onSuccessResponse(response: RetrieveCompaniesResponse.SpecificCompany?) {
                                    if (response != null && response.data != null) {
                                        lstCompany = response.data!!
                                        if (lstCompany.settings != null && lstCompany.settings!!.payments != null && lstCompany.settings!!.payments?.paymentSources != null && lstCompany.settings!!.payments?.paymentSources?.size!! > 0) {
                                            for (i in lstCompany.settings!!.payments?.paymentSources?.indices!!) {
                                                if (lstCompany.settings!!.payments?.paymentSources?.get(
                                                        i
                                                    )?.paymentType == UploadTransactionImage.PaymentType.FINAXAR.name && lstCompany.settings!!.payments?.paymentSources?.get(
                                                        i
                                                    )?.status.equals("ACTIVE", ignoreCase = true)
                                                ) {
                                                    lytFinaxarPay.setVisibility(View.VISIBLE)
                                                    if (mOrder.amount?.total?.amount!! <= lstCompany.settings!!.payments?.paymentSources?.get(
                                                            i
                                                        )?.availableLimit?.amount!!
                                                    ) {
                                                        lytFinaxarPay.setAlpha(1f)
                                                        lytFinaxarPay.setClickable(true)
                                                        txtFinaxarTagValue.setVisibility(View.GONE)
                                                        txtFinaxarTagValue.setText(
                                                            lstCompany.settings!!.payments?.paymentSources?.get(
                                                                i
                                                            )?.availableLimit?.displayValue + " "
                                                        )
                                                        //                                                txtFinaxarpayTag.setText(getResources().getString(R.string.txt_available_to_use));
                                                    } else {
                                                        txtFinaxarTagValue.setVisibility(View.GONE)
                                                        txtFinaxarpayTag.setText(
                                                            getResources().getString(
                                                                R.string.txt_insufficient_balance
                                                            )
                                                        )
                                                        lytFinaxarPay.setAlpha(0.5f)
                                                        lytFinaxarPay.setClickable(false)
                                                    }
                                                }
                                                if (lstCompany.settings!!.payments?.paymentSources?.get(
                                                        i
                                                    )?.paymentType == UploadTransactionImage.PaymentType.GRAB_FINANCE.name && lstCompany.settings!!.payments?.paymentSources?.get(
                                                        i
                                                    )?.status.equals("ACTIVE", ignoreCase = true)
                                                ) {
                                                    lytGrabFinance.setVisibility(View.VISIBLE)
                                                    if (mOrder.amount?.total?.amount!! <= lstCompany.settings!!.payments?.paymentSources?.get(
                                                            i
                                                        )?.availableLimit?.amount!!
                                                    ) {
                                                        lytGrabFinance.setAlpha(1f)
                                                        lytGrabFinance.setClickable(true)
                                                        txtGrabFinanceTagValue.setVisibility(View.VISIBLE)
                                                        txtGrabFinanceTagValue.setText(
                                                            lstCompany.settings!!.payments?.paymentSources?.get(
                                                                i
                                                            )?.availableLimit?.displayValue + " "
                                                        )
                                                        txtGrabFinanceTag.setText(
                                                            getResources().getString(
                                                                R.string.txt_available_to_use
                                                            )
                                                        )
                                                    } else {
                                                        txtGrabFinanceTagValue.setVisibility(View.GONE)
                                                        txtGrabFinanceTag.setText(
                                                            getResources().getString(
                                                                R.string.txt_insufficient_balance
                                                            )
                                                        )
                                                        lytGrabFinance.setAlpha(0.5f)
                                                        lytGrabFinance.setClickable(false)
                                                    }
                                                }
                                                if (lstCompany.settings!!.payments?.paymentSources?.get(
                                                        i
                                                    )?.paymentType == UploadTransactionImage.PaymentType.FUNDING_SOCIETIES.name && lstCompany.settings!!.payments?.paymentSources?.get(
                                                        i
                                                    )?.status.equals("ACTIVE", ignoreCase = true)
                                                ) {
                                                    lytFundingSocieties.setVisibility(View.VISIBLE)
                                                    if (mOrder.amount?.total?.amount!! <= lstCompany.settings!!.payments?.paymentSources?.get(
                                                            i
                                                        )?.availableLimit?.amount!!
                                                    ) {
                                                        lytFundingSocieties.setAlpha(1f)
                                                        lytFundingSocieties.setClickable(true)
                                                        txtFundingSocietiesTagValue.setVisibility(
                                                            View.GONE
                                                        )
                                                        txtFundingSocietiesTag.setVisibility(View.GONE)
                                                        txtFundingSocietiesTagValue.setText(
                                                            lstCompany.settings!!.payments?.paymentSources?.get(
                                                                i
                                                            )?.availableLimit?.displayValue + " "
                                                        )
                                                        txtFundingSocietiesTag.setText(
                                                            getResources().getString(
                                                                R.string.txt_available_to_use
                                                            )
                                                        )
                                                    } else {
                                                        txtFundingSocietiesTagValue.setVisibility(
                                                            View.GONE
                                                        )
                                                        txtFundingSocietiesTag.setVisibility(View.VISIBLE)
                                                        txtFundingSocietiesTag.setText(
                                                            getResources().getString(
                                                                R.string.txt_insufficient_balance
                                                            )
                                                        )
                                                        lytFundingSocieties.setAlpha(0.5f)
                                                        lytFundingSocieties.setClickable(false)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onErrorResponse(error: VolleyError?) {}
                            })
                    }
                }

                override fun onError(error: VolleyErrorHelper?) {}
            })
    }

    private fun createFinaxarAlert(text: String) {
        val builder = AlertDialog.Builder(this@OrderPaymentListActivity)
        val dialog: AlertDialog
        createPlaceOrderDialogBtn(builder, text)
        createCancelDialogBtn(builder)
        dialog = builder.create()
        dialog.setTitle("Use $text for this payment?")
        dialog.setOnCancelListener(object : DialogInterface.OnCancelListener {
            override fun onCancel(dialog: DialogInterface) {}
        })
        dialog.show()
    }

    private fun createPlaceOrderDialogBtn(builder: AlertDialog.Builder, text: String) {
        val createOrderYesButton: String
        createOrderYesButton = "Yes, continue"
        builder.setPositiveButton(createOrderYesButton, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                //only create request if order has some products
                payFinaxar(text)
            }
        })
    }

    private fun createCancelDialogBtn(builder: AlertDialog.Builder) {
        builder.setNegativeButton(
            getString(R.string.dialog_cancel_button_text),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.cancel()
                }
            })
    }

    private fun payFinaxar(Text: String) {
        threeDotLoader.setVisibility(View.VISIBLE)
        val uploadTransactionImage = UploadTransactionImage()
        uploadTransactionImage.orderId = mOrder.orderId
        uploadTransactionImage.paymentType = Text
        val jsonRequest: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(uploadTransactionImage)
        OrderManagement.uploadTransactionImageToOrder(
            this@OrderPaymentListActivity,
            jsonRequest,
            object : OrderManagement.uploadTransactionrResponseListener {
                override fun onSuccessResponse(status: String?) {
                    threeDotLoader.setVisibility(View.GONE)
                    if (!StringHelper.isStringNullOrEmpty(status)) {
                        if (Text.equals(
                                UploadTransactionImage.PaymentType.GRAB_FINANCE.name,
                                ignoreCase = true
                            )
                        ) {
                            val dialogSuccess: AlertDialog = DialogHelper.alertDialogPaymentSuccess(
                                this@OrderPaymentListActivity, getString(
                                    R.string.txt_payment_success
                                )
                            )
                            Handler().postDelayed({
                                dialogSuccess.dismiss()
                                val intent = Intent(
                                    this@OrderPaymentListActivity,
                                    OrderDetailsActivity::class.java
                                )
                                intent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder.orderId)
                                intent.putExtra(
                                    ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                    mOrder.outlet?.outletId
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM,
                                    ZeemartAppConstants.CALLED_FROM_ORDER_PAYMENT_CONFIRMATION
                                )
                                startActivity(intent)
                                finish()
                            }, 2000)
                        } else {
                            val dialogSuccess: AlertDialog =
                                DialogHelper.alertPaymentDialogSmallSuccess(
                                    this@OrderPaymentListActivity, getString(
                                        R.string.txt_thanks
                                    ), getString(R.string.txt_get_back)
                                )
                            dialogSuccess.show()
                            Handler().postDelayed({
                                dialogSuccess.dismiss()
                                val intent = Intent(
                                    this@OrderPaymentListActivity,
                                    OrderDetailsActivity::class.java
                                )
                                intent.putExtra(ZeemartAppConstants.ORDER_ID, mOrder.orderId)
                                intent.putExtra(
                                    ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                    mOrder.outlet?.outletId
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM,
                                    ZeemartAppConstants.CALLED_FROM_ORDER_PAYMENT_CONFIRMATION
                                )
                                startActivity(intent)
                                finish()
                            }, 2000)
                        }
                    } else {
                        val dialogFailure: AlertDialog
                        dialogFailure = if (Text.equals(
                                UploadTransactionImage.PaymentType.GRAB_FINANCE.name,
                                ignoreCase = true
                            )
                        ) {
                            DialogHelper.alertDialogPaymentFailure(
                                this@OrderPaymentListActivity,
                                getString(R.string.txt_payment_failed),
                                getString(
                                    R.string.txt_please_try_again
                                )
                            )
                        } else {
                            DialogHelper.alertPaymentDialogSmallFailure(
                                this@OrderPaymentListActivity,
                                getString(R.string.txt_upload_failed),
                                getString(
                                    R.string.txt_please_try_again
                                )
                            )
                        }
                        dialogFailure.show()
                        Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoader.setVisibility(View.GONE)
                    var failureMessage: String =
                        getResources().getString(R.string.txt_upload_failed)
                    if (Text.equals(
                            UploadTransactionImage.PaymentType.GRAB_FINANCE.name,
                            ignoreCase = true
                        )
                    ) {
                        failureMessage = getResources().getString(R.string.txt_payment_failed)
                    }
                    val dialogFailure: AlertDialog = DialogHelper.alertPaymentDialogSmallFailure(
                        this@OrderPaymentListActivity, failureMessage, getString(
                            R.string.txt_please_try_again
                        )
                    )
                    dialogFailure.show()
                    Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                }
            })
    }
}