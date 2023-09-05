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
import zeemart.asia.buyers.models.StockageType.StockageTypeFilter
import zeemart.asia.buyers.models.inventory.Shelve.ShelveFilter

/**
 * Created by ParulBhandari on 13/3/2019.
 */
class FilterInventoryAdapter(
    private var context: Context,
    private var calledFrom: String,
    passedList: List<ShelveFilter>?, passedListStock: List<StockageTypeFilter>?,
) : RecyclerView.Adapter<FilterInventoryAdapter.ViewHolder>() {
     lateinit var shelveList: List<ShelveFilter>
     lateinit var stockTypeList: List<StockageTypeFilter>

    init {
        if (calledFrom == ZeemartAppConstants.SHELVE_FILTER) {
            shelveList = passedList!!
        } else if (calledFrom == ZeemartAppConstants.STOCK_TYPE_FILTER) {
            stockTypeList = passedListStock!!
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_filter_order_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (calledFrom == ZeemartAppConstants.SHELVE_FILTER) {
            holder.txtlstFilterName.text = shelveList!![position].shelveName
            holder.layoutFilterOrderRow.setOnClickListener {
                if (shelveList!![position].isShelveSelected) {
                    holder.imgSelectFilter.visibility = View.GONE
                    shelveList!![position].isShelveSelected = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    shelveList!![position].isShelveSelected = true
                }
            }
            if (shelveList!![position].isShelveSelected) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        } else if (calledFrom == ZeemartAppConstants.STOCK_TYPE_FILTER) {
            holder.txtlstFilterName.text =
                context.getString(stockTypeList!![position].stockageType?.resId!!)
            holder.layoutFilterOrderRow.setOnClickListener {
                if (stockTypeList!![position].isStockageTypeSelected) {
                    holder.imgSelectFilter.visibility = View.GONE
                    stockTypeList!![position].isStockageTypeSelected = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    stockTypeList!![position].isStockageTypeSelected = true
                }
            }
            if (stockTypeList!![position].isStockageTypeSelected) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return if (calledFrom == ZeemartAppConstants.SHELVE_FILTER) shelveList!!.size else if (calledFrom == ZeemartAppConstants.STOCK_TYPE_FILTER) stockTypeList!!.size else 0
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
}