package zeemart.asia.buyers.essentials

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential

class EssentialSupplierDetailActivity : AppCompatActivity() {
    private lateinit var txtSupplierDetailHeader: TextView
    private lateinit var btnClose: ImageButton
    private lateinit var txtSupplierName: TextView
    private lateinit var txtSupplierDescription: TextView
    private lateinit var lstEssentials: Essential
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_essential_supplier_details)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ESSENTIALS_LIST)) {
                lstEssentials = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ESSENTIALS_LIST), Essential::class.java
                )
            }
        }
        txtSupplierDetailHeader = findViewById(R.id.txt_deal_details_header_name)
        btnClose = findViewById(R.id.btn_close_essential_details)
        btnClose.setOnClickListener(View.OnClickListener { finish() })
        txtSupplierName = findViewById(R.id.txt_essential_supplier_name)
        txtSupplierDescription = findViewById(R.id.txt_essential_supplier_description)
        if (lstEssentials != null) {
            txtSupplierName.setText(lstEssentials!!.supplier!!.supplierName)
            txtSupplierDescription.setText(lstEssentials!!.description)
        }
        setUIFont()
    }

    private fun setUIFont() {
        setTypefaceView(
            txtSupplierDetailHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtSupplierDescription,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }
}