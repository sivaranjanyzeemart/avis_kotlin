package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.DeleteCountActivity
import zeemart.asia.buyers.inventory.InventoryDataManager
import zeemart.asia.buyers.inventory.InventoryDataManager.ShelveProductListUIModel
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import java.util.*
import java.util.regex.Pattern

class StockProductListAdapter(
    private val context: Context,
    stockInventoryProductList: List<ShelveProductListUIModel>,
    stockageId: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val stockageId: String?
    private var stockInventoryProductList: List<ShelveProductListUIModel> = ArrayList()
    private var filterStockInventoryProductList: List<ShelveProductListUIModel> = ArrayList()
    private val mFilter: FilterStockCountDetailProducts
    private lateinit var searchStringArray: Array<String>

    init {
        this.stockInventoryProductList = stockInventoryProductList
        filterStockInventoryProductList = stockInventoryProductList
        this.stockageId = stockageId
        mFilter = FilterStockCountDetailProducts()
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
        if (filterStockInventoryProductList[position].isDeleteThisStockCountMessage) {
            val viewHolderDeleteStock = holder as ViewHolderDeleteStock
            viewHolderDeleteStock.deleteStock.setOnClickListener(View.OnClickListener {
                if (stockageId != null) {
                    val newIntent = Intent(context, DeleteCountActivity::class.java)
                    newIntent.putExtra(InventoryDataManager.INTENT_STOCKAGE_ID, stockageId)
                    context.startActivity(newIntent)
                }
            })
        } else if (filterStockInventoryProductList[position].isBuyerAdminMessage) {
            //do nothing just display the message
        } else {
            val holderItem = holder as ViewHolder
            if (filterStockInventoryProductList[position].inventoryProduct!!.unitSize != null) holderItem.txtUOM.text =
                returnShortNameValueUnitSizeForQuantity(
                    filterStockInventoryProductList.get(position).inventoryProduct!!.unitSize
                ) else holderItem.txtUOM.text = ""
            if (filterStockInventoryProductList[position].inventoryProduct!!.stockQuantity != null) holderItem.txtQuantity.text =
                filterStockInventoryProductList.get(position).inventoryProduct!!.stockQuantityDisplayValue else holderItem.txtQuantity.text =
                ""
            holderItem.txtSupplierName.text =
                filterStockInventoryProductList.get(position).inventoryProduct!!.supplier!!.supplierName
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                )!! && (filterStockInventoryProductList[position].inventoryProduct!!.customName != null) && !StringHelper.isStringNullOrEmpty(
                    filterStockInventoryProductList[position].inventoryProduct!!.customName
                )
            ) {
                if (searchStringArray != null && searchStringArray!!.size > 0) {
                    val itemName =
                        filterStockInventoryProductList[position].inventoryProduct!!.customName + ""
                    val sb = SpannableStringBuilder(itemName)
                    var searchedString = ""
                    for (i in searchStringArray!!.indices) {
                        searchedString = searchedString + "|" + searchStringArray!![i]
                    }
                    searchedString = searchedString.substring(1, searchedString.length)
                    val p =
                        Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
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
                        filterStockInventoryProductList.get(position).inventoryProduct!!.customName
                }
            } else {
                if (searchStringArray != null && searchStringArray!!.size > 0) {
                    val itemName =
                        filterStockInventoryProductList[position].inventoryProduct!!.productName + ""
                    val sb = SpannableStringBuilder(itemName)
                    var searchedString = ""
                    for (i in searchStringArray!!.indices) {
                        searchedString = searchedString + "|" + searchStringArray!![i]
                    }
                    searchedString = searchedString.substring(1, searchedString.length)
                    val p =
                        Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
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
                        filterStockInventoryProductList.get(position).inventoryProduct!!.productName
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return filterStockInventoryProductList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (filterStockInventoryProductList[position].isBuyerAdminMessage) return VIEW_TYPE_BUYER_ADMIN_MESSAGE else return if (filterStockInventoryProductList.get(
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

        init {
            txtItemName = itemView.findViewById(R.id.inventory_product_txt_product_name)
            txtSupplierName = itemView.findViewById(R.id.inventory_product_txt_supplier_name)
            txtQuantity = itemView.findViewById(R.id.inventory_product_txt_quantity)
            txtUOM = itemView.findViewById(R.id.inventory_product_txt_uom)
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    inner class ViewHolderBuyerAdmin(itemView: View?) : RecyclerView.ViewHolder(
        (itemView)!!
    ) {
        var txtBuyerAdminMessage: TextView? = null
    }

    inner class ViewHolderDeleteStock(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deleteStock: LinearLayout

        init {
            deleteStock = itemView.findViewById(R.id.lyt_delete_Stock)
        }
    }

    private inner class FilterStockCountDetailProducts() : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            val searchString = constraint.toString()
            if (!StringHelper.isStringNullOrEmpty(searchString)) {
                searchStringArray =
                    searchString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val tempFilterList: MutableList<ShelveProductListUIModel> = ArrayList()
                for (i in stockInventoryProductList.indices) {
                    if (stockInventoryProductList[i].isBuyerAdminMessage || stockInventoryProductList[i].isDeleteThisStockCountMessage) {
                        tempFilterList.add(stockInventoryProductList[i])
                    } else {
                        var searchStringFound = false
                        for (j in searchStringArray!!.indices) {
                            if (SharedPref.readBool(
                                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                                    false
                                )!! && stockInventoryProductList[i].inventoryProduct!!.customName != null
                            ) {
                                if (stockInventoryProductList[i].inventoryProduct!!.customName!!.lowercase(
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
                                if (stockInventoryProductList[i].inventoryProduct!!.productName!!.lowercase(
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
                            tempFilterList.add(stockInventoryProductList[i])
                        }
                    }
                }
                filterResults.count = tempFilterList.size
                filterResults.values = tempFilterList
            } else {
                searchStringArray != null
                filterResults.count = stockInventoryProductList.size
                filterResults.values = stockInventoryProductList
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null) {
                filterStockInventoryProductList = results.values as List<ShelveProductListUIModel>
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