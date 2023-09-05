package zeemart.asia.buyers.adapter

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DialogHelper.alertDialogSmallSuccess
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.PromoListUI
import zeemart.asia.buyers.models.PromoListUI.PromoDataPassed
import zeemart.asia.buyers.models.RetrieveSpecificPromoCode.PromoCodes
import zeemart.asia.buyers.more.PromoCodeDetailsActivity

class AllPromoCodesListAdapter(
    private val context: Context,
    private val promoCodes: List<PromoCodes>
) : RecyclerView.Adapter<AllPromoCodesListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_promo_code_list_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtVoucherCode.text = promoCodes[position].promoCode
        holder.txtUsePromo.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                promoCodes[position].promoCode,
                promoCodes[position].promoCode
            )
            clipboard.setPrimaryClip(clip)
            alertDialogSmallSuccess((context as Activity), "Copied")
        }
        holder.txtVoucherTitle.text = promoCodes[position].title
        holder.txtVoucherDescription.text = promoCodes[position].description
        if (!StringHelper.isStringNullOrEmpty(promoCodes[position].imageURL)) {
            holder.imgCouponImage.visibility = View.VISIBLE
            Picasso.get().load(promoCodes[position].imageURL).fit().centerCrop()
                .into(holder.imgCouponImage)
        } else {
            holder.imgCouponImage.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PromoCodeDetailsActivity::class.java)
            val promoDataPassed = PromoDataPassed()
            promoDataPassed.promoCode = promoCodes[position].promoCode
            promoDataPassed.calledFrom = ZeemartAppConstants.ALL_PROMO_CODE_ACTIVITY
            promoDataPassed.promoCodeId = promoCodes[position].id
            val promoDataPassedString = ZeemartBuyerApp.gsonExposeExclusive.toJson(promoDataPassed)
            intent.putExtra(PromoListUI.PROMO_CODE_DETAILS, promoDataPassedString)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return promoCodes.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtVoucherTitle: TextView
        val txtVoucherDescription: TextView
        val txtVoucherCode: TextView
        val txtUsePromo: TextView
        private val imgCouponAppliedTick: ImageView
        val imgCouponImage: ImageView

        init {
            txtVoucherTitle = itemView.findViewById(R.id.txt_coupon_title)
            txtVoucherDescription = itemView.findViewById(R.id.txt_coupon_description)
            txtVoucherCode = itemView.findViewById(R.id.txt_voucher_code)
            txtUsePromo = itemView.findViewById(R.id.txt_use_promo)
            txtUsePromo.setText(R.string.txt_copy_code)
            imgCouponAppliedTick = itemView.findViewById(R.id.img_coupon_applied_tick)
            imgCouponImage = itemView.findViewById(R.id.img_coupon_image)
            ZeemartBuyerApp.setTypefaceView(
                txtVoucherTitle,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtVoucherDescription,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtUsePromo,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtVoucherCode,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }
}