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
import zeemart.asia.buyers.deals.DealDetailsActivity
import zeemart.asia.buyers.deals.ReviewDealsActivity
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper.DealsAndEssentialsProductsScrollCallback
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.Settings
import zeemart.asia.buyers.models.marketlist.Settings.Gst
import zeemart.asia.buyers.models.order.*
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.models.orderimportimportimport.DealsProductsPaginated
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderHelper.DealsProductsListener
import java.util.*

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class DealsOnBoardingProductListActivity : AppCompatActivity() {
    private lateinit var imgBack: ImageView
    private lateinit var edtSearch: EditText
    private lateinit var txtDealNameHeader: TextView
    private lateinit var lytOrderSummary: RelativeLayout
    private var txtTotalItems: TextView? = null
    private var imgDeals: ImageView? = null
    private lateinit var lytMinCart: RelativeLayout
    private lateinit var txtMinimumCartValue: TextView
    private lateinit var txtPayUpFront: TextView
    private lateinit var lstProductList: RecyclerView
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var edtSearchClear: ImageView
    private var lstDeals: DealsBaseResponse.Deals? = null
    private lateinit var products: MutableList<ProductDetailBySupplier>
    private var productListAdapter: SupplierProductListAdapter? = null
    private var cartItemCount = 0
    private var selectedProductList: List<Product>? = null
    private var selectedProductMap: Map<String, Product> = HashMap()
    private var showProductListCalledFrom: String? = null
    private var txtAbout: TextView? = null
    private var productList: List<DealsProductsPaginated.DealProducts>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var productDetailBySupplier: ProductDetailBySupplier? = null
    private var viewOrderScrollHelper: DealsAndEssentialsProductsListScrollHelper? = null
    private lateinit var lytHiddenContent: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deals_product_list)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.DEALS_LIST)) {
                lstDeals = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.DEALS_LIST),
                    DealsBaseResponse.Deals::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.PRODUCT_DETAILS_JSON)) {
                productDetailBySupplier = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.PRODUCT_DETAILS_JSON),
                    ProductDetailBySupplier::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
                val json = bundle.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                val products =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(json, Array<Product>::class.java)
                selectedProductList = listOf(*products)
                //create a selected product map
                if (selectedProductList != null && selectedProductList!!.isNotEmpty()) {
                    selectedProductMap =
                        ProductDataHelper.createSelectedProductMap(selectedProductList)!!
                }
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                showProductListCalledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
        }
        imgBack = findViewById(R.id.products_back_btn)
        imgBack.setOnClickListener(View.OnClickListener { createExitDialog() })
        edtSearch = findViewById(R.id.edit_search)
        txtDealNameHeader = findViewById(R.id.txt_product_list_supplier_heading)
        lytOrderSummary = findViewById(R.id.lyt_review_add_to_order)
        txtTotalItems = lytOrderSummary.findViewById(R.id.txt_review_total_items_in_cart)
        imgDeals = findViewById(R.id.img_deals)
        lytMinCart = findViewById(R.id.lyt_min_amount)
        lytMinCart.setOnClickListener(View.OnClickListener {
            val intent =
                Intent(this@DealsOnBoardingProductListActivity, DealDetailsActivity::class.java)
            val dealsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstDeals)
            intent.putExtra(ZeemartAppConstants.DEALS_LIST, dealsJson)
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
                CommonMethods.hideKeyboard(this@DealsOnBoardingProductListActivity)
            }
            false
        })
        edtSearchClear.setOnClickListener(View.OnClickListener { edtSearch.setText("") })
        if (lstDeals != null) {
            txtDealNameHeader.setText(lstDeals!!.title)
            if (lstDeals!!.deliveryFeePolicy != null) {
                val cartValue = String.format(
                    resources.getString(R.string.txt_min_cart_amount),
                    lstDeals!!.deliveryFeePolicy?.minOrder?.displayValue
                )
                txtMinimumCartValue.setText(cartValue)
            } else if (lstDeals!!.orderSettings != null && lstDeals!!.orderSettings?.deliveryFeePolicy != null) {
                val cartValue = String.format(
                    resources.getString(R.string.txt_min_cart_amount),
                    lstDeals!!.orderSettings?.deliveryFeePolicy?.minOrder?.displayValue
                )
                txtMinimumCartValue.setText(cartValue)
            }
            if (!StringHelper.isStringNullOrEmpty(lstDeals!!.paymentDescription)) {
                if (lstDeals!!.paymentDescription == ZeemartAppConstants.PAY_UP_FRONT) {
                    txtPayUpFront.setText(resources.getString(R.string.txt_pay_upfront))
                } else if (lstDeals!!.paymentDescription == ZeemartAppConstants.PAY_AFTER_DELIVERY) {
                    txtPayUpFront.setText(resources.getString(R.string.txt_pay_after_delivery))
                }
            }
            if (lstDeals!!.carouselBanners != null && !StringHelper.isStringNullOrEmpty(
                    lstDeals!!.carouselBanners!![0]
                )
            ) Picasso.get().load(lstDeals!!.carouselBanners!![0]).fit().into(imgDeals)
        }
        lytOrderSummary.setOnClickListener(View.OnClickListener { moveToReviewOrderScreen() })
        setFont()
        callDealsDetailsAPI()
        setLayoutReviewButton(cartItemCount)
        lytHiddenContent = findViewById(R.id.lyt_hidden_content)
        viewOrderScrollHelper = DealsAndEssentialsProductsListScrollHelper(
            this@DealsOnBoardingProductListActivity,
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
                        this@DealsOnBoardingProductListActivity,
                        ReviewDealsActivity::class.java
                    )
                    val newOrder = Orders()
                    val supplier = Supplier()
                    supplier.supplierId = lstDeals!!.supplier?.supplierId
                    supplier.supplierName = lstDeals!!.supplier?.supplierName
                    supplier.logoURL = lstDeals!!.supplier?.logoURL
                    val supplierGst = Gst()
                    supplierGst.percent = lstDeals!!.gstPercent
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
                        lstDeals!!.deliveryDates
                    )
                    newIntent.putExtra(ZeemartAppConstants.DELIVERY_DATES, deliveriesJson)
                    newIntent.putExtra(ZeemartAppConstants.DEAL_NUMBER, lstDeals!!.dealNumber)
                    newIntent.putExtra(ZeemartAppConstants.DEAL_NAME, lstDeals!!.title)
                    if (lstDeals != null && lstDeals!!.deliveryFeePolicy != null) {
                        val minimumOrderValueJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(
                            lstDeals!!.deliveryFeePolicy?.minOrder
                        )
                        newIntent.putExtra(
                            ZeemartAppConstants.DEAL_MINIMUM_ORDER_VALUE_DETAILS,
                            minimumOrderValueJson
                        )
                    }
                    val dealsList = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstDeals)
                    newIntent.putExtra(ZeemartAppConstants.DEALS_LIST, dealsList)
                    startActivityForResult(newIntent, RequestCode.DealsProductListActivity)
                }
            } else {
                getToastRed(getString(R.string.txt_no_products_selected))
            }
        } else {
            getToastRed(getString(R.string.txt_no_products_selected))
        }
    }

    private fun callDealsDetailsAPI() {
        val apiParamsHelper = ApiParamsHelper()
        if (lstDeals != null) apiParamsHelper.setDealNumber(lstDeals!!.dealNumber!!)
        OrderHelper.getDealOnBoardingProducts(
            this,
            apiParamsHelper,
            object : DealsProductsListener {
                override fun onSuccessResponse(dealsBaseResponse: DealsProductsPaginated?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                    if (dealsBaseResponse != null && dealsBaseResponse.data != null && dealsBaseResponse.data!!.dealProducts != null) {
                        productList = ArrayList(dealsBaseResponse.data!!.dealProducts)
                        addProductsList()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.DealsProductListActivity) {
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
    }

    private fun setFont() {
        setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDealNameHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
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
                            val productUom = ProductDataHelper.getDealProducts(
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
                    val productUom = ProductDataHelper.getDealProducts(
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

    private fun updateProductsAdapter() {
        if (products != null && products!!.size > 0) {
            if (edtSearch!!.text.toString().length == 0) {
                productListAdapter = SupplierProductListAdapter(
                    true,
                    this,
                    products,
                    "",
                    ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST,
                    lstDeals!!.supplier?.supplierName!!,
                    lstDeals!!.supplier?.logoURL!!,
                    lstDeals!!.supplier?.supplierId!!,
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
                        if (productDetailBySupplier!!.sku == products!![i].sku && productDetailBySupplier!!.priceList!![0].unitSize == products!![i].priceList!![0].unitSize) {
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
                        if (products[i].productName?.lowercase(Locale.getDefault())!!.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            ) || products[i].productCustomName != null && products[i].productCustomName?.lowercase(
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
            ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST,
            lstDeals!!.supplier?.supplierName!!,
            lstDeals!!.supplier?.logoURL!!,
            lstDeals!!.supplier?.supplierId!!,
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
            lytOrderSummary!!.background =
                resources.getDrawable(R.drawable.blue_rounded_corner)
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
            if (selectedProducts != null && selectedProducts.size > 0) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.txt_leave_this_page)
                builder.setMessage(R.string.txt_deal_items_not_saved)
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

    override fun onBackPressed() {
        createExitDialog()
    }
}