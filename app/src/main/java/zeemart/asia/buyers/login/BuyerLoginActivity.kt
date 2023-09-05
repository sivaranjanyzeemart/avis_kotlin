package zeemart.asia.buyers.login

import android.content.*
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.*
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONException
import org.json.JSONObject
import zeemart.asia.buyers.BuildConfig
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.ChangeLanguageLocale
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.activities.UpdateAppDialogActivity
import zeemart.asia.buyers.constants.MarketConstants
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.RejectedInvoiceActivity
import zeemart.asia.buyers.models.BuyerCredentials
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.modelsimport.BuyerOnBoardingCredentialsRequest
import zeemart.asia.buyers.modelsimport.BuyerOnBoardingCredentialsResponse
import zeemart.asia.buyers.network.LoginApi
import zeemart.asia.buyers.network.LoginApi.LoginBuyerListener
import zeemart.asia.buyers.network.LoginApi.SignUpBuyerListener
import zeemart.asia.buyers.network.PushNotificationApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.notifications.NotificationAnnouncementDetails
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.util.*

/**
 * Created by RajPrudhviMarella on 22/sep/2020.
 */
class BuyerLoginActivity : AppCompatActivity() {
    private lateinit var imgBackButton: ImageView
    private var txtHeader: TextView? = null
    private var edtEmail: EditText? = null
    private var edtPassword: EditText? = null
    private lateinit var btnLogin: Button
    private lateinit var txtForgotPassword: TextView
    private lateinit var txtSignUpForNewAccount: TextView
    private var isSignUpSelected = false
    private lateinit var lytNames: LinearLayout
    private var edtFirstName: EditText? = null
    private var edtLastName: EditText? = null
    private var textInputLayoutPassword: TextInputLayout? = null
    private lateinit var lytCountry: LinearLayout
    private var isMobileSelected = false
    private var lytMobileNumber: LinearLayout? = null
    private var txtCountryCode: TextView? = null
    private var edtMobileNumber: EditText? = null
    private var buyerCredentials: BuyerCredentials? = null
    private var mRemoteConfig: FirebaseRemoteConfig? = null
    private lateinit var threeDotLoader: CustomLoadingViewWhite
    private var txtSingapore: TextView? = null
    private var viewUnderLineEditEmail: View? = null
    private var viewUnderLineEditMobile: View? = null
    private var calledFrom: String? = null
    private var lytError: LinearLayout? = null
    private var txtError: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                isSignUpSelected = bundle.getString(ZeemartAppConstants.CALLED_FROM) == ZeemartAppConstants.CALLED_FOR_SIGN_UP
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        txtSingapore = findViewById(R.id.txt_singapore)
        threeDotLoader = findViewById(R.id.three_dot_loader)
        threeDotLoader.setVisibility(View.GONE)
        imgBackButton = findViewById(R.id.btn_close_login)
        imgBackButton.setOnClickListener(View.OnClickListener { finish() })
        txtHeader = findViewById(R.id.txt_header)
        edtEmail = findViewById(R.id.email_edit)
        edtPassword = findViewById(R.id.password_edit)
        btnLogin = findViewById(R.id.login_btn)
        txtForgotPassword = findViewById(R.id.txt_forgot_password)
        txtSignUpForNewAccount = findViewById(R.id.txt_sign_up_new_account)
        if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
            txtSignUpForNewAccount.setVisibility(View.VISIBLE)
            imgBackButton.setVisibility(View.VISIBLE)
        } else if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
            if (!isSingaporeBuyer) {
                txtSignUpForNewAccount.setVisibility(View.GONE)
                imgBackButton.setVisibility(View.GONE)
            } else {
                txtSignUpForNewAccount.setVisibility(View.VISIBLE)
                imgBackButton.setVisibility(View.VISIBLE)
            }
        } else {
            if (!isSingaporeBuyer) {
                txtSignUpForNewAccount.setVisibility(View.GONE)
                imgBackButton.setVisibility(View.GONE)
            } else {
                txtSignUpForNewAccount.setVisibility(View.VISIBLE)
                imgBackButton.setVisibility(View.VISIBLE)
            }
        }
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_LOGOUT) {
            imgBackButton.setVisibility(View.GONE)
        }
        lytNames = findViewById(R.id.lyt_names)
        lytNames.setVisibility(View.GONE)
        edtFirstName = findViewById(R.id.txt_first_name)
        edtLastName = findViewById(R.id.txt_last_name)
        textInputLayoutPassword = findViewById(R.id.txt_input_layout_password)
        lytCountry = findViewById(R.id.lyt_country)
        lytCountry.setVisibility(View.GONE)
        lytMobileNumber = findViewById(R.id.lyt_mobile_number)
        txtCountryCode = findViewById(R.id.txt_mobile_number)
        edtMobileNumber = findViewById(R.id.edt_mobile)
        viewUnderLineEditEmail = findViewById(R.id.under_line_edt_email)
        viewUnderLineEditMobile = findViewById(R.id.under_line_edt_mobile)
        lytError = findViewById(R.id.lyt_error)
        txtError = findViewById(R.id.txt_error)
        txtSignUpForNewAccount.setOnClickListener(View.OnClickListener {
            if (isSignUpSelected) {
                isSignUpSelected = false
                setLoginActivated()
            } else {
                isSignUpSelected = true
                setSignUpActivated()
            }
        })
        txtForgotPassword.setOnClickListener(View.OnClickListener {
            if (isSignUpSelected) {
                isMobileSelected = if (isMobileSelected) {
                    false
                } else {
                    true
                }
                setSignUpActivated()
            } else {
                startActivity(Intent(this@BuyerLoginActivity, ResetPasswordActivity::class.java))
            }
        })
        btnLogin.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (isMobileSelected) {
                    AnalyticsHelper.logGuestAction(
                        this@BuyerLoginActivity,
                        AnalyticsHelper.TAP_SIGNUP_EMAIL_USE_MOBILE
                    )
                } else {
                    AnalyticsHelper.logGuestAction(
                        this@BuyerLoginActivity,
                        AnalyticsHelper.TAP_SIGNUP_EMAIL_USE_EMAIL
                    )
                }
                //hide keyboard
                val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                if (isSignUpSelected) {
                    if (isValidSignUpDetails) onSignUpValidationSucceeded()
                } else {
                    if (isValidSigninDetails) onLogInValidationSucceeded()
                }
            }
        })
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mFCMRefreshTokenReceived,
            IntentFilter(NotificationConstants.REFRESH_TOKEN_RECEIVED)
        )

