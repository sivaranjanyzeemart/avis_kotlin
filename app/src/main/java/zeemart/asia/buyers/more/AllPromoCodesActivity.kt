package zeemart.asia.buyers.more

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.AllPromoCodesListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.modelsimport.RetrieveMultiplePromoCodes
import zeemart.asia.buyers.network.PromoCodeApi.MultiplePromoCodesResponseListener
import zeemart.asia.buyers.network.PromoCodeApi.retrieveMultiplePromotionCodes

class AllPromoCodesActivity : AppCompatActivity() {
    private var txtPromoCodesHeader: TextView? = null
    private lateinit var btnBack: ImageButton
    private lateinit var lstAllPromoCodes: RecyclerView
    private lateinit var lytNoPromoCodes: LinearLayout
    private var txtNoPromoCodes: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_all_promo_codes)
        txtPromoCodesHeader = findViewById(R.id.txt_promo_codes_header)
        btnBack = findViewById(R.id.btn_promo_codes_button_move_back)
        lstAllPromoCodes = findViewById(R.id.lst_all_promo_codes)
        lstAllPromoCodes.setLayoutManager(LinearLayoutManager(this))
        lytNoPromoCodes = findViewById(R.id.lyt_no_promo_codes)
        txtNoPromoCodes = findViewById(R.id.txt_no_promo_codes)
        lytNoPromoCodes.setVisibility(View.GONE)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        allPromoCodeData
        setFont()
    }

    private val allPromoCodeData: Unit
        private get() {
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setSortBy(ApiParamsHelper.SortField.TIME_CREATED)
            apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
            apiParamsHelper.setStatus(ApiParamsHelper.Status.ACTIVE)
            retrieveMultiplePromotionCodes(
                this,
                apiParamsHelper,
                SharedPref.allOutlets,
                object : MultiplePromoCodesResponseListener {
                    override fun onSuccessResponse(response: RetrieveMultiplePromoCodes.Response?) {
                        if (response != null && response.data != null && response.data!!.promoCodeList != null && response.data!!.promoCodeList!!.size > 0) {
                            lytNoPromoCodes!!.visibility = View.GONE
                            val allPromoCodes = response.data!!.promoCodeList
                            lstAllPromoCodes!!.adapter = AllPromoCodesListAdapter(
                                this@AllPromoCodesActivity,
                                allPromoCodes!!
                            )
                        } else {
                            lytNoPromoCodes!!.visibility = View.VISIBLE
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        lytNoPromoCodes!!.visibility = View.VISIBLE
                    }
                })
        }

    private fun setFont() {
        setTypefaceView(txtPromoCodesHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtNoPromoCodes, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }
}