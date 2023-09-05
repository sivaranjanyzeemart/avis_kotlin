package zeemart.asia.buyers.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.roundUpDoubleToLong
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier

/**
 * Created by ParulBhandari on 12/15/2017.
 */
class InvoiceProductsListAdapter(
    private val context: Context,
    private val products: List<Invoice.InvoiceProduct>,
    private val lstProductListSupplier: Map<String, ProductDetailBySupplier>,
) : RecyclerView.Adapter<InvoiceProductsListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_invoice_product_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtItemName.setText(products[position].productName + "")
        val itemPrice: String = products[position].unitPrice?.getDisplayValueWithUomAndBullet(
            returnShortNameValueUnitSizeForQuantity(
                products[position].unitSize
            )
        )!!
        holder.txtItemPerPrice.text = itemPrice
        holder.edtProductQuantity.setText(products[position].quantityDisplayValue)
        holder.txtUnitSizeValue.text =
            returnShortNameValueUnitSizeForQuantity(products.get(position).unitSize)
        if (products[position].totalPrice == null) {
            calculateTotalPriceQuantityChanged(position)
            holder.txtTotalPrice.setText(products[position].totalPrice?.displayValue)
        } else {
            if (products[position].discount!!.amount != null &&
                products[position].discount!!.amount != 0.0
            ) {
                holder.txtTotalPrice.setText(
                    products[position].totalPrice?.displayValue + " " + String.format(
                        context.getString(R.string.txt_include_discount_amount),
                        products[position].discount!!.displayValue
                    )
                )
            } else {
                holder.txtTotalPrice.setText(products[position].totalPrice?.displayValue)
            }
        }
        Log.d("TOTAL PRICE", "${products[position].totalPrice?.amount} ${"**********'"}")
        if ((lstProductListSupplier.containsKey(products[position].sku)) || (lstProductListSupplier.containsKey(
                products[position].productName
            ))
        ) {
            //Set the MOQ for each Product
            val productPriceList: List<ProductPriceList>?
            if (products[position].sku != null) {
                productPriceList = lstProductListSupplier[products[position].sku]!!.priceList
            } else {
                // for customLineItems
                productPriceList =
                    lstProductListSupplier[products[position].productName]!!.priceList
            }
            if (productPriceList != null) {
                for (i in productPriceList.indices) {
                    if ((productPriceList[i].unitSize == products[position].unitSize)) {
                        if (productPriceList[i].moq != null) {
                            val moq = productPriceList[i].moq
                            products[position].moq = (moq)
                        }
                    }
                }
            }
        }
    }

    /**
     * calculates the total price by multiplying unitPrice and quantity
     * round up the totalprice if in decimal
     * save the rounded cent value to the total Price object
     * @param position
     */
    private fun calculateTotalPriceQuantityChanged(position: Int) {
        val totalPrice = products[position].unitPrice?.amount?.times(products[position].quantity)
        //Create new total PriceDetail Object
        val totPrice = PriceDetails()
        totPrice.currencyCode = (products[position].unitPrice?.currencyCode.toString())
        totPrice.amount = roundUpDoubleToLong(totalPrice!!)
        products[position].totalPrice = totPrice
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtItemPerPrice: TextView
        var txtTotalPrice: TextView
        var edtProductQuantity: TextView
        var txtUnitSizeValue: TextView
        var btnDelete: ImageButton

        init {
            txtItemName = itemView.findViewById(R.id.txt_product_name)
            btnDelete = itemView.findViewById(R.id.btn_delete)
            txtItemPerPrice = itemView.findViewById(R.id.txt_per_price)
            edtProductQuantity = itemView.findViewById(R.id.txt_quantity_value)
            txtUnitSizeValue = itemView.findViewById(R.id.txt_unit_size_value)
            txtTotalPrice = itemView.findViewById(R.id.txt_price_product)
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(
                edtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtUnitSizeValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtItemPerPrice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtTotalPrice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }
}