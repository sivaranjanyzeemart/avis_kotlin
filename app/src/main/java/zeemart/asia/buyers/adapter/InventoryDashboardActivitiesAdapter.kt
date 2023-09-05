package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.*
import zeemart.asia.buyers.models.StockageType
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.models.inventory.StockCountEntriesMgr

/**
 * Created by ParulBhandari on 08/03/2019
 */
class InventoryDashboardActivitiesAdapter(
    private val context: Context,
    private val stockCountEntriesMgrList: List<StockCountEntriesMgr>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HeaderItemDecoration.StickyHeaderInterface {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_order_list_header, parent, false)
            return ViewHolderHeader(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_inventory_dashboard_activity_item, parent, false)
            return ViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val stockCountEntriesMgrItem = stockCountEntriesMgrList[position]
        if (stockCountEntriesMgrItem.isHeader) {
            val header = holder as ViewHolderHeader
            val headerDayData = "(" + stockCountEntriesMgrItem.headerDay?.trim { it <= ' ' } + ")"
            header.txtDay.text = headerDayData.trim { it <= ' ' }
            header.txtDate.text = stockCountEntriesMgrItem.headerDate
        } else {
            val item = holder as ViewHolder
            if ((stockCountEntriesMgrItem.stockageType == StockageType.STOCK_COUNT.name)) {
                item.txtListName.visibility = View.VISIBLE
                item.txtListName.text =
                    context.getString(R.string.txt_created_by) + " " + stockCountEntriesMgrItem.createdBy?.firstName + " " + stockCountEntriesMgrItem.createdBy?.lastName
                item.txtItemName.text = stockCountEntriesMgrList.get(position).shelve?.shelveName
                item.imgCountOrAdjustment.setImageDrawable(context.resources.getDrawable(R.drawable.inventory_stock_count))
                item.txtTimeActivityType.text = DateHelper.getDateInHourMinFormat(
                    stockCountEntriesMgrList.get(position).timeCreated
                ) + " • " + context.getString(StockageType.STOCK_COUNT.resId)
            } else if ((stockCountEntriesMgrItem.stockageType == StockageType.STOCK_COUNT_AMENDMENTS.name)) {
                item.txtListName.visibility = View.VISIBLE
                item.imgCountOrAdjustment.setImageDrawable(context.resources.getDrawable(R.drawable.inventory_stock_count))
                if (SharedPref.readBool(
                        SharedPref.USER_INVENTORY_SETTING_STATUS,
                        false
                    ) == true && (stockCountEntriesMgrList[position].products!![0].customName != null) && !StringHelper.isStringNullOrEmpty(
                        stockCountEntriesMgrList[position].products!![0].customName
                    )
                ) {
                    item.txtItemName.text =
                        stockCountEntriesMgrList.get(position).products?.get(0)?.customName
                } else {
                    item.txtItemName.text =
                        stockCountEntriesMgrList.get(position).products?.get(0)?.productName
                }
                item.txtTimeActivityType.text = DateHelper.getDateInHourMinFormat(
                    stockCountEntriesMgrList.get(position).timeCreated
                ) + " • " + context.getString(StockageType.STOCK_COUNT_AMENDMENTS.resId)
                item.txtListName.text = stockCountEntriesMgrItem.shelve?.shelveName
            } else if ((stockCountEntriesMgrItem.stockageType == StockageType.UOM_CONVERSION.name)) {
                item.txtListName.visibility = View.VISIBLE
                item.imgCountOrAdjustment.setImageDrawable(context.resources.getDrawable(R.drawable.uom_icon))
                if (SharedPref.readBool(
                        SharedPref.USER_INVENTORY_SETTING_STATUS,
                        false
                    ) == true && (stockCountEntriesMgrList[position].products!![0].customName != null) && !StringHelper.isStringNullOrEmpty(
                        stockCountEntriesMgrList[position].products!![0].customName
                    )
                ) {
                    item.txtItemName.text =
                        stockCountEntriesMgrList.get(position).products?.get(0)?.customName
                } else {
                    item.txtItemName.text =
                        stockCountEntriesMgrList.get(position).products?.get(0)?.productName
                }
                item.txtTimeActivityType.text = DateHelper.getDateInHourMinFormat(
                    stockCountEntriesMgrList.get(position).timeCreated
                ) + " • " + context.getString(StockageType.UOM_CONVERSION.resId)
                item.txtListName.text = stockCountEntriesMgrItem.shelve?.shelveName
            } else {
                item.txtListName.visibility = View.VISIBLE
                item.imgCountOrAdjustment.setImageDrawable(context.resources.getDrawable(R.drawable.inventory_adjustments))
                if (SharedPref.readBool(
                        SharedPref.USER_INVENTORY_SETTING_STATUS,
                        false
                    ) == true && (stockCountEntriesMgrList[position].products!![0].customName != null) && !StringHelper.isStringNullOrEmpty(
                        stockCountEntriesMgrList[position].products!![0].customName
                    )
                ) {
                    item.txtItemName.text =
                        stockCountEntriesMgrList.get(position).products?.get(0)?.customName
                } else {
                    item.txtItemName.text =
                        stockCountEntriesMgrList.get(position).products?.get(0)?.productName
                }
                item.txtTimeActivityType.text = DateHelper.getDateInHourMinFormat(
                    stockCountEntriesMgrList.get(position).timeCreated
                ) + " • " + context.getString(StockageType.ADJUSTMENT.resId)
                item.txtListName.text = stockCountEntriesMgrItem.shelve?.shelveName
            }
            if ((stockCountEntriesMgrItem.status == StockCountEntries.InventoryActivityStatus.DELETED.value)) item.itemView.alpha =
                0.3f else item.itemView.alpha = 1.0f
            item.itemView.setOnClickListener(View.OnClickListener {
                if (SharedPref.defaultOutlet != null) {
                    AnalyticsHelper.logActionForInventoryActivity(
                        context,
                        AnalyticsHelper.TAP_INVENTORY_ACTIVITY_DETAIL,
                        SharedPref.defaultOutlet!!,
                        stockCountEntriesMgrItem.stockageType
                    )
                }
                if ((stockCountEntriesMgrItem.stockageType == StockageType.STOCK_COUNT.name)) {
                    val newIntent = Intent(context, StockCountDetailActivity::class.java)
                    newIntent.putExtra(
                        InventoryDataManager.INTENT_STOCKAGE_ID,
                        stockCountEntriesMgrItem.stockageId
                    )
                    context.startActivity(newIntent)
                } else if ((stockCountEntriesMgrItem.stockageType == StockageType.ADJUSTMENT.name)) {
                    val newIntent = Intent(context, AdjustmentRecordActivity::class.java)
                    newIntent.putExtra(
                        InventoryDataManager.INTENT_STOCKAGE_ID,
                        stockCountEntriesMgrItem.stockageId
                    )
                    context.startActivity(newIntent)
                } else if ((stockCountEntriesMgrItem.stockageType == StockageType.STOCK_COUNT_AMENDMENTS.name)) {
                    val newIntent = Intent(context, AmendmentRecordActivity::class.java)
                    newIntent.putExtra(
                        InventoryDataManager.INTENT_STOCKAGE_ID,
                        stockCountEntriesMgrItem.stockageId
                    )
                    context.startActivity(newIntent)
                } else if ((stockCountEntriesMgrItem.stockageType == StockageType.UOM_CONVERSION.name)) {
                    val newIntent = Intent(context, UomConversionRecordActivity::class.java)
                    newIntent.putExtra(
                        InventoryDataManager.INTENT_STOCKAGE_ID,
                        stockCountEntriesMgrItem.stockageId
                    )
                    context.startActivity(newIntent)
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return stockCountEntriesMgrList.size
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
        val txtHeaderDate = header?.findViewById<TextView>(R.id.txt_list_header_date)
        setTypefaceView(txtHeaderDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        val txtHeaderDay = header?.findViewById<TextView>(R.id.txt_list_header_day)
        setTypefaceView(txtHeaderDay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        txtHeaderDate?.text = stockCountEntriesMgrList.get(headerPosition).headerDate
        val txtDay =
            "(" + stockCountEntriesMgrList[headerPosition].headerDay?.trim { it <= ' ' } + ")"
        txtHeaderDay?.text = txtDay.trim { it <= ' ' }
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return false
    }

    override fun onHeaderClicked(header: View?, position: Int) {
        //do nothing
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item = stockCountEntriesMgrList[position]
        return if (item.isHeader) {
            ITEM_TYPE_HEADER
        } else {
            ITEM_TYPE_ROW
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtTimeActivityType: TextView
        var txtItemName: TextView
        var txtListName: TextView
        var imgCountOrAdjustment: ImageView

        init {
            txtTimeActivityType = itemView.findViewById(R.id.txt_time_adjusment_or_count)
            txtItemName = itemView.findViewById(R.id.txt_adjustment_count_item_heading)
            txtListName = itemView.findViewById(R.id.txt_inventory_list_name)
            imgCountOrAdjustment = itemView.findViewById(R.id.img_adjustment_or_count)
            setTypefaceView(
                txtTimeActivityType,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtListName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView
        var txtDay: TextView

        init {
            txtDate = itemView.findViewById(R.id.txt_list_header_date)
            txtDay = itemView.findViewById(R.id.txt_list_header_day)
            setTypefaceView(txtDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtDay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    companion object {
        private val ITEM_TYPE_HEADER = 0
        private val ITEM_TYPE_ROW = 1
    }
}