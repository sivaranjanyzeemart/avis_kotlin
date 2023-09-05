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
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel

/**
 * Created by RajPrudhviMarella on 10/Jan/2022.
 */
class FilterInvoiceByOrderSupplier(
    private val listener: SelectedInvoiceFiltersListener,
    private val context: Context,
    suppliersList: List<DetailSupplierDataModel?>
) : RecyclerView.Adapter<FilterInvoiceByOrderSupplier.ViewHolder>() {

    var suppliersList: List<DetailSupplierDataModel?>? = null

    init {
        this.suppliersList = suppliersList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_filter_invoice_order_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtFilterName.text = suppliersList!![position]?.supplier?.supplierName
        holder.layoutFilterOrderRow.setOnClickListener {
            if (suppliersList!![position]?.isSupplierSelected == true) {
                holder.imgSelectFilter.visibility = View.GONE
                suppliersList!![position]?.isSupplierSelected = false
                listener.onFilterDeselected()
            } else {
                holder.imgSelectFilter.visibility = View.VISIBLE
                suppliersList!![position]?.isSupplierSelected = true
                listener.onFilterSelected()
            }
        }
        if (suppliersList!![position]?.isSupplierSelected == true) {
            holder.imgSelectFilter.visibility = View.VISIBLE
        } else {
            holder.imgSelectFilter.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return suppliersList!!.size
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
        fun onFilterSelected()
        fun onFilterDeselected()
    }

    companion object {
        var suppliersList: List<DetailSupplierDataModel>? = null
        fun resetFilterInvoiceList() {
            if (suppliersList != null) {
                for (i in suppliersList!!.indices) {
                    suppliersList!![i].isSupplierSelected = false
                }
            }
        }
    }
}