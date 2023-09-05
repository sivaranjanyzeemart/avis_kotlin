package zeemart.asia.buyers.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.InventoryDataManager
import zeemart.asia.buyers.inventory.InventoryDataManager.CountStockProductListUIModel
import zeemart.asia.buyers.inventory.StockCountActivity.InventoryItemCountListener
import zeemart.asia.buyers.inventory.UomOptionsSelectActivity
import zeemart.asia.buyers.models.UnitSizeModel
import java.util.*
import java.util.regex.Pattern

class StockCountProductListAdapter(
    private val context: Context,
    isLastStockDataAvailable: Boolean,
    productList: List<CountStockProductListUIModel>,
    listener: InventoryItemCountListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var inventoryProductList: List<CountStockProductListUIModel> = ArrayList()
    private var filterInventoryProductList: List<CountStockProductListUIModel> = ArrayList()
    private val listener: InventoryItemCountListener
    private lateinit var searchStringArray: Array<String>
    private val mFilter: FilterStockCountProducts
    var isLastStockDataAvailable = false

    init {
        inventoryProductList = productList
        filterInventoryProductList = productList
        this.listener = listener
        this.isLastStockDataAvailable = isLastStockDataAvailable
        mFilter = FilterStockCountProducts()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_PRODUCT_ROW) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_shelve_product_list_row, parent, false)
            return ViewHolder(itemView)
        } else if (viewType == ITEM_TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_stock_count_header, parent, false)
            return ViewHolderHeader(v)
        } else {
            val viewSwitch = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_count_include_items_received_today, parent, false)
            return ViewHolderSwitch(viewSwitch)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (filterInventoryProductList[position].isIncLudelatestCountRow) {
            val viewHolderSwitch = holder as ViewHolderSwitch
            viewHolderSwitch.switchItemCountReceivedTodayIncluded.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                    listener.onCountItemReceivedTodayChecked(
                        isChecked
                    )
                })
        } else if (filterInventoryProductList[position].isHeader) {
            val header = holder as ViewHolderHeader
            if (filterInventoryProductList.size - 1 == 1) {
                header.txtDate.text =
                    (filterInventoryProductList.size - 1).toString() + " " + context.resources.getString(
                        R.string.txt_item
                    )
            } else {
                header.txtDate.text =
                    (filterInventoryProductList.size - 1).toString() + " " + context.resources.getString(
                        R.string.txt_items
                    )
            }
            if (isLastStockDataAvailable) {
                header.txtOrderTotal.visibility = View.VISIBLE
                header.txtOrderTotal.text =
                    context.resources.getString(R.string.txt_auto_fill_with_last_count_data)
            } else {
                header.txtOrderTotal.visibility = View.GONE
            }
            header.txtOrderTotal.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    listener.onAutoFillClicked()
                }
            })
        } else {
            val holderItem = holder as ViewHolder
            Log.d("fatal", filterInventoryProductList[position].toString())
            if (searchStringArray != null && searchStringArray!!.size > 0) {
                val itemName = filterInventoryProductList[position].productName + ""
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
                holderItem.txtProductName.text = sb
            } else {
                holderItem.txtProductName.text =
                    filterInventoryProductList.get(position).productName
            }
            holderItem.txtSupplierName.text =
                filterInventoryProductList.get(position).supplier?.supplierName
            holderItem.txtUnitSize.text = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                filterInventoryProductList.get(position).unitSize
            )
            //holder.quantityEditTextListener.updatePosition(position);
            //holder.quantityEditTextListener.updateViewHolderReference(holder);
            listener.onInventoryItemCounted()
            if (filterInventoryProductList[position].quantity == null) {
                holderItem.btnDecrease.visibility = View.GONE
                holderItem.edtQuantity.setText("")
            } else {
                holderItem.btnDecrease.visibility = View.VISIBLE
                holderItem.edtQuantity.setText(filterInventoryProductList[position].displayStockQuantityValue())
            }
            if (filterInventoryProductList[position].orderByUnitConversions != null && filterInventoryProductList[position].orderByUnitConversions?.size!! > 1) {
                holderItem.txtOtherUOM.text = String.format(
                    context.getString(R.string.txt_uom_options_items),
                    filterInventoryProductList.get(position).orderByUnitConversions?.size
                )
                holderItem.txtOtherUOM.visibility = View.VISIBLE
            } else {
                holderItem.txtOtherUOM.visibility = View.GONE
            }
            holderItem.txtOtherUOM.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val newIntent = Intent(context, UomOptionsSelectActivity::class.java)
                    val productJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                        filterInventoryProductList[position]
                    )
                    newIntent.putExtra(InventoryDataManager.INTENT_PRODUCT_DETAIL, productJson)
                    (context as Activity).startActivityForResult(newIntent, 217)
                }
            })
            holderItem.btnDecrease.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    var value = 0.0
                    value = filterInventoryProductList[position].quantity!!
                    if (value > 0) {
                        value = value - 1
                        filterInventoryProductList.get(position).quantity = value
                        holderItem.edtQuantity.setText(filterInventoryProductList[position].displayStockQuantityValue())
                        holderItem.edtQuantity.setSelection(holderItem.edtQuantity.text.length)
                        setQuantity(position, value, filterInventoryProductList[position].unitSize!!)
                    }
                }
            })
            holderItem.btnIncrease.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    holderItem.btnDecrease.visibility = View.VISIBLE
                    if (filterInventoryProductList[position].quantity == null) {
                        filterInventoryProductList.get(position).quantity = 1.0
                        holderItem.edtQuantity.setText(filterInventoryProductList[position].displayStockQuantityValue())
                        holderItem.edtQuantity.setSelection(holderItem.edtQuantity.text.length)
                        setQuantity(position, 1.0, filterInventoryProductList[position].unitSize!!)
                    } else {
                        var value = filterInventoryProductList[position].quantity
                        value = value?.plus(1)
                        filterInventoryProductList.get(position).quantity = value
                        holderItem.edtQuantity.setText(filterInventoryProductList[position].displayStockQuantityValue())
                        holderItem.edtQuantity.setSelection(holderItem.edtQuantity.text.length)
                        setQuantity(position, value!!, filterInventoryProductList[position].unitSize!!)
                    }
                    listener.onInventoryItemCounted()
                }
            })
            holderItem.edtQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val value = holderItem.edtQuantity.text.toString()
                    if (value != "") {
                        holderItem.btnDecrease.visibility = View.VISIBLE
                        try {
                            val `val` = value.toDouble()
                            filterInventoryProductList.get(holderItem.adapterPosition).quantity =
                                `val`
                            listener.onInventoryItemCounted()
                            //                            setQuantity(position,val);
                        } catch (e: NumberFormatException) {
                        }
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })
            holderItem.edtQuantity.onFocusChangeListener = object : OnFocusChangeListener {
                override fun onFocusChange(v: View, hasFocus: Boolean) {
                    listener.onInventoryItemCounted()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (filterInventoryProductList[position].isIncLudelatestCountRow) {
            return VIEW_TYPE_SWITCH_FOR_ITEM_COUNT_RECEIVED_TODAY_ROW
        } else return if (filterInventoryProductList.get(position).isHeader) {
            ITEM_TYPE_HEADER
        } else {
            VIEW_TYPE_PRODUCT_ROW
        }
    }

    fun setQuantity(position: Int?, value: Double, unitSize: String) {
        if (filterInventoryProductList[(position)!!].orderByUnitConversions != null && filterInventoryProductList[(position)].orderByUnitConversions?.size!! > 0) {
            for (i in filterInventoryProductList[(position)].orderByUnitConversions?.indices!!) {
                if ((unitSize == filterInventoryProductList[(position)].orderByUnitConversions!![i].orderByUnit)) {
                    val orderByUnitConversion =
                        filterInventoryProductList[(position)].orderByUnitConversions!![i]
                    orderByUnitConversion.selectedQuantity = value
                    filterInventoryProductList.get((position)).orderByUnitConversions?.get(i)?.selectedQuantity =
                        value
                    Log.d(
                        "fatal",
                        "setQuantity: " + filterInventoryProductList[(position)].orderByUnitConversions!![i].toString()
                    )
                } else {
                    filterInventoryProductList.get((position)).orderByUnitConversions?.get(i)?.selectedQuantity =
                        null
                    Log.d(
                        "fatal",
                        "setQuantity: null" + filterInventoryProductList[(position)].orderByUnitConversions!![i].toString()
                    )
                }
            }
        }
        try {
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.d("fatal", e.toString())
        }
    }

    override fun getItemCount(): Int {
        return filterInventoryProductList.size
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtProductName: TextView
        var txtSupplierName: TextView
        var btnIncrease: ImageButton
        var btnDecrease: ImageButton
        var edtQuantity: EditText
        var txtUnitSize: TextView
        var txtOtherUOM: TextView

        init {
            txtProductName = itemView.findViewById(R.id.txt_product_name)
            txtSupplierName = itemView.findViewById(R.id.txt_product_supplier_name)
            btnIncrease = itemView.findViewById(R.id.btn_inc_quantity)
            btnDecrease = itemView.findViewById(R.id.btn_reduce_quantity)
            edtQuantity = itemView.findViewById(R.id.edt_stock_count_value)
            txtOtherUOM = itemView.findViewById(R.id.txt_other_uom)
            checkFocusableViewOnNextAndDonePress()
            txtUnitSize = itemView.findViewById(R.id.txt_product_unit_size)
            ZeemartBuyerApp.setTypefaceView(
                txtProductName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOtherUOM,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                edtQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtUnitSize,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        fun checkFocusableViewOnNextAndDonePress() {
            edtQuantity.setOnEditorActionListener(object : OnEditorActionListener {
                override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                        val v1 = v.focusSearch(View.FOCUS_DOWN)
                        if (v1 != null) {
                            if (!v.requestFocus(View.FOCUS_FORWARD)) {
                                return true
                            }
                        }
                        return false
                    } else return false
                }
            })
        }
    }

    class ViewHolderSwitch(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var switchItemCountReceivedTodayIncluded: Switch
        var txtCountsIncludeItemsReceivedToday: TextView

        init {
            switchItemCountReceivedTodayIncluded =
                itemView.findViewById(R.id.switch_include_item_received_today)
            txtCountsIncludeItemsReceivedToday =
                itemView.findViewById(R.id.txt_items_received_today)
            ZeemartBuyerApp.setTypefaceView(
                txtCountsIncludeItemsReceivedToday,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView
        var txtOrderTotal: TextView

        init {
            txtDate = itemView.findViewById(R.id.txt_list_header_date)
            txtOrderTotal = itemView.findViewById(R.id.txt_list_orders_total)
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderTotal,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    private inner class FilterStockCountProducts() : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            val searchString = constraint.toString()
            if (!StringHelper.isStringNullOrEmpty(searchString)) {
                searchStringArray =
                    searchString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val tempFilterList: MutableList<CountStockProductListUIModel> = ArrayList()
                for (i in inventoryProductList.indices) {
                    if (inventoryProductList[i].isIncLudelatestCountRow || inventoryProductList[i].isHeader) {
                        tempFilterList.add(inventoryProductList[i])
                    } else {
                        var searchStringFound = false
                        for (s: String in searchStringArray!!) {
                            if (inventoryProductList[i].productName?.lowercase(Locale.getDefault())
                                    ?.contains(
                                        s.lowercase(
                                            Locale.getDefault()
                                        )
                                    ) == true
                            ) {
                                searchStringFound = true
                            } else {
                                searchStringFound = false
                            }
                        }
                        if (searchStringFound) {
                            tempFilterList.add(inventoryProductList[i])
                        }
                    }
                }
                filterResults.count = tempFilterList.size
                filterResults.values = tempFilterList
            } else {
                searchStringArray = emptyArray()
                filterResults.count = inventoryProductList.size
                filterResults.values = inventoryProductList
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null) {
                filterInventoryProductList = results.values as List<CountStockProductListUIModel>
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private val ITEM_TYPE_HEADER = 0
        private val VIEW_TYPE_PRODUCT_ROW = 1
        private val VIEW_TYPE_SWITCH_FOR_ITEM_COUNT_RECEIVED_TODAY_ROW = 2
    }
}