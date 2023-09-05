package zeemart.asia.buyers.deals

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants

class AboutDealsActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageButton
    private lateinit var webViewAboutDeals: WebView
    private var txtAboutDeals: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_deals)
        btnBack = findViewById(R.id.btn_back_about_deals)
        txtAboutDeals = findViewById(R.id.txt_about_deals_header_name)
        setTypefaceView(txtAboutDeals, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        webViewAboutDeals = findViewById(R.id.web_view_about_deals)
        val settings = webViewAboutDeals.getSettings()
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        webViewAboutDeals.loadUrl("https://www.zeemart.asia/about-deals")
        btnBack.setOnClickListener(View.OnClickListener { finish() })
    }
}