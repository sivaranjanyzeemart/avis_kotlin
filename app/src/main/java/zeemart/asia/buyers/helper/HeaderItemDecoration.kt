package zeemart.asia.buyers.helper

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

/**
 * Created by ParulBhandari on 12/28/2017.
 */
class HeaderItemDecoration constructor(
    recyclerView: RecyclerView,
    private val mListener: StickyHeaderInterface
) : ItemDecoration() {
    private var mStickyHeaderHeight: Int = 0

    //stops from creating header view again and again
    private var mCurrentHeaderIndex: Int = 0
    private var headerView: View? = null

    init {

        // On Sticky Header Click
        recyclerView.addOnItemTouchListener(object : OnItemTouchListener {
            public override fun onInterceptTouchEvent(
                recyclerView: RecyclerView,
                motionEvent: MotionEvent
            ): Boolean {
                if (MotionEvent.ACTION_DOWN == motionEvent.getAction() && motionEvent.getY() <= mStickyHeaderHeight) {
                    // Handle the clicks on the header here ...
                    val childView: View? =
                        recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY())
                    if (childView != null) {
                        val headerPosition: Int = recyclerView.getChildAdapterPosition(childView)
                        mListener.onHeaderClicked(headerView, headerPosition)
                    }
                    return true
                }
                return false
            }

            public override fun onTouchEvent(
                recyclerView: RecyclerView,
                motionEvent: MotionEvent
            ) {
            }

            public override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    public override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val topChild: View? = parent.getChildAt(0)
        if (topChild == null) {
            return
        }
        val topChildPosition: Int = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }
        val currentHeader: View? = getHeaderViewForItem(topChildPosition, parent)
        fixLayoutSize(parent, currentHeader)
        val contactPoint: Int = currentHeader!!.getBottom()
        val childInContact: View? = getChildInContact(parent, contactPoint)
        if (childInContact == null) {
            return
        }
        if (mListener.isHeader(parent.getChildAdapterPosition(childInContact))) {
            moveHeader(c, currentHeader, childInContact)
            return
        }
        drawHeader(c, currentHeader)
    }

    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View? {
        /*int headerPosition = mListener.getHeaderPositionForItem(itemPosition);
        int layoutResId = mListener.getHeaderLayout(headerPosition);
        View header = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        mListener.bindHeaderData(header, headerPosition);
        return header;*/
        val headerPosition: Int = mListener.getHeaderPositionForItem(itemPosition)
        if (headerPosition != mCurrentHeaderIndex || headerView == null) {
            val layoutResId: Int = mListener.getHeaderLayout(headerPosition)
            val header: View =
                LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false)
            mListener.bindHeaderData(header, headerPosition)
            mCurrentHeaderIndex = headerPosition
            headerView = header
        }
        return headerView
    }

    private fun drawHeader(c: Canvas, header: View?) {
        c.save()
        c.translate(0f, 0f)
        header!!.draw(c)
        c.restore()
    }

    private fun moveHeader(c: Canvas, currentHeader: View?, nextHeader: View) {
        c.save()
        c.translate(0f, (nextHeader.getTop() - currentHeader!!.getHeight()).toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        var childInContact: View? = null
        for (i in 0 until parent.getChildCount()) {
            val child: View = parent.getChildAt(i)
            if (child.getBottom() > contactPoint) {
                if (child.getTop() <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    /**
     * Properly measures and layouts the top sticky header.
     *
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun fixLayoutSize(parent: ViewGroup, view: View?) {

        // Specs for parent (RecyclerView)
        val widthSpec: Int =
            View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY)
        val heightSpec: Int =
            View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec: Int = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.getPaddingLeft() + parent.getPaddingRight(),
            view!!.getLayoutParams().width
        )
        val childHeightSpec: Int = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.getPaddingTop() + parent.getPaddingBottom(),
            view.getLayoutParams().height
        )
        view.measure(childWidthSpec, childHeightSpec)
        view.layout(
            0,
            0,
            view.getMeasuredWidth(),
            view.getMeasuredHeight().also({ mStickyHeaderHeight = it })
        )
    }

    open interface StickyHeaderInterface {
        /**
         * This method gets called by [HeaderItemDecoration] to fetch the position of the header item in the adapter
         * that is used for (represents) item at specified position.
         *
         * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
         * @return int. Position of the header item in the adapter.
         */
        fun getHeaderPositionForItem(itemPosition: Int): Int

        /**
         * This method gets called by [HeaderItemDecoration] to get layout resource id for the header item at specified adapter's position.
         *
         * @param headerPosition int. Position of the header item in the adapter.
         * @return int. Layout resource id.
         */
        fun getHeaderLayout(headerPosition: Int): Int

        /**
         * This method gets called by [HeaderItemDecoration] to setup the header View.
         *
         * @param header         View. Header to set the data on.
         * @param headerPosition int. Position of the header item in the adapter.
         */
        fun bindHeaderData(header: View?, headerPosition: Int)

        /**
         * This method gets called by [HeaderItemDecoration] to verify whether the item represents a header.
         *
         * @param itemPosition int.
         * @return true, if item at the specified adapter's position represents a header.
         */
        fun isHeader(itemPosition: Int): Boolean

        /**
         * This method get called when the header is tapped by the user
         *
         * @param headerView
         * @param position
         */
        fun onHeaderClicked(headerView: View?, position: Int)
    }
}