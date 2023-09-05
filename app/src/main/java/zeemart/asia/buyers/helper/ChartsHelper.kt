package zeemart.asia.buyers.helper

import android.graphics.Typeface
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.PriceDetails

/**
 * Created by saiful on 28/3/18.
 */
object ChartsHelper {
    fun getAllDatesForChartInPeriod(start: Long, end: Long): List<ChartModel> {
        val datesList: MutableList<ChartModel> = ArrayList()
        val dateRangeInPeriod = DateHelper.getAllDatesInPeriod(start, end)
        for (i in dateRangeInPeriod.indices) {
            datesList.add(ChartModel(PriceDetails(), dateRangeInPeriod[i]))
        }
        return datesList
    }

    fun lineChartUiSetup(chart: LineChart) {
        val d = Description()
        d.text = ""
        chart.description = d
        chart.setScaleEnabled(false)
        chart.legend.isEnabled = false
        chart.setDrawGridBackground(false)
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        val yAxisRight = chart.axisRight
        yAxisRight.setDrawAxisLine(false)
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawGridLines(false)
        val yAxisLeft = chart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.granularity = 100f
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.valueFormatter = ChartYAxisValueFormatter()
        val legend = chart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.SQUARE
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 10f
        val typeface = Typeface.createFromAsset(
            chart.context.assets,
            "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        legend.typeface = typeface
        legend.xEntrySpace = 10f
        legend.xOffset = -10f
    }

    class ChartModel {
        @SerializedName("totalPrice")
        @Expose
        var totalPrice: PriceDetails? = null

        @SerializedName("invoiceDate")
        @Expose
        var invoiceDate: Long? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        constructor() {}
        constructor(totalPrice: PriceDetails?, invoiceDate: Long?) {
            this.totalPrice = totalPrice
            this.invoiceDate = invoiceDate
        }

        constructor(totalPrice: PriceDetails?, name: String?) {
            this.totalPrice = totalPrice
            this.name = name
        }

//        fun getInvoiceDate(): Long? {
//            return if (invoiceDate == 0L) null else invoiceDate
//        }
//
//        fun setInvoiceDate(invoiceDate: Long?) {
//            this.invoiceDate = invoiceDate
//        }
    }

    class ChartXAxisValueFormatter(private val mValues: Array<String>) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return if (mValues.size > value.toInt() && value >= 0) {
                mValues[value.toInt()]
            } else ""
        }
        /** this is only needed if numbers are returned, else return 0  */ //public int getDecimalDigits() { return 0; }
    }

    class ChartYAxisValueFormatter : IValueFormatter, IAxisValueFormatter {
        private var mText = ""

        constructor() {}

        /**
         * Creates a formatter that appends a specified text to the result string
         *
         * @param appendix a text that will be appended
         */
        constructor(appendix: String) {
            mText = appendix
        }

        // IValueFormatter
        override fun getFormattedValue(
            value: Float,
            entry: Entry,
            dataSetIndex: Int,
            viewPortHandler: ViewPortHandler
        ): String {
            return makePretty(value.toDouble()) + mText
        }

        // IAxisValueFormatter
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return makePretty(value.toDouble()) + mText
        }

        private fun makePretty(price: Double): String {
            val priceDetails = PriceDetails(price)
            return priceDetails.displayChartValue
        }
    }
}