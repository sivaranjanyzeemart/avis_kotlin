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
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.interfaces.DeliveryDateSelectedListener
import zeemart.asia.buyers.models.marketlist.DeliveryDate
import java.util.*

/**
 * Created by ParulBhandari on 1/11/2018.
 */
class DeliveryDatesHorizontalListAdapter(
    private val context: Context,
    var timeZone: TimeZone,
    private val dateDataList: List<DeliveryDate>,
    var listener: DeliveryDateSelectedListener
) : RecyclerView.Adapter<DeliveryDatesHorizontalListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_delivery_date_box, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cutOffTime =
            DateHelper.getDateInDateMonthHourMinute(dateDataList[position].cutOffDate, timeZone)
        val deliveryDate =
            DateHelper.getDateInDateMonthFormat(dateDataList[position].deliveryDate, timeZone)
        holder.txtDay.text =
            DateHelper.getDateInFullDayFormat(dateDataList[position].deliveryDate, timeZone)
        val cutOffTimeText =
            String.format(context.resources.getString(R.string.deliver_until_cutoff), cutOffTime)
        holder.txtcutOffTime.text = cutOffTimeText
        holder.txtDate.text = deliveryDate
        holder.txtHolidayIndicator.visibility = View.INVISIBLE
        if (dateDataList[position].isPH) {
            holder.txtHolidayIndicator.setText(R.string.holiday_ph)
            holder.txtHolidayIndicator.setTextColor(context.resources.getColor(R.color.pinky_red))
            holder.txtHolidayIndicator.visibility = View.VISIBLE
            holder.txtcutOffTime.text = cutOffTimeText
            holder.txtDate.text = "$deliveryDate*"
        } else if (dateDataList[position].isEvePH) {
            holder.txtHolidayIndicator.setText(R.string.holiday_eve)
            holder.txtHolidayIndicator.setTextColor(context.resources.getColor(R.color.chart_yellow))
            holder.txtHolidayIndicator.visibility = View.VISIBLE
            holder.txtcutOffTime.text = cutOffTimeText
            holder.txtDate.text = "$deliveryDate*"
        }
        if (dateDataList[position].dateSelected) {
            holder.lytSelectedDateLayout.setBackgroundColor(context.resources.getColor(R.color.text_blue))
            holder.imgTick.visibility = View.VISIBLE
            holder.txtDay.setTextColor(context.resources.getColor(R.color.white))
            holder.txtDate.setTextColor(context.resources.getColor(R.color.white))
        } else {
            holder.lytSelectedDateLayout.setBackgroundColor(context.resources.getColor(R.color.faint_grey))
            holder.imgTick.visibility = View.GONE
            holder.txtDay.setTextColor(context.resources.getColor(R.color.grey_medium))
            holder.txtDate.setTextColor(context.resources.getColor(R.color.text_blue))
        }
        holder.lytSelectedDateLayout.setOnClickListener {
            if (!dateDataList[position].dateSelected) {
                dateDataList[position].dateSelected = true
                holder.lytSelectedDateLayout.setBackgroundColor(context.resources.getColor(R.color.text_blue))
                holder.imgTick.visibility = View.VISIBLE
                for (i in dateDataList.indices) {
                    if (i != position) {
                        dateDataList[i].dateSelected = false
                    }
                }
            }
            //call the callback to set the selected Date on the adapter
            try {
                listener.deliveryDateSelected(
                    DateHelper.getDateInDayDateMonthYearFormat(
                        dateDataList[position].deliveryDate!!, timeZone
                    ), dateDataList[position].deliveryDate
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return dateDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDay: TextView
        var txtDate: TextView
        var txtHolidayIndicator: TextView
        var txtcutOffTime: TextView
        var imgTick: ImageView
        var lytSelectedDateLayout: RelativeLayout

        init {
            txtDay = itemView.findViewById(R.id.txt_day_delivery_date)
            setTypefaceView(txtDay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            txtDate = itemView.findViewById(R.id.txt_date_delivery_date)
            setTypefaceView(txtDate, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtHolidayIndicator = itemView.findViewById(R.id.txt_holiday_indicator)
            setTypefaceView(
                txtHolidayIndicator,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtcutOffTime = itemView.findViewById(R.id.txt_deliveryDate_cut_off_time)
            setTypefaceView(txtcutOffTime, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            imgTick = itemView.findViewById(R.id.img_unit_selected)
            lytSelectedDateLayout = itemView.findViewById(R.id.lyt_selected_date_blue_box)
        }
    }
}