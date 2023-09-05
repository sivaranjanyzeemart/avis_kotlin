package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.inventory.InventoryDataManager
import zeemart.asia.buyers.inventory.InventoryDataManager.InventoryShelvesUIModel
import zeemart.asia.buyers.inventory.ShelveProductsListActivity
import zeemart.asia.buyers.inventory.StockCountActivity
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.inventory.Shelve
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by ParulBhandari on 08/03/2019
 */
class InventoryDashboardListsAdapter(
    private val context: Context,
    private val stockShelveDataList: List<InventoryShelvesUIModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val SHELVE_LIST_ROW_VIEW_TYPE = 1
    private val BUYER_ADMIN_VIEW_TYPE = 2
    private var noItems = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SHELVE_LIST_ROW_VIEW_TYPE) {
            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_inventory_dashboard_lists_item,
                parent,
                false
            )
            ViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_buyer_admin_message, parent, false)
            ViewHolderBuyerAdmin(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!stockShelveDataList[position].isBuyerAdminMessage) {
            val holderItem = holder as ViewHolder
            holderItem.txtListItemName.text =
                stockShelveDataList[position].inventoryShelves?.shelveName
            if (stockShelveDataList[position].inventoryShelves?.summary != null && stockShelveDataList[position].inventoryShelves?.summary?.productsBelowParLevel != null && stockShelveDataList[position].inventoryShelves?.summary?.productsBelowParLevel != 0) {
                holderItem.txtOutOfStockItems.visibility = View.VISIBLE

                val counter =
                    stockShelveDataList[position].inventoryShelves?.summary?.productsBelowParLevel?.plus(stockShelveDataList[position].inventoryShelves?.summary?.productsOutOfStock!!)

                var outOfStock = " "
                outOfStock = if (counter == 1) {
                    String.format(context.resources.getString(R.string.txt_item_low_stock), counter)
                } else {
                    String.format(
                        context.resources.getString(R.string.txt_items_low_stock),
                        counter
                    )
                }
                holderItem.txtOutOfStockItems.text = outOfStock
            } else {
                holderItem.txtOutOfStockItems.visibility = View.GONE
            }
            if (stockShelveDataList[position].inventoryShelves != null && stockShelveDataList[position].inventoryShelves?.summary != null && stockShelveDataList[position].inventoryShelves?.summary
                    ?.productsInShelve != null && stockShelveDataList[position].inventoryShelves?.summary
                    ?.productsInShelve == 0
            ) {
                holderItem.btnCount.visibility = View.GONE
                noItems = true
            } else {
                if (stockShelveDataList[position].inventoryShelves != null && stockShelveDataList[position].inventoryShelves?.summary == null) {
                    holderItem.btnCount.visibility = View.GONE
                    noItems = true
                } else {
                    holderItem.btnCount.visibility = View.VISIBLE
                    noItems = false
                }
            }
            if (stockShelveDataList[position].inventoryShelves?.isSavedDraftStockCount!!) {
                val countDate = stockShelveDataList[position].inventoryShelves?.savedDraftTimeCreated
                val draftSaved = countDate?.times(1000)
                val lastCountedDate = Date(draftSaved!!)
                val todayDate = Date()
                val differenceMap = DateHelper.computeDateDiff(lastCountedDate, todayDate)
                var updatedTimeText = " "
                updatedTimeText = if (differenceMap[TimeUnit.DAYS] == 0L) {
                    if (differenceMap[TimeUnit.HOURS] == 0L) {
                        updatedTimeText + differenceMap[TimeUnit.MINUTES] + " " + context.resources.getString(
                            R.string.txt_minutes_ago
                        )
                    } else {
                        updatedTimeText + differenceMap[TimeUnit.HOURS] + " " + context.resources.getString(
                            R.string.txt_hours_ago
                        )
                    }
                } else {
                    val count =
                        DateHelper.getDaysBetweenDates(System.currentTimeMillis(), draftSaved)
                    if (count == 1) {
                        updatedTimeText + count + " " + context.resources.getString(R.string.txt_day_ago)
                    } else {
                        updatedTimeText + count + " " + context.resources.getString(R.string.txt_days_ago)
                    }
                }
                holderItem.txtDrafSaved.text = String.format(
                    context.getString(R.string.txt_inventory_draft_saved),
                    updatedTimeText
                )
                holderItem.txtDrafSaved.visibility = View.VISIBLE
            } else {
                holderItem.txtDrafSaved.visibility = View.GONE
            }
            if (stockShelveDataList[position].inventoryShelves?.summary != null
                && stockShelveDataList[position].inventoryShelves?.summary?.timeLastCounted != null
                && stockShelveDataList[position].inventoryShelves?.summary?.timeLastCounted != 0L) {
                val dateLastCounted =
                    stockShelveDataList[position].inventoryShelves?.summary?.timeLastCounted?.times(1000)
                val lastCountedDate = Date(dateLastCounted!!)
                val todayDate = Date()
                val differenceMap = DateHelper.computeDateDiff(lastCountedDate, todayDate)
                var updatedTimeText = " "
                if (stockShelveDataList[position].inventoryShelves?.summary?.productsBelowParLevel != null
                    && stockShelveDataList[position].inventoryShelves?.summary?.productsBelowParLevel != 0) {
                    updatedTimeText = " " + context.resources.getString(R.string.bullet) + " "
                }
                updatedTimeText = if (differenceMap[TimeUnit.DAYS] == 0L) {
                    if (differenceMap[TimeUnit.HOURS] == 0L) {
                        updatedTimeText + differenceMap[TimeUnit.MINUTES] + " " + context.resources.getString(
                            R.string.txt_minutes_ago
                        )
                    } else {
                        updatedTimeText + differenceMap[TimeUnit.HOURS] + " " + context.resources.getString(
                            R.string.txt_hours_ago
                        )
                    }
                } else {
                    val count =
                        DateHelper.getDaysBetweenDates(System.currentTimeMillis(), dateLastCounted)
                    if (count == 1) {
                        updatedTimeText + count + " " + context.resources.getString(R.string.txt_day_ago)
                    } else {
                        updatedTimeText + count + " " + context.resources.getString(R.string.txt_days_ago)
                    }
                }
                holderItem.txtCountedDaysAgo.setTextColor(context.resources.getColor(R.color.dark_grey))
                holderItem.txtCountedDaysAgo.text = updatedTimeText
            } else {
                holderItem.txtCountedDaysAgo.setTextColor(context.resources.getColor(R.color.pinky_red))
                holderItem.txtCountedDaysAgo.setText(R.string.txt_never_been_counted)
            }
            if (stockShelveDataList[position].inventoryShelves != null && stockShelveDataList[position].inventoryShelves?.summary != null
                && stockShelveDataList[position].inventoryShelves?.summary
                    ?.lastEstimatedValue != null
            ) holderItem.txtItemCount.text = stockShelveDataList[position].inventoryShelves?.summary
                ?.lastEstimatedValue?.displayValue
            if (!UserPermission.HasViewPrice()) {
                holderItem.txtItemCount.visibility = View.GONE
            } else {
                holderItem.txtItemCount.visibility = View.VISIBLE
            }
            setListItemBackground(holderItem, position)
            holder.itemView.setOnClickListener {
                if (SharedPref.defaultOutlet != null) {
                    AnalyticsHelper.logActionForInventory(
                        context,
                        AnalyticsHelper.TAP_INVENTORY_LISTS_LIST,
                        SharedPref.defaultOutlet!!
                    )
                }
                val newIntent = Intent(context, ShelveProductsListActivity::class.java)
                val shelve = Shelve()
                shelve.shelveId = stockShelveDataList[position].inventoryShelves?.shelveId
                shelve.shelveName = stockShelveDataList[position].inventoryShelves?.shelveName
                if (stockShelveDataList[position].inventoryShelves != null
                    && stockShelveDataList[position].inventoryShelves?.countDate != null) {
                    shelve.timeLastCounted =
                        stockShelveDataList[position].inventoryShelves?.countDate?.times(1000)
                }
                val shelveJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(shelve)
                newIntent.putExtra(InventoryDataManager.INTENT_SHELVE_ID, shelveJson)
                newIntent.putExtra(InventoryDataManager.INTENT_SHELVE_NO_ITEMS, noItems)
                if (stockShelveDataList[position].inventoryShelves != null
                    && stockShelveDataList[position].inventoryShelves?.summary != null
                    && stockShelveDataList[position].inventoryShelves?.summary
                        ?.lastEstimatedValue != null
                ) {
                    newIntent.putExtra(
                        InventoryDataManager.INTENT_SHELVE_VALUE,
                        stockShelveDataList[position].inventoryShelves?.summary?.lastEstimatedValue?.displayValue
                    )
                } else {
                    newIntent.putExtra(InventoryDataManager.INTENT_SHELVE_VALUE, "")
                }
                context.startActivity(newIntent)
            }
            holderItem.btnCount.setOnClickListener {
                if (SharedPref.defaultOutlet != null) {
                    AnalyticsHelper.logActionForInventory(
                        context,
                        AnalyticsHelper.TAP_INVENTORY_LISTS_COUNT,
                        SharedPref.defaultOutlet!!
                    )
                }
                val newIntent = Intent(context, StockCountActivity::class.java)
                val shelve = Shelve()
                shelve.shelveName = stockShelveDataList[position].inventoryShelves?.shelveName
                shelve.shelveId = stockShelveDataList[position].inventoryShelves?.shelveId
                if (stockShelveDataList[position].inventoryShelves != null
                    && stockShelveDataList[position].inventoryShelves?.countDate != null) {
                    shelve.timeLastCounted =
                        stockShelveDataList[position].inventoryShelves?.countDate?.times(1000)
                }
                val shelveJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(shelve)
                newIntent.putExtra(InventoryDataManager.INTENT_SHELVE_ID, shelveJson)
                val isStockEverCounted: Boolean
                isStockEverCounted =
                    if (stockShelveDataList[position].inventoryShelves != null
                        && stockShelveDataList[position].inventoryShelves?.summary != null
                        && stockShelveDataList[position].inventoryShelves?.summary?.timeLastCounted != null
                        && stockShelveDataList[position].inventoryShelves?.summary?.timeLastCounted != 0L) {
                        true
                    } else {
                        false
                    }
                newIntent.putExtra(
                    InventoryDataManager.INTENT_IS_LAST_STOCK_COUNT_DATA_AVAILABLE,
                    isStockEverCounted
                )
                context.startActivity(newIntent)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (stockShelveDataList[position].isBuyerAdminMessage) {
            BUYER_ADMIN_VIEW_TYPE
        } else {
            SHELVE_LIST_ROW_VIEW_TYPE
        }
    }

    fun setListItemBackground(holder: ViewHolder, position: Int) {
        if (itemCount == 1) {
            holder.itemView.setBackgroundResource(R.drawable.rect_rounded_corners_white)
        } else {
            if (position == 0) {
                holder.itemView.setBackgroundResource(R.drawable.rect_rounded_top_white)
            } else if (itemCount > 1 && position == itemCount - 1) {
                holder.itemView.setBackgroundResource(R.drawable.rect_rounded_bottom_white)
            } else {
                holder.itemView.setBackgroundResource(R.color.white)
            }
        }
    }

    override fun getItemCount(): Int {
        return stockShelveDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtListItemName: TextView
        var txtItemCount: TextView
        var txtCountedDaysAgo: TextView
        var txtOutOfStockItems: TextView
        var txtDrafSaved: TextView
        var btnCount: Button

        init {
            txtListItemName = itemView.findViewById(R.id.txt_inventory_list_item_heading)
            txtItemCount = itemView.findViewById(R.id.txt_inventory_list_item_no_of_items)
            txtCountedDaysAgo = itemView.findViewById(R.id.txt_inventory_list_counted_days_ago)
            txtOutOfStockItems = itemView.findViewById(R.id.txt_out_of_stock_items)
            btnCount = itemView.findViewById(R.id.btn_inventory_list_item_count)
            txtDrafSaved = itemView.findViewById(R.id.txt_inventory_list_draft_saved)
            ZeemartBuyerApp.setTypefaceView(
                txtCountedDaysAgo,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtDrafSaved,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOutOfStockItems,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtItemCount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtListItemName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                btnCount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    inner class ViewHolderBuyerAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtBuyerAdminMessage: TextView? = null

        init {
            val txtBuyerAdminMessage: TextView
            txtBuyerAdminMessage = itemView.findViewById(R.id.txt_buyer_admin_message)
            txtBuyerAdminMessage.setText(R.string.txt_buye_admin_message_shelve_list)
            ZeemartBuyerApp.setTypefaceView(
                txtBuyerAdminMessage,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }
}