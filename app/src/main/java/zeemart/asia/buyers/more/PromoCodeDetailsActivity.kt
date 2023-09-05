package zeemart.asia.buyers.more

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.VolleyError
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper.alertDialogSmallSuccess
import zeemart.asia.buyers.helper.DialogHelper.displayErrorMessageDialog
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PromoListUI
import zeemart.asia.buyers.models.PromoListUI.PromoDataPassed
import zeemart.asia.buyers.models.RetrieveSpecificPromoCode
import zeemart.asia.buyers.models.ValidatePromoCode
import zeemart.asia.buyers.network.PromoCodeApi.RetrieveSpecificPromoCodeResponseListener
import zeemart.asia.buyers.network.PromoCodeApi.ValidatePromoCodeListener
import zeemart.asia.buyers.network.PromoCodeApi.retrieveSpecificPromoCode
import zeemart.asia.buyers.network.PromoCodeApi.validatePromotionCode

class PromoCodeDetailsActivity : AppCompatActivity() {
    private var txtPromoCodeDetailHeaderText: TextView? = null
    private lateinit var btnClose: ImageButton
    private var txtPromoTitle: TextView? = null
    private var txtPromoDescription: TextView? = null
    private var txtValidityTag: TextView? = null
    private var txtValidityTagValue: TextView? = null
    private var txtForAllOrderTag: TextView? = null
    private var txtForAllOrderTagValue: TextView? = null
    private var txtTermsTag: TextView? = null
    private var txtTermTagValue: TextView? = null
    private lateinit var btnUsePromo: Button
    private var imgPromoImage: ImageView? = null
    private var promoDataPassed: PromoDataPassed? = null
    private lateinit var supplierNamesArray: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_promo_code_details)
        txtPromoCodeDetailHeaderText = findViewById(R.id.txt_promo_header_name)
        btnClose = findViewById(R.id.btn_close_promo_details)
        txtPromoTitle = findViewById(R.id.txt_coupon_detail_title)
        txtPromoDescription = findViewById(R.id.txt_coupon_detail_description)
        txtValidityTagValue = findViewById(R.id.txt_validity_value)
        txtForAllOrderTag = findViewById(R.id.txt_for_all_orders_to_tag)
        txtForAllOrderTagValue = findViewById(R.id.txt_for_all_orders_to_value)
        txtTermsTag = findViewById(R.id.txt_terms_tag)
        txtTermTagValue = findViewById(R.id.txt_terms_value)
        btnUsePromo = findViewById(R.id.btn_use_now)
        txtValidityTag = findViewById(R.id.txt_validity_tag)
        imgPromoImage = findViewById(R.id.img_coupon_image)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(PromoListUI.PROMO_CODE_DETAILS)) {
                promoDataPassed = fromJson(
                    bundle.getString(PromoListUI.PROMO_CODE_DETAILS),
                    PromoDataPassed::class.java
                )
                getPromoDetailData(promoDataPassed!!.promoCodeId, promoDataPassed!!.outletId)
            }
        }
        btnClose.setOnClickListener(View.OnClickListener { finish() })
        if (promoDataPassed != null && promoDataPassed!!.calledFrom == ZeemartAppConstants.ALL_PROMO_CODE_ACTIVITY) {
            btnUsePromo.setText(getString(R.string.txt_copy_code))
            btnUsePromo.setEnabled(true)
            btnUsePromo.setClickable(true)
        } else {
            if (promoDataPassed != null && promoDataPassed!!.isPromoSelected) {
                btnUsePromo.setText(getString(R.string.txt_code_applied))
                btnUsePromo.setEnabled(false)
                btnUsePromo.setClickable(false)
            } else {
                btnUsePromo.setTag(getString(R.string.txt_use_promo))
                btnUsePromo.setEnabled(true)
                btnUsePromo.setClickable(true)
            }
        }
        btnUsePromo.setOnClickListener(View.OnClickListener {
            if (promoDataPassed != null) {
                if (promoDataPassed!!.calledFrom == ZeemartAppConstants.ALL_PROMO_CODE_ACTIVITY) {
                    val clipboard = this@PromoCodeDetailsActivity.getSystemService(
                        CLIPBOARD_SERVICE
                    ) as ClipboardManager
                    val clip = ClipData.newPlainText(
                        promoDataPassed!!.promoCode,
                        promoDataPassed!!.promoCode
                    )
                    clipboard.setPrimaryClip(clip)
                    val successDialog = alertDialogSmallSuccess(
                        (this@PromoCodeDetailsActivity as Activity), "Copied"
                    )
                    Handler().postDelayed({
                        successDialog.dismiss()
                        finish()
                    }, 2000)
                } else if (promoDataPassed!!.calledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
                    val request = ValidatePromoCode(
                        promoDataPassed!!.supplierId,
                        promoDataPassed!!.orderSubTotalPromoApplied,
                        promoDataPassed!!.promoCode
                    )
                    validatePromotionCode(
                        this@PromoCodeDetailsActivity,
                        request,
                        promoDataPassed!!.outletId,
                        object : ValidatePromoCodeListener {
                            override fun onSuccessResponse(response: ValidatePromoCode.Response?) {
                                if (response != null && response.data != null) {
                                    val newIntent = Intent()
                                    newIntent.putExtra(
                                        PromoListUI.PROMO_CODE_DETAILS,
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(response.data)
                                    )
                                    setResult(ZeemartAppConstants.ResultCode.RESULT_OK, newIntent)
                                    finish()
                                }
                            }

                            override fun onErrorResponse(error: VolleyError?) {
                                val response = error!!.message
                                if (!StringHelper.isStringNullOrEmpty(error.message)) {
                                    val validatePromoCodeResponseResponse = fromJson(
                                        error.message, ValidatePromoCode.Response::class.java
                                    )
                                    val dialog = displayErrorMessageDialog(
                                        this@PromoCodeDetailsActivity,
                                        validatePromoCodeResponseResponse!!.message,
                                        null
                                    )
                                }
                                Log.d("Error", error.toString())
                            }
                        })
                    //validate the promo and apply
                }
            }
        })
        setFont()
    }

    private fun getPromoDetailData(promoCodeId: String?, outletId: String?) {
        val isAllOutlet = true
        val outlets: MutableList<Outlet?>
        if (outletId != null) {
            val outlet = Outlet()
            outlet.outletId = outletId
            outlets = ArrayList()
            outlets.add(outlet)
        } else {
            outlets = SharedPref.allOutlets!! as MutableList<Outlet?>
        }
        retrieveSpecificPromoCode(
            this,
            promoCodeId,
            outlets as List<Outlet>?,
            object : RetrieveSpecificPromoCodeResponseListener {
                override fun onSuccessResponse(response: RetrieveSpecificPromoCode?) {
                    if (response != null && response.data != null) {
                        if (!StringHelper.isStringNullOrEmpty(response.data!!.imageURL)) {
                            Picasso.get().load(response.data!!.imageURL)
                                .fit().centerCrop()
                                .into(imgPromoImage)
                        } else {
                            imgPromoImage!!.visibility = View.GONE
                        }
                        if (!StringHelper.isStringNullOrEmpty(response.data!!.title)) {
                            txtPromoTitle!!.text = response.data!!.title
                        }
                        if (!StringHelper.isStringNullOrEmpty(response.data!!.description)) {
                            txtPromoDescription!!.text = response.data!!.description
                        }
                        if (response.data!!.startDate != null && response.data!!.endDate != null) {
                            txtValidityTagValue!!.text =
                                DateHelper.getDateInDateMonthYearTimeInBracketFormat(
                                    response.data!!.startDate, null
                                ) + " - " + DateHelper.getDateInDateMonthYearTimeInBracketFormat(
                                    response.data!!.endDate, null
                                )
                        } else {
                            txtValidityTag!!.visibility = View.GONE
                            txtValidityTagValue!!.visibility = View.GONE
                        }
                        if (!StringHelper.isStringNullOrEmpty(response.data!!.supplierNames)) {
                            txtForAllOrderTag!!.visibility = View.VISIBLE
                            txtForAllOrderTagValue!!.visibility = View.VISIBLE
                            supplierNamesArray =
                                response.data!!.supplierNames!!.split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            for (i in supplierNamesArray.indices) {
                                txtForAllOrderTagValue!!.append(resources.getString(R.string.bullet) + "  " + supplierNamesArray[i])
                                txtForAllOrderTagValue!!.append(resources.getString(R.string.txt_new_line))
                            }
                        } else {
                            txtForAllOrderTag!!.visibility = View.GONE
                            txtForAllOrderTagValue!!.visibility = View.GONE
                        }
                        if (response.data!!.terms != null && response.data!!.terms!!.size > 0) {
                            txtTermsTag!!.visibility = View.VISIBLE
                            txtTermTagValue!!.visibility = View.VISIBLE
                            var terms = ""
                            for (i in response.data!!.terms!!.indices) {
                                terms =
                                    terms + resources.getString(R.string.bullet) + "  " + response.data!!.terms!![i] + resources.getString(
                                        R.string.txt_new_line
                                    )
                            }
                            txtTermTagValue!!.text = terms
                        } else {
                            txtTermsTag!!.visibility = View.GONE
                            txtTermTagValue!!.visibility = View.GONE
                        }
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {}
            })
    }

    private fun setFont() {
        setTypefaceView(
            txtPromoCodeDetailHeaderText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtPromoTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtPromoDescription, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtValidityTagValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtValidityTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtForAllOrderTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtForAllOrderTagValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtTermsTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtTermTagValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnUsePromo, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }
}