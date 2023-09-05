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
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.order.SearchForNewOrderMgr
import zeemart.asia.buyers.modelsimport.SearchEssentialAndDealsModel
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsSuppliers
import zeemart.asia.buyers.orders.ProductDetailActivity
import zeemart.asia.buyers.orders.createorders.SearchedCategoriesProductsActivity
import java.util.*
import java.util.regex.Pattern

/**
 * Created by RajPrudhviMarella on 07/08/2020.
 */
class SearchEssentialsAndDealsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>, Filterable {
    var lstSearchDataArrayList: ArrayList<SearchForNewOrderMgr>
    var context: Context
    var mListener: OnItemClicked
    private lateinit var searchStringArray: Array<String>
    private var products: MutableList<ProductDetailBySupplier> =
        ArrayList<ProductDetailBySupplier>()
    private var supplierName: String? = null
    private var mFilter: FilterProducts
    private var supplierDetails: DetailSupplierDataModel?
    private var searchedString = ""
    private var supplierList: MutableList<DetailSupplierDataModel> =
        ArrayList<DetailSupplierDataModel>()
    private var categoryList: MutableList<SearchEssentialAndDealsModel.Category> =
        ArrayList<SearchEssentialAndDealsModel.Category>()
    private var viewAllClickListener: onViewAllClickListener? = null

    constructor(
        context: Context,
        lstSearchDataArrayList: ArrayList<SearchForNewOrderMgr>,
        supplierDetails: DetailSupplierDataModel?,
        mListener: OnItemClicked,
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
        viewAllClickListener: onViewAllClickListener?,
    ) {
        this.lstSearchDataArrayList = lstSearchDataArrayList
        this.context = context
        this.mListener = mListener
        this.supplierDetails = supplierDetails
        this.viewAllClickListener = viewAllClickListener
        mFilter = FilterProducts()
    }

    override fun getItemViewType(position: Int): Int {
        val data: SearchForNewOrderMgr = lstSearchDataArrayList[position]
        return if (data.isHeader) {
            VIEW_TYPE_HEADER
        } else if (data.suppliersList != null) {
            SUPPLIER_VIEW_TYPE
        } else if (data.productDetailBySupplier != null) {
            PRODUCT_VIEW_TYPE
        } else if (data.categoriesList != null) {
            CATEGORY_VIEW_TYPE
        } else if (data.isViewAll) {
            VIEW_ALL_VIEW_TYPE
        } else {
            0
        }
    }

