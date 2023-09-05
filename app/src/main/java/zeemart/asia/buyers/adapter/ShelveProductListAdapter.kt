package zeemart.asia.buyers.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.*
import zeemart.asia.buyers.inventory.InventoryDataManager.ShelveProductListUIModel
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.inventory.Shelve
import zeemart.asia.buyers.models.inventory.StockCountEntries.StockInventoryProduct
import java.util.*
import java.util.regex.Pattern

class ShelveProductListAdapter(
    private val context: Context,
    shelveProductList: List<ShelveProductListUIModel>,
    stockageId: String?,
    shelve: Shelve,
    isLastStockCount: Boolean,
    posValue: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val stockageId: String?
    private val shelve: Shelve
    private var shelveProductList: List<ShelveProductListUIModel> = ArrayList()
    private var filterableShelveProductList: List<ShelveProductListUIModel> = ArrayList()
    private lateinit var searchStringArray: Array<String>
    private val mFilter: FilterShelveProducts
    private val isLastStockCountAvailable: Boolean
    private val posIntegration: Boolean

    init {
        this.shelveProductList = shelveProductList
        filterableShelveProductList = shelveProductList
        this.stockageId = stockageId
        this.shelve = shelve
        mFilter = FilterShelveProducts()
        isLastStockCountAvailable = isLastStockCount
        posIntegration = posValue
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_STOCK_PRODUCT_ROW) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_shelve_inventory_list_item, parent, false)
            return ViewHolder(v)
        } else if (viewType == VIEW_TYPE_BUYER_ADMIN_MESSAGE) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_buyer_admin_message, parent, false)
            return ViewHolderBuyerAdmin(v)
        } else if (viewType == VIEW_TYPE_DELETE_STOCK) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_delete_stock_message, parent, false)
            return ViewHolderDeleteStock(v)
        }
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_shelve_inventory_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentPosition = holder.absoluteAdapterPosition
        if (filterableShelveProductList[currentPosition].isDeleteThisStockCountMessage) {
            val viewHolderDeleteStock = holder as ViewHolderDeleteStock
            viewHolderDeleteStock.deleteStock.setOnClickListener(View.OnClickListener {
                if (stockageId != null) {
                    val newIntent = Intent(context, DeleteCountActivity::class.java)
                    newIntent.putExtra(InventoryDataManager.INTENT_STOCKAGE_ID, stockageId)
                    context.startActivity(newIntent)
                }
            })
        } else if (filterableShelveProductList[currentPosition].isBuyerAdminMessage) {
            //do nothing just display the message
        } else {
            val holderItem = holder as ViewHolder
            holderItem.txtQuantity0Indicator.visibility = View.GONE
            if (filterableShelveProductList[currentPosition].inventoryProduct!!.unitSize != null) holderItem.txtUOM.text =
                returnShortNameValueUnitSizeForQuantity(
                    filterableShelveProductList.get(currentPosition).inventoryProduct!!.unitSize
                ) else holderItem.txtUOM.text = ""
            if (filterableShelveProductList[currentPosition].inventoryProduct!!.stockQuantity != null) {
                if ((filterableShelveProductList[currentPosition].inventoryProduct!!.stockQuantity != null && filterableShelveProductList[currentPosition].inventoryProduct!!.stockQuantity == 0.0) || filterableShelveProductList[currentPosition].inventoryProduct!!.getBelowParLevel()) {
                    holderItem.txtQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
                    holderItem.txtQuantity0Indicator.visibility = View.VISIBLE
                } else {
                    holderItem.txtQuantity.setTextColor(context.resources.getColor(R.color.black))
                    holderItem.txtQuantity0Indicator.visibility = View.GONE
                }
                holderItem.txtQuantity.text =
                    filterableShelveProductList.get(currentPosition).inventoryProduct!!.stockQuantityDisplayValue
            } else holderItem.txtQuantity.text = "-"
            if (posIntegration) {
                if (filterableShelveProductList[currentPosition].inventoryProduct!!.onHandQuantity != null) {
                    if ((filterableShelveProductList[currentPosition].inventoryProduct!!.onHandQuantity != null && filterableShelveProductList[currentPosition].inventoryProduct!!.onHandQuantity == 0.0) || filterableShelveProductList[currentPosition].inventoryProduct!!.getBelowParLevel()) {
                        holderItem.txtQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
                        holderItem.txtQuantity0Indicator.visibility = View.VISIBLE
                    } else {
                        holderItem.txtQuantity.setTextColor(context.resources.getColor(R.color.black))
                        holderItem.txtQuantity0Indicator.visibility = View.GONE
                    }
                    holderItem.txtQuantity.text =
                        filterableShelveProductList.get(currentPosition).inventoryProduct!!.onHandQuantityDisplayValue
                } else {
                    holderItem.txtQuantity.setTextColor(context.resources.getColor(R.color.grey_medium))
                    setTypefaceView(
                        holderItem.txtQuantity,
                        ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
                    )
                }
            }
            if (filterableShelveProductList[currentPosition].inventoryProduct!!.getBelowParLevel()) {
                holderItem.txtInventoryPerLevel.visibility = View.VISIBLE
                holderItem.txtInventoryPerLevel.text = String.format(
                    context.resources.getString(R.string.txt_items_per_level),
                    filterableShelveProductList.get(currentPosition).inventoryProduct!!.parLevelDisplayValue + " " + returnShortNameValueUnitSizeForQuantity(
                        filterableShelveProductList.get(currentPosition).inventoryProduct!!.unitSize
                    ) + " "
                )
            } else {
                holderItem.txtInventoryPerLevel.visibility = View.GONE
            }
            var SupplierName = ""
            if (filterableShelveProductList[currentPosition].inventoryProduct!!.getBelowParLevel()) {
                SupplierName = " " + context.resources.getString(R.string.bullet) + " "
            }
            holderItem.txtSupplierName.text =
                SupplierName + filterableShelveProductList.get(currentPosition).inventoryProduct!!.supplier!!.supplierName
            if (searchStringArray != null && searchStringArray!!.size > 0) {
                val itemName =
                    filterableShelveProductList[currentPosition].inventoryProduct!!.productName + ""
                val sb = SpannableStringBuilder(itemName)
                var searchedString = ""
                for (i in searchStringArray!!.indices) {
                    searchedString = searchedString + "|" + searchStringArray!![i]
                }
                searchedString = searchedString.substring(1, searchedString.length)
                val p = Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
                val m = p.matcher(itemName)
                while (m.find()) {
                    val color = context.resources.getColor(R.color.text_blue)
                    sb.setSpan(
                        ForegroundColorSpan(color),
                        m.start(),
                        m.end(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
                holderItem.txtItemName.text = sb
            } else {
                holderItem.txtItemName.text =
                    filterableShelveProductList.get(currentPosition).inventoryProduct!!.productName
            }
            holderItem.itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (isLastStockCountAvailable) if (filterableShelveProductList[currentPosition].inventoryProduct!!.stockQuantity != null) {
                        showAlertToSelectStockCountAmendmentOrAdjustment(filterableShelveProductList[currentPosition].inventoryProduct)
                    } else {
                        showAlertNoStockCountAdded()
                    }
                }
            })
        }
    }

    private fun showAlertNoStockCountAdded() {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(
            R.string.dialog_ok_button_text,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        val dialog = builder.create()
        dialog.setTitle(context.getString(R.string.txt_item_newly_added))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(context.getString(R.string.txt_item_not_included))
        dialog.show()
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(context.resources.getColor(R.color.color_azul_two))
    }

    private fun showAlertToSelectStockCountAmendmentOrAdjustment(shelveProductListUIModel: StockInventoryProduct?) {
        val d = Dialog(context, R.style.CustomDialogTheme)
        d.setContentView(R.layout.lyt_stock_count_action)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                d.dismiss()
            }
        })
        val txtStockMovements = d.findViewById<TextView>(R.id.txt_view_stock_movements)
        val txtAdjustment = d.findViewById<TextView>(R.id.txt_request_verify)
        val txtEditQuantity = d.findViewById<TextView>(R.id.txt_view_outlet)
        setTypefaceView(txtStockMovements, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtAdjustment, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtEditQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        val recordAdjustment = d.findViewById<RelativeLayout>(R.id.lyt_reuest_verify)
        recordAdjustment.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (SharedPref.defaultOutlet != null) {
                    AnalyticsHelper.logActionForInventory(
                        context,
                        AnalyticsHelper.TAP_INVENTORY_LIST_ADJUSTMENT,
                        SharedPref.defaultOutlet!!
                    )
                }
                val newIntent = Intent(context, SaveAdjustmentActivity::class.java)
                newIntent.putExtra(
                    InventoryDataManager.INTENT_SHELVE_ID,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(shelve)
                )
                val productJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(shelveProductListUIModel)
                newIntent.putExtra(InventoryDataManager.INTENT_PRODUCT_DETAIL, productJson)
                context.startActivity(newIntent)
                d.dismiss()
            }
        })
        val viewStockMovements = d.findViewById<RelativeLayout>(R.id.lyt_view_stock_movement)
        viewStockMovements.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val newIntent = Intent(context, ViewStockMovementsActivity::class.java)
                val productJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(shelveProductListUIModel)
                newIntent.putExtra(InventoryDataManager.INTENT_PRODUCT_DETAIL, productJson)
                context.startActivity(newIntent)
            }
        })
        val edtQuantity = d.findViewById<RelativeLayout>(R.id.lyt_view_outlet)
        edtQuantity.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (SharedPref.defaultOutlet != null) {
                    AnalyticsHelper.logActionForInventory(
                        context,
                        AnalyticsHelper.TAP_INVENTORY_LIST_EDITQTY,
                        SharedPref.defaultOutlet!!
                    )
                }
                val newIntent = Intent(context, SaveAmendmentActivity::class.java)
                newIntent.putExtra(
                    InventoryDataManager.INTENT_SHELVE_ID,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(shelve)
                )
                val productJson =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(shelveProductListUIModel)
                newIntent.putExtra(InventoryDataManager.INTENT_PRODUCT_DETAIL, productJson)
                context.startActivity(newIntent)
                d.dismiss()
            }
        }
        )
        d.show()
    }

    override fun getItemCount(): Int {
        return filterableShelveProductList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (filterableShelveProductList[position].isBuyerAdminMessage) return VIEW_TYPE_BUYER_ADMIN_MESSAGE else return if (filterableShelveProductList.get(
                position
            ).isDeleteThisStockCountMessage
        ) VIEW_TYPE_DELETE_STOCK else VIEW_TYPE_STOCK_PRODUCT_ROW
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtSupplierName: TextView
        var txtQuantity: TextView
        var txtUOM: TextView
        var txtInventoryPerLevel: TextView
        var txtQuantity0Indicator: TextView

        init {
            txtItemName = itemView.findViewById(R.id.inventory_product_txt_product_name)
            txtSupplierName = itemView.findViewById(R.id.inventory_product_txt_supplier_name)
            txtQuantity = itemView.findViewById(R.id.inventory_product_txt_quantity)
            txtUOM = itemView.findViewById(R.id.inventory_product_txt_uom)
            txtInventoryPerLevel = itemView.findViewById(R.id.inventory_per_level)
            txtQuantity0Indicator = itemView.findViewById(R.id.dashboard_text_draft_indicator)
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtInventoryPerLevel,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    inner class ViewHolderBuyerAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtBuyerAdminMessage: TextView

        init {
            txtBuyerAdminMessage = itemView.findViewById(R.id.txt_buyer_admin_message)
            setTypefaceView(
                txtBuyerAdminMessage,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    inner class ViewHolderDeleteStock(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deleteStock: LinearLayout

        init {
            deleteStock = itemView.findViewById(R.id.lyt_delete_Stock)
        }
    }

    private inner class FilterShelveProducts() : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            val searchString = constraint.toString()
            if (!StringHelper.isStringNullOrEmpty(searchString)) {
                searchStringArray =
                    searchString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val tempFilterList: MutableList<ShelveProductListUIModel> = ArrayList()
                for (i in shelveProductList.indices) {
                    if (shelveProductList[i].isBuyerAdminMessage || shelveProductList[i].isDeleteThisStockCountMessage) {
                        tempFilterList.add(shelveProductList[i])
                    } else {
                        var searchStringFound = false
                        for (j in searchStringArray!!.indices) {
                            if (SharedPref.readBool(
                                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                                    false
                                )!! && shelveProductList[i].inventoryProduct!!.customName != null
                            ) {
                                if (shelveProductList[i].inventoryProduct!!.customName!!.lowercase(
                                        Locale.getDefault()
                                    ).contains(
                                        searchStringArray!![j].lowercase(Locale.getDefault())
                                    )
                                ) {
                                    searchStringFound = true
                                    continue
                                } else {
                                    searchStringFound = false
                                    break
                                }
                            } else {
                                if (shelveProductList[i].inventoryProduct!!.productName!!.lowercase(
                                        Locale.getDefault()
                                    ).contains(
                                        searchStringArray!![j].lowercase(Locale.getDefault())
                                    )
                                ) {
                                    searchStringFound = true
                                    continue
                                } else {
                                    searchStringFound = false
                                    break
                                }
                            }
                        }
                        if (searchStringFound) {
                            tempFilterList.add(shelveProductList[i])
                        }
                    }
                }
                filterResults.count = tempFilterList.size
                filterResults.values = tempFilterList
            } else {
                searchStringArray != null
                filterResults.count = shelveProductList.size
                filterResults.values = shelveProductList
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null) {
                filterableShelveProductList = results.values as List<ShelveProductListUIModel>
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private val VIEW_TYPE_STOCK_PRODUCT_ROW = 1
        private val VIEW_TYPE_BUYER_ADMIN_MESSAGE = 2
        private val VIEW_TYPE_DELETE_STOCK = 3
    }
}