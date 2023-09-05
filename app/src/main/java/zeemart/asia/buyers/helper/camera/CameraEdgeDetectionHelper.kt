package zeemart.asia.buyers.helper.camera

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*

/**
 * class holding helper methods for edge detection
 */
object CameraEdgeDetectionHelper {
    const val defaultWidth = 1080
    const val defaultHeight = 1920
    @JvmField
    val defaultTl = Point(180.0, 320.0)
    @JvmField
    val defaultTr = Point(900.0, 320.0)
    @JvmField
    val defaultBr = Point(900.0, 1600.0)
    @JvmField
    val defaultBl = Point(180.0, 1600.0)
    var pic: Mat? = null
    var corners: Corners? = null
    fun cropPicture(picture: Mat?, pts: List<Point>): Mat {

        //pts.forEach { Log.i(TAG, "point: " + it.toString()) }
        val tl = pts[0]
        val tr = pts[1]
        val br = pts[2]
        val bl = pts[3]
        val widthA = Math.sqrt(Math.pow(br.x - bl.x, 2.0) + Math.pow(br.y - bl.y, 2.0))
        val widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2.0) + Math.pow(tr.y - tl.y, 2.0))
        val dw = Math.max(widthA, widthB)
        val maxWidth = dw.toInt()
        val heightA = Math.sqrt(Math.pow(tr.x - br.x, 2.0) + Math.pow(tr.y - br.y, 2.0))
        val heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2.0) + Math.pow(tl.y - bl.y, 2.0))
        val dh = Math.max(heightA, heightB)
        val maxHeight = dh.toInt()
        val croppedPic = Mat(maxHeight, maxWidth, CvType.CV_8UC4)
        val src_mat = Mat(4, 1, CvType.CV_32FC2)
        val dst_mat = Mat(4, 1, CvType.CV_32FC2)
        src_mat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y)
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh)
        val m = Imgproc.getPerspectiveTransform(src_mat, dst_mat)
        Imgproc.warpPerspective(picture, croppedPic, m, croppedPic.size())
        m.release()
        src_mat.release()
        dst_mat.release()
        return croppedPic
    }

    fun sortPoints(points: List<Point>?): List<Point> {
        val p0: Point
        val p1: Point
        val p2: Point
        val p3: Point
        val estimatedPoints: MutableList<Point> = ArrayList()
        p0 = Collections.min(points) { o1, o2 ->
            val o1XPlusY = o1.x + o1.y
            val o2XPlusY = o2.x + o2.y
            o1XPlusY.compareTo(o2XPlusY)
        }
        p1 = Collections.max(points) { o1, o2 ->
            val o1XPlusY = o1.x - o1.y
            val o2XPlusY = o2.x - o2.y
            o1XPlusY.compareTo(o2XPlusY)
        }
        p2 = Collections.max(points) { o1, o2 ->
            val o1XPlusY = o1.x + o1.y
            val o2XPlusY = o2.x + o2.y
            o1XPlusY.compareTo(o2XPlusY)
        }
        p3 = Collections.min(points) { o1, o2 ->
            val o1XPlusY = o1.x - o1.y
            val o2XPlusY = o2.x - o2.y
            o1XPlusY.compareTo(o2XPlusY)
        }
        estimatedPoints.add(p0)
        estimatedPoints.add(p1)
        estimatedPoints.add(p2)
        estimatedPoints.add(p3)
        return estimatedPoints
    }

    fun getCorners(contours: ArrayList<MatOfPoint>, size: Size?): Corners? {
        val indexTo: Int
        indexTo = if (contours.size >= 0 && contours.size <= 5) {
            contours.size - 1
        } else {
            4
        }
        for (index in contours.indices) {
            if (index >= 0 && index <= indexTo) {
                val c2f = MatOfPoint2f(*contours[index].toArray())
                val peri = Imgproc.arcLength(c2f, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)
                val points = Arrays.asList(*approx.toArray())
                // select biggest 4 angles polygon
                if (points.size >= 4) {
                    val foundPoints = sortPoints(points)
                    return Corners(foundPoints, size)
                }
            } else {
                return null
            }
        }
        return null
    }

    fun findContours(src: Mat): ArrayList<MatOfPoint> {
        val grayImage: Mat
        val cannedImage: Mat
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(9.0, 9.0))
        val dilate: Mat
        val size = Size(src.size().width, src.size().height)
        grayImage = Mat(size, CvType.CV_8UC4)
        cannedImage = Mat(size, CvType.CV_8UC1)
        dilate = Mat(size, CvType.CV_8UC1)
        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(grayImage, grayImage, Size(5.0, 5.0), 0.0)
        Imgproc.threshold(grayImage, grayImage, 20.0, 255.0, Imgproc.THRESH_TRIANGLE)
        Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0)
        Imgproc.dilate(cannedImage, dilate, kernel)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            dilate,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )
        Collections.sort(contours) { o1, o2 ->
            val areaO1 = Imgproc.contourArea(o1)
            val areaO2 = Imgproc.contourArea(o2)
            areaO2.compareTo(areaO1)
        }
        //contours.sortByDescending { p: MatOfPoint -> Imgproc.contourArea(p) }
        hierarchy.release()
        grayImage.release()
        cannedImage.release()
        kernel.release()
        dilate.release()
        return contours
    }

    fun isTriangularEdgesPresent(corners: Corners): Boolean {
        val tl = corners.points[0]
        val tr = corners.points[1]
        val br = corners.points[2]
        val bl = corners.points[3]
        val topXChange = (tr.x - tl.x).toInt()
        val rightYChange = (br.y - tr.y).toInt()
        val bottomXChange = (br.x - bl.x).toInt()
        val leftYChange = (bl.y - tl.y).toInt()
        //Log.d("Corners_count_change",topXChange+"**"+rightYChange+"**"+bottomXChange+"**"+leftYChange);
        return if (topXChange > 0 && rightYChange > 0 && bottomXChange > 0 && leftYChange > 0) false else true
    }

    fun enhancePicture(src: Bitmap): Bitmap {
        val src_mat = Mat()
        Utils.bitmapToMat(src, src_mat)
        Imgproc.cvtColor(src_mat, src_mat, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.adaptiveThreshold(
            src_mat,
            src_mat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            15,
            15.0
        )
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.RGB_565)
        Utils.matToBitmap(src_mat, result, true)
        src_mat.release()
        return result
    }

    class Corners(var points: List<Point>, var size: Size?)
}