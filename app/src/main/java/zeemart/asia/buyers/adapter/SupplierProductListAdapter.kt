package zeemart.asia.buyers.adapter

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.util.Log
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
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.ProductDataHelper.calculateTotalPriceChanged
import zeemart.asia.buyers.helper.ProductDataHelper.changeQuantityUOMTextFontSize
import zeemart.asia.buyers.helper.ProductDataHelper.createSelectedProductObject
import zeemart.asia.buyers.helper.ProductDataHelper.createSelectedProductObjectForBelowPar
import zeemart.asia.buyers.helper.ProductDataHelper.getKeyProductMap
import zeemart.asia.buyers.helper.ProductDataHelper.updateFavSkuStatus
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.login.BuyerLoginActivity
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.StocksList
import zeemart.asia.buyers.models.UnitSizeModel.Companion.getValueDecimal
import zeemart.asia.buyers.models.UnitSizeModel.Companion.getValueDecimalAllowedCheck
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Inventory
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.network.EssentialsApi.markEssentialsProductsAsFavourite
import zeemart.asia.buyers.network.EssentialsApi.removeEssentialsProductsAsFavourite
import zeemart.asia.buyers.orders.ProductDetailActivity
import java.util.*
import java.util.regex.Pattern

/**
 * Created by ParulBhandari on 12/8/2017.
 */
