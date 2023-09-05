package zeemart.asia.buyers.orders.createorders

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.ShowOrderItemsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.models.OrderStatus.Companion.SetStatusBackground
import zeemart.asia.buyers.models.order.Orders


/**
 * A simple [Fragment] subclass.
 */
class OrderDetailFragment : Fragment() {
    var lstOrderItems: RecyclerView? = null
    var lytSelectOrder: RelativeLayout? = null
    var txtOrderSelected: TextView? = null
    var imgOrderSelected: ImageView? = null
    var txtOrderStatus: TextView? = null
    var txtOrderNumber: TextView? = null
    var txtOrderTitle: TextView? = null
    var txtOrderCreator: TextView? = null
    private var txtTotalNumberOfItemsInOrder: TextView? = null
    private var order: Orders? = null
    private var position = 0
    private var horizontalRowId = 0
    private var isFragmentAttached = false
    override fun onAttach(context: Context) {
        isFragmentAttached = true
        super.onAttach(context)
    }

    override fun onDestroy() {
        isFragmentAttached = false
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.layout_order_detail_data, container, false)
        lstOrderItems = v.findViewById(R.id.lst_Order_Items)
        lytSelectOrder = v.findViewById(R.id.lytSelectOrder)
        txtOrderSelected = v.findViewById(R.id.txt_order_selected)
        imgOrderSelected = v.findViewById(R.id.img_slected)
        txtOrderStatus = v.findViewById(R.id.txt_order_status)
        txtOrderNumber = v.findViewById(R.id.txt_order_number)
        txtOrderTitle = v.findViewById(R.id.txt_order_detail_title)
        txtOrderCreator = v.findViewById(R.id.txt_createdby_orderDetail_repeatOrder)
        txtTotalNumberOfItemsInOrder = v.findViewById(R.id.txt_no_of_items_in_order)
        setUIFont()
        val bundle = arguments
        val orderJson = bundle!!.getString("ORDER_DETAIL_DATA")
        position = bundle.getInt("SELECTED_ORDER_POSITION")
        horizontalRowId = bundle.getInt("SELECTED_ROW_ID")
        order = ZeemartBuyerApp.gsonExposeExclusive.fromJson(orderJson, Orders::class.java)
        setUIForOrder()
        return v
    }

    private fun setUIForOrder() {
        if (isFragmentAttached) {
            lstOrderItems!!.layoutManager = LinearLayoutManager(activity)
            lstOrderItems!!.adapter = ShowOrderItemsAdapter(order!!.products!!)
            txtOrderNumber!!.text = "#" + order!!.orderId
            //            setStatusBackground(detailedOrder.getOrderStatus());
//            setStatusBackground(detailedOrder.getOrderStatus());
            SetStatusBackground(requireActivity(), order!!, txtOrderStatus!!)
            try {
                txtOrderTitle!!.text = DateHelper.getDateInDateMonthFormat(
                    order!!.timeUpdated,
                    order!!.outlet!!.timeZoneFromOutlet
                )
            } catch (e: Exception) {
                txtOrderTitle!!.text =
                    DateHelper.getDateInDateMonthFormat(order!!.timeUpdated, null)
                e.printStackTrace()
            }
            txtTotalNumberOfItemsInOrder!!.text =
                order!!.products!!.size.toString() + getString(R.string.txt_items)
            imgOrderSelected!!.visibility = View.GONE
            txtOrderSelected!!.setTextColor(requireActivity().resources.getColor(R.color.text_blue))
            txtOrderCreator!!.text =
                order!!.createdBy!!.firstName + " " + order!!.createdBy!!.lastName
            if (order!!.isOrderSelected) {
                txtOrderSelected!!.setText(R.string.txt_selected)
                lytSelectOrder!!.setBackgroundResource(R.drawable.blue_rounded_corner)
                imgOrderSelected!!.visibility = View.VISIBLE
                txtOrderSelected!!.setTextColor(requireActivity().resources.getColor(R.color.white))
            } else {
                txtOrderSelected!!.setText(R.string.txt_select_this_order)
                lytSelectOrder!!.setBackgroundResource(R.drawable.grey_rounded_corner)
                imgOrderSelected!!.visibility = View.GONE
                txtOrderSelected!!.setTextColor(requireActivity().resources.getColor(R.color.text_blue))
            }
            lytSelectOrder!!.setOnClickListener {
                if (imgOrderSelected!!.visibility == View.GONE) {
                    AnalyticsHelper.logAction(
                        activity,
                        AnalyticsHelper.TAP_REPEAT_ORDER_DETAIL_SELECT_ORDER
                    )
                    lytSelectOrder!!.setBackgroundResource(R.drawable.blue_rounded_corner)
                    imgOrderSelected!!.visibility = View.VISIBLE
                    txtOrderSelected!!.setTextColor(requireActivity().resources.getColor(R.color.white))
                    txtOrderSelected!!.setText(R.string.txt_selected)
                    (activity as RepeatOrderActivity?)!!.updateThePastOrderListSelectionStatus(
                        position,
                        horizontalRowId,
                        true
                    )
                } else if (imgOrderSelected!!.visibility == View.VISIBLE) {
                    lytSelectOrder!!.setBackgroundResource(R.drawable.grey_rounded_corner)
                    imgOrderSelected!!.visibility = View.GONE
                    txtOrderSelected!!.setTextColor(requireActivity().resources.getColor(R.color.text_blue))
                    txtOrderSelected!!.setText(R.string.txt_select_this_order)
                    (activity as RepeatOrderActivity?)!!.updateThePastOrderListSelectionStatus(
                        position,
                        horizontalRowId,
                        false
                    )
                }
            }
        }
    }

    private fun setUIFont() {
        setTypefaceView(txtOrderTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOrderCreator, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtOrderNumber, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtOrderStatus, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtOrderSelected, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    } //    private void setStatusBackground(String status)

    //    {
    //        if(status.equals(OrderStatus.CREATED.getStatusName()))
    //        {
    //            txtOrderStatus.setText(OrderStatus.CREATED.getStatusResId());
    //            txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_yellow);
    //        }
    //        else if(status.equals(OrderStatus.PLACED.getStatusName()))
    //        {
    //            txtOrderStatus.setText(OrderStatus.PLACED.getStatusResId());
    //            txtOrderStatus.setBackgroundResource(R.drawable.green_solid_round_corner);
    //        }
    //        else if(status.equals(OrderStatus.REJECTED.getStatusName()))
    //        {
    //            txtOrderStatus.setText(OrderStatus.REJECTED.getStatusResId());
    //            txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink);
    //        }
    //        else if(status.equals(OrderStatus.CANCELLED.getStatusName()))
    //        {
    //            txtOrderStatus.setText(OrderStatus.CANCELLED.getStatusResId());
    //            txtOrderStatus.setBackgroundResource(R.drawable.btn_rounded_pink);
    //        }
    //        else if(status.equals(OrderStatus.CREATING.getStatusName()))
    //        {
    //            txtOrderStatus.setText(OrderStatus.CREATING.getStatusResId());
    //            txtOrderStatus.setBackgroundResource(R.drawable.grey_solid_round_corner);
    //        }
    //        else
    //        {
    //            txtOrderStatus.setText(OrderStatus.UNAVAILABLE.getStatusResId());
    //            txtOrderStatus.setBackgroundResource(R.drawable.grey_solid_round_corner);
    //        }
    //
    //    }
    companion object {
        @JvmStatic
        fun newInstance(
            ordersData: String?,
            position: Int,
            horizontalRowId: Int
        ): OrderDetailFragment {
            val fragment = OrderDetailFragment()
            val bundle = Bundle()
            bundle.putString("ORDER_DETAIL_DATA", ordersData)
            bundle.putInt("SELECTED_ORDER_POSITION", position)
            bundle.putInt("SELECTED_ROW_ID", horizontalRowId)
            fragment.arguments = bundle
            return fragment
        }
    }
}