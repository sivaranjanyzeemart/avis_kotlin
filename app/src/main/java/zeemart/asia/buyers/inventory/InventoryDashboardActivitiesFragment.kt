package zeemart.asia.buyers.inventory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.InventoryDashboardActivitiesAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.pagination.InventoryDashboardActivitiesScrollHelper
import zeemart.asia.buyers.helper.pagination.PaginationListScrollHelper
import zeemart.asia.buyers.models.StockageType
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.StockCountEntriesMgr
import zeemart.asia.buyers.network.OutletInventoryApi

/**
 * A simple [Fragment] subclass.
 */
class InventoryDashboardActivitiesFragment : Fragment() {
    private lateinit var lstInventoryActivities: RecyclerView
    private lateinit var lytNoActivities: LinearLayout
    private lateinit var loader: CustomLoadingViewBlue
    private var currentPageNumber = 0
    private var totalPages = 1
    private val pageSize = 50
    private var isFragmentAttached = false
    private var txtNoInventorySetUp: TextView? = null
    private var txtNoInventorySetUpDetails: TextView? = null
    private var stockEntriesList: MutableList<StockCountEntriesMgr> =
        ArrayList<StockCountEntriesMgr>()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    override fun onResume() {
        super.onResume()
        if (isFragmentAttached) refreshInventoryHistory()
    }

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
        savedInstanceState: Bundle?,
    ): View? {
        val v: View =
            inflater.inflate(R.layout.fragment_inventory_dashboard_activities, container, false)
        lstInventoryActivities =
            v.findViewById<RecyclerView>(R.id.lst_inventory_dashboard_activities)
        val linearLayoutManager = LinearLayoutManager(activity)
        lstInventoryActivities.setLayoutManager(linearLayoutManager)
        val scrollHelper = InventoryDashboardActivitiesScrollHelper(
            activity, lstInventoryActivities, linearLayoutManager, object :
                PaginationListScrollHelper.ScrollCallback {
                override fun loadMore() {
                    if (currentPageNumber < totalPages) getActivitiesData(false, true)
                }
            })
        scrollHelper.setOnScrollListener()
        lytNoActivities = v.findViewById<LinearLayout>(R.id.lyt_no_activity)
        lytNoActivities.setVisibility(View.GONE)
        txtNoInventorySetUp = v.findViewById<TextView>(R.id.txt_no_activity_history)
        txtNoInventorySetUpDetails = v.findViewById<TextView>(R.id.txt_no_activity_history_detail)
        swipeRefreshLayout =
            v.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_inventory_activities)
        CommonMethods.hideSwipeRefreshLoader(swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                swipeRefreshLayout.setRefreshing(false)
                if (isFragmentAttached) refreshInventoryHistory()
            }
        })
        loader = v.findViewById<CustomLoadingViewBlue>(R.id.loader_inventory_activity)
        loader.setVisibility(View.GONE)
        setFont()
        return v
    }

    fun refreshInventoryHistory() {
        loader.setVisibility(View.VISIBLE)
        val count = InventoryDataManager.selectedFiltersForActivityCount
        if (count > 0) loadActivities(true) else loadActivities(false)
    }

    private fun setFont() {
        ZeemartBuyerApp.setTypefaceView(
            txtNoInventorySetUp,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoInventorySetUpDetails,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    fun loadActivities(isFilterApplied: Boolean) {
        //save the stock type filter in SharedPrefs
        if (!isFilterApplied) StockageType.initializeStockTypeListToSharedPrefs()
        loader.setVisibility(View.VISIBLE)
        currentPageNumber = 0
        totalPages = 1
        lstInventoryActivities.setAdapter(null)
        stockEntriesList.clear()
        if (isFragmentAttached) getActivitiesData(isFilterApplied, false)
    }

    private fun getActivitiesData(isFilterApplied: Boolean, isLoadMore: Boolean) {
        if (!isLoadMore) {
            lstInventoryActivities.setAdapter(null)
            stockEntriesList = ArrayList<StockCountEntriesMgr>()
        }
        if (currentPageNumber < totalPages) {
            currentPageNumber = currentPageNumber + 1
            val apiParamsHelper: ApiParamsHelper = getApiParamsHelper(isFilterApplied)
            OutletInventoryApi.getCountEntriesByOutlet(
                activity,
                apiParamsHelper,
                object : OutletInventoryApi.OutletStockCountEntriesListener {
                    override fun onStockShelveListSuccess(stockCountEntriesByOutletData: InventoryResponse.StockCountEntriesByOutlet?) {
                        if (isFragmentAttached) {
                            loader.setVisibility(View.GONE)
                            lstInventoryActivities.setVisibility(View.VISIBLE)
                            lytNoActivities.setVisibility(View.GONE)
                            if (stockCountEntriesByOutletData != null && stockCountEntriesByOutletData.data != null
                                && stockCountEntriesByOutletData.data!!
                                    .data != null && stockCountEntriesByOutletData.data!!
                                    .data?.size!! > 0
                            ) {
                                totalPages =
                                    stockCountEntriesByOutletData.data!!.numberOfPages!!

                                if (!isLoadMore) {
                                    stockEntriesList = ArrayList<StockCountEntriesMgr>()
                                }
                                stockEntriesList = createListWithHeadersData(
                                    stockEntriesList,
                                    stockCountEntriesByOutletData.data!!.data!! as MutableList<StockCountEntriesMgr>,
                                    isLoadMore
                                )
                                if (isLoadMore) {
                                    if (lstInventoryActivities.getAdapter() != null) lstInventoryActivities!!.getAdapter()
                                        ?.notifyDataSetChanged()
                                } else {
                                    lstInventoryActivities.setAdapter(
                                        InventoryDashboardActivitiesAdapter(
                                            activity!!, stockEntriesList
                                        )
                                    )
                                }
                            } else {
                                if (stockEntriesList.size == 0) {
                                    lstInventoryActivities.setVisibility(View.GONE)
                                    lytNoActivities.setVisibility(View.VISIBLE)
                                    if (isFilterApplied) {
                                        txtNoInventorySetUp?.setText(getString(R.string.txt_no_results))
                                        txtNoInventorySetUpDetails?.setText(R.string.txt_try_removing_filters)
                                    } else {
                                        txtNoInventorySetUp?.setText(getString(R.string.txt_no_activity_yet))
                                        txtNoInventorySetUpDetails?.setText(R.string.txt_no_activity_yet_detail)
                                    }
                                }
                            }
                        }
                    }

                    override fun errorResponse(error: VolleyError?) {
                        loader.setVisibility(View.GONE)
                        if (stockEntriesList.size == 0) {
                            lytNoActivities.setVisibility(View.VISIBLE)
                        }
                        //if somehow that page was not loaded, decrement the currentPageCount;
                    }
                })
        }
    }

    private fun getApiParamsHelper(isFilterApplied: Boolean): ApiParamsHelper {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSortOrder(ApiParamsHelper.SortOrder.DESC)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.TIME_CREATED)
        apiParamsHelper.setPageNumber(currentPageNumber)
        apiParamsHelper.setPageSize(pageSize)
        if (isFilterApplied) {
            val selectedShelveFilters = InventoryDataManager.selectedShelveIds
            val selectedStockageTypes: List<StockageType?> =
                InventoryDataManager.selectedStockageTypes as List<StockageType>
            apiParamsHelper.setStockageTypes(selectedStockageTypes.toTypedArray())
            apiParamsHelper.setShelveIds(selectedShelveFilters.toTypedArray())
            apiParamsHelper.setRemoveSquareBrackets(true)
        }
        return apiParamsHelper
    }

    fun createListWithHeadersData(
        superList: MutableList<StockCountEntriesMgr>,
        stockCountEntriesList: MutableList<StockCountEntriesMgr>,
        isLoadMore: Boolean,
    ): MutableList<StockCountEntriesMgr> {
        if (stockCountEntriesList.size > 0) {
            var item = StockCountEntriesMgr()
            item.isHeader = true
            val date: String = DateHelper.getDateInDateMonthYearFormat(
                stockCountEntriesList[0].timeUpdated
            )
            val day: String =
                DateHelper.getDateIn3LetterDayFormat(stockCountEntriesList[0].timeUpdated)
            var headerDate: String = date
            item.headerDate = date
            item.headerDay = day
            item.timeUpdated = stockCountEntriesList[0].timeUpdated
            if (superList.size > 0) {
                val lastDataSuperList: StockCountEntriesMgr = superList[superList.size - 1]
                val lastEntryTimeUpdatedDate: String =
                    DateHelper.getDateInDateMonthYearFormat(lastDataSuperList.timeUpdated)
                if (!headerDate.equals(lastEntryTimeUpdatedDate, ignoreCase = true)) {
                    stockCountEntriesList.add(0, item)
                    headerDate = item.headerDate!!
                }
            } else {
                stockCountEntriesList.add(0, item)
            }
            for (i in stockCountEntriesList.indices) {
                item = StockCountEntriesMgr()
                item.isHeader = true
                val dateHeader: String = DateHelper.getDateInDateMonthYearFormat(
                    stockCountEntriesList[i].timeUpdated
                )
                val dayHeader: String = DateHelper.getDateIn3LetterDayFormat(
                    stockCountEntriesList[i].timeUpdated
                )
                if (!headerDate.equals(dateHeader, ignoreCase = true)) {
                    headerDate = dateHeader
                    item.headerDate = dateHeader
                    item.headerDay = dayHeader
                    item.timeUpdated = stockCountEntriesList[i].timeUpdated
                    stockCountEntriesList.add(i, item)
                }
            }
        }
        superList.addAll(stockCountEntriesList)
        return superList
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(): InventoryDashboardActivitiesFragment {
            return InventoryDashboardActivitiesFragment()
        }
    }
}