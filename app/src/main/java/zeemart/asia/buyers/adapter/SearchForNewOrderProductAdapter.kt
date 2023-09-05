package zeemart.asia.buyers.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.ProductDataHelper.calculateTotalPriceChanged
import zeemart.asia.buyers.helper.ProductDataHelper.changeQuantityUOMTextFontSize
import zeemart.asia.buyers.helper.ProductDataHelper.createSelectedProductObject
import zeemart.asia.buyers.helper.ProductDataHelper.getKeyProductMap
import zeemart.asia.buyers.helper.ProductDataHelper.updateFavSkuStatus
import zeemart.asia.buyers.helper.ProductQuantityChangeDialogHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.StocksList
import zeemart.asia.buyers.models.UnitSizeModel.Companion.getValueDecimalAllowedCheck
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Inventory
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.order.SearchForNewOrderMgr
import zeemart.asia.buyers.orders.ProductDetailActivity
import java.util.*
import java.util.regex.Pattern

/**
 * Created by Muthumari on 21/10/2019.
 */
class SearchForNewOrderProductAdapter(
    var context: Context,
    var lstSearchDataArraylist: ArrayList<SearchForNewOrderMgr>,
    private val outletId: String,
    private val supplierDetails: DetailSupplierDataModel?,
    private val selectedProductsMap: MutableMap<String, Product>?,
    private val ordersList: List<Orders>?,
    supplierClickListener: SupplierClickListener,
    private val listener: SelectedProductsListener,
    viewAllClickListener: onViewAllClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable,
    ProductQuantityChangeDialogHelper.ProductDataChangeListener {
    private var products: MutableList<ProductDetailBySupplier?> = ArrayList()
    private lateinit var searchStringArray: Array<String>
    private var isDisplayStockInfo = false
    private lateinit var dialog: CustomChangeQuantityDialog
    private var item: Product? = null
    private val mFilter: FilterProducts
    private var supplierName: String? = null
    private var itemPrice: String? = null
    private var itemQuantity: String? = null
    private var itemUnitSize: String? = null
    private var supplierList: MutableList<DetailSupplierDataModel?> = ArrayList()
    private var searchedString = ""
    private val supplierClickListener: SupplierClickListener
    private val viewAllClickListener: onViewAllClickListener

    init {
        mFilter = FilterProducts()
        this.supplierClickListener = supplierClickListener
        this.viewAllClickListener = viewAllClickListener
    }

    override fun getItemViewType(position: Int): Int {
        val data = lstSearchDataArraylist[position]
        if (data.isHeader) {
            return VIEW_TYPE_HEADER
        } else if (data.detailSupplierDataModel != null) {
            return SUPPLIER_VIEW_TYPE
        } else if (data.productDetailBySupplier != null) {
            return PRODUCT_VIEW_TYPE
        } else return if (data.isViewAll) {
            VIEW_ALL_VIEW_TYPE
        } else {
            0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_HEADER) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_header_dashboard, parent, false)
            return ViewHolderHeader(itemView)
        } else if (viewType == SUPPLIER_VIEW_TYPE) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_supplier_list_new_order, parent, false)
            return ViewHolderSupplier(itemView)
        } else if (viewType == VIEW_ALL_VIEW_TYPE) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.lyt_view_all_button, parent, false)
            return ViewHolderViewAll(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_product_list_row, parent, false)
            return ViewHolderProducts(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        if (lstSearchDataArraylist[currentPosition].isHeader) {
            val viewHolderHeader = holder as ViewHolderHeader
            val headerWithCount =
                lstSearchDataArraylist[currentPosition].headerCount.toString() + " " + lstSearchDataArraylist[currentPosition].header
            viewHolderHeader.txtHeader.text = headerWithCount
        }
        if (lstSearchDataArraylist[currentPosition].isViewAll) {
            val viewHolderViewAll = holder as ViewHolderViewAll
            viewHolderViewAll.btnViewAll.setOnClickListener(View.OnClickListener { viewAllClickListener.onSupplierViewAllClicked() })
        } else if (lstSearchDataArraylist[currentPosition].detailSupplierDataModel != null) {
            supplierList.add(lstSearchDataArraylist[currentPosition].detailSupplierDataModel)
            val viewHolderSupplier = holder as ViewHolderSupplier
            if ((searchedString == "") && lstSearchDataArraylist[currentPosition].detailSupplierDataModel!!.supplier.supplierName != null) {
                viewHolderSupplier.txtSupplierName.text =
                    lstSearchDataArraylist.get(currentPosition).detailSupplierDataModel!!.supplier.supplierName
            } else {
                val itemName =
                    lstSearchDataArraylist[currentPosition].detailSupplierDataModel!!.supplier.supplierName + ""
                val sb = SpannableStringBuilder(itemName)
                val p = Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
                val m = p.matcher(itemName)
                while (m.find()) {
                    sb.setSpan(
                        ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
                        m.start(),
                        m.end(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
                viewHolderSupplier.txtSupplierName.text = sb
            }
            if (lstSearchDataArraylist[currentPosition].detailSupplierDataModel!!.deliveryDates != null && lstSearchDataArraylist[position].detailSupplierDataModel!!.deliveryDates!!.size > 0) {
                viewHolderSupplier.txtDateType.text = String.format(
                    context.getString(R.string.format_next_delivery),
                    DateHelper.getDateInDateMonthFormat(
                        lstSearchDataArraylist.get(currentPosition).detailSupplierDataModel!!.deliveryDates!!.get(
                            0
                        ).deliveryDate
                    )
                )
            }
            viewHolderSupplier.lytSupplierList.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    supplierClickListener.onSupplierClicked(lstSearchDataArraylist[currentPosition].detailSupplierDataModel)
                }
            })
            if (StringHelper.isStringNullOrEmpty(lstSearchDataArraylist[currentPosition].detailSupplierDataModel!!.supplier.logoURL)) {
                viewHolderSupplier.imgSupplier.visibility = View.INVISIBLE
                viewHolderSupplier.lytSupplierThumbNail.visibility = View.VISIBLE
                viewHolderSupplier.lytSupplierThumbNail.setBackgroundColor(
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        lstSearchDataArraylist[currentPosition].detailSupplierDataModel!!.supplier.supplierName!!,
                        context
                    )
                )
                viewHolderSupplier.txtSupplierThumbNail.text =
                    CommonMethods.SupplierThumbNailShortCutText(
                        lstSearchDataArraylist.get(currentPosition).detailSupplierDataModel!!.supplier.supplierName!!
                    )
                viewHolderSupplier.txtSupplierThumbNail.setTextColor(
                    CommonMethods.SupplierThumbNailTextColor(
                        lstSearchDataArraylist[currentPosition].detailSupplierDataModel!!.supplier.supplierName!!,
                        context
                    )
                )
            } else {
                viewHolderSupplier.imgSupplier.visibility = View.VISIBLE
                viewHolderSupplier.lytSupplierThumbNail.visibility = View.GONE
                Picasso.get()
                    .load(lstSearchDataArraylist[currentPosition].detailSupplierDataModel!!.supplier.logoURL)
                    .placeholder(
                        R.drawable.placeholder_all
                    ).resize(PLACE_HOLDER_SUPPLIER_IMAGE_SIZE, PLACE_HOLDER_SUPPLIER_IMAGE_SIZE)
                    .into(viewHolderSupplier.imgSupplier)
            }
        } else if (lstSearchDataArraylist[currentPosition].productDetailBySupplier != null) {
            products.add(lstSearchDataArraylist[currentPosition].productDetailBySupplier)
            if (supplierDetails != null) {
                if (supplierDetails.supplier == null && lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier != null) {
                    supplierDetails.supplier =
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier!!
                }
            }
            if (ordersList != null && ordersList.size > 0) {
                for (j in ordersList.indices) {
                    for (k in ordersList[j].products!!.indices) {
                        if ((lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.sku == ordersList[j].products!![k].sku)) {
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isSelected =
                                true
                            itemQuantity = ordersList[j].products!![k].quantityDisplayValue
                            itemUnitSize = ordersList[j].products!![k].unitSize
                        }
                    }
                }
            }
            val viewHolderProducts = holder as ViewHolderProducts
            if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isSelected) {
                //show the quantity layout with pre set quantity
                setSelectedProductLayout(viewHolderProducts, currentPosition)
            } else {
                displayAddToOrderLayout(viewHolderProducts, currentPosition)
            }
            viewHolderProducts.txtAddToOrder.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    listener.onProductSelected(lstSearchDataArraylist[currentPosition].productDetailBySupplier)
                    listener.onProductSelectedForRecentSearch(lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productName)
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isSelected = true
                    val item = createSelectedProductObject(
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier
                    )
                    if (item != null) {
                        val key = getKeyProductMap(item.sku, item.unitSize)
                        selectedProductsMap!![key] = item
                        SharedPref.writeLong(
                            SharedPref.ORDER_ITEMS_COUNT,
                            selectedProductsMap.size.toLong()
                        )
                    }
                    setSelectedProductLayout(viewHolderProducts, currentPosition)
                }
            })
            if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![0].price != null) itemPrice =
                lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![0].price!!.getDisplayValueWithUom(
                    returnShortNameValueUnitSize(
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize
                    )
                )
            if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                viewHolderProducts.txtProductUnitPrice.visibility = View.VISIBLE
                viewHolderProducts.txtProductUnitPrice.text = itemPrice
            } else {
                viewHolderProducts.txtProductUnitPrice.visibility = View.GONE
            }
            if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.images != null && lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.images!!.size > 0) {
                val imagesArray: List<ImagesModel?>? =
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.images
                var imageUrl: String? = null
                for (i in imagesArray!!.indices) {
                    if (!StringHelper.isStringNullOrEmpty(imageUrl)) {
                        break
                    }
                    if (imagesArray[i] != null) {
                        if ((imagesArray[i]!!.imageFileNames != null) && (imagesArray[i]!!.imageFileNames.size > 0
                                    ) && !StringHelper.isStringNullOrEmpty(imagesArray[i]!!.imageURL)
                        ) {
                            for (j in imagesArray[i]!!.imageFileNames.indices) {
                                if (!StringHelper.isStringNullOrEmpty(imagesArray[i]!!.imageFileNames[j])) {
                                    imageUrl =
                                        imagesArray[i]!!.imageURL + imagesArray[i]!!.imageFileNames[j]
                                    break
                                }
                            }
                        }
                    }
                }
                if (StringHelper.isStringNullOrEmpty(imageUrl)) {
                    viewHolderProducts.imgProductImage.visibility = View.GONE
                } else {
                    viewHolderProducts.imgProductImage.visibility = View.VISIBLE
                    Picasso.get().load(imageUrl).fit().into(viewHolderProducts.imgProductImage)
                }
            } else {
                viewHolderProducts.imgProductImage.visibility = View.GONE
            }
            viewHolderProducts.imgProductImage.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    listener.onItemClick(
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplierProductCode,
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productName
                    )
                    callProductDetail(currentPosition)
                }
            })
            if (selectedProductsMap != null && selectedProductsMap.size > 0) {
                val key = getKeyProductMap(
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.sku,
                    (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize)!!
                )
                item = selectedProductsMap[key]
            }
            //Product availability check
            var supplierInventory: Inventory? = null
            if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier != null && lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier!!.settings != null) {
                supplierInventory =
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier!!.settings!!.inventory
            }
            if ((supplierInventory != null) && (supplierInventory.status != null) && supplierInventory.isStatus(
                    Inventory.Status.ACTIVE
                ) && !supplierInventory.allowNegative!!
            ) {
                isDisplayStockInfo = true
            }
            val isItemAvailable =
                getIsItemUnAvailable(lstSearchDataArraylist[currentPosition].productDetailBySupplier)
            if (isDisplayStockInfo) {
                if (isItemAvailable) {
                    viewHolderProducts.lytProductName.alpha = 1.0f
                    viewHolderProducts.lytProductItems.isClickable = true
                    isDisplayStockInfo = false
                } else {
                    viewHolderProducts.lytProductQuantity.visibility = View.GONE
                    viewHolderProducts.lytAddToOrderAndUOM.visibility = View.VISIBLE
                    viewHolderProducts.lytProductName.alpha = ALPHA_ROW
                    viewHolderProducts.txtProductUnitPrice.alpha = ALPHA_ROW
                    viewHolderProducts.txtAddToOrder.isClickable = false
                    viewHolderProducts.txtAddToOrder.setText(R.string.txt_currently_unavailable)
                    viewHolderProducts.txtAddToOrder.setTextColor(context.resources.getColor(R.color.grey_medium))
                    isDisplayStockInfo = false
                }
            }
            val productCodeTag =
                lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplierProductCode
            if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier != null && lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier!!.supplierName != null) {
                supplierName =
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplier!!.supplierName
            }
            if (searchStringArray != null && searchStringArray!!.size > 0) {
                val itemName =
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productName + ""
                val sb = SpannableStringBuilder(itemName)
                var searchedString = ""
                for (i in searchStringArray!!.indices) {
                    searchedString = searchedString + "|" + searchStringArray!![i]
                }
                searchedString = searchedString.substring(1, searchedString.length)
                val p = Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
                val m = p.matcher(itemName)
                while (m.find()) {
                    val color = context.resources.getColor(R.color.text_blue)
                    sb.setSpan(
                        ForegroundColorSpan(color),
                        m.start(),
                        m.end(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
                val itemCustomName =
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productCustomName + ""
                val sbCustomName = SpannableStringBuilder(itemCustomName)
                val pCustomName =
                    Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
                val mCustomName = pCustomName.matcher(itemCustomName)
                while (mCustomName.find()) {
                    val color = context.resources.getColor(R.color.text_blue)
                    sbCustomName.setSpan(
                        ForegroundColorSpan(color),
                        mCustomName.start(),
                        mCustomName.end(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
                if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productCustomName != null) {
                    viewHolderProducts.txtProductCustomName.visibility = View.VISIBLE
                    viewHolderProducts.txtItemName.text = sbCustomName
                    viewHolderProducts.txtProductCustomName.text = sb
                } else {
                    viewHolderProducts.txtItemName.text = sb
                    viewHolderProducts.txtProductCustomName.visibility = View.GONE
                }
                if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                    viewHolderProducts.txtProductCodeTag.text = supplierName
                } else {
                    viewHolderProducts.txtProductCodeTag.text = "$productCodeTag • $supplierName"
                }
            } else {
                if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productCustomName != null) {
                    viewHolderProducts.txtProductCustomName.text =
                        lstSearchDataArraylist.get(currentPosition).productDetailBySupplier!!.productName
                    viewHolderProducts.txtProductCustomName.visibility = View.VISIBLE
                    viewHolderProducts.txtItemName.text =
                        lstSearchDataArraylist.get(currentPosition).productDetailBySupplier!!.productCustomName
                } else {
                    viewHolderProducts.txtItemName.text =
                        lstSearchDataArraylist.get(currentPosition).productDetailBySupplier!!.productName
                    viewHolderProducts.txtProductCustomName.visibility = View.GONE
                }
                if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                    viewHolderProducts.txtProductCodeTag.text = supplierName
                } else {
                    viewHolderProducts.txtProductCodeTag.text = "$productCodeTag • $supplierName"
                }
            }
            viewHolderProducts.lytProductName.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    listener.onItemClick(
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.supplierProductCode,
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productName
                    )
                    callProductDetail(currentPosition)
                }
            })
            if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.favourites != null && lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.favourites!!.size > 0) {
                for (i in lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.favourites!!.indices) {
                    if ((lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.favourites!![i] == SharedPref.read(
                            SharedPref.USER_ID,
                            ""
                        ))
                    ) {
                        viewHolderProducts.imgFavouriteProduct.setImageResource(R.drawable.favourite_blue)
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isFavourite =
                            true
                    }
                }
            } else {
                viewHolderProducts.imgFavouriteProduct.setImageResource(R.drawable.favourite_grey)
            }
            viewHolderProducts.imgFavouriteProduct.setOnClickListener(object :
                View.OnClickListener {
                override fun onClick(v: View) {
                    if (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isFavourite) {
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isFavourite =
                            false
                        updateFavSkuStatus(
                            context,
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.sku,
                            false
                        )
                        viewHolderProducts.imgFavouriteProduct.setImageResource(R.drawable.favourite_grey)
                        AnalyticsHelper.logAction(
                            context,
                            AnalyticsHelper.TAP_ADD_TO_ORDER_FAVOURITE,
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier
                        )
                    } else {
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isFavourite =
                            true
                        updateFavSkuStatus(
                            context,
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.sku,
                            true
                        )
                        viewHolderProducts.imgFavouriteProduct.setImageResource(R.drawable.favourite_blue)
                        AnalyticsHelper.logAction(
                            context,
                            AnalyticsHelper.TAP_ADD_TO_ORDER_FAVOURITE,
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier
                        )
                    }
                }
            })
            viewHolderProducts.txtProductQuantity.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (dialog == null || (dialog != null && !dialog!!.isShowing)) {
                        val key = getKeyProductMap(
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.sku,
                            (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize)!!
                        )
                        val createOrderHelperDialog = ProductQuantityChangeDialogHelper(
                            selectedProductsMap!![key],
                            context,
                            supplierDetails!!,
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier!!,
                            this@SearchForNewOrderProductAdapter
                        )
                        dialog = createOrderHelperDialog.createChangeQuantityDialog()
                        dialog.getWindow()!!
                            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    }
                    setSelectedProductLayout(viewHolderProducts, currentPosition)
                }
            })
            viewHolderProducts.btnIncreaseQuantity.setOnClickListener(object :
                View.OnClickListener {
                override fun onClick(v: View) {
                    listener.onProductSelectedForRecentSearch(lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.productName)
                    var product: Product?
                    val key = getKeyProductMap(
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.sku,
                        (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize)!!
                    )
                    if (selectedProductsMap != null && selectedProductsMap.size > 0) {
                        product = selectedProductsMap[key]
                    } else {
                        product = item
                    }
                    var quantity = product!!.quantity
                    quantity = quantity + 1
                    product.quantity = (quantity)
                    lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isSelected = true
                    product = calculateTotalPriceChanged(product)
                    viewHolderProducts.txtProductQuantity.text = getValueDecimalAllowedCheck(
                        product!!.unitSize, product.quantity
                    ) + " "
                    viewHolderProducts.txtProductQuantity.append(
                        changeQuantityUOMTextFontSize(
                            returnShortNameValueUnitSizeForQuantity(
                                product.unitSize
                            ), context
                        )
                    )
                }
            })
            viewHolderProducts.btnReduceQuantity.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    var product: Product?
                    val key = getKeyProductMap(
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.sku,
                        (lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize)!!
                    )
                    if (selectedProductsMap != null && selectedProductsMap.size > 0) {
                        product = selectedProductsMap[key]
                    } else {
                        product = item
                    }
                    var quantity = product!!.quantity
                    if (quantity == 0.0) {
                        lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.isSelected =
                            false
                        listener.onProductDeselected(lstSearchDataArraylist[currentPosition].productDetailBySupplier)
                        //reset all the price list uom
                        for (i in lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!!.indices) {
                            lstSearchDataArraylist[currentPosition].productDetailBySupplier!!.priceList!![i].selected =
                                false
                        }
                        selectedProductsMap!!.remove(key)
                        displayAddToOrderLayout(viewHolderProducts, currentPosition)
                    } else {
                        quantity = quantity - 1
                        product!!.quantity = (quantity)
                        product = calculateTotalPriceChanged(product)
                        viewHolderProducts.txtProductQuantity.text = getValueDecimalAllowedCheck(
                            product!!.unitSize, product!!.quantity
                        ) + " "
                        viewHolderProducts.txtProductQuantity.append(
                            changeQuantityUOMTextFontSize(
                                returnShortNameValueUnitSizeForQuantity(
                                    product!!.unitSize
                                ), context
                            )
                        )
                    }
                }
            })
        }
    }

    fun callProductDetail(position: Int) {
        AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ITEM_ADD_TO_ORDER_PRODUCT_DETAILS)
        val newIntent = Intent(context, ProductDetailActivity::class.java)
        val productDetails = ZeemartBuyerApp.gsonExposeExclusive.toJson(
            lstSearchDataArraylist[position].productDetailBySupplier,
            ProductDetailBySupplier::class.java
        )
        newIntent.putExtra(ZeemartAppConstants.PRODUCT_DETAILS_JSON, productDetails)
        val supplierDetail = ZeemartBuyerApp.gsonExposeExclusive.toJson(
            supplierDetails
        )
        newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetail)
        newIntent.putExtra(
            ZeemartAppConstants.PRODUCT_SKU,
            lstSearchDataArraylist[position].productDetailBySupplier!!.sku
        )
        newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outletId)
        newIntent.putExtra(
            ZeemartAppConstants.CALLED_FROM,
            ZeemartAppConstants.CALLED_FROM_SUPPLIER_SEARCH_LIST
        )
        if (lstSearchDataArraylist[position].productDetailBySupplier!!.supplier != null) newIntent.putExtra(
            ZeemartAppConstants.SUPPLIER_NAME,
            lstSearchDataArraylist[position].productDetailBySupplier!!.supplier!!.supplierName
        )
        newIntent.putExtra(
            ZeemartAppConstants.SUPPLIER_ID,
            lstSearchDataArraylist[position].productDetailBySupplier!!.supplierId
        )
        (context as Activity).startActivityForResult(
            newIntent,
            ZeemartAppConstants.RequestCode.ProductDetailsActivity
        )
    }

    fun displayAddToOrderLayout(holder: ViewHolderProducts, position: Int) {
        holder.lytProductQuantity.visibility = View.GONE
        holder.lytQuantitySelection.setBackgroundColor(context.resources.getColor(R.color.faint_white))
        holder.lytAddToOrderAndUOM.visibility = View.VISIBLE
        holder.txtAddToOrder.text = context.getString(R.string.txt_add_to_order)
        holder.txtAddToOrder.setTextColor(context.resources.getColor(R.color.text_blue))
        holder.txtItemUom.text = returnShortNameValueUnitSize(
            lstSearchDataArraylist.get(position).productDetailBySupplier!!.priceList!!.get(0).unitSize
        )
    }

    fun setSelectedProductLayout(holder: ViewHolderProducts, position: Int) {
        holder.lytQuantitySelection.setBackgroundColor(context.resources.getColor(R.color.white))
        //show the quantity layout with pre set quantity
        holder.lytProductQuantity.visibility = View.VISIBLE
        holder.lytAddToOrderAndUOM.visibility = View.GONE
        if (selectedProductsMap != null && selectedProductsMap.size > 0) {
            val key = getKeyProductMap(
                lstSearchDataArraylist[position].productDetailBySupplier!!.sku,
                (lstSearchDataArraylist[position].productDetailBySupplier!!.priceList!![0].unitSize)!!
            )
            val selectedProduct = selectedProductsMap[key]
            if (selectedProduct != null) {
                holder.txtProductQuantity.text = selectedProduct.quantityDisplayValue + " "
                holder.txtProductQuantity.append(
                    changeQuantityUOMTextFontSize(
                        returnShortNameValueUnitSizeForQuantity(selectedProduct.unitSize), context
                    )
                )
            }
        } else if (itemQuantity != null) {
            holder.txtProductQuantity.text = "$itemQuantity "
            holder.txtProductQuantity.append(
                changeQuantityUOMTextFontSize(
                    returnShortNameValueUnitSizeForQuantity(itemUnitSize), context
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return lstSearchDataArraylist.size
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    override fun onDataChange(product: Product?) {
        notifyDataSetChanged()
    }

    inner class ViewHolderSupplier(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSupplierName: TextView
        var imgSupplier: ImageView
        var txtDateType: TextView
        var lytSupplierList: RelativeLayout
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.img_supplier_new_order)
            txtSupplierName = itemView.findViewById(R.id.txt_supplier_name_new_order)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtDateType = itemView.findViewById(R.id.txt_date_type_new_order)
            setTypefaceView(txtDateType, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            lytSupplierList = itemView.findViewById(R.id.layout_supplier_list)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    inner class ViewHolderViewAll(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var btnViewAll: TextView

        init {
            btnViewAll = itemView.findViewById(R.id.btn_view_all)
            setTypefaceView(btnViewAll, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        }
    }

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeader: TextView

        init {
            txtHeader = itemView.findViewById(R.id.txt_dashboard_sublist_header)
            txtHeader.setTextColor(context.resources.getColor(R.color.black))
            setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtHeader.textSize = 16f
            val params = txtHeader.layoutParams as LinearLayout.LayoutParams
            params.setMargins(15, 0, 0, 0)
            txtHeader.layoutParams = params
        }
    }

    inner class ViewHolderProducts(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtProductCodeTag: TextView
        var txtAddToOrder: TextView
        var lytProductName: RelativeLayout
        var lytProductItems: LinearLayout
        var txtProductCustomName: TextView
        var txtProductUnitPrice: TextView
        var txtProductQuantity: TextView
        var btnIncreaseQuantity: ImageButton
        var btnReduceQuantity: ImageButton
        var imgFavouriteProduct: ImageButton
        var lytProductQuantity: LinearLayout
        var lytQuantitySelection: RelativeLayout
        var txtItemUom: TextView
        var lytAddToOrderAndUOM: LinearLayout
        var imgProductImage: ImageView
        var txtSupplierName: TextView? = null

        init {
            txtProductCodeTag = itemView.findViewById(R.id.txt_product_code_tag)
            lytProductName = itemView.findViewById(R.id.lyt_product_name)
            txtItemName = itemView.findViewById(R.id.txt_product_name)
            txtAddToOrder = itemView.findViewById(R.id.txt_add_order)
            lytProductItems = itemView.findViewById(R.id.lyt_product_details)
            imgFavouriteProduct = itemView.findViewById(R.id.img_favourite)
            btnReduceQuantity = itemView.findViewById(R.id.btn_reduce_quantity)
            txtProductQuantity = itemView.findViewById(R.id.txt_quantity_value)
            btnIncreaseQuantity = itemView.findViewById(R.id.btn_inc_quantity)
            lytProductQuantity = itemView.findViewById(R.id.lyt_quantity_sku)
            lytQuantitySelection = itemView.findViewById(R.id.lyt_selection)
            txtProductUnitPrice = itemView.findViewById(R.id.txt_product_unit_price)
            txtProductCustomName = itemView.findViewById(R.id.txt_product_changed_name)
            txtItemUom = itemView.findViewById(R.id.txt_uom_name)
            lytAddToOrderAndUOM = itemView.findViewById(R.id.lyt_add_to_order_and_uom)
            imgProductImage = itemView.findViewById(R.id.img_product_image)
            imgProductImage.visibility = View.GONE
            setTypefaceView(txtProductCodeTag, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtAddToOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtProductUnitPrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(
                txtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(
                txtProductCustomName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
            setTypefaceView(txtItemUom, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        }
    }

    private inner class FilterProducts() : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null && constraint.length > 0) {
                searchedString = constraint as String
                searchStringArray =
                    searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val tempList: MutableList<ProductDetailBySupplier?> = ArrayList()
                val tempListSupplier = ArrayList<DetailSupplierDataModel>()
                // search content in friend list
                for (i in products.indices) {
                    if (products[i]!!.productName!!.lowercase(Locale.getDefault()).contains(
                            constraint.toString().lowercase(
                                Locale.getDefault()
                            )
                        )
                    ) {
                        tempList.add(products[i])
                    }
                }
                for (i in supplierList.indices) {
                    if (!supplierList[i]!!.isFavoriteProducts && supplierList[i]!!.supplier.supplierName!!.lowercase(
                            Locale.getDefault()
                        ).contains(constraint.toString().lowercase(Locale.getDefault()))
                    ) {
                        tempListSupplier.add(supplierList[i]!!)
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
                filterResults.values = tempListSupplier
            } else {
                searchStringArray != null
                filterResults.count = products.size
                filterResults.values = products
                filterResults.values = supplierList
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null && results.count > 0) {
                products = results.values as MutableList<ProductDetailBySupplier?>
                supplierList = results.values as MutableList<DetailSupplierDataModel?>
                notifyDataSetChanged()
            }
        }
    }

    private fun getIsItemUnAvailable(product: ProductDetailBySupplier?): Boolean {
        var isItemAvailable = false
        val productPriceList: List<ProductPriceList>? = product!!.priceList
        if (productPriceList != null && productPriceList.size > 0) {
            for (i in productPriceList.indices) {
                if ((productPriceList[i].status == ZeemartAppConstants.VISIBLE)) {
                    val uomStockMap = createUnitSizeStockMap(product)
                    if (uomStockMap.containsKey(productPriceList[i].unitSize)) {
                        val stockAvailable =
                            calculateStockAvailable(uomStockMap[productPriceList[i].unitSize])
                        if (stockAvailable > 0 && stockAvailable > (productPriceList[i].moq)!!) {
                            isItemAvailable = true
                            break
                        }
                    }
                }
            }
        }
        return isItemAvailable
    }

    /**
     * Calculate the total stock available
     *
     * @param stock
     * @return
     */
    private fun calculateStockAvailable(stock: StocksList?): Double {
        var totalBought = 0.0
        var stockLeft = 0.0
        if (stock!!.orderedQuantity != null && stock.orderedQuantity!!.size > 0) for (i in stock.orderedQuantity!!.indices) {
            totalBought = totalBought + stock.orderedQuantity!![i]
        }
        if (stock.stockQuantity != null) {
            stockLeft = stock.stockQuantity!! - totalBought
        }
        return stockLeft
    }

    private fun createUnitSizeStockMap(product: ProductDetailBySupplier?): Map<String?, StocksList> {
        val unitSizeStockMap: MutableMap<String?, StocksList> = HashMap()
        if (product!!.stocks != null && product.stocks!!.size > 0) {
            for (i in product.stocks!!.indices) {
                unitSizeStockMap[product.stocks!!.get(i).unitSize] = product.stocks!!.get(i)
            }
        }
        return unitSizeStockMap
    }

    interface SelectedProductsListener {
        fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?)
        fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?)
        fun onProductSelectedForRecentSearch(productName: String?)
        fun onItemClick(item: String?, ProductName: String?)
    }

    interface SupplierClickListener {
        fun onSupplierClicked(supplier: DetailSupplierDataModel?)
        fun noSearchSupplierResult(filterSuppliers: List<DetailSupplierDataModel?>?)
    }

    interface onViewAllClickListener {
        fun onSupplierViewAllClicked()
    }

    companion object {
        private val ALPHA_ROW = 0.5f
        private val PLACE_HOLDER_SUPPLIER_IMAGE_SIZE = CommonMethods.dpToPx(30)
        private val VIEW_TYPE_HEADER = 1
        private val SUPPLIER_VIEW_TYPE = 2
        private val PRODUCT_VIEW_TYPE = 3
        private val VIEW_ALL_VIEW_TYPE = 4
    }
}