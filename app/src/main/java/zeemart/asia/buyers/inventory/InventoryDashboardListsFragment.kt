package zeemart.asia.buyers.inventory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.InventoryDashboardListsAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.models.inventory.InventoryProduct
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.Shelve
import zeemart.asia.buyers.models.inventoryimportimport.InventoryShelves
import zeemart.asia.buyers.network.OutletInventoryApi

/**
 * A simple [Fragment] subclass.
 */
class InventoryDashboardListsFragment : Fragment() {
    private lateinit var lstInventoryLists: RecyclerView
    private lateinit var lytNoInventory: LinearLayout
    private var isFragmentAttached = false
    private lateinit var loader: CustomLoadingViewBlue
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var lytInventoryUoms: RelativeLayout
    private var txtInventoryUoms: TextView? = null
    private var lstProducts: List<InventoryProduct>? = ArrayList<InventoryProduct>()
    private var txtNoInventorySetUp: TextView? = null
    private var txtNoInventorySetUpDetails: TextView? = null
    private val posIntegration = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View =
            inflater.inflate(R.layout.fragment_inventory_dashboard_lists, container, false)
        lytNoInventory = v.findViewById<LinearLayout>(R.id.lyt_no_inventory)
        lytNoInventory.setVisibility(View.GONE)
        lstInventoryLists = v.findViewById<RecyclerView>(R.id.lst_inventory_dashboard_list)
        lstInventoryLists.setLayoutManager(LinearLayoutManager(activity))
        loader = v.findViewById<CustomLoadingViewBlue>(R.id.loader_inventory_list)
        loader.setVisibility(View.VISIBLE)
        txtInventoryUoms = v.findViewById<TextView>(R.id.txt_inventory_uom_changed)
        ZeemartBuyerApp.setTypefaceView(
            txtInventoryUoms,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        lytInventoryUoms = v.findViewById<RelativeLayout>(R.id.lyt_inventory_uom_changed)
        txtNoInventorySetUp = v.findViewById<TextView>(R.id.txt_no_inventory_set_up)
        txtNoInventorySetUpDetails = v.findViewById<TextView>(R.id.txt_no_inventory_set_up_detail)
        ZeemartBuyerApp.setTypefaceView(
            txtNoInventorySetUp,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoInventorySetUpDetails,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        lytInventoryUoms.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, ReviewInventoryUOMSettings::class.java)
            intent.putExtra(
                ZeemartAppConstants.PRODUCT_DETAILS_JSON,
                ZeemartBuyerApp.gsonExposeExclusive.toJson(lstProducts)
            )
            startActivity(intent)
        })
        swipeRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_inventory_lists)
        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (isFragmentAttached) refreshInventoryList()
            }
        })
        //        if (isFragmentAttached)
