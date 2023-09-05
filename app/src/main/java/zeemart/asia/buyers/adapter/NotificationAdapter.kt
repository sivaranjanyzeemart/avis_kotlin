package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.NotificationDetailMgr
import zeemart.asia.buyers.orders.OrderDetailsActivity

/**
 * Created by ParulBhandari on 11/23/2017.
 */
class NotificationAdapter(context: Context, listNotifications: List<NotificationDetailMgr>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>(), HeaderItemDecoration.StickyHeaderInterface {
    private val inflater: LayoutInflater
    private val listNotifications: List<NotificationDetailMgr>
    private val context: Context
    private lateinit var view : View

    init {
        inflater = LayoutInflater.from(context)
        this.context = context
        this.listNotifications = listNotifications
    }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_HEADER) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_notification_header, parent, false)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_notification_row, parent, false)
        }
         return ViewHolderNotification(view)

     }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_HEADER) {
            //its a header row
            val viewHolderHeader = holder as HeaderViewHolder?
            viewHolderHeader!!.txtHeaderDate.text = listNotifications.get(position).headerDate
            viewHolderHeader.txtHeaderDay.text =
                "(" + listNotifications.get(position).headerDay?.trim { it <= ' ' } + ")"
        } else {
            //its a notification row
            val viewHolderItem = holder as ViewHolderNotification?
            viewHolderItem!!.lytListRow.setOnClickListener(View.OnClickListener { //call order detail activity if URI contains order id
                if ((listNotifications[position].uri?.type == NotificationConstants.ORDER_DETAILS)) {
                    if (!StringHelper.isStringNullOrEmpty(listNotifications[position].uri?.parameters?.orderId)) {
                        val newIntent = Intent(context, OrderDetailsActivity::class.java)
                        newIntent.putExtra(
                            ZeemartAppConstants.ORDER_ID,
                            listNotifications[position].uri?.parameters?.orderId
                        )
                        newIntent.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            listNotifications[position].outletId
                        )
                        context.startActivity(newIntent)
                    }
                }
            })
            viewHolderItem.txtNotifTitle.text =
                (Html.fromHtml(listNotifications.get(position).text))
            val dateDayTime =
                DateHelper.getDateInHourMinuteAMPM(listNotifications[position].dateTime)
            viewHolderItem.txtTimePlace.text = dateDayTime
            if (StringHelper.isStringNullOrEmpty(listNotifications[position].imageURL)) {
                if (!StringHelper.isStringNullOrEmpty(listNotifications[position].supplierName)) {
                    viewHolderItem.imgNotification.visibility = View.INVISIBLE
                    viewHolderItem.lytSupplierThumbNail.visibility = View.VISIBLE
                    viewHolderItem.lytSupplierThumbNail.setBackgroundColor(
                        CommonMethods.SupplierThumbNailBackgroundColor(
                            listNotifications[position].supplierName!!, context
                        )
                    )
                    viewHolderItem.txtSupplierThumbNail.text =
                        CommonMethods.SupplierThumbNailShortCutText(listNotifications.get(position).supplierName!!)
                    viewHolderItem.txtSupplierThumbNail.setTextColor(
                        CommonMethods.SupplierThumbNailTextColor(
                            listNotifications[position].supplierName!!, context
                        )
                    )
                } else {
                    viewHolderItem.imgNotification.visibility = View.VISIBLE
                    viewHolderItem.lytSupplierThumbNail.visibility = View.GONE
                    viewHolderItem.imgNotification.setImageDrawable(context.resources.getDrawable(R.drawable.placeholder_all))
                }
            } else {
                viewHolderItem.imgNotification.visibility = View.VISIBLE
                viewHolderItem.lytSupplierThumbNail.visibility = View.GONE
                Picasso.get().load(listNotifications[position].imageURL)
                    .placeholder(R.drawable.placeholder_all).fit().centerInside().into(
                    viewHolderItem.imgNotification
                )
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return listNotifications.size
    }

    override fun getItemViewType(position: Int): Int {
        if (listNotifications[position].isHeader) {
            return ITEM_HEADER
        } else return if (!listNotifications.get(position).isHeader) {
            ITEM_NOTIFICATION
        } else {
            super.getItemViewType(position)
        }
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
        return R.layout.lst_notification_header
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        if (listNotifications.size > 0) {
            val txtDate = header?.findViewById<TextView>(R.id.txt_list_header_date)
            txtDate?.text = listNotifications.get(headerPosition).headerDate
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            val txtDayString =
                "(" + listNotifications[headerPosition].headerDay?.trim { it <= ' ' } + ")"
            val txtDay = header?.findViewById<TextView>(R.id.txt_list_header_day)
            txtDay?.text = txtDayString.trim { it <= ' ' }
            ZeemartBuyerApp.setTypefaceView(
                txtDay,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return if (listNotifications.size > itemPosition) listNotifications.get(itemPosition).isHeader else false
    }

    override fun onHeaderClicked(header: View?, position: Int) {
        //do nothing
    }

    internal inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeaderDate: TextView
        var txtHeaderDay: TextView

        init {
            txtHeaderDate = itemView.findViewById(R.id.txt_list_header_date)
            txtHeaderDay = itemView.findViewById(R.id.txt_list_header_day)
            ZeemartBuyerApp.setTypefaceView(
                txtHeaderDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtHeaderDay,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    internal inner class ViewHolderNotification(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var txtNotifTitle: TextView
        var txtTimePlace: TextView
        var imgNotification: ImageView
        var lytListRow: RelativeLayout
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgNotification = itemView.findViewById(R.id.img_notif)
            txtNotifTitle = itemView.findViewById(R.id.txt_notif_details)
            txtTimePlace = itemView.findViewById(R.id.txt_time_place)
            lytListRow = itemView.findViewById(R.id.lyt_notif_row)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtNotifTitle,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtTimePlace,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    companion object {
        private val ITEM_HEADER = 1
        private val ITEM_NOTIFICATION = 2
    }
}