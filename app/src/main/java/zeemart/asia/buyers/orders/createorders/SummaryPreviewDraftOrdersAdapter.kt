package zeemart.asia.buyers.orders.createorders

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.DeliveryDatesHorizontalListAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.interfaces.DeliveryDateSelectedListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.marketlist.DeliveryDate
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.order.CartListDraftOrderUI
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.order.SingleDraftOrderUpdateRequest
import zeemart.asia.buyers.models.orderimport.OrderBaseResponse
import zeemart.asia.buyers.models.orderimportimport.DraftOrdersBySKUPaginated
import zeemart.asia.buyers.models.orderimportimport.ErrorModel
import zeemart.asia.buyers.network.DeleteOrder
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderManagement
import java.util.*

class SummaryPreviewDraftOrdersAdapter(
    private val context: Context,
    draftOrdersList: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?,
    supplierIDDetailMap: Map<String, DetailSupplierDataModel>?,
    listener: UpdateUiListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HeaderItemDecoration.StickyHeaderInterface {
    private var cartListData: MutableList<CartListDraftOrderUI>? = null
    private var draftOrdersList: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?
    private var supplierIDDetailMap: Map<String, DetailSupplierDataModel>? =
        HashMap<String, DetailSupplierDataModel>()
    private val listener: UpdateUiListener
    private var isAllOrderSelected = false

    init {
        this.draftOrdersList = draftOrdersList
        createDisplayCartList(this.draftOrdersList)
        this.listener = listener
        //set the value for select all flag
        setAllOrderSelected(draftOrdersList)
        this.supplierIDDetailMap = supplierIDDetailMap
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_SELECT_ALL) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_list_select_all_header_row, parent, false)
            ViewHolderSelectAll(v)
        } else if (viewType == ITEM_TYPE_SUPPLIER_HEADER) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_list_supplier_header, parent, false)
            ViewHolderSupplierHeader(v)
        } else if (viewType == ITEM_TYPE_PRODUCT) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_list_product_row, parent, false)
            ViewHolderProduct(v)
        } else if (viewType == ITEM_TYPE_SPECIAL_REQUEST) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_list_draft_special_request, parent, false)
            ViewHolderSpecialRequest(v)
        } else if (viewType == ITEM_TYPE_EDIT_ORDER) {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_list_edit_order, parent, false)
            ViewHolderEditOrder(v)
        } else {
            val v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cart_list_edit_order, parent, false)
            ViewHolderEditOrder(v)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ViewHolderSelectAll) {
            val viewHolderSelectAll = viewHolder as ViewHolderSelectAll
            viewHolderSelectAll.txtSelectAll.setOnClickListener(View.OnClickListener {
                if (!isAllOrderSelected) {
                    isAllOrderSelected = true
                    viewHolderSelectAll.txtSelectAll.setText(context.getString(R.string.txt_deselect_all))
                    for (i in draftOrdersList!!.indices) {
                        draftOrdersList!![i].orders.isOrderSelected
                    }
                } else {
                    isAllOrderSelected = false
                    viewHolderSelectAll.txtSelectAll.setText(context.getString(R.string.txt_select_all))
                    for (i in draftOrdersList!!.indices) {
                        draftOrdersList!![i].orders.isOrderSelected
                    }
                }
                notifyDataSetChanged()
            })
            if (isAllOrderSelected) {
                viewHolderSelectAll.txtSelectAll.setText(context.getString(R.string.txt_deselect_all))
            } else {
                viewHolderSelectAll.txtSelectAll.setText(context.getString(R.string.txt_select_all))
            }
            val noOfOrderSelected = noOfOrderSelected
            if (draftOrdersList!!.size > 1) {
                viewHolderSelectAll.txtNumberOfOrdersSelected.setText(
                    context.getString(
                        R.string.txt_out_of_orders_selected,
                        noOfOrderSelected,
                        draftOrdersList!!.size
                    )
                )
            } else {
                viewHolderSelectAll.txtNumberOfOrdersSelected.setText(
                    context.getString(
                        R.string.txt_out_of_order_selected,
                        noOfOrderSelected,
                        draftOrdersList!!.size
                    )
                )
            }
        } else if (viewHolder is ViewHolderSupplierHeader) {
            val viewHolderSupplierHeader = viewHolder as ViewHolderSupplierHeader
            val order: Orders? = cartListData!![position].order
            if (order?.supplier != null) {
                viewHolderSupplierHeader.txtSupplierName.setText(order.supplier!!.supplierName)
            }
            val supplierDataModel: DetailSupplierDataModel? =
                supplierIDDetailMap!![order?.supplier?.supplierId]
            if (supplierDataModel != null) {
                val logoURL = supplierDataModel.supplier.logoURL
                if (StringHelper.isStringNullOrEmpty(logoURL)) {
                    viewHolderSupplierHeader.imgSupplierImage.visibility = View.INVISIBLE
                    viewHolderSupplierHeader.lytSupplierThumbNail.visibility = View.VISIBLE
                    val context = viewHolderSupplierHeader.itemView.context
                    viewHolderSupplierHeader.lytSupplierThumbNail.setBackgroundColor(
                        CommonMethods.SupplierThumbNailBackgroundColor(
                            supplierDataModel.supplier.supplierName!!,
                            context
                        )
                    )
                    viewHolderSupplierHeader.txtSupplierThumbNail.text =
                        CommonMethods.SupplierThumbNailShortCutText(supplierDataModel.supplier.supplierName!!)
                    viewHolderSupplierHeader.txtSupplierThumbNail.setTextColor(
                        CommonMethods.SupplierThumbNailTextColor(
                            supplierDataModel.supplier.supplierName!!,
                            context
                        )
                    )
                }
            }
            else {
                viewHolderSupplierHeader.imgSupplierImage.visibility = View.VISIBLE
                viewHolderSupplierHeader.lytSupplierThumbNail.setVisibility(View.GONE)
                Picasso.get().load(supplierDataModel?.supplier?.logoURL)
                    .placeholder(R.drawable.placeholder_all).resize(
                        PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH, PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                    ).into(viewHolderSupplierHeader.imgSupplierImage)
            }
            var noOfProducts = 0
            if (order?.products != null) {
                noOfProducts = order.products!!.size
            }
            if (order?.isAllow == true) {
                viewHolderSupplierHeader.imgRedMarker.visibility = View.GONE
                viewHolderSupplierHeader.lytErrorMessage.setVisibility(View.GONE)
                listener.updatePlaceOrderButton(draftOrdersList)
            } else {
                //display warning icon and text in yellow
                if (!order?.isAddOn!!) {
                    viewHolderSupplierHeader.imgRedMarker.visibility = View.VISIBLE
                    viewHolderSupplierHeader.lytErrorMessage.setVisibility(View.VISIBLE)
                } else {
                    if (!order?.isErrorBelowMoq!!) {
                        viewHolderSupplierHeader.imgRedMarker.visibility = View.VISIBLE
                        viewHolderSupplierHeader.lytErrorMessage.setVisibility(View.VISIBLE)
                    } else {
                        viewHolderSupplierHeader.imgRedMarker.visibility = View.GONE
                        viewHolderSupplierHeader.lytErrorMessage.setVisibility(View.GONE)
                    }
                }
                viewHolderSupplierHeader.txtErrorMessage.setText(context.resources.getString(R.string.txt_order_cannot_be_placed))
                listener.updatePlaceOrderButton(draftOrdersList)
            }
            if (!UserPermission.HasViewPrice()) {
                viewHolderSupplierHeader.imgWarning.visibility = View.GONE
                if (noOfProducts == 1) {
                    viewHolderSupplierHeader.txtNoOfItemsAndTotalAmount.setText(
                        noOfProducts.toString() + " " + context.getString(
                            R.string.txt_item
                        )
                    )
                } else {
                    viewHolderSupplierHeader.txtNoOfItemsAndTotalAmount.setText(
                        noOfProducts.toString() + " " + context.getString(
                            R.string.txt_items
                        )
                    )
                }
            } else {
                if (order.isAddOn) {
                    val priceDetails = PriceDetails()
                    if (order.amount?.discount?.amount != 0.0) {
                        val orderSubTotalAfterDiscount: Double? =
                            order.amount?.subTotal?.amount?.minus(order.amount?.discount?.amount!!)
                        val subTotalAfterDiscount = PriceDetails(orderSubTotalAfterDiscount)
                        if (orderSubTotalAfterDiscount != null) {
                            priceDetails.amount =
                                orderSubTotalAfterDiscount + subTotalAfterDiscount.getAmount(
                                    supplierDataModel!!.supplier.settings!!.gst!!
                                )
                        }
                    } else {
                        priceDetails.amount =
                            order.amount!!.subTotal!!.amount!! + order.amount!!.subTotal!!.getAmount(
                                supplierDataModel!!.supplier.settings!!.gst!!
                            )

                    }
                    if (noOfProducts == 1) {
                        viewHolderSupplierHeader.txtNoOfItemsAndTotalAmount.setText(
                            context.getString(
                                R.string.txt_item_total_amount,
                                noOfProducts,
                                priceDetails.displayValue
                            )
                        )
                    } else {
                        viewHolderSupplierHeader.txtNoOfItemsAndTotalAmount.setText(
                            context.getString(
                                R.string.txt_items_total_amount,
                                noOfProducts,
                                priceDetails.displayValue
                            )
                        )
                    }
                    order.amount?.total?.amount = priceDetails.amount
                } else {
                    if (noOfProducts == 1) {
                        viewHolderSupplierHeader.txtNoOfItemsAndTotalAmount.setText(
                            context.getString(
                                R.string.txt_item_total_amount,
                                noOfProducts,
                                order.amount?.total?.displayValue
                            )
                        )
                    } else {
                        viewHolderSupplierHeader.txtNoOfItemsAndTotalAmount.setText(
                            context.getString(
                                R.string.txt_items_total_amount,
                                noOfProducts,
                                order.amount?.total?.displayValue
                            )
                        )
                    }
                }
                if (supplierDataModel?.deliveryFeePolicy != null && supplierDataModel.deliveryFeePolicy!!.minOrder != null && order.amount?.subTotal?.amount!! < supplierDataModel!!.deliveryFeePolicy?.minOrder?.amount!!) {
                    //display warning icon and text in yellow
                    viewHolderSupplierHeader.imgWarning.visibility = View.GONE
                    if (!order.isAddOn) {
                        viewHolderSupplierHeader.lytBelowMovMessage.setVisibility(View.VISIBLE)
                    } else {
                        viewHolderSupplierHeader.lytBelowMovMessage.setVisibility(View.GONE)
                    }
                    viewHolderSupplierHeader.txtBelowMov.setText(context.resources.getString(R.string.txt_below_minimum_order))
                } else {
                    //hide warning and display text in grey
                    viewHolderSupplierHeader.imgWarning.visibility = View.GONE
                    viewHolderSupplierHeader.lytBelowMovMessage.setVisibility(View.GONE)
                    viewHolderSupplierHeader.txtNoOfItemsAndTotalAmount.setTextColor(
                        context.resources.getColor(
                            R.color.grey_medium
                        )
                    )
                }
                if (supplierDataModel?.deliveryFeePolicy != null && supplierDataModel!!.deliveryFeePolicy!!.minOrder != null
                    && order.amount?.subTotal?.amount!! < supplierDataModel!!.deliveryFeePolicy?.minOrder?.amount!!
                    && supplierDataModel!!.deliveryFeePolicy!!.isBlockBelowMinOrder
                ) {
                    if (!order.isAddOn) {
                        viewHolderSupplierHeader.lytErrorMessage.setVisibility(View.VISIBLE)
                        viewHolderSupplierHeader.imgRedMarker.visibility = View.VISIBLE
                    } else {
                        if (!order.isErrorBelowMoq) {
                            viewHolderSupplierHeader.lytErrorMessage.setVisibility(View.VISIBLE)
                            viewHolderSupplierHeader.imgRedMarker.visibility = View.VISIBLE
                        } else {
                            viewHolderSupplierHeader.lytErrorMessage.setVisibility(View.GONE)
                            viewHolderSupplierHeader.imgRedMarker.visibility = View.GONE
                        }
                    }
                    viewHolderSupplierHeader.txtErrorMessage.setText(context.resources.getString(R.string.txt_order_cannot_be_placed))
                }
            }
            if (order.isOrderSelected) {
                viewHolderSupplierHeader.chkSelectDraftOrder.setChecked(true)
            } else {
                viewHolderSupplierHeader.chkSelectDraftOrder.setChecked(false)
            }
            viewHolderSupplierHeader.chkSelectDraftOrder.setOnClickListener(View.OnClickListener {
                if (viewHolderSupplierHeader.chkSelectDraftOrder.isChecked()) {
                    viewHolderSupplierHeader.chkSelectDraftOrder.setChecked(true)
                    order.isOrderSelected = true
                    listener.updatePlaceOrderButton(draftOrdersList)
                } else {
                    viewHolderSupplierHeader.chkSelectDraftOrder.setChecked(false)
                    order.isOrderSelected = false
                    listener.updatePlaceOrderButton(draftOrdersList)
                }
                setAllOrderSelected(draftOrdersList)
                notifyDataSetChanged()
            })
            viewHolderSupplierHeader.txtSupplierName.setOnClickListener(View.OnClickListener {
                if (order.isOrderSelected) {
                    viewHolderSupplierHeader.chkSelectDraftOrder.setChecked(false)
                    order.isOrderSelected = false
                } else {
                    viewHolderSupplierHeader.chkSelectDraftOrder.setChecked(true)
                    order.isOrderSelected = true
                }
                setAllOrderSelected(draftOrdersList)
                listener.updatePlaceOrderButton(draftOrdersList)
                notifyDataSetChanged()
            })
        } else if (viewHolder is ViewHolderProduct) {
            val viewHolderProduct = viewHolder as ViewHolderProduct
            val order: Orders? = cartListData!![position].order
            val supplierDataModel: DetailSupplierDataModel? =
                supplierIDDetailMap!![order?.supplier?.supplierId]
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                )!! && !StringHelper.isStringNullOrEmpty(
                    cartListData!![position].product?.customName
                )
            ) {
                viewHolderProduct.txtProductName.setText(cartListData!![position].product?.customName)
            } else {
                viewHolderProduct.txtProductName.setText(cartListData!![position].product?.productName)
            }
            viewHolderProduct.txtProductQuantity.setText(cartListData!![position].product?.quantityWithUnitSizeValue)
            if (!StringHelper.isStringNullOrEmpty(cartListData!![position].product?.notes)) {
                viewHolderProduct.txtProductNotes.setVisibility(View.VISIBLE)
                viewHolderProduct.txtProductNotes.setText(cartListData!![position].product?.notes)
            } else {
                viewHolderProduct.txtProductNotes.setVisibility(View.GONE)
            }
            if (order?.isAllow == true) {
                viewHolderProduct.imgRedMarker.visibility = View.GONE
            } else {
                if (!order?.isAddOn!!) {
                    viewHolderProduct.imgRedMarker.visibility = View.VISIBLE
                } else {
                    if (!order?.isErrorBelowMoq!!) {
                        viewHolderProduct.imgRedMarker.visibility = View.VISIBLE
                    } else {
                        viewHolderProduct.imgRedMarker.visibility = View.GONE
                    }
                }
            }
            if (supplierDataModel?.deliveryFeePolicy != null && supplierDataModel.deliveryFeePolicy!!.minOrder != null
                && order.amount?.subTotal?.amount!! < supplierDataModel!!.deliveryFeePolicy!!.minOrder?.amount!!
                && supplierDataModel!!.deliveryFeePolicy!!.isBlockBelowMinOrder
            ) {
                if (!order.isAddOn) {
                    viewHolderProduct.imgRedMarker.visibility = View.VISIBLE
                } else {
                    if (!order.isErrorBelowMoq) {
                        viewHolderProduct.imgRedMarker.visibility = View.VISIBLE
                    } else {
                        viewHolderProduct.imgRedMarker.visibility = View.GONE
                    }
                }
            }
            viewHolderProduct.itemView.setOnClickListener(View.OnClickListener {
                val newIntent = Intent(context, ReviewOrderActivity::class.java)
                val selectedOrder: MutableList<Orders> = ArrayList<Orders>()
                selectedOrder.add(order)
                val orderJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrder)
                newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
                val supplierJson: String =
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(order.supplier)
                val outletJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(order.outlet)
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
                newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
                (context as Activity).startActivityForResult(
                    newIntent,
                    REQUEST_CODE_SELECTED_DRAFT_ORDER
                )
            })
            //hide the last divider
            if (!cartListData!![position + 1].isDraftProduct) {
                viewHolderProduct.dividerProduct.visibility = View.GONE
                viewHolderProduct.itemView.setPadding(0, 0, CommonMethods.dpToPx(20), 0)
            } else {
                viewHolderProduct.dividerProduct.visibility = View.VISIBLE
                viewHolderProduct.itemView.setPadding(0, 0, CommonMethods.dpToPx(20), 0)
            }
        } else if (viewHolder is ViewHolderSpecialRequest) {
            val viewHolderSpecialRequest = viewHolder as ViewHolderSpecialRequest
            val order: Orders? = cartListData!![position].order
            val supplierDataModel: DetailSupplierDataModel? =
                supplierIDDetailMap!![order?.supplier?.supplierId]
            if (order?.isAllow == true) {
                viewHolderSpecialRequest.imgRedMarker.visibility = View.GONE
            } else {
                if (!order?.isAddOn!!) {
                    viewHolderSpecialRequest.imgRedMarker.visibility = View.VISIBLE
                } else {
                    if (!order?.isErrorBelowMoq!!) {
                        viewHolderSpecialRequest.imgRedMarker.visibility = View.VISIBLE
                    } else {
                        viewHolderSpecialRequest.imgRedMarker.visibility = View.GONE
                    }
                }
            }
            if (!StringHelper.isStringNullOrEmpty(order.notes)) {
                viewHolderSpecialRequest.txtSpecialRequest.setText(order.notes)
            }
            if (supplierDataModel?.deliveryFeePolicy != null && supplierDataModel!!.deliveryFeePolicy!!.minOrder != null
                && order.amount?.subTotal?.amount!! < supplierDataModel!!.deliveryFeePolicy!!.minOrder?.amount!!
                && supplierDataModel.deliveryFeePolicy!!.isBlockBelowMinOrder
            ) {
                if (!order.isAddOn) {
                    viewHolderSpecialRequest.imgRedMarker.visibility = View.VISIBLE
                } else {
                    if (!order.isErrorBelowMoq) {
                        viewHolderSpecialRequest.imgRedMarker.visibility = View.VISIBLE
                    } else {
                        viewHolderSpecialRequest.imgRedMarker.visibility = View.GONE
                    }
                }
            }
        } else if (viewHolder is ViewHolderEditOrder) {
            val viewHolderEditOrder = viewHolder as ViewHolderEditOrder
            val order: Orders = cartListData!![position].order!!
            val errorsList: MutableList<ErrorModel> = ArrayList<ErrorModel>()
            for (i in draftOrdersList!!.indices) {
                if (draftOrdersList!![i].orders.orderId.equals(order?.orderId)) {
                    if (draftOrdersList!![i].errors != null) draftOrdersList!![i].errors?.let {
                        errorsList.addAll(
                            it
                        )
                    }
                }
            }
            val supplierDataModel: DetailSupplierDataModel? =
                supplierIDDetailMap!![order?.supplier?.supplierId]
            if (order?.isDeleteOrder == true) {
                viewHolderEditOrder.txtEditOrder.setText(context.getString(R.string.txt_delete))
                viewHolderEditOrder.txtEditOrder.setTextColor(context.resources.getColor(R.color.pinky_red))
            } else {
                viewHolderEditOrder.txtEditOrder.setText(context.getString(R.string.txt_edit_order))
                viewHolderEditOrder.txtEditOrder.setTextColor(context.resources.getColor(R.color.text_blue))
            }
            if (order?.isAllow == true) {
                viewHolderEditOrder.imgRedMarker.visibility = View.GONE
            } else {
                if (!order?.isAddOn!!) {
                    viewHolderEditOrder.imgRedMarker.visibility = View.VISIBLE
                } else {
                    if (!order?.isErrorBelowMoq!!) {
                        viewHolderEditOrder.imgRedMarker.visibility = View.VISIBLE
                    } else {
                        viewHolderEditOrder.imgRedMarker.visibility = View.GONE
                    }
                }
            }
            if (supplierDataModel?.deliveryFeePolicy != null
                && supplierDataModel.deliveryFeePolicy!!.minOrder != null
                && order.amount?.subTotal?.amount!! < supplierDataModel.deliveryFeePolicy!!.minOrder?.amount!!
                && supplierDataModel.deliveryFeePolicy!!.isBlockBelowMinOrder
            ) {
                if (!order.isAddOn) {
                    viewHolderEditOrder.imgRedMarker.visibility = View.VISIBLE
                } else {
                    if (!order.isErrorBelowMoq) {
                        viewHolderEditOrder.imgRedMarker.visibility = View.VISIBLE
                    } else {
                        viewHolderEditOrder.imgRedMarker.visibility = View.GONE
                    }
                }
            }
            val deliveryDateData = getDeliveryDateData(order)
            if (deliveryDateData != null && order.outlet != null && order.outlet!!.timeZoneFromOutlet != null) {
                viewHolderEditOrder.txtDeliveryDate.text =
                    deliveryDateData.getSelectedDateDisplayFormat(order.outlet!!.timeZoneFromOutlet)
            }
            viewHolderEditOrder.lytChangeDeliveryDate.setOnClickListener(View.OnClickListener {
                val deliveryDateData = getDeliveryDateData(order)
                val timezone: TimeZone = order.outlet?.timeZoneFromOutlet!!
                createDeliveryDatesSelectionDialog(
                    timezone,
                    deliveryDateData!!.deliveryDates,
                    viewHolderEditOrder
                )
            })
            viewHolderEditOrder.txtEditOrder.setOnClickListener(View.OnClickListener {
                if (order.isDeleteOrder) {
                    //delete order both from the cart list and draftList
                    for (i in cartListData!!.indices) {
                        if (cartListData!![i].order != null && cartListData!![i].order?.orderId == order.orderId) {
                            cartListData!!.remove(cartListData!![i])
                        }
                    }
                    for (i in draftOrdersList!!.indices) {
                        if (draftOrdersList!![i].orders.orderId.equals(order.orderId)) {
                            draftOrdersList!!.remove(draftOrdersList!![i])
                        }
                    }
                    updateDataList(draftOrdersList)
                    listener.updatePlaceOrderButton(draftOrdersList)
                } else {
                    val newIntent = Intent(context, ReviewOrderActivity::class.java)
                    val selectedOrder: MutableList<Orders> = ArrayList<Orders>()
                    selectedOrder.add(order)
                    val orderJson: String =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedOrder)
                    newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
                    val supplierJson: String =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(order.supplier)
                    val outletJson: String =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(order.outlet)
                    newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
                    newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
                    if (errorsList != null) {
                        val errorsJson: String =
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(errorsList)
                        newIntent.putExtra(ZeemartAppConstants.ERROR_DETAILS, errorsJson)
                    }
                    (context as Activity).startActivityForResult(
                        newIntent,
                        REQUEST_CODE_SELECTED_DRAFT_ORDER
                    )
                }
            })
        }
    }

    fun setAllOrderSelected(draftOrdersList: List<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?) {
        val noOfSelectedOrders = noOfOrderSelected
        isAllOrderSelected = if (noOfSelectedOrders == draftOrdersList!!.size) {
            true
        } else {
            false
        }
    }

