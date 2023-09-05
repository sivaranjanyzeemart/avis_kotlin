package zeemart.asia.buyers.companies

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
import zeemart.asia.buyers.adapter.OutletListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.Companies
import zeemart.asia.buyers.models.Outlet

class ViewOutletActivity : AppCompatActivity() {
    private lateinit var titleTxt: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var outletCountTxt: TextView
    private lateinit var lstOutlets: RecyclerView
    private var company: Companies? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_outlet)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.COMPANY)) {
                company = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.COMPANY),
                    Companies::class.java
                )
            }
        }
        titleTxt = findViewById(R.id.txt_company_name_outlets)
        btnBack = findViewById(R.id.btn_back_companies)
        outletCountTxt = findViewById(R.id.txt_outlet_count)
        titleTxt.setText(company!!.name)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        lstOutlets = findViewById(R.id.lst_outlets)
        lstOutlets.setLayoutManager(LinearLayoutManager(this))
        if (company!!.outlet != null) {
            val outletSize = company!!.outlet!!.size
            if (outletSize == 1) {
                outletCountTxt.setText(
                    String.format(
                        resources.getString(R.string.txt_outlet_items),
                        1
                    )
                )
            } else {
                outletCountTxt.setText(
                    String.format(
                        resources.getString(R.string.txt_outlets_items),
                        outletSize
                    )
                )
            }
            setTypefaceView(outletCountTxt, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(titleTxt, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setAdapterCompaniesList(company!!.outlet)
        }
    }

    private fun setAdapterCompaniesList(outlets: List<Outlet>) {
        /*List<PromoListUI> promoCodes = new ArrayList<>();
        promoCodes.add(promoListUIData);*/
        lstOutlets!!.adapter = OutletListAdapter(this@ViewOutletActivity, outlets)
    }
}