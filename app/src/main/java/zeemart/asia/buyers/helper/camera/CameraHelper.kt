package zeemart.asia.buyers.helper.camera

import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.Log
import android.view.Surface
import android.view.WindowManager

/**
 * Created by ParulBhandari on 6/18/2018.
 */
object CameraHelper {
    fun getCamRotationDegrees(context: Context, mCamera: Camera?): Int {
        if (mCamera == null) {
            Log.d("", "getCamRotationDegrees - camera null")
            return 0
        }
        val info = CameraInfo()
        val cameraID = cameraId
        Camera.getCameraInfo(cameraID, info)
        val winManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = winManager.defaultDisplay.rotation
        var degrees = 0
        degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        return result
    }

    val cameraId: Int
        get() {
            var cameraId = -1
            val numberOfCameras = Camera.getNumberOfCameras()
            for (i in 0 until numberOfCameras) {
                val info = CameraInfo()
                Camera.getCameraInfo(i, info)
                if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                    Log.d("BACK CAMERA FOUND", "Camera found")
                    cameraId = i
                    break
                }
            }
            return cameraId
        }

    fun setImageOrientationFromSensorDetector(mCamera: Camera?, sensorDetectionAngle: Int): Int {
        val info = CameraInfo()
        Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info)
        var params: Camera.Parameters? = null
        val degrees: Int
        degrees = when (sensorDetectionAngle) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val rotate = (info.orientation - degrees + 360) % 360
        if (mCamera != null) {
            params = mCamera.parameters
            params.setRotation(rotate)
            mCamera.parameters = params
        }
        Log.d("ORIENTATIONS_IMAGE", "")
        return rotate
    }

    fun setImageOrientation(mCamera: Camera?, activity: Activity): Int {
        val info = CameraInfo()
        Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        var params: Camera.Parameters? = null
        degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val rotate = (info.orientation - degrees + 360) % 360
        if (mCamera != null) {
            params = mCamera.parameters
            params.setRotation(rotate)
            mCamera.parameters = params
        }
        return rotate
    }

    /**
     * method to get the optimal preview size
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    @JvmStatic
    fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = w.toDouble() / h
        if (sizes == null) return null
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        // Try to find an size match aspect ratio and size
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                val sizeHeight = size.height
                if (Math.abs(sizeHeight - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(sizeHeight - h).toDouble()
                }
            }
        }
        return optimalSize
    }

    fun hasFlash(camera: Camera?): Boolean {
        if (camera == null) {
            return false
        }
        val parameters = camera.parameters
        if (parameters.flashMode == null) {
            return false
        }
        val supportedFlashModes = parameters.supportedFlashModes
        return if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size == 1 && supportedFlashModes[0] == Camera.Parameters.FLASH_MODE_OFF
        ) {
            false
        } else true
    }
}