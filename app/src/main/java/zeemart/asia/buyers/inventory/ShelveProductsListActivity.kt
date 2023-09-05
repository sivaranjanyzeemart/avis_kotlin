package zeemart.asia.buyers.inventory

import android.content.Intent
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
import zeemart.asia.buyers.adapter.ShelveProductListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.models.StockageType
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.inventory.*
import zeemart.asia.buyers.network.OutletInventoryApi
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.util.*

class ShelveProductsListActivity() : AppCompatActivity() {
    private var txtShelveName: TextView? = null
    private var txtShelveCreationDate: TextView? = null
    private var txtShelveItemCount: TextView? = null
    private var edtSearchListText: EditText? = null
    private var layoutNoCount: RelativeLayout? = null
    private var lytStartStockCount: RelativeLayout? = null
    var lstShelveProductList: RecyclerView? = null
    private var shelve: Shelve? = null
    private var isLastStockCountAvailable = false
    private var isNoItems = false
    private var txtNoStockCountYet: TextView? = null
    private var txtStartStockCount: TextView? = null
    private var txtEstimatedTotal: TextView? = null
    private var edtSearchClear: ImageView? = null
    private var posIntegration = false
    private var estimatedTotalValue: String? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_list)
        txtShelveName = findViewById<TextView>(R.id.txt_heading)
        txtShelveCreationDate = findViewById<TextView>(R.id.txt_date_time)
        txtShelveItemCount = findViewById<TextView>(R.id.txt_item_count)
        txtEstimatedTotal = findViewById<TextView>(R.id.txt_total_price_count)
        txtStartStockCount = findViewById<TextView>(R.id.txt_start_stock_count_product_list_shelve)
        edtSearchListText = findViewById<EditText>(R.id.edit_search)
        edtSearchListText!!.setFocusable(false)
        edtSearchListText!!.setFocusableInTouchMode(false)
        edtSearchClear = findViewById<ImageView>(R.id.edit_search_clear)
        edtSearchListText!!.setHint(getResources().getString(R.string.txt_search_sku))
        edtSearchListText!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (SharedPref.defaultOutlet != null) {
                    AnalyticsHelper.logActionForInventory(
                        this@ShelveProductsListActivity,
                        AnalyticsHelper.TAP_INVENTORY_LIST_SEARCH,
                        SharedPref.defaultOutlet!!
                    )
                }
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    CommonMethods.hideKeyboard(this@ShelveProductsListActivity)
                }
                return false
            }
        })
        edtSearchListText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText: String = edtSearchListText!!.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear!!.visibility = View.VISIBLE
                } else {
                    edtSearchClear!!.visibility = View.GONE
                }
                if (lstShelveProductList?.getAdapter() != null) {
                    (lstShelveProductList?.getAdapter() as ShelveProductListAdapter).getFilter()
                        .filter(s)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        edtSearchClear!!.setOnClickListener(View.OnClickListener { edtSearchListText!!.setText("") })
        lstShelveProductList = findViewById<RecyclerView>(R.id.lst_shelve_product_list)
        lstShelveProductList!!.setLayoutManager(LinearLayoutManager(this))
        lytStartStockCount = findViewById<RelativeLayout>(R.id.lyt_start_stock_count)
        lytStartStockCount!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (SharedPref.defaultOutlet != null) {
                    AnalyticsHelper.logActionForInventory(
                        this@ShelveProductsListActivity,
                        AnalyticsHelper.TAP_INVENTORY_LISTS_LIST_COUNT,
                        SharedPref.defaultOutlet!!
                    )
                }
                startStockCountClicked()
            }
        })
        layoutNoCount = findViewById<RelativeLayout>(R.id.lyt_no_stock_count_using_this_list)
        layoutNoCount!!.setVisibility(View.GONE)
        txtNoStockCountYet = findViewById<TextView>(R.id.txt_no_stock_count_using_this_list)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            val shelveJson: String? = bundle.getString(InventoryDataManager.INTENT_SHELVE_ID)
            shelve =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<Shelve>(shelveJson, Shelve::class.java)
            isNoItems = bundle.getBoolean(InventoryDataManager.INTENT_SHELVE_NO_ITEMS)
            getStockInventoryData(shelve)
            txtShelveName!!.setText(shelve!!.shelveName + "")
            estimatedTotalValue = bundle.getString(InventoryDataManager.INTENT_SHELVE_VALUE)
        }

