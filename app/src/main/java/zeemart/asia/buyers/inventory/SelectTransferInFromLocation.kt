package zeemart.asia.buyers.inventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.FilterInventoryTranferOutletsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.inventory.AdjustmentReasons
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.modelsimport.InventoryTransferLocationModel

/**
 * Created by RajPrudhviMarella on 23/Mar/2022.
 */
class SelectTransferInFromLocation : AppCompatActivity() {
    private var txtHeader: TextView? = null
    private var btnBack: ImageButton? = null
    private var calledFrom: String? = null
    private var layoutDontSelect: RelativeLayout? = null
    private var lyt_filter_inventory_activity: RelativeLayout? = null
    private var txtDontSelectHeader: TextView? = null
    private var imgTickDontSelect: ImageView? = null
    private var isDontSelectSelected = false
    var lstLocations: RecyclerView? = null
    var btnSave: Button? = null
    var lstOutlets: List<InventoryTransferLocationModel.TransferOutlets>? = null
    var selectedOutlets: MutableList<InventoryTransferLocationModel.TransferOutlets>? = null
    private var product: StockCountEntries.StockInventoryProduct? = null
    private var txtOutletsHeader: TextView? = null
    private var txtListSubHeader: TextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_tranfer_in_from_location)
        val bundle: Bundle? = getIntent().getExtras()
        //save the called from and selected Filters information from bundle to the corresponding lists
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_FILTER_LOCATIONS)) {
                val location: String? =
                    bundle.getString(ZeemartAppConstants.SELECTED_FILTER_LOCATIONS)
                selectedOutlets = ZeemartBuyerApp.fromJsonList(
                    location,
                    InventoryTransferLocationModel.TransferOutlets::class.java,
                    object : TypeToken<List<InventoryTransferLocationModel.TransferOutlets?>?>() {}.type
                ) as MutableList<InventoryTransferLocationModel.TransferOutlets>?
            }
            if (bundle.containsKey(ZeemartAppConstants.OUTLET_DETAILS)) {
                val location: String? = bundle.getString(ZeemartAppConstants.OUTLET_DETAILS)
                lstOutlets = ZeemartBuyerApp.fromJsonList(
                    location,
                    InventoryTransferLocationModel.TransferOutlets::class.java,
                    object : TypeToken<List<InventoryTransferLocationModel.TransferOutlets?>?>() {}.type
                )
            }
            val productJson: String =
                bundle.getString(InventoryDataManager.INTENT_PRODUCT_DETAIL, "")
            product = ZeemartBuyerApp.fromJson<StockCountEntries.StockInventoryProduct>(
                productJson,
                StockCountEntries.StockInventoryProduct::class.java
            )
        }
        btnBack = findViewById<ImageButton>(R.id.filter_btn_cancel)
        lyt_filter_inventory_activity =
            findViewById<RelativeLayout>(R.id.lyt_filter_inventory_activity)
        btnBack!!.setOnClickListener(View.OnClickListener { finish() })
        txtHeader = findViewById<TextView>(R.id.txt_filter)
        txtListSubHeader = findViewById<TextView>(R.id.txt_list_sub_header)
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == AdjustmentReasons.Transfer_In.apiValue) {
            txtHeader!!.setText(getResources().getString(R.string.txt_tranfer_from))
        } else {
            txtHeader!!.setText(getResources().getString(R.string.txt_tranfer_to))
        }
        txtOutletsHeader = findViewById<TextView>(R.id.filter_txt_type)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOutletsHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        layoutDontSelect = findViewById<RelativeLayout>(R.id.filter_lyt_inventory_list_header)
        txtDontSelectHeader = findViewById<TextView>(R.id.txt_filter_name)
        ZeemartBuyerApp.setTypefaceView(
            txtDontSelectHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtListSubHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        imgTickDontSelect = findViewById<ImageView>(R.id.img_select_filter)
        layoutDontSelect!!.setOnClickListener(View.OnClickListener {
            selectedOutlets = ArrayList<InventoryTransferLocationModel.TransferOutlets>()
            isDontSelectSelected = true
            setDontSelectUI()
            for (i in lstOutlets!!.indices) {
                lstOutlets!![i].isSelected
            }
            lstLocations?.getAdapter()?.notifyDataSetChanged()
        })
        lstLocations = findViewById<RecyclerView>(R.id.filter_lst_type)
        lstLocations!!.setLayoutManager(LinearLayoutManager(this))
        btnSave = findViewById<Button>(R.id.filter_btn_apply_filter)
        btnSave!!.setOnClickListener {
            val intent =
                Intent(this@SelectTransferInFromLocation, SaveAdjustmentActivity::class.java)
            intent.putExtra(ZeemartAppConstants.CALLED_FROM, calledFrom)
            val json: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOutlets)
            intent.putExtra(ZeemartAppConstants.SELECTED_FILTER_LOCATIONS, json)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        ZeemartBuyerApp.setTypefaceView(
            btnSave,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (lstOutlets != null && lstOutlets!!.size > 0) {
            isDontSelectSelected = if (selectedOutlets != null && selectedOutlets!!.size > 0) {
                for (i in lstOutlets!!.indices) {
                    if (lstOutlets!![i].outletId
                            .equals(selectedOutlets!![0].outletId) && lstOutlets!![i].outletName
                            .equals(
                                selectedOutlets!![0].outletName
                            )
                    ) {
                        lstOutlets!![i].isSelected
                    }
                }
                false
            } else {
                true
            }
            setDontSelectUI()
            lstLocations!!.setAdapter(
                FilterInventoryTranferOutletsAdapter(
                    lstOutlets!!,
                    object : FilterInventoryTranferOutletsAdapter.SelectedInvoiceFiltersListener {
                        override fun onFilterSelected(transferOutlets: InventoryTransferLocationModel.TransferOutlets?) {
                            selectedOutlets = ArrayList<InventoryTransferLocationModel.TransferOutlets>()
                            selectedOutlets!!.add(transferOutlets!!)
                            isDontSelectSelected = false
                            setDontSelectUI()
                        }

                        override fun onFilterDeselected(transferOutlets: InventoryTransferLocationModel.TransferOutlets?) {}
                    },
                    this@SelectTransferInFromLocation
                )
            )
        }
        setDontSelectUI()
    }

    private fun setDontSelectUI() {
        if (isDontSelectSelected) {
            imgTickDontSelect!!.visibility = View.VISIBLE
        } else {
            imgTickDontSelect!!.visibility = View.GONE
        }
    }
}