package zeemart.asia.buyers.UserAgreementAndTermsPolicy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.userAgreement.Agreements

/**
 * Created by RajPrudhvi on 23/06/2020.
 */
/**
 * A simple [Fragment] subclass.
 * Use the [UserAgreementTermsOfUseWebFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserAgreementTermsOfUseWebFragment : Fragment() {
    private lateinit var webView: WebView
    private var lstAgreements: Agreements? = null
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            lstAgreements = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                requireArguments().getString(ZeemartAppConstants.USER_AGREEMENT), Agreements::class.java
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =
            inflater.inflate(R.layout.fragment_user_agreement_terms_of_use_web, container, false)
        webView = v.findViewById(R.id.web_view_terms_of_use)
        progressBar = v.findViewById(R.id.terms_progress_bar)
        progressBar.setVisibility(View.VISIBLE)
        if (lstAgreements != null && lstAgreements!!.data != null && lstAgreements!!.data?.buyer != null && lstAgreements!!.data?.buyer?.current != null && lstAgreements!!.data?.buyer?.current?.term != null) {
            val settings = webView.getSettings()
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.domStorageEnabled = true
            webView.setWebChromeClient(WebChromeClient())
            webView.setWebViewClient(WebViewClient())
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            checkPageFinished()
        } else {
            progressBar.setVisibility(View.GONE)
        }
        return v
    }

    fun checkPageFinished() {
        progressBar!!.visibility = View.VISIBLE
        //If view is blank:
        if (webView!!.contentHeight == 0) {

            //Run off main thread to control delay
            webView!!.postDelayed(
                { //Load url into the "WebView"
                    webView!!.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" + lstAgreements!!.data?.buyer?.current?.term?.files!![0])
                    progressBar!!.visibility = View.GONE
                }, //Set 1s delay to give the view a longer chance to load before
                // setting the view (or more likely to display blank)
                1000
            )
            //Set the view with the selected pdf
            webView!!.postDelayed(
                {
                    //If view is still blank:
                    if (webView!!.contentHeight == 0) {
                        //Loop until it works
                        checkPageFinished()
                    }
                }, //Safely loop this function after 1.5s delay if page is not loaded
                1500
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment UserAgreementTermsOfUseWebFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String?): UserAgreementTermsOfUseWebFragment {
            val fragment = UserAgreementTermsOfUseWebFragment()
            val args = Bundle()
            args.putString(ZeemartAppConstants.USER_AGREEMENT, param1)
            fragment.arguments = args
            return fragment
        }
    }
}