package zeemart.asia.buyers.helper

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import zeemart.asia.buyers.BuildConfig
import zeemart.asia.buyers.constants.MarketConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper

/**
 * Created by ParulBhandari on 3/7/2018.
 */
class AppUpdateHelper(var context: Context) {
    /**
     * this method is called everytime app comes to the foreground.
     */
    fun checkAppUpdate() {
        val forceUpdateVersionAndroid = "" + FirebaseRemoteConfig.getInstance()
            .getString(MarketConstants.FORCE_UPDATE_VERSION_ANDROID)
        val marketAppVersion = "" + FirebaseRemoteConfig.getInstance()
            .getString(MarketConstants.GOOGLE_PLAY_APP_VERSION)
        val currentAppVersion = BuildConfig.VERSION_NAME
        if (!StringHelper.isStringNullOrEmpty(forceUpdateVersionAndroid) && !StringHelper.isStringNullOrEmpty(
                currentAppVersion
            ) && !StringHelper.isStringNullOrEmpty(marketAppVersion)
        ) {
            val forceUpdateVersionAndroidDouble = forceUpdateVersionAndroid.toDouble()
            val currentAppVersionDouble = currentAppVersion.toDouble()
            val marketAppVersionDouble = marketAppVersion.toDouble()
            Log.d(
                "checkAppUpdate",
                "$forceUpdateVersionAndroidDouble***$currentAppVersionDouble###$marketAppVersionDouble"
            )
            if (currentAppVersionDouble < forceUpdateVersionAndroidDouble) {
                SharedPref.writeBool(
                    ZeemartAppConstants.DISPLAY_FORCE_UPDATE_DIALOG_IS_VISIBLE,
                    true
                )
            } else if (currentAppVersionDouble < marketAppVersionDouble) {
                SharedPref.writeBool(ZeemartAppConstants.DISPLAY_UPDATE_DIALOG_IS_VISIBLE, true)
            }
        }
    }
}