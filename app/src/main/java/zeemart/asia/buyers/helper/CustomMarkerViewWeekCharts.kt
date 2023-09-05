package zeemart.asia.buyers.helper

import android.content.*
import android.widget.LinearLayout
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
 * Created by Muthumari on 08/07/2019.
 */
class CustomMarkerViewWeekCharts(
    context: Context?,
    layoutResource: Int,
    dayValuesLastWeek: List<String>,
    dayValues: List<String>,
    entriesLastWeek: List<Entry>,
    entriesCurrentWeek: List<Entry>
) : MarkerView(context, layoutResource) {
    private val layoutWeekMonth: LinearLayout
    private val txtMarker: TextView
    private val txtCurrentWeekMarker: TextView
    private val txtLastWeekMarker: TextView
    private var mOffset: MPPointF? = null
    private val dayValues: List<String>
    private val dayValuesLastWeek: List<String>
    private val entriesLastWeek: List<Entry>
    private val entriesCurrentWeek: List<Entry>
    private var totalString: String? = null
    private var lastWeekValue: String? = null
    private var currentWeekValue: String? = null
    private var total: Double? = null
    private var totalLastWeek: Double? = null
    private var totalCurrentWeek: Double? = null

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource     the layout resource to use for the MarkerView
     * @param dayValuesLastWeek
     * @param dayValues
     * @param entriesCurrentWeek
     * @param entriesLastWeek
     */
    init {
        layoutWeekMonth = findViewById(R.id.layout_week_marker)
        txtMarker = findViewById(R.id.txt_marker_graph)
        txtCurrentWeekMarker = findViewById(R.id.txt_current_week)
        txtLastWeekMarker = findViewById(R.id.txt_last_week)
        txtMarker.visibility = GONE
        layoutWeekMonth.visibility = VISIBLE
        this.dayValuesLastWeek = dayValuesLastWeek
        this.dayValues = dayValues
        this.entriesLastWeek = entriesLastWeek
        this.entriesCurrentWeek = entriesCurrentWeek
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        super.refreshContent(e, highlight)
        total = e.y.toDouble()
        val priceDetails = PriceDetails(total)
        totalString = priceDetails.displayValue
        totalLastWeek = entriesLastWeek[e.x.toInt()].y.toDouble()
        val priceDetailLastWeek = PriceDetails(totalLastWeek)
        lastWeekValue = priceDetailLastWeek.displayValue
        totalCurrentWeek = entriesCurrentWeek[e.x.toInt()].y.toDouble()
        val priceDetailsCurrentWeek = PriceDetails(totalCurrentWeek)
        currentWeekValue = priceDetailsCurrentWeek.displayValue
        if (totalString == currentWeekValue) {
            txtCurrentWeekMarker.text = dayValues[e.x.toInt()] + ": " + totalString
            txtLastWeekMarker.text = dayValuesLastWeek[e.x.toInt()] + ": " + lastWeekValue
        } else {
            txtCurrentWeekMarker.text = dayValues[e.x.toInt()] + ": " + currentWeekValue
            txtLastWeekMarker.text = dayValuesLastWeek[e.x.toInt()] + ": " + totalString
        }
        if (e.x < 2) {
            val f = (width / 6.7).toFloat()
            mOffset = MPPointF(-f, -(height * 3).toFloat())
            layoutWeekMonth.setBackgroundResource(R.drawable.marker_right)
        } else if (e.x > 5) {
            //timestamps is an array containing xValues timestamps
            val f = (width / 1.15).toFloat()
            mOffset = MPPointF(-f, -(height * 3).toFloat())
            layoutWeekMonth.setBackgroundResource(R.drawable.marker_left)
        } else {
            mOffset = MPPointF(-(width / 2).toFloat(), -(height * 3).toFloat())
            layoutWeekMonth.setBackgroundResource(R.drawable.marker_center)
        }
        setTypefaceView(txtMarker, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtCurrentWeekMarker,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtLastWeekMarker, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF(-(width / 2).toFloat(), -(height * 3).toFloat())
        }
        return mOffset!!
    }
}