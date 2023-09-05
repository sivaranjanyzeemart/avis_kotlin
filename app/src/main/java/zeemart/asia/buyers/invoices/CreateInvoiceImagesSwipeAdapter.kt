package zeemart.asia.buyers.invoices

import android.app.Activity
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import android.util.Log
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
import zeemart.asia.buyers.helper.StringHelper
import java.io.File

/**
 * Created by ParulBhandari on 6/18/2018.
 */
class CreateInvoiceImagesSwipeAdapter(
    private val context: Context,
    private val imageUris: List<InQueueForUploadDataModel>
) : PagerAdapter() {
    override fun getCount(): Int {
        return imageUris.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout =
            LayoutInflater.from(context).inflate(R.layout.layout_invoice_image, container, false)!!
        val imageViewProduct =
            imageLayout.findViewById<ImageView>(R.id.image_product_product_details)
        val imageViewNoImage = imageLayout.findViewById<ImageView>(R.id.image_no_image)
        val txtNoImage = imageLayout.findViewById<TextView>(R.id.txt_no_image)
        val txtInvoicePdfDisplayName = imageLayout.findViewById<TextView>(R.id.txt_invoice_pdf_name)
        val txtNoPreviewForPdf = imageLayout.findViewById<TextView>(R.id.txt_no_preview_for_pdf)
        val lytInvoicePdfView = imageLayout.findViewById<RelativeLayout>(R.id.lyt_invoice_pdf_view)
        setTypefaceView(
            txtInvoicePdfDisplayName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtNoPreviewForPdf, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        lytInvoicePdfView.visibility = View.GONE
        imageViewProduct.visibility = View.GONE
        if (StringHelper.isStringNullOrEmpty(imageUris[position].imageFilePath)) {
            lytInvoicePdfView.visibility = View.GONE
            imageViewNoImage.visibility = View.VISIBLE
            imageViewProduct.visibility = View.GONE
            txtNoImage.visibility = View.VISIBLE
        } else {
            imageViewProduct.visibility = View.VISIBLE
            lytInvoicePdfView.visibility = View.GONE
            if (imageUris[position].isInvoiceSelectedIsImage) {
                val uri = imageUris[position].imageFilePath
                val builder = Picasso.Builder(context)
                builder.listener { picasso, uri, exception ->
                    Log.d("EXCEPTION", "****************************************")
                    exception.printStackTrace()
                }
                val displayMetrics = DisplayMetrics()
                (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                val width = displayMetrics.widthPixels
                Log.d("ROTATION", imageUris[position].rotation.toString() + "****")
                builder.build().load(File(uri)).into(imageViewProduct)
            } else {
                imageViewProduct.visibility = View.GONE
                lytInvoicePdfView.visibility = View.VISIBLE
                txtInvoicePdfDisplayName.text = imageUris[position].selectedPdfOriginalName
                txtNoPreviewForPdf.alpha = 0.3f
            }
        }
        imageLayout.tag = imageUris[position]
        container.addView(imageLayout)
        Log.i(
            "",
            "instantiateItem() [position: " + position + "]" + " childCount:" + container.childCount
        )
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        var view: View? = `object` as View
        val imgView = view!!.findViewById<View>(R.id.image_product_product_details) as ImageView
        val bmpDrawable = imgView.drawable as BitmapDrawable
        if (bmpDrawable != null && bmpDrawable.bitmap != null) {
            // This is the important part
            bmpDrawable.bitmap.recycle()
        }
        container.removeView(view)
        view = null
    }

    /**
     * @return true if the value returned from [.instantiateItem] is the
     * same object as the [View] added to the [ViewPager].
     */
    override fun getItemPosition(`object`: Any): Int {
        val inQueueForUploadDataModel = (`object` as View).tag as InQueueForUploadDataModel
        val position = imageUris.indexOf(inQueueForUploadDataModel)
        return if (position >= 0) {
            // The current data matches the data in this active fragment, so let it be as it is.
            position
        } else {
            // Returning POSITION_NONE means the current data does not matches the data this fragment is showing right now.  Returning POSITION_NONE constant
            // will force the view to redraw its layout all over again and show new data.
            POSITION_NONE
        }
    }
}