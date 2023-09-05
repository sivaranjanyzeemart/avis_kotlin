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
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential

/**
 * Created by RajPrudhvi on 4/17/2018.
 */
class SupplierListEssentialsAdapter(
    private val lstEssentials: List<Essential>,
    private val context: Context,
    private val supplierClickListener: SupplierClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.lyt_supplier_list_essentials, viewGroup, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holderSupplier = holder as ViewHolder
        holderSupplier.txtSupplierName.text = lstEssentials[position].supplier!!.supplierName
        holderSupplier.lytSupplierList.setOnClickListener {
            supplierClickListener.onItemSelected(
                lstEssentials[position]
            )
        }
        if (StringHelper.isStringNullOrEmpty(lstEssentials[position].supplier!!.logoURL)) {
            holderSupplier.imgSupplier.visibility = View.INVISIBLE
            holderSupplier.lytSupplierThumbNail.visibility = View.VISIBLE
            holderSupplier.lytSupplierThumbNail.setBackgroundColor(
                CommonMethods.SupplierThumbNailBackgroundColor(
                    lstEssentials[position].supplier!!.supplierName!!, context
                )
            )
            holderSupplier.txtSupplierThumbNail.text = CommonMethods.SupplierThumbNailShortCutText(
                lstEssentials[position].supplier!!.supplierName!!
            )
            holderSupplier.txtSupplierThumbNail.setTextColor(
                CommonMethods.SupplierThumbNailTextColor(
                    lstEssentials[position].supplier!!.supplierName!!, context
                )
            )
        } else {
            holderSupplier.imgSupplier.visibility = View.VISIBLE
            holderSupplier.lytSupplierThumbNail.visibility = View.GONE
            Picasso.get().load(lstEssentials[position].supplier!!.logoURL)
                .placeholder(R.drawable.placeholder_all).resize(
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
            ).into(holderSupplier.imgSupplier)
        }
        if (lstEssentials[position].rebatePercentage != null && lstEssentials[position].rebatePercentage != 0.0) {
            holderSupplier.imgRebate.visibility = View.VISIBLE
            holderSupplier.txtRebateAmount.visibility = View.VISIBLE
            holderSupplier.txtRebateAmount.text =
                lstEssentials[position].rebatePercentage.toString() + "%"
            if (lstEssentials[position].shortDesc != null) {
                holderSupplier.txtSupplierDescription.visibility = View.VISIBLE
                val supplierDescription = " • " + lstEssentials[position].shortDesc
                holderSupplier.txtSupplierDescription.text = supplierDescription
            } else {
                holderSupplier.txtSupplierDescription.visibility = View.GONE
            }
        } else {
            holderSupplier.imgRebate.visibility = View.GONE
            holderSupplier.txtRebateAmount.visibility = View.GONE
            if (lstEssentials[position].shortDesc != null) {
                holderSupplier.txtSupplierDescription.visibility = View.VISIBLE
                holderSupplier.txtSupplierDescription.text = lstEssentials[position].shortDesc
            } else {
                holderSupplier.txtSupplierDescription.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return lstEssentials.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var txtSupplierDescription: TextView
        var txtRebateAmount: TextView
        var lytSupplierList: RelativeLayout
        var imgRebate: ImageView
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.img_supplier_essentials)
            txtSupplierName = itemView.findViewById(R.id.txt_supplier_name_essentials)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtSupplierDescription = itemView.findViewById(R.id.txt_date_type_essentials)
            txtRebateAmount = itemView.findViewById(R.id.txt_rebate_amount)
            setTypefaceView(
                txtSupplierDescription,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtRebateAmount, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            lytSupplierList = itemView.findViewById(R.id.layout_supplier_list)
            imgRebate = itemView.findViewById(R.id.img_rebate)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }

        companion object {
            val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH = CommonMethods.dpToPx(30)
            val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT = CommonMethods.dpToPx(30)
        }
    }

    interface SupplierClickListener {
        fun onItemSelected(essential: Essential?)
    }
}