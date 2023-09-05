package zeemart.asia.buyers.helper.pagination

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class PaginationListScrollHelper(
    protected var context: Context?,
    protected var recyclerView: RecyclerView,
    protected var layoutManager: LinearLayoutManager,
    protected var scrollCallback: ScrollCallback
) {
    interface ScrollCallback {
        fun loadMore()
    }

    open fun setOnScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItems = recyclerView.adapter!!.itemCount
                val pos = layoutManager.findLastCompletelyVisibleItemPosition()
                if (pos == totalItems - 1) {
                    scrollCallback.loadMore()
                }
            }
        })
    }
}