package zeemart.asia.buyers.goodsReceivedNote

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
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
import zeemart.asia.buyers.adapter.GRNProductListAdapter
import zeemart.asia.buyers.adapter.GRNProductListAdapter.onProductSelectedGRNListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.GRNskuSelectionDialog
import zeemart.asia.buyers.helper.GRNskuSelectionDialog.GRNskuSelectionDialogChangeListener
import zeemart.asia.buyers.helper.ProductDataHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.ProductListBySupplierListner
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.ProductListBySupplier
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimportimport.EssentialProductsPaginated
import zeemart.asia.buyers.network.EssentialsApi
import zeemart.asia.buyers.network.EssentialsApi.EssentialsProductsResponseListener
import zeemart.asia.buyers.network.MarketListApi.retrieveMarketListOutlet
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.util.*

/**
 * Created by RajPrudhviMarella on 14/June/2021.
 */
class GRNProductListActivity : AppCompatActivity() {
    private lateinit var lstProducts: RecyclerView
    private lateinit var edtSearch: EditText
    private var supplierId: String? = null
    private lateinit var outletID: String
    private lateinit var products: MutableList<ProductDetailBySupplier>
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private lateinit var txtSearchCancel: TextView
    private var txtProductListHeading: TextView? = null
    private var txtCantFindItem: TextView? = null
    private lateinit var btnCantFindItem: RelativeLayout
    private lateinit var btnBack: ImageView
    private var supplier: Supplier? = null
    private lateinit var lytNoFilterResults: RelativeLayout
    private var txtNoFilterResults: TextView? = null
    private lateinit var txtDeselectFilters: TextView
    private var mOrder: Orders? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grn_product_list)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_ID)) {
                supplierId = bundle.getString(ZeemartAppConstants.SUPPLIER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.OUTLET_ID)) {
                outletID = bundle.getString(ZeemartAppConstants.OUTLET_ID).toString()
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_ID)) {
                supplierId = bundle.getString(ZeemartAppConstants.SUPPLIER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_DETAIL_INFO)) {
                supplier = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.SUPPLIER_DETAIL_INFO), Supplier::class.java
                )
            }
            if (bundle.containsKey(ZeemartAppConstants.ORDER_DETAILS_JSON)) {
                mOrder = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.ORDER_DETAILS_JSON), Orders::class.java
                )
            }
        }
        lytNoFilterResults = findViewById(R.id.lyt_no_filter_results)
        txtDeselectFilters = findViewById(R.id.txt_deselect_filters)
        txtNoFilterResults = findViewById(R.id.txt_no_result)
        txtProductListHeading = findViewById(R.id.txt_product_list_heading)
        btnCantFindItem = findViewById(R.id.btn_cant_find_item)
        txtCantFindItem = findViewById(R.id.txt_cant_find_item)
        btnBack = findViewById(R.id.products_back_btn)
        btnBack.setOnClickListener(View.OnClickListener {
            if (threeDotLoaderWhite!!.visibility == View.GONE) {
                finish()
            }
        })
        lstProducts = findViewById(R.id.lstProductList)
        lstProducts.setNestedScrollingEnabled(false)
        lstProducts.setLayoutManager(LinearLayoutManager(this))
        txtSearchCancel = findViewById(R.id.search_btn_cancel)
        txtSearchCancel.setVisibility(View.GONE)
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_product_list)
        edtSearch = findViewById(R.id.edit_search)
        edtSearch.setHint(resources.getString(R.string.content_product_list_new_order_edt_search_hint))
        edtSearch.setCursorVisible(false)
        edtSearch.setOnClickListener(View.OnClickListener {
            edtSearch.setCursorVisible(true)
            txtSearchCancel.setVisibility(View.VISIBLE)
        })
        //added to track search event
        edtSearch.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            AnalyticsHelper.logAction(
                this@GRNProductListActivity,
                AnalyticsHelper.TAP_ADD_TO_ORDER_SEARCH_PRODUCT
            )
        })
        edtSearch.addTextChangedListener(SearchTextWatcher())
        edtSearch.setFocusable(false)
        edtSearch.setFocusableInTouchMode(false)
        txtSearchCancel.setOnClickListener(View.OnClickListener {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            edtSearch.setText("")
            edtSearch.setCursorVisible(false)
            txtSearchCancel.setVisibility(View.GONE)
            lstProducts.setVisibility(View.VISIBLE)
            lytNoFilterResults.setVisibility(View.GONE)
        })
        txtDeselectFilters.setOnClickListener(View.OnClickListener { showSelectionDialog() })
        btnCantFindItem.setOnClickListener(View.OnClickListener { showSelectionDialog() })
        if (mOrder != null && !StringHelper.isStringNullOrEmpty(mOrder!!.orderType) && mOrder!!.orderType.equals(
                ApiParamsHelper.OrderType.ESSENTIAL.value,
                ignoreCase = true
            )
        ) {
            callEssentialsProductsListAPI()
        } else {
            getProductList()
        }
        setFont()
    }

    private fun showSelectionDialog() {
        val createOrderHelperDialog = GRNskuSelectionDialog(
            this@GRNProductListActivity,
            Product(),
            object : GRNskuSelectionDialogChangeListener {
                override fun onDataChange(product: Product?, isStatusChanged: Boolean) {
                    val intent = Intent(
                        this@GRNProductListActivity,
                        GoodsReceivedNoteDashBoardActivity::class.java
                    )
                    intent.putExtra(
                        ZeemartAppConstants.SELECTED_PRODUCT_MAP,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(product)
                    )
                    setResult(OrderDetailsActivity.IS_STATUS_CHANGED, intent)
                    finish()
                }

                override fun requestForRemove(product: Product?) {}
            })
        val dialog = createOrderHelperDialog.createCustomeSKUChangeQuantityDialog(false)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
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
            txtCantFindItem,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSearchCancel,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
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

    private fun callEssentialsProductsListAPI() {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setEssentialsId(mOrder!!.essentialsId!!)
        EssentialsApi.getPaginatedEssentialsProducts(
            this,
            apiParamsHelper,
            object : EssentialsProductsResponseListener {
                override fun onSuccessResponse(essentialsList: EssentialProductsPaginated?) {
                    if (essentialsList != null && essentialsList.data != null && essentialsList.data!!.essentialProducts != null
                        && essentialsList.data!!.essentialProducts?.size!! > 0) {
                        edtSearch!!.isFocusable = true
                        edtSearch!!.isFocusableInTouchMode = true
                        addEssentialProductsList(essentialsList.data!!.essentialProducts)
                    } else {
                        lytNoFilterResults!!.visibility = View.VISIBLE
                        txtNoFilterResults!!.text =
                            getString(R.string.txt_no_items_available_to_select)
                        val redString = resources.getString(R.string.txt_no_items_available_grn)
                        txtDeselectFilters!!.text = Html.fromHtml(redString)
                        lstProducts!!.visibility = View.GONE
                    }
                    threeDotLoaderWhite!!.visibility = View.GONE
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    private fun getProductList() {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSupplierId(supplierId!!)
        retrieveMarketListOutlet(
            this,
            apiParamsHelper,
            outletID,
            object : ProductListBySupplierListner {
                override fun onSuccessResponse(productList: ProductListBySupplier?) {
                    edtSearch.isFocusable = true
                    edtSearch.isFocusableInTouchMode = true
                    addProductsList(productList)
                    threeDotLoaderWhite!!.visibility = View.GONE
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    private fun addEssentialProductsList(productList: List<EssentialProductsPaginated.EssentialsProducts>?) {
        products = ArrayList()
        if (productList != null && productList.size > 0) {
            for (i in productList.indices) {
                val productUom = ProductDataHelper.getEssentialProducts(productList[i])
                productUom.priceList = (productList[i].unitPrices as MutableList<ProductPriceList>)
                productUom.isHasMultipleUom = (true)
                products.add(productUom)
            }
            setSupplierName()
            updateProductsAdapter()
        } else {
            lytNoFilterResults!!.visibility = View.VISIBLE
            txtNoFilterResults!!.text = getString(R.string.txt_no_items_available_to_select)
            val redString = resources.getString(R.string.txt_no_items_available_grn)
            txtDeselectFilters!!.text = Html.fromHtml(redString)
            lstProducts!!.visibility = View.GONE
        }
    }

    private fun addProductsList(productList: ProductListBySupplier?) {
        if (productList != null && productList.data != null && productList.data!!.products != null && productList.data!!.products?.size!! > 0) {
            products = ArrayList()
            productList.data!!.products?.let { products.addAll(it) }
            //get the fav on top + sorted product list
            products = ProductDataHelper.getDisplayProductList(
                this@GRNProductListActivity,
                products,
                outletID,
                supplierId
            ) as MutableList<ProductDetailBySupplier>
            setSupplierName()
            updateProductsAdapter()
        } else {
            lytNoFilterResults!!.visibility = View.VISIBLE
            txtNoFilterResults!!.text =
                getString(R.string.txt_no_items_available_to_select)
            val redString = resources.getString(R.string.txt_no_items_available_grn)
            txtDeselectFilters!!.text = Html.fromHtml(redString)
            lstProducts!!.visibility = View.GONE
        }
    }

    private fun setSupplierName() {
        if (supplier != null && products != null && products!!.size > 0) {
            for (j in products!!.indices) {
                products!![j].supplierName = supplier!!.supplierName
                products!![j].supplier = supplier
            }
        }
    }

    private fun updateProductsAdapter() {
        if (products != null && products!!.size > 0) {
            if (edtSearch!!.text.toString().length == 0) {
                lstProducts.adapter = GRNProductListAdapter(
                    this,
                    products,
                    null,
                    object : onProductSelectedGRNListener {
                        override fun onProductSelect(product: Product?) {
                            val intent = Intent(
                                this@GRNProductListActivity,
                                GoodsReceivedNoteDashBoardActivity::class.java
                            )
                            intent.putExtra(
                                ZeemartAppConstants.SELECTED_PRODUCT_MAP,
                                ZeemartBuyerApp.gsonExposeExclusive.toJson(product)
                            )
                            setResult(OrderDetailsActivity.IS_STATUS_CHANGED, intent)
                            finish()
                        }
                    })
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
                val productCodeTag = products[i].supplierProductCode?.let {
                    products[i].tags?.let { it1 ->
                        products[i].supplierName?.let { it2 ->
                            getProductCodeAndTag(it, it1, it2)
                        }
                    }
                }
                var searchStringFound = false
                for (j in searchStringArray.indices) {
                    searchStringFound =
                        if (products[i].productName?.lowercase(Locale.getDefault())?.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            ) == true || productCodeTag?.lowercase(Locale.getDefault())?.contains(
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
        if (filteredList.size == 0) {
            txtNoFilterResults!!.text = getString(R.string.content_orders_txt_no_search_1_text)
            val redString = resources.getString(R.string.txt_try_changing_items_grn)
            txtDeselectFilters!!.text = Html.fromHtml(redString)
            lytNoFilterResults!!.visibility = View.VISIBLE
            lstProducts!!.visibility = View.GONE
        } else {
            lytNoFilterResults!!.visibility = View.GONE
            lstProducts!!.visibility = View.VISIBLE
        }
        lstProducts.adapter = GRNProductListAdapter(
            this,
            filteredList,
            searchStringArray,
            object : onProductSelectedGRNListener {
                override fun onProductSelect(product: Product?) {
                    val intent = Intent(
                        this@GRNProductListActivity,
                        GoodsReceivedNoteDashBoardActivity::class.java
                    )
                    intent.putExtra(
                        ZeemartAppConstants.SELECTED_PRODUCT_MAP,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(product)
                    )
                    setResult(OrderDetailsActivity.IS_STATUS_CHANGED, intent)
                }
            })
    }

    private fun getProductCodeAndTag(
        productCode: String,
        tags: List<String>,
        supplierName: String,
    ): String {
        var productCodeTag = ""
        if (!StringHelper.isStringNullOrEmpty(productCode)) {
            productCodeTag = productCode
        }
        return productCodeTag
    }

    internal inner class SearchTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            updateProductsAdapter()
            if (!StringHelper.isStringNullOrEmpty(edtSearch!!.text.toString())) {
                lstProducts!!.visibility = View.VISIBLE
                txtSearchCancel!!.visibility = View.VISIBLE
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }
}