package zeemart.asia.buyers

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.GsonBuilder
import com.onesignal.OneSignal
import com.stripe.android.BuildConfig
import io.intercom.android.sdk.Intercom
import org.json.JSONArray
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AppUpdateHelper
import zeemart.asia.buyers.helper.MixPanelHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.login.BuyerLoginActivity
import zeemart.asia.buyers.models.UnitSizeModel
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

/**
 * Created by saiful on 22/9/17.
 * Applcation class for the Zeemart buyer version
 */
class ZeemartBuyerApp : Application(), LifecycleObserver {
    /**
     * Constructor.
     */
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        SharedPref.init(applicationContext)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance()
            .setUserId("" + SharedPref.read(SharedPref.USER_EMAIL_ID, ""))
        Intercom.initialize(
            this,
            "android_sdk-8489ca5c3a3f6865ef14e664df8dd2e738bdece4",
            "lzmzad7p"
        )
        SharedPref.registerIntercomUser()
        if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
        }
        if (!StringHelper.isStringNullOrEmpty(
                SharedPref.read(
                    SharedPref.NOTIFICATION_DEVICE_TOKEN,
                    ""
                )
            )
        ) {
            MixPanelHelper.registerFCM(
                applicationContext,
                SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")
            )
        }
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(oneSignalPublicKey)
        OneSignal.setNotificationOpenedHandler { result ->
            if (StringHelper.isStringNullOrEmpty(result.notification.launchURL)) {
                if (!StringHelper.isStringNullOrEmpty(
                        SharedPref.read(
                            SharedPref.PASSWORD_ENCRYPTED,
                            ""
                        )
                    )
                ) {
                    val intent = Intent(applicationContext, BaseNavigationActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    val intent = Intent(applicationContext, BuyerLoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            } else {
                val uri = Uri.parse(result.notification.launchURL)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        AppUpdateHelper(applicationContext).checkAppUpdate()
    }

    companion object {
        @JvmField
        val gsonExposeExclusive = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        private lateinit var instance: ZeemartBuyerApp

        /**
         * get the application context.
         *
         * @return returns the static instance of the application class.
         */
        @JvmStatic
        val context: Context
            get() {
                return instance
            }

        @JvmStatic
        fun <T> fromJson(json: String?, c: Class<T>): T? {
            return try {
                gsonExposeExclusive.fromJson(json, c)
            } catch (e: java.lang.Exception) {
                Log.e("Exception", e.message!!)
                FirebaseCrashlytics.getInstance()
                    .recordException(java.lang.Exception(ServiceConstant.CrashlyticsCustomErrorCode.FROM_GSON_FAIL.toString() + " " + c.name + " " + e.message))
                null
            }
        }

//        @JvmStatic
//        fun <T : Any> fromJson(json: String?, c: Class<T>): BuyerOnBoardingCredentialsResponse {
//            return try { gsonExposeExclusive.fromJson(json, c) }
//            catch (e: Exception) {
//                Log.e("Exception", e.message!!)
//                FirebaseCrashlytics.getInstance()
//                    .recordException(Exception(ServiceConstant.CrashlyticsCustomErrorCode.FROM_GSON_FAIL.toString() + " " + c.name + " " + e.message))
//                null
//            }
//        }

        @JvmStatic
        fun <T> fromJsonList(json: String?, c: Class<T>, type: Type?): List<T>? {
            return try {
                gsonExposeExclusive.fromJson(json, type)
            } catch (e: Exception) {
                Log.e("Exception e", e.message!!)
                FirebaseCrashlytics.getInstance()
                    .recordException(Exception(ServiceConstant.CrashlyticsCustomErrorCode.FROM_GSON_FAIL.toString() + " " + c.name + " " + e.message))
                try {
                    val list: MutableList<T> = ArrayList()
                    val jsonArray = JSONArray(json)
                    var i = 0
                    while (i < jsonArray.length()) {
                        val t = fromJson(
                            jsonArray.getJSONObject(i).toString(), c
                        )
                        if (t != null) {
                            list.add(t)
                        }
                        i++
                    }
                    return list
                } catch (e1: Exception) {
                    Log.e("Exception e1", e.message!!)
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception(ServiceConstant.CrashlyticsCustomErrorCode.FROM_GSON_FAIL.toString() + " " + c.name + " " + e.message))
                }
                null
            }
        }

        @JvmStatic
        fun getProperty(key: String?): String {
            return try {
                val properties = Properties()
                val assetManager = context.assets
                var inputStream: InputStream? = null
//                if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
//                    inputStream = assetManager.open("config_dev.properties")
//                }
//                if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
//                    inputStream = assetManager.open("config_prod.properties")
//                }
                inputStream = assetManager.open("config_dev.properties")
//                if (inputStream == null){
//                    return ""
//                }
                properties.load(inputStream)
                properties.getProperty(key)
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }
        }

        @JvmStatic
        val stripePublicKey: String
            get() {
                var publicKey = ""
                if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
                    publicKey = "pk_test_jF9rfoc6kzE48IBxVj6RQLcB00dQxOffcX"
                } else if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
                    publicKey = "pk_live_golUdnh5Mw8oFHWpkakMHNzL"
                }
                return publicKey
            }
        @JvmStatic
        val mixPanelPublicKey: String
            get() {
                var publicKey = ""
                if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
                    publicKey = "631c420d9210fe925f95572ba3823cc8"
                } else if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
                    publicKey = "0dafeb14d13646fff36034f9a6294025"
                }
                return publicKey
            }
        val oneSignalPublicKey: String
            get() {
                var publicKey = ""
                if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
                    publicKey = "354099eb-14e8-4f7c-b1e1-ce498694d019"
                } else if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
                    publicKey = "552772b5-fb68-4e10-9f05-efe5b293eb7c"
                }
                return publicKey
            }

        /**
         * Displays the red toast
         *
         * @param str, take message string as the parameter
         */
        @JvmStatic
        fun getToastRed(str: String?) {
            val inflater = context!!.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.toast_red, null)
            val text = layout.findViewById<TextView>(R.id.text)
            text.text = str
            val toast = Toast(context)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layout
            toast.show()
        }

        /**
         * Displays the green toast
         *
         * @param str, takes message string as the parameter
         */
        @JvmStatic
        fun getToastGreen(str: String?) {
            val inflater = context!!.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.toast_green, null)
            val text = layout.findViewById<TextView>(R.id.text)
            text.text = str
            val toast = Toast(context)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layout
            toast.show()
        }

        /**
         * sets the mentioned typeface for a particular view
         *
         * @param view
         * @param typefaceName
         */
        @JvmStatic
        fun setTypefaceView(view: View?, typefaceName: String?) {
            if (view != null && typefaceName != null) {
                var typeface: Typeface? = null
                if (typefaceName == ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD) {
                    typeface = Typeface.createFromAsset(
                        context!!.assets,
                        "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
                    )
                } else if (typefaceName == ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR) {
                    typeface = Typeface.createFromAsset(
                        context!!.assets,
                        "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
                    )
                } else if (typefaceName == ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD) {
                    typeface = Typeface.createFromAsset(
                        context!!.assets,
                        "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
                    )
                } else if (typefaceName == ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS) {
                    typeface = Typeface.createFromAsset(
                        context!!.assets,
                        "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
                    )
                }
                if (view is TextView) {
                    view.typeface = typeface
                } else if (view is EditText) {
                    view.typeface = typeface
                } else if (view is Button) {
                    view.typeface = typeface
                }
            }
        }

        /**
         * getPriceDouble off a double to the sepecified number of places in
         * ceiling mode if the number has more than 2 decimal places
         * eg. 32.222 will be rounded of to 32.23
         *
         * @param value
         * @param places
         * @return
         */
        @JvmStatic
        fun getPriceDouble(value: Double, places: Int): Double {
            require(places >= 0)
            return try {
                val decimalNumber = java.lang.Double.toString(value)
                val integerPlaces = decimalNumber.indexOf(".")
                val decimalPlaces = decimalNumber.length - integerPlaces - 1
                if (decimalPlaces <= places) {
                    var bd = BigDecimal(value)
                    bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP)
                    //Log.d("ROUNDED OFF VALUE",bd+"**********"+bd.doubleValue());
                    bd.toDouble()
                } else {
                    var bd = BigDecimal(value)
                    bd = bd.setScale(places, RoundingMode.HALF_UP)
                    val df = DecimalFormat("#.00")
                    df.format(bd)
                    //Log.d("ROUNDED OFF VALUE",bd.doubleValue()+"**********");
                    bd.toDouble()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                value
            }
        }

        @JvmStatic
        fun getDoubleToString(value: Double?): String {
            requireNotNull(value)
            return if (value != null && value % 1 == 0.0) {
                Integer.toString(value.toInt())
            } else UnitSizeModel.getValueDecimal(value)
        }

        /**
         *
         */
        @JvmStatic
        fun getTwoDecimalQuantity(price: Double): String {
            return if (price == 0.0) "0.00" else String.format(Locale("en", "sg"), "%.2f", price)
        }

        /**
         * Return dollar value for the cents
         *
         * @param price
         * @return
         */
        fun centsToDollar(price: Double?): Double {
            var priceDollar = 0.0
            if (price != null) {
                priceDollar = price / 100.00
            }
            return priceDollar
        }

        fun DollarToCent(price: Double?): Long {
            var priceCent = 0L
            if (price != null) {
                priceCent = (price * 100.toLong()).toLong()
            }
            return priceCent
        }

        @JvmStatic
        fun roundUpDoubleToLong(value: Double): Double {
            return Math.ceil(value)
        }

        @JvmStatic
        fun ChangeLanguageLocale(context: Context, locale: Locale?) {
            val configuration: Configuration
            configuration = Configuration(context.resources.configuration)
            configuration.locale = locale
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }
    }
}