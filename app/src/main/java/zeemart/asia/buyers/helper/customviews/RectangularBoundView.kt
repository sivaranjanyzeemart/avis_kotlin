package zeemart.asia.buyers.helper.customviews

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import org.opencv.core.Point
import org.opencv.core.Size
import zeemart.asia.buyers.R
import zeemart.asia.buyers.helper.camera.CameraEdgeDetectionHelper
import zeemart.asia.buyers.helper.camera.CameraEdgeDetectionHelper.Corners
import java.util.*

class RectangularBoundView : View {
    private val rectPaint = Paint()
    private val circlePaint = Paint()
    private val innerCirclePaint = Paint()
    private var ratioX = 1.0
    private var ratioY = 1.0
    private var tl = Point()
    private var tr = Point()
    private var br = Point()
    private var bl = Point()
    private val path = Path()
    private var point2Move = Point()
    private var cropMode = true
    private var latestDownX = 0.0f
    private var latestDownY = 0.0f
    private var mListener: AdjustImageListener? = null
    fun setOnAdjustImageListener(listener: AdjustImageListener?) {
        mListener = listener
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    fun init() {
        rectPaint.color = Color.BLUE
        rectPaint.isAntiAlias = true
        rectPaint.isDither = true
        rectPaint.strokeWidth = 6f
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeJoin = Paint.Join.ROUND // set the join to round you want
        rectPaint.strokeCap = Paint.Cap.ROUND // set the paint cap to round too
        rectPaint.pathEffect = CornerPathEffect(20f)
        //Inner Circle
        innerCirclePaint.color = resources.getColor(R.color.color_azul_two)
        innerCirclePaint.isDither = true
        innerCirclePaint.isAntiAlias = true
        innerCirclePaint.style = Paint.Style.FILL
        circlePaint.color = Color.WHITE
        circlePaint.isDither = true
        circlePaint.isAntiAlias = true
        circlePaint.strokeWidth = 6f
        circlePaint.style = Paint.Style.STROKE
    }

    fun onCornersDetected(corners: Corners?) {
        if (corners != null) {
            ratioX = corners.size!!.width / this.layoutParams.width
            ratioY = corners.size!!.height / this.layoutParams.height
            tl = corners.points[0]
            tr = corners.points[1]
            br = corners.points[2]
            bl = corners.points[3]
            //draw only if there are no triangular edges
            //if(!CameraEdgeDetectionHelper.isTriangularEdgesPresent(corners)){
            resize()
            path.reset()
            path.moveTo(tl.x.toFloat(), tl.y.toFloat())
            path.lineTo(tr.x.toFloat(), tr.y.toFloat())
            path.lineTo(br.x.toFloat(), br.y.toFloat())
            path.lineTo(bl.x.toFloat(), bl.y.toFloat())
            path.close()
            invalidate()
            //}
        }
    }

    fun onCornersNotDetected() {
        path.reset()
        invalidate()
    }

    fun onCorners2Crop(corners: Corners, size: Size) {
        cropMode = true
        tl = if (corners.points[0] != null) {
            corners.points[0]
        } else {
            CameraEdgeDetectionHelper.defaultTl
        }
        tr = if (corners.points[1] != null) {
            corners.points[1]
        } else {
            CameraEdgeDetectionHelper.defaultTr
        }
        br = if (corners.points[2] != null) {
            corners.points[2]
        } else {
            CameraEdgeDetectionHelper.defaultBr
        }
        bl = if (corners.points[3] != null) {
            corners.points[3]
        } else {
            CameraEdgeDetectionHelper.defaultBl
        }
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        //exclude status bar height
        val statusBarHeight = getStatusBarHeight(context)
        ratioX = size.width / displayMetrics.widthPixels
        ratioY = size.height / (displayMetrics.heightPixels - statusBarHeight)
        resize()
        //movePoints();
    }

    val corners2Crop: List<Point>
        get() {
            reverseSize()
            val points: MutableList<Point> = ArrayList()
            points.add(tl)
            points.add(tr)
            points.add(br)
            points.add(bl)
            return points
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, rectPaint)
        if (cropMode) {
            canvas.drawCircle(tl.x.toFloat(), tl.y.toFloat(), 20f, innerCirclePaint)
            canvas.drawCircle(tr.x.toFloat(), tr.y.toFloat(), 20f, innerCirclePaint)
            canvas.drawCircle(bl.x.toFloat(), bl.y.toFloat(), 20f, innerCirclePaint)
            canvas.drawCircle(br.x.toFloat(), br.y.toFloat(), 20f, innerCirclePaint)
            canvas.drawCircle(tl.x.toFloat(), tl.y.toFloat(), 20f, circlePaint)
            canvas.drawCircle(tr.x.toFloat(), tr.y.toFloat(), 20f, circlePaint)
            canvas.drawCircle(bl.x.toFloat(), bl.y.toFloat(), 20f, circlePaint)
            canvas.drawCircle(br.x.toFloat(), br.y.toFloat(), 20f, circlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!cropMode) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                latestDownX = event.x
                latestDownY = event.y
                calculatePoint2Move(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                point2Move.x = event.x - latestDownX + point2Move.x
                point2Move.y = event.y - latestDownY + point2Move.y
                movePoints()
                latestDownY = event.y
                latestDownX = event.x
            }
            else -> {}
        }
        return true
    }

    private fun calculatePoint2Move(downX: Float, downY: Float) {
        val points: MutableList<Point> = ArrayList()
        points.add(tl)
        points.add(tr)
        points.add(br)
        points.add(bl)
        point2Move = Collections.min(points) { o1, o2 ->
            val o1Val = Math.abs((o1.x - downX) * (o1.y - downY))
            val o2Val = Math.abs((o2.x - downX) * (o2.y - downY))
            o1Val.compareTo(o2Val)
        }
    }

    private fun movePoints() {
        path.reset()
        path.moveTo(tl.x.toFloat(), tl.y.toFloat())
        path.lineTo(tr.x.toFloat(), tr.y.toFloat())
        path.lineTo(br.x.toFloat(), br.y.toFloat())
        path.lineTo(bl.x.toFloat(), bl.y.toFloat())
        path.close()
        invalidate()
        mListener!!.adjustImage()
    }

    private fun resize() {
        tl.x = tl.x / ratioX
        tl.y = tl.y / ratioY
        tr.x = tr.x / ratioX
        tr.y = tr.y / ratioY
        br.x = br.x / ratioX
        br.y = br.y / ratioY
        bl.x = bl.x / ratioX
        bl.y = bl.y / ratioY
    }

    private fun reverseSize() {
        tl.x = tl.x * ratioX
        tl.y = tl.y * ratioY
        tr.x = tr.x * ratioX
        tr.y = tr.y * ratioY
        br.x = br.x * ratioX
        br.y = br.y * ratioY
        bl.x = bl.x * ratioX
        bl.y = bl.y * ratioY
    }

    private fun getNavigationBarHeight(pContext: Context): Int {
        val resources = pContext.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun getStatusBarHeight(pContext: Context): Int {
        val resources = pContext.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    interface AdjustImageListener {
        fun adjustImage()
    }
}