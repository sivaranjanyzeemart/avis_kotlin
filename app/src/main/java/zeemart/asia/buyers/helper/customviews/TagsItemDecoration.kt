package zeemart.asia.buyers.helper.customviews

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class TagsItemDecoration(@param:IntRange(from = 0) private val margin: Int) : ItemDecoration() {
    /**
     * Set different margins for the items inside the recyclerView: no top margin for the first row
     * and no left margin for the first column.
     */
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.bottom = margin
        outRect.right = margin
    }
}