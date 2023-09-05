package zeemart.asia.buyers.more

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.network.LoginApi
import zeemart.asia.buyers.network.VolleyErrorHelper

class ChangePasswordSettingActivity : AppCompatActivity() {
    lateinit var oldPasswordEdit: EditText
    lateinit var newPasswordEdit: EditText
    lateinit var confirmPasswordEdit: EditText
    lateinit var changePasswordBtn: Button
    var progressBar: ProgressBar? = null
    var txtHeader: TextView? = null
    var txt_forgetten: TextView? = null
    var txt_character: TextView? = null
    lateinit var change_password_back_image: ImageView
    protected fun showProgress(show: Boolean) {
        if (show) {
            oldPasswordEdit!!.isEnabled = false
            newPasswordEdit!!.isEnabled = false
            confirmPasswordEdit!!.isEnabled = false
            changePasswordBtn!!.isClickable = false
            progressBar!!.visibility = View.VISIBLE
        } else {
            oldPasswordEdit!!.isEnabled = true
            newPasswordEdit!!.isEnabled = true
            confirmPasswordEdit!!.isEnabled = true
            changePasswordBtn!!.isClickable = true
            progressBar!!.visibility = View.GONE
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password_setting)
        progressBar = findViewById(R.id.progress_bar_change_password_settings)
        oldPasswordEdit = findViewById(R.id.old_password_edit)
        newPasswordEdit = findViewById(R.id.new_password_edit)
        confirmPasswordEdit = findViewById(R.id.confirm_password_edit)
        txtHeader = findViewById(R.id.txt_header_home)
        changePasswordBtn = findViewById(R.id.change_password_btn)
        txt_forgetten = findViewById(R.id.txt_forgetten)
        txt_character = findViewById(R.id.txt_character)
        change_password_back_image = findViewById(R.id.change_password_back_image)
        changePasswordBtn.setOnClickListener(View.OnClickListener {
            if (oldPasswordEdit.getText().toString().length == 0) {
                getToastRed(getText(R.string.changepassword_invalid_old_password).toString())
                return@OnClickListener
            }
            if (newPasswordEdit.getText().toString().length == 0) {
                getToastRed(getText(R.string.changepassword_invalid_new_password).toString())
                return@OnClickListener
            }
            if (!newPasswordEdit.getText().toString()
                    .equals(confirmPasswordEdit.getText().toString(), ignoreCase = true)
            ) {
                getToastRed(getText(R.string.changepassword_invalid_confirm_password).toString())
                return@OnClickListener
            }
            if (!LoginApi.validPasswordStrength(newPasswordEdit.getText().toString())) {
                getToastRed(getText(R.string.txt_password_not_strong).toString())
                return@OnClickListener
            }
            if (!StringHelper.isStringNullOrEmpty(oldPasswordEdit.getText().toString()) &&
                SharedPref.read(SharedPref.PASSWORD_ENCRYPTED, "") != oldPasswordEdit.getText()
                    .toString()
            ) {
                getToastRed(getString(R.string.txt_incorrect_current_pwd))
                showProgress(false)
                return@OnClickListener
            }
            LoginApi.changePassword(
                this@ChangePasswordSettingActivity,
                oldPasswordEdit.getText().toString(),
                newPasswordEdit.getText().toString(),
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        showProgress(false)
                        passwordChangeSuccessfulAlert()
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        showProgress(false)
                        val errorMessage = error?.errorMessage
                        val errorType = error?.errorType
                        if (errorType == ServiceConstant.RETRY_API_CALL__500_MESSAGE) {
                        }
                        if (errorType == ServiceConstant.TRY_LOGIN_AGAIN_401_MESSAGE) {
                            //access token expired, try login again
                        } else if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                            getToastRed(errorMessage)
                        } else {
                            getToastRed(getText(R.string.changepassword_invalid_change_password).toString())
                        }
                    }
                })
            showProgress(true)
        })
        change_password_back_image.setOnClickListener(View.OnClickListener { onBackPressed() })
        setFont()
    }

    private fun setFont() {
        setTypefaceView(oldPasswordEdit, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(newPasswordEdit, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txt_forgetten, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txt_character, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(confirmPasswordEdit, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(changePasswordBtn, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    private fun passwordChangeSuccessfulAlert() {
        val builder = AlertDialog.Builder(this@ChangePasswordSettingActivity)
        builder.setPositiveButton(R.string.dialog_ok_button_text) { dialog, which ->
            dialog.dismiss()
            CommonMethods.clearLogout(this@ChangePasswordSettingActivity)
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(getString(R.string.password_successfully_changed))
        dialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}