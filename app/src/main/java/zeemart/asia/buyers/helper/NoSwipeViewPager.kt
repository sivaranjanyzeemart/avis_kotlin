package zeemart.asia.buyers.helper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Created by ParulBhandari on 11/21/2017.
 * Helper class to disable swiping in view pager
 */
class NoSwipeViewPager constructor(context: Context?, attrs: AttributeSet?) : ViewPager(
    (context)!!, attrs
) {
    private var enabled: Boolean = true
    public override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enabled) {
            return super.onTouchEvent(event)
        }
        return false
    }

    public override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (enabled) {
            return super.onInterceptTouchEvent(event)
        }
        return false
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}