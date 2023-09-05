package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.json.JSONException
import org.json.JSONObject
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper

import zeemart.asia.buyers.models.BadRequest
import zeemart.asia.buyers.models.BuyerCredentials
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.network.LoginApi.LoginBuyerListener
import java.io.*
import java.nio.charset.Charset

/**
 * Created by RajPrudhviMarella on 16/June/2021.
 */
open class ZeemartMultipartAPIRequestForMultipleFiles(
    private val context: Context,
    method: Int,
    url: String?,
    private val mListener: Response.Listener<NetworkResponse>,
    private val mErrorListener: Response.ErrorListener,
    private val requestHeaders: MutableMap<String, String?>
) : Request<NetworkResponse>(method, url, mErrorListener) {
    private val twoHyphens: String = "--"
    private val lineEnd: String = "\r\n"
    private val boundary: String = "apiclient-" + System.currentTimeMillis()
    var refreshToken: Boolean = false

    init {
        setCrashlyticsLogs()
    }

    fun setCrashlyticsLogs() {
        try {
            var headersLog: String = ""
            var paramsLog: String = ""
            var contentType: String = ""
            val requestBodyLog: String = ""
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
            FirebaseCrashlytics.getInstance()
                .log(getUrl() + " " + headersLog + " " + paramsLog + " " + contentType + " " + requestBodyLog)
        } catch (authFailureError: AuthFailureError) {
            authFailureError.printStackTrace()
        }
    }

    @Throws(AuthFailureError::class)
    public override fun getHeaders(): MutableMap<String, String> {
        val mHeaders: MutableMap<String, String> = HashMap<String, String>()
        mHeaders[ServiceConstant.REQUEST_BODY_HEADER_KEY_AUTH_TYPE] = ServiceConstant.REQUEST_BODY_HEADER_KEY_AUTH_TYPE_VALUE
        mHeaders[ServiceConstant.REQUEST_BODY_HEADER_KEY_CONTENT_TYPE] = bodyContentType
        if (!StringHelper.isStringNullOrEmpty(SharedPref.read(SharedPref.ACCESS_TOKEN, ""))) {
            mHeaders[ServiceConstant.REQUEST_BODY_AUTH_TOKEN_HEADER] =
                SharedPref.read(SharedPref.ACCESS_TOKEN, "")!!
        }
        for (key: String in requestHeaders.keys) {
            //iterate over key
            requestHeaders[key]?.let { mHeaders.put(key, it) }
        }
        return mHeaders
    }

    public override fun getBodyContentType(): String {
        return "multipart/form-data;boundary=" + boundary
    }

    @Throws(AuthFailureError::class)
    public override fun getBody(): ByteArray? {
        val bos: ByteArrayOutputStream = ByteArrayOutputStream()
        val dos: DataOutputStream = DataOutputStream(bos)
        try {
            // populate text payload
            val params: Map<String, String>? = getParams()
            if (params != null && params.size > 0) {
                textParse(dos, params, getParamsEncoding())
            }

            // populate data byte payload
            val data: Map<String, ArrayList<DataPart>>? = getByteData()
            if (data != null && data.size > 0) {
                dataParse(dos, data)
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
            return bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(AuthFailureError::class)
    protected open fun getByteData(): Map<String, ArrayList<DataPart>>? {
        return null
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        try {
            return Response.success(
                response,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: Exception) {
            return Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse) {
        mListener.onResponse(response)
    }

    public override fun deliverError(error: VolleyError) {
        if (error != null && error.networkResponse != null) {
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
        if (error != null) {
            if (error.networkResponse != null) {
                val response: NetworkResponse = error.networkResponse
                val statusCode: Int = response.statusCode
                val mError: VolleyErrorHelper = VolleyErrorHelper("", "", response)
                try {
                    if (statusCode == ServiceConstant.STATUS_CODE_401_UNAUTHORIZED) {
                        if (!StringHelper.isStringNullOrEmpty(
                                SharedPref.read(
                                    SharedPref.PASSWORD_ENCRYPTED,
                                    ""
                                )
                            )
                        ) {
                            //call the login API again to fetch the new Access Token
                            if (refreshToken) {
                                super.deliverError(mError)
                            } else {
                                getNewAccessToken()
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
                            Charset.forName(HttpHeaderParser.parseCharset(
                                response.headers,
                                ServiceConstant.DEFAULT_CHARSET
                            ))
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
                        val jsonString = String(
                            response.data,
                            Charset.forName(HttpHeaderParser.parseCharset(
                                response.headers,
                                ServiceConstant.DEFAULT_CHARSET
                            ))
                        )
                        Log.e("400", jsonString)
                        val badRequest: BadRequest = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            jsonString,
                            BadRequest::class.java
                        )
                        mError.errorMessage = (badRequest.message.toString())
                        mError.errorType = (ServiceConstant.DISPLAY_APP_SPECIFIC_400_MESSAGE)
                        super.deliverError(mError)
                        getToastRed(badRequest.message)
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
            }
        }
    }

    @Throws(IOException::class)
    private fun textParse(
        dataOutputStream: DataOutputStream,
        params: Map<String, String>,
        encoding: String
    ) {
        try {
            for (entry: Map.Entry<String, String> in params.entries) {
                buildTextPart(dataOutputStream, entry.key, entry.value)
            }
        } catch (uee: UnsupportedEncodingException) {
            throw RuntimeException("Encoding not supported: " + encoding, uee)
        }
    }

    @Throws(IOException::class)
    private fun dataParse(
        dataOutputStream: DataOutputStream,
        data: Map<String, ArrayList<DataPart>>
    ) {
        for (entry: Map.Entry<String, ArrayList<DataPart>> in data.entries) {
            buildDataPart(dataOutputStream, entry.value, entry.key)
        }
    }

    @Throws(IOException::class)
    private fun buildTextPart(
        dataOutputStream: DataOutputStream,
        parameterName: String,
        parameterValue: String
    ) {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd)
        //dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
        dataOutputStream.writeBytes(lineEnd)
        dataOutputStream.writeBytes(parameterValue + lineEnd)
    }

    @Throws(IOException::class)
    private fun buildDataPart(
        dataOutputStream: DataOutputStream,
        dataFile: ArrayList<DataPart>,
        inputName: String
    ) {
        for (i in dataFile.indices) {
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
            val dp: DataPart = dataFile.get(i)
            dataOutputStream.writeBytes(
                ("Content-Disposition: form-data; name=\"" +
                        inputName + "\"; filename=\"" + dp.getFileName() + "\"" + lineEnd)
            )
            if (dp.getType() != null && !dp.getType()!!.trim({ it <= ' ' }).isEmpty()) {
                dataOutputStream.writeBytes("Content-Type: " + dp.getType() + lineEnd)
            }
            dataOutputStream.writeBytes(lineEnd)
            val fileInputStream: ByteArrayInputStream = ByteArrayInputStream(dp.getContent())
            var bytesAvailable: Int = fileInputStream.available()
            val maxBufferSize: Int = 1024 * 1024
            var bufferSize: Int = Math.min(bytesAvailable, maxBufferSize)
            val buffer: ByteArray = ByteArray(bufferSize)
            var bytesRead: Int = fileInputStream.read(buffer, 0, bufferSize)
            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bufferSize)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            }
            dataOutputStream.writeBytes(lineEnd)
        }
    }

    inner class DataPart {
        private var fileName: String? = null
        private lateinit var content: ByteArray
        private var type: String? = null

        constructor() {}
        constructor(fileName: String?, content: ByteArray, type: String?) {
            this.fileName = fileName
            this.content = content
            this.type = type
        }

        constructor(name: String?, data: ByteArray) {
            fileName = name
            content = data
        }

        fun getFileName(): String? {
            return fileName
        }

        fun getContent(): ByteArray {
            return content
        }

        fun getType(): String? {
            return type
        }
    }

    /**
     * call the login API again to fetch the new Access Token
     */
    private fun getNewAccessToken() {
        val credentials: BuyerCredentials = BuyerCredentials(
            SharedPref.read(SharedPref.USERNAME, "")!!,
            SharedPref.read(SharedPref.PASSWORD_ENCRYPTED, "")!!
        )
        LoginApi.loginBuyer(context, credentials, object : LoginBuyerListener {
            public override fun onSuccessResponse(buyerDetails: BuyerDetails) {
                SharedPref.write(SharedPref.ACCESS_TOKEN, buyerDetails.mudra)
                SharedPref.write(
                    SharedPref.BUYER_DETAIL,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(buyerDetails)
                )
                VolleyRequest.Companion.getInstance(
                    context
                )!!
                    .addToRequestQueue<NetworkResponse>(this@ZeemartMultipartAPIRequestForMultipleFiles)
            }

            public override fun onErrorResponse(error: VolleyErrorHelper?) {
                CommonMethods.unregisterLogout(context)
            }
        })
    }
}