class SupplierProductListAdapter(
    isBlockToOrder: Boolean,
    var context: Context,
    private val products: List<ProductDetailBySupplier>,
    outletId: String,
    calledFrom: String,
    supplierName: String,
    supplierLogo: String,
    supplierId: String,
    supplierDetails: DetailSupplierDataModel?,
    selectedProductsMap: MutableMap<String, Product>?,
    searchStringArray: Array<String>?,
    productDetailBySupplier: ProductDetailBySupplier?,
    viewPrice: Boolean,
    selectedProductsListener: SelectedProductsListener
) : RecyclerView.Adapter<SupplierProductListAdapter.ViewHolder?>(), Filterable,
    ProductQuantityChangeDialogHelper.ProductDataChangeListener {
    private val outletId: String
    private var filterProducts: List<ProductDetailBySupplier>?
    private var searchStringArray: Array<String>?
    private val supplierName: String
    private val supplierLogo: String
    private var isDisplayStockInfo = false
    private val supplierId: String
    private var supplierDetails: DetailSupplierDataModel? = null
    private var selectedProductsMap: MutableMap<String, Product>?
    private var dialog: CustomChangeQuantityDialog? = null
    private val selectedProductsListener: SelectedProductsListener
    private val calledFrom: String
    private val selectedSearchedProductDetails: ProductDetailBySupplier?
    private var isFirstTimeHighLighted = true
    private var isBlockToOrder = false
    private var viewPrice = true
    lateinit var holder: ViewHolder

    init {
        filterProducts = products
        this.outletId = outletId
        this.supplierName = supplierName
        this.supplierLogo = supplierLogo
        this.supplierId = supplierId
        this.supplierDetails = supplierDetails
        this.selectedProductsMap = selectedProductsMap
        this.searchStringArray = searchStringArray
        this.selectedProductsListener = selectedProductsListener
        this.calledFrom = calledFrom
        selectedSearchedProductDetails = productDetailBySupplier
        this.isBlockToOrder = isBlockToOrder
        this.viewPrice = viewPrice
        var supplierInventory: Inventory? = null
        if (supplierDetails != null) {
            if (supplierDetails.supplier.settings != null) {
                supplierInventory = supplierDetails.supplier.settings!!.inventory
            }
            if ((supplierInventory != null) && (supplierInventory.status != null) && supplierInventory.isStatus(
                    Inventory.Status.ACTIVE
                ) && (supplierInventory.allowNegative == false)
            ) {
                isDisplayStockInfo = true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_product_list_row, parent, false)
        holder = ViewHolder(itemView)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder?.absoluteAdapterPosition!!
        if (isBlockToOrder) {
            holder!!.txtItemUom.visibility = View.GONE
            holder.txtAddToOrder.setOnClickListener(View.OnClickListener {
                AnalyticsHelper.logGuestAction(
                    context,
                    AnalyticsHelper.TAP_GUEST_MKTLIST_SIGNUP_TO_ORDER,
                    filterProducts!![currentPosition!!]
                )
                val intent = Intent(context, BuyerLoginActivity::class.java)
                intent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FOR_SIGN_UP
                )
                context.startActivity(intent)
            })
        } else {
            holder!!.txtAddToOrder.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    filterProducts!![currentPosition!!].isSelected = true
                    val item: Product?
                    if ((calledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST)) {
                        item = createSelectedProductObjectForBelowPar(
                            filterProducts!![currentPosition]
                        )
                    } else {
                        item = createSelectedProductObject(filterProducts!![currentPosition])
                    }
                    if (item != null) {
                        val key = getKeyProductMap(item.sku, item.unitSize)
                        selectedProductsMap!![key] = item
                        if (item.quantity > 0) {
                            selectedProductsListener.onProductSelected(filterProducts!![currentPosition])
                            selectedProductsListener.onProductSelectedForRecentSearch(filterProducts!![currentPosition].productName)
                        }
                    }
                    setSelectedProductLayout(holder, currentPosition)
                }
            })
        }
        if ((calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST)) {
            holder.imgFavouriteProduct.visibility = View.GONE
            if (filterProducts != null && filterProducts!![currentPosition].priceList!![0].dealPrice != null) {
                val itemPrice =
                    filterProducts!![currentPosition].priceList!![0].dealPrice!!.getDisplayValueWithUom(
                        returnShortNameValueUnitSizeForQuantity(
                            filterProducts!![currentPosition].priceList!![0].unitSize
                        )
                    )
                if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                    holder.txtProductUnitPrice.visibility = View.VISIBLE
                    if (filterProducts!![currentPosition].priceList!![0].originalPrice != null && filterProducts!![currentPosition].priceList!![0].originalPrice!!.amount != 0.0) {
                        holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.pinky_red))
                    } else {
                        holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    }
                    holder.txtProductUnitPrice.text = itemPrice
                } else {
                    holder.txtProductUnitPrice.visibility = View.GONE
                }
                if (filterProducts!![currentPosition].priceList!![0].originalPrice != null && filterProducts!![currentPosition].priceList!![0].originalPrice!!.amount != 0.0) {
                    val string = SpannableString(
                        filterProducts!![currentPosition].priceList!![0].originalPrice!!.getDisplayValueWithUom(
                            returnShortNameValueUnitSize(
                                filterProducts!![position].priceList!![0].unitSize
                            )
                        )
                    )
                    string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                    holder.txtProductStrikePrice.visibility = View.VISIBLE
                    holder.txtProductStrikePrice.text = string
                } else {
                    holder.txtProductStrikePrice.visibility = View.GONE
                }
            } else {
                if (filterProducts!![currentPosition].priceList!![0].originalPrice != null && filterProducts!![currentPosition].priceList!![0].originalPrice!!.amount != 0.0) {
                    val itemPrice =
                        filterProducts!![currentPosition].priceList!![0].originalPrice!!.getDisplayValueWithUom(
                            returnShortNameValueUnitSize(
                                filterProducts!![currentPosition].priceList!![0].unitSize
                            )
                        )
                    holder.txtProductUnitPrice.visibility = View.VISIBLE
                    holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    holder.txtProductUnitPrice.text = itemPrice
                } else {
                    holder.txtProductUnitPrice.visibility = View.GONE
                }
                holder.txtProductStrikePrice.visibility = View.GONE
            }
        } else if ((calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST)) {
            if (isBlockToOrder) {
                holder.imgFavouriteProduct.visibility = View.GONE
            } else {
                holder.imgFavouriteProduct.visibility = View.VISIBLE
            }
            if (filterProducts!![currentPosition].priceList!![0].essentialPrice != null) {
                val itemPrice =
                    filterProducts!![currentPosition].priceList!![0].essentialPrice!!.getDisplayValueWithUom(
                        returnShortNameValueUnitSize(
                            filterProducts!![currentPosition].priceList!![0].unitSize
                        )
                    )
                if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                    holder.txtProductUnitPrice.visibility = View.VISIBLE
                    if (filterProducts!![currentPosition].priceList!![0].originalPrice != null && filterProducts!![currentPosition].priceList!![0].originalPrice!!.amount != 0.0) {
                        holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.pinky_red))
                    } else {
                        holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    }
                    holder.txtProductUnitPrice.text = itemPrice
                } else {
                    holder.txtProductUnitPrice.visibility = View.GONE
                }
                if (filterProducts!![currentPosition].priceList!![0].originalPrice != null && filterProducts!![currentPosition].priceList!![0].originalPrice!!.amount != 0.0) {
                    val string = SpannableString(
                        filterProducts!![currentPosition].priceList!![0].originalPrice!!.getDisplayValueWithUom(
                            returnShortNameValueUnitSize(
                                filterProducts!![currentPosition].priceList!![0].unitSize
                            )
                        )
                    )
                    string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                    holder.txtProductStrikePrice.visibility = View.VISIBLE
                    holder.txtProductStrikePrice.text = string
                } else {
                    holder.txtProductStrikePrice.visibility = View.GONE
                }
            } else {
                if (filterProducts!![currentPosition].priceList!![0].originalPrice != null && filterProducts!![currentPosition].priceList!![0].originalPrice!!.amount != 0.0) {
                    val itemPrice =
                        filterProducts!![currentPosition].priceList!![0].originalPrice!!.getDisplayValueWithUom(
                            returnShortNameValueUnitSize(
                                filterProducts!![currentPosition].priceList!![0].unitSize
                            )
                        )
                    holder.txtProductUnitPrice.visibility = View.VISIBLE
                    holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.grey_medium))
                    holder.txtProductUnitPrice.text = itemPrice
                } else {
                    holder.txtProductUnitPrice.visibility = View.GONE
                }
                holder.txtProductStrikePrice.visibility = View.GONE
            }
        } else if ((calledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST)) {
            holder.txtProductUnitPrice.visibility = View.GONE
            if (filterProducts!![currentPosition].priceList!![0].onHandQty != null) {
                holder.txtOnHandTitle.visibility = View.VISIBLE
                holder.txtOnHandTitle.text = context.getString(R.string.txt_on_hand)
                holder.txtOnHandValue.visibility = View.VISIBLE
                var onHand: Double? = 0.0
                if (filterProducts!![currentPosition].priceList!![0].onHandQty != null) {
                    onHand = filterProducts!![currentPosition].priceList!![0].onHandQty
                }
                holder.txtOnHandValue.text =
                    getValueDecimal(filterProducts!!.get(currentPosition).priceList!!.get(0).onHandQty) + " " + returnShortNameValueUnitSize(
                        filterProducts!!.get(currentPosition).priceList!!.get(0).unitSize
                    )
                if (filterProducts!![currentPosition].priceList!![0].incomings != null && filterProducts!![currentPosition].priceList!![0].incomings!!.size > 0) {
                    holder.txtPlusSymbol.visibility = View.VISIBLE
                    holder.txtPlusSymbol.text = " + "
                    holder.txtIncomingExp.visibility = View.VISIBLE
                    holder.txtIncomingExp.text = context.getString(R.string.txt_exp)
                    holder.txtIncomingValue.visibility = View.VISIBLE
                    onHand =
                        onHand!! + (filterProducts!![currentPosition].priceList!![0].incomings!![0].incomingQty)!!
                    holder.txtIncomingValue.text = getValueDecimalAllowedCheck(
                        (filterProducts!!.get(currentPosition).priceList!!.get(0).unitSize)!!,
                        filterProducts!!.get(currentPosition).priceList!!.get(0).incomings!!.get(0).incomingQty
                    )
                }
                if (filterProducts!![currentPosition].priceList!![0].parLevel!! > (onHand)!!) {
                    holder.txtOnHandTitle.setTextColor(context.resources.getColor(R.color.pinky_red))
                    holder.txtOnHandValue.setTextColor(context.resources.getColor(R.color.pinky_red))
                    holder.txtPlusSymbol.setTextColor(context.resources.getColor(R.color.pinky_red))
                    holder.txtIncomingExp.setTextColor(context.resources.getColor(R.color.pinky_red))
                    holder.txtIncomingValue.setTextColor(context.resources.getColor(R.color.pinky_red))
                } else {
                    holder.txtOnHandTitle.setTextColor(context.resources.getColor(R.color.dark_grey))
                    holder.txtOnHandValue.setTextColor(context.resources.getColor(R.color.black))
                    holder.txtPlusSymbol.setTextColor(context.resources.getColor(R.color.dark_grey))
                    holder.txtIncomingExp.setTextColor(context.resources.getColor(R.color.dark_grey))
                    holder.txtIncomingValue.setTextColor(context.resources.getColor(R.color.black))
                }
            } else {
                holder.txtOnHandTitle.visibility = View.GONE
                holder.txtOnHandValue.visibility = View.GONE
                holder.txtPlusSymbol.visibility = View.GONE
                holder.txtIncomingExp.visibility = View.GONE
                holder.txtIncomingValue.visibility = View.GONE
                holder.txtProductUnitPrice.visibility = View.VISIBLE
                holder.txtProductUnitPrice.text = context.getString(R.string.txt_no_inventory_data)
                setTypefaceView(
                    holder.txtProductUnitPrice,
                    ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
                )
                holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.dark_grey))
            }
            holder.txtProductStrikePrice.visibility = View.GONE
        } else {
            holder.imgFavouriteProduct.visibility = View.VISIBLE
            if (viewPrice) {
                val itemPrice =
                    filterProducts!![currentPosition].priceList!![0].price!!.getDisplayValueWithUom(
                        returnShortNameValueUnitSize(
                            filterProducts!![currentPosition].priceList!![0].unitSize
                        )
                    )
                holder.txtOnHandTitle.visibility = View.GONE
                holder.txtOnHandValue.visibility = View.GONE
                holder.txtPlusSymbol.visibility = View.GONE
                holder.txtIncomingExp.visibility = View.GONE
                holder.txtIncomingValue.visibility = View.GONE
                if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                    holder.txtProductUnitPrice.visibility = View.VISIBLE
                    holder.txtProductUnitPrice.text = itemPrice
                } else {
                    holder.txtProductUnitPrice.visibility = View.GONE
                }
            } else {
                holder.txtProductUnitPrice.visibility = View.GONE
                if (filterProducts!![currentPosition].priceList!![0].onHandQty != null) {
                    holder.txtOnHandTitle.visibility = View.VISIBLE
                    holder.txtOnHandTitle.text = context.getString(R.string.txt_on_hand)
                    holder.txtOnHandValue.visibility = View.VISIBLE
                    holder.txtOnHandValue.text =
                        getValueDecimal(filterProducts!!.get(currentPosition).priceList!!.get(0).onHandQty) + " " + returnShortNameValueUnitSize(
                            filterProducts!!.get(currentPosition).priceList!!.get(0).unitSize
                        )
                    var onHand = filterProducts!![currentPosition].priceList!![0].onHandQty
                    if (filterProducts!![currentPosition].priceList!![0].incomings != null && filterProducts!![currentPosition].priceList!![0].incomings!!.size > 0) {
                        holder.txtPlusSymbol.visibility = View.VISIBLE
                        holder.txtPlusSymbol.text = " + "
                        holder.txtIncomingExp.visibility = View.VISIBLE
                        holder.txtIncomingExp.text = context.getString(R.string.txt_exp)
                        holder.txtIncomingValue.visibility = View.VISIBLE
                        onHand =
                            onHand!! + (filterProducts!![currentPosition].priceList!![0].incomings!![0].incomingQty)!!
                        holder.txtIncomingValue.text = getValueDecimalAllowedCheck(
                            (filterProducts!!.get(currentPosition).priceList!!.get(0).unitSize)!!,
                            filterProducts!!.get(currentPosition).priceList!!.get(0).incomings!!.get(0).incomingQty
                        )
                    }
                    if (filterProducts!![currentPosition].priceList!![0].parLevel != null && (filterProducts!![currentPosition].priceList!![0].parLevel!! > (onHand)!!)) {
                        holder.txtOnHandTitle.setTextColor(context.resources.getColor(R.color.pinky_red))
                        holder.txtOnHandValue.setTextColor(context.resources.getColor(R.color.pinky_red))
                        holder.txtPlusSymbol.setTextColor(context.resources.getColor(R.color.pinky_red))
                        holder.txtIncomingExp.setTextColor(context.resources.getColor(R.color.pinky_red))
                        holder.txtIncomingValue.setTextColor(context.resources.getColor(R.color.pinky_red))
                    } else {
                        holder.txtOnHandTitle.setTextColor(context.resources.getColor(R.color.dark_grey))
                        holder.txtOnHandValue.setTextColor(context.resources.getColor(R.color.black))
                        holder.txtPlusSymbol.setTextColor(context.resources.getColor(R.color.dark_grey))
                        holder.txtIncomingExp.setTextColor(context.resources.getColor(R.color.dark_grey))
                        holder.txtIncomingValue.setTextColor(context.resources.getColor(R.color.black))
                    }
                } else {
                    holder.txtOnHandTitle.visibility = View.GONE
                    holder.txtOnHandValue.visibility = View.GONE
                    holder.txtPlusSymbol.visibility = View.GONE
                    holder.txtIncomingExp.visibility = View.GONE
                    holder.txtIncomingValue.visibility = View.GONE
                    holder.txtProductUnitPrice.visibility = View.VISIBLE
                    holder.txtProductUnitPrice.text =
                        context.getString(R.string.txt_no_inventory_data)
                    setTypefaceView(
                        holder.txtProductUnitPrice,
                        ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
                    )
                    holder.txtProductUnitPrice.setTextColor(context.resources.getColor(R.color.dark_grey))
                }
            }
            holder.txtProductStrikePrice.visibility = View.GONE
        }
        if (filterProducts!![currentPosition].images != null && filterProducts!![currentPosition].images!!.size > 0) {
            val imagesArray: List<ImagesModel?>? = filterProducts!![currentPosition].images
            var imageUrl: String? = null
            for (i in imagesArray!!.indices) {
                if (!StringHelper.isStringNullOrEmpty(imageUrl)) {
                    break
                }
                if (imagesArray[i] != null) {
                    if ((calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST) || (calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST)) {
                        if (imagesArray[i]!!.imageURL != null) imageUrl = imagesArray[i]!!.imageURL
                    } else {
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
            }
            if (StringHelper.isStringNullOrEmpty(imageUrl)) {
                holder.imgProductImage.visibility = View.GONE
            } else {
                holder.imgProductImage.visibility = View.VISIBLE
                Picasso.get().load(imageUrl).fit().into(holder.imgProductImage)
            }
        } else {
            holder.imgProductImage.visibility = View.GONE
        }
        holder.imgProductImage.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                callProductDetail(currentPosition)
            }
        })
        val isItemAvailable = getIsItemUnAvailable(filterProducts!![currentPosition])
        if (isDisplayStockInfo) {
            if (isItemAvailable) {
                holder.lytProductName.alpha = 1.0f
                holder.lytProductItems.isClickable = true
                if (filterProducts!![currentPosition].isSelected) {
                    setSelectedProductLayout(holder, currentPosition)
                } else {
                    //show add to order text
                    displayAddToOrderLayout(holder, currentPosition)
                }
            } else {
                holder.lytProductQuantity.visibility = View.GONE
                holder.lytAddToOrderAndUOM.visibility = View.VISIBLE
                holder.lytProductName.alpha = ALPHA_ROW
                holder.txtProductUnitPrice.alpha = ALPHA_ROW
                holder.txtAddToOrder.isClickable = false
                holder.txtAddToOrder.setText(R.string.txt_currently_unavailable)
                holder.txtAddToOrder.setTextColor(context.resources.getColor(R.color.grey_medium))
            }
        } else {
            if (filterProducts!![currentPosition].isSelected) {
                //show the quantity layout with pre set quantity
                setSelectedProductLayout(holder, currentPosition)
            } else {
                displayAddToOrderLayout(holder, currentPosition)
            }
        }
        //        Log.d("SearchedString",searchStringArray.length+"*******");
        val productCodeTag = getProductCodeAndTag(
            filterProducts!![currentPosition].supplierProductCode,
            filterProducts!![currentPosition].customProductCode,
            filterProducts!![currentPosition].tags,
            filterProducts!![currentPosition].supplierName
        )
        if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
            holder.txtProductCodeTag.visibility = View.GONE
        }
        if (searchStringArray != null && searchStringArray!!.size > 0) {
            val itemName = filterProducts!![currentPosition].productName + ""
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
            val itemCustomName = filterProducts!![currentPosition].productCustomName + ""
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
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                )!! && filterProducts!![currentPosition].productCustomName != null
            ) {
                holder.txtProductCustomName.visibility = View.VISIBLE
                holder.txtItemName.text = sbCustomName
                holder.txtProductCustomName.text = sb
            } else {
                holder.txtItemName.text = sb
                holder.txtProductCustomName.visibility = View.GONE
            }
            var productCode = productCodeTag
            if (StringHelper.isStringNullOrEmpty(productCode)) {
                productCode = ""
            }
            val sbTag = SpannableStringBuilder(productCode)
            val pTag = Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
            val mTag = pTag.matcher(productCode)
            while (mTag.find()) {
                val color = context.resources.getColor(R.color.text_blue)
                sbTag.setSpan(
                    ForegroundColorSpan(color),
                    mTag.start(),
                    mTag.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            holder.txtProductCodeTag.text = sbTag
        } else {
            if (SharedPref.readBool(
                    SharedPref.USER_INVENTORY_SETTING_STATUS,
                    false
                )!! && filterProducts!![currentPosition].productCustomName != null
            ) {
                holder.txtProductCustomName.text = filterProducts!!.get(currentPosition).productName
                holder.txtProductCustomName.visibility = View.VISIBLE
                holder.txtItemName.text = filterProducts!!.get(currentPosition).productCustomName
            } else {
                holder.txtItemName.text = filterProducts!!.get(currentPosition).productName
                holder.txtProductCustomName.visibility = View.GONE
            }
            holder.txtProductCodeTag.text = productCodeTag
        }
        holder.lytProductName.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                callProductDetail(currentPosition)
            }
        })
        if (filterProducts!![currentPosition].isFavourite) {
            holder.imgFavouriteProduct.setImageResource(R.drawable.favourite_blue)
        } else {
            holder.imgFavouriteProduct.setImageResource(R.drawable.favourite_grey)
        }
        holder.imgFavouriteProduct.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (filterProducts!![currentPosition].isFavourite) {
                    filterProducts!![currentPosition].isFavourite = false
                    if ((calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST)) {
                        removeEssentialsProductsAsFavourite(
                            context,
                            filterProducts!![currentPosition].essentialsProductId
                        )
                    } else {
                        updateFavSkuStatus(context, filterProducts!![currentPosition].sku, false)
                    }
                    holder.imgFavouriteProduct.setImageResource(R.drawable.favourite_grey)
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_ADD_TO_ORDER_FAVOURITE,
                        filterProducts!![currentPosition]
                    )
                } else {
                    filterProducts!![currentPosition].isFavourite = true
                    if ((calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST)) {
                        markEssentialsProductsAsFavourite(
                            context,
                            filterProducts!![currentPosition].essentialsProductId
                        )
                    } else {
                        updateFavSkuStatus(context, filterProducts!![currentPosition].sku, true)
                    }
                    holder.imgFavouriteProduct.setImageResource(R.drawable.favourite_blue)
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_ADD_TO_ORDER_FAVOURITE,
                        filterProducts!![currentPosition]
                    )
                }
            }
        })

        holder.txtProductQuantity.setOnClickListener {
            Log.d("Map", "${selectedProductsMap}******")
            if (dialog == null || (dialog != null && !dialog!!.isShowing)) {
                val key = filterProducts!![position].priceList?.get(0)?.let { it1 ->
                    it1.unitSize?.let { it2 ->
                        ProductDataHelper.getKeyProductMap(
                            filterProducts!![position].sku,
                            it2
                        )
                    }
                }
                setSupplierDetails(position)
                val createOrderHelperDialog = supplierDetails?.let { it1 ->
                    ProductQuantityChangeDialogHelper(
                        selectedProductsMap?.get(key), context, it1, filterProducts!![position],
                        this@SupplierProductListAdapter )
                }
                if (createOrderHelperDialog != null) {
                    dialog = createOrderHelperDialog.createChangeQuantityDialog()
                }
                dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            } }

