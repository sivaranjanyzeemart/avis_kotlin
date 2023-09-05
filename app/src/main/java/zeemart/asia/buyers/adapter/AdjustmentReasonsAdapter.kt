package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.AdjustmentReasonsAdapter.ViewHolderAdjustmentReason
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.inventory.AdjustmentReasons.AdjustmentReasonSelected

class AdjustmentReasonsAdapter(
    private val context: Context,
    private val adjustmentReasonSelectedList: List<AdjustmentReasonSelected>,
    private val listener: AdjustmentReasonSelectedListener
) : RecyclerView.Adapter<ViewHolderAdjustmentReason>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAdjustmentReason {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_reason_adjustment_val, parent, false)
        return ViewHolderAdjustmentReason(v)
    }

    override fun onBindViewHolder(holder: ViewHolderAdjustmentReason, position: Int) {
        holder.txtAdjustmentReason.text =
            adjustmentReasonSelectedList[position].adjustmentReasons?.resId?.let {
                context.resources.getString(
                    it
                )
            }
        holder.txtAdjustmentReason.setOnClickListener {
            for (i in adjustmentReasonSelectedList.indices) {
                adjustmentReasonSelectedList[i].isSelected = false
            }
            notifyDataSetChanged()
            if (adjustmentReasonSelectedList[position].isSelected) {
                adjustmentReasonSelectedList[position].isSelected = false
                holder.txtAdjustmentReason.setBackgroundResource(R.drawable.faintgrey_solid_round_corner)
                holder.txtAdjustmentReason.setTextColor(context.resources.getColor(R.color.black))
            } else {
                adjustmentReasonSelectedList[position].isSelected = true
                holder.txtAdjustmentReason.setBackgroundResource(R.drawable.blue_solid_round_corner)
                holder.txtAdjustmentReason.setTextColor(context.resources.getColor(R.color.white))
            }
            listener.onAdjustmentSelected(adjustmentReasonSelectedList[position])
        }
        if (adjustmentReasonSelectedList[position].isSelected) {
            holder.txtAdjustmentReason.setBackgroundResource(R.drawable.blue_solid_round_corner)
            holder.txtAdjustmentReason.setTextColor(context.resources.getColor(R.color.white))
        } else {
            holder.txtAdjustmentReason.setBackgroundResource(R.drawable.faintgrey_solid_round_corner)
            holder.txtAdjustmentReason.setTextColor(context.resources.getColor(R.color.black))
        }
    }

    override fun getItemCount(): Int {
        return adjustmentReasonSelectedList.size
    }

    interface AdjustmentReasonSelectedListener {
        fun onAdjustmentSelected(adjustmentReasonSelected: AdjustmentReasonSelected?)
    }

    inner class ViewHolderAdjustmentReason(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtAdjustmentReason: TextView

        init {
            txtAdjustmentReason = itemView.findViewById(R.id.txt_adjustment_reason_value)
            ZeemartBuyerApp.setTypefaceView(
                txtAdjustmentReason,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }
}