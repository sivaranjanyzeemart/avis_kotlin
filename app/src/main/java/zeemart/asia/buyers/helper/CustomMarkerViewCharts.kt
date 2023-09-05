package zeemart.asia.buyers.helper

import android.content.*
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.context
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.PriceDetails

/**
 * Created by ParulBhandari on 4/18/2018.
 */
class CustomMarkerViewCharts(context: Context?, layoutResource: Int, isForViewOrders: Boolean) :
    MarkerView(context, layoutResource) {
    private val txtMarker: TextView
    private var mOffset: MPPointF? = null
    private val isForViewOrders: Boolean

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    init {
        txtMarker = findViewById(R.id.txt_marker_graph)
        this.isForViewOrders = isForViewOrders
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        super.refreshContent(e, highlight)
        val total = e.y.toDouble()
        //        Double totalDouble = ZeemartBuyerApp.getPriceDouble(total,2);
        if (isForViewOrders) {
            val totalString = PriceDetails(total).displayValue
            txtMarker.text = totalString
        } else {
            val priceDetails = PriceDetails(total)
            txtMarker.text = priceDetails.displayValue
        }
        setTypefaceView(txtMarker, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF(-(width / 2).toFloat(), -height.toFloat())
        }
        return mOffset!!
    }
}