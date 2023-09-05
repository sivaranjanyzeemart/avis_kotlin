package zeemart.asia.buyers.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.OutletTags
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier.Certification

class ProductCertificationsListingAdapter(
    var context: Context,
    var certifications: List<Certification>
) : RecyclerView.Adapter<ProductCertificationsListingAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lst_product_cert_items, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.certName.text = certifications[position].name
        setTypefaceView(holder.certName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        holder.lytCert.background =
            context.resources.getDrawable(R.drawable.btn_rounded_grey_for_tags)
        holder.certName.setTextColor(context.resources.getColor(R.color.black))
        when (certifications[position].name) {
            "Halal" -> holder.certIcon.setImageDrawable(
                context.getDrawable(
                    R.drawable.icon_halal
                )
            )
            "Organic" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_organic))
            "Vegan" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_vegan))
            "Vegetarian" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_vegetarian))
            "Kosher" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_kosher))
            "Gluten-free" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_gluten))
            "FDA" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_fda))
            "Fairtrade" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_fairtrade))
            "GMP" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_gmp))
            "HAACP" -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_haacp))
            else -> holder.certIcon.setImageDrawable(context.getDrawable(R.drawable.icon_vegan))
        }
        if (certifications[position].certificationURL != null) {
            holder.certName.setTextColor(context.resources.getColor(R.color.chart_blue))
            holder.certName.setOnClickListener {
                val images = certifications[position].certificationURL
                val urls: MutableList<String> = ArrayList()
                for (i in images!!.imageFileNames.indices) {
                    urls.add(images.imageURL + images.imageFileNames[i])
                }
                val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                dialog.setContentView(R.layout.dialog_image_fullscreen)
                dialog.window!!
                    .setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                val viewPager = dialog.findViewById<ViewPager>(R.id.imageView)
                viewPager.offscreenPageLimit = 0
                viewPager.adapter = CertificationSliderAdapter(context, urls)
                dialog.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return certifications.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val certName: TextView
        val certIcon: ImageView
        val lytCert: RelativeLayout

        init {
            certName = itemView.findViewById(R.id.txt_cert_name)
            lytCert = itemView.findViewById(R.id.lyt_cert_item)
            certIcon = itemView.findViewById(R.id.img_cert_icon)
            setTypefaceView(certName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    interface ProductListSelectedFilterItemClickListner {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
    }

    interface ProductListSelectedFilterCategoriesItemClickListener {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
    }

    interface ProductListSelectedFilterCertificationItemClickListener {
        fun onFilterSelected(outletTags: OutletTags?, textView: TextView?)
        fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?)
    }
}