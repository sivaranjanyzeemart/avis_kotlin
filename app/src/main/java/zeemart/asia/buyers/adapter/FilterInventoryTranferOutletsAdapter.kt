package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.modelsimport.InventoryTransferLocationModel

/**
 * Created by RajPrudhviMarella on 23/Mar/2022.
 */
class FilterInventoryTranferOutletsAdapter(
    private val lstOutlets: List<InventoryTransferLocationModel.TransferOutlets>,
    private val listener: SelectedInvoiceFiltersListener,
    private val context: Context
) : RecyclerView.Adapter<FilterInventoryTranferOutletsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_filter_order_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtFilterName.text = lstOutlets[position].outletName
        holder.layoutFilterOrderRow.setOnClickListener {
            for (i in lstOutlets.indices) {
                lstOutlets[i].isSelected = false
            }
            notifyDataSetChanged()
            if (lstOutlets[position].isSelected) {
                holder.imgSelectFilter.visibility = View.GONE
                lstOutlets[position].isSelected = false
                listener.onFilterDeselected(lstOutlets[position])
            } else {
                holder.imgSelectFilter.visibility = View.VISIBLE
                lstOutlets[position].isSelected = true
                listener.onFilterSelected(lstOutlets[position])
            }
        }
        if (lstOutlets[position].isSelected) {
            holder.imgSelectFilter.visibility = View.VISIBLE
        } else {
            holder.imgSelectFilter.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return lstOutlets.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutFilterOrderRow: RelativeLayout
        var txtFilterName: TextView
        var imgSelectFilter: ImageView

        init {
            txtFilterName = itemView.findViewById(R.id.txt_filter_name)
            imgSelectFilter = itemView.findViewById(R.id.img_select_filter)
            layoutFilterOrderRow = itemView.findViewById(R.id.lyt_filter_order_row)
            setTypefaceView(txtFilterName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    interface SelectedInvoiceFiltersListener {
        fun onFilterSelected(transferOutlets: InventoryTransferLocationModel.TransferOutlets?)
        fun onFilterDeselected(transferOutlets: InventoryTransferLocationModel.TransferOutlets?)
    }
}