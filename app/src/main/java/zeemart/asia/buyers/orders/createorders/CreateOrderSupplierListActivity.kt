package zeemart.asia.buyers.orders.createordersimport

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SupplierListNewOrderAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.network.DeleteOrder
import zeemart.asia.buyers.network.MarketListApi.retrieveSupplierList
import zeemart.asia.buyers.network.OrderHelper.DraftOrderResponseListener
import zeemart.asia.buyers.network.OrderHelper.returnDraftOrderForSupplier
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.createorders.ReviewOrderActivity
import java.util.*

class CreateOrderSupplierListActivity : AppCompatActivity() {
    private lateinit var lstSupplierCreateNewOrders: RecyclerView
    private lateinit var txtOutletName: Button
    private var threeDotLoaderBlue: CustomLoadingViewBlue? = null
    private lateinit var edtSearchSupplier: EditText
    private var supplierListNewOrder: ArrayList<DetailSupplierDataModel>? = null
    private var txtCreateNewOrderHeader: TextView? = null
    private lateinit var swipeRefreshLayoutSuppliersList: SwipeRefreshLayout
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var edtSearchClear: ImageView
    private lateinit var lytNoFilterResults: RelativeLayout
    private var txtNoFilterResults: TextView? = null
    private var txtDeselectFilters: TextView? = null
    private var outlet: Outlet? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order_supplier_list)
        outlet = SharedPref.defaultOutlet
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_supplier_list_white)
        lstSupplierCreateNewOrders = findViewById(R.id.lst_supplier_list_new_order)
        txtOutletName = findViewById(R.id.txt_outlet_name_supplier_list)
        lytNoFilterResults = findViewById(R.id.lyt_no_filter_results)
        txtDeselectFilters = findViewById(R.id.txt_deselect_filters)
        txtNoFilterResults = findViewById(R.id.txt_no_result)
        txtOutletName.setText(SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, ""))
        txtOutletName.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(
                this@CreateOrderSupplierListActivity,
                AnalyticsHelper.TAP_CREATE_ORDER_SELECT_OUTLET,
                outlet!!
            )
            val newIntent =
                Intent(this@CreateOrderSupplierListActivity, SelectOutletActivity::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_NEW_ORDER
            )
            startActivity(newIntent)
            finish()
        })
        threeDotLoaderBlue = findViewById(R.id.spin_kit_loader_supplier_list_blue)
        swipeRefreshLayoutSuppliersList = findViewById(R.id.swipe_refresh_supplier_list)
        txtCreateNewOrderHeader = findViewById(R.id.txt_create_new_order)
        lstSupplierCreateNewOrders.setVisibility(View.GONE)
        lstSupplierCreateNewOrders.setLayoutManager(LinearLayoutManager(this))
        edtSearchSupplier = findViewById(R.id.edit_search)
        edtSearchClear = findViewById(R.id.edit_search_clear)
        edtSearchSupplier.setHint(resources.getString(R.string.content_create_order_supplier_list_edt_search_supplier_hint))
        //added to track search action by user
        edtSearchSupplier.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                AnalyticsHelper.logAction(
                    this@CreateOrderSupplierListActivity,
                    AnalyticsHelper.TAP_CREATE_ORDER_SEARCH
                )
            }
        })
        edtSearchSupplier.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val searchText = edtSearchSupplier.getText().toString().trim { it <= ' ' }
                if (charSequence.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear.setVisibility(View.VISIBLE)
                    filterProductsOnSearchedText()
                } else {
                    edtSearchClear.setVisibility(View.GONE)
                    lstSupplierCreateNewOrders.setVisibility(View.VISIBLE)
                }
                if (supplierListNewOrder != null && lstSupplierCreateNewOrders.getAdapter() != null) {
                    (lstSupplierCreateNewOrders.getAdapter() as SupplierListNewOrderAdapter?)!!.filter.filter(
                        charSequence
                    )
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
        edtSearchClear.setOnClickListener(View.OnClickListener {
            lytNoFilterResults.setVisibility(View.GONE)
            lstSupplierCreateNewOrders.setVisibility(View.VISIBLE)
            edtSearchSupplier.setText("")
        })
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayoutSuppliersList)
        swipeRefreshLayoutSuppliersList.setOnRefreshListener(OnRefreshListener {
            refreshSupplierList()
            swipeRefreshLayoutSuppliersList.setRefreshing(false)
        })
        refreshSupplierList()
        setFont()
    }

    private fun filterProductsOnSearchedText() {
        val searchedString = edtSearchSupplier!!.text.toString()
        val filteredList: MutableList<DetailSupplierDataModel> = ArrayList()
        if (supplierListNewOrder != null) {
            for (i in supplierListNewOrder!!.indices) {
                if (supplierListNewOrder!![i].supplier.supplierName!!.lowercase(Locale.getDefault())
                        .contains(
                            searchedString.lowercase(
                                Locale.getDefault()
                            )
                        )
                ) {
                    if (!filteredList.contains(supplierListNewOrder!![i])) filteredList.add(
                        supplierListNewOrder!![i]
                    )
                }
            }
        }
        if (filteredList.size == 0) {
            lstSupplierCreateNewOrders!!.visibility = View.INVISIBLE
            lytNoFilterResults!!.visibility = View.VISIBLE
        } else {
            lytNoFilterResults!!.visibility = View.GONE
            lstSupplierCreateNewOrders!!.visibility = View.VISIBLE
        }
    }

    private fun setFont() {
        setTypefaceView(
            txtCreateNewOrderHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(edtSearchSupplier, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtNoFilterResults, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtDeselectFilters, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    private fun checkForExistingDraft(supplierId: String, orders: List<Orders>): Orders? {
        var draftOrder: Orders? = null
        for (i in orders.indices) {
            if (orders[i].orderStatus == OrderStatus.DRAFT.statusName && orders[i].createdBy!!.id == SharedPref.read(
                    SharedPref.USER_ID,
                    ""
                ) && orders[i].supplier!!.supplierId == supplierId
            ) {
                draftOrder = orders[i]
                break
            }
        }
        return draftOrder
    }

    private fun refreshSupplierList() {
        threeDotLoaderBlue!!.visibility = View.VISIBLE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        apiParamsHelper.setOrderEnabled(true)
        //apiParamsHelper.setIncludeFields(new ApiParamsHelper.IncludeFields[]{ApiParamsHelper.IncludeFields.SUPPLIER,ApiParamsHelper.IncludeFields.DELIVERY_DATES});
        val outlets: MutableList<Outlet?> = ArrayList()
        outlets.add(SharedPref.defaultOutlet)
        retrieveSupplierList(this, apiParamsHelper, outlets as List<Outlet>?,
            object : SupplierListResponseListener {
            override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                threeDotLoaderBlue!!.visibility = View.GONE
                if (supplierList != null && supplierList.size > 0) {
                    supplierListNewOrder = ArrayList(supplierList)
                    lstSupplierCreateNewOrders.visibility = View.VISIBLE
                    lstSupplierCreateNewOrders.adapter =
                        SupplierListNewOrderAdapter(this@CreateOrderSupplierListActivity,
                            supplierListNewOrder!!,
                            object : SupplierListNewOrderAdapter.SupplierClickListener {
                                override fun onItemSelected(supplier: DetailSupplierDataModel?) {
                                    moveToProductListActivity(supplier!!)
                                }
                            })
                } else {
                    lstSupplierCreateNewOrders.visibility = View.GONE
                    getToastRed(getString(R.string.txt_no_suppliers_for_outlet))
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {
                val errorMessage = error!!.errorMessage
                val errorType = error!!.errorType
                threeDotLoaderBlue!!.visibility = View.GONE
                if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                    getToastRed(errorMessage)
                }
            }
        })
    }

    private fun createExistingDraftDialog(order: Orders, supplier: DetailSupplierDataModel) {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(getString(R.string.txt_use_draft)) { dialog, which ->
            dialog.dismiss()
            val newIntent =
                Intent(this@CreateOrderSupplierListActivity, ReviewOrderActivity::class.java)
            val orderList: MutableList<Orders> = ArrayList()
            orderList.add(order)
            val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList)
            newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
            val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList[0].supplier)
            val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList[0].outlet)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
            startActivity(newIntent)
        }
        builder.setNegativeButton(getString(R.string.txt_discard_draft)) { dialog, which -> //call the delete draft API
            DeleteOrder(
                this@CreateOrderSupplierListActivity,
                object : DeleteOrder.GetResponseStatusListener {
                    override fun onSuccessResponse(status: String?) {
                        dialog.dismiss()
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        dialog.dismiss()
                    }
                }).deleteOrderData(order.orderId, order.outlet!!.outletId)
            val newIntent = Intent(
                this@CreateOrderSupplierListActivity,
                ProductListNewOrderActivity::class.java
            )
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplier.supplier.supplierId)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplier.supplier.supplierName)
            val supplierDetailsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplier)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailsJson)
            newIntent.putExtra(
                ZeemartAppConstants.OUTLET_ID,
                SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
            )
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_CREATE_NEW_ORDER
            )
            startActivity(newIntent)
        }
        builder.setNeutralButton(getString(R.string.dialog_cancel_button_text)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(getString(R.string.txt_have_existing_draft))
        dialog.setMessage(getString(R.string.txt_use_or_discard_draft))
        dialog.show()
    }

    fun generateJsonRequestBodyWithUpdatedStatus(status: String?, order: Orders?): String {
        var requestJson = ""
        if (order != null) {
            order.orderStatus = status
            requestJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order)
        }
        return requestJson
    }

    fun moveToProductListActivity(supplier: DetailSupplierDataModel) {
        //call the retrieve order API to get the list of draft orders
        val supplierDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
            ZeemartBuyerApp.gsonExposeExclusive.toJson(supplier.supplier), Supplier::class.java
        )
        AnalyticsHelper.logAction(
            this@CreateOrderSupplierListActivity,
            AnalyticsHelper.TAP_ITEM_CREATE_ORDER_SUPPLIER,
            outlet!!,
            supplierDetails
        )
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        returnDraftOrderForSupplier(
            this,
            supplier.supplier.supplierId!!,
            object : DraftOrderResponseListener {
                override fun onSuccessResponse(order: Orders?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    val newIntent = Intent(
                        this@CreateOrderSupplierListActivity,
                        ProductListNewOrderActivity::class.java
                    )
                    newIntent.putExtra(
                        ZeemartAppConstants.SUPPLIER_ID,
                        supplier.supplier.supplierId
                    )
                    newIntent.putExtra(
                        ZeemartAppConstants.SUPPLIER_NAME,
                        supplier.supplier.supplierName
                    )
                    newIntent.putExtra(
                        ZeemartAppConstants.OUTLET_ID,
                        SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
                    )
                    val supplierDetailsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplier)
                    newIntent.putExtra(
                        ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                        supplierDetailsJson
                    )
                    if (order != null) {
                        val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.products)
                        newIntent.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, orderJson)
                        newIntent.putExtra(ZeemartAppConstants.EXISTING_DRAFT_ID, order.orderId)
                    }
                    newIntent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        ZeemartAppConstants.CALLED_FROM_CREATE_NEW_ORDER
                    )
                    startActivity(newIntent)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }
}