package zeemart.asia.buyers.helper

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.HttpStack
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse


/**
 * Created by ParulBhandari on 3/21/2018.
 */
class CustomHurlStack
/**
 * @param urlRewriter Rewriter to use for request URLs
 */ @JvmOverloads constructor(
    private val mUrlRewriter: UrlRewriter? = null,
    private val mSslSocketFactory: SSLSocketFactory? = null,
) :
    HttpStack {
    /**
     * An interface for transforming URLs before use.
     */
    interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        fun rewriteUrl(originalUrl: String?): String?
    }

    /**
     * @param mUrlRewriter Rewriter to use for request URLs
     * @param mSslSocketFactory SSL factory to use for HTTPS connections
     */
    @Throws(IOException::class, AuthFailureError::class)
    override fun performRequest(
        request: Request<*>,
        additionalHeaders: Map<String, String>,
    ): HttpResponse {
        var url = request.url
        val map = HashMap<String, String>()
        map.putAll(request.headers)
        map.putAll(additionalHeaders)
        if (mUrlRewriter != null) {
            val rewritten = mUrlRewriter.rewriteUrl(url)
                ?: throw IOException("URL blocked by rewriter: $url")
            url = rewritten
        }
        val parsedUrl = URL(url)
        val connection = openConnection(parsedUrl, request)
        for (headerName in map.keys) {
            connection.addRequestProperty(headerName, map[headerName])
        }
        setConnectionParametersForRequest(connection, request)
        // Initialize HttpResponse with data from the HttpURLConnection.
        val protocolVersion: org.apache.http.ProtocolVersion =
            org.apache.http.ProtocolVersion("HTTP", 1, 1)
        val responseCode = connection.responseCode
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw IOException("Could not retrieve response code from HttpUrlConnection.")
        }
        val responseStatus: org.apache.http.StatusLine = org.apache.http.message.BasicStatusLine(
            protocolVersion,
            connection.responseCode, connection.responseMessage
        )
        val response: org.apache.http.message.BasicHttpResponse =
            org.apache.http.message.BasicHttpResponse(responseStatus)
        response.setEntity(entityFromConnection(connection))
        for ((key, value) in connection.headerFields) {
            if (key != null) {
                val h: org.apache.http.Header = org.apache.http.message.BasicHeader(
                    key,
                    value[0]
                )
                response.addHeader(h)
            }
        }
        return response
    }

    /**
     * Create an [HttpURLConnection] for the specified `url`.
     */
    @Throws(IOException::class)
    protected fun createConnection(url: URL): HttpURLConnection {
        return url.openConnection() as HttpURLConnection
    }

    /**
     * Opens an [HttpURLConnection] with parameters.
     * @param url
     * @return an open connection
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun openConnection(url: URL, request: Request<*>): HttpURLConnection {
        val connection = createConnection(url)
        val timeoutMs = request.timeoutMs
        connection.connectTimeout = timeoutMs
        connection.readTimeout = timeoutMs
        connection.useCaches = false
        connection.doInput = true

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https" == url.protocol && mSslSocketFactory != null) {
            (connection as HttpsURLConnection).sslSocketFactory = mSslSocketFactory
        }
        return connection
    }

    companion object {
        /**
         * Initializes an [HttpEntity] from the given [HttpURLConnection].
         * @param connection
         * @return an HttpEntity populated with data from `connection`.
         */
        private fun entityFromConnection(connection: HttpURLConnection): org.apache.http.HttpEntity {
            val entity: org.apache.http.entity.BasicHttpEntity =
                org.apache.http.entity.BasicHttpEntity()
            val inputStream: InputStream?
            inputStream = try {
                connection.inputStream
            } catch (ioe: IOException) {
                connection.errorStream
            }
            entity.setContent(inputStream)
            entity.setContentLength(connection.contentLength.toLong())
            entity.setContentEncoding(connection.contentEncoding)
            entity.setContentType(connection.contentType)
            return entity
        }

        @Throws(IOException::class, AuthFailureError::class)
        fun setConnectionParametersForRequest(
            connection: HttpURLConnection,
            request: Request<*>,
        ) {
            when (request.method) {
                Request.Method.DEPRECATED_GET_OR_POST -> {
                    // This is the deprecated way that needs to be handled for backwards compatibility.
                    // If the request's post body is null, then the assumption is that the request is
                    // GET.  Otherwise, it is assumed that the request is a POST.
                    val postBody = request.postBody
                    if (postBody != null) {
                        // Prepare output. There is no need to set Content-Length explicitly,
                        // since this is handled by HttpURLConnection using the size of the prepared
                        // output stream.
                        connection.doOutput = true
                        connection.requestMethod = "POST"
                        val out = DataOutputStream(connection.outputStream)
                        out.write(postBody)
                        out.close()
                    }
                }
                Request.Method.GET ->                 // Not necessary to set the request method because connection defaults to GET but
                    // being explicit here.
                    connection.requestMethod = "GET"
                Request.Method.DELETE -> {
                    connection.requestMethod = "DELETE"
                    addBodyIfExists(
                        connection,
                        request
                    ) // 2015/11/06 | For API19- ProtocolException ("DELETE does not support writing") will be thrown
                }
                Request.Method.POST -> {
                    connection.requestMethod = "POST"
                    addBodyIfExists(connection, request)
                }
                Request.Method.PUT -> {
                    connection.requestMethod = "PUT"
                    addBodyIfExists(connection, request)
                }
                Request.Method.HEAD -> connection.requestMethod = "HEAD"
                Request.Method.OPTIONS -> connection.requestMethod = "OPTIONS"
                Request.Method.TRACE -> connection.requestMethod = "TRACE"
                Request.Method.PATCH -> {
                    connection.requestMethod = "PATCH"
                    addBodyIfExists(connection, request)
                }
                else -> throw IllegalStateException("Unknown method type.")
            }
        }

        @Throws(IOException::class, AuthFailureError::class)
        private fun addBodyIfExists(connection: HttpURLConnection, request: Request<*>) {
            val body = request.body!!
            connection.doOutput = true
            val out = DataOutputStream(connection.outputStream)
            out.write(body)
            out.close()
        }
    }
}