package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.json.JSONException
import org.json.JSONObject
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.DebugConstants
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.BadRequest
import zeemart.asia.buyers.models.BuyerCredentials
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.models.orderimportimport.DraftErrorModel
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * Created by ParulBhandari on 11/22/2017.
 */
open class ZeemartAPIRequest : Request<String> {
    private var mListener: Response.Listener<String>
    private var errorListener: Response.ErrorListener
    private var requestBody: String?
    private var requestHeaders: Map<String, String>?
    private var context: Context?
    var refreshToken: Boolean = false
    private var draftErrorListener: DraftErrorListener? = null

    /**
     * @param method
     * @param url
     * @param requestBody
     * @param headers
     * @param responseListener
     * @param errorListener
     */
    constructor(
        context: Context?,
        method: Int,
        url: String?,
        requestBody: String?,
        headers: Map<String, String>?,
        responseListener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) : super(method, url, errorListener) {
        mListener = responseListener
        this.errorListener = errorListener
        this.requestBody = requestBody
        requestHeaders = headers
        this.context = context
        //set crashlytics logs
        setCrashlyticsLogs()
    }

    /**
     * @param method
     * @param url
     * @param requestBody
     * @param headers
     * @param responseListener
     * @param errorListener
     */
    constructor(
        context: Context?,
        method: Int,
        url: String?,
        requestBody: String?,
        headers: Map<String, String>?,
        responseListener: Response.Listener<String>,
        errorResponseListener: Response.ErrorListener,
        errorListener: DraftErrorListener?
    ) : super(method, url, errorResponseListener) {
        mListener = responseListener
        draftErrorListener = errorListener
        this.errorListener = errorResponseListener
        this.requestBody = requestBody
        requestHeaders = headers
        this.context = context
        //set crashlytics logs
        setCrashlyticsLogs()
    }

    fun setCrashlyticsLogs() {
        try {
            var headersLog: String = ""
            var paramsLog: String = ""
            var contentType: String = ""
            var requestBodyLog: String = ""
            if (getHeaders() != null) {
                val maskedHeader: MutableMap<String, String> = getHeaders()
                maskedHeader.put(ServiceConstant.REQUEST_BODY_AUTH_TOKEN_HEADER, "*****")
                headersLog = maskedHeader.toString()
            }
            if (getParams() != null) {
                paramsLog = getParams().toString()
            }
            if (getBodyContentType() != null) {
                contentType = getBodyContentType()
            }
            if (requestBody != null) {
                requestBodyLog = requestBody as String
                requestBodyLog = maskPassword(requestBodyLog, getUrl())
            }
            FirebaseCrashlytics.getInstance()
                .log(getUrl() + " " + headersLog + " " + paramsLog + " " + contentType + " " + requestBodyLog)
        } catch (authFailureError: AuthFailureError) {
            authFailureError.printStackTrace()
        }
    }