//        if (isNoItems) {
//            lytStartStockCount.setVisibility(View.GONE);
//        } else {
//            lytStartStockCount.setVisibility(View.VISIBLE);
//        }
        setFonts()
    }

    private fun setFonts() {
        ZeemartBuyerApp.setTypefaceView(
            txtShelveName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtShelveItemCount,
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
            txtNoStockCountYet,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtStartStockCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEstimatedTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun startStockCountClicked() {
        val newIntent = Intent(this, StockCountActivity::class.java)
        val shelveJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(shelve)
        newIntent.putExtra(InventoryDataManager.INTENT_SHELVE_ID, shelveJson)
        newIntent.putExtra(
            InventoryDataManager.INTENT_IS_LAST_STOCK_COUNT_DATA_AVAILABLE,
            isLastStockCountAvailable
        )
        startActivity(newIntent)
    }

    protected override fun onResume() {
        super.onResume()
        getStockInventoryData(shelve)
    }

    private fun getStockInventoryData(shelve: Shelve?) {
        OutletsApi.getSpecificOutlet(this, object : OutletsApi.GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                if ((specificOutlet?.data?.posIntegration != null) && specificOutlet.data!!.posIntegration!!.posIntegration == true) {
                    posIntegration = specificOutlet.data!!.posIntegration!!.posIntegration!!
                }
            }

            override fun onError(error: VolleyErrorHelper?) {}
        })
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.DESC)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.TIME_UPDATED)
        apiParamsHelper.setShelveIds(arrayOf<String?>(shelve?.shelveId))
        apiParamsHelper.setStockageTypes(arrayOf<StockageType?>(StockageType.STOCK_COUNT))
        apiParamsHelper.setRemoveSquareBrackets(true)
        OutletInventoryApi.getCountEntriesByOutlet(
            this,
            apiParamsHelper,
            object : OutletInventoryApi.OutletStockCountEntriesListener {
                override fun onStockShelveListSuccess(stockCountEntriesByOutletData: InventoryResponse.StockCountEntriesByOutlet?) {
                    if ((stockCountEntriesByOutletData != null) && (stockCountEntriesByOutletData.data != null
                                ) && (stockCountEntriesByOutletData.data!!
                            .data != null) && (stockCountEntriesByOutletData.data!!
                            .data!!.size!! > 0)
                    ) {
                        layoutNoCount?.setVisibility(View.GONE)
                        edtSearchListText?.setFocusable(true)
                        edtSearchListText?.setFocusableInTouchMode(true)
                        for (i in 0 until stockCountEntriesByOutletData.data!!.data!!
                            .size) {
                            //get the stock count entry for the first active status entry
                            if (stockCountEntriesByOutletData.data!!.data!!.get(i).status
                                    .equals(StockCountEntries.InventoryActivityStatus.ACTIVE.value)
                            ) {
                                val stockCountEntriesMgr: StockCountEntriesMgr =
                                    stockCountEntriesByOutletData.data!!.data?.get(i) as StockCountEntriesMgr
                                shelve?.timeLastCounted = stockCountEntriesMgr.countDate?.times(1000)
                                //get the products in the shelve from the products API
                                val apiParamsHelperProducts = ApiParamsHelper()
                                apiParamsHelperProducts.setSortOrder(ApiParamsHelper.SortOrder.ASC)
                                apiParamsHelperProducts.setSortBy(ApiParamsHelper.SortField.PRODUCT_NAME)
                                apiParamsHelperProducts.setShelveId(shelve?.shelveId!!)
                                apiParamsHelperProducts.setRemoveSquareBrackets(true)
                                OutletInventoryApi.getProductForShelve(
                                    this@ShelveProductsListActivity,
                                    apiParamsHelperProducts,
                                    object : OutletInventoryApi.ProductsByShelveDataListener {
                                        override fun onStockShelveListSuccess(productsByShelveId: InventoryResponse.ProductsByShelveId?) {
                                            if ((productsByShelveId != null) && (productsByShelveId.data != null
                                                        ) && (productsByShelveId.data!!
                                                    .data != null) && (productsByShelveId.data!!
                                                    .data?.size!! > 0)
                                            ) {
                                                val inventoryProducts: List<InventoryProduct> =
                                                    productsByShelveId.data!!.data!!
                                                setUIDetailsWithStocks(
                                                    stockCountEntriesMgr,
                                                    inventoryProducts
                                                )
                                                isLastStockCountAvailable = true
                                            }
                                        }

                                        override fun errorResponse(error: VolleyError?) {}
                                    })
                                break
                            }
                        }
                    } else {
                        //get the products in the shelve from the products API
                        val apiParamsHelperProducts = ApiParamsHelper()
                        apiParamsHelperProducts.setSortOrder(ApiParamsHelper.SortOrder.ASC)
                        apiParamsHelperProducts.setSortBy(ApiParamsHelper.SortField.PRODUCT_NAME)
                        apiParamsHelperProducts.setShelveId(shelve?.shelveId!!)
                        apiParamsHelperProducts.setIncludeFields(
                            arrayOf<ApiParamsHelper.IncludeFields>(
                                ApiParamsHelper.IncludeFields.SUPPLIER,
                                ApiParamsHelper.IncludeFields.PRODUCT_NAME,
                                ApiParamsHelper.IncludeFields.PAR_LEVEL
                            ) as Array<ApiParamsHelper.IncludeFields?>
                        )
                        apiParamsHelperProducts.setRemoveSquareBrackets(true)
                        OutletInventoryApi.getProductForShelve(
                            this@ShelveProductsListActivity,
                            apiParamsHelperProducts,
                            object : OutletInventoryApi.ProductsByShelveDataListener {
                                override fun onStockShelveListSuccess(productsByShelveId: InventoryResponse.ProductsByShelveId?) {
                                    layoutNoCount?.setVisibility(View.VISIBLE)
                                    edtSearchListText?.setFocusable(true)
                                    edtSearchListText?.setFocusableInTouchMode(true)
                                    if ((productsByShelveId != null) && (productsByShelveId.data != null
                                                ) && (productsByShelveId.data!!
                                            .data != null) && (productsByShelveId.data!!
                                            .data!!.size > 0)
                                    ) {
                                        val inventoryProducts: List<InventoryProduct> =
                                            productsByShelveId.data!!.data!!
                                        setUIDetailsWithoutStock(inventoryProducts)
                                        isLastStockCountAvailable = false
                                    }
                                }

                                override fun errorResponse(error: VolleyError?) {}
                            })
                    }
                }

                override fun errorResponse(error: VolleyError?) {}
            })
    }

    private fun setUIDetailsWithoutStock(inventoryProducts: List<InventoryProduct>) {
        txtShelveCreationDate?.setVisibility(View.INVISIBLE)
        val stockInventoryProductList: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        val stockInventoryProductListZeroQuantityProducts: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        val stockInventoryProductListBelowParProducts: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        for (i in inventoryProducts.indices) {
            val item = InventoryDataManager.ShelveProductListUIModel()
            val stockInventoryProduct = StockCountEntries.StockInventoryProduct()
            stockInventoryProduct.supplier = inventoryProducts[i].supplier
            stockInventoryProduct.productId = inventoryProducts[i].productId
            stockInventoryProduct.unitSize = inventoryProducts[i].unitSize
            stockInventoryProduct.parLevel = inventoryProducts[i].parLevel!!
            stockInventoryProduct.isBelowParLevel = inventoryProducts[i].belowParLevel
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                ) == true && !StringHelper.isStringNullOrEmpty(
                    inventoryProducts[i].customName
                )
            ) {
                stockInventoryProduct.productName = inventoryProducts[i].customName
            } else {
                stockInventoryProduct.productName = inventoryProducts[i].productName
            }
            item.inventoryProduct = stockInventoryProduct
            item.isBuyerAdminMessage = false
            item.isDeleteThisStockCountMessage = false
            if (item.inventoryProduct!!.stockQuantity != null && item.inventoryProduct!!.stockQuantity == 0.0) {
                stockInventoryProductListZeroQuantityProducts.add(item)
            } else if (item.inventoryProduct!!.getBelowParLevel()) {
                stockInventoryProductListBelowParProducts.add(item)
            } else {
                stockInventoryProductList.add(item)
            }
        }
        Collections.sort<InventoryDataManager.ShelveProductListUIModel>(
            stockInventoryProductList,
            object : Comparator<InventoryDataManager.ShelveProductListUIModel> {
                override fun compare(
                    a: InventoryDataManager.ShelveProductListUIModel,
                    b: InventoryDataManager.ShelveProductListUIModel
                ): Int {
                    return a.inventoryProduct?.productName?.compareTo(b.inventoryProduct?.productName!!)!!
                }
            })
        stockInventoryProductListZeroQuantityProducts.addAll(
            stockInventoryProductListBelowParProducts
        )
        stockInventoryProductListZeroQuantityProducts.addAll(stockInventoryProductList)
        val buyerAdminMessageItem = InventoryDataManager.ShelveProductListUIModel()
        buyerAdminMessageItem.isBuyerAdminMessage = true
        buyerAdminMessageItem.isDeleteThisStockCountMessage = false
        stockInventoryProductListZeroQuantityProducts.add(buyerAdminMessageItem)
        lstShelveProductList?.setAdapter(
            ShelveProductListAdapter(
                this,
                stockInventoryProductListZeroQuantityProducts,
                null,
                shelve!!,
                isLastStockCountAvailable,
                posIntegration
            )
        )
        if (inventoryProducts.size == 1) {
            txtShelveItemCount!!.setText(inventoryProducts.size.toString() + " " + getString(R.string.txt_item))
        } else {
            txtShelveItemCount!!.setText(inventoryProducts.size.toString() + " " + getString(R.string.txt_items))
        }
    }

    private fun setUIDetailsWithStocks(
        shelveDetails: StockCountEntries,
        inventoryProducts: List<InventoryProduct>
    ) {
        val dateCreated: Long = shelveDetails.timeCreated!!
        val countDate: Long = shelveDetails.countDate!!
        val dateUpdatedString: String =
            DateHelper.getDateInDateMonthYearTimeFormat(dateCreated, null)
        val countDateString: String = DateHelper.getDateInDateMonthYearFormat(countDate, null)
        if (!StringHelper.isStringNullOrEmpty(countDateString)) {
            txtShelveCreationDate?.setText(
                kotlin.String.format(
                    getResources().getString(R.string.txt_count_date_updated_on),
                    countDateString,
                    dateUpdatedString
                )
            )
        } else {
            txtShelveCreationDate?.setText("Updated $dateUpdatedString")
        }
        if (posIntegration) {
            if (StringHelper.isStringNullOrEmpty(estimatedTotalValue)) {
                txtEstimatedTotal?.setText("Value (on hand):" + " " + shelveDetails.estimatedTotalValue?.displayValue)
            } else {
                txtEstimatedTotal?.setText("Value (on hand): $estimatedTotalValue")
            }
        } else {
            txtEstimatedTotal?.setText(getResources().getString(R.string.txt_value_during) + " " + shelveDetails.estimatedTotalValue?.displayValue)
        }
        if (!UserPermission.HasViewPrice()) {
            txtEstimatedTotal?.setVisibility(View.GONE)
        } else {
            txtEstimatedTotal?.setVisibility(View.VISIBLE)
        }
        val stockInventoryList: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        val stockInventoryProductListZeroQuantityProducts: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        val stockInventoryProductListBelowParProducts: MutableList<InventoryDataManager.ShelveProductListUIModel> =
            ArrayList<InventoryDataManager.ShelveProductListUIModel>()
        for (i in inventoryProducts.indices) {
            val item = InventoryDataManager.ShelveProductListUIModel()
            val stockInventoryProduct = StockCountEntries.StockInventoryProduct()
            stockInventoryProduct.supplier = inventoryProducts[i].supplier
            stockInventoryProduct.productId = inventoryProducts[i].productId
            stockInventoryProduct.parLevel = inventoryProducts[i].parLevel!!
            stockInventoryProduct.isBelowParLevel = inventoryProducts[i].belowParLevel
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                ) == true && !StringHelper.isStringNullOrEmpty(
                    inventoryProducts[i].customName
                )
            ) {
                stockInventoryProduct.productName = inventoryProducts[i].customName
            } else {
                stockInventoryProduct.productName = inventoryProducts[i].productName
            }
            stockInventoryProduct.unitSize = inventoryProducts[i].unitSize
            if ((inventoryProducts[i].latestStockCounts != null) && (inventoryProducts[i].latestStockCounts?.size!! > 0)
                && (inventoryProducts[i].latestStockCounts?.get(
                    0
                )?.quantity != null)
            ) stockInventoryProduct.stockQuantity =
                inventoryProducts[i].latestStockCounts?.get(0)?.quantity
            if ((inventoryProducts[i].latestStockCounts != null) && (inventoryProducts[i].latestStockCounts?.size!! > 0)
                && (inventoryProducts[i].latestStockCounts?.get(
                    0
                )?.onHandQuantity != null)
            ) stockInventoryProduct.onHandQuantity =
                inventoryProducts[i].latestStockCounts?.get(0)?.onHandQuantity
            item.inventoryProduct = stockInventoryProduct
            item.isBuyerAdminMessage = false
            item.isDeleteThisStockCountMessage = false
            if (item.inventoryProduct!!.stockQuantity != null && item.inventoryProduct!!.stockQuantity == 0.0) {
                stockInventoryProductListZeroQuantityProducts.add(item)
            } else if (item.inventoryProduct!!.getBelowParLevel()) {
                stockInventoryProductListBelowParProducts.add(item)
            } else {
                stockInventoryList.add(item)
            }
        }
        //        //sort stockInventoryListByQuantity
