package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.google.gson.JsonSyntaxException
import com.onesignal.OneSignal
import org.json.JSONException
import org.json.JSONObject
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.MixPanelHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.BuyerCredentials
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.models.ViewSpecificUser
import zeemart.asia.buyers.modelsimport.BuyerCreatePasswordResponse
import zeemart.asia.buyers.modelsimport.BuyerOnBoardingCredentialsRequest
import zeemart.asia.buyers.modelsimport.BuyerOnBoardingCredentialsResponse

/**
 * Created by ParulBhandari on 11/9/2017.
 * For Login we can get following getStatusName 200, 403,404,405 and 500
 */
object LoginApi {
    fun validPasswordStrength(pwd: String): Boolean {
        val regex = Regex("^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!#$%&()*+,-./:;<=>?@\\^_`{|}~\"'\\[\\]]).*\$")
        return pwd.matches(regex)
    }

    // api id - 1.1 Login (Buyer)
    fun loginBuyer(
        context: Context?,
        buyerCredentials: BuyerCredentials,
        listener: LoginBuyerListener
    ) {
        var requestBody = ""
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerCredentials))
            requestBody = jsonObject.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val buyerLoginRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_AUTHTOKEN,
            requestBody,
            null,
            { response ->
                try {
                    val buyerDetails: BuyerDetails? =
                        ZeemartBuyerApp.fromJson(response, BuyerDetails::class.java)
                    //Save details in sharedPreference
                    SharedPref.write(SharedPref.USERNAME, buyerCredentials.zeemartId)
                    SharedPref.write(SharedPref.PASSWORD_ENCRYPTED, buyerCredentials.password)
                    SharedPref.write(SharedPref.ACCESS_TOKEN, buyerDetails!!.mudra)
                    SharedPref.write(
                        SharedPref.BUYER_DETAIL,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerDetails)
                    )
                    //call the user profile API and set the data in shared preference
                    UsersApi.viewSpecificUser(
                        context,
                        SharedPref.userId,
                        object : UsersApi.ViewSpecificUserListener {
                            override fun onSuccessResponse(user: ViewSpecificUser?) {
                                SharedPref.write(SharedPref.USER_EMAIL_ID, user!!.data?.email)
                                SharedPref.write(
                                    SharedPref.USER_FIRST_NAME,
                                    user.data?.firstName
                                )
                                SharedPref.write(
                                    SharedPref.USER_ROLE_GROUP,
                                    user.data?.roleGroup
                                )
                                SharedPref.write(SharedPref.USER_LAST_NAME, user.data?.lastName)
                                SharedPref.write(SharedPref.USER_ID, user.data?.userId)
                                SharedPref.write(SharedPref.USER_IMAGE_URL, user.data?.imageURL)
                                SharedPref.write(SharedPref.USER_PHONE_NUMBER, user.data?.phone)
                                user.data?.trackNotificationsStatus?.let {
                                    SharedPref.writeBool(
                                        SharedPref.USER_NOTIFICATION_SETTING_STATUS,
                                        it
                                    )
                                }
                                if (user.data?.settings?.inventorySettings != null && user.data?.settings?.inventorySettings!!.displayCustomName != null) user.data!!.settings?.inventorySettings?.displayCustomName?.let {
                                    SharedPref.writeBool(
                                        SharedPref.USER_INVENTORY_SETTING_STATUS,
                                        it
                                    )
                                }
                                if (user.data?.customNameAndCode != null) user.data!!.customNameAndCode?.let {
                                    SharedPref.writeBool(
                                        SharedPref.USER_INVENTORY_SETTING_STATUS,
                                        it
                                    )
                                }
                                if (user.data?.doNotSendReports != null) user.data!!.doNotSendReports?.let {
                                    SharedPref.writeBool(
                                        SharedPref.USER_REPORTS_SETTING_STATUS,
                                        it
                                    )
                                }
                                if (user.data?.language != null) {
                                    if (user.data!!.language == ZeemartAppConstants.Language.ENGLISH.locLangCode) {
                                        SharedPref.write(
                                            SharedPref.SELECTED_LANGUAGE,
                                            ZeemartAppConstants.Language.ENGLISH.langCode
                                        )
                                    } else if (user.data!!.language == ZeemartAppConstants.Language.BAHASA_INDONESIA.locLangCode) {
                                        SharedPref.write(
                                            SharedPref.SELECTED_LANGUAGE,
                                            ZeemartAppConstants.Language.BAHASA_INDONESIA.langCode
                                        )
                                    } else if (user.data!!.language == ZeemartAppConstants.Language.CHINESE.locLangCode) {
                                        SharedPref.write(
                                            SharedPref.SELECTED_LANGUAGE,
                                            ZeemartAppConstants.Language.CHINESE.langCode
                                        )
                                    } else {
                                        SharedPref.write(
                                            SharedPref.SELECTED_LANGUAGE,
                                            ZeemartAppConstants.Language.ENGLISH.langCode
                                        )
                                    }
                                }
                                listener.onSuccessResponse(buyerDetails)

                                //clevertap update user
                                //     CleverTapHelper.pushProfile(context, viewUser.getData());
                                if (!StringHelper.isStringNullOrEmpty(user.data?.email)) {
                                    user.data?.email?.let { OneSignal.setExternalUserId(it) }
                                } else {
                                    user.data?.phone?.let { OneSignal.setExternalUserId(it) }
                                }
                                if (user.data != null) {
                                    MixPanelHelper.pushProfile(context!!, user.data!!)
                                }
                                // intercom update user
                                SharedPref.registerIntercomUser()
                                SharedPref.updateIntercomUser()

                                //call the register device for notification API
                                PushNotificationApi.registerDeviceUserLoggedIn(
                                    context,
                                    SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")!!
                                )
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper) {
                                Log.d("", error.errorMessage + "*******")
                            }
                        })
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    listener.onErrorResponse(
                        VolleyErrorHelper(
                            ServiceConstant.NO_ACTION,
                            ServiceConstant.NO_ACTION,
                            null
                        )
                    )
                }
            }) { error ->
            error.printStackTrace()
            listener.onErrorResponse(error as VolleyErrorHelper)
        }
        buyerLoginRequest.refreshToken = true
        VolleyRequest.getInstance(context)!!.addToRequestQueue<String>(buyerLoginRequest)
    } // api id - 1.1 Login (Buyer)

    fun signUpBuyer(
        context: Context?,
        buyerCredentials: BuyerOnBoardingCredentialsRequest?,
        listener: SignUpBuyerListener
    ) {
        var requestBody = ""
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerCredentials))
            requestBody = jsonObject.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.e("signUpBuyer", "signUpBuyer: $requestBody")
        val buyerLoginRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_SIGN_UP,
            requestBody,
            null,
            { response ->
                try {
                    val buyerOnBoardingCredentialsResponse: BuyerOnBoardingCredentialsResponse? = ZeemartBuyerApp.fromJson(
                        response, BuyerOnBoardingCredentialsResponse::class.java)
                    listener.onSuccessResponse(buyerOnBoardingCredentialsResponse)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    listener.onErrorResponse(
                        VolleyErrorHelper(
                            ServiceConstant.NO_ACTION,
                            ServiceConstant.NO_ACTION,
                            null
                        )
                    )
                }
            }) { error ->
            error.printStackTrace()
            listener.onErrorResponse(error as VolleyErrorHelper)
        }
        buyerLoginRequest.refreshToken = true
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(buyerLoginRequest)
    }

    // api id - 1.4 Change Password (Supplier/Buyer)
    fun changePassword(
        context: Context?,
        oldPassword: String?,
        newPassword: String?,
        listener: GetRequestStatusResponseListener
    ) {
        val updatePassword = oldPassword?.let {
            if (newPassword != null) {
                BuyerCredentials.UpdatePassword(it, newPassword)
            }
        }
        val requestBody: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(updatePassword)
        val changePasswordRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_CHANGEPASSWORD,
            requestBody,
            CommonMethods.headerAllOutlets,
            { response -> listener.onSuccessResponse(response) }) { error ->
            listener.onErrorResponse(
                error as VolleyErrorHelper
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(changePasswordRequest)
    }

    //   api id - 1.7 Reset Password (Buyer)
    fun resetPassword(
        context: Context?,
        zeemartId: String?,
        verificationCode: String?,
        newPassword: String?,
        listener: GetRequestStatusResponseListener
    ) {
        val resetPassword =
            verificationCode?.let { BuyerCredentials.ResetPassword(it, newPassword!!, zeemartId!!) }
        val requestBody: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(resetPassword)
        Log.e("reset password", "resetPassword: $requestBody")
        val resetPasswordRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_RESETPASSWORD,
            requestBody,
            null,
            { response -> listener.onSuccessResponse(response) }) { error ->
            listener.onErrorResponse(
                error as VolleyErrorHelper
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(resetPasswordRequest)
    } //   api id - 1.7 Reset Password (Buyer)

    fun createPassword(
        context: Context?,
        userId: String?,
        buyerCreatePasswordResponse: BuyerCreatePasswordResponse?,
        listener: GetRequestStatusResponseListener
    ) {
        val requestBody: String =
            ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerCreatePasswordResponse)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setUserId(userId!!)
        Log.e("CreatePassword", "CreatePassword: $requestBody")
        val resetPasswordRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_CREATE_PASSWORD),
            requestBody,
            null,
            { response -> listener.onSuccessResponse(response) }) { error ->
            listener.onErrorResponse(
                error as VolleyErrorHelper
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(resetPasswordRequest)
    }

    //   api id - 1.5 Forgot Password (Supplier/Buyer)
    fun forgotPassword(
        context: Context?,
        zeemartId: String?,
        listener: GetRequestStatusResponseListener
    ) {
        val forgotPassword = zeemartId?.let { BuyerCredentials.ForgotPassword(it) }
        val requestBody: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(forgotPassword)
        Log.e("forgot password", "forgotPassword: $requestBody")
        val resetPasswordRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            ServiceConstant.ENDPOINT_SENDVC,
            requestBody,
            null,
            { response -> listener.onSuccessResponse(response) }) { error ->
            listener.onErrorResponse(
                error as VolleyErrorHelper
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(resetPasswordRequest)
    } //   api id - 1.5 Forgot Password (Supplier/Buyer)

    fun regenerateVerificationCode(
        context: Context?,
        zeemartId: String?,
        listener: GetRequestStatusResponseListener
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setUserId(zeemartId!!)
        val resetPasswordRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_RE_SEND_VC),
            null,
            null,
            { response -> listener.onSuccessResponse(response) }) { error ->
            listener.onErrorResponse(
                error as VolleyErrorHelper
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(resetPasswordRequest)
    }

    fun createCompanyDetails(
        context: Context?,
        userId: String?,
        requestbody: String,
        listener: GetRequestStatusResponseListener
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setUserId(userId!!)
        apiParamsHelper.setPlatForm(ZeemartAppConstants.ANDROID)
        Log.e("createCompanyDetails", "createCompanyDetails: $requestbody")
        val resetPasswordRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_CREATE_COMPANY_DETAILS),
            requestbody,
            null,
            { response -> listener.onSuccessResponse(response) }) { error ->
            listener.onErrorResponse(
                error as VolleyErrorHelper
            )
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(resetPasswordRequest)
    }

    // api id - 1.6 Validate Verification Code(Buyer)
    fun validateVerificationCode(
        context: Context?,
        zeemartId: String?,
        passCode: String?,
        requestBy: String?,
        listener: GetRequestStatusResponseListener
    ) {
        val validateCode = zeemartId?.let { BuyerCredentials.ValidateCode(it, passCode!!, requestBy!!) }
        val requestBody: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(validateCode)
        Log.e("validateVerification", "validateVerificationCode: $requestBody")
        val resetPasswordRequest: ZeemartAPIRequest = object : ZeemartAPIRequest(
            context,
            Method.POST,
            ServiceConstant.ENDPOINT_VALIDATEVC,
            requestBody,
            null,
            Response.Listener<String> { response -> listener.onSuccessResponse(response) },
            Response.ErrorListener { error -> listener.onErrorResponse(error as VolleyErrorHelper) }) {
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return super.getBody()
            }
        }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(resetPasswordRequest)
    }

    interface LoginBuyerListener {
        fun onSuccessResponse(buyerDetails: BuyerDetails)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    interface SignUpBuyerListener {
        fun onSuccessResponse(buyerDetails: BuyerOnBoardingCredentialsResponse?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }
}