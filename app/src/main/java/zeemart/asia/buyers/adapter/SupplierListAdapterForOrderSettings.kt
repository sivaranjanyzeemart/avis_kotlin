package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier

/**
 * Created by RajPrudhviMarella on 24/Aug/2021.
 */
class SupplierListAdapterForOrderSettings(
    var context: Context,
    private val lstSuppliers: List<Supplier>,
    private val outlet: Outlet,
    var mListener: onSupplierSelected
) : RecyclerView.Adapter<SupplierListAdapterForOrderSettings.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lyt_order_settings_supplier_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtSupplierName.text = lstSuppliers[position].supplierName
        if (StringHelper.isStringNullOrEmpty(lstSuppliers[position].logoURL)) {
            holder.imgSupplier.visibility = View.INVISIBLE
            holder.lytSupplierThumbNail.visibility = View.VISIBLE
            holder.lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    lstSuppliers[position].supplierName!!, context
                )
            )
            holder.txtSupplierThumbNail.text = CommonMethods.SupplierThumbNailShortCutText(
                lstSuppliers[position].supplierName!!
            )
            holder.txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    lstSuppliers[position].supplierName!!, context
                )
            )
        } else {
            holder.imgSupplier.visibility = View.VISIBLE
            holder.lytSupplierThumbNail.visibility = View.GONE
            Picasso.get().load(lstSuppliers[position].logoURL)
                .placeholder(R.drawable.placeholder_all).resize(
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
            ).into(holder.imgSupplier)
        }
        holder.lytSupplierList.setOnClickListener {
            mListener.onSupplierClicked(
                lstSuppliers[position], outlet
            )
        }
    }

    override fun getItemCount(): Int {
        return lstSuppliers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var lytSupplierList: RelativeLayout
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.img_search_supplier)
            txtSupplierName = itemView.findViewById(R.id.txt_supplier_name_report_search)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            lytSupplierList = itemView.findViewById(R.id.layout_supplier_list_search)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }

        companion object {
            val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH = CommonMethods.dpToPx(36)
            val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT = CommonMethods.dpToPx(36)
        }
    }

    interface onSupplierSelected {
        fun onSupplierClicked(supplier: Supplier?, outlet: Outlet?)
    }
}