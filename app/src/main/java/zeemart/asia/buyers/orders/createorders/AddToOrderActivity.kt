package zeemart.asia.buyers.orders.createorders

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter
import zeemart.asia.buyers.adapter.SupplierProductListAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.ProductListBySupplierListner
import zeemart.asia.buyers.models.OutletTags
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.ProductListBySupplier
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.CreateDraftOrder
import zeemart.asia.buyers.models.orderimportimport.DraftOrdersBySKUPaginated
import zeemart.asia.buyers.modelsimport.OrderSettingDeliveryPreferences
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.util.*

class AddToOrderActivity : AppCompatActivity() {
    private var lstProducts: RecyclerView? = null
    private var edtSearch: EditText? = null
    private var supplierId: String? = null
    private var outletID: String? = null
    private var products: MutableList<ProductDetailBySupplier>? = null
    private var productListAdapter: SupplierProductListAdapter? = null
    private var showProductListCalledFrom: String? = ""
    private var supplierName: String? = null
    private var supplierLogo: String? = null
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private var supplierDetails: DetailSupplierDataModel? = null
    private var selectedProductMap: MutableMap<String?, Product?> = HashMap()
    private var filterSelectedOutletTags: MutableList<String>? = null
    private var filterSelectedOutletCategories: MutableList<String>? = null
    private var filterSelectedOutletCertificates: MutableList<String>? = null
    private var filterSelectedOutletsupplier: MutableList<String>? = null
    private var lytNoFilterResults: RelativeLayout? = null
    private var txtNoFilterResults: TextView? = null
    private var txtDeselectFilters: TextView? = null
    private var lytRecentSearch: RelativeLayout? = null
    private var txtRecentSearchHeader: TextView? = null
    private var txtClearRecentSearch: TextView? = null
    private var lstRecentSearch: ListView? = null
    private var recentSearchList: MutableList<String>? = ArrayList()
    private var txtSearchCancel: TextView? = null
    private var txtProductListHeading: TextView? = null
    private var btnFilter: ImageView? = null
    private var txtNumberOfSelectedFilters: TextView? = null
    private var lytOrderSummary: RelativeLayout? = null
    private var txtTotalItems: TextView? = null
    private var cartItemCount = 0
    private var lytNoFavourites: RelativeLayout? = null
    private var txtNoFavourites: TextView? = null
    private var txtAddFavouritesToDisplay: TextView? = null
    private var lytNoItems: RelativeLayout? = null
    private var txtNoItems: TextView? = null
    private var txtNoItemsToDisplay: TextView? = null
    private var lstSuppliers: List<DetailSupplierDataModel>? = null
    private var selectedProductList: ArrayList<Product>? = null
    private val isProductTagsVisible = false
    private var outletTagsList: List<OutletTags>? = ArrayList<OutletTags>()
    private var filterOutletTagsList: MutableList<OutletTags> = ArrayList<OutletTags>()
    private var outletCategoryList: List<OutletTags>? = ArrayList<OutletTags>()
    private var filterOutletCategoryList: MutableList<OutletTags> = ArrayList<OutletTags>()
    private var outletCertificationList: MutableList<OutletTags>? = ArrayList<OutletTags>()
    private var filterOutletCertificationList: MutableList<OutletTags> = ArrayList<OutletTags>()
    private var supplierList: List<OutletTags>? = ArrayList<OutletTags>()
    private var filterOutletSupplierList: MutableList<OutletTags> = ArrayList<OutletTags>()
    private var selectedFilterTagCounter = 0
    private var selectedFilterCatageryCounter = 0
    private val clearselectedcategory = 0
    private var selectedFilterCertificationCounter = 0
    private var selectedFiltersupplierCounter = 0
    private var isCalledFromFavourite = false
    private var isCalledBelowPar = false
    private var btnBack: ImageView? = null
    private var lytItemsHeader: RelativeLayout? = null
    private var txtItemsCount: TextView? = null
    private var txtHeaderAction: TextView? = null
    private var lytReviewItems: RelativeLayout? = null
    private var calledFrom: String? = null
    private var imgInfo: ImageView? = null
    private var viewPrice = true
    private var fillAlltoPar = false
    private var posIntegration = false
    private var isShowSupplier = false
    var detailSuppliersList: List<DetailSupplierDataModel>? = ArrayList<DetailSupplierDataModel>()
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_order)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val detailsupplier: String = SharedPref.read(ZeemartAppConstants.SUPPLIER_DETAIL_INFO, "")!!
        detailSuppliersList =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<DetailSupplierDataModel>>(
                detailsupplier,
                object : TypeToken<List<DetailSupplierDataModel?>?>() {}.type
            )
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_ID)) {
                supplierId = bundle.getString(ZeemartAppConstants.SUPPLIER_ID)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_NAME)) {
                supplierName = bundle.getString(ZeemartAppConstants.SUPPLIER_NAME)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_LOGO)) {
                supplierLogo = bundle.getString(ZeemartAppConstants.SUPPLIER_LOGO)
            }
            if (bundle.containsKey(ZeemartAppConstants.POS_INTEGRATION)) {
                posIntegration = bundle.getBoolean(ZeemartAppConstants.POS_INTEGRATION)
            }
            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_DETAIL_INFO)) {
                supplierDetails =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<DetailSupplierDataModel>(
                        bundle.getString(ZeemartAppConstants.SUPPLIER_DETAIL_INFO),
                        DetailSupplierDataModel::class.java
                    )
            }
            if (bundle.containsKey(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
                val json: String? = bundle.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                val products: Array<Product> =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<Array<Product>>(
                        json,
                        Array<Product>::class.java
                    )
                selectedProductList = ArrayList(Arrays.asList(*products))
                //create a selected product map
                if (selectedProductList != null && selectedProductList!!.size > 0) {
                    selectedProductMap =
                        ProductDataHelper.createSelectedProductMap(selectedProductList) as MutableMap<String?, Product?>
                }
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                showProductListCalledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM)
            }
            if (bundle.containsKey(ZeemartAppConstants.OUTLET_ID)) {
                outletID = bundle.getString(ZeemartAppConstants.OUTLET_ID)!!
            }
        }
        if (!StringHelper.isStringNullOrEmpty(
                SharedPref.read(
                    ZeemartAppConstants.SUPPLIER_LIST,
                    ""
                )
            )
        ) {
            val json: String = SharedPref.read(ZeemartAppConstants.SUPPLIER_LIST, "")!!
            lstSuppliers =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<DetailSupplierDataModel>>(
                    json,
                    object : TypeToken<List<DetailSupplierDataModel?>?>() {}.type
                )
        }
        imgInfo = findViewById<ImageView>(R.id.products_info)
        imgInfo!!.setOnClickListener {
            threeDotLoaderWhite?.setVisibility(View.VISIBLE)
            OrderSettingsApi.getDeliveryDates(
                this@AddToOrderActivity,
                outletID,
                supplierDetails?.supplier?.supplierId,
                object : OrderSettingsApi.OrderSettingDeliveryDatesListener {
                    override fun onDeliveryDateListSuccess(allStockShelveData: OrderSettingDeliveryPreferences?) {
                        threeDotLoaderWhite?.setVisibility(View.GONE)
                        if (allStockShelveData != null && allStockShelveData.data != null) DialogHelper.showOrderSettingsProductInfoDialog(
                            this@AddToOrderActivity,
                            allStockShelveData.data!!.outlet?.outletName,
                            allStockShelveData.data!!.supplier?.supplierName,
                            allStockShelveData.data!!,
                            object : DialogHelper.onButtonSaveClickListener {
                                override fun onButtonSaveClicked(
                                    lstData: OrderSettingDeliveryPreferences.Data?,
                                    isWhatsapp: Boolean?,
                                    isSms: Boolean?
                                ) {
                                }
                            })
                    }

                    override fun errorResponse(error: VolleyError?) {
                        threeDotLoaderWhite!!.setVisibility(View.GONE)
                    }
                })
        }
        lytReviewItems = findViewById<RelativeLayout>(R.id.lyt_review_items)
        txtProductListHeading = findViewById<TextView>(R.id.txt_product_list_heading)
        btnBack = findViewById<ImageView>(R.id.products_back_btn)
        btnBack!!.setOnClickListener {
            if (threeDotLoaderWhite?.getVisibility() == View.GONE) {
                createOrEditDraftOrder(object : CreateDraftOrderResponseListener {
                    override fun doNavigation(response: String?) {
                        finish()
                    }
                })
            }
            if (showProductListCalledFrom != null && showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_SUPPLIER_SEARCH) {
                val newIntent = Intent(this@AddToOrderActivity, SearchForNewOrder::class.java)
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_SUPPLIER_ADD_TO_ORDER
                )
                setResult(Activity.RESULT_OK, newIntent)
            }
        }
        lytOrderSummary = findViewById<RelativeLayout>(R.id.lyt_review_add_to_order)
        lytOrderSummary!!.setVisibility(View.INVISIBLE)
        txtTotalItems = lytOrderSummary!!.findViewById<TextView>(R.id.txt_review_total_items_in_cart)
        lytOrderSummary!!.setOnClickListener(View.OnClickListener {
            if (threeDotLoaderWhite?.getVisibility() == View.GONE) {
                val selectedProducts = ArrayList<Product>()
                for (i in products?.indices!!) {
                    if (products!![i].isSelected) {
                        var item: Product?
                        val key: String = ProductDataHelper.getKeyProductMap(
                            products!![i].sku,
                            products!![i].priceList?.get(0)?.unitSize!!
                        )
                        item = if (selectedProductMap!!.containsKey(key)) {
                            selectedProductMap!![products!![i].sku + products!![i].priceList?.get(0)?.unitSize]
                        } else {
                            ProductDataHelper.createSelectedProductObject(products!![i])
                        }
                        if (item != null) {
                            selectedProducts.add(item)
                        }
                    }
                }
                if (selectedProducts.size > 0) {
                    if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
                        val intent: Intent = getIntent()
                        val selectedProductJson: String =
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProducts)
                        intent.putExtra(
                            ZeemartAppConstants.SELECTED_PRODUCT_LIST,
                            selectedProductJson
                        )
                        //                            if(!StringHelper.isStringNullOrEmpty(existingDraftOrderId))
//                                intent.putExtra(ZeemartAppConstants.EXISTING_DRAFT_ID,existingDraftOrderId);
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        createOrEditDraftOrder(object : CreateDraftOrderResponseListener {
                            override fun doNavigation(response: String?) {
                                val draftOrdersBySKUPaginated: DraftOrdersBySKUPaginated? =
                                    ZeemartBuyerApp.fromJson(
                                        response,
                                        DraftOrdersBySKUPaginated::class.java
                                    )
                                OrderHelper.validateAddOnOrderByOrderId(
                                    this@AddToOrderActivity,
                                    draftOrdersBySKUPaginated,
                                    object : OrderHelper.ValidAddOnOrderByOrderIdListener {
                                        override fun onSuccessResponse(addOnOrderValidResponse: DraftOrdersBySKUPaginated?) {
                                            val response: String =
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                    addOnOrderValidResponse
                                                )
                                            val orderSummaryPreviewIntent = Intent(
                                                this@AddToOrderActivity,
                                                ActivityOrderSummaryPreview::class.java
                                            )
                                            if (lstSuppliers != null && lstSuppliers!!.size > 0) {
                                                val detailSupplierJson: String =
                                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                        lstSuppliers
                                                    )
                                                SharedPref.write(
                                                    ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                                                    detailSupplierJson
                                                )
                                            }
                                            orderSummaryPreviewIntent.putExtra(
                                                ZeemartAppConstants.CART_DRAFT_LIST,
                                                response
                                            )
                                            startActivity(orderSummaryPreviewIntent)
                                            finish()
                                        }

                                        override fun onErrorResponse(error: VolleyError?) {
                                            val orderSummaryPreviewIntent = Intent(
                                                this@AddToOrderActivity,
                                                ActivityOrderSummaryPreview::class.java
                                            )
                                            if (lstSuppliers != null && lstSuppliers!!.size > 0) {
                                                val detailSupplierJson: String =
                                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                        lstSuppliers
                                                    )
                                                SharedPref.write(
                                                    ZeemartAppConstants.SUPPLIER_DETAIL_INFO,
                                                    detailSupplierJson
                                                )
                                            }
                                            orderSummaryPreviewIntent.putExtra(
                                                ZeemartAppConstants.CART_DRAFT_LIST,
                                                response
                                            )
                                            startActivity(orderSummaryPreviewIntent)
                                            finish()
                                        }
                                    })
                            }
                        })
                    }
                }
            }
        })
        lstProducts = findViewById<RecyclerView>(R.id.lstProductList)
        lstProducts!!.setNestedScrollingEnabled(false)
        lstProducts!!.setLayoutManager(LinearLayoutManager(this))
        txtNumberOfSelectedFilters = findViewById<TextView>(R.id.txt_number_of_selected_filters)
        txtNumberOfSelectedFilters!!.setVisibility(View.GONE)
        btnFilter = findViewById<ImageView>(R.id.btn_filter)
        filterSelectedOutletCategories = ArrayList()
        filterSelectedOutletCertificates = ArrayList()
        filterSelectedOutletTags = ArrayList()
        filterSelectedOutletsupplier = ArrayList()
        btnFilter!!.setOnClickListener {
            selectedFilterTagCounter = getSelectedCount(outletTagsList)
            selectedFilterCatageryCounter = getSelectedCount(outletCategoryList)
            selectedFiltersupplierCounter = getSelectedCount(supplierList)
            selectedFilterCertificationCounter = getSelectedCount(outletCertificationList)
            filterOutletCategoryList = ArrayList<OutletTags>()
            filterOutletTagsList = ArrayList<OutletTags>()
            filterOutletCertificationList = ArrayList<OutletTags>()
            filterOutletSupplierList = ArrayList<OutletTags>()
            // filterOutletSupplierList = new ArrayList<>();
            if (outletCategoryList != null && outletCategoryList!!.size > 0) {
                for (i in outletCategoryList!!.indices) {
                    val outletTags = OutletTags(outletCategoryList!![i])
                    filterOutletCategoryList.add(outletTags)
                }
            }
            if (outletTagsList != null && outletTagsList!!.size > 0) {
                for (i in outletTagsList!!.indices) {
                    val outletTags = OutletTags(outletTagsList!![i])
                    filterOutletTagsList.add(outletTags)
                }
            }
            if (outletCertificationList != null && outletCertificationList!!.size > 0) {
                for (i in outletCertificationList!!.indices) {
                    val outletTags = OutletTags(outletCertificationList!![i])
                    filterOutletCertificationList.add(outletTags)
                }
            }
            if (supplierList != null && supplierList!!.size > 0) {
                for (i in supplierList!!.indices) {
                    val outletTags = OutletTags(supplierList!![i])
                    filterOutletSupplierList.add(outletTags)
                }
            }
            DialogHelper.ShowFilterTagsDialog(
                this@AddToOrderActivity,
                false,
                isShowSupplier,
                selectedFilterCatageryCounter,
                clearselectedcategory,
                selectedFilterTagCounter,
                selectedFilterCertificationCounter,
                selectedFiltersupplierCounter,
                filterOutletTagsList,
                filterOutletCategoryList,
                filterOutletCertificationList,
                filterOutletSupplierList,
                object : ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterTagCounter = selectedFilterTagCounter + 1
                        val totalSelectedFilters = "" + selectedFilterTagCounter
                        if (selectedFilterTagCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }
                        //updateFilterIcon();
                        val categoryName: String? = outletTags?.categoryName
                        if (categoryName != null) {
                            filterSelectedOutletTags!!.add(categoryName)
                        }
                        //  setApiParamsAfterFilter();
                    }

                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterTagCounter = selectedFilterTagCounter - 1
                        val totalSelectedFilters = "" + selectedFilterTagCounter
                        if (selectedFilterTagCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }

                        //updateFilterIcon();
                        val categoryName: String? = outletTags?.categoryName
                        for (i in filterSelectedOutletTags!!.indices) {
                            if (categoryName == filterSelectedOutletTags!!.get(i)) {
                                filterSelectedOutletTags!!.removeAt(i)
                            }
                        }
                        //setApiParamsAfterFilter();
                    }
                },
                object :
                    ProductsFilterListingAdapter.ProductListSelectedFilterCategoriesItemClickListener {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                        Log.d("===> FOC", "onFilterDeselected: $filterOutletCategoryList")
                        Log.d("===> OC", "onFilterDeselected: " + outletCategoryList.toString())
                        selectedFilterCatageryCounter = selectedFilterCatageryCounter + 1
                        val totalSelectedFilters = "" + selectedFilterCatageryCounter
                        if (selectedFilterCatageryCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }

                        //  updateFilterIcon();
                        val categoryName: String? = outletTags?.categoryName
                        if (categoryName != null) {
                            filterSelectedOutletCategories!!.add(categoryName)
                        }
                        //  filterSelectedOutletCategories.add(categoryName);
                        //setApiParamsAfterFilter();
                    }

                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                        Log.d("===> FOC", "onFilterDeselected: $filterOutletCategoryList")
                        Log.d("===> OC", "onFilterDeselected: " + outletCategoryList.toString())
                        selectedFilterCatageryCounter = selectedFilterCatageryCounter - 1
                        val totalSelectedFilters = "" + selectedFilterCatageryCounter
                        if (selectedFilterCatageryCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }

                        //  updateFilterIcon();
                        val categoryName: String? = outletTags?.categoryName
                        for (i in filterSelectedOutletCategories!!.indices) {
                            if (categoryName == filterSelectedOutletCategories!!.get(i)) {
                                filterSelectedOutletCategories!!.removeAt(i)
                            }
                        }
                        //setApiParamsAfterFilter();
                    }

                    override fun onSavePressed() {
                        outletCategoryList = filterOutletCategoryList
                        outletTagsList = filterOutletTagsList
                        outletCertificationList = filterOutletCertificationList
                        supplierList = filterOutletSupplierList
                        updateFilterIcon()
                        setApiParamsAfterFilter()
                    }

                    override fun onResetPressed() {
                        setApiParamsAfterFilterForReset()
                    }

                    override fun onClear() {
                        // setApiParamsAfterFilterclear();
                    }
                },
                object :
                    ProductsFilterListingAdapter.ProductListSelectedFilterCertificationItemClickListener {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterCertificationCounter = selectedFilterCertificationCounter + 1
                        val totalSelectedFilters = "" + selectedFilterCertificationCounter
                        if (selectedFilterCertificationCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }

                        //updateFilterIcon();
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id)) outletTags?.id?.let { it1 ->
                            filterSelectedOutletCertificates!!.add(
                                it1
                            )
                        }
                        //  setApiParamsAfterFilter();
                    }

                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFilterCertificationCounter = selectedFilterCertificationCounter - 1
                        val totalSelectedFilters = "" + selectedFilterCertificationCounter
                        if (selectedFilterCertificationCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }

                        //  updateFilterIcon();
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id)) for (i in filterSelectedOutletCertificates!!.indices) {
                            if (outletTags?.id == filterSelectedOutletCertificates!!.get(i)) {
                                filterSelectedOutletCertificates!!.removeAt(i)
                            }
                        }
                        //setApiParamsAfterFilter();
                    }
                },
                object :
                    ProductsFilterListingAdapter.ProductListSelectedFilterSupplierItemClickListener {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFiltersupplierCounter = selectedFiltersupplierCounter + 1
                        val totalSelectedFilters = "" + selectedFiltersupplierCounter
                        if (selectedFiltersupplierCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }

                        //updateFilterIcon();
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id)) outletTags?.id?.let { it1 ->
                            filterSelectedOutletsupplier!!.add(
                                it1
                            )
                        }
                        //setApiParamsAfterFilter();
                    }

                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                        selectedFiltersupplierCounter = selectedFiltersupplierCounter - 1
                        val totalSelectedFilters = "" + selectedFiltersupplierCounter
                        if (selectedFiltersupplierCounter != 0) {
                            textView?.setVisibility(View.VISIBLE)
                            textView?.setText(totalSelectedFilters)
                        } else {
                            textView?.setVisibility(View.GONE)
                        }

                        // updateFilterIcon();
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id)) for (i in filterSelectedOutletsupplier!!.indices) {
                            if (outletTags?.id == filterSelectedOutletsupplier!!.get(i)) {
                                filterSelectedOutletsupplier!!.removeAt(i)
                            }
                        }
                        //setApiParamsAfterFilter();
                    }
                },
                object :
                    ProductsFilterListingAdapter.ProductListSelectedFilterCategoriesItemClickListeneremptydata {
                    override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {}
                    override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {}
                })
        }
        lytNoFavourites = findViewById<RelativeLayout>(R.id.lyt_no_favourites)
        txtAddFavouritesToDisplay = findViewById<TextView>(R.id.txt_tap_fav_button)
        txtAddFavouritesToDisplay!!.setText(getResources().getString(R.string.txt_try_selecting_fav_button))
        txtNoFavourites = findViewById<TextView>(R.id.txt_no_favourite_yet)
        lytNoItems = findViewById<RelativeLayout>(R.id.lyt_no_items)
        txtNoItemsToDisplay = findViewById<TextView>(R.id.txt_tap_no_item)
        txtAddFavouritesToDisplay!!.setText(getResources().getString(R.string.txt_marketlist_no_items_desc))
        txtNoItems = findViewById<TextView>(R.id.txt_no_items_yet)
        txtSearchCancel = findViewById<TextView>(R.id.search_btn_cancel)
        txtSearchCancel!!.setVisibility(View.GONE)
        lytRecentSearch = findViewById<RelativeLayout>(R.id.lyt_recent_search)
        lytRecentSearch!!.setVisibility(View.GONE)
        lstRecentSearch = findViewById<ListView>(R.id.lst_recent_search)
        lstRecentSearch!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                arg0: AdapterView<*>?, arg1: View,
                position: Int, id: Long
            ) {
                val name = recentSearchList!![position]
                if (name != null) {
                    edtSearch?.setText(name)
                    edtSearch?.getText()?.length?.let { edtSearch?.setSelection(it) }
                }
            }
        })
        txtRecentSearchHeader = findViewById<TextView>(R.id.txt_recent_search)
        txtClearRecentSearch = findViewById<TextView>(R.id.txt_recent_search_clear)
        txtClearRecentSearch!!.setOnClickListener(View.OnClickListener {
            recentSearchList!!.clear()
            setRecentSearchListAdapter(recentSearchList!!)
            setRecentSearchListVisibility()
            SharedPref.removeString(supplierId)
        })
        val jsonText: String = SharedPref.read(supplierId, "")!!
        val lstRecentSearch: Array<String> =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                jsonText,
                Array<String>::class.java
            ) ?: emptyArray()
        if (lstRecentSearch != null) recentSearchList = ArrayList(Arrays.asList(*lstRecentSearch))
        recentSearchList?.let { setRecentSearchListAdapter(it) }
        threeDotLoaderWhite =
            findViewById<CustomLoadingViewWhite>(R.id.spin_kit_loader_product_list)
        edtSearch = findViewById<EditText>(R.id.edit_search)
        edtSearch!!.setHint(getResources().getString(R.string.content_product_list_new_order_edt_search_hint))
        edtSearch!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val searchText: String = edtSearch!!.getText().toString()
                    if (!StringHelper.isStringNullOrEmpty(searchText)) {
                        setRecentSearchList(searchText)
                    }
                    return true
                }
                return false
            }
        })
        edtSearch!!.setCursorVisible(false)
        edtSearch!!.setOnClickListener(View.OnClickListener {
            edtSearch!!.setCursorVisible(true)
            txtSearchCancel!!.setVisibility(View.VISIBLE)
            setRecentSearchListVisibility()
        })
        //added to track search event
        edtSearch!!.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                AnalyticsHelper.logAction(
                    this@AddToOrderActivity,
                    AnalyticsHelper.TAP_ADD_TO_ORDER_SEARCH_PRODUCT
                )
            }
        })
        edtSearch!!.addTextChangedListener(SearchTextWatcher())
        edtSearch!!.setFocusable(false)
        edtSearch!!.setFocusableInTouchMode(false)
        lytNoFilterResults = findViewById<RelativeLayout>(R.id.lyt_no_filter_results)
        txtDeselectFilters = findViewById<TextView>(R.id.txt_deselect_filters)
        txtNoFilterResults = findViewById<TextView>(R.id.txt_no_result)
        lytItemsHeader = findViewById<RelativeLayout>(R.id.lyt_items)
        txtItemsCount = findViewById<TextView>(R.id.txt_item_count)
        txtHeaderAction = findViewById<TextView>(R.id.txt_header_action)
        txtHeaderAction!!.setOnClickListener(View.OnClickListener {
            if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_ITEMS) {
                if (fillAlltoPar) {
                    fillAlltoPar = false
                    txtHeaderAction!!.setText(getString(R.string.txt_fill_all_to_par))
                    deleteAll()
                } else {
                    fillAlltoPar = true
                    txtHeaderAction!!.setText(getString(R.string.txt_remove_all_from_order))
                    selectAll()
                }
            } else {
                if (viewPrice) {
                    viewPrice = false
                    txtHeaderAction!!.setText(getString(R.string.txt_view_item_price))
                } else {
                    viewPrice = true
                    txtHeaderAction!!.setText(getString(R.string.txt_view_stock_on_hand))
                }
                updateProductsAdapterForViewPrice(viewPrice)
            }
        })
        setFont()
        txtSearchCancel!!.setOnClickListener(View.OnClickListener { view ->
            if (products!!.size == 1) {
                txtItemsCount!!.setText(getString(R.string.txt_item_count))
            } else {
                txtItemsCount!!.setText(
                    String.format(
                        getString(R.string.txt_items_count),
                        products!!.size
                    )
                )
            }
            hideKeybaord(view)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            edtSearch!!.setText("")
            edtSearch!!.setCursorVisible(false)
            txtSearchCancel!!.setVisibility(View.GONE)
            lytRecentSearch!!.setVisibility(View.GONE)
            lstProducts!!.setVisibility(View.VISIBLE)
            lytReviewItems!!.setVisibility(View.VISIBLE)
            updateProductsAdapterForViewPrice(viewPrice)
        })
        if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_SUPPLIER_BROWSE || showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_SUPPLIER_SEARCH || showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
            isCalledFromFavourite = false
            txtProductListHeading!!.setText(supplierName)
            calledFrom = ZeemartAppConstants.CALLED_FROM_SUPPLIER_PRODUCT_LIST
            imgInfo!!.visibility = View.VISIBLE
            txtHeaderAction!!.setText(getString(R.string.txt_view_stock_on_hand))
            if (!posIntegration) {
                txtHeaderAction!!.setVisibility(View.GONE)
            }
        } else if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_FAVORITE_BROWSE) {
            isCalledFromFavourite = true
            isShowSupplier = true
            txtProductListHeading!!.setText(getResources().getString(R.string.txt_favourites))
            calledFrom = ZeemartAppConstants.CALLED_FROM_FAVOURITE_PRODUCT_LIST
            imgInfo!!.visibility = View.GONE
            txtHeaderAction!!.setVisibility(View.GONE)
        } else if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_ITEMS) {
            isCalledBelowPar = true
            isShowSupplier = true
            txtProductListHeading!!.setText(getResources().getString(R.string.txt_below_par_items))
            calledFrom = ZeemartAppConstants.CALLED_FROM_BELOW_PAR_PRODUCT_LIST
            imgInfo!!.visibility = View.GONE
            txtHeaderAction!!.setText(getString(R.string.txt_fill_all_to_par))
            txtNoFavourites!!.setText(getString(R.string.txt_no_items_below_par))
        }
        val apiParamsHelper = ApiParamsHelper()
        getProductList(apiParamsHelper, false, "")
        //        setLayoutReviewButton(cartItemCount);
    }

    private fun setApiParamsAfterFilterForReset() {
        selectedFiltersupplierCounter = 0
        selectedFilterTagCounter = 0
        selectedFilterCertificationCounter = 0
        selectedFilterCatageryCounter = 0
        filterSelectedOutletsupplier = ArrayList()
        filterSelectedOutletCertificates = ArrayList()
        filterSelectedOutletTags = ArrayList()
        filterSelectedOutletCategories = ArrayList()
        if (outletCategoryList != null && outletCategoryList!!.size > 0) {
            for (i in outletCategoryList!!.indices) {
                outletCategoryList!![i].isCategorySelected = false
            }
        }
        if (outletTagsList != null && outletTagsList!!.size > 0) {
            for (i in outletTagsList!!.indices) {
                outletTagsList!![i].isCategorySelected = false
            }
        }
        if (outletCertificationList != null && outletCertificationList!!.size > 0) {
            for (i in outletCertificationList!!.indices) {
                outletCertificationList!![i].isCategorySelected = false
            }
        }
        if (supplierList != null && supplierList!!.size > 0) {
            for (i in supplierList!!.indices) {
                supplierList!![i].isCategorySelected = false
            }
        }
        txtNumberOfSelectedFilters?.setVisibility(View.GONE)
        setApiParamsAfterFilter()
    }

    private fun selectAll() {
        if (products != null && products!!.size != 0) {
            val selectedProducts = ArrayList<Product?>()
            for (i in products!!.indices) {
                products!![i].isSelected = true
                if (products!![i].isSelected) {
                    var item: Product
                    val key: String = ProductDataHelper.getKeyProductMap(
                        products!![i].sku,
                        products!![i].priceList?.get(0)?.unitSize!!
                    )
                    if (selectedProductMap!!.containsKey(key)) {
//                        item = selectedProductMap.get(products.get(i).getSku() + products.get(i).getPriceList().get(0).getUnitSize());
                        item =
                            ProductDataHelper.createSelectedProductObjectForBelowPar(products!![i])!!
                        selectedProductMap!![key] = item
                    } else {
                        item =
                            ProductDataHelper.createSelectedProductObjectForBelowPar(products!![i])!!
                        val key1: String =
                            ProductDataHelper.getKeyProductMap(item.sku, item.unitSize)
                        selectedProductMap!![key1] = item
                    }
                    if (item != null) {
                        selectedProducts.add(item)
                    }
                }
            }
            Log.d("null", "updateData: " + selectedProductMap.toString())
            cartItemCount = selectedProductMap!!.size
            setLayoutReviewButton(selectedProductMap!!.size)
            productListAdapter?.onActivityResult(selectedProductMap as MutableMap<String, Product>?)
            productListAdapter?.notifyDataSetChanged()
        }
    }

    private fun deleteAll() {
        if (products != null && products!!.size != 0) {
            for (i in products!!.indices) {
                val key: String = ProductDataHelper.getKeyProductMap(
                    products!![i].sku,
                    products!![i].priceList?.get(0)?.unitSize!!
                )
                val product = selectedProductMap!![key]
                products!![i].isSelected = false
                //reset all the price list uom
                for (j in products!![i].priceList?.indices!!) {
                    products!![i].priceList?.get(j)?.selected = false
                }
                selectedProductMap!!.remove(key)
            }
            cartItemCount = selectedProductMap!!.size
            setLayoutReviewButton(selectedProductMap!!.size)
            productListAdapter?.onActivityResult(selectedProductMap as MutableMap<String, Product>?)
            productListAdapter?.notifyDataSetChanged()
        }
    }

    private fun checkAllSelected() {
        var count = 0
        if (products != null && products!!.size != 0) {
            for (i in products!!.indices) {
                val key: String = ProductDataHelper.getKeyProductMap(
                    products!![i].sku,
                    products!![i].priceList?.get(0)?.unitSize!!
                )
                if (selectedProductMap!!.containsKey(key)) {
                    count = count + 1
                }
            }
            if (count == products!!.size) {
                fillAlltoPar = true
                txtHeaderAction?.setText(getString(R.string.txt_remove_all_from_order))
            }
        } else {
            Log.d("Zeemart", "checkAllSelected: ")
        }
    }

    private fun setApiParamsAfterFilter() {
        val apiParamsHelper = ApiParamsHelper()
        var supIds = ""
        if (filterSelectedOutletTags != null && filterSelectedOutletTags!!.size > 0) apiParamsHelper.setTags(
            filterSelectedOutletTags!!
        )
        if (filterSelectedOutletCategories != null && filterSelectedOutletCategories!!.size > 0) apiParamsHelper.setCategories(
            filterSelectedOutletCategories!!
        )
        if (filterSelectedOutletsupplier != null && filterSelectedOutletsupplier!!.size > 0) {
            supIds = java.lang.String.join(",", filterSelectedOutletsupplier)
        }
        if (filterSelectedOutletCertificates != null && filterSelectedOutletCertificates!!.size > 0) apiParamsHelper.setCertificateId(
            filterSelectedOutletCertificates!!
        )
        getProductList(apiParamsHelper, true, supIds)
    }

    private fun createOrEditDraftOrder(mListener: CreateDraftOrderResponseListener) {
        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
        val selectedProducts: MutableList<Product?> = ArrayList()
        if (selectedProductMap != null && selectedProductMap!!.size > 0) {
            val keys: List<*> = ArrayList<Any?>(
                selectedProductMap!!.keys
            )
            for (i in keys.indices) {
                val item = selectedProductMap!![keys[i]]
                selectedProducts.add(item)
            }
        }
        val selectedProductsJson: String =
            ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProducts)
        val draftProducts: List<CreateDraftOrder.Product> =
            ProductDataHelper.createSelectedProductForDraftObject(selectedProductsJson)
        val createDraftOrder = CreateDraftOrder()
        createDraftOrder.outletId = (SharedPref.read(SharedPref.SELECTED_OUTLET_ID, ""))
        createDraftOrder.products = (draftProducts)
        OrderManagement.createDraftOrdersBasedOnSKUs(
            this@AddToOrderActivity,
            requestBody(createDraftOrder),
            object : OrderManagement.CreateDraftOrderBasedOnSKUsListener {
                override fun onSuccessResponse(status: String?) {
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                    mListener.doNavigation(status)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                }
            },
            createDraftOrder
        )
    }

    private fun requestBody(createDraftOrder: CreateDraftOrder): String {
        return ZeemartBuyerApp.gsonExposeExclusive.toJson(createDraftOrder)
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            edtSearch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNumberOfSelectedFilters,
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
        ZeemartBuyerApp.setTypefaceView(
            txtProductListHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRecentSearchHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtClearRecentSearch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSearchCancel,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAddFavouritesToDisplay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoFavourites,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoItemsToDisplay,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoItems,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTotalItems,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtItemsCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtHeaderAction,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun getProductList(
        apiParamsHelper: ApiParamsHelper,
        isCalledFromFilters: Boolean,
        supplierIds: String
    ) {
        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
        if (isCalledFromFavourite) {
            apiParamsHelper.setIsFavourite(true)
            if (StringHelper.isStringNullOrEmpty(supplierIds)) {
                apiParamsHelper.setSupplierId(supplierId!!)
            } else {
                apiParamsHelper.setSupplierId(supplierIds)
            }
        } else if (isCalledBelowPar) {
            apiParamsHelper.setIsBelowPar(true)
            if (StringHelper.isStringNullOrEmpty(supplierIds)) {
                apiParamsHelper.setSupplierId(supplierId!!)
            } else {
                apiParamsHelper.setSupplierId(supplierIds)
            }
        } else {
            apiParamsHelper.setSupplierId(supplierId!!)
        }
        outletID?.let {
            MarketListApi.retrieveMarketListOutlet(
                this,
                apiParamsHelper,
                it,
                object : ProductListBySupplierListner {
                    override fun onSuccessResponse(productList: ProductListBySupplier?) {
                        edtSearch?.setFocusable(true)
                        edtSearch?.setFocusableInTouchMode(true)
                        if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
                            addProductsList(productList, isCalledFromFilters)
                            setLayoutReviewButton(cartItemCount)
                            threeDotLoaderWhite?.setVisibility(View.GONE)
                        } else {
                            getDrafts(productList!!, isCalledFromFilters)
                        }
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        threeDotLoaderWhite?.setVisibility(View.GONE)
                    }
                })
        }
    }

    private fun addProductsList(productList: ProductListBySupplier?, isCalledFromFilters: Boolean) {
        if (productList != null && productList.data != null && productList.data!!.products != null && productList.data!!.products?.size!! > 0) {
            products = ArrayList<ProductDetailBySupplier>()
            for (i in productList.data!!.products?.indices!!) {
                if (productList.data!!.products?.get(i)?.priceList != null && productList.data!!.products?.get(
                        i
                    )?.priceList?.size!! > 1
                ) {
                    for (j in productList.data!!.products?.get(i)?.priceList?.indices!!) {
                        if (productList.data!!.products?.get(i)?.priceList?.get(j)?.status == "visible") {
                            val productUom: ProductDetailBySupplier? =
                                ZeemartBuyerApp.fromJson<ProductDetailBySupplier>(
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                        productList.data!!.products?.get(
                                            i
                                        )
                                    ), ProductDetailBySupplier::class.java
                                )
                            val productPriceLists: MutableList<ProductPriceList> =
                                ArrayList<ProductPriceList>()
                            productList.data!!.products?.get(i)?.priceList?.get(j)
                                ?.let { productPriceLists.add(it) }
                            productUom?.priceList = productPriceLists
                            productUom?.isHasMultipleUom = true
                            if (productUom != null) {
                                products!!.add(productUom)
                            }
                        }
                    }
                } else {
                    if (productList.data!!.products?.get(i)?.priceList != null && productList.data!!.products?.get(
                            i
                        )?.priceList?.get(0)?.status == "visible"
                    ) {
                        productList.data!!.products?.get(i)?.let { products!!.add(it) }
                    }
                }
            }
            //get the fav on top + sorted product list
            products = (ProductDataHelper.getDisplayProductList(
                this@AddToOrderActivity,
                products!!,
                outletID,
                supplierId
            ) as MutableList<ProductDetailBySupplier>?)!!
            setSupplierName()
            if (selectedProductList != null && selectedProductList!!.size > 0) {
                for (i in selectedProductList!!.indices) {
                    for (j in products!!.indices) {
                        if (products!![j].sku == selectedProductList!![i].sku && products!![j].priceList?.get(
                                0
                            )?.unitSize == selectedProductList!![i].unitSize
                        ) {
                            products!![j].isSelected = true
                            //                            if (showProductListCalledFrom.equals(ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER)) {
//                                cartItemCount = cartItemCount + 1;
//                                setLayoutReviewButton(cartItemCount);
//                            }
                            break
                        }
                    }
                }
            }
            updateProductsAdapter(false)
            if (!isCalledFromFilters) {
                val uniqueFilterTagsSet: MutableSet<String> = HashSet()
                val uniqueFilterCategoriesSet: MutableSet<String> = HashSet()
                val uniqueFilterSupplierSet: MutableSet<String> = HashSet()
                val uniqueFilterCertificationSet: MutableSet<OutletTags> = HashSet<OutletTags>()
                if (products != null && products!!.size > 0) {
                    for (i in products!!.indices) {
                        if (products!![i].tags != null) {
                            for (tag in products!![i].tags!!) {
                                if (!StringHelper.isStringNullOrEmpty(tag)) uniqueFilterTagsSet.add(
                                    tag
                                )
                            }
                        }
                        if (!StringHelper.isStringNullOrEmpty(products!![i].categoryPath)) {
                            val twoStringArray: Array<String>? =
                                products!![i].categoryPath?.split(":".toRegex(), limit = 2)
                                    ?.toTypedArray() //the main line
                            val categoryPath = twoStringArray?.get(0)
                            if (categoryPath != null) {
                                uniqueFilterCategoriesSet.add(categoryPath)
                            }
                        }
                        if (!StringHelper.isStringNullOrEmpty(products!![i].supplierId)) {
                            products!![i].supplierId?.let { uniqueFilterSupplierSet.add(it) }
                        }
                        if (products!![i].certifications != null) {
                            for (j in products!![i].certifications?.indices!!) {
                                if (!StringHelper.isStringNullOrEmpty(
                                        products!![i].certifications?.get(
                                            j
                                        )?.id
                                    ) && !StringHelper.isStringNullOrEmpty(
                                        products!![i].certifications?.get(j)?.name
                                    )
                                ) {
                                    val outletTags = OutletTags()
                                    outletTags.id = products!![i].certifications?.get(j)?.id
                                    outletTags.categoryName =
                                        products!![i].certifications?.get(j)?.name
                                    uniqueFilterCertificationSet.add(outletTags)
                                }
                            }
                        }
                    }
                }
                if (uniqueFilterTagsSet.size > 0) {
                    val outletTags: ArrayList<OutletTags> = ArrayList<OutletTags>()
                    for (tag in uniqueFilterTagsSet) {
                        val categoryObj = OutletTags()
                        categoryObj.categoryName = tag
                        categoryObj.isCategorySelected = false
                        outletTags.add(categoryObj)
                    }
                    OutletTags.sortByCategoryName(outletTags)
                    outletTagsList = outletTags
                }
                if (uniqueFilterCategoriesSet.size > 0) {
                    val outletTags: ArrayList<OutletTags> = ArrayList<OutletTags>()
                    for (tag in uniqueFilterCategoriesSet) {
                        val categoryObj = OutletTags()
                        categoryObj.categoryName = tag
                        categoryObj.isCategorySelected = false
                        outletTags.add(categoryObj)
                    }
                    OutletTags.sortByCategoryName(outletTags)
                    outletCategoryList = outletTags
                }
                if (detailSuppliersList != null) {
                    val outletTags: ArrayList<OutletTags> = ArrayList<OutletTags>()
                    for (supplier in uniqueFilterSupplierSet) {
                        val outletTag = OutletTags()
                        outletTag.id = supplier
                        for (i in detailSuppliersList!!.indices) {
                            if (detailSuppliersList!![i].supplier.supplierId == supplier) {
                                outletTag.categoryName =
                                    detailSuppliersList!![i].supplier.supplierName
                                outletTags.add(outletTag)
                                break
                            }
                        }
                        OutletTags.sortByCategoryName(outletTags)
                        supplierList = outletTags
                    }
                }
                if (uniqueFilterCertificationSet.size > 0) {
                    outletCertificationList!!.addAll(uniqueFilterCertificationSet)
                }
            }
            if (products!!.size == 1) {
                txtItemsCount?.setText(getString(R.string.txt_item_count))
            } else {
                txtItemsCount?.setText(
                    String.format(
                        getString(R.string.txt_items_count),
                        products!!.size
                    )
                )
            }
            if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_BELOW_PAR_ITEMS) {
                checkAllSelected()
            }
            lstProducts?.setVisibility(View.VISIBLE)
            lytItemsHeader?.setVisibility(View.VISIBLE)
            lytNoFavourites?.setVisibility(View.GONE)
        } else {
            lytItemsHeader?.setVisibility(View.GONE)
            if (isCalledFromFavourite && StringHelper.isStringNullOrEmpty(
                    edtSearch?.getText().toString()
                )
            ) {
                lytNoFavourites?.setVisibility(View.VISIBLE)
                lstProducts?.setVisibility(View.GONE)
            } else if (isCalledBelowPar && StringHelper.isStringNullOrEmpty(
                    edtSearch?.getText().toString()
                )
            ) {
                lytNoFavourites?.setVisibility(View.VISIBLE)
                txtNoFavourites?.setText(getString(R.string.txt_no_items_below_par))
                txtNoItemsToDisplay?.setText(getString(R.string.txt_no_items_below_par))
                ZeemartBuyerApp.setTypefaceView(
                    txtNoFavourites,
                    ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
                )
                txtNoFavourites?.setVisibility(View.VISIBLE)
                txtAddFavouritesToDisplay?.setVisibility(View.GONE)
                lstProducts?.setVisibility(View.GONE)
            } else {
                lytNoItems?.setVisibility(View.VISIBLE)
                lstProducts?.setVisibility(View.GONE)
                btnFilter!!.visibility = View.GONE
                txtNumberOfSelectedFilters?.setVisibility(View.GONE)
            }
        }
    }

    private fun setSupplierName() {
        if (lstSuppliers != null && lstSuppliers!!.size > 0 && products != null && products!!.size > 0) {
            for (i in lstSuppliers!!.indices) {
                for (j in products!!.indices) {
                    if (lstSuppliers!![i].supplier.supplierId == products!![j].supplierId) {
                        products!![j].supplierName = lstSuppliers!![i].supplier.supplierName
                        products!![j].supplier = lstSuppliers!![i].supplier
                    }
                }
            }
        }
    }

    private fun getDrafts(productList: ProductListBySupplier?, isCalledFromFilters: Boolean) {
        val supplierIdArray = arrayOfNulls<String>(
            lstSuppliers!!.size
        )
        for (i in lstSuppliers!!.indices) {
            supplierIdArray[i] = lstSuppliers!![i].supplier.supplierId
        }
        OrderHelper.returnDraftOrdersForOutlet(
            this@AddToOrderActivity,
            supplierIdArray,
            object : OrderHelper.OutletDraftOrdersListener {
                override fun onSuccessResponse(orders: List<Orders>?) {
                    if (orders != null && orders.isNotEmpty()) {
                        selectedProductList = ArrayList()
                        for (i in orders.indices) {
                            orders[i].products?.let { selectedProductList!!.addAll(it) }
                        }
                        selectedProductMap =
                            ProductDataHelper.createSelectedProductMap(selectedProductList) as MutableMap<String?, Product?>
                    }
                    cartItemCount = OrderHelper.getCartItemsCount(orders)
                    setLayoutReviewButton(cartItemCount)
                    addProductsList(productList, isCalledFromFilters)
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                    addProductsList(productList, isCalledFromFilters)
                }
            })
    }

    private fun setLayoutReviewButton(items: Int) {
        if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
            if (selectedProductList != null && selectedProductList!!.size > 0) {
                lytOrderSummary?.setVisibility(View.VISIBLE)
                lytOrderSummary?.setClickable(true)
                lytOrderSummary?.setBackgroundResource(R.drawable.blue_rounded_corner)
                txtTotalItems?.setText(getResources().getString(R.string.txt_done))
            } else {
                if (items > 0) {
                    lytOrderSummary?.setVisibility(View.VISIBLE)
                    lytOrderSummary?.setClickable(true)
                    lytOrderSummary?.setBackground(getResources().getDrawable(R.drawable.blue_rounded_corner))
                    txtTotalItems?.setText(getResources().getString(R.string.txt_done))
                } else {
                    lytOrderSummary?.setVisibility(View.VISIBLE)
                    lytOrderSummary?.setClickable(false)
                    lytOrderSummary?.setBackground(getResources().getDrawable(R.drawable.dark_grey_rounded_corner))
                    txtTotalItems?.setText(getResources().getString(R.string.txt_done))
                }
            }
        } else {
            if (items > 0) {
                lytOrderSummary?.setVisibility(View.VISIBLE)
                lytOrderSummary?.setClickable(true)
                lytOrderSummary?.setBackground(getResources().getDrawable(R.drawable.blue_rounded_corner))
                if (items == 1) {
                    txtTotalItems?.setText(
                        String.format(
                            getResources().getString(R.string.txt_review_order_cart_item),
                            items
                        )
                    )
                } else {
                    txtTotalItems?.setText(
                        String.format(
                            getResources().getString(R.string.txt_review_order_cart_items),
                            items
                        )
                    )
                }
            } else {
                lytOrderSummary?.setVisibility(View.VISIBLE)
                lytOrderSummary?.setClickable(false)
                lytOrderSummary?.setBackground(getResources().getDrawable(R.drawable.dark_grey_rounded_corner_review_btn))
                txtTotalItems?.setText(getResources().getString(R.string.txt_no_items_in_cart))
            }
        }
    }

    private fun updateProductsAdapter(isCalledFromSearchText: Boolean) {
//        if ((filterSelectedOutletTags != null && filterSelectedOutletTags.size() > 0) || ((filterSelectedOutletCategories != null && filterSelectedOutletCategories.size() > 0))) {
//            List<ProductDetailBySupplier> filteredProductsByTagList = new ArrayList<>();
//            for (int i = 0; i < products.size(); i++) {
//                for (int j = 0; j < filterSelectedOutletTags.size(); j++) {
//                    if (products.get(i).getOutletTags() != null)
//                        if ((products.get(i).getOutletTags()).containsAll(filterSelectedOutletTags)) {
//                            if (!(filteredProductsByTagList.contains(products.get(i))))
//                                filteredProductsByTagList.add(products.get(i));
//                        }
//                    for (int k = 0; k < filterSelectedOutletCategories.size(); k++)
//                        if ((products.get(i).getCategoryPath()).contains(filterSelectedOutletCategories.get(k))) {
//                            if (!(filteredProductsByTagList.contains(products.get(i))))
//                                filteredProductsByTagList.add(products.get(i));
//                        }
//                }
//            }
//            if (filteredProductsByTagList.size() == 0) {
//                lstProducts.setVisibility(View.GONE);
//                lytNoFilterResults.setVisibility(View.VISIBLE);
//                lytNoFavourites.setVisibility(View.GONE);
//            } else {
//                lytNoFilterResults.setVisibility(View.GONE);
//                if (StringHelper.isStringNullOrEmpty(edtSearch.getText().toString()) && isCalledFromSearchText) {
//                    lstProducts.setVisibility(View.GONE);
//                } else {
//                    lstProducts.setVisibility(View.VISIBLE);
//                }
//            }
//            if (edtSearch.getText().toString().length() == 0) {
//                productListAdapter = new SupplierProductListAdapter(false, this, filteredProductsByTagList, outletID, calledFrom, supplierName, supplierId, supplierDetails, selectedProductMap, null, null, new SupplierProductListAdapter.SelectedProductsListener() {
//                    @Override
//                    public void onProductSelected(ProductDetailBySupplier productDetailBySupplier) {
//                        cartItemCount = cartItemCount + 1;
//                        setLayoutReviewButton(cartItemCount);
//
//                    }
//
//                    @Override
//                    public void onProductDeselected(ProductDetailBySupplier productDetailBySupplier) {
//                        removeSelectedProductFromDraftList(productDetailBySupplier);
//                        cartItemCount = cartItemCount - 1;
//                        setLayoutReviewButton(cartItemCount);
//                    }
//
//                    @Override
//                    public void onProductSelectedForRecentSearch(String productName) {
//                        if (!StringHelper.isStringNullOrEmpty(productName) && !StringHelper.isStringNullOrEmpty(edtSearch.getText().toString())) {
//                            setRecentSearchList(productName);
//                        }
//                    }
//                });
//                lstProducts.setAdapter(productListAdapter);
//            } else {
//                filterProductsOnSearchedText(filteredProductsByTagList);
//            }
//        } else {
//            inside the else condition again checking another condition
        if (edtSearch?.text.toString().isEmpty()) {
            productListAdapter = SupplierProductListAdapter(
                false,
                this,
                products ?: emptyList(), // Provide an empty list as a default if products is null
                outletID ?: "", // Provide an empty string as a default if outletID is null
                calledFrom ?: "", // Provide an empty string as a default if calledFrom is null
                supplierName ?: "",
                supplierLogo ?: "",
                supplierId ?: "",
                supplierDetails!!,
                selectedProductMap!! as MutableMap<String, Product>?,
                null,
                null,
                viewPrice!!,
                object : SupplierProductListAdapter.SelectedProductsListener {
                    override fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?) {
                        cartItemCount = cartItemCount + 1
                        setLayoutReviewButton(cartItemCount)
                    }

                    override fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?) {
                        removeSelectedProductFromDraftList(productDetailBySupplier!!)
                        if (cartItemCount != 0) cartItemCount = cartItemCount - 1
                        setLayoutReviewButton(cartItemCount)
                    }

                    override fun onProductSelectedForRecentSearch(productName: String?) {
                        if (!StringHelper.isStringNullOrEmpty(productName) && !StringHelper.isStringNullOrEmpty(
                                edtSearch?.getText().toString()
                            )
                        ) {
                            setRecentSearchList(productName!!)
                        }
                    }
                })
            lstProducts?.adapter = productListAdapter
        } else {
            filterProductsOnSearchedText(products)
        }
    }

    private fun updateProductsAdapterForViewPrice(isPrice: Boolean) {
        productListAdapter?.viewPriceCalled(isPrice)
    }

    //    }
    private fun filterProductsOnSearchedText(products: List<ProductDetailBySupplier>?) {
        val searchedString: String = edtSearch?.getText().toString()
        val searchStringArray = searchedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val filteredList: MutableList<ProductDetailBySupplier> =
            ArrayList<ProductDetailBySupplier>()
        if (products != null) {
            for (i in products.indices) {
                val productCodeTag = products[i].supplierProductCode?.let {
                    products[i].supplierName?.let { it1 ->
                        getProductCodeAndTag(
                            it,
                            products[i].tags,
                            it1
                        )
                    }
                }
                var searchStringFound = false
                for (j in searchStringArray.indices) {
                    searchStringFound =
                        if (products[i].productName?.lowercase(Locale.getDefault())?.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            ) == true || productCodeTag?.lowercase(
                                Locale.getDefault()
                            )?.contains(
                                searchStringArray[j].lowercase(Locale.getDefault())
                            ) == true || products[i].productCustomName != null && products[i].productCustomName
                                ?.lowercase(
                                    Locale.getDefault()
                                )?.contains(
                                    searchStringArray[j].lowercase(Locale.getDefault())
                                ) == true
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
            lstProducts?.setVisibility(View.INVISIBLE)
            lytNoFilterResults?.setVisibility(View.VISIBLE)
            lytNoFavourites?.setVisibility(View.GONE)
        } else {
            lytNoFilterResults?.setVisibility(View.GONE)
            lstProducts?.setVisibility(View.VISIBLE)
        }
        productListAdapter = SupplierProductListAdapter(
            false,
            this,
            filteredList,
            outletID!!,
            calledFrom!!,
            supplierName!!,
            supplierLogo!!,
            supplierId!!,
            supplierDetails,
            selectedProductMap as MutableMap<String, Product>?,
            searchStringArray,
            null,
            viewPrice,
            object : SupplierProductListAdapter.SelectedProductsListener {
                override fun onProductSelected(productDetailBySupplier: ProductDetailBySupplier?) {
                    cartItemCount = cartItemCount + 1
                    setLayoutReviewButton(cartItemCount)
                }

                override fun onProductDeselected(productDetailBySupplier: ProductDetailBySupplier?) {
                    removeSelectedProductFromDraftList(productDetailBySupplier!!)
                    if (cartItemCount != 0) cartItemCount = cartItemCount - 1
                    setLayoutReviewButton(cartItemCount)
                }

                override fun onProductSelectedForRecentSearch(productName: String?) {
                    if (!StringHelper.isStringNullOrEmpty(productName) && !StringHelper.isStringNullOrEmpty(
                            edtSearch?.getText().toString()
                        )
                    ) {
                        setRecentSearchList(productName!!)
                    }
                }
            })
        lstProducts?.setAdapter(productListAdapter)
        if (filteredList.size == 1) {
            txtItemsCount?.setText(getString(R.string.txt_item_count))
        } else {
            txtItemsCount?.setText(
                String.format(
                    getString(R.string.txt_items_count),
                    filteredList.size
                )
            )
        }
    }

    private fun getProductCodeAndTag(
        productCode: String,
        tags: List<String>?,
        supplierName: String
    ): String {
        var productCodeTag = ""
        if (!StringHelper.isStringNullOrEmpty(productCode)) {
            productCodeTag = productCode
        } else if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_FAVORITE_BROWSE) {
            productCodeTag = if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
                supplierName
            } else {
                "$productCodeTag • $supplierName"
            }
        } else if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_SUPPLIER_BROWSE) {
            if (tags != null && tags.size > 0) {
                var tagValue = ""
                for (i in tags.indices) {
                    tagValue = tagValue + tags[i] + ","
                }
                if (tagValue.length > 0) {
                    val attributeValuesAll = tagValue.substring(0, tagValue.length - 1)
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

    internal inner class SearchTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//          ProductListNewOrderActivity.this.productListAdapter.getFilter().filter(s);
            if (products != null) {
                if (products!!.size == 1) {
                    txtItemsCount?.setText(getString(R.string.txt_item_count))
                } else {
                    txtItemsCount?.setText(
                        String.format(
                            getString(R.string.txt_items_count),
                            products!!.size
                        )
                    )
                }
                setRecentSearchListVisibility()
                updateProductsAdapter(true)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private fun setRecentSearchListVisibility() {
        if (StringHelper.isStringNullOrEmpty(edtSearch?.getText().toString())) {
            if (recentSearchList != null && recentSearchList!!.size > 0) {
                lytRecentSearch?.setVisibility(View.VISIBLE)
            } else {
                lytRecentSearch?.setVisibility(View.GONE)
            }
            //lstProducts.setVisibility(View.GONE);
            lytNoFilterResults?.setVisibility(View.GONE)
            threeDotLoaderWhite?.setVisibility(View.GONE)
            lytReviewItems?.setVisibility(View.GONE)
        } else {
            lytRecentSearch?.setVisibility(View.GONE)
            lstProducts?.setVisibility(View.VISIBLE)
            txtSearchCancel?.setVisibility(View.VISIBLE)
            lytReviewItems?.setVisibility(View.VISIBLE)
        }
    }

    private fun setRecentSearchListAdapter(recentSearchLst: MutableList<String>) {
        if (recentSearchLst.size > 0) for (i in recentSearchLst.indices) {
            if (recentSearchLst.size > RECENT_SEARCH_LIMIT) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            getApplicationContext(),
            android.R.layout.simple_list_item_1, recentSearchLst
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View = super.getView(position, convertView, parent)
                val typeface: Typeface = Typeface.createFromAsset(
                    getContext().getAssets(),
                    "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
                )
                val text: TextView = view.findViewById<View>(android.R.id.text1) as TextView
                text.setTypeface(typeface)
                return view
            }
        }
        lstRecentSearch!!.adapter = adapter
    }

    override fun onBackPressed() {
        threeDotLoaderWhite?.setVisibility(View.GONE)
        if (threeDotLoaderWhite?.getVisibility() == View.GONE) {
            if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
                finish()
            } else {
                createOrEditDraftOrder(object : CreateDraftOrderResponseListener {
                    override fun doNavigation(response: String?) {
                        finish()
                    }
                })
            }
        }
        if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_SUPPLIER_SEARCH) {
            val newIntent = Intent(this@AddToOrderActivity, SearchForNewOrder::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_SUPPLIER_ADD_TO_ORDER
            )
            setResult(Activity.RESULT_OK, newIntent)
        }
    }

    private fun setRecentSearchList(name: String) {
        if (!recentSearchList!!.contains(name)) {
            recentSearchList!!.add(TOP, name)
        }
        saveRecentSearchInSharedPref(recentSearchList)
        setRecentSearchListAdapter(recentSearchList!!)
    }

    private fun saveRecentSearchInSharedPref(recentSearchLst: MutableList<String>?) {
        for (i in recentSearchLst!!.indices) {
            if (recentSearchLst.size > RECENT_SEARCH_LIMIT) {
                recentSearchLst.removeAt(recentSearchLst.size - 1)
            }
        }
        val recentSearchJson: String = ZeemartBuyerApp.gsonExposeExclusive.toJson(recentSearchList)
        SharedPref.write(supplierId, recentSearchJson)
    }

    private fun getSelectedCount(outletTagsList: List<OutletTags>?): Int {
        var filterCount = 0
        if (outletTagsList != null && outletTagsList.size > 0) {
            for (i in outletTagsList.indices) {
                if (outletTagsList[i].isCategorySelected) {
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
                if (outletCategoryList!![i].isCategorySelected) {
                    filterCount = filterCount + 1
                }
            }
        }
        if (outletTagsList != null && outletTagsList!!.size > 0) {
            for (i in outletTagsList!!.indices) {
                if (outletTagsList!![i].isCategorySelected) {
                    filterCount = filterCount + 1
                }
            }
        }
        if (outletCertificationList != null && outletCertificationList!!.size > 0) {
            for (i in outletCertificationList!!.indices) {
                if (outletCertificationList!![i].isCategorySelected) {
                    filterCount = filterCount + 1
                }
            }
        }
        if (supplierList != null && supplierList!!.size > 0) {
            for (i in supplierList!!.indices) {
                if (supplierList!![i].isCategorySelected) {
                    filterCount = filterCount + 1
                }
            }
        }
        if (filterCount != 0) {
            txtNumberOfSelectedFilters?.setVisibility(View.VISIBLE)
            txtNumberOfSelectedFilters?.setText(filterCount.toString() + "")
        } else {
            txtNumberOfSelectedFilters?.setVisibility(View.GONE)
        }
    }

    private interface CreateDraftOrderResponseListener {
        fun doNavigation(response: String?)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == OrderDetailsActivity.IS_STATUS_CHANGED) {
            val bundle: Bundle? = data?.getExtras()
            if (bundle != null) {
                if (bundle.containsKey(ZeemartAppConstants.PRODUCT_DETAILS_JSON)) {
                    val productDetailJson: String? =
                        bundle.getString(ZeemartAppConstants.PRODUCT_DETAILS_JSON)
                    val productDetails: ProductDetailBySupplier =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson<ProductDetailBySupplier>(
                            productDetailJson,
                            ProductDetailBySupplier::class.java
                        )
                    if (productDetails != null) refreshParticularProduct(productDetails)
                }
            }
        }
        if (requestCode == ZeemartAppConstants.RequestCode.ProductDetailsActivity) {
            if (resultCode == -1) {
                val type = object : TypeToken<MutableMap<String?, Product?>>() {}.type
                selectedProductMap =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<MutableMap<String?, Product?>>(
                        data?.getStringExtra(ZeemartAppConstants.SELECTED_MAPPING), type
                    )
                val json: String? = data?.getStringExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST)
                val productsList: ProductDetailBySupplier =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<ProductDetailBySupplier>(
                        json,
                        ProductDetailBySupplier::class.java
                    )
                for (i in products!!.indices) {
                    if (products!![i].sku == productsList.sku && products!![i].productName == productsList.productName && products!![i].priceList?.get(
                            0
                        )?.unitSize == productsList.priceList?.get(0)?.unitSize
                    ) {
                        products!![i].isSelected = productsList.isSelected
                        products!![i].priceList = productsList.priceList
                        break
                    }
                }
                updateData()
                productListAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun updateData() {
        if (products != null && products!!.size != 0) {
            val selectedProducts = ArrayList<Product>()
            for (i in products!!.indices) {
                if (products!![i].isSelected) {
                    var item: Product?
                    val key: String = ProductDataHelper.getKeyProductMap(
                        products!![i].sku,
                        products!![i].priceList?.get(0)?.unitSize!!
                    )
                    if (selectedProductMap!!.containsKey(key)) {
                        item =
                            selectedProductMap!![products!![i].sku + products!![i].priceList?.get(0)?.unitSize]
                    } else {
                        item = ProductDataHelper.createSelectedProductObject(products!![i])
                        val key1: String =
                            ProductDataHelper.getKeyProductMap(item?.sku, item?.unitSize!!)
                        selectedProductMap!![key1] = item
                    }
                    if (item != null) {
                        selectedProducts.add(item)
                    }
                }
            }
            cartItemCount = selectedProductMap!!.size
            setLayoutReviewButton(selectedProductMap!!.size)
            productListAdapter?.onActivityResult(selectedProductMap as MutableMap<String, Product>?)
            productListAdapter?.notifyDataSetChanged()
        }
    }

    private fun refreshParticularProduct(productDetailBySupplier: ProductDetailBySupplier) {
        if (products != null) for (i in products!!.indices) {
            if (productDetailBySupplier.sku.equals(products!![i].sku, ignoreCase = true)) {
                products!![i].isFavourite = productDetailBySupplier.isFavourite
            }
        }
        if (lstProducts?.getAdapter() != null) lstProducts!!.getAdapter()!!.notifyDataSetChanged()
    }

    private fun removeSelectedProductFromDraftList(productDetailBySupplier: ProductDetailBySupplier) {
        if (selectedProductList != null && selectedProductList!!.size > 0) {
            for (i in selectedProductList!!.indices) {
                if (selectedProductList!![i].sku == productDetailBySupplier.sku) {
                    selectedProductList!!.remove(selectedProductList!![i])
                }
            }
        }
    }

    private fun hideKeybaord(v: View) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.applicationWindowToken, 0)
    }

    companion object {
        private const val RECENT_SEARCH_LIMIT = 20
        private const val TOP = 0
        private const val SET_IMAGE_SPAN_START = 29
        private const val SET_IMAGE_SPAN_END = 30
    }
}