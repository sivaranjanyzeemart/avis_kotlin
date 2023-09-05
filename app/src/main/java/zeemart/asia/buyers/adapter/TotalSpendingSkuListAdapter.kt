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
import zeemart.asia.buyers.models.reportsimport.ReportResponse

/**
 * Created by ParulBhandari on 2/20/2018.
 */
class TotalSpendingSkuListAdapter(
    var context: Context,
    skuList: List<ReportResponse.TotalSpendingSkuData>,
    listener: OnTotalSpendingItemClick,
    isShowSupplier: Boolean
) : RecyclerView.Adapter<TotalSpendingSkuListAdapter.ViewHolder?>() {
    var skuList: List<ReportResponse.TotalSpendingSkuData>
    var listener: OnTotalSpendingItemClick
    private val isShowSupplier: Boolean

    init {
        this.skuList = skuList
        this.listener = listener
        this.isShowSupplier = isShowSupplier
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_total_spending_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtSupplierName.setText(skuList[position].productName)
        //        long totalPrice = skuList.get(position).getTotalSpendingOnSkuAmount();
//        double totalPriceDouble = ZeemartBuyerApp.centsToDollar(totalPrice);
//        String totalPriceString = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(totalPriceDouble,2));
        val priceDetails = PriceDetails(skuList[position].totalSpendingOnSkuAmount)
        holder.txtTotalSpend.setText(priceDetails.displayValue)
        if (isShowSupplier) {
            holder.txtSpendingSupplierName.setVisibility(View.VISIBLE)
            holder.txtSpendingSupplierName.setText(
                skuList[position].supplier?.supplierName
            )
        } else {
            holder.txtSpendingSupplierName.setVisibility(View.GONE)
        }
        if (skuList[position].isCredits && skuList[position].totalSpendingOnSkuAmount !== 0.0) {
            holder.lytRow.setVisibility(View.VISIBLE)
            holder.txtSupplierName.setText(R.string.txt_credits)
            priceDetails.amount = skuList[position].totalSpendingOnSkuAmount
            holder.txtTotalSpend.setText("-" + priceDetails.displayValue)
            holder.imgArrow.visibility = View.INVISIBLE
            holder.lytRow.setClickable(false)
            holder.lytRow.setEnabled(false)
        } else if (skuList[position].productName == null) {
            holder.lytRow.setVisibility(View.GONE)
        }
        holder.lytRow.setOnClickListener(View.OnClickListener {
            listener.onItemClick(
                skuList[position].sku, skuList[position].productName
            )
        })
    }

    override fun getItemCount(): Int {
        return skuList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSupplierName: TextView
        var txtTotalSpend: TextView
        var txtSpendingSupplierName: TextView
        var imgArrow: ImageView
        var lytRow: ConstraintLayout

        init {
            txtSupplierName = itemView.findViewById<TextView>(R.id.total_spend_supplier_name)
            txtTotalSpend = itemView.findViewById<TextView>(R.id.total_spend_amount)
            txtSpendingSupplierName =
                itemView.findViewById<TextView>(R.id.total_spending_supplier_name)
            imgArrow = itemView.findViewById(R.id.dashboard_report_week_price_update_blue_arrow)
            lytRow = itemView.findViewById<ConstraintLayout>(R.id.lyt_total_spending_row)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSpendingSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtTotalSpend,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }
}