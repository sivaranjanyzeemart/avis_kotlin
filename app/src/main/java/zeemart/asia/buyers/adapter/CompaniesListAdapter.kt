package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.Companies
import zeemart.asia.buyers.models.OrderPayments.UploadTransactionImage

class CompaniesListAdapter(
    private val context: Context,
    private val companies: List<Companies>,
    private val companySelectedListener: CompanySelectedListener
) : RecyclerView.Adapter<CompaniesListAdapter.ViewHolder>() {
    private val outletId: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_companies_list_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtCompanyTitle.text = companies.get(position).name
        if (companies[position].outlet != null) {
            val outletSize = companies[position].outlet?.size
            if (outletSize == 1) {
                holder.txtOutlet.text =
                    String.format(context.resources.getString(R.string.txt_outlet_items), 1)
            } else {
                holder.txtOutlet.text = String.format(
                    context.resources.getString(R.string.txt_outlets_items),
                    outletSize
                )
            }
        } else {
            holder.txtOutlet.text =
                String.format(context.resources.getString(R.string.txt_outlet_items), 0)
        }

//        holder.txtOutlet.setText(companies.get(position).getOutlet().size());
        if (!StringHelper.isStringNullOrEmpty(companies[position].logoURL)) {
            holder.imgCompanyImage.visibility = View.VISIBLE
            Picasso.get().load(companies[position].logoURL).fit().centerInside()
                .into(holder.imgCompanyImage)
        } else {
        }
        val status = companies[position].verificationStatus
        if ((status == "unVerified")) {
            holder.txtStatus.visibility = View.VISIBLE
            holder.txtStatus.setBackgroundColor(context.resources.getColor(R.color.pinky_red))
            holder.txtStatus.setText(R.string.txt_un_verified)
            holder.txtStatus.setBackgroundResource(R.drawable.btn_rounded_pink)
        } else if ((status == "inProgress")) {
            holder.txtStatus.visibility = View.VISIBLE
            holder.txtStatus.setText(R.string.txt_verification_requested)
            holder.txtStatus.setTextColor(context.resources.getColor(R.color.black))
            holder.txtStatus.setBackgroundResource(R.drawable.btn_rounded_yellow)
        } else {
            holder.txtStatus.visibility = View.GONE
        }
        holder.itemView.setOnClickListener(View.OnClickListener {
            companySelectedListener.onViewCompanyDetailsClicked(
                companies[position]
            )
        })
        if ((companies[position].settings != null) && (companies[position].settings?.payments != null) && (companies[position].settings?.payments?.paymentSources != null)
            && (companies[position].settings?.payments?.paymentSources?.size!! > 0))
            for (i in companies[position].settings?.payments?.paymentSources?.indices!!) {
            if (companies[position].settings?.payments?.paymentSources!![i].paymentType.equals(
                    UploadTransactionImage.PaymentType.GRAB_FINANCE.name,
                    ignoreCase = true
                )
            ) {
                if (companies[position].settings?.payments?.paymentSources!![i].status.equals(
                        "ACTIVE",
                        ignoreCase = true
                    )
                ) {
                    holder.lytGrabPayment.visibility = View.VISIBLE
                    holder.txtAmount.text =
                        companies.get(position).settings?.payments?.paymentSources?.get(i)?.availableLimit?.displayValue
                } else {
                    holder.lytGrabPayment.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return companies.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtCompanyTitle: TextView
        val txtOutlet: TextView
        val txtStatus: TextView
        val imgCompanyImage: ImageView
        val lytGrabPayment: RelativeLayout
        private val txtGrabHeader: TextView
        val txtAmount: TextView

        init {
            txtCompanyTitle = itemView.findViewById(R.id.txt_company_title)
            txtOutlet = itemView.findViewById(R.id.txt_company_outlet)
            txtStatus = itemView.findViewById(R.id.txt_company_status)
            imgCompanyImage = itemView.findViewById(R.id.img_company_image)
            lytGrabPayment = itemView.findViewById(R.id.lyt_grab_payment)
            txtAmount = itemView.findViewById(R.id.txt_amount)
            txtGrabHeader = itemView.findViewById(R.id.txt_grab_finance)
            setTypefaceView(txtCompanyTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtOutlet, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtStatus, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtGrabHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtAmount, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        }
    }

    interface CompanySelectedListener {
        fun onViewCompanyDetailsClicked(companies: Companies?)
    }
}