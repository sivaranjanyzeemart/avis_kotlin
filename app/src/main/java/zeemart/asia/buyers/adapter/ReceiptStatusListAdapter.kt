package zeemart.asia.buyers.adapter

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.order.StatusDetail

/**
 * Created by ParulBhandari on 12/29/2017.
 */
class ReceiptStatusListAdapter(private val context: Context, statusDetails: List<StatusDetail.StatusDetailUI>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val statusDetails: List<StatusDetail.StatusDetailUI>

    init {
        this.statusDetails = statusDetails
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.receipt_status_row, parent, false)
        return ViewHolderItem(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: StatusDetail.StatusDetailUI = statusDetails[position]
        val viewHolderItem = holder as ViewHolderItem
        viewHolderItem.txtReceiptEmail.setText(item.displayName)
        val resId: Int = statusDetails[position].statusResourceString
        viewHolderItem.txtReceiptStatus.setText(context.resources.getString(resId))
        val colorResId: Int = statusDetails[position].statusTextColorResId
        viewHolderItem.txtReceiptStatus.setTextColor(context.resources.getColor(colorResId))
        viewHolderItem.txtReceiptTime.setText(item.displayTimestamp)
        viewHolderItem.imgReceiptStatus.setImageResource(item.displayStatusIconResId)
        ZeemartBuyerApp.setTypefaceView(
            viewHolderItem.txtReceiptEmail,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            viewHolderItem.txtReceiptStatus,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            viewHolderItem.txtReceiptTime,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            viewHolderItem.txtWhatsApp,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (statusDetails[position] is StatusDetail.Email) {
            viewHolderItem.txtWhatsApp.setVisibility(View.GONE)
            viewHolderItem.imgWhatsApp.visibility = View.GONE
            val emailList: StatusDetail.Email = statusDetails[position] as StatusDetail.Email
            if (emailList.emailId.equals(statusDetails[position].displayName)) {
                viewHolderItem.lytHeaderSupplierContact.setOnClickListener(View.OnClickListener {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_ORDER_STATUS_ACTIVITY_EMAIL
                    )
                    val emailId: String? = item.displayName
                    val emailViewList = arrayOf(
                        context.resources.getString(R.string.txt_email),
                        context.resources.getString(
                            R.string.contact_supplier_copy_add
                        )
                    )
                    val builder = AlertDialog.Builder(
                        context
                    )
                    builder.setTitle(null)
                        .setItems(emailViewList, object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, viewType: Int) {
                                if (viewType == ITEM_FIRST) {
                                    AnalyticsHelper.logAction(
                                        context,
                                        AnalyticsHelper.TAP_ORDER_STATUS_ACTIVITY_SEND_EMAIL
                                    )
                                    dialog.dismiss()
                                    val emailIntent = Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.parse(ZeemartAppConstants.INTENT_EMAIL_CONSTANT + emailId)
                                    )
                                    context.startActivity(
                                        Intent.createChooser(
                                            emailIntent, context.resources.getString(
                                                R.string.contact_supplier_choose_title
                                            )
                                        )
                                    )
                                } else if (viewType == ITEM_SECOND) {
                                    AnalyticsHelper.logAction(
                                        context,
                                        AnalyticsHelper.TAP_ORDER_STATUS_ACTIVITY_COPY_ADDRESS
                                    )
                                    dialog.dismiss()
                                    val clipMan =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip: ClipData = ClipData.newPlainText(null, emailId)
                                    clipMan.setPrimaryClip(clip)
                                    ZeemartBuyerApp.getToastGreen(
                                        emailId + " " + context.resources.getString(
                                            R.string.contact_supplier_email_copied
                                        )
                                    )
                                } else {
                                    dialog.dismiss()
                                }
                            }
                        })
                    val dialog = builder.create()
                    dialog.show()
                })
            }
        } else if (statusDetails[position] is StatusDetail.Mobile) {
            viewHolderItem.txtWhatsApp.setVisibility(View.GONE)
            viewHolderItem.imgWhatsApp.visibility = View.GONE
            val mobileList: StatusDetail.Mobile = statusDetails[position] as StatusDetail.Mobile
            if (mobileList.getPhoneNumber().equals(statusDetails[position].displayName)) {
                viewHolderItem.lytHeaderSupplierContact.setOnClickListener(View.OnClickListener {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_ORDER_STATUS_ACTIVITY_PHONE
                    )
                    val mobileNo: String? = item.displayName
                    val mobileViewList = arrayOf(
                        context.resources.getString(R.string.contact_supplier_call),
                        context.resources.getString(
                            R.string.contact_supplier_msg
                        )
                    )
                    val builder = AlertDialog.Builder(
                        context
                    )
                    builder.setTitle(null)
                        .setItems(mobileViewList, object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, viewType: Int) {
                                if (viewType == ITEM_FIRST) {
                                    AnalyticsHelper.logAction(
                                        context,
                                        AnalyticsHelper.TAP_ORDER_STATUS_ACTIVITY_CALL
                                    )
                                    dialog.dismiss()
                                    val intent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.fromParts(
                                            ZeemartAppConstants.INTENT_TEL_CONSTANT,
                                            mobileNo,
                                            null
                                        )
                                    )
                                    context.startActivity(intent)
                                } else if (viewType == ITEM_SECOND) {
                                    AnalyticsHelper.logAction(
                                        context,
                                        AnalyticsHelper.TAP_ORDER_STATUS_ACTIVITY_MESSAGE
                                    )
                                    dialog.dismiss()
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(ZeemartAppConstants.INTENT_SMS_CONSTANT + mobileNo)
                                    )
                                    context.startActivity(intent)
                                } else {
                                    dialog.dismiss()
                                }
                            }
                        })
                    val dialog = builder.create()
                    dialog.show()
                })
            }
        } else if (statusDetails[position] is StatusDetail.WhatsApp) {
            val whatsAppList: StatusDetail.WhatsApp = statusDetails[position] as StatusDetail.WhatsApp
            if (!StringHelper.isStringNullOrEmpty(whatsAppList.getPhoneNumber()) && !StringHelper.isStringNullOrEmpty(
                    statusDetails[position].displayName
                ) && whatsAppList.getPhoneNumber().equals(
                    statusDetails[position].displayName
                )
            ) {
                viewHolderItem.txtWhatsApp.setVisibility(View.VISIBLE)
                viewHolderItem.imgWhatsApp.visibility = View.VISIBLE
                viewHolderItem.lytHeaderSupplierContact.setOnClickListener(View.OnClickListener {
                    val url: String =
                        ServiceConstant.INTENT_FOR_WHATS_APP_URL + whatsAppList.getPhoneNumber()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse(url))
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_app_not_found))
                    }
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return statusDetails.size
    }

    inner class ViewHolderItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgReceiptStatus: ImageView
        var imgWhatsApp: ImageView
        var txtWhatsApp: TextView
        var txtReceiptEmail: TextView
        var txtReceiptStatus: TextView
        var txtReceiptTime: TextView
        var lytHeaderSupplierContact: LinearLayout

        init {
            imgReceiptStatus = itemView.findViewById(R.id.imgReceiptStatus)
            txtReceiptEmail = itemView.findViewById<TextView>(R.id.txtReceiptEmail)
            txtReceiptStatus = itemView.findViewById<TextView>(R.id.txtReceiptStatus)
            txtReceiptTime = itemView.findViewById<TextView>(R.id.txtReceiptTime)
            lytHeaderSupplierContact = itemView.findViewById<LinearLayout>(R.id.lyt_details)
            imgWhatsApp = itemView.findViewById(R.id.img_whats_app)
            txtWhatsApp = itemView.findViewById<TextView>(R.id.txt_whats_app)
        }
    }

    companion object {
        private const val ITEM_FIRST = 0
        private const val ITEM_SECOND = 1
    }
}