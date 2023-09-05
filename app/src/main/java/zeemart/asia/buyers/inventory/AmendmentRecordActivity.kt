package zeemart.asia.buyers.inventory

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.network.OutletInventoryApi

/**
 * Created by RajPrudhviMarella on 02/Nov/2021.
 */
class AmendmentRecordActivity : AppCompatActivity() {
    private var btnBack: ImageView? = null
    private var txtProductName: TextView? = null
    private var txtSupplierName: TextView? = null
    private var txtAmendmentQuantityValue: TextView? = null
    private var txtAmendmentPrevQuantityValue: TextView? = null
    private var txtStockCountDateValue: TextView? = null
    private var txtUpdatedByValue: TextView? = null
    private var txtStockCountDateTitle: TextView? = null
    private var txtInventoryList: TextView? = null
    private var stockageId: String? = null
    private var txtAmendmentRecordScreenHeading: TextView? = null
    private var txtQuantityTitle: TextView? = null
    private var txtPrevQuantityTitle: TextView? = null
    private var txtInventoryListTitle: TextView? = null
    private var txtUpdatedByTitle: TextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amendment_record)
        btnBack = findViewById<ImageView>(R.id.adjustment_record_back_btn)
        btnBack!!.setOnClickListener { finish() }
        txtProductName = findViewById<TextView>(R.id.txt_adjustment_record_product_name)
        txtSupplierName = findViewById<TextView>(R.id.txt_adjustment_record_supplier_name)
        txtAmendmentQuantityValue = findViewById<TextView>(R.id.txt_adjustment_qty_value)
        txtAmendmentPrevQuantityValue = findViewById<TextView>(R.id.txt_adjustment_reason_value)
        txtInventoryList = findViewById<TextView>(R.id.txt_adjustment_inventory_list_value)
        txtAmendmentRecordScreenHeading = findViewById<TextView>(R.id.txt_adjustment_record)
        txtQuantityTitle = findViewById<TextView>(R.id.txt_adjustment_qty_title)
        txtPrevQuantityTitle = findViewById<TextView>(R.id.txt_adjustment_reason_title)
        txtInventoryListTitle = findViewById<TextView>(R.id.txt_adjustment_inventory_list_title)
        txtStockCountDateValue = findViewById<TextView>(R.id.txt_adjustment_stock_count_value)
        txtStockCountDateTitle = findViewById<TextView>(R.id.txt_adjustment_stock_count_title)
        txtUpdatedByTitle = findViewById<TextView>(R.id.txt_adjustment_updated_by_title)
        txtUpdatedByValue = findViewById<TextView>(R.id.txt_adjustment_updated_by_value)
        val bundle: Bundle? = getIntent().getExtras()
        stockageId = bundle?.getString(InventoryDataManager.INTENT_STOCKAGE_ID, "")
        amendmentDetails
        setFont()
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtAmendmentRecordScreenHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtProductName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAmendmentPrevQuantityValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtStockCountDateValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpdatedByValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAmendmentQuantityValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtInventoryList,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtQuantityTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtInventoryListTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPrevQuantityTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtStockCountDateTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpdatedByTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private val amendmentDetails: Unit
        private get() {
            OutletInventoryApi.retrieveInventoryStockageEntryById(
                this,
                stockageId,
                object : OutletInventoryApi.StockageEntryResponseListener {
                    override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                        if (data != null && data.data != null && data.data!!
                                .products != null && data.data!!.products?.size!! > 0
                        ) {
                            setUIDataForAmendmentRecord(data.data!!)
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {}
                })
        }

    private fun setUIDataForAmendmentRecord(data: StockCountEntries) {
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && data.products?.get(0)?.customName != null
        ) {
            txtProductName?.setText(data.products!!.get(0).customName)
        } else {
            txtProductName?.setText(data.products?.get(0)?.productName)
        }
        txtSupplierName?.setText(data.products?.get(0)?.supplier?.supplierName)
        txtAmendmentQuantityValue?.setText(
            data.products?.get(0)?.stockQuantityDisplayValue + " " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                data.products?.get(0)?.unitSize
            )
        )
        txtAmendmentPrevQuantityValue?.setText(
            data.products?.get(0)?.previousQuantityDisplayValue + " " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                data.products?.get(0)?.unitSize
            )
        )
        val dateCreated: Long = data.timeCreated!!
        val dateUpdatedString: String = DateHelper.getDateInDateMonthYearFormat(dateCreated, null)
        txtStockCountDateValue?.setText(dateUpdatedString)
        txtInventoryList?.setText(data.shelve?.shelveName)
        var name = ""
        if (!StringHelper.isStringNullOrEmpty(data.updatedBy?.firstName)) {
            name = data.updatedBy?.firstName!!
        } else if (!StringHelper.isStringNullOrEmpty(data.updatedBy?.lastName)) {
            name = name + data.updatedBy?.lastName
        }
        txtUpdatedByValue?.setText(name)
    }
}