//        holder.txtProductQuantity.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View) {
//                Log.d("Map", selectedProductsMap.toString() + "******")
//                if (dialog?.isShowing == true) {
//                    val key = getKeyProductMap(
//                        filterProducts!![currentPosition].sku,
//                        (filterProducts!![currentPosition].priceList!![0].unitSize)!!
//                    )
//                    setSupplierDetails(currentPosition)
//                    val createOrderHelperDialog = supplierDetails?.let {
//                        ProductQuantityChangeDialogHelper(
//                            selectedProductsMap!![key],
//                            context,
//                            it,
//                            filterProducts!![currentPosition],
//                            this@SupplierProductListAdapter
//                        )
//                    }
//                    dialog = createOrderHelperDialog?.createChangeQuantityDialog()
//                    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
//                }
//            }
//        })
        holder.btnIncreaseQuantity.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val key = getKeyProductMap(
                    filterProducts!![currentPosition].sku,
                    (filterProducts!![currentPosition].priceList!![0].unitSize)!!
                )
                var product = selectedProductsMap!![key]
                var quantity = product!!.quantity
                if (quantity == 0.0) {
                    selectedProductsListener.onProductSelected(filterProducts!![currentPosition])
                    selectedProductsListener.onProductSelectedForRecentSearch(filterProducts!![currentPosition].productName)
                }
                quantity = quantity + 1
                product.quantity = (quantity)
                product = calculateTotalPriceChanged(product)
                holder.txtProductQuantity.text = getValueDecimalAllowedCheck(
                    product!!.unitSize, product.quantity
                ) + " "
                holder.txtProductQuantity.append(
                    changeQuantityUOMTextFontSize(
                        returnShortNameValueUnitSizeForQuantity(
                            product.unitSize
                        ), context
                    )
                )
            }
        })
        holder.btnReduceQuantity.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val key = getKeyProductMap(
                    filterProducts!![currentPosition].sku,
                    (filterProducts!![currentPosition].priceList!![0].unitSize)!!
                )
                var product = selectedProductsMap!![key]
                var quantity = product!!.quantity
                quantity = quantity - 1
                product!!.quantity = (quantity)
                product = calculateTotalPriceChanged(product)
                holder.txtProductQuantity.text = getValueDecimalAllowedCheck(
                    product!!.unitSize, product!!.quantity
                ) + " "
                holder.txtProductQuantity.append(
                    changeQuantityUOMTextFontSize(
                        returnShortNameValueUnitSizeForQuantity(
                            product!!.unitSize
                        ), context
                    )
                )
                if (quantity <= 0) {
                    filterProducts!![currentPosition].isSelected = false
                    selectedProductsListener.onProductDeselected(filterProducts!![currentPosition])
                    //reset all the price list uom
                    for (i in filterProducts!![currentPosition].priceList!!.indices) {
                        filterProducts!![currentPosition].priceList!![i].selected = false
                    }
                    selectedProductsMap!!.remove(key)
                    displayAddToOrderLayout(holder, currentPosition)
                }
            }
        })
        if (currentPosition == filterProducts!!.size - 1) {
            holder.lytMoreThanDays.visibility = View.VISIBLE
            holder.lytEmptySpace.visibility = View.VISIBLE
        } else {
            holder.lytEmptySpace.visibility = View.GONE
            holder.lytMoreThanDays.visibility = View.GONE
        }
        holder.lytMoreThanDays.isClickable = false
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {}
        }, 2000)
    }

    fun callProductDetail(position: Int) {
        if (!StringHelper.isStringNullOrEmpty(outletId)) {
            AnalyticsHelper.logAction(
                context,
                AnalyticsHelper.TAP_ITEM_ADD_TO_ORDER_PRODUCT_DETAILS
            )
            val newIntent = Intent(context, ProductDetailActivity::class.java)
            val productDetails = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                filterProducts!![position], ProductDetailBySupplier::class.java
            )
            newIntent.putExtra(ZeemartAppConstants.PRODUCT_DETAILS_JSON, productDetails)
            setSupplierDetails(position)
            val supplierDetailsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplierDetails)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetailsJson)
            newIntent.putExtra(ZeemartAppConstants.PRODUCT_SKU, filterProducts!![position].sku)
            newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outletId)
            val selectedProductJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                filterProducts!![position]
            )
            newIntent.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, selectedProductJson)
            newIntent.putExtra(
                ZeemartAppConstants.SELECTED_MAPPING,
                ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProductsMap)
            )
            if ((calledFrom == ZeemartAppConstants.CALLED_FROM_FAVOURITE_PRODUCT_LIST) || (calledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST)) {
                newIntent.putExtra(
                    ZeemartAppConstants.SUPPLIER_NAME,
                    filterProducts!![position].supplierName
                )
                //                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_LOGO, filterProducts.get(position).getSupplierName());
            } else {
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_NAME, supplierName)
                newIntent.putExtra(ZeemartAppConstants.SUPPLIER_LOGO, supplierLogo)
            }
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_ID, supplierId)
            newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, calledFrom)
            (context as Activity).startActivityForResult(
                newIntent,
                ZeemartAppConstants.RequestCode.ProductDetailsActivity
            )
        }
    }

    fun onActivityResult(selectedProductsMap: MutableMap<String, Product>?) {
        this.selectedProductsMap = selectedProductsMap
        notifyDataSetChanged()
    }

    fun displayAddToOrderLayout(holder: ViewHolder?, position: Int) {
        holder!!.lytProductQuantity.visibility = View.GONE
        if (isFirstTimeHighLighted && (selectedSearchedProductDetails != null) && (selectedSearchedProductDetails.sku == filterProducts!![position].sku) && !StringHelper.isStringNullOrEmpty(
                filterProducts!![position].priceList!![0].unitSize
            ) && (selectedSearchedProductDetails.priceList!![0].unitSize == filterProducts!![position].priceList!![0].unitSize)
        ) {
            val colorFade = ObjectAnimator.ofObject(
                holder.lytProductName,
                "backgroundColor" /*view attribute name*/,
                ArgbEvaluator(),
                context.resources.getColor(
                    R.color.notif_yellow
                ) /*from color*/,
                context.resources.getColor(R.color.white) /*to color*/
            )
            colorFade.duration = 2000
            colorFade.startDelay = 200
            colorFade.start()
            val colorFade1 = ObjectAnimator.ofObject(
                holder.lytProductItems,
                "backgroundColor" /*view attribute name*/,
                ArgbEvaluator(),
                context.resources.getColor(
                    R.color.notif_yellow
                ) /*from color*/,
                context.resources.getColor(R.color.white) /*to color*/
            )
            colorFade1.duration = 2000
            colorFade1.startDelay = 200
            colorFade1.start()
            val colorFade2 = ObjectAnimator.ofObject(
                holder.lytQuantitySelection,
                "backgroundColor" /*view attribute name*/,
                ArgbEvaluator(),
                context.resources.getColor(
                    R.color.notif_yellow
                ) /*from color*/,
                context.resources.getColor(R.color.white) /*to color*/
            )
            colorFade2.duration = 2000
            colorFade2.startDelay = 200
            colorFade2.start()
            isFirstTimeHighLighted = false
        } else {
            holder.lytQuantitySelection.setBackgroundColor(context.resources.getColor(R.color.faint_white))
            holder.lytProductItems.setBackgroundColor(context.resources.getColor(R.color.white))
        }
        holder.lytAddToOrderAndUOM.visibility = View.VISIBLE
        if ((calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST) || (calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST)) {
            if (isBlockToOrder) {
                holder.txtAddToOrder.text = context.resources.getString(R.string.txt_signup_login)
            } else {
                holder.txtAddToOrder.text = context.getString(R.string.txt_order)
            }
        } else {
            if ((calledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST)) {
                holder.txtAddToOrder.text = context.getString(R.string.txt_fill_to_par)
            } else {
                holder.txtAddToOrder.text = context.getString(R.string.txt_add_to_order)
            }
        }
        holder.txtAddToOrder.setTextColor(context.resources.getColor(R.color.text_blue))
        holder.txtItemUom.text =
            returnShortNameValueUnitSize(filterProducts!!.get(position).priceList!!.get(0).unitSize)
    }

    fun setSelectedProductLayout(holder: ViewHolder?, position: Int) {
        holder!!.lytQuantitySelection.setBackgroundColor(context.resources.getColor(R.color.white))
        //show the quantity layout with pre set quantity
        holder.lytProductQuantity.visibility = View.VISIBLE
        holder.lytAddToOrderAndUOM.visibility = View.GONE
        if (selectedProductsMap != null && selectedProductsMap!!.size > 0) {
            val key = getKeyProductMap(
                filterProducts!![position].sku,
                (filterProducts!![position].priceList!![0].unitSize)!!
            )
            val selectedProduct = selectedProductsMap!![key]
            holder.txtProductQuantity.text = selectedProduct!!.quantityDisplayValue + " "
            holder.txtProductQuantity.append(
                changeQuantityUOMTextFontSize(
                    returnShortNameValueUnitSizeForQuantity(
                        selectedProduct.unitSize
                    ), context
                )
            )
        }
    }

    private fun getProductCodeAndTag(
        productCode: String?,
        customProductCode: String?,
        tags: List<String>?,
        supplierName: String?
    ): String? {
        var productCodeTag: String? = ""
        if ((SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            )!! && !StringHelper.isStringNullOrEmpty(customProductCode))
        ) {
            productCodeTag = customProductCode
        } else {
            productCodeTag = productCode
        }
        //        if (!StringHelper.isStringNullOrEmpty(productCode)) {
