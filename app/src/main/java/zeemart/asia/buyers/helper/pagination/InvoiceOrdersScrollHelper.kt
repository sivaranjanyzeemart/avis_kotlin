package zeemart.asia.buyers.helper.pagination

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by RajprudhviMarella on 04/Jan/2021.
 */
class InvoiceOrdersScrollHelper(
    context: Context?,
    recyclerView: RecyclerView,
    layoutManager: LinearLayoutManager,
    private val deliveryScrollCallback: DeliveryScrollCallback
) : PaginationListScrollHelper(context, recyclerView, layoutManager, deliveryScrollCallback) {
    var loading = true
    private var previousTotal = 0
    private var visibleThreshold = 50
    private var isScrollStateIdle = false
    private var posFirst = 0

    interface DeliveryScrollCallback : ScrollCallback {
        fun isHideFilters(isHide: Boolean)
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
                    deliveryScrollCallback.isHideFilters(false)
                } else if (isScrollStateIdle && posFirst > 0) {
                    deliveryScrollCallback.isHideFilters(true)
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
                    scrollCallback.loadMore()
                    loading = true
                }
                posFirst = layoutManager.findFirstCompletelyVisibleItemPosition()
                val posLast = layoutManager.findLastCompletelyVisibleItemPosition()
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING
                    && posFirst == 0
                ) {
                    deliveryScrollCallback.isHideFilters(false)
                } else if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING && posFirst > 0) {
                    deliveryScrollCallback.isHideFilters(true)
                }
            }
        })
    }

    fun updateScrollListener(recyclerView: RecyclerView?, layoutManager: LinearLayoutManager?) {
        this.recyclerView = recyclerView!!
        this.layoutManager = layoutManager!!
        visibleThreshold = 50
        loading = true
        previousTotal = 0
    }
}