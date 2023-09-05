package zeemart.asia.buyers.selfOnBoarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
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
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SearchEssentialsAndDealsAdapter
import zeemart.asia.buyers.adapter.SelfOnBoardingSearchEssentialsAndDealsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.ProductDataHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.order.SearchForNewOrderMgr
import zeemart.asia.buyers.modelsimport.*
import zeemart.asia.buyers.network.EssentialsApi
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseOnBoardingListener
import zeemart.asia.buyers.network.EssentialsApi.SelfOnBoardingEssentialsProductsSearchResponseListener
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderHelper.DealsOnBoardingResponseListener
import java.util.*

/**
 * Created by RajPrudhviMarella on 26/Apr/2021.
 */
class SelfOnBoardingSearchedCategoriesActivity : AppCompatActivity() {
    private var categoriesList: SearchEssentialAndDealsModel.Category? = null
    private lateinit var btnBack: ImageView
    private lateinit var lstProducts: RecyclerView
    private lateinit var edtSearch: EditText
    private lateinit var edtSearchClear: ImageView
    private val lstSearchEssentialsAndDealsData = ArrayList<SearchForNewOrderMgr>()
    private lateinit var essentialProductList: MutableList<ProductDetailBySupplier>
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var txtProductListHeading: TextView
    private var lytNoFilterResults: RelativeLayout? = null
    private var txtNoFilterResults: TextView? = null
    private var txtDeselectFilters: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searched_categories_products)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.CATEGORY_LIST)) {
                val json = bundle.getString(ZeemartAppConstants.CATEGORY_LIST)
                categoriesList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    json,
                    SearchEssentialAndDealsModel.Category::class.java
                )
            }
        }
        txtProductListHeading = findViewById(R.id.txt_product_list_heading)
        txtProductListHeading.setText(categoriesList!!.name)
        lstProducts = findViewById(R.id.lstProductList)
        lstProducts.setNestedScrollingEnabled(false)
        lstProducts.setLayoutManager(LinearLayoutManager(this))
        btnBack = findViewById(R.id.products_back_btn)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_product_list)
        edtSearch = findViewById(R.id.edit_search)
        edtSearchClear = findViewById(R.id.edit_search_clear)
        edtSearch.setHint(resources.getString(R.string.txt_search))
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
                CommonMethods.hideKeyboard(this@SelfOnBoardingSearchedCategoriesActivity)
            }
            false
        })
        edtSearchClear.setOnClickListener(View.OnClickListener { edtSearch.setText("") })
        lytNoFilterResults = findViewById(R.id.lyt_no_filter_results)
        txtDeselectFilters = findViewById(R.id.txt_deselect_filters)
        txtNoFilterResults = findViewById(R.id.txt_no_result)
        setFont()
        callProductAPi()
    }

    private fun setFont() {
        setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtProductListHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtNoFilterResults, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtDeselectFilters, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    private fun callProductAPi() {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setCategory(categoriesList!!.path!!)
        EssentialsApi.getSearchedEssentialAndDealsForOnBoarding(
            this,
            apiParamsHelper,
            object : SelfOnBoardingEssentialsProductsSearchResponseListener {
                override fun onSuccessResponse(essentialsList: SelfOnBoardingSearchEssentialAndDealsModel?) {
                    if (essentialsList != null && essentialsList.data != null && essentialsList.data!!.products != null && essentialsList.data!!.products?.data != null && essentialsList.data!!.products?.data!!.size > 0) {
                        setData(essentialsList.data!!.products!!.data!!)
                    } else {
                        threeDotLoaderWhite!!.visibility = View.GONE
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    private fun setData(fetchedOrders: List<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts>) {
        val products: List<SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts> =
            ArrayList(fetchedOrders)
        essentialProductList = ArrayList()
        if (products.size > 0) {
            for (i in products.indices) {
                val productUom = ProductDataHelper.getEssentialProducts(products[i])
                val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                productPriceLists.add(products[i].unitPrices!![0])
                productUom.priceList = productPriceLists
                productUom.isHasMultipleUom = true
                essentialProductList.add(productUom)
            }
            for (i in essentialProductList.indices) {
                val searchNewOrdersManagerOrder = SearchForNewOrderMgr()
                searchNewOrdersManagerOrder.productDetailBySupplier = essentialProductList.get(i)
                lstSearchEssentialsAndDealsData.add(searchNewOrdersManagerOrder)
            }
        }
        updateProductsAdapter()
    }

    private fun updateProductsAdapter() {
        if (edtSearch!!.text.toString().length == 0) {
            lstProducts!!.adapter = SelfOnBoardingSearchEssentialsAndDealsAdapter(
                this,
                lstSearchEssentialsAndDealsData,
                null,
                object : SelfOnBoardingSearchEssentialsAndDealsAdapter.OnItemClicked {
                    override fun onEssentialOrDealSupplierClicked(supplier: SearchedEssentialAndDealsSuppliers?) {
                        threeDotLoaderWhite!!.visibility = View.VISIBLE
                        if (!StringHelper.isStringNullOrEmpty(supplier?.essentialsId)) {
                            moveToEssentialProductScreen(supplier?.essentialsId!!, null)
                        } else if (!StringHelper.isStringNullOrEmpty(supplier?.dealNumber)) {
                            moveToDealsProductScreen(supplier?.dealNumber!!, null)
                        }
                    }

                    override fun onEssentialOrDealProductClicked(products: ProductDetailBySupplier?) {
                        threeDotLoaderWhite!!.visibility = View.VISIBLE
                        if (!StringHelper.isStringNullOrEmpty(products?.essentialsId)) {
                            moveToEssentialProductScreen(products?.essentialsId!!, products)
                        } else if (!StringHelper.isStringNullOrEmpty(products?.dealNumber)) {
                            moveToDealsProductScreen(products?.dealNumber!!, products)
                        }
                    }

                    override fun onCategoryClicked(category: String?) {}
                })
        } else {
            filterProductsOnSearchedText(lstSearchEssentialsAndDealsData)
        }
        threeDotLoaderWhite!!.visibility = View.GONE
    }

    private fun filterProductsOnSearchedText(products: ArrayList<SearchForNewOrderMgr>?) {
        val searchedString = edtSearch!!.text.toString()
        val searchStringArray =
            searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val filteredList = ArrayList<SearchForNewOrderMgr>()
        if (products != null) {
            for (i in products.indices) {
                var searchStringFound = false
                for (j in searchStringArray.indices) {
                    searchStringFound =
                        if (products[i].productDetailBySupplier?.productName?.lowercase(
                                Locale.getDefault()
                            )!!.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            ) || products[i].productDetailBySupplier?.productCustomName != null && products[i].productDetailBySupplier?.productCustomName?.lowercase(
                                Locale.getDefault()
                            )!!.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            )
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
            SearchForNewOrderMgr.sortByProductName(filteredList)
        }
        if (filteredList.size == 0) {
            lstProducts!!.visibility = View.INVISIBLE
            lytNoFilterResults!!.visibility = View.VISIBLE
        } else {
            lytNoFilterResults!!.visibility = View.GONE
            lstProducts!!.visibility = View.VISIBLE
        }
        lstProducts!!.adapter = SearchEssentialsAndDealsAdapter(
            this,
            filteredList,
            null,
            object : SearchEssentialsAndDealsAdapter.OnItemClicked {
                override fun onEssentialOrDealSupplierClicked(supplier: SearchedEssentialAndDealsSuppliers?) {
                    threeDotLoaderWhite!!.visibility = View.VISIBLE
                    if (!StringHelper.isStringNullOrEmpty(supplier?.essentialsId)) {
                        moveToEssentialProductScreen(supplier?.essentialsId!!, null)
                    } else if (!StringHelper.isStringNullOrEmpty(supplier?.dealNumber)) {
                        moveToDealsProductScreen(supplier?.dealNumber!!, null)
                    }
                }

                override fun onEssentialOrDealProductClicked(products: ProductDetailBySupplier?) {
                    threeDotLoaderWhite!!.visibility = View.VISIBLE
                    if (!StringHelper.isStringNullOrEmpty(products?.essentialsId)) {
                        moveToEssentialProductScreen(products?.essentialsId!!, products)
                    } else if (!StringHelper.isStringNullOrEmpty(products?.dealNumber)) {
                        moveToDealsProductScreen(products?.dealNumber!!, products)
                    }
                }

                override fun onCategoryClicked(category: String?) {}
            })
    }

    private fun moveToDealsProductScreen(
        DealNumber: String,
        productDetailBySupplier: ProductDetailBySupplier?
    ) {
        OrderHelper.getActiveDealsForOnBoarding(this, object : DealsOnBoardingResponseListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsResponseForOnBoarding?) {
                if (dealsBaseResponse != null && dealsBaseResponse.data != null && dealsBaseResponse.data!!.deals != null && dealsBaseResponse.data!!.deals!!.size > 0) {
                    for (i in dealsBaseResponse.data!!.deals!!.indices) {
                        if (dealsBaseResponse.data!!.deals!![i].dealNumber == DealNumber) {
                            val intent = Intent(
                                this@SelfOnBoardingSearchedCategoriesActivity,
                                DealsOnBoardingProductListActivity::class.java
                            )
                            intent.putExtra(
                                ZeemartAppConstants.DEALS_LIST,
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                    dealsBaseResponse.data!!.deals!![0]
                                )
                            )
                            if (productDetailBySupplier != null) intent.putExtra(
                                ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetailBySupplier)
                            )
                            startActivity(intent)
                        }
                    }
                }
                threeDotLoaderWhite!!.visibility = View.GONE
            }

            override fun onErrorResponse(error: VolleyError?) {
                threeDotLoaderWhite!!.visibility = View.GONE
            }
        })
    }

    private fun moveToEssentialProductScreen(
        EssentialId: String,
        productDetailBySupplier: ProductDetailBySupplier?
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setEssentialsId(EssentialId)
        EssentialsApi.getPaginatedEssentialsForOnBoarding(
            this,
            object : EssentialsResponseOnBoardingListener {
                override fun onSuccessResponse(essentialsList: EssentialsResponseForOnBoarding?) {
                    if (essentialsList != null && essentialsList.data?.essentials != null && essentialsList.data!!.essentials!!.size > 0) {
                        for (i in essentialsList.data!!.essentials!!.indices) {
                            if (essentialsList.data!!.essentials!![i].essentialsId == EssentialId) {
                                val intent = Intent(
                                    this@SelfOnBoardingSearchedCategoriesActivity,
                                    EssentialsOnBoardingProductListActivity::class.java
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.ESSENTIALS_LIST,
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                        essentialsList.data!!.essentials!![i]
                                    )
                                )
                                if (productDetailBySupplier != null) intent.putExtra(
                                    ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                        productDetailBySupplier
                                    )
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.CALLED_FROM,
                                    ZeemartAppConstants.CALLED_FROM_ESSENTIAL_SEARCH_LIST
                                )
                                startActivity(intent)
                            }
                        }
                    }
                    threeDotLoaderWhite!!.visibility = View.GONE
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }
}