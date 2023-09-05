package zeemart.asia.buyers.helper.pagination

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by RajPrudhviMarella on 24/sep/2020.
 */
class DealsAndEssentialsProductsListScrollHelper(
    context: Context?,
    recyclerView: RecyclerView,
    layoutManager: LinearLayoutManager,
    private val viewOrderScrollCallback: DealsAndEssentialsProductsScrollCallback
) : PaginationListScrollHelper(context, recyclerView, layoutManager, viewOrderScrollCallback) {
    private val visibleThreshold = 30
    private var loading = true
    private var previousTotal = 0
    private var isScrollStateIdle = false
    private var posFirst = 0

    interface DealsAndEssentialsProductsScrollCallback : ScrollCallback {
        fun isHideImage(isHide: Boolean)
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
                    viewOrderScrollCallback.isHideImage(false)
                } else if (isScrollStateIdle && posFirst > 0) {
                    viewOrderScrollCallback.isHideImage(true)
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
                    viewOrderScrollCallback.isHideImage(false)
                } else if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING && posFirst > 0) {
                    viewOrderScrollCallback.isHideImage(true)
                }
            }
        })
    }
}