package zeemart.asia.buyers.selfOnBoarding

import android.content.Intent
import android.os.Bundle
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
import androidx.viewpager.widget.ViewPager
import com.android.volley.VolleyError
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.SpclDealsImageSliderAdapter
import zeemart.asia.buyers.adapter.SupplierListEssentialsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.deals.AboutDealsActivity
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.AnalyticsHelper.logGuestAction
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.LoaderInteface
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.orderimportimportimport.SpclDealsBaseResponse
import zeemart.asia.buyers.modelsimport.EssentialsResponseForOnBoarding
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseOnBoardingListener
import zeemart.asia.buyers.network.EssentialsApi.getPaginatedEssentialsForOnBoarding
import zeemart.asia.buyers.network.OrderHelper.DealsEssentialsOnBoardingResponseListener
import zeemart.asia.buyers.network.OrderHelper.getActiveEssentialsDealsForOnBoarding
import java.util.*

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class BrowseDealAndEssentialsSuppliers : AppCompatActivity(), LoaderInteface {
    private lateinit var edtSearch: EditText
    private var txtOrder: TextView? = null
    private lateinit var lytOrderSummary: RelativeLayout
    private var txtTotalItems: TextView? = null
    private lateinit var customLoadingViewWhite: CustomLoadingViewWhite
    private var dealsViewPager: ViewPager? = null
    private var timer: Timer? = null
    private lateinit var lytDeals: RelativeLayout
    private var txtDealsForYou: TextView? = null
    private lateinit var txtDealsDetails: TextView
    private var tabLayout: TabLayout? = null
    private lateinit var lstEssentials: RecyclerView
    private lateinit var lstEssentialsSuppliers: MutableList<Essential>
    private var lytEssentialBanner: RelativeLayout? = null
    private var txtItemsForSupplier: TextView? = null
    private var txtRebateWithEssentialOrder: TextView? = null
    private var txtSuppliers: TextView? = null
    private var isDealsLoaded = false
    private var isEssentialsLoaded = false
    private lateinit var imgBack: ImageView
    private var lytTabBanner: RelativeLayout? = null
    override fun onResume() {
        super.onResume()
        isEssentialsLoaded = false
        isDealsLoaded = false
        displayLoader(customLoadingViewWhite!!)
        lstEssentials!!.adapter = null
        lytEssentialBanner!!.visibility = View.GONE
        retrieveActiveDeals()
        retrieveActiveEssentials()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_deal_and_essentials_suppliers)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        customLoadingViewWhite = findViewById(R.id.spin_kit_loader_browse_suppliers_white)
        lytOrderSummary = findViewById(R.id.lyt_review_order_browse)
        imgBack = findViewById(R.id.products_back_btn)
        lytTabBanner = findViewById(R.id.lyt_tab_banner)
        imgBack.setOnClickListener(View.OnClickListener { finish() })
        lytOrderSummary.setVisibility(View.GONE)
        lytEssentialBanner = findViewById(R.id.lyt_essential_banner)
        txtItemsForSupplier = findViewById(R.id.txt_items_from_trusted_supplier)
        txtRebateWithEssentialOrder = findViewById(R.id.txt_get_up_to_5_percent)
        txtOrder = findViewById(R.id.txt_outlet_name_browse_new_order)
        txtTotalItems = lytOrderSummary.findViewById(R.id.txt_review_total_items_in_cart)
        edtSearch = findViewById(R.id.edit_search)
        txtSuppliers = findViewById(R.id.txt_suppliers)
        edtSearch.setHint(getString(R.string.txt_search_products_or_suppliers))
        edtSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                AnalyticsHelper.logGuestAction(
                    this@BrowseDealAndEssentialsSuppliers,
                    AnalyticsHelper.TAP_GUEST_CREATE_ORDER_SEARCH
                )
                val searchIntent = Intent(
                    this@BrowseDealAndEssentialsSuppliers,
                    SearchOnBoardingEssentialsDealsActivity::class.java
                )
                startActivity(searchIntent)
                return@OnEditorActionListener true
            }
            false
        })
        edtSearch.setTextIsSelectable(true)
        edtSearch.setFocusable(false)
        edtSearch.setClickable(false)
        edtSearch.setOnClickListener(View.OnClickListener {
            val newSearchIntent = Intent(
                this@BrowseDealAndEssentialsSuppliers,
                SearchOnBoardingEssentialsDealsActivity::class.java
            )
            startActivity(newSearchIntent)
        })
        lstEssentials = findViewById(R.id.lst_supplier_list_browse_essential_order)
        lstEssentials.setLayoutManager(LinearLayoutManager(this))
        dealsViewPager = findViewById(R.id.deals_view_pager)
        lytDeals = findViewById(R.id.lyt_deals)
        lytDeals.setVisibility(View.GONE)
        txtDealsForYou = findViewById(R.id.txt_deals_for_you)
        txtDealsDetails = findViewById(R.id.txt_about_this_deals)
        txtDealsDetails.setOnClickListener(View.OnClickListener {
            val testAboutDeals =
                Intent(this@BrowseDealAndEssentialsSuppliers, AboutDealsActivity::class.java)
            startActivity(testAboutDeals)
        })
        tabLayout = findViewById(R.id.tab_layout_carousal)
        displayLoader(customLoadingViewWhite)
        setFont()
    }

    private fun retrieveActiveEssentials() {
        getPaginatedEssentialsForOnBoarding(this, object : EssentialsResponseOnBoardingListener {
            override fun onSuccessResponse(essentialsList: EssentialsResponseForOnBoarding?) {
                if (essentialsList != null && essentialsList.data != null && essentialsList.data!!
                        .essentials != null && essentialsList.data!!.essentials!!
                        .size > 0
                ) {
                    lstEssentialsSuppliers = ArrayList()
                    lstEssentialsSuppliers.addAll(essentialsList.data!!.essentials!!)
                    var suppliersCount = ""
                    suppliersCount = if (essentialsList.data!!.essentials?.size === 1) {
                        "${essentialsList.data!!.essentials?.size} ${resources.getString(R.string.txt_supplier_small)}"

                    } else {
                        "${essentialsList.data!!.essentials?.size} ${resources.getString(
                            R.string.txt_suppliers
                        )}"
                    }
                    txtSuppliers!!.text = suppliersCount
                    Collections.sort(lstEssentialsSuppliers, object : Comparator<Essential?> {
                        override fun compare(o1: Essential?, o2: Essential?): Int {
                            return o1?.supplier!!.supplierName!!.compareTo(o2?.supplier!!.supplierName!!)
                        }
                    })
                    lstEssentials.adapter = SupplierListEssentialsAdapter(
                        lstEssentialsSuppliers,
                        this@BrowseDealAndEssentialsSuppliers,
                        object : SupplierListEssentialsAdapter.SupplierClickListener {
                            override fun onItemSelected(essential: Essential?) {
                                logGuestAction(
                                    this@BrowseDealAndEssentialsSuppliers,
                                    AnalyticsHelper.TAP_GUEST_CREATE_ORDER_ESSENTIALS,
                                    essential!!
                                )
                                val intent = Intent(
                                    this@BrowseDealAndEssentialsSuppliers,
                                    EssentialsOnBoardingProductListActivity::class.java
                                )
                                intent.putExtra(
                                    ZeemartAppConstants.ESSENTIALS_LIST,
                                    ZeemartBuyerApp.gsonExposeExclusive.toJson(essential)
                                )
                                startActivity(intent)
                            }
                        })
                    isEssentialsLoaded = true
                    hideLoader(customLoadingViewWhite)
                    setEssentialsTabActive()
                } else {
                    isEssentialsLoaded = true
                    hideLoader(customLoadingViewWhite)
                    lytEssentialBanner!!.visibility = View.GONE
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                isEssentialsLoaded = true
                hideLoader(customLoadingViewWhite)
                lytEssentialBanner!!.visibility = View.GONE
            }
        })
    }

    private fun retrieveActiveDeals() {
        getActiveEssentialsDealsForOnBoarding(
            this,
            object : DealsEssentialsOnBoardingResponseListener {
                override fun onSuccessResponse(dealsBaseResponse: SpclDealsBaseResponse?) {
                    if (dealsBaseResponse != null && dealsBaseResponse.data != null && dealsBaseResponse.data!!
                            .suppliers?.size!! > 0
                    ) {
                        val lstSuppliers: MutableList<SpclDealsBaseResponse.Deals> = ArrayList()
                        for (i in 0 until dealsBaseResponse.data!!.suppliers!!.size) {
                            if (dealsBaseResponse.data!!.suppliers!!.get(i)
                                    .carouselBanners != null && !StringHelper.isStringNullOrEmpty(
                                    dealsBaseResponse.data!!.suppliers!!.get(i)
                                        .carouselBanners!!.get(0)
                                )
                            ) {
                                lstSuppliers.add(dealsBaseResponse.data!!.suppliers!!.get(i))
                            }
                        }
                        if (lstSuppliers.size > 0) {
                            lytDeals.visibility = View.VISIBLE
                            val dealsImageSliderAdapter = SpclDealsImageSliderAdapter(
                                lstSuppliers,
                                this@BrowseDealAndEssentialsSuppliers,
                                object : SpclDealsImageSliderAdapter.DealsImageListener {
                                    override fun onDealSelected(deal: SpclDealsBaseResponse.Deals?) {
                                        val intent: Intent
                                        if (deal?.dealNumber != null) {
                                            intent = Intent(
                                                this@BrowseDealAndEssentialsSuppliers,
                                                DealsOnBoardingProductListActivity::class.java
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.DEALS_LIST,
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(deal)
                                            )
                                        } else {
                                            intent = Intent(
                                                this@BrowseDealAndEssentialsSuppliers,
                                                EssentialsOnBoardingProductListActivity::class.java
                                            )
                                            intent.putExtra(
                                                ZeemartAppConstants.ESSENTIALS_LIST,
                                                ZeemartBuyerApp.gsonExposeExclusive.toJson(deal)
                                            )
                                        }
                                        startActivity(intent)
                                    }
                                })
                            dealsViewPager!!.offscreenPageLimit = lstSuppliers.size
                            dealsViewPager!!.adapter = dealsImageSliderAdapter
                            if (dealsBaseResponse?.data!!.suppliers?.size === 1) {
                                tabLayout!!.visibility = View.GONE
                                lytTabBanner!!.visibility = View.GONE
                            } else {
                                lytTabBanner!!.visibility = View.VISIBLE
                                tabLayout!!.visibility = View.VISIBLE
                                tabLayout!!.setupWithViewPager(dealsViewPager, true)
                            }
                            val timerTask: TimerTask = object : TimerTask() {
                                override fun run() {
                                    dealsViewPager!!.post {
                                        dealsViewPager!!.currentItem =
                                            (dealsViewPager!!.currentItem + 1) % dealsBaseResponse.data!!
                                                .suppliers!!.size
                                    }
                                }
                            }
                            isDealsLoaded = true
                            timer = Timer()
                            timer!!.schedule(timerTask, 2500, 2500)
                            isDealsLoaded = true
                            hideLoader(customLoadingViewWhite)
                        } else {
                            isDealsLoaded = true
                            hideLoader(customLoadingViewWhite)
                        }
                    } else {
                        isDealsLoaded = true
                        lytDeals.visibility = View.GONE
                        hideLoader(customLoadingViewWhite)
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {
                    isDealsLoaded = true
                    hideLoader(customLoadingViewWhite)
                    lytDeals.visibility = View.GONE
                }
            })
    }

    private fun setFont() {
        setTypefaceView(txtTotalItems, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDealsForYou, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtDealsDetails, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtItemsForSupplier, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(
            txtRebateWithEssentialOrder,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSuppliers, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun displayLoader(view: View?) {
        if (view != null) {
            view.visibility = View.VISIBLE
        }
    }

    override fun hideLoader(view: View?) {
        if (view != null && isDealsLoaded && isEssentialsLoaded) {
            view.visibility = View.GONE
        }
    }

    private fun setEssentialsTabActive() {
        hideLoader(customLoadingViewWhite!!)
        lytEssentialBanner!!.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        if (timer != null) {
            timer!!.cancel()
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (timer != null) {
            timer!!.cancel()
        }
    }
}