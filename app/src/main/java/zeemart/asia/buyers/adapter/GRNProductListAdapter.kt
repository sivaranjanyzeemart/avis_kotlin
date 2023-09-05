package zeemart.asia.buyers.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.GRNskuSelectionDialog
import zeemart.asia.buyers.helper.GRNskuSelectionDialog.GRNskuSelectionDialogChangeListener
import zeemart.asia.buyers.helper.ProductDataHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import java.util.*
import java.util.regex.Pattern

/**
 * Created by RajPrudhviMarella on 14/June/2021.
 */
class GRNProductListAdapter(
    private val context: Context,
    private var filterProducts: List<ProductDetailBySupplier>,
    searchStringArray: Array<String>?,
    mListener: onProductSelectedGRNListener
) : RecyclerView.Adapter<GRNProductListAdapter.ViewHolder>(), Filterable,
    GRNskuSelectionDialogChangeListener {
    private lateinit var dialog: CustomChangeQuantityDialog
    private val products: List<ProductDetailBySupplier>
    private var searchStringArray: Array<String>?
    private val mListener: onProductSelectedGRNListener

    init {
        products = filterProducts
        this.searchStringArray = searchStringArray
        this.mListener = mListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_grn_sku_item_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        var productCodeTag = filterProducts[position].supplierProductCode
        if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
            holder.txtProductCodeTag.visibility = View.GONE
        }
        if (searchStringArray != null && searchStringArray!!.size > 0) {
            val itemName = filterProducts[position].productName + ""
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
            val itemCustomName = filterProducts[position].productCustomName + ""
            val sbCustomName = SpannableStringBuilder(itemCustomName)
            val pCustomName =
                Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
            val mCustomName = pCustomName.matcher(itemCustomName)
            while (mCustomName.find()) {
                val color = context.resources.getColor(R.color.text_blue)
                sbCustomName.setSpan(
                    ForegroundColorSpan(color),
                    mCustomName.start(),
                    mCustomName.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                ) == true && filterProducts[position].productCustomName != null
            ) {
                holder.txtProductCustomName.visibility = View.VISIBLE
                holder.txtItemName.text = sbCustomName
                holder.txtProductCustomName.text = sb
            } else {
                holder.txtItemName.text = sb
                holder.txtProductCustomName.visibility = View.GONE
            }
            var productCode = productCodeTag
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                ) == true && filterProducts[position].customProductCode != null
            ) {
                productCode = filterProducts[position].customProductCode
            }
            val sbTag = SpannableStringBuilder(productCode)
            val pTag = Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
            val mTag = pTag.matcher(productCode)
            while (mTag.find()) {
                val color = context.resources.getColor(R.color.text_blue)
                sbTag.setSpan(
                    ForegroundColorSpan(color),
                    mTag.start(),
                    mTag.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            holder.txtProductCodeTag.text = sbTag
        } else {
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                ) == true && filterProducts[position].productCustomName != null
            ) {

//            }
//            if (filterProducts.get(position).productCustomName != null) {
                holder.txtProductCustomName.text = filterProducts[position].productName
                holder.txtProductCustomName.visibility = View.VISIBLE
                holder.txtItemName.text = filterProducts[position].productCustomName
            } else {
                holder.txtItemName.text = filterProducts[position].productName
                holder.txtProductCustomName.visibility = View.GONE
            }
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                ) == true && filterProducts[position].customProductCode != null
            ) {
                productCodeTag = filterProducts[position].customProductCode
            }
            holder.txtProductCodeTag.text = productCodeTag
        }
        holder.lytProductItems.setOnClickListener {
            if (dialog == null || dialog != null && !dialog!!.isShowing) {
                val product = ProductDataHelper.createSelectedProductObjectForGRN(
                    filterProducts[position]
                )
                val createOrderHelperDialog = GRNskuSelectionDialog(
                    context, product, this@GRNProductListAdapter
                )
                dialog = if (product?.isCustom == true) {
                    createOrderHelperDialog.createCustomeSKUChangeQuantityDialog(false)
                } else {
                    createOrderHelperDialog.createChangeQuantityDialog(false)
                }
                dialog.getWindow()!!
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }

    override fun getItemCount(): Int {
        return filterProducts.size
    }

    override fun onDataChange(product: Product?, isChanged: Boolean) {
        mListener.onProductSelect(product)
    }

    override fun requestForRemove(product: Product?) {}
    override fun getFilter(): Filter {
        return FilterProducts()
    }

    private inner class FilterProducts : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null && constraint.length > 0) {
                val searchedString = constraint as String
                searchStringArray =
                    searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val tempList: MutableList<ProductDetailBySupplier> = ArrayList()
                for (i in products.indices) {
                    var searchStringFound = false
                    for (j in searchStringArray!!.indices) {
                        if (products[i].productName?.lowercase(Locale.getDefault())?.contains(
                                searchStringArray!![j].lowercase(Locale.getDefault())
                            ) == true || products[i].supplierProductCode?.lowercase(
                                Locale.getDefault()
                            )?.contains(
                                searchStringArray!![j].lowercase(Locale.getDefault())
                            ) == true || products[i].productCustomName?.lowercase(
                                Locale.getDefault()
                            )?.contains(
                                searchStringArray!![j].lowercase(Locale.getDefault())
                            )!!
                        ) {
                            searchStringFound = true
                            continue
                        } else {
                            searchStringFound = false
                            break
                        }
                    }
                    if (searchStringFound) {
                        tempList.add(products[i])
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
            } else {
                searchStringArray = null
                filterResults.count = products.size
                filterResults.values = products
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null) {
                filterProducts = results.values as List<ProductDetailBySupplier>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtProductCodeTag: TextView
        var lytProductItems: LinearLayout
        var txtProductCustomName: TextView

        init {
            txtProductCodeTag = itemView.findViewById(R.id.txt_product_code_tag)
            txtItemName = itemView.findViewById(R.id.txt_product_name)
            lytProductItems = itemView.findViewById(R.id.lyt_product_details)
            txtProductCustomName = itemView.findViewById(R.id.txt_product_changed_name)
            setTypefaceView(txtProductCodeTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(
                txtProductCustomName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
        }
    }

    interface onProductSelectedGRNListener {
        fun onProductSelect(product: Product?)
    }
}