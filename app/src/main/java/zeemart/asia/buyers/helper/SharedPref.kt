package zeemart.asia.buyers.helper

import android.app.Activity
import android.content.*
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UserAttributes
import io.intercom.android.sdk.identity.Registration
import org.json.JSONObject
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.marketlist.Product
import java.lang.reflect.Type
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by Parul Bhandari
 */
object SharedPref {
    val USER_PHONE_NUMBER: String = "USER_PHONE_NUMBER"
    val NOTIFICATION_DEVICE_TOKEN: String = "NOTIFICATION_DEVICE_TOKEN"
    val OUTLET_FILTER_LIST: String = "OUTLET_FILTER_LIST"
    val SUPPLIER_FILTERS_LIST: String = "SUPPLIER_FILTERS_LIST"
    val UNIT_SIZE_MAP: String = "UNIT_SIZE_MAP"
    val UNIT_SIZE_LIST: String = "UNIT_SIZE_LIST"
    val ORDER_FILTER_STATUS_LIST: String = "ORDER_FILTER_STATUS_LIST"
    val FAVORITE_ITEMS_MAP: String = "FAVORITE_ITEM_MAP"
    val DELIVERY_STATUS_FILTER_LIST: String = "DELIVERY_STATUS_FILTER_LIST"
    val INVOICE_STATUS_FILTER_LIST: String = "INVOICE_STATUS_FILTER_LIST"
    val ORDER_TYPE_FILTER_LIST: String = "ORDER_TYPE_FILTER_LIST"
    val FILTER_ORDERS_FROM_DATE: String = "FILTER_ORDERS_FROM_DATE"
    val FILTER_ORDER_UNTIL_DATE: String = "FILTER_ORDER_UNTIL_DATE"
    val SHELVE_FILTER_LIST: String = "SHELVE_FILTER_LIST"
    val STOCK_TYPES: String = "STOCK_TYPES"
    private var mSharedPref: SharedPreferences? = null
    val USERNAME: String = "USERNAME"
    val PASSWORD_ENCRYPTED: String = "PASSWORD_ENCRYPTED"
    val ACCESS_TOKEN: String = "ACCESS_TOKEN"
    val DISPLAY_USE_FILTER_FOR_INVOICED_POPUP: String = "DISPLAY_USE_FILTER_FOR_INVOICED_POPUP"
    val INVOICE_ID_DELETE: String = "INVOICE_ID_DELETE"
    val BUYER_DETAIL: String = "BUYER_DETAIL"
    val SELECTED_OUTLET_NAME: String = "SELECTED_OUTLET_NAME"
    val SELECTED_OUTLET_ID: String = "SELECTED_OUTLET_ID"
    val USER_EMAIL_ID: String = "USER_EMAIL_ID"
    val USER_FIRST_NAME: String = "USER_FIRST_NAME"
    val USER_ROLE_GROUP: String = "USER_ROLE_GROUP"
    val USER_LAST_NAME: String = "USER_LAST_NAME"
    val USER_ID: String = "USER_ID"
    val ANDROID_ID: String = "ANDROID_ID"
    val TOTAL_SUCCESSFUL_PLACED_ORDERS: String = "TOTAL_SUCCESSFUL_PLACED_ORDERS"
    val USER_DELIVERY_ADDRESS: String = "USER_DELIVERY_ADDRESS"
    val USER_DELIVERY_NAME: String = "USER_DELIVERY_NAME"
    val USER_PIN_CODE: String = "USER_PIN_CODE"
    val USER_IMAGE_URL: String = "USER_IMAGE_URL"
    val USER_NOTIFICATION_SETTING_STATUS: String = "USER_NOTIFICATION_SETTING_STATUS"
    val USER_APP_LOCK_SETTING_STATUS: String = "USER_APP_LOCK_SETTING_STATUS"
    val USER_APP_LOCK_SETTING_FILTER_VALUE: String = "USER_APP_LOCK_SETTING_FILTER_VALUE"
    val USER_APP_LOCK_SETTING_TIME_VALUE: String = "USER_APP_LOCK_SETTING_TIME_VALUE"
    val USER_IS_IN_BACKGROUND: String = "USER_IS_IN_BACKGROUND"
    val USER_IS_UNLOCKED: String = "USER_IS_UNLOCKED"
    val USER_INVENTORY_SETTING_STATUS: String = "USER_INVENTORY_SETTING_STATUS"
    val USER_REPORTS_SETTING_STATUS: String = "USER_REPORTS_SETTING_STATUS"
    val SELECTED_LANGUAGE: String = "SELECTED_LANGUAGE"
    val REPORT_RECENT_SEARCH: String = "REPORT_RECENT_SEARCH"
    val FLASH_INVOICE_UPLOAD: String = "FLASH_INVOICE_UPLOAD"
    val AUTO_CROP_INVOICE_UPLOAD: String = "AUTO_CROP_INVOICE_UPLOAD"
    val ORDER_ITEMS_COUNT: String = "ORDER_ITEMS_COUNT"
    val ORDER_BY_PRODUCT_RECENT_SEARCH: String = "ORDER_BY_PRODUCT_RECENT_SEARCH"
    val DISPLAY_SHOW_CASE_FOR_NEW_ORDER: String = "DISPLAY_SHOW_CASE_FOR_NEW_ORDER"
    val DISPLAY_BUBBLE_INVOICE: String = "DISPLAY_BUBBLE_INVOICE"
    val COMPANIES: String = "COMPANIES"

