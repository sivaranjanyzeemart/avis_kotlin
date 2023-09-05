package zeemart.asia.buyers.orders.createorders

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJsonList
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.PromoCodeListAdapter
import zeemart.asia.buyers.adapter.PromoCodeListAdapter.PromoCodeSelectedListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DialogHelper.displayErrorMessageDialog
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.PromoListUI
import zeemart.asia.buyers.models.PromoListUI.PromoDataPassed
import zeemart.asia.buyers.models.ValidatePromoCode
import zeemart.asia.buyers.modelsimport.RetrieveMultiplePromoCodes
import zeemart.asia.buyers.more.PromoCodeDetailsActivity
import zeemart.asia.buyers.network.PromoCodeApi
import zeemart.asia.buyers.network.PromoCodeApi.MultiplePromoCodesResponseListener
import zeemart.asia.buyers.network.PromoCodeApi.ValidatePromoCodeListener
import java.util.*

class AddPromoCodeActivity : AppCompatActivity() {
    private lateinit var edtEnterPromoCode: EditText
    private lateinit var btnSearchPromoCode: Button
    private lateinit var txtPromoCodeHeading: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var loader: CustomLoadingViewWhite
    private lateinit var supplierId: String
    private lateinit var outletId: String
    private lateinit var amount: PriceDetails
    private lateinit var btnDeleteSearchText: ImageButton
    private lateinit var lstPromoCodes: RecyclerView

