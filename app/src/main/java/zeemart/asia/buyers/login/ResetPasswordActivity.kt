package zeemart.asia.buyers.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.DebugConstants
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.network.LoginApi
import zeemart.asia.buyers.network.VolleyErrorHelper

class ResetPasswordActivity : AppCompatActivity() {
    private var emailEdit: EditText? = null
    private var lytResetPassword: RelativeLayout? = null
    private var backToLogin: TextView? = null
    private var txtRequestPassword: TextView? = null
    private var txtResetPassword: TextView? = null
    private var zeemartId: String? = null
    private var progressBar: ProgressBar? = null
    private var txtHeader: TextView? = null
    private lateinit var imgClose: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        txtHeader = findViewById(R.id.txt_reset_password)
        emailEdit = findViewById<View>(R.id.emailEditResetPassword) as EditText
        progressBar = findViewById<View>(R.id.progressBarResetPassword) as ProgressBar
        backToLogin = findViewById<View>(R.id.backToLogin) as TextView
        backToLogin!!.setOnClickListener { finish() }
        lytResetPassword = findViewById<View>(R.id.lytButtonResetPassword) as RelativeLayout
        lytResetPassword!!.setOnClickListener { view -> //hide keyboard
            val inputManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            if (isValidSigninDetails) {
                onValidationSucceeded()
            }
        }
        imgClose = findViewById(R.id.bt_back_btn)
        imgClose.setOnClickListener(View.OnClickListener { finish() })
        txtRequestPassword = findViewById<View>(R.id.txt_request_password) as TextView
        txtResetPassword = findViewById<View>(R.id.txt_reset_password_send_to_mail) as TextView
        setFontforViews()
    }

    private fun setFontforViews() {
        setTypefaceView(txtResetPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(emailEdit, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(backToLogin, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtRequestPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
    }

    private val isValidSigninDetails: Boolean
        private get() {
            var isValid = true
            if (StringHelper.isStringNullOrEmpty(emailEdit!!.text.toString())) {
                emailEdit!!.error = "please fill details"
                isValid = false
            } else {
                val isDigits = TextUtils.isDigitsOnly(emailEdit!!.text.toString())
                if (isDigits) {
                    if (emailEdit!!.text.length != 8) {
                        emailEdit!!.error = "invalid mobile number"
                        isValid = false
                    }
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(emailEdit!!.text.toString()).matches()) {
                        emailEdit!!.error = "invalid email"
                        isValid = false
                    }
                }
            }
            return isValid
        }

    private fun onValidationSucceeded() {
        showProgress(true)
        val isDigits = TextUtils.isDigitsOnly(emailEdit!!.text.toString())
        zeemartId = if (isDigits) {
            if (emailEdit!!.text.toString().contains(ZeemartAppConstants.SINGAPORE_COUNTRY_CODE)) {
                emailEdit!!.text.toString()
            } else {
                ZeemartAppConstants.SINGAPORE_COUNTRY_CODE + emailEdit!!.text.toString()
            }
        } else {
            emailEdit!!.text.toString()
        }
        LoginApi.forgotPassword(
            this@ResetPasswordActivity,
            zeemartId,
            object : GetRequestStatusResponseListener {
                override fun onSuccessResponse(status: String?) {
                    showProgress(false)
                    if (DebugConstants.isDebug) {
                        Log.d(DebugConstants.ZEEMARTDEBUG, status!!)
                    }
                    if (!StringHelper.isStringNullOrEmpty(status)) {
                        val newIntent =
                            Intent(this@ResetPasswordActivity, VerifyCodeActivity::class.java)
                        newIntent.putExtra(ZeemartAppConstants.RESET_PWD_ZEEMART_ID, zeemartId)
                        newIntent.putExtra(
                            ZeemartAppConstants.CALLED_FROM,
                            ZeemartAppConstants.CALLED_FOR_SIGN_IN
                        )
                        startActivity(newIntent)
                        finish()
                    } else {
                        getToastRed(getString(R.string.forgotpassword_invalid_email_add))
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    showProgress(false)
                    val errorMessage = error?.errorMessage
                    val errorType = error?.errorType
                    if (errorType == ServiceConstant.RETRY_API_CALL__500_MESSAGE) {
                    } else if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        getToastRed(errorMessage)
                    }
                }
            })
    }

    /**
     * method to show the progress bar
     *
     * @param show
     */
    private fun showProgress(show: Boolean) {
        if (show) {
            emailEdit!!.isEnabled = false
            lytResetPassword!!.isClickable = false
            progressBar!!.visibility = View.VISIBLE
        } else {
            emailEdit!!.isEnabled = true
            lytResetPassword!!.isClickable = true
            progressBar!!.visibility = View.GONE
        }
    }
}