//            productCodeTag = productCode;
//        }
        if ((calledFrom == ZeemartAppConstants.CALLED_FROM_FAVOURITE_PRODUCT_LIST) || (calledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST)) {
            if (!StringHelper.isStringNullOrEmpty(supplierName)) if (StringHelper.isStringNullOrEmpty(
                    productCodeTag
                )
            ) {
                productCodeTag = supplierName
            } else {
                productCodeTag = "$productCodeTag • $supplierName"
            }
        }
        if ((calledFrom == ZeemartAppConstants.CALLED_FROM_SUPPLIER_PRODUCT_LIST) || (calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST) || (calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST)) {
            if (tags != null && tags.size > 0) {
                var tagValue = ""
                for (i in tags.indices) {
                    tagValue = tagValue + tags[i] + ", "
                }
                if (tagValue.length > 0) {
                    val attributeValuesAll = tagValue.substring(0, tagValue.length - 2)
                    if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                        productCodeTag = attributeValuesAll
                    } else {
                        productCodeTag = "$productCodeTag • $attributeValuesAll"
                    }
                }
            }
        }
        return productCodeTag
    }

    override fun getItemCount(): Int {
        return if (filterProducts == null) 0 else filterProducts!!.size
    }

    override fun getFilter(): Filter {
        return FilterProducts()
    }

    override fun onDataChange(product: Product?) {
        notifyDataSetChanged()
    }

    fun viewPriceCalled(isPrice: Boolean) {
        viewPrice = isPrice
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        var lytEmptySpace: LinearLayout
        var txtProductStrikePrice: TextView
        var lytMoreThanDays: RelativeLayout
        var txtMoreThanDays: TextView
        var txtOnHandTitle: TextView
        var txtOnHandValue: TextView
        var txtPlusSymbol: TextView
        var txtIncomingValue: TextView
        var txtIncomingExp: TextView

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
            txtProductStrikePrice = itemView.findViewById(R.id.txt_product_strike_price)
            txtProductCustomName = itemView.findViewById(R.id.txt_product_changed_name)
            txtItemUom = itemView.findViewById(R.id.txt_uom_name)
            lytAddToOrderAndUOM = itemView.findViewById(R.id.lyt_add_to_order_and_uom)
            imgProductImage = itemView.findViewById(R.id.img_product_image)
            imgProductImage.visibility = View.GONE
            lytEmptySpace = itemView.findViewById(R.id.lyt_empty_space)
            lytEmptySpace.visibility = View.GONE
            lytMoreThanDays = itemView.findViewById(R.id.lyt_more_than)
            txtMoreThanDays = itemView.findViewById(R.id.txt_more_than)
            txtOnHandTitle = itemView.findViewById(R.id.txt_on_hand_title)
            txtOnHandValue = itemView.findViewById(R.id.txt_on_hand_value)
            txtPlusSymbol = itemView.findViewById(R.id.txt_plus_symbol)
            txtIncomingValue = itemView.findViewById(R.id.txt_incoming_value)
            txtIncomingExp = itemView.findViewById(R.id.txt_incoming_exp)
            txtOnHandTitle.visibility = View.GONE
            txtOnHandValue.visibility = View.GONE
            txtPlusSymbol.visibility = View.GONE
            txtIncomingValue.visibility = View.GONE
            txtIncomingExp.visibility = View.GONE
            setTypefaceView(txtMoreThanDays, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
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
            setTypefaceView(
                txtProductStrikePrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            setTypefaceView(txtOnHandTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtOnHandValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(
                txtIncomingValue,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtPlusSymbol, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtIncomingExp, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    private inner class FilterProducts() : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null && constraint.length > 0) {
                val searchedString = constraint as String
                searchStringArray =
                    searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val tempList: MutableList<ProductDetailBySupplier> = ArrayList()

                // search content in friend list
                Log.d("key", constraint.toString())
                for (i in products.indices) {
                    val productCodeTag = getProductCodeAndTag(
                        products[i].supplierProductCode,
                        products[i].customProductCode,
                        products[i].tags,
                        products[i].supplierName
                    )
                    var searchStringFound = false
                    for (j in searchStringArray!!.indices) {
                        if (products[i].productName!!.lowercase(Locale.getDefault()).contains(
                                searchStringArray!![j].lowercase(Locale.getDefault())
                            ) || productCodeTag!!.lowercase(
                                Locale.getDefault()
                            ).contains(
                                searchStringArray!![j].lowercase(Locale.getDefault())
                            ) || products[i].productCustomName!!
                                .lowercase(Locale.getDefault()).contains(
                                    searchStringArray!![j].lowercase(
                                        Locale.getDefault()
                                    )
                                )
                        ) {
                            searchStringFound = true
                            continue
                        } else {
                            searchStringFound = false
                            break
                        }
                    }
                    if (searchStringFound) {
                        tempList.add(products[i])
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
            } else {
                searchStringArray = null
                filterResults.count = products.size
                filterResults.values = products
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null) {
                filterProducts = results.values as List<ProductDetailBySupplier>
                notifyDataSetChanged()
            }
        }
    }

    private fun getIsItemUnAvailable(product: ProductDetailBySupplier): Boolean {
        var isItemAvailable = false
        val productPriceList: List<ProductPriceList>? = product.priceList
        if (productPriceList != null && productPriceList.size > 0) {
            for (i in productPriceList.indices) {
                if (productPriceList[i].isStatus(ProductPriceList.UnitSizeStatus.VISIBLE)) {
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

    private fun createUnitSizeStockMap(product: ProductDetailBySupplier): Map<String?, StocksList> {
        val unitSizeStockMap: MutableMap<String?, StocksList> = HashMap()
        if (product.stocks != null && product.stocks!!.size > 0) {
            for (i in product.stocks!!.indices) {
                unitSizeStockMap[product.stocks!!.get(i).unitSize] = product.stocks!!.get(i)
            }
        }
        return unitSizeStockMap
    }

//    private fun setSupplierDetails(position: Int) {
//        if (supplierDetails == null) {
//            supplierDetails = DetailSupplierDataModel()
//            supplierDetails?.supplier = (filterProducts!![position].supplier!!)
//        }
//    }
private fun setSupplierDetails(position: Int) {
    if (supplierDetails == null && filterProducts != null && filterProducts!!.size > position && filterProducts!![position].supplier != null) {
        supplierDetails = DetailSupplierDataModel()
        supplierDetails?.supplier = filterProducts!![position].supplier!!
    }
}

    interface SelectedProductsListener {
        fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?)
        fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?)
        fun onProductSelectedForRecentSearch(productName: String?)
    }

    companion object {
        private val ALPHA_ROW = 0.5f
    }
}