package zeemart.asia.buyers.invoices

import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter.InvoiceImageListener
import zeemart.asia.buyers.adapter.InvoiceProductsListAdapter
import zeemart.asia.buyers.adapter.LinkedCreditNotesListAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DialogHelper.ShowAddNewCardDialog
import zeemart.asia.buyers.helper.DialogHelper.ShowCardListDialog
import zeemart.asia.buyers.helper.DialogHelper.alertDialogPaymentFailure
import zeemart.asia.buyers.helper.DialogHelper.alertDialogPaymentSuccess
import zeemart.asia.buyers.helper.DialogHelper.onCardAddedListener
import zeemart.asia.buyers.helper.DialogHelper.onCardSelectedListener
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.interfaces.PermissionCallback
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.Invoice.InvoiceProduct
import zeemart.asia.buyers.models.invoice.Invoice.LinkedCreditNotes
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.paymentimport.LinkedSupplierPreference
import zeemart.asia.buyers.models.paymentimport.PayInvoiceResponse
import zeemart.asia.buyers.models.paymentimport.PaymentCardDetails
import zeemart.asia.buyers.models.paymentimport.PaymentStatus
import zeemart.asia.buyers.models.paymentimportimport.PayInvoiceRequest
import zeemart.asia.buyers.network.InvoiceHelper.GetECreditBySupplierListener
import zeemart.asia.buyers.network.InvoiceHelper.GetInvoice
import zeemart.asia.buyers.network.InvoiceHelper.deleteProcessedInvoice
import zeemart.asia.buyers.network.InvoiceHelper.retrieveECreditBySupplier
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.OutletsApi.GetSpecificOutletResponseListener
import zeemart.asia.buyers.network.PaymentApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.util.*

class InvoiceDetailActivity : AppCompatActivity() {
    private var invoiceId: String? = null
    private var invoice: Invoice? = null
    private var txtInvoiceDetailHeading: TextView? = null
    private lateinit var recyclerView: RecyclerView
    private var txtSupplierName: TextView? = null
    private var imgSupplierImage // image
            : ImageView? = null
    private var lytSupplierThumbNail: RelativeLayout? = null
    private var txtSupplierThumbNail: TextView? = null
    private var txtInvoiceDateValue // invoice date
            : TextView? = null
    private var txtInvoiceDateTag: TextView? = null
    private var lytPaymentDueDate: LinearLayout? = null
    private var txtPaymentDueDateValue: TextView? = null
    private var txtPaymentDueDateTag: TextView? = null
    private var txtDividerPaymentDueDate: TextView? = null
    private var txtInvoiceAmountValue // invoice amount
            : TextView? = null
    private var txtInvoiceAmountTag: TextView? = null
    private lateinit var txtPaymentMethod // payment method
            : TextView
    private var txtLocationValue // deliver to
            : TextView? = null
    private var txtLocationTag: TextView? = null
    private var txtLinkedToOrderValue // link to setonclick
            : TextView? = null
    private var txtLinkedToTag: TextView? = null
    private var txtItemCount: TextView? = null
    private var txtEstimatedSubtotalValue: TextView? = null
    private var txtEstimatedSubtotalTag: TextView? = null
    private var txtDiscountValue: TextView? = null
    private var txtDiscountTag: TextView? = null
    private var txtOthersValue: TextView? = null
    private var txtOthersTag: TextView? = null
    private var txtGstValue: TextView? = null
    private lateinit var txtGstTag: TextView
    private var txtDeliveryFeeValue: TextView? = null
    private var txtDeliveryFeeTag: TextView? = null
    private var txtEstimatedTotalValue: TextView? = null
    private var txtEstimatedTotalTag: TextView? = null
    private var txtDividerDeliverToLinkedTo: TextView? = null
    private var txtDividerStatusInvoiceDate: TextView? = null
    private var imgLinkedToIcon: ImageView? = null
    private var viewOriginalInvoice // link to image
            : TextView? = null
    private var btnMoreOptions: ImageButton? = null
    private lateinit var btnBack: ImageButton
    private var txtInvoiceNumber: TextView? = null
    private lateinit var txtDotSeperator: TextView
    private lateinit var txtInvoiceType: TextView
    private lateinit var imgInvoiceType: ImageView
    private var lytOrderTotal: RelativeLayout? = null
    private var lytLinkedTo: LinearLayout? = null
    private var lytStatus: LinearLayout? = null
    private var txtStatusTag: TextView? = null
    private lateinit var txtStatusValue: TextView
    private lateinit var lytPay: RelativeLayout
    private lateinit var txtPay: TextView
    private var permissionCallback: PermissionCallback? = null
    private var cardReferenceNo: String? = null
    private var lytSpace: LinearLayout? = null
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var lstLinkedCreditNotes: RecyclerView
    private var eCreditLst: ECreditBySupplier? = null
    private var count = 0

    //to be sent to clevertap
    private var totalPayableAmount: PriceDetails? = null

