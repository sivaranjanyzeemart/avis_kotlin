package zeemart.asia.buyers.essentials

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
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
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter.ProductListSelectedFilterCategoriesItemClickListeneremptydata
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter.ProductListSelectedFilterSupplierItemClickListener
import zeemart.asia.buyers.adapter.SupplierProductListAdapter
import zeemart.asia.buyers.companies.CompanyVerificationRequestActivity
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.DialogHelper.ShowFilterTagsDialog
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper.DealsAndEssentialsProductsScrollCallback
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.pagination.DealsAndEssentialsProductsListScrollHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.RetrieveCompaniesResponse.SpecificCompany
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.Settings
import zeemart.asia.buyers.models.marketlist.Settings.Gst
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimportimportimport.EssentialProductsPaginated
import zeemart.asia.buyers.network.CompaniesApi
import zeemart.asia.buyers.network.CompaniesApi.SpecificCompanyResponseListener
import zeemart.asia.buyers.network.EssentialsApi
import zeemart.asia.buyers.network.EssentialsApi.EssentialsProductsResponseListener
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseListener
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.OutletsApi.GetSpecificOutletResponseListener
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.OrderDetailsActivity
import zeemart.asia.buyers.orders.createorders.SearchForNewOrder
import java.util.*

/**
 * Created by RajPrudhvi on 4/20/2018.
 */
