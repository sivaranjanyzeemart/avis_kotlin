package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.InvoiceDetailActivity
import zeemart.asia.buyers.models.orderimportimport.OrderHistoryModelBaseResponse
import java.util.*

/**
 * Created by ParulBhandari on 9/4/2018.
 */
class OrderActivityLogAdapter(
    private val context: Context,
    var timeZone: TimeZone,
    var orderHistoryDataList: List<OrderHistoryModelBaseResponse.OrderHistoryModel>
) : RecyclerView.Adapter<OrderActivityLogAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_activity_history_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Sets the UI for the indicating line and activity indicating pointer
        //if its the first position
        if (position == 0) {
            if (orderHistoryDataList.size == 1) {
                holder.txtIndicatingLine.visibility = View.INVISIBLE
            } else {
                holder.txtIndicatingLine.visibility = View.VISIBLE
            }
            holder.divider.visibility = View.VISIBLE
            val constrainSet = ConstraintSet()
            constrainSet.clone(holder.layoutRow)
            constrainSet.connect(
                holder.txtIndicatingLine.id,
                ConstraintSet.BOTTOM,
                holder.layoutRow.id,
                ConstraintSet.BOTTOM
            )
            constrainSet.connect(
                holder.txtIndicatingLine.id,
                ConstraintSet.TOP,
                holder.orderStatusIndicator.id,
                ConstraintSet.BOTTOM
            )
            holder.orderStatusIndicator.setImageResource(R.drawable.blue_circle)
            constrainSet.setMargin(
                holder.orderStatusIndicator.id,
                ConstraintSet.START,
                ViewHolder.MARGIN_START_FOR_IMAGE_ORDER_INDICATOR
            )
            constrainSet.applyTo(holder.layoutRow)
            val pulse = AnimationUtils.loadAnimation(context, R.anim.pulse)
            holder.orderStatusIndicator.startAnimation(pulse)
        } else if (position == orderHistoryDataList.size - 1) {
            //if its the last position
            holder.divider.visibility = View.GONE
            val constrainSet = ConstraintSet()
            constrainSet.clone(holder.layoutRow)
            constrainSet.connect(
                holder.txtIndicatingLine.id,
                ConstraintSet.BOTTOM,
                holder.orderStatusIndicator.id,
                ConstraintSet.BOTTOM
            )
            constrainSet.connect(
                holder.txtIndicatingLine.id,
                ConstraintSet.TOP,
                holder.layoutRow.id,
                ConstraintSet.TOP
            )
            holder.orderStatusIndicator.setImageResource(R.drawable.grey_circle)
            constrainSet.setMargin(
                holder.orderStatusIndicator.id,
                ConstraintSet.START,
                ViewHolder.MARGIN_START_FOR_IMAGE_ORDER_STATUS_INDICATOR
            )
            constrainSet.applyTo(holder.layoutRow)
            holder.orderStatusIndicator.clearAnimation()
        } else {
            holder.divider.visibility = View.GONE
            val constrainSet = ConstraintSet()
            constrainSet.clone(holder.layoutRow)
            constrainSet.connect(
                holder.txtIndicatingLine.id,
                ConstraintSet.BOTTOM,
                holder.layoutRow.id,
                ConstraintSet.BOTTOM
            )
            constrainSet.connect(
                holder.txtIndicatingLine.id,
                ConstraintSet.TOP,
                holder.layoutRow.id,
                ConstraintSet.TOP
            )
            constrainSet.setMargin(
                holder.orderStatusIndicator.id,
                ConstraintSet.START,
                ViewHolder.MARGIN_START_FOR_IMAGE_ORDER_STATUS_INDICATOR
            )
            holder.orderStatusIndicator.setImageResource(R.drawable.grey_circle)
            constrainSet.applyTo(holder.layoutRow)
            holder.orderStatusIndicator.clearAnimation()
        }
        if (orderHistoryDataList[position].isOrderHistoryActivityName(OrderHistoryModelBaseResponse.OrderHistoryModel.OrderActivityName.ORDER_LINKED_TO_INVOICE) || orderHistoryDataList[position].isOrderHistoryActivityName(
                OrderHistoryModelBaseResponse.OrderHistoryModel.OrderActivityName.LINKED_INVOICE_VOIDED
            )
        ) {
            val builder = SpannableStringBuilder()
            val activityMessage =
                SpannableString(orderHistoryDataList[position].activityMessage + " ")
            builder.append(activityMessage)
            if (!StringHelper.isStringNullOrEmpty(orderHistoryDataList[position].activityAction?.invoiceNum)) {
                val invoiceNumber =
                    SpannableString(context.getString(R.string.hash) + orderHistoryDataList[position].activityAction?.invoiceNum)
                val color = context.resources.getColor(R.color.text_blue)
                invoiceNumber.setSpan(ForegroundColorSpan(color), 0, invoiceNumber.length, 0)
                builder.append(invoiceNumber)
                val invoiceId = orderHistoryDataList[position].activityAction?.invoiceId
                holder.lytItemRow.setOnClickListener {
                    val newIntent = Intent(context, InvoiceDetailActivity::class.java)
                    newIntent.putExtra(ZeemartAppConstants.INVOICE_ID, invoiceId)
                    context.startActivity(newIntent)
                }
            }
            holder.txtOrderStatus.setText(builder, TextView.BufferType.SPANNABLE)
        } else {
            holder.txtOrderStatus.text = orderHistoryDataList[position].activityMessage
        }
        if (orderHistoryDataList[position].timeActivity != null) {
            //set the status updated day
            val dateCreated = orderHistoryDataList[position].timeActivity
            val dateCreatedDate = dateCreated?.times(1000)
            val now = Calendar.getInstance(DateHelper.marketTimeZone)
            val days = DateHelper.getDaysBetweenDates(dateCreatedDate!!, now.timeInMillis)
            if (days == 0) {
                holder.txtDay.text = context.getString(R.string.txt_today)
            } else {
                if (days == 1) {
                    val updatedTimeText = context.getString(R.string.txt_yesterday)
                    holder.txtDay.text = updatedTimeText
                } else if (days <= 7 && days > 1) { // if (days <= 7 && days > 1) {
                    val updatedTimeText =
                        days.toString() + " " + context.resources.getString(R.string.txt_days_ago)
                    holder.txtDay.text = updatedTimeText
                } else {
                    //show the entire date
                    val dateDay = DateHelper.getDateInFullDayDateMonthYearFormat(
                        orderHistoryDataList[position].timeActivity!!, timeZone
                    )
                    holder.txtDay.text = dateDay
                }
            }
        }
        //set the updater name
        if (orderHistoryDataList[position].activityUser != null) {
            holder.txtOrderUpdatedBy.visibility = View.VISIBLE
            val firstName = orderHistoryDataList[position].activityUser?.firstName
            val lastName = orderHistoryDataList[position].activityUser?.lastName
            holder.txtOrderUpdatedBy.text = "$firstName $lastName"
            if (!StringHelper.isStringNullOrEmpty(orderHistoryDataList[position].activityUser?.imageURL)) {
                holder.orderStatusUpdatedByUserImage.visibility = View.VISIBLE
                Picasso.get().load(orderHistoryDataList[position].activityUser?.imageURL)
                    .placeholder(R.drawable.placeholder_user)
                    .resize(
                        ViewHolder.PLACE_HOLDER_USER_IMAGE_WIDTH,
                        ViewHolder.PLACE_HOLDER_USER_IMAGE_HEIGHT
                    )
                    .centerCrop()
                    .transform(CropCircleTransformation())
                    .into(holder.orderStatusUpdatedByUserImage)
            } else {
                holder.orderStatusUpdatedByUserImage.visibility = View.GONE
                /*Picasso.with(context).load(R.drawable.placeholder_user)
                        .resize(CommonMethods.dpToPx(30), CommonMethods.dpToPx(30))
                        .centerCrop()
                        .transform(new CropCircleTransformation())
                        .into(holder.orderStatusUpdatedByUserImage);*/
            }
        } else {
            holder.txtOrderUpdatedBy.visibility = View.GONE
            holder.orderStatusUpdatedByUserImage.visibility = View.GONE
        }
        if (StringHelper.isStringNullOrEmpty(orderHistoryDataList[position].activityRemark)) {
            holder.txtMessage.visibility = View.GONE
        } else {
            holder.txtMessage.visibility = View.VISIBLE
            holder.txtMessage.text = orderHistoryDataList[position].activityRemark
        }
        //set time 24 hour format
        val time =
            DateHelper.getDateInHourMinFormat(orderHistoryDataList[position].timeActivity, timeZone)
        holder.txtTime.text = time
        setFont(holder)
    }

    override fun getItemCount(): Int {
        return orderHistoryDataList.size
    }

    fun setFont(holder: ViewHolder) {
        if (holder.adapterPosition == 0) {
            setTypefaceView(
                holder.txtOrderStatus,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        } else {
            setTypefaceView(
                holder.txtOrderStatus,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
        setTypefaceView(holder.txtMessage, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS)
        setTypefaceView(holder.txtTime, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            holder.txtOrderUpdatedBy,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(holder.txtDay, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDay: TextView
        var txtOrderStatus: TextView
        var txtOrderUpdatedBy: TextView
        var txtMessage: TextView
        var txtTime: TextView
        var orderStatusUpdatedByUserImage: ImageView
        var txtIndicatingLine: TextView
        var orderStatusIndicator: ImageView
        var layoutRow: ConstraintLayout
        var divider: TextView
        var lytItemRow: RelativeLayout

        init {
            txtDay = itemView.findViewById(R.id.textViewDays)
            txtOrderStatus = itemView.findViewById(R.id.txt_order_status)
            txtOrderUpdatedBy = itemView.findViewById(R.id.textViewUpdatedBy)
            txtMessage = itemView.findViewById(R.id.txt_comments)
            txtTime = itemView.findViewById(R.id.txt_order_time)
            orderStatusUpdatedByUserImage =
                itemView.findViewById(R.id.imageViewOrderStatusUpdatedBy)
            txtIndicatingLine = itemView.findViewById(R.id.txt_activity_line)
            orderStatusIndicator = itemView.findViewById(R.id.img_history_pointer)
            layoutRow = itemView.findViewById(R.id.lyt_history_log_row)
            divider = itemView.findViewById(R.id.txt_divider_activity_row)
            lytItemRow = itemView.findViewById(R.id.lyt_history_bubble)
        }

        companion object {
            val MARGIN_START_FOR_IMAGE_ORDER_STATUS_INDICATOR = CommonMethods.dpToPx(15)
            val MARGIN_START_FOR_IMAGE_ORDER_INDICATOR = CommonMethods.dpToPx(12)
            val PLACE_HOLDER_USER_IMAGE_WIDTH = CommonMethods.dpToPx(30)
            val PLACE_HOLDER_USER_IMAGE_HEIGHT = CommonMethods.dpToPx(30)
        }
    }
}