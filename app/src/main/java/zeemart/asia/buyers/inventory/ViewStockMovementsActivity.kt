package zeemart.asia.buyers.inventory

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.ViewStockMovementsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.interfaces.NotifyDataChangedListener
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.ListWithStickyHeaderUIOnHand
import zeemart.asia.buyers.models.inventory.OnHandHistoryActivity
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI
import zeemart.asia.buyers.network.OutletInventoryApi
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.util.*

class ViewStockMovementsActivity : AppCompatActivity() {
    private val lstViewOrderWithHeaders: ArrayList<ListWithStickyHeaderUIOnHand> =
        ArrayList<ListWithStickyHeaderUIOnHand>()
    private var stockCountText: TextView? = null
    private var stockCountValueText: TextView? = null
    private var salesCountText: TextView? = null
    private var salesCountValueText: TextView? = null
    private var titleText: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var lytSalesValues: RelativeLayout? = null
    private val productId: String? = null
    private var product: StockCountEntries.StockInventoryProduct? = null
    private var onHandHistoryActivities: List<OnHandHistoryActivity?>? = null
    private var posIntegration = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_stock_movements)
        stockCountText = findViewById<TextView>(R.id.txt_stock_count_view_stock)
        stockCountValueText = findViewById<TextView>(R.id.txt_stock_count_value_view_stock)
        salesCountText = findViewById<TextView>(R.id.txt_sales_count_view_stock)
        lytSalesValues = findViewById<RelativeLayout>(R.id.lyt_sales_value_view_stock)
        salesCountValueText = findViewById<TextView>(R.id.txt_sales_value_view_stock)
        titleText = findViewById<TextView>(R.id.txt_view_stock_movements_title)
        recyclerView = findViewById<RecyclerView>(R.id.on_hand_activity_history_list)
        recyclerView!!.setLayoutManager(LinearLayoutManager(this))
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            val productJson: String =
                bundle.getString(InventoryDataManager.INTENT_PRODUCT_DETAIL, "")
            product = ZeemartBuyerApp.fromJson<StockCountEntries.StockInventoryProduct>(
                productJson,
                StockCountEntries.StockInventoryProduct::class.java
            )
            titleText!!.setText(product?.productName)
        }
        salesCountText!!.setText("Sales since last count")
        stockCountText!!.setText("Stock count")
        lytSalesValues!!.setVisibility(View.GONE)
        ZeemartBuyerApp!!.setTypefaceView(
            stockCountText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            salesCountText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            titleText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            salesCountValueText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            stockCountValueText,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        callApi()
    }

    private fun callApi() {
        OutletsApi.getSpecificOutlet(this, object : OutletsApi.GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                if (specificOutlet?.data?.posIntegration != null && specificOutlet.data!!.posIntegration!!.posIntegration == true) {
                    posIntegration = specificOutlet.data!!.posIntegration!!.posIntegration!!
                    lytSalesValues?.setVisibility(View.VISIBLE)
                } else {
                    posIntegration = false
                }
            }

            override fun onError(error: VolleyErrorHelper?) {}
        })
        OutletInventoryApi.getOnHandHistory(
            this,
            product?.productId,
            "",
            object : OutletInventoryApi.OnHandResponseListener {
                @SuppressLint("ResourceAsColor")
                override fun onSuccessResponse(data: InventoryResponse.OnHandHistoryData?) {
                    if (data != null && data.data != null) {
                        stockCountValueText?.setText(
                            ZeemartBuyerApp.getDoubleToString(
                                data.data!!.stockCountQty
                            )
                        )
                        salesCountValueText?.setText(
                            "-" + ZeemartBuyerApp.getDoubleToString(
                                data.data!!.posSalesQty
                            )
                        )
                        salesCountValueText?.setTextColor(getResources().getColor(R.color.pinky_red))
                        onHandHistoryActivities = data.data!!.onHandStockActivity
                        Log.d("===>", "onSuccessResponse: " + onHandHistoryActivities!!.size)
                        //recyclerView.setAdapter(new ViewStockMovementsAdapter(ViewStockMovementsActivity.this,onHandHistoryActivities));
                        setOrdersAdapter(onHandHistoryActivities)
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {}
            })
    }

    fun setOrdersAdapter(onHandHistoryActivityArrayList: List<OnHandHistoryActivity?>?) {
        if (onHandHistoryActivityArrayList != null && onHandHistoryActivityArrayList.size > 0) {
            val orders: ArrayList<OnHandHistoryActivity> =
                ArrayList<OnHandHistoryActivity>(onHandHistoryActivityArrayList)
            if (orders != null && orders.size > 0) {
                Collections.sort(orders, object : Comparator<OnHandHistoryActivity?> {
                    override fun compare(o1: OnHandHistoryActivity?, o2: OnHandHistoryActivity?): Int {
                        return o2?.timeCreated!!.compareTo(o1?.timeCreated!!)
                    }
                })
                for (i in orders.indices) {
                    val newListItem = ListWithStickyHeaderUIOnHand()
                    val headerViewOrders = HeaderViewOrderUI()
                    val date: String =
                        DateHelper.getDateInDateMonthYearFormat(orders[i].timeCreated, null)
                    val day: String =
                        DateHelper.getDateIn3LetterDayFormat(orders[i].timeCreated, null)
                    headerViewOrders.date = date
                    headerViewOrders.day = day
                    if (lstViewOrderWithHeaders.size == 0) {
                        saveHeaderAndOrder(orders[i], newListItem, headerViewOrders)
                    } else {
                        //if the last entry of the data list has same order date as that of the current order
                        // then do not add a header element otherwise add a header element
                        if (lstViewOrderWithHeaders[lstViewOrderWithHeaders.size - 1] != null) {
                            if (lstViewOrderWithHeaders[lstViewOrderWithHeaders.size - 1]?.headerData?.date == headerViewOrders.date) {
                                newListItem.onHandHistoryActivity = orders[i]
                                newListItem.headerData = headerViewOrders
                                newListItem.isHeader = false
                                lstViewOrderWithHeaders.add(newListItem)
                            } else {
                                saveHeaderAndOrder(orders[i], newListItem, headerViewOrders)
                            }
                        } else {
                            if (lstViewOrderWithHeaders[lstViewOrderWithHeaders.size - 2]?.headerData?.date == headerViewOrders.date) {
                                newListItem.onHandHistoryActivity = orders[i]
                                newListItem.headerData = headerViewOrders
                                newListItem.isHeader = false
                                lstViewOrderWithHeaders.add(newListItem)
                            } else {
                                saveHeaderAndOrder(orders[i], newListItem, headerViewOrders)
                            }
                        }
                    }
                }
                if (lstViewOrderWithHeaders.size == 0) {
                    // noOrderLayoutVisibility(true, false);
                } else {
                    //  noOrderLayoutVisibility(false, false);
                    val mAdapter = ViewStockMovementsAdapter(
                        this,
                        lstViewOrderWithHeaders,
                        object : NotifyDataChangedListener {
                            override fun notifyResetAdapter() {
                                //callRetrieveAllOrder(false);
                            }
                        })
                    recyclerView?.setAdapter(mAdapter)
                    //                        recyclerView.addItemDecoration(new HeaderItemDecoration(recyclerView, mAdapter));
                    //new ItemTouchHelper(new ListItemSwipeHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, mAdapter)).attachToRecyclerView(lstOrders);
                }
            }
        } else {
        }
    }

    private fun saveHeaderAndOrder(
        order: OnHandHistoryActivity,
        newListItem: ListWithStickyHeaderUIOnHand,
        headerViewOrders: HeaderViewOrderUI,
    ) {
        newListItem.headerData = headerViewOrders
        newListItem.isHeader = true
        lstViewOrderWithHeaders.add(newListItem)
        val orderDataEntry = ListWithStickyHeaderUIOnHand()
        orderDataEntry.isHeader = false
        orderDataEntry.headerData = headerViewOrders
        orderDataEntry.onHandHistoryActivity = order
        lstViewOrderWithHeaders.add(orderDataEntry)
    }
}