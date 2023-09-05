package zeemart.asia.buyers.inventory

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.InventoryUomAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.inventory.InventoryProduct

/**
 * Created by RajPrudhviMarella on 01/June/2021.
 */
class ReviewInventoryUOMSettings : AppCompatActivity() {
    private var txtHeader: TextView? = null
    private var txtSubHeader: TextView? = null
    private var txtSKUHeader: TextView? = null
    private var txtUOMHeader: TextView? = null
    private lateinit var btnBack: ImageButton
    private var txtLoginBuyerHub: TextView? = null
    private lateinit var lstSkus: RecyclerView
    private var lstProducts: List<InventoryProduct>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_inventory_uom_settings)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.PRODUCT_DETAILS_JSON)) {
                lstProducts = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.PRODUCT_DETAILS_JSON),
                    object : TypeToken<List<InventoryProduct?>?>() {}.type
                )
            }
        }
        txtHeader = findViewById(R.id.txt_header)
        txtLoginBuyerHub = findViewById(R.id.txt_buyer_hub_login)
        txtSubHeader = findViewById(R.id.txt_sub_header)
        txtSKUHeader = findViewById(R.id.txt_sku_header)
        txtUOMHeader = findViewById(R.id.txt_uom_header)
        btnBack = findViewById(R.id.btn_cancel)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        lstSkus = findViewById(R.id.lst_skus)
        lstSkus.setLayoutManager(LinearLayoutManager(this))
        lstSkus.setAdapter(InventoryUomAdapter(lstProducts!!))
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtLoginBuyerHub, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSubHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtSKUHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtUOMHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }
}