//            refreshInventoryList();
        return v
    }

    private fun refreshInventoryList() {
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayout)
        swipeRefreshLayout.setRefreshing(false)
        loader.setVisibility(View.VISIBLE)
        callInventoryShelvesData()
        callInventoryProducts()
    }

    private fun callInventoryProducts() {
        val apiParamsHelperProducts = ApiParamsHelper()
        apiParamsHelperProducts.setSortOrder(ApiParamsHelper.SortOrder.ASC)
        apiParamsHelperProducts.setSortBy(ApiParamsHelper.SortField.PRODUCT_NAME)
        apiParamsHelperProducts.setRemoveSquareBrackets(true)
        apiParamsHelperProducts.setIsConversionUpdated(true)
        OutletInventoryApi.getProductForShelve(
            activity,
            apiParamsHelperProducts,
            object : OutletInventoryApi.ProductsByShelveDataListener {
                override fun onStockShelveListSuccess(productsByShelveId: InventoryResponse.ProductsByShelveId?) {
                    if (productsByShelveId != null && productsByShelveId.data != null && productsByShelveId.data!!
                            .data != null && productsByShelveId.data!!.data?.size!! > 0
                    ) {
                        lstProducts = productsByShelveId.data!!.data
                        if (lstProducts != null && lstProducts!!.size > 0) {
                            lytInventoryUoms.setVisibility(View.VISIBLE)
                            if (lstProducts!!.size == 1) {
                                if (isFragmentAttached) txtInventoryUoms?.setText(
                                    String.format(
                                        getString(
                                            R.string.txt_inventory_uom_settings_item
                                        ), "1"
                                    )
                                )
                            } else {
                                if (isFragmentAttached) txtInventoryUoms?.setText(
                                    String.format(
                                        getString(
                                            R.string.txt_inventory_uom_settings_items
                                        ), lstProducts!!.size.toString() + ""
                                    )
                                )
                            }
                        } else {
                            lytInventoryUoms.setVisibility(View.GONE)
                        }
                    } else {
                        lytInventoryUoms.setVisibility(View.GONE)
                    }
                }

                override fun errorResponse(error: VolleyError?) {}
            })
    }

    private fun callInventoryShelvesData() {
        OutletInventoryApi.findAllStockShelvesByOutlet(
            activity,
            object : OutletInventoryApi.OutletStockShelvesListener {
                override fun onStockShelveListSuccess(allStockShelveData: InventoryResponse.AllStockShelvesByOutlet?) {
                    if (isFragmentAttached) {
                        loader.setVisibility(View.GONE)
                        if (allStockShelveData != null && allStockShelveData.data != null && allStockShelveData.data!!
                                .size!! > 0
                        ) {
                            //display list
                            lytNoInventory.setVisibility(View.GONE)
                            lstInventoryLists.setVisibility(View.VISIBLE)
                            displayListOfStocks(allStockShelveData.data!!)
                            saveShelvesToSharedPrefs(allStockShelveData.data!!)
                        } else {
                            //display no item page
                            lytNoInventory.setVisibility(View.VISIBLE)
                            lstInventoryLists.setVisibility(View.GONE)
                        }
                    }
                }

                override fun errorResponse(error: VolleyError?) {
                    loader.setVisibility(View.GONE)
                }
            })
    }

    private fun saveShelvesToSharedPrefs(inventoryShelvesList: List<InventoryShelves>) {
        val shelveFilters: MutableList<Shelve.ShelveFilter> = ArrayList<Shelve.ShelveFilter>()
        for (i in inventoryShelvesList.indices) {
            val shelve = Shelve.ShelveFilter()
            shelve.isShelveSelected = false
            shelve.shelveId = inventoryShelvesList[i].shelveId
            shelve.shelveName = inventoryShelvesList[i].shelveName
            shelveFilters.add(shelve)
        }
        Shelve.saveShelveListToSharedPrefs(shelveFilters)
    }

    fun displayListOfStocks(stockShelveDataList: List<InventoryShelves>) {
        val shelveList: MutableList<InventoryDataManager.InventoryShelvesUIModel> = ArrayList<InventoryDataManager.InventoryShelvesUIModel>()
        for (i in stockShelveDataList.indices) {
            val shelve = InventoryDataManager.InventoryShelvesUIModel()
            shelve.inventoryShelves = stockShelveDataList[i]
            shelve.isBuyerAdminMessage = false
            shelveList.add(shelve)
        }
        val buyerAdminMessage = InventoryDataManager.InventoryShelvesUIModel()
        buyerAdminMessage.isBuyerAdminMessage = true
        shelveList.add(buyerAdminMessage)
        lstInventoryLists.setAdapter(InventoryDashboardListsAdapter(requireActivity(), shelveList))
    }

    override fun onResume() {
        super.onResume()
        if (isFragmentAttached) refreshInventoryList()
    }

    companion object {
        fun newInstance(): InventoryDashboardListsFragment {
            return InventoryDashboardListsFragment()
        }
    }
}