package zeemart.asia.buyers.sharedpref

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MultipartBody

object SharedDataUtils {
    /**
     * Save preferences by using string value * * @param context The context of calling an getActivity * @param key The key to store in the preference * @param value The value to store for the key
     * @return
     */
    fun savePreferences(context: Context?, key: String, value: String?): String {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.apply()
        return key
    }

    /**
     * Save the preferences by using int value * * @param context The context of calling an getActivity * @param key The key is used for to store the value to preference * @param value The value used to store for key value
     */
    fun savePreferences(context: Context?, key: String?, value: Int) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * Save preferences using the boolean value * * @param context The context of calling activity * @param key The key to be stored from the preference * @param value The value to be stored for the key.
     */
    fun savePreferences(context: Context?, key: String?, value: Boolean) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * Read preferences. * * @param context The context of calling an activity * @param key The key to be read from the preference * @param defaultValue The default value if it is nil. * @return the string
     */
    fun getPreferences(context: Context?, key: String?, defaultValue: String?): String? {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getString(key, defaultValue)
    }

    /**
     * Read preferences value. * * @param context The context of calling an activity * @param key The key to be read from the preference * @param defaultValue The default value if it is nil. * @return the string
     */
    fun getPreferences(context: Context?, key: String?, defaultValue: Int): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getInt(key, defaultValue)
    }

    /**
     * Gets the preferences. * * @param context The context of calling an activity * @param key The key to get the string value from the preference * @param defaultValue The default value if it is nil * @return the preferences
     */
    fun getPreferences(context: Context?, key: String?, defaultValue: Boolean): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(key, defaultValue)
    }

    /**
     * Clear preferences value. * * @param context The context of calling activity * @param key The key to be read in the preference
     */
    fun clearPreferences(context: Context?, key: String?) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.remove(key)
        editor.apply()
    }

    /**
     * Request the permission for higher version * * @param activity Get the Activity from the class * @param permission Permission get from the manifest * @param requestCode To handle the call back function by using this code
     */
    fun requestPermission(activity: Activity?, permission: String, requestCode: Int?) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(permission), requestCode!!)
    }

    fun <T> setList(context: Context?, key: String?, list: List<MultipartBody.Part?>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        SharedDataUtils[context, key] = json
    }

    operator fun set(context: Context?, key: String?, value: String?) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getList(context: Context?, key: String?): List<MultipartBody.Part?>? {
        var arrayItems: List<MultipartBody.Part?>? = null
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val serializedObject = sp.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type = object : TypeToken<List<MultipartBody.Part?>?>() {}.type
            arrayItems = gson.fromJson<List<MultipartBody.Part?>>(serializedObject, type)
        }
        return arrayItems
    }

    fun <T> setList(context: Context?, key: String?, list: ArrayList<String?>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        setImageList(context, key, json)
    }

    fun setImageList(context: Context?, key: String?, value: String?) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getImageList(context: Context?, key: String?): ArrayList<String?>? {
        var arrayItems: ArrayList<String?>? = null
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val serializedObject = sp.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<String?>?>() {}.type
            arrayItems = gson.fromJson(serializedObject, type)
        }
        return arrayItems
    }
}