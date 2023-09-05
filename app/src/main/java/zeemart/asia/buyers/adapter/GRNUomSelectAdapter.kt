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
import zeemart.asia.buyers.models.UnitSizeModel

/**
 * Created by RajPrudhviMarella on 15/June/2021.
 */
class GRNUomSelectAdapter(
    private val context: Context,
    private val lstUnitSizeModel: List<UnitSizeModel>,
    private val listener: SelectedUOMListener
) : RecyclerView.Adapter<GRNUomSelectAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_filter_order_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtlstFilterName.text = lstUnitSizeModel[position].unitSize
        holder.layoutFilterOrderRow.setOnClickListener {
            for (i in lstUnitSizeModel.indices) {
                lstUnitSizeModel[i].isSelected = false
            }
            holder.imgSelectFilter.visibility = View.VISIBLE
            lstUnitSizeModel[position].isSelected = true
            listener.onSelectedUom(lstUnitSizeModel[position].unitSize)
        }
        if (lstUnitSizeModel[position].isSelected) {
            holder.imgSelectFilter.visibility = View.VISIBLE
        } else {
            holder.imgSelectFilter.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return lstUnitSizeModel.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutFilterOrderRow: RelativeLayout
        var txtlstFilterName: TextView
        var imgSelectFilter: ImageView

        init {
            txtlstFilterName = itemView.findViewById(R.id.txt_filter_name)
            setTypefaceView(txtlstFilterName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            imgSelectFilter = itemView.findViewById(R.id.img_select_filter)
            layoutFilterOrderRow = itemView.findViewById(R.id.lyt_filter_order_row)
        }
    }

    interface SelectedUOMListener {
        fun onSelectedUom(unitSize: String?)
    }
}