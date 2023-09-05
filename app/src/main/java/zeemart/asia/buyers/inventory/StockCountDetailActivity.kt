package zeemart.asia.buyers.inventory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.StockProductListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.InventoryDataManager.ShelveProductListUIModel
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.network.OutletInventoryApi
import java.util.*

class StockCountDetailActivity : AppCompatActivity() {
    private var txtShelveName: TextView? = null
    private var txtShelveCreationDate: TextView? = null
    private var txtShelveItemCount: TextView? = null
    private var edtSearchListText: EditText? = null
    private var lytStartStockCount: RelativeLayout? = null
    private var stockageID: String? = null
    var lstShelveProductList: RecyclerView? = null
    private var lytCountDeleted: RelativeLayout? = null
    private var txtDeleteReason: TextView? = null
    private var edtSearchClear: ImageView? = null
    private var txtEstimatedTotal: TextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_stock_count_detail)
        txtShelveName = findViewById<TextView>(R.id.txt_heading)
        txtShelveCreationDate = findViewById<TextView>(R.id.txt_date_time)
        txtShelveItemCount = findViewById<TextView>(R.id.txt_item_count)
        txtEstimatedTotal = findViewById<TextView>(R.id.txt_total_price_count)
        edtSearchListText = findViewById<EditText>(R.id.edit_search)
        edtSearchClear = findViewById<ImageView>(R.id.edit_search_clear)
        edtSearchListText!!.setHint(getResources().getString(R.string.txt_search_sku))
        edtSearchListText!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    CommonMethods.hideKeyboard(this@StockCountDetailActivity)
                }
                return false
            }
        })
        edtSearchClear!!.setOnClickListener { edtSearchListText!!.setText("") }
        lstShelveProductList = findViewById<RecyclerView>(R.id.lst_shelve_product_list)
        lstShelveProductList!!.setLayoutManager(LinearLayoutManager(this))
        lytStartStockCount = findViewById<RelativeLayout>(R.id.lyt_start_stock_count)
        lytStartStockCount!!.setVisibility(View.GONE)
        lytCountDeleted = findViewById<RelativeLayout>(R.id.lyt_count_deleted)
        lytCountDeleted!!.setVisibility(View.GONE)
        txtDeleteReason = findViewById<TextView>(R.id.txt_delete_stock_count_reason)
        edtSearchListText!!.setFocusableInTouchMode(false)
        edtSearchListText!!.setFocusable(false)
        edtSearchListText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText: String = edtSearchListText!!.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear!!.visibility = View.VISIBLE
                } else {
                    edtSearchClear!!.visibility = View.GONE
                }
                if (lstShelveProductList!!.getAdapter() != null) (lstShelveProductList!!.getAdapter() as StockProductListAdapter).getFilter()
                    .filter(s)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        val bundle: Bundle? = getIntent().getExtras()
        stockageID = bundle?.getString(InventoryDataManager.INTENT_STOCKAGE_ID, "")
        callApiToFetchStockDetails()
        setFont()
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtDeleteReason,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtShelveName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtShelveCreationDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            edtSearchListText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeleteReason,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtShelveItemCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun callApiToFetchStockDetails() {
        OutletInventoryApi.retrieveInventoryStockageEntryById(
            this,
            stockageID,
            object : OutletInventoryApi.StockageEntryResponseListener {
                override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                    if (data != null && data.data != null && data.data!!
                            .products != null
                    ) {
                        if (data.data!!.status
                                .equals(StockCountEntries.InventoryActivityStatus.ACTIVE.value)
                        ) {
                            edtSearchListText?.setFocusableInTouchMode(true)
                            edtSearchListText?.setFocusable(true)
                            setUIforStockActive(data.data!!)
                        } else if (data.data!!.status
                                .equals(StockCountEntries.InventoryActivityStatus.DELETED.value)
                        ) {
                            setUIforStockDeleted(data.data!!)
                        }
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {}
            })
    }

    private fun setUIforStockDeleted(data: StockCountEntries) {
        if (data.products?.size == 1) {
            txtShelveItemCount?.setText(data.products!!.size.toString() + " " + getString(R.string.txt_item))
        } else {
            txtShelveItemCount?.setText(data.products?.size.toString() + " " + getString(R.string.txt_items))
        }
        lytCountDeleted?.setVisibility(View.VISIBLE)
        txtDeleteReason?.setText(getString(R.string.txt_reason) + data.deleteRemark)
        txtEstimatedTotal?.setText("Est. value: " + data.estimatedTotalValue?.displayValue)
        if (!UserPermission.HasViewPrice()) {
            txtEstimatedTotal?.setVisibility(View.GONE)
        } else {
            txtEstimatedTotal?.setVisibility(View.VISIBLE)
        }
        txtShelveName?.setText(data.shelve?.shelveName)
        val dateUpdatedString: String =
            DateHelper.getDateInDateMonthYearTimeFormat(data.timeUpdated, null)
        txtShelveCreationDate?.setText(getString(R.string.txt_created_by) + " " + data.createdBy?.firstName + " " + data.createdBy?.lastName + " (" + dateUpdatedString + ")")
        val productList: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        for (i in data.products?.indices!!) {
            val item = InventoryDataManager.ShelveProductListUIModel()
            item.isDeleteThisStockCountMessage = false
            item.isBuyerAdminMessage = false
            item.inventoryProduct = data.products!!.get(i)
            productList.add(item)
        }
        Collections.sort(productList, object : Comparator<ShelveProductListUIModel?> {
            override fun compare(a: ShelveProductListUIModel?, b: ShelveProductListUIModel?): Int {
                return a?.inventoryProduct!!.productName!!.compareTo(b?.inventoryProduct!!.productName!!)
            }
        })

        lstShelveProductList?.setAlpha(0.5f)
        lstShelveProductList?.setAdapter(StockProductListAdapter(this, productList, stockageID))
    }

    private fun setUIforStockActive(data: StockCountEntries) {
        if (data.products?.size == 1) {
            txtShelveItemCount?.setText(data.products!!.size.toString() + " " + getString(R.string.txt_item))
        } else {
            txtShelveItemCount?.setText(data.products?.size.toString() + " " + getString(R.string.txt_items))
        }
        lytCountDeleted?.setVisibility(View.GONE)
        txtShelveName?.setText(data.shelve?.shelveName)
        val dateUpdatedString: String =
            DateHelper.getDateInDateMonthYearTimeFormat(data.timeUpdated, null)
        txtShelveCreationDate?.setText(getString(R.string.txt_created_by) + " " + data.createdBy?.firstName + " " + data.createdBy?.lastName + " (" + dateUpdatedString + ")")
        txtEstimatedTotal?.setText("Est. value: " + data.estimatedTotalValue?.displayValue)
        val productList: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        for (i in data.products?.indices!!) {
            val item = InventoryDataManager.ShelveProductListUIModel()
            item.isDeleteThisStockCountMessage = false
            item.isBuyerAdminMessage = false
            item.inventoryProduct = data.products!!.get(i)
            productList.add(item)
        }
        Collections.sort<ShelveProductListUIModel>(
            productList,
            object : Comparator<ShelveProductListUIModel?> {
                override fun compare(a: ShelveProductListUIModel?, b: ShelveProductListUIModel?): Int {
                    return a?.inventoryProduct!!.productName!!.compareTo(b?.inventoryProduct!!.productName!!)
                }
            })/*InventoryDataManager.ShelveProductListUIModel itemDeleteMessage = new InventoryDataManager.ShelveProductListUIModel();
        itemDeleteMessage.setDeleteThisStockCountMessage(true);
        itemDeleteMessage.setBuyerAdminMessage(false);
        productList.add(itemDeleteMessage);*/
        lstShelveProductList?.setAdapter(
            StockProductListAdapter(
                this,
                productList,
                stockageID
            )
        )
    }
}