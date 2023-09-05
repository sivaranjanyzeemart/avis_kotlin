package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.roundUpDoubleToLong
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.marketlist.Product

/**
 * Created by ParulBhandari on 12/15/2017.
 */
class OrderDetailProductListAdapter(
    private val context: Context,
    private val products: List<Product>
) : RecyclerView.Adapter<OrderDetailProductListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_view_order_details, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && !StringHelper.isStringNullOrEmpty(
                products[position].customName
            )
        ) {
            holder.txtProductCustomName.visibility = View.VISIBLE
            holder.txtProductCustomName.text = products[position].productName
            holder.txtItemName.text = products[position].customName
        } else {
            holder.txtItemName.text = products[position].productName
            holder.txtProductCustomName.visibility = View.GONE
        }
        holder.btnDelete.visibility = View.INVISIBLE
        val itemPrice = products[position].unitPrice?.getDisplayValueWithUom(
            returnShortNameValueUnitSize(
                products[position].unitSize
            )
        )
        if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
            holder.txtItemPerPrice.visibility = View.VISIBLE
            holder.txtItemPerPrice.text = itemPrice
        } else {
            holder.txtItemPerPrice.visibility = View.GONE
        }

//        if (products.get(position).quantity == null) {
//            products.get(position).quantity = (0.00);
//        }
//        Double quantityLocal = ZeemartBuyerApp.getPriceDouble(products.get(position).quantity, 2);
//        products.get(position).quantity = (quantityLocal);
//
//        if (UnitSizeModel.isDecimalAllowed(products.get(position).getUnitSize())) {
//            holder.edtProductQuantity.setText(ZeemartBuyerApp.getTwoDecimalQuantity(products.get(position).getQuantity()));
//        } else {
//            int localQuantity = quantityLocal.intValue();
//            holder.edtProductQuantity.setText(localQuantity + "");
//
//        }
        holder.edtProductQuantity.text = products[position].quantityDisplayValue
        holder.txtUnitSize.text =
            " " + returnShortNameValueUnitSize(products[position].unitSize)
        if (products[position].totalPrice == null) {
            calculateTotalPriceQuantityChanged(position)
            //display total price in given currency
//            Double dollarTotalPrice = ZeemartBuyerApp.centsToDollar(products.get(position).getTotalPrice().getAmount());
//            holder.txtTotalPrice.setText(" • " + ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(dollarTotalPrice, 2)));
            setTotalPriceText(position, holder)
        } else {
            //change given total price in required currency
//            double dollarTotalPrice = ZeemartBuyerApp.centsToDollar(products.get(position).getTotalPrice().getAmount());
//            holder.txtTotalPrice.setText(" • " + ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(dollarTotalPrice, 2)));
            setTotalPriceText(position, holder)
        }
        if (StringHelper.isStringNullOrEmpty(products[position].notes)) {
            holder.lytNotes.visibility = View.GONE
            holder.txtNotes.text = ""
        } else {
            holder.lytNotes.visibility = View.VISIBLE
            holder.txtNotes.text = products[position].notes
        }
        setUIFont(holder)
    }

    private fun setTotalPriceText(position: Int, holder: ViewHolder) {
        if (!StringHelper.isStringNullOrEmpty(products[position].totalPrice!!.displayValueWithBullet)) {
            holder.txtTotalPrice.visibility = View.VISIBLE
            holder.txtTotalPrice.text = products[position].totalPrice!!.displayValueWithBullet
        } else {
            holder.txtTotalPrice.visibility = View.GONE
        }
    }

    /**
     * calculates the total price by multiplying unitPrice and quantity
     * round up the totalprice if in decimal
     * save the rounded cent value to the total Price object
     *
     * @param position
     */
    private fun calculateTotalPriceQuantityChanged(position: Int) {
        val totalPrice =
            products[position].unitPrice?.amount!! * products[position].quantity
        //Create new total PriceDetail Object
        val totPrice = PriceDetails()
        totPrice.currencyCode = (products[position].unitPrice?.currencyCode)
        totPrice.amount = roundUpDoubleToLong(totalPrice)
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
        var btnDelete: ImageButton
        var txtUnitSize: TextView
        var txtNotes: TextView
        var imgNotes: ImageButton
        var lytNotes: RelativeLayout
        var txtProductCustomName: TextView

        init {
            txtUnitSize = itemView.findViewById(R.id.unit_size_value)
            txtItemName = itemView.findViewById(R.id.txt_product_name)
            btnDelete = itemView.findViewById(R.id.btn_delete)
            txtItemPerPrice = itemView.findViewById(R.id.txt_per_price)
            edtProductQuantity = itemView.findViewById(R.id.txt_quantity_value)
            txtTotalPrice = itemView.findViewById(R.id.txt_price_product)
            txtNotes = itemView.findViewById(R.id.txt_product_notes)
            imgNotes = itemView.findViewById(R.id.btn_view_notes)
            lytNotes = itemView.findViewById(R.id.lyt_notes)
            txtProductCustomName = itemView.findViewById(R.id.txt_product_custom_name)
        }
    }

    private fun setUIFont(holder: ViewHolder) {
        setTypefaceView(
            holder.edtProductQuantity,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(holder.txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            holder.txtItemPerPrice,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(holder.txtTotalPrice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(holder.txtUnitSize, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(holder.txtNotes, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS)
        setTypefaceView(
            holder.txtProductCustomName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
        )
    }
}