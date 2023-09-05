package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.helper.StringHelper

/**
 * Created by ParulBhandari on 1/26/2018.
 */
class ImageSwipeAdapter(context: Context, imageFileUrls: List<String>?) : PagerAdapter() {
    private val imageFileUrls: ArrayList<String>
    private val context: Context

    init {
        this.imageFileUrls = ArrayList(imageFileUrls)
        this.context = context
    }

    override fun getCount(): Int {
        return imageFileUrls.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout =
            LayoutInflater.from(context).inflate(R.layout.lyt_product_image, container, false)!!
        val imageViewProduct = imageLayout
            .findViewById<ImageView>(R.id.img_product)
        val imageViewNoImage = imageLayout.findViewById<ImageView>(R.id.image_no_image)
        val txtNoImage = imageLayout.findViewById<TextView>(R.id.txt_no_image)
        if (StringHelper.isStringNullOrEmpty(imageFileUrls[position])) {
            imageViewNoImage.visibility = View.VISIBLE
            imageViewProduct.visibility = View.GONE
            txtNoImage.visibility = View.VISIBLE
        } else {
            Picasso.get().load(imageFileUrls[position]).into(imageViewProduct, object : Callback {
                override fun onSuccess() {
                    imageViewNoImage.visibility = View.GONE
                    imageViewProduct.visibility = View.VISIBLE
                    txtNoImage.visibility = View.GONE
                }

                override fun onError(e: Exception) {
                    imageViewNoImage.visibility = View.VISIBLE
                    imageViewProduct.visibility = View.GONE
                    txtNoImage.visibility = View.VISIBLE
                }
            })
        }
        container.addView(imageLayout, position)
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
}