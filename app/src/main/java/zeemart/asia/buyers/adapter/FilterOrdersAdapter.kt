package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.DeliveryStatus.DeliveryStatusWithFilter
import zeemart.asia.buyers.models.OrderStatus.OrderStatusWithFilter
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.marketlist.DetailSupplierMgr
import zeemart.asia.buyers.models.order.Orders

/**
 * Created by ParulBhandari on 1/2/2018.
 */
class FilterOrdersAdapter(
    private val context: Context,
    private val calledFrom: String,
    passedList: List<*>?,
    private val listener: SelectedFiltersListener
) : RecyclerView.Adapter<FilterOrdersAdapter.ViewHolder>() {
    init {
        if (calledFrom == ZeemartAppConstants.CALLED_FOR_OUTLET) {
            outletList = passedList as List<OutletMgr>?
        } else if (calledFrom == ZeemartAppConstants.CALLED_FOR_SUPPLIER) {
            suppliersList = passedList as MutableList<DetailSupplierMgr>?
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_STATUS) {
            orderStatusList = passedList as List<OrderStatusWithFilter>?
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_DELIVERY_STATUS) {
            deliveryStatusList = passedList as List<DeliveryStatusWithFilter>?
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_INVOICE_STATUS) {
            invoiceStatusList = passedList as List<InvoiceStatusWithFilter>?
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_TYPE) {
            orderTypeList = passedList as List<OrderTypeWithFilter>?
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_filter_order_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (calledFrom == ZeemartAppConstants.CALLED_FOR_OUTLET) {
            holder.txtlstFilterName.text = outletList!![position].outletName
            holder.layoutFilterOrderRow.setOnClickListener {
                if (outletList!![position].isOutletSelected) {
                    holder.imgSelectFilter.visibility = View.GONE
                    outletList!![position].isOutletSelected = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    outletList!![position].isOutletSelected = true
                }
                listener.onFilterSelected()
            }
            if (outletList!![position].isOutletSelected) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        } else if (calledFrom == ZeemartAppConstants.CALLED_FOR_SUPPLIER) {
            holder.txtlstFilterName.text = suppliersList!![position].supplier.supplierName
            holder.layoutFilterOrderRow.setOnClickListener {
                if (suppliersList!![position].isSupplierSelected) {
                    holder.imgSelectFilter.visibility = View.GONE
                    suppliersList!![position].isSupplierSelected = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    suppliersList!![position].isSupplierSelected = true
                }
                listener.onFilterSelected()
            }
            if (suppliersList!![position].isSupplierSelected) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_STATUS) {
            //called from the order status
            if (orderStatusList!![position].orderStatus?.statusName == OrderStatus.PLACED.statusName) {
                holder.txtlstFilterName.text = context.getString(R.string.txt_placed)
            } else if (orderStatusList!![position].orderStatus?.statusName == OrderStatus.CANCELLED.statusName) {
                holder.txtlstFilterName.text = context.getString(R.string.txt_cancelled)
            } else if (orderStatusList!![position].orderStatus?.statusName == OrderStatus.REJECTED.statusName) {
                holder.txtlstFilterName.text = context.getString(R.string.txt_rejected)
            } else if (orderStatusList!![position].orderStatus?.statusName == OrderStatus.VOIDED.statusName) {
                holder.txtlstFilterName.text = context.getString(R.string.txt_voided)
            }
            holder.layoutFilterOrderRow.setOnClickListener {
                if (orderStatusList!![position].isFilterSelected) {
                    holder.imgSelectFilter.visibility = View.GONE
                    orderStatusList!![position].isFilterSelected = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    orderStatusList!![position].isFilterSelected = true
                }
                listener.onFilterSelected()
            }
            if (orderStatusList!![position].isFilterSelected) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_INVOICE_STATUS) {
            //called for deliveryStatus
            //called from the order status
            if (invoiceStatusList!![position].invoiceStatus?.statusName == Invoice.InvoiceStatus.NO_LINKED_INVOICE.statusName) {
                holder.txtlstFilterName.setText(R.string.txt_no_linked_invoiced)
            } else {
                holder.txtlstFilterName.setText(R.string.txt_Invoiced)
            }
            holder.layoutFilterOrderRow.setOnClickListener {
                if (invoiceStatusList!![position].isStatusSelectedForFilter) {
                    holder.imgSelectFilter.visibility = View.GONE
                    invoiceStatusList!![position].isStatusSelectedForFilter = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    invoiceStatusList!![position].isStatusSelectedForFilter = true
                }
                listener.onFilterSelected()
            }
            if (invoiceStatusList!![position].isStatusSelectedForFilter) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_TYPE) {
            //called for deliveryStatus
            //called from the order status
            if (orderTypeList!![position].orderType?.name == Orders.Type.DEAL.name) {
                holder.txtlstFilterName.setText(R.string.txt_deal)
            } else {
                holder.txtlstFilterName.setText(R.string.txt_my_essentials)
            }
            holder.layoutFilterOrderRow.setOnClickListener {
                if (orderTypeList!![position].isStatusSelectedForFilter) {
                    holder.imgSelectFilter.visibility = View.GONE
                    orderTypeList!![position].isStatusSelectedForFilter = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    orderTypeList!![position].isStatusSelectedForFilter = true
                }
                listener.onFilterSelected()
            }
            if (orderTypeList!![position].isStatusSelectedForFilter) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        } else {
            //called for deliveryStatus
            //called from the order status
            if (deliveryStatusList!![position].deliveryStatus?.statusName == DeliveryStatus.NOT_RECEIVED.statusName) {
                holder.txtlstFilterName.setText(R.string.txt_unmarked)
            } else {
                holder.txtlstFilterName.setText(R.string.txt_received)
            }
            holder.layoutFilterOrderRow.setOnClickListener {
                if (deliveryStatusList!![position].isStatusSelectedForFilter) {
                    holder.imgSelectFilter.visibility = View.GONE
                    deliveryStatusList!![position].isStatusSelectedForFilter = false
                } else {
                    holder.imgSelectFilter.visibility = View.VISIBLE
                    deliveryStatusList!![position].isStatusSelectedForFilter = true
                }
                listener.onFilterSelected()
            }
            if (deliveryStatusList!![position].isStatusSelectedForFilter) {
                holder.imgSelectFilter.visibility = View.VISIBLE
            } else {
                holder.imgSelectFilter.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return if (calledFrom == ZeemartAppConstants.CALLED_FOR_OUTLET) {
            outletList!!.size
        } else if (calledFrom == ZeemartAppConstants.CALLED_FOR_SUPPLIER) {
            if (suppliersList != null) {
                suppliersList!!.size
            } else 0
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_STATUS) {
            orderStatusList!!.size
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_INVOICE_STATUS) {
            invoiceStatusList!!.size
        } else if (calledFrom == ZeemartAppConstants.CALLED_FROM_FILTER_ORDER_TYPE) {
            orderTypeList!!.size
        } else {
            //called from delivery status
            deliveryStatusList!!.size
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutFilterOrderRow: RelativeLayout
        var txtlstFilterName: TextView
        var imgSelectFilter: ImageView

        init {
            txtlstFilterName = itemView.findViewById(R.id.txt_filter_name)
            setTypefaceView(txtlstFilterName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            imgSelectFilter = itemView.findViewById(R.id.img_select_filter)
            layoutFilterOrderRow = itemView.findViewById(R.id.lyt_filter_order_row)
        }
    }

    interface SelectedFiltersListener {
        fun onFilterSelected()
    }

    companion object {
        @JvmField
        var outletList: List<OutletMgr>? = null
        @JvmField
        var suppliersList: MutableList<DetailSupplierMgr>? = null
        @JvmField
        var orderStatusList: List<OrderStatusWithFilter>? = null
        @JvmField
        var deliveryStatusList: List<DeliveryStatusWithFilter>? = null
        @JvmField
        var invoiceStatusList: List<InvoiceStatusWithFilter>? = null
        @JvmField
        var orderTypeList: List<OrderTypeWithFilter>? = null
        @JvmStatic
        fun resetFilterOrderList() {
            if (deliveryStatusList != null) {
                for (i in deliveryStatusList!!.indices) {
                    deliveryStatusList!![i].isStatusSelectedForFilter = false
                }
            }
            if (outletList != null) {
                for (i in outletList!!.indices) {
                    outletList!![i].isOutletSelected = false
                }
            }
            if (suppliersList != null) {
                for (i in suppliersList!!.indices) {
                    suppliersList!![i].isSupplierSelected = false
                }
            }
            if (orderStatusList != null) {
                for (i in orderStatusList!!.indices) {
                    orderStatusList!![i].isFilterSelected = false
                }
            }
            if (invoiceStatusList != null) {
                for (i in invoiceStatusList!!.indices) {
                    invoiceStatusList!![i].isStatusSelectedForFilter = false
                }
            }
            if (orderTypeList != null) {
                for (i in orderTypeList!!.indices) {
                    orderTypeList!![i].isStatusSelectedForFilter = false
                }
            }
        }

        val isAnyFilterSelected: Boolean
            get() {
                var isAnyFilterSelected = false
                if (deliveryStatusList != null) {
                    for (i in deliveryStatusList!!.indices) {
                        if (deliveryStatusList!![i].isStatusSelectedForFilter) {
                            isAnyFilterSelected = true
                            break
                        }
                    }
                }
                if (invoiceStatusList != null) {
                    for (i in invoiceStatusList!!.indices) {
                        if (invoiceStatusList!![i].isStatusSelectedForFilter) {
                            isAnyFilterSelected = true
                            break
                        }
                    }
                }
                if (outletList != null) {
                    for (i in outletList!!.indices) {
                        if (outletList!![i].isOutletSelected) {
                            isAnyFilterSelected = true
                            break
                        }
                    }
                }
                if (suppliersList != null) {
                    for (i in suppliersList!!.indices) {
                        if (suppliersList!![i].isSupplierSelected) {
                            isAnyFilterSelected = true
                            break
                        }
                    }
                }
                if (orderStatusList != null) {
                    for (i in orderStatusList!!.indices) {
                        if (orderStatusList!![i].isFilterSelected) {
                            isAnyFilterSelected = true
                            break
                        }
                    }
                }
                if (orderTypeList != null) {
                    for (i in orderTypeList!!.indices) {
                        if (orderTypeList!![i].isStatusSelectedForFilter) {
                            isAnyFilterSelected = true
                            break
                        }
                    }
                }
                return isAnyFilterSelected
            }
    }
}