//        Collections.sort(stockInventoryList, new Comparator<InventoryDataManager.ShelveProductListUIModel>() {
//            @Override
//            public int compare(InventoryDataManager.ShelveProductListUIModel o1, InventoryDataManager.ShelveProductListUIModel o2) {
//                if (o1.getInventoryProduct().getStockQuantity() == null) {
//                    return (o2.getInventoryProduct().getStockQuantity() == null) ? stockInventoryList.size()-1 : -1;
//                }
//                if (o2.getInventoryProduct().getStockQuantity() == null) {
//                    return  stockInventoryList.size()-1 ;
//                }
//                return o1.getInventoryProduct().getStockQuantity().compareTo(o2.getInventoryProduct().getStockQuantity());
//
//            }
//        });
        Collections.sort<InventoryDataManager.ShelveProductListUIModel>(
            stockInventoryList,
            object : Comparator<InventoryDataManager.ShelveProductListUIModel> {
                override fun compare(
                    a: InventoryDataManager.ShelveProductListUIModel,
                    b: InventoryDataManager.ShelveProductListUIModel
                ): Int {
                    return a.inventoryProduct?.productName?.compareTo(b.inventoryProduct?.productName!!)!!
                }
            })
        stockInventoryProductListZeroQuantityProducts.addAll(
            stockInventoryProductListBelowParProducts
        )
        stockInventoryProductListZeroQuantityProducts.addAll(stockInventoryList)
        val itemAdminMessage = InventoryDataManager.ShelveProductListUIModel()
        itemAdminMessage.isBuyerAdminMessage = true
        itemAdminMessage.isDeleteThisStockCountMessage = false
        stockInventoryProductListZeroQuantityProducts.add(itemAdminMessage)
        //if its an active entry add delete button hide delete button for now
        /*if(shelveDetails.getStatus().equals(InventoryActivityStatus.ACTIVE.getValue())){
            InventoryDataManager.ShelveProductListUIModel itemDeleteMessage = new InventoryDataManager.ShelveProductListUIModel();
            itemDeleteMessage.setBuyerAdminMessage(false);
            itemDeleteMessage.setDeleteThisStockCountMessage(true);
            stockInventoryList.add(itemDeleteMessage);
        }*/lstShelveProductList?.setAdapter(
            ShelveProductListAdapter(
                this,
                stockInventoryProductListZeroQuantityProducts,
                shelveDetails.stockageId,
                shelve!!,
                isLastStockCountAvailable,
                posIntegration
            )
        )
        if (inventoryProducts.size == 1) {
            txtShelveItemCount?.setText(inventoryProducts.size.toString() + " " + getString(R.string.txt_item))
        } else {
            txtShelveItemCount?.setText(inventoryProducts.size.toString() + " " + getString(R.string.txt_items))
        }
    }
}