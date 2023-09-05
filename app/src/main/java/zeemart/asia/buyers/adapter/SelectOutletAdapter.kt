package zeemart.asia.buyers.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.AnalyticsHelper.logAction
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.SharedPref.write
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.OutletMgr
import zeemart.asia.buyers.network.InvoiceHelper.setBottomNavigationRejectedInvoiceCount
import zeemart.asia.buyers.orders.createorders.BrowseCreateNewOrder
import zeemart.asia.buyers.orders.createorders.RepeatOrderActivity
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity

/**
 * Created by ParulBhandari on 12/5/2017.
 */
class SelectOutletAdapter(
    private val context: Context,
    lstOutletNames: List<OutletMgr>,
    calledFrom: String?,
) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val lstOutletNames: List<OutletMgr>
    private val calledFrom: String?

    init {
        this.lstOutletNames = lstOutletNames
        this.calledFrom = calledFrom
    }

    override fun getCount(): Int {
        return lstOutletNames.size
    }

    override fun getItem(position: Int): Any {
        return lstOutletNames[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            convertView = inflater.inflate(R.layout.select_outlet_list_row, parent, false)
            holder.txtOutletName = convertView.findViewById(R.id.txt_outlet_name)
            holder.btnItemSelected = convertView.findViewById(R.id.btn_mark_tick)
            holder.lytParentLayout = convertView.findViewById(R.id.lyt_select_outlet_parent)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.txtOutletName.text = lstOutletNames[position].outletName
        convertView!!.setOnClickListener {
            val outletMgr = lstOutletNames[position]
            val outlet = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                ZeemartBuyerApp.gsonExposeExclusive.toJson(outletMgr),
                Outlet::class.java
            )
            logAction(context, AnalyticsHelper.TAP_ITEM_CREATE_ORDER_OUTLET, outlet)
            for (i in lstOutletNames.indices) {
                if (i == position) {
                    lstOutletNames[i].isOutletSelected = true
                } else {
                    lstOutletNames[i].isOutletSelected = false
                }
            }
            holder.lytParentLayout!!.setBackgroundResource(R.color.white)
            holder.btnItemSelected!!.visibility = View.VISIBLE
            notifyDataSetChanged()
            write(SharedPref.SELECTED_OUTLET_NAME, lstOutletNames[position].outletName)
            write(SharedPref.SELECTED_OUTLET_ID, lstOutletNames[position].outletId)
            if (calledFrom != null && calledFrom == ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_REPEAT_ORDER!!) {
                (context as SelectOutletActivity).finish()
                setBottomNavigationRejectedInvoiceCount(context)
            } else if (calledFrom != null && calledFrom == ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_NEW_ORDER) {
                (context as SelectOutletActivity).finish()
                setBottomNavigationRejectedInvoiceCount(context)
            } else if (calledFrom != null && calledFrom == ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_DASH_BOARD_FOR_BROWSE_SCREEN) {
                val browseForNewOrderIntent = Intent(context, BrowseCreateNewOrder::class.java)
                context.startActivity(browseForNewOrderIntent)
                (context as SelectOutletActivity).finish()
                setBottomNavigationRejectedInvoiceCount(context)
            } else if (calledFrom != null && calledFrom == ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_DASH_BOARD_FOR_REPEAT_ORDER) {
                val newIntent = Intent(context, RepeatOrderActivity::class.java)
                context.startActivity(newIntent)
                (context as SelectOutletActivity).finish()
                setBottomNavigationRejectedInvoiceCount(context)
            } else if (calledFrom != null && calledFrom == ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_INVOICE!!) {
                val intent = Intent()
                (context as SelectOutletActivity).setResult(Activity.RESULT_OK, intent)
                context.finish()
            } else {
                (context as SelectOutletActivity).finish()
                setBottomNavigationRejectedInvoiceCount(context)
            }
        }
        if (lstOutletNames[position].isOutletSelected) {
            holder.lytParentLayout!!.setBackgroundResource(R.color.white)
            holder.btnItemSelected!!.visibility = View.VISIBLE
        } else {
            holder.lytParentLayout!!.setBackgroundResource(R.color.opacity25)
            holder.btnItemSelected!!.visibility = View.GONE
        }
        setTypefaceView(holder.txtOutletName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        return convertView
    }

    internal inner class ViewHolder {
        lateinit var txtOutletName: TextView
        var btnItemSelected: ImageButton? = null
        var lytParentLayout: RelativeLayout? = null
    }
}