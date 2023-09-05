package zeemart.asia.buyers.helper.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import zeemart.asia.buyers.R

class CustomViewForTransparentCircle : View {
    lateinit var bm: Bitmap
    var cv: Canvas? = null
    var eraser: Paint? = null
    private val CIRCLE_WIDTH = 2
    private val CIRCLE_HEIGHT = 2
    private val TOP = 2
    private val LEFT = 2

    constructor(context: Context?) : super(context) {
        Init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        Init()
    }

    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        Init()
    }

    private fun Init() {
        eraser = Paint()
        eraser!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        eraser!!.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            cv = Canvas(bm)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width
        val h = height
        val radius = if (w > h) h / CIRCLE_HEIGHT else w / CIRCLE_WIDTH
        bm!!.eraseColor(Color.TRANSPARENT)
        cv!!.drawColor(resources.getColor(R.color.translucent))
        cv!!.drawCircle(
            (w / CIRCLE_WIDTH).toFloat(),
            (h / CIRCLE_HEIGHT).toFloat(),
            radius.toFloat(),
            eraser!!
        )
        canvas.drawBitmap(bm!!, LEFT.toFloat(), TOP.toFloat(), null)
        super.onDraw(canvas)
    }
}