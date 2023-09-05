package zeemart.asia.buyers.helper.pagination

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InvoiceScrollHelper(
    context: Context?,
    recyclerView: RecyclerView,
    layoutManager: LinearLayoutManager,
    private val invoiceScrollCallback: InvoiceScrollCallback
) : PaginationListScrollHelper(context, recyclerView, layoutManager, invoiceScrollCallback) {
    var loading = true
    private var previousTotal = 0
    private val visibleThreshold = 50
    private var posFirst = 0
    private var isScrollStateIdle = false

    interface InvoiceScrollCallback : ScrollCallback {
        fun isHidePaymentDue(isHide: Boolean)
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
                    invoiceScrollCallback.isHidePaymentDue(false)
                } else if (isScrollStateIdle && posFirst > 0) {
                    invoiceScrollCallback.isHidePaymentDue(true)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = recyclerView.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                Log.d(
                    "INVOICE_SCROLL",
                    "TIC-->" + totalItemCount + "VIC-->" + visibleItemCount + "FVI-->" + firstVisibleItem
                )
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false
                        previousTotal = totalItemCount
                    }
                }
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
                    // Do something
                    Log.d("INVOICE_SCROLL", "Inside load more")
                    scrollCallback.loadMore()
                    loading = true
                }
                posFirst = layoutManager.findFirstCompletelyVisibleItemPosition()
                val posLast = layoutManager.findLastCompletelyVisibleItemPosition()
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING
                    && posFirst == 0
                ) {
                    invoiceScrollCallback.isHidePaymentDue(false)
                } else if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING && posFirst > 0) {
                    invoiceScrollCallback.isHidePaymentDue(true)
                }
            }
        })
    }
}