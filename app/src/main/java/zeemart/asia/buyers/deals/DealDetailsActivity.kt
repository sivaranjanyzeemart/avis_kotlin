package zeemart.asia.buyers.deals

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse

class DealDetailsActivity : AppCompatActivity() {
    private lateinit var txtDealDetailHeader: TextView
    private lateinit var btnClose: ImageButton
    private lateinit var txtDealTitle: TextView
    private lateinit var txtDealDescription: TextView
    private lateinit var txtDealPeriodTitle: TextView
    private lateinit var txtDealPeriodValue: TextView
    private lateinit var lstDeals: DealsBaseResponse.Deals
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal_details)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.DEALS_LIST)) {
                lstDeals = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.DEALS_LIST),
                    DealsBaseResponse.Deals::class.java
                )
            }
        }
        txtDealDetailHeader = findViewById<TextView>(R.id.txt_deal_details_header_name)
        btnClose = findViewById<ImageButton>(R.id.btn_close_deal_details)
        btnClose.setOnClickListener(View.OnClickListener { finish() })
        txtDealTitle = findViewById<TextView>(R.id.txt_deal_detail_title)
        txtDealDescription = findViewById<TextView>(R.id.txt_deal_description)
        txtDealPeriodTitle = findViewById<TextView>(R.id.txt_deal_period_title)
        txtDealPeriodValue = findViewById<TextView>(R.id.txt_deal_period_value)
        if (lstDeals != null) {
            txtDealTitle.setText(lstDeals.title)
            txtDealDescription.setText(lstDeals.description)
            val datePeriodValue: String = DateHelper.getDateInDateMonthYearFormat(
                lstDeals.activePeriod?.startTime,
                null
            ) + " - " + DateHelper.getDateInDateMonthYearFormat(
                lstDeals.activePeriod?.endTime, null
            )
            txtDealPeriodValue.setText(datePeriodValue)
        }
        setUIFont()
    }

    private fun setUIFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtDealDetailHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDealTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDealDescription,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDealPeriodTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDealPeriodValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }
}