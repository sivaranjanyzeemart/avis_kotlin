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
import zeemart.asia.buyers.helper.DateHelper.returnMonthYearString
import zeemart.asia.buyers.models.PriceDetails

/**
 * Created by Muthumari on 08/07/2019.
 */
class CustomMarkerViewMonthCharts(
    context: Context?,
    layoutResource: Int,
    dayValues: List<String>,
    entriesLastMonth: List<Entry>,
    entriesCurrentMonth: List<Entry>
) : MarkerView(context, layoutResource) {
    private val layoutWeekMonth: LinearLayout
    private val txtMarker: TextView
    private val txtCurrentMonthMarker: TextView
    private val txtLastMonthMarker: TextView
    private var mOffset: MPPointF? = null
    private val dayValues: List<String>
    private val entriesLastMonth: List<Entry>
    private val entriesCurrentMonth: List<Entry>
    private var total: Double? = null
    private var totalString: String? = null
    private var totalLastMonth: Double? = null
    private var lastMonthValue: String? = null
    private var totalCurrentMonth: Double? = null
    private var currentMonthValue: String? = null
    private var month: String? = null
    private var previousMonth: String? = null

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource      the layout resource to use for the MarkerView
     * @param dayValues
     * @param entriesLastMonth
     * @param entriesCurrentMonth
     */
    init {
        layoutWeekMonth = findViewById(R.id.layout_week_marker)
        txtMarker = findViewById(R.id.txt_marker_graph)
        txtCurrentMonthMarker = findViewById(R.id.txt_current_week)
        txtLastMonthMarker = findViewById(R.id.txt_last_week)
        txtMarker.visibility = GONE
        layoutWeekMonth.visibility = VISIBLE
        this.dayValues = dayValues
        this.entriesLastMonth = entriesLastMonth
        this.entriesCurrentMonth = entriesCurrentMonth
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        super.refreshContent(e, highlight)
        total = e.y.toDouble()
        val priceDetails = PriceDetails(total)
        totalString = priceDetails.displayValue
        if (entriesLastMonth.size > 30 && entriesLastMonth[e.x.toInt()].x.toDouble() == LAST_DATE) {
            totalLastMonth = entriesLastMonth[e.x.toInt()].y.toDouble()
            val priceDetailsLastMonth = PriceDetails(totalLastMonth)
            lastMonthValue = priceDetailsLastMonth.displayValue
            previousMonth = returnMonthYearString(1).substring(0, 3)
            txtCurrentMonthMarker.text =
                dayValues[e.x.toInt()] + " " + previousMonth + ": " + totalString
            txtLastMonthMarker.visibility = GONE
        } else if (entriesCurrentMonth.size > 30 && entriesCurrentMonth[e.x.toInt()].x.toDouble() == LAST_DATE) {
            totalCurrentMonth = entriesCurrentMonth[e.x.toInt()].y.toDouble()
            val priceDetailsCurrentMonth = PriceDetails(totalCurrentMonth)
            currentMonthValue = priceDetailsCurrentMonth.displayValue
            month = returnMonthYearString(0).substring(0, 3)
            txtCurrentMonthMarker.text = dayValues[e.x.toInt()] + " " + month + ": " + totalString
            txtLastMonthMarker.visibility = GONE
        } else {
            totalLastMonth = entriesLastMonth[e.x.toInt()].y.toDouble()
            val priceDetailsLastMonth = PriceDetails(totalLastMonth)
            lastMonthValue = priceDetailsLastMonth.displayValue
            totalCurrentMonth = entriesCurrentMonth[e.x.toInt()].y.toDouble()
            val priceDetailsCurrent = PriceDetails(totalCurrentMonth)
            currentMonthValue = priceDetailsCurrent.displayValue
            month = returnMonthYearString(0).substring(0, 3)
            previousMonth = returnMonthYearString(1).substring(0, 3)
            txtLastMonthMarker.visibility = VISIBLE
            if (totalString == currentMonthValue) {
                txtCurrentMonthMarker.text =
                    dayValues[e.x.toInt()] + " " + month + ": " + totalString
                txtLastMonthMarker.text =
                    dayValues[e.x.toInt()] + " " + previousMonth + ": " + lastMonthValue
            } else {
                txtCurrentMonthMarker.text =
                    dayValues[e.x.toInt()] + " " + month + ": " + currentMonthValue
                txtLastMonthMarker.text =
                    dayValues[e.x.toInt()] + " " + previousMonth + ": " + totalString
            }
        }
        if (e.x < 3) {
            val f = (width / 6.7).toFloat()
            mOffset = MPPointF(-f, -(height * 3).toFloat())
            layoutWeekMonth.setBackgroundResource(R.drawable.marker_right)
        } else if (e.x > 26) {
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
            txtCurrentMonthMarker,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtLastMonthMarker, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF(-(width / 2).toFloat(), -(height * 3).toFloat())
        }
        return mOffset!!
    }

    companion object {
        //if any month has 31 days -  get x axis value
        private const val LAST_DATE = 30.0
    }
}