package zeemart.asia.buyers.notifications

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes
import zeemart.asia.buyers.network.NotificationsApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.createorders.BrowseCreateNewOrder
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity

/**
 * Created by RajPrudhviMarella on 06/20/2019.
 */
class NotificationAnnouncementDetails : AppCompatActivity() {
    private var announcementID: String? = null
    private var imgAnnouncementDetails: ImageView? = null
    private lateinit var btnBack: ImageButton
    private var txtAnnouncementTitle: TextView? = null
    private var txtAnnouncementDate: TextView? = null
    private var txtAnnouncementDescription: TextView? = null
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    private var btnExternalLink: Button? = null
    private var url: String? = null
    private var isInternalLinkAvailable = false
    private var calledFrom: String? = null
    private var retrieveSpecificAnnouncementRes: RetrieveSpecificAnnouncementRes.Announcements? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_notification_announcement_details)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ANNOUNCEMENT_ID)) {
                announcementID = bundle.getString(ZeemartAppConstants.ANNOUNCEMENT_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        imgAnnouncementDetails = findViewById<ImageView>(R.id.img_announcement_details)
        btnBack = findViewById<ImageButton>(R.id.img_btn_back)
        txtAnnouncementTitle = findViewById<TextView>(R.id.txt_announcement_title)
        txtAnnouncementDate = findViewById<TextView>(R.id.txt_announcement_date)
        txtAnnouncementDescription = findViewById<TextView>(R.id.txt_announcement_description)
        threeDotLoaderWhite =
            findViewById<CustomLoadingViewWhite>(R.id.spin_kit_loader_announcements_details_white)
        btnExternalLink = findViewById<Button>(R.id.btn_external_link)
        btnExternalLink!!.visibility = View.GONE
        btnExternalLink!!.setOnClickListener {
            AnalyticsHelper.logAction(
                this@NotificationAnnouncementDetails,
                AnalyticsHelper.TAP_NOTIFICATIONS_NEWS_CTA,
                retrieveSpecificAnnouncementRes!!
            )
            if (isInternalLinkAvailable) {
                if (StringHelper.isStringNullOrEmpty(
                        SharedPref.read(
                            SharedPref.SELECTED_OUTLET_ID,
                            ""
                        )
                    )
                ) {
                    val newIntent = Intent(
                        this@NotificationAnnouncementDetails,
                        SelectOutletActivity::class.java
                    )
                    newIntent.putExtra(
                        ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM,
                        ZeemartAppConstants.SELECT_OUTLET_CALLED_FROM_DASH_BOARD_FOR_BROWSE_SCREEN
                    )
                    startActivity(newIntent)
                } else {
                    val browseForNewOrderIntent = Intent(
                        this@NotificationAnnouncementDetails,
                        BrowseCreateNewOrder::class.java
                    )
                    startActivity(browseForNewOrderIntent)
                }
            } else {
                if (!StringHelper.isStringNullOrEmpty(url)) {
                    try {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.setData(Uri.parse(url))
                        startActivity(i)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        threeDotLoaderWhite.setVisibility(View.VISIBLE)
        btnBack.setOnClickListener(View.OnClickListener { setActionOnBackPressed() })
        setFont()
        callNotificationAnnouncementsDetails()
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtAnnouncementTitle,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAnnouncementDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAnnouncementDescription,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnExternalLink,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun callNotificationAnnouncementsDetails() {
        SharedPref.defaultOutlet?.outletId?.let {
            NotificationsApi.retrieveSpecificAnnouncement(
                this@NotificationAnnouncementDetails,
                it,
                announcementID,
                object : NotificationsApi.RetrieveSpecificAnnouncementResponseListener {

                    override fun onSuccessResponse(notificationAnnouncementsDetails: RetrieveSpecificAnnouncementRes?) {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                        if (notificationAnnouncementsDetails != null) {
                            setUIForAnnouncementDetails(notificationAnnouncementsDetails)
                        }
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        threeDotLoaderWhite.setVisibility(View.GONE)
                    }
                })
        }
    }

    private fun setUIForAnnouncementDetails(notificationAnnouncementsDetails: RetrieveSpecificAnnouncementRes) {
        if (notificationAnnouncementsDetails.data != null) {
            retrieveSpecificAnnouncementRes = notificationAnnouncementsDetails.data
            AnalyticsHelper.logAction(
                this@NotificationAnnouncementDetails,
                AnalyticsHelper.TAP_NOTIFICATIONS_NEWS_DETAIL,
                retrieveSpecificAnnouncementRes!!
            )
            val day: String = DateHelper.getDateInDateMonthYearFormat(
                notificationAnnouncementsDetails.data!!.timeToBePublished
            )
            txtAnnouncementDate?.setText(day)
            txtAnnouncementDescription?.setText(
                notificationAnnouncementsDetails.data!!.fullDescription
            )
            txtAnnouncementTitle?.setText(notificationAnnouncementsDetails.data!!.title)
            if (!StringHelper.isStringNullOrEmpty(
                    notificationAnnouncementsDetails.data!!.imageURL
                )
            ) {
                //Log.d("ANNOUNCEMENT_IMAGE_URL",notificationAnnouncementsDetails.getData().getImageURL()+"*****");
                Picasso.get().load(notificationAnnouncementsDetails.data!!.imageURL)
                    .placeholder(
                        R.drawable.announcement_place_holder
                    ).centerCrop().fit().into(imgAnnouncementDetails)
            }
            if (!StringHelper.isStringNullOrEmpty(
                    notificationAnnouncementsDetails.data!!.callToAction?.buttonLabel
                ) && !StringHelper.isStringNullOrEmpty(
                    notificationAnnouncementsDetails.data!!.callToAction?.externalLink
                )
            ) {
                btnExternalLink!!.visibility = View.VISIBLE
                btnExternalLink?.setText(
                    notificationAnnouncementsDetails.data!!.callToAction?.buttonLabel
                )
                url = notificationAnnouncementsDetails.data!!.callToAction?.externalLink
            }
            if (!StringHelper.isStringNullOrEmpty(
                    notificationAnnouncementsDetails.data!!.callToAction
                        ?.gotoButtonLabel
                ) && !StringHelper.isStringNullOrEmpty(
                    notificationAnnouncementsDetails.data!!.callToAction?.gotoLink
                ) && notificationAnnouncementsDetails.data!!.callToAction?.gotoLink.equals("Create_Order", ignoreCase = true)
            ) {
                btnExternalLink!!.visibility = View.VISIBLE
                btnExternalLink!!.setText(
                    notificationAnnouncementsDetails.data!!.callToAction
                        ?.gotoButtonLabel
                )
                isInternalLinkAvailable = true
            }
        }
    }

    override fun onBackPressed() {
        setActionOnBackPressed()
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_right)
    }

    private fun setActionOnBackPressed() {
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == NotificationConstants.CALLED_FROM_ANNOUNCEMENT_DETAILS) {
            startActivity(
                Intent(
                    this@NotificationAnnouncementDetails,
                    BaseNavigationActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        finish()
    }
}