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
 * Created by RajPrudhviMarella on 18/May/2021.
 */
class TotalSpendingTagListAdapter(
    context: Context,
    categoryList: List<ReportResponse.TotalSpendingTagsData>,
    listener: OnTotalSpendingItemClick
) : RecyclerView.Adapter<TotalSpendingTagListAdapter.ViewHolder?>() {
    private val categoryList: List<ReportResponse.TotalSpendingTagsData>
    private val context: Context
    private val listener: OnTotalSpendingItemClick

    init {
        this.listener = listener
        this.context = context
        this.categoryList = categoryList
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
        holder.txtSupplierName.setText(categoryList[position].outletTag)
        val priceDetails = PriceDetails(categoryList[position].total)
        holder.txtTotalSpend.setText(priceDetails.displayValue)
        holder.txtSpendingSupplierName.setVisibility(View.GONE)
        holder.lytRow.setOnClickListener(View.OnClickListener {
            listener.onItemClick(
                categoryList[position].outletTag, ""
            )
        })
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSupplierName: TextView
        var txtTotalSpend: TextView
        var lytRow: ConstraintLayout
        var txtSpendingSupplierName: TextView

        init {
            txtSupplierName = itemView.findViewById<TextView>(R.id.total_spend_supplier_name)
            txtTotalSpend = itemView.findViewById<TextView>(R.id.total_spend_amount)
            lytRow = itemView.findViewById<ConstraintLayout>(R.id.lyt_total_spending_row)
            txtSpendingSupplierName =
                itemView.findViewById<TextView>(R.id.total_spending_supplier_name)
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