package zeemart.asia.buyers.helper.imgpostprocessing

import android.graphics.Bitmap
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class ImagePostProcessing {
    //public static Bitmap processedBitmap;
    //downscale the bitmap and identify most parallel set of points
    fun getPoint(bitmap: Bitmap?, holderWidth: Int, holderHeight: Int): MatOfPoint2f? {
        val src = ImageUtils.bitmapToMat(bitmap!!)

        // Downscale image for better performance.
        val ratio = DOWNSCALE_IMAGE_SIZE / Math.max(src.width(), src.height())
        //double ratio = 1;
        val downscaledSize = Size(src.width() * ratio, src.height() * ratio)
        val downscaled = Mat(downscaledSize, src.type())
        Imgproc.resize(src, downscaled, downscaledSize)
        val rectangle = getContourPointsLargestPoly(downscaled)
        return if (rectangle == null) {
            null
        } else {
            //return the points closest to the bounding rect
            val result = MathUtils.scaleRectangle(
                rectangle,
                1f / ratio
            )
            val boundingrect = Imgproc.boundingRect(result)
            /*
            //testing the image approximation points
            Log.d("IMAGE_PROCESSING_ALGO",Arrays.toString(result.toArray()));
            Imgproc.rectangle(src,new Point(boundingrect.x,boundingrect.y), new Point(boundingrect.x+boundingrect.width,boundingrect.y+boundingrect.height),new Scalar(0,0,255),10);
            processedBitmap = ImageUtils.matToBitmap(src);*/
            val rectTl = Point(boundingrect.x.toDouble(), boundingrect.y.toDouble())
            val rectTr = Point(
                (boundingrect.x + boundingrect.width).toDouble(),
                boundingrect.y.toDouble()
            )
            val rectBr = Point(
                (boundingrect.x + boundingrect.width).toDouble(),
                (boundingrect.y + boundingrect.height).toDouble()
            )
            val rectBl = Point(
                boundingrect.x.toDouble(),
                (boundingrect.y + boundingrect.height).toDouble()
            )
            val boundingRectPoints = arrayOfNulls<Point>(4)
            boundingRectPoints[0] = rectTl
            boundingRectPoints[1] = rectTr
            boundingRectPoints[2] = rectBr
            boundingRectPoints[3] = rectBl
            //processedBitmap = ImageUtils.matToBitmap(src);
            if (result.toArray().size < 4) {
                getPointsFullScreen(holderWidth, holderHeight)
            } else {
                val exactPointArray =
                    arrayOfNulls<Point>(4)
                for (i in boundingRectPoints.indices) {
                    var closestXDifference = -1.0
                    var closestYDifference = -1.0
                    var point: Point? = Point()
                    for (j in result.toArray().indices) {
                        val absoluteXDiff = Math.abs(
                            boundingRectPoints[i]!!.x - result.toArray()[j].x
                        )
                        val absoluteYDiff = Math.abs(
                            boundingRectPoints[i]!!.y - result.toArray()[j].y
                        )
                        if (closestXDifference == -1.0 && closestYDifference == -1.0) {
                            closestXDifference = absoluteXDiff
                            closestYDifference = absoluteYDiff
                        }
                        if (absoluteXDiff + absoluteYDiff <= closestXDifference + closestYDifference) {
                            closestXDifference = absoluteXDiff
                            closestYDifference = absoluteYDiff
                            point = result.toArray()[j]
                        }
                    }
                    exactPointArray[i] = point
                }
                //Log.d("IMAGE_PROCESSING_FINAL",Arrays.toString(fineTunedResult.toArray()));
                MatOfPoint2f(*exactPointArray)
            }
        }
    }

    /**
     * Image processing algo to get the largest bounding rect.
     * @param src
     * @return
     */
    fun getContourPointsLargestPoly(src: Mat?): MatOfPoint2f? {
        //list of rectangles detected
        val largestRectangles: MutableList<MatOfPoint2f> = ArrayList()
        // Blur the image to filter out the noise.
        val blurred = Mat()
        Imgproc.medianBlur(src, blurred, 19)
        // Set up images to use.
        val gray0 = Mat(blurred.size(), CvType.CV_8U)
        val gray = Mat()
        // For Core.mixChannels.
        val contours: List<MatOfPoint> = ArrayList()
        val sources: MutableList<Mat> = ArrayList()
        sources.add(blurred)
        val destinations: MutableList<Mat> = ArrayList()
        destinations.add(gray0)
        // Find squares in every color plane of the image.
        for (c in 0..2) {
            val ch = intArrayOf(c, 0)
            val fromTo = MatOfInt(*ch)
            Core.mixChannels(sources, destinations, fromTo)
            val rectangles: MutableList<MatOfPoint2f> = ArrayList()
            // HACK: Use Canny instead of zero threshold level.
            // Canny helps to catch squares with gradient shading.
            // NOTE: No kernel size parameters on Java API.
            Imgproc.Canny(gray0, gray, 10.0, 20.0)
            // Dilate Canny output to remove potential holes between edge segments.
            Imgproc.dilate(gray, gray, Mat.ones(Size(3.0, 3.0), 0))
            // Find contours and store them all as a list.
            Imgproc.findContours(
                gray,
                contours,
                Mat(),
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE
            )
            // processedBitmap = ImageUtils.matToBitmap(gray);
            for (contour in contours) {
                val selectedContour = contour
                val contourFloat = MathUtils.toMatOfPointFloat(contour)

                // identify possible sets of points at two different accuracies of approximation
                val arcLen2 = Imgproc.arcLength(contourFloat, true) * 0.1
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(contourFloat, approx, arcLen2, true)
                rectangles.add(approx)
            }
            var biggestContourIndex = 0
            var polyArea = 0.0
            for (i in rectangles.indices) {
                val area = Imgproc.contourArea(rectangles[i])
                if (area > polyArea) {
                    polyArea = area
                    biggestContourIndex = i
                }
            }
            val biggestContourClosed = rectangles[biggestContourIndex]
            largestRectangles.add(biggestContourClosed)
        }
        //used for testing the bounded rect points
        /*for(MatOfPoint2f rect : largestRectangles ){
            Log.d("IMAGE_PROCESSING_RECT",Arrays.toString(rect.toArray()) + "******");
            Rect boundingrect = Imgproc.boundingRect(rect);
            Imgproc.rectangle(gray,new Point(boundingrect.x,boundingrect.y), new Point(boundingrect.x+boundingrect.width,boundingrect.y+boundingrect.height),new Scalar(0,0,255),10);
        }*/if (largestRectangles.size > 0) {
            for (i in largestRectangles.indices) {
                if (largestRectangles[i].toArray().size >= 4) {
                    //used for testing the bounded rect points
                    /* Log.d("IMAGE_PROCESSING_ILRECT",Arrays.toString(largestRectangles.get(i).toArray()) + "******");
                    Rect boundingrect = Imgproc.boundingRect(largestRectangles.get(i));
                    Imgproc.rectangle(src,new Point(boundingrect.x,boundingrect.y), new Point(boundingrect.x+boundingrect.width,boundingrect.y+boundingrect.height),new Scalar(255,0,0),5);
                    processedBitmap = ImageUtils.matToBitmap(src);*/
                    return largestRectangles[i]
                }
            }
            return largestRectangles[0]
        }
        return null
    }

    /**
     * get the corner points for entire screen irrespective of document edges
     * @param holderWidth
     * @param holderHeight
     * @return
     */
    fun getPointsFullScreen(holderWidth: Int, holderHeight: Int): MatOfPoint2f {
        val rectTlFull = Point(40.0, 40.0)
        val rectTrFull =
            Point((holderWidth - 40).toDouble(), 40.0)
        val rectBrFull =
            Point((holderWidth - 40).toDouble(), (holderHeight - 40).toDouble())
        val rectBlFull =
            Point(40.0, (holderHeight - 40).toDouble())
        val boundingRectPointsFull =
            arrayOfNulls<Point>(4)
        boundingRectPointsFull[0] = rectTlFull
        boundingRectPointsFull[1] = rectTrFull
        boundingRectPointsFull[2] = rectBrFull
        boundingRectPointsFull[3] = rectBlFull
        return MatOfPoint2f(*boundingRectPointsFull)
    }

    /**
     * get the corner points for entire screen irrespective of document edges
     * @param holderWidth
     * @param holderHeight
     * @return
     */
    fun getRotateImagePointsFullScreen(holderWidth: Int, holderHeight: Int): MatOfPoint2f {
        val rectTlFull = Point(180.0, 180.0)
        val rectTrFull =
            Point((holderWidth - 180).toDouble(), 180.0)
        val rectBrFull = Point(
            (holderWidth - 180).toDouble(),
            (holderHeight - 180).toDouble()
        )
        val rectBlFull =
            Point(180.0, (holderHeight - 180).toDouble())
        val boundingRectPointsFull =
            arrayOfNulls<Point>(4)
        boundingRectPointsFull[0] = rectTlFull
        boundingRectPointsFull[1] = rectTrFull
        boundingRectPointsFull[2] = rectBrFull
        boundingRectPointsFull[3] = rectBlFull
        return MatOfPoint2f(*boundingRectPointsFull)
    }

    companion object {
        init {
            OpenCVLoader.initDebug()
        }

        private const val DOWNSCALE_IMAGE_SIZE =
            600.0 //850f; the smaller the downscaled image, the faster it processes
    }
}