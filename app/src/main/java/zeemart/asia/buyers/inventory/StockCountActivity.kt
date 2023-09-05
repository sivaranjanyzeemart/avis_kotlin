package zeemart.asia.buyers.inventory

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.StockCountProductListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DialogHelper.DialogButtonClickListener
import zeemart.asia.buyers.helper.DialogHelper.DialogThreeButtonClickListener
import zeemart.asia.buyers.helper.DialogHelper.alertDialogSmallFailure
import zeemart.asia.buyers.helper.DialogHelper.alertDialogSmallSuccess
import zeemart.asia.buyers.helper.DialogHelper.setBasicAlertDialogWithTitleAndMessage
import zeemart.asia.buyers.helper.DialogHelper.setThreeButtonAlertDialogWithTitleAndMessage
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.InventoryDataManager.CountStockProductListUIModel
import zeemart.asia.buyers.models.StockageType
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.InventorySaveStockRequest
import zeemart.asia.buyers.models.inventory.Shelve
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.models.inventory.StockCountEntries.StockInventoryProduct
import zeemart.asia.buyers.network.OutletInventoryApi
import java.util.*
import java.util.concurrent.TimeUnit

class StockCountActivity : AppCompatActivity() {
    private lateinit var edtSearchSKU: EditText
    private lateinit var btnClose: ImageView
    private lateinit var btnSaveStockCount: Button
    private lateinit var lstShelveProductList: RecyclerView
    private var isLastStockDataAvailable = false
    private lateinit var shelve: Shelve
    private lateinit var txtStockCountHeading: TextView
    private lateinit var productsForShelve: InventoryResponse.ProductsByShelveId
    var stockCountProductsList: MutableList<CountStockProductListUIModel>? = ArrayList()
    private lateinit var edtSearchClear: ImageView
    private lateinit var lytReceivedDate: RelativeLayout
    private lateinit var txtReceivedDateHeader: TextView
    private lateinit var txtReceivedDateValue: TextView
    private var receivedDate = 0L
    lateinit var stockCountProductListAdapter: StockCountProductListAdapter
    var dateListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        myCalendar[Calendar.YEAR] = year
        myCalendar!![Calendar.MONTH] = monthOfYear
        myCalendar!![Calendar.DAY_OF_MONTH] = dayOfMonth
        updateLabel()
    }
    private lateinit var myCalendar: Calendar
    private lateinit var lytTodaysCount: LinearLayout
    private lateinit var txtTodaysCount: TextView
    private lateinit var imgSelect: ImageView
    private var isTodaysCountSelected = true
    private lateinit var dashboardListLayoutManager: LinearLayoutManager
    private lateinit var threeDotLoader: CustomLoadingViewWhite
    private lateinit var draftStockCount: StockCountEntries
    private var stockageId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_count)
        threeDotLoader = findViewById(R.id.three_dot_loader)
        edtSearchSKU = findViewById(R.id.edit_search)
        edtSearchSKU.setFocusableInTouchMode(false)
        edtSearchSKU.setFocusable(false)
        edtSearchClear = findViewById(R.id.edit_search_clear)
        edtSearchClear.setVisibility(View.GONE)
        edtSearchSKU.setHint(resources.getString(R.string.txt_search_sku))
        edtSearchSKU.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                CommonMethods.hideKeyboard(this@StockCountActivity)
            }
            false
        })
        edtSearchClear.setOnClickListener(View.OnClickListener { edtSearchSKU.setText("") })
        btnClose = findViewById(R.id.btn_close_stock_count)
        btnSaveStockCount = findViewById(R.id.btn_save_stock_count)
        lstShelveProductList = findViewById(R.id.lst_shelve_product_list)
        dashboardListLayoutManager = LinearLayoutManager(this)
        lstShelveProductList.setLayoutManager(dashboardListLayoutManager)
        txtStockCountHeading = findViewById(R.id.txt_stock_count_heading)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(InventoryDataManager.INTENT_SHELVE_ID)) {
                val shelveJson = bundle.getString(InventoryDataManager.INTENT_SHELVE_ID)
                shelve =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(shelveJson, Shelve::class.java)
                getProductsInShelve(shelve)
            }
            if (bundle.containsKey(InventoryDataManager.INTENT_IS_LAST_STOCK_COUNT_DATA_AVAILABLE)) {
                isLastStockDataAvailable =
                    bundle.getBoolean(InventoryDataManager.INTENT_IS_LAST_STOCK_COUNT_DATA_AVAILABLE)
            }
        }
        btnSaveStockCount.setOnClickListener(View.OnClickListener {
            if (SharedPref.defaultOutlet != null) {
                AnalyticsHelper.logActionForInventory(
                    this@StockCountActivity,
                    AnalyticsHelper.TAP_INVENTORY_LISTS_LIST_COUNT_SAVE,
                    SharedPref.defaultOutlet!!
                )
            }
            btnSaveStockClicked()
        })
        btnClose.setOnClickListener(View.OnClickListener { btnCloseClicked() })
        edtSearchSKU.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText = edtSearchSKU.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear.setVisibility(View.VISIBLE)
                } else {
                    edtSearchClear.setVisibility(View.GONE)
                }
                if (lstShelveProductList.getAdapter() != null) {
                    (lstShelveProductList.getAdapter() as StockCountProductListAdapter?)!!.filter.filter(
                        s
                    )
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        txtReceivedDateHeader = findViewById(R.id.txt_received_on)
        txtReceivedDateValue = findViewById(R.id.txt_received_date)
        lytReceivedDate = findViewById(R.id.lyt_received_Date)
        lytReceivedDate.setOnClickListener(View.OnClickListener {
            val mDate = Date(receivedDate * 1000)
            myCalendar = Calendar.getInstance(DateHelper.marketTimeZone)
            myCalendar.setTime(mDate)
            val datePickerDialog = DatePickerDialog(
                this@StockCountActivity, dateListener, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            //following line to restrict future date selection
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            val timeLastCounted = shelve!!.timeLastCounted?.minus(3600)
            val lastMonthDate = calendar.timeInMillis
            if (timeLastCounted!! > lastMonthDate) {
                datePickerDialog.datePicker.minDate = timeLastCounted
            } else {
                datePickerDialog.datePicker.minDate = lastMonthDate
            }
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        })
        val currentTime = System.currentTimeMillis()
        val dateTime = DateHelper.getDateInDateMonthYearFormat(currentTime / 1000)
        txtReceivedDateValue.setText(dateTime)
        receivedDate = currentTime / 1000
        lytTodaysCount = findViewById(R.id.lyt_count_includes)
        txtTodaysCount = findViewById(R.id.txt_items_received_today)
        imgSelect = findViewById(R.id.img_select_skus)
        isTodaysCountSelected = true
        imgSelect.setImageDrawable(resources.getDrawable(R.drawable.icon_select))
        lytTodaysCount.setOnClickListener(View.OnClickListener {
            isTodaysCountSelected = if (isTodaysCountSelected) {
                imgSelect.setImageDrawable(resources.getDrawable(R.drawable.icon_un_select))
                false
            } else {
                imgSelect.setImageDrawable(resources.getDrawable(R.drawable.icon_select))
                true
            }
        })
        setDashboardListScrollBehaviour()
        setFont()
        saveNewStockCountData()
    }

    private fun setDashboardListScrollBehaviour() {
        val totalItems = dashboardListLayoutManager.itemCount
        val lastVisibleItemPosition = dashboardListLayoutManager.findLastVisibleItemPosition()
        if (totalItems > lastVisibleItemPosition) {
            //add on scroll listener
            lstShelveProductList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val currentFirstVisibleItemPosition =
                            dashboardListLayoutManager!!.findFirstVisibleItemPosition()
                        if (currentFirstVisibleItemPosition == 0 && lytReceivedDate!!.visibility != View.GONE) {
                            lytReceivedDate!!.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val currentFirstVisibleItemPosition =
                        dashboardListLayoutManager!!.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition =
                        dashboardListLayoutManager!!.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = dashboardListLayoutManager!!.itemCount
                    if (currentFirstVisibleItemPosition > 0) {
                        lytReceivedDate!!.visibility = View.GONE
                    } else if (dy < 0 && lytReceivedDate!!.visibility == View.GONE && currentFirstVisibleItemPosition == 0) {
                        lytReceivedDate!!.visibility = View.VISIBLE
                    } else if (currentFirstVisibleItemPosition == 0 && lastVisibleItemPosition == totalItemCount - 1 && dy > 0) {
                        lytReceivedDate!!.visibility = View.VISIBLE
                    }
                }
            })
        } else {
            lstShelveProductList.clearOnScrollListeners()
        }
    }

    private fun updateLabel() {
        val dateTime = DateHelper.getDateInDateMonthYearFormat(myCalendar!!.timeInMillis / 1000)
        txtReceivedDateValue!!.text = dateTime
        receivedDate = myCalendar!!.timeInMillis / 1000
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtStockCountHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            edtSearchSKU,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSaveStockCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceivedDateHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReceivedDateValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTodaysCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun btnCloseClicked() {
        setThreeButtonAlertDialogWithTitleAndMessage(this,
            getString(R.string.txt_exit_draft_inventory),
            getString(
                R.string.txt_exit_draft_inventory_message
            ),
            getString(R.string.txt_save_inventory_exit_without),
            getString(
                R.string.txt_save_draft_inventory
            ),
            getString(R.string.txt_cancel),
            object : DialogThreeButtonClickListener {
                override fun positiveButtonClicked() {
                    saveDraftStockCountData()
                }

                override fun negativeButtonClicked() {
                    finish()
                }

                override fun neutralButtonClicked() {}
            })
    }

    private fun btnFillFromPreviousStockCountClick() {
        setBasicAlertDialogWithTitleAndMessage(this,
            getString(R.string.txt_use_last_count_data),
            getString(
                R.string.txt_replace_with_last_count_data
            ),
            getString(R.string.dialog_cancel_button_text),
            getString(
                R.string.txt_use_last_count_data_dialog_button
            ),
            object : DialogButtonClickListener {
                override fun positiveButtonClicked() {
                    callStockEntryAPIGetLastCountData()
                }

                override fun negativeButtonClicked() {}
            })
    }

    private fun callStockEntryAPIGetLastCountData() {
        threeDotLoader!!.visibility = View.VISIBLE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.DESC)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.TIME_UPDATED)
        apiParamsHelper.setShelveIds(arrayOf(shelve!!.shelveId))
        apiParamsHelper.setStockageTypes(arrayOf(StockageType.STOCK_COUNT))
        apiParamsHelper.setRemoveSquareBrackets(true)
        OutletInventoryApi.getCountEntriesByOutlet(
            this,
            apiParamsHelper,
            object : OutletInventoryApi.OutletStockCountEntriesListener {
                override fun onStockShelveListSuccess(stockCountEntriesByOutletData: InventoryResponse.StockCountEntriesByOutlet?) {
                    threeDotLoader!!.visibility = View.GONE
                    if (stockCountEntriesByOutletData != null && stockCountEntriesByOutletData.data != null
                        && stockCountEntriesByOutletData.data!!.data != null && stockCountEntriesByOutletData.data!!.data?.size!! > 0) {
                        for (i in stockCountEntriesByOutletData.data!!.data?.indices!!) {
                            //get the stock count entry for the first active status entry
                            if (stockCountEntriesByOutletData.data!!.data!![i].status == StockCountEntries.InventoryActivityStatus.ACTIVE.value) {
                                val activeStockCount = stockCountEntriesByOutletData.data!!.data!![i]
                                setRetrieveRecords(activeStockCount.products)
                                //stockCountProductsList = createProductQuantityList(activeStockCount.getProducts());
                                setSaveCountButtonSettings()
                                setAdapterForProducts(stockCountProductsList)
                                break
                            }
                        }
                    }
                }

                override fun errorResponse(error: VolleyError?) {
                    threeDotLoader!!.visibility = View.GONE
                }
            })
    }

    private fun createProductQuantityList(data: List<StockInventoryProduct>): List<CountStockProductListUIModel?> {
        val productQuantityListData: MutableList<CountStockProductListUIModel?> = ArrayList<CountStockProductListUIModel?>()
        val inventoryProductList = productsForShelve!!.data?.data
        if (productsForShelve != null && inventoryProductList?.size!! > 0) {
            for (i in inventoryProductList?.indices!!) {
                var lastStockQuantity: Double? = null
                for (j in data.indices) {
                    if (data[j].productId == inventoryProductList[i].productId) {
                        lastStockQuantity = data[j].stockQuantity
                        break
                    }
                }
                val productData = CountStockProductListUIModel()
                productData.customName = inventoryProductList[i].customName
                if (SharedPref.readBool(
                        SharedPref.USER_INVENTORY_SETTING_STATUS,
                        false
                    )!! && inventoryProductList[i].customName != null && !StringHelper.isStringNullOrEmpty(
                        inventoryProductList[i].customName
                    )
                ) {
                    productData.productName = inventoryProductList[i].customName
                } else {
                    productData.productName = inventoryProductList[i].productName
                }
                productData.productId = inventoryProductList[i].productId
                productData.unitSize = inventoryProductList[i].unitSize
                productData.quantity = lastStockQuantity
                productData.supplier = inventoryProductList[i].supplier
                productQuantityListData.add(productData)
            }
        }
        return productQuantityListData
    }

    private fun saveNewStockCountData() {
        if (stockCountProductsList!!.size > 0) {
            val productQuantityList: MutableList<InventorySaveStockRequest.Product> = ArrayList()
            for (i in stockCountProductsList!!.indices) {
                val product = InventorySaveStockRequest.Product()
                if (!StringHelper.isStringNullOrEmpty(stockCountProductsList!![i].productId) && stockCountProductsList!![i].quantity != null) {
                    product.productId = stockCountProductsList!![i].productId
                    product.stockQuantity = stockCountProductsList!![i].quantity
                    productQuantityList.add(product)
                }
            }
            threeDotLoader!!.visibility = View.VISIBLE
            val request = InventoryDataManager.createJsonRequestForSaveStockCount(
                productQuantityList,
                stockageId,
                " ",
                isTodaysCountSelected,
                shelve,
                receivedDate
            )
            Log.d("api", "saveNewStockCountData: $request")
            OutletInventoryApi.createStockCountEntry(
                this,
                request,
                object : OutletInventoryApi.StockageEntryResponseListener {
                    override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                        threeDotLoader!!.visibility = View.GONE
                        val dialogSuccess = alertDialogSmallSuccess(
                            this@StockCountActivity, getString(
                                R.string.txt_saved
                            )
                        )
                        dialogSuccess.show()
                        Handler().postDelayed({
                            dialogSuccess.dismiss()
                            finish()
                        }, 2000)
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        threeDotLoader!!.visibility = View.GONE
                        val dialogFailure = alertDialogSmallFailure(
                            this@StockCountActivity, getString(
                                R.string.txt_saving_failed
                            ), getString(R.string.txt_please_try_again)
                        )
                        dialogFailure.show()
                        Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                    }
                })
        }
    }

    private fun saveDraftStockCountData() {
        if (stockCountProductsList!!.size > 0) {
            val productQuantityList: MutableList<InventorySaveStockRequest.Product> = ArrayList()
            for (i in stockCountProductsList!!.indices) {
                val product = InventorySaveStockRequest.Product()
                if (!StringHelper.isStringNullOrEmpty(stockCountProductsList!![i].productId) && stockCountProductsList!![i].quantity != null) {
                    product.productId = stockCountProductsList!![i].productId
                    product.stockQuantity = stockCountProductsList!![i].quantity
                    productQuantityList.add(product)
                }
            }
            threeDotLoader!!.visibility = View.VISIBLE
            val request = InventoryDataManager.createJsonRequestForSaveStockCount(
                productQuantityList,
                stockageId,
                "DRAFT",
                isTodaysCountSelected,
                shelve,
                receivedDate
            )
            OutletInventoryApi.createStockCountEntry(
                this,
                request,
                object : OutletInventoryApi.StockageEntryResponseListener {
                    override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                        threeDotLoader!!.visibility = View.GONE
                        val dialogSuccess = alertDialogSmallSuccess(
                            this@StockCountActivity, getString(
                                R.string.txt_saved
                            )
                        )
                        dialogSuccess.show()
                        Handler().postDelayed({
                            dialogSuccess.dismiss()
                            finish()
                        }, 2000)
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        threeDotLoader!!.visibility = View.GONE
                        val dialogFailure = alertDialogSmallFailure(
                            this@StockCountActivity, getString(
                                R.string.txt_saving_failed
                            ), getString(R.string.txt_please_try_again)
                        )
                        dialogFailure.show()
                        Handler().postDelayed({ dialogFailure.dismiss() }, 2000)
                    }
                })
        }
    }

    private fun getDraftInShelve(shelve: Shelve?) {
        OutletInventoryApi.getDraftStockCount(
            this@StockCountActivity,
            shelve!!.shelveId,
            null,
            object : OutletInventoryApi.StockageEntryResponseListener {
                override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                    if (data != null && data.data != null) {
                        draftStockCount = data.data!!
                        stockageId = draftStockCount.stockageId!!
                        showDraftExisitingAlert()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {}
            })
    }

    private fun deleteDraft() {
        OutletInventoryApi.deleteDraftStockCount(
            this@StockCountActivity,
            stockageId,
            null,
            object : OutletInventoryApi.StockageEntryResponseListener {
                override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                    if (data != null && data.status == 200) {
                        stockageId = ""
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {}
            })
    }

    private fun showDraftExisitingAlert() {
        val countDate = draftStockCount!!.countDate
        val draftSaved = countDate?.times(1000)
        val lastCountedDate = Date(draftSaved!!)
        val todayDate = Date()
        val differenceMap = DateHelper.computeDateDiff(lastCountedDate, todayDate)
        var updatedTimeText = ""
        updatedTimeText = if (differenceMap[TimeUnit.DAYS] == 0L) {
            if (differenceMap[TimeUnit.HOURS] == 0L) {
                updatedTimeText + differenceMap[TimeUnit.MINUTES] + " " + getString(
                    R.string.txt_minutes_ago
                )
            } else {
                updatedTimeText + differenceMap[TimeUnit.HOURS] + " " + getString(
                    R.string.txt_hours_ago
                )
            }
        } else {
            val count = DateHelper.getDaysBetweenDates(System.currentTimeMillis(), draftSaved)
            if (count == 1) {
                updatedTimeText + count + " " + getString(R.string.txt_day_ago)
            } else {
                updatedTimeText + count + " " + getString(R.string.txt_days_ago)
            }
        }
        val title =
            String.format(getString(R.string.txt_inventory_draft_previously_saved), updatedTimeText)
        SetBasicAlertDialogWithTitleAndMessage(this,
            title,
            getString(R.string.txt_inventory_draft_continue),
            getString(
                R.string.txt_inventory_start_new
            ),
            getString(R.string.txt_inventory_use_draft),
            object : DialogButtonClickListener {
                override fun positiveButtonClicked() {
                    // stockCountProductsList = createProductQuantityList(draftStockCount.getProducts());
                    setRetrieveRecords(draftStockCount!!.products)
                    setSaveCountButtonSettings()
                    setAdapterForProducts(stockCountProductsList)
                }

                override fun negativeButtonClicked() {
                    deleteDraft()
                }
            })
    }

    private fun setRetrieveRecords(products: List<StockInventoryProduct>?) {
        if (products != null && products.size > 0 && stockCountProductsList != null && stockCountProductsList!!.size > 0) {
            for (i in products.indices) {
                for (j in stockCountProductsList!!.indices) {
                    if (products[i].productId == stockCountProductsList!![j].productId) {
                        stockCountProductsList!![j].quantity = products.get(i).stockQuantity
                        break
                    }
                }
            }
        }
    }

    private fun getProductsInShelve(shelve: Shelve?) {
        threeDotLoader!!.visibility = View.VISIBLE
        val apiParamsHelperProducts = ApiParamsHelper()
        apiParamsHelperProducts.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        apiParamsHelperProducts.setSortBy(ApiParamsHelper.SortField.PRODUCT_NAME)
        apiParamsHelperProducts.setShelveId(shelve!!.shelveId!!)
        apiParamsHelperProducts.setRemoveSquareBrackets(true)
        OutletInventoryApi.getProductForShelve(
            this@StockCountActivity,
            apiParamsHelperProducts,
            object : OutletInventoryApi.ProductsByShelveDataListener {
                override fun onStockShelveListSuccess(productsByShelveId: InventoryResponse.ProductsByShelveId?) {
                    threeDotLoader!!.visibility = View.GONE
                    if (productsByShelveId != null && productsByShelveId.data != null
                        && productsByShelveId.data!!.data != null && productsByShelveId.data!!.data?.size!! > 0) {
                        val inventoryProductList = productsByShelveId.data!!.data
                        productsForShelve = productsByShelveId
                        edtSearchSKU!!.isFocusable = true
                        edtSearchSKU!!.isFocusableInTouchMode = true
                        for (i in inventoryProductList?.indices!!) {
                            val productData = CountStockProductListUIModel()
                            productData.productId = inventoryProductList[i].productId
                            if (SharedPref.readBool(
                                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                                    false
                                )!! && inventoryProductList[i].customName != null && !StringHelper.isStringNullOrEmpty(
                                    inventoryProductList[i].customName
                                )
                            ) {
                                productData.productName = inventoryProductList[i].customName
                            } else {
                                productData.productName = inventoryProductList[i].productName
                            }
                            if (inventoryProductList[i].orderByUnitConversions != null) {
                                productData.orderByUnitConversions =
                                    inventoryProductList[i].orderByUnitConversions
                            }
                            productData.supplier = inventoryProductList[i].supplier
                            productData.unitSize = inventoryProductList[i].unitSize
                            stockCountProductsList!!.add(productData)
                            /* InventorySaveStockRequest.Product product = new InventorySaveStockRequest.Product();
                        product.setProductId(inventoryProductList.get(i).getProductId());
                        productQuantityList.add(product);*/
                        }
                        setAdapterForProducts(stockCountProductsList)
                        getDraftInShelve(shelve)
                    } else {
                        setSaveCountButtonSettings()
                    }
                }

                override fun errorResponse(error: VolleyError?) {
                    threeDotLoader!!.visibility = View.GONE
                }
            })
    }

    fun setAdapterForProducts(productList: List<CountStockProductListUIModel>?) {
        if (productList != null && productList.size > 0) {
            var isHeader = true
            for (i in productList.indices) {
                if (productList[i].isHeader) {
                    isHeader = false
                }
            }
            if (isHeader) {
                val stockProductListUIModel = CountStockProductListUIModel()
                stockProductListUIModel.isHeader = true
                stockCountProductsList!!.add(0, stockProductListUIModel)
            }
        }
        stockCountProductListAdapter = StockCountProductListAdapter(
            this@StockCountActivity,
            isLastStockDataAvailable,
            productList!!,
            object : InventoryItemCountListener {
                override fun onInventoryItemCounted() {
                    //set the save button text and item counted value
                    setSaveCountButtonSettings()
                }

                override fun onCountItemReceivedTodayChecked(isChecked: Boolean) {
                    isLastStockDataAvailable = isChecked
                }

                override fun onAutoFillClicked() {
                    btnFillFromPreviousStockCountClick()
                }
            })
        lstShelveProductList!!.adapter = stockCountProductListAdapter
    }

    private fun btnSaveStockClicked() {
        var count = 0
        for (i in stockCountProductsList!!.indices) {
            if (!stockCountProductsList!![i].isHeader && stockCountProductsList!![i].quantity == null) {
                count = count + 1
            }
        }
        if (count > 0) {
            val notRecordedItems = String.format(getString(R.string.txt_not_recorded), count)
            setBasicAlertDialogWithTitleAndMessage(this@StockCountActivity,
                notRecordedItems,
                getString(
                    R.string.txt_save_with_0
                ),
                getString(R.string.txt_yes_fill_the_blank),
                getString(R.string.dialog_cancel_button_text),
                object : DialogButtonClickListener {
                    override fun positiveButtonClicked() {}
                    override fun negativeButtonClicked() {
                        for (i in stockCountProductsList!!.indices) {
                            if (stockCountProductsList!![i].quantity == null) {
                                stockCountProductsList!![i].quantity = 0.0
                            }
                        }
                        saveNewStockCountData()
                    }
                })
        } else {
            setBasicAlertDialogWithTitleAndMessage(this@StockCountActivity,
                getString(R.string.txt_save_stock_count_really),
                getString(
                    R.string.txt_the_stock_levels_will_be_immediately_updated_upon_saving
                ),
                getString(R.string.dialog_cancel_button_text),
                getString(R.string.txt_yes_save),
                object : DialogButtonClickListener {
                    override fun positiveButtonClicked() {
                        //call the API to save stocks
                        saveNewStockCountData()
                    }

                    override fun negativeButtonClicked() {}
                })
        }
    }

    fun setSaveCountButtonSettings() {
        var count = 0
        //removing the row for include item received today from
        var totalItems = 0
        if (stockCountProductsList!!.size > 0) {
            totalItems = stockCountProductsList!!.size - 1
        }
        for (i in stockCountProductsList!!.indices) {
            if (stockCountProductsList!![i].quantity != null) {
                count = count + 1
            }
        }
        if (totalItems == 1) {
            txtStockCountHeading!!.text = String.format(
                resources.getString(R.string.txt_item_count_stock),
                count,
                totalItems
            )
        } else {
            txtStockCountHeading!!.text = String.format(
                resources.getString(R.string.txt_items_count_stock),
                count,
                totalItems
            )
        }
        btnSaveStockCount!!.isClickable = true
        btnSaveStockCount!!.setBackgroundResource(R.drawable.btn_rounded_green)
    }

    //interface for calculating how many items have been counted
    interface InventoryItemCountListener {
        fun onInventoryItemCounted()
        fun onCountItemReceivedTodayChecked(isChecked: Boolean)
        fun onAutoFillClicked()
    }

    override fun onBackPressed() {
        btnCloseClicked()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 217) {
            if (resultCode == RESULT_OK) {
                val productJson = data!!.getStringExtra(InventoryDataManager.INTENT_PRODUCT_DETAIL)
                val product =
                    ZeemartBuyerApp.fromJson(productJson, CountStockProductListUIModel::class.java)
                Log.d("fatal", "setSaveCountButtonSettings: $productJson")
                if (product != null) {
                    for (i in stockCountProductsList!!.indices) {
                        if (stockCountProductsList!![i].isHeader) {
                        } else {
                            val prodId = stockCountProductsList!![i].productId
                            val productId = product.productId
                            Log.d("fatal", "onActivityResult: $prodId$productId")
                            if (prodId == productId) {
                                stockCountProductsList!![i] = product
                                //                                setAdapterForProducts(stockCountProductsList);
                                stockCountProductListAdapter!!.notifyDataSetChanged()
                                Log.d("fatal", "onActivityResult: ")
                                break
                            } else {
                                Log.d("fatal", "onActivityResult: else")
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun SetBasicAlertDialogWithTitleAndMessage(
            context: Context,
            title: String?,
            message: String?,
            negativeButtonText: String?,
            positiveButtonText: String?,
            listener: DialogButtonClickListener
        ) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle(title)
            if (message != null) {
                alertDialogBuilder.setMessage(message)
            }
            alertDialogBuilder.setNegativeButton(negativeButtonText) { dialog, which -> listener.negativeButtonClicked() }
            alertDialogBuilder.setPositiveButton(positiveButtonText) { dialog, which -> listener.positiveButtonClicked() }
            alertDialogBuilder.setCancelable(false)
            val dialog = alertDialogBuilder.create()
            dialog.show()
            val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negativeButton.setTextColor(context.resources.getColor(R.color.pinky_red))
            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.setTextColor(context.resources.getColor(R.color.color_azul_two))
        }
    }
}