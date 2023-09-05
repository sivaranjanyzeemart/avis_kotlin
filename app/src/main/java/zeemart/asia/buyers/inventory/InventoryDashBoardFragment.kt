package zeemart.asia.buyers.inventory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.InventoryDashboardPagerAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.models.StockageType
import zeemart.asia.buyers.models.inventory.Shelve
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.createordersimport.SelectOutletActivity

/**
 * A simple [Fragment] subclass.
 * Use the [InventoryDashBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InventoryDashBoardFragment : Fragment() {
    private val oldOutletName: String? = SharedPref.defaultOutlet?.outletName
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var btnInventoryDashboardBack: ImageView
    private lateinit var txtOutletName: TextView
    private lateinit var btnFilter: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var txtFilterCount: TextView
    private lateinit var lytNoSubscription: LinearLayout
    private var txtNoSubscriptionHeader: TextView? = null
    private var txtNoSubscriptionContent: TextView? = null
    private var btnSubscription: Button? = null
    private var isFragmentAttached = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_inventory_dash_board, container, false)
        viewPager = view.findViewById<ViewPager>(R.id.inventory_dashboard_view_pager)
        lytNoSubscription = view.findViewById<LinearLayout>(R.id.lyt_no_subscription)
        lytNoSubscription.setVisibility(View.GONE)
        txtNoSubscriptionHeader = view.findViewById<TextView>(R.id.txt_no_search_1)
        txtNoSubscriptionContent = view.findViewById<TextView>(R.id.txt_no_search_2)
        btnSubscription = view.findViewById(R.id.txt_review_total_items_in_cart)
        btnInventoryDashboardBack = view.findViewById(R.id.btn_inventory_dashboard_back)
        btnInventoryDashboardBack.setVisibility(View.GONE)
        btnFilter = view.findViewById<ImageButton>(R.id.inventory_activity_btn_filter)
        btnFilter.setOnClickListener(View.OnClickListener { onButtonFilterClicked() })
        btnSettings = view.findViewById<ImageButton>(R.id.inventory_activity_btn_settings)
        btnSettings.setOnClickListener(View.OnClickListener { onButtonSettingsClicked() })
        btnSettings.setVisibility(View.GONE)
        txtFilterCount = view.findViewById<TextView>(R.id.txt_number_of_selected_activity_filters)
        tabLayout = view.findViewById<TabLayout>(R.id.lyt_tab_inventory_dashboard)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.getPosition() == InventoryDataManager.ACTIVITY_TAB_ID) {
                    btnFilter.setVisibility(View.VISIBLE)
                    btnSettings.setVisibility(View.GONE)
                    setFilterButtonSettings()
                } else {
                    btnFilter.setVisibility(View.GONE)
                    btnSettings.setVisibility(View.VISIBLE)
                    txtFilterCount.setVisibility(View.GONE)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        txtOutletName = view.findViewById<TextView>(R.id.txt_inventory_outlet_name)
        //set the adapter
        val tabsPagerAdapter = InventoryDashboardPagerAdapter(
            requireActivity(), childFragmentManager
        )
        tabsPagerAdapter.addFragments(InventoryDashboardListsFragment.newInstance())
        tabsPagerAdapter.addFragments(InventoryDashboardActivitiesFragment.newInstance())
        viewPager.setAdapter(tabsPagerAdapter)
        txtOutletName.setOnClickListener(View.OnClickListener {
            val intentChangeOutlet = Intent(activity, SelectOutletActivity::class.java)
            startActivity(intentChangeOutlet)
        })
        setFont()
        return view
    }

    fun setFilterButtonSettings() {
        if (tabLayout != null && tabLayout.getSelectedTabPosition() != 0 && tabLayout.getSelectedTabPosition() == InventoryDataManager.ACTIVITY_TAB_ID) {
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

    private fun onButtonFilterClicked() {
        val newIntent = Intent(activity, FilterInventoryActivity::class.java)
        startActivityForResult(newIntent, ZeemartAppConstants.RequestCode.FilterInventoryActivity)
    }

    private fun onButtonSettingsClicked() {
        val newIntent = Intent(activity, InventorySettingsActivity::class.java)
        startActivity(newIntent)
    }

    fun loadInventory() {
        if (isFragmentAttached) if (viewPager != null && tabLayout != null) {
            val tabsPagerAdapter = InventoryDashboardPagerAdapter(
                requireActivity(), childFragmentManager
            )
            tabsPagerAdapter.addFragments(InventoryDashboardListsFragment.newInstance())
            tabsPagerAdapter.addFragments(InventoryDashboardActivitiesFragment.newInstance())
            viewPager.setAdapter(tabsPagerAdapter)
            CommonMethods.setFontForTabs(tabLayout)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFragmentAttached) {
            if (SharedPref.defaultOutlet!= null) txtOutletName.setText(
                SharedPref.defaultOutlet!!.outletName
            ) else txtOutletName.setText("")
            OutletsApi.getSpecificOutlet(activity, object :
                OutletsApi.GetSpecificOutletResponseListener {
                override fun onSuccessResponse(specificOutlet: SpecificOutlet?) {
                    if ((specificOutlet != null && specificOutlet.data != null) and (specificOutlet?.data?.subscriptionSettings != null) && !StringHelper.isStringNullOrEmpty(
                            specificOutlet?.data?.subscriptionSettings?.status
                        )
                    ) {
                        if (specificOutlet?.data?.subscriptionSettings?.status == SpecificOutlet.SubscriptionStatus.ACTIVE.statusName && specificOutlet.data!!.subscriptionSettings!!.plan?.subscriptionType == SpecificOutlet.SubscriptionType.STARTER.typeName || specificOutlet?.data?.subscriptionSettings?.status == SpecificOutlet.SubscriptionStatus.ACTIVE.statusName && specificOutlet?.data?.subscriptionSettings?.plan?.subscriptionType == SpecificOutlet.SubscriptionType.GROWTH.typeName || specificOutlet?.data?.subscriptionSettings?.status == SpecificOutlet.SubscriptionStatus.SUSPENDED.statusName) {
                            lytNoSubscription.setVisibility(View.VISIBLE)
                            viewPager.setVisibility(View.GONE)
                            btnSubscription!!.setOnClickListener {
                                var uri =
                                    Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_SUBSCRIPTION_PLANS)
                                if (ZeemartAppConstants.Market.`this`
                                        .isMarket(ZeemartAppConstants.Market.AUSTRALIA)
                                ) {
                                    uri =
                                        Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_SUBSCRIPTION_PLANS_ASUTRALIA)
                                }
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            }
                        } else {
                            lytNoSubscription.setVisibility(View.GONE)
                            viewPager.setVisibility(View.VISIBLE)
                            if (viewPager.getAdapter() != null) {
                                viewPager.getAdapter()!!.notifyDataSetChanged()
                            } else {
                                //set the adapter
                                val tabsPagerAdapter = InventoryDashboardPagerAdapter(
                                    activity!!, childFragmentManager
                                )
                                tabsPagerAdapter.addFragments(InventoryDashboardListsFragment.newInstance())
                                tabsPagerAdapter.addFragments(InventoryDashboardActivitiesFragment.newInstance())
                                viewPager.setAdapter(tabsPagerAdapter)
                            }
                            CommonMethods.setFontForTabs(tabLayout)
                        }
                    } else {
                        lytNoSubscription.setVisibility(View.GONE)
                        viewPager.setVisibility(View.VISIBLE)
                        if (viewPager.getAdapter() != null) {
                            viewPager.getAdapter()!!.notifyDataSetChanged()
                        } else {
                            //set the adapter
                            val tabsPagerAdapter = InventoryDashboardPagerAdapter(
                                activity!!, childFragmentManager
                            )
                            tabsPagerAdapter.addFragments(InventoryDashboardListsFragment.newInstance())
                            tabsPagerAdapter.addFragments(InventoryDashboardActivitiesFragment.newInstance())
                            viewPager.setAdapter(tabsPagerAdapter)
                        }
                        CommonMethods.setFontForTabs(tabLayout)
                    }
                }

                override fun onError(error: VolleyErrorHelper?) {}
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
            setFilterButtonSettings()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        StockageType.initializeStockTypeListToSharedPrefs()
        Shelve.clearSharedPrefShelveList()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment InventoryDashBoardFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): InventoryDashBoardFragment {
            return InventoryDashBoardFragment()
        }
    }
}