    /**
     * initialize the shared Preference
     *
     * @param context
     */
    fun init(context: Context) {
        if (SharedPref.mSharedPref == null) SharedPref.mSharedPref =
            context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE)
    }

    /**
     * Read from shared preerence
     *
     * @param key
     * @param defValue
     * @return
     */
    fun read(key: String?, defValue: String?): String? {
        return SharedPref.mSharedPref?.getString(key, defValue)
    }

    fun readInt(key: String?, defValue: Int): Int? {
        return SharedPref.mSharedPref?.getInt(key, defValue)
    }

    fun readBool(key: String?, defValue: Boolean): Boolean? {
        return SharedPref.mSharedPref?.getBoolean(key, defValue)
    }

    fun readLong(key: String?, defValue: Long): Long? {
        return SharedPref.mSharedPref?.getLong(key, defValue)
    }

    /**
     * Write to shared preference
     *
     * @param key
     * @param value
     */
    fun write(key: String?, value: String?) {
        val prefsEditor: SharedPreferences.Editor? = SharedPref.mSharedPref?.edit()
        prefsEditor?.putString(key, value)
        prefsEditor?.commit()
    }

    fun writeLong(key: String?, `val`: Long) {
        val prefsEditor: SharedPreferences.Editor? = SharedPref.mSharedPref?.edit()
        prefsEditor?.putLong(key, `val`)
        prefsEditor?.commit()
    }

    fun writeBool(key: String?, `val`: Boolean) {
        val prefsEditor: SharedPreferences.Editor? = SharedPref.mSharedPref?.edit()
        prefsEditor?.putBoolean(key, `val`)
        prefsEditor?.commit()
    }

    fun writeInt(key: String?, `val`: Int) {
        val prefsEditor: SharedPreferences.Editor? = SharedPref.mSharedPref?.edit()
        prefsEditor?.putInt(key, `val`)
        prefsEditor?.commit()
    }

    fun removeVal(key: String?) {
        val prefsEditor: SharedPreferences.Editor? = SharedPref.mSharedPref?.edit()
        if (SharedPref.mSharedPref?.contains(key) == true) {
            prefsEditor?.remove(key)
            prefsEditor?.commit()
        }
    }

    val usernameMask: String
        get() {
            val name: StringBuilder = StringBuilder(SharedPref.read(SharedPref.USER_EMAIL_ID, ""))
            for (i in 0 until name.length) {
                if (i % 5 == 0) {
                    name.setCharAt(i, '*')
                }
            }
            return name.toString()
        }

    // returns first outlet in buyer detail if selected outlet is not set
    val defaultOutlet: Outlet?
        get() {
            if (!SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, "")?.isEmpty()!!) {
                if (!SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")?.isEmpty()!!) {
                    val oId: String? = SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
                    val oName: String? = SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, "")
                    val outlet: Outlet = Outlet(oId, oName)
                    return outlet
                }
            }
            if (!SharedPref.read(SharedPref.BUYER_DETAIL, "")?.isEmpty()!!) {
                val buyerDetails: String? = SharedPref.read(SharedPref.BUYER_DETAIL, "")
                val mBuyerDetails: BuyerDetails? = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    buyerDetails,
                    BuyerDetails::class.java
                )
                if ((mBuyerDetails != null) && (mBuyerDetails.outlet != null) && (
                            mBuyerDetails.outlet!!.size > 0)
                ) {
                    return mBuyerDetails.outlet!!.get(0)
                }
            }
            return null
        }
    val buyerDetail: BuyerDetails?
        get() {
            if (!SharedPref.read(SharedPref.BUYER_DETAIL, "")?.isEmpty()!!) {
                val buyerDetails: String? = SharedPref.read(SharedPref.BUYER_DETAIL, "")
                val mBuyerDetails: BuyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    buyerDetails,
                    BuyerDetails::class.java
                )
                return mBuyerDetails
            }
            return null
        }
    val allOutlets: List<Outlet>?
        get() {
            if (!SharedPref.read(SharedPref.BUYER_DETAIL, "")?.isEmpty()!!) {
                val buyerDetails: String? = SharedPref.read(SharedPref.BUYER_DETAIL, "")
                val mBuyerDetails: BuyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    buyerDetails,
                    BuyerDetails::class.java
                )
                return mBuyerDetails.outlet
            }
            return null
        }
    val currentUserDetail: UserDetails
        get() {
            val lastUpdatedBy: UserDetails = UserDetails()
            lastUpdatedBy.firstName = SharedPref.read(SharedPref.USER_FIRST_NAME, "")
            lastUpdatedBy.lastName = SharedPref.read(SharedPref.USER_LAST_NAME, "")
            lastUpdatedBy.id = SharedPref.read(SharedPref.USER_ID, "")
            lastUpdatedBy.phone = SharedPref.read(SharedPref.USER_PHONE_NUMBER, "")
            lastUpdatedBy.imageURL = SharedPref.read(SharedPref.USER_IMAGE_URL, "")
            lastUpdatedBy.doNotSendReports =
                SharedPref.readBool(SharedPref.USER_REPORTS_SETTING_STATUS, false)
            lastUpdatedBy.customNameAndCode =
                SharedPref.readBool(SharedPref.USER_INVENTORY_SETTING_STATUS, false)
            lastUpdatedBy.roleGroup = SharedPref.read(SharedPref.USER_ROLE_GROUP, "")
            lastUpdatedBy.language = SharedPref.read(SharedPref.SELECTED_LANGUAGE, "")
            return lastUpdatedBy
        }
    val userId: String?
        get() {
            try {
                return SharedPref.read(SharedPref.ACCESS_TOKEN, "")?.split("--".toRegex())
                    ?.dropLastWhile({ it.isEmpty() })?.toTypedArray()?.get(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    fun registerIntercomUser() {
        val userId: String? = SharedPref.userId
        if (userId != null) {
            val registration: Registration = Registration.create().withUserId(userId)
            Intercom.client().registerIdentifiedUser(registration)
        }
    }

    fun updateIntercomUser() {
        val userAttributes: UserAttributes = UserAttributes.Builder()
            .withUserId(SharedPref.currentUserDetail.id)
            .withName(SharedPref.currentUserDetail.firstName)
            .withEmail(SharedPref.read(SharedPref.USERNAME, ""))
            .withPhone(SharedPref.currentUserDetail.phone)
            .withCustomAttribute("outlet", SharedPref.defaultOutlet?.outletName)
            .build()
        Intercom.client().updateUser(userAttributes)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun SHA256(text: String): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray())
        val digest: ByteArray = md.digest()
        return Base64.encodeToString(digest, Base64.DEFAULT)
    }

    fun clearAllSharedPreferencesData() {
        //this needs to be maintained even if user logs out
        val deviceToken: String? = SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")
        SharedPref.writeInt(SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS, 0)
        Log.d("", SharedPref.mSharedPref.toString() + "****")
        val prefsEditor: SharedPreferences.Editor? = SharedPref.mSharedPref?.edit()
        prefsEditor?.clear()
        prefsEditor?.commit()
        SharedPref.write(SharedPref.NOTIFICATION_DEVICE_TOKEN, deviceToken)
        prefsEditor?.commit()
        Log.d("", SharedPref.mSharedPref.toString() + "****")
    }

    fun removeString(key: String?) {
        val prefsEditor: SharedPreferences.Editor? = SharedPref.mSharedPref?.edit()
        if (SharedPref.mSharedPref?.contains(key) == true) {
            prefsEditor?.remove(key)
            prefsEditor?.commit()
        }
    }

    fun loadMap(key: String?, context: Context): Map<String, Product> {
        val outputMap: Map<String, Product> = HashMap()
        val pSharedPref: SharedPreferences? =
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
        try {
            if (pSharedPref != null) {
                val jsonString: String? = pSharedPref.getString(key, (JSONObject()).toString())
                val type: Type = object : TypeToken<Map<String?, Product?>?>() {}.getType()
                val gson: Gson = Gson()
                return gson.fromJson(jsonString, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputMap
    }
}