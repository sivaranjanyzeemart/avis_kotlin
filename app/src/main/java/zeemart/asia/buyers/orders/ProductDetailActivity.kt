package zeemart.asia.buyers.orders

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrInterface
import com.r0adkll.slidr.model.SlidrListener
import com.r0adkll.slidr.model.SlidrPosition
import org.json.JSONObject
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.*
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.deals.DealProductListActivity
import zeemart.asia.buyers.essentials.EssentialsProductListActivity
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.ProductDataHelper.calculateTotalPriceChanged
import zeemart.asia.buyers.helper.ProductDataHelper.changeQuantityUOMTextFontSize
import zeemart.asia.buyers.helper.ProductDataHelper.createSelectedProductObject
import zeemart.asia.buyers.helper.ProductDataHelper.createSelectedProductObjectForBelowPar
import zeemart.asia.buyers.helper.ProductDataHelper.getKeyProductMap
import zeemart.asia.buyers.helper.ProductDataHelper.updateFavSkuStatus
import zeemart.asia.buyers.helper.ProductQuantityChangeDialogHelper
import zeemart.asia.buyers.helper.customviews.TagsItemDecoration
import zeemart.asia.buyers.helper.customviews.WrapContentViewPager
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.interfaces.ProductDetailsListener
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.UnitSizeModel.Companion.getValueDecimalAllowedCheck
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseListener
import zeemart.asia.buyers.network.EssentialsApi.getPaginatedEssentials
import zeemart.asia.buyers.network.EssentialsApi.markEssentialsProductsAsFavourite
import zeemart.asia.buyers.network.EssentialsApi.removeEssentialsProductsAsFavourite
import zeemart.asia.buyers.network.OrderHelper.DealsResponseListener
import zeemart.asia.buyers.network.OrderHelper.getActiveDeals
import zeemart.asia.buyers.network.ProductsApi.getSpecificProduct
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.createorders.AddToOrderActivity
import zeemart.asia.buyers.orders.createorders.SearchForNewOrder

