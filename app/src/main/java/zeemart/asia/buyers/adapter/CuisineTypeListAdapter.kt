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
import zeemart.asia.buyers.models.OutletFuturesModel

/**
 * Created by RajPrudhviMarella on 04/Nov/2020.
 */
class CuisineTypeListAdapter(
    private val context: Context,
    private val lstCuisineType: List<OutletFuturesModel.CuisineType>,
    private val listener: SelectedCuisineTypeFiltersListener
) : RecyclerView.Adapter<CuisineTypeListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lyt_cousine_type_filter, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtCuisineType.text = lstCuisineType[position].name
        holder.lytRow.setOnClickListener {
            if (lstCuisineType[position].isSelected) {
                holder.imgSelectFilter.visibility = View.GONE
                lstCuisineType[position].isSelected = false
                listener.onFilterDeselected(lstCuisineType[position])
            } else {
                holder.imgSelectFilter.visibility = View.VISIBLE
                lstCuisineType[position].isSelected = true
                listener.onFilterSelected(lstCuisineType[position])
            }
        }
        if (lstCuisineType[position].isSelected) {
            holder.imgSelectFilter.visibility = View.VISIBLE
        } else {
            holder.imgSelectFilter.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return lstCuisineType.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCuisineType: TextView
        val imgSelectFilter: ImageView
        val lytRow: RelativeLayout

        init {
            txtCuisineType = itemView.findViewById(R.id.txt_filter_name)
            imgSelectFilter = itemView.findViewById(R.id.img_select_filter)
            lytRow = itemView.findViewById(R.id.lyt_row_cuisine_type)
            setTypefaceView(txtCuisineType, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    interface SelectedCuisineTypeFiltersListener {
        fun onFilterSelected(cuisineType: OutletFuturesModel.CuisineType?)
        fun onFilterDeselected(cuisineType: OutletFuturesModel.CuisineType?)
    }
}