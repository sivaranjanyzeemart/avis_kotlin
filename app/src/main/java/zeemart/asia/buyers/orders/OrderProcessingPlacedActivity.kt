package zeemart.asia.buyers.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.OrderStatus.Companion.SetStatusBackground
import zeemart.asia.buyers.models.orderimport.RetrieveOrderDetails
import zeemart.asia.buyers.network.GetOrderDetails
import zeemart.asia.buyers.network.GetOrderDetails.GetOrderDetailedDataListener

class OrderProcessingPlacedActivity : AppCompatActivity() {
    private lateinit var txtOrderId: TextView
    private lateinit var txtDeliverTo: TextView
    private var txtDeliverToTag: TextView? = null
    private lateinit var txtDeliverOn: TextView
    private var txtDeliverOnTag: TextView? = null
    private lateinit var txtCreatedBy: TextView
    private lateinit var txtStatus: TextView
    private var txtStatusTag: TextView? = null
    private lateinit var txtSupplierName: TextView
    private var txtSupplierTag: TextView? = null
    private var orderId: String? = null
    private var outletId: String? = null
    private lateinit var btnDone: Button
    private var calledFrom: String? = null
    private lateinit var txtOrderProcessingTitle: TextView
    private var txtProcessingOrder: TextView? = null
    private var txtUpdateOrder: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_processing)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ORDER_ID)) {
                orderId = bundle.getString(ZeemartAppConstants.ORDER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID)) {
                outletId = bundle.getString(ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        txtStatus = findViewById(R.id.orderProcessing_txt_status_value)
        txtStatusTag = findViewById(R.id.orderProcessing_txt_status_tag)
        txtOrderId = findViewById(R.id.orderProcessing_txt_orderno_date)
        txtDeliverTo = findViewById(R.id.orderProcessing_txt_location_value)
        txtDeliverToTag = findViewById(R.id.orderProcessing_txt_location_tag)
        txtDeliverOn = findViewById(R.id.orderProcessing_txt_calendar_value)
        txtDeliverOnTag = findViewById(R.id.orderProcessing_txt_calendar_tag)
        txtSupplierName = findViewById(R.id.orderProcessing_txt_supplier_value)
        txtSupplierTag = findViewById(R.id.orderProcessing_txt_supplier_tag)
        txtCreatedBy = findViewById(R.id.orderProcessing_txt_created_by_value)
        txtOrderProcessingTitle = findViewById(R.id.orderProcessing_title)
        txtUpdateOrder = findViewById(R.id.txt_update_order)
        txtProcessingOrder = findViewById(R.id.txt_processing_order)
        setFont()
        if (calledFrom == ZeemartAppConstants.CALLED_FROM_CREATE_NEW_ORDER) {
            txtStatus.setText(getString(R.string.txt_processing))
            txtStatus.setBackgroundResource(R.drawable.grey_rounded_corner)
            txtStatus.setTextColor(resources.getColor(R.color.black))
            txtOrderProcessingTitle.setText(R.string.txt_youve_created_new_order)
        }

        //call the orderDetail API
        GetOrderDetails(this, object : GetOrderDetailedDataListener {
            override fun onSuccessResponse(orders: RetrieveOrderDetails?) {
                if (orders != null && orders.data != null) {
                    //String orderIdDateCreated = "#"+orders.get(0).getOrderId()+" . "+ DateHelper.getDateInDateMonthYearFormat(orders.get(0).timeUpdated, orders.get(0).getOutlet().getTimeZoneFromOutlet());
                    val orderIdDateCreated = "#" + orders.data!!.orderId
                    txtOrderId.setText(orderIdDateCreated)
                    txtDeliverTo.setText(orders.data!!.outlet?.outletName)
                    txtSupplierName.setText(orders.data!!.supplier?.supplierName)
                    val dateTime = DateHelper.getDateInFullDayDateMonthYearFormat(
                        orders.data!!.timeDelivered!!,
                        orders.data!!.outlet?.timeZoneFromOutlet
                    )
                    txtDeliverOn.setText(dateTime)
                    txtCreatedBy.setText(
                        orders.data!!.createdBy?.firstName + " " + orders.data!!
                            .createdBy?.lastName
                    )
                    if (calledFrom == ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS || calledFrom == ZeemartAppConstants.APPROVER_EDIT_ORDER) {
                        txtOrderProcessingTitle.setText(R.string.txt_your_response_has_been_sent)
                        SetStatusBackground(
                            this@OrderProcessingPlacedActivity,
                            orders.data!!,
                            txtStatus
                        )
                    }
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                if (error!!.networkResponse != null && error.message != null) {
                    if (error.networkResponse.statusCode.toString() == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                        getToastRed(error.message)
                    }
                }
            }
        }).getOrderDetailsData(orderId, outletId!!)
        btnDone = findViewById(R.id.orderProcessing_btn_done)
        btnDone.setOnClickListener(View.OnClickListener { setActionOnBackPressed() })
        if (SharedPref.readInt(
                SharedPref.TOTAL_SUCCESSFUL_PLACED_ORDERS,
                0
            )!! > 20
        ) CommonMethods.askAppReview(this@OrderProcessingPlacedActivity)
    }

    private fun setFont() {
        setTypefaceView(
            txtOrderProcessingTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtStatus, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtDeliverTo, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtDeliverOn, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOrderId, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtStatusTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtSupplierTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDeliverToTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDeliverOnTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtProcessingOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtUpdateOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnDone, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onBackPressed() {
        setActionOnBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_right)
    }

    private fun setActionOnBackPressed() {
        finish()
        val newIntent =
            Intent(this@OrderProcessingPlacedActivity, BaseNavigationActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(newIntent)
    }
}