package zeemart.asia.buyers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.inventory.InventoryProduct

/**
 * Created by RajPrudhviMarella on 01/June/2021.
 */
class InventoryUomAdapter(private val shelveProductList: List<InventoryProduct>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_inventory_uom_changes_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holderItem = holder as ViewHolder
        holderItem.txtQuantity.text = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
            shelveProductList[position].unitSize
        )
        holderItem.txtSupplierName.text = shelveProductList[position].supplier?.supplierName
        holderItem.txtItemName.text = shelveProductList[position].productName

        if (!StringHelper.isStringNullOrEmpty(shelveProductList[position].conversionReview) &&
            ((shelveProductList[position].conversionReview == ApiParamsHelper.ConversionReview.INVENTORY_UOM_UPDATE.value) ||
                    (shelveProductList[position].conversionReview) == ApiParamsHelper.ConversionReview.NON_INVENTORY_UOM_UPDATE.value))
        {
            val UOM = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                shelveProductList[position].lastupdatedData?.stockUnitQtyDisplayValue + " " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                    shelveProductList[position].lastupdatedData?.stockUnit
                ) + " = " + shelveProductList[position].lastupdatedData?.orderByUnitQtyDisplayValue + " " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                    shelveProductList[position].lastupdatedData?.orderByUnit
                )
            )
            holderItem.txtUOM.visibility = View.VISIBLE
            holderItem.txtUOM.text = UOM
        } else {
            holderItem.txtUOM.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return shelveProductList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtSupplierName: TextView
        var txtQuantity: TextView
        var txtUOM: TextView

        init {
            txtItemName = itemView.findViewById(R.id.inventory_product_txt_product_name)
            txtSupplierName = itemView.findViewById(R.id.inventory_product_txt_supplier_name)
            txtQuantity = itemView.findViewById(R.id.inventory_product_txt_quantity)
            txtUOM = itemView.findViewById(R.id.inventory_product_txt_uom)
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }
}