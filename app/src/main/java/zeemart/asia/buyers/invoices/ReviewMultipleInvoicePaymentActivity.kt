package zeemart.asia.buyers.invoices

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.ReviewPaymentInvoiceListAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.paymentimport.PayInvoiceResponse
import zeemart.asia.buyers.models.paymentimport.PaymentCardDetails
import zeemart.asia.buyers.models.paymentimport.PaymentStatus
import zeemart.asia.buyers.models.paymentimportimport.PayInvoiceRequest
import zeemart.asia.buyers.network.InvoiceHelper
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.PaymentApi
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by RajPrudhviMarella on 7/2/2020.
 */
class ReviewMultipleInvoicePaymentActivity : AppCompatActivity() {
    private var txtTotalTag: TextView? = null
    private var txtTotalValue: TextView? = null
    private var txtOutletName: TextView? = null
    private var txtSupplierName: TextView? = null
    private var txtDateHeader: TextView? = null
    private var txtSelectAll: TextView? = null
    private var btnPayInvoices: Button? = null
    private var btnImageCross: ImageView? = null
    private var threeDotLoader: CustomLoadingViewWhite? = null
    private var lstSupplier: Supplier? = null
    private var invoicesList: MutableList<Invoice>? = null
    private var startDate = ""
    private var endDate = ""
    private var lstInvoices: RecyclerView? = null
    private var cardReferenceNo: String? = null
    private var eCreditLst: ECreditBySupplier? = null
    private var selectedInvoices: List<Invoice> = ArrayList<Invoice>()
    private var totalPayableAmount: PriceDetails? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_multiple_invoice_payment)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_LIST)) {
                val supplierJson: String? = bundle.getString(ZeemartAppConstants.SUPPLIER_LIST)
                lstSupplier = ZeemartBuyerApp.gsonExposeExclusive.fromJson<Supplier>(
                    supplierJson,
                    Supplier::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.INVOICES_LIST)) {
                val invoicesJson: String? = bundle.getString(ZeemartAppConstants.INVOICES_LIST)
                invoicesList = ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<Invoice>>(
                    invoicesJson,
                    object : TypeToken<List<Invoice?>?>() {}.type
                ) as MutableList<Invoice>
                Invoice.sortByDueDate(invoicesList)
            }
            if (bundle.containsKey(ZeemartAppConstants.DUE_START_DATE)) {
                startDate = bundle.getString(ZeemartAppConstants.DUE_START_DATE).toString()
            }
            if (bundle.containsKey(ZeemartAppConstants.DUE_END_DATE)) {
                endDate = bundle.getString(ZeemartAppConstants.DUE_END_DATE).toString()
            }
        }
        txtDateHeader = findViewById<TextView>(R.id.txt_date_header_review_payment)
        txtSelectAll = findViewById<TextView>(R.id.txt_select_all_review_payment)
        txtTotalTag = findViewById<TextView>(R.id.txt_total_tag_review_payment)
        txtTotalValue = findViewById<TextView>(R.id.txt_total_value_review_payment)
        txtOutletName = findViewById<TextView>(R.id.txt_outlet_name_review_payment)
        txtSupplierName = findViewById<TextView>(R.id.txt_supplier_name_review_payment)
        btnPayInvoices = findViewById<Button>(R.id.btn_pay_invoice)
        btnImageCross = findViewById<ImageView>(R.id.btn_close_review_payment)
        threeDotLoader = findViewById<CustomLoadingViewWhite>(R.id.lyt_loading_overlay)
        threeDotLoader!!.setVisibility(View.VISIBLE)
        btnImageCross = findViewById<ImageView>(R.id.btn_close_review_payment)
        btnImageCross!!.setOnClickListener {
            val newIntent = Intent()
            newIntent.putExtra(ZeemartAppConstants.DUE_START_DATE, startDate)
            newIntent.putExtra(ZeemartAppConstants.DUE_END_DATE, endDate)
            setResult(ZeemartAppConstants.ResultCode.RESULT_OK, newIntent)
            finish()
        }
        lstInvoices = findViewById<RecyclerView>(R.id.lst_payment_invoices)
        lstInvoices!!.setLayoutManager(LinearLayoutManager(this))
        if (lstSupplier != null) {
            txtSupplierName!!.setText(lstSupplier!!.supplierName)
        }
        txtOutletName!!.setText(SharedPref.defaultOutlet?.outletName)
        setDueDate()
        setInvoicesAdapter()
        setFont()
    }

    private fun setInvoicesAdapter() {
        if (invoicesList != null && invoicesList!!.size > 0) {
            lstInvoices?.setAdapter(
                ReviewPaymentInvoiceListAdapter(
                    this,
                    invoicesList!!,
                    object : ReviewPaymentInvoiceListAdapter.ReviewPaymentInvoiceSelectedListener {
                       override fun onInvoiceItemSelected(invoices: List<Invoice>?) {
                            setPlaceOrderButtonUI()
                        }
                    })
            )
            setPlaceOrderButtonUI()
        }
        threeDotLoader?.setVisibility(View.GONE)
    }

    private fun setPlaceOrderButtonUI() {
        var noOfOrderSelected = 0
        val totalAmount: Double
        val priceDetails = PriceDetails()
        val selectedInvoiceList: MutableList<Invoice> = ArrayList<Invoice>()
        for (i in invoicesList!!.indices) {
            if (invoicesList!![i].isInvoiceSelected) {
                noOfOrderSelected = noOfOrderSelected + 1
                priceDetails.addAmount(invoicesList!![i].totalCharge?.amount!!)
                selectedInvoiceList.add(invoicesList!![i])
            }
        }
        /* total text,  pay button functionality*/txtTotalValue?.setText(priceDetails.displayValue)
        totalAmount = priceDetails.amount!!
        if (noOfOrderSelected > 0) {
            btnPayInvoices!!.background = getResources().getDrawable(R.drawable.blue_rounded_corner)
            btnPayInvoices!!.isClickable = true
            if (noOfOrderSelected == 1) {
                btnPayInvoices!!.text = String.format(
                    getResources().getString(R.string.txt_pay_number_invoice),
                    noOfOrderSelected
                )
            } else {
                btnPayInvoices!!.text = String.format(
                    getResources().getString(R.string.txt_pay_number_invoices),
                    noOfOrderSelected
                )
            }
            btnPayInvoices!!.setOnClickListener {
                CommonMethods.avoidMultipleClicks(btnPayInvoices)
                val selectedInvoiceListJson: String =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedInvoiceList)
                createPayConfirmationDialog(selectedInvoiceListJson, totalAmount)
            }
        } else {
            btnPayInvoices!!.background =
                getResources().getDrawable(R.drawable.dark_grey_rounded_corner)
            btnPayInvoices!!.isClickable = false
            btnPayInvoices!!.setText(getResources().getString(R.string.txt_no_invoice_selected))
        }
        /* select , deselect all invoices functionality*/if (noOfOrderSelected == invoicesList!!.size) {
            txtSelectAll?.setText(getResources().getString(R.string.txt_deselect_all))
            txtSelectAll?.setOnClickListener(View.OnClickListener {
                for (i in invoicesList!!.indices) {
                    if (invoicesList!![i].isInvoiceSelected) {
                        invoicesList!![i].isInvoiceSelected = false
                    }
                }
                if (lstInvoices?.getAdapter() != null) {
                    (lstInvoices?.getAdapter() as ReviewPaymentInvoiceListAdapter).updateDataList(
                        invoicesList!!
                    )
                }
                setPlaceOrderButtonUI()
            })
        } else {
            txtSelectAll?.setText(getResources().getString(R.string.txt_select_all))
            txtSelectAll?.setOnClickListener(View.OnClickListener {
                for (i in invoicesList!!.indices) {
                    if (!invoicesList!![i].isInvoiceSelected) {
                        invoicesList!![i].isInvoiceSelected = true
                    }
                }
                if (lstInvoices?.getAdapter() != null) {
                    (lstInvoices?.getAdapter() as ReviewPaymentInvoiceListAdapter).updateDataList(
                        invoicesList!!
                    )
                }
                setPlaceOrderButtonUI()
            })
        }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtDateHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectAll,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOutletName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnPayInvoices,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun setDueDate() {
        if (!StringHelper.isStringNullOrEmpty(startDate) && !StringHelper.isStringNullOrEmpty(
                endDate
            )
        ) {
            val startDateLong: Long = DateHelper.returnEpochTimeSOD(startDate)
            val endDateLong: Long = DateHelper.returnEpochTimeEOD(endDate)
            val startEndDateRange = DateHelper.StartDateEndDate(startDateLong, endDateLong)
            val startEndDate: String = startEndDateRange.startEndRangeDateMonthYearFormat
            txtDateHeader?.setText(
                kotlin.String.format(
                    getResources().getString(R.string.txt_due_date),
                    startEndDate
                )
            )
        }
    }

    private fun createPayConfirmationDialog(selectedInvoiceJson: String, totalAmount: Double) {
        if (!StringHelper.isStringNullOrEmpty(selectedInvoiceJson)) {
            selectedInvoices = ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<Invoice>>(
                selectedInvoiceJson,
                object : TypeToken<List<Invoice?>?>() {}.type
            )
        }
        val d = Dialog(this@ReviewMultipleInvoicePaymentActivity, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_payment_confirmation)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtInvoiceTotalTag: TextView = d.findViewById<TextView>(R.id.txt_invoice_total_tag)
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
        val txtCredits: TextView = d.findViewById<TextView>(R.id.txt_credits)
        val btnRemoveCredits: TextView = d.findViewById<TextView>(R.id.txt_remove_credit)
        val txtCreditFeeValue: TextView = d.findViewById<TextView>(R.id.txt_credit_fee_value)
        val lytCardDetails: LinearLayout = d.findViewById<LinearLayout>(R.id.lyt_card_detail)
        val txtTotalTag: TextView = d.findViewById<TextView>(R.id.txt_total)
        txtTotalTag.setVisibility(View.GONE)
        val lytRemoveCredits: RelativeLayout =
            d.findViewById<RelativeLayout>(R.id.lyt_remove_credit)
        lytRemoveCredits.setVisibility(View.GONE)
        val lytAddCard: LinearLayout = d.findViewById<LinearLayout>(R.id.lyt_add_new_card)
        val txtAddCard: TextView = d.findViewById<TextView>(R.id.txt_add_new_Card)
        lytAddCard.setVisibility(View.GONE)
        val txtCardNumber: TextView = d.findViewById<TextView>(R.id.txt_card_number)
        val txtTotal: TextView = d.findViewById<TextView>(R.id.txt_total_value)
        val btnPay = d.findViewById<Button>(R.id.btn_pay)
        val txtPaymentTerms: TextView = d.findViewById<TextView>(R.id.txt_payment_terms)
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
        apiParamsHelper.setSupplierId(lstSupplier!!.supplierId!!)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        InvoiceHelper.retrieveECreditBySupplier(
            this,
            apiParamsHelper,
            outlets,
            object : InvoiceHelper.GetECreditBySupplierListener {
                override fun result(eCredit: ECreditBySupplier?) {
                    val priceDetails = PriceDetails()
                    if (eCredit != null) {
                        eCreditLst = ECreditBySupplier()
                        eCreditLst = eCredit
                        if (eCredit.data?.totalCredits?.displayValue == PriceDetails().displayValue) {
                            lytUseCredits.setVisibility(View.GONE)
                        } else {
                            lytUseCredits.setVisibility(View.VISIBLE)
                        }
                        txtCreditAvailable.setText(
                            kotlin.String.format(
                                getResources().getString(R.string.txt_available_credit),
                                eCredit.data?.totalCredits?.displayValue
                            )
                        )
                        if (eCredit.data?.totalCredits?.amount!! < totalAmount) {
                            val amount = "-" + eCredit.data?.totalCredits?.displayValue
                            txtCreditFeeValue.setText(amount)
                        } else {
                            priceDetails.amount = totalAmount
                            val amount = "-" + priceDetails.displayValue
                            txtCreditFeeValue.setText(amount)
                        }
                    }
                    btnUseCredits.setOnClickListener(View.OnClickListener {
                        lytUseCredits.setVisibility(View.GONE)
                        lytRemoveCredits.setVisibility(View.VISIBLE)
                        if (totalAmount > eCredit?.data?.totalCredits?.amount!!) {
                            priceDetails.amount = totalAmount - eCredit.data!!.totalCredits!!.amount!!
                            txtTotalTag.setVisibility(View.GONE)
                            lytCardDetails.setVisibility(View.VISIBLE)
                            btnPay.setText(getString(R.string.txt_pay, priceDetails.displayValue))
                        } else {
                            priceDetails.amount = null
                            txtTotalTag.setVisibility(View.VISIBLE)
                            lytCardDetails.setVisibility(View.GONE)
                            lytAddCard.setVisibility(View.GONE)
                            btnPay.setText(getString(R.string.txt_submit))
                        }
                        txtTotal.setText(priceDetails.displayValue)
                        totalPayableAmount = priceDetails
                    })
                }
            })
        OutletsApi.getSpecificOutlet(this, object : OutletsApi.GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                val apiParamsHelper = ApiParamsHelper()
                if (specificOutlet != null && specificOutlet.data != null && specificOutlet.data!!.company != null
                    && specificOutlet.data!!.company?.companyId != null) apiParamsHelper.setCompanyId(
                    specificOutlet.data!!.company?.companyId!!
                )
                PaymentApi.retrieveCompanyCardPaymentData(
                    this@ReviewMultipleInvoicePaymentActivity,
                    apiParamsHelper,
                    SharedPref.defaultOutlet!!,
                    object : PaymentApi.CompanyCardDetailsListener {
                        override fun onSuccessResponse(paymentCardDetails: PaymentCardDetails?) {
                            val cardList: MutableList<PaymentCardDetails.PaymentResponse> =
                                ArrayList<PaymentCardDetails.PaymentResponse>()
                            if (paymentCardDetails != null && paymentCardDetails.data != null && paymentCardDetails.data!!
                                    .size!! > 0
                            ) {
                                lytAddCard.setVisibility(View.GONE)
                                lytCardDetails.setVisibility(View.VISIBLE)
                                for (i in 0 until paymentCardDetails.data!!.size) {
                                    if (paymentCardDetails.data!!.get(i)
                                            .status != null && paymentCardDetails.data!!
                                            .get(i).status
                                            .equals(PaymentStatus.ACTIVE.getmStatusName())
                                    ) {
                                        cardList.add(paymentCardDetails.data!!.get(i))
                                    }
                                }
                                for (i in 0 until paymentCardDetails.data!!.size) {
                                    if (paymentCardDetails.data!!.get(i).isDefaultCard) {
                                        txtCardNumber.setText(
                                            getResources().getString(R.string.txt_card_bullet) + " " + paymentCardDetails.data!!
                                                .get(i).customerCardData?.cardLast4Digits
                                        )
                                        if (paymentCardDetails.data!!.get(i)
                                                .customerCardData!!.isCardType(PaymentCardDetails.CardType.VISA)
                                        ) {
                                            imgCard.setImageResource(R.drawable.visa)
                                        } else if (paymentCardDetails.data!!.get(i)
                                                .customerCardData
                                                !!.isCardType(PaymentCardDetails.CardType.MASTERCARD)
                                        ) {
                                            imgCard.setImageResource(R.drawable.master_card)
                                        } else {
                                            imgCard.setImageResource(R.drawable.amex)
                                        }
                                        imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                        cardReferenceNo =
                                            paymentCardDetails.data!!.get(i).referenceNo
                                    }
                                }
                            } else {
                                lytAddCard.setVisibility(View.VISIBLE)
                                lytCardDetails.setVisibility(View.GONE)
                            }
                            lytAddCard.setOnClickListener(View.OnClickListener {
                                DialogHelper.ShowAddNewCardDialog(
                                    this@ReviewMultipleInvoicePaymentActivity,
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
                                            if (selectedCardDetails.customerCardData!!.isCardType(PaymentCardDetails.CardType.VISA)
                                            ) {
                                                imgCard.setImageResource(R.drawable.visa)
                                            } else if (selectedCardDetails.customerCardData
                                                    !!.isCardType(PaymentCardDetails.CardType.MASTERCARD)
                                            ) {
                                                imgCard.setImageResource(R.drawable.master_card)
                                            } else {
                                                imgCard.setImageResource(R.drawable.amex)
                                            }
                                            imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                            cardReferenceNo = selectedCardDetails.referenceNo
                                        }
                                    })
                            })
                            lytCardDetails.setOnClickListener(View.OnClickListener {
                                CommonMethods.avoidMultipleClicks(lytCardDetails)
                                if (cardList != null && cardList.size > 0) {
                                    val cardListJson: String =
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(cardList)
                                    DialogHelper.ShowCardListDialog(
                                        this@ReviewMultipleInvoicePaymentActivity,
                                        cardListJson,
                                        specificOutlet!!.data?.company?.companyId,
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
                                                if (specificOutlet != null && specificOutlet.data != null
                                                    && specificOutlet.data!!.company != null && specificOutlet.data!!.company?.companyId != null) paymentCardDefault.companyId =
                                                    specificOutlet.data!!.company?.companyId
                                                paymentCardDefault.referenceNo =
                                                    selectedCardDetails?.referenceNo
                                                paymentCardDefault.setAsDefaultBy =
                                                    SharedPref.currentUserDetail
                                                PaymentApi.setPaymentCardDefault(
                                                    this@ReviewMultipleInvoicePaymentActivity,
                                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                        paymentCardDefault
                                                    ),
                                                    object : GetRequestStatusResponseListener {
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
                                                        !!.isCardType(PaymentCardDetails.CardType.VISA)
                                                ) {
                                                    imgCard.setImageResource(R.drawable.visa)
                                                } else if (selectedCardDetails.customerCardData!!
                                                        .isCardType(PaymentCardDetails.CardType.MASTERCARD)
                                                ) {
                                                    imgCard.setImageResource(R.drawable.master_card)
                                                } else {
                                                    imgCard.setImageResource(R.drawable.amex)
                                                }
                                                imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                                cardReferenceNo =
                                                    selectedCardDetails.referenceNo
                                            }
                                        })
                                }
                            })
                        }

                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                            val errorMessage: String = error!!.errorMessage
                            val errorType: String = error.errorType
                            if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                ZeemartBuyerApp.getToastRed(errorMessage)
                            }
                        }
                    })
            }

            override fun onError(error: VolleyErrorHelper?) {
                val errorMessage: String = error!!.errorMessage
                val errorType: String = error.errorType
                if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                    ZeemartBuyerApp.getToastRed(errorMessage)
                }
            }
        })
        val priceDetails = PriceDetails()
        priceDetails.amount = totalAmount
        txtInvoiceTotal.setText(priceDetails.displayValue)
        txtTotal.setText(priceDetails.displayValue)
        totalPayableAmount = priceDetails
        btnPay.setText(getString(R.string.txt_pay, priceDetails.displayValue))
        btnRemoveCredits.setOnClickListener(View.OnClickListener {
            lytUseCredits.setVisibility(View.VISIBLE)
            lytRemoveCredits.setVisibility(View.GONE)
            txtInvoiceTotal.setText(priceDetails.displayValue)
            txtTotal.setText(priceDetails.displayValue)
            totalPayableAmount = priceDetails
            txtTotalTag.setVisibility(View.GONE)
            lytCardDetails.setVisibility(View.VISIBLE)
            btnPay.setText(getString(R.string.txt_pay, priceDetails.displayValue))
        })
        btnPay.setOnClickListener {
            CommonMethods.avoidMultipleClicks(btnPay)
            threeDotLoader!!.setVisibility(View.VISIBLE)
            val usedCredit = PayInvoiceRequest.UsedCredit()
            if (lytUseCredits.getVisibility() == View.GONE && eCreditLst != null) {
                if (eCreditLst!!.data?.totalCredits?.amount!! < totalAmount) {
                    usedCredit.amount = (eCreditLst!!.data?.totalCredits?.amount)
                } else {
                    usedCredit.amount = (totalAmount)
                }
                usedCredit.currencyCode = (eCreditLst!!.data?.totalCredits?.currencyCode)
            }
            val invoiceIdsList: MutableList<String> = ArrayList()
            for (i in selectedInvoices.indices) {
                invoiceIdsList.add(selectedInvoices!![i].invoiceId!!)
            }
            val payInvoiceRequest = PayInvoiceRequest()
            payInvoiceRequest.supplierId = (lstSupplier!!.supplierId)
            payInvoiceRequest.invoiceIds = (invoiceIdsList)
            payInvoiceRequest.usedCredit = (usedCredit)
            payInvoiceRequest.cardReferenceNo = (cardReferenceNo)
            payInvoiceRequest.paidBy = (SharedPref.currentUserDetail)
            payInvoiceRequest.status = (Invoice.PaymentStatus.PAID)
            PaymentApi.payInvoice(
                this@ReviewMultipleInvoicePaymentActivity,
                payInvoiceRequest,
                SharedPref.defaultOutlet!!,
                object : PaymentApi.PayInvoiceListener {
                    override fun onSuccessResponse(payInvoice: PayInvoiceResponse?) {
                        if (payInvoice != null) {
                            if (payInvoice.data
                                    !!.isTransactionStatus(PayInvoiceResponse.TransactionStatus.PAID)
                            ) {
                                threeDotLoader!!.setVisibility(View.GONE)
                                //send data to clevertap
                                AnalyticsHelper.logAction(
                                    this@ReviewMultipleInvoicePaymentActivity,
                                    AnalyticsHelper.TAP_INVOICE_PAY,
                                    SharedPref.defaultOutlet!!,
                                    lstSupplier!!,
                                    totalPayableAmount!!,
                                    invoiceIdsList.size
                                )
                                val dialogSuccess: AlertDialog =
                                    DialogHelper.alertDialogPaymentSuccess(
                                        this@ReviewMultipleInvoicePaymentActivity, getString(
                                            R.string.txt_payment_success
                                        )
                                    )
                                Handler().postDelayed({
                                    if (payInvoice.data != null && payInvoice.data!!
                                            .invoiceCreditData?.invoices != null
                                    ) {
                                        for (i in invoicesList!!.indices) {
                                            for (j in 0 until payInvoice.data!!
                                                .invoiceCreditData?.invoices!!.size) {
                                                if (invoicesList!![i].invoiceId == payInvoice.data!!
                                                        .invoiceCreditData?.invoices?.get(j)
                                                        ?.invoiceId
                                                ) {
                                                    invoicesList!!.remove(invoicesList!![i])
                                                }
                                            }
                                        }
                                        if (invoicesList != null && invoicesList!!.size > 0) {
                                            if (lstInvoices?.getAdapter() != null) {
                                                (lstInvoices?.getAdapter() as ReviewPaymentInvoiceListAdapter).updateDataList(
                                                    invoicesList!!
                                                )
                                            }
                                        } else {
                                            val newIntent = Intent()
                                            newIntent.putExtra(
                                                ZeemartAppConstants.DUE_START_DATE,
                                                startDate
                                            )
                                            newIntent.putExtra(
                                                ZeemartAppConstants.DUE_END_DATE,
                                                endDate
                                            )
                                            setResult(
                                                ZeemartAppConstants.ResultCode.RESULT_OK,
                                                newIntent
                                            )
                                            finish()
                                        }
                                    }
                                    dialogSuccess.dismiss()
                                }, 2000)
                            } else {
                                threeDotLoader!!.setVisibility(View.GONE)
                                val dialogFailure: AlertDialog =
                                    DialogHelper.alertDialogPaymentFailure(
                                        this@ReviewMultipleInvoicePaymentActivity, getString(
                                            R.string.txt_payment_failed
                                        ), payInvoice.data!!.failureReason
                                    )
                                dialogFailure.show()
                                Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                            }
                        } else {
                            threeDotLoader!!.setVisibility(View.GONE)
                            val dialogFailure: AlertDialog = DialogHelper.alertDialogPaymentFailure(
                                this@ReviewMultipleInvoicePaymentActivity, getString(
                                    R.string.txt_payment_failed
                                ), getString(R.string.txt_please_try_again)
                            )
                            dialogFailure.show()
                            Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                        }
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        val errorMessage: String = error!!.errorMessage
                        val errorType: String = error.errorType
                        if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                            ZeemartBuyerApp.getToastRed(errorMessage)
                        } else {
                            ZeemartBuyerApp.getToastRed(errorMessage)
                        }
                    }
                })
            d.dismiss()
        }
        d.show()
    }
}