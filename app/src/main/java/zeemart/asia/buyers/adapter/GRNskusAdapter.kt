package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.GRNskuSelectionDialog
import zeemart.asia.buyers.helper.GRNskuSelectionDialog.GRNskuSelectionDialogChangeListener
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.marketlist.Product

/**
 * Created by RajPrudhviMarella on 10/June/2021.
 */
class GRNskusAdapter(
    private val context: Context,
    private val products: MutableList<Product>,
    private val mListener: SelectedSKUSListener
) : RecyclerView.Adapter<GRNskusAdapter.ViewHolder>(), GRNskuSelectionDialogChangeListener {
    private lateinit var dialog: CustomChangeQuantityDialog
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_grn_sku_details, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && products[position].customName != null
        ) {
            holder.txtItemName.text = products[position].customName
            holder.txtProductCustomName.text = products[position].productName
            holder.txtProductCustomName.visibility = View.VISIBLE
        } else {
            holder.txtItemName.text = products[position].productName
            holder.txtProductCustomName.visibility = View.GONE
        }
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && products[position].customProductCode != null
        ) {
            holder.txtProdCode.text = products[position].customProductCode
        } else {
            holder.txtProdCode.text = products[position].supplierProductCode
        }
        holder.edtProductQuantity.text = products[position].quantity.toString() + ""
        holder.txtUnitSize.text = " " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(products[position].unitSize)
        holder.imgSelected.setOnClickListener { }
        holder.imgSelected.setOnClickListener {
            if (products[position].isSelected) {
                holder.imgSelected.setImageDrawable(context.resources.getDrawable(R.drawable.icon_un_select))
                products[position].isSelected = false
                holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.black))
                holder.lytRow.setBackgroundColor(context.resources.getColor(R.color.white))
                holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.dark_grey))
                holder.txtIndicator.visibility = View.GONE
                mListener.onSKUdeSelected()
            } else {
                holder.imgSelected.setImageDrawable(context.resources.getDrawable(R.drawable.icon_select))
                holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.color_azul_two))
                holder.lytRow.setBackgroundColor(context.resources.getColor(R.color.faint_bg_grey))
                holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.color_azul_two))
                holder.txtIndicator.visibility = View.VISIBLE
                products[position].isSelected = true
                mListener.onSKUSelected()
            }
        }
        if (products[position].isSelected) {
            holder.imgSelected.setImageDrawable(context.resources.getDrawable(R.drawable.icon_select))
            holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.color_azul_two))
            holder.lytRow.setBackgroundColor(context.resources.getColor(R.color.faint_bg_grey))
            holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.color_azul_two))
            holder.txtIndicator.visibility = View.VISIBLE
        } else {
            holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.black))
            holder.lytRow.setBackgroundColor(context.resources.getColor(R.color.white))
            holder.txtUnitSize.setTextColor(context.resources.getColor(R.color.dark_grey))
            holder.txtIndicator.visibility = View.GONE
            holder.imgSelected.setImageDrawable(context.resources.getDrawable(R.drawable.icon_un_select))
        }
        holder.lytRow.setOnClickListener {
            if (dialog == null || dialog != null && !dialog!!.isShowing) {
                val createOrderHelperDialog = GRNskuSelectionDialog(
                    context, products[position], this@GRNskusAdapter
                )
                dialog = if (products[position].isCustom) {
                    createOrderHelperDialog.createCustomeSKUChangeQuantityDialog(true)
                } else {
                    createOrderHelperDialog.createChangeQuantityDialog(true)
                }
                dialog.getWindow()!!
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onDataChange(product: Product?, isChanged: Boolean) {
        if (isChanged) {
            if (product?.isSelected!!) {
                mListener.onSKUSelected()
            } else {
                mListener.onSKUdeSelected()
            }
        }
        notifyDataSetChanged()
    }

    override fun requestForRemove(product: Product?) {
        for (i in products.indices) {
            if (products[i].productName == product?.productName) {
                if (products[i].isSelected) {
                    mListener.onSKUdeSelected()
                }
                products.remove(products[i])
            }
        }
        notifyDataSetChanged()
    }

    fun removeAt(adapterPosition: Int) {
        if (products[adapterPosition].isSelected) {
            mListener.onSKUdeSelected()
        }
        products.remove(products[adapterPosition])
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var edtProductQuantity: TextView
        var txtUnitSize: TextView
        var txtIndicator: TextView
        var txtProductCustomName: TextView
        var txtProdCode: TextView
        var imgSelected: ImageView
        var lytRow: ConstraintLayout

        init {
            txtUnitSize = itemView.findViewById(R.id.inventory_product_txt_uom)
            txtItemName = itemView.findViewById(R.id.inventory_product_txt_product_name)
            edtProductQuantity = itemView.findViewById(R.id.inventory_product_txt_quantity)
            txtProductCustomName = itemView.findViewById(R.id.inventory_product_txt_supplier_name)
            txtProdCode = itemView.findViewById(R.id.inventory_product_txt_supplier_code)
            imgSelected = itemView.findViewById(R.id.img_select_skus)
            lytRow = itemView.findViewById(R.id.lyt_row_grn_sku)
            txtIndicator = itemView.findViewById(R.id.dashboard_text_draft_indicator)
            setTypefaceView(
                edtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtUnitSize, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtProdCode, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtProductCustomName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
        }
    }

    fun selectAllProducts(isSelectAllClicked: Boolean) {
        for (i in products.indices) {
            if (isSelectAllClicked) {
                if (!products[i].isSelected) {
                    products[i].isSelected = true
                    mListener.onSKUSelected()
                }
            } else {
                if (products[i].isSelected) {
                    products[i].isSelected = false
                    mListener.onSKUdeSelected()
                }
            }
        }
        notifyDataSetChanged()
    }

    interface SelectedSKUSListener {
        fun onSKUSelected()
        fun onSKUdeSelected()
    }
}