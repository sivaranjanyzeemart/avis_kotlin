package zeemart.asia.buyers.adapter

import android.app.AlertDialog
import android.content.Context
import android.net.http.SslError
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Target
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants

class CertificationSliderAdapter(
    private val context: Context, //private InvoiceMgr invoiceMgr;
    private val imageUrls: List<String>?
) : PagerAdapter() {
    private val image: Target? = null
    override fun getCount(): Int {
        return imageUrls?.size ?: 1
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout =
            LayoutInflater.from(context).inflate(R.layout.layout_image_swipe, container, false)
        val webView = imageLayout.findViewById<WebView>(R.id.webview_product)
        val more_btn = imageLayout.findViewById<ImageView>(R.id.more_btn)
        val pageNumber = imageLayout.findViewById<TextView>(R.id.pageNumber)
        setTypefaceView(pageNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        val resourceUrl = imageUrls!![position]
        if (imageUrls.size > 1) {
            pageNumber.text = String.format(
                context.getString(R.string.format_txt_page_number),
                "" + (position + 1),
                "" + imageUrls.size
            )
        } else {
            pageNumber.text = "1 of 1"
        }
        if (resourceUrl.contains(ServiceConstant.FILE_EXT_PDF)) {
            val settings = webView.settings
            settings.javaScriptEnabled = true
            webView.settings.pluginState = WebSettings.PluginState.ON
            webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            webView.loadUrl(ServiceConstant.ENDPOINT_GOOGLE_DOC_EMBED + resourceUrl)
        } else {
            val settings = webView.settings
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(
                    view: WebView,
                    handler: SslErrorHandler,
                    error: SslError
                ) {
                    val builder = AlertDialog.Builder(
                        context
                    )
                    var message = context.getString(R.string.ssl_txt_ssl_cert_error_title)
                    when (error.primaryError) {
                        SslError.SSL_UNTRUSTED -> message =
                            context.getString(R.string.ssl_txt_ca_not_trusted)
                        SslError.SSL_EXPIRED -> message =
                            context.getString(R.string.ssl_txt_cert_expired)
                        SslError.SSL_IDMISMATCH -> message =
                            context.getString(R.string.ssl_txt_cert_hostname_mismatch)
                        SslError.SSL_NOTYETVALID -> message =
                            context.getString(R.string.ssl_txt_cert_invalid)
                        else -> {}
                    }
                    message += context.getString(R.string.ssl_txt_do_you_want_to_continue)
                    builder.setTitle(context.getString(R.string.ssl_txt_ssl_cert_error_title))
                    builder.setMessage(message)
                    builder.setPositiveButton(R.string.ssl_txt_continue) { dialog, which -> handler.proceed() }
                    builder.setNegativeButton(R.string.txt_cancel) { dialog, which -> handler.cancel() }
                    val dialog = builder.create()
                    dialog.show()
                }
            }
            webView.loadUrl(resourceUrl)
        }
        imageLayout.isFocusableInTouchMode = true
        imageLayout.requestFocus()

        //To access local variable - create temp variable
        val finalResourceUrl = resourceUrl
        more_btn.visibility = View.GONE
        container.addView(imageLayout, 0)
        return imageLayout
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}