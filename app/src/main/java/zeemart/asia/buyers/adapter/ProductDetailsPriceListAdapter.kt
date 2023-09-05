package zeemart.asia.buyers.adapter

import android.content.Context
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.StocksList
import zeemart.asia.buyers.models.UnitSizeModel.Companion.isDecimalAllowed
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Inventory

/**
 * Created by ParulBhandari on 1/25/2018.
 */
class ProductDetailsPriceListAdapter(
    private val context: Context,
    private val priceList: List<ProductPriceList>,
    private val stocks: List<StocksList>?,
    private val calledFrom: String,
    private val timePriceUpdated: Long,
    private val supplierDetails: DetailSupplierDataModel?
) : RecyclerView.Adapter<ProductDetailsPriceListAdapter.ViewHolder>() {
    var unitSizeStockMap: Map<String?, StocksList>? = null
    private var supplierInventory: Inventory? = null
    private var isDisplayStockInfo = false

    init {
        if (stocks != null) {
            unitSizeStockMap = createUnitSizeStockMap(stocks)
        }
        if (supplierDetails != null) {
            if (supplierDetails.supplier != null && supplierDetails.supplier.settings != null) {
                supplierInventory = supplierDetails.supplier.settings!!.inventory
            }
            if (supplierInventory != null && supplierInventory!!.status != null && supplierInventory!!.isStatus(
                    Inventory.Status.ACTIVE
                ) && supplierInventory!!.allowNegative == false
            ) {
                isDisplayStockInfo = true
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_product_detail_price_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!StringHelper.isStringNullOrEmpty(calledFrom)
            && (calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST) || (calledFrom
            == ZeemartAppConstants.CALLED_FROM_DEALS_SEARCH_LIST)) {
            holder.txtDatePerPriceUpdated.visibility = View.GONE
            if (priceList[position].dealPrice != null) {
                val itemPrice = priceList[position].dealPrice!!.displayValue
                if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                    holder.perUnitPrice.visibility = View.VISIBLE
                    if (priceList[position].originalPrice != null && priceList[position].originalPrice!!.amount != 0.0) {
                        holder.perUnitPrice.setTextColor(context.resources.getColor(R.color.pinky_red))
                    } else {
                        holder.perUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    }
                    holder.perUnitPrice.text = itemPrice
                } else {
                    holder.perUnitPrice.visibility = View.GONE
                }
                if (priceList[position].originalPrice != null && priceList[position].originalPrice!!.amount != 0.0) {
                    val string = SpannableString(priceList[position].originalPrice!!.displayValue)
                    string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                    holder.txtProductStrikePrice.visibility = View.VISIBLE
                    holder.txtProductStrikePrice.text = string
                } else {
                    holder.txtProductStrikePrice.visibility = View.GONE
                }
            } else {
                if (priceList[position].originalPrice != null && priceList[position].originalPrice!!.amount != 0.0) {
                    val itemPrice = priceList[position].originalPrice!!.displayValue
                    holder.perUnitPrice.visibility = View.VISIBLE
                    holder.perUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    holder.perUnitPrice.text = itemPrice
                } else {
                    holder.perUnitPrice.visibility = View.GONE
                }
                holder.txtProductStrikePrice.visibility = View.GONE
            }
        } else if (!StringHelper.isStringNullOrEmpty(calledFrom)
            && (calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST) || calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST) {
            holder.txtDatePerPriceUpdated.visibility = View.GONE
            if (priceList[position].essentialPrice != null) {
                val itemPrice = priceList[position].essentialPrice!!.displayValue
                if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                    holder.perUnitPrice.visibility = View.VISIBLE
                    if (priceList[position].originalPrice != null && priceList[position].originalPrice!!.amount != 0.0) {
                        holder.perUnitPrice.setTextColor(context.resources.getColor(R.color.pinky_red))
                    } else {
                        holder.perUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    }
                    holder.perUnitPrice.text = itemPrice
                } else {
                    holder.perUnitPrice.visibility = View.GONE
                }
                if (priceList[position].originalPrice != null && priceList[position].originalPrice!!.amount != 0.0) {
                    val string = SpannableString(priceList[position].originalPrice!!.displayValue)
                    string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                    holder.txtProductStrikePrice.visibility = View.VISIBLE
                    holder.txtProductStrikePrice.text = string
                } else {
                    holder.txtProductStrikePrice.visibility = View.GONE
                }
            } else {
                if (priceList[position].originalPrice != null && priceList[position].originalPrice!!.amount != 0.0) {
                    val itemPrice = priceList[position].originalPrice!!.displayValue
                    holder.perUnitPrice.visibility = View.VISIBLE
                    holder.perUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    holder.perUnitPrice.text = itemPrice
                } else {
                    holder.perUnitPrice.visibility = View.GONE
                }
                holder.txtProductStrikePrice.visibility = View.GONE
            }
        } else {
            holder.txtDatePerPriceUpdated.visibility = View.VISIBLE
            if (priceList[position].price != null) {
                val price = priceList[position].price!!.displayValue
                holder.perUnitPrice.text = price
            } else {
                holder.perUnitPrice.text = ""
            }
            holder.txtProductStrikePrice.visibility = View.GONE
        }
        val measure = returnShortNameValueUnitSizeForQuantity(
            priceList[position].unitSize
        )
        val datePerPrice = DateHelper.getDateInDateMonthYearFormat(
            timePriceUpdated
        )
        holder.txtDatePerPriceUpdated.text =
            String.format(context.getString(R.string.as_of), datePerPrice)
        val moqDouble = priceList[position].moq
        holder.txtunitSize.text = " " + "/" + " " + priceList[position].unitSize
        if (isDecimalAllowed(priceList[position].unitSize!!)) {
            holder.txtMoq.text = String.format(
                context.getString(R.string.format_moq),
                "" + moqDouble,
                measure
            )
        } else {
            if (moqDouble != null) {
                holder.txtMoq.text = String.format(
                    context.getString(R.string.format_moq),
                    "" + moqDouble.toInt(),
                    measure
                )
            }
        }
        val isItemAvailable = getIsItemUnAvailable(priceList[position])
        if (isDisplayStockInfo) {
            holder.txtitemStockInfo.visibility = View.VISIBLE
            holder.txtMoq.textSize = 10f
            if (isItemAvailable) {
                holder.txtitemStockInfo.text = context.getString(R.string.txt_in_stock)
                holder.txtitemStockInfo.setTextColor(context.resources.getColor(R.color.green))
            } else {
                holder.txtitemStockInfo.text =
                    context.getString(R.string.txt_currently_unavailable)
                holder.txtitemStockInfo.setTextColor(context.resources.getColor(R.color.pinky_red))
            }
        } else {
            holder.txtitemStockInfo.visibility = View.GONE
            holder.txtMoq.textSize = 16f
        }
    }

    private fun getIsItemUnAvailable(productPriceList: ProductPriceList): Boolean {
        var isItemAvailable = false
        if (productPriceList.isStatus(ProductPriceList.UnitSizeStatus.VISIBLE)) {
            if (unitSizeStockMap != null) if (unitSizeStockMap!!.containsKey(productPriceList.unitSize)) {
                val stockForUOM = unitSizeStockMap!![productPriceList.unitSize]
                val stockAvailable = calculateStockAvailable(stockForUOM)
                if (stockAvailable > 0 && stockAvailable > productPriceList.moq!!) {
                    isItemAvailable = true
                }
            }
        }
        return isItemAvailable
    }

    override fun getItemCount(): Int {
        return priceList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var perUnitPrice: TextView
        var txtunitSize: TextView
        var txtMoq: TextView
        var txtitemStockInfo: TextView
        var txtDatePerPriceUpdated: TextView
        var txtProductStrikePrice: TextView

        init {
            perUnitPrice = itemView.findViewById(R.id.product_detail_price_list_per_unit_price)
            setTypefaceView(perUnitPrice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtunitSize = itemView.findViewById(R.id.txtUnitSize)
            setTypefaceView(txtunitSize, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            txtMoq = itemView.findViewById(R.id.product_detail_MOQ)
            setTypefaceView(txtMoq, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            txtitemStockInfo = itemView.findViewById(R.id.txt_item_stock_info)
            setTypefaceView(
                txtitemStockInfo,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtDatePerPriceUpdated = itemView.findViewById(R.id.product_detail_price_list_per_date)
            setTypefaceView(
                txtDatePerPriceUpdated,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            txtProductStrikePrice = itemView.findViewById(R.id.txt_strike_price)
            setTypefaceView(
                txtProductStrikePrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    /**
     * Calculate the total stock available
     *
     * @param stock
     * @return
     */
    private fun calculateStockAvailable(stock: StocksList?): Double {
        var totalBought = 0.0
        var stockLeft = 0.0
        if (stock!!.orderedQuantity != null && stock.orderedQuantity!!.size > 0) for (i in stock.orderedQuantity!!.indices) {
            totalBought = totalBought + stock.orderedQuantity!![i]
        }
        if (stock.stockQuantity != null) {
            stockLeft = stock.stockQuantity!! - totalBought
        }
        return stockLeft
    }

    private fun createUnitSizeStockMap(stocks: List<StocksList>?): Map<String?, StocksList> {
        val unitSizeStockMap: MutableMap<String?, StocksList> = HashMap()
        if (stocks != null && stocks.size > 0) {
            for (i in stocks.indices) {
                unitSizeStockMap[stocks[i].unitSize] = stocks[i]
            }
        }
        return unitSizeStockMap
    }
}