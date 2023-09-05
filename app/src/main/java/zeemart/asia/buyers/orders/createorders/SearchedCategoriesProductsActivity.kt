package zeemart.asia.buyers.orders.createorders

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.SearchEssentialsAndDealsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.deals.DealProductListActivity
import zeemart.asia.buyers.essentials.EssentialsProductListActivity
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.ProductDataHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.EssentialsBaseResponse
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.order.SearchForNewOrderMgr
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.modelsimport.SearchEssentialAndDealsModel
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsPruducts
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsSuppliers
import zeemart.asia.buyers.network.EssentialsApi
import zeemart.asia.buyers.network.OrderHelper
import java.util.*

/**
 * Created by Rajprudhvi Marella on 05/Jan/2021.
 */
class SearchedCategoriesProductsActivity() : AppCompatActivity() {
    var categoriesList: SearchEssentialAndDealsModel.Category? = null
    private var btnBack: ImageView? = null
    private lateinit var lstProducts: RecyclerView
    private lateinit var edtSearch: EditText
    private var edtSearchClear: ImageView? = null
    private val lstSearchEssentialsAndDealsData: ArrayList<SearchForNewOrderMgr> =
        ArrayList<SearchForNewOrderMgr>()
    private var essentialProductList: MutableList<ProductDetailBySupplier>? = null
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var txtProductListHeading: TextView
    private var lytNoFilterResults: RelativeLayout? = null
    private var txtNoFilterResults: TextView? = null
    private var txtDeselectFilters: TextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searched_categories_products)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.CATEGORY_LIST)) {
                val json: String = bundle.getString(ZeemartAppConstants.CATEGORY_LIST).toString()
                categoriesList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    json,
                    SearchEssentialAndDealsModel.Category::class.java
                )
            }
        }
        txtProductListHeading = findViewById<TextView>(R.id.txt_product_list_heading)
        txtProductListHeading.text = (categoriesList?.name)
        lstProducts = findViewById<RecyclerView>(R.id.lstProductList)
        lstProducts.setNestedScrollingEnabled(false)
        lstProducts.setLayoutManager(LinearLayoutManager(this))
        btnBack = findViewById<ImageView>(R.id.products_back_btn)
        btnBack!!.setOnClickListener(View.OnClickListener { finish() })
        threeDotLoaderWhite =
            findViewById<CustomLoadingViewWhite>(R.id.spin_kit_loader_product_list)
        edtSearch = findViewById<EditText>(R.id.edit_search)
        edtSearchClear = findViewById<ImageView>(R.id.edit_search_clear)
        edtSearch.setHint(getResources().getString(R.string.txt_search))
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText: String = edtSearch.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear!!.visibility = View.VISIBLE
                } else {
                    edtSearchClear!!.visibility = View.GONE
                }
                updateProductsAdapter()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        edtSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    CommonMethods.hideKeyboard(this@SearchedCategoriesProductsActivity)
                }
                return false
            }
        })
        edtSearchClear!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                edtSearch.setText("")
            }
        })
        lytNoFilterResults = findViewById<RelativeLayout>(R.id.lyt_no_filter_results)
        txtDeselectFilters = findViewById<TextView>(R.id.txt_deselect_filters)
        txtNoFilterResults = findViewById<TextView>(R.id.txt_no_result)
        setFont()
        callProductAPi()
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            edtSearch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtProductListHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoFilterResults,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtDeselectFilters,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun callProductAPi() {
        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setCategory(categoriesList?.path!!)
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        EssentialsApi.getSearchedEssentialAndDeals(
            this,
            apiParamsHelper,
            object : EssentialsApi.EssentialsProductsSearchResponseListener {
                override fun onSuccessResponse(essentialsList: SearchEssentialAndDealsModel?) {
                    if (essentialsList != null) {
                        setData(essentialsList)
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                }
            })
    }

    private fun setData(searchEssentialAndDealsModel: SearchEssentialAndDealsModel) {
        val products: List<SearchedEssentialAndDealsPruducts>? =
            ArrayList<SearchedEssentialAndDealsPruducts>(searchEssentialAndDealsModel.data?.products?.data)
        essentialProductList = ArrayList<ProductDetailBySupplier>()
        if (products != null && products.size > 0) {
            for (i in products.indices) {
                val productUom: ProductDetailBySupplier = ProductDataHelper.getEssentialProducts(
                    products[i]
                )
                val productPriceLists: MutableList<ProductPriceList> = ArrayList<ProductPriceList>()
                products[i].unitPrices?.let { productPriceLists.add(it) }
                productUom.priceList = productPriceLists
                productUom.isHasMultipleUom = true
                essentialProductList!!.add(productUom)
            }
            for (i in essentialProductList!!.indices) {
                val searchNewOrdersManagerOrder = SearchForNewOrderMgr()
                searchNewOrdersManagerOrder.productDetailBySupplier = (essentialProductList!![i])
                lstSearchEssentialsAndDealsData.add(searchNewOrdersManagerOrder)
            }
        }
        updateProductsAdapter()
    }

    private fun updateProductsAdapter() {
        if (edtSearch.text.toString().length == 0) {
            lstProducts.adapter =
                SearchEssentialsAndDealsAdapter(
                    this,
                    lstSearchEssentialsAndDealsData,
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
                            if (!StringHelper.isStringNullOrEmpty(products!!.essentialsId)) {
                                moveToEssentialProductScreen(products.essentialsId!!, products)
                            } else if (!StringHelper.isStringNullOrEmpty(products.dealNumber)) {
                                moveToDealsProductScreen(products.dealNumber!!, products)
                            }
                        }

                        override fun onCategoryClicked(category: String?) {}
                    })
        } else {
            filterProductsOnSearchedText(lstSearchEssentialsAndDealsData)
        }
        threeDotLoaderWhite!!.visibility = View.GONE
    }

    private fun filterProductsOnSearchedText(products: ArrayList<SearchForNewOrderMgr>) {
        val searchedString: String = edtSearch.getText().toString()
        val searchStringArray =
            searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val filteredList: ArrayList<SearchForNewOrderMgr> = ArrayList<SearchForNewOrderMgr>()
        if (products != null) {
            for (i in products.indices) {
                var searchStringFound = false
                for (j in searchStringArray.indices) {
                    searchStringFound = products[i].productDetailBySupplier?.productName?.toLowerCase()
                        ?.contains(
                            searchStringArray[j].lowercase(Locale.getDefault())
                        ) == true || (products[i].productDetailBySupplier
                        ?.productCustomName != null && products[i].productDetailBySupplier
                        ?.productCustomName?.toLowerCase()?.contains(
                            searchStringArray[j].lowercase(Locale.getDefault())
                        ) == true)
                }
                if (searchStringFound) {
                    if (!(filteredList.contains(products[i]))) filteredList.add(products[i])
                }
            }
            SearchForNewOrderMgr.sortByProductName(filteredList)
        }
        if (filteredList.size == 0) {
            lstProducts.setVisibility(View.INVISIBLE)
            lytNoFilterResults?.setVisibility(View.VISIBLE)
        } else {
            lytNoFilterResults?.setVisibility(View.GONE)
            lstProducts.setVisibility(View.VISIBLE)
        }
        lstProducts.setAdapter(
            SearchEssentialsAndDealsAdapter(
                this,
                filteredList,
                null,
                object : SearchEssentialsAndDealsAdapter.OnItemClicked {
                    override fun onEssentialOrDealSupplierClicked(supplier: SearchedEssentialAndDealsSuppliers?) {
                        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
                        if (!StringHelper.isStringNullOrEmpty(supplier?.essentialsId)) {
                            supplier?.essentialsId?.let { moveToEssentialProductScreen(it, null) }
                        } else if (!StringHelper.isStringNullOrEmpty(supplier?.dealNumber)) {
                            supplier?.dealNumber?.let { moveToDealsProductScreen(it, null) }
                        }
                    }

                    override fun onEssentialOrDealProductClicked(products: ProductDetailBySupplier?) {
                        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
                        if (!StringHelper.isStringNullOrEmpty(products?.essentialsId)) {
                            products?.essentialsId?.let { moveToEssentialProductScreen(it, products) }
                        } else if (!StringHelper.isStringNullOrEmpty(products?.dealNumber)) {
                            products?.dealNumber?.let { moveToDealsProductScreen(it, products) }
                        }
                    }

                    override fun onCategoryClicked(category: String?) {}
                })
        )
        //        if (lstProducts.getAdapter() != null) {
//            ((SearchEssentialsAndDealsAdapter) lstProducts.getAdapter()).getFilter().filter(edtSearch.getText().toString());
//        }
    }

    private fun moveToDealsProductScreen(
        DealNumber: String,
        productDetailBySupplier: ProductDetailBySupplier?,
    ) {
        OrderHelper.getActiveDeals(this, DealNumber, object : OrderHelper.DealsResponseListener {
            override fun onSuccessResponse(dealsBaseResponse: DealsBaseResponse?) {
                if ((dealsBaseResponse != null) && (dealsBaseResponse.deals != null) && (dealsBaseResponse.deals!!
                        .size!! > 0)
                ) {
                    val intent = Intent(
                        this@SearchedCategoriesProductsActivity,
                        DealProductListActivity::class.java
                    )
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
                threeDotLoaderWhite?.setVisibility(View.GONE)
            }

            override fun onErrorResponse(error: VolleyError?) {
                threeDotLoaderWhite?.setVisibility(View.GONE)
            }
        })
    }

    private fun moveToEssentialProductScreen(
        EssentialId: String,
        productDetailBySupplier: ProductDetailBySupplier?,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setEssentialsId(EssentialId)
        EssentialsApi.getPaginatedEssentials(
            this,
            apiParamsHelper,
            object : EssentialsApi.EssentialsResponseListener {
                override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                    if ((essentialsList != null) && (essentialsList.essentials != null) && (essentialsList.essentials!!.size > 0)) {
                        val intent = Intent(
                            this@SearchedCategoriesProductsActivity,
                            EssentialsProductListActivity::class.java
                        )
                        intent.putExtra(
                            ZeemartAppConstants.ESSENTIALS_LIST,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                essentialsList.essentials!!.get(0)
                            )
                        )
                        intent.putExtra(
                            ZeemartAppConstants.ESSENTIALS_ID,
                            essentialsList.essentials!!.get(0).essentialsId
                        )
                        if (productDetailBySupplier != null) intent.putExtra(
                            ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(productDetailBySupplier)
                        )
                        startActivity(intent)
                    }
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                }
            })
    }
}