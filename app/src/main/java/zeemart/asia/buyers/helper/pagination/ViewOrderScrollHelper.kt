package zeemart.asia.buyers.helper.pagination

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViewOrderScrollHelper(
    context: Context?,
    recyclerView: RecyclerView,
    layoutManager: LinearLayoutManager,
    private val viewOrderScrollCallback: ViewOrderScrollCallback
) : PaginationListScrollHelper(context, recyclerView, layoutManager, viewOrderScrollCallback) {
    private var visibleThreshold = 30
    private var loading = true
    private var previousTotal = 0
    private var allDataLoaded = false
    private var isScrollStateIdle = false
    private var posFirst = 0

    interface ViewOrderScrollCallback : ScrollCallback {
        fun isHideSummaryGraph(isHide: Boolean)
    }

    override fun setOnScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrollStateIdle = if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    true
                } else {
                    false
                }
                if (isScrollStateIdle && posFirst == 0) {
                    viewOrderScrollCallback.isHideSummaryGraph(false)
                } else if (isScrollStateIdle && posFirst > 0) {
                    viewOrderScrollCallback.isHideSummaryGraph(true)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = recyclerView.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false
                        previousTotal = totalItemCount
                    }
                }
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {

                    // Do something
                    viewOrderScrollCallback.loadMore()
                    loading = true
                }
                posFirst = layoutManager.findFirstCompletelyVisibleItemPosition()
                val posLast = layoutManager.findLastCompletelyVisibleItemPosition()
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING
                    && posFirst == 0
                ) {
                    viewOrderScrollCallback.isHideSummaryGraph(false)
                } else if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING && posFirst > 0) {
                    viewOrderScrollCallback.isHideSummaryGraph(true)
                }
            }
        })
    }

    fun updateTotalItemCount(currentPage: Int, totalPage: Int) {
        allDataLoaded = if (currentPage == totalPage) true else false
    }

    fun updateScrollListener(recyclerView: RecyclerView?, layoutManager: LinearLayoutManager?) {
        this.recyclerView = recyclerView!!
        this.layoutManager = layoutManager!!
        visibleThreshold = 30
        loading = true
        previousTotal = 0
        allDataLoaded = false
    }
}