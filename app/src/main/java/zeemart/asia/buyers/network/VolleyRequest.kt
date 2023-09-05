package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import zeemart.asia.buyers.BuildConfig
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.context
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.CustomHurlStack
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

/**
 * Created by parul
 * Singleton class for Volley Request
 */
class VolleyRequest(internal var mCtx: Context?) {
    private var mRequestQueue: RequestQueue?
//    private var mCtx : Context

    init {
        mRequestQueue = getRequestQueue()

//        this@VolleyRequest.mCtx = mCtx!!
    }

    fun getRequestQueue(): RequestQueue? {
        if (mRequestQueue == null) {
            if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.PRODUCTION)) {
                mRequestQueue = Volley.newRequestQueue(
                    mCtx?.applicationContext,
                    CustomHurlStack(object : CustomHurlStack.UrlRewriter {
                        override fun rewriteUrl(originalUrl: String?): String? {
                            var url = originalUrl
                            if (originalUrl!!.contains("storage.googleapis.com")) {
                                url = originalUrl.replace("https", "http")
                            }
                            return url
                        }
                    }, socketFactory)
                )
            }
            if (BuildConfig.BUILD_TYPE.contains(ZeemartAppConstants.Environment.DEVELOPMENT)) {
                mRequestQueue = Volley.newRequestQueue(
                    mCtx?.applicationContext,
                    CustomHurlStack()
                )
            }
        }
        return mRequestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>?) {
        /**
         * check if there is internet connection then add the request to the request queue
         */
        if (CommonMethods.checkInternetConnection(mCtx?.getApplicationContext()!!)) {
            getRequestQueue()!!.add(req)
        } else {
            if (!toastShown) {
                getToastRed(mCtx?.getString(R.string.txt_no_internet))
                toastShown = true
            }
        }
    }//HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

    /**
     * generate the SLL certificate for the
     * @return
     */
    private val socketFactory: SSLSocketFactory?
        private get() {
            var cf: CertificateFactory? = null
            try {
                cf = CertificateFactory.getInstance("X.509")
                val caInput: InputStream = mCtx?.getResources()!!.openRawResource(R.raw.zeemart_site)
                val ca: Certificate
                try {
                    ca = cf.generateCertificate(caInput)
                    Log.e(
                        "CERT",
                        "ca=" + (ca as X509Certificate).subjectDN
                    )
                } finally {
                    caInput.close()
                }
                val keyStoreType = KeyStore.getDefaultType()
                val keyStore =
                    KeyStore.getInstance(keyStoreType)
                keyStore.load(null, null)
                keyStore.setCertificateEntry("ca", ca)
                val tmfAlgorithm =
                    TrustManagerFactory.getDefaultAlgorithm()
                val tmf =
                    TrustManagerFactory.getInstance(tmfAlgorithm)
                tmf.init(keyStore)
                var context: SSLContext? = null
                context = SSLContext.getInstance("TLS")
                context.init(null, tmf.trustManagers, null)
                //HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
                return context.socketFactory
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: KeyManagementException) {
                e.printStackTrace()
            }
            return null
        }

    companion object {
        private var mInstance: VolleyRequest? = null
        private var toastShown = false
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context?): VolleyRequest? {
            if (mInstance == null) {
                mInstance = VolleyRequest(context)
            }
            return mInstance
        }
    }
}