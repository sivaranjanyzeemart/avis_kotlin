package zeemart.asia.buyers.orderPayments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputListener
import com.stripe.android.view.CardInputWidget
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastGreen
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.stripePublicKey
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.AddCardRequestBody
import zeemart.asia.buyers.network.PaymentApi.Companion.addPaymentCard
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by RajPrudhviMarella on 03/Aug/2020.
 */
class AddCardActivity : AppCompatActivity() {
    private var txtByAddingCard: TextView? = null
    private var txtTermsOfUse: TextView? = null
    private var txtHeader: TextView? = null
    private lateinit var btnSave: Button
    private lateinit var imgFinish: ImageView
    private lateinit var cardInputWidget: CardInputWidget
    private lateinit var stripe: Stripe
    private var companyId: String? = null
    private lateinit var threeDotLoader: CustomLoadingViewWhite
    private var isCardCompleted = false
    private var isCVVCompleted = false
    private var isExpiryMonthCompleted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
        val bundle = intent.extras
        if (bundle != null && bundle.containsKey(ZeemartAppConstants.COMPANY_ID)) {
            companyId = bundle.getString(ZeemartAppConstants.COMPANY_ID)
        }
        PaymentConfiguration.init(this, stripePublicKey)
        cardInputWidget = findViewById(R.id.cardInputWidget)
        txtHeader = findViewById(R.id.txt_add_card_header)
        txtByAddingCard = findViewById(R.id.txt_agree_terms)
        txtTermsOfUse = findViewById(R.id.txt_terms_of_use)
        btnSave = findViewById(R.id.btn_save_card)
        threeDotLoader = findViewById(R.id.three_dot_loader)
        threeDotLoader.setVisibility(View.GONE)
        imgFinish = findViewById(R.id.btn_close_add_card)
        imgFinish.setOnClickListener(View.OnClickListener { finish() })
        setFont()
        btnSave.setBackgroundColor(resources.getColor(R.color.grey_medium))
        btnSave.setClickable(false)
        cardInputWidget.postalCodeEnabled = false
        cardInputWidget.setCvcNumberTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                isCVVCompleted = false
                setSaveButtonUi()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        cardInputWidget.setExpiryDateTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                isExpiryMonthCompleted = false
                setSaveButtonUi()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        cardInputWidget.setCardNumberTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                isCardCompleted = false
                setSaveButtonUi()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        cardInputWidget.setCardInputListener(object : CardInputListener {
            override fun onFocusChange(focusField: CardInputListener.FocusField) {}
            override fun onCardComplete() {
                isCardCompleted = true
                setSaveButtonUi()
            }

            override fun onExpirationComplete() {
                isExpiryMonthCompleted = true
                setSaveButtonUi()
            }

            override fun onCvcComplete() {
                isCVVCompleted = true
                setSaveButtonUi()
            }
        })
        btnSave.setOnClickListener {
            threeDotLoader.visibility = View.VISIBLE
            // Collect card details
            val cardInputWidget = findViewById<CardInputWidget>(R.id.cardInputWidget)
            val cardParams = cardInputWidget.cardParams
            if (cardParams != null) {
                // Use the key from the server to initialize the Stripe instance.
                stripe = Stripe(applicationContext, stripePublicKey)
                stripe.createCardToken(
                    cardParams, null, null,
                    object : ApiResultCallback<Token> {
                       override fun onSuccess(token: Token) {
                            val requestBody = AddCardRequestBody()
                            requestBody.cardLast4Digits = token.card!!.last4
                            requestBody.cardType = token.card!!.brand.displayName
                            requestBody.cardToken = token.id
                            requestBody.expMonth = token.card!!.expMonth
                            requestBody.expYear = token.card!!.expYear
                            requestBody.createdBy = SharedPref.currentUserDetail
                            requestBody.timeTokenGenerated = token.created.time
                            requestBody.companyId = companyId
                            addPaymentCard(
                                this@AddCardActivity,
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(requestBody),
                                object : GetRequestStatusResponseListener {
                                    override fun onSuccessResponse(status: String?) {
                                        threeDotLoader.visibility = View.GONE
                                        getToastGreen("card added Successfully")
                                        finish()
                                    }

                                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                                        threeDotLoader.visibility = View.GONE
                                        getToastRed("Didnt Successfully added card")
                                    }
                                })
                        }

                        override fun onError(e: Exception) {
                            threeDotLoader.visibility = View.GONE
                        }
                    })
            }
        }
    }

    private fun setSaveButtonUi() {
        if (isCardCompleted && isCVVCompleted && isExpiryMonthCompleted) {
            btnSave!!.setBackgroundColor(resources.getColor(R.color.green))
            btnSave!!.isClickable = true
        } else {
            btnSave!!.setBackgroundColor(resources.getColor(R.color.grey_medium))
            btnSave!!.isClickable = false
        }
    }

    private fun setFont() {
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtByAddingCard, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtTermsOfUse, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnSave, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }
}