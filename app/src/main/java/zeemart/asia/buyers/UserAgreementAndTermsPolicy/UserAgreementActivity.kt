package zeemart.asia.buyers.UserAgreementAndTermsPolicy

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.android.volley.VolleyError
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.adapter.UserAgreementViewPagerAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.models.userAgreement.Agreements
import zeemart.asia.buyers.models.userAgreement.ValidateAgreements
import zeemart.asia.buyers.network.UserAgreement
import zeemart.asia.buyers.network.UserAgreement.AcceptAgreementListener

/**
 * Created by RajPrudhvi on 15/06/2020.
 */
class UserAgreementActivity : AppCompatActivity() {
    private lateinit var lytAgreeTermsButton: RelativeLayout
    private lateinit var lytCancelAndLogoutButton: RelativeLayout
    private var txtAgreeTerms: TextView? = null
    private var txtCancelAndLogout: TextView? = null
    private var txtUserAgreementHeader: TextView? = null
    private var txtUserAgreementTerms: TextView? = null
    private var lstAgreements: Agreements? = null
    private var lstValidateAgreements: ValidateAgreements? = null
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_agreement)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.USER_AGREEMENT)) {
                lstAgreements = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.USER_AGREEMENT), Agreements::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.USER_VALID_AGREEMENT)) {
                lstValidateAgreements = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.USER_VALID_AGREEMENT),
                    ValidateAgreements::class.java
                )
            }
        }
        lytAgreeTermsButton = findViewById(R.id.lyt_agree_and_continue)
        lytCancelAndLogoutButton = findViewById(R.id.lyt_logout_and_cancel)
        txtAgreeTerms = findViewById(R.id.txt_agree_and_continue)
        txtCancelAndLogout = findViewById(R.id.txt_cancel_and_logout)
        txtUserAgreementHeader = findViewById(R.id.txt_user_agreement_header)
        txtUserAgreementTerms = findViewById(R.id.txt_user_agreement_terms)
        setFont()
        lytAgreeTermsButton.setOnClickListener(View.OnClickListener {
            lstValidateAgreements?.let { it1 ->
                UserAgreement.acceptAgreement(
                    this@UserAgreementActivity,
                    it1,
                    object : AcceptAgreementListener {
                        override fun onErrorResponse(error: VolleyError?) {
                            startActivity(
                                Intent(
                                    this@UserAgreementActivity,
                                    BaseNavigationActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            )
                            finish()
                        }

                        override fun onSuccessResponse(response: String?) {
                            finish()
                        }
                    })
            }
        })
        lytCancelAndLogoutButton.setOnClickListener(View.OnClickListener { dialogConfirmLogout() })
        viewPager = findViewById(R.id.user_agreement_view_pager)
        if (lstAgreements != null) {
            Log.e(
                "agreements",
                "onCreate: " + ZeemartBuyerApp.gsonExposeExclusive.toJson(lstAgreements)
            )
            viewPager.setAdapter(
                UserAgreementViewPagerAdapter(
                    this@UserAgreementActivity,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(lstAgreements),
                    supportFragmentManager
                )
            )
        }
        tabLayout = findViewById(R.id.user_agreement_tab_layout)
        tabLayout.setupWithViewPager(viewPager)
        CommonMethods.setFontForTabs(tabLayout)
    }

    private fun setFont() {
        setTypefaceView(txtAgreeTerms, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtCancelAndLogout, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtUserAgreementHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtUserAgreementTerms, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    private fun dialogConfirmLogout() {
        val builder = AlertDialog.Builder(this@UserAgreementActivity)
        builder.setPositiveButton(R.string.dialog_ok_button_text) { dialog, which ->
            CommonMethods.unregisterLogout(this@UserAgreementActivity)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.dialog_cancel_button_text) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setTitle(getString(R.string.txt_logout_title))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(getString(R.string.txt_logout_confirmation))
        dialog.show()
    }
}