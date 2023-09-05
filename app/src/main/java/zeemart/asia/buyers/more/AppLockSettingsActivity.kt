package zeemart.asia.buyers.more

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.SharedPref

/**
 * Created by Rajprudhvi Marella on 24/Jan/2022.
 */
class AppLockSettingsActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageButton
    private var txtLockSettingsHeader: TextView? = null
    private var txtChangePasswordHeader: TextView? = null
    private lateinit var txtChangePasswordValue: TextView
    private var txtTouchIdHeader: TextView? = null
    private var txtTouchIdValue: TextView? = null
    private lateinit var btnAppLockOnOff: Switch
    private lateinit var lytAppSettings: LinearLayout
    private lateinit var lytImmediatelyFilter: RelativeLayout
    private var lyt_notification_settings_header: RelativeLayout? = null
    private var txtImmediatelyFilter: TextView? = null
    private lateinit var imgImmediatelyFilter: ImageView
    private lateinit var lytAfter5minutesFilter: RelativeLayout
    private var txtAfter5minutesFilter: TextView? = null
    private lateinit var imgAfter5minutesFilter: ImageView
    private lateinit var lytAfter30minutesFilter: RelativeLayout
    private var txtAfter30minutesFilter: TextView? = null
    private lateinit var imgAfter30minutesFilter: ImageView
    private var txtEnableLock: TextView? = null
    override fun onUserLeaveHint() {
        finish()
        super.onUserLeaveHint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_lock_settings)
        txtLockSettingsHeader = findViewById(R.id.txt_notification_settings_header)
        lyt_notification_settings_header = findViewById(R.id.lyt_notification_settings_header)
        txtChangePasswordHeader = findViewById(R.id.txt_change_password_header)
        txtChangePasswordValue = findViewById(R.id.txt_change_password_value)
        txtChangePasswordValue.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(
                this@AppLockSettingsActivity,
                AnalyticsHelper.TAP_MORE_TAB_CHANGE_PASSWORD
            )
            val intent =
                Intent(this@AppLockSettingsActivity, ChangePasswordSettingActivity::class.java)
            startActivity(intent)
        })
        txtEnableLock = findViewById(R.id.txt_enable_lock)
        lytImmediatelyFilter = findViewById(R.id.lyt_filter_immediately_row)
        txtImmediatelyFilter = findViewById(R.id.txt_immediately)
        imgImmediatelyFilter = findViewById(R.id.img_select_filter_immediately)
        lytAfter5minutesFilter = findViewById(R.id.lyt_filter_after_5minutes_row)
        txtAfter5minutesFilter = findViewById(R.id.txt_after_5minutes)
        imgAfter5minutesFilter = findViewById(R.id.img_select_filter_after_5minutes)
        lytAfter30minutesFilter = findViewById(R.id.lyt_filter_after_30minutes_row)
        txtAfter30minutesFilter = findViewById(R.id.txt_after_30minutes)
        imgAfter30minutesFilter = findViewById(R.id.img_select_filter_after_30minutes)
        txtTouchIdHeader = findViewById(R.id.txt_notification_notifies)
        txtTouchIdValue = findViewById(R.id.txt_notification_remains)
        btnAppLockOnOff = findViewById(R.id.btn_notification_on_off)
        lytAppSettings = findViewById(R.id.lyt_options)
        btnBack = findViewById(R.id.notification_settings_image_button_move_back)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        if (SharedPref.readBool(SharedPref.USER_APP_LOCK_SETTING_STATUS, false)!!) {
            btnAppLockOnOff.setChecked(true)
            lytAppSettings.setVisibility(View.VISIBLE)
            imgAfter5minutesFilter.setVisibility(View.GONE)
            imgAfter30minutesFilter.setVisibility(View.GONE)
            imgImmediatelyFilter.setVisibility(View.GONE)
            if (SharedPref.read(
                    SharedPref.USER_APP_LOCK_SETTING_FILTER_VALUE,
                    ""
                ) == ApiParamsHelper.AppSettingsTime.AFTER_5_MINUTES.value
            ) {
                imgAfter5minutesFilter.setVisibility(View.VISIBLE)
            } else if (SharedPref.read(
                    SharedPref.USER_APP_LOCK_SETTING_FILTER_VALUE,
                    ""
                ) == ApiParamsHelper.AppSettingsTime.AFTER_30_MINUTES.value
            ) {
                imgAfter30minutesFilter.setVisibility(View.VISIBLE)
            } else {
                imgImmediatelyFilter.setVisibility(View.VISIBLE)
            }
        } else {
            btnAppLockOnOff.setChecked(false)
            lytAppSettings.setVisibility(View.GONE)
        }
        lytImmediatelyFilter.setOnClickListener(View.OnClickListener {
            SharedPref.write(
                SharedPref.USER_APP_LOCK_SETTING_FILTER_VALUE,
                ApiParamsHelper.AppSettingsTime.IMMEDIATELY.value
            )
            imgAfter5minutesFilter.setVisibility(View.GONE)
            imgAfter30minutesFilter.setVisibility(View.GONE)
            imgImmediatelyFilter.setVisibility(View.VISIBLE)
            SharedPref.writeBool(SharedPref.USER_IS_UNLOCKED, true)
        })
        lytAfter5minutesFilter.setOnClickListener(View.OnClickListener {
            SharedPref.write(
                SharedPref.USER_APP_LOCK_SETTING_FILTER_VALUE,
                ApiParamsHelper.AppSettingsTime.AFTER_5_MINUTES.value
            )
            imgAfter5minutesFilter.setVisibility(View.VISIBLE)
            imgAfter30minutesFilter.setVisibility(View.GONE)
            imgImmediatelyFilter.setVisibility(View.GONE)
            SharedPref.writeBool(SharedPref.USER_IS_UNLOCKED, true)
        })
        lytAfter30minutesFilter.setOnClickListener(View.OnClickListener {
            SharedPref.write(
                SharedPref.USER_APP_LOCK_SETTING_FILTER_VALUE,
                ApiParamsHelper.AppSettingsTime.AFTER_30_MINUTES.value
            )
            imgAfter5minutesFilter.setVisibility(View.GONE)
            imgAfter30minutesFilter.setVisibility(View.VISIBLE)
            imgImmediatelyFilter.setVisibility(View.GONE)
            SharedPref.writeBool(SharedPref.USER_IS_UNLOCKED, true)
        })
        if (btnAppLockOnOff != null) {
            btnAppLockOnOff!!.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    //do stuff when Switch is ON
                    SharedPref.writeBool(SharedPref.USER_APP_LOCK_SETTING_STATUS, true)
                    lytAppSettings.setVisibility(View.VISIBLE)
                    imgAfter5minutesFilter.setVisibility(View.VISIBLE)
                    imgAfter30minutesFilter.setVisibility(View.GONE)
                    imgImmediatelyFilter.setVisibility(View.GONE)
                    //                        Calendar currentTimeNow = Calendar.getInstance(DateHelper.getMarketTimeZone());
//                        currentTimeNow.add(Calendar.MINUTE, 5);
//                        SharedPref.writeLong(SharedPref.USER_APP_LOCK_SETTING_TIME_VALUE, currentTimeNow.getTimeInMillis());
                    SharedPref.write(
                        SharedPref.USER_APP_LOCK_SETTING_FILTER_VALUE,
                        ApiParamsHelper.AppSettingsTime.AFTER_5_MINUTES.value
                    )
                    SharedPref.writeBool(SharedPref.USER_IS_UNLOCKED, true)
                } else {
                    //do stuff when Switch if OFF
                    SharedPref.writeBool(SharedPref.USER_APP_LOCK_SETTING_STATUS, false)
                    lytAppSettings.setVisibility(View.GONE)
                    SharedPref.writeBool(SharedPref.USER_IS_UNLOCKED, false)
                }
            }
        }
        setFont()
    }

    private fun setFont() {
        setTypefaceView(
            txtLockSettingsHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtChangePasswordHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtTouchIdHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtChangePasswordValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtTouchIdValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtImmediatelyFilter, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtAfter5minutesFilter,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtAfter30minutesFilter,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtEnableLock, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }
}