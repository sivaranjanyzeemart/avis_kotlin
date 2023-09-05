package zeemart.asia.buyers.inventory

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.InventoryDashboardPagerAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.models.StockageType
import zeemart.asia.buyers.models.inventory.Shelve
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity

class InventoryDashboardActivity : AppCompatActivity() {
    private val oldOutletName: String? = SharedPref.defaultOutlet?.outletName
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private var btnInventoryDashboardBack: ImageView? = null
    private var txtOutletName: Button? = null
    private lateinit var btnFilter: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var txtFilterCount: TextView
    private lateinit var lytNoSubscription: LinearLayout
    private var txtNoSubscriptionHeader: TextView? = null
    private var txtNoSubscriptionContent: TextView? = null
    private var btnSubscription: Button? = null
    private var posIntegration = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_dashboard)
        viewPager = findViewById<ViewPager>(R.id.inventory_dashboard_view_pager)
        lytNoSubscription = findViewById<LinearLayout>(R.id.lyt_no_subscription)
        lytNoSubscription.setVisibility(View.GONE)
        txtNoSubscriptionHeader = findViewById<TextView>(R.id.txt_no_search_1)
        txtNoSubscriptionContent = findViewById<TextView>(R.id.txt_no_search_2)
        btnSubscription = findViewById<Button>(R.id.txt_review_total_items_in_cart)
        btnInventoryDashboardBack = findViewById<ImageView>(R.id.btn_inventory_dashboard_back)
        btnFilter = findViewById<ImageButton>(R.id.inventory_activity_btn_filter)
        btnFilter.setOnClickListener(View.OnClickListener {
            if (SharedPref.defaultOutlet != null) {
                AnalyticsHelper.logActionForInventory(
                    this@InventoryDashboardActivity,
                    AnalyticsHelper.TAP_INVENTORY_ACTIVITY_FILTER,
                    SharedPref.defaultOutlet!!
                )
            }
            onButtonFilterClicked()
        })
        btnSettings = findViewById<ImageButton>(R.id.inventory_activity_btn_settings)
        btnSettings.setOnClickListener(View.OnClickListener { onButtonSettingsClicked() })
        txtFilterCount = findViewById<TextView>(R.id.txt_number_of_selected_activity_filters)
        tabLayout = findViewById<TabLayout>(R.id.lyt_tab_inventory_dashboard)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.getPosition() == InventoryDataManager.ACTIVITY_TAB_ID) {
                    btnFilter.setVisibility(View.VISIBLE)
                    btnSettings.setVisibility(View.GONE)
                    setFilterButtonSettings()
                    if (SharedPref.defaultOutlet != null) {
                        AnalyticsHelper.logActionForInventory(
                            this@InventoryDashboardActivity,
                            AnalyticsHelper.TAP_INVENTORY_ACTIVITY,
                            SharedPref.defaultOutlet!!
                        )
                    }
                } else {
                    btnFilter.setVisibility(View.GONE)
                    btnSettings.setVisibility(View.VISIBLE)
                    txtFilterCount.setVisibility(View.GONE)
                    if (SharedPref.defaultOutlet != null) {
                        AnalyticsHelper.logActionForInventory(
                            this@InventoryDashboardActivity,
                            AnalyticsHelper.TAP_INVENTORY_LISTS,
                            SharedPref.defaultOutlet!!
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        txtOutletName = findViewById<Button>(R.id.txt_inventory_outlet_name)
        //set the adapter
        val tabsPagerAdapter = InventoryDashboardPagerAdapter(this, getSupportFragmentManager())
        tabsPagerAdapter.addFragments(InventoryDashboardListsFragment.newInstance())
        tabsPagerAdapter.addFragments(InventoryDashboardActivitiesFragment.newInstance())
        viewPager.setAdapter(tabsPagerAdapter)
        btnInventoryDashboardBack!!.setOnClickListener { finish() }
        txtOutletName!!.setOnClickListener {
            val intentChangeOutlet =
                Intent(this@InventoryDashboardActivity, SelectOutletActivity::class.java)
            startActivity(intentChangeOutlet)
        }
        setFont()
    }

    private fun onButtonFilterClicked() {
        val newIntent = Intent(this, FilterInventoryActivity::class.java)
        startActivityForResult(newIntent, ZeemartAppConstants.RequestCode.FilterInventoryActivity)
    }

    private fun onButtonSettingsClicked() {
        val newIntent = Intent(this, InventorySettingsActivity::class.java)
        startActivity(newIntent)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZeemartAppConstants.RequestCode.FilterInventoryActivity && resultCode == ZeemartAppConstants.ResultCode.RESULT_OK) {
            val isApplyFilter: Boolean =
                data?.getBooleanExtra(ZeemartAppConstants.APPLY_FILTER_SELECTED, false) == true
            if (isApplyFilter) {
                if (viewPager.getAdapter() is InventoryDashboardPagerAdapter) {
                    val fragmentObj: Any =
                        (viewPager.getAdapter() as InventoryDashboardPagerAdapter).getItem(
                            InventoryDataManager.ACTIVITY_TAB_ID
                        )
                    if (fragmentObj is InventoryDashboardActivitiesFragment) {
                        fragmentObj.loadActivities(true)
                    }
                }
            }
        }
    }

    fun setFilterButtonSettings() {
        if (tabLayout.getSelectedTabPosition() == InventoryDataManager.ACTIVITY_TAB_ID) {
            val count = InventoryDataManager.selectedFiltersForActivityCount
            if (count == 0) {
                txtFilterCount.setVisibility(View.GONE)
                btnFilter.setImageResource(R.drawable.filtericon)
            } else {
                txtFilterCount.setVisibility(View.VISIBLE)
                txtFilterCount.setText(count.toString() + "")
                btnFilter.setImageResource(R.drawable.filteryellow)
            }
        } else {
            txtFilterCount.setVisibility(View.GONE)
            btnFilter.setVisibility(View.GONE)
        }
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtOutletName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoSubscriptionHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoSubscriptionContent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSubscription,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        CommonMethods.setFontForTabs(tabLayout)
    }

    protected override fun onResume() {
        super.onResume()
        setFilterButtonSettings()
        if (SharedPref.defaultOutlet != null) txtOutletName?.setText(
            SharedPref.defaultOutlet!!.outletName
        ) else txtOutletName!!.text = ""
        OutletsApi.getSpecificOutlet(this, object : OutletsApi.GetSpecificOutletResponseListener {
            override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                if ((specificOutlet != null && specificOutlet.data != null) and (specificOutlet?.data?.subscriptionSettings != null)
                    && !StringHelper.isStringNullOrEmpty(
                        specificOutlet?.data?.subscriptionSettings?.status
                    )
                ) {
                    if (specificOutlet?.data?.subscriptionSettings?.status == SpecificOutlet.SubscriptionStatus.ACTIVE.statusName
                        && specificOutlet.data!!.subscriptionSettings!!.plan?.subscriptionType == SpecificOutlet.SubscriptionType.STARTER.typeName || specificOutlet?.data?.subscriptionSettings?.status == SpecificOutlet.SubscriptionStatus.SUSPENDED.statusName) {
                        lytNoSubscription.setVisibility(View.VISIBLE)
                        viewPager.setVisibility(View.GONE)
                        btnSubscription!!.setOnClickListener {
                            val uri = Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_SUBSCRIPTION_PLANS)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
                    } else {
                        lytNoSubscription.setVisibility(View.GONE)
                        viewPager.setVisibility(View.VISIBLE)
                        if (viewPager.getAdapter() != null) {
                            viewPager.getAdapter()!!.notifyDataSetChanged()
                            CommonMethods.setFontForTabs(tabLayout)
                        } else {
                            //set the adapter
                            val tabsPagerAdapter = InventoryDashboardPagerAdapter(
                                this@InventoryDashboardActivity,
                                getSupportFragmentManager()
                            )
                            tabsPagerAdapter.addFragments(InventoryDashboardListsFragment.newInstance())
                            tabsPagerAdapter.addFragments(InventoryDashboardActivitiesFragment.newInstance())
                            viewPager.setAdapter(tabsPagerAdapter)
                            CommonMethods.setFontForTabs(tabLayout)
                        }
                    }
                } else {
                    lytNoSubscription.setVisibility(View.GONE)
                    viewPager.setVisibility(View.VISIBLE)
                    if (viewPager.getAdapter() != null) {
                        viewPager.getAdapter()!!.notifyDataSetChanged()
                        CommonMethods.setFontForTabs(tabLayout)
                    } else {
                        //set the adapter
                        val tabsPagerAdapter = InventoryDashboardPagerAdapter(
                            this@InventoryDashboardActivity,
                            getSupportFragmentManager()
                        )
                        tabsPagerAdapter.addFragments(InventoryDashboardListsFragment.newInstance())
                        tabsPagerAdapter.addFragments(InventoryDashboardActivitiesFragment.newInstance())
                        viewPager.setAdapter(tabsPagerAdapter)
                        CommonMethods.setFontForTabs(tabLayout)
                    }
                }
                if (specificOutlet?.data?.posIntegration != null && specificOutlet.data!!.posIntegration!!.posIntegration == true) {
                    posIntegration = specificOutlet.data!!.posIntegration!!.posIntegration!!
                }
            }

            override fun onError(error: VolleyErrorHelper?) {}
        })
    }

    protected override fun onDestroy() {
        super.onDestroy()
        StockageType.initializeStockTypeListToSharedPrefs()
        Shelve.clearSharedPrefShelveList()
    }
}