package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse

/**
 * Created by ParulBhandari on 1/25/2018.
 */
class PriceUpdateReportSummaryListAdapter(
    private val context: Context,
    priceUpdateDataReceived: List<PriceUpdateModelBaseResponse.PriceDetailModel>,
    listener: OnTotalSpendingItemClick
) : RecyclerView.Adapter<PriceUpdateReportSummaryListAdapter.ViewHolder?>() {
    private val priceUpdateData: MutableList<PriceUpdateModelBaseResponse.PriceDetailModel> = ArrayList<PriceUpdateModelBaseResponse.PriceDetailModel>()
    private val listener: OnTotalSpendingItemClick
    private val noOfEntriesTobeShown = 5

    init {
        priceUpdateData.clear()
        if (priceUpdateData.size > noOfEntriesTobeShown) {
            for (i in 0 until noOfEntriesTobeShown) {
                priceUpdateData.add(priceUpdateDataReceived[i])
            }
        } else {
            priceUpdateData.addAll(priceUpdateDataReceived)
        }
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext()).inflate(
            R.layout.layout_report_summary_price_update_list_row,
            parent,
            false
        )
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        val size = priceUpdateData.size
        // Return at most 5 items from the ArrayList
        return if (size > 5) 5 else size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtProductName.setText(priceUpdateData[position].productName)
        holder.txtSupplierName.setText(priceUpdateData[position].supplier?.supplierName)
        val invoicedate: String =
            DateHelper.getDateInDateMonthFormat(priceUpdateData[position].invoiceDate)
        holder.dashboard_report_week_price_update_item_date.setText(invoicedate)
        //  System.out.printf("date" + iteminvoicedate);
//        Long amount = priceUpdateData.get(position).getTotalUnitPrice().getAmount();
//        double amountDollar = ZeemartBuyerApp.centsToDollar(amount);
//        String unitPrice = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(amountDollar,2));
//        holder.txtUnitPrice.setText(unitPrice+"/"+ UnitSizeModel.returnShortNameValueUnitSize(priceUpdateData.get(position).getUnitSize()));
        val uom: String? = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
            priceUpdateData[position].unitSize
        )
        val priceWithUom: String? =
            priceUpdateData[position].unitPrice?.getDisplayValueWithUomNoSpacing(uom)
        holder.txtUnitPrice.setText(priceWithUom)
        if (priceUpdateData[position].prevUnitPrice != null && priceUpdateData[position].unitPrice != null) {
            val priceChangeValue: Double? = priceUpdateData[position].unitPrice?.amount?.minus(
                    priceUpdateData[position].prevUnitPrice?.amount!!)
            if (priceChangeValue!! < 0) {
                holder.txtPriceVariation.setTextColor(context.resources.getColor(R.color.green))
                val percentageChange = calculatePercentageChange(priceUpdateData[position])
                val twodecimalPlacesPercentage: Double =
                    Math.abs(ZeemartBuyerApp.getPriceDouble(percentageChange as Double, 2))
                val calculatedPercentage: String =
                    ZeemartBuyerApp.getTwoDecimalQuantity(twodecimalPlacesPercentage)

//                Double priceChangeValDouble = ZeemartBuyerApp.centsToDollar(Math.abs(priceChenageValue));
//                String priceChangeValue = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(priceChangeValDouble,2));
                val priceDetails = PriceDetails(priceChangeValue)
                val priceChangedValue: String = priceDetails.displayValue
                holder.txtPriceVariation.setText("$priceChangedValue($calculatedPercentage%)")
            } else if (priceChangeValue > 0) {
                holder.txtPriceVariation.setTextColor(context.resources.getColor(R.color.pinky_red))
                val percentageChange = calculatePercentageChange(priceUpdateData[position])
                val twodecimalPlacesPercentage: Double =
                    Math.abs(ZeemartBuyerApp.getPriceDouble(percentageChange as Double, 2))
                val calculatedPercentage: String =
                    ZeemartBuyerApp.getTwoDecimalQuantity(twodecimalPlacesPercentage)

//                Double priceChangeValDouble = ZeemartBuyerApp.centsToDollar(Math.abs(priceChenageValue));
//                String priceChangeValue = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(priceChangeValDouble,2));
                val priceDetails = PriceDetails(priceChangeValue)
                val priceChangedValue: String = priceDetails.displayValue
                holder.txtPriceVariation.setText("+$priceChangedValue($calculatedPercentage%)")
            } else {
                holder.txtPriceVariation.setTextColor(context.resources.getColor(R.color.faint_grey))
                holder.txtPriceVariation.setText("$0.0(0%)")
            }
        } else {
            holder.txtPriceVariation.setTextColor(context.resources.getColor(R.color.faint_grey))
            holder.txtPriceVariation.setText("$0.0(0%)")
        }
        holder.lytRow.setOnClickListener(View.OnClickListener {
            listener.onItemClick(
                priceUpdateData[position].sku, priceUpdateData[position].productName
            )
        })
    }

    //show only top 5 price updates for dashboard week
    /* return priceUpdateData.size();*/
//    val itemCount:
//    // Return at most 5 items from the ArrayList
//            Int
//        get() {
//            //show only top 5 price updates for dashboard week
//            /* return priceUpdateData.size();*/
//            val size = priceUpdateData.size
//            // Return at most 5 items from the ArrayList
//            return if (size > 5) 5 else size
//        }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtProductName: TextView
        var txtSupplierName: TextView
        var txtUnitPrice: TextView
        var txtPriceVariation: TextView
        var dashboard_report_week_price_update_item_date: TextView
        var lytRow: ConstraintLayout

        init {
            txtProductName =
                itemView.findViewById<TextView>(R.id.dashboard_report_week_price_update_item_name)
            txtSupplierName =
                itemView.findViewById<TextView>(R.id.dashboard_report_week_price_update_supplier_name)
            txtUnitPrice =
                itemView.findViewById<TextView>(R.id.dashboard_report_week_price_update_item_unit_price)
            txtPriceVariation =
                itemView.findViewById<TextView>(R.id.dashboard_report_week_price_update_item_price_variation)
            dashboard_report_week_price_update_item_date =
                itemView.findViewById<TextView>(R.id.dashboard_report_week_price_update_item_date)
            lytRow = itemView.findViewById<ConstraintLayout>(R.id.lst_price_update_dashboard_row)

            //setFonts
            ZeemartBuyerApp.setTypefaceView(
                txtProductName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtUnitPrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                dashboard_report_week_price_update_item_date,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtPriceVariation,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    private fun calculatePercentageChange(data: PriceUpdateModelBaseResponse.PriceDetailModel): Any {
        if (data.prevUnitPrice != null && data.unitPrice != null) {
            val priceDifference: Double? =
                data.unitPrice!!.amount?.minus(data.prevUnitPrice?.amount!!)
            val pricePrevDouble = data.prevUnitPrice?.amount as Double
            val percenatageChange = pricePrevDouble?.times(100)?.let { priceDifference?.div(it) }
            if (!java.lang.Double.isInfinite(percenatageChange!!)) return percenatageChange
        }
        return 0
    }
}