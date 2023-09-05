package zeemart.asia.buyers.helper

import android.content.Context
import android.provider.Settings
import com.mixpanel.android.mpmetrics.MixpanelAPI
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.mixPanelPublicKey
import zeemart.asia.buyers.models.ViewSpecificUser

/**
 * Created by RajPrudhviMarella on 22/Sep/2021.
 */
object MixPanelHelper {
    var mixpanel: MixpanelAPI? = null
    fun pushProfile(context: Context, userData: ViewSpecificUser.Data) {
        MixPanelHelper.mixpanel = MixpanelAPI.getInstance(context, mixPanelPublicKey)
        MixPanelHelper.mixpanel!!.identify(userData.userId)
        MixPanelHelper.mixpanel!!.getPeople().identify(userData.userId)
        val people: MixpanelAPI.People = MixPanelHelper.mixpanel!!.getPeople()
        people.set("\$first_name", userData.firstName)
        people.set("\$last_name", userData.lastName)
        people.set("\$email", userData.email)
        people.set("UserType", userData.roleGroup)
        people.set("Market", userData.market)
        people.set("Phone", userData.phone)
        if (StringHelper.isStringNullOrEmpty(SharedPref.read(SharedPref.ANDROID_ID, ""))) {
            val m_androidId: String =
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
            SharedPref.write(SharedPref.ANDROID_ID, m_androidId)
            people.set("DeviceID", m_androidId)
        }
        people.set(
            "selfSignUp",
            SharedPref.readBool(SharedPref.DISPLAY_USE_FILTER_FOR_INVOICED_POPUP, false)
        )
        if (!StringHelper.isStringNullOrEmpty(
                SharedPref.read(
                    SharedPref.USER_PIN_CODE,
                    ""
                )
            )
        ) people.set("UserPostcode", SharedPref.read(SharedPref.USER_PIN_CODE, ""))
        val BusinessType: StringBuilder = StringBuilder()
        if (userData.outletFeatures != null && userData.outletFeatures!!.businessType != null) {
            for (i in userData.outletFeatures!!.businessType!!.indices) {
                if (i != userData.outletFeatures!!.businessType!!.size - 1) {
                    BusinessType.append(userData.outletFeatures!!.businessType!!.get(i).name)
                        .append(", ")
                } else {
                    BusinessType.append(userData.outletFeatures!!.businessType!!.get(i).name)
                }
            }
        }
        val cousineType: StringBuilder = StringBuilder()
        if (userData.outletFeatures != null && userData.outletFeatures!!.cuisineType != null) {
            for (i in userData.outletFeatures!!.cuisineType!!.indices) {
                if (i != userData.outletFeatures!!.cuisineType!!.size - 1) {
                    cousineType.append(userData.outletFeatures!!.cuisineType!!.get(i).name)
                        .append(", ")
                } else {
                    cousineType.append(userData.outletFeatures!!.cuisineType!!.get(i).name)
                }
            }
        }
        val cousineFeatures: StringBuilder = StringBuilder()
        if (userData.outletFeatures != null && userData.outletFeatures!!.cuisineFeatures != null) {
            for (i in userData.outletFeatures!!.cuisineFeatures!!.indices) {
                if (i != userData.outletFeatures!!.cuisineFeatures!!.size - 1) {
                    cousineFeatures.append(userData.outletFeatures!!.cuisineFeatures!!.get(i).name)
                        .append(", ")
                } else {
                    cousineFeatures.append(userData.outletFeatures!!.cuisineFeatures!!.get(i).name)
                }
            }
        }
        people.set("BusinessType", BusinessType)
        people.set("Cuisine", cousineType)
        people.set("Feature", cousineFeatures)
    }

    fun registerFCM(context: Context?, token: String?) {
        MixPanelHelper.mixpanel = MixpanelAPI.getInstance(context, mixPanelPublicKey)
        MixPanelHelper.mixpanel!!.getPeople().setPushRegistrationId(token)
    }

    fun pushEvent(context: Context?, action: String?) {
        MixPanelHelper.mixpanel = MixpanelAPI.getInstance(context, mixPanelPublicKey)
        MixPanelHelper.mixpanel!!.track(action)
    }

    fun pushEvent(context: Context?, action: String?, properties: Map<String?, Any?>?) {
        MixPanelHelper.mixpanel = MixpanelAPI.getInstance(context, mixPanelPublicKey)
        MixPanelHelper.mixpanel!!.trackMap(action, properties)
    }
}