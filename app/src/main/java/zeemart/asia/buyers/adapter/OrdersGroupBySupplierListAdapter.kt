package zeemart.asia.buyers.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.OrdersGroupBySupplierListAdapter.ViewHolderOrderHorizontalList
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.OrderHorizontalListOrderChangeListener
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.order.Orders

/**
 * Created by ParulBhandari on 12/2/2017.
 */
class OrdersGroupBySupplierListAdapter(
    private val context: Context,
    private val listener: OrderHorizontalListOrderChangeListener
) : RecyclerView.Adapter<ViewHolderOrderHorizontalList>() {
    private var lstPastOrders: ArrayList<Orders?>? = null
    var horizontalListId = -1
    fun setLstPastOrders(lstPastOrders: ArrayList<Orders?>) {
        this.lstPastOrders = lstPastOrders
        Log.d("TEst", lstPastOrders.toString() + "" + horizontalListId)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHorizontalList {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lst_orders_toa_supplier, parent, false)
        return ViewHolderOrderHorizontalList(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderOrderHorizontalList, position: Int) {

        val currentPosition = holder.absoluteAdapterPosition

        holder.lytOrderDetail.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (lstPastOrders!![currentPosition]!!.isOrderSelected) {
                    lstPastOrders!![currentPosition]!!.isOrderSelected = false
                    holder.imgOrderSelected.visibility = View.INVISIBLE
                    listener.onOrderDeselected(horizontalListId)
                } else {
                    lstPastOrders!![currentPosition]!!.isOrderSelected = true
                    holder.imgOrderSelected.visibility = View.VISIBLE
                    listener.onOrderSelected(horizontalListId)
                }
            }
        })
        if (lstPastOrders!![currentPosition]!!.isOrderSelected) {
            holder.imgOrderSelected.visibility = View.VISIBLE
        } else {
            holder.imgOrderSelected.visibility = View.INVISIBLE
        }
        holder.imgOrderInfo.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_REPEAT_ORDER_ORDER_DETAILS)
                listener.showOrderDetailFragment(lstPastOrders, currentPosition, horizontalListId)
            }
        })

        //String[] dateDayTime = CommonMethods.splitDateTimeFormatRepeatOrders(lstPastOrders.get(position).getTimeCreated());
        holder.txtDay.text = DateHelper.getDateInFullDayFormat(
            lstPastOrders!![currentPosition]!!.timeUpdated,
            SharedPref.defaultOutlet?.timeZoneFromOutlet
        )
        holder.txtDate.text = DateHelper.getDateInDateMonthFormat(
            lstPastOrders!![currentPosition]!!.timeDelivered,
            SharedPref.defaultOutlet?.timeZoneFromOutlet
        )
        holder.txtItems.text =
            lstPastOrders!![currentPosition]!!.products!!.size.toString() + " " + context.getString(
                R.string.txt_items
            )
        val status = lstPastOrders!![currentPosition]!!.orderStatus
        if (status == OrderStatus.CREATED.statusName) {
            holder.imgOrderStatus.visibility = View.VISIBLE
            holder.imgOrderStatus.setImageResource(R.drawable.yellow)
        } else if (status == OrderStatus.REJECTED.statusName || status == OrderStatus.CANCELLED.statusName) {
            holder.imgOrderStatus.visibility = View.VISIBLE
            holder.imgOrderStatus.setImageResource(R.drawable.red)
        } else if (status == OrderStatus.CREATING.statusName || status == OrderStatus.APPROVING.statusName) {
            holder.imgOrderStatus.visibility = View.VISIBLE
            holder.imgOrderStatus.setImageResource(R.drawable.grey)
        } else {
            holder.imgOrderStatus.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        Log.d("TEst", lstPastOrders.toString() + "***********" + horizontalListId)
        return if (lstPastOrders == null) {
            0
        } else {
            lstPastOrders!!.size
        }
    }

    inner class ViewHolderOrderHorizontalList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDay: TextView
        var txtDate: TextView
        var txtItems: TextView
        var imgOrderInfo: ImageButton
        var imgOrderSelected: ImageView
        var imgOrderStatus: ImageView
        var lytOrderDetail: RelativeLayout
        var progressBarViewOrderDetails: ProgressBar

        init {
            txtDay = itemView.findViewById(R.id.txt_day)
            txtDate = itemView.findViewById(R.id.txt_date)
            txtItems = itemView.findViewById(R.id.txt_items)
            imgOrderInfo = itemView.findViewById(R.id.img_order_info)
            imgOrderStatus = itemView.findViewById(R.id.image_order_status)
            imgOrderSelected = itemView.findViewById(R.id.img_select_order)
            lytOrderDetail = itemView.findViewById(R.id.lyt_order_detail)
            progressBarViewOrderDetails = itemView.findViewById(R.id.progressBarViewOrderDetails)
            setTypefaceView(txtDay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtItems, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    fun setSelectionTrue(position: Int, horizontalRowId: Int, isSelected: Boolean) {
        if (isSelected) {
            lstPastOrders!![position]!!.isOrderSelected = true
            //make the order in main list also as selected
            listener.onOrderSelected(horizontalRowId)
        } else {
            lstPastOrders!![position]!!.isOrderSelected = false
            listener.onOrderDeselected(horizontalRowId)
        }
        notifyDataSetChanged()
    }
}