package zeemart.asia.buyers.helper.camera

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.AutoFocusCallback
import android.hardware.Camera.PreviewCallback
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import zeemart.asia.buyers.helper.camera.CameraHelper.getOptimalPreviewSize

/**
 * Created by ParulBhandari on 6/13/2018.
 */
class CameraPreview(
    context: Context?,
    private val mCamera: Camera?,
    private val callback: PreviewCallback
) : SurfaceView(context), SurfaceHolder.Callback {
    private val mHolder: SurfaceHolder
    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder)
                mCamera.startPreview()
            }
        } catch (e: Exception) {
            Log.d("", "Error setting camera preview: " + e.message)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        /* if(mCamera != null)
        {
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                Log.d("", "Error Stopping camera preview: " + e.getMessage());
            }
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }*/
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera!!.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera!!.setPreviewDisplay(mHolder)
            mCamera.startPreview()
            mCamera.setPreviewCallback(callback)
            val params = mCamera.parameters
            val focusModes = params.supportedFocusModes
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                // set the focus mode
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
            val size = getOptimalPreviewSize(params.supportedPictureSizes, w, h)
            params.setPictureSize(size!!.width, size.height)
            mCamera.parameters = params
        } catch (e: Exception) {
            Log.d("", "Error starting camera preview: " + e.message)
        }
    }

    /**
     * callback for auto focusn
     */
    var myAutoFocusCallback = AutoFocusCallback { arg0, arg1 ->
        if (arg0) {
            mCamera!!.cancelAutoFocus()
        }
    }

    init {

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    /**
     * when user touch to focus
     * @param tfocusRect
     */
    fun doTouchFocus(tfocusRect: Rect?) {
        try {
            val focusList: MutableList<Camera.Area> = ArrayList()
            val focusArea = Camera.Area(tfocusRect, 1000)
            focusList.add(focusArea)
            val param = mCamera!!.parameters
            if (param.maxNumMeteringAreas > 0) {
                param.meteringAreas = focusList
            }
            if (param.maxNumFocusAreas > 0) {
                param.focusAreas = focusList
            }
            mCamera.parameters = param
            mCamera.autoFocus(myAutoFocusCallback)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("Camera Preview", "Unable to autofocus")
        }
    }

    /**
     * when user touch the camera preview screen
     * @param event
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            val touchRect =
                Rect((x - 100).toInt(), (y - 100).toInt(), (x + 100).toInt(), (y + 100).toInt())
            val targetFocusRect = Rect(
                touchRect.left * 2000 / this.width - 1000,
                touchRect.top * 2000 / this.height - 1000,
                touchRect.right * 2000 / this.width - 1000,
                touchRect.bottom * 2000 / this.height - 1000
            )
            doTouchFocus(targetFocusRect)
        }
        return false
    }
}