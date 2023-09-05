package zeemart.asia.buyers.orders.createorders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimport.CartSelectedOrdersBreakdownListUI

internal class SupplierAndDeliveryDateListAdapter constructor(
    private val context: Context,
    lstSupplierWithHeaders: List<CartSelectedOrdersBreakdownListUI>,
    supplierIDDetailMap: Map<String, DetailSupplierDataModel>?
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val HEADER_TYPE: Int = 1
    private val SUPPLIER_TYPE: Int = 2
    private val lstSupplierWithHeaders: List<CartSelectedOrdersBreakdownListUI>
    var supplierIDDetailMap: Map<String, DetailSupplierDataModel>?

    init {
        this.lstSupplierWithHeaders = lstSupplierWithHeaders
        this.supplierIDDetailMap = supplierIDDetailMap
    }

    public override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == HEADER_TYPE) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_total_breakdown_supplier_list_header, parent, false)
            return ViewHolderHeader(v)
        } else {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_total_breakdown_supplier_list_row, parent, false)
            return ViewHolderSupplier(v)
        }
    }

    public override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ViewHolderHeader) {
            viewHolder.txtDeliveryDate.setText(
                lstSupplierWithHeaders.get(position).deliveryDate
            )
        } else {
            val viewHolderSupplier: ViewHolderSupplier = viewHolder as ViewHolderSupplier
            val order: Orders? = lstSupplierWithHeaders.get(position).order
            if (order?.isAllow == true) {
                val supplierDataModel: DetailSupplierDataModel? =
                    supplierIDDetailMap!!.get(order.supplier?.supplierId)
                if (!UserPermission.HasViewPrice()) {
                    viewHolderSupplier.warningIcon.setVisibility(View.GONE)
                } else {
                    if ((supplierDataModel != null) && (supplierDataModel.deliveryFeePolicy != null) && (
                                supplierDataModel.deliveryFeePolicy!!.minOrder != null) && (
                                order.amount?.subTotal?.amount!! < supplierDataModel.deliveryFeePolicy!!.minOrder?.amount!!)
                    ) {
                        //display warning icon and text in yellow
                        if (!order.isAddOn) {
                            viewHolderSupplier.warningIcon.setVisibility(View.VISIBLE)
                            viewHolderSupplier.warningIcon.setImageResource(R.drawable.warning_yellow)
                            viewHolderSupplier.txtAmount.setTextColor(
                                context.getResources().getColor(
                                    R.color.squash
                                )
                            )
                        } else {
                            viewHolderSupplier.warningIcon.setVisibility(View.GONE)
                            viewHolderSupplier.txtAmount.setTextColor(
                                context.getResources().getColor(
                                    R.color.grey_medium
                                )
                            )
                        }
                    } else {
                        viewHolderSupplier.warningIcon.setVisibility(View.GONE)
                        viewHolderSupplier.txtAmount.setTextColor(
                            context.getResources().getColor(R.color.grey_medium)
                        )
                    }
                }
            } else {
                if (!order?.isAddOn!!) {
                    viewHolderSupplier.warningIcon.setVisibility(View.VISIBLE)
                    viewHolderSupplier.warningIcon.setImageResource(R.drawable.warning_red)
                    viewHolderSupplier.txtAmount.setTextColor(
                        context.getResources().getColor(R.color.pinky_red)
                    )
                } else {
                    viewHolderSupplier.warningIcon.setVisibility(View.GONE)
                    viewHolderSupplier.txtAmount.setTextColor(
                        context.getResources().getColor(R.color.grey_medium)
                    )
                }
            }
            viewHolderSupplier.txtSupplierName.setText(order.supplier?.supplierName)
            viewHolderSupplier.txtAmount.setText(order.amount?.total?.displayValue)
        }
    }

    public override fun getItemCount(): Int {
        return lstSupplierWithHeaders.size
    }

    public override fun getItemViewType(position: Int): Int {
        if (lstSupplierWithHeaders.get(position).isDeliveryDateHeader) {
            return HEADER_TYPE
        } else {
            return SUPPLIER_TYPE
        }
    }

    inner class ViewHolderHeader constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDeliveryDate: TextView

        init {
            txtDeliveryDate =
                itemView.findViewById<TextView>(R.id.txt_delivery_date_breakdown_supplier_list)
            ZeemartBuyerApp.setTypefaceView(
                txtDeliveryDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    inner class ViewHolderSupplier constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSupplierName: TextView
        val txtAmount: TextView
        val warningIcon: ImageView

        init {
            txtSupplierName =
                itemView.findViewById<TextView>(R.id.txt_supplierName_breakdown_supplier_list)
            txtAmount =
                itemView.findViewById<TextView>(R.id.txt_total_amount_breakdown_supplier_list)
            warningIcon = itemView.findViewById(R.id.img_warning_breakdown_supplier_list)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtAmount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }
}