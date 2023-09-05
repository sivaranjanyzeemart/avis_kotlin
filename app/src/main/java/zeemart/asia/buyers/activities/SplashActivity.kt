package zeemart.asia.buyers.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.BuildConfig
import zeemart.asia.buyers.R
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.login.BuyerLoginActivity
import zeemart.asia.buyers.selfOnBoarding.WelcomeScreenActivity

/**
 * Created by RajPrudhviMarella on 29/sep/2020.
 */
class SplashActivity : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
                moveToWelcomeScreen()
            } else if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
                checkCountry()
            } else {
                checkCountry()
            }
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }

    private fun checkCountry() {
        val telMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (telMgr != null) {
            val simState = telMgr.simState
            when (simState) {
                TelephonyManager.SIM_STATE_ABSENT -> moveToLoginScreen()
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
                            moveToWelcomeScreen()
                        } else {
                            moveToLoginScreen()
                        }
                    }
                }
            }
        } else {
            moveToLoginScreen()
        }
    }

    private fun moveToWelcomeScreen() {
        val newIntent = Intent(this@SplashActivity, WelcomeScreenActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(newIntent)
        finish()
    }

    private fun moveToLoginScreen() {
        val newIntent = Intent(this@SplashActivity, BuyerLoginActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(newIntent)
        finish()
    }
}