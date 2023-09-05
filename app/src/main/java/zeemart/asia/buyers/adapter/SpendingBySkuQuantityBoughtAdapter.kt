package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.reportsimport.QuantityBoughtUI

/**
 * Created by ParulBhandari on 2/20/2018.
 */
class SpendingBySkuQuantityBoughtAdapter(
    var context: Context,
    quantityBoughtList: ArrayList<QuantityBoughtUI>
) : RecyclerView.Adapter<SpendingBySkuQuantityBoughtAdapter.ViewHolder?>() {
    var quantityBoughtList: ArrayList<QuantityBoughtUI>
    var listener: OnTotalSpendingItemClick? = null

    init {
        this.quantityBoughtList = quantityBoughtList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_total_spending_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return quantityBoughtList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quantity: Double =
            ZeemartBuyerApp.getPriceDouble(quantityBoughtList[position].quantity!!, 2)
        holder.txtSupplierName.setText(
            quantity.toString() + " " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                quantityBoughtList[position].uom
            )
        )
        //        long totalPrice = quantityBoughtList.get(position).getAmount();
//        double totalPriceDouble = ZeemartBuyerApp.centsToDollar(totalPrice);
//        String totalPriceString = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(totalPriceDouble,2));
        val priceDetails = PriceDetails(quantityBoughtList[position].amount)
        holder.txtTotalSpend.setText(priceDetails.displayValue)
        holder.txtSpendingSupplierName.setVisibility(View.GONE)
        holder.imgBlueArrow.visibility = View.GONE
    }

//    val itemCount: Int
//        get() = quantityBoughtList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSupplierName: TextView
        var txtTotalSpend: TextView
        var txtSpendingSupplierName: TextView
        var lytRow: ConstraintLayout
        var imgBlueArrow: ImageView

        init {
            txtSupplierName = itemView.findViewById<TextView>(R.id.total_spend_supplier_name)
            txtTotalSpend = itemView.findViewById<TextView>(R.id.total_spend_amount)
            txtSpendingSupplierName =
                itemView.findViewById<TextView>(R.id.total_spending_supplier_name)
            lytRow = itemView.findViewById<ConstraintLayout>(R.id.lyt_total_spending_row)
            imgBlueArrow = itemView.findViewById(R.id.dashboard_report_week_price_update_blue_arrow)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtTotalSpend,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }
}