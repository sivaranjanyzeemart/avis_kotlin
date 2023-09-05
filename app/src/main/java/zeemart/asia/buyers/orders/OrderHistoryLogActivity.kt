package zeemart.asia.buyers.orders

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.OrderActivityLogAdapter
import zeemart.asia.buyers.adapter.ReceiptStatusListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.OrderStatus.Companion.SetStatusBackground
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimport.OrderHistoryModelBaseResponse
import zeemart.asia.buyers.models.orderimportimport.OrderHistoryModelBaseResponse.OrderHistoryModel
import zeemart.asia.buyers.network.GetOrderHistoryLog
import zeemart.asia.buyers.network.GetOrderHistoryLog.GetOrderHistoryDataListener
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 9/4/2018.
 */
class OrderHistoryLogActivity : AppCompatActivity() {
    lateinit var btnArrowBack: ImageButton
    lateinit var lstOrderHistory: RecyclerView
    lateinit var orderId: String
    lateinit var outletId: String
    lateinit var txtHeading: TextView
    lateinit var txtNoOrderHistory: TextView
    lateinit var lstReceiptStatus: RecyclerView
    lateinit var order: Orders
    lateinit var txtOrderStatus: TextView
    lateinit var txtOrderStatusTime: TextView
    lateinit var lblOrderNotificationSentTo: TextView
    lateinit var txtHeaderActivityHistory: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history_log)
        btnArrowBack = findViewById(R.id.imageButton_moveBack)
        txtHeaderActivityHistory = findViewById(R.id.txt_header_activity_history)
        lstOrderHistory = findViewById(R.id.lst_order_history)
        txtHeading = findViewById(R.id.txt_activity_log_heading)
        txtNoOrderHistory = findViewById(R.id.txt_no_activity_history)
        lstReceiptStatus = findViewById(R.id.lst_receipt_status)
        txtOrderStatus = findViewById(R.id.txt_order_status)
        txtOrderStatusTime = findViewById(R.id.txt_order_status_time)
        lblOrderNotificationSentTo = findViewById(R.id.lbl_order_notification_sent_to)
        btnArrowBack.setOnClickListener(View.OnClickListener { finish() })
        lstOrderHistory.setLayoutManager(LinearLayoutManager(this))
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_ID)) {
                orderId = bundle.getString(ZeemartAppConstants.ORDER_ID).toString()
            }
            if (bundle.containsKey(ZeemartAppConstants.OUTLET_ID)) {
                outletId = bundle.getString(ZeemartAppConstants.OUTLET_ID).toString()
            }
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                order = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }

        //call the API to fetch the order History
        val getOrderHistoryLog = GetOrderHistoryLog(this, object : GetOrderHistoryDataListener {
            override fun onSuccessResponse(orderHistoryData: List<OrderHistoryModel?>?) {
                if (orderHistoryData!!.size > 0) {
                    Log.d("ORDER HISTORY DATA", orderHistoryData[0]!!.orderId + "***")
                    //set the adapter for history data
                    val orderHistoryUpdateList: MutableList<OrderHistoryModel?> = ArrayList()
                    orderHistoryUpdateList.addAll(0, orderHistoryData)
                    if (orderHistoryUpdateList.isEmpty()) {
                        lstOrderHistory.setVisibility(View.GONE)
                        txtHeading.setVisibility(View.GONE)
                        txtNoOrderHistory.setVisibility(View.VISIBLE)
                    } else {
                        lstOrderHistory.setVisibility(View.VISIBLE)
                        txtHeading.setVisibility(View.VISIBLE)
                        txtNoOrderHistory.setVisibility(View.GONE)
                        lstOrderHistory.setAdapter(
                            OrderActivityLogAdapter(
                                this@OrderHistoryLogActivity,
                                order!!.outlet!!.timeZoneFromOutlet!!,
                                orderHistoryUpdateList as List<OrderHistoryModelBaseResponse.OrderHistoryModel>
                            )
                        )
                    }
                } else {
                    lstOrderHistory.setVisibility(View.GONE)
                    txtHeading.setVisibility(View.GONE)
                    txtNoOrderHistory.setVisibility(View.VISIBLE)
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {}
        })
        getOrderHistoryLog.getOrderHistoryLogData(arrayOf(orderId), outletId!!)
        lstReceiptStatus.setLayoutManager(LinearLayoutManager(this))
        //        val getReceiptStatus = GetOrderHistoryLog(this,this)
//        getOrderHistoryLog.getOrderHistoryLogData(arrayOf(orderId),outletId)
        val rslAdapter = ReceiptStatusListAdapter(this, order!!.allStatusDetail)
        lstReceiptStatus.setAdapter(rslAdapter)
        lstReceiptStatus.setNestedScrollingEnabled(false)
        lstOrderHistory.setNestedScrollingEnabled(false)

        //txtOrderStatus.setText(order.orderStatusResId)
        txtOrderStatusTime.setText(
            String.format(
                getString(R.string.format_updated_on), DateHelper.getDateInDateMonthYearTimeFormat(
                    order!!.timeUpdated, order!!.outlet!!.timeZoneFromOutlet
                )
            )
        )
        if (order!!.allStatusDetail.size == 0) lblOrderNotificationSentTo.setVisibility(View.GONE) else lblOrderNotificationSentTo.setVisibility(
            View.VISIBLE
        )
        setFont()
        SetStatusBackground(this, order!!, txtOrderStatus, OrderStatus.TextFormat.SentenceCap)
    }

    private fun setFont() {
        setTypefaceView(txtHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOrderStatus, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOrderStatusTime, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            lblOrderNotificationSentTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(
            txtHeaderActivityHistory,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }
}