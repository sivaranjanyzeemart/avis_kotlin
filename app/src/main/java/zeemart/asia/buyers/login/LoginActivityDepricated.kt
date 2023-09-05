package zeemart.asia.buyers.login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.Validator.ValidationListener
import com.mobsandgeeks.saripaar.annotation.Email
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
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.RejectedInvoiceActivity
import zeemart.asia.buyers.models.BuyerCredentials
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.network.LoginApi
import zeemart.asia.buyers.network.LoginApi.LoginBuyerListener
import zeemart.asia.buyers.network.PushNotificationApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.notifications.NotificationAnnouncementDetails
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.util.*

/**
 * Parul Bhandari
 * Buyer Login Activity class
 */
class LoginActivityDepricated : AppCompatActivity(), ValidationListener {
    //    @Password(min = 8, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS)
    private var passwordEdit: EditText? = null

    @Email
    private lateinit var emailEdit: EditText
    private var validator: Validator? = null
    private var progressBar: ProgressBar? = null
    private lateinit var forgotPassword: TextView
    private lateinit var signupText: TextView
    private lateinit var loginBtn: Button
    private var buyerCredentials: BuyerCredentials? = null
    private var mRemoteConfig: FirebaseRemoteConfig? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        passwordEdit = findViewById(R.id.password_edit)
        emailEdit = findViewById(R.id.email_edit)
        progressBar = findViewById(R.id.progress_bar)
        validator = Validator(this)
        validator!!.setValidationListener(this)
        signupText = findViewById(R.id.signup_text)
        signupText.setOnClickListener(View.OnClickListener {
            val uri = Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_SIGNUP)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })
        forgotPassword = findViewById(R.id.forgot_password)
        forgotPassword.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@LoginActivityDepricated,
                    ResetPasswordActivity::class.java
                )
            )
        })
        loginBtn = findViewById(R.id.login_btn)
        loginBtn.setClickable(true)
        loginBtn.setOnClickListener(View.OnClickListener { view ->
            if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
                if (!emailEdit.getText().toString().contains("@")) {
                    emailEdit.setText(emailEdit.getText().toString() + "@zeemart.asia")
                }
            }

            //hide keyboard
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            validator!!.validate()
        })
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mFCMRefreshTokenReceived,
            IntentFilter(NotificationConstants.REFRESH_TOKEN_RECEIVED)
        )
        ChangeLanguageLocale(
            this@LoginActivityDepricated,
            Locale(
                SharedPref.read(
                    SharedPref.SELECTED_LANGUAGE,
                    ZeemartAppConstants.Market.`this`.defaultLanguage.langCode
                )
            )
        )
        setFontforViews()
    }

    private fun setFontforViews() {
        setTypefaceView(passwordEdit, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(emailEdit, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(signupText, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(loginBtn, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(forgotPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFCMRefreshTokenReceived)
    }

    override fun onResume() {
        super.onResume()
        Log.d("EXTRA DATA", intent.extras.toString() + "******")
        if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
            passwordEdit!!.setText("!123456Zm")
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
            if (SharedPref.readBool(ZeemartAppConstants.DISPLAY_UPDATE_DIALOG_IS_VISIBLE, false)!!) {
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

    override fun onValidationSucceeded() {
        buyerCredentials =
            BuyerCredentials(emailEdit!!.text.toString(), passwordEdit!!.text.toString())
        if (buyerCredentials!!.checkNullEmpty()) {
            getToastRed(getText(R.string.login_empty_username_password).toString())
        } else {
            showProgress()
            loginBtn!!.isClickable = false
            LoginApi.loginBuyer(
                this@LoginActivityDepricated,
                buyerCredentials!!,
                object : LoginBuyerListener {
                    override fun onSuccessResponse(buyerDetails: BuyerDetails) {
                        AnalyticsHelper.logAction(
                            this@LoginActivityDepricated,
                            AnalyticsHelper.LOGIN_SUCCESS,
                            buyerDetails
                        )
                        hideProgress()
                        loginBtn!!.isClickable = true
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
                                    this@LoginActivityDepricated,
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
                                this@LoginActivityDepricated,
                                Locale(
                                    SharedPref.read(
                                        SharedPref.SELECTED_LANGUAGE,
                                        ZeemartAppConstants.Language.BAHASA_INDONESIA.langCode
                                    )
                                )
                            )
                        } else if (buyerDetails.language == ZeemartAppConstants.Language.VIETNAMESE.locLangCode) {
                            ChangeLanguageLocale(
                                this@LoginActivityDepricated,
                                Locale(
                                    SharedPref.read(
                                        SharedPref.SELECTED_LANGUAGE,
                                        ZeemartAppConstants.Language.VIETNAMESE.langCode
                                    )
                                )
                            )
                        } else if (buyerDetails.language == ZeemartAppConstants.Language.BAHASA_MELAYU.locLangCode) {
                            ChangeLanguageLocale(
                                this@LoginActivityDepricated,
                                Locale(
                                    SharedPref.read(
                                        SharedPref.SELECTED_LANGUAGE,
                                        ZeemartAppConstants.Language.BAHASA_MELAYU.langCode
                                    )
                                )
                            )
                        } else if (buyerDetails.language == ZeemartAppConstants.Language.CHINESE.locLangCode) {
                            ChangeLanguageLocale(
                                this@LoginActivityDepricated,
                                Locale(
                                    SharedPref.read(
                                        SharedPref.SELECTED_LANGUAGE,
                                        ZeemartAppConstants.Language.CHINESE.langCode
                                    )
                                )
                            )
                        } else if (buyerDetails.language == ZeemartAppConstants.Language.HINDI.locLangCode) {
                            ChangeLanguageLocale(
                                this@LoginActivityDepricated,
                                Locale(
                                    SharedPref.read(
                                        SharedPref.SELECTED_LANGUAGE,
                                        ZeemartAppConstants.Language.HINDI.langCode
                                    )
                                )
                            )
                        } else {
                            ChangeLanguageLocale(
                                this@LoginActivityDepricated,
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
                        loginBtn!!.isClickable = true
                        SharedPref.write(SharedPref.USERNAME, null)
                        SharedPref.write(SharedPref.PASSWORD_ENCRYPTED, null)
                        SharedPref.write(SharedPref.ACCESS_TOKEN, null)
                    }
                })
        }
    }

    override fun onValidationFailed(errors: List<ValidationError>) {
        loginBtn!!.isClickable = true
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(this)
            if (view is EditText) {
                view.error = message
            }
        }
    }

    /**
     * Parul Bhandari
     * void
     * show the progress bar
     */
    protected fun showProgress() {
        forgotPassword!!.isClickable = false
        loginBtn!!.isClickable = false
        passwordEdit!!.isEnabled = false
        emailEdit!!.isEnabled = false
        progressBar!!.visibility = View.VISIBLE
    }

    /**
     * Parul Bhandari
     * Hide the progress bar
     */
    protected fun hideProgress() {
        forgotPassword!!.isClickable = true
        loginBtn!!.isClickable = true
        passwordEdit!!.isEnabled = true
        emailEdit!!.isEnabled = true
        progressBar!!.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
                    this@LoginActivityDepricated,
                    SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")!!
                )
            }
        }
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