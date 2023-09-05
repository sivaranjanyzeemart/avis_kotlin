package zeemart.asia.buyers.inventory

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.Shelve
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.network.OutletInventoryApi

/**
 * Created by RajPrudhviMarella on 02/Nov/2021.
 */
class SaveAmendmentActivity : AppCompatActivity() {
    private var btnClose: ImageView? = null
    private var txtProductHeading: TextView? = null
    private var edtQuantity: EditText? = null
    private var imgDecrease: ImageView? = null
    private var imgIncrease: ImageView? = null
    private var txtProductUom: TextView? = null
    private var btnSaveAdjustment: Button? = null
    private var shelve: Shelve? = null
    private var product: StockCountEntries.StockInventoryProduct? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_amendment)
        btnClose = findViewById<ImageView>(R.id.btn_close_adjustment)
        btnClose!!.setOnClickListener { finish() }
        txtProductHeading = findViewById<TextView>(R.id.txt_product_name_adjustment)
        edtQuantity = findViewById<EditText>(R.id.edtChangeQuantity)
        imgDecrease = findViewById<ImageView>(R.id.img_dec_quant)
        imgDecrease!!.setOnClickListener { decreaseQuantityClicked() }
        imgDecrease!!.visibility = View.INVISIBLE
        imgIncrease = findViewById<ImageView>(R.id.img_inc_quant)
        imgIncrease!!.setOnClickListener { increaseQuantityClicked() }
        txtProductUom = findViewById<TextView>(R.id.txt_uom_adjustment)
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
            if (product != null && product!!.stockQuantity != null) {
                edtQuantity!!.setText(product!!.stockQuantityDisplayValue)
                if (!StringHelper.isStringNullOrEmpty(product!!.stockQuantityDisplayValue)
                    && product!!.stockQuantityDisplayValue.toString() != "0") {
                    imgDecrease!!.visibility = View.VISIBLE
                } else {
                    imgDecrease!!.visibility = View.INVISIBLE
                }
            }
        }
        btnSaveAdjustment = findViewById<Button>(R.id.btn_save_adjustment)
        btnSaveAdjustment!!.setOnClickListener {
            if (SharedPref.defaultOutlet != null) {
                AnalyticsHelper.logActionForInventory(
                    this@SaveAmendmentActivity,
                    AnalyticsHelper.TAP_INVENTORY_LIST_EDITQTY_SAVE,
                    SharedPref.defaultOutlet!!
                )
            }
            btnSaveAdjustmentClicked()
        }
        quantityAddTextChangeListener()
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
            btnSaveAdjustment,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun quantityAddTextChangeListener() {
        edtQuantity?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!StringHelper.isStringNullOrEmpty(s.toString()) && s.toString() != "0") {
                    imgDecrease!!.visibility = View.VISIBLE
                } else {
                    imgDecrease!!.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun btnSaveAdjustmentClicked() {
        val requestJson = InventoryDataManager.createJsonRequestForSaveAmendment(
            shelve!!,
            product!!,
            edtQuantity?.getText().toString()
        )
        OutletInventoryApi.createAmendmentEntry(
            this@SaveAmendmentActivity,
            requestJson,
            object : OutletInventoryApi.StockageEntryResponseListener {
                override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                    val dialog: AlertDialog = DialogHelper.alertDialogSmallSuccess(
                        this@SaveAmendmentActivity, getString(
                            R.string.txt_saved
                        )
                    )
                    Handler().postDelayed({
                        dialog.dismiss()
                        finish()
                    }, 2000)
                }

                override fun onErrorResponse(error: VolleyError?) {}
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
}