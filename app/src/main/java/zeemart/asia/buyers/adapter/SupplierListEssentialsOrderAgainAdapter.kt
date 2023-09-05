package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential

/**
 * Created by Rajprudhvi Marella on 30/oct/2018.
 */
class SupplierListEssentialsOrderAgainAdapter(
    private val lstEssentials: List<Essential>,
    private val context: Context,
    private val supplierClickListener: SupplierClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.lyt_supplier_list_order_again_essentials,
            viewGroup,
            false
        )
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
    }

    override fun getItemCount(): Int {
        return lstEssentials.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var lytSupplierList: CardView
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.img_supplier_essentials)
            txtSupplierName = itemView.findViewById(R.id.txt_supplier_name_essentials)
            lytSupplierList = itemView.findViewById(R.id.layout_supplier_list)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
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