    fun maskPassword(requestBodyLog: String, Url: String): String {
        var maskPasswordRequestBody: String = requestBodyLog
        if (Url.contains(ServiceConstant.ENDPOINT_RESETPASSWORD)) {
            val resetPassword: BuyerCredentials.ResetPassword =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<BuyerCredentials.ResetPassword>(
                    maskPasswordRequestBody,
                    BuyerCredentials.ResetPassword::class.java
                )
            resetPassword.newPassword = "*****"
            maskPasswordRequestBody = ZeemartBuyerApp.gsonExposeExclusive.toJson(resetPassword)
        } else if (Url.contains(ServiceConstant.ENDPOINT_CHANGEPASSWORD)) {
            val updatePassword: BuyerCredentials.UpdatePassword =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<BuyerCredentials.UpdatePassword>(
                    maskPasswordRequestBody,
                    BuyerCredentials.UpdatePassword::class.java
                )
            updatePassword.newPassword = "*****"
            updatePassword.password = "*****"
            maskPasswordRequestBody = ZeemartBuyerApp.gsonExposeExclusive.toJson(updatePassword)
        } else if (Url.contains(ServiceConstant.ENDPOINT_AUTHTOKEN)) {
            val buyerCredentials: BuyerCredentials =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<BuyerCredentials>(
                    maskPasswordRequestBody,
                    BuyerCredentials::class.java
                )
            buyerCredentials.password = "*****"
            maskPasswordRequestBody = ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerCredentials)
        }
        return maskPasswordRequestBody
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<String>? {
        Log.d(DebugConstants.ZEEMARTDEBUG, "parse Network response" + String(response.data))
        val statusCode: Int = response.statusCode
        val jsonString: String
        try {
            if ((statusCode == ServiceConstant.STATUS_CODE_200_OK) || (statusCode == ServiceConstant.STATUS_CODE_201_CREATED) || (statusCode == ServiceConstant.STATUS_CODE_202_ACCEPTED)) {
                jsonString = String(
                    response.data,
                    Charset.forName(HttpHeaderParser.parseCharset(response.headers, ServiceConstant.DEFAULT_CHARSET))
                )
                return Response.success<String>(
                    jsonString,
                    HttpHeaderParser.parseCacheHeaders(response)
                )
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return Response.error<String>(
                VolleyErrorHelper(
                    ServiceConstant.NO_ACTION,
                    ServiceConstant.NO_ACTION,
                    null
                )
            )
        }
        return null
    }

    override fun deliverResponse(response: String?) {
        if ((getUrl().contains(ZeemartBuyerApp.getProperty(ServiceConstant.StockManagementServer)) || getUrl().contains(
                ZeemartBuyerApp.getProperty(ServiceConstant.LoyaltyProgramManagementServer)
            )
                    || getUrl().contains(ZeemartBuyerApp.getProperty(ServiceConstant.OrderManagementServer)))
        ) {
            try {
                val status: Int = JSONObject(response!!).getInt(ServiceConstant.JSON_STATUS)
                val message: String = JSONObject(response).getString(ServiceConstant.JSON_MESSAGE)
                if (status != ServiceConstant.STATUS_CODE_200_OK) {
                    if (getUrl().contains(ZeemartBuyerApp.getProperty(ServiceConstant.LoyaltyProgramManagementServer))) {
                        errorListener.onErrorResponse(VolleyError(response))
                    }
                    errorListener.onErrorResponse(VolleyError(status.toString() + " " + message))
                    if (draftErrorListener != null) {
                        val draftErrorModel: DraftErrorModel =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                response,
                                DraftErrorModel::class.java
                            )
                        draftErrorListener!!.errorListener(
                            draftErrorModel,
                            VolleyError(status.toString() + " " + message)
                        )
                    }
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception(status.toString() + " " + message + " " + getUrl()))
                    return
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        mListener.onResponse(response!!)
    }

    public override fun deliverError(error: VolleyError) {
        if (error.networkResponse != null) {
            var message: String = ""
            val response: NetworkResponse = error.networkResponse
            try {
                val jsonString: String = String(
                    response.data,
                    Charset.forName(HttpHeaderParser.parseCharset(response.headers, ServiceConstant.DEFAULT_CHARSET))
                )
                message = " " + JSONObject(jsonString).getString(ServiceConstant.JSON_MESSAGE)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (response.statusCode != ServiceConstant.STATUS_CODE_401_UNAUTHORIZED) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Exception(error.networkResponse.statusCode.toString() + message + " " + getUrl()))
            }
        }
        if (error.networkResponse != null) {
            val response: NetworkResponse = error.networkResponse
            val statusCode: Int = response.statusCode
            Log.e("mystatusCode", statusCode.toString())
            val mError = VolleyErrorHelper("", "", response)
            try {
                if (statusCode == ServiceConstant.STATUS_CODE_401_UNAUTHORIZED) {
                    if (!StringHelper.isStringNullOrEmpty(
                            SharedPref.read(
                                SharedPref.PASSWORD_ENCRYPTED,
                                ""
                            )
                        )
                    ) {
                        if (refreshToken) {
                            super.deliverError(mError)
                        } else {
                            newAccessToken
                        }
                    } else {
                        mError.errorMessage = (ServiceConstant.TRY_LOGIN_AGAIN_401_MESSAGE)
                        mError.errorType = (ServiceConstant.TRY_LOGIN_AGAIN_401_MESSAGE)
                        super.deliverError(mError)
                    }
                } else if ((statusCode == ServiceConstant.STATUS_CODE_403_FORBIDDEN) || (statusCode == ServiceConstant.STATUS_CODE_404_NOT_FOUND) || (
                            statusCode == ServiceConstant.STATUS_CODE_405_METHOD_NOT_ALLOWED)
                ) {
                    //return the error message
                    val jsonString: String = String(
                        response.data,
                        Charset.forName(HttpHeaderParser.parseCharset(response.headers, ServiceConstant.DEFAULT_CHARSET))
                    )
                    val message: String =
                        JSONObject(jsonString).getString(ServiceConstant.JSON_MESSAGE)
                    mError.errorType = (ServiceConstant.STATUS_CODE_403_404_405_MESSAGE)
                    mError.errorMessage = (message)
                    super.deliverError(mError)
                } else if (statusCode == ServiceConstant.STATUS_CODE_500_INTERNAL_SERVER_ERROR) {
                    mError.errorMessage = (ServiceConstant.RETRY_API_CALL__500_MESSAGE)
                    mError.errorType = (ServiceConstant.RETRY_API_CALL__500_MESSAGE)
                    super.deliverError(mError)
                } else if (statusCode == ServiceConstant.STATUS_CODE_400_BAD_REQUEST) {
                    val jsonString: String = String(
                        response.data,
                        Charset.forName(HttpHeaderParser.parseCharset(response.headers, ServiceConstant.DEFAULT_CHARSET))
                    )
                    try {
                        val badRequest: BadRequest? = ZeemartBuyerApp.fromJson<BadRequest>(
                            jsonString,
                            BadRequest::class.java
                        )
                        mError.errorMessage = (badRequest?.message.toString())
                        mError.errorType = (ServiceConstant.DISPLAY_APP_SPECIFIC_400_MESSAGE)
                        super.deliverError(mError)
                        if ((context != null) && (badRequest?.message != null) && (badRequest.message!!.length > 0)) {
                            if (badRequest.title != null) DialogHelper.displayErrorMessageDialog(
                                context!!,
                                badRequest.title,
                                badRequest.message
                            ) else DialogHelper.displayErrorMessageDialog(
                                context!!,
                                "",
                                badRequest.message
                            )
                        }
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance()
                            .recordException(Exception(error.networkResponse.statusCode.toString() + jsonString + " " + getUrl()))
                        e.printStackTrace()
                    }
                } else {
                    super.deliverError(mError)
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                mError.errorMessage = (ServiceConstant.NO_ACTION)
                mError.errorType = (ServiceConstant.NO_ACTION)
                super.deliverError(mError)
            } catch (e: JSONException) {
                e.printStackTrace()
                mError.errorMessage = (ServiceConstant.NO_ACTION)
                mError.errorType = (ServiceConstant.NO_ACTION)
                super.deliverError(mError)
            }
        } else {
            Log.d("ERROR_SSL", getUrl() + "and error: " + error.toString())
            FirebaseCrashlytics.getInstance()
                .recordException(Exception(getUrl() + "and error: " + error.toString()))
        }
    }

    @Throws(AuthFailureError::class)
    public override fun getHeaders(): MutableMap<String, String> {
        val mHeaders: MutableMap<String, String> = HashMap<String, String>()
        mHeaders.put(
            ServiceConstant.REQUEST_BODY_HEADER_KEY_AUTH_TYPE,
            ServiceConstant.REQUEST_BODY_HEADER_KEY_AUTH_TYPE_VALUE
        )
        mHeaders.put(ServiceConstant.REQUEST_BODY_HEADER_KEY_CONTENT_TYPE, getBodyContentType())
        if (!StringHelper.isStringNullOrEmpty(SharedPref.read(SharedPref.ACCESS_TOKEN, ""))) {
            mHeaders.put(
                ServiceConstant.REQUEST_BODY_AUTH_TOKEN_HEADER,
                SharedPref.read(SharedPref.ACCESS_TOKEN, "")!!
            )
        }
        if (requestHeaders != null) {
            for (key: String in requestHeaders!!.keys) {
                //iterate over key
                requestHeaders!!.get(key)?.let { mHeaders.put(key, it) }
            }
        }
        return mHeaders
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        if (StringHelper.isStringNullOrEmpty(requestBody)) {
            return super.getBody() ?: ByteArray(0)
        } else {
            return requestBody?.toByteArray() ?: ByteArray(0)
        }
    }

     override fun getBodyContentType(): String {
        return "application/json"
    }

    /**
     * call the login API again to fetch the new Access Token
     */
    private val newAccessToken: Unit
        private get() {
            val credentials: BuyerCredentials = BuyerCredentials(
                SharedPref.read(SharedPref.USERNAME, "")!!,
                SharedPref.read(SharedPref.PASSWORD_ENCRYPTED, "")!!
            )
            LoginApi.loginBuyer(context, credentials, object : LoginApi.LoginBuyerListener {
                override fun onSuccessResponse(buyerDetails: BuyerDetails) {
                    SharedPref.write(SharedPref.ACCESS_TOKEN, buyerDetails.mudra)
                    SharedPref.write(
                        SharedPref.BUYER_DETAIL,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerDetails)
                    )
                    if (this@ZeemartAPIRequest != null) VolleyRequest.Companion.getInstance(context)!!
                        .addToRequestQueue<String>(this@ZeemartAPIRequest)
                }

                public override fun onErrorResponse(error: VolleyErrorHelper?) {
                    refreshToken = true
                    super@ZeemartAPIRequest.deliverError(error)
                    CommonMethods.unregisterLogout(context)
                }
            })
        }

    public override fun setRetryPolicy(retryPolicy: RetryPolicy): Request<*> {
        val defaultRetryPolicy: DefaultRetryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        return super.setRetryPolicy(defaultRetryPolicy)
    }

    open interface DraftErrorListener {
        fun errorListener(draftErrorModel: DraftErrorModel?, error: VolleyError?)
    }
}