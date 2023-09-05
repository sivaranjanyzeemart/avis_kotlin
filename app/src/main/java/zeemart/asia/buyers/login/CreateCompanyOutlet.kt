package zeemart.asia.buyers.login

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.ChangeLanguageLocale
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJsonList
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DialogHelper.alertPaymentDialogSmallFailure
import zeemart.asia.buyers.helper.DialogHelper.alertPaymentDialogSmallSuccess
import zeemart.asia.buyers.helper.DialogHelper.displayErrorMessageDialog
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.BuyerCredentials
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.models.OutletFuturesModel
import zeemart.asia.buyers.modelsimport.CreateCompanyOutletRequest
import zeemart.asia.buyers.modelsimport.CreateCompanyOutletResponse
import zeemart.asia.buyers.network.LoginApi
import zeemart.asia.buyers.network.LoginApi.LoginBuyerListener
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.io.IOException
import java.util.*

/**
 * Created by RajPrudhviMarella on 22/sep/2020.
 */
class CreateCompanyOutlet : AppCompatActivity() {
    private lateinit var imgBackButton: ImageView
    private lateinit var textPaymentTermsAndConditions: TextView
    private lateinit var btnCreateAccount: Button
    private var txtCompleteDetails: TextView? = null
    private var txtSubCompleteDetails: TextView? = null
    private var txtCompanyNameHeader: TextView? = null
    private lateinit var edtCompanyName: EditText
    private var txtCompanyEmailHeader: TextView? = null
    private lateinit var edtCompanyEmail: EditText
    private var txtDeliveryAddressHeader: TextView? = null
    private lateinit var edtDeliveryAddress: TextView
    private lateinit var edtUnitNumber: EditText
    private var txtLocationNameHeader: TextView? = null
    private lateinit var edtLocationName: EditText
    private lateinit var customLoadingViewWhite: CustomLoadingViewWhite
    private var place: Place? = null
    private lateinit var lytAlcohol: RelativeLayout
    private lateinit var imgTickAlcohol: ImageView
    private lateinit var txtAlcohol: TextView
    private lateinit var lytHalal: RelativeLayout
    private lateinit var imgTickHalal: ImageView
    private lateinit var txtHalal: TextView
    private var isHalalSelected = false
    private var isAlcoholSelected = false
    private var userID: String? = ""
    private var userName: String? = ""
    private var userPassword: String? = ""
    private var postalCode = ""
    private var isMobileSelected = false
    private lateinit var lytCuisineType: LinearLayout
    private var txtCuisineType: TextView? = null
    private var txtCuisineTypeHeader: TextView? = null
    private var lstSelectedCuisineType: List<OutletFuturesModel.CuisineType>? = null
    private lateinit var lytMobileNumber: LinearLayout
    private var txtMobileNumberHeader: TextView? = null
    private var txtMobileNumber: TextView? = null
    private lateinit var edtMobileNumber: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_company_outlet)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.USER_ID)) {
                userID = intent.extras!!.getString(ZeemartAppConstants.USER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.ON_BOARD_USER_NAME)) {
                userName = intent.extras!!.getString(ZeemartAppConstants.ON_BOARD_USER_NAME)
            }
            if (bundle.containsKey(ZeemartAppConstants.ON_BOARD_PASSWORD)) {
                userPassword = intent.extras!!.getString(ZeemartAppConstants.ON_BOARD_PASSWORD)
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED)) {
                isMobileSelected =
                    intent.extras!!.getBoolean(ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED)
            }
        }
        lytMobileNumber = findViewById(R.id.lyt_mobile_number)
        txtMobileNumber = findViewById(R.id.txt_mobile_number)
        txtMobileNumberHeader = findViewById(R.id.txt_mobile_number_header)
        edtMobileNumber = findViewById(R.id.edt_mobile)
        if (isMobileSelected) {
            lytMobileNumber.setVisibility(View.GONE)
            AnalyticsHelper.logGuestAction(
                this@CreateCompanyOutlet,
                AnalyticsHelper.TAP_SIGNUP_MOBILE_PASSWORD_CONTINUE
            )
        } else {
            lytMobileNumber.setVisibility(View.VISIBLE)
            AnalyticsHelper.logGuestAction(
                this@CreateCompanyOutlet,
                AnalyticsHelper.TAP_SIGNUP_EMAIL_PASSWORD_CONTINUE
            )
        }
        imgBackButton = findViewById(R.id.bt_back_btn)
        imgBackButton.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(this@CreateCompanyOutlet, BuyerLoginActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(newIntent)
        })
        textPaymentTermsAndConditions = findViewById(R.id.txt_create_account_terms)
        val wordtoSpan: Spannable =
            SpannableString(resources.getString(R.string.txt_accept_policies))
        val clickableSpanTerms: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val uri = Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_TERMS)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        val clickableSpanPrivacy: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val uri = Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_PRIVACY)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        if (Locale.getDefault().language == ZeemartAppConstants.Language.BAHASA_INDONESIA.langCode) {
            wordtoSpan.setSpan(clickableSpanTerms, 40, 62, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordtoSpan.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.chart_blue)),
                40,
                62,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            wordtoSpan.setSpan(clickableSpanPrivacy, 66, 83, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordtoSpan.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.chart_blue)),
                66,
                83,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (Locale.getDefault().language == ZeemartAppConstants.Language.CHINESE.langCode) {
            wordtoSpan.setSpan(clickableSpanTerms, 18, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordtoSpan.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.chart_blue)),
                18,
                23,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            wordtoSpan.setSpan(clickableSpanPrivacy, 26, 31, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordtoSpan.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.chart_blue)),
                26,
                31,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            wordtoSpan.setSpan(clickableSpanTerms, 51, 63, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordtoSpan.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.chart_blue)),
                51,
                63,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            wordtoSpan.setSpan(clickableSpanPrivacy, 68, 82, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordtoSpan.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.chart_blue)),
                68,
                82,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textPaymentTermsAndConditions.setText(wordtoSpan)
        textPaymentTermsAndConditions.setMovementMethod(LinkMovementMethod.getInstance())
        btnCreateAccount = findViewById(R.id.btn_create_account)
        txtCompleteDetails = findViewById(R.id.txt_complete_details)
        txtSubCompleteDetails = findViewById(R.id.txt_almost_details)
        txtCompanyNameHeader = findViewById(R.id.txt_company_name)
        edtCompanyName = findViewById(R.id.edt_company_name)
        txtCompanyEmailHeader = findViewById(R.id.txt_company_email)
        edtCompanyEmail = findViewById(R.id.edt_company_email)
        txtDeliveryAddressHeader = findViewById(R.id.txt_delivery_address)
        edtDeliveryAddress = findViewById(R.id.edt_delivery_address)
        if (!StringHelper.isStringNullOrEmpty(
                SharedPref.read(
                    SharedPref.USER_DELIVERY_ADDRESS,
                    ""
                )
            )
        ) {
            edtDeliveryAddress.setTextColor(resources.getColor(R.color.black))
            edtDeliveryAddress.setText(SharedPref.read(SharedPref.USER_DELIVERY_ADDRESS, ""))
        }
        edtDeliveryAddress.setOnClickListener(View.OnClickListener {
            if (!Places.isInitialized()) {
                Places.initialize(
                    this@CreateCompanyOutlet,
                    "AIzaSyA0ZnGsJJk1YpfL2g3NOpl7zHkQmr7CjW0"
                )
            }
            // return after the user has made a selection.
            val fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("SG").setTypeFilter(
                TypeFilter.ADDRESS
            ).build(this@CreateCompanyOutlet)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        })
        edtUnitNumber = findViewById(R.id.edt_unit_number)
        txtLocationNameHeader = findViewById(R.id.txt_location_name)
        edtLocationName = findViewById(R.id.edt_location_name)
        customLoadingViewWhite = findViewById(R.id.three_dot_loader)
        customLoadingViewWhite.setVisibility(View.GONE)
        imgTickAlcohol = findViewById(R.id.img_alocohol_tick)
        imgTickAlcohol.setVisibility(View.GONE)
        txtAlcohol = findViewById(R.id.txt_alcohol)
        imgTickHalal = findViewById(R.id.img_halal_tick)
        imgTickHalal.setVisibility(View.GONE)
        txtHalal = findViewById(R.id.txt_halal)
        lytAlcohol = findViewById(R.id.lyt_alcohol)
        lytAlcohol.setOnClickListener(View.OnClickListener {
            isHalalSelected = false
            txtHalal.setTextColor(resources.getColor(R.color.grey_medium))
            imgTickHalal.setVisibility(View.GONE)
            if (isAlcoholSelected) {
                isAlcoholSelected = false
                txtAlcohol.setTextColor(resources.getColor(R.color.grey_medium))
                imgTickAlcohol.setVisibility(View.GONE)
            } else {
                isAlcoholSelected = true
                txtAlcohol.setTextColor(resources.getColor(R.color.chart_blue))
                imgTickAlcohol.setVisibility(View.VISIBLE)
            }
        })
        lytHalal = findViewById(R.id.lyt_halal)
        lytHalal.setOnClickListener(View.OnClickListener {
            isAlcoholSelected = false
            txtAlcohol.setTextColor(resources.getColor(R.color.grey_medium))
            imgTickAlcohol.setVisibility(View.GONE)
            if (isHalalSelected) {
                isHalalSelected = false
                txtHalal.setTextColor(resources.getColor(R.color.grey_medium))
                imgTickHalal.setVisibility(View.GONE)
            } else {
                isHalalSelected = true
                txtHalal.setTextColor(resources.getColor(R.color.chart_blue))
                imgTickHalal.setVisibility(View.VISIBLE)
            }
        })
        lytCuisineType = findViewById(R.id.lyt_cousine_type)
        txtCuisineType = findViewById(R.id.txt_cousine_types)
        txtCuisineTypeHeader = findViewById(R.id.txt_cousine_type_header)
        lytCuisineType.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@CreateCompanyOutlet, SelectCuisineTypeActivity::class.java)
            if (lstSelectedCuisineType != null && lstSelectedCuisineType!!.size > 0) {
                intent.putExtra(
                    ZeemartAppConstants.CUISINE_TYPE_LIST,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(lstSelectedCuisineType)
                )
            }
            startActivityForResult(intent, CUISINE_TYPE_REQUEST_CODE)
        })
        btnCreateAccount.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                //hide keyboard
                val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                if (isValidate) {
                    customLoadingViewWhite.setVisibility(View.VISIBLE)
                    val createCompanyOutletRequest = CreateCompanyOutletRequest()
                    createCompanyOutletRequest.companyEmail = edtCompanyEmail.getText().toString()
                    createCompanyOutletRequest.companyName = edtCompanyName.getText().toString()
                    createCompanyOutletRequest.outletName = edtLocationName.getText().toString()
                    val address = CreateCompanyOutletRequest.Address()
                    address.line2 = edtUnitNumber.getText().toString()
                    if (!StringHelper.isStringNullOrEmpty(
                            SharedPref.read(
                                SharedPref.USER_DELIVERY_NAME,
                                ""
                            )
                        )
                    ) {
                        address.line1 = SharedPref.read(SharedPref.USER_DELIVERY_NAME, "")
                        if (!StringHelper.isStringNullOrEmpty(
                                SharedPref.read(
                                    SharedPref.USER_PIN_CODE,
                                    ""
                                )
                            )
                        ) address.postal = SharedPref.read(SharedPref.USER_PIN_CODE, "")
                    }
                    createCompanyOutletRequest.address = address
                    val tags: MutableList<String> = ArrayList()
                    if (isHalalSelected) {
                        tags.add("Halal-certified")
                    }
                    if (isAlcoholSelected) {
                        tags.add("Serves alcohol")
                    }
                    createCompanyOutletRequest.otherCuisineFeatures = tags
                    createCompanyOutletRequest.mobileNumber =
                        ZeemartAppConstants.SINGAPORE_COUNTRY_CODE + edtMobileNumber.getText()
                            .toString()
                    if (lstSelectedCuisineType != null) createCompanyOutletRequest.cuisineType =
                        lstSelectedCuisineType
                    if (isMobileSelected) {
                        AnalyticsHelper.logGuestAction(
                            this@CreateCompanyOutlet,
                            AnalyticsHelper.TAP_SIGNUP_MOBILE_COMPLETE_CREATE_ACC,
                            createCompanyOutletRequest
                        )
                    } else {
                        AnalyticsHelper.logGuestAction(
                            this@CreateCompanyOutlet,
                            AnalyticsHelper.TAP_SIGNUP_EMAIL_COMPLETE_CREATE_ACC,
                            createCompanyOutletRequest
                        )
                    }
                    LoginApi.createCompanyDetails(
                        this@CreateCompanyOutlet,
                        userID,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(createCompanyOutletRequest),
                        object : GetRequestStatusResponseListener {
                            override fun onSuccessResponse(status: String?) {
                                val createCompanyOutletResponse =
                                    fromJson(status, CreateCompanyOutletResponse::class.java)
                                if (createCompanyOutletResponse != null && createCompanyOutletResponse.status == ServiceConstant.STATUS_CODE_200_OK) {
                                    val buyerCredentials = BuyerCredentials(userName!!, userPassword!!)
                                    LoginApi.loginBuyer(
                                        this@CreateCompanyOutlet,
                                        buyerCredentials,
                                        object : LoginBuyerListener {
                                            override fun onSuccessResponse(buyerDetails: BuyerDetails) {
                                                customLoadingViewWhite.setVisibility(View.GONE)
                                                AnalyticsHelper.logAction(
                                                    this@CreateCompanyOutlet,
                                                    AnalyticsHelper.LOGIN_SUCCESS,
                                                    buyerDetails
                                                )
                                                try {
                                                    SharedPref.write(
                                                        SharedPref.USERNAME,
                                                        buyerCredentials.zeemartId
                                                    )
                                                    SharedPref.write(
                                                        SharedPref.PASSWORD_ENCRYPTED,
                                                        buyerCredentials.password
                                                    )
                                                    SharedPref.write(
                                                        SharedPref.ACCESS_TOKEN,
                                                        buyerDetails.mudra
                                                    )
                                                    SharedPref.write(
                                                        SharedPref.BUYER_DETAIL,
                                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                            buyerDetails
                                                        )
                                                    )
                                                    SharedPref.writeBool(
                                                        SharedPref.DISPLAY_USE_FILTER_FOR_INVOICED_POPUP,
                                                        true
                                                    )
                                                    if (buyerDetails != null) {
                                                        val dialogSuccess =
                                                            alertPaymentDialogSmallSuccess(
                                                                this@CreateCompanyOutlet, getString(
                                                                    R.string.txt_done_with_esclamation
                                                                ), getString(
                                                                    R.string.txt_account_created
                                                                )
                                                            )
                                                        dialogSuccess.show()
                                                        Handler().postDelayed({
                                                            dialogSuccess.dismiss()
                                                            SharedPref.writeBool(
                                                                SharedPref.DISPLAY_SHOW_CASE_FOR_NEW_ORDER,
                                                                true
                                                            )
                                                            startActivity(
                                                                Intent(
                                                                    this@CreateCompanyOutlet,
                                                                    BaseNavigationActivity::class.java
                                                                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                            )
                                                            finish()
                                                        }, 2000)
                                                    } else {
                                                        val dialogFailure =
                                                            alertPaymentDialogSmallFailure(
                                                                this@CreateCompanyOutlet,
                                                                getString(
                                                                    R.string.txt_sorry
                                                                ),
                                                                getString(R.string.txt_please_try_again)
                                                            )
                                                        dialogFailure.show()
                                                        Handler().postDelayed(
                                                            { dialogFailure.dismiss() },
                                                            2000
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    SharedPref.write(SharedPref.USERNAME, null)
                                                    SharedPref.write(
                                                        SharedPref.PASSWORD_ENCRYPTED,
                                                        null
                                                    )
                                                    SharedPref.write(SharedPref.ACCESS_TOKEN, null)
                                                    getToastRed(getText(R.string.login_log_in_unsuccessful).toString())
                                                }
                                                if (buyerDetails != null) if (buyerDetails.language == ZeemartAppConstants.Language.BAHASA_INDONESIA.locLangCode) {
                                                    ChangeLanguageLocale(
                                                        this@CreateCompanyOutlet,
                                                        Locale(
                                                            SharedPref.read(
                                                                SharedPref.SELECTED_LANGUAGE,
                                                                ZeemartAppConstants.Language.BAHASA_INDONESIA.langCode
                                                            )
                                                        )
                                                    )
                                                } else if (buyerDetails.language == ZeemartAppConstants.Language.VIETNAMESE.locLangCode) {
                                                    ChangeLanguageLocale(
                                                        this@CreateCompanyOutlet,
                                                        Locale(
                                                            SharedPref.read(
                                                                SharedPref.SELECTED_LANGUAGE,
                                                                ZeemartAppConstants.Language.VIETNAMESE.langCode
                                                            )
                                                        )
                                                    )
                                                } else if (buyerDetails.language == ZeemartAppConstants.Language.BAHASA_MELAYU.locLangCode) {
                                                    ChangeLanguageLocale(
                                                        this@CreateCompanyOutlet,
                                                        Locale(
                                                            SharedPref.read(
                                                                SharedPref.SELECTED_LANGUAGE,
                                                                ZeemartAppConstants.Language.BAHASA_MELAYU.langCode
                                                            )
                                                        )
                                                    )
                                                } else if (buyerDetails.language == ZeemartAppConstants.Language.CHINESE.locLangCode) {
                                                    ChangeLanguageLocale(
                                                        this@CreateCompanyOutlet,
                                                        Locale(
                                                            SharedPref.read(
                                                                SharedPref.SELECTED_LANGUAGE,
                                                                ZeemartAppConstants.Language.CHINESE.langCode
                                                            )
                                                        )
                                                    )
                                                } else if (buyerDetails.language == ZeemartAppConstants.Language.HINDI.locLangCode) {
                                                    ChangeLanguageLocale(
                                                        this@CreateCompanyOutlet,
                                                        Locale(
                                                            SharedPref.read(
                                                                SharedPref.SELECTED_LANGUAGE,
                                                                ZeemartAppConstants.Language.HINDI.langCode
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    ChangeLanguageLocale(
                                                        this@CreateCompanyOutlet,
                                                        Locale(
                                                            SharedPref.read(
                                                                SharedPref.SELECTED_LANGUAGE,
                                                                ZeemartAppConstants.Language.ENGLISH.langCode
                                                            )
                                                        )
                                                    )
                                                }
                                            }

                                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                                customLoadingViewWhite.setVisibility(View.GONE)
                                                val dialogFailure = alertPaymentDialogSmallFailure(
                                                    this@CreateCompanyOutlet, getString(
                                                        R.string.txt_sorry
                                                    ), getString(R.string.txt_please_try_again)
                                                )
                                                dialogFailure.show()
                                                Handler().postDelayed(
                                                    { dialogFailure.dismiss() },
                                                    2000
                                                )
                                                SharedPref.write(SharedPref.USERNAME, null)
                                                SharedPref.write(
                                                    SharedPref.PASSWORD_ENCRYPTED,
                                                    null
                                                )
                                                SharedPref.write(SharedPref.ACCESS_TOKEN, null)
                                            }
                                        })
                                } else {
                                    customLoadingViewWhite.setVisibility(View.GONE)
                                    if (createCompanyOutletResponse != null) if (createCompanyOutletResponse.message?.contains(
                                            "Company already exists"
                                        )!!
                                    ) {
                                        displayErrorMessageDialog(
                                            this@CreateCompanyOutlet,
                                            "Company already exists",
                                            "Please use another name or contact Zeemart at help@zeemart.asia for assistance"
                                        )
                                    } else {
                                        displayErrorMessageDialog(
                                            this@CreateCompanyOutlet,
                                            "SignUp",
                                            createCompanyOutletResponse.message
                                        )
                                    }
                                }
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                customLoadingViewWhite.setVisibility(View.GONE)
                                val errorMessage = error?.errorMessage
                                if (errorMessage?.contains("Company already exists")!!) {
                                    displayErrorMessageDialog(
                                        this@CreateCompanyOutlet,
                                        "Company already exists",
                                        "Please use another name or contact Zeemart at help@zeemart.asia for assistance"
                                    )
                                } else {
                                    displayErrorMessageDialog(
                                        this@CreateCompanyOutlet,
                                        "SignUp",
                                        errorMessage
                                    )
                                }
                            }
                        })
                }
            }
        })
        setFont()
    }

    private val isValidate: Boolean
        private get() {
            var isValidate = true
            if (StringHelper.isStringNullOrEmpty(edtLocationName!!.text.toString())) {
                edtLocationName!!.error = "Please fill this field"
                isValidate = false
            }
            if (StringHelper.isStringNullOrEmpty(edtCompanyName!!.text.toString())) {
                edtCompanyName!!.error = "Please fill this field"
                isValidate = false
            }
            if (StringHelper.isStringNullOrEmpty(edtCompanyEmail!!.text.toString())) {
                edtCompanyEmail!!.error = "Please fill this field"
                isValidate = false
            }
            if (!StringHelper.isStringNullOrEmpty(edtCompanyEmail!!.text.toString()) && !Patterns.EMAIL_ADDRESS.matcher(
                    edtCompanyEmail!!.text.toString()
                ).matches()
            ) {
                edtCompanyEmail!!.error = "invalid email"
                isValidate = false
            }
            if (StringHelper.isStringNullOrEmpty(edtDeliveryAddress!!.text.toString())) {
                edtDeliveryAddress!!.error = "Please fill this field"
                isValidate = false
            }
            if (!isMobileSelected) {
                if (StringHelper.isStringNullOrEmpty(edtMobileNumber!!.text.toString())) {
                    edtMobileNumber!!.error = "Please fill this field"
                    isValidate = false
                }
                if (!StringHelper.isStringNullOrEmpty(edtMobileNumber!!.text.toString()) && edtMobileNumber!!.text.length != 8) {
                    edtMobileNumber!!.error = "invalid mobile number"
                    isValidate = false
                }
            }
            return isValidate
        }

    private fun setFont() {
        setTypefaceView(btnCreateAccount, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            textPaymentTermsAndConditions,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtCompleteDetails, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(edtCompanyName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtCompanyEmail, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtDeliveryAddress, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtUnitNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtLocationName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtSubCompleteDetails, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCompanyNameHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(txtCompanyEmailHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(txtDeliveryAddressHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(txtLocationNameHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(txtCuisineTypeHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(txtAlcohol, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtHalal, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCuisineType, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtMobileNumberHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(txtMobileNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtMobileNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            place = Autocomplete.getPlaceFromIntent(data)
            edtDeliveryAddress!!.error = null
            SharedPref.write(SharedPref.USER_DELIVERY_NAME, place!!.getName())
            SharedPref.write(SharedPref.USER_DELIVERY_ADDRESS, place!!.getAddress())
            edtDeliveryAddress!!.text = place!!.getAddress()
            edtDeliveryAddress!!.setTextColor(resources.getColor(R.color.black))
            try {
                val addresses: List<Address>
                val geocoder = Geocoder(this@CreateCompanyOutlet, Locale.getDefault())
                try {
                    addresses = geocoder.getFromLocation(
                        Objects.requireNonNull(place!!.getLatLng()).latitude,
                        place!!.getLatLng().longitude,
                        1
                    )!! // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    postalCode = addresses[0].postalCode
                    SharedPref.write(SharedPref.USER_PIN_CODE, postalCode)
                    Log.e("AddressPostal: ", "" + postalCode)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                //setMarker(latLng);
            }
        }
        if (resultCode == SelectCuisineTypeActivity.Companion.CUISINE_TYPE_RESULT_CODE) {
            val bundle = data!!.extras
            if (bundle != null) if (bundle.containsKey(ZeemartAppConstants.CUISINE_TYPE_LIST)) {
                lstSelectedCuisineType = fromJsonList(
                    bundle.getString(ZeemartAppConstants.CUISINE_TYPE_LIST),
                    OutletFuturesModel.CuisineType::class.java,
                    object : TypeToken<List<OutletFuturesModel.CuisineType?>?>() {}.type
                )
                if (lstSelectedCuisineType != null && lstSelectedCuisineType!!.size > 0) txtCuisineType!!.text =
                    CommonMethods.getCommaSeperatedCuisineListList(lstSelectedCuisineType)
            }
        }
    }

    companion object {
        var AUTOCOMPLETE_REQUEST_CODE = 201
        var CUISINE_TYPE_REQUEST_CODE = 202
    }
}