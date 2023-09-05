package zeemart.asia.buyers.notifications

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastGreen
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.NotificationDataHelper
import zeemart.asia.buyers.helper.NotificationDataHelper.GetNotificationStatus
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by Rajprudhvi Marella on 30/10/2018.
 */
class NotificationSettings : AppCompatActivity() {
    private var txtNotificationSettingsHeader: TextView? = null
    private var txtNotificationNotifies: TextView? = null
    private var txtNotificationRemains: TextView? = null
    private var txtNotificationEmailNotOpen: TextView? = null
    private lateinit var btnBack: ImageButton
    private lateinit var btnNotificationOnOff: Switch
    private var progressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)
        progressBar = findViewById(R.id.progress_bar_notification)
        txtNotificationSettingsHeader = findViewById(R.id.txt_notification_settings_header)
        txtNotificationNotifies = findViewById(R.id.txt_notification_notifies)
        txtNotificationRemains = findViewById(R.id.txt_notification_remains)
        txtNotificationEmailNotOpen = findViewById(R.id.txt_notification_email_not_open)
        btnNotificationOnOff = findViewById(R.id.btn_notification_on_off)
        setFont()
        btnBack = findViewById(R.id.notification_settings_image_button_move_back)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        if (SharedPref.readBool(SharedPref.USER_NOTIFICATION_SETTING_STATUS, false) == true) {
            btnNotificationOnOff.setChecked(true)
        } else {
            btnNotificationOnOff.setChecked(false)
        }
        if (btnNotificationOnOff != null) {
            btnNotificationOnOff!!.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    //do stuff when Switch is ON
                    callNotificationApi(isChecked)
                } else {
                    //do stuff when Switch if OFF
                    callNotificationApi(isChecked)
                }
            }
        }
    }

    private fun callNotificationApi(isChecked: Boolean) {
        progressBar!!.visibility = View.VISIBLE
        //do stuff when Switch is ON
        NotificationDataHelper.callNotificationApi(isChecked, object : GetNotificationStatus {
            override fun onSuccessResponse(isChecked: Boolean) {
                progressBar!!.visibility = View.GONE
                if (isChecked) {
                    SharedPref.writeBool(SharedPref.USER_NOTIFICATION_SETTING_STATUS, true)
                    getToastGreen(resources.getString(R.string.notification_txt_turned_on))
                } else {
                    SharedPref.writeBool(SharedPref.USER_NOTIFICATION_SETTING_STATUS, false)
                    getToastGreen(resources.getString(R.string.notification_txt_turned_off))
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {
                progressBar!!.visibility = View.GONE
                getToastRed(error!!.errorMessage)
            }
        }, this@NotificationSettings)
    }

    private fun setFont() {
        setTypefaceView(
            txtNotificationSettingsHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtNotificationNotifies,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtNotificationEmailNotOpen,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtNotificationRemains,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }
}