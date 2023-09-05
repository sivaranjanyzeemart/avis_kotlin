package zeemart.asia.buyers.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.interfaces.NotifyDataChangedListener
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.inventory.AdjustmentReasons
import zeemart.asia.buyers.models.inventory.ListWithStickyHeaderUIOnHand
import zeemart.asia.buyers.models.inventory.OnHandHistoryActivity

class ViewStockMovementsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>,
    HeaderItemDecoration.StickyHeaderInterface {
    private var context: Context
    private var onHandHistoryActivities: List<OnHandHistoryActivity>? = null
    private var viewOrderSectionOrRowData: ArrayList<ListWithStickyHeaderUIOnHand>? = null
    private var listener: NotifyDataChangedListener? = null

    constructor(
        context: Context,
        viewOrderSectionOrRowData: ArrayList<ListWithStickyHeaderUIOnHand>?,
        listener: NotifyDataChangedListener?
    ) {
        this.context = context
        this.viewOrderSectionOrRowData = viewOrderSectionOrRowData
        this.listener = listener
    }

    constructor(context: Context, onHandHistoryActivityList: List<OnHandHistoryActivity>?) {
        this.context = context
        onHandHistoryActivities = onHandHistoryActivityList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_HEADER) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lst_order_list_header, parent, false)
            ViewHeader(v)
        } else if (viewType == ITEM_TYPE_LOADER) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lyt_loader_for_more_items, parent, false)
            ViewHolderLoader(v)
        } else {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_view_stock_row_item, parent, false)
            ViewHolder(v)
        }
    }

    @SuppressLint("ResourceAsColor", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: ListWithStickyHeaderUIOnHand = viewOrderSectionOrRowData!![position]
        if (item.isHeader) {
            val header = holder as ViewHeader
            val headerDayData = "(" + item.headerData?.day?.trim { it <= ' ' } + ")"
            header.txtDate.setText(item.headerData?.date?.trim { it <= ' ' })
            //            Double dollarTotalPrice = ZeemartBuyerApp.centsToDollar(item.getHeaderData().getOrderTotal());
//            String orderTotal = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(dollarTotalPrice, 2));
        } else if (item.isLoader) {
            val viewHolderLoader = holder as ViewOrderListAdapter.ViewHolderLoader
            /*set the Animation in onBindViewHolder , because if we use notifyDataSetChanged()/add/removeIt any items , it will cancel all animation at any time*/
            CustomLoadingViewBlue.setAnimationForThreeDots(
                context,
                viewHolderLoader.imgDot1,
                viewHolderLoader.imgDot2,
                viewHolderLoader.imgDot3
            )
        } else {
            //Log.d("===>", "onBindViewHolder: " + onHandHistoryActivities.get(position).toString());
            val itemHolder = holder as ViewHolder
            if (item.onHandHistoryActivity?.received != null) {
                itemHolder.txtActivityType.setText("Received")
                itemHolder.txtCountValue.setText(
                    String.format(
                        "+%s",
                        ZeemartBuyerApp.getDoubleToString(item.onHandHistoryActivity!!.received!!.quantity)
                    )
                )
                itemHolder.txtCountValue.setTextColor(context.resources.getColor(R.color.green))
                itemHolder.imageIcon.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_right_green))
            } else if (item.onHandHistoryActivity?.adjustment != null) {
                AdjustmentReasons.getReasonResId(item.onHandHistoryActivity!!.adjustment!!.stockageReason!!)
                    ?.let {
                        itemHolder.txtActivityType.setText(
                            it
                        )
                    }
                itemHolder.txtCountValue.setText(
                    String.format(
                        "%s",
                        ZeemartBuyerApp.getDoubleToString(item.onHandHistoryActivity!!.adjustment!!.quantity)
                    )
                )
                if (item.onHandHistoryActivity!!.adjustment!!.selectedOutlet != null
                    && item.onHandHistoryActivity!!.adjustment!!.selectedOutlet?.outletName != null
                ) {
                    itemHolder.txt_invoice_num.setText(item.onHandHistoryActivity!!.adjustment!!.selectedOutlet?.outletName)
                    if (item.onHandHistoryActivity!!.adjustment!!.selectedOutlet?.outletName == null) {
                        itemHolder.txt_invoice_num.setVisibility(View.GONE)
                    } else {
                        itemHolder.txt_invoice_num.setVisibility(View.VISIBLE)
                    }
                }
                if (AdjustmentReasons.isNegativeAdjustmentReason(item.onHandHistoryActivity!!.adjustment!!.stockageReason!!)) {
                    itemHolder.txtCountValue.setTextColor(context.resources.getColor(R.color.pinky_red))
                    itemHolder.imageIcon.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_left_red))
                } else {
                    itemHolder.txtCountValue.setTextColor(context.resources.getColor(R.color.green))
                    itemHolder.imageIcon.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_right_green))
                }
            } else if (item.onHandHistoryActivity?.posSales != null) {
                itemHolder.txtActivityType.setText(R.string.txt_sales)
                itemHolder.txtCountValue.setText("-" + item.onHandHistoryActivity!!.posSales!!.posSalesQty)
                itemHolder.txt_invoice_num.setText(item.onHandHistoryActivity!!.posSales!!.invoiceNumber.toString())
                itemHolder.txtCountValue.setTextColor(context.resources.getColor(R.color.pinky_red))
                itemHolder.imageIcon.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_left_red))
                if (item.onHandHistoryActivity!!.posSales!!.invoiceNumber == null) {
                    itemHolder.txt_invoice_num.setVisibility(View.GONE)
                } else {
                    itemHolder.txt_invoice_num.setVisibility(View.VISIBLE)
                }
            }
        }
    }

    class ViewHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView

        init {
            txtDate = itemView.findViewById<TextView>(R.id.txt_list_header_date)
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    inner class ViewHolderLoader(v: View) : RecyclerView.ViewHolder(v) {
        var imgDot1: ImageView
        var imgDot2: ImageView
        var imgDot3: ImageView

        init {
            imgDot1 = v.findViewById(R.id.img_dot_1)
            imgDot2 = v.findViewById(R.id.img_dot_2)
            imgDot3 = v.findViewById(R.id.img_dot_3)
        }
    }

//    val itemCount: Int
//        get() = viewOrderSectionOrRowData!!.size

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item: ListWithStickyHeaderUIOnHand = viewOrderSectionOrRowData!![position]
        return if (item.isHeader) {
            ITEM_TYPE_HEADER
        } else if (item.isLoader) {
            ITEM_TYPE_LOADER
        } else {
            ITEM_TYPE_ROW
        }
    }

    override fun getItemCount(): Int {
        return viewOrderSectionOrRowData!!.size
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var itemPosition = itemPosition
        var headerPosition = 0
        do {
            if (isHeader(itemPosition)) {
                headerPosition = itemPosition
                break
            }
            itemPosition -= 1
        } while (itemPosition >= 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return R.layout.lst_order_list_header
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        val txtHeaderDate: TextView =
            header?.findViewById<View>(R.id.txt_list_header_date) as TextView
        ZeemartBuyerApp.setTypefaceView(
            txtHeaderDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtHeaderDay: TextView = header.findViewById<View>(R.id.txt_list_header_day) as TextView
        ZeemartBuyerApp.setTypefaceView(
            txtHeaderDay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtHeaderDate.setText(viewOrderSectionOrRowData!![headerPosition].headerData?.date)
        val txtDay =
            "(" + viewOrderSectionOrRowData!![headerPosition].headerData?.day?.trim { it <= ' ' } + ")"
        txtHeaderDay.setText(txtDay.trim { it <= ' ' })

//        Double dollarTotalPrice = ZeemartBuyerApp.centsToDollar(viewOrderSectionOrRowData.get(headerPosition).getHeaderData().getOrderTotal());
//        String orderTotal = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(dollarTotalPrice, 2));
        val txtOrdersTotal: TextView =
            header.findViewById<View>(R.id.txt_list_orders_total) as TextView
        txtOrdersTotal.setText(PriceDetails(viewOrderSectionOrRowData!![headerPosition].headerData?.orderTotal).displayValue)
        ZeemartBuyerApp.setTypefaceView(
            txtOrdersTotal,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return if (itemPosition < viewOrderSectionOrRowData!!.size) {
            viewOrderSectionOrRowData!![itemPosition].isHeader
        } else {
            false
        }
    }

    override fun onHeaderClicked(header: View?, position: Int) {
        //do nothing
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtActivityType: TextView
        val txtCountValue: TextView
        val txt_invoice_num: TextView
        val imageIcon: ImageView

        init {
            txtActivityType = itemView.findViewById<TextView>(R.id.txt_activity_type)
            txtCountValue = itemView.findViewById<TextView>(R.id.txt_stock_count_value_view_stock)
            txt_invoice_num = itemView.findViewById<TextView>(R.id.txt_invoice_num)
            imageIcon = itemView.findViewById(R.id.icon_image)
            ZeemartBuyerApp.setTypefaceView(
                txtCountValue,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txt_invoice_num,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtActivityType,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    companion object {
        private const val ITEM_TYPE_HEADER = 0
        private const val ITEM_TYPE_ROW = 1
        private const val ITEM_TYPE_LOADER = 2
    }
}