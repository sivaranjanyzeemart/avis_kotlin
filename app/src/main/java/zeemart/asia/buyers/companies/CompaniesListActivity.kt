package zeemart.asia.buyers.companies

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.CompaniesListAdapter
import zeemart.asia.buyers.adapter.CompaniesListAdapter.CompanySelectedListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.Companies
import zeemart.asia.buyers.models.OrderPayments.UploadTransactionImage
import zeemart.asia.buyers.models.RetrieveCompaniesResponse
import zeemart.asia.buyers.more.PromoCodeDetailsActivity
import zeemart.asia.buyers.network.CompaniesApi.CompaniesResponseListener
import zeemart.asia.buyers.network.CompaniesApi.retrieveCompanies

class CompaniesListActivity : AppCompatActivity() {
    private lateinit var lstCompanies: RecyclerView
    private lateinit var btnBack: ImageButton
    private var txtManageCompanies: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companies_list)
        btnBack = findViewById(R.id.btn_back_companies)
        lstCompanies = findViewById(R.id.lst_companies)
        txtManageCompanies = findViewById(R.id.txt_manage_companies)
        setTypefaceView(txtManageCompanies, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        lstCompanies.setLayoutManager(LinearLayoutManager(this))
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        companies
    }

    private fun setAdapterCompaniesList(companies: List<Companies>) {
        /*List<PromoListUI> promoCodes = new ArrayList<>();
        promoCodes.add(promoListUIData);*/
        if (lstCompanies!!.adapter != null) {
            lstCompanies!!.adapter!!.notifyDataSetChanged()
        } else {
            lstCompanies!!.adapter = CompaniesListAdapter(
                this@CompaniesListActivity,
                companies,
                object : CompanySelectedListener {
                    override fun onViewCompanyDetailsClicked(companies: Companies?) {
                        val intent =
                            Intent(this@CompaniesListActivity, PromoCodeDetailsActivity::class.java)
                    }
                })
        }
    }

    private val companies: Unit
        private get() {
            val companyIds = CommonMethods.commaSepratedCompanyList ?: return
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setCompanyId(companyIds)
            retrieveCompanies(
                this,
                apiParamsHelper,
                SharedPref.allOutlets,
                object : CompaniesResponseListener {
                    override fun onSuccessResponse(response: RetrieveCompaniesResponse.Response?) {
                        if (response != null && response.data != null && response.data!!.companies != null && response.data!!.companies!!.size > 0) {
                            val companies = response.data!!.companies
                            lstCompanies!!.adapter = CompaniesListAdapter(
                                this@CompaniesListActivity,
                                companies!!,
                                object : CompanySelectedListener {
                                    override fun onViewCompanyDetailsClicked(companies: Companies?) {
                                        createDialog(companies)
                                    }
                                })
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {}
                })
        }

    private fun createDialog(companies: Companies?) {
        val d = Dialog(this@CompaniesListActivity, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_companies_actions)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val companyName = d.findViewById<TextView>(R.id.txt_company_name)
        val txtRequestVerify = d.findViewById<TextView>(R.id.txt_request_verify)
        val txtViewOutlet = d.findViewById<TextView>(R.id.txt_view_outlet)
        val txtGrabFinanceHeader = d.findViewById<TextView>(R.id.txt_grab_finance)
        val txtGrabFinanceValue = d.findViewById<TextView>(R.id.txt_amount)
        val lytGrabPayment = d.findViewById<RelativeLayout>(R.id.lyt_grab_payment)
        companyName.text = companies!!.name
        if (companies != null && companies.settings != null && companies.settings!!.payments != null && companies.settings!!.payments!!.paymentSources != null && companies.settings!!.payments!!.paymentSources!!.size > 0) for (i in companies.settings!!.payments!!.paymentSources!!.indices) {
            if (companies.settings!!.payments!!.paymentSources!![i].paymentType.equals(
                    UploadTransactionImage.PaymentType.GRAB_FINANCE.name,
                    ignoreCase = true
                )
            ) {
                if (companies.settings!!.payments!!.paymentSources!![i].status.equals(
                        "ACTIVE",
                        ignoreCase = true
                    )
                ) {
                    lytGrabPayment.visibility = View.VISIBLE
                    txtGrabFinanceValue.text = String.format(
                        resources.getString(R.string.txt_credit_limit_grab_finance),
                        companies.settings!!.payments!!.paymentSources!![i].approvedLimit!!.displayValue
                    )
                } else {
                    lytGrabPayment.visibility = View.GONE
                }
            }
        }
        setTypefaceView(companyName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtViewOutlet, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtRequestVerify, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtGrabFinanceHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtGrabFinanceValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        val requestVerify = d.findViewById<RelativeLayout>(R.id.lyt_reuest_verify)
        val lineSeparator = d.findViewById<RelativeLayout>(R.id.line_separator)
        requestVerify.setOnClickListener {
            openCompanyVerify(companies)
            d.dismiss()
        }
        if (companies.verificationStatus == "unVerified") {
            requestVerify.visibility = View.VISIBLE
            lineSeparator.visibility = View.VISIBLE
        } else {
            requestVerify.visibility = View.GONE
            lineSeparator.visibility = View.GONE
        }
        val viewOutlet = d.findViewById<RelativeLayout>(R.id.lyt_view_outlet)
        viewOutlet.setOnClickListener {
            openOutlets(companies)
            d.dismiss()
        }
        d.show()
    }

    fun openOutlets(company: Companies?) {
        val newIntent = Intent(this, ViewOutletActivity::class.java)
        newIntent.putExtra(
            ZeemartAppConstants.COMPANY,
            ZeemartBuyerApp.gsonExposeExclusive.toJson(company)
        )
        startActivity(newIntent)
    }

    fun openCompanyVerify(company: Companies?) {
        val newIntent = Intent(this, CompanyVerificationRequestActivity::class.java)
        newIntent.putExtra(
            ZeemartAppConstants.COMPANY,
            ZeemartBuyerApp.gsonExposeExclusive.toJson(company)
        )
        startActivity(newIntent)
    }

    public override fun onResume() {
        super.onResume()
        companies
    }
}