    override fun getItemCount(): Int {
        return lstSearchDataArrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_header_dashboard, parent, false)
            ViewHolderHeader(itemView)
        } else if (viewType == SUPPLIER_VIEW_TYPE) {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lyt_supplier_list_essentials, parent, false)
            ViewHolderSupplier(itemView)
        } else if (viewType == PRODUCT_VIEW_TYPE) {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_product_list_row, parent, false)
            ViewHolderProducts(itemView)
        } else if (viewType == VIEW_ALL_VIEW_TYPE) {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lyt_view_all_button, parent, false)
            ViewHolderViewAll(itemView)
        } else {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_catagory_list_row, parent, false)
            ViewHolderCategory(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (lstSearchDataArrayList[position].isHeader) {
            val viewHolderHeader = holder as ViewHolderHeader
            val headerWithCount =
                lstSearchDataArrayList[position].headerCount.toString() + " " + lstSearchDataArrayList[position].header

            viewHolderHeader.txtHeader.setText(headerWithCount)
        } else if (lstSearchDataArrayList[position].isViewAll) {
            val viewHolderViewAll = holder as ViewHolderViewAll
            viewHolderViewAll.btnViewAll.setOnClickListener(View.OnClickListener {
                if (lstSearchDataArrayList[position].viewAllFor
                        .equals(ZeemartAppConstants.VIEW_ALL_FOR_SUPPLIERS)
                ) {
                    viewAllClickListener!!.onViewAllSupplierClicked()
                } else {
                    viewAllClickListener!!.onViewAllCategoryClicked()
                }
            })
        } else if (lstSearchDataArrayList[position].categoriesList != null) {
            categoryList.add(lstSearchDataArrayList[position].categoriesList!!)
            val holderCategory = holder as ViewHolderCategory
            if (searchedString == "" && lstSearchDataArrayList[position].categoriesList
                    ?.name != null
            ) {
                holderCategory.txtCategoryName.setText(
                    lstSearchDataArrayList[position].categoriesList?.name
                )
            } else {
                val itemName: String =
                    lstSearchDataArrayList[position].categoriesList?.name + ""
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
                holderCategory.txtCategoryName.setText(sb)
            }
            holderCategory.lytCategory.setOnClickListener(View.OnClickListener {
                mListener.onCategoryClicked(
                    lstSearchDataArrayList[position].categoriesList?.name
                )
                val intent = Intent(context, SearchedCategoriesProductsActivity::class.java)
                intent.putExtra(
                    ZeemartAppConstants.CATEGORY_LIST, ZeemartBuyerApp.gsonExposeExclusive.toJson(
                        lstSearchDataArrayList[position].categoriesList
                    )
                )
                context.startActivity(intent)
            })
        } else if (lstSearchDataArrayList[position].suppliersList != null) {
            supplierList.add(lstSearchDataArrayList[position].detailSupplierDataModel!!)
            val holderSupplier = holder as ViewHolderSupplier
            if (searchedString == "" && lstSearchDataArrayList[position].suppliersList
                    ?.supplier?.supplierName != null
            ) {
                holderSupplier.txtSupplierName.setText(
                    lstSearchDataArrayList[position].suppliersList?.supplier
                        ?.supplierName
                )
            } else {
                val itemName: String =
                    lstSearchDataArrayList[position].suppliersList?.supplier
                        ?.supplierName + ""
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
                holderSupplier.txtSupplierName.setText(sb)
            }
            holderSupplier.lytSupplierList.setOnClickListener(View.OnClickListener {
                mListener.onEssentialOrDealSupplierClicked(
                    lstSearchDataArrayList[position].suppliersList
                )
            })
            if (StringHelper.isStringNullOrEmpty(
                    lstSearchDataArrayList[position].suppliersList?.supplier?.logoURL
                )
            ) {
                holderSupplier.imgSupplier.visibility = View.INVISIBLE
                holderSupplier.lytSupplierThumbNail.setVisibility(View.VISIBLE)
                holderSupplier.lytSupplierThumbNail.setBackgroundColor(
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        lstSearchDataArrayList[position].suppliersList?.supplier
                            ?.supplierName!!, context
                    )
                )
                holderSupplier.txtSupplierThumbNail.setText(
                    CommonMethods.SupplierThumbNailShortCutText(
                        lstSearchDataArrayList[position].suppliersList?.supplier
                            ?.supplierName!!
                    )
                )
                holderSupplier.txtSupplierThumbNail.setTextColor(
                    CommonMethods.SupplierThumbNailTextColor(
                        lstSearchDataArrayList[position].suppliersList?.supplier
                            ?.supplierName!!, context
                    )
                )
            } else {
                holderSupplier.imgSupplier.visibility = View.VISIBLE
                holderSupplier.lytSupplierThumbNail.setVisibility(View.GONE)
                Picasso.get().load(
                    lstSearchDataArrayList[position].suppliersList?.supplier?.logoURL
                ).placeholder(
                    R.drawable.placeholder_all
                ).resize(
                    ViewHolderSupplier.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                    ViewHolderSupplier.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                ).into(holderSupplier.imgSupplier)
            }
            if (lstSearchDataArrayList[position].suppliersList?.rebatePercentage != null) {
                holderSupplier.imgRebate.visibility = View.VISIBLE
                holderSupplier.txtRebateAmount.setVisibility(View.VISIBLE)
                holderSupplier.txtRebateAmount.text =
                    lstSearchDataArrayList[position].suppliersList!!.rebatePercentage.toString() + "%"
                if (lstSearchDataArrayList[position].suppliersList?.shortDesc != null) {
                    holderSupplier.txtSupplierDescription.setVisibility(View.VISIBLE)
                    val supplierDescription =
                        " • " + lstSearchDataArrayList[position].suppliersList?.shortDesc
                    holderSupplier.txtSupplierDescription.setText(supplierDescription)
                } else {
                    holderSupplier.txtSupplierDescription.setVisibility(View.GONE)
                }
            } else {
                holderSupplier.imgRebate.visibility = View.GONE
                holderSupplier.txtRebateAmount.setVisibility(View.GONE)
                if (lstSearchDataArrayList[position].suppliersList?.shortDesc != null) {
                    holderSupplier.txtSupplierDescription.setVisibility(View.VISIBLE)
                    holderSupplier.txtSupplierDescription.setText(
                        lstSearchDataArrayList[position].suppliersList?.shortDesc
                    )
                } else {
                    holderSupplier.txtSupplierDescription.setVisibility(View.GONE)
                }
            }
        } else if (lstSearchDataArrayList[position].productDetailBySupplier != null) {
            products.add(lstSearchDataArrayList[position].productDetailBySupplier!!)
            val viewHolderProducts = holder as ViewHolderProducts
            viewHolderProducts.txtAddToOrder.setText(context.resources.getString(R.string.txt_view_catalogue))
            if (!StringHelper.isStringNullOrEmpty(
                    lstSearchDataArrayList[position].productDetailBySupplier?.dealNumber
                )
            ) {
                viewHolderProducts.imgFavouriteProduct.setVisibility(View.GONE)
                if (lstSearchDataArrayList[position].productDetailBySupplier?.priceList
                        ?.get(0)?.dealPrice != null
                ) {
                    val itemPrice =
                        lstSearchDataArrayList[position].productDetailBySupplier!!.priceList!![0].dealPrice!!.getDisplayValueWithUom(
                            returnShortNameValueUnitSize(
                                lstSearchDataArrayList[position].productDetailBySupplier!!.priceList!![0].unitSize
                            )
                        )

                    if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.VISIBLE)
                        if (lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)
                                ?.originalPrice != null && lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)?.originalPrice?.amount !== 0.0
                        ) {
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
                        viewHolderProducts.txtProductUnitPrice.setText(itemPrice)
                    } else {
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.GONE)
                    }
                    if (lstSearchDataArrayList[position].productDetailBySupplier?.priceList
                            ?.get(0)
                            ?.originalPrice != null && lstSearchDataArrayList[position].productDetailBySupplier
                            ?.priceList?.get(0)?.originalPrice?.amount !== 0.0
                    ) {
                        val string = SpannableString(
                            lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)?.originalPrice?.getDisplayValueWithUom(
                                UnitSizeModel.returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[position].productDetailBySupplier
                                        ?.priceList?.get(0)?.unitSize
                                )
                            )
                        )
                        string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                        viewHolderProducts.txtProductStrikePrice.setVisibility(View.VISIBLE)
                        viewHolderProducts.txtProductStrikePrice.setText(string)
                    } else {
                        viewHolderProducts.txtProductStrikePrice.setVisibility(View.GONE)
                    }
                } else {
                    if (lstSearchDataArrayList[position].productDetailBySupplier?.priceList
                            ?.get(0)
                            ?.originalPrice != null && lstSearchDataArrayList[position].productDetailBySupplier
                            ?.priceList?.get(0)?.originalPrice?.amount!== 0.0
                    ) {
                        val itemPrice: String? =
                            lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)?.originalPrice?.getDisplayValueWithUom(
                                UnitSizeModel.returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[position].productDetailBySupplier
                                        ?.priceList?.get(0)?.unitSize
                                )
                            )
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.VISIBLE)
                        viewHolderProducts.txtProductUnitPrice.setTextColor(
                            context.resources.getColor(
                                R.color.grey_medium
                            )
                        )
                        viewHolderProducts.txtProductUnitPrice.setText(itemPrice)
                    } else {
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.GONE)
                    }
                    viewHolderProducts.txtProductStrikePrice.setVisibility(View.GONE)
                }
            } else if (!StringHelper.isStringNullOrEmpty(
                    lstSearchDataArrayList[position].productDetailBySupplier?.essentialsId
                )
            ) {
                viewHolderProducts.imgFavouriteProduct.setVisibility(View.GONE)
                if (lstSearchDataArrayList[position].productDetailBySupplier?.priceList
                        ?.get(0)?.essentialPrice != null
                ) {
                    val itemPrice: String? =
                        lstSearchDataArrayList[position].productDetailBySupplier?.priceList
                            ?.get(0)?.essentialPrice?.getDisplayValueWithUom(
                            UnitSizeModel.returnShortNameValueUnitSize(
                                lstSearchDataArrayList[position].productDetailBySupplier
                                    ?.priceList?.get(0)?.unitSize
                            )
                        )
                    if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.VISIBLE)
                        if (lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)
                                ?.originalPrice != null && lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)?.originalPrice?.amount !== 0.0
                        ) {
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
                        viewHolderProducts.txtProductUnitPrice.setText(itemPrice)
                    } else {
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.GONE)
                    }
                    if (lstSearchDataArrayList[position].productDetailBySupplier?.priceList
                            ?.get(0)
                            ?.originalPrice != null && lstSearchDataArrayList[position].productDetailBySupplier
                            ?.priceList?.get(0)?.originalPrice?.amount !== 0.0
                    ) {
                        val string = SpannableString(
                            lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)?.originalPrice?.getDisplayValueWithUom(
                                UnitSizeModel.returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[position].productDetailBySupplier
                                        ?.priceList?.get(0)?.unitSize
                                )
                            )
                        )
                        string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                        viewHolderProducts.txtProductStrikePrice.setVisibility(View.VISIBLE)
                        viewHolderProducts.txtProductStrikePrice.setText(string)
                    } else {
                        viewHolderProducts.txtProductStrikePrice.setVisibility(View.GONE)
                    }
                } else {
                    if (lstSearchDataArrayList[position].productDetailBySupplier?.priceList
                            ?.get(0)
                            ?.originalPrice != null && lstSearchDataArrayList[position].productDetailBySupplier
                            ?.priceList?.get(0)?.originalPrice?.amount !== 0.0
                    ) {
                        val itemPrice: String? =
                            lstSearchDataArrayList[position].productDetailBySupplier
                                ?.priceList?.get(0)?.originalPrice?.getDisplayValueWithUom(
                                UnitSizeModel.returnShortNameValueUnitSize(
                                    lstSearchDataArrayList[position].productDetailBySupplier
                                        ?.priceList?.get(0)?.unitSize
                                )
                            )
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.VISIBLE)
                        viewHolderProducts.txtProductUnitPrice.setTextColor(
                            context.resources.getColor(
                                R.color.grey_medium
                            )
                        )
                        viewHolderProducts.txtProductUnitPrice.setText(itemPrice)
                    } else {
                        viewHolderProducts.txtProductUnitPrice.setVisibility(View.GONE)
                    }
                    viewHolderProducts.txtProductStrikePrice.setVisibility(View.GONE)
                }
            }
            if (lstSearchDataArrayList[position].productDetailBySupplier
                    ?.images != null && lstSearchDataArrayList[position].productDetailBySupplier
                    ?.images?.size!! > 0
            ) {
                val imagesArray: List<ImagesModel>? =
                    lstSearchDataArrayList[position].productDetailBySupplier?.images
                var imageUrl: String? = null
                for (i in imagesArray?.indices!!) {
                    if (!StringHelper.isStringNullOrEmpty(imageUrl)) {
                        break
                    }
                    if (imagesArray[i] != null) {
                        if (imagesArray[i].imageURL != null) imageUrl = imagesArray[i].imageURL
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
                lstSearchDataArrayList[position].productDetailBySupplier
                    ?.supplierProductCode!!,
                lstSearchDataArrayList[position].productDetailBySupplier?.tags,
                lstSearchDataArrayList[position].productDetailBySupplier?.supplierName!!
            )
            if (lstSearchDataArrayList[position].productDetailBySupplier
                    ?.supplier != null && lstSearchDataArrayList[position].productDetailBySupplier
                    ?.supplier?.supplierName != null
            ) {
                supplierName =
                    lstSearchDataArrayList[position].productDetailBySupplier?.supplier
                        ?.supplierName
            }
            if (searchStringArray != null && searchStringArray!!.size > 0) {
                val itemName: String = lstSearchDataArrayList[position].productDetailBySupplier
                    ?.productName + ""
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
                val itemCustomName: String =
                    lstSearchDataArrayList[position].productDetailBySupplier
                        ?.productCustomName + ""
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
                if (lstSearchDataArrayList[position].productDetailBySupplier
                        ?.productCustomName != null
                ) {
                    viewHolderProducts.txtProductCustomName.setVisibility(View.VISIBLE)
                    viewHolderProducts.txtItemName.setText(sbCustomName)
                    viewHolderProducts.txtProductCustomName.setText(sb)
                } else {
                    viewHolderProducts.txtItemName.setText(sb)
                    viewHolderProducts.txtProductCustomName.setVisibility(View.GONE)
                }
                if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                    viewHolderProducts.txtProductCodeTag.setText(supplierName)
                } else {
                    viewHolderProducts.txtProductCodeTag.setText("$productCodeTag • $supplierName")
                }
            } else {
                if (lstSearchDataArrayList[position].productDetailBySupplier
                        ?.productCustomName != null
                ) {
                    viewHolderProducts.txtProductCustomName.setText(
                        lstSearchDataArrayList[position].productDetailBySupplier
                            ?.productName
                    )
                    viewHolderProducts.txtProductCustomName.setVisibility(View.VISIBLE)
                    viewHolderProducts.txtItemName.setText(
                        lstSearchDataArrayList[position].productDetailBySupplier
                            ?.productCustomName
                    )
                } else {
                    viewHolderProducts.txtItemName.setText(
                        lstSearchDataArrayList[position].productDetailBySupplier
                            ?.productName
                    )
                    viewHolderProducts.txtProductCustomName.setVisibility(View.GONE)
                }
                if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                    viewHolderProducts.txtProductCodeTag.setText(supplierName)
                } else {
                    viewHolderProducts.txtProductCodeTag.setText("$productCodeTag • $supplierName")
                }
            }
            if (position == lstSearchDataArrayList.size - 1) {
                viewHolderProducts.lytEmptySpace.setVisibility(View.VISIBLE)
            } else {
                viewHolderProducts.lytEmptySpace.setVisibility(View.GONE)
            }
            viewHolderProducts.imgProductImage.setOnClickListener { callProductDetail(position) }
            viewHolderProducts.lytProductName.setOnClickListener(View.OnClickListener {
                callProductDetail(
                    position
                )
            })
            viewHolderProducts.lytAddToOrderAndUOM.setOnClickListener(View.OnClickListener {
                mListener.onEssentialOrDealProductClicked(
                    lstSearchDataArrayList[position].productDetailBySupplier
                )
            })
        }
    }

    private fun getProductCodeAndTag(
        productCode: String,
        tags: List<String>?,
        supplierName: String,
    ): String {
        var productCodeTag = ""
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
                    productCodeTag = if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                        attributeValuesAll
                    } else {
                        "$productCodeTag • $attributeValuesAll"
                    }
                }
            }
        }
        return productCodeTag
    }

    inner class ViewHolderViewAll(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var btnViewAll: TextView

        init {
            btnViewAll = itemView.findViewById<TextView>(R.id.btn_view_all)
            ZeemartBuyerApp.setTypefaceView(
                btnViewAll,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    fun callProductDetail(position: Int) {
        if (supplierDetails != null) {
            val newIntent = Intent(context, ProductDetailActivity::class.java)
            val productDetails: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                lstSearchDataArrayList[position].productDetailBySupplier,
                ProductDetailBySupplier::class.java
            )
            newIntent.putExtra(ZeemartAppConstants.PRODUCT_DETAILS_JSON, productDetails)
            val supplierDetail: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplierDetails)
            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, supplierDetail)
            newIntent.putExtra(
                ZeemartAppConstants.PRODUCT_SKU,
                lstSearchDataArrayList[position].productDetailBySupplier?.sku
            )
            var outletId = ""
            outletId = if (SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")?.isEmpty() == true) ({
                SharedPref.defaultOutlet?.outletId
            }).toString() else ({
                SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
            }).toString()
            newIntent.putExtra(ZeemartAppConstants.OUTLET_ID, outletId)
            if (lstSearchDataArrayList[position].productDetailBySupplier
                    ?.supplier != null
            ) {
                newIntent.putExtra(
                    ZeemartAppConstants.SUPPLIER_NAME,
                    lstSearchDataArrayList[position].productDetailBySupplier?.supplier
                        ?.supplierName
                )
            }
            newIntent.putExtra(
                ZeemartAppConstants.SUPPLIER_ID,
                lstSearchDataArrayList[position].productDetailBySupplier?.supplierId
            )
            if (!StringHelper.isStringNullOrEmpty(
                    lstSearchDataArrayList[position].productDetailBySupplier?.essentialsId
                )
            ) {
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST
                )
            } else if (!StringHelper.isStringNullOrEmpty(
                    lstSearchDataArrayList[position].productDetailBySupplier?.dealNumber
                )
            ) {
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

//    val itemCount: Int
//        get() = lstSearchDataArrayList.size

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeader: TextView

        init {
            txtHeader = itemView.findViewById<TextView>(R.id.txt_dashboard_sublist_header)
            txtHeader.setTextColor(context.resources.getColor(R.color.black))
            ZeemartBuyerApp.setTypefaceView(
                txtHeader,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtHeader.setTextSize(16f)
            val params: LinearLayout.LayoutParams =
                txtHeader.getLayoutParams() as LinearLayout.LayoutParams
            params.setMargins(15, 0, 0, 0)
            txtHeader.setLayoutParams(params)
        }
    }

    class ViewHolderSupplier(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var txtSupplierDescription: TextView
        var txtRebateAmount: TextView
        var lytSupplierList: RelativeLayout
        var imgRebate: ImageView
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.img_supplier_essentials)
            txtSupplierName = itemView.findViewById<TextView>(R.id.txt_supplier_name_essentials)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtSupplierDescription = itemView.findViewById<TextView>(R.id.txt_date_type_essentials)
            txtRebateAmount = itemView.findViewById<TextView>(R.id.txt_rebate_amount)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierDescription,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtRebateAmount,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            lytSupplierList = itemView.findViewById<RelativeLayout>(R.id.layout_supplier_list)
            imgRebate = itemView.findViewById(R.id.img_rebate)
            lytSupplierThumbNail =
                itemView.findViewById<RelativeLayout>(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById<TextView>(R.id.txt_supplier_thumbnail)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }

        companion object {
            val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH: Int = CommonMethods.dpToPx(30)
            val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT: Int = CommonMethods.dpToPx(30)
        }
    }

    class ViewHolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtCategoryName: TextView
        var lytCategory: RelativeLayout

        init {
            txtCategoryName = itemView.findViewById<TextView>(R.id.txt_category_name)
            lytCategory = itemView.findViewById<RelativeLayout>(R.id.lyt_category)
            ZeemartBuyerApp.setTypefaceView(
                txtCategoryName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
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
            txtProductCodeTag = itemView.findViewById<TextView>(R.id.txt_product_code_tag)
            lytProductName = itemView.findViewById<RelativeLayout>(R.id.lyt_product_name)
            txtItemName = itemView.findViewById<TextView>(R.id.txt_product_name)
            txtAddToOrder = itemView.findViewById<TextView>(R.id.txt_add_order)
            lytProductItems = itemView.findViewById<LinearLayout>(R.id.lyt_product_details)
            imgFavouriteProduct = itemView.findViewById<ImageButton>(R.id.img_favourite)
            btnReduceQuantity = itemView.findViewById<ImageButton>(R.id.btn_reduce_quantity)
            txtProductQuantity = itemView.findViewById<TextView>(R.id.txt_quantity_value)
            btnIncreaseQuantity = itemView.findViewById<ImageButton>(R.id.btn_inc_quantity)
            lytProductQuantity = itemView.findViewById<LinearLayout>(R.id.lyt_quantity_sku)
            lytQuantitySelection = itemView.findViewById<RelativeLayout>(R.id.lyt_selection)
            txtProductUnitPrice = itemView.findViewById<TextView>(R.id.txt_product_unit_price)
            txtProductStrikePrice = itemView.findViewById<TextView>(R.id.txt_product_strike_price)
            txtProductCustomName = itemView.findViewById<TextView>(R.id.txt_product_changed_name)
            txtItemUom = itemView.findViewById<TextView>(R.id.txt_uom_name)
            txtItemUom.setVisibility(View.GONE)
            lytAddToOrderAndUOM = itemView.findViewById<LinearLayout>(R.id.lyt_add_to_order_and_uom)
            imgProductImage = itemView.findViewById(R.id.img_product_image)
            imgProductImage.visibility = View.GONE
            lytEmptySpace = itemView.findViewById<LinearLayout>(R.id.lyt_empty_space)
            lytEmptySpace.setVisibility(View.GONE)
            ZeemartBuyerApp.setTypefaceView(
                txtProductCodeTag,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtItemName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtAddToOrder,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductUnitPrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductCustomName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
            ZeemartBuyerApp.setTypefaceView(
                txtItemUom,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtProductStrikePrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    private inner class FilterProducts : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null && constraint.length > 0) {
                searchedString = constraint as String
                searchStringArray =
                    searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val tempList: MutableList<ProductDetailBySupplier> =
                    ArrayList<ProductDetailBySupplier>()
                val tempListSupplier: ArrayList<DetailSupplierDataModel> =
                    ArrayList<DetailSupplierDataModel>()
                val tempListCategories: ArrayList<SearchEssentialAndDealsModel.Category> =
                    ArrayList<SearchEssentialAndDealsModel.Category>()
                // search content in friend list
                for (i in products.indices) {
                    if (products[i].productName?.lowercase(Locale.getDefault())?.contains(
                            constraint.toString().lowercase(
                                Locale.getDefault()
                            )
                        ) == true
                    ) {
                        tempList.add(products[i])
                    }
                }
                for (i in supplierList.indices) {
                    if (!supplierList[i].isFavoriteProducts && supplierList[i].supplier.supplierName?.lowercase(
                            Locale.getDefault()
                        )?.contains(constraint.toString().lowercase(Locale.getDefault())) == true
                    ) {
                        tempListSupplier.add(supplierList[i])
                    }
                }
                for (i in categoryList.indices) {
                    if (categoryList[i].name?.toLowerCase()?.contains(
                            constraint.toString().lowercase(
                                Locale.getDefault()
                            )
                        ) == true
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
                products = results.values as MutableList<ProductDetailBySupplier>
                supplierList = results.values as MutableList<DetailSupplierDataModel>
                categoryList = results.values as MutableList<SearchEssentialAndDealsModel.Category>
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
        private const val VIEW_TYPE_HEADER = 1
        private const val SUPPLIER_VIEW_TYPE = 2
        private const val PRODUCT_VIEW_TYPE = 3
        private const val CATEGORY_VIEW_TYPE = 4
        private const val VIEW_ALL_VIEW_TYPE = 5
    }
}