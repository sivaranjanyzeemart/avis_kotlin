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
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.order.StatusDetail

/**
 * Created by Rajprudhvi Marella on 1/8/2018.
 */
class SupplierContactListAdapter(
    private val context: Context,
    statusDetails: List<StatusDetail.StatusDetailUI>
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val statusDetails: List<StatusDetail.StatusDetailUI>

    init {
        this.statusDetails = statusDetails
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_supplier_contact_info, parent, false)
        return ViewHolderItem(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: StatusDetail.StatusDetailUI = statusDetails[position]
        val viewHolderItem = holder as ViewHolderItem
        viewHolderItem.txtContact.setText(item.displayName)
        if (statusDetails[position] is StatusDetail.Email) {
            val emailList: StatusDetail.Email = statusDetails[position] as StatusDetail.Email
            if (emailList.emailId.equals(statusDetails[position].displayName)) {
                viewHolderItem.imageContact.setImageResource(R.drawable.mail_grey)
                viewHolderItem.lytHeaderSupplierContact.setOnClickListener(View.OnClickListener {
                    val emailId: String = item.displayName.toString()
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
            val mobileList: StatusDetail.Mobile = statusDetails[position] as StatusDetail.Mobile
            if (mobileList.getPhoneNumber().equals(statusDetails[position].displayName)) {
                viewHolderItem.imageContact.setImageResource(R.drawable.phone_grey)
                viewHolderItem.lytHeaderSupplierContact.setOnClickListener(View.OnClickListener {
                    val mobileNo: String = item.displayName.toString()
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
        }
    }

    override fun getItemCount(): Int {
        return statusDetails.size
    }

    inner class ViewHolderItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageContact: ImageView
        var txtContact: TextView
        var lytHeaderSupplierContact: LinearLayout

        init {
            imageContact = itemView.findViewById(R.id.img_contact_details)
            txtContact = itemView.findViewById<TextView>(R.id.txt_contact_details)
            lytHeaderSupplierContact =
                itemView.findViewById<LinearLayout>(R.id.lyt_header_supplier_contact)
            ZeemartBuyerApp.setTypefaceView(
                txtContact,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    companion object {
        private const val ITEM_FIRST = 0
        private const val ITEM_SECOND = 1
    }
}