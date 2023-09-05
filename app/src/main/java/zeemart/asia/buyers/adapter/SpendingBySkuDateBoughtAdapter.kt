package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.reportsimport.SkuDateBoughtUI

/**
 * Created by ParulBhandari on 2/20/2018.
 */
class SpendingBySkuDateBoughtAdapter(
    var context: Context,
    dateBoughtList: ArrayList<SkuDateBoughtUI>
) : RecyclerView.Adapter<SpendingBySkuDateBoughtAdapter.ViewHolder?>() {
    var dateBoughtList: ArrayList<SkuDateBoughtUI>

    init {
        this.dateBoughtList = dateBoughtList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_sku_date_bought_list_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dateBoughtList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date: String =
            DateHelper.getDateInDateMonthYearFormat(dateBoughtList[position].invoiceDate)
        val day: String =
            DateHelper.getDateIn3LetterDayFormat(dateBoughtList[position].invoiceDate)
        val invoiceDate = "$date ($day)"
        holder.txtDate.setText(invoiceDate)
        //        long totalPrice = dateBoughtList.get(position).getAmount();
//        double totalPriceDouble = ZeemartBuyerApp.centsToDollar(totalPrice);
//        String totalPriceString = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(totalPriceDouble,2));
        val priceDetails = PriceDetails(dateBoughtList[position].amount)
        holder.txtProductPrice.setText(priceDetails.displayValue)
        val quantity: Double =
            ZeemartBuyerApp.getPriceDouble(dateBoughtList!![position].quantity!!, 2)
        val quantityString =
            quantity.toString() + " " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                dateBoughtList[position].uom
            )
        holder.txtProductQuantity.setText(quantityString)
    }

//    val itemCount: Int
//        get() = dateBoughtList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView
        var txtProductQuantity: TextView
        var txtProductPrice: TextView

        init {
            txtDate = itemView.findViewById<TextView>(R.id.txt_product_name)
            txtProductQuantity = itemView.findViewById<TextView>(R.id.txt_product_quantity)
            txtProductPrice = itemView.findViewById<TextView>(R.id.txt_product_price)
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductPrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }
}