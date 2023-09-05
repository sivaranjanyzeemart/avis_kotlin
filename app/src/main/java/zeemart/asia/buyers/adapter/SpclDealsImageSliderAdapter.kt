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
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.orderimportimportimport.SpclDealsBaseResponse
import java.util.*

class SpclDealsImageSliderAdapter(
    lstDeals: List<SpclDealsBaseResponse.Deals>?,
    private val context: Context,
    private val mListener: DealsImageListener
) : PagerAdapter() {
    interface DealsImageListener {
        fun onDealSelected(deal: SpclDealsBaseResponse.Deals?)
    }

    private val lstDeals: List<SpclDealsBaseResponse.Deals>?

    init {
        this.lstDeals = lstDeals
    }

//    val count: Int
//        get() = lstDeals?.size ?: 0

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun getCount(): Int {
       return lstDeals?.size ?: 0
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout: View =
            LayoutInflater.from(context).inflate(R.layout.lyt_deal_image, container, false)!!
        val lytImageDeal: RelativeLayout =
            imageLayout.findViewById<RelativeLayout>(R.id.lyt_image_deal)
        val imageDeal = imageLayout.findViewById<ImageView>(R.id.img_deal)
        val txtDealEndInDays: TextView =
            imageLayout.findViewById<TextView>(R.id.txt_deal_end_in_days)
        ZeemartBuyerApp.setTypefaceView(
            txtDealEndInDays,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (lstDeals!![position].dealNumber != null) {
            val dealsEnd: Long? = lstDeals[position].activePeriod?.endTime?.times(1000)
            val now: Calendar = Calendar.getInstance(DateHelper.marketTimeZone)
            val currentDate = now.timeInMillis
            if (currentDate < dealsEnd!!) {
                val intDays: Int = DateHelper.getDaysBetweenDates(dealsEnd, currentDate)
                txtDealEndInDays.setVisibility(View.VISIBLE)
                if (intDays == 0) {
                    txtDealEndInDays.setText(context.resources.getString(R.string.txt_ends_today))
                } else {
                    txtDealEndInDays.setText(
                        String.format(
                            context.resources.getString(R.string.txt_deal_end_in_days),
                            intDays
                        )
                    )
                }
            } else {
                txtDealEndInDays.setVisibility(View.GONE)
            }
        } else {
            txtDealEndInDays.setVisibility(View.GONE)
        }
        if (lstDeals[position].carouselBanners != null && !StringHelper.isStringNullOrEmpty(
                lstDeals[position].carouselBanners?.get(0)
            )
        ) {
            Picasso.get().load(lstDeals[position].carouselBanners?.get(0)).fit().into(imageDeal)
        } else {
            if (lstDeals[position].dealSettings != null && !StringHelper.isStringNullOrEmpty(
                    lstDeals[position].dealSettings?.carouselBanners?.get(0)
                )
            ) {
                Picasso.get().load(lstDeals[position].dealSettings?.carouselBanners?.get(0))
                    .fit().into(imageDeal)
            } else {
                Picasso.get().load(R.drawable.placeholder_all).fit().into(imageDeal)
            }
        }
        lytImageDeal.setOnClickListener(View.OnClickListener {
            mListener.onDealSelected(
                lstDeals[position]
            )
        })
        container.addView(imageLayout, position)
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
}