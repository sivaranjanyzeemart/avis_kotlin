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
import zeemart.asia.buyers.helper.HeaderItemDecoration
import zeemart.asia.buyers.interfaces.OnTotalSpendingItemClick
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse
import zeemart.asia.buyers.models.reportsimportimport.PriceUpdateListWithStickyHeaderUI

/**
 * Created by ParulBhandari on 1/25/2018.
 */
class AllPriceUpdateListAdapter(
    private val context: Context,
    private val priceUpdateData: ArrayList<PriceUpdateListWithStickyHeaderUI>,
    private val listener: OnTotalSpendingItemClick,
) : RecyclerView.Adapter<AllPriceUpdateListAdapter.ViewHolder>(),
    HeaderItemDecoration.StickyHeaderInterface {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == ITEM_TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lst_notification_header, parent, false)
            return ViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_report_summary_price_update_list_row, parent, false)
            return ViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = priceUpdateData[position]
        if (item.isHeader) {
            val header = holder as ViewHolderHeader?
            val headerDayData = "(" + item.headerData?.day?.trim { it <= ' ' } + ")"
            header!!.txtDay.text = headerDayData.trim { it <= ' ' }
            header.txtDate.text = item.headerData?.date?.trim { it <= ' ' }
        } else {
            val priceUpdateItem = holder as ViewHolder?
            priceUpdateItem!!.txtProductName.text =
                priceUpdateData.get(position).skuPriceData.productName
            priceUpdateItem.txtSupplierName.text =
                priceUpdateData.get(position).skuPriceData.supplier?.supplierName
            //            Long amount = priceUpdateData.get(position).getSkuPriceData().getTotalUnitPrice().getAmount();
//            double amountDollar = ZeemartBuyerApp.centsToDollar(amount);
//            String unitPrice = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(amountDollar,2));
//            priceUpdateItem.txtUnitPrice.setText(unitPrice+"/"+ UnitSizeModel.returnShortNameValueUnitSize(priceUpdateData.get(position).getSkuPriceData().getUnitSize()));
            val uom = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                priceUpdateData[position].skuPriceData.unitSize
            )
            val priceWithUom =
                priceUpdateData[position].skuPriceData.unitPrice?.getDisplayValueWithUomNoSpacing(uom)
            priceUpdateItem.txtUnitPrice.text = priceWithUom
            if (priceUpdateData[position].skuPriceData.prevUnitPrice != null && priceUpdateData[position].skuPriceData.unitPrice != null) {
                val priceChangeLongValue: Double =
                    priceUpdateData[position].skuPriceData.unitPrice?.amount?.minus(priceUpdateData[position].skuPriceData.prevUnitPrice
                                !!.amount!!)!!.toDouble()

                if (priceChangeLongValue!! < 0) {
                    priceUpdateItem.txtPriceVariation.setTextColor(context.resources.getColor(R.color.green))
                    val percentageChange = calculatePercentageChange(
                        priceUpdateData[position].skuPriceData
                    )
                    val twodecimalPlacesPercentage =
                        Math.abs(ZeemartBuyerApp.getPriceDouble(percentageChange as Double, 2))
                    val calculatedPercentage =
                        ZeemartBuyerApp.getTwoDecimalQuantity(twodecimalPlacesPercentage)

//                    Double priceChangeValDouble = ZeemartBuyerApp.centsToDollar(Math.abs(priceChangeLongValue));
//                    String priceChangeValue = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(priceChangeValDouble,2));
                    val priceChangeValue = PriceDetails(priceChangeLongValue).displayValue
                    priceUpdateItem.txtPriceVariation.text =
                        "$priceChangeValue($calculatedPercentage%)"
                } else if (priceChangeLongValue > 0) {
                    priceUpdateItem.txtPriceVariation.setTextColor(context.resources.getColor(R.color.pinky_red))
                    val percentageChange = calculatePercentageChange(
                        priceUpdateData[position].skuPriceData
                    )
                    val twodecimalPlacesPercentage =
                        Math.abs(ZeemartBuyerApp.getPriceDouble(percentageChange as Double, 2))
                    val calculatedPercentage =
                        ZeemartBuyerApp.getTwoDecimalQuantity(twodecimalPlacesPercentage)

//                    Double priceChangeValDouble = ZeemartBuyerApp.centsToDollar(Math.abs(priceChangeLongValue));
//                    String priceChangeValue = ZeemartBuyerApp.getDollarPriceString(ZeemartBuyerApp.getPriceDouble(priceChangeValDouble,2));
                    val priceChangeValue = PriceDetails(priceChangeLongValue).displayValue
                    priceUpdateItem.txtPriceVariation.text =
                        "+$priceChangeValue($calculatedPercentage%)"
                } else {
                    priceUpdateItem.txtPriceVariation.setTextColor(context.resources.getColor(R.color.faint_grey))
                    priceUpdateItem.txtPriceVariation.text = "$0.0(0%)"
                }
            } else {
                priceUpdateItem.txtPriceVariation.setTextColor(context.resources.getColor(R.color.faint_grey))
                priceUpdateItem.txtPriceVariation.text = "$0.0(0%)"
            }
            priceUpdateItem.lytRow.setOnClickListener(View.OnClickListener {
                listener.onItemClick(
                    priceUpdateData[position].skuPriceData.sku,
                    priceUpdateData[position].skuPriceData.productName
                )
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item = priceUpdateData[position]
        return if (item.isHeader) {
            ITEM_TYPE_HEADER
        } else {
            ITEM_TYPE_ROW
        }
    }

    override fun getItemCount(): Int {
        return priceUpdateData.size
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var itemPosition = itemPosition
        var headerPosition = 0
        do {
            if (isHeader(itemPosition)) {
                headerPosition = itemPosition
                break
            }
            itemPosition -= 1
        } while (itemPosition >= 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return R.layout.lst_notification_header
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        (header?.findViewById<View>(R.id.txt_list_header_date) as TextView).text =
            priceUpdateData.get(headerPosition).headerData?.date?.trim { it <= ' ' }
        val txtDay = "(" + priceUpdateData[headerPosition].headerData?.day?.trim { it <= ' ' } + ")"
        (header.findViewById<View>(R.id.txt_list_header_day) as TextView).text = txtDay
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return priceUpdateData[itemPosition].isHeader
    }

    override fun onHeaderClicked(header: View?, position: Int) {
        //do nothing
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtProductName: TextView
        var txtSupplierName: TextView
        var txtUnitPrice: TextView
        var txtPriceVariation: TextView
        var lytRow: ConstraintLayout

        init {
            txtProductName =
                itemView.findViewById(R.id.dashboard_report_week_price_update_item_name)
            txtSupplierName =
                itemView.findViewById(R.id.dashboard_report_week_price_update_supplier_name)
            txtUnitPrice =
                itemView.findViewById(R.id.dashboard_report_week_price_update_item_unit_price)
            txtPriceVariation =
                itemView.findViewById(R.id.dashboard_report_week_price_update_item_price_variation)
            lytRow = itemView.findViewById(R.id.lst_price_update_dashboard_row)
            ZeemartBuyerApp.setTypefaceView(
                txtProductName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtUnitPrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtPriceVariation,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    private fun calculatePercentageChange(data: PriceUpdateModelBaseResponse.PriceDetailModel): Any {
        if (data.prevUnitPrice != null && data.unitPrice != null) {
            val priceDifference: Double =
                data.unitPrice!!.amount?.minus(data.prevUnitPrice!!.amount!!)!!

            val pricePrevDouble = data.prevUnitPrice!!.amount as Double
            val percenatageChange = (priceDifference?.div(pricePrevDouble))?.times(100)
            if (!java.lang.Double.isInfinite(percenatageChange))
                return percenatageChange
        }
        return 0
    }

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView
        var txtDay: TextView

        init {
            txtDate = itemView.findViewById(R.id.txt_list_header_date)
            txtDay = itemView.findViewById(R.id.txt_list_header_day)
            ZeemartBuyerApp.setTypefaceView(
                txtDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtDay,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    companion object {
        private val ITEM_TYPE_HEADER = 0
        private val ITEM_TYPE_ROW = 1
    }
}