package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.ProductAttributeModel

/**
 * Created by ParulBhandari on 1/25/2018.
 */
class ProductDetailsAttributeListAdapter(
    private val context: Context,
    private val dataList: List<ProductAttributeModel>
) : RecyclerView.Adapter<ProductDetailsAttributeListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_product_detail_attribute_list,
            parent,
            false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtAttributeName.text = dataList[position].specificAttribute
        var attributeValue = ""
        val attributeValues = dataList[position].specificAttributeValue
        for (i in attributeValues!!.indices) {
            attributeValue = attributeValue + attributeValues[i] + ","
        }
        if (attributeValue.length > 0) {
            val attributeValuesAll = attributeValue.substring(0, attributeValue.length - 1)
            holder.txtAttributeValue.text = attributeValuesAll
        } else {
            holder.txtAttributeValue.text = ""
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtAttributeName: TextView
        var txtAttributeValue: TextView

        init {
            txtAttributeName = itemView.findViewById(R.id.attributeName)
            setTypefaceView(txtAttributeName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            txtAttributeValue = itemView.findViewById(R.id.attributeValue)
            setTypefaceView(txtAttributeValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }
}