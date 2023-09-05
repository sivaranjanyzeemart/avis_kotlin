package zeemart.asia.buyers.helper.imgpostprocessing

import android.util.Log
import org.opencv.core.CvType
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import java.util.*

object MathUtils {
    fun toMatOfPointInt(mat: MatOfPoint2f): MatOfPoint {
        val matInt = MatOfPoint()
        mat.convertTo(matInt, CvType.CV_32S)
        return matInt
    }

    fun toMatOfPointFloat(mat: MatOfPoint): MatOfPoint2f {
        val matFloat = MatOfPoint2f()
        mat.convertTo(matFloat, CvType.CV_32FC2)
        return matFloat
    }

    //calculates the angle formed by three points
    fun angle(p1: Point, p2: Point, p0: Point): Double {
        val dx1 = p1.x - p0.x
        val dy1 = p1.y - p0.y
        val dx2 = p2.x - p0.x
        val dy2 = p2.y - p0.y
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
    }

    //to scale points to match preprocessed image size
    fun scaleRectangle(original: MatOfPoint2f, scale: Double): MatOfPoint2f {
        Log.d("IMGPROCESSING_SCALE", Arrays.toString(original.toArray()))
        Log.d("IMGPROCESSING_SCALE", "scale$scale****")
        val originalPoints = original.toList()
        val resultPoints: MutableList<Point> = ArrayList()
        for (point in originalPoints) {
            resultPoints.add(Point(point.x * scale, point.y * scale))
        }
        val result = MatOfPoint2f()
        result.fromList(resultPoints)
        return result
    }
}