    /**
     * saves the promoDetails of promo received via bundle
     * can be null if no promo is selected initially
     */
    private var promoListUI: PromoListUI? = null
    private var promoCodesList: MutableList<PromoListUI>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add_promo_code)
        val bundle = intent.extras
        loader = findViewById(R.id.spin_kit_loader_add_promo_code_white)
        loader.setVisibility(View.GONE)
        btnDeleteSearchText = findViewById(R.id.btn_delete_search_text)
        btnDeleteSearchText.setVisibility(View.INVISIBLE)
        btnDeleteSearchText.setOnClickListener(View.OnClickListener { edtEnterPromoCode!!.setText("") })
        lstPromoCodes = findViewById(R.id.lst_promo_code)
        lstPromoCodes.setLayoutManager(LinearLayoutManager(this))
        edtEnterPromoCode = findViewById(R.id.edt_enter_promo_code)
        edtEnterPromoCode.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (!StringHelper.isStringNullOrEmpty(
                    edtEnterPromoCode.getText().toString()
                )
            ) validatePromoCode(edtEnterPromoCode.getText().toString(), false)
            false
        })
        edtEnterPromoCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (StringHelper.isStringNullOrEmpty(edtEnterPromoCode.getText().toString())) {
                    btnSearchPromoCode!!.setBackgroundResource(R.drawable.rect_rounded_right_grey)
                    btnSearchPromoCode!!.isClickable = false
                    btnDeleteSearchText.setVisibility(View.INVISIBLE)
                } else {
                    btnSearchPromoCode!!.setBackgroundResource(R.drawable.rect_rounded_right_blue)
                    btnSearchPromoCode!!.isClickable = true
                    btnDeleteSearchText.setVisibility(View.VISIBLE)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        btnSearchPromoCode = findViewById(R.id.btn_apply_promo_code)
        btnSearchPromoCode.setOnClickListener(View.OnClickListener {
            if (!StringHelper.isStringNullOrEmpty(
                    edtEnterPromoCode.getText().toString()
                )
            ) validatePromoCode(edtEnterPromoCode.getText().toString(), false)
        })
        txtPromoCodeHeading = findViewById(R.id.txt_promo_code_heading)
        btnBack = findViewById(R.id.btn_back_promo_code)
        btnBack.setOnClickListener(View.OnClickListener {
            val newIntent = Intent()
            setResult(ZeemartAppConstants.ResultCode.RESULT_CANCELED, newIntent)
            CommonMethods.hideKeyboard(this@AddPromoCodeActivity)
            finish()
        })
        if (bundle != null) {
            supplierId = bundle.getString(ZeemartAppConstants.SUPPLIER_ID, "")
            outletId = bundle.getString(ZeemartAppConstants.OUTLET_ID).toString()
            val subTotal = bundle.getString(PromoListUI.SUBTOTAL_AMOUNT)
            amount =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson(subTotal, PriceDetails::class.java)
            if (bundle.containsKey(PromoListUI.PROMO_CODE_DETAILS)) {
                val promoListUIBundle = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(PromoListUI.PROMO_CODE_DETAILS),
                    PromoListUI::class.java
                )
                promoListUI = promoListUIBundle
                edtEnterPromoCode.setText(promoListUI!!.promoCode)
                edtEnterPromoCode.setSelection(edtEnterPromoCode.getText().toString().length)
            }
        }
        allValidPromoCodes
        setFont()
    }

    private val allValidPromoCodes: Unit
        private get() {
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setSortBy(ApiParamsHelper.SortField.TIME_CREATED)
            apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
            apiParamsHelper.setSupplierId(supplierId)
            apiParamsHelper.setStatus(ApiParamsHelper.Status.ACTIVE)
            val outlet = Outlet()
            outlet.outletId = outletId
            val outlets: MutableList<Outlet> = ArrayList()
            outlets.add(outlet)
            PromoCodeApi.retrieveMultiplePromotionCodes(
                this,
                apiParamsHelper,
                outlets,
                object : MultiplePromoCodesResponseListener {
                    override fun onSuccessResponse(response: RetrieveMultiplePromoCodes.Response?) {
                        if (response != null && response.data != null && response.data!!.promoCodeList != null && response.data!!.promoCodeList!!.size > 0) {
                            val promoCodeListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                response.data!!.promoCodeList
                            )
                            promoCodesList = fromJsonList(
                                promoCodeListJson,
                                PromoListUI::class.java,
                                object : TypeToken<MutableList<PromoListUI?>?>() {}.type
                            ) as MutableList<PromoListUI>?
                            if (promoListUI != null && promoListUI!!.isPromoSelected) {
                                for (i in promoCodesList!!.indices) {
                                    if (promoListUI!!.promoCode == promoCodesList!![i].promoCode) {
                                        val selectedPromoCode = promoCodesList!![i]
                                        selectedPromoCode.isPromoSelected = true
                                        promoCodesList!!.removeAt(i)
                                        promoCodesList!!.add(0, selectedPromoCode)
                                        break
                                    }
                                }
                            }
                        } else {
                            if (promoListUI != null && promoListUI!!.isPromoSelected) {
                                promoCodesList?.add(promoListUI!!)
                            }
                        }
                        setAdapterPromoList(promoCodesList)
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        TODO("Not yet implemented")
                    }
                })
        }

    private fun validatePromoCode(promoCode: String?, isUsePromo: Boolean) {
        val promoCodeUpperCase = promoCode!!.uppercase(Locale.getDefault())
        edtEnterPromoCode!!.setText(promoCodeUpperCase)
        edtEnterPromoCode!!.setSelection(promoCodeUpperCase.length)
        loader!!.visibility = View.VISIBLE
        CommonMethods.hideKeyboard(this)
        val request = ValidatePromoCode(supplierId, amount, promoCodeUpperCase)
        PromoCodeApi.validatePromotionCode(
            this,
            request,
            outletId,
            object : ValidatePromoCodeListener {
                override fun onSuccessResponse(response: ValidatePromoCode.Response?) {
                    loader!!.visibility = View.GONE
                    if (response != null) {
                        val promoDataNew = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(response.data),
                            PromoListUI::class.java
                        )
                        if (!isUsePromo) {
                            //set Adapter for list of promo Codes
                            //PromoCodeHelper.PromoListUI promoListUIModelAfterValidate = new PromoCodeHelper.PromoListUI(response.);
                            if (promoCodesList != null && promoCodesList!!.size > 0) {
                                var isSearchedPromoInList = false
                                for (i in promoCodesList!!.indices) {
                                    if (promoDataNew.promoCode == promoCodesList!![i].promoCode) {
                                        isSearchedPromoInList = true
                                        val selectedPromoCode = promoCodesList!![i]
                                        //selectedPromoCode.setIsPromoSelected(true);
                                        promoCodesList!!.removeAt(i)
                                        promoCodesList!!.add(0, selectedPromoCode)
                                        break
                                    }
                                }
                                if (!isSearchedPromoInList) {
                                    promoCodesList!!.add(0, promoDataNew)
                                }
                            } else {
                                if (promoListUI != null) {
                                    promoDataNew.orderSubTotalPromoApplied =
                                        promoListUI!!.orderSubTotalPromoApplied
                                    if (!StringHelper.isStringNullOrEmpty(promoListUI!!.promoCode) && promoListUI!!.isPromoSelected && promoListUI!!.promoCode == edtEnterPromoCode!!.text.toString()) {
                                        //display dialog prmo already selected
                                        promoDataNew.isPromoSelected = true
                                        displayErrorMessageDialog(
                                            this@AddPromoCodeActivity,
                                            "This promo code is already applied",
                                            null
                                        )
                                    } else {
                                        promoDataNew.isPromoSelected = false
                                    }
                                } else {
                                    promoDataNew.isPromoSelected = false
                                }
                                promoCodesList?.add(promoDataNew)
                            }
                            setAdapterPromoList(promoCodesList)
                        } else {
                            val newIntent = Intent()
                            promoDataNew.orderSubTotalPromoApplied = amount
                            val promoCodeDetails =
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(promoDataNew)
                            newIntent.putExtra(PromoListUI.PROMO_CODE_DETAILS, promoCodeDetails)
                            setResult(ZeemartAppConstants.ResultCode.RESULT_OK, newIntent)
                            finish()
                        }
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    loader!!.visibility = View.GONE
                    val response = error?.message
                    if (!StringHelper.isStringNullOrEmpty(response)) {
                        val validatePromoCodeResponseResponse =
                            fromJson(response, ValidatePromoCode.Response::class.java)
                        if (validatePromoCodeResponseResponse != null && validatePromoCodeResponseResponse.message != null) {
                            val dialog = displayErrorMessageDialog(
                                this@AddPromoCodeActivity,
                                validatePromoCodeResponseResponse.message,
                                null
                            )
                            dialog.setOnDismissListener { edtEnterPromoCode!!.setText("") }
                        }
                    }
                    Log.d("Error", error.toString())
                }
            })
    }

    private fun setAdapterPromoList(promoCodes: List<PromoListUI>?) {
        /*List<PromoListUI> promoCodes = new ArrayList<>();
        promoCodes.add(promoListUIData);*/
        if (lstPromoCodes!!.adapter != null) {
            lstPromoCodes!!.adapter!!.notifyDataSetChanged()
        } else {
            lstPromoCodes!!.adapter = PromoCodeListAdapter(
                this@AddPromoCodeActivity,
                promoCodes!!,
                outletId,
                object : PromoCodeSelectedListener {
                    override fun onUsePromoCodeClicked(promoSelectedData: PromoListUI?) {
                        //validate the promo code
                        if (!StringHelper.isStringNullOrEmpty(promoSelectedData?.promoCode)) validatePromoCode(
                            promoSelectedData?.promoCode,
                            true
                        )
                    }

                    override fun onViewPromoDetailsClicked(promoListUI: PromoListUI?) {
                        val intent =
                            Intent(this@AddPromoCodeActivity, PromoCodeDetailsActivity::class.java)
                        val promoDataPassed = PromoDataPassed()
                        promoDataPassed.outletId = outletId
                        promoDataPassed.supplierId = supplierId
                        promoDataPassed.promoCode = promoListUI?.promoCode
                        promoDataPassed.isPromoSelected = promoListUI?.isPromoSelected!!
                        if (amount != null) promoDataPassed.orderSubTotalPromoApplied = amount
                        promoDataPassed.calledFrom = ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER
                        promoDataPassed.promoCodeId = promoListUI?.id
                        val promoCodeDetails =
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(promoDataPassed)
                        intent.putExtra(PromoListUI.PROMO_CODE_DETAILS, promoCodeDetails)
                        this@AddPromoCodeActivity.startActivityForResult(
                            intent,
                            ZeemartAppConstants.RequestCode.AddPromoCodeActivity
                        )
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZeemartAppConstants.RequestCode.AddPromoCodeActivity && resultCode == RESULT_OK && data != null) {
            //
            val response = data.extras!!.getString(PromoListUI.PROMO_CODE_DETAILS)
            val promoDataNew =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson(response, PromoListUI::class.java)
            promoDataNew.isPromoSelected = true
            val newIntent = Intent()
            promoDataNew.orderSubTotalPromoApplied = amount
            val promoCodeDetails = ZeemartBuyerApp.gsonExposeExclusive.toJson(promoDataNew)
            newIntent.putExtra(PromoListUI.PROMO_CODE_DETAILS, promoCodeDetails)
            setResult(ZeemartAppConstants.ResultCode.RESULT_OK, newIntent)
            finish()
        }
    }

    private fun setFont() {
        setTypefaceView(btnSearchPromoCode, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtEnterPromoCode, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtPromoCodeHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }
}