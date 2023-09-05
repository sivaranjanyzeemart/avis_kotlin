package zeemart.asia.buyers.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.order.SearchForNewOrderMgr
import zeemart.asia.buyers.modelsimport.SearchEssentialAndDealsModel
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsSuppliers
import zeemart.asia.buyers.orders.ProductDetailActivity
import zeemart.asia.buyers.selfOnBoarding.SelfOnBoardingSearchedCategoriesActivity
import java.util.*
import java.util.regex.Pattern

/**
 * Created by RajPrudhviMarella on 26/Apr/2021.
 */
class SelfOnBoardingSearchEssentialsAndDealsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>,
    Filterable {
    var lstSearchDataArrayList: ArrayList<SearchForNewOrderMgr>
    var context: Context
    var mListener: OnItemClicked
    private lateinit var searchStringArray: Array<String>
    private var products: MutableList<ProductDetailBySupplier?> = ArrayList()
    private var supplierName: String? = null
    private var mFilter: FilterProducts
    private var supplierDetails: DetailSupplierDataModel?
    private var searchedString = ""
    private var supplierList: MutableList<DetailSupplierDataModel?> = ArrayList()
    private var categoryList: MutableList<SearchEssentialAndDealsModel.Category?> = ArrayList()
    private var viewAllClickListener: onViewAllClickListener? = null

    constructor(
        context: Context,
        lstSearchDataArrayList: ArrayList<SearchForNewOrderMgr>,
        supplierDetails: DetailSupplierDataModel?,
        mListener: OnItemClicked
    ) {
        this.lstSearchDataArrayList = lstSearchDataArrayList
        this.context = context
        this.mListener = mListener
        this.supplierDetails = supplierDetails
        mFilter = FilterProducts()
    }

    constructor(
        context: Context,
        lstSearchDataArrayList: ArrayList<SearchForNewOrderMgr>,
        supplierDetails: DetailSupplierDataModel?,
        mListener: OnItemClicked,
        viewAllClickListener: onViewAllClickListener?
    ) {
        this.lstSearchDataArrayList = lstSearchDataArrayList
        this.context = context
        this.mListener = mListener
        this.supplierDetails = supplierDetails
        this.viewAllClickListener = viewAllClickListener
        mFilter = FilterProducts()
    }

    override fun getItemViewType(position: Int): Int {
        val data = lstSearchDataArrayList[position]
        if (data.isHeader) {
            return VIEW_TYPE_HEADER
        } else if (data.suppliersList != null) {
            return SUPPLIER_VIEW_TYPE
        } else if (data.productDetailBySupplier != null) {
            return PRODUCT_VIEW_TYPE
        } else if (data.categoriesList != null) {
            return CATEGORY_VIEW_TYPE
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
                .inflate(R.layout.lyt_supplier_list_essentials, parent, false)
            return ViewHolderSupplier(itemView)
        } else if (viewType == PRODUCT_VIEW_TYPE) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_product_list_row, parent, false)
            return ViewHolderProducts(itemView)
        } else if (viewType == VIEW_ALL_VIEW_TYPE) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.lyt_view_all_button, parent, false)
            return ViewHolderViewAll(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_catagory_list_row, parent, false)
            return ViewHolderCategory(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        if (lstSearchDataArrayList[currentPosition].isHeader) {
            val viewHolderHeader = holder as ViewHolderHeader
            val headerWithCount =
                lstSearchDataArrayList[currentPosition].headerCount.toString() + " " + lstSearchDataArrayList[currentPosition].header
            viewHolderHeader.txtHeader.text = headerWithCount
        } else if (lstSearchDataArrayList[currentPosition].isViewAll) {
            val viewHolderViewAll = holder as ViewHolderViewAll
            viewHolderViewAll.btnViewAll.setOnClickListener(View.OnClickListener {
                if ((lstSearchDataArrayList[currentPosition].viewAllFor == ZeemartAppConstants.VIEW_ALL_FOR_SUPPLIERS)) {
                    viewAllClickListener!!.onViewAllSupplierClicked()
                } else {
                    viewAllClickListener!!.onViewAllCategoryClicked()
                }
            })
        } else if (lstSearchDataArrayList[currentPosition].categoriesList != null) {
            categoryList.add(lstSearchDataArrayList[currentPosition].categoriesList)
            val holderCategory = holder as ViewHolderCategory
            if ((searchedString == "") && lstSearchDataArrayList[currentPosition].categoriesList!!.name != null) {
                holderCategory.txtCategoryName.text =
                    lstSearchDataArrayList.get(currentPosition).categoriesList!!.name
            } else {
                val itemName = lstSearchDataArrayList[currentPosition].categoriesList!!.name + ""
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
                holderCategory.txtCategoryName.text = sb
            }
            holderCategory.lytCategory.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    mListener.onCategoryClicked(lstSearchDataArrayList[currentPosition].categoriesList!!.name)
                    val intent =
                        Intent(context, SelfOnBoardingSearchedCategoriesActivity::class.java)
                    intent.putExtra(
                        ZeemartAppConstants.CATEGORY_LIST,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            lstSearchDataArrayList[currentPosition].categoriesList
                        )
                    )
                    context.startActivity(intent)
                }
            })
        } else if (lstSearchDataArrayList[currentPosition].suppliersList != null) {
            supplierList.add(lstSearchDataArrayList[currentPosition].detailSupplierDataModel)
            val holderSupplier = holder as ViewHolderSupplier
            if ((searchedString == "") && lstSearchDataArrayList[currentPosition].suppliersList!!.supplier!!.supplierName != null) {
                holderSupplier.txtSupplierName.text =
                    lstSearchDataArrayList.get(currentPosition).suppliersList!!.supplier!!.supplierName
            } else {
                val itemName =
                    lstSearchDataArrayList[currentPosition].suppliersList!!.supplier!!.supplierName + ""
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
                holderSupplier.txtSupplierName.text = sb
            }
            holderSupplier.lytSupplierList.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    mListener.onEssentialOrDealSupplierClicked(lstSearchDataArrayList[currentPosition].suppliersList)
                }
            })
            if (StringHelper.isStringNullOrEmpty(lstSearchDataArrayList[currentPosition].suppliersList!!.supplier!!.logoURL)) {
                holderSupplier.imgSupplier.setImageResource(R.drawable.placeholder_all)
            } else {
                Picasso.get()
                    .load(lstSearchDataArrayList[currentPosition].suppliersList!!.supplier!!.logoURL)
                    .placeholder(
                        R.drawable.placeholder_all
                    ).resize(
                    ViewHolderSupplier.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                    ViewHolderSupplier.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                ).into(holderSupplier.imgSupplier)
            }
            if (lstSearchDataArrayList[currentPosition].suppliersList!!.rebatePercentage != null) {
                holderSupplier.imgRebate.visibility = View.VISIBLE
                holderSupplier.txtRebateAmount.visibility = View.VISIBLE
                holderSupplier.txtRebateAmount.text =
                    lstSearchDataArrayList.get(currentPosition).suppliersList!!.rebatePercentage.toString() + "%"
                if (lstSearchDataArrayList[currentPosition].suppliersList!!.shortDesc != null) {
                    holderSupplier.txtSupplierDescription.visibility = View.VISIBLE
                    val supplierDescription =
                        " • " + lstSearchDataArrayList[currentPosition].suppliersList!!.shortDesc
                    holderSupplier.txtSupplierDescription.text = supplierDescription
                } else {
                    holderSupplier.txtSupplierDescription.visibility = View.GONE
                }
            } else {
                holderSupplier.imgRebate.visibility = View.GONE
                holderSupplier.txtRebateAmount.visibility = View.GONE
                if (lstSearchDataArrayList[currentPosition].suppliersList!!.shortDesc != null) {
                    holderSupplier.txtSupplierDescription.visibility = View.VISIBLE
                    holderSupplier.txtSupplierDescription.text =
                        lstSearchDataArrayList.get(currentPosition).suppliersList!!.shortDesc
                } else {
                    holderSupplier.txtSupplierDescription.visibility = View.GONE
                }
            }
        } else if (lstSearchDataArrayList[currentPosition].productDetailBySupplier != null) {
            products.add(lstSearchDataArrayList[currentPosition].productDetailBySupplier)
            val viewHolderProducts = holder as ViewHolderProducts
            viewHolderProducts.txtAddToOrder.text =
                context.resources.getString(R.string.txt_view_catalogue)
            if (!StringHelper.isStringNullOrEmpty(lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.dealNumber)) {
                viewHolderProducts.imgFavouriteProduct.visibility = View.GONE
                if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].dealPrice != null) {
                    val itemPrice =
                        lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].dealPrice!!.getDisplayValueWithUom(
                            returnShortNameValueUnitSize(
                                lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize
                            )
                        )
                    if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                        viewHolderProducts.txtProductUnitPrice.visibility = View.VISIBLE
                        if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.amount != 0.0) {
                            viewHolderProducts.txtProductUnitPrice.setTextColor(
                                context.resources.getColor(
                                    R.color.pinky_red
                                )
                            )
                        } else {
                            viewHolderProducts.txtProductUnitPrice.setTextColor(
                                context.resources.getColor(
                                    R.color.grey_medium
                                )
                            )
                        }
                        viewHolderProducts.txtProductUnitPrice.text = itemPrice
                    } else {
                        viewHolderProducts.txtProductUnitPrice.visibility = View.GONE
                    }
                    if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.amount != 0.0) {
                        val string = SpannableString(
                            lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.getDisplayValueWithUom(
                                returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize
                                )
                            )
                        )
                        string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                        viewHolderProducts.txtProductStrikePrice.visibility = View.VISIBLE
                        viewHolderProducts.txtProductStrikePrice.text = string
                    } else {
                        viewHolderProducts.txtProductStrikePrice.visibility = View.GONE
                    }
                } else {
                    if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.amount != 0.0) {
                        val itemPrice =
                            lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.getDisplayValueWithUom(
                                returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize
                                )
                            )
                        viewHolderProducts.txtProductUnitPrice.visibility = View.VISIBLE
                        viewHolderProducts.txtProductUnitPrice.setTextColor(
                            context.resources.getColor(
                                R.color.grey_medium
                            )
                        )
                        viewHolderProducts.txtProductUnitPrice.text = itemPrice
                    } else {
                        viewHolderProducts.txtProductUnitPrice.visibility = View.GONE
                    }
                    viewHolderProducts.txtProductStrikePrice.visibility = View.GONE
                }
            } else if (!StringHelper.isStringNullOrEmpty(lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.essentialsId)) {
                viewHolderProducts.imgFavouriteProduct.visibility = View.GONE
                if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].essentialPrice != null) {
                    val itemPrice =
                        lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].essentialPrice!!.getDisplayValueWithUom(
                            returnShortNameValueUnitSize(
                                lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize
                            )
                        )
                    if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                        viewHolderProducts.txtProductUnitPrice.visibility = View.VISIBLE
                        if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.amount != 0.0) {
                            viewHolderProducts.txtProductUnitPrice.setTextColor(
                                context.resources.getColor(
                                    R.color.pinky_red
                                )
                            )
                        } else {
                            viewHolderProducts.txtProductUnitPrice.setTextColor(
                                context.resources.getColor(
                                    R.color.grey_medium
                                )
                            )
                        }
                        viewHolderProducts.txtProductUnitPrice.text = itemPrice
                    } else {
                        viewHolderProducts.txtProductUnitPrice.visibility = View.GONE
                    }
                    if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.amount != 0.0) {
                        val string = SpannableString(
                            lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.getDisplayValueWithUom(
                                returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize
                                )
                            )
                        )
                        string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                        viewHolderProducts.txtProductStrikePrice.visibility = View.VISIBLE
                        viewHolderProducts.txtProductStrikePrice.text = string
                    } else {
                        viewHolderProducts.txtProductStrikePrice.visibility = View.GONE
                    }
                } else {
                    if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.amount != 0.0) {
                        val itemPrice =
                            lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].originalPrice!!.getDisplayValueWithUom(
                                returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.priceList!![0].unitSize
                                )
                            )
                        viewHolderProducts.txtProductUnitPrice.visibility = View.VISIBLE
                        viewHolderProducts.txtProductUnitPrice.setTextColor(
                            context.resources.getColor(
                                R.color.grey_medium
                            )
                        )
                        viewHolderProducts.txtProductUnitPrice.text = itemPrice
                    } else {
                        viewHolderProducts.txtProductUnitPrice.visibility = View.GONE
                    }
                    viewHolderProducts.txtProductStrikePrice.visibility = View.GONE
                }
            }
            if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.images != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.images!!.size > 0) {
                val imagesArray: List<ImagesModel?>? =
                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.images
                var imageUrl: String? = null
                for (i in imagesArray!!.indices) {
                    if (!StringHelper.isStringNullOrEmpty(imageUrl)) {
                        break
                    }
                    if (imagesArray[i] != null) {
                        if (imagesArray[i]!!.imageURL != null) imageUrl = imagesArray[i]!!.imageURL
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
            val productCodeTag = getProductCodeAndTag(
                lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.supplierProductCode,
                lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.tags,
                lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.supplierName
            )
            if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.supplier != null && lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.supplier!!.supplierName != null) {
                supplierName =
                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.supplier!!.supplierName
            }
            if (searchStringArray != null && searchStringArray!!.size > 0) {
                val itemName =
                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.productName + ""
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
                    lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.productCustomName + ""
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
                if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.productCustomName != null) {
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
                if (lstSearchDataArrayList[currentPosition].productDetailBySupplier!!.productCustomName != null) {
                    viewHolderProducts.txtProductCustomName.text =
                        lstSearchDataArrayList.get(currentPosition).productDetailBySupplier!!.productName
                    viewHolderProducts.txtProductCustomName.visibility = View.VISIBLE
                    viewHolderProducts.txtItemName.text =
                        lstSearchDataArrayList.get(currentPosition).productDetailBySupplier!!.productCustomName
                } else {
                    viewHolderProducts.txtItemName.text =
                        lstSearchDataArrayList.get(currentPosition).productDetailBySupplier!!.productName
                    viewHolderProducts.txtProductCustomName.visibility = View.GONE
                }
                if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                    viewHolderProducts.txtProductCodeTag.text = supplierName
                } else {
                    viewHolderProducts.txtProductCodeTag.text = "$productCodeTag • $supplierName"
                }
            }
            if (currentPosition == lstSearchDataArrayList.size - 1) {
                viewHolderProducts.lytEmptySpace.visibility = View.VISIBLE
            } else {
                viewHolderProducts.lytEmptySpace.visibility = View.GONE
            }
            viewHolderProducts.imgProductImage.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    callProductDetail(currentPosition)
                }
            })
            viewHolderProducts.lytProductName.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    callProductDetail(currentPosition)
                }
            })
            viewHolderProducts.lytAddToOrderAndUOM.setOnClickListener(object :
                View.OnClickListener {
                override fun onClick(v: View) {
                    mListener.onEssentialOrDealProductClicked(lstSearchDataArrayList[currentPosition].productDetailBySupplier)
                }
            })
        }
    }

    private fun getProductCodeAndTag(
        productCode: String?,
        tags: List<String>?,
        supplierName: String?
    ): String? {
        var productCodeTag: String? = ""
        if (!StringHelper.isStringNullOrEmpty(productCode)) {
            productCodeTag = productCode
        } else {
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

    inner class ViewHolderViewAll(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var btnViewAll: TextView

        init {
            btnViewAll = itemView.findViewById(R.id.btn_view_all)
            setTypefaceView(btnViewAll, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        }
    }

    fun callProductDetail(position: Int) {
        if (supplierDetails != null) {
            val newIntent = Intent(context, ProductDetailActivity::class.java)
            val productDetails = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                lstSearchDataArrayList[position].productDetailBySupplier,
                ProductDetailBySupplier::class.java
            )
            newIntent.putExtra(ZeemartAppConstants.PRODUCT_DETAILS_JSON, productDetails)
            val supplierDetail = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplierDetails)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetail)
            newIntent.putExtra(
                ZeemartAppConstants.PRODUCT_SKU,
                lstSearchDataArrayList[position].productDetailBySupplier!!.sku
            )
            var outletId: String? = ""
            if (SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")?.isEmpty() == true) {
                outletId = SharedPref.defaultOutlet?.outletId
            } else {
                outletId = SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
            }
            newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outletId)
            if (lstSearchDataArrayList[position].productDetailBySupplier!!.supplier != null) {
                newIntent.putExtra(
                    ZeemartAppConstants.SUPPLIER_NAME,
                    lstSearchDataArrayList[position].productDetailBySupplier!!.supplier!!.supplierName
                )
            }
            newIntent.putExtra(
                ZeemartAppConstants.SUPPLIER_ID,
                lstSearchDataArrayList[position].productDetailBySupplier!!.supplierId
            )
            if (!StringHelper.isStringNullOrEmpty(lstSearchDataArrayList[position].productDetailBySupplier!!.essentialsId)) {
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST
                )
            } else if (!StringHelper.isStringNullOrEmpty(lstSearchDataArrayList[position].productDetailBySupplier!!.dealNumber)) {
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_DEALS_SEARCH_LIST
                )
            }
            (context as Activity).startActivityForResult(
                newIntent,
                ZeemartAppConstants.RequestCode.ProductDetailsActivity
            )
        }
    }

    override fun getItemCount(): Int {
        return lstSearchDataArrayList.size
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

    class ViewHolderSupplier(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var txtSupplierDescription: TextView
        var txtRebateAmount: TextView
        var lytSupplierList: RelativeLayout
        var imgRebate: ImageView

        init {
            imgSupplier = itemView.findViewById(R.id.img_supplier_essentials)
            txtSupplierName = itemView.findViewById(R.id.txt_supplier_name_essentials)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtSupplierDescription = itemView.findViewById(R.id.txt_date_type_essentials)
            txtRebateAmount = itemView.findViewById(R.id.txt_rebate_amount)
            setTypefaceView(
                txtSupplierDescription,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            setTypefaceView(txtRebateAmount, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            lytSupplierList = itemView.findViewById(R.id.layout_supplier_list)
            imgRebate = itemView.findViewById(R.id.img_rebate)
        }

        companion object {
            val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH = CommonMethods.dpToPx(30)
            val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT = CommonMethods.dpToPx(30)
        }
    }

    class ViewHolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtCategoryName: TextView
        var lytCategory: RelativeLayout

        init {
            txtCategoryName = itemView.findViewById(R.id.txt_category_name)
            lytCategory = itemView.findViewById(R.id.lyt_category)
            setTypefaceView(txtCategoryName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
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
        var lytEmptySpace: LinearLayout
        var txtProductStrikePrice: TextView

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
            txtItemUom.visibility = View.GONE
            lytAddToOrderAndUOM = itemView.findViewById(R.id.lyt_add_to_order_and_uom)
            imgProductImage = itemView.findViewById(R.id.img_product_image)
            imgProductImage.visibility = View.GONE
            lytEmptySpace = itemView.findViewById(R.id.lyt_empty_space)
            lytEmptySpace.visibility = View.GONE
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
        }
    }

    override fun getFilter(): Filter {
        return mFilter
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
                val tempListSupplier = ArrayList<DetailSupplierDataModel?>()
                val tempListCategories = ArrayList<SearchEssentialAndDealsModel.Category?>()
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
                        tempListSupplier.add(supplierList[i])
                    }
                }
                for (i in categoryList.indices) {
                    if (categoryList[i]!!.name!!.lowercase(Locale.getDefault()).contains(
                            constraint.toString().lowercase(
                                Locale.getDefault()
                            )
                        )
                    ) {
                        tempListCategories.add(categoryList[i])
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
                filterResults.values = tempListSupplier
                filterResults.values = tempListCategories
            } else {
                searchStringArray != null
                filterResults.count = products.size
                filterResults.values = products
                filterResults.values = supplierList
                filterResults.values = categoryList
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null && results.count > 0) {
                products = results.values as MutableList<ProductDetailBySupplier?>
                supplierList = results.values as MutableList<DetailSupplierDataModel?>
                categoryList = results.values as MutableList<SearchEssentialAndDealsModel.Category?>
                notifyDataSetChanged()
            }
        }
    }

    interface OnItemClicked {
        fun onEssentialOrDealSupplierClicked(supplier: SearchedEssentialAndDealsSuppliers?)
        fun onEssentialOrDealProductClicked(products: ProductDetailBySupplier?)
        fun onCategoryClicked(category: String?)
    }

    interface onViewAllClickListener {
        fun onViewAllSupplierClicked()
        fun onViewAllCategoryClicked()
    }

    companion object {
        private val VIEW_TYPE_HEADER = 1
        private val SUPPLIER_VIEW_TYPE = 2
        private val PRODUCT_VIEW_TYPE = 3
        private val CATEGORY_VIEW_TYPE = 4
        private val VIEW_ALL_VIEW_TYPE = 5
    }
}