    //broadcast receiver to listen to download complete action
    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            getToastRed(getString(R.string.txt_pdf_save_to_downloads))
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(onComplete)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_detail)
        txtInvoiceDetailHeading = findViewById(R.id.txt_invoice_details_heading)
        txtInvoiceNumber = findViewById(R.id.txt_invoice_number_invoice_details)
        txtInvoiceType = findViewById(R.id.txt_invoice_type)
        txtInvoiceType.setVisibility(View.GONE)
        imgInvoiceType = findViewById(R.id.img_e_invoice)
        imgInvoiceType.setVisibility(View.GONE)
        txtDotSeperator = findViewById(R.id.txt_dot_seperator)
        txtDotSeperator.setVisibility(View.GONE)
        txtSupplierName = findViewById(R.id.txt_supplier_name)
        imgSupplierImage = findViewById(R.id.img_supplier_image)
        lytSupplierThumbNail = findViewById(R.id.lyt_supplier_thumbnail)
        txtSupplierThumbNail = findViewById(R.id.txt_supplier_thumbnail)
        setTypefaceView(
            txtSupplierThumbNail,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtInvoiceDateValue = findViewById(R.id.txt_invoice_date_value) // invoice date
        txtInvoiceDateTag = findViewById(R.id.txt_invoice_date_tag)
        lytPaymentDueDate = findViewById(R.id.lyt_payment_due_date)
        txtPaymentDueDateTag = findViewById(R.id.txt_payment_due_date_tag)
        txtPaymentDueDateValue = findViewById(R.id.txt_payment_due_date_value) // due date
        txtDividerPaymentDueDate = findViewById(R.id.divider_status_payment_due_date)
        txtInvoiceAmountValue = findViewById(R.id.txt_invoice_amt_value) // invoice amount
        txtInvoiceAmountTag = findViewById(R.id.txt_invoice_amt_tag)
        txtPaymentMethod = findViewById(R.id.txt_payment_method) // payment method
        txtPaymentMethod.setVisibility(View.GONE)
        txtLocationValue = findViewById(R.id.txt_location_value) // deliver to
        txtLocationTag = findViewById(R.id.txt_location_tag)
        txtDividerDeliverToLinkedTo = findViewById(R.id.divider_deliver_to_linked_to)
        imgLinkedToIcon = findViewById(R.id.img_linked_to_icon)
        txtLinkedToTag = findViewById(R.id.txt_linked_to_tag)
        txtLinkedToOrderValue = findViewById(R.id.txt_linked_to_order_value) // link to order
        lstLinkedCreditNotes = findViewById(R.id.lst_linked_credit_notes) // link to creditnote
        lstLinkedCreditNotes.setLayoutManager(LinearLayoutManager(this))
        txtItemCount = findViewById(R.id.txt_itemCount)
        txtEstimatedSubtotalValue = findViewById(R.id.txt_estimated_subtotal_value)
        txtEstimatedSubtotalTag = findViewById(R.id.txt_estimated_sub_total_tag)
        txtDiscountValue = findViewById(R.id.txt_discount_value)
        txtDiscountTag = findViewById(R.id.txt_discount_tag)
        txtOthersTag = findViewById(R.id.txt_others_tag)
        txtOthersValue = findViewById(R.id.txt_others_value)
        txtGstTag = findViewById(R.id.txt_gst_tag)
        txtGstTag.setText(ZeemartAppConstants.Market.`this`.taxCode)
        txtGstValue = findViewById(R.id.txt_gst_value)
        txtDeliveryFeeValue = findViewById(R.id.txt_delivery_fee_value)
        txtDeliveryFeeTag = findViewById(R.id.txt_delivery_fee_tag)
        txtEstimatedTotalValue = findViewById(R.id.txt_estimated_total_value)
        txtEstimatedTotalTag = findViewById(R.id.txt_estimated_total)
        viewOriginalInvoice = findViewById(R.id.view_original_invoice) // link to image
        btnMoreOptions = findViewById(R.id.more_options)
        recyclerView = findViewById(R.id.recyclerView)
        lytOrderTotal = findViewById(R.id.lyt_order_total)
        lytLinkedTo = findViewById(R.id.lyt_invoice_detail_linked_to)
        lytStatus = findViewById(R.id.lyt_invoice_detail_status)
        lytSpace = findViewById(R.id.lyt_empty_space)
        txtStatusTag = findViewById(R.id.txt_status_tag)
        txtStatusValue = findViewById(R.id.txt_status_value)
        txtStatusValue.setVisibility(View.INVISIBLE)
        txtDividerStatusInvoiceDate = findViewById(R.id.divider_status_invoice_date)
        threeDotLoaderWhite = findViewById(R.id.lyt_loading_overlay)
        lytPay = findViewById(R.id.lyt_pay)
        txtPay = findViewById(R.id.txt_review_total_items_in_cart)
        txtPay.setText(resources.getString(R.string.txt_btn_pay))
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.setLayoutManager(linearLayoutManager)
        recyclerView.setNestedScrollingEnabled(false)
        invoiceId = intent.extras!!.getString("invoiceId")
        btnBack = findViewById(R.id.img_back_button)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        lytPay.setOnClickListener(View.OnClickListener { createPayConfirmationDialog(invoice) })
        loadInvoice()
        setFont()
    }

    private fun getSpecificOutlet(invoice: Invoice?) {
        OutletsApi.getSpecificOutlet(this, object : GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                if (specificOutlet != null && specificOutlet.data != null
                    && specificOutlet.data!!.company != null && specificOutlet.data!!.company?.companyId != null) {
                    invoice!!.companyId = specificOutlet.data!!.company?.companyId
                    getLinkedSupplierPreference(invoice)
                }
            }

            override fun onError(error: VolleyErrorHelper?) {
                val errorMessage = error?.errorMessage
                val errorType = error?.errorType
                if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                    getToastRed(errorMessage)
                }
            }
        })
    }

    private fun getLinkedSupplierPreference(invoice: Invoice?) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSupplierId(invoice!!.supplier?.supplierId!!)
        apiParamsHelper.setCompanyId(invoice.companyId!!)
        PaymentApi.viewLinkedSupplierPreference(
            this,
            apiParamsHelper,
            invoice.outlet!!,
            object : PaymentApi.CompanyPreferenceListener {
                override fun onSuccessResponse(linkedSupplierPreference: LinkedSupplierPreference?) {
                    if (linkedSupplierPreference?.data != null && linkedSupplierPreference.data!!.payments != null
                        && linkedSupplierPreference?.data!!.payments?.manualPayment?.isActive!! && invoice.isUnPaid(
                            invoice
                        )
                    ) {
                        lytSpace!!.visibility = View.VISIBLE
                        lytPay!!.visibility = View.VISIBLE
                    } else {
                        lytSpace!!.visibility = View.GONE
                        lytPay!!.visibility = View.GONE
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    val errorMessage = error?.errorMessage
                    val errorType = error?.errorType
                    if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        getToastRed(errorMessage)
                    }
                }
            })
    }

    private fun createPayConfirmationDialog(invoice: Invoice?) {
        val totalAmount: Double
        val d = Dialog(this@InvoiceDetailActivity, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_payment_confirmation)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtInvoiceTotalTag = d.findViewById<TextView>(R.id.txt_invoice_total_tag)
        val txtInvoiceTotal = d.findViewById<TextView>(R.id.txt_invoice_total_value)
        val txtProcessFeeTag = d.findViewById<TextView>(R.id.txt_process_fee_tag)
        val txtProcessFee = d.findViewById<TextView>(R.id.txt_process_fee_value)
        val txtCardNumber = d.findViewById<TextView>(R.id.txt_card_number)
        val imgCard = d.findViewById<ImageView>(R.id.img_card)
        val txtTotal = d.findViewById<TextView>(R.id.txt_total_value)
        val btnPay = d.findViewById<Button>(R.id.btn_pay)
        val txtPaymentterms = d.findViewById<TextView>(R.id.txt_payment_terms)
        txtPaymentterms.visibility = View.GONE
        val txtCreditAvailable = d.findViewById<TextView>(R.id.txt_available_credit)
        txtCreditAvailable.text = String.format(
            resources.getString(R.string.txt_available_credit),
            PriceDetails().displayValue
        )
        val btnUseCredits = d.findViewById<TextView>(R.id.txt_use_credit)
        val lytUseCredits = d.findViewById<RelativeLayout>(R.id.lyt_use_credit)
        val txtCredits = d.findViewById<TextView>(R.id.txt_credits)
        val btnRemoveCredits = d.findViewById<TextView>(R.id.txt_remove_credit)
        val txtCreditFeeValue = d.findViewById<TextView>(R.id.txt_credit_fee_value)
        val lytCardDetails = d.findViewById<LinearLayout>(R.id.lyt_card_detail)
        val txtTotalTag = d.findViewById<TextView>(R.id.txt_total)
        txtTotalTag.visibility = View.GONE
        val lytRemoveCredits = d.findViewById<RelativeLayout>(R.id.lyt_remove_credit)
        lytRemoveCredits.visibility = View.GONE
        val lytAddCard = d.findViewById<LinearLayout>(R.id.lyt_add_new_card)
        val txtAddCard = d.findViewById<TextView>(R.id.txt_add_new_Card)
        lytAddCard.visibility = View.GONE
        setTypefaceView(txtInvoiceTotalTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtInvoiceTotal, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtProcessFeeTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtProcessFee, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCardNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtTotal, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnPay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtPaymentterms, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCreditAvailable, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnUseCredits, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCredits, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnRemoveCredits, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCreditFeeValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtTotalTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtAddCard, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        txtInvoiceTotal.text = invoice!!.totalCharge?.displayValue
        totalAmount = invoice.totalCharge?.amount!!
        txtTotal.text = invoice.totalCharge?.displayValue
        totalPayableAmount = invoice.totalCharge
        btnPay.text = getString(R.string.txt_pay, invoice.totalCharge!!.displayValue)
        val builder = SpannableStringBuilder()
        val str1 = SpannableString(resources.getText(R.string.txt_payment_terms))
        str1.setSpan(ForegroundColorSpan(resources.getColor(R.color.dark_grey)), 0, str1.length, 0)
        builder.append(str1)
        builder.append(" ")
        val str2 = SpannableString(resources.getText(R.string.txt_payment_conditions))
        str2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_azul_two)),
            0,
            str2.length,
            0
        )
        builder.append(str2)
        txtPaymentterms.setText(builder, TextView.BufferType.SPANNABLE)

        /*Call eCredit API to get Credit Amount*/
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSupplierId(invoice.supplier?.supplierId!!)
        val outlets: MutableList<Outlet?> = ArrayList()
        outlets.add(SharedPref.defaultOutlet)
        retrieveECreditBySupplier(
            this,
            apiParamsHelper,
            outlets as List<Outlet>?,
            object : GetECreditBySupplierListener {
                override fun result(eCredit: ECreditBySupplier?) {
                    val priceDetails = PriceDetails()
                    if (eCredit != null) {
                        eCreditLst = ECreditBySupplier()
                        eCreditLst = eCredit
                        if (eCredit.data?.totalCredits?.displayValue == PriceDetails().displayValue) {
                            lytUseCredits.visibility = View.GONE
                        } else {
                            lytUseCredits.visibility = View.VISIBLE
                        }
                        txtCreditAvailable.text = String.format(
                            resources.getString(R.string.txt_available_credit),
                            eCredit.data?.totalCredits?.displayValue
                        )
                        if (eCredit.data?.totalCredits?.amount!! < totalAmount) {
                            val amount = "-" + eCredit.data?.totalCredits?.displayValue
                            txtCreditFeeValue.text = amount
                        } else {
                            priceDetails.amount = totalAmount
                            val amount = "-" + priceDetails.displayValue
                            txtCreditFeeValue.text = amount
                        }
                    }
                    btnUseCredits.setOnClickListener {
                        lytUseCredits.visibility = View.GONE
                        lytRemoveCredits.visibility = View.VISIBLE
                        if (totalAmount > eCredit!!.data?.totalCredits?.amount!!) {
                            priceDetails.amount = totalAmount - eCredit.data?.totalCredits?.amount!!
                            txtTotalTag.visibility = View.GONE
                            lytCardDetails.visibility = View.VISIBLE
                            btnPay.text = getString(R.string.txt_pay, priceDetails.displayValue)
                        } else {
                            priceDetails.amount = null
                            txtTotalTag.visibility = View.VISIBLE
                            lytCardDetails.visibility = View.GONE
                            lytAddCard.visibility = View.GONE
                            btnPay.text = getString(R.string.txt_submit)
                        }
                        txtTotal.text = priceDetails.displayValue
                        totalPayableAmount = priceDetails
                    }
                }
            })
        if (invoice.companyId != null) {
            apiParamsHelper.setCompanyId(invoice.companyId!!)
        }
        PaymentApi.retrieveCompanyCardPaymentData(
            this,
            apiParamsHelper,
            invoice.outlet!!,
            object : PaymentApi.CompanyCardDetailsListener {
                override fun onSuccessResponse(paymentCardDetails: PaymentCardDetails?) {
                    val cardList: MutableList<PaymentCardDetails.PaymentResponse> = ArrayList()
                    if (paymentCardDetails != null && paymentCardDetails.data != null && paymentCardDetails.data!!.size > 0) {
                        lytAddCard.visibility = View.GONE
                        lytCardDetails.visibility = View.VISIBLE
                        for (i in paymentCardDetails.data!!.indices) {
                            if (paymentCardDetails.data!![i].status != null && paymentCardDetails.data!![i].status == PaymentStatus.ACTIVE.getmStatusName()) {
                                cardList.add(paymentCardDetails.data!![i])
                            }
                        }
                        for (i in paymentCardDetails.data!!.indices) {
                            if (paymentCardDetails.data!![i].isDefaultCard) {
                                txtCardNumber.text =
                                    resources.getString(R.string.txt_card_bullet) + " " + paymentCardDetails.data!![i].customerCardData?.cardLast4Digits
                                if (paymentCardDetails.data!![i].customerCardData?.isCardType(
                                        PaymentCardDetails.CardType.VISA
                                    )!!
                                ) {
                                    imgCard.setImageResource(R.drawable.visa)
                                } else if (paymentCardDetails.data!![i].customerCardData?.isCardType(
                                        PaymentCardDetails.CardType.MASTERCARD
                                    )!!
                                ) {
                                    imgCard.setImageResource(R.drawable.master_card)
                                } else {
                                    imgCard.setImageResource(R.drawable.amex)
                                }
                                imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                cardReferenceNo = paymentCardDetails.data!![i].referenceNo
                            }
                        }
                    } else {
                        lytAddCard.visibility = View.VISIBLE
                        lytCardDetails.visibility = View.GONE
                    }
                    lytAddCard.setOnClickListener {
                        ShowAddNewCardDialog(
                            this@InvoiceDetailActivity,
                            invoice.companyId,
                            object : onCardAddedListener {
                                override fun onCardSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse) {
                                    selectedCardDetails.isDefaultCard = true
                                    cardList.add(selectedCardDetails)
                                    lytAddCard.visibility = View.GONE
                                    lytCardDetails.visibility = View.VISIBLE
                                    txtCardNumber.text =
                                        resources.getString(R.string.txt_card_bullet) + " " + selectedCardDetails.customerCardData?.cardLast4Digits
                                    if (selectedCardDetails.customerCardData?.isCardType(
                                            PaymentCardDetails.CardType.VISA
                                        )!!
                                    ) {
                                        imgCard.setImageResource(R.drawable.visa)
                                    } else if (selectedCardDetails.customerCardData?.isCardType(
                                            PaymentCardDetails.CardType.MASTERCARD
                                        )!!
                                    ) {
                                        imgCard.setImageResource(R.drawable.master_card)
                                    } else {
                                        imgCard.setImageResource(R.drawable.amex)
                                    }
                                    imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                    cardReferenceNo = selectedCardDetails.referenceNo
                                }
                            })
                    }
                    lytCardDetails.setOnClickListener {
                        CommonMethods.avoidMultipleClicks(lytCardDetails)
                        if (cardList != null && cardList.size > 0) {
                            val cardListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(cardList)
                            ShowCardListDialog(
                                this@InvoiceDetailActivity,
                                cardListJson,
                                invoice.companyId,
                                object : onCardSelectedListener {
                                    override fun onCardSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse?) {
                                        for (i in cardList.indices) {
                                            if (cardList[i].referenceNo == selectedCardDetails!!.referenceNo) {
                                                cardList[i].isDefaultCard = true
                                            } else {
                                                cardList[i].isDefaultCard = false
                                            }
                                        }
                                        val paymentCardDefault = SetPaymentCardDefault()
                                        paymentCardDefault.companyId = invoice.companyId
                                        paymentCardDefault.referenceNo =
                                            selectedCardDetails!!.referenceNo
                                        paymentCardDefault.setAsDefaultBy =
                                            SharedPref.currentUserDetail
                                        PaymentApi.setPaymentCardDefault(
                                            this@InvoiceDetailActivity,
                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                paymentCardDefault
                                            ),
                                            object : GetRequestStatusResponseListener {
                                                override fun onSuccessResponse(status: String?) {
//                                           No Implementation required
                                                }

                                                override fun onErrorResponse(error: VolleyErrorHelper?) {
//                                          No Implementation required
                                                }
                                            })
                                        txtCardNumber.text =
                                            resources.getString(R.string.txt_card_bullet) + " " + selectedCardDetails.customerCardData?.cardLast4Digits
                                        if (selectedCardDetails.customerCardData?.isCardType(
                                                PaymentCardDetails.CardType.VISA
                                            )!!
                                        ) {
                                            imgCard.setImageResource(R.drawable.visa)
                                        } else if (selectedCardDetails.customerCardData?.isCardType(
                                                PaymentCardDetails.CardType.MASTERCARD
                                            )!!
                                        ) {
                                            imgCard.setImageResource(R.drawable.master_card)
                                        } else {
                                            imgCard.setImageResource(R.drawable.amex)
                                        }
                                        imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
                                        cardReferenceNo = selectedCardDetails.referenceNo
                                    }
                                })
                        }
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    val errorMessage = error?.errorMessage
                    val errorType = error?.errorType
                    if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        getToastRed(errorMessage)
                    }
                }
            })
        btnRemoveCredits.setOnClickListener {
            lytUseCredits.visibility = View.VISIBLE
            lytRemoveCredits.visibility = View.GONE
            txtInvoiceTotal.text = invoice.totalCharge!!.displayValue
            txtTotal.text = invoice.totalCharge!!.displayValue
            totalPayableAmount = invoice.totalCharge
            txtTotalTag.visibility = View.GONE
            lytCardDetails.visibility = View.VISIBLE
            btnPay.text = getString(R.string.txt_pay, invoice.totalCharge!!.displayValue)
        }
        btnPay.setOnClickListener {
            val usedCredit = PayInvoiceRequest.UsedCredit()
            if (lytUseCredits.visibility == View.GONE && eCreditLst != null) {
                if (eCreditLst!!.data?.totalCredits?.amount!! < totalAmount) {
                    usedCredit.amount = eCreditLst!!.data?.totalCredits?.amount
                } else {
                    usedCredit.amount = invoice.totalCharge!!.amount
                }
                usedCredit.currencyCode = invoice.totalCharge!!.currencyCode
            }
            val invoiceIdsList: MutableList<String> = ArrayList()
            invoiceIdsList.add(invoice.invoiceId!!)
            val payInvoiceRequest = PayInvoiceRequest()
            payInvoiceRequest.supplierId = invoice.supplier!!.supplierId
            payInvoiceRequest.invoiceIds = invoiceIdsList
            payInvoiceRequest.usedCredit = usedCredit
            payInvoiceRequest.cardReferenceNo = cardReferenceNo
            payInvoiceRequest.paidBy = invoice.createdBy
            payInvoiceRequest.status = Invoice.PaymentStatus.PAID
            PaymentApi.payInvoice(
                this@InvoiceDetailActivity,
                payInvoiceRequest,
                invoice.outlet!!,
                object : PaymentApi.PayInvoiceListener {
                    override fun onSuccessResponse(payInvoice: PayInvoiceResponse?) {
                        if (payInvoice?.data != null) {
                            if (payInvoice?.data!!.isTransactionStatus(PayInvoiceResponse.TransactionStatus.PAID)) {
                                //send data to clevertap
                                AnalyticsHelper.logAction(
                                    this@InvoiceDetailActivity,
                                    AnalyticsHelper.TAP_INVOICE_PAY,
                                    invoice.outlet!!,
                                    invoice.supplier!!,
                                    totalPayableAmount!!,
                                    invoiceIdsList.size
                                )
                                val dialogSuccess = alertDialogPaymentSuccess(
                                    this@InvoiceDetailActivity, getString(
                                        R.string.txt_payment_success
                                    )
                                )
                                Handler().postDelayed({
                                    dialogSuccess.dismiss()
                                    loadInvoice()
                                }, 2000)
                            } else {
                                threeDotLoaderWhite!!.visibility = View.GONE
                                val dialogFailure = alertDialogPaymentFailure(
                                    this@InvoiceDetailActivity, getString(
                                        R.string.txt_payment_failed
                                    ), payInvoice.data!!.failureReason
                                )
                                dialogFailure.show()
                                Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                            }
                        } else {
                            threeDotLoaderWhite!!.visibility = View.GONE
                            val dialogFailure = alertDialogPaymentFailure(
                                this@InvoiceDetailActivity, getString(
                                    R.string.txt_payment_failed
                                ), getString(R.string.txt_please_try_again)
                            )
                            dialogFailure.show()
                            Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                        }
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        val errorMessage = error?.errorMessage
                        val errorType = error?.errorType
                        threeDotLoaderWhite!!.visibility = View.GONE
                        if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                            getToastRed(errorMessage)
                        } else {
                            getToastRed(errorMessage)
                        }
                    }
                })
            d.dismiss()
            threeDotLoaderWhite!!.visibility = View.VISIBLE
        }
        d.show()
    }

    private fun setFont() {
        setTypefaceView(
            txtInvoiceDetailHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtInvoiceNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtInvoiceDateValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtPaymentDueDateValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtInvoiceAmountValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtPaymentMethod, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtLocationValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtItemCount, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtEstimatedTotalValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtEstimatedTotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(viewOriginalInvoice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDeliveryFeeValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtGstValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtOthersValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDiscountValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtEstimatedSubtotalValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtDeliveryFeeTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtGstTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtOthersTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDiscountTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtLinkedToTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtLocationTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtInvoiceAmountTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtInvoiceDateTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtPaymentDueDateTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtEstimatedSubtotalTag,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtLinkedToOrderValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtInvoiceType, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtStatusTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtStatusValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtPay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    private fun createImageFullscreenDialog(invoice: Invoice?) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_fullscreen)
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val viewPager = dialog.findViewById<ViewPager>(R.id.imageView)
        viewPager.adapter =
            InvoiceImagesSliderAdapter(this, invoice!!, object : InvoiceImageListener {
                override fun invoiceUpdated(inv: Invoice?) {
                    //no action required for invoice update
                }

                override fun invoiceDeleted(inv: Invoice?, isDeleted: Boolean) {
                    //no action required when invoice deleted
                }

                override fun onRejectedInvoiceBackPressed() {
                    //not relevant for invoice detail screen
                }
            })
        dialog.show()
    }

    private fun createInvoiceDetailMenuDialog(inv: Invoice?) {
        AnalyticsHelper.logAction(
            this@InvoiceDetailActivity,
            AnalyticsHelper.TAP_INVOICE_DETAIL_MORE
        )
        val popup = PopupMenu(this@InvoiceDetailActivity, btnMoreOptions)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.invoice_details_menu, popup.menu)
        val item = popup.menu.getItem(0)
        if (inv!!.isInvoiceDeletable) {
            val s = SpannableString(resources.getString(R.string.txt_delete_processed_invoice))
            s.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_orange_popup_menu)),
                0,
                s.length,
                0
            )
            item.title = s
            item.isVisible = true
        } else {
            item.isVisible = false
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_report_mistake -> {
                    AnalyticsHelper.logAction(
                        this@InvoiceDetailActivity,
                        AnalyticsHelper.TAP_INVOICE_DETAIL_REPORT
                    )
                    popup.dismiss()
                    EmailHelper.SendFeedback(this@InvoiceDetailActivity, invoice!!)
                    true
                }
                R.id.item_delete_invoice -> {
                    //display the confirmation dialog
                    AnalyticsHelper.logAction(
                        this@InvoiceDetailActivity,
                        AnalyticsHelper.TAP_INVOICE_DETAIL_DELETE_INVOICE
                    )
                    val builder = AlertDialog.Builder(this@InvoiceDetailActivity)
                    builder.setPositiveButton(R.string.txt_yes_delete) { dialog, which ->
                        dialog.dismiss()
                        deleteProcessedInvoice(
                            this@InvoiceDetailActivity,
                            invoiceId,
                            object : GetRequestStatusResponseListener {
                                override fun onSuccessResponse(status: String?) {
                                    finish()
                                }

                                override fun onErrorResponse(error: VolleyErrorHelper?) {
                                    getToastRed(getString(R.string.txt_delete_processed_invoice_error))
                                }
                            })
                    }
                    builder.setNegativeButton(R.string.dialog_cancel_button_text) { dialog, which -> dialog.dismiss() }
                    val dialog = builder.create()
                    dialog.setCancelable(false)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.setMessage(getString(R.string.txt_delete_invoice_message))
                    dialog.show()
                    val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    deleteBtn.setTextColor(resources.getColor(R.color.chart_red))
                    deleteBtn.isAllCaps = false
                    val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    cancelBtn.isAllCaps = false
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun loadInvoice() {
        if (invoiceId != null) {
            val outlet: MutableList<Outlet?> = ArrayList()
            outlet.add(SharedPref.defaultOutlet)
            GetInvoice(this@InvoiceDetailActivity, outlet as List<Outlet>?, invoiceId, object : GetInvoice {
                override fun result(inv: Invoice?) {
                    invoice = inv
                    getSpecificOutlet(invoice)
                    if (invoice!!.isInvoiceType(Invoice.InvoiceType.EINVOICE)) {
                        imgInvoiceType!!.visibility = View.VISIBLE
                        btnMoreOptions!!.visibility = View.GONE
                        if (invoice!!.isStatus(Invoice.Status.VOIDED)) {
                            viewOriginalInvoice!!.visibility = View.GONE
                            //grey out the products and total
                            recyclerView!!.alpha = 0.5f
                            lytOrderTotal!!.alpha = 0.5f
                            lytStatus!!.visibility = View.VISIBLE
                            txtStatusValue!!.visibility = View.VISIBLE
                            txtDividerStatusInvoiceDate!!.visibility = View.VISIBLE
                            txtStatusValue!!.text = invoice!!.status?.uppercase(Locale.getDefault())
                            lytPaymentDueDate!!.visibility = View.GONE
                            txtDividerPaymentDueDate!!.visibility = View.GONE
                        } else {
                            viewOriginalInvoice!!.visibility = View.VISIBLE
                            recyclerView!!.alpha = 1f
                            lytOrderTotal!!.alpha = 1f
                            viewOriginalInvoice!!.text = getString(R.string.txt_download_pdf)
                            lytStatus!!.visibility = View.GONE
                            txtDividerStatusInvoiceDate!!.visibility = View.GONE
                        }
                    } else if (invoice!!.isInvoiceType(Invoice.InvoiceType.ECREDITNOTE)) {
                        recyclerView!!.alpha = 1f
                        lytOrderTotal!!.alpha = 1f
                        imgInvoiceType!!.visibility = View.VISIBLE
                        btnMoreOptions!!.visibility = View.GONE
                        viewOriginalInvoice!!.visibility = View.VISIBLE
                        viewOriginalInvoice!!.text = getString(R.string.txt_download_pdf)
                        lytStatus!!.visibility = View.GONE
                        txtDividerStatusInvoiceDate!!.visibility = View.GONE
                        lytPaymentDueDate!!.visibility = View.GONE
                        txtDividerPaymentDueDate!!.visibility = View.GONE
                    } else {
                        recyclerView!!.alpha = 1f
                        lytOrderTotal!!.alpha = 1f
                        imgInvoiceType!!.visibility = View.GONE
                        btnMoreOptions!!.visibility = View.VISIBLE
                        viewOriginalInvoice!!.visibility = View.VISIBLE
                        viewOriginalInvoice!!.text = getString(R.string.view_original_invoice)
                        lytStatus!!.visibility = View.GONE
                        txtDividerStatusInvoiceDate!!.visibility = View.GONE
                    }
                    btnMoreOptions!!.setOnClickListener { createInvoiceDetailMenuDialog(invoice) }
                    if (invoice!!.isInvoiceType(Invoice.InvoiceType.ECREDITNOTE)) {
                        txtInvoiceDetailHeading!!.text = getString(R.string.txt_credit_note_details)
                        txtInvoiceDateTag!!.text =
                            getString(R.string.content_filter_orders_txt_date_header_text)
                        val text =
                            getString(R.string.txt_e_credit_note) + " " + getString(R.string.txt_dot_seperator) + " " + invoice!!.invoiceNum
                        val spannable: Spannable = SpannableString(text)
                        spannable.setSpan(
                            ForegroundColorSpan(resources.getColor(R.color.color_orange)),
                            0,
                            getString(
                                R.string.txt_e_credit_note
                            ).length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        txtInvoiceNumber!!.text = spannable
                    } else {
                        txtInvoiceDetailHeading!!.text =
                            resources.getString(R.string.txt_invoice_details)
                        txtInvoiceDateTag!!.text = getString(R.string.invoice_date)
                        val text =
                            getString(R.string.txt_e_invoice) + " " + getString(R.string.txt_dot_seperator) + " " + invoice!!.invoiceNum
                        val spannable: Spannable = SpannableString(text)
                        spannable.setSpan(
                            ForegroundColorSpan(resources.getColor(R.color.color_orange)),
                            0,
                            getString(
                                R.string.txt_e_invoice
                            ).length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        txtInvoiceNumber!!.text = spannable
                    }
                    if (invoice!!.isInvoiceType(Invoice.InvoiceType.EINVOICE) || invoice!!.isInvoiceType(
                            Invoice.InvoiceType.ECREDITNOTE
                        )
                    ) {
                        if (invoice!!.isPaymentStatus(Invoice.PaymentStatus.PAID)) {
                            lytStatus!!.visibility = View.VISIBLE
                            txtStatusValue!!.visibility = View.VISIBLE
                            txtDividerStatusInvoiceDate!!.visibility = View.VISIBLE
                            txtStatusValue!!.background =
                                resources.getDrawable(R.drawable.green_solid_round_corner)
                            txtStatusValue!!.setTextColor(resources.getColor(R.color.white))
                            txtStatusValue!!.text = getString(R.string.txt_paid)
                        } else if (invoice!!.isPaymentStatus(Invoice.PaymentStatus.USED)) {
                            lytStatus!!.visibility = View.VISIBLE
                            txtStatusValue!!.visibility = View.VISIBLE
                            txtDividerStatusInvoiceDate!!.visibility = View.VISIBLE
                            txtStatusValue!!.setTextColor(resources.getColor(R.color.white))
                            txtStatusValue!!.background =
                                resources.getDrawable(R.drawable.green_solid_round_corner)
                            txtStatusValue!!.text = getString(R.string.txt_used)
                        }
                    }
                    txtSupplierName!!.text = invoice!!.supplier?.supplierName
                    if (StringHelper.isStringNullOrEmpty(invoice!!.supplier?.logoURL)) {
                        imgSupplierImage!!.visibility = View.INVISIBLE
                        lytSupplierThumbNail!!.visibility = View.VISIBLE
                        lytSupplierThumbNail!!.setBackgroundColor(
                            CommonMethods.SupplierThumbNailBackgroundColor(
                                invoice!!.supplier?.supplierName!!, this@InvoiceDetailActivity
                            )
                        )
                        txtSupplierThumbNail!!.text = CommonMethods.SupplierThumbNailShortCutText(
                            invoice!!.supplier?.supplierName!!
                        )
                        txtSupplierThumbNail!!.setTextColor(
                            CommonMethods.SupplierThumbNailTextColor(
                                invoice!!.supplier?.supplierName!!, this@InvoiceDetailActivity
                            )
                        )
                    } else {
                        imgSupplierImage!!.visibility = View.VISIBLE
                        lytSupplierThumbNail!!.visibility = View.GONE
                        Picasso.get().load(invoice!!.supplier?.logoURL)
                            .placeholder(R.drawable.placeholder_all).resize(
                            PLACE_HOLDER_SUPPLIER_IMAGE_SIZE, PLACE_HOLDER_SUPPLIER_IMAGE_SIZE
                        ).into(imgSupplierImage)
                    } //VolleyRequest.getInstance(InvoiceDetailActivity.this).setImageToImageView(imgSupplierImage, invoice.getSupplier().getLogoURL(),R.drawable.placeholder_all);
                    txtInvoiceDateValue!!.text =
                        DateHelper.getDateInDateMonthYearFormat(invoice!!.invoiceDate) // invoice date
                    txtPaymentDueDateValue!!.text =
                        DateHelper.getDateInDateMonthYearFormat(invoice!!.paymentDueDate) // due date
                    txtInvoiceAmountValue!!.text =
                        invoice!!.totalCharge?.displayValue // invoice amount
                    if (StringHelper.isStringNullOrEmpty(invoice!!.paymentTerms)) {
                        txtPaymentMethod!!.visibility = View.GONE
                    } else {
                        txtPaymentMethod!!.visibility = View.VISIBLE
                        txtPaymentMethod!!.text =
                            resources.getString(R.string.txt_invoice_payment_terms) + " " + invoice!!.paymentTerms // payment method
                    }
                    txtLocationValue!!.text = invoice!!.outlet?.outletName // deliver to
                    if (invoice!!.orderIds != null && invoice!!.orderIds?.size!! > 0) {
                        if (invoice!!.guestInvoice != null && invoice!!.guestInvoice == true) {
                            lytLinkedTo!!.visibility = View.GONE
                            txtDividerDeliverToLinkedTo!!.visibility = View.GONE
                        } else {
                            if (invoice!!.orderIds!![0].isEmpty()) {
                                lytLinkedTo!!.visibility = View.GONE
                                txtDividerDeliverToLinkedTo!!.visibility = View.GONE
                            } else {
                                lytLinkedTo!!.visibility = View.VISIBLE // link to order
                                txtDividerDeliverToLinkedTo!!.visibility = View.VISIBLE
                                txtLinkedToOrderValue!!.text =
                                    resources.getString(R.string.txt_order_with_hash_tag) + invoice!!.orderIds!![0] // link to order
                                if (invoice!!.linkedCreditNotes != null) {
                                    val linkedCreditNotesListAdapter = LinkedCreditNotesListAdapter(
                                        this@InvoiceDetailActivity,
                                        invoice!!.linkedCreditNotes,
                                        object : LinkedCreditNotesListAdapter.TapOnInvoiceListener{
                                           override fun onInvoiceTapped(linkedCreditNotes: LinkedCreditNotes?) {
                                                val newIntent = Intent(
                                                    this@InvoiceDetailActivity,
                                                    InvoiceDetailActivity::class.java
                                                )
                                                newIntent.putExtra(
                                                    ZeemartAppConstants.INVOICE_ID,
                                                    linkedCreditNotes?.invoiceId
                                                )
                                                this@InvoiceDetailActivity.startActivity(newIntent)
                                            }
                                        })
                                    lstLinkedCreditNotes.adapter = linkedCreditNotesListAdapter
                                } else {
                                    lstLinkedCreditNotes!!.visibility = View.GONE
                                }
                                txtLinkedToOrderValue!!.setOnClickListener {
                                    AnalyticsHelper.logAction(
                                        this@InvoiceDetailActivity,
                                        AnalyticsHelper.TAP_INVOICE_DETAIL_LINKED_ORDER
                                    )
                                    val newIntent = Intent(
                                        this@InvoiceDetailActivity,
                                        OrderDetailsActivity::class.java
                                    )
                                    newIntent.putExtra(
                                        ZeemartAppConstants.ORDER_ID,
                                        invoice!!.orderIds!![0]
                                    )
                                    newIntent.putExtra(
                                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                                        invoice!!.outlet?.outletId
                                    )
                                    startActivity(newIntent)
                                }
                            }
                        }
                    } else if (invoice!!.isInvoiceType(Invoice.InvoiceType.ECREDITNOTE) && invoice!!.linkedInvoice != null) {
                        lytLinkedTo!!.visibility = View.VISIBLE // link to order
                        txtDividerDeliverToLinkedTo!!.visibility = View.VISIBLE
                        txtLinkedToOrderValue!!.text =
                            resources.getString(R.string.txt_invoice_with_hash_tag) + invoice!!.linkedInvoice?.invoiceNum // link to order
                        lstLinkedCreditNotes!!.visibility = View.GONE
                        txtLinkedToOrderValue!!.setOnClickListener {
                            AnalyticsHelper.logAction(
                                this@InvoiceDetailActivity,
                                AnalyticsHelper.TAP_ITEM_INVOICES_INVOICE
                            )
                            val newIntent = Intent(
                                this@InvoiceDetailActivity,
                                InvoiceDetailActivity::class.java
                            )
                            newIntent.putExtra(
                                ZeemartAppConstants.INVOICE_ID,
                                invoice!!.linkedInvoice?.invoiceId
                            )
                            this@InvoiceDetailActivity.startActivity(newIntent)
                        }
                    } else {
                        lytLinkedTo!!.visibility = View.GONE
                        txtDividerDeliverToLinkedTo!!.visibility = View.GONE
                    }
                    if (invoice!!.products != null && invoice!!.products?.size!! > 0) {
                        count = count + invoice!!.products?.size!!
                    }
                    if (invoice!!.customLineItems != null && invoice!!.customLineItems?.size!! > 0) {
                        count = count + invoice!!.customLineItems?.size!!
                    }
                    if (count == 0) {
                        txtItemCount!!.text = String.format(getString(R.string.format_items), 0)
                    } else if (count == 1) {
                        txtItemCount!!.text =
                            count.toString() + " " + resources.getString(R.string.txt_item)
                    } else {
                        txtItemCount!!.text = String.format(getString(R.string.format_items), count)
                    }
                    if (!StringHelper.isStringNullOrEmpty(invoice!!.subtotal?.displayValue)) {
                        txtEstimatedSubtotalValue!!.text = invoice!!.subtotal?.displayValue
                    }
                    if (invoice!!.discount != null && !StringHelper.isStringNullOrEmpty(
                            invoice!!.discount?.displayValue
                        )
                    ) {
                        txtDiscountValue!!.text = invoice!!.discount?.displayValue
                    }
                    if (invoice!!.others != null && invoice!!.others?.charge?.amount != 0.0) {
                        if (!StringHelper.isStringNullOrEmpty(invoice!!.others?.name)) {
                            txtOthersTag!!.text = invoice!!.others?.name
                        }
                        if (!StringHelper.isStringNullOrEmpty(invoice!!.others?.charge?.displayValue)) {
                            txtOthersValue!!.text = invoice!!.others?.charge?.displayValue
                        }
                    } else {
                        txtOthersTag!!.visibility = View.GONE
                        txtOthersValue!!.visibility = View.GONE
                    }
                    if (!StringHelper.isStringNullOrEmpty(invoice!!.gst?.displayValue)) {
                        txtGstValue!!.text = invoice!!.gst?.displayValue
                    }
                    if (invoice!!.deliveryFee != null && invoice!!.deliveryFee?.amount != 0.0) {
                        txtDeliveryFeeValue!!.text = invoice!!.deliveryFee?.displayValue
                    } else {
                        txtDeliveryFeeTag!!.visibility = View.GONE
                        txtDeliveryFeeValue!!.visibility = View.GONE
                    }
                    if (!StringHelper.isStringNullOrEmpty(invoice!!.totalCharge?.displayValue)) {
                        txtEstimatedTotalValue!!.text = invoice!!.totalCharge?.displayValue
                    }
                    viewOriginalInvoice!!.setOnClickListener {
                        if ((invoice!!.isInvoiceType(Invoice.InvoiceType.EINVOICE) || invoice!!.isInvoiceType(
                                Invoice.InvoiceType.ECREDITNOTE
                            ))
                            && !invoice!!.isStatus(Invoice.Status.VOIDED)
                        ) {
                            val invoicePdfFileName =
                                "invoice_" + invoice!!.invoiceId + "_zeemart.pdf"
                            if (!StringHelper.isStringNullOrEmpty(invoice!!.pdfURL)) {
                                AnalyticsHelper.logAction(
                                    this@InvoiceDetailActivity,
                                    AnalyticsHelper.TAP_INVOICE_DETAIL_DOWNLOAD_PDF
                                )
                                downloadPDF(invoice!!.pdfURL!!, invoicePdfFileName)
                            } else getToastRed("PDF not available")
                        } else {
                            if (invoice!!.images != null && invoice!!.images?.size!! > 0) {
                                AnalyticsHelper.logAction(
                                    this@InvoiceDetailActivity,
                                    AnalyticsHelper.TAP_INVOICE_DETAIL_VIEW_ORIGINAL_INVOICE
                                )
                                createImageFullscreenDialog(invoice)
                            }
                        }
                    } // link to image
                    threeDotLoaderWhite!!.visibility = View.GONE
                    val pdMap: Map<String, ProductDetailBySupplier> = HashMap()
                    val lstProduct: MutableList<InvoiceProduct> = ArrayList()
                    if (invoice!!.products != null) {
                        lstProduct.addAll(invoice!!.products!!)
                    }
                    if (invoice!!.customLineItems != null) {
                        lstProduct.addAll(invoice!!.customLineItems!!)
                    }
                    recyclerView!!.adapter =
                        InvoiceProductsListAdapter(this@InvoiceDetailActivity, lstProduct, pdMap)
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if (invoice != null && StringHelper.isStringNullOrEmpty(invoice!!.invoiceType) && invoice!!.isInvoiceType(
                            Invoice.InvoiceType.EINVOICE
                        )
                        && !invoice!!.isStatus(Invoice.Status.VOIDED)
                    ) {
                        val invoicePDFFIleName = "invoice_" + invoice!!.invoiceId + "_zeemart.pdf"
                        if (!StringHelper.isStringNullOrEmpty(invoice!!.pdfURL)) downloadPDF(
                            invoice!!.pdfURL!!, invoicePDFFIleName
                        ) else getToastRed("PDF not available")
                    }
                }
                return
            }
            PermissionCallback.WRITE_IMAGE -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    permissionCallback!!.allowed(PermissionCallback.WRITE_IMAGE)
                } else {
                    permissionCallback!!.denied(PermissionCallback.WRITE_IMAGE)
                }
            }
            else -> {}
        }
    }

    /**
     * downloads the Order PDF file to the Downlpoads folder
     */
    private fun downloadPDF(pdfUrl: String, fileName: String) {
        if (ContextCompat.checkSelfPermission(
                this@InvoiceDetailActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request for permission
            ActivityCompat.requestPermissions(
                this@InvoiceDetailActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
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
            else -> {}
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
        private val PLACE_HOLDER_SUPPLIER_IMAGE_SIZE = CommonMethods.dpToPx(50)
    }
}