class ProductDetailActivity : AppCompatActivity(),
    ProductQuantityChangeDialogHelper.ProductDataChangeListener {
    private var productSku: String? = null
    private var productUnitsize: String? = null
    private var outletId: String? = null
    private var progressBar: ProgressBar? = null
    private lateinit var viewPager: WrapContentViewPager
    private lateinit var nestedScrollView: NestedScrollView
    private var txtSupplierName: TextView? = null
    private var txtItemName: TextView? = null
    private var txtDescription: TextView? = null
    private var txtSoldBySupplierName: TextView? = null
    private var txtSoldByHeader: TextView? = null
    private var txtPriceLastUpdated: TextView? = null
    private var supplierLogo: ImageView? = null
    private lateinit var lstCertificates: RecyclerView
    private var lstPrice: RecyclerView? = null
    private var lstDetails: RecyclerView? = null
    private var supplierName: String? = null
    private var supplierLogoURL: String? = null
    private var productPriceList: MutableList<ProductPriceList>? = null
    private lateinit var productDetails: ProductDetailBySupplier
    private lateinit var lytNoImage: LinearLayout
    private var txtDescriptionHeading: TextView? = null
    private var txtDetailsHeading: TextView? = null
    private var txtNoImage: TextView? = null
    private var supplierId: String? = null
    private var calledFrom: String? = ""
    private var supplierDetails: DetailSupplierDataModel? = null
    private var imgFavourite: ImageButton? = null
    private var isFavouriteSelected = false
    private var filterProducts: ProductDetailBySupplier? = null
    private var selectedProductsListener: SupplierProductListAdapter.SelectedProductsListener? =
        null
    private val selectedSearchedProductDetails: ProductDetailBySupplier? = null
    private var selectedProductsMap: MutableMap<String, Product>? = null
    var perUnitPrice: TextView? = null
    var txtProductStrikePrice: TextView? = null
    var lytAddToOrderAndUOM: LinearLayout? = null
    var lytProductQuantity: LinearLayout? = null
    var lytQuantitySelection: RelativeLayout? = null
    var txtItemUom: TextView? = null
    var txtAddToOrder: TextView? = null
    var txtProductUnitPrice: TextView? = null
    var txtProductQuantity: TextView? = null
    var btnIncreaseQuantity: ImageButton? = null
    var btnReduceQuantity: ImageButton? = null
    private lateinit var dialog: CustomChangeQuantityDialog
    private lateinit var btnBack: ImageButton
    private var slidrInterface: SlidrInterface? = null
    var slidrListener: SlidrListener = object : SlidrListener {
        override fun onSlideStateChanged(state: Int) {
            onStatusChanged(true)
        }

        override fun onSlideChange(percent: Float) {}
        override fun onSlideOpened() {}
        override fun onSlideClosed(): Boolean {
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        val bundle = intent.extras
        val config = SlidrConfig.Builder()
            .position(SlidrPosition.TOP)
            .listener(slidrListener)
            .build()
        slidrInterface = Slidr.attach(this, config)
        setUIComponents()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.OUTLET_ID)) {
                outletId = bundle.getString(ZeemartAppConstants.OUTLET_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.PRODUCT_SKU)) {
                productSku = bundle.getString(ZeemartAppConstants.PRODUCT_SKU)
            }
            if (bundle.containsKey(ZeemartAppConstants.PRODUCT_UNITSIZE)) {
                productUnitsize = bundle.getString(ZeemartAppConstants.PRODUCT_UNITSIZE)
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_MAPPING)) {
                val type = object : TypeToken<Map<String?, Product?>?>() {}.type
                selectedProductsMap = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.SELECTED_MAPPING), type
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
                val json = bundle.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                val products = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    json,
                    ProductDetailBySupplier::class.java
                )
                filterProducts = products
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_NAME)) {
                supplierName = bundle.getString(ZeemartAppConstants.SUPPLIER_NAME)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_LOGO)) {
                supplierLogoURL = bundle.getString(ZeemartAppConstants.SUPPLIER_LOGO)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_ID)) {
                supplierId = bundle.getString(ZeemartAppConstants.SUPPLIER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_DETAIL_INFO)) {
                supplierDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.SUPPLIER_DETAIL_INFO),
                    DetailSupplierDataModel::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                calledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
            if (bundle.containsKey(ZeemartAppConstants.PRODUCT_DETAILS_JSON)) {
                val productDetailJson = bundle.getString(ZeemartAppConstants.PRODUCT_DETAILS_JSON)
                productDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    productDetailJson,
                    ProductDetailBySupplier::class.java
                )
                productPriceList = productDetails.priceList
                for (i in productPriceList!!.indices) {
                    if (productPriceList!![i].isStatus(ProductPriceList.UnitSizeStatus.INVISIBLE)) {
                        productPriceList!!.removeAt(i)
                    }
                }
            }
        }
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST)
        {
            imgFavourite!!.visibility = View.GONE
        } else {
            imgFavourite!!.visibility = View.VISIBLE
        }
        if (productDetails != null && productDetails!!.isFavourite) {
            imgFavourite!!.setImageResource(R.drawable.favourite_blue)
        } else {
            imgFavourite!!.setImageResource(R.drawable.favourite_grey)
        }
        imgFavourite!!.setOnClickListener {
            isFavouriteSelected = true
            if (productDetails!!.isFavourite) {
                productDetails!!.isFavourite = false
                imgFavourite!!.setImageResource(R.drawable.favourite_grey)
                if (!StringHelper.isStringNullOrEmpty(calledFrom)
                    && calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST || calledFrom
                    == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST) {
                    removeEssentialsProductsAsFavourite(
                        this@ProductDetailActivity,
                        productDetails!!.essentialsProductId
                    )
                } else {
                    updateFavSkuStatus(this@ProductDetailActivity, productDetails!!.sku, false)
                }
            } else {
                productDetails!!.isFavourite = true
                imgFavourite!!.setImageResource(R.drawable.favourite_blue)
                if (!StringHelper.isStringNullOrEmpty(calledFrom)
                    && calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST || calledFrom
                    == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST) {
                    markEssentialsProductsAsFavourite(
                        this@ProductDetailActivity,
                        productDetails!!.essentialsProductId
                    )
                } else {
                    updateFavSkuStatus(this@ProductDetailActivity, productDetails!!.sku, true)
                }
            }
        }
        progressBar!!.visibility = View.VISIBLE
        viewPager!!.visibility = View.GONE
        nestedScrollView!!.visibility = View.GONE
        lstDetails!!.isNestedScrollingEnabled = false
        lstPrice!!.isNestedScrollingEnabled = false
        lstCertificates!!.isNestedScrollingEnabled = false
        btnIncreaseQuantity!!.setOnClickListener {
            val key =
                getKeyProductMap(filterProducts!!.sku, filterProducts!!.priceList!![0].unitSize!!)
            var product = selectedProductsMap!![key]
            var quantity = product!!.quantity
            if (quantity == 0.0) {
                selectedProductsListener!!.onProductSelected(filterProducts)
                selectedProductsListener!!.onProductSelectedForRecentSearch(filterProducts!!.productName)
            }
            quantity = quantity + 1
            product.quantity = (quantity)
            product = calculateTotalPriceChanged(product)
            txtProductQuantity!!.text =
                getValueDecimalAllowedCheck(product!!.unitSize, product.quantity) + " "
            txtProductQuantity!!.append(
                changeQuantityUOMTextFontSize(
                    returnShortNameValueUnitSizeForQuantity(
                        product.unitSize
                    ), this@ProductDetailActivity
                )
            )
        }
        btnReduceQuantity!!.setOnClickListener {
            val key =
                getKeyProductMap(filterProducts!!.sku, filterProducts!!.priceList!![0].unitSize!!)
            var product = selectedProductsMap!![key]
            var quantity = product!!.quantity
            quantity = quantity - 1
            product!!.quantity = (quantity)
            product = calculateTotalPriceChanged(product)
            txtProductQuantity!!.text =
                getValueDecimalAllowedCheck(product!!.unitSize, product!!.quantity) + " "
            txtProductQuantity!!.append(
                changeQuantityUOMTextFontSize(
                    returnShortNameValueUnitSizeForQuantity(
                        product!!.unitSize
                    ), this@ProductDetailActivity
                )
            )
            if (quantity <= 0) {
                filterProducts!!.isSelected = false
                selectedProductsListener!!.onProductDeselected(filterProducts)
                //reset all the price list uom
                for (i in filterProducts!!.priceList!!.indices) {
                    filterProducts!!.priceList!![i].selected = false
                }
                selectedProductsMap!!.remove(key)
                displayAddToOrderLayout()
            }
        }
        txtProductQuantity!!.setOnClickListener {
            Log.d("Map", selectedProductsMap.toString() + "******")
            if (dialog == null || !dialog!!.isShowing) {
                val key = getKeyProductMap(
                    filterProducts!!.sku,
                    filterProducts!!.priceList!![0].unitSize!!
                )
                val createOrderHelperDialog = ProductQuantityChangeDialogHelper(
                    selectedProductsMap!![key],
                    this@ProductDetailActivity,
                    supplierDetails!!,
                    filterProducts!!,
                    this@ProductDetailActivity
                )
                dialog = createOrderHelperDialog.createChangeQuantityDialog()
                dialog.getWindow()!!
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
        if (filterProducts != null && filterProducts!!.isSelected) {
            setSelectedProductLayout()
        } else {
            //show add to order text
            displayAddToOrderLayout()
        }
        selectedProductsListener = object : SupplierProductListAdapter.SelectedProductsListener {
            override fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?) {}
            override fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?) {}
            override fun onProductSelectedForRecentSearch(productName: String?) {}
        }
        //call the API to display Product details
        getSpecificProduct(this, productSku, outletId!!, object : ProductDetailsListener {
            override fun onSuccessResponse(productData: ProductDetailModel?) {
                progressBar!!.visibility = View.GONE
                if (productData != null) {
                    viewPager!!.visibility = View.VISIBLE
                    nestedScrollView!!.visibility = View.VISIBLE
                    txtItemName!!.text = productData.productName + ""
                    txtSupplierName!!.text =
                        String.format(getString(R.string.format_sold_by), supplierName)
                    txtSoldBySupplierName!!.text = supplierName
                    displayDetails()
                    checkSelectedProduct()
                    val productDataList: MutableList<ProductAttributeModel> = ArrayList()
                    if (!StringHelper.isStringNullOrEmpty(productData.description)) {
                        txtDescription!!.text = productData.description
                    }
                    if (!StringHelper.isStringNullOrEmpty(productData.brand)) {
                        val brandData = ProductAttributeModel()
                        brandData.specificAttribute = "Brand"
                        val brandValue: MutableList<String?> = ArrayList()
                        brandValue.add(productData.brand)
                        brandData.specificAttributeValue = brandValue as List<String>?
                        productDataList.add(brandData)
                    }
                    if (!StringHelper.isStringNullOrEmpty(productData.supplierProductCode)) {
                        val productCodeData = ProductAttributeModel()
                        productCodeData.specificAttribute = "Supplier’s product code"
                        val productCodeValue: MutableList<String?> = ArrayList()
                        productCodeValue.add(productData.supplierProductCode)
                        productCodeData.specificAttributeValue = productCodeValue as List<String>?
                        productDataList.add(productCodeData)
                    }
                    if (productData.tags != null && productData.tags!!.size > 0) {
                        val tagsData = ProductAttributeModel()
                        tagsData.specificAttribute = "Tags"
                        tagsData.specificAttributeValue = productData.tags
                        productDataList.add(tagsData)
                    }
                    if (productPriceList != null && productPriceList!!.size > 0 && !StringHelper.isStringNullOrEmpty(
                            productPriceList!![0].shelfLife
                        )
                    ) {
                        val tagsData = ProductAttributeModel()
                        tagsData.specificAttribute = "Shelf life"
                        val productShelfLife: MutableList<String?> = ArrayList()
                        productShelfLife.add(productPriceList!![0].shelfLife)
                        tagsData.specificAttributeValue = productShelfLife as List<String>?
                        productDataList.add(tagsData)
                    }
                    if (productData.attributes != null) {
                        if (productData.attributes!!.size > 0) {
                            for (i in productData.attributes!!.indices) {
                                productDataList.add(productData.attributes!![i])
                            }
                        }
                    }
                    if (productDataList.size > 0) {
                        txtDetailsHeading!!.visibility = View.VISIBLE
                        lstDetails!!.visibility = View.VISIBLE
                        lstDetails!!.layoutManager = LinearLayoutManager(this@ProductDetailActivity)
                        lstDetails!!.adapter = ProductDetailsAttributeListAdapter(
                            this@ProductDetailActivity,
                            productDataList
                        )
                    } else {
                        txtDetailsHeading!!.visibility = View.GONE
                        lstDetails!!.visibility = View.GONE
                    }
                    if (productPriceList != null && productPriceList!!.size > 0) {
                        lstPrice!!.visibility = View.GONE
                        lstPrice!!.layoutManager = LinearLayoutManager(this@ProductDetailActivity)
                        lstPrice!!.adapter = ProductDetailsPriceListAdapter(
                            this@ProductDetailActivity,
                            productPriceList!!,
                            productDetails!!.stocks,
                            calledFrom!!,
                            productDetails!!.timePriceUpdated,
                            supplierDetails
                        )
                        loadPriceInfo(productPriceList!![0])
                    } else {
                        lstPrice!!.visibility = View.GONE
                    }
                    if (productData.certifications != null && productData.certifications!!.size > 0) {
                        lstCertificates!!.visibility = View.VISIBLE
                        val flexboxLayoutManagerCategories =
                            FlexboxLayoutManager(this@ProductDetailActivity)
                        lstCertificates!!.layoutManager = flexboxLayoutManagerCategories
                        lstCertificates!!.addItemDecoration(TagsItemDecoration(15))
                        //                        lstCertificates.setLayoutManager(new LinearLayoutManager(ProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, true));
                        lstCertificates!!.adapter = ProductCertificationsListingAdapter(
                            this@ProductDetailActivity,
                            productData.certifications!!
                        )
                    } else {
                        lstCertificates!!.visibility = View.GONE
                    }
                    if (productData.images != null && productData.images!!.size > 0) {
                        val imagesArray: List<ImagesModel?>? = productData.images
                        val imageUrls: MutableList<String> = ArrayList()
                        for (i in imagesArray!!.indices) {
                            if (imagesArray[i] != null) {
                                if (imagesArray[i]!!.imageFileNames != null && imagesArray[i]!!.imageFileNames.size > 0 && !StringHelper.isStringNullOrEmpty(
                                        imagesArray[i]!!.imageURL
                                    )
                                ) {
                                    for (j in imagesArray[i]!!.imageFileNames.indices) {
                                        if (!StringHelper.isStringNullOrEmpty(imagesArray[i]!!.imageFileNames[j])) {
                                            val imageUrl =
                                                imagesArray[i]!!.imageURL + imagesArray[i]!!.imageFileNames[j]
                                            imageUrls.add(imageUrl)
                                        }
                                    }
                                }
                            }
                        }
                        if (imageUrls != null && imageUrls.size > 0) {
                            viewPager!!.visibility = View.VISIBLE
                            lytNoImage!!.visibility = View.GONE
                            viewPager!!.adapter =
                                ImageSwipeAdapter(this@ProductDetailActivity, imageUrls)
                        } else {
                            viewPager!!.visibility = View.INVISIBLE
                            lytNoImage!!.visibility = View.VISIBLE
                        }
                    } else {
                        viewPager!!.visibility = View.INVISIBLE
                        lytNoImage!!.visibility = View.VISIBLE
                    }
                } else {
                    displayDataFromProductsListing()
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {
                progressBar!!.visibility = View.GONE
                displayDataFromProductsListing()
            }
        })
        btnBack = findViewById(R.id.img_btn_back)
        btnBack.setOnClickListener(View.OnClickListener {
            onStatusChanged(false)
            overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
        })
    }

    private fun setSupplierDetails(position: Int) {
        if (supplierDetails == null) {
            supplierDetails = DetailSupplierDataModel()
            supplierDetails!!.supplier = filterProducts!!.supplier!!
        }
    }

    private fun loadMap(key: String): Map<String, Product> {
        val outputMap: Map<String, Product> = HashMap()
        val pSharedPref = getSharedPreferences(this.packageName, MODE_PRIVATE)
        try {
            if (pSharedPref != null) {
                val jsonString = pSharedPref.getString(key, JSONObject().toString())
                val type = object : TypeToken<Map<String?, Product?>?>() {}.type
                val gson = Gson()
                return gson.fromJson(jsonString, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputMap
    }

    private fun saveMap(key: String, inputMap: Map<String, Product>) {
        val pSharedPref = getSharedPreferences(this.packageName, MODE_PRIVATE)
        if (pSharedPref != null) {
//            JSONObject jsonObject = new JSONObject(inputMap);
//            String jsonString = jsonObject.toString();
//            SharedPreferences.Editor editor = pSharedPref.edit();
//            editor.remove(key).commit();
//            editor.putString(key, jsonString);
//            editor.commit();
            val gson = Gson()
            val hashMapString = gson.toJson(inputMap)
            //save in shared prefs
            pSharedPref.edit().putString(key, hashMapString).apply()
        }
    }

    private fun setUIComponents() {
        progressBar = findViewById(R.id.progressBarProductDetail)
        viewPager = findViewById(R.id.product_detail_view_pager)
        //need to specify width for the view pager so that the nested scroll view is scrollable.
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.width = CommonMethods.getDeviceDimensions(this@ProductDetailActivity).deviceWidth
        viewPager.setLayoutParams(params)
        nestedScrollView = findViewById(R.id.product_detail_nested_scroll_view)
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val TAG = "nested_sync"
            if (scrollY > oldScrollY) {
                Log.e(TAG, "Scroll DOWN")
                slidrInterface!!.lock()
            }
            if (scrollY < oldScrollY) {
                Log.e(TAG, "Scroll UP")
                slidrInterface!!.lock()
            }
            if (scrollY == 0) {
                Log.e(TAG, "TOP SCROLL")
                slidrInterface!!.unlock()
            }
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                Log.e(TAG, "BOTTOM SCROLLED")
                slidrInterface!!.lock()
            }
        })
        txtItemName = findViewById(R.id.product_details_product_name)
        txtSupplierName = findViewById(R.id.product_details_supplier_name)
        txtSoldByHeader = findViewById(R.id.product_detail_sold_by_text)
        txtSoldBySupplierName = findViewById(R.id.txt_supplier_name)
        txtPriceLastUpdated = findViewById(R.id.product_detail_price_last_updated)
        supplierLogo = findViewById(R.id.img_supplier)
        txtDescription = findViewById(R.id.product_detail_description_data)
        txtNoImage = findViewById(R.id.txt_no_image)
        //txtBrandName = findViewById(R.id.product_detail_brand_value);
        lstPrice = findViewById(R.id.product_detail_price_list)
        lstCertificates = findViewById(R.id.product_detail_lst_certificates)
        val flexboxLayoutManager = FlexboxLayoutManager(this)
        lstCertificates.setLayoutManager(flexboxLayoutManager)
        lstCertificates.addItemDecoration(TagsItemDecoration(15))
        lstDetails = findViewById(R.id.product_detail_lst_product_attributes)
        //txtBrandTag = findViewById(R.id.product_detail_brand_tag);
        lytNoImage = findViewById(R.id.lyt_no_image)
        lytNoImage.setVisibility(View.GONE)
        txtDescriptionHeading = findViewById(R.id.product_detail_description_heading)
        txtDetailsHeading = findViewById(R.id.product_detail_detail_heading)
        imgFavourite = findViewById(R.id.img_favourite)
        perUnitPrice = findViewById(R.id.txt_product_unit_price)
        txtProductStrikePrice = findViewById(R.id.txt_product_strike_price)
        lytAddToOrderAndUOM = findViewById(R.id.lyt_add_to_order_and_uom)
        lytProductQuantity = findViewById(R.id.lyt_quantity_sku)
        lytQuantitySelection = findViewById(R.id.lyt_selection)
        txtItemUom = findViewById(R.id.txt_uom_name)
        txtAddToOrder = findViewById(R.id.txt_add_order)
        btnReduceQuantity = findViewById(R.id.btn_reduce_quantity)
        txtProductQuantity = findViewById(R.id.txt_quantity_value)
        btnIncreaseQuantity = findViewById(R.id.btn_inc_quantity)
        setFonts()
    }

    private fun setFonts() {
        setTypefaceView(txtItemName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtSoldBySupplierName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtPriceLastUpdated, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDescription, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtDescriptionHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtDetailsHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSoldByHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtNoImage, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtAddToOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(perUnitPrice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtProductStrikePrice, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtProductQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    private fun displayDetails() {
        if (!StringHelper.isStringNullOrEmpty(supplierLogoURL)) {
            supplierLogo!!.visibility = View.VISIBLE
            //     Picasso.get().load(supplierLogoURL).placeholder(R.drawable.placeholder_all).resize(supplierLogo.getWidth(),supplierLogo.getMaxHeight()).into(supplierLogo);
        }
        val datePerPrice =
            DateHelper.getDateInDateMonthYearFormat(productDetails!!.timePriceUpdated)
        txtPriceLastUpdated!!.text = String.format(
            this.getString(R.string.txt_price_last_updated),
            datePerPrice
        )
    }

    private fun loadPriceInfo(priceList: ProductPriceList) {
        if (!StringHelper.isStringNullOrEmpty(calledFrom) && (calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST || calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_SEARCH_LIST)) {
            if (priceList.dealPrice != null) {
                val itemPrice = priceList.dealPrice!!.displayValue
                if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                    perUnitPrice!!.visibility = View.VISIBLE
                    if (priceList.originalPrice != null && priceList.originalPrice!!.amount != 0.0) {
                        perUnitPrice!!.setTextColor(this.resources.getColor(R.color.pinky_red))
                    } else {
                        perUnitPrice!!.setTextColor(this.resources.getColor(R.color.black))
                    }
                    perUnitPrice!!.text =
                        itemPrice + "/" + returnShortNameValueUnitSizeForQuantity(priceList.unitSize)
                } else {
                    perUnitPrice!!.visibility = View.GONE
                }
                var moq = ""
                if (priceList.moq != null && priceList.moq != 0.0) {
                    moq = priceList.moq.toString() + " " + returnShortNameValueUnitSizeForQuantity(
                        priceList.unitSize
                    )
                }
                if (priceList.originalPrice != null && priceList.originalPrice!!.amount != 0.0) {
                    val string = SpannableString(priceList.originalPrice!!.displayValue)
                    string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                    txtProductStrikePrice!!.visibility = View.VISIBLE
                    txtProductStrikePrice!!.text = "$string • MOQ $moq"
                } else {
                    txtProductStrikePrice!!.visibility = View.VISIBLE
                    txtProductStrikePrice!!.text = "MOQ $moq"
                }
                if (calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_SEARCH_LIST) {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_view_catalogue)
                    txtAddToOrder!!.setOnClickListener {
                        moveToDealsProductScreen(
                            productDetails!!.dealNumber,
                            productDetails
                        )
                    }
                } else {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_order)
                    txtAddToOrder!!.setOnClickListener {
                        filterProducts!!.isSelected = true
                        val item = createSelectedProductObject(filterProducts)
                        //                            item.quantity = (filterProducts.getPriceList().get(0).getMoq());
                        if (item != null) {
                            val key = getKeyProductMap(item.sku, item.unitSize)
                            selectedProductsMap!![key] = item
                            if (item.quantity > 0) {
                                selectedProductsListener!!.onProductSelected(filterProducts)
                                selectedProductsListener!!.onProductSelectedForRecentSearch(
                                    filterProducts!!.productName
                                )
                            }
                        }
                        setSelectedProductLayout()
                    }
                }
            } else {
                if (priceList.originalPrice != null && priceList.originalPrice!!.amount != 0.0) {
                    val itemPrice = priceList.originalPrice!!.displayValue
                    perUnitPrice!!.visibility = View.VISIBLE
                    perUnitPrice!!.setTextColor(this.resources.getColor(R.color.black))
                    perUnitPrice!!.text =
                        itemPrice + "/" + returnShortNameValueUnitSizeForQuantity(priceList.unitSize)
                } else {
                    perUnitPrice!!.visibility = View.GONE
                }
                txtProductStrikePrice!!.visibility = View.GONE
                if (calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_SEARCH_LIST) {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_view_catalogue)
                    txtAddToOrder!!.setOnClickListener {
                        moveToDealsProductScreen(
                            productDetails!!.dealNumber,
                            productDetails
                        )
                    }
                } else {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_order)
                    txtAddToOrder!!.setOnClickListener {
                        filterProducts!!.isSelected = true
                        val item = createSelectedProductObject(filterProducts)
                        item!!.quantity = (filterProducts!!.priceList!![0].moq!!)
                        if (item != null) {
                            val key = getKeyProductMap(item.sku, item.unitSize)
                            selectedProductsMap!![key] = item
                            if (item.quantity > 0) {
                                selectedProductsListener!!.onProductSelected(filterProducts)
                                selectedProductsListener!!.onProductSelectedForRecentSearch(
                                    filterProducts!!.productName
                                )
                            }
                        }
                        setSelectedProductLayout()
                    }
                }
            }
            txtPriceLastUpdated!!.visibility = View.GONE
        } else if (!StringHelper.isStringNullOrEmpty(calledFrom) && (calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST || calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST)) {
            if (priceList.essentialPrice != null) {
                val itemPrice = priceList.essentialPrice!!.displayValue
                if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
                    perUnitPrice!!.visibility = View.VISIBLE
                    if (priceList.originalPrice != null && priceList.originalPrice!!.amount != 0.0) {
                        perUnitPrice!!.setTextColor(this.resources.getColor(R.color.pinky_red))
                    } else {
                        perUnitPrice!!.setTextColor(this.resources.getColor(R.color.black))
                    }
                    perUnitPrice!!.text =
                        itemPrice + "/" + returnShortNameValueUnitSizeForQuantity(priceList.unitSize)
                } else {
                    perUnitPrice!!.visibility = View.GONE
                }
                var moq = ""
                if (priceList.moq != null && priceList.moq != 0.0) {
                    moq = priceList.moq.toString() + "/" + returnShortNameValueUnitSizeForQuantity(
                        priceList.unitSize
                    )
                }
                if (priceList.originalPrice != null && priceList.originalPrice!!.amount != 0.0) {
                    val string = SpannableString(priceList.originalPrice!!.displayValue)
                    string.setSpan(StrikethroughSpan(), 0, string.length, 0)
                    txtProductStrikePrice!!.visibility = View.VISIBLE
                    txtProductStrikePrice!!.text = "$string • MOQ $moq"
                } else {
                    txtProductStrikePrice!!.visibility = View.VISIBLE
                    txtProductStrikePrice!!.text = "MOQ $moq"
                }
                if (calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST) {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_view_catalogue)
                    txtAddToOrder!!.setOnClickListener {
                        moveToEssentialProductScreen(
                            productDetails!!.essentialsId,
                            productDetails
                        )
                    }
                } else {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_order)
                    txtAddToOrder!!.setOnClickListener {
                        filterProducts!!.isSelected = true
                        val item = createSelectedProductObject(filterProducts)
                        item!!.quantity = (filterProducts!!.priceList!![0].moq!!)
                        if (item != null) {
                            val key = getKeyProductMap(item.sku, item.unitSize)
                            selectedProductsMap!![key] = item
                            if (item.quantity > 0) {
                                selectedProductsListener!!.onProductSelected(filterProducts)
                                selectedProductsListener!!.onProductSelectedForRecentSearch(
                                    filterProducts!!.productName
                                )
                            }
                        }
                        setSelectedProductLayout()
                    }
                }
            } else {
                if (priceList.originalPrice != null && priceList.originalPrice!!.amount != 0.0) {
                    val itemPrice = priceList.originalPrice!!.displayValue
                    perUnitPrice!!.visibility = View.VISIBLE
                    perUnitPrice!!.setTextColor(this.resources.getColor(R.color.black))
                    perUnitPrice!!.text =
                        itemPrice + "/" + returnShortNameValueUnitSizeForQuantity(priceList.unitSize)
                } else {
                    perUnitPrice!!.visibility = View.GONE
                }
                txtProductStrikePrice!!.visibility = View.GONE
                if (calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST) {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_view_catalogue)
                    txtAddToOrder!!.setOnClickListener {
                        moveToEssentialProductScreen(
                            productDetails!!.essentialsId,
                            productDetails
                        )
                    }
                } else {
                    txtAddToOrder!!.text = resources.getString(R.string.txt_order)
                    txtAddToOrder!!.setOnClickListener {
                        filterProducts!!.isSelected = true
                        val item = createSelectedProductObject(filterProducts)
                        item!!.quantity = (filterProducts!!.priceList!![0].moq!!)
                        if (item != null) {
                            val key = getKeyProductMap(item.sku, item.unitSize)
                            selectedProductsMap!![key] = item
                            if (item.quantity > 0) {
                                selectedProductsListener!!.onProductSelected(filterProducts)
                                selectedProductsListener!!.onProductSelectedForRecentSearch(
                                    filterProducts!!.productName
                                )
                            }
                        }
                        setSelectedProductLayout()
                    }
                }
            }
            txtPriceLastUpdated!!.visibility = View.GONE
        } else {
            if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST) {
                txtAddToOrder!!.text = resources.getString(R.string.txt_fill_to_par)
            } else {
                txtAddToOrder!!.text = resources.getString(R.string.txt_add_to_order)
            }
            if (priceList.price != null) {
                val price = priceList.price!!.displayValue
                perUnitPrice!!.text =
                    price + "/" + returnShortNameValueUnitSizeForQuantity(priceList.unitSize)
            } else {
                perUnitPrice!!.text = ""
            }
            var moq = ""
            if (priceList.moq != null && priceList.moq != 0.0) {
                moq = priceList.moq.toString() + "/" + returnShortNameValueUnitSizeForQuantity(
                    priceList.unitSize
                )
                txtProductStrikePrice!!.text = "MOQ $moq"
            } else if (priceList.moq != null && priceList.moq == 0.0) {
                moq = priceList.moq.toString() + "/" + returnShortNameValueUnitSizeForQuantity(
                    priceList.unitSize
                )
                txtProductStrikePrice!!.text = "MOQ $moq"
            }
            txtAddToOrder!!.setOnClickListener {
                if (filterProducts != null) {
                    filterProducts!!.isSelected = true
                    val item: Product?
                    if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST) {
                        item = createSelectedProductObjectForBelowPar(filterProducts)
                    } else {
                        item = createSelectedProductObject(filterProducts)
                        item!!.quantity = (filterProducts!!.priceList!![0].moq!!)
                    }
                    //                    Product item = ProductDataHelper.createSelectedProductObject(filterProducts);
//
                    if (item != null) {
                        val key = getKeyProductMap(item.sku, item.unitSize)
                        selectedProductsMap!![key] = item
                        if (item.quantity > 0) {
                            selectedProductsListener!!.onProductSelected(filterProducts)
                            selectedProductsListener!!.onProductSelectedForRecentSearch(
                                filterProducts!!.productName
                            )
                        }
                    }
                    setSelectedProductLayout()
                }
            }
        }
    }

    private fun moveToDealsProductScreen(
        DealNumber: String?,
        productDetailBySupplier: ProductDetailBySupplier?
    ) {
        getActiveDeals(this, DealNumber, object : DealsResponseListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsBaseResponse?) {
                if (dealsBaseResponse != null && dealsBaseResponse.deals != null && dealsBaseResponse.deals!!
                        .size > 0
                ) {
                    val intent =
                        Intent(this@ProductDetailActivity, DealProductListActivity::class.java)
                    intent.putExtra(
                        ZeemartAppConstants.DEALS_LIST,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            dealsBaseResponse.deals!!.get(0)
                        )
                    )
                    intent.putExtra(
                        ZeemartAppConstants.DEALS_NUMBER,
                        dealsBaseResponse.deals!!.get(0).dealNumber
                    )
                    if (productDetailBySupplier != null) intent.putExtra(
                        ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetailBySupplier)
                    )
                    startActivity(intent)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {}
        })
    }

    private fun moveToEssentialProductScreen(
        EssentialId: String?,
        productDetailBySupplier: ProductDetailBySupplier?
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setEssentialsId(EssentialId!!)
        getPaginatedEssentials(this, apiParamsHelper, object : EssentialsResponseListener {
            override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                if (essentialsList != null && essentialsList.essentials != null && essentialsList.essentials!!.size > 0) {
                    val intent = Intent(
                        this@ProductDetailActivity,
                        EssentialsProductListActivity::class.java
                    )
                    intent.putExtra(
                        ZeemartAppConstants.ESSENTIALS_LIST,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            essentialsList.essentials!![0]
                        )
                    )
                    intent.putExtra(
                        ZeemartAppConstants.ESSENTIALS_ID,
                        essentialsList.essentials!![0].essentialsId
                    )
                    if (productDetailBySupplier != null) intent.putExtra(
                        ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetailBySupplier)
                    )
                    intent.putExtra(
                        ZeemartAppConstants.CALLED_FROM,
                        ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST
                    )
                    startActivity(intent)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {}
        })
    }

    private fun displayDataFromProductsListing() {
        nestedScrollView!!.visibility = View.VISIBLE
        viewPager!!.visibility = View.VISIBLE
        txtItemName!!.text = productDetails!!.productName + ""
        if (!StringHelper.isStringNullOrEmpty(supplierName)) {
            txtSupplierName!!.text = String.format(supplierName!!)
            txtSoldBySupplierName!!.text = supplierName
        }
        if (!StringHelper.isStringNullOrEmpty(supplierLogoURL)) {
            supplierLogo!!.visibility = View.VISIBLE
            //  Picasso.get().load(supplierLogoURL).placeholder(R.drawable.placeholder_all).resize(supplierLogo.getWidth(),supplierLogo.getMaxHeight()).into(supplierLogo);
        }
        val datePerPrice =
            DateHelper.getDateInDateMonthYearFormat(productDetails!!.timePriceUpdated)
        txtPriceLastUpdated!!.text = String.format(
            this.getString(R.string.txt_price_last_updated),
            datePerPrice
        )
        //txtBrandName.setVisibility(View.GONE);
        //txtBrandTag.setVisibility(View.GONE);
        lstDetails!!.visibility = View.GONE
        lstPrice!!.visibility = View.GONE
        lstPrice!!.layoutManager = LinearLayoutManager(this)
        lstPrice!!.adapter = ProductDetailsPriceListAdapter(
            this,
            productPriceList!!,
            productDetails!!.stocks,
            calledFrom!!,
            productDetails!!.timePriceUpdated,
            supplierDetails
        )
    }

    override fun onBackPressed() {
        onStatusChanged(false)
        overridePendingTransition(R.anim.hold_anim, R.anim.slide_to_bottom)
    }

    private fun onStatusChanged(isFromSlide: Boolean) {
        if (isFavouriteSelected) {
            val intent: Intent
            if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST) {
                finish()
            } else if (!StringHelper.isStringNullOrEmpty(calledFrom) && calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST) {
                intent =
                    Intent(this@ProductDetailActivity, EssentialsProductListActivity::class.java)
                intent.putExtra(
                    ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetails)
                )
                setResult(OrderDetailsActivity.IS_STATUS_CHANGED, intent)
            } else if (!StringHelper.isStringNullOrEmpty(calledFrom)
                && calledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST || calledFrom
                        == ZeemartAppConstants.CALLED_FROM_DEALS_SEARCH_LIST || calledFrom
                == ZeemartAppConstants.CALLED_FROM_SUPPLIER_SEARCH_LIST) {
                intent = Intent(this@ProductDetailActivity, SearchForNewOrder::class.java)
                intent.putExtra(
                    ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetails)
                )
                setResult(OrderDetailsActivity.IS_STATUS_CHANGED, intent)
            } else {
                intent = Intent(this@ProductDetailActivity, AddToOrderActivity::class.java)
                intent.putExtra(
                    ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                    ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetails)
                )
                setResult(OrderDetailsActivity.IS_STATUS_CHANGED, intent)
            }
        }
        val selectedProductJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(filterProducts)
        val i = Intent()
        i.putExtra(
            ZeemartAppConstants.SELECTED_MAPPING,
            ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProductsMap)
        )
        i.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, selectedProductJson)
        setResult(RESULT_OK, i)
        if (!isFromSlide) finish()
    }

    fun checkSelectedProduct() {
        displayAddToOrderLayout()
        if (filterProducts != null && filterProducts!!.isSelected) {
            setSelectedProductLayout()
        }
    }

    fun displayAddToOrderLayout() {
        lytProductQuantity!!.visibility = View.GONE

//        lytQuantitySelection.setBackgroundColor(this.getResources().getColor(R.color.faint_white));
        lytAddToOrderAndUOM!!.visibility = View.VISIBLE

        //  txtAddToOrder.setTextColor(this.getResources().getColor(R.color.text_blue));
    }

    fun setSelectedProductLayout() {
//        lytQuantitySelection.setBackgroundColor(this.getResources().getColor(R.color.white));
        //show the quantity layout with pre set quantity
        lytProductQuantity!!.visibility = View.VISIBLE
        lytAddToOrderAndUOM!!.visibility = View.GONE
        if (selectedProductsMap != null && selectedProductsMap!!.size > 0) {
            val key =
                getKeyProductMap(filterProducts!!.sku, filterProducts!!.priceList!![0].unitSize!!)
            val selectedProduct = selectedProductsMap!![key]
            txtProductQuantity!!.text = selectedProduct!!.quantityDisplayValue + " "
            txtProductQuantity!!.append(
                changeQuantityUOMTextFontSize(
                    returnShortNameValueUnitSizeForQuantity(
                        selectedProduct.unitSize
                    ), this
                )
            )
        }
    }

    override fun onDataChange(product: Product?) {
//        String key = ProductDataHelper.getKeyProductMap(filterProducts.get(position).getSku(), filterProducts.get(position).getPriceList().get(0).getUnitSize());
        filterProducts!!.priceList!![0].moq = product?.quantity
        txtProductQuantity!!.text = product?.quantityDisplayValue + " "
        txtProductQuantity!!.append(
            changeQuantityUOMTextFontSize(
                returnShortNameValueUnitSizeForQuantity(
                    product?.unitSize
                ), this
            )
        )
    }
}