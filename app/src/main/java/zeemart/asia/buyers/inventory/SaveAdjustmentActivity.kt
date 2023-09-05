package zeemart.asia.buyers.inventory

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.AdjustmentReasonsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.inventory.AdjustmentReasons
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.Shelve
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.modelsimport.InventoryTransferLocationModel
import zeemart.asia.buyers.network.OutletInventoryApi
import java.util.*

class SaveAdjustmentActivity : AppCompatActivity() {
    private var btnClose: ImageView? = null
    private var txtProductHeading: TextView? = null
    private var grdReasons: RecyclerView? = null
    private var edtQuantity: EditText? = null
    private var imgDecrease: ImageView? = null
    private var imgIncrease: ImageView? = null
    private var txtProductUom: TextView? = null
    private var edtOptionalNotes: EditText? = null
    private var btnSaveAdjustment: Button? = null
    private var shelve: Shelve? = null
    private var product: StockCountEntries.StockInventoryProduct? = null
    private var txtEnterAdjustmentReason: TextView? = null
    private var txtSelectedLocation: TextView? = null
    var selectedOutlets: List<InventoryTransferLocationModel.TransferOutlets>? = ArrayList<InventoryTransferLocationModel.TransferOutlets>()
    private val adjustmentReasonList: MutableList<AdjustmentReasons.AdjustmentReasonSelected> =
        ArrayList<AdjustmentReasons.AdjustmentReasonSelected>()
    var lstOutlets: List<InventoryTransferLocationModel.TransferOutlets>? = null
    private var threeDotLoader: CustomLoadingViewWhite? = null
    private var adjustmentReason = ""
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_save_adjustment)
        btnClose = findViewById<ImageView>(R.id.btn_close_adjustment)
        btnClose!!.setOnClickListener { finish() }
        txtProductHeading = findViewById<TextView>(R.id.txt_product_name_adjustment)
        grdReasons = findViewById<RecyclerView>(R.id.lst_adjustment_reasons)
        edtQuantity = findViewById<EditText>(R.id.edtChangeQuantity)
        imgDecrease = findViewById<ImageView>(R.id.img_dec_quant)
        imgDecrease!!.setOnClickListener { decreaseQuantityClicked() }
        imgDecrease!!.visibility = View.INVISIBLE
        imgIncrease = findViewById<ImageView>(R.id.img_inc_quant)
        imgIncrease!!.setOnClickListener { increaseQuantityClicked() }
        txtProductUom = findViewById<TextView>(R.id.txt_uom_adjustment)
        edtOptionalNotes = findViewById<EditText>(R.id.edt_additional_notes_adjustment)
        txtEnterAdjustmentReason = findViewById<TextView>(R.id.txt_title_enter_adjustment)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            val shelveJson: String = bundle.getString(InventoryDataManager.INTENT_SHELVE_ID, "")
            shelve =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<Shelve>(shelveJson, Shelve::class.java)
            val productJson: String =
                bundle.getString(InventoryDataManager.INTENT_PRODUCT_DETAIL, "")
            product = ZeemartBuyerApp.fromJson<StockCountEntries.StockInventoryProduct>(
                productJson,
                StockCountEntries.StockInventoryProduct::class.java
            )
        }
        btnSaveAdjustment = findViewById<Button>(R.id.btn_save_adjustment)
        btnSaveAdjustment!!.setOnClickListener { btnSaveAdjustmentClicked() }
        quantityAddTextChangeListener()
        createAdjustmentReasonList()
        grdReasons!!.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        grdReasons!!.setAdapter(
            AdjustmentReasonsAdapter(
                this,
                adjustmentReasonList,
                object : AdjustmentReasonsAdapter.AdjustmentReasonSelectedListener {
                    override fun onAdjustmentSelected(adjustmentReasonSelected: AdjustmentReasons.AdjustmentReasonSelected?) {
                        setSaveAdjustmentButton()
                        if (lstOutlets != null && lstOutlets!!.size > 0) {
                            if (adjustmentReasonSelected?.isSelected!! && (adjustmentReasonSelected.adjustmentReasons?.apiValue == AdjustmentReasons.Transfer_In.apiValue) || adjustmentReasonSelected.adjustmentReasons?.apiValue == AdjustmentReasons.Transfer_Out.apiValue) {
                                val intent = Intent(
                                    this@SaveAdjustmentActivity,
                                    SelectTransferInFromLocation::class.java
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM,
                                    adjustmentReasonSelected.adjustmentReasons?.apiValue
                                )
                                val json: String =
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOutlets)
                                intent.putExtra(ZeemartAppConstants.SELECTED_FILTER_LOCATIONS, json)
                                val productJson: String =
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(product)
                                intent.putExtra(
                                    InventoryDataManager.INTENT_PRODUCT_DETAIL,
                                    productJson
                                )
                                val outletList: String =
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(lstOutlets)
                                intent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletList)
                                startActivityForResult(intent, 101)
                            } else {
                                txtSelectedLocation?.setVisibility(View.GONE)
                            }
                        } else {
                            txtSelectedLocation?.setVisibility(View.GONE)
                        }
                    }
                })
        )
        grdReasons!!.setNestedScrollingEnabled(false)
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && product?.customName != null
        ) {
            txtProductHeading!!.setText(product!!.customName)
        } else {
            txtProductHeading!!.setText(product?.productName)
        }
        txtProductUom!!.setText(UnitSizeModel.returnShortNameValueUnitSizeForQuantity(product?.unitSize))
        btnSaveAdjustment!!.isClickable = false
        txtSelectedLocation = findViewById<TextView>(R.id.txt_selected_location)
        txtSelectedLocation!!.setVisibility(View.GONE)
        threeDotLoader = findViewById<CustomLoadingViewWhite>(R.id.three_dot_loader)
        threeDotLoader!!.setVisibility(View.GONE)
        callLocationsApi()
        setFont()
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtProductHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtProductUom,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            edtQuantity,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            edtOptionalNotes,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSaveAdjustment,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSaveAdjustment,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEnterAdjustmentReason,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSelectedLocation,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun setSaveAdjustmentButton() {
        if (!StringHelper.isStringNullOrEmpty(edtQuantity?.getText().toString())) {
            var isReasonSelected = false
            for (i in adjustmentReasonList.indices) {
                if (adjustmentReasonList[i].isSelected) {
                    isReasonSelected = true
                    adjustmentReason = adjustmentReasonList[i].adjustmentReasons?.apiValue.toString()
                }
            }
            if (isReasonSelected) {
                btnSaveAdjustment!!.isClickable = true
                btnSaveAdjustment!!.setBackgroundResource(R.drawable.btn_rounded_green)
            } else {
                btnSaveAdjustment!!.isClickable = false
                btnSaveAdjustment!!.setBackgroundResource(R.drawable.btn_rounded_dark_grey)
            }
        } else {
            btnSaveAdjustment!!.isClickable = false
            btnSaveAdjustment!!.setBackgroundResource(R.drawable.btn_rounded_dark_grey)
        }
    }

    private fun createAdjustmentReasonList() {
        val adjustmentReasonEnumList: List<AdjustmentReasons> = ArrayList<AdjustmentReasons>(
            Arrays.asList<AdjustmentReasons>(*AdjustmentReasons.values())
        )
        for (i in adjustmentReasonEnumList.indices) {
            val adjustmentReasonSelected = AdjustmentReasons.AdjustmentReasonSelected()
            adjustmentReasonSelected.adjustmentReasons = adjustmentReasonEnumList[i]
            adjustmentReasonSelected.isSelected = false
            adjustmentReasonList.add(adjustmentReasonSelected)
        }
    }

    private fun quantityAddTextChangeListener() {
        edtQuantity?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d("STRING", s.toString())
                if (!StringHelper.isStringNullOrEmpty(s.toString()) && s.toString() != "0") {
                    imgDecrease!!.visibility = View.VISIBLE
                } else {
                    imgDecrease!!.visibility = View.INVISIBLE
                }
                setSaveAdjustmentButton()
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun btnSaveAdjustmentClicked() {
        threeDotLoader?.setVisibility(View.VISIBLE)
        if (SharedPref.defaultOutlet != null) {
            AnalyticsHelper.logActionForInventory(
                this@SaveAdjustmentActivity,
                AnalyticsHelper.TAP_INVENTORY_LIST_ADJUSTMENT_SAVE,
                SharedPref.defaultOutlet!!,
                adjustmentReason
            )
        }
        val requestJson = InventoryDataManager.createJsonRequestForSaveAdjustment(
            shelve!!,
            product!!,
            adjustmentReasonList,
            edtOptionalNotes?.getText().toString(),
            edtQuantity?.getText().toString(),
            selectedOutlets
        )
        Log.d("api", "btnSaveAdjustmentClicked: $requestJson")
        OutletInventoryApi.createAdjustmentEntry(
            this,
            requestJson,
            object : OutletInventoryApi.StockageEntryResponseListener {
                override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                    threeDotLoader?.setVisibility(View.GONE)
                    val dialog: AlertDialog = DialogHelper.alertDialogSmallSuccess(
                        this@SaveAdjustmentActivity, getString(
                            R.string.txt_saved
                        )
                    )
                    Handler().postDelayed({
                        dialog.dismiss()
                        finish()
                    }, 2000)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoader?.setVisibility(View.GONE)
                    val dialog: AlertDialog = DialogHelper.alertDialogSmallFailure(
                        this@SaveAdjustmentActivity, getString(
                            R.string.txt_saving_failed
                        ), getString(R.string.txt_please_try_again)
                    )
                    Handler().postDelayed({
                        dialog.dismiss()
                        finish()
                    }, 2000)
                }
            })
    }

    private fun decreaseQuantityClicked() {
        val quantity: String = edtQuantity?.getText().toString()
        if (!StringHelper.isStringNullOrEmpty(quantity)) {
            try {
                var quantityDouble = quantity.toDouble()
                quantityDouble = quantityDouble - 1
                if (quantityDouble >= 0) {
                    val displayQuantity: String = ZeemartBuyerApp.getDoubleToString(quantityDouble)
                    edtQuantity?.setText(displayQuantity)
                    edtQuantity?.setSelection(edtQuantity!!.getText().toString().length)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
    }

    private fun increaseQuantityClicked() {
        val quantity: String = edtQuantity?.getText().toString()
        var quantityDouble = 1.0
        if (!StringHelper.isStringNullOrEmpty(quantity)) {
            try {
                quantityDouble = quantity.toDouble()
                quantityDouble = quantityDouble + 1
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        val displayQuantity: String = ZeemartBuyerApp.getDoubleToString(quantityDouble)
        edtQuantity?.setText(displayQuantity)
        edtQuantity?.setSelection(edtQuantity!!.getText().toString().length)
    }

    private fun callLocationsApi() {
        threeDotLoader?.setVisibility(View.VISIBLE)
        lstOutlets = ArrayList<InventoryTransferLocationModel.TransferOutlets>()
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setProductName(product?.productName!!)
        apiParamsHelper.setUnitSize(product?.unitSize!!)
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        OutletInventoryApi.getAllTransferLocations(
            this,
            apiParamsHelper,
            object : OutletInventoryApi.OutletTransferListener {
                override fun outletTransferSuccess(outletsLists: List<InventoryTransferLocationModel.TransferOutlets?>?) {
                    threeDotLoader?.setVisibility(View.GONE)
                    if (outletsLists != null && outletsLists.size > 0) {
                        lstOutlets = outletsLists as List<InventoryTransferLocationModel.TransferOutlets>
                    }
                }

                override fun errorResponse(error: VolleyError?) {
                    threeDotLoader?.setVisibility(View.GONE)
                }
            })
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            txtSelectedLocation?.setVisibility(View.VISIBLE)
            val bundle: Bundle? = data?.getExtras()
            var calledFrom = ""
            var reason = ""
            if (bundle?.containsKey(ZeemartAppConstants.CALLED_FROM) == true) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM).toString()
            }
            if (bundle?.containsKey(ZeemartAppConstants.SELECTED_FILTER_LOCATIONS) == true) {
                val location: String? =
                    bundle.getString(ZeemartAppConstants.SELECTED_FILTER_LOCATIONS)
                selectedOutlets = ZeemartBuyerApp.fromJsonList(
                    location,
                    InventoryTransferLocationModel.TransferOutlets::class.java,
                    object : TypeToken<List<InventoryTransferLocationModel.TransferOutlets?>?>() {}.type
                )
            }
            reason = if (selectedOutlets != null && selectedOutlets!!.size > 0) ({
                selectedOutlets!![0].outletName
            }).toString() else {
                getResources().getString(R.string.txt_dont_select_location)
            }
            if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == AdjustmentReasons.Transfer_In.apiValue) {
                txtSelectedLocation?.setText(getResources().getString(R.string.txt_from) + ": " + reason)
            } else {
                txtSelectedLocation?.setText(getResources().getString(R.string.txt_to) + ": " + reason)
            }
        } else {
            txtSelectedLocation?.setVisibility(View.VISIBLE)
            if (selectedOutlets != null && selectedOutlets!!.size == 0) {
                var reason = ""
                for (i in adjustmentReasonList.indices) {
                    if (adjustmentReasonList[i].isSelected) {
                        reason = adjustmentReasonList[i].adjustmentReasons?.apiValue.toString()
                    }
                }
                if (!StringHelper.isStringNullOrEmpty(reason) && reason == AdjustmentReasons.Transfer_In.apiValue) {
                    txtSelectedLocation?.setText(getString(R.string.txt_no_transfer_source_selected))
                } else {
                    txtSelectedLocation?.setText(getString(R.string.txt_no_transfer_destination_selected))
                }
            }
        }
    }
}