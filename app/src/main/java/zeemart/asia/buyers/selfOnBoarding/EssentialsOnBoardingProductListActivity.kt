package zeemart.asia.buyers.selfOnBoarding

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SupplierProductListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants.RequestCode
import zeemart.asia.buyers.essentials.EssentialSupplierDetailActivity
import zeemart.asia.buyers.essentials.ReviewEssentialsActivity
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper.DealsAndEssentialsProductsScrollCallback
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.Settings
import zeemart.asia.buyers.models.marketlist.Settings.Gst
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimportimport.EssentialProductsPaginated
import zeemart.asia.buyers.network.EssentialsApi
import zeemart.asia.buyers.network.EssentialsApi.EssentialsProductsOnBoardingResponseListener
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.createorders.SearchForNewOrder
import java.util.*

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class EssentialsOnBoardingProductListActivity : AppCompatActivity() {
    private lateinit var imgBack: ImageView
    private lateinit var edtSearch: EditText
    private lateinit var txtEssentialNameHeader: TextView
    private lateinit var lytOrderSummary: RelativeLayout
    private var txtTotalItems: TextView? = null
    private var imgEssentials: ImageView? = null
    private lateinit var lytMinCart: RelativeLayout
    private var txtAbout: TextView? = null
    private lateinit var txtMinimumCartValue: TextView
    private lateinit var txtPayUpFront: TextView
    private lateinit var lstProductList: RecyclerView
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var edtSearchClear: ImageView
    private var lstEssentials: Essential? = null
    private lateinit var products: MutableList<ProductDetailBySupplier>
    private var productListAdapter: SupplierProductListAdapter? = null
    private var cartItemCount = 0
    private var selectedProductList: List<Product>? = null
    private var selectedProductMap: Map<String, Product> = HashMap()
    private var showProductListCalledFrom: String? = null
    private var productList: List<EssentialProductsPaginated.EssentialsProducts>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var productDetailBySupplier: ProductDetailBySupplier? = null
    private var viewOrderScrollHelper: DealsAndEssentialsProductsListScrollHelper? = null
    private lateinit var lytHiddenContent: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_essentials_on_boarding_product_list)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ESSENTIALS_LIST)) {
                lstEssentials = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ESSENTIALS_LIST), Essential::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
                val json = bundle.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                val products =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(json, Array<Product>::class.java)
                selectedProductList = Arrays.asList(*products)
                //create a selected product map
                if (selectedProductList != null && selectedProductList!!.size > 0) {
                    selectedProductMap =
                        ProductDataHelper.createSelectedProductMap(selectedProductList)!!
                }
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                showProductListCalledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
            if (bundle.containsKey(ZeemartAppConstants.PRODUCT_DETAILS_JSON)) {
                productDetailBySupplier = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.PRODUCT_DETAILS_JSON),
                    ProductDetailBySupplier::class.java
                )
            }
        }
        imgBack = findViewById(R.id.products_back_btn)
        imgBack.setOnClickListener(View.OnClickListener { createExitDialog() })
        edtSearch = findViewById(R.id.edit_search)
        txtEssentialNameHeader = findViewById(R.id.txt_product_list_supplier_heading)
        lytOrderSummary = findViewById(R.id.lyt_review_add_to_order)
        txtTotalItems = lytOrderSummary.findViewById(R.id.txt_review_total_items_in_cart)
        imgEssentials = findViewById(R.id.img_deals)
        lytMinCart = findViewById(R.id.lyt_min_amount)
        lytMinCart.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@EssentialsOnBoardingProductListActivity,
                EssentialSupplierDetailActivity::class.java
            )
            val essentialsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstEssentials)
            intent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, essentialsJson)
            startActivity(intent)
        })
        txtMinimumCartValue = findViewById(R.id.txt_minimum_cart_value)
        txtPayUpFront = findViewById(R.id.txt_pay_up_front)
        txtAbout = findViewById(R.id.txt_about)
        lstProductList = findViewById(R.id.lstProductList)
        lstProductList.setNestedScrollingEnabled(false)
        linearLayoutManager = LinearLayoutManager(this)
        lstProductList.setLayoutManager(linearLayoutManager)
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_product_list)
        edtSearch = findViewById(R.id.edit_search)
        edtSearchClear = findViewById(R.id.edit_search_clear)
        edtSearch.setHint(resources.getString(R.string.content_product_list_new_order_edt_search_hint))
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText = edtSearch.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear.setVisibility(View.VISIBLE)
                } else {
                    edtSearchClear.setVisibility(View.GONE)
                }
                updateProductsAdapter()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        edtSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                CommonMethods.hideKeyboard(this@EssentialsOnBoardingProductListActivity)
            }
            false
        })
        edtSearchClear.setOnClickListener(View.OnClickListener { edtSearch.setText("") })
        if (lstEssentials != null) {
            txtEssentialNameHeader.setText(lstEssentials!!.supplier?.supplierName)
            if (lstEssentials!!.deliveryFeePolicy != null) {
                val cartValue = String.format(
                    resources.getString(R.string.txt_min_cart_amount),
                    lstEssentials!!.deliveryFeePolicy?.minOrder?.displayValue
                )
                txtMinimumCartValue.setText(cartValue)
            } else if (lstEssentials!!.orderSettings != null && lstEssentials!!.orderSettings?.deliveryFeePolicy != null) {
                val cartValue = String.format(
                    resources.getString(R.string.txt_min_cart_amount),
                    lstEssentials!!.orderSettings?.deliveryFeePolicy?.minOrder?.displayValue
                )
                txtMinimumCartValue.setText(cartValue)
            }
            if (!StringHelper.isStringNullOrEmpty(lstEssentials!!.paymentTermDescription)) {
                if (lstEssentials!!.paymentTermDescription == ZeemartAppConstants.PAY_UP_FRONT) {
                    txtPayUpFront.setText(resources.getString(R.string.txt_pay_upfront))
                } else if (lstEssentials!!.paymentTermDescription == ZeemartAppConstants.PAY_AFTER_DELIVERY) {
                    txtPayUpFront.setText(resources.getString(R.string.txt_pay_after_delivery))
                }
            } else {
                if (lstEssentials!!.paymentTermsDescription == ZeemartAppConstants.PAY_UP_FRONT) {
                    txtPayUpFront.setText(resources.getString(R.string.txt_pay_upfront))
                } else if (lstEssentials!!.paymentTermsDescription == ZeemartAppConstants.PAY_AFTER_DELIVERY) {
                    txtPayUpFront.setText(resources.getString(R.string.txt_pay_after_delivery))
                }
            }
            if (!StringHelper.isStringNullOrEmpty(lstEssentials!!.landingBanners!![0])) Picasso.get()
                .load(
                    lstEssentials!!.landingBanners!![0]
                ).fit().into(imgEssentials)
        }
        lytOrderSummary.setOnClickListener(View.OnClickListener { moveToReviewOrderScreen() })
        setFont()
        callEssentialsProductsListAPI()
        setLayoutReviewButton(cartItemCount)
        lytHiddenContent = findViewById(R.id.lyt_hidden_content)
        viewOrderScrollHelper = DealsAndEssentialsProductsListScrollHelper(
            this@EssentialsOnBoardingProductListActivity,
            lstProductList,
            linearLayoutManager!!,
            object : DealsAndEssentialsProductsScrollCallback {
                override fun isHideImage(isHide: Boolean) {
                    val animate =
                        TranslateAnimation(0f, 0f, -lytHiddenContent.getHeight().toFloat(), 0f)
                    animate.duration = 500
                    animate.fillAfter = true
                    if (isHide) {
                        lytHiddenContent.setAnimation(null)
                        lytHiddenContent.setVisibility(View.GONE)
                    } else {
                        lytHiddenContent.startAnimation(animate)
                        lytHiddenContent.setVisibility(View.VISIBLE)
                    }
                }

                override fun loadMore() {}
            })
        viewOrderScrollHelper!!.setOnScrollListener()
    }

    private fun callEssentialsProductsListAPI() {
        val apiParamsHelper = ApiParamsHelper()
        if (lstEssentials != null) apiParamsHelper.setEssentialsId(lstEssentials!!.essentialsId!!)
        apiParamsHelper.setSupplierId(lstEssentials!!.supplier?.supplierId!!)
        EssentialsApi.getPaginatedEssentialsOnBoardingProducts(
            this,
            apiParamsHelper,
            object : EssentialsProductsOnBoardingResponseListener {
                override fun onSuccessResponse(essentialsList: EssentialProductsPaginated?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    if (essentialsList != null && essentialsList.data != null && essentialsList.data != null && essentialsList.data!!.essentialProducts != null && essentialsList.data!!.essentialProducts!!.size > 0) {
                        productList = ArrayList(essentialsList.data!!.essentialProducts)
                        addProductsList()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    private fun moveToReviewOrderScreen() {
        if (products != null && products!!.size != 0) {
            val selectedProducts = ArrayList<Product>()
            for (i in products!!.indices) {
                if (products!![i].isSelected) {
                    var item: Product?
                    val key = ProductDataHelper.getKeyProductMap(
                        products!![i].sku,
                        products!![i].priceList!![0].unitSize!!
                    )
                    item = if (selectedProductMap.containsKey(key)) {
                        selectedProductMap[products!![i].sku + products!![i].priceList!![0].unitSize]
                    } else {
                        ProductDataHelper.createSelectedProductObject(products!![i])
                    }
                    if (item != null) {
                        selectedProducts.add(item)
                    }
                }
            }
            if (selectedProducts.size > 0) {
                if (!StringHelper.isStringNullOrEmpty(showProductListCalledFrom) && showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
                    val intent = intent
                    val selectedProductJson =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProducts)
                    intent.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, selectedProductJson)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    val newIntent = Intent(
                        this@EssentialsOnBoardingProductListActivity,
                        ReviewEssentialsActivity::class.java
                    )
                    val newOrder = Orders()
                    val supplier = Supplier()
                    supplier.supplierId = lstEssentials!!.supplier?.supplierId
                    supplier.supplierName = lstEssentials!!.supplier?.supplierName
                    supplier.logoURL = lstEssentials!!.supplier?.logoURL
                    val supplierGst = Gst()
                    supplierGst.percent = lstEssentials!!.gstPercent
                    val supplierSettings = Settings()
                    supplierSettings.gst = supplierGst
                    supplier.settings = supplierSettings
                    newOrder.supplier = supplier
                    newOrder.products = selectedProducts
                    val outlet = Outlet()
                    outlet.outletName = SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, "")
                    outlet.outletId = SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")
                    newOrder.outlet = outlet
                    val createdByUserDetails = UserDetails()
                    createdByUserDetails.firstName = SharedPref.read(SharedPref.USER_FIRST_NAME, "")
                    createdByUserDetails.id = SharedPref.read(SharedPref.USER_ID, "")
                    createdByUserDetails.imageURL = SharedPref.read(SharedPref.USER_IMAGE_URL, "")
                    createdByUserDetails.lastName = SharedPref.read(SharedPref.USER_LAST_NAME, "")
                    createdByUserDetails.phone = SharedPref.read(SharedPref.USER_PHONE_NUMBER, "")
                    newOrder.createdBy = createdByUserDetails
                    val orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(newOrder)
                    newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson)
                    val supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(newOrder.supplier)
                    val outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(newOrder.outlet)
                    newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson)
                    newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson)
                    val deliveriesJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                        lstEssentials!!.deliveryDates
                    )
                    newIntent.putExtra(ZeemartAppConstants.DELIVERY_DATES, deliveriesJson)
                    newIntent.putExtra(
                        ZeemartAppConstants.ESSENTIAL_ID,
                        lstEssentials!!.essentialsId
                    )
                    if (lstEssentials!!.deliveryFeePolicy != null) {
                        val minimumOrderValueJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            lstEssentials!!.deliveryFeePolicy?.minOrder
                        )
                        newIntent.putExtra(
                            ZeemartAppConstants.ESSENTIAL_MINIMUM_ORDER_VALUE_DETAILS,
                            minimumOrderValueJson
                        )
                    }
                    val essentialList = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstEssentials)
                    newIntent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, essentialList)
                    startActivityForResult(newIntent, RequestCode.EssentialsProductListActivity)
                }
            } else {
                getToastRed(getString(R.string.txt_no_products_selected))
            }
        } else {
            getToastRed(getString(R.string.txt_no_products_selected))
        }
    }

    private fun setFont() {
        setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtEssentialNameHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtTotalItems, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtMinimumCartValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtPayUpFront, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtAbout, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    private fun addProductsList() {
        if (productList != null && productList!!.size > 0) {
            products = ArrayList()
            for (i in productList!!.indices) {
                if (productList!![i].unitPrices?.size!! > 1) {
                    for (j in productList!![i].unitPrices?.indices!!) {
                        if (productList!![i].unitPrices!![j].status == "ACTIVE") {
                            val productUom = ProductDataHelper.getEssentialProducts(
                                productList!![i]
                            )
                            val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                            productPriceLists.add(productList!![i].unitPrices!![j])
                            productUom.priceList = productPriceLists
                            productUom.isHasMultipleUom = true
                            products.add(productUom)
                        }
                    }
                } else {
                    val productUom = ProductDataHelper.getEssentialProducts(
                        productList!![i]
                    )
                    val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                    productPriceLists.add(productList!![i].unitPrices!![0])
                    productUom.priceList = productPriceLists
                    productUom.isHasMultipleUom = true
                    products.add(productUom)
                }
            }
            if (selectedProductList != null && selectedProductList!!.size > 0) {
                for (i in selectedProductList!!.indices) {
                    for (j in products.indices) {
                        if (products.get(j).sku == selectedProductList!![i].sku && products.get(j).priceList!![0].unitSize == selectedProductList!![i].unitSize) {
                            products.get(j).isSelected = true
                            cartItemCount = cartItemCount + 1
                            setLayoutReviewButton(cartItemCount)
                            break
                        }
                    }
                }
            }
            updateProductsAdapter()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.EssentialsProductListActivity) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.hasExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
                        val json =
                            data.extras!!.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                        val products = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            json,
                            Array<Product>::class.java
                        )
                        selectedProductList = Arrays.asList(*products)
                        //create a selected product map
                        if (selectedProductList != null && selectedProductList!!.size > 0) {
                            selectedProductMap =
                                ProductDataHelper.createSelectedProductMap(selectedProductList)!!
                        }
                        cartItemCount = 0
                        setLayoutReviewButton(cartItemCount)
                        addProductsList()
                    }
                }
            }
        }
        if (resultCode == OrderDetailsActivity.IS_STATUS_CHANGED) {
            val bundle = data!!.extras
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.PRODUCT_DETAILS_JSON)) {
                    val productDetailJson =
                        bundle.getString(ZeemartAppConstants.PRODUCT_DETAILS_JSON)
                    val productDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        productDetailJson,
                        ProductDetailBySupplier::class.java
                    )
                    productDetails?.let { refreshParticularProduct(it) }
                }
            }
        }
    }

    private fun updateProductsAdapter() {
        if (products != null && products!!.size > 0) {
            if (edtSearch!!.text.toString().length == 0) {
                productListAdapter = SupplierProductListAdapter(
                    true,
                    this,
                    products,
                    "",
                    ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST,
                    lstEssentials!!.supplier?.supplierName!!,
                    lstEssentials!!.supplier?.logoURL!!,
                    lstEssentials!!.supplier?.supplierId!!,
                    null,
                    selectedProductMap as MutableMap<String, Product>?,
                    null,
                    productDetailBySupplier,
                    true,
                    object : SupplierProductListAdapter.SelectedProductsListener {
                        override fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?) {
                            cartItemCount = cartItemCount + 1
                            setLayoutReviewButton(cartItemCount)
                        }

                        override fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?) {
                            cartItemCount = cartItemCount - 1
                            setLayoutReviewButton(cartItemCount)
                        }

                        override fun onProductSelectedForRecentSearch(productName: String?) {}
                    })
                lstProductList!!.adapter = productListAdapter
                if (productDetailBySupplier != null) {
                    for (i in products!!.indices) {
                        if (productDetailBySupplier!!.sku == products!![i].sku && !StringHelper.isStringNullOrEmpty(
                                products!![i].priceList!![0].unitSize
                            ) && productDetailBySupplier!!.priceList!![0].unitSize == products!![i].priceList!![0].unitSize
                        ) {
                            lstProductList!!.scrollToPosition(i)
                        }
                    }
                }
            } else {
                filterProductsOnSearchedText(products)
            }
        }
    }

    private fun filterProductsOnSearchedText(products: List<ProductDetailBySupplier>?) {
        val searchedString = edtSearch!!.text.toString()
        val searchStringArray =
            searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val filteredList: MutableList<ProductDetailBySupplier> = ArrayList()
        if (products != null) {
            for (i in products.indices) {
                var searchStringFound = false
                for (j in searchStringArray.indices) {
                    searchStringFound =
                        if (products[i].productName?.lowercase(Locale.getDefault())?.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            )!! || products[i].productCustomName != null && products[i].productCustomName?.lowercase(
                                Locale.getDefault()
                            )?.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            )!!
                        ) {
                            true
                        } else {
                            false
                        }
                }
                if (searchStringFound) {
                    if (!filteredList.contains(products[i])) filteredList.add(products[i])
                }
            }
            ProductDetailBySupplier.sortByProductName(filteredList)
        }
        productListAdapter = SupplierProductListAdapter(
            true,
            this,
            filteredList,
            "",
            ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST,
            lstEssentials!!.supplier?.supplierName!!,
            lstEssentials!!.supplier?.logoURL!!,
            lstEssentials!!.supplier?.supplierId!!,
            null,
            selectedProductMap as MutableMap<String, Product>?,
            searchStringArray,
            productDetailBySupplier,
            true,
            object : SupplierProductListAdapter.SelectedProductsListener {
                override fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?) {
                    cartItemCount = cartItemCount + 1
                    setLayoutReviewButton(cartItemCount)
                }

                override fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?) {
                    cartItemCount = cartItemCount - 1
                    setLayoutReviewButton(cartItemCount)
                }

                override fun onProductSelectedForRecentSearch(productName: String?) {}
            })
        lstProductList!!.adapter = productListAdapter
    }

    private fun setLayoutReviewButton(items: Int) {
        if (items > 0) {
//            lytOrderSummary.setVisibility(View.VISIBLE);
//            lytOrderSummary.setClickable(true);
            /*REPLACE THIS AFTER*/
            lytOrderSummary!!.visibility = View.GONE
            lytOrderSummary!!.isClickable = false
            lytOrderSummary!!.background = resources.getDrawable(R.drawable.blue_rounded_corner)
            if (!StringHelper.isStringNullOrEmpty(showProductListCalledFrom) && showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
                txtTotalItems!!.text = resources.getString(R.string.txt_done)
            } else {
                if (items == 1) {
                    txtTotalItems!!.text = String.format(
                        resources.getString(R.string.txt_review_order_cart_item),
                        items
                    )
                } else {
                    txtTotalItems!!.text = String.format(
                        resources.getString(R.string.txt_review_order_cart_items),
                        items
                    )
                }
            }
        } else {
            //            lytOrderSummary.setVisibility(View.VISIBLE);
//            lytOrderSummary.setClickable(true);
            /*REPLACE THIS AFTER*/
            lytOrderSummary!!.visibility = View.GONE
            lytOrderSummary!!.isClickable = false
            lytOrderSummary!!.background =
                resources.getDrawable(R.drawable.dark_grey_rounded_corner)
            if (!StringHelper.isStringNullOrEmpty(showProductListCalledFrom) && showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
                txtTotalItems!!.text = resources.getString(R.string.txt_done)
            } else {
                txtTotalItems!!.text = resources.getString(R.string.txt_no_items_in_cart)
            }
        }
    }

    private fun createExitDialog() {
        if (!StringHelper.isStringNullOrEmpty(showProductListCalledFrom) && showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
            finish()
        } else {
            val selectedProducts = ArrayList<Product>()
            if (products != null && products!!.size > 0) {
                for (i in products!!.indices) {
                    if (products!![i].isSelected) {
                        var item: Product?
                        val key = ProductDataHelper.getKeyProductMap(
                            products!![i].sku,
                            products!![i].priceList!![0].unitSize!!
                        )
                        item = if (selectedProductMap.containsKey(key)) {
                            selectedProductMap[products!![i].sku + products!![i].priceList!![0].unitSize]
                        } else {
                            ProductDataHelper.createSelectedProductObject(products!![i])
                        }
                        if (item != null) {
                            selectedProducts.add(item)
                        }
                    }
                }
            }
            if (!StringHelper.isStringNullOrEmpty(showProductListCalledFrom) && showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST) {
                val newIntent = Intent(
                    this@EssentialsOnBoardingProductListActivity,
                    SearchForNewOrder::class.java
                )
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_SUPPLIER_ADD_TO_ORDER
                )
                setResult(RESULT_OK, newIntent)
            }
            if (selectedProducts != null && selectedProducts.size > 0) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.txt_leave_this_page)
                builder.setMessage(R.string.txt_essential_items_not_saved)
                builder.setPositiveButton(resources.getString(R.string.txt_stay_here)) { dialog, which -> dialog.dismiss() }
                builder.setNegativeButton(resources.getString(R.string.txt_leave)) { dialog, which -> finish() }
                val d = builder.create()
                d.show()
                val negativeButton = d.getButton(DialogInterface.BUTTON_NEGATIVE)
                negativeButton.setTextColor(resources.getColor(R.color.pinky_red))
                val positiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE)
                positiveButton.setTextColor(resources.getColor(R.color.text_blue))
            } else {
                finish()
            }
        }
    }

    private fun refreshParticularProduct(productDetailBySupplier: ProductDetailBySupplier) {
        if (products != null) for (i in products!!.indices) {
            if (productDetailBySupplier.sku.equals(products!![i].sku, ignoreCase = true)) {
                products!![i].isFavourite = productDetailBySupplier.isFavourite
            }
        }
        if (lstProductList!!.adapter != null) lstProductList!!.adapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        createExitDialog()
    }
}