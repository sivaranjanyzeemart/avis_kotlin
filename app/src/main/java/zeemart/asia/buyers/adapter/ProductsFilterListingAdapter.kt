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
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.models.OutletTags

/**
 * Created by Rajprudhvi Marella on 07/09/2018.
 */
class ProductsFilterListingAdapter(
    var mListner: ProductListSelectedFilterItemClickListner,
    var context: Context,
    var outletTags: List<OutletTags>
) : RecyclerView.Adapter<ProductsFilterListingAdapter.ViewHolder>() {
    var localOutletTags: List<OutletTags>

    init {
        localOutletTags = outletTags
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lst_product_filter_items, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.categoryName.text = outletTags[position].categoryName
        setTypefaceView(holder.categoryName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        holder.lyt_filter_order_row.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_ADD_TO_ORDER_FILTER)
            if (localOutletTags[position].isCategorySelected) {
                holder.categoryName.setTextColor(context.resources.getColor(R.color.black))
                holder.img_select_filter_tag.visibility = View.GONE
                localOutletTags[position].isCategorySelected = false
                mListner.onFilterDeselected(localOutletTags[position], null)
            } else {
                holder.categoryName.setTextColor(context.resources.getColor(R.color.black))
                holder.img_select_filter_tag.visibility = View.VISIBLE
                localOutletTags[position].isCategorySelected = true
                mListner.onFilterSelected(localOutletTags[position], null)
            }
        }
        if (localOutletTags[position].isCategorySelected) {
            holder.img_select_filter_tag.visibility = View.VISIBLE
        } else {
            holder.img_select_filter_tag.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return outletTags.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView
        val lyt_filter_order_row: RelativeLayout
        val img_select_filter_tag: ImageView

        init {
            categoryName = itemView.findViewById(R.id.txt_product_tag)
            lyt_filter_order_row = itemView.findViewById(R.id.lyt_filter_order_row)
            img_select_filter_tag = itemView.findViewById(R.id.img_select_filter_tag)
            setTypefaceView(categoryName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    interface ProductListSelectedFilterItemClickListner {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
    }

    interface ProductListSelectedFilterCategoriesItemClickListener {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
        fun onSavePressed()
        fun onResetPressed()
        fun onClear()
    }

    interface ProductListSelectedFilterCategoriesItemClickListeneremptydata {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
    }

    interface ProductListSelectedFilterCertificationItemClickListener {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
    }

    interface ProductListSelectedFilterSupplierItemClickListener {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
    }
}