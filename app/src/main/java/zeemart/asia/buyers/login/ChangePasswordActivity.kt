package zeemart.asia.buyers.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.modelsimport.BuyerCreatePasswordResponse
import zeemart.asia.buyers.network.LoginApi
import zeemart.asia.buyers.network.VolleyErrorHelper

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var edtEnterNewPassword: EditText
    private var edtReenterPassword: EditText? = null
    private lateinit var lytChangePassword: RelativeLayout
    private var txtPasswordValidation: TextView? = null
    private lateinit var txtPasswordDoNotMatch: TextView
    private var zeemartId: String? = null
    private var passcode: String? = null
    private lateinit var imageButtonClose: ImageView
    private var progressBarChangePassword: ProgressBar? = null
    private var calledFrom: String? = null
    private var txtHeader: TextView? = null
    private var userID: String? = ""
    private var txtContinue: TextView? = null
    private var isMobileSelected = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        val bundle = intent.extras
        if (bundle != null) {
            zeemartId = bundle.getString(ZeemartAppConstants.RESET_PWD_ZEEMART_ID)
            passcode = bundle.getString(ZeemartAppConstants.VERIFICATION_PASSCODE)
            calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            if (bundle.containsKey(ZeemartAppConstants.USER_ID)) {
                userID = intent.extras!!.getString(ZeemartAppConstants.USER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED)) {
                isMobileSelected =
                    intent.extras!!.getBoolean(ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED)
            }
        }
        txtHeader = findViewById(R.id.txt_change_password_header)
        imageButtonClose = findViewById(R.id.image_button_close)
        imageButtonClose.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(this@ChangePasswordActivity, BuyerLoginActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(newIntent)
        })
        progressBarChangePassword = findViewById(R.id.progress_bar_change_password)
        edtEnterNewPassword = findViewById(R.id.edt_enter_new_password)
        edtReenterPassword = findViewById(R.id.edt_re_enter_new_password)
        txtPasswordValidation = findViewById(R.id.txt_password_validation)
        txtPasswordDoNotMatch = findViewById(R.id.txt_password_no_match)
        txtPasswordDoNotMatch.setVisibility(View.GONE)
        lytChangePassword = findViewById(R.id.lyt_submit_new_password)
        txtContinue = findViewById(R.id.txt_submit_new_password)
        setFontforViews()
        lytChangePassword.setOnClickListener(View.OnClickListener { v ->
            if (isMobileSelected) {
                AnalyticsHelper.logGuestAction(
                    this@ChangePasswordActivity,
                    AnalyticsHelper.TAP_SIGNUP_MOBILE_VERIFY_CONTINUE
                )
            } else {
                AnalyticsHelper.logGuestAction(
                    this@ChangePasswordActivity,
                    AnalyticsHelper.TAP_SIGNUP_EMAIL_VERIFY_CONTINUE
                )
            }
            //hide keyboard
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                v.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            if (checkForPasswordEquality()) {
                progressBarChangePassword!!.setVisibility(View.VISIBLE)
                if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FOR_SIGN_UP) {
                    val buyerCreatePasswordResponse = BuyerCreatePasswordResponse()
                    buyerCreatePasswordResponse.password = edtEnterNewPassword!!.getText().toString()
                    buyerCreatePasswordResponse.confirmPassword =
                        edtReenterPassword!!.getText().toString()
                    LoginApi.createPassword(
                        this@ChangePasswordActivity,
                        userID,
                        buyerCreatePasswordResponse,
                        object : GetRequestStatusResponseListener {
                            override fun onSuccessResponse(status: String?) {
                                progressBarChangePassword!!.setVisibility(View.GONE)
                                if (!StringHelper.isStringNullOrEmpty(status)) {
                                    val intent = Intent(
                                        this@ChangePasswordActivity,
                                        CreateCompanyOutlet::class.java
                                    )
                                    intent.putExtra(ZeemartAppConstants.USER_ID, userID)
                                    intent.putExtra(
                                        ZeemartAppConstants.ON_BOARD_USER_NAME,
                                        zeemartId
                                    )
                                    intent.putExtra(
                                        ZeemartAppConstants.ON_BOARD_PASSWORD,
                                        edtEnterNewPassword!!.getText().toString()
                                    )
                                    intent.putExtra(
                                        ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED,
                                        isMobileSelected
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                progressBarChangePassword!!.setVisibility(View.GONE)
                                val errorMessage = error?.errorMessage
                                val errorType = error?.errorType
                                if (errorType == ServiceConstant.RETRY_API_CALL__500_MESSAGE) {
                                } else if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                    getToastRed(errorMessage)
                                } else if (errorType == ServiceConstant.TRY_LOGIN_AGAIN_401_MESSAGE) {
                                    //the auth token has expired get new access token
                                }
                            }
                        })
                } else {
                    LoginApi.resetPassword(
                        this@ChangePasswordActivity,
                        zeemartId,
                        passcode,
                        edtEnterNewPassword!!.getText().toString(),
                        object : GetRequestStatusResponseListener {
                            override fun onSuccessResponse(status: String?) {
                                progressBarChangePassword!!.setVisibility(View.GONE)
                                if (!StringHelper.isStringNullOrEmpty(status)) {
                                    createPasswordChangedAlert()
                                }
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                progressBarChangePassword!!.setVisibility(View.GONE)
                                val errorMessage = error?.errorMessage
                                val errorType = error?.errorType
                                if (errorType == ServiceConstant.RETRY_API_CALL__500_MESSAGE) {
                                } else if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                    getToastRed(errorMessage)
                                } else if (errorType == ServiceConstant.TRY_LOGIN_AGAIN_401_MESSAGE) {
                                    //the auth token has expired get new access token
                                }
                            }
                        })
                }
            }
        })
    }

    /**
     * set the font for the respective views
     */
    private fun setFontforViews() {
        setTypefaceView(edtEnterNewPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(edtReenterPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtPasswordValidation, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtPasswordDoNotMatch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(lytChangePassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtContinue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    /**
     * checks if the enter password and reenter password fields are equal
     */
    private fun checkForPasswordEquality(): Boolean {
        return if (StringHelper.isStringNullOrEmpty(edtEnterNewPassword!!.text.toString())) {
            createValidationAlert(
                getString(R.string.txt_please_enter_new_password),
                getString(R.string.txt_invalid_password)
            )
            false
        } else {
            if (edtEnterNewPassword!!.text.toString() != edtReenterPassword!!.text.toString()) {
                createValidationAlert(
                    getString(R.string.txt_password_check_again),
                    getString(R.string.txt_password_doesnt_match)
                )
                return false
            } else if (!LoginApi.validPasswordStrength(edtEnterNewPassword!!.text.toString())) {
                createValidationAlert(
                    getString(R.string.txt_min_characters),
                    getString(R.string.txt_invalid_password)
                )
                return false
            }
            true
        }
    }

    /**
     * Creates the Password reset Successful Alert dialog
     */
    private fun createPasswordChangedAlert() {
        val builder = AlertDialog.Builder(this@ChangePasswordActivity)
        builder.setPositiveButton(getString(R.string.txt_login_now_button)) { dialog, which ->
            CommonMethods.unregisterLogout(
                this@ChangePasswordActivity
            )
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(Html.fromHtml(getString(R.string.password_reset_successful)))
        dialog.show()
    }

    private fun createValidationAlert(message: String, title: String) {
        val builder = AlertDialog.Builder(this@ChangePasswordActivity)
        builder.setPositiveButton(getString(R.string.txt_yes)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        if (!StringHelper.isStringNullOrEmpty(title)) {
            dialog.setTitle(title)
        } else {
            dialog.setTitle(getString(R.string.txt_reset_password))
        }
        dialog.setMessage(message)
        dialog.show()
    }
}