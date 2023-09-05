package zeemart.asia.buyers.models.invoice

import android.util.Log

/**
 * Created by saiful on 4/1/18.
 */
class InvoiceMgr : Invoice() {
    var isHeader = false
    var headerDate = ""
    var headerDay = ""

    override val invoiceImagesList: List<String>
        get() {
            val imageUrls: MutableList<String> = ArrayList()
            for (imgs in images!!) {
                for (name in imgs.imageFileNames!!) {
                    imageUrls.add(imgs.imageURL + name)
                    Log.e("imageUrls=", "" + imgs.imageURL + name)
                }
            }
            return imageUrls
        }
}