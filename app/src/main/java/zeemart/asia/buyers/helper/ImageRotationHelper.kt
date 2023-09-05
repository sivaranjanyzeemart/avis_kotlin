package zeemart.asia.buyers.helper

import android.app.Activity
import android.content.*
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.view.*
import java.io.IOException
import java.io.InputStream

/**
 * Created by ParulBhandari on 5/4/2018.
 */
object ImageRotationHelper {
    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    @Throws(IOException::class)
    fun handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri?): Bitmap? {
        val display: Display = (context as Activity).getWindowManager().getDefaultDisplay()
        val size: Point = Point()
        display.getSize(size)
        val width: Int = size.x
        val height: Int = size.y
        val MAX_HEIGHT: Int = height
        val MAX_WIDTH: Int = width

        // First decode with inJustDecodeBounds=true to check dimensions
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var imageStream: InputStream? =
            context.getContentResolver().openInputStream((selectedImage)!!)
        BitmapFactory.decodeStream(imageStream, null, options)
        imageStream!!.close()

        // Calculate inSampleSize
        options.inSampleSize =
            ImageRotationHelper.calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        imageStream = context.getContentResolver().openInputStream((selectedImage))
        var img: Bitmap? = BitmapFactory.decodeStream(imageStream, null, options)
        img = ImageRotationHelper.rotateImageIfRequired(context, img!!, selectedImage)
        return img
    }

    /**
     * Calculate an inSampleSize for use in a [BitmapFactory.Options] object when decoding
     * bitmaps using the decode* methods from [BitmapFactory]. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     * method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height: Int = options.outHeight
        val width: Int = options.outWidth
        var inSampleSize: Int = 1
        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            val heightRatio: Int = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio: Int = Math.round(width.toFloat() / reqWidth.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).
            val totalPixels: Float = (width * height).toFloat()

            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap: Float = (reqWidth * reqHeight * 2).toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
        }
        return inSampleSize
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    @Throws(IOException::class)
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input: InputStream? = context.getContentResolver().openInputStream(selectedImage)
        val ei: ExifInterface
        if (Build.VERSION.SDK_INT > 23) ei = ExifInterface((input)!!) else ei = ExifInterface(
            (selectedImage.getPath())!!
        )
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return ImageRotationHelper.rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> return ImageRotationHelper.rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> return ImageRotationHelper.rotateImage(img, 270)
            else -> return img
        }
    }

    fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix: Matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg: Bitmap =
            Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true)
        img.recycle()
        return rotatedImg
    }
}