//        ZeemartBuyerApp.ChangeLanguageLocale(BuyerLoginActivity.this, new Locale(SharedPref.read(SharedPref.SELECTED_LANGUAGE, ZeemartAppConstants.Market.getThis().getDefaultLanguage().getLangCode())));
        setFont()
        if (isSignUpSelected) {
            setSignUpActivated()
        } else {
            setLoginActivated()
        }
    }

    //check if the network country code is "sg"
    private val isSingaporeBuyer: Boolean
        private get() {
            var isSingaporeSelected = false
            val telMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (telMgr != null) {
                val simState = telMgr.simState
                when (simState) {
                    TelephonyManager.SIM_STATE_ABSENT -> isSingaporeSelected = false
                    TelephonyManager.SIM_STATE_READY -> {
                        val tm = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                        if (tm != null) {
                            val countryCodeValue = tm.networkCountryIso
                            val isAeroplaneModeOn = Settings.System.getInt(
                                contentResolver,
                                Settings.Global.AIRPLANE_MODE_ON, 0
                            ) != 0
                            //check if the network country code is "sg"
                            if (countryCodeValue.equals(
                                    ZeemartAppConstants.Market.SINGAPORE.countryCode,
                                    ignoreCase = true
                                ) && !isAeroplaneModeOn
                            ) {
                                isSingaporeSelected = true
                            }
                        }
                    }
                }
            } else {
                isSingaporeSelected = false
            }
            return isSingaporeSelected
        }

    private fun setLoginActivated() {
        edtEmail!!.error = null
        edtEmail!!.setText("")
        lytMobileNumber!!.visibility = View.GONE
        lytCountry.visibility = View.GONE
        textInputLayoutPassword!!.visibility = View.VISIBLE
        lytNames.visibility = View.GONE
        txtForgotPassword.text = resources.getString(R.string.txt_forgot_password)
        txtForgotPassword.setTextColor(resources.getColor(R.color.chart_blue))
        txtHeader!!.text = resources.getString(R.string.txt_welcome_back)
        btnLogin.text = resources.getString(R.string.txt_login)
        edtEmail!!.visibility = View.VISIBLE
        viewUnderLineEditEmail!!.visibility = View.VISIBLE
        val color = resources.getColor(R.color.chart_blue)
        val string = getString(R.string.txt_new_account_signup, color)
        txtSignUpForNewAccount.text = Html.fromHtml(string)
        edtEmail!!.hint = resources.getString(R.string.login_email_add)
    }

    private fun setSignUpActivated() {
        txtForgotPassword.setTextColor(resources.getColor(R.color.dark_grey))
        edtEmail!!.error = null
        edtEmail!!.setText("")
        edtMobileNumber!!.setText("")
        lytMobileNumber!!.visibility = View.GONE
        lytCountry.visibility = View.GONE
        textInputLayoutPassword!!.visibility = View.GONE
        lytNames.visibility = View.VISIBLE
        edtEmail!!.hint = resources.getString(R.string.txt_email)
        edtMobileNumber!!.hint = resources.getString(R.string.txt_mobile_number)
        if (isMobileSelected) {
            lytError!!.visibility = View.GONE
            lytMobileNumber!!.visibility = View.VISIBLE
            edtEmail!!.visibility = View.GONE
            viewUnderLineEditEmail!!.visibility = View.GONE
            val color = resources.getColor(R.color.chart_blue)
            val string = getString(R.string.txt_no_mobile_num, color)
            txtForgotPassword.text = Html.fromHtml(string)
        } else {
            lytError!!.visibility = View.GONE
            val color = resources.getColor(R.color.chart_blue)
            val string = getString(R.string.txt_no_email, color)
            txtForgotPassword.text = Html.fromHtml(string)
            lytMobileNumber!!.visibility = View.GONE
            edtEmail!!.visibility = View.VISIBLE
            viewUnderLineEditEmail!!.visibility = View.VISIBLE
        }
        txtHeader!!.text = resources.getString(R.string.txt_signup)
        btnLogin.text = resources.getString(R.string.txt_signup)
        val color = resources.getColor(R.color.chart_blue)
        val string = getString(R.string.txt_already_have_an_account, color)
        txtSignUpForNewAccount.text = Html.fromHtml(string)
    }

    private fun setFont() {
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(btnLogin, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtSignUpForNewAccount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(edtEmail, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtForgotPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtFirstName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtMobileNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCountryCode, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtLastName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtSingapore, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtError, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    private fun onSignUpValidationSucceeded() {
        val buyerOnBoardingCredentialsRequest = BuyerOnBoardingCredentialsRequest()
        buyerOnBoardingCredentialsRequest.country = "SINGAPORE"
        buyerOnBoardingCredentialsRequest.firstName = edtFirstName!!.text.toString()
        buyerOnBoardingCredentialsRequest.lastName = edtLastName!!.text.toString()
        if (isMobileSelected) {
            buyerOnBoardingCredentialsRequest.phone =
                ZeemartAppConstants.SINGAPORE_COUNTRY_CODE + "" + edtMobileNumber!!.text.toString()
        } else {
            buyerOnBoardingCredentialsRequest.email = edtEmail!!.text.toString()
        }
        showProgress()
        btnLogin!!.isClickable = false
        LoginApi.signUpBuyer(this, buyerOnBoardingCredentialsRequest, object : SignUpBuyerListener {
            override fun onSuccessResponse(buyerDetails: BuyerOnBoardingCredentialsResponse?) {
                hideProgress()
                btnLogin!!.isClickable = true
                if (buyerDetails != null) {
                    if (buyerDetails.status == ServiceConstant.STATUS_CODE_200_OK) {
                        val intent = Intent(this@BuyerLoginActivity, VerifyCodeActivity::class.java)
                        intent.putExtra(
                            ZeemartAppConstants.CALLED_FROM,
                            ZeemartAppConstants.CALLED_FOR_SIGN_UP
                        )
                        intent.putExtra(
                            ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED,
                            isMobileSelected
                        )
                        if (buyerDetails != null && buyerDetails.data != null && !StringHelper.isStringNullOrEmpty(
                                buyerDetails.data!!.userId
                            )
                        ) intent.putExtra(ZeemartAppConstants.USER_ID, buyerDetails.data!!.userId)
                        if (isMobileSelected) {
                            intent.putExtra(
                                ZeemartAppConstants.RESET_PWD_ZEEMART_ID,
                                ZeemartAppConstants.SINGAPORE_COUNTRY_CODE + edtMobileNumber!!.text.toString()
                            )
                        } else {
                            intent.putExtra(
                                ZeemartAppConstants.RESET_PWD_ZEEMART_ID,
                                edtEmail!!.text.toString()
                            )
                        }
                        startActivity(intent)
                    } else {
                        if (!StringHelper.isStringNullOrEmpty(buyerDetails.message)) {
                            lytError!!.visibility = View.VISIBLE
                            if (isMobileSelected) {
                                txtError!!.text =
                                    resources.getString(R.string.txt_mobile_number_already_registered)
                                viewUnderLineEditMobile!!.setBackgroundColor(resources.getColor(R.color.pinky_red))
                            } else {
                                txtError!!.text =
                                    resources.getString(R.string.txt_email_id_already_registered)
                                viewUnderLineEditEmail!!.setBackgroundColor(resources.getColor(R.color.pinky_red))
                            }
                        }
                    }
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {}
        })
    }

    private fun onLogInValidationSucceeded() {
        var userName = ""
        val isDigits = TextUtils.isDigitsOnly(edtEmail!!.text.toString())
        userName = if (isDigits) {
            if (edtEmail!!.text.toString().contains(ZeemartAppConstants.SINGAPORE_COUNTRY_CODE)) {
                edtEmail!!.text.toString()
            } else {
                ZeemartAppConstants.SINGAPORE_COUNTRY_CODE + edtEmail!!.text.toString()
            }
        } else {
            edtEmail!!.text.toString()
        }
        buyerCredentials = BuyerCredentials(userName, edtPassword!!.text.toString())
        if (buyerCredentials!!.checkNullEmpty()) {
            getToastRed(getText(R.string.login_empty_username_password).toString())
        } else {
            showProgress()
            btnLogin.isClickable = false
            LoginApi.loginBuyer(
                this,
                buyerCredentials!!,
                object : LoginBuyerListener {
                    override fun onSuccessResponse(buyerDetails: BuyerDetails) {
                        AnalyticsHelper.logAction(
                            this@BuyerLoginActivity,
                            AnalyticsHelper.LOGIN_SUCCESS,
                            buyerDetails
                        )
                        hideProgress()
                        btnLogin.isClickable = true
                        try {
                            SharedPref.write(SharedPref.USERNAME, buyerCredentials!!.zeemartId)
                            SharedPref.write(
                                SharedPref.PASSWORD_ENCRYPTED,
                                buyerCredentials!!.password
                            )
                            SharedPref.write(SharedPref.ACCESS_TOKEN, buyerDetails.mudra)
                            SharedPref.write(
                                SharedPref.BUYER_DETAIL,
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerDetails)
                            )
                            SharedPref.writeBool(
                                SharedPref.DISPLAY_USE_FILTER_FOR_INVOICED_POPUP,
                                true
                            )
                            startActivity(
                                Intent(
                                    this@BuyerLoginActivity,
                                    BaseNavigationActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            )
                            finish()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            SharedPref.write(SharedPref.USERNAME, null)
                            SharedPref.write(SharedPref.PASSWORD_ENCRYPTED, null)
                            SharedPref.write(SharedPref.ACCESS_TOKEN, null)
                            getToastRed(getText(R.string.login_log_in_unsuccessful).toString())
                        }
                        if (buyerDetails != null) if (buyerDetails.language == ZeemartAppConstants.Language.BAHASA_INDONESIA.locLangCode) {
                            ChangeLanguageLocale(
                                this@BuyerLoginActivity,
                                Locale(
                                    SharedPref.read(
                                        SharedPref.SELECTED_LANGUAGE,
                                        ZeemartAppConstants.Language.BAHASA_INDONESIA.langCode
                                    )
                                )
                            )
                            /*} else if (buyerDetails.getLanguage().equals(ZeemartAppConstants.Language.VIETNAMESE.getLocLangCode())) {
                            ZeemartBuyerApp.ChangeLanguageLocale(BuyerLoginActivity.this, new Locale(SharedPref.read(SharedPref.SELECTED_LANGUAGE, ZeemartAppConstants.Language.VIETNAMESE.getLangCode())));
                        } else if (buyerDetails.getLanguage().equals(ZeemartAppConstants.Language.BAHASA_MELAYU.getLocLangCode())) {
                            ZeemartBuyerApp.ChangeLanguageLocale(BuyerLoginActivity.this, new Locale(SharedPref.read(SharedPref.SELECTED_LANGUAGE, ZeemartAppConstants.Language.BAHASA_MELAYU.getLangCode())));*/
                        } else if (buyerDetails.language == ZeemartAppConstants.Language.CHINESE.locLangCode) {
                            ChangeLanguageLocale(
                                this@BuyerLoginActivity,
                                Locale(
                                    SharedPref.read(
                                        SharedPref.SELECTED_LANGUAGE,
                                        ZeemartAppConstants.Language.CHINESE.langCode
                                    )
                                )
                            )
                            /*  } else if (buyerDetails.getLanguage().equals(ZeemartAppConstants.Language.HINDI.getLocLangCode())) {
                            ZeemartBuyerApp.ChangeLanguageLocale(BuyerLoginActivity.this, new Locale(SharedPref.read(SharedPref.SELECTED_LANGUAGE, ZeemartAppConstants.Language.HINDI.getLangCode())));*/
                        } else {
                            ChangeLanguageLocale(
                                this@BuyerLoginActivity,
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
                        hideProgress()
                        val errorMessage = error?.errorMessage
                        val errorType = error?.errorType
                        if (errorType == ServiceConstant.RETRY_API_CALL__500_MESSAGE) {
                            //call 500
                        } else if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                            getToastRed(errorMessage)
                        } else {
                            getToastRed(getString(R.string.login_invalid_username_password))
                        }
                        btnLogin!!.isClickable = true
                        SharedPref.write(SharedPref.USERNAME, null)
                        SharedPref.write(SharedPref.PASSWORD_ENCRYPTED, null)
                        SharedPref.write(SharedPref.ACCESS_TOKEN, null)
                    }
                })
        }
    }

    private val isValidSigninDetails: Boolean
        private get() {
            var isValid = true
            if (StringHelper.isStringNullOrEmpty(edtEmail!!.text.toString())) {
                edtEmail!!.error = "please fill details"
                isValid = false
            } else {
                val isDigits = TextUtils.isDigitsOnly(edtEmail!!.text.toString())
                if (isDigits) {
                    if (edtEmail!!.text.length != 8) {
                        edtEmail!!.error = "invalid mobile number"
                        isValid = false
                    }
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail!!.text.toString()).matches()) {
                        edtEmail!!.error = "invalid email"
                        isValid = false
                    }
                }
            }
            if (StringHelper.isStringNullOrEmpty(edtPassword!!.text.toString())) {
                edtPassword!!.error = "please fill details"
                isValid = false
            }
            return isValid
        }
    private val isValidSignUpDetails: Boolean
        private get() {
            var isValid = true
            if (isMobileSelected) {
                val isDigits = TextUtils.isDigitsOnly(edtMobileNumber!!.text.toString())
                if (StringHelper.isStringNullOrEmpty(edtMobileNumber!!.text.toString())) {
                    edtMobileNumber!!.error = "please fill details"
                    isValid = false
                } else {
                    if (!isDigits) {
                        edtMobileNumber!!.error = "invalid mobile number"
                        isValid = false
                    }
                }
            } else {
                if (StringHelper.isStringNullOrEmpty(edtEmail!!.text.toString())) {
                    edtEmail!!.error = "please fill details"
                    isValid = false
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail!!.text.toString()).matches()) {
                        edtEmail!!.error = "invalid email"
                        isValid = false
                    }
                }
            }
            if (StringHelper.isStringNullOrEmpty(edtFirstName!!.text.toString())) {
                edtFirstName!!.error = "please fill details"
                isValid = false
            }
            if (StringHelper.isStringNullOrEmpty(edtLastName!!.text.toString())) {
                edtLastName!!.error = "please fill details"
                isValid = false
            }
            return isValid
        }

    /**
     * register this receiver to know everytime
     * a notification with an orderId comes so that we can refresh the notification badge and the notification fragment
     */
    private val mFCMRefreshTokenReceived: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //call save device token on zeemart server
            //call the register first time user on notification server API
            if (StringHelper.isStringNullOrEmpty(
                    SharedPref.read(
                        SharedPref.PASSWORD_ENCRYPTED,
                        ""
                    )
                )
            ) {
                PushNotificationApi.registerDeviceUserNotLoggedIn(
                    this@BuyerLoginActivity,
                    SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")!!
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFCMRefreshTokenReceived)
    }

    override fun onResume() {
        super.onResume()
        Log.d("EXTRA DATA", intent.extras.toString() + "******")
        if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
            edtPassword!!.setText("!123456Zm")
        }
        val password = SharedPref.read(SharedPref.PASSWORD_ENCRYPTED, null)
        if (!StringHelper.isStringNullOrEmpty(password)) {
            /**
             * called when a notification is clicked from the notification tray
             * and the app is in the background
             */
            if (intent.extras != null) {
                val bundle = intent.extras
                if (bundle!!.containsKey(NotificationConstants.URI_KEY)) {
                    var type: String? = null
                    var orderId: String? = null
                    var invoiceId: String? = null
                    var announcementId: String? = null
                    try {
                        val jsonObjURI = JSONObject(bundle.getString(NotificationConstants.URI_KEY))
                        type = jsonObjURI.getString("type")
                        val jsonObjectParameters = jsonObjURI.getJSONObject("parameters")
                        if (type == NotificationConstants.INVOICE_DETAILS) {
                            invoiceId = jsonObjectParameters.getString("invoiceId")
                        } else if (type == NotificationConstants.ANNOUNCEMENTS) {
                            announcementId = jsonObjectParameters.getString("announcementId")
                        } else {
                            orderId = jsonObjectParameters.getString("orderId")
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    if (type != null && type == NotificationConstants.ORDER_DETAILS && orderId != null && bundle.containsKey(
                            NotificationConstants.OUTLETID_KEY
                        )
                    ) {
                        val intent = Intent(this, OrderDetailsActivity::class.java)
                        intent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
                        intent.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            bundle.getString(NotificationConstants.OUTLETID_KEY)
                        )
                        val stackBuilder = TaskStackBuilder.create(this)
                        stackBuilder.addNextIntentWithParentStack(intent)
                        stackBuilder.startActivities()
                    } else if (type != null && type == NotificationConstants.INVOICE_DETAILS && invoiceId != null && bundle.containsKey(
                            NotificationConstants.OUTLETID_KEY
                        )
                    ) {
                        val intent = Intent(this, RejectedInvoiceActivity::class.java)
                        intent.putExtra(ZeemartAppConstants.INVOICE_ID, invoiceId)
                        intent.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            bundle.getString(NotificationConstants.OUTLETID_KEY)
                        )
                        val stackBuilder = TaskStackBuilder.create(this)
                        stackBuilder.addNextIntentWithParentStack(intent)
                        stackBuilder.startActivities()
                    } else if (type != null && type == NotificationConstants.ANNOUNCEMENTS && announcementId != null && bundle.containsKey(
                            NotificationConstants.OUTLETID_KEY
                        )
                    ) {
                        val intent = Intent(this, NotificationAnnouncementDetails::class.java)
                        intent.putExtra(ZeemartAppConstants.ANNOUNCEMENT_ID, announcementId)
                        intent.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            bundle.getString(NotificationConstants.OUTLETID_KEY)
                        )
                        intent.putExtra(
                            ZeemartAppConstants.CALLED_FROM,
                            NotificationConstants.CALLED_FROM_ANNOUNCEMENT_DETAILS
                        )
                        val stackBuilder = TaskStackBuilder.create(this)
                        stackBuilder.addNextIntentWithParentStack(intent)
                        stackBuilder.startActivities()
                    }
                } else {
                    startActivity(
                        Intent(
                            this,
                            BaseNavigationActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                }
                finish()
            } else {
                startActivity(
                    Intent(
                        this,
                        BaseNavigationActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
                finish()
            }
        } else {
            if (SharedPref.readBool(ZeemartAppConstants.DISPLAY_UPDATE_DIALOG_IS_VISIBLE, false) == true) {
                val newIntentFront = Intent(this, UpdateAppDialogActivity::class.java)
                newIntentFront.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                newIntentFront.putExtra(
                    ZeemartAppConstants.UPDATE_DIALOG_TYPE,
                    ZeemartAppConstants.UPDATE_APP_TO_LATEST_DIALOG
                )
                startActivity(newIntentFront)
            } else if (SharedPref.readBool(
                    ZeemartAppConstants.DISPLAY_FORCE_UPDATE_DIALOG_IS_VISIBLE,
                    false
                )!!
            ) {
                val newIntent = Intent(this, UpdateAppDialogActivity::class.java)
                newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                newIntent.putExtra(
                    ZeemartAppConstants.UPDATE_DIALOG_TYPE,
                    ZeemartAppConstants.FORCE_UPDATE
                )
                startActivity(newIntent)
            }
        }
    }

    /**
     * Parul Bhandari
     * void
     * show the progress bar
     */
    protected fun showProgress() {
        threeDotLoader!!.visibility = View.VISIBLE
    }

    /**
     * Parul Bhandari
     * Hide the progress bar
     */
    protected fun hideProgress() {
        threeDotLoader!!.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    //set up the config to fetch the remote config data from firebase
    fun setUpRemoteConfigUpdates() {
        // cache expiration in seconds
        var cacheExpiration: Long = 3600
        if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
            cacheExpiration = 3600
        }
        if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
            cacheExpiration = 0
        }
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(cacheExpiration)
            .build()
        mRemoteConfig = FirebaseRemoteConfig.getInstance()
        mRemoteConfig!!.setConfigSettingsAsync(configSettings)

        //set default config values
        val defaults = HashMap<String, Any>()
        defaults[MarketConstants.FORCE_UPDATE_VERSION_ANDROID] = "1.0"
        defaults[MarketConstants.GOOGLE_PLAY_APP_VERSION] = "1.0"
        mRemoteConfig!!.setDefaultsAsync(defaults)
        mRemoteConfig!!.fetch(cacheExpiration)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // task successful. Activate the fetched data
                    mRemoteConfig!!.activate()
                    /* String val = mRemoteConfig.getString(ZeemartAppConstants.FORCE_UPDATE_VERSION_ANDROID,"1.0");
                                Log.d("CONFIG VAL",val+"************"+mRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()+mRemoteConfig.getInfo().getLastFetchStatus()+"&&&&&&"+mRemoteConfig.getInfo().getFetchTimeMillis());*/
                } else {
                    //task failed
                }
            }
    }

    override fun onPostResume() {
        super.onPostResume()
        val forceUpdateVersion = "" + FirebaseRemoteConfig.getInstance()
            .getString(MarketConstants.FORCE_UPDATE_VERSION_ANDROID)
        val googlePlayVersion = "" + FirebaseRemoteConfig.getInstance()
            .getString(MarketConstants.GOOGLE_PLAY_APP_VERSION)
        if (StringHelper.isStringNullOrEmpty(forceUpdateVersion) || StringHelper.isStringNullOrEmpty(
                googlePlayVersion
            )
        ) {
            Log.d("onPostResume", "$forceUpdateVersion***###$googlePlayVersion")
            setUpRemoteConfigUpdates()
        }
    }
}