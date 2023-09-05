package zeemart.asia.buyers.orders.createordersimport

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SelectOutletAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.OutletMgr
import java.util.*

class SelectOutletActivity : AppCompatActivity() {
    private lateinit var lstOutlets: ListView
    private lateinit var btnCancel: ImageButton
    private var calledFrom: String? = null
    private var txtSelectOutletHeading: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_select_outlet)
        lstOutlets = findViewById(R.id.lst_outlet_ids)
        val bundle = intent.extras
        if (bundle != null && bundle.containsKey(ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM)) {
            calledFrom = bundle.getString(ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM)
        }
        val buyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
            SharedPref.read(
                SharedPref.BUYER_DETAIL,
                ""
            ), BuyerDetails::class.java
        )
        val outletList: MutableList<OutletMgr> = ArrayList()
        for (i in buyerDetails.outlet!!.indices) {
            val outletMgr = OutletMgr()
            outletMgr.outletId = buyerDetails.outlet!![i].outletId
            outletMgr.outletName = buyerDetails.outlet!![i].outletName
            outletList.add(outletMgr)
        }
        for (i in outletList.indices) {
            if (outletList[i].outletName == SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, "")) {
                outletList[i].isOutletSelected = true
                break
            }
        }
        //sorts the outlet list alphabetically PBF-275
        Collections.sort(outletList, object : Comparator<Outlet?> {

            override fun compare(p0: Outlet?, p1: Outlet?): Int {
                return p0?.outletName!!.compareTo(p1?.outletName!!)
            }
        })
        txtSelectOutletHeading = findViewById(R.id.txt_choose_delivery_location)
        setTypefaceView(
            txtSelectOutletHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val selectOutletAdapter = SelectOutletAdapter(this, outletList, calledFrom)
        lstOutlets.setAdapter(selectOutletAdapter)
        btnCancel = findViewById(R.id.btn_close_lst_outlet_ids)
        btnCancel.setOnClickListener(View.OnClickListener { SetActionOnBackPressed() }
        )
    }

    override fun onBackPressed() {
//        super.onBackPressed();
        SetActionOnBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }

    private fun SetActionOnBackPressed() {
        finish()
    }
}