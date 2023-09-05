package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class TotalSpendingListAdapter(
    context: Context,
    supplierList: List<ReportResponse.TotalSpendingBySupplierData>,
    listener: OnTotalSpendingItemClick
) : RecyclerView.Adapter<TotalSpendingListAdapter.ViewHolder?>() {
    private val supplierList: List<ReportResponse.TotalSpendingBySupplierData>
    private val listener: OnTotalSpendingItemClick
    private val context: Context

    init {
        this.listener = listener
        this.context = context
        this.supplierList = supplierList
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
        holder.txtSupplierName.setText(supplierList[position].supplier?.supplierName)
        //        long totalPrice = supplierList.get(position).getTotalSpendingSupplier();
//        double totalPriceDouble = ZeemartBuyerApp.centsToDollar(totalPrice);
//        String totalPriceString = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(totalPriceDouble,2));
        val colors = ArrayList<Int>()
        colors.add(context.resources.getColor(R.color.chart_blue))
        colors.add(context.resources.getColor(R.color.chart_light_blue))
        colors.add(context.resources.getColor(R.color.chart_green))
        colors.add(context.resources.getColor(R.color.chart_yellow))
        colors.add(context.resources.getColor(R.color.chart_orange))
        colors.add(context.resources.getColor(R.color.chart_red))
        if (position > 5) {
            holder.txtListIndicator.setBackgroundColor(context.resources.getColor(R.color.dark_grey))
        } else {
            holder.txtListIndicator.setBackgroundColor(colors[position])
        }
        val totalSpendPrice = PriceDetails(supplierList[position].totalSpending)
        holder.txtTotalSpend.setText(totalSpendPrice.displayValue)
        holder.txtSpendingSupplierName.setVisibility(View.GONE)
        holder.txtListIndicator.setVisibility(View.VISIBLE)
        holder.lytRow.setOnClickListener(View.OnClickListener {
            listener.onItemClick(
                supplierList[position].supplier?.supplierId,
                supplierList[position].supplier?.supplierName
            )
        })
    }

    override fun getItemCount(): Int {
        return supplierList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSupplierName: TextView
        var txtTotalSpend: TextView
        var lytRow: ConstraintLayout
        var txtSpendingSupplierName: TextView
        var txtListIndicator: TextView

        init {
            txtSupplierName = itemView.findViewById<TextView>(R.id.total_spend_supplier_name)
            txtTotalSpend = itemView.findViewById<TextView>(R.id.total_spend_amount)
            lytRow = itemView.findViewById<ConstraintLayout>(R.id.lyt_total_spending_row)
            txtSpendingSupplierName =
                itemView.findViewById<TextView>(R.id.total_spending_supplier_name)
            txtListIndicator = itemView.findViewById<TextView>(R.id.list_indicator)
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