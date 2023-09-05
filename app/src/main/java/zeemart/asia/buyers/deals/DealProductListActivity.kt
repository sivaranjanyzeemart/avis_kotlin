package zeemart.asia.buyers.deals

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
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter.ProductListSelectedFilterCategoriesItemClickListeneremptydata
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter.ProductListSelectedFilterSupplierItemClickListener
import zeemart.asia.buyers.adapter.SupplierProductListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants.RequestCode
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DialogHelper.ShowFilterTagsDialog
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper.DealsAndEssentialsProductsScrollCallback
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.Settings
import zeemart.asia.buyers.models.marketlist.Settings.Gst
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.models.orderimportimportimport.DealsProductsPaginated
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderHelper.DealsProductsListener
import zeemart.asia.buyers.network.OrderHelper.DealsResponseListener
import java.util.*

/**
 * Created by RajPrudhvi on 4/01/2018.
 */
class DealProductListActivity : AppCompatActivity() {
    private lateinit var imgBack: ImageView
    private lateinit var edtSearch: EditText
    private lateinit var txtDealNameHeader: TextView
    private lateinit var txtOutLetNameHeader: TextView
    private lateinit var lytOrderSummary: RelativeLayout
    private lateinit var txtTotalItems: TextView
    private lateinit var imgDeals: ImageView
    private lateinit var lytMinCart: RelativeLayout
    private lateinit var txtMinimumCartValue: TextView
    private lateinit var txtPayUpFront: TextView
    private lateinit var lstProductList: RecyclerView
    private lateinit var threeDotLoaderWhite: CustomLoadingViewWhite
    private lateinit var edtSearchClear: ImageView
    private lateinit var dealNumber: String
    private lateinit var lstDeals: DealsBaseResponse.Deals
    private lateinit var products: MutableList<ProductDetailBySupplier>
    private lateinit var productListAdapter: SupplierProductListAdapter
    private var cartItemCount = 0
    private lateinit var selectedProductList: List<Product>
    private var selectedProductMap: MutableMap<String, Product>? = HashMap()
    private lateinit var showProductListCalledFrom: String
    private lateinit var txtAbout: TextView
    private lateinit var productList: List<DealsProductsPaginated.DealProducts>
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var productDetailBySupplier: ProductDetailBySupplier
    private lateinit var viewOrderScrollHelper: DealsAndEssentialsProductsListScrollHelper
    private lateinit var lytHiddenContent: RelativeLayout
    private lateinit var btnFilter: ImageView
    private lateinit var txtNumberOfSelectedFilters: TextView
    private var outletCategoryList: List<OutletTags?> = ArrayList()
    private var filterOutletCategoryList: MutableList<OutletTags?> = ArrayList()
    private var outletCertificationList: MutableList<OutletTags?> = ArrayList()
    private var filterOutletCertificationList: MutableList<OutletTags?> = ArrayList()
    private var selectedFilterCatageryCounter = 0
    private var selectedFilterCertificationCounter = 0
    private lateinit var filterSelectedOutletCategories: MutableList<String>
    private var filterOutletCategories: List<String> = ArrayList()
    private lateinit var filterSelectedOutletCertificates: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_deals_products_to_order)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.DEALS_NUMBER)) {
                dealNumber = bundle.getString(ZeemartAppConstants.DEALS_NUMBER).toString()
            }
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
                selectedProductList = Arrays.asList(*products)
                //create a selected product map
                if (selectedProductList != null && selectedProductList!!.size > 0) {
                    selectedProductMap =
                        ProductDataHelper.createSelectedProductMap(selectedProductList) as MutableMap<String, Product>
                }
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                showProductListCalledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM).toString()
            }
        }
        txtNumberOfSelectedFilters = findViewById(R.id.txt_number_of_selected_filters)
        txtNumberOfSelectedFilters.setVisibility(View.GONE)
        btnFilter = findViewById(R.id.btn_filter)
        imgBack = findViewById(R.id.products_back_btn)
        imgBack.setOnClickListener(View.OnClickListener { createExitDialog() })
        edtSearch = findViewById(R.id.edit_search)
        txtDealNameHeader = findViewById(R.id.txt_product_list_supplier_heading)
        txtOutLetNameHeader = findViewById(R.id.txt_product_list_outlet_heading)
        lytOrderSummary = findViewById(R.id.lyt_review_add_to_order)
        txtTotalItems = lytOrderSummary.findViewById(R.id.txt_review_total_items_in_cart)
        imgDeals = findViewById(R.id.img_deals)
        lytMinCart = findViewById(R.id.lyt_min_amount)
        lytMinCart.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@DealProductListActivity, DealDetailsActivity::class.java)
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
                CommonMethods.hideKeyboard(this@DealProductListActivity)
            }
            false
        })
        edtSearchClear.setOnClickListener(View.OnClickListener { edtSearch.setText("") })
        if (lstDeals != null) {
            txtDealNameHeader.setText(lstDeals!!.title)
            txtOutLetNameHeader.setText(lstDeals!!.supplier?.supplierName)
            if (lstDeals!!.deliveryFeePolicy != null) {
                val cartValue = String.format(
                    resources.getString(R.string.txt_min_cart_amount),
                    lstDeals!!.deliveryFeePolicy?.minOrder?.displayValue
                )
                txtMinimumCartValue.setText(cartValue)
            }
            if (!StringHelper.isStringNullOrEmpty(lstDeals!!.paymentDescription) && lstDeals!!.paymentDescription == ZeemartAppConstants.PAY_UP_FRONT) {
                txtPayUpFront.setText(resources.getString(R.string.txt_pay_upfront))
            } else if (!StringHelper.isStringNullOrEmpty(lstDeals!!.paymentDescription) && lstDeals!!.paymentDescription == ZeemartAppConstants.PAY_AFTER_DELIVERY) {
                txtPayUpFront.setText(resources.getString(R.string.txt_pay_after_delivery))
            }
            if (!StringHelper.isStringNullOrEmpty(lstDeals!!.carouselBanners!![0])) Picasso.get()
                .load(
                    lstDeals!!.carouselBanners!![0]
                ).fit().into(imgDeals)
        }
        setFont()
        val apiParamsHelper = ApiParamsHelper()
        callDealsAPI()
        setLayoutReviewButton(cartItemCount)
        lytHiddenContent = findViewById(R.id.lyt_hidden_content)
        viewOrderScrollHelper = DealsAndEssentialsProductsListScrollHelper(
            this@DealProductListActivity,
            lstProductList,
            linearLayoutManager,
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
        filterSelectedOutletCategories = ArrayList()
        filterSelectedOutletCertificates = ArrayList()
        btnFilter.setOnClickListener(View.OnClickListener {
            selectedFilterCatageryCounter = getSelectedCount(outletCategoryList)
            selectedFilterCertificationCounter = getSelectedCount(outletCertificationList)
            filterOutletCategoryList = ArrayList()
            filterOutletCertificationList = ArrayList()
            if (outletCategoryList != null && outletCategoryList!!.size > 0) {
                for (i in outletCategoryList!!.indices) {
                    val outletTags = outletCategoryList!![i]?.let { it1 -> OutletTags(it1) }
                    filterOutletCategoryList.add(outletTags)
                }
            }
            if (outletCertificationList != null && outletCertificationList!!.size > 0) {
                for (i in outletCertificationList!!.indices) {
                    val outletTags = outletCertificationList!![i]?.let { it1 -> OutletTags(it1) }
                    filterOutletCertificationList.add(outletTags)
                }
            }
            ShowFilterTagsDialog(
                this@DealProductListActivity,
                true,
                true,
                selectedFilterCatageryCounter,
                0,
                0,
                selectedFilterCertificationCounter,
                0,
                ArrayList(),
                filterOutletCategoryList as List<OutletTags>,
                filterOutletCertificationList as List<OutletTags>,
                ArrayList(),
                object : ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {}
                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {}
                },
                object :
                    ProductsFilterListingAdapter.ProductListSelectedFilterCategoriesItemClickListener {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterCatageryCounter = selectedFilterCatageryCounter + 1
                        val totalSelectedFilters = "" + selectedFilterCatageryCounter
                        if (selectedFilterCatageryCounter != 0) {
                            textView?.visibility = View.VISIBLE
                            textView?.text = totalSelectedFilters
                        } else {
                            textView?.visibility = View.GONE
                        }

                        // updateFilterIcon();
                        val categoryName = outletTags?.categoryName
                        if (categoryName != null) {
                            filterSelectedOutletCategories.add(categoryName)
                        }
                        // setApiParamsAfterFilter();
                    }

                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterCatageryCounter = selectedFilterCatageryCounter - 1
                        val totalSelectedFilters = "" + selectedFilterCatageryCounter
                        if (selectedFilterCatageryCounter != 0) {
                            textView?.visibility = View.VISIBLE
                            textView?.text = totalSelectedFilters
                        } else {
                            textView?.visibility = View.GONE
                        }

                        // updateFilterIcon();
                        val categoryName = outletTags?.categoryName
                        for (i in filterSelectedOutletCategories.indices) {
                            if (categoryName == filterSelectedOutletCategories.get(i)) {
                                filterSelectedOutletCategories.removeAt(i)
                            }
                        }
                        // setApiParamsAfterFilter();
                    }

                    override fun onSavePressed() {
                        outletCategoryList = filterOutletCategoryList
                        outletCertificationList = filterOutletCertificationList
                        updateFilterIcon()
                        setApiParamsAfterFilter()
                    }

                    override fun onResetPressed() {
                        // filterOutletCategories = new ArrayList<>();
                        setApiParamsAfterFilterForReset()
                    }

                    override fun onClear() {}
                },
                object :
                    ProductsFilterListingAdapter.ProductListSelectedFilterCertificationItemClickListener {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterCertificationCounter = selectedFilterCertificationCounter + 1
                        val totalSelectedFilters = "" + selectedFilterCertificationCounter
                        if (selectedFilterCertificationCounter != 0) {
                            textView?.visibility = View.VISIBLE
                            textView?.text = totalSelectedFilters
                        } else {
                            textView?.visibility = View.GONE
                        }

                        // updateFilterIcon();
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id)) outletTags?.id?.let { it1 ->
                            filterSelectedOutletCertificates.add(
                                it1
                            )
                        }
                        // setApiParamsAfterFilter();
                    }

                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterCertificationCounter = selectedFilterCertificationCounter - 1
                        val totalSelectedFilters = "" + selectedFilterCertificationCounter
                        if (selectedFilterCertificationCounter != 0) {
                            textView?.visibility = View.VISIBLE
                            textView?.text = totalSelectedFilters
                        } else {
                            textView?.visibility = View.GONE
                        }

                        //   updateFilterIcon();
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id)) for (i in filterSelectedOutletCertificates.indices) {
                            if (outletTags?.id == filterSelectedOutletCertificates.get(i)) {
                                filterSelectedOutletCertificates.removeAt(i)
                            }
                        }
                        // setApiParamsAfterFilter();
                    }
                },
                object : ProductListSelectedFilterSupplierItemClickListener {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {}
                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {}
                },
                object : ProductListSelectedFilterCategoriesItemClickListeneremptydata {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {}
                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {}
                })
        })
    }

    private fun setApiParamsAfterFilterForReset() {
        selectedFilterCertificationCounter = 0
        selectedFilterCatageryCounter = 0
        updateFilterIcon()
        filterSelectedOutletCategories = ArrayList()
        filterSelectedOutletCertificates = ArrayList()
        if (outletCategoryList != null && outletCategoryList!!.size > 0) {
            for (i in outletCategoryList!!.indices) {
                outletCategoryList!![i]!!.isCategorySelected = false
            }
        }
        if (outletCertificationList != null && outletCertificationList!!.size > 0) {
            for (i in outletCertificationList!!.indices) {
                outletCertificationList!![i]!!.isCategorySelected = false
            }
        }
        txtNumberOfSelectedFilters!!.visibility = View.GONE
        setApiParamsAfterFilter()
    }

    private fun getSelectedCount(outletTagsList: List<OutletTags?>?): Int {
        var filterCount = 0
        if (outletTagsList != null && outletTagsList.size > 0) {
            for (i in outletTagsList.indices) {
                if (outletTagsList[i]!!.isCategorySelected) {
                    filterCount = filterCount + 1
                }
            }
        }
        return filterCount
    }

    private fun updateFilterIcon() {
        var filterCount = 0
        if (outletCategoryList != null && outletCategoryList!!.size > 0) {
            for (i in outletCategoryList!!.indices) {
                if (outletCategoryList!![i]!!.isCategorySelected) {
                    filterCount = filterCount + 1
                }
            }
        }
        if (outletCertificationList != null && outletCertificationList!!.size > 0) {
            for (i in outletCertificationList!!.indices) {
                if (outletCertificationList!![i]!!.isCategorySelected) {
                    filterCount = filterCount + 1
                }
            }
        }
        if (filterCount != 0) {
            txtNumberOfSelectedFilters!!.visibility = View.VISIBLE
            txtNumberOfSelectedFilters!!.text = filterCount.toString() + ""
        } else {
            txtNumberOfSelectedFilters!!.visibility = View.GONE
        }
    }

    private fun setApiParamsAfterFilter() {
        val apiParamsHelper = ApiParamsHelper()
        if (filterSelectedOutletCategories != null && filterSelectedOutletCategories!!.size > 0) apiParamsHelper.setCategory(
            filterSelectedOutletCategories
        )
        if (filterSelectedOutletCertificates != null && filterSelectedOutletCertificates!!.size > 0) apiParamsHelper.setCertifications(
            filterSelectedOutletCertificates
        )
        callDealsDetailsAPI(apiParamsHelper, true)
    }

    private fun moveToReviewOrderScreen() {
        if (products != null && products!!.size != 0) {
            val selectedProducts = ArrayList<Product>()
            for (i in products!!.indices) {
                if (products!![i].isSelected) {
                    var item: Product?
                    val key = products!![i].priceList!![0].unitSize?.let {
                        ProductDataHelper.getKeyProductMap(
                            products!![i].sku,
                            it
                        )
                    }
                    item = if (selectedProductMap?.containsKey(key) == true) {
                        selectedProductMap!![products!![i].sku + products!![i].priceList!![0].unitSize]
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
                    val newIntent =
                        Intent(this@DealProductListActivity, ReviewDealsActivity::class.java)
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
                ZeemartBuyerApp.getToastRed(getString(R.string.txt_no_products_selected))
            }
        } else {
            ZeemartBuyerApp.getToastRed(getString(R.string.txt_no_products_selected))
        }
    }

    private fun loadInfo() {
        if (lstDeals != null) {
            txtDealNameHeader!!.text = lstDeals!!.title
            txtOutLetNameHeader!!.text = lstDeals!!.supplier?.supplierName
            if (lstDeals!!.deliveryFeePolicy != null) {
                val cartValue = String.format(
                    resources.getString(R.string.txt_min_cart_amount),
                    lstDeals!!.deliveryFeePolicy?.minOrder?.displayValue
                )
                txtMinimumCartValue!!.text = cartValue
            }
            if (lstDeals!!.paymentDescription == ZeemartAppConstants.PAY_UP_FRONT) {
                txtPayUpFront!!.text = resources.getString(R.string.txt_pay_upfront)
            } else if (lstDeals!!.paymentDescription == ZeemartAppConstants.PAY_AFTER_DELIVERY) {
                txtPayUpFront!!.text = resources.getString(R.string.txt_pay_after_delivery)
            }
            if (!StringHelper.isStringNullOrEmpty(lstDeals!!.carouselBanners!![0])) Picasso.get()
                .load(
                    lstDeals!!.carouselBanners!![0]
                ).fit().into(imgDeals)
        }
        lytOrderSummary!!.setOnClickListener { moveToReviewOrderScreen() }
    }

    private fun callDealsAPI() {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setDealNumber(dealNumber)
        OrderHelper.getActiveDeals(this, dealNumber, object : DealsResponseListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsBaseResponse?) {
                if (dealsBaseResponse != null && dealsBaseResponse.deals != null && dealsBaseResponse.deals!!.size > 0) {
                    lstDeals = dealsBaseResponse.deals!![0]
                    callProductAPI()
                    loadInfo()
                } else {
                }
            }

            override fun onErrorResponse(error: VolleyError?) {}
        })
    }

    private fun callProductAPI() {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setDealNumber(dealNumber)
        OrderHelper.getDealProducts(this, apiParamsHelper, object : DealsProductsListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsProductsPaginated?) {
                threeDotLoaderWhite!!.visibility = View.GONE
                if (dealsBaseResponse != null && dealsBaseResponse.data != null && dealsBaseResponse.data!!.dealProducts != null) {
                    productList = ArrayList(dealsBaseResponse.data!!.dealProducts)
                    addProductsList(false)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                threeDotLoaderWhite!!.visibility = View.GONE
            }
        })
    }

    private fun callDealsDetailsAPI(
        apiParamsHelper: ApiParamsHelper,
        isCalledFromFilters: Boolean
    ) {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        if (lstDeals != null) apiParamsHelper.setDealNumber(lstDeals!!.dealNumber!!)
        OrderHelper.getDealProducts(this, apiParamsHelper, object : DealsProductsListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsProductsPaginated?) {
                threeDotLoaderWhite!!.visibility = View.GONE
                if (dealsBaseResponse != null && dealsBaseResponse.data != null && dealsBaseResponse.data!!.dealProducts != null) {
                    productList = ArrayList(dealsBaseResponse.data!!.dealProducts)
                    addProductsList(isCalledFromFilters)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                threeDotLoaderWhite!!.visibility = View.GONE
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        productListAdapter!!.notifyDataSetChanged()
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
                                ProductDataHelper.createSelectedProductMap(selectedProductList) as MutableMap<String, Product>
                        }
                        cartItemCount = 0
                        setLayoutReviewButton(cartItemCount)
                        addProductsList(true)
                    }
                }
            }
        }
        if (requestCode == RequestCode.ProductDetailsActivity) {
            if (resultCode == -1) {
                val type = object : TypeToken<Map<String?, Product?>?>() {}.type
                selectedProductMap = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    data!!.getStringExtra(ZeemartAppConstants.SELECTED_MAPPING), type
                )
                val json = data.getStringExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                val productsList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    json,
                    ProductDetailBySupplier::class.java
                )
                for (i in products!!.indices) {
                    if (products!![i].sku == productsList.sku && products!![i].productName == productsList.productName
                        && products!![i].priceList!![0].unitSize == productsList.priceList!![0].unitSize) {
                        products!![i].isSelected = productsList.isSelected
                        products!![i].priceList = productsList.priceList
                        break
                    }
                }
                updateData()
                productListAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun updateData() {
        if (products != null && products!!.size != 0) {
            val selectedProducts = ArrayList<Product>()
            for (i in products!!.indices) {
                if (products!![i].isSelected) {
                    var item: Product?
                    val key = products!![i].priceList!![0].unitSize?.let {
                        ProductDataHelper.getKeyProductMap(
                            products!![i].sku,
                            it
                        )
                    }
                    if (selectedProductMap?.containsKey(key) == true) {
                        item =
                            selectedProductMap!![products!![i].sku + products!![i].priceList!![0].unitSize]
                    } else {
                        item = ProductDataHelper.createSelectedProductObject(products!![i])
                        val key1 = ProductDataHelper.getKeyProductMap(item?.sku, item?.unitSize!!)
                        selectedProductMap?.set(key1, item)
                    }
                    if (item != null) {
                        selectedProducts.add(item)
                    }
                }
            }
            setLayoutReviewButton(selectedProducts.size)
            productListAdapter!!.onActivityResult(selectedProductMap)
            productListAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            edtSearch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDealNameHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOutLetNameHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalItems,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtMinimumCartValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPayUpFront,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAbout,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNumberOfSelectedFilters,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun addProductsList(isCalledFromFilters: Boolean) {
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
                            productUom.priceList = (productPriceLists)
                            productUom.isHasMultipleUom = (true)
                            products.add(productUom)
                        }
                    }
                } else {
                    val productUom = ProductDataHelper.getDealProducts(
                        productList!![i]
                    )
                    val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                    productPriceLists.add(productList!![i].unitPrices!![0])
                    productUom.priceList = (productPriceLists)
                    productUom.isHasMultipleUom = (true)
                    products.add(productUom)
                }
            }
            if (selectedProductList != null && selectedProductList!!.size > 0) {
                for (i in selectedProductList!!.indices) {
                    for (j in products.indices) {
                        if (products.get(j).sku == selectedProductList!![i].sku
                            && products.get(j).priceList!![0].unitSize == selectedProductList!![i].unitSize) {
                            products.get(j).isSelected = true
                            cartItemCount = cartItemCount + 1
                            setLayoutReviewButton(cartItemCount)
                            break
                        }
                    }
                }
            }
            updateProductsAdapter()
            if (!isCalledFromFilters) {
                val uniqueFilterCategoriesSet: MutableSet<String> = HashSet()
                val uniqueFilterCertificationSet: MutableSet<OutletTags?> = HashSet()
                if (products != null && products.size > 0) {
                    for (i in products.indices) {
                        if (!StringHelper.isStringNullOrEmpty(products.get(i).categoryPath)) {
                            val twoStringArray =
                                products.get(i).categoryPath?.split(":".toRegex(), limit = 2)
                                    ?.toTypedArray() //the main line
                            val categoryPath = twoStringArray!![0]
                            uniqueFilterCategoriesSet.add(categoryPath)
                        }
                        if (products.get(i).certifications != null) {
                            for (j in products.get(i).certifications!!.indices) {
                                if (!StringHelper.isStringNullOrEmpty(products.get(i).certifications!![j].id) && !StringHelper.isStringNullOrEmpty(
                                        products.get(i).certifications!![j].name
                                    )
                                ) {
                                    val outletTags = OutletTags()
                                    outletTags.id = products.get(i).certifications!![j].id
                                    outletTags.categoryName = products.get(i).certifications!![j].name
                                    uniqueFilterCertificationSet.add(outletTags)
                                }
                            }
                        }
                    }
                }
                if (uniqueFilterCategoriesSet != null && uniqueFilterCategoriesSet.size > 0) {
                    val outletTags = ArrayList<OutletTags?>()
                    for (tag in uniqueFilterCategoriesSet) {
                        val categoryObj = OutletTags()
                        categoryObj.categoryName = tag
                        categoryObj.isCategorySelected = false
                        outletTags.add(categoryObj)
                    }
                    OutletTags.sortByCategoryName(outletTags as List<OutletTags>)
                    outletCategoryList = outletTags
                }
                if (uniqueFilterCertificationSet != null && uniqueFilterCertificationSet.size > 0) {
                    outletCertificationList!!.addAll(uniqueFilterCertificationSet)
                }
            }
        }
    }

    private fun updateProductsAdapter() {
        if (products != null && products!!.size > 0) {
            if (edtSearch!!.text.toString().length == 0) {
                productListAdapter = SupplierProductListAdapter(
                    false,
                    this,
                    products,
                    SharedPref.defaultOutlet?.outletId!!,
                    ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST,
                    lstDeals!!.supplier?.supplierName!!,
                    lstDeals!!.supplier?.supplierId!!,
                    lstDeals!!.supplier?.logoURL!!,
                    null,
                    selectedProductMap,
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
                        if (productDetailBySupplier!!.sku == products!![i].sku
                            && productDetailBySupplier!!.priceList!![0].unitSize == products!![i].priceList!![0].unitSize) {
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
                            ) == true || products[i].productCustomName != null && products[i].productCustomName?.lowercase(
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
            false,
            this,
            filteredList,
            SharedPref.defaultOutlet?.outletId!!,
            ZeemartAppConstants.CALLED_FROM_DEALS_PRODUCT_LIST,
            lstDeals!!.supplier?.supplierName!!,
            lstDeals!!.supplier?.logoURL!!,
            lstDeals!!.supplier?.supplierId!!,
            null,
            selectedProductMap,
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
            lytOrderSummary!!.visibility = View.VISIBLE
            lytOrderSummary!!.isClickable = true
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
            lytOrderSummary!!.visibility = View.VISIBLE
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
                        item = if (selectedProductMap?.containsKey(key) == true) {
                            selectedProductMap!![products!![i].sku + products!![i].priceList!![0].unitSize]
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