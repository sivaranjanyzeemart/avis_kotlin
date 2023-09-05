package zeemart.asia.buyers.orders.createorders

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.RepeatOrderListAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.interfaces.UpdateRepeatOrderActivityListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.RepeatOrderWithPagination
import zeemart.asia.buyers.network.DeleteOrder
import zeemart.asia.buyers.network.MarketListApi
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.createordersimport.OrderDetailDialogFragment
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity


class RepeatOrderActivity : AppCompatActivity(), UpdateRepeatOrderActivityListener {
    private lateinit var lstRepeatOrders: RecyclerView
    private var txtDeliverTo: TextView? = null
    private lateinit var txtOutletName: TextView
    private var imgArrow: ImageView? = null
    private var btnDeselect: Button? = null
    private lateinit var btnReviewOrder: Button

    //private SwipeRefreshLayout lytSwipeRefreshLayoutOrders;
    private var selectedOrderList: MutableMap<String, Orders>? = null
    private lateinit var ordersReceivedBySupplier: List<RepeatOrderWithPagination.RepeatOrders>
    private val detailedSupplierMap: MutableMap<String, DetailSupplierDataModel> =
        HashMap<String, DetailSupplierDataModel>()
    private lateinit var progressBarRepeatOrder: ProgressBar
    private var btnNoPastOrders: Button? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repeat_order)
        lstRepeatOrders = findViewById<RecyclerView>(R.id.lst_orders)
        txtDeliverTo = findViewById<TextView>(R.id.txt_deliverTo)
        txtOutletName = findViewById<TextView>(R.id.txt_deliver_to_outlet_name)
        imgArrow = findViewById<ImageView>(R.id.img_change_outlet)
        btnDeselect = findViewById<Button>(R.id.btnDeSelect)
        btnReviewOrder = findViewById<Button>(R.id.btnReviewOrder)
        progressBarRepeatOrder = findViewById<ProgressBar>(R.id.progressBarRepeatOrder)
        btnNoPastOrders = findViewById<Button>(R.id.btn_no_orders_repeat_order)
        btnNoPastOrders!!.visibility = View.GONE
        btnReviewOrder!!.setOnClickListener {
            val (_, value) = selectedOrderList!!.entries.iterator().next()
            value.supplier?.supplierId?.let { it1 ->
                OrderHelper.checkExistingDraftThenDiscard(
                    this@RepeatOrderActivity,
                    it1,
                    btnReviewOrder
                )
            }
            val orderList: ArrayList<Orders?> = ArrayList<Orders?>()
            for (key in selectedOrderList!!.keys) {
                orderList.add(selectedOrderList!![key])
            }
            if (selectedOrderList!!.size > 1) {
                AnalyticsHelper.logAction(
                    this@RepeatOrderActivity,
                    AnalyticsHelper.TAP_REPEAT_ORDER_REVIEW_COMBINED_ORDER
                )
            } else if (selectedOrderList!!.size == 1) {
                val orders: Orders? = orderList[0]
                orders?.outlet = SharedPref.defaultOutlet
                AnalyticsHelper.logAction(
                    this@RepeatOrderActivity,
                    AnalyticsHelper.TAP_REPEAT_ORDER_REVIEW_ORDER,
                    orders!!
                )
            }
            val newIntent = Intent(this@RepeatOrderActivity, ReviewOrderActivity::class.java)
            val orderJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList)
            newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
            val supplierJson: String =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList[0]?.supplier)
            val outlet = Outlet()
            outlet.outletName = SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, "")
            outlet.outletId = SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
            val outletJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(outlet)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
            startActivity(newIntent)
        }
        txtOutletName.setText(SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, ""))
        txtOutletName.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(this@RepeatOrderActivity, SelectOutletActivity::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_REPEAT_ORDER
            )
            startActivity(newIntent)
        })
        //lytSwipeRefreshLayoutOrders = findViewById(R.id.swipeRefreshRepeatOrder);
        //lytSwipeRefreshLayoutOrders.setRefreshing(false);
        btnReviewOrder!!.visibility = View.GONE
        btnDeselect!!.visibility = View.GONE
        lstRepeatOrders.setHasFixedSize(true)
        val mLinearLayoutManager = LinearLayoutManager(this)
        lstRepeatOrders.setLayoutManager(mLinearLayoutManager)
        imgArrow!!.setOnClickListener {
            val newIntent = Intent(this@RepeatOrderActivity, SelectOutletActivity::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_REPEAT_ORDER
            )
            startActivity(newIntent)
        }
        btnDeselect!!.setOnClickListener { refreshRepeatOrdersActivity() }
        progressBarRepeatOrder.setVisibility(View.VISIBLE)
        lstRepeatOrders.setVisibility(View.INVISIBLE)
        //new RetrieveOrders(this, this, false,null,null).getOrders();
        callRepeatOrderDataAPI()
        setRepeatOrderScreenUIInitialState()
        setFont()
    }

    protected override fun onResume() {
        super.onResume()
        progressBarRepeatOrder.setVisibility(View.VISIBLE)
        lstRepeatOrders.setVisibility(View.INVISIBLE)
        callRepeatOrderDataAPI()
        setRepeatOrderScreenUIInitialState()
    }

    private fun callRepeatOrderDataAPI() {
        OrderHelper.getRepeatedOrdersBySupplier(
            this,
            SharedPref.defaultOutlet!!,
            object : OrderHelper.RepeatOrderResponseListener {
                override fun onSuccessResponse(supplierOrdersList: RepeatOrderWithPagination?) {
                    if (supplierOrdersList != null && supplierOrdersList.data != null && supplierOrdersList.data!!
                            .size!! > 0
                    ) {
                        ordersReceivedBySupplier = supplierOrdersList.data!!
                        val apiParamsHelper = ApiParamsHelper()
                        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.ASC)
                        apiParamsHelper.setOutletId(SharedPref.defaultOutlet!!.outletId!!)
                        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
                        outlets.add(SharedPref.defaultOutlet!!)
                        MarketListApi.retrieveSupplierListNew(
                            this@RepeatOrderActivity,
                            apiParamsHelper,
                            outlets,
                            object : SupplierListResponseListener {
                                override fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?) {
                                    progressBarRepeatOrder.setVisibility(View.GONE)
                                    if (supplierList != null && supplierList.size > 0) {
                                        lstRepeatOrders.setVisibility(View.VISIBLE)
                                        btnNoPastOrders!!.visibility = View.GONE
                                        val orderBySupplierMap: LinkedHashMap<Supplier, ArrayList<Orders?>> =
                                            getGroupBySupplierMap(supplierOrdersList.data!!) as LinkedHashMap<Supplier, ArrayList<Orders?>>
                                        detailedSupplierMap.clear()
                                        for (i in supplierList.indices) {
                                            detailedSupplierMap[supplierList[i]?.supplier?.supplierId] =
                                                supplierList[i]!!
                                        }
                                        val adapter = RepeatOrderListAdapter(
                                            this@RepeatOrderActivity,
                                            this@RepeatOrderActivity,
                                            orderBySupplierMap,
                                            detailedSupplierMap as Map<String?, DetailSupplierDataModel>?
                                        )
                                        lstRepeatOrders.setAdapter(adapter)
                                    } else {
                                        lstRepeatOrders.setVisibility(View.GONE)
                                        btnNoPastOrders!!.visibility = View.VISIBLE
                                    }
                                }

                                override fun onErrorResponse(error: VolleyErrorHelper?) {
                                    progressBarRepeatOrder.setVisibility(View.GONE)
                                    //display the list of past orders if supplier for that outlet API call fails
                                    lstRepeatOrders.setVisibility(View.VISIBLE)
                                    btnNoPastOrders!!.visibility = View.GONE
                                    val adapter = RepeatOrderListAdapter(
                                        this@RepeatOrderActivity,
                                        this@RepeatOrderActivity,
                                        getGroupBySupplierMap(supplierOrdersList.data!!) as LinkedHashMap<Supplier, ArrayList<Orders?>>, null)
                                    lstRepeatOrders.setAdapter(adapter)
                                }
                            })
                    } else {
                        lstRepeatOrders.setVisibility(View.GONE)
                        btnNoPastOrders!!.visibility = View.VISIBLE
                        progressBarRepeatOrder.setVisibility(View.GONE)
                    }
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    progressBarRepeatOrder.setVisibility(View.GONE)
                    //lytSwipeRefreshLayoutOrders.setRefreshing(false);
                    Log.d("VolleyError", error?.message + "***")
                    val errorMessage: String? = error?.errorMessage
                    val errorType: String? = error?.errorType
                    if (errorType == ServiceConstant.RETRY_API_CALL__500_MESSAGE) {
                    } else if (errorType == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        ZeemartBuyerApp.getToastRed(errorMessage)
                    }
                }
            })
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtDeliverTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOutletName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    /**
     * Refreshes the Repeat order activity
     */
    private fun refreshRepeatOrdersActivity() {
        for (i in ordersReceivedBySupplier!!.indices) {
            for (j in 0 until ordersReceivedBySupplier!![i].orders?.size!!) {
                ordersReceivedBySupplier!![i].orders?.get(j)?.isOrderSelected
            }
        }
        val adapter = RepeatOrderListAdapter(
            this@RepeatOrderActivity,
            this@RepeatOrderActivity,
            getGroupBySupplierMap(ordersReceivedBySupplier) as LinkedHashMap<Supplier, ArrayList<Orders?>>,
            detailedSupplierMap as Map<String?, DetailSupplierDataModel>
        )

        lstRepeatOrders.setAdapter(adapter)
        selectedOrderList!!.clear()
        setRepeatOrderScreenUIInitialState()
    }

    override fun noOfSelectedOrders(
        selectedOrder: Int,
        selectedOrders: Map<String?, Orders?>?,
    ) {
        Log.d("Selected Order Size", selectedOrders?.size.toString() + "***")
        selectedOrderList = selectedOrders as MutableMap<String, Orders>?
        if (selectedOrder == 0) {
            setRepeatOrderScreenUIInitialState()
        } else {
            btnDeselect!!.visibility = View.VISIBLE
            imgArrow!!.visibility = View.GONE
            txtDeliverTo?.setText(getString(R.string.txt_create_repeat_order))
            txtOutletName.setText(
                kotlin.String.format(
                    getString(R.string.format_past_orders_selected),
                    "" + selectedOrder
                )
            )
            txtOutletName.setClickable(false)
            btnReviewOrder!!.visibility = View.VISIBLE
            if (selectedOrder > 1) {
                btnReviewOrder.setText(getString(R.string.title_review_combined_order))
            } else {
                btnReviewOrder.setText(getString(R.string.title_review_order))
            }
        }
    }

    override fun callOrderDetailDialogFragment(
        pastOrders: ArrayList<Orders?>?,
        position: Int,
        horizontalRowId: Int,
    ) {
        val fm: FragmentManager = getSupportFragmentManager()
        val pastOrderJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(
            pastOrders,
            object : TypeToken<ArrayList<Orders?>?>() {}.type
        )
        val fragment: OrderDetailDialogFragment = OrderDetailDialogFragment.Companion.newInstance(
            pastOrderJson,
            position,
            horizontalRowId
        )
        fragment.show(fm, "display_order_detail_dialog")
    }

    /**
     * Creates a hash map or list of orders linked with the suppliers.
     *
     * @param repeatOrderBySupplierList
     * @return
     */
    fun getGroupBySupplierMap(repeatOrderBySupplierList: List<RepeatOrderWithPagination.RepeatOrders>): java.util.LinkedHashMap<Supplier?, java.util.ArrayList<Orders?>?>? {
        val listOrdersGroupBySuppliers =
            java.util.LinkedHashMap<Supplier, java.util.ArrayList<Orders>>()
        for (i in repeatOrderBySupplierList.indices) {
            listOrdersGroupBySuppliers.put(
                repeatOrderBySupplierList[i].supplier!!, java.util.ArrayList<Orders>(
                    repeatOrderBySupplierList[i].orders
                )
            )
        }
        return listOrdersGroupBySuppliers as LinkedHashMap<Supplier?, ArrayList<Orders?>?>?
    }

    private fun createExistingDraftDialog(order: Orders) {
        val builder = AlertDialog.Builder(this@RepeatOrderActivity)
        builder.setPositiveButton(
            getString(R.string.txt_use_draft),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                    val newIntent =
                        Intent(this@RepeatOrderActivity, ReviewOrderActivity::class.java)
                    val orderList: ArrayList<Orders> = ArrayList<Orders>()
                    orderList.add(order)
                    val orderJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList)
                    newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
                    val supplierJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                        orderList[0].supplier
                    )
                    val outletJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                        orderList[0].outlet
                    )
                    newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
                    newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
                    startActivity(newIntent)
                    refreshRepeatOrdersActivity()
                }
            })
        builder.setNegativeButton(
            getString(R.string.txt_discard_draft),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    //call the delete draft API
                    DeleteOrder(
                        this@RepeatOrderActivity,
                        object : DeleteOrder.GetResponseStatusListener {
                            override fun onSuccessResponse(status: String?) {
                                dialog.dismiss()
                            }

                            override fun onErrorResponse(error: VolleyError?) {
                                dialog.dismiss()
                            }
                        }).deleteOrderData(order.orderId, order.outlet?.outletId)
                }
            })
        builder.setNeutralButton(
            getString(R.string.dialog_cancel_button_text),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(getString(R.string.txt_have_existing_draft))
        dialog.setMessage(getString(R.string.txt_use_or_discard_draft))
        dialog.show()
    }

    fun updateThePastOrderListSelectionStatus(
        position: Int,
        horizontalRowId: Int,
        isOrderSelected: Boolean,
    ) {
        if (lstRepeatOrders.findViewHolderForAdapterPosition(horizontalRowId) != null) {
            (lstRepeatOrders.findViewHolderForAdapterPosition(horizontalRowId) as RepeatOrderListAdapter.ViewHolderSupplierList).adapter.setSelectionTrue(
                position,
                horizontalRowId,
                isOrderSelected
            )
            if (isOrderSelected) {
                Handler().postDelayed({
                    (lstRepeatOrders.findViewHolderForAdapterPosition(
                        horizontalRowId
                    ) as RepeatOrderListAdapter.ViewHolderSupplierList).lstGroupBySupplier.scrollToPosition(position)
                }, 200)
            }
        }
    }

    private fun setRepeatOrderScreenUIInitialState() {
        btnDeselect!!.visibility = View.GONE
        imgArrow!!.visibility = View.VISIBLE
        txtDeliverTo?.setText(getString(R.string.deliver_to))
        txtOutletName.setText(SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, ""))
        txtOutletName.setClickable(true)
        btnReviewOrder!!.visibility = View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }
}