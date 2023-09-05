package zeemart.asia.buyers.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DialogHelper.ShowEditDeliverySettings
import zeemart.asia.buyers.helper.DialogHelper.onEditDeliverySettingsListener
import zeemart.asia.buyers.interfaces.DeliveryDateSelectedListener
import zeemart.asia.buyers.modelsimport.OrderSettingDeliveryPreferences
import java.util.*

/**
 * Created by RajPrudhviMarella on 23/Aug/2021.
 */
class DeliveryDatesForOrderSettingsAdapter(
    private val context: Context,
    var isFromInfo: Boolean,
    private val dateDataList: List<OrderSettingDeliveryPreferences.CutOffTimes>,
    var listener: DeliveryDateSelectedListener
) : RecyclerView.Adapter<DeliveryDatesForOrderSettingsAdapter.ViewHolder>() {
    var timeZone: TimeZone? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.lyt_delivery_date_order_settings_row,
            parent,
            false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.txtDay.text = dateDataList[position].deliverOn
        if (dateDataList[position].status == "A") {
            var cutOffTimeText = ""
            cutOffTimeText = if (dateDataList[position].cutoffTime?.days!! > 1) {
                String.format(
                    context.getString(R.string.txt_days_earlier),
                    dateDataList[position].cutoffTime?.days
                )
            } else {
                String.format(
                    context.getString(R.string.txt_day_earlier),
                    dateDataList[position].cutoffTime?.days
                )
            }
            holder.txtTime.text = dateDataList[position].cutoffTime?.time
            holder.txtTime.visibility = View.VISIBLE
            holder.txtcutOffTime.text = cutOffTimeText
            holder.imgTick.setImageDrawable(context.resources.getDrawable(R.drawable.icon_green_tick))
        } else {
            holder.txtcutOffTime.text = context.resources.getString(R.string.txt_no_deliveries)
            holder.txtTime.visibility = View.INVISIBLE
            holder.imgTick.setImageDrawable(context.resources.getDrawable(R.drawable.icon_close_red))
        }
        holder.lytSelectedDateLayout.setOnClickListener {
            if (!isFromInfo) ShowEditDeliverySettings(
                context,
                dateDataList[position],
                object : onEditDeliverySettingsListener {
                    override fun onEditedDeliverySettings(
                        status: String?,
                        cutoffTime: OrderSettingDeliveryPreferences.CutoffTime?
                    ) {
                        dateDataList[position].status = status
                        dateDataList[position].cutoffTime = cutoffTime
                        notifyDataSetChanged()
                    }
                })
        }
        val paddingDp = 3
        val density = context.resources.displayMetrics.density
        val paddingPixel = (paddingDp * density).toInt()
        val tenPaddingDp = 20
        val Density = context.resources.displayMetrics.density
        val paddingPixelTen = (tenPaddingDp * Density).toInt()
        if (position == 0) {
            setMargins(holder.lytSelectedDateLayout, paddingPixelTen, 0, paddingPixel, 0)
        } else if (position == dateDataList.size - 1) {
            setMargins(holder.lytSelectedDateLayout, paddingPixel, 0, paddingPixelTen, 0)
        } else {
            setMargins(holder.lytSelectedDateLayout, paddingPixel, 0, paddingPixel, 0)
        }
    }

    private fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    override fun getItemCount(): Int {
        return dateDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDay: TextView
        var txtcutOffTime: TextView
        var lytSelectedDateLayout: RelativeLayout
        var imgTick: ImageView
        var txtTime: TextView

        init {
            txtDay = itemView.findViewById(R.id.txt_day_delivery_date)
            imgTick = itemView.findViewById(R.id.img_tick)
            txtTime = itemView.findViewById(R.id.txt_time)
            ZeemartBuyerApp.setTypefaceView(
                txtDay,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtTime,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtcutOffTime = itemView.findViewById(R.id.txt_deliveryDate_cut_off_time)
            ZeemartBuyerApp.setTypefaceView(
                txtcutOffTime,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            lytSelectedDateLayout = itemView.findViewById(R.id.lyt_row)
        }
    }
}