class EssentialsProductListActivity : AppCompatActivity() {
    private var imgBack: ImageView? = null
    private var edtSearch: EditText? = null
    private var txtEssentialNameHeader: TextView? = null
    private var txtOutLetNameHeader: TextView? = null
    private var lytOrderSummary: RelativeLayout? = null
    private var txtTotalItems: TextView? = null
    private var imgEssentials: ImageView? = null
    private var lytMinCart: RelativeLayout? = null
    private var txtAbout: TextView? = null
    private var txtMinimumCartValue: TextView? = null
    private var txtPayUpFront: TextView? = null
    private var lstProductList: RecyclerView? = null
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    private var essentialsId: String? = null
    private var edtSearchClear: ImageView? = null
    private var lstEssentials: Essential? = null
    private var products: MutableList<ProductDetailBySupplier>? = null
    private var productListAdapter: SupplierProductListAdapter? = null
    private var cartItemCount = 0
    private var selectedProductList: List<Product>? = null
    private var selectedProductMap: MutableMap<String, Product?> = HashMap()
    private var showProductListCalledFrom: String? = null
    private var productList: List<EssentialProductsPaginated.EssentialsProducts>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var productDetailBySupplier: ProductDetailBySupplier? = null
    private var viewOrderScrollHelper: DealsAndEssentialsProductsListScrollHelper? = null
    private var lytHiddenContent: RelativeLayout? = null
    private var btnFilter: ImageView? = null
    private var txtNumberOfSelectedFilters: TextView? = null
    private var outletCategoryList: List<OutletTags?>? = ArrayList()
    private var filterOutletCategoryList: MutableList<OutletTags?> = ArrayList()
    private var outletCertificationList: MutableList<OutletTags?>? = ArrayList()
    private var filterOutletCertificationList: MutableList<OutletTags?> = ArrayList()
    private var selectedFilterCatageryCounter = 0
    private var selectedFilterCertificationCounter = 0
    private var filterSelectedOutletCategories: MutableList<String>? = null
    private val filterOutletCategories: List<String> = ArrayList()
    private var filterSelectedOutletCertificates: MutableList<String>? = null
    private var lytBusinessVerifying: RelativeLayout? = null
    private var txtVerifyingHeader: TextView? = null
    private var txtVerifyingContent: TextView? = null
    private var lytBusinessRequest: RelativeLayout? = null
    private var txtRequestHeader: TextView? = null
    private var txtRequestContent: TextView? = null
    private var btnRequest: Button? = null
    private var lstCompany: Companies? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_essentials_product_list_screen)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.ESSENTIALS_ID)) {
                essentialsId = bundle.getString(ZeemartAppConstants.ESSENTIALS_ID).toString()
            }
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
                        ProductDataHelper.createSelectedProductMap(selectedProductList) as MutableMap<String, Product?>
                }
            }
            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
                showProductListCalledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM).toString()
            }
            if (bundle.containsKey(ZeemartAppConstants.PRODUCT_DETAILS_JSON)) {
                productDetailBySupplier = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    bundle.getString(ZeemartAppConstants.PRODUCT_DETAILS_JSON),
                    ProductDetailBySupplier::class.java
                )
            }
        }
        lytBusinessVerifying = findViewById(R.id.lyt_company_verifying)
        txtVerifyingHeader = findViewById(R.id.txt_order_payable1)
        txtVerifyingContent = findViewById(R.id.txt_order_payable2)
        lytBusinessRequest = findViewById(R.id.lyt_company_verify_request)
        txtRequestHeader = findViewById(R.id.txt_request_header)
        txtRequestContent = findViewById(R.id.txt_request_content)
        btnRequest = findViewById(R.id.btn_request)
        btnRequest!!.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(
                this@EssentialsProductListActivity,
                CompanyVerificationRequestActivity::class.java
            )
            newIntent.putExtra(
                ZeemartAppConstants.COMPANY,
                ZeemartBuyerApp.gsonExposeExclusive.toJson(lstCompany)
            )
            startActivityForResult(newIntent, REQUEST_CODE_BUSINESS_STATUS_CHANGED)
        })
        setCompanyLayoutView()
        txtNumberOfSelectedFilters = findViewById(R.id.txt_number_of_selected_filters)
        txtNumberOfSelectedFilters!!.setVisibility(View.GONE)
        btnFilter = findViewById(R.id.btn_filter)
        lytHiddenContent = findViewById(R.id.lyt_hidden_content)
        imgBack = findViewById(R.id.products_back_btn)
        imgBack!!.setOnClickListener(View.OnClickListener { createExitDialog() })
        edtSearch = findViewById(R.id.edit_search)
        txtEssentialNameHeader = findViewById(R.id.txt_product_list_supplier_heading)
        txtOutLetNameHeader = findViewById(R.id.txt_product_list_outlet_heading)
        lytOrderSummary = findViewById(R.id.lyt_review_add_to_order)
        txtTotalItems = lytOrderSummary!!.findViewById(R.id.txt_review_total_items_in_cart)
        imgEssentials = findViewById(R.id.img_deals)
        lytMinCart = findViewById(R.id.lyt_min_amount)
        txtMinimumCartValue = findViewById(R.id.txt_minimum_cart_value)
        txtPayUpFront = findViewById(R.id.txt_pay_up_front)
        txtAbout = findViewById(R.id.txt_about)
        lstProductList = findViewById(R.id.lstProductList)
        lstProductList!!.setNestedScrollingEnabled(false)
        linearLayoutManager = LinearLayoutManager(this)
        lstProductList!!.setLayoutManager(linearLayoutManager)
        threeDotLoaderWhite = findViewById(R.id.spin_kit_loader_product_list)
        edtSearch = findViewById(R.id.edit_search)
        edtSearchClear = findViewById(R.id.edit_search_clear)
        edtSearch!!.setHint(resources.getString(R.string.content_product_list_new_order_edt_search_hint))
        edtSearch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchText = edtSearch!!.getText().toString().trim { it <= ' ' }
                if (s.length != 0 && !StringHelper.isStringNullOrEmpty(searchText)) {
                    edtSearchClear!!.setVisibility(View.VISIBLE)
                } else {
                    edtSearchClear!!.setVisibility(View.GONE)
                }
                updateProductsAdapter()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        edtSearch!!.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                CommonMethods.hideKeyboard(this@EssentialsProductListActivity)
            }
            false
        })
        edtSearchClear!!.setOnClickListener(View.OnClickListener { edtSearch!!.setText("") })
        lytOrderSummary!!.setOnClickListener(View.OnClickListener { moveToReviewOrderScreen() })
        setFont()
        callProductAPi()
        setLayoutReviewButton(cartItemCount)
        viewOrderScrollHelper = DealsAndEssentialsProductsListScrollHelper(
            this@EssentialsProductListActivity,
            lstProductList!!,
            linearLayoutManager!!,
            object : DealsAndEssentialsProductsScrollCallback {
                override fun isHideImage(isHide: Boolean) {
                    val animate =
                        TranslateAnimation(0f, 0f, -lytHiddenContent!!.getHeight().toFloat(), 0f)
                    animate.duration = 500
                    animate.fillAfter = true
                    if (isHide) {
                        lytHiddenContent!!.setAnimation(null)
                        lytHiddenContent!!.setVisibility(View.GONE)
                    } else {
                        lytHiddenContent!!.startAnimation(animate)
                        lytHiddenContent!!.setVisibility(View.VISIBLE)
                        setCompanyLayoutView()
                    }
                }

                override fun loadMore() {}
            })
        viewOrderScrollHelper!!.setOnScrollListener()
        filterSelectedOutletCategories = ArrayList()
        filterSelectedOutletCertificates = ArrayList()
        btnFilter!!.setOnClickListener(View.OnClickListener {
            selectedFilterCatageryCounter = getSelectedCount(outletCategoryList)
            selectedFilterCertificationCounter = getSelectedCount(outletCertificationList)
            filterOutletCategoryList = ArrayList()
            filterOutletCertificationList = ArrayList()
            if (outletCategoryList != null && outletCategoryList!!.size > 0) {
                for (i in outletCategoryList!!.indices) {
                    val outletTags = OutletTags(outletCategoryList!![i]!!)
                    filterOutletCategoryList.add(outletTags)
                }
            }
            if (outletCertificationList != null && outletCertificationList!!.size > 0) {
                for (i in outletCertificationList!!.indices) {
                    val outletTags = OutletTags(outletCertificationList!![i]!!)
                    filterOutletCertificationList.add(outletTags)
                }
            }
            ShowFilterTagsDialog(
                this@EssentialsProductListActivity,
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

                        //updateFilterIcon();
                        val categoryName = outletTags?.categoryName
                        filterSelectedOutletCategories!!.add(categoryName!!)
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
                        for (i in filterSelectedOutletCategories!!.indices) {
                            if (categoryName == filterSelectedOutletCategories!!.get(i)) {
                                filterSelectedOutletCategories!!.removeAt(i)
                            }
                        }
                        //setApiParamsAfterFilter();
                    }

                    override fun onSavePressed() {
                        outletCategoryList = filterOutletCategoryList
                        outletCertificationList = filterOutletCertificationList
                        updateFilterIcon()
                        setApiParamsAfterFilter()
                    }

                    override fun onResetPressed() {
                        //  filterOutletCategories = new ArrayList<>();
                        setApiParamsAfterFilterForReset()
                    }

                    override fun onClear() {
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
                    }
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
                        updateFilterIcon()
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id)) filterSelectedOutletCertificates!!.add(
                            outletTags?.id!!
                        )
                        //setApiParamsAfterFilter();
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
                        updateFilterIcon()
                        if (!StringHelper.isStringNullOrEmpty(outletTags?.id))
                            for (i in filterSelectedOutletCertificates!!.indices) {
                            if (outletTags?.id == filterSelectedOutletCertificates!!.get(i)) {
                                filterSelectedOutletCertificates!!.removeAt(i)
                            }
                        }
                        //setApiParamsAfterFilter();
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

    private fun setCompanyLayoutView() {
        if (lstEssentials != null && lstEssentials!!.rebatePercentage != null && lstEssentials!!.rebatePercentage != 0.0) {
            if (lstCompany != null && !StringHelper.isStringNullOrEmpty(lstCompany!!.verificationStatus)) {
                if (lstCompany!!.verificationStatus == "unVerified") {
                    lytBusinessRequest!!.visibility = View.VISIBLE
                    lytBusinessVerifying!!.visibility = View.GONE
                } else if (lstCompany!!.verificationStatus == "inProgress") {
                    lytBusinessVerifying!!.visibility = View.VISIBLE
                    lytBusinessRequest!!.visibility = View.GONE
                } else {
                    lytBusinessVerifying!!.visibility = View.GONE
                    lytBusinessRequest!!.visibility = View.GONE
                }
            } else {
                lytBusinessVerifying!!.visibility = View.GONE
                lytBusinessRequest!!.visibility = View.GONE
            }
        } else {
            lytBusinessRequest!!.visibility = View.GONE
            lytBusinessVerifying!!.visibility = View.GONE
        }
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

        /* int totalSelectedFilters = selectedFilterCatageryCounter + selectedFilterCertificationCounter;
        if (totalSelectedFilters != 0) {
            txtNumberOfSelectedFilters.setVisibility(View.VISIBLE);
            txtNumberOfSelectedFilters.setText(totalSelectedFilters + "");
        } else {
            txtNumberOfSelectedFilters.setVisibility(View.GONE);
        }*/
    }

    private fun setApiParamsAfterFilter() {
        val apiParamsHelper = ApiParamsHelper()
        if (filterSelectedOutletCategories != null && filterSelectedOutletCategories!!.size > 0) apiParamsHelper.setCategory(
            filterSelectedOutletCategories!!
        )
        if (filterSelectedOutletCertificates != null && filterSelectedOutletCertificates!!.size > 0) apiParamsHelper.setCertifications(
            filterSelectedOutletCertificates!!
        )
        callEssentialsProductsListAPI(apiParamsHelper, true)
    }

    private fun setApiParamsAfterFilterForReset() {
        selectedFilterCertificationCounter = 0
        selectedFilterCatageryCounter = 0
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

    private fun callProductAPi() {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setEssentialsId(essentialsId!!)
        EssentialsApi.getPaginatedEssentials(
            this,
            apiParamsHelper,
            object : EssentialsResponseListener {
                override fun onSuccessResponse(essentialsList: EssentialsBaseResponse?) {
                    if (essentialsList != null && essentialsList.essentials != null && essentialsList.essentials!!.size > 0) {
                        lstEssentials = essentialsList.essentials!![0]
                        callEssentialsProductsListAPI(apiParamsHelper, false)
                        loadInfo()
                    } else {
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {}
            })
    }

    private fun loadInfo() {
        if (lstEssentials != null) {
            lytMinCart!!.setOnClickListener {
                val intent = Intent(
                    this@EssentialsProductListActivity,
                    EssentialSupplierDetailActivity::class.java
                )
                val essentialsJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(lstEssentials)
                intent.putExtra(ZeemartAppConstants.ESSENTIALS_LIST, essentialsJson)
                startActivity(intent)
            }
            txtEssentialNameHeader!!.text = lstEssentials!!.supplier?.supplierName
            txtOutLetNameHeader!!.text = SharedPref.defaultOutlet?.outletName
            if (lstEssentials!!.deliveryFeePolicy != null) {
                val cartValue = String.format(
                    resources.getString(R.string.txt_min_cart_amount),
                    lstEssentials!!.deliveryFeePolicy?.minOrder?.displayValue
                )
                txtMinimumCartValue!!.text = cartValue
            }
            if (lstEssentials!!.paymentTermDescription == ZeemartAppConstants.PAY_UP_FRONT) {
                txtPayUpFront!!.text = resources.getString(R.string.txt_pay_upfront)
            } else if (lstEssentials!!.paymentTermDescription == ZeemartAppConstants.PAY_AFTER_DELIVERY) {
                txtPayUpFront!!.text = resources.getString(R.string.txt_pay_after_delivery)
            }
            val color = resources.getColor(R.color.color_orange)
            val string = String.format(
                resources.getString(R.string.txt_business_verifying_content),
                color,
                lstEssentials!!.rebatePercentage.toString() + "%"
            )
            txtVerifyingContent!!.text = Html.fromHtml(string)
            //
            val requestString = String.format(
                resources.getString(R.string.txt_business_request_content),
                color,
                lstEssentials!!.rebatePercentage.toString() + "%"
            )
            txtRequestContent!!.text = Html.fromHtml(requestString)
            if (!StringHelper.isStringNullOrEmpty(lstEssentials!!.landingBanners!![0])) Picasso.get()
                .load(
                    lstEssentials!!.landingBanners!![0]
                ).fit().into(imgEssentials)
        }
    }

    private fun callEssentialsProductsListAPI(
        apiParamsHelper: ApiParamsHelper,
        isCalledFromFilters: Boolean
    ) {
        threeDotLoaderWhite!!.visibility = View.VISIBLE
        if (lstEssentials != null) apiParamsHelper.setEssentialsId(lstEssentials!!.essentialsId!!)
        EssentialsApi.getPaginatedEssentialsProducts(
            this,
            apiParamsHelper,
            object : EssentialsProductsResponseListener {
                override fun onSuccessResponse(essentialsList: EssentialProductsPaginated?) {
                    if (essentialsList != null && essentialsList.data != null && essentialsList.data!!.essentialProducts != null
                        && essentialsList.data!!.essentialProducts?.size!! > 0) {
                        productList = ArrayList(essentialsList.data!!.essentialProducts!!)
                        addProductsList(isCalledFromFilters)
                        callCompanyVerification()
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    threeDotLoaderWhite!!.visibility = View.GONE
                }
            })
    }

    private fun callCompanyVerification() {
        OutletsApi.getSpecificOutlet(this, object : GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                if (specificOutlet != null) {
                    val apiParamsHelper = ApiParamsHelper()
                    apiParamsHelper.setCompanyId(specificOutlet.data?.company?.companyId!!)
                    CompaniesApi.retrieveSpecificCompany(
                        this@EssentialsProductListActivity,
                        apiParamsHelper,
                        SharedPref.allOutlets,
                        object : SpecificCompanyResponseListener {
                            override fun onSuccessResponse(response: SpecificCompany?) {
                                if (response != null && response.data != null) {
                                    lstCompany = response.data!!
                                    setCompanyLayoutView()
                                }
                                threeDotLoaderWhite!!.visibility = View.GONE
                            }

                            override fun onErrorResponse(error: VolleyError?) {
                                threeDotLoaderWhite!!.visibility = View.GONE
                            }
                        })
                }
                threeDotLoaderWhite!!.visibility = View.GONE
            }

            override fun onError(error: VolleyErrorHelper?) {
                threeDotLoaderWhite!!.visibility = View.GONE
            }
        })
    }

    private fun updateData() {
        if (products != null && products!!.size != 0) {
            val selectedProducts = ArrayList<Product>()
            for (i in products!!.indices) {
                if (products!![i].isSelected) {
                    var item: Product?
                    val key = ProductDataHelper.getKeyProductMap(
                        products!![i].sku,
                        products!![i].priceList!![0].unitSize!!
                    )
                    if (selectedProductMap.containsKey(key)) {
                        item =
                            selectedProductMap[products!![i].sku + products!![i].priceList!![0].unitSize]
                    } else {
                        item = ProductDataHelper.createSelectedProductObject(products!![i])
                        val key1 = ProductDataHelper.getKeyProductMap(item?.sku, item?.unitSize!!)
                        selectedProductMap[key1] = item
                    }
                    if (item != null) {
                        selectedProducts.add(item)
                    }
                }
            }
            setLayoutReviewButton(selectedProducts.size)
            productListAdapter!!.onActivityResult(selectedProductMap as MutableMap<String, Product>)
            productListAdapter!!.notifyDataSetChanged()
        }
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
                        this@EssentialsProductListActivity,
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
                    startActivityForResult(
                        newIntent,
                        ZeemartAppConstants.RequestCode.EssentialsProductListActivity
                    )
                }
            } else {
                ZeemartBuyerApp.getToastRed(getString(R.string.txt_no_products_selected))
            }
        } else {
            ZeemartBuyerApp.getToastRed(getString(R.string.txt_no_products_selected))
        }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            edtSearch,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEssentialNameHeader,
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
        ZeemartBuyerApp.setTypefaceView(
            txtVerifyingContent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtVerifyingHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRequestContent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRequestHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnRequest,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
    }

    private fun addProductsList(isCalledFromFilters: Boolean) {
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
                            productUom.priceList = (productPriceLists)
                            productUom.isHasMultipleUom = (true)
                            products!!.add(productUom)
                        }
                    }
                } else {
                    val productUom = ProductDataHelper.getEssentialProducts(
                        productList!![i]
                    )
                    val productPriceLists: MutableList<ProductPriceList> = ArrayList()
                    productPriceLists.add(productList!![i].unitPrices!![0])
                    productUom.priceList = (productPriceLists)
                    productUom.isHasMultipleUom = (true)
                    products!!.add(productUom)
                }
            }
            if (selectedProductList != null && selectedProductList!!.size > 0) {
                for (i in selectedProductList!!.indices) {
                    for (j in products!!.indices) {
                        if (products!!.get(j).sku == selectedProductList!![i].sku && products!!.get(j).priceList!![0].unitSize == selectedProductList!![i].unitSize) {
                            products!!.get(j).isSelected = true
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
                if (products != null && products!!.size > 0) {
                    for (i in products!!.indices) {
                        if (!StringHelper.isStringNullOrEmpty(products!!.get(i).categoryPath)) {
                            val twoStringArray =
                                products!!.get(i).categoryPath?.split(":".toRegex(), limit = 2)
                                    ?.toTypedArray() //the main line
                            val categoryPath = twoStringArray!![0]
                            uniqueFilterCategoriesSet.add(categoryPath)
                        }
                        if (products!!.get(i).certifications != null) {
                            for (j in products!!.get(i).certifications!!.indices) {
                                if (!StringHelper.isStringNullOrEmpty(products!!.get(i).certifications!![j].id)
                                    && !StringHelper.isStringNullOrEmpty(
                                        products!!.get(i).certifications!![j].name
                                    )
                                ) {
                                    val outletTags = OutletTags()
                                    outletTags.id = products!!.get(i).certifications!![j].id
                                    outletTags.categoryName = products!!.get(i).certifications!![j].name
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        productListAdapter!!.notifyDataSetChanged()
        if (requestCode == ZeemartAppConstants.RequestCode.EssentialsProductListActivity) {
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
                                ProductDataHelper.createSelectedProductMap(selectedProductList) as MutableMap<String, Product?>
                        }
                        cartItemCount = 0
                        setLayoutReviewButton(cartItemCount)
                        addProductsList(true)
                    }
                }
            }
        }
        if (requestCode == ZeemartAppConstants.RequestCode.ProductDetailsActivity) {
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
        if (resultCode == CompanyVerificationRequestActivity.IS_COMPANY_STATUS_CHANGED) {
            lytBusinessVerifying!!.visibility = View.VISIBLE
            lytBusinessRequest!!.visibility = View.GONE
        }
    }

    private fun updateProductsAdapter() {
        if (products != null && products!!.size > 0) {
            if (edtSearch!!.text.toString().length == 0) {
                productListAdapter = SupplierProductListAdapter(
                    false,
                    this,
                    products!!,
                    SharedPref.defaultOutlet?.outletId!!,
                    ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST,
                    lstEssentials!!.supplier?.supplierName!!,
                    lstEssentials!!.supplier?.logoURL!!,
                    lstEssentials!!.supplier?.supplierId!!,
                    null,
                    selectedProductMap as MutableMap<String, Product>,
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
            false,
            this,
            filteredList,
            SharedPref.defaultOutlet?.outletId!!,
            ZeemartAppConstants.CALLED_FROM_ESSENTIAL_PRODUCT_LIST,
            lstEssentials!!.supplier?.supplierName!!,
            lstEssentials!!.supplier?.logoURL!!,
            lstEssentials!!.supplier?.supplierId!!,
            null,
            selectedProductMap as MutableMap<String, Product>,
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
                val newIntent =
                    Intent(this@EssentialsProductListActivity, SearchForNewOrder::class.java)
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

    companion object {
        private const val REQUEST_CODE_BUSINESS_STATUS_CHANGED = 102
    }
}