package zeemart.asia.buyers.helper.pagination

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InventoryDashboardActivitiesScrollHelper(
    context: Context?,
    recyclerView: RecyclerView,
    layoutManager: LinearLayoutManager,
    scrollCallback: ScrollCallback
) : PaginationListScrollHelper(context, recyclerView, layoutManager, scrollCallback) {
    var loading = true
    private var previousTotal = 0
    private val visibleThreshold = 50
    override fun setOnScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            }
        })
    }
}