package zeemart.asia.buyers.inventory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.helper.NotificationDataHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.ViewSpecificUser
import zeemart.asia.buyers.network.UsersApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.util.*

class InventorySettingsActivity : AppCompatActivity() {
    private var txtInventorySettingsHeader: TextView? = null
    private var txtPreferences: TextView? = null
    private var txtNotificationRemains: TextView? = null
    private var txtNotificationEmailNotOpen: TextView? = null
    private var txtPreferenceReports: TextView? = null
    private var txtPreferenceReportsSettings: TextView? = null
    private lateinit var btnBack: ImageButton
    private var btnNotificationOnOff: Switch? = null
    private var btnReportsOnOff: Switch? = null
    private lateinit var lytLanguage: RelativeLayout
    private var lytPreference: RelativeLayout? = null
    private var progressBar: ProgressBar? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_settings)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar_notification)
        txtInventorySettingsHeader = findViewById<TextView>(R.id.txt_inventory_settings_header)
        txtPreferences = findViewById<TextView>(R.id.txt_preferences)
        txtNotificationRemains = findViewById<TextView>(R.id.txt_notification_remains)
        txtPreferenceReports = findViewById<TextView>(R.id.txt_notification_reports)
        txtPreferenceReportsSettings =
            findViewById<TextView>(R.id.txt_notification_reports_settings)
        txtNotificationEmailNotOpen = findViewById<TextView>(R.id.txt_notification_email_not_open)
        btnNotificationOnOff = findViewById<Switch>(R.id.btn_notification_on_off)
        btnReportsOnOff = findViewById<Switch>(R.id.btn_notification_on_off_reports)
        lytPreference = findViewById<RelativeLayout>(R.id.lyt_notification_reports)
        setFont()
        callUserApi()
        lytLanguage = findViewById<RelativeLayout>(R.id.lyt_language)
        lytLanguage.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(
                this@InventorySettingsActivity,
                AnalyticsHelper.TAP_MORE_TAB_LANGUAGE
            )
            DialogHelper.ShowLanguageSelection(
                this@InventorySettingsActivity,
                object : DialogHelper.LanguageSelectionCallback {
                    override fun selected(locale: Locale?, language: String?) {
                        Log.d("===> LANGUAGE", language!!)
                        val data = Intent()
                        data.putExtra("LANGUAGE", language)
                        setResult(217, data)
                        finish()
                    }

                    override fun dismiss() {}
                })
        })
        btnBack = findViewById<ImageButton>(R.id.notification_settings_image_button_move_back)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
    }

    private fun checkNotification() {
        if (SharedPref.readBool(SharedPref.USER_INVENTORY_SETTING_STATUS, false) == true) {
            btnNotificationOnOff!!.isChecked = true
        } else {
            btnNotificationOnOff!!.isChecked = false
        }
        if (btnNotificationOnOff != null) {
            btnNotificationOnOff!!.setOnCheckedChangeListener(object :
                CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
                    if (isChecked) {
                        //do stuff when Switch is ON
                        callNotificationApi(isChecked)
                    } else {
                        //do stuff when Switch if OFF
                        callNotificationApi(isChecked)
                    }
                }
            })
        }
    }

    private fun callNotificationApi(isChecked: Boolean) {
        progressBar?.setVisibility(View.VISIBLE)
        //do stuff when Switch is ON
        NotificationDataHelper.callInventorySettingsApi(isChecked, object :
            NotificationDataHelper.GetNotificationStatus {
            override fun onSuccessResponse(isChecked: Boolean) {
                progressBar?.setVisibility(View.GONE)
                if (isChecked) {
                    SharedPref.writeBool(SharedPref.USER_INVENTORY_SETTING_STATUS, true)
                    //                    ZeemartBuyerApp.getToastGreen(getResources().getString(R.string.notification_txt_turned_on));
                } else {
                    SharedPref.writeBool(SharedPref.USER_INVENTORY_SETTING_STATUS, false)
                    //                    ZeemartBuyerApp.getToastGreen(getResources().getString(R.string.notification_txt_turned_off));
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {
                progressBar?.setVisibility(View.GONE)
                ZeemartBuyerApp.getToastRed(error?.errorMessage)
            }
        }, this@InventorySettingsActivity)
    }

    private fun callUserApi() {
        UsersApi.viewSpecificUser(this, SharedPref.userId, object :
            UsersApi.ViewSpecificUserListener {
            override fun onSuccessResponse(viewUser: ViewSpecificUser?) {
                SharedPref.write(SharedPref.USER_EMAIL_ID, viewUser?.data?.email)
                SharedPref.write(SharedPref.USER_FIRST_NAME, viewUser?.data?.firstName)
                SharedPref.write(SharedPref.USER_ROLE_GROUP, viewUser?.data?.roleGroup)
                SharedPref.write(SharedPref.USER_LAST_NAME, viewUser?.data?.lastName)
                SharedPref.write(SharedPref.USER_ID, viewUser?.data?.userId)
                SharedPref.write(SharedPref.USER_IMAGE_URL, viewUser?.data?.imageURL)
                SharedPref.write(SharedPref.USER_PHONE_NUMBER, viewUser?.data?.phone)
                SharedPref.writeBool(
                    SharedPref.USER_NOTIFICATION_SETTING_STATUS,
                    viewUser?.data?.trackNotificationsStatus!!
                )
                if (viewUser.data!!.settings?.inventorySettings != null && viewUser.data!!.settings?.inventorySettings!!.displayCustomName != null) SharedPref.writeBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    viewUser.data!!.settings?.inventorySettings?.displayCustomName!!
                )
                if (viewUser.data!!.customNameAndCode != null) SharedPref.writeBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    viewUser.data!!.customNameAndCode!!
                )
                if (viewUser.data!!.doNotSendReports != null) SharedPref.writeBool(
                    SharedPref.USER_REPORTS_SETTING_STATUS,
                    viewUser.data!!.doNotSendReports!!
                )
                checkNotification()
                checkReportsPermission()
            }

            override fun onErrorResponse(error: VolleyErrorHelper) {
                Log.d("", error.errorMessage + "*******")
                checkNotification()
                checkReportsPermission()
            }
        })
    }

    private fun callREportsSettingsApi(isChecked: Boolean) {
        progressBar?.setVisibility(View.VISIBLE)
        //do stuff when Switch is ON
        NotificationDataHelper.callREportsSettingsApi(isChecked, object :
            NotificationDataHelper.GetNotificationStatus {
            override fun onSuccessResponse(isChecked: Boolean) {
                SharedPref.writeBool(SharedPref.USER_REPORTS_SETTING_STATUS, isChecked)
                progressBar?.setVisibility(View.GONE)
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {
                progressBar?.setVisibility(View.GONE)
                ZeemartBuyerApp.getToastRed(error?.errorMessage)
            }
        }, this@InventorySettingsActivity)
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtInventorySettingsHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPreferences,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNotificationEmailNotOpen,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNotificationRemains,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPreferenceReports,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPreferenceReportsSettings,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun checkReportsPermission() {
        val userDetails: UserDetails = SharedPref.currentUserDetail
        Log.d("===> LANGUAGE", userDetails.roleGroup!!)
        if (userDetails.roleGroup != null && userDetails.roleGroup == "Owner") {
            lytPreference?.setVisibility(View.VISIBLE)
            txtPreferenceReportsSettings?.setVisibility(View.VISIBLE)
        } else {
            lytPreference?.setVisibility(View.GONE)
            txtPreferenceReportsSettings?.setVisibility(View.GONE)
        }
        if (userDetails.doNotSendReports != null && userDetails.doNotSendReports!!) {
            btnReportsOnOff!!.isChecked = true
        } else {
            btnReportsOnOff!!.isChecked = false
        }
        if (btnReportsOnOff != null) {
            btnReportsOnOff!!.setOnCheckedChangeListener(object :
                CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
                    if (isChecked) {
                        //do stuff when Switch is ON
                        callREportsSettingsApi(isChecked)
                    } else {
                        //do stuff when Switch if OFF
                        callREportsSettingsApi(isChecked)
                    }
                }
            })
        }
    }

    companion object {
        fun editUserLanguage(language: String, context: Context?) {
            val userDetails: UserDetails = SharedPref.currentUserDetail
            userDetails.language = language
            UsersApi.editSingleUser(
                context,
                userDetails,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {}
                    override fun onErrorResponse(error: VolleyErrorHelper?) {}
                })
        }
    }
}