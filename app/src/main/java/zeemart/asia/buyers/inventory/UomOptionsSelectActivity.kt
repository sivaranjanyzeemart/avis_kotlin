package zeemart.asia.buyers.inventory

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getDoubleToString
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.UomOptionsSelectAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.inventory.InventoryDataManager.CountStockProductListUIModel
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.inventory.InventoryProduct.OrderByUnitConversion

class UomOptionsSelectActivity : AppCompatActivity() {
    private lateinit var btnClose: ImageView
    private var txtProductHeading: TextView? = null
    private var txtProductMessage: TextView? = null
    private lateinit var recyclerView: RecyclerView
    private var btnSaveStockCount: Button? = null
    private var product: CountStockProductListUIModel? = null
    var selectedQuantity = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_uom_options_select)
        btnClose = findViewById(R.id.btn_close_adjustment)
        txtProductHeading = findViewById(R.id.txt_product_name_adjustment)
        txtProductMessage = findViewById(R.id.txt_title_enter_adjustment)
        btnSaveStockCount = findViewById(R.id.btn_save_adjustment)
        btnClose.setOnClickListener(View.OnClickListener { finish() })
        recyclerView = findViewById(R.id.lst_uom_options)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        val bundle = intent.extras
        if (bundle != null) {
            val productJson = bundle.getString(InventoryDataManager.INTENT_PRODUCT_DETAIL, "")
            product = fromJson(productJson, CountStockProductListUIModel::class.java)
        }
        if (product != null) {
            txtProductHeading!!.setText(product!!.productName)
            txtProductMessage!!.setText(
                String.format(
                    getString(R.string.txt_uom_message), returnShortNameValueUnitSizeForQuantity(
                        product!!.unitSize
                    )
                )
            )
            recyclerView.setAdapter(
                UomOptionsSelectAdapter(
                    this,
                    product!!.orderByUnitConversions!!,
                    object : UomItemCountListener {
                        override fun onInventoryItemCounted(orderByUnitConversion: List<OrderByUnitConversion>) {
                            setSaveCountButtonSettings(orderByUnitConversion)
                        }
                    })
            )
        }
        btnSaveStockCount!!.setOnClickListener(View.OnClickListener {
            product!!.quantity = selectedQuantity
            val productJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(product)
            val intent = Intent()
            intent.putExtra(InventoryDataManager.INTENT_PRODUCT_DETAIL, productJson)
            setResult(RESULT_OK, intent)
            finish()
        })
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtProductHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtProductMessage, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnSaveStockCount, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    fun setSaveCountButtonSettings(orderByUnitConversions: List<OrderByUnitConversion>) {
        var value = 0.0
        for (i in orderByUnitConversions.indices) {
            if (orderByUnitConversions[i].selectedQuantity != null && orderByUnitConversions[i].selectedQuantity != 0.0) {
                value =
                    value + orderByUnitConversions[i].orderByUnitQtyConversionValue * orderByUnitConversions[i].selectedQuantity!!
            }
        }
        selectedQuantity = value
        btnSaveStockCount!!.text = String.format(
            resources.getString(R.string.txt_save_uom_btn_title),
            getDoubleToString(value),
            returnShortNameValueUnitSizeForQuantity(
                product!!.unitSize
            )
        )
        product!!.orderByUnitConversions = orderByUnitConversions
    }

    interface UomItemCountListener {
        fun onInventoryItemCounted(orderByUnitConversion: List<OrderByUnitConversion>)
    }
}