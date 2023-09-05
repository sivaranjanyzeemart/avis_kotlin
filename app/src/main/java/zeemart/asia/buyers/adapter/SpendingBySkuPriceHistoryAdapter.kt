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
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse

/**
 * Created by ParulBhandari on 2/20/2018.
 */
class SpendingBySkuPriceHistoryAdapter(
    var context: Context,
    priceUpdateList: List<PriceUpdateModelBaseResponse.PriceDetailModel>,
) : RecyclerView.Adapter<SpendingBySkuPriceHistoryAdapter.ViewHolder?>() {
    var priceUpdateList: List<PriceUpdateModelBaseResponse.PriceDetailModel>

    init {
        this.priceUpdateList = priceUpdateList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext()).inflate(
            R.layout.layout_spending_by_sku_price_history_row,
            parent,
            false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date: String =
            DateHelper.getDateInDateMonthYearFormat(priceUpdateList[position].invoiceDate)
        val day: String =
            DateHelper.getDateIn3LetterDayFormat(priceUpdateList[position].invoiceDate)
        val invoiceDate = "$date ($day)"
        holder.txtDate.setText(invoiceDate)
        holder.txt_invoice_order_num.setText("Invoice AB-1990200/KLM-93")
        //        long totalPrice = priceUpdateList.get(position).getTotalUnitPrice().getAmount();
//        double totalPriceDouble = ZeemartBuyerApp.centsToDollar(totalPrice);
//        String totalPriceString = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(totalPriceDouble,2));
        holder.txtProductPrice.setText(priceUpdateList[position].unitPrice?.displayValue)
        if (priceUpdateList[position].discount != null
            && priceUpdateList[position].discount?.amount != 0.0)
            {
            holder.txtProductDiscount.setVisibility(View.VISIBLE)
            holder.txtProductPrice.setBackgroundResource(R.drawable.faint_yellow_solid_rounded_corner)
        } else {
            holder.txtProductDiscount.setVisibility(View.GONE)
            holder.txtProductPrice.setBackgroundResource(R.color.transparent)
        }
        if (priceUpdateList[position].invoiceNum != null && !priceUpdateList[position].invoiceNum
                ?.isEmpty()!!
        ) {
            holder.txt_invoice_order_num.setText("Invoice" + " " + priceUpdateList[position].invoiceNum)
        } else if (priceUpdateList[position].isAdminHubUpdate) {
            holder.txt_invoice_order_num.setText("Zeemart")
        } else if (priceUpdateList[position].updatedBy != null) {
            holder.txt_invoice_order_num.setText(
                priceUpdateList[position].updatedBy?.firstName +
                        priceUpdateList[position].updatedBy?.lastName)
        } else {
            holder.txt_invoice_order_num.setVisibility(View.GONE)
        }
    }

    override fun getItemCount(): Int {
        return priceUpdateList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView
        var txtProductDiscount: TextView
        var txtProductPrice: TextView
        var txt_invoice_order_num: TextView

        init {
            txtDate = itemView.findViewById<TextView>(R.id.txt_invoice_date)
            txt_invoice_order_num = itemView.findViewById<TextView>(R.id.txt_invoice_order_id)
            txtProductDiscount = itemView.findViewById<TextView>(R.id.txt_product_discounted)
            txtProductPrice = itemView.findViewById<TextView>(R.id.txt_product_price)
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txt_invoice_order_num,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductDiscount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductPrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }
}