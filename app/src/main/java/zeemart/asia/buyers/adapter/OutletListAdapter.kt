package zeemart.asia.buyers.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.Outlet

class OutletListAdapter(private val context: Context, private val outlets: List<Outlet>) :
    RecyclerView.Adapter<OutletListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_outlet_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("AAAAA", outlets[position].toString())
        holder.txtOutletName.text = outlets[position].outletName
    }

    override fun getItemCount(): Int {
        return outlets.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtOutletName: TextView

        init {
            txtOutletName = itemView.findViewById(R.id.txt_outlet_name)
            setTypefaceView(txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }
}