//    val itemCount: Int
//        get() = cartListData!!.size

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item: CartListDraftOrderUI = cartListData!![position]
        return if (item.isSelectAllHeader) {
            ITEM_TYPE_SELECT_ALL
        } else if (item.isSupplierDetailHeader) {
            ITEM_TYPE_SUPPLIER_HEADER
        } else if (item.isDraftProduct) {
            ITEM_TYPE_PRODUCT
        } else if (item.isDraftNotes) {
            ITEM_TYPE_SPECIAL_REQUEST
        } else {
            ITEM_TYPE_EDIT_ORDER
        }
    }

    override fun getItemCount(): Int {
        return cartListData!!.size
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
        return if (headerPosition == 0) {
            R.layout.sticky_list_no_layout_header
        } else {
            R.layout.layout_cart_list_supplier_header
        }
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        if (headerPosition != 0 && cartListData!![headerPosition].order != null) {
            header?.setBackgroundResource(R.color.white)
            val txtSupplierName: TextView =
                header?.findViewById<TextView>(R.id.txt_supplier_name_draft_list_row)!!
            val txtNoOfItemsAndTotalAmount: TextView =
                header?.findViewById<TextView>(R.id.txt_draft_order_items_total_price)!!
            val chkSelectDraftOrder: CheckBox =
                header.findViewById<CheckBox>(R.id.check_box_select_draft)
            val imgSupplierImage =
                header.findViewById<ImageView>(R.id.img_supplier_image_draft_order)
            val imgWarning = header.findViewById<ImageView>(R.id.img_warning_draft_order)
            val txtErrorMessage: TextView =
                header.findViewById<TextView>(R.id.txt_error_message_draft_order)
            val lytErrorMessage: LinearLayout =
                header.findViewById<LinearLayout>(R.id.lyt_error_draft_order)
            val imgRedMarker =
                header.findViewById<ImageView>(R.id.marker_red_order_not_allow_supplier)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtNoOfItemsAndTotalAmount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtErrorMessage,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            val order: Orders? = cartListData!![headerPosition].order
            if (order?.isOrderSelected == true) {
                chkSelectDraftOrder.setChecked(true)
            } else {
                chkSelectDraftOrder.setChecked(false)
            }
            if (order?.supplier != null) {
                txtSupplierName.setText(order.supplier!!.supplierName)
            }
            val supplierDataModel: DetailSupplierDataModel? =
                supplierIDDetailMap!![order?.supplier?.supplierId]
            if (StringHelper.isStringNullOrEmpty(supplierDataModel?.supplier?.logoURL)) {
                imgSupplierImage.visibility = View.GONE
            } else {
                Picasso.get().load(supplierDataModel?.supplier?.logoURL)
                    .placeholder(R.drawable.placeholder_all).resize(
                        PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH, PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                    ).into(imgSupplierImage)
            }
            var noOfProducts = 0
            if (order?.products != null) {
                noOfProducts = order.products!!.size
            }
            if (order?.isAddOn == true) {
                val priceDetails = PriceDetails()
                if (order?.amount?.discount?.amount != 0.0) {
                    val orderSubTotalAfterDiscount: Double? =
                        order.amount?.subTotal?.amount?.minus(order.amount!!.discount?.amount!!)
                    val subTotalAfterDiscount = PriceDetails(orderSubTotalAfterDiscount)
                    priceDetails.amount =
                        orderSubTotalAfterDiscount!! + subTotalAfterDiscount.getAmount(
                            supplierDataModel!!.supplier.settings!!.gst!!
                        )

                } else {
                    priceDetails.amount =
                        order.amount!!.subTotal!!.amount!! + order.amount!!.subTotal!!.getAmount(
                            supplierDataModel!!.supplier.settings!!.gst!!
                        )

                }
                if (noOfProducts == 1) {
                    txtNoOfItemsAndTotalAmount.setText(
                        context.getString(
                            R.string.txt_item_total_amount,
                            noOfProducts,
                            priceDetails.displayValue
                        )
                    )
                } else {
                    txtNoOfItemsAndTotalAmount.setText(
                        context.getString(
                            R.string.txt_items_total_amount,
                            noOfProducts,
                            priceDetails.displayValue
                        )
                    )
                }
                order.amount?.total?.amount = priceDetails.amount
            } else {
                if (noOfProducts == 1) {
                    txtNoOfItemsAndTotalAmount.setText(
                        context.getString(
                            R.string.txt_item_total_amount,
                            noOfProducts,
                            order?.amount?.total?.displayValue
                        )
                    )
                } else {
                    txtNoOfItemsAndTotalAmount.setText(
                        context.getString(
                            R.string.txt_items_total_amount,
                            noOfProducts,
                            order?.amount?.total?.displayValue
                        )
                    )
                }
            }
            if (supplierDataModel?.deliveryFeePolicy != null && supplierDataModel.deliveryFeePolicy!!.minOrder != null
                && order?.amount?.subTotal?.amount!! < supplierDataModel.deliveryFeePolicy!!.minOrder?.amount!!
            ) {
                //display warning icon and text in yellow
                if (!order?.isAddOn!!) {
                    imgWarning.visibility = View.VISIBLE
                } else {
                    imgWarning.visibility = View.GONE
                }
                txtNoOfItemsAndTotalAmount.setTextColor(context.resources.getColor(R.color.squash))
            } else {
                //hide warning and display text in grey
                imgWarning.visibility = View.GONE
                txtNoOfItemsAndTotalAmount.setTextColor(context.resources.getColor(R.color.grey_medium))
            }
            if (order?.isAllow!!) {
                imgRedMarker.visibility = View.GONE
                lytErrorMessage.setVisibility(View.GONE)
                listener.updatePlaceOrderButton(draftOrdersList)
            } else {
                //display warning icon and text in yellow
                if (!order.isAddOn) {
                    imgRedMarker.visibility = View.VISIBLE
                    lytErrorMessage.setVisibility(View.VISIBLE)
                } else {
                    if (!order.isErrorBelowMoq) {
                        imgRedMarker.visibility = View.VISIBLE
                        lytErrorMessage.setVisibility(View.VISIBLE)
                    } else {
                        imgRedMarker.visibility = View.GONE
                        lytErrorMessage.setVisibility(View.GONE)
                    }
                }
                txtErrorMessage.setText(context.resources.getString(R.string.txt_order_cannot_be_placed))
                listener.updatePlaceOrderButton(draftOrdersList)
            }
        }
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return if (itemPosition < cartListData!!.size) {
            cartListData!![itemPosition].isSupplierDetailHeader
        } else {
            false
        }
    }

    override fun onHeaderClicked(headerView: View?, position: Int) {
        if (!cartListData!![position].isSelectAllHeader) {
            val chkSelectDraftOrder: CheckBox =
                headerView?.findViewById<CheckBox>(R.id.check_box_select_draft)!!
            if (cartListData!![position].order?.isOrderSelected == true) {
                chkSelectDraftOrder.setChecked(false)
                cartListData!![position].order?.isOrderSelected = false
            } else {
                chkSelectDraftOrder.setChecked(true)
                cartListData!![position].order?.isOrderSelected = true
            }
            setAllOrderSelected(draftOrdersList)
            notifyDataSetChanged()
        }
    }

    /********** View Holder classes  */
    inner class ViewHolderSelectAll(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtNumberOfOrdersSelected: TextView
        var txtSelectAll: TextView

        init {
            txtNumberOfOrdersSelected =
                itemView.findViewById<TextView>(R.id.txt_num_of_orders_selected)
            txtSelectAll = itemView.findViewById<TextView>(R.id.txt_select_all_draft_orders)
            ZeemartBuyerApp.setTypefaceView(
                txtNumberOfOrdersSelected,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSelectAll,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    inner class ViewHolderSupplierHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSupplierName: TextView
        var txtNoOfItemsAndTotalAmount: TextView
        var chkSelectDraftOrder: CheckBox
        var imgSupplierImage: ImageView
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView
        var imgWarning: ImageView
        var imgError: ImageView
        var txtErrorMessage: TextView
        var lytErrorMessage: LinearLayout
        var imgRedMarker: ImageView
        var lytBelowMovMessage: LinearLayout
        var txtBelowMov: TextView

        init {
            txtSupplierName = itemView.findViewById<TextView>(R.id.txt_supplier_name_draft_list_row)
            txtNoOfItemsAndTotalAmount =
                itemView.findViewById<TextView>(R.id.txt_draft_order_items_total_price)
            chkSelectDraftOrder = itemView.findViewById<CheckBox>(R.id.check_box_select_draft)
            imgSupplierImage = itemView.findViewById(R.id.img_supplier_image_draft_order)
            imgWarning = itemView.findViewById(R.id.img_warning_draft_order)
            imgError = itemView.findViewById(R.id.img_error_draft_order)
            txtErrorMessage = itemView.findViewById<TextView>(R.id.txt_error_message_draft_order)
            lytErrorMessage = itemView.findViewById<LinearLayout>(R.id.lyt_error_draft_order)
            imgRedMarker = itemView.findViewById(R.id.marker_red_order_not_allow_supplier)
            txtBelowMov =
                itemView.findViewById<TextView>(R.id.txt_error_below_mov_message_draft_order)
            lytBelowMovMessage =
                itemView.findViewById<LinearLayout>(R.id.lyt_error_below_mov_draft_order)
            lytSupplierThumbNail =
                itemView.findViewById<RelativeLayout>(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById<TextView>(R.id.txt_supplier_thumbnail)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtNoOfItemsAndTotalAmount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtErrorMessage,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtBelowMov,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    class ViewHolderProduct(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtProductName: TextView
        var txtProductNotes: TextView
        var txtProductQuantity: TextView
        var imgRedMarker: ImageView
        var dividerProduct: View

        init {
            txtProductName = itemView.findViewById<TextView>(R.id.txt_draft_product_name)
            txtProductNotes = itemView.findViewById<TextView>(R.id.txt_draft_product_notes)
            txtProductQuantity = itemView.findViewById<TextView>(R.id.txt_draft_product_quantity)
            imgRedMarker = itemView.findViewById(R.id.marker_red_order_not_allow_product)
            dividerProduct = itemView.findViewById(R.id.divider_product)
            ZeemartBuyerApp.setTypefaceView(
                txtProductName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductNotes,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val SWIPE_BUTTON_WIDTH: Int = CommonMethods.dpToPx(60)
        }
    }

    inner class ViewHolderSpecialRequest(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSpecialRequest: TextView
        var imgRedMarker: ImageView

        init {
            txtSpecialRequest = itemView.findViewById<TextView>(R.id.txt_draft_order_sp_req)
            imgRedMarker = itemView.findViewById(R.id.marker_red_order_special_request)
            ZeemartBuyerApp.setTypefaceView(
                txtSpecialRequest,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
        }
    }

    inner class ViewHolderEditOrder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDeliveryDate: TextView
        var txtEditOrder: TextView
        var lytChangeDeliveryDate: LinearLayout
        var imgRedMarker: ImageView

        init {
            txtDeliveryDate = itemView.findViewById<TextView>(R.id.txt_delivery_date_draft_order)
            txtEditOrder = itemView.findViewById<TextView>(R.id.txt_edit_draft_order)
            lytChangeDeliveryDate =
                itemView.findViewById<LinearLayout>(R.id.lyt_change_delivery_date_draft_order)
            imgRedMarker = itemView.findViewById(R.id.marker_red_order_not_allow_edit_order)
            ZeemartBuyerApp.setTypefaceView(
                txtDeliveryDate,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtEditOrder,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    /******** interface to pass data to the activity class  */
    interface UpdateUiListener {
        fun updatePlaceOrderButton(orders: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?)
        fun showLoadingOverlay(isShowing: Boolean)
    }

    /*********** utility methods
     * @param draftOrdersList
     */
    fun createDisplayCartList(draftOrdersList: List<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?) {
        cartListData = ArrayList<CartListDraftOrderUI>()
        val selectAllHeader = CartListDraftOrderUI()
        selectAllHeader.isSelectAllHeader = true
        cartListData!!.add(selectAllHeader)
        for (i in draftOrdersList!!.indices) {
            val supplierDetailHeader = CartListDraftOrderUI()
            supplierDetailHeader.isSupplierDetailHeader = true
            supplierDetailHeader.order = draftOrdersList[i].orders
            cartListData!!.add(supplierDetailHeader)
            if (draftOrdersList[i].orders
                    .products != null && draftOrdersList[i].orders.products
                    ?.size!! > 0
            ) {
                for (j in 0 until draftOrdersList[i].orders.products?.size!!) {
                    val draftOrderProduct = CartListDraftOrderUI()
                    draftOrderProduct.isDraftProduct = true
                    draftOrderProduct.order = draftOrdersList[i].orders
                    draftOrderProduct.product = draftOrdersList[i].orders.products?.get(j)
                    cartListData!!.add(draftOrderProduct)
                }
            }
            if (!StringHelper.isStringNullOrEmpty(draftOrdersList[i].orders.notes)) {
                val orderNotes = CartListDraftOrderUI()
                orderNotes.isDraftNotes = true
                orderNotes.order = draftOrdersList[i].orders
                cartListData!!.add(orderNotes)
            }
            val editOrder = CartListDraftOrderUI()
            editOrder.isEditDraft = true
            editOrder.order = draftOrdersList[i].orders
            cartListData!!.add(editOrder)
        }
    }

    val noOfOrderSelected: Int
        get() {
            var noOfOrderSelected = 0
            for (i in draftOrdersList!!.indices) {
                if (draftOrdersList!![i].orders.isOrderSelected) {
                    noOfOrderSelected = noOfOrderSelected + 1
                }
            }
            return noOfOrderSelected
        }

    class DeliveryDateData {
        //list holding all the delivery date for an order, with selected delivery date set to true
        var deliveryDates: List<DeliveryDate>? = null
        var selectedDateLongFormat: Long? = null
        fun getSelectedDateDisplayFormat(timezone: TimeZone?): String {
            if (selectedDateLongFormat != null) {
                var dateDay = ""
                dateDay = if (timezone != null) {
                    DateHelper.getDateInDayDateMonthYearFormat(selectedDateLongFormat!!, timezone)
                } else {
                    DateHelper.getDateInDayDateMonthYearFormat(
                        selectedDateLongFormat!!,
                        TimeZone.getDefault()
                    )
                }
                return dateDay
            }
            return ""
        }
    }

    fun getDeliveryDateData(orderData: Orders?): DeliveryDateData? {
        val supplierDetails: DetailSupplierDataModel? =
            supplierIDDetailMap!![orderData?.supplier?.supplierId]
        if (supplierDetails != null && supplierDetails.deliveryDates != null && supplierDetails.deliveryDates!!.size > 0) {
            val deliveryDates: List<DeliveryDate> = supplierDetails.deliveryDates!!
            var timeDelivered: Long = 0
            if (orderData != null && orderData.timeDelivered != null) {
                val latestDeliveryDate: Calendar =
                    Calendar.getInstance(DateHelper.marketTimeZone)
                val dLatestDeliveryDate: Date? = deliveryDates[0].deliveryDate?.times(1000)
                    ?.let { Date(it) }
                latestDeliveryDate.time = dLatestDeliveryDate
                val orderDeliveryDate: Calendar =
                    Calendar.getInstance(DateHelper.marketTimeZone)
                val dOrderDeliveryDate: Date = Date(orderData.timeDelivered!! * 1000)
                orderDeliveryDate.time = dOrderDeliveryDate
                if (orderDeliveryDate.before(latestDeliveryDate)) {
                    timeDelivered = deliveryDates[0].deliveryDate!!
                    deliveryDates[0].dateSelected = true
                    orderData.timeDelivered = deliveryDates[0].deliveryDate
                } else {
                    timeDelivered = orderData.timeDelivered!!
                    val dateDayOrderDelivery: String = DateHelper.getDateInDateMonthYearFormat(
                        timeDelivered,
                        orderData.outlet?.timeZoneFromOutlet
                    )
                    for (i in deliveryDates.indices) {
                        val dateDaySupplierDeliveryDate: String =
                            DateHelper.getDateInDateMonthYearFormat(
                                deliveryDates[i].deliveryDate
                            )
                        if (dateDayOrderDelivery == dateDaySupplierDeliveryDate) {
                            timeDelivered = deliveryDates[i].deliveryDate!!
                            deliveryDates[i].dateSelected = true
                        } else {
                            deliveryDates[i].dateSelected = false
                        }
                    }
                }
            } else {
                timeDelivered = deliveryDates[0].deliveryDate!!
                deliveryDates[0].dateSelected = true
            }
            val deliveryDateData = DeliveryDateData()
            deliveryDateData.deliveryDates = deliveryDates
            deliveryDateData.selectedDateLongFormat = timeDelivered
            return deliveryDateData
        }
        return null
    }

    fun createDeliveryDatesSelectionDialog(
        timeZone: TimeZone?,
        dates: List<DeliveryDate>?,
        viewHolderEditOrder: ViewHolderEditOrder,
    ) {
        var isDatePHorEPH = false
        val d = Dialog(context, R.style.CustomDialogTheme)
        d.setContentView(R.layout.layout_delivery_dates)
        val dismissBg = d.findViewById<View>(R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val lstDeliveryDateList: RecyclerView =
            d.findViewById<RecyclerView>(R.id.lst_deliveryDatesSelection)
        val txtDeliveryDateHeader1: TextView =
            d.findViewById<TextView>(R.id.deliveryDates_header_text_1)
        val txtDeliveryDateHeader2: TextView =
            d.findViewById<TextView>(R.id.deliveryDates_header_text_2)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDateHeader1,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDateHeader2,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        for (i in dates!!.indices) {
            if (dates[i].isPH || dates[i].isEvePH) {
                isDatePHorEPH = true
                break
            }
        }
        if (isDatePHorEPH) {
            txtDeliveryDateHeader2.setVisibility(View.VISIBLE)
        } else {
            txtDeliveryDateHeader2.setVisibility(View.GONE)
        }
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        lstDeliveryDateList.setLayoutManager(layoutManager)
        val adapter = timeZone?.let {
            DeliveryDatesHorizontalListAdapter(
                context, it, dates,
                object : DeliveryDateSelectedListener {
                    override fun deliveryDateSelected(date: String?, deliveryDate: Long?) {
                        d.dismiss()
                        viewHolderEditOrder.txtDeliveryDate.setText(date?.trim { it <= ' ' })
                        var selectedDate: Long = 0
                        for (i in dates.indices) {
                            if (dates[i].dateSelected) {
                                selectedDate = dates[i].deliveryDate!!
                                break
                            }
                        }
                        val orderJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            cartListData!![viewHolderEditOrder.getAdapterPosition()].order
                        )
                        val delDateChangedOrder: Orders? =
                            ZeemartBuyerApp.fromJson<Orders>(orderJson, Orders::class.java)
                        delDateChangedOrder?.timeDelivered = selectedDate
                        listener.showLoadingOverlay(true)
                        //call API to update delivery date in the draft
                        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
                        outlet.add(SharedPref.defaultOutlet!!)
                        val requestJson: String =
                            SingleDraftOrderUpdateRequest.getJsonRequestFromOrder(
                                delDateChangedOrder
                            )
                        OrderManagement.singleDraftOrderUpdate(
                            context,
                            requestJson,
                            delDateChangedOrder?.outlet?.outletId,
                            outlet,
                            object : OrderManagement.SingleDraftOrderUpdateListener {
                                override fun onSuccessResponse(orderData: OrderBaseResponse.Data?) {
                                    listener.showLoadingOverlay(false)
                                    OrderHelper.validateAddOnOrderByOrderId(
                                        context,
                                        orderData,
                                        object : OrderHelper.ValidAddOnOrderByOrderIdsListener {
                                            override fun onSuccessResponse(draftOrdersBySKUPaginated: OrderBaseResponse.Data?) {
                                                if (draftOrdersBySKUPaginated != null && draftOrdersBySKUPaginated.data != null) {
                                                    //match the orderId and update the draftOrderList and notify data set changed
                                                    val order: Orders =
                                                        draftOrdersBySKUPaginated.data!!
                                                    for (i in draftOrdersList!!.indices) {
                                                        if (order.orderId == draftOrdersList!![i].orders
                                                                .orderId
                                                        ) {
                                                            if (draftOrdersList!![i].orders
                                                                    .isOrderSelected
                                                            ) {
                                                                order.isOrderSelected = true
                                                            }
                                                            //check if order has some error after updating and cannot be placed uncomment once API fixed
                                                            if (draftOrdersBySKUPaginated.errors != null && draftOrdersBySKUPaginated.errors!!
                                                                    .size!! > 0
                                                            ) {
                                                                for (j in 0 until draftOrdersBySKUPaginated.errors!!
                                                                    .size) {
                                                                    if (draftOrdersBySKUPaginated.errors!!
                                                                            .get(j)
                                                                            .isErrorCode(ErrorModel.ErrorCode.REQUEST_VALIDATION_ERROR)
                                                                    ) {
                                                                        order.isAllow = false
                                                                        order.isErrorBelowMoq =
                                                                            false
                                                                        break
                                                                    }
                                                                }
                                                            } else {
                                                                order.isAllow = true
                                                                order.isErrorBelowMoq = true
                                                            }
                                                            val draftOrdersBySKU =
                                                                DraftOrdersBySKUPaginated.DraftOrdersBySKU()
                                                            draftOrdersBySKU.orders
                                                            draftOrdersList!![i] = draftOrdersBySKU
                                                            //update the subsequent entries for cartlist also
                                                            for (j in cartListData!!.indices) {
                                                                if (cartListData!![j].order != null && cartListData!![j].order?.orderId == order.orderId) {
                                                                    cartListData!![j].order = order
                                                                }
                                                            }
                                                            break
                                                        }
                                                    }
                                                    updateDataList(draftOrdersList)
                                                } else {
                                                    //match the orderId and update the draftOrderList and notify data set changed
                                                    val order: Orders = orderData?.data!!
                                                    for (i in draftOrdersList!!.indices) {
                                                        if (order.orderId == draftOrdersList!![i].orders
                                                                .orderId
                                                        ) {
                                                            if (draftOrdersList!![i].orders
                                                                    .isOrderSelected
                                                            ) {
                                                                order.isOrderSelected = true
                                                            }
                                                            //check if order has some error after updating and cannot be placed uncomment once API fixed
                                                            if (orderData?.errors != null && orderData.errors!!
                                                                    .size!! > 0
                                                            ) {
                                                                for (j in 0 until orderData.errors
                                                                    ?.size!!) {
                                                                    if (orderData.errors!!.get(j)
                                                                            .isErrorCode(ErrorModel.ErrorCode.REQUEST_VALIDATION_ERROR)
                                                                    ) {
                                                                        order.isAllow = false
                                                                        order.isErrorBelowMoq =
                                                                            false
                                                                        break
                                                                    }
                                                                }
                                                            } else {
                                                                order.isAllow = true
                                                                order.isErrorBelowMoq = true
                                                            }
                                                            val draftOrdersBySKU =
                                                                DraftOrdersBySKUPaginated.DraftOrdersBySKU()
                                                            draftOrdersBySKU.orders
                                                            draftOrdersList!![i] = draftOrdersBySKU
                                                            //update the subsequent entries for cartlist also
                                                            for (j in cartListData!!.indices) {
                                                                if (cartListData!![j].order != null && cartListData!![j].order?.orderId == order.orderId) {
                                                                    cartListData!![j].order = order
                                                                }
                                                            }
                                                            break
                                                        }
                                                    }
                                                    updateDataList(draftOrdersList)
                                                }
                                            }

                                            override fun onErrorResponse(error: VolleyError?) {}
                                        })
                                }

                                override fun onErrorResponse(error: VolleyError?) {
                                    listener.showLoadingOverlay(false)
                                    //set old delivery date again
                                    getDeliveryDateData(cartListData!![viewHolderEditOrder.getAdapterPosition()].order)!!.deliveryDates
                                    updateDataList(draftOrdersList)
                                }
                            })
                    }
                })
        }
        lstDeliveryDateList.setAdapter(adapter)
        for (i in dates.indices) {
            if (dates[i].dateSelected) {
                lstDeliveryDateList.scrollToPosition(i)
            }
        }
        d.show()
    }

    fun updateDataList(draftOrderListNew: MutableList<DraftOrdersBySKUPaginated.DraftOrdersBySKU>?) {
        draftOrdersList = draftOrderListNew
        createDisplayCartList(draftOrdersList)
        //set the value for select all flag
        Log.d("CartLIST", cartListData.toString() + "*********")
        setAllOrderSelected(draftOrdersList)
        notifyDataSetChanged()
    }

    fun getSwipeDeleteRowData(position: Int): CartListDraftOrderUI {
        return cartListData!![position]
    }

    fun removeAt(position: Int) {
        val order: Orders? = cartListData!![position].order!!
        val orderClone: Orders? = ZeemartBuyerApp.fromJson<Orders>(
            ZeemartBuyerApp.gsonExposeExclusive.toJson(order),
            Orders::class.java
        )
        val product: Product? = cartListData!![position].product!!
        listener.showLoadingOverlay(true)
        if (orderClone != null && product != null) {
            if (orderClone.products?.size == 1) {
                if (orderClone.products?.get(0)?.sku == product.sku) {
                    //delete the entire draft order
                    listener.showLoadingOverlay(true)
                    DeleteOrder(context, object : DeleteOrder.GetResponseStatusListener {
                        override fun onSuccessResponse(status: String?) {
                            listener.showLoadingOverlay(false)
                            val iterator: MutableIterator<DraftOrdersBySKUPaginated.DraftOrdersBySKU> =
                                draftOrdersList!!.iterator()
                            while (iterator.hasNext()) {
                                val OrderFromDraftList: DraftOrdersBySKUPaginated.DraftOrdersBySKU =
                                    iterator.next()
                                if (OrderFromDraftList.orders.orderId
                                        .equals(orderClone.orderId)
                                ) {
                                    iterator.remove()
                                }
                            }
                            updateDataList(draftOrdersList)
                            listener.updatePlaceOrderButton(draftOrdersList)
                        }

                        override fun onErrorResponse(error: VolleyError?) {
                            listener.showLoadingOverlay(false)
                            if (error?.networkResponse != null && error.message != null) {
                                if (error.networkResponse.statusCode.toString() == ServiceConstant.STATUS_CODE_403_404_405_MESSAGE) {
                                    ZeemartBuyerApp.getToastRed(error.message)
                                }
                            }
                        }
                    }).deleteOrderData(orderClone.orderId, orderClone.outlet?.outletId)
                }
            } else {
                //delete the single product from the order
                val iterator: MutableIterator<Product> =
                    orderClone.products?.iterator() as MutableIterator<Product>
                while (iterator.hasNext()) {
                    val dummyProduct = iterator.next()
                    if (dummyProduct.sku == product.sku && dummyProduct.unitSize == product.unitSize) {
                        iterator.remove()
                    }
                }
                val outlets: MutableList<Outlet>? = ArrayList<Outlet>()
                orderClone.outlet?.let { outlets?.add(it) }
                val requestJson: String =
                    SingleDraftOrderUpdateRequest.getJsonRequestFromOrder(orderClone)
                OrderManagement.singleDraftOrderUpdate(
                    context,
                    requestJson,
                    orderClone.outlet?.outletId,
                    outlets,
                    object : OrderManagement.SingleDraftOrderUpdateListener {
                        override fun onSuccessResponse(orderData: OrderBaseResponse.Data?) {
                            val updatedOrder: Orders = orderData?.data!!
                            if (orderData.errors != null && orderData.errors?.size!! > 0) {
                                Log.d("ORDER_UPDATE", "product delete failure")
                                for (j in 0 until orderData.errors?.size!!) {
                                    if (orderData.errors?.get(j)
                                            ?.isErrorCode(ErrorModel.ErrorCode.REQUEST_VALIDATION_ERROR) == true
                                    ) {
                                        orderData.data!!.isErrorBelowMoq
                                        break
                                    }
                                }
                                updatedOrder.isAllow = false
                                for (i in draftOrdersList!!.indices) {
                                    if (draftOrdersList!![i].orders.orderId
                                            .equals(updatedOrder.orderId)
                                    ) {
                                        draftOrdersList!![i].orders.isAllow
                                    }
                                }
                                for (i in 0 until orderData.errors?.size!!) {
                                    val errorMessage: String =
                                        orderData.errors!!.get(i).message!!
                                    listener.showLoadingOverlay(false)
                                    DialogHelper.displayErrorMessageDialog(
                                        context as Activity,
                                        "",
                                        errorMessage
                                    )
                                }
                            } else {
                                Log.d("ORDER_UPDATE", "product delete successfull")
                                updatedOrder.isAllow = true
                                updatedOrder.isErrorBelowMoq = true
                                for (i in draftOrdersList!!.indices) {
                                    if (draftOrdersList!![i].orders.orderId
                                            .equals(updatedOrder.orderId)
                                    ) {
                                        if (draftOrdersList!![i].orders.isOrderSelected) {
                                            updatedOrder.isOrderSelected = true
                                        }
                                        val draftOrdersBySKU =
                                            DraftOrdersBySKUPaginated.DraftOrdersBySKU()
                                        draftOrdersBySKU.orders = updatedOrder
                                        draftOrdersList!![i] = draftOrdersBySKU
                                    }
                                }
                            }
                            updateDataList(draftOrdersList)
                            listener.showLoadingOverlay(false)
                        }

                        override fun onErrorResponse(error: VolleyError?) {
                            Log.d("ORDER_UPDATE", "product delete failure")
                            updateDataList(draftOrdersList)
                            listener.showLoadingOverlay(false)
                        }
                    })
            }
        }
    }

    companion object {
        private const val ITEM_TYPE_SELECT_ALL = 0
        private const val ITEM_TYPE_SUPPLIER_HEADER = 1
        private const val ITEM_TYPE_PRODUCT = 2
        private const val ITEM_TYPE_SPECIAL_REQUEST = 3
        private const val ITEM_TYPE_EDIT_ORDER = 4
        private val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH: Int = CommonMethods.dpToPx(36)
        private val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT: Int = CommonMethods.dpToPx(36)
        private const val REQUEST_CODE_SELECTED_DRAFT_ORDER = 101
    }
}