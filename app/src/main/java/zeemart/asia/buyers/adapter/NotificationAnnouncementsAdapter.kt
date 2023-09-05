package zeemart.asia.buyers.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.NotificationAnnouncementsAdapter.ViewHolderNotificationAnnouncement
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.reportsimportimport.MarkAnnouncementReadReq
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes
import zeemart.asia.buyers.network.NotificationsApi.markNotificationAnnouncement
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.notifications.NotificationAnnouncementDetails

/**
 * Created by RajPrudhviMarella on 06/20/2019.
 */
class NotificationAnnouncementsAdapter(
    private var lstNotificationAnnouncements: List<RetrieveSpecificAnnouncementRes.Announcements>?,
    private val context: Context,
    private val mListener: GetRequestStatusResponseListener,
) : RecyclerView.Adapter<ViewHolderNotificationAnnouncement>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolderNotificationAnnouncement {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lst_notification_announcements, parent, false)
        return ViewHolderNotificationAnnouncement(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderNotificationAnnouncement, position: Int) {
        holder.txtNotificationTitle.text = lstNotificationAnnouncements!![position].title
        holder.txtNotificationDescription.text =
            lstNotificationAnnouncements!![position].shortDescription
        holder.lytNotification.setOnClickListener {
            if (lstNotificationAnnouncements!![position].isRead!= null && !lstNotificationAnnouncements!![position].isRead!!) {
                val listAnnouncementIds = java.util.ArrayList<String>()
                listAnnouncementIds.add(lstNotificationAnnouncements!![position].announcementId!!)
                val notificationRead = MarkAnnouncementReadReq()
                notificationRead.announcementIds = (listAnnouncementIds)
                markNotificationAnnouncement(
                    context,
                    notificationRead,
                    object : GetRequestStatusResponseListener {
                        override fun onSuccessResponse(status: String?) {
                            mListener.onSuccessResponse(status)
                        }

                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                            mListener.onErrorResponse(error)
                        }
                    }
                )
            }
            val newIntent = Intent(context, NotificationAnnouncementDetails::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.ANNOUNCEMENT_ID,
                lstNotificationAnnouncements!![position].announcementId
            )
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(newIntent)
        }
        if (lstNotificationAnnouncements!![position].isRead != null && !lstNotificationAnnouncements!![position].isRead!!) {
            holder.imgDot.visibility = View.VISIBLE
        } else {
            holder.imgDot.visibility = View.GONE
        }
        if (!StringHelper.isStringNullOrEmpty(lstNotificationAnnouncements!![position].imageURL)) {
            Picasso.get().load(lstNotificationAnnouncements!![position].imageURL)
                .placeholder(R.drawable.announcement_place_holder).centerCrop().fit()
                .into(holder.imgNotificationAnnouncement)
        } else {
            holder.imgNotificationAnnouncement.setImageDrawable(context.resources.getDrawable(R.drawable.announcement_place_holder))
        }
    }

    override fun getItemCount(): Int {
        return if (lstNotificationAnnouncements == null) {
            0
        } else {
            lstNotificationAnnouncements!!.size
        }
    }

    inner class ViewHolderNotificationAnnouncement(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val imgDot: ImageView
        val imgNotificationAnnouncement: ImageView
        val txtNotificationTitle: TextView
        val txtNotificationDescription: TextView
        val lytNotification: ConstraintLayout

        init {
            imgDot = itemView.findViewById(R.id.img_dot)
            imgNotificationAnnouncement = itemView.findViewById(R.id.img_notif_announcement)
            txtNotificationTitle = itemView.findViewById(R.id.txt_notif_announcement_title)
            txtNotificationDescription =
                itemView.findViewById(R.id.txt_notif_announcement_description)
            lytNotification = itemView.findViewById(R.id.lyt_notification_announcement)
            setTypefaceView(
                txtNotificationTitle,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(
                txtNotificationDescription,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    fun updateDataList(lstNotificationAnnouncements: List<RetrieveSpecificAnnouncementRes.Announcements>?) {
        this.lstNotificationAnnouncements = ArrayList(lstNotificationAnnouncements)
    }
}