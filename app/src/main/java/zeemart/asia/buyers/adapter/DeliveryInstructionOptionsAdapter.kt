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
import zeemart.asia.buyers.models.OutletSettingModel.DeliveryInstructionIsSelected

class DeliveryInstructionOptionsAdapter(
    private val context: Context,
    private val deliveryInstructionIsSelectedList: List<DeliveryInstructionIsSelected>,
    private val listener: DeliveryOptionTappedListener
) : RecyclerView.Adapter<DeliveryInstructionOptionsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_delivery_instruction_options,
            parent,
            false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtDeliveryInstructionName.text =
            deliveryInstructionIsSelectedList[position].deliveryInstruction
        holder.itemView.setOnClickListener {
            if (deliveryInstructionIsSelectedList[position].isSelected) {
                //holder.imgSelectFilter.setVisibility(View.GONE);
                for (i in deliveryInstructionIsSelectedList.indices) {
                    deliveryInstructionIsSelectedList[i].isSelected = false
                }
            } else {
                //holder.imgSelectFilter.setVisibility(View.VISIBLE);
                for (i in deliveryInstructionIsSelectedList.indices) {
                    deliveryInstructionIsSelectedList[i].isSelected = i == position
                }
            }
            notifyDataSetChanged()
            listener.onDeliveryOptionTapped()
        }
        if (deliveryInstructionIsSelectedList[position].isSelected) holder.imgSelectFilter.visibility =
            View.VISIBLE else holder.imgSelectFilter.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return deliveryInstructionIsSelectedList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutFilterOrderRow: RelativeLayout
        var txtDeliveryInstructionName: TextView
        var imgSelectFilter: ImageView

        init {
            txtDeliveryInstructionName = itemView.findViewById(R.id.txt_delivery_instruction_name)
            setTypefaceView(
                txtDeliveryInstructionName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            imgSelectFilter = itemView.findViewById(R.id.img_select_filter)
            layoutFilterOrderRow = itemView.findViewById(R.id.lyt_filter_order_row)
        }
    }

    interface DeliveryOptionTappedListener {
        fun onDeliveryOptionTapped()
    }
}