package zeemart.asia.buyers.helper.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class VerticalTextView(context: Context?, attrs: AttributeSet?) : AppCompatTextView(
    context!!, attrs
) {
    var topDown = false

    init {
        val gravity = gravity
        topDown =
            if (Gravity.isVertical(gravity) && gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.BOTTOM) {
                setGravity(gravity and Gravity.HORIZONTAL_GRAVITY_MASK or Gravity.TOP)
                false
            } else {
                true
            }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        val textPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState
        canvas.save()
        if (topDown) {
            canvas.translate(width.toFloat(), 0f)
            canvas.rotate(ROTATE_CLOCKWISE.toFloat())
        } else {
            canvas.translate(0f, height.toFloat())
            canvas.rotate(ROTATE_ANTICLOCKWISE.toFloat())
        }
        canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())
        layout.draw(canvas)
        canvas.restore()
    }

    companion object {
        private const val ROTATE_CLOCKWISE = 90
        private const val ROTATE_ANTICLOCKWISE = -90
    }
}