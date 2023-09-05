package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.PromoListUI

class PromoCodeListAdapter(
    private val context: Context,
    private val promoCodes: List<PromoListUI>,
    private val outletId: String,
    private val promoCodeSelectedListener: PromoCodeSelectedListener
) : RecyclerView.Adapter<PromoCodeListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_promo_code_list_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition

        holder.txtVoucherCode.text = promoCodes.get(currentPosition).promoCode
        holder.txtUsePromo.setOnClickListener(View.OnClickListener {
            if (!promoCodes[currentPosition].isPromoSelected) promoCodeSelectedListener.onUsePromoCodeClicked(
                promoCodes[currentPosition]
            )
        })
        if (promoCodes[currentPosition].isPromoSelected) {
            holder.txtUsePromo.setText(R.string.txt_code_applied)
            holder.imgCouponAppliedTick.visibility = View.VISIBLE
            holder.txtUsePromo.setTextColor(context.resources.getColor(R.color.green))
        } else {
            holder.txtUsePromo.setText(R.string.txt_use_promo)
            holder.imgCouponAppliedTick.visibility = View.GONE
            holder.txtUsePromo.setTextColor(context.resources.getColor(R.color.text_blue))
        }
        holder.txtVoucherTitle.text = promoCodes.get(currentPosition).title
        holder.txtVoucherDescription.text = promoCodes.get(currentPosition).description
        if (!StringHelper.isStringNullOrEmpty(promoCodes[currentPosition].imageURL)) {
            holder.imgCouponImage.visibility = View.VISIBLE
            Picasso.get().load(promoCodes[currentPosition].imageURL).fit().centerInside()
                .into(holder.imgCouponImage)
        } else {
            holder.imgCouponImage.visibility = View.GONE
        }
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                promoCodeSelectedListener.onViewPromoDetailsClicked(promoCodes[currentPosition])
            }
        })
    }

    override fun getItemCount(): Int {
        return promoCodes.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtVoucherTitle: TextView
        val txtVoucherDescription: TextView
        val txtVoucherCode: TextView
        val txtUsePromo: TextView
        val imgCouponAppliedTick: ImageView
        val imgCouponImage: ImageView

        init {
            txtVoucherTitle = itemView.findViewById(R.id.txt_coupon_title)
            txtVoucherDescription = itemView.findViewById(R.id.txt_coupon_description)
            txtVoucherCode = itemView.findViewById(R.id.txt_voucher_code)
            txtUsePromo = itemView.findViewById(R.id.txt_use_promo)
            imgCouponAppliedTick = itemView.findViewById(R.id.img_coupon_applied_tick)
            imgCouponImage = itemView.findViewById(R.id.img_coupon_image)
            setTypefaceView(txtVoucherTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(
                txtVoucherDescription,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            setTypefaceView(txtUsePromo, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtVoucherCode, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    interface PromoCodeSelectedListener {
        fun onUsePromoCodeClicked(promoListUI: PromoListUI?)
        fun onViewPromoDetailsClicked(promoListUI: PromoListUI?)
    }
}