package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.modelsimport.OutletSuppliersResponseForOrderSettings

/**
 * Created by RajPrudhviMarella on 24/Aug/2021.
 */
class OutletListAdapterForOrderSettings(
    lstOutlets: List<OutletSuppliersResponseForOrderSettings.Datum>,
    context: Context,
    mListener: onOutletSelected
) : RecyclerView.Adapter<OutletListAdapterForOrderSettings.ViewHolder?>() {
    private val lstOutlets: List<OutletSuppliersResponseForOrderSettings.Datum>
    var context: Context
    var mListener: onOutletSelected

    init {
        this.lstOutlets = lstOutlets
        this.context = context
        this.mListener = mListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.lyt_order_settings_outlet_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtOutletName.setText(lstOutlets[position].outlet?.outletName)
        if (lstOutlets[position].suppliers != null && lstOutlets[position].suppliers
                ?.size!! > 0
        ) {
            holder.txtReviewCount.setText(
                java.lang.String.format(
                    context.getString(R.string.txt_to_review),
                    lstOutlets[position].suppliers?.size
                )
            )
            holder.lstSuppliers.setAdapter(
                SupplierListAdapterForOrderSettings(
                    context,
                    lstOutlets[position].suppliers!!,
                    lstOutlets[position].outlet!!,
                    object : SupplierListAdapterForOrderSettings.onSupplierSelected {
                        override fun onSupplierClicked(supplier: Supplier?, outlet: Outlet?) {
                            mListener.onOutletClicked(supplier, outlet)
                        }
                    })
            )
        } else {
            holder.txtReviewCount.setText(
                String.format(
                    context.getString(R.string.txt_to_review),
                    0
                )
            )
        }
        if (lstOutlets[position].isDisplaySuppliersList) {
            holder.lstSuppliers.setVisibility(View.VISIBLE)
        } else {
            holder.lstSuppliers.setVisibility(View.GONE)
        }
        holder.txtReviewCount.setOnClickListener(View.OnClickListener {
            for (i in lstOutlets.indices) {
                lstOutlets[i].isDisplaySuppliersList
            }
            lstOutlets[position].isDisplaySuppliersList
            notifyDataSetChanged()
        })
    }

    override fun getItemCount(): Int {
        return lstOutlets.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtOutletName: TextView
        var txtReviewCount: TextView
        var lstSuppliers: RecyclerView

        init {
            txtOutletName = itemView.findViewById<TextView>(R.id.txt_outlet_name)
            txtReviewCount = itemView.findViewById<TextView>(R.id.txt_review_count)
            lstSuppliers = itemView.findViewById<RecyclerView>(R.id.lst_suppliers)
            lstSuppliers.setLayoutManager(LinearLayoutManager(context))
            ZeemartBuyerApp.setTypefaceView(
                txtOutletName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtReviewCount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    interface onOutletSelected {
        fun onOutletClicked(supplier: Supplier?, outlet: Outlet?)
    }
}