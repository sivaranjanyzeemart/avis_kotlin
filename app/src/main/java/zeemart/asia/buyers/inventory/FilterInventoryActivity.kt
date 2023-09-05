package zeemart.asia.buyers.inventory

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.FilterInventoryAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.StockageType.Companion.stockTypeListFromSharedPrefs
import zeemart.asia.buyers.models.StockageType.Companion.updateStockListInSharedPrefs
import zeemart.asia.buyers.models.StockageType.StockageTypeFilter
import zeemart.asia.buyers.models.inventory.Shelve.Companion.saveShelveListToSharedPrefs
import zeemart.asia.buyers.models.inventory.Shelve.Companion.shelveListFromSharedPrefs
import zeemart.asia.buyers.models.inventory.Shelve.ShelveFilter

class FilterInventoryActivity : AppCompatActivity() {
    private lateinit var lstStockTypes: RecyclerView
    private lateinit var lstShelves: RecyclerView
    private var nestedScrollView: NestedScrollView? = null
    private lateinit var btnApplyFilter: Button
    private val calledFrom: String? = null
    private lateinit var btnReset: Button
    private lateinit var btnCancel: ImageButton
    private var txtShelveHeader: TextView? = null
    private var txtStockTypeHeader: TextView? = null
    private var stockTypeFilterList: List<StockageTypeFilter>? = null
    private lateinit var shelveFilterList: List<ShelveFilter>
    private var txtFilterHeading: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_inventory)
        lstShelves = findViewById(R.id.filter_lst_inventory_list)
        lstShelves.setLayoutManager(LinearLayoutManager(this))
        lstShelves.setNestedScrollingEnabled(false)
        lstStockTypes = findViewById(R.id.filter_lst_type)
        lstStockTypes.setLayoutManager(LinearLayoutManager(this))
        lstStockTypes.setNestedScrollingEnabled(false)
        nestedScrollView = findViewById(R.id.filter_nested_scroll_view)
        btnApplyFilter = findViewById(R.id.filter_btn_apply_filter)
        btnApplyFilter.setOnClickListener(View.OnClickListener { applyFilterButtonClicked() })
        btnReset = findViewById(R.id.filter_btn_reset)
        btnReset.setOnClickListener(View.OnClickListener { resetButtonClicked() })
        btnCancel = findViewById(R.id.filter_btn_cancel)
        btnCancel.setOnClickListener(View.OnClickListener { cancelButtonClicked() })
        txtShelveHeader = findViewById(R.id.filter_txt_inventory_list)
        txtStockTypeHeader = findViewById(R.id.filter_txt_type)
        txtFilterHeading = findViewById(R.id.txt_filter)
        //initialize local list
        stockTypeFilterList = stockTypeListFromSharedPrefs
        shelveFilterList = shelveListFromSharedPrefs
        setListAdapters()
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtStockTypeHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtShelveHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnApplyFilter, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnReset, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtFilterHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    private fun setListAdapters() {
        if (lstStockTypes.adapter != null)
            lstStockTypes.adapter!!.notifyDataSetChanged()
        else {
            lstStockTypes.adapter =
                FilterInventoryAdapter(
                    this,
                    ZeemartAppConstants.STOCK_TYPE_FILTER, null,
                    stockTypeFilterList)
        }
        if (lstShelves.adapter != null)
            lstShelves.adapter!!.notifyDataSetChanged()
        else {
            lstShelves.adapter =
                FilterInventoryAdapter(this,
                    ZeemartAppConstants.SHELVE_FILTER, shelveFilterList, null)
        }
    }
    private fun applyFilterButtonClicked() {
        saveShelveListToSharedPrefs(shelveFilterList)
        updateStockListInSharedPrefs(stockTypeFilterList)
        val newIntent = Intent()
        newIntent.putExtra(ZeemartAppConstants.APPLY_FILTER_SELECTED, true)
        setResult(ZeemartAppConstants.ResultCode.RESULT_OK, newIntent)
        finish()
    }

    private fun cancelButtonClicked() {
        finish()
    }

    private fun resetButtonClicked() {
        for (i in stockTypeFilterList!!.indices) {
            stockTypeFilterList!![i]!!.isStockageTypeSelected = false
        }
        for (i in shelveFilterList!!.indices) {
            shelveFilterList!![i].isShelveSelected = false
        }
        setListAdapters()
    }
}