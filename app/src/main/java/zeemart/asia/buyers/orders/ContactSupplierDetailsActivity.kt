package zeemart.asia.buyers.orders

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SupplierContactListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.order.Orders

/**
 * Created by Rajprudhvi Marella on 1/8/2018.
 */
class ContactSupplierDetailsActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageButton
    private var supplierId: String? = null
    private var orderId: String? = null
    private var outletId: String? = null
    private var txtContactSupplierHeaderText: TextView? = null
    private lateinit var lstSupplierContactInfo: RecyclerView
    private lateinit var mOrder: Orders
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_supplier)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_ID)) {
                supplierId = bundle.getString(ZeemartAppConstants.SUPPLIER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.ORDER_ID)) {
                orderId = bundle.getString(ZeemartAppConstants.ORDER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.OUTLET_ID)) {
                outletId = bundle.getString(ZeemartAppConstants.OUTLET_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }
        txtContactSupplierHeaderText = findViewById(R.id.contact_supplier_header)
        setTypefaceView(
            txtContactSupplierHeaderText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        lstSupplierContactInfo = findViewById(R.id.lst_supplier_contact_details)
        lstSupplierContactInfo.setLayoutManager(LinearLayoutManager(this))
        lstSupplierContactInfo.setAdapter(
            SupplierContactListAdapter(
                this,
                mOrder!!.allStatusDetail
            )
        )
        btnBack = findViewById(R.id.contact_supplier_image_button_move_back)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
    }
}