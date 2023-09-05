package zeemart.asia.buyers.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.RepeatOrderListAdapter.ViewHolderSupplierList
import zeemart.asia.buyers.constants.DebugConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.OrderHorizontalListOrderChangeListener
import zeemart.asia.buyers.interfaces.UpdateRepeatOrderActivityListener
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.Orders

/**
 * Created by ParulBhandari on 12/2/2017.
 */
class RepeatOrderListAdapter(
    private val context: Context,
    private val listener: UpdateRepeatOrderActivityListener,
    private val ordersGroupBySupplier: LinkedHashMap<Supplier, ArrayList<Orders?>>,
    private val supplierMap: Map<String?, DetailSupplierDataModel>?
) : RecyclerView.Adapter<ViewHolderSupplierList>(), OrderHorizontalListOrderChangeListener {
    private var lastSelectedSupplier = -1
    private val uniqueSuppliers = ArrayList<Supplier>()

    init {
        for (keys in ordersGroupBySupplier.keys) {
            uniqueSuppliers.add(keys)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSupplierList {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lst_orders_row, parent, false)
        return ViewHolderSupplierList(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderSupplierList, position: Int) {
        Log.d(
            DebugConstants.ZEEMARTDEBUG + " ORDERS",
            "$position*****set Id for horizontal list Adapter"
        )
        holder.adapter.horizontalListId = position
        val ordersToSupplier = ordersGroupBySupplier[uniqueSuppliers[position]]!!
        holder.adapter.setLstPastOrders(ordersToSupplier)
        if (supplierMap != null && supplierMap.containsKey(uniqueSuppliers[position].supplierId)) {
            val imageUrl = supplierMap[uniqueSuppliers[position].supplierId]!!.supplier.logoURL
            val supplierName =
                supplierMap[uniqueSuppliers[position].supplierId]!!.supplier.supplierName
            if (StringHelper.isStringNullOrEmpty(imageUrl)) {
                holder.imgSupplierImage.visibility = View.INVISIBLE
                holder.lytSupplierThumbNail.visibility = View.VISIBLE
                holder.lytSupplierThumbNail.setBackgroundColor(
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        supplierName!!,
                        context
                    )
                )
                holder.txtSupplierThumbNail.text =
                    CommonMethods.SupplierThumbNailShortCutText(supplierName)
                holder.txtSupplierThumbNail.setTextColor(
                    CommonMethods.SupplierThumbNailTextColor(
                        supplierName,
                        context
                    )
                )
            } else {
                holder.imgSupplierImage.visibility = View.VISIBLE
                holder.lytSupplierThumbNail.visibility = View.GONE
                Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder_all).resize(
                    holder.PLACE_HOLDER_SUPPLIER_IMAGE_SIZE,
                    holder.PLACE_HOLDER_SUPPLIER_IMAGE_SIZE
                ).into(holder.imgSupplierImage)
            }
            val supplierData = supplierMap[uniqueSuppliers[position].supplierId]
            val deliveryDateList = supplierData!!.deliveryDates
            if (deliveryDateList != null && deliveryDateList.size > 0) {
                if (StringHelper.isStringNullOrEmpty(supplierData.supplier.shortDesc)) {
                    holder.txtDateType.text = String.format(
                        context.getString(R.string.format_next_delivery),
                        DateHelper.getDateInDateMonthFormat(deliveryDateList[0].deliveryDate)
                    )
                } else {
                    holder.txtDateType.text = String.format(
                        context.getString(R.string.format_next_delivery_short_desc),
                        DateHelper.getDateInDateMonthFormat(deliveryDateList[0].deliveryDate),
                        supplierData.supplier.shortDesc
                    )
                }
            } else {
                holder.txtDateType.text = supplierData.supplier.shortDesc
            }
        } else {
            holder.imgSupplierImage.setImageResource(R.drawable.placeholder_all)
            holder.txtDateType.text = ""
        }
        if (uniqueSuppliers[position].isSupplierSelected) {
            holder.crdRepeatOrder.setBackgroundResource(R.color.white)
        } else {
            if (lastSelectedSupplier == -1) {
                holder.crdRepeatOrder.setBackgroundResource(R.color.white)
            } else {
                holder.crdRepeatOrder.setBackgroundResource(R.color.opacity30)
            }
        }
        holder.txtSupplierName.text = uniqueSuppliers[position].supplierName
    }

    override fun getItemCount(): Int {
        return uniqueSuppliers.size
    }

    inner class ViewHolderSupplierList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplierImage: ImageView
        var txtSupplierName: TextView
        var txtDateType: TextView
        var lstGroupBySupplier: RecyclerView
        var crdRepeatOrder: CardView
        var adapter: OrdersGroupBySupplierListAdapter
        val PLACE_HOLDER_SUPPLIER_IMAGE_SIZE = CommonMethods.dpToPx(30)
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplierImage = itemView.findViewById(R.id.img_supplier_new_order)
            txtSupplierName = itemView.findViewById(R.id.txt_supplier_name_new_order)
            txtDateType = itemView.findViewById(R.id.txt_date_type_new_order)
            crdRepeatOrder = itemView.findViewById(R.id.card_repeat_order)
            lstGroupBySupplier = itemView.findViewById(R.id.lst_orders_groupby_supplier)
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            lstGroupBySupplier.layoutManager = layoutManager
            adapter = OrdersGroupBySupplierListAdapter(context, this@RepeatOrderListAdapter)
            lstGroupBySupplier.adapter = adapter
            lstGroupBySupplier.setHasFixedSize(true)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtDateType, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onOrderDeselected(rowId: Int) {
        var count = 0
        val ordersBySupplier = ordersGroupBySupplier[uniqueSuppliers[rowId]]!!
        for (i in ordersBySupplier.indices) {
            if (ordersBySupplier[i]!!.isOrderSelected) {
                count++
            }
        }
        if (count == 0) {
            lastSelectedSupplier = -1
            uniqueSuppliers[rowId].isSupplierSelected = false
        }
        count = 0
        val selectedOrders = LinkedHashMap<String?, Orders?>()
        for (k in ordersGroupBySupplier[uniqueSuppliers[rowId]]!!.indices) {
            if (ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]!!.isOrderSelected) {
                count++
                ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]!!.supplier =
                    uniqueSuppliers[rowId]
                selectedOrders[ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]!!.orderId] =
                    ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]
            }
        }
        listener.noOfSelectedOrders(count, selectedOrders)
        notifyDataSetChanged()
    }

    override fun showOrderDetailFragment(
        pastOrderList: ArrayList<Orders?>?,
        position: Int,
        horizontalRowId: Int
    ) {
        listener.callOrderDetailDialogFragment(pastOrderList, position, horizontalRowId)
    }

    override fun onOrderSelected(rowId: Int) {
        //For the first time when user selects any order
        if (lastSelectedSupplier == -1) {
            lastSelectedSupplier = rowId
            uniqueSuppliers[rowId].isSupplierSelected = true
            notifyDataSetChanged()
        } else {
            if (lastSelectedSupplier == rowId) {
                notifyDataSetChanged()
            } else {
                for (i in ordersGroupBySupplier[uniqueSuppliers[lastSelectedSupplier]]!!.indices) {
                    ordersGroupBySupplier[uniqueSuppliers[lastSelectedSupplier]]!![i]!!.isOrderSelected =
                        false
                }
                uniqueSuppliers[lastSelectedSupplier].isSupplierSelected = false
                uniqueSuppliers[rowId].isSupplierSelected = true
                lastSelectedSupplier = rowId
                notifyDataSetChanged()
            }
        }
        var count = 0
        val selectedOrders = LinkedHashMap<String?, Orders?>()
        for (k in ordersGroupBySupplier[uniqueSuppliers[rowId]]!!.indices) {
            if (ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]!!.isOrderSelected) {
                count++
                ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]!!.supplier =
                    uniqueSuppliers[rowId]
                selectedOrders[ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]!!.orderId] =
                    ordersGroupBySupplier[uniqueSuppliers[rowId]]!![k]
            }
        }
        listener.noOfSelectedOrders(count, selectedOrders)
    }
}