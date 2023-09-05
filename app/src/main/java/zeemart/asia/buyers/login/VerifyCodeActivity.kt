package zeemart.asia.buyers.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastGreen
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.DebugConstants
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.network.LoginApi
import zeemart.asia.buyers.network.VolleyErrorHelper

class VerifyCodeActivity : AppCompatActivity() {
    private var edtPassCode1: EditText? = null
    private var edtPassCode2: EditText? = null
    private var edtPassCode3: EditText? = null
    private var edtPassCode4: EditText? = null
    private var edtPassCode5: EditText? = null
    private var edtPassCode6: EditText? = null
    private var lytChangePassword: RelativeLayout? = null
    private var txtCodeValidation: TextView? = null
    private var progressBar: ProgressBar? = null
    private var zeemartId: String? = null
    private var isPassCodeError = false
    private var passcode: String? = null
    private var txtResetPasswordEmail: TextView? = null
    private var calledFrom: String? = null
    private lateinit var txtHeader: TextView
    private var txtEnterVerificationCode: TextView? = null
    private var isMobileSelected = false
    private var userID: String? = ""
    private var txtSendOtpAgain: TextView? = null
    private lateinit var imgClose: ImageView
    private var txtContinue: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        isPassCodeError = false
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.RESET_PWD_ZEEMART_ID)) {
                zeemartId = intent.extras!!.getString(ZeemartAppConstants.RESET_PWD_ZEEMART_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = intent.extras!!.getString(ZeemartAppConstants.CALLED_FROM)
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED)) {
                isMobileSelected =
                    intent.extras!!.getBoolean(ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED)
            }
            if (bundle.containsKey(ZeemartAppConstants.USER_ID)) {
                userID = intent.extras!!.getString(ZeemartAppConstants.USER_ID)
            }
        }
        imgClose = findViewById(R.id.bt_back_btn)
        imgClose.setOnClickListener(View.OnClickListener { finish() })
        txtHeader = findViewById(R.id.headerTxt)
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FOR_SIGN_UP) {
            if (isMobileSelected) {
                txtHeader.setText(resources.getString(R.string.txt_verify_mobile))
            } else {
                txtHeader.setText(resources.getString(R.string.txt_verify_email))
            }
        } else {
            txtHeader.setText(resources.getString(R.string.verifycode_enter_password_reset_code))
        }
        txtEnterVerificationCode = findViewById(R.id.tv1)
        setTypefaceView(
            txtEnterVerificationCode,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtContinue = findViewById(R.id.txtVerifyCode)
        setTypefaceView(txtContinue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        txtResetPasswordEmail = findViewById<View>(R.id.txt_email_pwd_reset) as TextView
        setTypefaceView(
            txtResetPasswordEmail,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtResetPasswordEmail!!.text = zeemartId + ""
        val edtListener = VerifyCodeTextChangeListner()
        val edtTouchListener = PassCodeEditOnTouchListener()
        edtPassCode1 = findViewById<View>(R.id.code1) as EditText
        edtPassCode1!!.addTextChangedListener(edtListener)
        edtPassCode1!!.setOnTouchListener(edtTouchListener)
        edtPassCode2 = findViewById<View>(R.id.code2) as EditText
        edtPassCode2!!.addTextChangedListener(edtListener)
        edtPassCode2!!.setOnTouchListener(edtTouchListener)
        edtPassCode3 = findViewById<View>(R.id.code3) as EditText
        edtPassCode3!!.addTextChangedListener(edtListener)
        edtPassCode3!!.setOnTouchListener(edtTouchListener)
        edtPassCode4 = findViewById<View>(R.id.code4) as EditText
        edtPassCode4!!.addTextChangedListener(edtListener)
        edtPassCode4!!.setOnTouchListener(edtTouchListener)
        edtPassCode5 = findViewById<View>(R.id.code5) as EditText
        edtPassCode5!!.addTextChangedListener(edtListener)
        edtPassCode5!!.setOnTouchListener(edtTouchListener)
        edtPassCode6 = findViewById<View>(R.id.code6) as EditText
        edtPassCode6!!.addTextChangedListener(edtListener)
        edtPassCode6!!.setOnTouchListener(edtTouchListener)
        progressBar = findViewById<View>(R.id.progressBarVerifyCode) as ProgressBar
        val txtDidntReqThisChange = findViewById<View>(R.id.txtDidntReqChange) as TextView
        setTypefaceView(
            txtDidntReqThisChange,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtSendOtpAgain = findViewById<View>(R.id.txtSkpLoginAgain) as TextView
        txtSendOtpAgain!!.setOnClickListener { callResendVerificationApi() }
        setTypefaceView(txtSendOtpAgain, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setCounterForText()
        txtCodeValidation = findViewById<View>(R.id.txtCodeValidation) as TextView
        setTypefaceView(txtCodeValidation, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        txtCodeValidation!!.visibility = View.GONE
        lytChangePassword = findViewById<View>(R.id.lytVerifyCode) as RelativeLayout
        lytChangePassword!!.setOnClickListener {
            isPassCodeError = false
            passcode = validatePasscode()
            if (isMobileSelected) {
                AnalyticsHelper.logGuestAction(
                    this@VerifyCodeActivity,
                    AnalyticsHelper.TAP_SIGNUP_MOBILE_SIGNUP,
                    isMobileSelected,
                    zeemartId!!
                )
            } else {
                AnalyticsHelper.logGuestAction(
                    this@VerifyCodeActivity,
                    AnalyticsHelper.TAP_SIGNUP_EMAIL_SIGNUP,
                    isMobileSelected,
                    zeemartId!!
                )
            }
            if (!StringHelper.isStringNullOrEmpty(passcode)) {
                //show progressBar
                showProgress(true)
                var requestedBy = ""
                requestedBy = if (calledFrom == ZeemartAppConstants.CALLED_FOR_SIGN_UP) {
                    ZeemartAppConstants.ONBOARD_USER
                } else {
                    ZeemartAppConstants.ZM_USER
                }
                //call the verify passcode API
                LoginApi.validateVerificationCode(
                    this@VerifyCodeActivity,
                    zeemartId,
                    passcode,
                    requestedBy,
                    object : GetRequestStatusResponseListener {
                        override fun onSuccessResponse(status: String?) {
                            showProgress(false)
                            if (DebugConstants.isDebug) {
                                Log.d(DebugConstants.ZEEMARTDEBUG, status!!)
                            }
                            if (!StringHelper.isStringNullOrEmpty(status)) {
                                txtCodeValidation!!.visibility = View.GONE
                                isPassCodeError = false
                                val newIntent = Intent(
                                    this@VerifyCodeActivity,
                                    ChangePasswordActivity::class.java
                                )
                                newIntent.putExtra(
                                    ZeemartAppConstants.RESET_PWD_ZEEMART_ID,
                                    zeemartId
                                )
                                newIntent.putExtra(
                                    ZeemartAppConstants.VERIFICATION_PASSCODE,
                                    passcode
                                )
                                newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, calledFrom)
                                newIntent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM_MOBILE_SELECTED,
                                    isMobileSelected
                                )
                                if (!StringHelper.isStringNullOrEmpty(userID)) newIntent.putExtra(
                                    ZeemartAppConstants.USER_ID,
                                    userID
                                )
                                startActivity(newIntent)
                                finish()
                            }
                        }

                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                            showProgress(false)
                            isPassCodeError = true
                            txtCodeValidation!!.visibility = View.VISIBLE
                            txtCodeValidation!!.setText(R.string.incorrect_code)
                            txtCodeValidation!!.setTextColor(resources.getColor(R.color.pinky_red))
                            edtPassCode1!!.setBackgroundResource(R.drawable.red_border)
                            edtPassCode2!!.setBackgroundResource(R.drawable.red_border)
                            edtPassCode3!!.setBackgroundResource(R.drawable.red_border)
                            edtPassCode4!!.setBackgroundResource(R.drawable.red_border)
                            edtPassCode5!!.setBackgroundResource(R.drawable.red_border)
                            edtPassCode6!!.setBackgroundResource(R.drawable.red_border)
                            val errorMessage = error?.errorMessage
                            val errorType = error?.errorType
                            if (errorType == ServiceConstant.RETRY_API_CALL__500_MESSAGE) {
                            } else if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                getToastRed(errorMessage)
                            }
                        }
                    })
            } else {
                getToastRed(getString(R.string.Please_enter_passcode))
            }
        }
    }

    private fun callResendVerificationApi() {
        if (calledFrom == ZeemartAppConstants.CALLED_FOR_SIGN_UP) {
            LoginApi.regenerateVerificationCode(
                this@VerifyCodeActivity,
                userID,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        if (!StringHelper.isStringNullOrEmpty(status)) {
                            getToastGreen("Verification code sent again successfully")
                            setCounterForText()
                        }
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {}
                })
        } else {
            LoginApi.forgotPassword(
                this@VerifyCodeActivity,
                zeemartId,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        if (!StringHelper.isStringNullOrEmpty(status)) {
                            getToastGreen("Verification code sent again successfully")
                            setCounterForText()
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
    }

    private fun setCounterForText() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtSendOtpAgain!!.isClickable = false
                txtSendOtpAgain!!.alpha = 0.5f
                txtSendOtpAgain!!.text =
                    resources.getString(R.string.txt_send_again) + " (" + millisUntilFinished / 1000 + ")"
            }

            override fun onFinish() {
                txtSendOtpAgain!!.isClickable = true
                txtSendOtpAgain!!.alpha = 1f
                txtSendOtpAgain!!.text = resources.getString(R.string.txt_send_again)
            }
        }.start()
    }

    /**
     * checks if the edit text are all filled or not
     *
     * @return passcode
     */
    private fun validatePasscode(): String? {
        var passCode: String? = null
        if (!StringHelper.isStringNullOrEmpty(edtPassCode1!!.text.toString()) && !StringHelper.isStringNullOrEmpty(
                edtPassCode2!!.text.toString()
            ) && !StringHelper.isStringNullOrEmpty(
                edtPassCode3!!.text.toString()
            )
            && !StringHelper.isStringNullOrEmpty(edtPassCode4!!.text.toString()) && !StringHelper.isStringNullOrEmpty(
                edtPassCode5!!.text.toString()
            ) && !StringHelper.isStringNullOrEmpty(
                edtPassCode6!!.text.toString()
            )
        ) {
            passCode = StringHelper.join(
                arrayOf(
                    edtPassCode1!!.text.toString(),
                    edtPassCode2!!.text.toString(),
                    edtPassCode3!!.text.toString(),
                    edtPassCode4!!.text.toString(),
                    edtPassCode5!!.text.toString(),
                    edtPassCode6!!.text.toString()
                )
            )
        }
        if (DebugConstants.isDebug) {
            Log.d(DebugConstants.ZEEMARTDEBUG, "$passCode***")
        }
        return passCode
    }

    private inner class VerifyCodeTextChangeListner : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (isPassCodeError) {
                edtPassCode1!!.setBackgroundColor(resources.getColor(R.color.white))
                edtPassCode2!!.setBackgroundColor(resources.getColor(R.color.white))
                edtPassCode3!!.setBackgroundColor(resources.getColor(R.color.white))
                edtPassCode4!!.setBackgroundColor(resources.getColor(R.color.white))
                edtPassCode5!!.setBackgroundColor(resources.getColor(R.color.white))
                edtPassCode6!!.setBackgroundColor(resources.getColor(R.color.white))
                txtCodeValidation!!.visibility = View.GONE
            }
            if (edtPassCode1!!.hasFocus() && edtPassCode1!!.text.length == 1) {
                edtPassCode2!!.requestFocus()
                edtPassCode2!!.setText("")
            } else if (edtPassCode2!!.hasFocus() && edtPassCode2!!.text.length == 1) {
                edtPassCode3!!.requestFocus()
                edtPassCode3!!.setText("")
            } else if (edtPassCode3!!.hasFocus() && edtPassCode3!!.text.length == 1) {
                edtPassCode4!!.requestFocus()
                edtPassCode4!!.setText("")
            } else if (edtPassCode4!!.hasFocus() && edtPassCode4!!.text.length == 1) {
                edtPassCode5!!.requestFocus()
                edtPassCode5!!.setText("")
            } else if (edtPassCode5!!.hasFocus() && edtPassCode5!!.text.length == 1) {
                edtPassCode6!!.requestFocus()
                edtPassCode6!!.setText("")
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    /**
     * method to show the progress bar
     *
     * @param show
     */
    private fun showProgress(show: Boolean) {
        if (show) {
            edtPassCode1!!.isEnabled = false
            edtPassCode2!!.isEnabled = false
            edtPassCode3!!.isEnabled = false
            edtPassCode4!!.isEnabled = false
            edtPassCode5!!.isEnabled = false
            edtPassCode6!!.isEnabled = false
            lytChangePassword!!.isClickable = false
            progressBar!!.visibility = View.VISIBLE
        } else {
            edtPassCode1!!.isEnabled = true
            edtPassCode2!!.isEnabled = true
            edtPassCode3!!.isEnabled = true
            edtPassCode4!!.isEnabled = true
            edtPassCode5!!.isEnabled = true
            edtPassCode6!!.isEnabled = true
            lytChangePassword!!.isClickable = true
            progressBar!!.visibility = View.GONE
        }
    }

    private inner class PassCodeEditOnTouchListener : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val edtText = v as EditText
            edtText.setText("")
            edtText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
            return false
        }
    }
}