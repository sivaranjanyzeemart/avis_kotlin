package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import java.util.*

class DealsImageSliderAdapter(
    private val lstDeals: List<DealsBaseResponse.Deals>?,
    private val context: Context,
    private val mListener: DealsImageListener
) : PagerAdapter() {
    interface DealsImageListener {
        fun onDealSelected(deal: DealsBaseResponse.Deals?)
    }

    override fun getCount(): Int {
        return lstDeals?.size ?: 0
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout =
            LayoutInflater.from(context).inflate(R.layout.lyt_deal_image, container, false)!!
        val lytImageDeal = imageLayout.findViewById<RelativeLayout>(R.id.lyt_image_deal)
        val imageDeal = imageLayout.findViewById<ImageView>(R.id.img_deal)
        val txtDealEndInDays = imageLayout.findViewById<TextView>(R.id.txt_deal_end_in_days)
        setTypefaceView(txtDealEndInDays, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        val dealsEnd = lstDeals!![position].activePeriod?.endTime?.times(1000)
        val now = Calendar.getInstance(DateHelper.marketTimeZone)
        val currentDate = now.timeInMillis
        if (currentDate < dealsEnd!!) {
            val intDays = DateHelper.getDaysBetweenDates(dealsEnd, currentDate)
            txtDealEndInDays.visibility = View.VISIBLE
            if (intDays == 0) {
                txtDealEndInDays.text = context.resources.getString(R.string.txt_ends_today)
            } else {
                txtDealEndInDays.text = String.format(
                    context.resources.getString(R.string.txt_deal_end_in_days),
                    intDays
                )
            }
        } else {
            txtDealEndInDays.visibility = View.GONE
        }
        if (lstDeals[position].carouselBanners != null && !StringHelper.isStringNullOrEmpty(
                lstDeals[position].carouselBanners!![0]
            )
        ) {
            Picasso.get().load(lstDeals[position].carouselBanners!![0]).fit().into(imageDeal)
        } else {
            Picasso.get().load(R.drawable.placeholder_all).fit().into(imageDeal)
        }
        lytImageDeal.setOnClickListener {
            mListener.onDealSelected(
                lstDeals[position]
            )
        }
        container.addView(imageLayout, position)
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
}